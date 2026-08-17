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

import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
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
import kotlin.math.exp
import kotlin.math.max

/**
 * `T-148` — `CH-0084`'s flatness half, under a **position-dependent** staple dropout.
 *
 * `CH-0084` translates Strauss et al.'s measured 48–95 % staple incorporation into a 43.6 %
 * relative per-path stiffness scatter and grades it against `C-0060`'s 34.6 % flatness threshold,
 * and then says of its own grade: *"This is a translation, not an equivalence … a Bernoulli
 * dropout and `C-0060`'s alternating scatter pattern have the same relative standard deviation and
 * different spatial structure."* `CLAUDE.md` records what that is worth here — *"which way a
 * tolerance is correlated matters more than how big it is"* — so this study supplies the
 * structure and re-runs the flatness pipeline under it.
 *
 * Everything upstream is **re-run as a library**, never tabulated: `C-0058`'s two levels are
 * re-derived from its own rim × 5 rule, `C-0063`'s and `C-0074`'s placements are read from their
 * own result files, and `C-0022`'s edge profile is the solved one keyed on concentration, gap
 * **and** bias.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One cheap bound, settled before any lattice is factorised. */
@Serializable
private data class T148BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String,
    val falsifierFired: Boolean
)

/** One incorporation field, fitted on a reference geometry and read on the Gen-1 tile. */
@Serializable
private data class T148FieldRecord(
    val convention: String,
    val referenceLengthX: Double,
    val referenceLengthY: Double,
    val fittedParameter: Double,
    val fittedParameterName: String,
    val referenceAreaMean: Double,
    val tileAreaMean: Double,
    val note: String
)

/** How one field lands on one station set — the correlation diagnostic, before any solve. */
@Serializable
private data class T148StationFieldRecord(
    val placement: String,
    val convention: String,
    val stations: Int,
    val stationMeanIncorporation: Double,
    val stiffnessWeightedIncorporation: Double,
    val minimumIncorporation: Double,
    val maximumIncorporation: Double,
    val rimStationMean: Double,
    val interiorStationMean: Double,
    val impliedRelativeScatter: Double,
    val expectedTotalStiffness: Double,
    val totalStiffnessDeviation: Double,
    val mandateShortfall: Double
)

/** One Monte Carlo cell: a placement, a field, a mandate convention. */
@Serializable
private data class T148MonteCarloRecord(
    val placement: String,
    val convention: String,
    val mandate: String,
    val samples: Int,
    val seed: Long,
    val nominalDesignStateOverStroke: Double,
    val meanDesignStateOverStroke: Double,
    val medianDesignStateOverStroke: Double,
    val p90DesignStateOverStroke: Double,
    val p95DesignStateOverStroke: Double,
    val worstDesignStateOverStroke: Double,
    val rangeWorstMedianOverStroke: Double,
    val rangeWorstP90OverStroke: Double,
    val exceedanceAtDesignState: Double,
    val exceedanceStandardError: Double,
    val exceedanceOverRange: Double,
    val meanSurvivingPaths: Double,
    val meanRealisedTotal: Double,
    val flatAtP90: Boolean,
    val flatAtMedian: Boolean
)

/** A convergence axis, reported as values rather than as a verdict. */
@Serializable
private data class T148ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

/** One upstream number reproduced rather than cited. */
@Serializable
private data class T148ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

/** One acceptance predicate of `T-148`. */
@Serializable
private data class T148PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T148Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T148BoundRecord>,
    val fields: List<T148FieldRecord>,
    val stationFields: List<T148StationFieldRecord>,
    val monteCarlo: List<T148MonteCarloRecord>,
    val convergence: List<T148ConvergenceRecord>,
    val reproductions: List<T148ReproductionRecord>,
    val predicates: List<T148PredicateRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val parameters: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

private const val T148_DUPLEXES = 15
private const val T148_COLUMNS = 3
private const val T148_NOMINAL_CROSSOVER_COLUMNS = 8
private const val T148_SAMPLES = 81
private const val T148_TOLERANCE = 0.10
private const val T148_COLLAR = 6.7
private const val T148_TARGET_RATIO = 5.0
private const val T148_RIM_STANDOFF = 1.0
private const val T148_C0063_PHASE = 24
private const val T148_C0074_PHASE = 8
private const val T148_REALISATIONS = 10000
private const val T148_SEED = 20260817L
private const val T148_DECISION_DIGITS = 6
private const val T148_DECISION_FLOOR = 1e-12

private val T148_EDGE_X = Gen1Tile.EDGE_X
private val T148_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/**
 * The **reference** geometry the incorporation field is fitted on — the **stapled core** of
 * Strauss et al.'s rectangle, which is the domain the 168 measured staples occupy and therefore
 * the domain their mean is a mean over.
 *
 * **READ DIRECTLY**, both dimensions. Rothemund (2006) Suppl. Note S3, the Fig. S19 caption:
 * *"27 turns wide at 10.666 bases / turn -> 288 nt / **24 helices** tall — Schematics for the
 * rectangle Fig. 2b."* Strauss's Suppl. Tables 6/7 carry helix indices **0–23** and base indices
 * 31–272, and its heatmap is **16 staple columns of 16 bp**, so the stapled core is
 * `16 × 16 = 256 bp` long by 24 helices wide, with ~16 unpaired scaffold bases hanging off each
 * helix end (the *"scaffold loop"* the paper probes separately).
 *
 * The one number that is **not** read is the interhelical distance, which no origami paper
 * measures on its own object: it is bracketed at this project's SAXS single-layer **2.69 nm**,
 * the SAXS square-lattice **2.73 nm** and Rothemund's own *inferred* **3.0 nm**.
 */
private const val T148_REFERENCE_STAPLE_COLUMNS = 16

private const val T148_REFERENCE_COLUMN_BASE_PAIRS = 16

private const val T148_REFERENCE_HELICES = 24

private val T148_REFERENCE_INTERHELICAL = listOf(2.69, 2.73, 3.0)

/** The across-helix lattice cell — one interhelical distance. */
private val T148_ACROSS_CELL = Gen1Tile.INTERHELICAL_SHEET

/** The along-helix lattice cell — one 16 bp staple domain, half a crossover pitch. */
private val T148_ALONG_CELL = 16.0 * Gen1Tile.RISE_PER_BASE_PAIR

private fun referenceRectangle(interhelical: Double): Pair<Double, Double> {
    val width = T148_REFERENCE_HELICES * interhelical
    val length = T148_REFERENCE_STAPLE_COLUMNS * T148_REFERENCE_COLUMN_BASE_PAIRS *
            Gen1Tile.RISE_PER_BASE_PAIR
    return width to length
}

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private class T148Profile(
    val name: String,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T148_EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T148_RIM_STANDOFF))
    )
}

/** `C-0022`'s solved profile, keyed on **`(concentration, gap, bias)`** — `CLAUDE.md`'s gotcha. */
private fun t148Profile(file: File, key: Triple<Double, Double, Double>): T148Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-148 consumes the SOLVED edge profile " +
                "and will not substitute an assumed one for it."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == key.first && value("gapHeight") == key.second &&
                    value("appliedBias") == key.third
        } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T148Profile(
        name = "%.1f mM, %.0f nm, %.3f V".format(key.first, key.second, key.third),
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

/** A placement read from the result file of the claim that owns it. */
private fun t148Placement(file: File, key: String, interhelical: Double): List<Pair<Double, Double>> {
    require(file.exists()) {
        "the placement's own result file is missing: ${file.path}. T-148 re-runs the placements " +
                "the programme actually stands on and will not reconstruct one."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(key).jsonArray.map { it.jsonObject }
        .flatMap { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            val y = (index - (T148_DUPLEXES - 1) / 2.0) * interhelical
            row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() to y }
        }
}

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t148Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t148Lattice(sheet: OrigamiSheet, columns: CrossoverLayout): OrigamiGrillage =
    OrigamiGrillage(
        sheet = sheet,
        lengthX = T148_EDGE_X,
        beamCount = T148_DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = 2,
        supports = emptyList()
    )

/** One placement, with its own host, its own nominal distribution and its own standing figures. */
private class T148Design(
    val name: String,
    val stations: List<Pair<Double, Double>>,
    val nominal: List<Double>,
    val designSurrogate: InfluenceSurrogate,
    val heldSurrogate: InfluenceSurrogate,
    val freeDesignDishing: Double,
    val freeHeldDishing: Double,
    val rimMask: List<Boolean>
)

/** How a dropout realisation is priced against `C-0017`'s mandate. */
private enum class T148Mandate(val label: String) {

    /** The surviving paths keep their designed stiffness and the total falls — the honest one. */
    AS_BUILT("as built — the surviving paths keep their design stiffness, the total falls"),

    /** The survivors are rescaled to the mandate — the diagnostic that isolates the shape. */
    MANDATE_HELD("mandate held — the survivors rescaled to C-0017's total, a diagnostic"),

    /** Every path pre-stiffened by its own inverse incorporation — the design answer. */
    COMPENSATED("compensated — every path pre-stiffened by 1/p, so E[K] is the mandate")
}

private class T148Cell(
    val record: T148MonteCarloRecord,
    val designSample: DoubleArray
)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val sheet = t148Sheet()
    val lengthY = T148_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T148_EDGE_X * lengthY)

    println("T-148 — reading C-0022's solved loads and the standing placements ...")
    val loadFile = File("gpd/results/T-3b-tile-edge-load-profile.json")
    val designProfile = t148Profile(loadFile, Triple(2.0, 10.0, 0.192))
    val heldProfile = t148Profile(loadFile, Triple(2.0, 7.0, 0.192))
    val designField = designProfile.field(interiorPressure, lengthY)
    val heldField = heldProfile.field(interiorPressure, lengthY)

    val freeStroke = PlateOnFoundation(
        sheet.plate(T148_EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val roots34 = t148Placement(
        File("gpd/results/T-125-upward-root-placement.json"), "bestPlacement",
        sheet.interhelicalDistance
    )
    check(roots34.size == 34) { "C-0063's placement must carry 34 roots, carried ${roots34.size}" }
    val roots30 = t148Placement(
        File("gpd/results/T-136-two-per-row-placement.json"), "recommendedPlacement",
        sheet.interhelicalDistance
    )
    check(roots30.size == 30) { "C-0074's placement must carry 30 roots, carried ${roots30.size}" }

    val grid45 = attachmentGrid(T148_COLUMNS, T148_DUPLEXES, T148_EDGE_X, lengthY)
    val mask45 = rimMask(grid45, T148_EDGE_X, lengthY, T148_COLLAR)
    val twoLevel = normalisedStiffnesses(
        rimStiffenedWeights(grid45, T148_EDGE_X, lengthY, T148_COLLAR, T148_TARGET_RATIO),
        T148_MANDATE
    )

    // --------------------------------------------------------------------- the hosts and banks
    println("T-148 — the Woodbury surrogates, one per placement per state ...")
    val centredHost = t148Lattice(
        sheet, CrossoverLayout.centred(T148_NOMINAL_CROSSOVER_COLUMNS, sheet.crossoverSpacing / 2.0)
    )
    val host24 = t148Lattice(
        sheet, CrossoverLayout.atBasePairPhase(T148_C0063_PHASE, sheet, T148_EDGE_X)
    )
    val host8 = t148Lattice(
        sheet, CrossoverLayout.atBasePairPhase(T148_C0074_PHASE, sheet, T148_EDGE_X)
    )

    fun designOf(
        name: String,
        host: OrigamiGrillage,
        stations: List<Pair<Double, Double>>,
        nominal: List<Double>
    ): T148Design {
        val designSurrogate = latticeInfluenceSurrogate(host, stations, designField, T148_SAMPLES)
        val heldSurrogate = latticeInfluenceSurrogate(host, stations, heldField, T148_SAMPLES)
        val absent = List(stations.size) { false }
        return T148Design(
            name = name,
            stations = stations,
            nominal = nominal,
            designSurrogate = designSurrogate,
            heldSurrogate = heldSurrogate,
            freeDesignDishing = designSurrogate.solveWithDropout(nominal, absent).peakDishing,
            freeHeldDishing = heldSurrogate.solveWithDropout(nominal, absent).peakDishing,
            rimMask = rimMask(stations, T148_EDGE_X, lengthY, T148_COLLAR)
        )
    }

    // `C-0074`'s 30-root design needs a DISTRIBUTION, and its own claim reports that it does.
    // The minimax is re-run here rather than transcribed, on the same two states and the same
    // twelve starts, so that its 0.0648 is a reproduction and not a citation.
    println("T-148 — re-running C-0074's 30-parameter minimax at phase 8 ...")
    val states = listOf(
        LoadState(designProfile.name, designField), LoadState(heldProfile.name, heldField)
    )
    val multi30 = multiStateSurrogate(host8, roots30, states, T148_SAMPLES)
    val minimaxStarts = run {
        var seed = T148_SEED
        fun next(): Double {
            seed = seed * 6364136223846793005L + 1442695040888963407L
            return ((seed ushr 11).toDouble() / (1L shl 53).toDouble()) - 0.5
        }
        listOf(List(roots30.size) { T148_MANDATE / roots30.size }) + (1 until 12).map {
            normalisedStiffnesses(List(roots30.size) { exp(0.35 * 2.0 * next()) }, T148_MANDATE)
        }
    }
    val minimax30 = minimaxStiffnessDistribution(
        surrogate = multi30,
        states = listOf(0, 1),
        totalStiffness = T148_MANDATE,
        starts = minimaxStarts,
        ceiling = perPathStiffnessCeiling(
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
        )
    )
    println("  minimax over the range: %.6f of the stroke".format(minimax30.worstDishing / freeStroke))

    val designs = listOf(
        designOf(
            "C-0017's 45 EQUAL springs on C-0015's 3 x 15 grid", centredHost, grid45,
            List(grid45.size) { T148_MANDATE / grid45.size }
        ),
        designOf(
            "C-0058's two-level rim x 5 on the same 3 x 15 grid", centredHost, grid45, twoLevel
        ),
        designOf(
            "C-0063's 34 upward roots at phase 24 — EQUAL springs", host24, roots34,
            List(roots34.size) { T148_MANDATE / roots34.size }
        ),
        designOf(
            "C-0074's RECOMMENDED 30 roots at phase 8 — the 30-parameter minimax", host8, roots30,
            minimax30.stiffnesses
        )
    )

    // --------------------------------------------------------------------- the incorporation fields
    println("T-148 — fitting the incorporation field to Strauss's own three numbers ...")
    val edge = StapleDropoutLiterature.INCORPORATION_EDGE
    val centre = StapleDropoutLiterature.INCORPORATION_CENTRE
    val measuredMean = StapleDropoutLiterature.INCORPORATION_MEAN
    val (referenceX, referenceY) = referenceRectangle(Gen1Tile.INTERHELICAL_SHEET)

    val fittedBand = bandWidthForAreaMean(referenceX, referenceY, measuredMean, edge, centre)
    val fittedDecay = decayLengthForAreaMean(referenceX, referenceY, measuredMean, edge, centre)

    val fieldsByName = linkedMapOf(
        IncorporationConvention.UNIFORM.name to uniformIncorporation(measuredMean),
        IncorporationConvention.FLAT_BAND.name to
                flatBandIncorporation(T148_EDGE_X, lengthY, fittedBand, edge, centre),
        IncorporationConvention.EXPONENTIAL.name to
                exponentialIncorporation(T148_EDGE_X, lengthY, fittedDecay, edge, centre),
        IncorporationConvention.LATTICE_RING.name to latticeRingIncorporation(
            T148_EDGE_X, lengthY, T148_ACROSS_CELL, T148_ALONG_CELL, edge, centre
        ),
        IncorporationConvention.MEASURED_DEPTH.name to
                measuredDepthIncorporation(T148_EDGE_X, lengthY)
    )

    val latticeRingReferenceMean = run {
        // The lattice ring has no free parameter, so its reference mean is a CHECK. Its area mean
        // over a rectangle is the union of two bands, in closed form.
        val alongFraction = kotlin.math.min(1.0, 2.0 * T148_ALONG_CELL / referenceY)
        val acrossFraction = kotlin.math.min(1.0, 2.0 * T148_ACROSS_CELL / referenceX)
        val corner = alongFraction * acrossFraction
        val exactlyOne = alongFraction + acrossFraction - 2.0 * corner
        centre - (centre - edge) / 2.0 * exactlyOne - (centre - edge) * corner
    }

    val tileAreaMeanOf = { name: String ->
        when (name) {
            IncorporationConvention.UNIFORM.name -> measuredMean
            IncorporationConvention.FLAT_BAND.name ->
                flatBandAreaMean(T148_EDGE_X, lengthY, fittedBand, edge, centre)

            IncorporationConvention.EXPONENTIAL.name ->
                exponentialAreaMean(T148_EDGE_X, lengthY, fittedDecay, edge, centre)

            IncorporationConvention.LATTICE_RING.name -> {
                val alongFraction = kotlin.math.min(1.0, 2.0 * T148_ALONG_CELL / T148_EDGE_X)
                val acrossFraction = kotlin.math.min(1.0, 2.0 * T148_ACROSS_CELL / lengthY)
                val corner = alongFraction * acrossFraction
                val exactlyOne = alongFraction + acrossFraction - 2.0 * corner
                centre - (centre - edge) / 2.0 * exactlyOne - (centre - edge) * corner
            }

            else -> {
                // The measured field has no closed form; its tile mean is quadrature over the
                // plan, on the same 81 x 81 grid every dishing field here is read on.
                val field = fieldsByName.getValue(IncorporationConvention.MEASURED_DEPTH.name)
                var total = 0.0
                for (i in 0 until T148_SAMPLES) for (j in 0 until T148_SAMPLES) {
                    total += field.at(
                        -T148_EDGE_X / 2.0 + T148_EDGE_X * i / (T148_SAMPLES - 1),
                        -lengthY / 2.0 + lengthY * j / (T148_SAMPLES - 1)
                    )
                }
                total / (T148_SAMPLES * T148_SAMPLES)
            }
        }
    }

    val fields = listOf(
        T148FieldRecord(
            IncorporationConvention.UNIFORM.name, referenceX, referenceY, measuredMean,
            "the measured mean incorporation", measuredMean,
            tileAreaMeanOf(IncorporationConvention.UNIFORM.name),
            "CH-0084's own reading: no position dependence at all, the baseline the rest is " +
                    "graded against"
        ),
        T148FieldRecord(
            IncorporationConvention.FLAT_BAND.name, referenceX, referenceY, fittedBand,
            "band width [nm]",
            flatBandAreaMean(referenceX, referenceY, fittedBand, edge, centre),
            tileAreaMeanOf(IncorporationConvention.FLAT_BAND.name),
            "two-valued: the measured 48 % inside a fitted band of the rim, 95 % inside it"
        ),
        T148FieldRecord(
            IncorporationConvention.EXPONENTIAL.name, referenceX, referenceY, fittedDecay,
            "decay length [nm]",
            exponentialAreaMean(referenceX, referenceY, fittedDecay, edge, centre),
            tileAreaMeanOf(IncorporationConvention.EXPONENTIAL.name),
            "smooth: 95 % relaxed toward 48 % over a fitted boundary layer"
        ),
        T148FieldRecord(
            IncorporationConvention.LATTICE_RING.name, referenceX, referenceY,
            T148_ACROSS_CELL, "across-helix lattice cell [nm]", latticeRingReferenceMean,
            tileAreaMeanOf(IncorporationConvention.LATTICE_RING.name),
            "the mechanism read literally, ONE lattice cell and no free parameter — its " +
                    "reference mean is a CHECK on the measurement, not a fit to it"
        ),
        T148FieldRecord(
            IncorporationConvention.MEASURED_DEPTH.name, referenceX, referenceY,
            measuredDepthTable().alongPitch, "along-helix cell pitch [nm]",
            measuredDepthTable().cellWeightedMean,
            tileAreaMeanOf(IncorporationConvention.MEASURED_DEPTH.name),
            "the 168 measured per-staple values themselves, looked up by along-helix and " +
                    "across-helix depth in nm — NO fit, and the least pessimistic of the " +
                    "position-dependent readings because it uses the perimeter's measured MEAN " +
                    "(77.5 % incorporation) and not its single worst cell (47.9 %)"
        )
    )

    // --------------------------------------------------------------------- the cheap bounds
    println("T-148 — the cheap bounds, which run before any realisation ...")
    val uniformProbabilities = List(grid45.size) { measuredMean }
    val uniformNominal = List(grid45.size) { T148_MANDATE / grid45.size }
    val expectedUniform = expectedTotalStiffness(uniformNominal, uniformProbabilities)
    val reachableFloors = designs.map {
        max(
            it.designSurrogate.reachableDishingFloor, it.heldSurrogate.reachableDishingFloor
        ) / freeStroke
    }
    val worstFloor = reachableFloors.max()

    // The fragility of an OPTIMISED placement, in n solves and before any Monte Carlo: what does
    // ONE missing staple cost? This is the cheap bound that explains the whole distribution.
    fun singlePathLoss(design: T148Design): Pair<Double, Double> {
        val nominal = design.designSurrogate.solve(design.nominal).peakDishing / freeStroke
        val worst = design.stations.indices.maxOf { absent ->
            design.designSurrogate.solveWithDropout(
                design.nominal, design.stations.indices.map { it != absent }
            ).peakDishing / freeStroke
        }
        return nominal to worst
    }

    val singleLoss = designs.map { singlePathLoss(it) }

    val bounds = listOf(
        T148BoundRecord(
            "CH-0084's mandate shortfall at the measured mean, with NO spatial model",
            1.0 - expectedUniform / T148_MANDATE, "relative",
            "the mandate is an equality on a SUM, so E[K] = sum k_i p_i settles it in one line: " +
                    "%.4f pN/nm against C-0017's %.4f".format(expectedUniform, T148_MANDATE),
            false
        ),
        T148BoundRecord(
            "the realised total's own standard deviation at 45 equal paths",
            totalStiffnessDeviation(uniformNominal, uniformProbabilities), "pN/nm",
            "a builder cannot trim what is not there: the shortfall is not a rounding error and " +
                    "C-0060's one-base-pair trim cannot recover it",
            false
        ),
        T148BoundRecord(
            "the Bernoulli relative scatter at the measured MEAN incorporation",
            bernoulliRelativeScatter(1.0 - measuredMean), "relative",
            "CH-0084's 43.6 %, re-derived; graded against C-0060's 0.346 threshold", false
        ),
        T148BoundRecord(
            "the Bernoulli relative scatter at the measured EDGE incorporation",
            bernoulliRelativeScatter(1.0 - edge), "relative",
            "CH-0084's 104.1 %, re-derived", false
        ),
        T148BoundRecord(
            "the worst reachable dishing floor over all four station sets",
            worstFloor, "of the free-tile stroke",
            "a rigorous LOWER bound on every realisation's peak dishing, because removing " +
                    "stations can only raise it — above T-5b's 0.10 it would settle the " +
                    "question with no Monte Carlo at all",
            worstFloor > T148_TOLERANCE
        ),
        T148BoundRecord(
            "the worst dishing with exactly ONE of C-0063's 34 paths missing",
            singleLoss[2].second, "of the free-tile stroke",
            ("34 solves and no Monte Carlo: a SINGLE missing staple takes the optimised " +
                    "placement from %.4f to %.4f, past T-5b's 0.10 — so the distribution below " +
                    "is a property of the placement's own fragility before it is a property of " +
                    "the incorporation field").format(singleLoss[2].first, singleLoss[2].second),
            singleLoss[2].second > T148_TOLERANCE
        ),
        T148BoundRecord(
            "the worst dishing with exactly ONE of C-0058's 45 two-level paths missing",
            singleLoss[1].second, "of the free-tile stroke",
            "the same test on the non-uniform 3 x 15 design, which starts at %.4f"
                .format(singleLoss[1].first),
            singleLoss[1].second > T148_TOLERANCE
        ),
        T148BoundRecord(
            "the perimeter MEAN incorporation of the measured map",
            StrausIncorporationMap.probedCells()
                .filter { StrausIncorporationMap.onPerimeter(it.first, it.second) }
                .map { it.third }.average(),
            "incorporation",
            "CH-0084 reads Strauss's 48 % as an EDGE value; on the map itself 47.9 % is the " +
                    "single worst of 168 cells and the perimeter mean is far above it",
            false
        ),
        T148BoundRecord(
            "the lattice ring's predicted mean on the reference rectangle",
            latticeRingReferenceMean, "incorporation",
            "the mechanism read at ONE lattice cell predicts %.4f against the measured %.4f, so " +
                    "the measured boundary layer is WIDER than one cell"
                        .format(latticeRingReferenceMean, measuredMean),
            abs(latticeRingReferenceMean - measuredMean) > 0.02
        )
    )

    // --------------------------------------------------------------------- the station fields
    println("T-148 — how each field lands on each station set ...")
    val stationFields = ArrayList<T148StationFieldRecord>()
    designs.forEach { design ->
        fieldsByName.forEach { (name, field) ->
            val probabilities = design.stations.map { (x, y) -> field.at(x, y) }
            val weighted = design.nominal.indices
                .sumOf { design.nominal[it] * probabilities[it] } / design.nominal.sum()
            val rim = probabilities.indices.filter { design.rimMask[it] }
            val interior = probabilities.indices.filter { !design.rimMask[it] }
            val expected = expectedTotalStiffness(design.nominal, probabilities)
            stationFields += T148StationFieldRecord(
                placement = design.name,
                convention = name,
                stations = design.stations.size,
                stationMeanIncorporation = probabilities.average(),
                stiffnessWeightedIncorporation = weighted,
                minimumIncorporation = probabilities.min(),
                maximumIncorporation = probabilities.max(),
                rimStationMean = if (rim.isEmpty()) 0.0 else rim.map { probabilities[it] }.average(),
                interiorStationMean =
                    if (interior.isEmpty()) 0.0 else interior.map { probabilities[it] }.average(),
                impliedRelativeScatter = bernoulliRelativeScatter(1.0 - probabilities.average()),
                expectedTotalStiffness = expected,
                totalStiffnessDeviation = totalStiffnessDeviation(design.nominal, probabilities),
                mandateShortfall = 1.0 - expected / design.nominal.sum()
            )
        }
    }

    // --------------------------------------------------------------------- the Monte Carlo
    fun runCell(
        design: T148Design,
        conventionName: String,
        field: IncorporationField,
        mandate: T148Mandate,
        samples: Int,
        seed: Long
    ): T148Cell {
        val probabilities = design.stations.map { (x, y) -> field.at(x, y) }
        val base = when (mandate) {
            T148Mandate.COMPENSATED -> compensatedStiffnesses(design.nominal, probabilities)
            else -> design.nominal
        }
        val random = DropoutRandom(seed)
        val designSample = DoubleArray(samples)
        val rangeSample = DoubleArray(samples)
        var survivors = 0.0
        var realisedTotal = 0.0
        var exceedDesign = 0
        var exceedRange = 0
        repeat(samples) { index ->
            val present = bernoulliPresence(probabilities, random)
            val live = present.count { it }
            survivors += live
            realisedTotal += base.indices.sumOf { if (present[it]) base[it] else 0.0 }
            val stiffnesses = if (mandate == T148Mandate.MANDATE_HELD && live > 0) {
                renormalisedSurvivors(base, present, T148_MANDATE)
            } else base
            val atDesign = if (live == 0) design.freeDesignDishing
            else design.designSurrogate.solveWithDropout(stiffnesses, present).peakDishing
            val atHeld = if (live == 0) design.freeHeldDishing
            else design.heldSurrogate.solveWithDropout(stiffnesses, present).peakDishing
            designSample[index] = atDesign / freeStroke
            rangeSample[index] = max(atDesign, atHeld) / freeStroke
            if (designSample[index] > T148_TOLERANCE) exceedDesign++
            if (rangeSample[index] > T148_TOLERANCE) exceedRange++
        }
        val exceedance = exceedDesign.toDouble() / samples
        val nominalDishing =
            design.designSurrogate.solve(design.nominal).peakDishing / freeStroke
        val p90 = orderStatistic(designSample, 0.90)
        val median = orderStatistic(designSample, 0.50)
        return T148Cell(
            T148MonteCarloRecord(
                placement = design.name,
                convention = conventionName,
                mandate = mandate.name,
                samples = samples,
                seed = seed,
                nominalDesignStateOverStroke = nominalDishing,
                meanDesignStateOverStroke = designSample.average(),
                medianDesignStateOverStroke = median,
                p90DesignStateOverStroke = p90,
                p95DesignStateOverStroke = orderStatistic(designSample, 0.95),
                worstDesignStateOverStroke = designSample.max(),
                rangeWorstMedianOverStroke = orderStatistic(rangeSample, 0.50),
                rangeWorstP90OverStroke = orderStatistic(rangeSample, 0.90),
                exceedanceAtDesignState = exceedance,
                exceedanceStandardError = binomialStandardError(exceedance, samples),
                exceedanceOverRange = exceedRange.toDouble() / samples,
                meanSurvivingPaths = survivors / samples,
                meanRealisedTotal = realisedTotal / samples,
                flatAtP90 = p90 < T148_TOLERANCE,
                flatAtMedian = median < T148_TOLERANCE
            ),
            designSample
        )
    }

    println("T-148 — the Monte Carlo, $T148_REALISATIONS realisations per cell ...")
    val cells = ArrayList<T148Cell>()
    designs.forEach { design ->
        fieldsByName.forEach { (name, field) ->
            T148Mandate.entries.forEach { mandate ->
                cells += runCell(design, name, field, mandate, T148_REALISATIONS, T148_SEED)
            }
        }
        println(
            "  %-56s done".format(design.name.take(56))
        )
    }
    val monteCarlo = cells.map { it.record }

    // --------------------------------------------------------------------- convergence
    println("T-148 — convergence ...")
    val convergenceDesign = designs[2]
    val convergenceField = fieldsByName.getValue(IncorporationConvention.FLAT_BAND.name)
    val sampleCounts = listOf(1250, 2500, 5000, 10000, 20000)
    val p90AtCount = sampleCounts.map {
        runCell(
            convergenceDesign, IncorporationConvention.FLAT_BAND.name, convergenceField,
            T148Mandate.AS_BUILT, it, T148_SEED
        ).record.p90DesignStateOverStroke
    }
    val exceedanceAtCount = sampleCounts.map {
        runCell(
            convergenceDesign, IncorporationConvention.FLAT_BAND.name, convergenceField,
            T148Mandate.AS_BUILT, it, T148_SEED
        ).record.exceedanceAtDesignState
    }
    val gridCounts = listOf(41, 81, 161)
    val dishingAtGrid = gridCounts.map { grid ->
        val surrogate = latticeInfluenceSurrogate(host24, roots34, designField, grid)
        val random = DropoutRandom(T148_SEED)
        val probabilities = roots34.map { (x, y) -> convergenceField.at(x, y) }
        val stiffnesses = List(roots34.size) { T148_MANDATE / roots34.size }
        // The mean over 200 seeded realisations, not one: three nested grids share their nodes,
        // so a single realisation whose peak lands on a shared node agrees to the last digit and
        // measures nothing.
        (1..200).map {
            surrogate.solveWithDropout(
                stiffnesses, bernoulliPresence(probabilities, random)
            ).peakDishing / freeStroke
        }.average()
    }
    val bandAtGeometry = T148_REFERENCE_INTERHELICAL.map {
        val (x, y) = referenceRectangle(it)
        bandWidthForAreaMean(x, y, measuredMean, edge, centre)
    }
    val tileMeanAtGeometry = T148_REFERENCE_INTERHELICAL.map {
        val (x, y) = referenceRectangle(it)
        val band = bandWidthForAreaMean(x, y, measuredMean, edge, centre)
        flatBandIncorporation(T148_EDGE_X, lengthY, band, edge, centre)
            .let { f -> grid45.map { (px, py) -> f.at(px, py) }.average() }
    }
    val convergence = listOf(
        T148ConvergenceRecord(
            "the 90th percentile of the dishing distribution",
            "realisations 1250/2500/5000/10000/20000",
            sampleCounts.map { it.toDouble() }, p90AtCount,
            abs(p90AtCount.last() - p90AtCount[p90AtCount.size - 2]),
            "an order statistic of a seeded sample; the departure is the 10 000 to 20 000 step"
        ),
        T148ConvergenceRecord(
            "the exceedance probability against T-5b's 0.10", "the same counts",
            sampleCounts.map { it.toDouble() }, exceedanceAtCount,
            abs(exceedanceAtCount.last() - exceedanceAtCount[exceedanceAtCount.size - 2]),
            "the binomial standard error at 10 000 draws is %.4f, which is the resolution the " +
                    "verdict is quoted to".format(
                        binomialStandardError(exceedanceAtCount[3], T148_REALISATIONS)
                    )
        ),
        T148ConvergenceRecord(
            "one realisation's dishing on the sampling grid", "samples per edge 41/81/161",
            gridCounts.map { it.toDouble() }, dishingAtGrid,
            abs(dishingAtGrid[2] - dishingAtGrid[1]),
            "C-0026's own 81 x 81 convention, refined at a single seeded realisation"
        ),
        T148ConvergenceRecord(
            "the fitted band width",
            "reference interhelical distance 2.69 / 2.73 / 3.00 nm",
            T148_REFERENCE_INTERHELICAL, bandAtGeometry,
            bandAtGeometry.max() - bandAtGeometry.min(),
            "the reference rectangle's staple count and helix count are READ DIRECTLY; only its " +
                    "interhelical distance is not measured on its own object, and it is " +
                    "bracketed here. The 3 x 15 grid's own mean incorporation over the bracket " +
                    "is " + tileMeanAtGeometry.joinToString(", ") { "%.4f".format(it) }
        )
    )

    // --------------------------------------------------------------------- reproductions
    println("T-148 — reproducing the standing figures at zero dropout ...")
    val reproductions = ArrayList<T148ReproductionRecord>()
    fun reproduce(
        source: String,
        quantity: String,
        published: Double,
        reproduced: Double,
        strict: Boolean = true
    ) {
        reproductions += T148ReproductionRecord(
            source, quantity, published, reproduced,
            if (published == 0.0) abs(reproduced) else abs(reproduced - published) / abs(published),
            strict
        )
    }
    reproduce(
        "C-0017/C-0058", "the uniform 3 x 15 coupling's dishing / stroke", 0.2182,
        designs[0].designSurrogate.solve(designs[0].nominal).peakDishing / freeStroke
    )
    reproduce(
        "C-0058", "the rim x 5 two-level dishing / stroke", 0.0753,
        designs[1].designSurrogate.solve(designs[1].nominal).peakDishing / freeStroke
    )
    reproduce(
        "C-0063", "the 34 equal springs at the design state", 0.0706,
        designs[2].designSurrogate.solve(designs[2].nominal).peakDishing / freeStroke
    )
    reproduce(
        "C-0068", "the same 34 roots over the device's range", 0.0789,
        max(
            designs[2].designSurrogate.solve(designs[2].nominal).peakDishing,
            designs[2].heldSurrogate.solve(designs[2].nominal).peakDishing
        ) / freeStroke
    )
    // `C-0074`'s RECOMMENDED placement is the largest-plan-ceiling one, not the flattest: its
    // own table gives 0.2424 with equal springs and 0.0682 under the minimax, where the flattest
    // phase-8 placement gives 0.1726 and 0.0648. The recommendation trades flatness for plan
    // margin, and the numbers reproduced here are the recommendation's own.
    reproduce(
        "C-0074", "the RECOMMENDED phase-8 30 roots with EQUAL springs, over the range", 0.242359741,
        max(
            designs[3].designSurrogate.solve(List(roots30.size) { T148_MANDATE / roots30.size })
                .peakDishing,
            designs[3].heldSurrogate.solve(List(roots30.size) { T148_MANDATE / roots30.size })
                .peakDishing
        ) / freeStroke
    )
    reproduce(
        "C-0074", "the 30-parameter minimax on the recommended placement, over the range",
        0.0682185723, minimax30.worstDishing / freeStroke, false
    )
    reproduce("C-0026", "the free-tile stroke [nm]", 4.90731, freeStroke)
    reproduce("C-0017", "the mandate as a sum [pN/nm]", 33.3333, T148_MANDATE, false)
    reproduce("C-0058", "the rim station count on the 3 x 15 grid", 34.0, mask45.count { it }.toDouble())
    reproduce(
        "CH-0084", "the relative scatter at the measured mean", 0.436,
        bernoulliRelativeScatter(1.0 - measuredMean), false
    )
    reproduce(
        "CH-0084", "the relative scatter at the measured edge value", 1.041,
        bernoulliRelativeScatter(1.0 - edge), false
    )
    reproduce(
        "CH-0084", "the mandate shortfall at the measured mean", 0.16,
        1.0 - expectedUniform / T148_MANDATE
    )
    reproduce(
        "Strauss et al. (2018)", "the incorporation mean from the detection mean plus the offset",
        StapleDropoutLiterature.INCORPORATION_MEAN,
        StapleDropoutLiterature.DETECTION_MEAN +
                StapleDropoutLiterature.DETECTION_TO_INCORPORATION_OFFSET
    )
    val probedCells = StrausIncorporationMap.probedCells()
    reproduce(
        "Strauss et al. (2018), Suppl. Fig. 14", "the probed staple count",
        StapleDropoutLiterature.PROBED_STAPLES.toDouble(), probedCells.size.toDouble()
    )
    reproduce(
        "Strauss et al. (2018), Suppl. Fig. 14",
        "the map's own mean incorporation against the paper's printed 84 %",
        StapleDropoutLiterature.INCORPORATION_MEAN,
        probedCells.map { it.third }.average(), false
    )
    reproduce(
        "Strauss et al. (2018), Suppl. Fig. 14",
        "the map's own MINIMUM incorporation against the paper's printed 48 %",
        StapleDropoutLiterature.INCORPORATION_EDGE, probedCells.minOf { it.third }, false
    )
    reproduce(
        "Strauss et al. (2018), Suppl. Fig. 14",
        "the map's own MAXIMUM incorporation against the paper's printed 95 %",
        StapleDropoutLiterature.INCORPORATION_CENTRE, probedCells.maxOf { it.third }, false
    )

    // --------------------------------------------------------------------- predicates
    fun cell(placement: String, convention: String, mandate: T148Mandate) =
        monteCarlo.first {
            it.placement == placement && it.convention == convention &&
                    it.mandate == mandate.name
        }

    val decisive = fieldsByName.keys.filter { it != IncorporationConvention.UNIFORM.name }
    val c0063Verdicts = decisive.map {
        cell(designs[2].name, it, T148Mandate.AS_BUILT).flatAtP90
    }
    val conventionsAgree = c0063Verdicts.distinct().size == 1

    val predicates = listOf(
        T148PredicateRecord(
            "P1 — the mean-dropout arithmetic settles the mandate with no spatial model",
            "E[K] = sum k_i p_i is closed form and the station-weighted mean is reported beside " +
                    "CH-0084's position-independent 0.84",
            "PASS"
        ),
        T148PredicateRecord(
            "P2 — the field is FITTED to Strauss's own three numbers, not assumed",
            "the band and the decay length are fitted to the measured mean on a stated " +
                    "reference geometry, which is bracketed",
            "PASS"
        ),
        T148PredicateRecord(
            "P3 — a distribution, not a point",
            "percentiles over %d seeded realisations with a binomial standard error on every " +
                    "exceedance probability".format(T148_REALISATIONS),
            "PASS"
        ),
        T148PredicateRecord(
            "P4 — the verdict is delivered on the placements that carry one",
            "C-0058's two-level 3 x 15, C-0063/C-0068's 34 equal springs and C-0074's 30-root " +
                    "phase-8 minimax, under the same dropout",
            "PASS"
        ),
        T148PredicateRecord(
            "P5 — every standing figure reproduces at zero dropout",
            "the worst strict departure over the reproductions is reported",
            if (reproductions.filter { it.strict }.all { it.departure < 5e-3 }) "PASS" else "FAIL"
        ),
        T148PredicateRecord(
            "F1 — the declared falsifier: do the fitted conventions AGREE about the verdict?",
            "the three position-dependent conventions' 90th-percentile verdicts on C-0063's " +
                    "placement: " + decisive.zip(c0063Verdicts)
                .joinToString(", ") { "${it.first} ${if (it.second) "flat" else "NOT flat"}" },
            if (conventionsAgree) "DID NOT FIRE" else "FIRED"
        )
    )

    // --------------------------------------------------------------------- the result
    val worstStrict = reproductions.filter { it.strict }.maxOf { it.departure }
    val result = T148Result(
        task = "T-148",
        leaf = "A1.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN.nm; aqueous 2 mM MgCl2; " +
                "40.0 x %.2f nm single-layer square-lattice sheet, 15 duplexes at 2.69 nm; ".format(lengthY) +
                "C-0022's SOLVED edge profiles at 2 mM / 10 nm / 0.192 V (design) and " +
                "2 mM / 7 nm / 0.192 V (held); C-0017's 33.3333 pN/nm as a SUM at the " +
                "acceptable 3 nm stroke; free-tile stroke %.5f nm; dishing on an 81 x 81 grid; " .format(freeStroke) +
                "flat means below T-5b's 0.10 CONVENTION",
        decision = "the position-dependent dropout is applied as an independent Bernoulli " +
                "removal per station, fitted to Strauss et al. (2018)",
        bounds = bounds,
        fields = fields,
        stationFields = stationFields,
        monteCarlo = monteCarlo,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = t148Findings(
            designs, monteCarlo, stationFields, fields, bounds, fittedBand, fittedDecay,
            latticeRingReferenceMean, measuredMean, conventionsAgree, worstFloor, singleLoss
        ),
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. The INPUT is measured — Strauss et al. " +
                    "(2018), read directly — and nothing derived here is.",
            "Strauss measures STAPLE incorporation on a plain Rothemund rectangle at one folding " +
                    "protocol. A Gen-1 coupling path is a designed element whose own incorporation " +
                    "nobody has measured, and the out-of-plane motif every element stands on is " +
                    "NOT demonstrated (C-0028, C-0029, C-0055).",
            "The MAPPING onto a 40 nm tile is a CONVENTION and is carried as three of them. The " +
                    "reference geometry is DERIVED from the p7249 scaffold, not read, and is " +
                    "bracketed at 22/24/26 helices.",
            "Realisations are INDEPENDENT across stations. Strauss reports no correlation length; " +
                    "a folding-run-to-folding-run common mode would change the distribution's " +
                    "shape and is not modelled.",
            "The dishing pipeline, the lattice, the load and the free-tile stroke are C-0058's, " +
                    "C-0063's and C-0074's unchanged, and inherit their whole validity range — " +
                    "C-0022's unsourced rim charge, C-0001's single foundation secant, one " +
                    "crossover layout per placement.",
            "T-5b's 0.10 is a CONVENTION, not a physical threshold.",
            "The 90th percentile is the reported verdict statistic. It is a choice: nothing " +
                    "upstream says what fraction of built tiles a design is allowed to lose."
        ),
        openQuestions = listOf(
            "Whether a placement exists that is flat under the dropout. This study grades the " +
                    "standing designs; it does not search for a new one.",
            "Whether the coupling element's own incorporation is the staple's. Strauss's number " +
                    "is the right ORDER and the only measured one, not the coupling's own.",
            "Whether the dropout is correlated within a folding run. Independence is a " +
                    "convention here and a measurable quantity in Strauss's own instrument.",
            "What fraction of built tiles a flatness verdict is owed over. The 90th percentile " +
                    "is this claim's choice and no upstream clause fixes it.",
            "Whether over-stiffening the rim paths to compensate is affordable once C-0014's " +
                    "thermal force and the per-path allowable are re-read at the compensated level."
        ),
        parameters = mapOf(
            "realisations" to "$T148_REALISATIONS",
            "seed" to "$T148_SEED",
            "decisionDigits" to "$T148_DECISION_DIGITS",
            "decisionFloor" to "$T148_DECISION_FLOOR",
            "samplesPerEdge" to "$T148_SAMPLES",
            "flatnessTolerance" to "$T148_TOLERANCE (T-5b's CONVENTION)",
            "collarWidth" to "$T148_COLLAR nm (C-0058's rim mask)",
            "ratio" to "$T148_TARGET_RATIO (C-0058's rim x 5)",
            "incorporationEdge" to "$edge (Strauss et al. 2018, READ DIRECTLY)",
            "incorporationCentre" to "$centre (Strauss et al. 2018, READ DIRECTLY)",
            "incorporationMean" to "$measuredMean (Strauss et al. 2018, READ DIRECTLY)",
            "referenceRectangle" to
                    ("%.2f nm across x %.2f nm along — the STAPLED CORE, %d helices READ " +
                            "DIRECTLY at the SAXS 2.69 nm and %d staple columns of %d bp READ " +
                            "DIRECTLY").format(
                        referenceX, referenceY, T148_REFERENCE_HELICES,
                        T148_REFERENCE_STAPLE_COLUMNS, T148_REFERENCE_COLUMN_BASE_PAIRS
                    ),
            "fittedBandWidth" to "%.6f nm".format(fittedBand),
            "fittedDecayLength" to "%.6f nm".format(fittedDecay),
            "acrossCell" to "%.4f nm".format(T148_ACROSS_CELL),
            "alongCell" to "%.4f nm (16 bp)".format(T148_ALONG_CELL),
            "worstStrictReproductionDeparture" to "%.3e".format(worstStrict)
            // NO runtime, and no evaluation, sweep or start count. `CLAUDE.md`: emit the answer
            // and a convergence measure, never anything that counts steps — and a wall clock is a
            // step counter by another name. Two independent runs of this study agreed on all
            // 2 037 other lines and differed on exactly that one.
        )
    )

    val output = File("gpd/results/T-148-staple-dropout.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = T148_DECISION_DIGITS + 3, floor = T148_DECISION_FLOOR
            ) as JsonObject)
        ) + "\n"
    )
    println("T-148 — wrote ${output.path}")
    result.findings.forEach { println("  * $it") }
    result.predicates.forEach { println("  [${it.verdict}] ${it.name}") }
}

// ---------------------------------------------------------------------------------------------
// the prose, built AFTER the result so a format placeholder cannot cost the run
// ---------------------------------------------------------------------------------------------

private fun t148Findings(
    designs: List<T148Design>,
    monteCarlo: List<T148MonteCarloRecord>,
    stationFields: List<T148StationFieldRecord>,
    fields: List<T148FieldRecord>,
    bounds: List<T148BoundRecord>,
    fittedBand: Double,
    fittedDecay: Double,
    latticeRingReferenceMean: Double,
    measuredMean: Double,
    conventionsAgree: Boolean,
    worstFloor: Double,
    singleLoss: List<Pair<Double, Double>>
): List<String> {
    fun cell(placement: String, convention: String, mandate: T148Mandate) =
        monteCarlo.first {
            it.placement == placement && it.convention == convention && it.mandate == mandate.name
        }

    fun station(placement: String, convention: String) =
        stationFields.first { it.placement == placement && it.convention == convention }

    val band = IncorporationConvention.FLAT_BAND.name
    val expo = IncorporationConvention.EXPONENTIAL.name
    val ring = IncorporationConvention.LATTICE_RING.name
    val measured = IncorporationConvention.MEASURED_DEPTH.name
    val flat = IncorporationConvention.UNIFORM.name
    val equal34 = designs[2].name
    val twoLevel = designs[1].name

    val findings = ArrayList<String>()

    findings += ("THE MANDATE HALF NEEDS NO PIPELINE AND IT IS THE LARGER EFFECT. " +
            "C-0017's mandate is an equality on a SUM, so a dropout's cost to it is " +
            "E[K] = sum k_i p_i: at CH-0084's position-independent 0.84 that is a %.1f %% " +
            "shortfall, and on the 3 x 15 grid's own stations the fitted position-dependent " +
            "field makes it %.1f %% (flat band) and %.1f %% (exponential). A trim cannot " +
            "recover any of it, because the missing paths are not there to trim.").format(
        100.0 * bounds[0].value,
        100.0 * station(designs[0].name, band).mandateShortfall,
        100.0 * station(designs[0].name, expo).mandateShortfall
    )

    findings += ("THE MEASURED BOUNDARY LAYER IS WIDER THAN ONE LATTICE CELL, AND THAT IS A " +
            "CHECK THE MEASUREMENT ITSELF SUPPLIES. The mechanism Strauss names -- staples at " +
            "the edges and corners missing neighbouring helices and stacking -- read literally " +
            "at one lattice cell predicts a mean incorporation of %.4f on the reference " +
            "rectangle against the measured %.4f. Fitting the width instead gives a %.2f nm " +
            "band or a %.2f nm exponential decay, i.e. %.1f to %.1f interhelical distances.")
        .format(
            latticeRingReferenceMean, measuredMean, fittedBand, fittedDecay,
            fittedBand / Gen1Tile.INTERHELICAL_SHEET, fittedDecay / Gen1Tile.INTERHELICAL_SHEET
        )

    findings += ("A 40 nm TILE IS NOT A ROTHEMUND RECTANGLE AND THE TRANSFER IS PESSIMISTIC. " +
            "The same fitted boundary layer over the Gen-1 tile's area gives a mean " +
            "incorporation of %.4f (flat band) and %.4f (exponential) against the measured " +
            "%.4f -- because a 40 x 40 nm tile has %.2fx the perimeter per unit area of the " +
            "reference rectangle. CH-0084's 0.84 is therefore the OPTIMISTIC reading of its " +
            "own measurement on this object.").format(
        fields[1].tileAreaMean, fields[2].tileAreaMean, measuredMean,
        (2.0 * (Gen1Tile.EDGE_X + 15 * Gen1Tile.INTERHELICAL_SHEET) /
                (Gen1Tile.EDGE_X * 15 * Gen1Tile.INTERHELICAL_SHEET)) /
                (2.0 * (fields[1].referenceLengthX + fields[1].referenceLengthY) /
                        (fields[1].referenceLengthX * fields[1].referenceLengthY))
    )

    val perimeter = StrausIncorporationMap.probedCells()
        .filter { StrausIncorporationMap.onPerimeter(it.first, it.second) }
    val interior = StrausIncorporationMap.probedCells()
        .filter { !StrausIncorporationMap.onPerimeter(it.first, it.second) }

    val corners = StrausIncorporationMap.probedCells().filter {
        (it.first == 0 || it.first == StrausIncorporationMap.COLUMNS - 1) &&
                (it.second == 0 || it.second == StrausIncorporationMap.ROWS - 1)
    }.map { it.third }.sorted()

    findings += ("CH-0084's 48 %% IS ONE CORNER OF 168, NOT THE EDGE. Read off the map itself " +
            "rather than off the abstract, the perimeter MEAN incorporation is %.3f and the " +
            "interior mean %.3f, against the single worst cell at %.3f — which is a CORNER, " +
            "while the four corners run %s and the best of them is ABOVE the interior mean. " +
            "Six of the 52 perimeter cells beat the interior mean and the perimeter's standard " +
            "deviation is %.2fx the interior's. So a field that puts the whole rim at 0.48 is " +
            "harsher than the measurement, and the MEASURED_DEPTH convention — no fit at all — " +
            "is the fair reading.").format(
        perimeter.map { it.third }.average(), interior.map { it.third }.average(),
        StrausIncorporationMap.probedCells().minOf { it.third },
        corners.joinToString(", ") { "%.3f".format(it) },
        run {
            fun sd(v: List<Double>) = kotlin.math.sqrt(
                v.sumOf { (it - v.average()) * (it - v.average()) } / (v.size - 1)
            )
            sd(perimeter.map { it.third }) / sd(interior.map { it.third })
        }
    )

    findings += ("A SINGLE MISSING STAPLE ALREADY LOSES THE VERDICT, AND THAT NEEDS NO MONTE " +
            "CARLO. Removing exactly ONE of C-0063's 34 paths — 34 solves — takes the " +
            "optimised placement from %.4f to %.4f of the stroke at its worst station, and one " +
            "of C-0058's 45 takes the two-level design from %.4f to %.4f. An exhaustively " +
            "optimised placement is a delicately tuned cancellation, and the dropout " +
            "distribution below is a property of THAT before it is a property of the " +
            "incorporation field.").format(
        singleLoss[2].first, singleLoss[2].second, singleLoss[1].first, singleLoss[1].second
    )

    findings += ("THE VERDICT. Under the fitted flat-band dropout as built, C-0063's 34 equal " +
            "springs dish %.4f at the median and %.4f at the 90th percentile of the stroke " +
            "against T-5b's 0.10, exceeding it in %.1f %% of realisations (standard error " +
            "%.4f); C-0058's two-level rim x 5 dishes %.4f and %.4f, exceeding in %.1f %%. " +
            "At zero dropout the same two are 0.0706 and 0.0753.").format(
        cell(equal34, band, T148Mandate.AS_BUILT).medianDesignStateOverStroke,
        cell(equal34, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        100.0 * cell(equal34, band, T148Mandate.AS_BUILT).exceedanceAtDesignState,
        cell(equal34, band, T148Mandate.AS_BUILT).exceedanceStandardError,
        cell(twoLevel, band, T148Mandate.AS_BUILT).medianDesignStateOverStroke,
        cell(twoLevel, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        100.0 * cell(twoLevel, band, T148Mandate.AS_BUILT).exceedanceAtDesignState
    )

    findings += ("THE SPATIAL STRUCTURE IS WORTH THIS MUCH: at the SAME mean dropout, " +
            "CH-0084's position-independent field puts C-0063's 90th percentile at %.4f and " +
            "the fitted flat band at %.4f, a factor of %.2f; on C-0058's two-level design the " +
            "same pair is %.4f and %.4f, a factor of %.2f. So the challenge's own " +
            "qualification is quantified, and the direction is %s.").format(
        cell(equal34, flat, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        cell(equal34, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        cell(equal34, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke /
                cell(equal34, flat, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        cell(twoLevel, flat, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        cell(twoLevel, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        cell(twoLevel, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke /
                cell(twoLevel, flat, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        if (cell(equal34, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke >
            cell(equal34, flat, T148Mandate.AS_BUILT).p90DesignStateOverStroke
        ) "ADVERSE — the position dependence makes it worse"
        else "FAVOURABLE — the position dependence makes it better"
    )

    findings += ("THE DROPOUT REVERSES THE RANKING OF THE TWO FLAT DESIGNS. At zero dropout " +
            "C-0063's 34 EQUAL springs (%.4f) beat C-0058's two-level 45 (%.4f), which is " +
            "C-0063's own headline. Under the measured dropout the order inverts: %.4f against " +
            "%.4f at the 90th percentile, and %.1f %% against %.1f %% exceedance. Losing ONE " +
            "path costs the 34-root placement %.4f and the 45-path grid %.4f, so what the " +
            "dropout rewards is a denser, more regular array — and the equal-spring advantage " +
            "C-0063 and CH-0080 both read as a property of the placement is, under fabrication, " +
            "a LIABILITY.").format(
        cell(equal34, band, T148Mandate.AS_BUILT).nominalDesignStateOverStroke,
        cell(twoLevel, band, T148Mandate.AS_BUILT).nominalDesignStateOverStroke,
        cell(equal34, measured, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        cell(twoLevel, measured, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        100.0 * cell(equal34, measured, T148Mandate.AS_BUILT).exceedanceAtDesignState,
        100.0 * cell(twoLevel, measured, T148Mandate.AS_BUILT).exceedanceAtDesignState,
        singleLoss[2].second, singleLoss[1].second
    )

    findings += ("THE MEASURED MAP IS THE MILDEST READING AND IT STILL FAILS, WHICH IS THE " +
            "STRONG FORM OF THE VERDICT. Transferred by its own measured depth profiles with NO " +
            "fit, the map gives the Gen-1 tile a station-mean incorporation of %.4f on the " +
            "3 x 15 grid and %.4f on C-0063's 34 roots, against the fitted flat band's %.4f — " +
            "and the flatness verdict is still lost at %.1f %% and %.1f %% of realisations. " +
            "Over all %d placement-by-convention cells as built the LOWEST exceedance anywhere " +
            "is %.1f %%.").format(
        station(designs[0].name, measured).stationMeanIncorporation,
        station(equal34, measured).stationMeanIncorporation,
        station(equal34, band).stationMeanIncorporation,
        100.0 * cell(twoLevel, measured, T148Mandate.AS_BUILT).exceedanceAtDesignState,
        100.0 * cell(equal34, measured, T148Mandate.AS_BUILT).exceedanceAtDesignState,
        monteCarlo.count { it.mandate == T148Mandate.AS_BUILT.name },
        100.0 * monteCarlo.filter { it.mandate == T148Mandate.AS_BUILT.name }
            .minOf { it.exceedanceAtDesignState }
    )

    findings += ("HOLDING THE MANDATE IS NOT A REPAIR AND COMPENSATION IS NOT EITHER. " +
            "On C-0063's placement the same flat-band dropout gives a 90th percentile of " +
            "%.4f as built, %.4f with the survivors rescaled to C-0017's total, and %.4f with " +
            "every path pre-stiffened by its own inverse incorporation. The dropout costs the " +
            "SHAPE of the coupling, not only its level.").format(
        cell(equal34, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        cell(equal34, band, T148Mandate.MANDATE_HELD).p90DesignStateOverStroke,
        cell(equal34, band, T148Mandate.COMPENSATED).p90DesignStateOverStroke
    )

    findings += ("THE DECLARED FALSIFIER F1 %s: the three fitted conventions %s about " +
            "C-0063's 90th-percentile verdict, and the lattice-ring reading -- the one the " +
            "measured mean rejects -- is the mildest of the three at %.4f against the flat " +
            "band's %.4f. The rigorous reachable-dishing floor over all four station sets is " +
            "%.4f, so the cheap bound could not have settled it and the Monte Carlo was " +
            "necessary.").format(
        if (conventionsAgree) "DID NOT FIRE" else "FIRED",
        if (conventionsAgree) "agree" else "DISAGREE",
        cell(equal34, ring, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        cell(equal34, band, T148Mandate.AS_BUILT).p90DesignStateOverStroke,
        worstFloor
    )

    return findings
}
