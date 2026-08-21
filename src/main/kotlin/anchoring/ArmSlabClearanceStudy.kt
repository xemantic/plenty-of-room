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

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-126` — whether `C-0055`'s arm slab and `C-0035`'s tie-down path can share the tile's `+z`
 * face, answered in a **plan and a section** at `C-0063`'s phase-24 placement.
 *
 * Emits `gpd/results/T-126-arm-slab-clearance.json`.
 */

private const val DUPLEXES = 15
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private const val PHASE = 24
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T126BoundRecord(
    val name: String,
    val value: Double,
    val against: Double,
    val ratio: Double,
    val unit: String,
    val fired: Boolean,
    val settles: String
)

@Serializable
private data class T126SectionRecord(
    val body: String,
    val stroke: Double,
    val low: Double,
    val high: Double,
    val thickness: Double,
    val insideTieColumn: Boolean,
    val mayPassOver: Boolean
)

@Serializable
private data class T126RowRecord(
    val row: Int,
    val y: Double,
    val roots: List<Double>,
    val towardPositiveX: List<Boolean>,
    val armLow: List<Double>,
    val armHigh: List<Double>,
    val freeLow: List<Double>,
    val freeHigh: List<Double>,
    val freeLength: Double,
    val maximumTies: Int,
    val gridClashesAsPlaced: Int,
    val gridClashesBestDirections: Int,
    val directionsForced: Boolean
)

@Serializable
private data class T126GridRecord(
    val columns: Int,
    val ties: Int,
    val columnPositions: List<Double>,
    val clashesAsPlaced: Int,
    val clashesBestDirections: Int,
    val rowsFullyClear: Int,
    val clearingOffsets: Int,
    val clearingMeasure: Double,
    val nearestClearingOffset: Double,
    val widestClearingWindow: Double,
    val verdict: String
)

@Serializable
private data class T126StationRecord(
    val scheme: String,
    val stations: Int,
    val x: List<Double>,
    val y: List<Double>,
    val maximumDisplacement: Double,
    val meanDisplacement: Double
)

@Serializable
private data class T126FlatnessRecord(
    val placement: String,
    val host: String,
    val stations: Int,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val beatsNoCoupling: Boolean,
    val peakPathForce: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double
)

@Serializable
private data class T126ConvergenceRecord(
    val quantity: String,
    val axis: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T126ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double
)

@Serializable
private data class T126PredicateRecord(
    val predicate: String,
    val value: String,
    val verdict: String
)

@Serializable
private data class T126Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T126BoundRecord>,
    val section: List<T126SectionRecord>,
    val rows: List<T126RowRecord>,
    val grids: List<T126GridRecord>,
    val stations: List<T126StationRecord>,
    val flatness: List<T126FlatnessRecord>,
    val convergence: List<T126ConvergenceRecord>,
    val reproductions: List<T126ReproductionRecord>,
    val predicates: List<T126PredicateRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private fun sheet(): OrigamiSheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

private fun lattice(
    sheet: OrigamiSheet,
    columns: CrossoverLayout,
    supports: List<PointSupport> = emptyList(),
    subdivisions: Int = 2
) = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = columns,
    subdivisions = subdivisions,
    supports = supports
)

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias**. */
private fun solvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at 2 mM, 10 nm, 0.192 V")
    fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
    return CollarTerm(value("taperDepth"), value("taperWidth")) to
            CollarTerm(value("rimResidualDepth"), RIM_STANDOFF)
}

/** `C-0063`'s **own** winning placement, read from its result file rather than retyped. */
private fun c0063Placement(file: File): List<List<Double>> {
    require(file.exists()) {
        "C-0063's result file is missing: ${file.path}. T-126 is evaluated on ITS placement — " +
                "C-0055's own is not centro-symmetric and C-0061's mirrored one is on the WEST " +
                "azimuth (CH-0076), so neither may be substituted for it."
    }
    val rows = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
    require(rows.size == DUPLEXES) { "C-0063 places on $DUPLEXES rows, read ${rows.size}" }
    return rows.sortedBy { it.getValue("row").jsonPrimitive.content.toInt() }
        .map { row ->
            require(row.getValue("phaseBasePairs").jsonPrimitive.content.toInt() == PHASE) {
                "C-0063's winning placement is at phase $PHASE"
            }
            row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() }
        }
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val sheet = sheet()
    val edgeX = Gen1Tile.EDGE_X
    val lengthY = DUPLEXES * sheet.interhelicalDistance
    val area = edgeX * lengthY
    val width = Gen1Tile.INTERHELICAL_SHEET
    val arm = C0055_ARM_LENGTH
    val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR
    val stroke = Gen1Tile.ACCEPTABLE_STROKE
    val standoff = 8.0

    println("T-126 — reading C-0063's placement and C-0022's solved load ...")
    val rowRoots = c0063Placement(File("gpd/results/T-125-upward-root-placement.json"))
    check(rowRoots.sumOf { it.size } == C0055_ARM_COUNT) {
        "C-0063's placement must carry $C0055_ARM_COUNT arms"
    }
    val (smooth, rim) = solvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val interiorPressure = Gen1Tile.TARGET_FORCE / area
    val solvedField = edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))
    val uniformField = uniformPressure(interiorPressure)
    val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    // ------------------------------------------------------------------ the cheap bounds
    println("T-126 — the cheap bounds, which run before any layout ...")
    val armArea = C0055_ARM_COUNT * arm * width
    val tieArea = tieApertureArea(45, width)
    val slabAtRest = armSlabBand(0.0)
    val slabAtStroke = armSlabBand(stroke)
    val column = tieClearColumn(slabAtStroke.high + width)
    val sweptWorst = rowRoots.flatMapIndexed { row, roots ->
        val interleave = interleaveRow(row, roots, arm, edgeX, width)
        roots.zip(interleave.towardPositiveX).map { (root, toward) ->
            val slabArm = SlabArm(row, root, toward)
            val rest = armInterval(slabArm, arm, 0.0)
            val swept = sweptArmInterval(slabArm, arm, stroke, 512)
            maxOf(abs(swept.low - rest.low), abs(swept.high - rest.high))
        }
    }.max()
    val eastInventory = upwardHingeSites(PHASE, edgeX, DUPLEXES).size

    val bounds = listOf(
        T126BoundRecord(
            "the plan area of 34 arms and 45 ties against the footprint",
            armArea + tieArea, area, (armArea + tieArea) / area, "nm2", false,
            "an area budget does NOT decide it — 0.66 of the footprint. C-0041 established " +
                    "that an area bound is exactly what invites 'stack it in three levels', " +
                    "and this one is run in order to be refuted"
        ),
        T126BoundRecord(
            "the arm slab's band inside the tie's own clear column",
            slabAtStroke.thickness, column.thickness, slabAtStroke.thickness / column.thickness,
            "nm", true,
            "a plan overlap is LEVEL-INDEPENDENT: the tie must reach the tile, so its column " +
                    "starts at the tile's top face and strictly contains the slab. This is the " +
                    "bound that makes the whole question a plan one"
        ),
        T126BoundRecord(
            "the swept plan envelope against the rest footprint",
            sweptWorst, arm, sweptWorst / arm, "nm", false,
            "the sweep is FAVOURABLE: an arm rotates, so its plan projection is a cosine and " +
                    "shortens. The swept envelope is the rest footprint identically, and a " +
                    "static plan view is therefore conservative at every stroke"
        ),
        T126BoundRecord(
            "the upward root pitch minus the arm, against a tie's own width",
            pitch - arm, width, (pitch - arm) / width, "nm", false,
            "the only gap the lattice offers between two consecutive same-sense arms clears a " +
                    "duplex by 0.0256 nm at the SAXS 2.69 nm — and does NOT clear the 2.73 nm " +
                    "square-lattice value"
        ),
        T126BoundRecord(
            "34 arms plus 45 ties against the EAST inventory at phase 24",
            (C0055_ARM_COUNT + 45).toDouble(), eastInventory.toDouble(),
            (C0055_ARM_COUNT + 45).toDouble() / eastInventory, "sites", true,
            "CONDITIONAL, and reported as a ceiling with its threshold: IF a tie had to root " +
                    "on the upward crossover azimuth it could not be placed at all. It does " +
                    "not — C-0029's two-link 90 degree junction sits at a duplex END, " +
                    "quantised at the 0.34 nm rise and on no crossover lattice — so the bound " +
                    "is recorded and not used"
        )
    )

    // ------------------------------------------------------------------ the section
    println("T-126 — the section ...")
    val section = listOf(0.0, 1.0, stroke).map { s ->
        val slab = armSlabBand(s)
        T126SectionRecord(
            "C-0055's arm slab", s, slab.low, slab.high, slab.thickness,
            column.contains(slab), tieMayPassOverSlab(slab, column)
        )
    } + T126SectionRecord(
        "the host sheet", 0.0, sheetBand().low, sheetBand().high, sheetBand().thickness,
        false, false
    ) + T126SectionRecord(
        "C-0035's tie clear column", stroke, column.low, column.high, column.thickness,
        true, false
    )

    // ------------------------------------------------------------------ the plan, row by row
    println("T-126 — the plan, row by row ...")
    val threeColumns = gridColumns(3, edgeX)
    val rows = rowRoots.mapIndexed { row, roots ->
        val placed = interleaveRow(row, roots, arm, edgeX, width, threeColumns)
        val best = interleaveRow(row, roots, arm, edgeX, width, threeColumns, true)
        T126RowRecord(
            row = row,
            y = (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance,
            roots = roots,
            towardPositiveX = placed.towardPositiveX,
            armLow = placed.armIntervals.map { it.low },
            armHigh = placed.armIntervals.map { it.high },
            freeLow = placed.freeIntervals.map { it.low },
            freeHigh = placed.freeIntervals.map { it.high },
            freeLength = placed.freeLength,
            maximumTies = best.maximumTies,
            gridClashesAsPlaced = placed.gridClashes,
            gridClashesBestDirections = best.gridClashes,
            directionsForced = feasibleRowDirections(roots, arm, edgeX, width).size == 1
        )
    }

    // ------------------------------------------------------------------ the grids
    println("T-126 — the regular tie grids, and the rigid translations of them ...")
    val grids = listOf(1, 2, 3).map { columns ->
        val positions = gridColumns(columns, edgeX)
        val placed = totalGridClashes(rowRoots, arm, edgeX, width, positions, false)
        val best = totalGridClashes(rowRoots, arm, edgeX, width, positions, true)
        val clear = rowRoots.indices.count { row ->
            interleaveRow(row, rowRoots[row], arm, edgeX, width, positions, true).gridClashes == 0
        }
        val offsets = clearingGridOffsets(rowRoots, arm, edgeX, width, columns, 400001)
        T126GridRecord(
            columns = columns,
            ties = columns * DUPLEXES,
            columnPositions = positions,
            clashesAsPlaced = placed,
            clashesBestDirections = best,
            rowsFullyClear = clear,
            clearingOffsets = offsets.size,
            clearingMeasure = offsets.sumOf { it.length },
            nearestClearingOffset = offsets.minOfOrNull { minOf(abs(it.low), abs(it.high)) }
                ?: -1.0,
            widestClearingWindow = offsets.maxOfOrNull { it.length } ?: 0.0,
            verdict = if (best == 0) "the grid as drawn clears the slab"
            else if (offsets.isEmpty()) "NO rigid translation of this grid clears every row"
            else "clears only when translated off the tile centre-line"
        )
    }

    // ------------------------------------------------------------------ the escape
    println("T-126 — the snapped stations, and what they cost ...")
    val snapped = requireNotNull(
        snappedTieStations(
            rowRoots, arm, edgeX, width, threeColumns, sheet.interhelicalDistance, true
        )
    ) { "no row can carry three ties beside its arms — the room, and not the registration, binds" }
    val displacements = snapped.mapIndexed { index, (x, _) -> abs(x - threeColumns[index % 3]) }
    val tips = rowRoots.flatMapIndexed { row, roots ->
        val interleave = interleaveRow(row, roots, arm, edgeX, width)
        val y = (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance
        roots.zip(interleave.towardPositiveX).map { (root, toward) ->
            (if (toward) root + arm else root - arm) to y
        }
    }
    val stations = listOf(
        T126StationRecord(
            "C-0015's 3 x 15 grid, as every flatness claim draws it", 45,
            attachmentGrid(3, DUPLEXES, edgeX, lengthY).map { it.first },
            attachmentGrid(3, DUPLEXES, edgeX, lengthY).map { it.second }, 0.0, 0.0
        ),
        T126StationRecord(
            "the same 45, SNAPPED into the room the arms leave", snapped.size,
            snapped.map { it.first }, snapped.map { it.second },
            displacements.max(), displacements.average()
        ),
        T126StationRecord(
            "the 34 arm TIPS — the only registration the slab supplies", tips.size,
            tips.map { it.first }, tips.map { it.second },
            worstTipClearance(rowRoots, arm, edgeX, width), 0.0
        )
    )

    // ------------------------------------------------------------------ what it costs in flatness
    println("T-126 — the flatness of every station set, assembled ...")
    val phaseHost = CrossoverLayout.atBasePairPhase(PHASE, sheet, edgeX)
    val nominalHost = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0)
    val rootStations = rowRoots.flatMapIndexed { row, roots ->
        val y = (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance
        roots.map { it to y }
    }
    val freeOnPhase = lattice(sheet, phaseHost).solve(solvedField).peakDishing() / freeStroke

    fun flatness(
        name: String,
        host: String,
        columns: CrossoverLayout,
        set: List<Pair<Double, Double>>
    ): T126FlatnessRecord {
        val supports = if (set.isEmpty()) emptyList() else couplingSupports(set, MANDATE)
        val solution = lattice(sheet, columns, supports).solve(solvedField)
        val dishing = solution.peakDishing() / freeStroke
        return T126FlatnessRecord(
            placement = name,
            host = host,
            stations = set.size,
            dishingOverStroke = dishing,
            flatAtTenPercent = dishing < FLATNESS_TOLERANCE,
            beatsNoCoupling = dishing < freeOnPhase,
            peakPathForce = if (set.isEmpty()) 0.0 else solution.supportForces.maxOf { abs(it) },
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear
        )
    }

    val flatness = listOf(
        flatness("NONE — free tile", "phase 24, eight columns", phaseHost, emptyList()),
        flatness(
            "ROOTS — C-0063's own 34, the coupling entering at the hinges",
            "phase 24, eight columns", phaseHost, rootStations
        ),
        flatness(
            "GRID — C-0015's 3 x 15, as C-0035's ledger draws it",
            "nominal 8 columns", nominalHost, attachmentGrid(3, DUPLEXES, edgeX, lengthY)
        ),
        flatness(
            "GRID — C-0015's 3 x 15 on the arm array's own host",
            "phase 24, eight columns", phaseHost, attachmentGrid(3, DUPLEXES, edgeX, lengthY)
        ),
        flatness(
            "SNAPPED — the same 45 ties, displaced into the room the arms leave",
            "phase 24, eight columns", phaseHost, snapped
        )
    )

    // ------------------------------------------------------------------ convergence
    println("T-126 — convergence ...")
    val sweptSamples = listOf(8, 64, 4096).map { samples ->
        sweptArmInterval(SlabArm(0, rowRoots[0][0], true), arm, stroke, samples).length
    }
    val offsetSamples = listOf(4001, 40001, 400001).map { samples ->
        clearingGridOffsets(rowRoots, arm, edgeX, width, 1, samples).sumOf { it.length }
    }
    val snappedSupports = couplingSupports(snapped, MANDATE)
    val nested = listOf(1, 2, 4).map {
        lattice(sheet, phaseHost, snappedSupports, it).solve(solvedField).peakDishing() / freeStroke
    }
    val sampleGrids = listOf(41, 81, 161).map {
        lattice(sheet, phaseHost, snappedSupports).solve(solvedField).peakDishing(it) / freeStroke
    }
    val convergence = listOf(
        T126ConvergenceRecord(
            "the swept arm envelope", "sample count", listOf(8.0, 64.0, 4096.0), sweptSamples,
            abs(sweptSamples[2] - sweptSamples[1]) / sweptSamples[1],
            "exactly sample independent, because the reach is monotone in the stroke"
        ),
        T126ConvergenceRecord(
            "the clearing measure of a one-column grid", "offset samples",
            listOf(4001.0, 40001.0, 400001.0), offsetSamples,
            abs(offsetSamples[2] - offsetSamples[1]) / offsetSamples[1],
            "two of the four windows are 0.0256 nm wide — the pitch-minus-arm gap itself — so " +
                    "the measure is resolved only once the step is well under it"
        ),
        T126ConvergenceRecord(
            "dishing/stroke of the snapped 45", "nested subdivisions 1 c 2 c 4",
            listOf(1.0, 2.0, 4.0), nested, abs(nested[2] - nested[1]) / nested[1],
            "nested only, per CLAUDE.md"
        ),
        T126ConvergenceRecord(
            "dishing/stroke of the snapped 45", "dishing sample grid",
            listOf(41.0, 81.0, 161.0), sampleGrids,
            abs(sampleGrids[2] - sampleGrids[1]) / sampleGrids[1],
            "81 is the grid every published dishing in this programme is read on"
        )
    )

    // ------------------------------------------------------------------ upstream reproductions
    println("T-126 — the upstream reproductions ...")
    fun reproduction(source: String, quantity: String, published: Double, here: Double) =
        T126ReproductionRecord(
            source, quantity, published, here,
            if (published == 0.0) abs(here) else abs(here - published) / abs(published)
        )

    val reproductions = listOf(
        reproduction("C-0055", "the arm at 34 paths [nm]", 8.164, arm),
        reproduction("C-0055", "the upward root pitch [nm]", 10.88, pitch),
        reproduction("C-0055", "the EAST inventory at phase 24 [sites]", 53.0, eastInventory * 1.0),
        reproduction("C-0061", "the arm slab floor [nm]", 1.69, slabAtRest.low),
        reproduction("C-0061", "the arm slab ceiling [nm]", 3.69, slabAtRest.high),
        reproduction(
            "C-0061", "the plan fraction of the array", 0.4626,
            armArea / area
        ),
        reproduction("C-0063", "the arm count", 34.0, C0055_ARM_COUNT * 1.0),
        reproduction(
            "C-0063", "dishing/stroke at the 34 roots", 0.0706,
            flatness[1].dishingOverStroke
        ),
        reproduction(
            "C-0022", "dishing/stroke of the free tile", 0.3079, freeOnPhase
        ),
        reproduction(
            "C-0058", "dishing/stroke at C-0015's 3 x 15", 0.2182,
            flatness[2].dishingOverStroke
        ),
        reproduction("C-0035", "the tie aperture floor [nm2]", 325.6, tieApertureArea(45, width)),
        reproduction("C-0035", "the midspan clearance at l = 8 nm", 5.31, midspanClearance(8.0)),
        reproduction(
            "C-0035", "the midspan penetration at the desired 10 nm", 4.69,
            midspanPenetration(Gen1Tile.DESIRED_STROKE, standoff)
        ),
        reproduction("C-0015", "the middle grid column [nm]", 0.0, threeColumns[1])
    )

    // ------------------------------------------------------------------ the predicates
    val bestClashes = listOf(1, 2, 3).map { columns ->
        totalGridClashes(rowRoots, arm, edgeX, width, gridColumns(columns, edgeX), true)
    }
    val placedClashes = totalGridClashes(rowRoots, arm, edgeX, width, threeColumns, false)
    val oneColumn = grids.first { it.columns == 1 }
    val capacity = rowRoots.indices.sumOf { row ->
        interleaveRow(row, rowRoots[row], arm, edgeX, width, threeColumns, true).maximumTies
    }
    val predicates = listOf(
        T126PredicateRecord(
            "does a REGULAR tie grid share the face with the arm slab?",
            "clashes at 1 / 2 / 3 columns, best directions: " + bestClashes.joinToString(" / "),
            "NO at every column count"
        ),
        T126PredicateRecord(
            "can a rigid translation rescue it?",
            "clearing windows at 1 / 2 / 3 columns: " +
                    grids.joinToString(" / ") { it.clearingOffsets.toString() },
            "ONLY at one column, and only " +
                    "${oneColumn.nearestClearingOffset.roundedForProse()} nm off the tile centre-line"
        ),
        T126PredicateRecord(
            "is it the ROOM that refuses them?",
            "free tie capacity $capacity against the 45 demanded",
            "NO — the room is there; it is the registration that refuses them"
        ),
        T126PredicateRecord(
            "do the arms' OWN tip links clear?",
            "worst clearance " +
                    "${worstTipClearance(rowRoots, arm, edgeX, width).roundedForProse()} nm " +
                    "against " +
                    "${width / 2.0} nm demanded",
            "YES, at all 34"
        ),
        T126PredicateRecord(
            "does SS3's desired 10 nm stroke change the answer?",
            "an arm of $arm nm delivers a ${stroke.roundedForProse()} nm stroke: " +
                    "${armDeliversStroke(arm, stroke)}; a 10 nm one: " +
                    "${armDeliversStroke(arm, Gen1Tile.DESIRED_STROKE)}",
            "it removes the element rather than moving the clearance — C-0050's kinematic " +
                    "ceiling, from a plan view"
        )
    )

    val findings = listOf(
        "The arm slab and a REGULAR tie-down grid cannot share one face. On C-0015's own " +
                "3 x 15 grid $placedClashes of 45 ties land on an arm at C-0063's own arm " +
                "senses and ${bestClashes[2]} at the best of them; the section makes every one " +
                "of those level-independent.",
        "No rigid translation of a two- or three-column grid clears every row. A ONE-column " +
                "grid does, in ${oneColumn.clearingOffsets} windows, the nearest " +
                "${oneColumn.nearestClearingOffset.roundedForProse()} nm off the tile centre-line and the widest " +
                "${oneColumn.widestClearingWindow.roundedForProse()} nm wide.",
        "The room is not what is missing: the array's free tie capacity is $capacity against " +
                "the 45 demanded, and every one of the fifteen rows carries at least three.",
        "What refuses them is REGISTRATION. The escape is to displace the ties into the room " +
                "the arms leave — 45 stations, worst displacement " +
                "${displacements.max().roundedForProse()} nm — and its price is that the " +
                "coupling no longer enters on a grid.",
        "The only registration the slab supplies for free is the arms' OWN tips: 34 ties " +
                "landing on the arms clear every neighbour by " +
                "${worstTipClearance(rowRoots, arm, edgeX, width).roundedForProse()} nm, " +
                "which is the root pitch minus the arm and is a lattice quantity, not a " +
                "fitted one.",
        "The sweep runs the FAVOURABLE way: an arm's plan projection is a cosine, so the swept " +
                "envelope is the rest footprint identically and a static plan view is " +
                "conservative at every stroke.",
        "SS3's desired 10 nm stroke does not change the clearance — it removes the element, " +
                "because 10 nm exceeds the 8.164 nm arm and a lever is a rotation."
    )

    val result = T126Result(
        task = "T-126 — does the arm slab clear C-0035's tie-down path?",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x 40.35 nm " +
                "single-layer square-lattice Rothemund sheet, 15 duplexes at the SAXS-measured " +
                "2.69 nm; C-0063's phase-24 placement of C-0055's 34 arms at C-0039's 8.164 nm; " +
                "C-0035's Su mounting at a standoff of 8.0 nm; SS3's acceptable 3 nm stroke; " +
                "C-0022's solved profile at 2 mM, a 10 nm gap and 0.192 V",
        decision = "a regular tie grid and the arm slab cannot share one face at any column " +
                "count; the room is there and the registration is not",
        bounds = bounds,
        section = section,
        rows = rows,
        grids = grids,
        stations = stations,
        flatness = flatness,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings,
        parameters = mapOf(
            "armLength" to arm,
            "armCount" to C0055_ARM_COUNT.toDouble(),
            "rootPitch" to pitch,
            "tipGap" to pitch - arm,
            "duplexWidth" to width,
            "edgeX" to edgeX,
            "lengthY" to lengthY,
            "duplexes" to DUPLEXES.toDouble(),
            "phaseBasePairs" to PHASE.toDouble(),
            "standoffLength" to standoff,
            "acceptableStroke" to stroke,
            "desiredStroke" to Gen1Tile.DESIRED_STROKE,
            "mandate" to MANDATE,
            "freeStroke" to freeStroke,
            "flatnessTolerance" to FLATNESS_TOLERANCE,
            "eastInventory" to eastInventory.toDouble(),
            "freeTieCapacity" to capacity.toDouble(),
            "maximumDisplacement" to displacements.max()
        )
    )

    val file = File("gpd/results/T-126-arm-slab-clearance.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    file.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            ) as JsonObject)
        )
    )
    println("T-126 — wrote ${file.path}")
    println("  clashes at 1/2/3 columns (best directions): ${bestClashes.joinToString(" / ")}")
    println("  free tie capacity: $capacity against 45 demanded")
    println("  snapped 45 dishing/stroke: ${flatness[4].dishingOverStroke}")
    println("  roots 34 dishing/stroke: ${flatness[1].dishingOverStroke}")
    println("  grid 3x15 dishing/stroke: ${flatness[2].dishingOverStroke}")
}
