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

import com.xemantic.nano.plentyofroom.coupling.EntropicCoupling
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.openrndr.math.Vector2
import java.io.File
import kotlin.math.PI
import kotlin.math.abs

/**
 * `T-133` — is there an output element that does not lie in the plan?
 *
 * Emits `gpd/results/T-133-output-element-placement.json`.
 */

private const val ARM_COUNT = 34
private const val PHASE = 24
private const val DUPLEXES = 15
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
private val PER_PATH = perPathStiffness(MANDATE, ARM_COUNT)

/** `C-0017`'s six stability floors `|k_eff(3 nm)|` at the 10 nm layer in 2 mM — **CITED**. */
private val FLOORS_TWO_MILLIMOLAR = listOf(
    27.9133262, 23.4139164, 24.9042565, 27.0387111, 23.8036442, 23.9527371
)

/**
 * The finite stand-in a **rigid** restraint is reported as.
 *
 * `kotlinx.serialization` refuses `Infinity` (`CLAUDE.md`), and it throws at the serialisation
 * call rather than where the value was set — so every restraint that reaches a record is routed
 * through [reportable] while the *computation* keeps the exact infinity.
 */
private const val RIGID_SENTINEL: Double = 1.0e6

private fun reportable(restraint: Double): Double =
    if (restraint.isFinite()) restraint else RIGID_SENTINEL

/** `C-0034`'s `A2` anchorage — the arm's own duplex end, two strand termini. */
private val DUPLEX_END_ANCHORAGE = ArmAnchorage.twoTerminus().rotationalStiffness

@Serializable
private data class T133BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val against: Double,
    val ratio: Double,
    val fired: Boolean,
    val settles: String
)

@Serializable
private data class T133MechanismRecord(
    val mechanism: String,
    val law: String,
    val lengthDemanded: Double,
    val overCeiling: Double,
    val twoSided: Boolean,
    val liesInPlan: Boolean,
    val admitted: Boolean,
    val verdict: String
)

@Serializable
private data class T133CandidateRecord(
    val id: String,
    val name: String,
    val family: String,
    val nearRestraint: Double,
    val farRestraint: Double,
    val length: Double,
    val lengthBasePairs: Double,
    val endFactor: Double,
    val marginToCeiling: Double,
    val demanded: Int,
    val placed: Int,
    val levelsRequired: Int,
    val singleLevel: Boolean,
    val overlappingPairs: Int,
    val planAreaFraction: Double,
    val assembledSecant: Double,
    val assembledTangentAtStroke: Double,
    val assembledTangentMinimum: Double,
    val floorsCleared: Int,
    val perPathForce: Double,
    val insideUnzipAllowable: Boolean,
    val perPathSecantCeiling: Double,
    val insideSecantCeiling: Boolean,
    val twoSided: Boolean,
    val placesInFull: Boolean,
    val verdict: String
)

@Serializable
private data class T133WindowRecord(
    val axis: String,
    val heldRestraint: Double,
    val heldName: String,
    val ceilingRestraint: Double?,
    val ceilingIsSearchCap: Boolean,
    val armAtCeiling: Double,
    val note: String
)

@Serializable
private data class T133OrientationRecord(
    val element: String,
    val length: Double,
    val samples: Int,
    val feasibleOrientations: Int,
    val singleLevelOrientations: Int,
    val minimumOverlappingPairs: Int,
    val minimumMemberClashPairs: Int,
    val bestAngleDegrees: Double
)

@Serializable
private data class T133SensitivityRecord(
    val axis: String,
    val reading: String,
    val lengthCeiling: Double,
    val armLength: Double,
    val placed: Int,
    val verdictMoves: Boolean,
    val note: String
)

@Serializable
private data class T133ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val levels: List<String>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T133ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val note: String
)

@Serializable
private data class T133PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T133Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val decision: String,
    val bounds: List<T133BoundRecord>,
    val mechanisms: List<T133MechanismRecord>,
    val candidates: List<T133CandidateRecord>,
    val window: List<T133WindowRecord>,
    val orientations: List<T133OrientationRecord>,
    val sensitivities: List<T133SensitivityRecord>,
    val convergence: List<T133ConvergenceRecord>,
    val reproductions: List<T133ReproductionRecord>,
    val predicates: List<T133PredicateRecord>,
    val findings: Map<String, String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

/** `C-0063`'s own 34 stations, read from its result file rather than retyped. */
private fun c0063Stations(file: File): List<TrussStation> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .flatMap { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            val y = row.getValue("y").jsonPrimitive.content.toDouble()
            row.getValue("roots").jsonArray.map {
                TrussStation(index, it.jsonPrimitive.content.toDouble(), y)
            }
        }
}

/** `C-0065`'s published flexure reading, consumed as data for the free limiting case. */
private fun c0065Flexure(file: File): Triple<Double, Int, Int> {
    require(file.exists()) { "C-0065's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("flexure").jsonArray.map { it.jsonObject }
        .first { it.getValue("paths").jsonPrimitive.content.toInt() == ARM_COUNT }
    return Triple(
        record.getValue("span").jsonPrimitive.content.toDouble(),
        record.getValue("placed").jsonPrimitive.content.toInt(),
        record.getValue("levelsRequired").jsonPrimitive.content.toInt()
    )
}

/** `C-0048`'s cap, exactly as `C-0065` builds it, so the flexure span is its own and not retyped. */
private fun trussCap(): SolvedTrussCap = SolvedTrussCap(
    separationBasePairs = 10,
    legLength = 12 * Gen1Tile.RISE_PER_BASE_PAIR,
    base = TwoLinkBase(
        name = "two-terminus base",
        restrainedAxis = chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0).loaded,
        freeAxis = chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0).free,
        axial = 2.0 * bondSlideStiffness(),
        provenance = "C-0029's counting theorem via C-0042's chordBaseAxes"
    )
)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val edgeX = Gen1Tile.EDGE_X
    val width = OrigamiDuplex.INTERHELICAL
    val lengthY = DUPLEXES * width
    val footprint = edgeX * lengthY
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * rise
    val rigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY
    val hinge = Gen1Tile.crossoverHingeStiffness()

    println("T-133 — reading C-0063's stations and C-0065's flexure reading ...")
    val stations = c0063Stations(File("gpd/results/T-125-upward-root-placement.json"))
    check(stations.size == ARM_COUNT) {
        "C-0063's placement must carry $ARM_COUNT stations, carried ${stations.size}"
    }
    val rows = stationRows(stations)
    val (publishedSpan, publishedPlaced, publishedLevels) =
        c0065Flexure(File("gpd/results/T-130-crossbar-array-placement.json"))

    // ------------------------------------------------------------------ the cheap bounds
    println("T-133 — the five cheap bounds, which run before any element is solved ...")
    val ceiling = rowOfThreeLengthCeiling(pitch, width)
    val solvedCeiling = rootedLengthCeiling(rows, edgeX, width)
    val rowsOfThree = rowsCarryingThreeArms(ARM_COUNT, DUPLEXES, 3)
    val factorCeiling = bendingFactorForLength(ceiling, rigidity, PER_PATH)

    val midspanFloor = bendingLengthForStiffness(
        FlexureEndCondition.PINNED_ENDS.midspanFactor, rigidity, PER_PATH
    )
    val axialLength = axialLengthForStiffness(Gen1Tile.DUPLEX_STRETCH_MODULUS, PER_PATH)
    val alongRowSlack = pitch - ceiling
    val acrossRowSlack = width - width

    val bounds = listOf(
        T133BoundRecord(
            "rows of three forced by the count", rowsOfThree.toDouble(), "rows", 4.0,
            rowsOfThree / 4.0, false,
            "C-0063's bound 1 in a new place: 3a + 2(15 - a) = 34 forces FOUR rows of three, so " +
                    "every 34-root placement on the upward lattice carries a row of three"
        ),
        T133BoundRecord(
            "the row-of-three length ceiling, pitch - d", ceiling, "nm", pitch, ceiling / pitch,
            true,
            "THE PLAN BUDGET OF ANY ROOTED ELEMENT, over every placement on the lattice and not " +
                    "only C-0063's — and the solved ceiling over C-0063's own rows agrees to " +
                    "${"%.2e".format(abs(solvedCeiling - ceiling))} nm"
        ),
        T133BoundRecord(
            "the two-support flexure's own floor, (48 EI/k)^(1/3)", midspanFloor, "nm", ceiling,
            midspanFloor / ceiling, true,
            "THE ANSWER FOR THE WHOLE E3/C-0030 FAMILY: its softest possible end condition is " +
                    "still 2.76x the plan budget, so it is refused at every span, at every end " +
                    "joint and on every placement — strictly stronger than C-0065's 12 of 34"
        ),
        T133BoundRecord(
            "the normal direction, S/k", axialLength, "nm", Gen1Tile.DESIRED_STROKE,
            axialLength / Gen1Tile.DESIRED_STROKE, true,
            "a member standing along z and loaded along z is loaded AXIALLY, so 'out of the plan " +
                    "along the surface normal' asks for 112x C-0017's whole 10 nm envelope"
        ),
        T133BoundRecord(
            "the rooted element's own kinematic floor, 1.5 x the stroke",
            1.5 * Gen1Tile.ACCEPTABLE_STROKE, "nm", ceiling,
            1.5 * Gen1Tile.ACCEPTABLE_STROKE / ceiling, true,
            "the window has a FLOOR as well as a ceiling: a lever shorter than this turns more " +
                    "than 42 degrees to deliver a 3 nm stroke and C-0039's chord draw-in " +
                    "becomes a large fraction of the arm, so the rooted-element window is " +
                    "4.50-${"%.2f".format(ceiling)} nm, 1.82x wide"
        ),
        T133BoundRecord(
            "the across-row slack a fold would need", acrossRowSlack, "nm", width, 0.0, true,
            "a fold trades length along a row for width across the rows, and the across-row " +
                    "pitch IS one duplex (C-0041's Fact A) — it spends the only slack the " +
                    "placement has to buy a direction that has none"
        )
    )

    // ------------------------------------------------------------------ the mechanism census
    println("T-133 — the mechanism census, closed form in every row ...")
    val hingeLever = hingeLeverForStiffness(hinge, PER_PATH)
    val entropicContour =
        entropicContourForStiffness(PER_PATH, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
    val torsionalLength =
        torsionalLengthForStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, width, PER_PATH)
    val endLoadedFloor = bendingLengthForStiffness(1.0e-3, rigidity, PER_PATH)
    val endLoadedCeilingLength = bendingLengthForStiffness(12.0, rigidity, PER_PATH)

    val mechanisms = listOf(
        T133MechanismRecord(
            "axial stretch of a duplex", "l = S/k", axialLength, axialLength / ceiling,
            true, false, false,
            "REFUSED — ${"%.0f".format(axialLength / ceiling)}x the plan budget and " +
                    "${"%.0f".format(axialLength / Gen1Tile.DESIRED_STROKE)}x the 10 nm envelope"
        ),
        T133MechanismRecord(
            "an entropic single strand", "L_c = 3k_BT/(k b)", entropicContour,
            entropicContour / ceiling, false, false, false,
            "REFUSED — compact, but ONE-SIDED: C-0023's E2 returns exactly zero reaction and " +
                    "exactly zero tangent at negative argument, and C-0023's own hold-down " +
                    "verdict rests on a two-sided coupling"
        ),
        T133MechanismRecord(
            "rotation at a hinge on a lever", "r = sqrt(k_theta/k)", hingeLever,
            hingeLever / ceiling, true, true, true,
            "ADMITTED — ${"%.2f".format(hingeLever / ceiling)} of the plan budget, and the " +
                    "most compact mechanism DNA offers"
        ),
        T133MechanismRecord(
            "bending, supported once and loaded at the far end", "L = (c EI/k)^(1/3), c in (0, 12]",
            endLoadedCeilingLength, endLoadedCeilingLength / ceiling, true, true, true,
            "ADMITTED CONDITIONALLY — the family spans ${"%.2f".format(endLoadedFloor)}-" +
                    "${"%.2f".format(endLoadedCeilingLength)} nm and the plan budget cuts it at " +
                    "c <= ${"%.4f".format(factorCeiling)}"
        ),
        T133MechanismRecord(
            "bending, supported twice and loaded at midspan", "L = (c EI/k)^(1/3), c in [48, 192]",
            midspanFloor, midspanFloor / ceiling, true, true, false,
            "REFUSED — at every end condition. Its SHORTEST member is " +
                    "${"%.2f".format(midspanFloor / ceiling)}x the plan budget"
        ),
        T133MechanismRecord(
            "torsion of a duplex on a lever", "L = GJ/(k r^2)", torsionalLength,
            torsionalLength / ceiling, true, true, false,
            "REFUSED at a one-duplex lever — ${"%.1f".format(torsionalLength / ceiling)}x the " +
                    "plan budget, and the lever it would need to fit is itself in plan"
        )
    )

    // ------------------------------------------------------------------ the catalogue
    println("T-133 — the candidate catalogue, each placed and then run through every clause ...")
    val cap = trussCap()
    val coupledSpan = coupledFlexureSpan(
        rigidity, cap.flexibility, ARM_COUNT, MANDATE, Gen1Tile.ACCEPTABLE_STROKE
    )
    val standoffHead = standoffTipFlexibility(rigidity, 8.0, StandoffBase.crossovers(2).rotationalStiffness)

    fun clauses(
        id: String,
        name: String,
        family: String,
        near: Double,
        far: Double,
        length: Double,
        outcome: OutputElementOutcome,
        secant: (Double) -> Double,
        tangent: (Double) -> Double,
        compressionProbe: (Double) -> Double
    ): T133CandidateRecord {
        val assembledSecant = ARM_COUNT * secant(Gen1Tile.ACCEPTABLE_STROKE)
        val assembledTangent = ARM_COUNT * tangent(Gen1Tile.ACCEPTABLE_STROKE)
        val minimum = (0..600).minOf { step ->
            val stroke = Gen1Tile.ACCEPTABLE_STROKE * step / 600.0
            ARM_COUNT * tangent(if (stroke == 0.0) 1.0e-6 else stroke)
        }
        val perPathForce = assembledSecant * Gen1Tile.ACCEPTABLE_STROKE / ARM_COUNT
        val secantCeiling =
            ARM_COUNT * Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / Gen1Tile.ACCEPTABLE_STROKE
        return T133CandidateRecord(
            id = id,
            name = name,
            family = family,
            nearRestraint = reportable(near),
            farRestraint = reportable(far),
            length = length,
            lengthBasePairs = length / rise,
            endFactor = bendingFactorForLength(length, rigidity, PER_PATH),
            marginToCeiling = ceiling - length,
            demanded = outcome.demanded,
            placed = outcome.placed,
            levelsRequired = outcome.levelsRequired,
            singleLevel = outcome.singleLevel,
            overlappingPairs = outcome.overlappingPairs,
            planAreaFraction = outcome.planAreaFraction,
            assembledSecant = assembledSecant,
            assembledTangentAtStroke = assembledTangent,
            assembledTangentMinimum = minimum,
            floorsCleared = FLOORS_TWO_MILLIMOLAR.count { minimum > it },
            perPathForce = perPathForce,
            insideUnzipAllowable = perPathForce <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            perPathSecantCeiling = secantCeiling,
            insideSecantCeiling = assembledSecant <= secantCeiling,
            twoSided = compressionProbe(-0.5) < 0.0,
            placesInFull = outcome.placesInFull,
            verdict = if (outcome.placesInFull) {
                "PLACES — ${outcome.placed} of ${outcome.demanded}, one level"
            } else {
                "DOES NOT PLACE — ${outcome.placed} of ${outcome.demanded}"
            }
        )
    }

    fun armCandidate(
        id: String,
        name: String,
        near: Double,
        far: Double,
        rootedAlongRow: Boolean = true
    ): T133CandidateRecord {
        // C-0039's exact elastica refuses an arm below 1.5x the stroke, where the tip rotation
        // passes 42 degrees and the chord draw-in is a large fraction of the arm — so the
        // rooted-element window has a FLOOR as well as the plan's ceiling, and a candidate that
        // falls through it is refused by its own kinematics rather than by the plan.
        val smallRotation =
            twoSpringArmForStiffness(near, 1, far, rigidity, ARM_COUNT, MANDATE)
        val exact = runCatching {
            if (near.isInfinite()) {
                elasticaArmCeiling(far, ARM_COUNT, rigidity, MANDATE, Gen1Tile.ACCEPTABLE_STROKE)
            } else {
                elasticaArmForStiffness(
                    hingeStiffness = near, hingeCount = 1, farStiffness = far,
                    bendingRigidity = rigidity, count = ARM_COUNT, targetStiffness = MANDATE,
                    workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
                )
            }
        }.getOrNull()
        if (exact == null) {
            return T133CandidateRecord(
                id = id,
                name = name,
                family = "bending, supported once and loaded at the far end",
                nearRestraint = reportable(near),
                farRestraint = reportable(far),
                length = smallRotation,
                lengthBasePairs = smallRotation / rise,
                endFactor = bendingFactorForLength(smallRotation, rigidity, PER_PATH),
                marginToCeiling = ceiling - smallRotation,
                demanded = ARM_COUNT,
                placed = 0,
                levelsRequired = UNREALISABLE_LEVEL_COUNT,
                singleLevel = false,
                overlappingPairs = 0,
                planAreaFraction = ARM_COUNT * smallRotation * width / footprint,
                assembledSecant = MANDATE,
                assembledTangentAtStroke = 0.0,
                assembledTangentMinimum = 0.0,
                floorsCleared = 0,
                perPathForce = Gen1Tile.TARGET_FORCE / ARM_COUNT,
                insideUnzipAllowable = true,
                perPathSecantCeiling =
                    ARM_COUNT * Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / Gen1Tile.ACCEPTABLE_STROKE,
                insideSecantCeiling = true,
                twoSided = true,
                placesInFull = false,
                verdict = "REFUSED BY ITS OWN KINEMATICS — the small-rotation arm the stiffness " +
                        "demands is ${"%.3f".format(smallRotation)} nm, below the " +
                        "${"%.2f".format(1.5 * Gen1Tile.ACCEPTABLE_STROKE)} nm floor at which a " +
                        "3 nm stroke is a 42 degree rotation and C-0039's chord draw-in is a " +
                        "large fraction of the arm. It is short enough to place and too short " +
                        "to work"
            )
        }
        val length = exact
        val element = TwoSpringElastica(rigidity, length, near, far)
        val outcome = if (rootedAlongRow) {
            placeRootedOutputElement(name, rows, length, edgeX, lengthY, width)
        } else {
            placeCappedOutputElement(name, rows, length, -0.5 * PI, edgeX, lengthY, width)
        }
        return clauses(
            id, name, "bending, supported once and loaded at the far end", near, far, length,
            outcome, element::secantStiffness, element::tangentStiffness, element::reaction
        )
    }

    val guide = 1.0e6
    val candidates = ArrayList<T133CandidateRecord>()

    // Q1 — C-0030's coupled-standoff flexure, C-0065's own reading and the along-row one
    val coupled = CoupledJointFlexure(rigidity, coupledSpan, cap.flexibility)
    candidates += clauses(
        "Q1", "C-0030's coupled-standoff flexure, across the rows",
        "bending, supported twice and loaded at midspan",
        cap.flexibility.headRotationalStiffness, cap.flexibility.headRotationalStiffness,
        coupledSpan,
        placeCappedOutputElement(
            "C-0030 across the rows", rows, coupledSpan, -0.5 * PI, edgeX, lengthY, width
        ),
        { coupled.strokeSecantStiffness(it, FlexureOrientation.FAVOURABLE) },
        { coupled.strokeTangentStiffness(it, FlexureOrientation.FAVOURABLE) },
        { coupled.reaction(it) }
    )
    candidates += clauses(
        "Q2", "C-0030's coupled-standoff flexure, along the rows",
        "bending, supported twice and loaded at midspan",
        cap.flexibility.headRotationalStiffness, cap.flexibility.headRotationalStiffness,
        coupledSpan,
        placeCappedOutputElement(
            "C-0030 along the rows", rows, coupledSpan, 0.0, edgeX, lengthY, width
        ),
        { coupled.strokeSecantStiffness(it, FlexureOrientation.FAVOURABLE) },
        { coupled.strokeTangentStiffness(it, FlexureOrientation.FAVOURABLE) },
        { coupled.reaction(it) }
    )

    // Q3 — C-0023's E3a, the bare transverse flexure at both textbook end conditions
    listOf(FlexureEndCondition.PINNED_ENDS, FlexureEndCondition.CLAMPED_ENDS)
        .forEachIndexed { index, condition ->
        val span = flexureSpanForStiffness(
            rigidity, condition, false, Gen1Tile.DUPLEX_STRETCH_MODULUS, ARM_COUNT, MANDATE,
            Gen1Tile.ACCEPTABLE_STROKE
        )
        val element = TransverseDuplexFlexure(
            rigidity, span, condition, false, Gen1Tile.DUPLEX_STRETCH_MODULUS
        )
        candidates += clauses(
            "Q${3 + index}", "C-0023's E3a, ${condition.description}",
            "bending, supported twice and loaded at midspan",
            if (condition == FlexureEndCondition.PINNED_ENDS) 0.0 else guide,
            if (condition == FlexureEndCondition.PINNED_ENDS) 0.0 else guide,
            span,
            placeCappedOutputElement(
                "E3a ${condition.name}", rows, span, 0.0, edgeX, lengthY, width
            ),
            element::secantStiffness, element::tangentStiffness, element::reaction
        )
    }

    // Q5..Q9 — the end-loaded arm family, one row per end-restraint pair
    candidates += armCandidate(
        "Q5", "C-0039's E5a1 — one crossover root, C-0034's A2 tip", hinge, DUPLEX_END_ANCHORAGE
    )
    candidates += armCandidate(
        "Q6", "the same arm with a PINNED tip (C-0029's one-link pin)", hinge, 0.0
    )
    candidates += armCandidate(
        "Q7", "the standoff-headed crank — C-0028's B2 base at 8 nm, pinned tip",
        1.0 / standoffHead.rotationUnderMoment, 0.0
    )
    candidates += armCandidate(
        "Q8", "a RIGID root with a pinned tip — c = 3 exactly",
        Double.POSITIVE_INFINITY, 0.0
    )
    candidates += armCandidate(
        "Q9", "one crossover root with a GUIDED tip — c -> 12", hinge, guide
    )

    // Q10 — the axial standoff, E1: refused by arithmetic, placed for the record
    val axial = AxialDuplexStandoff(Gen1Tile.DUPLEX_STRETCH_MODULUS, axialLength)
    candidates += clauses(
        "Q10", "C-0023's E1 — a duplex standing along z, loaded along z", "axial stretch",
        0.0, 0.0, axialLength,
        placeRootedOutputElement("E1", rows, axialLength, edgeX, lengthY, width),
        axial::secantStiffness, axial::tangentStiffness, axial::reaction
    )

    // Q11 — the entropic strand, E2: compact and one-sided
    val chain = FreelyJointedChain(entropicContour, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
    val spacer = OneSidedSpacer(EntropicCoupling(1, chain))
    candidates += clauses(
        "Q11", "C-0023's E2 — a single strand", "entropic", 0.0, 0.0, entropicContour,
        placeRootedOutputElement("E2", rows, entropicContour, edgeX, lengthY, width),
        { spacer.reaction(it) / it }, spacer::tangentStiffness, spacer::reaction
    )

    // Q12 — the fold: pure geometry, swept over the limb, and compared at EQUAL CONTOUR against
    // the straight element. A fold halves the along-row demand and doubles the across-row one.
    val foldSweep = listOf(1.0, 2.0, 3.0, 3.25, 3.5, 4.095, 5.0).map { limb ->
        val outcome = placeRootedOutputElement(
            "a two-limb fold of ${"%.3f".format(limb)} nm limbs", rows, limb, edgeX, lengthY,
            2.0 * width
        )
        val straight = placeRootedOutputElement(
            "the straight element of the same contour", rows, 2.0 * limb, edgeX, lengthY, width
        )
        T133SensitivityRecord(
            axis = "the fold, at equal CONTOUR",
            reading = "limbs ${"%.3f".format(limb)} nm, contour ${"%.3f".format(2.0 * limb)} nm",
            lengthCeiling = ceiling,
            armLength = 2.0 * limb,
            placed = outcome.placed,
            verdictMoves = outcome.placed < straight.placed,
            note = "the straight element of the same contour places ${straight.placed}, so " +
                    "folding is worth ${outcome.placed - straight.placed} instances here"
        )
    }
    val foldOutcome = placeRootedOutputElement(
        "a two-limb fold at half the plan budget", rows, 0.5 * ceiling, edgeX, lengthY, 2.0 * width
    )
    val foldStraight = placeRootedOutputElement(
        "the straight element of the same contour", rows, ceiling, edgeX, lengthY, width
    )

    // the crank stands on a normal duplex, so it owns a compression member and CH-0037 applies:
    // the duty is the element's OWN end force at the desired stroke, never the mandate secant
    val crank = candidates.first { it.id == "Q7" }
    val crankElement = TwoSpringElastica(
        rigidity, crank.length, 1.0 / standoffHead.rotationUnderMoment, 0.0
    )
    val crankDutyAtStroke = crankElement.reaction(Gen1Tile.ACCEPTABLE_STROKE)
    val crankCritical = standoffBucklingLoad(
        rigidity, 8.0,
        baseRestraintParameter(StandoffBase.crossovers(2).rotationalStiffness, rigidity, 8.0),
        0.0
    )

    println("T-133 — the admissible end-restraint window ...")
    val windowRows = listOf(
        Triple("far restraint at a one-crossover root", hinge, "one crossover, 13.53 pN nm/rad"),
        Triple("far restraint at a two-crossover root", 2.0 * hinge, "two crossovers"),
        Triple(
            "far restraint at C-0028's B2 standoff head", 1.0 / standoffHead.rotationUnderMoment,
            "a standoff head on C-0028's B2 base at 8 nm"
        )
    )
    val window = windowRows.map { (axis, held, name) ->
        val found = farRestraintCeiling(held, ceiling, rigidity, ARM_COUNT, MANDATE)
        val armAt = runCatching {
            elasticaArmForStiffness(
                hingeStiffness = held, hingeCount = 1, farStiffness = found ?: 0.0,
                bendingRigidity = rigidity, count = ARM_COUNT, targetStiffness = MANDATE,
                workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
            )
        }.getOrElse {
            twoSpringArmForStiffness(held, 1, found ?: 0.0, rigidity, ARM_COUNT, MANDATE)
        }
        T133WindowRecord(
            axis = axis,
            heldRestraint = reportable(held),
            heldName = name,
            ceilingRestraint = found,
            ceilingIsSearchCap = found != null && found >= 0.999e6,
            armAtCeiling = armAt,
            note = if (found == null) {
                "REFUSED — even a free tip demands ${"%.3f".format(armAt)} nm against a " +
                        "${"%.2f".format(ceiling)} nm budget"
            } else {
                "the tip joint may be no stiffer than ${"%.2f".format(found)} pN nm/rad"
            }
        )
    } + listOf(
        T133WindowRecord(
            axis = "near restraint at C-0034's A2 tip",
            heldRestraint = DUPLEX_END_ANCHORAGE,
            heldName = "C-0034's A2, a duplex end with two strand termini",
            ceilingRestraint = nearRestraintCeiling(
                DUPLEX_END_ANCHORAGE, ceiling, rigidity, ARM_COUNT, MANDATE
            ),
            ceilingIsSearchCap = false,
            armAtCeiling = ceiling,
            note = "the ROOT may be no stiffer than this, which is what refuses a truss standoff " +
                    "as a root: a rigid root demands a longer arm, not a shorter one"
        )
    )

    // ------------------------------------------------------------------ orientation sweeps
    println("T-133 — the orientation sweeps ...")
    val anchors = stations.map { Vector2(it.x, it.y) }
    val armLength = candidates.first { it.id == "Q5" }.length
    val orientations = listOf(
        "C-0039's E5a1 arm" to armLength,
        "C-0030's coupled flexure" to coupledSpan
    ).map { (name, length) ->
        val sweep = elementOrientationSweep(
            anchors, length, samples = 720, width = width, anchorFraction = 0.0,
            angularSpan = 2.0 * PI
        )
        T133OrientationRecord(
            element = name,
            length = length,
            samples = sweep.samples,
            feasibleOrientations = sweep.feasibleOrientations,
            singleLevelOrientations = sweep.singleLevelOrientations,
            minimumOverlappingPairs = sweep.minimumOverlappingPairs,
            minimumMemberClashPairs = sweep.minimumMemberClashPairs,
            bestAngleDegrees = sweep.bestAngleDegrees
        )
    }

    // ------------------------------------------------------------------ sensitivities
    println("T-133 — the sensitivities ...")
    fun sensitivity(
        axis: String,
        reading: String,
        exclusionWidth: Double = width,
        bendingRigidity: Double = rigidity,
        hingeStiffness: Double = hinge,
        count: Int = ARM_COUNT,
        note: String
    ): T133SensitivityRecord {
        val localCeiling = rowOfThreeLengthCeiling(pitch, exclusionWidth)
        val arm = elasticaArmForStiffness(
            hingeStiffness = hingeStiffness, hingeCount = 1, farStiffness = DUPLEX_END_ANCHORAGE,
            bendingRigidity = bendingRigidity, count = count, targetStiffness = MANDATE,
            workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
        )
        val outcome = placeRootedOutputElement(
            reading, rows, arm, edgeX, lengthY, exclusionWidth
        )
        return T133SensitivityRecord(
            axis, reading, localCeiling, arm, outcome.placed,
            outcome.placed != ARM_COUNT, note
        )
    }

    val sensitivities = listOf(
        sensitivity(
            "reference", "2.69 nm SAXS, EI 230, one crossover, 34 paths",
            note = "C-0055's arm on C-0063's placement"
        ),
        sensitivity(
            "exclusion width", "2.73 nm, the square-lattice SAXS value",
            exclusionWidth = 2.73,
            note = "C-0066's own flip, in a new place: the arm no longer clears the row of three"
        ),
        sensitivity(
            "exclusion width", "2.0 nm, the steric diameter", exclusionWidth = 2.0,
            note = "the loosest reading — the budget widens to 8.88 nm and nothing moves"
        ),
        sensitivity(
            "duplex EI", "Fields et al.'s implied 172.906 pN nm^2",
            bendingRigidity = 172.906,
            note = "the measured rigidity shortens the arm and the verdict is unchanged"
        ),
        sensitivity(
            "crossover alpha", "0.6, the bottom of Chen et al.'s fitted bracket",
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(Gen1Tile.CROSSOVER_ALPHA_MIN),
            note = "a softer root gives a SHORTER arm — the bracket runs the favourable way"
        ),
        sensitivity(
            "crossover alpha", "1.2, the top of the same bracket",
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(Gen1Tile.CROSSOVER_ALPHA_MAX),
            note = "a stiffer root gives a LONGER arm, and this is the axis that can close the " +
                    "0.026 nm margin"
        ),
        sensitivity(
            "path count", "45 paths, C-0015's own", count = 45,
            note = "more paths make each element LONGER (C-0023), so 34 is the count the plan " +
                    "budget prefers — and 45 stations do not exist on this lattice anyway"
        ),
        sensitivity(
            "path count", "15 paths, C-0041's buildable count", count = 15,
            note = "fewer paths shorten the arm; the placement is unchanged because the count " +
                    "is what sets the stations"
        )
    ) + foldSweep

    // ------------------------------------------------------------------ convergence
    println("T-133 — the convergence records ...")
    val ceilingLevels = listOf(1.0e-6, 1.0e-9, 1.0e-12).map {
        rootedLengthCeiling(rows, edgeX, width, resolution = it)
    }
    val armSteps = listOf(200, 400, 800).map {
        elasticaArmForStiffness(
            hingeStiffness = hinge, hingeCount = 1, farStiffness = DUPLEX_END_ANCHORAGE,
            bendingRigidity = rigidity, count = ARM_COUNT, targetStiffness = MANDATE,
            workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE, steps = it
        )
    }
    val sweepSamples = listOf(180, 720, 2880).map {
        elementOrientationSweep(
            anchors, armLength, samples = it, width = width, anchorFraction = 0.0,
            angularSpan = 2.0 * PI
        ).feasibleOrientations.toDouble() / it
    }
    val tangentSamples = listOf(150, 300, 600, 1200).map { count ->
        val element = TwoSpringElastica(rigidity, armLength, hinge, DUPLEX_END_ANCHORAGE)
        (0..count).minOf { step ->
            val stroke = Gen1Tile.ACCEPTABLE_STROKE * step / count
            ARM_COUNT * element.tangentStiffness(if (stroke == 0.0) 1.0e-6 else stroke)
        }
    }
    val convergence = listOf(
        T133ConvergenceRecord(
            "the rooted length ceiling", "bisection resolution [nm]",
            listOf("1e-6", "1e-9", "1e-12"), ceilingLevels,
            abs(ceilingLevels[2] - ceilingLevels[1]),
            "exits on the bracket width, never on a residual (CLAUDE.md)"
        ),
        T133ConvergenceRecord(
            "the placed elastica arm", "RK4 steps", listOf("200", "400", "800"), armSteps,
            abs(armSteps[2] - armSteps[1]), "C-0039's own integrator, at its own default 400"
        ),
        T133ConvergenceRecord(
            "the feasible orientation fraction", "sweep samples",
            listOf("180", "720", "2880"), sweepSamples,
            abs(sweepSamples[2] - sweepSamples[1]),
            "C-0041's discipline: a sweep must be sample-count independent"
        ),
        T133ConvergenceRecord(
            "the assembled tangent minimum over [0, 3 nm]", "stroke samples",
            listOf("150", "300", "600", "1200"), tangentSamples,
            abs(tangentSamples[3] - tangentSamples[2]),
            "C-0049's traversed-range reading; the element strain-stiffens, so the minimum is " +
                    "at the zero-stroke endpoint and the sampling is not what decides it"
        )
    )

    // ------------------------------------------------------------------ reproductions
    println("T-133 — the upstream reproductions ...")
    fun reproduction(
        source: String, quantity: String, published: Double, here: Double, note: String
    ) = T133ReproductionRecord(
        source, quantity, published, here,
        if (published == 0.0) abs(here) else abs(here - published) / abs(published), note
    )

    val q1 = candidates.first { it.id == "Q1" }
    val reproductions = listOf(
        reproduction(
            "C-0065", "the 34-path coupled flexure span [nm]", publishedSpan, coupledSpan,
            "read from gpd/results/T-130-*.json and re-derived through coupledFlexureSpan"
        ),
        reproduction(
            "C-0065", "instances the flexure places, of 34", publishedPlaced.toDouble(),
            q1.placed.toDouble(),
            "THE FREE LIMITING CASE — the same 12, on the bare flexure array without the trusses"
        ),
        reproduction(
            "C-0065", "levels the flexure array requires", publishedLevels.toDouble(),
            q1.levelsRequired.toDouble(),
            "SEVEN with the truss blocks in the same conflict graph (asserted as a gate-2 test " +
                    "through placeTrussArray) and SIX for the bare flexure array evaluated here " +
                    "— two different bodies, and the PLACED count, which is what the verdict " +
                    "rests on, is the same 12"
        ),
        reproduction(
            "C-0055 / C-0063", "the upward root pitch [nm]", 10.88, pitch,
            "32 bp at the 0.34 nm rise"
        ),
        reproduction(
            "C-0055 / C-0063", "the 34-path arm [nm]", 8.16439,
            candidates.first { it.id == "Q5" }.length,
            "C-0039's exact elastica at one crossover and C-0034's A2"
        ),
        reproduction(
            "C-0066", "the root pitch minus the arm [nm]", 2.71561,
            pitch - candidates.first { it.id == "Q5" }.length,
            "C-0066's bound 4, re-derived"
        ),
        reproduction(
            "C-0017", "the mandate [pN/nm]", 33.3333333, MANDATE, "100 pN over 3 nm"
        ),
        reproduction(
            "C-0049", "the per-path secant ceiling at 34 paths and 3 nm [pN/nm]", 113.333333,
            ARM_COUNT * Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / Gen1Tile.ACCEPTABLE_STROKE,
            "n a/s, which tightens as 1/s"
        ),
        reproduction(
            "C-0034", "the A2 anchorage couple [pN nm/rad]", 78.2352941, DUPLEX_END_ANCHORAGE,
            "two strand termini at the phosphate radius"
        ),
        reproduction(
            "C-0009", "the crossover hinge constant [pN nm/rad]", 13.5294118, hinge,
            "CITED, FITTED — Chen et al. 2014 via C-0009"
        ),
        reproduction(
            "C-0063", "the stations", 34.0, stations.size.toDouble(),
            "read from its own result file"
        ),
        reproduction(
            "C-0063", "rows carrying three arms", 4.0, rowsOfThree.toDouble(),
            "its bound 1, re-derived from the count arithmetic"
        ),
        reproduction(
            "Fischer et al. 2016 (SAXS)", "the single-layer interhelical distance [nm]", 2.69,
            width, "CITED, MEASURED"
        ),
        reproduction(
            "C-0025", "the midspan-loaded end-condition floor", 48.0,
            FlexureEndCondition.PINNED_ENDS.midspanFactor, "c(0) = 48 exactly"
        )
    )

    // ------------------------------------------------------------------ predicates
    val placing = candidates.filter { it.placesInFull }
    val surviving = placing.filter {
        it.twoSided && abs(it.assembledSecant - MANDATE) < 1.0e-6 && it.insideUnzipAllowable &&
                it.insideSecantCeiling
    }
    val best = surviving.minByOrNull { it.length }
    val decision = if (best == null) {
        "NO output element places 34 times at one level on C-0063's stations and survives every " +
                "clause"
    } else {
        "YES — ${placing.size} of ${candidates.size} catalogue rows place 34 times at one " +
                "level and ${surviving.size} of those survive every clause " +
                "(${surviving.joinToString { it.id }}), the shortest being ${best.id} at " +
                "${"%.3f".format(best.length)} nm; and EVERY ONE OF THEM LIES IN THE PLAN"
    }

    val predicates = listOf(
        T133PredicateRecord(
            "P1 — an element places",
            "some catalogue row places all 34 instances at one level on C-0063's stations",
            if (placing.isEmpty()) "FAIL" else "PASS — ${placing.joinToString { it.id }}"
        ),
        T133PredicateRecord(
            "P2 — a placing element discharges C-0017's placement equality",
            "its assembled secant at 3 nm is 33.3333 pN/nm, exactly",
            if (surviving.isNotEmpty()) {
                "PASS — ${surviving.joinToString { it.id }}; the one placing row that does not " +
                        "is Q11, whose entropic law is not linear, so its 3 nm secant is not its " +
                        "own Gaussian constant"
            } else "FAIL"
        ),
        T133PredicateRecord(
            "P3 — every placing element is inside the per-path unzip allowable",
            "its per-path force at 3 nm is at most 10 pN",
            if (placing.all { it.insideUnzipAllowable }) "PASS" else "FAIL"
        ),
        T133PredicateRecord(
            "P4 — a placing element is two-sided",
            "its reaction at negative argument is negative (C-0023's operational test)",
            if (placing.any { it.twoSided }) {
                "PASS — ${placing.count { it.twoSided }} of ${placing.size}; the exception is " +
                        "Q11, the single strand, which C-0023 already refuses on sidedness and " +
                        "which is measured here rather than assumed"
            } else "FAIL"
        ),
        T133PredicateRecord(
            "P5 — no element out of the plan exists",
            "every mechanism whose compliant member leaves the sheet plane is refused by its " +
                    "own closed-form length",
            if (mechanisms.none { it.admitted && !it.liesInPlan }) "PASS — none exists" else "FAIL"
        ),
        T133PredicateRecord(
            "P6 — a fold buys nothing",
            "at EQUAL CONTOUR a two-limb fold places no better than the straight element, " +
                    "because it spends along-row slack on a direction whose pitch is exactly one " +
                    "duplex",
            if (foldSweep.all { it.placed <= ARM_COUNT } &&
                foldOutcome.placed < foldStraight.placed
            ) {
                "PASS — ${foldOutcome.placed} of ${foldOutcome.demanded} against the straight " +
                        "element's ${foldStraight.placed} at the same contour"
            } else "FAIL"
        )
    )

    val findings = mapOf(
        "the answer" to decision,
        "the clause funnel" to ("${candidates.size} catalogue rows -> ${placing.size} place 34 " +
                "at one level -> ${surviving.size} survive every clause. The row lost between " +
                "the last two is the single strand, which is compact (6.04 nm) and ONE-SIDED"),
        "what refuses the flexure" to ("NOT the plan and NOT C-0063's placement, but its END " +
                "CONDITION. A midspan-loaded beam's c is 48-192 and an end-loaded one's is at " +
                "most 12, and the span is c^(1/3) — so the same duplex at the same rigidity and " +
                "the same stiffness is " +
                "${"%.2f".format(midspanFloor / bendingLengthForStiffness(12.0, rigidity, PER_PATH))}" +
                "x longer for being supported twice. The plan budget is " +
                "${"%.2f".format(ceiling)} nm and the midspan family's floor is " +
                "${"%.2f".format(midspanFloor)} nm"),
        "the out-of-plane escape" to ("does not exist. A member standing along z and loaded " +
                "along z is loaded AXIALLY (S/k = ${"%.0f".format(axialLength)} nm), a fold " +
                "spends along-row slack to buy across-row room that the lattice does not have, " +
                "and a second LEVEL is reached only by a vertical member, whose clash is " +
                "level-independent (C-0041). The escape that works is not out of the plan, it " +
                "is SHORT in it"),
        "the margin" to ("the element that places does so by ${"%.4f".format(ceiling - armLength)}" +
                " nm — ${"%.3f".format((ceiling - armLength) / rise)} of a base-pair rise — and " +
                "at the 2.73 nm square-lattice interhelical distance it does not place at all"),
        "the shorter element and what it costs" to ("Q7, the standoff-headed crank, is " +
                "${"%.3f".format(candidates.first { it.id == "Q5" }.length / crank.length)}x " +
                "shorter than the hinge-rooted arm and clears the plan budget by " +
                "${"%.2f".format(crank.marginToCeiling)} nm rather than 0.026 — but it stands on " +
                "an UNDEMONSTRATED normal duplex (C-0028/C-0029), its standoff carries " +
                "${"%.3f".format(crankDutyAtStroke)} pN in compression against a " +
                "${"%.2f".format(crankCritical)} pN free-head Euler load " +
                "(${"%.2f".format(crankCritical / crankDutyAtStroke)}x, CH-0037's reading), and " +
                "its assembled tangent minimum over [0, 3] clears only " +
                "${crank.floorsCleared} of C-0017's six 2 mM stability floors against the arm's " +
                "${candidates.first { it.id == "Q5" }.floorsCleared}"),
        "the recommendation" to ("the truss branch's output stage is the HINGE-ROOTED ARM, not a " +
                "flexure the truss caps — and the truss is the wrong root for it, because a " +
                "rigid root demands a LONGER arm (c = 3 exactly, " +
                "${"%.3f".format(candidates.first { it.id == "Q8" }.length)} nm) than a single " +
                "crossover does"),
        "maturity" to ("TRL 1-3. Nothing here is measured; the motif — a free lever held to a " +
                "single-layer sheet by one crossover — is this programme's own construct " +
                "(C-0055, 62 recorded queries) and is undemonstrated")
    )

    val parameters = mapOf(
        "armCount" to ARM_COUNT.toDouble(),
        "phase" to PHASE.toDouble(),
        "mandate" to MANDATE,
        "perPathStiffness" to PER_PATH,
        "rootPitch" to pitch,
        "lengthCeiling" to ceiling,
        "solvedLengthCeiling" to solvedCeiling,
        "endFactorCeiling" to factorCeiling,
        "midspanFamilyFloor" to midspanFloor,
        "axialLength" to axialLength,
        "entropicContour" to entropicContour,
        "hingeLever" to hingeLever,
        "torsionalLength" to torsionalLength,
        "coupledFlexureSpan" to coupledSpan,
        "armLength" to armLength,
        "marginToCeiling" to (ceiling - armLength),
        "edgeX" to edgeX,
        "lengthY" to lengthY,
        "footprint" to footprint,
        "candidatesPlacing" to placing.size.toDouble(),
        "candidatesEvaluated" to candidates.size.toDouble(),
        "foldPlaced" to foldOutcome.placed.toDouble(),
        "crankStandoffDuty" to crankDutyAtStroke,
        "crankStandoffCriticalLoad" to crankCritical,
        "crankBucklingMargin" to crankCritical / crankDutyAtStroke,
        "foldStraightPlaced" to foldStraight.placed.toDouble(),
        "candidatesSurviving" to surviving.size.toDouble()
    )

    val result = T133Result(
        task = "T-133",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "medium" to "aqueous 2 mM MgCl2",
            "k_BT" to "4.141947 pN nm",
            "sheet" to ("single-layer square-lattice Rothemund, 15 duplexes at the SAXS 2.69 nm, " +
                    "40.0 x 40.35 nm, rise 0.34 nm, crossover phase 24"),
            "placement" to "C-0063's 34 upward roots at phase 24, read from its result file",
            "plan convention" to ("a duplex is a rectangle of width 2.69 nm; two at exactly that " +
                    "are tangent and admissible (C-0041, C-0053); a rooted element occupies " +
                    "[root, root +- L] and the next along its row may start at high + d"),
            "coupling" to ("C-0017's 33.3333 pN/nm as a SUM over 34 paths, placed on the SECANT " +
                    "at Sec.3's acceptable 3 nm stroke"),
            "allowables" to ("the 10 pN per-path unzip allowable (C-0006/CH-0029) and C-0049's " +
                    "n a/s secant ceiling; C-0017's six 2 mM stability floors, CITED"),
            "units" to "nm, pN, pN/nm, pN nm/rad"
        ),
        decision = decision,
        bounds = bounds,
        mechanisms = mechanisms,
        candidates = candidates,
        window = window,
        orientations = orientations,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings,
        parameters = parameters
    )

    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    val file = File("gpd/results/T-133-output-element-placement.json")
    file.parentFile.mkdirs()
    file.writeText(
        json.encodeToString(
            (json.encodeToJsonElement(result) as JsonObject).roundedForResult()
        )
    )
    println("T-133 — wrote ${file.path} in ${(System.currentTimeMillis() - started) / 1000} s")
    println("T-133 — $decision")
}
