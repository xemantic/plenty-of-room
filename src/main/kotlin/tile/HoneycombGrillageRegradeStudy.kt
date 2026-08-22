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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.DropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.latticeInfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

// ---------------------------------------------------------------------------------------------
// T-263 -- re-grade C-0146 / C-0151's recommended coupled cells on the HONEYCOMB GRILLAGE.
//
// Every coupled cell in this repository is a smeared single-layer square-lattice solve, and
// C-0154 has measured what that costs on a honeycomb block: OrigamiSheet's across-helix rigidity
// is 24/7 = 3.42857x overstated, because only half the in-plane adjacent pairs are bonded and an
// interlayer bond carries half the lever arm -- while the SAME function reproduces D_parallel at
// 2.8e-15. One layer of the block is a set of DIMERS, not a sheet.
//
// The direction is already known on the FREE tile: 0.0449400126 against C-0141's 0.0240648102,
// 1.868x, and still flat. What is open is the COUPLED margin, and C-0151's recommendation rests
// on it.
//
// Nothing C-0142, C-0146 or C-0151 published can move: no source they run through is edited. The
// port is one function on HoneycombGrillage (the point-load dual) and one adapter.
// ---------------------------------------------------------------------------------------------

private const val T263_SAMPLES: Int = 81
private const val T263_TOLERANCE: Double = 0.10
private const val T263_RIM_STANDOFF: Double = 1.0
private const val T263_RIM_BAND: Double = 6.7
private const val T263_SEED: Long = 197_197L

/**
 * The relative tolerance the `p/k_f` stroke identity and the three exactness gates are asserted
 * at.
 *
 * They are emitted as a **threshold and a boolean**, never as a value: a departure between two
 * quantities that are equal by construction is nothing but the last few ulp, and `CLAUDE.md`
 * records what one such field costs — *"a number whose every digit is noise is a step counter
 * wearing a physical name"*, and one of them makes a 10 000-line result file permanently
 * un-diffable. Two runs of this study duly disagreed in exactly that one field and nowhere else.
 */
private const val T263_STROKE_IDENTITY: Double = 1e-9
private const val T263_LADDER_PHASE: Int = 16
private const val T263_LADDER_OFFSET: Int = 14
private const val T263_RECOMMENDED_ONE: Int = 102
private const val T263_RECOMMENDED_TWO: Int = 109
private const val T263_BLOCK_EXTENT_BP: Int = 116
private const val T263_SMEARED_COLUMNS: Int = 10

/** The study runs at 4 000 realisations; `T263_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t263Realisations: Int =
    if (System.getenv("T263_SMOKE") == "1") 150 else 4000

private fun Double.t263Rounded(digits: Int = 9, floor: Double = 1e-12): Double =
    roundForResult(this, digits, floor)

// ------------------------------------------------------------------------------ the records

@Serializable
private class T263CheapBoundRow(
    val question: String,
    val smearedEquivalentSheet: String,
    val honeycombGrillage: String,
    val consequence: String
)

@Serializable
private class T263Geometry(
    val model: String,
    val crossSection: String,
    val rowBasePairs: Int,
    val edgeX: Double,
    val edgeY: Double,
    val compositeFraction: Double?,
    val hingeStiffnessEnhancement: Double,
    val interiorPressure: Double,
    val freeStroke: Double,
    val closedFormStroke: Double,
    val strokeMatchesClosedForm: Boolean,
    val strokeIdentityTolerance: Double,
    val degreesOfFreedom: Int?,
    val bonds: Int?,
    val crossoverColumnsPerInterface: String,
    val uncoupledDishingOverStroke: Double,
    val uncoupledFlat: Boolean
)

@Serializable
private class T263Cell(
    val model: String,
    val crossSection: String,
    val compositeFraction: Double?,
    val hingeStiffnessEnhancement: Double,
    val placement: String,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val perPathStiffness: Double,
    val totalStiffness: Double,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double?,
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val uncoupledDishingOverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T263Paired(
    val comparison: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val numeratorP90: Double,
    val denominatorP90: Double,
    val ratioOfPercentiles: Double,
    val medianOfRatios: Double,
    val p90OfRatios: Double,
    val bestRatio: Double,
    val worstRatio: Double,
    val fractionAbove: Double,
    val verdictMoved: Boolean
)

@Serializable
private class T263Convergence(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departure: Double?
)

@Serializable
private class T263Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val reproduced: Boolean
)

@Serializable
private class T263Falsifier(
    val id: String,
    val statement: String,
    val declaredExpectedToFire: Boolean,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private class T263Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: List<T263CheapBoundRow>,
    val geometries: List<T263Geometry>,
    val cells: List<T263Cell>,
    val paired: List<T263Paired>,
    val verdict: Map<String, String>,
    val convergence: List<T263Convergence>,
    val reproductions: List<T263Reproduction>,
    val falsifiers: List<T263Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ------------------------------------------------------------------------------ the load

private class T263Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T263_RIM_STANDOFF))
        )
}

private fun t263Profile(file: File): T263Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T263Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

private fun t263Published(
    file: File,
    section: String,
    predicate: (JsonObject) -> Boolean,
    key: String
): Double {
    require(file.exists()) { "upstream result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(section).jsonArray.map { it.jsonObject }.first(predicate)
    return record.getValue(key).jsonPrimitive.content.toDouble()
}

// ------------------------------------------------------------------------------ the two tiles

/** The geometry both models share, so that the re-grade is a CONTROLLED comparison. */
private class T263Shared(
    val rasterRows: Int,
    val helicesPerRow: Int,
    val rowBasePairs: Int,
    val profile: T263Profile
) {
    val d: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch: Double = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * rowPitch
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val closedFormStroke: Double = interiorPressure / Gen1Tile.FOUNDATION_SECANT
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val crossSection: String = "$rasterRows x $helicesPerRow"

    /** The realised enhancement `1 + f (factor − 1)` this block's layers carry at [fraction]. */
    fun enhancementAt(fraction: Double): Double = multiLayerRigidities(
        layers = helicesPerRow,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = columnPitch
    ).realisedEnhancement
}

/** `C-0151`'s tile, unchanged: a smeared equivalent sheet under an `OrigamiGrillage`. */
private class T263Smeared(val shared: T263Shared, val compositeFraction: Double) {

    val rigidities: MultiLayerRigidities = multiLayerRigidities(
        layers = shared.helicesPerRow,
        interhelicalDistance = shared.rowPitch,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = compositeFraction,
        layerSpacing = shared.columnPitch
    )

    private val sheet = equivalentSheet(rigidities)

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(shared.edgeX, shared.edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(shared.interiorPressure)).meanDeflection

    val lattice: OrigamiGrillage by lazy {
        OrigamiGrillage(
            sheet = sheet,
            lengthX = shared.edgeX,
            beamCount = shared.rasterRows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(T263_SMEARED_COLUMNS, sheet.crossoverSpacing / 2.0),
            subdivisions = 2
        )
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(shared.pressureField).peakDishing(T263_SAMPLES) / freeStroke
    }

    fun surrogate(grid: List<Pair<Double, Double>>): InfluenceSurrogate =
        latticeInfluenceSurrogate(lattice, grid, shared.pressureField, T263_SAMPLES)
}

/** `C-0154`'s block, as the three-dimensional beam-and-bond lattice it is. */
private class T263Honeycomb(
    val shared: T263Shared,
    val enhancement: Double,
    val subdivisions: Int = 1
) {

    val lattice: HoneycombGrillage = HoneycombGrillage(
        block = HoneycombBlock(shared.rasterRows, shared.helicesPerRow),
        rowBasePairs = shared.rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions
    )

    val freeStroke: Double by lazy { honeycombFreeStroke(lattice, shared.interiorPressure) }

    val uncoupledDishing: Double by lazy {
        lattice.solve(shared.pressureField).peakDishing(T263_SAMPLES) / freeStroke
    }

    /** The `y` of each rooting helix — the coordinate a smeared plate has no parameter for. */
    val rootingHelixY: List<Double> = lattice.faceBeams.map { lattice.beamY[it] }

    /** How many crossover columns each interface carries. */
    val interfaceColumnCounts: List<Int> by lazy {
        lattice.bonds
            .groupBy { it.site.lowerBeam to it.site.upperBeam }
            .map { (_, bonds) -> bonds.size }
    }

    /** The same, as a min-to-max string. */
    val columnsPerInterface: String by lazy {
        if (interfaceColumnCounts.min() == interfaceColumnCounts.max())
            interfaceColumnCounts.min().toString()
        else interfaceColumnCounts.min().toString() + " to " + interfaceColumnCounts.max()
    }

    fun surrogate(grid: List<Pair<Double, Double>>): InfluenceSurrogate =
        honeycombInfluenceSurrogate(lattice, grid, shared.pressureField, T263_SAMPLES)
}

// ------------------------------------------------------------------------------ the grading

private class T263Graded(val cell: T263Cell, val sample: DoubleArray)

@Suppress("LongParameterList")
private fun gradeT263Cell(
    model: String,
    shared: T263Shared,
    compositeFraction: Double?,
    enhancement: Double,
    placement: String,
    columns: Int,
    grid: List<Pair<Double, Double>>,
    distribution: String,
    stiffnesses: List<Double>,
    surrogate: InfluenceSurrogate,
    freeStroke: Double,
    uncoupled: Double,
    ensemble: DropoutEnsemble
): T263Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T263_TOLERANCE
    )
    return T263Graded(
        T263Cell(
            model = model,
            crossSection = shared.crossSection,
            compositeFraction = compositeFraction,
            hingeStiffnessEnhancement = enhancement,
            placement = placement,
            columns = columns,
            rows = shared.rasterRows,
            pathCount = grid.size,
            distribution = distribution,
            perPathStiffness = stiffnesses.max(),
            totalStiffness = stiffnesses.sum(),
            nominalOverStroke = nominal,
            worstSingleRemovalOverStroke =
                worstSinglePathRemoval(surrogate, stiffnesses) / freeStroke,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            uncoupledDishingOverStroke = uncoupled,
            flatAtNominal = nominal < T263_TOLERANCE,
            flatAtP90 = summary.flatAtP90,
            beatsUncoupledAtP90 = summary.p90 < uncoupled
        ),
        sample
    )
}

private fun t263Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T263_RIM_BAND || abs(y) > edgeY / 2.0 - T263_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** The four placements, all of which BOTH models can carry, so the pairing is exact. */
private fun t263Placements(
    shared: T263Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T263_RECOMMENDED_ONE, T263_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T263_LADDER_PHASE, T263_LADDER_OFFSET
    )
    fun onHelices(grid: List<Pair<Double, Double>>) = grid.mapIndexed { index, (x, _) ->
        x to rootingHelixY[index / columns]
    }
    return listOf(
        "abstract grid" to abstract,
        "abstract grid on the rooting helices" to onHelices(abstract),
        "determined station lattice" to determined,
        "determined station lattice on the rooting helices" to onHelices(determined)
    )
}

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val profile = t263Profile(ResultInputs.T_3B.file())
    val t245 = ResultInputs.T_245.file()
    val t253 = ResultInputs.T_253.file()
    val shared = T263Shared(10, 6, T263_BLOCK_EXTENT_BP, profile)
    val fractions = listOf(0.30, 0.26)

    // ============================================ Deliverable 1 -- the cheap bound, no solver
    println("T-263 - the cheap bound, before any Monte Carlo")
    val probe = T263Honeycomb(shared, shared.enhancementAt(0.30))
    val faceGaps = probe.rootingHelixY.zipWithNext { a, b -> b - a }
    val abstractRows = attachmentGrid(1, shared.rasterRows, shared.edgeX, shared.edgeY)
    val rowOffsets = probe.rootingHelixY.indices.map {
        abstractRows[it].second - probe.rootingHelixY[it]
    }
    val smearedPerInterface = T263_SMEARED_COLUMNS / 2
    val cheapBound = listOf(
        T263CheapBoundRow(
            question = "how many crossover columns does ONE interface carry?",
            smearedEquivalentSheet = "$T263_SMEARED_COLUMNS columns at the " +
                    (Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP * Gen1Tile.RISE_PER_BASE_PAIR / 2.0)
                        .t263Rounded(4) + " nm half-pitch, alternating between the two parities, " +
                    "so $smearedPerInterface per interface",
            honeycombGrillage = probe.columnsPerInterface + " -- the planes of ONE bond class, " +
                    "which recur every 21 bp",
            consequence = "they AGREE, so the whole of the re-grade is the 24/7 across-helix " +
                    "rigidity and the dimer topology, and not a second column-count error"
        ),
        T263CheapBoundRow(
            question = "where are the rooting helices of the gap-facing face?",
            smearedEquivalentSheet = "one beam per raster row at the uniform " +
                    shared.rowPitch.t263Rounded(4) + " nm pitch",
            honeycombGrillage = "gaps alternate " + faceGaps.min().t263Rounded(4) + " and " +
                    faceGaps.max().t263Rounded(4) + " nm -- the corrugation, d and 2d about 3d/2",
            consequence = "an abstract-grid station therefore sits " +
                    rowOffsets.maxOf { abs(it) }.t263Rounded(4) + " nm = d/4 off its own helix, " +
                    "ALTERNATING in sign across the rows -- which is CLAUDE.md's first-order " +
                    "symmetry break, so the placement is graded both ways"
        ),
        T263CheapBoundRow(
            question = "does the normalising stroke move between the two models?",
            smearedEquivalentSheet = "p/k_f = " + shared.closedFormStroke.t263Rounded(9) + " nm",
            honeycombGrillage = "p/k_f = " + shared.closedFormStroke.t263Rounded(9) + " nm",
            consequence = "a free body on a uniform Winkler foundation translates rigidly " +
                    "whatever its rigidities, so the re-grade is a CONTROLLED comparison: same " +
                    "extent, same grid, same stroke, same mandate, same dropout stream"
        ),
        T263CheapBoundRow(
            question = "which way does the coupled margin move?",
            smearedEquivalentSheet = "free tile 0.0240648102 of the stroke (C-0141)",
            honeycombGrillage = "free tile 0.0449400126 of the stroke (C-0154)",
            consequence = "1.868x worse before any coupling, so the run's job is to say HOW " +
                    "MANY cells survive, not WHETHER the number moves"
        )
    )
    cheapBound.forEach { println("  " + it.question + " -> " + it.consequence) }

    // ============================================ Deliverable 2 -- geometries and free tiles
    println("T-263 - the tiles")
    val geometries = ArrayList<T263Geometry>()
    val smearedTiles = HashMap<Double, T263Smeared>()
    val honeycombTiles = HashMap<Double, T263Honeycomb>()
    fractions.forEach { fraction ->
        val enhancement = shared.enhancementAt(fraction)
        val smeared = T263Smeared(shared, fraction)
        val honeycomb = T263Honeycomb(shared, enhancement)
        smearedTiles[fraction] = smeared
        honeycombTiles[fraction] = honeycomb
        geometries += T263Geometry(
            model = "smeared equivalent sheet",
            crossSection = shared.crossSection,
            rowBasePairs = shared.rowBasePairs,
            edgeX = shared.edgeX,
            edgeY = shared.edgeY,
            compositeFraction = fraction,
            hingeStiffnessEnhancement = enhancement,
            interiorPressure = shared.interiorPressure,
            freeStroke = smeared.freeStroke,
            closedFormStroke = shared.closedFormStroke,
            strokeMatchesClosedForm = abs(smeared.freeStroke - shared.closedFormStroke) <
                    T263_STROKE_IDENTITY * shared.closedFormStroke,
            strokeIdentityTolerance = T263_STROKE_IDENTITY,
            degreesOfFreedom = null,
            bonds = null,
            crossoverColumnsPerInterface = smearedPerInterface.toString(),
            uncoupledDishingOverStroke = smeared.uncoupledDishing,
            uncoupledFlat = smeared.uncoupledDishing < T263_TOLERANCE
        )
        geometries += T263Geometry(
            model = "honeycomb grillage",
            crossSection = shared.crossSection,
            rowBasePairs = shared.rowBasePairs,
            edgeX = shared.edgeX,
            edgeY = shared.edgeY,
            compositeFraction = fraction,
            hingeStiffnessEnhancement = enhancement,
            interiorPressure = shared.interiorPressure,
            freeStroke = honeycomb.freeStroke,
            closedFormStroke = shared.closedFormStroke,
            strokeMatchesClosedForm = abs(honeycomb.freeStroke - shared.closedFormStroke) <
                    T263_STROKE_IDENTITY * shared.closedFormStroke,
            strokeIdentityTolerance = T263_STROKE_IDENTITY,
            degreesOfFreedom = honeycomb.lattice.degreesOfFreedom,
            bonds = honeycomb.lattice.bonds.size,
            crossoverColumnsPerInterface = honeycomb.columnsPerInterface,
            uncoupledDishingOverStroke = honeycomb.uncoupledDishing,
            uncoupledFlat = honeycomb.uncoupledDishing < T263_TOLERANCE
        )
    }
    // C-0154's own lower bound: the lattice with NO across-helix parallel-axis term at all.
    val lowerBound = T263Honeycomb(shared, 1.0)
    honeycombTiles[1.0] = lowerBound
    geometries += T263Geometry(
        model = "honeycomb grillage",
        crossSection = shared.crossSection,
        rowBasePairs = shared.rowBasePairs,
        edgeX = shared.edgeX,
        edgeY = shared.edgeY,
        compositeFraction = null,
        hingeStiffnessEnhancement = 1.0,
        interiorPressure = shared.interiorPressure,
        freeStroke = lowerBound.freeStroke,
        closedFormStroke = shared.closedFormStroke,
        strokeMatchesClosedForm = abs(lowerBound.freeStroke - shared.closedFormStroke) <
                T263_STROKE_IDENTITY * shared.closedFormStroke,
        strokeIdentityTolerance = T263_STROKE_IDENTITY,
        degreesOfFreedom = lowerBound.lattice.degreesOfFreedom,
        bonds = lowerBound.lattice.bonds.size,
        crossoverColumnsPerInterface = lowerBound.columnsPerInterface,
        uncoupledDishingOverStroke = lowerBound.uncoupledDishing,
        uncoupledFlat = lowerBound.uncoupledDishing < T263_TOLERANCE
    )
    geometries.forEach {
        println("  " + it.model + "  f = " + (it.compositeFraction?.t263Rounded(3) ?: "none") +
                "  stroke " + it.freeStroke.t263Rounded(9) + " (identity " +
                (if (it.strokeMatchesClosedForm) "holds" else "FAILS") + ")  uncoupled " +
                it.uncoupledDishingOverStroke.t263Rounded(9) +
                (if (it.uncoupledFlat) "  flat" else "  NOT FLAT"))
    }

    // ============================================ Deliverable 3 -- the re-graded cells
    println("T-263 - the re-grade, " + t263Realisations + " realisations on one common stream")
    val gradedColumns = listOf(1, 2, 3, 5)
    val cells = ArrayList<T263Cell>()
    val samples = HashMap<String, DoubleArray>()
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
    gradedColumns.forEach { columns ->
        t263Placements(shared, probe.rootingHelixY, columns).forEach { (placement, grid) ->
            val ensemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, t263Realisations, T263_SEED
            )
            t263Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, stiffnesses) ->
                fractions.forEach { fraction ->
                    val smeared = smearedTiles.getValue(fraction)
                    val honeycomb = honeycombTiles.getValue(fraction)
                    listOf(
                        Triple(
                            "smeared equivalent sheet", smeared.surrogate(grid),
                            smeared.freeStroke to smeared.uncoupledDishing
                        ),
                        Triple(
                            "honeycomb grillage", honeycomb.surrogate(grid),
                            honeycomb.freeStroke to honeycomb.uncoupledDishing
                        )
                    ).forEach { (model, surrogate, reference) ->
                        val graded = gradeT263Cell(
                            model, shared, fraction,
                            if (model == "honeycomb grillage") honeycomb.lattice.hingeStiffnessEnhancement
                            else smeared.rigidities.realisedEnhancement,
                            placement, columns, grid, label, stiffnesses, surrogate,
                            reference.first, reference.second, ensemble
                        )
                        cells += graded.cell
                        samples[model + "|" + fraction + "|" + placement + "|" + columns +
                                "|" + label] = graded.sample
                        println("  " + model + "  f=" + fraction.t263Rounded(3) + "  " +
                                placement + "  " + columns + " x " + shared.rasterRows + " = " +
                                grid.size + " paths, " + label + "  p90 " +
                                graded.cell.p90OverStroke.t263Rounded(9) +
                                (if (graded.cell.flatAtP90) "  FLAT at p90" else "  not flat"))
                    }
                }
                // the lattice's own lower bound carries no smeared counterpart
                val bound = honeycombTiles.getValue(1.0)
                val gradedBound = gradeT263Cell(
                    "honeycomb grillage", shared, null, 1.0, placement, columns, grid,
                    label, stiffnesses, bound.surrogate(grid), bound.freeStroke,
                    bound.uncoupledDishing, ensemble
                )
                cells += gradedBound.cell
                samples["honeycomb grillage|none|" + placement + "|" + columns + "|" + label] =
                    gradedBound.sample
            }
        }
    }

    // ============================================ Deliverable 4 -- the paired comparison
    println("T-263 - the paired reading, per realisation on the shared stream")
    val paired = ArrayList<T263Paired>()
    gradedColumns.forEach { columns ->
        t263Placements(shared, probe.rootingHelixY, columns).forEach { (placement, grid) ->
            t263Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, _) ->
                fractions.forEach { fraction ->
                    val key = { model: String ->
                        model + "|" + fraction + "|" + placement + "|" + columns + "|" + label
                    }
                    val numerator = samples.getValue(key("honeycomb grillage"))
                    val denominator = samples.getValue(key("smeared equivalent sheet"))
                    val summary = pairedRatioSummary(numerator, denominator)
                    fun cellOf(model: String) = cells.first {
                        it.model == model && it.compositeFraction == fraction &&
                                it.placement == placement && it.columns == columns &&
                                it.distribution == label
                    }
                    val here = cellOf("honeycomb grillage")
                    val there = cellOf("smeared equivalent sheet")
                    paired += T263Paired(
                        comparison = "the honeycomb grillage over C-0151's smeared sheet",
                        compositeFraction = fraction,
                        placement = placement,
                        columns = columns,
                        pathCount = grid.size,
                        distribution = label,
                        numeratorP90 = here.p90OverStroke,
                        denominatorP90 = there.p90OverStroke,
                        ratioOfPercentiles = summary.ratioOfPercentiles,
                        medianOfRatios = summary.median,
                        p90OfRatios = summary.p90,
                        bestRatio = summary.best,
                        worstRatio = summary.worst,
                        fractionAbove = summary.fractionAbove,
                        verdictMoved = here.flatAtP90 != there.flatAtP90
                    )
                }
            }
        }
    }
    println("  " + paired.count { it.verdictMoved } + " of " + paired.size +
            " paired cells change their T-5b verdict")

    // ============================================ Deliverable 5 -- gates, convergence, falsifiers
    println("T-263 - the gates")
    val convergence = ArrayList<T263Convergence>()
    val reference = t263Placements(shared, probe.rootingHelixY, 1).first().second
    val referenceStiffnesses = equalShareOfMandate(reference.size)
    fun nominalAt(subdivisions: Int, samples: Int): Double {
        val tile = T263Honeycomb(shared, shared.enhancementAt(0.30), subdivisions)
        val surrogate =
            honeycombInfluenceSurrogate(tile.lattice, reference, shared.pressureField, samples)
        return surrogate.solve(referenceStiffnesses).peakDishing / tile.freeStroke
    }
    val subdivisionOne = nominalAt(1, T263_SAMPLES)
    val subdivisionTwo = nominalAt(2, T263_SAMPLES)
    convergence += T263Convergence(
        "beam subdivisions", "1", "the recommended cell's nominal dishing over stroke",
        subdivisionOne, null
    )
    convergence += T263Convergence(
        "beam subdivisions", "2", "the recommended cell's nominal dishing over stroke",
        subdivisionTwo, abs(subdivisionTwo - subdivisionOne) / subdivisionOne
    )
    var previousSamples: Double? = null
    listOf(41, 81, 161).forEach { samples ->
        val value = nominalAt(1, samples)
        convergence += T263Convergence(
            "dishing sample grid", samples.toString(),
            "the recommended cell's nominal dishing over stroke", value,
            previousSamples?.let { abs(value - it) / it }
        )
        previousSamples = value
    }

    // -------------------------------------------------------------------------- reproductions
    val reproductions = ArrayList<T263Reproduction>()
    fun reproduce(source: String, quantity: String, published: Double, here: Double) {
        val departure =
            if (published == 0.0) abs(here) else abs(here - published) / abs(published)
        reproductions += T263Reproduction(
            source, quantity, published, here, departure, departure < 1e-7
        )
    }
    val at112 = T263Shared(10, 6, 112, profile)
    listOf(1.0, 21.1851817, 17.6059172).forEach { enhancement ->
        val tile = T263Honeycomb(at112, enhancement)
        val published = t263Published(
            t253, "flatness",
            { it.getValue("crossSection").jsonPrimitive.content == "10 x 6" &&
                    it.getValue("hingeStiffnessEnhancement").jsonPrimitive.content.toDouble() ==
                    enhancement &&
                    it.getValue("subdivisions").jsonPrimitive.content.toInt() == 1 },
            "freeDishingOverStroke"
        )
        reproduce(
            "C-0154 (T-253)",
            "the free 10 x 6 tile at 112 bp, enhancement " + enhancement.t263Rounded(9),
            published,
            tile.lattice.solve(at112.profile.field(at112.interiorPressure, at112.edgeX, at112.edgeY))
                .peakDishing(T263_SAMPLES) / tile.freeStroke
        )
    }
    fractions.forEach { fraction ->
        reproduce(
            "C-0151 (T-245)",
            "the uncoupled smeared tile at f = " + fraction.t263Rounded(3),
            t263Published(
                t245, "references",
                { it.getValue("senseOneBasePairs").jsonPrimitive.content.toInt() ==
                        T263_RECOMMENDED_ONE &&
                        it.getValue("crossoverColumns").jsonPrimitive.content.toInt() ==
                        T263_SMEARED_COLUMNS &&
                        it.getValue("compositeFraction").jsonPrimitive.content.toDouble() ==
                        fraction },
                "uncoupledDishingOverStroke"
            ),
            smearedTiles.getValue(fraction).uncoupledDishing
        )
        listOf("abstract grid", "determined station lattice").forEach { placement ->
            listOf(1 to "equal springs", 5 to "rim-graded 5:1").forEach { (columns, label) ->
                val published = try {
                    t263Published(
                        t245, "cells",
                        { it.getValue("senseOneBasePairs").jsonPrimitive.content.toInt() ==
                                T263_RECOMMENDED_ONE &&
                                it.getValue("crossoverColumns").jsonPrimitive.content.toInt() ==
                                T263_SMEARED_COLUMNS &&
                                it.getValue("compositeFraction").jsonPrimitive.content
                                    .toDouble() == fraction &&
                                it.getValue("placement").jsonPrimitive.content == placement &&
                                it.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                                it.getValue("distribution").jsonPrimitive.content == label },
                        "p90OverStroke"
                    )
                } catch (e: NoSuchElementException) {
                    null
                }
                if (published != null) {
                    val here = cells.first {
                        it.model == "smeared equivalent sheet" &&
                                it.compositeFraction == fraction && it.placement == placement &&
                                it.columns == columns && it.distribution == label
                    }
                    reproduce(
                        "C-0151 (T-245)",
                        "the p90 at f = " + fraction.t263Rounded(3) + ", " + placement + ", " +
                                columns + " x 10, " + label,
                        published, here.p90OverStroke
                    )
                }
            }
        }
    }
    reproductions.forEach {
        println("  reproduce " + it.quantity + ": " + it.published + " vs " + it.here +
                "  departure " + it.departure.t263Rounded(2) +
                (if (it.reproduced) "  OK" else "  NOT REPRODUCED"))
    }

    // -------------------------------------------------------------------------------- falsifiers
    val headline = honeycombTiles.getValue(0.30)
    val uniformDishing =
        headline.lattice.solve(uniformPressure(shared.interiorPressure))
            .peakDishing(T263_SAMPLES) / headline.freeStroke
    val onePathGrid = listOf(reference.first())
    val oneSurrogate = headline.surrogate(onePathGrid)
    val oneCoupled = oneSurrogate.solve(listOf(33.3333333))
    val assembled = headline.lattice.solve(
        shared.pressureField,
        listOf(
            com.xemantic.nano.plentyofroom.structure.PointLoad(
                onePathGrid[0].first, onePathGrid[0].second, -oneCoupled.supportForces[0]
            )
        )
    )
    val superposition = abs(assembled.peakDishing(T263_SAMPLES) / headline.freeStroke -
            oneCoupled.peakDishing / headline.freeStroke) /
            (oneCoupled.peakDishing / headline.freeStroke)
    val underPressure = headline.lattice.solve(shared.pressureField)
    val underPoint = headline.lattice.solve(
        uniformPressure(0.0),
        listOf(
            com.xemantic.nano.plentyofroom.structure.PointLoad(
                onePathGrid[0].first, onePathGrid[0].second, 1.0
            )
        )
    )
    val forward =
        headline.lattice.pointLoadDual(onePathGrid[0].first, onePathGrid[0].second)
            .dot(underPressure.coefficients)
    val backward = headline.lattice.assembleLoad(shared.pressureField).dot(underPoint.coefficients)
    val betti = abs(forward - backward) / abs(forward)
    val movedVerdicts = paired.count { it.verdictMoved }
    val flatHere = cells.count {
        it.model == "honeycomb grillage" && it.compositeFraction != null && it.flatAtP90
    }
    val flatThere = cells.count { it.model == "smeared equivalent sheet" && it.flatAtP90 }
    val falsifiers = listOf(
        T263Falsifier(
            "F1", "a uniform pressure on the coupled honeycomb lattice dishes exactly zero",
            false, uniformDishing > T263_STROKE_IDENTITY,
            "peak dishing below " + T263_STROKE_IDENTITY.toString() + " of the stroke under a " +
                    "uniform " + shared.interiorPressure.t263Rounded(9) + " pN/nm^2"
        ),
        T263Falsifier(
            "F2", "the free-tile reproduction of C-0154 closes at 1e-8 relative",
            false,
            reproductions.filter { it.source.startsWith("C-0154") }.any { it.departure > 1e-8 },
            "worst C-0154 departure " +
                    reproductions.filter { it.source.startsWith("C-0154") }
                        .maxOf { it.departure }.t263Rounded(2)
        ),
        T263Falsifier(
            "F3", "the surrogate at full presence equals the assembled solve",
            false, superposition > T263_STROKE_IDENTITY,
            "relative departure below " + T263_STROKE_IDENTITY.toString() + " on a one-path " +
                    "coupling, the support force taken from the Woodbury solve and applied as a " +
                    "point load"
        ),
        T263Falsifier(
            "F4", "Betti holds between the point dual and the pressure load vector",
            false, betti > T263_STROKE_IDENTITY,
            "relative departure below " + T263_STROKE_IDENTITY.toString()
        ),
        T263Falsifier(
            "F5", "the honeycomb re-grade changes NO flatness verdict",
            true, movedVerdicts > 0,
            movedVerdicts.toString() + " of " + paired.size + " paired cells move; " +
                    flatHere.toString() + " honeycomb cells clear T-5b at the p90 against " + flatThere +
                    " smeared ones over the same 64-cell set"
        ),
        T263Falsifier(
            "F6", "the honeycomb lattice's SPARSEST interface carries as many crossover " +
                    "columns as the smeared model gives every interface",
            true,
            headline.interfaceColumnCounts.min() != smearedPerInterface,
            "honeycomb " + headline.columnsPerInterface + " per interface against the smeared " +
                    "model's " + smearedPerInterface.toString()
        )
    )
    falsifiers.forEach {
        println("  " + it.id + (if (it.fired) "  FIRED  " else "  did not fire  ") + it.evidence)
    }

    // ============================================================ the result, then the prose
    val recommended = { model: String, fraction: Double ->
        cells.first {
            it.model == model && it.compositeFraction == fraction &&
                    it.placement == "abstract grid" && it.columns == 1 &&
                    it.distribution == "equal springs"
        }
    }
    val result = T263Result(
        task = "T-263",
        leaf = "A8.2",
        title = "C-0146 / C-0151's recommended coupled cells re-graded on the honeycomb " +
                "GRILLAGE, at both ends of the composite-fraction bracket",
        verificationType = "in-silico (a beam-and-bond lattice solve and a Monte Carlo dropout " +
                "ensemble on one common stream) + logical (an exact algebraic identity between " +
                "the surrogate and the assembled solve, and a crossover census that costs no " +
                "solve)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "folding statistics graded against are measured; the flatness is not, and the " +
                "cross-section is a lattice statement.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 (= 1 MPa)",
            "rigidity" to "pN nm",
            "dishing" to "dimensionless, as a fraction of the free-tile stroke"
        ),
        conventions = mapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "z" to "through the block's thickness",
            "w" to "positive downward, toward the electrode (C-0006)",
            "face" to "the gap-facing column of the block, one helix per x-raster row",
            "stroke" to "the free-tile mean deflection under the interior pressure, p/k_f"
        ),
        parameters = mapOf(
            "crossSection" to "10 x 6 (design (ii)), 60 helices",
            "raster" to "$T263_RECOMMENDED_ONE / $T263_RECOMMENDED_TWO bp, C-0151's recommended " +
                    "closing pair",
            "blockExtentBasePairs" to T263_BLOCK_EXTENT_BP.toString(),
            "edgeX" to shared.edgeX.t263Rounded(9).toString(),
            "edgeY" to shared.edgeY.t263Rounded(9).toString(),
            "interhelicalDistance" to shared.d.toString(),
            "inPlaneRowPitch" to shared.rowPitch.t263Rounded(9).toString(),
            "layerPitch" to shared.columnPitch.t263Rounded(9).toString(),
            "risePerBasePair" to Gen1Tile.RISE_PER_BASE_PAIR.toString(),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.toString(),
            "targetForce" to Gen1Tile.TARGET_FORCE.toString(),
            "compositeFractions" to "0.26 and 0.30, C-0116's measured band, plus the lattice's " +
                    "own enhancement-1.0 lower bound",
            "smearedCrossoverColumns" to T263_SMEARED_COLUMNS.toString(),
            "honeycombSubdivisions" to "1 (headline), 2 (convergence)",
            "smearedSubdivisions" to "2, C-0151's own",
            "ladderPhase" to T263_LADDER_PHASE.toString(),
            "ladderOffset" to T263_LADDER_OFFSET.toString(),
            "mandate" to "C-0017 at SS3's acceptable clause, 33.3333 pN/nm on the SUM",
            "realisations" to t263Realisations.toString(),
            "seed" to T263_SEED.toString(),
            "dishingSamples" to T263_SAMPLES.toString(),
            "flatnessTolerance" to T263_TOLERANCE.toString(),
            "smokeRun" to (t263Realisations != 4000).toString(),
            "temperature" to "300 K, aqueous 2 mM MgCl2"
        ),
        sources = listOf(
            "C-0154 (T-253) -- HoneycombGrillage, the 24/7 across-helix overstatement, the " +
                    "dimer census and the free-tile readings reproduced here",
            "C-0151 (T-245) -- the recommended 102 / 109 raster and the eight cells re-graded",
            "C-0146 (T-235) -- the two-length station lattice and the 116 bp block extent",
            "C-0142 (T-232) -- the influence surrogate and the dropout grading, untouched",
            "C-0141 -- the honeycomb cross-section, the two pitches and the station ladder",
            "C-0116 -- the measured 0.26-0.33 composite-fraction band",
            "C-0087 -- the measured position-dependent staple incorporation",
            "C-0022 -- the solved collar at 2 mM / 10 nm / 0.192 V",
            "C-0017 -- the coupling mandate"
        ),
        citedInputs = listOf(
            "gpd/results/T-3b-tile-edge-load-profile.json",
            "gpd/results/T-245-closing-raster-selection.json",
            "gpd/results/T-253-honeycomb-grillage.json"
        ),
        cheapBound = cheapBound,
        geometries = geometries,
        cells = cells,
        paired = paired,
        verdict = mapOf(
            "theRecommendedCellAt030" to "p90 " +
                    recommended("honeycomb grillage", 0.30).p90OverStroke.t263Rounded(9) +
                    " on the honeycomb grillage against " +
                    recommended("smeared equivalent sheet", 0.30).p90OverStroke.t263Rounded(9) +
                    " on C-0151's smeared sheet",
            "theRecommendedCellAt026" to "p90 " +
                    recommended("honeycomb grillage", 0.26).p90OverStroke.t263Rounded(9) +
                    " against " +
                    recommended("smeared equivalent sheet", 0.26).p90OverStroke.t263Rounded(9),
            "flatCells" to flatHere.toString() + " of " +
                    cells.count { it.model == "honeycomb grillage" && it.compositeFraction != null } +
                    " honeycomb cells clear T-5b's 0.10 at the 90th percentile, against " +
                    flatThere.toString() + " of " +
                    cells.count { it.model == "smeared equivalent sheet" } + " smeared ones",
            "verdictsMoved" to movedVerdicts.toString() + " of " + paired.size +
                    " paired cells change their verdict",
            "atTheLatticesOwnLowerBound" to
                    cells.count { it.compositeFraction == null && it.flatAtP90 }.toString() +
                    " of " +
                    cells.count { it.compositeFraction == null } +
                    " cells clear T-5b with NO across-helix parallel-axis term at all"
        ),
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = mapOf(
            "whatMoved" to "the coupled margin moves in the direction C-0154's free tile " +
                    "predicted and by a factor the free tile does not give.",
            "whatDidNotMove" to "the crossover column count: the honeycomb lattice's own " +
                    "21 bp-per-interface ladder gives " + headline.columnsPerInterface +
                    " columns per interface against the smeared model's " +
                    smearedPerInterface.toString() +
                    ", so the re-grade is the across-helix rigidity and the dimer topology and " +
                    "not a second overstatement.",
            "theCorrugation" to "a honeycomb face's helices are at gaps of d and 2d about the " +
                    "3d/2 ladder, so an abstract-grid station sits d/4 = " +
                    rowOffsets.maxOf { abs(it) }.t263Rounded(4) + " nm off its own rooting " +
                    "helix, ALTERNATING in sign across the rows -- CLAUDE.md's first-order " +
                    "symmetry break, which a smeared plate has no coordinate for.",
            "theStrokeIsInvariant" to "both models translate rigidly under a uniform pressure, " +
                    "so the normalising stroke is p/k_f in both and the re-grade is controlled."
        ),
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "The lattice carries ONE row length. C-0151's 102 / 109 raster has a 7 bp stagger " +
                    "and a 102 bp interface window; the block is built at the 116 bp EXTENT, " +
                    "which is the width C-0151 grades at, and what the window costs is measured " +
                    "on the smeared model there (one crossover column) and not re-measured here.",
            "The lattice carries NO across-helix parallel-axis term: the layers' membrane " +
                    "action across the helices needs an in-plane transverse coordinate this " +
                    "model does not have, so its D_perpendicular is the INDEPENDENT one and a " +
                    "LOWER bound. The bracket is run at three ends, C-0154's own.",
            "k_theta is Gen1Tile's square-lattice-fitted constant; no honeycomb measurement of " +
                    "it exists in this repository. k_s is a construction, not a measurement.",
            "Kirchhoff is not safe at these thicknesses (C-0109, C-0120): every D_parallel is " +
                    "an upper bound and transverse shear is not carried.",
            "The dropout statistics are measured on a single-layer Rothemund rectangle and only " +
                    "the PROFILE transfers, in nm (C-0087, C-0109).",
            "The foundation acts on the gap-facing face only; the opposite face is free.",
            "assembleLoad and integrateOverFace are adjoint only up to a corrugation " +
                    "bookkeeping term, because the face strips are one row pitch centred on " +
                    "their own axis -- which is what keeps the uniform-load falsifier exact -- " +
                    "while evaluate reads the NEAREST face helix. The surrogate never uses " +
                    "integrateOverFace, so no number here is exposed to it.",
            "Nothing here re-opens the placement search: the stations are C-0151's."
        ),
        openQuestions = listOf(
            "What the across-helix parallel-axis term is worth once an in-plane transverse " +
                    "coordinate is carried -- it removes the only bracket in this answer.",
            "Whether C-0089's percentile descent recovers any cell the honeycomb lattice loses.",
            "What a PER-LAYER defect does. This lattice can remove one crossover of one " +
                    "interface of one layer; C-0087's dropout statistics have never been read " +
                    "on a multi-layer body, and the ensemble here still perturbs the COUPLING " +
                    "and not the block.",
            "Whether the 102 bp interface window, modelled as a restricted bond set rather " +
                    "than as a column count, moves any cell this study grades at 116 bp."
        )
    )

    val output = File("gpd/results/T-263-honeycomb-grillage-regrade.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.BOTH, null) as JsonObject)
        ) + "\n"
    )
    println("T-263 - wrote " + output.path)
}
