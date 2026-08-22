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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.SOLVED_HEIGHT_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

/**
 * Task `T-1e` / leaf `A2.1` — **`N` inverted on the first-moment thickness `2⟨z⟩` as well as on
 * the force-onset height**, so that the definitional part of `CH-0010`'s chain-length gap is
 * separated from the physical part *exactly* rather than by scaling.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh brush.FirstMomentConventionStudyKt
 * ```
 *
 * Emits `gpd/results/T-1e-first-moment-convention.json`, deterministically — no timestamp, and
 * every floating-point number rounded at the serialisation boundary to the precision the answer is
 * determined to (`C-0073`).
 *
 * ## The one thing to read before the numbers
 *
 * There are **two** height conventions here and neither is more correct than the other; they
 * answer different questions.
 *
 * - `L₀^F`, the **force-onset** height, is where the layer first carries a stated load. It is
 *   where the **tile sits**, and it is `T-1d`'s and `T-2`'s convention.
 * - `L₀^M = 2⟨z⟩`, the **first-moment** thickness, is a moment of the profile. It is what an
 *   Alexander box means by "its height" (`⟨z⟩ = L/2` identically), and it is what an ellipsometric
 *   or reflectivity measurement would return.
 *
 * A layer specified at `2⟨z⟩ = 10 nm` is **not** a layer specified at `L₀^F = 10 nm`; it is a
 * taller layer holding a longer chain. Quoting a chain length without the convention is the same
 * class of error as quoting a stiffness without a compression.
 */

/** Which interaction free energy a record was computed with — `T-1c`'s bracket, unchanged. */
private enum class T1eInteraction(val label: String) {
    TWO_BODY("two-body"),
    VIRIAL("virial"),
    DES_CLOIZEAUX("des-Cloizeaux")
}

/** Which family of density profiles the free energy was minimised over. */
private enum class T1eProfile(val label: String) {
    BOX("alexander-box"),
    STRONG_STRETCHING("strong-stretching"),
    SCF("scf")
}

/**
 * One (profile, interaction) pair at one design point, in **both** height conventions.
 *
 * The two chain lengths are the deliverable and everything else is what is needed to read them:
 * the shape ratio that connects the conventions, and — for the first-moment chain, which is the
 * one nobody has looked at before — where its force onset actually lands and what volume fraction
 * and coil overlap it carries.
 */
@Serializable
data class FirstMomentRecord(

    val profile: String,

    val interaction: String,

    /** The **target** height in nm, read in each convention in turn. */
    val targetHeight: Double,

    val graftingDensity: Double,

    /** `N` such that the FORCE-ONSET height is [targetHeight] — `C-0011`'s convention. */
    val forceOnsetChainLength: Double,

    /** `N` such that `2⟨z⟩` is [targetHeight] — this task's addition. */
    val firstMomentChainLength: Double,

    /** `N_M/N_F` — the whole of the definitional difference, at this point. */
    val conventionFactor: Double,

    /** `N_M − N_F` in monomers, carried so that the decomposition can be tested both ways. */
    val conventionDifference: Double,

    /** `N_F M₀` in g/mol. */
    val forceOnsetMolarMass: Double,

    /** `N_M M₀` in g/mol. */
    val firstMomentMolarMass: Double,

    /** `2⟨z⟩` in nm of the force-onset layer — `C-0011`'s `firstMomentHeight`, reproduced. */
    val forceOnsetFirstMoment: Double,

    /** `L₀^F/2⟨z⟩` of the force-onset layer — the shape ratio the scaling estimate assumes fixed. */
    val shapeRatio: Double,

    /**
     * The force-onset height in nm of the **first-moment** chain.
     *
     * The number §3 has to be read against: a layer specified at `2⟨z⟩ = 10 nm` puts its tile
     * here, not at 10 nm.
     */
    val firstMomentForceOnsetHeight: Double,

    /** `φ = N σ v₀/L₀^F` of the first-moment layer at its own resting height. */
    val firstMomentMeanVolumeFraction: Double,

    /** `Σ = π R₀² σ` of the first-moment layer — the 1-D mean field's own validity condition. */
    val firstMomentCoilOverlap: Double,

    /** `Σ = π R₀² σ` of the force-onset layer, for comparison. */
    val forceOnsetCoilOverlap: Double
)

/** The decomposition of `CH-0010`'s chain-length gap, at one interaction and one design point. */
@Serializable
data class GapDecomposition(

    val interaction: String,

    val layerHeight: Double,

    val graftingDensity: Double,

    /** `C-0003`'s published bracket, CITED — 224.8 – 374.3 monomers at the 10 nm point. */
    val standingChainLengthLow: Double,

    val standingChainLengthHigh: Double,

    /** `C-0011`'s answer, recomputed here: `N` at a force-onset `L₀ = 10 nm`. */
    val solvedForceOnsetChainLength: Double,

    /** This task's answer: `N` at a first-moment `2⟨z⟩ = 10 nm`. */
    val solvedFirstMomentChainLength: Double,

    /** The Alexander box, read on the same functional — for a box it is its own height. */
    val boxFirstMomentChainLength: Double,

    /** Strong stretching, read on the same functional. */
    val strongStretchingFirstMomentChainLength: Double,

    /** `standing/solvedForceOnset` — the gap `CH-0010` reports, as a ratio. */
    val totalGapLow: Double,

    val totalGapHigh: Double,

    /** `solvedFirstMoment/solvedForceOnset` — the definitional factor, exactly. */
    val conventionFactor: Double,

    /**
     * `trialFunctionFirstMoment/solvedFirstMoment` — what is left when both sides are read on the
     * same functional. This is the **physics**: the conformational normal stress neither trial
     * function contains.
     */
    val physicsResidueLow: Double,

    val physicsResidueHigh: Double,

    /**
     * `conventionFactor × physicsResidue / totalGap`, which is **1 by construction** where the two
     * factors are computed against the same endpoints — emitted so the identity is checkable in
     * the file rather than asserted in prose.
     */
    val multiplicativeClosureLow: Double,

    val multiplicativeClosureHigh: Double
)

/** How the two conventions move when the load that defines the resting height moves. */
@Serializable
data class ThresholdSensitivity(

    /** The load in pN over the 40 × 40 nm tile at which the layer is declared to be at rest. */
    val restingLoad: Double,

    /** `N` at a force-onset `L₀ = 10 nm` under this threshold — `C-0011`'s 43.6 / 62.1 / 108.6. */
    val forceOnsetChainLength: Double,

    /** `N` at a first-moment `2⟨z⟩ = 10 nm` under this threshold. */
    val firstMomentChainLength: Double,

    /**
     * `2⟨z⟩` in nm of the **1 pN design chain**, measured at the wall this threshold defines.
     *
     * One chain, three wall positions: the cleanest statement of how much of the threshold
     * survives into the moment, with the chain length held fixed so it cannot compensate.
     */
    val designChainFirstMoment: Double
)

/** One convergence probe — a quantity, a knob, and what moved. */
@Serializable
data class FirstMomentConvergence(

    val quantity: String,

    val knob: String,

    val knobValue: Double,

    val value: Double,

    /** Relative departure from the finest probe of the same ladder. */
    val relativeDeparture: Double,

    /** `log₂` of the ratio of successive differences, where three rungs exist. */
    val observedOrder: Double? = null
)

/** The scaling estimate `C-0011` published, and the exponents it could have been read at. */
@Serializable
data class ScalingEstimate(

    val description: String,

    /** `d ln(quantity)/d ln N`, measured between two of `T-1d`'s three layer heights. */
    val exponent: Double,

    /** `N_F · (L₀^F/2⟨z⟩)^(1/exponent)`. */
    val estimatedChainLength: Double,

    /** The exact answer, so the estimate's error is in the file rather than in the prose. */
    val exactChainLength: Double,

    val relativeError: Double
)

/** A downstream claim, the quantity it consumes, and whether its verdict moves. */
@Serializable
data class DownstreamCheck(

    val claim: String,

    val quantity: String,

    val verdictMoves: Boolean,

    val reason: String
)

@Serializable
data class FirstMomentConventionResult(
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
    val scalingEstimates: List<ScalingEstimate>,
    val decomposition: List<GapDecomposition>,
    val thresholdSensitivity: List<ThresholdSensitivity>,
    val convergence: List<FirstMomentConvergence>,
    val designPoints: List<FirstMomentRecord>,
    val decompositionStability: Map<String, Double>,
    val specificationCheck: Map<String, String>,
    val downstream: List<DownstreamCheck>,
    val falsifiers: List<String>
)

/** §3: the tile is 40 × 40 nm. */
private const val TILE_EDGE = 40.0

private const val TILE_AREA = TILE_EDGE * TILE_EDGE

/** `T-1d`'s primary resting load in pN over the tile. */
private const val PRIMARY_RESTING_LOAD = 1.0

private val RESTING_LOADS = listOf(0.1, 1.0, 10.0)

private val LAYER_HEIGHTS = listOf(5.0, 7.0, 10.0)

private const val GRAFTING_DENSITY_MIN = 0.002

private const val GRAFTING_DENSITY_MAX = 1.0

/** `T-1d`'s own grid, so that the two conventions are comparable point by point. */
private const val GRAFTING_DENSITY_SAMPLES = 61

private val PRODUCTION_GRID = ScfDiscretisation(
    nodeSpacing = 0.2,
    contourStepsPerMonomer = 2.0
)

/** `A₂` in `mol·cm³/g²` for PEG in water at 25 °C — MEASURED, via `C-0003`. */
private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

/** `A₃` in `cm⁶·mol/g³`, same convention and same source chain — MEASURED, via `C-0003`. */
private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

private const val REFERENCE_HEIGHT = 10.0

private const val REFERENCE_DENSITY = 0.024

/** `C-0003`'s published `N(10 nm, σ = 0.024)` bracket — CITED, not recomputed. */
private const val STANDING_CHAIN_LOW = 224.8

private const val STANDING_CHAIN_HIGH = 374.3

/** `C-0011`'s published scaling estimate, the interval this task's falsifier 1 is written on. */
private const val SCALING_ESTIMATE_LOW = 190.0

private const val SCALING_ESTIMATE_HIGH = 210.0

/** §3's stated layer-height band in nm. */
private const val SPECIFIED_HEIGHT_LOW = 5.0

private const val SPECIFIED_HEIGHT_HIGH = 10.0

private const val WORKER_THREADS = 4

private val progress = AtomicInteger()

fun main() {
    val peg = PegWater()
    val started = System.nanoTime()
    val densities = logarithmicSweep(
        GRAFTING_DENSITY_MIN, GRAFTING_DENSITY_MAX, GRAFTING_DENSITY_SAMPLES
    )
    // The sweep carries ONE interaction law — the des Cloizeaux limb, T-1d's primary — because
    // the three differ by 1.45x in the interaction and by 1.5 % in the answer, which the design
    // point below measures. The two analytic models ride along for nothing.
    val sweepTasks = LAYER_HEIGHTS.flatMap { height ->
        densities.flatMap { density ->
            listOf(T1eProfile.SCF, T1eProfile.BOX, T1eProfile.STRONG_STRETCHING).map { profile ->
                Triple(profile, height, density)
            }
        }
    }
    // and the design point carries all three interactions, in both conventions
    val designTasks = T1eInteraction.entries.flatMap { interaction ->
        LAYER_HEIGHTS.map { height -> interaction to height }
    }
    val total = sweepTasks.size + designTasks.size
    val pool = ForkJoinPool(WORKER_THREADS)
    val sweep: List<FirstMomentRecord>
    val design: List<FirstMomentRecord>
    try {
        sweep = pool.submit<List<FirstMomentRecord>> {
            sweepTasks.parallelStream().map { (profile, height, density) ->
                record(peg, profile, T1eInteraction.DES_CLOIZEAUX, height, density, total)
            }.toList()
        }.get(8, TimeUnit.HOURS)
        design = pool.submit<List<FirstMomentRecord>> {
            designTasks.parallelStream().map { (interaction, height) ->
                record(peg, T1eProfile.SCF, interaction, height, REFERENCE_DENSITY, total)
            }.toList()
        }.get(8, TimeUnit.HOURS)
    } finally {
        pool.shutdown()
    }
    val ordered = sweepTasks.map { (profile, height, density) ->
        sweep.first {
            it.profile == profile.label && it.targetHeight == height &&
                    it.graftingDensity == density
        }
    } + designTasks.map { (interaction, height) ->
        design.first { it.interaction == interaction.label && it.targetHeight == height }
    }
    val decomposition = decomposition(peg, design)
    val result = FirstMomentConventionResult(
        task = "T-1e",
        leaf = "A2.1",
        title = "The chain length inverted on the first-moment thickness as well as on the " +
                "force-onset height, and the exact split of CH-0010's chain-length gap",
        verificationType = "in-silico (numerical SCF, Edwards propagator — C-0011's machinery, " +
                "unedited), with the two trial-function models read on the same functional and " +
                "the strong-stretching first moment checked against its closed-form Beta ratio",
        acceptance = "the definitional part of CH-0010's chain-length gap separated from the " +
                "physical part EXACTLY, rather than by scaling: N inverted on 2<z> by a root " +
                "find on the solved profile, at the same states the force-onset inversion is " +
                "reported at, with all three profile models put in one convention and the " +
                "physical residue named with its own uncertainty",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "The interaction free energy and the chain statistics are anchored to " +
                "measurement; nothing about THIS layer is measured.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= MPa)",
            "graftingDensity" to "chains/nm^2",
            "molarMass" to "g/mol",
            "chainLength" to "monomers",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z normal to the electrode, positive away from it, origin at the electrode surface",
            "chains grafted at z = 0, one end fixed and the other free",
            "the tile is a rigid non-adsorbing wall at height h; compression means h < L0",
            "the propagator is ABSORBING at both the grafting surface and the tile",
            "FORCE-ONSET height L0^F: the height at which the layer carries " +
                    "$PRIMARY_RESTING_LOAD pN over the 40 x 40 nm tile. This is where the TILE " +
                    "SITS, and it is T-1d's and T-2's convention.",
            "FIRST-MOMENT thickness L0^M = 2<z> = 2 int z phi dz / int phi dz, taken on the " +
                    "profile at that chain's OWN force-onset resting height — which is the " +
                    "functional C-0011 already emits as firstMomentHeight. Exactly L for a box " +
                    "profile.",
            "a layer specified at 2<z> = 10 nm is NOT a layer specified at L0^F = 10 nm; it is a " +
                    "taller layer holding a longer chain, and its force onset is emitted as " +
                    "firstMomentForceOnsetHeight",
            "the layer height is the independent variable and N follows from it, by inverting " +
                    "the stated functional of EACH profile model",
            "a GRAFTED layer carries no chain translational entropy, so the van't Hoff limb of " +
                    "the measured equation of state is removed before the free energy is used"
        ),
        validity = listOf(
            "TRL 1-3. Nothing here is measured about this layer. PASS means model-consistent " +
                    "and traceable.",
            "Everything C-0011's validity range says applies unchanged: mean field at " +
                    "phi ~ 0.01 with the fluctuation corrections NOT bounded (T-1f); an " +
                    "interaction free energy not measured below phi#; monodisperse chains; " +
                    "laterally uniform grafting; a rigid tile; mechanical only.",
            "The first-moment chains are 3x longer than the force-onset ones and their layers " +
                    "1.8x taller, so they sit FURTHER from the fitted range of the equation of " +
                    "state in volume fraction and CLOSER to it in chain length. Neither " +
                    "direction is a new licence.",
            "N sigma v0 / 0.8 < h is enforced in code; every solve throws rather than " +
                    "extrapolating the equation of state into a melt.",
            "Both inversions are bracketed at a relative 1e-6 and the outer bracket contains a " +
                    "whole resting-height solve, so the determined precision is at best " +
                    "SOLVED_HEIGHT_SIGNIFICANT_DIGITS = " +
                    "$SOLVED_HEIGHT_SIGNIFICANT_DIGITS and that is what this file emits.",
            "The sweep carries the des Cloizeaux interaction only; the three-law spread is " +
                    "measured at the design point and carried into the residue's uncertainty.",
            "C-0003's 224.8-374.3 bracket is CITED from that claim, not recomputed. The box and " +
                    "strong-stretching first-moment inversions ARE recomputed here."
        ),
        parameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "medium" to "aqueous buffer, 2-10 mM MgCl2 (does not enter: C-0007 puts the " +
                    "layer's buffer dependence at <= 0.4 %)",
            "tileArea" to TILE_AREA.toString(),
            "restingLoad" to PRIMARY_RESTING_LOAD.toString(),
            "monomerVolume" to peg.monomerVolume.roundedForProse().toString(),
            "monomerMolarMass" to peg.monomerMolarMass.toString(),
            "kuhnLength" to peg.kuhnLength.toString(),
            "monomersPerKuhnSegment" to peg.monomersPerKuhnSegment.roundedForProse().toString(),
            "nodeSpacing" to PRODUCTION_GRID.nodeSpacing.toString(),
            "contourStepsPerMonomer" to PRODUCTION_GRID.contourStepsPerMonomer.toString(),
            "graftingDensityRange" to
                    listOf(GRAFTING_DENSITY_MIN, GRAFTING_DENSITY_MAX).toString(),
            "graftingDensitySamples" to GRAFTING_DENSITY_SAMPLES.toString(),
            "secondVirialCoefficient" to OSMOTIC_SECOND_VIRIAL.toString(),
            "thirdVirialCoefficient" to OSMOTIC_THIRD_VIRIAL.toString(),
            "standingChainLengthBracket" to
                    listOf(STANDING_CHAIN_LOW, STANDING_CHAIN_HIGH).toString(),
            "publishedScalingEstimate" to
                    listOf(SCALING_ESTIMATE_LOW, SCALING_ESTIMATE_HIGH).toString()
        ),
        scalingEstimates = scalingEstimates(ordered),
        decomposition = decomposition,
        thresholdSensitivity = thresholdSensitivity(peg),
        convergence = convergence(peg),
        designPoints = ordered,
        decompositionStability = decompositionStability(sweep),
        specificationCheck = specificationCheck(sweep),
        downstream = downstream(design, decomposition),
        falsifiers = falsifiers(sweep, design, decomposition)
    )
    val json = Json { prettyPrint = true; encodeDefaults = true }
    File("gpd/results/T-1e-first-moment-convention.json").writeText(
        json.encodeToString(
            json.parseToJsonElement(json.encodeToString(result))
                .roundedForResult(digits = SOLVED_HEIGHT_SIGNIFICANT_DIGITS).withEmissionHeader(LatticeTag.NONE, null)
        ) + "\n"
    )
    println(
        "written: gpd/results/T-1e-first-moment-convention.json " +
                "(${ordered.size} records) in " +
                "${(System.nanoTime() - started) / 1_000_000_000L} s"
    )
}

private fun interactionFor(peg: PegWater, choice: T1eInteraction): InteractionFreeEnergy =
    when (choice) {
        T1eInteraction.TWO_BODY -> twoBodyInteraction(
            peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL), peg.monomerVolume
        )
        T1eInteraction.DES_CLOIZEAUX -> desCloizeauxInteraction(
            peg.crossoverIndex, peg.monomerVolume
        )
        T1eInteraction.VIRIAL -> additiveInteraction(
            "virial",
            listOf(
                twoBodyInteraction(
                    peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL), peg.monomerVolume
                ),
                threeBodyInteraction(
                    peg.reducedThirdVirialCoefficient(OSMOTIC_THIRD_VIRIAL), peg.monomerVolume
                )
            )
        )
    }

private fun layerModel(
    profile: T1eProfile,
    interaction: InteractionFreeEnergy,
    restingLoad: Double = PRIMARY_RESTING_LOAD
): GraftedLayerModel = when (profile) {
    T1eProfile.BOX -> AlexanderBoxLayer(interaction)
    T1eProfile.STRONG_STRETCHING -> StrongStretchingLayer(interaction)
    T1eProfile.SCF -> SelfConsistentFieldLayer(
        interaction, PRODUCTION_GRID, restingLoad / TILE_AREA
    )
}

private fun record(
    peg: PegWater,
    profile: T1eProfile,
    choice: T1eInteraction,
    targetHeight: Double,
    graftingDensity: Double,
    total: Int
): FirstMomentRecord {
    val model = layerModel(profile, interactionFor(peg, choice))
    val onForce = model.chainLengthAtHeight(peg, targetHeight, graftingDensity)
    val forceChain = peg.graftedChain(onForce, graftingDensity)
    val forceMoment = model.restingFirstMomentThickness(forceChain)
    val onMoment = model.chainLengthForFirstMomentThickness(
        peg, targetHeight, graftingDensity, seed = onForce
    )
    val momentChain = peg.graftedChain(onMoment, graftingDensity)
    val momentOnset = model.equilibriumHeight(momentChain)
    val done = progress.incrementAndGet()
    println("[$done/$total] ${profile.label} ${choice.label} h=$targetHeight sigma=$graftingDensity")
    return FirstMomentRecord(
        profile = profile.label,
        interaction = choice.label,
        targetHeight = targetHeight,
        graftingDensity = graftingDensity,
        forceOnsetChainLength = onForce,
        firstMomentChainLength = onMoment,
        conventionFactor = onMoment / onForce,
        conventionDifference = onMoment - onForce,
        forceOnsetMolarMass = onForce * peg.monomerMolarMass,
        firstMomentMolarMass = onMoment * peg.monomerMolarMass,
        forceOnsetFirstMoment = forceMoment,
        shapeRatio = targetHeight / forceMoment,
        firstMomentForceOnsetHeight = momentOnset,
        firstMomentMeanVolumeFraction = momentChain.meanVolumeFraction(momentOnset),
        firstMomentCoilOverlap = coilOverlap(momentChain),
        forceOnsetCoilOverlap = coilOverlap(forceChain)
    )
}

/**
 * A number destined for a **prose** field, rounded to the precision this file emits.
 *
 * `C-0073`: *"a number emitted as a STRING is not rounded"* — `roundedForResult` dispatches on the
 * JSON type and passes strings through, correctly, so an interpolated `Double` carries
 * `Double.toString()`'s full round-trip precision into a file that declares six significant
 * digits. Every findings string below goes through here.
 */
private fun six(value: Double): String =
    roundForResult(value, SOLVED_HEIGHT_SIGNIFICANT_DIGITS).toString()

private fun coilOverlap(chain: GraftedChain): Double =
    PI * chain.idealEndToEnd * chain.idealEndToEnd * chain.graftingDensity

/**
 * The scaling estimate `C-0011` published, evaluated at every exponent it could have been read at.
 *
 * The point of emitting this is that the estimate is worth what its exponent is worth, and the
 * exponent of `L₀^F` in `N` is **not** the exponent of `2⟨z⟩` in `N` — they differ by 10 % here,
 * and `C-0011`'s formula uses the first where it needs the second.
 */
private fun scalingEstimates(records: List<FirstMomentRecord>): List<ScalingEstimate> {
    // One record per layer height, and it must be ONE: the sweep's nearest grid point to
    // sigma = 0.024 is 0.0240225 and the design block adds a record at 0.024 exactly, so a filter
    // on "near enough to the reference density" returns two records at the same height — and a
    // pair drawn from those two has `ln(h_high/h_low) = 0` in its denominator, i.e. an exponent of
    // zero and an estimate of Infinity. Keyed on the height instead, the pairs are the three real
    // ones.
    val scf = records.filter {
        it.profile == T1eProfile.SCF.label &&
                it.interaction == T1eInteraction.DES_CLOIZEAUX.label &&
                it.graftingDensity == REFERENCE_DENSITY
    }.sortedBy { it.targetHeight }.distinctBy { it.targetHeight }
    if (scf.size < 2) return emptyList()
    val reference = scf.last()
    val estimates = mutableListOf<ScalingEstimate>()
    for (i in scf.indices) for (j in i + 1 until scf.size) {
        val low = scf[i]
        val high = scf[j]
        val momentExponent = ln(high.forceOnsetFirstMoment / low.forceOnsetFirstMoment) /
                ln(high.forceOnsetChainLength / low.forceOnsetChainLength)
        val onsetExponent = ln(high.targetHeight / low.targetHeight) /
                ln(high.forceOnsetChainLength / low.forceOnsetChainLength)
        estimates += estimate(
            "d ln 2<z> / d ln N measured between L0^F = ${low.targetHeight.roundedForProse()} and " +
                    "${high.targetHeight.roundedForProse()} nm — the RIGHT exponent for this extrapolation",
            momentExponent, reference
        )
        estimates += estimate(
            "d ln L0^F / d ln N measured between L0^F = ${low.targetHeight.roundedForProse()} and " +
                    "${high.targetHeight.roundedForProse()} nm — the exponent C-0011's formula actually uses",
            onsetExponent, reference
        )
    }
    listOf(0.49, 0.5, 0.55, 0.64).forEach { exponent ->
        estimates += estimate(
            "asserted exponent ${exponent.roundedForProse()}, from C-0011's quoted N^(0.5-0.55) and its own " +
                    "stated 0.49-0.64 band",
            exponent, reference
        )
    }
    return estimates
}

private fun estimate(
    description: String,
    exponent: Double,
    reference: FirstMomentRecord
): ScalingEstimate {
    // A guard rather than a hope: `shapeRatio^(1/exponent)` is Infinity at exponent zero, and
    // kotlinx.serialization refuses Infinity at the serialisation call, hundreds of lines from
    // where the value was made (CLAUDE.md). An exponent that is not a positive finite number is
    // not an estimate, and it is emitted as one that failed rather than as a number.
    require(exponent.isFinite() && exponent > 0.0) {
        "a scaling exponent must be positive and finite, was: ${exponent.roundedForProse()} ($description)"
    }
    val estimated = reference.forceOnsetChainLength * reference.shapeRatio.pow(1.0 / exponent)
    return ScalingEstimate(
        description = description,
        exponent = exponent,
        estimatedChainLength = estimated,
        exactChainLength = reference.firstMomentChainLength,
        relativeError = estimated / reference.firstMomentChainLength - 1.0
    )
}

/**
 * The decomposition, at the 10 nm design point, one row per interaction law.
 *
 * The box and strong-stretching first-moment inversions are recomputed here rather than read from
 * `C-0003`, because that is the whole point: the trial functions have to be read on the **same**
 * functional as the solved layer or the comparison is still between two definitions.
 */
private fun decomposition(
    peg: PegWater,
    design: List<FirstMomentRecord>
): List<GapDecomposition> = T1eInteraction.entries.map { choice ->
    val interaction = interactionFor(peg, choice)
    val solved = design.first {
        it.interaction == choice.label && it.targetHeight == REFERENCE_HEIGHT
    }
    val box = AlexanderBoxLayer(interaction).chainLengthForFirstMomentThickness(
        peg, REFERENCE_HEIGHT, REFERENCE_DENSITY
    )
    val sst = StrongStretchingLayer(interaction).chainLengthForFirstMomentThickness(
        peg, REFERENCE_HEIGHT, REFERENCE_DENSITY
    )
    val trialLow = minOf(box, sst)
    val trialHigh = maxOf(box, sst)
    val totalLow = STANDING_CHAIN_LOW / solved.forceOnsetChainLength
    val totalHigh = STANDING_CHAIN_HIGH / solved.forceOnsetChainLength
    val residueLow = trialLow / solved.firstMomentChainLength
    val residueHigh = trialHigh / solved.firstMomentChainLength
    GapDecomposition(
        interaction = choice.label,
        layerHeight = REFERENCE_HEIGHT,
        graftingDensity = REFERENCE_DENSITY,
        standingChainLengthLow = STANDING_CHAIN_LOW,
        standingChainLengthHigh = STANDING_CHAIN_HIGH,
        solvedForceOnsetChainLength = solved.forceOnsetChainLength,
        solvedFirstMomentChainLength = solved.firstMomentChainLength,
        boxFirstMomentChainLength = box,
        strongStretchingFirstMomentChainLength = sst,
        totalGapLow = totalLow,
        totalGapHigh = totalHigh,
        conventionFactor = solved.conventionFactor,
        physicsResidueLow = residueLow,
        physicsResidueHigh = residueHigh,
        // the closure is against the RECOMPUTED trial-function endpoints rather than against
        // C-0003's cited bracket, so it is an identity and not an accident
        multiplicativeClosureLow = solved.conventionFactor * residueLow /
                (trialLow / solved.forceOnsetChainLength),
        multiplicativeClosureHigh = solved.conventionFactor * residueHigh /
                (trialHigh / solved.forceOnsetChainLength)
    )
}

/**
 * How each convention responds to a hundred-fold change in the load that defines the resting
 * height, at the 10 nm design point.
 *
 * `C-0011` measures the force-onset convention as a **2.5× family** in `N` over this range. The
 * question deliverable 5 turns on is whether the first-moment convention is one too.
 */
private fun thresholdSensitivity(peg: PegWater): List<ThresholdSensitivity> {
    val interaction = interactionFor(peg, T1eInteraction.DES_CLOIZEAUX)
    val primary = SelfConsistentFieldLayer(
        interaction, PRODUCTION_GRID, PRIMARY_RESTING_LOAD / TILE_AREA
    )
    val designChain = peg.graftedChain(
        primary.chainLengthAtRestingHeight(peg, REFERENCE_HEIGHT, REFERENCE_DENSITY),
        REFERENCE_DENSITY
    )
    return RESTING_LOADS.map { load ->
        val model = SelfConsistentFieldLayer(interaction, PRODUCTION_GRID, load / TILE_AREA)
        val onForce = model.chainLengthAtRestingHeight(
            peg, REFERENCE_HEIGHT, REFERENCE_DENSITY
        )
        ThresholdSensitivity(
            restingLoad = load,
            forceOnsetChainLength = onForce,
            firstMomentChainLength = model.chainLengthForFirstMomentThickness(
                peg, REFERENCE_HEIGHT, REFERENCE_DENSITY, seed = onForce
            ),
            designChainFirstMoment = model.restingFirstMomentThickness(designChain)
        )
    }
}

/**
 * Gate 4, emitted rather than only asserted: the order of the first moment and of the inverted
 * chain length in the node spacing.
 *
 * `CLAUDE.md`: *"convergence is a property of the quantity"*. A first moment is a ratio of two
 * quadratures over a node count that steps with the solved wall height, and it earns its own order
 * rather than inheriting the pressure's.
 */
private fun convergence(peg: PegWater): List<FirstMomentConvergence> {
    val interaction = interactionFor(peg, T1eInteraction.DES_CLOIZEAUX)
    val spacings = listOf(0.4, 0.2, 0.1)
    val chain = peg.graftedChain(80.0, REFERENCE_DENSITY)
    val moments = spacings.map { spacing ->
        SelfConsistentFieldLayer(
            interaction,
            ScfDiscretisation(nodeSpacing = spacing, contourStepsPerMonomer = 2.0),
            PRIMARY_RESTING_LOAD / TILE_AREA
        ).firstMomentThickness(chain, 9.0)
    }
    val lengths = spacings.map { spacing ->
        SelfConsistentFieldLayer(
            interaction,
            ScfDiscretisation(nodeSpacing = spacing, contourStepsPerMonomer = 2.0),
            PRIMARY_RESTING_LOAD / TILE_AREA
        ).chainLengthForFirstMomentThickness(peg, 4.0, 0.05)
    }
    return ladder("firstMomentThickness", "nodeSpacing", spacings, moments) +
            ladder("firstMomentChainLength", "nodeSpacing", spacings, lengths)
}

private fun ladder(
    quantity: String,
    knob: String,
    values: List<Double>,
    results: List<Double>
): List<FirstMomentConvergence> {
    val reference = results.last()
    val order = ln(
        abs(results[1] - results[0]) / abs(results[2] - results[1])
    ) / ln(values[0] / values[1])
    return results.indices.map { i ->
        FirstMomentConvergence(
            quantity = quantity,
            knob = knob,
            knobValue = values[i],
            value = results[i],
            relativeDeparture = results[i] / reference - 1.0,
            observedOrder = if (i == 0) order else null
        )
    }
}

/**
 * Whether the gap decomposes as a **product** or as a **sum**, decided by which of the two is the
 * stabler across the grid rather than by preference.
 *
 * The ratio and the difference are both exact at any one point; what a *decomposition* claims is
 * that one of them can be carried to another point. The relative spread of each across the 10 nm
 * grid is the measurement that settles it.
 */
private fun decompositionStability(records: List<FirstMomentRecord>): Map<String, Double> {
    val scf = records.filter {
        it.profile == T1eProfile.SCF.label &&
                it.interaction == T1eInteraction.DES_CLOIZEAUX.label &&
                it.targetHeight == REFERENCE_HEIGHT
    }.distinctBy { it.graftingDensity }
    val factors = scf.map { it.conventionFactor }
    val differences = scf.map { it.conventionDifference }
    val ratios = scf.map { it.shapeRatio }
    return mapOf(
        "conventionFactorMin" to factors.min(),
        "conventionFactorMax" to factors.max(),
        "conventionFactorSpread" to factors.max() / factors.min(),
        "conventionDifferenceMin" to differences.min(),
        "conventionDifferenceMax" to differences.max(),
        "conventionDifferenceSpread" to differences.max() / differences.min(),
        "shapeRatioMin" to ratios.min(),
        "shapeRatioMax" to ratios.max()
    )
}

/**
 * Where a first-moment-specified layer actually puts the tile, against §3's stated 5–10 nm band.
 *
 * This is what makes the convention question a **specification** question rather than a
 * bookkeeping one: the two conventions do not describe the same device.
 */
private fun specificationCheck(records: List<FirstMomentRecord>): Map<String, String> {
    val scf = records.filter {
        it.profile == T1eProfile.SCF.label &&
                it.interaction == T1eInteraction.DES_CLOIZEAUX.label &&
                it.targetHeight == REFERENCE_HEIGHT
    }.distinctBy { it.graftingDensity }
    val onsets = scf.map { it.firstMomentForceOnsetHeight }
    val insideBand = scf.count {
        it.firstMomentForceOnsetHeight in SPECIFIED_HEIGHT_LOW..SPECIFIED_HEIGHT_HIGH
    }
    return mapOf(
        "specifiedHeightBand" to "$SPECIFIED_HEIGHT_LOW-$SPECIFIED_HEIGHT_HIGH nm (section 3)",
        "forceOnsetHeightOfAFirstMomentTenNanometreLayer" to
                "${six(onsets.min())}-${six(onsets.max())} nm across the " +
                        "${scf.size}-point grafting-density grid",
        "gridPointsWhoseTileSitsInsideSection3sBand" to "$insideBand of ${scf.size}",
        "verdict" to if (insideBand == 0) {
            "A layer specified at 2<z> = 10 nm puts its tile OUTSIDE section 3's stated " +
                    "5-10 nm band at every grafting density on the grid. The first-moment " +
                    "convention cannot be used to specify a Gen-1 layer without changing " +
                    "section 3, which is a specification question and not a modelling one."
        } else {
            "Some grid points of a 2<z> = 10 nm layer do sit inside section 3's band; the " +
                    "convention question is then not decided by the specification alone."
        }
    )
}

private fun downstream(
    records: List<FirstMomentRecord>,
    decomposition: List<GapDecomposition>
): List<DownstreamCheck> {
    val scf = records.first {
        it.profile == T1eProfile.SCF.label &&
                it.interaction == T1eInteraction.DES_CLOIZEAUX.label &&
                it.targetHeight == REFERENCE_HEIGHT &&
                it.graftingDensity == REFERENCE_DENSITY
    }
    val primary = decomposition.first { it.interaction == T1eInteraction.DES_CLOIZEAUX.label }
    return listOf(
        DownstreamCheck(
            claim = "C-0011",
            quantity = "N(10 nm) = 62.1, the force-onset inversion",
            verdictMoves = false,
            reason = "Reproduced here at ${six(scf.forceOnsetChainLength)} monomers in its own " +
                    "convention. Nothing about it is corrected; a second convention is added " +
                    "beside it."
        ),
        DownstreamCheck(
            claim = "C-0011 / CH-0010",
            quantity = "the scaling estimate N ~ 190-210 for a 10 nm first-moment layer",
            verdictMoves = false,
            reason = "The exact inversion gives ${six(primary.solvedFirstMomentChainLength)} " +
                    "monomers. The ESTIMATE stands; what does not stand is the exponent it was " +
                    "read at — see the scalingEstimates block and CH-0090."
        ),
        DownstreamCheck(
            claim = "C-0016 / C-0027 / C-0051",
            quantity = "the design window sigma in [0.0116, 0.2601] at 10 nm, whose lower edge " +
                    "is coil overlap Sigma >= 1",
            verdictMoves = false,
            reason = "The window is quoted in the FORCE-ONSET convention and the tile sits " +
                    "where that convention says. Re-specifying the layer at 2<z> = 10 nm is a " +
                    "different device, not a correction to this one, and section 3 does not " +
                    "admit it (see specificationCheck). No edge moves."
        ),
        DownstreamCheck(
            claim = "C-0016",
            quantity = "\"a bench would order 8-9 kDa PEG in the first-moment convention\"",
            verdictMoves = true,
            reason = "The exact inversion gives ${six(primary.solvedFirstMomentChainLength)} " +
                    "monomers, ${six(scf.firstMomentMolarMass / 1000.0)} kDa — BELOW the " +
                    "banner's 8-9 kDa band, which was read off the scaling estimate rather " +
                    "than computed. The banner's POINT is untouched and its arithmetic is not: " +
                    "the factor between the conventions is " +
                    "${six(primary.conventionFactor)}x, not four. And the deeper correction is " +
                    "that the first-moment layer is not a 10 nm layer in any sense the device " +
                    "cares about (see specificationCheck). Raised as CH-0091."
        ),
        DownstreamCheck(
            claim = "C-0002 / C-0036",
            quantity = "phi/phi# and the concentrated crossover, both proportional to N",
            verdictMoves = false,
            reason = "Both are evaluated at the layer the tile occupies. The first-moment " +
                    "layer's own mean phi is emitted (firstMomentMeanVolumeFraction) so the " +
                    "comparison can be made, but no standing verdict is read at that state."
        ),
        DownstreamCheck(
            claim = "C-0003",
            quantity = "the N = 224.8-374.3 bracket and its 28 % box-to-SST spread",
            verdictMoves = true,
            reason = "Read on the first-moment functional the box and strong stretching agree " +
                    "with each other to " +
                    "${
                        six(
                            abs(
                                primary.boxFirstMomentChainLength /
                                        primary.strongStretchingFirstMomentChainLength - 1.0
                            ) * 100.0
                        )
                    } %, not 28 %. Most of C-0003's own internal spread is a convention " +
                    "difference between its two models, not profile uncertainty — which " +
                    "sharpens CH-0010's \"they agree because they share a defect\" rather than " +
                    "contradicting it. Raised as CH-0090."
        )
    )
}

private fun falsifiers(
    sweep: List<FirstMomentRecord>,
    design: List<FirstMomentRecord>,
    decomposition: List<GapDecomposition>
): List<String> {
    val primary = decomposition.first { it.interaction == T1eInteraction.DES_CLOIZEAUX.label }
    val exact = primary.solvedFirstMomentChainLength
    val stability = decompositionStability(sweep)
    val reference = design.first {
        it.interaction == T1eInteraction.DES_CLOIZEAUX.label &&
                it.targetHeight == REFERENCE_HEIGHT
    }
    return listOf(
        "1 — the exact inversion landing inside C-0011's quoted 190-210: " +
                if (exact in SCALING_ESTIMATE_LOW..SCALING_ESTIMATE_HIGH) {
                    "FIRED. The exact answer is ${six(exact)} monomers, inside the published " +
                            "interval, so the scaling was sufficient AT THE DESIGN POINT. What " +
                            "it was not sufficient for is stated in the scalingEstimates and " +
                            "decompositionStability blocks."
                } else {
                    "did NOT fire. The exact answer is ${six(exact)} monomers, OUTSIDE " +
                            "190-210, and every entry of scalingEstimates read at a " +
                            "measured exponent overstates it."
                },
        "2 — the decomposition not being a decomposition: see decompositionStability; the " +
                "convention factor spreads " +
                "${six(stability.getValue("conventionFactorSpread"))}x across the 10 nm grid " +
                "and the difference spreads " +
                "${six(stability.getValue("conventionDifferenceSpread"))}x, so the PRODUCT " +
                "form is the transferable one by a factor of " +
                "${
                    six(
                        stability.getValue("conventionDifferenceSpread") /
                                stability.getValue("conventionFactorSpread")
                    )
                }.",
        "3 — 2<z> being as threshold-dependent as the force-onset height: see " +
                "thresholdSensitivity, and the ratio of the two families is in the claim.",
        "4 — the physical residue coming out at or below one: it is " +
                "${six(primary.physicsResidueLow)}-${six(primary.physicsResidueHigh)} at the " +
                "des Cloizeaux interaction, so it did not fire.",
        "5 — the accessor failing to reproduce T-1d's emitted firstMomentHeight: asserted as a " +
                "test at departure 0.0 against ScfProfile.firstMomentHeight and within the " +
                "emission slack against the committed T-1d file. Its shape ratio here is " +
                "${six(reference.shapeRatio)}."
    )
}
