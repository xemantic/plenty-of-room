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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.ShearJointAllowable
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

/**
 * Task `T-79` / leaf `A8.2` — **a large-rotation two-spring elastica for `E5`'s arm**: the
 * composition that is exact in the rotation *and* in the end condition, which neither of the two
 * readings that bracket the arm at 11.03–12.50 nm is.
 *
 * ```shell
 * tools/study.sh anchoring.TwoSpringElasticaStudyKt
 * ```
 *
 * Emits `gpd/results/T-79-two-spring-elastica.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val UNREACHABLE = -1.0

private fun finite(value: Double): Double =
    if (value.isFinite()) value else UNREACHABLE

private const val TARGET_FORCE = 100.0
private const val ACCEPTABLE_STROKE = 3.0
private const val DESIRED_STROKE = 10.0
private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE
private const val PATH_COUNT = 45
private const val COMPLIANT_CEILING = 40.0
private const val UNZIP_ALLOWABLE = 10.0
private const val DESIGN_HINGE_COUNT = 16
private const val STEPS = 400

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val HINGE = Gen1Tile.crossoverHingeStiffness()
private val ALLOWABLE = ShearJointAllowable()

/** The adopted anchorage: `C-0034`'s `A2`, the arm's own duplex end at the phosphate radius. */
private val ADOPTED = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS)

private val ANCHORAGES: List<ArmAnchorage> = listOf(
    ArmAnchorage.idealGuide(),
    ArmAnchorage.singleLink(),
    ADOPTED,
    ArmAnchorage.twoTerminus(
        BForm.PHOSPHATE_RADIUS * kotlin.math.sin(BForm.MINOR_GROOVE_BACKBONE_ANGLE * PI / 360.0)
    ),
    ArmAnchorage.nickedContinuation(),
    ArmAnchorage.multiCrossoverClamp(2)
)

// ---------------------------------------------------------------------------------------------

@Serializable
data class T79ContinuumRecord(
    val nearRestraint: Double,
    val farRestraint: Double,
    val closedFormFactor: Double,
    val elasticaVanishingLoadFactor: Double,
    val relativeDeparture: Double
)

@Serializable
data class T79PlacementRecord(
    val anchorageId: String,
    val anchorageName: String,
    val hingeCount: Int,
    val farStiffness: Double,
    val armLength: Double,
    val armBasePairs: Double,
    val armCeiling: Double,
    val farRestraint: Double,
    val vanishingLoadFactor: Double,
    val secantAtAcceptable: Double,
    val tangentAtAcceptable: Double,
    val tangentToSecantAtAcceptable: Double,
    val reachesDesiredStroke: Boolean,
    val secantAtDesired: Double,
    val tangentAtDesired: Double,
    val nearRotationDesiredDegrees: Double,
    val farRotationDesiredDegrees: Double,
    val drawInAtAcceptable: Double,
    val drawInAtDesired: Double,
    val usableStrokeInsideCeiling: Double,
    val insideCeilingAtAcceptable: Boolean,
    val insideCeilingAtDesired: Boolean,
    val verdict: String
)

@Serializable
data class T79StrokeRecord(
    val stroke: Double,
    val forcePerPath: Double,
    val assembledForce: Double,
    val secant: Double,
    val tangent: Double,
    val tangentToSecant: Double,
    val nearRotationDegrees: Double,
    val farRotationDegrees: Double,
    val drawIn: Double,
    val drawInChordBound: Double,
    val drawInBasePairs: Double,
    val heldTensionBound: Double,
    val hingeBondForce: Double,
    val anchorageLinkForce: Double,
    val anchorageBondedLength: Double,
    val insideCompliantCeiling: Boolean,
    val insideUnzipAllowable: Boolean
)

@Serializable
data class T79BracketRecord(
    val reading: String,
    val exactRotation: Boolean,
    val exactEndCondition: Boolean,
    val armLength: Double,
    val armBasePairs: Double,
    val realisedFactor: Double,
    val insideC0034Bracket: Boolean,
    val relativeToLongEnd: Double
)

@Serializable
data class T79SensitivityRecord(
    val axis: String,
    val label: String,
    val value: Double,
    val farStiffness: Double,
    val armLength: Double,
    val armCeiling: Double,
    val tangentAtAcceptable: Double,
    val usableStrokeInsideCeiling: Double,
    val reachesDesiredStroke: Boolean,
    val clearsDesiredStrokeInsideCeiling: Boolean
)

@Serializable
data class T79ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T79ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class T79Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val cheapBounds: Map<String, Double>,
    val continuum: List<T79ContinuumRecord>,
    val placements: List<T79PlacementRecord>,
    val strokes: List<T79StrokeRecord>,
    val bracket: List<T79BracketRecord>,
    val sensitivities: List<T79SensitivityRecord>,
    val convergence: List<T79ConvergenceRecord>,
    val reproductions: List<T79ReproductionRecord>,
    val findings: Map<String, String>
)

// ---------------------------------------------------------------------------------------------

private fun beamAt(arm: Double, hingeCount: Int, far: Double, steps: Int = STEPS) =
    TwoSpringElastica(EI, arm, hingeCount * HINGE, far, steps)

/**
 * The largest stroke at which [beam] still holds `count` of itself inside [ceiling] pN/nm on the
 * **tangent** — the quantity `C-0023`'s compliance clause actually asks for once the element
 * stiffens, and the one `C-0034`'s linear reading could not produce.
 */
private fun usableStroke(
    beam: TwoSpringElastica,
    count: Int = PATH_COUNT,
    ceiling: Double = COMPLIANT_CEILING
): Double {
    val top = min(0.9 * beam.length, DESIRED_STROKE)
    fun excess(stroke: Double): Double = count * beam.tangentStiffness(stroke) - ceiling
    // Scanned from BELOW, never evaluated at the geometric limit first: past a right angle the
    // elastica's shooting residual stops being monotone, and the ceiling is crossed long before
    // that on every design here. `C-0012`'s "scan for the first sign change, then bisect".
    val samples = 90
    var lastGood = 1.0e-3
    var atLastGood = excess(lastGood)
    if (atLastGood >= 0.0) return 0.0
    for (i in 1..samples) {
        val stroke = top * i / samples
        val here = runCatching { excess(stroke) }.getOrNull() ?: return lastGood
        if (here >= 0.0) return illinoisRoot(lastGood, stroke, atLastGood, here) { excess(it) }
        lastGood = stroke
        atLastGood = here
    }
    return top
}

private fun strokeRecord(
    beam: TwoSpringElastica,
    hingeCount: Int,
    stroke: Double
): T79StrokeRecord {
    val state = beam.stateAtDisplacement(stroke)
    val secant = PATH_COUNT * beam.secantStiffness(stroke)
    val tangent = PATH_COUNT * beam.tangentStiffness(stroke)
    val linkForce = farAnchorageLinkForce(abs(state.farMoment), BForm.PHOSPHATE_RADIUS)
    val hingeForce = beam.hingeBondForce(stroke, hingeCount)
    return T79StrokeRecord(
        stroke = stroke,
        forcePerPath = state.force,
        assembledForce = PATH_COUNT * state.force,
        secant = secant,
        tangent = tangent,
        tangentToSecant = tangent / secant,
        nearRotationDegrees = state.nearRotation * 180.0 / PI,
        farRotationDegrees = state.farRotation * 180.0 / PI,
        drawIn = state.drawIn,
        drawInChordBound = chordDrawInBound(beam.length, stroke),
        drawInBasePairs = state.drawIn / RISE,
        heldTensionBound = restrainedTensionBound(beam.length, stroke),
        hingeBondForce = hingeForce,
        anchorageLinkForce = linkForce,
        anchorageBondedLength =
            if (linkForce > 0.0 && linkForce < ALLOWABLE.saturationForce)
                bondedLengthForTension(linkForce, ALLOWABLE)
            else UNREACHABLE,
        insideCompliantCeiling = tangent <= COMPLIANT_CEILING,
        insideUnzipAllowable = maxOf(hingeForce, linkForce, state.force) <= UNZIP_ALLOWABLE
    )
}

private fun placementRecord(
    anchorage: ArmAnchorage,
    hingeCount: Int
): T79PlacementRecord {
    val far = anchorage.rotationalStiffness
    val arm = elasticaArmForStiffness(
        HINGE, hingeCount, far, EI, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, STEPS
    )
    val cap = elasticaArmCeiling(far, PATH_COUNT, EI, MANDATE, ACCEPTABLE_STROKE, STEPS)
    val beam = beamAt(arm, hingeCount, far)
    val acceptable = strokeRecord(beam, hingeCount, ACCEPTABLE_STROKE)
    val desired =
        if (arm > DESIRED_STROKE) {
            runCatching { strokeRecord(beam, hingeCount, DESIRED_STROKE) }.getOrNull()
        } else null
    val reaches = desired != null
    val usable = usableStroke(beam)
    val verdict = when {
        arm <= DESIRED_STROKE -> "FAILS the desired stroke: the arm is shorter than 10 nm"
        !reaches -> "FAILS the desired stroke: the arm folds before reaching it"
        desired != null && !desired.insideCompliantCeiling ->
            "places, but past the 40 pN/nm ceiling at the desired stroke"
        else -> "places and holds the ceiling at both strokes"
    }
    return T79PlacementRecord(
        anchorageId = anchorage.id,
        anchorageName = anchorage.name,
        hingeCount = hingeCount,
        farStiffness = finite(far),
        armLength = arm,
        armBasePairs = arm / RISE,
        armCeiling = cap,
        farRestraint = finite(beam.farRestraint),
        vanishingLoadFactor = beam.smallRotationArmFactor,
        secantAtAcceptable = acceptable.secant,
        tangentAtAcceptable = acceptable.tangent,
        tangentToSecantAtAcceptable = acceptable.tangentToSecant,
        reachesDesiredStroke = reaches,
        secantAtDesired = desired?.secant ?: UNREACHABLE,
        tangentAtDesired = desired?.tangent ?: UNREACHABLE,
        nearRotationDesiredDegrees = desired?.nearRotationDegrees ?: UNREACHABLE,
        farRotationDesiredDegrees = desired?.farRotationDegrees ?: UNREACHABLE,
        drawInAtAcceptable = acceptable.drawIn,
        drawInAtDesired = desired?.drawIn ?: UNREACHABLE,
        usableStrokeInsideCeiling = usable,
        insideCeilingAtAcceptable = acceptable.insideCompliantCeiling,
        insideCeilingAtDesired = desired?.insideCompliantCeiling ?: false,
        verdict = verdict
    )
}

// ---------------------------------------------------------------------------------------------

fun main() {
    // ------------------------------------------------------------------ the cheap bounds
    val shortEnd = 11.028
    val longEnd = 12.496
    val cheapBounds = mapOf(
        "chordDrawInBoundShortArmAcceptable" to chordDrawInBound(shortEnd, ACCEPTABLE_STROKE),
        "chordDrawInBoundShortArmDesired" to chordDrawInBound(shortEnd, DESIRED_STROKE),
        "chordDrawInBoundLongArmDesired" to chordDrawInBound(longEnd, DESIRED_STROKE),
        "chordDrawInBoundDesiredBasePairs" to chordDrawInBound(longEnd, DESIRED_STROKE) / RISE,
        "heldStrainBoundLongArmDesired" to restrainedAxialStrainBound(longEnd, DESIRED_STROKE),
        "heldTensionBoundLongArmDesired" to restrainedTensionBound(longEnd, DESIRED_STROKE),
        "nickedDuplexCeiling" to 65.0,
        "geometricStiffeningAtThirtyFiveDegrees" to
                1.0 / (kotlin.math.cos(35.0 * PI / 180.0) * kotlin.math.cos(35.0 * PI / 180.0)),
        "sineOverAngleAtNineDegrees" to
                kotlin.math.sin(9.0 * PI / 180.0) / (9.0 * PI / 180.0),
        "desiredStroke" to DESIRED_STROKE
    )

    // ------------------------------------------------------------------ the continuum
    val continuumLength = 12.0
    val continuum = listOf(
        0.0 to 4.082,
        0.7 to 0.0,
        3.75 to 0.25,
        11.3 to 4.082,
        11.3 to 32.76,
        60.0 to 201.2,
        Double.POSITIVE_INFINITY to 0.0,
        Double.POSITIVE_INFINITY to Double.POSITIVE_INFINITY
    ).map { (rhoNear, rhoFar) ->
        val beam = TwoSpringElastica(
            EI, continuumLength,
            if (rhoNear.isInfinite()) Double.POSITIVE_INFINITY
            else rhoNear * EI / continuumLength,
            if (rhoFar.isInfinite()) Double.POSITIVE_INFINITY else rhoFar * EI / continuumLength,
            STEPS
        )
        val probe = 1.0e-7
        val measured = probe / beam.stateAtForce(probe).displacement *
                continuumLength * continuumLength * continuumLength / EI
        val closed = twoSpringArmFactor(rhoNear, rhoFar)
        T79ContinuumRecord(
            nearRestraint = finite(rhoNear),
            farRestraint = finite(rhoFar),
            closedFormFactor = closed,
            elasticaVanishingLoadFactor = measured,
            relativeDeparture = abs(measured / closed - 1.0)
        )
    }

    // ------------------------------------------------------------------ the placements
    // C-0040/CH-0054: a 40 nm hinge line carries FOUR crossovers, and a flexure at 45 paths owns
    // one or two, so the counts E5g16/E5a16 assume are swept beside the ones the lattice supplies.
    val placements = ANCHORAGES.flatMap { anchorage ->
        val counts =
            if (anchorage.id == ADOPTED.id) listOf(1, 2, 3, 4, 6, 8, 16, 32, 64)
            else listOf(8, 16, 32, 64)
        counts.map { placementRecord(anchorage, it) }
    }

    val adopted = placements.first {
        it.anchorageId == ADOPTED.id && it.hingeCount == DESIGN_HINGE_COUNT &&
                it.farStiffness > 70.0
    }
    val adoptedBeam = beamAt(adopted.armLength, DESIGN_HINGE_COUNT, ADOPTED.rotationalStiffness)

    // ------------------------------------------------------------------ the stroke sweep
    val strokes = listOf(
        0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0
    ).map { strokeRecord(adoptedBeam, DESIGN_HINGE_COUNT, it) }

    // ------------------------------------------------------------------ the bracket
    val seriesArm = anchoredArmForStiffness(
        HINGE, DESIGN_HINGE_COUNT, ADOPTED.rotationalStiffness, EI, PATH_COUNT, MANDATE,
        ACCEPTABLE_STROKE
    )
    val bvpArm = twoSpringArmForStiffness(
        HINGE, DESIGN_HINGE_COUNT, ADOPTED.rotationalStiffness, EI, PATH_COUNT, MANDATE
    )
    val assertedArm = rotatingArmForStiffness(
        HINGE, EI, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, DESIGN_HINGE_COUNT, 12.0
    )
    val bracket = listOf(
        T79BracketRecord(
            reading = "C-0029 series composition, exact rotation (C-0034's short end)",
            exactRotation = true,
            exactEndCondition = false,
            armLength = seriesArm,
            armBasePairs = seriesArm / RISE,
            realisedFactor =
                guidedArmFactor(armRestraintParameter(ADOPTED.rotationalStiffness, seriesArm, EI)),
            insideC0034Bracket = seriesArm in shortEnd - 1e-2..longEnd + 1e-2,
            relativeToLongEnd = seriesArm / bvpArm
        ),
        T79BracketRecord(
            reading = "C-0029's E5g16 as filed, series composition at an asserted c = 12",
            exactRotation = true,
            exactEndCondition = false,
            armLength = assertedArm,
            armBasePairs = assertedArm / RISE,
            realisedFactor = 12.0,
            insideC0034Bracket = assertedArm in shortEnd - 1e-2..longEnd + 1e-2,
            relativeToLongEnd = assertedArm / bvpArm
        ),
        T79BracketRecord(
            reading = "C-0034 two-spring BVP, small deflection (C-0034's long end)",
            exactRotation = false,
            exactEndCondition = true,
            armLength = bvpArm,
            armBasePairs = bvpArm / RISE,
            realisedFactor = TwoSpringArm(
                EI, bvpArm, DESIGN_HINGE_COUNT * HINGE, ADOPTED.rotationalStiffness
            ).armFactor,
            insideC0034Bracket = bvpArm in shortEnd - 1e-2..longEnd + 1e-2,
            relativeToLongEnd = 1.0
        ),
        T79BracketRecord(
            reading = "T-79 two-spring ELASTICA, exact in BOTH",
            exactRotation = true,
            exactEndCondition = true,
            armLength = adopted.armLength,
            armBasePairs = adopted.armBasePairs,
            realisedFactor = adopted.vanishingLoadFactor,
            insideC0034Bracket = adopted.armLength in shortEnd - 1e-2..longEnd + 1e-2,
            relativeToLongEnd = adopted.armLength / bvpArm
        )
    )

    // ------------------------------------------------------------------ sensitivities
    fun sensitivity(
        axis: String,
        label: String,
        value: Double,
        far: Double,
        rigidity: Double = EI,
        hingeConstant: Double = HINGE,
        hingeCount: Int = DESIGN_HINGE_COUNT,
        paths: Int = PATH_COUNT
    ): T79SensitivityRecord {
        val arm = elasticaArmForStiffness(
            hingeConstant, hingeCount, far, rigidity, paths, MANDATE, ACCEPTABLE_STROKE, STEPS
        )
        val cap = elasticaArmCeiling(far, paths, rigidity, MANDATE, ACCEPTABLE_STROKE, STEPS)
        val beam = TwoSpringElastica(rigidity, arm, hingeCount * hingeConstant, far, STEPS)
        val usable = usableStroke(beam, paths)
        val reaches = arm > DESIRED_STROKE
        val insideAtDesired = reaches && (
                runCatching {
                    paths * beam.tangentStiffness(DESIRED_STROKE)
                }.getOrNull() ?: Double.MAX_VALUE
                ) <= COMPLIANT_CEILING
        return T79SensitivityRecord(
            axis = axis,
            label = label,
            value = value,
            farStiffness = finite(far),
            armLength = arm,
            armCeiling = cap,
            tangentAtAcceptable = paths * beam.tangentStiffness(ACCEPTABLE_STROKE),
            usableStrokeInsideCeiling = usable,
            reachesDesiredStroke = reaches,
            clearsDesiredStrokeInsideCeiling = insideAtDesired
        )
    }

    val sensitivities = buildList {
        listOf(0.6, 0.8, 1.0, 1.2).forEach { alpha ->
            add(
                sensitivity(
                    "crossover alpha (Chen et al., CITED and FITTED)",
                    "alpha = $alpha",
                    alpha,
                    maximumBaseRotationalStiffness(BForm.PHOSPHATE_RADIUS, alpha),
                    hingeConstant = Gen1Tile.crossoverHingeStiffness(alpha)
                )
            )
        }
        listOf(0.75, 1.0).forEach { factor ->
            add(
                sensitivity(
                    "duplex EI (CanDo MODEL INPUT; Fields et al. imply -25 %)",
                    "EI x $factor",
                    factor * EI,
                    ADOPTED.rotationalStiffness,
                    rigidity = factor * EI
                )
            )
        }
        listOf(0.90, 1.00).forEach { radius ->
            add(
                sensitivity(
                    "phosphate radius (CITED bracket)",
                    "r_P = $radius nm",
                    radius,
                    maximumBaseRotationalStiffness(radius)
                )
            )
        }
        listOf(120.0, 180.0).forEach { groove ->
            val lever = BForm.PHOSPHATE_RADIUS * kotlin.math.sin(groove * PI / 360.0)
            add(
                sensitivity(
                    "backbone separation (CONVENTION)",
                    "delta = $groove deg",
                    groove,
                    maximumBaseRotationalStiffness(lever)
                )
            )
        }
        ANCHORAGES.forEach { anchorage ->
            add(
                sensitivity(
                    "anchorage catalogue (C-0034's counting theorem)",
                    anchorage.id + " " + anchorage.name.take(40),
                    anchorage.linkCount.toDouble(),
                    anchorage.rotationalStiffness
                )
            )
        }
        // C-0041/CH-0055: the 45-path array has no plan view and the tile carries 15; CH-0029's
        // unzip allowable at the desired stroke bounds the count below at 34.
        listOf(15, 34, 45).forEach { paths ->
            add(
                sensitivity(
                    "load path count (C-0041 / CH-0055; CH-0029's floor is 34)",
                    "$paths paths",
                    paths.toDouble(),
                    ADOPTED.rotationalStiffness,
                    paths = paths
                )
            )
        }
        // C-0040/CH-0054: the hinge count the lattice actually supplies
        listOf(1, 2, 3, 4, 6).forEach { count ->
            add(
                sensitivity(
                    "hinge count the lattice supplies (C-0040 / CH-0054)",
                    "$count crossovers",
                    count.toDouble(),
                    ADOPTED.rotationalStiffness,
                    hingeCount = count
                )
            )
        }
    }

    // ------------------------------------------------------------------ convergence
    val far = ADOPTED.rotationalStiffness
    val finestArm = elasticaArmForStiffness(
        HINGE, DESIGN_HINGE_COUNT, far, EI, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, 3200
    )
    val finestForce = beamAt(12.5, DESIGN_HINGE_COUNT, far, 3200)
        .forceForDisplacement(DESIRED_STROKE)
    val finestTangent = PATH_COUNT *
            beamAt(12.5, DESIGN_HINGE_COUNT, far, 3200).tangentStiffness(DESIRED_STROKE)
    val convergence = buildList {
        listOf(100, 200, 400, 800, 1600).forEach { steps ->
            val value = elasticaArmForStiffness(
                HINGE, DESIGN_HINGE_COUNT, far, EI, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, steps
            )
            add(
                T79ConvergenceRecord(
                    "the placed arm on the adopted anchorage", "RK4 steps", steps.toDouble(),
                    value, abs(value / finestArm - 1.0)
                )
            )
        }
        listOf(100, 200, 400, 800, 1600).forEach { steps ->
            val value = beamAt(12.5, DESIGN_HINGE_COUNT, far, steps)
                .forceForDisplacement(DESIRED_STROKE)
            add(
                T79ConvergenceRecord(
                    "the tip force at the desired stroke on a 12.5 nm arm", "RK4 steps",
                    steps.toDouble(), value, abs(value / finestForce - 1.0)
                )
            )
        }
        listOf(100, 200, 400, 800, 1600).forEach { steps ->
            val value = PATH_COUNT *
                    beamAt(12.5, DESIGN_HINGE_COUNT, far, steps).tangentStiffness(DESIRED_STROKE)
            add(
                T79ConvergenceRecord(
                    "the assembled tangent at the desired stroke", "RK4 steps", steps.toDouble(),
                    value, abs(value / finestTangent - 1.0)
                )
            )
        }
    }

    // ------------------------------------------------------------------ reproductions
    val guidedBeam = TwoSpringElastica(
        EI, 12.2423721, DESIGN_HINGE_COUNT * HINGE, Double.POSITIVE_INFINITY, STEPS
    )
    val overPlacedProbe = 1.0e-7
    val reproductions = listOf(
        T79ReproductionRecord(
            "C-0034's series placement (short end of its bracket)", 11.028, seriesArm,
            abs(seriesArm / 11.028 - 1.0)
        ),
        T79ReproductionRecord(
            "C-0034's two-spring BVP placement (long end)", 12.496, bvpArm,
            abs(bvpArm / 12.496 - 1.0)
        ),
        T79ReproductionRecord(
            "C-0029's E5g16 arm at an asserted c = 12", 12.2423721, assertedArm,
            abs(assertedArm / 12.2423721 - 1.0)
        ),
        T79ReproductionRecord(
            "C-0029's cantilever hinge-arm ceiling", 9.76624511,
            hingeArmCeiling(3.0, PATH_COUNT, EI, MANDATE),
            abs(hingeArmCeiling(3.0, PATH_COUNT, EI, MANDATE) / 9.76624511 - 1.0)
        ),
        T79ReproductionRecord(
            "C-0029's guided hinge-arm ceiling", 15.5029478,
            hingeArmCeiling(12.0, PATH_COUNT, EI, MANDATE),
            abs(hingeArmCeiling(12.0, PATH_COUNT, EI, MANDATE) / 15.5029478 - 1.0)
        ),
        T79ReproductionRecord(
            "C-0029's two-terminus couple ceiling", 78.2352941, ADOPTED.rotationalStiffness,
            abs(ADOPTED.rotationalStiffness / 78.2352941 - 1.0)
        ),
        T79ReproductionRecord(
            "C-0009's crossover hinge constant", 13.5294118, HINGE, abs(HINGE / 13.5294118 - 1.0)
        ),
        T79ReproductionRecord(
            "CH-0044's over-placement of E5g16 on its own c = 12", 54.61,
            PATH_COUNT * guidedBeam.smallRotationStiffness,
            abs(PATH_COUNT * guidedBeam.smallRotationStiffness / 54.61 - 1.0)
        ),
        T79ReproductionRecord(
            "the same, from the ELASTICA at vanishing load", 54.61,
            PATH_COUNT * overPlacedProbe / guidedBeam.stateAtForce(overPlacedProbe).displacement,
            abs(
                PATH_COUNT * overPlacedProbe /
                        guidedBeam.stateAtForce(overPlacedProbe).displacement / 54.61 - 1.0
            )
        ),
        T79ReproductionRecord(
            "C-0034's realised factor at the BVP placement", 6.284,
            TwoSpringArm(
                EI, bvpArm, DESIGN_HINGE_COUNT * HINGE, ADOPTED.rotationalStiffness
            ).armFactor,
            abs(
                TwoSpringArm(
                    EI, bvpArm, DESIGN_HINGE_COUNT * HINGE, ADOPTED.rotationalStiffness
                ).armFactor / 6.284 - 1.0
            )
        ),
        T79ReproductionRecord(
            "CH-0029's shear allowable at 8 bp", 18.796,
            ALLOWABLE.ruptureForce(8.0, ShearJointAllowable.REFERENCE_LOADING_RATE), abs(ALLOWABLE.ruptureForce(8.0, ShearJointAllowable.REFERENCE_LOADING_RATE) / 18.796 - 1.0)
        ),
        T79ReproductionRecord(
            "the mandate secant discharged by the elastica placement", MANDATE,
            adopted.secantAtAcceptable, abs(adopted.secantAtAcceptable / MANDATE - 1.0)
        )
    )

    // ------------------------------------------------------------------ findings
    fun f(value: Double, digits: Int = 3): String = "%.${digits}f".format(value)

    val desiredRecord = strokes.first { it.stroke == DESIRED_STROKE }
    val acceptableRecord = strokes.first { it.stroke == ACCEPTABLE_STROKE }
    val minimumTangent = strokes.minOf { it.tangent }
    val findings = mapOf(
        "theArm" to (
                "Placed on the composition that is exact in BOTH the rotation and the end " +
                        "condition, E5a16's arm is ${f(adopted.armLength)} nm = " +
                        "${f(adopted.armBasePairs, 1)} bp — OUTSIDE C-0034's 11.028-12.496 nm " +
                        "bracket, ${f(100.0 * (adopted.armLength / bvpArm - 1.0), 1)} % beyond " +
                        "its long end. The bracket fails because its two errors do NOT run " +
                        "opposite ways: both readings are corrections to the same linear " +
                        "boundary-value problem and both STIFFEN it, so applying both moves the " +
                        "arm further out, not back inside. C-0034's own third failure mode."
                ),
        "theCeiling" to (
                "The placement clause is discharged exactly — " +
                        "${f(adopted.secantAtAcceptable, 4)} pN/nm on the secant at 3 nm, by " +
                        "construction. The compliance clause is not: the tangent is " +
                        "${f(adopted.tangentAtAcceptable)} pN/nm at the acceptable stroke " +
                        "(t/s = ${f(adopted.tangentToSecantAtAcceptable)}) and " +
                        "${f(desiredRecord.tangent, 1)} pN/nm at the desired one, " +
                        "${f(desiredRecord.tangent / COMPLIANT_CEILING, 1)}x past C-0023's 40 " +
                        "pN/nm ceiling. The SECANT at the desired stroke is already " +
                        "${f(desiredRecord.secant, 1)} pN/nm, " +
                        "${f(desiredRecord.secant / MANDATE)}x the mandate. C-0034 reported " +
                        "36.78 pN/nm there, inside the ceiling with 8.1 % to spare."
                ),
        "theUsableStroke" to (
                "The stroke E5a16 delivers INSIDE its own compliance ceiling is " +
                        "${f(adopted.usableStrokeInsideCeiling)} nm — it clears §3's acceptable " +
                        "3 nm and it does not clear §3's desired 10 nm on any anchorage or hinge " +
                        "count in the sweep. The reason is geometric and needs no constitutive " +
                        "law: the arm is capped at ${f(adopted.armCeiling)} nm by its own " +
                        "placement condition, so a 10 nm stroke is at least " +
                        "${f(100.0 * DESIRED_STROKE / adopted.armCeiling, 0)} % of the arm's own " +
                        "contour at every design point, and a beam driven that far past its own " +
                        "length stiffens whatever it is made of."
                ),
        "theDrawIn" to (
                "The exact solve delivers the arc-length demand the linear readings could not " +
                        "see. At the acceptable stroke the two attachment points must approach " +
                        "each other by ${f(acceptableRecord.drawIn)} nm = " +
                        "${f(acceptableRecord.drawInBasePairs, 1)} bp; at the desired stroke by " +
                        "${f(desiredRecord.drawIn)} nm = ${f(desiredRecord.drawInBasePairs, 1)} " +
                        "bp, ${f(100.0 * desiredRecord.drawIn / adopted.armLength, 0)} % of the " +
                        "arm. C-0029 quotes 0.095 nm at 3 nm because RotatingHingeArm charges " +
                        "only the hinge's own rigid swing and not the arm's bending. And the " +
                        "demand is NOT a design choice: an inextensible arm whose ends hold " +
                        "their axial separation cannot deflect at all, so C-0023's free/held " +
                        "binary does not exist here. Held, the arm must STRETCH — at least " +
                        "${f(restrainedTensionBound(adopted.armLength, DESIRED_STROKE), 0)} pN " +
                        "at the desired stroke, past the 65 pN nicked ceiling."
                ),
        "thePerPathAllowable" to (
                "Read at the stroke it is asked at, not at the placement point. At 3 nm the " +
                        "element's own tension is ${f(acceptableRecord.forcePerPath)} pN, its " +
                        "hinge bond force ${f(acceptableRecord.hingeBondForce)} pN and its " +
                        "anchorage link force ${f(acceptableRecord.anchorageLinkForce)} pN — all " +
                        "inside C-0006's 10 pN unzip allowable. At 10 nm they are " +
                        "${f(desiredRecord.forcePerPath)}, ${f(desiredRecord.hingeBondForce)} " +
                        "and ${f(desiredRecord.anchorageLinkForce)} pN, and the anchorage alone " +
                        "demands ${f(desiredRecord.anchorageBondedLength, 1)} bp of bonded " +
                        "length on CH-0029's ladder against C-0034's 7.3 bp."
                ),
        "theStability" to (
                "C-0017's stability clause is read on the tangent and this element is " +
                        "strain-STIFFENING everywhere: the tangent rises monotonically from " +
                        "${f(minimumTangent)} pN/nm over the whole 0.5-10 nm sweep, so " +
                        "min_s k_tangent is the small-stroke end and CH-0047's question about " +
                        "which range the minimum is taken over does not bind here. What binds " +
                        "is the compliance CEILING, which is the other side of the same curve."
                ),
        "whatSurvives" to (
                "C-0034's counting theorem, its anchorage catalogue, its fixed-point cap and " +
                        "CH-0044's diagnosis all survive and are re-run rather than restated. " +
                        "The cap moves outward, from 13.428 to ${f(adopted.armCeiling)} nm, " +
                        "because the exact arm stiffens and a longer one is therefore needed to " +
                        "reach the mandate. What does not survive is the verdict that E5a16 " +
                        "clears §3's DESIRED stroke: on the exact composition it clears the " +
                        "acceptable stroke with " +
                        "${f(100.0 * (1.0 - adopted.tangentAtAcceptable / COMPLIANT_CEILING), 1)}" +
                        " % of ceiling margin and misses the desired one on compliance."
                )
    )

    val result = T79Result(
        task = "T-79",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "thermalEnergy" to "4.141947 pN nm",
            "medium" to "aqueous 2 mM MgCl2",
            "tile" to "40 x 40 nm single-layer square-lattice Rothemund sheet",
            "loadPaths" to "45, on C-0015's 3 x 15 grid",
            "mandate" to "100 pN over the acceptable 3 nm stroke = 33.3333 pN/nm",
            "desiredStroke" to "10 nm",
            "complianceCeiling" to "40 pN/nm (C-0023's declared ceiling)",
            "bendingRigidity" to "230 pN nm^2 (CanDo MODEL INPUT, not a measurement)",
            "hingeConstant" to "13.53 pN nm/rad (Chen et al., CITED and FITTED, via C-0009)",
            "anchorage" to "78.235 pN nm/rad, C-0029's two-terminus counting theorem",
            "axialCondition" to "free to draw in (H = 0); the held reading is a STRAIN bound",
            "integrator" to "RK4 over (phi, phi', x, z), $STEPS steps, shooting on the near rotation"
        ),
        cheapBounds = cheapBounds,
        continuum = continuum,
        placements = placements,
        strokes = strokes,
        bracket = bracket,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings
    )

    val json = Json { prettyPrint = true }
    val file = File("gpd/results/T-79-two-spring-elastica.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()))

    println("T-79 — a large-rotation two-spring elastica for E5's arm")
    println()
    println("cheap bounds")
    cheapBounds.forEach { (key, value) -> println("  %-42s %12.5f".format(key, value)) }
    println()
    println("the vanishing-load limit against C-0034's closed form (rho_n, rho_f, c, c elastica, dep)")
    continuum.forEach {
        println(
            "  %10.3f %10.3f %10.6f %10.6f %10.2e".format(
                it.nearRestraint, it.farRestraint, it.closedFormFactor,
                it.elasticaVanishingLoadFactor, it.relativeDeparture
            )
        )
    }
    println()
    println("placements (anchorage, n, arm, bp, cap, tan3, t/s, sec10, tan10, usable, verdict)")
    placements.forEach {
        println(
            "  %-5s %3d %7.3f %6.1f %7.3f %8.3f %6.3f %8.2f %9.2f %7.3f  %s".format(
                it.anchorageId, it.hingeCount, it.armLength, it.armBasePairs, it.armCeiling,
                it.tangentAtAcceptable, it.tangentToSecantAtAcceptable, it.secantAtDesired,
                it.tangentAtDesired, it.usableStrokeInsideCeiling, it.verdict.take(46)
            )
        )
    }
    println()
    println("the adopted design over the stroke (d, F, secant, tangent, th_n, th_f, draw-in, bp, hinge, link)")
    strokes.forEach {
        println(
            "  %5.1f %8.4f %9.3f %10.3f %7.2f %7.2f %8.4f %6.1f %7.3f %8.3f".format(
                it.stroke, it.forcePerPath, it.secant, it.tangent, it.nearRotationDegrees,
                it.farRotationDegrees, it.drawIn, it.drawInBasePairs, it.hingeBondForce,
                it.anchorageLinkForce
            )
        )
    }
    println()
    println("the bracket (reading, exact rotation, exact end, arm, bp, c, inside)")
    bracket.forEach {
        println(
            "  %-62s %-6s %-6s %7.3f %6.1f %7.3f %s".format(
                it.reading.take(62), it.exactRotation, it.exactEndCondition, it.armLength,
                it.armBasePairs, it.realisedFactor, it.insideC0034Bracket
            )
        )
    }
    println()
    println("sensitivities (axis, label, arm, cap, tan3, usable, reaches, clears)")
    sensitivities.forEach {
        println(
            "  %-44s %-44s %7.3f %7.3f %8.3f %7.3f %-6s %s".format(
                it.axis.take(44), it.label.take(44), it.armLength, it.armCeiling,
                it.tangentAtAcceptable, it.usableStrokeInsideCeiling, it.reachesDesiredStroke,
                it.clearsDesiredStrokeInsideCeiling
            )
        )
    }
    println()
    println("convergence")
    convergence.forEach {
        println(
            "  %-52s %-12s %8.0f %16.9f %10.2e".format(
                it.quantity.take(52), it.control, it.level, it.value, it.departureFromFinest
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    reproductions.forEach {
        println(
            "  %-58s %14.7f %14.7f %10.2e".format(
                it.quantity.take(58), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
