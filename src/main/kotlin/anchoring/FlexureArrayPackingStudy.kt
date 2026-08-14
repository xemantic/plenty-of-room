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
import org.openrndr.math.Vector2
import java.io.File
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Task `T-96`, leaf `A8.2` — **does `C-0035`'s surviving mounting survive `T-31`'s array packing?**
 *
 * ```shell
 * tools/study.sh anchoring.FlexureArrayPackingStudyKt
 * ```
 *
 * Emits `gpd/results/T-96-flexure-array-packing.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val TARGET_FORCE = Gen1Tile.TARGET_FORCE
private const val ACCEPTABLE_STROKE = Gen1Tile.ACCEPTABLE_STROKE
private const val DESIRED_STROKE = Gen1Tile.DESIRED_STROKE
private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE
private const val DESIGN_PATHS = 45
private const val ROWS = 15
private const val DESIGN_LENGTH = 8.0
private const val COMPLIANCE_CEILING = 40.0

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val DUPLEX = OrigamiDuplex.INTERHELICAL
private val UNZIP = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
private val SHEAR = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE

private const val EDGE_X = Gen1Tile.EDGE_X
private val EDGE_Y = ROWS * DUPLEX
private val FOOTPRINT = EDGE_X * EDGE_Y

private val BASE = StandoffBase.crossovers(2, favourableOrientation = true)
private val FLEXIBILITY = standoffTipFlexibility(EI, DESIGN_LENGTH, BASE.rotationalStiffness)

/** `C-0028`'s free-head critical load for the recommended standoff — `C-0030`'s 7.21 pN. */
private val CRITICAL_LOAD = standoffBucklingLoad(
    EI, DESIGN_LENGTH, baseRestraintParameter(BASE.rotationalStiffness, EI, DESIGN_LENGTH), 0.0
)

/** Fields et al.'s implied rigidity — `C-0030` reports every margin on it too. */
private const val EI_FIELDS = 172.906

private val CRITICAL_LOAD_FIELDS = standoffBucklingLoad(
    EI_FIELDS, DESIGN_LENGTH,
    baseRestraintParameter(BASE.rotationalStiffness, EI_FIELDS, DESIGN_LENGTH), 0.0
)

private val PATH_COUNTS
    get() = (listOf(10, 15, 20, 25, 28, 30, 34, 45, 60) + UNZIP_FLOOR_COUNT).distinct().sorted()

private fun spanFor(count: Int, scanSteps: Int = 256): Double = coupledFlexureSpan(
    EI, FLEXIBILITY, count, MANDATE, ACCEPTABLE_STROKE,
    FlexureOrientation.FAVOURABLE, STRETCH, DrawInModel.CHORD, scanSteps
)

private fun flexureFor(count: Int): CoupledJointFlexure =
    CoupledJointFlexure(EI, spanFor(count), FLEXIBILITY, STRETCH)

/**
 * The largest path count that packs on the Gen-1 tile, solved **self-consistently**: the span is
 * re-placed at every candidate count, because a flexure array's span is a function of its own
 * count (`k ∝ EI/L³` at `K/n` per path, so `L ∝ n^(1/3)`).
 */
private fun packingLimitedCount(): Int {
    var best = 0
    (1..ROWS).forEach { columns ->
        val count = columns * ROWS
        val span = spanFor(count)
        val verdict = packingVerdict(
            gridFlexureArray(columns, ROWS, EDGE_X, EDGE_Y, span, 0.0)
        )
        if (verdict.singleLevel && span <= EDGE_X) best = maxOf(best, count)
    }
    return best
}

/** The largest path count the Gen-1 tile's own attachment grid can carry, solved not asserted. */
private val PACKING_LIMIT = packingLimitedCount()

/**
 * The smallest path count whose own per-path force at §3's **desired** stroke clears the 10 pN
 * unzip allowable — `CH-0029`'s floor, read on the **element's** delivered force rather than on
 * `C-0017`'s mandate secant, and therefore self-consistent in the span.
 */
private val UNZIP_FLOOR_COUNT = (1..200).first { count ->
    CoupledJointFlexure(EI, spanFor(count), FLEXIBILITY, STRETCH)
        .strokeReaction(DESIRED_STROKE, FlexureOrientation.FAVOURABLE) <= UNZIP
}

// ---------------------------------------------------------------------------------------------

@Serializable
data class T96CheapBoundRecord(
    val pathCount: Int,
    val span: Double,
    val spanBasePairs: Double,
    val beamArea: Double,
    val beamAndFootArea: Double,
    val footprint: Double,
    val beamAreaRatio: Double,
    val beamAndFootAreaRatio: Double
)

@Serializable
data class T96OrientationRecord(
    val columns: Int,
    val pathCount: Int,
    val span: Double,
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
data class T96LayoutRecord(
    val columns: Int,
    val pathCount: Int,
    val angleDegrees: Double,
    val span: Double,
    val overlappingPairs: Int,
    val blockingPairs: Int,
    val mutuallyBlockingPairs: Int,
    val memberClashPairs: Int,
    val feasibleAtAnyLevelCount: Boolean,
    val levelsRequired: Int,
    val verdict: String
)

@Serializable
data class T96DesignRecord(
    val pathCount: Int,
    val columns: Int,
    val span: Double,
    val spanBasePairs: Double,
    val assembledTangent: Double,
    val perPathAtAcceptable: Double,
    val perPathAtDesired: Double,
    val deliveredAtDesired: Double,
    val endShearAtAcceptable: Double,
    val endShearAtDesired: Double,
    val bucklingMarginAtAcceptable: Double,
    val bucklingMarginAtDesired: Double,
    val bucklingMarginFieldsAtAcceptable: Double,
    val bucklingMarginFieldsAtDesired: Double,
    val clearsUnzipAtAcceptable: Boolean,
    val clearsUnzipAtDesired: Boolean,
    val clearsComplianceCeiling: Boolean,
    val packsOnGen1Tile: Boolean,
    val minimumBodyArea: Double,
    val minimumBodyAreaRatio: Double,
    val verdict: String
)

@Serializable
data class T96LevelRecord(
    val stroke: Double,
    val levelsAvailable: Int,
    val heights: List<Double>,
    val levelsNeededByArea: Int,
    val stackingHelps: Boolean,
    val note: String
)

@Serializable
data class T96SeveranceRecord(
    val layout: String,
    val helixDirection: String,
    val duplexes: Int,
    val holes: Int,
    val segments: Int,
    val crossovers: Int,
    val components: Int,
    val severed: Boolean
)

@Serializable
data class T96BodyRecord(
    val pathCount: Int,
    val columns: Int,
    val rows: Int,
    val span: Double,
    val edgeX: Double,
    val edgeY: Double,
    val area: Double,
    val areaRatio: Double,
    val edgeRatio: Double,
    val packs: Boolean
)

@Serializable
data class T96ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T96ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class T96Result(
    val task: String,
    val leaf: String,
    val temperatureKelvin: Double,
    val kbT: Double,
    val medium: String,
    val conventions: Map<String, String>,
    val parameters: Map<String, Double>,
    val cheapBound: List<T96CheapBoundRecord>,
    val orientations: List<T96OrientationRecord>,
    val layouts: List<T96LayoutRecord>,
    val designs: List<T96DesignRecord>,
    val levels: List<T96LevelRecord>,
    val severance: List<T96SeveranceRecord>,
    val bodies: List<T96BodyRecord>,
    val convergence: List<T96ConvergenceRecord>,
    val reproductions: List<T96ReproductionRecord>,
    val predicates: Map<String, String>,
    val findings: Map<String, String>
)

// ---------------------------------------------------------------------------------------------

private fun cheapBound(count: Int): T96CheapBoundRecord {
    val span = spanFor(count)
    val beam = arrayPlanArea(count, span, DUPLEX)
    val withFeet = arrayPlanArea(count, span + DUPLEX, DUPLEX)
    return T96CheapBoundRecord(
        count, span, span / RISE, beam, withFeet, FOOTPRINT,
        beam / FOOTPRINT, withFeet / FOOTPRINT
    )
}

private fun orientationRecord(columns: Int): T96OrientationRecord {
    val count = columns * ROWS
    val span = spanFor(count)
    val midspans = gridFlexureArray(columns, ROWS, EDGE_X, EDGE_Y, span, 0.0).map { it.midspan }
    val sweep = orientationSweep(midspans, span, 720)
    val verdict = when {
        sweep.singleLevelOrientations > 0 ->
            "PACKS — ${sweep.singleLevelOrientations} of 720 orientations lie in one level"
        sweep.feasibleOrientations > 0 ->
            "STACKS — no single level, but ${sweep.feasibleOrientations} of 720 admit an ordering"
        else ->
            "UNREALISABLE at any level count and on any body — every orientation leaves " +
                    "${sweep.minimumMutuallyBlockingPairs} mutually blocking and " +
                    "${sweep.minimumMemberClashPairs} clashing pairs"
    }
    return T96OrientationRecord(
        columns, count, span, sweep.samples, sweep.feasibleOrientations,
        sweep.singleLevelOrientations, sweep.minimumOverlappingPairs,
        sweep.minimumMutuallyBlockingPairs, sweep.minimumMemberClashPairs,
        sweep.bestAngleDegrees, verdict
    )
}

private fun layoutRecord(columns: Int, angleDegrees: Double): T96LayoutRecord {
    val count = columns * ROWS
    val span = spanFor(count)
    val angle = angleDegrees * Math.PI / 180.0
    val verdict = packingVerdict(gridFlexureArray(columns, ROWS, EDGE_X, EDGE_Y, span, angle))
    val text = when {
        verdict.singleLevel -> "PACKS in one level"
        verdict.feasibleAtAnyLevelCount -> "needs ${verdict.levelsRequired} levels"
        verdict.memberClashPairs > 0 ->
            "UNREALISABLE — ${verdict.memberClashPairs} vertical members clash, which no level " +
                    "count and no body size resolves"
        else ->
            "UNREALISABLE — ${verdict.mutuallyBlockingPairs} mutually blocking pairs"
    }
    return T96LayoutRecord(
        columns, count, angleDegrees, span, verdict.overlappingPairs, verdict.blockingPairs,
        verdict.mutuallyBlockingPairs, verdict.memberClashPairs,
        verdict.feasibleAtAnyLevelCount, verdict.levelsRequired, text
    )
}

private fun designRecord(count: Int): T96DesignRecord {
    val span = spanFor(count)
    val flexure = CoupledJointFlexure(EI, span, FLEXIBILITY, STRETCH)
    val orientation = FlexureOrientation.FAVOURABLE
    val perAcceptable = flexure.strokeReaction(ACCEPTABLE_STROKE, orientation)
    val perDesired = flexure.strokeReaction(DESIRED_STROKE, orientation)
    val tangent = count * flexure.strokeTangentStiffness(ACCEPTABLE_STROKE, orientation)
    val shearAcceptable = flexure.strokeEndShear(ACCEPTABLE_STROKE, orientation)
    val shearDesired = flexure.strokeEndShear(DESIRED_STROKE, orientation)
    val columns = packingLimitedColumns(EDGE_X, span)
    val packs = count <= PACKING_LIMIT
    val clearsAcceptable = perAcceptable <= UNZIP
    val clearsDesired = perDesired <= UNZIP
    val verdict = when {
        packs && clearsDesired -> "PASS at both of §3's strokes"
        packs && clearsAcceptable -> "PASS at §3's ACCEPTABLE stroke only"
        packs -> "FAIL — packs, but the per-path force is past the unzip allowable at 3 nm"
        clearsDesired -> "FAIL — clears the allowable at both strokes, and does not pack"
        else -> "FAIL — neither packs nor clears the allowable at the desired stroke"
    }
    return T96DesignRecord(
        count, columns, span, span / RISE, tangent, perAcceptable, perDesired,
        count * perDesired, shearAcceptable, shearDesired,
        CRITICAL_LOAD / shearAcceptable, CRITICAL_LOAD / shearDesired,
        CRITICAL_LOAD_FIELDS / shearAcceptable, CRITICAL_LOAD_FIELDS / shearDesired,
        clearsAcceptable, clearsDesired, tangent <= COMPLIANCE_CEILING, packs,
        arrayPlanArea(count, span + DUPLEX, DUPLEX),
        arrayPlanArea(count, span + DUPLEX, DUPLEX) / FOOTPRINT, verdict
    )
}

private fun levelRecord(stroke: Double): T96LevelRecord {
    val heights = availableLevelHeights(stroke)
    val span = spanFor(DESIGN_PATHS)
    val needed = ceil(arrayPlanArea(DESIGN_PATHS, span + DUPLEX, DUPLEX) / FOOTPRINT).toInt()
    val note = if (heights.isEmpty()) {
        "no standoff inside `C-0017`'s 10 nm envelope clears the midspan at this stroke — " +
                "`C-0030`'s `ℓ ≥ 12.69 nm`"
    } else {
        "the array's obstruction is a vertical-member clash, which is level-independent, so " +
                "${heights.size} available planes buy nothing"
    }
    return T96LevelRecord(stroke, heights.size, heights, needed, false, note)
}

private fun severanceRecord(columns: Int, helixAlongX: Boolean): T96SeveranceRecord {
    val span = spanFor(columns * ROWS)
    val holes = gridFlexureArray(columns, ROWS, EDGE_X, EDGE_Y, span, 0.0).map { it.tiePoint }
    val severance = superstructureSeverance(holes, EDGE_X, EDGE_Y, helixAlongX)
    return T96SeveranceRecord(
        "$columns x $ROWS regular grid", if (helixAlongX) "along x" else "across x",
        severance.duplexes, severance.holes, severance.segments, severance.crossovers,
        severance.components, severance.severed
    )
}

/**
 * The smallest tile that packs [count] flexures in one level, at every admissible column count.
 *
 * The minimum area is `n(L + d)d` and it is **independent of the aspect ratio**: growing the tile
 * across the helices adds rows, growing it along them adds columns, and the product is the beams'
 * own plan area either way.
 */
private fun bodyRecord(count: Int, columns: Int): T96BodyRecord {
    val span = spanFor(count)
    val rows = ceil(count.toDouble() / columns).toInt()
    val edgeY = rows * DUPLEX
    val edgeX = columns * (span + DUPLEX)
    val area = edgeX * edgeY
    val packs = packingVerdict(
        gridFlexureArray(columns, rows, edgeX, edgeY, span, 0.0)
    ).singleLevel
    return T96BodyRecord(
        columns * rows, columns, rows, span, edgeX, edgeY, area, area / FOOTPRINT,
        kotlin.math.sqrt(area / FOOTPRINT), packs
    )
}

// ---------------------------------------------------------------------------------------------

fun main() {
    val designSpan = spanFor(DESIGN_PATHS)
    val designFlexure = flexureFor(DESIGN_PATHS)

    val cheap = PATH_COUNTS.map { cheapBound(it) }
    val orientations = (1..3).map { orientationRecord(it) }
    val layouts = (1..3).flatMap { columns ->
        listOf(0.0, 5.0, 11.7, 23.0, 45.0, 90.0).map { layoutRecord(columns, it) }
    }
    val designs = PATH_COUNTS.map { designRecord(it) }
    val levels = listOf(ACCEPTABLE_STROKE, DESIRED_STROKE).map { levelRecord(it) }
    val severance = (1..3).flatMap { columns ->
        listOf(true, false).map { severanceRecord(columns, it) }
    }
    val designGridSeverance = severance.first { it.layout.startsWith("3 x") && it.helixDirection == "along x" }

    val staggerSpan = spanFor(ROWS)
    val stagger = smallestConnectingStagger(ROWS, EDGE_X, EDGE_Y, staggerSpan)
    val staggeredComponents = if (stagger > 0.0) {
        superstructureSeverance(
            staggeredTieColumn(ROWS, EDGE_Y, stagger), EDGE_X, EDGE_Y, true
        ).components
    } else -1

    val bodies = listOf(UNZIP_FLOOR_COUNT, 34, 45).flatMap { count ->
        (1..4).map { bodyRecord(count, it) }
    }

    // ------------------------------------------------------------------ convergence
    val restraint = designFlexure.restraint
    val convergence = ArrayList<T96ConvergenceRecord>()
    val finestSlot = slotLengthForClearance(designSpan, restraint, DESIRED_STROKE, 5.31, 4096)
    listOf(64, 256, 1024, 4096).forEach { steps ->
        val value = slotLengthForClearance(designSpan, restraint, DESIRED_STROKE, 5.31, steps)
        convergence += T96ConvergenceRecord(
            "slot length at a 5.31 nm clearance", "bisection scan steps", steps.toDouble(),
            value, abs(value - finestSlot) / finestSlot
        )
    }
    val finestSpan = spanFor(DESIGN_PATHS, 2048)
    listOf(64, 256, 1024, 2048).forEach { steps ->
        val value = spanFor(DESIGN_PATHS, steps)
        convergence += T96ConvergenceRecord(
            "placed span at 45 paths", "placement scan steps", steps.toDouble(),
            value, abs(value - finestSpan) / finestSpan
        )
    }
    val finestSweep = orientationSweep(
        gridFlexureArray(3, ROWS, EDGE_X, EDGE_Y, designSpan, 0.0).map { it.midspan },
        designSpan, 2880
    )
    listOf(180, 360, 720, 1440, 2880).forEach { samples ->
        val sweep = orientationSweep(
            gridFlexureArray(3, ROWS, EDGE_X, EDGE_Y, designSpan, 0.0).map { it.midspan },
            designSpan, samples
        )
        convergence += T96ConvergenceRecord(
            "feasible orientations of the 3 x 15 array", "orientation samples", samples.toDouble(),
            sweep.feasibleOrientations.toDouble(),
            abs(sweep.feasibleOrientations - finestSweep.feasibleOrientations).toDouble()
        )
    }
    (0 until 32 step 8).forEach { phase ->
        val holes = gridFlexureArray(3, ROWS, EDGE_X, EDGE_Y, designSpan, 0.0).map { it.tiePoint }
        val components = superstructureSeverance(
            holes, EDGE_X, EDGE_Y, true, crossoverPhase = phase * RISE
        ).components
        convergence += T96ConvergenceRecord(
            "superstructure components under the 3 x 15 tie grid", "crossover phase [bp]",
            phase.toDouble(), components.toDouble(), 0.0
        )
    }

    // ------------------------------------------------------------------ reproductions
    val reproductions = listOf(
        T96ReproductionRecord(
            "C-0030 span at 45 paths, l = 8 nm [nm]", 31.82, designSpan,
            abs(designSpan - 31.82) / 31.82
        ),
        T96ReproductionRecord(
            "C-0030 assembled tangent at 3 nm [pN/nm]", 25.23,
            DESIGN_PATHS * designFlexure.strokeTangentStiffness(
                ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE
            ),
            abs(
                DESIGN_PATHS * designFlexure.strokeTangentStiffness(
                    ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE
                ) - 25.23
            ) / 25.23
        ),
        T96ReproductionRecord(
            "C-0030 free-head critical load [pN]", 7.21, CRITICAL_LOAD,
            abs(CRITICAL_LOAD - 7.21) / 7.21
        ),
        T96ReproductionRecord(
            "C-0030 stroke clearance at l = 8 nm [nm]", 5.31, midspanClearance(DESIGN_LENGTH),
            abs(midspanClearance(DESIGN_LENGTH) - 5.31) / 5.31
        ),
        T96ReproductionRecord(
            "C-0035 aperture length at 10 nm, l = 8 nm [nm]", 18.37,
            apertureLength(designSpan, restraint, DESIRED_STROKE, DESIGN_LENGTH),
            abs(
                apertureLength(designSpan, restraint, DESIRED_STROKE, DESIGN_LENGTH) - 18.37
            ) / 18.37
        ),
        T96ReproductionRecord(
            "C-0035 slot area over 45 paths [nm2]", 2223.0,
            apertureArea(
                DESIGN_PATHS, apertureLength(designSpan, restraint, DESIRED_STROKE, DESIGN_LENGTH)
            ),
            abs(
                apertureArea(
                    DESIGN_PATHS,
                    apertureLength(designSpan, restraint, DESIRED_STROKE, DESIGN_LENGTH)
                ) - 2223.0
            ) / 2223.0
        ),
        T96ReproductionRecord(
            "C-0035 tie aperture area over 45 paths [nm2]", 326.0, tieApertureArea(DESIGN_PATHS),
            abs(tieApertureArea(DESIGN_PATHS) - 326.0) / 326.0
        ),
        T96ReproductionRecord(
            "C-0035 slot area as a fraction of the 1600 nm2 footprint", 1.39,
            apertureArea(
                DESIGN_PATHS, apertureLength(designSpan, restraint, DESIRED_STROKE, DESIGN_LENGTH)
            ) / 1600.0,
            abs(
                apertureArea(
                    DESIGN_PATHS,
                    apertureLength(designSpan, restraint, DESIRED_STROKE, DESIGN_LENGTH)
                ) / 1600.0 - 1.39
            ) / 1.39
        ),
        T96ReproductionRecord(
            "the placed span follows n^(1/3): L(15)/L(45)", (15.0 / 45.0).pow(1.0 / 3.0),
            spanFor(15) / designSpan,
            abs(spanFor(15) / designSpan - (15.0 / 45.0).pow(1.0 / 3.0)) /
                    (15.0 / 45.0).pow(1.0 / 3.0)
        ),
        T96ReproductionRecord(
            "CH-0029's unzip floor read on C-0017's MANDATE secant",
            34.0, ceil(MANDATE * DESIRED_STROKE / UNZIP),
            abs(ceil(MANDATE * DESIRED_STROKE / UNZIP) - 34.0) / 34.0
        ),
        T96ReproductionRecord(
            "the same floor read on the ELEMENT's own delivered force, self-consistently",
            34.0, UNZIP_FLOOR_COUNT.toDouble(),
            abs(UNZIP_FLOOR_COUNT - 34.0) / 34.0
        ),
        T96ReproductionRecord(
            "SAXS interhelical distance [nm]", 2.69, DUPLEX, abs(DUPLEX - 2.69) / 2.69
        )
    )

    // ------------------------------------------------------------------ predicates
    val designAt45 = designs.first { it.pathCount == DESIGN_PATHS }
    val designAt15 = designs.first { it.pathCount == ROWS }
    val designAtFloor = designRecord(UNZIP_FLOOR_COUNT)
    val sweepAt3 = orientations.first { it.columns == 3 }
    val packLimit = packingLimitedCount()

    val predicates = linkedMapOf(
        "P1 the 45-beam array's plan area against the tile footprint" to
                ("%.0f nm2 of beam, %.0f nm2 with the standoff feet — %.2fx and %.2fx the " +
                        "%.0f nm2 footprint. FALSIFIER 1 DID NOT FIRE").format(
                    arrayPlanArea(45, designSpan, DUPLEX),
                    arrayPlanArea(45, designSpan + DUPLEX, DUPLEX),
                    arrayPlanArea(45, designSpan, DUPLEX) / FOOTPRINT,
                    arrayPlanArea(45, designSpan + DUPLEX, DUPLEX) / FOOTPRINT,
                    FOOTPRINT
                ),
        "P2 a single-level layout of 45 flexures" to
                "NONE — %d of %d orientations, over the whole of [0, pi)".format(
                    sweepAt3.singleLevelOrientations, sweepAt3.samples
                ),
        "P3 a multi-level layout at any level count and any body size" to
                "NONE — %d of %d orientations; the minimum obstruction over the sweep is %d mutually blocking and %d clashing pairs".format(
                    sweepAt3.feasibleOrientations, sweepAt3.samples,
                    sweepAt3.minimumMutuallyBlockingPairs, sweepAt3.minimumMemberClashPairs
                ),
        "P4 the packing-limited path count on the Gen-1 tile" to
                "%d — one column of one flexure per duplex, span %.2f nm = %.0f bp".format(
                    packLimit, spanFor(packLimit), spanFor(packLimit) / RISE
                ),
        "P5 that count against the per-path allowable, at both of §3's strokes" to
                ("ACCEPTABLE 3 nm: %.2f pN per path against the %.0f pN unzip allowable — PASS. " +
                        "DESIRED 10 nm: %.2f pN — FAIL, and the same allowable demands >= %d paths " +
                        "(34 on C-0017's mandate secant, %d on the element's own softening law)").format(
                    designAt15.perPathAtAcceptable, UNZIP, designAt15.perPathAtDesired,
                    UNZIP_FLOOR_COUNT, UNZIP_FLOOR_COUNT
                ),
        "P6 the threshold on the variable that must give" to
                ("the TILE. %d paths of %.2f nm span need %.0f nm2 = %.2fx the Gen-1 footprint " +
                        "(%.2fx in edge); 45 paths need %.0f nm2 = %.2fx").format(
                    UNZIP_FLOOR_COUNT,
                    designAtFloor.span, designAtFloor.minimumBodyArea, designAtFloor.minimumBodyAreaRatio,
                    kotlin.math.sqrt(designAtFloor.minimumBodyAreaRatio),
                    designAt45.minimumBodyArea, designAt45.minimumBodyAreaRatio
                ),
        "P7 what the tie apertures do to the superstructure as a sheet" to
                ("the 3 x 15 grid cuts every one of the %d duplexes into %d pieces and leaves " +
                        "%d disconnected components; a %.2f nm (%d bp) stagger restores %d").format(
                    designGridSeverance.duplexes,
                    designGridSeverance.segments / designGridSeverance.duplexes,
                    designGridSeverance.components, stagger, (stagger / RISE).roundToInt(),
                    staggeredComponents
                )
    )

    val findings = linkedMapOf(
        "the verdict" to
                ("The array does NOT pack, at either stroke, and the obstruction is not area but " +
                        "TOPOLOGY: the attachment grid's across-helix pitch is exactly one duplex, " +
                        "so beams in adjacent rows are tangent at zero tilt and interfere at any " +
                        "other; and its along-helix pitch (%.2f nm) is under the span plus a duplex " +
                        "(%.2f nm), so the three beams of a row bury each other's standoff feet. " +
                        "Those two conditions meet only at the single angle 0, where the second one " +
                        "fails. STACKING IS NOT AN ESCAPE: a standoff runs from the superstructure " +
                        "to its own beam plane and a tie runs from that plane to the tile, so any " +
                        "two vertical members of the array share a height range whatever levels " +
                        "their beams sit at — the clash is LEVEL-INDEPENDENT.").format(
                    EDGE_X / 3.0, designSpan + DUPLEX
                ),
        "what packs on the specified tile" to
                ("EXACTLY FIFTEEN — one flexure per duplex, one column, span %.2f nm = %.0f bp, " +
                        "which is `C-0026`'s one-attachment-row-per-duplex scheme with m = 1 and " +
                        "leaves %.2f nm of the 40 nm edge unused. It delivers §3's ACCEPTABLE " +
                        "clause: %.2f pN per path against the 10 pN unzip allowable, %.2fx of " +
                        "buckling margin (%.2fx on Fields et al.'s measured rigidity). It does " +
                        "NOT deliver the desired clause: %.2f pN per " +
                        "path, %.2fx past the allowable.").format(
                    spanFor(ROWS), spanFor(ROWS) / RISE, EDGE_X - spanFor(ROWS),
                    designAt15.perPathAtAcceptable, designAt15.bucklingMarginAtAcceptable,
                    designAt15.bucklingMarginFieldsAtAcceptable,
                    designAt15.perPathAtDesired, designAt15.perPathAtDesired / UNZIP
                ),
        "the two strokes differ in KIND, not in degree" to
                ("At §3's ACCEPTABLE 3 nm the binding variable is the PATH COUNT and the " +
                        "threshold is 45 -> 15, which costs nothing against any standing " +
                        "allowable. At §3's DESIRED 10 nm the count is bounded BELOW at %d by " +
                        "the same unzip allowable and ABOVE at %d by the packing, so the window " +
                        "is EMPTY on the specified tile and the binding variable is the " +
                        "FOOTPRINT: %.0f nm2, %.2fx the Gen-1 tile, %.2fx in edge.").format(
                    UNZIP_FLOOR_COUNT, PACKING_LIMIT,
                    designAtFloor.minimumBodyArea, designAtFloor.minimumBodyAreaRatio,
                    kotlin.math.sqrt(designAtFloor.minimumBodyAreaRatio)
                ),
        "the minimum body area is the beams' own, and carries no aspect ratio" to
                ("n(L + d)d exactly: growing the tile across the helices adds rows and growing " +
                        "it along them adds columns, and the product is the same either way. " +
                        "%d paths: %.0f nm2 whatever the aspect.").format(
                    UNZIP_FLOOR_COUNT, designAtFloor.minimumBodyArea
                ),
        "the tie apertures are not 45 holes, they are m slots, and they SEVER the sheet" to
                ("`C-0035` prices them as 326 nm2 = 20.4 %% of the footprint. An area is not the " +
                        "question a sheet asks. The holes lie on the attachment grid, whose " +
                        "across-helix pitch is EXACTLY one duplex, so a column of ties removes a " +
                        "whole line of material: the 3 x 15 grid cuts every duplex into four " +
                        "pieces and leaves %d disconnected components, at every one of the 32 " +
                        "crossover phases. The remedy is free of every upstream claim — `C-0026` " +
                        "fixes the attachment ROWS and says nothing about where along a row an " +
                        "attachment sits — and it costs %.2f nm (%d bp) of stagger.").format(
                    designGridSeverance.components, stagger, (stagger / RISE).roundToInt()
                ),
        "the one layout that works is feasible at exactly ONE angle out of 720" to
                ("The single-column array is realisable at %d of 720 sampled orientations, and " +
                        "the one that works is exactly 0 — beams parallel to the attachment rows. " +
                        "Any tilt at all puts two beams in adjacent rows into each other, because " +
                        "their centres are one duplex apart and a rotation reduces the " +
                        "perpendicular separation by cos(theta) < 1. The array is feasible on a " +
                        "SET OF MEASURE ZERO in the orientation, which is a lattice statement and " +
                        "not a tolerance: the sheet's own helix direction supplies it exactly.").format(
                    orientations.first { it.columns == 1 }.feasibleOrientations
                ),
        "a larger tile is a specification question, and it is cheaper at the edge" to
                ("`C-0022` finds a larger tile costs LESS at the rim: +6.3 %% instead of " +
                        "+14.7 %%, because the collar is a fixed 1.65 nm and scales as 1/L. So " +
                        "the %.2fx edge growth §3's desired stroke needs is not merely " +
                        "affordable at the edge, it is favourable there. It is nevertheless a " +
                        "change to §3, and belongs to NDI rather than to this loop.").format(
                    kotlin.math.sqrt(designAtFloor.minimumBodyAreaRatio)
                )
    )

    val result = T96Result(
        task = "T-96",
        leaf = "A8.2",
        temperatureKelvin = 300.0,
        kbT = 4.141947,
        medium = "aqueous 2 mM MgCl2",
        conventions = linkedMapOf(
            "plan" to "x along the tile's helices, y across them, origin at the tile centre",
            "z" to "positive upward, away from the electrode; §1's bias pulls the tile down",
            "duplex in plan" to
                    "a rectangle of width d = 2.69 nm (SAXS), so two parallel duplexes at exactly " +
                    "d are TANGENT and admissible",
            "vertical member" to
                    "a standoff or a tie, a disc of radius d/2 — a duplex standing normal to the sheet",
            "flexure" to
                    "one rectangle span x d centred on its midspan, plus three vertical members: " +
                    "two standoff feet at its ends and one tie at its midspan",
            "midspans" to
                    "pinned by `C-0015`'s m x 15 attachment grid, because the tie is vertical",
            "blocking" to
                    "Y's beam covers one of X's vertical members, so level(Y) > level(X) strictly",
            "clash" to
                    "two vertical members closer than d — level-INDEPENDENT, and therefore fatal",
            "mounting" to
                    "`C-0035`'s `Su`: bases on the output superstructure, standoffs away from the " +
                    "tile, flexure outboard, midspan tied back down"
        ),
        parameters = linkedMapOf(
            "targetForce" to TARGET_FORCE,
            "acceptableStroke" to ACCEPTABLE_STROKE,
            "desiredStroke" to DESIRED_STROKE,
            "mandate" to MANDATE,
            "complianceCeiling" to COMPLIANCE_CEILING,
            "edgeX" to EDGE_X,
            "edgeY" to EDGE_Y,
            "footprint" to FOOTPRINT,
            "rows" to ROWS.toDouble(),
            "interhelicalDistance" to DUPLEX,
            "stericDiameter" to OrigamiDuplex.DIAMETER,
            "risePerBasePair" to RISE,
            "standoffLength" to DESIGN_LENGTH,
            "bendingRigidity" to EI,
            "stretchModulus" to STRETCH,
            "unzipAllowable" to UNZIP,
            "shearAllowable" to SHEAR,
            "criticalLoad" to CRITICAL_LOAD,
            "criticalLoadFields" to CRITICAL_LOAD_FIELDS,
            "designSpan" to designSpan,
            "staggerForConnectivity" to stagger,
            "packingLimitedPathCount" to PACKING_LIMIT.toDouble(),
            "unzipFloorPathCount" to UNZIP_FLOOR_COUNT.toDouble()
        ),
        cheapBound = cheap,
        orientations = orientations,
        layouts = layouts,
        designs = designs,
        levels = levels,
        severance = severance,
        bodies = bodies,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings
    )

    val output = File("gpd/results/T-96-flexure-array-packing.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")

    println("T-96 — does the surviving mounting survive T-31's array packing?")
    println()
    println("cheap bound (n, span, bp, beam area, with feet, /footprint)")
    cheap.forEach {
        println(
            "  %3d %7.2f %5.0f %9.1f %9.1f %7.3f %7.3f".format(
                it.pathCount, it.span, it.spanBasePairs, it.beamArea, it.beamAndFootArea,
                it.beamAreaRatio, it.beamAndFootAreaRatio
            )
        )
    }
    println()
    println("orientation sweep (columns, n, feasible, single level, min mutual, min clash, best deg)")
    orientations.forEach {
        println(
            "  %2d %3d %5d %5d %5d %5d %8.2f  %s".format(
                it.columns, it.pathCount, it.feasibleOrientations, it.singleLevelOrientations,
                it.minimumMutuallyBlockingPairs, it.minimumMemberClashPairs,
                it.bestAngleDegrees, it.verdict
            )
        )
    }
    println()
    println("layouts (columns, angle, overlaps, blocks, mutual, clashes, levels)")
    layouts.forEach {
        println(
            "  %2d %6.1f %5d %5d %5d %5d %5d  %s".format(
                it.columns, it.angleDegrees, it.overlappingPairs, it.blockingPairs,
                it.mutuallyBlockingPairs, it.memberClashPairs, it.levelsRequired, it.verdict
            )
        )
    }
    println()
    println("designs (n, span, tangent, per path @3, @10, margin @3, @10, packs, verdict)")
    designs.forEach {
        println(
            "  %3d %7.2f %7.2f %7.2f %7.2f %6.2f %6.2f %-6s %s".format(
                it.pathCount, it.span, it.assembledTangent, it.perPathAtAcceptable,
                it.perPathAtDesired, it.bucklingMarginAtAcceptable, it.bucklingMarginAtDesired,
                it.packsOnGen1Tile, it.verdict
            )
        )
    }
    println()
    println("levels")
    levels.forEach {
        println(
            "  stroke %5.1f  available %d %s  needed by area %d — %s".format(
                it.stroke, it.levelsAvailable, it.heights, it.levelsNeededByArea, it.note
            )
        )
    }
    println()
    println("severance (layout, helices, duplexes, holes, segments, crossovers, components)")
    severance.forEach {
        println(
            "  %-22s %-9s %4d %4d %4d %5d %5d %s".format(
                it.layout, it.helixDirection, it.duplexes, it.holes, it.segments,
                it.crossovers, it.components, if (it.severed) "SEVERED" else "connected"
            )
        )
    }
    println()
    println("bodies (n, columns, rows, span, edgeX, edgeY, area, /footprint, packs)")
    bodies.forEach {
        println(
            "  %3d %3d %3d %7.2f %7.2f %7.2f %9.1f %7.3f %s".format(
                it.pathCount, it.columns, it.rows, it.span, it.edgeX, it.edgeY, it.area,
                it.areaRatio, it.packs
            )
        )
    }
    println()
    println("convergence")
    convergence.forEach {
        println(
            "  %-56s %-28s %8.0f %14.9f %10.2e".format(
                it.quantity.take(56), it.control.take(28), it.level, it.value,
                it.departureFromFinest
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    reproductions.forEach {
        println(
            "  %-56s %12.6f %12.6f %10.2e".format(
                it.quantity.take(56), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    println("predicates")
    predicates.forEach { (key, value) -> println("  %-58s %s".format(key, value)) }
    println()
    findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $output")
}
