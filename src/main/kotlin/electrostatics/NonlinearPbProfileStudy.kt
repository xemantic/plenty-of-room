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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.exp

/**
 * Task `T-3a` — the 1-D nonlinear Poisson-Boltzmann profile of the Gen-1 stack in the actual
 * 2:1 buffer, **tile and electrode solved as one system**. Leaf `A7.4`.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=electrostatics.NonlinearPbProfileStudyKt
 * ```
 *
 * Emits `gpd/results/T-3a-nonlinear-pb-profile.json`, deterministically — no timestamp, and
 * every floating-point number rounded at the serialisation boundary per `ResultRounding`.
 */

/** The saturation constants and the effective charge, per buffer — what replaces `C-0005`'s ceiling. */
@Serializable
data class EffectiveChargePoint(
    val concentration: Double,
    val ionicStrength: Double,
    val bulkDebyeLength: Double,
    val symmetricCeilingDivalent: Double,
    val asymmetricSaturatedNegativeSurface: Double,
    val asymmetricSaturatedPositiveSurface: Double,
    val asymmetricOverSymmetric: Double,
    val positiveOverNegative: Double,
    val tileChargeModel: String,
    val tileBareChargeDensity: Double,
    val tileReducedSurfacePotential: Double,
    val tileEffectiveChargeDensity: Double,
    val tileEffectiveOverBare: Double,
    val tileFractionOfSaturation: Double
)

/** The electrode, with the compact layer in series — the `CH-0007` table. */
@Serializable
data class ElectrodePoint(
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val diffuseLayerPotential: Double,
    val compactLayerDrop: Double,
    val compactLayerFraction: Double,
    val electrodeChargeDensity: Double,
    val electrodeEffectiveChargeDensity: Double,
    val pointIonBoundary: Double,
    val diffuseOverPointIonBoundary: Double,
    val pointIonValid: Boolean
)

/** One `(medium, buffer, gap, bias)` state point of the force sweep. */
@Serializable
data class ForcePoint(
    val medium: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val diffuseLayerPotential: Double,
    val electrodeChargeDensity: Double,
    val tileReducedSurfacePotential: Double,
    val disjoiningPressure: Double,
    val forceOnTile: Double,
    val electrostaticStiffness: Double,
    val forceDecayLength: Double,
    val bulkDebyeLength: Double,
    val decayOverBulkDebye: Double,
    val stiffnessOverSection1Estimate: Double,
    val linearBareBoundaryForce: Double,
    val linearSaturatedAmplitudeForce: Double,
    val nonlinearOverLinearBare: Double,
    val nonlinearOverLinearSaturated: Double,
    val forceWithoutCompactLayer: Double,
    val meetsHundredPiconewton: Boolean,
    val pointIonValid: Boolean,
    val firstIntegralRelativeSpread: Double,
    val firstIntegralCoreSpread: Double,
    val numericallyResolved: Boolean
)

/** The bias needed to reach the §3 100 pN target, per gap and buffer. */
@Serializable
data class ThresholdPoint(
    val medium: String,
    val concentration: Double,
    val gapHeight: Double,
    val biasForHundredPiconewton: Double?,
    val diffuseLayerPotentialThere: Double?,
    val withinPointIonValidity: Boolean,
    val forceAtTwoVolts: Double,
    val forceAtPointIonBoundary: Double
)

/** The size-modified (Bikerman) bracket — the `T-6b` step, folded in. */
@Serializable
data class BikermanPoint(
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val ionRadius: Double,
    val maximumIonDensity: Double,
    val pointIonForce: Double,
    val sizeModifiedForce: Double,
    val ratio: Double
)

/** The compressed PEG layer as a medium — `C-0005` §4(c) numbers, carried into the solve. */
@Serializable
data class LayerForcePoint(
    val restingHeight: Double,
    val restingVolumeFraction: Double,
    val concentration: Double,
    val gapHeight: Double,
    val polymerVolumeFraction: Double,
    val saltPartitionCoefficient: Double,
    val effectivePermittivity: Double,
    val localDebyeLength: Double,
    val appliedBias: Double,
    val forceWithLayer: Double,
    val forceWithoutLayer: Double,
    val layerAmplification: Double,
    val decayLengthWithLayer: Double
)

/** Mesh convergence of the whole pipeline — gate 4, emitted rather than asserted only. */
@Serializable
data class ConvergencePoint(
    val gapHeight: Double,
    val nodes: Int,
    val force: Double,
    val relativeDeparture: Double,
    val firstIntegralRelativeSpread: Double,
    val firstIntegralCoreSpread: Double
)

/** What `T-4` gets, against `C-0001`'s (provisional) brush stiffness. */
@Serializable
data class PullInPoint(
    val gapHeight: Double,
    val concentration: Double,
    val brushStiffness: Double,
    val brushSecantStiffness: Double,
    val biasWhereStiffnessCancels: Double?,
    val diffusePotentialThere: Double?,
    val electrostaticStiffnessAtTwoVolts: Double,
    val ratioAtTwoVolts: Double
)

@Serializable
data class NonlinearPbResult(
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
    val thermalVoltage: Double,
    val bjerrumLength: Double,
    val tile: DnaOrigamiTile,
    val tileChargeModels: Map<String, Double>,
    val effectiveCharge: List<EffectiveChargePoint>,
    val electrode: List<ElectrodePoint>,
    val forces: List<ForcePoint>,
    val thresholds: List<ThresholdPoint>,
    val bikerman: List<BikermanPoint>,
    val layer: List<LayerForcePoint>,
    val convergence: List<ConvergencePoint>,
    val pullIn: List<PullInPoint>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private val BUFFERS = listOf(2.0, 5.0, 10.0)
private val GAPS = listOf(3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0)
private val BIASES = listOf(0.0, 0.1, 0.25, 0.5, 1.0, 2.0)
private const val FOOTPRINT = 1600.0
private const val STERN_CAPACITANCE = 20.0
private const val SEARCH_NODES = 800
private const val STIFFNESS_STEP = 0.02

/**
 * The mesh grows with the gap.
 *
 * Not cosmetic: the disjoining pressure at a 30 nm gap is four orders of magnitude below the
 * osmotic and Maxwell terms it is the difference of, so the relative error on the pressure is
 * that factor times the relative error on the profile. A mesh fixed at 4000 nodes leaves 4% at
 * 30 nm — visible in the `convergence` table, which is why the table is emitted.
 */
private fun nodesFor(gapHeight: Double): Int =
    maxOf(DEFAULT_GAP_MESH_NODES, (gapHeight * 1200.0).toInt())

/** `C-0002`'s unperturbed volume fraction at each §3 layer height — **CITED from `C-0002`**. */
private val LAYER_HEIGHTS = listOf(5.0 to 0.0708, 7.0 to 0.0439, 10.0 to 0.0288872)

/** `C-0001`'s stiffness at the unperturbed height and its secant — **CITED, and a lower bound**. */
private val BRUSH_STIFFNESS = mapOf(5.0 to (111.6 to 137.0), 7.0 to (26.9 to 45.3), 10.0 to (7.4 to 20.2))

private class Solver(
    val buffer: MagnesiumChlorideBuffer,
    val bjerrumLength: Double,
    val tileCharge: Double,
    val mediumFor: (Double) -> GapMediumProfile,
    val ionsFor: (Double) -> IonModel
) {

    val stern: Double = sternChargeDensityPerVolt(STERN_CAPACITANCE)

    fun diffusePotential(gapHeight: Double, appliedBias: Double): Double =
        diffusePotentialOfAppliedBias(
            gapHeight, appliedBias, tileCharge, stern,
            ionsFor(gapHeight), mediumFor(gapHeight), bjerrumLength, nodes = SEARCH_NODES
        )

    fun solve(gapHeight: Double, diffuse: Double): GapSolution =
        PoissonBoltzmannGap(
            gapHeight, ionsFor(gapHeight), mediumFor(gapHeight), bjerrumLength,
            nodes = nodesFor(gapHeight)
        ).solve(diffuse / thermalVoltage(), tileCharge)

    /** The force in pN at a fixed **applied** bias, re-solving the Stern series at each gap. */
    fun force(gapHeight: Double, appliedBias: Double): Double =
        solve(gapHeight, diffusePotential(gapHeight, appliedBias)).forceOnTile(FOOTPRINT)

}

fun main() {
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val peg = PegWater()
    val fibreRadius = peg.kuhnSegmentDiameter / 2.0
    val surviving = tile.manningSurvivingFraction(2, lb)
    val chargeModels = mapOf(
        "bottom helix row only, Manning-renormalised" to
                singleHelixLayerChargeDensity(tile) * surviving,
        "half the tile facing the gap, Manning-renormalised (NOMINAL)" to
                tile.projectedChargeDensity * surviving / 2.0,
        "whole tile, Manning-renormalised" to tile.projectedChargeDensity * surviving,
        "whole tile, BARE — what C-0005 forbids" to tile.projectedChargeDensity
    )
    val nominal = -chargeModels.getValue("half the tile facing the gap, Manning-renormalised (NOMINAL)")

    val result = NonlinearPbResult(
        task = "T-3a",
        leaf = "A7.4",
        title = "1-D nonlinear Poisson-Boltzmann profile of the Gen-1 stack in the actual 2:1 " +
                "MgCl2 buffer, tile and electrode solved as ONE system",
        verificationType = "in-silico (closed-form 2:1 Gouy-Chapman + graded finite-volume " +
                "Newton solve of the nonlinear two-point boundary-value problem) + logical",
        acceptance = "An effective charge density from an ASYMMETRIC-electrolyte solve replacing " +
                "C-0005's symmetric z:z ceiling; F_es(h, V) over the §3 ranges from the osmotic + " +
                "Maxwell first integral rather than a superposition formula; k_es differentiated " +
                "from the solve with the error in §1's |k_es| = F_es/lambda_D quantified; and the " +
                "force's own decay length against all three Debye lengths CH-0004 distinguishes.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "And inside mean field: C-0005 establishes that the one-loop correction is " +
                "123-214% of the leading term across this whole gap range, so every force here " +
                "is a MEAN-FIELD number whose error is not bounded by its own expansion.",
        units = mapOf(
            "length" to "nm",
            "chargeDensity" to "e/nm^2",
            "numberDensity" to "1/nm^3",
            "concentration" to "mM",
            "potential" to "V",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "energy" to "pN*nm",
            "temperature" to "K",
            "capacitance" to "uF/cm^2"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; the electrode surface is z = 0",
            "the tile sits at z = h; 'gap' always means the tile-electrode separation h",
            "the tile carries NET NEGATIVE charge; a POSITIVE electrode bias pulls it toward -z, " +
                    "so F_es,z < 0 and k_es = -dF_es,z/dz < 0 — §1 of the problem definition, " +
                    "restated here and enforced as a test",
            "y = e psi / k_BT is the VALENCY-FREE reduced potential; valencies live in the " +
                    "Boltzmann factors, exp(-2y) for Mg2+ and exp(+y) for Cl-, never in y",
            "MgCl2 is 2:1: I = 3c, kappa^2 = 24 pi l_B c, and the first integral is " +
                    "(y'/kappa)^2 = (e^-2y + 2e^y - 3)/3, NOT the symmetric sinh form",
            "the ELECTRODE is a constant-potential (Dirichlet) boundary in series with a compact " +
                    "Stern layer; the TILE is a constant-charge (Neumann) boundary. This is the " +
                    "MIXED problem and it equals neither canonical case",
            "sigma is signed here — the tile's is negative — unlike T-6, where sigma_s was a " +
                    "magnitude. The sign is load-bearing in a two-dissimilar-surface problem",
            "the decay length of the force is reported as ell = -1/(d ln|F_es|/dh) = F_es/k_es"
        ),
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE,
            "waterRelativePermittivity" to WATER_RELATIVE_PERMITTIVITY,
            "pegRelativePermittivity" to PEG_RELATIVE_PERMITTIVITY,
            "tileEdge" to tile.edge,
            "tileThickness" to tile.thickness,
            "tileFootprintArea" to FOOTPRINT,
            "manningSurvivingFraction" to surviving,
            "nominalTileChargeDensity" to nominal,
            "sternCapacitance" to STERN_CAPACITANCE,
            "sternChargeDensityPerVolt" to sternChargeDensityPerVolt(STERN_CAPACITANCE),
            "meshNodes" to DEFAULT_GAP_MESH_NODES.toDouble(),
            "meshGrading" to DEFAULT_GAP_MESH_GRADING,
            "biasSearchNodes" to SEARCH_NODES.toDouble(),
            "stiffnessDifferencingStep" to STIFFNESS_STEP,
            "hydratedMagnesiumRadius" to HYDRATED_MAGNESIUM_RADIUS,
            "hydratedChlorideRadius" to HYDRATED_CHLORIDE_RADIUS,
            "pegFibreRadius" to fibreRadius
        ),
        citedInputs = listOf(
            "eps_r(water, 300 K) = 78 — CITED, as in C-0005. l_B goes as 1/eps and every force " +
                    "here goes roughly as l_B, so the 3% literature spread is ~3% on F_es.",
            "the Manning-renormalised tile charge, 11.90% of bare (1276 e) — CITED FROM C-0005, " +
                    "which derived it. Not re-derived here. But see findings: the tile is " +
                    "charge-SATURATED, so a factor of three in this number is under 2x in F_es.",
            "Stern capacitance ~20 uF/cm^2 — CITED, order-of-magnitude for aqueous electrodes, " +
                    "and it is now LOAD-BEARING rather than decorative: it sets how much of an " +
                    "applied bias reaches the diffuse layer, hence where point-ion PB dies.",
            "hydrated ionic radii, Nightingale (1959): Mg2+ 4.28 A, Cl- 3.32 A — CITED. They set " +
                    "the Bikerman lattice density and the point-ion boundary.",
            "the PEG layer's salt partition coefficients and effective permittivity — CITED FROM " +
                    "C-0005 (Ogston steric x Born dielectric, combined by Donnan), and one-sided: " +
                    "PEG-cation coordination could raise K and is not bounded there or here.",
            "C-0002's unperturbed layer volume fractions at 5/7/10 nm — CITED FROM C-0002.",
            "C-0001's brush stiffness 111.6 / 26.9 / 7.4 pN/nm — CITED FROM C-0001 and flagged " +
                    "there as a LOWER BOUND pending T-1c. Used only for the T-4 hand-off ratio."
        ),
        temperature = ROOM_TEMPERATURE,
        medium = "aqueous MgCl2 buffer, 2/5/10 mM, 300 K",
        thermalEnergy = thermalEnergy(),
        thermalVoltage = thermalVoltage(),
        bjerrumLength = lb,
        tile = tile,
        tileChargeModels = chargeModels,
        effectiveCharge = effectiveChargePoints(chargeModels, lb),
        electrode = electrodePoints(nominal, lb),
        forces = forcePoints(nominal, lb),
        thresholds = thresholdPoints(nominal, lb),
        bikerman = bikermanPoints(nominal, lb),
        layer = layerPoints(nominal, lb, fibreRadius),
        convergence = convergencePoints(nominal, lb),
        pullIn = pullInPoints(nominal, lb),
        findings = emptyMap(),
        validity = validity(),
        openQuestions = openQuestions()
    )
    val complete = result.copy(findings = findings(result))
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-3a-nonlinear-pb-profile.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForResult()) + "\n"
    )
    report(complete, output)
}

private fun freeBufferSolver(
    concentration: Double,
    tileCharge: Double,
    bjerrumLength: Double
): Solver {
    val buffer = MagnesiumChlorideBuffer(concentration)
    val ions = IonModel(buffer.magnesiumNumberDensity)
    val medium = uniformMedium(GapMedium())
    return Solver(buffer, bjerrumLength, tileCharge, { medium }, { ions })
}

private fun effectiveChargePoints(
    chargeModels: Map<String, Double>,
    bjerrumLength: Double
): List<EffectiveChargePoint> = BUFFERS.flatMap { concentration ->
    val buffer = MagnesiumChlorideBuffer(concentration)
    val kappa = buffer.inverseDebyeLength()
    val negative = asymmetricSaturatedEffectiveChargeDensity(kappa, bjerrumLength, true)
    val positive = asymmetricSaturatedEffectiveChargeDensity(kappa, bjerrumLength, false)
    val symmetric = saturatedEffectiveChargeDensity(kappa, 2, bjerrumLength)
    chargeModels.map { (label, density) ->
        val surface = asymmetricReducedSurfacePotential(-density, kappa, bjerrumLength)
        val effective = asymmetricEffectiveChargeDensity(surface, kappa, bjerrumLength)
        EffectiveChargePoint(
            concentration = concentration,
            ionicStrength = buffer.ionicStrength,
            bulkDebyeLength = buffer.debyeLength(),
            symmetricCeilingDivalent = symmetric,
            asymmetricSaturatedNegativeSurface = negative,
            asymmetricSaturatedPositiveSurface = positive,
            asymmetricOverSymmetric = negative / symmetric,
            positiveOverNegative = positive / negative,
            tileChargeModel = label,
            tileBareChargeDensity = density,
            tileReducedSurfacePotential = surface,
            tileEffectiveChargeDensity = effective,
            tileEffectiveOverBare = abs(effective) / density,
            tileFractionOfSaturation = abs(effective) / negative
        )
    }
}

private fun electrodePoints(tileCharge: Double, bjerrumLength: Double): List<ElectrodePoint> =
    BUFFERS.flatMap { concentration ->
        val buffer = MagnesiumChlorideBuffer(concentration)
        val kappa = buffer.inverseDebyeLength()
        val solver = freeBufferSolver(concentration, tileCharge, bjerrumLength)
        val boundary = stericSaturationPotential(
            1, buffer.chlorideNumberDensity, HYDRATED_CHLORIDE_RADIUS
        )
        listOf(5.0, 10.0).flatMap { height ->
            (BIASES + listOf(1.5)).sorted().map { bias ->
                val diffuse = solver.diffusePotential(height, bias)
                val solution = solver.solve(height, diffuse)
                val charge = solution.electrodeSurfaceChargeDensity
                val compact = charge / solver.stern
                ElectrodePoint(
                    concentration = concentration,
                    gapHeight = height,
                    appliedBias = bias,
                    diffuseLayerPotential = diffuse,
                    compactLayerDrop = compact,
                    compactLayerFraction = if (bias > 0.0) compact / bias else 0.0,
                    electrodeChargeDensity = charge,
                    electrodeEffectiveChargeDensity = asymmetricEffectiveChargeDensity(
                        asymmetricReducedSurfacePotential(charge, kappa, bjerrumLength),
                        kappa, bjerrumLength
                    ),
                    pointIonBoundary = boundary,
                    diffuseOverPointIonBoundary = diffuse / boundary,
                    pointIonValid = diffuse <= boundary
                )
            }
        }
    }

private fun forcePoints(tileCharge: Double, bjerrumLength: Double): List<ForcePoint> =
    BUFFERS.flatMap { concentration ->
        val buffer = MagnesiumChlorideBuffer(concentration)
        val kappa = buffer.inverseDebyeLength()
        val debye = buffer.debyeLength()
        val solver = freeBufferSolver(concentration, tileCharge, bjerrumLength)
        val boundary = stericSaturationPotential(
            1, buffer.chlorideNumberDensity, HYDRATED_CHLORIDE_RADIUS
        )
        GAPS.flatMap { height ->
            BIASES.map { bias ->
                val diffuse = solver.diffusePotential(height, bias)
                val solution = solver.solve(height, diffuse)
                val force = solution.forceOnTile(FOOTPRINT)
                val stiffness = -(
                        solver.force(height + STIFFNESS_STEP, bias) -
                                solver.force(height - STIFFNESS_STEP, bias)
                        ) / (2.0 * STIFFNESS_STEP)
                val decay = if (stiffness != 0.0) force / stiffness else Double.NaN
                val linearBare = linearMixedDisjoiningPressure(
                    height, diffuse / thermalVoltage(), tileCharge, kappa, bjerrumLength
                ) * thermalEnergy() * FOOTPRINT
                // the other cheap bound: linear superposition of the two SATURATED far fields,
                // P = kT kappa^2 A_e A_t exp(-kappa h) / (2 pi l_B)
                val electrodeAmplitude = asymmetricFarFieldAmplitude(
                    asymmetricReducedSurfacePotential(
                        solution.electrodeSurfaceChargeDensity, kappa, bjerrumLength
                    )
                )
                val tileAmplitude = asymmetricFarFieldAmplitude(
                    asymmetricReducedSurfacePotential(tileCharge, kappa, bjerrumLength)
                )
                val linearSaturated = kappa * kappa * electrodeAmplitude * tileAmplitude *
                        exp(-kappa * height) / (2.0 * kotlin.math.PI * bjerrumLength) *
                        thermalEnergy() * FOOTPRINT
                val withoutCompact = solver.solve(height, bias).forceOnTile(FOOTPRINT)
                ForcePoint(
                    medium = "free bulk buffer",
                    concentration = concentration,
                    gapHeight = height,
                    appliedBias = bias,
                    diffuseLayerPotential = diffuse,
                    electrodeChargeDensity = solution.electrodeSurfaceChargeDensity,
                    tileReducedSurfacePotential = solution.tileReducedPotential,
                    disjoiningPressure = solution.disjoiningPressureInPiconewtonPerSquareNanometre,
                    forceOnTile = force,
                    electrostaticStiffness = stiffness,
                    forceDecayLength = decay,
                    bulkDebyeLength = debye,
                    decayOverBulkDebye = decay / debye,
                    stiffnessOverSection1Estimate = abs(stiffness) / (abs(force) / debye),
                    linearBareBoundaryForce = linearBare,
                    linearSaturatedAmplitudeForce = linearSaturated,
                    nonlinearOverLinearBare = force / linearBare,
                    nonlinearOverLinearSaturated = force / linearSaturated,
                    forceWithoutCompactLayer = withoutCompact,
                    meetsHundredPiconewton = abs(force) >= 100.0,
                    pointIonValid = diffuse <= boundary,
                    firstIntegralRelativeSpread = solution.firstIntegralRelativeSpread,
                    firstIntegralCoreSpread = solution.firstIntegralCoreSpread,
                    numericallyResolved = solution.numericallyResolved
                )
            }
        }
    }

private fun thresholdPoints(tileCharge: Double, bjerrumLength: Double): List<ThresholdPoint> =
    BUFFERS.flatMap { concentration ->
        val buffer = MagnesiumChlorideBuffer(concentration)
        val solver = freeBufferSolver(concentration, tileCharge, bjerrumLength)
        val boundary = stericSaturationPotential(
            1, buffer.chlorideNumberDensity, HYDRATED_CHLORIDE_RADIUS
        )
        listOf(5.0, 7.0, 10.0, 15.0).map { height ->
            var low = 0.0
            var high = 2.0
            repeat(24) {
                val middle = 0.5 * (low + high)
                if (abs(solver.force(height, middle)) < 100.0) low = middle else high = middle
            }
            val bias = 0.5 * (low + high)
            val reached = abs(solver.force(height, 2.0)) >= 100.0
            val diffuse = solver.diffusePotential(height, bias)
            ThresholdPoint(
                medium = "free bulk buffer",
                concentration = concentration,
                gapHeight = height,
                biasForHundredPiconewton = if (reached) bias else null,
                diffuseLayerPotentialThere = if (reached) diffuse else null,
                withinPointIonValidity = reached && diffuse <= boundary,
                forceAtTwoVolts = solver.force(height, 2.0),
                forceAtPointIonBoundary = solver.solve(height, boundary).forceOnTile(FOOTPRINT)
            )
        }
    }

private fun bikermanPoints(tileCharge: Double, bjerrumLength: Double): List<BikermanPoint> =
    BUFFERS.flatMap { concentration ->
        val buffer = MagnesiumChlorideBuffer(concentration)
        val medium = uniformMedium(GapMedium())
        val stern = sternChargeDensityPerVolt(STERN_CAPACITANCE)
        listOf(5.0, 7.0, 10.0).flatMap { height ->
            listOf(0.25, 1.0, 2.0).flatMap { bias ->
                listOf(HYDRATED_MAGNESIUM_RADIUS, HYDRATED_CHLORIDE_RADIUS).map { radius ->
                    val maximum = closePackedNumberDensity(radius)
                    val point = IonModel(buffer.magnesiumNumberDensity)
                    val sized = IonModel(buffer.magnesiumNumberDensity, maximum)
                    fun force(model: IonModel): Double {
                        val diffuse = diffusePotentialOfAppliedBias(
                            height, bias, tileCharge, stern, model, medium, bjerrumLength,
                            nodes = SEARCH_NODES
                        )
                        return PoissonBoltzmannGap(
                            height, model, medium, bjerrumLength, nodes = nodesFor(height)
                        ).solve(diffuse / thermalVoltage(), tileCharge).forceOnTile(FOOTPRINT)
                    }
                    val reference = force(point)
                    val modified = force(sized)
                    BikermanPoint(
                        concentration = concentration,
                        gapHeight = height,
                        appliedBias = bias,
                        ionRadius = radius,
                        maximumIonDensity = maximum,
                        pointIonForce = reference,
                        sizeModifiedForce = modified,
                        ratio = modified / reference
                    )
                }
            }
        }
    }

private fun layerPoints(
    tileCharge: Double,
    bjerrumLength: Double,
    fibreRadius: Double
): List<LayerForcePoint> = LAYER_HEIGHTS.flatMap { (resting, restingFraction) ->
    BUFFERS.flatMap { concentration ->
        val buffer = MagnesiumChlorideBuffer(concentration)
        val bare = freeBufferSolver(concentration, tileCharge, bjerrumLength)
        listOf(0.6, 0.8, 1.0).flatMap { compression ->
            val height = resting * compression
            val fraction = restingFraction / compression
            val partitioning = LayerPartitioning(fraction, fibreRadius)
            val salt = partitioning.saltPartitionCoefficient
            val layerMedium = uniformMedium(
                GapMedium(
                    relativePermittivity = partitioning.effectivePermittivity,
                    magnesiumPartitionCoefficient = partitioning.magnesiumPartitionCoefficient,
                    chloridePartitionCoefficient = partitioning.chloridePartitionCoefficient
                )
            )
            val ions = IonModel(buffer.magnesiumNumberDensity)
            val solver = Solver(buffer, bjerrumLength, tileCharge, { layerMedium }, { ions })
            listOf(0.25, 1.0, 2.0).map { bias ->
                val force = solver.force(height, bias)
                val stiffness = -(
                        solver.force(height + STIFFNESS_STEP, bias) -
                                solver.force(height - STIFFNESS_STEP, bias)
                        ) / (2.0 * STIFFNESS_STEP)
                LayerForcePoint(
                    restingHeight = resting,
                    restingVolumeFraction = restingFraction,
                    concentration = concentration,
                    gapHeight = height,
                    polymerVolumeFraction = fraction,
                    saltPartitionCoefficient = salt,
                    effectivePermittivity = partitioning.effectivePermittivity,
                    localDebyeLength = buffer.debyeLength() * partitioning.debyeLengthRatio,
                    appliedBias = bias,
                    forceWithLayer = force,
                    forceWithoutLayer = bare.force(height, bias),
                    layerAmplification = force / bare.force(height, bias),
                    decayLengthWithLayer = force / stiffness
                )
            }
        }
    }
}

private fun convergencePoints(tileCharge: Double, bjerrumLength: Double): List<ConvergencePoint> {
    val buffer = MagnesiumChlorideBuffer(2.0)
    val ions = IonModel(buffer.magnesiumNumberDensity)
    val medium = uniformMedium(GapMedium())
    val stern = sternChargeDensityPerVolt(STERN_CAPACITANCE)
    return listOf(5.0, 30.0).flatMap { height ->
        val diffuse = diffusePotentialOfAppliedBias(
            height, 1.0, tileCharge, stern, ions, medium, bjerrumLength, nodes = 16000
        )
        fun force(nodes: Int) =
            PoissonBoltzmannGap(height, ions, medium, bjerrumLength, nodes = nodes)
                .solve(diffuse / thermalVoltage(), tileCharge)
        val reference = force(32000).forceOnTile(FOOTPRINT)
        listOf(500, 1000, 2000, 4000, 8000, 16000).map { nodes ->
            val solution = force(nodes)
            ConvergencePoint(
                gapHeight = height,
                nodes = nodes,
                force = solution.forceOnTile(FOOTPRINT),
                relativeDeparture = abs(solution.forceOnTile(FOOTPRINT) / reference - 1.0),
                firstIntegralRelativeSpread = solution.firstIntegralRelativeSpread,
                firstIntegralCoreSpread = solution.firstIntegralCoreSpread
            )
        }
    }
}

private fun pullInPoints(tileCharge: Double, bjerrumLength: Double): List<PullInPoint> =
    BUFFERS.flatMap { concentration ->
        val solver = freeBufferSolver(concentration, tileCharge, bjerrumLength)
        BRUSH_STIFFNESS.entries.sortedBy { it.key }.map { (height, stiffness) ->
            fun electrostatic(bias: Double): Double = -(
                    solver.force(height + STIFFNESS_STEP, bias) -
                            solver.force(height - STIFFNESS_STEP, bias)
                    ) / (2.0 * STIFFNESS_STEP)
            var low = 0.0
            var high = 2.0
            repeat(20) {
                val middle = 0.5 * (low + high)
                if (abs(electrostatic(middle)) < stiffness.first) low = middle else high = middle
            }
            val bias = 0.5 * (low + high)
            val cancels = abs(electrostatic(2.0)) >= stiffness.first
            val atTwoVolts = electrostatic(2.0)
            PullInPoint(
                gapHeight = height,
                concentration = concentration,
                brushStiffness = stiffness.first,
                brushSecantStiffness = stiffness.second,
                biasWhereStiffnessCancels = if (cancels) bias else null,
                diffusePotentialThere =
                    if (cancels) solver.diffusePotential(height, bias) else null,
                electrostaticStiffnessAtTwoVolts = atTwoVolts,
                ratioAtTwoVolts = abs(atTwoVolts) / stiffness.first
            )
        }
    }

private fun findings(result: NonlinearPbResult): Map<String, String> {
    fun f(value: Double, digits: Int = 3) = "%.${digits}f".format(value)
    val twoMillimolar = result.effectiveCharge.first {
        it.concentration == 2.0 &&
                it.tileChargeModel.startsWith("half the tile")
    }
    val working = result.forces.filter {
        it.concentration == 2.0 && it.gapHeight in listOf(5.0, 7.0, 10.0)
    }
    val atFiveTwoVolts = working.first { it.gapHeight == 5.0 && it.appliedBias == 2.0 }
    val atFiveQuarter = working.first { it.gapHeight == 5.0 && it.appliedBias == 0.25 }
    val atTenTwoVolts = working.first { it.gapHeight == 10.0 && it.appliedBias == 2.0 }
    val far = result.forces.first {
        it.concentration == 2.0 && it.gapHeight == 30.0 && it.appliedBias == 2.0
    }
    val zeroBias = result.forces.first {
        it.concentration == 2.0 && it.gapHeight == 5.0 && it.appliedBias == 0.0
    }
    val twoVoltElectrode = result.electrode.first {
        it.concentration == 2.0 && it.gapHeight == 5.0 && it.appliedBias == 2.0
    }
    return mapOf(
        "sigma_eff_replacing_C0005_ceiling" to
                "The 2:1 saturated far-field amplitude is 12 - 6 sqrt(3) = 1.6077 k_BT/e at a " +
                "NEGATIVE wall and exactly 6 at a POSITIVE one, so sigma_eff saturates at " +
                "${f(twoMillimolar.asymmetricSaturatedNegativeSurface, 5)} e/nm^2 (tile) and " +
                "${f(twoMillimolar.asymmetricSaturatedPositiveSurface, 5)} e/nm^2 (electrode) at " +
                "2 mM. C-0005's symmetric z:z ceiling is " +
                "${f(twoMillimolar.symmetricCeilingDivalent, 5)} e/nm^2: the asymmetric value is " +
                "exactly 6 - 3 sqrt(3) = 0.8038 of it, i.e. the ceiling was 24% HIGH, which is the " +
                "'order of tens of per cent' C-0005 predicted for itself. At the nominal " +
                "Manning-renormalised tile charge of ${f(twoMillimolar.tileBareChargeDensity, 4)} " +
                "e/nm^2 the ACTUAL effective charge is " +
                "${f(twoMillimolar.tileEffectiveChargeDensity, 5)} e/nm^2 — " +
                "${f(twoMillimolar.tileEffectiveOverBare * 100.0, 1)}% of the Manning charge and " +
                "${f(twoMillimolar.tileFractionOfSaturation * 100.0, 1)}% of the way to saturation. " +
                "The tile is SATURATED, and that is what makes the force insensitive to which " +
                "reading of the tile charge is used.",
        "asymmetry_is_a_factor_of_2_plus_root_3" to
                "A 2:1 electrolyte does NOT screen the two signs of surface charge equally: the " +
                "positive electrode's saturated effective charge is exactly 2 + sqrt(3) = 3.732 " +
                "times the negative tile's, at every concentration. No symmetric closed form can " +
                "produce that, and it is why C-0005 was right to refuse to compute a force from " +
                "its own ceiling. A second consequence, also absent from any symmetric theory: at " +
                "a POSITIVE wall sigma_eff EXCEEDS the bare charge by up to 1.238x around " +
                "y0 = 1.35, because the divalent COION is expelled harder than a monovalent one.",
        "force_at_the_working_gaps" to
                "At 2 mM, free buffer, nominal tile charge: |F_es| = " +
                "${f(abs(atFiveQuarter.forceOnTile), 0)} pN at 5 nm and 0.25 V, " +
                "${f(abs(atFiveTwoVolts.forceOnTile), 0)} pN at 5 nm and 2 V, " +
                "${f(abs(atTenTwoVolts.forceOnTile), 0)} pN at 10 nm and 2 V. The §3 target of " +
                "100 pN is reached at every §3 layer height. But the force SATURATES in bias — " +
                "see biasSaturation — so almost all of it is available below 0.5 V.",
        "zero_bias_is_a_near_cancellation_not_a_baseline_attraction" to
                "At V = 0 the electrode is a grounded conductor, not a neutral wall, and the " +
                "tile's field induces ${f(zeroBias.electrodeChargeDensity, 4)} e/nm^2 of " +
                "countercharge on it. But that charge has to charge the COMPACT layer too, which " +
                "pulls the diffuse-layer potential to ${f(zeroBias.diffuseLayerPotential, 4)} V " +
                "— negative, hence repulsive to the negative tile — and the two nearly cancel. " +
                "The net at 2 mM is under 4 pN in magnitude at EVERY gap and it CHANGES SIGN " +
                "between 4 and 5 nm (+3.94 pN at 3 nm, -0.41 pN at 5 nm). Against that, an ideal " +
                "constant-potential electrode with no compact layer gives " +
                "${f(abs(zeroBias.forceWithoutCompactLayer), 1)} pN of attraction at 5 nm. The two " +
                "readings BRACKET the zero-bias offset; no single number is defensible, and the " +
                "useful statement is that it is small compared with any biased force and that a " +
                "constant-charge electrode model would give exactly zero and miss the physics.",
        "the_decay_length_settling_CH0004" to
                "The force's own decay length, -1/(d ln|F_es|/dh), is " +
                "${f(atFiveTwoVolts.forceDecayLength)} nm at 5 nm and " +
                "${f(atTenTwoVolts.forceDecayLength)} nm at 10 nm under 2 V, rising to " +
                "${f(far.forceDecayLength)} nm at 30 nm against a bulk lambda_D of " +
                "${f(far.bulkDebyeLength)} nm. So: (1) it is NOT the bulk lambda_D at the working " +
                "gap — §1's estimate is wrong there, as CH-0004 said; (2) it is NOT the " +
                "0.84-1.18 nm counterion length CH-0004 proposed either — that is 2 to 3 times " +
                "too short; (3) it approaches the BULK lambda_D from below as the gap opens, " +
                "which is CH-0004's own 'if this challenge is itself wrong' clause firing. The " +
                "counterions are sequestered at the tile underside and the mid-gap is nearer bulk " +
                "composition than the uniform-density count suggested.",
        "error_in_section_1_stiffness_estimate" to
                "|k_es| = |F_es|/lambda_D understates the true |k_es| by " +
                "${f(atFiveTwoVolts.stiffnessOverSection1Estimate, 2)}x at 5 nm and " +
                "${f(atTenTwoVolts.stiffnessOverSection1Estimate, 2)}x at 10 nm (2 mM, 2 V), and " +
                "by ${f(zeroBias.stiffnessOverSection1Estimate, 2)}x at zero bias. The error runs " +
                "in the NON-CONSERVATIVE direction for pull-in, exactly as CH-0004 warned, but " +
                "the factor is under 2.5x rather than the 3.3-4.7x CH-0004 estimated.",
        "biasSaturation" to
                "The compact layer takes " +
                "${f(twoVoltElectrode.compactLayerFraction * 100.0, 1)}% of a 2 V bias, so the " +
                "diffuse layer only ever sees ${f(twoVoltElectrode.diffuseLayerPotential, 3)} V. " +
                "Since the diffuse layer is what the tile feels, and since its far field " +
                "saturates, the force is nearly flat in bias above ~0.5 V: " +
                "|F_es| grows only ${f(abs(atFiveTwoVolts.forceOnTile / atFiveQuarter.forceOnTile), 2)}x " +
                "between 0.25 V and 2 V — a factor of 8 in bias.",
        "point_ion_boundary_read_correctly" to
                "C-0005's 0.197 V is a DIFFUSE-LAYER drop and was compared against §3's 2 V " +
                "applied bias as 'a factor of ten'. With the compact layer in series the applied " +
                "bias that produces 0.197 V of diffuse drop is about 1 V, and 2 V produces " +
                "${f(twoVoltElectrode.diffuseLayerPotential, 3)} V of diffuse drop — only " +
                "${f(twoVoltElectrode.diffuseOverPointIonBoundary, 2)}x past the boundary, not " +
                "10x. Filed as CH-0007. The direction is favourable: point-ion PB survives far " +
                "further up the §3 bias range than C-0005's comparison implied.",
        "the_two_cheap_bounds_bracket_the_answer" to
                "Both cheap bounds were run first, and neither is adequate — they bracket the " +
                "nonlinear answer from opposite sides, which is the cost justification for the " +
                "solve. (a) Linearised Debye-Huckel at the SAME boundary data OVERSTATES: the " +
                "nonlinear force is ${f(abs(atFiveTwoVolts.nonlinearOverLinearBare), 3)} of it at " +
                "5 nm / 2 V, because a linear theory has no charge saturation and takes the " +
                "9.1 k_BT/e electrode potential at face value. (b) Linear superposition of the " +
                "two SATURATED far fields UNDERSTATES: the nonlinear force is " +
                "${f(abs(atFiveTwoVolts.nonlinearOverLinearSaturated), 2)}x it at 5 nm / 2 V, " +
                "because at kappa*h = ${f(5.0 / atFiveTwoVolts.bulkDebyeLength, 2)} the two " +
                "double layers overlap and superposition is outside its own premise. The true " +
                "answer sits between an overestimate of ~4x and an underestimate of ~4x, so " +
                "nothing short of the nonlinear solve settles it.",
        "pull_in_margin_handed_to_T4" to
                "Against C-0001's (provisional, lower-bound) brush stiffness, |k_es| equals " +
                "k_brush at an APPLIED bias of " +
                result.pullIn.filter { it.concentration == 2.0 }.joinToString("; ") { point ->
                    "${f(point.biasWhereStiffnessCancels ?: Double.NaN, 3)} V at " +
                            "${f(point.gapHeight, 0)} nm"
                } +
                " (2 mM), and exceeds it by " +
                "${f(result.pullIn.first { it.concentration == 2.0 && it.gapHeight == 5.0 }.ratioAtTwoVolts, 1)}x " +
                "to ${f(result.pullIn.first { it.concentration == 2.0 && it.gapHeight == 7.0 }.ratioAtTwoVolts, 1)}x " +
                "at 2 V. Set against the bias needed for 100 pN — " +
                result.thresholds.filter { it.concentration == 2.0 }.joinToString("; ") { point ->
                    "${f(point.biasForHundredPiconewton ?: Double.NaN, 3)} V at " +
                            "${f(point.gapHeight, 0)} nm"
                } +
                " — the margin is favourable at 5 nm (pull-in sits ABOVE the force target) and " +
                "adverse at 10 nm (pull-in sits an order of magnitude BELOW it). This is T-4's " +
                "question, not this task's, and it is handed over rather than answered: C-0001's " +
                "stiffness is a lower bound pending T-1c, and a larger k_brush moves every " +
                "cancellation bias up.",
        "lateral_load_profile" to
                "A 1-D solve CANNOT supply the lateral load profile T-5b needs, and this is stated " +
                "plainly rather than estimated. What it CAN supply, and what converts a geometric " +
                "edge perturbation into the load non-uniformity C-0006's exactly-linear dishing " +
                "result consumes, is the pressure sensitivity d ln|P|/dh = -1/ell, i.e. " +
                "${f(1.0 / atFiveTwoVolts.forceDecayLength, 4)} per nm at 5 nm and " +
                "${f(1.0 / atTenTwoVolts.forceDecayLength, 4)} per nm at 10 nm. A 2-D solve of the " +
                "tile edge is the task that would close it."
    )
}

private fun validity(): List<String> = listOf(
    "MEAN FIELD. C-0005 puts the one-loop correction at 123-214% of the leading PB term across " +
            "the entire 5-10 nm range for Mg2+, so PB here is UNCONTROLLED: the size of the error " +
            "is not bounded by its own expansion. Every force in this file is a mean-field number " +
            "and must travel with that statement. The correction is ATTRACTIVE between LIKE " +
            "charges; for the oppositely charged tile-electrode pair no published result gives " +
            "even the direction, and none is claimed here.",
    "POINT IONS, except where the Bikerman bracket is reported. The bracket says size " +
            "modification RAISES |F_es| by a few per cent to ~20% depending on the assumed ion " +
            "size, so the point-ion numbers are a LOWER bound on |F_es| within mean field.",
    "The compact-layer capacitance of 20 uF/cm^2 is an order-of-magnitude literature value and it " +
            "is now load-bearing: it decides how much of the applied bias reaches the diffuse " +
            "layer. Halving it roughly doubles the compact drop and lowers the diffuse potential; " +
            "the force is nearly flat in that region, so the FORCE is insensitive, but the " +
            "point-ion boundary in APPLIED bias moves with it.",
    "2 V in aqueous buffer is beyond the thermodynamic water window (1.23 V) and beyond practical " +
            "overpotential limits on most electrodes. Faradaic current, gas evolution and " +
            "electrode corrosion are outside every model here. The 2 V column is reported because " +
            "§3 asks for it, not because it is an operable point.",
    "The tile is modelled as a uniformly charged PLANE at z = h. It is a 10 nm slab of duplexes " +
            "with electrolyte in its interstices; the three readings of its gap-facing charge span " +
            "a factor of three and are all reported. Charge saturation makes the force insensitive " +
            "to the choice (under 2x across that factor of three), which is the only reason a " +
            "single number can be quoted at all.",
    "1-D. No edge, no fringing field, no lateral structure. The 40 x 40 nm tile is 4 to 13 gap " +
            "heights across, so fringing affects a rim of order h; that is an argument, not a " +
            "computed profile, and it is NOT used to produce a number.",
    "No charge regulation at the tile (phosphate pKa ~ 1, so this is safe) and none at the " +
            "electrode beyond the Stern series. No dielectric layer of §1. No specific " +
            "Mg2+-phosphate or Mg2+-PEG chemistry.",
    "The PEG layer enters only through C-0005's salt partition coefficients and effective " +
            "permittivity, and the disjoining pressure is referenced to the LOCAL medium so that " +
            "the pure salt-depletion term — which belongs to the layer's own free energy and hence " +
            "to T-1/T-1c — is excluded rather than double-counted.",
    "Nothing here is measured. PASS means model-consistent and traceable."
)

private fun openQuestions(): List<String> = listOf(
    "The direction of the correlation correction for OPPOSITELY charged walls is unknown. Every " +
            "published Xi criterion is a like-charge result. This is the single largest " +
            "uncertainty on F_es and no closed form repairs it; explicit-ion simulation is the " +
            "only route, at the 1-3 week cost C-0005 priced.",
    "The lateral load profile across the tile is NOT computed here and cannot be. T-5b needs it " +
            "and C-0006 shows dishing is exactly linear in it, so a 2-D (or axisymmetric) solve of " +
            "the tile edge would convert directly into a dishing amplitude.",
    "Whether the electrode can be biased to 1-2 V in aqueous MgCl2 at all is an electrochemistry " +
            "question this task does not touch. If the usable window is ~1 V the force numbers " +
            "barely move, because of saturation — but that is a happy accident, not an argument.",
    "The Stern capacitance is cited, not derived, and it now sets where point-ion PB dies in " +
            "APPLIED bias. T-6b should sharpen it, or replace the series model with a " +
            "size-modified treatment that has no separate compact layer.",
    "The layer's partition coefficients are C-0005's one-sided exclusion bound. If PEG-cation " +
            "coordination raises K above 1 (P-8), the layer would screen MORE, not less, and the " +
            "layerAmplification column would invert."
)

private fun report(result: NonlinearPbResult, output: File) {
    println("T-3a — ${result.title}")
    println("leaf ${result.leaf}; 300 K, aqueous MgCl2; l_B = ${"%.4f".format(result.bjerrumLength)} nm")
    println()
    println("--- effective charge: C-0005's ceiling vs the asymmetric solve ".padEnd(110, '-'))
    println("%6s %12s %12s %12s %10s %46s %10s %12s".format(
        "c[mM]", "sym(z:z)", "asym(neg)", "asym(pos)", "asym/sym", "tile model", "sigma_t", "sigma_eff"
    ))
    result.effectiveCharge.filter { it.concentration == 2.0 || it.tileChargeModel.startsWith("half") }
        .forEach {
            println("%6.1f %12.5f %12.5f %12.5f %10.4f %46s %10.4f %12.5f".format(
                it.concentration, it.symmetricCeilingDivalent,
                it.asymmetricSaturatedNegativeSurface, it.asymmetricSaturatedPositiveSurface,
                it.asymmetricOverSymmetric, it.tileChargeModel.take(46),
                it.tileBareChargeDensity, it.tileEffectiveChargeDensity
            ))
        }
    println()
    println("--- the electrode: how much of the bias reaches the diffuse layer ".padEnd(110, '-'))
    println("%6s %6s %8s %10s %10s %10s %10s %8s".format(
        "c[mM]", "h[nm]", "V[V]", "psi_d[V]", "Stern[V]", "Stern%", "sigma_e", "PB ok"
    ))
    result.electrode.filter { it.gapHeight == 5.0 }.forEach {
        println("%6.1f %6.1f %8.2f %10.4f %10.4f %10.1f %10.4f %8s".format(
            it.concentration, it.gapHeight, it.appliedBias, it.diffuseLayerPotential,
            it.compactLayerDrop, it.compactLayerFraction * 100.0,
            it.electrodeChargeDensity, it.pointIonValid
        ))
    }
    println()
    println("--- F_es(h, V), 2 mM, free buffer ".padEnd(110, '-'))
    println("%6s %8s %12s %12s %10s %10s %10s %8s".format(
        "h[nm]", "V[V]", "F[pN]", "k_es[pN/nm]", "ell[nm]", "ell/lamD", "k/k_§1", "100pN"
    ))
    result.forces.filter { it.concentration == 2.0 }.forEach {
        println("%6.1f %8.2f %12.2f %12.3f %10.3f %10.3f %10.3f %8s".format(
            it.gapHeight, it.appliedBias, it.forceOnTile, it.electrostaticStiffness,
            it.forceDecayLength, it.decayOverBulkDebye, it.stiffnessOverSection1Estimate,
            it.meetsHundredPiconewton
        ))
    }
    println()
    println("--- bias needed for the §3 100 pN target ".padEnd(110, '-'))
    println("%6s %6s %14s %14s %10s %14s".format("c[mM]", "h[nm]", "V(100pN)", "psi_d there", "PB ok", "F(2V)[pN]"))
    result.thresholds.forEach {
        println("%6.1f %6.1f %14.4f %14.4f %10s %14.1f".format(
            it.concentration, it.gapHeight, it.biasForHundredPiconewton ?: Double.NaN,
            it.diffuseLayerPotentialThere ?: Double.NaN, it.withinPointIonValidity,
            it.forceAtTwoVolts
        ))
    }
    println()
    println("--- the PEG layer in the gap ".padEnd(110, '-'))
    println("%6s %6s %8s %8s %8s %8s %12s %12s %8s".format(
        "L0", "h[nm]", "c[mM]", "phi", "K_salt", "V[V]", "F_layer", "F_bare", "ratio"
    ))
    result.layer.filter { it.concentration == 2.0 }.forEach {
        println("%6.1f %6.1f %8.1f %8.4f %8.4f %8.2f %12.1f %12.1f %8.3f".format(
            it.restingHeight, it.gapHeight, it.concentration, it.polymerVolumeFraction,
            it.saltPartitionCoefficient, it.appliedBias, it.forceWithLayer,
            it.forceWithoutLayer, it.layerAmplification
        ))
    }
    println()
    println("--- Bikerman bracket (T-6b, folded in) ".padEnd(110, '-'))
    println("%6s %6s %8s %10s %12s %12s %8s".format("c[mM]", "h[nm]", "V[V]", "r_ion", "F_point", "F_sized", "ratio"))
    result.bikerman.filter { it.concentration == 2.0 && it.gapHeight == 5.0 }.forEach {
        println("%6.1f %6.1f %8.2f %10.3f %12.1f %12.1f %8.4f".format(
            it.concentration, it.gapHeight, it.appliedBias, it.ionRadius,
            it.pointIonForce, it.sizeModifiedForce, it.ratio
        ))
    }
    println()
    println("--- mesh convergence ".padEnd(110, '-'))
    println("%8s %8s %14s %14s %16s".format("h[nm]", "nodes", "F[pN]", "departure", "1st-int spread"))
    result.convergence.forEach {
        println("%8.1f %8d %14.4f %14.3e %16.3e".format(
            it.gapHeight, it.nodes, it.force, it.relativeDeparture, it.firstIntegralRelativeSpread
        ))
    }
    println()
    println("--- hand-off to T-4: |k_es| against C-0001's brush stiffness ".padEnd(110, '-'))
    println("%6s %8s %14s %16s %16s %10s".format(
        "h[nm]", "c[mM]", "k_brush", "V(k_eff = 0)", "k_es(2V)", "ratio"
    ))
    result.pullIn.forEach {
        println("%6.1f %8.1f %14.1f %16.4f %16.1f %10.2f".format(
            it.gapHeight, it.concentration, it.brushStiffness,
            it.biasWhereStiffnessCancels ?: Double.NaN,
            it.electrostaticStiffnessAtTwoVolts, it.ratioAtTwoVolts
        ))
    }
    println()
    println("--- FINDINGS ".padEnd(110, '-'))
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written: ${output.path}")
}
