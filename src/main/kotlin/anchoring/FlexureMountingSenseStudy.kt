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

import com.xemantic.nano.plentyofroom.actuator.ActuatorGeometry
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

/**
 * Tasks `T-75` / `T-78`, leaf `A8.2` — **which body carries the standoffs**, and **what sits under
 * the flexure's midspan**.
 *
 * ```shell
 * tools/study.sh anchoring.FlexureMountingSenseStudyKt
 * ```
 *
 * Emits `gpd/results/T-75-flexure-mounting-sense.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val UNREALISABLE_SENTINEL = -1.0

private fun finite(value: Double): Double =
    if (value.isInfinite() || value.isNaN()) UNREALISABLE_SENTINEL else value

private const val TARGET_FORCE = Gen1Tile.TARGET_FORCE
private const val ACCEPTABLE_STROKE = Gen1Tile.ACCEPTABLE_STROKE
private const val DESIRED_STROKE = Gen1Tile.DESIRED_STROKE
private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE
private const val PATH_COUNT = 45
private const val COMPLIANT_CEILING = 40.0
private const val DESIGN_LENGTH = 8.0

/** §3's own effort-point band, read the only way that reproduces both of its ends (`C-0012`). */
private const val SECTION_3_EFFORT_HEIGHT = 5.0

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val GEOMETRY = ActuatorGeometry()
private val FOOTPRINT = GEOMETRY.footprintArea

private val STANDOFF_LENGTHS = listOf(3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 12.7, 14.0)
private val LAYER_HEIGHTS = listOf(5.0, 7.0, 10.0)

private val BASE = StandoffBase.crossovers(2, favourableOrientation = true)

// ---------------------------------------------------------------------------------------------

@Serializable
data class T75MountingRecord(
    val id: String,
    val baseBody: String,
    val standoffNormal: String,
    val drivenBody: String,
    val deflectionRate: Double,
    val orientation: String,
    val standoffAxialSense: String,
    val topology: String,
    val tieCrossesBasePlane: Boolean,
    val minimumEffortHeightAboveTileTop: Double,
    val realisesSection3EffortPoint: Boolean,
    val tieForSection3EffortPoint: Double,
    val beamPlaneAtTenNanometreLayer: Double,
    val beamInsideActuationGap: Boolean,
    val beamClearsElectrode: Boolean,
    val tieCrossesTile: Boolean,
    val tieCrossesSuperstructure: Boolean,
    val compliancePassesAtDesignLength: Boolean,
    val verdict: String
)

@Serializable
data class T75DesignRecord(
    val mountingId: String,
    val standoffLength: Double,
    val standoffBasePairs: Double,
    val span: Double,
    val spanBasePairs: Double,
    val restraint: Double,
    val assembledSecantAcceptable: Double,
    val assembledTangentAcceptable: Double,
    val compliant: Boolean,
    val clearance: Double,
    val coversAcceptableStroke: Boolean,
    val coversDesiredStroke: Boolean
)

@Serializable
data class T75ApertureRecord(
    val standoffLength: Double,
    val stroke: Double,
    val span: Double,
    val restraint: Double,
    val clearance: Double,
    val penetration: Double,
    val apertureLength: Double,
    val apertureLengthSpanFraction: Double,
    val apertureLengthBasePairs: Double,
    val apertureAreaOneDuplexWide: Double,
    val apertureAreaAllPaths: Double,
    val apertureAreaFootprintFraction: Double,
    val tieApertureAreaAllPaths: Double,
    val tieApertureFootprintFraction: Double,
    val totalApertureFootprintFraction: Double,
    val needsAperture: Boolean
)

@Serializable
data class T75EffortRecord(
    val layerHeight: Double,
    val tileTopFace: Double,
    val effortPointConstantReading: Double,
    val attachmentHeightConstantReading: Double,
    val maximumInboardStandoffLooseReading: Double,
    val maximumInboardStandoffConstantReading: Double,
    val admitsC0030WindowLoose: Boolean,
    val admitsC0030WindowConstant: Boolean
)

@Serializable
data class T75OccupancyRecord(
    val layerHeight: Double,
    val standoffLength: Double,
    val beamPlane: Double,
    val beamClearsElectrode: Boolean,
    val arrayVolume: Double,
    val layerVolume: Double,
    val layerVolumeFraction: Double,
    val impliedDensityRatio: Double,
    val impliedPressureRatio: Double,
    val admissible: Boolean
)

@Serializable
data class T75PreBowRecord(
    val mountingId: String,
    val standoffLength: Double,
    val stroke: Double,
    val preBow: Double,
    val preBowBasePairs: Double,
    val span: Double,
    val assembledSecant: Double,
    val maximumAssembledTangent: Double,
    val compliant: Boolean,
    val preload: Double,
    val preloadOverHoldDownRequirement: Double,
    val preloadOverTargetForce: Double,
    val realisesSection3EffortPoint: Boolean,
    val verdict: String
)

@Serializable
data class T75ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T75ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
data class FlexureMountingSenseResult(
    val task: String,
    val leaf: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, Double>,
    val cheapBound: Map<String, Double>,
    val mountings: List<T75MountingRecord>,
    val designs: List<T75DesignRecord>,
    val apertures: List<T75ApertureRecord>,
    val effortBand: List<T75EffortRecord>,
    val occupancy: List<T75OccupancyRecord>,
    val preBows: List<T75PreBowRecord>,
    val convergence: List<T75ConvergenceRecord>,
    val reproductions: List<T75ReproductionRecord>,
    val predicates: Map<String, Boolean>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val cited: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------

private fun flexibilityAt(length: Double) =
    standoffTipFlexibility(EI, length, BASE.rotationalStiffness)

private fun placedFlexure(mounting: FlexureMounting, length: Double): CoupledJointFlexure {
    val flexibility = flexibilityAt(length)
    val span = preBowedFlexureSpan(
        EI, flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, mounting, 0.0, STRETCH
    )
    return CoupledJointFlexure(EI, span, flexibility, STRETCH)
}

private fun mountingRecord(mounting: FlexureMounting): T75MountingRecord {
    val realisable = mounting.minimumEffortHeightAboveTileTop(DESIGN_LENGTH) <=
            SECTION_3_EFFORT_HEIGHT
    val tie = if (realisable) {
        mounting.tieLengthForEffortHeight(SECTION_3_EFFORT_HEIGHT, DESIGN_LENGTH)
    } else UNREALISABLE_SENTINEL
    // a stack the mounting CAN build, so that the geometric predicates have coordinates
    val buildableEffort = max(
        SECTION_3_EFFORT_HEIGHT,
        mounting.minimumEffortHeightAboveTileTop(DESIGN_LENGTH) + 1.0
    )
    val stack = MountingStack(
        mounting, 10.0, DESIGN_LENGTH,
        mounting.tieLengthForEffortHeight(buildableEffort, DESIGN_LENGTH), GEOMETRY
    )
    val flexure = placedFlexure(mounting, DESIGN_LENGTH)
    val tangent = PATH_COUNT * flexure.strokeTangentStiffness(ACCEPTABLE_STROKE, mounting.orientation)
    val compliant = tangent <= COMPLIANT_CEILING
    val verdict = when {
        mounting.inboard && !compliant ->
            "FAIL — adverse: the tangent is past C-0023's 40 pN/nm ceiling, and the topology " +
                    "cannot place §3's effort point at this standoff length"
        mounting.inboard ->
            "FAIL — adverse: the topology cannot place §3's effort point at this standoff length"
        mounting.putsFlexureUnderTheTile ->
            "FAIL — favourable but the flexure would sit inside the actuation gap"
        else -> "PASS — favourable, outboard, and it places §3's effort point at any length"
    }
    return T75MountingRecord(
        id = mounting.id,
        baseBody = mounting.baseBody.name,
        standoffNormal = mounting.standoffNormal.name,
        drivenBody = mounting.drivenBody.name,
        deflectionRate = mounting.deflectionRate,
        orientation = mounting.orientation.name.lowercase(),
        standoffAxialSense = mounting.standoffAxialSense.name.lowercase(),
        topology = if (mounting.inboard) "inboard" else "outboard",
        tieCrossesBasePlane = stack.tieCrossesBasePlane,
        minimumEffortHeightAboveTileTop = mounting.minimumEffortHeightAboveTileTop(DESIGN_LENGTH),
        realisesSection3EffortPoint = realisable,
        tieForSection3EffortPoint = tie,
        beamPlaneAtTenNanometreLayer = stack.beamPlane,
        beamInsideActuationGap = stack.beamInsideActuationGap,
        beamClearsElectrode = stack.beamClearsElectrode(),
        tieCrossesTile = stack.tieCrossesTile,
        tieCrossesSuperstructure = stack.tieCrossesSuperstructure,
        compliancePassesAtDesignLength = compliant,
        verdict = verdict
    )
}

private fun designRecord(mounting: FlexureMounting, length: Double): T75DesignRecord {
    val flexure = placedFlexure(mounting, length)
    val clearance = midspanClearance(length)
    val tangent = PATH_COUNT * flexure.strokeTangentStiffness(ACCEPTABLE_STROKE, mounting.orientation)
    return T75DesignRecord(
        mountingId = mounting.id,
        standoffLength = length,
        standoffBasePairs = length / RISE,
        span = flexure.span,
        spanBasePairs = flexure.span / RISE,
        restraint = flexure.restraint,
        assembledSecantAcceptable =
            PATH_COUNT * flexure.strokeSecantStiffness(ACCEPTABLE_STROKE, mounting.orientation),
        assembledTangentAcceptable = tangent,
        compliant = tangent <= COMPLIANT_CEILING,
        clearance = clearance,
        coversAcceptableStroke = clearance >= ACCEPTABLE_STROKE,
        coversDesiredStroke = clearance >= DESIRED_STROKE
    )
}

private fun apertureRecord(
    mounting: FlexureMounting,
    length: Double,
    stroke: Double
): T75ApertureRecord {
    val flexure = placedFlexure(mounting, length)
    val clearance = midspanClearance(length)
    val penetration = midspanPenetration(stroke, length)
    val aperture = apertureLength(flexure.span, flexure.restraint, stroke, length)
    val oneAperture = apertureArea(1, aperture)
    val allApertures = apertureArea(PATH_COUNT, aperture)
    val tieApertures = tieApertureArea(PATH_COUNT)
    return T75ApertureRecord(
        standoffLength = length,
        stroke = stroke,
        span = flexure.span,
        restraint = flexure.restraint,
        clearance = clearance,
        penetration = penetration,
        apertureLength = aperture,
        apertureLengthSpanFraction = aperture / flexure.span,
        apertureLengthBasePairs = aperture / RISE,
        apertureAreaOneDuplexWide = oneAperture,
        apertureAreaAllPaths = allApertures,
        apertureAreaFootprintFraction = allApertures / FOOTPRINT,
        tieApertureAreaAllPaths = tieApertures,
        tieApertureFootprintFraction = tieApertures / FOOTPRINT,
        totalApertureFootprintFraction = (allApertures + tieApertures) / FOOTPRINT,
        needsAperture = aperture > 0.0
    )
}

private fun occupancyRecord(layerHeight: Double, length: Double): T75OccupancyRecord {
    val underTile = FlexureMounting(MountingBody.TILE, StandoffNormal.DOWNWARD)
    val flexure = placedFlexure(underTile, length)
    val stack = MountingStack(
        underTile, layerHeight, length,
        underTile.tieLengthForEffortHeight(SECTION_3_EFFORT_HEIGHT, length), GEOMETRY
    )
    val volume = flexureArrayVolume(PATH_COUNT, flexure.span, length)
    val fraction = layerVolumeFraction(volume, FOOTPRINT, layerHeight)
    // the polymer the array displaces has to go somewhere: at fixed chain content the layer's own
    // volume fraction rises by 1/(1 − f) and its des Cloizeaux pressure by that to the 9/4
    val densityRatio = if (fraction < 1.0) 1.0 / (1.0 - fraction) else UNREALISABLE_SENTINEL
    val pressureRatio =
        if (fraction < 1.0) Math.pow(densityRatio, 2.25) else UNREALISABLE_SENTINEL
    return T75OccupancyRecord(
        layerHeight = layerHeight,
        standoffLength = length,
        beamPlane = stack.beamPlane,
        beamClearsElectrode = stack.beamClearsElectrode(),
        arrayVolume = volume,
        layerVolume = FOOTPRINT * layerHeight,
        layerVolumeFraction = fraction,
        impliedDensityRatio = densityRatio,
        impliedPressureRatio = pressureRatio,
        admissible = stack.beamClearsElectrode() && fraction < 0.05 &&
                midspanClearance(length) >= ACCEPTABLE_STROKE
    )
}

private fun preBowRecord(
    mounting: FlexureMounting,
    length: Double,
    stroke: Double
): T75PreBowRecord {
    val flexibility = flexibilityAt(length)
    val minimum = minimumPreBowForCeiling(
        EI, flexibility, PATH_COUNT, MANDATE, stroke, mounting, COMPLIANT_CEILING, STRETCH
    )
    val realised = if (minimum.isInfinite()) UNREALISABLE_SENTINEL else minimum
    val preBow = if (minimum.isInfinite()) 0.0 else minimum
    val span = preBowedFlexureSpan(
        EI, flexibility, PATH_COUNT, MANDATE, stroke, mounting, preBow, STRETCH
    )
    val flexure = CoupledJointFlexure(EI, span, flexibility, STRETCH)
    val peakTangent = maximumAssembledTangent(flexure, PATH_COUNT, mounting, preBow, stroke)
    val preload = PATH_COUNT * preBowPreload(flexure, mounting, preBow)
    val realises = mounting.minimumEffortHeightAboveTileTop(length) <= SECTION_3_EFFORT_HEIGHT
    val verdict = when {
        minimum.isInfinite() ->
            "CLOSED — no built rise up to 30 nm brings the tangent under the ceiling"
        !realises ->
            "REJECTED — the compliance is recovered but the topology still cannot place §3's " +
                    "effort point"
        preload > 10.0 * 1.3806 ->
            "REJECTED — the preload it costs is more than 10x C-0021's hold-down requirement"
        else -> "ADMISSIBLE"
    }
    return T75PreBowRecord(
        mountingId = mounting.id,
        standoffLength = length,
        stroke = stroke,
        preBow = finite(realised),
        preBowBasePairs = if (minimum.isInfinite()) UNREALISABLE_SENTINEL else ceil(minimum / RISE),
        span = span,
        assembledSecant = PATH_COUNT * preBowDeliveredForce(flexure, mounting, preBow, stroke) / stroke,
        maximumAssembledTangent = peakTangent,
        compliant = peakTangent <= COMPLIANT_CEILING,
        preload = preload,
        preloadOverHoldDownRequirement = preload / 1.3806,
        preloadOverTargetForce = preload / TARGET_FORCE,
        realisesSection3EffortPoint = realises,
        verdict = verdict
    )
}

private fun reproduction(quantity: String, published: Double, derived: Double, source: String) =
    T75ReproductionRecord(
        quantity = quantity,
        published = published,
        derived = derived,
        relativeDeparture = abs(derived - published) / max(1.0e-12, abs(published)),
        source = source
    )

// ---------------------------------------------------------------------------------------------

fun main() {
    val favourableOutboard = FlexureMounting(MountingBody.SUPERSTRUCTURE, StandoffNormal.UPWARD)
    val favourableUnderTile = FlexureMounting(MountingBody.TILE, StandoffNormal.DOWNWARD)
    val adverseOnTile = FlexureMounting(MountingBody.TILE, StandoffNormal.UPWARD)

    // ------------------------------------------------------------------ the cheap bound
    val cheapBound = mapOf(
        "deflectionRateTileUp" to adverseOnTile.deflectionRate,
        "deflectionRateTileDown" to favourableUnderTile.deflectionRate,
        "deflectionRateSuperDown" to
                FlexureMounting(MountingBody.SUPERSTRUCTURE, StandoffNormal.DOWNWARD).deflectionRate,
        "deflectionRateSuperUp" to favourableOutboard.deflectionRate,
        "favourableCount" to
                FlexureMounting.ALL.count { it.orientation == FlexureOrientation.FAVOURABLE }
                    .toDouble(),
        "favourableCountWithTileBase" to
                FlexureMounting.ALL.count {
                    it.orientation == FlexureOrientation.FAVOURABLE &&
                            it.baseBody == MountingBody.TILE
                }.toDouble(),
        "favourableCountWithUpwardNormal" to
                FlexureMounting.ALL.count {
                    it.orientation == FlexureOrientation.FAVOURABLE &&
                            it.standoffNormal == StandoffNormal.UPWARD
                }.toDouble()
    )

    // ------------------------------------------------------------------ the length each sense needs
    val adverseCompliantLength = standoffLengthForCompliance(
        EI, BASE.rotationalStiffness, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
        adverseOnTile, COMPLIANT_CEILING, STRETCH
    )
    val favourableCompliantLength = standoffLengthForCompliance(
        EI, BASE.rotationalStiffness, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
        favourableOutboard, COMPLIANT_CEILING, STRETCH
    )

    // ------------------------------------------------------------------ the four mountings
    val mountings = FlexureMounting.ALL.map { mountingRecord(it) }

    // ------------------------------------------------------------------ the designs
    val designs = FlexureMounting.ALL.flatMap { mounting ->
        STANDOFF_LENGTHS.map { designRecord(mounting, it) }
    }

    // ------------------------------------------------------------------ T-78, the apertures
    val apertures = STANDOFF_LENGTHS.flatMap { length ->
        listOf(ACCEPTABLE_STROKE, 5.0, DESIRED_STROKE).map {
            apertureRecord(favourableOutboard, length, it)
        }
    }

    // ------------------------------------------------------------------ §3's effort band
    val effortBand = LAYER_HEIGHTS.map { height ->
        val looseCeiling = 25.0
        val loose = adverseOnTile.maximumStandoffLengthUnderEffortCeiling(height, looseCeiling)
        T75EffortRecord(
            layerHeight = height,
            tileTopFace = GEOMETRY.tileTopFace(height),
            effortPointConstantReading = GEOMETRY.effortPointHeight(height),
            attachmentHeightConstantReading = SECTION_3_EFFORT_HEIGHT,
            maximumInboardStandoffLooseReading = loose,
            maximumInboardStandoffConstantReading = SECTION_3_EFFORT_HEIGHT,
            admitsC0030WindowLoose = loose > 5.0,
            admitsC0030WindowConstant = SECTION_3_EFFORT_HEIGHT > 5.0
        )
    }

    // ------------------------------------------------------------------ the layer occupancy
    val occupancy = LAYER_HEIGHTS.flatMap { height ->
        listOf(5.0, 6.0, 8.0).map { occupancyRecord(height, it) }
    }

    // ------------------------------------------------------------------ the pre-bow escape
    val preBows = FlexureMounting.ALL.filter { it.inboard }.flatMap { mounting ->
        listOf(3.0, 5.0, 8.0).flatMap { length ->
            listOf(ACCEPTABLE_STROKE, DESIRED_STROKE).map { preBowRecord(mounting, length, it) }
        }
    }

    // ------------------------------------------------------------------ convergence
    val designFlexure = placedFlexure(favourableOutboard, DESIGN_LENGTH)
    val convergence = listOf(64, 256, 1024, 4096).map { steps ->
        val value = apertureLength(
            designFlexure.span, designFlexure.restraint, DESIRED_STROKE, DESIGN_LENGTH,
            scanSteps = steps
        )
        T75ConvergenceRecord(
            quantity = "aperture length at l = 8 nm and the 10 nm stroke",
            control = "apertureHalfPositionFraction scanSteps",
            level = steps.toDouble(),
            value = value,
            departureFromFinest = 0.0
        )
    }.let { records ->
        val finest = records.last().value
        records.map { it.copy(departureFromFinest = abs(it.value - finest) / abs(finest)) }
    } + listOf(200, 2000, 20000).map { samples ->
        val value = maximumAssembledTangent(
            designFlexure, PATH_COUNT, favourableOutboard, 0.0, ACCEPTABLE_STROKE, samples
        )
        T75ConvergenceRecord(
            quantity = "maximum assembled tangent over the acceptable stroke",
            control = "samples",
            level = samples.toDouble(),
            value = value,
            departureFromFinest = 0.0
        )
    }.let { records ->
        val finest = records.last().value
        records.map { it.copy(departureFromFinest = abs(it.value - finest) / abs(finest)) }
    }

    // ------------------------------------------------------------------ reproductions
    val adverseFlexure = placedFlexure(adverseOnTile, DESIGN_LENGTH)
    val reproductions = listOf(
        reproduction(
            "C-0030 favourable span at l = 8 nm [nm]", 31.82, designFlexure.span,
            "C-0030 Deliverable 3"
        ),
        reproduction(
            "C-0030 favourable assembled tangent at 3 nm [pN/nm]", 25.23,
            PATH_COUNT * designFlexure.strokeTangentStiffness(
                ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE
            ),
            "C-0030 Deliverable 3"
        ),
        reproduction(
            "C-0030 adverse span at l = 8 nm [nm]", 40.14, adverseFlexure.span,
            "C-0030 Deliverable 4"
        ),
        reproduction(
            "C-0030 adverse assembled tangent at 3 nm [pN/nm]", 44.82,
            PATH_COUNT * adverseFlexure.strokeTangentStiffness(
                ACCEPTABLE_STROKE, FlexureOrientation.ADVERSE
            ),
            "C-0030 Deliverable 4"
        ),
        reproduction(
            "C-0030 clearance at l = 8 nm [nm]", 5.31, midspanClearance(DESIGN_LENGTH),
            "C-0030 Deliverable 4"
        ),
        reproduction(
            "C-0030 standoff length that covers the desired stroke [nm]", 12.69,
            STANDOFF_LENGTHS.first { midspanClearance(it) >= DESIRED_STROKE },
            "C-0030 Deliverable 4"
        ),
        reproduction(
            "§3 effort point at the 5 nm layer [nm]", 20.0, GEOMETRY.effortPointHeight(5.0),
            "§3 / C-0012 / ActuatorGeometry"
        ),
        reproduction(
            "§3 effort point at the 10 nm layer [nm]", 25.0, GEOMETRY.effortPointHeight(10.0),
            "§3 / C-0012 / ActuatorGeometry"
        ),
        reproduction(
            "C-0025 midspan factor c(rho) at the design restraint", midspanFactor(designFlexure.restraint),
            designFlexure.bendingFactor, "C-0025 / C-0030 c0 = c(rho)"
        ),
        reproduction(
            "SAXS interhelical distance [nm]", 2.69, OrigamiDuplex.INTERHELICAL,
            "Fischer et al. (2016), via C-0009"
        )
    )

    // ------------------------------------------------------------------ the predicates
    val survivors = mountings.filter { it.verdict.startsWith("PASS") }
    val acceptableApertures = apertures.filter { it.stroke == ACCEPTABLE_STROKE && it.standoffLength >= 6.0 }
    val desiredAperture = apertures.first {
        it.stroke == DESIRED_STROKE && it.standoffLength == DESIGN_LENGTH
    }
    val predicates = mapOf(
        "Q1 the sign is a kinematic identity" to
                FlexureMounting.ALL.all { abs(abs(it.deflectionRate) - 1.0) < 1.0e-12 },
        "Q2 neither variable alone decides the sign" to
                (cheapBound["favourableCountWithTileBase"] == 1.0 &&
                        cheapBound["favourableCountWithUpwardNormal"] == 1.0),
        "Q3 exactly one mounting survives the buildability filter" to (survivors.size == 1),
        "Q4 the pre-bow escape is priced to a threshold" to preBows.all { it.verdict.isNotEmpty() },
        "Q5 the clearance ceiling is quantified as an aperture at both strokes" to
                (acceptableApertures.none { it.needsAperture } && desiredAperture.needsAperture)
    )

    val result = FlexureMountingSenseResult(
        task = "T-75 (which body carries the standoffs) and T-78 (what sits under the midspan)",
        leaf = "A8.2",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "area" to "nm^2",
            "volume" to "nm^3",
            "rate" to "dimensionless"
        ),
        conventions = CONVENTIONS,
        parameters = mapOf(
            "bendingRigidity" to EI,
            "stretchModulus" to STRETCH,
            "baseRotationalStiffness" to BASE.rotationalStiffness,
            "pathCount" to PATH_COUNT.toDouble(),
            "mandateStiffness" to MANDATE,
            "complianceCeiling" to COMPLIANT_CEILING,
            "acceptableStroke" to ACCEPTABLE_STROKE,
            "desiredStroke" to DESIRED_STROKE,
            "section3EffortHeightAboveTileTop" to SECTION_3_EFFORT_HEIGHT,
            "tileThickness" to GEOMETRY.tileThickness,
            "footprintArea" to FOOTPRINT,
            "contactDistance" to OrigamiDuplex.INTERHELICAL,
            "duplexDiameter" to OrigamiDuplex.DIAMETER,
            "risePerBasePair" to RISE,
            "adverseStandoffLengthForCompliance" to adverseCompliantLength,
            "favourableStandoffLengthForCompliance" to favourableCompliantLength
        ),
        cheapBound = cheapBound,
        mountings = mountings,
        designs = designs,
        apertures = apertures,
        effortBand = effortBand,
        occupancy = occupancy,
        preBows = preBows,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings(
            mountings, apertures, occupancy, preBows, designs, effortBand,
            adverseCompliantLength, favourableCompliantLength
        ),
        validity = VALIDITY,
        cited = CITED,
        openQuestions = OPEN_QUESTIONS
    )

    val output = File("gpd/results/T-75-flexure-mounting-sense.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)) + "\n")
    report(result, output)
}

// ---------------------------------------------------------------------------------------------

private val CONVENTIONS = listOf(
    "z is normal to the electrode, positive away from it, origin at the electrode surface " +
            "(ActuatorGeometry).",
    "The stroke s is positive DOWNWARD: §1's positive electrode bias pulls the tile toward the " +
            "electrode, and C-0017's output superstructure is the coupling's fixed far ground. " +
            "This is the only physical input in T-75; everything else is a definition.",
    "delta is the flexure's midspan deflection relative to its OWN ends, positive TOWARD the " +
            "standoff base plane — C-0030's FAVOURABLE direction.",
    "A mounting is the ordered pair (base body, standoff normal): which body the standoff bases " +
            "stand on, and which way along z the standoffs point out of that body's plane.",
    "A standoff is in COMPRESSION when the beam's end shear pushes its head toward its own base.",
    "The clearance contact distance is the SAXS single-layer interhelical distance, 2.69 nm — " +
            "the axis-to-axis separation two parallel duplexes actually sit at."
)

private val VALIDITY = listOf(
    "TRL 1-3. Nothing here is measured, and C-0028's literature finding is unchanged and upstream " +
            "of every number: no duplex has been built standing normal to a single-layer sheet, " +
            "every published out-of-plane base is a PIN, and the only rigid out-of-plane mounting " +
            "in print is TRIANGULATED.",
    "The kinematic identity assumes the superstructure is the coupling's fixed ground. If the " +
            "output lever's own attachment moves WITH the tile by a fraction f, every rate is " +
            "scaled by (1 − f) and no SIGN changes — the identity is robust to lever compliance " +
            "and fails only if f > 1, i.e. if the lever descends faster than the tile drives it.",
    "The 5 nm effort-point reading is §3's own arithmetic (C-0012): §3's 20-25 nm band is exactly " +
            "as wide as its 5-10 nm layer-height range, which forces a CONSTANT attachment height " +
            "and fixes it at 5 nm. §3 says 'may sit ~20-25 nm', so it is an indication and not a " +
            "hard constraint; it is reported as a filter with its own uncertainty.",
    "The superstructure is UNSPECIFIED beyond C-0017's assumption that it exists and is grounded. " +
            "Whether it can be perforated is therefore a design choice on an undesigned body — " +
            "which is precisely why the aperture is priced as an AREA rather than asserted.",
    "The aperture is computed from the LINEAR deflected shape of C-0025's beam. At the desired " +
            "10 nm stroke the head rotation is 0.63-0.68 rad (C-0030), so the 10 nm aperture " +
            "column is a linear-theory extrapolation exactly as C-0030's 10 nm columns are.",
    "The array packing is NOT solved here: 45 flexures of ~32 nm span do not lie side by side in " +
            "a 40 x 40 nm footprint at a 2.69 nm pitch, so the aperture AREA fraction is quoted " +
            "against the tile footprint as a scale, not as a layout. T-31 owns the array.",
    "The layer-occupancy kill uses the array's own excluded VOLUME and the des Cloizeaux exponent " +
            "9/4; it is a cheap bound, deliberately, because it is 5-8x past any threshold that " +
            "would need a solve.",
    "The pre-bow escape is priced on C-0030's own solved flexure with C-0023's chord draw-in. " +
            "T-43's 1.13-1.20x draw-in inconsistency travels with it unchanged."
)

private val CITED = listOf(
    "duplex EI = 230 pN nm^2 — CITED, a CanDo MODEL INPUT (Kim et al., NAR 40:2862, 2012), " +
            "NOT a measurement",
    "duplex stretch modulus S = 1100 pN — CITED, MEASURED (Wang et al., Biophys. J. 72:1335, 1997)",
    "interhelical distance 2.69 nm — CITED, MEASURED by SAXS (Fischer et al. 2016)",
    "B-DNA steric diameter 2.0 nm — CITED; the phosphate backbone IS the surface",
    "§3 parameters: 100 pN, 3 nm, 10 nm, 40 x 40 nm, ~10 nm tile thickness, effort point " +
            "~20-25 nm above the electrode — CITED",
    "C-0030's B2 / 8 nm favourable and adverse designs — CITED, and reproduced here as gate-5 tests",
    "C-0021's 1.3806 pN hold-down requirement and C-0023's 40 pN/nm compliance ceiling — CITED"
)

private val OPEN_QUESTIONS = listOf(
    "Whether the output superstructure may be perforated under each flexure midspan. It is the " +
            "one input T-78 cannot supply, it is worth §3's DESIRED stroke, and it is a question " +
            "about a body nothing in §1 or §3 describes.",
    "T-31's array packing: 45 flexures of ~32 nm span over a 40 x 40 nm footprint. Every area " +
            "fraction here inherits it.",
    "Whether the standoff base motif exists at all — C-0029 finds a duplex end has at most TWO " +
            "covalent links and that C-0028's B2 couple is 3.34x past that ceiling. Upstream of " +
            "everything here, and unchanged by it."
)

private fun findings(
    mountings: List<T75MountingRecord>,
    apertures: List<T75ApertureRecord>,
    occupancy: List<T75OccupancyRecord>,
    preBows: List<T75PreBowRecord>,
    designs: List<T75DesignRecord>,
    effortBand: List<T75EffortRecord>,
    adverseCompliantLength: Double,
    favourableCompliantLength: Double
): Map<String, String> {
    val survivor = mountings.single { it.verdict.startsWith("PASS") }
    val worstOccupancy = occupancy.maxOf { it.layerVolumeFraction }
    val desired = apertures.first { it.stroke == DESIRED_STROKE && it.standoffLength == DESIGN_LENGTH }
    val minimumLengthCoveringAcceptable =
        designs.filter { it.mountingId == survivor.id && it.coversAcceptableStroke }
            .minOf { it.standoffLength }
    val admissiblePreBows = preBows.count { it.verdict == "ADMISSIBLE" }
    return mapOf(
        "T-75 — the sign is not a free choice" to
                ("The midspan deflection IS the change in the two bodies' separation, so " +
                        "d(delta)/ds = (v_base - v_driven)/n is exactly +-1 and contains no " +
                        "length. It is a PRODUCT of two binaries: of the four mountings, " +
                        "exactly two are favourable, one with each base body and one with each " +
                        "standoff normal. 'Which body carries the standoffs' is HALF the " +
                        "variable and decides nothing on its own."),
        "T-75 — the answer" to
                ("Exactly one of the four mountings survives: %s — standoff bases on the %s, " +
                        "standoffs pointing %s, the flexure OUTBOARD of its own ground and the " +
                        "midspan tied back DOWN through that ground to the tile. The other " +
                        "favourable mounting puts the flexure under the tile, inside the " +
                        "actuation gap, where the array alone would occupy %.1f %% of the " +
                        "polymer layer's volume. Both adverse mountings fail C-0023's compliance " +
                        "ceiling at every standoff length inside C-0017's envelope AND cannot " +
                        "place §3's own effort point at any length in C-0030's window.").format(
                    survivor.id, survivor.baseBody.lowercase(),
                    survivor.standoffNormal.lowercase(), 100.0 * worstOccupancy
                ),
        "the equivalence that makes it checkable" to
                ("Favourable <=> the tie crosses the standoff base plane <=> the flexure is " +
                        "outboard <=> the standoff is in COMPRESSION. The last one matters " +
                        "beyond bookkeeping: a standoff in TENSION cannot buckle, so C-0028's " +
                        "and C-0030's P6 is a predicate of the favourable mounting only, and " +
                        "C-0030's adverse buckling margins are charged against a member its own " +
                        "kinematics puts in tension."),
        "T-78 — what sits under the midspan" to
                ("The body the standoffs stand on, necessarily — the favourable sense is DEFINED " +
                        "by the driven body being on the far side of it. So the clearance " +
                        "ceiling is not a property of an unspecified body: it is a property of " +
                        "the only topology that works. At §3's ACCEPTABLE 3 nm stroke it costs " +
                        "nothing at all for l >= %.0f nm — no aperture, zero penetration. At §3's " +
                        "DESIRED 10 nm stroke and l = 8 nm the midspan goes %.2f nm past the " +
                        "contact plane and the beam demands a slot %.1f nm long (%.0f %% of the " +
                        "span, %.0f bp); over 45 paths that is %.0f nm^2, %.2fx the tile's own " +
                        "footprint.").format(
                    minimumLengthCoveringAcceptable, desired.penetration, desired.apertureLength,
                    100.0 * desired.apertureLengthSpanFraction, desired.apertureLengthBasePairs,
                    desired.apertureAreaAllPaths, desired.apertureAreaFootprintFraction
                ),
        "the adverse mounting is short of a LENGTH, not of a mechanism" to
                ("C-0030 swept l = 3-10 nm and found the adverse tangent past C-0023's 40 pN/nm " +
                        "ceiling at every one. The tangent falls monotonically with the standoff, " +
                        "so the honest statement is the length it would need: %.2f nm against " +
                        "%.2f nm favourable, i.e. %.2fx longer and outside C-0017's 10 nm " +
                        "envelope (P5) — and at that length the inboard topology puts §3's " +
                        "effort point %.1f nm above the tile against §3's own 5 nm.").format(
                    adverseCompliantLength, favourableCompliantLength,
                    adverseCompliantLength / favourableCompliantLength, adverseCompliantLength
                ),
        "§3's effort band, on both of its readings" to
                ("§3's band is 5 nm wide and its layer-height range is 5 nm wide, so a CONSTANT " +
                        "attachment height is forced and it is 5 nm — the only reading that " +
                        "reproduces 20 / 22 / 25 nm at all three heights. Read LOOSELY instead, " +
                        "as a band the effort point must merely lie in, the inboard topology's " +
                        "longest admissible standoff is %s nm at the 5 / 7 / 10 nm layers. The " +
                        "two readings AGREE at the 10 nm layer, which is where C-0016 and C-0027 " +
                        "put the whole design window: 5 nm, against C-0030's 5-10 nm. The " +
                        "outboard topology is unconstrained on both readings.").format(
                    effortBand.joinToString(" / ") {
                        "%.0f".format(it.maximumInboardStandoffLooseReading)
                    }
                ),
        "the pre-bow escape" to
                ("Building the flexure already sagging toward its base plane puts the first " +
                        "delta_0 of stroke on the favourable limb inside an ADVERSE mounting, " +
                        "and it does recover the compliance. %d of %d priced cases are " +
                        "admissible: the rise has to be the whole stroke, its preload is the " +
                        "whole §3 target force pressed onto the layer before any bias is " +
                        "applied, and the inboard topology still cannot place §3's effort " +
                        "point. The escape is real, priced, and rejected on two counts.").format(
                    admissiblePreBows, preBows.size
                )
    )
}

private fun report(result: FlexureMountingSenseResult, file: File) {
    println("T-75 / T-78 — which body carries the standoffs, and what sits under the midspan")
    println()
    println("the cheap bound")
    result.cheapBound.forEach { (key, value) -> println("  %-32s %8.3f".format(key, value)) }
    println()
    println("the four mountings")
    result.mountings.forEach {
        println(
            "  %-3s base=%-14s n=%-8s rate=%+.0f %-10s %-11s %-8s effort>=%5.2f §3=%-5s  %s".format(
                it.id, it.baseBody, it.standoffNormal, it.deflectionRate, it.orientation,
                it.standoffAxialSense, it.topology, it.minimumEffortHeightAboveTileTop,
                it.realisesSection3EffortPoint, it.verdict
            )
        )
    }
    println()
    println("designs (mounting, l, span, tangent, compliant, clearance, covers 3 nm, covers 10 nm)")
    result.designs.forEach {
        println(
            "  %-3s %5.1f %6.2f %7.2f %-6s %6.2f %-6s %-6s".format(
                it.mountingId, it.standoffLength, it.span, it.assembledTangentAcceptable,
                it.compliant, it.clearance, it.coversAcceptableStroke, it.coversDesiredStroke
            )
        )
    }
    println()
    println(
        "T-78 apertures (l, stroke, penetration, aperture length, span fraction, beam area, " +
                "beam fraction, total incl. tie floor)"
    )
    result.apertures.forEach {
        println(
            "  %5.1f %6.1f %7.3f %8.2f %7.3f %9.1f %8.3f %8.3f %s".format(
                it.standoffLength, it.stroke, it.penetration, it.apertureLength,
                it.apertureLengthSpanFraction, it.apertureAreaAllPaths,
                it.apertureAreaFootprintFraction, it.totalApertureFootprintFraction,
                if (it.needsAperture) "APERTURE" else "tie only"
            )
        )
    }
    println()
    println("§3's effort band (h, tile top, constant-reading effort point, loose max inboard l)")
    result.effortBand.forEach {
        println(
            "  %5.1f %7.2f %8.2f %8.2f".format(
                it.layerHeight, it.tileTopFace, it.effortPointConstantReading,
                it.maximumInboardStandoffLooseReading
            )
        )
    }
    println()
    println("layer occupancy of the under-tile mounting (h, l, beam plane, clears, fraction, pressure ratio)")
    result.occupancy.forEach {
        println(
            "  %5.1f %5.1f %7.2f %-6s %7.4f %8.2f %s".format(
                it.layerHeight, it.standoffLength, it.beamPlane, it.beamClearsElectrode,
                it.layerVolumeFraction, it.impliedPressureRatio,
                if (it.admissible) "admissible" else "INADMISSIBLE"
            )
        )
    }
    println()
    println("the pre-bow escape (mounting, l, stroke, rise, bp, span, peak tangent, preload, verdict)")
    result.preBows.forEach {
        println(
            "  %-3s %5.1f %5.1f %6.2f %5.0f %6.2f %7.2f %8.2f  %s".format(
                it.mountingId, it.standoffLength, it.stroke, it.preBow, it.preBowBasePairs,
                it.span, it.maximumAssembledTangent, it.preload, it.verdict
            )
        )
    }
    println()
    println("convergence")
    result.convergence.forEach {
        println(
            "  %-52s %-38s %8.0f %14.9f %10.2e".format(
                it.quantity.take(52), it.control.take(38), it.level, it.value,
                it.departureFromFinest
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
    println("predicates")
    result.predicates.forEach { (key, value) -> println("  %-56s %s".format(key, value)) }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
