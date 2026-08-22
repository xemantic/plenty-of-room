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

package com.xemantic.nano.plentyofroom.material

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.abs
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Task `P-6` — how far PEG/water's solvent quality moves between the Gen-1 buffers,
 * and how far the layer's mechanics move with it.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=material.SolventQualitySaltStudyKt
 * ```
 *
 * Emits `gpd/results/P-6-solvent-quality-vs-salt.json`, deterministically.
 */

/** One determination of a quantity, with where it came from and what it is worth. */
@Serializable
data class Determination(
    val quantity: String,
    val value: Double,
    val unit: String,
    val provenance: String,
    val source: String,
    val note: String = ""
)

/** The layer's response to a solvent-quality shift, at one `C-0002` design point. */
@Serializable
data class LayerResponse(
    val label: String,
    val layerHeight: Double,
    val monomersPerChain: Double,
    val volumeFraction: Double,
    val osmoticPressure: Double,
    val osmoticModulus: Double,
    val desCloizeauxModulusFraction: Double,
    val modulusResponseBufferStep: Double,
    val modulusResponseBufferStepMeanField: Double,
    val modulusResponseLocalSpan: Double,
    val modulusResponseLocalSpanMeanField: Double
)

/** One salt scenario, evaluated end to end. */
@Serializable
data class SaltScenario(
    val label: String,
    val cloudPointSlope: Double,
    val lowMolarity: Double,
    val highMolarity: Double,
    val extrapolationDecadesLow: Double,
    val extrapolationDecadesHigh: Double,
    val cloudPointShift: Double,
    val chiShift: Double,
    val excludedVolumeFractionalShift: Double,
    val crossoverIndexFractionalShift: Double,
    val equilibriumHeightFractionalShift: Double
)

@Serializable
data class SolventQualitySaltResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val verdict: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val determinations: List<Determination>,
    val thetaTemperatureBand: Map<String, Double>,
    val ionChannel: Map<String, Double>,
    val transferFunctions: Map<String, Double>,
    val scenarios: List<SaltScenario>,
    val layerResponses: List<LayerResponse>,
    val thresholds: Map<String, Double>,
    val openQuestions: List<String>
)

/** The `C-0002` design points, `(label, L₀, N, φ)`. */
private val DESIGN_POINTS = listOf(
    listOf("L0 = 5 nm, brush onset", 5.0, 63.7, 0.0708),
    listOf("L0 = 7 nm, brush onset", 7.0, 113.2, 0.0439),
    listOf("L0 = 10 nm, window lower edge", 10.0, 199.4, 0.0289),
    listOf("L0 = 10 nm, window upper edge", 10.0, 185.1, 0.0335)
)

/** The salt-free cloud point of PEO in water — Boucher & Hines (1978), `369 ± 3 K`. */
private const val PEO_CLOUD_POINT = 369.0

/**
 * The ceiling on the cloud-point slope of **any** salt on PEO, constructed from
 * Boucher & Hines (1976): across their whole survey of sulfates, carbonates, nitrates and
 * chlorides, `θ` lies between 300 and 360 K, against a salt-free 369 K. Their concentrations
 * are molar, so 69 K/M is a **conservative** ceiling for the *strongest* salts, and the same
 * paper says chlorides are "much less effective" than the sulfates and carbonates.
 */
private const val STRONGEST_SALT_CEILING = 69.0

private const val BUFFER_LOW = 0.002
private const val BUFFER_HIGH = 0.010

/** Bulk salt depleted into the layer at the 2 mM buffer — `C-0005`, `K_salt = 0.52`. */
private const val LOCAL_IONIC_LOW = 0.001

/** Gap-averaged counterion concentration at a 5 nm gap — `C-0005`. */
private const val LOCAL_IONIC_HIGH = 0.066

fun main() {
    val peg = PegWater()
    val chi = ReciprocalTemperatureChi()
    val chiQuadratic = ThetaExpansionChi()
    val virial = ChainSecondVirialCoefficient()
    val waterSite = waterMoleculeVolume()
    val chiAt300 = chi.chi()
    val cloudPointDerivative = chi.chiTemperatureDerivative(PEO_CLOUD_POINT)
    val excludedVolume = monomerExcludedVolume(chiAt300, peg.monomerVolume, waterSite)
    val excludedVolumeVirial = virial.monomerExcludedVolume(peg.monomerMolarMass)

    fun shift(slope: Double, low: Double, high: Double) = solventQualityShift(
        depression = CloudPointDepression(
            slope = slope,
            saltFreeCloudPoint = PEO_CLOUD_POINT,
            fittedRangeLow = 0.1,
            fittedRangeHigh = 1.0,
            source = "Boucher & Hines (1976) ceiling"
        ),
        lowMolarity = low,
        highMolarity = high,
        chiAtOperatingTemperature = chiAt300,
        chiTemperatureDerivativeAtCloudPoint = cloudPointDerivative,
        monomerVolume = peg.monomerVolume,
        latticeSiteVolume = waterSite
    )

    val bufferCeiling = shift(STRONGEST_SALT_CEILING, BUFFER_LOW, BUFFER_HIGH)
    val localCeiling = shift(STRONGEST_SALT_CEILING, LOCAL_IONIC_LOW, LOCAL_IONIC_HIGH)
    val perUnitSlope = abs(shift(1.0, BUFFER_LOW, BUFFER_HIGH).excludedVolumeFractionalShift)

    val scenarios = listOf(
        SaltScenario(
            label = "Gen-1 buffer step, 2 -> 10 mM, at the strongest-PEO-salt ceiling",
            cloudPointSlope = STRONGEST_SALT_CEILING,
            lowMolarity = BUFFER_LOW, highMolarity = BUFFER_HIGH,
            extrapolationDecadesLow = bufferCeiling.extrapolationDecadesLow,
            extrapolationDecadesHigh = bufferCeiling.extrapolationDecadesHigh,
            cloudPointShift = bufferCeiling.cloudPointShift,
            chiShift = bufferCeiling.chiShift,
            excludedVolumeFractionalShift = bufferCeiling.excludedVolumeFractionalShift,
            crossoverIndexFractionalShift = bufferCeiling.crossoverIndexFractionalShift,
            equilibriumHeightFractionalShift = bufferCeiling.equilibriumHeightFractionalShift
        ),
        SaltScenario(
            label = "layer-local ionic span, 1 -> 66 mM (C-0005), at the same ceiling",
            cloudPointSlope = STRONGEST_SALT_CEILING,
            lowMolarity = LOCAL_IONIC_LOW, highMolarity = LOCAL_IONIC_HIGH,
            extrapolationDecadesLow = localCeiling.extrapolationDecadesLow,
            extrapolationDecadesHigh = localCeiling.extrapolationDecadesHigh,
            cloudPointShift = localCeiling.cloudPointShift,
            chiShift = localCeiling.chiShift,
            excludedVolumeFractionalShift = localCeiling.excludedVolumeFractionalShift,
            crossoverIndexFractionalShift = localCeiling.crossoverIndexFractionalShift,
            equilibriumHeightFractionalShift = localCeiling.equilibriumHeightFractionalShift
        ),
        shift(20.0, BUFFER_LOW, BUFFER_HIGH).let {
            SaltScenario(
                "Gen-1 buffer step at a plausible chloride slope of 20 K/M", 20.0,
                BUFFER_LOW, BUFFER_HIGH, it.extrapolationDecadesLow, it.extrapolationDecadesHigh,
                it.cloudPointShift, it.chiShift, it.excludedVolumeFractionalShift,
                it.crossoverIndexFractionalShift, it.equilibriumHeightFractionalShift
            )
        },
        shift(-20.0, BUFFER_LOW, BUFFER_HIGH).let {
            SaltScenario(
                "Gen-1 buffer step if MgCl2 salts PEG IN at -20 K/M", -20.0,
                BUFFER_LOW, BUFFER_HIGH, it.extrapolationDecadesLow, it.extrapolationDecadesHigh,
                it.cloudPointShift, it.chiShift, it.excludedVolumeFractionalShift,
                it.crossoverIndexFractionalShift, it.equilibriumHeightFractionalShift
            )
        }
    )

    val layerResponses = DESIGN_POINTS.map { row ->
        val label = row[0] as String
        val height = row[1] as Double
        val n = row[2] as Double
        val phi = row[3] as Double
        val eos = peg.equationOfState(monomersPerChain = n)
        LayerResponse(
            label = label,
            layerHeight = height,
            monomersPerChain = n,
            volumeFraction = phi,
            osmoticPressure = eos.pressure(phi),
            osmoticModulus = eos.osmoticModulus(phi),
            desCloizeauxModulusFraction = eos.desCloizeauxModulusFraction(phi),
            modulusResponseBufferStep = eos.osmoticModulusResponse(
                phi, bufferCeiling.excludedVolumeFractionalShift
            ),
            modulusResponseBufferStepMeanField = eos.osmoticModulusResponse(
                phi, bufferCeiling.excludedVolumeFractionalShift, MEAN_FIELD_TRANSFER_EXPONENT
            ),
            modulusResponseLocalSpan = eos.osmoticModulusResponse(
                phi, localCeiling.excludedVolumeFractionalShift
            ),
            modulusResponseLocalSpanMeanField = eos.osmoticModulusResponse(
                phi, localCeiling.excludedVolumeFractionalShift, MEAN_FIELD_TRANSFER_EXPONENT
            )
        )
    }

    val ions = ionNumberDensity(BUFFER_HIGH, ionsPerFormulaUnit = 3)
    val gapFacingCharge = 0.5 * 1276.0 / 1600.0

    val result = SolventQualitySaltResult(
        task = "P-6",
        leaf = "none — premise task under A2.1, feeding T-3",
        title = "chi(T, salt) for PEG in water, and the coupling between the Gen-1 buffer " +
                "and the polymer layer's mechanics",
        verificationType = "logical + in-silico, closed against published measurement",
        acceptance = "the change in PEG/water solvent quality between 2 mM and 10 mM MgCl2 at " +
                "300 K is bounded as d(chi), d(v)/v and as the consequent fractional change in " +
                "the layer's osmotic modulus; OR it is shown the available methods cannot " +
                "determine it, with the reason and the missing measurement named",
        verdict = "PASS on BOTH branches: the coefficient itself cannot be determined (no k_s " +
                "for MgCl2 exists, and Boucher & Hines report theta(c) is NON-MONOTONIC for " +
                "Group II chlorides so a linear k_s is not even well posed), but the effect is " +
                "nonetheless BOUNDED from above by the strongest salting-out salt in the PEO " +
                "literature, and the bound is small",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "chi(T) is fitted to measurement; the salt bound is a ceiling constructed from " +
                "a published survey, not a measurement of MgCl2.",
        units = mapOf(
            "length" to "nm",
            "volume" to "nm^3",
            "pressure" to "pN/nm^2 (= MPa)",
            "temperature" to "K",
            "molarity" to "mol/L",
            "numberDensity" to "nm^-3",
            "cloudPointSlope" to "K per mol/L",
            "fractionalShift" to "1 (dimensionless fraction, negative = poorer solvent)"
        ),
        conventions = listOf(
            "chi is quoted on the lattice site it was FITTED on; the measured PEG/water chi " +
                    "uses a WATER-MOLECULE site, so v = v0 (v0/v_water)(1 - 2 chi), which is " +
                    "2.01x the familiar v0 (1 - 2 chi)",
            "a positive cloud-point slope k_s means salting OUT (the cloud point falls)",
            "a negative fractional shift means a POORER solvent",
            "phi is the physical polymer volume fraction, per C-0002",
            "the layer stiffness follows the osmotic modulus exactly, k/A = K/h at fixed N and sigma"
        ),
        validity = listOf(
            "chi(T) is fitted to PEG 4600 in D2O, not H2O; D2O is the poorer solvent, so this " +
                    "is a lower bound on the solvent quality of PEG in H2O",
            "the salt ceiling is constructed from a survey whose concentrations are MOLAR; the " +
                    "Gen-1 buffer is 1.0-1.7 decades below it, and Boucher & Hines report " +
                    "theta(c) is NON-MONOTONIC for Group II chlorides, so linear extrapolation " +
                    "down to 2-10 mM is an assumption, stated here and not hidden",
            "the transfer d(chi_salt) = -(d chi/dT)|_cp * dT_cp assumes the salt's contribution " +
                    "to chi is itself temperature independent between 300 K and the cloud point",
            "the counterion cloud of C-0005 is treated as if it were neutral salt; it is not, " +
                    "and since PEG salting-out is anion-driven while cations bind, this is " +
                    "conservative in magnitude and probably wrong in sign",
            "this is a BULK chi; Lee et al. (2012) measure an EFFECTIVE chi ~ 0.6 for densely " +
                    "grafted PEO, i.e. poor solvent, which is a far larger effect than any salt " +
                    "considered here and is NOT incorporated"
        ),
        temperature = ROOM_TEMPERATURE,
        medium = "aqueous; Gen-1 buffer 2/5/10 mM MgCl2; layer-local Mg2+ 1-66 mM per C-0005",
        thermalEnergy = thermalEnergy(),
        determinations = listOf(
            Determination(
                "chi of PEG/water at 300 K, reciprocal-T fit", chiAt300, "1", "MEASURED",
                "Pedersen & Sommer, Progr. Colloid Polym. Sci. 130:70 (2005), chi = 1.156 - 235.3/T",
                "SAXS, PEG 4600 in D2O, 10-100 C, 1-20 wt %"
            ),
            Determination(
                "chi of PEG/water at 300 K, theta-expansion fit", chiQuadratic.chi(), "1",
                "MEASURED", "same paper, Eq. (8)",
                "the two fits of one dataset agree to 1.8% in chi and 5.5% in (1 - 2 chi)"
            ),
            Determination(
                "d chi / dT at 300 K", chi.chiTemperatureDerivative(), "1/K", "DERIVED",
                "-b/T^2 from the fit above", "positive: heating makes water a WORSE solvent for PEG"
            ),
            Determination(
                "d chi / dT at the 369 K cloud point", cloudPointDerivative, "1/K", "DERIVED",
                "same", "51% below the 300 K value; the salt shift is read HERE, not at 300 K"
            ),
            Determination(
                "monomer excluded volume from chi", excludedVolume, "nm^3", "DERIVED",
                "v = v0 (v0/v_water)(1 - 2 chi)", "water-molecule lattice convention"
            ),
            Determination(
                "monomer excluded volume from B2", excludedVolumeVirial, "nm^3", "DERIVED",
                "B2 = 2.00 nm^3/K (373.2 - T), Chudoba et al. (2017) Eq. 9, v = 2 B2 / N^2",
                "agrees with the chi route to 16% — the load-bearing cross-check"
            ),
            Determination(
                "water molecule volume", waterSite, "nm^3", "DERIVED",
                "M(H2O) / (rho N_A) at 300 K", "the Flory-Huggins lattice site of the measured chi"
            ),
            Determination(
                "v0 / v_water", peg.monomerVolume / waterSite, "1", "DERIVED", "",
                "the factor dropped when a water-lattice chi is fed into v0 (1 - 2 chi)"
            ),
            Determination(
                "thermal blob volume fraction phi** = v/v0",
                excludedVolume / peg.monomerVolume, "1", "DERIVED", "",
                "the layer sits at 0.029-0.071, i.e. 0.06-0.14 of it — good-solvent premise holds"
            ),
            Determination(
                "blob prefactor C implied by the measured alpha",
                peg.crossoverIndex / desCloizeauxIndexFromExcludedVolume(
                    kuhnPairExcludedVolume(excludedVolume, peg.monomersPerKuhnSegment),
                    peg.kuhnLength, peg.kuhnSegmentVolume, peg.monomerVolume
                ), "1", "DERIVED", "",
                "order unity, from the chi route; 0.45 from the independent B2 route"
            ),
            Determination(
                "cited chi = 0.45 of C-0001", 0.45, "1", "CITED — NOW FALSIFIED",
                "C-0001, untraced",
                "no primary source found; the 0.44 in circulation is POLYSTYRENE IN TOLUENE, " +
                        "quoted for contrast in the very paper that measures PEG at 0.372"
            )
        ),
        thetaTemperatureBand = mapOf(
            "Flory-Huggins analysis (Pedersen & Sommer)" to chi.thetaTemperature,
            "cloud points (Boucher & Hines 1978)" to PEO_CLOUD_POINT,
            "virial analysis (Chudoba et al. / Pedersen & Sommer)" to virial.thetaTemperature,
            "cited by C-0002" to peg.thetaTemperature,
            "band width" to peg.thetaTemperature - chi.thetaTemperature,
            "tau at 300 K, lowest theta" to 1.0 - ROOM_TEMPERATURE / chi.thetaTemperature,
            "tau at 300 K, C-0002 theta" to peg.reducedTemperature()
        ),
        ionChannel = mapOf(
            "ion number density at 10 mM MgCl2 [nm^-3]" to ions,
            "ion van't Hoff pressure [pN/nm^2]" to thermalEnergy() * ions,
            "layer osmotic pressure at the 10 nm design point [pN/nm^2]" to
                    peg.equationOfState(199.4).pressure(0.0289),
            "ratio, ion pressure to layer pressure" to
                    thermalEnergy() * ions / peg.equationOfState(199.4).pressure(0.0289),
            "osmotic pressure exerted by ideal excluded salt [pN/nm^2]" to 0.0,
            "gap-averaged counterion molarity at 10 nm [mol/L]" to
                    gapAveragedCounterionMolarity(gapFacingCharge, 10.0),
            "gap-averaged counterion molarity at 5 nm [mol/L]" to
                    gapAveragedCounterionMolarity(gapFacingCharge, 5.0)
        ),
        transferFunctions = mapOf(
            "d(v)/v per kelvin of cloud-point depression" to -2.0 * cloudPointDerivative /
                    (1.0 - 2.0 * chiAt300),
            "d(chi) per kelvin of cloud-point depression" to cloudPointDerivative,
            "d ln alpha / d ln v (des Cloizeaux blob)" to DES_CLOIZEAUX_TRANSFER_EXPONENT,
            "d ln alpha / d ln v (mean field, the bound)" to MEAN_FIELD_TRANSFER_EXPONENT,
            "d ln L0 / d ln v" to 1.0 / 3.0,
            "d(v)/v per (K per molar) over the 2-10 mM step" to -perUnitSlope
        ),
        scenarios = scenarios,
        layerResponses = layerResponses,
        thresholds = mapOf(
            "k_s [K/M] needed to move v by 1% over the 2-10 mM step" to 0.01 / perUnitSlope,
            "k_s [K/M] needed to move v by 5% over the 2-10 mM step" to 0.05 / perUnitSlope,
            "ceiling on k_s from the strongest PEO salting-out salts" to STRONGEST_SALT_CEILING,
            "ratio, threshold to ceiling" to 0.01 / perUnitSlope / STRONGEST_SALT_CEILING,
            "grafting-density shift in chi (Lee et al. 2012) over the buffer-step shift" to
                    (1.2 * THETA_CHI - chiAt300) / abs(bufferCeiling.chiShift),
            "theta-temperature band over the buffer-step cloud-point shift" to
                    (peg.thetaTemperature - chi.thetaTemperature) / abs(bufferCeiling.cloudPointShift)
        ),
        openQuestions = listOf(
            "No cloud-point slope k_s for PEG + MgCl2 exists in accessible form. Boucher & " +
                    "Hines (1976) is the one study that measured Group II chlorides and it is " +
                    "paywalled; its abstract says theta(c) shows MINIMA for those salts, so a " +
                    "single linear k_s is probably not well posed. The missing measurement is " +
                    "named: theta or cloud point of PEG/PEO against MgCl2 concentration BELOW " +
                    "50 mM.",
            "The SIGN is not established. Sadeghi & Jahani (2012) find PEG400 forms no aqueous " +
                    "two-phase system with MgCl2 at all (only with Na2CO3, Na2SO4, Na3Cit) and " +
                    "attribute salting-IN to direct binding of cations to the ether oxygens. " +
                    "Section 2 of the problem definition asserts kosmotropic salting-out; for " +
                    "MgCl2 specifically that direction is not supported.",
            "No binding constant for Mg2+ to PEG ether oxygens IN WATER was located — the same " +
                    "gap C-0005 hit from the electrostatic side. The NMR work that measures " +
                    "multivalent cation binding to PEO (Furo group) is in METHANOL.",
            "The effective chi of a densely GRAFTED PEO layer is reported at ~1.2x the theta " +
                    "value, i.e. poor solvent, from an SCF fit to neutron reflectivity (Lee et " +
                    "al., J. Phys. Chem. B 116:7367, 2012). That is a 0.23 shift in chi against " +
                    "under 0.001 for the whole buffer range. It is not incorporated anywhere in " +
                    "this project and it is the larger question by two orders of magnitude."
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/P-6-solvent-quality-vs-salt.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).withEmissionHeader(LatticeTag.NONE, null)
        ) + "\n"
    )

    println("=== P-6 — solvent quality of PEG/water against salt ".padEnd(96, '='))
    println()
    println("--- what chi actually is ".padEnd(96, '-'))
    result.determinations.forEach {
        println(
            "%-52s %12.6f %-8s %s".format(
                it.quantity.take(52), it.value, it.unit, it.provenance
            )
        )
    }
    println()
    println("--- the theta temperature is itself a band ".padEnd(96, '-'))
    result.thetaTemperatureBand.forEach { (k, v) -> println("%-52s %12.4f".format(k, v)) }
    println()
    println("--- channel 1: mobile ions ".padEnd(96, '-'))
    result.ionChannel.forEach { (k, v) -> println("%-52s %12.6f".format(k, v)) }
    println()
    println("--- transfer functions ".padEnd(96, '-'))
    result.transferFunctions.forEach { (k, v) -> println("%-52s %12.6f".format(k, v)) }
    println()
    println("--- channel 2: solvent quality, per scenario ".padEnd(96, '-'))
    println("%-58s %8s %10s %10s".format("scenario", "dT_cp", "d chi", "dv/v"))
    result.scenarios.forEach {
        println(
            "%-58s %8.3f %10.2e %10.5f".format(
                it.label.take(58), it.cloudPointShift, it.chiShift,
                it.excludedVolumeFractionalShift
            )
        )
    }
    println()
    println("--- the layer's response ".padEnd(96, '-'))
    println("%-34s %8s %10s %10s %10s %10s".format("design point", "phi", "K", "fracDC", "dK/K buf", "dK/K loc"))
    result.layerResponses.forEach {
        println(
            "%-34s %8.4f %10.5f %10.4f %10.5f %10.5f".format(
                it.label.take(34), it.volumeFraction, it.osmoticModulus,
                it.desCloizeauxModulusFraction, it.modulusResponseBufferStep,
                it.modulusResponseLocalSpan
            )
        )
    }
    println()
    println("--- thresholds ".padEnd(96, '-'))
    result.thresholds.forEach { (k, v) -> println("%-62s %12.3f".format(k, v)) }
    println()
    println("still open:")
    result.openQuestions.forEach { println("  - ${it.take(150)}...") }
    println()
    println("written: ${output.path}")
}
