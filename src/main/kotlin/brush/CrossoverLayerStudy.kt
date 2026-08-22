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
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.material.ScalingEquationOfState
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.ln
import kotlin.math.pow
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Task `T-1c` / leaf `A2.1` — the layer response re-derived from a **crossover-valid free energy**
 * rather than from a fixed osmotic exponent, with the Alexander-de Gennes height relation
 * replaced by a free-energy minimisation that does not assume blobs.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=brush.CrossoverLayerStudyKt
 * ```
 *
 * Emits `gpd/results/T-1c-crossover-valid-layer-response.json`, deterministically — no timestamp,
 * so a re-run that changes nothing produces no diff.
 */

/**
 * Which interaction free energy a response was computed with.
 *
 * All three are anchored in published measurement on PEG in water, and they bracket the layer's
 * own volume fraction from both sides of the dilute-semidilute crossover. Which of them is right
 * *there* is exactly what is not measured, so the spread between them is reported as the answer's
 * uncertainty rather than resolved by choosing.
 */
private enum class InteractionChoice(val label: String, val exponent: String) {

    /** The unscreened dilute limb alone, `Π = (B/2)(k_BT/v₀)φ²`, `B` from the measured `A₂`. */
    TWO_BODY("two-body", "2"),

    /** The measured virial description, `A₂` and `A₃` together — exponents 2 and 3. */
    VIRIAL("virial", "2 + 3"),

    /** The screened semidilute limb of the measured equation of state, `Π = α(k_BT/v₀)φ^(9/4)`. */
    DES_CLOIZEAUX("des-Cloizeaux", "9/4")
}

/** Which family of density profiles the free energy was minimised over. */
private enum class ProfileChoice(val label: String) {
    BOX("alexander-box"),
    STRONG_STRETCHING("strong-stretching")
}

/** The response of one (profile, interaction) pair at one point of the design space. */
@Serializable
data class CrossoverLayerResponse(

    /** The profile family the free energy was minimised over. */
    val profile: String,

    /** The interaction free energy that was minimised. */
    val interaction: String,

    /** The osmotic exponent that interaction carries — a consequence, not a setting. */
    val interactionExponent: String,

    /** `B = v/v₀`, the dimensionless two-body coefficient this response was evaluated at. */
    val secondVirialCoefficient: Double,

    /** `N` — inverted from the specified layer height through THIS model, not through Alexander-de Gennes. */
    val monomersPerChain: Double,

    /** `N M₀` in g/mol. */
    val chainMolarMass: Double,

    /** `L₀/N` in nm — the derived height relation, with no convention prefactor left in it. */
    val heightPerMonomer: Double,

    /** `φ = N σ v₀ / L₀`, the mean physical volume fraction of the unperturbed layer. */
    val meanVolumeFraction: Double,

    /** `φ#` of the **bulk** equation of state at this chain length. */
    val crossoverVolumeFraction: Double,

    /** `φ/φ#` — where the layer sits on the bulk crossover, reported for continuity with `C-0002`. */
    val volumeFractionInCrossoverUnits: Double,

    /** `d lnΠ/d lnφ` of the **bulk** equation of state — NOT the exponent this layer's pressure has. */
    val bulkLocalExponent: Double,

    /**
     * `d lnΠ_int/d lnφ` of the **grafted layer's own** interaction, at the unperturbed height.
     *
     * This is the quantity `CH-0001` needed and did not have. It never falls below 2, because
     * the term that drags the bulk exponent down towards 1 is chain translational entropy, and
     * a grafted layer has none.
     */
    val interactionLocalExponent: Double,

    /** `d lnΠ_int/d lnφ` at the working height under the §3 target force. */
    val workingInteractionLocalExponent: Double,

    /** `R₀ = b √N_K` in nm. */
    val idealEndToEnd: Double,

    /** `L₀/R₀` — the strong-stretching premise, as a number. */
    val stretchingRatio: Double,

    /** `k(L₀)` in `pN/nm`. Zero for the strong-stretching profile, whose outer edge is diffuse. */
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
    val workingVolumeFraction: Double,

    /** `φ/φ#` at the working height. */
    val workingVolumeFractionInCrossoverUnits: Double,

    /** `d ln k_secant / d ln σ` at fixed layer height. */
    val stiffnessSensitivity: Double
)

/** One point of the (layer height, grafting density) design space. */
@Serializable
data class CrossoverDesignPoint(
    val layerHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val responses: List<CrossoverLayerResponse>
)

/**
 * The `σ` interval at one layer height over which a model satisfies both stated criteria.
 *
 * Two criteria, because the stroke alone bounds the window only from **above**. `C-0001`'s lower
 * edge came from `Σ ≥ 5`, which `CH-0001` disqualified thermodynamically and `CH-0003`
 * geometrically, so a replacement is needed and the weakest defensible one is used here:
 * the layer must be stretched beyond the chain's own unperturbed size at all, `L₀/R₀ ≥ 1`.
 * Windows are also emitted with that requirement switched off, so the two contributions are
 * separable by whoever consumes them.
 */
@Serializable
data class StrokeWindow(
    val layerHeight: Double,
    val profile: String,
    val interaction: String,
    val requiredStroke: Double,
    val requiredStretchingRatio: Double,
    val lowestGraftingDensity: Double?,
    val highestGraftingDensity: Double?,
    val empty: Boolean
)

/**
 * How the layer response moves when the interaction free energy is scaled by a constant.
 *
 * This is the sensitivity `C-0007` asks every claim built on a **bulk** solution property to
 * state, now that an SCF fit to neutron reflectivity puts the effective `χ` of a grafted PEO
 * layer at ~0.60 against ~0.372 in bulk. At fixed layer height, grafting density and compression
 * ratio, the exact answer is `k ∝ K^(1/(m+1))` and `N ∝ K^(−1/(m+1))`, because the chain length
 * the specified height demands moves against the interaction and very nearly cancels it.
 */
@Serializable
data class InteractionStrengthSensitivity(
    val scale: Double,
    val monomersPerChain: Double,
    val heightPerMonomer: Double,
    val meanVolumeFraction: Double,
    val stretchingRatio: Double,
    val stiffnessAtFourFifths: Double,
    val strokeUnderTargetForce: Double,
    val secantStiffness: Double
)

/** The numbers that decide whether the semidilute premise had to be there at all. */
@Serializable
data class PremiseDiagnostics(
    val osmoticSecondVirial: Double,
    val osmoticThirdVirial: Double,
    val measuredSecondVirialCoefficient: Double,
    val measuredExcludedVolume: Double,
    val measuredThirdVirialCoefficient: Double,
    val floryHugginsChi: Double,
    val matchedSecondVirialCoefficient: Double,
    val matchedOverMeasured: Double,
    val alexanderDeGennesImpliedSecondVirialCoefficient: Double,
    val alexanderDeGennesImpliedExcludedVolume: Double,
    val alexanderDeGennesImpliedOverMeasured: Double,
    val thermalBlobKuhnSegments: Double,
    val thermalBlobMonomers: Double,
    val thermalBlobMolarMass: Double,
    val chainKuhnSegments: Double,
    val chainThermalBlobs: Double,
    val interactionCrossoverVolumeFraction: Double,
    val bulkCrossoverVolumeFraction: Double,
    val layerVolumeFraction: Double,
    val layerOverInteractionCrossover: Double,
    val twoBodyPressureAtLayer: Double,
    val virialPressureAtLayer: Double,
    val desCloizeauxPressureAtLayer: Double,
    val desCloizeauxOverVirial: Double
)

/** Like for like against `C-0001`, at the design points `C-0002` placed on the equation of state. */
@Serializable
data class StandingClaimComparison(
    val label: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val alexanderDeGennesChainLength: Double,
    val crossoverValidChainLength: Double,
    val chainLengthRatio: Double,
    val standingEquilibriumStiffness: Double,
    val crossoverValidStiffnessAtFourFifths: Double,
    val standingStrokeAtTargetForce: Double,
    val crossoverValidStrokeAtTargetForce: Double,
    val strokeRatio: Double,
    val standingVolumeFractionInCrossoverUnits: Double,
    val crossoverValidVolumeFractionInCrossoverUnits: Double
)

@Serializable
data class CrossoverLayerResult(
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
    val premiseDiagnostics: PremiseDiagnostics,
    val standingClaimComparison: List<StandingClaimComparison>,
    val strokeWindows: List<StrokeWindow>,
    val interactionStrengthSensitivity: List<InteractionStrengthSensitivity>,
    val predictedStiffnessExponentInInteractionStrength: Double,
    val designPoints: List<CrossoverDesignPoint>
)

private const val TILE_EDGE = 40.0

private const val TARGET_FORCE = 100.0

private const val ACCEPTABLE_STROKE = 3.0

private const val DESIRED_STROKE = 10.0

private val LAYER_HEIGHTS = listOf(5.0, 7.0, 10.0)

private const val GRAFTING_DENSITY_MIN = 0.002

private const val GRAFTING_DENSITY_MAX = 1.0

private const val GRAFTING_DENSITY_SAMPLES = 61

private const val PROFILE_PANELS = 1024

/** The `C-0001` design points, so the comparison is like for like rather than re-sited. */
private val STANDING_DESIGN_POINTS = listOf(
    Triple("L0 = 5 nm, brush onset", 5.0, 0.092),
    Triple("L0 = 7 nm, brush onset", 7.0, 0.045),
    Triple("L0 = 10 nm, window lower edge", 10.0, 0.024),
    Triple("L0 = 10 nm, window upper edge", 10.0, 0.030)
)

private val SWEPT_MODELS = listOf(
    ProfileChoice.BOX to InteractionChoice.TWO_BODY,
    ProfileChoice.BOX to InteractionChoice.VIRIAL,
    ProfileChoice.BOX to InteractionChoice.DES_CLOIZEAUX,
    ProfileChoice.STRONG_STRETCHING to InteractionChoice.TWO_BODY,
    ProfileChoice.STRONG_STRETCHING to InteractionChoice.VIRIAL,
    ProfileChoice.STRONG_STRETCHING to InteractionChoice.DES_CLOIZEAUX
)

/**
 * `A₂` in `mol·cm³/g²` for PEG in water at 25 °C, convention `Π/(RT) = c/M + A₂c² + A₃c³`,
 * `c` in `g/cm³`, **no factor of two**.
 *
 * MEASURED. Hasse, Kany, Tintinger & Maurer, *Macromolecules* 28:3540 (1995), for Mw = 6902 —
 * read from the re-tabulation in A. Shvets, *Theory of colloidal stabilization by unattached
 * polymers*, PhD thesis, Univ. de Strasbourg, arXiv:2010.08110, Table 2.3, because the original
 * is paywalled. Corroborated by three independent measurements read directly:
 * `2.34e-3` at 20.4 kDa (Li et al., *Polymer* 80:205 (2015), Table I, membrane osmometry, 25 °C),
 * `1.715e-3` (Kany 1998 via Grünfelder, Diss. Kaiserslautern 2002, Table 4.3, isopiestic, 25 °C),
 * and `2.1e-3` at 20 kDa (Cohen & Highsmith, *Biophys. J.* 73:1689 (1997), Table 1, after the
 * `×10` conversion out of their `g/dl` convention).
 *
 * The spread across the four is 1.7-2.3e-3, i.e. ±15%, and the molar-mass dependence over
 * 2-20 kDa is reported by different groups as `M^(−0.2)`, `M^(−0.32)`, saturating, and absent.
 * It is treated here as constant over our range, which the ±15% spread already covers.
 */
private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

/** `A₃` in `cm⁶·mol/g³`, same convention and same source chain (Kany via Grünfelder, Table 4.3). */
private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

fun main() {
    val peg = PegWater()
    val tileArea = TILE_EDGE * TILE_EDGE
    val densities = logarithmicSweep(
        GRAFTING_DENSITY_MIN, GRAFTING_DENSITY_MAX, GRAFTING_DENSITY_SAMPLES
    )
    val designPoints = LAYER_HEIGHTS.flatMap { height ->
        densities.map { density ->
            CrossoverDesignPoint(
                layerHeight = height,
                graftingDensity = density,
                graftingSpacing = 1.0 / density.pow(0.5),
                responses = SWEPT_MODELS.map { (profile, choice) ->
                    response(peg, profile, choice, height, density, tileArea)
                }
            )
        }
    }
    val result = CrossoverLayerResult(
        task = "T-1c",
        leaf = "A2.1",
        title = "Layer response from a crossover-valid free energy, not a fixed osmotic exponent",
        verificationType = "in-silico (analytic derivation + numeric minimisation), " +
                "against the measured PEG/water equation of state of C-0002",
        acceptance = "stiffness and stroke re-derived from an interaction free energy valid " +
                "across the dilute-semidilute crossover; the Alexander-de Gennes height relation " +
                "either justified at phi/phi# ~ 1 or replaced; N(L0) no longer resting on the " +
                "failed semidilute premise; the spread between admissible free energies reported " +
                "as the uncertainty",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "The interaction free energy is anchored to measurement; nothing about THIS " +
                "layer is measured.",
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
            "the tile is a rigid non-adsorbing wall at height h; compression means h < L0",
            "disjoining pressure positive when the layer pushes the tile away",
            "stiffness k = -dF/dh = -A dP/dh, positive for a restoring layer",
            "the layer height is the independent variable; N follows from it, by inverting the " +
                    "free-energy minimum of EACH model rather than the Alexander-de Gennes relation",
            "phi ALWAYS means the physical volume fraction N sigma v0 / h",
            "a GRAFTED layer carries no chain translational entropy, so the van't Hoff limb of " +
                    "the measured equation of state is removed before the free energy is used"
        ),
        validity = listOf(
            "N sigma v0 < h <= L0 — below the dry thickness the volume fraction would exceed 1, " +
                    "above L0 a non-adsorbing layer loses contact and the pressure is zero",
            "STRONG STRETCHING IS NOT SATISFIED: L0/R0 stays between 0.6 and 2.2 over the whole " +
                    "5-10 nm x 0.002-1.0 nm^-2 box, where the theory wants >> 1. Both profile " +
                    "models are therefore used outside their premise and the spread between them " +
                    "is a lower bound on the profile uncertainty, not a full error bar",
            "the interaction free energy below phi# is NOT measured: the fitted alpha phi^(9/4) " +
                    "limb is constrained by data only where it dominates the van't Hoff limb. " +
                    "The two-body limb and the des Cloizeaux limb are carried as a bracket and " +
                    "B is fixed by matching them at phi#, NOT by an independent A2",
            "the compressed strong-stretching profile is the truncated parabola; the known free-end " +
                    "dead zone near the wall is not resolved",
            "the equation of state was fitted to LINEAR PEG in PURE WATER at 20 C; the Gen-1 " +
                    "buffer is 2-10 mM MgCl2 and the §3 chemistry allows a PS->PEG block copolymer",
            "purely mechanical: no electrostatics, no ion partitioning, no poroelasticity, " +
                    "no tile compliance"
        ),
        parameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "medium" to "aqueous buffer, 2/5/10 mM MgCl2 (not yet entering this task)",
            "thermalEnergy" to thermalEnergy().toString(),
            "thermalEnergyElectronVolts" to (thermalEnergy() / ELECTRON_VOLT).roundedForProse().toString(),
            "tileFootprint" to "${TILE_EDGE.toInt()} x ${TILE_EDGE.toInt()} nm",
            "tileArea" to tileArea.toString(),
            "targetForce" to TARGET_FORCE.toString(),
            "targetStrokeAcceptable" to ACCEPTABLE_STROKE.toString(),
            "targetStrokeDesired" to DESIRED_STROKE.toString(),
            "monomerVolume" to peg.monomerVolume.roundedForProse().toString(),
            "monomerVolumeProvenance" to "DERIVED from M0 and the cited partial specific volume (C-0002)",
            "kuhnLength" to peg.kuhnLength.toString(),
            "monomersPerKuhnSegment" to peg.monomersPerKuhnSegment.roundedForProse().toString(),
            "kuhnProvenance" to "CITED, Rubinstein & Colby Table 2.1 (PEO) — independent of the " +
                    "failed semidilute premise, which is why the elasticity is written on it",
            "crossoverIndex" to peg.crossoverIndex.toString(),
            "crossoverIndexProvenance" to "MEASURED, Cohen et al. (2009), 12 PEG molar masses, " +
                    "0-50 wt %, r^2 = 0.9926",
            "osmoticSecondVirial" to OSMOTIC_SECOND_VIRIAL.toString(),
            "osmoticThirdVirial" to OSMOTIC_THIRD_VIRIAL.toString(),
            "virialProvenance" to "MEASURED, PEG/water 25 C, convention Pi/(RT) = c/M + A2 c^2 " +
                    "+ A3 c^3 with c in g/cm^3 and NO factor of two. A2 = 1.9e-3 mol cm^3/g^2 " +
                    "(Hasse et al., Macromolecules 28:3540 (1995), Mw = 6902, read via the " +
                    "re-tabulation in Shvets, arXiv:2010.08110 Table 2.3 — SECONDARY), " +
                    "corroborated by 2.34e-3 (Li et al., Polymer 80:205 (2015) Table I, read " +
                    "directly), 1.715e-3 (Kany via Gruenfelder, Diss. Kaiserslautern 2002 " +
                    "Table 4.3) and 2.1e-3 (Cohen & Highsmith, Biophys. J. 73:1689 (1997) " +
                    "Table 1). Spread +/-15%.",
            "layerHeights" to LAYER_HEIGHTS.toString(),
            "graftingDensityRange" to listOf(GRAFTING_DENSITY_MIN, GRAFTING_DENSITY_MAX).toString(),
            "graftingDensitySamples" to GRAFTING_DENSITY_SAMPLES.toString(),
            "profilePanels" to PROFILE_PANELS.toString()
        ),
        premiseDiagnostics = premiseDiagnostics(peg),
        standingClaimComparison = STANDING_DESIGN_POINTS.map { (label, height, density) ->
            comparison(peg, label, height, density, tileArea)
        },
        strokeWindows = strokeWindows(designPoints),
        interactionStrengthSensitivity = INTERACTION_SCALES.map {
            interactionStrengthSensitivity(peg, it, tileArea)
        },
        predictedStiffnessExponentInInteractionStrength =
            1.0 / (ScalingEquationOfState.DES_CLOIZEAUX_EXPONENT + 1.0),
        designPoints = designPoints
    )
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-1c-crossover-valid-layer-response.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).withEmissionHeader(LatticeTag.NONE, null)
        ) + "\n"
    )
    report(result, output)
}

/** `d lnΠ/d lnφ` of [interaction] at [volumeFraction]. */
private fun localExponent(interaction: InteractionFreeEnergy, volumeFraction: Double): Double =
    interaction.osmoticPressureSlope(volumeFraction) * volumeFraction /
            interaction.osmoticPressure(volumeFraction)

private fun interactionFor(
    peg: PegWater,
    choice: InteractionChoice
): InteractionFreeEnergy {
    val twoBody = twoBodyInteraction(
        peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL), peg.monomerVolume
    )
    val threeBody = threeBodyInteraction(
        peg.reducedThirdVirialCoefficient(OSMOTIC_THIRD_VIRIAL), peg.monomerVolume
    )
    return when (choice) {
        InteractionChoice.TWO_BODY -> twoBody
        InteractionChoice.VIRIAL -> additiveInteraction("virial", listOf(twoBody, threeBody))
        InteractionChoice.DES_CLOIZEAUX ->
            desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)
    }
}

private fun layerModel(
    profile: ProfileChoice,
    interaction: InteractionFreeEnergy
): GraftedLayerModel = when (profile) {
    ProfileChoice.BOX -> AlexanderBoxLayer(interaction)
    ProfileChoice.STRONG_STRETCHING -> StrongStretchingLayer(interaction, PROFILE_PANELS)
}

/**
 * Returns the model and the chain length that produce a layer of exactly [layerHeight].
 *
 * This is the inversion that replaces `N = L₀/(a^(5/3)σ^(1/3))`. Every interaction here has a
 * measured, chain-length-independent coefficient, so no self-consistency loop is needed: the
 * height relation is inverted once, against the free-energy minimum of the model being used.
 */
private fun consistentModel(
    peg: PegWater,
    profile: ProfileChoice,
    choice: InteractionChoice,
    layerHeight: Double,
    graftingDensity: Double
): Pair<GraftedLayerModel, Double> {
    val model = layerModel(profile, interactionFor(peg, choice))
    return model to model.chainLengthForHeight(peg, layerHeight, graftingDensity)
}

private fun response(
    peg: PegWater,
    profile: ProfileChoice,
    choice: InteractionChoice,
    layerHeight: Double,
    graftingDensity: Double,
    tileArea: Double
): CrossoverLayerResponse {
    val (model, length) = consistentModel(peg, profile, choice, layerHeight, graftingDensity)
    val chain = peg.graftedChain(length, graftingDensity)
    val equilibrium = model.equilibriumHeight(chain)
    val working = model.heightUnderLoad(chain, TARGET_FORCE, tileArea)
    val equationOfState = peg.equationOfState(length)
    val volumeFraction = chain.meanVolumeFraction(equilibrium)
    val workingVolumeFraction = chain.meanVolumeFraction(working)
    val tangent = model.stiffness(chain, working, tileArea)
    return CrossoverLayerResponse(
        profile = profile.label,
        interaction = choice.label,
        interactionExponent = choice.exponent,
        secondVirialCoefficient = peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL),
        monomersPerChain = length,
        chainMolarMass = length * peg.monomerMolarMass,
        heightPerMonomer = equilibrium / length,
        meanVolumeFraction = volumeFraction,
        crossoverVolumeFraction = equationOfState.crossoverVolumeFraction,
        volumeFractionInCrossoverUnits = volumeFraction /
                equationOfState.crossoverVolumeFraction,
        bulkLocalExponent = equationOfState.localExponent(volumeFraction),
        interactionLocalExponent = localExponent(model.interaction, volumeFraction),
        workingInteractionLocalExponent = localExponent(model.interaction, workingVolumeFraction),
        idealEndToEnd = chain.idealEndToEnd,
        stretchingRatio = chain.stretchingRatio(equilibrium),
        equilibriumStiffness = model.stiffness(chain, equilibrium, tileArea),
        stiffnessAtNineTenths = model.stiffness(chain, 0.9 * equilibrium, tileArea),
        stiffnessAtFourFifths = model.stiffness(chain, 0.8 * equilibrium, tileArea),
        stiffnessAtSevenTenths = model.stiffness(chain, 0.7 * equilibrium, tileArea),
        heightUnderTargetForce = working,
        strokeUnderTargetForce = equilibrium - working,
        secantStiffness = TARGET_FORCE / (equilibrium - working),
        tangentStiffness = tangent,
        positionalRms = equipartitionRms(tangent),
        workingVolumeFraction = workingVolumeFraction,
        workingVolumeFractionInCrossoverUnits = workingVolumeFraction /
                equationOfState.crossoverVolumeFraction,
        stiffnessSensitivity = sensitivity(peg, profile, choice, layerHeight, graftingDensity, tileArea)
    )
}

/**
 * Returns `d ln k_secant / d ln σ` at fixed layer height, by central difference over a ±1%
 * perturbation of the grafting density — the same estimator `T-1` used, so the two are comparable.
 */
private fun sensitivity(
    peg: PegWater,
    profile: ProfileChoice,
    choice: InteractionChoice,
    layerHeight: Double,
    graftingDensity: Double,
    tileArea: Double
): Double {
    val perturbation = 0.01
    fun secantAt(factor: Double): Double {
        val (model, length) = consistentModel(
            peg, profile, choice, layerHeight, graftingDensity * factor
        )
        val chain = peg.graftedChain(length, graftingDensity * factor)
        val equilibrium = model.equilibriumHeight(chain)
        val stroke = equilibrium - model.heightUnderLoad(chain, TARGET_FORCE, tileArea)
        return TARGET_FORCE / stroke
    }
    return (ln(secantAt(1.0 + perturbation)) - ln(secantAt(1.0 - perturbation))) /
            (ln(1.0 + perturbation) - ln(1.0 - perturbation))
}

private fun premiseDiagnostics(peg: PegWater): PremiseDiagnostics {
    // evaluated at the C-0001 10 nm window lower edge, re-derived through the primary model
    val (_, length) = consistentModel(
        peg, ProfileChoice.STRONG_STRETCHING, InteractionChoice.DES_CLOIZEAUX, 10.0, 0.024
    )
    val chain = peg.graftedChain(length, 0.024)
    val volumeFraction = chain.meanVolumeFraction(10.0)
    val measured = peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL)
    val third = peg.reducedThirdVirialCoefficient(OSMOTIC_THIRD_VIRIAL)
    val matched = matchedSecondVirialCoefficient(peg.crossoverIndex, length)
    val implied = alexanderDeGennesImpliedSecondVirialCoefficient(peg)
    val blob = peg.thermalBlobKuhnSegments(measured)
    val twoBody = twoBodyInteraction(measured, peg.monomerVolume)
    val virial = additiveInteraction(
        "virial", listOf(twoBody, threeBodyInteraction(third, peg.monomerVolume))
    )
    val desCloizeaux = desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)
    return PremiseDiagnostics(
        osmoticSecondVirial = OSMOTIC_SECOND_VIRIAL,
        osmoticThirdVirial = OSMOTIC_THIRD_VIRIAL,
        measuredSecondVirialCoefficient = measured,
        measuredExcludedVolume = measured * peg.monomerVolume,
        measuredThirdVirialCoefficient = third,
        floryHugginsChi = floryHugginsChi(measured),
        matchedSecondVirialCoefficient = matched,
        matchedOverMeasured = matched / measured,
        alexanderDeGennesImpliedSecondVirialCoefficient = implied,
        alexanderDeGennesImpliedExcludedVolume = implied * peg.monomerVolume,
        alexanderDeGennesImpliedOverMeasured = implied / measured,
        thermalBlobKuhnSegments = blob,
        thermalBlobMonomers = blob * peg.monomersPerKuhnSegment,
        thermalBlobMolarMass = blob * peg.monomersPerKuhnSegment * peg.monomerMolarMass,
        chainKuhnSegments = chain.kuhnSegments,
        chainThermalBlobs = chain.kuhnSegments / blob,
        interactionCrossoverVolumeFraction = (measured / (2.0 * peg.crossoverIndex)).pow(4.0),
        bulkCrossoverVolumeFraction = peg.equationOfState(length).crossoverVolumeFraction,
        layerVolumeFraction = volumeFraction,
        layerOverInteractionCrossover = volumeFraction /
                (measured / (2.0 * peg.crossoverIndex)).pow(4.0),
        twoBodyPressureAtLayer = twoBody.osmoticPressure(volumeFraction),
        virialPressureAtLayer = virial.osmoticPressure(volumeFraction),
        desCloizeauxPressureAtLayer = desCloizeaux.osmoticPressure(volumeFraction),
        desCloizeauxOverVirial = desCloizeaux.osmoticPressure(volumeFraction) /
                virial.osmoticPressure(volumeFraction)
    )
}

private fun comparison(
    peg: PegWater,
    label: String,
    layerHeight: Double,
    graftingDensity: Double,
    tileArea: Double
): StandingClaimComparison {
    val standingBrush = brushOfHeight(layerHeight, graftingDensity, peg.effectiveMonomerLength)
    val standing = DeGennesScaling()
    val standingWorking = standing.heightUnderLoad(standingBrush, TARGET_FORCE, tileArea)
    val standingEquationOfState = peg.equationOfState(standingBrush.monomersPerChain)
    val fresh = response(
        peg, ProfileChoice.STRONG_STRETCHING, InteractionChoice.DES_CLOIZEAUX,
        layerHeight, graftingDensity, tileArea
    )
    return StandingClaimComparison(
        label = label,
        layerHeight = layerHeight,
        graftingDensity = graftingDensity,
        alexanderDeGennesChainLength = standingBrush.monomersPerChain,
        crossoverValidChainLength = fresh.monomersPerChain,
        chainLengthRatio = fresh.monomersPerChain / standingBrush.monomersPerChain,
        standingEquilibriumStiffness = standing.stiffness(standingBrush, layerHeight, tileArea),
        crossoverValidStiffnessAtFourFifths = fresh.stiffnessAtFourFifths,
        standingStrokeAtTargetForce = layerHeight - standingWorking,
        crossoverValidStrokeAtTargetForce = fresh.strokeUnderTargetForce,
        strokeRatio = fresh.strokeUnderTargetForce / (layerHeight - standingWorking),
        standingVolumeFractionInCrossoverUnits = peg.volumeFraction(
            standingBrush.monomersPerChain, graftingDensity, layerHeight
        ) / standingEquationOfState.crossoverVolumeFraction,
        crossoverValidVolumeFractionInCrossoverUnits = fresh.volumeFractionInCrossoverUnits
    )
}

private val WINDOW_CRITERIA = listOf(
    ACCEPTABLE_STROKE to 0.0,
    ACCEPTABLE_STROKE to 1.0,
    DESIRED_STROKE to 0.0,
    DESIRED_STROKE to 1.0
)

private fun strokeWindows(designPoints: List<CrossoverDesignPoint>): List<StrokeWindow> =
    LAYER_HEIGHTS.flatMap { height ->
        SWEPT_MODELS.flatMap { (profile, choice) ->
            WINDOW_CRITERIA.map { (required, stretching) ->
                val surviving = designPoints
                    .filter { it.layerHeight == height }
                    .mapNotNull { point ->
                        val response = point.responses.first {
                            it.profile == profile.label && it.interaction == choice.label
                        }
                        if (response.strokeUnderTargetForce >= required &&
                            response.stretchingRatio >= stretching
                        ) point.graftingDensity else null
                    }
                StrokeWindow(
                    layerHeight = height,
                    profile = profile.label,
                    interaction = choice.label,
                    requiredStroke = required,
                    requiredStretchingRatio = stretching,
                    lowestGraftingDensity = surviving.minOrNull(),
                    highestGraftingDensity = surviving.maxOrNull(),
                    empty = surviving.isEmpty()
                )
            }
        }
    }

/** The scale factors the interaction free energy is multiplied by for the sensitivity block. */
private val INTERACTION_SCALES = listOf(0.25, 0.5, 1.0, 2.0, 4.0)

/** The layer height and grafting density the sensitivity is evaluated at — the `C-0001` window edge. */
private const val SENSITIVITY_HEIGHT = 10.0

private const val SENSITIVITY_DENSITY = 0.024

private fun interactionStrengthSensitivity(
    peg: PegWater,
    scale: Double,
    tileArea: Double
): InteractionStrengthSensitivity {
    val model = StrongStretchingLayer(
        desCloizeauxInteraction(peg.crossoverIndex * scale, peg.monomerVolume), PROFILE_PANELS
    )
    val length = model.chainLengthForHeight(peg, SENSITIVITY_HEIGHT, SENSITIVITY_DENSITY)
    val chain = peg.graftedChain(length, SENSITIVITY_DENSITY)
    val equilibrium = model.equilibriumHeight(chain)
    val working = model.heightUnderLoad(chain, TARGET_FORCE, tileArea)
    return InteractionStrengthSensitivity(
        scale = scale,
        monomersPerChain = length,
        heightPerMonomer = equilibrium / length,
        meanVolumeFraction = chain.meanVolumeFraction(equilibrium),
        stretchingRatio = chain.stretchingRatio(equilibrium),
        stiffnessAtFourFifths = model.stiffness(chain, 0.8 * equilibrium, tileArea),
        strokeUnderTargetForce = equilibrium - working,
        secantStiffness = TARGET_FORCE / (equilibrium - working)
    )
}

private fun report(result: CrossoverLayerResult, output: File) {
    println("T-1c / A2.1 — ${result.title}")
    println("300 K, aqueous buffer, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    println()
    val diagnostics = result.premiseDiagnostics
    println("--- did the semidilute premise have to be there at all? ".padEnd(96, '-'))
    println("B from the MEASURED A2                   = ${"%.4f".format(diagnostics.measuredSecondVirialCoefficient)}  (v = ${"%.2f".format(diagnostics.measuredExcludedVolume * 1000.0)} A^3, chi = ${"%.3f".format(diagnostics.floryHugginsChi)})")
    println("B matched at phi# instead                = ${"%.4f".format(diagnostics.matchedSecondVirialCoefficient)}  (${"%.2f".format(diagnostics.matchedOverMeasured)}x the measured one)")
    println("B implied by L0 = N a^(5/3) sigma^(1/3)  = ${"%.4f".format(diagnostics.alexanderDeGennesImpliedSecondVirialCoefficient)}  (v = ${"%.2f".format(diagnostics.alexanderDeGennesImpliedExcludedVolume * 1000.0)} A^3, ${"%.2f".format(diagnostics.alexanderDeGennesImpliedOverMeasured)}x the measured one)")
    println("thermal blob                             = ${"%.0f".format(diagnostics.thermalBlobKuhnSegments)} Kuhn segments = ${"%.0f".format(diagnostics.thermalBlobMonomers)} monomers (${"%.0f".format(diagnostics.thermalBlobMolarMass / 1000.0)} kDa)")
    println("our chain                                = ${"%.1f".format(diagnostics.chainKuhnSegments)} Kuhn segments = ${"%.3f".format(diagnostics.chainThermalBlobs)} thermal blobs -> NOT swollen")
    println("interaction crossover phi_x              = ${"%.5f".format(diagnostics.interactionCrossoverVolumeFraction)}")
    println("bulk crossover phi#                      = ${"%.5f".format(diagnostics.bulkCrossoverVolumeFraction)}")
    println("layer phi                                = ${"%.5f".format(diagnostics.layerVolumeFraction)}  (${"%.1f".format(diagnostics.layerOverInteractionCrossover)}x phi_x)")
    println("Pi_int at the layer: two-body            = ${"%.5f".format(diagnostics.twoBodyPressureAtLayer)} MPa")
    println("                     virial (A2+A3)      = ${"%.5f".format(diagnostics.virialPressureAtLayer)} MPa")
    println("                     des Cloizeaux       = ${"%.5f".format(diagnostics.desCloizeauxPressureAtLayer)} MPa  (${"%.2f".format(diagnostics.desCloizeauxOverVirial)}x the virial one)")
    println()
    println("--- against C-0001, like for like ".padEnd(96, '-'))
    println(
        "%-28s %7s %7s %7s %9s %9s %7s".format(
            "design point", "N(AdG)", "N(new)", "ratio", "stroke0", "strokeN", "ratio"
        )
    )
    result.standingClaimComparison.forEach {
        println(
            "%-28s %7.1f %7.1f %7.3f %9.2f %9.2f %7.3f".format(
                it.label.take(28), it.alexanderDeGennesChainLength, it.crossoverValidChainLength,
                it.chainLengthRatio, it.standingStrokeAtTargetForce,
                it.crossoverValidStrokeAtTargetForce, it.strokeRatio
            )
        )
    }
    println()
    LAYER_HEIGHTS.forEach { height ->
        println("--- layer height L0 = $height nm ".padEnd(96, '-'))
        println(
            "%9s %-16s %-14s %7s %7s %8s %8s %9s %9s".format(
                "sigma", "profile", "interaction", "N", "L0/R0", "phi/phi#",
                "k(0.8L0)", "stroke", "k_sec"
            )
        )
        result.designPoints
            .filter { it.layerHeight == height }
            .filter { it.graftingDensity > 0.015 && it.graftingDensity < 0.16 }
            .forEach { point ->
                point.responses.forEach { response ->
                    println(
                        "%9.4f %-16s %-14s %7.1f %7.2f %8.2f %8.2f %9.2f %9.2f".format(
                            point.graftingDensity, response.profile, response.interaction,
                            response.monomersPerChain, response.stretchingRatio,
                            response.volumeFractionInCrossoverUnits,
                            response.stiffnessAtFourFifths, response.strokeUnderTargetForce,
                            response.secantStiffness
                        )
                    )
                }
            }
        println()
    }
    println("--- design windows in sigma [nm^-2] ".padEnd(96, '-'))
    result.strokeWindows.filter { it.requiredStroke == ACCEPTABLE_STROKE }.forEach {
        val window = if (it.empty) "EMPTY"
        else "[${"%.4f".format(it.lowestGraftingDensity)}, ${"%.4f".format(it.highestGraftingDensity)}]"
        println(
            "L0 = %5.1f nm  %-16s %-14s stroke >= %4.1f nm, L0/R0 >= %3.1f : %s".format(
                it.layerHeight, it.profile, it.interaction, it.requiredStroke,
                it.requiredStretchingRatio, window
            )
        )
    }
    println("stroke >= ${DESIRED_STROKE.toInt()} nm anywhere: " +
            "${!result.strokeWindows.filter { it.requiredStroke == DESIRED_STROKE }.all { it.empty }}")
    println()
    println("--- sensitivity to the STRENGTH of the interaction free energy ".padEnd(96, '-'))
    println("predicted exponent of k in the interaction coefficient: 1/(m+1) = " +
            "${"%.4f".format(result.predictedStiffnessExponentInInteractionStrength)}")
    println("%8s %8s %10s %9s %8s %10s %9s".format(
        "scale", "N", "L0/N", "phi", "L0/R0", "k(0.8L0)", "stroke"
    ))
    result.interactionStrengthSensitivity.forEach {
        println("%8.2f %8.1f %10.5f %9.4f %8.2f %10.2f %9.2f".format(
            it.scale, it.monomersPerChain, it.heightPerMonomer, it.meanVolumeFraction,
            it.stretchingRatio, it.stiffnessAtFourFifths, it.strokeUnderTargetForce
        ))
    }
    println()
    println("written: ${output.path} (${result.designPoints.size} design points, " +
            "${result.designPoints.sumOf { it.responses.size }} responses)")
}

