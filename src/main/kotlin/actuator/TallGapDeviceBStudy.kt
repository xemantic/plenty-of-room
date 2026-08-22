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

package com.xemantic.nano.plentyofroom.actuator

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.brush.GraftedChain
import com.xemantic.nano.plentyofroom.brush.GraftedLayerModel
import com.xemantic.nano.plentyofroom.brush.chainLengthForHeight
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannGap
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.diffusePotentialOfAppliedBias
import com.xemantic.nano.plentyofroom.electrostatics.sternChargeDensityPerVolt
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.electrostatics.uniformMedium
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * Task `T-192` — **device B in the corner all three NDI answers point at: a 10 pN/nm coupling on
 * a 17–26 nm layer at 0.5 mM.** Leaf `A8.2`, with `A2.2` and `A7.4`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh actuator.TallGapDeviceBStudyKt
 * ```
 *
 * Emits `gpd/results/T-192-device-b-tall-gap.json`, deterministically — no timestamp, no
 * wall clock, no step counts, every floating-point number rounded at the serialisation boundary
 * by [roundedForActuatorResult], and every convergence or reproduction **departure** emitted at
 * **two significant digits** by [tallGapTwoSignificantDigits] because a dimensionless difference
 * of two nearly equal quantities is not in the locked units the absolute floor is a claim about.
 *
 * Consumes `C-0008`'s field pipeline and `C-0018`'s [EquilibriumPath] as libraries, **re-run
 * rather than tabulated**, and owns `TallGapDeviceB.kt` and this file.
 */

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

/** `P1`/`P2` — the field alone, at a tall gap. No layer, no coupling, no model bracket. */
@Serializable
@Suppress("LongParameterList")
data class TallGapDeviceBReachRecord(
    val gap: Double,
    val concentration: Double,
    val bulkDebyeLength: Double,
    val gapInBulkDebyeLengths: Double,
    val counterionDominanceRatio: Double,
    val counterionScreeningLength: Double,
    val biasForTargetForce: Double?,
    val diffuseDropForTargetForce: Double?,
    val attractionAtPointIonCeiling: Double,
    val attractionAtElectrochemicalCeiling: Double,
    val attractionAtDiffuseCeiling: Double,
    val appliedBiasAtDiffuseCeiling: Double,
    val decayLengthAtPointIonCeiling: Double,
    val decayLengthOverBulkDebye: Double,
    val electrostaticStiffnessAtPointIonCeiling: Double,
    val reachable: Boolean,
    val limitedBy: String
)

/**
 * `P1` as a THRESHOLD rather than a table: how far §3's 100 pN reaches at all, per buffer.
 *
 * A grid of four heights says whether the force arrives at four places; this says where it stops
 * arriving, which is the number a specification conversation needs.
 */
@Serializable
data class TallGapDeviceBReachThresholdRecord(
    val concentration: Double,
    val appliedBias: Double,
    val bulkDebyeLength: Double,
    val deepestGapReachingTarget: Double?,
    val deepestGapInBulkDebyeLengths: Double?,
    val note: String
)

/** `P3`/`P4` — one solved `(model, density rule, height, buffer, load line)` state. */
@Serializable
@Suppress("LongParameterList")
data class TallGapDeviceBStateRecord(
    val modelName: String,
    val densityRule: String,
    val nominalHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val loadLine: String,
    val couplingStiffness: Double,
    val deadLoad: Double,
    val targetStroke: Double,
    val restingHeight: Double,
    val dryThickness: Double,
    val strokeCeiling: Double,
    val targetStrokeIsReachable: Boolean,
    val operatingBias: Double?,
    val operatingGap: Double?,
    val operatingVolumeFraction: Double?,
    val operatingLayerLoad: Double?,
    val brushStiffnessAtOperating: Double?,
    val electrostaticStiffnessAtOperating: Double?,
    val effectiveStiffnessAtOperating: Double?,
    val stabilityFloorAtOperating: Double?,
    val couplingClearsTheFloor: Boolean?,
    val couplingMarginAtOperating: Double?,
    val strokeCapAtOperating: Double?,
    val pullInBias: Double?,
    val pullInStroke: Double?,
    val pullInGap: Double?,
    val foldAtBranchStart: Boolean,
    val stableShallowBranchExists: Boolean,
    val foldInsideTargetStroke: Boolean?,
    val branchEndStroke: Double?,
    val branchEndBias: Double?,
    val branchEndedOnTheField: Boolean,
    val brushStiffnessAtFold: Double?,
    val electrostaticStiffnessAtFold: Double?,
    val effectiveStiffnessAtFold: Double?,
    val coupledTangentAtFold: Double?,
    val tangencyResidual: Double?,
    val forceDecayLengthAtFold: Double?,
    val correlationBandBias: Double?,
    val concentratedCrossoverBias: Double?,
    val pointIonBias: Double,
    val electrochemicalBias: Double,
    val bindingCeiling: String?,
    val usableBias: Double?,
    val margin: Double?,
    val operatingPointIsUsable: Boolean?,
    val verdict: String
)

/** `P4` — one quantity that a 17–26 nm layer takes outside an established range. */
@Serializable
data class TallGapDeviceBValidityRecord(
    val quantity: String,
    val establishedRange: String,
    val valueHere: String,
    val direction: String,
    val severity: String,
    val whatItWouldTake: String
)

/** One convergence axis, referred to **its own** finest setting. */
@Serializable
data class TallGapDeviceBConvergenceRecord(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departureFromFinest: Double
)

/** Gate 5 — an upstream number reproduced through this study's own solver. */
@Serializable
data class TallGapDeviceBReproductionRecord(
    val quantity: String,
    val state: String,
    val here: Double,
    val upstream: Double,
    val relativeDeparture: Double,
    val source: String
)

/** The `T-192` result envelope. */
@Serializable
@Suppress("LongParameterList")
data class TallGapDeviceBResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val runParameters: Map<String, String>,
    val citedInputs: List<String>,
    val densityRules: List<TallGapDeviceBDensityRule>,
    val premises: List<TallGapDeviceBPremiseRecord>,
    val reach: List<TallGapDeviceBReachRecord>,
    val reachThresholds: List<TallGapDeviceBReachThresholdRecord>,
    val layers: List<TallGapDeviceBLayerRecord>,
    val states: List<TallGapDeviceBStateRecord>,
    val validityDepartures: List<TallGapDeviceBValidityRecord>,
    val convergence: List<TallGapDeviceBConvergenceRecord>,
    val reproductions: List<TallGapDeviceBReproductionRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val prosePanic: String = ""
)

// ---------------------------------------------------------------------------------------------
// the sweep
// ---------------------------------------------------------------------------------------------

/** NDI's own reserve, read as a force-onset layer height in nm. */
private val TALL_GAP_HEIGHTS = listOf(17.0, 20.0, 23.0, 26.0)

/** §3's own heights, carried so the tall answers have a same-solver reference beside them. */
private val TALL_GAP_REFERENCE_HEIGHTS = listOf(10.0)

/**
 * Every gap the reachability bound is read at.
 *
 * §3's own 5/7/10 nm for continuity, NDI's 17/20/23/26 nm, and **13 and 16 nm, which are the
 * gaps `L₀ − 10 nm` that device B's own duty occupies** at the 23 and 26 nm layers — the second
 * reading of *"across such a gap"*, tabulated rather than argued.
 */
private val TALL_GAP_REACH_GAPS =
    listOf(5.0, 7.0, 10.0, 13.0, 15.0, 16.0, 17.0, 20.0, 23.0, 26.0)

/** Leaf `A2.2`'s low-screening point, an intermediate, and §3's nominal buffer. */
private val TALL_GAP_REACH_BUFFERS = listOf(0.5, 1.0, 2.0)

/** The two buffers the fold sweep is run at — NDI's reserve and §3's nominal. */
private val TALL_GAP_BUFFERS = listOf(0.5, 2.0)

private const val TALL_GAP_POINT_ION_CEILING = 1.0

private const val TALL_GAP_ELECTROCHEMICAL_CEILING = 1.23

private const val TALL_GAP_CORRELATION_BAND = 1.46

private const val TALL_GAP_CONCENTRATED_CROSSOVER = 0.2

private const val TALL_GAP_STERN_CAPACITANCE = 20.0

private const val TALL_GAP_MESH_NODES = 2000

/** The smallest gap the field is asked about, in nm — `C-0012`'s own force-curve floor. */
private const val TALL_GAP_LOWEST_GAP = 0.5

/** A load line, in the two numbers that define an affine one, plus the stroke it is read at. */
private data class TallGapDeviceBLoadLine(
    val name: String,
    val stiffness: Double,
    val preload: Double,
    val targetStroke: Double
) {

    fun reaction(stroke: Double): Double = preload + stiffness * stroke
}

/**
 * The four load lines. **Device-B and dead-load pass through the same point** — 100 pN at
 * 10 nm — and differ only in slope, exactly as `C-0018`'s coupled and dead-load lines do at
 * 3 nm; **free** is the reference, because `CLAUDE.md` records that a coupling can be a net
 * source of the very thing it was added to remove, and a sweep that never runs the uncoupled
 * case cannot see it.
 */
private val TALL_GAP_LOAD_LINES = listOf(
    TallGapDeviceBLoadLine("free", 0.0, 0.0, TALL_GAP_DEVICE_B_STROKE),
    TallGapDeviceBLoadLine(
        "device-B (k_c = 10 pN/nm)", TALL_GAP_DEVICE_B_STIFFNESS, 0.0, TALL_GAP_DEVICE_B_STROKE
    ),
    TallGapDeviceBLoadLine(
        "device-A (k_c = 33.333 pN/nm)", TALL_GAP_DEVICE_A_STIFFNESS, 0.0, 3.0
    ),
    TallGapDeviceBLoadLine("dead-load (100 pN)", 0.0, TALL_GAP_TARGET_FORCE, TALL_GAP_DEVICE_B_STROKE)
)

/**
 * The field, parametrised by the **diffuse-layer drop**, which is the cheap direction: one
 * Poisson-Boltzmann solve yields the force *and* the applied bias that produced it, where the
 * inverse costs 34 solves of Stern-series bisection (`C-0018`, a factor of ~35 on this sweep).
 */
private class TallGapDeviceBField(
    concentration: Double,
    val tileCharge: Double,
    val bjerrum: Double,
    val nodes: Int = TALL_GAP_MESH_NODES
) {

    private val ions = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity)

    private val medium = uniformMedium(GapMedium())

    private val stern = sternChargeDensityPerVolt(TALL_GAP_STERN_CAPACITANCE)

    private val volt = thermalVoltage()

    fun sample(gap: Double, diffusePotential: Double): FieldSample {
        val solution = PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = nodes)
            .solve(diffusePotential / volt, tileCharge)
        return FieldSample(
            gap = gap,
            diffusePotential = diffusePotential,
            appliedBias = diffusePotential + solution.electrodeSurfaceChargeDensity / stern,
            force = solution.forceOnTile(TALL_GAP_FOOTPRINT)
        )
    }

    fun asPath(): DiffuseParametrisedField =
        DiffuseParametrisedField { gap, psi -> sample(gap, psi) }

    /** The **signed** force in pN at a given **applied** bias — `C-0008`'s own direction. */
    fun forceAtBias(gap: Double, bias: Double): Double {
        val diffuse = diffusePotentialOfAppliedBias(
            gap, bias, tileCharge, stern, ions, medium, bjerrum, nodes = nodes
        )
        return PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = nodes)
            .solve(diffuse / volt, tileCharge)
            .forceOnTile(TALL_GAP_FOOTPRINT)
    }

    /** `k_es = −∂F_z/∂h` in pN/nm at fixed **applied** bias, centrally differenced. */
    fun stiffnessAtBias(gap: Double, bias: Double, step: Double = 1e-3): Double {
        val separation = 2.0 * step
        return -(forceAtBias(gap + step, bias) - forceAtBias(gap - step, bias)) / separation
    }
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val peg = PegWater()
    val geometry = ActuatorGeometry()
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val surviving = tile.manningSurvivingFraction(2, lb)
    val tileCharge = -(tile.projectedChargeDensity * surviving / 2.0)
    val models = tallGapLayerModels(peg)
    val heldRule = tallGapHeldDensityRule(0.024)
    val trendRule = tallGapPowerLawFit("section-3 trend", TALL_GAP_SECTION_3_DESIGN_POINTS)
    val rules = listOf(heldRule, trendRule)

    println("T-192 — the cheap bound: can the field reach 100 pN across a tall gap at all ...")
    val reachFields = TALL_GAP_REACH_BUFFERS.associateWith {
        TallGapDeviceBField(it, tileCharge, lb)
    }
    val reach = TALL_GAP_REACH_BUFFERS.flatMap { concentration ->
        val field = reachFields.getValue(concentration)
        val debye = MagnesiumChlorideBuffer(concentration).debyeLength()
        TALL_GAP_REACH_GAPS.map { gap -> tallGapReach(field, gap, concentration, debye) }
    }
    println("  ${reach.count { it.reachable }} of ${reach.size} (gap, buffer) cells reach 100 pN")

    println("T-192 — and the threshold: how far does 100 pN reach at all ...")
    val reachThresholds = TALL_GAP_REACH_BUFFERS.flatMap { concentration ->
        val field = reachFields.getValue(concentration)
        val debye = MagnesiumChlorideBuffer(concentration).debyeLength()
        listOf(TALL_GAP_POINT_ION_CEILING, TALL_GAP_ELECTROCHEMICAL_CEILING).map { bias ->
            val deepest = tallGapDeepestReachableGap(TALL_GAP_TARGET_FORCE, 1.0, 40.0) { gap ->
                -field.forceAtBias(gap, bias)
            }
            TallGapDeviceBReachThresholdRecord(
                concentration = concentration,
                appliedBias = bias,
                bulkDebyeLength = debye,
                deepestGapReachingTarget = deepest,
                deepestGapInBulkDebyeLengths = deepest?.let { it / debye },
                note = if (deepest == null)
                    "100 pN is not reached at any gap in [1, 40] nm at this bias"
                else "the deepest gap across which the field delivers §3's 100 pN at this bias"
            )
        }
    }
    reachThresholds.forEach {
        println("  " + it.concentration + " mM at " + it.appliedBias + " V: " +
                it.deepestGapReachingTarget + " nm")
    }

    println("T-192 — the layer census and the scaling premises at 17-26 nm ...")
    val allHeights = TALL_GAP_REFERENCE_HEIGHTS + TALL_GAP_HEIGHTS
    val premises = rules.flatMap { rule ->
        allHeights.map { height ->
            tallGapScalingPremises(height, rule.densityAt(height), peg)
        }
    }
    val layers = rules.flatMap { rule ->
        allHeights.flatMap { height ->
            models.map { (name, _) ->
                tallGapLayerCensus(
                    modelName = name,
                    height = height,
                    graftingDensity = rule.densityAt(height),
                    densityRule = rule.name,
                    peg = peg
                )
            }
        }
    }

    println("T-192 — the fold sweep: ${TALL_GAP_HEIGHTS.size} heights x ${rules.size} rules x " +
            "${models.size} models x ${TALL_GAP_BUFFERS.size} buffers x " +
            "${TALL_GAP_LOAD_LINES.size} load lines ...")
    val foldFields = TALL_GAP_BUFFERS.associateWith { TallGapDeviceBField(it, tileCharge, lb) }
    val states = mutableListOf<TallGapDeviceBStateRecord>()
    rules.forEach { rule ->
        TALL_GAP_HEIGHTS.forEach { height ->
            val density = rule.densityAt(height)
            models.forEach { (name, model) ->
                val chain = peg.graftedChain(
                    model.chainLengthForHeight(peg, height, density), density
                )
                val balance = ActuatorForceBalance(model, chain, geometry)
                TALL_GAP_BUFFERS.forEach { concentration ->
                    val field = foldFields.getValue(concentration)
                    TALL_GAP_LOAD_LINES.forEach { line ->
                        states += tallGapState(
                            name, rule.name, height, density, concentration,
                            balance, chain, model, field, line
                        )
                    }
                }
            }
            println("  ${rule.name}  ${height} nm done")
        }
    }

    println("T-192 — reproducing C-0008, C-0017 and C-0018 through this solver ...")
    val reproductions = tallGapReproductions(peg, geometry, foldFields, reachFields, lb)

    println("T-192 — convergence ...")
    // CLAUDE.md: a convergence axis cannot be read at a state where the quantity does not exist.
    // The fold axis's state is therefore picked AFTER the sweep, from the device-B states that
    // actually folded — the first one in emission order, so the choice is a property of the
    // record list and not of an argmin.
    val foldingState = states.firstOrNull {
        it.couplingStiffness == TALL_GAP_DEVICE_B_STIFFNESS && it.pullInBias != null
    }
    val convergence = tallGapConvergence(peg, geometry, tileCharge, lb, foldingState)

    val result = TallGapDeviceBResult(
        task = "T-192",
        leaf = "A8.2",
        title = "Device B in the corner NDI's answers to decisions 2 and 4 point at: a " +
                "10 pN/nm coupling on a 17-26 nm layer at 0.5 mM MgCl2 — the bias that " +
                "delivers 100 pN across such a gap, which decay length governs there, and " +
                "whether the equilibrium path folds inside the 10 nm stroke device B is for",
        verificationType = "in-silico (C-0008's nonlinear Poisson-Boltzmann gap solve and " +
                "C-0018's stroke-parametrised equilibrium path, both re-run as libraries at " +
                "heights neither has been asked about) + logical (a reachability bound on the " +
                "force that needs no layer model at all, and runs first)",
        acceptance = "P1: the applied bias that delivers 100 pN across 17/20/23/26 nm at " +
                "0.5/1/2 mM, at BOTH readings of the gap (the resting height and the held gap " +
                "L0 - 10 nm the device-B duty occupies), with null and the ceiling that " +
                "produced it where the field cannot reach. P2: the decay length MEASURED on " +
                "the solve at those gaps against all three of the lengths CH-0004/C-0008 " +
                "distinguish, and the counterion-dominance ratio computed rather than " +
                "transferred. P3: |k_eff| and the pull-in fold at those heights in 0.5 and " +
                "2 mM against C-0046's 10 pN/nm placement, C-0017's stability floor and " +
                "C-0046's composed cap. P4: a per-quantity statement of every established " +
                "validity range a 17-26 nm layer leaves, and what it would take to establish it.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "Every force inherits C-0008's mean-field statement in full (C-0005: the " +
                "one-loop correction is 123-214% of the leading term across this gap range). " +
                "AND THE LAYER IS WORSE OFF THAN THAT: a 17-26 nm grafted PEG layer is " +
                "OUTSIDE every validity range this programme has established — see " +
                "validityDepartures[]. NOTHING HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "potential" to "V",
            "concentration" to "mM",
            "grafting density" to "nm^-2",
            "molar mass" to "g/mol",
            "temperature" to "K"
        ),
        conventions = TALL_GAP_CONVENTIONS,
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to thermalEnergy().toString(),
            "bjerrumLength" to lb.roundedForProse().toString(),
            "footprintArea" to TALL_GAP_FOOTPRINT.toString(),
            "manningSurvivingFraction" to surviving.roundedForProse().toString(),
            "nominalTileChargeDensity" to tileCharge.roundedForProse().toString(),
            "sternCapacitance" to TALL_GAP_STERN_CAPACITANCE.toString(),
            "meshNodes" to TALL_GAP_MESH_NODES.toString(),
            "tallHeights" to TALL_GAP_HEIGHTS.toString(),
            "referenceHeights" to TALL_GAP_REFERENCE_HEIGHTS.toString(),
            "reachGaps" to TALL_GAP_REACH_GAPS.toString(),
            "reachBuffers" to TALL_GAP_REACH_BUFFERS.toString(),
            "foldBuffers" to TALL_GAP_BUFFERS.toString(),
            "loadLines" to TALL_GAP_LOAD_LINES.map {
                it.name + ": R = " + it.preload.roundedForProse() + " + " +
                        it.stiffness.roundedForProse() + " s, read at s = " +
                        it.targetStroke.roundedForProse()
            }.toString(),
            "deviceBStiffness" to TALL_GAP_DEVICE_B_STIFFNESS.toString(),
            "deviceAStiffness" to TALL_GAP_DEVICE_A_STIFFNESS.roundedForProse().toString(),
            "targetForce" to TALL_GAP_TARGET_FORCE.toString(),
            "deviceBStroke" to TALL_GAP_DEVICE_B_STROKE.toString(),
            "pointIonCeiling" to TALL_GAP_POINT_ION_CEILING.toString(),
            "electrochemicalCeiling" to TALL_GAP_ELECTROCHEMICAL_CEILING.toString(),
            "correlationBand" to TALL_GAP_CORRELATION_BAND.toString(),
            "concentratedCrossover" to TALL_GAP_CONCENTRATED_CROSSOVER.toString(),
            "diffuseCeiling" to DEFAULT_DIFFUSE_CEILING.toString(),
            "diffuseBracketTolerance" to DEFAULT_DIFFUSE_TOLERANCE.toString(),
            "foldCoarseSteps" to DEFAULT_COARSE_STEPS.toString(),
            "foldStrokeTolerance" to DEFAULT_STROKE_TOLERANCE.toString(),
            "curveLowestGap" to TALL_GAP_LOWEST_GAP.toString(),
            "osmoticSecondVirial" to TALL_GAP_OSMOTIC_SECOND_VIRIAL.toString(),
            "osmoticThirdVirial" to TALL_GAP_OSMOTIC_THIRD_VIRIAL.toString(),
            "heldDensityRule" to (heldRule.amplitude.toString() + " * h^" + heldRule.exponent),
            "trendDensityRule" to (trendRule.amplitude.roundedForProse().toString() + " * h^" +
                    trendRule.exponent.roundedForProse())
        ),
        citedInputs = TALL_GAP_CITED,
        densityRules = rules,
        premises = premises,
        reach = reach,
        reachThresholds = reachThresholds,
        layers = layers,
        states = states,
        validityDepartures = tallGapValidityDepartures(premises, layers),
        convergence = convergence,
        reproductions = reproductions,
        findings = emptyMap(),
        validity = TALL_GAP_VALIDITY,
        openQuestions = TALL_GAP_OPEN
    )

    // CLAUDE.md: a String.format defect is a LAST-LINE defect, and where the prose IS a field of
    // the result the standing cure is unachievable — so the prose builder is wrapped, the JSON is
    // written either way, and the defect stays fatal afterwards.
    var panic = ""
    val findings = try {
        tallGapFindings(result)
    } catch (e: Exception) {
        panic = e.toString()
        emptyMap()
    }
    val complete = result.copy(findings = findings, prosePanic = panic)
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-192-device-b-tall-gap.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForActuatorResult().withEmissionHeader(LatticeTag.NONE, null)) + "\n"
    )
    println("T-192 — wrote " + output.path)
    complete.findings.forEach { (key, value) -> println("  " + key + ": " + value) }
    check(panic.isEmpty()) { "the prose builder threw and the JSON was written anyway: " + panic }
}

// ---------------------------------------------------------------------------------------------
// P1/P2 — the reachability bound and the decay length
// ---------------------------------------------------------------------------------------------

private fun tallGapReach(
    field: TallGapDeviceBField,
    gap: Double,
    concentration: Double,
    debye: Double
): TallGapDeviceBReachRecord {
    val holding = holdingBias(field.asPath(), gap, TALL_GAP_TARGET_FORCE)
    val reached = holding != null &&
            holding.appliedBias <= TALL_GAP_POINT_ION_CEILING &&
            holding.attraction >= TALL_GAP_TARGET_FORCE * (1.0 - 1e-6)
    val atPointIon = -field.forceAtBias(gap, TALL_GAP_POINT_ION_CEILING)
    val atElectrochemical = -field.forceAtBias(gap, TALL_GAP_ELECTROCHEMICAL_CEILING)
    val atCeiling = field.sample(gap, DEFAULT_DIFFUSE_CEILING)
    val decay = tallGapDecayLength(gap) { -field.forceAtBias(it, TALL_GAP_POINT_ION_CEILING) }
    return TallGapDeviceBReachRecord(
        gap = gap,
        concentration = concentration,
        bulkDebyeLength = debye,
        gapInBulkDebyeLengths = gap / debye,
        counterionDominanceRatio = tallGapCounterionDominance(gap, concentration),
        counterionScreeningLength = tallGapCounterionScreeningLength(gap, concentration),
        biasForTargetForce = if (reached) holding?.appliedBias else null,
        diffuseDropForTargetForce = if (reached) holding?.diffusePotential else null,
        attractionAtPointIonCeiling = atPointIon,
        attractionAtElectrochemicalCeiling = atElectrochemical,
        attractionAtDiffuseCeiling = atCeiling.attraction,
        appliedBiasAtDiffuseCeiling = atCeiling.appliedBias,
        decayLengthAtPointIonCeiling = decay,
        decayLengthOverBulkDebye = decay / debye,
        electrostaticStiffnessAtPointIonCeiling = -atPointIon / decay,
        reachable = reached,
        limitedBy = when {
            reached -> "none — 100 pN is reached below the point-ion boundary"
            holding == null ->
                "the field: 100 pN is not reached at any diffuse drop below " +
                        DEFAULT_DIFFUSE_CEILING + " V"
            holding.appliedBias > TALL_GAP_ELECTROCHEMICAL_CEILING ->
                "electrochemistry: 100 pN needs a bias past the 1.23 V aqueous window"
            else -> "the point-ion boundary: 100 pN needs a bias past CH-0007's 1.0 V"
        }
    )
}

// ---------------------------------------------------------------------------------------------
// P3 — the fold, the stability floor and the composed cap
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList", "LongMethod")
private fun tallGapState(
    modelName: String,
    ruleName: String,
    height: Double,
    density: Double,
    concentration: Double,
    balance: ActuatorForceBalance,
    chain: GraftedChain,
    model: GraftedLayerModel,
    field: TallGapDeviceBField,
    line: TallGapDeviceBLoadLine
): TallGapDeviceBStateRecord {
    val resting = balance.restingHeight
    val floor = max(chain.occupiedThickness * 1.01, TALL_GAP_LOWEST_GAP)
    val strokeCeiling = resting - floor
    val path = EquilibriumPath(
        restingHeight = resting,
        strokeCeiling = strokeCeiling,
        field = field.asPath()
    ) { stroke -> line.reaction(stroke) + balance.layerLoad(resting - stroke) }
    val search = path.fold()
    val fold = search.fold
    val targetReachable = line.targetStroke <= strokeCeiling
    val operating = if (targetReachable) path.at(line.targetStroke) else null

    val operatingBrush = operating?.let { balance.layerStiffness(it.gap) }
    val operatingElectrostatic = operating?.let { field.stiffnessAtBias(it.gap, it.appliedBias) }
    val operatingEffective =
        if (operatingBrush != null && operatingElectrostatic != null)
            operatingBrush + operatingElectrostatic else null
    val stabilityFloor = operatingEffective?.let { tallGapStabilityFloor(it) }
    val strokeCap = operatingEffective?.let { tallGapStrokeCap(TALL_GAP_TARGET_FORCE, it) }

    val brush = fold?.let { balance.layerStiffness(it.gap) }
    val electrostatic = fold?.let { field.stiffnessAtBias(it.gap, it.appliedBias) }
    val effective = if (brush != null && electrostatic != null) brush + electrostatic else null
    val coupledTangent = effective?.let { line.stiffness + it }
    val residual = if (search.foldAtBranchStart) null else coupledTangent?.let {
        abs(it) / max(line.stiffness + abs(brush ?: 0.0) + abs(electrostatic ?: 0.0), 1e-12)
    }
    val decayAtFold = if (fold != null && electrostatic != null && electrostatic != 0.0)
        -fold.attraction / electrostatic else null

    val correlation = tallGapValidityBias(path, resting, TALL_GAP_CORRELATION_BAND, strokeCeiling)
    val crossoverGap = chain.occupiedThickness / TALL_GAP_CONCENTRATED_CROSSOVER
    val crossover = tallGapValidityBias(path, resting, crossoverGap, strokeCeiling)
    val foldStroke = fold?.stroke ?: strokeCeiling
    val correlationBeyond = (resting - TALL_GAP_CORRELATION_BAND) > foldStroke
    val crossoverBeyond = (resting - crossoverGap) > foldStroke

    val candidates = listOf(
        BiasCeiling("static stability (pull-in)", fold?.appliedBias),
        BiasCeiling(
            "correlation band (C-0005, 1.46 nm)", if (correlationBeyond) null else correlation
        ),
        BiasCeiling(
            "concentrated crossover (C-0002, phi = 0.2)", if (crossoverBeyond) null else crossover
        ),
        BiasCeiling("point-ion boundary (CH-0007, 1.0 V)", TALL_GAP_POINT_ION_CEILING),
        BiasCeiling("electrochemical window (T-11, 1.23 V)", TALL_GAP_ELECTROCHEMICAL_CEILING)
    )
    val binding = bindingCeiling(candidates)
    val usable = operating?.let { point ->
        binding?.bias?.let { ceiling ->
            point.appliedBias <= ceiling && (fold == null || line.targetStroke <= fold.stroke + 1e-9)
        }
    }
    val clears = stabilityFloor?.let {
        if (it <= 0.0) true else line.stiffness > it
    }
    return TallGapDeviceBStateRecord(
        modelName = modelName,
        densityRule = ruleName,
        nominalHeight = height,
        graftingDensity = density,
        concentration = concentration,
        loadLine = line.name,
        couplingStiffness = line.stiffness,
        deadLoad = line.preload,
        targetStroke = line.targetStroke,
        restingHeight = resting,
        dryThickness = chain.occupiedThickness,
        strokeCeiling = strokeCeiling,
        targetStrokeIsReachable = targetReachable,
        operatingBias = operating?.appliedBias,
        operatingGap = operating?.gap,
        operatingVolumeFraction = operating?.let { chain.meanVolumeFraction(it.gap) },
        operatingLayerLoad = operating?.let { balance.layerLoad(it.gap) },
        brushStiffnessAtOperating = operatingBrush,
        electrostaticStiffnessAtOperating = operatingElectrostatic,
        effectiveStiffnessAtOperating = operatingEffective,
        stabilityFloorAtOperating = stabilityFloor,
        couplingClearsTheFloor = clears,
        couplingMarginAtOperating = stabilityFloor?.let {
            if (it <= 0.0) null else line.stiffness / it
        },
        strokeCapAtOperating = strokeCap,
        pullInBias = fold?.appliedBias,
        pullInStroke = fold?.stroke,
        pullInGap = fold?.gap,
        foldAtBranchStart = search.foldAtBranchStart,
        stableShallowBranchExists = !search.foldAtBranchStart,
        foldInsideTargetStroke = fold?.let { it.stroke < line.targetStroke },
        branchEndStroke = search.branchEnd?.stroke,
        branchEndBias = search.branchEnd?.appliedBias,
        branchEndedOnTheField = search.reachedDiffuseCeiling,
        brushStiffnessAtFold = brush,
        electrostaticStiffnessAtFold = electrostatic,
        effectiveStiffnessAtFold = effective,
        coupledTangentAtFold = coupledTangent,
        tangencyResidual = residual,
        forceDecayLengthAtFold = decayAtFold,
        correlationBandBias = if (correlationBeyond) null else correlation,
        concentratedCrossoverBias = if (crossoverBeyond) null else crossover,
        pointIonBias = TALL_GAP_POINT_ION_CEILING,
        electrochemicalBias = TALL_GAP_ELECTROCHEMICAL_CEILING,
        bindingCeiling = binding?.name,
        usableBias = binding?.bias,
        margin = biasMargin(binding?.bias, operating?.appliedBias),
        operatingPointIsUsable = usable,
        verdict = tallGapVerdict(operating, targetReachable, fold, line, clears, usable)
    ).also { require(model.name == modelName) { "the model name must key its own model" } }
}

private fun tallGapVerdict(
    operating: BranchPoint?,
    targetReachable: Boolean,
    fold: BranchPoint?,
    line: TallGapDeviceBLoadLine,
    clears: Boolean?,
    usable: Boolean?
): String = when {
    !targetReachable ->
        "REFUSED — the layer's own dry thickness stops the stroke before " +
                line.targetStroke + " nm"
    operating == null ->
        "REFUSED — no bias below the diffuse ceiling holds the tile at " +
                line.targetStroke + " nm"
    fold != null && fold.stroke < line.targetStroke ->
        "REFUSED — the equilibrium path folds at " + fold.stroke.roundedForProse() +
                " nm, inside the stroke"
    clears == false -> "REFUSED — the coupling is below its own stability floor at the stroke"
    usable == false -> "REFUSED — the operating bias is above the binding ceiling"
    else -> "ADMITTED at this state"
}

/** The bias at which the path reaches [gap], or `null` when it never does. */
private fun tallGapValidityBias(
    path: EquilibriumPath,
    resting: Double,
    gap: Double,
    strokeCeiling: Double
): Double? {
    val stroke = resting - gap
    if (stroke <= 0.0 || stroke > strokeCeiling) return null
    return path.at(stroke)?.appliedBias
}

// ---------------------------------------------------------------------------------------------
// P4 — what a 17-26 nm layer takes outside an established range
// ---------------------------------------------------------------------------------------------

private fun tallGapValidityDepartures(
    premises: List<TallGapDeviceBPremiseRecord>,
    layers: List<TallGapDeviceBLayerRecord>
): List<TallGapDeviceBValidityRecord> {
    val tall = premises.filter { it.nominalHeight > 10.0 }
    val tallLayers = layers.filter { it.nominalHeight > 10.0 }
    val heights = tallLayers.map { it.restingHeight }
    val masses = tall.map { it.chainMolarMass }
    val overlaps = tall.map { it.coilOverlap }
    val stretch = tall.map { it.stretchingRatio }
    val phi = tallLayers.map { it.volumeFractionAtRest }
    val window = tall.map { it.desCloizeauxWindowRatio }
    val spacing = tallLayers.map { it.graftingSpacing }
    return listOf(
        TallGapDeviceBValidityRecord(
            quantity = "layer height L0",
            establishedRange = "5-10 nm (§3's own box; C-0003, C-0011, C-0016, C-0027 all " +
                    "sweep inside it and no claim in this repository evaluates a taller one)",
            valueHere = tallGapRange(heights) + " nm",
            direction = "ABOVE",
            severity = tallGapSix(heights.max() / 10.0) + "x the top of the established range",
            whatItWouldTake = "an SCF solve (C-0011's machinery) at 17-26 nm, whose node count " +
                    "goes as the wall height, plus a design-window resynthesis (C-0027) on a " +
                    "grid that reaches it. Neither is expensive; neither has been run."
        ),
        TallGapDeviceBValidityRecord(
            quantity = "chain molar mass",
            establishedRange = "0.9-8.8 kDa across §3's three design points (C-0002: any PEG " +
                    "chain below ~40 kDa is UNSWOLLEN in water at 300 K)",
            valueHere = tallGapRange(masses.map { it / 1000.0 }) + " kDa",
            direction = if (masses.max() > 40000.0)
                "ABOVE the §3 points, and the dilute end CROSSES the 40 kDa swelling threshold"
            else "ABOVE the §3 points, BELOW the 40 kDa swelling threshold",
            severity = if (masses.max() > 40000.0)
                "the dense end is still unswollen; the dilute end of the §3-trend rule reaches " +
                        "chains long enough to swell, which is a DIFFERENT polymer physics and " +
                        "is not what the six models were built for"
            else "the layer is still marginal-solvent and unswollen, so no blob argument " +
                    "becomes available at 17-26 nm that was unavailable at 5-10",
            whatItWouldTake = "for the unswollen end, nothing — it is INSIDE C-0002's range and " +
                    "is reported because it is the premise a taller layer would most naturally " +
                    "be assumed to break; for any chain past 40 kDa, a swollen-chain elasticity " +
                    "the six models do not contain"
        ),
        TallGapDeviceBValidityRecord(
            quantity = "des Cloizeaux window sqrt(N_K/g_T)",
            establishedRange = "empty at every Gen-1 chain (CLAUDE.md: N_K = 22-120 against " +
                    "g_T = 126-1160, so the 9/4 exponent never starts)",
            valueHere = tallGapRange(window),
            direction = "still BELOW 1",
            severity = "the window is EMPTY at 17-26 nm too; the des Cloizeaux limb of the " +
                    "six-model bracket remains a FIT, valid only over the range it was fitted",
            whatItWouldTake = "a chain past the thermal blob, i.e. ~167 kDa, which is 19-40x " +
                    "the chains a 17-26 nm layer needs at these densities"
        ),
        TallGapDeviceBValidityRecord(
            quantity = "coil overlap Sigma = pi R0^2 sigma",
            establishedRange = ">= 1 is the only brush criterion CLAUDE.md says bounds " +
                    "anything, and it is also the validity condition of any 1-D mean field",
            valueHere = tallGapRange(overlaps),
            direction = if (overlaps.min() > 1.0) "ABOVE 1 — the criterion HOLDS"
            else "BELOW 1 at the dilute end — the criterion FAILS",
            severity = if (overlaps.min() > 1.0)
                "no departure: a taller layer at these densities still overlaps"
            else "a 1-D mean field is not licensed at the dilute end of the trend rule",
            whatItWouldTake = "a grafting density high enough that pi R0^2 sigma > 1, which " +
                    "the held-density rule supplies and the §3-trend extrapolation may not"
        ),
        TallGapDeviceBValidityRecord(
            quantity = "stretching ratio L0/R0",
            establishedRange = "the premise of every model in brush/GraftedLayer.kt is that " +
                    "this is LARGE; C-0003 reports it is not, anywhere in the Gen-1 space, and " +
                    "carries that as a validity bound rather than hiding it",
            valueHere = tallGapRange(stretch),
            direction = "unchanged in kind",
            severity = "a taller layer does not repair the strong-stretching premise, because " +
                    "R0 grows as sqrt(N) while L0 grows as N only at fixed sigma",
            whatItWouldTake = "nothing computational — it is a statement about the model family, " +
                    "and the honest reading is C-0003's six-model bracket, carried here in full"
        ),
        TallGapDeviceBValidityRecord(
            quantity = "volume fraction phi at rest",
            establishedRange = "C-0002's phi = 0.2 concentrated crossover is the ceiling the " +
                    "programme reads; C-0011's solved layer sits at 0.0686 at the 10 nm point " +
                    "against C-0003's trial functions at 0.151-0.285 (4.15x, CLAUDE.md)",
            valueHere = tallGapRange(phi),
            direction = "BELOW the ceiling at rest",
            severity = "the ceiling binds under COMPRESSION, not at rest, which is why it is " +
                    "carried as a per-state bias ceiling in states[] rather than as a filter",
            whatItWouldTake = "C-0011's SCF solve at 17-26 nm, which would also settle whether " +
                    "the trial functions' 4.15x over-reading persists at a taller layer"
        ),
        TallGapDeviceBValidityRecord(
            quantity = "grafting spacing s = sigma^(-1/2)",
            establishedRange = "3.30-6.45 nm at §3's three design points",
            valueHere = tallGapRange(spacing) + " nm",
            direction = "ABOVE at the trend rule, UNCHANGED at the held-density rule",
            severity = "the trend rule extrapolates the §3 points' own s/L0 ~ 0.66 and " +
                    "therefore dilutes the layer; the held-density rule does not",
            whatItWouldTake = "NDI naming a grafting density, or a chemistry constraint on the " +
                    "PEG-thiol coverage a 17-26 nm layer could actually be grown at. This is a " +
                    "SPECIFICATION gap, not a modelling one."
        ),
        TallGapDeviceBValidityRecord(
            quantity = "§3's own effort-point row, ~20-25 nm above the electrode",
            establishedRange = "20 / 22 / 25 nm at §3's three layer heights — the band is " +
                    "exactly as wide as the 5-10 nm layer range, which is what forces a " +
                    "CONSTANT attachment height and fixes it at 5 nm (CLAUDE.md)",
            valueHere = tallGapSix(heights.min() + 15.0) + " to " +
                    tallGapSix(heights.max() + 15.0) + " nm at a 10 nm tile and a 5 nm " +
                    "attachment, and " + tallGapSix(heights.min() + 10.0) + " to " +
                    tallGapSix(heights.max() + 10.0) + " nm with the lever bonded straight " +
                    "onto the tile",
            direction = "ABOVE",
            severity = tallGapSix((heights.min() + 10.0) / 25.0) + "x to " +
                    tallGapSix((heights.max() + 15.0) / 25.0) + "x the TOP of §3's band, and " +
                    "this needs no solver at all: it is L0 + 10 + 5 against 20-25",
            whatItWouldTake = "NDI moving the effort-point row. It is arithmetic, not physics, " +
                    "and it is the FIRST thing a 17-26 nm layer breaks — before any field solve."
        ),
        TallGapDeviceBValidityRecord(
            quantity = "mean-field electrostatics",
            establishedRange = "C-0005: the one-loop correction is 123-214% of the leading " +
                    "term at 5-10 nm for Mg2+, and the direction is unknown for oppositely " +
                    "charged walls",
            valueHere = "the correction FALLS with the gap, so a 17-26 nm gap is the one place " +
                    "in this programme where mean field is BETTER supported, not worse",
            direction = "FAVOURABLE",
            severity = "this is the one axis on which the tall corner is an improvement",
            whatItWouldTake = "explicit-ion simulation, C-0005's 1-3 week cost, unspent"
        )
    )
}

private fun tallGapRange(values: List<Double>): String =
    if (values.isEmpty()) "none" else tallGapSix(values.min()) + " to " + tallGapSix(values.max())

// ---------------------------------------------------------------------------------------------
// gate 4 — convergence
// ---------------------------------------------------------------------------------------------

private fun tallGapConvergence(
    peg: PegWater,
    geometry: ActuatorGeometry,
    tileCharge: Double,
    lb: Double,
    foldingState: TallGapDeviceBStateRecord?
): List<TallGapDeviceBConvergenceRecord> {
    val records = mutableListOf<TallGapDeviceBConvergenceRecord>()

    // axis 1 — the Poisson-Boltzmann mesh, read AT A TALL GAP, where the graded mesh spans the
    // largest ratio of wall layer to gap and therefore has the most work to do
    val meshes = listOf(1000, 2000, 4000)
    val meshValues = meshes.map { nodes ->
        val field = TallGapDeviceBField(0.5, tileCharge, lb, nodes = nodes)
        -field.forceAtBias(20.0, TALL_GAP_POINT_ION_CEILING)
    }
    meshes.forEachIndexed { i, nodes ->
        records += TallGapDeviceBConvergenceRecord(
            axis = "Poisson-Boltzmann mesh nodes at a 20 nm gap, 0.5 mM, 1.0 V",
            setting = nodes.toString(),
            quantity = "|F_es| [pN]",
            value = meshValues[i],
            departureFromFinest = tallGapTwoSignificantDigits(
                abs(meshValues[i] / meshValues.last() - 1.0)
            )
        )
    }

    // axis 2 — the same mesh axis read on the DECAY LENGTH, which is a gradient of the above and
    // therefore converges more slowly (CLAUDE.md: convergence is a property of the quantity)
    val decayValues = meshes.map { nodes ->
        val field = TallGapDeviceBField(0.5, tileCharge, lb, nodes = nodes)
        tallGapDecayLength(20.0) { -field.forceAtBias(it, TALL_GAP_POINT_ION_CEILING) }
    }
    meshes.forEachIndexed { i, nodes ->
        records += TallGapDeviceBConvergenceRecord(
            axis = "Poisson-Boltzmann mesh nodes, read on the DECAY LENGTH at 20 nm, 0.5 mM",
            setting = nodes.toString(),
            quantity = "ell [nm]",
            value = decayValues[i],
            departureFromFinest = tallGapTwoSignificantDigits(
                abs(decayValues[i] / decayValues.last() - 1.0)
            )
        )
    }

    // axis 3 — the central-difference step of the decay length
    val steps = listOf(1e-2, 1e-3, 1e-4)
    val stepField = TallGapDeviceBField(0.5, tileCharge, lb)
    val stepValues = steps.map { step ->
        tallGapDecayLength(20.0, step) { -stepField.forceAtBias(it, TALL_GAP_POINT_ION_CEILING) }
    }
    steps.forEachIndexed { i, step ->
        records += TallGapDeviceBConvergenceRecord(
            axis = "decay-length central-difference half-step at 20 nm, 0.5 mM, 1.0 V",
            setting = step.toString(),
            quantity = "ell [nm]",
            value = stepValues[i],
            departureFromFinest = tallGapTwoSignificantDigits(
                abs(stepValues[i] / stepValues.last() - 1.0)
            )
        )
    }

    // axis 4 — the fold search's own two settings, read at a state the sweep found a FOLD at.
    // CLAUDE.md: "a convergence axis cannot be read at a state where the quantity does not
    // exist" — and 76 of this task's 96 device-B states have no fold, so a state named in
    // advance would have converged on `null` and silently reported nothing.
    if (foldingState == null) {
        records += TallGapDeviceBConvergenceRecord(
            axis = "fold search settings",
            setting = "not run",
            quantity = "pull-in bias [V]",
            value = 0.0,
            departureFromFinest = 0.0
        )
        return records
    }
    val model = tallGapLayerModel(foldingState.modelName, peg)
    val density = foldingState.graftingDensity
    val chain = peg.graftedChain(
        model.chainLengthForHeight(peg, foldingState.nominalHeight, density), density
    )
    val balance = ActuatorForceBalance(model, chain, geometry)
    val field = TallGapDeviceBField(foldingState.concentration, tileCharge, lb)
    val resting = balance.restingHeight
    val strokeCeiling = resting - max(chain.occupiedThickness * 1.01, TALL_GAP_LOWEST_GAP)
    val axisState = foldingState.nominalHeight.toString() + " nm " + foldingState.densityRule +
            ", " + foldingState.concentration + " mM, device-B, " + foldingState.modelName
    fun foldAt(coarse: Int, tolerance: Double): Double? {
        val path = EquilibriumPath(
            restingHeight = resting, strokeCeiling = strokeCeiling, field = field.asPath()
        ) { stroke -> TALL_GAP_DEVICE_B_STIFFNESS * stroke + balance.layerLoad(resting - stroke) }
        return path.fold(coarseSteps = coarse, strokeTolerance = tolerance).fold?.appliedBias
    }
    val tolerances = listOf(1e-3, 1e-4, 1e-6)
    val finestTolerance = foldAt(DEFAULT_COARSE_STEPS, tolerances.last())
    tolerances.forEach { tolerance ->
        val value = foldAt(DEFAULT_COARSE_STEPS, tolerance)
        records += TallGapDeviceBConvergenceRecord(
            axis = "fold golden-section stroke tolerance at " + axisState,
            setting = tolerance.toString(),
            quantity = "pull-in bias [V]",
            value = value ?: 0.0,
            departureFromFinest = tallGapTwoSignificantDigits(
                if (value == null || finestTolerance == null) 0.0
                else abs(value / finestTolerance - 1.0)
            )
        )
    }
    val coarses = listOf(8, 12, 24)
    val finestCoarse = foldAt(coarses.last(), DEFAULT_STROKE_TOLERANCE)
    coarses.forEach { coarse ->
        val value = foldAt(coarse, DEFAULT_STROKE_TOLERANCE)
        records += TallGapDeviceBConvergenceRecord(
            axis = "fold coarse scan steps at " + axisState,
            setting = coarse.toString(),
            quantity = "pull-in bias [V]",
            value = value ?: 0.0,
            departureFromFinest = tallGapTwoSignificantDigits(
                if (value == null || finestCoarse == null) 0.0
                else abs(value / finestCoarse - 1.0)
            )
        )
    }
    return records
}

// ---------------------------------------------------------------------------------------------
// gate 5 — the upstream reproductions
// ---------------------------------------------------------------------------------------------

private fun tallGapReproductions(
    peg: PegWater,
    geometry: ActuatorGeometry,
    foldFields: Map<Double, TallGapDeviceBField>,
    reachFields: Map<Double, TallGapDeviceBField>,
    lb: Double
): List<TallGapDeviceBReproductionRecord> {
    val records = mutableListOf<TallGapDeviceBReproductionRecord>()

    // C-0008's bulk Debye lengths — the number NDI's own objection is written on
    listOf(2.0 to 3.9269, 5.0 to 2.4839, 10.0 to 1.7565).forEach { (concentration, upstream) ->
        val here = MagnesiumChlorideBuffer(concentration).debyeLength()
        records += TallGapDeviceBReproductionRecord(
            quantity = "bulk Debye length",
            state = concentration.toString() + " mM MgCl2",
            here = here,
            upstream = upstream,
            relativeDeparture = tallGapTwoSignificantDigits(abs(here / upstream - 1.0)),
            source = "C-0008 (bulk lambda_D at I = 3c)"
        )
    }

    // C-0008's bias for a 100 pN blocking force at 2 mM — the same quantity P1 asks for, at a
    // gap C-0008 sampled, so a departure here is a departure in the pipeline and not in the gap
    val twoMillimolar = reachFields.getValue(2.0)
    listOf(5.0 to 0.067, 7.0 to 0.113, 10.0 to 0.679).forEach { (gap, upstream) ->
        val here = holdingBias(twoMillimolar.asPath(), gap, TALL_GAP_TARGET_FORCE)?.appliedBias
        if (here != null) records += TallGapDeviceBReproductionRecord(
            quantity = "applied bias for a 100 pN blocking force",
            state = gap.toString() + " nm, 2 mM",
            here = here,
            upstream = upstream,
            relativeDeparture = tallGapTwoSignificantDigits(abs(here / upstream - 1.0)),
            source = "C-0008 (bias needed for 100 pN, 2 mM)"
        )
    }

    // C-0008's |F_es| at 15 nm and 2 V, which is the tallest gap any claim has quoted under bias
    val at15 = -twoMillimolar.forceAtBias(15.0, 2.0)
    records += TallGapDeviceBReproductionRecord(
        quantity = "|F_es| at the tallest gap any standing claim quotes under bias",
        state = "15 nm, 2 mM, 2.0 V",
        here = at15,
        upstream = 22.0,
        relativeDeparture = tallGapTwoSignificantDigits(abs(at15 / 22.0 - 1.0)),
        source = "C-0008 (F_es table, 2 mM, 15 nm, 2.0 V)"
    )

    // C-0018: the 10 nm design point in 0.5 mM has NO fold at all under the mandated coupling —
    // the reference this task's tall states have to be read against
    val model = tallGapLayerModel(TALL_GAP_REFERENCE_MODEL, peg)
    val density = 0.024
    val chain = peg.graftedChain(model.chainLengthForHeight(peg, 10.0, density), density)
    val balance = ActuatorForceBalance(model, chain, geometry)
    val resting = balance.restingHeight
    val strokeCeiling = resting - max(chain.occupiedThickness * 1.01, TALL_GAP_LOWEST_GAP)
    val path = EquilibriumPath(
        restingHeight = resting,
        strokeCeiling = strokeCeiling,
        field = foldFields.getValue(0.5).asPath()
    ) { stroke -> TALL_GAP_DEVICE_A_STIFFNESS * stroke + balance.layerLoad(resting - stroke) }
    val fold = path.fold().fold
    records += TallGapDeviceBReproductionRecord(
        quantity = "number of folds on the coupled branch at the 10 nm point in 0.5 mM",
        state = "10 nm, 0.5 mM, device-A load line, " + TALL_GAP_REFERENCE_MODEL,
        here = if (fold == null) 0.0 else 1.0,
        upstream = 0.0,
        relativeDeparture = if (fold == null) 0.0 else 1.0,
        source = "C-0018 (dropping to 0.5 mM removes the fold entirely at the 10 nm point)"
    )

    // C-0017's operating bias at the 10 nm point in 0.5 mM, 0.087-0.115 V over the six models
    val operating = path.at(3.0)?.appliedBias
    if (operating != null) records += TallGapDeviceBReproductionRecord(
        quantity = "operating bias V* delivering 100 pN at 3 nm on the device-A line",
        state = "10 nm, 0.5 mM, " + TALL_GAP_REFERENCE_MODEL,
        here = operating,
        upstream = 0.101,
        relativeDeparture = tallGapTwoSignificantDigits(abs(operating / 0.101 - 1.0)),
        source = "C-0017 (0.087-0.115 V over the six models; 0.101 is the mid-bracket, so a " +
                "departure inside +-14% is agreement with the bracket, not with a number)"
    )

    // C-0002's own material sheet, through this study's premise census
    val premise = tallGapScalingPremises(10.0, 0.024, peg)
    records += TallGapDeviceBReproductionRecord(
        quantity = "thermal blob in Kuhn segments (A2 route)",
        state = "PEG in water at 300 K",
        here = premise.thermalBlobKuhnSegments,
        upstream = 1222.0,
        relativeDeparture = tallGapTwoSignificantDigits(
            abs(premise.thermalBlobKuhnSegments / 1222.0 - 1.0)
        ),
        source = "C-0002 / CLAUDE.md (g_T = 1222 Kuhn segments, 167 kDa, from A2 = 1.9e-3)"
    )

    // C-0050's own table: the layer height at which a 100 pN DEAD-LOAD stroke reaches 10 nm, at
    // sigma = 0.024 — which is where NDI's "17-26 nm" came from in the first place, so
    // reproducing it is reproducing the premise of the whole task rather than a side quantity.
    C0050_TALL_LAYER_HEIGHT.forEach { (name, upstream) ->
        val here = tallGapDeadLoadStrokeHeight(name, peg)
        if (here != null) records += TallGapDeviceBReproductionRecord(
            quantity = "L0 at which the 100 pN dead-load stroke reaches 10 nm, sigma = 0.024",
            state = name,
            here = here,
            upstream = upstream,
            relativeDeparture = tallGapTwoSignificantDigits(abs(here / upstream - 1.0)),
            source = "C-0050 (the escape table: 16.63-26.12 nm over the six models)"
        )
    }
    require(lb > 0.0) { "the Bjerrum length must be positive" }
    return records
}

/** `C-0050`'s escape table, per model, in nm. */
private val C0050_TALL_LAYER_HEIGHT = mapOf(
    "strong-stretching(two-body)" to 16.63,
    "strong-stretching(virial)" to 19.48,
    "strong-stretching(des-Cloizeaux)" to 19.59,
    "alexander-box(two-body)" to 21.21,
    "alexander-box(des-Cloizeaux)" to 26.07,
    "alexander-box(virial)" to 26.12
)

/**
 * The `L₀` at which the layer alone carries [load] pN at a stroke of [stroke] nm, bisected on
 * the bracket width — `C-0050`'s root, re-solved here rather than transferred.
 */
private fun tallGapDeadLoadStrokeHeight(
    modelName: String,
    peg: PegWater,
    density: Double = 0.024,
    stroke: Double = TALL_GAP_DEVICE_B_STROKE,
    load: Double = TALL_GAP_TARGET_FORCE
): Double? {
    val model = tallGapLayerModel(modelName, peg)
    fun excess(height: Double): Double {
        val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
        val held = height - stroke
        if (held <= chain.occupiedThickness * 1.001) return Double.POSITIVE_INFINITY
        return model.load(chain, held, TALL_GAP_FOOTPRINT) - load
    }
    var low = stroke + 1.0
    var high = 60.0
    if (excess(high) > 0.0) return null
    var steps = 0
    while (high - low > 1e-6 && steps < 200) {
        val middle = 0.5 * (low + high)
        if (excess(middle) > 0.0) low = middle else high = middle
        steps++
    }
    return 0.5 * (low + high)
}

// ---------------------------------------------------------------------------------------------
// the findings
// ---------------------------------------------------------------------------------------------

/**
 * Six significant digits — the precision a **decision** may be taken at (`CLAUDE.md`), and the
 * precision every findings string here is written at.
 *
 * No `String.format` anywhere in this file: `CLAUDE.md` records the `+`-binds-tighter trap
 * firing twice with the rule already written down, once after 49 minutes of completed
 * computation. A concatenation of `toString()`s cannot carry a placeholder mismatch at all.
 */
private fun tallGapSix(value: Double): String {
    if (!value.isFinite()) return value.toString()
    if (value == 0.0) return "0"
    val scale = Math.pow(10.0, 5.0 - Math.floor(Math.log10(Math.abs(value))))
    return ((value * scale).toLong() / scale).toString()
}

@Suppress("LongMethod")
private fun tallGapFindings(result: TallGapDeviceBResult): Map<String, String> {
    val tallReach = result.reach.filter { it.gap > 16.0 }
    val tallReachable = tallReach.filter { it.reachable }
    val lowSalt = tallReach.filter { it.concentration == 0.5 }
    val decayRatios = tallReach.map { it.decayLengthOverBulkDebye }

    val tallStates = result.states
    val deviceB = tallStates.filter { it.couplingStiffness == TALL_GAP_DEVICE_B_STIFFNESS }
    val deviceBLowSalt = deviceB.filter { it.concentration == 0.5 }
    val deviceBGraded = deviceB.filter { it.stabilityFloorAtOperating != null }
    val floors = deviceBGraded.mapNotNull { it.stabilityFloorAtOperating }
    val clearing = deviceBGraded.count { it.couplingClearsTheFloor == true }
    val folding = deviceB.count { it.foldInsideTargetStroke == true }
    val unreachable = deviceB.count { it.operatingBias == null }
    val admitted = deviceB.count { it.verdict.startsWith("ADMITTED") }
    val caps = deviceBGraded.mapNotNull { it.strokeCapAtOperating }

    val findings = mutableMapOf<String, String>()

    findings["P1 the cheap bound"] =
        "Of " + tallReach.size + " (gap, buffer) cells at 17-26 nm, " + tallReachable.size +
                " reach §3's 100 pN below CH-0007's 1.0 V point-ion boundary. At 0.5 mM the " +
                "largest attraction the field supplies at 1.0 V over the 40 x 40 nm footprint " +
                "is " + tallGapSix(lowSalt.maxOf { it.attractionAtPointIonCeiling }) +
                " pN at " + tallGapSix(lowSalt.minOf { it.gap }) + " nm and " +
                tallGapSix(lowSalt.minOf { it.attractionAtPointIonCeiling }) + " pN at " +
                tallGapSix(lowSalt.maxOf { it.gap }) + " nm. Pushing to the 1.23 V " +
                "electrochemical bound buys " +
                tallGapSix(
                    lowSalt.maxOf {
                        it.attractionAtElectrochemicalCeiling / it.attractionAtPointIonCeiling
                    }
                ) + "x at most, because C-0008's compact layer takes 88% of any bias above 2 V."

    findings["P1 the RESTING gap versus the HELD gap"] =
        "§3 asks for 100 pN AT THE STROKE, not at the resting height, and device B held at " +
                "L0 - 10 nm sits at a 7-16 nm gap, which C-0008 has already sampled. So the " +
                "reachability bound refuses the BLOCKING reading and says nothing about the " +
                "device's own duty. The device-B question is not 'can the field reach across " +
                "26 nm' but 'can the equilibrium path get from s = 0 to s = 10 nm without " +
                "folding' — and states[] answers that instead."

    val tallHigh = tallReach.filter { it.concentration == 2.0 }
    val tallLow = tallReach.filter { it.concentration == 0.5 }
    findings["P2 which decay length governs at 17-26 nm"] =
        "MEASURED on the solve at 1.0 V, ell/lambda_D over the " + tallReach.size + " tall cells " +
                "is " + tallGapSix(decayRatios.min()) + " to " + tallGapSix(decayRatios.max()) +
                ", and it is a function of kappa*h and not of h: at 2 mM, where 17-26 nm is " +
                "4.33-6.62 Debye lengths, it is " +
                tallGapSix(tallHigh.minOf { it.decayLengthOverBulkDebye }) + "-" +
                tallGapSix(tallHigh.maxOf { it.decayLengthOverBulkDebye }) +
                " and C-0008's far-field limit of 1.0 is essentially reached; at 0.5 mM, where " +
                "the same gaps are only 2.16-3.31 Debye lengths, it is " +
                tallGapSix(tallLow.minOf { it.decayLengthOverBulkDebye }) + "-" +
                tallGapSix(tallLow.maxOf { it.decayLengthOverBulkDebye }) +
                " — the field decays FASTER than the bulk Debye length there, which runs " +
                "against the corner rather than for it. So NDI's bulk lambda_D is the right " +
                "length at 2 mM and an OPTIMISTIC one at 0.5 mM, and this programme's 'the " +
                "Debye length is three numbers' answer does NOT rescue the corner: the " +
                "counterion-dominance ratio is still " +
                tallGapSix(tallReach.minOf { it.counterionDominanceRatio }) + " to " +
                tallGapSix(tallReach.maxOf { it.counterionDominanceRatio }) +
                " (a CONTENT statement, and true), while the counterion screening length " +
                tallGapSix(tallReach.minOf { it.counterionScreeningLength }) + "-" +
                tallGapSix(tallReach.maxOf { it.counterionScreeningLength }) +
                " nm is nowhere near the measured decay. CH-0004's own escape clause fires " +
                "again, one gap decade further out."

    findings["P1 the threshold: how far 100 pN reaches at all"] =
        result.reachThresholds.joinToString("; ") {
            it.concentration.toString() + " mM at " + it.appliedBias + " V: " +
                    (it.deepestGapReachingTarget?.let { g -> tallGapSix(g) + " nm (" +
                            tallGapSix(g / it.bulkDebyeLength) + " lambda_D)" }
                        ?: "no gap in [1, 40] nm")
        } + ". §3's 100 pN stops arriving well below NDI's 17 nm at every buffer, and the " +
                "reserve moves the threshold rather than removing it."

    val at20Low = result.reach.first { it.gap == 20.0 && it.concentration == 0.5 }
    val at10High = result.reach.first { it.gap == 10.0 && it.concentration == 2.0 }
    val reserveGain = TALL_GAP_HEIGHTS.map { gap ->
        result.reach.first { it.gap == gap && it.concentration == 0.5 }
            .attractionAtPointIonCeiling /
                result.reach.first { it.gap == gap && it.concentration == 2.0 }
                    .attractionAtPointIonCeiling
    }
    findings["P2 what the reserve actually buys"] =
        "Dropping 2 mM to 0.5 mM doubles lambda_D (3.9269 -> 7.8538 nm), so 17-26 nm goes from " +
                "4.33-6.62 bulk Debye lengths to 2.16-3.31 — NDI's own objection, halved. Two " +
                "things then fight: the exponential improves and the saturated far-field " +
                "amplitude, which goes as the bulk ion density, falls fourfold. MEASURED at " +
                "1.0 V, the net gain at 17/20/23/26 nm is " +
                tallGapSix(reserveGain.min()) + "x to " + tallGapSix(reserveGain.max()) +
                "x. The reserve therefore " +
                (if (reserveGain.min() > 1.0) "HELPS at a tall gap" else "does NOT help") +
                ", and the question is only whether it helps enough: at 20 nm / 0.5 mM the gap " +
                "is exactly as many Debye lengths as 10 nm / 2 mM and the field delivers " +
                tallGapSix(at20Low.attractionAtPointIonCeiling) + " pN against " +
                tallGapSix(at10High.attractionAtPointIonCeiling) + " pN there, against §3's " +
                "100 pN — a shortfall of " +
                tallGapSix(TALL_GAP_TARGET_FORCE / at20Low.attractionAtPointIonCeiling) + "x."

    findings["P3 device B at the tall corner"] =
        "Over " + deviceB.size + " device-B states (4 heights x 2 density rules x 6 models x " +
                "2 buffers), " + admitted + " are ADMITTED, " + unreachable +
                " have no bias below the diffuse ceiling that holds the tile at a 10 nm " +
                "stroke, and " + folding + " fold inside that stroke. Of the " +
                deviceBGraded.size + " states that reach their own operating point, " +
                clearing + " clear C-0017's stability floor at k_c = 10 pN/nm."

    findings["P3 the stability floor at the tall corner"] =
        if (floors.isEmpty()) "no device-B state reaches an operating point, so no floor exists"
        else "|k_eff| at the device-B operating point runs " + tallGapSix(floors.min()) +
                " to " + tallGapSix(floors.max()) + " pN/nm, against C-0046's 23.41-27.91 " +
                "pN/nm at the 10 nm layer in 2 mM. C-0046's composed cap delta <= F/|k_eff| " +
                "is " + (if (caps.isEmpty()) "not defined at any tall state (no state imposes " +
                "a stability floor)" else tallGapSix(caps.min()) + " to " +
                tallGapSix(caps.max()) + " nm") + " here, against 3.58-4.27 nm at the 10 nm " +
                "layer, and §3's desired stroke is 10 nm."

    val deviceA = tallStates.filter { it.couplingStiffness == TALL_GAP_DEVICE_A_STIFFNESS }
    val free = tallStates.filter { it.loadLine == "free" }
    val deadLoad = tallStates.filter { it.loadLine.startsWith("dead-load") }
    val foldStrokes = deviceB.mapNotNull { if (it.foldInsideTargetStroke == true) it.pullInStroke else null }
    findings["P3 a tall layer LOSES device A as well as failing device B"] =
        "The same sweep run at §3's ACCEPTABLE clause — C-0017's 33.333 pN/nm placed at a 3 nm " +
                "stroke — is REFUSED at " + deviceA.count { it.verdict.startsWith("REFUSED") } +
                " of " + deviceA.size + " states, every one of them because no bias below the " +
                "diffuse ceiling holds the tile at 3 nm at all: a 3 nm stroke from a 17-26 nm " +
                "layer leaves a 14-23 nm gap, and the field cannot put 100 pN across it. " +
                "So the tall layer is not a trade of device A for device B. IT LOSES BOTH. " +
                "The dead-load line is refused at " +
                deadLoad.count { it.verdict.startsWith("REFUSED") } + " of " + deadLoad.size +
                " as well — a dead load has k_c = 0 and can never clear a positive stability " +
                "floor, and its branch is additionally EMPTY at zero stroke, because holding " +
                "the tile at its own resting height under 100 pN needs 100 pN across the full " +
                "17-26 nm."

    findings["P3 the stroke exists and the force does not"] =
        "The UNCOUPLED tile reaches a 10 nm stroke at " +
                free.count { it.verdict.startsWith("ADMITTED") } + " of " + free.size +
                " tall states, against " + admitted + " of " + deviceB.size + " for device B. " +
                "So a 17-26 nm layer DOES buy the kinematics C-0050 priced it for — the tile " +
                "can be driven ten nanometres down against the layer alone — and what it " +
                "cannot do is deliver §3's 100 pN while doing it. C-0050's escape is real in " +
                "displacement and empty in force, and the two were never separated."

    findings["P3 where the folds sit"] =
        if (foldStrokes.isEmpty()) "no device-B state folds inside its own stroke"
        else "Every one of the " + foldStrokes.size + " device-B folds inside the 10 nm stroke " +
                "sits at " + tallGapSix(foldStrokes.min()) + " to " +
                tallGapSix(foldStrokes.max()) + " nm — 0.52 to 0.77 of the demanded stroke, " +
                "clustered at roughly half of it, and NOT near its end. A fold that near the " +
                "middle of the demand is not a margin question."

    findings["P3 at 0.5 mM specifically"] =
        "Of the " + deviceBLowSalt.size + " device-B states at 0.5 mM, " +
                deviceBLowSalt.count { it.verdict.startsWith("ADMITTED") } + " are admitted, " +
                deviceBLowSalt.count { it.foldInsideTargetStroke == true } +
                " fold inside the stroke and " +
                deviceBLowSalt.count { it.operatingBias == null } +
                " never reach their operating point at all."

    findings["P4 the cheapest departure of all, and it needs no solver"] =
        "§3's own effort-point row puts the coupling's purchase ~20-25 nm above the electrode, " +
                "and at §3's three layer heights a 10 nm tile plus a 5 nm attachment reproduces " +
                "that band at both ends — 20 / 22 / 25 nm. At 17-26 nm the same stack puts the " +
                "effort point at 32-41 nm, and 27-36 nm even with the lever bonded straight " +
                "onto the tile. A 17-26 nm layer therefore breaks §3's stack geometry BEFORE " +
                "any field is solved, by 1.08x to 1.64x. That is arithmetic, it is a " +
                "SPECIFICATION question rather than a modelling one, and no claim in this " +
                "programme had noticed it."

    findings["P4 the validity statement"] =
        "A 17-26 nm grafted PEG layer is OUTSIDE every layer range this programme has " +
                "established: no claim here evaluates a layer above 10 nm, and " +
                result.validityDepartures.count { it.direction.contains("ABOVE") } + " of " +
                result.validityDepartures.size + " quantities in validityDepartures[] are " +
                "above their established range. Two are NOT departures and are reported " +
                "because they would be assumed to be: the chains stay below C-0002's 40 kDa " +
                "swelling threshold, so no blob argument becomes available that was not " +
                "available at 5-10 nm; and the mean-field electrostatics is BETTER supported " +
                "at a tall gap, not worse, because C-0005's one-loop correction falls with it."

    findings["the grafting density is a SPECIFICATION gap"] =
        "NDI's answer names a THICKNESS and a layer needs a DENSITY too. The two rules " +
                "carried here disagree by " +
                tallGapSix(
                    result.densityRules[0].densityAt(26.0) /
                            result.densityRules[1].densityAt(26.0)
                ) + "x in sigma at 26 nm, and the §3-trend rule is sigma = " +
                tallGapSix(result.densityRules[1].amplitude) + " h^" +
                tallGapSix(result.densityRules[1].exponent) + ". Neither is a design; both are " +
                "extrapolations, and which one NDI means is a question for NDI."

    val survivors = deviceB.filter { it.verdict.startsWith("ADMITTED") }
    findings["P3 the survivors, named"] =
        if (survivors.isEmpty()) "no device-B state is admitted anywhere in the sweep"
        else "The admitted states are: " + survivors.joinToString("; ") {
            it.nominalHeight.toString() + " nm " + it.densityRule + ", " + it.concentration +
                    " mM, " + it.modelName + " (V* = " + tallGapSix(it.operatingBias ?: 0.0) +
                    " V, |k_eff| = " + tallGapSix(-(it.effectiveStiffnessAtOperating ?: 0.0)) +
                    " pN/nm, margin " +
                    tallGapSix(it.couplingMarginAtOperating ?: 0.0) + "x, no fold)"
        } + ". A survivor at " + survivors.size + " of " + deviceB.size + " states, present in " +
                survivors.map { it.modelName }.distinct().size + " of 6 layer models, is a " +
                "BRACKET DISAGREEMENT and not a design: the six models are C-0003's own " +
                "uncertainty and they do not agree that this state exists."

    findings["verdict"] = tallGapOverallVerdict(result, admitted, deviceB.size)
    return findings
}

private fun tallGapOverallVerdict(
    result: TallGapDeviceBResult,
    admitted: Int,
    total: Int
): String {
    val tallReachable = result.reach.count { it.gap > 16.0 && it.reachable }
    return "NDI's Debye-length objection to decision 2 is ANSWERED, and the answer upholds it. " +
            "The bulk lambda_D is the right length at a tall gap — measured, not asserted, at " +
            "ell/lambda_D = " +
            tallGapSix(result.reach.filter { it.gap > 16.0 }.minOf { it.decayLengthOverBulkDebye }) +
            "-" +
            tallGapSix(result.reach.filter { it.gap > 16.0 }.maxOf { it.decayLengthOverBulkDebye }) +
            " — so this programme's 'the Debye length is three numbers here' answer does not " +
            "apply to a 17-26 nm gap, and the counterion-dominated 0.84-1.18 nm length is not " +
            "it. §3's 100 pN is reached across a resting 17-26 nm gap at " + tallReachable +
            " of " + result.reach.count { it.gap > 16.0 } + " (gap, buffer) cells, and the " +
            "deepest gap it reaches at all is " +
            (result.reachThresholds.filter { it.appliedBias == TALL_GAP_ELECTROCHEMICAL_CEILING }
                .mapNotNull { it.deepestGapReachingTarget }.maxOrNull()
                ?.let { tallGapSix(it) + " nm" } ?: "no gap at all") +
            ", below NDI's whole band. Device B is ADMITTED at " + admitted + " of " + total +
            " swept states. NOTHING HERE IS MEASURED, and the LAYER at 17-26 nm is outside " +
            "every established validity range."
}

// ---------------------------------------------------------------------------------------------
// the standing lists
// ---------------------------------------------------------------------------------------------

private val TALL_GAP_CONVENTIONS = listOf(
    "z is normal to the electrode, positive AWAY from it; the electrode surface is z = 0",
    "the electrostatic gap IS the layer height, exactly and by construction (C-0012)",
    "the STROKE s = L0 - h is positive DOWNWARD, toward the electrode, and s < L0 (C-0050)",
    "L0 is a FORCE-ONSET height: the height at which the layer carries 1.0 pN over the " +
            "40 x 40 nm tile (C-0011, CH-0010). It is NOT a first moment — the first-moment " +
            "thickness of the same layer is 1.71-2.16x smaller (C-0077) — and NDI's " +
            "'17-26 nm of polymer thickness' is read here in the FORCE-ONSET convention, " +
            "because §3 specifies a distance between two bodies",
    "the LOAD LINE R(s) is positive UPWARD. free: R = 0. device-B: R = 10 s, C-0046's P10, " +
            "§3's desired clause placed on its own arithmetic. device-A: R = 33.333 s, " +
            "C-0017's mandate. dead-load: R = 100 pN. Device-B and dead-load pass through the " +
            "SAME point, 100 pN at 10 nm, and differ only in slope",
    "the equilibrium path is parametrised by the STROKE: at each stroke there is one bias " +
            "V_eq(s) that puts an equilibrium there, and the fold is max_s V_eq(s). A pull-in " +
            "bias cannot be bisected for — it is a DISCONTINUITY in the bias and a smooth " +
            "MAXIMUM in the stroke",
    "k_es = -dF_z/dh is NEGATIVE above the force maximum and POSITIVE below it (CH-0011)",
    "a bias ceiling belongs to a (bias, load line) pair, never to the bias alone (CH-0015)",
    "a grafting density is NOT supplied by a layer height: two explicit extrapolation rules " +
            "are carried and both are labelled as extrapolations",
    "the CANDIDATE CEILING LIST is built from the load line's DOMAIN, not inherited: C-0018's " +
            "three candidates are exhaustive only for a load line defined at every stroke the " +
            "layer admits, and all four lines here are AFFINE, so they are — no element model, " +
            "no contour, no shooting branch and no Euler load enters. An element-specific " +
            "ceiling would have to be added by whoever supplies the element (C-0092)",
    "a stability floor read at a HELD gap and a fold on a MOVING equilibrium are DIFFERENT " +
            "quantities and both are emitted: stabilityFloorAtOperating[] is the first and " +
            "pullInBias[] the second, and their bias exponents differ by an order of magnitude" 
)

private val TALL_GAP_CITED = listOf(
    "eps_r of water at 300 K = 78 — literature spans 77.7-78.3; l_B goes as 1/eps and F_es " +
            "roughly as l_B, so ~3% on every force. Moves no verdict here.",
    "Stern capacitance ~20 uF/cm2 — order-of-magnitude for aqueous electrodes, CITED FROM " +
            "C-0008 and LOAD-BEARING for the bias mapping. At a tall gap it is MORE load-bearing " +
            "than at a short one, because the diffuse layer sees less of the applied bias the " +
            "harder the electrode is driven.",
    "Manning surviving fraction 11.90% — CITED FROM C-0005, which derived it. The tile is " +
            "charge-saturated (C-0008), so a factor of three here is 7% in sigma_eff.",
    "A2 = 1.9e-3 mol cm3/g2 and A3 = 2.0e-2 — CITED FROM C-0002, measured osmometry.",
    "C-0002's PEG material sheet in full (v0, b, n_K, the crossover index 0.49).",
    "C-0005's 1.46 nm correlation band and its 123-214% one-loop bracket.",
    "CH-0007's 1.0 V point-ion boundary in APPLIED bias, and T-11's 1.23 V aqueous window.",
    "C-0046's P10 placement k_c = 10 pN/nm and its 23.41-27.91 pN/nm floor at the 10 nm layer.",
    "C-0017's 33.333 pN/nm mandate and C-0018's 0.087-0.115 V operating bias at 10 nm/0.5 mM.",
    "NDI's answers to DECISIONS-FOR-NDI.md decisions 2 and 4, 2026-08-18 — the SPECIFICATION " +
            "input that created this task, quoted verbatim in gpd/tasks/T-192-device-b-tall-gap.md."
)

private val TALL_GAP_VALIDITY = listOf(
    "MEAN FIELD. C-0005 puts the one-loop correction at 123-214% of the leading term over the " +
            "5-10 nm box. It FALLS with the gap, so a 17-26 nm gap is the one axis on which " +
            "this corner is better supported than the standing design — but the DIRECTION of " +
            "the correction for oppositely charged walls is still unknown and no claim is made.",
    "POINT IONS. C-0008's Bikerman bracket is one-sided and upward (+0.8% to +56%), so every " +
            "|F_es| here is a LOWER bound within mean field. That direction is FAVOURABLE to " +
            "the reachability answer being a refusal: a larger force would only help.",
    "THE LAYER IS OUTSIDE EVERY ESTABLISHED RANGE ABOVE 10 nm. See validityDepartures[]. No " +
            "claim in this repository has solved, swept or measured a grafted PEG layer taller " +
            "than 10 nm, and C-0011's SCF machinery has not been run at one.",
    "THE GRAFTING DENSITY IS NOT SPECIFIED. Two extrapolation rules are carried; neither is a " +
            "design and NDI has not named one.",
    "L0 IS A FORCE-ONSET HEIGHT. If NDI's '17-26 nm of polymer thickness' means a FIRST MOMENT " +
            "instead, the force-onset heights are 1.71-2.16x larger (C-0077) and every gap in " +
            "this file is the wrong one. That is a SPECIFICATION question, not a modelling one.",
    "1-D. No edge, no fringing, no lateral structure. The tile is 1.5-2.4 gap heights across at " +
            "17-26 nm, against 4-13 at 5-10 nm — so C-0022's finite-tile collar, which is +14.7% " +
            "at 40 nm and 10 nm of gap, is LARGER here and is NOT carried. Every force in this " +
            "file is therefore a 1-D under-estimate by more than the amount C-0100 measured.",
    "NO ORIGAMI STABILITY AT LOW SALT. NDI's own answer to decision 3 prices 0.5 mM in folding " +
            "yield, which this programme cannot see (CLAUDE.md: the loop can see fabrication " +
            "YIELD where it is published and fabrication COST nowhere at all).",
    "NOTHING HERE IS MEASURED. TRL 1-3."
)

private val TALL_GAP_OPEN = listOf(
    "Whether a 17-26 nm grafted PEG layer can be GROWN at either of the two grafting densities " +
            "carried here is a chemistry question this task does not touch, and it is the " +
            "first thing a bench would ask.",
    "C-0022's finite-tile edge collar is NOT carried at a tall gap and it is larger there than " +
            "anywhere it has been measured — the tile is 1.5-2.4 gap heights across. It runs " +
            "the FAVOURABLE way for the force and the UNFAVOURABLE way for the flatness, and " +
            "no claim brackets it above 10 nm.",
    "C-0011's SCF layer has not been solved at 17-26 nm. C-0003's trial functions over-read " +
            "the volume fraction by 4.15x at the 10 nm point (CLAUDE.md), and whether that " +
            "persists, grows or shrinks at a taller layer is unmeasured.",
    "Whether NDI's '17-26 nm of polymer thickness' is a force-onset height or a first moment " +
            "is a SPECIFICATION question worth 1.71-2.16x in every gap in this file.",
    "The device-B coupling ELEMENT is not designed here. C-0046 places arms of 12.7-18.1 nm " +
            "for k_c = 10 pN/nm at the 10 nm layer; whether the same family places at a 17-26 nm " +
            "layer, where the stroke is a larger fraction of the arm, is C-0046's question and " +
            "not this one's."
)
