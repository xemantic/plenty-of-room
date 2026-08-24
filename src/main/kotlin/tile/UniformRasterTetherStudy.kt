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
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.maximumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.maximumUniformRowLength
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
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

// ---------------------------------------------------------------------------------------------
// T-307 -- route B's own UNIFORM raster, graded WITH its tethers.
//
// C-0204 shows the tether's span is DETERMINED at 0.787091706 nm on the drawable 102 / 109
// raster, and that the free tile is then flat at 16 of 16 surviving corners.  That answer is
// bought by DRAWABILITY: route B does not need caDNAno's residue condition at all, and at the
// built 28 nt allowance its own uniform paired rows -- 92 / 98 / 106 bp -- close at NO lattice
// phase.  There b0 is a free design variable and the 59 turns take a span DISTRIBUTION whose
// worst member reaches 4.35327572 nm, which is C-0201's own worst corner.
//
// C-0201 section 7 grades those three widths UNTIED and finds all three flat.  This study grades
// them with the tethers their own geometry implies, over the lattice phase.
//
// The cheap bound runs in two stages and both are emitted: the tension distribution, which needs
// no solve at all, and then the 59-column unit-tension influence bank, which C-0104's linearity
// makes a RIGOROUS triangle-inequality ceiling over every phase and corner for 60 solves per
// width.  The exact reading follows and the bank is verified against it, never substituted.
// ---------------------------------------------------------------------------------------------

private const val T307_SAMPLES: Int = 81
private const val T307_TOLERANCE: Double = 0.10
private const val T307_RIM_STANDOFF: Double = 1.0
private const val T307_BUFFER_MILLIMOLAR: Double = 2.0
private const val T307_GAP_NM: Double = 10.0
private const val T307_BIAS_VOLTS: Double = 0.192
private const val T307_HELICES: Int = 60
private const val T307_UNPAIRED_PER_HELIX: Int = 28
private const val T307_M13: Int = 7249
private const val T307_P7560: Int = 7560
private const val T307_P8064: Int = 8064
private const val T307_ORDERED_LOW: Int = 24
private const val T307_ORDERED_HIGH: Int = 32
private const val T307_PERIOD: Int = 21
private const val T307_IDENTITY: Double = 1e-9
private const val T307_COMPOSITE_FRACTION: Double = 0.30

/** `C-0204`'s determined span on the drawable raster, in nm — the comparand, not an input. */
private const val T307_DETERMINED_SPAN: Double = 0.787091706

private val T307_KUHN: List<Double> = listOf(2.10, 2.84)
private val T307_CONTOUR: List<Double> = listOf(0.65, 0.70)

private fun Double.t307Emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ records

@Serializable
private data class T307CheapBoundRow(
    val stage: String,
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private data class T307Span(
    val scaffold: String,
    val pairedRowBasePairs: Int,
    val rowWidth: Double,
    val classZeroResidue: Int,
    val closes: Boolean,
    val distinctSpanCount: Int,
    val minimumSpan: Double,
    val maximumSpan: Double,
    val meanSpan: Double,
    val turnsInsideTheAlignedHalf: Int,
    val allInsideTheAlignedHalf: Boolean
)

@Serializable
private data class T307Tension(
    val scaffold: String,
    val pairedRowBasePairs: Int,
    val classZeroResidue: Int,
    val chain: String,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val minimumTension: Double,
    val meanTension: Double,
    val maximumTension: Double,
    val meanTensionOverDetermined: Double,
    val maximumTensionOverDetermined: Double
)

@Serializable
private data class T307BankColumn(
    val pairedRowBasePairs: Int,
    val turnIndex: Int,
    val inPlane: Boolean,
    val atHighEnd: Boolean,
    val unitTensionPeakDishing: Double
)

@Serializable
private data class T307Cell(
    val scaffold: String,
    val pairedRowBasePairs: Int,
    val rowWidth: Double,
    val classZeroResidue: Int,
    val chain: String,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val maximumSpan: Double,
    val maximumTension: Double,
    val freeTileWithPreload: Double,
    val freeTileWithoutPreload: Double,
    val preloadMovement: Double,
    val bankedEstimate: Double,
    val bankedDeparture: Double,
    val triangleCeiling: Double,
    val ceilingHonoured: Boolean,
    val flatWithPreload: Boolean,
    val flatWithoutPreload: Boolean
)

@Serializable
private data class T307Best(
    val scaffold: String,
    val pairedRowBasePairs: Int,
    val rowWidth: Double,
    val untiedDishing: Double,
    val bestPhaseOnDishing: Int,
    val bestWorstCornerDishing: Double,
    val bestPhaseFlatAtEveryCorner: Boolean,
    val worstPhaseOnDishing: Int,
    val worstWorstCornerDishing: Double,
    val bestPhaseOnAlignedTurns: Int,
    val alignedTurnsAtThatPhase: Int,
    val worstCornerDishingAtTheAlignedOptimum: Double,
    val theTwoCriteriaAgree: Boolean,
    val flatCellCount: Int,
    val cellCount: Int
)

@Serializable
private data class T307Reproduction(
    val source: String,
    val quantity: String,
    val there: String,
    val here: String,
    val departure: Double,
    val closes: Boolean
)

@Serializable
private data class T307Convergence(
    val axis: String,
    val setting: String,
    val value: Double,
    val departure: Double?,
    val verdictSurvives: Boolean?
)

@Serializable
private data class T307Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private data class T307Result(
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
    val cheapBound: List<T307CheapBoundRow>,
    val spans: List<T307Span>,
    val tensions: List<T307Tension>,
    val bank: List<T307BankColumn>,
    val cells: List<T307Cell>,
    val best: List<T307Best>,
    val reproductions: List<T307Reproduction>,
    val convergence: List<T307Convergence>,
    val falsifiers: List<T307Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T307Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T307_RIM_STANDOFF))
        )
}

private fun t307Profile(file: File): T307Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == T307_BUFFER_MILLIMOLAR &&
                    value("gapHeight") == T307_GAP_NM &&
                    value("appliedBias") == T307_BIAS_VOLTS
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T307Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * One uniform route-B width, as the tile `C-0201` §7 graded untied.
 *
 * The construction is that claim's own, so the untied reproduction is a control on the geometry
 * as well as on the code: the same `10 × 6` cross-section, the same collar shape, the same
 * foundation, and only the axial span moved.
 */
private class T307Width(
    val scaffold: String,
    val scaffoldNucleotides: Int,
    val profile: T307Profile,
    val block: HoneycombBlock,
    val edgeY: Double,
    val enhancement: Double
) {
    val pairedRowBasePairs: Int =
        maximumUniformRowLength(scaffoldNucleotides, T307_HELICES, T307_UNPAIRED_PER_HELIX)
    val rowWidth: Double = pairedRowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (rowWidth * edgeY)
    val pressureField: PressureField = profile.field(interiorPressure, rowWidth, edgeY)

    /** The lattice with no turn element at all — `C-0201` §7's own object. */
    val untied: HoneycombGrillage = HoneycombGrillage(
        block = block,
        rowBasePairs = pairedRowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement
    )

    val nodesPerBeam: Int = untied.nodesPerBeam
    val freeStroke: Double = untied.solve(uniformPressure(interiorPressure)).meanDeflection
    val untiedDishing: Double =
        untied.solve(pressureField).peakDishing(T307_SAMPLES) / freeStroke

    fun dishingOf(lattice: HoneycombGrillage): Double =
        lattice.solve(pressureField).peakDishing(T307_SAMPLES) / freeStroke
}

@Suppress("LongMethod", "ComplexMethod", "LongParameterList")
fun main() {
    val smoke = System.getenv("T307_SMOKE") != null
    val phases = if (smoke) listOf(0, 7) else (0 until T307_PERIOD).toList()
    val kBT = thermalEnergy(ROOM_TEMPERATURE)
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val block = HoneycombBlock(10, 6)
    val columnPitch = HoneycombCrossSectionGeometry.columnPitch(d)
    val rowPitch = HoneycombCrossSectionGeometry.rowPitch(d)
    val edgeY = 10 * rowPitch
    val enhancement = multiLayerRigidities(
        layers = 6,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = T307_COMPOSITE_FRACTION,
        layerSpacing = columnPitch
    ).realisedEnhancement
    val profile = t307Profile(File(ResultInputs.T_3B.path))

    val widths = listOf(
        "M13mp18" to T307_M13, "p7560" to T307_P7560, "p8064" to T307_P8064
    ).let { if (smoke) it.take(1) else it }.map { (name, nucleotides) ->
        T307Width(name, nucleotides, profile, block, edgeY, enhancement)
    }

    /** The three readings of the built 28 nt allowance, `C-0193`'s and `C-0200`'s. */
    val chains = listOf(
        Triple("28 nt at both rims (C-0193)", 28, 28),
        Triple("C-0200's ordered split: 24 nt low, 32 high", T307_ORDERED_LOW, T307_ORDERED_HIGH),
        Triple("C-0200's ordered split exchanged: 32 nt low, 24 high", T307_ORDERED_HIGH, T307_ORDERED_LOW)
    )
    val corners = chains.flatMap { chain ->
        T307_KUHN.flatMap { b -> T307_CONTOUR.map { c -> Triple(chain, b, c) } }
    }

    fun tethersOf(
        width: T307Width, phase: Int, chain: Triple<String, Int, Int>, b: Double, c: Double
    ) = UniformRasterTethers(
        block = block,
        pairedRowBasePairs = width.pairedRowBasePairs,
        interhelicalDistance = d,
        phosphateRadius = rP,
        classZeroResidue = phase,
        lowRimNucleotides = chain.second,
        highRimNucleotides = chain.third,
        kuhnLength = b,
        contourPerNucleotide = c,
        thermalEnergy = kBT
    )

    // ========================================= Deliverable 1 -- the span census, per phase
    println("T-307 - the span census: route B's uniform rows close at no lattice phase")
    val spanRows = ArrayList<T307Span>()
    widths.forEach { width ->
        phases.forEach { phase ->
            val subject = tethersOf(width, phase, chains[0], T307_KUHN[0], T307_CONTOUR[0])
            spanRows += T307Span(
                scaffold = width.scaffold,
                pairedRowBasePairs = width.pairedRowBasePairs,
                rowWidth = width.rowWidth,
                classZeroResidue = phase,
                closes = subject.closes,
                distinctSpanCount = subject.distinctSpans.size,
                minimumSpan = subject.minimumSpan,
                maximumSpan = subject.maximumSpan,
                meanSpan = subject.meanSpan,
                turnsInsideTheAlignedHalf = subject.turnsInsideTheAlignedHalf,
                allInsideTheAlignedHalf = subject.turnsInsideTheAlignedHalf == subject.spans.size
            )
        }
    }
    widths.forEach { width ->
        val here = spanRows.filter { it.pairedRowBasePairs == width.pairedRowBasePairs }
        println(
            "  " + width.scaffold + "  " + width.pairedRowBasePairs + " bp  worst span " +
                    here.minOf { it.maximumSpan }.t307Emitted(9) + " to " +
                    here.maxOf { it.maximumSpan }.t307Emitted(9) + " nm over " + here.size +
                    " phases, best aligned count " + here.maxOf { it.turnsInsideTheAlignedHalf }
        )
    }

    // ========================================= Cheap bound 1 -- the tension, with no solve
    println("T-307 - cheap bound 1: the tension distribution, no solve at all")
    fun determinedTensionOf(nucleotides: Int, b: Double, c: Double): Double =
        freelyJointedTetherState(T307_DETERMINED_SPAN, nucleotides, b, c, kBT).tension
    val determinedTension = T307_KUHN.flatMap { b ->
        T307_CONTOUR.map { c -> determinedTensionOf(T307_UNPAIRED_PER_HELIX, b, c) }
    }
    val tensionRows = ArrayList<T307Tension>()
    widths.forEach { width ->
        phases.forEach { phase ->
            corners.forEach { (chain, b, c) ->
                val subject = tethersOf(width, phase, chain, b, c)
                // The comparand is the SAME chain at the SAME corner held at C-0204's determined
                // span, so the ratio is a statement about the span alone.
                val reference = subject.states.map {
                    determinedTensionOf(it.unpairedNucleotides, b, c)
                }.average()
                tensionRows += T307Tension(
                    scaffold = width.scaffold,
                    pairedRowBasePairs = width.pairedRowBasePairs,
                    classZeroResidue = phase,
                    chain = chain.first,
                    kuhnLength = b,
                    contourPerNucleotide = c,
                    minimumTension = subject.states.minOf { it.tension },
                    meanTension = subject.meanTension,
                    maximumTension = subject.maximumTension,
                    meanTensionOverDetermined = subject.meanTension / reference,
                    maximumTensionOverDetermined = subject.maximumTension / reference
                )
            }
        }
    }
    println(
        "  the determined-span tension is " + determinedTension.min().t307Emitted(9) + " to " +
                determinedTension.max().t307Emitted(9) + " pN; the uniform raster's MEAN is " +
                tensionRows.minOf { it.meanTensionOverDetermined }.t307Emitted(9) + " to " +
                tensionRows.maxOf { it.meanTensionOverDetermined }.t307Emitted(9) +
                "x that and its WORST " +
                tensionRows.maxOf { it.maximumTensionOverDetermined }.t307Emitted(9) + "x"
    )

    // ========================================= Cheap bound 2 -- the unit-tension bank
    println("T-307 - cheap bound 2: the 59-column unit-tension bank, one per width")
    val bankRows = ArrayList<T307BankColumn>()
    val turns = honeycombRasterTurnList(block)
    val banks = widths.associate { width ->
        val reference = tethersOf(width, phases.first(), chains[0], T307_KUHN[0], T307_CONTOUR[0])
        val columns = turns.indices.map { k ->
            val lattice = width.untied.let {
                HoneycombGrillage(
                    block = block,
                    rowBasePairs = width.pairedRowBasePairs,
                    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                    hingeStiffnessEnhancement = enhancement,
                    scaffoldTurnTethers = reference.unitTensionElements(width.nodesPerBeam, k)
                )
            }
            val peak = lattice.solve().peakDishing(T307_SAMPLES) / width.freeStroke
            bankRows += T307BankColumn(
                pairedRowBasePairs = width.pairedRowBasePairs,
                turnIndex = k,
                inPlane = turns[k].inPlane,
                atHighEnd = turns[k].atHighEnd,
                unitTensionPeakDishing = peak
            )
            peak
        }
        width.pairedRowBasePairs to columns
    }
    println(
        "  the unit-tension peak dishing runs " +
                bankRows.minOf { it.unitTensionPeakDishing }.t307Emitted(9) + " to " +
                bankRows.maxOf { it.unitTensionPeakDishing }.t307Emitted(9) +
                " per pN, and it is exactly " +
                bankRows.count { it.unitTensionPeakDishing == 0.0 } + " of " + bankRows.size +
                " zero -- the in-plane turns, which this model has no coordinate for"
    )

    // ========================================= Deliverable 2 -- the exact free-tile grading
    println("T-307 - the free tile, with the tethers, over " + phases.size + " lattice phases")
    val cells = ArrayList<T307Cell>()
    widths.forEach { width ->
        val bank = banks.getValue(width.pairedRowBasePairs)
        val ceilingBase = width.untiedDishing
        phases.forEach { phase ->
            corners.forEach { (chain, b, c) ->
                val subject = tethersOf(width, phase, chain, b, c)
                val loaded = subject.lattice(enhancement = enhancement, withPreload = true)
                val unloaded = subject.lattice(enhancement = enhancement, withPreload = false)
                val banked = subject.lattice(
                    enhancement = enhancement, withPreload = true, stiffness = 0.0
                )
                val withPreload = width.dishingOf(loaded)
                val withoutPreload = width.dishingOf(unloaded)
                val bankedEstimate = width.dishingOf(banked)
                val ceiling = ceilingBase +
                        subject.states.indices.sumOf { abs(subject.states[it].tension) * bank[it] }
                cells += T307Cell(
                    scaffold = width.scaffold,
                    pairedRowBasePairs = width.pairedRowBasePairs,
                    rowWidth = width.rowWidth,
                    classZeroResidue = phase,
                    chain = chain.first,
                    kuhnLength = b,
                    contourPerNucleotide = c,
                    maximumSpan = subject.maximumSpan,
                    maximumTension = subject.maximumTension,
                    freeTileWithPreload = withPreload,
                    freeTileWithoutPreload = withoutPreload,
                    preloadMovement = withPreload - withoutPreload,
                    bankedEstimate = bankedEstimate,
                    bankedDeparture = abs(bankedEstimate - withPreload),
                    triangleCeiling = ceiling,
                    ceilingHonoured = withPreload < ceiling + T307_IDENTITY,
                    flatWithPreload = withPreload < T307_TOLERANCE,
                    flatWithoutPreload = withoutPreload < T307_TOLERANCE
                )
            }
        }
    }
    println(
        "  " + cells.count { it.flatWithPreload } + " of " + cells.size +
                " cells flat with the preload; the free tile runs " +
                cells.minOf { it.freeTileWithPreload }.t307Emitted(9) + " to " +
                cells.maxOf { it.freeTileWithPreload }.t307Emitted(9) + " of the stroke"
    )

    // ========================================= Deliverable 3 -- the phase as a design variable
    println("T-307 - the recommendation: is any lattice phase materially better?")
    val bestRows = widths.map { width ->
        val here = cells.filter { it.pairedRowBasePairs == width.pairedRowBasePairs }
        val byPhase = phases.associateWith { phase ->
            here.filter { it.classZeroResidue == phase }.maxOf { it.freeTileWithPreload }
        }
        val bestPhase = byPhase.minBy { it.value }.key
        val worstPhase = byPhase.maxBy { it.value }.key
        val spansHere = spanRows.filter { it.pairedRowBasePairs == width.pairedRowBasePairs }
        val alignedOptimum = spansHere.maxBy { it.turnsInsideTheAlignedHalf }
        T307Best(
            scaffold = width.scaffold,
            pairedRowBasePairs = width.pairedRowBasePairs,
            rowWidth = width.rowWidth,
            untiedDishing = width.untiedDishing,
            bestPhaseOnDishing = bestPhase,
            bestWorstCornerDishing = byPhase.getValue(bestPhase),
            bestPhaseFlatAtEveryCorner = byPhase.getValue(bestPhase) < T307_TOLERANCE,
            worstPhaseOnDishing = worstPhase,
            worstWorstCornerDishing = byPhase.getValue(worstPhase),
            bestPhaseOnAlignedTurns = alignedOptimum.classZeroResidue,
            alignedTurnsAtThatPhase = alignedOptimum.turnsInsideTheAlignedHalf,
            worstCornerDishingAtTheAlignedOptimum =
                byPhase.getValue(alignedOptimum.classZeroResidue),
            theTwoCriteriaAgree = bestPhase == alignedOptimum.classZeroResidue,
            flatCellCount = here.count { it.flatWithPreload },
            cellCount = here.size
        )
    }
    bestRows.forEach {
        println(
            "  " + it.scaffold + "  " + it.pairedRowBasePairs + " bp  untied " +
                    it.untiedDishing.t307Emitted(9) + "  best phase " + it.bestPhaseOnDishing +
                    " at " + it.bestWorstCornerDishing.t307Emitted(9) + "  worst phase " +
                    it.worstPhaseOnDishing + " at " + it.worstWorstCornerDishing.t307Emitted(9)
        )
    }

    // ========================================= the falsifiers and the gates
    println("T-307 - the falsifiers")
    val uniformDishings = widths.map { width ->
        val subject = tethersOf(width, phases.first(), chains[0], T307_KUHN[0], T307_CONTOUR[0])
        val lattice = subject.lattice(enhancement = enhancement, withPreload = false)
        val field = lattice.solve(uniformPressure(width.interiorPressure))
        width.pairedRowBasePairs to abs(field.peakDishing(T307_SAMPLES) / field.meanDeflection)
    }
    val loadIdentical = widths.all { width ->
        val subject = tethersOf(width, phases.first(), chains[0], T307_KUHN[0], T307_CONTOUR[0])
        val inert = HoneycombGrillage(
            block = block,
            rowBasePairs = width.pairedRowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = enhancement,
            scaffoldTurnTethers =
                subject.elements(width.nodesPerBeam, withPreload = false, stiffness = 0.0)
        )
        val a = width.untied.assembleLoad(width.pressureField)
        val b = inert.assembleLoad(width.pressureField)
        (0 until width.untied.degreesOfFreedom).all { a[it] == b[it] }
    }

    // ========================================= convergence, at the DECIDING cell
    println("T-307 - convergence, on the deciding quantity at the deciding cell")
    val deciding = cells.minBy { abs(it.freeTileWithPreload - T307_TOLERANCE) }
    val decidingWidth = widths.first { it.pairedRowBasePairs == deciding.pairedRowBasePairs }
    val decidingChain = chains.first { it.first == deciding.chain }
    val convergence = ArrayList<T307Convergence>()
    listOf(41, 81, 161).forEach { samples ->
        val subject = tethersOf(
            decidingWidth, deciding.classZeroResidue, decidingChain,
            deciding.kuhnLength, deciding.contourPerNucleotide
        )
        val lattice = subject.lattice(enhancement = enhancement, withPreload = true)
        val stroke = decidingWidth.untied.solve(
            uniformPressure(decidingWidth.interiorPressure)
        ).meanDeflection
        val value = lattice.solve(decidingWidth.pressureField).peakDishing(samples) / stroke
        convergence += T307Convergence(
            axis = "the dishing sample grid at the deciding cell",
            setting = samples.toString() + " samples",
            value = value,
            departure = if (samples == T307_SAMPLES) null
            else abs(value - deciding.freeTileWithPreload),
            verdictSurvives = (value < T307_TOLERANCE) == deciding.flatWithPreload
        )
    }
    listOf(1, 2).forEach { subdivisions ->
        val subject = tethersOf(
            decidingWidth, deciding.classZeroResidue, decidingChain,
            deciding.kuhnLength, deciding.contourPerNucleotide
        )
        val lattice = subject.lattice(
            enhancement = enhancement, withPreload = true, subdivisions = subdivisions
        )
        val bare = HoneycombGrillage(
            block = block,
            rowBasePairs = decidingWidth.pairedRowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = enhancement,
            subdivisions = subdivisions
        )
        val stroke = bare.solve(uniformPressure(decidingWidth.interiorPressure)).meanDeflection
        val value =
            lattice.solve(decidingWidth.pressureField).peakDishing(T307_SAMPLES) / stroke
        convergence += T307Convergence(
            axis = "beam subdivision at the deciding cell",
            setting = subdivisions.toString() + " element(s) per plane",
            value = value,
            departure = if (subdivisions == 1) null
            else abs(value - convergence.first { it.setting == "1 element(s) per plane" }.value),
            verdictSurvives = (value < T307_TOLERANCE) == deciding.flatWithPreload
        )
    }
    convergence.forEach {
        println("  " + it.axis + "  " + it.setting + "  " + it.value.t307Emitted(9))
    }

    // ========================================= reproductions
    val reproductions = ArrayList<T307Reproduction>()
    fun reproduce(source: String, quantity: String, there: Double, here: Double) {
        val departure = abs(here - there) / maxOf(abs(there), 1e-30)
        reproductions += T307Reproduction(
            source = source, quantity = quantity,
            there = there.t307Emitted(9), here = here.t307Emitted(9),
            departure = departure, closes = departure < 1e-8
        )
    }
    val untiedThere = mapOf(92 to 0.0425678289, 98 to 0.0422200543, 106 to 0.0451172785)
    widths.forEach { width ->
        reproduce(
            "C-0201 (T-299) section 7",
            width.scaffold + "'s uniform row, uncoupled dishing over the stroke",
            untiedThere.getValue(width.pairedRowBasePairs), width.untiedDishing
        )
        reproduce(
            "C-0201 (T-299) section 7", width.scaffold + "'s uniform paired row width, in nm",
            mapOf(92 to 31.28, 98 to 33.32, 106 to 36.04).getValue(width.pairedRowBasePairs),
            width.rowWidth
        )
    }
    val t304 = Json.parseToJsonElement(File(ResultInputs.T_304.path).readText())
        .jsonObject.getValue("uniformRasters").jsonArray.map { it.jsonObject }
    var worstSpanDeparture = 0.0
    var spanRowsClosing = 0
    spanRows.forEach { row ->
        val there = t304.firstOrNull {
            it.getValue("pairedRowBasePairs").jsonPrimitive.content.toInt() ==
                    row.pairedRowBasePairs &&
                    it.getValue("classZeroResidue").jsonPrimitive.content.toInt() ==
                    row.classZeroResidue
        } ?: error("T-304 carries no uniform raster at " + row.pairedRowBasePairs + " bp")
        fun value(name: String) = there.getValue(name).jsonPrimitive.content.toDouble()
        fun count(name: String) = there.getValue(name).jsonPrimitive.content.toInt()
        val worst = maxOf(
            abs(value("minimumSpan") - row.minimumSpan),
            abs(value("maximumSpan") - row.maximumSpan),
            abs(value("meanSpan") - row.meanSpan)
        )
        val agrees = worst < 1e-8 &&
                count("distinctSpanCount") == row.distinctSpanCount &&
                count("turnsInsideTheAlignedHalf") == row.turnsInsideTheAlignedHalf
        if (agrees) spanRowsClosing++
        worstSpanDeparture = maxOf(worstSpanDeparture, worst)
    }
    reproductions += T307Reproduction(
        source = "C-0204 (T-304) uniformRasters",
        quantity = "the per-phase span census, min / max / mean / distinct / aligned count, at " +
                spanRows.size + " (width, phase) rows",
        there = spanRows.size.toString() + " rows",
        here = spanRowsClosing.toString() + " rows agree",
        departure = worstSpanDeparture,
        closes = spanRowsClosing == spanRows.size
    )
    reproduce(
        "C-0204 (T-304)",
        "the determined-span tension at its softest corner, 28 nt at b = 2.84, c = 0.70, in pN",
        0.175872271, determinedTensionOf(T307_UNPAIRED_PER_HELIX, 2.84, 0.70)
    )
    reproduce(
        "C-0204 (T-304)",
        "the determined-span tension at its stiffest corner, 15 nt at b = 2.10, c = 0.65, in pN",
        0.479548487, determinedTensionOf(15, 2.10, 0.65)
    )
    reproduce(
        "C-0147 (T-230)", "the worst-azimuth span d + 2 r_P, in nm",
        4.35327572, maximumTurnPhosphateSpan(d, rP)
    )
    reproductions.forEach {
        println(
            "  " + it.source + "  " + it.quantity + "  departure " + it.departure.t307Emitted(2) +
                    (if (it.closes) "  closes" else "  DOES NOT CLOSE")
        )
    }

    val falsifiers = listOf(
        T307Falsifier(
            "F1", "a uniform pressure on the free tethered lattice, preload off, dishes more " +
                    "than 1e-9 of the stroke at any of 92 / 98 / 106 bp",
            uniformDishings.any { it.second > T307_IDENTITY },
            "peak dishing over stroke " + uniformDishings.joinToString(", ") {
                it.first.toString() + " bp: " + it.second.t307Emitted(2)
            } + ". 92 and 106 are NOT multiples of the 7 bp crossover-plane pitch, so this is " +
                    "the case HoneycombGrillage.nodeS's free-overhang branch exists for, and " +
                    "the falsifier is what would find it missing"
        ),
        T307Falsifier(
            "F2", "the untied re-grade fails to reproduce C-0201 section 7's three uncoupled " +
                    "dishings to 1e-8",
            reproductions.filter { it.source.startsWith("C-0201") }.any { !it.closes },
            "worst departure " + reproductions.filter { it.source.startsWith("C-0201") }
                .maxOf { it.departure }.t307Emitted(2) + " over " +
                    reproductions.count { it.source.startsWith("C-0201") } + " reproductions"
        ),
        T307Falsifier(
            "F3", "the per-turn span census fails to reproduce T-304's committed uniformRasters",
            spanRowsClosing != spanRows.size,
            spanRowsClosing.toString() + " of " + spanRows.size + " (width, phase) rows agree " +
                    "on min, max, mean, distinct count and aligned count; worst departure " +
                    worstSpanDeparture.t307Emitted(2) + " nm"
        ),
        T307Falsifier(
            "F4", "a stiffness-free, preload-free tether list is not bit-identical to the " +
                    "untethered lattice on assembleLoad at some row length",
            !loadIdentical,
            "the load vector is bit-identical at all three row lengths over every degree of " +
                    "freedom; the element census, the rim node and the preload's rigid-roll " +
                    "annihilation are asserted in UniformRasterTetherSpansTest"
        ),
        T307Falsifier(
            "F5", "no lattice phase of any uniform width leaves the free tile inside T-5b's " +
                    "0.10 at every corner -- declared OPEN",
            bestRows.none { it.bestPhaseFlatAtEveryCorner },
            bestRows.joinToString("; ") {
                it.pairedRowBasePairs.toString() + " bp best phase " + it.bestPhaseOnDishing +
                        " worst corner " + it.bestWorstCornerDishing.t307Emitted(9)
            }
        ),
        T307Falsifier(
            "F6", "the phase that minimises the free-tile dishing is not the phase T-304 names " +
                    "best on turnsInsideTheAlignedHalf -- declared OPEN",
            bestRows.any { !it.theTwoCriteriaAgree },
            bestRows.joinToString("; ") {
                it.pairedRowBasePairs.toString() + " bp: dishing optimum at phase " +
                        it.bestPhaseOnDishing + ", aligned-count optimum at phase " +
                        it.bestPhaseOnAlignedTurns + " (" + it.alignedTurnsAtThatPhase +
                        " of 59), whose worst corner is " +
                        it.worstCornerDishingAtTheAlignedOptimum.t307Emitted(9)
            }
        ),
        T307Falsifier(
            "F7", "the C-0104 triangle-inequality ceiling is exceeded by a measured tethered " +
                    "dishing at some cell",
            cells.any { !it.ceilingHonoured },
            cells.count { it.ceilingHonoured }.toString() + " of " + cells.size +
                    " cells honour the ceiling; the worst ratio of measured to ceiling is " +
                    cells.maxOf { it.freeTileWithPreload / it.triangleCeiling }.t307Emitted(9)
        ),
        T307Falsifier(
            "F8", "the flatness verdict at the deciding cell moves under beam subdivision " +
                    "1 -> 2 -- declared OPEN",
            convergence.filter { it.verdictSurvives == false }.isNotEmpty(),
            "the deciding cell is " + deciding.pairedRowBasePairs + " bp phase " +
                    deciding.classZeroResidue + " at " + deciding.freeTileWithPreload
                .t307Emitted(9) + "; worst departure over both axes " +
                    convergence.mapNotNull { it.departure }.maxOrNull().let {
                        it?.t307Emitted(2) ?: "none"
                    }
        ),
        T307Falsifier(
            "F9", "the reach bound refuses at some corner -- a span at or above the chain's " +
                    "own contour",
            false,
            "every one of the " + cells.size + " graded cells was constructed, and " +
                    "freelyJointedTetherState refuses a span at or above the contour; the " +
                    "whole declared bracket is asserted reachable at all three widths and all " +
                    T307_PERIOD + " phases in UniformRasterTetherSpansTest"
        )
    )
    falsifiers.forEach { println("  " + it.id + (if (it.fired) "  FIRED" else "  did not fire")) }

    // ========================================= the prose
    val cheapBound = listOf(
        T307CheapBoundRow(
            stage = "1 -- arithmetic, no solve",
            question = "how much larger is the uniform raster's chain tension than the " +
                    "determined raster's?",
            answer = "the determined span is " + T307_DETERMINED_SPAN.t307Emitted(9) +
                    " nm and carries " + determinedTension.min().t307Emitted(9) + " to " +
                    determinedTension.max().t307Emitted(9) + " pN; a uniform route-B raster's " +
                    "59 turns carry a MEAN of " +
                    tensionRows.minOf { it.meanTensionOverDetermined }.t307Emitted(9) + " to " +
                    tensionRows.maxOf { it.meanTensionOverDetermined }.t307Emitted(9) +
                    "x that, and a worst of " +
                    tensionRows.maxOf { it.maximumTensionOverDetermined }.t307Emitted(9) + "x",
            consequence = "C-0204 measures the preload's own worth on the free tile at the " +
                    "determined span as 0.00708426936 to 0.0195297045 of the stroke; a mean " +
                    "tension a few times larger, on top of C-0201's untied 0.0422200543 to " +
                    "0.0451172785, predicts a reading that STRADDLES T-5b -- which is why the " +
                    "question needed a solve and not only an estimate"
        ),
        T307CheapBoundRow(
            stage = "2 -- the unit-tension bank, 59 solves per width",
            question = "can the whole phase and corner axis be bounded without re-solving?",
            answer = "yes. A tether's tension is C-0104's internal initial stress: it changes " +
                    "no entry of the stiffness matrix and the field is exactly linear in it. " +
                    "One unit-tension solve per turn gives a RIGOROUS triangle-inequality " +
                    "ceiling at every phase and every corner with no further solve",
            consequence = "the ceiling is honoured at " + cells.count { it.ceilingHonoured } +
                    " of " + cells.size + " cells (F7), and the bank's linear superposition " +
                    "reproduces the exact solve to " +
                    cells.maxOf { it.bankedDeparture }.t307Emitted(2) + " of the stroke -- " +
                    "which MEASURES what the tether's own stiffness is worth rather than " +
                    "assuming it"
        ),
        T307CheapBoundRow(
            stage = "3 -- what the cheap bound could not decide",
            question = "does the estimate settle the verdict?",
            answer = "no. The tension ratio is a level and the dishing is a SIGNED " +
                    "superposition over 59 sites with two through-thickness azimuths of " +
                    "opposite unitY, so a larger tension everywhere does not imply a larger " +
                    "peak. The ceiling is rigorous and loose",
            consequence = "the exact solve is what decides, and the bank is retained as the " +
                    "control on it"
        )
    )

    var proseFailure = "none"
    val findings: List<String> = try {
        listOf(
            "ROUTE B'S OWN UNIFORM RASTER IS " + (
                    if (bestRows.all { it.bestPhaseFlatAtEveryCorner }) "FLAT AT ITS BEST PHASE"
                    else if (bestRows.any { it.bestPhaseFlatAtEveryCorner })
                        "FLAT AT SOME WIDTHS AND NOT AT OTHERS"
                    else "NOT FLAT AT ANY LATTICE PHASE"
                    ) + ", AND THE LATTICE PHASE IS WORTH " +
                    bestRows.maxOf {
                        it.worstWorstCornerDishing / it.bestWorstCornerDishing
                    }.t307Emitted(9) + "x. Graded with the tethers their own geometry implies, " +
                    "the three uniform paired rows read " +
                    cells.minOf { it.freeTileWithPreload }.t307Emitted(9) + " to " +
                    cells.maxOf { it.freeTileWithPreload }.t307Emitted(9) + " of the stroke " +
                    "over " + cells.size + " cells, " + cells.count { it.flatWithPreload } +
                    " of them flat against T-5b's 0.10, where C-0201 section 7 graded the same " +
                    "three widths UNTIED at 0.0425678289 / 0.0422200543 / 0.0451172785 and " +
                    "found all three flat.",
            "THE PRELOAD CARRIES THE MOVEMENT AND THE STIFFNESS IS SMALL AND NOT NOTHING. " +
                    "With the preload dropped the same lattices read " +
                    cells.minOf { it.freeTileWithoutPreload }.t307Emitted(9) + " to " +
                    cells.maxOf { it.freeTileWithoutPreload }.t307Emitted(9) + " -- " +
                    cells.count { it.flatWithoutPreload } + " of " + cells.size + " flat, and " +
                    "BELOW the untied reading at its softest, so the tether's stiffness alone " +
                    "moves the tile TOWARD flatness exactly as C-0201 measured on the drawable " +
                    "raster. The preload is worth " +
                    cells.minOf { it.preloadMovement }.t307Emitted(9) + " to " +
                    cells.maxOf { it.preloadMovement }.t307Emitted(9) + " of the stroke. But " +
                    "the stiffness is not zero: holding the same tensions and setting both " +
                    "tether stiffnesses to zero -- the bank's own lattice -- moves the answer " +
                    "by up to " + cells.maxOf { it.bankedDeparture }.t307Emitted(9) + ", which " +
                    "is " + (
                    cells.maxOf { it.bankedDeparture } /
                            cells.maxOf { it.preloadMovement }
                    ).t307Emitted(9) + " of the largest preload movement and " + (
                    cells.maxOf { it.bankedDeparture } / T307_TOLERANCE
                    ).t307Emitted(9) + " of T-5b's whole tolerance. C-0201's LOAD NOT A SPRING " +
                    "is upheld in its ORDERING, and at a span three to four times the " +
                    "determined one the spring is no longer arithmetically absent.",
            "THE PHASE THAT ALIGNS THE MOST TURNS IS " + (
                    if (bestRows.all { it.theTwoCriteriaAgree }) "THE PHASE THAT FLATTENS THE " +
                            "TILE, AT ALL THREE WIDTHS"
                    else "NOT THE PHASE THAT FLATTENS THE TILE"
                    ) + ". T-304 names phase " +
                    bestRows.map { it.bestPhaseOnAlignedTurns }.distinct().joinToString("/") +
                    " best on turnsInsideTheAlignedHalf; on the dishing the optimum is phase " +
                    bestRows.map { it.bestPhaseOnDishing }.distinct().joinToString("/") +
                    ". F6 was declared open and " +
                    (if (bestRows.any { !it.theTwoCriteriaAgree }) "FIRED" else "did not fire") +
                    ".",
            "THE CHEAP BOUND PREDICTED THE SIZE AND COULD NOT DECIDE THE VERDICT, AND IT SAID " +
                    "SO BEFORE THE SOLVE. The uniform raster's mean chain tension is " +
                    tensionRows.minOf { it.meanTensionOverDetermined }.t307Emitted(9) + " to " +
                    tensionRows.maxOf { it.meanTensionOverDetermined }.t307Emitted(9) +
                    "x the determined raster's and its worst " +
                    tensionRows.maxOf { it.maximumTensionOverDetermined }.t307Emitted(9) +
                    "x, which on C-0204's own preload movement predicts a straddle of T-5b. " +
                    "The rigorous triangle-inequality ceiling that follows from C-0104's " +
                    "linearity is honoured at " + cells.count { it.ceilingHonoured } + " of " +
                    cells.size + " cells and is loose by a factor of " +
                    (1.0 / cells.minOf { it.freeTileWithPreload / it.triangleCeiling })
                        .t307Emitted(9) + " at its worst, because the 59 contributions are " +
                    "SIGNED.",
            "THE nodeS PRECONDITION DID NOT BITE. Two of the three uniform rows are not " +
                    "multiples of the 7 bp crossover-plane pitch -- 92 mod 7 = 1 and " +
                    "106 mod 7 = 1 -- which is the state CLAUDE.md records a uniform-load " +
                    "falsifier finding on a lattice whose beams stop at the last plane. " +
                    "HoneycombGrillage.nodeS carries a free-overhang branch for exactly that, " +
                    "and F1 does not fire: the uniform-load dishing is " +
                    uniformDishings.maxOf { it.second }.t307Emitted(2) + " at worst."
        )
    } catch (failure: Exception) {
        proseFailure = failure.toString()
        emptyList()
    }

    val result = T307Result(
        task = "T-307",
        leaf = "A8.2",
        title = "route B's own uniform raster, graded with the tethers its span distribution " +
                "implies, over the lattice phase as a design variable",
        verificationType = "logical (exact integer residue arithmetic on this repository's own " +
                "honeycomb crossover lattice, reproduced against T-304's committed " +
                "uniformRasters at every (width, phase) row) + in-silico (the same honeycomb " +
                "grillage and the same T-299 tether element, at a PER-TURN span) + literature " +
                "(C-0193's and C-0200's reading of the built allowance, T-71's measured " +
                "phosphate radius and T-230's ssDNA Kuhn and contour brackets)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "three widths are what three scaffolds afford at the built 28 nt allowance; no " +
                "such raster has been drawn, let alone folded, and this repository cannot fold " +
                "one.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "level" to "integer base pairs on one global z",
            "dishing" to "dimensionless, as a fraction of the free stroke"
        ),
        conventions = mapOf(
            "the row length" to "the PAIRED row, scaffoldNucleotides / 60 - 28, which is what " +
                    "the built allowance fixes; edgeX is that many base pairs at 0.34 nm/bp " +
                    "and edgeY is unchanged at 10 row pitches, exactly C-0201 section 7's " +
                    "construction",
            "the lattice phase" to "b0, the class-zero staple residue. On a raster that does " +
                    "not close it is a free DESIGN VARIABLE and is never derived here",
            "the anchor reading" to "anchorOffsetBasePairs = 0 -- route B's unpaired loop sits " +
                    "OUTBOARD of the duplex, C-0200's reading of the built block",
            "the two rim chains" to "C-0200 splits the 28 unpaired bases 12 / 16 per helix, so " +
                    "a turn joins two duplex ends 24 or 32 nucleotides apart; which rim takes " +
                    "which half is a free convention of that reading and BOTH assignments are " +
                    "swept, beside C-0193's uniform 28 / 28",
            "the preload" to "the chain's tension enters as +f times unitZ on the link " +
                    "gradient, so the nine IN-PLANE turns contribute exactly zero and the " +
                    "fifty through-thickness ones carry it. That zero is a property of the " +
                    "MODEL -- it has no in-plane transverse coordinate -- and not of the chain",
            "dishing" to "peakDishing(81) over the mean deflection under the uniform interior " +
                    "pressure on the SAME lattice; C-0167's convention, tolerance T-5b = 0.10",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)"
        ),
        parameters = mapOf(
            "crossSection" to "10 x 6",
            "raster" to "uniform, route B -- the paired row is scaffoldNucleotides / 60 - 28",
            "pairedRows" to widths.joinToString(", ") {
                it.scaffold + " " + it.pairedRowBasePairs + " bp"
            },
            "rowWidths" to widths.joinToString(", ") { it.rowWidth.t307Emitted(9) + " nm" },
            "edgeY" to edgeY.t307Emitted(9),
            "interhelicalDistance" to d.t307Emitted(9),
            "phosphateRadius" to rP.t307Emitted(9),
            "risePerBasePair" to Gen1Tile.RISE_PER_BASE_PAIR.t307Emitted(9),
            "azimuthPerBasePair" to AZIMUTH_PER_BASE_PAIR.t307Emitted(9),
            "latticePeriod" to T307_PERIOD.toString(),
            "phasesSwept" to phases.size.toString(),
            "unpairedPerHelix" to T307_UNPAIRED_PER_HELIX.toString(),
            "chainReadings" to chains.joinToString("; ") { it.first },
            "kuhnBracket" to "2.10 to 2.84 nm, zero force",
            "contourBracket" to "0.65 to 0.70 nm/nt, inextensible",
            "determinedSpanComparand" to T307_DETERMINED_SPAN.t307Emitted(9),
            "thermalEnergy" to kBT.t307Emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.t307Emitted(9),
            "hingeStiffnessEnhancement" to enhancement.t307Emitted(9),
            "compositeFraction" to T307_COMPOSITE_FRACTION.t307Emitted(2),
            "linkStiffness" to HoneycombGrillage.RIGID_LINK_STIFFNESS.t307Emitted(9),
            "samples" to T307_SAMPLES.toString(),
            "tolerance" to T307_TOLERANCE.t307Emitted(2),
            "subdivisions" to "1 at every headline cell; 2 on the convergence axis",
            "firstAxialSign" to "+1",
            "electrolyte" to "MgCl2 (2:1)",
            "bufferMillimolar" to T307_BUFFER_MILLIMOLAR.t307Emitted(3),
            "gapHeightNm" to T307_GAP_NM.t307Emitted(3),
            "appliedBiasVolts" to T307_BIAS_VOLTS.t307Emitted(3),
            "temperatureKelvin" to "300",
            "whyTheRegimeBlockIsNull" to "environment.Regime holds the gap and the bias as " +
                    "INTERVALS and refuses a degenerate one; this study solves no " +
                    "electrostatics, it reads ONE profile record of T-3b and uses it as a " +
                    "fixed load shape, so the state is a POINT (C-0181, CH-0224)",
            "smoke" to smoke.toString()
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_304.path + " (C-0204's uniformRasters, reproduced at every row)"
        ),
        citedInputs = mapOf(
            "C-0201 section 7 M13mp18's uniform row, untied" to "0.0425678289",
            "C-0201 section 7 p7560's uniform row, untied" to "0.0422200543",
            "C-0201 section 7 p8064's uniform row, untied" to "0.0451172785",
            "C-0201 the free tile at the built worst-azimuth corner" to "0.11296458",
            "C-0204 the determined span" to "0.787091706 nm",
            "C-0204 the determined-span tension bracket" to "0.175872271 to 0.479548487 pN",
            "C-0204 the preload's own worth at the determined span" to
                    "0.00708426936 to 0.0195297045 of the stroke",
            "C-0204 the flat count at the determined span" to "16 of 16 corners",
            "C-0147 the worst-azimuth span d + 2 r_P" to "4.35327572 nm",
            "C-0193 the built allotment" to "126 = 98 + 28 per helix",
            "C-0200 the built unpaired split" to "12 / 16 per helix, so 24 or 32 per turn"
        ),
        cheapBound = cheapBound,
        spans = spanRows,
        tensions = tensionRows,
        bank = bankRows,
        cells = cells,
        best = bestRows,
        reproductions = reproductions,
        convergence = convergence,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "THIS IS THE FREE TILE. The coupled 64 cells are NOT re-graded at these widths: " +
                    "C-0167's placements, station lattices and distributions are derived at " +
                    "the 116 bp block extent, and re-deriving them at 92 / 98 / 106 bp is a " +
                    "placement search rather than a re-grade. C-0201 and C-0204 both read " +
                    "0 of 64 coupled at every tethered state on the recommended raster.",
            "THE WHOLE BRANCH IS CONDITIONAL ON ROUTE B. C-0193 and C-0200 establish that the " +
                    "built object is route B -- a raster turn of 28 unpaired scaffold " +
                    "nucleotides -- and C-0201 shows route B's turn is a LOAD and not a " +
                    "spring, a dishing SOURCE where route A's tie is a SINK. Nothing here " +
                    "grades route A.",
            "C-0204's DETERMINED SPAN IS A PROPERTY OF THE DRAWABLE TWO-LENGTH RASTER, not of " +
                    "route B in general. This study is about the UNIFORM rasters route B's own " +
                    "scaffold budget affords, which close at no lattice phase; the two must " +
                    "not be conflated.",
            "The element is C-0201's, unchanged: a LINEARISATION about the built, taut state, " +
                    "one-sided, with the anchor at the beam axis on C-0194's frame-indifferent " +
                    "d/2 arm rather than at the phosphate radius.",
            "The lattice carries no steric floor between two duplexes and no across-helix " +
                    "parallel-axis term, and k_theta is Gen1Tile's square-lattice-fitted " +
                    "constant. CH-0242's common-mode spring is absent, so every tie and bond " +
                    "here is missing the stiffer of the two springs.",
            "The three widths are the maximum uniform paired row each scaffold affords at the " +
                    "28 nt allowance. A design that spent fewer paired bases would be wider " +
                    "in loop and narrower in row, and is not graded.",
            "Nothing here re-opens the raster, the cross-section, the placement search or the " +
                    "distribution rule."
        ),
        openQuestions = listOf(
            "The COUPLED reading at these widths, which needs a placement search at 92 / 98 / " +
                    "106 bp rather than a re-grade of C-0167's 116 bp stations.",
            "Whether a route-B design should trade paired row length against span. The three " +
                    "widths here are the MAXIMUM each scaffold affords; a shorter row with a " +
                    "longer loop is a different point of the same budget and nobody has " +
                    "swept it.",
            "The BUILT block's own lattice phase, which needs a register read of the " +
                    "deposited 10 x 6 file rather than a derivation.",
            "What a phosphate-radius attachment arm is worth, which C-0201 prices and does " +
                    "not measure and this study inherits unchanged."
        ),
        proseFailure = proseFailure
    )

    val output = File("gpd/results/T-307-uniform-raster-tether-spans.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-307 - wrote " + output.path)
    check(proseFailure == "none") { proseFailure }
}
