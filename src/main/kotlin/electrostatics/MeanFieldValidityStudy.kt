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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.nano.plentyofroom.ELECTRON_VOLT
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Task `T-6` — the validity boundary of mean-field screening at 2 mM `Mg²⁺` in the Gen-1
 * geometry, leaf `A7.4`.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=electrostatics.MeanFieldValidityStudyKt
 * ```
 *
 * Emits `gpd/results/T-6-mean-field-screening-validity.json`, deterministically —
 * no timestamp, so a re-run that changes nothing produces no diff.
 */

/** One buffer of the §3 sweep, with everything that follows from its ionic strength alone. */
@Serializable
data class BufferPoint(
    val concentration: Double,
    val ionicStrength: Double,
    val magnesiumNumberDensity: Double,
    val chlorideNumberDensity: Double,
    val debyeLength: Double,
    val inverseDebyeLength: Double,
    val saturatedEffectiveChargeDensityDivalent: Double,
    val saturatedEffectiveChargeDensityMonovalent: Double,
    val stericSaturationPotentialChlorideAtAnode: Double,
    val stericSaturationPotentialMagnesiumAtCathode: Double,
    val reducedGapAt5nm: Double,
    val reducedGapAt10nm: Double
)

/** One candidate reading of "the tile's surface charge density", and what it implies. */
@Serializable
data class SurfaceModel(
    val label: String,
    val role: String,
    val surfaceChargeDensity: Double,
    val surfaceChargeDensityCoulombPerSquareMetre: Double,
    val counterionValency: Int,
    val gouyChapmanLength: Double,
    val couplingParameter: Double,
    val lateralCounterionSpacing: Double,
    val wignerSeitzRadius: Double,
    val plasmaParameter: Double,
    val contactDensity: Double,
    val contactDensityMolar: Double,
    val contactOverStericLimit: Double,
    val meanFieldValidityGap: Double?,
    val loopExpansionValidityGap: Double?,
    val rouzinaBloomfieldRange: Double,
    val regime: String
)

/** The deviation of the true pressure from the mean-field one at one gap. */
@Serializable
data class DeviationPoint(
    val label: String,
    val gap: Double,
    val reducedGap: Double,
    val meanFieldPressureCoefficient: Double,
    val oneLoopMagnitude: Double,
    val relativeDeviation: Double,
    val meanFieldControlled: Boolean
)

/** The Manning renormalisation of the tile charge, per counterion valency. */
@Serializable
data class ManningPoint(
    val counterionValency: Int,
    val manningParameter: Double,
    val manningParameterNajiConvention: Double,
    val survivingFraction: Double,
    val condensedFraction: Double,
    val effectiveCharge: Double,
    val effectiveProjectedChargeDensity: Double
)

/** §4(c) — ion partitioning into the PEG layer at one volume fraction. */
@Serializable
data class PartitioningPoint(
    val label: String,
    val polymerVolumeFraction: Double,
    val effectivePermittivity: Double,
    val magnesiumStericPartitionCoefficient: Double,
    val magnesiumBornPartitionCoefficient: Double,
    val magnesiumPartitionCoefficient: Double,
    val chloridePartitionCoefficient: Double,
    val saltPartitionCoefficient: Double,
    val debyeLengthRatio: Double,
    val localDebyeLengthAt2mM: Double
)

/** The ion inventory of the gap, per buffer and gap height. */
@Serializable
data class GapPoint(
    val concentration: Double,
    val gapHeight: Double,
    val counterionsRequired: Double,
    val bulkCounterionsAvailable: Double,
    val dominanceRatio: Double,
    val counterionNumberDensityMillimolar: Double,
    val localScreeningLength: Double,
    val bulkDebyeLength: Double,
    val screeningLengthRatio: Double
)

@Serializable
data class MeanFieldValidityResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val runParameters: Map<String, Double>,
    val citedInputs: List<String>,
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val thermalEnergyElectronVolts: Double,
    val thermalVoltage: Double,
    val bjerrumLength: Double,
    val bjerrumLengthAtPermittivity80: Double,
    val tile: DnaOrigamiTile,
    val tileNucleotides: Double,
    val tileBasePairs: Double,
    val tileHelixCount: Double,
    val buffers: List<BufferPoint>,
    val surfaces: List<SurfaceModel>,
    val manning: List<ManningPoint>,
    val deviations: List<DeviationPoint>,
    val partitioning: List<PartitioningPoint>,
    val gaps: List<GapPoint>,
    val boundary: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private val BUFFERS = listOf(2.0, 5.0, 10.0)
private val GAP_HEIGHTS = listOf(5.0, 7.0, 10.0)

/** `C-0002`'s volume fractions at the surviving `T-1` design points, unperturbed and loaded. */
private val LAYER_VOLUME_FRACTIONS = listOf(
    "L0 = 10 nm, window lower edge (C-0002)" to 0.0288872,
    "L0 = 10 nm, window upper edge (C-0002)" to 0.0335,
    "L0 = 7 nm, brush onset (C-0002)" to 0.0439,
    "L0 = 5 nm, brush onset (C-0002)" to 0.0708,
    "compressed, upper bound of the C-0002 working range" to 0.0750
)

fun main() {
    val peg = PegWater()
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val fibreRadius = peg.kuhnSegmentDiameter / 2.0
    val result = MeanFieldValidityResult(
        task = "T-6",
        leaf = "A7.4",
        title = "Validity boundary of mean-field (Poisson-Boltzmann) screening at " +
                "2/5/10 mM MgCl2 in the Gen-1 tile-over-electrode geometry",
        verificationType = "in-silico (closed-form evaluation of published asymptotic " +
                "criteria) + logical",
        acceptance = "Quantified deviation from mean-field, with the boundary stated.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "The criteria are taken from published Monte-Carlo-validated asymptotics for " +
                "a MODEL system (two uniformly charged planar walls, point counterions, no " +
                "salt); nothing about THIS device is measured.",
        units = mapOf(
            "length" to "nm",
            "chargeDensity" to "e/nm^2",
            "numberDensity" to "1/nm^3",
            "concentration" to "mM",
            "potential" to "V",
            "energy" to "pN*nm",
            "temperature" to "K",
            "capacitance" to "uF/cm^2"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; the electrode surface is z = 0",
            "the tile sits at height z = h; 'gap' always means the tile-electrode separation",
            "the electrostatic force on the tile is ATTRACTIVE (toward -z) under positive " +
                    "electrode bias, so k_es = -dF_es,z/dz < 0 — §1 of the problem definition, " +
                    "restated because T-4 depends on the sign",
            "sigma_s is always a MAGNITUDE, in elementary charges per nm^2",
            "Xi = 2 pi q^3 l_B^2 sigma_s (Naji Eq. 4); mu = 1/(2 pi q l_B sigma_s) (Naji Eq. 3)",
            "the Manning parameter reported as manningParameter is VALENCY-FREE, xi_M = l_B/b; " +
                    "Naji et al. Eq. (28) include the valency, and that reading is reported " +
                    "separately as manningParameterNajiConvention",
            "a_perp = sqrt(q/sigma_s) (Naji Eq. 5) is used for the Rouzina-Bloomfield range; " +
                    "the Wigner-Seitz radius sqrt(q/(pi sigma_s)) is used for the plasma " +
                    "parameter. They differ by sqrt(pi) and are NOT interchangeable"
        ),
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE,
            "waterRelativePermittivity" to WATER_RELATIVE_PERMITTIVITY,
            "pegRelativePermittivity" to PEG_RELATIVE_PERMITTIVITY,
            "tileEdge" to tile.edge,
            "tileThickness" to tile.thickness,
            "interhelicalDistance" to tile.interhelicalDistance,
            "risePerBasePair" to tile.risePerBasePair,
            "helixRadius" to tile.helixRadius,
            "hydratedMagnesiumRadius" to HYDRATED_MAGNESIUM_RADIUS,
            "hydratedChlorideRadius" to HYDRATED_CHLORIDE_RADIUS,
            "pegFibreRadius" to fibreRadius,
            "sternCapacitance" to 20.0,
            "sternChargeDensityPerVolt" to sternChargeDensityPerVolt(20.0),
            "attractionOnsetCoupling" to ATTRACTION_ONSET_COUPLING,
            "unbindingTransitionCoupling" to UNBINDING_TRANSITION_COUPLING,
            "imageChargeAttractionOnsetCoupling" to IMAGE_CHARGE_ATTRACTION_ONSET_COUPLING,
            "wignerCrystalPlasmaParameter" to WIGNER_CRYSTAL_PLASMA_PARAMETER
        ),
        citedInputs = listOf(
            "eps_r(water, 300 K) = 78 — CITED. Literature spans 77.7-78.3; l_B goes as 1/eps " +
                    "and Xi as l_B^2, so the 3% spread is a 6% spread in Xi. Moves no verdict.",
            "eps_r(bulk PEO) = 5 — CITED, and only ever used inside a mixing rule at phi ~ 0.03 " +
                    "where the whole polymer contribution is a 4% decrement.",
            "B-DNA rise per base pair = 0.34 nm — CITED. Everything about the tile's charge " +
                    "is DERIVED from it.",
            "B-DNA duplex radius = 1.0 nm — CITED.",
            "honeycomb-lattice interhelical distance = 2.6 nm — CITED, and the largest single " +
                    "uncertainty here: the projected charge density goes as its inverse square. " +
                    "It does NOT affect the duplex surface charge density, which is what Xi uses.",
            "hydrated ionic radii, Nightingale (1959): Mg2+ 4.28 A, Cl- 3.32 A — CITED. " +
                    "The first-shell geometric radius of Mg(H2O)6 2+ is 3.47 A; using it raises " +
                    "the hard-core-corrected Xi from 16.8 to 17.8, i.e. across the Xi = 17 " +
                    "unbinding threshold rather than below it. Reported, not hidden.",
            "Stern capacitance ~20 uF/cm^2 — CITED, order-of-magnitude for aqueous electrodes.",
            "Xi thresholds 12 / 17 / 30 and Gamma_c = 125 — CITED from Naji et al. (2005), " +
                    "who obtain them from Monte-Carlo simulation of the model system.",
            "the Netz/Naji criteria themselves are CITED formulas, EVALUATED here at our " +
                    "parameters; the evaluation is derived, the criteria are not."
        ),
        temperature = ROOM_TEMPERATURE,
        medium = "aqueous MgCl2 buffer, 2/5/10 mM, 300 K",
        thermalEnergy = thermalEnergy(),
        thermalEnergyElectronVolts = thermalEnergy() / ELECTRON_VOLT,
        thermalVoltage = thermalVoltage(),
        bjerrumLength = lb,
        bjerrumLengthAtPermittivity80 = bjerrumLength(relativePermittivity = 80.0),
        tile = tile,
        tileNucleotides = tile.nucleotides,
        tileBasePairs = tile.basePairs,
        tileHelixCount = tile.helixCount,
        buffers = BUFFERS.map { bufferPoint(it, lb) },
        surfaces = surfaceModels(tile, lb),
        manning = listOf(1, 2, 3).map { manningPoint(tile, it, lb) },
        deviations = deviationPoints(tile, lb),
        partitioning = LAYER_VOLUME_FRACTIONS.map { (label, phi) ->
            partitioningPoint(label, phi, fibreRadius)
        },
        gaps = BUFFERS.flatMap { molarity ->
            GAP_HEIGHTS.map { height -> gapPoint(tile, molarity, height, lb) }
        },
        boundary = boundary(tile, lb),
        validity = listOf(
            "The Netz/Naji criteria are derived for TWO UNIFORMLY CHARGED PLANAR WALLS with " +
                    "POINT counterions and NO SALT. Our geometry is a charge-patterned slab of " +
                    "duplexes over a metal electrode, in salt, with a polymer layer between. " +
                    "The transfer is justified by counterion dominance (see gaps[]) but it is a " +
                    "TRANSFER, and the numbers are bounds rather than predictions.",
            "The two walls in the model are LIKE charged. Under positive bias the Gen-1 " +
                    "electrode and tile are OPPOSITELY charged. The coupling parameter is a " +
                    "property of each surface separately and transfers; the ATTRACTION " +
                    "thresholds (Xi > 12 etc.) are a two-like-walls result and do NOT.",
            "Charge regulation is not modelled: phosphate pKa ~ 1, so the DNA charge is " +
                    "bias-independent, but the electrode's is not.",
            "The dielectric jump at the electrode (metal, image charges) is named through the " +
                    "Xi ~ 30 threshold but not computed. The low-k/high-k dielectric layer of " +
                    "§1 would change it and is not modelled at all.",
            "Specific Mg2+-phosphate chemistry (inner-sphere coordination, site binding) is " +
                    "outside every model used here. Manning condensation is territorial, " +
                    "not chemical.",
            "The ion partitioning bound counts only EXCLUSION mechanisms and is therefore a " +
                    "LOWER bound on the partition coefficient. PEG-cation coordination runs " +
                    "the other way and is not bounded — see openQuestions."
        ),
        openQuestions = listOf(
            "§4(c) IS NOT CLOSED BY THIS TASK. The steric+Born bound gives K_salt = 0.77-0.52, " +
                    "i.e. weaker screening inside the layer than outside. But PEG's ether " +
                    "oxygens coordinate cations (the mechanism behind PEO polymer electrolytes), " +
                    "which would RAISE K above the bound and could in principle push it above 1. " +
                    "No binding constant for Mg2+/PEG in water was located this iteration. " +
                    "The sign of the §4(c) answer is therefore established only as a bound, " +
                    "and the bound is one-sided. Raised as a queue item.",
            "The intermediate-coupling regime 1 < Xi < 100 has, in Naji et al.'s own words, no " +
                    "systematic theory: neither the loop expansion about mean field nor the " +
                    "virial expansion about strong coupling converges there. Our Xi = 17-24 is " +
                    "inside that gap. This is a statement about the state of the literature, " +
                    "not about our implementation, and it cannot be fixed by a better " +
                    "closed-form calculation.",
            "Whether Mg2+ at 2-10 mM can drive helix-helix attraction WITHIN the origami is " +
                    "left open. The Rouzina-Bloomfield range a_perp = 1.46 nm is smaller than " +
                    "the 2.6 nm interhelical distance by 1.8x, so the criterion says no; but " +
                    "that margin is smaller than the spread between our surface-charge models, " +
                    "and origami folding demonstrably REQUIRES Mg2+, which means the effect is " +
                    "not negligible at the folding stage. T-5 territory, flagged here.",
            "No published Xi criterion exists for OPPOSITELY charged walls, which is the " +
                    "actuated configuration. The like-charge thresholds are reported because " +
                    "they are what exists, and are explicitly NOT transferred."
        )
    )
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-6-mean-field-screening-validity.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).withEmissionHeader(LatticeTag.NONE, null)
        ) + "\n"
    )
    report(result, output)
}

private fun bufferPoint(concentration: Double, bjerrumLength: Double): BufferPoint {
    val buffer = MagnesiumChlorideBuffer(concentration)
    return BufferPoint(
        concentration = concentration,
        ionicStrength = buffer.ionicStrength,
        magnesiumNumberDensity = buffer.magnesiumNumberDensity,
        chlorideNumberDensity = buffer.chlorideNumberDensity,
        debyeLength = buffer.debyeLength(),
        inverseDebyeLength = buffer.inverseDebyeLength(),
        saturatedEffectiveChargeDensityDivalent =
            saturatedEffectiveChargeDensity(buffer.inverseDebyeLength(), 2, bjerrumLength),
        saturatedEffectiveChargeDensityMonovalent =
            saturatedEffectiveChargeDensity(buffer.inverseDebyeLength(), 1, bjerrumLength),
        stericSaturationPotentialChlorideAtAnode = stericSaturationPotential(
            1, buffer.chlorideNumberDensity, HYDRATED_CHLORIDE_RADIUS
        ),
        stericSaturationPotentialMagnesiumAtCathode = stericSaturationPotential(
            2, buffer.magnesiumNumberDensity, HYDRATED_MAGNESIUM_RADIUS
        ),
        reducedGapAt5nm = 5.0 / buffer.debyeLength(),
        reducedGapAt10nm = 10.0 / buffer.debyeLength()
    )
}

private fun surfaceModels(tile: DnaOrigamiTile, bjerrumLength: Double): List<SurfaceModel> {
    val candidates = listOf(
        Triple(
            "DNA duplex cylinder surface", tile.duplexSurfaceChargeDensity,
            "LOCAL coupling — what a condensed counterion actually sits on. THE value Xi is read from."
        ),
        Triple(
            "DNA duplex, hydrated-Mg2+ hard core (Naji Eq. 30)",
            tile.hardCoreSurfaceChargeDensity(HYDRATED_MAGNESIUM_RADIUS),
            "LOCAL coupling, corrected for the fact that a hydrated ion cannot reach the phosphates"
        ),
        Triple(
            "single row of helices, projected", singleHelixLayerChargeDensity(tile),
            "the other reading of §3's 'single-layer honeycomb'"
        ),
        Triple(
            "whole 10 nm tile, projected onto its footprint", tile.projectedChargeDensity,
            "FAR FIELD only — the plane a distant electrode sees. Using it for Xi would be a category error."
        )
    )
    return candidates.flatMap { (label, density, role) ->
        listOf(1, 2).map { valency ->
            val surface = ChargedSurface(density, valency)
            val coupling = surface.couplingParameter(bjerrumLength)
            val mu = surface.gouyChapmanLength(bjerrumLength)
            val ionRadius =
                if (valency == 2) HYDRATED_MAGNESIUM_RADIUS else HYDRATED_CHLORIDE_RADIUS
            SurfaceModel(
                label = label,
                role = role,
                surfaceChargeDensity = density,
                surfaceChargeDensityCoulombPerSquareMetre =
                    surface.surfaceChargeDensityInCoulombPerSquareMetre,
                counterionValency = valency,
                gouyChapmanLength = mu,
                couplingParameter = coupling,
                lateralCounterionSpacing = surface.lateralCounterionSpacing,
                wignerSeitzRadius = surface.wignerSeitzRadius,
                plasmaParameter = surface.plasmaParameter(bjerrumLength),
                contactDensity = surface.contactDensity(bjerrumLength),
                contactDensityMolar =
                    perCubicNanometreToMillimolar(surface.contactDensity(bjerrumLength)) / 1000.0,
                contactOverStericLimit =
                    surface.contactDensity(bjerrumLength) / closePackedNumberDensity(ionRadius),
                meanFieldValidityGap = meanFieldValidityGap(coupling, mu),
                loopExpansionValidityGap = loopExpansionValidityGap(coupling, mu),
                rouzinaBloomfieldRange = surface.rouzinaBloomfieldRange,
                regime = regimeOf(coupling)
            )
        }
    }
}

/**
 * Names the regime by Naji et al.'s own practical boundaries: `Ξ ≲ 1` weak coupling,
 * `Ξ ≳ 10²` strong coupling, and in between an intermediate regime for which they state
 * plainly that neither expansion converges.
 */
private fun regimeOf(coupling: Double): String = when {
    coupling <= 1.0 -> "WEAK COUPLING — mean-field PB applies"
    coupling < 100.0 ->
        "INTERMEDIATE COUPLING — neither the loop expansion about PB nor the virial " +
                "expansion about strong coupling converges (Naji et al. §IV C)"
    else -> "STRONG COUPLING — the counterion layer is quasi-2D and correlated"
}

private fun manningPoint(
    tile: DnaOrigamiTile,
    valency: Int,
    bjerrumLength: Double
): ManningPoint {
    val surviving = tile.manningSurvivingFraction(valency, bjerrumLength)
    return ManningPoint(
        counterionValency = valency,
        manningParameter = tile.manningParameter(bjerrumLength),
        manningParameterNajiConvention = valency * tile.manningParameter(bjerrumLength),
        survivingFraction = surviving,
        condensedFraction = tile.manningCondensedFraction(valency, bjerrumLength),
        effectiveCharge = tile.nucleotides * surviving,
        effectiveProjectedChargeDensity = tile.projectedChargeDensity * surviving
    )
}

private fun deviationPoints(tile: DnaOrigamiTile, bjerrumLength: Double): List<DeviationPoint> {
    val cases = listOf(
        "DNA duplex surface, Mg2+ (q=2)" to ChargedSurface(tile.duplexSurfaceChargeDensity, 2),
        "DNA duplex surface, hydrated hard core, Mg2+ (q=2)" to
                ChargedSurface(tile.hardCoreSurfaceChargeDensity(HYDRATED_MAGNESIUM_RADIUS), 2),
        "DNA duplex surface, monovalent counterion (q=1), for contrast" to
                ChargedSurface(tile.duplexSurfaceChargeDensity, 1)
    )
    return cases.flatMap { (label, surface) ->
        val coupling = surface.couplingParameter(bjerrumLength)
        val mu = surface.gouyChapmanLength(bjerrumLength)
        (GAP_HEIGHTS + listOf(15.0, 20.0, 25.0)).map { gap ->
            val reduced = gap / mu
            DeviationPoint(
                label = label,
                gap = gap,
                reducedGap = reduced,
                meanFieldPressureCoefficient = poissonBoltzmannPressureCoefficient(reduced),
                oneLoopMagnitude = oneLoopPressureCoefficientMagnitude(reduced),
                relativeDeviation = meanFieldDeviation(coupling, reduced),
                meanFieldControlled = meanFieldDeviation(coupling, reduced) < 1.0
            )
        }
    }
}

private fun partitioningPoint(
    label: String,
    volumeFraction: Double,
    fibreRadius: Double
): PartitioningPoint {
    val layer = LayerPartitioning(volumeFraction, fibreRadius)
    return PartitioningPoint(
        label = label,
        polymerVolumeFraction = volumeFraction,
        effectivePermittivity = layer.effectivePermittivity,
        magnesiumStericPartitionCoefficient = layer.magnesiumStericPartitionCoefficient,
        magnesiumBornPartitionCoefficient = layer.magnesiumBornPartitionCoefficient,
        magnesiumPartitionCoefficient = layer.magnesiumPartitionCoefficient,
        chloridePartitionCoefficient = layer.chloridePartitionCoefficient,
        saltPartitionCoefficient = layer.saltPartitionCoefficient,
        debyeLengthRatio = layer.debyeLengthRatio,
        localDebyeLengthAt2mM = MagnesiumChlorideBuffer(2.0).debyeLength() * layer.debyeLengthRatio
    )
}

private fun gapPoint(
    tile: DnaOrigamiTile,
    concentration: Double,
    gapHeight: Double,
    bjerrumLength: Double
): GapPoint {
    val buffer = MagnesiumChlorideBuffer(concentration)
    val gap = CounterionDominatedGap(
        tile = tile,
        buffer = buffer,
        gapHeight = gapHeight,
        counterionValency = 2,
        chargeFraction = tile.manningSurvivingFraction(2, bjerrumLength)
    )
    return GapPoint(
        concentration = concentration,
        gapHeight = gapHeight,
        counterionsRequired = gap.counterionsRequired,
        bulkCounterionsAvailable = gap.bulkCounterionsAvailable,
        dominanceRatio = gap.dominanceRatio,
        counterionNumberDensityMillimolar =
            perCubicNanometreToMillimolar(gap.counterionNumberDensity),
        localScreeningLength = gap.localScreeningLength(bjerrumLength),
        bulkDebyeLength = buffer.debyeLength(),
        screeningLengthRatio = gap.localScreeningLength(bjerrumLength) / buffer.debyeLength()
    )
}

private fun boundary(tile: DnaOrigamiTile, bjerrumLength: Double): Map<String, String> {
    val surface = ChargedSurface(tile.duplexSurfaceChargeDensity, 2)
    val hardCore =
        ChargedSurface(tile.hardCoreSurfaceChargeDensity(HYDRATED_MAGNESIUM_RADIUS), 2)
    val coupling = surface.couplingParameter(bjerrumLength)
    val mu = surface.gouyChapmanLength(bjerrumLength)
    val hardCoreCoupling = hardCore.couplingParameter(bjerrumLength)
    val hardCoreMu = hardCore.gouyChapmanLength(bjerrumLength)
    fun f(value: Double, digits: Int = 2) = "%.${digits}f".format(value)
    // a weakly coupled wall has NO validity boundary, and before T-221 the bisection returned
    // its own bracket floor for one; the null is a verdict and it must reach the prose (CH-0178)
    fun fOrNone(value: Double?, digits: Int = 2) =
        if (value == null) "NONE — the criterion holds at every separation" else f(value, digits)
    return mapOf(
        "band_A_qualitative_failure" to
                "gap < a_perp = ${f(surface.rouzinaBloomfieldRange)} nm " +
                "(${f(hardCore.rouzinaBloomfieldRange)} nm hard-core corrected). " +
                "Rouzina-Bloomfield: correlation attraction is possible and PB cannot produce " +
                "it AT ALL. NOT reached in the Gen-1 geometry — the polymer layer holds the " +
                "tile 5-10 nm off the electrode, 3.4x to 6.8x outside this band.",
        "band_B_uncontrolled" to
                "a_perp < gap < ${fOrNone(meanFieldValidityGap(coupling, mu))} nm " +
                "(${fOrNone(meanFieldValidityGap(hardCoreCoupling, hardCoreMu))} nm hard-core " +
                "corrected). The one-loop correction is a finite fraction of, and eventually " +
                "exceeds, the leading PB term. PB is qualitatively right (monotone, no " +
                "attraction) but quantitatively uncontrolled. THE ENTIRE 5-10 nm GEN-1 " +
                "WORKING RANGE IS IN THIS BAND.",
        "band_C_controlled" to
                "gap > ${fOrNone(meanFieldValidityGap(coupling, mu))} nm " +
                "(Naji Eq. 20 closed form: ${fOrNone(loopExpansionValidityGap(coupling, mu))} nm). " +
                "The loop expansion converges and PB is quantitatively usable, with the " +
                "residual error given by the relativeDeviation column.",
        "deviation_at_working_range" to
                "one-loop / leading PB = " +
                "${f(meanFieldDeviation(coupling, 5.0 / mu))} at 5 nm, " +
                "${f(meanFieldDeviation(coupling, 7.0 / mu))} at 7 nm, " +
                "${f(meanFieldDeviation(coupling, 10.0 / mu))} at 10 nm " +
                "(bare charge, q = 2). With a monovalent counterion at the same surface it " +
                "would be ${f(meanFieldDeviation(
                    ChargedSurface(tile.duplexSurfaceChargeDensity, 1).couplingParameter(bjerrumLength),
                    7.0 / ChargedSurface(tile.duplexSurfaceChargeDensity, 1)
                        .gouyChapmanLength(bjerrumLength)
                ))} at 7 nm. The divalence, not the surface charge, is what breaks mean field.",
        "electrode_bias_boundary" to
                "point-ion PB at the electrode fails above " +
                "${f(stericSaturationPotential(1, MagnesiumChlorideBuffer(2.0).chlorideNumberDensity, HYDRATED_CHLORIDE_RADIUS), 3)} V " +
                "of diffuse-layer drop (Cl- counterion, 2 mM) — a factor of " +
                "${f(2.0 / stericSaturationPotential(1, MagnesiumChlorideBuffer(2.0).chlorideNumberDensity, HYDRATED_CHLORIDE_RADIUS))} " +
                "below the §3 bias target of 2 V. Above it the compact layer carries the " +
                "potential and the electrode charge is Stern-limited at " +
                "${f(sternChargeDensityPerVolt(20.0))} e/nm^2 per volt, NOT Gouy-Chapman.",
        "explicit_ions_required_when" to
                "(1) any quantity that depends on the ion distribution within a_perp = 1.5 nm " +
                "of a phosphate — effective charge beyond Manning, helix-helix forces inside " +
                "the origami, local dielectric response; (2) any bias above ~0.2 V where the " +
                "compact layer dominates; (3) any question about charge inversion or " +
                "like-charge attraction, which PB cannot answer even in principle. " +
                "NOT required for the tile-electrode force at 5-10 nm, where the cheap route " +
                "is PB with a Manning-renormalised, saturation-capped effective charge and a " +
                "stated factor-of-2 uncertainty."
    )
}

private fun report(result: MeanFieldValidityResult, output: File) {
    println("T-6 — ${result.title}")
    println("leaf ${result.leaf}; 300 K, aqueous MgCl2; l_B = ${"%.4f".format(result.bjerrumLength)} nm")
    println()
    println("--- §3 buffer sweep ".padEnd(100, '-'))
    println("%8s %8s %10s %12s %14s %14s".format("c[mM]", "I[mM]", "lambda_D", "kappa*h(5nm)", "psi_max(Cl-)", "sigma_sat(q=2)"))
    result.buffers.forEach {
        println(
            "%8.1f %8.1f %10.4f %12.3f %14.4f %14.5f".format(
                it.concentration, it.ionicStrength, it.debyeLength,
                it.reducedGapAt5nm, it.stericSaturationPotentialChlorideAtAnode,
                it.saturatedEffectiveChargeDensityDivalent
            )
        )
    }
    println()
    println("--- surface charge models and their coupling parameters ".padEnd(100, '-'))
    println("%-46s %3s %9s %8s %9s %9s".format("surface", "q", "sigma", "mu[nm]", "Xi", "a_perp"))
    result.surfaces.forEach {
        println(
            "%-46s %3d %9.4f %8.4f %9.2f %9.3f".format(
                it.label.take(46), it.counterionValency, it.surfaceChargeDensity,
                it.gouyChapmanLength, it.couplingParameter, it.lateralCounterionSpacing
            )
        )
    }
    println()
    println("--- Manning renormalisation of the tile charge ".padEnd(100, '-'))
    println("%3s %10s %12s %12s %14s".format("q", "xi_M", "surviving", "condensed", "Q_eff[e]"))
    result.manning.forEach {
        println(
            "%3d %10.4f %12.4f %12.4f %14.1f".format(
                it.counterionValency, it.manningParameter, it.survivingFraction,
                it.condensedFraction, it.effectiveCharge
            )
        )
    }
    println()
    println("--- deviation from mean field ".padEnd(100, '-'))
    println("%-52s %8s %12s %10s".format("case", "gap[nm]", "one-loop/PB", "controlled"))
    result.deviations.forEach {
        println(
            "%-52s %8.1f %12.3f %10s".format(
                it.label.take(52), it.gap, it.relativeDeviation, it.meanFieldControlled
            )
        )
    }
    println()
    println("--- §4(c) ion partitioning into the PEG layer ".padEnd(100, '-'))
    println("%-42s %8s %8s %8s %10s %12s".format("layer", "phi", "eps_eff", "K_salt", "lam_in/lam", "lam_in@2mM"))
    result.partitioning.forEach {
        println(
            "%-42s %8.4f %8.2f %8.4f %10.4f %12.3f".format(
                it.label.take(42), it.polymerVolumeFraction, it.effectivePermittivity,
                it.saltPartitionCoefficient, it.debyeLengthRatio, it.localDebyeLengthAt2mM
            )
        )
    }
    println()
    println("--- ion inventory of the gap ".padEnd(100, '-'))
    println("%8s %8s %12s %12s %10s %14s".format("c[mM]", "h[nm]", "needed", "from bulk", "ratio", "lambda_local"))
    result.gaps.forEach {
        println(
            "%8.1f %8.1f %12.1f %12.2f %10.2f %14.3f".format(
                it.concentration, it.gapHeight, it.counterionsRequired,
                it.bulkCounterionsAvailable, it.dominanceRatio, it.localScreeningLength
            )
        )
    }
    println()
    println("--- THE BOUNDARY ".padEnd(100, '-'))
    result.boundary.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("still open:")
    result.openQuestions.forEach { println("  - ${it.take(160)}...") }
    println()
    println("written: ${output.path}")
}
