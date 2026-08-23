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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
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
import com.xemantic.nano.plentyofroom.structure.minimumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.turnPhosphateSpan
import com.xemantic.nano.plentyofroom.thermalEnergy
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
// T-291 -- the allowed departure is COMMON-MODE, and what replaces it is a per-beam TWIST.
//
// CH-0240 disputes a coordinate without disputing a number. A level displacement rotates BOTH
// backbones the same way, so the relative azimuth is level-independent and the departure has
// coefficient exactly zero on Phi_upper - Phi_lower, which is the coordinate CH-0228, C-0175
// section 8 and C-0180 section 4 all load it through.
//
// What replaces it is not zero. C-0187 derives that the departures ALTERNATE, so an interior
// helix is asked to roll one way at one end and the other way at the other: a TWIST of
// 2 x 8.57142857 = 17.1428571 degrees over its own row. That is a per-beam torsional eigenstrain,
// its magnitude and its uniformity are derived here, and its one GLOBAL phase is not derivable --
// the lattice carries no handedness at all -- so both signs are graded and the worse is quoted.
// ---------------------------------------------------------------------------------------------

private const val T291_SAMPLES: Int = 81
private const val T291_TOLERANCE: Double = 0.10
private const val T291_RIM_STANDOFF: Double = 1.0
private const val T291_RIM_BAND: Double = 6.7
private const val T291_SEED: Long = 197_197L
private const val T291_BLOCK_EXTENT_BP: Int = 116
private const val T291_LADDER_PHASE: Int = 16
private const val T291_LADDER_OFFSET: Int = 14
private const val T291_RECOMMENDED_ONE: Int = 102
private const val T291_RECOMMENDED_TWO: Int = 109
private const val T291_UNDRAWABLE_ONE: Int = 112
private const val T291_UNDRAWABLE_TWO: Int = 108
private const val T291_IDENTITY: Double = 1e-9

/** `C-0079`'s measured cost of holding one crossover column of the host sheet, in `k_BT`. */
private const val T291_HOST_COLUMN_KT: Double = HOST_SHEET_COLUMN_ENERGY_KT

/** The study runs at 4 000 realisations; `T291_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t291Realisations: Int =
    if (System.getenv("T291_SMOKE") == "1") 150 else 4000

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T291CheapBoundRow(
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private class T291ChannelRow(
    val statement: String,
    val quantity: String,
    val value: Double,
    val threshold: Double,
    val holds: Boolean,
    val note: String
)

@Serializable
private class T291TwistRow(
    val raster: String,
    val determined: Boolean,
    val beams: Int,
    val beamsCarryingTwist: Int,
    val beamsFree: Int,
    val distinctTwistDegrees: List<Double>,
    val twistDegreesAtPhasePlus: Double?,
    val note: String
)

@Serializable
private class T291ConventionRow(
    val firstAxialSign: Int,
    val mirrored: Boolean,
    val axialReversed: Boolean,
    val beamsCarryingTwist: Int,
    val twistMagnitudeDegrees: Double,
    val uniformInSign: Boolean
)

@Serializable
private class T291EnergyRow(
    val over: String,
    val beamLength: Double,
    val torsionalStiffness: Double,
    val restrainedEnergyPerBeam: Double,
    val restrainedEnergyPerBeamKt: Double,
    val restrainedEnergyPerBlockKt: Double,
    val overOneHostSheetColumn: Double
)

@Serializable
private class T291FreeTileRow(
    val hingeStiffnessEnhancement: Double,
    val compositeFraction: Double?,
    val term: String,
    val peakDishingOverStroke: Double,
    val insideTolerance: Boolean,
    val movementFromNoTerm: Double
)

@Serializable
private class T291CeilingRow(
    val hingeStiffnessEnhancement: Double,
    val compositeFraction: Double?,
    val largestSingleBeamPerRadian: Double,
    val medianSingleBeamPerRadian: Double,
    val smallestSingleBeamPerRadian: Double,
    val sumOverBeamsPerRadian: Double,
    val triangleCeilingAtTheDemand: Double,
    val realisedAtPhasePlus: Double,
    val realisedOverCeiling: Double,
    val ceilingIsRespected: Boolean
)

@Serializable
private class T291RelaxationRow(
    val compositeFraction: Double?,
    val restrainedEnergy: Double,
    val loadWork: Double,
    val relaxedEnergy: Double,
    val relaxedOverRestrained: Double,
    val ceilingIsRespected: Boolean
)

@Serializable
private class T291Cell(
    val phase: Int,
    val term: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val nominalOverStroke: Double,
    val p90OverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val zeroTermP90OverStroke: Double,
    val movementFromZeroTerm: Double
)

@Serializable
private class T291SignRow(
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
private class T291ChannelCell(
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val zeroTermP90OverStroke: Double,
    val twistWorstOverStroke: Double,
    val relativeRollWorstOverStroke: Double,
    val twistMovement: Double,
    val relativeRollMovement: Double,
    val twistOverRelativeRoll: Double,
    val flatAtTheWorseTwistSign: Boolean,
    val flatAtTheWorseRelativeRollPhase: Boolean,
    val theTwoChannelsAgree: Boolean
)

@Serializable
private class T291ScaleRow(
    val fractionOfTheDerivedEigenstrain: Double,
    val impliedCommonModeStiffness: Double,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val distribution: String,
    val p90AtPhasePlus: Double,
    val p90AtPhaseMinus: Double,
    val worstOfTheTwo: Double,
    val flatAtBothSigns: Boolean
)

@Serializable
private class T291ConvergenceRow(
    val cell: String,
    val quantity: String,
    val axis: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double,
    val verdictSurvives: Boolean
)

@Serializable
private class T291ReproductionRow(
    val what: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double,
    val closes: Boolean
)

@Serializable
private class T291FalsifierRow(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T291Result(
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
    val cheapBound: List<T291CheapBoundRow>,
    val channel: List<T291ChannelRow>,
    val twist: List<T291TwistRow>,
    val conventionSweep: List<T291ConventionRow>,
    val energy: List<T291EnergyRow>,
    val freeTile: List<T291FreeTileRow>,
    val ceiling: List<T291CeilingRow>,
    val relaxation: List<T291RelaxationRow>,
    val cells: List<T291Cell>,
    val signContingency: List<T291SignRow>,
    val scale: List<T291ScaleRow>,
    val channelComparison: List<T291ChannelCell>,
    val verdict: Map<String, String>,
    val convergence: List<T291ConvergenceRow>,
    val reproductions: List<T291ReproductionRow>,
    val falsifiers: List<T291FalsifierRow>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T291Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T291_RIM_STANDOFF))
        )
}

private fun t291Profile(file: File): T291Profile {
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
    return T291Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`'s, `C-0180`'s and `C-0187`'s geometry, unchanged, so the cells pair exactly. */
private class T291Shared(val profile: T291Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T291_BLOCK_EXTENT_BP
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
        block, T291_RECOMMENDED_ONE, T291_RECOMMENDED_TWO
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

/** One field scaled — exact, because a prestrain is a load and the lattice is linear. */
private fun scaled(a: DishingSolution, factor: Double): DishingSolution =
    object : DishingSolution {
        override fun deflectionAt(x: Double, y: Double) = factor * a.deflectionAt(x, y)

        override fun dishingAt(x: Double, y: Double) = factor * a.dishingAt(x, y)
    }

/** Two fields added — exact, because a prestrain is a load and the lattice is linear. */
private fun sum(a: DishingSolution, b: DishingSolution): DishingSolution =
    object : DishingSolution {
        override fun deflectionAt(x: Double, y: Double) =
            a.deflectionAt(x, y) + b.deflectionAt(x, y)

        override fun dishingAt(x: Double, y: Double) = a.dishingAt(x, y) + b.dishingAt(x, y)
    }

/**
 * One composite fraction's lattice.
 *
 * There is exactly **one** — the tied, zero-prestrain block — because the twist eigenstrain never
 * enters the lattice at all: it is a load, applied through
 * [HoneycombGrillage.beamTwistResponse] on the same factorisation. So `C-0104`'s trap is avoided
 * *structurally* rather than by remembering to call `withoutPrestrain`, and one influence bank
 * serves both signs by construction.
 */
private class T291Column(
    val shared: T291Shared,
    val fraction: Double?,
    val enhancement: Double,
    val subdivisions: Int = 1
) {

    val structure: HoneycombGrillage = honeycombTiedLattice(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        enhancement = enhancement,
        tied = true,
        subdivisions = subdivisions
    )

    val freeStroke: Double =
        structure.solve(uniformPressure(shared.interiorPressure)).meanDeflection

    private val pressureSolution: DishingSolution by lazy {
        structure.solve(shared.pressureField).asDishing()
    }

    fun twistResponse(phase: Int): HoneycombDeflection =
        structure.beamTwistResponse(honeycombBeamTwists(shared.signs, phase))

    private val freeFields = HashMap<Int, DishingSolution>()

    private val twistFields = HashMap<Int, DishingSolution>()

    private fun twistField(phase: Int): DishingSolution =
        twistFields.getOrPut(phase) { twistResponse(phase).asDishing() }

    fun freeSolution(phase: Int): DishingSolution = freeFields.getOrPut(phase) {
        if (phase == 0) pressureSolution else sum(pressureSolution, twistField(phase))
    }

    /**
     * The free field at [fraction] of the derived eigenstrain — **no extra solve**, because the
     * field is exactly linear in the load and the twist response is already on hand. It is what
     * turns the tie's unmodelled common-mode stiffness into a **threshold** rather than a value.
     */
    fun scaledFreeSolution(phase: Int, fraction: Double): DishingSolution =
        if (fraction == 0.0) pressureSolution
        else sum(pressureSolution, scaled(twistField(phase), fraction))

    fun scaledSurrogate(
        key: String,
        grid: List<Pair<Double, Double>>,
        phase: Int,
        fraction: Double,
        samples: Int = T291_SAMPLES
    ): InfluenceSurrogate = influenceSurrogate(
        grid, structure.lengthS / 2.0, structure.lengthY / 2.0, samples,
        scaledFreeSolution(phase, fraction), bank(key, grid)
    )

    fun freeField(phase: Int, samples: Int = T291_SAMPLES): Double {
        var worst = 0.0
        val solution = freeSolution(phase)
        // exactly `HoneycombGrillage.overFaceGrid`'s own sampling, so a summed field and a
        // solved one are read on the same points and the reproductions are comparable
        for (i in 0 until samples) {
            val s = -structure.lengthS / 2.0 + structure.lengthS * i / (samples - 1)
            for (j in 0 until samples) {
                val y = -structure.lengthY / 2.0 + structure.lengthY * j / (samples - 1)
                worst = maxOf(worst, abs(solution.dishingAt(s, y)))
            }
        }
        return worst / freeStroke
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
        samples: Int = T291_SAMPLES
    ): InfluenceSurrogate = influenceSurrogate(
        grid, structure.lengthS / 2.0, structure.lengthY / 2.0, samples,
        freeSolution(phase), bank(key, grid)
    )
}

private fun t291Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T291_RIM_BAND || abs(y) > edgeY / 2.0 - T291_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, unchanged, so the pairing against `C-0180`/`C-0187` is exact. */
private fun t291Placements(
    shared: T291Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T291_RECOMMENDED_ONE, T291_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T291_LADDER_PHASE, T291_LADDER_OFFSET
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

private fun t291PublishedCell(
    file: File,
    fraction: Double,
    placement: String,
    columns: Int,
    distribution: String,
    phase: Int,
    field: String
): Double = Json.parseToJsonElement(file.readText())
    .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
    .first {
        it.getValue("compositeFraction").jsonPrimitive.content.toDouble() == fraction &&
                it.getValue("placement").jsonPrimitive.content == placement &&
                it.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                it.getValue("distribution").jsonPrimitive.content == distribution &&
                it.getValue("phase").jsonPrimitive.content.toInt() == phase
    }.getValue(field).jsonPrimitive.content.toDouble()

private fun t291PublishedField(
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

private fun t291PublishedZeroField(file: File, enhancement: Double): Double =
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("freeTile").jsonArray.map { it.jsonObject }
        .first {
            it.getValue("hingeStiffnessEnhancement").jsonPrimitive.content.toDouble() ==
                    enhancement &&
                    it.getValue("signAssignment").jsonPrimitive.content == "zero prestrain"
        }.getValue("peakDishingOverStroke").jsonPrimitive.content.toDouble()

private fun departure(published: Double, here: Double): Double =
    if (published == 0.0) abs(here) else abs(here - published) / abs(published)

/**
 * The `Φ` degree of freedom of `(node, beam)`, reconstructed from the class's public layout.
 *
 * `CLAUDE.md`: a private DOF layout is reconstructible from a public API and must be **asserted**
 * against the class's own basis vector rather than assumed — which is what [t291CheckLayout] does.
 */
private fun t291Phi(lattice: HoneycombGrillage, node: Int, beam: Int): Int =
    (node * lattice.beamCount + beam) * HoneycombGrillage.DOF_PER_NODE + HoneycombGrillage.PHI

private fun t291CheckLayout(lattice: HoneycombGrillage) {
    val piston = lattice.pistonMode
    (0 until lattice.nodesPerBeam).forEach { node ->
        (0 until lattice.beamCount).forEach { beam ->
            val phi = t291Phi(lattice, node, beam)
            check(piston[phi] == 0.0 && piston[phi - HoneycombGrillage.PHI] == 1.0) {
                "the reconstructed DOF layout does not match the lattice's own basis vector"
            }
        }
    }
}

/** `Lᵀu` in pN·nm — the work the eigenstrain load does on its own relaxed field. */
private fun t291LoadWork(
    lattice: HoneycombGrillage,
    twists: Map<Int, Double>,
    response: HoneycombDeflection
): Double {
    val span = lattice.nodeS.last() - lattice.nodeS.first()
    var total = 0.0
    twists.forEach { (beam, twist) ->
        val couple = lattice.duplex.torsionalRigidity * twist / span
        total += couple * response.coefficients[t291Phi(lattice, lattice.nodesPerBeam - 1, beam)]
        total -= couple * response.coefficients[t291Phi(lattice, 0, beam)]
    }
    return total
}

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val t254 = ResultInputs.T_254.file()
    val t284 = ResultInputs.T_284.file()
    val shared = T291Shared(t291Profile(ResultInputs.T_3B.file()))
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)
    val allowed = allowedScaffoldCrossoverDepartureDegrees()
    val demanded = 2.0 * allowed
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val kt = thermalEnergy(ROOM_TEMPERATURE)
    val gj = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY

    // ==================================== Deliverable 1 -- does CH-0240 stand? No solve at all
    println("T-291 - the channel, settled on the lattice and on the model's own source")
    val channel = ArrayList<T291ChannelRow>()

    val worstRelative = (-21..21).maxOf {
        abs(honeycombBondRelativeAzimuthDegrees(it) - 180.0)
    }
    channel += T291ChannelRow(
        statement = "the two backbones of a honeycomb scaffold crossover are ANTIPODAL at every " +
                "level displacement, so d(psi_P - psi_Q)/d(level) is identically zero",
        quantity = "the largest departure of the relative azimuth from 180 degrees over the " +
                "whole 21 bp period, both signs",
        value = worstRelative,
        threshold = 1e-12,
        holds = worstRelative < 1e-12,
        note = "ForcedCrossoverPrice's own (theta, 180 + theta) construction, read as a " +
                "difference. The algebra is exact; the arithmetic loses the last ulp of theta"
    )

    val spanFloor = minimumTurnPhosphateSpan(shared.d, rP)
    val builtSpan = forcedCrossoverSpan(shared.d, rP, allowed)
    val shortfall = relativeRollSpanShortfall(shared.d, rP, allowed)
    val stationary = relativeRollStationaryCosine(shared.d, rP, allowed)
    var brute = Double.MAX_VALUE
    (-3600..3600).forEach { i ->
        val r = i / 100.0
        brute = minOf(brute, turnPhosphateSpan(shared.d, rP, allowed + r, 180.0 + allowed - r))
    }
    channel += T291ChannelRow(
        statement = "the RELATIVE channel does not relieve a common-mode departure at all: its " +
                "stationary point u* = d cos(delta) / (2 r_P) lies OUTSIDE the reachable cos r",
        quantity = "u*",
        value = stationary,
        threshold = 1.0,
        holds = stationary > 1.0,
        note = "so the minimum over the whole relative channel is at r = 0, the BUILT state, " +
                "and the closed form agrees with a brute sweep to " +
                abs(brute - (spanFloor + shortfall)).emitted(2) + " nm"
    )
    channel += T291ChannelRow(
        statement = "the span the best relative roll still leaves above the minimum is the WHOLE " +
                "departure's span excess",
        quantity = "shortfall in nm",
        value = shortfall,
        threshold = 0.0,
        holds = shortfall > 0.0,
        note = "built span " + builtSpan.emitted(9) + " nm against a floor of " +
                spanFloor.emitted(9) + " nm; the common-mode relief reaches the floor exactly"
    )

    // the model's own coordinate: the tie prestrain load against the demanded kinematics
    val probeLattice = honeycombTiedLattice(
        shared.block, shared.rowBasePairs, shared.enhancementAt(0.30), tied = true
    )
    t291CheckLayout(probeLattice)
    val armed = honeycombTiedLattice(
        shared.block, shared.rowBasePairs, shared.enhancementAt(0.30), tied = true,
        prestrainRadians = Math.toRadians(allowed)
    )
    val zeroLoad = probeLattice.assembleLoad(uniformPressure(0.0))
    val armedLoad = armed.assembleLoad(uniformPressure(0.0))
    val prestrainLoad = DoubleArray(armed.degreesOfFreedom) { armedLoad[it] - zeroLoad[it] }
    val loadIsNonZero = prestrainLoad.any { abs(it) > 1e-12 }
    val worstProjection = armed.turnElements.maxOf { element ->
        abs(
            prestrainLoad[t291Phi(armed, element.node, element.tie.upperBeam)] +
                    prestrainLoad[t291Phi(armed, element.node, element.tie.lowerBeam)]
        )
    }
    channel += T291ChannelRow(
        statement = "the tie prestrain load is ORTHOGONAL to the demanded common-mode roll at " +
                "every one of the " + armed.turnElements.size + " raster turns",
        quantity = "the largest |L . c_k| over the ties, in pN*nm",
        value = worstProjection,
        threshold = 0.0,
        holds = worstProjection == 0.0 && loadIsNonZero,
        note = "an equal-and-opposite couple pair is the work conjugate of Phi_upper - " +
                "Phi_lower, and a common-mode azimuth has coefficient exactly zero on it. The " +
                "load is not vacuously zero: " + loadIsNonZero
    )
    val challengeStands = channel.all { it.holds }
    channel.forEach { println("  " + it.quantity + " = " + it.value.emitted(6) + "  holds " + it.holds) }

    // ==================================== Deliverable 2 -- the replacement, and its sign
    println("T-291 - the per-beam twist the alternation demands")
    val twistRows = ArrayList<T291TwistRow>()
    listOf(
        "102 / 109 (C-0151, drawable)" to (T291_RECOMMENDED_ONE to T291_RECOMMENDED_TWO),
        "112 / 108 (C-0140, undrawable)" to (T291_UNDRAWABLE_ONE to T291_UNDRAWABLE_TWO)
    ).forEach { (label, pair) ->
        val signs = HoneycombRasterTurnSigns(shared.block, pair.first, pair.second)
        val determined = signs.classZeroResidueCandidates.size == 1
        val demands = if (determined) honeycombBeamTwistDemands(signs, phase = 1) else emptyList()
        val carrying = demands.filter { it.twistDegrees != 0.0 }
        twistRows += T291TwistRow(
            raster = label,
            determined = determined,
            beams = shared.block.helices,
            beamsCarryingTwist = carrying.size,
            beamsFree = demands.size - carrying.size,
            distinctTwistDegrees = carrying.map { it.twistDegrees }.distinct().sorted(),
            twistDegreesAtPhasePlus = carrying.firstOrNull()?.twistDegrees,
            note = if (determined) {
                "every interior helix carries a turn at EACH rim, and the derived departures " +
                        "alternate, so the demand is one uniform twist; the two raster termini " +
                        "carry a single-ended roll demand, which is a rigid roll and free"
            } else {
                "the raster does not close, so no departure is determined and no twist follows"
            }
        )
    }
    twistRows.forEach {
        println("  " + it.raster + " -> " + it.beamsCarryingTwist + " beams at " +
                (it.twistDegreesAtPhasePlus?.emitted(9) ?: "none") + " deg")
    }

    val conventionSweep = ArrayList<T291ConventionRow>()
    listOf(1, -1).forEach { first ->
        listOf(false, true).forEach { mirrored ->
            listOf(false, true).forEach { reversed ->
                val signs = HoneycombRasterTurnSigns(
                    shared.block, T291_RECOMMENDED_ONE, T291_RECOMMENDED_TWO,
                    firstAxialSign = first, mirrored = mirrored, axialReversed = reversed
                )
                val carrying = honeycombBeamTwistDemands(signs, phase = 1)
                    .filter { it.twistDegrees != 0.0 }
                conventionSweep += T291ConventionRow(
                    firstAxialSign = first,
                    mirrored = mirrored,
                    axialReversed = reversed,
                    beamsCarryingTwist = carrying.size,
                    twistMagnitudeDegrees = abs(carrying.first().twistDegrees),
                    uniformInSign = carrying.all { it.twistDegrees == carrying.first().twistDegrees }
                )
            }
        }
    }
    val twistInvariant = conventionSweep.all {
        it.beamsCarryingTwist == 58 && it.uniformInSign &&
                abs(it.twistMagnitudeDegrees - demanded) < 1e-12
    }

    // ==================================== Deliverable 3 -- the cheap bound
    println("T-291 - the cheap bound, before any lattice is assembled")
    val energy = ArrayList<T291EnergyRow>()
    listOf(
        "the 102 bp row" to T291_RECOMMENDED_ONE,
        "the 109 bp row" to T291_RECOMMENDED_TWO,
        "the 116 bp block extent, which is the model's own beam" to T291_BLOCK_EXTENT_BP
    ).forEach { (label, bp) ->
        val length = bp * Gen1Tile.RISE_PER_BASE_PAIR
        val perBeam = beamTwistRestrainedEnergy(gj, length, Math.toRadians(demanded))
        energy += T291EnergyRow(
            over = label,
            beamLength = length,
            torsionalStiffness = gj / length,
            restrainedEnergyPerBeam = perBeam,
            restrainedEnergyPerBeamKt = perBeam / kt,
            restrainedEnergyPerBlockKt = 58.0 * perBeam / kt,
            overOneHostSheetColumn = 58.0 * perBeam / kt / T291_HOST_COLUMN_KT
        )
    }
    energy.forEach {
        println("  " + it.over + ": GJ/L = " + it.torsionalStiffness.emitted(9) +
                " pN*nm/rad, block " + it.restrainedEnergyPerBlockKt.emitted(9) + " kT")
    }
    val relativeRollCharge = 59.0 * 0.5 * Gen1Tile.crossoverHingeStiffness() *
            Math.toRadians(allowed) * Math.toRadians(allowed) / kt

    val cheapBound = listOf(
        T291CheapBoundRow(
            question = "does CH-0240 stand?",
            answer = if (challengeStands) "YES, on three checks and no solve" else "NO",
            consequence = "the relative azimuth is level-free, the relative channel cannot " +
                    "relieve the departure at all (u* = " + stationary.emitted(9) + " > 1), and " +
                    "the applied load is exactly orthogonal to the demanded kinematics at all " +
                    armed.turnElements.size + " ties"
        ),
        T291CheapBoundRow(
            question = "what does the alternation demand instead?",
            answer = demanded.emitted(9) + " degrees of TWIST on each of 58 interior helices",
            consequence = "to the digit the departure C-0152 prices a FORCED crossover at, " +
                    "reached from the other side; the two raster termini carry none"
        ),
        T291CheapBoundRow(
            question = "what does that channel cost, before any lattice is assembled?",
            answer = energy.first().restrainedEnergyPerBlockKt.emitted(9) + " kT over the block " +
                    "at the 102 bp row and " + energy[1].restrainedEnergyPerBlockKt.emitted(9) +
                    " kT at the 109 bp one, as a rigid-duplex CEILING",
            consequence = "against C-0079's measured " + T291_HOST_COLUMN_KT.emitted(9) +
                    " kT per crossover column of the host sheet, which folds: " +
                    energy.first().overOneHostSheetColumn.emitted(9) + " of one column. The " +
                    "relative-roll charge the corpus applies is " +
                    relativeRollCharge.emitted(9) + " kT over 59 ties, i.e. " +
                    (energy.first().restrainedEnergyPerBlockKt / relativeRollCharge).emitted(9) +
                    "x smaller than the channel that actually carries the demand"
        ),
        T291CheapBoundRow(
            question = "and is GJ/L comparable with k_theta at all?",
            answer = "GJ over the 102 bp row is " + energy.first().torsionalStiffness.emitted(9) +
                    " pN*nm/rad against k_theta = " +
                    Gen1Tile.crossoverHingeStiffness().emitted(9),
            consequence = "the duplex's own torsional compliance over one row and the " +
                    "crossover's dihedral spring agree to " +
                    (
                        100.0 * abs(
                            energy.first().torsionalStiffness -
                                    Gen1Tile.crossoverHingeStiffness()
                        ) / Gen1Tile.crossoverHingeStiffness()
                        ).emitted(3) +
                    " %, so the two channels are priced alike PER SITE and the whole factor " +
                    "between them is that the twist is 2 delta and the energy is quadratic"
        )
    )

    // ==================================== Deliverable 4 -- the free tile and the ceiling
    println("T-291 - the free tile, and the triangle-inequality ceiling on 60 unit responses")
    val columns = fractions.associateWith { T291Column(shared, it, shared.enhancementAt(it)) }
    val bare = T291Column(shared, null, 1.0)
    val freeColumns = listOf<Pair<Double?, T291Column>>(
        0.30 to columns.getValue(0.30), 0.26 to columns.getValue(0.26), null to bare
    )
    val freeTile = ArrayList<T291FreeTileRow>()
    val ceiling = ArrayList<T291CeilingRow>()
    val relaxation = ArrayList<T291RelaxationRow>()
    freeColumns.forEach { (f, column) ->
        val none = column.freeField(0)
        listOf(
            0 to "no eigenstrain",
            1 to "the derived twist, phase +1",
            -1 to "the derived twist, phase -1"
        ).forEach { (phase, label) ->
            val peak = column.freeField(phase)
            freeTile += T291FreeTileRow(
                hingeStiffnessEnhancement = column.enhancement,
                compositeFraction = f,
                term = label,
                peakDishingOverStroke = peak,
                insideTolerance = peak < T291_TOLERANCE,
                movementFromNoTerm = peak - none
            )
        }
        // the ceiling: unit twist responses over every beam, then a triangle inequality
        val unit = (0 until column.structure.beamCount).map { beam ->
            column.structure.beamTwistResponse(mapOf(beam to 1.0))
                .peakDishing(T291_SAMPLES) / column.freeStroke
        }
        val sorted = unit.sorted()
        val twists = honeycombBeamTwists(shared.signs, phase = 1)
        val ceilingValue = twists.entries.sumOf { (beam, twist) -> abs(twist) * unit[beam] }
        val realised = column.twistResponse(1).peakDishing(T291_SAMPLES) / column.freeStroke
        ceiling += T291CeilingRow(
            hingeStiffnessEnhancement = column.enhancement,
            compositeFraction = f,
            largestSingleBeamPerRadian = sorted.last(),
            medianSingleBeamPerRadian = sorted[sorted.size / 2],
            smallestSingleBeamPerRadian = sorted.first(),
            sumOverBeamsPerRadian = unit.sum(),
            triangleCeilingAtTheDemand = ceilingValue,
            realisedAtPhasePlus = realised,
            realisedOverCeiling = realised / ceilingValue,
            ceilingIsRespected = realised <= ceilingValue * (1.0 + T291_IDENTITY)
        )
        // the relaxation: E = E0 - (1/2) L.u, exactly, and E0 is the cheap bound
        val response = column.twistResponse(1)
        val span = column.structure.nodeS.last() - column.structure.nodeS.first()
        val restrained = twists.values.sumOf {
            beamTwistRestrainedEnergy(gj, span, it)
        }
        val work = t291LoadWork(column.structure, twists, response)
        relaxation += T291RelaxationRow(
            compositeFraction = f,
            restrainedEnergy = restrained,
            loadWork = work,
            relaxedEnergy = restrained - 0.5 * work,
            relaxedOverRestrained = (restrained - 0.5 * work) / restrained,
            ceilingIsRespected = work >= 0.0 && restrained - 0.5 * work <= restrained
        )
    }
    freeTile.forEach {
        println("  f = " + (it.compositeFraction?.emitted(3) ?: "none") + "  " + it.term +
                " -> " + it.peakDishingOverStroke.emitted(9))
    }
    ceiling.forEach {
        println("  ceiling f = " + (it.compositeFraction?.emitted(3) ?: "none") + " -> " +
                it.triangleCeilingAtTheDemand.emitted(9) + ", realised " +
                it.realisedAtPhasePlus.emitted(9))
    }

    // ==================================== Deliverable 5 -- the 64 coupled cells at both signs
    println("T-291 - the grade, " + t291Realisations + " realisations on C-0167's own stream")
    val probe = honeycombTiedLattice(
        shared.block, shared.rowBasePairs, shared.enhancementAt(0.30), tied = false
    )
    val rootingHelixY = probe.faceBeams.map { probe.beamY[it] }
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
    val cells = ArrayList<T291Cell>()
    val ensembles = HashMap<String, DropoutEnsemble>()
    gradedColumns.forEach { columnCount ->
        t291Placements(shared, rootingHelixY, columnCount).forEach { (placement, grid) ->
            val key = placement + "|" + columnCount
            val ensemble = ensembles.getOrPut(key) {
                dropoutEnsemble(
                    grid.map { (x, y) -> incorporation.at(x, y) }, t291Realisations, T291_SEED
                )
            }
            t291Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, stiffnesses) ->
                fractions.forEach { fraction ->
                    val column = columns.getValue(fraction)
                    val zeroSurrogate = column.surrogate(key, grid, 0)
                    val zeroSample = dropoutDishingSample(zeroSurrogate, stiffnesses, ensemble)
                    zeroSample.indices.forEach {
                        zeroSample[it] = zeroSample[it] / column.freeStroke
                    }
                    val zeroP90 = summariseDropoutDishing(
                        zeroSample,
                        zeroSurrogate.solve(stiffnesses).peakDishing / column.freeStroke,
                        ensemble.meanSurvivors, T291_TOLERANCE
                    ).p90
                    listOf(1, -1).forEach { phase ->
                        val surrogate = column.surrogate(key, grid, phase)
                        val nominal = surrogate.solve(stiffnesses).peakDishing / column.freeStroke
                        val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                        sample.indices.forEach { sample[it] = sample[it] / column.freeStroke }
                        val summary = summariseDropoutDishing(
                            sample, nominal, ensemble.meanSurvivors, T291_TOLERANCE
                        )
                        cells += T291Cell(
                            phase = phase,
                            term = "the derived twist, phase " + (if (phase > 0) "+1" else "-1"),
                            compositeFraction = fraction,
                            placement = placement,
                            columns = columnCount,
                            pathCount = grid.size,
                            distribution = label,
                            nominalOverStroke = nominal,
                            p90OverStroke = summary.p90,
                            flatAtNominal = nominal < T291_TOLERANCE,
                            flatAtP90 = summary.flatAtP90,
                            zeroTermP90OverStroke = zeroP90,
                            movementFromZeroTerm = summary.p90 - zeroP90
                        )
                    }
                }
            }
        }
    }

    val signContingency = cells.filter { it.phase == 1 }.map { plus ->
        val minus = cells.first {
            it.phase == -1 && it.compositeFraction == plus.compositeFraction &&
                    it.placement == plus.placement && it.columns == plus.columns &&
                    it.distribution == plus.distribution
        }
        T291SignRow(
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
            flatAtTheWorsePhase = maxOf(plus.p90OverStroke, minus.p90OverStroke) <
                    T291_TOLERANCE
        )
    }
    signContingency.filter { it.flatAtPhasePlus || it.flatAtPhaseMinus }.forEach {
        println("  f=" + it.compositeFraction.emitted(3) + "  " + it.placement + "  " +
                it.columns + " col, " + it.pathCount + " paths, " + it.distribution +
                "  +1 " + it.p90AtPhasePlus.emitted(9) + "  -1 " + it.p90AtPhaseMinus.emitted(9) +
                (if (it.flatAtTheWorsePhase) "  FLAT AT BOTH" else "  sign-contingent"))
    }
    val flatAtBoth = signContingency.count { it.flatAtTheWorsePhase }
    val flatAtPlus = signContingency.count { it.flatAtPhasePlus }
    val flatAtMinus = signContingency.count { it.flatAtPhaseMinus }
    val contingent = signContingency.count { it.verdictDependsOnThePhase }
    val worstMovement = cells.maxOf { abs(it.movementFromZeroTerm) }
    val medianMovement = cells.map { abs(it.movementFromZeroTerm) }.sorted()
        .let { (it[it.size / 2 - 1] + it[it.size / 2]) / 2.0 }

    // ==================================== the channel comparison, cell by cell
    println("T-291 - the two channels, cell by cell")
    val channelComparison = signContingency.map { row ->
        val plus = t291PublishedCell(
            t284, row.compositeFraction, row.placement, row.columns, row.distribution, 1,
            "p90OverStroke"
        )
        val minus = t291PublishedCell(
            t284, row.compositeFraction, row.placement, row.columns, row.distribution, -1,
            "p90OverStroke"
        )
        val zero = t291PublishedCell(
            t284, row.compositeFraction, row.placement, row.columns, row.distribution, 1,
            "zeroPrestrainP90OverStroke"
        )
        val rollWorst = maxOf(plus, minus)
        val twistMove = maxOf(abs(row.p90AtPhasePlus - zero), abs(row.p90AtPhaseMinus - zero))
        val rollMove = maxOf(abs(plus - zero), abs(minus - zero))
        T291ChannelCell(
            compositeFraction = row.compositeFraction,
            placement = row.placement,
            columns = row.columns,
            pathCount = row.pathCount,
            distribution = row.distribution,
            zeroTermP90OverStroke = zero,
            twistWorstOverStroke = row.worstOfTheTwo,
            relativeRollWorstOverStroke = rollWorst,
            twistMovement = twistMove,
            relativeRollMovement = rollMove,
            twistOverRelativeRoll = if (rollMove == 0.0) 0.0 else twistMove / rollMove,
            flatAtTheWorseTwistSign = row.flatAtTheWorsePhase,
            flatAtTheWorseRelativeRollPhase = rollWorst < T291_TOLERANCE,
            theTwoChannelsAgree = row.flatAtTheWorsePhase == (rollWorst < T291_TOLERANCE)
        )
    }
    val channelsDisagree = channelComparison.count { !it.theTwoChannelsAgree }
    val worstRatio = channelComparison.maxOf { it.twistOverRelativeRoll }
    val medianRatio = channelComparison.map { it.twistOverRelativeRoll }.sorted()
        .let { (it[it.size / 2 - 1] + it[it.size / 2]) / 2.0 }

    // ==================================== the threshold the unmodelled tie stiffness must beat
    println("T-291 - the eigenstrain scaled, which turns a missing stiffness into a threshold")
    val scaleRows = ArrayList<T291ScaleRow>()
    val impliedFull = 2.0 * gj / (columns.getValue(0.30).structure.nodeS.let {
        it.last() - it.first()
    })
    val recoveredCells = listOf(
        Triple(0.30, "abstract grid", 3),
        Triple(0.30, "abstract grid on the rooting helices", 5)
    )
    val fractionLadder = listOf(0.0, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.25, 0.5, 1.0)
    recoveredCells.forEach { (f, placement, cols) ->
        val grid = t291Placements(shared, rootingHelixY, cols).first { it.first == placement }.second
        val key = placement + "|" + cols
        val stiffnesses = t291Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == "rim-graded 5:1" }.second
        val ensemble = ensembles.getValue(key)
        val column = columns.getValue(f)
        fractionLadder.forEach { fraction ->
            val readings = listOf(1, -1).map { phase ->
                val surrogate = column.scaledSurrogate(key, grid, phase, fraction)
                val nominal = surrogate.solve(stiffnesses).peakDishing / column.freeStroke
                val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / column.freeStroke }
                summariseDropoutDishing(
                    sample, nominal, ensemble.meanSurvivors, T291_TOLERANCE
                ).p90
            }
            val worst = maxOf(readings[0], readings[1])
            scaleRows += T291ScaleRow(
                fractionOfTheDerivedEigenstrain = fraction,
                impliedCommonModeStiffness = fraction * impliedFull,
                compositeFraction = f,
                placement = placement,
                columns = cols,
                distribution = "rim-graded 5:1",
                p90AtPhasePlus = readings[0],
                p90AtPhaseMinus = readings[1],
                worstOfTheTwo = worst,
                flatAtBothSigns = worst < T291_TOLERANCE
            )
        }
    }
    val perCellFlatFraction = recoveredCells.map { (f, placement, cols) ->
        val ladder = scaleRows.filter {
            it.compositeFraction == f && it.placement == placement && it.columns == cols
        }
        val flat = ladder.filter { it.flatAtBothSigns }
            .maxOfOrNull { it.fractionOfTheDerivedEigenstrain } ?: 0.0
        val firstOut = ladder.filter { !it.flatAtBothSigns }
            .minOfOrNull { it.fractionOfTheDerivedEigenstrain }
        Triple(placement + ", " + cols + " col", flat, firstOut)
    }
    val bothCellsFlatFraction = perCellFlatFraction.minOf { it.second }
    val perCellLadder = perCellFlatFraction.joinToString("; ") {
        it.first + " survives to " + it.second.emitted(9) +
                (it.third?.let { out -> " and is out at " + out.emitted(9) } ?: "")
    }
    scaleRows.forEach {
        println("  scale " + it.fractionOfTheDerivedEigenstrain.emitted(3) + "  " +
                it.placement + " " + it.columns + " col -> worst " +
                it.worstOfTheTwo.emitted(9) + (if (it.flatAtBothSigns) "  flat" else ""))
    }

    // ==================================== convergence, at the cells the verdict rests on
    println("T-291 - convergence, taken at the cells the verdict rests on")
    val convergence = ArrayList<T291ConvergenceRow>()
    val recovered = listOf(
        Triple(0.30, "abstract grid", 3),
        Triple(0.30, "abstract grid on the rooting helices", 5)
    ).map { (f, placement, cols) ->
        signContingency.first {
            it.compositeFraction == f && it.placement == placement && it.columns == cols &&
                    it.distribution == "rim-graded 5:1"
        }
    }
    val deciding = (recovered + signContingency
        .filter { it.flatAtPhasePlus || it.flatAtPhaseMinus }
        .sortedBy { it.worstOfTheTwo }
        .take(2)).distinct()
    val fine = HashMap<Double, T291Column>()
    deciding.forEach { row ->
        val columnCount = row.columns
        val grid = t291Placements(shared, rootingHelixY, columnCount)
            .first { it.first == row.placement }.second
        val key = row.placement + "|" + columnCount
        val stiffnesses = t291Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == row.distribution }.second
        val ensemble = ensembles.getValue(key)
        val column = columns.getValue(row.compositeFraction)
        val refined = fine.getOrPut(row.compositeFraction) {
            T291Column(shared, row.compositeFraction, column.enhancement, subdivisions = 2)
        }
        val name = row.placement + ", " + columnCount + " col, " + row.pathCount + " paths, " +
                row.distribution + ", f = " + row.compositeFraction.emitted(3)
        listOf(1, -1).forEach { phase ->
            fun p90(target: T291Column, samples: Int): Double {
                val surrogate = target.surrogate(key, grid, phase, samples)
                val nominal = surrogate.solve(stiffnesses).peakDishing / target.freeStroke
                val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / target.freeStroke }
                return summariseDropoutDishing(
                    sample, nominal, ensemble.meanSurvivors, T291_TOLERANCE
                ).p90
            }
            val coarse = p90(column, T291_SAMPLES)
            val subdivided = p90(refined, T291_SAMPLES)
            convergence += T291ConvergenceRow(
                cell = name + ", phase " + (if (phase > 0) "+1" else "-1"),
                quantity = "the ensemble's p90 over the stroke",
                axis = "beam subdivisions 1 -> 2",
                coarse = coarse,
                fine = subdivided,
                departure = abs(subdivided - coarse),
                verdictSurvives = (coarse < T291_TOLERANCE) == (subdivided < T291_TOLERANCE)
            )
            val dense = p90(column, 161)
            convergence += T291ConvergenceRow(
                cell = name + ", phase " + (if (phase > 0) "+1" else "-1"),
                quantity = "the ensemble's p90 over the stroke",
                axis = "dishing sample grid 81 -> 161",
                coarse = coarse,
                fine = dense,
                departure = abs(dense - coarse),
                verdictSurvives = (coarse < T291_TOLERANCE) == (dense < T291_TOLERANCE)
            )
        }
    }
    convergence.forEach {
        println("  " + it.axis + "  " + it.cell + "  departure " + it.departure.emitted(2))
    }
    val convergenceHolds = convergence.all { it.verdictSurvives }

    // ==================================== the reproductions
    println("T-291 - the reproductions")
    val reproductions = ArrayList<T291ReproductionRow>()
    listOf(0.30 to 21.1851817, 0.26 to 18.4938242).forEach { (f, published) ->
        // the tied lattice at NO eigenstrain is the object this study and C-0187 share exactly,
        // and it is the only free-tile number both can be right about
        val here = columns.getValue(f).freeField(0)
        val there = t291PublishedZeroField(t284, published)
        reproductions += T291ReproductionRow(
            what = "the tied free tile at f = " + f.emitted(3) + ", no eigenstrain (C-0187)",
            published = there,
            here = here,
            relativeDeparture = departure(there, here),
            closes = departure(there, here) < 1e-6
        )
        // C-0175 section 8's own relative-roll reading of the same tile, carried unchanged as
        // the term this study replaces rather than reproduced -- it is a different load
        val roll = t291PublishedField(t254, published, "by the rim the turn sits at")
        reproductions += T291ReproductionRow(
            what = "C-0175 section 8's relative-roll free tile at f = " + f.emitted(3) +
                    ", carried unchanged as the term this study replaces",
            published = roll,
            here = roll,
            relativeDeparture = 0.0,
            closes = true
        )
    }
    listOf(
        Triple(0.30, "abstract grid", 3),
        Triple(0.30, "abstract grid on the rooting helices", 5)
    ).forEach { (f, placement, cols) ->
        val here = signContingency.first {
            it.compositeFraction == f && it.placement == placement && it.columns == cols &&
                    it.distribution == "rim-graded 5:1"
        }
        val there = t291PublishedCell(t284, f, placement, cols, "rim-graded 5:1", 1,
            "zeroPrestrainP90OverStroke")
        val mine = cells.first {
            it.compositeFraction == f && it.placement == placement && it.columns == cols &&
                    it.distribution == "rim-graded 5:1" && it.phase == 1
        }.zeroTermP90OverStroke
        reproductions += T291ReproductionRow(
            what = "C-0180's recovered cell (" + placement + ", " + cols +
                    " col) at zero prestrain",
            published = there,
            here = mine,
            relativeDeparture = departure(there, mine),
            closes = departure(there, mine) < 1e-6
        )
        check(here.columns == cols)
    }
    val reproductionsClose = reproductions.all { it.closes }
    reproductions.forEach {
        println("  " + it.what + " -> " + it.relativeDeparture.emitted(2))
    }

    // ==================================== the falsifiers
    val f1 = worstRelative >= 1e-12
    val f2 = !(worstProjection == 0.0 && loadIsNonZero)
    val f3 = stationary <= 1.0
    val f4 = !twistInvariant
    val f5 = false
    val f6 = false
    val f7 = false
    val f8 = ceiling.any { !it.ceilingIsRespected }
    val f9 = contingent == 0
    val f10 = channelComparison.all { abs(it.twistOverRelativeRoll - 1.0) < 0.01 }
    val f11 = !convergenceHolds

    val falsifiers = listOf(
        T291FalsifierRow(
            "F1",
            "the two backbones are NOT antipodal at every level displacement",
            f1,
            "the largest departure from 180 degrees over the whole period is " +
                    worstRelative.emitted(2) + " degrees"
        ),
        T291FalsifierRow(
            "F2",
            "the tie prestrain load has a nonzero projection on the demanded common-mode roll",
            f2,
            "exactly " + worstProjection.emitted(2) + " at all " + armed.turnElements.size +
                    " ties, on a load that is not itself zero"
        ),
        T291FalsifierRow(
            "F3",
            "a pure relative roll reaches the span the common-mode relief reaches",
            f3,
            "u* = " + stationary.emitted(9) + " > 1, so the relative channel's own minimum is " +
                    "the BUILT state and it leaves " + shortfall.emitted(9) + " nm on the table"
        ),
        T291FalsifierRow(
            "F4",
            "the derived per-beam twist is not " + demanded.emitted(9) +
                    " degrees, or not the same sign on every interior beam, or not invariant " +
                    "over the eight readings of the free conventions",
            f4,
            "58 beams at " + demanded.emitted(9) + " degrees and 2 free, at all eight readings"
        ),
        T291FalsifierRow(
            "F5",
            "the twist eigenstrain's response is not exactly linear in the eigenstrain",
            f5,
            "asserted in RasterTurnTwistEigenstrainTest at 1e-9 over a sampled face"
        ),
        T291FalsifierRow(
            "F6",
            "a uniform twist over every beam does not relax into the twisted ribbon " +
                    "W = y theta s / L as the foundation vanishes",
            f6,
            "asserted in RasterTurnTwistEigenstrainTest over three decades of foundation, " +
                    "monotone, and it is what fixes the response's sign against the eigenstrain's"
        ),
        T291FalsifierRow(
            "F7",
            "a uniform pressure on the tied, zero-eigenstrain lattice dishes anything but zero",
            f7,
            "asserted in RasterTurnTwistEigenstrainTest; its eigenstrain sibling is asserted the " +
                    "other way, because a uniform TWIST relaxes into a saddle and not a rigid mode"
        ),
        T291FalsifierRow(
            "F8",
            "the twist term's peak dishing exceeds the triangle-inequality ceiling on its own " +
                    "60 unit responses",
            f8,
            "realised over ceiling " +
                    ceiling.joinToString(", ") { it.realisedOverCeiling.emitted(9) }
        ),
        T291FalsifierRow(
            "F9",
            "DECLARED OPEN - the coupled flat census is the same at both signs of the eigenstrain",
            f9,
            flatAtBoth.toString() + " of 64 flat at BOTH signs, " + flatAtPlus + " at +1, " +
                    flatAtMinus + " at -1, " + contingent + " sign-contingent"
        ),
        T291FalsifierRow(
            "F10",
            "DECLARED OPEN - the twist term and the relative-roll term move a cell's p90 by the " +
                    "same amount, i.e. the channel substitution is immaterial",
            f10,
            "the twist moves a cell " + medianRatio.emitted(9) + "x the relative roll at the " +
                    "median and " + worstRatio.emitted(9) + "x at the worst; the two channels " +
                    "disagree about the verdict at " + channelsDisagree + " of 64 cells"
        ),
        T291FalsifierRow(
            "F11",
            "a convergence step moves a verdict at any deciding cell",
            f11,
            convergence.count { it.verdictSurvives }.toString() + " of " + convergence.size +
                    " steps leave the verdict standing"
        )
    )

    val verdict = linkedMapOf(
        "CH-0240" to (if (challengeStands) "UPHELD" else "NOT UPHELD") +
                " - on three checks and no solve: the relative azimuth is level-free, the " +
                "relative channel cannot relieve a common-mode departure at all, and the " +
                "applied load is exactly orthogonal to the demanded kinematics at every tie",
        "whatIsWithdrawn" to "the COORDINATE, and no number: CH-0228's, C-0175 section 8's and " +
                "C-0180 section 4's readings are all correct about the object they were taken " +
                "on, and that object is not the load the lattice demands",
        "theReplacement" to demanded.emitted(9) + " degrees of twist on each of 58 interior " +
                "helices, uniform in sign, at all eight readings of the free conventions",
        "theSign" to "DERIVED up to ONE global phase, exactly as C-0187's roll was, and for a " +
                "stated reason: the grillage carries no handedness at all - neither s against " +
                "the raster's own axial datum nor Phi's rotational sense against B-DNA's - so " +
                "no constant of the model maps an azimuthal sense onto Phi. Both signs graded, " +
                "the verdict quoted at the worse",
        "flatAtBothSigns" to flatAtBoth.toString() + " of 64",
        "flatAtPhasePlus" to flatAtPlus.toString() + " of 64",
        "flatAtPhaseMinus" to flatAtMinus.toString() + " of 64",
        "signContingentCells" to contingent.toString() + " of 64",
        "worstMovementOverTheStroke" to worstMovement.emitted(9),
        "medianMovementOverTheStroke" to medianMovement.emitted(9),
        "theMagnitudeIsNotSettled" to "the load's SHAPE is derived exactly and its MAGNITUDE " +
                "scales with the tie's COMMON-MODE stiffness, which this lattice does not " +
                "carry at all (CH-0242). Modelling the demand as a per-beam torsional " +
                "eigenstrain against the duplex's own GJ is the model's natural choice and is " +
                "what is priced here; it corresponds to a common-mode stiffness of " +
                impliedFull.emitted(9) + " pN*nm/rad",
        "theFractionEachRecoveredCellSurvives" to perCellLadder,
        "impliedCommonModeCeiling" to "the tighter cell survives an eigenstrain of " +
                perCellFlatFraction.maxOf { it.second }.emitted(9) + " of the derived one, i.e. " +
                "a common-mode tie stiffness of " +
                (perCellFlatFraction.maxOf { it.second } * impliedFull).emitted(9) +
                " pN*nm/rad, which is " +
                (
                    100.0 * perCellFlatFraction.maxOf { it.second } * impliedFull /
                            Gen1Tile.crossoverHingeStiffness()
                    ).emitted(3) +
                " % of k_theta on the relative coordinate -- and CH-0242 says the physical " +
                "common-mode stiffness is 3.52810239x k_theta rather than a hundredth of it"
    )

    val findings = listOf(
        "CH-0240 is UPHELD, on the lattice and on the model's own source, with no solve: the " +
                "two backbones of a honeycomb scaffold crossover are antipodal at every level " +
                "displacement (worst departure from 180 degrees over the whole period, both " +
                "signs: " + worstRelative.emitted(2) + "), and the tie prestrain load's " +
                "projection on the demanded common-mode roll is EXACTLY " +
                worstProjection.emitted(2) + " at all " + armed.turnElements.size + " ties.",
        "And the relative channel does not merely relieve the departure less well -- it does " +
                "not relieve it AT ALL. The span's stationary point in the relative roll is " +
                "u* = d cos(delta) / (2 r_P) = " + stationary.emitted(9) + ", outside the " +
                "reachable cos r, so the minimum over the whole channel is at r = 0, the built " +
                "state itself, leaving " + shortfall.emitted(9) + " nm above the floor. " +
                "CH-0240's own section 4 says a relative roll reduces the span 'just less " +
                "efficiently'; at this geometry it increases it, which strengthens the " +
                "challenge and withdraws that clause.",
        "What replaces it is " + demanded.emitted(9) + " degrees of TWIST on each of the 58 " +
                "interior helices -- UNIFORM in sign, where the roll assignment alternates -- " +
                "and the two raster termini carry none, because a single-ended roll demand is a " +
                "rigid roll. Invariant at all eight readings of firstAxialSign, mirrored and " +
                "axialReversed.",
        "The sign is derived to the same depth C-0187 reached and no further: 2^58 assignments " +
                "collapse to ONE global phase, and that phase is not derivable because the " +
                "grillage carries no handedness. Both signs are graded and the verdict is " +
                "quoted at the worse.",
        "The cheap bound, before any lattice: the rigid-duplex ceiling is " +
                energy.first().restrainedEnergyPerBeamKt.emitted(9) + " kT per beam at the 102 " +
                "bp row and " + energy.first().restrainedEnergyPerBlockKt.emitted(9) +
                " kT over the block, which is " +
                energy.first().overOneHostSheetColumn.emitted(9) + " of ONE crossover column of " +
                "the host sheet C-0079 measures at " + T291_HOST_COLUMN_KT.emitted(9) +
                " kT -- and the host sheet folds.",
        "GJ over one 102 bp row is " + energy.first().torsionalStiffness.emitted(9) +
                " pN*nm/rad against k_theta = " + Gen1Tile.crossoverHingeStiffness().emitted(9) +
                ", so the twist channel and the roll channel are priced ALIKE PER SITE and the " +
                "whole factor between the two block totals is that the twist is 2 delta and the " +
                "energy is quadratic: " +
                (energy.first().restrainedEnergyPerBlockKt / relativeRollCharge).emitted(9) +
                "x, against a relative-roll charge of " + relativeRollCharge.emitted(9) + " kT.",
        "The realised energy is far below the ceiling, and the reason is geometric: the state " +
                "that relaxes every hinge, every link and every beam torsion at once is a " +
                "twisted RIBBON, W = y theta s / L, which costs nothing except against the " +
                "FOUNDATION. Measured, " +
                relaxation.joinToString("; ") {
                    "f = " + (it.compositeFraction?.emitted(3) ?: "none") + " relaxes to " +
                            it.relaxedOverRestrained.emitted(9) + " of the restrained ceiling"
                } + ".",
        "On the free tile the twist term is worth " +
                freeTile.filter { it.term != "no eigenstrain" }
                    .joinToString("; ") {
                        "f = " + (it.compositeFraction?.emitted(3) ?: "none") + " " +
                                it.term + " -> " + it.peakDishingOverStroke.emitted(9)
                    } + ".",
        "Graded on C-0167's 64 coupled cells at both signs: " + flatAtPlus + " of 64 flat at " +
                "phase +1, " + flatAtMinus + " at phase -1, " + flatAtBoth + " at BOTH, " +
                contingent + " sign-contingent. The worst |movement| from the zero-eigenstrain " +
                "cell over the 128 prestrained cells is " + worstMovement.emitted(9) +
                " of the stroke and the median " + medianMovement.emitted(9) + ".",
        "The two channels are not interchangeable and the difference is measured cell by cell: " +
                "the twist moves a cell " + medianRatio.emitted(9) + "x what the relative roll " +
                "moves it at the median and " + worstRatio.emitted(9) + "x at the worst, and " +
                "they disagree about the flat verdict at " + channelsDisagree + " of 64 cells.",
        "The MAGNITUDE is not settled and is reported as a THRESHOLD instead. The load's shape " +
                "is derived exactly; what scales it is the tie's own COMMON-MODE stiffness, " +
                "which the lattice does not carry at all. Modelling the demand as a per-beam " +
                "eigenstrain against the duplex's GJ is the model's natural choice and " +
                "corresponds to " + impliedFull.emitted(9) + " pN*nm/rad. Swept over ten " +
                "fractions of it: " + perCellLadder + ". So the coupled recovery survives only " +
                "a common-mode tie stiffness under " +
                (perCellFlatFraction.maxOf { it.second } * impliedFull).emitted(9) +
                " pN*nm/rad -- " +
                (
                    100.0 * perCellFlatFraction.maxOf { it.second } * impliedFull /
                            Gen1Tile.crossoverHingeStiffness()
                    ).emitted(3) +
                " % of k_theta -- where CH-0242 puts the physical one at 3.52810239x k_theta. " +
                "No nonzero rung of the ladder keeps BOTH cells flat.",
        "Convergence: " + convergence.count { it.verdictSurvives } + " of " + convergence.size +
                " steps leave the verdict standing. Reproductions close: " + reproductionsClose +
                "."
    )
    findings.forEach { println("  " + it) }

    val result = T291Result(
        task = "T-291",
        leaf = "A8.2",
        title = "The allowed departure is COMMON-MODE, so the corpus's load sits in a channel " +
                "whose eigenstrain is exactly zero -- and what replaces it is a per-beam TWIST " +
                "of " + demanded.emitted(9) + " degrees on every interior helix",
        verificationType = "logical (two lines of algebra on the challenged claims' own azimuth " +
                "convention and a reading of the model file all of them consume, settled with " +
                "no solver at all) + in-silico (the replacement eigenstrain assembled on the " +
                "same three-dimensional beam-and-bond lattice and graded through the same exact " +
                "Woodbury coupling surrogate, the same C-0087-measured incorporation as a " +
                "Bernoulli dropout over " + t291Realisations + " realisations on one common " +
                "stream restricted per cell, and the same T-5b convention C-0180 and C-0187 used)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "departure's magnitude is C-0152's rigid-duplex reading of caDNAno's own rule; " +
                "GJ is Gen1Tile's DUPLEX_TORSIONAL_RIGIDITY, which is CanDo's model input and " +
                "not a measurement; the tie's axial station is s = +-L/2 exactly, where a " +
                "scaffold crossover sits 5 bp from a staple position.",
        units = linkedMapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm, and k_BT = " + kt.emitted(9) + " pN*nm at 300 K",
            "angle" to "degrees at every API, radians only where a lattice is loaded",
            "torsionalRigidity" to "pN*nm^2",
            "rotationalStiffness" to "pN*nm/rad",
            "dishing" to "dimensionless, as a fraction of the closed-form free stroke"
        ),
        conventions = linkedMapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "z" to "along the block's thickness",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "Phi" to "the roll about a beam's OWN axis, right-handed about +s in the (s, y, W) " +
                    "frame -- which is HoneycombGrillage's own link gradient [1, armY, -1, " +
                    "armY] read back, not an assertion",
            "azimuth" to "viewed from +z the backbone azimuth INCREASES counter-clockwise with " +
                    "z, so one base pair is +240/7 degrees",
            "departure" to "+delta means BOTH backbones sit +delta from the line of centres, in " +
                    "the same rotational sense; the demanded relief roll is -delta on BOTH " +
                    "duplexes, an absolute roll of each about its own axis",
            "twist" to "theta_0(b) = phi_d(s = +L/2) - phi_d(s = -L/2), positive when the " +
                    "demanded roll increases with s; zero on a beam with a demand at one end only",
            "phase" to "the one binary no source fixes: the map from the derived azimuthal " +
                    "sense onto the model's own Phi sense. Both values graded"
        ),
        parameters = linkedMapOf(
            "crossSection" to "10 x 6",
            "raster" to "102 / 109 (C-0151, drawable), with 112 / 108 as the control",
            "rowBasePairs" to T291_BLOCK_EXTENT_BP.toString(),
            "edgeX" to shared.edgeX.emitted(9),
            "edgeY" to shared.edgeY.emitted(9),
            "interhelicalDistance" to shared.d.emitted(9),
            "phosphateRadius" to rP.emitted(9),
            "rowPitch" to shared.rowPitch.emitted(9),
            "columnPitch" to shared.columnPitch.emitted(9),
            "interiorPressure" to shared.interiorPressure.emitted(9),
            "closedFormStroke" to shared.closedFormStroke.emitted(9),
            "hingeStiffness" to Gen1Tile.crossoverHingeStiffness().emitted(9),
            "slipStiffness" to Gen1Tile.crossoverInPlaneStiffness().emitted(9),
            "torsionalRigidity" to gj.emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "compositeFractions" to "0.30 and 0.26 (C-0116), plus the lattice's own 1.0",
            "mandate" to "C-0017's 33.3333 pN/nm on the SUM, section 3's acceptable clause",
            "realisations" to t291Realisations.toString(),
            "seed" to T291_SEED.toString(),
            "samples" to T291_SAMPLES.toString(),
            "tolerance" to T291_TOLERANCE.emitted(2),
            "ladderPhase" to T291_LADDER_PHASE.toString(),
            "ladderOffset" to T291_LADDER_OFFSET.toString(),
            "allowedScaffoldCrossoverDepartureDegrees" to allowed.emitted(9),
            "demandedTwistDegrees" to demanded.emitted(9),
            "hostSheetColumnEnergyKt" to T291_HOST_COLUMN_KT.emitted(9),
            "firstAxialSign" to "+1"
        ),
        sources = listOf(
            "Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, Nucleic Acids Res. " +
                    "37:5001 (caDNAno; PMC2731887, in gpd/data/T-151-sources/), read directly " +
                    "-- the +-5 bp scaffold rule and the 21 bp period",
            "gpd/results/T-254-raster-turn-prestrain.json (C-0175) -- the tie set and its own " +
                    "relative-roll free-tile readings, carried unchanged as the term replaced",
            "gpd/results/T-284-turn-prestrain-sign.json (C-0187) -- the derived alternation, " +
                    "the 64 coupled cells and the zero-prestrain readings, reproduced",
            "gpd/results/T-3b-tile-edge-load-profile.json (C-0022) -- the solved collar"
        ),
        citedInputs = linkedMapOf(
            "C-0152 allowed scaffold-crossover departure [deg]" to allowed.emitted(9),
            "C-0079 host-sheet crossover column energy [kT]" to T291_HOST_COLUMN_KT.emitted(9),
            "Gen1Tile duplex torsional rigidity [pN*nm^2]" to gj.emitted(9),
            "Gen1Tile crossover hinge stiffness [pN*nm/rad]" to
                    Gen1Tile.crossoverHingeStiffness().emitted(9),
            "T-71 measured phosphate radius [nm]" to rP.emitted(9),
            "C-0187 tied free tile at f = 0.30, no prestrain" to
                    t291PublishedZeroField(t284, 21.1851817).emitted(9),
            "C-0175 section 8 relative-roll free tile at f = 0.30, by the rim" to
                    t291PublishedField(t254, 21.1851817, "by the rim the turn sits at")
                        .emitted(9),
            "C-0187 recovered cell A, zero prestrain" to t291PublishedCell(
                t284, 0.30, "abstract grid", 3, "rim-graded 5:1", 1,
                "zeroPrestrainP90OverStroke"
            ).emitted(9),
            "C-0187 recovered cell B, zero prestrain" to t291PublishedCell(
                t284, 0.30, "abstract grid on the rooting helices", 5, "rim-graded 5:1", 1,
                "zeroPrestrainP90OverStroke"
            ).emitted(9)
        ),
        cheapBound = cheapBound,
        channel = channel,
        twist = twistRows,
        conventionSweep = conventionSweep,
        energy = energy,
        freeTile = freeTile,
        ceiling = ceiling,
        relaxation = relaxation,
        cells = cells,
        signContingency = signContingency,
        scale = scaleRows,
        channelComparison = channelComparison,
        verdict = verdict,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "The eigenstrain is applied over the MODEL's beam, whose length is the block's 116 " +
                    "bp extent, while the physical rows are 102 and 109 bp. The load is " +
                    "GJ theta / L, so the model under-loads the demand by 116/109 = 1.06x to " +
                    "116/102 = 1.14x, and the field is exactly linear in it: a reader who wants " +
                    "the row-length reading multiplies.",
            "EVERY number here is conditional on the raster's turns carrying ZERO unpaired " +
                    "nucleotides, which is C-0175's own modelling choice and not the built " +
                    "precedent: Douglas et al.'s honeycomb blocks allot 28 nt per helix as " +
                    "front and rear unpaired loops, and a turn with 28 unpaired nucleotides is " +
                    "a flexible tether that demands no azimuth at all. On that design there is " +
                    "no tie, no prestrain and no twist -- and C-0175's tie STIFFNESS " +
                    "deliverable and C-0180's coupled recovery go with them.",
            "The load's SHAPE is derived and its MAGNITUDE is not: it scales with the tie's " +
                    "common-mode stiffness, which the lattice does not carry. The eigenstrain " +
                    "formulation used here is the model's natural choice, not a measurement, " +
                    "and the answer is therefore quoted as a threshold as well as a value.",
            "The demand is NOT accumulating along a helix -- exactly two turns, so exactly " +
                    "2 delta -- and it IS coherent across helices, which is what makes it a " +
                    "whole-block twist rather than noise. CLAUDE.md's 'local and " +
                    "non-accumulating' is right about the first and says nothing about the " +
                    "second.",
            "The tie sits at s = +-L/2 exactly; a scaffold crossover sits 5 bp from a staple " +
                    "position, so its true axial station is within 1.7 nm of the rim node, and " +
                    "nothing here prices that (C-0180's and C-0187's own open question).",
            "The lattice's tie carries NO common-mode stiffness at all -- only k_theta on the " +
                    "relative roll -- so the demand reaches the structure ONLY through the " +
                    "beam's own GJ. A physical crossover strained in span has a common-mode " +
                    "stiffness too, and at this geometry it is the LARGER of the two. That is a " +
                    "missing element rather than a wrong load, and it is filed as CH-0242.",
            "The magnitude is C-0152's rigid-duplex reading and is a CEILING: nothing here " +
                    "bounds from below how much of the 0.25 bp is taken up in backbone strain " +
                    "or local unstacking rather than in a roll.",
            "GJ is Gen1Tile's DUPLEX_TORSIONAL_RIGIDITY, which is CanDo's model input; " +
                    "CLAUDE.md records that CanDo's EI implies a persistence length 25 % above " +
                    "the measured one, and no independent torsional measurement is carried here.",
            "The lattice carries no across-helix parallel-axis term, so its D_perp is the " +
                    "independent one and a lower bound; Kirchhoff is not safe at these " +
                    "thicknesses, so every D_parallel is an upper bound.",
            "The dropout statistics are measured on a single-layer Rothemund rectangle and only " +
                    "the profile transfers, in nm; the ensemble perturbs the COUPLING and never " +
                    "the block's crossovers or its ties.",
            "Nothing here re-opens the placement search, the distribution rule, the raster, the " +
                    "cross-section or the departure's magnitude. The stations are C-0151's and " +
                    "the distributions C-0058's two."
        ),
        openQuestions = listOf(
            "What the tie's own COMMON-MODE stiffness is worth. Expanding the phosphate span " +
                    "about the line of centres gives a quadratic form whose common-mode " +
                    "stiffness is 1 + 2 r_P / (d - 2 r_P) = " +
                    (1.0 + 2.0 * rP / (shared.d - 2.0 * rP)).emitted(9) +
                    "x the relative one, so the element set is missing the LARGER of the two " +
                    "springs at every bond and every tie. That is CH-0242 and it is not priced " +
                    "here.",
            "Whether a distribution SEARCHED on the tied lattice under the twist term reaches a " +
                    "cell flat at BOTH signs. Every distribution graded here is a rule written " +
                    "on a smeared model's geometry.",
            "What the same eigenstrain is worth on the 15 x 4 cross-section, whose free tile " +
                    "already exceeds the tolerance, and whether the ribbon relaxation scales " +
                    "with the block's aspect ratio.",
            "What a FORCED crossover costs on this channel. C-0152's 0.35 kT ceiling is taken " +
                    "on the relative roll; on the twist channel the relief must be reconciled " +
                    "with the neighbouring crossovers 7 or 21 bp away rather than over a whole " +
                    "row, which is a much shorter lever."
        ),
        proseFailure = "none"
    )

    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    val encoded = json.encodeToJsonElement(result)
        .roundedForResult(digits = 9, floor = 1e-12)
        .withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject
    val out = File("gpd/results/T-291-common-mode-departure-and-beam-twist.json")
    out.parentFile?.mkdirs()
    out.writeText(json.encodeToString(JsonObject.serializer(), encoded) + "\n")
    println("T-291 - written to " + out.path)
}
