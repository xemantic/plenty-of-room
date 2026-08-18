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

import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.anchoring.singleStrandedRadiusOfGyration
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * Task `T-195` — the **unpaired scaffold remainder** as a default body beside the actuated gap.
 * Leaf `A7.4`, with `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh electrostatics.ScaffoldRemainderStudyKt
 * ```
 *
 * Emits `gpd/results/T-195-scaffold-remainder.json`.
 *
 * NDI's answer to decision 5 (2026-08-18) — *"M13, circular ~7–8 K nucleotides"* — makes a
 * scaffold longer than the tile needs the **default**. `C-0086` measured that on the
 * **single-layer** sheet and found a 5 569 nt, 33 nm coil carrying 1.66× the sheet's own
 * backbone charge. Since then `C-0109` spent 6 720 nt on a four-layer tile and `C-0119`/`C-0120`
 * moved the recommended cross-section. This study asks the question at the **current** tile and
 * the **current** scaffold, and it runs its two cheap bounds before any field solve.
 */

// ------------------------------------------------------------------------------- the geometry

/** One candidate cross-section: what it pairs, and over what footprint. */
@Serializable
data class ScaffoldRemainderCrossSection(
    val name: String,
    val duplexes: Int,
    val basePairsPerRow: Int,
    val layers: Int,
    val alongHelices: Double,
    val acrossHelices: Double
) {
    /** One scaffold nucleotide per base pair of every duplex. */
    val pairedNucleotides: Int get() = duplexes * basePairsPerRow

    /** Scaffold **and** staples: the tile's whole phosphate count. */
    val backboneNucleotides: Int get() = 2 * pairedNucleotides

    val footprintArea: Double get() = alongHelices * acrossHelices
}

private val T195_CROSS_SECTIONS = listOf(
    ScaffoldRemainderCrossSection(
        "single layer, 15 x 112 bp (C-0086's sheet at the buildable width)",
        duplexes = 15, basePairsPerRow = 112, layers = 1,
        alongHelices = 38.08, acrossHelices = 40.35
    ),
    ScaffoldRemainderCrossSection(
        "four layer, 15 x 4 honeycomb (C-0109/C-0119)",
        duplexes = 60, basePairsPerRow = 112, layers = 4,
        alongHelices = 38.08, acrossHelices = 38.04
    ),
    ScaffoldRemainderCrossSection(
        "four layer, 10 x 6 honeycomb (C-0120, RECOMMENDED)",
        duplexes = 60, basePairsPerRow = 112, layers = 4,
        alongHelices = 38.08, acrossHelices = 25.36
    )
)

/** One candidate scaffold, with where its length was read. */
@Serializable
data class ScaffoldRemainderScaffold(
    val name: String,
    val nucleotides: Int,
    val provenance: String
)

private val T195_SCAFFOLDS = listOf(
    ScaffoldRemainderScaffold(
        "M13mp18 (wild type, circular)", M13MP18_NUCLEOTIDES,
        "CITED via C-0055; the scaffold C-0086 and C-0109 both budget against"
    ),
    ScaffoldRemainderScaffold(
        "p7560", P7560_NUCLEOTIDES,
        "READ DIRECTLY, Douglas et al. caDNAno PMC2731887 Methods: \"20 nM scaffold (p7560 or " +
                "p8064, derived from M13mp18)\"; the scaffold of design (ii), the 10 x 6 " +
                "cross-section C-0120 recommends"
    ),
    ScaffoldRemainderScaffold(
        "p8064", P8064_NUCLEOTIDES,
        "READ DIRECTLY, same passage; the scaffold of designs (i), (iii) and (v), i.e. of the " +
                "15 x 4 cross-section C-0119 reads as the programme's tile"
    )
)

// ------------------------------------------------------------------------------- the records

@Serializable
data class ScaffoldRemainderBudget(
    val crossSection: String,
    val scaffold: String,
    val scaffoldNucleotides: Int,
    val pairedNucleotides: Int,
    val remainderNucleotides: Int,
    val occupancy: Double,
    val bareChargeRatio: Double,
    val manningChargeRatioAtShortContour: Double,
    val manningChargeRatioAtLongContour: Double
)

@Serializable
data class ScaffoldRemainderCoil(
    val crossSection: String,
    val scaffold: String,
    val remainderNucleotides: Int,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val contourLength: Double,
    val kuhnSegments: Double,
    val idealRadiusOfGyration: Double,
    val floryEndToEnd: Double
)

@Serializable
data class ScaffoldRemainderConfinement(
    val remainderNucleotides: Int,
    val gapHeight: Double,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val idealFreeEnergy: Double,
    val swollenFreeEnergy: Double,
    val weakerFreeEnergy: Double,
    val boltzmannWeight: Double,
    val penetratingNucleotides: Double,
    val penetratingFraction: Double
)

@Serializable
data class ScaffoldRemainderSaturation(
    val wall: String,
    val crossSection: String,
    val scaffold: String,
    val concentration: Double,
    val contourPerNucleotide: Double,
    val bareGapFacingCharge: Double,
    val addedCharge: Double,
    val addedOverBare: Double,
    val effectiveNominal: Double,
    val effectivePerturbed: Double,
    val saturatedCeiling: Double,
    val saturationFractionNominal: Double,
    val effectiveRelativeMovement: Double
)

@Serializable
data class ScaffoldRemainderBiasReRead(
    val scenario: String,
    val wall: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val nominalCharge: Double,
    val addedCharge: Double,
    val diffuseNominal: Double,
    val loadNominal: Double,
    val diffusePerturbed: Double,
    val loadAtSameBias: Double,
    val loadRelativeMovement: Double,
    val biasAtSameLoad: Double,
    val biasRelativeMovement: Double,
    val collarWidthCeiling: Double,
    val collarWidthCeilingPerturbed: Double
)

@Serializable
data class ScaffoldRemainderReproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val recovered: Double,
    val relativeDeparture: Double
)

@Serializable
data class ScaffoldRemainderConvergence(
    val quantity: String,
    val coarseNodes: Int,
    val fineNodes: Int,
    val coarse: Double,
    val fine: Double,
    val relativeDeparture: Double
)

@Serializable
data class ScaffoldRemainderPredicate(
    val id: String,
    val statement: String,
    val verdict: String,
    val evidence: String
)

@Serializable
data class ScaffoldRemainderFalsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
data class ScaffoldRemainderResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: String,
    val conventions: List<String>,
    val citedInputs: List<String>,
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val bjerrumLength: Double,
    val cheapBound: Map<String, String>,
    val scaffolds: List<ScaffoldRemainderScaffold>,
    val crossSections: List<ScaffoldRemainderCrossSection>,
    val budgets: List<ScaffoldRemainderBudget>,
    val coils: List<ScaffoldRemainderCoil>,
    val confinement: List<ScaffoldRemainderConfinement>,
    val saturation: List<ScaffoldRemainderSaturation>,
    val biasReReads: List<ScaffoldRemainderBiasReRead>,
    val reproductions: List<ScaffoldRemainderReproduction>,
    val convergence: List<ScaffoldRemainderConvergence>,
    val predicates: List<ScaffoldRemainderPredicate>,
    val falsifiers: List<ScaffoldRemainderFalsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val parameters: Map<String, Double>
)

// -------------------------------------------------------------------------- the fixed inputs

private const val T195_STERN_CAPACITANCE = 20.0
private const val T195_SEARCH_NODES = 800
private const val T195_RATIO_NODES = 800
private const val T195_DUPLEX_CHARGE_SPACING = 0.17
private const val T195_COUNTERION_VALENCY = 2

/** `C-0022`'s own gap-facing tile charge, `e/nm²`, **signed negative** — reproduced, not copied. */
private fun t195TileCharge(): Double {
    val tile = DnaOrigamiTile()
    return -tile.projectedChargeDensity * tile.manningSurvivingFraction(2, bjerrumLength()) / 2.0
}

private val T195_KUHN_LENGTHS = listOf(
    SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY,
    SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY_TWO_MILLIMOLAR,
    SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
    SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR
)

private val T195_CONTOURS = listOf(
    SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN,
    SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MAX
)

private val T195_CONCENTRATIONS = listOf(0.5, 2.0, 10.0)
private val T195_GAPS = listOf(2.0, 5.0, 7.0, 10.0)

/** One of `C-0022`'s solved operating states, read from its own result file. */
private data class T195State(
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val publishedLoad: Double,
    val source: String
)

private fun t195States(file: File): List<T195State> {
    val root = Json.parseToJsonElement(file.readText())
    val profiles = root as kotlinx.serialization.json.JsonObject
    val array = profiles["profiles"] as kotlinx.serialization.json.JsonArray
    return array.map { element ->
        val record = element as kotlinx.serialization.json.JsonObject
        fun number(key: String) =
            (record[key] as kotlinx.serialization.json.JsonPrimitive).content.toDouble()
        fun text(key: String) =
            (record[key] as kotlinx.serialization.json.JsonPrimitive).content
        T195State(
            number("concentration"), number("gapHeight"), number("appliedBias"),
            number("oneDimensionalLoad"), text("biasSource")
        )
    }
}

// ------------------------------------------------------------------------------ the plumbing

private fun t195Diffuse(
    concentration: Double,
    gapHeight: Double,
    appliedBias: Double,
    tileCharge: Double,
    nodes: Int = T195_SEARCH_NODES
): Double = diffusePotentialOfAppliedBias(
    gapHeight, appliedBias, tileCharge, sternChargeDensityPerVolt(T195_STERN_CAPACITANCE),
    IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
    uniformMedium(GapMedium()), bjerrumLength(), nodes = nodes
)

private fun t195Load(
    concentration: Double,
    gapHeight: Double,
    diffuse: Double,
    tileCharge: Double,
    nodes: Int
): Double = -PoissonBoltzmannGap(
    gapHeight, IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
    uniformMedium(GapMedium()), bjerrumLength(), nodes = nodes
).solve(diffuse / thermalVoltage(), tileCharge)
    .disjoiningPressureInPiconewtonPerSquareNanometre

/**
 * The applied bias that restores [targetLoad] on a tile carrying [tileCharge], bisected on the
 * **diffuse-layer drop** and converted once at the end — `CLAUDE.md`'s *"parametrise a biased gap
 * by the diffuse-layer drop, not the applied bias"*, which is a factor of 35 in solves.
 */
private fun t195BiasForLoad(
    concentration: Double,
    gapHeight: Double,
    tileCharge: Double,
    targetLoad: Double,
    nodes: Int
): Double {
    var low = 0.0
    var high = 0.35
    repeat(48) {
        val middle = 0.5 * (low + high)
        if (t195Load(concentration, gapHeight, middle, tileCharge, nodes) < targetLoad) low = middle
        else high = middle
    }
    val diffuse = 0.5 * (low + high)
    return appliedBiasOfDiffusePotential(
        gapHeight, diffuse, tileCharge, sternChargeDensityPerVolt(T195_STERN_CAPACITANCE),
        IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
        uniformMedium(GapMedium()), bjerrumLength(), nodes = nodes
    )
}

private fun t195Departure(published: Double, recovered: Double): Double =
    if (published == 0.0) abs(recovered) else abs(recovered - published) / abs(published)

// ------------------------------------------------------------------------------------ the run

fun main() {
    val lb = bjerrumLength()
    val duplexFraction =
        manningSurvivingFractionOfSpacing(T195_DUPLEX_CHARGE_SPACING, T195_COUNTERION_VALENCY, lb)
    val singleFractionShort =
        manningSurvivingFractionOfSpacing(T195_CONTOURS.first(), T195_COUNTERION_VALENCY, lb)
    val singleFractionLong =
        manningSurvivingFractionOfSpacing(T195_CONTOURS.last(), T195_COUNTERION_VALENCY, lb)

    println("T-195 — the Manning asymmetry, before anything else:")
    println("  duplex b = 0.17 nm -> %.6f survives".format(duplexFraction))
    println("  ssDNA  b = 0.57 nm -> %.6f survives (%.3fx)".format(
        singleFractionShort, singleFractionShort / duplexFraction))
    println("  ssDNA  b = 0.70 nm -> %.6f survives (%.3fx)".format(
        singleFractionLong, singleFractionLong / duplexFraction))

    // -------------------------------------------------------------- deliverable 1: the budget
    val budgets = mutableListOf<ScaffoldRemainderBudget>()
    for (section in T195_CROSS_SECTIONS) {
        for (scaffold in T195_SCAFFOLDS) {
            val remainder = unpairedRemainder(scaffold.nucleotides, section.pairedNucleotides)
            budgets += ScaffoldRemainderBudget(
                crossSection = section.name,
                scaffold = scaffold.name,
                scaffoldNucleotides = scaffold.nucleotides,
                pairedNucleotides = section.pairedNucleotides,
                remainderNucleotides = remainder,
                occupancy = section.pairedNucleotides.toDouble() / scaffold.nucleotides,
                bareChargeRatio = bareChargeRatio(remainder, section.backboneNucleotides),
                manningChargeRatioAtShortContour = manningChargeRatio(
                    remainder, section.backboneNucleotides, singleFractionShort, duplexFraction
                ),
                manningChargeRatioAtLongContour = manningChargeRatio(
                    remainder, section.backboneNucleotides, singleFractionLong, duplexFraction
                )
            )
        }
    }
    println("T-195 — the budget, at the current tile and the current scaffold:")
    for (row in budgets.filter { it.crossSection.contains("RECOMMENDED") }) {
        println("  %s: remainder %d nt, bare %.4f, Manning %.4f-%.4f".format(
            row.scaffold, row.remainderNucleotides, row.bareChargeRatio,
            row.manningChargeRatioAtShortContour, row.manningChargeRatioAtLongContour))
    }

    // ------------------------------------------------ deliverable 2: the coil, and cheap bound 2
    val coils = mutableListOf<ScaffoldRemainderCoil>()
    val distinctRemainders = budgets.map { it.remainderNucleotides }.distinct().sorted()
    for (row in budgets) {
        for (kuhn in T195_KUHN_LENGTHS) {
            for (contour in T195_CONTOURS) {
                coils += ScaffoldRemainderCoil(
                    crossSection = row.crossSection,
                    scaffold = row.scaffold,
                    remainderNucleotides = row.remainderNucleotides,
                    kuhnLength = kuhn,
                    contourPerNucleotide = contour,
                    contourLength = row.remainderNucleotides * contour,
                    kuhnSegments = row.remainderNucleotides * contour / kuhn,
                    idealRadiusOfGyration =
                        singleStrandedRadiusOfGyration(row.remainderNucleotides, kuhn, contour),
                    floryEndToEnd =
                        singleStrandedFloryRadius(row.remainderNucleotides, kuhn, contour)
                )
            }
        }
    }

    val confinement = mutableListOf<ScaffoldRemainderConfinement>()
    for (remainder in distinctRemainders) {
        for (gap in T195_GAPS) {
            for (kuhn in T195_KUHN_LENGTHS) {
                for (contour in T195_CONTOURS) {
                    val ideal = idealSlitConfinementFreeEnergy(
                        singleStrandedRadiusOfGyration(remainder, kuhn, contour), gap
                    )
                    val swollen = swollenSlitConfinementFreeEnergy(
                        singleStrandedFloryRadius(remainder, kuhn, contour), gap
                    )
                    val weaker = minOf(ideal, swollen)
                    val penetrating = slitPenetratingNucleotides(gap, kuhn, contour, remainder)
                    confinement += ScaffoldRemainderConfinement(
                        remainderNucleotides = remainder,
                        gapHeight = gap,
                        kuhnLength = kuhn,
                        contourPerNucleotide = contour,
                        idealFreeEnergy = ideal,
                        swollenFreeEnergy = swollen,
                        weakerFreeEnergy = weaker,
                        boltzmannWeight = kotlin.math.exp(-weaker),
                        penetratingNucleotides = penetrating,
                        penetratingFraction = penetrating / remainder
                    )
                }
            }
        }
    }
    val weakestConfinement = confinement.minOf { it.weakerFreeEnergy }
    val largestPenetratingFraction = confinement.maxOf { it.penetratingFraction }
    println("T-195 — cheap bound 2 (confinement): the WEAKEST expulsion anywhere in the bracket " +
            "is %.3f k_BT, and at most %.4f of the remainder threads the gap".format(
                weakestConfinement, largestPenetratingFraction))

    // ------------------------------------------------- deliverable 3: cheap bound 1, saturation
    val tileCharge = t195TileCharge()
    val saturation = mutableListOf<ScaffoldRemainderSaturation>()
    val walls = buildList {
        add(Triple("C-0022's as-solved 40 x 40 tile", abs(tileCharge), 1600.0))
        for (section in T195_CROSS_SECTIONS) {
            add(
                Triple(
                    section.name,
                    section.pairedNucleotides / section.footprintArea * duplexFraction,
                    section.footprintArea
                )
            )
        }
    }
    for ((wallName, bare, area) in walls) {
        for (section in T195_CROSS_SECTIONS) {
            for (scaffold in T195_SCAFFOLDS) {
                val remainder = unpairedRemainder(scaffold.nucleotides, section.pairedNucleotides)
                for (contour in T195_CONTOURS) {
                    val fraction =
                        manningSurvivingFractionOfSpacing(contour, T195_COUNTERION_VALENCY, lb)
                    val added = smearedRemainderChargeDensity(remainder, fraction, area)
                    for (concentration in T195_CONCENTRATIONS) {
                        val buffer = MagnesiumChlorideBuffer(concentration)
                        val kappa = buffer.inverseDebyeLength()
                        val nominal = negativeWallEffectiveChargeDensity(bare, kappa, lb)
                        val perturbed =
                            negativeWallEffectiveChargeDensity(bare + added, kappa, lb)
                        val ceiling = asymmetricSaturatedEffectiveChargeDensity(
                            kappa, lb, negativeSurface = true
                        )
                        saturation += ScaffoldRemainderSaturation(
                            wall = wallName,
                            crossSection = section.name,
                            scaffold = scaffold.name,
                            concentration = concentration,
                            contourPerNucleotide = contour,
                            bareGapFacingCharge = bare,
                            addedCharge = added,
                            addedOverBare = added / bare,
                            effectiveNominal = nominal,
                            effectivePerturbed = perturbed,
                            saturatedCeiling = ceiling,
                            saturationFractionNominal = nominal / ceiling,
                            effectiveRelativeMovement = perturbed / nominal - 1.0
                        )
                    }
                }
            }
        }
    }
    // The bound that matters is read on the wall the section belongs to: a remainder is smeared
    // over its OWN tile's footprint, never over another's.
    fun ownWall(row: ScaffoldRemainderSaturation) =
        row.wall == row.crossSection ||
                (row.wall.startsWith("C-0022") && row.crossSection.contains("RECOMMENDED"))
    val ownSaturation = saturation.filter { ownWall(it) }
    val recommendedWorst = ownSaturation
        .filter { it.crossSection.contains("RECOMMENDED") && it.wall.contains("RECOMMENDED") }
        .maxOf { it.effectiveRelativeMovement }
    val singleLayerWorst = ownSaturation
        .filter { it.crossSection.startsWith("single layer") && it.wall.startsWith("single layer") }
        .maxOf { it.effectiveRelativeMovement }
    val asSolvedWorst = ownSaturation
        .filter { it.wall.startsWith("C-0022") }
        .maxOf { it.effectiveRelativeMovement }
    fun onM13(rows: List<ScaffoldRemainderSaturation>) =
        rows.filter { it.scaffold.startsWith("M13mp18") }.maxOf { it.effectiveRelativeMovement }
    val singleLayerOnM13 = onM13(ownSaturation.filter {
        it.crossSection.startsWith("single layer") && it.wall.startsWith("single layer")
    })
    val recommendedOnM13 = onM13(ownSaturation.filter {
        it.crossSection.contains("RECOMMENDED") && it.wall.contains("RECOMMENDED")
    })
    println("T-195 — cheap bound 1 (saturation), WORST case, whole remainder on the closest plane:")
    println("  single-layer sheet         %.4f".format(singleLayerWorst))
    println("  10 x 6 four-layer tile     %.4f".format(recommendedWorst))
    println("  C-0022's as-solved tile    %.4f".format(asSolvedWorst))

    // -------------------------------------- deliverable 4: the bias and the edge load, re-read
    val states = t195States(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val reproductions = mutableListOf<ScaffoldRemainderReproduction>()
    for (state in states) {
        val diffuse = t195Diffuse(state.concentration, state.gapHeight, state.appliedBias, tileCharge)
        val load = t195Load(
            state.concentration, state.gapHeight, diffuse, tileCharge,
            nodes = max(4000, (state.gapHeight * 1200.0).toInt())
        )
        reproductions += ScaffoldRemainderReproduction(
            "C-0022",
            "the 1-D load at %.1f mM, %.1f nm, %.3f V".format(
                state.concentration, state.gapHeight, state.appliedBias
            ),
            state.publishedLoad, load, t195Departure(state.publishedLoad, load)
        )
    }
    val worstReproduction = reproductions.maxOf { it.relativeDeparture }
    check(worstReproduction < 1e-6) {
        "F2 FIRED: the 1-D pipeline does not reproduce C-0022, worst departure $worstReproduction"
    }
    println("T-195 — gate: all %d of C-0022's one-dimensional loads reproduce, worst %.2e"
        .format(states.size, worstReproduction))

    // the two perturbations: the unconditional bound, and the penetration-limited estimate
    val recommended = T195_CROSS_SECTIONS.last()
    val boundRemainder = unpairedRemainder(P8064_NUCLEOTIDES, recommended.pairedNucleotides)
    val designRemainder = unpairedRemainder(P7560_NUCLEOTIDES, recommended.pairedNucleotides)
    val boundCharge = smearedRemainderChargeDensity(boundRemainder, singleFractionLong, 1600.0)

    val biasReReads = mutableListOf<ScaffoldRemainderBiasReRead>()
    for (state in states) {
        val kappa = MagnesiumChlorideBuffer(state.concentration).inverseDebyeLength()
        val penetrating = slitPenetratingNucleotides(
            state.gapHeight, SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
            SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN, designRemainder
        )
        val designCharge = penetrating * singleFractionShort / 1600.0
        val scenarios = listOf(
            Triple(
                "unconditional bound: the whole p8064 remainder (%d nt) on the gap-facing plane"
                    .format(boundRemainder),
                boundCharge, "C-0022's as-solved 40 x 40 tile"
            ),
            Triple(
                "penetration-limited: %.1f nt of the p7560 remainder inside the gap"
                    .format(penetrating),
                designCharge, "C-0022's as-solved 40 x 40 tile"
            )
        )
        val nominalDiffuse =
            t195Diffuse(state.concentration, state.gapHeight, state.appliedBias, tileCharge)
        val nominalLoad = t195Load(
            state.concentration, state.gapHeight, nominalDiffuse, tileCharge, T195_RATIO_NODES
        )
        for ((scenario, added, wall) in scenarios) {
            val perturbedCharge = tileCharge - added
            val perturbedDiffuse = t195Diffuse(
                state.concentration, state.gapHeight, state.appliedBias, perturbedCharge
            )
            val perturbedLoad = t195Load(
                state.concentration, state.gapHeight, perturbedDiffuse, perturbedCharge,
                T195_RATIO_NODES
            )
            val restoredBias = t195BiasForLoad(
                state.concentration, state.gapHeight, perturbedCharge, nominalLoad,
                T195_RATIO_NODES
            )
            biasReReads += ScaffoldRemainderBiasReRead(
                scenario = scenario,
                wall = wall,
                concentration = state.concentration,
                gapHeight = state.gapHeight,
                appliedBias = state.appliedBias,
                nominalCharge = tileCharge,
                addedCharge = -added,
                diffuseNominal = nominalDiffuse,
                loadNominal = nominalLoad,
                diffusePerturbed = perturbedDiffuse,
                loadAtSameBias = perturbedLoad,
                loadRelativeMovement = perturbedLoad / nominalLoad - 1.0,
                biasAtSameLoad = restoredBias,
                biasRelativeMovement = restoredBias / state.appliedBias - 1.0,
                collarWidthCeiling = 1.0 / transverseDecayRateBound(kappa, state.gapHeight),
                collarWidthCeilingPerturbed = 1.0 / transverseDecayRateBound(kappa, state.gapHeight)
            )
        }
    }
    val boundRows = biasReReads.filter { it.scenario.startsWith("unconditional") }
    val designRows = biasReReads.filter { it.scenario.startsWith("penetration") }
    val worstBoundBias = boundRows.maxOf { abs(it.biasRelativeMovement) }
    val worstDesignBias = designRows.maxOf { abs(it.biasRelativeMovement) }
    val worstBoundLoad = boundRows.maxOf { abs(it.loadRelativeMovement) }
    val worstDesignLoad = designRows.maxOf { abs(it.loadRelativeMovement) }
    println("T-195 — the bias re-read over all %d of C-0022's states:".format(states.size))
    println("  unconditional bound: load moves up to %.4f, bias up to %.4f"
        .format(worstBoundLoad, worstBoundBias))
    println("  penetration-limited: load moves up to %.4f, bias up to %.4f"
        .format(worstDesignLoad, worstDesignBias))

    // --------------------------------------------------------------------------- convergence
    val convergence = mutableListOf<ScaffoldRemainderConvergence>()
    val probe = states.first { it.concentration == 2.0 && it.gapHeight == 10.0 }
    for ((coarse, fine) in listOf(400 to 800, 800 to 1600)) {
        fun movement(nodes: Int): Double {
            val nominalDiffuse =
                t195Diffuse(probe.concentration, probe.gapHeight, probe.appliedBias, tileCharge, nodes)
            val nominalLoad =
                t195Load(probe.concentration, probe.gapHeight, nominalDiffuse, tileCharge, nodes)
            val perturbed = tileCharge - boundCharge
            val perturbedDiffuse =
                t195Diffuse(probe.concentration, probe.gapHeight, probe.appliedBias, perturbed, nodes)
            val perturbedLoad =
                t195Load(probe.concentration, probe.gapHeight, perturbedDiffuse, perturbed, nodes)
            return perturbedLoad / nominalLoad - 1.0
        }
        val a = movement(coarse)
        val b = movement(fine)
        convergence += ScaffoldRemainderConvergence(
            "the worst-case load movement at 2 mM, 10 nm", coarse, fine, a, b,
            t195Departure(a, b)
        )
    }

    // ------------------------------------------------------- more upstream reproductions
    val singleLayer = T195_CROSS_SECTIONS.first()
    val c0086Remainder = unpairedRemainder(M13MP18_NUCLEOTIDES, singleLayer.pairedNucleotides)
    reproductions += ScaffoldRemainderReproduction(
        "C-0086", "the unpaired remainder on the single-layer sheet, nt",
        5569.0, c0086Remainder.toDouble(), t195Departure(5569.0, c0086Remainder.toDouble())
    )
    reproductions += ScaffoldRemainderReproduction(
        "C-0086", "the ideal coil radius of gyration, nm", 33.3,
        singleStrandedRadiusOfGyration(
            c0086Remainder, SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
            SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN
        ),
        t195Departure(
            33.3,
            singleStrandedRadiusOfGyration(
                c0086Remainder, SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
                SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN
            )
        )
    )
    val c0086Ratio = bareChargeRatio(c0086Remainder, singleLayer.backboneNucleotides)
    reproductions += ScaffoldRemainderReproduction(
        "C-0086", "the remainder over the sheet's own bare backbone charge",
        1.66, c0086Ratio, t195Departure(1.66, c0086Ratio)
    )
    val fourLayerRemainder = unpairedRemainder(M13MP18_NUCLEOTIDES, recommended.pairedNucleotides)
    reproductions += ScaffoldRemainderReproduction(
        "C-0109/C-0119", "the four-layer remainder on M13mp18, nt",
        529.0, fourLayerRemainder.toDouble(), t195Departure(529.0, fourLayerRemainder.toDouble())
    )
    reproductions += ScaffoldRemainderReproduction(
        "C-0119", "the four-layer remainder on p8064, nt",
        1344.0, boundRemainder.toDouble(), t195Departure(1344.0, boundRemainder.toDouble())
    )
    reproductions += ScaffoldRemainderReproduction(
        "C-0119", "the four-layer scaffold occupancy on M13mp18",
        0.927, recommended.pairedNucleotides / M13MP18_NUCLEOTIDES.toDouble(),
        t195Departure(0.927, recommended.pairedNucleotides / M13MP18_NUCLEOTIDES.toDouble())
    )
    val saturationAtTwo = asymmetricSaturatedEffectiveChargeDensity(
        MagnesiumChlorideBuffer(2.0).inverseDebyeLength(), lb, negativeSurface = true
    )
    reproductions += ScaffoldRemainderReproduction(
        "C-0008", "the saturated effective charge density at 2 mM, e/nm^2",
        0.04562, saturationAtTwo, t195Departure(0.04562, saturationAtTwo)
    )

    // ---------------------------------------------------------------------------- the verdict
    val recommendedBudget = budgets.first {
        it.crossSection.contains("RECOMMENDED") && it.scaffold == "p7560"
    }
    val singleLayerBudget = budgets.first {
        it.crossSection.startsWith("single layer") && it.scaffold.startsWith("M13mp18")
    }
    val exposureReduction = singleLayerWorst / recommendedWorst

    val predicates = listOf(
        ScaffoldRemainderPredicate(
            "P1", "the scaffold each cross-section is folded from is quoted from a source read " +
                    "directly, with its nucleotide count",
            "PASS",
            "p7560 and p8064, Douglas et al. PMC2731887 Methods, already in gpd/data/T-151-sources/; " +
                    "design (ii) is the 10 x 6 cross-section C-0120 recommends and it is folded " +
                    "from p7560, so the remainder at the recommended tile is %d nt"
                        .format(recommendedBudget.remainderNucleotides)
        ),
        ScaffoldRemainderPredicate(
            "P2", "the charge ledger, bare and Manning, with C-0086's 1.66x reproduced",
            if (reproductions.filter { it.source == "C-0086" }.maxOf { it.relativeDeparture } < 1e-2)
                "PASS" else "FAIL",
            ("the recommended tile carries %.4f bare and %.4f-%.4f Manning against the single " +
                    "layer's %.4f and its Manning %.4f-%.4f").format(
                        recommendedBudget.bareChargeRatio,
                        recommendedBudget.manningChargeRatioAtShortContour,
                        recommendedBudget.manningChargeRatioAtLongContour,
                        singleLayerBudget.bareChargeRatio,
                        singleLayerBudget.manningChargeRatioAtShortContour,
                        singleLayerBudget.manningChargeRatioAtLongContour
                    )
        ),
        ScaffoldRemainderPredicate(
            "P3", "the unconditional saturation bound is smaller than C-0022's own model spreads",
            if (recommendedWorst < 0.072) "PASS" else "FAIL",
            ("the whole remainder on the closest plane moves sigma_eff by at most %.4f at the " +
                    "recommended tile and %.4f at C-0022's as-solved charge, against C-0008's " +
                    "0.072 charge-reading ambiguity, C-0034's 0.147 fringing and C-0005's " +
                    "1.23-2.14 mean-field bracket").format(recommendedWorst, asSolvedWorst)
        ),
        ScaffoldRemainderPredicate(
            "P4", "the coil is larger than the gap everywhere in the ssDNA bracket, and the " +
                    "penetration count is stated",
            if (weakestConfinement > 1.0) "PASS" else "FAIL",
            ("the weakest expulsion anywhere in the 4 x 2 Kuhn/contour bracket at the widest gap " +
                    "is %.3f k_BT, and at most %.4f of the remainder threads the slit")
                .format(weakestConfinement, largestPenetratingFraction)
        ),
        ScaffoldRemainderPredicate(
            "P5", "the bias moves less than the bracket C-0008 already carries at the same states",
            if (worstBoundLoad < 0.56) "PASS" else "FAIL",
            ("over all %d of C-0022's states the unconditional bound moves the load by at most " +
                    "%.4f and the held bias by %.4f, against the +0.56 the Bikerman finite-ion " +
                    "correction C-0008 already carries; the penetration-limited estimate moves " +
                    "them by %.4f and %.4f. The collar WIDTH is 1/q0 with " +
                    "q0^2 >= kappa^2 + (pi/2h)^2 and carries no surface charge at all, so it " +
                    "does not move").format(
                        states.size, worstBoundLoad, worstBoundBias,
                        worstDesignLoad, worstDesignBias
                    )
        )
    )

    val falsifiers = listOf(
        ScaffoldRemainderFalsifier(
            "F1", "the worst-case sigma_eff perturbation exceeds C-0022's standing model spreads",
            recommendedWorst >= 0.072,
            "the recommended tile's worst case is %.4f; the field solve is not owed"
                .format(recommendedWorst)
        ),
        ScaffoldRemainderFalsifier(
            "F2", "C-0086's coil and C-0022's 21 one-dimensional loads do not reproduce",
            worstReproduction >= 1e-6,
            "worst load departure %.2e over %d states".format(worstReproduction, states.size)
        ),
        ScaffoldRemainderFalsifier(
            "F3", "the coil is smaller than the gap somewhere in the ssDNA bracket",
            weakestConfinement <= 1.0,
            "the weakest expulsion is %.3f k_BT, at the largest gap and the smallest remainder"
                .format(weakestConfinement)
        ),
        ScaffoldRemainderFalsifier(
            "F4", "the re-read moves further than C-0008's own Bikerman bracket at the same state",
            worstBoundLoad >= 0.56,
            ("the largest load movement under the unconditional bound is %.4f and the largest " +
                    "bias movement %.4f").format(worstBoundLoad, worstBoundBias)
        ),
        ScaffoldRemainderFalsifier(
            "F5", "the four-layer tile does not reduce the exposure",
            exposureReduction <= 1.0,
            ("the same bound is %.4f on the single-layer sheet and %.4f on the recommended " +
                    "four-layer tile, a reduction of %.2fx").format(
                        singleLayerWorst, recommendedWorst, exposureReduction
                    )
        )
    )

    val findings = listOf(
        ("WHICH REMAINDER IS REAL: %d nt, and it is p7560 rather than M13mp18 or p8064. The " +
                "caDNAno paper folds every one of its 60-helix blocks from p7560 and its " +
                "64-helix blocks from p8064, and design (ii) — 10 x 6 — is the cross-section " +
                "C-0120 recommends. C-0109's 529 is M13mp18's number and C-0119's 1 344 is the " +
                "15 x 4 cross-section C-0120 replaced.").format(
                    recommendedBudget.remainderNucleotides
                ),
        ("THE FOUR-LAYER TILE HAS SPENT THE EXCESS, AND THE REDUCTION IS %.1fx IN THE QUANTITY " +
                "THAT MATTERS. The whole remainder smeared on the tile's own gap-facing plane " +
                "moves sigma_eff by at most %.4f on the recommended tile against %.4f on " +
                "C-0086's single-layer sheet. The single-layer number is NOT bounded away and " +
                "would have owed a field solve; the four-layer number is inside C-0008's own " +
                "0.072 charge-reading ambiguity.").format(
                    exposureReduction, recommendedWorst, singleLayerWorst
                ),
        ("THE MANNING FRACTIONS OF THE TWO BODIES DIFFER BY %.2fx AND C-0086 COMPARED THEM ON " +
                "BARE CHARGE. Duplex DNA keeps %.4f of its phosphates under Mg2+ and ssDNA keeps " +
                "%.4f-%.4f, because the charge spacing is 0.17 nm against 0.57-0.70. So the " +
                "single-layer exposure C-0086 published as 1.66x is %.2f-%.2fx in effective " +
                "charge, and the recommended tile's is %.4f-%.4f.").format(
                    singleFractionShort / duplexFraction, duplexFraction,
                    singleFractionShort, singleFractionLong,
                    singleLayerBudget.manningChargeRatioAtShortContour,
                    singleLayerBudget.manningChargeRatioAtLongContour,
                    recommendedBudget.manningChargeRatioAtShortContour,
                    recommendedBudget.manningChargeRatioAtLongContour
                ),
        ("THE COIL CANNOT BE IN THE GAP, AND THAT NEEDS NO PLACEMENT ASSUMPTION. Over the whole " +
                "4 x 2 ssDNA bracket and every gap in 2-10 nm the weaker of the ideal and " +
                "swollen slit penalties is %.2f k_BT at its weakest, so at most %.4f of the " +
                "remainder threads the slit — %.1f nt at the recommended tile's widest gap. " +
                "Sec 3 does not say where the coil is and this makes the answer independent of " +
                "that.").format(
                    weakestConfinement, largestPenetratingFraction,
                    slitPenetratingNucleotides(
                        10.0, SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
                        SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN, designRemainder
                    )
                ),
        ("THE BIAS RE-READ: over all %d of C-0022's states the unconditional bound moves the " +
                "held bias by at most %.4f and the penetration-limited estimate by at most " +
                "%.4f. The collar's WIDTH is 1/q0 with q0^2 >= kappa^2 + (pi/2h)^2 and contains " +
                "no surface charge, so within linear theory a uniform change of the tile's " +
                "charge cannot move it — only the level, which a force-pinned operating point " +
                "absorbs into the bias. The 2-D edge solve is therefore not re-run, and that is " +
                "an argument rather than an omission.").format(
                    states.size, worstBoundBias, worstDesignBias
                ),
        ("A REMAINDER IS AN ARTEFACT OF THE SPAN, NOT OF M13. p7560 = 60 x 126 and p8064 = " +
                "64 x 126 exactly, and the paper's own acknowledgements thank a named person " +
                "for CLONING both scaffold vectors — so the standard practice in the very " +
                "source this cross-section comes from is to build the scaffold to the design. " +
                "The Gen-1 tile's remainder exists because Sec 3's ~40 nm forces a 112 bp span " +
                "where those scaffolds were cut for 126.")
    )

    val result = ScaffoldRemainderResult(
        task = "T-195",
        leaf = "A7.4",
        title = "The unpaired scaffold remainder as a default body beside the actuated gap",
        verificationType = "literature (which scaffold, read directly) + logical (two closed-form " +
                "cheap bounds) + in-silico (a 1-D Poisson-Boltzmann re-read at all of C-0022's " +
                "states)",
        maturity = "TRL 1-3. Nothing here is measured. PASS means model-consistent and traceable.",
        units = "nm, pN, e, e/nm^2, k_BT, V; k_BT = 4.141947 pN nm at 300 K; pN/nm^2 = 1 MPa",
        conventions = listOf(
            "z runs from the electrode at z = 0 to the tile's gap-facing face at z = h",
            "the tile is negative and the electrode positive; a gap-facing charge density is the " +
                    "tile's own charge halved, which is C-0022's reading, restated",
            "a remainder is scaffold - duplexes x basePairsPerRow: one scaffold nucleotide per " +
                    "base pair of every duplex",
            "a nucleotide is one phosphate, i.e. one elementary charge before condensation",
            "MgCl2 is 2:1, so I = 3c and the first integral is the asymmetric one"
        ),
        citedInputs = listOf(
            "p7560 = 7560 nt and p8064 = 8064 nt, and the design-to-scaffold pairing - READ " +
                    "DIRECTLY, Douglas et al. caDNAno PMC2731887, in gpd/data/T-151-sources/",
            "M13mp18 = 7249 nt - CITED via C-0055",
            "the ssDNA Kuhn bracket 1.34/1.41/2.10/2.84 nm and the contour 0.57-0.70 nm/nt - " +
                    "CITED from C-0023's SsDnaTether, a 2x method systematic carried whole",
            "the Manning surviving fractions are DERIVED here from l_B and the two charge " +
                    "spacings, not cited",
            "C-0022's 21 operating states and their one-dimensional loads - read from " +
                    "gpd/results/T-3b-tile-edge-load-profile.json and REPRODUCED, not copied",
            "the Stern capacitance 20 uF/cm^2 - CITED, as in C-0008 and C-0022",
            "eps_r(water, 300 K) = 78 - CITED"
        ),
        temperature = 300.0,
        medium = "aqueous MgCl2, 0.5 / 2 / 10 mM, 300 K",
        thermalEnergy = thermalEnergy(),
        bjerrumLength = lb,
        cheapBound = mapOf(
            "1 - saturation" to ("the tile's gap-facing wall sits at %.4f of the 2:1 saturated " +
                    "far-field amplitude at 2 mM, so smearing the WHOLE remainder onto it moves " +
                    "sigma_eff by %.4f at the recommended tile - run before any field solve, and " +
                    "it settles the question").format(
                        ownSaturation.first {
                            it.wall.contains("RECOMMENDED") && it.concentration == 2.0
                        }.saturationFractionNominal,
                        recommendedWorst
                    ),
            "2 - confinement" to ("the coil is %.1f-%.1f nm ideal and %.1f-%.1f nm swollen " +
                    "against a 2-10 nm gap, so the weakest slit penalty anywhere in the bracket " +
                    "is %.2f k_BT and at most %.4f of the chain threads the gap").format(
                        coils.filter { it.remainderNucleotides == designRemainder }
                            .minOf { it.idealRadiusOfGyration },
                        coils.filter { it.remainderNucleotides == designRemainder }
                            .maxOf { it.idealRadiusOfGyration },
                        coils.filter { it.remainderNucleotides == designRemainder }
                            .minOf { it.floryEndToEnd },
                        coils.filter { it.remainderNucleotides == designRemainder }
                            .maxOf { it.floryEndToEnd },
                        weakestConfinement, largestPenetratingFraction
                    )
        ),
        scaffolds = T195_SCAFFOLDS,
        crossSections = T195_CROSS_SECTIONS,
        budgets = budgets,
        coils = coils,
        confinement = confinement,
        saturation = saturation,
        biasReReads = biasReReads,
        reproductions = reproductions,
        convergence = convergence,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "Point-ion Poisson-Boltzmann, mean field. C-0005's 123-214 % loop correction applies " +
                    "unchanged and is far larger than anything measured here; every movement " +
                    "reported is a RATIO of two solves at matched refinement, which divides that " +
                    "bracket out.",
            "The saturation bound smears a three-dimensional coil onto a plane. That is not a " +
                    "conformation - it is a ceiling no conformation can exceed, and it is the " +
                    "only sense in which the number is safe.",
            "The slit confinement laws are the ideal Edwards ground state and de Gennes' blob " +
                    "scaling, both for a NEUTRAL chain against non-adsorbing walls. Electrostatic " +
                    "self-repulsion and the grafted PEG layer the coil would also have to " +
                    "displace are POSITIVE terms omitted, so the expulsion is understated.",
            "The Manning result is a rod limit applied to a flexible chain, which condenses no " +
                    "MORE than a rod, so the ssDNA surviving fraction is a LOWER bound and the " +
                    "charge ratio quoted is the favourable-to-nobody end.",
            "The collar argument is linear theory: the transverse eigenvalue bound holds for the " +
                    "linearised operator about the solved profile. A charge large enough to " +
                    "change the profile's shape would change the eigenvalue; nothing here is.",
            "Sec 3 fixes no attachment point for the scaffold's unpaired arc, so WHERE the coil " +
                    "sits is not determined. The bounds are constructed to be independent of it."
        ),
        openQuestions = listOf(
            "Which scaffold NDI will actually buy. \"M13, circular ~7-8 K nucleotides\" admits " +
                    "7249, 7560 and 8064, and the remainder is 529, 840 or 1344 nt accordingly. " +
                    "The physics verdict is the same at all three; the number is not.",
            "Whether a cloned scaffold at the tile's own length is available. The caDNAno paper " +
                    "cloned two, so it is standard practice in the source this cross-section " +
                    "comes from - but it is a purchase decision, not a calculation.",
            "The remainder's effect on FOLDING YIELD at a four-layer honeycomb block is not " +
                    "addressed here. Rothemund's measured verdict is favourable on a " +
                    "single-layer rectangle; no measurement on a multilayer block was found.",
            "Whether the coil, expelled from the gap, sits above the tile and interferes with " +
                    "the output coupling's plan. That is a packing question on the OTHER face " +
                    "and it is not asked here."
        ),
        parameters = mapOf(
            "duplexChargeSpacing" to T195_DUPLEX_CHARGE_SPACING,
            "counterionValency" to T195_COUNTERION_VALENCY.toDouble(),
            "duplexSurvivingFraction" to duplexFraction,
            "ssDnaSurvivingFractionShortContour" to singleFractionShort,
            "ssDnaSurvivingFractionLongContour" to singleFractionLong,
            "sternCapacitance" to T195_STERN_CAPACITANCE,
            "searchNodes" to T195_SEARCH_NODES.toDouble(),
            "ratioNodes" to T195_RATIO_NODES.toDouble(),
            "selfAvoidingWalkExponent" to SELF_AVOIDING_WALK_EXPONENT,
            "operatingStates" to states.size.toDouble(),
            "gapSolves" to (states.size * 2 * 50).toDouble(),
            "tileGapFacingCharge" to tileCharge,
            "unconditionalAddedCharge" to boundCharge,
            "recommendedRemainderNucleotides" to recommendedBudget.remainderNucleotides.toDouble(),
            "recommendedWorstEffectiveMovement" to recommendedWorst,
            "singleLayerWorstEffectiveMovement" to singleLayerWorst,
            "asSolvedWorstEffectiveMovement" to asSolvedWorst,
            "exposureReduction" to exposureReduction,
            "singleLayerWorstOnM13" to singleLayerOnM13,
            "recommendedWorstOnM13" to recommendedOnM13,
            "exposureReductionOnM13" to singleLayerOnM13 / recommendedOnM13,
            "weakestConfinementFreeEnergy" to weakestConfinement,
            "largestPenetratingFraction" to largestPenetratingFraction,
            "worstBoundBiasMovement" to worstBoundBias,
            "worstDesignBiasMovement" to worstDesignBias,
            "worstBoundLoadMovement" to worstBoundLoad,
            "worstDesignLoadMovement" to worstDesignLoad,
            "worstReproductionDeparture" to worstReproduction
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-195-scaffold-remainder.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = mapOf(
                    "relativeDeparture" to 2,
                    "worstReproductionDeparture" to 2,
                    "boltzmannWeight" to 3
                ),
                // Dimensionless ratios and departures are not in the locked units, so the default
                // absolute floor - a claim about piconewtons - does not travel here (CLAUDE.md).
                floor = 0.0
            )
        ) + "\n"
    )
    println("T-195 — wrote ${output.path}")
}
