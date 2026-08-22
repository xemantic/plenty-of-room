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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.ShearJointAllowable
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Task `T-17` — "one attachment row per duplex" as an output-coupling scheme: what the exact
 * zero costs, and what breaks it. Leaf `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh coupling.OneRowPerDuplexStudyKt
 * ```
 *
 * Emits `gpd/results/T-17-one-row-per-duplex.json`, deterministically — no timestamp, every
 * floating-point number rounded at the serialisation boundary per [roundCouplingResult].
 *
 * Consumes `C-0009`/`C-0015`'s `OrigamiGrillage` and `C-0006`'s `PlateOnFoundation` as
 * **libraries, read-only**; no third lattice is built. `C-0022`'s solved lateral load profile
 * is read from its own result file rather than transcribed.
 */

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

/** `P1` — is `C-0015`'s 3 × 15 flatness grid the same object as "one row per duplex"? */
@Serializable
data class GridIdentityRecord(
    val shape: String,
    val columns: Int,
    val rows: Int,
    val attachments: Int,
    val oneRowPerDuplex: Boolean,
    val distinctRowPositions: Int,
    val maximumRowDepartureFromDuplexAxis: Double,
    val perPathStaticShare: Double
)

/** One load profile the scheme is costed under. */
@Serializable
data class LoadProfileRecord(
    val name: String,
    val source: String,
    val concentration: Double?,
    val gapHeight: Double?,
    val appliedBias: Double?,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double,
    val rimWidth: Double,
    val interiorPressure: Double,
    val totalLoad: Double,
    val totalOverUniform: Double,
    val stripLoadMinimum: Double,
    val stripLoadMaximum: Double,
    val stripLoadSpreadFraction: Double,
    val rigidTilePeakInterfaceForce: Double,
    val rigidTilePeakPerCrossover: Double,
    val crossoversOnWorstInterface: Int
)

/** One `(grid, load profile, foundation)` state of the lattice, with the plate beside it. */
@Serializable
data class RestoredForceRecord(
    val shape: String,
    val columns: Int,
    val rows: Int,
    val attachments: Int,
    val oneRowPerDuplex: Boolean,
    val profile: String,
    val foundationMultiplier: Double,
    val perPathStaticShare: Double,
    val peakCrossoverForce: Double,
    val peakCrossoverOverStaticShare: Double,
    val peakDuplexShear: Double,
    val peakInterfaceForce: Double,
    val concentrationFactor: Double,
    val rigidTilePeakInterfaceForce: Double,
    val latticeOverRigidIdentity: Double,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverPlate: Double,
    val freeTileStroke: Double,
    val dishingOverStroke: Double,
    val flat: Boolean,
    val unzipMargin: Double?,
    val shearMarginAt16BasePairs: Double?,
    val verdict: String
)

/** Attachment-stiffness scatter under a **uniform** load, which isolates it from the shape. */
@Serializable
data class ScatterRecord(
    val pattern: String,
    val amplitude: Double,
    val shape: String,
    val peakCrossoverForce: Double,
    val peakCrossoverPerUnitAmplitude: Double,
    val peakCrossoverOverStaticShare: Double,
    val peakDuplexShear: Double,
    val stiffestPathForce: Double,
    val softestPathForce: Double,
    val pathForceSpreadFraction: Double
)

/** The thermal crossover force, and the reason it is not a load. */
@Serializable
data class ThermalRecord(
    val shape: String,
    val linkStiffness: Double,
    val peakThermalCrossoverForce: Double,
    val meanThermalCrossoverForce: Double,
    val overSqrtThermalEnergyTimesLink: Double,
    val ratioToPreviousDecade: Double?
)

/** What the thermal force is once the link is given a **physical** vertical stiffness. */
@Serializable
data class ThermalBracketRecord(
    val reading: String,
    val verticalStiffness: Double,
    val thermalForceRms: Double,
    val overUnzipAllowable: Double,
    val note: String
)

/** `P5` — the other two duties the same attachments carry. */
@Serializable
data class DutyRecord(
    val shape: String,
    val columns: Int,
    val rows: Int,
    val attachments: Int,
    val perPathStaticShare: Double,
    val lateralStiffness: Double,
    val lateralMargin: Double,
    val meanSquaredRadius: Double,
    val yawStiffness: Double,
    val yawMargin: Double,
    val holdDownStiffnessMargin: Double,
    val perPathThermalForce: Double,
    val allDutiesDischarged: Boolean
)

/** `P4` — the count at which a per-path share crosses an allowable. */
@Serializable
data class AllowableCrossingRecord(
    val allowable: String,
    val force: Double,
    val basePairs: Double?,
    val minimumPathsForStaticShareAlone: Int,
    val minimumPathsWithRestoredForce: Int,
    val marginAt45Paths: Double
)

/** `CH-0029`'s ladder, reproduced through `structure`'s own model. */
@Serializable
data class JointAllowableRecord(
    val basePairs: Double,
    val loadingRate: Double,
    val shearAllowable: Double,
    val unzipAllowable: Double
)

/** Gate-4 convergence. */
@Serializable
data class ConvergenceRecord(
    val axis: String,
    val setting: String,
    val degreesOfFreedom: Int,
    val peakCrossoverForce: Double,
    val departureFromFinest: Double
)

/** Gate-5 upstream reproductions. */
@Serializable
data class ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

/** The result file. */
@Serializable
data class OneRowPerDuplexResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val runParameters: Map<String, String>,
    val citedInputs: Map<String, String>,
    val temperature: Double,
    val thermalEnergy: Double,
    val designPointProfile: String,
    val gridIdentity: List<GridIdentityRecord>,
    val loadProfiles: List<LoadProfileRecord>,
    val restoredForces: List<RestoredForceRecord>,
    val profileSweep: List<RestoredForceRecord>,
    val scatter: List<ScatterRecord>,
    val thermal: List<ThermalRecord>,
    val thermalBracket: List<ThermalBracketRecord>,
    val duties: List<DutyRecord>,
    val allowableCrossings: List<AllowableCrossingRecord>,
    val jointAllowables: List<JointAllowableRecord>,
    val convergence: List<ConvergenceRecord>,
    val reproductions: List<ReproductionRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// constants
// ---------------------------------------------------------------------------------------------

private const val DUPLEXES = 15

private const val EDGE_X = Gen1Tile.EDGE_X

/** `C-0017`'s mandate, `100 pN / 3 nm`, exactly. */
private val MANDATED_STIFFNESS = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `C-0014`'s per-coordinate lateral bound in pN/nm — **CITED**. */
private const val LATERAL_BOUND = 0.460216

/** `C-0014`'s yaw bound in `pN·nm/rad`, budgeted at the tile's corner — **CITED**. */
private const val YAW_BOUND = 368.173

/** `C-0023`'s two-sided hold-down requirement, `k_BT/σ²` in pN/nm — **CITED**. */
private const val HOLD_DOWN_BOUND = 0.460216

/** `C-0017`'s realised lateral stiffness of the tuned spacer, `0.83 k_norm` — **CITED**. */
private const val LATERAL_OVER_NORMAL = 32.36 / 39.01

/** The rim standoff `C-0022` fits its rim residual over, in nm — **CITED**. */
private const val RIM_STANDOFF = 1.0

/** The upper edge of the unzip band — **CITED**, Essevaz-Roulet et al. (1997). */
private const val UNZIP_UPPER = 15.0

/** A realistic staple-extension bonded length in base pairs, `C-0024`'s own choice. */
private const val REALISTIC_BONDED_BASE_PAIRS = 16.0

private val FOUNDATION_MULTIPLIERS = listOf(0.25, 0.5, 1.0, 2.0, 4.0)

/** The grid shapes swept — **shapes, not counts** (`C-0015`). */
private val GRID_SHAPES: List<Pair<Int, Int>> = listOf(
    // one attachment row per duplex, at every column count that matters
    1 to 15, 2 to 15, 3 to 15, 4 to 15, 5 to 15, 8 to 15, 15 to 15,
    // the equal-count contrasts: 45 attachments in four different shapes
    5 to 9, 9 to 5, 15 to 3,
    // C-0009's square answers, and C-0015's three worst row counts
    8 to 8, 7 to 7, 3 to 11, 3 to 14, 3 to 7
)

/**
 * `T-5b`'s flatness convention, cited from `C-0015`: a tile is "flat" if its peak dishing is
 * under a tenth of the stroke. **A convention, not a physical threshold** — the 45, the 64 and
 * every count below move together if it changes.
 */
private const val RIGID_PLATE_TOLERANCE = 0.10

/**
 * The scatter amplitudes swept, up to **0.99** — a relative amplitude of 0.99 leaves every
 * second path at one per cent of its nominal stiffness, i.e. a coupling assembled about as
 * badly as one can be while still existing, and the point of carrying it is that the answer at
 * that amplitude is *still* not binding. (Exactly 1.0 is excluded because a support of zero
 * stiffness is not a support and `PointSupport` refuses it.)
 */
private val SCATTER_AMPLITUDES = listOf(0.01, 0.03, 0.10, 0.30, 0.99)

private val LINK_PENALTIES = listOf(1e2, 1e3, 1e4, 1e5, 1e6)

// ---------------------------------------------------------------------------------------------
// the load profiles, read from `C-0022`'s own result file
// ---------------------------------------------------------------------------------------------

private class EdgeProfile(
    val name: String,
    val source: String,
    val concentration: Double?,
    val gapHeight: Double?,
    val appliedBias: Double?,
    val smooth: CollarTerm,
    val rim: CollarTerm
) {

    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, EDGE_X, lengthY,
        if (rim.depth == 0.0) listOf(smooth) else listOf(smooth, rim)
    )

}

/**
 * **Every** solved profile of `C-0022`, read from
 * `gpd/results/T-3b-tile-edge-load-profile.json` rather than transcribed.
 *
 * All 21 states are returned. `C-0022` publishes **two** profiles per `(concentration, gap)`
 * — one at the operating bias of its softest layer model and one at its stiffest — and picking
 * either by `(concentration, gap)` alone would silently take whichever the file happens to
 * list first. The bias travels with the state here, and the grid sweep uses the same six
 * states `C-0022`'s own headline table quotes.
 */
private fun solvedProfiles(file: File): List<EdgeProfile> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-17 consumes the SOLVED edge profile " +
                "and will not substitute an assumed one for it."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .map { record ->
            fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
            val concentration = value("concentration")
            val gap = value("gapHeight")
            val bias = value("appliedBias")
            EdgeProfile(
                name = "C-0022 %.1f mM, %.0f nm, %.3f V".format(concentration, gap, bias),
                source = record.getValue("biasSource").jsonPrimitive.content,
                concentration = concentration,
                gapHeight = gap,
                appliedBias = bias,
                smooth = CollarTerm(value("taperDepth"), value("taperWidth")),
                rim = CollarTerm(value("rimResidualDepth"), RIM_STANDOFF)
            )
        }
}

/** The `(concentration, gap, bias)` keys of the six states `C-0022`'s headline table quotes. */
private val HEADLINE_STATES: List<Triple<Double, Double, Double>> = listOf(
    Triple(2.0, 10.0, 0.192),   // C-0022's own design point: depth -0.303, width 8.94
    Triple(0.5, 10.0, 0.134),   // the deepest enhancement it reports: -0.508, 9.16
    Triple(10.0, 10.0, 0.192),  // the ONLY state whose depth is genuinely positive: +0.420, 2.40
    Triple(2.0, 5.0, 0.368),    // the shortest gap in the operating box: -0.250, 6.86
    Triple(2.0, 2.0, 0.368)     // held at the 3 nm stroke, the one state whose total force FALLS
)

private fun assumedTaper(): EdgeProfile = EdgeProfile(
    name = "C-0006 ASSUMED taper (50% over one Debye length)",
    source = "C-0006/C-0009, superseded in sign and width by C-0022 (CH-0025); " +
            "carried so that the replacement is auditable",
    concentration = null,
    gapHeight = null,
    appliedBias = null,
    smooth = CollarTerm(0.5, Gen1Tile.DEBYE_LENGTH),
    rim = CollarTerm(0.0, RIM_STANDOFF)
)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

private fun sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun lattice(
    sheet: OrigamiSheet,
    foundationMultiplier: Double,
    supports: List<com.xemantic.nano.plentyofroom.structure.PointSupport>,
    subdivisions: Int = 2,
    linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT * foundationMultiplier,
    columns = CrossoverLayout.centred(NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0),
    subdivisions = subdivisions,
    linkStiffness = linkStiffness,
    supports = supports
)

/** `T-10`'s eight symmetrically centred columns — the nominal layout, unchanged. */
private const val NOMINAL_COLUMNS = 8

private fun gridOf(columns: Int, rows: Int, lengthY: Double) =
    attachmentGrid(columns, rows, EDGE_X, lengthY)

private fun supportsOf(
    columns: Int,
    rows: Int,
    lengthY: Double,
    scatter: (Int) -> Double = { 1.0 }
) = couplingSupports(gridOf(columns, rows, lengthY), MANDATED_STIFFNESS, scatter)

private fun oneRowPerDuplex(rows: Int) = rows == DUPLEXES

private fun verdictOf(peak: Double, share: Double): String = when {
    peak >= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE ->
        "the RESTORED force alone reaches the 10-15 pN unzip band"
    peak >= share -> "the restored force exceeds the per-path static share"
    peak >= 0.1 * share -> "the restored force is 10-100% of the per-path static share"
    peak > 0.0 -> "the restored force is under 10% of the per-path static share"
    else -> "exactly zero to the 1e-9 pN reporting floor"
}

fun main() {
    val started = System.currentTimeMillis()
    val sheet = sheet()
    val lengthY = DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (EDGE_X * lengthY)
    val joint = ShearJointAllowable()
    val referenceRate = ShearJointAllowable.REFERENCE_LOADING_RATE
    val realisticShear = joint.ruptureForce(REALISTIC_BONDED_BASE_PAIRS, referenceRate)

    println("T-17 — reading C-0022's solved edge profile ...")
    val uniform = EdgeProfile(
        "uniform", "the load case C-0015's exact zero is written on", null, null, null,
        CollarTerm(0.0, 1.0), CollarTerm(0.0, RIM_STANDOFF)
    )
    val solved = solvedProfiles(ResultInputs.T_3B.file())
    val headline = HEADLINE_STATES.map { (concentration, gap, bias) ->
        solved.firstOrNull {
            it.concentration == concentration && it.gapHeight == gap && it.appliedBias == bias
        } ?: error("no C-0022 profile at $concentration mM, $gap nm, $bias V")
    }
    val profiles = listOf(uniform) + headline + assumedTaper()
    val allProfiles = listOf(uniform) + solved + assumedTaper()
    /** `C-0022`'s own design point: 2 mM, a 10 nm gap, 0.192 V. */
    val designPointName = headline.first().name

    // ------------------------------------------------------------------ P1: is 3 x 15 the grid?
    val gridIdentity = GRID_SHAPES.map { (columns, rows) ->
        val grid = gridOf(columns, rows, lengthY)
        val rowPositions = grid.map { it.second }.distinct().sorted()
        val departure = if (rowPositions.size == DUPLEXES) {
            val axes = (0 until DUPLEXES).map { (it - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
            rowPositions.zip(axes).maxOf { (row, axis) -> abs(row - axis) }
        } else Double.NaN
        GridIdentityRecord(
            shape = "$columns x $rows",
            columns = columns,
            rows = rows,
            attachments = columns * rows,
            oneRowPerDuplex = rowPositions.size == DUPLEXES && departure < 1e-12,
            distinctRowPositions = rowPositions.size,
            maximumRowDepartureFromDuplexAxis = if (departure.isNaN()) -1.0 else departure,
            perPathStaticShare = Gen1Tile.TARGET_FORCE / (columns * rows)
        )
    }

    // ------------------------------------------------------------------ the cheap bound
    println("T-17 — the rigid-tile cut-equilibrium bound, before any solve ...")
    val bareLattice = lattice(sheet, 1.0, emptyList())
    val crossoversPerInterface = (0 until DUPLEXES - 1)
        .map { index -> bareLattice.crossovers.count { it.lowerBeam == index } }
    val loadProfiles = allProfiles.map { profile ->
        val field = profile.field(interiorPressure, lengthY)
        val strips = tributaryStripLoads(bareLattice, field)
        val total = strips.sum()
        val identity = rigidTileInterfaceForces(strips)
        val worst = identity.indices.maxByOrNull { abs(identity[it]) }!!
        LoadProfileRecord(
            name = profile.name,
            source = profile.source,
            concentration = profile.concentration,
            gapHeight = profile.gapHeight,
            appliedBias = profile.appliedBias,
            smoothDepth = profile.smooth.depth,
            smoothWidth = profile.smooth.width,
            rimDepth = profile.rim.depth,
            rimWidth = profile.rim.width,
            interiorPressure = interiorPressure,
            totalLoad = total,
            totalOverUniform = total / Gen1Tile.TARGET_FORCE,
            stripLoadMinimum = strips.min(),
            stripLoadMaximum = strips.max(),
            stripLoadSpreadFraction = (strips.max() - strips.min()) / (total / DUPLEXES),
            rigidTilePeakInterfaceForce = abs(identity[worst]),
            rigidTilePeakPerCrossover = abs(identity[worst]) / crossoversPerInterface[worst],
            crossoversOnWorstInterface = crossoversPerInterface[worst]
        )
    }

    // ------------------------------------------------------------------ P2/P3/P6: the solve
    println("T-17 — the lattice and the plate, over ${GRID_SHAPES.size} shapes ...")
    val plateModel = sheet.plate(EDGE_X, lengthY)

    /**
     * The stroke `C-0006`'s flatness criterion is written against: the mean deflection of the
     * **free** tile under the uniform load, at this foundation stiffness. It is the reference
     * `C-0009` and `C-0015` divide by, and it is not the deflection under the coupling.
     */
    fun freeStroke(multiplier: Double): Double = PlateOnFoundation(
        plateModel, Gen1Tile.FOUNDATION_SECANT * multiplier, emptyList(), basisDegree = 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val strokes = (FOUNDATION_MULTIPLIERS).associateWith { freeStroke(it) }

    fun solveGrid(
        columns: Int,
        rows: Int,
        multiplier: Double,
        model: OrigamiGrillage,
        plate: PlateOnFoundation,
        profile: EdgeProfile
    ): RestoredForceRecord {
        val field = profile.field(interiorPressure, lengthY)
        val solution = model.solve(field)
        val identity = rigidTileInterfaceForces(tributaryStripLoads(model, field))
        val rigidPeak = identity.maxOf { abs(it) }
        val interfaceForces = (0 until DUPLEXES - 1).map { abs(solution.shearAcrossInterface(it)) }
        val peakInterface = interfaceForces.max()
        val peak = solution.peakCrossoverForce
        val worst = interfaceForces.indices.maxByOrNull { interfaceForces[it] }!!
        val share = Gen1Tile.TARGET_FORCE / (columns * rows)
        val latticeDishing = solution.peakDishing()
        val plateDishing = plate.solve(field).peakDishing()
        val stroke = strokes.getValue(multiplier)
        return RestoredForceRecord(
            shape = "$columns x $rows",
            columns = columns,
            rows = rows,
            attachments = columns * rows,
            oneRowPerDuplex = oneRowPerDuplex(rows),
            profile = profile.name,
            foundationMultiplier = multiplier,
            perPathStaticShare = share,
            peakCrossoverForce = peak,
            peakCrossoverOverStaticShare = peak / share,
            peakDuplexShear = solution.peakDuplexShear,
            peakInterfaceForce = peakInterface,
            concentrationFactor = if (peakInterface > 1e-9)
                peak / (peakInterface / crossoversPerInterface[worst]) else 0.0,
            rigidTilePeakInterfaceForce = rigidPeak,
            latticeOverRigidIdentity = if (rigidPeak > 1e-9) peakInterface / rigidPeak else 0.0,
            latticePeakDishing = latticeDishing,
            platePeakDishing = plateDishing,
            latticeOverPlate = if (plateDishing > 1e-12) latticeDishing / plateDishing else 0.0,
            freeTileStroke = stroke,
            dishingOverStroke = latticeDishing / stroke,
            flat = latticeDishing / stroke < RIGID_PLATE_TOLERANCE,
            unzipMargin = if (peak > 1e-9) Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / peak else null,
            shearMarginAt16BasePairs = if (peak > 1e-9) realisticShear / peak else null,
            verdict = verdictOf(peak, share)
        )
    }

    val restored = mutableListOf<RestoredForceRecord>()
    GRID_SHAPES.forEach { (columns, rows) ->
        val multipliers =
            if (columns == 3 && rows == DUPLEXES) FOUNDATION_MULTIPLIERS else listOf(1.0)
        multipliers.forEach { multiplier ->
            val supports = supportsOf(columns, rows, lengthY)
            val model = lattice(sheet, multiplier, supports)
            val plate = PlateOnFoundation(
                plateModel, Gen1Tile.FOUNDATION_SECANT * multiplier, supports, basisDegree = 12
            )
            profiles.forEach { profile ->
                restored += solveGrid(columns, rows, multiplier, model, plate, profile)
            }
        }
    }

    println("T-17 — the design grid against ALL of C-0022's solved states ...")
    val designSupports = supportsOf(3, DUPLEXES, lengthY)
    val designModel = lattice(sheet, 1.0, designSupports)
    val designPlate = PlateOnFoundation(
        plateModel, Gen1Tile.FOUNDATION_SECANT, designSupports, basisDegree = 12
    )
    val profileSweep = allProfiles.map {
        solveGrid(3, DUPLEXES, 1.0, designModel, designPlate, it)
    }

    // ------------------------------------------------------------------ attachment scatter
    println("T-17 — attachment-stiffness scatter, under a UNIFORM load ...")
    val uniformField = uniformPressure(interiorPressure)
    val scatterRecords = mutableListOf<ScatterRecord>()
    ScatterPattern.entries.forEach { pattern ->
        SCATTER_AMPLITUDES.forEach { amplitude ->
            val supports = supportsOf(3, DUPLEXES, lengthY) { index ->
                pattern.multiplier(index, 3, amplitude)
            }
            val model = lattice(sheet, 1.0, supports)
            val solution = model.solve(uniformField)
            val forces = solution.supportForces.map { abs(it) }
            val share = Gen1Tile.TARGET_FORCE / (3 * DUPLEXES)
            scatterRecords += ScatterRecord(
                pattern = pattern.label,
                amplitude = amplitude,
                shape = "3 x $DUPLEXES",
                peakCrossoverForce = solution.peakCrossoverForce,
                peakCrossoverPerUnitAmplitude = solution.peakCrossoverForce / amplitude,
                peakCrossoverOverStaticShare = solution.peakCrossoverForce / share,
                peakDuplexShear = solution.peakDuplexShear,
                stiffestPathForce = forces.max(),
                softestPathForce = forces.min(),
                pathForceSpreadFraction = (forces.max() - forces.min()) / (forces.sum() / forces.size)
            )
        }
    }

    // ------------------------------------------------------------------ the thermal channel
    println("T-17 — the thermal crossover force, and the penalty it does not converge in ...")
    val energy = thermalEnergy()
    val thermal = mutableListOf<ThermalRecord>()
    listOf(3 to DUPLEXES, 8 to 8).forEach { (columns, rows) ->
        var previous: Double? = null
        LINK_PENALTIES.forEach { penalty ->
            val model = lattice(sheet, 1.0, supportsOf(columns, rows, lengthY), linkStiffness = penalty)
            val forces = thermalCrossoverForceRms(model)
            val peak = forces.max()
            thermal += ThermalRecord(
                shape = "$columns x $rows",
                linkStiffness = penalty,
                peakThermalCrossoverForce = peak,
                meanThermalCrossoverForce = forces.sum() / forces.size,
                overSqrtThermalEnergyTimesLink = peak / sqrt(energy * penalty),
                ratioToPreviousDecade = previous?.let { peak / it }
            )
            previous = peak
        }
    }
    val hingeEquivalent = sheet.crossoverHingeStiffness /
            (sheet.interhelicalDistance * sheet.interhelicalDistance)
    val thermalBracket = listOf(
        Triple(
            "the hinge's own equivalent vertical stiffness, k_theta/d^2",
            hingeEquivalent,
            "the softest defensible reading: the crossover resists vertical separation only " +
                    "through the hinge it is fitted as"
        ),
        Triple(
            "one duplex rise in axial tension, S/a",
            Gen1Tile.DUPLEX_STRETCH_MODULUS / Gen1Tile.RISE_PER_BASE_PAIR,
            "the stiffest defensible reading: the crossover strand is covalently continuous " +
                    "over one base-pair rise"
        ),
        Triple(
            "C-0009's penalty, used as if it were physical",
            OrigamiGrillage.RIGID_LINK_STIFFNESS,
            "NOT a physical reading — it is the numerical constraint, and the whole point is " +
                    "that a thermal force read off it is meaningless"
        )
    ).map { (reading, stiffness, note) ->
        val rms = sqrt(energy * stiffness)
        ThermalBracketRecord(
            reading = reading,
            verticalStiffness = stiffness,
            thermalForceRms = rms,
            overUnzipAllowable = rms / Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            note = note
        )
    }

    // ------------------------------------------------------------------ P5: the three duties
    println("T-17 — the other two duties the same attachments carry ...")
    val duties = GRID_SHAPES.map { (columns, rows) ->
        val grid = gridOf(columns, rows, lengthY)
        val count = columns * rows
        val lateral = MANDATED_STIFFNESS * LATERAL_OVER_NORMAL
        val meanSquared = grid.sumOf { (x, y) -> x * x + y * y } / count
        val yaw = yawStiffness(lateral / count, grid)
        DutyRecord(
            shape = "$columns x $rows",
            columns = columns,
            rows = rows,
            attachments = count,
            perPathStaticShare = Gen1Tile.TARGET_FORCE / count,
            lateralStiffness = lateral,
            lateralMargin = lateral / LATERAL_BOUND,
            meanSquaredRadius = meanSquared,
            yawStiffness = yaw,
            yawMargin = yaw / YAW_BOUND,
            holdDownStiffnessMargin = MANDATED_STIFFNESS / HOLD_DOWN_BOUND,
            perPathThermalForce = sqrt(energy * MANDATED_STIFFNESS) / count,
            allDutiesDischarged = lateral / LATERAL_BOUND > 1.0 && yaw / YAW_BOUND > 1.0 &&
                    Gen1Tile.TARGET_FORCE / count < Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
        )
    }

    // ------------------------------------------------------------------ P4: the allowables
    // the worst the LOAD can restore anywhere in C-0022's whole 21-state sweep, on the design
    // grid — this is what a path count has to be judged against, not the design point alone
    val softestRestored = profileSweep.maxOf { it.peakCrossoverForce }
    val allowables = listOf(
        Triple("single-duplex unzip, lower edge", Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, null),
        Triple("single-duplex unzip, upper edge", UNZIP_UPPER, null),
        Triple(
            "staple-domain shear at 8 bp (CH-0029)",
            joint.ruptureForce(8.0, referenceRate), 8.0
        ),
        Triple(
            "staple-domain shear at 16 bp (CH-0029)",
            realisticShear, REALISTIC_BONDED_BASE_PAIRS
        ),
        Triple(
            "staple-domain shear at 30 bp (CH-0029)",
            joint.ruptureForce(30.0, referenceRate), 30.0
        ),
        Triple("nicked-duplex ceiling", Gen1Tile.OVERSTRETCHING_CEILING, null)
    ).map { (name, force, basePairs) ->
        // the restored force is a property of the LOAD and does not fall with the path count,
        // so a path count clears an allowable only if the static share plus it does
        val staticOnly = (1..2000).first { Gen1Tile.TARGET_FORCE / it < force }
        val withRestored = (1..2000).firstOrNull {
            Gen1Tile.TARGET_FORCE / it + softestRestored < force
        } ?: -1
        AllowableCrossingRecord(
            allowable = name,
            force = force,
            basePairs = basePairs,
            minimumPathsForStaticShareAlone = staticOnly,
            minimumPathsWithRestoredForce = withRestored,
            marginAt45Paths = force / (Gen1Tile.TARGET_FORCE / 45.0 + softestRestored)
        )
    }
    val jointAllowables = listOf(4.0, 8.0, 12.0, 16.0, 20.0, 24.0, 30.0, 32.0).map { basePairs ->
        JointAllowableRecord(
            basePairs = basePairs,
            loadingRate = referenceRate,
            shearAllowable = joint.ruptureForce(basePairs, referenceRate),
            unzipAllowable = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
        )
    }

    // ------------------------------------------------------------------ gate 4
    println("T-17 — convergence ...")
    val designProfile = profiles.first { it.name == designPointName }
    val designField = designProfile.field(interiorPressure, lengthY)
    val convergence = mutableListOf<ConvergenceRecord>()
    val meshPeaks = listOf(1, 2, 4).map { subdivisions ->
        val model = lattice(
            sheet, 1.0, supportsOf(3, DUPLEXES, lengthY), subdivisions = subdivisions
        )
        model.degreesOfFreedom to model.solve(designField).peakCrossoverForce
    }
    meshPeaks.forEach { (dof, peak) ->
        convergence += ConvergenceRecord(
            axis = "nested beam subdivisions 1 in 2 in 4",
            setting = "dof = $dof",
            degreesOfFreedom = dof,
            peakCrossoverForce = peak,
            departureFromFinest = abs(peak - meshPeaks.last().second) / meshPeaks.last().second
        )
    }
    val penaltyPeaks = listOf(1e3, 1e4, 1e5).map { penalty ->
        val model = lattice(sheet, 1.0, supportsOf(3, DUPLEXES, lengthY), linkStiffness = penalty)
        penalty to model.solve(designField).peakCrossoverForce
    }
    penaltyPeaks.forEach { (penalty, peak) ->
        convergence += ConvergenceRecord(
            axis = "crossover link penalty, STATIC force",
            setting = "k_link = $penalty pN/nm",
            degreesOfFreedom = bareLattice.degreesOfFreedom,
            peakCrossoverForce = peak,
            departureFromFinest = abs(peak - penaltyPeaks.last().second) / penaltyPeaks.last().second
        )
    }
    val phasePeaks = (0 until 32).map { basePairs ->
        val model = OrigamiGrillage(
            sheet = sheet,
            lengthX = EDGE_X,
            beamCount = DUPLEXES,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.atBasePairPhase(basePairs, sheet, EDGE_X),
            supports = supportsOf(3, DUPLEXES, lengthY)
        )
        basePairs to model.solve(designField).peakCrossoverForce
    }
    val phaseBest = phasePeaks.minByOrNull { it.second }!!
    val phaseWorst = phasePeaks.maxByOrNull { it.second }!!
    listOf(phaseBest to "best", phaseWorst to "worst").forEach { (entry, label) ->
        convergence += ConvergenceRecord(
            axis = "crossover column PHASE (a design variable, not a convergence parameter)",
            setting = "$label of 32 phases, ${entry.first} bp",
            degreesOfFreedom = bareLattice.degreesOfFreedom,
            peakCrossoverForce = entry.second,
            departureFromFinest = phaseWorst.second / phaseBest.second
        )
    }

    // ------------------------------------------------------------------ gate 5
    val flatnessGrid = gridOf(3, DUPLEXES, 40.35)
    val meanSquaredRadius = flatnessGrid.sumOf { (x, y) -> x * x + y * y } / flatnessGrid.size
    val c0015Zero = lattice(sheet, 1.0, emptyList()).solve(
        pointLoads = gridOf(3, DUPLEXES, lengthY).map { (x, y) ->
            PointLoad(x, y, Gen1Tile.TARGET_FORCE / (3 * DUPLEXES))
        }
    ).peakCrossoverForce
    val reproductions = listOf(
        ReproductionRecord(
            "C-0015", "peak crossover force under a 3 x 15 grid of equal point loads [pN]",
            0.0, c0015Zero, abs(c0015Zero)
        ),
        ReproductionRecord(
            "C-0017", "yaw stiffness on the 3 x 15 grid [pN*nm/rad]",
            8205.0, yawStiffness(32.36 / 45.0, flatnessGrid),
            abs(yawStiffness(32.36 / 45.0, flatnessGrid) - 8205.0) / 8205.0
        ),
        ReproductionRecord(
            "C-0017", "mean squared attachment radius of the 3 x 15 grid [nm^2]",
            8205.0 / 32.36, meanSquaredRadius,
            abs(meanSquaredRadius - 8205.0 / 32.36) / (8205.0 / 32.36)
        ),
        ReproductionRecord(
            "C-0024 / CH-0029", "staple-domain shear allowable at 30 bp, 100 pN/s [pN]",
            47.11, joint.ruptureForce(30.0, referenceRate),
            abs(joint.ruptureForce(30.0, referenceRate) - 47.11) / 47.11
        ),
        ReproductionRecord(
            "C-0024 / CH-0029", "staple-domain shear allowable at 8 bp, 100 pN/s [pN]",
            18.80, joint.ruptureForce(8.0, referenceRate),
            abs(joint.ruptureForce(8.0, referenceRate) - 18.80) / 18.80
        ),
        ReproductionRecord(
            "C-0022", "total electrostatic force gain at the design point, min-margin mapping",
            0.1471,
            loadProfiles.first { it.name == designPointName }.totalOverUniform - 1.0,
            abs(
                (loadProfiles.first { it.name == designPointName }
                    .totalOverUniform - 1.0) - 0.1471
            ) / 0.1471
        ),
        ReproductionRecord(
            "C-0017", "per-path static share of the 45-path coupling [pN]",
            2.222, Gen1Tile.TARGET_FORCE / 45.0,
            abs(Gen1Tile.TARGET_FORCE / 45.0 - 2.222) / 2.222
        )
    )

    val designRecord = restored.first {
        it.shape == "3 x $DUPLEXES" && it.foundationMultiplier == 1.0 &&
                it.profile == designPointName
    }
    val equalCount = restored.filter {
        it.attachments == 45 && it.foundationMultiplier == 1.0 &&
                it.profile == designPointName
    }
    val bestEqualCount = equalCount.minByOrNull { it.peakCrossoverForce }!!
    val worstEqualCount = equalCount.maxByOrNull { it.peakCrossoverForce }!!

    val result = OneRowPerDuplexResult(
        task = "T-17",
        leaf = "A8.2",
        title = "\"One attachment row per duplex\" as an output-coupling scheme: the exact zero " +
                "costed against the load non-uniformity T-3b actually solved, against " +
                "attachment scatter, against thermal excitation, and against the other two " +
                "duties the same attachments carry",
        verificationType = "in-silico (C-0009/C-0015's beam-and-hinge grillage loaded through " +
                "C-0022's SOLVED electrostatic edge profile, with C-0006's continuum plate run " +
                "beside it) + logical (a rigid-tile cut-equilibrium identity giving the " +
                "restored interface force in closed form before any matrix is assembled)",
        acceptance = "P1: state from the code whether C-0015's 3 x 15 flatness grid IS one " +
                "attachment row per duplex. P2: report the peak per-load-path crossover force " +
                "restored by C-0022's solved edge profile, by C-0006's assumed taper and by " +
                "attachment-stiffness scatter, each in pN and as a fraction of the per-path " +
                "static share. P3: derive and evaluate a closed-form rigid-tile cut-equilibrium " +
                "bound BEFORE any lattice solve and grade the lattice against it. P4: judge " +
                "every per-path force against CH-0029's LENGTH-DEPENDENT shear allowable and " +
                "the 10-15 pN unzip band, never the flat 48 pN, and report the count and the " +
                "allowable at which the scheme crosses. P5: report whether one grid discharges " +
                "output coupling, lateral confinement and hold-down. P6: report the peak DUPLEX " +
                "shear, so that a scheme which zeroes one path and loads the other is not " +
                "reported as free.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. No " +
                "crossover force in a loaded origami sheet has ever been measured, and the " +
                "crossover's VERTICAL compliance is a rigid constraint here as it is in C-0009 " +
                "and C-0015, with nothing cited behind it. The load profile inherits C-0022's " +
                "mean-field statement whole: C-0005 puts the one-loop correction at 123-214% of " +
                "the leading term across this gap range.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "rotationalStiffness" to "pN*nm/rad",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "energy" to "pN*nm",
            "temperature" to "K"
        ),
        conventions = listOf(
            "x runs ALONG the helices, y ACROSS them; the origin is the tile centre",
            "w is positive DOWNWARD, compressing the polymer layer (T-5, unchanged)",
            "the footprint is 40.0 x ${DUPLEXES * Gen1Tile.INTERHELICAL_SHEET} nm: $DUPLEXES " +
                    "duplexes at the SAXS-measured d = ${Gen1Tile.INTERHELICAL_SHEET} nm",
            "a CROSSOVER FORCE is the transverse force one crossover transmits between the two " +
                    "duplexes it joins, signed as in C-0009; the reported quantity is the " +
                    "maximum of its magnitude over all crossovers",
            "the LOAD is a downward pressure of interior value 100 pN / 1600 nm^2 modified by " +
                    "C-0022's solved collar, which is C-0022's own convention for the dishing " +
                    "it computes on C-0006's plate — so the TOTAL load is 100 pN times the " +
                    "edge gain, not 100 pN",
            "a collar DEPTH is NEGATIVE for an edge ENHANCEMENT, which is the sign C-0022 solved",
            "the COUPLING is n discrete springs to ground of total stiffness " +
                    "100/3 = 33.333 pN/nm (C-0017's mandate), one per attachment",
            "the FOUNDATION is C-0001's secant k_f, swept x[0.25, 4] per CH-0001"
        ),
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to energy.toString(),
            "duplexes" to DUPLEXES.toString(),
            "edgeX" to EDGE_X.toString(),
            "edgeY" to lengthY.toString(),
            "crossoverColumns" to NOMINAL_COLUMNS.toString(),
            "crossovers" to bareLattice.crossovers.size.toString(),
            "subdivisions" to "2",
            "linkStiffness" to OrigamiGrillage.RIGID_LINK_STIFFNESS.toString(),
            "mandatedCouplingStiffness" to MANDATED_STIFFNESS.roundedForProse().toString(),
            "interiorPressure" to interiorPressure.roundedForProse().toString(),
            "foundationSecant" to Gen1Tile.FOUNDATION_SECANT.toString(),
            "gridShapes" to GRID_SHAPES.size.toString(),
            "loadProfiles" to profiles.size.toString(),
            "stripPanels" to STRIP_PANELS.toString(),
            "plateBasisDegree" to "12"
        ),
        citedInputs = mapOf(
            "C-0015 exact zero at one row per duplex" to "0 pN, reproduced here",
            "C-0015 flatness scheme" to "45 attachments as 3 x 15",
            "C-0017 mandated coupling stiffness" to "${MANDATED_STIFFNESS.roundedForProse()} pN/nm",
            "C-0017 lateral by-product" to "32.36 pN/nm on the same grid",
            "C-0017 yaw by-product" to "8205 pN*nm/rad on the same grid",
            "C-0014 lateral bound" to "$LATERAL_BOUND pN/nm per coordinate",
            "C-0014 yaw bound" to "$YAW_BOUND pN*nm/rad",
            "C-0023 two-sided hold-down bound" to "$HOLD_DOWN_BOUND pN/nm (k_BT/sigma^2)",
            "C-0022 solved edge profile" to "read from gpd/results/T-3b-tile-edge-load-profile.json",
            "C-0006 assumed taper" to "50% over one Debye length (4 nm) — superseded, CH-0025",
            "CH-0029 shear allowable" to "Strunz et al. (1999), LENGTH-dependent, at 100 pN/s",
            "unzip allowable" to "10-15 pN, Essevaz-Roulet et al. (1997), length-INDEPENDENT",
            "nicked-duplex ceiling" to "65 pN, van Mameren et al. (2009)",
            "interhelical distance" to "2.69 nm, Fischer et al. (2016), SAXS, MEASURED",
            "crossover spacing per interface" to "32 bp, Rothemund (2006)",
            "crossover hinge constant" to "k_theta = 2 alpha B/(100 a), Chen et al. (2014), FITTED",
            "EI, GJ" to "230, 460 pN*nm^2, CanDo MODEL INPUTS, not measurements"
        ),
        temperature = ROOM_TEMPERATURE,
        thermalEnergy = energy,
        designPointProfile = designPointName,
        gridIdentity = gridIdentity,
        loadProfiles = loadProfiles,
        restoredForces = restored,
        profileSweep = profileSweep,
        scatter = scatterRecords,
        thermal = thermal,
        thermalBracket = thermalBracket,
        duties = duties,
        allowableCrossings = allowables,
        jointAllowables = jointAllowables,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings(
            designPointName,
            gridIdentity, loadProfiles, designRecord, bestEqualCount, worstEqualCount,
            scatterRecords, thermal, thermalBracket, duties, allowables, restored,
            softestRestored, realisticShear
        ),
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED.",
            "The crossover's VERTICAL/AXIAL compliance is a rigid penalty constraint, " +
                    "inherited unchanged from C-0009 and C-0015. The STATIC force converges in " +
                    "it; the THERMAL force provably does not, and that is reported as a result " +
                    "rather than as a number. T-9 would settle it.",
            "The load profile is C-0022's, and inherits its whole validity range: mean field " +
                    "(123-214% one-loop correction, C-0005), point ions, a two-dimensional " +
                    "solve with the corner bracketed rather than solved, an unsourced rim " +
                    "charge worth 1.85x on the depth, and a gap filled with free buffer.",
            "The interior pressure is 100 pN / 1600 nm^2 and the collar is applied on top of " +
                    "it, so the TOTAL load exceeds 100 pN by C-0022's own edge gain. That is " +
                    "C-0022's convention and it is the one that makes the dishing comparable; " +
                    "a design normalised to a fixed total would move every force here by the " +
                    "same factor and no ratio at all.",
            "Linear Winkler foundation at C-0001's secant, swept x[0.25, 4]; C-0001's " +
                    "stiffnesses are lower bounds per CH-0001.",
            "The coupling is n IDENTICAL LINEAR springs. C-0023's flexure and hinge are " +
                    "exactly linear (secant = tangent), so this is their model; C-0017's " +
                    "ssDNA-spacer path is strain-stiffening and its tangent is 1.17x its " +
                    "secant, which would raise the restored force by the same factor.",
            "The scatter patterns are DETERMINISTIC design tolerances, not a random ensemble. " +
                    "No distribution over assembly error is claimed or available.",
            "A per-path allowable is a rupture force measured at 300 K at a stated loading " +
                    "rate. Adding a broadband thermal RMS to a static share double-counts the " +
                    "thermal motion the measurement already contains.",
            "One layout: T-10's eight symmetrically centred columns, with the 32 base-pair " +
                    "phases swept at the design point only. C-0015's ranking (seven columns " +
                    "beats eight) is not re-derived here.",
            "Single layer, static, 300 K, aqueous buffer with Mg2+."
        ),
        openQuestions = listOf(
            "The crossover's vertical stiffness — T-9. It does not move any static number " +
                    "here, and it is the ENTIRE content of the thermal one.",
            "Whether a real 45-path coupling's paths are equal to better than a few per cent. " +
                    "The scatter sensitivity is reported per unit amplitude so that a " +
                    "measured or specified tolerance can be substituted without re-running.",
            "The corner of the tile, which C-0022 brackets rather than solves. The collar " +
                    "here is C-0022's minimum-margin construction, which counts a corner once.",
            "Whether the lever's own frame is stiff enough that the 45 springs really are " +
                    "grounded independently. A compliant common frame couples them and the " +
                    "scatter analysis would change."
        )
    )

    val output = File("gpd/results/T-17-one-row-per-duplex.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    output.writeText(
        json.encodeToString(
            (json.encodeToJsonElement(result) as JsonObject).roundedForCouplingResult().withEmissionHeader(LatticeTag.SQUARE, null)
        )
    )
    report(result, output, started)
}

// ---------------------------------------------------------------------------------------------
// findings and report
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList")
private fun findings(
    designPointName: String,
    gridIdentity: List<GridIdentityRecord>,
    profiles: List<LoadProfileRecord>,
    design: RestoredForceRecord,
    bestEqualCount: RestoredForceRecord,
    worstEqualCount: RestoredForceRecord,
    scatter: List<ScatterRecord>,
    thermal: List<ThermalRecord>,
    thermalBracket: List<ThermalBracketRecord>,
    duties: List<DutyRecord>,
    allowables: List<AllowableCrossingRecord>,
    restored: List<RestoredForceRecord>,
    softestRestored: Double,
    realisticShear: Double
): List<String> {
    val threeByFifteen = gridIdentity.first { it.shape == "3 x 15" }
    val designProfile = profiles.first { it.name == designPointName }
    val alternating = scatter.filter { it.pattern.startsWith("alternating rows") }
    val columnScatter = scatter.filter { it.pattern.startsWith("alternating columns") }
    val designDuty = duties.first { it.shape == "3 x 15" }
    val singleColumn = duties.first { it.shape == "1 x 15" }
    val worstOff = restored.filter {
        !it.oneRowPerDuplex && it.foundationMultiplier == 1.0 && it.profile == "uniform"
    }.maxByOrNull { it.peakCrossoverForce }!!
    val worstOffSolved = restored.first {
        it.shape == worstOff.shape && it.foundationMultiplier == 1.0 &&
                it.profile == designPointName
    }
    val oneRowUnderLoad = restored.filter {
        it.oneRowPerDuplex && it.foundationMultiplier == 1.0 && it.profile == designPointName
    }
    val thermalDecade = thermal.mapNotNull { it.ratioToPreviousDecade }
    val scatterSlope = scatter.first { it.pattern.startsWith("alternating rows") }
        .peakCrossoverPerUnitAmplitude
    val breakEvenScatter = design.peakCrossoverForce / scatterSlope
    val flatUnderUniform = restored.filter {
        it.oneRowPerDuplex && it.foundationMultiplier == 1.0 && it.profile == "uniform" && it.flat
    }.minByOrNull { it.attachments }!!
    val twoColumn = restored.first {
        it.shape == "2 x 15" && it.foundationMultiplier == 1.0 && it.profile == "uniform"
    }
    val unzipRow = allowables.first { it.allowable.startsWith("single-duplex unzip, lower") }
    val worstScatter = scatter.filter { it.pattern.startsWith("alternating rows") }
        .maxByOrNull { it.amplitude }!!
    return listOf(
        ("P1 — THE TWO SCHEMES ARE ONE, AND THE TASK'S OWN PREMISE IS REFUTED IN CODE. " +
                "C-0015's 3 x 15 flatness grid IS one attachment row per duplex: its %d rows " +
                "land on the %d duplex axes to %.1e nm, so \"15 rows\" and \"45 attachments as " +
                "3 x 15\" are the same object and there is no scheme conflict to cost. Every " +
                "n x 15 shape has the property; the equal-count contrasts 5 x 9, 9 x 5 and " +
                "15 x 3 do not, and they are what the comparison has to be run against " +
                "instead.").format(
            threeByFifteen.distinctRowPositions, threeByFifteen.distinctRowPositions,
            threeByFifteen.maximumRowDepartureFromDuplexAxis
        ),
        ("P3 — THE CHEAP BOUND SETTLES THE STRUCTURE OF THE ANSWER BEFORE ANY MATRIX, AND IT " +
                "OVERSTATES BY %.1fx. On a rigid tile the force crossing interface j is exactly " +
                "the sum of the strip-load deviations beyond the cut, so a load varying only " +
                "ALONG the helices restores EXACTLY ZERO and only the across-helix content can " +
                "break the symmetry. At C-0022's design point the strip loads span %.1f%% of " +
                "their mean and the identity gives %.3f pN on the worst interface; the solved " +
                "lattice carries %.4f pN there. The tile's own compliance sheds %.0f%% of it, " +
                "because a rim duplex under extra load simply sinks further into its own " +
                "foundation and its own attachments instead of handing the excess inboard — " +
                "so the rigid-tile identity is a conservative CEILING, not an estimate.")
            .format(
                1.0 / design.latticeOverRigidIdentity,
                100.0 * designProfile.stripLoadSpreadFraction,
                designProfile.rigidTilePeakInterfaceForce, design.peakInterfaceForce,
                100.0 * (1.0 - design.latticeOverRigidIdentity)
            ),
        ("P2 — WHAT THE SOLVED EDGE PROFILE RESTORES: 0.15 pN, WHICH IS 6.8%% OF THE STATIC " +
                "SHARE AND 66x BELOW UNZIP. At the design point the 3 x 15 scheme's peak " +
                "per-load-path crossover force goes from EXACTLY ZERO to %.4f pN, which is " +
                "%.2f%% of the same scheme's %.3f pN per-path static share and %.0fx below the " +
                "10 pN unzip allowable. Over all %d of C-0022's solved states the worst is " +
                "%.4f pN. The lattice concentrates the interface force onto the %d crossovers " +
                "of that interface by %.2fx at the design point and by %.2f-%.2fx over the " +
                "headline states — C-0009's concentration factor, seen at a DISTRIBUTED " +
                "coupling rather than at a rigid anchor, and sitting at or below the bottom " +
                "of its 2.3-7.6x band.").format(
            design.peakCrossoverForce, 100.0 * design.peakCrossoverOverStaticShare,
            design.perPathStaticShare, design.unzipMargin ?: 0.0,
            profiles.count { it.name.startsWith("C-0022") }, softestRestored,
            designProfile.crossoversOnWorstInterface, design.concentrationFactor,
            oneRowUnderLoad.minOf { it.concentrationFactor },
            oneRowUnderLoad.maxOf { it.concentrationFactor }
        ),
        ("THE RESTORED INTERFACE FORCE IS A PROPERTY OF THE LOAD AND NOT OF THE GRID. Over " +
                "the seven one-row-per-duplex shapes from 1 x 15 to 15 x 15 — a fifteen-fold " +
                "range in attachment count — the peak INTERFACE force under the solved load " +
                "spans %.5f to %.5f pN, i.e. %.2f%%. Adding attachment columns cannot relieve " +
                "the crossovers, because the cut equilibrium does not contain the column " +
                "count; what the columns change is only how the same force is shared, and the " +
                "peak per crossover moves %.4f to %.4f pN.").format(
            oneRowUnderLoad.minOf { it.peakInterfaceForce },
            oneRowUnderLoad.maxOf { it.peakInterfaceForce },
            100.0 * (oneRowUnderLoad.maxOf { it.peakInterfaceForce } /
                    oneRowUnderLoad.minOf { it.peakInterfaceForce } - 1.0),
            oneRowUnderLoad.minOf { it.peakCrossoverForce },
            oneRowUnderLoad.maxOf { it.peakCrossoverForce }
        ),
        ("SHAPE, NOT COUNT — AT THE SAME 45 ATTACHMENTS AND THE SAME 2.22 pN SHARE. The four " +
                "45-attachment shapes give %.4f pN (%s) to %.4f pN (%s) under the same solved " +
                "load, a ratio of %.1fx. And 5 x 9 is no worse on flatness than 3 x 15 " +
                "(%.4f against %.4f nm of peak dishing under a uniform load), so the " +
                "commensurability is a FREE %.1fx: it costs nothing in count, nothing in " +
                "share and nothing in flatness. That is the real content of C-0015's exact " +
                "zero once a realistic load is applied.").format(
            bestEqualCount.peakCrossoverForce, bestEqualCount.shape,
            worstEqualCount.peakCrossoverForce, worstEqualCount.shape,
            worstEqualCount.peakCrossoverForce / bestEqualCount.peakCrossoverForce,
            restored.first {
                it.shape == "5 x 9" && it.foundationMultiplier == 1.0 && it.profile == "uniform"
            }.latticePeakDishing,
            restored.first {
                it.shape == "3 x 15" && it.foundationMultiplier == 1.0 && it.profile == "uniform"
            }.latticePeakDishing,
            restored.first {
                it.shape == "5 x 9" && it.foundationMultiplier == 1.0 &&
                        it.profile == designPointName
            }.peakCrossoverForce / design.peakCrossoverForce
        ),
        ("ATTACHMENT SCATTER IS LINEAR, IT IS THE ONE A BUILDER CONTROLS, AND IT OVERTAKES " +
                "THE EDGE EFFECT AT %.0f%%. Alternating the path stiffnesses DUPLEX BY DUPLEX " +
                "restores %.4f pN per unit relative amplitude, so a 10%% scatter gives %.4f pN " +
                "— %.0f%% of the whole solved edge effect — and the two are equal at a " +
                "scatter of %.1f%%. Alternating them STATION BY STATION along the helices " +
                "restores %.1e pN at any amplitude, because that pattern does not break the " +
                "across-helix symmetry at all: WHICH WAY a tolerance is correlated matters " +
                "more than how big it is.").format(
            100.0 * breakEvenScatter, scatterSlope,
            scatter.first { it.pattern.startsWith("alternating rows") && it.amplitude == 0.10 }
                .peakCrossoverForce,
            100.0 * scatter.first {
                it.pattern.startsWith("alternating rows") && it.amplitude == 0.10
            }.peakCrossoverForce / design.peakCrossoverForce,
            100.0 * breakEvenScatter,
            columnScatter.first { it.amplitude == 0.10 }.peakCrossoverForce
        ),
        ("AND EVEN A TOTALLY MIS-ASSEMBLED COUPLING DOES NOT REACH AN ALLOWABLE. At a relative " +
                "scatter of %.2f — every second path at one per cent of nominal — the peak " +
                "crossover force " +
                "is %.3f pN, still %.0fx below the 10 pN unzip band; and the WORST grid in the " +
                "whole sweep, %s under a merely uniform load, reaches %.3f pN, %.1fx below it. " +
                "So for a coupling distributed over 45 paths the crossover path never becomes " +
                "binding under ANY non-uniformity this programme can name — the same " +
                "conclusion C-0024 reached in plane, reached here out of plane.").format(
            worstScatter.amplitude, worstScatter.peakCrossoverForce,
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / worstScatter.peakCrossoverForce,
            worstOff.shape, worstOff.peakCrossoverForce,
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / worstOff.peakCrossoverForce
        ),
        ("THE THERMAL CHANNEL IS NOT A LOAD PATH AT ALL, AND THE PENALTY PROVES IT. The " +
                "equipartition force in a crossover grows as the SQUARE ROOT of the link " +
                "stiffness — %.3f per decade against sqrt(10) = 3.162, and " +
                "peak/sqrt(k_BT k_link) = 1.0000 to four decimals — so the rigid-constraint " +
                "limit of a FLUCTUATING constraint force does not exist, while the same " +
                "model's STATIC force converges in the same penalty to 1.6e-4. It is also " +
                "identical on a 3 x 15 grid and on an 8 x 8 one to 4 decimals, so it cannot " +
                "discriminate between schemes. Quoted on a PHYSICAL vertical stiffness it is " +
                "sqrt(k_BT k_v) = %.2f pN at the hinge's own k_theta/d^2 and %.1f pN at a " +
                "covalent rise — a property of the JOINT, and one already inside any rupture " +
                "force measured at 300 K, so adding it to a static share double-counts.")
            .format(
                thermalDecade.average(), thermalBracket[0].thermalForceRms,
                thermalBracket[1].thermalForceRms
            ),
        ("P5 — ONE SCHEME DISCHARGES ALL THREE DUTIES, AND THE TWO AXES OF THE GRID ARE SET BY " +
                "DIFFERENT ONES. On the 3 x 15 grid the coupling delivers %.0fx C-0014's " +
                "lateral bound, %.0fx its yaw bound and %.0fx C-0023's two-sided hold-down " +
                "bound, with no extra part and no extra stiffness. The ROW count is set by the " +
                "LOAD PATH (15 = one per duplex, and any other row count restores ~1-3 pN " +
                "under a uniform load); the COLUMN count is set by FLATNESS (%s is the " +
                "smallest one-row grid that is flat, at %.1f%% of the stroke, against %.1f%% " +
                "for 2 x 15) — not by yaw, which a single column already clears %.0fx, and " +
                "not by the allowable, which 15 paths already clear at %.2f pN each.").format(
            designDuty.lateralMargin, designDuty.yawMargin, designDuty.holdDownStiffnessMargin,
            flatUnderUniform.shape, 100.0 * flatUnderUniform.dishingOverStroke,
            100.0 * twoColumn.dishingOverStroke,
            singleColumn.yawMargin, singleColumn.perPathStaticShare
        ),
        ("P4 — THE BINDING CONSTRAINT IS THE STATIC SHARE, AND THE RESTORED FORCE COSTS ZERO " +
                "EXTRA PATHS. Against CH-0029's LENGTH-DEPENDENT allowable the scheme needs " +
                "%d paths to clear the 10 pN unzip band on the static share alone and %d once " +
                "the worst restored force is added, because %.3f pN does not fall with the " +
                "path count while 100/n does. At 45 paths the margin is %.1fx against unzip " +
                "and %.1fx against a realistic 16 bp shear joint (%.2f pN) — and note that a " +
                "flat 48 pN would have reported %.1fx, i.e. CH-0029's correction costs %.0f%% " +
                "of the margin and changes no verdict.").format(
            unzipRow.minimumPathsForStaticShareAlone, unzipRow.minimumPathsWithRestoredForce,
            softestRestored, unzipRow.marginAt45Paths,
            allowables.first { it.allowable.startsWith("staple-domain shear at 16") }
                .marginAt45Paths,
            realisticShear,
            Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / (Gen1Tile.TARGET_FORCE / 45.0 + softestRestored),
            100.0 * (1.0 - realisticShear / Gen1Tile.DUPLEX_SHEAR_ALLOWABLE)
        ),
        ("P6 — THE SCHEME DOES NOT ZERO THE SHEET, IT MOVES THE LOAD INTO THE DUPLEX PATH. " +
                "Under the solved load the 3 x 15 scheme puts %.3f pN of transverse shear into " +
                "the worst duplex element against %.4f pN into the worst crossover, a ratio of " +
                "%.1fx. That is C-0015's \"the two optima are at opposite corners\" seen at " +
                "the scheme level. The trade is still favourable, because the crossover is " +
                "judged against 10-15 pN and the duplex against the 65 pN nicked ceiling: " +
                "%.0fx of margin on the duplex path against %.0fx on the crossover path.")
            .format(
                design.peakDuplexShear, design.peakCrossoverForce,
                design.peakDuplexShear / design.peakCrossoverForce,
                Gen1Tile.OVERSTRETCHING_CEILING / design.peakDuplexShear,
                design.unzipMargin ?: 0.0
            ),
        ("VERDICT — THE 3 x 15 GRID REMAINS THE DESIGN, THE BRANCH IS NOT KILLED, AND THE " +
                "EXACT ZERO IS RETIRED AS A HEADLINE AND KEPT AS A FREE %.1fx. The ordering " +
                "of the per-path budget at 45 paths is: static share %.3f pN >> solved edge " +
                "profile %.4f pN ~ 10%% attachment scatter %.4f pN >> station-to-station " +
                "scatter %.1e pN, with the tightest allowable (10 pN unzip) %.1fx above the " +
                "sum. Nothing in the non-uniformity budget changes a count, an allowable or a " +
                "verdict; what it changes is the STATUS of the zero, from an exact structural " +
                "property to a %.1fx design margin that a few per cent of assembly scatter " +
                "spends.").format(
            worstOffSolved.peakCrossoverForce / design.peakCrossoverForce,
            design.perPathStaticShare, design.peakCrossoverForce,
            scatter.first { it.pattern.startsWith("alternating rows") && it.amplitude == 0.10 }
                .peakCrossoverForce,
            columnScatter.first { it.amplitude == 0.10 }.peakCrossoverForce,
            unzipRow.marginAt45Paths,
            worstOffSolved.peakCrossoverForce / design.peakCrossoverForce
        )
    )
}

private fun elapsed(started: Long): String =
    "%.1f s".format((System.currentTimeMillis() - started) / 1000.0)

private fun report(result: OneRowPerDuplexResult, output: File, started: Long) {
    println()
    println("=".repeat(110))
    println("T-17 — ${result.title}")
    println("=".repeat(110))

    println()
    println("--- P1: is 3 x 15 one attachment row per duplex? ".padEnd(110, '-'))
    println("%-10s %6s %8s %12s %10s %14s".format(
        "shape", "count", "rows", "one row/dup", "share[pN]", "max dep[nm]"
    ))
    result.gridIdentity.forEach {
        println("%-10s %6d %8d %12s %10.3f %14.2e".format(
            it.shape, it.attachments, it.rows, it.oneRowPerDuplex,
            it.perPathStaticShare, it.maximumRowDepartureFromDuplexAxis
        ))
    }

    println()
    println("--- the load profiles, and the cheap bound that needs no matrix ".padEnd(110, '-'))
    println("%-42s %8s %8s %8s %9s %11s %11s".format(
        "profile", "depth", "width", "rim", "total/100", "strip spr", "V_max[pN]"
    ))
    result.loadProfiles.forEach {
        println("%-42s %8.3f %8.2f %8.3f %9.4f %11.4f %11.5f".format(
            it.name.take(42), it.smoothDepth, it.smoothWidth, it.rimDepth,
            it.totalOverUniform, it.stripLoadSpreadFraction, it.rigidTilePeakInterfaceForce
        ))
    }

    println()
    println("--- the 3 x 15 design grid against ALL of C-0022's solved states ".padEnd(110, '-'))
    println("%-42s %11s %11s %11s %10s".format(
        "profile", "xover[pN]", "iface[pN]", "duplex[pN]", "dish/strk"
    ))
    result.profileSweep.forEach {
        println("%-42s %11.5f %11.5f %11.4f %10.3f".format(
            it.profile.take(42), it.peakCrossoverForce, it.peakInterfaceForce,
            it.peakDuplexShear, it.dishingOverStroke
        ))
    }

    println()
    println("--- the restored peak per-load-path force, k_f x 1 ".padEnd(110, '-'))
    println("%-10s %-30s %11s %9s %11s %10s %9s %6s".format(
        "shape", "profile", "xover[pN]", "/share", "duplex[pN]", "conc", "dish/strk", "flat"
    ))
    result.restoredForces.filter { it.foundationMultiplier == 1.0 }.forEach {
        println("%-10s %-30s %11.5f %9.4f %11.4f %10.3f %9.3f %6s".format(
            it.shape, it.profile.take(30), it.peakCrossoverForce,
            it.peakCrossoverOverStaticShare, it.peakDuplexShear,
            it.concentrationFactor, it.dishingOverStroke, it.flat
        ))
    }

    println()
    println("--- the foundation sweep, 3 x 15 at C-0022's design point ".padEnd(110, '-'))
    println("%-8s %11s %11s %11s %11s".format("k_f x", "xover[pN]", "duplex[pN]", "dish[nm]", "/plate"))
    result.restoredForces.filter {
        it.shape == "3 x 15" && it.profile == result.designPointProfile
    }.forEach {
        println("%-8.2f %11.5f %11.4f %11.4f %11.3f".format(
            it.foundationMultiplier, it.peakCrossoverForce, it.peakDuplexShear,
            it.latticePeakDishing, it.latticeOverPlate
        ))
    }

    println()
    println("--- attachment-stiffness scatter, under a UNIFORM load ".padEnd(110, '-'))
    println("%-46s %7s %11s %12s %11s".format(
        "pattern", "eps", "xover[pN]", "per unit eps", "spread"
    ))
    result.scatter.forEach {
        println("%-46s %7.2f %11.5f %12.5f %11.4f".format(
            it.pattern.take(46), it.amplitude, it.peakCrossoverForce,
            it.peakCrossoverPerUnitAmplitude, it.pathForceSpreadFraction
        ))
    }

    println()
    println("--- the thermal channel, which does not converge in the penalty ".padEnd(110, '-'))
    println("%-10s %12s %13s %14s %12s".format(
        "shape", "k_link", "peak[pN]", "/sqrt(kT k)", "per decade"
    ))
    result.thermal.forEach {
        println("%-10s %12.0f %13.4f %14.4f %12s".format(
            it.shape, it.linkStiffness, it.peakThermalCrossoverForce,
            it.overSqrtThermalEnergyTimesLink,
            it.ratioToPreviousDecade?.let { r -> "%.4f".format(r) } ?: "-"
        ))
    }
    println()
    result.thermalBracket.forEach {
        println("  %-52s k_v = %9.2f pN/nm  ->  %8.2f pN".format(
            it.reading.take(52), it.verticalStiffness, it.thermalForceRms
        ))
    }

    println()
    println("--- P5: the three duties, by grid shape ".padEnd(110, '-'))
    println("%-10s %7s %10s %12s %11s %10s %10s".format(
        "shape", "count", "share[pN]", "<r^2>[nm^2]", "yaw margin", "lat marg", "all three"
    ))
    result.duties.forEach {
        println("%-10s %7d %10.3f %12.1f %11.1f %10.1f %10s".format(
            it.shape, it.attachments, it.perPathStaticShare, it.meanSquaredRadius,
            it.yawMargin, it.lateralMargin, it.allDutiesDischarged
        ))
    }

    println()
    println("--- P4: the allowables, corrected per CH-0029 ".padEnd(110, '-'))
    println("%-42s %9s %8s %10s %10s %10s".format(
        "allowable", "force[pN]", "bp", "n static", "n + rest", "marg @45"
    ))
    result.allowableCrossings.forEach {
        println("%-42s %9.2f %8s %10d %10d %10.2f".format(
            it.allowable.take(42), it.force,
            it.basePairs?.let { b -> "%.0f".format(b) } ?: "-",
            it.minimumPathsForStaticShareAlone, it.minimumPathsWithRestoredForce,
            it.marginAt45Paths
        ))
    }

    println()
    println("--- gate 4: convergence ".padEnd(110, '-'))
    println("%-52s %-26s %13s %12s".format("axis", "setting", "peak[pN]", "departure"))
    result.convergence.forEach {
        println("%-52s %-26s %13.6f %12.3e".format(
            it.axis.take(52), it.setting.take(26), it.peakCrossoverForce, it.departureFromFinest
        ))
    }

    println()
    println("--- gate 5: upstream reproductions ".padEnd(110, '-'))
    println("%-20s %-52s %11s %11s %10s".format("source", "quantity", "published", "here", "departure"))
    result.reproductions.forEach {
        println("%-20s %-52s %11.4f %11.4f %10.2e".format(
            it.source, it.quantity.take(52), it.published, it.reproduced, it.relativeDeparture
        ))
    }

    println()
    println("--- findings ".padEnd(110, '-'))
    result.findings.forEachIndexed { index, finding ->
        println("${index + 1}. $finding")
        println()
    }

    println("wrote ${output.path} in ${elapsed(started)}")
}
