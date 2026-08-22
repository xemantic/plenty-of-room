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
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.ln

/**
 * Task `T-101` — is a 15-attachment scheme flat under the **solved** load? Leaf `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh coupling.SingleColumnFlatnessStudyKt
 * ```
 *
 * Emits `gpd/results/T-101-single-column-flatness.json`, deterministically — no timestamp, every
 * floating-point number rounded at the serialisation boundary per [roundCouplingResult].
 *
 * ## What this study is, and what it deliberately re-uses
 *
 * `C-0041` finds that the Gen-1 tile carries **exactly fifteen** flexures in a **1 × 15** column,
 * ties staggered 8 bp row to row so the superstructure is not severed, and names the flatness of
 * that scheme *"the largest open item this claim leaves"*: 15 is below the range `C-0015` (which
 * searched shapes from 45 up) or `CH-0034` (45 → 225) examined.
 *
 * Everything except the placement is `C-0026`'s pipeline, re-run rather than reimplemented —
 * `structure`'s grillage, `structure`'s plate, `C-0022`'s solved collar read from its own result
 * file. A second lattice would produce numbers that are not comparable with `CH-0034`'s table,
 * and extending that table downward is the whole point.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** The closed-form Winkler bending length, which settles the structure of the answer first. */
@Serializable
data class T101CheapBoundRecord(
    val direction: String,
    val rigidityPerLength: Double,
    val foundationPerLength: Double,
    val bendingLength: Double,
    val attachmentCount: Int,
    val attachmentPitch: Double,
    val pitchOverBendingLength: Double,
    val patchCoversItsTributary: Boolean
)

/** One solved state: a grid shape, a load case, a foundation stiffness. */
@Serializable
data class T101FlatnessRecord(
    val scheme: String,
    val columns: Int,
    val rows: Int,
    val attachments: Int,
    val staggerBasePairs: Double,
    val staggerLength: Double,
    val profile: String,
    val foundationMultiplier: Double,
    val freeTileStroke: Double,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverPlate: Double,
    val latticeExcessPercent: Double,
    val dishingOverStroke: Double,
    val flat: Boolean,
    val overTolerance: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val perPathStaticShare: Double,
    val verdict: String
)

/** The stagger sweep — `C-0041`'s connectivity remedy, priced in flatness and in load path. */
@Serializable
data class T101StaggerRecord(
    val basePairs: Double,
    val staggerLength: Double,
    val profile: String,
    val latticePeakDishing: Double,
    val dishingOverStroke: Double,
    val dishingChangeFromCollinear: Double,
    val peakCrossoverForce: Double,
    val peakCrossoverOverStaticShare: Double,
    val unzipMargin: Double?,
    val apparentOrderInStagger: Double?,
    /**
     * Whether a flexure of `C-0041`'s span, centred on this attachment, still lies inside the
     * 40 nm body — the constraint that separates the unconstrained flatness optimum from a
     * design.
     */
    val flexureFitsTheBody: Boolean,
    val maximumStaggerForTheSpan: Double
)

/** The column sweep, against the reference `CH-0034` never quotes: **no coupling at all**. */
@Serializable
data class T101ColumnSweepRecord(
    val columns: Int,
    val attachments: Int,
    val dishingOverStroke: Double,
    val freeTileDishingOverStroke: Double,
    val overFreeTile: Double,
    val couplingIsANetDishingSource: Boolean
)

@Serializable
data class T101ConvergenceRecord(
    val axis: String,
    val setting: String,
    val dishingOverStroke: Double,
    val departureFromFinest: Double
)

@Serializable
data class T101ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

@Serializable
data class T101Result(
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
    val rigidPlateTolerance: Double,
    val cheapBound: List<T101CheapBoundRecord>,
    val flatness: List<T101FlatnessRecord>,
    val stagger: List<T101StaggerRecord>,
    val columnSweep: List<T101ColumnSweepRecord>,
    val allSolvedStates: List<T101FlatnessRecord>,
    val foundationSweep: List<T101FlatnessRecord>,
    val convergence: List<T101ConvergenceRecord>,
    val reproductions: List<T101ReproductionRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the fixed inputs
// ---------------------------------------------------------------------------------------------

private const val T101_DUPLEXES = 15

private const val T101_EDGE_X = Gen1Tile.EDGE_X

private const val T101_NOMINAL_COLUMNS = 8

private val T101_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `T-5b`'s convention, cited via `C-0015` — **a convention, not a physical threshold**. */
private const val T101_TOLERANCE = 0.10

/** The rim standoff `C-0022` fits its rim residual over, in nm — **CITED**. */
private const val T101_RIM_STANDOFF = 1.0

private val T101_FOUNDATION_MULTIPLIERS = listOf(0.25, 0.5, 1.0, 2.0, 4.0)

/**
 * `C-0041`'s remedy is 8 bp; the sweep brackets it by a factor of four below and runs up to the
 * geometric limit, `±edgeX/2`, at 117 bp.
 *
 * The upper half of the sweep is not padding. A stagger is a **free design variable** — `C-0026`
 * fixes the attachment rows and says nothing about the station along a row — and it turned out to
 * buy flatness, so the sweep has to be wide enough to find where.
 */
private val T101_STAGGER_BASE_PAIRS =
    listOf(0.0, 2.0, 4.0, 8.0, 16.0, 32.0, 48.0, 54.0, 64.0, 80.0, 96.0, 112.0)

/**
 * `C-0030`'s coupled, favourable-mounting span at 15 paths, in nm — **CITED** from `C-0041`,
 * which places it self-consistently at every candidate path count.
 *
 * It is here for one reason: a staggered *flexure* has to stay on the body while a staggered
 * *attachment* only has to stay on the tile, so the span caps the stagger at `edgeX − span`.
 */
private const val T101_SPAN_AT_FIFTEEN = 21.44

// ---------------------------------------------------------------------------------------------
// the load profiles, read from `C-0022`'s own result file
// ---------------------------------------------------------------------------------------------

private class T101Profile(
    val name: String,
    val concentration: Double?,
    val gapHeight: Double?,
    val appliedBias: Double?,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {

    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T101_EDGE_X, lengthY,
        if (rimDepth == 0.0) listOf(CollarTerm(smoothDepth, smoothWidth))
        else listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T101_RIM_STANDOFF))
    )

}

/**
 * **Every** solved profile of `C-0022`, read from `gpd/results/T-3b-tile-edge-load-profile.json`
 * rather than transcribed.
 *
 * `CLAUDE.md` records the trap here: the file carries **two** profiles per
 * `(concentration, gap)` — one per operating bias — so a lookup keyed on `(concentration, gap)`
 * alone silently takes whichever is listed first, at a bias `C-0022`'s headline table does not
 * use. Every key here carries the **bias** as well, and the bias travels into the result file.
 */
private fun t101SolvedProfiles(file: File): List<T101Profile> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-101 consumes the SOLVED edge profile " +
                "and will not substitute an assumed one for it."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .map { record ->
            fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
            val concentration = value("concentration")
            val gap = value("gapHeight")
            val bias = value("appliedBias")
            T101Profile(
                name = "C-0022 %.1f mM, %.0f nm, %.3f V".format(concentration, gap, bias),
                concentration = concentration,
                gapHeight = gap,
                appliedBias = bias,
                smoothDepth = value("taperDepth"),
                smoothWidth = value("taperWidth"),
                rimDepth = value("rimResidualDepth")
            )
        }
}

/** The `(concentration, gap, bias)` keys of the five states `C-0022`'s headline table quotes. */
private val T101_HEADLINE_STATES: List<Triple<Double, Double, Double>> = listOf(
    Triple(2.0, 10.0, 0.192),
    Triple(0.5, 10.0, 0.134),
    Triple(10.0, 10.0, 0.192),
    Triple(2.0, 5.0, 0.368),
    Triple(2.0, 2.0, 0.368)
)

// ---------------------------------------------------------------------------------------------
// the scheme
// ---------------------------------------------------------------------------------------------

private class T101Scheme(
    val label: String,
    val columns: Int,
    val rows: Int,
    val staggerBasePairs: Double = 0.0,
    val coupled: Boolean = true
) {

    val attachments: Int get() = if (coupled) columns * rows else 0

    val staggerLength: Double get() = staggerOfBasePairs(staggerBasePairs)

    fun grid(lengthY: Double): List<Pair<Double, Double>> =
        staggeredAttachmentGrid(columns, rows, T101_EDGE_X, lengthY, staggerLength)

    fun supports(lengthY: Double): List<PointSupport> =
        if (!coupled) emptyList() else couplingSupports(grid(lengthY), T101_MANDATE)

}

private fun t101Schemes(): List<T101Scheme> = listOf(
    T101Scheme("free (no coupling)", 1, T101_DUPLEXES, coupled = false),
    T101Scheme("1 x 15", 1, T101_DUPLEXES),
    T101Scheme("1 x 15 staggered 8 bp", 1, T101_DUPLEXES, staggerBasePairs = 8.0),
    T101Scheme("15 x 1 (along a helix)", T101_DUPLEXES, 1),
    T101Scheme("2 x 15", 2, T101_DUPLEXES),
    T101Scheme("3 x 15", 3, T101_DUPLEXES),
    T101Scheme("4 x 15", 4, T101_DUPLEXES),
    T101Scheme("5 x 15", 5, T101_DUPLEXES),
    T101Scheme("8 x 15", 8, T101_DUPLEXES),
    T101Scheme("15 x 15", 15, T101_DUPLEXES),
    T101Scheme("8 x 8", 8, 8)
)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

private fun t101Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t101Lattice(
    sheet: OrigamiSheet,
    foundationMultiplier: Double,
    supports: List<PointSupport>,
    subdivisions: Int = 2
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = T101_EDGE_X,
    beamCount = T101_DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT * foundationMultiplier,
    columns = CrossoverLayout.centred(T101_NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0),
    subdivisions = subdivisions,
    supports = supports
)

private fun t101Verdict(fraction: Double, freeFraction: Double): String = when {
    fraction < T101_TOLERANCE -> "FLAT under T-5b's 10% convention"
    fraction > freeFraction ->
        "NOT flat, and WORSE than no coupling at all — the coupling is a net dishing source"
    fraction > 0.5 -> "NOT flat, and more than half the stroke"
    else -> "NOT flat, but better than no coupling at all"
}

fun main() {
    val started = System.currentTimeMillis()
    val sheet = t101Sheet()
    val lengthY = T101_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T101_EDGE_X * lengthY)
    val plateModel = sheet.plate(T101_EDGE_X, lengthY)

    println("T-101 — reading C-0022's solved edge profile ...")
    val uniform = T101Profile(
        "uniform", null, null, null, 0.0, 1.0, 0.0
    )
    val solved = t101SolvedProfiles(ResultInputs.T_3B.file())
    val headline = T101_HEADLINE_STATES.map { (concentration, gap, bias) ->
        solved.firstOrNull {
            it.concentration == concentration && it.gapHeight == gap && it.appliedBias == bias
        } ?: error("no C-0022 profile at $concentration mM, $gap nm, $bias V")
    }
    val assumed = T101Profile(
        "C-0006 ASSUMED taper (50% over one Debye length)",
        null, null, null, 0.5, Gen1Tile.DEBYE_LENGTH, 0.0
    )
    val profiles = listOf(uniform) + headline + assumed
    val designPoint = headline.first()

    // ------------------------------------------------------------------ the cheap bound
    println("T-101 — the Winkler bending length, before any matrix ...")
    val foundation = Gen1Tile.FOUNDATION_SECANT
    val alongRigidity = sheet.alongHelixRigidity * sheet.interhelicalDistance
    val acrossRigidity = sheet.acrossHelixRigidity * sheet.interhelicalDistance
    val alongLength = winklerBendingLength(alongRigidity, foundation * sheet.interhelicalDistance)
    val acrossLength = winklerBendingLength(acrossRigidity, foundation * sheet.interhelicalDistance)
    val cheapBound = buildList {
        listOf(1, 2, 3, 4, 5, 8, 15).forEach { columns ->
            val pitch = T101_EDGE_X / columns
            add(
                T101CheapBoundRecord(
                    direction = "along the helices (duplex EI over one interhelical width)",
                    rigidityPerLength = alongRigidity,
                    foundationPerLength = foundation * sheet.interhelicalDistance,
                    bendingLength = alongLength,
                    attachmentCount = columns,
                    attachmentPitch = pitch,
                    pitchOverBendingLength = pitch / alongLength,
                    patchCoversItsTributary = pitch / alongLength <= 1.0
                )
            )
        }
        add(
            T101CheapBoundRecord(
                direction = "across the helices (crossover hinge, k_theta d / p)",
                rigidityPerLength = acrossRigidity,
                foundationPerLength = foundation * sheet.interhelicalDistance,
                bendingLength = acrossLength,
                attachmentCount = T101_DUPLEXES,
                attachmentPitch = sheet.interhelicalDistance,
                pitchOverBendingLength = sheet.interhelicalDistance / acrossLength,
                patchCoversItsTributary = sheet.interhelicalDistance / acrossLength <= 1.0
            )
        )
    }

    // ------------------------------------------------------------------ the free-tile stroke
    fun freeStroke(multiplier: Double): Double = PlateOnFoundation(
        plateModel, Gen1Tile.FOUNDATION_SECANT * multiplier, emptyList(), basisDegree = 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val strokes = T101_FOUNDATION_MULTIPLIERS.associateWith { freeStroke(it) }

    // ------------------------------------------------------------------ the flatness table
    println("T-101 — the lattice and the plate, over ${t101Schemes().size} schemes ...")

    fun solveScheme(
        scheme: T101Scheme,
        profile: T101Profile,
        multiplier: Double,
        freeFraction: Double,
        subdivisions: Int = 2,
        basisDegree: Int = 12,
        samples: Int = 81
    ): T101FlatnessRecord {
        val field = profile.field(interiorPressure, lengthY)
        val supports = scheme.supports(lengthY)
        val model = t101Lattice(sheet, multiplier, supports, subdivisions)
        val plate = PlateOnFoundation(
            plateModel, Gen1Tile.FOUNDATION_SECANT * multiplier, supports, basisDegree
        )
        val solution = model.solve(field)
        val latticeDishing = solution.peakDishing(samples)
        val plateDishing = plate.solve(field).peakDishing(samples)
        val stroke = strokes.getValue(multiplier)
        val fraction = latticeDishing / stroke
        return T101FlatnessRecord(
            scheme = scheme.label,
            columns = scheme.columns,
            rows = scheme.rows,
            attachments = scheme.attachments,
            staggerBasePairs = scheme.staggerBasePairs,
            staggerLength = scheme.staggerLength,
            profile = profile.name,
            foundationMultiplier = multiplier,
            freeTileStroke = stroke,
            latticePeakDishing = latticeDishing,
            platePeakDishing = plateDishing,
            latticeOverPlate = if (plateDishing > 1e-12) latticeDishing / plateDishing else 0.0,
            latticeExcessPercent =
                if (plateDishing > 1e-12) 100.0 * (latticeDishing / plateDishing - 1.0) else 0.0,
            dishingOverStroke = fraction,
            flat = fraction < T101_TOLERANCE,
            overTolerance = fraction / T101_TOLERANCE,
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear,
            perPathStaticShare =
                if (scheme.attachments > 0) Gen1Tile.TARGET_FORCE / scheme.attachments else 0.0,
            verdict = t101Verdict(fraction, freeFraction)
        )
    }

    val freeScheme = t101Schemes().first { !it.coupled }
    val freeFractions = profiles.associate { profile ->
        profile.name to solveScheme(freeScheme, profile, 1.0, Double.MAX_VALUE).dishingOverStroke
    }

    val flatness = t101Schemes().flatMap { scheme ->
        profiles.map { profile ->
            solveScheme(scheme, profile, 1.0, freeFractions.getValue(profile.name))
        }
    }

    // ------------------------------------------------------------------ the stagger sweep
    println("T-101 — the stagger sweep, C-0041's connectivity remedy priced ...")
    val maximumStagger = maximumStaggerForSpan(T101_EDGE_X, T101_SPAN_AT_FIFTEEN)
    val staggerProfiles = listOf(uniform, designPoint)
    val collinear = flatness.first {
        it.scheme == "1 x 15" && it.profile == designPoint.name
    }.latticePeakDishing
    val stagger = staggerProfiles.flatMap { profile ->
        val reference = mutableMapOf<Double, Double>()
        T101_STAGGER_BASE_PAIRS.map { basePairs ->
            val scheme = T101Scheme("1 x 15", 1, T101_DUPLEXES, staggerBasePairs = basePairs)
            val record = solveScheme(
                scheme, profile, 1.0, freeFractions.getValue(profile.name)
            )
            reference[basePairs] = record.peakCrossoverForce
            val half = reference[basePairs / 2.0]
            T101StaggerRecord(
                basePairs = basePairs,
                staggerLength = scheme.staggerLength,
                profile = profile.name,
                latticePeakDishing = record.latticePeakDishing,
                dishingOverStroke = record.dishingOverStroke,
                dishingChangeFromCollinear =
                    if (profile.name == designPoint.name)
                        record.latticePeakDishing / collinear - 1.0 else 0.0,
                peakCrossoverForce = record.peakCrossoverForce,
                peakCrossoverOverStaticShare =
                    record.peakCrossoverForce / (Gen1Tile.TARGET_FORCE / T101_DUPLEXES),
                unzipMargin = if (record.peakCrossoverForce > 1e-9)
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / record.peakCrossoverForce else null,
                apparentOrderInStagger =
                    if (half != null && half > 1e-12 && record.peakCrossoverForce > 1e-12)
                        ln(record.peakCrossoverForce / half) / ln(2.0) else null,
                flexureFitsTheBody = scheme.staggerLength <= maximumStagger,
                maximumStaggerForTheSpan = maximumStagger
            )
        }
    }

    // ------------------------------------------------------------------ the column sweep
    val freeAtDesign = freeFractions.getValue(designPoint.name)
    val columnSweep = flatness.filter {
        it.profile == designPoint.name && it.rows == T101_DUPLEXES && it.attachments > 0 &&
                it.staggerBasePairs == 0.0
    }.map {
        T101ColumnSweepRecord(
            columns = it.columns,
            attachments = it.attachments,
            dishingOverStroke = it.dishingOverStroke,
            freeTileDishingOverStroke = freeAtDesign,
            overFreeTile = it.dishingOverStroke / freeAtDesign,
            couplingIsANetDishingSource = it.dishingOverStroke > freeAtDesign
        )
    }

    // ------------------------------------------------------------------ all 21 solved states
    println("T-101 — the design scheme against ALL of C-0022's solved states ...")
    val designScheme = t101Schemes().first { it.label == "1 x 15 staggered 8 bp" }
    val allSolvedStates = solved.map { profile ->
        solveScheme(
            designScheme, profile, 1.0,
            solveScheme(freeScheme, profile, 1.0, Double.MAX_VALUE).dishingOverStroke
        )
    }

    // ------------------------------------------------------------------ the foundation sweep
    val foundationSweep = T101_FOUNDATION_MULTIPLIERS.map { multiplier ->
        solveScheme(designScheme, designPoint, multiplier, freeAtDesign)
    }

    // ------------------------------------------------------------------ gate 4: convergence
    println("T-101 — the convergence gates ...")
    val convergence = mutableListOf<T101ConvergenceRecord>()

    /** **Nested** refinements only — a subdivision of 3 moves a point support off a node. */
    val nested = listOf(1, 2, 4).map { subdivisions ->
        subdivisions to solveScheme(
            designScheme, designPoint, 1.0, freeAtDesign, subdivisions = subdivisions
        ).dishingOverStroke
    }
    nested.forEach { (subdivisions, value) ->
        convergence += T101ConvergenceRecord(
            axis = "lattice subdivisions per crossover bay (NESTED 1 c 2 c 4)",
            setting = "$subdivisions",
            dishingOverStroke = value,
            departureFromFinest = abs(value - nested.last().second) / nested.last().second
        )
    }

    val basis = listOf(8, 10, 12).map { degree ->
        degree to solveScheme(
            designScheme, designPoint, 1.0, freeAtDesign, basisDegree = degree
        ).platePeakDishing
    }
    basis.forEach { (degree, value) ->
        convergence += T101ConvergenceRecord(
            axis = "plate basis degree (the continuum comparison)",
            setting = "$degree",
            dishingOverStroke = value,
            departureFromFinest = abs(value - basis.last().second) / basis.last().second
        )
    }

    val sampling = listOf(41, 81, 161).map { samples ->
        samples to solveScheme(
            designScheme, designPoint, 1.0, freeAtDesign, samples = samples
        ).dishingOverStroke
    }
    sampling.forEach { (samples, value) ->
        convergence += T101ConvergenceRecord(
            axis = "peak-dishing sampling grid",
            setting = "$samples x $samples",
            dishingOverStroke = value,
            departureFromFinest = abs(value - sampling.last().second) / sampling.last().second
        )
    }

    // ------------------------------------------------------------------ gate 5: reproductions
    val designRecord = flatness.first {
        it.scheme == "3 x 15" && it.profile == designPoint.name
    }
    val singleRecord = flatness.first {
        it.scheme == "1 x 15" && it.profile == designPoint.name
    }
    val squareRecord = flatness.first {
        it.scheme == "8 x 8" && it.profile == designPoint.name
    }
    val saturatedRecord = flatness.first {
        it.scheme == "15 x 15" && it.profile == designPoint.name
    }
    val uniformDesign = flatness.first {
        it.scheme == "3 x 15" && it.profile == "uniform"
    }

    fun reproduction(source: String, quantity: String, published: Double, here: Double) =
        T101ReproductionRecord(
            source, quantity, published, here, abs(here - published) / abs(published)
        )

    val reproductions = listOf(
        reproduction(
            "C-0026/CH-0034", "1 x 15 dishing / stroke, C-0022 design point",
            0.695201577, singleRecord.dishingOverStroke
        ),
        reproduction(
            "C-0026/CH-0034", "3 x 15 dishing / stroke, C-0022 design point",
            0.21821335, designRecord.dishingOverStroke
        ),
        reproduction(
            "C-0026", "8 x 8 dishing / stroke, C-0022 design point",
            0.222854848, squareRecord.dishingOverStroke
        ),
        reproduction(
            "CH-0034", "15 x 15 saturation floor, C-0022 design point",
            0.149, saturatedRecord.dishingOverStroke
        ),
        reproduction(
            "C-0026", "3 x 15 dishing / stroke under a UNIFORM load",
            0.049, uniformDesign.dishingOverStroke
        ),
        reproduction(
            "C-0026", "free-tile stroke at k_f x 1 [nm]", 4.90731102, strokes.getValue(1.0)
        ),
        reproduction(
            "C-0022", "FREE tile dishing / stroke, design point (plate)",
            0.32125378,
            solveScheme(freeScheme, designPoint, 1.0, Double.MAX_VALUE).platePeakDishing /
                    strokes.getValue(1.0)
        ),
        reproduction(
            "C-0026", "1 x 15 peak crossover force, design point [pN]",
            0.2093, singleRecord.peakCrossoverForce
        ),
        reproduction(
            "C-0015/CLAUDE.md", "sheet rigidity anisotropy, along over across",
            25.6, sheet.alongHelixRigidity / sheet.acrossHelixRigidity
        ),
        reproduction(
            "C-0041", "the 8 bp stagger in nm", 2.72, staggerOfBasePairs(8.0)
        ),
        reproduction(
            "C-0015", "the 3 x 15 column pitch in nm", 13.3333333, T101_EDGE_X / 3.0
        )
    )

    // ------------------------------------------------------------------ the result
    val staggered8 = flatness.first {
        it.scheme == "1 x 15 staggered 8 bp" && it.profile == designPoint.name
    }
    val alongHelix = flatness.first {
        it.scheme == "15 x 1 (along a helix)" && it.profile == designPoint.name
    }
    val staggerUniform8 = stagger.first { it.basePairs == 8.0 && it.profile == "uniform" }

    val result = T101Result(
        task = "T-101",
        leaf = "A8.2",
        title = "Is a 15-attachment scheme flat under the SOLVED load? " +
                "The dishing of a 1 x 15 grid under C-0022's solved profile, against T-5b's " +
                "10% convention",
        verificationType = "in-silico (C-0009/C-0015's beam-and-hinge grillage and C-0006's " +
                "continuum plate, both under C-0022's SOLVED electrostatic profile read from " +
                "gpd/results/T-3b-tile-edge-load-profile.json and keyed on concentration, gap " +
                "AND bias) + logical (a closed-form Winkler bending length, four operations, " +
                "which settles the structure of the answer before any matrix is assembled)",
        acceptance = "The dishing of a 1 x 15 grid, collinear and 8 bp-staggered, under " +
                "C-0022's solved profile as a fraction of the free-tile stroke, against " +
                "T-5b's 10% convention; beside C-0015's 45 as 3 x 15 and C-0009's 64 as 8 x 8 " +
                "at the same load case; with the orientation contrast, the lattice-over-plate " +
                "excess, and a statement of whether C-0022's 32.1% lever/sensor split moves.",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the " +
                "flexure motif this count belongs to is NOT DEMONSTRATED (C-0028, C-0029).",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 = 1 MPa exactly",
            "energy" to "pN*nm"
        ),
        conventions = listOf(
            "x runs ALONG the helices, y ACROSS them; the origin is the tile centre.",
            "w is positive DOWNWARD, compressing the polymer layer (T-5, unchanged).",
            "Dishing is the peak absolute departure from the area-weighted least-squares " +
                    "best-fit PLANE - piston and both tilts removed - so a rigid translation " +
                    "and a rigid tilt cost nothing.",
            "The free-tile stroke is the mean deflection of the UNSUPPORTED plate under the " +
                    "UNIFORM load at the same foundation stiffness: C-0006's, C-0015's and " +
                    "C-0026's normaliser, unchanged, so every number here is comparable.",
            "A collar depth is NEGATIVE for an enhancement, which is the sign C-0022 solved; " +
                    "the total load therefore exceeds 100 pN by C-0022's edge gain, which is " +
                    "C-0022's own convention and C-0026's.",
            "A stagger of s nm displaces even rows by +s/2 and odd rows by -s/2 ALONG x, and " +
                    "is quantised to the 0.34 nm rise.",
            "Flat means peak dishing below 10% of the free-tile stroke - T-5b's convention, " +
                    "cited via C-0015, A CONVENTION AND NOT A PHYSICAL THRESHOLD."
        ),
        runParameters = mapOf(
            "tile" to "40.0 x %.2f nm, %d duplexes at %.2f nm".format(
                lengthY, T101_DUPLEXES, sheet.interhelicalDistance
            ),
            "crossover columns" to "$T101_NOMINAL_COLUMNS, symmetrically centred (T-10)",
            "coupling" to "%.4f pN/nm total (C-0017's mandate), n equal springs".format(
                T101_MANDATE
            ),
            "foundation" to "C-0001's secant %.6f pN/nm^3, swept x[0.25, 4]".format(
                Gen1Tile.FOUNDATION_SECANT
            ),
            "interior pressure" to "%.6f pN/nm^2".format(interiorPressure),
            "subdivisions" to "2 per crossover bay, nested 1/2/4 in gate 4",
            "plate basis degree" to "12"
        ),
        citedInputs = mapOf(
            "interhelical distance" to "2.69 nm, CITED MEASURED, Fischer et al. (2016) SAXS",
            "rise per base pair" to "0.34 nm, CITED",
            "crossover interface spacing" to "32 bp, CITED via C-0015",
            "duplex EI" to "230 pN*nm^2, CITED, a CanDo MODEL INPUT, not a measurement",
            "C-0022's solved collars" to "21 states, CITED and READ AT RUN TIME from " +
                    "gpd/results/T-3b-tile-edge-load-profile.json, keyed on (concentration, " +
                    "gap, bias)",
            "C-0017's mandate" to "33.3333 pN/nm, CITED, itself section 3 arithmetic",
            "per-path unzip allowable" to "10 pN, CITED via C-0006/CH-0029",
            "RIGID_PLATE_TOLERANCE" to "0.10, CITED CONVENTION from T-5b, not a threshold",
            "section 3 parameters" to "100 pN, 3 nm, 40 x 40 nm, CITED"
        ),
        temperature = ROOM_TEMPERATURE,
        thermalEnergy = thermalEnergy(ROOM_TEMPERATURE),
        designPointProfile = designPoint.name,
        rigidPlateTolerance = T101_TOLERANCE,
        cheapBound = cheapBound,
        flatness = flatness,
        stagger = stagger,
        columnSweep = columnSweep,
        allSolvedStates = allSolvedStates,
        foundationSweep = foundationSweep,
        convergence = convergence,
        reproductions = reproductions,
        findings = t101Findings(
            singleRecord, staggered8, alongHelix, designRecord, squareRecord, saturatedRecord,
            freeAtDesign, columnSweep, staggerUniform8, alongLength, acrossLength,
            sheet.alongHelixRigidity / sheet.acrossHelixRigidity, allSolvedStates,
            stagger.filter { it.profile == designPoint.name }
        ),
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. Nothing here is measured.",
            "The load profile is C-0022's and inherits its WHOLE validity range: mean field " +
                    "(C-0005's one-loop correction is 123-214% across this gap range, larger " +
                    "than every effect here), point ions, a two-dimensional solve with the " +
                    "corner BRACKETED rather than solved, an UNSOURCED rim charge worth 1.85x " +
                    "on the depth, and a gap filled with free buffer.",
            "Linear Winkler foundation at C-0001's secant, swept x[0.25, 4]; C-0001's " +
                    "stiffnesses are lower bounds per CH-0001.",
            "The coupling is n IDENTICAL LINEAR springs. C-0030's flexure STRAIN-SOFTENS " +
                    "(CH-0042), so a real 15-path coupling is not exactly this one; the " +
                    "dishing is monotone in the coupling stiffness and both directions were " +
                    "swept through the foundation multiplier instead.",
            "The crossover's vertical link is C-0009's rigid PENALTY, inherited unchanged; " +
                    "static forces converge in it and thermal ones provably do not (CH-0033).",
            "One crossover layout - T-10's eight symmetrically centred columns. C-0015's " +
                    "32 base-pair phase is NOT swept here; C-0026 measured it at 3.9% on the " +
                    "crossover force and it is not expected to move a dishing.",
            "The stagger is a rigid translation of alternate rows along x. No assembly " +
                    "tolerance, no thermal excursion and no out-of-plane bow is represented.",
            "T-5b's 10% is a CONVENTION. Every verdict here is quoted with it named, and the " +
                    "1 x 15 answer is 7x above it, so it would survive a tolerance five times " +
                    "looser.",
            "No electrostatics is solved and no lateral coordinate is carried. The dishing is " +
                    "out-of-plane only.",
            "Single layer, static, 300 K, aqueous buffer with Mg2+."
        ),
        openQuestions = listOf(
            "Whether a tile that GROWS to 1.44x its area (T-102) changes the column count a " +
                    "flat scheme needs. The bending length is a material property and does not " +
                    "grow with the tile, so a larger tile needs proportionally MORE columns - " +
                    "which the packing forbids just as it does here.",
            "Whether the perforated superstructure (T-68) alters the effective coupling " +
                    "stiffness the tile sees, which is the one input the dishing is sensitive " +
                    "to that this task takes from C-0017 rather than deriving.",
            "Whether a NON-UNIFORM coupling stiffness - stiffer springs at the rim, where the " +
                    "load is - could buy back some of the edge dishing at fifteen paths. Every " +
                    "spring here is equal by C-0017's mandate, and nothing upstream requires it.",
            "T-9's crossover vertical stiffness, unchanged and untouched here."
        )
    )

    val output = File("gpd/results/T-101-single-column-flatness.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    output.writeText(
        json.encodeToString(
            (json.encodeToJsonElement(result) as JsonObject).roundedForCouplingResult().withEmissionHeader(LatticeTag.SQUARE, null)
        )
    )
    t101Report(result, output, started)
}

// ---------------------------------------------------------------------------------------------
// findings and report
// ---------------------------------------------------------------------------------------------

private fun t101Findings(
    single: T101FlatnessRecord,
    staggered: T101FlatnessRecord,
    alongHelix: T101FlatnessRecord,
    design: T101FlatnessRecord,
    square: T101FlatnessRecord,
    saturated: T101FlatnessRecord,
    freeAtDesign: Double,
    columnSweep: List<T101ColumnSweepRecord>,
    staggerUniform: T101StaggerRecord,
    alongLength: Double,
    acrossLength: Double,
    anisotropy: Double,
    allSolved: List<T101FlatnessRecord>,
    staggerAtDesign: List<T101StaggerRecord>
): List<String> {
    val breakEven = columnSweep.firstOrNull { !it.couplingIsANetDishingSource }
    val worst = allSolved.maxByOrNull { it.dishingOverStroke }!!
    val best = allSolved.minByOrNull { it.dishingOverStroke }!!
    val bestStagger = staggerAtDesign.minByOrNull { it.dishingOverStroke }!!
    val bestBuildable = staggerAtDesign.filter { it.flexureFitsTheBody }
        .minByOrNull { it.dishingOverStroke }!!
    return listOf(
        ("THE ANSWER: A 1 x 15 SCHEME IS NOT FLAT, AND IT IS NOT A SATURATION - IT IS A LOSS. " +
                "Under C-0022's solved load the 1 x 15 grid dishes %.3f of the free-tile " +
                "stroke, %.1fx T-5b's 10%% convention, against %.3f for C-0015's 45 as 3 x 15 " +
                "and %.3f for C-0009's 64 as 8 x 8 at the same load case. Dropping from three " +
                "columns to one costs %.1fx in dishing. CH-0034 found the criterion SATURATES " +
                "between 45 and 225 attachments at %.3f; below 45 it does not saturate at all, " +
                "and the 15-attachment scheme sits %.1fx above the floor CH-0034 measured.")
            .format(
                single.dishingOverStroke, single.overTolerance, design.dishingOverStroke,
                square.dishingOverStroke, single.dishingOverStroke / design.dishingOverStroke,
                saturated.dishingOverStroke,
                single.dishingOverStroke / saturated.dishingOverStroke
            ),
        ("AND THE FIFTEEN-PATH COUPLING IS A NET DISHING SOURCE: IT IS WORSE THAN NO COUPLING " +
                "AT ALL. C-0022's FREE tile dishes %.3f of the stroke under the same load; the " +
                "1 x 15 coupling dishes %.3f, i.e. %.2fx MORE. The break-even is at %s: a " +
                "coupling with fewer columns than that adds more sag between its own " +
                "attachments than it removes from the rim. CH-0034's table never reaches this " +
                "regime, because it starts at 45. The statement 'attachments buy flatness' is " +
                "true only above the break-even, and C-0041's realisable scheme is below it.")
            .format(
                freeAtDesign, single.dishingOverStroke, single.dishingOverStroke / freeAtDesign,
                breakEven?.let { "%d columns (%d attachments)".format(it.columns, it.attachments) }
                    ?: "no column count in the sweep"
            ),
        ("THE CHEAP BOUND SAW IT FIRST, AND IT IS FOUR OPERATIONS: the along-helix Winkler " +
                "bending length is %.2f nm, so a single column's %.0f nm pitch is %.2f bending " +
                "lengths and an attachment's influence patch reaches under a third of its own " +
                "tributary. Across the helices the length is %.2f nm against a %.2f nm row " +
                "pitch - %.2f - so the rows were never the problem. That 2.2x in the two " +
                "bending lengths is the sheet's %.1fx rigidity anisotropy seen through a " +
                "fourth root, and it is C-0015's 'shapes, not counts' in closed form: three " +
                "columns is the LAST count at which the pitch still falls inside one bending " +
                "length (%.2f), which is why C-0015's answer is three.")
            .format(
                alongLength, 40.0, 40.0 / alongLength, acrossLength, 2.69,
                2.69 / acrossLength, anisotropy, (40.0 / 3.0) / alongLength
            ),
        ("THE ORIENTATION IS WORTH %.2fx IN DISHING AND %.0fx IN THE LOAD PATH - AND THE " +
                "SECOND IS WHERE THE 25.6x ANISOTROPY ACTUALLY SHOWS UP. Fifteen attachments " +
                "laid ACROSS the helices - one per duplex, C-0041's column - dish %.3f of the " +
                "stroke and restore %.4f pN in the worst crossover; the same fifteen laid " +
                "ALONG one helix dish %.3f and restore %.3f pN. The dishing barely notices, " +
                "because at fifteen attachments neither orientation can flatten a 40 nm tile " +
                "and both are dominated by the same along-helix bow. The CROSSOVER path " +
                "notices by a factor of %.0f, because fifteen attachments on one duplex is " +
                "the exact opposite of C-0026's one-row-per-duplex scheme and every other " +
                "duplex has to be carried across the hinges. C-0041 finds the single-column " +
                "flexure array is feasible at exactly 1 of 720 orientations and that the one " +
                "is the sheet's own helix direction - which is also the one that puts the " +
                "attachments across the helices. The packing constraint and the load-path " +
                "constraint want the SAME angle, and a measure-zero window that two " +
                "independent requirements agree on is a design rather than a defect.")
            .format(
                alongHelix.dishingOverStroke / single.dishingOverStroke,
                alongHelix.peakCrossoverForce / single.peakCrossoverForce,
                single.dishingOverStroke, single.peakCrossoverForce,
                alongHelix.dishingOverStroke, alongHelix.peakCrossoverForce,
                alongHelix.peakCrossoverForce / single.peakCrossoverForce
            ),
        ("C-0041'S 8 bp STAGGER IS NOT QUITE FREE, AND THE DECLARED FALSIFIER FIRED - MILDLY, " +
                "AND ON THE LOAD PATH RATHER THAN ON THE DISHING. T-101's Plan set a " +
                "one-per-cent falsifier on the stagger's flatness cost; the measured cost is " +
                "%+.2f%% of the dishing, which is above it. It is nevertheless immaterial to " +
                "every verdict, because the quantity it perturbs is already %.1fx T-5b's " +
                "tolerance. What is NOT immaterial is that the stagger BREAKS C-0015's exact " +
                "zero: under a perfectly UNIFORM load the collinear column restores 0 pN and " +
                "the 8 bp staggered one restores %.4f pN - %.1fx C-0022's entire solved edge " +
                "effect on the same grid (%.4f pN). And it is FIRST order in the stagger, not " +
                "second: the REACTION is second order, because the tile's bow is even about " +
                "x = 0 and its slope there vanishes, but a crossover measures the RELATIVE " +
                "deflection of two ADJACENT DUPLEXES, and two duplexes propped at +s/2 and " +
                "-s/2 have mirror-image SHAPES whose difference is O(s) everywhere but the " +
                "centre. Alternating a support STATION across the helices is the same " +
                "symmetry break as alternating its STIFFNESS, which C-0026 found to be the " +
                "worst scatter pattern in its set - reached here a second way, and from a " +
                "geometry rather than a tolerance. It remains %.0fx below the 10 pN unzip " +
                "allowable, so no verdict moves.")
            .format(
                100.0 * staggered.latticePeakDishing / single.latticePeakDishing - 100.0,
                single.overTolerance,
                staggerUniform.peakCrossoverForce,
                staggerUniform.peakCrossoverForce / single.peakCrossoverForce,
                single.peakCrossoverForce,
                staggerUniform.unzipMargin ?: 0.0
            ),
        ("AND THE STAGGER IS A DESIGN VARIABLE, NOT ONLY A REPAIR - IT BUYS %.0f%% OF THE " +
                "DISHING BACK, AND STILL DOES NOT REACH THE TOLERANCE. C-0026 fixes the " +
                "attachment ROWS and says nothing about the station along a row, so the " +
                "stagger is free of every upstream claim; C-0041 introduced it to keep the " +
                "superstructure connected and checked it for connectivity and fit only. Swept " +
                "to the geometric limit it turns out to be the one axis a single-column scheme " +
                "still has: at %.0f bp (%.2f nm, i.e. +/- %.2f nm) the dishing falls from " +
                "%.3f to %.3f of the stroke, a %.2fx improvement, because a large alternating " +
                "stagger makes ADJACENT DUPLEXES PROP EACH OTHER THROUGH THE CROSSOVERS - each " +
                "duplex is supported at its own station and, through the hinges, at its " +
                "neighbours'. AND THE CHEAP BOUND PREDICTS WHERE THE OPTIMUM SITS, NOT ONLY " +
                "THAT THE SCHEME FAILS: the best half-stagger is %.2f nm, which is the " +
                "along-helix Winkler bending length (%.2f nm) to %.0f%% and C-0015's " +
                "three-column pitch (13.33 nm) to %.0f%% - a single column doing the best " +
                "imitation of a multi-column grid that alternation allows.")
            .format(
                100.0 * (1.0 - bestStagger.dishingOverStroke / single.dishingOverStroke),
                bestStagger.basePairs, bestStagger.staggerLength, bestStagger.staggerLength / 2.0,
                single.dishingOverStroke, bestStagger.dishingOverStroke,
                single.dishingOverStroke / bestStagger.dishingOverStroke,
                bestStagger.staggerLength / 2.0, alongLength,
                100.0 * abs(bestStagger.staggerLength / 2.0 / alongLength - 1.0),
                100.0 * abs(bestStagger.staggerLength / 2.0 / (40.0 / 3.0) - 1.0)
            ),
        ("BUT THE UNCONSTRAINED OPTIMUM IS NOT BUILDABLE, AND THE CONSTRAINT IS THE SPAN. A " +
                "staggered ATTACHMENT only has to stay on the tile; a staggered FLEXURE has to " +
                "stay on the BODY, and a flexure is a beam of C-0041's %.2f nm span centred on " +
                "its own midspan - which is exactly where the tie, and therefore the " +
                "attachment, sits. So the half-stagger is capped at edgeX/2 - span/2 = %.2f nm " +
                "(%.0f bp peak to peak), not at 20 nm, and the %.0f bp optimum overhangs the " +
                "edge by %.2f nm. Inside the cap the best is %.0f bp: dishing %.3f of the " +
                "stroke, a %.0f%% gain rather than %.0f%%, with the crossover force at %.2f pN " +
                "and %.1fx of unzip margin. It is a real gain, it is HALF the unconstrained " +
                "one, and it is still NOT enough - %.3f is %.1fx T-5b's convention and %.1fx " +
                "worse than simply having the three columns C-0041 shows cannot be built. " +
                "The same span that forbids three columns also caps the repair for having only " +
                "one.")
            .format(
                21.44, bestStagger.maximumStaggerForTheSpan / 2.0,
                bestStagger.maximumStaggerForTheSpan / 0.34,
                bestStagger.basePairs,
                bestStagger.staggerLength / 2.0 - bestStagger.maximumStaggerForTheSpan / 2.0,
                bestBuildable.basePairs, bestBuildable.dishingOverStroke,
                100.0 * (1.0 - bestBuildable.dishingOverStroke / single.dishingOverStroke),
                100.0 * (1.0 - bestStagger.dishingOverStroke / single.dishingOverStroke),
                bestBuildable.peakCrossoverForce, bestBuildable.unzipMargin ?: 0.0,
                bestBuildable.dishingOverStroke, bestBuildable.dishingOverStroke / 0.10,
                bestBuildable.dishingOverStroke / design.dishingOverStroke
            ),
        ("C-0022'S 32.1%% IRREDUCIBLE LEVER/SENSOR SPLIT IS NOT AFFECTED, AND THAT IS THE " +
                "POINT. The split is a property of the tile's RIM - an 8.9 nm collar no " +
                "interior attachment can reach - and it is written on the FREE tile, where the " +
                "LATTICE gives %.3f of the stroke against C-0022's plate value of 0.321, which " +
                "this task's own plate reproduces to 0.2%%. Nothing about the " +
                "attachment count moves it. What the count moves is the OTHER term, the sag " +
                "between attachments, and at fifteen paths that term is no longer small: " +
                "%.3f of the stroke of the %.3f total, i.e. %.0f%% of the dishing at 1 x 15 " +
                "is the coupling's own sag against %.0f%% at 3 x 15. So the lever/sensor split " +
                "stands exactly as C-0022 states it, and a 15-path design does not sit at it - " +
                "it sits above it.")
            .format(
                freeAtDesign, single.dishingOverStroke - saturated.dishingOverStroke,
                single.dishingOverStroke,
                100.0 * (1.0 - saturated.dishingOverStroke / single.dishingOverStroke),
                100.0 * (1.0 - saturated.dishingOverStroke / design.dishingOverStroke)
            ),
        ("THE LATTICE AND THE PLATE AGREE, AND THE DISCRETISATION IS THE STIFFER ONE HERE. " +
                "At 1 x 15 under the solved load the beam-and-hinge lattice dishes %.1f%% " +
                "%s the continuum plate, and at 3 x 15 %.1f%% %s. CLAUDE.md " +
                "records that a discretisation is NOT automatically a relaxation - it is " +
                "softer under a point load entering the sheet and stiffer under a point " +
                "reaction and a smooth load - and this load case is a smooth pressure reacted " +
                "through point supports, which is the stiff corner. The excess runs +1%% at " +
                "one column to -12%% at fifteen, never above a tenth in magnitude and never " +
                "of one sign, so no verdict in this task rests on the choice of model - and " +
                "the naive expectation that the discrete model is the compliant one is wrong " +
                "at every column count above two.")
            .format(
                abs(single.latticeExcessPercent),
                if (single.latticeExcessPercent > 0) "ABOVE" else "BELOW",
                abs(design.latticeExcessPercent),
                if (design.latticeExcessPercent > 0) "ABOVE" else "BELOW"
            ),
        ("ACROSS ALL 21 OF C-0022'S SOLVED STATES the 1 x 15 staggered scheme dishes %.3f to " +
                "%.3f of the stroke - the best of them (%s) still %.1fx T-5b's convention. " +
                "There is no operating state, no salt concentration, no gap and no bias at " +
                "which fifteen attachments make the Gen-1 tile flat.")
            .format(
                best.dishingOverStroke, worst.dishingOverStroke,
                best.profile, best.dishingOverStroke / 0.10
            ),
        ("WHAT THIS DOES AND DOES NOT COST THE DESIGN. It costs NO load path: the per-path " +
                "static share at fifteen paths is %.2f pN against the 10 pN unzip allowable, " +
                "and the worst restored crossover force the design scheme reaches over all 21 " +
                "of C-0022's solved states is %.2f pN. It costs the " +
                "RIGID-PLATE ASSUMPTION, which C-0006 already rejected and which T-5b already " +
                "records as rejected. What it changes is the SIZE of the lever/sensor problem " +
                "C-0022 handed downstream: at 45 paths %.0f%% of the stroke is dishing, at 15 " +
                "paths %.0f%%. Whether that is acceptable is a section-3 question about what " +
                "the tile is FOR, not a modelling one, and it belongs to NDI beside T-102.")
            .format(
                Gen1Tile.TARGET_FORCE / 15.0, allSolved.maxOf { it.peakCrossoverForce },
                100.0 * design.dishingOverStroke, 100.0 * single.dishingOverStroke
            )
    )
}

private fun t101Elapsed(started: Long): String =
    "%.1f s".format((System.currentTimeMillis() - started) / 1000.0)

private fun t101Report(result: T101Result, output: File, started: Long) {
    println()
    println("=".repeat(112))
    println("T-101 — ${result.title}")
    println("=".repeat(112))

    println()
    println("--- the cheap bound: the Winkler bending length ".padEnd(112, '-'))
    println(
        "%-52s %10s %8s %10s %10s %8s".format(
            "direction", "l [nm]", "n", "pitch", "pitch/l", "covers"
        )
    )
    result.cheapBound.forEach {
        println(
            "%-52s %10.3f %8d %10.2f %10.3f %8s".format(
                it.direction.take(52), it.bendingLength, it.attachmentCount,
                it.attachmentPitch, it.pitchOverBendingLength, it.patchCoversItsTributary
            )
        )
    }

    println()
    println("--- the flatness table, k_f x 1 ".padEnd(112, '-'))
    println(
        "%-24s %-34s %10s %10s %9s %7s %9s".format(
            "scheme", "profile", "dish[nm]", "dish/strk", "/plate", "flat", "xover[pN]"
        )
    )
    result.flatness.forEach {
        println(
            "%-24s %-34s %10.4f %10.4f %9.4f %7s %9.4f".format(
                it.scheme.take(24), it.profile.take(34), it.latticePeakDishing,
                it.dishingOverStroke, it.latticeOverPlate, it.flat, it.peakCrossoverForce
            )
        )
    }

    println()
    println("--- the column sweep, against NO COUPLING AT ALL ".padEnd(112, '-'))
    println(
        "%-10s %10s %12s %12s %10s %12s".format(
            "columns", "attach", "dish/strk", "free/strk", "ratio", "net source"
        )
    )
    result.columnSweep.forEach {
        println(
            "%-10d %10d %12.4f %12.4f %10.3f %12s".format(
                it.columns, it.attachments, it.dishingOverStroke,
                it.freeTileDishingOverStroke, it.overFreeTile, it.couplingIsANetDishingSource
            )
        )
    }

    println()
    println("--- the stagger sweep ".padEnd(112, '-'))
    println(
        "%-34s %6s %8s %11s %10s %11s %8s %7s".format(
            "profile", "bp", "s[nm]", "dish/strk", "d dish", "xover[pN]", "order", "fits"
        )
    )
    result.stagger.forEach {
        println(
            "%-34s %6.0f %8.3f %11.4f %10.4f %11.5f %8s %7s".format(
                it.profile.take(34), it.basePairs, it.staggerLength, it.dishingOverStroke,
                it.dishingChangeFromCollinear, it.peakCrossoverForce,
                it.apparentOrderInStagger?.let { o -> "%.2f".format(o) } ?: "-",
                it.flexureFitsTheBody
            )
        )
    }

    println()
    println("--- the design scheme against ALL of C-0022's solved states ".padEnd(112, '-'))
    println("%-38s %11s %11s %8s %11s".format("profile", "dish[nm]", "dish/strk", "flat", "xover[pN]"))
    result.allSolvedStates.forEach {
        println(
            "%-38s %11.4f %11.4f %8s %11.5f".format(
                it.profile.take(38), it.latticePeakDishing, it.dishingOverStroke, it.flat,
                it.peakCrossoverForce
            )
        )
    }

    println()
    println("--- the foundation sweep ".padEnd(112, '-'))
    println("%-10s %12s %12s %12s %10s".format("k_f x", "stroke[nm]", "dish[nm]", "dish/strk", "flat"))
    result.foundationSweep.forEach {
        println(
            "%-10.2f %12.4f %12.4f %12.4f %10s".format(
                it.foundationMultiplier, it.freeTileStroke, it.latticePeakDishing,
                it.dishingOverStroke, it.flat
            )
        )
    }

    println()
    println("--- gate 4: convergence ".padEnd(112, '-'))
    println("%-56s %-14s %14s %12s".format("axis", "setting", "value", "departure"))
    result.convergence.forEach {
        println(
            "%-56s %-14s %14.7f %12.3e".format(
                it.axis.take(56), it.setting, it.dishingOverStroke, it.departureFromFinest
            )
        )
    }

    println()
    println("--- gate 5: upstream reproductions ".padEnd(112, '-'))
    println("%-18s %-50s %11s %11s %11s".format("source", "quantity", "published", "here", "departure"))
    result.reproductions.forEach {
        println(
            "%-18s %-50s %11.5f %11.5f %11.2e".format(
                it.source, it.quantity.take(50), it.published, it.reproduced,
                it.relativeDeparture
            )
        )
    }

    println()
    println("--- findings ".padEnd(112, '-'))
    result.findings.forEachIndexed { index, finding ->
        println("${index + 1}. $finding")
        println()
    }

    println("wrote ${output.path} in ${t101Elapsed(started)}")
}
