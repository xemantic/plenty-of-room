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

package com.xemantic.nano.plentyofroom.stability

import com.xemantic.nano.plentyofroom.actuator.ActuatorForceBalance
import com.xemantic.nano.plentyofroom.actuator.ActuatorGeometry
import com.xemantic.nano.plentyofroom.actuator.BiasCeiling
import com.xemantic.nano.plentyofroom.actuator.DiffuseParametrisedField
import com.xemantic.nano.plentyofroom.actuator.EquilibriumPath
import com.xemantic.nano.plentyofroom.actuator.FieldSample
import com.xemantic.nano.plentyofroom.actuator.biasMargin
import com.xemantic.nano.plentyofroom.actuator.bindingCeiling
import com.xemantic.nano.plentyofroom.actuator.roundedForActuatorResult
import com.xemantic.nano.plentyofroom.brush.AlexanderBoxLayer
import com.xemantic.nano.plentyofroom.brush.GraftedLayerModel
import com.xemantic.nano.plentyofroom.brush.InteractionFreeEnergy
import com.xemantic.nano.plentyofroom.brush.StrongStretchingLayer
import com.xemantic.nano.plentyofroom.brush.additiveInteraction
import com.xemantic.nano.plentyofroom.brush.chainLengthForHeight
import com.xemantic.nano.plentyofroom.brush.desCloizeauxInteraction
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.reducedThirdVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.threeBodyInteraction
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannGap
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.sternChargeDensityPerVolt
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.electrostatics.uniformMedium
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Task `T-157` — **does the recommended arm fold on the LARGE-rotation branch?** Leaf `A2.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh stability.LargeRotationArmBranchStudyKt
 * ```
 *
 * Emits `gpd/results/T-157-large-rotation-arm-branch.json`, deterministically — no timestamp, no
 * step count, every floating-point number rounded at the serialisation boundary.
 */

// ---------------------------------------------------------------- study-local names (CLAUDE.md)

private val T157_BUFFERS = listOf(0.5, 2.0)

private const val T157_LAYER_HEIGHT = 10.0

private const val T157_GRAFTING_DENSITY = 0.024

private const val T157_FOOTPRINT = 1600.0

private const val T157_STERN_CAPACITANCE = 20.0

private const val T157_MESH_NODES = 2000

private const val T157_LOWEST_GAP = 0.5

private const val T157_SECOND_VIRIAL = 1.9e-3

private const val T157_THIRD_VIRIAL = 2.0e-2

private const val T157_CROSSOVER = 0.2

private const val T157_TRUSTED_BIAS = 1.0

/** `C-0084`'s own safety below a stroke ceiling, in nm — kept so the two runs are comparable. */
private const val T157_CEILING_SAFETY = 0.01

/** The tip forces the multi-branch enumeration is run at, in pN per arm. */
private val T157_ENUMERATION_FORCES = listOf(1.0, 10.0, 50.0, 100.0, 200.0, 500.0, 1000.0, 5000.0)

private class T157Field(
    concentration: Double,
    val tileCharge: Double,
    val bjerrum: Double
) {

    private val ions = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity)

    private val medium = uniformMedium(GapMedium())

    private val stern = sternChargeDensityPerVolt(T157_STERN_CAPACITANCE)

    private val volt = thermalVoltage()

    fun sample(gap: Double, diffusePotential: Double): FieldSample {
        val solution = PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = T157_MESH_NODES)
            .solve(diffusePotential / volt, tileCharge)
        return FieldSample(
            gap = gap,
            diffusePotential = diffusePotential,
            appliedBias = diffusePotential + solution.electrodeSurfaceChargeDensity / stern,
            force = solution.forceOnTile(T157_FOOTPRINT)
        )
    }

    fun asPath(): DiffuseParametrisedField = DiffuseParametrisedField { gap, psi -> sample(gap, psi) }
}

private fun t157Interaction(peg: PegWater, choice: String): InteractionFreeEnergy {
    val twoBody = twoBodyInteraction(
        peg.reducedSecondVirialCoefficient(T157_SECOND_VIRIAL), peg.monomerVolume
    )
    val threeBody = threeBodyInteraction(
        peg.reducedThirdVirialCoefficient(T157_THIRD_VIRIAL), peg.monomerVolume
    )
    return when (choice) {
        "two-body" -> twoBody
        "virial" -> additiveInteraction("virial", listOf(twoBody, threeBody))
        else -> desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)
    }
}

private fun t157LayerModels(peg: PegWater): List<Pair<String, GraftedLayerModel>> =
    listOf("alexander-box", "strong-stretching").flatMap { profile ->
        listOf("two-body", "virial", "des-Cloizeaux").map { interaction ->
            val energy = t157Interaction(peg, interaction)
            val model: GraftedLayerModel =
                if (profile == "alexander-box") AlexanderBoxLayer(energy)
                else StrongStretchingLayer(energy)
            model.name to model
        }
    }

/** The continued branch, presented as a `StrokeLoadLine` over the whole 34-arm array. */
private class T157BranchLoadLine(
    override val name: String,
    val branch: LargeRotationArmBranch,
    val count: Int = GEN1_RECOMMENDED_PATH_COUNT
) : StrokeLoadLine {

    override fun reaction(stroke: Double): Double {
        require(stroke >= 0.0) { "stroke must not be negative, was: ${stroke.roundedForProse()}" }
        return branch.reaction(stroke, count)
    }

    override fun tangent(stroke: Double): Double {
        require(stroke >= 0.0) { "stroke must not be negative, was: ${stroke.roundedForProse()}" }
        val step = 1.0e-4 * max(1.0, stroke)
        val low = max(1.0e-9, stroke - step)
        val high = min(branch.strokeSupremum, stroke + step)
        return (reaction(high) - reaction(low)) / (high - low)
    }
}

// ---------------------------------------------------------------- the emitted records

@Serializable
private data class T157EnumerationRecord(
    val force: Double,
    val rootsFound: Int,
    val rootsOnTheSmallRotationBranch: Int,
    val deepestStroke: Double,
    val deepestStrokeOverContour: Double,
    val strokeOfTheSmallRotationRoot: Double?,
    val maximumRotationOfTheDeepestRoot: Double,
    val everyRootBelowTheContour: Boolean
)

@Serializable
private data class T157BranchRecord(
    val nearRotation: Double,
    val force: Double,
    val arrayReaction: Double,
    val stroke: Double,
    val contourMinusStroke: Double,
    val maximumRotation: Double,
    val rotationOverRightAngle: Double,
    val drawIn: Double,
    val firstIntegralSpread: Double
)

@Serializable
private data class T157FoldRecord(
    val model: String,
    val concentration: Double,
    val restingHeight: Double,
    val layerStrokeCeiling: Double,
    val strokeCeiling: Double,
    val strokeCeilingOwner: String,
    val ladderCeilingInC0084: Double,
    val ceilingExtension: Double,
    val operatingBias: Double?,
    val folds: Boolean,
    val pullInBias: Double?,
    val pullInStroke: Double?,
    val foldIsInTheExtension: Boolean,
    val branchEndStroke: Double?,
    val branchEndBias: Double?,
    val bindingCeiling: String?,
    val usableBias: Double?,
    val biasMargin: Double?,
    val biasMarginInC0084: Double?,
    val marginMovement: Double?,
    val tangencyResidual: Double?,
    val verdict: String,
    val searchEvaluations: Int
)

@Serializable
private data class T157Convergence(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
private data class T157Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T157Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val citedInputs: List<String>,
    val contourBound: Map<String, String>,
    val enumeration: List<T157EnumerationRecord>,
    val branch: List<T157BranchRecord>,
    val folds: List<T157FoldRecord>,
    val convergence: List<T157Convergence>,
    val falsifiers: List<T157Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------- the study

@Suppress("LongMethod")
fun main() {
    println("T-157 — the contour bound, before any solver ...")
    val branch = recommendedArmBranches()
    val contour = branch.contour
    val ladder = ladderRefusalStroke()
    println("  contour %.8f nm; C-0084's ladder refusal %.8f nm; open window %.6f nm"
        .format(contour, ladder, contour - ladder))

    println("T-157 — enumerating the branches ...")
    val enumeration = T157_ENUMERATION_FORCES.map { force ->
        val roots = branch.branchesAt(force)
        val deepest = roots.maxByOrNull { it.stroke }
        T157EnumerationRecord(
            force = force,
            rootsFound = roots.size,
            rootsOnTheSmallRotationBranch = roots.count { it.onTheSmallRotationBranch },
            deepestStroke = deepest?.stroke ?: 0.0,
            deepestStrokeOverContour = (deepest?.stroke ?: 0.0) / contour,
            strokeOfTheSmallRotationRoot =
                roots.firstOrNull { it.onTheSmallRotationBranch }?.stroke,
            maximumRotationOfTheDeepestRoot = deepest?.maximumRotation ?: 0.0,
            everyRootBelowTheContour = roots.all { it.stroke < contour }
        )
    }
    enumeration.forEach {
        println("  F = %-8.4g roots %-3d  small-rotation %-2d  deepest %.6f nm (%.4f of the contour)"
            .format(it.force, it.rootsFound, it.rootsOnTheSmallRotationBranch,
                it.deepestStroke, it.deepestStrokeOverContour))
    }

    val supremum = branch.strokeSupremum
    println("T-157 — the continued branch reaches %.7f nm, %.6f nm short of the contour"
        .format(supremum, contour - supremum))
    val table = branch.branchTable
    val sampled = (0 until 64).map { table[it * (table.size - 1) / 63] }.distinct()
    val branchRecords = sampled.map {
        T157BranchRecord(
            nearRotation = it.nearRotation,
            force = it.force,
            arrayReaction = GEN1_RECOMMENDED_PATH_COUNT * it.force,
            stroke = it.stroke,
            contourMinusStroke = contour - it.stroke,
            maximumRotation = it.maximumRotation,
            rotationOverRightAngle = it.maximumRotation / (0.5 * PI),
            drawIn = it.drawIn,
            firstIntegralSpread = it.firstIntegralSpread
        )
    }

    println("T-157 — the coupled path over the EXTENDED domain, at the recommended device ...")
    val peg = PegWater()
    val geometry = ActuatorGeometry()
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val tileCharge = -(tile.projectedChargeDensity * tile.manningSurvivingFraction(2, lb) / 2.0)
    val fields = T157_BUFFERS.associateWith { T157Field(it, tileCharge, lb) }
    val line = T157BranchLoadLine("LQ5 recommended hinge-rooted arm, continued branch (T-157)", branch)
    val extendedCeiling = supremum - T157_CEILING_SAFETY
    val upstream = c0084Margins()
    val folds = mutableListOf<T157FoldRecord>()
    t157LayerModels(peg).forEach { (name, model) ->
        val chain = peg.graftedChain(
            model.chainLengthForHeight(peg, T157_LAYER_HEIGHT, T157_GRAFTING_DENSITY),
            T157_GRAFTING_DENSITY
        )
        val balance = ActuatorForceBalance(model, chain, geometry)
        val resting = balance.restingHeight
        val layerCeiling = resting - max(chain.occupiedThickness * 1.01, T157_LOWEST_GAP)
        T157_BUFFERS.forEach { concentration ->
            val field = fields.getValue(concentration)
            val ceiling = min(layerCeiling, extendedCeiling)
            val owner = if (ceiling < layerCeiling) "element model (continued branch)" else "layer"
            val path = EquilibriumPath(
                restingHeight = resting,
                strokeCeiling = ceiling,
                field = field.asPath()
            ) { stroke -> line.reaction(stroke) + balance.layerLoad(resting - stroke) }
            val search = path.fold()
            val fold = search.fold
            val operating = path.at(GEN1_ACCEPTABLE_STROKE)
            val crossoverStroke = resting - chain.occupiedThickness / T157_CROSSOVER
            val crossover =
                if (crossoverStroke <= 0.0 || crossoverStroke > ceiling) null
                else path.at(crossoverStroke)?.appliedBias
            val crossoverBeyond = crossoverStroke > (fold?.stroke ?: ceiling)
            val elementBoundary =
                fold == null && owner.startsWith("element model") && !search.reachedDiffuseCeiling
            val candidates = listOf(
                BiasCeiling("static stability (pull-in)", fold?.appliedBias),
                BiasCeiling(
                    "concentrated crossover (C-0002, phi = 0.2)",
                    if (crossoverBeyond) null else crossover
                ),
                BiasCeiling("point-ion boundary (CH-0007, 1.0 V)", T157_TRUSTED_BIAS)
            ) + if (!elementBoundary) emptyList() else listOf(
                BiasCeiling(
                    "element model branch end (T-157's continued branch)",
                    search.branchEnd?.appliedBias
                )
            )
            val binding = bindingCeiling(candidates)
            val margin = biasMargin(binding?.bias, operating?.appliedBias)
            val before = upstream["$name|${concentration.roundedForProse()}"]
            val tangencyResidual = fold?.let { point ->
                if (search.foldAtBranchStart || abs(point.stroke - ceiling) <= 1e-6) null
                else {
                    val brush = balance.layerStiffness(point.gap)
                    val tangent = line.tangent(point.stroke)
                    abs(tangent + brush) / max(abs(tangent) + abs(brush), 1e-12)
                }
            }
            folds += T157FoldRecord(
                model = name,
                concentration = concentration,
                restingHeight = resting,
                layerStrokeCeiling = layerCeiling,
                strokeCeiling = ceiling,
                strokeCeilingOwner = owner,
                ladderCeilingInC0084 = ladder - T157_CEILING_SAFETY,
                ceilingExtension = ceiling - (ladder - T157_CEILING_SAFETY),
                operatingBias = operating?.appliedBias,
                folds = fold != null,
                pullInBias = fold?.appliedBias,
                pullInStroke = fold?.stroke,
                foldIsInTheExtension =
                    fold != null && fold.stroke > ladder - T157_CEILING_SAFETY,
                branchEndStroke = search.branchEnd?.stroke,
                branchEndBias = search.branchEnd?.appliedBias,
                bindingCeiling = binding?.name,
                usableBias = binding?.bias,
                biasMargin = margin,
                biasMarginInC0084 = before,
                marginMovement = if (before != null && margin != null) margin / before else null,
                tangencyResidual = tangencyResidual,
                verdict = when {
                    fold == null -> "NO FOLD below ${ceiling.roundedForProse()} nm of stroke, " +
                            "which is " +
                            "${"%.4f".format(ceiling / contour)} of the arm's own contour"
                    fold.stroke > ladder - T157_CEILING_SAFETY ->
                        "FOLD, and it is INSIDE the extension C-0084 could not reach"
                    else -> "FOLD, and it is below C-0084's own ceiling — inconsistent, investigate"
                },
                searchEvaluations = path.evaluations
            )
            println("  %-32s %4s mM: %s".format(name, concentration, folds.last().verdict))
        }
    }

    val convergence = t157Convergence(contour)
    val output = File("gpd/results/T-157-large-rotation-arm-branch.json")
    val json = Json { prettyPrint = true }
    val result = T157Result(
        task = "T-157",
        leaf = "A2.2",
        title = "Does C-0069's Q5 fold on the LARGE-rotation branch? An inextensibility bound " +
                "that holds on every branch, a multi-branch enumeration of C-0039's shooting " +
                "residual, a continuation of the branch connected to the unloaded state, and " +
                "C-0018's equilibrium path re-run over the extended domain",
        verificationType = "logical (delta = integral of sin phi < L, on every branch, at every " +
                "force, with no solver) + in-silico (a scan for every sign change of the far-end " +
                "moment residual; a continuation in the near-end rotation truncated where the " +
                "first integral stops being conserved; C-0018's equilibrium path, solver " +
                "unchanged, over the extended domain)",
        acceptance = "C-0084 still-open item 1: whether a pull-in fold exists above 7.9097 nm of " +
                "stroke at all - or the statement that the branch cannot be enumerated",
        maturity = "TRL 1-3 - model-consistent and traceable, NOTHING HERE IS MEASURED, AND THE " +
                "MOTIF IS NOT DEMONSTRATED (C-0055's 62 recorded queries stand). Every force " +
                "inherits C-0008's mean field, whose one-loop correction (C-0005) is 123-214 % " +
                "of the leading term over this gap range",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "rotationalStiffness" to "pN nm/rad",
            "rotation" to "rad",
            "potential" to "V",
            "concentration" to "mM",
            "temperature" to "K"
        ),
        conventions = listOf(
            "T = 300 K, k_BT = 4.141947 pN nm; aqueous MgCl2 at 0.5 and 2.0 mM",
            "C-0039's geometry, restated: arc length s in [0, L] from the hinge; phi(s) the " +
                    "tangent angle from the undeformed axis toward the stroke; x = integral of " +
                    "cos phi, z = integral of sin phi; EI phi'' = -F cos phi, EI phi'(0) = " +
                    "k_n phi(0), EI phi'(L) = -k_f phi(L)",
            "the conserved first integral is 0.5 EI phi'^2 + F sin phi, and its measured spread " +
                    "along a sweep is the integrator's own error",
            "one arm's stroke is delta = z(L); the array's stroke is the same delta, and its " +
                    "reaction is 34 times one arm's tip force (C-0017's mandate is a SUM)",
            "the device's stroke s = L0 - h is positive downward; L0 is a force-onset height at " +
                    "a defining load of 1.0 pN over the tile (C-0011, CH-0010)",
            "a fold is max_s V_eq(s), and at it k_c(s) + k_eff(s) = 0 exactly",
            "the recommended device is the 10 nm layer at sigma = 0.024 nm^-2, placed at 100 pN " +
                    "over section 3's acceptable 3 nm stroke (C-0071, C-0068)"
        ),
        parameters = mapOf(
            "bendingRigidity" to "230.0 pN nm^2 (CanDo model input, not a measurement)",
            "contour" to "${contour.roundedForProse()} nm",
            "rootStiffness" to "${GEN1_ARM_ROOT_STIFFNESS.roundedForProse()} pN nm/rad",
            "tipStiffness" to "${GEN1_ARM_TIP_STIFFNESS.roundedForProse()} pN nm/rad",
            "pathCount" to "$GEN1_RECOMMENDED_PATH_COUNT",
            "rk4Steps" to "${branch.steps}",
            "rotationStep" to "${branch.rotationStep} rad",
            "firstIntegralTolerance" to "$BRANCH_FIRST_INTEGRAL_TOLERANCE",
            "layerHeight" to "$T157_LAYER_HEIGHT nm",
            "graftingDensity" to "$T157_GRAFTING_DENSITY nm^-2",
            "buffers" to T157_BUFFERS.toString(),
            "meshNodes" to "$T157_MESH_NODES"
        ),
        citedInputs = listOf(
            "C-0039 - the two-spring elastica and its shooting solve",
            "C-0069 / C-0071 - the recommended element Q5 and its placement",
            "C-0084 - the fold search this task bounds, and its 7.9097 nm ceiling",
            "CH-0099 - the ceiling taxonomy this task moves the value of",
            "C-0018 - the equilibrium path, re-used unchanged",
            "C-0034 - the closed-form end-condition factor, the vanishing-load limit",
            "C-0003 / C-0002 / C-0008 - the six layer models, the material, the field"
        ),
        contourBound = mapOf(
            "statement" to ("delta = integral of sin phi over [0, L] <= L, with equality only if " +
                    "phi is identically pi/2, which the near-end condition EI phi'(0) = " +
                    "k_n phi(0) forbids for any finite k_n > 0. So delta < L STRICTLY, on every " +
                    "branch, at every tip force, at every rotation"),
            "contour" to "${contour.roundedForProse()} nm",
            "C-0084's ceiling" to "${(ladder - T157_CEILING_SAFETY).roundedForProse()} nm " +
                    "(paths), ${ladder.roundedForProse()} nm (refusal)",
            "the open window it leaves" to "${(contour - ladder).roundedForProse()} nm",
            "the window this task closes" to "${(supremum - ladder).roundedForProse()} nm",
            "the window still open" to "${(contour - supremum).roundedForProse()} nm",
            "cost" to "zero - it is a bound on an integral of a bounded function"
        ),
        enumeration = enumeration,
        branch = branchRecords,
        folds = folds,
        convergence = convergence,
        falsifiers = t157Falsifiers(folds, ladder, supremum, contour, enumeration),
        findings = t157Findings(folds, ladder, supremum, contour, enumeration, branch),
        validity = listOf(
            "TRL 1-3. Nothing here is measured and the motif is not demonstrated",
            "MEAN FIELD, inherited whole. C-0005's one-loop correction is 123-214 % of the " +
                    "leading term over this gap range - larger than every margin here",
            "The contour bound is EXACT and needs no validity range: it is a bound on an integral " +
                    "of a bounded function, and no elastica branch, no rotation and no force " +
                    "escapes it",
            "The branch continuation is truncated where the FIRST INTEGRAL stops being conserved " +
                    "to " + BRANCH_FIRST_INTEGRAL_TOLERANCE + ", which is a MEASURED limit of the " +
                    "integrator and not a property of the elastica. A stiffer integrator would " +
                    "extend it; nothing can extend it past the contour",
            "The multi-branch ROOT COUNT is a sampling statistic of the shooting scan, so it is a " +
                    "LOWER bound (CLAUDE.md: a search over a continuum returns a density). " +
                    "Existence of a root is monotone under refinement and is safe; the count is " +
                    "not, and roots closer together than one scan cell are missed",
            "Only the 10 nm layer is run. The 5 nm and 7 nm devices are not this element's " +
                    "(C-0064, C-0068) and 10 mM has no operating point at all (C-0084)",
            "1-D, static, and C-0033's collar is not composed - the same choices C-0018, C-0032 " +
                    "and C-0084 made",
            "The load line is the tile MEAN under a uniform load; a real 34-attachment coupling " +
                    "dishes the tile (C-0063, C-0068)"
        ),
        openQuestions = listOf(
            "The last " + (contour - supremum).roundedForProse() + " nm below the contour, " +
                    "which the integrator " +
                    "cannot resolve at this step count. The contour bound says no equilibrium " +
                    "exists AT or above the contour; it does not by itself exclude a fold inside " +
                    "that sliver",
            "The disconnected curled branches are enumerated and are NOT priced: they are not " +
                    "reachable by quasi-static loading from the unloaded state, and whether a " +
                    "dynamic bias step could reach one is a question this static treatment " +
                    "cannot answer",
            "C-0033's collar at the extended fold gaps, which is one evaluation per state and is " +
                    "not done here",
            "Whether C-0084's other 96 states move. Only the 12 of the recommended device are " +
                    "re-run"
        )
    )
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForActuatorResult().withEmissionHeader(LatticeTag.SQUARE, null)) + "\n"
    )
    println()
    result.findings.forEach { (key, value) -> println("  $key:\n    $value\n") }
    result.falsifiers.forEach { println("  ${it.id} fired=${it.fired}: ${it.outcome}") }
    println()
    println("  written to $output")
}

// ---------------------------------------------------------------- helpers

/** `C-0084`'s own bias margins at the recommended device, read from its result file. */
private fun c0084Margins(): Map<String, Double> {
    val file = ResultInputs.T_149.file()
    if (!file.exists()) return emptyMap()
    val reader = Json { ignoreUnknownKeys = true }
    val rows = reader.parseToJsonElement(file.readText()).jsonObject["folds"]!!.jsonArray
    return rows.mapNotNull { row ->
        val fold = reader.decodeFromJsonElement<T157UpstreamFold>(row)
        if (fold.layerHeight != T157_LAYER_HEIGHT || !fold.loadLine.startsWith("LQ5")) null
        else fold.biasMargin?.let { "${fold.model}|${fold.concentration.roundedForProse()}" to it }
    }.toMap()
}

/** The one `T-149` row shape this study reads — keyed on every dimension its sweep varied. */
@Serializable
private data class T157UpstreamFold(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val loadLine: String,
    val biasMargin: Double? = null
)

private fun t157Convergence(contour: Double): List<T157Convergence> {
    val finest = recommendedArmBranches(steps = 1600, rotationStep = 1.0e-3)
    val finestSupremum = finest.strokeSupremum
    val stepRows = listOf(200, 400, 800, 1600).map { steps ->
        val here = recommendedArmBranches(steps = steps, rotationStep = 2.0e-3)
        val target = min(7.5, here.strokeSupremum - 1.0e-3)
        val force = here.forceForStroke(target)
        val reference = recommendedArmBranches(steps = 1600, rotationStep = 2.0e-3)
            .forceForStroke(target)
        T157Convergence(
            axis = "RK4 steps",
            setting = "$steps",
            quantity = "the tip force at a ${"%.3f".format(target)} nm stroke [pN]",
            value = force,
            departureFromFinest = abs(force / reference - 1.0)
        )
    }
    val marchRows = listOf(8.0e-3, 4.0e-3, 2.0e-3, 1.0e-3).map { step ->
        val here = recommendedArmBranches(steps = 1600, rotationStep = step)
        T157Convergence(
            axis = "continuation step [rad]",
            setting = "$step",
            quantity = "the deepest stroke the branch reaches [nm]",
            value = here.strokeSupremum,
            departureFromFinest = abs(here.strokeSupremum - finestSupremum)
        )
    }
    val scanRows = listOf(1000, 2000, 4000, 8000).map { steps ->
        val roots = recommendedArmBranches().branchesAt(1000.0, scanSteps = steps)
        T157Convergence(
            axis = "shooting scan cells",
            setting = "$steps",
            quantity = "roots of the far-end residual at F = 1000 pN (a LOWER bound)",
            value = roots.size.toDouble(),
            departureFromFinest = abs(roots.count { it.stroke < contour } - roots.size).toDouble()
        )
    }
    return stepRows + marchRows + scanRows
}

private fun t157Falsifiers(
    folds: List<T157FoldRecord>,
    ladder: Double,
    supremum: Double,
    contour: Double,
    enumeration: List<T157EnumerationRecord>
): List<T157Falsifier> {
    val inExtension = folds.count { it.foldIsInTheExtension }
    return listOf(
        T157Falsifier(
            id = "F1",
            statement = "the refusal being real - the branch genuinely ending near 7.92 nm, " +
                    "which would leave C-0084 and CH-0099 standing as written",
            fired = supremum <= ladder,
            outcome = if (supremum > ladder)
                ("NO, and it is the finding. The continued branch reaches %.7f nm, %.6f nm past " +
                        "C-0084's ladder refusal at %.7f nm, with max|phi| still below a right " +
                        "angle. The refusal is a property of the doubling force ladder, not of " +
                        "the elastica").format(supremum, supremum - ladder, ladder)
            else "YES - the branch ends where C-0039's ladder says it does"
        ),
        T157Falsifier(
            id = "F2",
            statement = "a fold in the extension - V_eq turning over anywhere the ladder could " +
                    "not reach, which would make C-0084's 'no fold' wrong at some model rather " +
                    "than merely bounded",
            fired = inExtension > 0,
            outcome = if (inExtension == 0)
                ("NO. At %d of %d states of the recommended device the path has no fold below " +
                        "%.4f nm of stroke, which is %.4f of the arm's own contour")
                    .format(folds.count { !it.folds }, folds.size,
                        folds.first().strokeCeiling, folds.first().strokeCeiling / contour)
            else "YES at $inExtension of ${folds.size} states"
        ),
        T157Falsifier(
            id = "F3",
            statement = "a branch reaching PAST the contour - impossible by the bound, and the " +
                    "strongest falsifier available here precisely because it cannot fire",
            fired = enumeration.any { !it.everyRootBelowTheContour },
            outcome = if (enumeration.all { it.everyRootBelowTheContour })
                ("NO. Every root at every enumerated force is below the contour; the deepest " +
                        "any branch reaches is %.6f of it")
                    .format(enumeration.maxOf { it.deepestStrokeOverContour })
            else "YES - the integrator is wrong and every elastica number in the corpus is in question"
        )
    )
}

private fun t157Findings(
    folds: List<T157FoldRecord>,
    ladder: Double,
    supremum: Double,
    contour: Double,
    enumeration: List<T157EnumerationRecord>,
    branch: LargeRotationArmBranch
): Map<String, String> = mapOf(
    "the contour bound" to
            ("delta = integral of sin phi < L = %.8f nm, on EVERY branch, at every force, with " +
                    "no solver. So no equilibrium and no fold exists at or above the contour, and " +
                    "C-0084's open question is bounded at %.6f nm before any code runs")
                .format(contour, contour - ladder),
    "C-0084's branch end is a FORCE-LADDER ARTEFACT" to
            ("the continued branch reaches %.7f nm - %.6f nm past the %.7f nm at which " +
                    "C-0039's doubling ladder refuses - with max|phi| = %.7f rad, still BELOW a " +
                    "right angle (%.7f). The branch does not end there; the ladder loses it, " +
                    "because a second root of the shooting residual appears at a tip force well " +
                    "below the right angle and a bracket found by doubling can land on either")
                .format(supremum, supremum - ladder, ladder,
                    branch.branchTable.last().maximumRotation, 0.5 * PI),
    "the multi-branch structure" to
            ("the far-end residual has one root up to about %g pN of tip force and %d at 1000 pN " +
                    "on a 4000-cell scan. AT MOST ONE root is on the small-rotation branch at " +
                    "every force enumerated, and wherever the scan finds it it is the DEEPEST: " +
                    "the others are curled shapes whose integral of sin phi cancels, so they " +
                    "reach a SMALLER stroke. The large-rotation branches do not extend the " +
                    "stroke - they RETREAT from it. At %d of %d enumerated forces the scan finds " +
                    "no small-rotation root at all, and that is the density caveat in action " +
                    "rather than a branch end: at high force the primary root sits within one " +
                    "scan cell of a curled neighbour, and the CONTINUATION is what resolves it")
                .format(
                    enumeration.last { it.rootsFound == 1 }.force,
                    enumeration.single { it.force == 1000.0 }.rootsFound,
                    enumeration.count { it.rootsOnTheSmallRotationBranch == 0 },
                    enumeration.size
                ),
    "the answer" to
            (if (folds.none { it.folds })
                ("NO FOLD, at %d of %d states of the recommended device, over a domain running to " +
                        "%.4f nm of stroke - %.4f of the arm's contour and %.2fx section 3's " +
                        "acceptable 3 nm. C-0084's bounded negative becomes a bound at the " +
                        "CONTOUR: the only stroke a fold could still hide in is the %.6f nm the " +
                        "integrator cannot resolve, %.4f of one base-pair rise")
                    .format(folds.count { !it.folds }, folds.size, folds.first().strokeCeiling,
                        folds.first().strokeCeiling / contour, folds.first().strokeCeiling / 3.0,
                        contour - supremum, (contour - supremum) / 0.34)
            else "A FOLD EXISTS in the extension at ${folds.count { it.foldIsInTheExtension }} " +
                    "of ${folds.size} states"),
    "what it does to the margin" to
            (folds.mapNotNull { it.marginMovement }.let { moves ->
                if (moves.isEmpty()) "C-0084's file was not available to compare against"
                else ("the bias margin at the recommended device moves by a factor of %.4f-%.4f " +
                        "against C-0084's, because the element boundary the margin is read " +
                        "against has moved outward by %.4f nm of stroke")
                    .format(moves.min(), moves.max(), folds.first().ceilingExtension)
            }),
    "what CH-0099 keeps and what it loses" to
            ("the CANDIDATE stands - a coupling element has a domain and C-0018's three-candidate " +
                    "list has no name for it. The VALUE does not: the domain ends at the contour, " +
                    "not at 7.92 nm, and CH-0099's 2.57-3.74x inflation is measured against a " +
                    "boundary %.6f nm too shallow").format(supremum - ladder)
)
