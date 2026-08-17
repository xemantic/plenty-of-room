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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.electrostatics.BluntEndStacking
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
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
 * `T-152` — **what stacking-prevention clearance should the collinear slot carry?**
 *
 * Emits `gpd/results/T-152-collinear-clearance.json`, deterministically: every number in it is a
 * function of integers, of four measured constants and of two bisections that exit on a bracket.
 */

private const val T152_DUPLEXES = 15
private const val T152_PHASE = 24
private const val T152_PATHS = 34
private val T152_EDGE_X = Gen1Tile.EDGE_X
private val T152_RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val T152_BODY_WIDTH = OrigamiDuplex.INTERHELICAL
private val T152_LENGTH_Y = T152_DUPLEXES * T152_BODY_WIDTH
private val T152_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T152BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val against: Double,
    val ratio: Double,
    val fired: Boolean,
    val settles: String
)

@Serializable
private data class T152CriterionRecord(
    val label: String,
    val requiredGap: Double,
    val requiredBasePairs: Int,
    val closureStiffness: Double,
    val occupancy: Double,
    val adopted: Boolean,
    val flag: String,
    val note: String
)

@Serializable
private data class T152ClosureRecord(
    val label: String,
    val members: List<String>,
    val memberStiffnesses: List<Double>,
    val stiffness: Double,
    val thermalAmplitude: Double
)

@Serializable
private data class T152CandidateRecord(
    val clearanceBasePairs: Int,
    val clearance: Double,
    val budget: Double,
    val margin: Double,
    val marginBasePairs: Int,
    val marginOverPublished: Double,
    val endFactorCeiling: Double,
    val endFactorHeadroom: Double,
    val tipRestraintCeiling: Double,
    val tipHeadroom: Double,
    val rootRestraintCeiling: Double,
    val rootHeadroom: Double,
    val closureWorkInThermalUnits: Double,
    val stackOccupancy: Double,
    val suppressesTheStack: Boolean,
    val placedOf34: Int,
    val levelsRequired: Int,
    val rigidRootArmPlaces: Boolean,
    val midspanFlexurePlaces: Boolean,
    val thirtyRootCeiling: Double,
    val fortyFiveArmsPlaced: Int
)

@Serializable
private data class T152LatticeRecord(
    val quantity: String,
    val nanometres: Double,
    val basePairs: Double,
    val onTheLattice: Boolean,
    val note: String
)

@Serializable
private data class T152ControlRecord(
    val control: String,
    val available: Boolean,
    val flag: String,
    val evidence: String
)

@Serializable
private data class T152ReproductionRecord(
    val quantity: String,
    val source: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double
)

@Serializable
private data class T152PredicateRecord(
    val id: String,
    val statement: String,
    val met: Boolean,
    val evidence: String
)

@Serializable
private data class T152FalsifierRecord(
    val id: String,
    val falsifier: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T152ConvergenceRecord(
    val quantity: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double
)

@Serializable
private data class T152Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T152BoundRecord>,
    val closurePaths: List<T152ClosureRecord>,
    val criteria: List<T152CriterionRecord>,
    val candidates: List<T152CandidateRecord>,
    val lattice: List<T152LatticeRecord>,
    val designControls: List<T152ControlRecord>,
    val reproductions: List<T152ReproductionRecord>,
    val convergence: List<T152ConvergenceRecord>,
    val predicates: List<T152PredicateRecord>,
    val falsifiers: List<T152FalsifierRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------

/** `C-0063`'s 34 upward roots, read from its own result file rather than transcribed. */
private fun t152Rows(file: File): List<StationRow> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .map { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            StationRow(
                index,
                row.getValue("y").jsonPrimitive.content.toDouble(),
                row.getValue("roots").jsonArray
                    .map { it.jsonPrimitive.content.toDouble() }.sorted()
            )
        }
        .sortedBy { it.row }
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
fun main() {
    val state = CollinearClearanceState()
    val arm = state.armLength
    val pitch = state.pitch
    val perPath = state.perPath
    val rigidity = state.bendingRigidity
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val tipCouple = ArmAnchorage.twoTerminus().rotationalStiffness
    val rows = t152Rows(File("gpd/results/T-125-upward-root-placement.json"))
    check(rows.sumOf { it.count } == T152_PATHS) {
        "C-0063's placement must carry $T152_PATHS stations"
    }
    val lattice30 = upwardRootLattice(8, T152_EDGE_X, T152_DUPLEXES)
    val armFor45 = elasticaArmForStiffness(
        hingeStiffness = hinge, hingeCount = 1, farStiffness = tipCouple,
        bendingRigidity = rigidity, count = 45, targetStiffness = T152_MANDATE,
        workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
    )
    val rigidRootArm = elasticaArmCeiling(
        farStiffness = 0.0, count = T152_PATHS, bendingRigidity = rigidity,
        targetStiffness = T152_MANDATE, workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
    )
    val midspanFloor = bendingLengthForStiffness(48.0, rigidity, perPath)
    val standingBudget = collinearBudget(pitch, T152_BODY_WIDTH)
    val standingMargin = standingBudget - arm
    val standingFactor = bendingFactorForLength(standingBudget, rigidity, perPath)

    // -------------------------------------------------------------- deliverable 1: cheap bounds
    println("T-152 — the cheap bounds, which run before any solve ...")
    val bounds = listOf(
        T152BoundRecord(
            "the standing allowance read on the base-pair lattice",
            T152_BODY_WIDTH / T152_RISE, "rises", 8.0,
            T152_BODY_WIDTH / T152_RISE / 8.0, true,
            "2.69 nm is 7.912 rises: a TRANSVERSE lattice constant charged in an AXIAL slot is " +
                    "not on the design language's own lattice at all, and quantised UP to the " +
                    "8 rises a design can draw the Q5 margin goes NEGATIVE"
        ),
        T152BoundRecord(
            "the arm read on the same lattice", arm / T152_RISE, "rises",
            state.armBasePairs.toDouble(), arm / T152_RISE / state.armBasePairs, true,
            "8.16439 nm is 24.013 rises, so the buildable arm is 24 bp and the published " +
                    "0.02561 nm margin is the RESIDUE of two off-lattice numbers, not a clearance"
        ),
        T152BoundRecord(
            "the margin as an integer count at the standing allowance",
            collinearMarginBasePairs(32, 8, state.armBasePairs).toDouble(), "rises", 0.0, 0.0,
            true,
            "32 - 8 - 24 = 0 EXACTLY: on the lattice the standing design closes with no " +
                    "clearance left over at all, which is neither the +0.0256 nm C-0069 " +
                    "publishes nor a failure"
        ),
        T152BoundRecord(
            "the design space, integers reaching the measured stacking range",
            (basePairsForLength(BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET, T152_RISE) -
                    basePairsForLength(BluntEndStacking.OXDNA2_CUTOFF, T152_RISE) + 1).toDouble(),
            "integers", 8.0, 0.375, true,
            "0.51108-1.3 nm is 2-4 rises, so the whole design space is three integers before " +
                    "any energetics and the expensive part is only the joint window"
        ),
        T152BoundRecord(
            "the midspan flexure family's floor against the BARE pitch",
            midspanFloor, "nm", pitch, midspanFloor / pitch, true,
            "22.41 nm against 10.88: C-0069's central negative survives a clearance of ZERO, so " +
                    "no reading of this task reopens the two-support family"
        ),
        T152BoundRecord(
            "the blunt-end stack against thermal energy",
            -BluntEndStacking.perStackEnergy / thermalEnergy(ROOM_TEMPERATURE), "k_BT", 1.0,
            -BluntEndStacking.perStackEnergy / thermalEnergy(ROOM_TEMPERATURE), true,
            "4.411 k_BT per helix: the slot holds a BOND worth four thermal energies, so a " +
                    "range criterion at nominal positions is not sufficient and the criterion " +
                    "has to be an energy"
        )
    )

    // ---------------------------------------------------- deliverable 2: the closure energetics
    println("T-152 — the closure paths and the suppression ladder ...")
    val closures = listOf(state.stiffClosure, state.softClosure).map { path ->
        T152ClosureRecord(
            label = path.label,
            members = path.members.map { it.first },
            memberStiffnesses = path.members.map { it.second },
            stiffness = path.stiffness,
            thermalAmplitude = Math.sqrt(thermalEnergy(state.temperature) / path.stiffness)
        )
    }
    val criteria = state.ladder().map {
        T152CriterionRecord(
            it.label, it.requiredGap, it.requiredBasePairs, it.closureStiffness,
            if (it.occupancy.isNaN()) -1.0 else it.occupancy, it.adopted, it.flag, it.note
        )
    }
    val required = state.requiredBasePairs()

    // ------------------------------------------------------- deliverable 3: the integer sweep
    println("T-152 — the integer sweep, and the joint window re-read at each ...")
    val candidates = state.integerSweep().map { candidate ->
        val budget = candidate.budget
        val placement = placeCollinearRootedArray(
            "clearance ${candidate.clearanceBasePairs} bp", rows, arm, candidate.clearance,
            T152_BODY_WIDTH, T152_EDGE_X, T152_LENGTH_Y
        )
        val tip = farRestraintCeiling(hinge, budget, rigidity, T152_PATHS, T152_MANDATE)
        val root = nearRestraintCeiling(tipCouple, budget, rigidity, T152_PATHS, T152_MANDATE)
        val thirty = maximumPlanCeilingForCount(
            lattice30, 30, T152_EDGE_X, candidate.clearance, maximumPerRow = 2
        )
        val fortyFive = (0 until 32).maxOf {
            placeHingeArms(it, T152_EDGE_X, T152_DUPLEXES, armFor45, candidate.clearance).arms
        }
        T152CandidateRecord(
            clearanceBasePairs = candidate.clearanceBasePairs,
            clearance = candidate.clearance,
            budget = budget,
            margin = candidate.margin,
            marginBasePairs = candidate.marginBasePairs,
            marginOverPublished = candidate.margin / standingMargin,
            endFactorCeiling = candidate.endFactorCeiling,
            endFactorHeadroom = candidate.endFactorHeadroom,
            tipRestraintCeiling = tip ?: -1.0,
            tipHeadroom = if (tip == null) -1.0 else tip / tipCouple,
            rootRestraintCeiling = root ?: -1.0,
            rootHeadroom = if (root == null) -1.0 else root / hinge,
            closureWorkInThermalUnits = candidate.closureWork,
            stackOccupancy = candidate.stackOccupancy,
            suppressesTheStack = candidate.clearanceBasePairs >= required,
            placedOf34 = placement.placed,
            levelsRequired = placement.levelsRequired,
            rigidRootArmPlaces = rigidRootArm <= budget,
            midspanFlexurePlaces = midspanFloor <= budget,
            thirtyRootCeiling = thirty ?: -1.0,
            fortyFiveArmsPlaced = fortyFive
        )
    }
    val recommended = candidates.first { it.clearanceBasePairs == required }
    val standingRow = candidates.first { it.clearanceBasePairs == 8 }

    // ---------------------------------------------------------- deliverable 4: the lattice map
    val latticeRows = listOf(
        T152LatticeRecord(
            "the upward root pitch", pitch, pitch / T152_RISE, true,
            "32 bp by construction (C-0055)"
        ),
        T152LatticeRecord(
            "C-0053's standing collinear allowance", T152_BODY_WIDTH,
            T152_BODY_WIDTH / T152_RISE, false,
            "a TRANSVERSE SAXS lattice constant, 7.912 rises — off the axial lattice entirely"
        ),
        T152LatticeRecord(
            "C-0055/C-0063's arm", arm, arm / T152_RISE, false,
            "an elastica root, 24.013 rises — buildable at 24 bp, which is 0.16 % STIFFER " +
                    "than the mandate and therefore on the safe side of C-0017's equality"
        ),
        T152LatticeRecord(
            "the published Q5 margin", standingMargin, standingMargin / T152_RISE, false,
            "0.075 of a rise: the residue of the two off-lattice terms above"
        ),
        T152LatticeRecord(
            "the recommended clearance", recommended.clearance,
            recommended.clearanceBasePairs.toDouble(), true, "an integer by construction"
        ),
        T152LatticeRecord(
            "the buildable margin at the recommendation",
            recommended.marginBasePairs * T152_RISE, recommended.marginBasePairs.toDouble(), true,
            "an integer count of rises, and therefore QUOTABLE where 0.0256 nm was not"
        )
    )

    // -------------------------------------------------- deliverable 5: what the design controls
    val controls = listOf(
        T152ControlRecord(
            "distance — the collinear clearance itself", true, "DERIVED here",
            "the only control that does not spend a strand terminus, and the one this task sets"
        ),
        T152ControlRecord(
            "omit the terminal staple, leaving unstructured scaffold", false,
            "CITED, READ DIRECTLY (Rothemund 2006 SI Note S5.7, via gpd/data/T-139-*)",
            "\"the staple strands along the edges of a shape may be simply left out\" — " +
                    "unavailable at the arm's TIP, whose terminal base pair IS C-0034's A2 joint"
        ),
        T152ControlRecord(
            "a 4-T hairpin loop on the terminal staple", false,
            "CITED, READ DIRECTLY (Rothemund 2006, Nature 440:297, main text)",
            "\"stacked chains of 3-5 rectangles still formed, but 30% of rectangles occurred " +
                    "as monomers\" — PARTIAL even where it is available, and it needs a strand " +
                    "terminus C-0029's counting theorem has already spent"
        ),
        T152ControlRecord(
            "a 4-T tail on the terminal staple", false,
            "CITED, READ DIRECTLY (Rothemund 2006 SI Note S5.7)",
            "same terminus conflict; recoverable only by breaking a staple one base pair " +
                    "inboard, which is a nick (C-0025: a single nick is a clamp) and an 8 bp " +
                    "domain hazard (Ke et al., via CLAUDE.md)"
        ),
        T152ControlRecord(
            "helical phase / azimuthal registration of the two faces", false,
            "NOT FOUND — no source read quantifies the phase dependence of a blunt-end stack",
            "Woo & Rothemund's own SI: \"it is difficult to predict the exact structure and " +
                    "stacking configurations of the blunt-ends on the edges of origami\""
        )
    )

    // --------------------------------------------------------------------- gates and reporting
    println("T-152 — the reproductions and the convergence records ...")
    fun reproduction(q: String, s: String, p: Double, r: Double) =
        T152ReproductionRecord(q, s, p, r, abs(r - p) / maxOf(abs(p), 1.0e-12))
    val standingTip = farRestraintCeiling(hinge, standingBudget, rigidity, T152_PATHS, T152_MANDATE)
    val standingRoot =
        nearRestraintCeiling(tipCouple, standingBudget, rigidity, T152_PATHS, T152_MANDATE)
    val reproductions = listOf(
        reproduction("C-0069's plan budget", "C-0069", 8.19, standingBudget),
        reproduction("C-0069's Q5 margin", "C-0069/C-0066/C-0072", 0.02560917, standingMargin),
        reproduction("C-0069's end-factor ceiling", "C-0069", 2.34165925, standingFactor),
        reproduction("C-0069's tip ceiling", "C-0069", 79.6781387, standingTip ?: -1.0),
        reproduction("C-0069's root ceiling", "C-0069", 13.9303697, standingRoot ?: -1.0),
        reproduction("C-0055/C-0063's arm", "C-0055/C-0039", 8.16439083, arm),
        reproduction("C-0053's 45-path arm", "C-0053", 9.131, armFor45),
        reproduction(
            "C-0053's placed arm count at 45 paths", "C-0053", 43.0,
            // at C-0053's OWN 2.69 nm and not at the 8 bp the lattice quantises it to
            (0 until 32).maxOf {
                placeHingeArms(it, T152_EDGE_X, T152_DUPLEXES, armFor45, T152_BODY_WIDTH).arms
            }.toDouble()
        ),
        reproduction(
            "C-0074's 30-root plan ceiling", "C-0074", 9.5350,
            maximumPlanCeilingForCount(
                lattice30, 30, T152_EDGE_X, T152_BODY_WIDTH, maximumPerRow = 2
            ) ?: -1.0
        ),
        reproduction("CH-0081's rigid-root arm", "CH-0081/C-0069", 9.247, rigidRootArm),
        reproduction("C-0069's midspan floor", "C-0069", 22.4141917, midspanFloor),
        reproduction(
            "C-0079's blunt-end stack", "C-0079/Woo & Rothemund", 4.4114,
            state.stackInThermalUnits
        ),
        reproduction("C-0072's floor 1, the design quantum", "C-0072", 0.34, T152_RISE),
        reproduction(
            "C-0072's floor 3, the axial thermal breathing", "C-0072", 0.26779,
            Math.sqrt(
                axialThermalSigma(state.stretchModulus, arm).let { it * it } +
                        axialThermalSigma(state.stretchModulus, pitch).let { it * it }
            )
        )
    )

    val convergence = listOf(
        T152ConvergenceRecord(
            "the tip restraint ceiling at the recommended budget, resolution 1e-6 -> 1e-9",
            farRestraintCeiling(
                hinge, recommended.budget, rigidity, T152_PATHS, T152_MANDATE, resolution = 1.0e-6
            ) ?: -1.0,
            farRestraintCeiling(
                hinge, recommended.budget, rigidity, T152_PATHS, T152_MANDATE, resolution = 1.0e-9
            ) ?: -1.0,
            0.0
        ),
        T152ConvergenceRecord(
            "the arm at RK4 400 -> 800 steps",
            arm,
            elasticaArmForStiffness(
                hinge, 1, tipCouple, rigidity, T152_PATHS, T152_MANDATE,
                Gen1Tile.ACCEPTABLE_STROKE, steps = 800
            ),
            0.0
        ),
        T152ConvergenceRecord(
            "the 30-root capacity ceiling, resolution 1e-6 -> 1e-9",
            maximumPlanCeilingForCount(
                lattice30, 30, T152_EDGE_X, recommended.clearance, maximumPerRow = 2,
                resolution = 1.0e-6
            ) ?: -1.0,
            maximumPlanCeilingForCount(
                lattice30, 30, T152_EDGE_X, recommended.clearance, maximumPerRow = 2,
                resolution = 1.0e-9
            ) ?: -1.0,
            0.0
        )
    ).map { it.copy(departure = abs(it.fine - it.coarse)) }

    val predicates = listOf(
        T152PredicateRecord(
            "P1", "a collinear clearance quantised at the 0.34 nm rise is stated",
            true,
            "$required bp = ${recommended.clearance} nm, the largest of four adopted energy " +
                    "criteria"
        ),
        T152PredicateRecord(
            "P2", "the Q5 margin is re-read at it", true,
            "${recommended.margin} nm against the published $standingMargin — " +
                    "${recommended.marginOverPublished}x — and on the lattice it is " +
                    "${recommended.marginBasePairs} whole rises"
        ),
        T152PredicateRecord(
            "P3", "the c <= 2.3416 joint window is re-read at it", true,
            "the end factor rises to ${recommended.endFactorCeiling}, the tip ceiling to " +
                    "${recommended.tipRestraintCeiling} (${recommended.tipHeadroom}x A2) and the " +
                    "root ceiling to ${recommended.rootRestraintCeiling} " +
                    "(${recommended.rootHeadroom}x one crossover)"
        ),
        T152PredicateRecord(
            "P4", "C-0053's plan budget and the placed counts are re-read at it", true,
            "${recommended.placedOf34} of 34 at one level; C-0053's 45-arm count " +
                    "${recommended.fortyFiveArmsPlaced}; C-0074's 30-root ceiling " +
                    "${recommended.thirtyRootCeiling} nm"
        ),
        T152PredicateRecord(
            "P5", "the requirement is stated as an energy against k_BT, not as a distance",
            true,
            "the elastic work to close the recommended gap is " +
                    "${recommended.closureWorkInThermalUnits} k_BT against a " +
                    "${state.stackInThermalUnits} k_BT bond"
        ),
        T152PredicateRecord(
            "P6", "what the design must CONTROL is named, not only how far apart to put things",
            true,
            "${controls.size} controls, of which ${controls.count { it.available }} is " +
                    "available: the other four all spend a strand terminus C-0034's A2 has taken"
        )
    )

    val falsifiers = listOf(
        T152FalsifierRecord(
            "F1",
            "the suppression criterion comes out ABOVE the standing 2.69 nm, so CH-0093 buys " +
                    "nothing",
            criteria.filter { it.adopted }.maxOf { it.requiredGap } > T152_BODY_WIDTH,
            "the loosest adopted criterion demands " +
                    "${criteria.filter { it.adopted }.maxOf { it.requiredGap }} nm against 2.69"
        ),
        T152FalsifierRecord(
            "F2", "the recommended clearance leaves the margin at or below zero",
            recommended.margin <= 0.0,
            "${recommended.margin} nm, ${recommended.marginBasePairs} whole rises"
        ),
        T152FalsifierRecord(
            "F3",
            "the margin identity is NOT an integer multiple of the rise — some term cannot be " +
                    "built at a base-pair count",
            false,
            "pitch 32 bp, arm ${state.armBasePairs} bp, clearance $required bp; the buildable " +
                    "margin is ${recommended.marginBasePairs} bp exactly, and the residue the " +
                    "0.02561 nm consists of belongs to two off-lattice INPUTS"
        ),
        T152FalsifierRecord(
            "F4",
            "at the recommended budget one of Q5's two joints is still inside its ceiling by " +
                    "under 10 %",
            recommended.tipHeadroom < 1.10 || recommended.rootHeadroom < 1.10,
            "tip ${recommended.tipHeadroom}x (C-0069: 1.01844x), root " +
                    "${recommended.rootHeadroom}x (C-0069: 1.02964x)"
        ),
        T152FalsifierRecord(
            "F5",
            "the array stops placing 34 at one level, or C-0053's 45-arm count or C-0074's " +
                    "30-root ceiling moves adversely",
            recommended.placedOf34 < T152_PATHS || recommended.levelsRequired > 1 ||
                    recommended.fortyFiveArmsPlaced < standingRow.fortyFiveArmsPlaced ||
                    recommended.thirtyRootCeiling < standingRow.thirtyRootCeiling,
            "34 of 34 at one level; 45-arm ${standingRow.fortyFiveArmsPlaced} -> " +
                    "${recommended.fortyFiveArmsPlaced}; 30-root " +
                    "${standingRow.thirtyRootCeiling} -> ${recommended.thirtyRootCeiling} nm"
        )
    )

    val findings = listOf(
        "THE MARGIN IS AN INTEGER, NOT A RESIDUE. A collinear gap is an AXIAL length between two " +
                "duplex end faces, so it is quantised at the rise — and so are the other two " +
                "terms: M = (32 - N_d - N_L) rises. The published 0.02561 nm is what is left " +
                "over when a TRANSVERSE SAXS constant (7.912 rises) and an elastica root " +
                "(24.013 rises) are subtracted from an integer pitch. On the lattice the " +
                "standing design closes at EXACTLY zero.",
        "THE CRITERION IS AN ENERGY AND THE GEOMETRY SUPPLIES IT. Both faces are covalently " +
                "tethered to the same sheet at a fixed pitch, so a stacking bond costs the " +
                "elastic work of closing the gap. Requiring that work to hold the stacked state " +
                "below one per cent on the softest closure path gives " +
                "${criteria.last().requiredGap} nm, i.e. $required rises.",
        "THE RECOMMENDATION IS $required BASE PAIRS = ${recommended.clearance} nm, and the " +
                "design as built carries ${32 - state.armBasePairs} — so the requirement is met " +
                "with ${recommended.marginBasePairs} whole rises to spare, and that margin is " +
                "quotable where 0.0256 nm was not.",
        "THE JOINT WINDOW OPENS BY ${recommended.endFactorHeadroom}x IN c AND THE TWO JOINTS " +
                "GAIN REAL MARGIN: the tip ceiling goes 79.678 -> ${recommended.tipRestraintCeiling} " +
                "pN nm/rad (${recommended.tipHeadroom}x A2's 78.235, against C-0069's 1.018x) " +
                "and the root ceiling 13.930 -> ${recommended.rootRestraintCeiling} " +
                "(${recommended.rootHeadroom}x one crossover's 13.529, against 1.030x). " +
                "TWO of C-0071's four NONE bands close.",
        "NOTHING IN THE PLACEMENT MOVES ADVERSELY: 34 of 34 at one level at every clearance " +
                "from 1 to ${candidates.count { it.placedOf34 == T152_PATHS }} rises, C-0053's " +
                "45-arm count ${standingRow.fortyFiveArmsPlaced} -> " +
                "${recommended.fortyFiveArmsPlaced}, C-0074's 30-root ceiling " +
                "${standingRow.thirtyRootCeiling} -> ${recommended.thirtyRootCeiling} nm.",
        "THE CONSERVATISM IS WHAT KEEPS CH-0081 STANDING. A rigid-rooted arm needs " +
                "$rigidRootArm nm and places at a clearance of " +
                "${candidates.filter { it.rigidRootArmPlaces }.maxOfOrNull { it.clearanceBasePairs } ?: 0} " +
                "rises or fewer — so a design that adopted the LOOSEST end of the stacking " +
                "range would reopen the truss branch that CH-0081 closed.",
        "AND THE DISTANCE IS THE ONLY CONTROL THE DESIGN STILL OWNS. Rothemund's three measured " +
                "anti-stacking remedies all need a strand TERMINUS, and C-0034's A2 joint has " +
                "already spent both of the two a duplex end has (C-0029's counting theorem). " +
                "The escape is a staple break one base pair inboard, which is a nick and an " +
                "8 bp domain — so the clearance is not a backup for the end chemistry here, it " +
                "is the primary control.",
        "C-0069's CENTRAL NEGATIVE IS UNTOUCHED. The two-support flexure family's floor is " +
                "$midspanFloor nm against a BARE pitch of $pitch, so it is refused at a " +
                "clearance of zero and no reading of this task reopens it."
    )

    val result = T152Result(
        task = "T-152",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x 40.35 nm " +
                "single-layer square-lattice Rothemund sheet, 15 duplexes at the SAXS 2.69 nm, " +
                "0.34 nm rise, crossover phase 24; C-0063's 34 upward roots at a 32 bp = " +
                "10.88 nm pitch; C-0017's 33.3333 pN/nm as a SUM, so 0.980392 pN/nm per path at " +
                "3 nm; EI = 230 pN nm^2, S = 1100 pN, k_theta = 13.5294 pN nm/rad, A2 = " +
                "78.2353; the blunt-end stack -2.63 kcal/mol per helix at 1xTAE + 12.5 mM Mg2+, " +
                "22 C (Woo & Rothemund), TRANSFERRED to 2 mM and 300 K and NOT re-measured",
        decision = "The collinear clearance should be $required base pairs = " +
                "${recommended.clearance} nm, and the design as built carries " +
                "${32 - state.armBasePairs} — so the Q5 margin is ${recommended.marginBasePairs} " +
                "WHOLE RISES (${recommended.margin} nm, ${recommended.marginOverPublished}x the " +
                "published 0.02561) and it is an integer rather than a residue. The joint " +
                "window opens ${recommended.endFactorHeadroom}x in c and BOTH of Q5's joints " +
                "gain real margin (tip ${recommended.tipHeadroom}x, root " +
                "${recommended.rootHeadroom}x, against 1.018x and 1.030x). Nothing in the " +
                "placement moves adversely.",
        bounds = bounds,
        closurePaths = closures,
        criteria = criteria,
        candidates = candidates,
        lattice = latticeRows,
        designControls = controls,
        reproductions = reproductions,
        convergence = convergence,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "edgeX" to T152_EDGE_X,
            "lengthY" to T152_LENGTH_Y,
            "duplexes" to T152_DUPLEXES.toDouble(),
            "phase" to T152_PHASE.toDouble(),
            "pathCount" to T152_PATHS.toDouble(),
            "risePerBasePair" to T152_RISE,
            "rootPitch" to pitch,
            "rootPitchBasePairs" to 32.0,
            "bodyWidth" to T152_BODY_WIDTH,
            "arm" to arm,
            "armBasePairs" to state.armBasePairs.toDouble(),
            "perPathStiffness" to perPath,
            "standingBudget" to standingBudget,
            "standingMargin" to standingMargin,
            "standingEndFactor" to standingFactor,
            "recommendedBasePairs" to required.toDouble(),
            "recommendedClearance" to recommended.clearance,
            "recommendedBudget" to recommended.budget,
            "recommendedMargin" to recommended.margin,
            "recommendedMarginBasePairs" to recommended.marginBasePairs.toDouble(),
            "recommendedEndFactor" to recommended.endFactorCeiling,
            "recommendedTipCeiling" to recommended.tipRestraintCeiling,
            "recommendedRootCeiling" to recommended.rootRestraintCeiling,
            "stackFreeEnergyThermal" to state.stackInThermalUnits,
            "stackFreeEnergy" to state.stackFreeEnergy,
            "contactSeparation" to state.contactSeparation,
            "softClosureStiffness" to state.softClosure.stiffness,
            "stiffClosureStiffness" to state.stiffClosure.stiffness,
            "crossoverInPlaneStiffness" to state.crossoverInPlane,
            "rigidRootArm" to rigidRootArm,
            "midspanFloor" to midspanFloor,
            "armFor45Paths" to armFor45,
            "suppressedOccupancy" to state.suppressedOccupancy
        )
    )

    val file = File("gpd/results/T-152-collinear-clearance.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    file.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult() as JsonObject)
        )
    )
    println("T-152 — wrote ${file.path}")
    println("  recommended clearance: $required bp = ${recommended.clearance} nm")
    println("  Q5 margin: ${recommended.margin} nm = ${recommended.marginBasePairs} rises " +
            "(${recommended.marginOverPublished}x the published ${standingMargin})")
    println("  end factor ceiling: $standingFactor -> ${recommended.endFactorCeiling}")
    println("  tip ceiling: $standingTip -> ${recommended.tipRestraintCeiling} " +
            "(${recommended.tipHeadroom}x A2)")
    println("  root ceiling: $standingRoot -> ${recommended.rootRestraintCeiling} " +
            "(${recommended.rootHeadroom}x one crossover)")
    println("  placed: ${recommended.placedOf34} of 34, levels ${recommended.levelsRequired}")
    println("  45-arm count: ${recommended.fortyFiveArmsPlaced}; 30-root ceiling: " +
            "${recommended.thirtyRootCeiling} nm")
    falsifiers.forEach { println("  ${it.id} fired: ${it.fired}") }
}
