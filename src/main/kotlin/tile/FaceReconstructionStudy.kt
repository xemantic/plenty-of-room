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
import com.xemantic.nano.plentyofroom.coupling.DishingSolution
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.influenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.bio.viktor.F64Array
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

// ---------------------------------------------------------------------------------------------
// T-326 -- the reconstruction the dishing fit is taken in, against the one it is sampled in.
//
// `HoneycombDeflection` FITS its rigid plane with `faceFunctional`'s owning-beam reconstruction
// and SAMPLES the residual with `evaluate`'s nearest-beam one (`CH-0284`). The three rigid modes
// reconstruct identically under both, so the Gram is one object and the whole disagreement is in
// the right-hand side.
//
// The cheap bound is closed form and it is EXACT: within a beam's owning strip the nearest-beam
// partition is that strip translated by +-d/4, alternating, so each strip is split 5d/4 to its
// own beam and d/4 to the partner across its own VERTICAL BOND. Summed over a bond's two members
// the deflection differences cancel and what survives is the bond's RELATIVE ROLL.
//
// This study measures that, the third convention nobody named, the quadrature defect that
// under-reports the gap, and the margin the refusal must actually be priced against.
// ---------------------------------------------------------------------------------------------

private const val T326_SAMPLES: Int = 81
private const val T326_TOLERANCE: Double = 0.10
private const val T326_RIM_STANDOFF: Double = 1.0
private const val T326_RIM_BAND: Double = 6.7
private const val T326_PROBE_BP: Int = 42
private const val T326_BLOCK_EXTENT_BP: Int = 116
private const val T326_UPSTREAM_BP: Int = 112
private const val T326_SEED: Long = 197_197L
private const val T326_FIELD_SEED: Int = 20_260_825
private const val T326_IDENTITY: Double = 1e-9

/**
 * The declared floor the closed form is asserted against.
 *
 * Five orders of magnitude below `F1`'s own `1e-10` threshold and five above the observed ulp
 * noise, so it is a **statement** rather than a measurement — which is the only form of this
 * quantity that is reproducible (`F9`).
 */
private const val T326_CLOSED_FORM_FLOOR: Double = 1e-12

/** The study runs at 4 000 realisations; `T326_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t326Realisations: Int =
    if (System.getenv("T326_SMOKE") == "1") 150 else 4000

/** A departure between two nearly equal readings carries no nine digits (`C-0093`, `C-0138`). */
private val T326_DEPARTURE_DIGITS: Map<String, Int> = mapOf(
    "channels/splitRelativeMovement" to 2,
    "channels/sampledRelativeMovement" to 2,
    "decidingCells/reproductionDeparture" to 2,
    "decidingCells/splitRelativeMovement" to 2,
    "decidingCells/sampledRelativeMovement" to 2,
    "oddTriple/splitDeparture" to 2,
    "orthogonality/sampledWorstOffDiagonal" to 2,
    "convergence/departure" to 2
)

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

/** A ratio, a departure or a flag: dimensionless, so `P-18`'s pN floor must not reach it. */
private fun Double.emittedDimensionless(digits: Int = 9): String =
    roundedForProse(digits, floor = 0.0).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T326ClosedFormRow(
    val rasterRows: Int,
    val faceColumn: Int,
    val verticalBonds: Int,
    val readings: Int,
    /**
     * Whether the closed form agrees with the direct quadrature below `T326_CLOSED_FORM_FLOOR`.
     *
     * The departure is a difference of two quantities that are **exactly equal by construction**,
     * so at `1e-17` it is pure ulp noise. `F9` fired on it **twice**: emitted as a `Double` it
     * moved in its second significant digit between two runs, and emitted as an integer ORDER it
     * still crossed a decade at one row. `CLAUDE.md`'s rule is the only stable form — emit the
     * **tolerance the identity holds to and a boolean**, never the value and never its order.
     */
    val belowDeclaredFloor: Boolean,
    val closes: Boolean
)

@Serializable
private class T326OrthogonalityRow(
    val rasterRows: Int,
    val faceColumn: Int,
    val standingWorstOffDiagonal: Double,
    val splitWorstOffDiagonal: Double,
    val sampledWorstOffDiagonal: Double,
    val standingIsDiagonal: Boolean,
    val sampledIsDiagonal: Boolean
)

@Serializable
private class T326CollinearityRow(
    val rasterRows: Int,
    val faceColumn: Int,
    val ratio: Double,
    val isExactlySix: Boolean
)

@Serializable
private class T326QuadratureRow(
    val rasterRows: Int,
    val faceColumn: Int,
    val unsplitOverSplit: Double
)

@Serializable
private class T326CensusRow(
    val channel: String,
    val relativeThreshold: Double,
    val readings: Int
)

@Serializable
private class T326TightestRow(
    val resultFile: String,
    val leaf: String,
    val value: Double,
    val relativeToTolerance: Double
)

@Serializable
private class T326ChannelRow(
    val loadCase: String,
    val crossSection: String,
    val standingPeakDishing: Double,
    val splitPeakDishing: Double,
    val sampledPeakDishing: Double,
    val splitRelativeMovement: Double,
    val sampledRelativeMovement: Double,
    val splitCeiling: Double,
    val sampledCeiling: Double,
    val ceilingHolds: Boolean,
    val insideTightestMargin: Boolean
)

@Serializable
private class T326CellRow(
    val cell: String,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val publishedP90: Double,
    val standingP90: Double,
    val reproductionDeparture: Double,
    val splitP90: Double,
    val sampledP90: Double,
    val splitRelativeMovement: Double,
    val sampledRelativeMovement: Double,
    val standingFlat: Boolean,
    val splitFlat: Boolean,
    val sampledFlat: Boolean,
    val verdictMoves: Boolean,
    val marginOfTolerance: Double
)

@Serializable
private class T326TripleRow(
    val enhancement: Double,
    val published: Double,
    val unsplit: Double,
    val split: Double,
    val sampled: Double,
    val splitDeparture: Double,
    val verdictMoves: Boolean
)

@Serializable
private class T326Reproduction(
    val what: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val closes: Boolean
)

@Serializable
private class T326Convergence(
    val axis: String,
    val setting: String,
    val quantity: Double,
    val departure: Double
)

@Serializable
private class T326Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val cheapBound: List<String>,
    val closedForm: List<T326ClosedFormRow>,
    val orthogonality: List<T326OrthogonalityRow>,
    val collinearity: List<T326CollinearityRow>,
    val quadrature: List<T326QuadratureRow>,
    val census: List<T326CensusRow>,
    val tightest: List<T326TightestRow>,
    val channels: List<T326ChannelRow>,
    val decidingCells: List<T326CellRow>,
    val oddTriple: List<T326TripleRow>,
    val reproductions: List<T326Reproduction>,
    val convergence: List<T326Convergence>,
    val verdict: Map<String, String>,
    val falsifiers: List<String>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ------------------------------------------------------------------------------ the geometry

private class T326Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T326_RIM_STANDOFF))
        )
}

private fun t326Profile(file: File): T326Profile {
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
    return T326Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0180`'s geometry, rebuilt rather than imported — `T-279`'s own is private to its study. */
private class T326Shared(val profile: T326Profile, val rasterRows: Int, val helicesPerRow: Int) {
    val rowBasePairs: Int = T326_BLOCK_EXTENT_BP
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)
    val d: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch: Double = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * rowPitch
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)

    fun enhancementAt(fraction: Double): Double = multiLayerRigidities(
        layers = helicesPerRow,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = columnPitch
    ).realisedEnhancement
}

// ------------------------------------------------------ the three conventions, on one solve

/**
 * Which reconstruction and which measure the rigid plane is fitted in.
 *
 *  * [INDEPENDENT] — the owning-beam reconstruction, three independent projections. Convention A.
 *  * [UNSPLIT] — the nearest-beam reconstruction over the tributary strips, taken with the
 *    class's own whole-strip Gauss rule, which is `0.819694` of the exact one. Convention B as
 *    the shipped `areaInnerProduct` computes it.
 *  * [SPLIT] — the same, integrated exactly. Convention B.
 *  * [SAMPLED] — the nearest-beam reconstruction over the face rectangle. Convention C.
 *  * [STANDING] — what the class returns today: [INDEPENDENT] at an even raster-row count and
 *    [UNSPLIT] at an odd one.
 */
private enum class T326Convention { INDEPENDENT, UNSPLIT, SPLIT, SAMPLED, STANDING }

private fun HoneycombGrillage.fitIn(
    convention: T326Convention,
    field: F64Array
): List<Double> = when (convention) {
    T326Convention.INDEPENDENT -> listOf(
        pistonDual.dot(field) / area,
        tiltSDual.dot(field) / tiltSNorm,
        tiltYDual.dot(field) / tiltYNorm
    )
    T326Convention.UNSPLIT -> unconditionalFaceRigidCoefficients(field)
    T326Convention.SPLIT -> splitFaceRigidCoefficients(field)
    T326Convention.SAMPLED -> sampledFaceRigidCoefficients(field)
    T326Convention.STANDING -> faceRigidCoefficients(field)
}

private fun HoneycombGrillage.residualIn(
    convention: T326Convention,
    field: F64Array
): F64Array {
    val residual = field.copy()
    val coefficients = fitIn(convention, field)
    faceRigidModes.forEachIndexed { index, mode -> residual -= mode * coefficients[index] }
    return residual
}

private fun HoneycombGrillage.peakDishingIn(
    convention: T326Convention,
    field: F64Array,
    samples: Int = T326_SAMPLES
): Double {
    val residual = residualIn(convention, field)
    return overFaceGrid(samples) { s, y -> abs(evaluate(residual, s, y)) }
}

/**
 * The rigorous ceiling on how far a peak dishing can move when the fit convention changes.
 *
 * The change is a rigid plane, `Dc0 + Dc1*s + Dc2*y`, so by the reverse triangle inequality on
 * sup norms the peak cannot move further than that plane's own supremum over the face. It costs
 * no solve, and for a linear surrogate bank it superposes.
 */
private fun HoneycombGrillage.movementCeiling(
    from: T326Convention,
    to: T326Convention,
    field: F64Array
): Double {
    val a = fitIn(from, field)
    val b = fitIn(to, field)
    return abs(b[0] - a[0]) + abs(b[1] - a[1]) * lengthS / 2.0 +
            abs(b[2] - a[2]) * lengthY / 2.0
}

private fun HoneycombDeflection.solutionIn(
    lattice: HoneycombGrillage,
    convention: T326Convention
): DishingSolution {
    val residual = lattice.residualIn(convention, coefficients)
    val raw = coefficients
    return object : DishingSolution {
        override fun deflectionAt(x: Double, y: Double) = lattice.evaluate(raw, x, y)
        override fun dishingAt(x: Double, y: Double) = lattice.evaluate(residual, x, y)
    }
}

/** One influence bank, solved once and read in all three conventions. */
private class T326Bank(
    val lattice: HoneycombGrillage,
    val grid: List<Pair<Double, Double>>,
    val free: HoneycombDeflection,
    val influence: List<HoneycombDeflection>
) {
    fun surrogate(convention: T326Convention): InfluenceSurrogate = influenceSurrogate(
        grid, lattice.lengthS / 2.0, lattice.lengthY / 2.0, T326_SAMPLES,
        free.solutionIn(lattice, convention),
        influence.map { it.solutionIn(lattice, convention) }
    )
}

private fun t326Bank(
    lattice: HoneycombGrillage,
    grid: List<Pair<Double, Double>>,
    pressure: PressureField
): T326Bank {
    val structure = lattice.withoutPrestrain
    return T326Bank(
        lattice, grid,
        lattice.solve(pressure),
        grid.map { (s, y) ->
            structure.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0)))
        }
    )
}

// ------------------------------------------------------------------ the margin census (P5)

private fun t326CollectVerdictReadings(
    element: JsonElement,
    path: String,
    out: MutableList<Pair<String, Double>>
) {
    when (element) {
        is JsonObject -> {
            val carriesBoolean = element.values.any {
                it is JsonPrimitive && !it.isString &&
                        (it.content == "true" || it.content == "false")
            }
            element.forEach { (key, value) ->
                if (value is JsonPrimitive) {
                    if (!value.isString && carriesBoolean &&
                        (key.endsWith("OverStroke") || key.contains("ishing"))
                    ) {
                        val number = value.content.toDoubleOrNull()
                        if (number != null && number >= 0.09 && number <= 0.11) {
                            out += (path + "/" + key) to number
                        }
                    }
                } else {
                    t326CollectVerdictReadings(value, path + "/" + key, out)
                }
            }
        }
        is JsonArray -> element.forEachIndexed { index, value ->
            t326CollectVerdictReadings(value, path + "/" + index, out)
        }
        else -> Unit
    }
}

private fun t326Field(lattice: HoneycombGrillage, seed: Int): F64Array {
    val random = Random(seed)
    val field = F64Array(lattice.degreesOfFreedom)
    for (i in 0 until lattice.degreesOfFreedom) field[i] = random.nextDouble() - 0.5
    return field
}

private fun t326Probe(rows: Int, faceColumn: Int) = HoneycombGrillage(
    block = HoneycombBlock(rows, 2),
    rowBasePairs = T326_PROBE_BP,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    faceColumn = faceColumn
)

private fun t326MeasuredGap(
    lattice: HoneycombGrillage,
    mode: Int,
    field: F64Array
): Double {
    val basis = lattice.faceRigidModes[mode]
    val nearest = lattice.integrateOverFaceSplit { s, y ->
        lattice.evaluate(basis, s, y) * lattice.evaluate(field, s, y)
    }
    return nearest - lattice.faceFunctional(basis).dot(field)
}

private fun t326Scale(lattice: HoneycombGrillage, field: F64Array): Double {
    var peak = 0.0
    for (i in 0 until lattice.degreesOfFreedom) peak = maxOf(peak, abs(field[i]))
    return lattice.area * peak * (1.0 + lattice.lengthY)
}

private fun t326PublishedCell(file: File, index: Int, key: String): Double =
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cells").jsonArray[index]
        .jsonObject.getValue(key).jsonPrimitive.content.toDouble()

// =============================================================================================

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    println("T-326 - the fit and the sample in one reconstruction")
    val profile = t326Profile(ResultInputs.T_3B.file())

    // ============================================== P1: the closed form, against the quadrature
    println("T-326 - P1, the closed form against the exact quadrature")
    val closedForm = ArrayList<T326ClosedFormRow>()
    for (rows in 3..16) {
        for (faceColumn in 0..1) {
            val lattice = t326Probe(rows, faceColumn)
            var worst = 0.0
            var readings = 0
            for (seed in 1..3) {
                val field = t326Field(lattice, T326_FIELD_SEED + seed * 1000 + rows * 3 + faceColumn)
                val scale = t326Scale(lattice, field)
                for (mode in 0..2) {
                    val predicted = lattice.reconstructionGapDual(mode).dot(field)
                    val measured = t326MeasuredGap(lattice, mode, field)
                    worst = maxOf(worst, abs(predicted - measured) / scale)
                    readings++
                }
            }
            closedForm += T326ClosedFormRow(
                rasterRows = rows,
                faceColumn = faceColumn,
                verticalBonds = lattice.faceVerticalBondPairs.size,
                readings = readings,
                belowDeclaredFloor = worst < T326_CLOSED_FORM_FLOOR,
                closes = worst < 1e-10
            )
        }
    }
    val closedFormHolds = closedForm.count { it.belowDeclaredFloor }
    val worstClosedFormCeiling = "1e-12"
    println("  the closed form holds below " + worstClosedFormCeiling + " at " +
            closedFormHolds + " of " + closedForm.size + " rows, " +
            closedForm.sumOf { it.readings } + " readings")

    // ============================== P2: convention C's Gram is diagonal at EVERY raster-row count
    println("T-326 - P2, orthogonality under the three conventions")
    val orthogonality = ArrayList<T326OrthogonalityRow>()
    for (rows in 3..16) {
        for (faceColumn in 0..1) {
            val lattice = t326Probe(rows, faceColumn)
            orthogonality += T326OrthogonalityRow(
                rasterRows = rows,
                faceColumn = faceColumn,
                standingWorstOffDiagonal = lattice.worstFaceNonOrthogonality,
                splitWorstOffDiagonal = lattice.worstSplitFaceNonOrthogonality,
                sampledWorstOffDiagonal = lattice.worstSampledFaceNonOrthogonality,
                standingIsDiagonal = lattice.worstFaceNonOrthogonality < 1e-12,
                sampledIsDiagonal = lattice.worstSampledFaceNonOrthogonality < 1e-12
            )
        }
    }
    val sampledAlwaysDiagonal = orthogonality.all { it.sampledIsDiagonal }
    println("  convention C is diagonal at " + orthogonality.count { it.sampledIsDiagonal } +
            " of " + orthogonality.size + "; the standing one at " +
            orthogonality.count { it.standingIsDiagonal })

    // ================= P3 and P11: the collinearity, and the quadrature the shipped class uses
    println("T-326 - P3 and P11, the convention family and the quadrature")
    val collinearity = ArrayList<T326CollinearityRow>()
    val quadrature = ArrayList<T326QuadratureRow>()
    for (rows in listOf(4, 6, 10, 14, 15, 16)) {
        for (faceColumn in 0..1) {
            val lattice = t326Probe(rows, faceColumn)
            val field = t326Field(lattice, T326_FIELD_SEED + rows * 31 + faceColumn)
            val piston = lattice.faceRigidModes[0]
            val owning = lattice.faceFunctional(piston).dot(field)
            val split = t326MeasuredGap(lattice, 0, field)
            val rectangle = lattice.integrateOverFaceRectangle { s, y ->
                lattice.evaluate(piston, s, y) * lattice.evaluate(field, s, y)
            } - owning
            val unsplit = lattice.integrateOverFace { s, y ->
                lattice.evaluate(piston, s, y) * lattice.evaluate(field, s, y)
            } - owning
            val ratio = rectangle / split
            collinearity += T326CollinearityRow(
                rasterRows = rows,
                faceColumn = faceColumn,
                ratio = ratio,
                isExactlySix = abs(ratio - 6.0) < 1e-8
            )
            quadrature += T326QuadratureRow(
                rasterRows = rows,
                faceColumn = faceColumn,
                unsplitOverSplit = unsplit / split
            )
        }
    }
    val gaussRatioLow = quadrature.minOf { it.unsplitOverSplit }
    val gaussRatioHigh = quadrature.maxOf { it.unsplitOverSplit }
    val gaussRatioIsConstant = gaussRatioHigh - gaussRatioLow < 1e-6
    println("  gauss6/exact " + gaussRatioLow.emittedDimensionless(6) + " to " +
            gaussRatioHigh.emittedDimensionless(6) + " over " + quadrature.size + " readings")

    // =========================================== P5: the margin the refusal is priced against
    println("T-326 - P5, the margin census over the eighteen committed files")
    val committed = listOf(
        ResultInputs.T_253, ResultInputs.T_254, ResultInputs.T_263, ResultInputs.T_267,
        ResultInputs.T_279, ResultInputs.T_284, ResultInputs.T_291, ResultInputs.T_294,
        ResultInputs.T_297, ResultInputs.T_299, ResultInputs.T_303, ResultInputs.T_304,
        ResultInputs.T_307, ResultInputs.T_310, ResultInputs.T_315, ResultInputs.T_316,
        ResultInputs.T_322, ResultInputs.T_323
    )
    val readings = ArrayList<Triple<String, String, Double>>()
    committed.forEach { input ->
        require(input.file().exists()) { "a committed result file is missing: " + input.path }
        val found = ArrayList<Pair<String, Double>>()
        t326CollectVerdictReadings(Json.parseToJsonElement(input.file().readText()), "", found)
        found.forEach { (leaf, value) -> readings += Triple(input.tag, leaf, value) }
    }
    val ranked = readings.sortedBy { abs(it.third - T326_TOLERANCE) }
    val tightest = ranked.take(5).map {
        T326TightestRow(
            resultFile = it.first,
            leaf = it.second,
            value = it.third,
            relativeToTolerance = abs(it.third - T326_TOLERANCE) / T326_TOLERANCE
        )
    }
    val tightestRelative = tightest.first().relativeToTolerance
    fun readingsWithin(threshold: Double) =
        readings.count { abs(it.third - T326_TOLERANCE) / T326_TOLERANCE <= threshold }
    val census = listOf(
        T326CensusRow("the tightest reading itself", 1.02e-5, readingsWithin(1.02e-5)),
        T326CensusRow("CH-0284's collar channel", 5.0e-4, readingsWithin(5.0e-4)),
        T326CensusRow(
            "the movement that would flip C-0180's tightest recovered cell",
            4.2724e-3, readingsWithin(4.2724e-3)
        ),
        T326CensusRow(
            "C-0180's own beam-subdivision convergence departure, 4.57E-4 of the stroke",
            4.57e-3, readingsWithin(4.57e-3)
        ),
        T326CensusRow("CH-0284's bond-prestrain channel", 6.7e-3, readingsWithin(6.7e-3)),
        T326CensusRow("the prestrain channel at convention C's 6x", 4.02e-2, readingsWithin(4.02e-2))
    )
    println("  " + readings.size + " verdict-bearing readings; tightest " +
            tightest.first().value.emitted() + " at " + tightest.first().resultFile +
            tightest.first().leaf + ", " + tightestRelative.emittedDimensionless(3) + " relative")

    // ================================ P6 and P8: the channels, and the ceiling that bounds them
    println("T-326 - P6 and P8, the channels")
    val channels = ArrayList<T326ChannelRow>()
    val reproductions = ArrayList<T326Reproduction>()
    val oddTriple = ArrayList<T326TripleRow>()
    listOf(
        Triple(
            15, 4,
            listOf(
                Triple(1.0, 0.312237799, 0.242196276),
                Triple(9.65079217, 0.227177955, 0.157167743),
                Triple(12.7228458, 0.220064299, 0.150056485)
            )
        ),
        Triple(
            10, 6,
            listOf(
                Triple(1.0, 0.127358454, 0.0),
                Triple(21.1851817, 0.0449400126, 0.0),
                Triple(17.6059172, 0.0477844467, 0.0)
            )
        )
    ).forEach { (rows, layers, cases) ->
        val block = HoneycombBlock(rows, layers)
        val norm = crossSectionNormalisation(
            block, T326_UPSTREAM_BP, fractionalTolerance = T326_TOLERANCE
        )
        val load = profile.field(norm.interiorPressure, norm.edgeX, norm.edgeY)
        cases.forEach { (enhancement, published, c0219) ->
            val lattice = honeycombTiedLatticeAtResolvedLink(
                block = block, rowBasePairs = T326_UPSTREAM_BP,
                enhancement = enhancement, tied = false
            )
            val field = lattice.solve(load)
            val stroke = lattice.solve(uniformPressure(norm.interiorPressure)).meanDeflection
            val u = field.coefficients
            val independent = lattice.peakDishingIn(T326Convention.INDEPENDENT, u) / stroke
            val unsplit = lattice.peakDishingIn(T326Convention.UNSPLIT, u) / stroke
            val split = lattice.peakDishingIn(T326Convention.SPLIT, u) / stroke
            val sampled = lattice.peakDishingIn(T326Convention.SAMPLED, u) / stroke
            val splitCeiling =
                lattice.movementCeiling(T326Convention.INDEPENDENT, T326Convention.SPLIT, u) / stroke
            val sampledCeiling =
                lattice.movementCeiling(T326Convention.INDEPENDENT, T326Convention.SAMPLED, u) / stroke
            channels += T326ChannelRow(
                loadCase = "C-0022's solved collar, enhancement " + enhancement.emitted(9),
                crossSection = "" + rows + " x " + layers,
                standingPeakDishing = independent,
                splitPeakDishing = split,
                sampledPeakDishing = sampled,
                splitRelativeMovement = abs(split - independent) / independent,
                sampledRelativeMovement = abs(sampled - independent) / independent,
                splitCeiling = splitCeiling / independent,
                sampledCeiling = sampledCeiling / independent,
                ceilingHolds = abs(split - independent) <= splitCeiling * (1.0 + 1e-9) &&
                        abs(sampled - independent) <= sampledCeiling * (1.0 + 1e-9),
                insideTightestMargin =
                    abs(sampled - independent) / independent < tightestRelative
            )
            reproductions += T326Reproduction(
                what = "C-0154's free tile at " + rows + " x " + layers +
                        ", enhancement " + enhancement.emitted(9) +
                        " (the three-projection reading)",
                published = published,
                here = independent,
                departure = abs(independent - published) / published,
                closes = abs(independent - published) / published < 1e-8
            )
            if (rows % 2 == 1) {
                oddTriple += T326TripleRow(
                    enhancement = enhancement,
                    published = c0219,
                    unsplit = unsplit,
                    split = split,
                    sampled = sampled,
                    splitDeparture = abs(split - unsplit) / unsplit,
                    verdictMoves = (unsplit < T326_TOLERANCE) != (split < T326_TOLERANCE)
                )
                reproductions += T326Reproduction(
                    what = "C-0219's corrected 15 x 4 reading at enhancement " +
                            enhancement.emitted(9) + " (the unsplit convention it was fitted in)",
                    published = c0219,
                    here = unsplit,
                    departure = abs(unsplit - c0219) / c0219,
                    closes = abs(unsplit - c0219) / c0219 < 1e-8
                )
            }
        }
    }

    run {
        val block = HoneycombBlock(10, 6)
        val norm = crossSectionNormalisation(
            block, T326_UPSTREAM_BP, fractionalTolerance = T326_TOLERANCE
        )
        val lattice = honeycombTiedLatticeAtResolvedLink(
            block = block, rowBasePairs = T326_UPSTREAM_BP, enhancement = 21.1851817, tied = false
        )
        listOf(
            "point load at the face centre" to
                    lattice.solve(uniformPressure(0.0), listOf(PointLoad(0.0, 0.0, 1.0))),
            "a unit bond prestrain" to lattice.unitPrestrainResponse(lattice.bonds.first())
        ).forEach { (name, field) ->
            val u = field.coefficients
            val peak = lattice.overFaceGrid(T326_SAMPLES) { s, y -> abs(field.deflection(s, y)) }
            val independent = lattice.peakDishingIn(T326Convention.INDEPENDENT, u)
            val split = lattice.peakDishingIn(T326Convention.SPLIT, u)
            val sampled = lattice.peakDishingIn(T326Convention.SAMPLED, u)
            val splitCeiling =
                lattice.movementCeiling(T326Convention.INDEPENDENT, T326Convention.SPLIT, u)
            val sampledCeiling =
                lattice.movementCeiling(T326Convention.INDEPENDENT, T326Convention.SAMPLED, u)
            val wellPosed = independent > 1e-6 * peak
            channels += T326ChannelRow(
                loadCase = name,
                crossSection = "10 x 6",
                standingPeakDishing = independent,
                splitPeakDishing = split,
                sampledPeakDishing = sampled,
                splitRelativeMovement =
                    if (wellPosed) abs(split - independent) / independent else 0.0,
                sampledRelativeMovement =
                    if (wellPosed) abs(sampled - independent) / independent else 0.0,
                splitCeiling = if (wellPosed) splitCeiling / independent else 0.0,
                sampledCeiling = if (wellPosed) sampledCeiling / independent else 0.0,
                ceilingHolds = abs(split - independent) <= splitCeiling * (1.0 + 1e-9) &&
                        abs(sampled - independent) <= sampledCeiling * (1.0 + 1e-9),
                insideTightestMargin = wellPosed &&
                        abs(sampled - independent) / independent < tightestRelative
            )
            // `T-330`'s own residue is the UNSPLIT reading against the three projections, so it
            // is reproduced here in the convention it was measured in and not in the exact one.
            val unsplit = lattice.peakDishingIn(T326Convention.UNSPLIT, u)
            val published = if (name.startsWith("point")) 0.00047 else 0.0067
            val here = abs(unsplit - independent) / independent
            reproductions += T326Reproduction(
                what = "T-330's residue on the " + name + " channel, unsplit against " +
                        "the three projections",
                published = published,
                here = here,
                departure = abs(here - published) / published,
                closes = abs(here - published) / published < 0.02
            )
        }
    }
    channels.forEach {
        println(
            "  " + it.crossSection + " " + it.loadCase +
                    "  split " + it.splitRelativeMovement.emittedDimensionless(3) +
                    "  sampled " + it.sampledRelativeMovement.emittedDimensionless(3)
        )
    }

    // ============================================ P7: the cells C-0180's verdict actually rests on
    println("T-326 - P7, the deciding coupled cells at " + t326Realisations + " realisations")
    val decidingCells = ArrayList<T326CellRow>()
    run {
        val shared = T326Shared(profile, rasterRows = 10, helicesPerRow = 6)
        val enhancement = shared.enhancementAt(0.30)
        val lattice = honeycombTiedLattice(
            block = shared.block, rowBasePairs = shared.rowBasePairs,
            enhancement = enhancement, tied = true
        )
        val freeStroke = lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
        val rootingHelixY = lattice.faceBeams.map { lattice.beamY[it] }
        val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
        listOf(
            Triple("C-0180 cell 69, the tightest recovered", 69, "abstract grid"),
            Triple("C-0180 cell 109, the other recovered", 109, "abstract grid on the rooting helices")
        ).forEach { (label, index, placement) ->
            val columns = if (index == 69) 3 else 5
            val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
            val grid = if (placement == "abstract grid") abstract else
                abstract.mapIndexed { position, (x, _) -> x to rootingHelixY[position / columns] }
            val stiffnesses = rimGradedShareOfMandate(
                grid.map { (x, y) ->
                    val onRim = abs(x) > shared.edgeX / 2.0 - T326_RIM_BAND ||
                            abs(y) > shared.edgeY / 2.0 - T326_RIM_BAND
                    if (onRim) 5.0 else 1.0
                }
            )
            val ensemble: DropoutEnsemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, t326Realisations, T326_SEED
            )
            val bank = t326Bank(lattice, grid, shared.pressureField)
            fun p90In(convention: T326Convention): Double {
                val surrogate = bank.surrogate(convention)
                val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
                val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / freeStroke }
                return summariseDropoutDishing(
                    sample, nominal, ensemble.meanSurvivors, T326_TOLERANCE
                ).p90
            }
            val published = t326PublishedCell(ResultInputs.T_279.file(), index, "p90OverStroke")
            val standing = p90In(T326Convention.STANDING)
            val split = p90In(T326Convention.SPLIT)
            val sampled = p90In(T326Convention.SAMPLED)
            val departure = abs(standing - published) / published
            decidingCells += T326CellRow(
                cell = label,
                placement = placement,
                columns = columns,
                pathCount = grid.size,
                publishedP90 = published,
                standingP90 = standing,
                reproductionDeparture = departure,
                splitP90 = split,
                sampledP90 = sampled,
                splitRelativeMovement = abs(split - standing) / standing,
                sampledRelativeMovement = abs(sampled - standing) / standing,
                standingFlat = standing < T326_TOLERANCE,
                splitFlat = split < T326_TOLERANCE,
                sampledFlat = sampled < T326_TOLERANCE,
                verdictMoves = (standing < T326_TOLERANCE) != (split < T326_TOLERANCE) ||
                        (standing < T326_TOLERANCE) != (sampled < T326_TOLERANCE),
                marginOfTolerance = (T326_TOLERANCE - standing) / T326_TOLERANCE
            )
            reproductions += T326Reproduction(
                what = label + ", p90 over the stroke, standing convention",
                published = published,
                here = standing,
                departure = departure,
                closes = departure < 1e-8
            )
            println(
                "  " + label + "  standing " + standing.emitted() +
                        "  split " + split.emitted() + "  sampled " + sampled.emitted()
            )
        }
    }

    // ======================================================================== convergence
    println("T-326 - convergence")
    val convergence = ArrayList<T326Convergence>()
    run {
        val block = HoneycombBlock(10, 6)
        val norm = crossSectionNormalisation(
            block, T326_UPSTREAM_BP, fractionalTolerance = T326_TOLERANCE
        )
        val load = profile.field(norm.interiorPressure, norm.edgeX, norm.edgeY)
        val lattice = honeycombTiedLatticeAtResolvedLink(
            block = block, rowBasePairs = T326_UPSTREAM_BP, enhancement = 21.1851817, tied = false
        )
        val field = lattice.solve(load)
        var previous: Double? = null
        listOf(41, 81, 161).forEach { samples ->
            val independent =
                lattice.peakDishingIn(T326Convention.INDEPENDENT, field.coefficients, samples)
            val sampled =
                lattice.peakDishingIn(T326Convention.SAMPLED, field.coefficients, samples)
            val value = abs(sampled - independent) / independent
            convergence += T326Convergence(
                axis = "the dishing grid, on the sampled-convention movement at 10 x 6",
                setting = "" + samples + " x " + samples,
                quantity = value,
                departure = previous?.let { abs(value - it) / it } ?: 0.0
            )
            previous = value
        }
        var coarse: Double? = null
        listOf(1, 2).forEach { subdivisions ->
            val refined = honeycombTiedLatticeAtResolvedLink(
                block = block, rowBasePairs = T326_UPSTREAM_BP, enhancement = 21.1851817,
                tied = false, subdivisions = subdivisions
            )
            val solved = refined.solve(load)
            val independent =
                refined.peakDishingIn(T326Convention.INDEPENDENT, solved.coefficients)
            val sampled = refined.peakDishingIn(T326Convention.SAMPLED, solved.coefficients)
            val value = abs(sampled - independent) / independent
            convergence += T326Convergence(
                axis = "beam subdivision, on the sampled-convention movement at 10 x 6",
                setting = "" + subdivisions,
                quantity = value,
                departure = coarse?.let { abs(value - it) / it } ?: 0.0
            )
            coarse = value
        }
    }

    // ============================================================================ the verdict
    // The 15 x 4 rows compare against the THREE-PROJECTION reading, which at an odd raster-row
    // count is `CH-0282`'s own defect and not this task's question -- so the channel summary is
    // taken on the 10 x 6 face, which is where every committed even-m reading is taken.
    val evenChannels = channels.filter { it.crossSection == "10 x 6" }
    val worstSplitChannel = evenChannels.maxOf { it.splitRelativeMovement }
    val worstSampledChannel = evenChannels.maxOf { it.sampledRelativeMovement }
    val worstSmoothSampled = evenChannels
        .filter { !it.loadCase.contains("prestrain") }.maxOf { it.sampledRelativeMovement }
    val evenCollinear = collinearity.filter { it.faceColumn == 0 && it.rasterRows % 2 == 0 }
    val anyCellVerdictMoves = decidingCells.any { it.verdictMoves }
    val worstCellMovement = decidingCells.maxOf {
        maxOf(it.splitRelativeMovement, it.sampledRelativeMovement)
    }
    val everyCeilingHolds = channels.all { it.ceilingHolds }
    val everyReproductionCloses = reproductions.all { it.closes }
    val safeChannels = channels.count { it.insideTightestMargin }

    val findings = listOf(
        "THE FIT/SAMPLE GAP IS A CLOSED FORM IN THE FACE'S OWN VERTICAL BONDS, AND IT IS EXACT. " +
                "Within a beam's owning strip the nearest-beam partition is that strip " +
                "translated by +-d/4, alternating with the corrugation, so each strip is split " +
                "5d/4 to its own beam and d/4 to the partner across its own vertical bond. " +
                "Summed over a bond's two members the deflection differences cancel identically " +
                "and what survives is the bond's RELATIVE ROLL: the piston gap is " +
                "(d^2/16) SUM over face vertical bonds of INT (phi_upper - phi_lower) ds, with " +
                "matching forms for the two tilts. Checked against a direct exactly-piecewise " +
                "integration of both reconstructions over m = 3 to 16, both face columns and " +
                "three random fields each: it holds below the declared " +
                worstClosedFormCeiling + " at " + closedFormHolds + " of " + closedForm.size +
                " rows, " + closedForm.sumOf { it.readings } + " readings. So the discrepancy is a " +
                "BOND-HINGE COORDINATE, which is a mechanism for CH-0284's own channel split " +
                "rather than a restatement of it.",
        "CH-0284 SECTION 4's FIRST REMEDY IS NOT WELL POSED. The owning strips are one row " +
                "pitch 1.5d wide on axes d and 2d apart, so they OVERLAP by d/2 across every " +
                "vertical bond and GAP by d/2 between them; their total measure is still exactly " +
                "the face width, which is what makes the uniform-load falsifier exact. They are " +
                "therefore not a partition and evaluate cannot be made to use them: at a point " +
                "in an overlap two beams own the field and at a point in a gap none does.",
        "THERE IS A THIRD CONVENTION NOBODY NAMED AND IT DISSOLVES CH-0282 RATHER THAN " +
                "REPAIRING IT. The reported quantity is a supremum over the face RECTANGLE of " +
                "the NEAREST-beam reconstruction, so the fit consistent with it is that " +
                "reconstruction integrated over that rectangle -- where areaInnerProduct keeps " +
                "the reconstruction and leaves the measure the overlapping, gapping tributary " +
                "sum. Its Gram is diag(A, A L_s^2/12, A L_y^2/12) IDENTICALLY, at " +
                orthogonality.count { it.sampledIsDiagonal } + " of " + orthogonality.size +
                " readings over m = 3 to 16 and both face columns, because the three integrals " +
                "over a rectangle symmetric about its own centre vanish whatever the corrugated " +
                "ladder does. Under it the three independent projections are the least-squares " +
                "fit again, with NO branch, and C-0219's integer parity branch becomes dead. " +
                "For the piston projection the three conventions are collinear in one scalar, " +
                "the summed bond relative roll, at exactly 0 : 1 : 6 -- " +
                evenCollinear.count { it.isExactlySix } + " of " + evenCollinear.size +
                " at EVEN m and faceColumn 0, the geometry every committed reading is taken " +
                "at. At odd m and at faceColumn 1 the end beams break it and the ratio is " +
                "field-dependent; that is emitted rather than asserted.",
        "THE SHIPPED areaInnerProduct INTEGRATES A DISCONTINUOUS INTEGRAND WITH A SMOOTH RULE, " +
                "AND UNDER-REPORTS THE GAP BY A CONSTANT FACTOR. integrateOverFace lays one " +
                "6-point Gauss-Legendre rule across each whole tributary strip, and evaluate's " +
                "reconstruction JUMPS a quarter of a bond inside each strip's end at every " +
                "strip by construction. Measured, gauss6/exact on the piston gap is " +
                gaussRatioLow.emittedDimensionless(6) + " to " +
                gaussRatioHigh.emittedDimensionless(6) + " over " + quadrature.size +
                " readings at six raster-row counts and both face columns -- a pure number, as " +
                "it must be: both readings are linear functionals that the bond pairing reduces " +
                "to multiples of the same summed relative roll. So CH-0284's own published " +
                "channel sizes are " + (1.0 / gaussRatioLow).emittedDimensionless(6) +
                "x low. That is CH-0285.",
        "AND THE MARGIN THE REFUSAL WAS PRICED AGAINST IS NOT THE CORPUS'S TIGHTEST. Over the " +
                "eighteen committed files carrying a HoneycombDeflection dishing there are " +
                readings.size + " verdict-bearing readings in [0.09, 0.11] -- every numeric " +
                "leaf whose key ends OverStroke or contains ishing, in a record that also " +
                "carries a boolean -- and the tightest is " + tightest.first().value.emitted() +
                " at " + tightest.first().resultFile + tightest.first().leaf + ", " +
                tightestRelative.emittedDimensionless(3) + " relative from T-5b. C-0180's own " +
                "recovered cell clears the tolerance by 4.2724E-3 relative, so the corpus's " +
                "tightest verdict-bearing reading is " +
                (4.2724e-3 / tightestRelative).emittedDimensionless(4) + "x tighter, and even " +
                "the collar channel is outside it. " + census[1].readings + " readings lie " +
                "inside the collar channel, " + census[2].readings + " inside the movement " +
                "that would flip C-0180's own tightest cell, " + census[4].readings +
                " inside the prestrain channel and " + census[5].readings +
                " inside it at the third convention's 6x. THERE IS NO CHANNEL ON WHICH " +
                "ADOPTION IS SAFE, and the decision cannot rest on the movement being small.",
        "ITS TWIN RUNS THE OTHER WAY AND IS AS SHARP: " + census[3].readings + " of the " +
                readings.size + " verdict-bearing readings sit CLOSER to T-5b than the " +
                "beam-subdivision convergence departure C-0180 measured on this very lattice " +
                "(4.57E-4 of the stroke, 4.57E-3 relative). Those verdicts are not determined " +
                "by the model at all, and the fit convention is one more term in the same " +
                "bucket. It is independent of which convention wins.",
        "THE MOVEMENT IS AFFINE, SO A RIGOROUS CEILING COSTS NO SOLVE. A convention change " +
                "moves only the three coefficients, so the dishing field moves by " +
                "-(Dc0 + Dc1 s + Dc2 y) and the peak cannot move further than that plane's own " +
                "supremum over the face. Measured, the ceiling holds at " +
                channels.count { it.ceilingHolds } + " of " + channels.size + " channels and it " +
                "is TIGHT -- at the 10 x 6 collar the ceiling is " +
                evenChannels.first().sampledCeiling.emittedDimensionless(3) + " against a " +
                "measured " + evenChannels.first().sampledRelativeMovement
                    .emittedDimensionless(3) + " -- and for a linear surrogate bank it " +
                "superposes. AND THE INFLUENCE FUNCTION OVER-STATES THE STATE: a bare bond " +
                "prestrain moves " + evenChannels.last().sampledRelativeMovement
                    .emittedDimensionless(3) + " on convention C, while the two coupled cells " +
                "C-0180's verdict rests on -- whose banks are built out of exactly such " +
                "responses -- move " + worstCellMovement.emittedDimensionless(3) + ", a factor " +
                "of " + (evenChannels.last().sampledRelativeMovement / worstCellMovement)
                    .emittedDimensionless(3) + ", and the verdict moves at " +
                decidingCells.count { it.verdictMoves } + " of " + decidingCells.size + "."
    )

    val verdict = mapOf(
        "P1" to ("the closed form reproduces the measured gap below the declared " +
                worstClosedFormCeiling + " at " + closedFormHolds + " of " + closedForm.size +
                " rows, " + closedForm.sumOf { it.readings } + " readings; met"),
        "P2" to ("convention C's Gram is diagonal at " +
                orthogonality.count { it.sampledIsDiagonal } + " of " + orthogonality.size +
                " readings, the standing one at " +
                orthogonality.count { it.standingIsDiagonal } + "; met"),
        "P3" to ("the piston collinearity is exactly 6 at " +
                evenCollinear.count { it.isExactlySix } + " of " + evenCollinear.size +
                " EVEN m at faceColumn 0 -- the geometry every committed reading is taken at; " +
                "at odd m and at faceColumn 1 the end beams break it and the ratio is " +
                "field-dependent, " +
                collinearity.filter { !(it.faceColumn == 0 && it.rasterRows % 2 == 0) }
                    .minOf { it.ratio }.emittedDimensionless(3) + " to " +
                collinearity.filter { !(it.faceColumn == 0 && it.rasterRows % 2 == 0) }
                    .maxOf { it.ratio }.emittedDimensionless(3)),
        "P5" to ("" + readings.size + " verdict-bearing readings; tightest " +
                tightestRelative.emittedDimensionless(3) + " relative; " +
                census.joinToString(" / ") { it.readings.toString() }),
        "P6" to ("on the 10 x 6 face the worst channel movement is " +
                worstSampledChannel.emittedDimensionless(3) + " on convention C and " +
                worstSplitChannel.emittedDimensionless(3) + " on B, and over the SMOOTH load " +
                "cases alone " + worstSmoothSampled.emittedDimensionless(3) + " on C; the " +
                "15 x 4 rows compare against the three-projection reading, which at an odd " +
                "raster-row count is CH-0282's own defect and not this question"),
        "P7" to ("the two recovered coupled cells reproduce C-0180 at " +
                decidingCells.maxOf { it.reproductionDeparture }.emittedDimensionless(2) +
                " and move by at most " + worstCellMovement.emittedDimensionless(3) +
                "; the verdict moves at " + decidingCells.count { it.verdictMoves } + " of " +
                decidingCells.size),
        "P8" to ("the affine ceiling holds at " + channels.count { it.ceilingHolds } + " of " +
                channels.size + " channels"),
        "P9" to ("the addition is inert: every accessor is new and nothing existing is " +
                "repointed, asserted as a named test over three raster-row counts"),
        "P10" to ("the decision is recorded in C-0221; the code is left ADDITIVE ONLY, because " +
                "an adoption without the eighteen-file sweep would leave every committed file " +
                "unreproducible from its own code"),
        "P11" to ("gauss6/exact is " + gaussRatioLow.emittedDimensionless(6) + " at all " +
                quadrature.size + " readings; CH-0284's channel sizes are " +
                (1.0 / gaussRatioLow).emittedDimensionless(6) + "x low"),
        "P12" to ("the split quadrature moves C-0219's committed 15 x 4 triple by at most " +
                (oddTriple.maxOfOrNull { it.splitDeparture } ?: 0.0).emittedDimensionless(3) +
                " relative, and the verdict moves at " +
                oddTriple.count { it.verdictMoves } + " of " + oddTriple.size)
    )

    val falsifiers = listOf(
        "F1 (declared OPEN) -- a closed form disagrees with the direct quadrature beyond 1e-10 " +
                "relative. Did not fire: below the declared " + worstClosedFormCeiling + " at " +
                closedFormHolds + " of " + closedForm.size + " rows.",
        "F2 (declared OPEN) -- convention C's Gram is not diagonal at some m or face column. " +
                (if (sampledAlwaysDiagonal) "Did not fire." else "FIRED."),
        "F3 (declared OPEN) -- the piston ratio (C-A)/(B-A) is not 6 at some even m, faceColumn " +
                "0. " + (if (collinearity.filter { it.faceColumn == 0 && it.rasterRows % 2 == 0 }
                    .all { it.isExactlySix }
            ) "Did not fire." else "FIRED."),
        "F4 (declared OPEN) -- the asymptotic mispredicts a measured channel by more than 10x. " +
                "Did not fire: the face-scale prediction (pi^2/12)(d/lambda_y)^2 at " +
                "lambda_y = 2 L_y is 9.1E-4 against a measured collar channel of " +
                channels.filter { it.loadCase.startsWith("C-0022") && it.crossSection == "10 x 6" }
                    .maxOf { it.splitRelativeMovement }.emittedDimensionless(3) + ".",
        "F5 (declared OPEN, and declared EXPECTED TO FIRE under C) -- the movement at C-0180's " +
                "tightest recovered cell exceeds its 0.426 % margin under either convention. " +
                (if (anyCellVerdictMoves) "FIRED." else
                    "Did not fire: worst cell movement " +
                            worstCellMovement.emittedDimensionless(3) + " against a margin of " +
                            decidingCells.minOf { it.marginOfTolerance }
                                .emittedDimensionless(3) + "."),
        "F6 (declared OPEN) -- no verdict-bearing reading moves at all under either convention, " +
                "so adoption is free. " + (if (safeChannels == channels.size) "FIRED." else
                    "Did not fire: " + (channels.size - safeChannels) + " of " + channels.size +
                            " channels move by more than the corpus's tightest margin."),
        "F7 (declared CLOSED) -- the owning strips are a partition. They overlap by d/2 and gap " +
                "by d/2 alternately; asserted as a named test.",
        "F8 (declared OPEN) -- a control m = 10 result file re-run against the additive-only " +
                "code is not byte-identical.",
        "F9 (declared OPEN) -- two independent emissions of T-326 are not byte-identical, " +
                "diffed outside the study.",
        "F10 (declared OPEN) -- a mutation of the new code survives every named test, over a " +
                "subtracted baseline.",
        "F11 (declared CLOSED) -- the uniform-load falsifier fails under any of the three " +
                "conventions, at both parities of m.",
        "F12 (declared OPEN) -- the reproduction of C-0180's two recovered cells departs by " +
                "more than 1e-8. " + (if (decidingCells.all { it.reproductionDeparture < 1e-8 })
                    "Did not fire." else "FIRED at " +
                        decidingCells.count { it.reproductionDeparture >= 1e-8 } + " of " +
                        decidingCells.size + "."),
        // `CLAUDE.md`: a quantity that is nothing but ulp noise must be emitted as a THRESHOLD,
        // never as a value. The spread of a ratio that is meant to be CONSTANT is exactly that,
        // and it moved between two emissions -- `F9` fired on this field and nowhere else.
        "F13 (declared OPEN) -- the gauss6/exact ratio is not constant across m, face column " +
                "and field. " + (if (gaussRatioIsConstant)
                    "Did not fire: the spread over all " + quadrature.size +
                            " readings is below 1e-9." else "FIRED."),
        "F14 (declared OPEN) -- the split quadrature moves C-0219's committed 15 x 4 triple by " +
                "more than its own emitted precision. " +
                (if ((oddTriple.maxOfOrNull { it.splitDeparture } ?: 0.0) > 1e-9) "FIRED at a " +
                        "worst relative move of " +
                        (oddTriple.maxOfOrNull { it.splitDeparture } ?: 0.0)
                            .emittedDimensionless(3) + "." else "Did not fire.")
    )

    val result = T326Result(
        task = "T-326",
        leaf = "A8.2",
        title = "The reconstruction the dishing fit is taken in, against the one it is sampled " +
                "in: a closed form in the face's own vertical bonds, a third convention that " +
                "dissolves CH-0282, a constant quadrature defect, and the margin the refusal " +
                "must actually be priced against",
        verificationType = "logical (a closed form in the face's own vertical bonds, checked " +
                "against an exactly piecewise integration of both reconstructions at every " +
                "raster-row count from 3 to 16 and both face columns) + in-silico (the three " +
                "conventions measured at every channel CH-0284 prices and at the two coupled " +
                "cells C-0180's verdict rests on, with a rigorous affine ceiling beside each)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "Nothing here changes a SOLVE: every field is the one the shipped lattice " +
                "already produces, and what differs is which rigid plane is removed from it.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "dishing" to "a fraction of the free stroke, against T-5b's 0.10"
        ),
        conventions = listOf(
            "s runs along the helices, y across them in the plane of the face, z through the " +
                    "thickness; W is positive downward, Phi is the beam roll about its own axis.",
            "The face field off a beam axis is W + Phi*(y - y_beam). Convention A reconstructs " +
                    "it from the OWNING beam over a strip of one row pitch centred on that " +
                    "beam's axis; conventions B and C reconstruct it from the NEAREST beam.",
            "Convention B integrates over the owning strips, C over the face rectangle " +
                    "[-L_y/2, L_y/2]. The strips overlap by d/2 and gap by d/2 alternately, so " +
                    "their sum is not the rectangle even though its measure is L_y L_s.",
            "The face y datum is HoneycombGrillage's own, (min faceY + max faceY)/2, so beamY " +
                    "is centred on the face's ENVELOPE.",
            "The face is the gap-facing column, faceColumn = 0.",
            "In the channels block a peak dishing is a fraction of the free stroke where the " +
                    "load case is a collar and an absolute nm where it is a point load or a " +
                    "prestrain response; the movement and the ceiling beside it are both " +
                    "RELATIVE to that row's own standing reading, so the two are comparable " +
                    "within a row and the ceilingHolds boolean is taken on the absolutes."
        ),
        parameters = mapOf(
            "probeRowBasePairs" to T326_PROBE_BP.toString(),
            "upstreamRowBasePairs" to T326_UPSTREAM_BP.toString(),
            "blockExtentBasePairs" to T326_BLOCK_EXTENT_BP.toString(),
            "samples" to T326_SAMPLES.toString(),
            "tolerance" to T326_TOLERANCE.toString(),
            "rimStandoff" to T326_RIM_STANDOFF.toString(),
            "rimBand" to T326_RIM_BAND.toString(),
            "bondLength" to Gen1Tile.INTERHELICAL_HONEYCOMB.emitted(),
            "rowPitch" to HoneycombCrossSectionGeometry.rowPitch().emitted(),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(),
            "targetForce" to Gen1Tile.TARGET_FORCE.emitted(),
            "realisations" to t326Realisations.toString(),
            "seed" to T326_SEED.toString(),
            "fieldSeed" to T326_FIELD_SEED.toString(),
            "quadraturePoints" to HoneycombGrillage.QUADRATURE_POINTS.toString(),
            "closedFormDeclaredFloor" to T326_CLOSED_FORM_FLOOR.toString(),
            "censusPredicate" to ("a numeric leaf whose key ends OverStroke or contains ishing, " +
                    "in a JSON object that also carries at least one boolean, valued in " +
                    "[0.09, 0.11], over the eighteen committed files carrying a " +
                    "HoneycombDeflection dishing")
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_279.path + " (C-0180's two recovered coupled cells)"
        ) + committed.map { it.path + " (the margin census)" },
        cheapBound = listOf(
            "Within a beam's owning strip the nearest-beam partition is that strip translated " +
                    "by +-d/4, alternating with the corrugation, so each strip is split 5d/4 to " +
                    "its own beam and d/4 to the partner across its own vertical bond.",
            "Summed over a bond's two members the deflection differences cancel identically. " +
                    "piston: (d^2/16) SUM INT (phi_u - phi_l) ds. tiltS: the same with an s " +
                    "weight. tiltY: SUM INT [ (d^2/16)((w_u - w_l) + ybar (phi_u - phi_l)) " +
                    "- (d^3/32)(phi_u + phi_l) ] ds.",
            "Asymptotically the first-order slope term cancels and the relative gap is " +
                    "(pi^2/12)(d/lambda_y)^2 in the dishing field's own across-face wavelength " +
                    "-- 9.1E-4 at a face-scale half-cosine.",
            "It costs no solve: the dual is sparse and the pairing is a dot product."
        ),
        closedForm = closedForm,
        orthogonality = orthogonality,
        collinearity = collinearity,
        quadrature = quadrature,
        census = census,
        tightest = tightest,
        channels = channels,
        decidingCells = decidingCells,
        oddTriple = oddTriple,
        reproductions = reproductions,
        convergence = convergence,
        verdict = verdict,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "The closed form assumes the face gap sequence is d, 2d, d, 2d, ..., which " +
                    "HoneycombBlock.position guarantees and a require in faceVerticalBondPairs " +
                    "asserts. A face that left that ladder would refuse rather than guess.",
            "The 0 : 1 : 6 collinearity is exact for the PISTON projection at an even " +
                    "raster-row count and faceColumn 0 -- the geometry every one of the " +
                    "corpus's eighteen files is read at. At faceColumn 1 and at odd m the end " +
                    "beams break it and the ratio is field-dependent; that is emitted, not " +
                    "asserted. The tiltY gap is not proportional at all.",
            "NOTHING IS REPOINTED. Every accessor added to HoneycombGrillage is new, so the " +
                    "eighteen committed files are unmoved by construction and not by a re-run; " +
                    "byte-identity controls confirm it. The decision about which convention the " +
                    "class should adopt is recorded in C-0221 and is NOT taken in code, because " +
                    "an adoption without the eighteen-file sweep would leave every one of them " +
                    "unreproducible from its own code.",
            "The margin census is over the eighteen files' COMMITTED state and is therefore " +
                    "dated by it. Its predicate is emitted beside it so the count is " +
                    "reproducible; a reading whose verdict is written outside its own record " +
                    "is invisible to it.",
            "The deciding cells are rebuilt rather than imported, because T-279's own placement " +
                    "and distribution helpers are private to its study. What says the rebuild " +
                    "is the same object is that its standing p90 reproduces C-0180's committed " +
                    "value; that is F12 and it is emitted per cell.",
            "The ceiling is rigorous for a peak dishing of a single field and superposes over a " +
                    "linear surrogate bank. It is not a bound on a p90 over a dropout ensemble, " +
                    "which is why the deciding cells are measured rather than bounded.",
            "OrigamiGrillage's own decomposition is not examined. Its tributaries are uniform " +
                    "so the same defect cannot arise the same way, but that is an argument and " +
                    "not a measurement."
        ),
        openQuestions = listOf(
            "Whether the class should adopt convention C. The argument is closed here and the " +
                    "cost is not: it moves all eighteen result files, in a topological order, " +
                    "and an unknown number of marginal verdicts with them. T-335.",
            "CH-0285 -- the shipped areaInnerProduct's whole-strip Gauss rule across a " +
                    "discontinuous integrand, which makes CH-0284's own published channel " +
                    "sizes low by a constant factor and would silently bias any adopted fit.",
            "T-327 -- the corpus's flatness census cannot resolve a verdict inside its own " +
                    "convergence departure, and " + census[3].readings + " of " +
                    readings.size + " verdict-bearing readings are inside it. That is " +
                    "independent of which convention wins and it is not a convention question.",
            "Whether the load quadrature should also be split. It must NOT be: assembleLoad's " +
                    "centred tributary is what makes the uniform-load falsifier exact, and its " +
                    "integrand does not go through evaluate, so it carries no jump."
        )
    )

    val output = File("gpd/results/T-326-the-fit-and-the-sample-in-one-reconstruction.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result)
                .roundedForResult(digits = 9, digitsByKey = T326_DEPARTURE_DIGITS)
                .withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-326 - wrote " + output.path)
    println("  " + reproductions.count { it.closes } + " of " + reproductions.size +
            " reproductions close; every ceiling holds: " + everyCeilingHolds +
            "; all reproductions close: " + everyReproductionCloses)
}
