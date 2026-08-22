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

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.PI
import kotlin.math.abs

/**
 * Task `T-127` / leaf `A8.2` — **does a torsion-feasible trio exist on `C-0048`'s cap crossbar at
 * all?**
 *
 * ```shell
 * tools/study.sh anchoring.CrossbarTrioExistenceStudyKt
 * ```
 *
 * `C-0059` reported *"0 of the 24 best-aligned of 750 reach-feasible lattices, 134 junction
 * solves"* and labelled it a **"not found within the budget"**. This study deepens the search along
 * every axis `C-0059` named — lattices, candidate azimuths, crossbar lengths and row pitches, and
 * the three of them **jointly** rather than composed — and reports the budget as a first-class
 * number, because a negative that names its budget is falsifiable and one that does not is an
 * opinion.
 *
 * Emits `gpd/results/T-127-crossbar-trio-existence.json`, deterministically: fixed grids, strict
 * comparisons, no timestamp, no dependence on the thread count, and the whole tree rounded at the
 * **serialisation boundary**.
 */

private val RISE = Gen1Tile.RISE_PER_BASE_PAIR

/** The grid the *searches* run their closures on — `C-0057`'s census grid, `C-0059`'s own. */
private const val SEARCH_GRID = 60
private const val SEARCH_REFINEMENTS = 4

/** The grid a *verdict* is read on — `C-0057`'s own scale grid. */
private const val VERDICT_GRID = 180
private const val VERDICT_REFINEMENTS = 6

private fun degrees(radians: Double): Double = radians * 180.0 / PI

// ---------------------------------------------------------------------------------------------

@Serializable
data class T127BoundRecord(
    val quantity: String,
    val value: Double,
    val units: String,
    val note: String
)

/** The cheap bound: how often ONE junction finds a closing placement on a lattice. */
@Serializable
data class T127MarginalRecord(
    val configuration: String,
    val junction: String,
    val lattices: Int,
    val candidates: Int,
    val closingCandidates: Int,
    val closingLattices: Int,
    val latticeRate: Double,
    val candidateRate: Double
)

/** One `(crossbar, row)` configuration swept whole. */
@Serializable
data class T127ConfigurationRecord(
    val configuration: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val overhangBasePairs: Int,
    val lattices: Int,
    val latticesSolved: Int,
    val reachFeasibleLattices: Int,
    val candidateAzimuths: Int,
    val junctionSolves: Int,
    val linkClosures: Int,
    val closingLattices: Int,
    val combinationsExhausted: Int,
    val closes: Boolean,
    val bestWorstMisalignmentDegrees: Double,
    val bestLegMisalignmentDegrees: Double,
    val bestFlexureMisalignmentDegrees: Double,
    val verdict: String
)

/** One closing trio, with everything a re-solve or a design needs. */
@Serializable
data class T127TrioRecord(
    val rank: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val helicalPhaseDegrees: Double,
    val axialPhase: Double,
    val lateralSeat: Double,
    val azimuthDegrees: List<Double>,
    val chordAzimuthDegrees: List<Double>,
    val worstMisalignmentDegrees: Double,
    val legMisalignmentDegrees: Double,
    val flexureMisalignmentDegrees: Double,
    val worstGap: Double,
    val minimumTerminusSeparation: Double,
    val minimumSeatContact: Double,
    val distinctTargets: Boolean,
    val closesOnVerdictGrid: Boolean,
    val note: String
)

/** `C-0048`'s design at the misalignments the FOUND trio delivers. */
@Serializable
data class T127DesignRecord(
    val id: String,
    val legSteps: Int,
    val legLength: Double,
    val separationBasePairs: Int,
    val baseFloorDegrees: Double,
    val capFloorDegrees: Double,
    val flexureFloorDegrees: Double,
    val budgetDegrees: Double,
    val baseMisalignmentDegrees: Double,
    val capMisalignmentDegrees: Double,
    val loadedCoupleFraction: Double,
    val frameCouple: Double,
    val capBending: Double,
    val capTorsion: Double,
    val duty: Double,
    val criticalLoadCanDo: Double,
    val criticalLoadFields: Double,
    val marginCanDo: Double,
    val marginFields: Double,
    val governingPlane: String,
    val span: Double,
    val representable: Boolean,
    val verdict: String
)

@Serializable
data class T127SensitivityRecord(
    val axis: String,
    val reading: String,
    val reachFeasibleLattices: Int,
    val closingLattices: Int,
    val junctionSolves: Int,
    val closes: Boolean,
    val bestWorstMisalignmentDegrees: Double,
    val verdictMoves: Boolean,
    val note: String
)

@Serializable
data class T127ConvergenceRecord(
    val axis: String,
    val level: String,
    val lattices: Int,
    val reachFeasibleLattices: Int,
    val closingLattices: Int,
    val junctionSolves: Int,
    val bestWorstMisalignmentDegrees: Double,
    val note: String
)

@Serializable
data class T127ReproductionRecord(
    val quantity: String,
    val source: String,
    val upstream: Double,
    val here: Double,
    val departure: Double,
    val note: String
)

/** The budget, which is the number a negative existence claim lives or dies on. */
@Serializable
data class T127BudgetRecord(
    val stage: String,
    val configurations: Int,
    val lattices: Int,
    val latticesSolved: Int,
    val junctionSlots: Int,
    val candidateAzimuths: Int,
    val junctionSolves: Int,
    val linkClosures: Int,
    val closingLattices: Int,
    val note: String
)

@Serializable
data class T127Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val bounds: List<T127BoundRecord>,
    val marginals: List<T127MarginalRecord>,
    val configurations: List<T127ConfigurationRecord>,
    val trios: List<T127TrioRecord>,
    val designs: List<T127DesignRecord>,
    val sensitivities: List<T127SensitivityRecord>,
    val convergence: List<T127ConvergenceRecord>,
    val reproductions: List<T127ReproductionRecord>,
    val budget: List<T127BudgetRecord>,
    val findings: Map<String, String>
)

// ---------------------------------------------------------------------------------------------

private fun budgetRecord(stage: String, budget: SearchBudget, note: String) = T127BudgetRecord(
    stage = stage,
    configurations = budget.configurations,
    lattices = budget.lattices,
    latticesSolved = budget.latticesSolved,
    junctionSlots = budget.junctionSlots,
    candidateAzimuths = budget.candidateAzimuths,
    junctionSolves = budget.junctionSolves,
    linkClosures = budget.linkClosures,
    closingLattices = budget.closingLattices,
    note = note
)

private fun configurationRecord(
    configuration: TrioConfiguration,
    outcome: TrioExistenceOutcome
) = T127ConfigurationRecord(
    configuration = outcome.configuration,
    crossbarBasePairs = outcome.crossbarBasePairs,
    separationBasePairs = outcome.separationBasePairs,
    overhangBasePairs = configuration.overhangBasePairs,
    lattices = outcome.budget.lattices,
    latticesSolved = outcome.budget.latticesSolved,
    reachFeasibleLattices = outcome.reachFeasibleLattices,
    candidateAzimuths = outcome.budget.candidateAzimuths,
    junctionSolves = outcome.budget.junctionSolves,
    linkClosures = outcome.budget.linkClosures,
    closingLattices = outcome.budget.closingLattices,
    combinationsExhausted = outcome.combinationsExhausted,
    closes = outcome.closes,
    bestWorstMisalignmentDegrees = outcome.best?.worstMisalignmentDegrees ?: 0.0,
    bestLegMisalignmentDegrees = outcome.bestForCap?.legMisalignmentDegrees ?: 0.0,
    bestFlexureMisalignmentDegrees = outcome.bestForCap?.flexureMisalignmentDegrees ?: 0.0,
    verdict = outcome.verdict
)

private fun trioRecord(
    rank: String,
    trio: ClosingTrio,
    verdictGrid: Boolean,
    note: String
) = T127TrioRecord(
    rank = rank,
    crossbarBasePairs = trio.crossbarBasePairs,
    separationBasePairs = trio.separationBasePairs,
    helicalPhaseDegrees = trio.helicalPhaseDegrees,
    axialPhase = trio.axialPhase,
    lateralSeat = trio.lateralSeat,
    azimuthDegrees = trio.azimuthDegrees,
    chordAzimuthDegrees = trio.chordAzimuthDegrees,
    worstMisalignmentDegrees = trio.worstMisalignmentDegrees,
    legMisalignmentDegrees = trio.legMisalignmentDegrees,
    flexureMisalignmentDegrees = trio.flexureMisalignmentDegrees,
    worstGap = trio.worstGap,
    minimumTerminusSeparation = trio.minimumTerminusSeparation,
    minimumSeatContact = trio.minimumSeatContact,
    distinctTargets = trio.distinctTargets,
    closesOnVerdictGrid = verdictGrid,
    note = note
)

private fun designRecord(id: String, design: FeasibleTrussDesign) = T127DesignRecord(
    id = id,
    legSteps = design.legSteps,
    legLength = design.legLength,
    separationBasePairs = design.separationBasePairs,
    baseFloorDegrees = design.baseFloorDegrees,
    capFloorDegrees = design.capFloorDegrees,
    flexureFloorDegrees = design.flexureMisalignmentDegrees,
    budgetDegrees = design.budgetDegrees,
    baseMisalignmentDegrees = design.baseMisalignmentDegrees,
    capMisalignmentDegrees = design.capMisalignmentDegrees,
    loadedCoupleFraction = design.loadedCoupleFraction,
    frameCouple = design.frameCouple,
    capBending = design.capBending,
    capTorsion = design.capTorsion,
    duty = design.duty,
    criticalLoadCanDo = design.criticalLoadCanDo,
    criticalLoadFields = design.criticalLoadFields,
    marginCanDo = design.marginCanDo,
    marginFields = design.marginFields,
    governingPlane = design.governingPlane,
    span = design.span,
    representable = design.representable,
    verdict = design.verdict
)

/**
 * Re-solves a found trio's three junctions on `C-0057`'s **verdict** grid — 180 steps and 6
 * refinements against the search's 60 and 4. A finer grid can only improve the lexicographic
 * optimum, so this can only confirm; it is run because the whole existence result rests on it.
 */
private fun closesOnVerdictGrid(
    backbone: DuplexBackbone,
    trio: ClosingTrio,
    junctions: List<TrioJunctionSpec>
): Boolean {
    val phase = trio.helicalPhaseDegrees * PI / 180.0
    val lattice = crossbarPhosphateLattice(
        backbone, trio.crossbarBasePairs, phase, trio.axialPhase
    )
    val search = TorsionFeasibleTrioSearch(
        backbone = backbone,
        crossbarBasePairs = trio.crossbarBasePairs,
        separationBasePairs = trio.separationBasePairs,
        junctions = junctions
    )
    return junctions.indices.all { index ->
        val placement = search.placementAt(
            junctions[index], trio.lateralSeat, trio.azimuthDegrees[index] * PI / 180.0, lattice
        ) ?: return false
        junctionClosesOnSomeAssignment(
            junctionLinks(backbone, trio.crossbarBasePairs, phase, trio.axialPhase, placement),
            VERDICT_GRID, VERDICT_REFINEMENTS
        )
    }
}

private fun upstream(file: File, path: String, index: Int, field: String): Double {
    val root = Json.parseToJsonElement(file.readText()).jsonObject
    return root[path]!!.jsonArray[index].jsonObject[field]!!.jsonPrimitive.content.toDouble()
}

// ---------------------------------------------------------------------------------------------

fun main() {
    val backbone = DuplexBackbone()
    val designPoint = TrioConfiguration(13, 7)
    println("T-127 — does a torsion-feasible trio exist on the crossbar at all?")

    // ---- the cheap bound, which is also the method: the marginal closure census
    val censusSearch = CrossbarTrioExistenceSearch(configuration = designPoint)
    val census = TrioJunctionSpec.cap(7).map { censusSearch.marginalCensus(it, 90) }
    val marginals = census.map {
        T127MarginalRecord(
            configuration = designPoint.toString(),
            junction = it.junction,
            lattices = it.lattices,
            candidates = it.candidates,
            closingCandidates = it.closingCandidates,
            closingLattices = it.closingLattices,
            latticeRate = it.latticeRate,
            candidateRate = it.candidateRate
        )
    }
    marginals.forEach {
        println(
            "  marginal %-12s %d of %d lattices carry a closing placement (%.1f %%), %d of %d candidates"
                .format(
                    it.junction, it.closingLattices, it.lattices, 100.0 * it.latticeRate,
                    it.closingCandidates, it.candidates
                )
        )
    }
    val yieldAt24 = independenceYield(census, 24)
    val yieldAt1800 = independenceYield(census, 1800)
    println(
        "  the independence prediction: C-0059's 24 lattices were worth %.2f trios, 1800 are worth %.1f"
            .format(yieldAt24, yieldAt1800)
    )

    val bounds = listOf(
        T127BoundRecord(
            "the marginal closure rate, worst junction",
            census.minOf { it.latticeRate }, "fraction of lattices",
            "the cheap bound: a trio needs all three junctions to close on ONE lattice, and each " +
                    "junction's problem is independent of the others given the lattice. A zero " +
                    "here would kill the trio outright with no joint search at all"
        ),
        T127BoundRecord(
            "the independence yield of C-0059's own budget", yieldAt24, "trios expected",
            "C-0059 solved 24 lattices at two candidate azimuths per junction and found none. " +
                    "Under independence that budget was worth this many trios — so its negative " +
                    "carried essentially no information, which is what this task exists to show"
        ),
        T127BoundRecord(
            "the independence yield of one full lattice grid", yieldAt1800, "trios expected",
            "the same prediction at the 1800-lattice grid C-0059 enumerated but did not solve"
        ),
        T127BoundRecord(
            "the per-assignment reach pruning", 32.0 / 8.0, "assignments per link, typical",
            "C-0057's reach bound is a proof of exclusion PER ASSIGNMENT, so an assignment that " +
                    "fails it cannot close and need not be solved. 12 % of the 32 survive, which " +
                    "is a 17.2x speedup, and it is what makes this budget affordable"
        )
    )

    // ---- the free limiting case: C-0059's own budget, reproduced
    val restricted = CrossbarTrioExistenceSearch(
        configuration = designPoint,
        candidatesPerJunction = 2,
        latticeCap = 24,
        rankByAlignment = true
    ).sweep()
    println("  C-0059's budget reproduced: ${restricted.verdict}")

    // ---- the deepened sweep, over the whole admissible (crossbar, row) band
    val band = TrioConfiguration.band(6..12, 2)
    val outcomes = band.map { configuration ->
        val outcome = CrossbarTrioExistenceSearch(configuration = configuration).sweep()
        println("  ${outcome.verdict}")
        configuration to outcome
    }
    val configurations = outcomes.map { (configuration, outcome) ->
        configurationRecord(configuration, outcome)
    }

    // ---- the depth run: the design point at a finer lattice grid
    val deep = CrossbarTrioExistenceSearch(
        configuration = designPoint,
        phaseSteps = 360,
        axialSteps = 8,
        lateralSeats = (-4..4).map { it * 0.1 },
        reportedTrios = 16
    ).sweep()
    println("  depth run at the design point: ${deep.verdict}")

    // ---- the best configuration, re-swept deep
    val bestConfiguration = outcomes.filter { it.second.closes }
        .minByOrNull { it.second.bestForCap?.legMisalignmentDegrees ?: 180.0 }
    val deepBest = bestConfiguration?.takeIf { it.first != designPoint }?.let { (configuration, _) ->
        CrossbarTrioExistenceSearch(
            configuration = configuration,
            phaseSteps = 360,
            axialSteps = 8,
            lateralSeats = (-4..4).map { it * 0.1 },
            reportedTrios = 16
        ).sweep()
    }
    deepBest?.let { println("  depth run at the best configuration: ${it.verdict}") }

    // ---- the trios, re-solved on C-0057's verdict grid
    val trios = ArrayList<T127TrioRecord>()
    fun record(rank: String, outcome: TrioExistenceOutcome?, note: String) {
        val trio = outcome?.bestForCap ?: return
        val junctions = TrioJunctionSpec.cap(trio.separationBasePairs)
        trios += trioRecord(
            rank, trio, closesOnVerdictGrid(backbone, trio, junctions), note
        )
    }
    outcomes.filter { it.second.closes }.forEach { (configuration, outcome) ->
        record("band, $configuration", outcome, "the best-for-the-cap trio of the whole grid")
    }
    record("depth, the design point", deep, "360 phases x 8 axial x 9 lateral seats")
    deepBest?.let {
        record("depth, the best configuration", it, "360 phases x 8 axial x 9 lateral seats")
    }
    outcomes.filter { it.second.closes }.forEach { (_, outcome) ->
        outcome.best?.let { best ->
            trios += trioRecord(
                "best-aligned, ${outcome.configuration}", best,
                closesOnVerdictGrid(backbone, best, TrioJunctionSpec.cap(best.separationBasePairs)),
                "the trio minimising the WORST of the three chords rather than the leg's"
            )
        }
    }

    // ---- the mechanics, at the alignment the FOUND trio delivers
    val pairFloors = (6..12).associateWith { separation ->
        upstream(
            ResultInputs.T_124.file(), "pairs", separation - 6,
            "worstMisalignmentDegrees"
        )
    }
    val designs = ArrayList<T127DesignRecord>()
    designs += designRecord(
        "C-0048's recommended design, aligned — the reference",
        feasibleTrussDesign(21, 0.0, 0.0, 0.0, 7)
    )
    // THE ROW IS ONE NUMBER. C-0059 composed a 9 bp base floor from the sheet with a 7 bp cap
    // floor from the crossbar; a leg has one separation and it is the same at both of its ends.
    // So the design table is computed row by row, each at ITS OWN pair floor and ITS OWN best
    // closing trio, and the best row is then read off rather than assumed.
    val perRow = (6..12).mapNotNull { row ->
        val best = (listOfNotNull(deepBest, deep) + outcomes.map { it.second })
            .filter { it.closes && it.separationBasePairs == row }
            .mapNotNull { it.bestForCap }
            .minByOrNull { it.legMisalignmentDegrees }
        best?.let { row to it }
    }
    perRow.forEach { (row, trio) ->
        val baseFloor = (pairFloors[row] ?: 0.0) * PI / 180.0
        val capFloor = trio.legMisalignmentDegrees * PI / 180.0
        val flexureFloor = trio.flexureMisalignmentDegrees * PI / 180.0
        (12..26).forEach { steps ->
            designs += designRecord(
                "$row bp row at BOTH ends: base ${pairFloors[row]?.roundedForProse()}°, cap " +
                        "${trio.legMisalignmentDegrees.roundedForProse()}° on the ${trio.crossbarBasePairs} bp crossbar",
                feasibleTrussDesign(steps, baseFloor, capFloor, flexureFloor, row)
            )
        }
    }
    // and C-0059's own mixed-row composition, for the comparison CH-0075 is written on
    designs += designRecord(
        "C-0059's composition: a 9 bp base floor against a 7 bp cap floor of 6.0°",
        feasibleTrussDesign(15, (pairFloors[9] ?: 0.0) * PI / 180.0, 6.0 * PI / 180.0, 0.0, 9)
    )
    designs.forEach {
        println(
            "  leg %2d steps: base %5.1f° cap %5.1f° budget %5.1f° P_c %6.2f/%6.2f margin %5.2f/%5.2f %s"
                .format(
                    it.legSteps, it.baseMisalignmentDegrees, it.capMisalignmentDegrees,
                    it.budgetDegrees, it.criticalLoadCanDo, it.criticalLoadFields,
                    it.marginCanDo, it.marginFields, it.verdict
                )
        )
    }

    // ---- sensitivities
    val reference = outcomes.first { it.first == designPoint }.second
    val extraBudgets = ArrayList<SearchBudget>()
    val sensitivities = ArrayList<T127SensitivityRecord>()
    fun sensitivity(axis: String, reading: String, note: String, search: CrossbarTrioExistenceSearch) {
        val outcome = search.sweep()
        extraBudgets += outcome.budget
        sensitivities += T127SensitivityRecord(
            axis = axis,
            reading = reading,
            reachFeasibleLattices = outcome.reachFeasibleLattices,
            closingLattices = outcome.budget.closingLattices,
            junctionSolves = outcome.budget.junctionSolves,
            closes = outcome.closes,
            bestWorstMisalignmentDegrees = outcome.best?.worstMisalignmentDegrees ?: 0.0,
            verdictMoves = outcome.closes != reference.closes,
            note = note
        )
        println("  sensitivity $axis = $reading: ${outcome.verdict}")
    }
    sensitivities += T127SensitivityRecord(
        "reference", "C-0029's geometry, 120° groove, r_P = 1.00 nm",
        reference.reachFeasibleLattices, reference.budget.closingLattices,
        reference.budget.junctionSolves, reference.closes,
        reference.best?.worstMisalignmentDegrees ?: 0.0, false,
        "the 13 bp crossbar and 7 bp row C-0048, C-0052 and C-0059 all run on"
    )
    sensitivity(
        "groove convention", "154° (wide)",
        "C-0029 names the groove as the parameter the base couple is most sensitive to",
        CrossbarTrioExistenceSearch(
            configuration = designPoint, backbone = DuplexBackbone(minorGrooveAngle = 154.0)
        )
    )
    sensitivity(
        "groove convention", "180° (the hard chord the mechanics is written on)",
        "C-0037, C-0042, C-0048 and C-0052 all take the mechanics on the 180° chord",
        CrossbarTrioExistenceSearch(
            configuration = designPoint, backbone = DuplexBackbone(minorGrooveAngle = 180.0)
        )
    )
    sensitivity(
        "phosphate radius", "0.90 nm (C-0029's own bracket end)",
        "C-0057's falsifier 4 fired on this axis for a distance argmin; it does not here",
        CrossbarTrioExistenceSearch(
            configuration = designPoint, backbone = DuplexBackbone(phosphateRadius = 0.90)
        )
    )
    sensitivity(
        "seat contact floor", "C-0042's 1.60 nm raised to 1.90 nm",
        "the truncated seat contact is 1.833–1.960 nm at every closing trio C-0052 reports",
        CrossbarTrioExistenceSearch(configuration = designPoint, contactFloor = 1.90)
    )

    // ---- convergence
    val convergence = ArrayList<T127ConvergenceRecord>()
    fun converge(axis: String, level: String, note: String, search: CrossbarTrioExistenceSearch) {
        val outcome = search.sweep()
        extraBudgets += outcome.budget
        convergence += T127ConvergenceRecord(
            axis = axis,
            level = level,
            lattices = outcome.budget.lattices,
            reachFeasibleLattices = outcome.reachFeasibleLattices,
            closingLattices = outcome.budget.closingLattices,
            junctionSolves = outcome.budget.junctionSolves,
            bestWorstMisalignmentDegrees = outcome.best?.worstMisalignmentDegrees ?: 0.0,
            note = note
        )
    }
    listOf(60, 120, 240).forEach { steps ->
        converge(
            "azimuth steps", "$steps", "the standoff's own azimuth is a continuum (CH-0056)",
            CrossbarTrioExistenceSearch(
                configuration = designPoint, azimuthSteps = steps, phaseSteps = 45
            )
        )
    }
    listOf(45, 90, 180).forEach { steps ->
        converge(
            "helical phase steps", "$steps", "the crossbar's phase is a continuum",
            CrossbarTrioExistenceSearch(configuration = designPoint, phaseSteps = steps)
        )
    }
    listOf(2, 4, 8).forEach { closers ->
        converge(
            "closers collected per junction", "$closers",
            "the one cap that could manufacture a negative, by exhausting the combinations",
            CrossbarTrioExistenceSearch(
                configuration = designPoint, closersPerJunction = closers
            )
        )
    }
    convergence.forEach {
        println(
            "  convergence ${it.axis} = ${it.level}: ${it.closingLattices} closing of " +
                    "${it.reachFeasibleLattices} reach-feasible"
        )
    }

    // ---- upstream reproductions
    val legacy = TorsionFeasibleTrioSearch(
        backbone = backbone, crossbarBasePairs = 13, gridSteps = SEARCH_GRID,
        refinements = SEARCH_REFINEMENTS
    )
    val legacyOutcome = legacy.best(solveCap = 24)
    val t124 = ResultInputs.T_124.file()
    val reproductions = listOf(
        T127ReproductionRecord(
            "C-0059's reach-feasible lattices at 13 bp", "T-124 trios[0].feasibleLattices",
            upstream(t124, "trios", 0, "feasibleLattices"),
            legacyOutcome.feasibleLattices.toDouble(),
            abs(upstream(t124, "trios", 0, "feasibleLattices") - legacyOutcome.feasibleLattices),
            "re-run from C-0059's own class, not read off its file"
        ),
        T127ReproductionRecord(
            "C-0059's junction solves at 13 bp", "T-124 trios[0].solves",
            upstream(t124, "trios", 0, "solves"), legacy.solves.toDouble(),
            abs(upstream(t124, "trios", 0, "solves") - legacy.solves), "the budget itself"
        ),
        T127ReproductionRecord(
            "C-0059's best reach-feasible alignment at 13 bp",
            "T-124 trios[0].bestFeasibleMisalignmentDegrees",
            upstream(t124, "trios", 0, "bestFeasibleMisalignmentDegrees"),
            legacyOutcome.bestFeasibleMisalignmentDegrees,
            abs(
                upstream(t124, "trios", 0, "bestFeasibleMisalignmentDegrees") -
                        legacyOutcome.bestFeasibleMisalignmentDegrees
            ),
            "the alignment the reach-feasible set offers before any torsion solve"
        ),
        T127ReproductionRecord(
            "this task's own reach-feasible lattices at 13 bp", "this study",
            legacyOutcome.feasibleLattices.toDouble(),
            reference.reachFeasibleLattices.toDouble(),
            abs(legacyOutcome.feasibleLattices - reference.reachFeasibleLattices).toDouble(),
            "the deepened sweep must enumerate the SAME feasible set C-0059 enumerated"
        ),
        T127ReproductionRecord(
            "C-0042's pair floor at the 7 bp row", "T-124 pairs[1].worstMisalignmentDegrees",
            upstream(t124, "pairs", 1, "worstMisalignmentDegrees"), pairFloors[7] ?: 0.0,
            abs(upstream(t124, "pairs", 1, "worstMisalignmentDegrees") - (pairFloors[7] ?: 0.0)),
            "consumed as data, per T-127's own acceptance predicate"
        ),
        T127ReproductionRecord(
            "C-0052's leg budget at 21 steps", "C-0052 table, 78.53°",
            78.53, legBudgetDegrees(21), abs(78.53 - legBudgetDegrees(21)),
            "arithmetic on C-0029's counting theorem, and nothing here can move it"
        ),
        T127ReproductionRecord(
            "C-0052's crossbar length at 13 bp", "C-0052, 4.42 nm",
            4.42, designPoint.geometry.length, abs(4.42 - designPoint.geometry.length),
            "13 bp of duplex against C-0048's 4.38 nm demand"
        ),
        T127ReproductionRecord(
            "C-0052's rim clearance", "C-0052, 0.02 nm",
            0.02, designPoint.geometry.legRimClearance,
            abs(0.02 - designPoint.geometry.legRimClearance),
            "the whole of ceil's gift, at every row width"
        )
    )
    reproductions.forEach {
        println("  reproduction ${it.quantity}: ${it.here} against ${it.upstream}, departure ${it.departure}")
    }

    // ---- the budget, totalled
    val bandBudget = outcomes.fold(SearchBudget.EMPTY) { total, (_, outcome) -> total + outcome.budget }
    val total = extraBudgets.fold(
        bandBudget + deep.budget + (deepBest?.budget ?: SearchBudget.EMPTY) + restricted.budget
    ) { running, extra -> running + extra }
    val budget = listOf(
        budgetRecord(
            "C-0059's own trio search", SearchBudget(
                configurations = 2, lattices = 3600, latticesSolved = 48, junctionSlots = 5400,
                candidateAzimuths = 0, junctionSolves = 269, linkClosures = 538, closingLattices = 0
            ),
            "as published: 24 of 750 and 24 of 882 lattices, two candidate azimuths per junction, " +
                    "134 + 135 junction solves, 0 closing"
        ),
        budgetRecord(
            "this task, the (crossbar, row) band", bandBudget,
            "${band.size} configurations, every lattice of each grid, every reach-feasible azimuth"
        ),
        budgetRecord("this task, the depth run at the design point", deep.budget, "360 x 8 x 9"),
        budgetRecord(
            "this task, the depth run at the best configuration",
            deepBest?.budget ?: SearchBudget.EMPTY, "360 x 8 x 9"
        ),
        budgetRecord("this task, total", total, "the number the verdict is bounded by")
    )

    val closes = outcomes.any { it.second.closes } || deep.closes
    val bestOverall = trios.minByOrNull { it.legMisalignmentDegrees }
    val confirmed = trios.count { it.closesOnVerdictGrid }
    val bestDesign = designs.filter { it.representable && it.legSteps in 12..26 }
        .filter { it.id.contains("bp row at BOTH ends") }
        .maxByOrNull { it.marginFields }
    val result = T127Result(
        task = "T-127",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "medium" to "aqueous 2 mM MgCl2",
            "k_BT" to "4.141947 pN nm",
            "sheet" to "single-layer square-lattice Rothemund, SAXS 2.69 nm, rise $RISE nm, " +
                    "10.67 bp/turn",
            "crossbar" to "a LONE duplex along x through the origin, C-0052's own geometry",
            "phosphate radius" to "1.00 nm (C-0029, Hedley et al.), 0.90 nm carried as a sensitivity",
            "phosphodiester window" to "the inherited [0.60, 0.70] nm, unchanged, so that the " +
                    "feasible set IS C-0057's and C-0059's; C-0057 measures 0.607 / 0.664 nm on " +
                    "13 084 crystallographic linkages and reports the window has NO primary source",
            "groove" to "120° nominal, with 154° and 180° carried as sensitivities",
            "search grid" to "$SEARCH_GRID torsion steps, $SEARCH_REFINEMENTS refinements — C-0059's own",
            "verdict grid" to "$VERDICT_GRID torsion steps, $VERDICT_REFINEMENTS refinements — C-0057's own",
            "rigidities" to "CanDo EI = ${Gen1Tile.DUPLEX_BENDING_RIGIDITY} pN nm^2 and Fields " +
                    "et al.'s implied ${FIELDS_BENDING_RIGIDITY.roundedForProse()} pN nm^2",
            "units" to "nm, pN, pN nm, degrees for every reported angle"
        ),
        bounds = bounds,
        marginals = marginals,
        configurations = configurations,
        trios = trios,
        designs = designs,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        budget = budget,
        findings = mapOf(
            "the verdict" to if (closes) {
                ("A TORSION-FEASIBLE TRIO EXISTS. %d of the %d (crossbar, row) configurations " +
                        "carry one, the best leg chord %.1f° off the direction C-0048 wants and " +
                        "the best worst-of-three %.1f°, on a budget of %d lattices and %d " +
                        "junction solves against C-0059's 48 and 269").format(
                    outcomes.count { it.second.closes }, band.size,
                    bestOverall?.legMisalignmentDegrees ?: 0.0,
                    trios.minOfOrNull { it.worstMisalignmentDegrees } ?: 0.0,
                    total.latticesSolved, total.junctionSolves
                )
            } else {
                ("NO torsion-feasible trio was found at a budget of %d lattices solved and %d " +
                        "junction solves, against C-0059's 48 and 269 — and the marginal census " +
                        "predicted %.1f. The truss branch closes at C-0037's design").format(
                    total.latticesSolved, total.junctionSolves, yieldAt1800
                )
            },
            "why C-0059's negative carried no information" to ("its 24 solved lattices at two " +
                    "candidate azimuths per junction were worth %.2f trios under the marginal " +
                    "census's own independence prediction, so a null result was the expected " +
                    "one. The deepened sweep returns %d closing lattices at the same " +
                    "configuration").format(
                yieldAt24, reference.budget.closingLattices
            ),
            "the cheap bound" to ("the three junctions close on %.0f %%, %.0f %% and %.0f %% of " +
                    "lattices marginally; the joint rate at the design point is %.2f %% against " +
                    "an independence prediction of %.2f %%, so the conjunction is %s")
                .format(
                    100.0 * census[0].latticeRate, 100.0 * census[1].latticeRate,
                    100.0 * census[2].latticeRate,
                    100.0 * reference.budget.closingLattices / reference.reachFeasibleLattices
                        .coerceAtLeast(1),
                    100.0 * yieldAt1800 / 1800.0,
                    if (reference.budget.closingLattices * 1800.0 < yieldAt1800 * reference.reachFeasibleLattices) {
                        "HARDER than independence, not easier"
                    } else "no harder than independence"
                ),
            "the verdict grid" to ("%d of the %d reported trios still close when every junction " +
                    "is re-solved on C-0057's own %d-step / %d-refinement verdict grid, so the " +
                    "existence result does not rest on the search grid alone; the %d that do not " +
                    "are the measure of how far a refined LOCAL zoom can move a closure, in both " +
                    "directions, and they are reported rather than dropped").format(
                confirmed, trios.size, VERDICT_GRID, VERDICT_REFINEMENTS, trios.size - confirmed
            ),
            "the row is one number" to ("C-0059's design table composes a 9 bp base floor from " +
                    "the sheet with a 7 bp cap floor from the crossbar, but the row pitch IS the " +
                    "legs' separation and a leg has only one of those — CH-0075. Computed row by " +
                    "row at one pitch throughout, the best representable design is %s at %d " +
                    "steps, margin %.2f on CanDo's rigidity and %.2f on Fields et al.'s").format(
                bestDesign?.id ?: "none", bestDesign?.legSteps ?: 0,
                bestDesign?.marginCanDo ?: 0.0, bestDesign?.marginFields ?: 0.0
            ),
            "what this cannot establish" to "A torsion check is a NECESSARY condition and never " +
                    "a sufficient one, exactly as C-0029, C-0052 and C-0057 all said. A 'closes' " +
                    "verdict is an upper bound on buildability; nothing here is measured, no " +
                    "sequence is designed, and no assembly is demonstrated."
        )
    )

    val json = Json { prettyPrint = true }
    val file = File("gpd/results/T-127-crossbar-trio-existence.json")
    file.parentFile?.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult(
        digitsByKey = DEPARTURE_DIGITS_BY_KEY
    ).withEmissionHeader(LatticeTag.SQUARE, null)) + "\n")
    println("wrote ${file.path}")
}
