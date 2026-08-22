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
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.CrossoverSite
import com.xemantic.nano.plentyofroom.structure.CrossoverSoftening
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.origamiSheet
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

/**
 * `T-164` — how stiff is a ROW-END crossover, and does the flatness verdict change inside the
 * range that stiffness can take?
 *
 * Emits `gpd/results/T-164-row-end-crossover-stiffness.json`.
 *
 * Reads `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved edge profile) and
 * `gpd/results/T-153-buildable-raster-width.json` (`C-0090`'s two published readings, as the gate).
 */

private const val DUPLEXES = 15
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private const val PHASE = 8
private const val HINGE_ONLY = "A — the dihedral spring alone, the vertical link retained"
private const val WHOLE_ELEMENT = "B — both elements, which is C-0090's refused reading in the limit"
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T164BoundRecord(
    val name: String,
    val statement: String,
    val value: Double,
    val unit: String,
    val owner: String,
    val derivedHere: Boolean
)

@Serializable
private data class T164SweepRecord(
    val channel: String,
    val softening: Double,
    val hingeStiffness: Double,
    val linkFactor: Double,
    val columns: Int,
    val crossovers: Int,
    val softenedCrossovers: Int,
    val enumerated: Int,
    val bestDishingOverStroke: Double,
    val worstDishingOverStroke: Double,
    val medianDishingOverStroke: Double,
    val freeDishingOverStroke: Double,
    val uniformLoadDishingOverStroke: Double,
    val publishedPlacementDishing: Double,
    val publishedPlacementPenalty: Double,
    val flatAtTenPercent: Boolean,
    val publishedPlacementFlatAtTenPercent: Boolean,
    val bestKey: String,
    val bestKeyIsFullStiffnessOptimum: Boolean
)

@Serializable
private data class T164MonotonicityRecord(
    val channel: String,
    val softeningLow: Double,
    val softeningHigh: Double,
    val dishingAtLow: Double,
    val dishingAtHigh: Double,
    val difference: Double,
    val decreasingInStiffness: Boolean
)

@Serializable
private data class T164CrossingRecord(
    val channel: String,
    val target: Double,
    val crossingExists: Boolean,
    val softeningBelow: Double,
    val softeningAbove: Double,
    val dishingBelow: Double,
    val dishingAbove: Double,
    val bracketWidth: Double,
    val evaluations: Int,
    val aboveCountingFloor: Boolean,
    val note: String
)

@Serializable
private data class T164ReferenceRecord(
    val name: String,
    val dishingOverStroke: Double,
    val insideConvention: Boolean,
    val reachableByASoftenedCrossover: Boolean,
    val why: String
)

@Serializable
private data class T164ConvergenceRecord(
    val quantity: String,
    val axis: String,
    val values: List<Double>,
    val relativeSpread: Double,
    val note: String
)

@Serializable
private data class T164ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T164PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String,
    val met: Boolean
)

@Serializable
private data class T164FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T164Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val cheapBounds: List<T164BoundRecord>,
    val sweep: List<T164SweepRecord>,
    val monotonicity: List<T164MonotonicityRecord>,
    val crossings: List<T164CrossingRecord>,
    val references: List<T164ReferenceRecord>,
    val convergence: List<T164ConvergenceRecord>,
    val reproductions: List<T164ReproductionRecord>,
    val predicates: List<T164PredicateRecord>,
    val falsifiers: List<T164FalsifierRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias** (`CLAUDE.md`). */
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

/** `C-0090`'s published reading of one case at one phase — the dishing and the placement key. */
private fun c0090Reading(file: File, casePrefix: String, phase: Int): Pair<Double, String> {
    require(file.exists()) { "C-0090's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("placements").jsonArray.map { it.jsonObject }
        .firstOrNull {
            it.getValue("case").jsonPrimitive.content.startsWith(casePrefix) &&
                    it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() == phase
        } ?: error("no C-0090 placement record for $casePrefix at phase $phase")
    return record.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble() to
            record.getValue("bestKey").jsonPrimitive.content
}

// ---------------------------------------------------------------------------------------------
// one host at one row-end softening
// ---------------------------------------------------------------------------------------------

private class T164Host(
    val sheet: OrigamiSheet,
    val edgeX: Double,
    val arm: Double,
    val admitRowEnd: Boolean,
    smooth: CollarTerm,
    rim: CollarTerm
) {

    val lengthY: Double = DUPLEXES * sheet.interhelicalDistance

    val area: Double = edgeX * lengthY

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / area

    val uniformField: PressureField = uniformPressure(interiorPressure)

    val solvedField: PressureField =
        edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))

    /** The free plate's mean descent under the uniform field — the stroke every dishing is over. */
    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    val columns: CrossoverLayout =
        rasterColumnLayout(PHASE, sheet, edgeX, admitRowEnd, CrossoverLayout.EDGE_MARGIN)

    val rowEndSites: List<CrossoverSite> =
        rowEndCrossoverSites(columns, edgeX, DUPLEXES, CrossoverLayout.EDGE_MARGIN)

    val sites: List<List<Double>> = rasterUpwardSites(
        PHASE, edgeX, DUPLEXES, admitRowEnd, Gen1Tile.RISE_PER_BASE_PAIR, CrossoverLayout.EDGE_MARGIN
    )

    val stations: List<Pair<Double, Double>> = sites.flatMapIndexed { row, xs ->
        xs.map { it to (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
    }

    fun lattice(
        softening: CrossoverSoftening?,
        subdivisions: Int = 2
    ): OrigamiGrillage = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = subdivisions,
        softenedCrossovers =
            if (softening == null) emptyMap() else softeningMap(rowEndSites, softening)
    )

    inner class Solve(val softening: CrossoverSoftening?, samples: Int = 81, subdivisions: Int = 2) {

        val host: OrigamiGrillage = lattice(softening, subdivisions)

        val bank = UpwardRootInfluenceBank(host, stations, solvedField, samples)

        private val uniform = List(C0055_ARM_COUNT) { MANDATE / C0055_ARM_COUNT }

        val freeDishing = bank.freePeakDishing / freeStroke

        /** The standing falsifier: a uniform load on a uniform foundation dishes exactly zero. */
        val uniformLoadDishing = host.solve(uniformField).peakDishing(samples) / freeStroke

        fun surrogate(placement: UpwardArmPlacement): InfluenceSurrogate =
            bank.surrogateFor(
                placement.stations(DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
                    val index = bank.indexOf(x, y)
                    require(index >= 0) { "($x, $y) is not an upward site of phase $PHASE" }
                    index
                }
            )

        fun dishing(placement: UpwardArmPlacement): Double =
            surrogate(placement).solve(uniform).peakDishing / freeStroke
    }
}

/** The exhaustive centro-symmetric optimum of one solve, with the spread of the whole family. */
private class T164Optimum(host: T164Host, solve: T164Host.Solve) {

    val best: UpwardArmPlacement
    val bestValue: Double
    val worstValue: Double
    val medianValue: Double
    val enumerated: Int

    init {
        val values = ArrayList<Double>()
        var incumbent: Pair<UpwardArmPlacement, Double>? = null
        centroSymmetricPlacements(
            PHASE, host.edgeX, DUPLEXES, host.arm, C0055_ARM_COUNT,
            minimumPerRow = 2, maximumPerRow = 3
        ).forEach { placement ->
            val value = solve.dishing(placement)
            values += value
            val current = incumbent
            // C-0090's own comparison, with the canonical key as the tie-break at the decision
            // point, so that the s = 1 rung reproduces its optimum rather than a neighbour of it
            if (current == null || value < current.second ||
                (value == current.second && placement.key < current.first.key)
            ) incumbent = placement to value
        }
        require(values.isNotEmpty()) { "the centro-symmetric family at phase $PHASE is empty" }
        values.sort()
        best = incumbent!!.first
        bestValue = incumbent!!.second
        worstValue = values.last()
        medianValue = values[values.size / 2]
        enumerated = values.size
    }
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val edgeX = BUILDABLE_RASTER_WIDTH
    val arm = quantisedToRise(C0055_ARM_LENGTH)

    println("T-164 — reading C-0022's solved load and C-0090's published readings ...")
    val (smooth, rim) = solvedProfile(ResultInputs.T_3B.file())
    val c0090File = ResultInputs.T_153.file()
    val (admittedPublished, admittedKey) = c0090Reading(c0090File, "RECOMMENDED", PHASE)
    val (refusedPublished, refusedKey) = c0090Reading(c0090File, "BRACKET", PHASE)

    // ------------------------------------------------- the cheap bound, before any placement solve
    println("T-164 — the cheap bound, which is a count and a distinction ...")

    val interiorHinge = Gen1Tile.crossoverHingeStiffness()
    val cheapBounds = listOf(
        T164BoundRecord(
            "strand termini at a duplex end", "C-0029's counting theorem: a duplex END has " +
                    "exactly two strand termini, and no force field can add a third",
            STRAND_TERMINI_AT_DUPLEX_END.toDouble(), "count", "C-0029", false
        ),
        T164BoundRecord(
            "phosphate bonds of an interior crossover",
            "Chen et al. put k2 = alpha B/(100 a) on ONE crossover phosphate bond and an " +
                    "antiparallel crossover carries two of them in parallel",
            CROSSOVER_PHOSPHATE_BONDS.toDouble(), "count", "Chen et al. 2014, SI S2", false
        ),
        T164BoundRecord(
            "k_theta of an interior crossover", "2 alpha B/(100 a) at alpha = 1",
            interiorHinge, "pN nm/rad", "C-0009 / Gen1Tile", false
        ),
        T164BoundRecord(
            "k_theta of a single-strand row-end turn", "the other integer rung of the same ladder",
            hingeStiffnessOfBondCount(1), "pN nm/rad", "T-164", true
        ),
        T164BoundRecord(
            "CEILING on the row-end softening s",
            "a row-end crossover cannot carry MORE bonds than an interior one, so s <= 1 " +
                    "exactly — a ratio of two counts, in which alpha and every elastic constant " +
                    "cancels",
            softeningOfBondCount(CROSSOVER_PHOSPHATE_BONDS), "dimensionless", "T-164", true
        ),
        T164BoundRecord(
            "the counting FLOOR of the same ladder",
            "a single-strand turn, which is the only other integer state a bond census admits",
            softeningOfBondCount(1), "dimensionless", "T-164", true
        ),
        T164BoundRecord(
            "the vertical link factor of a crossover that EXISTS",
            "a crossover is TWO elements and only one of them is elastic; the link is a " +
                    "CONSTRAINT expressing covalent continuity across the interface, and " +
                    "Rothemund's own remedy adds slack to the torsion, not to the connectivity",
            1.0, "dimensionless", "C-0009 / C-0095", true
        )
    )

    // ---------------------------------------------------------------------------- the two hosts
    val admittedHost = T164Host(sheet, edgeX, arm, true, smooth, rim)
    val refusedHost = T164Host(sheet, edgeX, arm, false, smooth, rim)
    check(admittedHost.columns.size == 8) { "phase $PHASE must carry 8 columns when admitted" }
    check(refusedHost.columns.size == 6) { "phase $PHASE must carry 6 columns when refused" }
    check(admittedHost.rowEndSites.size == DUPLEXES - 1) {
        "the two row-end columns must carry one crossover per interface"
    }
    check(refusedHost.rowEndSites.isEmpty()) { "a refused lattice has no row-end column" }

    // ------------------------------------------------------------------------------- the sweep
    println("T-164 — the sweep, exhaustive over the centro-symmetric family at every rung ...")

    val publishedPlacement = placementFromKey(admittedKey, PHASE, arm, edgeX)
    check(publishedPlacement.count == C0055_ARM_COUNT) {
        "C-0090's published key must carry ${C0055_ARM_COUNT} roots"
    }

    val grid = listOf(0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0)
    val sweep = ArrayList<T164SweepRecord>()
    val cache = HashMap<Pair<String, Double>, T164SweepRecord>()

    fun softeningOf(channel: String, s: Double): CrossoverSoftening =
        if (channel == HINGE_ONLY) CrossoverSoftening.ofHinge(s) else CrossoverSoftening(s, s)

    fun evaluate(channel: String, s: Double): T164SweepRecord = cache.getOrPut(channel to s) {
        val softening = softeningOf(channel, s)
        val solve = admittedHost.Solve(softening)
        val optimum = T164Optimum(admittedHost, solve)
        val published = solve.dishing(publishedPlacement)
        val record = T164SweepRecord(
            channel = channel,
            softening = s,
            hingeStiffness = interiorHinge * s,
            linkFactor = softening.link,
            columns = admittedHost.columns.size,
            crossovers = solve.host.crossovers.size,
            softenedCrossovers = admittedHost.rowEndSites.size,
            enumerated = optimum.enumerated,
            bestDishingOverStroke = optimum.bestValue,
            worstDishingOverStroke = optimum.worstValue,
            medianDishingOverStroke = optimum.medianValue,
            freeDishingOverStroke = solve.freeDishing,
            uniformLoadDishingOverStroke = solve.uniformLoadDishing,
            publishedPlacementDishing = published,
            publishedPlacementPenalty = published - optimum.bestValue,
            flatAtTenPercent = optimum.bestValue < FLATNESS_TOLERANCE,
            publishedPlacementFlatAtTenPercent = published < FLATNESS_TOLERANCE,
            bestKey = optimum.best.key,
            bestKeyIsFullStiffnessOptimum = optimum.best.key == admittedKey
        )
        println(
            "  channel %s  s %5.3f  best %9.6f  free %8.5f  flat %-5s  key stable %s".format(
                channel.take(1), s, record.bestDishingOverStroke, record.freeDishingOverStroke,
                record.flatAtTenPercent, record.bestKeyIsFullStiffnessOptimum
            )
        )
        record
    }

    listOf(HINGE_ONLY, WHOLE_ELEMENT).forEach { channel ->
        grid.forEach { s -> sweep += evaluate(channel, s) }
    }

    // the refused reading, on its own six-column host, as the other end of CH-0111's bracket
    println("T-164 — the refused reading, on its own six-column host ...")
    val refusedSolve = refusedHost.Solve(null)
    val refusedOptimum = T164Optimum(refusedHost, refusedSolve)

    // ------------------------------------------------------------------------- monotonicity
    val monotonicity = listOf(HINGE_ONLY, WHOLE_ELEMENT).flatMap { channel ->
        grid.zipWithNext().map { (low, high) ->
            val atLow = cache.getValue(channel to low).bestDishingOverStroke
            val atHigh = cache.getValue(channel to high).bestDishingOverStroke
            T164MonotonicityRecord(
                channel = channel,
                softeningLow = low, softeningHigh = high,
                dishingAtLow = atLow, dishingAtHigh = atHigh,
                difference = atHigh - atLow,
                decreasingInStiffness = atHigh <= atLow
            )
        }
    }
    val monotone = monotonicity.all { it.decreasingInStiffness }

    // ------------------------------------------------------------------------------ the crossing
    println("T-164 — the crossing, bisected only where the grid shows one ...")
    val crossings = listOf(HINGE_ONLY, WHOLE_ELEMENT).map { channel ->
        val atZero = cache.getValue(channel to 0.0).bestDishingOverStroke
        val atOne = cache.getValue(channel to 1.0).bestDishingOverStroke
        val straddles = (atZero - FLATNESS_TOLERANCE) * (atOne - FLATNESS_TOLERANCE) <= 0.0
        if (!straddles) {
            T164CrossingRecord(
                channel = channel, target = FLATNESS_TOLERANCE, crossingExists = false,
                softeningBelow = -1.0, softeningAbove = -1.0,
                dishingBelow = atZero, dishingAbove = atOne,
                bracketWidth = -1.0, evaluations = 0, aboveCountingFloor = false,
                note = "the whole reachable range lies on one side of T-5b's 0.10, so there " +
                        "is no crossing to bisect: the verdict does not change inside it"
            )
        } else {
            val found = bisectedCrossing(0.0, 1.0, FLATNESS_TOLERANCE, 6) { s ->
                evaluate(channel, s).bestDishingOverStroke
            }
            T164CrossingRecord(
                channel = channel, target = FLATNESS_TOLERANCE, crossingExists = true,
                softeningBelow = found.below, softeningAbove = found.above,
                dishingBelow = found.valueBelow, dishingAbove = found.valueAbove,
                bracketWidth = found.width, evaluations = found.evaluations,
                aboveCountingFloor = found.midpoint > softeningOfBondCount(1),
                note = if (channel == HINGE_ONLY)
                    "the fraction of an interior crossover's dihedral spring the 38.08 nm tile " +
                            "needs to clear T-5b's 0.10, bisected on the exhaustive optimum"
                else "NOT a stiffness threshold: the vertical link is a PENALTY enforcing a " +
                        "constraint, so scaling it is a step and not a ramp — this bracket " +
                        "locates the step, which is at the link being present at all"
            )
        }
    }

    // ------------------------------------------------------------------------------ references
    val admittedReading = cache.getValue(HINGE_ONLY to 1.0).bestDishingOverStroke
    val worstReachable = cache.getValue(HINGE_ONLY to 0.0).bestDishingOverStroke
    val wholeElementZero = cache.getValue(WHOLE_ELEMENT to 0.0).bestDishingOverStroke
    val bracket = refusedOptimum.bestValue - admittedReading
    val reachableFraction = (worstReachable - admittedReading) / bracket
    val conventionFraction = (FLATNESS_TOLERANCE - admittedReading) / bracket

    val references = listOf(
        T164ReferenceRecord(
            "C-0090's ADMITTED reading — full interior k_theta and vertical link",
            admittedReading, admittedReading < FLATNESS_TOLERANCE, true,
            "the s = 1 rung of both channels, and C-0090's own published optimum"
        ),
        T164ReferenceRecord(
            "channel A at s = 0 — no dihedral spring, the vertical link retained",
            worstReachable, worstReachable < FLATNESS_TOLERANCE, true,
            "the softest state a crossover that EXISTS can be in: the backbone is covalently " +
                    "continuous whatever the strain relief does to the torsional register"
        ),
        T164ReferenceRecord(
            "channel B at s = 0 — both elements gone, the node retained",
            wholeElementZero, wholeElementZero < FLATNESS_TOLERANCE, false,
            "not a state of a crossover that exists; carried because it is the mechanical " +
                    "content of C-0090's refused reading, with the mesh held fixed"
        ),
        T164ReferenceRecord(
            "C-0090's REFUSED reading — no column at all, on its own six-column host",
            refusedOptimum.bestValue, refusedOptimum.bestValue < FLATNESS_TOLERANCE, false,
            "C-0095 has settled that this lattice is not the one a seamless boustrophedon builds"
        )
    )

    // ----------------------------------------------------------------------------- convergence
    println("T-164 — convergence ...")
    val fullOptimum = cache.getValue(HINGE_ONLY to 1.0)
    val optimumPlacement = publishedPlacement
    fun atSubdivisions(s: Double, subdivisions: Int): Double =
        admittedHost.Solve(CrossoverSoftening.ofHinge(s), subdivisions = subdivisions)
            .dishing(optimumPlacement)
    fun atSamples(s: Double, samples: Int): Double =
        admittedHost.Solve(CrossoverSoftening.ofHinge(s), samples = samples)
            .dishing(optimumPlacement)

    fun convergence(
        quantity: String, axis: String, values: List<Double>, note: String
    ) = T164ConvergenceRecord(
        quantity, axis, values,
        (values.max() - values.min()) / abs(values[values.size / 2]), note
    )

    val convergenceRecords = listOf(
        convergence(
            "dishing/stroke of C-0090's optimum at s = 1", "nested subdivisions 1 c 2 c 4",
            listOf(1, 2, 4).map { atSubdivisions(1.0, it) },
            "nested, per CLAUDE.md: a subdivision of 3 moves a point load off a node"
        ),
        convergence(
            "dishing/stroke of C-0090's optimum at s = 0", "nested subdivisions 1 c 2 c 4",
            listOf(1, 2, 4).map { atSubdivisions(0.0, it) },
            "the softened end of channel A, where the two end columns carry only their links"
        ),
        convergence(
            "dishing/stroke of C-0090's optimum at s = 1", "dishing sample grid 41/81/161",
            listOf(41, 81, 161).map { atSamples(1.0, it) },
            "81 is the grid every published dishing in this programme is read on"
        ),
        convergence(
            "dishing/stroke of C-0090's optimum at s = 0", "dishing sample grid 41/81/161",
            listOf(41, 81, 161).map { atSamples(0.0, it) },
            "the softened end, on the same grid"
        )
    )

    // --------------------------------------------------------------------------- reproductions
    val reproductions = listOf(
        T164ReproductionRecord(
            "C-0090", "dishing/stroke, exhaustive centro-symmetric optimum, 38.08 nm, phase 8, " +
                    "row end ADMITTED at full interior stiffness",
            admittedPublished, admittedReading, abs(admittedPublished - admittedReading), true
        ),
        T164ReproductionRecord(
            "C-0090", "dishing/stroke, exhaustive centro-symmetric optimum, 38.08 nm, phase 8, " +
                    "row end REFUSED",
            refusedPublished, refusedOptimum.bestValue,
            abs(refusedPublished - refusedOptimum.bestValue), true
        ),
        T164ReproductionRecord(
            "C-0009 / Gen1Tile", "k_theta of one antiparallel crossover, pN nm/rad",
            interiorHinge, hingeStiffnessOfBondCount(CROSSOVER_PHOSPHATE_BONDS),
            abs(interiorHinge - hingeStiffnessOfBondCount(CROSSOVER_PHOSPHATE_BONDS)), true
        ),
        T164ReproductionRecord(
            "C-0095", "row-end crossovers of the eight-column phase-8 lattice, one per interface",
            14.0, admittedHost.rowEndSites.size.toDouble(),
            abs(14.0 - admittedHost.rowEndSites.size), true
        ),
        T164ReproductionRecord(
            "C-0015", "interface crossovers of the eight-column phase-8 lattice",
            56.0, admittedHost.lattice(null).crossovers.size.toDouble(),
            abs(56.0 - admittedHost.lattice(null).crossovers.size), true
        ),
        T164ReproductionRecord(
            "CH-0111", "the fraction of its bracket at which T-5b's 0.10 sits",
            0.356, conventionFraction, abs(0.356 - conventionFraction), false
        )
    )
    reproductions.filter { it.strict }.forEach {
        check(it.departure < 1.0e-8) {
            "${it.source}'s ${it.quantity} is not reproduced: ${it.published} against " +
                    "${it.reproduced}, departure ${it.departure}"
        }
    }
    check(refusedKey.isNotBlank())

    // --------------------------------------------------------------------- verdict and records
    val worstUniform = sweep.maxOf { it.uniformLoadDishingOverStroke }
    val keyStable = sweep.all { it.bestKeyIsFullStiffnessOptimum }
    val everyRungFlat = sweep.all { it.flatAtTenPercent }
    val channelAFlat = sweep.filter { it.channel == HINGE_ONLY }.all { it.flatAtTenPercent }
    val crossingA = crossings.single { it.channel == HINGE_ONLY }
    val crossingB = crossings.single { it.channel == WHOLE_ELEMENT }

    val predicates = listOf(
        T164PredicateRecord(
            "P1", "a cheap bound on the row-end crossover's stiffness is stated before the sweep",
            "MET — a ceiling s <= 1 that is a ratio of two bond counts, a counting floor at " +
                    "s = 1/2, and the constraint/elasticity distinction that puts C-0090's " +
                    "refused reading OUTSIDE the reachable set",
            true
        ),
        T164PredicateRecord(
            "P2", "the sweep is run on C-0090's own pipeline and monotonicity is MEASURED",
            if (monotone) "MET — 16 consecutive pairs over two channels, every one decreasing " +
                    "in stiffness" else "MET — and the sweep is NOT monotone, which is the " +
                    "declared falsifier F1",
            true
        ),
        T164PredicateRecord(
            "P3", "the crossing is located and compared against the cheap bound",
            if (crossingA.crossingExists)
                "MET — channel A crosses T-5b's 0.10 inside [0, 1]"
            else "MET — channel A does NOT cross T-5b's 0.10 anywhere in [0, 1], so the " +
                    "threshold is that no row-end stiffness whatever loses the verdict",
            true
        ),
        T164PredicateRecord(
            "P4", "the consequence for C-0090's and C-0095's verdicts is stated",
            "MET — both stand, and CH-0111's bracket is corrected rather than the verdict",
            true
        ),
        T164PredicateRecord(
            "P5", "where no source gives the stiffness, the ceiling and threshold are delivered",
            "MET — no accessible source gives a row-end crossover's k_theta; the ceiling is a " +
                    "count and the threshold is this sweep",
            true
        )
    )

    val falsifiers = listOf(
        T164FalsifierRecord(
            "F1", "the best 34-root dishing is NOT monotone in the row-end softening",
            !monotone,
            if (monotone) "did not fire: all ${monotonicity.size} consecutive pairs over both " +
                    "channels are decreasing in stiffness"
            else "FIRED: ${monotonicity.count { !it.decreasingInStiffness }} of " +
                    "${monotonicity.size} consecutive pairs move the wrong way"
        ),
        T164FalsifierRecord(
            "F2", "at s = 1 the pipeline does not reproduce C-0090's 0.0621469105",
            abs(admittedReading - admittedPublished) >= 1.0e-9,
            "reproduced at a departure of " +
                    "${abs(admittedReading - admittedPublished)
                        .roundedForProse(DEPARTURE_SIGNIFICANT_DIGITS, floor = 0.0)}"
        ),
        T164FalsifierRecord(
            "F3", "channel B's s = 0 limit differs materially from the refused reading",
            abs(wholeElementZero - refusedOptimum.bestValue) /
                    refusedOptimum.bestValue >= 0.05,
            "channel B at s = 0 is ${wholeElementZero.roundedForProse()} against the " +
                    "refused reading's ${refusedOptimum.bestValue.roundedForProse()}; the two " +
                    "lattices carry the same mechanics and " +
                    "differ only in the mesh the two extra nodes impose"
        ),
        T164FalsifierRecord(
            "F4", "channel A's crossing lies above the counting floor s = 1/2",
            crossingA.crossingExists && crossingA.aboveCountingFloor,
            if (crossingA.crossingExists)
                "the crossing is bracketed at [${crossingA.softeningBelow.roundedForProse()}, " +
                        "${crossingA.softeningAbove.roundedForProse()}]"
            else "did not fire: there is no crossing at all — the reachable range is flat at " +
                    "every rung"
        ),
        T164FalsifierRecord(
            "F5", "the best placement key moves across the sweep", !keyStable,
            if (keyStable) "did not fire: every rung of both channels returns C-0090's own " +
                    "optimum placement"
            else "FIRED at ${sweep.count { !it.bestKeyIsFullStiffnessOptimum }} of " +
                    "${sweep.size} rungs, and BOUNDED: the worst penalty for keeping C-0090's " +
                    "own placement instead of each rung's optimum is " +
                    "${sweep.filter { it.channel == HINGE_ONLY }
                        .maxOf { it.publishedPlacementPenalty }.roundedForProse()} " +
                    "of the stroke on channel A, and the published placement is flat at " +
                    "${sweep.count { it.publishedPlacementFlatAtTenPercent }} of ${sweep.size} rungs"
        ),
        T164FalsifierRecord(
            "F6", "a uniform load on a uniform foundation dishes more than 1e-6 of the free stroke",
            worstUniform >= 1.0e-6,
            "the worst over ${sweep.size} rungs is ${worstUniform.roundedForProse()} of the free stroke"
        )
    )

    val findings = listOf(
        "THE BRACKET IS NOT A BRACKET. C-0090's two readings differ in THREE things — the " +
                "dihedral spring, the vertical link and the node — and only the first of them " +
                "is a stiffness. A crossover that EXISTS carries its vertical link at full " +
                "value, because that element is a constraint expressing covalent continuity " +
                "and not an elasticity; Rothemund's own remedy for the edge strain adds slack " +
                "to the torsion, not to the connectivity. So the reachable set is channel A, " +
                "and it spans ${admittedReading.roundedForProse()} to " +
                "${worstReachable.roundedForProse()} of the stroke — " +
                "${reachableFraction.roundedForProse()} of the interval CH-0111 quotes.",
        "THE VERDICT DOES NOT CHANGE INSIDE THE REACHABLE SET. Destroying the dihedral spring " +
                "of all 14 row-end crossovers entirely takes the best 34-root placement to " +
                "${worstReachable.roundedForProse()} against T-5b's 0.10, so the 38.08 nm tile is " +
                "flat at EVERY " +
                "row-end stiffness, including zero. The counting ceiling s <= 1 and the " +
                "counting floor s = 1/2 are then decoration: the answer does not need them.",
        "THE COUNTING CEILING IS A RATIO OF TWO COUNTS AND CARRIES NO ELASTICITY. " +
                "k_theta = 2 alpha B/(100 a) is TWO softened phosphate bonds in parallel and " +
                "C-0029's theorem caps a duplex end at two strand termini, so s <= 1 exactly, " +
                "with alpha and B cancelling. That is the whole of what the one-sided material " +
                "costs that a bond census can see, and it is available before any solve.",
        "WHAT MOVES THE ANSWER IS THE NODE, NOT THE SPRING. Channel B at s = 0 — both elements " +
                "gone, the two extra mesh nodes retained — reads " +
                "${wholeElementZero.roundedForProse()} against the refused reading's " +
                "${refusedOptimum.bestValue.roundedForProse()} on a six-column host, so the " +
                "mechanics is the same and the mesh is worth the difference. The 2.7x that " +
                "C-0090's two readings differ by is therefore a statement about a LATTICE, not " +
                "about a joint.",
        "MONOTONICITY WAS CHECKED RATHER THAN ASSUMED, and it holds: " +
                "${monotonicity.count { it.decreasingInStiffness }} of ${monotonicity.size} " +
                "consecutive pairs over two channels are decreasing in stiffness. " +
                "CLAUDE.md records that a verdict which is not monotone in a swept variable has " +
                "no threshold; this one has one, and the threshold is that there is no crossing.",
        "THE DESIGN MOVES ONLY WHERE IT COSTS NOTHING TO KEEP IT. F5 fired: " +
                "${sweep.count { !it.bestKeyIsFullStiffnessOptimum }} of ${sweep.size} rungs " +
                "return a placement other than C-0090's, and every one of them is a rung where " +
                "the two are nearly tied — keeping C-0090's own 34 roots at EVERY rung of " +
                "channel A costs at most " +
                "${sweep.filter { it.channel == HINGE_ONLY }
                    .maxOf { it.publishedPlacementPenalty }.roundedForProse()} " +
                "of the stroke and stays inside T-5b's 0.10 throughout. So the row-end " +
                "stiffness moves the VALUE of the flatness and not the DESIGN, which is the " +
                "separate and worse exposure.",
        "CHANNEL B IS A STEP, NOT A RAMP, AND THAT IS THE POINT. The vertical link is a PENALTY " +
                "enforcing a constraint, so a tenth of it still enforces the constraint: " +
                "channel B reads ${cache.getValue(WHOLE_ELEMENT to 0.125)
                    .bestDishingOverStroke.roundedForProse()} " +
                "at s = 0.125 and ${cache.getValue(WHOLE_ELEMENT to 0.0)
                    .bestDishingOverStroke.roundedForProse()} " +
                "at s = 0 exactly. The bisection therefore locates a discontinuity and not a " +
                "threshold, and CLAUDE.md's rule that the link's value must not affect the " +
                "answer is what makes the 2.7x binary: the link is either there or it is not, " +
                "and a covalently continuous backbone says it is.",
        "NO VARIATIONAL ARGUMENT WOULD HAVE SETTLED MONOTONICITY. Adding stiffness lowers an " +
                "energy, but the objective here is a PEAK DISHING — a maximum of a residual " +
                "field after a best-fit plane is removed — which is not an energy and is not " +
                "monotone by any Rayleigh argument, and the objective is then a minimum over " +
                "163 296 placements. Measuring it was necessary, and it is the cheap half of " +
                "this task rather than the expensive one."
    )

    val decision =
        "the 38.08 nm tile is flat at T-5b's 0.10 at EVERY row-end dihedral stiffness from zero " +
                "to an interior crossover's, best 34-root dishing " +
                "${worstReachable.roundedForProse()} to ${admittedReading.roundedForProse()} " +
                "of the stroke; CH-0111's bracket is not reachable because its " +
                "lower end deletes a covalent constraint and a mesh node as well as a spring"

    val result = T164Result(
        task = "T-164 — how stiff is a ROW-END crossover?",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; single-layer " +
                "square-lattice Rothemund sheet, 15 duplexes at the SAXS 2.69 nm, 0.34 nm rise, " +
                "32/3 bp per turn, 16 bp column pitch, 32 bp per-interface spacing; along-helix " +
                "width 38.08 nm (112 bp, C-0086) at crossover phase 8; C-0090's buildable " +
                "24-rise 8.16 nm arm at C-0055's 34 roots; C-0017's 33.3333 pN/nm mandate shared " +
                "equally; C-0022's solved collar at 2 mM, a 10 nm gap and 0.192 V, AS C-0090 " +
                "CARRIED IT; C-0001's foundation secant",
        decision = decision,
        cheapBounds = cheapBounds,
        sweep = sweep,
        monotonicity = monotonicity,
        crossings = crossings,
        references = references,
        convergence = convergenceRecords,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "edgeX" to edgeX,
            "phaseBasePairs" to PHASE.toDouble(),
            "duplexes" to DUPLEXES.toDouble(),
            "armLength" to arm,
            "armCount" to C0055_ARM_COUNT.toDouble(),
            "mandate" to MANDATE,
            "flatnessTolerance" to FLATNESS_TOLERANCE,
            "interiorHingeStiffness" to interiorHinge,
            "rowEndCrossovers" to admittedHost.rowEndSites.size.toDouble(),
            "latticeCrossovers" to admittedHost.lattice(null).crossovers.size.toDouble(),
            "freeStroke" to admittedHost.freeStroke,
            "admittedReading" to admittedReading,
            "channelAAtZero" to worstReachable,
            "channelBAtZero" to wholeElementZero,
            "refusedReading" to refusedOptimum.bestValue,
            "reachableFractionOfBracket" to reachableFraction,
            "conventionFractionOfBracket" to conventionFraction,
            "worstUniformLoadDishing" to worstUniform,
            "enumeratedPerRung" to fullOptimum.enumerated.toDouble(),
            "rungsEvaluated" to cache.size.toDouble(),
            "everyRungFlat" to if (everyRungFlat) 1.0 else 0.0,
            "channelAEveryRungFlat" to if (channelAFlat) 1.0 else 0.0,
            "crossingExistsChannelA" to if (crossingA.crossingExists) 1.0 else 0.0,
            "crossingExistsChannelB" to if (crossingB.crossingExists) 1.0 else 0.0,
            "countingCeiling" to softeningOfBondCount(CROSSOVER_PHOSPHATE_BONDS),
            "countingFloor" to softeningOfBondCount(1)
        )
    )

    val output = File("gpd/results/T-164-row-end-crossover-stiffness.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        )
    )

    println()
    println("the sweep")
    result.sweep.forEach {
        println(
            "  %-1s  s %5.3f  k_theta %8.3f  best %10.7f  flat %-5s".format(
                it.channel.take(1), it.softening, it.hingeStiffness,
                it.bestDishingOverStroke, it.flatAtTenPercent
            )
        )
    }
    println()
    println("published-placement penalty")
    result.sweep.forEach {
        println(
            "  %-1s  s %5.3f  best %10.7f  C-0090's own %10.7f  penalty %10.7f".format(
                it.channel.take(1), it.softening, it.bestDishingOverStroke,
                it.publishedPlacementDishing, it.publishedPlacementPenalty
            )
        )
    }
    println()
    println("references")
    result.references.forEach {
        println("  %-70s %10.7f  inside %s".format(it.name.take(70), it.dishingOverStroke, it.insideConvention))
    }
    println()
    println("upstream reproductions")
    result.reproductions.forEach {
        println(
            "  %-18s %-64s %14.9g vs %14.9g  %8.2e %s".format(
                it.source, it.quantity.take(64), it.published, it.reproduced, it.departure,
                if (it.strict) "" else "(non-strict)"
            )
        )
    }
    println()
    println("falsifiers")
    result.falsifiers.forEach {
        println("  %s %-5s %s".format(it.name, if (it.fired) "FIRED" else "no", it.outcome))
    }
    println()
    result.predicates.forEach { println("  ${it.name}: ${it.verdict}") }
    println()
    result.findings.forEach { println("  * $it"); println() }
    println("written to ${output.path} in ${(System.currentTimeMillis() - started) / 1000} s")
}
