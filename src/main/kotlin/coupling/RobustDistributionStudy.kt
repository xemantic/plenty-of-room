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
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
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

/**
 * Task `T-123` — is **any** distribution flat at every one of `C-0022`'s solved states? Leaf `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh coupling.RobustDistributionStudyKt
 * ```
 *
 * Emits `gpd/results/T-123-robust-distribution.json`, deterministically — no timestamp, every
 * floating-point number rounded at the serialisation boundary per [roundCouplingResult], and
 * every **decision** the search makes taken on rounded values too, which is the half of that
 * discipline `C-0058` had to discover.
 *
 * ## What this study is
 *
 * `C-0058` answers *"can a distribution make the tile flat"* — yes, at one state — and then
 * qualifies it: the flat design dishes 0.187 at `C-0022`'s 2 nm state where the uniform coupling
 * dishes 0.071, and its minimax over all five solved states reaches only 0.1587. It says plainly
 * that this is a *"not found"* from a coordinate descent at three starts.
 *
 * This study replaces the search (a smoothed minimax with analytic gradients and ~60 starts,
 * `RobustDistribution.kt`), asks **which** states bind by optimising every one of the 31 non-empty
 * subsets of the five, and then asks the question the five states themselves beg: **the device
 * does not visit them.** Three of the five are the rest states of three *different buffers* and
 * two are the rest and held states of a *different layer height*. So the traversed range of each
 * device is assembled from `C-0022`'s own 21 solved profiles and the minimax re-run over it.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One of `C-0022`'s solved states, with what the free and the uniformly coupled tile do under it. */
@Serializable
data class T123StateRecord(
    val name: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val collarDepth: Double,
    val collarWidth: Double,
    val rimResidualDepth: Double,
    val collarSense: String,
    val rimSense: String,
    val freeTileDishingOverStroke: Double,
    val uniformDishingOverStroke: Double,
    val uniformIsFlat: Boolean,
    /** `C-0058`'s published flat design, evaluated here. */
    val rimTimesFiveDishingOverStroke: Double,
    val rimTimesFiveIsFlat: Boolean,
    val singleStateOptimumOverStroke: Double,
    val reachableFloorOverStroke: Double
)

/** The cheap bound, per state and over a set — the only rigorous statement here. */
@Serializable
data class T123CheapBoundRecord(
    val set: String,
    val states: Int,
    val worstReachableFloor: Double,
    val worstReachableFloorOverStroke: Double,
    val toleranceExceeded: Boolean,
    val falsifierFired: Boolean,
    val verdict: String
)

/** State `i`'s own optimum, read at state `j` — the tension matrix. */
@Serializable
data class T123TensionRecord(
    val optimisedAt: String,
    val readAt: String,
    val dishingOverStroke: Double,
    val ownOptimumOverStroke: Double,
    val penalty: Double,
    val flatThere: Boolean,
    /** The cosine between the two states' free-tile dishing fields. */
    val freeFieldCosine: Double,
    /** The best worst-case over the PAIR, which is what a two-state minimax can do. */
    val pairMinimaxOverStroke: Double,
    val pairIsFlat: Boolean
)

/** One subset of the five headline states, optimised on its own. */
@Serializable
data class T123SubsetRecord(
    val members: List<String>,
    val size: Int,
    val worstDishing: Double,
    val worstDishingOverStroke: Double,
    val flat: Boolean,
    val bindingStates: List<String>,
    val uniformWorstOverStroke: Double,
    val improvementOverUniform: Double,
    val peakPathStiffness: Double,
    val peakPathForceAtAcceptableStroke: Double,
    val admissibleUnderTheUnzipAllowable: Boolean,
    val startsWithinOnePartInAMillion: Int,
    val startsUsed: Int
)

/** One **operating range** — the states a named device actually traverses. */
@Serializable
data class T123RangeRecord(
    val device: String,
    val rationale: String,
    val members: List<String>,
    val interpolatedMembers: Int,
    val worstDishing: Double,
    val worstDishingOverStroke: Double,
    val flat: Boolean,
    val bindingStates: List<String>,
    val uniformWorstOverStroke: Double,
    val rimTimesFiveWorstOverStroke: Double,
    val improvementOverUniform: Double,
    val peakPathStiffness: Double,
    val peakPathForceAtAcceptableStroke: Double,
    val unzipMargin: Double,
    val admissibleUnderTheUnzipAllowable: Boolean,
    val peakThermalForce: Double,
    val maximumOverMinimumStiffness: Double
)

/** A distribution solved on the ASSEMBLED lattice and plate, not on the surrogate. */
@Serializable
data class T123DistributionRecord(
    val label: String,
    val profile: String,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverPlate: Double,
    val latticeExcessPercent: Double,
    val dishingOverStroke: Double,
    val flat: Boolean,
    val peakSupportForce: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val peakThermalForce: Double,
    val peakPathStiffness: Double,
    val peakPathForceAtAcceptableStroke: Double,
    val unzipMargin: Double
)

/** The robust distribution, attachment by attachment. */
@Serializable
data class T123PathRecord(
    val label: String,
    val index: Int,
    val x: Double,
    val y: Double,
    val stiffness: Double,
    val shareOfTheUniformPath: Double,
    val forceAtAcceptableStroke: Double,
    val thermalForce: Double
)

/** What `C-0060`'s catalogue would have to deliver, level by level. */
@Serializable
data class T123BuildRecord(
    val label: String,
    val levels: Int,
    val distinctLevels: Int,
    val stiffnessValues: List<Double>,
    val levelRatio: Double,
    val insideTheMeasuredFlatWindow: Boolean,
    val coarsestQuantumOfTheStiffestLevel: Double,
    val worstDishingOverStroke: Double,
    val quantisationPenalty: Double,
    val flat: Boolean,
    val verdict: String
)

@Serializable
data class T123ConvergenceRecord(
    val axis: String,
    val setting: String,
    val worstDishingOverStroke: Double,
    val departureFromFinest: Double
)

@Serializable
data class T123ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

@Serializable
data class T123Result(
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
    val rigidPlateTolerance: Double,
    val mandatedTotalStiffness: Double,
    val freeTileStroke: Double,
    val states: List<T123StateRecord>,
    val cheapBound: List<T123CheapBoundRecord>,
    val tension: List<T123TensionRecord>,
    val subsets: List<T123SubsetRecord>,
    val ranges: List<T123RangeRecord>,
    val distributions: List<T123DistributionRecord>,
    val paths: List<T123PathRecord>,
    val buildability: List<T123BuildRecord>,
    val convergence: List<T123ConvergenceRecord>,
    val reproductions: List<T123ReproductionRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the fixed inputs
// ---------------------------------------------------------------------------------------------

private const val T123_DUPLEXES = 15

private const val T123_COLUMNS = 3

private const val T123_NOMINAL_COLUMNS = 8

private const val T123_SAMPLES = 81

private const val T123_TOLERANCE = 0.10

private const val T123_RIM_STANDOFF = 1.0

private val T123_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `C-0058`'s best one-parameter design: the 34 stations within 6.7 nm of an edge carry 5×. */
private const val T123_BEST_COLLAR = 6.7

private const val T123_BEST_RATIO = 5.0

/** `C-0060`'s **measured** flat ratio window, cited and re-checked here. */
private const val T123_FLAT_WINDOW_LOW = 3.5

private const val T123_FLAT_WINDOW_HIGH = 20.0

// ---------------------------------------------------------------------------------------------
// the states, read from `C-0022`'s own result file
// ---------------------------------------------------------------------------------------------

private class T123Profile(
    val name: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double,
    val interpolated: Boolean = false
) {

    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T123_RIM_STANDOFF))
    )

}

/**
 * `C-0022`'s solved profiles, keyed on **`(concentration, gapHeight, appliedBias)`**.
 *
 * `CLAUDE.md`'s trap, avoided by construction: the file carries more than one profile per
 * `(concentration, gap)` — one per operating bias — so keying on two of the three silently takes
 * whichever is listed first, at a bias no headline table uses.
 */
private fun t123SolvedProfiles(file: File): List<T123Profile> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-123 consumes the SOLVED edge profiles " +
                "and will not substitute assumed ones for them."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .map { record ->
            fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
            T123Profile(
                name = "%.1f mM, %.0f nm, %.3f V".format(
                    value("concentration"), value("gapHeight"), value("appliedBias")
                ),
                concentration = value("concentration"),
                gapHeight = value("gapHeight"),
                appliedBias = value("appliedBias"),
                smoothDepth = value("taperDepth"),
                smoothWidth = value("taperWidth"),
                rimDepth = value("rimResidualDepth")
            )
        }
}

/** The `(concentration, gap, bias)` keys of the five states `C-0058`'s Deliverable 4 quotes. */
private val T123_HEADLINE_KEYS: List<Triple<Double, Double, Double>> = listOf(
    Triple(2.0, 10.0, 0.192),
    Triple(0.5, 10.0, 0.134),
    Triple(10.0, 10.0, 0.192),
    Triple(2.0, 5.0, 0.368),
    Triple(2.0, 2.0, 0.368)
)

/**
 * The operating **ranges**, each a device and the solved profiles it traverses.
 *
 * A device is a `(buffer, layer height, bias)`; under bias it descends from `gap = L₀` to
 * `gap = L₀ − s`. `C-0018` places the 10 nm device at 2 mM with 1–3 % of pull-in margin and
 * `C-0032` recommends 0.5 mM, where every predicate clears; §3's acceptable stroke is 3 nm, so a
 * 10 nm device traverses gaps 10 → 7 nm.
 *
 * Where `C-0022` did not solve the compressed end at the device's own bias, the range **brackets**
 * it with the two neighbouring biases it did solve, which is a wider requirement and not a
 * narrower one.
 */
private class T123Range(
    val device: String,
    val rationale: String,
    val keys: List<Triple<Double, Double, Double>>,
    /** Interpolate this many intermediate gaps between the first two keys, as a discretisation check. */
    val interpolations: Int = 0
)

private val T123_RANGES = listOf(
    T123Range(
        device = "2 mM, L0 = 10 nm, 0.192 V (C-0018's placed device)",
        rationale = "C-0018 places this device with 1-3 % of pull-in margin; S3's acceptable " +
                "stroke is 3 nm, so it traverses gaps 10 -> 7 nm at its OWN operating bias, " +
                "and C-0022 solved both ends of that at 0.192 V",
        keys = listOf(Triple(2.0, 10.0, 0.192), Triple(2.0, 7.0, 0.192)),
        interpolations = 2
    ),
    T123Range(
        device = "0.5 mM, L0 = 10 nm, 0.134 V (C-0032's recommendation)",
        rationale = "C-0032 finds every predicate clears at 0.5 mM where the 2 mM design sits ON " +
                "its own fold; C-0022 did not solve the compressed end at 0.134 V, so the range " +
                "BRACKETS it with the 0.082 V and 0.155 V profiles it did solve at 7 nm",
        keys = listOf(
            Triple(0.5, 10.0, 0.134), Triple(0.5, 7.0, 0.082), Triple(0.5, 7.0, 0.155)
        )
    ),
    T123Range(
        device = "2 mM, L0 = 5 nm, 0.368 V",
        rationale = "the rest and held states of the FIVE nanometre layer — C-0022's 2 nm state " +
                "is this device held at S3's 3 nm stroke, not a state of the 10 nm device at all",
        keys = listOf(Triple(2.0, 5.0, 0.368), Triple(2.0, 2.0, 0.368))
    ),
    T123Range(
        device = "10 mM, L0 = 10 nm, 0.192 V",
        rationale = "the third buffer of C-0022's headline five, with its compressed end " +
                "bracketed by the 0.082 V and 0.155 V profiles solved at 7 nm",
        keys = listOf(
            Triple(10.0, 10.0, 0.192), Triple(10.0, 7.0, 0.082), Triple(10.0, 7.0, 0.155)
        )
    )
)

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t123Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t123Lattice(
    sheet: OrigamiSheet,
    supports: List<PointSupport> = emptyList(),
    subdivisions: Int = 2
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = T123_DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = CrossoverLayout.centred(T123_NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0),
    subdivisions = subdivisions,
    supports = supports
)

/**
 * A deterministic log-normal ensemble of starts — a seeded linear congruential generator, so the
 * ensemble is a fixed table and not a random one. A search reported from random starts is not
 * reproducible, and `gpd/README.md`'s rule is that a re-run which changes nothing produces no diff.
 */
private fun t123RandomStarts(paths: Int, count: Int, spread: Double): List<List<Double>> {
    var seed = 20260814L
    fun next(): Double {
        seed = (seed * 6364136223846793005L + 1442695040888963407L)
        return ((seed ushr 11).toDouble() / (1L shl 53).toDouble()) - 0.5
    }
    return (0 until count).map { List(paths) { kotlin.math.exp(spread * 2.0 * next()) } }
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = t123Sheet()
    val lengthY = T123_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lengthY)
    val plateModel = sheet.plate(Gen1Tile.EDGE_X, lengthY)
    val grid = attachmentGrid(T123_COLUMNS, T123_DUPLEXES, Gen1Tile.EDGE_X, lengthY)
    val paths = grid.size

    println("T-123 — reading C-0022's solved edge profiles ...")
    val solved = t123SolvedProfiles(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    fun profileAt(key: Triple<Double, Double, Double>): T123Profile = solved.firstOrNull {
        it.concentration == key.first && it.gapHeight == key.second && it.appliedBias == key.third
    } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")

    val headline = T123_HEADLINE_KEYS.map { profileAt(it) }
    val rangeProfiles = T123_RANGES.flatMap { range ->
        val members = range.keys.map { profileAt(it) }
        val interpolated = if (range.interpolations > 0 && members.size >= 2) {
            (1..range.interpolations).map { step ->
                val f = step.toDouble() / (range.interpolations + 1)
                val a = members[0]
                val b = members[1]
                T123Profile(
                    name = "INTERPOLATED %.1f mM, %.2f nm, %.3f V".format(
                        a.concentration, a.gapHeight + f * (b.gapHeight - a.gapHeight), a.appliedBias
                    ),
                    concentration = a.concentration,
                    gapHeight = a.gapHeight + f * (b.gapHeight - a.gapHeight),
                    appliedBias = a.appliedBias,
                    smoothDepth = a.smoothDepth + f * (b.smoothDepth - a.smoothDepth),
                    smoothWidth = a.smoothWidth + f * (b.smoothWidth - a.smoothWidth),
                    rimDepth = a.rimDepth + f * (b.rimDepth - a.rimDepth),
                    interpolated = true
                )
            }
        } else emptyList()
        members + interpolated
    }
    val uniformProfile = T123Profile("uniform load (the falsifier case)", 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
    val allProfiles = (headline + rangeProfiles + uniformProfile).distinctBy { it.name }
    val indexOf = allProfiles.withIndex().associate { (index, profile) -> profile.name to index }
    val loadStates = allProfiles.map { LoadState(it.name, it.field(interiorPressure, lengthY)) }
    println("T-123 — ${allProfiles.size} load states, ${paths} attachments as $T123_COLUMNS x $T123_DUPLEXES")

    // The free-tile stroke — C-0006's, C-0015's, C-0026's, C-0047's and C-0058's normaliser.
    val barePlate = PlateOnFoundation(plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), 12)
    val stroke = barePlate.solve(uniformPressure(interiorPressure)).meanDeflection
    val bareLattice = t123Lattice(sheet)

    println("T-123 — the multi-state surrogate: one factorisation, ${paths + allProfiles.size} load cases ...")
    val surrogate = multiStateSurrogate(bareLattice, grid, loadStates, T123_SAMPLES)

    val uniformStiffness = normalisedStiffnesses(List(paths) { 1.0 }, T123_MANDATE)
    val rimWeights = rimStiffenedWeights(
        grid, Gen1Tile.EDGE_X, lengthY, T123_BEST_COLLAR, T123_BEST_RATIO
    )
    val rimStiffness = normalisedStiffnesses(rimWeights, T123_MANDATE)
    val uniformPeaks = surrogate.peakDishing(uniformStiffness)
    val rimPeaks = surrogate.peakDishing(rimStiffness)
    val ceiling = perPathStiffnessCeiling(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
    )

    // ------------------------------------------------------------------ the starts
    val richStarts: List<List<Double>> = buildList {
        add(List(paths) { 1.0 })
        headline.forEach { add(loadMatchedWeights(grid, it.field(interiorPressure, lengthY))) }
        listOf(3.0, 6.7, 8.94, 13.0).forEach { collar ->
            listOf(0.4, 2.0, 5.0, 10.0, 20.0).forEach { ratio ->
                add(rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, collar, ratio))
            }
        }
        addAll(t123RandomStarts(paths, 8, 0.5))
        addAll(t123RandomStarts(paths, 8, 1.2))
    }

    fun minimax(
        states: List<Int>,
        starts: List<List<Double>>,
        levels: List<Double> = listOf(0.3, 0.1, 0.03, 0.01, 3e-3, 1e-3),
        iterations: Int = 20
    ) = minimaxStiffnessDistribution(
        surrogate = surrogate,
        states = states,
        totalStiffness = T123_MANDATE,
        starts = starts,
        ceiling = ceiling,
        smoothingLevels = levels,
        iterationsPerLevel = iterations,
        polishSweeps = 3
    )

    // ------------------------------------------------------------------ the single-state optima
    println("T-123 — each of the five headline states on its own ...")
    val headlineIndices = headline.map { indexOf.getValue(it.name) }
    val singleOptima = headlineIndices.associateWith { state ->
        minimax(listOf(state), richStarts)
    }

    val stateRecords = headline.mapIndexed { position, profile ->
        val state = headlineIndices[position]
        T123StateRecord(
            name = profile.name,
            concentration = profile.concentration,
            gapHeight = profile.gapHeight,
            appliedBias = profile.appliedBias,
            collarDepth = profile.smoothDepth,
            collarWidth = profile.smoothWidth,
            rimResidualDepth = profile.rimDepth,
            collarSense = if (profile.smoothDepth < 0.0) "ENHANCEMENT" else "taper",
            rimSense = if (profile.rimDepth < 0.0) "ENHANCEMENT" else "taper",
            freeTileDishingOverStroke = surrogate.freeFieldPeak(state) / stroke,
            uniformDishingOverStroke = uniformPeaks[state] / stroke,
            uniformIsFlat = uniformPeaks[state] / stroke < T123_TOLERANCE,
            rimTimesFiveDishingOverStroke = rimPeaks[state] / stroke,
            rimTimesFiveIsFlat = rimPeaks[state] / stroke < T123_TOLERANCE,
            singleStateOptimumOverStroke = singleOptima.getValue(state).worstDishing / stroke,
            reachableFloorOverStroke = surrogate.reachableDishingFloor(state) / stroke
        )
    }

    // ------------------------------------------------------------------ the cheap bound
    println("T-123 — the cheap bound, per state, before any minimax ...")
    fun boundRecord(label: String, states: List<Int>): T123CheapBoundRecord {
        val worst = states.maxOf { surrogate.reachableDishingFloor(it) }
        val fraction = worst / stroke
        return T123CheapBoundRecord(
            set = label,
            states = states.size,
            worstReachableFloor = worst,
            worstReachableFloorOverStroke = fraction,
            toleranceExceeded = fraction >= T123_TOLERANCE,
            falsifierFired = fraction >= T123_TOLERANCE,
            verdict = if (fraction >= T123_TOLERANCE)
                "PROVEN: no distribution whatever is flat at every one of these states"
            else "the bound does not forbid a flat distribution; the search decides"
        )
    }

    val cheapBound = buildList {
        add(boundRecord("C-0058's five headline states", headlineIndices))
        headline.forEachIndexed { position, profile ->
            add(boundRecord("single state: ${profile.name}", listOf(headlineIndices[position])))
        }
        T123_RANGES.forEach { range ->
            add(
                boundRecord(
                    "operating range: ${range.device}",
                    range.keys.map { indexOf.getValue(profileAt(it).name) }
                )
            )
        }
    }

    // ------------------------------------------------------------------ the tension matrix
    println("T-123 — the tension matrix: each state's own optimum read at every other ...")
    val pairMinimax = mutableMapOf<Pair<Int, Int>, MinimaxOptimum>()
    val tension = buildList {
        headlineIndices.forEach { first ->
            val optimum = singleOptima.getValue(first)
            val peaks = surrogate.peakDishing(optimum.stiffnesses)
            headlineIndices.forEach { second ->
                if (first != second) {
                    val key = if (first < second) first to second else second to first
                    val pair = pairMinimax.getOrPut(key) {
                        minimax(listOf(key.first, key.second), richStarts)
                    }
                    add(
                        T123TensionRecord(
                            optimisedAt = surrogate.stateNames[first],
                            readAt = surrogate.stateNames[second],
                            dishingOverStroke = peaks[second] / stroke,
                            ownOptimumOverStroke =
                                singleOptima.getValue(second).worstDishing / stroke,
                            penalty = peaks[second] /
                                    singleOptima.getValue(second).worstDishing,
                            flatThere = peaks[second] / stroke < T123_TOLERANCE,
                            freeFieldCosine = surrogate.freeFieldCosine(first, second),
                            pairMinimaxOverStroke = pair.worstDishing / stroke,
                            pairIsFlat = pair.worstDishing / stroke < T123_TOLERANCE
                        )
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------ every subset of the five
    println("T-123 — all 31 non-empty subsets of the five headline states ...")
    val leanStarts: List<List<Double>> = buildList {
        add(List(paths) { 1.0 })
        headline.forEach { add(loadMatchedWeights(grid, it.field(interiorPressure, lengthY))) }
        listOf(3.0, 6.7, 8.94).forEach { collar ->
            listOf(0.4, 2.0, 5.0, 10.0).forEach { ratio ->
                add(rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, collar, ratio))
            }
        }
        singleOptima.values.forEach { add(it.stiffnesses) }
    }
    val subsets = (1 until (1 shl headlineIndices.size)).map { mask ->
        headlineIndices.filterIndexed { position, _ -> (mask shr position) and 1 == 1 }
    }.sortedWith(compareBy({ it.size }, { it.joinToString(",") }))
    val subsetOptima = mutableMapOf<String, MinimaxOptimum>()
    val subsetRecords = subsets.map { states ->
        val key = states.joinToString(",")
        val optimum = when {
            states.size == 1 -> singleOptima.getValue(states[0])
            states.size == 2 -> pairMinimax.getValue(
                minOf(states[0], states[1]) to maxOf(states[0], states[1])
            )
            states.size == headlineIndices.size -> minimax(states, richStarts)
            else -> minimax(states, leanStarts)
        }
        subsetOptima[key] = optimum
        val peakStiffness = optimum.stiffnesses.max()
        val peakForce = peakStiffness * Gen1Tile.ACCEPTABLE_STROKE
        val uniformWorst = states.maxOf { uniformPeaks[it] }
        T123SubsetRecord(
            members = states.map { surrogate.stateNames[it] },
            size = states.size,
            worstDishing = optimum.worstDishing,
            worstDishingOverStroke = optimum.worstDishing / stroke,
            flat = optimum.worstDishing / stroke < T123_TOLERANCE,
            bindingStates = optimum.bindingStates,
            uniformWorstOverStroke = uniformWorst / stroke,
            improvementOverUniform = 1.0 - optimum.worstDishing / uniformWorst,
            peakPathStiffness = peakStiffness,
            peakPathForceAtAcceptableStroke = peakForce,
            admissibleUnderTheUnzipAllowable =
                peakForce <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE * (1.0 + 1e-9),
            startsWithinOnePartInAMillion = optimum.startsWithinOnePartInAMillion,
            startsUsed = optimum.startsUsed
        )
    }
    val portfolio = subsetOptima.getValue(headlineIndices.joinToString(","))

    // ------------------------------------------------------------------ the operating ranges
    println("T-123 — the operating ranges the device actually traverses ...")
    val rangeOptima = mutableMapOf<String, MinimaxOptimum>()
    val rangeRecords = T123_RANGES.map { range ->
        val solvedMembers = range.keys.map { indexOf.getValue(profileAt(it).name) }
        val optimum = minimax(solvedMembers, richStarts)
        rangeOptima[range.device] = optimum
        val peakStiffness = optimum.stiffnesses.max()
        val peakForce = peakStiffness * Gen1Tile.ACCEPTABLE_STROKE
        val uniformWorst = solvedMembers.maxOf { uniformPeaks[it] }
        T123RangeRecord(
            device = range.device,
            rationale = range.rationale,
            members = solvedMembers.map { surrogate.stateNames[it] },
            interpolatedMembers = range.interpolations,
            worstDishing = optimum.worstDishing,
            worstDishingOverStroke = optimum.worstDishing / stroke,
            flat = optimum.worstDishing / stroke < T123_TOLERANCE,
            bindingStates = optimum.bindingStates,
            uniformWorstOverStroke = uniformWorst / stroke,
            rimTimesFiveWorstOverStroke = solvedMembers.maxOf { rimPeaks[it] } / stroke,
            improvementOverUniform = 1.0 - optimum.worstDishing / uniformWorst,
            peakPathStiffness = peakStiffness,
            peakPathForceAtAcceptableStroke = peakForce,
            unzipMargin = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / peakForce,
            admissibleUnderTheUnzipAllowable =
                peakForce <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE * (1.0 + 1e-9),
            peakThermalForce = perPathThermalForces(optimum.stiffnesses).max(),
            maximumOverMinimumStiffness = optimum.stiffnesses.max() / optimum.stiffnesses.min()
        )
    }
    val designRange = T123_RANGES.first()
    val designRangeOptimum = rangeOptima.getValue(designRange.device)

    // ------------------------------------------------------------------ assembled solves
    println("T-123 — the winning distributions on the ASSEMBLED lattice and plate ...")
    fun assembled(
        label: String,
        stiffnesses: List<Double>,
        profile: T123Profile
    ): T123DistributionRecord {
        val supports = grid.mapIndexed { index, (x, y) -> PointSupport(x, y, stiffnesses[index]) }
        val field = profile.field(interiorPressure, lengthY)
        val solution = t123Lattice(sheet, supports).solve(field)
        val lattice = solution.peakDishing(T123_SAMPLES)
        val plate = PlateOnFoundation(plateModel, Gen1Tile.FOUNDATION_SECANT, supports, 12)
            .solve(field).peakDishing(T123_SAMPLES)
        val peakStiffness = stiffnesses.max()
        val peakForce = peakStiffness * Gen1Tile.ACCEPTABLE_STROKE
        return T123DistributionRecord(
            label = label,
            profile = profile.name,
            latticePeakDishing = lattice,
            platePeakDishing = plate,
            latticeOverPlate = lattice / plate,
            latticeExcessPercent = 100.0 * (lattice / plate - 1.0),
            dishingOverStroke = lattice / stroke,
            flat = lattice / stroke < T123_TOLERANCE,
            peakSupportForce = solution.supportForces.maxOf { abs(it) },
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear,
            peakThermalForce = perPathThermalForces(stiffnesses).max(),
            peakPathStiffness = peakStiffness,
            peakPathForceAtAcceptableStroke = peakForce,
            unzipMargin = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / peakForce
        )
    }

    val distributions = buildList {
        designRange.keys.map { profileAt(it) }.forEach { profile ->
            add(assembled("uniform (C-0058's limiting case)", uniformStiffness, profile))
            add(assembled("C-0058's rim x 5 over 6.70 nm", rimStiffness, profile))
            add(
                assembled(
                    "T-123 ROBUST over the design device's traversed range",
                    designRangeOptimum.stiffnesses, profile
                )
            )
            add(assembled("T-123 MINIMAX over the five headline states", portfolio.stiffnesses, profile))
        }
    }

    val pathRecords = buildList {
        listOf(
            "T-123 ROBUST over the design device's traversed range" to designRangeOptimum.stiffnesses,
            "T-123 MINIMAX over the five headline states" to portfolio.stiffnesses
        ).forEach { (label, stiffnesses) ->
            val thermal = perPathThermalForces(stiffnesses)
            stiffnesses.forEachIndexed { index, stiffness ->
                add(
                    T123PathRecord(
                        label = label,
                        index = index,
                        x = grid[index].first,
                        y = grid[index].second,
                        stiffness = stiffness,
                        shareOfTheUniformPath = stiffness * paths / T123_MANDATE,
                        forceAtAcceptableStroke = stiffness * Gen1Tile.ACCEPTABLE_STROKE,
                        thermalForce = thermal[index]
                    )
                )
            }
        }
    }

    // ------------------------------------------------------------------ buildability, from C-0060
    println("T-123 — what C-0060's catalogue would have to deliver ...")
    val buildability = buildList {
        listOf(
            Triple(
                "ROBUST over the design device's traversed range",
                designRangeOptimum.stiffnesses,
                designRange.keys.map { indexOf.getValue(profileAt(it).name) }
            ),
            Triple(
                "MINIMAX over the five headline states", portfolio.stiffnesses, headlineIndices
            )
        ).forEach { (label, stiffnesses, states) ->
            val reference = surrogate.worstDishing(stiffnesses, states)
            listOf(2, 3, 4, paths).forEach { levels ->
                val quantised = quantiseToLevels(stiffnesses, levels, T123_MANDATE)
                val values = quantised.distinct().sorted()
                val worst = surrogate.worstDishing(quantised, states)
                val ratio = values.last() / values.first()
                // C-0060's coarsest quantum: one base pair of an 11 bp hinge arm is 19.1 % of a
                // level's stiffness; the finest, one nucleotide of a 99 nt limb, is 1.0 %.
                val quantum = 0.191
                add(
                    T123BuildRecord(
                        label = label,
                        levels = levels,
                        distinctLevels = values.size,
                        stiffnessValues = values,
                        levelRatio = ratio,
                        insideTheMeasuredFlatWindow =
                            ratio in T123_FLAT_WINDOW_LOW..T123_FLAT_WINDOW_HIGH,
                        coarsestQuantumOfTheStiffestLevel = quantum,
                        worstDishingOverStroke = worst / stroke,
                        quantisationPenalty = worst / reference - 1.0,
                        flat = worst / stroke < T123_TOLERANCE,
                        verdict = when {
                            worst / stroke >= T123_TOLERANCE ->
                                "NOT flat once quantised to $levels levels"
                            values.size > 2 ->
                                "flat, but needs ${values.size} distinct levels where C-0060 " +
                                        "prices two"
                            ratio !in T123_FLAT_WINDOW_LOW..T123_FLAT_WINDOW_HIGH ->
                                "flat, but the level ratio is outside C-0060's measured window"
                            else -> "flat, two levels, inside C-0060's measured window"
                        }
                    )
                )
            }
        }
    }

    // ------------------------------------------------------------------ gate 4 — convergence
    println("T-123 — convergence ...")
    val convergence = buildList {
        // the sampling grid, at the two answers that carry the verdicts
        listOf(
            "the peak-dishing sampling grid, at the ROBUST distribution" to
                    (designRangeOptimum.stiffnesses to designRange.keys.map { profileAt(it) }),
            "the peak-dishing sampling grid, at the five-state MINIMAX" to
                    (portfolio.stiffnesses to headline)
        ).forEach { (axis, pair) ->
            val (stiffnesses, profiles) = pair
            val values = listOf(41, 81, 161).map { samples ->
                samples to multiStateSurrogate(
                    bareLattice, grid,
                    profiles.map { LoadState(it.name, it.field(interiorPressure, lengthY)) },
                    samples
                ).worstDishing(stiffnesses) / stroke
            }
            val finest = values.last().second
            values.forEach { (samples, value) ->
                add(
                    T123ConvergenceRecord(
                        axis = axis,
                        setting = "$samples x $samples",
                        worstDishingOverStroke = value,
                        departureFromFinest = abs(value / finest - 1.0)
                    )
                )
            }
        }
        // NESTED beam subdivisions, never 1/2/3/4 (CLAUDE.md)
        val subdivisionValues = listOf(1, 2, 4).map { subdivisions ->
            subdivisions to multiStateSurrogate(
                t123Lattice(sheet, subdivisions = subdivisions), grid,
                headline.map { LoadState(it.name, it.field(interiorPressure, lengthY)) },
                T123_SAMPLES
            ).worstDishing(portfolio.stiffnesses) / stroke
        }
        val finestSubdivision = subdivisionValues.last().second
        subdivisionValues.forEach { (subdivisions, value) ->
            add(
                T123ConvergenceRecord(
                    axis = "NESTED beam subdivisions 1 in 2 in 4 (never 1/2/3/4)",
                    setting = "$subdivisions per interval",
                    worstDishingOverStroke = value,
                    departureFromFinest = abs(value / finestSubdivision - 1.0)
                )
            )
        }
        // the SEARCH's own convergence: does adding starts move the answer?
        val startCounts = listOf(1, 6, 16, richStarts.size)
        val startValues = startCounts.map { count ->
            count to minimax(headlineIndices, richStarts.take(count)).worstDishing / stroke
        }
        val finestStarts = startValues.last().second
        startValues.forEach { (count, value) ->
            add(
                T123ConvergenceRecord(
                    axis = "the number of starts, on the five-state minimax",
                    setting = "$count starts",
                    worstDishingOverStroke = value,
                    departureFromFinest = abs(value / finestStarts - 1.0)
                )
            )
        }
        // the smoothing homotopy
        listOf(
            listOf(0.1) to "one level, mu = 0.1",
            listOf(0.3, 0.03) to "two levels",
            listOf(0.3, 0.1, 0.03, 0.01, 3e-3, 1e-3) to "six levels (the setting used)"
        ).forEach { (levels, label) ->
            val value = minimax(headlineIndices, leanStarts, levels).worstDishing / stroke
            add(
                T123ConvergenceRecord(
                    axis = "the smoothing homotopy, on the five-state minimax at lean starts",
                    setting = label,
                    worstDishingOverStroke = value,
                    departureFromFinest = 0.0
                )
            )
        }
        // the RANGE discretisation: does adding the interpolated intermediate gaps move it?
        val designSolved = designRange.keys.map { indexOf.getValue(profileAt(it).name) }
        val designInterpolated = designSolved + allProfiles.withIndex()
            .filter { it.value.interpolated && it.value.concentration == 2.0 }
            .map { it.index }
        listOf(
            designSolved to "the two SOLVED endpoints",
            designInterpolated to "the endpoints plus ${designRange.interpolations} INTERPOLATED gaps"
        ).forEach { (states, label) ->
            val value = surrogate.worstDishing(designRangeOptimum.stiffnesses, states) / stroke
            add(
                T123ConvergenceRecord(
                    axis = "the operating range's discretisation, at the ROBUST distribution",
                    setting = label,
                    worstDishingOverStroke = value,
                    departureFromFinest = abs(
                        value / (surrogate.worstDishing(
                            designRangeOptimum.stiffnesses, designInterpolated
                        ) / stroke) - 1.0
                    )
                )
            )
        }
    }

    // ------------------------------------------------------------------ gate 5 — reproductions
    println("T-123 — upstream reproductions ...")
    fun reproduction(source: String, quantity: String, published: Double, reproduced: Double) =
        T123ReproductionRecord(
            source, quantity, published, reproduced,
            if (published == 0.0) abs(reproduced) else abs(reproduced / published - 1.0)
        )

    val designState = indexOf.getValue(headline[0].name)
    val uniformLoadState = indexOf.getValue(uniformProfile.name)
    val reproductions = buildList {
        add(reproduction("C-0026", "free-tile stroke [nm]", 4.90731, stroke))
        add(
            reproduction(
                "C-0047/C-0058", "3 x 15 uniform dishing / stroke at the design point", 0.2182,
                uniformPeaks[designState] / stroke
            )
        )
        add(
            reproduction(
                "C-0058", "rim x 5 over 6.70 nm, dishing / stroke at the design point", 0.0753,
                rimPeaks[designState] / stroke
            )
        )
        add(
            reproduction(
                "C-0058", "rim x 5 at the 2 nm state, dishing / stroke", 0.1867,
                rimPeaks[indexOf.getValue(headline[4].name)] / stroke
            )
        )
        add(
            reproduction(
                "C-0058", "uniform at the 2 nm state, dishing / stroke", 0.0710,
                uniformPeaks[indexOf.getValue(headline[4].name)] / stroke
            )
        )
        add(
            reproduction(
                "C-0058", "uniform at the 10 mM state, dishing / stroke", 0.2551,
                uniformPeaks[indexOf.getValue(headline[2].name)] / stroke
            )
        )
        add(
            reproduction(
                "C-0058", "the five-state minimax worst case / stroke (a NOT FOUND, to be beaten)",
                0.1587, portfolio.worstDishing / stroke
            )
        )
        add(
            reproduction(
                "C-0017", "the mandated total coupling stiffness [pN/nm]", 33.3333333, T123_MANDATE
            )
        )
        add(
            reproduction(
                "C-0049", "the admissible stiffness ratio at 45 paths and the 3 nm stroke", 4.5,
                admissibleStiffnessRatio(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, T123_MANDATE, paths
                )
            )
        )
        add(
            reproduction(
                "C-0049", "the same at S3's DESIRED 10 nm stroke — it tightens as 1/s", 1.35,
                admissibleStiffnessRatio(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.DESIRED_STROKE, T123_MANDATE, paths
                )
            )
        )
        add(
            reproduction(
                "C-0014", "per-path thermal force at 45 equal paths [pN]",
                perAnchorThermalForce(T123_MANDATE, paths),
                perPathThermalForces(uniformStiffness).max()
            )
        )
        add(
            reproduction(
                "C-0060", "the stiff level of C-0058's rim x 5 rule [pN/nm]", 0.9208,
                rimStiffness.max()
            )
        )
        add(
            reproduction(
                "C-0060", "the soft level of the same [pN/nm]", 0.1842, rimStiffness.min()
            )
        )
        // the free falsifier: a uniform load on a free tile dishes exactly zero
        add(
            reproduction(
                "T-123 falsifier", "free tile, UNIFORM load, peak dishing [nm]", 0.0,
                bareLattice.solve(uniformPressure(interiorPressure)).peakDishing(T123_SAMPLES)
            )
        )
        add(
            reproduction(
                "T-123 falsifier", "free PLATE, UNIFORM load, peak dishing [nm]", 0.0,
                barePlate.solve(uniformPressure(interiorPressure)).peakDishing(T123_SAMPLES)
            )
        )
        // gate 5: the multi-state surrogate against C-0058's single-state one
        add(
            reproduction(
                "T-123 gate 5",
                "C-0058's InfluenceSurrogate vs this multi-state one, at the rim design [nm]",
                latticeInfluenceSurrogate(
                    bareLattice, grid, headline[0].field(interiorPressure, lengthY), T123_SAMPLES
                ).solve(rimStiffness).peakDishing,
                rimPeaks[designState]
            )
        )
        // gate 5: the surrogate against the ASSEMBLED solve, at this task's own answer
        add(
            reproduction(
                "T-123 gate 5",
                "assembled vs surrogate peak dishing at the ROBUST distribution [nm]",
                distributions.first {
                    it.label.startsWith("T-123 ROBUST") && it.profile == headline[0].name
                }.latticePeakDishing,
                surrogate.peakDishing(designRangeOptimum.stiffnesses)[designState]
            )
        )
        // the uniform-load state, where a coupling is the ONLY source of dishing
        add(
            reproduction(
                "T-123 diagnostic",
                "the free tile's own dishing under the UNIFORM load state [nm]", 0.0,
                surrogate.freeFieldPeak(uniformLoadState)
            )
        )
    }

    // ------------------------------------------------------------------ the findings
    val binding = portfolio.bindingStates
    val worstPair = tension.filter { it.optimisedAt != it.readAt }.maxBy { it.pairMinimaxOverStroke }
    val mostAntagonistic = tension.minBy { it.freeFieldCosine }
    val designUniform = designRange.keys.map { indexOf.getValue(profileAt(it).name) }
        .maxOf { uniformPeaks[it] } / stroke
    val twoLevel = buildability.first {
        it.label.startsWith("ROBUST") && it.levels == 2
    }
    /** What sixteen more starts are worth on the five-state answer, from gate 4. */
    val startDeparture = convergence.first {
        it.axis.startsWith("the number of starts") && it.setting.startsWith("16 ")
    }.departureFromFinest
    val homotopyWorth = convergence.first {
        it.axis.startsWith("the smoothing") && it.setting.startsWith("one level")
    }.worstDishingOverStroke / convergence.first {
        it.axis.startsWith("the smoothing") && it.setting.startsWith("six levels")
    }.worstDishingOverStroke - 1.0
    // The subset structure, which is the whole of "which states bind".
    val twoNanometre = surrogate.stateNames[headlineIndices[4]]
    val tenNanometreStates = headline.filter { it.gapHeight == 10.0 }.map { it.name }.toSet()
    // The dichotomy is not "contains the 2 nm state" — that state is compatible with the 5 nm
    // rest state, which belongs to its OWN device. It is "mixes the 2 nm state with a 10 nm one".
    val mixedSubsets = subsetRecords.filter {
        twoNanometre in it.members && it.members.any { name -> name in tenNanometreStates }
    }
    val unmixedSubsets = subsetRecords.filterNot {
        twoNanometre in it.members && it.members.any { name -> name in tenNanometreStates }
    }
    val withoutTwo = subsetRecords.filter { twoNanometre !in it.members }
    val twoNanometreCosines = tension.filter { it.optimisedAt == twoNanometre }.map { it.freeFieldCosine }
    val otherCosines = tension.filter {
        it.optimisedAt != twoNanometre && it.readAt != twoNanometre
    }.map { it.freeFieldCosine }
    val fourWithoutTwo = withoutTwo.first { it.size == 4 }
    val threeLevel = buildability.first { it.label.startsWith("ROBUST") && it.levels == 3 }
    val rimOverTheRange = rangeRecords.first().rimTimesFiveWorstOverStroke

    val findings = listOf(
        ("NO DISTRIBUTION IS FLAT AT ALL FIVE OF C-0022'S SOLVED STATES, AND IT IS NOW A " +
                "MEASURED TENSION RATHER THAN A NOT-FOUND. A smoothed minimax with analytic " +
                "gradients and %d starts — against C-0058's cyclic coordinate descent from " +
                "three — reaches a worst case of %.4f of the free-tile stroke against C-0058's " +
                "%.4f, an improvement of %.1f%% that leaves it still %.2fx T-5b's 0.10. THE " +
                "SEARCH IS NOT WHAT IS LIMITING: going from 16 starts to %d moves the answer by " +
                "%.1e, and the six-level smoothing homotopy — the thing a coordinate descent " +
                "does not have — is worth %.1f%% on its own, about a third of the improvement " +
                "over C-0058 with the gradient supplying the rest. Only %d of the %d starts " +
                "land within one part in a million of the best, " +
                "and that is the expected shape of a NONSMOOTH objective rather than evidence of " +
                "a stall: many basins, one value. The binding set at the optimum is %s — TWO " +
                "states active at once, which is what a minimax that has equalised looks like.")
            .format(
                richStarts.size, portfolio.worstDishing / stroke, 0.1587,
                100.0 * (1.0 - portfolio.worstDishing / stroke / 0.1587),
                portfolio.worstDishing / stroke / T123_TOLERANCE,
                richStarts.size, startDeparture, 100.0 * homotopyWorth,
                portfolio.startsWithinOnePartInAMillion, richStarts.size,
                binding.joinToString(" and ")
            ),
        ("THE ANTAGONIST IS ONE STATE, IT IS ANTAGONISTIC TO ALL FOUR OTHERS, AND THE SUBSET " +
                "STRUCTURE IS A CLEAN DICHOTOMY THAT RECOVERS THE DEVICES. Of the 31 non-empty " +
                "subsets of C-0022's five, every one of the %d that puts the 2 nm state " +
                "TOGETHER WITH A 10 nm ONE is NOT flat (%.4f to %.4f) and every one of the " +
                "other %d IS flat (%.4f to %.4f) — including the four-state set of everything " +
                "but the 2 nm state at %.4f, and including the 2 nm state paired with the 5 nm " +
                "rest state, which is its OWN device, at %.4f. So *which states bind* is not a " +
                "ranking and not a pair: it is one state, incompatible with one group. The " +
                "mechanism is visible before any optimiser runs. The free-tile dishing field of " +
                "the 2 nm state has a cosine of %+.3f to %+.3f against every other state — " +
                "NEGATIVE, and exactly %+.3f against the 10 mM one — while all six pairs among " +
                "the other four run %+.3f to %+.3f. C-0022 says why in one row of its own table: " +
                "the 2 nm state is the ONLY one of its 21 solved states whose finite tile " +
                "carries LESS total force than a 1-D pressure over the footprint (-3.91 %%), " +
                "i.e. the only one whose edge effect is a net LOSS. Its rim residual is %+.3f, " +
                "above one in magnitude so the load REVERSES SIGN inside the rim standoff, " +
                "against %+.3f at the design point. A distribution that flattens an edge " +
                "ENHANCEMENT deepens an edge DEFICIT: the tension is a SIGN, not a magnitude, " +
                "and no search can remove it. The worst pair in the matrix is %s with %s at " +
                "%.4f, outside the tolerance with the other three states absent.")
            .format(
                mixedSubsets.size, mixedSubsets.minOf { it.worstDishingOverStroke },
                mixedSubsets.maxOf { it.worstDishingOverStroke },
                unmixedSubsets.size, unmixedSubsets.minOf { it.worstDishingOverStroke },
                unmixedSubsets.maxOf { it.worstDishingOverStroke },
                fourWithoutTwo.worstDishingOverStroke,
                subsetRecords.first {
                    it.size == 2 && twoNanometre in it.members
                            && it.members.none { name -> name in tenNanometreStates }
                }.worstDishingOverStroke,
                twoNanometreCosines.max(), twoNanometreCosines.min(),
                mostAntagonistic.freeFieldCosine,
                otherCosines.min(), otherCosines.max(),
                stateRecords[4].rimResidualDepth, stateRecords[0].rimResidualDepth,
                worstPair.optimisedAt, worstPair.readAt, worstPair.pairMinimaxOverStroke
            ),
        ("BUT THE FIVE STATES ARE NOT AN OPERATING RANGE — THEY ARE FOUR DIFFERENT DEVICES. " +
                "Three of C-0022's five are the REST states of three different buffers at a " +
                "10 nm layer, and the other two are the rest and held states of a FIVE nanometre " +
                "layer: the 2 nm state is the 5 nm device at S3's 3 nm stroke and no state of " +
                "the 10 nm device at all. What a device traverses is one buffer, one layer and " +
                "one bias, from gap L0 down to L0 - s. Over C-0018's placed device — 2 mM, " +
                "10 nm, 0.192 V, gaps 10 -> 7 nm, BOTH ends solved by C-0022 at that bias — the " +
                "minimax reaches %.4f of the stroke, %s T-5b's 0.10, against %.4f for the " +
                "uniform coupling and %.4f for C-0058's rim rule. Adding %d interpolated " +
                "intermediate gaps moves it by %.1e. A ROBUST DISTRIBUTION EXISTS OVER THE RANGE " +
                "THE DEVICE ACTUALLY OCCUPIES; what does not exist is one flat across three " +
                "buffers and two layer heights, and no claim upstream ever asked for that.")
            .format(
                designRangeOptimum.worstDishing / stroke,
                if (designRangeOptimum.worstDishing / stroke < T123_TOLERANCE) "INSIDE"
                else "OUTSIDE",
                designUniform,
                rangeRecords.first().rimTimesFiveWorstOverStroke,
                designRange.interpolations,
                convergence.filter { it.axis.startsWith("the operating range") }
                    .maxOf { it.departureFromFinest }
            ),
        ("THE CHEAP BOUND DID NOT FIRE, AND SAYING SO IS WHAT MAKES THE NEGATIVE HONEST. " +
                "Dishing is affine in the attachment FORCES, so the least-squares minimum over " +
                "the whole of R^n bounds every distribution at every state from below, and the " +
                "states DECOUPLE under that relaxation — so the largest per-state floor is a " +
                "rigorous lower bound on the minimax. It is %.4f of the stroke over the five, " +
                "far below the 0.10 that would have PROVEN no flat distribution exists. So the " +
                "five-state answer is a not-found at a large budget and not an impossibility " +
                "theorem, and the reason the bound is loose is that it lets each state choose " +
                "its own forces, which is exactly the freedom the question forbids. Maxwell-" +
                "Betti reciprocity of the influence matrix, measured between two quadratures " +
                "rather than imposed, holds to %.1e.")
            .format(
                cheapBound.first().worstReachableFloorOverStroke, surrogate.reciprocityResidual
            ),
        ("THE BUILDABLE ROBUST DESIGN IS NOT THIS OPTIMUM QUANTISED — IT IS C-0058'S OWN " +
                "TWO-LEVEL RULE, AND PROJECTION IS NOT OPTIMISATION. The 45-parameter robust " +
                "optimum spans %.2fx in per-path stiffness and does NOT survive C-0060's two " +
                "levels: quantised optimally onto two it measures %.4f of the stroke — outside " +
                "T-5b's 0.10, though only by %.1f%% — at a level ratio of %.2f, BELOW C-0060's " +
                "measured flat window of 3.5 to 20. It needs THREE levels (%.4f of the stroke, " +
                "ratio %.2f, inside the window) to be flat, where C-0060 prices two, and that " +
                "is reported as the real failure it is. But a two-level family does not have to " +
                "be reached by projection: C-0058's OWN published rim x 5 rule over a 6.70 nm " +
                "collar — two levels, ratio exactly 5, the design C-0060 shows all seven " +
                "catalogue elements can build — is flat over the whole traversed range at " +
                "%.4f, %.1f%% BETTER than the projected optimum. SEARCHING INSIDE THE BUILDABLE " +
                "FAMILY BEATS QUANTISING OUTSIDE IT, and the buildability question therefore " +
                "does not reopen the range verdict.")
            .format(
                rangeRecords.first().maximumOverMinimumStiffness,
                twoLevel.worstDishingOverStroke,
                100.0 * (twoLevel.worstDishingOverStroke / T123_TOLERANCE - 1.0),
                twoLevel.levelRatio,
                threeLevel.worstDishingOverStroke, threeLevel.levelRatio,
                rimOverTheRange,
                100.0 * (1.0 - rimOverTheRange / twoLevel.worstDishingOverStroke)
            ),
        ("AND NOTHING IN THE ALLOWABLE STACK IS THREATENED BY EITHER. The robust optimum's " +
                "stiffest path carries %.3f pN/nm, i.e. %.3f pN at S3's acceptable 3 nm against " +
                "C-0006/CH-0029's 10 pN unzip allowable — %.2fx clear — and inside C-0049's " +
                "admissible ratio of %.2f, which tightens as 1/s to %.2f at S3's DESIRED 10 nm " +
                "stroke, where not even the ROBUST design is admissible. On the assembled " +
                "lattice the worst crossover carries %.3f pN (%.1fx clear of unzip) and the " +
                "worst duplex shear %.3f pN against the 48-65 pN band. C-0014's per-path " +
                "THERMAL force, which is LINEAR in a path's share and not the square root of " +
                "it, peaks at %.3f pN against the uniform coupling's %.3f — a %.0f %% premium, " +
                "the price of the non-uniformity and the largest cost item here.")
            .format(
                rangeRecords.first().peakPathStiffness,
                rangeRecords.first().peakPathForceAtAcceptableStroke,
                rangeRecords.first().unzipMargin,
                admissibleStiffnessRatio(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, T123_MANDATE, paths
                ),
                admissibleStiffnessRatio(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.DESIRED_STROKE, T123_MANDATE, paths
                ),
                distributions.first {
                    it.label.startsWith("T-123 ROBUST") && it.profile == headline[0].name
                }.peakCrossoverForce,
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / distributions.first {
                    it.label.startsWith("T-123 ROBUST") && it.profile == headline[0].name
                }.peakCrossoverForce,
                distributions.first {
                    it.label.startsWith("T-123 ROBUST") && it.profile == headline[0].name
                }.peakDuplexShear,
                rangeRecords.first().peakThermalForce,
                perPathThermalForces(uniformStiffness).max(),
                100.0 * (rangeRecords.first().peakThermalForce /
                        perPathThermalForces(uniformStiffness).max() - 1.0)
            )
    )

    val result = T123Result(
        task = "T-123",
        leaf = "A8.2",
        title = "Is ANY distribution flat at every one of C-0022's solved states?",
        verificationType = "in-silico (C-0009's grillage and C-0006's plate under C-0022's " +
                "SOLVED load, on an exact multi-state Woodbury surrogate with analytic " +
                "gradients) + logical (a per-state least-squares bound in the space of " +
                "attachment forces, which bounds every distribution whatever)",
        acceptance = "C-0058's minimax re-run with a real optimiser, more starts and the " +
                "worst-case dishing as the objective, over all five of C-0022's solved states; " +
                "which states bind and why; the operating range the requirement is owed over; " +
                "the cost and the buildability consumed from C-0060",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 = 1 MPa exactly",
            "energy" to "pN*nm"
        ),
        conventions = listOf(
            "x runs ALONG the helices, y ACROSS them; the origin is the tile centre",
            "w is positive DOWNWARD, compressing the polymer layer (T-5)",
            "dishing is the peak absolute departure from the area-weighted least-squares " +
                    "best-fit PLANE, sampled on the same 81 x 81 grid as C-0026, CH-0034, " +
                    "C-0047, C-0058 and C-0060",
            "the free-tile stroke is the mean deflection of the UNSUPPORTED plate under the " +
                    "UNIFORM load at the same foundation stiffness",
            "flat means peak dishing below 10% of that stroke — T-5b's CONVENTION, not a " +
                    "physical threshold",
            "a collar depth is NEGATIVE for an enhancement, which is the sign C-0022 solved; " +
                    "its rim residual may exceed one in magnitude, which means the load " +
                    "REVERSES SIGN inside the collar",
            "the coupling is n springs whose stiffnesses SUM to C-0017's mandate; the " +
                    "distribution is the design variable and the sum is not",
            "a STATE is a (concentration, gap, bias) triple of C-0022's solved profiles, and " +
                    "every lookup here is keyed on all three",
            "an operating RANGE is the set of states one device traverses: one buffer, one " +
                    "layer height, one bias, from gap L0 down to L0 - s"
        ),
        runParameters = mapOf(
            "duplexes" to "$T123_DUPLEXES",
            "attachments" to "$paths as $T123_COLUMNS x $T123_DUPLEXES (C-0015's grid)",
            "interhelicalDistance" to "${Gen1Tile.INTERHELICAL_SHEET} nm (SAXS, Fischer 2016)",
            "crossoverColumns" to "$T123_NOMINAL_COLUMNS, symmetrically centred (T-10)",
            "subdivisions" to "2 per interval, nested 1/2/4 in gate 4",
            "plateBasisDegree" to "12",
            "dishingSamples" to "$T123_SAMPLES x $T123_SAMPLES",
            "foundationStiffness" to "${Gen1Tile.FOUNDATION_SECANT} pN/nm^3 (C-0001's secant)",
            "loadStates" to "${allProfiles.size} (5 headline + the traversed ranges + " +
                    "interpolated intermediates + the uniform-load falsifier)",
            "optimiser" to "smoothed minimax (log-sum-exp) with analytic gradients through the " +
                    "Woodbury solve, nonlinear conjugate gradients on the log-weights, six " +
                    "smoothing levels x 25 iterations, ${richStarts.size} starts, then " +
                    "C-0058's own coordinate descent as a polish stage",
            "perPathCeiling" to "$ceiling pN/nm — the 10 pN unzip allowable at the 3 nm stroke"
        ),
        citedInputs = mapOf(
            "C-0017 mandate" to "$T123_MANDATE pN/nm = 100 pN / 3 nm",
            "C-0006/CH-0029 unzip allowable" to "${Gen1Tile.DUPLEX_UNZIP_ALLOWABLE} pN per path",
            "C-0006 duplex shear allowable" to "${Gen1Tile.DUPLEX_SHEAR_ALLOWABLE} pN, " +
                    "${Gen1Tile.OVERSTRETCHING_CEILING} pN nicked ceiling",
            "C-0022 solved collars" to "read at run time from " +
                    "gpd/results/T-3b-tile-edge-load-profile.json, keyed on " +
                    "(concentration, gap, bias)",
            "T-5b tolerance" to "$T123_TOLERANCE — a CONVENTION",
            "duplex EI" to "${Gen1Tile.DUPLEX_BENDING_RIGIDITY} pN*nm^2 — a CanDo MODEL INPUT",
            "C-0060 flat ratio window" to "$T123_FLAT_WINDOW_LOW to $T123_FLAT_WINDOW_HIGH, MEASURED there",
            "C-0060 coarsest quantum" to "19.1 % of a level's own stiffness (an 11 bp hinge arm)",
            "C-0060 scatter tolerance" to "34.6 % relative, at which flatness is lost",
            "C-0018/C-0032 operating point" to "10 nm layer, 2 mM with 1-3 % pull-in margin, " +
                    "0.5 mM clearing every predicate",
            "S3 parameters" to "${Gen1Tile.TARGET_FORCE} pN, ${Gen1Tile.ACCEPTABLE_STROKE} nm " +
                    "acceptable, ${Gen1Tile.DESIRED_STROKE} nm desired, 40 x 40 nm"
        ),
        temperature = ROOM_TEMPERATURE,
        thermalEnergy = thermalEnergy(),
        rigidPlateTolerance = T123_TOLERANCE,
        mandatedTotalStiffness = T123_MANDATE,
        freeTileStroke = stroke,
        states = stateRecords,
        cheapBound = cheapBound,
        tension = tension,
        subsets = subsetRecords,
        ranges = rangeRecords,
        distributions = distributions,
        paths = pathRecords,
        buildability = buildability,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings,
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the " +
                    "flexure motif this coupling belongs to is NOT DEMONSTRATED (C-0028, C-0029).",
            "EVERY STATION HERE IS C-0026's 3 x 15 GRID, which CH-0074/C-0061 show no placement " +
                    "claim supplies. C-0063 (T-125, the same iteration) RESOLVES CH-0074 the " +
                    "other way — a 34-root placement on C-0055's upward lattice is flat at " +
                    "0.0706 with EQUAL springs — but it is a DIFFERENT station set, and C-0058's " +
                    "rim rule REVERSES SIGN on it (0.0706 uniform against 0.2214 at x5). So no " +
                    "distribution in this study transfers to it, and the state-robustness " +
                    "question is OPEN on C-0063's placement, where C-0063 itself reports one " +
                    "state only.",
            "The load profiles are C-0022's and inherit its whole validity range: mean field, " +
                    "point ions, a two-dimensional solve with the corner bracketed rather than " +
                    "solved, an unsourced rim charge worth 1.85x on the collar depth, and a gap " +
                    "filled with free buffer.",
            "The INTERPOLATED intermediate gaps are linear interpolations of C-0022's solved " +
                    "(depth, width, rim) triples between two solved endpoints. They are a " +
                    "discretisation check on the range, NOT new solves, and no verdict rests " +
                    "on them.",
            "Where C-0022 did not solve a device's compressed end at its own bias, the range " +
                    "BRACKETS it with the two neighbouring solved biases — a wider requirement, " +
                    "not a narrower one.",
            "Linear Winkler foundation at C-0001's secant, one multiplier only.",
            "The coupling is n INDEPENDENT LINEAR springs. C-0030's flexure strain-softens " +
                    "(CH-0042), so a real coupling is not exactly this one.",
            "The optimiser is a DESCENT reporting the best point it found, never a global " +
                    "optimum. The per-state bound it is quoted against is rigorous; the optimum " +
                    "is not, so a five-state NEGATIVE is a not-found at a large budget.",
            "One crossover layout — T-10's eight symmetrically centred columns; C-0015's 32 bp " +
                    "phase is not swept.",
            "T-5b's 10 % is a CONVENTION and every verdict here is quoted with it named.",
            "Single layer, static, 300 K, aqueous buffer with Mg2+."
        ),
        openQuestions = listOf(
            "Whether the two-state incompatibility is a THEOREM. The bound used here decouples " +
                    "the states and is therefore loose; the tight relaxation is a semidefinite " +
                    "programme over the common compliance operator T = (M + diag(1/k))^-1, " +
                    "which was not implemented.",
            "WHETHER C-0063's FLAT PLACEMENT IS FLAT OVER A RANGE. C-0063 reports its 0.0706 " +
                    "at ONE state, the same design point C-0058 was tuned at — and this study's " +
                    "whole finding is that a flatness verdict at one state does not travel. The " +
                    "multi-state surrogate and the minimax here are exactly the instrument, and " +
                    "the 34 upward roots are exactly the station set to run them on.",
            "Whether the device is required to be flat across BUFFERS at all: if a Gen-1 tile " +
                    "is to run in more than one salt without re-tuning, the five-state answer " +
                    "is the one that governs and it is negative.",
            "The foundation multiplier, held at C-0001's secant throughout."
        )
    )

    val output = File("gpd/results/T-123-robust-distribution.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            json.encodeToJsonElement(result).jsonObject.roundedForCouplingResult().jsonObject
        )
    )
    t123Report(result, output, started)
}

private fun t123Report(result: T123Result, output: File, started: Long) {
    println()
    println("=".repeat(126))
    println("T-123 — ${result.title}")
    println("=".repeat(126))
    println(
        "free-tile stroke %.5f nm; mandate %.4f pN/nm; tolerance %.2f of the stroke"
            .format(result.freeTileStroke, result.mandatedTotalStiffness, result.rigidPlateTolerance)
    )

    println()
    println("--- C-0022's five headline states ".padEnd(126, '-'))
    println(
        "%-26s %8s %8s %8s %9s %9s %9s %9s %9s".format(
            "state", "depth", "width", "rim", "free/str", "unif/str", "rim5/str", "own opt", "floor"
        )
    )
    result.states.forEach {
        println(
            "%-26s %8.3f %8.2f %8.3f %9.4f %9.4f %9.4f %9.4f %9.4f".format(
                it.name.take(26), it.collarDepth, it.collarWidth, it.rimResidualDepth,
                it.freeTileDishingOverStroke, it.uniformDishingOverStroke,
                it.rimTimesFiveDishingOverStroke, it.singleStateOptimumOverStroke,
                it.reachableFloorOverStroke
            )
        )
    }

    println()
    println("--- the cheap bound ".padEnd(126, '-'))
    println("%-52s %7s %12s %10s %s".format("set", "states", "floor/str", "fired?", "verdict"))
    result.cheapBound.forEach {
        println(
            "%-52s %7d %12.5f %10s %s".format(
                it.set.take(52), it.states, it.worstReachableFloorOverStroke, it.falsifierFired,
                it.verdict.take(48)
            )
        )
    }

    println()
    println("--- the tension matrix ".padEnd(126, '-'))
    println(
        "%-26s %-26s %10s %10s %9s %9s %9s".format(
            "optimised at", "read at", "there/str", "own/str", "penalty", "cosine", "pair/str"
        )
    )
    result.tension.forEach {
        println(
            "%-26s %-26s %10.4f %10.4f %9.2f %+9.3f %9.4f".format(
                it.optimisedAt.take(26), it.readAt.take(26), it.dishingOverStroke,
                it.ownOptimumOverStroke, it.penalty, it.freeFieldCosine, it.pairMinimaxOverStroke
            )
        )
    }

    println()
    println("--- every subset of the five ".padEnd(126, '-'))
    println(
        "%-58s %5s %10s %7s %10s %9s".format(
            "members", "n", "worst/str", "flat", "unif/str", "F@3nm"
        )
    )
    result.subsets.forEach {
        println(
            "%-58s %5d %10.4f %7s %10.4f %9.3f".format(
                it.members.joinToString("; ") { name -> name.take(16) }.take(58), it.size,
                it.worstDishingOverStroke, it.flat, it.uniformWorstOverStroke,
                it.peakPathForceAtAcceptableStroke
            )
        )
    }

    println()
    println("--- the operating ranges ".padEnd(126, '-'))
    println(
        "%-46s %6s %10s %7s %10s %10s %9s".format(
            "device", "states", "worst/str", "flat", "unif/str", "rim5/str", "F@3nm"
        )
    )
    result.ranges.forEach {
        println(
            "%-46s %6d %10.4f %7s %10.4f %10.4f %9.3f".format(
                it.device.take(46), it.members.size, it.worstDishingOverStroke, it.flat,
                it.uniformWorstOverStroke, it.rimTimesFiveWorstOverStroke,
                it.peakPathForceAtAcceptableStroke
            )
        )
    }

    println()
    println("--- the assembled solves ".padEnd(126, '-'))
    println(
        "%-52s %-24s %9s %8s %9s %9s %9s".format(
            "distribution", "state", "dish/str", "flat", "xover", "shear", "therm"
        )
    )
    result.distributions.forEach {
        println(
            "%-52s %-24s %9.4f %8s %9.4f %9.4f %9.4f".format(
                it.label.take(52), it.profile.take(24), it.dishingOverStroke, it.flat,
                it.peakCrossoverForce, it.peakDuplexShear, it.peakThermalForce
            )
        )
    }

    println()
    println("--- buildability, on C-0060's numbers ".padEnd(126, '-'))
    println(
        "%-46s %7s %8s %9s %8s %10s %9s".format(
            "distribution", "levels", "distinct", "ratio", "window", "worst/str", "penalty"
        )
    )
    result.buildability.forEach {
        println(
            "%-46s %7d %8d %9.3f %8s %10.4f %+9.2f%%".format(
                it.label.take(46), it.levels, it.distinctLevels, it.levelRatio,
                it.insideTheMeasuredFlatWindow, it.worstDishingOverStroke,
                100.0 * it.quantisationPenalty
            )
        )
    }

    println()
    println("--- gate 4: convergence ".padEnd(126, '-'))
    println("%-58s %-42s %13s %12s".format("axis", "setting", "value", "departure"))
    result.convergence.forEach {
        println(
            "%-58s %-42s %13.7f %12.3e".format(
                it.axis.take(58), it.setting.take(42), it.worstDishingOverStroke,
                it.departureFromFinest
            )
        )
    }

    println()
    println("--- gate 5: upstream reproductions ".padEnd(126, '-'))
    println(
        "%-18s %-62s %11s %11s %11s".format("source", "quantity", "published", "here", "departure")
    )
    result.reproductions.forEach {
        println(
            "%-18s %-62s %11.5f %11.5f %11.2e".format(
                it.source, it.quantity.take(62), it.published, it.reproduced, it.relativeDeparture
            )
        )
    }

    println()
    println("--- findings ".padEnd(126, '-'))
    result.findings.forEachIndexed { index, finding ->
        println("${index + 1}. $finding")
        println()
    }

    println(
        "wrote ${output.path} in %.1f s".format((System.currentTimeMillis() - started) / 1000.0)
    )
}
