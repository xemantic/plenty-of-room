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
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForProse
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
// T-279 -- re-grade C-0167's 64 coupled cells on the TIED honeycomb lattice.
//
// CH-0227: HoneycombGrillage.bonds is the STAPLE crossover ladder, 435 bonds on the recommended
// 10 x 6 block, and a Rothemund-style raster also TURNS, H - 1 = 59 times, each turn a scaffold
// crossover with zero unpaired nucleotides and therefore a covalent tie at s = +-L/2, past the
// last plane of the ladder. The split is 435 + 59, which is C-0099's square-lattice 56 = 42 + 14
// read on the honeycomb.
//
// On the FREE tile C-0175 has measured what they are worth: 0.0501417316 -> 0.0446459684 at
// f = 0.30, i.e. every uncoupled reference in C-0154 and C-0167 is 1.12x too soft. The direction
// is CONSERVATIVE, which is the safe one, and that is why nobody re-ran the coupled cells.
//
// The re-grade CANNOT be inferred from the free tile: a coupling changes the load path, and
// C-0154's own composite fraction reads 0.2468 on the rigidity against 0.9405 on the dishing.
// So the answer is measured cell by cell, PAIRED per realisation on one shared dropout stream,
// and never published as a multiplier.
// ---------------------------------------------------------------------------------------------

private const val T279_SAMPLES: Int = 81
private const val T279_TOLERANCE: Double = 0.10
private const val T279_RIM_STANDOFF: Double = 1.0
private const val T279_RIM_BAND: Double = 6.7
private const val T279_SEED: Long = 197_197L
private const val T279_BLOCK_EXTENT_BP: Int = 116
private const val T279_LADDER_PHASE: Int = 16
private const val T279_LADDER_OFFSET: Int = 14
private const val T279_RECOMMENDED_ONE: Int = 102
private const val T279_RECOMMENDED_TWO: Int = 109

/**
 * The relative tolerance every same-quantity identity is asserted at.
 *
 * Emitted as a **threshold and a boolean**, never as a value: a departure between two quantities
 * equal by construction is nothing but the last few ulp, and one such field makes a result file
 * permanently un-diffable (`CLAUDE.md`).
 */
private const val T279_IDENTITY: Double = 1e-9

/** The study runs at 4 000 realisations; `T279_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t279Realisations: Int =
    if (System.getenv("T279_SMOKE") == "1") 150 else 4000

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T279CheapBoundRow(
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private class T279Geometry(
    val tieState: String,
    val compositeFraction: Double?,
    val hingeStiffnessEnhancement: Double,
    val bonds: Int,
    val turnTies: Int,
    val degreesOfFreedom: Int,
    val bandwidth: Int,
    val freeStroke: Double,
    val closedFormStroke: Double,
    val strokeMatchesClosedForm: Boolean,
    val strokeIdentityTolerance: Double,
    val uncoupledDishingOverStroke: Double,
    val uncoupledFlat: Boolean
)

@Serializable
private class T279Cell(
    val tieState: String,
    val compositeFraction: Double,
    val hingeStiffnessEnhancement: Double,
    val placement: String,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val uncoupledDishingOverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T279Paired(
    val comparison: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val realisations: Int,
    val medianRatio: Double,
    val p90Ratio: Double,
    val bestRatio: Double,
    val worstRatio: Double,
    val fractionTiedIsWorse: Double,
    val ratioOfPercentiles: Double,
    val untiedP90OverStroke: Double,
    val tiedP90OverStroke: Double,
    val untiedFlatAtP90: Boolean,
    val tiedFlatAtP90: Boolean,
    val verdictMoved: Boolean,
    val pairedAndUnpairedDisagreeInSign: Boolean
)

@Serializable
private class T279Prestrain(
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val departureDegrees: Double,
    val signAssignment: String,
    val nominalOverStroke: Double,
    val p90OverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val movementFromZeroPrestrain: Double
)

@Serializable
private class T279Convergence(
    val axis: String,
    val cell: String,
    val quantity: String,
    val coarse: String,
    val fine: String,
    val coarseValue: Double,
    val fineValue: Double,
    val departure: Double,
    val verdictAtCoarse: Boolean?,
    val verdictAtFine: Boolean?,
    val verdictSurvives: Boolean?
)

@Serializable
private class T279Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val closes: Boolean
)

@Serializable
private class T279Falsifier(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T279Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val cheapBound: List<T279CheapBoundRow>,
    val geometries: List<T279Geometry>,
    val cells: List<T279Cell>,
    val paired: List<T279Paired>,
    val prestrained: List<T279Prestrain>,
    val verdict: Map<String, String>,
    val convergence: List<T279Convergence>,
    val reproductions: List<T279Reproduction>,
    val falsifiers: List<T279Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T279Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T279_RIM_STANDOFF))
        )
}

private fun t279Profile(file: File): T279Profile {
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
    return T279Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`'s geometry, unchanged — so the only thing that differs between the halves is a tie. */
private class T279Shared(val profile: T279Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T279_BLOCK_EXTENT_BP
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)
    val d: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch: Double = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * rowPitch
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val closedFormStroke: Double = interiorPressure / Gen1Tile.FOUNDATION_SECANT
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val crossSection: String = "$rasterRows x $helicesPerRow"

    fun enhancementAt(fraction: Double): Double = multiLayerRigidities(
        layers = helicesPerRow,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = columnPitch
    ).realisedEnhancement
}

/** One tie state of one enhancement — the object a whole column of the comparison is graded on. */
private class T279Tile(
    val shared: T279Shared,
    val enhancement: Double,
    val tied: Boolean,
    val prestrainRadians: Double = 0.0,
    val subdivisions: Int = 1
) {
    val lattice: HoneycombGrillage = honeycombTiedLattice(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        enhancement = enhancement,
        tied = tied,
        prestrainRadians = prestrainRadians,
        subdivisions = subdivisions
    )

    val freeStroke: Double by lazy {
        lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(shared.pressureField).peakDishing(T279_SAMPLES) / freeStroke
    }

    fun surrogate(grid: List<Pair<Double, Double>>, samples: Int = T279_SAMPLES):
            InfluenceSurrogate =
        honeycombTiedSurrogate(lattice, grid, shared.pressureField, samples)
}

// ------------------------------------------------------------------------------ the grading

private class T279Graded(val cell: T279Cell, val sample: DoubleArray)

@Suppress("LongParameterList")
private fun gradeT279Cell(
    tieState: String,
    shared: T279Shared,
    fraction: Double,
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
): T279Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T279_TOLERANCE
    )
    return T279Graded(
        T279Cell(
            tieState = tieState,
            compositeFraction = fraction,
            hingeStiffnessEnhancement = enhancement,
            placement = placement,
            columns = columns,
            rows = shared.rasterRows,
            pathCount = grid.size,
            distribution = distribution,
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
            flatAtNominal = nominal < T279_TOLERANCE,
            flatAtP90 = summary.flatAtP90,
            beatsUncoupledAtP90 = summary.p90 < uncoupled
        ),
        sample
    )
}

private fun t279Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T279_RIM_BAND || abs(y) > edgeY / 2.0 - T279_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, unchanged, so the pairing is exact. */
private fun t279Placements(
    shared: T279Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T279_RECOMMENDED_ONE, T279_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T279_LADDER_PHASE, T279_LADDER_OFFSET
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

private fun t279PublishedCell(
    file: File,
    fraction: Double,
    placement: String,
    columns: Int,
    distribution: String,
    key: String
): Double {
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
        .first {
            it.getValue("model").jsonPrimitive.content == "honeycomb grillage" &&
                    it.getValue("compositeFraction").jsonPrimitive.content.toDoubleOrNull() ==
                    fraction &&
                    it.getValue("placement").jsonPrimitive.content == placement &&
                    it.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                    it.getValue("distribution").jsonPrimitive.content == distribution
        }
    return record.getValue(key).jsonPrimitive.content.toDouble()
}

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val t263 = ResultInputs.T_263.file()
    val shared = T279Shared(t279Profile(ResultInputs.T_3B.file()))
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)

    // ============================================ Deliverable 1 -- the cheap bound, no solver
    println("T-279 - the cheap bound, out of C-0167's own committed result file")
    val publishedCells = Json.parseToJsonElement(t263.readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
        .filter {
            it.getValue("model").jsonPrimitive.content == "honeycomb grillage" &&
                    it.getValue("compositeFraction").jsonPrimitive.content.toDoubleOrNull() != null
        }
    val publishedP90 = publishedCells.map {
        it.getValue("p90OverStroke").jsonPrimitive.content.toDouble()
    }
    val freeTileRatio = 0.890395426
    val multiplierThreshold = T279_TOLERANCE / freeTileRatio
    val candidates = publishedP90.count { it < multiplierThreshold }
    val cheapBound = listOf(
        T279CheapBoundRow(
            question = "how many of C-0167's 64 cells could the ties possibly rescue?",
            answer = "under the MULTIPLIER hypothesis -- every cell moving by the free tile's " +
                    "own " + freeTileRatio.emitted(9) + " -- a cell clears T-5b's " +
                    T279_TOLERANCE.emitted(2) + " only if its untied p90 is below " +
                    multiplierThreshold.emitted(9) + ", and " + candidates + " of " +
                    publishedP90.size + " are",
            consequence = "the cheap bound NARROWS the question to " + candidates +
                    " cells and cannot answer it: the tightest untied cell sits at " +
                    publishedP90.min().emitted(9) + ", " +
                    ((publishedP90.min() / T279_TOLERANCE - 1.0) * 100.0).emitted(3) +
                    " per cent over the tolerance"
        ),
        T279CheapBoundRow(
            question = "is the free tile's ratio a bound on a coupled cell at all?",
            answer = "no. A coupling changes the load path, and C-0154's own composite " +
                    "fraction reads 0.2468 on the RIGIDITY against 0.9405 on the DISHING -- " +
                    "the same lattice change is worth 3.8x more on one functional than on the " +
                    "other",
            consequence = "C-0167 measured the same thing from the other side: its own model " +
                    "change gave per-realisation median ratios running 1.064 to 2.475 across " +
                    "these 64 cells at a free-tile ratio of 1.868, and SIX cells where the " +
                    "unpaired reading has the opposite SIGN from the paired one"
        ),
        T279CheapBoundRow(
            question = "what IS bounded before any solve?",
            answer = "the Loewner statement K_tied >= K_untied fixes the sign of the strain " +
                    "energy under any fixed load -- the deflection AT a unit point load falls",
            consequence = "and it bounds NOTHING about peak dishing, which is a seminorm of " +
                    "the field, so a cell may read worse with the ties present. That is F7, " +
                    "declared open"
        )
    )
    cheapBound.forEach { println("  " + it.question + " -> " + it.consequence) }

    // ============================================ Deliverable 2 -- the tiles, tied and untied
    println("T-279 - the tiles")
    val tiles = HashMap<Pair<Double, Boolean>, T279Tile>()
    val geometries = ArrayList<T279Geometry>()
    val probe = T279Tile(shared, shared.enhancementAt(0.30), tied = false)
    val rootingHelixY = probe.lattice.faceBeams.map { probe.lattice.beamY[it] }
    fractions.forEach { fraction ->
        val enhancement = shared.enhancementAt(fraction)
        listOf(false, true).forEach { tied ->
            val tile = T279Tile(shared, enhancement, tied)
            tiles[fraction to tied] = tile
            geometries += T279Geometry(
                tieState = if (tied) "435 staple bonds + 59 raster turn ties" else
                    "435 staple bonds, no turn ties (C-0154 / C-0167)",
                compositeFraction = fraction,
                hingeStiffnessEnhancement = enhancement,
                bonds = tile.lattice.bonds.size,
                turnTies = tile.lattice.turnElements.size,
                degreesOfFreedom = tile.lattice.degreesOfFreedom,
                bandwidth = tile.lattice.bandwidth,
                freeStroke = tile.freeStroke,
                closedFormStroke = shared.closedFormStroke,
                strokeMatchesClosedForm = abs(tile.freeStroke - shared.closedFormStroke) <
                        T279_IDENTITY * shared.closedFormStroke,
                strokeIdentityTolerance = T279_IDENTITY,
                uncoupledDishingOverStroke = tile.uncoupledDishing,
                uncoupledFlat = tile.uncoupledDishing < T279_TOLERANCE
            )
        }
    }
    // C-0154's own lower bound: the lattice with no across-helix parallel-axis term at all.
    listOf(false, true).forEach { tied ->
        val tile = T279Tile(shared, 1.0, tied)
        geometries += T279Geometry(
            tieState = if (tied) "435 staple bonds + 59 raster turn ties" else
                "435 staple bonds, no turn ties (C-0154 / C-0167)",
            compositeFraction = null,
            hingeStiffnessEnhancement = 1.0,
            bonds = tile.lattice.bonds.size,
            turnTies = tile.lattice.turnElements.size,
            degreesOfFreedom = tile.lattice.degreesOfFreedom,
            bandwidth = tile.lattice.bandwidth,
            freeStroke = tile.freeStroke,
            closedFormStroke = shared.closedFormStroke,
            strokeMatchesClosedForm = abs(tile.freeStroke - shared.closedFormStroke) <
                    T279_IDENTITY * shared.closedFormStroke,
            strokeIdentityTolerance = T279_IDENTITY,
            uncoupledDishingOverStroke = tile.uncoupledDishing,
            uncoupledFlat = tile.uncoupledDishing < T279_TOLERANCE
        )
    }
    geometries.forEach {
        println("  " + it.tieState + "  f = " +
                (it.compositeFraction?.emitted(3) ?: "none") + "  uncoupled " +
                it.uncoupledDishingOverStroke.emitted(9) +
                (if (it.uncoupledFlat) "  flat" else "  NOT FLAT"))
    }

    // ============================================ Deliverable 3 -- the 64 cells, both tie states
    println("T-279 - the re-grade, " + t279Realisations + " realisations on one common stream")
    val cells = ArrayList<T279Cell>()
    val samples = HashMap<String, DoubleArray>()
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
    gradedColumns.forEach { columns ->
        t279Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            val ensemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, t279Realisations, T279_SEED
            )
            t279Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, stiffnesses) ->
                fractions.forEach { fraction ->
                    listOf(false, true).forEach { tied ->
                        val tile = tiles.getValue(fraction to tied)
                        val graded = gradeT279Cell(
                            if (tied) "tied" else "untied", shared, fraction, tile.enhancement,
                            placement, columns, grid, label, stiffnesses, tile.surrogate(grid),
                            tile.freeStroke, tile.uncoupledDishing, ensemble
                        )
                        cells += graded.cell
                        samples[(if (tied) "tied" else "untied") + "|" + fraction + "|" +
                                placement + "|" + columns + "|" + label] = graded.sample
                    }
                }
            }
        }
    }
    cells.filter { it.tieState == "tied" }.forEach {
        println("  tied  f=" + it.compositeFraction.emitted(3) + "  " + it.placement + "  " +
                it.columns + " x " + it.rows + " = " + it.pathCount + " paths, " +
                it.distribution + "  p90 " + it.p90OverStroke.emitted(9) +
                (if (it.flatAtP90) "  FLAT at p90" else "  not flat"))
    }

    // ============================================ Deliverable 4 -- the paired reading
    println("T-279 - the paired reading, per realisation on the shared stream")
    val paired = ArrayList<T279Paired>()
    gradedColumns.forEach { columns ->
        t279Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            t279Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, _) ->
                fractions.forEach { fraction ->
                    val key = { state: String ->
                        state + "|" + fraction + "|" + placement + "|" + columns + "|" + label
                    }
                    val summary = pairedRatioSummary(
                        samples.getValue(key("tied")), samples.getValue(key("untied"))
                    )
                    fun cellOf(state: String) = cells.first {
                        it.tieState == state && it.compositeFraction == fraction &&
                                it.placement == placement && it.columns == columns &&
                                it.distribution == label
                    }
                    val tied = cellOf("tied")
                    val untied = cellOf("untied")
                    paired += T279Paired(
                        comparison = "the tied honeycomb lattice over C-0167's untied one",
                        compositeFraction = fraction,
                        placement = placement,
                        columns = columns,
                        pathCount = grid.size,
                        distribution = label,
                        realisations = summary.realisations,
                        medianRatio = summary.median,
                        p90Ratio = summary.p90,
                        bestRatio = summary.best,
                        worstRatio = summary.worst,
                        fractionTiedIsWorse = summary.fractionAbove,
                        ratioOfPercentiles = summary.ratioOfPercentiles,
                        untiedP90OverStroke = untied.p90OverStroke,
                        tiedP90OverStroke = tied.p90OverStroke,
                        untiedFlatAtP90 = untied.flatAtP90,
                        tiedFlatAtP90 = tied.flatAtP90,
                        verdictMoved = untied.flatAtP90 != tied.flatAtP90,
                        pairedAndUnpairedDisagreeInSign =
                            (summary.median > 1.0) != (summary.ratioOfPercentiles > 1.0)
                    )
                }
            }
        }
    }

    // ============================================ Deliverable 5 -- the ties as a LOAD (CH-0228)
    println("T-279 - the ties as a LOAD: every allowed scaffold crossover is a prestrain")
    val departure = allowedScaffoldCrossoverDepartureDegrees()
    val prestrained = ArrayList<T279Prestrain>()
    val prestrainTiles = HashMap<Pair<Double, Int>, T279Tile>()
    fractions.forEach { fraction ->
        listOf(1, -1).forEach { sign ->
            prestrainTiles[fraction to sign] = T279Tile(
                shared, shared.enhancementAt(fraction), tied = true,
                prestrainRadians = sign * Math.toRadians(departure)
            )
        }
    }
    gradedColumns.forEach { columns ->
        t279Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            val ensemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, t279Realisations, T279_SEED
            )
            t279Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, stiffnesses) ->
                fractions.forEach { fraction ->
                    val bare = cells.first {
                        it.tieState == "tied" && it.compositeFraction == fraction &&
                                it.placement == placement && it.columns == columns &&
                                it.distribution == label
                    }
                    listOf(1, -1).forEach { sign ->
                        val tile = prestrainTiles.getValue(fraction to sign)
                        val zero = tiles.getValue(fraction to true)
                        val surrogate = tile.surrogate(grid)
                        val nominal = surrogate.solve(stiffnesses).peakDishing / zero.freeStroke
                        val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                        sample.indices.forEach { sample[it] = sample[it] / zero.freeStroke }
                        val summary = summariseDropoutDishing(
                            sample, nominal, ensemble.meanSurvivors, T279_TOLERANCE
                        )
                        prestrained += T279Prestrain(
                            compositeFraction = fraction,
                            placement = placement,
                            columns = columns,
                            pathCount = grid.size,
                            distribution = label,
                            departureDegrees = sign * departure,
                            signAssignment = if (sign > 0) "every turn the same way, positive"
                            else "every turn the same way, negative",
                            nominalOverStroke = nominal,
                            p90OverStroke = summary.p90,
                            flatAtNominal = nominal < T279_TOLERANCE,
                            flatAtP90 = summary.flatAtP90,
                            movementFromZeroPrestrain = summary.p90 - bare.p90OverStroke
                        )
                    }
                }
            }
        }
    }

    // ============================================ the verdict
    val tiedCells = cells.filter { it.tieState == "tied" }
    val untiedCells = cells.filter { it.tieState == "untied" }
    val verdict = linkedMapOf(
        "cellsGraded" to (tiedCells.size.toString() + " tied and " + untiedCells.size +
                " untied, one common dropout stream restricted per cell"),
        "tiedCellsFlatAtP90" to (tiedCells.count { it.flatAtP90 }.toString() + " of " +
                tiedCells.size),
        "untiedCellsFlatAtP90" to (untiedCells.count { it.flatAtP90 }.toString() + " of " +
                untiedCells.size + " -- C-0167's own 0 of 64, reproduced"),
        "tiedCellsFlatAtNominal" to (tiedCells.count { it.flatAtNominal }.toString() + " of " +
                tiedCells.size),
        "verdictsMoved" to (paired.count { it.verdictMoved }.toString() + " of " + paired.size),
        "medianRatioRange" to (paired.minOf { it.medianRatio }.emitted(9) + " to " +
                paired.maxOf { it.medianRatio }.emitted(9)),
        "cellsAtWhichTheTiesAreADISHINGSOURCE" to
                (paired.count { it.medianRatio > 1.0 }.toString() + " of " + paired.size),
        "cellsWhereThePairedAndUnpairedReadingsDisagreeInSign" to
                (paired.count { it.pairedAndUnpairedDisagreeInSign }.toString() + " of " +
                        paired.size),
        "everyCoupledCellIsWorseThanTheUncoupledTile" to
                (tiedCells.count { !it.beatsUncoupledAtP90 }.toString() + " of " +
                        tiedCells.size + " (C-0109, reproduced on the tied lattice)"),
        "prestrainedCellsFlatAtP90" to (prestrained.count { it.flatAtP90 }.toString() + " of " +
                prestrained.size + ", at +-" + departure.emitted(9) + " degrees")
    )
    verdict.forEach { (k, v) -> println("  " + k + ": " + v) }

    // ============================================ convergence
    //
    // CLAUDE.md: declare a falsifier on every threshold the moving quantity feeds, not only on
    // the one the study is about. The quantity that decides this study is the p90 of the cells
    // whose T-5b verdict MOVES, and the tightest of them clears the tolerance by well under a
    // per cent -- so the convergence axes are taken THERE, on the p90 itself, and not only on a
    // nominal at some other cell.
    println("T-279 - convergence")
    val convergence = ArrayList<T279Convergence>()
    val fineTile = T279Tile(shared, shared.enhancementAt(0.30), tied = true, subdivisions = 2)
    val decidingCells = (paired.filter { it.verdictMoved } +
            paired.filter { it.tiedFlatAtP90 }).distinctBy {
        listOf(it.compositeFraction, it.placement, it.columns, it.distribution).joinToString("|")
    }
    decidingCells.forEach { deciding ->
        val grid = t279Placements(shared, rootingHelixY, deciding.columns)
            .first { it.first == deciding.placement }.second
        val stiffnesses = t279Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == deciding.distribution }.second
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> incorporation.at(x, y) }, t279Realisations, T279_SEED
        )
        val label = "f = " + deciding.compositeFraction.emitted(3) + ", " + deciding.placement +
                ", " + deciding.columns + " x " + shared.rasterRows + " = " + grid.size +
                " paths, " + deciding.distribution
        fun p90At(tile: T279Tile, samples: Int): Double {
            val surrogate = tile.surrogate(grid, samples)
            val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
            sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
            return summariseDropoutDishing(
                sample, surrogate.solve(stiffnesses).peakDishing / tile.freeStroke,
                ensemble.meanSurvivors, T279_TOLERANCE
            ).p90
        }
        val coarseTile = tiles.getValue(deciding.compositeFraction to true)
        val base = deciding.tiedP90OverStroke
        if (deciding.compositeFraction == 0.30) {
            val fine = p90At(fineTile, T279_SAMPLES)
            convergence += T279Convergence(
                axis = "beam subdivisions",
                cell = label,
                quantity = "the p90 of the dropout ensemble, over the stroke",
                coarse = "1", fine = "2",
                coarseValue = base, fineValue = fine,
                departure = abs(fine - base) / abs(base),
                verdictAtCoarse = base < T279_TOLERANCE,
                verdictAtFine = fine < T279_TOLERANCE,
                verdictSurvives = (base < T279_TOLERANCE) == (fine < T279_TOLERANCE)
            )
        }
        listOf(41 to 81, 81 to 161).forEach { (coarse, fine) ->
            val a = p90At(coarseTile, coarse)
            val b = p90At(coarseTile, fine)
            convergence += T279Convergence(
                axis = "the dishing sample grid",
                cell = label,
                quantity = "the p90 of the dropout ensemble, over the stroke",
                coarse = coarse.toString(), fine = fine.toString(),
                coarseValue = a, fineValue = b,
                departure = if (a == 0.0) abs(b) else abs(b - a) / abs(a),
                verdictAtCoarse = a < T279_TOLERANCE,
                verdictAtFine = b < T279_TOLERANCE,
                verdictSurvives = (a < T279_TOLERANCE) == (b < T279_TOLERANCE)
            )
        }
    }
    // and C-0167's own convergence cell, so the two studies' axes are comparable
    val recommendedGrid = t279Placements(shared, rootingHelixY, 1)
        .first { it.first == "abstract grid" }.second
    val recommendedShare = equalShareOfMandate(recommendedGrid.size)
    val recommendedTile = tiles.getValue(0.30 to true)
    val coarseNominal = recommendedTile.surrogate(recommendedGrid)
        .solve(recommendedShare).peakDishing / recommendedTile.freeStroke
    val fineNominal = fineTile.surrogate(recommendedGrid)
        .solve(recommendedShare).peakDishing / fineTile.freeStroke
    convergence += T279Convergence(
        axis = "beam subdivisions",
        cell = "C-0167's own convergence cell: f = 0.30, abstract grid, 1 x 10 = 10 paths, " +
                "equal springs, TIED",
        quantity = "the nominal dishing over the stroke, no defects",
        coarse = "1", fine = "2",
        coarseValue = coarseNominal, fineValue = fineNominal,
        departure = abs(fineNominal - coarseNominal) / abs(coarseNominal),
        verdictAtCoarse = null, verdictAtFine = null, verdictSurvives = null
    )
    listOf(41 to 81, 81 to 161).forEach { (coarse, fine) ->
        val a = recommendedTile.surrogate(recommendedGrid, coarse)
            .solve(recommendedShare).peakDishing / recommendedTile.freeStroke
        val b = recommendedTile.surrogate(recommendedGrid, fine)
            .solve(recommendedShare).peakDishing / recommendedTile.freeStroke
        convergence += T279Convergence(
            axis = "the dishing sample grid",
            cell = "C-0167's own convergence cell: f = 0.30, abstract grid, 1 x 10 = 10 paths, " +
                    "equal springs, TIED",
            quantity = "the nominal dishing over the stroke, no defects",
            coarse = coarse.toString(), fine = fine.toString(),
            coarseValue = a, fineValue = b,
            departure = if (a == 0.0) abs(b) else abs(b - a) / abs(a),
            verdictAtCoarse = null, verdictAtFine = null, verdictSurvives = null
        )
    }
    // the SAME cell on the UNTIED lattice, which must reproduce C-0167's own 0.00011 -- the
    // control that says the subdivision sensitivity below belongs to the TIES and not to this
    // study's code.
    val untiedRecommended = tiles.getValue(0.30 to false)
    val untiedFine = T279Tile(shared, shared.enhancementAt(0.30), tied = false, subdivisions = 2)
    val untiedCoarseNominal = untiedRecommended.surrogate(recommendedGrid)
        .solve(recommendedShare).peakDishing / untiedRecommended.freeStroke
    val untiedFineNominal = untiedFine.surrogate(recommendedGrid)
        .solve(recommendedShare).peakDishing / untiedFine.freeStroke
    convergence += T279Convergence(
        axis = "beam subdivisions",
        cell = "C-0167's own convergence cell: f = 0.30, abstract grid, 1 x 10 = 10 paths, " +
                "equal springs, UNTIED -- the control",
        quantity = "the nominal dishing over the stroke, no defects",
        coarse = "1", fine = "2",
        coarseValue = untiedCoarseNominal, fineValue = untiedFineNominal,
        departure = abs(untiedFineNominal - untiedCoarseNominal) / abs(untiedCoarseNominal),
        verdictAtCoarse = null, verdictAtFine = null, verdictSurvives = null
    )
    convergence.forEach {
        println("  " + it.axis + "  " + it.coarse + " -> " + it.fine + "  departure " +
                it.departure.emitted(2) + "  " + it.cell +
                (if (it.verdictSurvives == false) "  VERDICT MOVES" else ""))
    }

    // ============================================ reproductions
    println("T-279 - reproductions")
    val reproductions = ArrayList<T279Reproduction>()
    fun reproduce(source: String, quantity: String, published: Double, here: Double) {
        val d = if (published == 0.0) abs(here) else abs(here - published) / abs(published)
        reproductions += T279Reproduction(source, quantity, published, here, d, d < 1e-8)
    }
    // C-0175's tied free tiles
    reproduce(
        "C-0175 (T-254)", "tied free tile, 10 x 6, f = 0.30",
        0.0446459684, tiles.getValue(0.30 to true).uncoupledDishing
    )
    reproduce(
        "C-0175 (T-254)", "tied free tile, 10 x 6, f = 0.26",
        0.0467367262, tiles.getValue(0.26 to true).uncoupledDishing
    )
    reproduce(
        "C-0175 (T-254)", "untied free tile, 10 x 6, f = 0.30",
        0.0501417316, tiles.getValue(0.30 to false).uncoupledDishing
    )
    reproduce(
        "C-0167 (T-263)", "untied free tile, 10 x 6, f = 0.26",
        0.0522223659, tiles.getValue(0.26 to false).uncoupledDishing
    )
    reproduce(
        "C-0167 (T-263)", "the recommended cell's nominal, UNTIED, subdivisions 1",
        0.0626407003, untiedCoarseNominal
    )
    reproduce(
        "C-0167 (T-263)", "the recommended cell's nominal, UNTIED, subdivisions 2",
        0.0626474141, untiedFineNominal
    )
    // C-0167's own 64 committed cells, every one of them
    var worstCellDeparture = 0.0
    var cellsClosing = 0
    untiedCells.forEach { cell ->
        listOf("p90OverStroke" to cell.p90OverStroke, "nominalOverStroke" to cell.nominalOverStroke)
            .forEach { (key, here) ->
                val published = t279PublishedCell(
                    t263, cell.compositeFraction, cell.placement, cell.columns,
                    cell.distribution, key
                )
                val d = if (published == 0.0) abs(here) else abs(here - published) / abs(published)
                if (d < 1e-8) cellsClosing++
                worstCellDeparture = maxOf(worstCellDeparture, d)
            }
    }
    // CLAUDE.md: a difference of two nearly equal numbers is emitted at TWO significant digits,
    // and BOTH columns of this record are one -- `here` carries a departure, not a quantity, so
    // rounding only the key the rounding layer knows about leaves the file un-diffable. Two runs
    // of this study duly disagreed in exactly this one field and nowhere else.
    val worstCellDepartureEmitted = roundForResult(worstCellDeparture, 2, 0.0)
    reproductions += T279Reproduction(
        source = "C-0167 (T-263)",
        quantity = "all " + untiedCells.size + " committed cells, p90 and nominal -- " +
                cellsClosing + " of " + (2 * untiedCells.size) + " values close at 1e-8; both " +
                "columns of this row are a DEPARTURE and are emitted at two significant digits",
        published = 0.0,
        here = worstCellDepartureEmitted,
        departure = worstCellDepartureEmitted,
        closes = cellsClosing == 2 * untiedCells.size
    )
    reproductions.forEach {
        println("  " + it.source + "  " + it.quantity + "  departure " + it.departure.emitted(2) +
                (if (it.closes) "  closes" else "  DOES NOT CLOSE"))
    }

    // ============================================ falsifiers
    val uniformField = tiles.getValue(0.30 to true).lattice
        .solve(uniformPressure(shared.interiorPressure))
    val uniformDishing = uniformField.peakDishing(T279_SAMPLES) / uniformField.meanDeflection
    val emptyTie = T279Tile(shared, shared.enhancementAt(0.30), tied = false)
    val c0167Object = HoneycombGrillage(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = shared.enhancementAt(0.30)
    )
    val loadA = c0167Object.assembleLoad(shared.pressureField)
    val loadB = emptyTie.lattice.assembleLoad(shared.pressureField)
    var loadIdentical = true
    for (i in 0 until c0167Object.degreesOfFreedom) {
        if (loadA[i] != loadB[i]) loadIdentical = false
    }
    val siteSetIdentical =
        c0167Object.bonds.map { it.site } == emptyTie.lattice.bonds.map { it.site }
    val multiplierWorst = paired.maxOf { abs(it.medianRatio - freeTileRatio) }
    val bestTiedP90 = tiedCells.minOf { it.p90OverStroke }
    val falsifiers = listOf(
        T279Falsifier(
            "F1", "a uniform pressure on the tied lattice dishes exactly zero",
            uniformDishing > T279_IDENTITY,
            "peak dishing over stroke " + uniformDishing.emitted(2) + " against " +
                    T279_IDENTITY.emitted(2) + "; the stroke is p/k_f to " +
                    T279_IDENTITY.emitted(2) + " with 59 rim ties present"
        ),
        T279Falsifier(
            "F2", "the untied re-grade reproduces C-0167's 64 committed cells at 1e-8",
            worstCellDeparture > 1e-8,
            "worst relative departure over " + (2 * untiedCells.size) + " values: " +
                    worstCellDeparture.emitted(2)
        ),
        T279Falsifier(
            "F3", "an empty tie list is bit-identical to C-0167's object",
            !(loadIdentical && siteSetIdentical),
            "the crossover site set and assembleLoad are bit-identical; the point-load dual " +
                    "and the solved field are asserted in HoneycombTiedRegradeTest"
        ),
        T279Falsifier(
            "F4", "the tied free tile reproduces C-0175's three readings at 1e-8",
            reproductions.take(4).any { !it.closes },
            "worst of the four free-tile reproductions: " +
                    reproductions.take(4).maxOf { it.departure }.emitted(2)
        ),
        T279Falsifier(
            "F5", "the ties move NO flatness verdict -- declared open",
            paired.count { it.verdictMoved } > 0,
            paired.count { it.verdictMoved }.toString() + " of " + paired.size +
                    " paired cells change their T-5b verdict"
        ),
        T279Falsifier(
            "F6", "the per-cell movement is a MULTIPLIER, every median ratio within 1e-3 of " +
                    "the free tile's " + freeTileRatio.emitted(9) + " -- declared open",
            multiplierWorst > 1e-3,
            "the largest departure of a cell's median per-realisation ratio from the free " +
                    "tile's is " + multiplierWorst.emitted(3) + ", over a median-ratio range of " +
                    paired.minOf { it.medianRatio }.emitted(9) + " to " +
                    paired.maxOf { it.medianRatio }.emitted(9)
        ),
        T279Falsifier(
            "F8", "a cell whose T-5b verdict the ties MOVE keeps that verdict under its own " +
                    "convergence axes -- the beam subdivision and the dishing sample grid, " +
                    "taken on the p90 itself at the deciding cell -- declared open",
            convergence.any { it.verdictSurvives == false },
            convergence.filter { it.verdictSurvives != null }.let { rows ->
                rows.count { it.verdictSurvives == false }.toString() + " of " + rows.size +
                        " deciding-cell convergence steps move the verdict; the largest " +
                        "departure on a deciding p90 is " +
                        rows.maxOf { it.departure }.emitted(3) + " against a tightest margin " +
                        "of " + (1.0 - (bestTiedP90 / T279_TOLERANCE)).emitted(3) +
                        " of the tolerance"
            }
        ),
        T279Falsifier(
            "F7", "the tied lattice reads WORSE than the untied one at some cell -- declared open",
            paired.any { it.medianRatio > 1.0 },
            paired.count { it.medianRatio > 1.0 }.toString() + " of " + paired.size +
                    " cells have a median per-realisation ratio above one; the worst single " +
                    "realisation ratio anywhere is " + paired.maxOf { it.worstRatio }.emitted(9)
        )
    )
    falsifiers.forEach {
        println("  " + it.name + (if (it.fired) "  FIRED  " else "  did not fire  ") + it.note)
    }

    // ============================================ emission
    val bestTied = tiedCells.minByOrNull { it.p90OverStroke }
    val result = T279Result(
        task = "T-279",
        leaf = "A8.2",
        title = "C-0167's 64 coupled cells re-graded on the TIED honeycomb lattice",
        verificationType = "in-silico (the same beam-and-bond lattice, the same exact Woodbury " +
                "coupling surrogate and the same measured-incorporation dropout ensemble, with " +
                "59 covalent scaffold-turn ties added) + logical (an exact bit-identity between " +
                "the empty-tie lattice and the object C-0167 measured, and a cheap bound taken " +
                "out of C-0167's own committed result file with no solve)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "k_theta at a scaffold turn is asserted equal to k_theta at a staple crossover " +
                "because it is the same covalent object, not because anything measured it " +
                "(CH-0227 section 7); the tie's axial station is taken at s = +-L/2 exactly, " +
                "where a scaffold crossover sits 5 bp from a staple position.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 = 1 MPa",
            "angle" to "rad internally, degrees in prose",
            "dishing" to "dimensionless, as a fraction of the free stroke"
        ),
        conventions = mapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "z" to "along the block's thickness",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "pointLoad" to "force positive downward, so a coupling's upward support force " +
                    "enters as its negative",
            "tie" to "a raster turn is a scaffold crossover with zero unpaired nucleotides, " +
                    "assembled with the same hinge, link and slip a lattice bond carries, at " +
                    "s = +-L/2, alternating ends along the raster path"
        ),
        parameters = mapOf(
            "crossSection" to shared.crossSection,
            "rowBasePairs" to shared.rowBasePairs.toString(),
            "edgeX" to shared.edgeX.emitted(9),
            "edgeY" to shared.edgeY.emitted(9),
            "interhelicalDistance" to shared.d.emitted(9),
            "rowPitch" to shared.rowPitch.emitted(9),
            "columnPitch" to shared.columnPitch.emitted(9),
            "interiorPressure" to shared.interiorPressure.emitted(9),
            "closedFormStroke" to shared.closedFormStroke.emitted(9),
            "hingeStiffness" to Gen1Tile.crossoverHingeStiffness().emitted(9),
            "slipStiffness" to Gen1Tile.crossoverInPlaneStiffness().emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "compositeFractions" to "0.30 and 0.26 (C-0116), plus the lattice's own 1.0",
            "mandate" to "C-0017's 33.3333 pN/nm on the SUM, section 3's acceptable clause",
            "realisations" to t279Realisations.toString(),
            "seed" to T279_SEED.toString(),
            "samples" to T279_SAMPLES.toString(),
            "tolerance" to T279_TOLERANCE.emitted(2),
            "raster" to (T279_RECOMMENDED_ONE.toString() + " / " + T279_RECOMMENDED_TWO +
                    " (C-0151, drawable)"),
            "ladderPhase" to T279_LADDER_PHASE.toString(),
            "ladderOffset" to T279_LADDER_OFFSET.toString(),
            "allowedScaffoldCrossoverDepartureDegrees" to departure.emitted(9),
            "firstAxialSign" to "+1"
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_263.path + " (C-0167's 64 committed cells, reproduced)"
        ),
        citedInputs = mapOf(
            "C-0175 tied free tile, f = 0.30" to "0.0446459684",
            "C-0175 tied free tile, f = 0.26" to "0.0467367262",
            "C-0167 untied free tile, f = 0.30" to "0.0501417316",
            "C-0167 untied free tile, f = 0.26" to "0.0522223659",
            "C-0175 free-tile ratio at f = 0.30" to "0.890395426",
            "C-0154 composite fraction on the rigidity" to "0.2468",
            "C-0154 composite fraction on the dishing" to "0.9405"
        ),
        cheapBound = cheapBound,
        geometries = geometries,
        cells = cells,
        paired = paired,
        prestrained = prestrained,
        verdict = verdict,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = listOf(
            "The 59 raster turn ties move " + paired.count { it.verdictMoved } + " of " +
                    paired.size + " flatness verdicts. C-0167's 0 of 64 becomes " +
                    tiedCells.count { it.flatAtP90 } + " of " + tiedCells.size + ".",
            "It is NOT a multiplier: the per-realisation median ratio runs " +
                    paired.minOf { it.medianRatio }.emitted(9) + " to " +
                    paired.maxOf { it.medianRatio }.emitted(9) + " across the 64 paired cells, " +
                    "against the free tile's own " + freeTileRatio.emitted(9) + " -- a spread of " +
                    (paired.maxOf { it.medianRatio } - paired.minOf { it.medianRatio })
                        .emitted(9) + ", so no table can be rescaled.",
            "The tightest tied cell is " +
                    (bestTied?.p90OverStroke?.emitted(9) ?: "none") + " at " +
                    (bestTied?.placement ?: "-") + ", " + (bestTied?.columns ?: 0) + " x " +
                    shared.rasterRows + " = " + (bestTied?.pathCount ?: 0) + " paths, " +
                    (bestTied?.distribution ?: "-") + ", f = " +
                    (bestTied?.compositeFraction?.emitted(3) ?: "-") + ".",
            "The uncoupled tied block is flat at " +
                    tiles.getValue(0.30 to true).uncoupledDishing.emitted(9) + " and " +
                    tiles.getValue(0.26 to true).uncoupledDishing.emitted(9) + " at the two ends " +
                    "of C-0116's band, and " +
                    tiedCells.count { !it.beatsUncoupledAtP90 } + " of " + tiedCells.size +
                    " coupled cells are worse than it -- C-0109 on the tied lattice.",
            "The verdict is thin and it is CONVERGED where it is thin: the tightest tied " +
                    "cell clears T-5b by " +
                    ((1.0 - bestTiedP90 / T279_TOLERANCE) * 100.0).emitted(3) +
                    " per cent, and over the deciding cells' own convergence axes -- the beam " +
                    "subdivision and the dishing sample grid, taken on the p90 itself -- " +
                    convergence.filter { it.verdictSurvives != null }
                        .count { it.verdictSurvives == false } + " of " +
                    convergence.count { it.verdictSurvives != null } +
                    " steps move it, at a largest departure of " +
                    convergence.filter { it.verdictSurvives != null }
                        .maxOf { it.departure }.emitted(3) + ".",
            "The ties as a LOAD (CH-0228): at the allowed " + departure.emitted(9) +
                    " degrees carried by every allowed honeycomb scaffold crossover, " +
                    prestrained.count { it.flatAtP90 } + " of " + prestrained.size +
                    " prestrained cells clear T-5b at the 90th percentile, and the worst " +
                    "movement from the zero-prestrain tied cell is " +
                    prestrained.maxOf { abs(it.movementFromZeroPrestrain) }.emitted(9) +
                    " of the stroke."
        ),
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "k_theta is Gen1Tile's SQUARE-lattice-fitted constant and k_s is a construction; no " +
                    "honeycomb measurement of either exists in this repository. A scaffold " +
                    "turn is assembled with the same three elements a staple crossover has " +
                    "because it is the same covalent object.",
            "The tie sits at s = +-L/2 exactly. A scaffold crossover sits 5 bp from a staple " +
                    "position, so its true axial station is within 1.7 nm of the rim node.",
            "The lattice carries NO across-helix parallel-axis term, so its D_perp is the " +
                    "INDEPENDENT one and a lower bound; the composite fraction enters as a " +
                    "smeared multiplier on k_theta (C-0167 section 8, unchanged).",
            "Kirchhoff is not safe at these thicknesses (C-0109, C-0120): transverse shear is " +
                    "not carried, so every D_parallel here is an upper bound.",
            "The dropout statistics are measured on a single-layer Rothemund rectangle and only " +
                    "the PROFILE transfers, in nm (C-0087, C-0109); the ensemble perturbs the " +
                    "COUPLING and never the block's own crossovers or its ties.",
            "The lattice carries ONE row length. C-0151's 102 / 109 raster has a 7 bp stagger " +
                    "and a 102 bp interface window; the block here is built at the 116 bp " +
                    "extent, which is the width C-0167 grades at, so the comparison is " +
                    "controlled and the window is not modelled.",
            "Nothing here re-opens the placement search, the distribution rule, the raster or " +
                    "the cross-section. The stations are C-0151's and the distributions are " +
                    "C-0058's two.",
            "The prestrain deliverable sweeps the two UNIFORM sign assignments only. C-0175 " +
                    "measures a 0.7 per cent spread over three assignments on the free tile and " +
                    "the triangle-inequality ceiling bounds every subset; neither is re-taken " +
                    "here on a coupled cell."
        ),
        openQuestions = listOf(
            "Whether the recommended 10 x 6 block needs an attachment coupling at all. The " +
                    "uncoupled TIED block is flat at both ends of the measured band and every " +
                    "coupled cell graded here is worse than it.",
            "What the across-helix parallel-axis term is worth once an in-plane transverse " +
                    "coordinate is carried -- it removes the only bracket in this answer.",
            "Whether a distribution SEARCHED on the tied lattice, rather than transferred onto " +
                    "it, recovers any cell. Every distribution graded here is a rule written on " +
                    "a smeared model's geometry.",
            "What the tie's true axial station is worth. A scaffold crossover sits 5 bp from a " +
                    "staple position and the ties are placed at the rim node exactly.",
            "Whether the 102 bp interface window, modelled as a restricted bond set, moves any " +
                    "cell graded here at the 116 bp extent."
        ),
        proseFailure = "none"
    )

    val output = File("gpd/results/T-279-tied-honeycomb-regrade.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-279 - wrote " + output.path)
}
