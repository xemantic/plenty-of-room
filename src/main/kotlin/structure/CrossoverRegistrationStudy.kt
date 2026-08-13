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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

/**
 * Task `T-14` / leaf `A8.2` — the crossover column **phase** and the attachment
 * **registration** as the free staple-layout design variables they are.
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.CrossoverRegistrationStudyKt
 * ```
 *
 * Emits `gpd/results/T-14-crossover-phase-and-registration.json`, deterministically.
 */

// --------------------------------------------------------------------------- records

/** Where one peak per-load-path force sits against the per-path allowables. */
@Serializable
data class AllowableMargin(
    val peakForce: Double,
    val unzipLowerMargin: Double,
    val unzipUpperMargin: Double,
    val shearMargin: Double,
    val ceilingMargin: Double,
    val verdict: String
)

/**
 * One base-pair phase of the crossover column lattice, as **arithmetic alone** —
 * the cheap bound, computed before any lattice is assembled.
 */
@Serializable
data class PhaseGeometry(
    val basePairs: Int,
    val phase: Double,
    val columns: Int,
    val crossovers: Int,
    val evenParityColumns: Int,
    val oddParityColumns: Int,
    val nearestColumnToEdge: Double,
    val crossoverCountRatioToContinuum: Double,
    val acrossHelixRigidityChange: Double,
    val centroSymmetric: Boolean,
    val symmetryGroup: String
)

/** One attachment position inside the one-crossover unit cell. */
@Serializable
data class RegistrationPoint(
    val alongCellBasePairs: Int,
    val alongCell: Double,
    val acrossCell: Double,
    val nearestCrossoverDistance: Double,
    val onCrossoverColumn: Boolean,
    val anchorForce: Double,
    val anchoredPeakCrossover: Double,
    val anchoredPeakCrossoverStiffAnchor: Double,
    val anchoredPeakDuplexShear: Double,
    val leverPeakCrossover: Double,
    val leverPeakDuplexShear: Double
)

/** The extremes of one registration map, and how wide it is. */
@Serializable
data class RegistrationExtremes(
    val loadCase: String,
    val best: Double,
    val worst: Double,
    val mean: Double,
    val ratio: Double,
    val bestAlongCell: Double,
    val bestAcrossCell: Double,
    val worstAlongCell: Double,
    val worstAcrossCell: Double,
    val bestPlacement: String,
    val worstPlacement: String
)

/** One crossover column phase, solved. */
@Serializable
data class PhaseCase(
    val basePairs: Int,
    val phase: Double,
    val columns: Int,
    val crossovers: Int,
    val centredAnchorPeakCrossover: Double,
    val centredAnchorPeakCrossoverStiffAnchor: Double,
    val centredLeverPeakCrossover: Double,
    val centredLeverPeakDuplexShear: Double,
    val thermalDishingRms: Double,
    val registration: List<RegistrationExtremes>
)

/**
 * One load class, summarised over the complete phase × registration sweep.
 *
 * The two ratios are kept apart deliberately. `phaseRatioAt…Registration` moves the phase
 * while holding the attachment at the *best* (or *worst*) registration of each phase, so it
 * is the phase lever alone; `registrationRatioAt…Phase` holds the phase and sweeps the cell.
 * Multiplying them and comparing against `jointRatio` is what answers "compose or trade".
 */
@Serializable
data class LoadClassSummary(
    val loadCase: String,
    val jointBestForce: Double,
    val jointWorstForce: Double,
    val jointRatio: Double,
    val jointBestPhaseBasePairs: Int,
    val jointWorstPhaseBasePairs: Int,
    val jointBestColumns: Int,
    val jointWorstColumns: Int,
    val jointBestPlacement: String,
    val jointWorstPlacement: String,
    val phaseRatioAtBestRegistration: Double,
    val phaseRatioAtWorstRegistration: Double,
    val registrationRatioAtBestPhase: Double,
    val registrationRatioAtWorstPhase: Double,
    val separableProduct: Double,
    val jointBestAllowable: AllowableMargin,
    val jointWorstAllowable: AllowableMargin
)

/** One `(k_f, k_θ)` state of the layer and the joint, with the whole phase sweep inside it. */
@Serializable
data class LayerStateCase(
    val label: String,
    val foundationMultiplier: Double,
    val foundationStiffness: Double,
    val crossoverAlpha: Double,
    val crossoverHingeStiffness: Double,
    val acrossHelixRigidity: Double,
    val centredAnchorBestPhaseBasePairs: Int,
    val centredAnchorWorstPhaseBasePairs: Int,
    val centredAnchorPhaseRatio: Double,
    val loadClasses: List<LoadClassSummary>,
    val phases: List<PhaseCase>
)

/** One attachment grid in the flatness scan, with its commensurability computed first. */
@Serializable
data class CommensurabilityRecord(
    val attachmentColumns: Int,
    val attachmentRows: Int,
    val attachments: Int,
    val forcePerAttachment: Double,
    val rowSpacing: Double,
    val rowSpacingInDuplexes: Double,
    val distinctRowOffsets: Int,
    val rowOffsetSpread: Double,
    val meanRowOffset: Double,
    val maximumRowOffset: Double,
    val commensurate: Boolean,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverStroke: Double,
    val plateOverStroke: Double,
    val latticeOverPlate: Double
)

/**
 * One `(columns × rows)` attachment grid on both models.
 *
 * The whole rectangle of shapes is scanned rather than the square diagonal, because the
 * square diagonal confounds three things at once — the attachment count, the aspect ratio
 * against a 25×-anisotropic sheet, and the registration of the rows against the duplex
 * lattice. On the full rectangle each can be held while the others move.
 */
@Serializable
data class RectangularGridRecord(
    val attachmentColumns: Int,
    val attachmentRows: Int,
    val attachments: Int,
    val meanRowOffset: Double,
    val rowOffsetSpread: Double,
    val maximumRowOffset: Double,
    val distinctRowOffsets: Int,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverPlate: Double,
    val latticeOverStroke: Double,
    val plateOverStroke: Double,
    val latticeFlat: Boolean,
    val plateFlat: Boolean,
    val latticePeakCrossoverForce: Double,
    val forcePerAttachment: Double
)

/**
 * The cheapest attachment scheme that keeps the tile flat, on each model — **searched over
 * grid shapes, not only over square grids**.
 *
 * `C-0009` scanned the square diagonal and concluded that flatness needs 64 attachment
 * points against the tile's 56 crossovers. The square diagonal is a one-parameter slice of
 * a two-parameter design space, and the sheet is 25× stiffer along the helices than across
 * them, so it is not the slice a designer would choose.
 */
@Serializable
data class FlatnessMinimum(
    val model: String,
    val squareGridAttachments: Int,
    val bestShape: String,
    val bestAttachments: Int,
    val bestPeakDishingOverStroke: Double,
    val bestForcePerAttachment: Double,
    val bestPeakCrossoverForce: Double,
    val attachmentsPerCrossover: Double,
    val crossovers: Int,
    val savingAgainstSquareGrid: Double
)

/** A pair of attachment grids with the same count and transposed shape. */
@Serializable
data class TranspositionRecord(
    val attachments: Int,
    val shapeA: String,
    val shapeB: String,
    val rowOffsetSpreadA: Double,
    val rowOffsetSpreadB: Double,
    val latticeDishingA: Double,
    val latticeDishingB: Double,
    val latticeRatio: Double,
    val plateDishingA: Double,
    val plateDishingB: Double,
    val plateRatio: Double,
    val meanRowOffsetA: Double,
    val meanRowOffsetB: Double,
    /** The lattice's preference with the plate's — anisotropy and aspect ratio — divided out. */
    val latticeExcessOverPlate: Double,
    val commensurateWins: Boolean
)

/** Attachments placed on the duplex axes against attachments placed on the interfaces. */
@Serializable
data class AxisVersusInterfaceRecord(
    val attachmentColumns: Int,
    val attachmentRows: Int,
    val onDuplexAxisDishing: Double,
    val onInterfaceDishing: Double,
    val onDuplexAxisPeakCrossover: Double,
    val onInterfacePeakCrossover: Double,
    val dishingRatio: Double,
    val crossoverForceRatio: Double,
    val plateOnDuplexAxisDishing: Double,
    val plateOnInterfaceDishing: Double,
    val plateDishingRatio: Double,
    /**
     * The lattice's preference with the plate's divided out. A shift of the whole grid by
     * `d/2` also moves it relative to the free edges, which a plate feels just as much, so
     * only this excess is a statement about the duplex lattice.
     */
    val latticeExcessOverPlate: Double
)

/**
 * The registration map binned by the **distance from the attachment to the nearest
 * crossover** — which the map turns out to be governed by, for both load classes.
 *
 * Two quantities run in opposite directions across the bins, which is the whole reason both
 * are carried: the crossover force falls with distance and the duplex transverse shear
 * rises. Their optima are at opposite corners of the one-crossover cell.
 */
@Serializable
data class CrossoverProximityRecord(
    val distanceBin: Double,
    val points: Int,
    val meanAnchoredPeakCrossover: Double,
    val minAnchoredPeakCrossover: Double,
    val maxAnchoredPeakCrossover: Double,
    val meanAnchoredPeakDuplexShear: Double,
    val meanLeverPeakCrossover: Double,
    val minLeverPeakCrossover: Double,
    val maxLeverPeakCrossover: Double,
    val meanLeverPeakDuplexShear: Double
)

/** One offset of a whole attachment grid relative to the duplex axes. */
@Serializable
data class AttachmentRowPhaseRecord(
    val attachmentColumns: Int,
    val attachmentRows: Int,
    val rowOffset: Double,
    val rowOffsetInDuplexes: Double,
    val peakDishing: Double,
    val peakDishingOverStroke: Double,
    val peakCrossoverForce: Double,
    val dishingRelativeToOnAxis: Double,
    val crossoverForceRelativeToOnAxis: Double,
    val platePeakDishing: Double,
    val plateDishingRelativeToOnAxis: Double,
    val latticeExcessOverPlate: Double
)

/** The finite-tile contamination of the registration variable, measured. */
@Serializable
data class PeriodicityRecord(
    val translation: String,
    val alongCell: Double,
    val acrossCell: Double,
    val peakCrossoverHere: Double,
    val peakCrossoverTranslated: Double,
    val residual: Double
)

/** Gate 4, on the phases whose edge element is shortest. */
@Serializable
data class PhaseConvergenceRecord(
    val basePairs: Int,
    val nearestColumnToEdge: Double,
    val subdivisions: Int,
    val leverPeakCrossover: Double,
    val anchoredPeakCrossover: Double
)

@Serializable
data class RegistrationParameters(
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val tileFootprint: String,
    val tileArea: Double,
    val beamCount: Int,
    val interhelicalDistance: Double,
    val crossoverSpacing: Double,
    val columnSpacing: Double,
    val basePairPhases: Int,
    val risePerBasePair: Double,
    val targetForce: Double,
    val targetPressure: Double,
    val subdivisions: Int,
    val plateBasisDegree: Int,
    val registrationAlongSteps: Int,
    val registrationAcrossSteps: Int,
    val coarseAlongSteps: Int,
    val coarseAcrossSteps: Int,
    val anchorStiffnessFractions: List<Double>,
    val foundationSweep: List<Double>,
    val crossoverAlphaSweep: List<Double>,
    val duplexUnzipAllowableLower: Double,
    val duplexUnzipAllowableUpper: Double,
    val duplexShearAllowable: Double,
    val overstretchingCeiling: Double,
    val provenance: Map<String, String>
)

@Serializable
data class RegistrationResult(
    val task: String,
    val leaves: List<String>,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: RegistrationParameters,
    val phaseGeometry: List<PhaseGeometry>,
    val nominal: LayerStateCase,
    val nominalRegistrationMap: List<RegistrationPoint>,
    val foundationStates: List<LayerStateCase>,
    val hingeStates: List<LayerStateCase>,
    val cornerStates: List<LayerStateCase>,
    val commensurability: List<CommensurabilityRecord>,
    val rectangularScan: List<RectangularGridRecord>,
    val flatnessMinimum: List<FlatnessMinimum>,
    val transpositions: List<TranspositionRecord>,
    val axisVersusInterface: List<AxisVersusInterfaceRecord>,
    val crossoverProximity: List<CrossoverProximityRecord>,
    val attachmentRowPhase: List<AttachmentRowPhaseRecord>,
    val periodicity: List<PeriodicityRecord>,
    val phaseConvergence: List<PhaseConvergenceRecord>,
    val verdict: Map<String, String>
)

// --------------------------------------------------------------------------- run parameters

private const val BEAM_COUNT = 15

private const val SUBDIVISIONS = 2

private const val PLATE_BASIS_DEGREE = 12

/** The one-crossover cell is sampled at one base pair along the helices. */
private const val REGISTRATION_ALONG_STEPS = 32

/** …and at nine stations across one duplex, the two ends being the two sides of one interface. */
private const val REGISTRATION_ACROSS_STEPS = 9

private const val COARSE_ALONG_STEPS = 8

private const val COARSE_ACROSS_STEPS = 5

/** The anchor stiffness as a fraction of the whole layer's, `C-0009`'s nominal and worst. */
private const val ANCHOR_FRACTION = 1.0

private const val ANCHOR_FRACTION_STIFF = 10.0

/** The upper edge of the unzip band, in pN — Essevaz-Roulet et al. report "10–15 pN". */
private const val DUPLEX_UNZIP_UPPER: Double = 15.0

/**
 * A dishing amplitude below this fraction of the stroke leaves the rigid-plate picture
 * standing — `T-5b`'s criterion, and `C-0009`'s, inherited unchanged so that the attachment
 * counts reported here are directly comparable with the 64 `C-0009` published.
 */
private const val RIGID_PLATE_TOLERANCE: Double = 0.10

private val ALPHA_SWEEP = listOf(0.6, 1.0, 1.2)

/** `CH-0005`'s out-of-range probe: the `α` at which `D_⊥` reaches `D_∥`. */
private val ISOTROPIC_ALPHA: Double =
    Gen1Tile.DUPLEX_BENDING_RIGIDITY / Gen1Tile.INTERHELICAL_SHEET *
            (Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR) /
            Gen1Tile.INTERHELICAL_SHEET / Gen1Tile.crossoverHingeStiffness(1.0)

private val ATTACHMENT_SIDES = 1..14

/** Transposed grid shapes of equal attachment count, the causal test of the commensurability rule. */
/** The shapes the rectangular scan sweeps, in both directions. */
private val GRID_SIDES = 1..15

private val TRANSPOSITIONS = listOf(
    // the decisive pair: five rows exactly on the duplex axes against eleven that are not,
    // at the same attachment count — the on-axis grid has FEWER rows and a worse aspect
    // ratio for a 25x-anisotropic sheet, so if it still wins, registration is doing the work
    11 to 5, 13 to 5, 7 to 5,
    // rows exactly on the duplex axes against an incommensurate row count
    4 to 15, 7 to 15, 11 to 15, 13 to 15,
    // equal mean offset, different spread — does the spread matter on its own?
    8 to 10, 10 to 12,
    // a coarser mean-offset contrast
    7 to 12, 11 to 12,
    // CONTROLS: both row counts sit exactly on duplex axes, so the rule predicts no excess
    5 to 15, 3 to 5,
    // CONTROL: equal mean offset AND equal spread
    4 to 12, 6 to 10
)

private fun sheetOf(alpha: Double): OrigamiSheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP, crossoverAlpha = alpha
)

private fun plateOf(sheet: OrigamiSheet): OrthotropicPlate = OrthotropicPlate(
    lengthX = Gen1Tile.EDGE_X,
    lengthY = BEAM_COUNT * sheet.interhelicalDistance,
    rigidityX = sheet.alongHelixRigidity,
    rigidityY = sheet.acrossHelixRigidity,
    twistingRigidity = sheet.twistingRigidity
)

private fun latticeAtPhase(
    sheet: OrigamiSheet,
    foundationStiffness: Double,
    basePairs: Int,
    subdivisions: Int = SUBDIVISIONS
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = BEAM_COUNT,
    foundationStiffness = foundationStiffness,
    columns = CrossoverLayout.atBasePairPhase(basePairs, sheet, Gen1Tile.EDGE_X),
    subdivisions = subdivisions
)

/** Returns the positions of a [columns] × [rows] grid inset within the footprint. */
private fun insetRectangle(
    columns: Int,
    rows: Int,
    lengthX: Double,
    lengthY: Double
): List<Pair<Double, Double>> = (0 until columns).flatMap { i ->
    (0 until rows).map { j ->
        Pair(
            -lengthX / 2.0 + lengthX * (i + 0.5) / columns,
            -lengthY / 2.0 + lengthY * (j + 0.5) / rows
        )
    }
}

/**
 * The one-crossover unit cell, sampled.
 *
 * `u` runs over one **per-interface** spacing `p` at one base pair a step, centred on the
 * tile centre; `v` runs across one duplex from interface to interface. The cell area is
 * `p × d`, which holds exactly one crossover, so this is a complete registration sweep and
 * not a sample of one.
 */
private fun registrationCell(
    sheet: OrigamiSheet,
    alongSteps: Int,
    acrossSteps: Int
): List<Pair<Double, Double>> {
    val alongStep = sheet.crossoverSpacing / alongSteps
    return (0 until alongSteps).flatMap { j ->
        val u = (j - alongSteps / 2) * alongStep
        (0 until acrossSteps).map { k ->
            u to (k.toDouble() / (acrossSteps - 1) - 0.5) * sheet.interhelicalDistance
        }
    }
}

private fun perPathVerdict(force: Double): String = when {
    force >= Gen1Tile.OVERSTRETCHING_CEILING ->
        "ABOVE the 65 pN nicked-duplex overstretching ceiling"
    force >= Gen1Tile.DUPLEX_SHEAR_ALLOWABLE ->
        "above the 48 pN quasi-static single-duplex shear allowable"
    force >= DUPLEX_UNZIP_UPPER -> "above the whole 10-15 pN unzip band"
    force >= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE -> "INSIDE the 10-15 pN unzip band"
    else -> "below every per-path allowable"
}

private fun allowable(force: Double): AllowableMargin = AllowableMargin(
    peakForce = force,
    unzipLowerMargin = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / force,
    unzipUpperMargin = DUPLEX_UNZIP_UPPER / force,
    shearMargin = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / force,
    ceilingMargin = Gen1Tile.OVERSTRETCHING_CEILING / force,
    verdict = perPathVerdict(force)
)

/**
 * A placement described by its **distances**, not by a bucket.
 *
 * The registration map turns out to be governed by one of these three and not by the
 * qualitative label a four-point sample suggests, so the label is quantitative on purpose.
 */
private fun placementName(
    lattice: OrigamiGrillage,
    x: Double,
    y: Double,
    sheet: OrigamiSheet
): String = ("%.2f nm from the nearest crossover, |across| = %.3f d, %.3f column pitches " +
        "from the nearest column").format(
    lattice.crossovers.minOf { hypot(it.x - x, it.y - y) },
    abs(y) / sheet.interhelicalDistance,
    lattice.columnX.minOf { abs(it - x) } / (sheet.crossoverSpacing / 2.0)
)

// --------------------------------------------------------------------------- registration map

private fun registrationMap(
    lattice: OrigamiGrillage,
    sheet: OrigamiSheet,
    cell: List<Pair<Double, Double>>,
    pressure: Double,
    alongStep: Double
): List<RegistrationPoint> {
    val load = uniformPressure(pressure)
    val layerStiffness = lattice.foundationStiffness * lattice.area
    val anchored = lattice.solveWithEachAnchor(
        cell.map { (x, y) -> PointSupport(x, y, ANCHOR_FRACTION * layerStiffness) }, load
    )
    val anchoredStiff = lattice.solveWithEachAnchor(
        cell.map { (x, y) -> PointSupport(x, y, ANCHOR_FRACTION_STIFF * layerStiffness) }, load
    )
    return cell.mapIndexed { index, (x, y) ->
        val lever = lattice.solve(
            pointLoads = listOf(PointLoad(x, y, Gen1Tile.TARGET_FORCE))
        )
        RegistrationPoint(
            alongCellBasePairs = Math.round(x / Gen1Tile.RISE_PER_BASE_PAIR).toInt(),
            alongCell = x,
            acrossCell = y,
            nearestCrossoverDistance = lattice.crossovers.minOf { hypot(it.x - x, it.y - y) },
            onCrossoverColumn =
                lattice.columnX.minOf { abs(it - x) } < alongStep / 2.0,
            anchorForce = anchored[index].supportForces.single(),
            anchoredPeakCrossover = anchored[index].peakCrossoverForce,
            anchoredPeakCrossoverStiffAnchor = anchoredStiff[index].peakCrossoverForce,
            anchoredPeakDuplexShear = anchored[index].peakDuplexShear,
            leverPeakCrossover = lever.peakCrossoverForce,
            leverPeakDuplexShear = lever.peakDuplexShear
        )
    }
}

private fun extremes(
    loadCase: String,
    points: List<RegistrationPoint>,
    lattice: OrigamiGrillage,
    sheet: OrigamiSheet,
    quantity: (RegistrationPoint) -> Double
): RegistrationExtremes {
    // rounded selection with the cell coordinates as the tie-break, for the same reason
    // [leastBy] rounds: an argmin is not a rounded double and a last-ulp tie flips it
    val ordered = points.sortedWith(
        compareBy({ roundForResult(quantity(it)) }, { it.alongCell }, { it.acrossCell })
    )
    val best = ordered.first()
    val worst = ordered.last()
    return RegistrationExtremes(
        loadCase = loadCase,
        best = quantity(best),
        worst = quantity(worst),
        mean = points.sumOf(quantity) / points.size,
        ratio = quantity(worst) / quantity(best),
        bestAlongCell = best.alongCell,
        bestAcrossCell = best.acrossCell,
        worstAlongCell = worst.alongCell,
        worstAcrossCell = worst.acrossCell,
        bestPlacement = placementName(lattice, best.alongCell, best.acrossCell, sheet),
        worstPlacement = placementName(lattice, worst.alongCell, worst.acrossCell, sheet)
    )
}

private fun phaseCase(
    sheet: OrigamiSheet,
    foundationStiffness: Double,
    basePairs: Int,
    cell: List<Pair<Double, Double>>,
    pressure: Double,
    alongStep: Double,
    withThermal: Boolean
): Pair<PhaseCase, List<RegistrationPoint>> {
    val lattice = latticeAtPhase(sheet, foundationStiffness, basePairs)
    val layerStiffness = foundationStiffness * lattice.area
    val load = uniformPressure(pressure)
    val centred = lattice.solveWithAnchor(
        PointSupport(0.0, 0.0, ANCHOR_FRACTION * layerStiffness), load
    )
    val centredStiff = lattice.solveWithAnchor(
        PointSupport(0.0, 0.0, ANCHOR_FRACTION_STIFF * layerStiffness), load
    )
    val centredLever = lattice.solve(
        pointLoads = listOf(PointLoad(0.0, 0.0, Gen1Tile.TARGET_FORCE))
    )
    val points = registrationMap(lattice, sheet, cell, pressure, alongStep)
    val thermal = if (withThermal) {
        latticeAtPhase(sheet, Gen1Tile.FOUNDATION_AT_REST, basePairs)
            .thermalFluctuation(ROOM_TEMPERATURE).dishingRms
    } else 0.0
    return PhaseCase(
        basePairs = basePairs,
        phase = basePairs * Gen1Tile.RISE_PER_BASE_PAIR,
        columns = lattice.crossoverColumns,
        crossovers = lattice.crossovers.size,
        centredAnchorPeakCrossover = centred.peakCrossoverForce,
        centredAnchorPeakCrossoverStiffAnchor = centredStiff.peakCrossoverForce,
        centredLeverPeakCrossover = centredLever.peakCrossoverForce,
        centredLeverPeakDuplexShear = centredLever.peakDuplexShear,
        thermalDishingRms = thermal,
        registration = listOf(
            extremes("anchored, k_a = k_f A", points, lattice, sheet) {
                it.anchoredPeakCrossover
            },
            extremes("anchored, k_a = 10 k_f A", points, lattice, sheet) {
                it.anchoredPeakCrossoverStiffAnchor
            },
            extremes("concentrated lever attachment", points, lattice, sheet) {
                it.leverPeakCrossover
            }
        )
    ) to points
}

private val LOAD_CLASSES = listOf(
    "anchored, k_a = k_f A",
    "anchored, k_a = 10 k_f A",
    "concentrated lever attachment"
)

private fun layerState(
    label: String,
    foundationMultiplier: Double,
    alpha: Double,
    alongSteps: Int,
    acrossSteps: Int,
    withThermal: Boolean
): Pair<LayerStateCase, List<RegistrationPoint>> {
    val sheet = sheetOf(alpha)
    val foundation = Gen1Tile.FOUNDATION_SECANT * foundationMultiplier
    val area = Gen1Tile.EDGE_X * BEAM_COUNT * sheet.interhelicalDistance
    val pressure = Gen1Tile.TARGET_FORCE / area
    val cell = registrationCell(sheet, alongSteps, acrossSteps)
    val alongStep = sheet.crossoverSpacing / alongSteps
    var nominalMap: List<RegistrationPoint> = emptyList()
    val phases = (0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD).map { basePairs ->
        val (case, points) = phaseCase(
            sheet, foundation, basePairs, cell, pressure, alongStep, withThermal
        )
        if (basePairs == NOMINAL_PHASE_BASE_PAIRS) nominalMap = points
        case
    }
    val centredBest = phases.leastBy { it.centredAnchorPeakCrossoverStiffAnchor }
    val centredWorst = phases.greatestBy { it.centredAnchorPeakCrossoverStiffAnchor }
    val loadClasses = LOAD_CLASSES.map { loadCase ->
        fun band(case: PhaseCase): RegistrationExtremes =
            case.registration.first { it.loadCase == loadCase }
        val jointBest = phases.leastBy { band(it).best }
        val jointWorst = phases.greatestBy { band(it).worst }
        val bestOverPhases = phases.maxOf { band(it).best } / phases.minOf { band(it).best }
        val worstOverPhases = phases.maxOf { band(it).worst } / phases.minOf { band(it).worst }
        val registrationBest = band(jointBest).ratio
        val registrationWorst = band(jointWorst).ratio
        LoadClassSummary(
            loadCase = loadCase,
            jointBestForce = band(jointBest).best,
            jointWorstForce = band(jointWorst).worst,
            jointRatio = band(jointWorst).worst / band(jointBest).best,
            jointBestPhaseBasePairs = jointBest.basePairs,
            jointWorstPhaseBasePairs = jointWorst.basePairs,
            jointBestColumns = jointBest.columns,
            jointWorstColumns = jointWorst.columns,
            jointBestPlacement = band(jointBest).bestPlacement,
            jointWorstPlacement = band(jointWorst).worstPlacement,
            phaseRatioAtBestRegistration = bestOverPhases,
            phaseRatioAtWorstRegistration = worstOverPhases,
            registrationRatioAtBestPhase = registrationBest,
            registrationRatioAtWorstPhase = registrationWorst,
            separableProduct = worstOverPhases * registrationWorst,
            jointBestAllowable = allowable(band(jointBest).best),
            jointWorstAllowable = allowable(band(jointWorst).worst)
        )
    }
    return LayerStateCase(
        label = label,
        foundationMultiplier = foundationMultiplier,
        foundationStiffness = foundation,
        crossoverAlpha = alpha,
        crossoverHingeStiffness = sheet.crossoverHingeStiffness,
        acrossHelixRigidity = sheet.acrossHelixRigidity,
        centredAnchorBestPhaseBasePairs = centredBest.basePairs,
        centredAnchorWorstPhaseBasePairs = centredWorst.basePairs,
        centredAnchorPhaseRatio = centredWorst.centredAnchorPeakCrossoverStiffAnchor /
                centredBest.centredAnchorPeakCrossoverStiffAnchor,
        loadClasses = loadClasses,
        phases = phases
    ) to nominalMap
}

/**
 * The phase minimising [quantity], selected on the **rounded** value with the base-pair
 * phase as the tie-break.
 *
 * Rounding at the serialisation boundary is not enough when a result file contains an
 * *argmin*: the index of an extremum is not a rounded double, and the phase sweep is flat to
 * under 0.5 % within a column count, so at some layer states two phases tie to the last unit
 * in the last place. `minByOrNull` then returns whichever the JIT's summation order happened
 * to favour, and the file stops being reproducible even though every number in it is
 * identical. The decision has to be rounded too.
 */
private fun List<PhaseCase>.leastBy(quantity: (PhaseCase) -> Double): PhaseCase =
    sortedWith(compareBy({ roundForResult(quantity(it)) }, { it.basePairs })).first()

/** [leastBy], the other way up. */
private fun List<PhaseCase>.greatestBy(quantity: (PhaseCase) -> Double): PhaseCase =
    sortedWith(compareBy({ -roundForResult(quantity(it)) }, { it.basePairs })).first()

/** The summary of [loadCase] inside a state, by name. */
private fun LayerStateCase.loadClass(loadCase: String): LoadClassSummary =
    loadClasses.first { it.loadCase == loadCase }

/** The anchored load class `C-0009`'s worst case belongs to. */
private const val ANCHORED_STIFF = "anchored, k_a = 10 k_f A"

private const val LEVER = "concentrated lever attachment"

/** The phase at which `T-10`'s eight symmetrically centred columns sit, in base pairs. */
private const val NOMINAL_PHASE_BASE_PAIRS = 8

// --------------------------------------------------------------------------- the study

fun main() {
    val started = System.currentTimeMillis()
    val nominalSheet = sheetOf(1.0)
    val area = Gen1Tile.EDGE_X * BEAM_COUNT * nominalSheet.interhelicalDistance
    val pressure = Gen1Tile.TARGET_FORCE / area

    val phaseGeometry = phaseGeometry(nominalSheet)
    val (nominal, nominalMap) = layerState(
        label = "C-0001 secant, Chen et al. alpha = 1.0 — the C-0009 design point",
        foundationMultiplier = 1.0,
        alpha = 1.0,
        alongSteps = REGISTRATION_ALONG_STEPS,
        acrossSteps = REGISTRATION_ACROSS_STEPS,
        withThermal = true
    )
    println("nominal phase x registration map done at ${elapsed(started)}")

    val foundationStates = Gen1Tile.FOUNDATION_SWEEP.map { multiplier ->
        layerState(
            label = "k_f x $multiplier, alpha = 1.0",
            foundationMultiplier = multiplier,
            alpha = 1.0,
            alongSteps = COARSE_ALONG_STEPS,
            acrossSteps = COARSE_ACROSS_STEPS,
            withThermal = false
        ).first
    }
    println("foundation sweep done at ${elapsed(started)}")

    val hingeStates = (ALPHA_SWEEP + ISOTROPIC_ALPHA).map { alpha ->
        layerState(
            label = if (alpha in ALPHA_SWEEP) "k_f x 1.0, Chen et al. alpha = $alpha"
            else "k_f x 1.0, OUT OF RANGE probe alpha = %.2f (D_perp = D_par, the CH-0005 scenario)"
                .format(alpha),
            foundationMultiplier = 1.0,
            alpha = alpha,
            alongSteps = COARSE_ALONG_STEPS,
            acrossSteps = COARSE_ACROSS_STEPS,
            withThermal = false
        ).first
    }
    println("hinge sweep done at ${elapsed(started)}")

    val cornerStates = listOf(0.25 to 0.6, 0.25 to 1.2, 4.0 to 0.6, 4.0 to 1.2).map { (kf, a) ->
        layerState(
            label = "corner: k_f x $kf, alpha = $a",
            foundationMultiplier = kf,
            alpha = a,
            alongSteps = COARSE_ALONG_STEPS,
            acrossSteps = COARSE_ACROSS_STEPS,
            withThermal = false
        ).first
    }
    println("corner states done at ${elapsed(started)}")

    val nominalLattice = latticeAtPhase(
        nominalSheet, Gen1Tile.FOUNDATION_SECANT, NOMINAL_PHASE_BASE_PAIRS
    )
    val nominalPlate = PlateOnFoundation(
        plateOf(nominalSheet), Gen1Tile.FOUNDATION_SECANT, basisDegree = PLATE_BASIS_DEGREE
    )
    val stroke = pressure / Gen1Tile.FOUNDATION_SECANT

    val commensurability = ATTACHMENT_SIDES.map { side ->
        commensurabilityRecord(side, side, nominalLattice, nominalPlate, nominalSheet, stroke)
    }
    val rectangularScan = GRID_SIDES.flatMap { columns ->
        GRID_SIDES.map { rows ->
            rectangularGrid(columns, rows, nominalLattice, nominalPlate, stroke)
        }
    }
    val transpositions = TRANSPOSITIONS.map { (a, b) ->
        transposition(a, b, nominalLattice, nominalPlate, nominalSheet, stroke)
    }
    val axisVersusInterface = listOf(5 to 5, 8 to 8, 3 to 5).map { (columns, rows) ->
        axisVersusInterface(columns, rows, nominalLattice, nominalPlate, nominalSheet)
    }
    val proximity = crossoverProximity(nominalMap)
    val rowPhase = listOf(5 to 5, 8 to 8).flatMap { (columns, rows) ->
        attachmentRowPhase(columns, rows, 13, nominalLattice, nominalPlate, nominalSheet, stroke)
    }
    println("commensurability done at ${elapsed(started)}")

    val periodicity = periodicity(nominalLattice, nominalSheet, pressure)
    val phaseConvergence = phaseConvergence(nominalSheet, phaseGeometry, pressure)
    println("gates done at ${elapsed(started)}")

    val result = RegistrationResult(
        task = "T-14",
        leaves = listOf("A8.2"),
        title = "Crossover column phase and attachment registration as design variables",
        verificationType = "in-silico (the T-10 beam-and-hinge grillage, re-parameterised by " +
                "the staple layout's own free variables and swept completely over both)",
        acceptance = "The peak per-load-path force as a complete function of the crossover " +
                "column phase (all 32 base-pair phases of the 32 bp per-interface period) and " +
                "of the attachment registration over the whole one-crossover unit cell, with " +
                "the best and worst layouts named and their margins to the 10-15 pN unzip, " +
                "48 pN shear and 65 pN nicked-ceiling allowables reported; the ranking stated " +
                "as a function of k_f and k_theta; and the non-monotone flatness curve " +
                "explained by a commensurability metric computed independently of the solve",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "moment" to "pN*nm",
            "pressure" to "pN/nm^2 (= MPa)",
            "foundationStiffness" to "pN/nm^3",
            "flexuralRigidity" to "pN*nm",
            "hingeStiffness" to "pN*nm/rad",
            "phase" to "base pairs (and nm)",
            "temperature" to "K"
        ),
        conventions = listOf(
            "x along the helices, y across them, origin at the centre of the footprint — T-5",
            "w positive DOWNWARD, compressing the polymer layer — T-5",
            "a crossover force is signed so that positive is transmitted from the far side of " +
                    "its interface toward the near one — T-10",
            "the column PHASE is the offset of the infinite column lattice from the tile " +
                    "centre, in base pairs; its period is the PER-INTERFACE spacing p = 32 bp, " +
                    "not the per-helix 16 bp, because a shift by p/2 hands every interface the " +
                    "other parity's columns",
            "the REGISTRATION of an attachment is its position within the p x d one-crossover " +
                    "unit cell: `alongCell` from the tile centre along the helices over one " +
                    "full p, `acrossCell` from a duplex axis over one full d",
            "an attachment at acrossCell = +/- d/2 is at an interface, i.e. the two ends of " +
                    "the across-cell range are the two sides of the same crossover line"
        ),
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. NOT measured.",
            "THE STAPLE LAYOUT IS NOT REPORTED AS A RESULT. Whether a Gen-1 tile has seven or " +
                    "eight crossover columns and at what phase is a property of a design " +
                    "nobody in this programme has. What is reported is the SENSITIVITY to that " +
                    "choice and the RULE it implies.",
            "the crossover's VERTICAL/AXIAL compliance is a rigid constraint here, inherited " +
                    "from C-0009 and the single assumption under it with nothing cited behind " +
                    "it; T-9 could settle it at the same cost as k_theta",
            "k_theta is a FITTED model input (Chen et al.), not a measurement, and is swept " +
                    "over its whole admissible range plus the out-of-range isotropic probe",
            "an attachment is modelled as attaching at ONE point of ONE duplex, which is what " +
                    "a tether is; a tether bonded to a crossover or to two duplexes at once " +
                    "would spread the load and is not represented",
            "linear Winkler foundation, drained per C-0004; k_f from C-0001 are lower bounds " +
                    "per CH-0001, swept x[0.25, 4]",
            "no electrostatics is solved: the load enters as a uniform 100 pN or as a " +
                    "concentrated 100 pN, per T-10",
            "the registration variable is lattice-periodic only up to the finite-tile " +
                    "contamination, which is MEASURED here rather than assumed away"
        ),
        parameters = RegistrationParameters(
            temperature = ROOM_TEMPERATURE,
            medium = "aqueous buffer, 2/5/10 mM MgCl2",
            thermalEnergy = thermalEnergy(),
            tileFootprint = "${Gen1Tile.EDGE_X} x ${nominalLattice.lengthY} nm",
            tileArea = area,
            beamCount = BEAM_COUNT,
            interhelicalDistance = nominalSheet.interhelicalDistance,
            crossoverSpacing = nominalSheet.crossoverSpacing,
            columnSpacing = nominalSheet.crossoverSpacing / 2.0,
            basePairPhases = CrossoverLayout.BASE_PAIRS_PER_PERIOD,
            risePerBasePair = Gen1Tile.RISE_PER_BASE_PAIR,
            targetForce = Gen1Tile.TARGET_FORCE,
            targetPressure = pressure,
            subdivisions = SUBDIVISIONS,
            plateBasisDegree = PLATE_BASIS_DEGREE,
            registrationAlongSteps = REGISTRATION_ALONG_STEPS,
            registrationAcrossSteps = REGISTRATION_ACROSS_STEPS,
            coarseAlongSteps = COARSE_ALONG_STEPS,
            coarseAcrossSteps = COARSE_ACROSS_STEPS,
            anchorStiffnessFractions = listOf(ANCHOR_FRACTION, ANCHOR_FRACTION_STIFF),
            foundationSweep = Gen1Tile.FOUNDATION_SWEEP,
            crossoverAlphaSweep = ALPHA_SWEEP + ISOTROPIC_ALPHA,
            duplexUnzipAllowableLower = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            duplexUnzipAllowableUpper = DUPLEX_UNZIP_UPPER,
            duplexShearAllowable = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
            overstretchingCeiling = Gen1Tile.OVERSTRETCHING_CEILING,
            provenance = mapOf(
                "crossoverSpacing" to "CITED — Rothemund Nature 440:297 (2006): 1.5 turns " +
                        "alternating between two neighbours = 32 bp per interface, so the " +
                        "column pitch is 16 bp and the PHASE PERIOD is 32 bp",
                "risePerBasePair" to "CITED — Douglas et al. Nature 459:414 (2009), 0.34 nm; " +
                        "this is what quantises the phase into exactly 32 values",
                "interhelicalDistance" to "CITED, MEASURED — Fischer et al. Nano Lett 16:4282 " +
                        "(2016), SAXS, 26.9 +/- 0.2 A for a one-layer sheet",
                "duplexRigidities" to "CITED — CanDo, Kim et al. NAR 40:2862 (2012)",
                "crossoverHingeStiffness" to "CITED, fitted — Chen et al. JACS 136:6995 (2014)",
                "perPathAllowables" to "CITED, MEASURED — Essevaz-Roulet et al. PNAS 94:11935 " +
                        "(1997) 10-15 pN unzip; Strunz et al. PNAS 96:11277 (1999) 48 +/- 2 pN " +
                        "shear; van Mameren et al. PNAS 106:18231 (2009) 65 pN nicked ceiling. " +
                        "The 35-60 pN band of §4(f) is a whole-cross-section number and is NOT " +
                        "used as a per-path allowable.",
                "foundationStiffness" to "DERIVED from C-0001, under challenge (CH-0001)",
                "latticeModel" to "DERIVED — the T-10 grillage, C-0009, unchanged except for " +
                        "the column layout being a phase rather than a count"
            )
        ),
        phaseGeometry = phaseGeometry,
        nominal = nominal,
        nominalRegistrationMap = nominalMap,
        foundationStates = foundationStates,
        hingeStates = hingeStates,
        cornerStates = cornerStates,
        commensurability = commensurability,
        rectangularScan = rectangularScan,
        flatnessMinimum = listOf(
            flatnessMinimum(
                "lattice", rectangularScan, { it.latticeFlat }, { it.latticeOverStroke },
                nominalLattice.crossovers.size
            ),
            flatnessMinimum(
                "continuum plate", rectangularScan, { it.plateFlat }, { it.plateOverStroke },
                nominalLattice.crossovers.size
            )
        ),
        transpositions = transpositions,
        axisVersusInterface = axisVersusInterface,
        crossoverProximity = proximity,
        attachmentRowPhase = rowPhase,
        periodicity = periodicity,
        phaseConvergence = phaseConvergence,
        verdict = verdicts(
            nominal, foundationStates, hingeStates, cornerStates,
            commensurability, transpositions, axisVersusInterface, rowPhase
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-14-crossover-phase-and-registration.json")
    output.parentFile.mkdirs()
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    report(result, output, started)
}

private fun elapsed(started: Long): String =
    "%.1f s".format((System.currentTimeMillis() - started) / 1000.0)

// --------------------------------------------------------------------------- the cheap bound

/**
 * The phase table, from arithmetic alone — the cheap bound this task runs before any lattice
 * is assembled.
 *
 * It settles in advance how much of any phase effect can possibly be a **rigidity** effect:
 * the phase moves the crossover count by at most one column's worth, and `D_⊥` with it, so
 * anything larger than that is load-path topology and not the sheet getting softer.
 */
private fun phaseGeometry(sheet: OrigamiSheet): List<PhaseGeometry> {
    val continuumCount = Gen1Tile.EDGE_X * BEAM_COUNT * sheet.interhelicalDistance /
            (sheet.interhelicalDistance * sheet.crossoverSpacing)
    val reference = CrossoverLayout.atBasePairPhase(
        NOMINAL_PHASE_BASE_PAIRS, sheet, Gen1Tile.EDGE_X
    )
    val referenceCrossovers = crossoverCount(reference, BEAM_COUNT)
    return (0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD).map { basePairs ->
        val layout = CrossoverLayout.atBasePairPhase(basePairs, sheet, Gen1Tile.EDGE_X)
        val crossovers = crossoverCount(layout, BEAM_COUNT)
        val centro = (layout.size + BEAM_COUNT) % 2 == 1
        PhaseGeometry(
            basePairs = basePairs,
            phase = basePairs * Gen1Tile.RISE_PER_BASE_PAIR,
            columns = layout.size,
            crossovers = crossovers,
            evenParityColumns = layout.countOfParity(0),
            oddParityColumns = layout.countOfParity(1),
            nearestColumnToEdge = Gen1Tile.EDGE_X / 2.0 - layout.positions.maxOf { abs(it) },
            crossoverCountRatioToContinuum = crossovers / continuumCount,
            acrossHelixRigidityChange = crossovers.toDouble() / referenceCrossovers,
            centroSymmetric = centro,
            symmetryGroup = if (centro) "centro-symmetric (2), no mirror"
            else "trivial (1): neither mirror nor inversion"
        )
    }
}

private fun crossoverCount(layout: CrossoverLayout, beamCount: Int): Int =
    (0 until beamCount - 1).sumOf { beam ->
        layout.parities.count { (it + beam) % 2 == 0 }
    }

// --------------------------------------------------------------------------- commensurability

private fun commensurabilityRecord(
    columns: Int,
    rows: Int,
    lattice: OrigamiGrillage,
    plate: PlateOnFoundation,
    sheet: OrigamiSheet,
    stroke: Double
): CommensurabilityRecord {
    val count = columns * rows
    val loads = insetRectangle(columns, rows, Gen1Tile.EDGE_X, lattice.lengthY).map { (x, y) ->
        PointLoad(x, y, Gen1Tile.TARGET_FORCE / count)
    }
    val latticePeak = lattice.solve(pointLoads = loads).peakDishing()
    val platePeak = plate.solve(pointLoads = loads).peakDishing()
    val spread = attachmentOffsetSpread(rows, BEAM_COUNT)
    val offsets = attachmentRowOffsets(rows, BEAM_COUNT)
    return CommensurabilityRecord(
        attachmentColumns = columns,
        attachmentRows = rows,
        attachments = count,
        forcePerAttachment = Gen1Tile.TARGET_FORCE / count,
        rowSpacing = lattice.lengthY / rows,
        rowSpacingInDuplexes = BEAM_COUNT.toDouble() / rows,
        distinctRowOffsets = distinctAttachmentOffsets(rows, BEAM_COUNT),
        rowOffsetSpread = spread,
        meanRowOffset = offsets.sumOf { abs(it) } / offsets.size,
        maximumRowOffset = offsets.maxOf { abs(it) },
        commensurate = spread < 1e-9,
        latticePeakDishing = latticePeak,
        platePeakDishing = platePeak,
        latticeOverStroke = latticePeak / stroke,
        plateOverStroke = platePeak / stroke,
        latticeOverPlate = latticePeak / platePeak
    )
}

private fun rectangularGrid(
    columns: Int,
    rows: Int,
    lattice: OrigamiGrillage,
    plate: PlateOnFoundation,
    stroke: Double
): RectangularGridRecord {
    val count = columns * rows
    val loads = insetRectangle(columns, rows, Gen1Tile.EDGE_X, lattice.lengthY).map { (x, y) ->
        PointLoad(x, y, Gen1Tile.TARGET_FORCE / count)
    }
    val solution = lattice.solve(pointLoads = loads)
    val latticePeak = solution.peakDishing()
    val platePeak = plate.solve(pointLoads = loads).peakDishing()
    val offsets = attachmentRowOffsets(rows, BEAM_COUNT)
    return RectangularGridRecord(
        attachmentColumns = columns,
        attachmentRows = rows,
        attachments = count,
        meanRowOffset = offsets.sumOf { abs(it) } / offsets.size,
        rowOffsetSpread = attachmentOffsetSpread(rows, BEAM_COUNT),
        maximumRowOffset = offsets.maxOf { abs(it) },
        distinctRowOffsets = distinctAttachmentOffsets(rows, BEAM_COUNT),
        latticePeakDishing = latticePeak,
        platePeakDishing = platePeak,
        latticeOverPlate = latticePeak / platePeak,
        latticeOverStroke = latticePeak / stroke,
        plateOverStroke = platePeak / stroke,
        latticeFlat = latticePeak / stroke < RIGID_PLATE_TOLERANCE,
        plateFlat = platePeak / stroke < RIGID_PLATE_TOLERANCE,
        latticePeakCrossoverForce = solution.peakCrossoverForce,
        forcePerAttachment = Gen1Tile.TARGET_FORCE / count
    )
}

/**
 * The smallest attachment count that keeps the model flat, searched over the whole
 * `(columns × rows)` rectangle and reported against the square-grid answer `C-0009` found.
 */
private fun flatnessMinimum(
    model: String,
    scan: List<RectangularGridRecord>,
    flat: (RectangularGridRecord) -> Boolean,
    dishing: (RectangularGridRecord) -> Double,
    crossovers: Int
): FlatnessMinimum {
    val square = scan.filter { it.attachmentColumns == it.attachmentRows && flat(it) }
        .minOf { it.attachments }
    val best = scan.filter(flat).minByOrNull { it.attachments }!!
    return FlatnessMinimum(
        model = model,
        squareGridAttachments = square,
        bestShape = "${best.attachmentColumns} x ${best.attachmentRows} (columns x rows)",
        bestAttachments = best.attachments,
        bestPeakDishingOverStroke = dishing(best),
        bestForcePerAttachment = best.forcePerAttachment,
        bestPeakCrossoverForce = best.latticePeakCrossoverForce,
        attachmentsPerCrossover = best.attachments.toDouble() / crossovers,
        crossovers = crossovers,
        savingAgainstSquareGrid = 1.0 - best.attachments.toDouble() / square
    )
}

private fun transposition(
    a: Int,
    b: Int,
    lattice: OrigamiGrillage,
    plate: PlateOnFoundation,
    sheet: OrigamiSheet,
    stroke: Double
): TranspositionRecord {
    val first = commensurabilityRecord(a, b, lattice, plate, sheet, stroke)
    val second = commensurabilityRecord(b, a, lattice, plate, sheet, stroke)
    return TranspositionRecord(
        attachments = a * b,
        shapeA = "$a x $b (columns x rows)",
        shapeB = "$b x $a (columns x rows)",
        rowOffsetSpreadA = first.rowOffsetSpread,
        rowOffsetSpreadB = second.rowOffsetSpread,
        latticeDishingA = first.latticePeakDishing,
        latticeDishingB = second.latticePeakDishing,
        latticeRatio = second.latticePeakDishing / first.latticePeakDishing,
        plateDishingA = first.platePeakDishing,
        plateDishingB = second.platePeakDishing,
        plateRatio = second.platePeakDishing / first.platePeakDishing,
        meanRowOffsetA = first.meanRowOffset,
        meanRowOffsetB = second.meanRowOffset,
        latticeExcessOverPlate = (second.latticePeakDishing / first.latticePeakDishing) /
                (second.platePeakDishing / first.platePeakDishing),
        commensurateWins = if (first.rowOffsetSpread < second.rowOffsetSpread)
            first.latticePeakDishing < second.latticePeakDishing
        else second.latticePeakDishing < first.latticePeakDishing
    )
}

/**
 * The same attachment grid placed on the duplex axes and on the interfaces — the direct
 * form of the "on a crossover or between" question, at identical count and identical force.
 */
private fun snappedGrid(
    columns: Int,
    rows: Int,
    lattice: OrigamiGrillage,
    sheet: OrigamiSheet,
    shift: Double
): List<PointLoad> {
    val force = Gen1Tile.TARGET_FORCE / (columns * rows)
    return insetRectangle(columns, rows, Gen1Tile.EDGE_X, lattice.lengthY).map { (x, y) ->
        val duplex = Math.round((y - lattice.beamY[0]) / sheet.interhelicalDistance)
        PointLoad(x, lattice.beamY[0] + duplex * sheet.interhelicalDistance + shift, force)
    }
}

private fun axisVersusInterface(
    columns: Int,
    rows: Int,
    lattice: OrigamiGrillage,
    plate: PlateOnFoundation,
    sheet: OrigamiSheet
): AxisVersusInterfaceRecord {
    val half = sheet.interhelicalDistance / 2.0
    val onAxisLoads = snappedGrid(columns, rows, lattice, sheet, 0.0)
    val onInterfaceLoads = snappedGrid(columns, rows, lattice, sheet, half)
    val onAxis = lattice.solve(pointLoads = onAxisLoads)
    val onInterface = lattice.solve(pointLoads = onInterfaceLoads)
    val plateAxis = plate.solve(pointLoads = onAxisLoads).peakDishing()
    val plateInterface = plate.solve(pointLoads = onInterfaceLoads).peakDishing()
    val latticeRatio = onInterface.peakDishing() / onAxis.peakDishing()
    val plateRatio = plateInterface / plateAxis
    return AxisVersusInterfaceRecord(
        attachmentColumns = columns,
        attachmentRows = rows,
        onDuplexAxisDishing = onAxis.peakDishing(),
        onInterfaceDishing = onInterface.peakDishing(),
        onDuplexAxisPeakCrossover = onAxis.peakCrossoverForce,
        onInterfacePeakCrossover = onInterface.peakCrossoverForce,
        dishingRatio = latticeRatio,
        crossoverForceRatio = onInterface.peakCrossoverForce / onAxis.peakCrossoverForce,
        plateOnDuplexAxisDishing = plateAxis,
        plateOnInterfaceDishing = plateInterface,
        plateDishingRatio = plateRatio,
        latticeExcessOverPlate = latticeRatio / plateRatio
    )
}

// --------------------------------------------------------------------------- gates

/**
 * The nominal registration map, binned by distance to the nearest crossover in half-nm steps.
 */
private fun crossoverProximity(map: List<RegistrationPoint>): List<CrossoverProximityRecord> =
    map.groupBy { Math.round(it.nearestCrossoverDistance * 2.0) / 2.0 }
        .toSortedMap()
        .map { (bin, points) ->
            CrossoverProximityRecord(
                distanceBin = bin,
                points = points.size,
                meanAnchoredPeakCrossover =
                    points.sumOf { it.anchoredPeakCrossoverStiffAnchor } / points.size,
                minAnchoredPeakCrossover = points.minOf { it.anchoredPeakCrossoverStiffAnchor },
                maxAnchoredPeakCrossover = points.maxOf { it.anchoredPeakCrossoverStiffAnchor },
                meanAnchoredPeakDuplexShear =
                    points.sumOf { it.anchoredPeakDuplexShear } / points.size,
                meanLeverPeakCrossover = points.sumOf { it.leverPeakCrossover } / points.size,
                minLeverPeakCrossover = points.minOf { it.leverPeakCrossover },
                maxLeverPeakCrossover = points.maxOf { it.leverPeakCrossover },
                meanLeverPeakDuplexShear =
                    points.sumOf { it.leverPeakDuplexShear } / points.size
            )
        }

/**
 * Gate 3: the registration variable has to be **lattice-periodic**, or what is being called
 * registration is really position-in-tile.
 *
 * Translating the anchor by a lattice vector of the crossover pattern — `(p, 0)` or the
 * centring vector `(p/2, d)` — must leave the peak force alone up to the finite-tile
 * contamination, which is what this measures rather than assumes.
 */
private fun periodicity(
    lattice: OrigamiGrillage,
    sheet: OrigamiSheet,
    pressure: Double
): List<PeriodicityRecord> {
    val stiffness = ANCHOR_FRACTION_STIFF * lattice.foundationStiffness * lattice.area
    val load = uniformPressure(pressure)
    fun peak(x: Double, y: Double): Double =
        lattice.solveWithAnchor(PointSupport(x, y, stiffness), load).peakCrossoverForce
    val p = sheet.crossoverSpacing
    val d = sheet.interhelicalDistance
    // the starting points are deliberately NOT symmetric about the origin. At the nominal
    // phase the lattice is centro-symmetric, so a translation from -v/2 to +v/2 would be the
    // point inversion and would return an exactly zero residual whatever the periodicity —
    // it would test the symmetry group rather than the registration variable.
    val starts = listOf(0.0 to 0.0, 1.36 to 0.6725, -2.04 to -1.0)
    return listOf(
        Triple("one per-interface spacing along the helices, (p, 0)", p, 0.0),
        Triple("the centring vector, (p/2, d)", p / 2.0, d),
        Triple("two duplexes across, (0, 2d)", 0.0, 2.0 * d)
    ).flatMap { (name, dx, dy) ->
        starts.map { (x0, y0) ->
            val here = peak(x0, y0)
            val there = peak(x0 + dx, y0 + dy)
            PeriodicityRecord(
                translation = "$name, from ($x0, $y0)",
                alongCell = x0,
                acrossCell = y0,
                peakCrossoverHere = here,
                peakCrossoverTranslated = there,
                residual = abs(there - here) / here
            )
        }
    }
}

/**
 * The dishing and the peak per-load-path force of one attachment grid as a **continuous
 * function of where its rows sit relative to the duplex axes** — the direct form of the
 * "on a crossover or between" question, at fixed attachment count, fixed force per
 * attachment, fixed spacing and fixed aspect ratio.
 *
 * This is what turns the commensurability metric from a correlation into a mechanism: if
 * dishing is a smooth periodic function of the row offset with period `d`, then a row count
 * that parks every row at the same offset can sit at the minimum of that function, and one
 * that spreads its rows over the period cannot.
 */
private fun attachmentRowPhase(
    columns: Int,
    rows: Int,
    steps: Int,
    lattice: OrigamiGrillage,
    plate: PlateOnFoundation,
    sheet: OrigamiSheet,
    stroke: Double
): List<AttachmentRowPhaseRecord> {
    val d = sheet.interhelicalDistance
    val onAxisLoads = snappedGrid(columns, rows, lattice, sheet, 0.0)
    val onAxis = lattice.solve(pointLoads = onAxisLoads)
    val plateOnAxis = plate.solve(pointLoads = onAxisLoads).peakDishing()
    return (0 until steps).map { k ->
        val shift = (k.toDouble() / (steps - 1) - 0.5) * d
        val loads = snappedGrid(columns, rows, lattice, sheet, shift)
        val solution = lattice.solve(pointLoads = loads)
        val platePeak = plate.solve(pointLoads = loads).peakDishing()
        val latticeRelative = solution.peakDishing() / onAxis.peakDishing()
        val plateRelative = platePeak / plateOnAxis
        AttachmentRowPhaseRecord(
            attachmentColumns = columns,
            attachmentRows = rows,
            rowOffset = shift,
            rowOffsetInDuplexes = shift / d,
            peakDishing = solution.peakDishing(),
            peakDishingOverStroke = solution.peakDishing() / stroke,
            peakCrossoverForce = solution.peakCrossoverForce,
            dishingRelativeToOnAxis = latticeRelative,
            crossoverForceRelativeToOnAxis =
                solution.peakCrossoverForce / onAxis.peakCrossoverForce,
            platePeakDishing = platePeak,
            plateDishingRelativeToOnAxis = plateRelative,
            latticeExcessOverPlate = latticeRelative / plateRelative
        )
    }
}

/**
 * Gate 4: the phase that pushes a crossover column closest to the tile edge makes the
 * shortest beam element in the whole sweep, and is the only place the mesh could bite.
 */
private fun phaseConvergence(
    sheet: OrigamiSheet,
    geometry: List<PhaseGeometry>,
    pressure: Double
): List<PhaseConvergenceRecord> {
    val tightest = geometry.minByOrNull { it.nearestColumnToEdge }!!
    val loosest = geometry.maxByOrNull { it.nearestColumnToEdge }!!
    val foundation = Gen1Tile.FOUNDATION_SECANT
    return listOf(tightest, loosest).flatMap { phase ->
        listOf(1, 2, 4).map { subdivisions ->
            val lattice = latticeAtPhase(sheet, foundation, phase.basePairs, subdivisions)
            val stiffness = ANCHOR_FRACTION_STIFF * foundation * lattice.area
            PhaseConvergenceRecord(
                basePairs = phase.basePairs,
                nearestColumnToEdge = phase.nearestColumnToEdge,
                subdivisions = subdivisions,
                leverPeakCrossover = lattice.solve(
                    pointLoads = listOf(PointLoad(0.0, 0.0, Gen1Tile.TARGET_FORCE))
                ).peakCrossoverForce,
                anchoredPeakCrossover = lattice.solveWithAnchor(
                    PointSupport(0.0, 0.0, stiffness), uniformPressure(pressure)
                ).peakCrossoverForce
            )
        }
    }
}

// --------------------------------------------------------------------------- verdicts

private fun verdicts(
    nominal: LayerStateCase,
    foundationStates: List<LayerStateCase>,
    hingeStates: List<LayerStateCase>,
    cornerStates: List<LayerStateCase>,
    commensurability: List<CommensurabilityRecord>,
    transpositions: List<TranspositionRecord>,
    axisVersusInterface: List<AxisVersusInterfaceRecord>,
    rowPhase: List<AttachmentRowPhaseRecord>
): Map<String, String> {
    val everyState = foundationStates + hingeStates + cornerStates
    val anchored = everyState.map { it.loadClass(ANCHORED_STIFF) }
    val worstEverywhere = anchored.maxOf { it.jointWorstForce }
    val bestWorstCase = anchored.maxOf { it.jointBestForce }
    val bestColumns = anchored.map { it.jointBestColumns }.distinct()
    val worstColumns = anchored.map { it.jointWorstColumns }.distinct()
    val nominalAnchored = nominal.loadClass(ANCHORED_STIFF)
    val nominalLever = nominal.loadClass(LEVER)
    val plateMonotone = commensurability.zipWithNext()
        .all { (a, b) -> b.platePeakDishing < a.platePeakDishing }
    val latticeAnomalies = commensurability.zipWithNext()
        .filter { (a, b) -> b.latticePeakDishing > a.latticePeakDishing }
        .map { (a, b) ->
            "${b.attachments} (${b.distinctRowOffsets} offsets) above " +
                    "${a.attachments} (${a.distinctRowOffsets} offsets)"
        }
    return mapOf(
        "design-lever-size" to (
                "Over the complete 32 base-pair phase sweep and the complete one-crossover " +
                        "registration cell, the peak per-load-path force spans %.3f to %.3f pN " +
                        "at a discrete anchor (a factor of %.2f) and %.2f to %.2f pN at a " +
                        "concentrated attachment (a factor of %.2f), at the design point — " +
                        "chosen entirely by the staple layout, at no cost in material, force " +
                        "or stroke."
                ).format(
                nominalAnchored.jointBestForce, nominalAnchored.jointWorstForce,
                nominalAnchored.jointRatio,
                nominalLever.jointBestForce, nominalLever.jointWorstForce,
                nominalLever.jointRatio
            ),
        "margin-to-unzip" to (
                "The worst layout anywhere in the k_f and k_theta sweeps puts %.2f pN on one " +
                        "crossover; the best layout's worst case anywhere is %.2f pN. Against " +
                        "the 10-15 pN unzip band those are margins of %.2f and %.2f to the " +
                        "lower edge, and %.2f and %.2f to the upper. So layout alone %s keep " +
                        "the worst case clear of the unzip band."
                ).format(
                worstEverywhere, bestWorstCase,
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / worstEverywhere,
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / bestWorstCase,
                DUPLEX_UNZIP_UPPER / worstEverywhere, DUPLEX_UNZIP_UPPER / bestWorstCase,
                if (bestWorstCase < Gen1Tile.DUPLEX_UNZIP_ALLOWABLE) "DOES" else "does NOT"
            ),
        "is-it-lattice-geometry" to (
                "Across every state of the layer and the joint in the sweep the best layout " +
                        "for a discrete anchor has %s crossover columns and the worst has %s, " +
                        "so the COARSE ranking is pure lattice geometry and moves with neither " +
                        "k_f nor k_theta. Within a column count the fine ranking does move — " +
                        "the best base-pair phase is one of %s — but the spread within a count " +
                        "is under 1 %% at the design point. The FORCES are not invariant and " +
                        "are reported per state."
                ).format(
                bestColumns.joinToString("/"), worstColumns.joinToString("/"),
                anchored.map { it.jointBestPhaseBasePairs }.distinct().sorted()
            ),
        "levers-compose" to (
                "For a discrete anchor at the design point, registration alone spans %.2f at " +
                        "the best phase and %.2f at the worst, while the phase alone spans " +
                        "%.3f at fixed best registration and %.3f at fixed worst registration. " +
                        "The joint span is %.2f against %.2f for the separable product, i.e. " +
                        "%.0f %% of it, so the two levers %s. Registration is the large lever " +
                        "and phase the small one."
                ).format(
                nominalAnchored.registrationRatioAtBestPhase,
                nominalAnchored.registrationRatioAtWorstPhase,
                nominalAnchored.phaseRatioAtBestRegistration,
                nominalAnchored.phaseRatioAtWorstRegistration,
                nominalAnchored.jointRatio, nominalAnchored.separableProduct,
                100.0 * nominalAnchored.jointRatio / nominalAnchored.separableProduct,
                if (nominalAnchored.jointRatio > 0.9 * nominalAnchored.separableProduct)
                    "compose almost independently" else "TRADE against each other"
            ),
        "flatness-commensurability" to (
                "The continuum plate's flatness curve is %s, so every non-monotonicity is a " +
                        "lattice property: the lattice curve rises at %s. The transposition " +
                        "test at fixed attachment count and fixed force per attachment gives " +
                        "the row-commensurate arrangement a lattice-specific advantage of up " +
                        "to x%.2f with the plate divided out, against x%.2f for the control " +
                        "pairs whose two members are equally registered, and the " +
                        "row-phase sweep at fixed count makes dishing a smooth periodic " +
                        "function of the offset with period d, worse at the interfaces than " +
                        "on the axes by x%.2f on the lattice — but only x%.2f once the plate, " +
                        "which feels the same shift toward the free edge, is divided out."
                ).format(
                if (plateMonotone) "monotone at every one of the ${commensurability.size} grids"
                else "NOT monotone, which refutes the lattice attribution",
                latticeAnomalies.joinToString("; "),
                transpositions.maxOf {
                    maxOf(it.latticeExcessOverPlate, 1.0 / it.latticeExcessOverPlate)
                },
                transpositions.filter { abs(it.meanRowOffsetA - it.meanRowOffsetB) < 1e-9 }
                    .maxOf {
                        maxOf(it.latticeExcessOverPlate, 1.0 / it.latticeExcessOverPlate)
                    },
                rowPhase.maxOf { it.dishingRelativeToOnAxis },
                rowPhase.maxOf { it.latticeExcessOverPlate }
            ),
        "design-rule" to (
                "Make the number of attachment ROWS a divisor of the duplex count, so that " +
                        "every attachment sits exactly on a duplex axis. At fixed attachment " +
                        "count and fixed force per attachment the on-axis arrangement beats " +
                        "an incommensurate one by a lattice-specific factor of up to x%.2f in " +
                        "peak dishing, with the plate's anisotropy and aspect-ratio preference " +
                        "divided out. The CONTROLS behave: two row counts that both sit on " +
                        "axes differ by only x%.2f."
                ).format(
                transpositions.maxOf {
                    maxOf(it.latticeExcessOverPlate, 1.0 / it.latticeExcessOverPlate)
                },
                transpositions.filter { it.meanRowOffsetA < 1e-9 && it.meanRowOffsetB < 1e-9 }
                    .maxOfOrNull {
                        maxOf(it.latticeExcessOverPlate, 1.0 / it.latticeExcessOverPlate)
                    } ?: Double.NaN
            )
    )
}

// --------------------------------------------------------------------------- console report

private fun report(result: RegistrationResult, output: File, started: Long) {
    println("T-14 / A8.2 — ${result.title}")
    println("300 K, aqueous buffer, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    println()
    println("--- the cheap bound: the phase table, arithmetic only ".padEnd(110, '-'))
    println("%6s %8s %8s %11s %14s %12s %s".format(
        "bp", "phase", "columns", "crossovers", "D_perp change", "edge gap", "symmetry"
    ))
    result.phaseGeometry.forEach {
        println("%6d %8.2f %8d %11d %14.4f %12.3f %s".format(
            it.basePairs, it.phase, it.columns, it.crossovers,
            it.acrossHelixRigidityChange, it.nearestColumnToEdge, it.symmetryGroup
        ))
    }
    println()
    println("--- the phase sweep at the design point ".padEnd(110, '-'))
    println("%6s %8s %11s %13s %13s %13s %11s".format(
        "bp", "columns", "crossovers", "anchor[pN]", "best reg", "worst reg", "thermal[nm]"
    ))
    result.nominal.phases.forEach {
        val band = it.registration.first { r -> r.loadCase == "anchored, k_a = 10 k_f A" }
        println("%6d %8d %11d %13.3f %13.3f %13.3f %11.4f".format(
            it.basePairs, it.columns, it.crossovers,
            it.centredAnchorPeakCrossoverStiffAnchor, band.best, band.worst,
            it.thermalDishingRms
        ))
    }
    println()
    println("--- the lever, state by state, load reacted at a discrete anchor ".padEnd(110, '-'))
    println("%46s %7s %7s %7s %7s %8s %8s %7s".format(
        "state", "bestBP", "bestCol", "worstBP", "wrstCol", "best[pN]", "worst[pN]", "ratio"
    ))
    (result.foundationStates + result.hingeStates + result.cornerStates).forEach {
        val a = it.loadClass(ANCHORED_STIFF)
        println("%46s %7d %7d %7d %7d %8.3f %8.3f %7.2f".format(
            it.label.take(46), a.jointBestPhaseBasePairs, a.jointBestColumns,
            a.jointWorstPhaseBasePairs, a.jointWorstColumns,
            a.jointBestForce, a.jointWorstForce, a.jointRatio
        ))
    }
    println()
    println("--- the same, load entering at a concentrated attachment ".padEnd(110, '-'))
    println("%46s %7s %7s %7s %7s %8s %8s %7s".format(
        "state", "bestBP", "bestCol", "worstBP", "wrstCol", "best[pN]", "worst[pN]", "ratio"
    ))
    (result.foundationStates + result.hingeStates + result.cornerStates).forEach {
        val a = it.loadClass(LEVER)
        println("%46s %7d %7d %7d %7d %8.3f %8.3f %7.2f".format(
            it.label.take(46), a.jointBestPhaseBasePairs, a.jointBestColumns,
            a.jointWorstPhaseBasePairs, a.jointWorstColumns,
            a.jointBestForce, a.jointWorstForce, a.jointRatio
        ))
    }
    println()
    println("--- the nominal registration cell, both load classes ".padEnd(110, '-'))
    result.nominal.loadClasses.forEach {
        println(("%32s: best %7.3f pN at (%6.2f, %6.2f) %-46s\n" +
                "%32s  worst %7.3f pN at (%6.2f, %6.2f) %-46s").format(
            it.loadCase, it.jointBestForce,
            result.nominal.phases.first { p -> p.basePairs == it.jointBestPhaseBasePairs }
                .registration.first { r -> r.loadCase == it.loadCase }.bestAlongCell,
            result.nominal.phases.first { p -> p.basePairs == it.jointBestPhaseBasePairs }
                .registration.first { r -> r.loadCase == it.loadCase }.bestAcrossCell,
            it.jointBestPlacement, "", it.jointWorstForce,
            result.nominal.phases.first { p -> p.basePairs == it.jointWorstPhaseBasePairs }
                .registration.first { r -> r.loadCase == it.loadCase }.worstAlongCell,
            result.nominal.phases.first { p -> p.basePairs == it.jointWorstPhaseBasePairs }
                .registration.first { r -> r.loadCase == it.loadCase }.worstAcrossCell,
            it.jointWorstPlacement
        ))
    }
    println()
    println("--- commensurability of the attachment grid with the duplex lattice ".padEnd(110, '-'))
    println("%6s %6s %7s %8s %8s %8s %8s %10s %10s %8s".format(
        "cols", "rows", "count", "offsets", "spread", "mean|o|", "max|o|",
        "lattice/S", "plate/S", "L/P"
    ))
    result.commensurability.forEach {
        println("%6d %6d %7d %8d %8.3f %8.3f %8.3f %10.4f %10.4f %8.3f".format(
            it.attachmentColumns, it.attachmentRows, it.attachments,
            it.distinctRowOffsets, it.rowOffsetSpread, it.meanRowOffset, it.maximumRowOffset,
            it.latticeOverStroke, it.plateOverStroke, it.latticeOverPlate
        ))
    }
    println()
    println("--- lattice/plate excess over the whole (columns x rows) rectangle ".padEnd(110, '-'))
    println("%6s".format("c\\r") + GRID_SIDES.joinToString("") { "%7d".format(it) })
    GRID_SIDES.forEach { columns ->
        println("%6d".format(columns) + GRID_SIDES.joinToString("") { rows ->
            "%7.3f".format(
                result.rectangularScan.first {
                    it.attachmentColumns == columns && it.attachmentRows == rows
                }.latticeOverPlate
            )
        })
    }
    println("        rows:   " + GRID_SIDES.joinToString("") {
        "%7.3f".format(attachmentOffsetSpread(it, BEAM_COUNT))
    } + "   <- row offset spread")
    println()
    println("--- the cheapest flat attachment scheme, over grid SHAPES and not only counts ".padEnd(110, '-'))
    result.flatnessMinimum.forEach {
        println(("%16s: square grid needs %3d; best shape %-24s needs %3d (%.1f %% fewer), " +
                "dishing %.4f of stroke, %.2f pN each, %.2f attachments per crossover").format(
            it.model, it.squareGridAttachments, it.bestShape, it.bestAttachments,
            100.0 * it.savingAgainstSquareGrid, it.bestPeakDishingOverStroke,
            it.bestForcePerAttachment, it.attachmentsPerCrossover
        ))
    }
    println()
    println("--- transposed grids of equal attachment count ".padEnd(110, '-'))
    println("%16s %16s %8s %8s %8s %8s %9s %9s %8s".format(
        "shape A", "shape B", "meanA", "meanB", "sprdA", "sprdB", "L B/A", "P B/A", "excess"
    ))
    result.transpositions.forEach {
        println("%16s %16s %8.3f %8.3f %8.3f %8.3f %9.3f %9.3f %8.3f".format(
            it.shapeA.substringBefore(" ("), it.shapeB.substringBefore(" ("),
            it.meanRowOffsetA, it.meanRowOffsetB,
            it.rowOffsetSpreadA, it.rowOffsetSpreadB,
            it.latticeRatio, it.plateRatio, it.latticeExcessOverPlate
        ))
    }
    println()
    println("--- attachments on the duplex axes against attachments on the interfaces ".padEnd(110, '-'))
    result.axisVersusInterface.forEach {
        println(("%2d x %2d: lattice dishing %.4f -> %.4f nm (x%.3f), plate x%.3f, " +
                "EXCESS x%.3f; crossover %.3f -> %.3f pN (x%.3f)").format(
                it.attachmentColumns, it.attachmentRows, it.onDuplexAxisDishing,
                it.onInterfaceDishing, it.dishingRatio, it.plateDishingRatio,
                it.latticeExcessOverPlate, it.onDuplexAxisPeakCrossover,
                it.onInterfacePeakCrossover, it.crossoverForceRatio
            ))
    }
    println()
    println("--- the registration map by distance to the nearest crossover ".padEnd(110, '-'))
    println("%9s %7s %11s %11s %11s %11s".format(
        "dist[nm]", "points", "anchor[pN]", "anchorV[pN]", "lever[pN]", "leverV[pN]"
    ))
    result.crossoverProximity.forEach {
        println("%9.1f %7d %11.3f %11.3f %11.3f %11.3f".format(
            it.distanceBin, it.points, it.meanAnchoredPeakCrossover,
            it.meanAnchoredPeakDuplexShear, it.meanLeverPeakCrossover,
            it.meanLeverPeakDuplexShear
        ))
    }
    println()
    println("--- attachment row offset from the duplex axes, at fixed count ".padEnd(110, '-'))
    println("%6s %6s %10s %12s %11s %11s %10s %12s".format(
        "cols", "rows", "offset/d", "dishing[nm]", "L vs axis", "P vs axis", "excess",
        "crossover[pN]"
    ))
    result.attachmentRowPhase.forEach {
        println("%6d %6d %10.4f %12.4f %11.4f %11.4f %10.4f %12.4f".format(
            it.attachmentColumns, it.attachmentRows, it.rowOffsetInDuplexes,
            it.peakDishing, it.dishingRelativeToOnAxis, it.plateDishingRelativeToOnAxis,
            it.latticeExcessOverPlate, it.peakCrossoverForce
        ))
    }
    println()
    println("--- gate 3: lattice periodicity of the registration variable ".padEnd(110, '-'))
    result.periodicity.forEach {
        println("%62s  %8.4f -> %8.4f pN  residual %6.2f %%".format(
            it.translation, it.peakCrossoverHere, it.peakCrossoverTranslated,
            100.0 * it.residual
        ))
    }
    println()
    println("--- gate 4: convergence at the tightest and loosest phase ".padEnd(110, '-'))
    result.phaseConvergence.forEach {
        println("bp %2d, edge gap %6.3f nm, subdivisions %d: lever %8.4f pN, anchored %8.4f pN"
            .format(
                it.basePairs, it.nearestColumnToEdge, it.subdivisions,
                it.leverPeakCrossover, it.anchoredPeakCrossover
            ))
    }
    println()
    result.verdict.forEach { (key, value) -> println("$key: $value"); println() }
    println("written: ${output.path} in ${elapsed(started)}")

    // the falsifier T-5 wired in and every structural task since has inherited
    val uniform = latticeAtPhase(sheetOf(1.0), Gen1Tile.FOUNDATION_SECANT, 3)
    val flat = uniform.solve(uniformPressure(Gen1Tile.TARGET_FORCE / uniform.area))
    check(flat.peakDishing() < 1e-9) {
        "a uniform load must dish no lattice at any phase; the assembly is wrong if it does"
    }
    check(result.nominal.phases.size == CrossoverLayout.BASE_PAIRS_PER_PERIOD) {
        "the phase sweep must be complete over the 32 bp period"
    }
    check(result.nominal.loadClasses.minOf { min(it.jointBestForce, it.jointWorstForce) } > 0.0) {
        "a peak per-load-path force must be positive"
    }
}
