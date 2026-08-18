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
import com.xemantic.nano.plentyofroom.coupling.LoadState
import com.xemantic.nano.plentyofroom.coupling.MultiStateSurrogate
import com.xemantic.nano.plentyofroom.coupling.admissibleStiffnessRatio
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.minimaxStiffnessDistribution
import com.xemantic.nano.plentyofroom.coupling.normalisedStiffnesses
import com.xemantic.nano.plentyofroom.coupling.perPathStiffnessCeiling
import com.xemantic.nano.plentyofroom.coupling.perPathThermalForces
import com.xemantic.nano.plentyofroom.coupling.rimStiffenedWeights
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundForResult
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
 * `T-129` — is `C-0063`'s flat 34-root placement flat over the states a **device** traverses?
 *
 * Emits `gpd/results/T-129-range-robust-placement.json`.
 */

private const val DUPLEXES = 15
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private const val C0058_COLLAR_WIDTH = 6.70
private const val C0063_PHASE = 24
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** The starts each range minimax is run from, and each subset minimax. */
private const val RANGE_STARTS = 24
private const val SUBSET_STARTS = 12

/** §3's own grafting densities at the two layer heights `C-0022` solved (`C-0050`'s table). */
private const val S3_SIGMA_AT_TEN = 0.024
private const val S3_SIGMA_AT_FIVE = 0.092

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T129BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String,
    val falsifierFired: Boolean
)

@Serializable
private data class T129StateRecord(
    val state: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val freeDishingOverStroke: Double,
    val equalSpringDishingOverStroke: Double,
    val reachableFloorOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val beatsNoCouplingAtAll: Boolean,
    val occupiedByTenNanometreDevice: Boolean,
    val strokeDemandedOfTenNanometreDevice: Double
)

@Serializable
private data class T129CosineRecord(
    val first: String,
    val second: String,
    val cosine: Double,
    val sameDevice: Boolean,
    val equalSpringWorstOverStroke: Double
)

@Serializable
private data class T129RangeRecord(
    val device: String,
    val rationale: String,
    val restingHeight: Double,
    val states: List<String>,
    val floorOverStroke: Double,
    val equalSpringWorstOverStroke: Double,
    val minimaxWorstOverStroke: Double,
    val bindingStates: List<String>,
    val flatWithEqualSprings: Boolean,
    val flatWithADistribution: Boolean,
    val minimaxPeakRatio: Double,
    val peakPathStiffness: Double,
    val peakPathForceAtAcceptableStroke: Double,
    val peakSolvedPathForce: Double,
    val peakThermalForce: Double,
    val withinUnzipAllowable: Boolean,
    val startsUsed: Int,
    val startsWithinOnePartInAMillion: Int
)

@Serializable
private data class T129SubsetRecord(
    val states: List<String>,
    val size: Int,
    val containsTwoNanometreState: Boolean,
    val containsTenNanometreState: Boolean,
    val equalSpringWorstOverStroke: Double,
    val minimaxWorstOverStroke: Double,
    val flatWithEqualSprings: Boolean,
    val flatWithADistribution: Boolean
)

@Serializable
private data class T129PlacementRecord(
    val family: String,
    val phaseBasePairs: Int,
    val objective: String,
    val enumerated: Int,
    val bestOverStroke: Double,
    val medianOverStroke: Double,
    val worstOverStroke: Double,
    val bestKey: String,
    val bestFlatAtTenPercent: Boolean,
    val bestAtDesignStateOverStroke: Double,
    val c0063OverStroke: Double,
    val placementsBetterThanC0063: Int,
    val beatsC0063: Boolean
)

@Serializable
private data class T129DistributionRecord(
    val placement: String,
    val rule: String,
    val ratio: Double,
    val designStateOverStroke: Double,
    val rangeWorstOverStroke: Double,
    val flatOverRange: Boolean,
    val peakPathForceAtAcceptableStroke: Double,
    val peakThermalForce: Double
)

@Serializable
private data class T129ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T129ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T129PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T129Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T129BoundRecord>,
    val states: List<T129StateRecord>,
    val cosines: List<T129CosineRecord>,
    val ranges: List<T129RangeRecord>,
    val subsets: List<T129SubsetRecord>,
    val placements: List<T129PlacementRecord>,
    val distributions: List<T129DistributionRecord>,
    val convergence: List<T129ConvergenceRecord>,
    val reproductions: List<T129ReproductionRecord>,
    val predicates: List<T129PredicateRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the states, read from `C-0022`'s own result file
// ---------------------------------------------------------------------------------------------

private class T129Profile(
    val name: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {

    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, RIM_STANDOFF))
    )

}

/**
 * `C-0022`'s solved profiles, keyed on **`(concentration, gapHeight, appliedBias)`**.
 *
 * `CLAUDE.md`'s trap, avoided by construction: the file carries more than one profile per
 * `(concentration, gap)` — one per operating bias — so keying on two of the three silently takes
 * whichever is listed first, at a bias no headline table uses.
 */
private fun t129SolvedProfiles(file: File): List<T129Profile> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-129 reads the SOLVED edge profiles and " +
                "will not substitute assumed ones for them."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .map { record ->
            fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
            T129Profile(
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

/** `C-0022`'s five headline states, which `C-0058` and `C-0064` both quote. */
private val T129_HEADLINE_KEYS: List<Triple<Double, Double, Double>> = listOf(
    Triple(2.0, 10.0, 0.192),
    Triple(0.5, 10.0, 0.134),
    Triple(10.0, 10.0, 0.192),
    Triple(2.0, 5.0, 0.368),
    Triple(2.0, 2.0, 0.368)
)

/**
 * The operating **ranges** — `C-0064`'s own four devices, re-declared here rather than imported,
 * because a range is a *premise* of this task and not a number to be inherited.
 */
private class T129Range(
    val device: String,
    val rationale: String,
    val restingHeight: Double,
    val keys: List<Triple<Double, Double, Double>>
)

private val T129_RANGES = listOf(
    T129Range(
        device = "2 mM, L0 = 10 nm, 0.192 V (C-0018's placed device)",
        rationale = "C-0018 places this device with 1-3 % of pull-in margin; S3's acceptable " +
                "stroke is 3 nm, so it traverses gaps 10 -> 7 nm at its OWN operating bias, and " +
                "C-0022 solved both ends of that at 0.192 V",
        restingHeight = 10.0,
        keys = listOf(Triple(2.0, 10.0, 0.192), Triple(2.0, 7.0, 0.192))
    ),
    T129Range(
        device = "0.5 mM, L0 = 10 nm, 0.134 V (C-0032's recommendation)",
        rationale = "C-0032 finds every predicate clears at 0.5 mM where the 2 mM design sits ON " +
                "its own fold; C-0022 did not solve the compressed end at 0.134 V, so the range " +
                "BRACKETS it with the 0.082 V and 0.155 V profiles it did solve at 7 nm",
        restingHeight = 10.0,
        keys = listOf(
            Triple(0.5, 10.0, 0.134), Triple(0.5, 7.0, 0.082), Triple(0.5, 7.0, 0.155)
        )
    ),
    T129Range(
        device = "2 mM, L0 = 5 nm, 0.368 V",
        rationale = "the rest and held states of the FIVE nanometre layer — C-0022's 2 nm state " +
                "is this device held at S3's 3 nm stroke, and it is the ONLY device that occupies " +
                "a 2 nm gap at all",
        restingHeight = 5.0,
        keys = listOf(Triple(2.0, 5.0, 0.368), Triple(2.0, 2.0, 0.368))
    ),
    T129Range(
        device = "10 mM, L0 = 10 nm, 0.192 V",
        rationale = "the third buffer of C-0022's headline five, with its compressed end " +
                "bracketed by the 0.082 V and 0.155 V profiles solved at 7 nm",
        restingHeight = 10.0,
        keys = listOf(
            Triple(10.0, 10.0, 0.192), Triple(10.0, 7.0, 0.082), Triple(10.0, 7.0, 0.155)
        )
    )
)

// ---------------------------------------------------------------------------------------------
// the placement, read from `C-0063`'s own result file
// ---------------------------------------------------------------------------------------------

/** `C-0063`'s winning placement, read from `T-125`'s result file rather than retyped. */
private fun c0063Placement(file: File): UpwardArmPlacement {
    require(file.exists()) {
        "C-0063's result file is missing: ${file.path}. T-129 asks whether ITS placement is flat " +
                "over a range and will not substitute a reconstruction for it."
    }
    val rows = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
    val phases = rows.map { it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() }.distinct()
    require(phases == listOf(C0063_PHASE)) {
        "C-0063's placement must be at phase $C0063_PHASE, was: $phases"
    }
    return UpwardArmPlacement(
        C0063_PHASE,
        rows.map { row ->
            val roots = row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() }
            UpwardArmRow(
                row = row.getValue("row").jsonPrimitive.content.toInt(),
                roots = roots,
                towardPositiveX = row.getValue("towardPositiveX").jsonArray
                    .map { it.jsonPrimitive.content.toBoolean() }
            )
        }.sortedBy { it.row }
    )
}

/**
 * `C-0050`'s dead-load stroke at §3's own 100 pN for a layer resting at [restingHeight], taken as
 * the **largest** over its six layer models — and, when [graftingDensity] is `null`, over every
 * grafting density it solved at that height as well.
 *
 * The largest is the **most permissive** reading, so an exclusion drawn from it is an exclusion at
 * every model and every density in `C-0027`'s window, not only at §3's nominal one.
 */
private fun c0050DeadLoadStroke(
    file: File,
    restingHeight: Double,
    graftingDensity: Double? = null
): Double {
    require(file.exists()) {
        "C-0050's result file is missing: ${file.path}. T-129 needs its dead-load stroke to say " +
                "which gaps a device can occupy AT ALL, and will not assume one."
    }
    val strokes = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("reach").jsonArray.map { it.jsonObject }
        .filter { it.getValue("nominalHeight").jsonPrimitive.content.toDouble() == restingHeight }
        .filter { record ->
            graftingDensity == null ||
                    abs(
                        record.getValue("graftingDensity").jsonPrimitive.content.toDouble() -
                                graftingDensity
                    ) < 1e-12
        }
        .map { it.getValue("deadLoadStroke").jsonPrimitive.content.toDouble() }
    require(strokes.isNotEmpty()) { "C-0050 solved no layer resting at $restingHeight nm" }
    return strokes.max()
}

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t129Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t129Lattice(
    sheet: OrigamiSheet,
    columns: CrossoverLayout,
    subdivisions: Int = 2
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = columns,
    subdivisions = subdivisions,
    supports = emptyList()
)

/**
 * A deterministic log-normal ensemble of starts — a seeded linear congruential generator, so the
 * ensemble is a fixed table and not a random one (`gpd/README.md`: a re-run that changes nothing
 * must produce no diff).
 */
private fun t129RandomStarts(paths: Int, count: Int, spread: Double): List<List<Double>> {
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
    val sheet = t129Sheet()
    val edgeX = Gen1Tile.EDGE_X
    val lengthY = DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)
    val arm = C0055_ARM_LENGTH
    val count = C0055_ARM_COUNT
    val ceiling = perPathStiffnessCeiling(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
    )
    val admissible = admissibleStiffnessRatio(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, MANDATE, count
    )

    println("T-129 — reading C-0022's solved loads, C-0063's placement and C-0050's stroke ...")
    val solved = t129SolvedProfiles(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    fun profileAt(key: Triple<Double, Double, Double>): T129Profile = solved.firstOrNull {
        it.concentration == key.first && it.gapHeight == key.second && it.appliedBias == key.third
    } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")

    val placement = c0063Placement(File("gpd/results/T-125-upward-root-placement.json"))
    require(placement.count == count) {
        "C-0063's placement must carry $count roots, carried ${placement.count}"
    }
    val lattice24 = upwardRootLattice(C0063_PHASE, edgeX, DUPLEXES)
    placement.rows.forEach { row ->
        row.roots.forEach { root ->
            require(lattice24[row.row].any { abs(it - root) < 1e-9 }) {
                "root $root of row ${row.row} is not on the phase-$C0063_PHASE upward lattice"
            }
        }
    }
    require(placement.isCentroSymmetric(DUPLEXES)) {
        "C-0063's placement is reported centro-symmetric and this one is not"
    }
    val reachFile = File("gpd/results/T-108-desired-stroke-reach.json")
    // The permissive reading — the largest anywhere at a 10 nm layer, over every model and every
    // grafting density C-0027's window contains.
    val deadLoadStrokeAtTen = c0050DeadLoadStroke(reachFile, 10.0)
    // And at §3's own grafting density for that height, which is what C-0050 quotes.
    val deadLoadStrokeAtTenDesign = c0050DeadLoadStroke(reachFile, 10.0, S3_SIGMA_AT_TEN)
    val deadLoadStrokeAtFive = c0050DeadLoadStroke(reachFile, 5.0)

    // ------------------------------------------------------------------ the states
    val headline = T129_HEADLINE_KEYS.map { profileAt(it) }
    val rangeProfiles = T129_RANGES.flatMap { range -> range.keys.map { profileAt(it) } }
    // Two linearly interpolated intermediate gaps of the design device's own range, as a
    // DISCRETISATION check only — C-0064's item, re-run on this station set.
    val designRange = T129_RANGES.first()
    val designEnds = designRange.keys.map { profileAt(it) }
    val interpolated = (1..2).map { step ->
        val f = step.toDouble() / 3.0
        val a = designEnds[0]
        val b = designEnds[1]
        T129Profile(
            name = "INTERPOLATED %.1f mM, %.2f nm, %.3f V".format(
                a.concentration, a.gapHeight + f * (b.gapHeight - a.gapHeight), a.appliedBias
            ),
            concentration = a.concentration,
            gapHeight = a.gapHeight + f * (b.gapHeight - a.gapHeight),
            appliedBias = a.appliedBias,
            smoothDepth = a.smoothDepth + f * (b.smoothDepth - a.smoothDepth),
            smoothWidth = a.smoothWidth + f * (b.smoothWidth - a.smoothWidth),
            rimDepth = a.rimDepth + f * (b.rimDepth - a.rimDepth)
        )
    }
    val uniformProfile = T129Profile(
        "uniform load (the falsifier case)", 0.0, 0.0, 0.0, 0.0, 1.0, 0.0
    )
    val profiles = (headline + rangeProfiles + interpolated + uniformProfile).distinctBy { it.name }
    val indexOfState = profiles.withIndex().associate { (index, p) -> p.name to index }
    val loadStates = profiles.map { LoadState(it.name, it.field(interiorPressure, lengthY)) }
    val designState = indexOfState.getValue(headline[0].name)
    val uniformState = indexOfState.getValue(uniformProfile.name)
    val twoNanometreState = indexOfState.getValue(headline[4].name)
    println("T-129 — ${profiles.size} load states; C-0063's placement carries $count roots")

    val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    // ------------------------------------------------------- the bank over every phase-24 root
    println("T-129 — the multi-state bank over every candidate root at phase $C0063_PHASE ...")
    val host24 = t129Lattice(sheet, CrossoverLayout.atBasePairPhase(C0063_PHASE, sheet, edgeX))
    val sites24 = lattice24.flatMapIndexed { row, xs ->
        xs.map { it to (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
    }
    val bank24 = MultiStateRootBank(host24, sites24, loadStates)
    val c0063Indices = placement.stations(DUPLEXES).map { (x, y) ->
        val index = bank24.indexOf(x, y)
        require(index >= 0) { "($x, $y) is not an upward site of phase $C0063_PHASE" }
        index
    }
    val c0063Surrogate = bank24.surrogateFor(c0063Indices)
    val equal = List(count) { MANDATE / count }
    val equalPeaks = c0063Surrogate.peakDishing(equal)

    // ------------------------------------------------------------------ the cheap bounds
    println("T-129 — the cheap bounds, which run before any optimisation ...")
    val floors = profiles.indices.associateWith {
        c0063Surrogate.reachableDishingFloor(it) / freeStroke
    }
    fun rangeIndices(range: T129Range) = range.keys.map { indexOfState.getValue(profileAt(it).name) }
    val worstRangeFloor = T129_RANGES.maxOf { range -> rangeIndices(range).maxOf { floors.getValue(it) } }
    val equalRangeWorst = T129_RANGES.associate { range ->
        range.device to c0063Surrogate.worstDishing(equal, rangeIndices(range)) / freeStroke
    }
    val designCosines = profiles.indices.filter { it != uniformState }.map { state ->
        c0063Surrogate.freeFieldCosine(designState, state)
    }

    val bounds = listOf(
        T129BoundRecord(
            name = "the per-state least-squares floor, worst over the four device ranges",
            value = worstRangeFloor, unit = "of the free-tile stroke",
            settles = "no distribution whatever on C-0063's 34 stations can beat this at that " +
                    "state, so a floor above 0.10 would prove the placement is a single-state " +
                    "result; dishing is affine in the attachment forces and every distribution " +
                    "produces some force vector",
            falsifierFired = worstRangeFloor > FLATNESS_TOLERANCE
        ),
        T129BoundRecord(
            name = "the worst free-field cosine of the design state against another state",
            value = designCosines.min(), unit = "dimensionless",
            settles = "C-0064's instrument: a negative cosine means the two loads dish the free " +
                    "tile in opposite senses and no one correction serves both — it costs one " +
                    "pass over two precomputed fields and needs no optimiser",
            falsifierFired = false
        ),
        T129BoundRecord(
            name = "the stroke C-0022's 2 nm state demands of a 10 nm device",
            value = strokeToOccupy(10.0, 2.0), unit = "nm",
            settles = "against C-0050's dead-load stroke at S3's own 100 pN — the largest of its " +
                    "six layer models at the 10 nm design point — which is what makes the " +
                    "exclusion of that state PHYSICAL rather than convenient",
            falsifierFired = gapOccupiable(10.0, 2.0, deadLoadStrokeAtTen)
        ),
        T129BoundRecord(
            name = "C-0050's dead-load stroke at a 10 nm layer, the largest anywhere it solved",
            value = deadLoadStrokeAtTen, unit = "nm",
            settles = ("the ceiling the 8 nm demand above is read against, taken over all six " +
                    "layer models AND every grafting density of C-0027's 10 nm window (at S3's " +
                    "own sigma = %.3f nm^-2 it is %.4f nm); C-0017's own theorem is that a " +
                    "coupling can only REDUCE the delivered stroke, so this is a supremum over " +
                    "every coupling that could ever be designed")
                .format(S3_SIGMA_AT_TEN, deadLoadStrokeAtTenDesign),
            falsifierFired = false
        ),
        T129BoundRecord(
            name = "C-0050's dead-load stroke at S3's own 5 nm layer, the largest of six models",
            value = deadLoadStrokeAtFive, unit = "nm",
            settles = "reported because it does NOT clear the 3 nm its own device needs to reach " +
                    "C-0022's 2 nm state: that state is held by its own solved 0.368 V bias and " +
                    "not by a 100 pN dead load, which is a fact about C-0022's state set and is " +
                    "recorded rather than smoothed",
            falsifierFired = gapOccupiable(5.0, 2.0, deadLoadStrokeAtFive)
        ),
        T129BoundRecord(
            name = "34 equal springs on C-0063's placement, worst over the design device's range",
            value = equalRangeWorst.getValue(T129_RANGES[0].device),
            unit = "of the free-tile stroke",
            settles = "the headline, and it costs one Cholesky per state: whether C-0063's " +
                    "equal-spring advantage survives the range its own device traverses",
            falsifierFired = equalRangeWorst.getValue(T129_RANGES[0].device) > FLATNESS_TOLERANCE
        )
    )
    bounds.forEach {
        println("  %-64s %10.4f %s".format(it.name, it.value, it.unit))
    }

    // ------------------------------------------------------------------ the per-state table
    val stateRecords = profiles.mapIndexed { index, profile ->
        val demanded = if (profile.gapHeight in 0.0..10.0 && profile.gapHeight > 0.0) {
            strokeToOccupy(10.0, profile.gapHeight)
        } else 0.0
        T129StateRecord(
            state = profile.name,
            concentration = profile.concentration,
            gapHeight = profile.gapHeight,
            appliedBias = profile.appliedBias,
            freeDishingOverStroke = bank24.freePeakDishing(index) / freeStroke,
            equalSpringDishingOverStroke = equalPeaks[index] / freeStroke,
            reachableFloorOverStroke = floors.getValue(index),
            flatAtTenPercent = equalPeaks[index] / freeStroke < FLATNESS_TOLERANCE,
            beatsNoCouplingAtAll = equalPeaks[index] < bank24.freePeakDishing(index),
            occupiedByTenNanometreDevice = profile.gapHeight > 0.0 &&
                    gapOccupiable(10.0, profile.gapHeight, deadLoadStrokeAtTen),
            strokeDemandedOfTenNanometreDevice = demanded
        )
    }

    // ------------------------------------------------------------------ the cosine matrix
    val deviceStates = profiles.indices.filter { it != uniformState && !profiles[it].name.startsWith("INTERPOLATED") }
    val deviceOf = HashMap<String, MutableSet<String>>()
    T129_RANGES.forEach { range ->
        range.keys.forEach { key ->
            deviceOf.getOrPut(profileAt(key).name) { HashSet() } += range.device
        }
    }
    val cosines = deviceStates.flatMap { first ->
        deviceStates.filter { it > first }.map { second ->
            val a = profiles[first].name
            val b = profiles[second].name
            T129CosineRecord(
                first = a,
                second = b,
                cosine = c0063Surrogate.freeFieldCosine(first, second),
                sameDevice = (deviceOf[a] ?: emptySet<String>())
                    .intersect(deviceOf[b] ?: emptySet()).isNotEmpty(),
                equalSpringWorstOverStroke =
                    c0063Surrogate.worstDishing(equal, listOf(first, second)) / freeStroke
            )
        }
    }

    // ------------------------------------------------------------------ the ranges
    println("T-129 — the multi-state minimax over each device's own traversed range ...")
    val starts = listOf(equal) +
            listOf(2.0, 5.0).map {
                normalisedStiffnesses(
                    rimStiffenedWeights(
                        placement.stations(DUPLEXES), edgeX, lengthY, C0058_COLLAR_WIDTH, it
                    ),
                    MANDATE
                )
            } +
            t129RandomStarts(count, RANGE_STARTS - 3, 0.35).map {
                normalisedStiffnesses(it, MANDATE)
            }

    fun minimax(states: List<Int>, starts: List<List<Double>>) = minimaxStiffnessDistribution(
        surrogate = c0063Surrogate,
        states = states,
        totalStiffness = MANDATE,
        starts = starts,
        ceiling = ceiling
    )

    val ranges = T129_RANGES.map { range ->
        val states = rangeIndices(range)
        val equalWorst = c0063Surrogate.worstDishing(equal, states) / freeStroke
        val optimum = minimax(states, starts)
        val minimaxWorst = optimum.worstDishing / freeStroke
        val peakStiffness = optimum.stiffnesses.max()
        val solvedPeak = states.maxOf { state ->
            c0063Surrogate.supportForces(optimum.stiffnesses, state).maxOf { abs(it) }
        }
        val thermal = perPathThermalForces(optimum.stiffnesses).max()
        println(
            ("  %-52s equal %6.4f  minimax %6.4f  binding %s").format(
                range.device, equalWorst, minimaxWorst, optimum.bindingStates.size
            )
        )
        T129RangeRecord(
            device = range.device,
            rationale = range.rationale,
            restingHeight = range.restingHeight,
            states = states.map { profiles[it].name },
            floorOverStroke = states.maxOf { floors.getValue(it) },
            equalSpringWorstOverStroke = equalWorst,
            minimaxWorstOverStroke = minimaxWorst,
            bindingStates = optimum.bindingStates,
            flatWithEqualSprings = equalWorst < FLATNESS_TOLERANCE,
            flatWithADistribution = minimaxWorst < FLATNESS_TOLERANCE,
            minimaxPeakRatio = peakStiffness / (MANDATE / count),
            peakPathStiffness = peakStiffness,
            peakPathForceAtAcceptableStroke = peakStiffness * Gen1Tile.ACCEPTABLE_STROKE,
            peakSolvedPathForce = solvedPeak,
            peakThermalForce = thermal,
            withinUnzipAllowable =
                peakStiffness * Gen1Tile.ACCEPTABLE_STROKE <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            startsUsed = optimum.startsUsed,
            startsWithinOnePartInAMillion = optimum.startsWithinOnePartInAMillion
        )
    }

    // ------------------------------------------------------------------ the 31 subsets
    println("T-129 — all 31 non-empty subsets of C-0022's five headline states ...")
    val headlineIndices = headline.map { indexOfState.getValue(it.name) }
    val subsetStarts = starts.take(SUBSET_STARTS)
    val subsets = (1 until (1 shl headlineIndices.size)).map { mask ->
        val states = headlineIndices.filterIndexed { position, _ -> (mask shr position) and 1 == 1 }
        val equalWorst = c0063Surrogate.worstDishing(equal, states) / freeStroke
        val optimum = minimax(states, subsetStarts)
        val worst = optimum.worstDishing / freeStroke
        T129SubsetRecord(
            states = states.map { profiles[it].name },
            size = states.size,
            containsTwoNanometreState = twoNanometreState in states,
            containsTenNanometreState = states.any { profiles[it].gapHeight == 10.0 },
            equalSpringWorstOverStroke = equalWorst,
            minimaxWorstOverStroke = worst,
            flatWithEqualSprings = equalWorst < FLATNESS_TOLERANCE,
            flatWithADistribution = worst < FLATNESS_TOLERANCE
        )
    }

    // ------------------------------------------- the placement, swept under the RANGE objective
    println("T-129 — re-sweeping the centro-symmetric family under the RANGE objective ...")
    val designStates = rangeIndices(T129_RANGES[0])
    val symmetricPhases = centroSymmetricUpwardPhases(edgeX, DUPLEXES)
    val c0063RangeValue = c0063Surrogate.worstDishing(equal, designStates) / freeStroke

    class PhaseSweep(val phase: Int) {
        val sites = upwardRootLattice(phase, edgeX, DUPLEXES)
        val stations = sites.flatMapIndexed { row, xs ->
            xs.map { it to (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
        }
        val bank = if (phase == C0063_PHASE) bank24 else MultiStateRootBank(
            t129Lattice(sheet, CrossoverLayout.atBasePairPhase(phase, sheet, edgeX)),
            stations, loadStates
        )

        fun surrogate(candidate: UpwardArmPlacement): MultiStateSurrogate =
            bank.surrogateFor(
                candidate.stations(DUPLEXES).map { (x, y) ->
                    val index = bank.indexOf(x, y)
                    require(index >= 0) { "($x, $y) is not an upward site of phase $phase" }
                    index
                }
            )
    }

    // Two objectives, in ONE enumeration: the design device's range, and the 5 nm device's — the
    // one C-0022's 2 nm state belongs to, and the only range equal springs do not clear.
    class SweepObjective(val name: String, val states: List<Int>) {
        val reference: Double = c0063Surrogate.worstDishing(equal, states) / freeStroke
    }

    val sweepObjectives = listOf(
        SweepObjective(
            "the worst over the 2 mM / 10 nm / 0.192 V device's traversed range", designStates
        ),
        SweepObjective(
            "the worst over the 2 mM / 5 nm / 0.368 V device's traversed range",
            rangeIndices(T129_RANGES[2])
        )
    )

    val placements = symmetricPhases.flatMap { phase ->
        val sweep = PhaseSweep(phase)
        val values = sweepObjectives.map { ArrayList<Double>() }
        val best = arrayOfNulls<Pair<UpwardArmPlacement, Double>>(sweepObjectives.size)
        val better = IntArray(sweepObjectives.size)
        centroSymmetricPlacements(
            phase, edgeX, DUPLEXES, arm, count, minimumPerRow = 2, maximumPerRow = 3
        ).forEach { candidate ->
            val surrogate = sweep.surrogate(candidate)
            sweepObjectives.forEachIndexed { index, objective ->
                // The decision is rounded and the placement's own canonical key is the tie-break
                // (`CLAUDE.md`: an argmin is not reproducible unless the COMPARISON is too).
                val value =
                    roundForResult(surrogate.worstDishing(equal, objective.states) / freeStroke)
                values[index] += value
                if (value < roundForResult(objective.reference)) better[index]++
                val current = best[index]
                if (current == null || value < current.second ||
                    (value == current.second && candidate.key < current.first.key)
                ) best[index] = candidate to value
            }
        }
        sweepObjectives.mapIndexed { index, objective ->
            val winner = best[index] ?: error("the symmetric family at phase $phase is empty")
            val sorted = values[index].sorted()
            val atDesign =
                sweep.surrogate(winner.first).worstDishing(equal, listOf(designState)) / freeStroke
            println(
                "  phase %2d  %-64s enumerated %6d  best %6.4f  median %6.4f".format(
                    phase, objective.name, sorted.size, sorted.first(), sorted[sorted.size / 2]
                )
            )
            T129PlacementRecord(
                family = "centro-symmetric, 2 or 3 arms per row, exhaustive",
                phaseBasePairs = phase,
                objective = objective.name,
                enumerated = sorted.size,
                bestOverStroke = winner.second,
                medianOverStroke = sorted[sorted.size / 2],
                worstOverStroke = sorted.last(),
                bestKey = winner.first.key,
                bestFlatAtTenPercent = winner.second < FLATNESS_TOLERANCE,
                bestAtDesignStateOverStroke = atDesign,
                c0063OverStroke = objective.reference,
                placementsBetterThanC0063 = better[index],
                beatsC0063 = winner.second < roundForResult(objective.reference)
            )
        }
    }

    // ------------------------------------------------------------------ the distributions
    println("T-129 — C-0058's rim family and the minimax, over the design device's range ...")
    val stations = placement.stations(DUPLEXES)
    val distributions = listOf(1.0, 2.0, 3.0, 5.0).map { ratio ->
        val stiffnesses = normalisedStiffnesses(
            rimStiffenedWeights(stations, edgeX, lengthY, C0058_COLLAR_WIDTH, ratio), MANDATE
        )
        val rangeWorst = c0063Surrogate.worstDishing(stiffnesses, designStates) / freeStroke
        T129DistributionRecord(
            placement = "C-0063's swept placement, 34 upward roots at phase $C0063_PHASE",
            rule = if (ratio == 1.0) "uniform — 34 EQUAL springs"
            else "C-0058's rim rule over a %.2f nm collar".format(C0058_COLLAR_WIDTH),
            ratio = ratio,
            designStateOverStroke =
                c0063Surrogate.worstDishing(stiffnesses, listOf(designState)) / freeStroke,
            rangeWorstOverStroke = rangeWorst,
            flatOverRange = rangeWorst < FLATNESS_TOLERANCE,
            peakPathForceAtAcceptableStroke = stiffnesses.max() * Gen1Tile.ACCEPTABLE_STROKE,
            peakThermalForce = perPathThermalForces(stiffnesses).max()
        )
    } + T129DistributionRecord(
        placement = "C-0063's swept placement, 34 upward roots at phase $C0063_PHASE",
        rule = "the 34-parameter minimax over the design device's own range",
        ratio = ranges[0].minimaxPeakRatio,
        designStateOverStroke = ranges[0].minimaxWorstOverStroke,
        rangeWorstOverStroke = ranges[0].minimaxWorstOverStroke,
        flatOverRange = ranges[0].flatWithADistribution,
        peakPathForceAtAcceptableStroke = ranges[0].peakPathForceAtAcceptableStroke,
        peakThermalForce = ranges[0].peakThermalForce
    )

    // ------------------------------------------------------------------ convergence
    println("T-129 — convergence: subdivisions, sampling grid and the range's discretisation ...")
    val c0063Stations = placement.stations(DUPLEXES)
    val rangeStates = designStates.map { loadStates[it] }
    fun rangeReading(subdivisions: Int, samples: Int, states: List<LoadState>): Double {
        val bank = MultiStateRootBank(
            t129Lattice(
                sheet, CrossoverLayout.atBasePairPhase(C0063_PHASE, sheet, edgeX), subdivisions
            ),
            c0063Stations, states, samples
        )
        return bank.surrogateFor(c0063Stations.indices.toList())
            .worstDishing(equal, states.indices.toList()) / freeStroke
    }

    val subdivisionResults = listOf(1, 2, 4).map { rangeReading(it, 81, rangeStates) }
    val samplingResults = listOf(41, 81, 161).map { rangeReading(2, it, rangeStates) }
    // And the same sampling sweep on the TIGHTEST range that equal springs do clear — the one
    // whose margin against T-5b's 0.10 a 2.7 % grid sensitivity could actually consume.
    val tightest = ranges.filter { it.flatWithEqualSprings }.maxBy { it.equalSpringWorstOverStroke }
    val tightestStates = T129_RANGES.first { it.device == tightest.device }
        .let { range -> rangeIndices(range).map { loadStates[it] } }
    val tightestSampling = listOf(41, 81, 161).map { rangeReading(2, it, tightestStates) }
    val interpolatedStates = designStates + interpolated.map { indexOfState.getValue(it.name) }
    val discretisation = listOf(
        c0063Surrogate.worstDishing(equal, designStates) / freeStroke,
        c0063Surrogate.worstDishing(equal, interpolatedStates) / freeStroke
    )
    val convergence = listOf(
        T129ConvergenceRecord(
            quantity = "equal-spring worst dishing/stroke over the design device's range",
            parameter = "NESTED subdivisions 1 c 2 c 4",
            values = listOf(1.0, 2.0, 4.0),
            results = subdivisionResults,
            departure = abs(subdivisionResults[2] - subdivisionResults[1]) / subdivisionResults[2],
            note = "nested only, per CLAUDE.md — a subdivision of 3 moves a station off a node"
        ),
        T129ConvergenceRecord(
            quantity = "equal-spring worst dishing/stroke over the design device's range",
            parameter = "dishing sample grid 41 / 81 / 161",
            values = listOf(41.0, 81.0, 161.0),
            results = samplingResults,
            departure = abs(samplingResults[2] - samplingResults[1]) / samplingResults[2],
            note = "the same 2.7 % scale C-0063 reports at one state; both ends inside 0.10"
        ),
        T129ConvergenceRecord(
            quantity = "equal-spring worst dishing/stroke over the TIGHTEST flat range (" +
                    tightest.device + ")",
            parameter = "dishing sample grid 41 / 81 / 161",
            values = listOf(41.0, 81.0, 161.0),
            results = tightestSampling,
            departure = abs(tightestSampling[2] - tightestSampling[1]) / tightestSampling[2],
            note = "the margin this range has against T-5b's 0.10 is what a grid sensitivity " +
                    "could consume, so it is measured there and not only at the design device"
        ),
        T129ConvergenceRecord(
            quantity = "equal-spring worst dishing/stroke over the design device's range",
            parameter = "the range's own discretisation: 2 solved ends against 2 ends + 2 " +
                    "interpolated intermediate gaps",
            values = listOf(2.0, 4.0),
            results = discretisation,
            departure = abs(discretisation[1] - discretisation[0]) / discretisation[0],
            note = "the collar family is smooth in the gap, so two solved endpoints ARE the range"
        )
    )

    // ------------------------------------------------------------------ upstream reproductions
    println("T-129 — reproducing what upstream published on these stations and on the grid ...")
    val gridStations = attachmentGrid(3, DUPLEXES, edgeX, lengthY)
    val nominalHost = t129Lattice(sheet, CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0))
    val gridBank = MultiStateRootBank(nominalHost, gridStations, loadStates)
    val gridSurrogate = gridBank.surrogateFor(gridStations.indices.toList())
    val gridEqual = List(gridStations.size) { MANDATE / gridStations.size }
    val gridRim5 = normalisedStiffnesses(
        rimStiffenedWeights(gridStations, edgeX, lengthY, C0058_COLLAR_WIDTH, 5.0), MANDATE
    )
    val gridUniformDesign =
        gridSurrogate.worstDishing(gridEqual, listOf(designState)) / freeStroke
    val gridRim5Range = gridSurrogate.worstDishing(gridRim5, designStates) / freeStroke
    // C-0064's "uniform" column at the 5 nm device — the comparison that reverses here.
    val gridUniformFiveRange =
        gridSurrogate.worstDishing(gridEqual, rangeIndices(T129_RANGES[2])) / freeStroke
    val gridStarts = listOf(gridEqual, gridRim5) +
            t129RandomStarts(gridStations.size, RANGE_STARTS - 2, 0.35)
                .map { normalisedStiffnesses(it, MANDATE) }
    val gridRangeMinimax = minimaxStiffnessDistribution(
        surrogate = gridSurrogate,
        states = designStates,
        totalStiffness = MANDATE,
        starts = gridStarts,
        ceiling = ceiling
    ).worstDishing / freeStroke

    val c0063Single = c0063Surrogate.worstDishing(equal, listOf(designState)) / freeStroke
    fun reproduction(source: String, quantity: String, published: Double, reproduced: Double, strict: Boolean) =
        T129ReproductionRecord(
            source, quantity, published, reproduced,
            if (published == 0.0) abs(reproduced) else abs(reproduced - published) / abs(published),
            strict
        )

    val reproductions = listOf(
        reproduction(
            "C-0063", "dishing/stroke, its 34 roots, equal springs, at its own state",
            0.0706145537, c0063Single, false
        ),
        reproduction(
            "C-0022", "dishing/stroke, free uncoupled tile at the design state",
            0.3079, bank24.freePeakDishing(designState) / freeStroke, false
        ),
        reproduction("C-0026", "free-tile stroke [nm]", 4.90731102, freeStroke, false),
        reproduction(
            "C-0058", "dishing/stroke, 3 x 15, uniform, at the design state",
            0.2182, gridUniformDesign, false
        ),
        reproduction(
            "C-0064", "dishing/stroke, 3 x 15, rim x 5, over the 2 mM device's range",
            0.0753, gridRim5Range, false
        ),
        reproduction(
            "C-0064", "the 45-path minimax over the 2 mM device's range",
            0.0373, gridRangeMinimax, false
        ),
        reproduction(
            "C-0064", "dishing/stroke, 3 x 15, UNIFORM, over the 5 nm device's range",
            0.0796, gridUniformFiveRange, false
        ),
        reproduction(
            "C-0055", "upward root pitch [nm]", 10.88,
            UPWARD_ROOT_PITCH_BASE_PAIRS * Gen1Tile.RISE_PER_BASE_PAIR, true
        ),
        reproduction("C-0055", "arms placed", 34.0, placement.count.toDouble(), true),
        reproduction("C-0055", "arm length [nm]", 8.164, arm, false),
        reproduction("C-0049", "the per-path stiffness ceiling [pN/nm]", 3.33333333, ceiling, false),
        reproduction(
            "C-0063", "the admissible per-path ratio at 34 paths", 3.4, admissible, false
        ),
        reproduction(
            "C-0050", "dead-load stroke at the 10 nm design point, best of six models [nm]",
            6.01348358, deadLoadStrokeAtTenDesign, false
        ),
        reproduction(
            "C-0050", "dead-load stroke, best over the whole sweep [nm]",
            7.42353439, deadLoadStrokeAtTen, false
        )
    )

    // ------------------------------------------------------------------ the predicates
    val designRangeRecord = ranges[0]
    val allFlatEqual = ranges.all { it.flatWithEqualSprings }
    val flatEqualDevices = ranges.filter { it.flatWithEqualSprings }
    val notFlatEqual = ranges.filterNot { it.flatWithEqualSprings }
    val allFlatMinimax = ranges.all { it.flatWithADistribution }
    val fiveStateEqual = c0063Surrogate.worstDishing(equal, headlineIndices) / freeStroke
    val fiveStateMinimax = subsets.first { it.size == 5 }.minimaxWorstOverStroke
    val predicates = listOf(
        T129PredicateRecord(
            "P1 — equal springs over a traversed range",
            "the worst peak dishing of 34 EQUAL springs on C-0063's placement over each device's " +
                    "own range, against T-5b's 0.10 of the free-tile stroke",
            ("%.4f / %.4f / %.4f / %.4f over the four devices — %d of 4 inside T-5b's 0.10 with " +
                    "EQUAL springs%s").format(
                ranges[0].equalSpringWorstOverStroke, ranges[1].equalSpringWorstOverStroke,
                ranges[2].equalSpringWorstOverStroke, ranges[3].equalSpringWorstOverStroke,
                flatEqualDevices.size,
                if (allFlatEqual) "" else
                    ", the exception being " + notFlatEqual.joinToString("; ") { it.device }
            )
        ),
        T129PredicateRecord(
            "P2 — does the range need a distribution?",
            "the 34-parameter multi-state minimax against the equal-spring reading over the same " +
                    "range, at C-0017's unchanged total",
            ("%.4f / %.4f / %.4f / %.4f over the four devices, %s inside T-5b's 0.10; at the " +
                    "design device the distribution is worth %.1f %% over equal springs and asks " +
                    "a peak ratio of only %.2f, and the one range equal springs miss is " +
                    "recovered at a peak ratio of %.2f").format(
                ranges[0].minimaxWorstOverStroke, ranges[1].minimaxWorstOverStroke,
                ranges[2].minimaxWorstOverStroke, ranges[3].minimaxWorstOverStroke,
                if (allFlatMinimax) "ALL FOUR" else "NOT all",
                100.0 * (1.0 - designRangeRecord.minimaxWorstOverStroke /
                        designRangeRecord.equalSpringWorstOverStroke),
                designRangeRecord.minimaxPeakRatio,
                ranges[2].minimaxPeakRatio
            )
        ),
        T129PredicateRecord(
            "P3 — which states one device co-occupies",
            ("the 2 nm state demands %.1f nm of stroke of a 10 nm device against C-0050's %.3f " +
                    "nm dead-load ceiling, and %.1f nm of the 5 nm device that owns it").format(
                strokeToOccupy(10.0, 2.0), deadLoadStrokeAtTen, strokeToOccupy(5.0, 2.0)
            ),
            ("the 2 nm state is NOT occupied by any 10 nm device and IS the compressed end of the " +
                    "5 nm device, whose own range reads %.4f — the exclusion is physical, and it " +
                    "excludes nothing, because that device is evaluated too")
                .format(ranges[2].equalSpringWorstOverStroke)
        ),
        T129PredicateRecord(
            "P4 — is the single-state placement the right placement for a range?",
            "the centro-symmetric family re-enumerated exhaustively under the range objective",
            placements.joinToString("; ") {
                ("phase %d: best %.4f against C-0063's %.4f, %d of %d placements better")
                    .format(
                        it.phaseBasePairs, it.bestOverStroke, it.c0063OverStroke,
                        it.placementsBetterThanC0063, it.enumerated
                    )
            }
        ),
        T129PredicateRecord(
            "the portfolio duty, for contrast",
            "the same placement asked to be flat at all five of C-0022's headline states at once",
            ("%.4f with equal springs and %.4f with the best distribution found — %s T-5b's 0.10")
                .format(
                    fiveStateEqual, fiveStateMinimax,
                    if (fiveStateMinimax < FLATNESS_TOLERANCE) "inside" else "OUTSIDE"
                )
        )
    )

    // ------------------------------------------------------------------ the findings
    val twoNanometreSubsets = subsets.filter {
        it.containsTwoNanometreState && it.containsTenNanometreState
    }
    val otherSubsets = subsets.filterNot {
        it.containsTwoNanometreState && it.containsTenNanometreState
    }
    val findings = listOf(
        ("C-0063's placement is flat with EQUAL springs over the traversed range of every 10 nm " +
                "device C-0022 solved, and over no other: %.4f (2 mM, C-0018's placed device), " +
                "%.4f (0.5 mM, C-0032's recommendation) and %.4f (10 mM), against %.4f at the " +
                "single state C-0063 reported. So the single-state verdict TRAVELS for the " +
                "device it was read on — but the margin against T-5b's 0.10 falls from 1.42x to " +
                "%.2fx, and it is the compressed end of the stroke that spends it.").format(
            ranges[0].equalSpringWorstOverStroke, ranges[1].equalSpringWorstOverStroke,
            ranges[3].equalSpringWorstOverStroke, c0063Single,
            FLATNESS_TOLERANCE / ranges.filter { it.flatWithEqualSprings }
                .maxOf { it.equalSpringWorstOverStroke }
        ),
        ("The exception is the FIVE nanometre device, and it is the one whose range contains " +
                "C-0022's 2 nm state: equal springs dish %.4f there, %.1fx outside the " +
                "convention, where the same equal springs on C-0058's 3 x 15 grid dish %.4f " +
                "(re-derived here) and are inside it. On this station set the 5 nm device NEEDS " +
                "a distribution — " +
                "the 34-parameter minimax recovers it to %.4f at a peak ratio of %.2f — so " +
                "C-0063's equal-spring advantage is a property of the 10 nm devices, not of the " +
                "placement.").format(
            ranges[2].equalSpringWorstOverStroke,
            ranges[2].equalSpringWorstOverStroke / FLATNESS_TOLERANCE,
            gridUniformFiveRange, ranges[2].minimaxWorstOverStroke, ranges[2].minimaxPeakRatio
        ),
        ("And at the 5 nm layer's states the equal-spring coupling is a net dishing SOURCE, " +
                "which is C-0047's own bar and C-0061's failure mode at a different placement: " +
                "the free tile dishes %.4f at the 5 nm rest state and %.4f at the 2 nm held " +
                "state, against %.4f and %.4f coupled — %.2fx and %.2fx WORSE than no coupling " +
                "at all. The 34 roots are placed where a 10 nm layer's edge collar puts the " +
                "load, and a 5 nm layer's is a different field: its free-tile dishing is %.1fx " +
                "smaller at the rest state, so there is far less for a coupling to correct and " +
                "the coupling's own sag dominates.").format(
            stateRecords[3].freeDishingOverStroke, stateRecords[4].freeDishingOverStroke,
            stateRecords[3].equalSpringDishingOverStroke,
            stateRecords[4].equalSpringDishingOverStroke,
            stateRecords[3].equalSpringDishingOverStroke / stateRecords[3].freeDishingOverStroke,
            stateRecords[4].equalSpringDishingOverStroke / stateRecords[4].freeDishingOverStroke,
            stateRecords[0].freeDishingOverStroke / stateRecords[3].freeDishingOverStroke
        ),
        ("Where equal springs do clear, the distribution is worth little and C-0058's rim rule " +
                "still runs the wrong way: the minimax over the design device's range reaches " +
                "%.4f against %.4f (%.1f %%) at a peak ratio of %.2f, while the rim rule gives " +
                "%.4f at x2 and %.4f at its own x5 — the sign reversal C-0063 found at one " +
                "state, reproduced over a range.").format(
            designRangeRecord.minimaxWorstOverStroke,
            designRangeRecord.equalSpringWorstOverStroke,
            100.0 * (1.0 - designRangeRecord.minimaxWorstOverStroke /
                    designRangeRecord.equalSpringWorstOverStroke),
            designRangeRecord.minimaxPeakRatio,
            distributions[1].rangeWorstOverStroke, distributions[3].rangeWorstOverStroke
        ),
        ("The exclusion of C-0022's 2 nm state from a 10 nm device's range is PHYSICAL and it " +
                "changes no verdict here, because the device that does occupy that state is " +
                "evaluated on its own: reaching a 2 nm gap from a 10 nm layer demands %.1f nm of " +
                "stroke against C-0050's %.4f nm, the largest dead-load stroke it finds anywhere " +
                "at a 10 nm layer under S3's own 100 pN (%.4f nm at S3's own grafting density), " +
                "and C-0017's theorem says a coupling can only reduce it. The honest caveat: the " +
                "same test does not clear the 5 nm device's own 3 nm either (%.4f nm), so " +
                "C-0022's 2 nm state is held by its solved 0.368 V bias rather than by a 100 pN " +
                "dead load — a fact about C-0022's state set, recorded rather than smoothed.")
            .format(
                strokeToOccupy(10.0, 2.0), deadLoadStrokeAtTen, deadLoadStrokeAtTenDesign,
                deadLoadStrokeAtFive
            ),
        ("C-0064's sign dichotomy transfers in DIRECTION and not in exactness: of the 31 " +
                "non-empty subsets of C-0022's five headline states, the %d that put the 2 nm " +
                "state with a 10 nm state run %.4f-%.4f and %d of them are inside 0.10, while " +
                "the other %d run %.4f-%.4f and %d of %d are — against 0 of 14 and 17 of 17 on " +
                "C-0058's 3 x 15 grid. The five-state portfolio duty reaches %.4f here against " +
                "C-0064's 0.1254 on that grid, so the placement does not repeal the " +
                "incompatibility either; on this station set the ANTAGONIST IS THE 5 nm LAYER " +
                "rather than the 2 nm gap alone — the two states of the 5 nm device are " +
                "themselves anti-parallel, cosine %.4f, where the 10 nm devices' own pairs run " +
                "+0.9969 to +0.9998.").format(
            twoNanometreSubsets.size,
            twoNanometreSubsets.minOf { it.minimaxWorstOverStroke },
            twoNanometreSubsets.maxOf { it.minimaxWorstOverStroke },
            twoNanometreSubsets.count { it.flatWithADistribution },
            otherSubsets.size,
            otherSubsets.minOf { it.minimaxWorstOverStroke },
            otherSubsets.maxOf { it.minimaxWorstOverStroke },
            otherSubsets.count { it.flatWithADistribution }, otherSubsets.size,
            fiveStateMinimax,
            c0063Surrogate.freeFieldCosine(
                indexOfState.getValue(headline[3].name), twoNanometreState
            )
        ),
        ("The placement was re-swept under the RANGE objective itself, exhaustively over the " +
                "centro-symmetric family at both phases the congruence admits and under two " +
                "objectives: %s.").format(
            placements.joinToString("; ") {
                ("phase %d, %s, %d enumerated, best %.4f (flat: %s) against C-0063's %.4f, %d " +
                        "better").format(
                    it.phaseBasePairs, it.objective, it.enumerated, it.bestOverStroke,
                    it.bestFlatAtTenPercent, it.c0063OverStroke, it.placementsBetterThanC0063
                )
            }
        )
    )

    val result = T129Result(
        task = "T-129 — is C-0063's flat 34-root placement flat over a RANGE?",
        leaf = "A8.2",
        conditions = ("T = 300 K, k_BT = 4.141947 pN nm; aqueous MgCl2 at 0.5 / 2 / 10 mM; " +
                "40.0 x %.2f nm single-layer square-lattice Rothemund sheet, %d duplexes at " +
                "2.69 nm, phase %d carrying its OWN eight crossover columns; C-0055's %d upward " +
                "roots at C-0063's swept placement; C-0017's %.4f pN/nm mandate as a SUM; " +
                "C-0022's SOLVED edge profiles keyed on (concentration, gap, bias); C-0001's " +
                "foundation secant; free-tile stroke %.5f nm").format(
            lengthY, DUPLEXES, C0063_PHASE, count, MANDATE, freeStroke
        ),
        decision = ("with EQUAL springs C-0063's placement dishes %.4f / %.4f / %.4f of the " +
                "stroke over the three 10 nm devices' own traversed ranges — all inside T-5b's " +
                "0.10 — and %.4f over the 5 nm device's, which is outside it and is recovered by " +
                "a distribution at %.4f").format(
            ranges[0].equalSpringWorstOverStroke, ranges[1].equalSpringWorstOverStroke,
            ranges[3].equalSpringWorstOverStroke, ranges[2].equalSpringWorstOverStroke,
            ranges[2].minimaxWorstOverStroke
        ),
        bounds = bounds,
        states = stateRecords,
        cosines = cosines,
        ranges = ranges,
        subsets = subsets,
        placements = placements,
        distributions = distributions,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings,
        parameters = mapOf(
            "armCount" to count.toDouble(),
            "armLength" to arm,
            "phase" to C0063_PHASE.toDouble(),
            "edgeX" to edgeX,
            "lengthY" to lengthY,
            "duplexes" to DUPLEXES.toDouble(),
            "mandate" to MANDATE,
            "freeStroke" to freeStroke,
            "flatnessTolerance" to FLATNESS_TOLERANCE,
            "collarWidth" to C0058_COLLAR_WIDTH,
            "unzipAllowable" to Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            "perPathStiffnessCeiling" to ceiling,
            "admissibleRatio" to admissible,
            "deadLoadStrokeAtTenNanometres" to deadLoadStrokeAtTen,
            "deadLoadStrokeAtTenNanometresAtS3Sigma" to deadLoadStrokeAtTenDesign,
            "deadLoadStrokeAtFiveNanometres" to deadLoadStrokeAtFive,
            "s3GraftingDensityAtTen" to S3_SIGMA_AT_TEN,
            "s3GraftingDensityAtFive" to S3_SIGMA_AT_FIVE,
            "singleStateDishingOverStroke" to c0063Single,
            "designRangeEqualSpringOverStroke" to designRangeRecord.equalSpringWorstOverStroke,
            "designRangeMinimaxOverStroke" to designRangeRecord.minimaxWorstOverStroke,
            "designRangeFloorOverStroke" to designRangeRecord.floorOverStroke,
            "fiveStateEqualSpringOverStroke" to fiveStateEqual,
            "fiveStateMinimaxOverStroke" to fiveStateMinimax,
            "placementsEnumerated" to placements.sumOf { it.enumerated }.toDouble(),
            "rangeStarts" to RANGE_STARTS.toDouble(),
            "subsetStarts" to SUBSET_STARTS.toDouble()
        )
    )

    val output = File("gpd/results/T-129-range-robust-placement.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult() as JsonObject)
        )
    )

    println()
    println("cheap bounds")
    result.bounds.forEach {
        println("  %-64s %10.4f %-22s fired %s".format(it.name, it.value, it.unit, it.falsifierFired))
    }
    println()
    println("the ranges — one device each")
    result.ranges.forEach {
        println(
            "  %-52s equal %6.4f (%s)  minimax %6.4f (%s)  ratio %5.2f".format(
                it.device, it.equalSpringWorstOverStroke, it.flatWithEqualSprings,
                it.minimaxWorstOverStroke, it.flatWithADistribution, it.minimaxPeakRatio
            )
        )
    }
    println()
    println("distributions over the design device's range")
    result.distributions.forEach {
        println(
            "  %-58s ratio %5.2f  range worst %6.4f  flat %s".format(
                it.rule, it.ratio, it.rangeWorstOverStroke, it.flatOverRange
            )
        )
    }
    println()
    println("upstream reproductions")
    result.reproductions.forEach {
        println(
            "  %-8s %-58s %12.6g vs %12.6g  %8.2e %s".format(
                it.source, it.quantity, it.published, it.reproduced, it.departure,
                if (it.strict) "" else "(non-strict)"
            )
        )
    }
    println()
    println("predicates")
    result.predicates.forEach { println("  ${it.name}: ${it.verdict}") }
    println()
    result.findings.forEach { println("  * $it"); println() }
    println("written to ${output.path} in ${(System.currentTimeMillis() - started) / 1000} s")
}
