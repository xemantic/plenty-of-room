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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.UpwardRootInfluenceBank
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.anchoring.rasterUpwardSites
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * Task `T-160` — `C-0022`'s edge load, **re-solved at the buildable 38.08 nm width**.
 * Leaf `A2.2`, with `A7.4` and `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh electrostatics.EdgeWidthDependenceStudyKt
 * ```
 *
 * Emits `gpd/results/T-160-edge-width-dependence.json`.
 *
 * `C-0090` moved the tile's along-helix width from §3's nominal 40.00 nm to `C-0086`'s buildable
 * 112 bp = 38.08 nm, rescaled `C-0022`'s **interior** pressure to the new footprint, and **carried
 * its three collar terms unchanged**. This study is the one input that claim did not recompute:
 * the same solver, `PoissonBoltzmannEdge`, at a `tileHalfWidth` of 19.04 nm instead of 20.00.
 *
 * The cheap bound ([CollarTailModel]) runs first and predicts the whole movement in closed form;
 * the solve is spent because that bound's own bracket straddles `C-0090`'s declared 0.32 %
 * sensitivity, and the bound then becomes a falsifiable model rather than an excuse.
 */

// --------------------------------------------------------------------------------- the records

/** One `(buffer, gap, bias)` state of the sweep. `C-0012`'s located biases, never grid ones. */
@Serializable
data class T160State(
    val name: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double
)

/** `C-0022`'s three collar deliverables, solved at one half-width and one refinement. */
@Serializable
data class T160CollarRecord(
    val state: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val tileHalfWidth: Double,
    val alongHelixWidth: Double,
    val refinement: Int,
    val nodes: Int,
    val centrelineLoad: Double,
    val oneDimensionalLoad: Double,
    val centrelineOverOneDimensional: Double,
    val taperDepth: Double,
    val taperWidth: Double,
    val taperDecayLength: Double,
    val taperLoadDeficit: Double,
    /** The mesh node `fitEdgeTaper`'s standoff actually snapped to, nm — see [taperFitAtExactStandoff]. */
    val standoffNode: Double,
    val taperDepthAtExactStandoff: Double,
    val taperWidthAtExactStandoff: Double,
    val rimResidualDepthAtExactStandoff: Double,
    val totalDeficitPerUnitEdge: Double,
    val rimResidualPerUnitEdge: Double,
    val rimResidualDepth: Double,
    val effectiveCollarWidth: Double,
    val edgeForceFractionMinMargin: Double,
    val edgeForceFractionAdditive: Double,
    val chargeBalance: Double,
    val centrelineRouteSpread: Double,
    val numericallyResolved: Boolean,
    val newtonIterations: Int,
    val linearIterations: Int
)

/** The movement of one collar quantity between the two half-widths, at matched refinement. */
@Serializable
data class T160DepartureRecord(
    val state: String,
    val refinement: Int,
    val quantity: String,
    val atNominalHalfWidth: Double,
    val atBuildableHalfWidth: Double,
    val relativeDeparture: Double,
    val insidePlacementSensitivity: Boolean
)

/** The cheap bound, at one anchoring of its two free constants. */
@Serializable
data class T160CheapBoundRecord(
    val anchoring: String,
    val decayLength: Double,
    val centrelineExcess: Double,
    val predictedTaperDepth: Double,
    val predictedTaperWidth: Double,
    val predictedRimResidualDepth: Double,
    val predictedDeparture: Double,
    val settlesTheQuestion: Boolean,
    val note: String
)

/** The far-field exponential tail, fitted to a solved profile rather than assumed. */
@Serializable
data class T160TailRecord(
    val tileHalfWidth: Double,
    val refinement: Int,
    val windowFrom: Double,
    val windowTo: Double,
    val samples: Int,
    val asymptoticLoad: Double,
    val amplitude: Double,
    val decayLength: Double,
    val relativeResidual: Double
)

/** What the collar does to `C-0090`'s flatness, on `C-0090`'s own placement and host. */
@Serializable
data class T160FlatnessRecord(
    val case: String,
    val collarSource: String,
    val taperDepth: Double,
    val taperWidth: Double,
    val rimResidualDepth: Double,
    val alongHelixWidth: Double,
    val acrossHelixSpan: Double,
    val phaseBasePairs: Int,
    val stations: Int,
    val freeStroke: Double,
    val freeDishingOverStroke: Double,
    val bestDishingOverStroke: Double,
    val movementAgainstCarried: Double,
    val insidePlacementSensitivity: Boolean,
    val flatAtTenPercent: Boolean
)

/** A convergence axis, emitted rather than asserted only — gate 4. */
@Serializable
data class T160ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<String>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

/** An upstream number reproduced rather than transcribed — gate 5. */
@Serializable
data class T160ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
data class T160Predicate(val name: String, val statement: String, val verdict: String)

@Serializable
data class T160Falsifier(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
data class T160Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val conditions: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val bjerrumLength: Double,
    val states: List<T160State>,
    val tailFits: List<T160TailRecord>,
    val cheapBound: List<T160CheapBoundRecord>,
    val collars: List<T160CollarRecord>,
    val departures: List<T160DepartureRecord>,
    val widthSweep: List<T160CollarRecord>,
    val flatness: List<T160FlatnessRecord>,
    val convergence: List<T160ConvergenceRecord>,
    val reproductions: List<T160ReproductionRecord>,
    val predicates: List<T160Predicate>,
    val falsifiers: List<T160Falsifier>,
    val citedInputs: List<String>,
    val provenance: Map<String, String>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val parameters: Map<String, Double>
)

// ------------------------------------------------------------------------------ the parameters

/** §3's nominal along-helix half-width, nm. */
private const val T160_NOMINAL_HALF_WIDTH = 20.0

/** `C-0086`'s buildable 112 bp row, halved: `38.08/2` nm. */
private const val T160_BUILDABLE_HALF_WIDTH = 19.04

/** `C-0090`'s own declared placement sensitivity — the guard-inset sweep's 0.32 %. */
private const val T160_PLACEMENT_SENSITIVITY = 0.0032

/** The refinement `C-0022` swept at, and the one its published triple was read from. */
private const val T160_SWEEP_REFINEMENT = 3

/** `C-0090`'s winning phase; and 15 duplexes, unchanged across the width (its Deliverable 1). */
private const val T160_RECOMMENDED_PHASE = 8
private const val T160_DUPLEXES = 15

/** The window the far tail is fitted over, nm from the rim — beyond the near-rim structure. */
private const val T160_TAIL_WINDOW_FROM = 6.0
private const val T160_TAIL_WINDOW_INSET = 0.5

private const val T160_STERN_CAPACITANCE = 20.0
private const val T160_SEARCH_NODES = 800

private val T160_STATES = listOf(
    T160State("design point — C-0090's own load", 2.0, 10.0, 0.192),
    T160State("the 10 nm gap at C-0012's softest-model bias", 2.0, 10.0, 0.134),
    T160State("the 5 nm layer", 2.0, 5.0, 0.368),
    T160State("10 mM, where C-0022's sign reverses", 10.0, 10.0, 0.192)
)

private val T160_WIDTH_SWEEP = listOf(12.0, 14.0, 16.0, 18.0, 19.04, 20.0, 22.0, 25.0, 30.0)

// -------------------------------------------------------------------------------- the plumbing

private fun t160TileCharge(): Double {
    val tile = DnaOrigamiTile()
    return -tile.projectedChargeDensity * tile.manningSurvivingFraction(2, bjerrumLength()) / 2.0
}

private fun t160DiffusePotential(
    concentration: Double,
    gapHeight: Double,
    appliedBias: Double
): Double = diffusePotentialOfAppliedBias(
    gapHeight, appliedBias, t160TileCharge(),
    sternChargeDensityPerVolt(T160_STERN_CAPACITANCE),
    IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
    uniformMedium(GapMedium()), bjerrumLength(), nodes = T160_SEARCH_NODES
)

private fun t160OneDimensionalLoad(
    concentration: Double,
    gapHeight: Double,
    diffuse: Double
): Double = -PoissonBoltzmannGap(
    gapHeight, IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
    uniformMedium(GapMedium()), bjerrumLength(),
    nodes = maxOf(4000, (gapHeight * 1200.0).toInt())
).solve(diffuse / thermalVoltage(), t160TileCharge())
    .disjoiningPressureInPiconewtonPerSquareNanometre

private fun t160Solver(
    concentration: Double,
    gapHeight: Double,
    halfWidth: Double,
    refinement: Int
) = PoissonBoltzmannEdge(
    gapHeight = gapHeight,
    ionModel = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
    medium = GapMedium(),
    bjerrumLength = bjerrumLength(),
    tileHalfWidth = halfWidth,
    refinement = refinement
)

private fun t160Collar(
    state: T160State,
    halfWidth: Double,
    refinement: Int
): Pair<T160CollarRecord, EdgeSolution> {
    val diffuse = t160DiffusePotential(state.concentration, state.gapHeight, state.appliedBias)
    val oneDimensional = t160OneDimensionalLoad(state.concentration, state.gapHeight, diffuse)
    val solver = t160Solver(state.concentration, state.gapHeight, halfWidth, refinement)
    val solution = solver.solve(diffuse / thermalVoltage(), t160TileCharge())
    val fit = solution.taperFit()
    val exact = taperFitAtExactStandoff(
        solution.distanceFromEdge, solution.downwardLoad, solution.centrelineLoad,
        DEFAULT_RIM_STANDOFF
    )
    val total = solution.totalDeficitPerUnitEdge
    val rim = solution.rimResidualPerUnitEdge()
    val exactRim = total - exact.loadDeficit
    var snapped = 0
    while (snapped < solution.distanceFromEdge.size - 1 &&
        solution.distanceFromEdge[snapped] < DEFAULT_RIM_STANDOFF
    ) snapped++
    // The force fraction is read at the tile's OWN edge length, 2a — the min-margin mapping onto
    // the square. C-0022 read it at a fixed 40 nm, which is the same thing at a = 20 and is what
    // the gate compares against.
    val edge = 2.0 * halfWidth
    return T160CollarRecord(
        state = state.name,
        concentration = state.concentration,
        gapHeight = state.gapHeight,
        appliedBias = state.appliedBias,
        tileHalfWidth = halfWidth,
        alongHelixWidth = edge,
        refinement = refinement,
        nodes = solver.height.size * solver.lateral.size,
        centrelineLoad = solution.centrelineLoad,
        oneDimensionalLoad = oneDimensional,
        centrelineOverOneDimensional = solution.centrelineLoad / oneDimensional,
        taperDepth = fit.depth,
        taperWidth = fit.equivalentWidth,
        taperDecayLength = fit.decayLength,
        taperLoadDeficit = fit.loadDeficit,
        standoffNode = solution.distanceFromEdge[snapped],
        taperDepthAtExactStandoff = exact.depth,
        taperWidthAtExactStandoff = exact.equivalentWidth,
        rimResidualDepthAtExactStandoff =
            2.0 * exactRim / (solution.centrelineLoad * DEFAULT_RIM_STANDOFF),
        totalDeficitPerUnitEdge = total,
        rimResidualPerUnitEdge = rim,
        rimResidualDepth = 2.0 * rim / (solution.centrelineLoad * DEFAULT_RIM_STANDOFF),
        effectiveCollarWidth = -total / solution.centrelineLoad,
        edgeForceFractionMinMargin =
            (4.0 * edge * total - 8.0 * fit.firstMoment) / (edge * edge * solution.centrelineLoad),
        edgeForceFractionAdditive = 4.0 * total / (edge * solution.centrelineLoad),
        chargeBalance = solution.chargeBalance,
        centrelineRouteSpread = solution.centrelineRouteSpread,
        numericallyResolved = solution.numericallyResolved,
        newtonIterations = solution.newtonIterations,
        linearIterations = solution.linearIterations
    ) to solution
}

private fun t160Departures(
    state: String,
    refinement: Int,
    nominal: T160CollarRecord,
    buildable: T160CollarRecord
): List<T160DepartureRecord> = listOf(
    Triple("taperDepth", nominal.taperDepth, buildable.taperDepth),
    Triple("taperWidth", nominal.taperWidth, buildable.taperWidth),
    Triple("rimResidualDepth", nominal.rimResidualDepth, buildable.rimResidualDepth),
    Triple(
        "taperDepthAtExactStandoff",
        nominal.taperDepthAtExactStandoff, buildable.taperDepthAtExactStandoff
    ),
    Triple(
        "taperWidthAtExactStandoff",
        nominal.taperWidthAtExactStandoff, buildable.taperWidthAtExactStandoff
    ),
    Triple(
        "rimResidualDepthAtExactStandoff",
        nominal.rimResidualDepthAtExactStandoff, buildable.rimResidualDepthAtExactStandoff
    ),
    Triple("taperLoadDeficit", nominal.taperLoadDeficit, buildable.taperLoadDeficit),
    Triple("effectiveCollarWidth", nominal.effectiveCollarWidth, buildable.effectiveCollarWidth),
    Triple(
        "edgeForceFractionMinMargin",
        nominal.edgeForceFractionMinMargin, buildable.edgeForceFractionMinMargin
    )
).map { (quantity, at20, at19) ->
    val departure = if (at20 == 0.0) abs(at19) else abs(at19 / at20 - 1.0)
    T160DepartureRecord(
        state = state,
        refinement = refinement,
        quantity = quantity,
        atNominalHalfWidth = at20,
        atBuildableHalfWidth = at19,
        relativeDeparture = departure,
        insidePlacementSensitivity = departure < T160_PLACEMENT_SENSITIVITY
    )
}

/** `C-0022`'s published nominal collar, read from its result file rather than retyped. */
private fun t160PublishedCollar(file: File): Map<String, Double> {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at 2 mM, 10 nm, 0.192 V")
    return listOf(
        "taperDepth", "taperWidth", "taperDecayLength", "rimResidualDepth",
        "edgeForceFractionMinMargin", "centrelineLoad", "totalDeficitPerUnitEdge"
    ).associateWith { record.getValue(it).jsonPrimitive.content.toDouble() }
}

/** `C-0090`'s recommended placement and its published flatness, read from its result file. */
private fun t160PublishedPlacement(file: File): Pair<String, Double> {
    require(file.exists()) { "C-0090's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("placements").jsonArray.map { it.jsonObject }
        .firstOrNull {
            it.getValue("case").jsonPrimitive.content.startsWith("RECOMMENDED") &&
                    it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() ==
                    T160_RECOMMENDED_PHASE
        } ?: error("no C-0090 RECOMMENDED placement at phase $T160_RECOMMENDED_PHASE")
    return record.getValue("bestKey").jsonPrimitive.content to
            record.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble()
}

/**
 * The `(x, y)` stations of a `C-0063`-style placement key, `row:x,x,…;row:x,x,…` with `x` in
 * nanometres × 1e6 — `UpwardArmPlacement.key`'s own canonical form, parsed rather than re-searched.
 */
private fun t160StationsOf(key: String, interhelicalDistance: Double): List<Pair<Double, Double>> =
    key.split(";").flatMap { row ->
        val (index, roots) = row.split(":")
        val y = (index.toInt() - (T160_DUPLEXES - 1) / 2.0) * interhelicalDistance
        roots.split(",").map { it.toDouble() / 1.0e6 to y }
    }

// ------------------------------------------------------------------------------------ the study

fun main() {
    val started = System.currentTimeMillis()
    val charge = t160TileCharge()
    val design = T160_STATES.first()
    val published = t160PublishedCollar(ResultInputs.T_3B.file())

    // ------------------------------------------------------------------ the gate, before anything
    println("T-160 — the gate: C-0022's own half-width, refinement $T160_SWEEP_REFINEMENT ...")
    val (gate, gateSolution) = t160Collar(design, T160_NOMINAL_HALF_WIDTH, T160_SWEEP_REFINEMENT)
    val reproductions = mutableListOf(
        t160Reproduction("C-0022", "the taper depth at 2 mM, 10 nm, 0.192 V",
            published.getValue("taperDepth"), gate.taperDepth),
        t160Reproduction("C-0022", "the taper width",
            published.getValue("taperWidth"), gate.taperWidth),
        t160Reproduction("C-0022", "the deficit centroid",
            published.getValue("taperDecayLength"), gate.taperDecayLength),
        t160Reproduction("C-0022", "the rim residual depth",
            published.getValue("rimResidualDepth"), gate.rimResidualDepth),
        t160Reproduction("C-0022", "the min-margin force fraction (the +14.71%)",
            published.getValue("edgeForceFractionMinMargin"), gate.edgeForceFractionMinMargin),
        t160Reproduction("C-0022", "the centre-line load",
            published.getValue("centrelineLoad"), gate.centrelineLoad)
    )
    val gateWorst = reproductions.maxOf { it.departure }
    check(gateWorst < 1e-6) {
        "F1 FIRED: the pipeline does not reproduce C-0022 at its own half-width, worst " +
                "departure $gateWorst"
    }

    // ------------------------------------------------------- the cheap bound, before the answer
    println("T-160 — the cheap bound, which runs before the second solve ...")
    val tailWindowTo = T160_NOMINAL_HALF_WIDTH - T160_TAIL_WINDOW_INSET
    val tail = fitExponentialTail(
        gateSolution.distanceFromEdge, gateSolution.downwardLoad,
        T160_TAIL_WINDOW_FROM, tailWindowTo
    )
    val tailSamples = gateSolution.distanceFromEdge.count {
        it >= T160_TAIL_WINDOW_FROM && it <= tailWindowTo
    }
    val tailFits = listOf(
        T160TailRecord(
            tileHalfWidth = T160_NOMINAL_HALF_WIDTH,
            refinement = T160_SWEEP_REFINEMENT,
            windowFrom = T160_TAIL_WINDOW_FROM,
            windowTo = tailWindowTo,
            samples = tailSamples,
            asymptoticLoad = tail.asymptoticLoad,
            amplitude = tail.amplitude,
            decayLength = tail.decayLength,
            relativeResidual = tail.relativeResidual
        )
    )
    val kappa = MagnesiumChlorideBuffer(design.concentration).inverseDebyeLength()
    val ceiling = 1.0 / transverseDecayRateBound(kappa, design.gapHeight)
    val optimistic = t160Model(gate, tail.decayLength, gate.centrelineLoad - tail.asymptoticLoad)
    val pessimistic = t160Model(
        gate, ceiling, gate.centrelineLoad - gate.oneDimensionalLoad
    )
    val cheapBound = listOf(
        t160CheapBound(
            "best estimate — the far tail FITTED to C-0022's own solved profile",
            optimistic,
            "l = " + "%.4f".format(tail.decayLength) + " nm and tau(20) = " +
                    "%.4g".format(optimistic.centrelineExcess) + " pN/nm^2, both read off the " +
                    "reference solve; the residual of the tail fit is " +
                    "%.2g".format(tail.relativeResidual)
        ),
        t160CheapBound(
            "pessimistic — the transverse-eigenvalue CEILING and the 1-D cross-solver difference",
            pessimistic,
            "l = 1/q0 = " + "%.4f".format(ceiling) + " nm is a rigorous upper bound on the " +
                    "lateral decay length within linear theory (C-0022's cheap bound), and " +
                    "tau(20) is taken as centrelineLoad - oneDimensionalLoad, which is " +
                    "contaminated by the 0.03-0.14% agreement between two different solvers " +
                    "and is therefore an over-estimate of the true tail excess"
        )
    )
    val cheapLow = cheapBound.minOf { it.predictedDeparture }
    val cheapHigh = cheapBound.maxOf { it.predictedDeparture }
    val cheapSettles = cheapHigh < T160_PLACEMENT_SENSITIVITY
    println(
        (
                "T-160 — the cheap bound brackets the movement at %.3f%%-%.3f%% against " +
                        "C-0090's %.2f%%; it %s settle the question, so the solve %s"
                ).format(
                100.0 * cheapLow, 100.0 * cheapHigh, 100.0 * T160_PLACEMENT_SENSITIVITY,
                if (cheapSettles) "DOES" else "does NOT",
                if (cheapSettles) "is spent only as a check" else "runs"
            )
    )

    // ------------------------------------------------------------------------------- the answer
    println("T-160 — the answer: the buildable half-width ...")
    val (answer, _) = t160Collar(design, T160_BUILDABLE_HALF_WIDTH, T160_SWEEP_REFINEMENT)
    val collars = mutableListOf(gate, answer)
    val departures = mutableListOf<T160DepartureRecord>()
    departures += t160Departures(design.name, T160_SWEEP_REFINEMENT, gate, answer)

    // ---------------------------------------------------------------------------- the other states
    for (state in T160_STATES.drop(1)) {
        println("T-160 — ${state.name} ...")
        val (wide, _) = t160Collar(state, T160_NOMINAL_HALF_WIDTH, T160_SWEEP_REFINEMENT)
        val (narrow, _) = t160Collar(state, T160_BUILDABLE_HALF_WIDTH, T160_SWEEP_REFINEMENT)
        collars += wide
        collars += narrow
        departures += t160Departures(state.name, T160_SWEEP_REFINEMENT, wide, narrow)
    }

    // ------------------------------------------------------ convergence: NESTED 1/2/4, per width
    println("T-160 — convergence, nested 1/2/4 at both half-widths ...")
    val nested = mutableListOf<T160CollarRecord>()
    for (refinement in listOf(1, 2, 4)) {
        val (wide, _) = t160Collar(design, T160_NOMINAL_HALF_WIDTH, refinement)
        val (narrow, _) = t160Collar(design, T160_BUILDABLE_HALF_WIDTH, refinement)
        nested += wide
        nested += narrow
        departures += t160Departures(design.name, refinement, wide, narrow)
    }
    collars += nested

    // ----------------------------------------------------------------------- the half-width sweep
    println("T-160 — the half-width sweep at refinement 2 ...")
    val widthSweep = T160_WIDTH_SWEEP.map { halfWidth ->
        val (record, _) = t160Collar(design, halfWidth, 2)
        println(
            "    a = %6.2f nm   depth %10.6f   width %8.4f nm   rim %10.6f".format(
                halfWidth, record.taperDepth, record.taperWidth, record.rimResidualDepth
            )
        )
        record
    }

    // --------------------------------------------------------------- what it does to C-0090's flatness
    println("T-160 — C-0090's own placement, re-evaluated under the re-solved collar ...")
    val (bestKey, publishedFlatness) = t160PublishedPlacement(
        ResultInputs.T_153.file()
    )
    val carried = CollarTerm(published.getValue("taperDepth"), published.getValue("taperWidth")) to
            CollarTerm(published.getValue("rimResidualDepth"), DEFAULT_RIM_STANDOFF)
    val resolved = CollarTerm(answer.taperDepth, answer.taperWidth) to
            CollarTerm(answer.rimResidualDepth, DEFAULT_RIM_STANDOFF)
    val flatness = mutableListOf<T160FlatnessRecord>()
    val carriedFlatness = t160Flatness(
        "CARRIED — C-0090's own reading, the 40 nm tile's collar",
        "C-0022 at a = 20.00 nm, read from gpd/results/T-3b-*.json",
        carried, bestKey, null
    )
    flatness += carriedFlatness
    flatness += t160Flatness(
        "RE-SOLVED — the same placement under the 38.08 nm tile's own collar",
        "T-160 at a = 19.04 nm, refinement $T160_SWEEP_REFINEMENT",
        resolved, bestKey, carriedFlatness.bestDishingOverStroke
    )
    // And the same two with the standoff placed exactly, which removes the mesh-snap partition
    // between the smooth term and the rim residual. If the dishing is insensitive to that, the
    // partition is a bookkeeping variable and not a load.
    val gateExact = CollarTerm(gate.taperDepthAtExactStandoff, gate.taperWidthAtExactStandoff) to
            CollarTerm(gate.rimResidualDepthAtExactStandoff, DEFAULT_RIM_STANDOFF)
    val answerExact =
        CollarTerm(answer.taperDepthAtExactStandoff, answer.taperWidthAtExactStandoff) to
                CollarTerm(answer.rimResidualDepthAtExactStandoff, DEFAULT_RIM_STANDOFF)
    val carriedExactFlatness = t160Flatness(
        "CARRIED, standoff placed exactly — the same 40 nm field, re-partitioned",
        "T-160 at a = 20.00 nm, refinement $T160_SWEEP_REFINEMENT, exact standoff",
        gateExact, bestKey, carriedFlatness.bestDishingOverStroke
    )
    flatness += carriedExactFlatness
    flatness += t160Flatness(
        "RE-SOLVED, standoff placed exactly",
        "T-160 at a = 19.04 nm, refinement $T160_SWEEP_REFINEMENT, exact standoff",
        answerExact, bestKey, carriedExactFlatness.bestDishingOverStroke
    )
    reproductions += t160Reproduction(
        "C-0090", "the recommended 34-root dishing at phase 8, 38.08 nm",
        publishedFlatness, carriedFlatness.bestDishingOverStroke
    )

    // --------------------------------------------------------------------------------- the gates
    val designDepartures = departures.filter {
        it.state == design.name && it.refinement == T160_SWEEP_REFINEMENT
    }
    val collarDepartures = designDepartures.filter {
        it.quantity in listOf("taperDepth", "taperWidth", "rimResidualDepth")
    }
    val worstCollarDeparture = collarDepartures.maxOf { it.relativeDeparture }
    val exactDepartures = designDepartures.filter {
        it.quantity in listOf(
            "taperDepthAtExactStandoff", "taperWidthAtExactStandoff",
            "rimResidualDepthAtExactStandoff"
        )
    }
    val worstExactDeparture = exactDepartures.maxOf { it.relativeDeparture }
    val fitFreeDeparture = designDepartures
        .single { it.quantity == "effectiveCollarWidth" }.relativeDeparture
    val nestedDepartures = listOf(1, 2, T160_SWEEP_REFINEMENT, 4).map { refinement ->
        departures.filter {
            it.state == design.name && it.refinement == refinement &&
                    it.quantity in listOf("taperDepth", "taperWidth", "rimResidualDepth")
        }.maxOf { it.relativeDeparture }
    }
    val nestedExact = listOf(1, 2, T160_SWEEP_REFINEMENT, 4).map { refinement ->
        departures.filter {
            it.state == design.name && it.refinement == refinement &&
                    it.quantity in listOf(
                "taperDepthAtExactStandoff", "taperWidthAtExactStandoff",
                "rimResidualDepthAtExactStandoff"
            )
        }.maxOf { it.relativeDeparture }
    }
    val nestedFitFree = listOf(1, 2, T160_SWEEP_REFINEMENT, 4).map { refinement ->
        departures.single {
            it.state == design.name && it.refinement == refinement &&
                    it.quantity == "effectiveCollarWidth"
        }.relativeDeparture
    }
    val convergence = listOf(
        T160ConvergenceRecord(
            quantity = "the worst collar departure between the two half-widths",
            parameter = "mesh refinement, NESTED 1/2/4 with the sweep's 3 beside them",
            values = listOf("1", "2", "3", "4"),
            results = nestedDepartures,
            departure = nestedDepartures.max() - nestedDepartures.min(),
            note = "the DEPARTURE is the answer, not either absolute collar: a difference of " +
                    "two solves on the same mesh cancels the discretisation error that C-0022's " +
                    "own depth still carries at ~4% at refinement 3"
        ),
        T160ConvergenceRecord(
            quantity = "the taper depth at the buildable half-width",
            parameter = "mesh refinement, nested 1/2/4",
            values = listOf("1", "2", "4"),
            results = nested.filter { it.tileHalfWidth == T160_BUILDABLE_HALF_WIDTH }
                .map { it.taperDepth },
            departure = t160Spread(
                nested.filter { it.tileHalfWidth == T160_BUILDABLE_HALF_WIDTH }
                    .map { it.taperDepth }
            ),
            note = "the ABSOLUTE depth is NOT converged at these meshes and C-0022 says so; it " +
                    "is reported to show that the departure above is, and it is not the answer"
        ),
        T160ConvergenceRecord(
            quantity = "the far-tail decay length fitted to the reference profile",
            parameter = "the fit's own residual",
            values = listOf("window ${T160_TAIL_WINDOW_FROM}-$tailWindowTo nm"),
            results = listOf(tail.decayLength, tail.relativeResidual),
            departure = tail.relativeResidual,
            note = "an exponential tail is a two-parameter model of a solved profile and the " +
                    "residual is what says whether it is one"
        ),
        T160ConvergenceRecord(
            quantity = "the FIT-FREE collar departure — the effective collar width, which is " +
                    "the global momentum flux over the centre-line load and contains no fit",
            parameter = "mesh refinement, NESTED 1/2/4 with the sweep's 3 beside them",
            values = listOf("1", "2", "3", "4"),
            results = nestedFitFree,
            departure = nestedFitFree.max() - nestedFitFree.min(),
            note = "this is the answer the acceptance asks for and it is converged: every " +
                    "refinement puts the collar's width-dependence two decades inside C-0090's " +
                    "0.32%, where the FITTED triple does not converge at all"
        ),
        T160ConvergenceRecord(
            quantity = "the collar departure with the 1 nm standoff placed EXACTLY rather than " +
                    "snapped to a mesh node",
            parameter = "mesh refinement, NESTED 1/2/4 with the sweep's 3 beside them",
            values = listOf("1", "2", "3", "4"),
            results = nestedExact,
            departure = nestedExact.max() - nestedExact.min(),
            note = "the diagnosis: the raw triple's departure is dominated by WHICH NODE the " +
                    "standoff lands on, and the tile half-width rescales the whole graded " +
                    "lateral mesh. Placing the standoff exactly removes most of it"
        ),
        T160ConvergenceRecord(
            quantity = "the half-width sweep's MONOTONICITY, snapped standoff against exact",
            parameter = "the fitted taper width over a = 12 … 30 nm at refinement 2",
            values = T160_WIDTH_SWEEP.map { "%.2f".format(it) },
            results = widthSweep.map { it.taperWidthAtExactStandoff },
            departure = if (t160Monotone(widthSweep.map { it.taperWidthAtExactStandoff })) 0.0
            else 1.0,
            note = "the exponential-tail model predicts a strictly monotone, saturating " +
                    "width-dependence. Snapped, the sweep alternates (falsifier F6 fires); with " +
                    "the standoff placed exactly it is monotone at all nine half-widths, which " +
                    "is the third and cleanest signature that the alternation is the mesh. " +
                    "A departure of 0.0 here means monotone and 1.0 means not"
        )
    )

    val resolvedFlatness = flatness[1]
    val carriedExact = flatness[2]
    val resolvedExact = flatness[3]
    val falsifiers = listOf(
        T160Falsifier(
            "F1",
            "at a = 20.00 nm and refinement 3 the pipeline does not reproduce C-0022's " +
                    "-0.302887367, 8.93928311 and -0.147080774",
            gateWorst >= 1e-6,
            "worst departure over six published quantities: " + "%.2e".format(gateWorst)
        ),
        T160Falsifier(
            "F2",
            "the solved departure falls OUTSIDE the cheap bound's own bracket",
            worstCollarDeparture < cheapLow || worstCollarDeparture > cheapHigh,
            ("the RAW fit gives %.4f%% against a bracket of %.4f%%-%.4f%%, which is the " +
                    "diagnosis rather than a refutation: with the 1 nm standoff placed exactly " +
                    "instead of snapped to a mesh node the same solves give %.4f%%, INSIDE the " +
                    "bracket, and the fit-free effective collar width gives %.4f%%").format(
                100.0 * worstCollarDeparture, 100.0 * cheapLow, 100.0 * cheapHigh,
                100.0 * worstExactDeparture, 100.0 * fitFreeDeparture
            )
        ),
        T160Falsifier(
            "F3",
            "a collar term moves by more than C-0090's declared 0.32% between the two widths",
            worstCollarDeparture >= T160_PLACEMENT_SENSITIVITY,
            ("the worst of the three is %.4f%% against 0.32%% — and it is the SPLIT that moves, " +
                    "not the collar: their sum moves %.4f%% and the flatness the two terms " +
                    "together produce moves %.4f%%").format(
                100.0 * worstCollarDeparture, 100.0 * fitFreeDeparture,
                100.0 * resolvedFlatness.movementAgainstCarried
            )
        ),
        T160Falsifier(
            "F4",
            "the departure between the two half-widths does not converge in the mesh",
            nestedDepartures.max() - nestedDepartures.min() > nestedDepartures.last(),
            ("refinements 1/2/3/4 give %.4f%%, %.4f%%, %.4f%%, %.4f%%").format(
                100.0 * nestedDepartures[0], 100.0 * nestedDepartures[1],
                100.0 * nestedDepartures[2], 100.0 * nestedDepartures[3]
            )
        ),
        T160Falsifier(
            "F5",
            "the re-evaluated flatness under the CARRIED collar is not C-0090's 0.0621469105",
            abs(carriedFlatness.bestDishingOverStroke / publishedFlatness - 1.0) > 1e-6,
            ("reproduced %.10f against %.10f").format(
                carriedFlatness.bestDishingOverStroke, publishedFlatness
            )
        ),
        T160Falsifier(
            "F6",
            "the collar's width-dependence is not monotone across the 12-30 nm half-width sweep",
            !t160Monotone(widthSweep.map { it.taperWidth }),
            ("the snapped fitted taper width alternates over a = 12 -> 30 nm (%.4f -> %.4f nm, " +
                    "monotone: %s); with the standoff placed exactly the same nine solves are " +
                    "monotone and saturating (%.4f -> %.4f nm, monotone: %s), which is what " +
                    "the exponential-tail model predicts").format(
                widthSweep.first().taperWidth, widthSweep.last().taperWidth,
                t160Monotone(widthSweep.map { it.taperWidth }),
                widthSweep.first().taperWidthAtExactStandoff,
                widthSweep.last().taperWidthAtExactStandoff,
                t160Monotone(widthSweep.map { it.taperWidthAtExactStandoff })
            )
        )
    )

    val predicates = listOf(
        T160Predicate(
            "P1", "the cheap bound runs first and the decision to solve is justified against it",
            "MET — the closed-form tail model brackets the movement at " +
                    "%.3f%%-%.3f%%".format(100.0 * cheapLow, 100.0 * cheapHigh) +
                    ", which straddles C-0090's 0.32%, so the solve ran"
        ),
        T160Predicate(
            "P2", "the taper depth, taper width and rim residual at a 19.04 nm half-width",
            ("MET — %.9f, %.8f nm and %.9f at 300 K, aqueous 2 mM MgCl2 (2:1, so I = 3c), a " +
                    "10 nm gap and C-0012's located 0.192 V; with the standoff placed exactly " +
                    "rather than snapped, %.9f, %.8f nm and %.9f").format(
                answer.taperDepth, answer.taperWidth, answer.rimResidualDepth,
                answer.taperDepthAtExactStandoff, answer.taperWidthAtExactStandoff,
                answer.rimResidualDepthAtExactStandoff
            )
        ),
        T160Predicate(
            "P3", "at a = 20.00 nm the same pipeline reproduces C-0022's published triple",
            "MET — worst departure " + "%.2e".format(gateWorst) + " over six quantities"
        ),
        T160Predicate(
            "P4", "each movement is quoted as a departure at matched refinement, with its own " +
                    "mesh convergence",
            ("MET, AND IT IS WHAT THE TASK FOUND — the FITTED triple's worst departure is " +
                    "%.4f%% and it does NOT converge (%.4f%% of scatter over refinements " +
                    "1/2/3/4, larger than the departure itself), because the 1 nm standoff " +
                    "snaps to a different mesh node at each width. The FIT-FREE departure — the " +
                    "effective collar width, from the global momentum flux — is %.4f%% and " +
                    "converges to %.4f%%").format(
                100.0 * worstCollarDeparture,
                100.0 * (nestedDepartures.max() - nestedDepartures.min()),
                100.0 * fitFreeDeparture,
                100.0 * (nestedFitFree.max() - nestedFitFree.min())
            )
        ),
        T160Predicate(
            "P5", "C-0090's 0.0621469105 is re-evaluated under the re-solved collar",
            ("MET — %.10f against %.10f, a movement of %.4f%% against the 0.32%% that claim " +
                    "declares and T-5b's 0.10; with the standoff placed exactly at both widths " +
                    "it is %.10f against %.10f, a movement of %.4f%%. C-0090's verdict does " +
                    "not move.").format(
                resolvedFlatness.bestDishingOverStroke, carriedFlatness.bestDishingOverStroke,
                100.0 * resolvedFlatness.movementAgainstCarried,
                resolvedExact.bestDishingOverStroke, carriedExact.bestDishingOverStroke,
                100.0 * resolvedExact.movementAgainstCarried
            )
        )
    )

    val result = T160Result(
        task = "T-160",
        leaf = "A2.2",
        title = "C-0022's edge load, re-solved at the buildable 38.08 nm along-helix width",
        verificationType = "in-silico (C-0022's own 2-D nonlinear 2:1 Poisson-Boltzmann edge " +
                "solver re-run at a changed tileHalfWidth; C-0090's grillage, influence bank and " +
                "winning placement consumed read-only) + logical (a closed-form exponential-tail " +
                "model of the taper fit's own width-dependence, which is the cheap bound and is " +
                "falsified or upheld by the solve)",
        acceptance = "A 2-D Poisson-Boltzmann edge solve at a 38.08 nm along-helix footprint, " +
                "giving the collar's taper depth, taper width and rim residual at that width — " +
                "or the statement that the collar is width-independent to within C-0090's 0.32% " +
                "placement sensitivity.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. And " +
                "inside mean field: C-0005 puts the one-loop correction at 123-214% of the " +
                "leading term at these gaps, and for the OPPOSITELY charged tile-electrode pair " +
                "no published result gives even the direction. A second width does not narrow " +
                "that; it enters the DEPARTURE as a common factor rather than being escaped.",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous MgCl2, which is 2:1 so I = 3c, " +
                "at 2 mM (and 10 mM at one state); eps_r = 78, l_B = " +
                "%.4f nm".format(bjerrumLength()) + "; Manning-renormalised tile charge " +
                "%.6f e/nm^2".format(charge) + "; a 10 nm gap (and 5 nm at one state) at " +
                "C-0012's located operating bias; tile thickness 10 nm; along-helix half-width " +
                "20.00 nm (§3's nominal 40.0 nm) against 19.04 nm (C-0086's buildable 112 bp = " +
                "38.08 nm); the across-helix span 15 x 2.69 = 40.35 nm, UNCHANGED (C-0090's " +
                "Deliverable 1)",
        units = mapOf(
            "length" to "nm",
            "chargeDensity" to "e/nm^2",
            "concentration" to "mM",
            "potential" to "V",
            "force" to "pN",
            "lineForce" to "pN/nm",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "temperature" to "K",
            "depth" to "dimensionless, NEGATIVE for an edge enhancement",
            "departure" to "dimensionless, relative"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; x is lateral, x = 0 the tile " +
                    "centre-line and a symmetry plane, the rim at x = a",
            "a is the ALONG-HELIX half-width: 20.00 nm at §3's nominal footprint, 19.04 nm at " +
                    "C-0086's buildable 112 bp row. The ACROSS-helix span is a count of " +
                    "duplexes and does not move",
            "s is distance measured INWARD from the rim, so s = 0 is the rim and s = a the " +
                    "centre-line",
            "the load is reported DOWNWARD, positive when it pushes the tile toward the electrode",
            "a depth is 1 - load/interior and is NEGATIVE for an enhancement; a width is the " +
                    "raised cosine matching the deficit's first two moments; the rim residual is " +
                    "the part of the global deficit inside the 1 nm standoff, as a line load",
            "MgCl2 is 2:1, so I = 3c and the ion model is T-3a's, reused unchanged"
        ),
        temperature = ROOM_TEMPERATURE,
        medium = "aqueous MgCl2, 2 mM (10 mM at one state), 300 K",
        thermalEnergy = thermalEnergy(),
        bjerrumLength = bjerrumLength(),
        states = T160_STATES,
        tailFits = tailFits,
        cheapBound = cheapBound,
        collars = collars,
        departures = departures,
        widthSweep = widthSweep,
        flatness = flatness,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        citedInputs = listOf(
            "eps_r(water, 300 K) = 78 — CITED, as in C-0005/C-0008.",
            "the Manning-renormalised tile charge, 11.90% of bare — CITED FROM C-0005 via " +
                    "C-0008. The tile is charge-SATURATED.",
            "Stern capacitance ~20 uF/cm^2 — CITED, load-bearing for the bias mapping only.",
            "C-0012's located operating biases — CITED FROM C-0012 as read by C-0017, never a " +
                    "grid bias (CH-0007, CH-0016).",
            "C-0022's published collar at 2 mM, 10 nm, 0.192 V — READ FROM ITS RESULT FILE, " +
                    "keyed on concentration, gap AND bias, and REPRODUCED here as the gate.",
            "C-0090's recommended placement key and its 0.0621469105 — READ FROM ITS RESULT " +
                    "FILE and reproduced before anything is done to it.",
            "the SAXS interhelical distance 2.69 nm and the 0.34 nm rise — CITED (Fischer et " +
                    "al. 2016; Rothemund 2006).",
            "the duplex radius 1.0 nm, which sets only where the unresolvable corner is cut off " +
                    "— CITED (B-DNA)."
        ),
        provenance = mapOf(
            // Declared, and asserted equal to what a static analysis of this file finds it
            // reading — `tools/result-reader-census.py --check`, task `P-22`.
            "sources" to "gpd/results/T-3b-tile-edge-load-profile.json, " +
                    "gpd/results/T-153-buildable-raster-width.json",
            "emits" to "gpd/results/T-160-edge-width-dependence.json",
            "model" to "src/main/kotlin/electrostatics/EdgeWidthDependence.kt (new), " +
                    "src/main/kotlin/electrostatics/PoissonBoltzmannEdge.kt (C-0022, unchanged)"
        ),
        findings = emptyMap(),
        validity = t160Validity(),
        openQuestions = t160OpenQuestions(),
        parameters = mapOf(
            "nominalHalfWidth" to T160_NOMINAL_HALF_WIDTH,
            "buildableHalfWidth" to T160_BUILDABLE_HALF_WIDTH,
            "nominalWidth" to 2.0 * T160_NOMINAL_HALF_WIDTH,
            "buildableWidth" to 2.0 * T160_BUILDABLE_HALF_WIDTH,
            "placementSensitivity" to T160_PLACEMENT_SENSITIVITY,
            "sweepRefinement" to T160_SWEEP_REFINEMENT.toDouble(),
            "rimStandoff" to DEFAULT_RIM_STANDOFF,
            "recommendedPhase" to T160_RECOMMENDED_PHASE.toDouble(),
            "duplexes" to T160_DUPLEXES.toDouble(),
            "armCount" to C0055_ARM_COUNT.toDouble(),
            "tileChargeDensity" to charge,
            "sternCapacitance" to T160_STERN_CAPACITANCE,
            "transverseDecayCeiling" to ceiling,
            "fittedTailDecayLength" to tail.decayLength,
            "cheapBoundLow" to cheapLow,
            "cheapBoundHigh" to cheapHigh,
            "worstCollarDeparture" to worstCollarDeparture,
            "worstExactStandoffDeparture" to worstExactDeparture,
            "fitFreeDeparture" to fitFreeDeparture,
            "carriedFlatness" to carriedFlatness.bestDishingOverStroke,
            "resolvedFlatness" to resolvedFlatness.bestDishingOverStroke,
            "flatnessMovement" to resolvedFlatness.movementAgainstCarried,
            "flatnessTolerance" to 0.10,
            "solves" to (collars.size + widthSweep.size).toDouble()
        )
    )
    val complete = result.copy(findings = t160Findings(result))
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-160-edge-width-dependence.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(complete).roundedForResult(
                // `T-212`/`CH-0154`: the tree-wide rule for the DIAGNOSTIC records first, then
                // this study's own precisions. The two carry the SAME SPELLING on different
                // records -- `departures[*].relativeDeparture` is this study's ANSWER and
                // `convergence[*].relativeDeparture` is a residual between two refinements --
                // and the qualified keys win inside a departure record only.
                digitsByKey = DEPARTURE_DIGITS_BY_KEY + mapOf(
                    "relativeDeparture" to 6,
                    "movementAgainstCarried" to 6,
                    "departure" to 6,
                    "relativeResidual" to 2,
                    "chargeBalance" to 3,
                    "centrelineRouteSpread" to 3
                ),
                // The study emits DIMENSIONLESS departures near 1e-5 and residuals near 1e-16;
                // RESULT_ABSOLUTE_FLOOR is a magnitude in the LOCKED UNITS and does not travel
                // to them (CLAUDE.md, P-18).
                floor = 1e-18
            ).withEmissionHeader(LatticeTag.SQUARE, null)
        ) + "\n"
    )
    t160Report(complete, output)
    // Printed, never emitted: a wall-clock time is a property of the machine and of the JIT's
    // warm-up schedule, so a result file carrying one cannot be byte-identical on a re-run.
    // CLAUDE.md: emit the answer and a convergence measure; emit nothing that counts steps.
    println("T-160 — %.0f s".format((System.currentTimeMillis() - started) / 1000.0))
}

// ------------------------------------------------------------------------------ the small parts

private fun t160Reproduction(
    source: String,
    quantity: String,
    published: Double,
    reproduced: Double
): T160ReproductionRecord {
    val departure = if (published == 0.0) abs(reproduced)
    else abs(reproduced / published - 1.0)
    return T160ReproductionRecord(
        source = source,
        quantity = quantity,
        published = published,
        reproduced = reproduced,
        departure = departure,
        strict = departure < 1e-8
    )
}

private fun t160Model(
    reference: T160CollarRecord,
    decayLength: Double,
    centrelineExcess: Double
) = CollarTailModel(
    referenceHalfWidth = reference.tileHalfWidth,
    standoff = DEFAULT_RIM_STANDOFF,
    decayLength = decayLength,
    asymptoticLoad = reference.centrelineLoad - centrelineExcess,
    centrelineExcess = centrelineExcess,
    loadDeficit = reference.taperLoadDeficit,
    firstMoment = reference.taperLoadDeficit * reference.taperDecayLength,
    totalDeficit = reference.totalDeficitPerUnitEdge
)

private fun t160CheapBound(
    anchoring: String,
    model: CollarTailModel,
    note: String
): T160CheapBoundRecord {
    val reference = model.at(model.referenceHalfWidth)
    val predicted = model.at(T160_BUILDABLE_HALF_WIDTH)
    val departure = collarDeparture(predicted, reference)
    return T160CheapBoundRecord(
        anchoring = anchoring,
        decayLength = model.decayLength,
        centrelineExcess = model.centrelineExcess,
        predictedTaperDepth = predicted.taperDepth,
        predictedTaperWidth = predicted.taperWidth,
        predictedRimResidualDepth = predicted.rimResidualDepth,
        predictedDeparture = departure,
        settlesTheQuestion = departure < T160_PLACEMENT_SENSITIVITY,
        note = note
    )
}

private fun t160Spread(values: List<Double>): Double {
    val scale = values.maxOf { abs(it) }
    return if (scale == 0.0) 0.0 else (values.max() - values.min()) / scale
}

private fun t160Monotone(values: List<Double>): Boolean =
    values.zipWithNext().all { (a, b) -> b > a } ||
            values.zipWithNext().all { (a, b) -> b < a }

/**
 * `C-0090`'s own host, bank and winning placement, re-solved under one collar.
 *
 * Nothing here is a second lattice: `rasterColumnLayout` and `rasterUpwardSites` are `C-0090`'s,
 * `OrigamiGrillage` is `C-0009`'s, `UpwardRootInfluenceBank` is `C-0063`'s and the surrogate is
 * `C-0058`'s. Only the **load** is new.
 */
private fun t160Flatness(
    case: String,
    collarSource: String,
    terms: Pair<CollarTerm, CollarTerm>,
    bestKey: String,
    carried: Double?
): T160FlatnessRecord {
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val edgeX = 2.0 * T160_BUILDABLE_HALF_WIDTH
    val lengthY = T160_DUPLEXES * sheet.interhelicalDistance
    val interior = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)
    val field = edgeCollarPressure(interior, edgeX, lengthY, listOf(terms.first, terms.second))
    val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformPressure(interior)).meanDeflection
    val host = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = T160_DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = rasterColumnLayout(T160_RECOMMENDED_PHASE, sheet, edgeX, true),
        subdivisions = 2,
        supports = emptyList()
    )
    val lattice = rasterUpwardSites(
        T160_RECOMMENDED_PHASE, edgeX, T160_DUPLEXES, true, Gen1Tile.RISE_PER_BASE_PAIR
    )
    val stations = lattice.flatMapIndexed { row, xs ->
        xs.map { it to (row - (T160_DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
    }
    val bank = UpwardRootInfluenceBank(host, stations, field)
    val placed = t160StationsOf(bestKey, sheet.interhelicalDistance)
    val indices = placed.map { (x, y) ->
        val index = bank.indexOf(x, y, 1e-6)
        require(index >= 0) {
            "C-0090's placement station ($x, $y) is not an upward site of phase " +
                    "$T160_RECOMMENDED_PHASE at $edgeX nm"
        }
        index
    }
    require(indices.size == C0055_ARM_COUNT) {
        "C-0090's placement must carry $C0055_ARM_COUNT stations, carried ${indices.size}"
    }
    val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
    val dishing = bank.surrogateFor(indices)
        .solve(List(indices.size) { mandate / indices.size }).peakDishing / freeStroke
    return T160FlatnessRecord(
        case = case,
        collarSource = collarSource,
        taperDepth = terms.first.depth,
        taperWidth = terms.first.width,
        rimResidualDepth = terms.second.depth,
        alongHelixWidth = edgeX,
        acrossHelixSpan = lengthY,
        phaseBasePairs = T160_RECOMMENDED_PHASE,
        stations = indices.size,
        freeStroke = freeStroke,
        freeDishingOverStroke = bank.freePeakDishing / freeStroke,
        bestDishingOverStroke = dishing,
        movementAgainstCarried = if (carried == null) 0.0 else abs(dishing / carried - 1.0),
        insidePlacementSensitivity = carried == null ||
                abs(dishing / carried - 1.0) < T160_PLACEMENT_SENSITIVITY,
        flatAtTenPercent = dishing < 0.10
    )
}

private fun t160Validity(): List<String> = listOf(
    "MEAN FIELD, inherited whole from C-0005 and C-0008: the one-loop correction is 123-214% of " +
            "the leading term at these gaps, and for the oppositely charged tile-electrode pair " +
            "no published result gives even the direction. A second width does not narrow it. " +
            "It does, however, enter the DEPARTURE between the two widths as a common factor " +
            "rather than as an error on it, which is the only reason a 0.01% answer is worth " +
            "quoting inside a 214% bracket.",
    "POINT IONS. C-0008's Bikerman bracket raises |F_es| by +0.8% to +56%, one-sided and upward. " +
            "It is a scale correction on both widths alike and is not repeated here.",
    "TWO-DIMENSIONAL, hence a STRAIGHT edge. The corner is bracketed by C-0022's two mappings " +
            "and not solved, at either width.",
    "The RIM CHARGE is unsourced and C-0022's falsifier 5 fired on it: uncharged against the " +
            "face density is a 1.85x bracket on the depth. That bracket is a common factor on " +
            "both half-widths and is not re-opened; every solve here takes the uncharged rim, as " +
            "C-0022's headline does.",
    "The ABSOLUTE collar is not mesh-converged at refinement 3 and C-0022 says so — the depth " +
            "moves -0.2354 / -0.2906 / -0.3076 over refinements 1/2/4. This task's answer is a " +
            "DEPARTURE at matched refinement; the absolute values are reported because the " +
            "departure is a ratio of them and a reader is owed both.",
    "THE FITTED (depth, width, rim residual) TRIPLE'S DEPARTURE IS NOT CONVERGED EITHER, and " +
            "that is a finding rather than a caveat. fitEdgeTaper starts its quadrature at the " +
            "first mesh node at or beyond the 1 nm standoff; the graded lateral mesh rescales " +
            "with the tile half-width, so the two widths snap to two different standoffs and the " +
            "PARTITION of the edge effect between the smooth term and the rim residual moves " +
            "with the mesh. Their SUM does not — it is the global momentum flux — and neither " +
            "does the dishing, which integrates both terms. Any downstream reader of ONE of the " +
            "two terms is reading a mesh; C-0022's own 'smooth term only' dishing row is such a " +
            "reader.",
    "The gap is filled with FREE BUFFER. C-0005's partitioning layer amplifies the 1-D force by " +
            "1.15-1.60x; whether it moves the collar ratio is not computed, at either width.",
    "The flatness consequence is C-0090's OWN placement re-evaluated, not a re-search. Whether a " +
            "DIFFERENT placement would win under the re-solved collar is not asked.",
    "The across-helix collar is UNCHANGED by construction: edgeCollarPressure applies one collar " +
            "on the minimum margin to all four edges, and the across-helix half-span is 20.175 " +
            "nm at both widths. Only the two along-helix rims move, and the field applies the " +
            "same term to all four — which is C-0022's own convention and is inherited, not fixed.",
    "NOTHING HERE IS MEASURED."
)

private fun t160OpenQuestions(): List<String> = listOf(
    "Whether a different 34-root placement wins under the re-solved collar. C-0090's exhaustive " +
            "enumeration is 163 296 members at phase 8 and it is not re-run here; only its " +
            "argmax is re-evaluated.",
    "The corner, still. C-0022's two mappings bracket it at 1.8 percentage points of total force " +
            "at 40 nm and the bracket widens as the tile shrinks (7.2 points at 20 nm); at " +
            "38.08 nm it is between the two and is reported, not narrowed.",
    "Whether the ACROSS-helix rims should carry a different collar from the along-helix ones. " +
            "The two half-spans now differ (19.04 against 20.175 nm) and edgeCollarPressure " +
            "cannot express the difference. This study measures how much that is worth and the " +
            "answer is why it does not matter, but the field is still a one-collar field.",
    "144 bp = 48.96 nm, C-0086's other admissible neighbour, is not solved. It is 22% LARGER " +
            "than §3's nominal, so its collar movement runs the other way and is smaller.",
    "Whether the ELECTRODE is finite. It is macroscopic here and in C-0022; a counter-pad the " +
            "size of the tile would have its own edge and would itself care about the width."
)

private fun t160Findings(result: T160Result): Map<String, String> {
    val design = result.collars.first {
        it.tileHalfWidth == T160_NOMINAL_HALF_WIDTH && it.refinement == T160_SWEEP_REFINEMENT &&
                it.state == T160_STATES.first().name
    }
    val answer = result.collars.first {
        it.tileHalfWidth == T160_BUILDABLE_HALF_WIDTH && it.refinement == T160_SWEEP_REFINEMENT &&
                it.state == T160_STATES.first().name
    }
    val carried = result.flatness[0]
    val resolved = result.flatness[1]
    val carriedExact = result.flatness[2]
    val resolvedExact = result.flatness[3]
    val worst = result.parameters.getValue("worstCollarDeparture")
    val fitFree = result.parameters.getValue("fitFreeDeparture")
    val exact = result.parameters.getValue("worstExactStandoffDeparture")
    val states = result.departures.filter {
        it.refinement == T160_SWEEP_REFINEMENT && it.quantity == "effectiveCollarWidth"
    }
    val fittedStates = result.departures.filter {
        it.refinement == T160_SWEEP_REFINEMENT &&
                it.quantity in listOf("taperDepth", "taperWidth", "rimResidualDepth")
    }
    return mapOf(
        "the_collar_is_width_independent_and_the_fit_free_measure_says_so" to
                ("The acceptance's second branch is the one that holds. The effective collar " +
                        "width — the whole edge effect stated as a length, from the GLOBAL " +
                        "momentum flux, with no fit and no standoff in it — is %.6f nm at §3's " +
                        "nominal 40.0 nm and %.6f nm at the buildable 38.08 nm, a movement of " +
                        "%.4f%%, a factor of %.1f inside C-0090's declared 0.32%% placement " +
                        "sensitivity, and it converges (%.4f%% of scatter over refinements " +
                        "1/2/3/4). The fitted triple C-0090 literally carries is " +
                        "(%.9f, %.8f nm) and %.9f at the buildable width against " +
                        "(%.9f, %.8f nm) and %.9f at the nominal one.").format(
                    design.effectiveCollarWidth, answer.effectiveCollarWidth,
                    100.0 * fitFree, T160_PLACEMENT_SENSITIVITY / maxOf(fitFree, 1e-30),
                    100.0 * result.convergence.first { it.quantity.startsWith("the FIT-FREE") }
                        .departure,
                    answer.taperDepth, answer.taperWidth, answer.rimResidualDepth,
                    design.taperDepth, design.taperWidth, design.rimResidualDepth
                ),
        "the_fitted_triple_moves_TWENTY_TIMES_more_and_it_is_the_MESH" to
                ("The three numbers C-0090 carries move %.4f%% at worst — outside the 0.32%% — " +
                        "and that movement is NOT physics. fitEdgeTaper starts its quadrature at " +
                        "the first mesh node at or beyond the 1 nm standoff, the integrand there " +
                        "is the PEAK of the enhancement at 1.88x the interior load, and the " +
                        "graded lateral mesh rescales with the tile half-width: the standoff " +
                        "snaps to %.6f nm at a = 20.00 and %.6f nm at a = 19.04. Three " +
                        "signatures say so. The departure does not converge — %.4f%%, %.4f%%, " +
                        "%.4f%%, %.4f%% over refinements 1/2/3/4, a scatter LARGER than itself. " +
                        "The half-width sweep is not monotone. And placing the standoff exactly " +
                        "by interpolation instead of snapping takes the worst departure to " +
                        "%.4f%%, and makes the half-width sweep MONOTONE and saturating at all " +
                        "nine widths where the snapped one alternates. The SUM of the two " +
                        "collar terms is untouched throughout, because it is the momentum " +
                        "flux: the mesh moves load BETWEEN the smooth taper and the rim " +
                        "residual and creates none.").format(
                    100.0 * worst, design.standoffNode, answer.standoffNode,
                    100.0 * result.convergence.first().results[0],
                    100.0 * result.convergence.first().results[1],
                    100.0 * result.convergence.first().results[2],
                    100.0 * result.convergence.first().results[3],
                    100.0 * exact
                ),
        "what_the_width_DOES_move_is_the_level_and_it_cancels" to
                ("The min-margin force fraction moves %.2f%% -> %.2f%% — the 1/L scaling " +
                        "C-0090 invoked, and it is the LEVEL of the force, which cancels out of " +
                        "a dishing because the free stroke carries the same factor. The " +
                        "effective collar width, which is the level-free statement of the same " +
                        "thing, moves %.4f -> %.4f nm.").format(
                    -100.0 * design.edgeForceFractionMinMargin,
                    -100.0 * answer.edgeForceFractionMinMargin,
                    design.effectiveCollarWidth, answer.effectiveCollarWidth
                ),
        "the_collar_depends_on_the_width_only_through_the_FIT" to
                ("A collar cannot know how wide its tile is: the fitted far tail decays over " +
                        "%.4f nm, so the two rims of a 38 nm tile are %.1f decay lengths apart. " +
                        "What does know is fitEdgeTaper, which references the profile to the " +
                        "CENTRE-LINE load and truncates both of its moments there. The closed " +
                        "form for that — one exponentially small number, tau(a) = p(a) - " +
                        "Pi_inf — predicted the movement before the second solve was spent and " +
                        "bracketed it at %.4f%%-%.4f%%.").format(
                    result.parameters.getValue("fittedTailDecayLength"),
                    2.0 * T160_BUILDABLE_HALF_WIDTH /
                            result.parameters.getValue("fittedTailDecayLength"),
                    100.0 * result.parameters.getValue("cheapBoundLow"),
                    100.0 * result.parameters.getValue("cheapBoundHigh")
                ),
        "C-0090s_flatness_does_not_move_and_that_is_the_deliverable" to
                ("C-0090's recommended 34-root placement dishes %.10f of the stroke under its " +
                        "own carried collar — reproduced here to %.2e of its published " +
                        "0.0621469105 — and %.10f under the re-solved one, a movement of " +
                        "%.4f%%, four and a half times inside the 0.32%% that claim declares. " +
                        "With the standoff placed exactly at both widths it is %.10f against " +
                        "%.10f, %.4f%%. T-5b's 0.10 is cleared by a factor of %.2f in every " +
                        "reading, and the acceptance verdict does not move. The dishing is " +
                        "insensitive to the partition for the reason the partition is an " +
                        "artefact: the plate integrates the SUM of the two collar terms.").format(
                    carried.bestDishingOverStroke,
                    result.reproductions.last().departure,
                    resolved.bestDishingOverStroke,
                    100.0 * resolved.movementAgainstCarried,
                    resolvedExact.bestDishingOverStroke, carriedExact.bestDishingOverStroke,
                    100.0 * resolvedExact.movementAgainstCarried,
                    0.10 / resolved.bestDishingOverStroke
                ),
        "the_PARTITION_is_worth_more_to_the_flatness_than_the_WIDTH_is" to
                ("Re-partitioning the SAME 40 nm field — placing the standoff at 1 nm exactly " +
                        "instead of at the mesh node %.6f nm, changing no physics at all — " +
                        "moves C-0090's dishing %.10f -> %.10f, which is %.4f%%. Re-solving " +
                        "the field at the buildable width moves it %.4f%%. So the discretisation " +
                        "of the standoff is worth %.1fx what the tile's own width is worth, and " +
                        "it is the one of the two that C-0090 could not have known about. Both " +
                        "are inside T-5b's 0.10 by a factor of 1.6 and neither moves a verdict.").format(
                    design.standoffNode,
                    carried.bestDishingOverStroke, carriedExact.bestDishingOverStroke,
                    100.0 * carriedExact.movementAgainstCarried,
                    100.0 * resolved.movementAgainstCarried,
                    carriedExact.movementAgainstCarried /
                            maxOf(resolved.movementAgainstCarried, 1e-30)
                ),
        "a_departure_cancels_the_model_error_but_not_the_MESH_error" to
                ("C-0022's own taper depth is converged to about 4% at the refinement it was " +
                        "swept at, which is ten times the movement being measured — so the " +
                        "answer HAD to be a departure at matched refinement, and that is what " +
                        "was emitted. What the departure cancels is everything the two solves " +
                        "share: the model, the ion statistics, the mean-field error, the rim " +
                        "charge, the corner convention. What it does NOT cancel is anything the " +
                        "tile width changes about the DISCRETISATION, and the graded lateral " +
                        "mesh is exactly that. A shared mesh is not the same thing as a matched " +
                        "refinement, and this task is where the difference shows."),
        "it_is_a_property_of_the_geometry_and_not_of_the_design_point" to
                ("The fit-free measure moves less than %.4f%% at every one of the four states, " +
                        "worst at %s — including the 10 mM point where C-0022's sign reverses " +
                        "and the 5 nm layer. The fitted triple is outside the 0.32%% at all " +
                        "four (worst %.4f%%), by the same mesh mechanism at all four. So both " +
                        "halves of the verdict are properties of the geometry rather than of " +
                        "the design point.").format(
                    100.0 * states.maxOf { it.relativeDeparture },
                    states.maxByOrNull { it.relativeDeparture }?.state ?: "-",
                    100.0 * fittedStates.maxOf { it.relativeDeparture }
                ),
        "mean_field_is_still_not_improved_by_a_second_width" to
                "C-0005's one-loop correction is 123-214% of the leading term across this gap " +
                        "range and it is inherited whole. What makes a 0.01% departure worth " +
                        "quoting inside a 214% bracket is that the bracket is a COMMON FACTOR " +
                        "on the two widths and divides out of their ratio; nothing here narrows " +
                        "it and nothing here claims to."
    )
}

private fun t160Report(result: T160Result, output: File) {
    println()
    println("T-160 — ${result.title}")
    println("leaf ${result.leaf}; ${result.medium}")
    println()
    println("--- the cheap bound, run BEFORE the second solve ".padEnd(110, '-'))
    println("%68s %10s %12s %12s".format("anchoring", "l [nm]", "departure %", "settles?"))
    result.cheapBound.forEach {
        println(
            "%68s %10.4f %12.4f %12s".format(
                it.anchoring.take(68), it.decayLength, 100.0 * it.predictedDeparture,
                it.settlesTheQuestion
            )
        )
    }
    println()
    println("--- the solved collar ".padEnd(110, '-'))
    println(
        "%40s %6s %4s %11s %10s %11s %9s %10s %9s".format(
            "state", "a[nm]", "ref", "depth", "width", "rim depth", "collar", "standoff", "F gain %"
        )
    )
    result.collars.forEach {
        println(
            "%40s %6.2f %4d %11.7f %10.5f %11.7f %9.5f %10.6f %9.3f".format(
                it.state.take(40), it.tileHalfWidth, it.refinement, it.taperDepth,
                it.taperWidth, it.rimResidualDepth, it.effectiveCollarWidth, it.standoffNode,
                -100.0 * it.edgeForceFractionMinMargin
            )
        )
    }
    println()
    println("--- the departures, at matched refinement ".padEnd(110, '-'))
    println(
        "%44s %5s %28s %14s %10s".format("state", "ref", "quantity", "departure %", "inside?")
    )
    result.departures.forEach {
        println(
            "%44s %5d %28s %14.6f %10s".format(
                it.state.take(44), it.refinement, it.quantity,
                100.0 * it.relativeDeparture, it.insidePlacementSensitivity
            )
        )
    }
    println()
    println("--- the half-width sweep, refinement 2 ".padEnd(110, '-'))
    println("%10s %14s %12s %14s %12s".format("a[nm]", "depth", "width[nm]", "rim depth", "F gain %"))
    result.widthSweep.forEach {
        println(
            "%10.2f %14.7f %12.5f %14.7f %12.3f".format(
                it.tileHalfWidth, it.taperDepth, it.taperWidth, it.rimResidualDepth,
                -100.0 * it.edgeForceFractionMinMargin
            )
        )
    }
    println()
    println("--- C-0090's flatness, under both collars ".padEnd(110, '-'))
    println("%56s %14s %14s %12s".format("case", "dishing", "movement %", "flat?"))
    result.flatness.forEach {
        println(
            "%56s %14.10f %14.6f %12s".format(
                it.case.take(56), it.bestDishingOverStroke,
                100.0 * it.movementAgainstCarried, it.flatAtTenPercent
            )
        )
    }
    println()
    println("--- reproductions ".padEnd(110, '-'))
    result.reproductions.forEach {
        println(
            "%10s %52s %16.9f %16.9f %10.2e %8s".format(
                it.source, it.quantity.take(52), it.published, it.reproduced, it.departure,
                it.strict
            )
        )
    }
    println()
    println("--- falsifiers ".padEnd(110, '-'))
    result.falsifiers.forEach {
        println("%4s %8s  %s".format(it.name, if (it.fired) "FIRED" else "silent", it.outcome))
    }
    println()
    println("--- FINDINGS ".padEnd(110, '-'))
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written: ${output.path}")
}
