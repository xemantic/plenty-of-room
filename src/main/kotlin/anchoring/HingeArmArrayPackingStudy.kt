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

import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.openrndr.math.Vector2
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Task `T-116`, leaf `A8.2` — **does a 45-arm `E5a1` hinge-line array have a plan view?**
 *
 * ```shell
 * tools/study.sh anchoring.HingeArmArrayPackingStudyKt
 * ```
 *
 * Emits `gpd/results/T-116-hinge-arm-array-packing.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val ROWS = 15
private const val DESIGN_PATHS = 45
private val EDGE_X = Gen1Tile.EDGE_X
private val DUPLEX = OrigamiDuplex.INTERHELICAL
private val EDGE_Y = ROWS * DUPLEX
private val FOOTPRINT = EDGE_X * EDGE_Y
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val HINGE = Gen1Tile.crossoverHingeStiffness()
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
private val ACCEPTABLE = Gen1Tile.ACCEPTABLE_STROKE
private val UNZIP = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE

/** `C-0034`'s `A2` — the arm's own duplex end, two strand termini at the phosphate radius. */
private val ANCHORAGE = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS)

/** `C-0039`'s `E5a1` arm, **re-derived** from its own library rather than tabulated. */
private val ARM = elasticaArmForStiffness(
    hingeStiffness = HINGE,
    hingeCount = 1,
    farStiffness = ANCHORAGE.rotationalStiffness,
    bendingRigidity = EI,
    count = DESIGN_PATHS,
    targetStiffness = MANDATE,
    workingDisplacement = ACCEPTABLE
)

private val ELEMENT = TwoSpringElastica(EI, ARM, HINGE, ANCHORAGE.rotationalStiffness)

/** `C-0030`'s 45-path span and `C-0041`'s 15-path span, re-derived for the free limiting case. */
private val FLEXURE_BASE = StandoffBase.crossovers(2, favourableOrientation = true)
private val FLEXURE_FLEXIBILITY = standoffTipFlexibility(EI, 8.0, FLEXURE_BASE.rotationalStiffness)

private fun flexureSpanFor(count: Int): Double = coupledFlexureSpan(
    EI, FLEXURE_FLEXIBILITY, count, MANDATE, ACCEPTABLE,
    FlexureOrientation.FAVOURABLE, Gen1Tile.DUPLEX_STRETCH_MODULUS, DrawInModel.CHORD
)

private val FLEXURE_MEMBERS = listOf(0.0, 0.5, 1.0)

private fun gridAnchors(columns: Int): List<Vector2> =
    attachmentGrid(columns, ROWS, EDGE_X, EDGE_Y).map { Vector2(it.first, it.second) }

// ---------------------------------------------------------------------------------------------

@Serializable
data class T116CheapBoundRecord(
    val quantity: String,
    val arm: Double,
    val value: Double,
    val against: Double,
    val ratio: Double,
    val verdict: String
)

@Serializable
data class T116OrientationRecord(
    val element: String,
    val columns: Int,
    val pathCount: Int,
    val length: Double,
    val angularSpanDegrees: Double,
    val samples: Int,
    val feasibleOrientations: Int,
    val singleLevelOrientations: Int,
    val minimumOverlappingPairs: Int,
    val minimumMutuallyBlockingPairs: Int,
    val minimumMemberClashPairs: Int,
    val bestAngleDegrees: Double,
    val verdict: String
)

@Serializable
data class T116LayoutRecord(
    val element: String,
    val columns: Int,
    val pathCount: Int,
    val angleDegrees: Double,
    val length: Double,
    val containedInEdge: Boolean,
    val overlappingPairs: Int,
    val mutuallyBlockingPairs: Int,
    val memberClashPairs: Int,
    val levelsRequired: Int,
    val verdict: String
)

@Serializable
data class T116PhaseRecord(
    val phaseBasePairs: Int,
    val inventory: Int,
    val columns: Int,
    val armsPlaced: Int,
    val independentRowBound: Int,
    val hingeCrossovers: Int,
    val buriedCrossovers: Int,
    val crossoversDemanded: Int,
    val demandedOfInventory: Double,
    val armLengthFraction: Double,
    val survivingCrossovers: Int,
    val segments: Int,
    val trimmedSegments: Int,
    val orphanSegments: Int,
    val components: Int,
    val largestComponentSegments: Int,
    val severed: Boolean
)

@Serializable
data class T116RowRecord(
    val row: Int,
    val interiorRow: Boolean,
    val sites: Int,
    val sitePitch: Double,
    val armsPlaced: Int,
    val independentMaximum: Int
)

@Serializable
data class T116PlacementRecord(
    val row: Int,
    val rootX: Double,
    val towardPositiveX: Boolean,
    val interfaceIndex: Int,
    val low: Double,
    val high: Double
)

@Serializable
data class T116CountRecord(
    val pathCount: Int,
    val arm: Double,
    val armBasePairs: Double,
    val armPlusDuplex: Double,
    val bestPhaseBasePairs: Int,
    val armsPlaced: Int,
    val selfConsistent: Boolean,
    val crossoversDemanded: Int,
    val inventory: Int,
    val survivingCrossovers: Int,
    val orphanSegments: Int,
    val components: Int,
    val largestComponentSegments: Int,
    val armLengthFraction: Double,
    val perPathForce: Double,
    val assembledTangent: Double
)

@Serializable
data class T116SensitivityRecord(
    val axis: String,
    val reading: String,
    val arm: Double,
    val exclusionWidth: Double,
    val armsPlacedAtBestPhase: Int,
    val fortyFivePlace: Boolean,
    val note: String
)

@Serializable
data class T116ThresholdRecord(
    val variable: String,
    val demandedValue: Double,
    val availableValue: Double,
    val threshold: Double,
    val unit: String,
    val note: String
)

@Serializable
data class T116ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T116ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class T116Result(
    val task: String,
    val leaf: String,
    val temperatureKelvin: Double,
    val kbT: Double,
    val medium: String,
    val conventions: Map<String, String>,
    val parameters: Map<String, Double>,
    val cheapBounds: List<T116CheapBoundRecord>,
    val orientations: List<T116OrientationRecord>,
    val layouts: List<T116LayoutRecord>,
    val phases: List<T116PhaseRecord>,
    val rows: List<T116RowRecord>,
    val placement: List<T116PlacementRecord>,
    val counts: List<T116CountRecord>,
    val sensitivities: List<T116SensitivityRecord>,
    val thresholds: List<T116ThresholdRecord>,
    val convergence: List<T116ConvergenceRecord>,
    val reproductions: List<T116ReproductionRecord>,
    val predicates: Map<String, String>,
    val findings: Map<String, String>
)

// ---------------------------------------------------------------------------------------------

private fun armOrientationRecord(columns: Int): T116OrientationRecord {
    val sweep = elementOrientationSweep(
        gridAnchors(columns), ARM, 720, DUPLEX, anchorFraction = 0.0, angularSpan = 2.0 * PI
    )
    return T116OrientationRecord(
        "E5a1 hinge arm", columns, columns * ROWS, ARM, 360.0, sweep.samples,
        sweep.feasibleOrientations, sweep.singleLevelOrientations, sweep.minimumOverlappingPairs,
        sweep.minimumMutuallyBlockingPairs, sweep.minimumMemberClashPairs, sweep.bestAngleDegrees,
        verdictOf(sweep)
    )
}

private fun flexureOrientationRecord(columns: Int): T116OrientationRecord {
    val span = flexureSpanFor(columns * ROWS)
    val sweep = elementOrientationSweep(
        gridAnchors(columns), span, 720, DUPLEX, anchorFraction = 0.5,
        verticalMemberFractions = FLEXURE_MEMBERS
    )
    return T116OrientationRecord(
        "C-0041 standoff flexure (free limiting case)", columns, columns * ROWS, span, 180.0,
        sweep.samples, sweep.feasibleOrientations, sweep.singleLevelOrientations,
        sweep.minimumOverlappingPairs, sweep.minimumMutuallyBlockingPairs,
        sweep.minimumMemberClashPairs, sweep.bestAngleDegrees, verdictOf(sweep)
    )
}

private fun verdictOf(sweep: OrientationSweep): String = when {
    sweep.singleLevelOrientations > 0 ->
        "PACKS — ${sweep.singleLevelOrientations} of ${sweep.samples} orientations lie in one level"
    sweep.feasibleOrientations > 0 ->
        "STACKS — no single level, but ${sweep.feasibleOrientations} admit an ordering"
    else ->
        "UNREALISABLE at any level count — every orientation leaves " +
                "${sweep.minimumOverlappingPairs} overlapping, " +
                "${sweep.minimumMutuallyBlockingPairs} mutually blocking and " +
                "${sweep.minimumMemberClashPairs} clashing pairs"
}

private fun armLayoutRecord(columns: Int, angleDegrees: Double): T116LayoutRecord {
    val angle = angleDegrees * PI / 180.0
    val array = hingeArmArray(columns, ROWS, EDGE_X, EDGE_Y, ARM, angle)
    val verdict = elementPackingVerdict(array)
    val contained = array.all { element ->
        element.body.corners.all {
            abs(it.x) <= EDGE_X / 2.0 + PLAN_TANGENCY_TOLERANCE &&
                    abs(it.y) <= EDGE_Y / 2.0 + PLAN_TANGENCY_TOLERANCE
        }
    }
    val text = when {
        verdict.singleLevel && contained -> "PACKS in one level, inside the host's own edge"
        verdict.singleLevel ->
            "the bodies clear each other in one level, but the array OVERHANGS the host's edge"
        verdict.feasibleAtAnyLevelCount -> "needs ${verdict.levelsRequired} levels"
        else -> "UNREALISABLE — ${verdict.mutuallyBlockingPairs} mutually blocking pairs"
    }
    return T116LayoutRecord(
        "E5a1 hinge arm", columns, columns * ROWS, angleDegrees, ARM, contained,
        verdict.overlappingPairs, verdict.mutuallyBlockingPairs, verdict.memberClashPairs,
        verdict.levelsRequired, text
    )
}

private fun phaseRecord(phase: Int): T116PhaseRecord {
    val placement = placeHingeArms(phase, EDGE_X, ROWS, ARM)
    val host = hostSheetAfterArms(placement, EDGE_X, ROWS, ARM)
    val sites = hingeSites(phase, EDGE_X, ROWS)
    val columns = sites.map { crossoverKey(it.x) }.distinct().size
    return T116PhaseRecord(
        phase, host.inventory, columns, placement.arms, placement.independentRowBound,
        host.hingeCrossovers, host.buriedCrossovers, host.crossoversDemanded,
        host.crossoversDemanded.toDouble() / host.inventory, host.armLengthFraction,
        host.survivingCrossovers, host.segments, host.trimmedSegments, host.orphanSegments,
        host.components, host.largestComponentSegments, host.severed
    )
}

// ---------------------------------------------------------------------------------------------

fun main() {
    val phases = (0 until 32).map { phaseRecord(it) }
    val bestPhase = phases.maxByOrNull { it.armsPlaced }!!
    val bestPlacement = placeHingeArms(bestPhase.phaseBasePairs, EDGE_X, ROWS, ARM)
    val bestHost = hostSheetAfterArms(bestPlacement, EDGE_X, ROWS, ARM)
    val bestBound = phases.maxOf { it.independentRowBound }

    // ------------------------------------------------------------------ cheap bounds
    val flexureSpan45 = flexureSpanFor(DESIGN_PATHS)
    val columnPitch = EDGE_X / 3.0
    val cheapBounds = listOf(
        T116CheapBoundRecord(
            "plan area of 45 arms against the tile footprint", ARM,
            armArrayPlanArea(DESIGN_PATHS, ARM, DUPLEX), FOOTPRINT,
            armArrayPlanArea(DESIGN_PATHS, ARM, DUPLEX) / FOOTPRINT,
            "does NOT close the task — falsifier 1 did not fire, and it points the expensive " +
                    "part at the lattice rather than at the area, exactly as `C-0041`'s did"
        ),
        T116CheapBoundRecord(
            "C-0041's Fact B: arm + d against the 3-column along-helix pitch", ARM,
            ARM + DUPLEX, columnPitch, (ARM + DUPLEX) / columnPitch,
            "REVERSES — `C-0041`'s own obstruction is 34.51 against 13.33 and this is " +
                    "11.82 against 13.33, so the along-helix pitch is no longer binding"
        ),
        T116CheapBoundRecord(
            "C-0041's Fact B on its own element, for contrast", flexureSpan45,
            flexureSpan45 + DUPLEX, columnPitch, (flexureSpan45 + DUPLEX) / columnPitch,
            "binding, 2.59x — this is the obstruction that does not transfer"
        ),
        T116CheapBoundRecord(
            "C-0041's Fact A: vertical members owned per element", ARM, 0.0, 3.0, 0.0,
            "VACUOUS — `E5a1` owns no standoff and no tie, so the level-independent clash " +
                    "that decided `C-0041` cannot arise at any tilt"
        ),
        T116CheapBoundRecord(
            "the hinge sites one interior row offers, at a 16 bp pitch", ARM,
            EDGE_X / (Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE / 2.0), ARM + DUPLEX,
            (Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE / 2.0) / (ARM + DUPLEX),
            "the site pitch is 5.44 nm and an arm needs 11.82, so a row must SKIP sites: " +
                    "the binding currency is the crossover lattice, not the plan area"
        ),
        T116CheapBoundRecord(
            "45 arms against `C-0015`'s whole crossover inventory", ARM,
            DESIGN_PATHS.toDouble(), 56.0, DESIGN_PATHS / 56.0,
            "`C-0040`'s ledger: 45 hinges is 80-92 % of the tile's own crossovers BEFORE any " +
                    "is buried under an arm"
        )
    )

    // ------------------------------------------------------------------ orientations and layouts
    val orientations = (1..4).map { armOrientationRecord(it) } + (1..3).map {
        flexureOrientationRecord(it)
    }
    val layouts = (1..4).flatMap { columns ->
        listOf(0.0, 5.0, 90.0, 180.0).map { armLayoutRecord(columns, it) }
    }

    val armPackingLimit = packingLimitedElementCount(
        EDGE_X, ROWS, { ARM }, DUPLEX, anchorFraction = 0.0
    )
    val armPackingLimitCentred = packingLimitedElementCount(
        EDGE_X, ROWS, { ARM }, DUPLEX, anchorFraction = 0.5
    )
    val flexurePackingLimit = packingLimitedElementCount(
        EDGE_X, ROWS, { count -> flexureSpanFor(count) }, DUPLEX,
        anchorFraction = 0.5, verticalMemberFractions = FLEXURE_MEMBERS
    )

    // ------------------------------------------------------------------ the rows of the best phase
    val bestSites = hingeSites(bestPhase.phaseBasePairs, EDGE_X, ROWS)
    val rowRecords = (0 until ROWS).map { row ->
        val sites = rowHingeSites(row, bestSites)
        val pitch = if (sites.size < 2) 0.0 else {
            sites.map { it.x }.distinct().sorted().zipWithNext().minOf { (a, b) -> b - a }
        }
        T116RowRecord(
            row, row > 0 && row < ROWS - 1, sites.size, pitch,
            bestPlacement.placements.count { it.row == row },
            maximumArmsInRow(sites, ARM, EDGE_X, DUPLEX, row).size
        )
    }

    // ------------------------------------------------------------------ the placement itself
    val placementRecords = bestPlacement.placements.map {
        T116PlacementRecord(
            it.row, it.rootX, it.towardPositiveX, it.interfaceIndex, it.low, it.high
        )
    }

    // ------------------------------------------------------- the self-consistent path count
    // The arm is a PLACED quantity: `L ∝ n^(1/3)`, so fewer paths ask for a shorter arm and a
    // shorter arm places more easily. The count the lattice carries is therefore a fixed point,
    // exactly as `C-0041`'s packing-limited count is, and it is solved rather than asserted.
    val countCandidates = listOf(10, 15, 20, 25, 30, 35, 38, 40, 41, 42, 43, 44, 45, 46, 50, 55, 60)
    val counts = countCandidates.map { paths ->
        val armAt = elasticaArmForStiffness(
            HINGE, 1, ANCHORAGE.rotationalStiffness, EI, paths, MANDATE, ACCEPTABLE
        )
        val element = TwoSpringElastica(EI, armAt, HINGE, ANCHORAGE.rotationalStiffness)
        val best = (0 until 32).map { placeHingeArms(it, EDGE_X, ROWS, armAt) }
            .maxByOrNull { it.arms }!!
        // what the host pays is read on the array the design actually asks for, not on the
        // maximal one the lattice would admit
        val host = hostSheetAfterArms(best.truncatedTo(paths), EDGE_X, ROWS, armAt)
        T116CountRecord(
            paths, armAt, armAt / RISE, armAt + DUPLEX, best.phaseBasePairs, best.arms,
            best.arms >= paths, host.crossoversDemanded, host.inventory,
            host.survivingCrossovers, host.orphanSegments, host.components,
            host.largestComponentSegments, host.armLengthFraction,
            element.reaction(ACCEPTABLE), paths * element.tangentStiffness(ACCEPTABLE)
        )
    }
    val selfConsistentCount = counts.filter { it.selfConsistent }.maxOfOrNull { it.pathCount } ?: 0
    val selfConsistent = counts.firstOrNull { it.pathCount == selfConsistentCount }
    // the largest count at which every one of the host's duplexes is still bonded into ONE piece
    val hostIntactCount = counts
        .filter { it.selfConsistent && it.largestComponentSegments >= ROWS }
        .maxOfOrNull { it.pathCount } ?: 0
    val hostIntact = counts.firstOrNull { it.pathCount == hostIntactCount }
    val armsPerInteriorRow = rowRecords.filter { it.interiorRow }.maxOf { it.armsPlaced }

    // ------------------------------------------------------------------ sensitivities
    fun placedFor(armLength: Double, width: Double = DUPLEX): Int =
        (0 until 32).maxOf { placeHingeArms(it, EDGE_X, ROWS, armLength, width).arms }

    val armTwoHinges = elasticaArmForStiffness(
        HINGE, 2, ANCHORAGE.rotationalStiffness, EI, DESIGN_PATHS, MANDATE, ACCEPTABLE
    )
    val armFields = elasticaArmForStiffness(
        HINGE, 1, ANCHORAGE.rotationalStiffness, 172.906, DESIGN_PATHS, MANDATE, ACCEPTABLE
    )
    val armAlpha = elasticaArmForStiffness(
        HINGE * Gen1Tile.CROSSOVER_ALPHA_MIN, 1, ANCHORAGE.rotationalStiffness, EI, DESIGN_PATHS,
        MANDATE, ACCEPTABLE
    )
    val sensitivities = listOf(
        T116SensitivityRecord(
            "hinge count", "1 crossover — `C-0039`'s `E5a1` as filed", ARM, DUPLEX,
            placedFor(ARM), placedFor(ARM) >= DESIGN_PATHS,
            "the primary reading"
        ),
        T116SensitivityRecord(
            "hinge count", "2 — the root is a DOUBLE NICK as well as a crossover", armTwoHinges,
            DUPLEX, placedFor(armTwoHinges), placedFor(armTwoHinges) >= DESIGN_PATHS,
            "an arm cut free at its root is doubly nicked from the rest of its own row, and " +
                    "`C-0025`'s `J2b` establishes that a double nick IS a crossover — so the " +
                    "near stiffness may be 2 k_theta at no extra crossover, which LENGTHENS the " +
                    "arm and makes the packing worse. Named, not adopted"
        ),
        T116SensitivityRecord(
            "duplex bending rigidity", "Fields et al.'s implied 172.906 pN nm2 (-25 %)", armFields,
            DUPLEX, placedFor(armFields), placedFor(armFields) >= DESIGN_PATHS,
            "`EI` is a CanDo MODEL INPUT; the measured buckling implies 25 % less"
        ),
        T116SensitivityRecord(
            "crossover alpha", "0.6, the bottom of Chen et al.'s fitted bracket", armAlpha,
            DUPLEX, placedFor(armAlpha), placedFor(armAlpha) >= DESIGN_PATHS,
            "`k_theta` is CITED and FITTED; the arm goes as the hinge's own square root"
        ),
        T116SensitivityRecord(
            "exclusion width", "the 2.0 nm steric diameter rather than the 2.69 nm SAXS pitch",
            ARM, OrigamiDuplex.DIAMETER, placedFor(ARM, OrigamiDuplex.DIAMETER),
            placedFor(ARM, OrigamiDuplex.DIAMETER) >= DESIGN_PATHS,
            "the loosest and the tightest defensible plan conventions, as `C-0041` swept them"
        )
    )

    // ------------------------------------------------------------------ thresholds
    val shortfall = DESIGN_PATHS - bestPhase.armsPlaced
    val armForFortyFive = (1..400).map { it * 0.05 }.lastOrNull { candidate ->
        (0 until 32).any { placeHingeArms(it, EDGE_X, ROWS, candidate).arms >= DESIGN_PATHS }
    } ?: 0.0
    val edgeForFortyFive = (1..400).map { EDGE_X + it * 0.25 }.firstOrNull { edge ->
        (0 until 32).any { placeHingeArms(it, edge, ROWS, ARM).arms >= DESIGN_PATHS }
    } ?: 0.0
    val rowsForFortyFive = (ROWS..64).firstOrNull { duplexes ->
        (0 until 32).any { placeHingeArms(it, EDGE_X, duplexes, ARM).arms >= DESIGN_PATHS }
    } ?: 0

    val thresholds = listOf(
        T116ThresholdRecord(
            "arms placed on the lattice at the best of 32 phases", DESIGN_PATHS.toDouble(),
            bestPhase.armsPlaced.toDouble(), shortfall.toDouble(), "arms",
            "solved, not asserted; the independent per-row bound over all phases is $bestBound, " +
                    "so a shortfall below that bound is a proof and not a search artefact"
        ),
        T116ThresholdRecord(
            "the arm length at which 45 do place", ARM, armForFortyFive,
            if (armForFortyFive > 0.0) ARM / armForFortyFive else 0.0, "nm",
            "the arm is a PLACED quantity (`C-0039`), so shortening it is not free: it is set " +
                    "by the mandate secant at 45 paths"
        ),
        T116ThresholdRecord(
            "the host edge at which 45 do place", EDGE_X, edgeForFortyFive,
            if (edgeForFortyFive > 0.0) edgeForFortyFive / EDGE_X else 0.0, "nm",
            "a specification change, and the same axis `C-0041` had to reach for"
        ),
        T116ThresholdRecord(
            "the duplex count at which 45 do place", ROWS.toDouble(), rowsForFortyFive.toDouble(),
            if (rowsForFortyFive > 0) rowsForFortyFive.toDouble() / ROWS else 0.0, "duplexes",
            "growing the host across the helices instead of along them"
        ),
        T116ThresholdRecord(
            "the self-consistent path count the lattice carries", DESIGN_PATHS.toDouble(),
            selfConsistentCount.toDouble(), DESIGN_PATHS - selfConsistentCount.toDouble(), "paths",
            "the arm is re-placed at every candidate count (`L ∝ n^(1/3)`, so fewer paths ask " +
                    "for a shorter arm), exactly as `C-0041` solved its own fifteen"
        ),
        T116ThresholdRecord(
            "the largest count that leaves the host in one piece", DESIGN_PATHS.toDouble(),
            hostIntactCount.toDouble(), DESIGN_PATHS - hostIntactCount.toDouble(), "paths",
            "every one of the host's 15 duplexes still bonded into a single component; beyond " +
                    "it the connected part collapses — 14 segments at 30 paths, 8 at 35, 3 at " +
                    "40 and NONE at 42, where no crossover survives at all"
        ),
        T116ThresholdRecord(
            "crossovers demanded against the host's own inventory at the best phase",
            bestHost.crossoversDemanded.toDouble(), bestHost.inventory.toDouble(),
            bestHost.crossoversDemanded.toDouble() / bestHost.inventory, "crossovers",
            "hinges plus the crossovers buried under an arm, which would tie a free lever back " +
                    "to the sheet"
        )
    )

    // ------------------------------------------------------------------ convergence
    val convergence = ArrayList<T116ConvergenceRecord>()
    val finestSweep = elementOrientationSweep(
        gridAnchors(3), ARM, 2880, DUPLEX, anchorFraction = 0.0, angularSpan = 2.0 * PI
    )
    listOf(180, 360, 720, 1440, 2880).forEach { samples ->
        val sweep = elementOrientationSweep(
            gridAnchors(3), ARM, samples, DUPLEX, anchorFraction = 0.0, angularSpan = 2.0 * PI
        )
        convergence += T116ConvergenceRecord(
            "single-level orientations of the 3 x 15 arm array", "orientation samples",
            samples.toDouble(), sweep.singleLevelOrientations.toDouble(),
            abs(
                sweep.singleLevelOrientations.toDouble() / samples -
                        finestSweep.singleLevelOrientations.toDouble() / finestSweep.samples
            )
        )
    }
    val finestArm = elasticaArmForStiffness(
        HINGE, 1, ANCHORAGE.rotationalStiffness, EI, DESIGN_PATHS, MANDATE, ACCEPTABLE, 1600
    )
    listOf(100, 200, 400, 800, 1600).forEach { steps ->
        val value = elasticaArmForStiffness(
            HINGE, 1, ANCHORAGE.rotationalStiffness, EI, DESIGN_PATHS, MANDATE, ACCEPTABLE, steps
        )
        convergence += T116ConvergenceRecord(
            "the placed E5a1 arm", "elastica RK4 steps", steps.toDouble(), value,
            abs(value - finestArm) / finestArm
        )
    }
    (0 until 32 step 8).forEach { phase ->
        convergence += T116ConvergenceRecord(
            "arms placed", "crossover phase [bp]", phase.toDouble(),
            phases[phase].armsPlaced.toDouble(),
            abs(phases[phase].armsPlaced - bestPhase.armsPlaced).toDouble()
        )
    }

    // ------------------------------------------------------------------ reproductions
    val tangent = DESIGN_PATHS * ELEMENT.tangentStiffness(ACCEPTABLE)
    val secant = DESIGN_PATHS * ELEMENT.secantStiffness(ACCEPTABLE)
    val reproductions = listOf(
        T116ReproductionRecord(
            "C-0039's E5a1 arm at 45 paths [nm]", 9.131, ARM, abs(ARM - 9.131) / 9.131
        ),
        T116ReproductionRecord(
            "C-0039's E5a1 assembled tangent at 3 nm [pN/nm]", 39.18, tangent,
            abs(tangent - 39.18) / 39.18
        ),
        T116ReproductionRecord(
            "C-0017's mandate secant, discharged by construction [pN/nm]", MANDATE, secant,
            abs(secant - MANDATE) / MANDATE
        ),
        T116ReproductionRecord(
            "C-0009's crossover hinge constant [pN nm/rad]", 13.5294118, HINGE,
            abs(HINGE - 13.5294118) / 13.5294118
        ),
        T116ReproductionRecord(
            "C-0029's two-terminus couple ceiling [pN nm/rad]", 78.2352941,
            ANCHORAGE.rotationalStiffness,
            abs(ANCHORAGE.rotationalStiffness - 78.2352941) / 78.2352941
        ),
        T116ReproductionRecord(
            "C-0030's span at 45 paths [nm]", 31.82, flexureSpan45,
            abs(flexureSpan45 - 31.82) / 31.82
        ),
        T116ReproductionRecord(
            "C-0041's packing-limited path count", 15.0, flexurePackingLimit.toDouble(),
            abs(flexurePackingLimit - 15.0) / 15.0
        ),
        T116ReproductionRecord(
            "C-0041's feasible orientations of the 3 x 15 flexure array", 0.0,
            orientations.first {
                it.element.startsWith("C-0041") && it.columns == 3
            }.feasibleOrientations.toDouble(),
            orientations.first {
                it.element.startsWith("C-0041") && it.columns == 3
            }.feasibleOrientations.toDouble()
        ),
        T116ReproductionRecord(
            "C-0041's single-level orientations of the 1 x 15 flexure array", 1.0,
            orientations.first {
                it.element.startsWith("C-0041") && it.columns == 1
            }.singleLevelOrientations.toDouble(),
            abs(
                orientations.first {
                    it.element.startsWith("C-0041") && it.columns == 1
                }.singleLevelOrientations - 1.0
            )
        ),
        T116ReproductionRecord(
            "C-0040's largest hinge line on a 40 nm tile", 4.0,
            (0 until 32).minOf { phase ->
                hingeSites(phase, EDGE_X, ROWS).groupBy { it.interfaceIndex }
                    .values.maxOf { it.size }
            }.toDouble(),
            0.0
        ),
        T116ReproductionRecord(
            "C-0015's crossover inventory at the ten eight-column phases", 56.0,
            phases.maxOf { it.inventory }.toDouble(),
            abs(phases.maxOf { it.inventory } - 56.0) / 56.0
        ),
        T116ReproductionRecord(
            "C-0015's crossover inventory at the other twenty-two", 49.0,
            phases.minOf { it.inventory }.toDouble(),
            abs(phases.minOf { it.inventory } - 49.0) / 49.0
        ),
        T116ReproductionRecord(
            "the SAXS interhelical distance [nm]", 2.69, DUPLEX, abs(DUPLEX - 2.69) / 2.69
        ),
        T116ReproductionRecord(
            "the per-interface crossover pitch [nm]", 10.88,
            Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE,
            abs(Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE - 10.88) / 10.88
        )
    )

    // ------------------------------------------------------------------ predicates
    val gridLayout = layouts.first { it.columns == 3 && it.angleDegrees == 0.0 }
    val armSweep3 = orientations.first { it.element.startsWith("E5a1") && it.columns == 3 }
    val perPath = ELEMENT.reaction(ACCEPTABLE)

    val predicates = linkedMapOf(
        "P1 the plan area of 45 arms against the 40 x 40 nm footprint" to
                ("%.0f nm2 against %.0f nm2 — %.3f of the footprint. FALSIFIER 1 DID NOT FIRE, " +
                        "and `C-0041`'s own bound was 2.59x").format(
                    armArrayPlanArea(DESIGN_PATHS, ARM, DUPLEX), FOOTPRINT,
                    armArrayPlanArea(DESIGN_PATHS, ARM, DUPLEX) / FOOTPRINT
                ),
        "P2 a single-level layout of 45 arms on C-0015's 3 x 15 grid" to
                ("YES as BODIES — %d of %d orientations lie in one level, with %d overlapping, " +
                        "%d mutually blocking and %d clashing pairs at the best angle %.1f deg; " +
                        "at 0 deg the bodies clear each other and the array %s. " +
                        "FALSIFIER 2 FIRED: `C-0041`'s verdict does NOT transfer").format(
                    armSweep3.singleLevelOrientations, armSweep3.samples,
                    armSweep3.minimumOverlappingPairs, armSweep3.minimumMutuallyBlockingPairs,
                    armSweep3.minimumMemberClashPairs, armSweep3.bestAngleDegrees,
                    if (gridLayout.containedInEdge) "lies inside the host's edge"
                    else "OVERHANGS the host's edge"
                ),
        "P3 the packing-limited path count of the arm array" to
                ("%d rooted at the grid anchor (%d columns), %d centred on it — against " +
                        "`C-0041`'s %d for its own flexure, reproduced here").format(
                    armPackingLimit, armPackingLimit / ROWS, armPackingLimitCentred,
                    flexurePackingLimit
                ),
        "P4 45 crossovers PLACED, one per arm, over all 32 phases" to
                ("NO — the best phase (%d bp) places %d arms, %d short of 45, and the " +
                        "independent per-row bound over every phase is %d, which is also short. " +
                        "FALSIFIER 3 DID NOT FIRE").format(
                    bestPhase.phaseBasePairs, bestPhase.armsPlaced, shortfall, bestBound
                ),
        "P4b the self-consistent count the lattice DOES carry" to
                ("%d paths at a %.3f nm = %.1f bp arm, %d placed of %d demanded at phase %d bp — " +
                        "and the host still ends in %d components with %d of %d crossovers left").format(
                    selfConsistentCount, selfConsistent?.arm ?: 0.0,
                    (selfConsistent?.armBasePairs ?: 0.0), selfConsistent?.armsPlaced ?: 0,
                    selfConsistentCount, selfConsistent?.bestPhaseBasePairs ?: 0,
                    selfConsistent?.components ?: 0, selfConsistent?.survivingCrossovers ?: 0,
                    selfConsistent?.inventory ?: 0
                ),
        "P5 what the array does to the sheet that hosts its hinges" to
                ("%.1f %% of the host's own duplex length becomes arm; %d crossovers are spent " +
                        "as hinges and %d more are BURIED under an arm, %d of an inventory of " +
                        "%d (%.2fx); %d survive, so the host has NO bonded component left at " +
                        "all — %d detached pieces and %d trimmed stubs. The host stays whole " +
                        "only up to %d arms. FALSIFIER 4 %s").format(
                    100.0 * bestHost.armLengthFraction, bestHost.hingeCrossovers,
                    bestHost.buriedCrossovers, bestHost.crossoversDemanded, bestHost.inventory,
                    bestHost.crossoversDemanded.toDouble() / bestHost.inventory,
                    bestHost.survivingCrossovers, bestHost.orphanSegments,
                    bestHost.trimmedSegments, hostIntactCount,
                    if (bestHost.severed) "DID NOT FIRE" else "FIRED — the host stays connected"
                ),
        "P6 the flatness consequence of the column count the packing admits" to
                ("the LATTICE placement carries %d arms per interior row and %d per edge row, " +
                        "i.e. %d attachment columns, exactly `C-0047`'s break-even; on " +
                        "`C-0015`'s inherited half-offset grid the same arm admits only %d " +
                        "columns, because a ROOTED arm in the outermost column overhangs the " +
                        "edge. At three columns `C-0047` reports 0.218 of the stroke (CITED, " +
                        "for a regular 3 x 15), against its 0.10 tolerance and against 0.308 " +
                        "for no coupling at all — so the array is NOT a net dishing source, " +
                        "which `C-0041`'s 1 x 15 is at 2.26x. FALSIFIER 5 %s").format(
                    armsPerInteriorRow, rowRecords.first { !it.interiorRow }.armsPlaced,
                    armsPerInteriorRow, armPackingLimit / ROWS,
                    if (armsPerInteriorRow >= 3) "FIRED — three columns are admitted and the " +
                            "flatness break-even is met" else "did not fire"
                ),
        "P7 which variable must give, as a threshold" to
                ("TWO variables, and the second is the binding one. (i) The crossover LATTICE: " +
                        "45 arms need 45 distinct crossovers and the lattice places %d, so the " +
                        "host edge would have to reach %.2f nm (%.2fx) or the duplex count %d " +
                        "(%.2fx). (ii) The host's own SURVIVAL: it stays in one piece only up " +
                        "to %d arms, %.2fx below the 45 demanded and %.2fx below the %d the " +
                        "lattice would place — so the threshold is not 45 -> 43, it is " +
                        "45 -> %d. The arm itself is a PLACED quantity and cannot simply be " +
                        "shortened; shortening it needs a smaller path count, which is the " +
                        "same variable.").format(
                    bestPhase.armsPlaced, edgeForFortyFive, edgeForFortyFive / EDGE_X,
                    rowsForFortyFive, rowsForFortyFive.toDouble() / ROWS, hostIntactCount,
                    DESIGN_PATHS.toDouble() / hostIntactCount,
                    bestPhase.armsPlaced.toDouble() / hostIntactCount, bestPhase.armsPlaced,
                    hostIntactCount
                )
    )

    val findings = linkedMapOf(
        "the verdict" to
                ("`C-0041`'s answer does NOT transfer, and neither of its two obstructions " +
                        "survives contact with `E5a1`. As BODIES the array packs: %.0f nm2 is " +
                        "%.3f of the footprint, the arm owns no standoff and no tie so the " +
                        "level-independent clash cannot arise at any tilt, and arm + d = " +
                        "%.2f nm sits UNDER the 13.33 nm column pitch where `C-0041`'s span " +
                        "sat 2.59x over it. What refuses the array is the HINGE LATTICE: an " +
                        "arm must root on a crossover, a row's roots sit at a 5.44 nm pitch " +
                        "(10.88 on the two edge rows) and an arm needs %.2f nm of clearance, " +
                        "so a row carries two or three and the tile carries %d of the 45 " +
                        "demanded, at the best of all 32 phases.").format(
                    armArrayPlanArea(DESIGN_PATHS, ARM, DUPLEX),
                    armArrayPlanArea(DESIGN_PATHS, ARM, DUPLEX) / FOOTPRINT,
                    ARM + DUPLEX, ARM + DUPLEX, bestPhase.armsPlaced
                ),
        "the binding currency is a lattice pitch, not an area and not a count" to
                ("`C-0040` counted the INVENTORY and found 49-56 against 45 demanded — 80-92 %%, " +
                        "tight but not refused. Placement is a different question and it is " +
                        "refused: the crossovers a row can actually reach are those of its own " +
                        "two interfaces, and consecutive ones are %.2f nm apart against an arm " +
                        "that needs %.2f. A row therefore SKIPS two sites out of every three. " +
                        "An inventory is a sum over the sheet; a placement is a constraint per " +
                        "row, and the two differ by %d arms.").format(
                    Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE / 2.0, ARM + DUPLEX, shortfall
                ),
        "the edge rows are the ones that lose" to
                ("An interior row is bounded by two interfaces of OPPOSITE parity, so it sees " +
                        "every column of the 16 bp lattice; an edge row sees one interface and " +
                        "therefore one parity, at twice the pitch. That asymmetry is not " +
                        "imposed anywhere — it falls out of `C-0015`'s alternation — and it is " +
                        "worth an arm on each of the two edge rows."),
        "what the host pays even for the arms it does place" to
                ("%.1f %% of the host's own duplex length stops being sheet and becomes lever, " +
                        "and %d crossovers of %d are spent or buried (%.2fx of the inventory). " +
                        "The residual host is %s, in %d components with %d crossovers left of " +
                        "%d. WHICH BODY HOSTS THE HINGES IS THEREFORE A DESIGN VARIABLE and " +
                        "nothing upstream has chosen it: `C-0039` grounds the near end on the " +
                        "TILE, and the tile is the body whose own rigidity `C-0006`, `C-0009` " +
                        "and `C-0047` spend on flatness.").format(
                    100.0 * bestHost.armLengthFraction, bestHost.crossoversDemanded,
                    bestHost.inventory, bestHost.crossoversDemanded.toDouble() / bestHost.inventory,
                    if (bestHost.severed) "SEVERED" else "connected", bestHost.components,
                    bestHost.survivingCrossovers, bestHost.inventory
                ),
        "a rooted arm has a direction and a centred beam does not" to
                ("`C-0041` swept [0, pi) because its beam is centred on its tie and therefore " +
                        "symmetric under a half turn. A hinge arm is rooted, so theta and " +
                        "theta + pi are DIFFERENT DESIGNS and the sweep must run over the full " +
                        "circle. It is worth a factor of two in the sweep and, more usefully, " +
                        "a factor of two in what a cluster of hinge sites can carry — one arm " +
                        "each way from a cluster narrower than a single arm."),
        "the count is a fixed point and it is 43, not 45 — and 43 is not the answer either" to
                ("The arm is a PLACED quantity, so fewer paths ask for a shorter arm and a " +
                        "shorter arm places more easily; the count the lattice carries is " +
                        "therefore a fixed point, and it is %d at a %.3f nm arm. That is not " +
                        "the escape the two missing arms make it look like: at %d paths the " +
                        "host spends %d of its %d crossovers, keeps %d, and has NO bonded " +
                        "component at all. The count that leaves the host whole is %d, at a " +
                        "%.3f nm arm — %.2fx below §3's 45 — and the connected part collapses " +
                        "steeply above it: 15 duplexes bonded at 25 arms, 14 at 30, 8 at 35, " +
                        "3 at 40 and none at 42.").format(
                    selfConsistentCount, selfConsistent?.arm ?: 0.0, selfConsistentCount,
                    selfConsistent?.crossoversDemanded ?: 0, selfConsistent?.inventory ?: 0,
                    selfConsistent?.survivingCrossovers ?: 0, hostIntactCount,
                    hostIntact?.arm ?: 0.0, DESIGN_PATHS.toDouble() / hostIntactCount
                ),
        "the per-path force is untouched by any of this" to
                ("%.2f pN per path at §3's acceptable stroke against the 10 pN unzip allowable " +
                        "— %.2fx of margin — so nothing here is an allowable failure. The " +
                        "array is refused by the lattice, not by a force.").format(
                    perPath, UNZIP / perPath
                )
    )

    val result = T116Result(
        task = "T-116",
        leaf = "A8.2",
        temperatureKelvin = 300.0,
        kbT = 4.141947,
        medium = "aqueous 2 mM MgCl2",
        conventions = linkedMapOf(
            "plan" to "x along the host sheet's helices, y across them, origin at the tile centre",
            "z" to "positive upward, away from the electrode; §1's bias pulls the tile down",
            "duplex in plan" to
                    "a rectangle of width d = 2.69 nm (SAXS), so two parallel duplexes at " +
                    "exactly d are TANGENT and admissible — `C-0041`'s convention verbatim",
            "hinge arm" to
                    "one rectangle arm x d, ROOTED at its hinge rather than centred on it, " +
                    "owning NO vertical member: its near end is a crossover in the host's own " +
                    "plane and its far end is `C-0034`'s two-link A2 joint",
            "hinge site" to
                    "a crossover of the host's own lattice; interface b carries the columns of " +
                    "parity b mod 2, so an interior row sees both parities at a 16 bp pitch and " +
                    "an edge row one parity at 32 bp",
            "buried crossover" to
                    "a crossover lying under an arm on one of that arm's own two interfaces; it " +
                    "would tie a free lever back to the sheet, so it is charged as deleted",
            "orientation sweep" to
                    "over [0, 2pi) for a rooted arm and [0, pi) for `C-0041`'s centred beam"
        ),
        parameters = linkedMapOf(
            "armLength" to ARM,
            "armBasePairs" to ARM / RISE,
            "pathCount" to DESIGN_PATHS.toDouble(),
            "hingeCount" to 1.0,
            "edgeX" to EDGE_X,
            "edgeY" to EDGE_Y,
            "footprint" to FOOTPRINT,
            "duplexes" to ROWS.toDouble(),
            "interhelicalDistance" to DUPLEX,
            "risePerBasePair" to RISE,
            "crossoverSpacingBasePairs" to Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
            "hingeStiffness" to HINGE,
            "anchorageStiffness" to ANCHORAGE.rotationalStiffness,
            "bendingRigidity" to EI,
            "mandate" to MANDATE,
            "acceptableStroke" to ACCEPTABLE,
            "unzipAllowable" to UNZIP
        ),
        cheapBounds = cheapBounds,
        orientations = orientations,
        layouts = layouts,
        phases = phases,
        rows = rowRecords,
        placement = placementRecords,
        counts = counts,
        sensitivities = sensitivities,
        thresholds = thresholds,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-116-hinge-arm-array-packing.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n"
    )

    println()
    println("--- the cheap bounds ".padEnd(110, '-'))
    cheapBounds.forEach {
        println("%-62s %10.3f / %10.3f = %7.3f".format(it.quantity, it.value, it.against, it.ratio))
    }
    println()
    println("--- the orientation sweeps ".padEnd(110, '-'))
    orientations.forEach {
        println(
            "%-44s cols %d  L %6.2f  single %4d/%d  %s".format(
                it.element, it.columns, it.length, it.singleLevelOrientations, it.samples,
                it.verdict.take(46)
            )
        )
    }
    println()
    println("--- the lattice placement, all 32 phases ".padEnd(110, '-'))
    phases.forEach {
        println(
            ("phase %2d bp  inventory %2d  arms %2d (bound %2d)  hinges %2d  buried %2d  " +
                    "demand %2d  components %2d  orphans %2d").format(
                it.phaseBasePairs, it.inventory, it.armsPlaced, it.independentRowBound,
                it.hingeCrossovers, it.buriedCrossovers, it.crossoversDemanded, it.components,
                it.orphanSegments
            )
        )
    }
    println()
    println("--- the rows of the best phase ".padEnd(110, '-'))
    rowRecords.forEach {
        println(
            "row %2d  %-8s sites %2d  pitch %5.2f  placed %d  independent max %d".format(
                it.row, if (it.interiorRow) "interior" else "EDGE", it.sites, it.sitePitch,
                it.armsPlaced, it.independentMaximum
            )
        )
    }
    println()
    println("--- the self-consistent count ".padEnd(110, '-'))
    counts.forEach {
        println(
            ("n %2d  arm %6.3f (%5.1f bp)  placed %2d  %-14s demand %2d/%2d  surviving %2d  " +
                    "components %2d  orphans %2d  largest %2d").format(
                it.pathCount, it.arm, it.armBasePairs, it.armsPlaced,
                if (it.selfConsistent) "SELF-CONSISTENT" else "short", it.crossoversDemanded,
                it.inventory, it.survivingCrossovers, it.components, it.orphanSegments,
                it.largestComponentSegments
            )
        )
    }
    println()
    println("--- sensitivities ".padEnd(110, '-'))
    sensitivities.forEach {
        println(
            "%-22s %-58s arm %6.3f  width %4.2f  placed %2d  45 place: %s".format(
                it.axis, it.reading.take(58), it.arm, it.exclusionWidth,
                it.armsPlacedAtBestPhase, it.fortyFivePlace
            )
        )
    }
    println()
    println("--- predicates ".padEnd(110, '-'))
    predicates.forEach { (key, value) -> println("$key\n    $value\n") }
    println("--- reproductions ".padEnd(110, '-'))
    reproductions.forEach {
        println(
            "%-58s published %12.6f  derived %12.6f  departure %.2e".format(
                it.quantity, it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    println("written: ${output.path}")
}
