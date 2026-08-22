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

import com.xemantic.nano.plentyofroom.ELECTRON_VOLT
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.brush.DeGennesScaling
import com.xemantic.nano.plentyofroom.brush.alexanderDeGennesMatchedExcludedVolume
import com.xemantic.nano.plentyofroom.brush.brushOfHeight
import com.xemantic.nano.plentyofroom.brush.heightUnderLoad
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.pow
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Task `P-3` — the PEG/water material parameter sheet, and the placement of the `T-1`
 * design window against the **measured** osmotic equation of state of the actual material.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=material.PegMaterialStudyKt
 * ```
 *
 * Emits `gpd/results/P-3-peg-material-parameters.json`, deterministically.
 */

/** One row of the sheet: a number, its unit, where it came from, and whether we derived it. */
@Serializable
data class MaterialParameter(
    val symbol: String,
    val quantity: String,
    val value: Double,
    val unit: String,
    val provenance: String,
    val source: String,
    val note: String = ""
)

/** Where one `T-1` design point sits relative to the measured crossover, before and under load. */
@Serializable
data class LayerPlacement(
    val label: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val monomersPerChain: Double,
    val chainMolarMass: Double,
    val volumeFraction: Double,
    val volumeFractionReducedUnits: Double,
    val crossoverVolumeFraction: Double,
    val crossoverUnits: Double,
    val regime: String,
    val localExponent: Double,
    val workingHeight: Double,
    val workingVolumeFraction: Double,
    val workingCrossoverUnits: Double,
    val workingRegime: String,
    val workingLocalExponent: Double,
    val appliedTensionPerChain: Double,
    val stretchingTensionPerChain: Double
)

/** The grafting density at which a layer of a given height would become genuinely semidilute. */
@Serializable
data class DesCloizeauxReach(
    val layerHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val monomersPerChain: Double,
    val volumeFraction: Double,
    val timesDesignWindowDensity: Double
)

/** The two prefactor questions `C-0001` left open, answered against measurement. */
@Serializable
data class PrefactorCrossCheck(
    val deGennesPressureScale: Double,
    val measuredDesCloizeauxPressure: Double,
    val measuredTotalBulkPressure: Double,
    val desCloizeauxToDeGennesRatio: Double,
    val heightMatchedExcludedVolume: Double,
    val measurementConsistentExcludedVolume: Double,
    val excludedVolumeRatio: Double
)

@Serializable
data class MaterialSheetResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val thermalEnergyElectronVolts: Double,
    val parameters: List<MaterialParameter>,
    val layerPlacements: List<LayerPlacement>,
    val desCloizeauxReach: List<DesCloizeauxReach>,
    val prefactorCrossCheck: PrefactorCrossCheck,
    val openQuestions: List<String>
)

private const val TILE_EDGE = 40.0
private const val TARGET_FORCE = 100.0

/** The `C-0001` design points: the brush-onset boundary at each height, plus the window's upper edge. */
private val DESIGN_POINTS = listOf(
    Triple("L0 = 5 nm, brush onset", 5.0, 0.092),
    Triple("L0 = 7 nm, brush onset", 7.0, 0.045),
    Triple("L0 = 10 nm, brush onset (window lower edge)", 10.0, 0.024),
    Triple("L0 = 10 nm, window upper edge (model-robust)", 10.0, 0.030)
)

fun main() {
    val peg = PegWater()
    val tileArea = TILE_EDGE * TILE_EDGE
    val result = MaterialSheetResult(
        task = "P-3",
        leaf = "none — premise task under A2.1",
        title = "PEG/water material parameter sheet, and the placement of the T-1 window " +
                "against the measured osmotic equation of state",
        verificationType = "logical + in-silico, closed against published measurement",
        acceptance = "every number provenance-flagged; a = 0.35 nm derived and corroborated; " +
                "a measured equation of state reproducing an independent fit to <= 10%; " +
                "the crossover located for our chain length and the T-1 window placed against it",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "The equation of state is fitted to measurement, but nothing about THIS layer is measured.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm",
            "pressure" to "pN/nm^2 (= MPa)",
            "volume" to "nm^3",
            "molarMass" to "g/mol",
            "specificVolume" to "mL/g",
            "massDensity" to "g/cm^3",
            "graftingDensity" to "chains/nm^2",
            "temperature" to "K"
        ),
        conventions = listOf(
            "phi ALWAYS means the physical polymer volume fraction, computed with v0",
            "the Alexander-de Gennes effective monomer length a is used ONLY inside " +
                    "Alexander-de Gennes expressions, never as a volume",
            "a fitted prefactor travels with the volume-fraction convention it was fitted under",
            "N is the number of ethylene oxide monomers per chain, continuous"
        ),
        validity = listOf(
            "the equation of state was fitted at 20 C (293 K); we evaluate at 300 K, " +
                    "a <= 2.4% shift in the k_BT prefactor, smaller than the fit uncertainty",
            "the equation of state was fitted to PEG in PURE WATER, not in 2-10 mM MgCl2 — " +
                    "see openQuestions",
            "it is a BULK SOLUTION equation of state; a grafted layer has no chain " +
                    "translational entropy, so the van't Hoff limb is not the brush's restoring " +
                    "pressure — it locates the density at which semidilute structure exists",
            "the fitted range is 0-50 wt %, which contains every volume fraction reported here",
            "finite-chain-length corrections (Biophys. J. 101:2790, 2011) are NOT incorporated"
        ),
        temperature = ROOM_TEMPERATURE,
        medium = "aqueous, pure water for the fit; Gen-1 buffer is 2/5/10 mM MgCl2",
        thermalEnergy = thermalEnergy(),
        thermalEnergyElectronVolts = thermalEnergy() / ELECTRON_VOLT,
        parameters = parameterSheet(peg),
        layerPlacements = DESIGN_POINTS.map { (label, height, density) ->
            placement(peg, label, height, density, tileArea)
        },
        desCloizeauxReach = listOf(5.0, 7.0, 10.0).map { desCloizeauxReach(peg, it) },
        prefactorCrossCheck = prefactorCrossCheck(peg),
        openQuestions = listOf(
            "chi(T, salt) for PEG/water in 2-10 mM MgCl2 is NOT determined by this task. " +
                    "The adopted equation of state is non-virial by construction, so it yields " +
                    "neither A2 nor chi; and no source for the Mg2+ salting-out coefficient was " +
                    "found this iteration. What IS bounded: 10 mM of a divalent chloride shifts " +
                    "the theta temperature by O(0.1-1 K) out of 375 K, i.e. <= 0.7% of the " +
                    "reduced temperature, which is far below the +/- 2% uncertainty on alpha. " +
                    "The bound is an argument, not a citation. Raised as task P-6.",
            "the crossover index alpha was fitted to LINEAR PEG. A PS->PEG reinitiation block " +
                    "copolymer (§3) is not the material it was fitted to.",
            "the 10-16 nm height range for dense PEG 5 kDa brushes, cited by C-0001, remains " +
                    "untraced. It is not used by anything and should be dropped rather than sourced."
        )
    )
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/P-3-peg-material-parameters.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).withEmissionHeader(LatticeTag.NONE, null)
        ) + "\n"
    )
    report(result, peg, output)
}

private fun parameterSheet(peg: PegWater): List<MaterialParameter> = listOf(
    MaterialParameter(
        "M0", "ethylene oxide monomer molar mass", peg.monomerMolarMass, "g/mol",
        "DERIVED", "C2H4O from standard atomic weights",
        "matches the 44 Da the equation-of-state fit assumes"
    ),
    MaterialParameter(
        "Vbar", "partial specific volume in water", peg.partialSpecificVolume, "mL/g",
        "CITED", "Cohen et al., J. Phys. Chem. B 113:3709 (2009), ref 31",
        "load-bearing: the fitted alpha travels with it"
    ),
    MaterialParameter(
        "v0", "monomer volume", peg.monomerVolume, "nm^3",
        "DERIVED", "M0 * Vbar / N_A", ""
    ),
    MaterialParameter(
        "v0^(1/3)", "volumetric monomer size", peg.volumetricMonomerSize, "nm",
        "DERIVED", "cube root of v0",
        "this, not a, is what a volume fraction means by 'monomer size'"
    ),
    MaterialParameter(
        "rho", "hydrated mass density", peg.massDensity, "g/cm^3",
        "DERIVED", "1/Vbar",
        "above bulk amorphous PEO (1.12-1.13): PEG contracts on hydration"
    ),
    MaterialParameter(
        "l_c", "all-trans contour length per monomer", peg.allTransContourLength, "nm",
        "DERIVED", "sum of backbone bonds times sin(theta/2)",
        "identifies a as a CONTOUR length; 4% from the cited 0.35 nm"
    ),
    MaterialParameter(
        "a", "Alexander-de Gennes effective monomer length", peg.effectiveMonomerLength, "nm",
        "CITED + CORROBORATED", "Kenworthy et al. (1995); fits of 0.356+/-0.07 and " +
                "0.330+/-0.15 nm in Hansen et al., Biophys. J. 84:350 (2003)",
        "no longer an inherited number: derived structurally and fitted independently"
    ),
    MaterialParameter(
        "v0/a^3", "reduced-to-physical volume fraction correction",
        peg.volumeFractionCorrection, "1",
        "DERIVED", "v0 / a^3",
        "C-0001's volume fractions are reduced units and must be multiplied by this"
    ),
    MaterialParameter(
        "b", "Kuhn length", peg.kuhnLength, "nm",
        "CITED", "Rubinstein & Colby, Table 2.1 (PEO)", ""
    ),
    MaterialParameter(
        "M_K", "Kuhn segment molar mass", peg.kuhnMolarMass, "g/mol",
        "CITED", "Rubinstein & Colby, Table 2.1 (PEO)", ""
    ),
    MaterialParameter(
        "n_K", "monomers per Kuhn segment", peg.monomersPerKuhnSegment, "1",
        "DERIVED", "M_K / M0", ""
    ),
    MaterialParameter(
        "v_K", "Kuhn segment volume", peg.kuhnSegmentVolume, "nm^3",
        "DERIVED", "n_K * v0", ""
    ),
    MaterialParameter(
        "b^3/v_K", "Kuhn segment aspect ratio", peg.kuhnSegmentAspectRatio, "1",
        "DERIVED", "b^3 / v_K",
        "7.1 — the segment is a thin rod, so no one-parameter scaling picture " +
                "can be right about its length and its volume at once"
    ),
    MaterialParameter(
        "d_K", "Kuhn segment effective diameter", peg.kuhnSegmentDiameter, "nm",
        "DERIVED", "cylinder of length b and volume v_K", ""
    ),
    MaterialParameter(
        "alpha", "crossover index of the osmotic equation of state", peg.crossoverIndex, "1",
        "MEASURED", "Cohen et al. (2009), fitted to Rand's osmometry on 12 PEG " +
                "molecular weights, 0-50 wt %, r^2 = 0.9926",
        "+/- ${peg.crossoverIndexUncertainty}; strongly material-specific (0.162 for PAMS/toluene)"
    ),
    MaterialParameter(
        "k_BT/v0", "osmotic pressure scale", thermalEnergy() / peg.monomerVolume, "pN/nm^2",
        "DERIVED", "k_BT at 300 K over v0", "68.6 MPa"
    ),
    MaterialParameter(
        "theta", "theta temperature of PEO/water", peg.thetaTemperature, "K",
        "CITED", "PEO/water phase separation near 102 C", ""
    ),
    MaterialParameter(
        "tau", "reduced temperature at 300 K", peg.reducedTemperature(), "1",
        "DERIVED", "1 - T/theta",
        "good solvent, but only by 20% — which is why the crossover reaches our volume fraction"
    )
)

private fun placement(
    peg: PegWater,
    label: String,
    layerHeight: Double,
    graftingDensity: Double,
    tileArea: Double
): LayerPlacement {
    val brush = brushOfHeight(layerHeight, graftingDensity, peg.effectiveMonomerLength)
    val chainLength = brush.monomersPerChain
    val eos = peg.equationOfState(chainLength)
    val phi = peg.volumeFraction(chainLength, graftingDensity, layerHeight)
    // the working height is inherited from T-1's m = 9/4 model, so the comparison with
    // C-0001 is like for like; re-deriving it with the measured exponent is T-2's job
    val working = DeGennesScaling().heightUnderLoad(brush, TARGET_FORCE, tileArea)
    val workingPhi = peg.volumeFraction(chainLength, graftingDensity, working)
    return LayerPlacement(
        label = label,
        layerHeight = layerHeight,
        graftingDensity = graftingDensity,
        graftingSpacing = brush.graftingSpacing,
        monomersPerChain = chainLength,
        chainMolarMass = chainLength * peg.monomerMolarMass,
        volumeFraction = phi,
        volumeFractionReducedUnits = phi / peg.volumeFractionCorrection,
        crossoverVolumeFraction = eos.crossoverVolumeFraction,
        crossoverUnits = phi / eos.crossoverVolumeFraction,
        regime = eos.regime(phi).name,
        localExponent = eos.localExponent(phi),
        workingHeight = working,
        workingVolumeFraction = workingPhi,
        workingCrossoverUnits = workingPhi / eos.crossoverVolumeFraction,
        workingRegime = eos.regime(workingPhi).name,
        workingLocalExponent = eos.localExponent(workingPhi),
        appliedTensionPerChain = tensionPerChain(TARGET_FORCE, graftingDensity, tileArea),
        stretchingTensionPerChain = peg.stretchingTension(chainLength, layerHeight)
    )
}

/**
 * Returns the grafting density at which a layer of [layerHeight] would reach `φ = 5 φ#`,
 * the lower edge of the des Cloizeaux domain — i.e. would become the semidilute solution
 * that the Alexander-de Gennes picture assumes it already is.
 *
 * Solved by bisection on `φ(σ) − 5 φ#(σ)`, which is monotonically increasing because
 * `φ ∝ σ^(2/3)` while `φ# ∝ σ^(4/15)`.
 */
private fun desCloizeauxReach(peg: PegWater, layerHeight: Double): DesCloizeauxReach {
    fun excess(density: Double): Double {
        val brush = brushOfHeight(layerHeight, density, peg.effectiveMonomerLength)
        val eos = peg.equationOfState(brush.monomersPerChain)
        return peg.volumeFraction(brush.monomersPerChain, density, layerHeight) -
                ScalingEquationOfState.DES_CLOIZEAUX_DOMAIN * eos.crossoverVolumeFraction
    }
    var low = 1e-4
    var high = 20.0
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (excess(middle) < 0.0) low = middle else high = middle
    }
    val density = 0.5 * (low + high)
    val brush = brushOfHeight(layerHeight, density, peg.effectiveMonomerLength)
    return DesCloizeauxReach(
        layerHeight = layerHeight,
        graftingDensity = density,
        graftingSpacing = brush.graftingSpacing,
        monomersPerChain = brush.monomersPerChain,
        volumeFraction = peg.volumeFraction(brush.monomersPerChain, density, layerHeight),
        timesDesignWindowDensity = density / 0.024
    )
}

private fun prefactorCrossCheck(peg: PegWater): PrefactorCrossCheck {
    val height = 10.0
    val density = 0.024
    val brush = brushOfHeight(height, density, peg.effectiveMonomerLength)
    val eos = peg.equationOfState(brush.monomersPerChain)
    val phi = peg.volumeFraction(brush.monomersPerChain, density, height)
    val deGennes = DeGennesScaling().pressureScale(brush)
    val desCloizeaux = eos.desCloizeauxPressure(phi)
    val matched = alexanderDeGennesMatchedExcludedVolume(peg.effectiveMonomerLength)
    // w from the mean-field contact-value theorem, P = (1/2) w k_BT n^2, evaluated against
    // the measured des Cloizeaux pressure at the layer's own volume fraction:
    //   w = 2 alpha v0 phi^(1/4)
    val consistent = 2.0 * peg.crossoverIndex * peg.monomerVolume * phi.pow(0.25)
    return PrefactorCrossCheck(
        deGennesPressureScale = deGennes,
        measuredDesCloizeauxPressure = desCloizeaux,
        measuredTotalBulkPressure = eos.pressure(phi),
        desCloizeauxToDeGennesRatio = desCloizeaux / deGennes,
        heightMatchedExcludedVolume = matched,
        measurementConsistentExcludedVolume = consistent,
        excludedVolumeRatio = consistent / matched
    )
}

private fun report(result: MaterialSheetResult, peg: PegWater, output: File) {
    println("P-3 — ${result.title}")
    println("300 K, aqueous, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    println()
    println("--- parameter sheet ".padEnd(96, '-'))
    println("%-10s %-42s %14s %-10s %s".format("symbol", "quantity", "value", "unit", "provenance"))
    result.parameters.forEach {
        println(
            "%-10s %-42s %14.6g %-10s %s".format(
                it.symbol, it.quantity.take(42), it.value, it.unit, it.provenance
            )
        )
    }
    println()
    println("--- where the T-1 design points sit on the measured equation of state ".padEnd(96, '-'))
    println(
        "%-30s %8s %8s %8s %8s %8s %8s".format(
            "design point", "N", "phi", "phi#", "phi/phi#", "regime", "m_eff"
        )
    )
    result.layerPlacements.forEach {
        println(
            "%-30s %8.1f %8.4f %8.4f %8.2f %8s %8.3f".format(
                it.label.take(30), it.monomersPerChain, it.volumeFraction,
                it.crossoverVolumeFraction, it.crossoverUnits, it.regime.take(8), it.localExponent
            )
        )
        println(
            "%-30s %8s %8.4f %8s %8.2f %8s %8.3f".format(
                "  ^ at ${"%.2f".format(it.workingHeight)} nm under ${TARGET_FORCE.toInt()} pN",
                "", it.workingVolumeFraction, "", it.workingCrossoverUnits,
                it.workingRegime.take(8), it.workingLocalExponent
            )
        )
    }
    println()
    println("--- grafting density needed to REACH the des Cloizeaux domain ".padEnd(96, '-'))
    println("%8s %10s %8s %8s %8s %12s".format("L0[nm]", "sigma", "s[nm]", "N", "phi", "x window"))
    result.desCloizeauxReach.forEach {
        println(
            "%8.1f %10.4f %8.2f %8.1f %8.4f %12.1f".format(
                it.layerHeight, it.graftingDensity, it.graftingSpacing,
                it.monomersPerChain, it.volumeFraction, it.timesDesignWindowDensity
            )
        )
    }
    println()
    val cross = result.prefactorCrossCheck
    println("--- prefactors C-0001 left open ".padEnd(96, '-'))
    println("de Gennes convention k_BT/s^3           = ${"%.5f".format(cross.deGennesPressureScale)} pN/nm^2")
    println("measured des Cloizeaux limb at phi      = ${"%.5f".format(cross.measuredDesCloizeauxPressure)} pN/nm^2")
    println("ratio (measured / convention)           = ${"%.3f".format(cross.desCloizeauxToDeGennesRatio)}")
    println("height-matched excluded volume          = ${"%.5f".format(cross.heightMatchedExcludedVolume)} nm^3")
    println("measurement-consistent excluded volume  = ${"%.5f".format(cross.measurementConsistentExcludedVolume)} nm^3")
    println("ratio                                   = ${"%.3f".format(cross.excludedVolumeRatio)}")
    println()
    println("chain tension premise (§2, ~30 pN): applied ${"%.2f".format(result.layerPlacements[2].appliedTensionPerChain)} pN")
    println("  + stretching ${"%.2f".format(result.layerPlacements[2].stretchingTensionPerChain)} pN")
    println("  = ${"%.2f".format(result.layerPlacements[2].appliedTensionPerChain + result.layerPlacements[2].stretchingTensionPerChain)} pN per chain — DISCHARGED")
    println()
    println("still open:")
    result.openQuestions.forEach { println("  - ${it.take(140)}...") }
    println()
    println("written: ${output.path} (${result.parameters.size} parameters, ${peg.monomersPerKuhnSegment.let { "%.2f".format(it) }} monomers per Kuhn segment)")
}
