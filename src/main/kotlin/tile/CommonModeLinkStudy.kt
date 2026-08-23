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
// T-297 -- CH-0242 says the lattice's crossover carries only the CHEAPER of a bond's two
// azimuthal springs. It does not. The vertical LINK's residual is
//
//     R = dW + (d/2) unitY (Phi_a + Phi_b)
//
// -- a function of the SUM, i.e. the common mode itself -- and d/2 is the ONLY arm that
// annihilates the linearised rigid roll, so no other arm may appear in a linear element. The
// lattice therefore sits at the RIGID end of the common mode rather than at the free one, and
// what is wrong is a MAGNITUDE: the penalty against the span law's own T/g.
//
// Everything here is a stiffness sweep of an EXISTING constructor argument plus one new load,
// so no existing consumer of HoneycombGrillage can have moved and that is asserted.
// ---------------------------------------------------------------------------------------------

private const val T297_SAMPLES: Int = 81
private const val T297_TOLERANCE: Double = 0.10
private const val T297_RIM_STANDOFF: Double = 1.0
private const val T297_RIM_BAND: Double = 6.7
private const val T297_SEED: Long = 197_197L
private const val T297_ROW_BP: Int = 116
private const val T297_RECOMMENDED_ONE: Int = 102
private const val T297_RECOMMENDED_TWO: Int = 109
private const val T297_IDENTITY: Double = 1e-9

/** The study runs at 4 000 realisations; `T297_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t297Realisations: Int = if (System.getenv("T297_SMOKE") == "1") 150 else 4000

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T297CheapBoundRow(
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private class T297ChannelRow(
    val statement: String,
    val quantity: String,
    val value: Double,
    val reference: Double,
    val ratio: Double,
    val holds: Boolean,
    val note: String
)

@Serializable
private class T297ProbeRow(
    val state: String,
    val linkStiffness: Double,
    val hingeEnergy: Double,
    val linkEnergy: Double,
    val slipEnergy: Double,
    val closedFormLinkEnergy: Double?,
    val departure: Double?,
    val note: String
)

@Serializable
private class T297SweepRow(
    val crossSection: String,
    val hingeStiffnessEnhancement: Double,
    val linkStiffness: Double,
    val linkStiffnessOverSpanDerived: Double,
    val freeDishingWithoutTies: Double,
    val freeDishingWithTies: Double,
    val tieRatio: Double,
    val insideTolerance: Boolean
)

@Serializable
private class T297EigenstrainRow(
    val term: String,
    val linkStiffness: Double,
    val phase: Int,
    val hingeStiffnessEnhancement: Double,
    val peakDishingOverStroke: Double,
    val insideTolerance: Boolean,
    val note: String
)

@Serializable
private class T297CellRow(
    val term: String,
    val linkStiffness: Double,
    val phase: Int,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val compositeFraction: Double,
    val nominalOverStroke: Double,
    val p90OverStroke: Double,
    val flatAtP90: Boolean,
    val zeroTermP90OverStroke: Double,
    val movementFromZeroTerm: Double
)

@Serializable
private class T297Convergence(
    val axis: String,
    val quantity: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double,
    val verdictSurvives: Boolean
)

@Serializable
private class T297Reproduction(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val departure: Double,
    val source: String
)

@Serializable
private class T297Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private class T297Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: List<T297CheapBoundRow>,
    val channel: List<T297ChannelRow>,
    val probe: List<T297ProbeRow>,
    val sweep: List<T297SweepRow>,
    val eigenstrain: List<T297EigenstrainRow>,
    val cells: List<T297CellRow>,
    val convergence: List<T297Convergence>,
    val reproductions: List<T297Reproduction>,
    val falsifiers: List<T297Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T297Collar(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, lengthS: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, lengthS, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T297_RIM_STANDOFF))
        )
}

private fun t297Collar(file: File): T297Collar {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T297Collar(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * `T-254`'s own lattice, with the link stiffness exposed.
 *
 * Every other argument is `RasterTurnPrestrainStudy`'s, so at [linkStiffness] equal to
 * `RIGID_LINK_STIFFNESS` this is that study's object — asserted rather than claimed.
 */
private fun t297Lattice(
    block: HoneycombBlock,
    enhancement: Double,
    ties: List<HoneycombScaffoldTurnTie>,
    linkStiffness: Double,
    subdivisions: Int = 1
) = HoneycombGrillage(
    block = block,
    rowBasePairs = T297_ROW_BP,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    hingeStiffnessEnhancement = enhancement,
    subdivisions = subdivisions,
    linkStiffness = linkStiffness,
    scaffoldTurnTies = ties
)

private fun HoneycombDeflection.asDishing(): DishingSolution =
    object : DishingSolution {
        override fun deflectionAt(x: Double, y: Double) = deflection(x, y)
        override fun dishingAt(x: Double, y: Double) = dishing(x, y)
    }

private fun sum(a: DishingSolution, b: DishingSolution): DishingSolution =
    object : DishingSolution {
        override fun deflectionAt(x: Double, y: Double) =
            a.deflectionAt(x, y) + b.deflectionAt(x, y)

        override fun dishingAt(x: Double, y: Double) = a.dishingAt(x, y) + b.dishingAt(x, y)
    }

/** The link offsets `C-0187`'s derived assignment demands, keyed on the turn index. */
private fun t297LinkOffsets(
    lattice: HoneycombGrillage,
    signs: HoneycombRasterTurnSigns,
    phase: Int
): Map<Int, Double> {
    val demands = honeycombTurnRollDemands(signs, phase)
    check(demands.size == lattice.turnElements.size) {
        "the demand list carries ${demands.size} turns and the lattice ${lattice.turnElements.size}"
    }
    return demands.indices.associateWith {
        turnLinkOffset(
            lattice.bondLength,
            lattice.turnElements[it].unitY,
            Math.toRadians(demands[it].rollDegrees)
        )
    }
}

// ------------------------------------------------------------------------------ the cells

private class T297Cell(
    val label: String,
    val placement: String,
    val columns: Int,
    val grid: List<Pair<Double, Double>>,
    val distribution: String,
    val stiffnesses: List<Double>
)

private class T297Graded(
    val nominal: Double,
    val p90: Double,
    val flat: Boolean
)

@Suppress("LongMethod", "ComplexMethod", "LongParameterList")
private fun t297Grade(
    structure: HoneycombGrillage,
    free: DishingSolution,
    bank: List<DishingSolution>,
    cell: T297Cell,
    stroke: Double,
    ensembleProbabilities: List<Double>,
    samples: Int
): T297Graded {
    val surrogate = influenceSurrogate(
        cell.grid, structure.lengthS / 2.0, structure.lengthY / 2.0, samples, free, bank
    )
    val ensemble = dropoutEnsemble(ensembleProbabilities, t297Realisations, T297_SEED)
    val nominal = surrogate.solve(cell.stiffnesses).peakDishing / stroke
    val sample = dropoutDishingSample(surrogate, cell.stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / stroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T297_TOLERANCE
    )
    return T297Graded(nominal, summary.p90, summary.flatAtP90)
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val collar = t297Collar(ResultInputs.T_3B.file())
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val kTheta = Gen1Tile.crossoverHingeStiffness()
    val kt = thermalEnergy(ROOM_TEMPERATURE)
    val g = crossoverSpanFloor(d, rP)
    val tension = impliedCrossoverBondTension(kTheta, rP)
    val kSpan = spanDerivedLinkStiffness(kTheta, rP, d)
    val penalty = HoneycombGrillage.RIGID_LINK_STIFFNESS
    val allowed = allowedScaffoldCrossoverDepartureDegrees()
    val ladder = listOf(kSpan, 1e2, 1e3, penalty, 1e5, 1e6)

    // ==================== Deliverable 1 -- the cheap bound, and it needs no solve at all
    println("T-297 - the cheap bound, before any lattice is assembled")
    val cheap = ArrayList<T297CheapBoundRow>()
    val channel = ArrayList<T297ChannelRow>()

    val challenge = commonModeSpanRatio(d, rP)
    val linearised = linearisedCommonModeSpanRatio(d, rP)
    val geometric = geometricCommonModeSpanRatio(d, rP)
    channel += T297ChannelRow(
        statement = "CH-0242's ratio, re-derived rather than inherited: the common mode's span " +
                "excess over the relative one's at FIXED axes is 1 + 2 r_P/(d - 2 r_P), which " +
                "is d/(d - 2 r_P)",
        quantity = "the ratio, dimensionless",
        value = challenge,
        reference = 3.52810239,
        ratio = challenge / 3.52810239,
        holds = abs(challenge - 3.52810239) > 1e-4,
        note = "the value in circulation -- CH-0242's headline and section 1, C-0190's headline " +
                "and section 6, the challenges index and two prose strings of T-291's own " +
                "result file -- is 3.52810239, and T-291's openQuestions block emitted " +
                challenge.emitted(9) + " from this same expression. No verdict moves: the 409x " +
                "it is quoted in is 409x either way"
    )
    channel += T297ChannelRow(
        statement = "the frame-indifferent element a LINEAR lattice may carry reads the same " +
                "configuration at d^2/(2 g r_P), and the difference from CH-0242's expansion is " +
                "exactly d/(2 r_P) -- the prestress geometric term linearisation excludes",
        quantity = "d^2/(2 g r_P) - d/(2 r_P), which must equal d/g",
        value = linearised - geometric,
        reference = challenge,
        ratio = (linearised - geometric) / challenge,
        holds = abs(linearised - geometric - challenge) < 1e-12,
        note = "an identity, asserted rather than fitted: " + linearised.emitted(9) + " - " +
                geometric.emitted(9) + " = " + challenge.emitted(9)
    )
    val latticeInPlane = latticeCommonModeAzimuthalStiffness(penalty, d, 1.0)
    val latticeInterlayer = latticeCommonModeAzimuthalStiffness(penalty, d, 0.5)
    channel += T297ChannelRow(
        statement = "the lattice's link residual dW + (d/2) unitY (Phi_a + Phi_b) IS the common " +
                "mode, so the model's own common-mode azimuthal stiffness at fixed axes is " +
                "k_link d^2 unitY^2 / 4 -- and it EXCEEDS the physical one",
        quantity = "the in-plane bond's common-mode stiffness in pN*nm/rad",
        value = latticeInPlane,
        reference = challenge * kTheta,
        ratio = latticeInPlane / (challenge * kTheta),
        holds = latticeInPlane > challenge * kTheta,
        note = "the interlayer bond reads " + latticeInterlayer.emitted(9) + " pN*nm/rad, " +
                (latticeInterlayer / (challenge * kTheta)).emitted(9) + "x the physical one. " +
                "CH-0242 section 3's 'and nothing else on the azimuthal coordinates' is false"
    )
    channel += T297ChannelRow(
        statement = "and d/2 is the ONLY connector arm that annihilates the linearised rigid " +
                "roll Phi = alpha, W = alpha y: the residual an arm a leaves is " +
                "alpha unitY (2a - d)",
        quantity = "the residual in nm at a = r_P and a 1 mrad rigid roll, in-plane bond",
        value = abs(rigidRollLinkResidual(d, 1.0, rP, 1e-3)),
        reference = abs(rigidRollLinkResidual(d, 1.0, frameIndifferentLinkArm(d), 1e-3)),
        ratio = 0.0,
        holds = abs(rigidRollLinkResidual(d, 1.0, frameIndifferentLinkArm(d), 1e-3)) < 1e-18,
        note = "so the geometry's own arm r_P is not admissible in a linear element and the " +
                "model's d/2 is a theorem, not a fitted parameter (CLAUDE.md's own frame-" +
                "indifference entry, met on the honeycomb)"
    )
    channel += T297ChannelRow(
        statement = "matching the SAME bond tension that fixes k_theta -- CH-0242's own premise, " +
                "that both eigenmodes are one span mechanism -- gives T = 2 k_theta / r_P and a " +
                "link stiffness k_R = T/g",
        quantity = "k_R in pN/nm",
        value = kSpan,
        reference = penalty,
        ratio = penalty / kSpan,
        holds = kSpan < penalty,
        note = "the implied bond tension is " + tension.emitted(9) + " pN, which over the built " +
                "span's excess above T-71's measured C2'-endo step implies an effective step " +
                "stiffness of " + (tension / (g - MeasuredBackbone.STEP_SOUTH)).emitted(6) + " pN/nm"
    )
    channel.forEach {
        println("  " + it.quantity + " = " + it.value.emitted(9) +
                (if (it.holds) "  holds" else "  DOES NOT HOLD"))
    }

    cheap += T297CheapBoundRow(
        question = "which coordinate of the lattice does a crossover's COMMON azimuthal mode " +
                "live on?",
        answer = "the vertical LINK. HoneycombGrillage assembles it with the gradient " +
                "(1, armY, -1, armY) over (W_a, Phi_a, W_b, Phi_b), armY = (d/2) unitY, so its " +
                "residual is a function of the SUM of the two rolls.",
        consequence = "CH-0242's premise -- that the bond and tie carry the relative roll and " +
                "nothing else on the azimuthal coordinates -- is false, and the question " +
                "changes from 'add a spring' to 'measure what the penalty costs'."
    )
    cheap += T297CheapBoundRow(
        question = "is the lattice above or below the physical common-mode stiffness?",
        answer = "ABOVE, by " + (latticeInPlane / (challenge * kTheta)).emitted(9) +
                "x in plane and " + (latticeInterlayer / (challenge * kTheta)).emitted(9) +
                "x through the thickness.",
        consequence = "the lattice sits at the RIGID end of the common mode, not at the free " +
                "one, so the challenge's direction is reversed and C-0175's and C-0180's tie " +
                "deliverables are not lower bounds for the reason it gives."
    )
    cheap += T297CheapBoundRow(
        question = "what does the expensive half then cost?",
        answer = "a sweep of an EXISTING constructor argument. linkStiffness is already a " +
                "HoneycombGrillage parameter, so the error the approximation carries is " +
                ladder.size.toString() + " solves of a lattice that exists.",
        consequence = "no new element, no new stiffness matrix, and every influence bank in " +
                "the corpus stands."
    )
    cheap.forEach { println("  " + it.question + " -> " + it.answer) }

    // ==================== Deliverable 2 -- the probe, read off the ASSEMBLED lattice
    println("T-297 - the probe, on the assembled 10 x 6 lattice rather than on the source")
    val block = HoneycombBlock(10, 6)
    val probeBare = t297Lattice(block, 1.0, emptyList(), penalty)
    val probeTies = honeycombScaffoldTurnTies(block, probeBare.nodesPerBeam)
    val probeLattice = t297Lattice(block, 1.0, probeTies, penalty)
    val theta = 1e-3
    val probe = ArrayList<T297ProbeRow>()

    val commonField = probeLattice.nodalField(
        { _, _, _ -> 0.0 }, { _, _, _ -> 0.0 }, { _, _, _ -> theta }, { _, _, _ -> 0.0 }
    )
    val closedForm = probeLattice.bonds.sumOf {
        val residual = turnLinkOffset(d, it.unitY, theta)
        0.5 * penalty * residual * residual
    }
    probe += T297ProbeRow(
        state = "a COMMON roll of 1 mrad at every beam, axes at nominal",
        linkStiffness = penalty,
        hingeEnergy = probeLattice.hingeEnergy(commonField),
        linkEnergy = probeLattice.linkEnergy(commonField),
        slipEnergy = probeLattice.slipEnergy(commonField),
        closedFormLinkEnergy = closedForm,
        departure = abs(probeLattice.linkEnergy(commonField) - closedForm) / closedForm,
        note = "the hinge stores exactly zero and the link stores the closed form, which is " +
                "what makes the link the common-mode coordinate"
    )
    val rigidField = probeLattice.nodalField(
        { _, y, _ -> theta * y }, { _, _, _ -> 0.0 }, { _, _, _ -> theta }, { _, _, _ -> 0.0 }
    )
    probe += T297ProbeRow(
        state = "a RIGID roll of 1 mrad: Phi = alpha at every beam and W = alpha y",
        linkStiffness = penalty,
        hingeEnergy = probeLattice.hingeEnergy(rigidField),
        linkEnergy = probeLattice.linkEnergy(rigidField),
        slipEnergy = probeLattice.slipEnergy(rigidField),
        closedFormLinkEnergy = 0.0,
        departure = probeLattice.turnElements.maxOf {
            abs(probeLattice.turnLinkExtension(rigidField, it))
        },
        note = "every element stores zero, which is frame indifference measured rather than " +
                "argued; the departure column is the worst turn tie's own link extension in nm"
    )
    // consecutive x-raster rows counter-rolled: an IN-PLANE bond joins two adjacent rows, so it
    // sees Phi_a - Phi_b = 2 theta and Phi_a + Phi_b = 0 -- the pure relative eigenmode.
    val minY = probeLattice.beamY.min()
    val rowPitch = probeLattice.rowPitch
    val relativeField = probeLattice.nodalField(
        { _, _, _ -> 0.0 }, { _, _, _ -> 0.0 },
        { _, y, _ -> if (Math.round((y - minY) / rowPitch) % 2L == 0L) theta else -theta },
        { _, _, _ -> 0.0 }
    )
    // hingeEnergy sums the lattice bonds AND the raster turn ties, so the closed form must too.
    val inPlaneJoints = probeLattice.bonds.count { abs(it.unitY) > 0.75 } +
            probeLattice.turnElements.count { abs(it.unitY) > 0.75 }
    val inPlaneHinge = 0.5 * kTheta * inPlaneJoints * (2.0 * theta) * (2.0 * theta)
    probe += T297ProbeRow(
        state = "consecutive x-raster rows counter-rolled by 1 mrad, so every IN-PLANE bond " +
                "carries the pure RELATIVE eigenmode",
        linkStiffness = penalty,
        hingeEnergy = probeLattice.hingeEnergy(relativeField),
        linkEnergy = probeLattice.linkEnergy(relativeField),
        slipEnergy = probeLattice.slipEnergy(relativeField),
        closedFormLinkEnergy = inPlaneHinge,
        departure = null,
        note = "the closed-form column is the in-plane JOINTS' own hinge energy, " +
                "k_theta (2 theta)^2 / 2 at each of the " + inPlaneJoints +
                " bonds and ties that join two adjacent rows; the interlayer ones join two " +
                "helices of the SAME row and therefore see the common mode instead, which is " +
                "why both springs read non-zero -- they are one residual and one hinge, not " +
                "two azimuthal springs"
    )
    probe.forEach {
        println("  " + it.state + " -> hinge " + it.hingeEnergy.emitted(6) +
                "  link " + it.linkEnergy.emitted(6) + "  slip " + it.slipEnergy.emitted(6))
    }

    // ==================== Deliverable 3 -- the sweep, which IS the error the model carries
    println("T-297 - the link sweep, C-0175 section 9's own table re-taken at each end")
    val t254 = Json.parseToJsonElement(ResultInputs.T_254.readText())
        .jsonObject.getValue("stiffness").jsonArray.map { it.jsonObject }
    val sweep = ArrayList<T297SweepRow>()
    val reproductions = ArrayList<T297Reproduction>()
    val blocks = HashMap<String, HoneycombBlock>()
    t254.forEach { row ->
        val cross = row.getValue("crossSection").jsonPrimitive.content
        val enhancement = row.getValue("hingeStiffnessEnhancement").jsonPrimitive.content.toDouble()
        val parts = cross.split(" x ")
        val design = blocks.getOrPut(cross) { HoneycombBlock(parts[0].toInt(), parts[1].toInt()) }
        ladder.forEach { link ->
            val bare = t297Lattice(design, enhancement, emptyList(), link)
            val ties = honeycombScaffoldTurnTies(design, bare.nodesPerBeam)
            val armed = t297Lattice(design, enhancement, ties, link)
            val pressure = collar.field(
                Gen1Tile.TARGET_FORCE / armed.area, armed.lengthS, armed.lengthY
            )
            val stroke = Gen1Tile.TARGET_FORCE / armed.area / Gen1Tile.FOUNDATION_SECANT
            val without = bare.solve(pressure).peakDishing(T297_SAMPLES) / stroke
            val with = armed.solve(pressure).peakDishing(T297_SAMPLES) / stroke
            sweep += T297SweepRow(
                crossSection = cross,
                hingeStiffnessEnhancement = enhancement,
                linkStiffness = link,
                linkStiffnessOverSpanDerived = link / kSpan,
                freeDishingWithoutTies = without,
                freeDishingWithTies = with,
                tieRatio = with / without,
                insideTolerance = with < T297_TOLERANCE
            )
            if (link == penalty) {
                listOf(
                    "freeDishingWithoutTies" to without,
                    "freeDishingWithTies" to with
                ).forEach { (name, value) ->
                    val published = row.getValue(name).jsonPrimitive.content.toDouble()
                    reproductions += T297Reproduction(
                        quantity = "C-0175 section 9, " + cross + " at enhancement " +
                                enhancement.emitted(9) + ": " + name,
                        published = published,
                        derived = value,
                        departure = abs(value - published) / published,
                        source = "gpd/results/T-254-raster-turn-prestrain.json, stiffness record"
                    )
                }
            }
        }
    }
    sweep.filter { it.crossSection == "10 x 6" }.forEach {
        println("  " + it.crossSection + " f-enh " + it.hingeStiffnessEnhancement.emitted(6) +
                "  k_link " + it.linkStiffness.emitted(6) + " -> " +
                it.freeDishingWithTies.emitted(9) +
                (if (it.insideTolerance) "  inside" else "  OUTSIDE"))
    }
    val verdictMoves = sweep.groupBy { it.crossSection to it.hingeStiffnessEnhancement }
        .count { (_, rows) -> rows.map { it.insideTolerance }.toSet().size > 1 }
    val worstSweepSpread = sweep.groupBy { it.crossSection to it.hingeStiffnessEnhancement }
        .maxOf { (_, rows) ->
            val values = rows.map { it.freeDishingWithTies }
            (values.max() - values.min()) / values.min()
        }
    println("  verdicts moving over the ladder: " + verdictMoves + " of " +
            sweep.groupBy { it.crossSection to it.hingeStiffnessEnhancement }.size +
            "; worst relative spread " + worstSweepSpread.emitted(6))

    // ==================== Deliverable 4 -- the departure on the coordinate it lives on
    println("T-297 - the departure as a LINK eigenstrain, which is what pins C-0190's magnitude")
    val signs = HoneycombRasterTurnSigns(block, T297_RECOMMENDED_ONE, T297_RECOMMENDED_TWO)
    check(signs.closes) { "the recommended raster must close for its departures to be determined" }
    val enhancements = t254.filter { it.getValue("crossSection").jsonPrimitive.content == "10 x 6" }
        .map { it.getValue("hingeStiffnessEnhancement").jsonPrimitive.content.toDouble() }
        .sorted()
    val eigenstrain = ArrayList<T297EigenstrainRow>()
    val eigenFields = HashMap<String, Double>()
    val ends = listOf(kSpan, penalty)
    ends.forEach { link ->
        enhancements.forEach { enhancement ->
            val bare = t297Lattice(block, enhancement, emptyList(), link)
            val ties = honeycombScaffoldTurnTies(block, bare.nodesPerBeam)
            val armed = t297Lattice(block, enhancement, ties, link)
            val pressure = collar.field(
                Gen1Tile.TARGET_FORCE / armed.area, armed.lengthS, armed.lengthY
            )
            val stroke = Gen1Tile.TARGET_FORCE / armed.area / Gen1Tile.FOUNDATION_SECANT
            val pressureField = armed.solve(pressure).asDishing()
            val zero = armed.solve(pressure).peakDishing(T297_SAMPLES) / stroke
            eigenstrain += T297EigenstrainRow(
                term = "no eigenstrain",
                linkStiffness = link,
                phase = 0,
                hingeStiffnessEnhancement = enhancement,
                peakDishingOverStroke = zero,
                insideTolerance = zero < T297_TOLERANCE,
                note = "the tied free tile, which is C-0175 section 9's own reading at " +
                        "k_link = 1e4"
            )
            listOf(1, -1).forEach { phase ->
                val offsets = t297LinkOffsets(armed, signs, phase)
                val field = armed.turnLinkOffsetResponse(offsets).asDishing()
                val total = sum(pressureField, field)
                var worst = 0.0
                for (i in 0 until T297_SAMPLES) {
                    val s = -armed.lengthS / 2.0 + armed.lengthS * i / (T297_SAMPLES - 1)
                    for (j in 0 until T297_SAMPLES) {
                        val y = -armed.lengthY / 2.0 + armed.lengthY * j / (T297_SAMPLES - 1)
                        worst = maxOf(worst, abs(total.dishingAt(s, y)))
                    }
                }
                val value = worst / stroke
                eigenFields[link.toString() + "|" + enhancement + "|" + phase] = value
                eigenstrain += T297EigenstrainRow(
                    term = "the derived departure as a LINK offset, phase " +
                            (if (phase > 0) "+1" else "-1"),
                    linkStiffness = link,
                    phase = phase,
                    hingeStiffnessEnhancement = enhancement,
                    peakDishingOverStroke = value,
                    insideTolerance = value < T297_TOLERANCE,
                    note = "each turn relaxed at R0 = d unitY rho with rho = " +
                            allowed.emitted(9) + " degrees, C-0187's derived assignment"
                )
            }
        }
    }
    eigenstrain.forEach {
        println("  k_link " + it.linkStiffness.emitted(6) + "  enh " +
                it.hingeStiffnessEnhancement.emitted(6) + "  phase " + it.phase + " -> " +
                it.peakDishingOverStroke.emitted(9) +
                (if (it.insideTolerance) "  inside" else "  OUTSIDE"))
    }

    // ==================== Deliverable 5 -- the two cells C-0190's threshold is quoted on
    println("T-297 - the two recovered cells, graded at both ends of the link ladder")
    val faceY = probeBare.faceBeams.map { probeBare.beamY[it] }
    val edgeX = probeBare.lengthS
    val edgeY = probeBare.lengthY
    val incorporation = measuredDepthIncorporation(edgeX, edgeY)
    fun rimGraded(grid: List<Pair<Double, Double>>) = rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T297_RIM_BAND || abs(y) > edgeY / 2.0 - T297_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
    val gridA = attachmentGrid(3, 10, edgeX, edgeY)
    val gridBRaw = attachmentGrid(5, 10, edgeX, edgeY)
    val gridB = gridBRaw.mapIndexed { index, (x, _) -> x to faceY[index / 5] }
    val cellList = listOf(
        T297Cell("cell A", "abstract grid", 3, gridA, "rim-graded 5:1", rimGraded(gridA)),
        T297Cell(
            "cell B", "abstract grid on the rooting helices", 5, gridB, "rim-graded 5:1",
            rimGraded(gridB)
        )
    )
    val recoveredEnhancement = enhancements.last()
    val cells = ArrayList<T297CellRow>()
    @Suppress("LongMethod")
    fun gradeAll(
        link: Double,
        subdivisions: Int,
        samples: Int,
        target: MutableList<T297CellRow>
    ) {
        val bare = t297Lattice(block, recoveredEnhancement, emptyList(), link, subdivisions)
        val ties = honeycombScaffoldTurnTies(block, bare.nodesPerBeam)
        val armed = t297Lattice(block, recoveredEnhancement, ties, link, subdivisions)
        val pressure = collar.field(
            Gen1Tile.TARGET_FORCE / armed.area, armed.lengthS, armed.lengthY
        )
        val stroke = armed.solve(uniformPressure(Gen1Tile.TARGET_FORCE / armed.area))
            .meanDeflection
        val pressureField = armed.solve(pressure).asDishing()
        val fields = HashMap<Int, DishingSolution>()
        listOf(1, -1).forEach { phase ->
            fields[phase] = sum(
                pressureField,
                armed.turnLinkOffsetResponse(t297LinkOffsets(armed, signs, phase)).asDishing()
            )
        }
        cellList.forEach { cell ->
            val bank = cell.grid.map { (s, y) ->
                armed.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0))).asDishing()
            }
            val probabilities = cell.grid.map { (x, y) -> incorporation.at(x, y) }
            val zero = t297Grade(
                armed, pressureField, bank, cell, stroke, probabilities, samples
            )
            listOf(0, 1, -1).forEach { phase ->
                val graded = if (phase == 0) zero else t297Grade(
                    armed, fields.getValue(phase), bank, cell, stroke, probabilities, samples
                )
                target.add(
                    T297CellRow(
                        term = if (phase == 0) "no eigenstrain"
                        else "the derived departure as a LINK offset, phase " +
                                (if (phase > 0) "+1" else "-1"),
                        linkStiffness = link,
                        phase = phase,
                        placement = cell.placement,
                        columns = cell.columns,
                        pathCount = cell.grid.size,
                        distribution = cell.distribution,
                        compositeFraction = 0.30,
                        nominalOverStroke = graded.nominal,
                        p90OverStroke = graded.p90,
                        flatAtP90 = graded.flat,
                        zeroTermP90OverStroke = zero.p90,
                        movementFromZeroTerm = graded.p90 - zero.p90
                    )
                )
            }
        }
    }
    // the whole ladder, not only its two ends: the free tile's verdict is penalty-
    // independent and the COUPLED one need not be, which is what F10 asks.
    ladder.sorted().forEach { gradeAll(it, 1, T297_SAMPLES, cells) }
    cells.forEach {
        println("  k_link " + it.linkStiffness.emitted(6) + "  " + it.placement + " " +
                it.columns + " col  phase " + it.phase + " -> p90 " + it.p90OverStroke.emitted(9) +
                (if (it.flatAtP90) "  FLAT" else "  not flat"))
    }

    // ==================== convergence, taken on the DECIDING quantity at the DECIDING cell
    println("T-297 - convergence, at the cell whose verdict is closest to the tolerance")
    val convergence = ArrayList<T297Convergence>()
    val deciding = cells.filter { it.phase != 0 }
        .minByOrNull { abs(it.p90OverStroke - T297_TOLERANCE) }!!
    fun matching(rows: List<T297CellRow>): T297CellRow = rows.first {
        it.phase == deciding.phase && it.columns == deciding.columns &&
                it.linkStiffness == deciding.linkStiffness
    }
    val subdivided = ArrayList<T297CellRow>()
    gradeAll(deciding.linkStiffness, 2, T297_SAMPLES, subdivided)
    val fineSubdivision = matching(subdivided)
    convergence += T297Convergence(
        axis = "beam subdivisions 1 -> 2, at " + deciding.placement + " " + deciding.columns +
                " col, phase " + deciding.phase + ", k_link " + deciding.linkStiffness.emitted(6),
        quantity = "the ensemble's p90 over the stroke",
        coarse = deciding.p90OverStroke,
        fine = fineSubdivision.p90OverStroke,
        departure = abs(fineSubdivision.p90OverStroke - deciding.p90OverStroke),
        verdictSurvives = fineSubdivision.flatAtP90 == deciding.flatAtP90
    )
    val resampled = ArrayList<T297CellRow>()
    gradeAll(deciding.linkStiffness, 1, 161, resampled)
    val fineSamples = matching(resampled)
    convergence += T297Convergence(
        axis = "dishing sample grid 81 -> 161, at the same cell",
        quantity = "the ensemble's p90 over the stroke",
        coarse = deciding.p90OverStroke,
        fine = fineSamples.p90OverStroke,
        departure = abs(fineSamples.p90OverStroke - deciding.p90OverStroke),
        verdictSurvives = fineSamples.flatAtP90 == deciding.flatAtP90
    )
    val penaltyLadder = listOf(1e4, 1e5, 1e6).map { link ->
        val bare = t297Lattice(block, recoveredEnhancement, emptyList(), link)
        val ties = honeycombScaffoldTurnTies(block, bare.nodesPerBeam)
        val armed = t297Lattice(block, recoveredEnhancement, ties, link)
        val stroke = Gen1Tile.TARGET_FORCE / armed.area / Gen1Tile.FOUNDATION_SECANT
        armed.turnLinkOffsetResponse(t297LinkOffsets(armed, signs, 1))
            .peakDishing(T297_SAMPLES) / stroke
    }
    convergence += T297Convergence(
        axis = "the link PENALTY, 1e4 -> 1e5 -> 1e6, on the eigenstrain field alone",
        quantity = "the peak dishing over the stroke of the link eigenstrain's own response",
        coarse = penaltyLadder[1],
        fine = penaltyLadder[2],
        departure = abs(penaltyLadder[2] - penaltyLadder[1]),
        verdictSurvives = abs(penaltyLadder[2] - penaltyLadder[1]) <
                abs(penaltyLadder[1] - penaltyLadder[0])
    )
    convergence.forEach {
        println("  " + it.axis + " -> departure " + it.departure.emitted(2) +
                (if (it.verdictSurvives) "  survives" else "  MOVES"))
    }

    // ==================== the reproductions that pair this study against C-0190's own cells
    val t291Scale = Json.parseToJsonElement(ResultInputs.T_291.readText())
        .jsonObject.getValue("scale").jsonArray.map { it.jsonObject }
    cellList.forEach { cell ->
        val published = t291Scale.first {
            it.getValue("placement").jsonPrimitive.content == cell.placement &&
                    it.getValue("columns").jsonPrimitive.content.toInt() == cell.columns &&
                    it.getValue("fractionOfTheDerivedEigenstrain").jsonPrimitive.content
                        .toDouble() == 0.0
        }.getValue("worstOfTheTwo").jsonPrimitive.content.toDouble()
        val derived = cells.first {
            it.phase == 0 && it.columns == cell.columns && it.linkStiffness == penalty
        }.zeroTermP90OverStroke
        reproductions += T297Reproduction(
            quantity = "C-0190 section 6's zero-eigenstrain p90 at " + cell.placement + " " +
                    cell.columns + " col",
            published = published,
            derived = derived,
            departure = abs(derived - published) / published,
            source = "gpd/results/T-291-common-mode-departure-and-beam-twist.json, scale record"
        )
    }
    val worstReproduction = reproductions.maxOf { it.departure }
    println("T-297 - reproductions: " + reproductions.size + ", worst departure " +
            worstReproduction.emitted(2))

    // ==================== the falsifiers
    val commonRollHinge = probe[0].hingeEnergy
    val commonRollLink = probe[0].linkEnergy
    val rigidTotal = probe[1].hingeEnergy + probe[1].linkEnergy + probe[1].slipEnergy
    val uniformDishing = ends.maxOf { link ->
        val bare = t297Lattice(block, recoveredEnhancement, emptyList(), link)
        val ties = honeycombScaffoldTurnTies(block, bare.nodesPerBeam)
        val armed = t297Lattice(block, recoveredEnhancement, ties, link)
        armed.solve(uniformPressure(Gen1Tile.TARGET_FORCE / armed.area))
            .peakDishing(T297_SAMPLES)
    }
    val relativeProjection = ends.maxOf { link ->
        val bare = t297Lattice(block, recoveredEnhancement, emptyList(), link)
        val ties = honeycombScaffoldTurnTies(block, bare.nodesPerBeam)
        val armed = t297Lattice(block, recoveredEnhancement, ties, link)
        val load = armed.turnLinkOffsetLoad(t297LinkOffsets(armed, signs, 1))
        armed.turnElements.maxOf { element ->
            val upper = (element.node * armed.beamCount + element.tie.upperBeam) *
                    HoneycombGrillage.DOF_PER_NODE + HoneycombGrillage.PHI
            val lower = (element.node * armed.beamCount + element.tie.lowerBeam) *
                    HoneycombGrillage.DOF_PER_NODE + HoneycombGrillage.PHI
            abs(load[upper] - load[lower])
        }
    }
    val recoveredCells = cells.filter { it.phase != 0 }
    val stillFlat = recoveredCells.count { it.flatAtP90 }
    val falsifiers = listOf(
        T297Falsifier(
            id = "F1",
            statement = "the link's residual does NOT carry the sum of the two rolls, so the " +
                    "common mode really is absent from the element set",
            fired = commonRollLink < T297_IDENTITY,
            outcome = "a common roll of 1 mrad at every beam stores " +
                    commonRollHinge.emitted(2) + " pN*nm in the hinges and " +
                    commonRollLink.emitted(9) + " in the links, matching the closed form to " +
                    (probe[0].departure ?: 0.0).emitted(2)
        ),
        T297Falsifier(
            id = "F2",
            statement = "the lattice's own common-mode stiffness is BELOW CH-0242's physical " +
                    "one, so the challenge stands in direction as well as in kind",
            fired = latticeInPlane < challenge * kTheta,
            outcome = "it is " + (latticeInPlane / (challenge * kTheta)).emitted(9) +
                    "x above it in plane and " +
                    (latticeInterlayer / (challenge * kTheta)).emitted(9) +
                    "x through the thickness"
        ),
        T297Falsifier(
            id = "F3",
            statement = "some arm other than d/2 also annihilates the linearised rigid roll, so " +
                    "the link's arm is a fitted parameter rather than a theorem",
            fired = abs(rigidRollLinkResidual(d, 1.0, rP, 1e-3)) < 1e-18,
            outcome = "the arm r_P leaves " +
                    abs(rigidRollLinkResidual(d, 1.0, rP, 1e-3)).emitted(2) + " nm at 1 mrad " +
                    "where d/2 leaves exactly zero, and a rigid roll of the assembled lattice " +
                    "stores " + rigidTotal.emitted(2) + " pN*nm in total"
        ),
        T297Falsifier(
            id = "F4",
            statement = "a uniform pressure on the free tied lattice dishes anything but zero, " +
                    "at either end of the link ladder",
            fired = uniformDishing > T297_IDENTITY,
            outcome = "the worst reading over both ends is " + uniformDishing.emitted(2) + " nm"
        ),
        T297Falsifier(
            id = "F5",
            statement = "DECLARED OPEN -- the free tile's flatness verdict moves across T-5b's " +
                    "0.10 between the two ends of the link ladder",
            fired = verdictMoves > 0,
            outcome = verdictMoves.toString() + " of " +
                    sweep.groupBy { it.crossSection to it.hingeStiffnessEnhancement }.size +
                    " (cross-section, coupling) pairs move a verdict over the whole ladder; " +
                    "the worst relative spread in the tied free tile is " +
                    worstSweepSpread.emitted(6)
        ),
        T297Falsifier(
            id = "F6",
            statement = "the link eigenstrain's field does not converge as the penalty stiffens",
            fired = !convergence[2].verdictSurvives,
            outcome = "the ladder reads " + penaltyLadder[0].emitted(9) + ", " +
                    penaltyLadder[1].emitted(9) + ", " + penaltyLadder[2].emitted(9) +
                    " of the stroke at 1e4, 1e5 and 1e6 pN/nm"
        ),
        T297Falsifier(
            id = "F7",
            statement = "the link eigenstrain has a nonzero projection on the RELATIVE roll, " +
                    "which would make it the same load the corpus already carries",
            fired = relativeProjection > 1e-9,
            outcome = "the worst projection over both link ends and all 59 ties is " +
                    relativeProjection.emitted(2) + " pN*nm, on a load that is not itself zero"
        ),
        T297Falsifier(
            id = "F8",
            statement = "C-0175 section 9's six free-tile readings are not reproduced at the " +
                    "standing link stiffness",
            fired = worstReproduction > 1e-6,
            outcome = reproductions.size.toString() + " reproductions, worst departure " +
                    worstReproduction.emitted(2)
        ),
        T297Falsifier(
            id = "F9",
            statement = "DECLARED OPEN -- the two coupled cells C-0180 recovered survive the " +
                    "derived departure once it is carried on the coordinate it lives on",
            fired = stillFlat > 0,
            outcome = stillFlat.toString() + " of " + recoveredCells.size +
                    " loaded readings are flat at the 90th percentile"
        ),
        T297Falsifier(
            id = "F10",
            statement = "DECLARED OPEN -- the COUPLED cells' verdict is as insensitive to the " +
                    "link stiffness as the free tile's is",
            fired = cells.filter { it.phase == 0 }
                .groupBy { it.columns }
                .any { (_, rows) -> rows.map { it.flatAtP90 }.toSet().size > 1 },
            outcome = "the zero-eigenstrain p90 of the two recovered cells over the ladder: " +
                    cells.filter { it.phase == 0 }.sortedWith(
                        compareBy({ it.columns }, { it.linkStiffness })
                    ).joinToString("; ") {
                        it.columns.toString() + " col at k_link " +
                                it.linkStiffness.emitted(6) + " -> " +
                                it.p90OverStroke.emitted(9) +
                                (if (it.flatAtP90) " flat" else " NOT flat")
                    }
        )
    )
    falsifiers.forEach { println("  " + it.id + (if (it.fired) "  FIRED" else "  did not fire")) }

    // ==================== the findings
    val tiedAtSpan = sweep.first {
        it.crossSection == "10 x 6" && it.hingeStiffnessEnhancement == recoveredEnhancement &&
                it.linkStiffness == kSpan
    }
    val tiedAtPenalty = sweep.first {
        it.crossSection == "10 x 6" && it.hingeStiffnessEnhancement == recoveredEnhancement &&
                it.linkStiffness == penalty
    }
    val eigenAtSpan = eigenFields.getValue(kSpan.toString() + "|" + recoveredEnhancement + "|1")
    val eigenAtPenalty = eigenFields.getValue(penalty.toString() + "|" + recoveredEnhancement + "|1")
    val findings = listOf(
        "CH-0242 is UPHELD IN PART and REFUTED in its direction. The premise -- that " +
                "HoneycombGrillage's bond and tie carry the relative roll 'and nothing else on " +
                "the azimuthal coordinates' -- is false: the vertical LINK's residual is " +
                "dW + (d/2) unitY (Phi_a + Phi_b), a function of the SUM, and it is the common " +
                "mode itself. Measured on the assembled lattice, a common roll of 1 mrad at " +
                "every beam stores " + commonRollHinge.emitted(2) + " pN*nm in the hinges and " +
                commonRollLink.emitted(9) + " pN*nm in the links.",
        "And the direction reverses. The model's own common-mode azimuthal stiffness at fixed " +
                "axes is k_link d^2 unitY^2 / 4 = " + latticeInPlane.emitted(9) +
                " pN*nm/rad in plane and " + latticeInterlayer.emitted(9) +
                " through the thickness, against CH-0242's physical " + challenge.emitted(9) +
                "x k_theta = " + (challenge * kTheta).emitted(9) + " pN*nm/rad. The lattice is " +
                (latticeInPlane / (challenge * kTheta)).emitted(9) + "x and " +
                (latticeInterlayer / (challenge * kTheta)).emitted(9) +
                "x too STIFF, not missing the spring: it sits at the RIGID end of the mode.",
        "d/2 is a theorem and r_P is not admissible. The linearised rigid roll is Phi = alpha, " +
                "W = alpha y, and the residual an arm a leaves under it is alpha unitY (2a - d) " +
                "-- zero at every bond direction iff a = d/2. The span's own arm r_P leaves " +
                abs(rigidRollLinkResidual(d, 1.0, rP, 1e-3)).emitted(2) + " nm at 1 mrad, so a " +
                "linear element may not use it, and the difference is exactly the prestress " +
                "geometric term: d^2/(2 g r_P) - d/(2 r_P) = d/g, i.e. " + linearised.emitted(9) +
                " - " + geometric.emitted(9) + " = " + challenge.emitted(9) + ".",
        "What IS wrong is a MAGNITUDE, and it is the penalty's. Attributing all of k_theta to " +
                "the span mechanism -- CH-0242's own premise -- gives a bond tension of " +
                tension.emitted(9) + " pN and a link stiffness k_R = T/g = " + kSpan.emitted(9) +
                " pN/nm, against RIGID_LINK_STIFFNESS = " + penalty.emitted(9) + " pN/nm: " +
                (penalty / kSpan).emitted(9) + "x. The tension cross-checks: over the built " +
                "span's " + (g - MeasuredBackbone.STEP_SOUTH).emitted(6) +
                " nm excess above T-71's measured C2'-endo step it implies an effective step " +
                "stiffness of " + (tension / (g - MeasuredBackbone.STEP_SOUTH)).emitted(6) +
                " pN/nm.",
        "The error that carries is measured rather than bracketed, because linkStiffness is an " +
                "EXISTING constructor argument. Over the whole ladder " + verdictMoves + " of " +
                sweep.groupBy { it.crossSection to it.hingeStiffnessEnhancement }.size +
                " (cross-section, coupling) pairs move a flatness verdict and the worst " +
                "relative spread in the tied free tile is " + worstSweepSpread.emitted(6) +
                ". At the recommended 10 x 6 and f = 0.30 the tied free tile reads " +
                tiedAtSpan.freeDishingWithTies.emitted(9) + " at k_R against " +
                tiedAtPenalty.freeDishingWithTies.emitted(9) + " at the penalty.",
        "And the departure's MAGNITUDE stops being a threshold. Because the common mode is the " +
                "link, the departure is an OFFSET in the link's own residual, R0 = d unitY rho " +
                "-- a load, so no entry of the stiffness matrix moves and C-0104's trap does " +
                "not arise. Its projection on the relative roll is " +
                relativeProjection.emitted(2) + " pN*nm at every one of the 59 ties, which is " +
                "the exact mirror of C-0190's F2. The free tile at 10 x 6, f = 0.30 reads " +
                eigenAtSpan.emitted(9) + " at k_R and " + eigenAtPenalty.emitted(9) +
                " at the penalty, against C-0190's per-beam twist reading of the same tile.",
        "But the FREE tile's insensitivity to the penalty does not transfer to the COUPLED " +
                "cells, and F10 fired. The two cells C-0180 recovered are flat at the standing " +
                "penalty and NOT flat at the span law's own k_R: " +
                cells.filter { it.phase == 0 }.sortedWith(
                    compareBy({ it.columns }, { it.linkStiffness })
                ).joinToString("; ") {
                    it.columns.toString() + " col at k_link " + it.linkStiffness.emitted(6) +
                            " -> " + it.p90OverStroke.emitted(9) +
                            (if (it.flatAtP90) " flat" else " NOT flat")
                } + ". So the corpus's coupled recovery rests on a link stiffness the span law " +
                "alone does not supply, and k_R is a pure-tension LOWER bound on it -- the " +
                "connector's own bending and the junction's stacking are not in it.",
        "Graded on the two cells C-0190 quoted its threshold on, " + stillFlat + " of " +
                recoveredCells.size + " loaded readings are flat at the 90th percentile. " +
                "Convergence: " + convergence.count { it.verdictSurvives } + " of " +
                convergence.size + " axes leave the verdict standing, worst departure " +
                convergence.maxOf { it.departure }.emitted(2) + ". Reproductions: " +
                reproductions.size + ", worst departure " + worstReproduction.emitted(2) + ".",
        "One number in circulation is wrong and no verdict of any claim turns on it. " +
                "1 + 2 r_P/(d - 2 r_P) = d/(d - 2 r_P) = " + challenge.emitted(9) +
                ", where CH-0242's headline and section 1, C-0190's headline and section 6, " +
                "the challenges index and two prose strings of T-291's own result file all " +
                "carry 3.52810239 -- 0.0105 % low. T-291's openQuestions block emitted the " +
                "right one from the same expression, so the artifact carried the correction " +
                "the prose did not."
    )
    findings.forEach { println("  " + it) }

    val result = T297Result(
        task = "T-297",
        leaf = "A8.2",
        title = "A crossover's COMMON azimuthal mode is the vertical LINK, so the lattice sits " +
                "at the RIGID end of it rather than missing it -- and what is wrong is the " +
                "penalty's magnitude, " + (penalty / kSpan).emitted(9) +
                "x the span law's own T/g",
        verificationType = "logical (the element decomposition, the frame-indifference theorem " +
                "that fixes the connector arm at d/2, and the exact identity " +
                "d^2/(2 g r_P) - d/(2 r_P) = d/g, all algebra on the corpus's own " +
                "turnPhosphateSpan and on the committed source of HoneycombGrillage.assemble) " +
                "+ in-silico (the assembled stiffness matrix probed directly through the " +
                "lattice's own energy accessors, the free tile swept over the link stiffness, " +
                "and the departure carried as a link eigenstrain and graded through C-0058's " +
                "exact Woodbury surrogate on C-0167's stations, C-0087's measured " +
                "incorporation and " + t297Realisations + " realisations of one common stream)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "bond tension T = 2 k_theta / r_P is an ATTRIBUTION, not a measurement: it " +
                "assumes, with CH-0242, that both eigenmodes of the span form are one " +
                "mechanism, and k_theta is Chen et al.'s fitted dihedral constant with nothing " +
                "in it resolved into stacking, backbone and junction geometry. Every number " +
                "here is conditional on the raster's turns carrying ZERO unpaired nucleotides " +
                "(C-0175's modelling choice, T-296).",
        units = linkedMapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm, and k_BT = " + kt.emitted(9) + " pN*nm at 300 K",
            "angle" to "degrees at every API, radians only where a lattice is loaded",
            "rotationalStiffness" to "pN*nm/rad",
            "translationalStiffness" to "pN/nm",
            "dishing" to "dimensionless, as a fraction of the closed-form free stroke"
        ),
        conventions = linkedMapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "z" to "along the block's thickness",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "Phi" to "the roll about a beam's own axis, positive so that a point at +y offset " +
                    "moves by +Phi*y in W",
            "theta_u, theta_l" to "the two backbone azimuths off the line of centres in the " +
                    "SAME rotational sense, ForcedCrossoverPrice's (theta, 180 + theta)",
            "R" to "the link residual dW + (d/2) unitY (Phi_a + Phi_b), which IS the common mode",
            "g" to "d - 2 r_P, the phosphate span at zero departure"
        ),
        parameters = linkedMapOf(
            "interhelicalDistance" to d.emitted(9) + " nm (SAXS, honeycomb)",
            "phosphateRadius" to rP.emitted(9) + " nm (T-71, measured on 13 084 linkages)",
            "spanFloor" to g.emitted(9) + " nm",
            "hingeStiffness" to kTheta.emitted(9) + " pN*nm/rad (Gen1Tile, alpha = 1)",
            "slipStiffness" to Gen1Tile.crossoverInPlaneStiffness().emitted(9) + " pN/nm",
            "linkStiffnessLadder" to ladder.joinToString(", ") { it.emitted(9) } + " pN/nm",
            "spanDerivedLinkStiffness" to kSpan.emitted(9) + " pN/nm",
            "standingPenalty" to penalty.emitted(9) + " pN/nm (RIGID_LINK_STIFFNESS)",
            "rowBasePairs" to T297_ROW_BP.toString() + " bp = " +
                    (T297_ROW_BP * Gen1Tile.RISE_PER_BASE_PAIR).emitted(9) + " nm",
            "crossSections" to "10 x 6 and 15 x 4, 60 helices each",
            "raster" to T297_RECOMMENDED_ONE.toString() + " / " + T297_RECOMMENDED_TWO +
                    " bp (C-0151, drawable)",
            "allowedDeparture" to allowed.emitted(9) + " degrees",
            "foundation" to "C-0001's secant, on the gap-facing face only",
            "collar" to "C-0022's solved profile at 2 mM / 10 nm / 0.192 V",
            "targetForce" to Gen1Tile.TARGET_FORCE.emitted(9) + " pN over the face",
            "tolerance" to T297_TOLERANCE.emitted(9) + " (T-5b)",
            "samples" to T297_SAMPLES.toString() + " x " + T297_SAMPLES,
            "realisations" to t297Realisations.toString(),
            "seed" to T297_SEED.toString(),
            "distribution" to "rim-graded 5:1 at a " + T297_RIM_BAND.emitted(9) + " nm band, " +
                    "C-0058's own",
            "mandate" to "C-0017's 33.3333 pN/nm on the SUM"
        ),
        sources = listOf(
            "gpd/results/T-3b-tile-edge-load-profile.json",
            "gpd/results/T-254-raster-turn-prestrain.json",
            "gpd/results/T-291-common-mode-departure-and-beam-twist.json"
        ),
        citedInputs = listOf(
            "CH-0242's ratio, re-derived here rather than inherited: the circulating " +
                    "3.52810239 is 0.0105 % below the correct " + challenge.emitted(9),
            "k_theta = " + kTheta.emitted(9) + " pN*nm/rad is Chen et al.'s fitted SQUARE-" +
                    "lattice dihedral constant; no honeycomb measurement of it exists here",
            "RIGID_LINK_STIFFNESS = " + penalty.emitted(9) + " pN/nm is OrigamiGrillage's own " +
                    "penalty, priced in its KDoc against the duplex stretch modulus",
            "r_P = " + rP.emitted(9) + " nm and the C2'-endo step " +
                    MeasuredBackbone.STEP_SOUTH.emitted(9) + " nm are T-71's measurements"
        ),
        cheapBound = cheap,
        channel = channel,
        probe = probe,
        sweep = sweep,
        eigenstrain = eigenstrain,
        cells = cells,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "T = 2 k_theta / r_P is an ATTRIBUTION of the whole of k_theta to the span " +
                    "mechanism. It is CH-0242's own premise -- without it the challenge's own " +
                    "ratio does not follow either -- but if k_theta is largely stacking or " +
                    "backbone bending rather than connector tension, T is smaller and the " +
                    "span-derived link stiffness is smaller with it. The direction of that " +
                    "error is known: it makes the penalty MORE wrong, never less.",
            "The link's arm d/2 is forced by frame indifference WITHIN this kinematics. A " +
                    "model carrying an in-plane transverse coordinate could use the geometry's " +
                    "own r_P and would then also carry the prestress geometric term this one " +
                    "excludes; the lattice has no such coordinate (C-0154 section 9).",
            "The model's common-mode stiffness is unitY-dependent -- four times larger at an " +
                    "in-plane bond than at an interlayer one -- where the span form has no " +
                    "such anisotropy. That is the same missing coordinate, seen from the side.",
            "Every number is conditional on the raster's turns carrying ZERO unpaired " +
                    "nucleotides. Douglas et al. allot 28 nt per helix as front and rear loops, " +
                    "and a turn with 28 unpaired nucleotides demands no azimuth at all (T-296).",
            "The lattice carries no across-helix parallel-axis term, so its D_perp is the " +
                    "independent one and a lower bound; Kirchhoff is not safe at these " +
                    "thicknesses, so every D_parallel is an upper bound.",
            "The dropout statistics are measured on a single-layer Rothemund rectangle and " +
                    "only the profile transfers, in nm; the ensemble perturbs the COUPLING and " +
                    "never the block's own crossovers or its ties.",
            "Nothing here re-opens the placement search, the distribution rule, the raster, " +
                    "the cross-section or the departure's magnitude in degrees.",
            "The two cells graded are the two C-0190 section 6 quotes its threshold on and not " +
                    "the whole 64-cell census; a full re-grade at a moved link stiffness is not " +
                    "run here."
        ),
        openQuestions = listOf(
            "What the crossover connector's force-extension law actually is. The ratio between " +
                    "the two azimuthal springs is convention-free; the absolute link stiffness " +
                    "is T/g and T is attributed rather than measured.",
            "Whether the whole 64-cell census of C-0167 moves at the span-derived link " +
                    "stiffness. Only the two recovered cells are graded here.",
            "What an in-plane transverse coordinate would buy. It would make the link's arm " +
                    "the geometry's r_P rather than d/2, remove the unitY anisotropy, and let " +
                    "the prestress geometric term be carried honestly rather than excluded.",
            "Whether C-0190's per-beam twist and this study's link eigenstrain are the same " +
                    "load in the limit of a rigid link. They load different coordinates of the " +
                    "same demand, and the corpus now carries both."
        ),
        proseFailure = "none"
    )

    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    val encoded = json.encodeToJsonElement(result)
        .roundedForResult(digits = 9, floor = 1e-12)
        .withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject
    val out = File("gpd/results/T-297-the-common-mode-is-the-link.json")
    out.parentFile?.mkdirs()
    out.writeText(json.encodeToString(JsonObject.serializer(), encoded) + "\n")
    println("T-297 - written to " + out.path)
}
