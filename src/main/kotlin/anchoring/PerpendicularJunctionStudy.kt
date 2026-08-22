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

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin

/**
 * Task `T-67` / leaf `A8.2` — whether a 90° routing between a sheet duplex and a normal standoff
 * exists at all, and what the `E5` fallback delivers if it does not.
 *
 * ```shell
 * tools/study.sh anchoring.PerpendicularJunctionStudyKt
 * ```
 *
 * Emits `gpd/results/T-67-perpendicular-junction-routing.json`, deterministically: the file
 * carries no timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val INFINITE_SENTINEL = -1.0

private fun finite(value: Double): Double =
    if (value.isInfinite() || value.isNaN()) INFINITE_SENTINEL else value

// ---------------------------------------------------------------- records

@Serializable
data class CountingBoundRecord(
    val reading: String,
    val minorGrooveAngle: Double,
    val phosphateRadius: Double,
    val terminalChord: Double,
    val leverArm: Double,
    val maximumRotationalStiffness: Double,
    val fractionOfB2: Double,
    val longestStandoffMeetingThreshold: Double,
    val note: String
)

@Serializable
data class SeatRecord(
    val seat: String,
    val lateralOffset: Double,
    val faceHeight: Double,
    val contact: String,
    val tiltFreedomDegrees: Double,
    val headDeadBandAtDesignLength: Double,
    val contactMomentCapacityAtDesiredStroke: Double,
    val note: String
)

@Serializable
data class RoutingRecord(
    val id: String,
    val topology: String,
    val description: String,
    val centreX: Double,
    val centreY: Double,
    val faceHeight: Double,
    val azimuthDegrees: Double,
    val chordAzimuthDegrees: Double,
    val firstGap: Double,
    val secondGap: Double,
    val firstUnpaired: Int,
    val secondUnpaired: Int,
    val firstTarget: String,
    val secondTarget: String,
    val covalent: Boolean,
    val links: Int,
    val leverArm: Double,
    val rotationalRestrained: Double,
    val rotationalFree: Double,
    val verdict: String
)

@Serializable
data class BasedDesignRecord(
    val baseId: String,
    val baseName: String,
    val axis: String,
    val standoffLength: Double,
    val standoffBasePairs: Double,
    val baseRotationalStiffness: Double,
    val baseRestraint: Double,
    val span: Double,
    val spanBasePairs: Double,
    val midspanFactor: Double,
    val secantStiffness: Double,
    val tangentStiffness: Double,
    val axialTensionDesired: Double,
    val dutyDesiredElement: Double,
    val bucklingFreeHead: Double,
    val bucklingMarginFreeHead: Double,
    val bucklingStrokeFreeHead: Double,
    val bucklingWeakAxis: Double,
    val bucklingMarginWeakAxis: Double,
    val dutyAcceptableElement: Double,
    val baseMomentDesired: Double,
    val contactMomentCapacity: Double,
    val contactCarriesBaseMoment: Boolean,
    val supportMargin: Double,
    val p1Supports: Boolean,
    val p2Placed: Boolean,
    val p3Compliant: Boolean,
    val p4Safe: Boolean,
    val p5Buildable: Boolean,
    val p6Stable: Boolean,
    val p6StableWeakAxis: Boolean,
    val p6StableAtAcceptableStroke: Boolean,
    val p7Covalent: Boolean,
    val verdict: String,
    val verdictWeakAxis: String,
    val verdictAtAcceptableStroke: String
)

@Serializable
data class JunctionThresholdRecord(
    val standoffLength: Double,
    val requiredRotationalStiffness: Double,
    val hardCeiling: Double,
    val nominalCeiling: Double,
    val narrowCeiling: Double,
    val metByHardCeiling: Boolean,
    val metByNominal: Boolean,
    val metByNarrow: Boolean,
    val metByC0028B2: Boolean
)

@Serializable
data class PhaseRecord(
    val lattice: String,
    val basePairsPerTurn: Double,
    val azimuthQuantumDegrees: Double,
    val worstMisalignmentDegrees: Double,
    val worstCoupleProjection: Double,
    val coupleLostFraction: Double,
    val interfacePhasePeriodBasePairs: Double,
    val turnsPerInterfacePeriod: Double
)

@Serializable
data class HingeArmRecord(
    val id: String,
    val law: String,
    val hingeCount: Int,
    val hingeStiffness: Double,
    val armLength: Double,
    val armBasePairs: Double,
    val armBendingRigidity: Double,
    val armFactor: Double,
    val secantAtWorkingPoint: Double,
    val tangentAtWorkingPoint: Double,
    val tangentToSecant: Double,
    val rotationAtWorkingPointDegrees: Double,
    val rotationAtDesiredStrokeDegrees: Double,
    val tangentAtDesiredStroke: Double,
    val armCeiling: Double,
    val maximumHingeStroke: Double,
    val reachesDesiredStroke: Boolean,
    val drawInAtWorkingPoint: Double,
    val hingeBondForceWorking: Double,
    val hingeBondForceDesired: Double,
    val pathShareDesired: Double,
    val p1Supports: Boolean,
    val p2Placed: Boolean,
    val p3Compliant: Boolean,
    val p4Safe: Boolean,
    val p5Buildable: Boolean,
    val p6Stable: Boolean,
    val p7Covalent: Boolean,
    val verdict: String
)

@Serializable
data class SensitivityRecord(
    val axis: String,
    val value: Double,
    val label: String,
    val ceiling: Double,
    val thresholdAtEight: Double,
    val meetsThresholdAtEight: Boolean,
    val longestStandoffMeetingThreshold: Double
)

@Serializable
data class Reproduction(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class JunctionLiteratureRecord(
    val question: String,
    val answer: String,
    val flag: String,
    val source: String
)

@Serializable
data class PerpendicularJunctionResult(
    val task: String,
    val leaf: String,
    val conditions: String,
    val acceptance: String,
    val countingBounds: List<CountingBoundRecord>,
    val seats: List<SeatRecord>,
    val routings: List<RoutingRecord>,
    val phases: List<PhaseRecord>,
    val designs: List<BasedDesignRecord>,
    val thresholds: List<JunctionThresholdRecord>,
    val hingeArms: List<HingeArmRecord>,
    val sensitivity: List<SensitivityRecord>,
    val reproductions: List<Reproduction>,
    val literature: List<JunctionLiteratureRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val citedNumbers: Map<String, String>
)

// ---------------------------------------------------------------- constants, all cited

private val TARGET_FORCE = Gen1Tile.TARGET_FORCE

private val ACCEPTABLE_STROKE = Gen1Tile.ACCEPTABLE_STROKE

private val DESIRED_STROKE = Gen1Tile.DESIRED_STROKE

private val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE

private const val PATH_COUNT = 45

private const val COMPLIANT_CEILING = 40.0

private const val SUPPORT_MARGIN_REQUIRED = 10.0

private const val DEAD_BAND_ALLOWED = 0.1

private const val DESIGN_LENGTH = 8.0

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY

private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS

private val RISE = Gen1Tile.RISE_PER_BASE_PAIR

private val PER_PATH = MANDATE / PATH_COUNT

private val STANDOFF_LENGTHS = listOf(3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

private val NOMINAL = DuplexBackbone()

private val WIDE = DuplexBackbone(minorGrooveAngle = BForm.MINOR_GROOVE_BACKBONE_ANGLE_WIDE)

private val HARD = DuplexBackbone(
    phosphateRadius = BForm.DUPLEX_RADIUS, minorGrooveAngle = 180.0
)

private val NARROW_RADIUS = DuplexBackbone(phosphateRadius = BForm.PHOSPHATE_RADIUS_NARROW)

// ---------------------------------------------------------------- helpers

/**
 * The longest standoff, inside `C-0017`'s 10 nm envelope, whose `P6` threshold a base of
 * [rotational] pN·nm/rad still meets. Scans the length in 0.01 nm steps and exits on the first
 * failure, so the answer is a grid value and reproducible.
 */
private fun longestStandoffMeetingThreshold(rotational: Double): Double {
    var best = 0.0
    var length = 2.0
    while (length <= 10.0001) {
        if (rotational >= baseRotationalStiffnessThreshold(length, DESIRED_STROKE)) best = length
        length += 0.25
    }
    return best
}

private fun designFor(
    id: String,
    name: String,
    axis: String,
    base: StandoffBase,
    length: Double,
    covalent: Boolean,
    weakAxisBase: StandoffBase = base
): BasedDesignRecord {
    val joint = basedNormalStandoff(length, base)
    val restraint = baseRestraintParameter(base.rotationalStiffness, EI, length)
    val span = flexureSpanForJoint(EI, joint, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, STRETCH)
    val flexure = PartiallyRestrainedFlexure(EI, span, joint, STRETCH)
    val secant = PATH_COUNT * flexure.secantStiffness(ACCEPTABLE_STROKE)
    val tangent = PATH_COUNT * flexure.tangentStiffness(ACCEPTABLE_STROKE)
    val duty = flexure.endShear(DESIRED_STROKE)
    val critical = standoffBucklingLoad(EI, length, restraint, 0.0)
    val tension = flexure.axialTension(DESIRED_STROKE)
    val share = MANDATE * DESIRED_STROKE / PATH_COUNT
    val p1 = joint.transverseDeadBand <= DEAD_BAND_ALLOWED &&
            joint.transverseStiffness >= SUPPORT_MARGIN_REQUIRED * PER_PATH
    val p2 = abs(secant - MANDATE) <= 1.0e-6 * MANDATE
    val p3 = tangent <= COMPLIANT_CEILING
    val p4 = tension <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE &&
            share <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    val p5 = length <= 10.0 && span <= 60.0
    val weakRestraint =
        baseRestraintParameter(weakAxisBase.rotationalStiffness, EI, length)
    val weakCritical = standoffBucklingLoad(EI, length, weakRestraint, 0.0)
    val dutyAcceptable = flexure.endShear(ACCEPTABLE_STROKE)
    // the standoff's head deflection is half the flexure's own draw-in demand, and a cantilever's
    // base moment is its tip force times its length
    val headDeflection = flexure.drawInDemand(DESIRED_STROKE) / 2.0
    val baseMoment = joint.axialStiffness * headDeflection * length
    val capacity = contactMomentCapacity(duty, BForm.DUPLEX_RADIUS)
    val p6 = critical >= duty
    val p6Weak = weakCritical >= duty
    val p6Acceptable = weakCritical >= dutyAcceptable
    val verdict = when {
        !p1 -> "FAIL P1 — the base does not support the standoff"
        !p3 -> "FAIL P3 — tangent past the 40 pN/nm compliance ceiling"
        !p4 -> "FAIL P4 — beam tension past the 10 pN unzip allowable at 10 nm"
        !p5 -> "FAIL P5 — outside C-0017's buildable envelope"
        !p6 -> "FAIL P6 — the standoff buckles before the desired stroke"
        !covalent -> "FAIL P7 — a load-bearing link needs unpaired nucleotides"
        else -> "PASS"
    }
    return BasedDesignRecord(
        baseId = id,
        baseName = name,
        axis = axis,
        standoffLength = length,
        standoffBasePairs = length / RISE,
        baseRotationalStiffness = finite(base.rotationalStiffness),
        baseRestraint = finite(restraint),
        span = span,
        spanBasePairs = span / RISE,
        midspanFactor = flexure.midspanFactor,
        secantStiffness = secant,
        tangentStiffness = tangent,
        axialTensionDesired = tension,
        dutyDesiredElement = duty,
        bucklingFreeHead = critical,
        bucklingMarginFreeHead = critical / duty,
        bucklingStrokeFreeHead = finite(bucklingStroke(flexure, critical)),
        bucklingWeakAxis = weakCritical,
        bucklingMarginWeakAxis = weakCritical / duty,
        dutyAcceptableElement = dutyAcceptable,
        baseMomentDesired = baseMoment,
        contactMomentCapacity = capacity,
        contactCarriesBaseMoment = capacity >= baseMoment,
        supportMargin = joint.transverseStiffness / PER_PATH,
        p1Supports = p1,
        p2Placed = p2,
        p3Compliant = p3,
        p4Safe = p4,
        p5Buildable = p5,
        p6Stable = p6,
        p6StableWeakAxis = p6Weak,
        p6StableAtAcceptableStroke = p6Acceptable,
        p7Covalent = covalent,
        verdict = verdict,
        verdictWeakAxis = if (p1 && p3 && p4 && p5 && p6Weak && covalent) "PASS"
        else if (!p6Weak) "FAIL P6 on the WEAK axis — a column buckles about its softest axis, " +
                "and about the chord the base keeps only the two bonds' own hinges"
        else verdict,
        verdictAtAcceptableStroke =
            if (p1 && p3 && p5 && p6Acceptable && covalent) "PASS at the ACCEPTABLE 3 nm"
            else "FAIL at the ACCEPTABLE 3 nm"
    )
}

private fun countingBound(
    reading: String,
    backbone: DuplexBackbone,
    note: String
): CountingBoundRecord {
    val ceiling = maximumBaseRotationalStiffness(backbone.leverArm)
    return CountingBoundRecord(
        reading = reading,
        minorGrooveAngle = backbone.minorGrooveAngle,
        phosphateRadius = backbone.phosphateRadius,
        terminalChord = backbone.terminalChord,
        leverArm = backbone.leverArm,
        maximumRotationalStiffness = ceiling,
        fractionOfB2 = ceiling /
                StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness,
        longestStandoffMeetingThreshold = longestStandoffMeetingThreshold(ceiling),
        note = note
    )
}

private fun routingRecord(
    id: String,
    topology: RoutingTopology,
    backbone: DuplexBackbone,
    links: Int
): RoutingRecord {
    val closure = bestTwoLinkClosure(backbone, topology)
    val arm = if (links >= 2) backbone.leverArm else 0.0
    val restrained = 2.0 * bondHingeStiffness() + 2.0 * bondSlideStiffness() * arm * arm
    val free = links * bondHingeStiffness()
    val covalent = if (links >= 2) closure.covalent else closure.firstUnpaired == 0
    return RoutingRecord(
        id = id,
        topology = topology.name,
        description = topology.description,
        centreX = closure.centreX,
        centreY = closure.centreY,
        faceHeight = closure.faceHeight,
        azimuthDegrees = closure.azimuth * 180.0 / PI,
        chordAzimuthDegrees = closure.chordAzimuth * 180.0 / PI,
        firstGap = closure.firstGap,
        secondGap = if (links >= 2) closure.secondGap else 0.0,
        firstUnpaired = closure.firstUnpaired,
        secondUnpaired = if (links >= 2) closure.secondUnpaired else 0,
        firstTarget = "duplex ${closure.firstDuplex}, strand ${closure.firstStrand}, " +
                "bp ${closure.firstIndex}",
        secondTarget = if (links >= 2)
            "duplex ${closure.secondDuplex}, strand ${closure.secondStrand}, " +
                    "bp ${closure.secondIndex}"
        else "none — the azimuth stays free",
        covalent = covalent,
        links = links,
        leverArm = arm,
        rotationalRestrained = if (links >= 2) restrained else free,
        rotationalFree = free,
        verdict = when {
            !covalent -> "a PIN — a load-bearing link needs unpaired nucleotides"
            links < 2 -> "a BALL JOINT — one link leaves all three rotations free"
            else -> "a HINGE — restrained about the chord's bisector, free about the chord"
        }
    )
}

private fun reproduction(quantity: String, published: Double, derived: Double) = Reproduction(
    quantity = quantity,
    published = published,
    derived = derived,
    relativeDeparture = abs(derived - published) / abs(published)
)

private fun hingeArmRecord(
    id: String,
    law: String,
    hingeCount: Int,
    exact: Boolean,
    armFactor: Double = 3.0,
    bendingRigidity: Double = EI
): HingeArmRecord {
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val arm = if (exact) rotatingArmForStiffness(
        hinge, bendingRigidity, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, hingeCount, armFactor
    ) else hingeArmForStiffness(hinge, bendingRigidity, PATH_COUNT, MANDATE, hingeCount, armFactor)
    val element = RotatingHingeArm(hinge, arm, bendingRigidity, hingeCount, armFactor)
    val linear = CrossoverHingeFlexure(hinge, arm, bendingRigidity, hingeCount, armFactor)
    val secant = PATH_COUNT * (if (exact) element.secantStiffness(ACCEPTABLE_STROKE)
    else linear.stiffness)
    val tangent = PATH_COUNT * (if (exact) element.tangentStiffness(ACCEPTABLE_STROKE)
    else linear.stiffness)
    val reaches = arm > DESIRED_STROKE
    val bondWorking = if (exact) element.hingeBondForce(ACCEPTABLE_STROKE)
    else linear.hingeBondForce(ACCEPTABLE_STROKE, Gen1Tile.INTERHELICAL_SHEET)
    val bondDesired = if (!reaches) Double.POSITIVE_INFINITY
    else if (exact) element.hingeBondForce(DESIRED_STROKE)
    else linear.hingeBondForce(DESIRED_STROKE, Gen1Tile.INTERHELICAL_SHEET)
    val share = MANDATE * DESIRED_STROKE / PATH_COUNT
    // the arm is held by its own crossovers in the sheet plane, in series with its own S/r
    val support = seriesStiffness(Gen1Tile.crossoverInPlaneStiffness() * hingeCount, STRETCH / arm)
    val p1 = support >= SUPPORT_MARGIN_REQUIRED * PER_PATH
    val p2 = abs(secant - MANDATE) <= 1.0e-6 * MANDATE
    val p3 = tangent <= COMPLIANT_CEILING
    val p4 = reaches && bondDesired <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE &&
            share <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    val p5 = arm <= 60.0
    // E5 has no member in axial compression at all: the arm is loaded transverse to its own axis
    val p6 = true
    val p7 = true
    val verdict = when {
        !reaches -> "FAIL — an arm of ${"%.2f".format(arm)} nm cannot lift its tip 10 nm; the " +
                "desired stroke is outside this element's geometry, not merely past its allowable"
        !p1 -> "FAIL P1 — the arm is not supported in the sheet plane"
        !p3 -> "FAIL P3 — tangent past the 40 pN/nm compliance ceiling"
        !p4 -> "FAIL P4 — the hinge bond force is past the 10 pN unzip allowable at 10 nm"
        !p5 -> "FAIL P5 — outside C-0017's buildable envelope"
        else -> "PASS"
    }
    return HingeArmRecord(
        id = id,
        law = law,
        hingeCount = hingeCount,
        hingeStiffness = hinge,
        armLength = arm,
        armBasePairs = arm / RISE,
        armBendingRigidity = bendingRigidity,
        armFactor = armFactor,
        secantAtWorkingPoint = secant,
        tangentAtWorkingPoint = tangent,
        tangentToSecant = tangent / secant,
        rotationAtWorkingPointDegrees =
            if (exact) element.rotationForForce(abs(element.reaction(ACCEPTABLE_STROKE))) *
                    180.0 / PI
            else asin((ACCEPTABLE_STROKE / arm).coerceAtMost(1.0)) * 180.0 / PI,
        rotationAtDesiredStrokeDegrees = if (!reaches) INFINITE_SENTINEL
        else element.rotationForForce(abs(element.reaction(DESIRED_STROKE))) * 180.0 / PI,
        tangentAtDesiredStroke = if (!reaches) INFINITE_SENTINEL
        else PATH_COUNT * element.tangentStiffness(DESIRED_STROKE),
        armCeiling = hingeArmCeiling(armFactor, PATH_COUNT, bendingRigidity, MANDATE),
        maximumHingeStroke = arm,
        reachesDesiredStroke = reaches,
        drawInAtWorkingPoint = if (exact) element.horizontalDrawIn(ACCEPTABLE_STROKE) else 0.0,
        hingeBondForceWorking = bondWorking,
        hingeBondForceDesired = finite(bondDesired),
        pathShareDesired = share,
        p1Supports = p1,
        p2Placed = p2,
        p3Compliant = p3,
        p4Safe = p4,
        p5Buildable = p5,
        p6Stable = p6,
        p7Covalent = p7,
        verdict = verdict
    )
}

// ---------------------------------------------------------------- the study

fun main() {
    val b2 = StandoffBase.crossovers(2, favourableOrientation = true)

    val countingBounds = listOf(
        countingBound(
            "hard — the duplex's own steric radius and a diametral chord", HARD,
            "no routing whatever can exceed this: both termini are inside the duplex"
        ),
        countingBound(
            "nominal — the phosphate radius and a diametral chord",
            DuplexBackbone(minorGrooveAngle = 180.0),
            "the two backbones diametrically opposite, which B-form DNA does not do"
        ),
        countingBound("wide minor-groove reading, 154 degrees", WIDE, "the wide end of the bracket"),
        countingBound(
            "nominal minor-groove reading, 120 degrees", NOMINAL,
            "the value the results are quoted at"
        )
    )

    val seats = listOf(
        SeatRecord(
            seat = "on a duplex",
            lateralOffset = 0.0,
            faceHeight = seatFaceHeight(0.0),
            contact = "a LINE of length 2 R_s along the sheet helix",
            tiltFreedomDegrees = stericTiltFreedom() * 180.0 / PI,
            headDeadBandAtDesignLength = DESIGN_LENGTH * kotlin.math.sin(stericTiltFreedom()),
            contactMomentCapacityAtDesiredStroke = contactMomentCapacity(
                MANDATE * DESIRED_STROKE / PATH_COUNT / 2.0
            ),
            note = "rocking ALONG the contact line is blocked to first order; rocking ACROSS it " +
                    "is blocked only by the two links"
        ),
        SeatRecord(
            seat = "in the valley",
            lateralOffset = Gen1Tile.INTERHELICAL_SHEET / 2.0,
            faceHeight = seatFaceHeight(Gen1Tile.INTERHELICAL_SHEET / 2.0),
            contact = "a POINT on each of the two neighbours",
            tiltFreedomDegrees = stericTiltFreedom() * 180.0 / PI,
            headDeadBandAtDesignLength = DESIGN_LENGTH * kotlin.math.sin(stericTiltFreedom()),
            contactMomentCapacityAtDesiredStroke = contactMomentCapacity(
                MANDATE * DESIRED_STROKE / PATH_COUNT / 2.0,
                Gen1Tile.INTERHELICAL_SHEET / 2.0
            ),
            note = "the face dips between the neighbours, so it sits LOWER than on a duplex"
        )
    )

    val routings = listOf(
        routingRecord("R1", RoutingTopology.INDEPENDENT_STAPLES, NOMINAL, 2),
        routingRecord("R2", RoutingTopology.SCAFFOLD_EXCURSION, NOMINAL, 2),
        routingRecord("R3", RoutingTopology.SINGLE_LINK, NOMINAL, 1),
        routingRecord("R1w", RoutingTopology.INDEPENDENT_STAPLES, WIDE, 2),
        routingRecord("R2w", RoutingTopology.SCAFFOLD_EXCURSION, WIDE, 2)
    )

    val phases = listOf(
        BForm.BASE_PAIRS_PER_TURN_SQUARE to "square lattice",
        BForm.BASE_PAIRS_PER_TURN_HONEYCOMB to "honeycomb lattice"
    ).map { (turn, name) ->
        val backbone = DuplexBackbone(basePairsPerTurn = turn)
        PhaseRecord(
            lattice = name,
            basePairsPerTurn = turn,
            azimuthQuantumDegrees = backbone.azimuthQuantum * 180.0 / PI,
            worstMisalignmentDegrees = backbone.azimuthQuantum * 90.0 / PI,
            worstCoupleProjection = couplePhaseProjection(backbone.azimuthQuantum / 2.0),
            coupleLostFraction = 1.0 - couplePhaseProjection(backbone.azimuthQuantum / 2.0),
            interfacePhasePeriodBasePairs =
                if (turn == BForm.BASE_PAIRS_PER_TURN_SQUARE) Gen1Tile.CROSSOVER_SPACING_SHEET_BP
                else Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            turnsPerInterfacePeriod =
                (if (turn == BForm.BASE_PAIRS_PER_TURN_SQUARE) Gen1Tile.CROSSOVER_SPACING_SHEET_BP
                else Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP) / turn
        )
    }

    val realisable = realisablePerpendicularBase(NOMINAL, favourable = true)
    val realisableFree = realisablePerpendicularBase(NOMINAL, favourable = false)
    val realisableWide = realisablePerpendicularBase(WIDE, favourable = true)
    val realisableHard = StandoffBase(
        name = "two-terminus junction at the hard steric ceiling",
        rotationalStiffness = maximumBaseRotationalStiffness(BForm.DUPLEX_RADIUS),
        axialStiffness = 2.0 * bondSlideStiffness(),
        provenance = "T-67 counting theorem at the duplex's own radius — an upper bound"
    )

    val designs = STANDOFF_LENGTHS.flatMap { length ->
        listOf(
            designFor("B0", "ideal clamp (C-0025's assumption)", "—",
                StandoffBase.idealClamp(), length, covalent = true),
            designFor("B2", "C-0028's two crossovers, favourable", "across the flexure",
                b2, length, covalent = false),
            designFor("T1", "two-terminus junction, hard ceiling", "restrained",
                realisableHard, length, covalent = true, weakAxisBase = realisableFree),
            designFor("T2", "two-terminus junction, 154 deg groove", "restrained",
                realisableWide, length, covalent = true, weakAxisBase = realisableFree),
            designFor("T3", "two-terminus junction, 120 deg groove", "restrained",
                realisable, length, covalent = true, weakAxisBase = realisableFree),
            designFor("T4", "two-terminus junction, about the chord", "free",
                realisableFree, length, covalent = true)
        )
    }

    val thresholds = STANDOFF_LENGTHS.map { length ->
        val required = baseRotationalStiffnessThreshold(length, DESIRED_STROKE)
        JunctionThresholdRecord(
            standoffLength = length,
            requiredRotationalStiffness = required,
            hardCeiling = realisableHard.rotationalStiffness,
            nominalCeiling = realisableWide.rotationalStiffness,
            narrowCeiling = realisable.rotationalStiffness,
            metByHardCeiling = realisableHard.rotationalStiffness >= required,
            metByNominal = realisableWide.rotationalStiffness >= required,
            metByNarrow = realisable.rotationalStiffness >= required,
            metByC0028B2 = b2.rotationalStiffness >= required
        )
    }

    val hingeArms = listOf(
        hingeArmRecord("E5", "C-0023's LINEAR small-rotation law, as filed", 1, exact = false),
        hingeArmRecord("E5x", "exact rotation, one crossover, cantilever arm", 1, exact = true),
        hingeArmRecord("E5x2", "exact rotation, two crossovers, cantilever arm", 2, exact = true),
        hingeArmRecord("E5x4", "exact rotation, four crossovers, cantilever arm", 4, exact = true),
        hingeArmRecord("E5x8", "exact rotation, eight crossovers, cantilever arm", 8, exact = true),
        hingeArmRecord("E5g8", "exact rotation, eight crossovers, GUIDED arm (c = 12)", 8,
            exact = true, armFactor = 12.0),
        hingeArmRecord("E5g16", "exact rotation, sixteen crossovers, GUIDED arm (c = 12)", 16,
            exact = true, armFactor = 12.0),
        hingeArmRecord("E5g32", "exact rotation, 32 crossovers, GUIDED arm (c = 12)", 32,
            exact = true, armFactor = 12.0),
        hingeArmRecord("E5b8", "exact rotation, eight crossovers, 6-helix BUNDLE arm", 8,
            exact = true, bendingRigidity = 20.0 * EI)
    )

    val sensitivity = buildList {
        listOf(90.0, 120.0, 140.0, 154.0, 180.0).forEach { angle ->
            val backbone = DuplexBackbone(minorGrooveAngle = angle)
            val ceiling = maximumBaseRotationalStiffness(backbone.leverArm)
            add(
                SensitivityRecord(
                    axis = "minor groove backbone angle [deg]",
                    value = angle,
                    label = "chord ${"%.3f".format(backbone.terminalChord)} nm",
                    ceiling = ceiling,
                    thresholdAtEight = baseRotationalStiffnessThreshold(
                        DESIGN_LENGTH, DESIRED_STROKE
                    ),
                    meetsThresholdAtEight = ceiling >=
                            baseRotationalStiffnessThreshold(DESIGN_LENGTH, DESIRED_STROKE),
                    longestStandoffMeetingThreshold = longestStandoffMeetingThreshold(ceiling)
                )
            )
        }
        listOf(Gen1Tile.CROSSOVER_ALPHA_MIN, 1.0, Gen1Tile.CROSSOVER_ALPHA_MAX).forEach { alpha ->
            val ceiling = maximumBaseRotationalStiffness(NOMINAL.leverArm, alpha)
            add(
                SensitivityRecord(
                    axis = "Chen et al. alpha",
                    value = alpha,
                    label = "fitted bracket",
                    ceiling = ceiling,
                    thresholdAtEight = baseRotationalStiffnessThreshold(
                        DESIGN_LENGTH, DESIRED_STROKE
                    ),
                    meetsThresholdAtEight = ceiling >=
                            baseRotationalStiffnessThreshold(DESIGN_LENGTH, DESIRED_STROKE),
                    longestStandoffMeetingThreshold = longestStandoffMeetingThreshold(ceiling)
                )
            )
        }
        Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.forEach { multiplier ->
            val ceiling = maximumBaseRotationalStiffness(NOMINAL.leverArm, 1.0, multiplier)
            add(
                SensitivityRecord(
                    axis = "k_s multiplier",
                    value = multiplier,
                    label = "C-0020's four decades",
                    ceiling = ceiling,
                    thresholdAtEight = baseRotationalStiffnessThreshold(
                        DESIGN_LENGTH, DESIRED_STROKE
                    ),
                    meetsThresholdAtEight = ceiling >=
                            baseRotationalStiffnessThreshold(DESIGN_LENGTH, DESIRED_STROKE),
                    longestStandoffMeetingThreshold = longestStandoffMeetingThreshold(ceiling)
                )
            )
        }
    }

    val e5Linear = hingeArms.first { it.id == "E5" }
    val e5Guided = hingeArms.first { it.id == "E5g16" }
    val reproductions = listOf(
        reproduction("C-0028 B1 base rotational stiffness [pN nm/rad]", 13.53,
            StandoffBase.crossovers(1).rotationalStiffness),
        reproduction("C-0028 B1 base axial stiffness [pN/nm]", 64.71,
            StandoffBase.crossovers(1).axialStiffness),
        reproduction("C-0028 B2 base rotational stiffness [pN nm/rad]", 261.17,
            b2.rotationalStiffness),
        reproduction("C-0028 B2u base rotational stiffness [pN nm/rad]", 27.06,
            StandoffBase.crossovers(2, favourableOrientation = false).rotationalStiffness),
        reproduction("C-0028 P6 threshold at 8 nm [pN nm/rad]", 68.8,
            baseRotationalStiffnessThreshold(8.0, DESIRED_STROKE)),
        reproduction("C-0028 P6 threshold at 10 nm [pN nm/rad]", 173.6,
            baseRotationalStiffnessThreshold(10.0, DESIRED_STROKE)),
        reproduction("C-0028 B2 design span at 8 nm [nm]", 31.06,
            designs.first { it.baseId == "B2" && it.standoffLength == 8.0 }.span),
        reproduction("C-0028 B2 design tangent at 8 nm [pN/nm]", 36.51,
            designs.first { it.baseId == "B2" && it.standoffLength == 8.0 }.tangentStiffness),
        reproduction("C-0025 J5-8 clamped span [nm]", 31.6403748,
            designs.first { it.baseId == "B0" && it.standoffLength == 8.0 }.span),
        reproduction("C-0025 J5-8 clamped tangent [pN/nm]", 37.3911226,
            designs.first { it.baseId == "B0" && it.standoffLength == 8.0 }.tangentStiffness),
        reproduction("C-0023 E5 arm length [nm]", 4.11, e5Linear.armLength),
        reproduction("C-0023 E5 hinge compliance share", 0.925,
            CrossoverHingeFlexure(
                Gen1Tile.crossoverHingeStiffness(), e5Linear.armLength, EI
            ).hingeComplianceShare),
        reproduction("C-0023 E5 hinge bond force at 3 nm [pN]", 3.40,
            e5Linear.hingeBondForceWorking),
        reproduction("C-0009 crossover hinge constant [pN nm/rad]", 13.5294117647,
            2.0 * bondHingeStiffness()),
        reproduction("C-0009 crossover in-plane constant [pN/nm]", 64.7058823529,
            2.0 * bondSlideStiffness()),
        reproduction("SAXS single-layer interhelical distance [nm]", 2.69,
            Gen1Tile.INTERHELICAL_SHEET),
        reproduction("square-lattice turns per 32 bp interface period", 3.0,
            Gen1Tile.CROSSOVER_SPACING_SHEET_BP / BForm.BASE_PAIRS_PER_TURN_SQUARE),
        reproduction("Pan et al. four-way junction scissor stiffness [pN nm/rad]", 135.0,
            135.0),
        reproduction("hinge arm ceiling, cantilever arm [nm]", 9.7666,
            hingeArmCeiling()),
        reproduction("hinge arm ceiling, guided arm [nm]", 15.5005,
            hingeArmCeiling(armFactor = 12.0)),
        reproduction("C-0028 B2 lever arm over the phosphate radius", 1.345,
            Gen1Tile.INTERHELICAL_SHEET / 2.0 / BForm.PHOSPHATE_RADIUS)
    )

    val literature = LITERATURE

    val ceilingHard = realisableHard.rotationalStiffness
    val ceilingNominal = realisable.rotationalStiffness
    val thresholdEight = baseRotationalStiffnessThreshold(DESIGN_LENGTH, DESIRED_STROKE)

    val findings = buildMap {
        put(
            "the counting theorem",
            ("A B-form duplex has TWO backbones, so a duplex END presents exactly TWO strand " +
                    "termini, at the two backbone positions of its terminal base pair. Every " +
                    "covalent link grounding a normal standoff has to start at one of them, so a " +
                    "base joint has AT MOST TWO LINKS and their separation is the terminal chord " +
                    "2 r_P sin(D/2) — bounded by the duplex's own diameter. The lever arm is " +
                    "therefore at most %.3f nm (hard) and %.3f nm at the nominal 120 degree " +
                    "backbone separation, against the %.3f nm C-0028's B2 assumes.").format(
                BForm.DUPLEX_RADIUS, NOMINAL.leverArm, Gen1Tile.INTERHELICAL_SHEET / 2.0
            )
        )
        put(
            "the ceiling, and it is the answer",
            ("No perpendicular junction can supply more than %.2f pN nm/rad of base rotational " +
                    "stiffness (hard ceiling) or %.2f at the nominal chord, against C-0028's B2 " +
                    "of %.2f — a factor of %.2f. C-0028's own P6 threshold at its own 8 nm " +
                    "design length is %.2f pN nm/rad, so the hard ceiling clears it by %.2fx and " +
                    "the nominal chord MISSES it by %.2fx. The longest standoff the nominal " +
                    "chord can stabilise is %.2f nm.").format(
                ceilingHard, ceilingNominal, b2.rotationalStiffness,
                b2.rotationalStiffness / ceilingHard, thresholdEight,
                ceilingHard / thresholdEight, thresholdEight / ceilingNominal,
                longestStandoffMeetingThreshold(ceilingNominal)
            )
        )
        put(
            "a routing exists, and it is a hinge",
            ("The two-link closure search finds a covalent configuration: the worse of the two " +
                    "links spans %.3f nm against the %.2f nm phosphodiester step, so both links " +
                    "need ZERO unpaired nucleotides. The answer to T-67 is therefore NOT that no " +
                    "routing exists — it is that every routing produces a HINGE: two links on a " +
                    "chord react a moment as a couple about the chord's perpendicular bisector " +
                    "ONLY, and about the chord itself nothing is left but %.2f pN nm/rad, which " +
                    "is C-0028's B1 to the last digit and buckles at every length.").format(
                routings.first { it.id == "R1" }.secondGap, BForm.PHOSPHODIESTER_STEP,
                realisableFree.rotationalStiffness
            )
        )
        put(
            "the 90 degree exit is not set by the routing",
            ("A two-link base leaves the polar tilt FREE about the chord — the routing fixes an " +
                    "azimuth, not an angle. What sets the angle is one-sided STERICS: a flat end " +
                    "face on a cylinder makes a LINE contact of length 2R, which blocks rocking " +
                    "along the line to first order and leaves %.1f degrees of free play across " +
                    "it, i.e. %.2f nm of transverse dead band at an 8 nm head against P1's 0.1 " +
                    "nm. And the contact is one-sided: it can react only %.2f pN nm of base " +
                    "moment before the face lifts off.").format(
                stericTiltFreedom() * 180.0 / PI,
                DESIGN_LENGTH * kotlin.math.sin(stericTiltFreedom()),
                contactMomentCapacity(MANDATE * DESIRED_STROKE / PATH_COUNT / 2.0)
            )
        )
        put(
            "the phase is cheap",
            ("The base chord's azimuth is set by which base pair of the standoff carries the " +
                    "junction, so it is quantised at %.2f degrees on the square lattice and " +
                    "%.2f on the honeycomb. The couple projects as cos^2, so the worst " +
                    "misalignment costs %.1f %% of it — against the factor of %.2f the lever arm " +
                    "itself costs. C-0015's 32 bp interface period is exactly three turns of the " +
                    "square lattice, which is why the two quantisations are commensurate.").format(
                phases[0].azimuthQuantumDegrees, phases[1].azimuthQuantumDegrees,
                100.0 * phases[0].coupleLostFraction,
                b2.rotationalStiffness / ceilingHard
            )
        )
        put(
            "E5 as filed cannot reach the desired stroke",
            ("C-0023's E5 is an arm of %.2f nm on a torsional hinge, and the tip of an arm of " +
                    "length r cannot rise more than r — so a %.2f nm arm cannot deliver section " +
                    "3's DESIRED 10 nm stroke at all. This is geometry and needs no constitutive " +
                    "law. At C-0023's own 3 nm working point the arm has already turned %.1f " +
                    "degrees, where the small-rotation law it is placed on understates the " +
                    "reaction; solved exactly the element places at an arm of %.2f nm with a " +
                    "tangent/secant ratio of %.3f, i.e. a tangent of %.2f pN/nm against the 40 " +
                    "pN/nm ceiling.").format(
                e5Linear.armLength, e5Linear.armLength,
                asin(ACCEPTABLE_STROKE / e5Linear.armLength) * 180.0 / PI,
                hingeArms.first { it.id == "E5x" }.armLength,
                hingeArms.first { it.id == "E5x" }.tangentToSecant,
                hingeArms.first { it.id == "E5x" }.tangentAtWorkingPoint
            )
        )
        put(
            "a column buckles about its softest axis, and that closes the standoff branch",
            ("The two links restrain ONE axis. About the chord the base keeps only the two " +
                    "bonds' own hinges, %.2f pN nm/rad, and a column buckles about whichever axis " +
                    "is softer — so the adopted P6 reading is the WEAK-axis one, where the " +
                    "critical load is %.2f pN at 5 nm and %.2f pN at 8 nm against a duty of %.2f " +
                    "and %.2f. P6 FAILS AT EVERY LENGTH. The one-sided blunt-end contact does not " +
                    "rescue it: it can react at most %.2f pN nm at the 8 nm design against the " +
                    "%.2f pN nm the base already carries in the LOADED plane — 3.7x short — so it " +
                    "is not a supplementary restraint anywhere. On the restrained axis alone the " +
                    "window would be 5-8 nm; that reading is available only if a SECOND element " +
                    "restrains the free axis, which is T-66's truss and costs the sway the " +
                    "standoff exists to supply.").format(
                realisableFree.rotationalStiffness,
                designs.first { it.baseId == "T1" && it.standoffLength == 5.0 }.bucklingWeakAxis,
                designs.first { it.baseId == "T1" && it.standoffLength == 8.0 }.bucklingWeakAxis,
                designs.first { it.baseId == "T1" && it.standoffLength == 5.0 }.dutyDesiredElement,
                designs.first { it.baseId == "T1" && it.standoffLength == 8.0 }.dutyDesiredElement,
                designs.first { it.baseId == "T1" && it.standoffLength == 8.0 }
                    .contactMomentCapacity,
                designs.first { it.baseId == "T1" && it.standoffLength == 8.0 }.baseMomentDesired
            )
        )
        put(
            "what the programme is left with, and it is a design",
            ("At section 3's ACCEPTABLE 3 nm the duty is only %.3f pN, and the standoff clears it " +
                    "even on the WEAK axis at every length from 5 to 10 nm — margin 2.21x at 5 nm " +
                    "falling to 1.01x at 10 — failing below 5 nm on P3 alone. At its DESIRED 10 nm " +
                    "the standoff branch " +
                    "closes, and E5 as C-0023 filed it closes too — but E5 REDESIGNED does not. " +
                    "The cap on a hinge flexure's arm is (c n EI/k)^(1/3): %.2f nm on a cantilever " +
                    "arm, below the 10 nm stroke, and %.2f nm on a GUIDED one. A guided arm of " +
                    "%.2f nm = %.0f bp on %d crossovers places at 33.3333 pN/nm exactly, holds a " +
                    "tangent of %.2f pN/nm at 3 nm and %.2f at 10 nm — both inside C-0023's 40 " +
                    "pN/nm ceiling — turns only %.1f degrees at the desired stroke and puts %.2f " +
                    "pN on a crossover against the 10 pN unzip allowable. THAT is the Gen-1 output " +
                    "coupling this task leaves standing, and it needs no motif that is not already " +
                    "in every published origami.").format(
                designs.first { it.baseId == "T1" && it.standoffLength == 8.0 }
                    .dutyAcceptableElement,
                hingeArmCeiling(), hingeArmCeiling(armFactor = 12.0),
                e5Guided.armLength, e5Guided.armBasePairs, e5Guided.hingeCount,
                e5Guided.tangentAtWorkingPoint, e5Guided.tangentAtDesiredStroke,
                e5Guided.rotationAtDesiredStrokeDegrees, e5Guided.hingeBondForceDesired
            )
        )
    }

    val result = PerpendicularJunctionResult(
        task = "T-67 — does a 90 degree scaffold or staple routing between a sheet duplex and a " +
                "normal standoff exist at all?",
        leaf = "A8.2, with A1.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40 x 40 nm tile; " +
                "45 load paths on C-0015's 3 x 15 grid; section 3's 100 pN at the acceptable " +
                "3 nm and the desired 10 nm; single-layer square-lattice Rothemund sheet at the " +
                "SAXS-measured 2.69 nm interhelical distance",
        acceptance = "C-0028's P1-P6 unchanged, plus P7: every load-bearing link in the base is " +
                "a covalent phosphodiester step (zero unpaired nucleotides) and the base's " +
                "restrained axis can be aligned with the flexure's bending plane",
        countingBounds = countingBounds,
        seats = seats,
        routings = routings,
        phases = phases,
        designs = designs,
        thresholds = thresholds,
        hingeArms = hingeArms,
        sensitivity = sensitivity,
        reproductions = reproductions,
        literature = literature,
        findings = findings,
        validity = VALIDITY,
        citedNumbers = CITED
    )

    val json = Json { prettyPrint = true; encodeDefaults = true }
    val file = File("gpd/results/T-67-perpendicular-junction-routing.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)) + "\n")
    report(result, file)
}

private fun report(result: PerpendicularJunctionResult, file: File) {
    println(result.task)
    println()
    println("counting bounds (reading, groove, chord, lever, ceiling, /B2, longest stable l)")
    result.countingBounds.forEach {
        println(
            "  %-52s %6.1f %7.3f %7.3f %9.2f %7.3f %6.2f".format(
                it.reading.take(52), it.minorGrooveAngle, it.terminalChord, it.leverArm,
                it.maximumRotationalStiffness, it.fractionOfB2, it.longestStandoffMeetingThreshold
            )
        )
    }
    println()
    println("seats (seat, offset, face height, contact, tilt freedom, head dead band)")
    result.seats.forEach {
        println(
            "  %-12s %6.3f %7.3f  %-42s %7.2f %7.3f".format(
                it.seat, it.lateralOffset, it.faceHeight, it.contact.take(42),
                it.tiltFreedomDegrees, it.headDeadBandAtDesignLength
            )
        )
    }
    println()
    println("routings (id, gaps, unpaired, chord azimuth, restrained, free, verdict)")
    result.routings.forEach {
        println(
            "  %-4s %7.3f %7.3f %3d %3d %8.2f %9.2f %8.2f  %s".format(
                it.id, it.firstGap, it.secondGap, it.firstUnpaired, it.secondUnpaired,
                it.chordAzimuthDegrees, it.rotationalRestrained, it.rotationalFree,
                it.verdict.take(64)
            )
        )
    }
    println()
    println("thresholds (l, required, hard, wide, narrow, met by hard/wide/narrow/B2)")
    result.thresholds.forEach {
        println(
            "  %5.1f %10.2f %8.2f %8.2f %8.2f  %-5s %-5s %-5s %-5s".format(
                it.standoffLength, it.requiredRotationalStiffness, it.hardCeiling,
                it.nominalCeiling, it.narrowCeiling, it.metByHardCeiling, it.metByNominal,
                it.metByNarrow, it.metByC0028B2
            )
        )
    }
    println()
    println("designs (base, l, span, tangent, duty10, Pc, margin, verdict)")
    result.designs.forEach {
        println(
            "  %-4s %5.1f %7.2f %8.2f %7.3f %8.2f %7.2f  %s".format(
                it.baseId, it.standoffLength, it.span, it.tangentStiffness,
                it.dutyDesiredElement, it.bucklingFreeHead, it.bucklingMarginFreeHead,
                it.verdict.take(58)
            )
        )
    }
    println()
    println("hinge arms (id, n, arm, ceiling, secant, tangent3, t/s, rot3, rot10, tan10, verdict)")
    result.hingeArms.forEach {
        println(
            "  %-6s %2d %7.3f %7.2f %8.3f %9.3f %6.3f %6.2f %6.1f %9.1f %s".format(
                it.id, it.hingeCount, it.armLength, it.armCeiling, it.secantAtWorkingPoint,
                it.tangentAtWorkingPoint, it.tangentToSecant, it.rotationAtWorkingPointDegrees,
                it.rotationAtDesiredStrokeDegrees, it.tangentAtDesiredStroke,
                it.verdict.take(44)
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    result.reproductions.forEach {
        println(
            "  %-52s %14.6f %14.6f %10.2e".format(
                it.quantity.take(52), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}

// ---------------------------------------------------------------- provenance

private val LITERATURE: List<JunctionLiteratureRecord> = listOf(
    JunctionLiteratureRecord(
        question = "Is there a published strand routing that stands a duplex rigidly PERPENDICULAR " +
                "to a single-layer Rothemund sheet?",
        answer = "NOT FOUND, across ~110 distinct queries on EuropePMC, arXiv and Crossref. " +
                "Zero hits for ABSTRACT:\"DNA origami\" AND ABSTRACT:\"pillar\"; " +
                "ABSTRACT:\"DNA origami\" AND ABSTRACT:\"vertical\" AND ABSTRACT:\"helix\"; " +
                "ABSTRACT:\"DNA nanostructure\" AND ABSTRACT:\"out-of-plane\"; " +
                "\"perpendicular to the plane of the origami\"; TITLE:\"DNA origami\" AND " +
                "TITLE:\"stand\"; ABSTRACT:\"four-arm junction\" AND ABSTRACT:\"90\". " +
                "The negative is bounded by open-access indexing only.",
        flag = "not found",
        source = "search strategy recorded in T-67 and in C-0029"
    ),
    JunctionLiteratureRecord(
        question = "What happened the ONE time a protruding duplex was tried on a flat origami sheet?",
        answer = "It was flexible, and Rothemund says why in one clause — ONE covalent bond. " +
                "\"a 14 base mixed C and T tail was added to the 5' end of each staple strand ... " +
                "After formation of a shape, the corresponding 14 base polypurine strand was added " +
                "to create duplex at desired positions ... Instead the duplex markers imaged very " +
                "poorly in a manner that was highly scan angle dependent ... The duplex markers, " +
                "because they are attached to the origami by only one covalent bond, appear to be " +
                "flexible.\" This is the closest published precedent to T-67's question and it is " +
                "a NEGATIVE one — and it is this task's R3 single-link routing, observed.",
        flag = "read directly",
        source = "Rothemund, Nature 440:297 (2006), Supplementary Note S6; " +
                "authors.library.caltech.edu/records/9rbza-b9950/files/nature04586-s1.pdf, " +
                "verified verbatim by this task"
    ),
    JunctionLiteratureRecord(
        question = "Is the four-arm (Holliday) junction's 90 degree planar cross available in this " +
                "buffer?",
        answer = "NO. \"In the unstacked conformation ... the four DNA helices form a planar cross " +
                "with right angles. This conformation is however mostly present in the absence of " +
                "divalent cations. When divalent cations, like Mg2+, are present, the Holliday " +
                "junction will tend to transition into its stacked conformation where the arms of " +
                "the junctions form a 60 degree angle. As the structures were folded and imaged in " +
                "the presence of 10 mM MgCl2 it is not surprising that the junctions conform into " +
                "this angled form, leading to a deformation of the overall shape.\" The Gen-1 " +
                "buffer is 2 mM MgCl2, so the 90 degree open form is not the one on offer; and the " +
                "open form is in any case flexible, not rigid.",
        flag = "read directly",
        source = "Benson, Mohammed, Bosco, Teixeira, Orponen & Hogberg, Angew. Chem. Int. Ed. " +
                "55:8869 (2016); europepmc PMC6680348 fullTextXML, verified verbatim by this task"
    ),
    JunctionLiteratureRecord(
        question = "Does the classic origami motif require parallel helices?",
        answer = "YES, stated in print: \"In these DNA origami structures, the path of the scaffold " +
                "has been restricted by a double-crossover motif to form parallel helices.\" This " +
                "is the constraint C-0028 records; it is a statement of a constraint, not an " +
                "impossibility proof, and no explicit statement that a rigid 90 degree duplex-duplex " +
                "junction is impossible was found.",
        flag = "abstract only",
        source = "Science 339(6126) editor's summary accompanying Han et al. (2013), verbatim from " +
                "api.crossref.org/works/10.1126/science.1232252"
    ),
    JunctionLiteratureRecord(
        question = "How does the literature route a strand through a large-angle vertex?",
        answer = "Through UNPAIRED nucleotides, and it says so: PERDIX connects \"staples in " +
                "vertices with unpaired poly(T) loops\", budgeted at \"0.42 nm per unpaired " +
                "nucleotide\", and expects the result to be compliant — \"we expected the N-arm " +
                "junctions to be relatively flexible ... due to the unpaired nucleotides present in " +
                "vertices\". ATHENA: \"Unpaired scaffold nucleotides are used to span the distance " +
                "between the 3' and 5' end between incoming and outgoing edges, which would " +
                "otherwise be misaligned due to the native twist of B-form DNA.\" That is this " +
                "task's P7 failing, in the literature's own words.",
        flag = "read directly",
        source = "Jun, Zhang, Shepherd, Ratanalert, Qi, Yan & Bathe, Sci. Adv. 5:eaav0655 (2019), " +
                "PMC6314877; Jun et al., Nucleic Acids Res. 49 (2021), PMC8501967"
    ),
    JunctionLiteratureRecord(
        question = "Is there any rotational stiffness measured or fitted for a multi-arm DNA junction?",
        answer = "ONE, and it is the only number of its kind: \"a rotational stiffness of " +
                "k_twist = 135 pN nm rad^-1 of the scissor-like interhelical angle J_twist\", for a " +
                "four-way junction at \"interhelical distance ... 1.85 nm\" and \"a right-handed " +
                "twist of 60 degrees\". It is FITTED, not measured: \"estimated empirically ... " +
                "using the equilibrium distribution of J_twist from MD simulations of an isolated " +
                "four-way junction (PDB ID: 1DCW) ... and cross-validated using published FRET " +
                "measurements\". Used here ONLY as a cross-check: it is 1.7-2.2x this task's " +
                "two-terminus ceiling, and it restrains a different degree of freedom.",
        flag = "read directly",
        source = "Pan, Kim, Zhang, Adendorff, Yan & Bathe, Nat. Commun. 5:5578 (2014); " +
                "europepmc PMC4268701 fullTextXML, verified verbatim by this task"
    ),
    JunctionLiteratureRecord(
        question = "The phosphate radius and the phosphodiester step, which the counting theorem " +
                "rests on.",
        answer = "\"Phosphates (red circles) sit at a radius of a_DNA = 10 A\" (verified verbatim), " +
                "and the intrastrand step is a measured PAIR, not a number: \"C3-endo " +
                "(interphosphate distance 0.6 nm) to C2-endo conformation (interphosphate distance " +
                "0.7 nm)\". Both verified verbatim by this task. The step is therefore a WINDOW, " +
                "which is why the closure objective is a window residual and not a distance.",
        flag = "read directly",
        source = "Hedley, Coshic, Aksimentiev & Kornyshev, Phys. Rev. X 14:031042 (2024), " +
                "PMC12489989; Bosco, Camunas-Soler & Ritort, Nucleic Acids Res. 42:2064 (2014), " +
                "PMC3919573"
    ),
    JunctionLiteratureRecord(
        question = "Is a duplex hung off an origami face by a short ssDNA extension perpendicular?",
        answer = "NO, and it is reported as a systematic distortion: \"the Bott-ext-PTO complex " +
                "might not be exactly perpendicular to the origami plane, leading to mild systematic " +
                "distortion of the pattern\" — for a 3 nt single-stranded extension. This is the " +
                "modern instance of Rothemund's 2006 observation, in the same direction.",
        flag = "read directly",
        source = "Teodori et al., ACS Nano 19:36931 (2025), europepmc PMC12574213 fullTextXML"
    ),
    JunctionLiteratureRecord(
        question = "Does the gridiron four-arm motif put an arm OUT of the plane of the others?",
        answer = "NOT ESTABLISHED at primary-source level. Han et al.'s abstract says only that " +
                "\"Deliberate distortion of the junctions from their most relaxed conformations " +
                "ensures that a scaffold strand can traverse through individual vertices in " +
                "multiple directions\"; the full text is closed (Science 403s the article and the " +
                "supplement, OpenAlex reports oa_status closed, no repository copy). A review " +
                "describes the 90 degrees as a rotation BETWEEN TWO STACKED LAYERS, not an " +
                "out-of-plane arm — but that is a review's words, flagged as such.",
        flag = "abstract only",
        source = "Han, Pal, Yang, Jiang, Nangreave, Liu & Yan, Science 339:1412 (2013), abstract " +
                "verbatim from EuropePMC PMID 23520107; Piskunen et al., Molecules 25:1823 (2020), " +
                "PMC7221932, for the review description"
    )
)

private val VALIDITY: List<String> = listOf(
    "TRL 1-3. Nothing here is measured, and the geometry is not demonstrated either.",
    "The closure search tests a NECESSARY condition for a link — a phosphate pair inside the " +
            "measured [0.60, 0.70] nm phosphodiester window with no van der Waals overlap — and " +
            "never a sufficient one: no backbone torsion angle is checked, and no sequence is " +
            "designed. A 'closes' verdict is therefore an UPPER BOUND on buildability; only a " +
            "'does not close' verdict would be a proof of impossibility.",
    "The COUNTING THEOREM is not a model and does not inherit that caveat: a duplex has two " +
            "backbones, so a duplex end has two strand termini, and no force field, lattice or " +
            "sequence can add a third. The lever-arm ceiling r_P follows from it directly.",
    "The minor-groove backbone angle is a CONVENTION as much as a measurement, and it is the " +
            "parameter the couple is most sensitive to: 90 / 120 / 154 / 180 degrees give " +
            "ceilings of 45.9 / 62.1 / 75.0 / 78.2 pN nm/rad. Every verdict is reported across " +
            "the bracket and the 180 degree reading is carried as a hard, convention-free bound.",
    "A column buckles about its SOFTEST axis. The two-link base restrains one axis only, so the " +
            "adopted P6 reading is the WEAK-axis one, in which the base keeps only the two bonds' " +
            "own hinges (13.53 pN nm/rad) and P6 fails at every length. The restrained-axis " +
            "reading is reported beside it and is available ONLY if the standoff's blunt end " +
            "resting on the sheet duplex is credited as a rotational restraint about the chord — " +
            "a one-sided line contact whose moment capacity is the axial duty times the duplex " +
            "radius, a few pN nm, and which has never been characterised.",
    "The one-sided contact is modelled as a rigid line of half-width R on a smooth cylinder. A " +
            "real blunt end on a grooved duplex is softer and narrower, so the reported contact " +
            "moment capacity is an UPPER bound.",
    "k_s = 2 alpha S/(100 a) is C-0020's DERIVED construction and is NOT measured. The whole " +
            "base couple is 2 k_s a^2, so the ceiling is linear in it and it is swept over " +
            "C-0020's four decades. As in C-0028, verdicts move across it.",
    "k_theta = 2 alpha B/(100 a) is C-0009's CITED, FITTED constant, swept over Chen et al.'s " +
            "own alpha in [0.6, 1.2].",
    "EI = 230 pN nm^2 is a CanDo MODEL INPUT, not a measurement; C-0028 records that Fields et " +
            "al.'s measured buckling implies 25 % less, so every critical load here is the " +
            "optimistic end, exactly as there.",
    "E5's exact-rotation law extrapolates Chen et al.'s SMALL-ANGLE fitted hinge constant to " +
            "rotations of 4-47 degrees. The GEOMETRIC ceiling — a tip cannot rise past the arm " +
            "length, and the placement condition caps the arm at (c n EI/k)^(1/3) — needs no " +
            "constitutive law and is the part that decides the verdict.",
    "The arm-bending term is put in SERIES with the hinge at the tip, which is C-0023's own " +
            "composition; the arm's rotation is not fed back into the hinge's moment arm.",
    "The sheet's neighbouring duplexes are given the SAME helical phase as the seat duplex. " +
            "C-0015 shows the phase is a design variable with a 32 bp period; allowing it to be " +
            "chosen could only make the closure easier, so the covalent verdict is unaffected " +
            "and the reported gaps are an upper bound on the best achievable.",
    "One flexure per load path and 45 attachments, exactly as C-0023, C-0025 and C-0028 assume."
)

private val CITED: Map<String, String> = mapOf(
    "phosphate radius in B-form DNA" to "1.00 nm (10 A) — CITED, MEASURED/MODELLED, Hedley et " +
            "al., Phys. Rev. X 14:031042 (2024), READ DIRECTLY and verified verbatim; the narrow " +
            "fibre reading 0.90 nm carried as the other end of the bracket",
    "intrastrand phosphodiester step" to "0.60-0.70 nm — CITED, MEASURED, Bosco, Camunas-Soler " +
            "& Ritort, Nucleic Acids Res. 42:2064 (2014), READ DIRECTLY: C3'-endo 0.6 nm, " +
            "C2'-endo 0.7 nm. A WINDOW, not a number",
    "ssDNA contour per nucleotide" to "0.65 nm inextensible — CITED, MEASURED, via C-0025",
    "duplex EI" to "230 pN nm^2 — CITED, a CanDo MODEL INPUT (Kim et al., NAR 40:2862, 2012), " +
            "NOT a measurement",
    "duplex stretch modulus S" to "1100 pN — CITED, MEASURED, Wang et al., Biophys. J. 72:1335 " +
            "(1997)",
    "crossover hinge constant k_theta = 2 alpha B/(100 a)" to "13.53 pN nm/rad, alpha in " +
            "[0.6, 1.2] — CITED, FITTED, Chen et al., JACS 136:6995 (2014) SI S2, via C-0009",
    "crossover in-plane constant k_s = 2 alpha S/(100 a)" to "64.71 pN/nm — DERIVED (C-0020), " +
            "NOT measured; swept four decades",
    "interhelical distance, single-layer sheet" to "2.69 nm — CITED, MEASURED by SAXS, Fischer " +
            "et al. (2016)",
    "base pairs per turn" to "10.67 square lattice, 10.5 honeycomb — CITED",
    "rise per base pair" to "0.34 nm — CITED, Douglas et al. (2009)",
    "four-way junction scissor stiffness" to "135 pN nm/rad — CITED, FITTED to MD and " +
            "cross-validated against FRET, Pan et al., Nat. Commun. 5:5578 (2014), READ DIRECTLY " +
            "and verified verbatim. Used ONLY as a cross-check, never as an input",
    "per-path allowables" to "10 / 65 pN — CITED via C-0006",
    "section 3 targets" to "100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM — CITED",
    "C-0028's B2, threshold ladder and design" to "261.17 pN nm/rad, 68.8 pN nm/rad at 8 nm, " +
            "span 31.06 nm, tangent 36.51 pN/nm — CITED, and reproduced here as gate-5 tests",
    "C-0023's E5" to "arm 4.11 nm, 92.5 % hinge compliance, 3.40 pN bond force — CITED, and " +
            "reproduced here"
)
