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
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs

/**
 * Task `T-106` / leaf `A8.2` — the truss **cap** as a solved body rather than a series spring,
 * which is `C-0037`'s open item 2 and `C-0042`'s.
 *
 * ```shell
 * tools/study.sh anchoring.TrussCapStudyKt
 * ```
 *
 * Emits `gpd/results/T-106-truss-cap.json`, deterministically: the file carries no timestamp and
 * the whole tree is rounded at the **serialisation boundary**.
 */

private const val INFINITE_SENTINEL = -1.0

private fun finite(value: Double): Double =
    if (value.isInfinite() || value.isNaN()) INFINITE_SENTINEL else value

private const val TARGET_FORCE = 100.0
private const val ACCEPTABLE_STROKE = 3.0
private const val DESIRED_STROKE = 10.0
private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE
private const val PATH_COUNT = 45
private const val COMPLIANT_CEILING = 40.0
private const val SUPPORT_MARGIN_REQUIRED = 10.0
private val PER_PATH = MANDATE / PATH_COUNT

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR

/** §3's envelope, read on the **flexure's height above the sheet** rather than on the leg. */
private val HEIGHTS = listOf(5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

private val SEPARATIONS = (6..16).toList()

/** `C-0029`'s realisable two-link base on the hard, convention-free 180° chord. */
private val HARD_BASE = TwoLinkBase.realisable()

private val LINK = 2.0 * bondSlideStiffness()

/** The design point `C-0037` and `C-0042` both report at. */
private const val DESIGN_HEIGHT = 8.0

// ---------------------------------------------------------------------------------------------

@Serializable
data class T106BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val note: String
)

@Serializable
data class T106GeometryRecord(
    val separationBasePairs: Int,
    val separation: Double,
    val minimumCapLength: Double,
    val minimumCapBasePairs: Int,
    val perpendicularSeatContact: Double,
    val parallelSeatContact: Double,
    val separateBodyRequired: Boolean,
    val rigidHeight: Double,
    val junctionCount: Int,
    val covalentLinkCount: Int,
    val interhelicalCrossoverPossible: Boolean,
    val note: String
)

@Serializable
data class T106CoupleRecord(
    val separationBasePairs: Int,
    val separation: Double,
    val secondMoment: Double,
    val legAxialStiffness: Double,
    val assertedFrameCouple: Double,
    val capBendingPinned: Double,
    val capBendingClamped: Double,
    val solvedFrameCouple: Double,
    val solvedFrameCoupleClamped: Double,
    val ratioToAsserted: Double,
    val capTorsion: Double
)

@Serializable
data class T106DesignRecord(
    val id: String,
    val capModel: String,
    val separationBasePairs: Int,
    val flexureHeight: Double,
    val legLength: Double,
    val junctionAzimuthDegrees: Double,
    val flexureJunction: String,
    val headJunctionLoaded: Double,
    val headJunctionFree: Double,
    val frameCouple: Double,
    val span: Double,
    val spanBasePairs: Double,
    val bendingFactor: Double,
    val couplingFactor: Double,
    val suppliedDrawInAcceptable: Double,
    val demandedDrawInAcceptable: Double,
    val supplyToDemandAcceptable: Double,
    val secantAcceptable: Double,
    val tangentAcceptable: Double,
    val tangentToSecant: Double,
    val minimumTangent: Double,
    val axialForceDesired: Double,
    val dutyDesiredElement: Double,
    val loadedCriticalLoad: Double,
    val freeCriticalLoad: Double,
    val criticalLoad: Double,
    val criticalLoadFields: Double,
    val governingPlane: String,
    val bucklingMargin: Double,
    val bucklingMarginFields: Double,
    val peakLegCompression: Double,
    val perLegCriticalLoad: Double,
    val perLegMargin: Double,
    val bucklingStroke: Double,
    val transverseStiffness: Double,
    val strokeClearance: Double,
    val p1Supports: Boolean,
    val p2Placed: Boolean,
    val p3Compliant: Boolean,
    val p4Safe: Boolean,
    val p5Envelope: Boolean,
    val p6Stable: Boolean,
    val p7FlexureStable: Boolean,
    val p8Supplies: Boolean,
    val p9LegShare: Boolean,
    val verdict: String
)

@Serializable
data class T106SensitivityRecord(
    val axis: String,
    val label: String,
    val frameCouple: Double,
    val criticalLoad: Double,
    val governingPlane: String,
    val bucklingMargin: Double,
    val bucklingMarginFields: Double,
    val tangentAcceptable: Double,
    val supplyToDemandAcceptable: Double,
    val verdict: String,
    val verdictMoves: Boolean
)

@Serializable
data class T106ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T106ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
data class T106LiteratureRecord(
    val question: String,
    val answer: String,
    val flag: String,
    val source: String
)

@Serializable
data class T106Result(
    val task: String,
    val leaf: String,
    val temperatureKelvin: Double,
    val kbT: Double,
    val units: String,
    val bounds: List<T106BoundRecord>,
    val geometry: List<T106GeometryRecord>,
    val couples: List<T106CoupleRecord>,
    val designs: List<T106DesignRecord>,
    val sensitivities: List<T106SensitivityRecord>,
    val convergence: List<T106ConvergenceRecord>,
    val reproductions: List<T106ReproductionRecord>,
    val literature: List<T106LiteratureRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------

private fun capFor(
    separationBasePairs: Int,
    flexureHeight: Double,
    asserted: Boolean = false,
    junctionMisalignment: Double = 0.0,
    flexureJunctionRotational: Double = maximumBaseRotationalStiffness(BForm.PHOSPHATE_RADIUS),
    base: TwoLinkBase = HARD_BASE,
    capEndFactor: Double = 12.0,
    torsionalRigidity: Double = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
    bendingRigidity: Double = EI,
    linkStiffness: Double = LINK,
    elementsPerLeg: Int = 32
): SolvedTrussCap {
    val height = if (asserted) 0.0 else BForm.DUPLEX_RADIUS
    return SolvedTrussCap(
        separationBasePairs = separationBasePairs,
        legLength = flexureHeight - height,
        base = base,
        capJunctionMisalignment = junctionMisalignment,
        flexureJunctionRotational = flexureJunctionRotational,
        linkStiffness = linkStiffness,
        capEndFactor = capEndFactor,
        torsionalRigidity = torsionalRigidity,
        bendingRigidity = bendingRigidity,
        elementsPerLeg = elementsPerLeg,
        asserted = asserted
    )
}

private fun design(
    id: String,
    cap: SolvedTrussCap,
    flexureHeight: Double,
    flexureJunctionLabel: String,
    orientation: FlexureOrientation = FlexureOrientation.FAVOURABLE,
    drawInModel: DrawInModel = DrawInModel.CHORD,
    fieldsRigidity: Double = FIELDS_BENDING_RIGIDITY
): T106DesignRecord {
    val bendingRigidity = cap.bendingRigidity
    val span = coupledFlexureSpan(
        bendingRigidity, cap.flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, orientation,
        cap.stretchModulus, drawInModel
    )
    val flexure = CoupledJointFlexure(
        bendingRigidity, span, cap.flexibility, cap.stretchModulus, drawInModel
    )
    val secant = PATH_COUNT * flexure.strokeSecantStiffness(ACCEPTABLE_STROKE, orientation)
    val tangent = PATH_COUNT * flexure.strokeTangentStiffness(ACCEPTABLE_STROKE, orientation)
    var minimumTangent = Double.MAX_VALUE
    for (i in 0..1000) {
        val value = PATH_COUNT *
                flexure.strokeTangentStiffness(i * DESIRED_STROKE / 1000.0, orientation)
        if (value < minimumTangent) minimumTangent = value
    }
    val duty = flexure.strokeEndShear(DESIRED_STROKE, orientation)
    val peakLeg = duty / cap.legCount
    val perLegCritical = cap.criticalLoad / cap.legCount
    val fields = cap.criticalLoad * fieldsRigidity / bendingRigidity
    val tension = flexure.strokeAxialForce(DESIRED_STROKE, orientation)
    val share = MANDATE * DESIRED_STROKE / PATH_COUNT
    val supplied = flexure.couplingFactor * ACCEPTABLE_STROKE
    val demanded = flexure.chordExtension(ACCEPTABLE_STROKE)
    val braced = bracedColumnBucklingLoad(bendingRigidity, span, flexure.restraint)
    val peakFlexure = peakFlexureCompression(flexure, orientation, DESIRED_STROKE)
    val clearance = if (orientation == FlexureOrientation.FAVOURABLE)
        favourableStrokeClearance(flexureHeight) else Double.POSITIVE_INFINITY

    val p1 = cap.transverseStiffness >= SUPPORT_MARGIN_REQUIRED * PER_PATH &&
            cap.layout.stericallyRealisable
    val p2 = abs(secant - MANDATE) <= 1.0e-6 * MANDATE
    val p3 = tangent <= COMPLIANT_CEILING
    val p4 = tension <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE &&
            share <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    val p5 = flexureHeight <= 10.0 && span <= 60.0
    val p6 = cap.criticalLoad >= duty
    val p7 = peakFlexure <= braced
    val p8 = supplied >= demanded
    val p9 = peakLeg <= perLegCritical
    val verdict = when {
        !p1 -> "FAIL P1 — the legs do not support the flexure, or they overlap"
        !p3 -> "FAIL P3 — tangent past the 40 pN/nm compliance ceiling"
        !p4 -> "FAIL P4 — beam tension past the 10 pN unzip allowable at 10 nm"
        !p5 -> "FAIL P5 — outside C-0017's buildable envelope"
        !p6 -> "FAIL P6 — the truss buckles before the desired stroke"
        !p7 -> "FAIL P7 — the FLEXURE buckles under the compression the joint imposes"
        !p8 -> "FAIL P8 — the joint no longer supplies the draw-in it is charged for"
        !p9 -> "FAIL P9 — the head moment overloads one leg"
        else -> "PASS"
    }
    return T106DesignRecord(
        id = id,
        capModel = if (cap.asserted) "C-0037 series spring" else "solved crossbar",
        separationBasePairs = cap.separationBasePairs,
        flexureHeight = flexureHeight,
        legLength = cap.legLength,
        junctionAzimuthDegrees = cap.capJunctionMisalignment * 180.0 / PI,
        flexureJunction = flexureJunctionLabel,
        headJunctionLoaded = cap.headJunctionLoaded,
        headJunctionFree = cap.headJunctionFree,
        frameCouple = cap.frameCouple,
        span = span,
        spanBasePairs = span / RISE,
        bendingFactor = flexure.bendingFactor,
        couplingFactor = flexure.couplingFactor,
        suppliedDrawInAcceptable = supplied,
        demandedDrawInAcceptable = demanded,
        supplyToDemandAcceptable = supplied / demanded,
        secantAcceptable = secant,
        tangentAcceptable = tangent,
        tangentToSecant = tangent / secant,
        minimumTangent = minimumTangent,
        axialForceDesired = tension,
        dutyDesiredElement = duty,
        loadedCriticalLoad = cap.loadedCriticalLoad,
        freeCriticalLoad = cap.freeCriticalLoad,
        criticalLoad = cap.criticalLoad,
        criticalLoadFields = fields,
        governingPlane = cap.governingPlane,
        bucklingMargin = cap.criticalLoad / duty,
        bucklingMarginFields = fields / duty,
        peakLegCompression = peakLeg,
        perLegCriticalLoad = perLegCritical,
        perLegMargin = perLegCritical / peakLeg,
        bucklingStroke = finite(coupledBucklingStroke(flexure, orientation, cap.criticalLoad)),
        transverseStiffness = cap.transverseStiffness,
        strokeClearance = finite(clearance),
        p1Supports = p1,
        p2Placed = p2,
        p3Compliant = p3,
        p4Safe = p4,
        p5Envelope = p5,
        p6Stable = p6,
        p7FlexureStable = p7,
        p8Supplies = p8,
        p9LegShare = p9,
        verdict = verdict
    )
}

private fun sensitivity(
    axis: String,
    label: String,
    record: T106DesignRecord,
    reference: T106DesignRecord
) = T106SensitivityRecord(
    axis = axis,
    label = label,
    frameCouple = record.frameCouple,
    criticalLoad = record.criticalLoad,
    governingPlane = record.governingPlane,
    bucklingMargin = record.bucklingMargin,
    bucklingMarginFields = record.bucklingMarginFields,
    tangentAcceptable = record.tangentAcceptable,
    supplyToDemandAcceptable = record.supplyToDemandAcceptable,
    verdict = record.verdict,
    verdictMoves = record.verdict != reference.verdict
)

// ---------------------------------------------------------------------------------------------

private fun bounds(): List<T106BoundRecord> {
    val w7 = 7 * RISE
    val legAxial = legAxialStiffness(7.0, HARD_BASE, STRETCH)
    val moment = TrussLayout.row(2, w7, PI / 2.0).acrossSecondMoment
    val couple = legAxial * moment
    val capBend = capBendingStiffness(EI, w7)
    val headAxes = chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0)
    val assembled = trussTipFlexibility(EI, 7.0, HARD_BASE.restrainedAxis, 2, 0.0)
    return listOf(
        T106BoundRecord(
            "bound 1 — a leg's line contact with a duplex laid ACROSS the row, at the 7 bp pitch",
            capSeatContactAcrossRow(w7), "nm",
            "ZERO, and it is zero at every separation above C-0042's 2.00 nm steric floor, " +
                    "because the floor already puts each leg a full radius off such a seat's " +
                    "axis. No duplex across the row can seat both legs, so the flexure cannot " +
                    "be the cap and the cap is a body of its own"
        ),
        T106BoundRecord(
            "bound 1b — the same contact for a crossbar laid ALONG the row",
            TrussCapGeometry(w7).parallelSeatContact, "nm",
            "the full duplex diameter, for every separation: a crossbar parallel to the row " +
                    "seats both legs on their whole end faces"
        ),
        T106BoundRecord(
            "bound 2 — covalent links a cap that IS the flexure's end could offer, per leg",
            1.0, "links",
            "a duplex end has exactly two strand termini and there are two legs, so such a cap " +
                    "is C-0037's H1 ball joint per leg and not its nominal H2. The counting " +
                    "theorem excludes the same geometry bound 1 excludes, by a different route"
        ),
        T106BoundRecord(
            "bound 3 — the crossbar's own bending against the couple it carries, 7 bp",
            capBend / couple, "ratio",
            "k_cap,bend = 12EI/w = %.1f against k_a Σd² = %.1f pN·nm/rad. Above the ~5 the " +
                    "task's falsifier 2 names, so the cap's BENDING is a correction and not the " +
                    "answer".format(capBend, couple)
        ),
        T106BoundRecord(
            "bound 4 — the crossbar's torsion against the head's own rotational stiffness, 7 bp",
            capTorsionalStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, w7) *
                    assembled.rotationUnderMoment, "ratio",
            "4C/w = %.1f pN·nm/rad against the assembled head's %.1f. The loaded plane's cap " +
                    "term is a few per cent".format(
                        capTorsionalStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, w7),
                        1.0 / assembled.rotationUnderMoment
                    )
        ),
        T106BoundRecord(
            "bound 5 — the head junction's ROTATION against the head's own, 7 bp",
            headAxes.loaded * assembled.rotationUnderMoment, "ratio",
            "C-0029's two links carry the head's rotation as well as its axial force, at most " +
                    "%.2f pN·nm/rad on one axis and %.2f on the other, against the assembled " +
                    "head's %.2f. THIS is the term C-0037 does not carry, and it is the one " +
                    "below the falsifier's 10x".format(
                        headAxes.loaded, headAxes.free, 1.0 / assembled.rotationUnderMoment
                    )
        ),
        T106BoundRecord(
            "bound 5b — the conserved chord budget of a cap junction",
            headAxes.total, "pN·nm/rad",
            "loaded + free is invariant under the chord azimuth — C-0042's rank-one identity " +
                    "on the base, now on the CAP, so the cap junction's azimuth is a second " +
                    "trade of exactly C-0037's kind"
        ),
        T106BoundRecord(
            "bound 6 — the cap's rigid height between the leg heads and the flexure's axis",
            BForm.DUPLEX_RADIUS, "nm",
            "the crossbar's own radius. It raises C12 by e·C22 — the entry C-0030 shows " +
                    "supplies the draw-in — and costs the same e of geometric softening in the " +
                    "buckling problem. A series spring has no height"
        )
    )
}

private fun geometry(): List<T106GeometryRecord> = SEPARATIONS.map { bp ->
    val g = TrussCapGeometry(bp * RISE)
    T106GeometryRecord(
        separationBasePairs = bp,
        separation = bp * RISE,
        minimumCapLength = g.minimumLength,
        minimumCapBasePairs = g.minimumBasePairs,
        perpendicularSeatContact = g.perpendicularSeatContact,
        parallelSeatContact = g.parallelSeatContact,
        separateBodyRequired = g.separateBodyRequired,
        rigidHeight = g.rigidHeight,
        junctionCount = g.junctionCount,
        covalentLinkCount = g.covalentLinkCount,
        interhelicalCrossoverPossible =
            abs(bp * RISE - Gen1Tile.INTERHELICAL_SHEET) <= 0.5 * RISE,
        note = if (g.separateBodyRequired)
            "the cap is a separate crossbar, ${g.minimumBasePairs} bp, hosting three junctions"
        else "a duplex across the row would seat both legs — the cap need not be a body"
    )
}

private fun couples(): List<T106CoupleRecord> = SEPARATIONS.map { bp ->
    val w = bp * RISE
    val cap = capFor(bp, DESIGN_HEIGHT)
    val clampedCap = capFor(bp, DESIGN_HEIGHT, capEndFactor = 16.0)
    T106CoupleRecord(
        separationBasePairs = bp,
        separation = w,
        secondMoment = cap.layout.acrossSecondMoment,
        legAxialStiffness = cap.legAxial,
        assertedFrameCouple = cap.assertedFrameCouple,
        capBendingPinned = capBendingStiffness(EI, w, 12.0),
        capBendingClamped = capBendingStiffness(EI, w, 16.0),
        solvedFrameCouple = cap.frameCouple,
        solvedFrameCoupleClamped = clampedCap.frameCouple,
        ratioToAsserted = cap.frameCouple / cap.assertedFrameCouple,
        capTorsion = cap.capTorsion
    )
}

private fun main0(): T106Result {
    val designs = mutableListOf<T106DesignRecord>()

    // 1. C-0037's own reading, reproduced through this file's pipeline
    SEPARATIONS.forEach { bp ->
        designs += design(
            "A$bp", capFor(bp, DESIGN_HEIGHT, asserted = true), DESIGN_HEIGHT, "rigid (C-0037)"
        )
    }
    // 2. the solved cap, both junction azimuths, over the separations
    SEPARATIONS.forEach { bp ->
        designs += design("Sx$bp", capFor(bp, DESIGN_HEIGHT), DESIGN_HEIGHT, "two-link")
        designs += design(
            "Sy$bp", capFor(bp, DESIGN_HEIGHT, junctionMisalignment = PI / 2.0),
            DESIGN_HEIGHT, "two-link"
        )
    }
    // 3. the envelope, at the (separation, cap-junction azimuth) the solved cap recommends —
    //    both are design variables and neither was one upstream, so the choice is made on the
    //    predicate that binds, P6, read on the MEASURED rigidity
    val candidates = designs.filter { it.id.startsWith("Sx") || it.id.startsWith("Sy") }
        .filter { it.verdict == "PASS" }
    val bestMargin = candidates.maxOf { it.bucklingMarginFields }
    val best = candidates.filter { it.bucklingMarginFields >= bestMargin - 1.0e-9 }
        .minWith(compareBy({ it.separationBasePairs }, { it.junctionAzimuthDegrees }))
    val recommended = best.separationBasePairs
    val recommendedAzimuth = if (best.junctionAzimuthDegrees > 45.0) PI / 2.0 else 0.0
    val recommendedPrefix = if (recommendedAzimuth > 0.0) "Sy" else "Sx"
    HEIGHTS.forEach { h ->
        designs += design(
            "H%.0f".format(h), capFor(recommended, h, junctionMisalignment = recommendedAzimuth),
            h, "two-link"
        )
    }
    // 4. C-0037's own reading over the same envelope, so the two windows are comparable
    HEIGHTS.forEach { h ->
        designs += design(
            "AH%.0f".format(h), capFor(recommended, h, asserted = true), h, "rigid (C-0037)"
        )
    }

    val reference = designs.first { it.id == "$recommendedPrefix$recommended" }
    val sensitivities = mutableListOf<T106SensitivityRecord>()

    sensitivities += sensitivity(
        "cap model", "C-0037's series spring, rigid cap, rigid junctions, no height",
        designs.first { it.id == "A$recommended" }, reference
    )
    sensitivities += sensitivity(
        "cap junction azimuth",
        if (recommendedAzimuth > 0.0) "chord ALONG the flexure axis — strong constant in the loaded plane"
        else "chord ACROSS the flexure axis — strong constant in the free plane",
        designs.first {
            it.id == (if (recommendedAzimuth > 0.0) "Sx" else "Sy") + recommended
        }, reference
    )
    listOf(
        "rigid" to Double.POSITIVE_INFINITY,
        "two-link (nominal)" to maximumBaseRotationalStiffness(BForm.PHOSPHATE_RADIUS),
        "one-link (Pumm et al.'s built precedent)" to
                (bondHingeStiffness() + bondSlideStiffness() * 0.0)
    ).forEach { (label, value) ->
        sensitivities += sensitivity(
            "flexure end junction", label,
            design(
                "F", capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth, flexureJunctionRotational = value),
                DESIGN_HEIGHT, label
            ),
            reference
        )
    }
    listOf(12.0 to "pinned attachments, free overhangs", 16.0 to "clamped attachments").forEach {
        sensitivities += sensitivity(
            "cap end condition", it.second,
            design(
                "C", capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth, capEndFactor = it.first),
                DESIGN_HEIGHT, "two-link"
            ),
            reference
        )
    }
    listOf(
        Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY to "CanDo's model input, 460 pN·nm²",
        Gen1Tile.DUPLEX_TORSIONAL_PERSISTENCE * 4.141947 to "100 nm of torsional persistence",
        103.0 * 4.141947 to "Kriegel et al.'s measured 103 nm"
    ).forEach {
        sensitivities += sensitivity(
            "cap torsional rigidity", it.second,
            design(
                "T", capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth, torsionalRigidity = it.first),
                DESIGN_HEIGHT, "two-link"
            ),
            reference
        )
    }
    listOf(1.0 / 32.0, 1.0 / 8.0, 1.0, 8.0).forEach { factor ->
        val label = "k_s x %.4f (C-0020's four decades, unmeasured)".format(factor)
        sensitivities += sensitivity(
            "k_s", label,
            design(
                "K", capFor(
                    recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth,
                    base = TwoLinkBase.realisable(
                        DuplexBackbone(minorGrooveAngle = 180.0), inPlaneMultiplier = factor
                    ),
                    linkStiffness = LINK * factor
                ),
                DESIGN_HEIGHT, "two-link"
            ),
            reference
        )
    }
    listOf(EI to "CanDo's 230 pN·nm²", FIELDS_BENDING_RIGIDITY to "Fields et al.'s 172.9").forEach {
        sensitivities += sensitivity(
            "EI everywhere", it.second,
            design(
                "E", capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth, bendingRigidity = it.first),
                DESIGN_HEIGHT, "two-link", fieldsRigidity = it.first
            ),
            reference
        )
    }
    sensitivities += sensitivity(
        "mounting", "adverse — C-0035's determination reversed",
        design(
            "M", capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth), DESIGN_HEIGHT, "two-link",
            orientation = FlexureOrientation.ADVERSE
        ),
        reference
    )
    sensitivities += sensitivity(
        "draw-in model", "C-0025's deflected shape rather than C-0023's chord",
        design(
            "D", capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth), DESIGN_HEIGHT, "two-link",
            drawInModel = DrawInModel.SHAPE
        ),
        reference
    )

    // ---------------------------------------------------------------- convergence
    val convergence = mutableListOf<T106ConvergenceRecord>()
    val meshes = listOf(8, 16, 32, 64)
    val meshLoads = meshes.map {
        capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth, elementsPerLeg = it)
            .freeCriticalLoad
    }
    meshes.forEachIndexed { i, m ->
        convergence += T106ConvergenceRecord(
            "free-plane critical load of the capped truss", "elements per leg", m.toDouble(),
            meshLoads[i], abs(meshLoads[i] - meshLoads.last()) / meshLoads.last()
        )
    }
    val quadratures = listOf(256, 512, 1024, 2048)
    val quadratureValues = quadratures.map {
        cappedHeadFlexibilityByIntegration(
            EI, 7.0, HARD_BASE.restrainedAxis, 2, 0.0,
            headJunctionRotational = 78.24, headJunctionShear = LINK,
            capSeriesRotational = 689.0, flexureJunctionRotational = 78.24,
            flexureJunctionShear = LINK, rigidHeight = 1.0, steps = it
        ).translationUnderMoment
    }
    val closed = cappedHeadFlexibility(
        EI, 7.0, HARD_BASE.restrainedAxis, 2, 0.0,
        headJunctionRotational = 78.24, headJunctionShear = LINK,
        capSeriesRotational = 689.0, flexureJunctionRotational = 78.24,
        flexureJunctionShear = LINK, rigidHeight = 1.0
    ).translationUnderMoment
    quadratures.forEachIndexed { i, s ->
        convergence += T106ConvergenceRecord(
            "assembled C12 by quadrature against the closed form", "Simpson steps", s.toDouble(),
            quadratureValues[i], abs(quadratureValues[i] - closed) / closed
        )
    }
    val scans = listOf(64, 256, 1024, 4096)
    val cappedFlexibility = capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth).flexibility
    val spans = scans.map {
        coupledFlexureSpan(
            EI, cappedFlexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
            FlexureOrientation.FAVOURABLE, STRETCH, DrawInModel.CHORD, scanSteps = it
        )
    }
    scans.forEachIndexed { i, step ->
        convergence += T106ConvergenceRecord(
            "the placed span of the capped design", "span scan steps", step.toDouble(),
            spans[i], abs(spans[i] - spans.last()) / spans.last()
        )
    }

    // ---------------------------------------------------------------- reproductions
    val reproductions = mutableListOf<T106ReproductionRecord>()
    fun reproduce(name: String, published: Double, derived: Double, source: String) {
        reproductions += T106ReproductionRecord(
            name, published, derived, abs(derived - published) / abs(published), source
        )
    }
    val a8 = capFor(8, DESIGN_HEIGHT, asserted = true)
    reproduce("C-0037 L2a8 frame couple", 96.88, a8.frameCouple, "C-0037 recommended design")
    reproduce("C-0037 L2a8 free-plane critical load", 11.70, a8.assertedFreeCriticalLoad, "C-0037")
    reproduce("C-0037 L2a8 loaded-plane critical load", 9.77, a8.loadedCriticalLoad, "C-0037")
    reproduce("C-0037 leg axial stiffness at 8 nm", 44.0, a8.legAxial, "C-0037 model section")
    val a7 = capFor(7, DESIGN_HEIGHT, asserted = true)
    reproduce("C-0042 7 bp free-plane critical load", 10.30, a7.assertedFreeCriticalLoad, "C-0042")
    reproduce("C-0042 7 bp loaded-plane critical load", 9.77, a7.loadedCriticalLoad, "C-0042")
    reproduce(
        "C-0029 hard-chord ceiling", 78.24, maximumBaseRotationalStiffness(1.0), "C-0029 bound 2"
    )
    reproduce("C-0029 free axis", 13.53, 2.0 * bondHingeStiffness(), "C-0029 gate 3")
    reproduce("C-0029 head link axial stiffness", 64.71, LINK, "C-0037 head tie H2")
    reproduce(
        "C-0042 steric floor in base pairs", 6.0, pairStericFloorBasePairs().toDouble(),
        "C-0042 bound 1"
    )
    reproduce(
        "C-0042 chord budget", 91.76, chordBaseAxes(
            DuplexBackbone(minorGrooveAngle = 180.0), 0.3
        ).total, "C-0042 gate 3"
    )
    reproduce("Fields et al. implied rigidity", 172.906, FIELDS_BENDING_RIGIDITY, "C-0030")
    reproduce("SAXS interhelical distance", 2.69, Gen1Tile.INTERHELICAL_SHEET, "Fischer et al.")
    reproduce(
        "C-0037 L2a8 span at 8 nm", 33.43,
        designs.first { it.id == "A8" }.span, "C-0037 window table"
    )
    reproduce(
        "C-0037 L2a8 tangent at 8 nm", 26.09,
        designs.first { it.id == "A8" }.tangentAcceptable, "C-0037 window table"
    )
    reproduce(
        "C-0037 L2a8 supply/demand at 8 nm", 2.90,
        designs.first { it.id == "A8" }.supplyToDemandAcceptable, "C-0037 window table"
    )

    // ---------------------------------------------------------------- findings
    val solved = designs.first { it.id == "$recommendedPrefix$recommended" }
    val assertedAt = designs.first { it.id == "A$recommended" }
    val solved7 = designs.first { it.id == recommendedPrefix + "7" }
    val asserted7 = designs.first { it.id == "A7" }
    val findings = linkedMapOf<String, String>()

    findings["the cap is a body, and a count says which body"] = (
            "A leg is seated on a duplex only if its axis lies within one radius of that " +
                    "duplex's axis, and C-0042's steric floor already puts two legs a full " +
                    "diameter apart — so a duplex laid ACROSS the row seats NEITHER leg, at " +
                    "every separation from 6 to 16 bp (line contact 0.000 nm, against 2.000 nm " +
                    "for a crossbar laid ALONG the row). The flexure therefore cannot be the " +
                    "cap. The counting theorem says it again independently: the flexure's own " +
                    "end has two termini and there are two legs, so such a cap is one link per " +
                    "leg. THE CAP IS A SEPARATE CROSSBAR DUPLEX, %d bp at the recommended " +
                    "pitch, hosting THREE 90 degree junctions and %d covalent links."
            ).format(
            TrussCapGeometry(recommended * RISE).minimumBasePairs,
            TrussCapGeometry(recommended * RISE).covalentLinkCount
        )

    findings["C-0037's frame couple survives its own cap and not its own junctions"] = (
            "The crossbar's BENDING is the term C-0037's series spring is missing, and it is " +
                    "small: 12EI/w = %.0f against the couple's %.0f pN·nm/rad, so the frame " +
                    "couple falls only from %.2f to %.2f pN·nm/rad (%.1f %%) at %d bp. What " +
                    "moves the answer is the term neither claim carries: C-0029's two links at " +
                    "the LEG HEAD carry the head's ROTATION as well as its axial force, and a " +
                    "chord's two axes are simultaneous: %.2f pN·nm/rad in the loaded plane and " +
                    "%.2f in the free one at the adopted azimuth, against an assembled head of " +
                    "%.2f. C-0037 takes both as infinite, and the softer of the two CAPS the " +
                    "head restraint its frame couple can ever deliver."
            ).format(
            capBendingStiffness(EI, recommended * RISE),
            capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth).legAxial *
                    capFor(recommended, DESIGN_HEIGHT, junctionMisalignment = recommendedAzimuth)
                        .layout.acrossSecondMoment,
            assertedAt.frameCouple, solved.frameCouple,
            100.0 * (solved.frameCouple / assertedAt.frameCouple - 1.0),
            recommended,
            solved.headJunctionLoaded, solved.headJunctionFree,
            1.0 / trussTipFlexibility(
                EI, DESIGN_HEIGHT - 1.0, HARD_BASE.restrainedAxis, 2, 0.0
            ).rotationUnderMoment
        )

    findings["C-0042's seven base pairs survives, but only at the other cap azimuth"] = (
            "C-0042 resolves C-0037's 'between 6 and 8 bp' to SEVEN, on the grounds that 7 bp " +
                    "is the smallest row whose FREE plane has crossed above its loaded one — " +
                    "10.30 against 9.77 pN, reproduced here to %.1e. On a solved cap that " +
                    "verdict is CONDITIONAL on a variable neither claim has: with the cap " +
                    "junction's chord laid ALONG the flexure axis the free plane at 7 bp is " +
                    "%.2f pN and NO separation up to 16 bp crosses; laid ACROSS it the free " +
                    "plane is %.2f pN, the loaded plane governs and 7 bp is again the smallest " +
                    "that does it. So C-0042's separation stands and its reason does not: the " +
                    "crossing is bought at the cap, not at the row. The margin falls from " +
                    "%.2f / %.2f to %.2f / %.2f (CanDo / Fields et al.)."
            ).format(
            abs(a7.assertedFreeCriticalLoad - 10.30) / 10.30,
            designs.first { it.id == "Sx7" }.freeCriticalLoad,
            designs.first { it.id == "Sy7" }.freeCriticalLoad,
            asserted7.bucklingMargin, asserted7.bucklingMarginFields,
            solved7.bucklingMargin, solved7.bucklingMarginFields
        )

    findings["the cap has a height, and it is the one thing that runs the favourable way"] = (
            "The crossbar's axis sits one radius above the leg heads and the flexure butts its " +
                    "side, so the flexure's axis is e = 1.00 nm above them. A rigid offset is a " +
                    "unit-determinant congruence: C22 is untouched, C12 gains e·C22 and C11 " +
                    "gains 2e·C12 + e²C22. C12 is the entry C-0030 shows SUPPLIES the draw-in, " +
                    "so the cap's height raises the supply — %.2f against %.2f at the design " +
                    "point — while costing e of geometric softening in the buckling problem and " +
                    "one nanometre of leg for a given flexure height."
            ).format(solved.suppliedDrawInAcceptable, assertedAt.suppliedDrawInAcceptable)

    findings["the cap junction's azimuth is a third instance of one conserved budget"] = (
            "C-0037 found the leg-row azimuth spends Σx² + Σy² = w²/2; C-0042 found the base " +
                    "chord spends 4k_θ + 2k_s a² = 91.76 pN·nm/rad. The CAP junction spends the " +
                    "same 91.76, and in direct opposition: laid along the flexure axis it puts " +
                    "%.2f in the loaded plane (margin %.2f, tangent %.2f) and laid across it " +
                    "puts %.2f in the free one (margin %.2f, tangent %.2f). Unlike the leg row, " +
                    "this trade has no free corner — both planes want it."
            ).format(
            designs.first { it.id == "Sx" + recommended }.headJunctionLoaded,
            designs.first { it.id == "Sx" + recommended }.bucklingMargin,
            designs.first { it.id == "Sx" + recommended }.tangentAcceptable,
            designs.first { it.id == "Sy" + recommended }.headJunctionFree,
            designs.first { it.id == "Sy" + recommended }.bucklingMargin,
            designs.first { it.id == "Sy" + recommended }.tangentAcceptable
        )

    findings["one separation, and only one, lets the legs tie each other directly"] = (
            "The row pitch is quantised at the 0.34 nm rise and the sheet's own interhelical " +
                    "distance is the SAXS 2.69 nm, so at EIGHT base pairs — and at no other " +
                    "separation in the band — the two legs stand 2.72 nm apart, within 1.1 %% " +
                    "of the one spacing at which an ordinary antiparallel crossover can be " +
                    "built between them. That is the only motif in this programme's catalogue " +
                    "with a fitted constant, and it would put a SECOND axial tie in parallel " +
                    "with the crossbar's. It is reported and not adopted: the crossbar is still " +
                    "needed to reach the flexure, and 8 bp costs 0.34 nm of row width against " +
                    "the adopted %d bp."
            ).format(recommended)

    findings["the design that results"] = (
            "%d bp = %.2f nm row, legs %.2f nm on a %.2f nm crossbar of %d bp, flexure at " +
                    "%.1f nm; span %.2f nm = %.0f bp; tangent %.2f pN/nm; supply/demand %.2f; " +
                    "duty %.2f pN; P_c %.2f pN in the %s plane, margin %.2f on CanDo and %.2f " +
                    "on Fields et al.; verdict %s."
            ).format(
            recommended, recommended * RISE, solved.legLength,
            TrussCapGeometry(recommended * RISE).minimumLength,
            TrussCapGeometry(recommended * RISE).minimumBasePairs,
            solved.flexureHeight, solved.span, solved.spanBasePairs, solved.tangentAcceptable,
            solved.supplyToDemandAcceptable, solved.dutyDesiredElement, solved.criticalLoad,
            solved.governingPlane, solved.bucklingMargin, solved.bucklingMarginFields,
            solved.verdict
        )

    val literature = listOf(
        T106LiteratureRecord(
            "What torsional constant does a B-form duplex have?",
            "C = 103 ± 4 nm of torsional persistence at 6.5 pN, identical within error at " +
                    "20 mM NaCl, 100 mM NaCl, 500 mM NaCl and 10 mM MgCl₂: \"the measured " +
                    "high-force (6.5 pN) torsional stiffness values of C = 103 ± 4 nm are " +
                    "identical, within experimental errors, for all tested salt concentration, " +
                    "suggesting that the intrinsic torsional stiffness of DNA does not depend " +
                    "on salt\". That is 426.6 pN·nm², against CanDo's model input of 460",
            "read directly (abstract, verbatim, EuropePMC PMC5449586)",
            "Kriegel, Ermann, Forbes, Dulin, Dekker & Lipfert, Nucleic Acids Res. 45:5920 (2017)"
        ),
        T106LiteratureRecord(
            "Does the literature's only rigid out-of-plane mounting describe its CAP?",
            "Its cap is an entire 18-nm multilayer PLATE, not a duplex, and each of its two " +
                    "spacers is attached to it by ONE covalent bond per end (C-0037's hand count " +
                    "of the SI strand table: universal complements exactly 39 nt with no flank). " +
                    "So the one built precedent caps a two-leg frame with a body far stiffer " +
                    "than a crossbar AND attaches to it with HALF the links C-0037 assumes",
            "read directly by C-0037, re-read here",
            "Pumm et al., Nature 607:492 (2022), Methods and SI pp. 22–23"
        ),
        T106LiteratureRecord(
            "Is there a published crossbar tying two duplexes that stand off an origami sheet?",
            "NOT FOUND, over 10 further EuropePMC queries in this task on top of C-0042's 11, " +
                    "C-0037's ~72 and C-0029's ~110. Query strings recorded in the claim",
            "not found",
            "EuropePMC REST search"
        ),
        T106LiteratureRecord(
            "What is the nearest published relative of the cap junction — a duplex END meeting " +
                    "a duplex SIDE at 90 degrees?",
            "The T junction, and it is used exactly the way this claim's cap junction has to " +
                    "be: \"we present one-, two-, and three-layer T-shaped crossover tiles, by " +
                    "integrating T junction with antiparallel crossover tiles. These tiles " +
                    "carry over the orthogonal binding directions from T junction and retain " +
                    "the rigidity from antiparallel crossover tiles\". So in print the " +
                    "orthogonal joint supplies DIRECTION and the crossovers supply RIGIDITY — " +
                    "which is this claim's finding one level up. It is also an IN-PLANE motif " +
                    "and a base-pairing one, so it adds no covalent link and the counting " +
                    "theorem is untouched by it",
            "read directly (abstract, verbatim, EuropePMC PMC10667507)",
            "Chen, Xiao et al., 'DNA T-shaped crossover tiles for 2D tessellation and nanoring " +
                    "reconfiguration' (2023)"
        ),
        T106LiteratureRecord(
            "Is there a measured rotational stiffness for a perpendicular duplex-to-duplex joint?",
            "NOT FOUND — unchanged from C-0029, whose only nearby number is Pan et al.'s " +
                    "four-way junction scissor stiffness of 135 pN·nm/rad, fitted to MD",
            "not found",
            "C-0029's ~110 queries, re-checked"
        )
    )

    return T106Result(
        task = "T-106",
        leaf = "A8.2",
        temperatureKelvin = 300.0,
        kbT = 4.141947,
        units = "nm, pN, pN·nm, pN·nm/rad, pN/nm",
        bounds = bounds(),
        geometry = geometry(),
        couples = couples(),
        designs = designs,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        literature = literature,
        findings = findings,
        validity = listOf(
            "TRL 1-3. Nothing here is measured, and the motif is not demonstrated: two duplexes " +
                    "standing normal to a single-layer sheet under a shared crossbar is not in " +
                    "the literature, and this claim adds a third body to a motif that already " +
                    "had none.",
            "The cap's geometry is DERIVED but not ROUTED. This claim shows the cap must be a " +
                    "separate crossbar and how long it is; it does not run C-0042's closure " +
                    "search on it, so whether three 90 degree junctions close on one 13 bp " +
                    "crossbar is open exactly as the pair was before C-0042.",
            "A 13 bp crossbar is a short duplex and its own thermodynamic stability is not " +
                    "modelled. It is held by six covalent links and nothing else.",
            "The cap's bending is taken on the statically determinate frame-couple path, so its " +
                    "12EI/w is exact for moment-free attachments and 16EI/w for clamped ones; " +
                    "the true value lies between and the bracket is worth under 2 %.",
            "The cap's TORSION is the loaded plane's only cap term because the cross row has " +
                    "Σx² = 0 exactly (C-0037's finding 2, C-0042's Q4). A row that is not " +
                    "straight would couple the planes and this decomposition would not hold.",
            "The crossbar's bending about the sheet normal — the path that shares the head SHEAR " +
                    "between the legs — is 3EI/(w/2)³ per side, two orders above the legs' own " +
                    "sway, and is taken as rigid.",
            "The rigid height is the crossbar's radius. A design that let the flexure sit ON the " +
                    "crossbar rather than butt its side would double it.",
            "Cap yaw is not modelled and nothing loads it; the frame couple is taken to be " +
                    "unaffected by the axial preload, as C-0037 and C-0042 both assume.",
            "k_s is C-0020's DERIVED, unmeasured construction, and the base couple, the head " +
                    "links and the cap junctions ALL rest on it. Swept four decades.",
            "EI = 230 pN·nm² is a CanDo MODEL INPUT; every critical load is also given on " +
                    "Fields et al.'s implied 172.9. The torsional constant is carried on both " +
                    "CanDo's 460 and the measured 103 nm.",
            "SMALL DEFLECTION, exactly as C-0025, C-0028, C-0030, C-0037 and C-0042 flag."
        ),
        openQuestions = listOf(
            "Whether three 90 degree junctions close on one crossbar duplex — C-0042's search " +
                    "at the other end of the same legs, with a third junction and a lone seat.",
            "Whether a 13 bp crossbar is thermodynamically stable enough to be a structural " +
                    "member, or whether it must be longer and therefore heavier in plan.",
            "k_s. T-9, and it still moves a verdict.",
            "Whether the plan view admits 180 standoffs, 90 crossbars and 45 flexures on a " +
                    "40 x 40 nm footprint. T-96, now with a third body per joint.",
            "Whether this branch should be preferred to E5a16 at all. T-98 — and this claim " +
                    "ADDS an open premise where C-0042 removed one."
        ),
        citedNumbers = listOf(
            "phosphate radius 1.00 nm — CITED, READ DIRECTLY (Hedley et al., Phys. Rev. X " +
                    "14:031042, 2024), via C-0029",
            "duplex steric radius 1.00 nm — CITED, the standard 2 nm diameter",
            "rise per base pair 0.34 nm — CITED (Douglas et al., 2009)",
            "interhelical distance 2.69 nm — CITED, MEASURED by SAXS (Fischer et al., 2016)",
            "duplex EI 230 pN·nm² — CITED, a CanDo MODEL INPUT (Kim et al., NAR 40:2862, 2012)",
            "duplex GJ 460 pN·nm² — CITED, a CanDo MODEL INPUT; the measured torsional " +
                    "persistence 103 ± 4 nm (Kriegel et al., NAR 45:5920, 2017) gives 426.6, " +
                    "READ DIRECTLY here",
            "duplex stretch modulus 1100 pN — CITED, MEASURED (Wang et al., 1997)",
            "k_bond,θ 6.765 pN·nm/rad — CITED+FITTED (Chen et al., JACS 136:6995, 2014)",
            "k_bond,s 32.35 pN/nm — DERIVED (C-0020), NOT measured; swept four decades",
            "Fields et al.'s implied rigidity 172.9 pN·nm² — CITED, MEASURED (NAR 41:9881, 2013)",
            "Pumm et al.'s spacer count and attachment — CITED, READ DIRECTLY by C-0037",
            "per-path allowables 10 / 65 pN — CITED via C-0006",
            "§3 targets 100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM — CITED"
        )
    )
}

fun main() {
    val result = main0()
    val json = Json { prettyPrint = true }
    val file = File("gpd/results/T-106-truss-cap.json")
    file.parentFile?.mkdirs()
    file.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n"
    )

    println("T-106 — the truss cap as a solved body")
    println()
    println("cheap bounds")
    result.bounds.forEach { println("  %-72s %12.4f %s".format(it.name.take(72), it.value, it.unit)) }
    println()
    println("geometry (bp, w, cap nm/bp, contact across, contact along, separate body, links)")
    result.geometry.forEach {
        println(
            "  %3d %5.2f %6.2f %3d %6.3f %6.3f %6s %2d".format(
                it.separationBasePairs, it.separation, it.minimumCapLength,
                it.minimumCapBasePairs, it.perpendicularSeatContact, it.parallelSeatContact,
                it.separateBodyRequired, it.covalentLinkCount
            )
        )
    }
    println()
    println("frame couple (bp, Σd², k_a, asserted, cap bend, solved, ratio, torsion)")
    result.couples.forEach {
        println(
            "  %3d %7.4f %7.2f %8.2f %9.1f %8.2f %6.3f %8.1f".format(
                it.separationBasePairs, it.secondMoment, it.legAxialStiffness,
                it.assertedFrameCouple, it.capBendingPinned, it.solvedFrameCouple,
                it.ratioToAsserted, it.capTorsion
            )
        )
    }
    println()
    println("designs (id, cap, bp, h, span, tangent, supply, duty, Pc loaded/free, plane, margin, Fields, verdict)")
    result.designs.forEach {
        println(
            "  %-8s %-20s %3d %5.1f %6.2f %6.2f %5.2f %5.2f %6.2f %6.2f %-7s %5.2f %5.2f  %s".format(
                it.id, it.capModel.take(20), it.separationBasePairs, it.flexureHeight, it.span,
                it.tangentAcceptable, it.supplyToDemandAcceptable, it.dutyDesiredElement,
                it.loadedCriticalLoad, it.freeCriticalLoad, it.governingPlane,
                it.bucklingMargin, it.bucklingMarginFields, it.verdict
            )
        )
    }
    println()
    println("sensitivities (axis, label, frame, Pc, plane, margin, Fields, tangent, supply, moves)")
    result.sensitivities.forEach {
        println(
            "  %-26s %-46s %7.2f %6.2f %-7s %5.2f %5.2f %6.2f %5.2f %s".format(
                it.axis.take(26), it.label.take(46), it.frameCouple, it.criticalLoad,
                it.governingPlane, it.bucklingMargin, it.bucklingMarginFields,
                it.tangentAcceptable, it.supplyToDemandAcceptable, it.verdictMoves
            )
        )
    }
    println()
    println("convergence")
    result.convergence.forEach {
        println(
            "  %-52s %-24s %8.3f %14.9f %10.2e".format(
                it.quantity.take(52), it.control, it.level, it.value, it.departureFromFinest
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    result.reproductions.forEach {
        println(
            "  %-48s %12.6f %12.6f %10.2e".format(
                it.quantity.take(48), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
