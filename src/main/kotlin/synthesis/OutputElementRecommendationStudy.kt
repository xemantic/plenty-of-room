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

package com.xemantic.nano.plentyofroom.synthesis

import com.xemantic.nano.plentyofroom.anchoring.ArmAnchorage
import com.xemantic.nano.plentyofroom.anchoring.OrigamiDuplex
import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.anchoring.StandoffBase
import com.xemantic.nano.plentyofroom.anchoring.UPWARD_ROOT_PITCH_BASE_PAIRS
import com.xemantic.nano.plentyofroom.anchoring.axialLengthForStiffness
import com.xemantic.nano.plentyofroom.anchoring.bendingFactorForLength
import com.xemantic.nano.plentyofroom.anchoring.bendingLengthForStiffness
import com.xemantic.nano.plentyofroom.anchoring.elasticaArmCeiling
import com.xemantic.nano.plentyofroom.anchoring.elasticaArmForStiffness
import com.xemantic.nano.plentyofroom.anchoring.entropicContourForStiffness
import com.xemantic.nano.plentyofroom.anchoring.farRestraintCeiling
import com.xemantic.nano.plentyofroom.anchoring.hingeLeverForStiffness
import com.xemantic.nano.plentyofroom.anchoring.nearRestraintCeiling
import com.xemantic.nano.plentyofroom.anchoring.perPathStiffness
import com.xemantic.nano.plentyofroom.anchoring.rowOfThreeLengthCeiling
import com.xemantic.nano.plentyofroom.anchoring.standoffTipFlexibility
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

/**
 * Task `T-135` — **which output element does the Gen-1 programme recommend, and on what
 * premises?** Leaf `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh synthesis.OutputElementRecommendationStudyKt
 * ```
 *
 * Emits `gpd/results/T-135-output-element-recommendation.json`, deterministically — no timestamp,
 * every floating-point number rounded at the serialisation boundary, and every ranking decided by
 * a fixed declaration order rather than by a scan over a map (`CLAUDE.md`).
 *
 * **Nothing here is read from another study's result file.** Every number the recommendation
 * writes is recomputed from the library that owns it — `C-0039`'s exact elastica, `C-0034`'s
 * anchorage, `C-0028`'s standoff head, `C-0069`'s plan budget and restraint window, `C-0017`'s
 * mandate — and the departure from the owning claim's published value is quoted. That is §7's
 * *"numbers that were inherited get re-derived rather than cited"* applied to a synthesis, where
 * the temptation to transcribe is strongest.
 */

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

/** The recommended element, assembled, with every number recomputed here. */
@Serializable
@Suppress("LongParameterList")
data class T135RecommendedElement(
    val id: String,
    val name: String,
    val owner: String,
    val clause: String,
    val pathCount: Int,
    val perPathSecant: Double,
    val length: Double,
    val lengthBasePairs: Double,
    val endConditionFactor: Double,
    val planBudget: Double,
    val planMargin: Double,
    val rootJoint: String,
    val rootStiffness: Double,
    val rootCeiling: Double,
    val tipJoint: String,
    val tipStiffness: Double,
    val tipCeiling: Double,
    val kinematicFloor: Double,
    val perPathForce: Double,
    val compressionMembers: Int,
    val twoSided: Boolean,
    val note: String
)

/** One element the recommendation does **not** pick, and the clause that removes it. */
@Serializable
data class T135RejectedElement(
    val id: String,
    val name: String,
    val length: Double,
    val againstBudget: Double,
    val placed: Int,
    val removedBy: String,
    val owner: String
)

/** The whole result envelope. */
@Serializable
@Suppress("LongParameterList")
data class T135Result(
    val task: String,
    val leaf: String,
    val question: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val decidability: RecommendationDecidability,
    val candidates: List<RecommendationCandidate>,
    val recommended: T135RecommendedElement,
    val rejected: List<T135RejectedElement>,
    val premises: List<RecommendationPremise>,
    val premiseCounts: Map<String, Int>,
    val margins: List<RecommendationMargin>,
    val marginCounts: Map<String, Int>,
    val conditionals: List<RecommendationConditional>,
    val conditionalCounts: Map<String, Int>,
    val failureRoutes: List<RecommendationFailureRoute>,
    val failureRouteCounts: Map<String, Int>,
    val sectionSix: List<RecommendationSectionSixVerdict>,
    val openItems: List<String>,
    val reproductions: List<RecommendationReproduction>,
    val convergence: List<RecommendationConvergence>,
    val verdict: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// parameters — every one of them named, none of them inferred
// ---------------------------------------------------------------------------------------------

/** §3's **acceptable** clause, which `C-0050` shows is the only one this stack can be written at. */
private const val ACCEPTABLE_STROKE = Gen1Tile.ACCEPTABLE_STROKE

private const val TARGET_FORCE = Gen1Tile.TARGET_FORCE

/** `C-0006`/`CH-0029`'s per-path unzip allowable — **CITED**. */
private const val UNZIP_ALLOWABLE = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE

/** `C-0017`'s worst of six 2 mM stability floors at the 10 nm layer — **CITED**. */
private const val WORST_FLOOR_TWO_MILLIMOLAR = 27.9133262

/** `C-0069`'s assembled tangent minimum for the recommended array over `[0, 3]` — **CITED**. */
private const val ASSEMBLED_TANGENT_MINIMUM = 30.028762

/** `C-0049`'s per-path secant ceiling `n·a/s` at 34 paths and 3 nm — reproduced below. */
private const val PER_PATH_SECANT_CEILING = 113.333333

/** `T-5b`'s dishing convention, a fraction of the free-tile stroke. */
private const val FLATNESS_CONVENTION = 0.10

/** `C-0063`'s dishing at its own design state — **CITED**, consumed as data. */
private const val DISHING_AT_DESIGN_STATE = 0.0706

/** `C-0068`'s worst range dishing over the three buffers (10 mM) — **CITED**. */
private const val DISHING_OVER_RANGE = 0.0896

/** `C-0063`'s worst solved per-path force and worst crossover force — **CITED**. */
private const val WORST_SOLVED_PATH_FORCE = 2.298

private const val WORST_CROSSOVER_FORCE = 1.246

/** `C-0066`'s tie demand at an arm tip: half a duplex. */
private const val TIE_DEMAND = OrigamiDuplex.INTERHELICAL / 2.0

/** `C-0028`'s `B2` standoff length, the runner-up's base. */
private const val STANDOFF_LENGTH = 8.0

/** §3's desired clause, carried only to record that it is VIOLATED by a lever's own kinematics. */
private const val DESIRED_STROKE = Gen1Tile.DESIRED_STROKE

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod")
fun main() {
    val mandate = mandatedCouplingStiffness(TARGET_FORCE, ACCEPTABLE_STROKE)
    val perPath = perPathStiffness(mandate, ARM_COUNT)
    val rigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val tipCouple = ArmAnchorage.twoTerminus().rotationalStiffness
    val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * Gen1Tile.RISE_PER_BASE_PAIR
    val width = OrigamiDuplex.INTERHELICAL
    val budget = rowOfThreeLengthCeiling(pitch, width)

    println("T-135 — the recommended element, recomputed from the libraries that own it ...")

    val arm = elasticaArmForStiffness(
        hingeStiffness = hinge, hingeCount = 1, farStiffness = tipCouple,
        bendingRigidity = rigidity, count = ARM_COUNT, targetStiffness = mandate,
        workingDisplacement = ACCEPTABLE_STROKE
    )
    val tipCeiling = requireNotNull(farRestraintCeiling(hinge, budget, rigidity, ARM_COUNT, mandate)) {
        "the tip-restraint ceiling must exist at a one-crossover root"
    }
    val rootCeiling = requireNotNull(
        nearRestraintCeiling(tipCouple, budget, rigidity, ARM_COUNT, mandate)
    ) { "the root-restraint ceiling must exist at C-0034's A2 tip" }
    val kinematicFloor = 1.5 * ACCEPTABLE_STROKE
    val perPathForce = mandate * ACCEPTABLE_STROKE / ARM_COUNT

    // the runner-up and the two refusals, recomputed rather than quoted
    val standoffHead = standoffTipFlexibility(
        rigidity, STANDOFF_LENGTH, StandoffBase.crossovers(2).rotationalStiffness
    )
    val crank = elasticaArmForStiffness(
        hingeStiffness = 1.0 / standoffHead.rotationUnderMoment, hingeCount = 1,
        farStiffness = 0.0, bendingRigidity = rigidity, count = ARM_COUNT,
        targetStiffness = mandate, workingDisplacement = ACCEPTABLE_STROKE
    )
    val rigidRootArm = elasticaArmCeiling(0.0, ARM_COUNT, rigidity, mandate, ACCEPTABLE_STROKE)
    val twoSupportFloor = bendingLengthForStiffness(48.0, rigidity, perPath)
    val axialLength = axialLengthForStiffness(Gen1Tile.DUPLEX_STRETCH_MODULUS, perPath)
    val strandContour =
        entropicContourForStiffness(perPath, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)

    // ------------------------------------------------------- the cheap bound: is it decidable?

    println("T-135 — the decidability bound ...")

    val candidates = listOf(
        RecommendationCandidate(
            id = "Q5", name = "C-0055/C-0063's hinge-rooted arm — one crossover root, C-0034's A2 tip",
            undemonstratedMotifs = 1, stabilityFloorsCleared = 6, compressionMembers = 0,
            placesInFull = true, singleLevel = true, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q7", name = "the standoff-headed crank — C-0028's B2 base at 8 nm, pinned tip",
            undemonstratedMotifs = 2, stabilityFloorsCleared = 4, compressionMembers = 1,
            placesInFull = true, singleLevel = true, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q11", name = "C-0023's E2 — a single strand",
            undemonstratedMotifs = 0, stabilityFloorsCleared = 6, compressionMembers = 0,
            placesInFull = true, singleLevel = true, twoSided = false
        ),
        RecommendationCandidate(
            id = "Q1", name = "C-0030's coupled flexure, across the rows",
            undemonstratedMotifs = 2, stabilityFloorsCleared = 6, compressionMembers = 1,
            placesInFull = false, singleLevel = false, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q2", name = "C-0030's coupled flexure, along the rows",
            undemonstratedMotifs = 2, stabilityFloorsCleared = 6, compressionMembers = 1,
            placesInFull = false, singleLevel = false, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q3", name = "C-0023's E3a, pinned ends",
            undemonstratedMotifs = 1, stabilityFloorsCleared = 6, compressionMembers = 0,
            placesInFull = false, singleLevel = false, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q4", name = "C-0023's E3a, clamped ends",
            undemonstratedMotifs = 1, stabilityFloorsCleared = 6, compressionMembers = 0,
            placesInFull = false, singleLevel = false, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q6", name = "the same arm with a pinned tip — refused by its own kinematics",
            undemonstratedMotifs = 1, stabilityFloorsCleared = 0, compressionMembers = 0,
            placesInFull = false, singleLevel = false, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q8", name = "a RIGID root with a pinned tip — the truss reading (CH-0081)",
            undemonstratedMotifs = 2, stabilityFloorsCleared = 6, compressionMembers = 1,
            placesInFull = false, singleLevel = false, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q9", name = "one crossover root with a GUIDED tip",
            undemonstratedMotifs = 1, stabilityFloorsCleared = 6, compressionMembers = 0,
            placesInFull = false, singleLevel = false, twoSided = true
        ),
        RecommendationCandidate(
            id = "Q10", name = "C-0023's E1 — a duplex along z, loaded along z",
            undemonstratedMotifs = 1, stabilityFloorsCleared = 6, compressionMembers = 1,
            placesInFull = false, singleLevel = false, twoSided = true
        )
    )
    val bound = decidability(candidates, placeAtOneLevel = 3)
    check(bound.catalogueSize == 11) { "C-0069's catalogue is 11 rows, was ${bound.catalogueSize}" }

    // ------------------------------------------------------------------------ the recommendation

    val recommended = T135RecommendedElement(
        id = "Q5",
        name = "C-0055/C-0063's hinge-rooted arm (C-0039's E5a1)",
        owner = "C-0069, on C-0055's site, C-0063's placement and C-0034's tip",
        clause = "§3 ACCEPTABLE — 100 pN over 3 nm, per C-0050",
        pathCount = ARM_COUNT,
        perPathSecant = perPath,
        length = arm,
        lengthBasePairs = arm / Gen1Tile.RISE_PER_BASE_PAIR,
        endConditionFactor = bendingFactorForLength(arm, rigidity, perPath),
        planBudget = budget,
        planMargin = budget - arm,
        rootJoint = "one antiparallel crossover at C-0055's unused EAST azimuth",
        rootStiffness = hinge,
        rootCeiling = rootCeiling,
        tipJoint = "C-0034's A2 — a duplex end, two strand termini on a 1.0 nm phosphate radius",
        tipStiffness = tipCouple,
        tipCeiling = tipCeiling,
        kinematicFloor = kinematicFloor,
        perPathForce = perPathForce,
        compressionMembers = 0,
        twoSided = true,
        note = "34 instances at one level, 0 overlaps, 0 clashes, 0.463 of the plan; the coupling " +
                "enters at the hinges, so the design needs no tie grid at all"
    )

    val rejected = listOf(
        T135RejectedElement(
            "Q7", "the standoff-headed crank — C-0028's B2 base at 8 nm, pinned tip", crank,
            crank / budget, ARM_COUNT,
            "places, and loses all three tie-break axes: a second undemonstrated motif, 4 of 6 " +
                    "stability floors, and a compression member",
            "C-0069, C-0028"
        ),
        T135RejectedElement(
            "Q8", "an end-loaded arm on a RIGID (truss) root", rigidRootArm,
            rigidRootArm / budget, 24,
            "12.9 % past the plan budget — a stiffer root demands a LONGER arm (CH-0081)",
            "CH-0081, C-0069"
        ),
        T135RejectedElement(
            "E3a", "the two-support flexure family at its SOFTEST end condition", twoSupportFloor,
            twoSupportFloor / budget, 23,
            "refused at every span, every end joint and every placement — c >= 48 forces " +
                    "(48 EI/k)^(1/3), 2.74x the budget",
            "C-0069, C-0025, C-0030, C-0065"
        ),
        T135RejectedElement(
            "Q10", "C-0023's E1 — a duplex along z, loaded along z", axialLength,
            axialLength / budget, 0,
            "the only genuinely out-of-plane mechanism DNA has, and it is AXIAL: 112x " +
                    "C-0017's whole 10 nm envelope",
            "C-0069, C-0023"
        ),
        T135RejectedElement(
            "Q11", "C-0023's E2 — a single strand", strandContour, strandContour / budget,
            ARM_COUNT,
            "places 34 at one level and is ONE-SIDED — it carries no compression, measured at " +
                    "negative argument rather than assumed",
            "C-0069, C-0023"
        )
    )

    // ------------------------------------------------------------------------ the premise ledger

    println("T-135 — the premise ledger ...")
    val premises = premises()
    val premiseCounts = PremiseStatus.entries.associate { status ->
        status.name to premises.count { it.status == status }
    }

    // ------------------------------------------------------------------------ the margin ledger

    println("T-135 — the margin ledger ...")
    val margins = margins(arm, budget, hinge, rootCeiling, tipCouple, tipCeiling, pitch, perPathForce)
    val marginCounts = MarginClass.entries.associate { band ->
        band.name to margins.count { it.classification == band }
    }

    // ------------------------------------------------------------- the specification conditionals

    val conditionals = conditionals()
    val conditionalCounts = ConditionalStatus.entries.associate { status ->
        status.name to conditionals.count { it.status == status }
    }

    // ------------------------------------------------------------------------ the failure routes

    val routes = failureRoutes()
    val routeCounts = RouteEffect.entries.associate { effect ->
        effect.name to routes.count { it.effect == effect }
    } + RouteDecider.entries.associate { decider ->
        "decidedBy ${decider.name}" to routes.count { it.decidedBy == decider }
    } + mapOf(
        "insideAPublishedBracket" to routes.count { it.insidePublishedBracket },
        "removeTheElement AND insideAPublishedBracket" to routes.count {
            it.effect == RouteEffect.REMOVES_THE_ELEMENT && it.insidePublishedBracket
        }
    )

    // --------------------------------------------------------------------- upstream reproductions

    println("T-135 — the upstream reproductions ...")
    val reproductions = listOf(
        recommendationReproduction("C-0017", "the mandate as a sum [pN/nm]", 33.3333333, mandate),
        recommendationReproduction("C-0069", "the per-path secant at 34 paths [pN/nm]", 0.980392157, perPath),
        recommendationReproduction("C-0055", "the upward root pitch [nm]", 10.88, pitch),
        recommendationReproduction("C-0069", "the plan budget, pitch - d [nm]", 8.19, budget),
        recommendationReproduction("C-0069", "the hinge-rooted arm [nm]", 8.16439083, arm),
        recommendationReproduction("C-0069", "the arm's plan margin [nm]", 0.0256091734, budget - arm),
        recommendationReproduction(
            "C-0069", "the end-condition budget c", 2.3416,
            bendingFactorForLength(budget, rigidity, perPath)
        ),
        recommendationReproduction("C-0069", "the tip-restraint ceiling [pN nm/rad]", 79.678, tipCeiling),
        recommendationReproduction("C-0069", "the root-restraint ceiling [pN nm/rad]", 13.930, rootCeiling),
        recommendationReproduction("C-0034", "the A2 couple [pN nm/rad]", 78.2352941, tipCouple),
        recommendationReproduction("C-0009", "the crossover hinge [pN nm/rad]", 13.5294118, hinge),
        recommendationReproduction("C-0069", "the two-support family's floor [nm]", 22.414, twoSupportFloor),
        recommendationReproduction("C-0069", "the axial length S/k [nm]", 1122.0, axialLength),
        recommendationReproduction("C-0069", "the entropic contour [nm]", 6.035, strandContour),
        recommendationReproduction(
            "C-0069", "the hinge lever sqrt(k_theta/k) [nm]", 3.715,
            hingeLeverForStiffness(hinge, perPath)
        ),
        recommendationReproduction("C-0069", "the standoff-headed crank [nm]", 5.331, crank),
        recommendationReproduction("CH-0081", "the rigid-root arm [nm]", 9.247, rigidRootArm),
        recommendationReproduction("C-0069", "the rooted element's kinematic floor [nm]", 4.50, kinematicFloor),
        recommendationReproduction("C-0069", "the per-path force at 3 nm [pN]", 2.94117647, perPathForce),
        recommendationReproduction(
            "C-0049", "the per-path secant ceiling n a/s [pN/nm]", PER_PATH_SECANT_CEILING,
            ARM_COUNT * UNZIP_ALLOWABLE / ACCEPTABLE_STROKE
        ),
        recommendationReproduction("C-0066", "the tip-to-neighbour clearance [nm]", 2.71561, pitch - arm),
        recommendationReproduction("C-0069", "the two-support family against the budget", 2.737, twoSupportFloor / budget),
        recommendationReproduction("CH-0081", "the rigid-root arm against the budget", 1.129, rigidRootArm / budget)
    )
    val worst = reproductions.maxByOrNull { it.departure }

    // ------------------------------------------------------------------------ convergence

    println("T-135 — the convergence records ...")
    fun armAt(steps: Int) = elasticaArmForStiffness(
        hingeStiffness = hinge, hingeCount = 1, farStiffness = tipCouple,
        bendingRigidity = rigidity, count = ARM_COUNT, targetStiffness = mandate,
        workingDisplacement = ACCEPTABLE_STROKE, steps = steps
    )
    val convergence = listOf(
        recommendationConvergence(
            "the hinge-rooted arm [nm]", "elastica RK4 steps 200 -> 800", armAt(200), armAt(800)
        ),
        recommendationConvergence(
            "the root-restraint ceiling [pN nm/rad]", "bisection resolution 1e-4 -> 1e-7",
            requireNotNull(
                nearRestraintCeiling(tipCouple, budget, rigidity, ARM_COUNT, mandate, resolution = 1.0e-4)
            ),
            requireNotNull(
                nearRestraintCeiling(tipCouple, budget, rigidity, ARM_COUNT, mandate, resolution = 1.0e-7)
            )
        ),
        recommendationConvergence(
            "the tip-restraint ceiling [pN nm/rad]", "bisection resolution 1e-4 -> 1e-7",
            requireNotNull(
                farRestraintCeiling(hinge, budget, rigidity, ARM_COUNT, mandate, resolution = 1.0e-4)
            ),
            requireNotNull(
                farRestraintCeiling(hinge, budget, rigidity, ARM_COUNT, mandate, resolution = 1.0e-7)
            )
        ),
        recommendationConvergence(
            "the plan budget [nm]", "a closed form — no resolution at all", budget, budget
        )
    )

    // ------------------------------------------------------------------------ the result

    val result = T135Result(
        task = "T-135",
        leaf = "A8.2",
        question = "Which output element does the Gen-1 programme recommend, and on what premises?",
        units = mapOf(
            "length" to "nm", "force" to "pN", "stiffness" to "pN/nm",
            "rotational stiffness" to "pN·nm/rad",
            "energy" to "pN·nm (k_BT = 4.141947 at 300 K)",
            "dishing" to "fraction of the free-tile stroke", "margin" to "dimensionless ratio"
        ),
        conventions = listOf(
            "x along the host sheet's helices, y across them, z normal and positive UPWARD",
            "a margin carries a SENSE: limit/value against a ceiling, value/limit against a floor",
            "a margin below 1.05 is NONE — the threshold is a DECLARED convention, chosen at the " +
                    "coarsest quantum this programme can build (one base-pair rise on the 8.16 nm " +
                    "member, 4.2 %)",
            "a premise is flagged by HOW IT WAS OBTAINED, never by how confident anyone is",
            "a failure route is a SINGLE standing result, reversed",
            "every number below is read at §3's ACCEPTABLE clause, because C-0050 shows the " +
                    "desired clause is unreachable on §3's own stack",
            "C-0017's mandate is a SUM, so the per-path secant is 33.3333/34 = 0.980392 pN/nm",
            "PASS means model-consistent and traceable. NOTHING HERE IS MEASURED and the MOTIF " +
                    "IS NOT DEMONSTRATED"
        ),
        parameters = mapOf(
            "pathCount" to ARM_COUNT.toString(),
            "targetForce" to TARGET_FORCE.toString(),
            "acceptableStroke" to ACCEPTABLE_STROKE.toString(),
            "desiredStroke" to DESIRED_STROKE.toString(),
            "rootPitchBasePairs" to UPWARD_ROOT_PITCH_BASE_PAIRS.toString(),
            "interhelicalDistance" to width.toString(),
            "risePerBasePair" to Gen1Tile.RISE_PER_BASE_PAIR.toString(),
            "duplexBendingRigidity" to rigidity.toString(),
            "duplexStretchModulus" to Gen1Tile.DUPLEX_STRETCH_MODULUS.toString(),
            "crossoverHingeStiffness" to hinge.toString(),
            "duplexEndCouple" to tipCouple.toString(),
            "standoffLength" to STANDOFF_LENGTH.toString(),
            "unzipAllowable" to UNZIP_ALLOWABLE.toString(),
            "worstStabilityFloorTwoMillimolar" to WORST_FLOOR_TWO_MILLIMOLAR.toString(),
            "assembledTangentMinimum" to ASSEMBLED_TANGENT_MINIMUM.toString(),
            "flatnessConvention" to FLATNESS_CONVENTION.toString(),
            "noMarginThreshold" to NO_MARGIN_THRESHOLD.toString(),
            "thinMarginThreshold" to THIN_MARGIN_THRESHOLD.toString()
        ),
        decidability = bound,
        candidates = candidates,
        recommended = recommended,
        rejected = rejected,
        premises = premises,
        premiseCounts = premiseCounts,
        margins = margins,
        marginCounts = marginCounts,
        conditionals = conditionals,
        conditionalCounts = conditionalCounts,
        failureRoutes = routes,
        failureRouteCounts = routeCounts,
        sectionSix = sectionSix(),
        openItems = openItems(),
        reproductions = reproductions,
        convergence = convergence,
        verdict = verdict(bound, recommended, premises, margins, conditionals, routes)
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-135-output-element-recommendation.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY
        )) + "\n"
    )
    report(result, worst, output)
}

// ---------------------------------------------------------------------------------------------
// the ledgers
// ---------------------------------------------------------------------------------------------

private fun premises(): List<RecommendationPremise> = listOf(
    RecommendationPremise(
        "M1",
        "a FREE LEVER may be held to a single-layer sheet by ONE crossover at the unused " +
                "out-of-plane azimuth. The site and a crossover on it are published (Ke et al., " +
                "JACS 131:15903, 2009, read directly); a free lever held there was NOT FOUND in " +
                "62 recorded queries",
        "C-0055",
        PremiseStatus.UNDEMONSTRATED,
        "the element itself — without it there is no recommendation at all"
    ),
    RecommendationPremise(
        "M2",
        "a duplex may stand NORMAL to a single-layer sheet on a clamped base. Every published " +
                "out-of-plane base on an origami body is a PIN, and the only rigid out-of-plane " +
                "mounting in print is triangulated",
        "C-0028, C-0029",
        PremiseStatus.UNDEMONSTRATED,
        "not the recommended element — the runner-up Q7 and the whole truss branch. It is a " +
                "premise of the DECISION (tie-break axis 1), not of the design"
    ),
    RecommendationPremise(
        "M3",
        "C-0009's crossover hinge constant, FITTED to IN-PLANE sheet crossovers, transfers to " +
                "the UNUSED OUT-OF-PLANE azimuth. C-0055 shows that site is in BETTER helical " +
                "register (4.286 deg against 8.571 deg) but no measurement covers its stiffness",
        "C-0009, C-0055, T-9",
        PremiseStatus.UNDEMONSTRATED,
        "the arm's length, through c^(1/3): at the top of Chen et al.'s own fitted bracket the " +
                "arm is 8.332 nm and places 30 of 34 instead of 34"
    ),
    RecommendationPremise(
        "M4",
        "a duplex END has exactly two strand termini on a 1.0 nm phosphate radius, so its " +
                "anchorage couple is 2 k_s a^2 = 78.2353 pN nm/rad and no force field can add a third",
        "C-0034, C-0029",
        PremiseStatus.DERIVED,
        "the tip joint, and the whole end-condition factor c = 2.32"
    ),
    RecommendationPremise(
        "M5",
        "the crossover hinge constant is 13.5294 pN nm/rad at alpha = 1, from a fitted bracket " +
                "alpha in [0.6, 1.2]",
        "C-0009, Chen et al., JACS 136:6995 (2014)",
        PremiseStatus.CITED_FITTED,
        "the root joint; the top of the bracket MOVES THE VERDICT (failure route R2)"
    ),
    RecommendationPremise(
        "M6",
        "the duplex bending rigidity is EI = 230 pN nm^2",
        "CanDo (Kim et al., NAR 40:2862, 2012), via C-0009",
        PremiseStatus.CITED_MODEL_INPUT,
        "the arm's length as EI^(1/3); Fields et al.'s implied 172.906 gives 7.883 nm and does " +
                "NOT move the verdict"
    ),
    RecommendationPremise(
        "M7",
        "the interhelical distance of a single-layer sheet is 2.69 nm",
        "SAXS, Fischer et al. 2016, via C-0009",
        PremiseStatus.CITED_MEASURED,
        "the plan budget pitch - d, hence the whole 0.0256 nm margin. The SAME paper's " +
                "square-lattice 2.73 nm takes the placement to 18 of 34 (failure route R1)"
    ),
    RecommendationPremise(
        "M8",
        "the rise is 0.34 nm/bp and a given interface is crossed every 32 bp, so the upward " +
                "root pitch is 10.88 nm",
        "Rothemund 2006, Ke et al. 2009, via C-0015 and C-0055",
        PremiseStatus.CITED_MEASURED,
        "the root lattice and therefore the plan budget"
    ),
    RecommendationPremise(
        "L1",
        "34 upward roots at crossover phase 24, four rows of three and eleven of two — the " +
                "self-consistent count, not a rounding of §3's 45",
        "C-0055, C-0063",
        PremiseStatus.DERIVED,
        "the path count, and through it the per-path secant and the arm's length"
    ),
    RecommendationPremise(
        "L2",
        "C-0053's footprint convention: a rooted element occupies [root, root +/- L] and the " +
                "next along the same row may start at high + d, i.e. consecutive collinear " +
                "elements need a full duplex of clearance",
        "C-0053",
        PremiseStatus.DERIVED,
        "the ENTIRE 0.0256 nm margin. At a zero-gap convention the budget is the bare 10.88 nm " +
                "and the margin 2.72 nm — the single largest lever on this recommendation"
    ),
    RecommendationPremise(
        "L3",
        "the plan budget of any rooted element on any 34-root placement is pitch - d = 8.19 nm, " +
                "exactly, because 3a + 2(15 - a) = 34 forces four rows of three",
        "C-0069, on C-0063's bound 1",
        PremiseStatus.DERIVED,
        "the ceiling every candidate is graded against; it survives any placement on this lattice"
    ),
    RecommendationPremise(
        "K1",
        "C-0017's 33.3333 pN/nm is §3's own 100 pN over 3 nm and is a SUM over the load paths",
        "C-0017, §3",
        PremiseStatus.SPECIFICATION,
        "the per-path secant, and hence every mechanism length in the census"
    ),
    RecommendationPremise(
        "K2",
        "C-0039's exact elastica, including its refusal below 1.5 x the stroke, where the tip " +
                "turns past 42 deg and the chord draw-in is a large fraction of the arm",
        "C-0039",
        PremiseStatus.DERIVED,
        "the arm's length, and the window's FLOOR at 4.50 nm — which is what refuses a pinned tip"
    ),
    RecommendationPremise(
        "K3",
        "C-0017's six 2 mM stability floors at the 10 nm layer, 23.414 to 27.913 pN/nm",
        "C-0017",
        PremiseStatus.CITED_MODEL_INPUT,
        "tie-break axis 2, and the recommendation's thinnest mechanical margin (1.0758). Each " +
                "floor is a mean-field solve inheriting C-0005's 123-214 % one-loop correction, " +
                "which is TWO ORDERS larger than that margin"
    ),
    RecommendationPremise(
        "K4",
        "the per-path allowable is 10 pN unzip, with 65 pN a hard ceiling because every origami " +
                "helix is nicked",
        "C-0006, CH-0029",
        PremiseStatus.CITED_MEASURED,
        "the force clause, which the recommendation clears by 3.4x on the mandate secant and " +
                "4.35x on C-0063's solved load. A rupture force is loading-rate dependent and " +
                "the 10 pN is quoted without one"
    ),
    RecommendationPremise(
        "K5",
        "the layer is 10 nm of PEG at sigma = 0.024 nm^-2 in 2 mM MgCl2, and C-0022's solved " +
                "edge load is the load the flatness is read under",
        "C-0001, C-0003, C-0022, §3",
        PremiseStatus.SPECIFICATION,
        "the flatness verdict entirely. C-0068 shows the equal-spring advantage belongs to the " +
                "10 nm layer: the SAME 34 roots dish 0.2000 at the 5 nm layer's own two states"
    ),
    RecommendationPremise(
        "K6",
        "T-5b's flatness convention: dishing below 0.10 of the free-tile stroke",
        "C-0006, T-5b",
        PremiseStatus.DERIVED,
        "the flatness verdict's threshold; it is a convention this programme declared"
    ),
    RecommendationPremise(
        "S1",
        "the design is written at §3's ACCEPTABLE clause because the desired ~10 nm stroke is " +
                "unreachable on §3's own stack — the stroke is L0 - h and §3 names no layer " +
                "taller than 10 nm",
        "C-0050",
        PremiseStatus.DERIVED,
        "which clause the whole recommendation discharges. At the desired clause NO rooted " +
                "element exists on this lattice at all, because a lever cannot stroke past its " +
                "own length and the budget is 8.19 nm"
    ),
    RecommendationPremise(
        "S2",
        "the operating buffer is §3's 2 mM MgCl2",
        "§3, C-0017, C-0032",
        PremiseStatus.SPECIFICATION,
        "where the six stability floors are read. Unlike C-0030's element the recommendation " +
                "clears 6 of 6 THERE — but its own pull-in FOLD has never been computed"
    ),
    RecommendationPremise(
        "S3",
        "the sheet is a 40 x 40 nm single-layer square-lattice Rothemund sheet of 15 duplexes",
        "§3, C-0009, C-0015",
        PremiseStatus.SPECIFICATION,
        "the 15 rows the 3a + 2(15 - a) = 34 arithmetic runs on, and the footprint the array packs into"
    )
)

@Suppress("LongParameterList")
private fun margins(
    arm: Double,
    budget: Double,
    hinge: Double,
    rootCeiling: Double,
    tipCouple: Double,
    tipCeiling: Double,
    pitch: Double,
    perPathForce: Double
): List<RecommendationMargin> = listOf(
    recommendationMargin(
        "the arm's plan length against the row-of-three budget", "C-0069", "plan",
        arm, budget, MarginSense.CEILING,
        "0.0256 nm, which is 0.075 of a base-pair rise. THE binding knife edge"
    ),
    recommendationMargin(
        "the tip joint against the end-condition ceiling", "C-0069", "joint",
        tipCouple, tipCeiling, MarginSense.CEILING,
        "C-0034's A2 was chosen because a duplex end has two strand termini, not for this"
    ),
    recommendationMargin(
        "the root joint against the end-condition ceiling", "C-0069", "joint",
        hinge, rootCeiling, MarginSense.CEILING,
        "C-0055 chose the root because it is the unused upward azimuth, not for this"
    ),
    recommendationMargin(
        "the arm against C-0039's 1.5 x stroke kinematic floor", "C-0069, C-0039", "kinematics",
        arm, 1.5 * ACCEPTABLE_STROKE, MarginSense.FLOOR,
        "the window is 4.50 ... 8.19 nm, 1.82x wide, and the arm sits near its ceiling"
    ),
    recommendationMargin(
        "the assembled tangent minimum against C-0017's worst 2 mM floor", "C-0069, C-0017",
        "stability", ASSEMBLED_TANGENT_MINIMUM, WORST_FLOOR_TWO_MILLIMOLAR, MarginSense.FLOOR,
        "6 of 6 floors cleared, but the worst by 7.6 % against a floor carrying C-0005's " +
                "123-214 % one-loop correction"
    ),
    recommendationMargin(
        "the per-path force at 3 nm against the unzip allowable", "C-0069, C-0006", "force",
        perPathForce, UNZIP_ALLOWABLE, MarginSense.CEILING,
        "on the mandate secant; the allowable is CITED and carries no loading rate"
    ),
    recommendationMargin(
        "C-0063's worst SOLVED per-path force against the unzip allowable", "C-0063", "force",
        WORST_SOLVED_PATH_FORCE, UNZIP_ALLOWABLE, MarginSense.CEILING,
        "under C-0022's solved edge load rather than on the mandate secant"
    ),
    recommendationMargin(
        "C-0063's worst crossover force against the unzip allowable", "C-0063", "force",
        WORST_CROSSOVER_FORCE, UNZIP_ALLOWABLE, MarginSense.CEILING,
        "8.3x the 3 x 15 grid's, exactly as C-0061 warned, and still 8x clear"
    ),
    recommendationMargin(
        "the per-path secant against C-0049's n a/s ceiling", "C-0049", "compliance",
        Gen1Tile.TARGET_FORCE / ACCEPTABLE_STROKE, PER_PATH_SECANT_CEILING, MarginSense.CEILING,
        "the same 3.4x as the force clause, because both are the unzip allowable"
    ),
    recommendationMargin(
        "the dishing at C-0063's design state against T-5b's convention", "C-0063", "flatness",
        DISHING_AT_DESIGN_STATE, FLATNESS_CONVENTION, MarginSense.CEILING,
        "34 EQUAL springs; no distribution at all"
    ),
    recommendationMargin(
        "the dishing over the device's own RANGE against T-5b's convention", "C-0068", "flatness",
        DISHING_OVER_RANGE, FLATNESS_CONVENTION, MarginSense.CEILING,
        "worst of the three buffers (10 mM); 0.0789 at 2 mM and 0.0853 at 0.5 mM. The range " +
                "spends the margin: 1.42x at the design state becomes 1.12x"
    ),
    recommendationMargin(
        "the tie registration the design USES — a tie on the arm's own tip", "C-0066",
        "registration", TIE_DEMAND, pitch - arm, MarginSense.CEILING,
        "the tie lands on the arm's A2 end, so it needs half a duplex against the 2.71561 nm " +
                "the lattice leaves. NOT a knife edge"
    ),
    recommendationMargin(
        "the tie registration the design does NOT use — a FREE-STANDING tie between two arms",
        "C-0066", "registration", OrigamiDuplex.INTERHELICAL, pitch - arm, MarginSense.CEILING,
        "the same 0.0256 nm as the plan margin, and the SAME lattice quantity. Discharged by " +
                "the tip registration, and recorded so that it is not double-counted"
    ),
    recommendationMargin(
        "§3's DESIRED 10 nm stroke against the arm's own length", "C-0050, C-0066", "kinematics",
        DESIRED_STROKE, arm, MarginSense.CEILING,
        "VIOLATED, and deliberately: a lever cannot deliver a stroke longer than itself, which " +
                "is C-0050's kinematic ceiling arriving from a plan view"
    )
)

private fun conditionals(): List<RecommendationConditional> = listOf(
    RecommendationConditional(
        "T-63", "Is 0.5 mM MgCl2 acceptable as the Gen-1 nominal buffer?", "C-0012 ... C-0032",
        ConditionalStatus.BINDING,
        "the requirement C-0032 raised does NOT transfer: it was measured on C-0030's " +
                "strain-SOFTENING flexure (22.88 pN/nm against a 23.41-27.91 floor), and the " +
                "recommended array's own tangent minimum is 30.03 and clears 6 of 6 at 2 mM. " +
                "What has NOT been computed is this element's own pull-in FOLD, and CLAUDE.md " +
                "forbids transferring a fold margin between load lines",
        "at 0.5 mM every floor falls to 3.86-15.94 and the fold disappears entirely, so the " +
                "answer can only help; the question binds because the 2 mM fold is unknown, " +
                "not because 2 mM is known to fail"
    ),
    RecommendationConditional(
        "P-13", "What is the electrode made of, and what is its potential of zero charge?",
        "C-0021",
        ConditionalStatus.BINDING,
        "metal against oxide is 2.6x on the one hold-down that cannot be designed away, and a " +
                "contact potential of 0.9-5.1 mV supplies the whole thermal-scale hold-down. No " +
                "§3 row fixes it and no element choice can",
        "it moves the zero-bias resting position, which is where the stroke is measured FROM " +
                "(C-0031): 0.07-0.38 nm against a 33 pN/nm coupling"
    ),
    RecommendationConditional(
        "T-115", "May the polymer layer be taller than 10 nm — 17-26 nm?", "C-0050, C-0068",
        ConditionalStatus.BINDING,
        "the recommendation's flatness belongs to the 10 nm layer, and C-0068 shows THE LAYER " +
                "SELECTS THE PHASE — 24 for 10 nm, 8 for 5 nm — with the two argmins mutually " +
                "poor. A 5 nm layer makes the same 34 roots dish 0.2000, 2.0x outside T-5b",
        "a 17-26 nm layer is the only thing that buys §3's DESIRED stroke, and it re-opens " +
                "C-0002's crossover, C-0005's screening validity, C-0007's drainage and " +
                "C-0012's bias — none of which has been evaluated there"
    ),
    RecommendationConditional(
        "T-112", "Which device does §3's DESIRED clause ask for?", "CH-0059, C-0046, C-0050",
        ConditionalStatus.BINDING,
        "the recommendation discharges the ACCEPTABLE clause and nothing more. §3's desired " +
                "clause asks for a different coupling (100/10 = 10 pN/nm) which C-0017's own " +
                "stability floor refuses at 2.34-2.79x",
        "if the desired clause is a requirement rather than an aspiration, no element in this " +
                "programme discharges it and the recommendation must be withdrawn as a whole"
    ),
    RecommendationConditional(
        "T-95", "May the output superstructure be perforated under each flexure midspan?",
        "C-0035, C-0066",
        ConditionalStatus.DISCHARGED_BY_THIS_ELEMENT,
        "the question was raised for C-0035's mounting, whose tie must cross a STANDOFF BASE " +
                "PLANE at a FLEXURE MIDSPAN. The recommendation has neither: C-0066's " +
                "registration puts the tie on the arm's own A2 end, so no tie has to reach the " +
                "tile and the section theorem lapses",
        "it re-binds immediately if the two-support flexure branch is revived — which CH-0081 " +
                "and C-0069's 2.74x say it should not be"
    ),
    RecommendationConditional(
        "T-102", "May §3's tile grow by 1.44x in area?", "C-0041, C-0022",
        ConditionalStatus.DISCHARGED_BY_THIS_ELEMENT,
        "the growth was priced for C-0041's FLEXURE array at the DESIRED stroke, where the " +
                "path-count window on a 40 x 40 nm tile is empty. The recommended arm places 34 " +
                "instances at one level on the Gen-1 footprint at the acceptable clause",
        "it re-binds at §3's own 45 paths, where the arm is 9.131 nm and places 24 of 34 " +
                "(failure route R4)"
    )
)

private fun failureRoutes(): List<RecommendationFailureRoute> = listOf(
    failureRoute(
        "R1",
        "the interhelical distance for THIS sheet being the square lattice's 2.73 nm rather " +
                "than the single-layer 2.69 nm",
        "C-0069's sensitivity table; Fischer et al. 2016 (SAXS) supplies both numbers",
        RouteEffect.REMOVES_THE_ELEMENT, RouteDecider.MEASUREMENT,
        insidePublishedBracket = true,
        consequence = "the budget falls to 8.15 nm and the arm places 18 of 34"
    ),
    failureRoute(
        "R2",
        "the crossover hinge constant sitting at the TOP of Chen et al.'s own fitted bracket, " +
                "alpha = 1.2",
        "C-0069's sensitivity table; C-0009, Chen et al. 2014",
        RouteEffect.REMOVES_THE_ELEMENT, RouteDecider.MEASUREMENT,
        insidePublishedBracket = true,
        consequence = "the arm is 8.332 nm and places 30 of 34 — the positive verdict becomes negative"
    ),
    failureRoute(
        "R3",
        "any tolerance or scatter model on the plan positions",
        "C-0069's and C-0066's own Still-open items; T-134",
        RouteEffect.REMOVES_THE_ELEMENT, RouteDecider.CALCULATION,
        insidePublishedBracket = false,
        consequence = "0.0256 nm is 0.075 of a base-pair rise and any real scatter erases it. " +
                "Nothing in this programme has ever modelled scatter"
    ),
    failureRoute(
        "R4",
        "§3's 45 load paths being mandated rather than C-0055's self-consistent 34",
        "C-0069's sensitivity table; C-0015",
        RouteEffect.REMOVES_THE_ELEMENT, RouteDecider.SPECIFICATION,
        insidePublishedBracket = false,
        consequence = "more paths make each element LONGER (L ~ n^(1/3)): 9.131 nm, 24 of 34"
    ),
    failureRoute(
        "R5",
        "the motif being unbuildable — a free lever held to a single-layer sheet by ONE crossover",
        "C-0055, 62 recorded queries",
        RouteEffect.REMOVES_THE_ELEMENT, RouteDecider.EXPERIMENT,
        insidePublishedBracket = false,
        consequence = "there is no element, and no calculation in this repository can close it. " +
                "It is the only route of the five that a simulation cannot settle"
    ),
    failureRoute(
        "R6",
        "the layer being 5 nm rather than 10 nm",
        "C-0068, CH-0080",
        RouteEffect.REMOVES_A_PREMISE, RouteDecider.SPECIFICATION,
        insidePublishedBracket = false,
        consequence = "the same 34 roots dish 0.2000 against T-5b's 0.10 and are worse than no " +
                "coupling at all at both of that device's states; the phase would have to move " +
                "from 24 to 8, and phase 8's best dishes 0.2416 at the 10 nm design state"
    ),
    failureRoute(
        "R7",
        "this element's own pull-in fold collapsing at 2 mM as C-0030's did",
        "C-0032, C-0018 — NOT computed for this element",
        RouteEffect.REMOVES_A_PREMISE, RouteDecider.CALCULATION,
        insidePublishedBracket = false,
        consequence = "the 2 mM operating point goes and 0.5 mM becomes a requirement again. " +
                "The element is strain-STIFFENING at the placement stroke, which is the " +
                "favourable side of C-0032's own test — but that is a direction, not a bound"
    ),
    failureRoute(
        "R8",
        "C-0053's footprint convention being relaxed to zero clearance between collinear elements",
        "C-0053, inherited by C-0055, C-0063, C-0065, C-0066 and C-0069 alike",
        RouteEffect.CHANGES_UNIQUENESS, RouteDecider.CALCULATION,
        insidePublishedBracket = false,
        consequence = "the budget rises from 8.19 to the bare 10.88 nm, the margin from 0.026 " +
                "to 2.72 nm, and the RIGID-root arm at 9.247 nm then places — so CH-0081 lapses " +
                "and the truss branch re-enters the output role. It would move all six claims, " +
                "not just this one"
    ),
    failureRoute(
        "R9",
        "a compliance mechanism in DNA that is neither entropic nor bending",
        "C-0023's two-mechanism statement, re-measured in C-0069's six-row census",
        RouteEffect.CHANGES_UNIQUENESS, RouteDecider.CALCULATION,
        insidePublishedBracket = false,
        consequence = "the census would be incomplete and the element space would have to be " +
                "re-enumerated; it would not by itself remove the recommended element"
    )
)

private fun sectionSix(): List<RecommendationSectionSixVerdict> = listOf(
    RecommendationSectionSixVerdict(
        "1 — stiffness of the polymer layer",
        "number with stated model, parameters and validity range; sensitivity to grafting density",
        "PASS, then superseded twice", "C-0001 -> C-0003 -> C-0011",
        "nothing — the recommendation consumes the layer, it does not change it"
    ),
    RecommendationSectionSixVerdict(
        "2 — feasible design window",
        "non-empty region satisfying §4(a)-(d) simultaneously, or a proof of emptiness naming " +
                "the binding constraint",
        "PASS — non-empty at 7 and 10 nm, empty at 5 nm",
        "C-0016, C-0017, C-0032, C-0051",
        "the window's output-coupling axis stops being hypothetical: it now has a NAMED element " +
                "with a placement, 34 paths at 0.980392 pN/nm each. It does not move an edge — " +
                "the upper edge is a DEAD-LOAD stroke and no coupling choice reaches it"
    ),
    RecommendationSectionSixVerdict(
        "3 — stroke and blocking force vs bias, including ionic screening",
        "stroke >= ~3 nm and force >= 100 pN at <= 2 V, or a demonstration that it is unreachable",
        "PASS on the acceptable clause, and the DESIRED ~10 nm clause is demonstrated " +
                "UNREACHABLE on §3's own stack — which is the predicate's own second branch, " +
                "stated plainly as §7 asks",
        "C-0008, C-0012, C-0050, C-0069",
        "supplies the element that discharges the placement clause, and corroborates the " +
                "negative half from a third direction: the plan budget is 8.19 nm and a lever " +
                "cannot deliver a stroke longer than its own length, so at the desired clause " +
                "NO rooted element exists on this lattice at all"
    ),
    RecommendationSectionSixVerdict(
        "4 — electrostatic softening and pull-in",
        "either a maximum usable bias with margin to the operating point, or a demonstration " +
                "that the osmotic divergence removes the instability",
        "PASS, both branches answered, each for a different load line",
        "C-0018, CH-0017, C-0032",
        "NOT re-run for this element. C-0032's fold was computed on C-0030's flexure, and a " +
                "fold margin does not transfer between load lines. THIS IS OPEN ITEM 1 and it " +
                "is the largest thing this recommendation does not know"
    ),
    RecommendationSectionSixVerdict(
        "5 — load distribution across the origami",
        "peak per-load-path force reported against the 35-60 pN disassembly band, distributed " +
                "and concentrated attachment treated separately",
        "PASS", "C-0006, C-0009, C-0063",
        "the recommended array's own worst path is 2.298 pN under C-0022's SOLVED load (2.941 " +
                "on the mandate secant) and its worst crossover 1.246 pN — 8x clear of the 10 " +
                "pN unzip allowable and an order below the 35-60 pN band"
    ),
    RecommendationSectionSixVerdict(
        "5b — deflected shape of the tile",
        "deformation amplitude reported against the stroke; rigid-plate assumption upheld or " +
                "rejected, with consequences for force transfer and sensing",
        "PASS, verdict RIGID PLATE REJECTED", "C-0006, C-0009, C-0063, C-0068",
        "this is where the recommendation contributes most: it is the first Gen-1 design in " +
                "the programme whose flatness stands on a placement a claim actually supplies — " +
                "0.0706 at the design state and 0.0789-0.0896 over the range, all inside " +
                "T-5b's 0.10, with 34 EQUAL springs and no distribution"
    ),
    RecommendationSectionSixVerdict(
        "6 — validity boundary of mean-field screening at 2 mM Mg2+",
        "quantified deviation from mean-field, with the boundary stated", "PASS",
        "C-0005, C-0008",
        "nothing — but it is what makes the recommendation's stability margin unquotable as an " +
                "establishment: C-0005's 123-214 % one-loop correction is two orders larger " +
                "than the 7.6 % by which the array clears its worst floor"
    ),
    RecommendationSectionSixVerdict(
        "7 — poroelastic drainage time vs thickness and volume fraction",
        "bounded, with the conditions under which it would constrain >= 1 kHz operation stated",
        "PASS — not binding, boundary named", "C-0004",
        "nothing"
    ),
    RecommendationSectionSixVerdict(
        "8 — tile positional variance at 300 K",
        "sigma_RMS <= 3.0 nm for the nominal Gen-1 tile",
        "PASS at the operating point, PARTIAL against leaf A1.2", "C-0010",
        "the array adds 0.346 pN of C-0014 thermal force per path (C-0063), which is inside " +
                "every allowable; the variance verdict itself is untouched"
    )
)

private fun openItems(): List<String> = listOf(
    "1. THE PULL-IN FOLD HAS NEVER BEEN COMPUTED FOR THIS ELEMENT. C-0032 computed one for " +
            "C-0030's flexure and found the 10 nm / 2 mM bias margin collapse to 1.0000-1.0019. " +
            "The recommended array is strain-STIFFENING at the placement stroke, which is the " +
            "favourable side of that test, but CLAUDE.md's own rule forbids transferring a fold " +
            "margin between load lines. This is the largest single thing the recommendation " +
            "does not know, and it is a calculation, not a measurement",
    "2. The 0.0256 nm plan margin against a tolerance model — T-134, running concurrently. " +
            "This synthesis hands it one finding: the two 0.0256 nm quantities in circulation " +
            "are ONE lattice number, pitch - d - L, and only the PLAN one binds. C-0066's " +
            "free-standing-tie reading is discharged by the tip registration, which has 2.02x",
    "3. Neither of the recommendation's two joints has a margin, and neither was chosen for " +
            "the bound it sits inside. T-9's k_theta and k_s are unmeasured, and the top of " +
            "the fitted bracket moves the verdict",
    "4. The flatness of the array has been solved at C-0063's phase-24 placement and over its " +
            "own device's range (C-0068), but NOT with the arms' own mass and stiffness in the " +
            "grillage as a solved body at every state — C-0061/T-121 did that at one state",
    "5. Whether a different 34-root placement admits a longer element. The row-of-three bound " +
            "says no on this lattice; a different COUNT would re-open it, and a different count " +
            "re-solves C-0055's self-consistency",
    "6. The recommendation has no sequence design and no yield estimate. Ke et al. report the " +
            "8 bp staple break the site forces as a YIELD cost, and nothing here prices it"
)

@Suppress("LongParameterList")
private fun verdict(
    bound: RecommendationDecidability,
    recommended: T135RecommendedElement,
    premises: List<RecommendationPremise>,
    margins: List<RecommendationMargin>,
    conditionals: List<RecommendationConditional>,
    routes: List<RecommendationFailureRoute>
): Map<String, String> {
    val undemonstrated = premises.filter { it.status == PremiseStatus.UNDEMONSTRATED }
    val noMargin = margins.filter { it.classification == MarginClass.NONE }
    val removers = routes.filter { it.effect == RouteEffect.REMOVES_THE_ELEMENT }
    val bracketed = removers.filter { it.insidePublishedBracket }
    return mapOf(
        "T-135 — the recommendation" to
                ("YES, the programme can recommend, and it recommends ${bound.winner} — " +
                        "${recommended.name}, ${"%.5f".format(recommended.length)} nm = " +
                        "${"%.1f".format(recommended.lengthBasePairs)} bp, rooted on one " +
                        "antiparallel crossover at C-0055's unused out-of-plane azimuth and " +
                        "tipped on C-0034's A2 duplex end, ${recommended.pathCount} instances at " +
                        "one level, at §3's ACCEPTABLE clause"),
        "T-135 — why it is decidable NOW" to
                ("the cheap bound: ${bound.catalogueSize} catalogued elements, " +
                        "${bound.placeAtOneLevel} place all 34 at one level, ${bound.survivors} " +
                        "survive every clause, and the three tie-break axes — each an integer " +
                        "count owned by a standing claim, none of them a new result — are " +
                        "${if (bound.unanimous) "UNANIMOUS" else "IN DISAGREEMENT"}. The " +
                        "declared falsifier ${if (bound.falsifierFired) "FIRED" else "did not fire"}"),
        "T-135 — the premises" to
                ("${premises.size} premises, of which ${undemonstrated.size} are " +
                        "UNDEMONSTRATED: " + undemonstrated.joinToString("; ") { it.id } +
                        ". M1 is upstream of the element itself, M3 of its length, and M2 of " +
                        "the DECISION rather than of the design"),
        "T-135 — where there is no margin" to
                ("${noMargin.size} of ${margins.size} graded quantities have NO margin at all " +
                        "(< ${NO_MARGIN_THRESHOLD}x): " +
                        noMargin.joinToString("; ") {
                            "${it.quantity} at ${"%.5f".format(it.margin)}x"
                        } +
                        ". Three of them are ONE arithmetic — pitch - d - L — and the fourth, " +
                        "the free-standing tie, is discharged by the registration the design uses"),
        "T-135 — the specification conditionals" to
                ("${conditionals.count { it.status == ConditionalStatus.BINDING }} still bind (" +
                        conditionals.filter { it.status == ConditionalStatus.BINDING }
                            .joinToString(", ") { it.id } +
                        ") and ${conditionals.count { it.status == ConditionalStatus.DISCHARGED_BY_THIS_ELEMENT }} " +
                        "are DISCHARGED BY THIS ELEMENT (" +
                        conditionals.filter { it.status == ConditionalStatus.DISCHARGED_BY_THIS_ELEMENT }
                            .joinToString(", ") { it.id } +
                        ") — both of them raised by the flexure-and-tie branch that CH-0081 and " +
                        "C-0069 remove from the output role. A window loses an axis when a " +
                        "constraint is discharged, and an intersection records neither"),
        "T-135 — the failure routes" to
                ("${routes.size} routes: ${removers.size} REMOVE THE ELEMENT, " +
                        "${routes.count { it.effect == RouteEffect.REMOVES_A_PREMISE }} remove a " +
                        "PREMISE, ${routes.count { it.effect == RouteEffect.CHANGES_UNIQUENESS }} " +
                        "change its UNIQUENESS. Of the ${removers.size} removers, " +
                        "${bracketed.size} are ALREADY INSIDE A PUBLISHED BRACKET (" +
                        bracketed.joinToString(", ") { it.id } + ") — not hypotheses at all — " +
                        "and ${removers.count { it.decidedBy == RouteDecider.EXPERIMENT }} can " +
                        "be settled only at a bench"),
        "T-135 — what the recommendation is NOT" to
                ("it is not a demonstration. TRL 1-3: nothing here is measured, the motif is " +
                        "not in the literature, and the design has no margin on either of its " +
                        "two joints. It is the element that survives every clause this " +
                        "programme has written, and it is offered as that and nothing more"),
        "§6 verdict" to
                ("no §6 verdict moves. The recommendation contributes to task 5b (the first " +
                        "flat Gen-1 design standing on a placement a claim supplies), to task 3 " +
                        "(a named element for the acceptable clause, and a third corroboration " +
                        "of the desired clause's negative) and to task 5 (2.298 pN worst solved " +
                        "path). Task 4 is the one it does NOT discharge for itself")
    )
}

private fun report(
    result: T135Result,
    worst: RecommendationReproduction?,
    output: File
) {
    println()
    println("T-135 — which output element does the Gen-1 programme recommend?")
    println()
    println("  decidability: ${result.decidability.catalogueSize} catalogued, " +
            "${result.decidability.placeAtOneLevel} place at one level, " +
            "${result.decidability.survivors} survive every clause")
    result.decidability.axes.forEach {
        println("    axis '${it.axis}': ${it.values} -> winner ${it.winner}")
    }
    println("    unanimous: ${result.decidability.unanimous}, winner: ${result.decidability.winner}, " +
            "falsifier fired: ${result.decidability.falsifierFired}")
    println()
    println("  recommended: ${result.recommended.id} — ${result.recommended.name}")
    println("    length ${"%.5f".format(result.recommended.length)} nm against a " +
            "${"%.2f".format(result.recommended.planBudget)} nm budget, margin " +
            "${"%.5f".format(result.recommended.planMargin)} nm")
    println("    root ${"%.4f".format(result.recommended.rootStiffness)} against a ceiling of " +
            "${"%.4f".format(result.recommended.rootCeiling)}; tip " +
            "${"%.4f".format(result.recommended.tipStiffness)} against " +
            "${"%.4f".format(result.recommended.tipCeiling)}")
    println()
    println("  premises: ${result.premises.size}, by status ${result.premiseCounts}")
    println("  margins: ${result.margins.size}, by band ${result.marginCounts}")
    result.margins.filter { it.classification == MarginClass.NONE }.forEach {
        println("    NO MARGIN: ${it.quantity} — ${"%.5f".format(it.margin)}x")
    }
    println("  conditionals: ${result.conditionalCounts}")
    println("  failure routes: ${result.failureRoutes.size} — ${result.failureRouteCounts}")
    println()
    println("  worst upstream reproduction: ${worst?.owner} ${worst?.quantity} at " +
            "${worst?.departure}")
    println("  worst convergence departure: " +
            "${result.convergence.maxOfOrNull { it.departure }}")
    println()
    result.verdict.forEach { (key, value) -> println("  $key:\n    $value\n") }
    println("  written to $output")
}
