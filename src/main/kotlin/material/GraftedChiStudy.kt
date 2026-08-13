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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.abs

/**
 * Task `P-9` — is the effective `χ` of a *grafted* PEG layer the bulk one?
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=material.GraftedChiStudyKt
 * ```
 *
 * Emits `gpd/results/P-9-grafted-chi.json`, deterministically.
 */

/** One self-consistent-field `χ` fit, reduced to the quantities that decide transferability. */
@Serializable
data class ScfFitReport(
    val label: String,
    val geometry: String,
    val areaPerChain: Double,
    val graftingDensity: Double,
    val reducedGraftingDensity: Double,
    val monomersPerChain: Double,
    val insideGen1GraftingWindow: Boolean,
    val insideGen1ChainLengthWindow: Boolean,
    val fittedChi: Double,
    val fittedChiUncertainty: Double,
    val modelThetaChi: Double,
    val chiPastModelTheta: Double,
    val chiRatioToModelTheta: Double,
    val floryHugginsByRatio: Double,
    val floryHugginsByOffset: Double,
    val transferSpread: Double,
    val modelSiteVolumeRatio: Double,
    val source: String
)

/** One Alexander-de Gennes compression fit, inverted into an effective `χ`. */
@Serializable
data class CompressionFitReport(
    val label: String,
    val geometry: String,
    val fittedMonomerLength: Double,
    val fittedMonomerLengthUncertainty: Double,
    val bulkMonomerLength: Double,
    val restingHeight: Double,
    val monomersPerChain: Double,
    val graftingSpacing: Double,
    val graftingDensity: Double,
    val physicalVolumeFraction: Double,
    val aboveGen1GraftingWindow: Boolean,
    val interactionStrengthRatio: Double,
    val interactionStrengthRatioLow: Double,
    val interactionStrengthRatioHigh: Double,
    val effectiveChi: Double,
    val effectiveChiLow: Double,
    val effectiveChiHigh: Double,
    val effectiveChiMeanField: Double,
    val chiShiftFromBulk: Double,
    val stiffnessRatio: Double,
    val stiffnessRatioLow: Double,
    val stiffnessRatioHigh: Double,
    val strokeRatio: Double,
    val strokeRatioLow: Double,
    val strokeRatioHigh: Double,
    val source: String
)

@Serializable
data class GraftedChiResult(
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
    val bulkChi: Double,
    val bulkChiSource: String,
    val scfFits: List<ScfFitReport>,
    val compressionFits: List<CompressionFitReport>,
    val bounds: Map<String, Double>,
    val exposure: Map<String, Double>,
    val searchNegatives: List<String>,
    val openQuestions: List<String>
)

/**
 * The two area-per-chain conditions at which Lee et al. fitted `χ`, read from the PDF
 * (§3.3 and Figure 4). Both sit **inside** the Gen-1 grafting window, which is why `P-9`
 * cannot be closed by calling the system the wrong one.
 */
private val SCF_FITS = listOf(
    ScfBrushChiFit(
        label = "Lee et al. 2012, alpha = 1350 A^2/chain",
        areaPerChain = 1350.0,
        fittedChi = 0.789,
        fittedChiUncertainty = 0.066,
        modelThetaChi = 0.696
    ),
    ScfBrushChiFit(
        label = "Lee et al. 2012, alpha = 2200 A^2/chain",
        areaPerChain = 2200.0,
        fittedChi = 0.852,
        fittedChiUncertainty = 0.051,
        modelThetaChi = 0.696
    )
)

/**
 * The two unconstrained Alexander-de Gennes fits of Hansen et al. to the DSPC:PEG-5000
 * osmotic-stress data — the only PEG-brush compression data in that literature that met
 * their own semidilute brush criterion.
 */
private val COMPRESSION_FITS = listOf(
    AlexanderDeGennesBrushFit(
        label = "Hansen et al. 2003, nominal f = 0.10",
        fittedMonomerLength = 3.56,
        fittedMonomerLengthUncertainty = 0.07,
        restingHeight = 105.0
    ),
    AlexanderDeGennesBrushFit(
        label = "Hansen et al. 2003, nominal f = 0.20",
        fittedMonomerLength = 3.30,
        fittedMonomerLengthUncertainty = 0.15,
        restingHeight = 109.0
    )
)

fun main() {
    val peg = PegWater()
    val chiFit = ReciprocalTemperatureChi()
    val bulkChi = chiFit.chi()
    val waterSite = waterMoleculeVolume()

    val scfReports = SCF_FITS.map {
        ScfFitReport(
            label = it.label,
            geometry = it.geometry,
            areaPerChain = it.areaPerChain,
            graftingDensity = it.graftingDensity,
            reducedGraftingDensity = it.reducedGraftingDensity,
            monomersPerChain = it.monomersPerChain,
            insideGen1GraftingWindow = it.graftingDensity in
                    GEN1_GRAFTING_DENSITY_LOW..GEN1_GRAFTING_DENSITY_HIGH,
            insideGen1ChainLengthWindow = it.monomersPerChain in 60.0..375.0,
            fittedChi = it.fittedChi,
            fittedChiUncertainty = it.fittedChiUncertainty,
            modelThetaChi = it.modelThetaChi,
            chiPastModelTheta = it.chiPastModelTheta,
            chiRatioToModelTheta = it.chiRatioToModelTheta,
            floryHugginsByRatio = it.floryHugginsByRatio,
            floryHugginsByOffset = it.floryHugginsByOffset,
            transferSpread = it.transferSpread,
            modelSiteVolumeRatio = it.modelSiteVolumeRatio,
            source = it.source
        )
    }

    val compressionReports = COMPRESSION_FITS.map {
        val band = it.interactionStrengthRatioBand()
        val chiBand = it.effectiveChiBand(bulkChi)
        CompressionFitReport(
            label = it.label,
            geometry = it.geometry,
            fittedMonomerLength = it.fittedMonomerLength,
            fittedMonomerLengthUncertainty = it.fittedMonomerLengthUncertainty,
            bulkMonomerLength = it.bulkMonomerLength,
            restingHeight = it.restingHeight,
            monomersPerChain = it.monomersPerChain,
            graftingSpacing = it.graftingSpacing,
            graftingDensity = it.graftingDensity,
            physicalVolumeFraction = it.physicalVolumeFraction(peg.monomerVolume),
            aboveGen1GraftingWindow = it.graftingDensity > GEN1_GRAFTING_DENSITY_HIGH,
            interactionStrengthRatio = it.interactionStrengthRatio(),
            interactionStrengthRatioLow = band.min(),
            interactionStrengthRatioHigh = band.max(),
            effectiveChi = it.effectiveChi(bulkChi),
            effectiveChiLow = chiBand.min(),
            effectiveChiHigh = chiBand.max(),
            effectiveChiMeanField = it.effectiveChi(
                bulkChi, MEAN_FIELD_EXCLUDED_VOLUME_EXPONENT
            ),
            chiShiftFromBulk = it.effectiveChi(bulkChi) - bulkChi,
            stiffnessRatio = stiffnessRatioFromInteractionRatio(it.interactionStrengthRatio()),
            stiffnessRatioLow = band.minOf { r -> stiffnessRatioFromInteractionRatio(r) },
            stiffnessRatioHigh = band.maxOf { r -> stiffnessRatioFromInteractionRatio(r) },
            strokeRatio = strokeRatioFromInteractionRatio(it.interactionStrengthRatio()),
            strokeRatioLow = band.minOf { r -> strokeRatioFromInteractionRatio(r) },
            strokeRatioHigh = band.maxOf { r -> strokeRatioFromInteractionRatio(r) },
            source = it.source
        )
    }

    val chiBandAll = COMPRESSION_FITS.flatMap { it.effectiveChiBand(bulkChi) }
    val ratioBandAll = COMPRESSION_FITS.flatMap { it.interactionStrengthRatioBand() }
    val compressionShift = chiBandAll.maxOf { abs(it - bulkChi) }
    val scfRatioTransferShift = SCF_FITS.maxOf { abs(it.floryHugginsByRatio - bulkChi) }
    val scfOffsetTransferShift = SCF_FITS.maxOf { abs(it.floryHugginsByOffset - bulkChi) }
    val transferSpreadAll = SCF_FITS.flatMap {
        listOf(it.floryHugginsByRatio, it.floryHugginsByOffset)
    }.let { it.max() - it.min() }

    val result = GraftedChiResult(
        task = "P-9",
        leaf = "none — premise task under A2.1, consumed by A2.1 (T-1c/T-1d) and A2.2 (T-3)",
        title = "The effective chi of a grafted PEG layer, against the bulk equation of state",
        verificationType = "logical + in-silico, closed against two published measurements",
        acceptance = "(a) INAPPLICABLE — the chi ~ 0.60 attributed to a grafted PEO layer is not " +
                "a value the source reports, is an adjustable parameter of a bespoke continuum " +
                "SCF whose own theta point is 0.696 rather than 1/2, and describes an air/water " +
                "Langmuir monolayer under LATERAL compression. C-0002's bulk equation of state " +
                "stands. The residual effect is bounded independently at |d chi| <= 0.053.",
        verdict = "PASS — (a), with the bound of (b) supplied by an independent right-geometry " +
                "measurement rather than asserted",
        maturity = "TRL 1-3. Both inputs are fits to published measurement; nothing about the " +
                "Gen-1 layer is measured. PASS means model-consistent and traceable.",
        units = mapOf(
            "length (sources)" to "A",
            "length (exported)" to "nm",
            "grafting density" to "nm^-2",
            "area per chain" to "A^2/chain",
            "volume" to "nm^3",
            "pressure" to "pN/nm^2 (= MPa exactly)",
            "energy" to "pN.nm",
            "chi, excluded volume, ratios" to "dimensionless"
        ),
        conventions = listOf(
            "chi is on the WATER-MOLECULE Flory-Huggins site of C-0007: v = v0 (v0/v_site)(1-2chi), " +
                    "with v0/v_site = 2.010. A monomer-site chi is a different number.",
            "Volume fractions are PHYSICAL (N sigma v0 / h), never the Alexander-de Gennes reduced " +
                    "density n a^3, which for PEG is 1.408x smaller (CLAUDE.md).",
            "Positive d chi means a POORER solvent. Positive interaction ratio means a STRONGER " +
                    "repulsion.",
            "K is the des Cloizeaux amplitude at fixed physical monomer number density. In the " +
                    "Alexander-de Gennes single-length convention K ~ a^(15/4); in excluded " +
                    "volume K ~ v^(3/4) (blob) or v^1 (mean field), and both are carried.",
            "The interaction RATIO is convention-free; the absolute chi it is translated into is " +
                    "not, and inherits C-0003's finding that the Alexander-de Gennes unity " +
                    "prefactor is worth 6.6x in excluded volume. Only the ratio is load-bearing.",
            "Geometry: the Gen-1 layer is compressed NORMALLY against a rigid tile. A Langmuir " +
                    "surface pressure is a LATERAL one and is a different observable."
        ),
        validity = listOf(
            "The SCF chi values are parameters of Lee et al.'s own continuum model with unequal " +
                    "segment volumes (v_PEO = 59.2 A^3, v_water = 29.9 A^3). The paper states in " +
                    "section 3.3 that setting chi = 0.5 in that model does NOT reproduce theta " +
                    "behaviour, and locates its theta at chi ~ 0.696. Neither transfer onto the " +
                    "Flory-Huggins axis is licensed by the source; both are carried to show the gap.",
            "The compression fits are on PEG-lipid grafted to DSPC bilayers, N = 113, L0 = 10.5-10.9 " +
                    "nm — the Gen-1 chain length and the Gen-1 height, but at 1.5-2.5x the Gen-1 " +
                    "grafting density and 1.7-5.0x the Gen-1 volume fraction. The extrapolation to " +
                    "the Gen-1 window assumes any density-driven effect is MONOTONE in density, so " +
                    "that a bound obtained above the window bounds the window.",
            "Attributing the whole 3.56 -> 3.30 A drift in the fitted monomer length to solvent " +
                    "quality is an UPPER bound: Hansen et al. call the two values 'nearly constant' " +
                    "and treat the difference as fit scatter, not as physics.",
            "The Alexander-de Gennes height relation used to recover the grafting spacing is the " +
                    "one C-0003 REPLACED for the Gen-1 layer. It is used here because the question " +
                    "is what Hansen et al.'s fit means, and it means what their form says.",
            "Both sources are in pure water / D2O. The Gen-1 buffer is 2-10 mM MgCl2, which C-0007 " +
                    "shows moves the modulus by under 0.5 %.",
            "The n-cluster many-body attraction Lee et al. posit for PEO is NOT refuted here. What " +
                    "is refuted is the magnitude attributed to it, and only for normal compression."
        ),
        temperature = ROOM_TEMPERATURE,
        medium = "aqueous; the SCF fit is in D2O, the compression fit in water",
        thermalEnergy = thermalEnergy(),
        bulkChi = bulkChi,
        bulkChiSource = "C-0007, chi(T) = 1.156 - 235.3/T fitted by Pedersen & Sommer (2005) to " +
                "SAXS on PEG 4600 in D2O; water-molecule lattice site of volume " +
                "%.5f nm^3".format(waterSite),
        scfFits = scfReports,
        compressionFits = compressionReports,
        bounds = mapOf(
            "bulk chi at 300 K" to bulkChi,
            "compression-fitted effective chi, lowest" to chiBandAll.min(),
            "compression-fitted effective chi, highest" to chiBandAll.max(),
            "largest |d chi| from compression fits" to compressionShift,
            "interaction ratio, lowest" to ratioBandAll.min(),
            "interaction ratio, highest" to ratioBandAll.max(),
            "d chi claimed by the ratio transfer of the SCF fit" to scfRatioTransferShift,
            "d chi claimed by the offset transfer of the SCF fit" to scfOffsetTransferShift,
            "spread between the two SCF transfers" to transferSpreadAll,
            "spread as a fraction of the ratio-transfer shift" to
                    transferSpreadAll / scfRatioTransferShift,
            "ratio-transfer shift over the compression bound" to
                    scfRatioTransferShift / compressionShift,
            "density trend across the two compression fits, d chi" to
                    (COMPRESSION_FITS[1].effectiveChi(bulkChi) -
                            COMPRESSION_FITS[0].effectiveChi(bulkChi))
        ),
        exposure = mapOf(
            "stiffness exponent 1/(m+1), des Cloizeaux" to DES_CLOIZEAUX_STIFFNESS_EXPONENT,
            "stroke log-slope, inherited from C-0003" to C0003_STROKE_LOG_SLOPE,
            "stiffness ratio at the weakest bounded interaction" to
                    stiffnessRatioFromInteractionRatio(ratioBandAll.min()),
            "stiffness ratio at the strongest bounded interaction" to
                    stiffnessRatioFromInteractionRatio(ratioBandAll.max()),
            "stroke ratio at the weakest bounded interaction" to
                    strokeRatioFromInteractionRatio(ratioBandAll.min()),
            "stroke ratio at the strongest bounded interaction" to
                    strokeRatioFromInteractionRatio(ratioBandAll.max()),
            "C-0003's own 16x interaction span, stiffness ratio" to
                    stiffnessRatioFromInteractionRatio(16.0),
            "C-0003's own 16x interaction span, stroke ratio" to
                    strokeRatioFromInteractionRatio(16.0)
        ),
        searchNegatives = listOf(
            "EuropePMC REST search on EXT_ID:22616550 returns the record and the DOI but " +
                    "isOpenAccess = N, inPMC = N, hasPDF = N — no full text.",
            "Unpaywall (10.1021/jp301817e): is_oa = false, oa_locations empty, " +
                    "has_repository_copy = false. OpenAlex: oa_status = closed, " +
                    "best_oa_location = null. Both are WRONG — a free copy exists.",
            "The free copy is at NIST's own repository, tsapps.nist.gov/publication/" +
                    "get_pdf.cfm?pub_id=910992, because two authors are NIST NCNR staff. It is " +
                    "indexed by neither Unpaywall nor OpenAlex. ACS itself refuses.",
            "The Supporting Information (SCF equations S1.1-S1.26, Figures S1-S5, Table S1) is " +
                    "NOT in the NIST PDF and remains paywalled at ACS. The functional form of the " +
                    "SCF free energy is therefore still unread; the model's theta point is taken " +
                    "from the main text, where it is stated as a number.",
            "Hansen et al. (2003) is not in the PMC open-access subset (oa.fcgi answers " +
                    "idIsNotOpenAccess) and the EuropePMC fullTextXML endpoint returns empty, but " +
                    "the PMC article page at pmc.ncbi.nlm.nih.gov/articles/PMC1302616/ serves the " +
                    "complete body. Its inline equations are images and did not survive extraction."
        ),
        openQuestions = listOf(
            "The n-cluster many-body attraction itself is not bounded, only its consequence for " +
                    "NORMAL compression. Lee et al.'s observable is a LATERAL surface pressure and " +
                    "the two are not the same measurement.",
            "No compression measurement of a PEG brush INSIDE the Gen-1 grafting window exists in " +
                    "what was searched. Hansen et al. bound it from above, at 1.5-2.5x the density, " +
                    "and the transfer assumes monotonicity in grafting density.",
            "The SCF Supporting Information is unread, so the exact relation between that model's " +
                    "chi and a Flory-Huggins chi cannot be derived — only bracketed by the two " +
                    "linear transfers, which differ by 0.089.",
            "Hansen et al.'s two fitted monomer lengths do show the SIGN Lee et al. report — the " +
                    "denser layer is the poorer effective solvent, by 0.044 in chi. That is inside " +
                    "their own fit scatter and 5x smaller than the claimed shift, but it is not zero."
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/P-9-grafted-chi.json")
    output.parentFile.mkdirs()
    output.writeText(json.encodeToString(result) + "\n")

    println("=== P-9 — the effective chi of a GRAFTED PEG layer ".padEnd(100, '='))
    println()
    println("bulk chi at 300 K (C-0007, measured): %.4f".format(bulkChi))
    println()
    println("--- the SCF fit: a parameter of a model, at an air/water interface ".padEnd(100, '-'))
    println(
        "%-40s %9s %8s %8s %8s %9s %9s %7s".format(
            "condition", "sigma", "Sigma", "chi", "chi_th", "FH ratio", "FH offst", "inWin"
        )
    )
    scfReports.forEach {
        println(
            "%-40s %9.5f %8.3f %8.3f %8.3f %9.4f %9.4f %7s".format(
                it.label.take(40), it.graftingDensity, it.reducedGraftingDensity,
                it.fittedChi, it.modelThetaChi, it.floryHugginsByRatio,
                it.floryHugginsByOffset, it.insideGen1GraftingWindow
            )
        )
    }
    println()
    println("--- the compression fit: normal osmotic stress on a grafted layer ".padEnd(100, '-'))
    println(
        "%-36s %8s %9s %8s %9s %9s %8s %8s".format(
            "fit", "a [A]", "sigma", "phi", "K_b/K_bk", "chi_eff", "k ratio", "stroke"
        )
    )
    compressionReports.forEach {
        println(
            "%-36s %8.2f %9.5f %8.4f %9.4f %9.4f %8.4f %8.4f".format(
                it.label.take(36), it.fittedMonomerLength, it.graftingDensity,
                it.physicalVolumeFraction, it.interactionStrengthRatio, it.effectiveChi,
                it.stiffnessRatio, it.strokeRatio
            )
        )
    }
    println()
    println("--- bounds ".padEnd(100, '-'))
    result.bounds.forEach { (k, v) -> println("%-62s %12.5f".format(k, v)) }
    println()
    println("--- exposure through C-0003 ".padEnd(100, '-'))
    result.exposure.forEach { (k, v) -> println("%-62s %12.5f".format(k, v)) }
    println()
    println("still open:")
    result.openQuestions.forEach { println("  - ${it.take(140)}") }
    println()
    println("written: ${output.path}")
}
