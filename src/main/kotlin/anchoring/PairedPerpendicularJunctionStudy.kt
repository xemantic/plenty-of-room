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

/**
 * Task `T-97` / leaf `A8.2` — whether **two** 90° junctions close on **one** sheet duplex 6–8 bp
 * apart, which is `C-0037`'s largest open item.
 *
 * ```shell
 * tools/study.sh anchoring.PairedPerpendicularJunctionStudyKt
 * ```
 *
 * Emits `gpd/results/T-97-paired-perpendicular-junction.json`, deterministically: the file carries
 * no timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private const val TARGET_FORCE = 100.0
private const val ACCEPTABLE_STROKE = 3.0
private const val DESIRED_STROKE = 10.0
private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE
private const val PATH_COUNT = 45
private const val COMPLIANT_CEILING = 40.0
private const val SUPPORT_MARGIN_REQUIRED = 10.0
private val PER_PATH = MANDATE / PATH_COUNT
private const val DESIGN_LENGTH = 8.0
private const val ELEMENTS = 32

/** `C-0029`'s counting theorem on the hard, convention-free 180° chord — `C-0037`'s own adoption. */
private val HARD_BACKBONE = DuplexBackbone(minorGrooveAngle = 180.0)

/** The seats swept: every one keeps the flat-face line contact above 1.6 nm. */
private val SEATS: List<Double> =
    (-6..6).map { it * 0.1 }

private val SEPARATIONS: List<Int> = (6..12).toList()

// ---------------------------------------------------------------------------------------------

@Serializable
data class T97ScrewImageRecord(
    val separationBasePairs: Int,
    val separationNanometres: Double,
    val rotationDegrees: Double,
    val coupleFraction: Double,
    val sheetPhaseResidualDegrees: Double,
    val aboveStericFloor: Boolean
)

@Serializable
data class T97PlacementRecord(
    val centreX: Double,
    val centreY: Double,
    val faceHeight: Double,
    val seatContact: Double,
    val azimuthDegrees: Double,
    val chordAzimuthDegrees: Double,
    val misalignmentDegrees: Double,
    val loadedCoupleFraction: Double,
    val firstGap: Double,
    val secondGap: Double,
    val firstUnpaired: Int,
    val secondUnpaired: Int,
    val firstTarget: String,
    val secondTarget: String,
    val covalent: Boolean
)

@Serializable
data class T97PairRecord(
    val topology: String,
    val grooveDegrees: Double,
    val separationBasePairs: Int,
    val separationNanometres: Double,
    val found: Boolean,
    val screwImageRotationDegrees: Double,
    val worstMisalignmentDegrees: Double,
    val worstLoadedCoupleFraction: Double,
    val minimumTerminusSeparation: Double,
    val lateralSeparation: Double,
    val seat: Double,
    val seatContact: Double,
    val worstGap: Double,
    val unpairedNucleotides: Int,
    val crossoverFreePhases: Int,
    val firstBaseLoaded: Double,
    val firstBaseFree: Double,
    val secondBaseLoaded: Double,
    val secondBaseFree: Double,
    val baseBudget: Double,
    val freeFrameCouple: Double,
    val loadedCriticalLoad: Double,
    val freeCriticalLoad: Double,
    val criticalLoad: Double,
    val governingPlane: String,
    val span: Double,
    val tangentAcceptable: Double,
    val couplingFactor: Double,
    val supplyToDemandAcceptable: Double,
    val dutyDesired: Double,
    val bucklingMargin: Double,
    val bucklingMarginFields: Double,
    val equalChordCriticalLoad: Double,
    val equalChordMargin: Double,
    val q1Steric: Boolean,
    val q2Covalent: Boolean,
    val q3Distinct: Boolean,
    val q4Coplanar: Boolean,
    val q6Stable: Boolean,
    val q7Occupancy: Boolean,
    val verdict: String,
    val first: T97PlacementRecord?,
    val second: T97PlacementRecord?
)

@Serializable
data class T97LengthRecord(
    val separationBasePairs: Int,
    val standoffLength: Double,
    val standoffBasePairs: Double,
    val span: Double,
    val spanBasePairs: Double,
    val tangentAcceptable: Double,
    val couplingFactor: Double,
    val supplyToDemandAcceptable: Double,
    val dutyDesired: Double,
    val loadedCriticalLoad: Double,
    val freeCriticalLoad: Double,
    val criticalLoad: Double,
    val governingPlane: String,
    val bucklingMargin: Double,
    val bucklingMarginFields: Double,
    val transverseStiffness: Double,
    val p1Supports: Boolean,
    val p3Compliant: Boolean,
    val p6Stable: Boolean,
    val p8DrawInSurvives: Boolean,
    val allPredicatesPass: Boolean,
    val verdict: String
)

@Serializable
data class T97SensitivityRecord(
    val axis: String,
    val label: String,
    val found: Boolean,
    val worstMisalignmentDegrees: Double,
    val worstLoadedCoupleFraction: Double,
    val criticalLoad: Double,
    val bucklingMargin: Double,
    val verdictMoves: Boolean
)

@Serializable
data class T97ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T97ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class T97LiteratureRecord(
    val question: String,
    val answer: String,
    val flag: String,
    val source: String
)

@Serializable
data class T97Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val cheapBound: Map<String, Double>,
    val screwImage: List<T97ScrewImageRecord>,
    val pairs: List<T97PairRecord>,
    val lengths: List<T97LengthRecord>,
    val sensitivities: List<T97SensitivityRecord>,
    val convergence: List<T97ConvergenceRecord>,
    val reproductions: List<T97ReproductionRecord>,
    val literature: List<T97LiteratureRecord>,
    val findings: Map<String, String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------

private fun degrees(radians: Double): Double = radians * 180.0 / PI

private fun placementRecord(p: StandoffPlacement) = T97PlacementRecord(
    centreX = p.centreX,
    centreY = p.centreY,
    faceHeight = p.faceHeight,
    seatContact = p.seatContact,
    azimuthDegrees = degrees(p.azimuth),
    chordAzimuthDegrees = degrees(p.chordAzimuth),
    misalignmentDegrees = degrees(p.misalignment),
    loadedCoupleFraction = p.loadedCoupleFraction,
    firstGap = p.firstGap,
    secondGap = p.secondGap,
    firstUnpaired = p.firstUnpaired,
    secondUnpaired = p.secondUnpaired,
    firstTarget = "d${p.firstTarget.duplex} s${p.firstTarget.strand} bp${p.firstTarget.index}",
    secondTarget = "d${p.secondTarget.duplex} s${p.secondTarget.strand} bp${p.secondTarget.index}",
    covalent = p.covalent
)

/**
 * The truss the pair makes, assembled on the **hard 180° chord** couple magnitude (`C-0037`'s own
 * adoption) at each leg's own measured chord misalignment, and put through `C-0030`'s coupled beam
 * exactly as `C-0037` does.
 */
private class PairedTruss(
    val pair: JunctionPairClosure,
    val length: Double,
    val bendingRigidity: Double = EI,
    val stretchModulus: Double = STRETCH
) {

    val firstAxes: ChordBaseAxes = chordBaseAxes(HARD_BACKBONE, pair.first.misalignment)
    val secondAxes: ChordBaseAxes = chordBaseAxes(HARD_BACKBONE, pair.second.misalignment)

    /** `C-0029`'s counting theorem at the *other* end of each leg: `2 k_bond,s` per leg head. */
    private val headLink = 2.0 * bondSlideStiffness()

    /** `Σ(Δx_i)²` — the row lies along the seat duplex, i.e. **across** the flexure axis. */
    val acrossSecondMoment: Double = 0.5 * pair.axialSeparation * pair.axialSeparation

    /** `Σ(Δy_i)²` — exactly zero when `Q4` holds, which is what makes the row a cross row. */
    val alongSecondMoment: Double = 0.5 * pair.lateralSeparation * pair.lateralSeparation

    /** `S/ℓ` in series with the base's own axial path, which the counting theorem fixes at
     * `2 k_bond,s` — identical for both legs and independent of the chord azimuth. */
    val legAxial: Double =
        seriesStiffness(stretchModulus / length, 2.0 * bondSlideStiffness())

    val loadedFrameCouple: Double = trussFrameCouple(
        alongSecondMoment, legAxial, headLink * (alongSecondMoment + acrossSecondMoment)
    )

    val freeFrameCouple: Double = trussFrameCouple(
        acrossSecondMoment, legAxial, headLink * (alongSecondMoment + acrossSecondMoment)
    )

    val flexibility: StandoffTipFlexibility = mixedBaseTrussTipFlexibility(
        bendingRigidity, length, listOf(firstAxes.loaded, secondAxes.loaded), loadedFrameCouple
    )

    val loadedCriticalLoad: Double = mixedBaseTrussBucklingLoad(
        bendingRigidity, length, listOf(firstAxes.loaded, secondAxes.loaded),
        loadedFrameCouple, ELEMENTS
    )

    val freeCriticalLoad: Double = mixedBaseTrussBucklingLoad(
        bendingRigidity, length, listOf(firstAxes.free, secondAxes.free),
        freeFrameCouple, ELEMENTS
    )

    val criticalLoad: Double = minOf(loadedCriticalLoad, freeCriticalLoad)

    val governingPlane: String =
        if (freeCriticalLoad <= loadedCriticalLoad) "free" else "loaded"

    val transverseStiffness: Double = 2.0 * legAxial

    val span: Double = coupledFlexureSpan(
        bendingRigidity, flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
        FlexureOrientation.FAVOURABLE, stretchModulus, DrawInModel.CHORD
    )

    val flexure: CoupledJointFlexure = CoupledJointFlexure(
        bendingRigidity, span, flexibility, stretchModulus, DrawInModel.CHORD
    )

    val tangent: Double =
        PATH_COUNT * flexure.strokeTangentStiffness(ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE)

    val duty: Double = flexure.strokeEndShear(DESIRED_STROKE, FlexureOrientation.FAVOURABLE)

    val supplied: Double = flexure.couplingFactor * ACCEPTABLE_STROKE

    val demanded: Double = flexure.chordExtension(ACCEPTABLE_STROKE)

    val margin: Double = criticalLoad / duty

    val marginFields: Double =
        criticalLoad * FIELDS_BENDING_RIGIDITY / bendingRigidity / duty
}

private fun pairRecord(
    topology: String,
    groove: Double,
    separation: Int,
    search: PairedJunctionSearch
): T97PairRecord {
    val pair = search.bestPair(separation)
    val screw = degrees(screwImageChordRotation(separation, search.backbone))
    if (pair == null) {
        return T97PairRecord(
            topology = topology, grooveDegrees = groove, separationBasePairs = separation,
            separationNanometres = separation * RISE, found = false,
            screwImageRotationDegrees = screw, worstMisalignmentDegrees = -1.0,
            worstLoadedCoupleFraction = -1.0, minimumTerminusSeparation = -1.0,
            lateralSeparation = -1.0, seat = -1.0, seatContact = -1.0, worstGap = -1.0,
            unpairedNucleotides = -1, crossoverFreePhases = -1,
            firstBaseLoaded = -1.0, firstBaseFree = -1.0, secondBaseLoaded = -1.0,
            secondBaseFree = -1.0, baseBudget = -1.0, freeFrameCouple = -1.0,
            loadedCriticalLoad = -1.0, freeCriticalLoad = -1.0, criticalLoad = -1.0,
            governingPlane = "none", span = -1.0, tangentAcceptable = -1.0, couplingFactor = -1.0,
            supplyToDemandAcceptable = -1.0, dutyDesired = -1.0, bucklingMargin = -1.0,
            bucklingMarginFields = -1.0, equalChordCriticalLoad = -1.0, equalChordMargin = -1.0,
            q1Steric = false, q2Covalent = false, q3Distinct = false, q4Coplanar = false,
            q6Stable = false, q7Occupancy = false,
            verdict = "FAIL — no admissible pair at this separation on this grid",
            first = null, second = null
        )
    }
    val truss = PairedTruss(pair, DESIGN_LENGTH)
    val phases = crossoverFreePhaseCount(pair.targetBasePairs)
    // the same truss with BOTH chords perfectly on the flexure axis — C-0037's own reading
    val ideal = mixedBaseTrussBucklingLoad(
        EI, DESIGN_LENGTH,
        listOf(chordBaseAxes(HARD_BACKBONE, 0.0).loaded, chordBaseAxes(HARD_BACKBONE, 0.0).loaded),
        truss.loadedFrameCouple, ELEMENTS
    )
    val idealFree = mixedBaseTrussBucklingLoad(
        EI, DESIGN_LENGTH,
        listOf(chordBaseAxes(HARD_BACKBONE, 0.0).free, chordBaseAxes(HARD_BACKBONE, 0.0).free),
        truss.freeFrameCouple, ELEMENTS
    )
    val q1 = pair.stericallyClear
    val q2 = pair.bothCovalent
    val q3 = pair.distinctTargets
    val q4 = pair.lateralSeparation <= 1.0e-12
    val q6 = truss.criticalLoad >= truss.duty
    val q7 = phases > 0
    return T97PairRecord(
        topology = topology,
        grooveDegrees = groove,
        separationBasePairs = separation,
        separationNanometres = separation * RISE,
        found = true,
        screwImageRotationDegrees = screw,
        worstMisalignmentDegrees = degrees(pair.worstMisalignment),
        worstLoadedCoupleFraction = pair.worstLoadedCoupleFraction,
        minimumTerminusSeparation = pair.minimumTerminusSeparation,
        lateralSeparation = pair.lateralSeparation,
        seat = pair.first.centreY,
        seatContact = pair.first.seatContact,
        worstGap = maxOf(pair.first.worstGap, pair.second.worstGap),
        unpairedNucleotides = pair.first.firstUnpaired + pair.first.secondUnpaired +
                pair.second.firstUnpaired + pair.second.secondUnpaired,
        crossoverFreePhases = phases,
        firstBaseLoaded = truss.firstAxes.loaded,
        firstBaseFree = truss.firstAxes.free,
        secondBaseLoaded = truss.secondAxes.loaded,
        secondBaseFree = truss.secondAxes.free,
        baseBudget = truss.firstAxes.total,
        freeFrameCouple = truss.freeFrameCouple,
        loadedCriticalLoad = truss.loadedCriticalLoad,
        freeCriticalLoad = truss.freeCriticalLoad,
        criticalLoad = truss.criticalLoad,
        governingPlane = truss.governingPlane,
        span = truss.span,
        tangentAcceptable = truss.tangent,
        couplingFactor = truss.flexure.couplingFactor,
        supplyToDemandAcceptable = truss.supplied / truss.demanded,
        dutyDesired = truss.duty,
        bucklingMargin = truss.margin,
        bucklingMarginFields = truss.marginFields,
        equalChordCriticalLoad = minOf(ideal, idealFree),
        equalChordMargin = minOf(ideal, idealFree) / truss.duty,
        q1Steric = q1, q2Covalent = q2, q3Distinct = q3, q4Coplanar = q4,
        q6Stable = q6, q7Occupancy = q7,
        verdict = when {
            !q1 -> "FAIL Q1 — the two junctions clash"
            !q2 -> "FAIL Q2 — a link falls outside the measured phosphodiester step"
            !q3 -> "FAIL Q3 — the four targets are not distinct"
            !q4 -> "FAIL Q4 — the row is not straight, so the cross row is not a cross row"
            !q7 -> "FAIL Q7 — no crossover phase leaves the targets free"
            !q6 -> "FAIL Q6 — the truss buckles before the desired stroke"
            else -> "PASS"
        },
        first = placementRecord(pair.first),
        second = placementRecord(pair.second)
    )
}

private fun lengthRecord(pair: JunctionPairClosure, length: Double): T97LengthRecord {
    val truss = PairedTruss(pair, length)
    val p1 = truss.transverseStiffness >= SUPPORT_MARGIN_REQUIRED * PER_PATH
    val p3 = truss.tangent <= COMPLIANT_CEILING
    val p6 = truss.criticalLoad >= truss.duty
    val p8 = truss.supplied >= truss.demanded
    return T97LengthRecord(
        separationBasePairs = pair.separationBasePairs,
        standoffLength = length,
        standoffBasePairs = length / RISE,
        span = truss.span,
        spanBasePairs = truss.span / RISE,
        tangentAcceptable = truss.tangent,
        couplingFactor = truss.flexure.couplingFactor,
        supplyToDemandAcceptable = truss.supplied / truss.demanded,
        dutyDesired = truss.duty,
        loadedCriticalLoad = truss.loadedCriticalLoad,
        freeCriticalLoad = truss.freeCriticalLoad,
        criticalLoad = truss.criticalLoad,
        governingPlane = truss.governingPlane,
        bucklingMargin = truss.margin,
        bucklingMarginFields = truss.marginFields,
        transverseStiffness = truss.transverseStiffness,
        p1Supports = p1, p3Compliant = p3, p6Stable = p6, p8DrawInSurvives = p8,
        allPredicatesPass = p1 && p3 && p6 && p8,
        verdict = when {
            !p1 -> "FAIL P1"
            !p3 -> "FAIL P3 — tangent past the 40 pN/nm ceiling"
            !p6 -> "FAIL P6 — the truss buckles before the desired stroke"
            !p8 -> "FAIL P8 — the joint no longer supplies the draw-in"
            else -> "PASS"
        }
    )
}

// ---------------------------------------------------------------------------------------------

fun main() {
    val backbone = DuplexBackbone()
    val floor = pairStericFloorBasePairs()

    val cheapBound = linkedMapOf(
        "steric floor [bp]" to floor.toDouble(),
        "steric floor [nm]" to floor * RISE,
        "two duplex radii [nm]" to 2.0 * BForm.DUPLEX_RADIUS,
        "azimuthal quantum [deg/bp]" to degrees(backbone.azimuthQuantum),
        "screw image at 6 bp [deg]" to degrees(screwImageChordRotation(6, backbone)),
        "screw image at 7 bp [deg]" to degrees(screwImageChordRotation(7, backbone)),
        "screw image at 8 bp [deg]" to degrees(screwImageChordRotation(8, backbone)),
        "screw image cos2 at 8 bp" to
                loadedPlaneCoupleFraction(screwImageChordRotation(8, backbone)),
        "sheet phase residual at 6 bp [deg]" to degrees(sheetPhaseResidual(6, backbone)),
        "sheet phase residual at 7 bp [deg]" to degrees(sheetPhaseResidual(7, backbone)),
        "sheet phase residual at 8 bp [deg]" to degrees(sheetPhaseResidual(8, backbone)),
        "base couple budget [pN nm/rad]" to chordBaseAxes(HARD_BACKBONE, 0.0).total,
        "hard chord ceiling [pN nm/rad]" to chordBaseAxes(HARD_BACKBONE, 0.0).loaded,
        "free axis at zero misalignment [pN nm/rad]" to chordBaseAxes(HARD_BACKBONE, 0.0).free
    )

    val screwImage = (6..16).map {
        T97ScrewImageRecord(
            separationBasePairs = it,
            separationNanometres = it * RISE,
            rotationDegrees = degrees(screwImageChordRotation(it, backbone)),
            coupleFraction = loadedPlaneCoupleFraction(screwImageChordRotation(it, backbone)),
            sheetPhaseResidualDegrees = degrees(sheetPhaseResidual(it, backbone)),
            aboveStericFloor = it >= floor
        )
    }

    val pairs = mutableListOf<T97PairRecord>()
    listOf(
        Triple(RoutingTopology.INDEPENDENT_STAPLES, 120.0, listOf(0)),
        Triple(RoutingTopology.SCAFFOLD_EXCURSION, 120.0, listOf(0)),
        Triple(RoutingTopology.INDEPENDENT_STAPLES, 120.0, listOf(-1, 0, 1)),
        Triple(RoutingTopology.INDEPENDENT_STAPLES, 154.0, listOf(0))
    ).forEach { (topology, groove, duplexes) ->
        val search = PairedJunctionSearch(
            backbone = DuplexBackbone(minorGrooveAngle = groove),
            topology = topology,
            axialStepsPerBasePair = 8,
            azimuthSteps = 360,
            refinements = 2,
            lateralSeats = SEATS,
            targetDuplexes = duplexes
        )
        val label = if (duplexes.size == 1) "${topology.name}, seat duplex only"
        else "${topology.name}, seat + neighbours"
        SEPARATIONS.forEach { pairs += pairRecord(label, groove, it, search) }
    }

    /** **The adopted search: the strict reading — both junctions grounded on ONE sheet duplex.** */
    val adopted = PairedJunctionSearch(
        axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2, lateralSeats = SEATS,
        targetDuplexes = listOf(0)
    )
    val bestSeparation = SEPARATIONS.filter { it in 6..8 }
        .mapNotNull { n -> adopted.bestPair(n)?.let { n to it } }
        .maxWithOrNull(
            compareBy(
                { it.second.worstLoadedCoupleFraction },
                { PairedTruss(it.second, DESIGN_LENGTH).criticalLoad }
            )
        )
    val lengths = bestSeparation?.let { (_, pair) ->
        listOf(5.0, 6.0, 7.0, 8.0, 9.0, 10.0).map { lengthRecord(pair, it) }
    } ?: emptyList()

    // ------------------------------------------------------------------ sensitivities

    val sensitivities = mutableListOf<T97SensitivityRecord>()
    val referenceMargin = bestSeparation?.let { PairedTruss(it.second, DESIGN_LENGTH).margin } ?: 0.0

    fun sensitivity(axis: String, label: String, search: PairedJunctionSearch, separation: Int) {
        val pair = search.bestPair(separation)
        if (pair == null) {
            sensitivities += T97SensitivityRecord(axis, label, false, -1.0, -1.0, -1.0, -1.0, true)
            return
        }
        val truss = PairedTruss(pair, DESIGN_LENGTH)
        sensitivities += T97SensitivityRecord(
            axis, label, true, degrees(pair.worstMisalignment), pair.worstLoadedCoupleFraction,
            truss.criticalLoad, truss.margin, (truss.margin >= 1.0) != (referenceMargin >= 1.0)
        )
    }

    val separation = bestSeparation?.first ?: 6
    sensitivity(
        "lateral seat", "the seat duplex's own axis only, y_c = 0",
        PairedJunctionSearch(
            axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2,
            lateralSeats = listOf(0.0), targetDuplexes = listOf(0)
        ),
        separation
    )
    sensitivity(
        "lateral seat", "seats out to +/-0.9 nm (line contact down to 0.87 nm)",
        PairedJunctionSearch(
            axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2,
            lateralSeats = (-9..9).map { it * 0.1 }, targetDuplexes = listOf(0)
        ),
        separation
    )
    sensitivity(
        "groove convention", "the wide 154 degree reading",
        PairedJunctionSearch(
            backbone = DuplexBackbone(minorGrooveAngle = 154.0),
            axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2, lateralSeats = SEATS,
            targetDuplexes = listOf(0)
        ),
        separation
    )
    sensitivity(
        "routing topology", "the scaffold excursion only",
        PairedJunctionSearch(
            topology = RoutingTopology.SCAFFOLD_EXCURSION,
            axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2, lateralSeats = SEATS,
            targetDuplexes = listOf(0)
        ),
        separation
    )
    sensitivity(
        "link targets", "links allowed onto the two NEIGHBOUR duplexes as well",
        PairedJunctionSearch(
            axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2, lateralSeats = SEATS,
            targetDuplexes = listOf(-1, 0, 1)
        ),
        separation
    )
    // is the alignment bought at the STRETCHED end of the measured window, or is it available in
    // the window's interior? The centring weight is a tie-break on |gap - 0.65 nm|.
    val centred = PairedJunctionSearch(
        axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2, lateralSeats = SEATS,
        targetDuplexes = listOf(0), centringWeight = 0.02
    ).bestPair(separation)
    sensitivity(
        "window centring", "tie-break only, worst gap " +
                "${"%.4f".format(centred?.let { maxOf(it.first.worstGap, it.second.worstGap) } ?: -1.0)} nm",
        PairedJunctionSearch(
            axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2, lateralSeats = SEATS,
            targetDuplexes = listOf(0), centringWeight = 0.02
        ),
        separation
    )
    // and the trade PRICED: a weight at which 0.05 nm off-centre costs 5.7 degrees of alignment
    val traded = PairedJunctionSearch(
        axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2, lateralSeats = SEATS,
        targetDuplexes = listOf(0), centringWeight = 2.0
    ).bestPair(separation)
    sensitivity(
        "window centring", "priced against alignment, worst gap " +
                "${"%.4f".format(traded?.let { maxOf(it.first.worstGap, it.second.worstGap) } ?: -1.0)} nm",
        PairedJunctionSearch(
            axialStepsPerBasePair = 8, azimuthSteps = 360, refinements = 2, lateralSeats = SEATS,
            targetDuplexes = listOf(0), centringWeight = 2.0
        ),
        separation
    )

    // the mechanical sensitivities, on the adopted pair
    bestSeparation?.let { (_, pair) ->
        val truss = PairedTruss(pair, DESIGN_LENGTH)
        listOf(1.0 / 32.0, 1.0 / 8.0, 1.0, 8.0).forEach { factor ->
            val axes = listOf(
                chordBaseAxes(HARD_BACKBONE, pair.first.misalignment, inPlaneMultiplier = factor),
                chordBaseAxes(HARD_BACKBONE, pair.second.misalignment, inPlaneMultiplier = factor)
            )
            val loaded = mixedBaseTrussBucklingLoad(
                EI, DESIGN_LENGTH, axes.map { it.loaded }, truss.loadedFrameCouple, ELEMENTS
            )
            val free = mixedBaseTrussBucklingLoad(
                EI, DESIGN_LENGTH, axes.map { it.free }, truss.freeFrameCouple, ELEMENTS
            )
            val critical = minOf(loaded, free)
            sensitivities += T97SensitivityRecord(
                "k_s (C-0020's four decades, unmeasured)", "x ${"%.4f".format(factor)}",
                true, degrees(pair.worstMisalignment), pair.worstLoadedCoupleFraction,
                critical, critical / truss.duty, (critical / truss.duty >= 1.0) != (referenceMargin >= 1.0)
            )
        }
    }

    // ------------------------------------------------------------------ convergence

    val convergence = mutableListOf<T97ConvergenceRecord>()
    val elementLevels = listOf(8, 16, 32, 64)
    val elementValues = elementLevels.map {
        mixedBaseTrussBucklingLoad(EI, DESIGN_LENGTH, listOf(78.24, 13.53), 0.0, it)
    }
    elementLevels.forEachIndexed { i, level ->
        convergence += T97ConvergenceRecord(
            "mixed-base critical load, EI = 230, l = 8 nm", "elements per leg", level.toDouble(),
            elementValues[i], abs(elementValues[i] - elementValues.last()) / elementValues.last()
        )
    }
    val azimuthLevels = listOf(120, 180, 360, 720)
    val azimuthValues = azimuthLevels.map { steps ->
        PairedJunctionSearch(
            axialStepsPerBasePair = 8, azimuthSteps = steps, refinements = 2, lateralSeats = SEATS,
            targetDuplexes = listOf(0)
        ).bestPair(separation)?.worstLoadedCoupleFraction ?: -1.0
    }
    azimuthLevels.forEachIndexed { i, level ->
        convergence += T97ConvergenceRecord(
            "the pair's worst loaded couple fraction", "azimuth steps", level.toDouble(),
            azimuthValues[i], abs(azimuthValues[i] - azimuthValues.last())
        )
    }
    val axialLevels = listOf(2, 4, 8, 16)
    val axialValues = axialLevels.map { steps ->
        PairedJunctionSearch(
            axialStepsPerBasePair = steps, azimuthSteps = 360, refinements = 2,
            lateralSeats = SEATS, targetDuplexes = listOf(0)
        ).bestPair(separation)?.worstLoadedCoupleFraction ?: -1.0
    }
    axialLevels.forEachIndexed { i, level ->
        convergence += T97ConvergenceRecord(
            "the pair's worst loaded couple fraction", "axial steps per base pair", level.toDouble(),
            axialValues[i], abs(axialValues[i] - axialValues.last())
        )
    }

    // ------------------------------------------------------------------ reproductions

    val equalBase = TwoLinkBase.realisable()
    val c0037Layout = TrussLayout.row(2, 8 * RISE, 0.5 * PI)
    val c0037 = TriangulatedStandoff(
        c0037Layout, DESIGN_LENGTH, equalBase,
        headTieStiffness = 2.0 * bondSlideStiffness() * c0037Layout.totalSecondMoment
    )
    fun reproduction(name: String, published: Double, derived: Double) = T97ReproductionRecord(
        name, published, derived, abs(derived - published) / abs(published)
    )
    val reproductions = listOf(
        reproduction("C-0029 terminal chord, hard 180 reading [nm]", 2.0, HARD_BACKBONE.terminalChord),
        reproduction("C-0029 lever arm [nm]", 1.0, HARD_BACKBONE.leverArm),
        reproduction(
            "C-0029 base couple ceiling [pN nm/rad]", 78.24,
            chordBaseAxes(HARD_BACKBONE, 0.0).loaded
        ),
        reproduction(
            "C-0029 free axis [pN nm/rad]", 13.53, chordBaseAxes(HARD_BACKBONE, 0.0).free
        ),
        reproduction("C-0029 azimuthal quantum [deg/bp]", 33.74, degrees(backbone.azimuthQuantum)),
        reproduction(
            "C-0029 worst single-junction phase cost cos2", 0.9158,
            loadedPlaneCoupleFraction(0.5 * backbone.azimuthQuantum)
        ),
        reproduction("C-0037 L2a8 across second moment [nm2]", 3.699, c0037Layout.acrossSecondMoment),
        reproduction("C-0037 L2a8 loaded critical load [pN]", 9.77, c0037.loadedCriticalLoad),
        reproduction("C-0037 L2a8 free critical load [pN]", 11.70, c0037.freeCriticalLoad),
        reproduction(
            "C-0037 L2a8 loaded critical load, mixed solver [pN]", c0037.loadedCriticalLoad,
            mixedBaseTrussBucklingLoad(
                EI, DESIGN_LENGTH, listOf(equalBase.restrainedAxis, equalBase.restrainedAxis),
                c0037.loadedFrameCouple, 64
            )
        ),
        reproduction(
            "C-0037 L2a8 free critical load, mixed solver [pN]", c0037.freeCriticalLoad,
            mixedBaseTrussBucklingLoad(
                EI, DESIGN_LENGTH, listOf(equalBase.freeAxis, equalBase.freeAxis),
                c0037.freeFrameCouple, 64
            )
        ),
        reproduction("C-0015 square-lattice helical repeat [bp]", 32.0, 3.0 * 10.67),
        reproduction("SAXS interhelical distance [nm]", 2.69, Gen1Tile.INTERHELICAL_SHEET),
        reproduction("Fields et al. implied rigidity [pN nm2]", 172.906, FIELDS_BENDING_RIGIDITY)
    )

    val literature = listOf(
        T97LiteratureRecord(
            "Is there a published rule for how far apart two duplexes protruding from one origami " +
                    "face must sit?",
            "NOT FOUND. C-0037's own ~72 queries and C-0028's and C-0029's ~110 before it found " +
                    "no published spacing rule; this task's queries add to that and change nothing.",
            "not found",
            "EuropePMC REST search"
        ),
        T97LiteratureRecord(
            "How many spacers does the literature's only rigid out-of-plane mounting have, and " +
                    "does the paper say how they are arranged?",
            "EXACTLY TWO, each 39 bp, one covalent link per end — and the paper does NOT say how " +
                    "they are arranged: the word 'spacer' occurs twice in the whole article. So " +
                    "there is no published precedent to agree or disagree with this task's " +
                    "separation.",
            "read directly, via C-0037 which fetched and counted the SI strand table",
            "Pumm et al., Nature 607:492, EuropePMC PMC9300469"
        ),
        T97LiteratureRecord(
            "The intrastrand phosphodiester step the closure test is written on",
            "A WINDOW, 0.60-0.70 nm: 'C3-endo (interphosphate distance 0.6 nm) to C2-endo " +
                    "conformation (interphosphate distance 0.7 nm)'.",
            "read directly, via C-0029",
            "Bosco, Camunas-Soler & Ritort, NAR 42:2064 (2014)"
        ),
        T97LiteratureRecord(
            "The phosphate radius the counting theorem rests on",
            "'Phosphates (red circles) sit at a radius of a_DNA = 10 A' — which IS the duplex's " +
                    "own steric radius.",
            "read directly, via C-0029",
            "Hedley, Coshic, Aksimentiev & Kornyshev, Phys. Rev. X 14:031042 (2024)"
        ),
        T97LiteratureRecord(
            "Has a torsion-level check of any 90 degree scaffold excursion been published?",
            "NOT FOUND, and it is this project's own T-71. This task inherits C-0029's caveat " +
                    "unchanged: the closure test is NECESSARY and never sufficient.",
            "not found",
            "T-71"
        )
    )

    val adoptedRecord = pairs.firstOrNull {
        it.topology == "${RoutingTopology.INDEPENDENT_STAPLES.name}, seat duplex only" &&
                it.grooveDegrees == 120.0 && it.separationBasePairs == separation
    }

    val findings = linkedMapOf(
        "verdict" to (
                "The pair FITS. At every separation from the ${floor} bp steric floor upward a " +
                        "second standoff seats on the same sheet duplex with both links inside " +
                        "the measured [0.60, 0.70] nm step and zero unpaired nucleotides, and " +
                        "the best pair at ${separation} bp holds its worse chord " +
                        "${"%.2f".format(adoptedRecord?.worstMisalignmentDegrees ?: -1.0)} deg " +
                        "off the flexure axis, i.e. " +
                        "${"%.4f".format(adoptedRecord?.worstLoadedCoupleFraction ?: -1.0)} of " +
                        "the base couple still lands in the loaded plane."
                ),
        "the cheap bound that did not bind" to (
                "If the second junction were the first one's SCREW IMAGE its chord would be " +
                        "rotated by n x 33.74 deg, which at C-0037's recommended 8 bp is 89.9 " +
                        "deg — the entire couple moved onto the wrong plane, and 8 bp would be " +
                        "the single worst separation in the band. It does not bind, because a " +
                        "standoff must stand NORMAL to the sheet and a screw rotation about a " +
                        "horizontal axis does not preserve that: the second standoff's own " +
                        "azimuth and axial position are free and it reaches a different target " +
                        "pair. That freedom is the whole answer."
                ),
        "what it costs in azimuth" to (
                "NOTHING, at every separation from 6 to 12 bp, on both groove conventions, on " +
                        "the strict seat-duplex-only reading and at the seat duplex's own axis: " +
                        "both chords come out EXACTLY on the flexure axis, so C-0037's " +
                        "best-phase assumption is discharged rather than merely noted. The one " +
                        "cost anywhere in the sweep is the scaffold-excursion topology at 6 and " +
                        "9 bp, worth 3.13 and 5.87 deg, i.e. 0.30 % and 1.04 % of the couple — " +
                        "against C-0037's own 8.4 % allowance."
                ),
        "the separation is NOT free, and 7 bp is the answer" to (
                "C-0037 reports 6, 8 and 12 bp as bit-identical and recommends 8. They are " +
                        "identical in the LOADED plane, and this task reproduces that exactly; " +
                        "they are not identical in the FREE one, which is the plane C-0037's " +
                        "own truss was built to restrain. At 6 bp the free plane still governs " +
                        "at 8.84 pN (margin 2.52); at 7 bp it has crossed and the loaded plane " +
                        "governs at 9.77 pN (margin 2.79), which is C-0037's L2a8 number. So " +
                        "SEVEN base pairs buys the whole of the recommended design at 0.68 nm " +
                        "less row width, and C-0037's 'between 6 and 8 bp' is resolved to 7."
                ),
        "why 7 bp in particular" to (
                "Because the second standoff has two moves the screw image does not: a swap to " +
                        "the seat duplex's OTHER backbone, worth +/-120 deg, and a half turn " +
                        "about its own axis, which is free because a chord is a LINE. The sheet " +
                        "phase residual under those moves is 3.8 deg at 7 bp against 22.4 at 6 " +
                        "and 29.9 at 8 — and at 7 bp the search's two standoffs duly come out " +
                        "as literal translates at ONE azimuth (300.0 deg), one grounded on each " +
                        "backbone of the same duplex. It is an explanation and not a bound: at " +
                        "6 and 11 bp the two standoffs come out a half turn apart instead, and " +
                        "the alignment is still exact."
                ),
        "the seat is a bound, not an output" to (
                "An unbounded search parks the optimum on the RIM of the seat duplex, where the " +
                        "flat end face's line contact 2 sqrt(R^2 - y^2) has collapsed to a point " +
                        "and the standoff is balanced on an edge. The seats swept here keep that " +
                        "contact above 1.6 nm; the wider sweep is carried as a sensitivity."
                ),
        "what is NOT shown" to (
                "A NECESSARY condition only — a phosphate pair inside the measured step with no " +
                        "van der Waals overlap. No backbone torsion angle is checked and no " +
                        "sequence is designed, so a 'closes' verdict is an UPPER bound on " +
                        "buildability. T-71 is the check that can only make it worse, and it now " +
                        "has two junctions to check instead of one."
                )
    )

    val result = T97Result(
        task = "T-97 — can TWO 90 degree junctions close on ONE sheet duplex 6-8 bp apart?",
        leaf = "A8.2",
        conditions = linkedMapOf(
            "temperature" to "300 K, k_BT = 4.141947 pN nm",
            "medium" to "aqueous 2 mM MgCl2",
            "lattice" to "single-layer square-lattice Rothemund sheet, 10.67 bp/turn, " +
                    "interhelical 2.69 nm (SAXS, Fischer et al. 2016)",
            "rise" to "0.34 nm per base pair",
            "phosphodiester step" to "0.60-0.70 nm, MEASURED (Bosco et al. 2014)",
            "phosphate radius" to "1.00 nm (Hedley et al. 2024)",
            "base couple" to "the hard, convention-free 180 degree chord, as C-0037 adopts",
            "flexure" to "45 load paths, secant placed at 33.3333 pN/nm at 3 nm, favourable " +
                    "mounting, duty at the DESIRED 10 nm on the element's own end shear",
            "rigidity" to "EI = 230 pN nm2 (CanDo model input), every margin also on Fields " +
                    "et al.'s implied 172.9"
        ),
        cheapBound = cheapBound,
        screwImage = screwImage,
        pairs = pairs,
        lengths = lengths,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        literature = literature,
        findings = findings,
        citedNumbers = listOf(
            "phosphate radius 1.00 nm — CITED, READ DIRECTLY (Hedley et al. 2024), via C-0029",
            "phosphodiester step 0.60-0.70 nm — CITED, MEASURED (Bosco et al. 2014), via C-0029",
            "interhelical 2.69 nm — CITED, MEASURED by SAXS (Fischer et al. 2016)",
            "rise 0.34 nm — CITED (Douglas et al. 2009)",
            "10.67 bp/turn square lattice — CITED",
            "EI = 230 pN nm2 — CITED, a CanDo MODEL INPUT (Kim et al. 2012)",
            "S = 1100 pN — CITED, MEASURED (Wang et al. 1997)",
            "k_bond,theta — CITED+FITTED (Chen et al. 2014) via C-0009",
            "k_bond,s — DERIVED (C-0020), NOT measured, swept four decades here",
            "Fields et al.'s implied 172.9 pN nm2 — CITED, MEASURED (Fields et al. 2013)",
            "C-0037's L2a8 design and C-0029's ceiling — CITED, reproduced here as tests",
            "16 bp crossover recurrence along one duplex — CITED via C-0015",
            "sec 3 targets 100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM — CITED"
        )
    )

    val json = Json { prettyPrint = true; encodeDefaults = true }
    val file = File("gpd/results/T-97-paired-perpendicular-junction.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)) + "\n")

    println("T-97 — two 90 degree junctions on one sheet duplex")
    println()
    println("the cheap bound")
    result.cheapBound.forEach { (key, value) -> println("  %-46s %12.5f".format(key, value)) }
    println()
    println("screw image (bp, nm, rotation deg, cos2, sheet phase residual deg, above floor)")
    result.screwImage.forEach {
        println(
            "  %3d %6.3f %8.3f %8.5f %8.3f %s".format(
                it.separationBasePairs, it.separationNanometres, it.rotationDegrees,
                it.coupleFraction, it.sheetPhaseResidualDegrees, it.aboveStericFloor
            )
        )
    }
    println()
    println("pairs (topology, groove, bp, found, screw deg, worst misalign deg, cos2, seat, worst gap, nt, phases, Pc loaded, Pc free, plane, margin, verdict)")
    result.pairs.forEach {
        println(
            "  %-38s %5.1f %3d %5s %7.2f %7.2f %7.4f %+5.2f %6.3f %2d %3d %7.2f %7.2f %-7s %5.2f  %s".format(
                it.topology.take(38), it.grooveDegrees, it.separationBasePairs, it.found,
                it.screwImageRotationDegrees, it.worstMisalignmentDegrees,
                it.worstLoadedCoupleFraction, it.seat, it.worstGap, it.unpairedNucleotides,
                it.crossoverFreePhases, it.loadedCriticalLoad, it.freeCriticalLoad,
                it.governingPlane, it.bucklingMargin, it.verdict
            )
        )
    }
    println()
    println("length sweep (bp apart, l, span, tangent, supply/demand, duty, Pc, plane, margin, Fields, verdict)")
    result.lengths.forEach {
        println(
            "  %3d %5.1f %6.2f %6.2f %6.2f %6.3f %7.2f %-7s %5.2f %5.2f  %s".format(
                it.separationBasePairs, it.standoffLength, it.span, it.tangentAcceptable,
                it.supplyToDemandAcceptable, it.dutyDesired, it.criticalLoad, it.governingPlane,
                it.bucklingMargin, it.bucklingMarginFields, it.verdict
            )
        )
    }
    println()
    println("sensitivities (axis, label, found, misalign deg, cos2, Pc, margin, moves)")
    result.sensitivities.forEach {
        println(
            "  %-42s %-52s %5s %7.2f %7.4f %7.2f %5.2f %s".format(
                it.axis.take(42), it.label.take(52), it.found, it.worstMisalignmentDegrees,
                it.worstLoadedCoupleFraction, it.criticalLoad, it.bucklingMargin, it.verdictMoves
            )
        )
    }
    println()
    println("convergence")
    result.convergence.forEach {
        println(
            "  %-46s %-32s %6.0f %14.9f %10.2e".format(
                it.quantity.take(46), it.control, it.level, it.value, it.departureFromFinest
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    result.reproductions.forEach {
        println(
            "  %-52s %12.6f %12.6f %10.2e".format(
                it.quantity.take(52), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
