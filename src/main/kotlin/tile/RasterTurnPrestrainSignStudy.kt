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
import com.xemantic.nano.plentyofroom.coupling.DishingSolution
import com.xemantic.nano.plentyofroom.coupling.DropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

// ---------------------------------------------------------------------------------------------
// T-284 -- what sets the SIGN of a raster turn's 8.57142857 degree departure.
//
// C-0180 section 4 grades the recommended block's two recovered coupled cells at +-8.57142857
// degrees on EVERY turn and finds that exactly one of the two is flat at each sign, a DIFFERENT
// one -- so the whole coupled recovery rests on a binary C-0175 calls "fixed by no source in this
// repository".
//
// It is fixed, and by the rule that supplies the magnitude. caDNAno's "five base pairs, or half a
// turn" is an integer approximation to 5.25, so a crossover at +5 bp falls a quarter of a base
// pair SHORT of the exact half turn and one at -5 bp sits a quarter PAST it: equal magnitude,
// opposite sign. Which side a turn takes is C-0148's closure condition, and on a CLOSING raster
// two distinct reduced residues pin b0 uniquely. The 2^59 assignments collapse to two.
//
// What the lattice cannot fix is the one GLOBAL phase, because the departure rotates BOTH
// backbones the same way (ForcedCrossoverPrice's own header) and the model's tie prestrain is a
// RELATIVE roll. So both phases are graded, and the verdict is quoted at the worse.
// ---------------------------------------------------------------------------------------------

private const val T284_SAMPLES: Int = 81
private const val T284_TOLERANCE: Double = 0.10
private const val T284_RIM_STANDOFF: Double = 1.0
private const val T284_RIM_BAND: Double = 6.7
private const val T284_SEED: Long = 197_197L
private const val T284_BLOCK_EXTENT_BP: Int = 116
private const val T284_LADDER_PHASE: Int = 16
private const val T284_LADDER_OFFSET: Int = 14
private const val T284_RECOMMENDED_ONE: Int = 102
private const val T284_RECOMMENDED_TWO: Int = 109
private const val T284_UNDRAWABLE_ONE: Int = 112
private const val T284_UNDRAWABLE_TWO: Int = 108

/**
 * A raster whose every helix carries `C-0136`'s residue **0** and therefore carries the sign
 * **through**: all 59 crossovers take one reduced residue, two `b₀` candidates admit it, and the
 * assignment is genuinely uniform. `112 ≡ 7` and `119 ≡ 14 (mod 21)`, which are `7Δ` at the two
 * effective senses the `10 × 6` block puts on its helices. It is **constructed**, not recommended.
 */
private const val T284_UNIFORM_ONE: Int = 112
private const val T284_UNIFORM_TWO: Int = 119

/** The relative tolerance every same-quantity identity is asserted at, emitted as a boolean. */
private const val T284_IDENTITY: Double = 1e-9

/** The study runs at 4 000 realisations; `T284_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t284Realisations: Int =
    if (System.getenv("T284_SMOKE") == "1") 150 else 4000

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T284CheapBoundRow(
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private class T284DerivationRow(
    val raster: String,
    val crossSection: String,
    val rasterCrossovers: Int,
    val distinctReducedResidues: List<Int>,
    val classZeroResidueCandidates: List<Int>,
    val closes: Boolean,
    val assignmentIsDetermined: Boolean,
    val distinctPerHelixLengthResidues: List<Int>,
    val alternating: Boolean?,
    val turnsDisplacedFiveAbove: Int?,
    val turnsDisplacedFiveBelow: Int?,
    val highRimDisplacementBasePairs: Int?,
    val note: String
)

@Serializable
private class T284ConventionRow(
    val firstAxialSign: Int,
    val mirrored: Boolean,
    val axialReversed: Boolean,
    val classZeroResidue: Int,
    val highRimDisplacementBasePairs: Int,
    val highRimDepartureDegrees: Double,
    val alternating: Boolean,
    val partitionByRimIsUnchanged: Boolean
)

@Serializable
private class T284TurnRow(
    val index: Int,
    val lowerBeam: Int,
    val upperBeam: Int,
    val inPlane: Boolean,
    val atHighEnd: Boolean,
    val reducedResidue: Int,
    val displacementBasePairs: Int,
    val departureDegrees: Double
)

@Serializable
private class T284FreeTileRow(
    val hingeStiffnessEnhancement: Double,
    val compositeFraction: Double?,
    val signAssignment: String,
    val peakDishingOverStroke: Double,
    val insideTolerance: Boolean
)

@Serializable
private class T284Cell(
    val phase: Int,
    val signAssignment: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val nominalOverStroke: Double,
    val p90OverStroke: Double,
    // `T-337`. `C-0223`: the verdict below IS `exceedance <= tolerance`, so the proportion it
    // is a function of is emitted beside it rather than discarded at the grading site.
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val zeroPrestrainP90OverStroke: Double,
    val movementFromZeroPrestrain: Double
)

@Serializable
private class T284SignContingentRow(
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val p90AtPhasePlus: Double,
    val p90AtPhaseMinus: Double,
    val flatAtPhasePlus: Boolean,
    val flatAtPhaseMinus: Boolean,
    val verdictDependsOnThePhase: Boolean,
    val worstOfTheTwo: Double,
    val flatAtTheWorsePhase: Boolean
)

@Serializable
private class T284ConvergenceRow(
    val cell: String,
    val quantity: String,
    val axis: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double,
    val verdictSurvives: Boolean
)

@Serializable
private class T284ReproductionRow(
    val what: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double,
    val closes: Boolean
)

@Serializable
private class T284FalsifierRow(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T284Result(
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
    val cheapBound: List<T284CheapBoundRow>,
    val derivation: List<T284DerivationRow>,
    val conventionSweep: List<T284ConventionRow>,
    val turns: List<T284TurnRow>,
    val freeTile: List<T284FreeTileRow>,
    val cells: List<T284Cell>,
    val signContingency: List<T284SignContingentRow>,
    val verdict: Map<String, String>,
    val convergence: List<T284ConvergenceRow>,
    val reproductions: List<T284ReproductionRow>,
    val falsifiers: List<T284FalsifierRow>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T284Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T284_RIM_STANDOFF))
        )
}

private fun t284Profile(file: File): T284Profile {
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
    return T284Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`'s and `C-0180`'s geometry, unchanged, so the cells pair exactly. */
private class T284Shared(val profile: T284Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T284_BLOCK_EXTENT_BP
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)
    val d: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch: Double = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * rowPitch
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val closedFormStroke: Double = interiorPressure / Gen1Tile.FOUNDATION_SECANT
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val signs: HoneycombRasterTurnSigns = HoneycombRasterTurnSigns(
        block, T284_RECOMMENDED_ONE, T284_RECOMMENDED_TWO
    )

    fun enhancementAt(fraction: Double): Double = multiLayerRigidities(
        layers = helicesPerRow,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = columnPitch
    ).realisedEnhancement
}

private fun HoneycombDeflection.asDishing(): DishingSolution =
    object : DishingSolution {
        override fun deflectionAt(x: Double, y: Double) = deflection(x, y)
        override fun dishingAt(x: Double, y: Double) = dishing(x, y)
    }

/**
 * One composite fraction's lattices: the zero-prestrain tied block, which carries the influence
 * bank, and the two phases of the derived assignment, which carry only the free field.
 *
 * The prestrain is a **load**, so all three share one stiffness matrix — `C-0104`'s rule turned
 * into the study's cost model. The bank is taken on the zero-prestrain lattice, which **is**
 * `withoutPrestrain` for all three, and that reuse is asserted rather than assumed.
 */
private class T284Column(
    val shared: T284Shared,
    val fraction: Double?,
    val enhancement: Double,
    val subdivisions: Int = 1
) {
    private fun lattice(phase: Int?) = honeycombTiedLattice(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        enhancement = enhancement,
        tied = true,
        subdivisions = subdivisions
    ).let { bare ->
        if (phase == null) bare else HoneycombGrillage(
            block = shared.block,
            rowBasePairs = shared.rowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = enhancement,
            subdivisions = subdivisions,
            scaffoldTurnTies = shared.signs.ties(bare.nodesPerBeam, phase)
        )
    }

    val structure: HoneycombGrillage = lattice(null)
    val plus: HoneycombGrillage = lattice(1)
    val minus: HoneycombGrillage = lattice(-1)

    val freeStroke: Double =
        structure.solve(uniformPressure(shared.interiorPressure)).meanDeflection

    fun latticeAt(phase: Int): HoneycombGrillage =
        when (phase) {
            0 -> structure
            1 -> plus
            -1 -> minus
            else -> error("phase must be 0, +1 or -1, was: " + phase)
        }

    fun freeField(phase: Int, samples: Int = T284_SAMPLES): Double =
        latticeAt(phase).solve(shared.pressureField).peakDishing(samples) / freeStroke

    private val freeFields = HashMap<Int, DishingSolution>()

    private fun freeSolution(phase: Int): DishingSolution = freeFields.getOrPut(phase) {
        latticeAt(phase).solve(shared.pressureField).asDishing()
    }

    private val banks = HashMap<String, List<DishingSolution>>()

    fun bank(key: String, grid: List<Pair<Double, Double>>): List<DishingSolution> =
        banks.getOrPut(key) {
            grid.map { (s, y) ->
                structure.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0))).asDishing()
            }
        }

    fun surrogate(
        key: String,
        grid: List<Pair<Double, Double>>,
        phase: Int,
        samples: Int = T284_SAMPLES
    ): InfluenceSurrogate = influenceSurrogate(
        grid, structure.lengthS / 2.0, structure.lengthY / 2.0, samples,
        freeSolution(phase), bank(key, grid)
    )
}

private fun t284Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T284_RIM_BAND || abs(y) > edgeY / 2.0 - T284_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, unchanged, so the pairing against `C-0180` is exact. */
private fun t284Placements(
    shared: T284Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T284_RECOMMENDED_ONE, T284_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T284_LADDER_PHASE, T284_LADDER_OFFSET
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

private fun t284PublishedPrestrained(
    file: File,
    fraction: Double,
    placement: String,
    columns: Int,
    distribution: String,
    departureSign: Int
): Double = Json.parseToJsonElement(file.readText())
    .jsonObject.getValue("prestrained").jsonArray.map { it.jsonObject }
    .first {
        it.getValue("compositeFraction").jsonPrimitive.content.toDouble() == fraction &&
                it.getValue("placement").jsonPrimitive.content == placement &&
                it.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                it.getValue("distribution").jsonPrimitive.content == distribution &&
                (it.getValue("departureDegrees").jsonPrimitive.content.toDouble() > 0.0) ==
                (departureSign > 0)
    }.getValue("p90OverStroke").jsonPrimitive.content.toDouble()

private fun t284PublishedTied(
    file: File,
    fraction: Double,
    placement: String,
    columns: Int,
    distribution: String
): Double = Json.parseToJsonElement(file.readText())
    .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
    .first {
        it.getValue("tieState").jsonPrimitive.content == "tied" &&
                it.getValue("compositeFraction").jsonPrimitive.content.toDouble() == fraction &&
                it.getValue("placement").jsonPrimitive.content == placement &&
                it.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                it.getValue("distribution").jsonPrimitive.content == distribution
    }.getValue("p90OverStroke").jsonPrimitive.content.toDouble()

private fun t284PublishedField(
    file: File,
    enhancement: Double,
    assignment: String
): Double = Json.parseToJsonElement(file.readText())
    .jsonObject.getValue("fields").jsonArray.map { it.jsonObject }
    .first {
        it.getValue("crossSection").jsonPrimitive.content == "10 x 6" &&
                it.getValue("hingeStiffnessEnhancement").jsonPrimitive.content.toDouble() ==
                enhancement &&
                it.getValue("signAssignment").jsonPrimitive.content == assignment
    }.getValue("peakDishingOverStroke").jsonPrimitive.content.toDouble()

private fun departure(published: Double, here: Double): Double =
    if (published == 0.0) abs(here) else abs(here - published) / abs(published)

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val t254 = ResultInputs.T_254.file()
    val t279 = ResultInputs.T_279.file()
    val shared = T284Shared(t284Profile(ResultInputs.T_3B.file()))
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)
    val allowed = allowedScaffoldCrossoverDepartureDegrees()

    // ============================================ Deliverable 1 -- the derivation, no solver
    println("T-284 - the derivation, on the residue lattice, with no solve at all")
    val derivation = ArrayList<T284DerivationRow>()
    listOf(
        "102 / 109 (C-0151, drawable)" to (T284_RECOMMENDED_ONE to T284_RECOMMENDED_TWO),
        "112 / 108 (C-0140, undrawable)" to (T284_UNDRAWABLE_ONE to T284_UNDRAWABLE_TWO),
        "112 / 119 (constructed: every helix at C-0136's residue 0)" to
                (T284_UNIFORM_ONE to T284_UNIFORM_TWO)
    ).forEach { (label, pair) ->
        val signs = HoneycombRasterTurnSigns(shared.block, pair.first, pair.second)
        val determined = signs.classZeroResidueCandidates.size == 1
        derivation += T284DerivationRow(
            raster = label,
            crossSection = "10 x 6",
            rasterCrossovers = signs.reducedResidues.size,
            distinctReducedResidues = signs.reducedResidues.distinct().sorted(),
            classZeroResidueCandidates = signs.classZeroResidueCandidates,
            closes = signs.closes,
            assignmentIsDetermined = determined,
            distinctPerHelixLengthResidues = signs.perHelixLengthResidues.distinct().sorted(),
            alternating = if (determined) signs.isAlternating else null,
            turnsDisplacedFiveAbove =
                if (determined) signs.signs.count { it.displacementBasePairs > 0 } else null,
            turnsDisplacedFiveBelow =
                if (determined) signs.signs.count { it.displacementBasePairs < 0 } else null,
            highRimDisplacementBasePairs =
                if (determined) signs.highRimDisplacementBasePairs else null,
            note = when {
                determined ->
                    "two reduced residues ten apart pin b0 uniquely, so every turn's side of " +
                            "its own staple position is derived and the 2^59 sign assignments " +
                            "collapse to the one GLOBAL phase"
                !signs.closes ->
                    "three reduced residues, no b0, and therefore no determined assignment: on " +
                            "this raster the question T-284 asks has no answer at all"
                else ->
                    "ONE reduced residue, which two b0 candidates admit, so the assignment is " +
                            "genuinely UNIFORM and its sign is genuinely one free binary -- " +
                            "C-0180's own sweep, on a raster it was not taken on. Every helix " +
                            "carries C-0136's residue 0 and therefore carries the sign THROUGH"
            }
        )
    }
    derivation.forEach {
        println("  " + it.raster + " -> residues " + it.distinctReducedResidues +
                ", b0 " + it.classZeroResidueCandidates +
                ", determined " + it.assignmentIsDetermined)
    }

    val recommended = shared.signs
    val agrees = recommended.alternationAgreesWithLengths

    // the convention sweep: eight readings, and the partition by axial rim must not move
    val conventionSweep = ArrayList<T284ConventionRow>()
    listOf(1, -1).forEach { first ->
        listOf(false, true).forEach { mirrored ->
            listOf(false, true).forEach { reversed ->
                val signs = HoneycombRasterTurnSigns(
                    shared.block, T284_RECOMMENDED_ONE, T284_RECOMMENDED_TWO,
                    firstAxialSign = first, mirrored = mirrored, axialReversed = reversed
                )
                val highDeparture = signs.signs.first { it.atHighEnd }.departureDegrees
                conventionSweep += T284ConventionRow(
                    firstAxialSign = first,
                    mirrored = mirrored,
                    axialReversed = reversed,
                    classZeroResidue = signs.classZeroResidue,
                    highRimDisplacementBasePairs = signs.highRimDisplacementBasePairs,
                    highRimDepartureDegrees = highDeparture,
                    alternating = signs.isAlternating,
                    partitionByRimIsUnchanged =
                        abs(highDeparture - allowed) < 1e-12
                )
            }
        }
    }
    val partitionHolds = conventionSweep.all { it.partitionByRimIsUnchanged && it.alternating }

    val faceBeams = (0 until shared.rasterRows).map { it * shared.helicesPerRow }.toSet()
    val ties = honeycombRasterTurnList(shared.block)
    val turns = recommended.signs.mapIndexed { k, sign ->
        T284TurnRow(
            index = sign.index,
            lowerBeam = ties[k].lowerBeam,
            upperBeam = ties[k].upperBeam,
            inPlane = ties[k].inPlane,
            atHighEnd = sign.atHighEnd,
            reducedResidue = sign.reducedResidue,
            displacementBasePairs = sign.displacementBasePairs,
            departureDegrees = sign.departureDegrees
        )
    }
    val faceTurns = turns.count { it.lowerBeam in faceBeams || it.upperBeam in faceBeams }

    val cheapBound = listOf(
        T284CheapBoundRow(
            question = "is the sign of a turn's departure a free binary at all?",
            answer = "no. caDNAno's +-5 bp is an integer approximation to 5.25, so +5 falls a " +
                    "quarter of a base pair SHORT of the exact half turn and -5 sits a quarter " +
                    "PAST it -- equal magnitude, opposite sign -- and C-0148's closure " +
                    "condition says which side every raster crossover is on",
            consequence = "on the recommended raster the reduced residues are " +
                    derivation.first().distinctReducedResidues + ", ten apart, so b0 = " +
                    recommended.classZeroResidue + " is unique and all 59 signs are DERIVED"
        ),
        T284CheapBoundRow(
            question = "is the pattern a theorem or a coincidence of these two lengths?",
            answer = "a theorem. C-0136's per-helix residue (L - 7 Delta_eff) mod 21 is 0 where " +
                    "a helix carries the sign THROUGH and 10 or 11 where it FLIPS it, and " +
                    "102 / 109 reads " + recommended.perHelixLengthResidues.distinct().sorted() +
                    " at every one of its " + recommended.perHelixLengthResidues.size +
                    " interior helices",
            consequence = "so the assignment ALTERNATES, strictly, and the two constructions " +
                    "agree helix for helix: " + agrees
        ),
        T284CheapBoundRow(
            question = "what does the lattice NOT fix?",
            answer = "the one global phase. Displacing a crossover rotates BOTH backbones in " +
                    "the same sense (ForcedCrossoverPrice's own header), and the model's tie " +
                    "prestrain is a RELATIVE roll -- so no lattice fact orients it",
            consequence = "both phases are graded and the verdict is quoted at the WORSE one. " +
                    "C-0180's two UNIFORM readings are neither of them: the lattice's " +
                    "assignment is not uniform"
        )
    )
    cheapBound.forEach { println("  " + it.question + " -> " + it.consequence) }

    // ============================================ Deliverable 2 -- the free tile
    println("T-284 - the free tile at the derived assignment, both phases")
    val columns = HashMap<Double, T284Column>()
    fractions.forEach { columns[it] = T284Column(shared, it, shared.enhancementAt(it)) }
    val bare = T284Column(shared, null, 1.0)

    val freeTile = ArrayList<T284FreeTileRow>()
    val freeTileColumns: List<Pair<Double?, T284Column>> =
        fractions.map { (it as Double?) to columns.getValue(it) } + listOf(null to bare)
    freeTileColumns.forEach { (f, column) ->
        listOf(0 to "zero prestrain", 1 to "the derived assignment, phase +1",
            -1 to "the derived assignment, phase -1").forEach { (phase, label) ->
            val peak = column.freeField(phase)
            freeTile += T284FreeTileRow(
                hingeStiffnessEnhancement = column.enhancement,
                compositeFraction = f,
                signAssignment = label,
                peakDishingOverStroke = peak,
                insideTolerance = peak < T284_TOLERANCE
            )
        }
    }
    freeTile.forEach {
        println("  f = " + (it.compositeFraction?.emitted(3) ?: "none") + "  " +
                it.signAssignment + " -> " + it.peakDishingOverStroke.emitted(9))
    }

    // ============================================ Deliverable 3 -- the 64 cells at both phases
    println("T-284 - the grade, " + t284Realisations + " realisations on C-0167's own stream")
    val probe = honeycombTiedLattice(shared.block, shared.rowBasePairs,
        shared.enhancementAt(0.30), tied = false)
    val rootingHelixY = probe.faceBeams.map { probe.beamY[it] }
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
    val cells = ArrayList<T284Cell>()
    val ensembles = HashMap<String, DropoutEnsemble>()
    gradedColumns.forEach { columnCount ->
        t284Placements(shared, rootingHelixY, columnCount).forEach { (placement, grid) ->
            val key = placement + "|" + columnCount
            val ensemble = ensembles.getOrPut(key) {
                dropoutEnsemble(
                    grid.map { (x, y) -> incorporation.at(x, y) }, t284Realisations, T284_SEED
                )
            }
            t284Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, stiffnesses) ->
                fractions.forEach { fraction ->
                    val column = columns.getValue(fraction)
                    val zeroSurrogate = column.surrogate(key, grid, 0)
                    val zeroSample = dropoutDishingSample(zeroSurrogate, stiffnesses, ensemble)
                    zeroSample.indices.forEach { zeroSample[it] = zeroSample[it] / column.freeStroke }
                    val zeroP90 = summariseDropoutDishing(
                        zeroSample, zeroSurrogate.solve(stiffnesses).peakDishing / column.freeStroke,
                        ensemble.meanSurvivors, T284_TOLERANCE
                    ).p90
                    listOf(1, -1).forEach { phase ->
                        val surrogate = column.surrogate(key, grid, phase)
                        val nominal = surrogate.solve(stiffnesses).peakDishing / column.freeStroke
                        val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                        sample.indices.forEach { sample[it] = sample[it] / column.freeStroke }
                        val summary = summariseDropoutDishing(
                            sample, nominal, ensemble.meanSurvivors, T284_TOLERANCE
                        )
                        cells += T284Cell(
                            phase = phase,
                            signAssignment = "the derived assignment, phase " +
                                    (if (phase > 0) "+1" else "-1"),
                            compositeFraction = fraction,
                            placement = placement,
                            columns = columnCount,
                            pathCount = grid.size,
                            distribution = label,
                            nominalOverStroke = nominal,
                            p90OverStroke = summary.p90,
                            exceedance = summary.exceedance,
                            exceedanceStandardError = summary.exceedanceStandardError,
                            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
                            flatAtNominal = nominal < T284_TOLERANCE,
                            flatAtP90 = summary.flatAtP90,
                            zeroPrestrainP90OverStroke = zeroP90,
                            movementFromZeroPrestrain = summary.p90 - zeroP90
                        )
                    }
                }
            }
        }
    }

    // ============================================ Deliverable 4 -- the sign contingency
    val signContingency = cells.filter { it.phase == 1 }.map { plus ->
        val minus = cells.first {
            it.phase == -1 && it.compositeFraction == plus.compositeFraction &&
                    it.placement == plus.placement && it.columns == plus.columns &&
                    it.distribution == plus.distribution
        }
        T284SignContingentRow(
            compositeFraction = plus.compositeFraction,
            placement = plus.placement,
            columns = plus.columns,
            pathCount = plus.pathCount,
            distribution = plus.distribution,
            p90AtPhasePlus = plus.p90OverStroke,
            p90AtPhaseMinus = minus.p90OverStroke,
            flatAtPhasePlus = plus.flatAtP90,
            flatAtPhaseMinus = minus.flatAtP90,
            verdictDependsOnThePhase = plus.flatAtP90 != minus.flatAtP90,
            worstOfTheTwo = maxOf(plus.p90OverStroke, minus.p90OverStroke),
            flatAtTheWorsePhase = maxOf(plus.p90OverStroke, minus.p90OverStroke) < T284_TOLERANCE
        )
    }
    signContingency.filter { it.flatAtPhasePlus || it.flatAtPhaseMinus }.forEach {
        println("  f=" + it.compositeFraction.emitted(3) + "  " + it.placement + "  " +
                it.columns + " col, " + it.pathCount + " paths, " + it.distribution +
                "  +1 " + it.p90AtPhasePlus.emitted(9) + "  -1 " + it.p90AtPhaseMinus.emitted(9) +
                (if (it.flatAtTheWorsePhase) "  FLAT AT BOTH" else "  sign-contingent"))
    }
    val flatAtBoth = signContingency.count { it.flatAtTheWorsePhase }
    val contingent = signContingency.count { it.verdictDependsOnThePhase }

    // ============================================ Deliverable 5 -- convergence at the thin cells
    println("T-284 - convergence, taken at the cells the verdict rests on")
    val convergence = ArrayList<T284ConvergenceRow>()
    // C-0180's OWN two recovered cells, named rather than re-derived, so `F7` is a statement
    // about the cells the challenge is about and not about whichever two are tightest here.
    val recoveredByC0180 = listOf(
        Triple(0.30, "abstract grid", 3),
        Triple(0.30, "abstract grid on the rooting helices", 5)
    ).map { (f, placement, cols) ->
        signContingency.first {
            it.compositeFraction == f && it.placement == placement && it.columns == cols &&
                    it.distribution == "rim-graded 5:1"
        }
    }
    val deciding = (recoveredByC0180 + signContingency
        .filter { it.flatAtPhasePlus || it.flatAtPhaseMinus }
        .sortedBy { it.worstOfTheTwo }
        .take(2)).distinct()
    val fine = HashMap<Double, T284Column>()
    deciding.forEach { row ->
        val columnCount = row.columns
        val grid = t284Placements(shared, rootingHelixY, columnCount)
            .first { it.first == row.placement }.second
        val key = row.placement + "|" + columnCount
        val stiffnesses = t284Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == row.distribution }.second
        val ensemble = ensembles.getValue(key)
        val column = columns.getValue(row.compositeFraction)
        val refined = fine.getOrPut(row.compositeFraction) {
            T284Column(shared, row.compositeFraction, column.enhancement, subdivisions = 2)
        }
        val name = row.placement + ", " + columnCount + " col, " + row.pathCount + " paths, " +
                row.distribution + ", f = " + row.compositeFraction.emitted(3)
        listOf(1, -1).forEach { phase ->
            fun p90(target: T284Column, samples: Int): Double {
                val surrogate = target.surrogate(key, grid, phase, samples)
                val nominal = surrogate.solve(stiffnesses).peakDishing / target.freeStroke
                val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / target.freeStroke }
                return summariseDropoutDishing(
                    sample, nominal, ensemble.meanSurvivors, T284_TOLERANCE
                ).p90
            }
            val coarse = p90(column, T284_SAMPLES)
            val subdivided = p90(refined, T284_SAMPLES)
            convergence += T284ConvergenceRow(
                cell = name + ", phase " + (if (phase > 0) "+1" else "-1"),
                quantity = "the ensemble's p90 over the stroke",
                axis = "beam subdivisions 1 -> 2",
                coarse = coarse,
                fine = subdivided,
                departure = abs(subdivided - coarse),
                verdictSurvives = (coarse < T284_TOLERANCE) == (subdivided < T284_TOLERANCE)
            )
            val dense = p90(column, 161)
            convergence += T284ConvergenceRow(
                cell = name + ", phase " + (if (phase > 0) "+1" else "-1"),
                quantity = "the ensemble's p90 over the stroke",
                axis = "dishing sample grid 81 -> 161",
                coarse = coarse,
                fine = dense,
                departure = abs(dense - coarse),
                verdictSurvives = (coarse < T284_TOLERANCE) == (dense < T284_TOLERANCE)
            )
        }
    }
    convergence.forEach {
        println("  " + it.axis + "  " + it.cell + "  departure " + it.departure.emitted(2))
    }

    // ============================================ the reproductions
    println("T-284 - the reproductions")
    val reproductions = ArrayList<T284ReproductionRow>()
    // C-0175's own records key on the enhancement ROUNDED to nine digits, which is not the
    // Double this study computes -- so the join is on the composite fraction, and the published
    // literal is what identifies the row over there.
    listOf(21.1851817 to 0.30, 18.4938242 to 0.26, 1.0 to null).forEach { (factor, f) ->
        val here = freeTile.first {
            it.compositeFraction == f && it.signAssignment.endsWith("phase +1")
        }.peakDishingOverStroke
        val published = t284PublishedField(t254, factor, "by the rim the turn sits at")
        reproductions += T284ReproductionRow(
            what = "C-0175 section 8's free tile at " +
                    (if (f == null) "no enhancement" else "f = " + f.emitted(3)) +
                    ", 'by the rim the turn sits at' -- which IS the derived assignment at " +
                    "phase +1",
            published = published,
            here = here,
            relativeDeparture = departure(published, here),
            closes = departure(published, here) < 1e-8
        )
    }
    // and C-0180's four deciding readings, at ITS uniform assignment, are NOT reproduced here --
    // they are quoted as the comparand, out of its own committed file.
    listOf(
        Triple(0.30, "abstract grid", 3),
        Triple(0.30, "abstract grid on the rooting helices", 5)
    ).forEach { (f, placement, cols) ->
        val here = cells.first {
            it.phase == 1 && it.compositeFraction == f && it.placement == placement &&
                    it.columns == cols && it.distribution == "rim-graded 5:1"
        }.zeroPrestrainP90OverStroke
        val published = t284PublishedTied(t279, f, placement, cols, "rim-graded 5:1")
        reproductions += T284ReproductionRow(
            what = "C-0180's zero-prestrain tied cell: " + placement + ", " + cols + " col, " +
                    "rim-graded 5:1, f = " + f.emitted(3),
            published = published,
            here = here,
            relativeDeparture = departure(published, here),
            closes = departure(published, here) < 1e-8
        )
    }
    reproductions.forEach {
        println("  " + it.what + " -> " + it.relativeDeparture.emitted(2) +
                (if (it.closes) "  closes" else "  DOES NOT CLOSE"))
    }

    // ============================================ the falsifiers
    val uniformDishing = bare.structure.solve(uniformPressure(0.02)).peakDishing(T284_SAMPLES)
    // F5: the free field is exactly linear in the assignment vector.
    val linearPlus = bare.plus.solve(shared.pressureField)
    val linearMinus = bare.minus.solve(shared.pressureField)
    val linearZero = bare.structure.solve(shared.pressureField)
    val linearityResidual = (0..20).maxOf { i ->
        (0..20).maxOf { j ->
            val s = bare.structure.lengthS * (i / 20.0 - 0.5)
            val y = bare.structure.lengthY * (j / 20.0 - 0.5)
            abs(
                linearPlus.deflection(s, y) + linearMinus.deflection(s, y) -
                        2.0 * linearZero.deflection(s, y)
            )
        }
    } / bare.freeStroke
    // the bank reuse: one surrogate built the long way must equal the shared-bank one.
    val checkGrid = t284Placements(shared, rootingHelixY, 3).first().second
    val checkColumn = columns.getValue(0.30)
    val checkShare = t284Distributions(checkGrid, shared.edgeX, shared.edgeY)
        .first { it.first == "rim-graded 5:1" }.second
    val bankReuseDeparture = departure(
        honeycombTiedSurrogate(checkColumn.plus, checkGrid, shared.pressureField, T284_SAMPLES)
            .solve(checkShare).peakDishing,
        checkColumn.surrogate("check|3", checkGrid, 1).solve(checkShare).peakDishing
    )

    val falsifiers = listOf(
        T284FalsifierRow(
            id = "F1",
            statement = "the 59 raster crossovers take exactly two reduced residues, ten apart, " +
                    "so b0 has exactly one candidate",
            fired = !(derivation.first().distinctReducedResidues.size == 2 &&
                    derivation.first().classZeroResidueCandidates.size == 1),
            note = "residues " + derivation.first().distinctReducedResidues + ", b0 = " +
                    recommended.classZeroResidue
        ),
        T284FalsifierRow(
            id = "F2",
            statement = "the derived displacement is NOT constant over the 59 turns",
            fired = !recommended.isAlternating,
            note = "strictly alternating: " +
                    recommended.signs.count { it.displacementBasePairs < 0 } + " turns at -5 bp " +
                    "and " + recommended.signs.count { it.displacementBasePairs > 0 } +
                    " at +5 bp, and the per-helix residue is " +
                    recommended.perHelixLengthResidues.distinct().sorted() +
                    " at every interior helix"
        ),
        T284FalsifierRow(
            id = "F3",
            statement = "the partition of the turns by axial rim is the same at every free " +
                    "convention",
            fired = !partitionHolds,
            note = "eight readings over firstAxialSign, mirrored and axialReversed; a turn at " +
                    "the HIGH rim carries +" + allowed.emitted(9) + " degrees at all eight"
        ),
        T284FalsifierRow(
            id = "F4",
            statement = "the derived assignment reproduces C-0175 section 8's free-tile reading",
            fired = reproductions.take(3).any { !it.closes },
            note = "three enhancements, worst relative departure " +
                    reproductions.take(3).maxOf { it.relativeDeparture }.emitted(2)
        ),
        T284FalsifierRow(
            id = "F5",
            statement = "the free field is exactly linear in the assignment vector",
            fired = linearityResidual > T284_IDENTITY,
            note = "peak |field(a) + field(-a) - 2 field(0)| over the stroke is below the " +
                    "identity tolerance " + T284_IDENTITY.emitted(2) + ": " +
                    (linearityResidual < T284_IDENTITY)
        ),
        T284FalsifierRow(
            id = "F6",
            statement = "a uniform pressure on the tied, zero-prestrain lattice dishes exactly " +
                    "zero",
            fired = uniformDishing > T284_IDENTITY,
            note = "the standing falsifier holds with 59 rim ties present, and it is NOT " +
                    "asserted on a prestrained lattice: a uniform eigenstrain relaxes into a " +
                    "cylinder (CLAUDE.md), and the derived assignment is not uniform anyway"
        ),
        T284FalsifierRow(
            id = "F7",
            statement = "DECLARED OPEN -- the two cells C-0180 recovers are flat at BOTH phases " +
                    "of the derived assignment",
            fired = recoveredByC0180.any { !it.flatAtTheWorsePhase },
            note = "C-0180's cell A reads " + recoveredByC0180[0].p90AtPhasePlus.emitted(9) +
                    " at phase +1 and " + recoveredByC0180[0].p90AtPhaseMinus.emitted(9) +
                    " at phase -1; cell B " + recoveredByC0180[1].p90AtPhasePlus.emitted(9) +
                    " and " + recoveredByC0180[1].p90AtPhaseMinus.emitted(9) + ". Of the " +
                    signContingency.count { it.flatAtPhasePlus || it.flatAtPhaseMinus } +
                    " cells flat at either phase, " + flatAtBoth + " are flat at both"
        ),
        T284FalsifierRow(
            id = "F8",
            statement = "DECLARED OPEN -- the flat census over all 64 cells is the same at both " +
                    "phases",
            fired = contingent > 0,
            note = contingent.toString() + " of 64 cells have a verdict that depends on the phase"
        ),
        T284FalsifierRow(
            id = "F9",
            statement = "the undrawable 112 / 108 raster determines no assignment at all",
            fired = derivation[1].assignmentIsDetermined,
            note = "three reduced residues " + derivation[1].distinctReducedResidues +
                    " and no b0, so the question T-284 answers has no answer there"
        ),
        T284FalsifierRow(
            id = "F11",
            statement = "a raster carrying ONE reduced residue leaves TWO b0 candidates, and the " +
                    "class refuses rather than guessing -- the state no raster this repository " +
                    "owns is in, constructed because a mutation relaxing that check failed " +
                    "nothing against the corpus's own two",
            fired = !(derivation.last().distinctReducedResidues.size == 1 &&
                    derivation.last().classZeroResidueCandidates.size == 2 &&
                    !derivation.last().assignmentIsDetermined),
            note = "112 / 119 on 10 x 6: reduced residues " +
                    derivation.last().distinctReducedResidues + ", b0 candidates " +
                    derivation.last().classZeroResidueCandidates + ", per-helix residues " +
                    derivation.last().distinctPerHelixLengthResidues +
                    " -- so C-0180's UNIFORM assignment is the right one for a raster this " +
                    "programme does not recommend"
        ),
        T284FalsifierRow(
            id = "F10",
            statement = "the shared influence bank equals a surrogate built the long way",
            fired = bankReuseDeparture > T284_IDENTITY,
            note = "the prestrain is a LOAD, so one bank on the zero-prestrain lattice serves " +
                    "both phases; asserted below " + T284_IDENTITY.emitted(2) + ": " +
                    (bankReuseDeparture < T284_IDENTITY)
        )
    )
    falsifiers.forEach {
        println("  " + it.id + (if (it.fired) "  FIRED  " else "  did not fire  ") + it.statement)
    }

    val worstFlat = signContingency.filter { it.flatAtTheWorsePhase }.minByOrNull { it.worstOfTheTwo }
    val verdict = linkedMapOf(
        "whatSetsTheSign" to ("which of caDNAno's two +-5 bp positions the crossover occupies, " +
                "which C-0148's closure condition determines: on the recommended 102 / 109 " +
                "raster the reduced residues are " + derivation.first().distinctReducedResidues +
                ", ten apart, so b0 = " + recommended.classZeroResidue + " is unique"),
        "theAssignment" to ("strictly ALTERNATING along the raster path -- equivalently, set by " +
                "the axial RIM the turn sits at: " +
                recommended.signs.count { it.displacementBasePairs < 0 } +
                " turns at the high rim carry -5 bp and +" + allowed.emitted(9) + " degrees, " +
                recommended.signs.count { it.displacementBasePairs > 0 } +
                " at the low rim carry +5 bp and the negation"),
        "howMuchFreedomIsLeft" to "one global phase of 2^59",
        "whyThatOneIsNotDerivable" to ("a level displacement rotates BOTH backbones the same " +
                "way, so the departure is COMMON-MODE, and the model's tie prestrain is a " +
                "RELATIVE roll -- see CH-0240"),
        "cellsFlatAtP90AtPhasePlus" to (cells.count { it.phase == 1 && it.flatAtP90 }
            .toString() + " of 64"),
        "cellsFlatAtP90AtPhaseMinus" to (cells.count { it.phase == -1 && it.flatAtP90 }
            .toString() + " of 64"),
        "cellsFlatAtBOTHphases" to (flatAtBoth.toString() + " of 64"),
        "cellsWhoseVerdictDependsOnThePhase" to (contingent.toString() + " of 64"),
        "C-0180sUniformReadingAtEitherSign" to "1 of 64, a different one at each sign",
        "C-0180sTwoRecoveredCellsAtTheDerivedAssignment" to ("cell A " +
                recoveredByC0180[0].p90AtPhasePlus.emitted(9) + " / " +
                recoveredByC0180[0].p90AtPhaseMinus.emitted(9) + ", cell B " +
                recoveredByC0180[1].p90AtPhasePlus.emitted(9) + " / " +
                recoveredByC0180[1].p90AtPhaseMinus.emitted(9) + " -- flat at BOTH phases at " +
                recoveredByC0180.count { it.flatAtTheWorsePhase } + " of 2"),
        "theMarginAtTheWORSEphase" to (worstFlat?.let {
            it.worstOfTheTwo.emitted(9) + " against T-5b's " + T284_TOLERANCE.emitted(2) + ", " +
                    ((1.0 - it.worstOfTheTwo / T284_TOLERANCE) * 100.0).emitted(3) + " per cent " +
                    "of the tolerance, at " + it.placement + ", " + it.columns + " col, " +
                    it.pathCount + " paths, " + it.distribution + ", f = " +
                    it.compositeFraction.emitted(3)
        } ?: "no cell is flat at both phases"),
        "turnsTouchingTheGapFacingFace" to (faceTurns.toString() + " of " + turns.size),
        "worstMovementFromZeroPrestrain" to
                cells.maxOf { abs(it.movementFromZeroPrestrain) }.emitted(9)
    )

    val findings = listOf(
        "THE SIGN IS DERIVED, AND IT IS THE SAME RULE THAT SUPPLIES THE MAGNITUDE. caDNAno's " +
                "'five base pairs, OR HALF A TURN' is an integer approximation to 5.25 bp, so a " +
                "crossover placed +5 bp from its pair's staple position falls a quarter of a " +
                "base pair SHORT of the exact half turn and one placed -5 bp sits a quarter " +
                "PAST it. Which side a raster turn takes is C-0148's closure condition, already " +
                "modelled in this tree as (level - 7 class) mod 21: on the recommended 102 / " +
                "109 raster the residues are " + derivation.first().distinctReducedResidues +
                ", exactly ten apart, so b0 = " + recommended.classZeroResidue + " is UNIQUE " +
                "and all 59 signs follow. The 2^59 assignments C-0175 calls unfixed collapse to " +
                "TWO.",
        "AND IT IS STRICTLY ALTERNATING, WHICH IS A THEOREM ABOUT THE ROW LENGTHS RATHER THAN A " +
                "PROPERTY OF THIS BLOCK. C-0136's per-helix residue (L - 7 Delta_eff) mod 21 is " +
                "0 where a helix carries the sign THROUGH and 10 or 11 where it FLIPS it; " +
                "102 / 109 reads " + recommended.perHelixLengthResidues.distinct().sorted() +
                " at every one of its " + recommended.perHelixLengthResidues.size +
                " interior helices, so every helix flips. Read on the block instead of on the " +
                "path it is simpler still: a turn at the HIGH axial rim carries +" +
                allowed.emitted(9) + " degrees and one at the low rim the negation, at all " +
                "eight readings of the free conventions. That is C-0175 section 8's third " +
                "swept assignment, 'by the rim the turn sits at' -- one of three guesses, and " +
                "the derived one.",
        "SO C-0180's TWO UNIFORM READINGS ARE BOTH OFF-LATTICE, and the binary that is really " +
                "left is a GLOBAL phase rather than a uniform sign. Graded on the same 64 " +
                "cells, the same stations, the same distributions and the same dropout stream: " +
                cells.count { it.phase == 1 && it.flatAtP90 } + " of 64 are flat at phase +1, " +
                cells.count { it.phase == -1 && it.flatAtP90 } + " at phase -1, " + flatAtBoth +
                " at BOTH, and " + contingent + " of 64 carry a verdict that depends on the " +
                "phase.",
        "THE ONE THING THE LATTICE CANNOT ORIENT IS THE ONE THING THE MODEL NEEDS, and that is " +
                "CH-0240 rather than a gap in this derivation: a level displacement rotates " +
                "BOTH backbones in the SAME sense -- ForcedCrossoverPrice's own header says so " +
                "-- so the departure is common-mode, and the coefficient of a common-mode " +
                "azimuth on a RELATIVE-roll spring is exactly zero. The global phase is a " +
                "property of the model's sign convention, not of the raster.",
        "THE FREE TILE COULD NOT HAVE SEEN ANY OF THIS. C-0175 section 8's three assignments " +
                "span 0.7 per cent there and its 'by the rim' row is the derived one by " +
                "accident; the phase is invisible on the free tile because peak dishing under " +
                "the prestrain alone is a seminorm. It is only against the collar load, and " +
                "only at a coupled margin, that the phase reaches a verdict.",
        "AND A UNIFORM ASSIGNMENT DOES EXIST -- ON A RASTER THIS PROGRAMME DOES NOT RECOMMEND. " +
                "C-0136's per-helix residue 0 carries the sign THROUGH a helix, so a raster whose " +
                "every helix is at 0 puts all 59 crossovers on ONE reduced residue, which TWO b0 " +
                "candidates admit: the assignment is then genuinely uniform and its sign is " +
                "genuinely one free binary. 112 / 119 on the 10 x 6 block is such a raster -- " +
                "112 = 7 and 119 = 14 modulo 21, which are 7 Delta at this block's two effective " +
                "senses. So C-0180's UNIFORM sweep is the right sweep for the wrong raster, and " +
                "the state was CONSTRUCTED rather than found: a mutation relaxing " +
                "classZeroResidue's uniqueness check to a non-emptiness one failed NOTHING " +
                "against the corpus's own two rasters, because one has exactly one candidate and " +
                "the other has none.",
        "THE UNDRAWABLE RASTER HAS NO ANSWER AT ALL. 112 / 108 carries three reduced residues " +
                derivation[1].distinctReducedResidues + " and no b0, so its ten forced " +
                "crossovers are not the only thing it costs: on that raster no turn's sign is " +
                "determined, and the question this task answers cannot be asked. C-0151 " +
                "selected 102 / 109 on closure and this is a second consequence of the same rule."
    )

    val result = T284Result(
        task = "T-284",
        leaf = "A8.2",
        title = "What sets the sign of a raster turn's 8.57142857 degree departure",
        verificationType = "logical (a congruence on caDNAno's own +-5 bp rule, exhaustible " +
                "over the 59 turns and over every free convention, with no solver) + in-silico " +
                "(the derived assignment graded through the same beam-and-bond lattice, the " +
                "same exact Woodbury coupling surrogate and the same measured-incorporation " +
                "dropout ensemble C-0180 used)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "departure's magnitude is C-0152's rigid-duplex reading of caDNAno's own rule, " +
                "not a measurement; k_theta at a scaffold turn is asserted equal to k_theta at " +
                "a staple crossover; the tie's axial station is s = +-L/2 exactly.",
        units = linkedMapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 = 1 MPa",
            "angle" to "rad internally, degrees in prose and in every emitted departure",
            "level" to "integer base pairs on one global z",
            "dishing" to "dimensionless, as a fraction of the free stroke"
        ),
        conventions = linkedMapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "z" to "along the block's thickness",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "azimuth" to "viewed from +z the backbone azimuth INCREASES counter-clockwise with " +
                    "z, so one base pair is +240/7 degrees and one class step (+7 bp) is +240",
            "displacement" to "the base pairs a scaffold crossover sits from its pair's staple " +
                    "position; +5 is a quarter of a base pair SHORT of the exact +5.25 half " +
                    "turn and therefore a NEGATIVE departure",
            "phase" to "the one binary the lattice does not fix: which way the derived departure " +
                    "maps onto the model's relative roll Phi_upper - Phi_lower",
            "residue" to "mod 21, non-negative, datum at the first raster crossover (C-0140)"
        ),
        parameters = linkedMapOf(
            "crossSection" to "10 x 6",
            "raster" to "102 / 109 (C-0151, drawable), with 112 / 108 as the control",
            "rowBasePairs" to T284_BLOCK_EXTENT_BP.toString(),
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
            "realisations" to t284Realisations.toString(),
            "seed" to T284_SEED.toString(),
            "samples" to T284_SAMPLES.toString(),
            "tolerance" to T284_TOLERANCE.emitted(2),
            "ladderPhase" to T284_LADDER_PHASE.toString(),
            "ladderOffset" to T284_LADDER_OFFSET.toString(),
            "allowedScaffoldCrossoverDepartureDegrees" to allowed.emitted(9),
            "exactHalfTurnBasePairs" to EXACT_HALF_TURN_BASE_PAIRS.emitted(9),
            "firstAxialSign" to "+1"
        ),
        sources = listOf(
            "Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, Nucleic Acids Res. " +
                    "37:5001 (caDNAno; PMC2731887, in gpd/data/T-151-sources/), read directly " +
                    "-- the +-5 bp scaffold rule and the 21 bp period",
            "gpd/results/T-254-raster-turn-prestrain.json (C-0175) -- the tie set and the three " +
                    "swept sign assignments on the free tile",
            "gpd/results/T-279-tied-honeycomb-regrade.json (C-0180) -- the 64 coupled cells, " +
                    "the zero-prestrain tied readings and the two uniform prestrained ones",
            "gpd/results/T-3b-tile-edge-load-profile.json (C-0022) -- the solved collar"
        ),
        citedInputs = linkedMapOf(
            "C-0152 allowed scaffold-crossover departure [deg]" to allowed.emitted(9),
            "C-0175 free tile, by the rim, f = 0.30" to
                    t284PublishedField(t254, 21.1851817, "by the rim the turn sits at").emitted(9),
            "C-0175 free tile, every turn the same way, f = 0.30" to
                    t284PublishedField(t254, 21.1851817, "every turn the same way").emitted(9),
            "C-0180 recovered cell A, zero prestrain" to
                    t284PublishedTied(t279, 0.30, "abstract grid", 3, "rim-graded 5:1").emitted(9),
            "C-0180 recovered cell B, zero prestrain" to t284PublishedTied(
                t279, 0.30, "abstract grid on the rooting helices", 5, "rim-graded 5:1"
            ).emitted(9),
            "C-0180 recovered cell A at a uniform +8.57142857 deg" to t284PublishedPrestrained(
                t279, 0.30, "abstract grid", 3, "rim-graded 5:1", 1
            ).emitted(9),
            "C-0180 recovered cell A at a uniform -8.57142857 deg" to t284PublishedPrestrained(
                t279, 0.30, "abstract grid", 3, "rim-graded 5:1", -1
            ).emitted(9),
            "C-0180 recovered cell B at a uniform +8.57142857 deg" to t284PublishedPrestrained(
                t279, 0.30, "abstract grid on the rooting helices", 5, "rim-graded 5:1", 1
            ).emitted(9),
            "C-0180 recovered cell B at a uniform -8.57142857 deg" to t284PublishedPrestrained(
                t279, 0.30, "abstract grid on the rooting helices", 5, "rim-graded 5:1", -1
            ).emitted(9)
        ),
        cheapBound = cheapBound,
        derivation = derivation,
        conventionSweep = conventionSweep,
        turns = turns,
        freeTile = freeTile,
        cells = cells,
        signContingency = signContingency,
        verdict = verdict,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "The derivation is about caDNAno's DEFAULT rule. A design that forces a crossover " +
                    "puts it somewhere the rule does not allow, and no residue then says which " +
                    "side it is on; the recommended raster forces none.",
            "The magnitude is C-0152's rigid-duplex reading and is a CEILING: nothing here " +
                    "bounds from below how much of the 0.25 bp is taken up in backbone strain " +
                    "or local unstacking instead of in a roll.",
            "The map from a derived departure to the model's RELATIVE roll is not derivable at " +
                    "all -- the departure is common-mode. Both phases are therefore graded and " +
                    "the margin is quoted at the worse; CH-0240 is raised against the channel.",
            "k_theta at a scaffold turn is asserted equal to k_theta at a staple crossover " +
                    "because it is the same covalent object, and k_theta itself is Gen1Tile's " +
                    "square-lattice-fitted constant (C-0175, C-0180, inherited verbatim).",
            "The tie sits at s = +-L/2 exactly; a scaffold crossover sits 5 bp from a staple " +
                    "position, so its true axial station is within 1.7 nm of the rim node, and " +
                    "nothing here prices that (C-0180's own open question).",
            "The lattice carries no across-helix parallel-axis term, so its D_perp is the " +
                    "independent one and a lower bound; Kirchhoff is not safe at these " +
                    "thicknesses, so every D_parallel is an upper bound.",
            "The dropout statistics are measured on a single-layer Rothemund rectangle and only " +
                    "the profile transfers, in nm; the ensemble perturbs the COUPLING and never " +
                    "the block's crossovers or its ties. A missing scaffold turn is not in this " +
                    "model at all.",
            "Nothing here re-opens the placement search, the distribution rule, the raster or " +
                    "the cross-section. The stations are C-0151's and the distributions " +
                    "C-0058's two."
        ),
        openQuestions = listOf(
            "What the common-mode departure IS worth, which needs a per-beam torsional " +
                    "eigenstrain this tree does not carry: the alternation puts opposite roll " +
                    "demands at a helix's two ends, i.e. " + (2.0 * allowed).emitted(9) +
                    " degrees of demanded TWIST over its own row -- which is, to the digit, the " +
                    "departure C-0152 prices a FORCED crossover at. That is T-291.",
            "Whether a distribution SEARCHED on the tied lattice at the derived assignment " +
                    "recovers more than the cells found here, at either phase.",
            "What the tie's true axial station is worth: 5 bp from the rim node, against a " +
                    "margin measured in tenths of a per cent of the tolerance.",
            "Whether any OTHER closing two-length pair puts a per-helix residue of 0 somewhere, " +
                    "which would give a raster whose sign assignment is partly uniform -- the " +
                    "family is 441 residue pairs and the census is one modular pass."
        ),
        proseFailure = "none"
    )

    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    val encoded = json.encodeToJsonElement(result)
        .roundedForResult(digits = 9, floor = 1e-12)
        .withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject
    val out = File("gpd/results/T-284-turn-prestrain-sign.json")
    out.parentFile?.mkdirs()
    out.writeText(json.encodeToString(JsonObject.serializer(), encoded) + "\n")
    println("T-284 - written to " + out.path)
    findings.forEach { println("  " + it) }
}
