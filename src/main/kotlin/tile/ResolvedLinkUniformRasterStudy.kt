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
import com.xemantic.nano.plentyofroom.electrostatics.MengMagnesium
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
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
// T-315 -- route B's uniform raster re-graded at the RESOLVED PER-BOND link.
//
// C-0207 graded 756 cells at k_link = 1e4 pN/nm -- RIGID_LINK_STIFFNESS, a numerical PENALTY
// 39.2452209x above every rung a crossover connector can supply.  C-0208 has since resolved a
// bond's normal link by the bond's own line of centres, k_radial unitZ^2 + k_transverse unitY^2,
// so the same lattice's staple bonds sit at 254.808095 pN/nm in plane and 629.20588 to
// 1365.32644 through the thickness.  CH-0265 asks whether 756 of 756 survives that.
//
// The cheap bound needs NO solve.  CH-0265's adverse direction is read off a COUPLED p90 under
// measured dropout; this study grades the FREE tile, and C-0194 / T-297's committed six-decade
// link sweep carries a free-tile row at THIS study's own cross-section and enhancement.  One
// division per committed row gives a crossing threshold on C-0207's own cells, and it is emitted
// before the grading section runs.
// ---------------------------------------------------------------------------------------------

private const val T315_SAMPLES: Int = 81
private const val T315_TOLERANCE: Double = 0.10
private const val T315_RIM_STANDOFF: Double = 1.0
private const val T315_BUFFER_MILLIMOLAR: Double = 2.0
private const val T315_GAP_NM: Double = 10.0
private const val T315_BIAS_VOLTS: Double = 0.192
private const val T315_HELICES: Int = 60
private const val T315_UNPAIRED_PER_HELIX: Int = 28
private const val T315_M13: Int = 7249
private const val T315_P7560: Int = 7560
private const val T315_P8064: Int = 8064
private const val T315_ORDERED_LOW: Int = 24
private const val T315_ORDERED_HIGH: Int = 32
private const val T315_PERIOD: Int = 21
private const val T315_IDENTITY: Double = 1e-9
private const val T315_COMPOSITE_FRACTION: Double = 0.30
private const val T315_CONTACT_BP: Double = 21.0
private const val T315_BLOCK_EXTENT_BP: Int = 116

/** `C-0207`'s own link, `HoneycombGrillage.RIGID_LINK_STIFFNESS`, named for the control rung. */
private const val T315_PENALTY: Double = 10_000.0

/** The softest rung `T-297`'s committed free-tile sweep carries, in pN/nm. */
private const val T315_T297_SOFTEST: Double = 41.4338953

private val T315_KUHN: List<Double> = listOf(2.10, 2.84)
private val T315_CONTOUR: List<Double> = listOf(0.65, 0.70)

private fun Double.t315Emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ records

@Serializable
private data class T315CheapBoundRow(
    val stage: String,
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private data class T315Rung(
    val name: String,
    val ground: String,
    val transverseLinkStiffness: Double,
    val radialLinkStiffness: Double?,
    val inPlaneLinkStiffness: Double,
    val throughThicknessLinkStiffness: Double,
    val overThePenalty: Double
)

@Serializable
private data class T315BondCensus(
    val rung: String,
    val pairedRowBasePairs: Int,
    val crossoverPlanes: Int,
    val totalBonds: Int,
    val inPlaneBonds: Int,
    val throughThicknessBonds: Int,
    val meanSquaredUnitZInPlane: Double,
    val meanSquaredUnitZThroughThickness: Double,
    val worstInPlaneDeparture: Double,
    val worstThroughThicknessDeparture: Double,
    val distinctLinkStiffnessCount: Int
)

@Serializable
private data class T315Untied(
    val rung: String,
    val scaffold: String,
    val pairedRowBasePairs: Int,
    val freeStroke: Double,
    val untiedDishing: Double,
    val overThePenaltyReading: Double,
    val uniformLoadDishing: Double
)

@Serializable
private data class T315Cell(
    val rung: String,
    val scaffold: String,
    val pairedRowBasePairs: Int,
    val classZeroResidue: Int,
    val chain: String,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val freeTileWithPreload: Double,
    val freeTileWithoutPreload: Double,
    val preloadMovement: Double,
    val penaltyReading: Double,
    val overThePenaltyReading: Double,
    val triangleCeiling: Double,
    val ceilingHonoured: Boolean,
    val flatWithPreload: Boolean,
    val flatWithoutPreload: Boolean
)

@Serializable
private data class T315Best(
    val rung: String,
    val scaffold: String,
    val pairedRowBasePairs: Int,
    val bestPhaseOnDishing: Int,
    val bestWorstCornerDishing: Double,
    val worstPhaseOnDishing: Int,
    val worstWorstCornerDishing: Double,
    val phaseWorth: Double,
    val flatCellCount: Int,
    val cellCount: Int,
    val penaltyBestPhase: Int,
    val theRecommendationHolds: Boolean
)

@Serializable
private data class T315Monotone(
    val cell: String,
    val rung: String,
    val throughThicknessLinkStiffness: Double,
    val freeTileWithPreload: Double
)

@Serializable
private data class T315Reproduction(
    val source: String,
    val quantity: String,
    val there: String,
    val here: String,
    val departure: Double,
    val closes: Boolean
)

@Serializable
private data class T315Convergence(
    val axis: String,
    val setting: String,
    val value: Double,
    val departure: Double?,
    val verdictSurvives: Boolean
)

@Serializable
private data class T315Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private data class T315Result(
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
    val cheapBound: List<T315CheapBoundRow>,
    val rungs: List<T315Rung>,
    val bondCensus: List<T315BondCensus>,
    val untied: List<T315Untied>,
    val cells: List<T315Cell>,
    val best: List<T315Best>,
    val monotonicity: List<T315Monotone>,
    val reproductions: List<T315Reproduction>,
    val convergence: List<T315Convergence>,
    val falsifiers: List<T315Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the load shape

private class T315Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T315_RIM_STANDOFF))
        )
}

private fun t315Profile(file: File): T315Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == T315_BUFFER_MILLIMOLAR &&
                    value("gapHeight") == T315_GAP_NM &&
                    value("appliedBias") == T315_BIAS_VOLTS
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T315Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * One uniform route-B width, at one rung of the link.
 *
 * The construction is `C-0207`'s own, so the penalty rung's cells are a control on the geometry
 * as well as on the code. The free stroke is read at **this** rung, on the untied lattice of the
 * same row: a uniform load translates a free tile rigidly, so it is link-independent and that is
 * asserted rather than assumed.
 */
private class T315Width(
    val scaffold: String,
    scaffoldNucleotides: Int,
    val profile: T315Profile,
    val block: HoneycombBlock,
    val edgeY: Double,
    val enhancement: Double
) {
    val pairedRowBasePairs: Int =
        maximumUniformRowLength(scaffoldNucleotides, T315_HELICES, T315_UNPAIRED_PER_HELIX)
    val rowWidth: Double = pairedRowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (rowWidth * edgeY)
    val pressureField: PressureField = profile.field(interiorPressure, rowWidth, edgeY)

    fun untiedAt(rung: ResolvedLinkRung): HoneycombGrillage = honeycombTiedLatticeAtResolvedLink(
        block = block,
        rowBasePairs = pairedRowBasePairs,
        enhancement = enhancement,
        tied = false,
        transverseLinkStiffness = rung.transverseLinkStiffness,
        radialLinkStiffness = rung.radialLinkStiffness
    )
}

@Suppress("LongMethod", "ComplexMethod", "LongParameterList", "NestedBlockDepth")
fun main() {
    val smoke = System.getenv("T315_SMOKE") != null
    val phases = if (smoke) listOf(0, 14) else (0 until T315_PERIOD).toList()
    val kBT = thermalEnergy(ROOM_TEMPERATURE)
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val kTheta = Gen1Tile.crossoverHingeStiffness()
    val block = HoneycombBlock(10, 6)
    val columnPitch = HoneycombCrossSectionGeometry.columnPitch(d)
    val rowPitch = HoneycombCrossSectionGeometry.rowPitch(d)
    val edgeY = 10 * rowPitch
    val enhancement = multiLayerRigidities(
        layers = 6,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = T315_COMPOSITE_FRACTION,
        layerSpacing = columnPitch
    ).realisedEnhancement
    val profile = t315Profile(ResultInputs.T_3B.file())

    // ---- the two constants, through the corpus's own functions rather than transcribed
    val shearCeiling = crossoverLinkStiffnessBracket(
        hingeStiffness = kTheta,
        phosphateRadius = rP,
        interhelicalDistance = d,
        thermalEnergy = kBT,
        softestPersistenceLength = 1.34 / 2.0,
        stiffestPersistenceLength = 2.84 / 2.0
    ).ceiling
    val radial = crossoverRadialLinkBracket(
        hingeStiffness = kTheta,
        phosphateRadius = rP,
        interhelicalDistance = d,
        relaxedStep = MeasuredBackbone.STEP_SOUTH,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS,
        equationOfState = MengMagnesium.equationOfState,
        contactLength = T315_CONTACT_BP * Gen1Tile.RISE_PER_BASE_PAIR
    )

    val penaltyRung = ResolvedLinkRung(
        name = "the penalty (C-0207's own reading)",
        ground = "HoneycombGrillage.RIGID_LINK_STIFFNESS, a numerical penalty",
        transverseLinkStiffness = T315_PENALTY,
        radialLinkStiffness = null
    )
    val resolvedCeilingRung = ResolvedLinkRung(
        name = "the resolved ceiling",
        ground = "C-0205's shear ceiling transverse, C-0208's radial bracket CEILING " +
                "(the duplex stretch modulus over the span, in parallel with the measured pair)",
        transverseLinkStiffness = shearCeiling,
        radialLinkStiffness = radial.ceiling
    )
    val resolvedFloorRung = ResolvedLinkRung(
        name = "the resolved floor",
        ground = "C-0205's shear ceiling transverse, C-0208's radial bracket FLOOR " +
                "(C-0194's implied step stiffness, in parallel with the measured pair)",
        transverseLinkStiffness = shearCeiling,
        radialLinkStiffness = radial.floor
    )
    val uniformShearRung = ResolvedLinkRung(
        name = "the uniform shear ceiling",
        ground = "C-0208's own first radial rung -- radial = transverse, the SOFTEST " +
                "defensible lattice, and what C-0205 bisected its uniform thresholds on",
        transverseLinkStiffness = shearCeiling,
        radialLinkStiffness = shearCeiling
    )
    val rungs = listOf(penaltyRung, resolvedCeilingRung, resolvedFloorRung, uniformShearRung)

    val widths = listOf(
        "M13mp18" to T315_M13, "p7560" to T315_P7560, "p8064" to T315_P8064
    ).let { if (smoke) it.take(1) else it }.map { (name, nucleotides) ->
        T315Width(name, nucleotides, profile, block, edgeY, enhancement)
    }

    val chains = listOf(
        Triple("28 nt at both rims (C-0193)", 28, 28),
        Triple("C-0200's ordered split: 24 nt low, 32 high", T315_ORDERED_LOW, T315_ORDERED_HIGH),
        Triple(
            "C-0200's ordered split exchanged: 32 nt low, 24 high",
            T315_ORDERED_HIGH, T315_ORDERED_LOW
        )
    )
    val corners = chains.flatMap { chain ->
        T315_KUHN.flatMap { b -> T315_CONTOUR.map { c -> Triple(chain, b, c) } }
    }

    fun tethersOf(
        width: T315Width, phase: Int, chain: Triple<String, Int, Int>, b: Double, c: Double
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

    // ================================= CHEAP BOUND -- no solve at all, before anything is graded
    println("T-315 - the cheap bound, with no solve: what a softer link can do to C-0207's cells")
    val t297 = Json.parseToJsonElement(ResultInputs.T_297.file().readText())
        .jsonObject.getValue("sweep").jsonArray.map { it.jsonObject }
        .filter { it.getValue("crossSection").jsonPrimitive.content == "10 x 6" }
    fun t297Row(link: Double) = t297.firstOrNull {
        abs(
            it.getValue("hingeStiffnessEnhancement").jsonPrimitive.content.toDouble() - enhancement
        ) < 1e-6 && it.getValue("linkStiffness").jsonPrimitive.content.toDouble() == link
    } ?: error("T-297 carries no 10 x 6 free-tile row at link " + link)
    fun t297Free(link: Double) =
        t297Row(link).getValue("freeDishingWithoutTies").jsonPrimitive.content.toDouble()
    val t297AtPenalty = t297Free(T315_PENALTY)
    val t297AtSoftest = t297Free(T315_T297_SOFTEST)
    val amplification = t297AtSoftest / t297AtPenalty
    val crossingThreshold = T315_TOLERANCE / amplification

    val t307Cells = Json.parseToJsonElement(ResultInputs.T_307.file().readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
    fun t307Value(record: JsonObject, name: String) =
        record.getValue(name).jsonPrimitive.content.toDouble()
    val t307Readings = t307Cells.map { t307Value(it, "freeTileWithPreload") }
    val couldCross = t307Readings.count { it > crossingThreshold }
    val worstT307 = t307Readings.max()
    val slack = crossingThreshold / worstT307
    println(
        "  T-297's own 10 x 6 free tile at this enhancement moves " +
                t297AtPenalty.t315Emitted(9) + " to " + t297AtSoftest.t315Emitted(9) +
                " over six decades, an amplification of " + amplification.t315Emitted(9) +
                "x at " + T315_T297_SOFTEST.t315Emitted(9) + " pN/nm -- a rung " +
                (shearCeiling / T315_T297_SOFTEST).t315Emitted(9) +
                "x BELOW the softest bond this study contains"
    )
    println(
        "  so a C-0207 cell can only cross T-5b if it reads above " +
                crossingThreshold.t315Emitted(9) + ", and " + couldCross + " of " +
                t307Readings.size + " do; the worst is " + worstT307.t315Emitted(9) +
                ", which keeps " + slack.t315Emitted(9) + "x of slack"
    )
    val prediction = if (couldCross == 0)
        "the count SURVIVES at " + t307Readings.size + " of " + t307Readings.size
    else couldCross.toString() + " cells could cross"
    println("  PREDICTION, recorded before the grading section runs: " + prediction)

    // ================================= the bond census, per width and per rung
    println("T-315 - the bond census: the 116 bp block's 135 / 300 does NOT transfer")
    val bondCensus = ArrayList<T315BondCensus>()
    widths.forEach { width ->
        rungs.forEach { rung ->
            val lattice = width.untiedAt(rung)
            val census = ResolvedLinkBondCensus(lattice, rung)
            bondCensus += T315BondCensus(
                rung = rung.name,
                pairedRowBasePairs = width.pairedRowBasePairs,
                crossoverPlanes = lattice.planeBasePairs.size,
                totalBonds = census.totalBonds,
                inPlaneBonds = census.inPlaneBonds,
                throughThicknessBonds = census.throughThicknessBonds,
                meanSquaredUnitZInPlane = census.meanSquaredUnitZInPlane,
                meanSquaredUnitZThroughThickness = census.meanSquaredUnitZThroughThickness,
                worstInPlaneDeparture = census.worstInPlaneDeparture,
                worstThroughThicknessDeparture = census.worstThroughThicknessDeparture,
                distinctLinkStiffnessCount = census.distinctLinkStiffnessCount
            )
        }
    }
    widths.forEach { width ->
        val here = bondCensus.first { it.pairedRowBasePairs == width.pairedRowBasePairs }
        println(
            "  " + width.pairedRowBasePairs + " bp  " + here.crossoverPlanes + " planes  " +
                    here.totalBonds + " bonds = " + here.inPlaneBonds + " in plane + " +
                    here.throughThicknessBonds + " through the thickness"
        )
    }

    // ================================= the untied control at every rung
    println("T-315 - the untied control, three widths at each rung")
    val untiedRows = ArrayList<T315Untied>()
    val strokeOf = HashMap<Pair<Int, String>, Double>()
    val untiedOf = HashMap<Pair<Int, String>, Double>()
    widths.forEach { width ->
        rungs.forEach { rung ->
            val lattice = width.untiedAt(rung)
            val uniform = lattice.solve(uniformPressure(width.interiorPressure))
            val stroke = uniform.meanDeflection
            val dishing = lattice.solve(width.pressureField).peakDishing(T315_SAMPLES) / stroke
            strokeOf[width.pairedRowBasePairs to rung.name] = stroke
            untiedOf[width.pairedRowBasePairs to rung.name] = dishing
            untiedRows += T315Untied(
                rung = rung.name,
                scaffold = width.scaffold,
                pairedRowBasePairs = width.pairedRowBasePairs,
                freeStroke = stroke,
                untiedDishing = dishing,
                overThePenaltyReading = dishing /
                        untiedOf.getValue(width.pairedRowBasePairs to penaltyRung.name),
                // a quantity whose every digit is noise -- the exact answer is zero and the
                // JIT moves the last ulp between runs -- so it is emitted at TWO significant
                // digits with no floor, where the movement cannot reach it (CLAUDE.md)
                uniformLoadDishing = abs(uniform.peakDishing(T315_SAMPLES) / stroke)
                    .roundedForProse(2, floor = 0.0)
            )
        }
    }
    untiedRows.filter { it.rung != penaltyRung.name }.forEach {
        println(
            "  " + it.pairedRowBasePairs + " bp  " + it.rung + "  untied " +
                    it.untiedDishing.t315Emitted(9) + "  " +
                    it.overThePenaltyReading.t315Emitted(9) + "x the penalty reading"
        )
    }

    // ================================= the unit-tension bank, per width and per rung
    println("T-315 - the C-0104 bank, 59 unit-tension solves per (width, rung)")
    val turns = honeycombRasterTurnList(block)
    val banks = HashMap<Pair<Int, String>, List<Double>>()
    widths.forEach { width ->
        rungs.forEach { rung ->
            val reference = tethersOf(width, phases.first(), chains[0], T315_KUHN[0], T315_CONTOUR[0])
            val nodes = width.untiedAt(rung).nodesPerBeam
            val stroke = strokeOf.getValue(width.pairedRowBasePairs to rung.name)
            banks[width.pairedRowBasePairs to rung.name] = turns.indices.map { k ->
                val lattice = reference.latticeAtRung(rung, enhancement) {
                    reference.unitTensionElements(nodes, k)
                }
                lattice.solve().peakDishing(T315_SAMPLES) / stroke
            }
        }
    }

    // ================================= the re-grade
    println("T-315 - the re-grade: " + (widths.size * phases.size * corners.size) + " cells per rung")
    val cells = ArrayList<T315Cell>()
    val penaltyOf = HashMap<String, Double>()
    fun key(width: T315Width, phase: Int, chain: String, b: Double, c: Double) =
        width.pairedRowBasePairs.toString() + "|" + phase + "|" + chain + "|" + b + "|" + c
    rungs.forEach { rung ->
        widths.forEach { width ->
            val bank = banks.getValue(width.pairedRowBasePairs to rung.name)
            val stroke = strokeOf.getValue(width.pairedRowBasePairs to rung.name)
            val ceilingBase = untiedOf.getValue(width.pairedRowBasePairs to rung.name)
            phases.forEach { phase ->
                corners.forEach { (chain, b, c) ->
                    val subject = tethersOf(width, phase, chain, b, c)
                    val loaded = subject.latticeAtRung(rung, enhancement, withPreload = true)
                    val unloaded = subject.latticeAtRung(rung, enhancement, withPreload = false)
                    val withPreload =
                        loaded.solve(width.pressureField).peakDishing(T315_SAMPLES) / stroke
                    val withoutPreload =
                        unloaded.solve(width.pressureField).peakDishing(T315_SAMPLES) / stroke
                    val cellKey = key(width, phase, chain.first, b, c)
                    if (rung === penaltyRung) penaltyOf[cellKey] = withPreload
                    val ceiling = ceilingBase +
                            subject.states.indices.sumOf {
                                abs(subject.states[it].tension) * bank[it]
                            }
                    cells += T315Cell(
                        rung = rung.name,
                        scaffold = width.scaffold,
                        pairedRowBasePairs = width.pairedRowBasePairs,
                        classZeroResidue = phase,
                        chain = chain.first,
                        kuhnLength = b,
                        contourPerNucleotide = c,
                        freeTileWithPreload = withPreload,
                        freeTileWithoutPreload = withoutPreload,
                        preloadMovement = withPreload - withoutPreload,
                        penaltyReading = penaltyOf.getValue(cellKey),
                        overThePenaltyReading = withPreload / penaltyOf.getValue(cellKey),
                        triangleCeiling = ceiling,
                        ceilingHonoured = withPreload < ceiling + T315_IDENTITY,
                        flatWithPreload = withPreload < T315_TOLERANCE,
                        flatWithoutPreload = withoutPreload < T315_TOLERANCE
                    )
                }
            }
        }
        val here = cells.filter { it.rung == rung.name }
        println(
            "  " + rung.name + "  in plane " + rung.inPlaneLinkStiffness.t315Emitted(9) +
                    ", through " + rung.throughThicknessLinkStiffness.t315Emitted(9) +
                    " pN/nm  ->  " + here.count { it.flatWithPreload } + " of " + here.size +
                    " flat, " + here.minOf { it.freeTileWithPreload }.t315Emitted(9) + " to " +
                    here.maxOf { it.freeTileWithPreload }.t315Emitted(9) + " of the stroke"
        )
    }

    // ================================= the phase recommendation, per rung
    val bestRows = ArrayList<T315Best>()
    rungs.forEach { rung ->
        widths.forEach { width ->
            val here = cells.filter {
                it.rung == rung.name && it.pairedRowBasePairs == width.pairedRowBasePairs
            }
            val byPhase = phases.associateWith { phase ->
                here.filter { it.classZeroResidue == phase }.maxOf { it.freeTileWithPreload }
            }
            val bestPhase = byPhase.minBy { it.value }.key
            val worstPhase = byPhase.maxBy { it.value }.key
            val penaltyBest = bestRows.firstOrNull {
                it.rung == penaltyRung.name && it.pairedRowBasePairs == width.pairedRowBasePairs
            }?.bestPhaseOnDishing ?: bestPhase
            bestRows += T315Best(
                rung = rung.name,
                scaffold = width.scaffold,
                pairedRowBasePairs = width.pairedRowBasePairs,
                bestPhaseOnDishing = bestPhase,
                bestWorstCornerDishing = byPhase.getValue(bestPhase),
                worstPhaseOnDishing = worstPhase,
                worstWorstCornerDishing = byPhase.getValue(worstPhase),
                phaseWorth = byPhase.getValue(worstPhase) / byPhase.getValue(bestPhase),
                flatCellCount = here.count { it.flatWithPreload },
                cellCount = here.size,
                penaltyBestPhase = penaltyBest,
                theRecommendationHolds = bestPhase == penaltyBest
            )
        }
    }

    // ================================= monotonicity, at the deciding cell
    val deciding = cells.filter { it.rung == penaltyRung.name }
        .minBy { abs(it.freeTileWithPreload - T315_TOLERANCE) }
    val monotonicity = rungs.map { rung ->
        val here = cells.first {
            it.rung == rung.name &&
                    it.pairedRowBasePairs == deciding.pairedRowBasePairs &&
                    it.classZeroResidue == deciding.classZeroResidue &&
                    it.chain == deciding.chain &&
                    it.kuhnLength == deciding.kuhnLength &&
                    it.contourPerNucleotide == deciding.contourPerNucleotide
        }
        T315Monotone(
            cell = deciding.pairedRowBasePairs.toString() + " bp phase " +
                    deciding.classZeroResidue + ", " + deciding.chain + ", b = " +
                    deciding.kuhnLength.t315Emitted(3) + ", c = " +
                    deciding.contourPerNucleotide.t315Emitted(3),
            rung = rung.name,
            throughThicknessLinkStiffness = rung.throughThicknessLinkStiffness,
            freeTileWithPreload = here.freeTileWithPreload
        )
    }.sortedBy { it.throughThicknessLinkStiffness }
    val monotone = monotonicity.zipWithNext().all {
        it.first.freeTileWithPreload >= it.second.freeTileWithPreload - T315_IDENTITY
    }
    println(
        "T-315 - monotonicity at the deciding cell: " + monotonicity.joinToString(", ") {
            it.throughThicknessLinkStiffness.t315Emitted(6) + " -> " +
                    it.freeTileWithPreload.t315Emitted(9)
        } + (if (monotone) "  MONOTONE" else "  NOT MONOTONE")
    )

    // ================================= convergence, at the deciding cell of the SOFTEST rung
    println("T-315 - convergence, on the deciding quantity at the deciding cell")
    val decidingRung = uniformShearRung
    val decidingWidth = widths.first { it.pairedRowBasePairs == deciding.pairedRowBasePairs }
    val decidingChain = chains.first { it.first == deciding.chain }
    val decidingAtRung = cells.first {
        it.rung == decidingRung.name &&
                it.pairedRowBasePairs == deciding.pairedRowBasePairs &&
                it.classZeroResidue == deciding.classZeroResidue &&
                it.chain == deciding.chain &&
                it.kuhnLength == deciding.kuhnLength &&
                it.contourPerNucleotide == deciding.contourPerNucleotide
    }
    val convergence = ArrayList<T315Convergence>()
    listOf(41, 81, 161).forEach { samples ->
        val subject = tethersOf(
            decidingWidth, deciding.classZeroResidue, decidingChain,
            deciding.kuhnLength, deciding.contourPerNucleotide
        )
        val stroke = strokeOf.getValue(decidingWidth.pairedRowBasePairs to decidingRung.name)
        val value = subject.latticeAtRung(decidingRung, enhancement)
            .solve(decidingWidth.pressureField).peakDishing(samples) / stroke
        convergence += T315Convergence(
            axis = "the dishing sample grid at the deciding cell, at the softest rung",
            setting = samples.toString() + " samples",
            value = value,
            departure = if (samples == T315_SAMPLES) null
            else abs(value - decidingAtRung.freeTileWithPreload),
            verdictSurvives = (value < T315_TOLERANCE) == decidingAtRung.flatWithPreload
        )
    }
    listOf(1, 2).forEach { subdivisions ->
        val subject = tethersOf(
            decidingWidth, deciding.classZeroResidue, decidingChain,
            deciding.kuhnLength, deciding.contourPerNucleotide
        )
        val lattice = subject.latticeAtRung(decidingRung, enhancement, subdivisions = subdivisions)
        val bare = honeycombTiedLatticeAtResolvedLink(
            block, decidingWidth.pairedRowBasePairs, enhancement, tied = false,
            transverseLinkStiffness = decidingRung.transverseLinkStiffness,
            radialLinkStiffness = decidingRung.radialLinkStiffness,
            subdivisions = subdivisions
        )
        val stroke = bare.solve(uniformPressure(decidingWidth.interiorPressure)).meanDeflection
        val value = lattice.solve(decidingWidth.pressureField).peakDishing(T315_SAMPLES) / stroke
        convergence += T315Convergence(
            axis = "beam subdivision at the deciding cell, at the softest rung",
            setting = subdivisions.toString() + " element(s) per plane",
            value = value,
            departure = if (subdivisions == 1) null
            else abs(value - convergence.first { it.setting == "1 element(s) per plane" }.value),
            verdictSurvives = (value < T315_TOLERANCE) == decidingAtRung.flatWithPreload
        )
    }
    convergence.forEach {
        println("  " + it.axis + "  " + it.setting + "  " + it.value.t315Emitted(9))
    }

    // ================================= reproductions
    val reproductions = ArrayList<T315Reproduction>()
    fun reproduce(source: String, quantity: String, there: Double, here: Double) {
        val departure = abs(here - there) / maxOf(abs(there), 1e-30)
        reproductions += T315Reproduction(
            source = source, quantity = quantity,
            there = there.t315Emitted(9), here = here.t315Emitted(9),
            departure = departure, closes = departure < 1e-8
        )
    }
    reproduce("C-0205 (T-303) section 1", "the shear ceiling, in pN/nm", 254.808095, shearCeiling)
    reproduce(
        "C-0208 (T-310) section 1a", "the radial bracket floor, in pN/nm",
        754.005141, radial.floor
    )
    reproduce(
        "C-0208 (T-310) section 1a", "the radial bracket ceiling, in pN/nm",
        1735.49922, radial.ceiling
    )
    reproduce(
        "C-0208 (T-310) section 1", "the resolved through-thickness link at the radial floor",
        629.20588, resolvedFloorRung.throughThicknessLinkStiffness
    )
    reproduce(
        "C-0208 (T-310) section 1", "the resolved through-thickness link at the radial ceiling",
        1365.32644, resolvedCeilingRung.throughThicknessLinkStiffness
    )
    reproduce(
        "CH-0265", "the penalty over the shear ceiling", 39.2452209, T315_PENALTY / shearCeiling
    )
    val untiedThere = mapOf(92 to 0.0425678289, 98 to 0.0422200543, 106 to 0.0451172785)
    widths.forEach { width ->
        reproduce(
            "C-0201 (T-299) section 7, through C-0207",
            width.scaffold + "'s uniform row, uncoupled dishing at the penalty",
            untiedThere.getValue(width.pairedRowBasePairs),
            untiedOf.getValue(width.pairedRowBasePairs to penaltyRung.name)
        )
    }
    // the penalty control against C-0207's committed 756 cells, cell by cell
    var worstControlDeparture = 0.0
    var controlRowsClosing = 0
    val controlCells = cells.filter { it.rung == penaltyRung.name }
    controlCells.forEach { cell ->
        val there = t307Cells.firstOrNull {
            it.getValue("pairedRowBasePairs").jsonPrimitive.content.toInt() ==
                    cell.pairedRowBasePairs &&
                    it.getValue("classZeroResidue").jsonPrimitive.content.toInt() ==
                    cell.classZeroResidue &&
                    it.getValue("chain").jsonPrimitive.content == cell.chain &&
                    t307Value(it, "kuhnLength") == cell.kuhnLength &&
                    t307Value(it, "contourPerNucleotide") == cell.contourPerNucleotide
        } ?: error("C-0207 carries no cell at " + cell.pairedRowBasePairs + " bp phase " + cell.classZeroResidue)
        val departure = abs(t307Value(there, "freeTileWithPreload") - cell.freeTileWithPreload)
        if (departure < 1e-8) controlRowsClosing++
        worstControlDeparture = maxOf(worstControlDeparture, departure)
    }
    reproductions += T315Reproduction(
        source = "C-0207 (T-307) cells",
        quantity = "the free tile with the preload at the PENALTY rung, cell by cell",
        there = controlCells.size.toString() + " cells",
        here = controlRowsClosing.toString() + " agree to 1e-8",
        departure = worstControlDeparture,
        closes = controlRowsClosing == controlCells.size
    )
    reproductions.forEach {
        println(
            "  " + it.source + "  " + it.quantity + "  departure " + it.departure.t315Emitted(2) +
                    (if (it.closes) "  closes" else "  DOES NOT CLOSE")
        )
    }

    // ================================= the falsifiers
    println("T-315 - the falsifiers")
    val uniformDishings = widths.map { width ->
        val subject = tethersOf(width, phases.first(), chains[0], T315_KUHN[0], T315_CONTOUR[0])
        val lattice = subject.latticeAtRung(resolvedFloorRung, enhancement, withPreload = false)
        val field = lattice.solve(uniformPressure(width.interiorPressure))
        width.pairedRowBasePairs to abs(field.peakDishing(T315_SAMPLES) / field.meanDeflection)
    }
    val loadIdentical = widths.all { width ->
        val subject = tethersOf(width, phases.first(), chains[0], T315_KUHN[0], T315_CONTOUR[0])
        val standing = subject.lattice(enhancement = enhancement)
        val here = subject.latticeAtRung(penaltyRung, enhancement)
        val a = standing.assembleLoad(width.pressureField)
        val b = here.assembleLoad(width.pressureField)
        standing.bonds.map { it.site } == here.bonds.map { it.site } &&
                (0 until standing.degreesOfFreedom).all { a[it] == b[it] }
    }
    // F4's DECLARED form asks for a two-valued census at every rung that is not the penalty.
    // That is wrong about the fourth rung by construction -- radial = transverse makes the two
    // readings ONE number -- so both readings are emitted rather than one being picked
    // (CLAUDE.md: a pre-registered criterion can still be arithmetically wrong).
    fun expectedDistinct(rung: ResolvedLinkRung): Int =
        if (Math.round(rung.inPlaneLinkStiffness / 1e-9) ==
            Math.round(rung.throughThicknessLinkStiffness / 1e-9)
        ) 1 else 2
    val censusGeometrySound = bondCensus.all {
        it.meanSquaredUnitZInPlane == 0.0 &&
                (it.throughThicknessBonds == 0 ||
                        abs(it.meanSquaredUnitZThroughThickness - 0.75) < 1e-12) &&
                it.worstInPlaneDeparture < 1e-9 &&
                it.worstThroughThicknessDeparture < 1e-9
    }
    val censusAsDeclared = bondCensus.all {
        it.distinctLinkStiffnessCount == (if (it.rung == penaltyRung.name) 1 else 2)
    }
    val censusCorrected = bondCensus.all { row ->
        row.distinctLinkStiffnessCount ==
                expectedDistinct(rungs.first { it.name == row.rung })
    }
    val declaredOffenders = bondCensus.filter {
        it.distinctLinkStiffnessCount != (if (it.rung == penaltyRung.name) 1 else 2)
    }
    val resolvedRungs = rungs.filter { it !== penaltyRung }
    val resolvedCells = cells.filter { cell -> resolvedRungs.any { it.name == cell.rung } }
    val floorCells = cells.filter { it.rung == resolvedFloorRung.name }
    val brokeThePrediction = cells.any {
        it.rung != penaltyRung.name && !it.flatWithPreload &&
                it.penaltyReading < crossingThreshold
    }
    val recommendationMoves = bestRows.any { !it.theRecommendationHolds }
    val tetherUnreached = widths.all { width ->
        val subject = tethersOf(width, phases.first(), chains[0], T315_KUHN[0], T315_CONTOUR[0])
        val nodes = width.untiedAt(penaltyRung).nodesPerBeam
        val reference = subject.elements(nodes)
        rungs.all { rung ->
            val here = subject.elements(width.untiedAt(rung).nodesPerBeam)
            here.size == reference.size && here.indices.all { k ->
                here[k].secantStiffness == reference[k].secantStiffness &&
                        here[k].tangentStiffness == reference[k].tangentStiffness &&
                        here[k].tension == reference[k].tension &&
                        here[k].node == reference[k].node
            }
        }
    }

    val falsifiers = listOf(
        T315Falsifier(
            "F1", "a uniform pressure on the free tethered lattice AT THE RESOLVED LINK, " +
                    "preload off, dishes more than 1e-9 of the stroke at any of 92 / 98 / 106 bp",
            uniformDishings.any { it.second > T315_IDENTITY },
            "peak dishing over stroke " + uniformDishings.joinToString(", ") {
                it.first.toString() + " bp: " + it.second.t315Emitted(2)
            } + ". 92 and 106 are NOT multiples of the 7 bp crossover-plane pitch, so this is " +
                    "the case HoneycombGrillage.nodeS's free-overhang branch exists for"
        ),
        T315Falsifier(
            "F2", "the penalty control rung fails to reproduce C-0207's committed " +
                    "freeTileWithPreload at all " + controlCells.size + " cells to 1e-8",
            controlRowsClosing != controlCells.size,
            controlRowsClosing.toString() + " of " + controlCells.size + " cells agree; worst " +
                    "departure " + worstControlDeparture.t315Emitted(2) + " of the stroke"
        ),
        T315Falsifier(
            "F3", "a lattice built through this task's own entry point at a null radial " +
                    "constant and the default link is NOT bit-identical to " +
                    "UniformRasterTethers.lattice's, on assembleLoad over every degree of " +
                    "freedom or on the crossover site set, at some row length",
            !loadIdentical,
            "the load vector is bit-identical at all three row lengths over every degree of " +
                    "freedom and the bond site set agrees; every band entry of the assembled " +
                    "stiffness matrix is asserted equal in ResolvedLinkUniformRasterTest"
        ),
        T315Falsifier(
            "F4 as declared", "the bond census is not two-valued in the resolved link, or a " +
                    "bond's own link departs from its rung's reading by more than 1e-9 relative",
            !censusGeometrySound || !censusAsDeclared,
            bondCensus.size.toString() + " (width, rung) censuses; in plane <unitZ^2> is " +
                    "exactly " + bondCensus.maxOf { it.meanSquaredUnitZInPlane }.t315Emitted(2) +
                    " and through the thickness " +
                    bondCensus.maxOf { it.meanSquaredUnitZThroughThickness }.t315Emitted(9) +
                    "; worst departure " + bondCensus.maxOf {
                maxOf(it.worstInPlaneDeparture, it.worstThroughThicknessDeparture)
            }.t315Emitted(2) + ". The two-valued clause is refused at " +
                    declaredOffenders.size + " of " + bondCensus.size + " rows, all of them " +
                    "the UNIFORM SHEAR CEILING rung, whose radial and transverse constants are " +
                    "one number by construction -- so the declared clause is wrong about that " +
                    "rung and not about the lattice"
        ),
        T315Falsifier(
            "F4 corrected", "the bond census is not one-valued where a rung's two readings " +
                    "AGREE at the census's own 1e-9 quantisation and two-valued where they do " +
                    "not, or a bond's own link departs from its rung's reading by more than " +
                    "1e-9 relative",
            !censusGeometrySound || !censusCorrected,
            "every one of the " + bondCensus.size + " censuses takes the count its own rung's " +
                    "two readings imply: 1 at the penalty and at the uniform shear ceiling, 2 " +
                    "at the resolved floor and ceiling; every departure is exactly " +
                    bondCensus.maxOf {
                        maxOf(it.worstInPlaneDeparture, it.worstThroughThicknessDeparture)
                    }.t315Emitted(2) + " and <unitZ^2> is exactly 0 in plane and " +
                    bondCensus.maxOf { it.meanSquaredUnitZThroughThickness }.t315Emitted(9) +
                    " through the thickness"
        ),
        T315Falsifier(
            "F5", "the flat count at the RESOLVED FLOOR is not " + floorCells.size + " of " +
                    floorCells.size + " -- the deliverable's own question, declared OPEN",
            floorCells.count { it.flatWithPreload } != floorCells.size,
            floorCells.count { it.flatWithPreload }.toString() + " of " + floorCells.size +
                    " flat at the resolved floor, " +
                    floorCells.minOf { it.freeTileWithPreload }.t315Emitted(9) + " to " +
                    floorCells.maxOf { it.freeTileWithPreload }.t315Emitted(9) + " of the stroke"
        ),
        T315Falsifier(
            "F6", "the free-tile dishing at the deciding cell is NOT monotone in the link " +
                    "stiffness over the four rungs -- declared OPEN",
            !monotone,
            monotonicity.joinToString("; ") {
                it.throughThicknessLinkStiffness.t315Emitted(6) + " pN/nm -> " +
                        it.freeTileWithPreload.t315Emitted(9)
            }
        ),
        T315Falsifier(
            "F7", "the C-0104 triangle-inequality ceiling, rebuilt at each rung's own link, is " +
                    "exceeded by a measured dishing at some cell",
            cells.any { !it.ceilingHonoured },
            cells.count { it.ceilingHonoured }.toString() + " of " + cells.size +
                    " cells honour their own rung's ceiling; the worst ratio of measured to " +
                    "ceiling is " +
                    cells.maxOf { it.freeTileWithPreload / it.triangleCeiling }.t315Emitted(9)
        ),
        T315Falsifier(
            "F8", "a cell flat at the penalty is NOT flat at the resolved link although its " +
                    "penalty reading lies below the cheap bound's crossing threshold " +
                    crossingThreshold.t315Emitted(9) + " -- the cheap bound's own falsifier",
            brokeThePrediction,
            resolvedCells.count { it.flatWithPreload }.toString() + " of " + resolvedCells.size +
                    " resolved-rung cells are flat; the largest measured amplification over " +
                    "the penalty reading is " +
                    cells.maxOf { it.overThePenaltyReading }.t315Emitted(9) + "x against the " +
                    "cheap bound's assumed ceiling of " + amplification.t315Emitted(9) + "x"
        ),
        T315Falsifier(
            "F9", "the phase recommendation b0 = 5 / 16 / 9 (C-0207 section 3) changes at the " +
                    "resolved link -- declared OPEN",
            recommendationMoves,
            bestRows.filter { it.rung != penaltyRung.name }.joinToString("; ") {
                it.pairedRowBasePairs.toString() + " bp at " + it.rung + ": phase " +
                        it.bestPhaseOnDishing + " against the penalty's " + it.penaltyBestPhase
            }
        ),
        T315Falsifier(
            "F10", "the tether element list is not identical across the rungs -- the link " +
                    "resolution has reached the chain, which it must not",
            !tetherUnreached,
            "the 59 elements agree on secant, tangent, tension and node at every rung and " +
                    "every width; the resolution reaches bonds and ties only"
        )
    )
    falsifiers.forEach { println("  " + it.id + (if (it.fired) "  FIRED" else "  did not fire")) }

    // ================================= the prose
    val floorFlat = floorCells.count { it.flatWithPreload }
    val softest = cells.filter { it.rung == uniformShearRung.name }
    val cheapBound = listOf(
        T315CheapBoundRow(
            stage = "1 -- arithmetic on two committed result files, no solve",
            question = "how much can a softer link raise a FREE-tile dishing on this " +
                    "cross-section, and can that reach T-5b from C-0207's own readings?",
            answer = "C-0194 / T-297's committed six-decade link sweep carries a 10 x 6 " +
                    "free-tile row at THIS study's own enhancement, " +
                    enhancement.t315Emitted(9) + ": it reads " + t297AtPenalty.t315Emitted(9) +
                    " at the 1e4 penalty and " + t297AtSoftest.t315Emitted(9) + " at " +
                    T315_T297_SOFTEST.t315Emitted(9) + " pN/nm, an amplification of " +
                    amplification.t315Emitted(9) + "x -- and that rung is " +
                    (shearCeiling / T315_T297_SOFTEST).t315Emitted(9) + "x BELOW the softest " +
                    "bond any rung of this study contains, so it is a deliberate over-estimate",
            consequence = "a C-0207 cell can cross T-5b's 0.10 only if it reads above " +
                    crossingThreshold.t315Emitted(9) + ". " + couldCross + " of " +
                    t307Readings.size + " do. The worst cell in C-0207's whole census, " +
                    worstT307.t315Emitted(9) + ", keeps " + slack.t315Emitted(9) + "x of slack " +
                    "against that threshold, so the PREDICTION recorded before the grading ran " +
                    "was: " + prediction
        ),
        T315CheapBoundRow(
            stage = "2 -- why the prediction is not a theorem",
            question = "does the amplification transfer?",
            answer = "no, and it is stated as a prediction for three reasons. T-297's sweep is " +
                    "read on the 116 bp block, not on 92 / 98 / 106 bp; it carries route-A " +
                    "TIES or none, not 59 tethers with a preload; and it is a UNIFORM link " +
                    "where this study's is per-bond, which is CH-0264's own trap",
            consequence = "the uniform reading bounds the resolved one only if the dishing is " +
                    "monotone in the link, which is why the softest defensible lattice -- " +
                    "radial = transverse = the shear ceiling -- is graded as a fourth rung and " +
                    "monotonicity is MEASURED at the deciding cell (F6)"
        ),
        T315CheapBoundRow(
            stage = "3 -- and why CH-0265's own direction does not settle it either",
            question = "C-0205 section 4 reads 0 of 16 tethered cells flat with the p90 rising " +
                    "as the link softens. Why is that not the answer?",
            answer = "because it is a COUPLED p90 under C-0087's measured staple dropout, over " +
                    "4 000 realisations of a placement and a distribution, and this is the " +
                    "FREE tile. The two quantities differ by the whole coupling",
            consequence = "the direction transfers in SIGN and its size does not, which is " +
                    "exactly what a re-grade measures. C-0207 section 7's coupled reading at " +
                    "these widths is still open and still needs a placement search"
        )
    )

    var proseFailure = "none"
    val findings: List<String> = try {
        listOf(
            "C-0207'S " + controlCells.size + " OF " + controlCells.size + " " +
                    (if (floorFlat == floorCells.size) "SURVIVES" else "DOES NOT SURVIVE") +
                    " THE RESOLVED PER-BOND LINK, AND THE COUNT IS NOW QUOTED WITH THE LINK " +
                    "STIFFNESS ATTACHED. At C-0208's resolved link -- " +
                    resolvedFloorRung.inPlaneLinkStiffness.t315Emitted(9) + " pN/nm at the " +
                    bondCensus.minOf { it.inPlaneBonds } + " to " +
                    bondCensus.maxOf { it.inPlaneBonds } + " in-plane bonds and " +
                    resolvedFloorRung.throughThicknessLinkStiffness.t315Emitted(9) + " to " +
                    resolvedCeilingRung.throughThicknessLinkStiffness.t315Emitted(9) +
                    " pN/nm at the " + bondCensus.minOf { it.throughThicknessBonds } + " to " +
                    bondCensus.maxOf { it.throughThicknessBonds } + " that run " +
                    "through the thickness -- route B's three uniform paired rows read " +
                    resolvedCells.minOf { it.freeTileWithPreload }.t315Emitted(9) + " to " +
                    resolvedCells.maxOf { it.freeTileWithPreload }.t315Emitted(9) +
                    " of the stroke over " + resolvedCells.size + " cells, " +
                    resolvedCells.count { it.flatWithPreload } + " of them flat against " +
                    "T-5b's 0.10, where C-0207 read " +
                    controlCells.minOf { it.freeTileWithPreload }.t315Emitted(9) + " to " +
                    controlCells.maxOf { it.freeTileWithPreload }.t315Emitted(9) +
                    " at the penalty and this study reproduces all " + controlRowsClosing +
                    " of its cells at a worst departure of " +
                    worstControlDeparture.t315Emitted(2) + ".",
            "THE LINK IS WORTH AT MOST " +
                    cells.maxOf { it.overThePenaltyReading }.t315Emitted(9) +
                    "x OF THE READING, AND THE CHEAP BOUND SAID SO WITH NO SOLVE. The " +
                    "prediction recorded before the grading section ran was " + prediction +
                    ", from one division on C-0194 / T-297's committed free-tile link sweep: " +
                    "the largest amplification six decades of link can produce on this " +
                    "cross-section is " + amplification.t315Emitted(9) + "x, so nothing below " +
                    crossingThreshold.t315Emitted(9) + " can reach T-5b, and C-0207's worst " +
                    "cell is " + worstT307.t315Emitted(9) + ". Measured, the amplification is " +
                    cells.maxOf { it.overThePenaltyReading }.t315Emitted(9) + "x at its worst " +
                    "and the softest defensible lattice -- every bond at the shear ceiling -- " +
                    "reads " + softest.maxOf { it.freeTileWithPreload }.t315Emitted(9) +
                    " at its worst cell. F8 " + (if (brokeThePrediction) "FIRED" else
                        "did not fire") + ".",
            "AND THE REASON CH-0265'S DIRECTION DOES NOT DECIDE IT IS THAT ITS DIRECTION IS " +
                    "MEASURED ON A DIFFERENT QUANTITY. C-0205 section 4's 0 of 16 is a COUPLED " +
                    "p90 under C-0087's measured staple dropout; C-0207's 756 of 756 is a FREE " +
                    "tile. The sign transfers -- every resolved rung here reads " +
                    resolvedCells.minOf { it.overThePenaltyReading }.t315Emitted(9) + "x to " +
                    resolvedCells.maxOf { it.overThePenaltyReading }.t315Emitted(9) +
                    "x its own penalty cell -- and the " +
                    "size does not, because the free tile has no coupling to lose. The " +
                    "COUPLED reading at 92 / 98 / 106 bp remains open and still needs a " +
                    "placement search (C-0207 section 7).",
            "THE 116 bp BLOCK'S 135 / 300 BOND CENSUS DOES NOT TRANSFER, AND THE CROSSOVER " +
                    "PLANES ARE WHY. The planes are every 7 bp, so a shorter row carries fewer " +
                    "of them: " + bondCensus.filter { it.rung == penaltyRung.name }
                .joinToString("; ") {
                    it.pairedRowBasePairs.toString() + " bp carries " + it.crossoverPlanes +
                            " planes and " + it.totalBonds + " bonds = " + it.inPlaneBonds +
                            " + " + it.throughThicknessBonds
                } + ", against 435 = 135 + 300 at the " + T315_BLOCK_EXTENT_BP +
                    " bp block extent C-0208 censused. And the SPLIT moves too, not only the " +
                    "total: the in-plane share runs " +
                    bondCensus.minOf {
                        it.inPlaneBonds.toDouble() / it.totalBonds
                    }.t315Emitted(9) + " to " +
                    bondCensus.maxOf {
                        it.inPlaneBonds.toDouble() / it.totalBonds
                    }.t315Emitted(9) + " against " + (135.0 / 435.0).t315Emitted(9) +
                    " at 116 bp, because a row's crossover planes fall on the three bond " +
                    "classes in a proportion its own length decides. So the fraction of the " +
                    "lattice the RADIAL constant reaches is a function of the row length.",
            "THE PHASE RECOMMENDATION " + (
                    if (recommendationMoves) "MOVES" else "HOLDS"
                    ) + " AT THE RESOLVED LINK. C-0207 section 3 recommends b0 = " +
                    bestRows.filter { it.rung == penaltyRung.name }
                        .joinToString(" / ") { it.bestPhaseOnDishing.toString() } +
                    " at 92 / 98 / 106 bp on the minimax over the twelve chain corners; at the " +
                    "resolved floor the optimum is " + bestRows
                .filter { it.rung == resolvedFloorRung.name }
                .joinToString(" / ") { it.bestPhaseOnDishing.toString() } +
                    " and at the softest rung " + bestRows
                .filter { it.rung == uniformShearRung.name }
                .joinToString(" / ") { it.bestPhaseOnDishing.toString() } +
                    ". The phase is worth " +
                    bestRows.maxOf { it.phaseWorth }.t315Emitted(9) + "x at its most, and it " +
                    "costs nothing, because the phase is free. F9 " +
                    (if (recommendationMoves) "FIRED" else "did not fire") + ".",
            "F4 FIRED AS DECLARED AND ITS CORRECTION DOES NOT, AND THE FIRING IS ABOUT THIS " +
                    "STUDY'S OWN PREDICATE. The declared clause asks for a TWO-valued bond " +
                    "census at every rung that is not the penalty; the fourth rung sets the " +
                    "radial constant EQUAL to the transverse one, so its two readings are one " +
                    "number by construction and its census is one-valued at " +
                    declaredOffenders.size + " of " + bondCensus.size + " rows -- every one of " +
                    "them that rung. Read as it should have been written -- one-valued where a " +
                    "rung's two readings agree at the census's own 1e-9 quantisation and two " +
                    "where they do not -- it does not fire. Both readings are emitted rather " +
                    "than one being picked, and the geometry clause of the same falsifier is " +
                    "clean at every row: <unitZ^2> is exactly 0 in plane and " +
                    bondCensus.maxOf { it.meanSquaredUnitZThroughThickness }.t315Emitted(9) +
                    " through the thickness, and every link departure is exactly " +
                    bondCensus.maxOf {
                        maxOf(it.worstInPlaneDeparture, it.worstThroughThicknessDeparture)
                    }.t315Emitted(2) + "."
        )
    } catch (failure: Exception) {
        proseFailure = failure.toString()
        emptyList()
    }

    val result = T315Result(
        task = "T-315",
        leaf = "A8.2",
        title = "route B's uniform raster re-graded at C-0208's resolved per-bond link, and " +
                "the flat count restated with the link stiffness attached",
        verificationType = "logical (the link resolution is closed form and the bond census is " +
                "exact integer lattice geometry) + in-silico (the same honeycomb grillage, the " +
                "same T-299 tether element and the same T-307 per-turn spans -- only the link " +
                "moves) + literature (C-0205's shear ceiling and C-0208's radial bracket, both " +
                "re-derived here through the corpus's own functions rather than transcribed)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "RADIAL constant is unsourceable (C-0208 section 6) and is carried as a " +
                "bracket; the answer is stated at both of its ends and at a fourth rung below " +
                "both. No such raster has been drawn, let alone folded.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "dishing" to "dimensionless, as a fraction of the free stroke of the SAME lattice"
        ),
        conventions = mapOf(
            "the link" to "k_radial unitZ^2 + k_transverse unitY^2 on the bond's own line of " +
                    "centres (C-0208). An in-plane bond has unitZ = 0 and reads the transverse " +
                    "constant exactly; one through the thickness has unitZ^2 = 3/4",
            "a null radial constant" to "the standing single-scalar object by IDENTITY, not by " +
                    "arithmetic -- unitY^2 + unitZ^2 is not exactly one in floating point",
            "the free stroke" to "the mean deflection of the UNTIED lattice of the same row AT " +
                    "THE SAME RUNG under the uniform interior pressure. A uniform load " +
                    "translates a free tile rigidly, so it is link-independent, and the " +
                    "uniform-load dishing is emitted beside it",
            "dishing" to "peakDishing(81) over that stroke; C-0167's convention, T-5b = 0.10",
            "the row length" to "the PAIRED row, scaffoldNucleotides / 60 - 28 (C-0207's own)",
            "the lattice phase" to "b0, the class-zero staple residue, a free DESIGN VARIABLE " +
                    "on a raster that does not close",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)"
        ),
        parameters = mapOf(
            "crossSection" to "10 x 6",
            "raster" to "uniform, route B -- the paired row is scaffoldNucleotides / 60 - 28",
            "pairedRows" to widths.joinToString(", ") {
                it.scaffold + " " + it.pairedRowBasePairs + " bp"
            },
            "rowWidths" to widths.joinToString(", ") { it.rowWidth.t315Emitted(9) + " nm" },
            "edgeY" to edgeY.t315Emitted(9),
            "interhelicalDistance" to d.t315Emitted(9),
            "phosphateRadius" to rP.t315Emitted(9),
            "risePerBasePair" to Gen1Tile.RISE_PER_BASE_PAIR.t315Emitted(9),
            "latticePeriod" to T315_PERIOD.toString(),
            "phasesSwept" to phases.size.toString(),
            "unpairedPerHelix" to T315_UNPAIRED_PER_HELIX.toString(),
            "chainReadings" to chains.joinToString("; ") { it.first },
            "kuhnBracket" to "2.10 to 2.84 nm, zero force",
            "contourBracket" to "0.65 to 0.70 nm/nt, inextensible",
            "thermalEnergy" to kBT.t315Emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.t315Emitted(9),
            "hingeStiffnessEnhancement" to enhancement.t315Emitted(9),
            "compositeFraction" to T315_COMPOSITE_FRACTION.t315Emitted(2),
            "transverseLinkStiffness" to shearCeiling.t315Emitted(9),
            "radialLinkBracket" to radial.floor.t315Emitted(9) + " to " +
                    radial.ceiling.t315Emitted(9),
            "rungs" to rungs.joinToString("; ") {
                it.name + " (in plane " + it.inPlaneLinkStiffness.t315Emitted(9) +
                        ", through " + it.throughThicknessLinkStiffness.t315Emitted(9) + ")"
            },
            "samples" to T315_SAMPLES.toString(),
            "tolerance" to T315_TOLERANCE.t315Emitted(2),
            "subdivisions" to "1 at every headline cell; 2 on the convergence axis",
            "firstAxialSign" to "+1",
            "electrolyte" to "MgCl2 (2:1)",
            "bufferMillimolar" to T315_BUFFER_MILLIMOLAR.t315Emitted(3),
            "gapHeightNm" to T315_GAP_NM.t315Emitted(3),
            "appliedBiasVolts" to T315_BIAS_VOLTS.t315Emitted(3),
            "temperatureKelvin" to "300",
            "whyTheRegimeBlockIsNull" to "environment.Regime holds the gap and the bias as " +
                    "INTERVALS and refuses a degenerate one; this study solves no " +
                    "electrostatics, it reads ONE profile record of T-3b and uses it as a " +
                    "fixed load shape, so the state is a POINT (C-0181, CH-0224)",
            "smoke" to smoke.toString()
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_297.path + " (C-0194's free-tile link sweep -- the cheap bound)",
            ResultInputs.T_307.path + " (C-0207's 756 cells, reproduced cell by cell)"
        ),
        citedInputs = mapOf(
            "C-0207's headline" to "756 of 756 cells flat, 0.0483790868 to 0.0946863482",
            "C-0205 section 1 the shear ceiling" to "254.808095 pN/nm",
            "C-0208 section 1a the radial bracket" to "754.005141 to 1735.49922 pN/nm",
            "C-0208 section 1 the resolved through-thickness link" to
                    "629.20588 to 1365.32644 pN/nm",
            "CH-0265 the penalty over the ceiling" to "39.2452209x",
            "C-0205 section 4 route B's coupled direction" to
                    "0 of 16 flat at p90, rising as the link softens",
            "C-0208 the bond census at the 116 bp block extent" to "435 = 135 + 300",
            "C-0201 section 7 the three untied readings" to
                    "0.0425678289 / 0.0422200543 / 0.0451172785"
        ),
        cheapBound = cheapBound,
        rungs = rungs.map {
            T315Rung(
                name = it.name,
                ground = it.ground,
                transverseLinkStiffness = it.transverseLinkStiffness,
                radialLinkStiffness = it.radialLinkStiffness,
                inPlaneLinkStiffness = it.inPlaneLinkStiffness,
                throughThicknessLinkStiffness = it.throughThicknessLinkStiffness,
                overThePenalty = it.throughThicknessLinkStiffness / T315_PENALTY
            )
        },
        bondCensus = bondCensus,
        untied = untiedRows,
        cells = cells,
        best = bestRows,
        monotonicity = monotonicity,
        reproductions = reproductions,
        convergence = convergence,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "THIS IS THE FREE TILE, exactly as C-0207 is. The coupled reading at 92 / 98 / " +
                    "106 bp is NOT answered: C-0167's placements, station lattices and " +
                    "distributions are derived at the 116 bp block extent and re-deriving them " +
                    "here is a placement search rather than a re-grade (C-0207 section 7).",
            "THE RADIAL CONSTANT IS UNSOURCEABLE. C-0208's bracket is two CONSTRUCTED " +
                    "connector candidates in parallel with one MEASURED pair term, and its own " +
                    "section 6 records eight further recorded queries finding nothing that " +
                    "measures a crossover on this coordinate. The answer is stated at both " +
                    "ends of the bracket and at a fourth rung BELOW both.",
            "THE TRANSVERSE CONSTANT IS PINNED AT C-0205's CEILING throughout, which is the " +
                    "GENEROUS reading: C-0208 section 1a records that adding the measured " +
                    "pair's transverse eigenvalue would lower it by 1.09182329x.",
            "THE WHOLE BRANCH IS CONDITIONAL ON ROUTE B, which C-0193 and C-0200 establish is " +
                    "what the only folded instance of this cross-section does. Route A is " +
                    "drawable and undemonstrated. Nothing here grades route A.",
            "The element is C-0201's, unchanged: a linearisation about the built, taut state, " +
                    "one-sided, with the anchor at the beam axis on C-0194's frame-indifferent " +
                    "d/2 arm rather than at the phosphate radius.",
            "The lattice carries no steric floor between two duplexes and no across-helix " +
                    "parallel-axis term, k_theta is Gen1Tile's square-lattice-fitted constant, " +
                    "and CH-0242's common-mode spring is absent -- so every bond here is still " +
                    "missing the stiffer of the two springs, at every rung.",
            "The nine IN-PLANE turns contribute exactly zero preload because this model has no " +
                    "in-plane transverse coordinate. That zero is a property of the MODEL.",
            "Nothing here re-opens the span census, the raster, the cross-section, the chain " +
                    "model or the placement search."
        ),
        openQuestions = listOf(
            "The COUPLED reading at 92 / 98 / 106 bp at the resolved link, which needs a " +
                    "placement search rather than a re-grade of C-0167's 116 bp stations.",
            "What the RADIAL link constant actually is. C-0208 brackets it and records that " +
                    "nothing published measures it.",
            "Whether a route-B design should trade paired row length against span; the three " +
                    "widths here are the maximum each scaffold affords.",
            "The BUILT block's own lattice phase, which needs a register read of the deposited " +
                    "10 x 6 file rather than a derivation."
        ),
        proseFailure = proseFailure
    )

    val output = File("gpd/results/T-315-the-uniform-raster-at-the-resolved-link.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-315 - wrote " + output.path)
    check(proseFailure == "none") { proseFailure }
}
