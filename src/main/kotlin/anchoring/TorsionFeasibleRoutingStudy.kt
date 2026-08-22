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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs

/**
 * Task `T-124` / leaf `A8.2` — **the truss branch's junctions re-derived on `C-0057`'s
 * torsion-feasible set**, rather than on the phosphate distance all three claims optimised.
 *
 * ```shell
 * tools/study.sh anchoring.TorsionFeasibleRoutingStudyKt
 * ```
 *
 * Emits `gpd/results/T-124-torsion-feasible-routing.json`, deterministically: fixed grids, strict
 * comparisons, no timestamp, and the whole tree rounded at the **serialisation boundary**.
 */

private val RISE = Gen1Tile.RISE_PER_BASE_PAIR

/** The grid the *searches* run their closures on — `C-0057`'s own census grid. */
private const val SEARCH_GRID = 60
private const val SEARCH_REFINEMENTS = 4

/** The grid a *verdict* is read on — `C-0057`'s own scale grid, and `bestLinkClosure`'s default. */
private const val VERDICT_GRID = 180
private const val VERDICT_REFINEMENTS = 6

// ---------------------------------------------------------------------------------------------

@Serializable
data class T124BoundRecord(
    val quantity: String,
    val value: Double,
    val units: String,
    val note: String
)

@Serializable
data class T124CensusRecord(
    val topology: String,
    val placements: Int,
    val covalent: Int,
    val reachFeasible: Int,
    val inAlignmentBand: Int,
    val bestFeasibleMisalignmentDegrees: Double,
    val upstreamCovalent: Int,
    val upstreamReachFeasible: Int,
    val departure: Int,
    val note: String
)

/** How the reach-feasible set is distributed over the standoff's own azimuth — cheap bound 2. */
@Serializable
data class T124AzimuthRecord(
    val topology: String,
    val azimuthDegrees: Double,
    val chordAzimuthDegrees: Double,
    val misalignmentDegrees: Double,
    val feasiblePlacements: Int
)

@Serializable
data class T124AlignedRecord(
    val scale: String,
    val topology: String,
    val placements: Int,
    val covalent: Int,
    val reachFeasible: Int,
    val inAlignmentBand: Int,
    val solved: Int,
    val closing: Int,
    val bestFeasibleMisalignmentDegrees: Double,
    val deepestSolvedMisalignmentDegrees: Double,
    val closes: Boolean,
    val bestMisalignmentDegrees: Double,
    val bestChordAzimuthDegrees: Double,
    val bestAzimuthDegrees: Double,
    val bestCentreX: Double,
    val bestCentreY: Double,
    val bestWorstGap: Double,
    val bestWorstCovalentZ: Double,
    val bestMinimumOccupancy: Double,
    val bestLoadedCoupleFraction: Double,
    val verdict: String
)

@Serializable
data class T124PairRecord(
    val separationBasePairs: Int,
    val axialPositions: Int,
    val closingPositions: Int,
    val solves: Int,
    val closes: Boolean,
    val worstMisalignmentDegrees: Double,
    val firstChordDegrees: Double,
    val secondChordDegrees: Double,
    val worstGap: Double,
    val worstCovalentZ: Double,
    val minimumOccupancy: Double,
    val worstLoadedCoupleFraction: Double,
    val firstCentreX: Double,
    val secondCentreX: Double,
    val lateralSeat: Double,
    val verdict: String
)

@Serializable
data class T124TrioRecord(
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val lattices: Int,
    val feasibleLattices: Int,
    val solvedLattices: Int,
    val bestFeasibleMisalignmentDegrees: Double,
    val solves: Int,
    val closes: Boolean,
    val worstMisalignmentDegrees: Double,
    val legMisalignmentDegrees: Double,
    val flexureMisalignmentDegrees: Double,
    val worstGap: Double,
    val worstCovalentZ: Double,
    val minimumOccupancy: Double,
    val helicalPhaseDegrees: Double,
    val axialPhase: Double,
    val lateralSeat: Double,
    val verdict: String
)

@Serializable
data class T124DesignRecord(
    val label: String,
    val legSteps: Int,
    val legLength: Double,
    val separationBasePairs: Int,
    val baseFloorDegrees: Double,
    val capFloorDegrees: Double,
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
    val tangent: Double,
    val representable: Boolean,
    val verdict: String
)

@Serializable
data class T124SensitivityRecord(
    val axis: String,
    val label: String,
    val covalent: Int,
    val reachFeasible: Int,
    val inAlignmentBand: Int,
    val bestFeasibleMisalignmentDegrees: Double,
    val closes: Boolean,
    val bestMisalignmentDegrees: Double,
    val verdictMoves: Boolean
)

@Serializable
data class T124ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Int,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T124ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class T124Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val bounds: List<T124BoundRecord>,
    val census: List<T124CensusRecord>,
    val azimuths: List<T124AzimuthRecord>,
    val aligned: List<T124AlignedRecord>,
    val pairs: List<T124PairRecord>,
    val trios: List<T124TrioRecord>,
    val designs: List<T124DesignRecord>,
    val sensitivities: List<T124SensitivityRecord>,
    val convergence: List<T124ConvergenceRecord>,
    val reproductions: List<T124ReproductionRecord>,
    val findings: Map<String, String>
)

// ---------------------------------------------------------------------------------------------

private fun degrees(radians: Double): Double = radians * 180.0 / PI

private fun censusRecord(
    enumeration: SingleJunctionEnumeration,
    allowance: Double,
    upstreamCovalent: Int,
    upstreamFeasible: Int
): T124CensusRecord = T124CensusRecord(
    topology = enumeration.topology.name,
    placements = enumeration.placements,
    covalent = enumeration.covalent,
    reachFeasible = enumeration.feasible.size,
    inAlignmentBand = enumeration.withinBand(allowance).size,
    bestFeasibleMisalignmentDegrees = degrees(enumeration.bestFeasibleMisalignment),
    upstreamCovalent = upstreamCovalent,
    upstreamReachFeasible = upstreamFeasible,
    departure = abs(enumeration.covalent - upstreamCovalent) +
            abs(enumeration.feasible.size - upstreamFeasible),
    note = "C-0057's census re-derived from its own libraries, not read off its result file"
)

private fun alignedRecord(outcome: AlignedClosureOutcome): T124AlignedRecord = T124AlignedRecord(
    scale = outcome.scale,
    topology = outcome.topology,
    placements = outcome.placements,
    covalent = outcome.covalent,
    reachFeasible = outcome.reachFeasible,
    inAlignmentBand = outcome.inAlignmentBand,
    solved = outcome.solved,
    closing = outcome.closing,
    bestFeasibleMisalignmentDegrees = outcome.bestFeasibleMisalignmentDegrees,
    deepestSolvedMisalignmentDegrees = outcome.deepestSolvedMisalignmentDegrees,
    closes = outcome.closes,
    bestMisalignmentDegrees = outcome.bestMisalignmentDegrees,
    bestChordAzimuthDegrees = outcome.bestChordAzimuthDegrees,
    bestAzimuthDegrees = outcome.bestAzimuthDegrees,
    bestCentreX = outcome.bestCentreX,
    bestCentreY = outcome.bestCentreY,
    bestWorstGap = outcome.bestWorstGap,
    bestWorstCovalentZ = outcome.bestWorstCovalentZ,
    bestMinimumOccupancy = outcome.bestMinimumOccupancy,
    bestLoadedCoupleFraction = outcome.bestLoadedCoupleFraction,
    verdict = outcome.verdict
)

private fun designRecord(label: String, design: FeasibleTrussDesign): T124DesignRecord =
    T124DesignRecord(
        label = label,
        legSteps = design.legSteps,
        legLength = design.legLength,
        separationBasePairs = design.separationBasePairs,
        baseFloorDegrees = design.baseFloorDegrees,
        capFloorDegrees = design.capFloorDegrees,
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
        tangent = design.tangent,
        representable = design.representable,
        verdict = design.verdict
    )

fun main() {
    val backbone = DuplexBackbone()
    val allowance = alignmentAllowance(backbone)
    val set = SingleJunctionFeasibleSet(backbone)

    // ---- the two cheap bounds, which run before any solve
    println("T-124 — the truss branch's junctions on the torsion-feasible set")
    println("cheap bound 1: the free-phosphate reach interval")
    println("cheap bound 2: the alignment band, ±%.2f°".format(degrees(allowance)))

    val independent = set.enumerate(RoutingTopology.INDEPENDENT_STAPLES)
    val excursion = set.enumerate(RoutingTopology.SCAFFOLD_EXCURSION)
    val census = listOf(
        censusRecord(independent, allowance, 3546, 1855),
        censusRecord(excursion, allowance, 280, 137)
    )
    census.forEach {
        println(
            "  %-20s covalent %5d (C-0057: %5d)  reach-feasible %5d (%5d)  in band %4d  best %5.1f°"
                .format(
                    it.topology, it.covalent, it.upstreamCovalent, it.reachFeasible,
                    it.upstreamReachFeasible, it.inAlignmentBand,
                    it.bestFeasibleMisalignmentDegrees
                )
        )
    }

    // ---- the azimuth distribution of the reach-feasible set: what bound 2 actually says
    val azimuths = listOf(independent, excursion).flatMap { enumeration ->
        enumeration.feasible.groupBy {
            Math.round(degrees(it.closure.azimuth)).toInt()
        }.toSortedMap().map { (azimuth, placements) ->
            T124AzimuthRecord(
                topology = enumeration.topology.name,
                azimuthDegrees = azimuth.toDouble(),
                chordAzimuthDegrees = degrees(
                    chordAzimuthOfStandoff(azimuth * PI / 180.0, backbone)
                ),
                misalignmentDegrees = degrees(placements.first().misalignment),
                feasiblePlacements = placements.size
            )
        }
    }
    println("  the reach-feasible set occupies ${azimuths.count { it.topology == "INDEPENDENT_STAPLES" }}" +
            " of ${set.azimuthSteps} azimuth values")

    // ---- scale 1: C-0029's single junction, re-optimised on alignment over the feasible set
    val alignedIndependent = bestAlignedClosure(
        independent, backbone, allowance = allowance, solveCap = 120,
        gridSteps = SEARCH_GRID, refinements = SEARCH_REFINEMENTS,
        scale = "C-0029 single junction"
    )
    val alignedExcursion = bestAlignedClosure(
        excursion, backbone, allowance = allowance, solveCap = 120,
        gridSteps = SEARCH_GRID, refinements = SEARCH_REFINEMENTS,
        scale = "C-0029 single junction, scaffold excursion"
    )
    val aligned = listOf(alignedIndependent, alignedExcursion).map { alignedRecord(it) }
    aligned.forEach { println("  ${it.scale}: ${it.verdict}") }

    // ---- scale 2: C-0042's pair on one sheet duplex
    val pairSearch = TorsionFeasiblePairSearch(
        backbone = backbone,
        gridSteps = SEARCH_GRID,
        refinements = SEARCH_REFINEMENTS
    )
    val pairs = (6..12).map { separation ->
        val outcome = pairSearch.bestPair(separation)
        T124PairRecord(
            separationBasePairs = outcome.separationBasePairs,
            axialPositions = outcome.axialPositions,
            closingPositions = outcome.closingPositions,
            solves = pairSearch.solves,
            closes = outcome.closes,
            worstMisalignmentDegrees = outcome.worstMisalignmentDegrees,
            firstChordDegrees = outcome.firstChordDegrees,
            secondChordDegrees = outcome.secondChordDegrees,
            worstGap = outcome.worstGap,
            worstCovalentZ = outcome.worstCovalentZ,
            minimumOccupancy = outcome.minimumOccupancy,
            worstLoadedCoupleFraction = outcome.worstLoadedCoupleFraction,
            firstCentreX = outcome.firstCentreX,
            secondCentreX = outcome.secondCentreX,
            lateralSeat = outcome.lateralSeat,
            verdict = outcome.verdict
        )
    }
    pairs.forEach { println("  pair at ${it.separationBasePairs} bp: ${it.verdict}") }

    // ---- scale 3: C-0052's trio on the lone 13 bp crossbar
    val trios = listOf(13, 15).map { crossbar ->
        val search = TorsionFeasibleTrioSearch(
            backbone = backbone,
            crossbarBasePairs = crossbar,
            gridSteps = SEARCH_GRID,
            refinements = SEARCH_REFINEMENTS
        )
        val outcome = search.best(solveCap = 24)
        T124TrioRecord(
            crossbarBasePairs = outcome.crossbarBasePairs,
            separationBasePairs = outcome.separationBasePairs,
            lattices = outcome.lattices,
            feasibleLattices = outcome.feasibleLattices,
            solvedLattices = outcome.solvedLattices,
            bestFeasibleMisalignmentDegrees = outcome.bestFeasibleMisalignmentDegrees,
            solves = search.solves,
            closes = outcome.closes,
            worstMisalignmentDegrees = outcome.worstMisalignmentDegrees,
            legMisalignmentDegrees = outcome.legMisalignmentDegrees,
            flexureMisalignmentDegrees = outcome.flexureMisalignmentDegrees,
            worstGap = outcome.worstGap,
            worstCovalentZ = outcome.worstCovalentZ,
            minimumOccupancy = outcome.minimumOccupancy,
            helicalPhaseDegrees = outcome.helicalPhaseDegrees,
            axialPhase = outcome.axialPhase,
            lateralSeat = outcome.lateralSeat,
            verdict = outcome.verdict
        )
    }
    trios.forEach { println("  trio on ${it.crossbarBasePairs} bp: ${it.verdict}") }

    // ---- the mechanics at the alignment feasibility delivers
    // **The base floor is the PAIR's, not the single junction's.** A truss stands on two legs, and
    // the pair is strictly harder: two closing placements at a fixed separation on one seat duplex.
    val bestPair = pairs.filter { it.closes }.minByOrNull { it.worstMisalignmentDegrees }
    val baseFloor = (bestPair?.worstMisalignmentDegrees
        ?: alignedIndependent.bestMisalignmentDegrees) * PI / 180.0
    val bestSeparation = bestPair?.separationBasePairs ?: 7
    val recommendedRow = pairs.firstOrNull { it.separationBasePairs == 7 }
    val closingTrio = trios.firstOrNull { it.closes }
    val capFloor = closingTrio?.let { it.legMisalignmentDegrees * PI / 180.0 }
        ?: (trios.minOfOrNull { it.bestFeasibleMisalignmentDegrees } ?: 0.0) * PI / 180.0
    val flexureFloor = closingTrio?.let { it.flexureMisalignmentDegrees * PI / 180.0 } ?: 0.0
    val designs = ArrayList<T124DesignRecord>()
    designs += designRecord(
        "C-0048's recommended design, aligned — the reference",
        feasibleTrussDesign(21, 0.0, 0.0, 0.0, 7)
    )
    designs += designRecord(
        "C-0042's own 7 bp row, on the feasible set",
        feasibleTrussDesign(
            21, (recommendedRow?.worstMisalignmentDegrees ?: 0.0) * PI / 180.0, capFloor,
            flexureFloor, 7
        )
    )
    (12..26).forEach { steps ->
        designs += designRecord(
            "on the feasible set, $bestSeparation bp row",
            feasibleTrussDesign(steps, baseFloor, capFloor, flexureFloor, bestSeparation)
        )
    }
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
    val sensitivities = ArrayList<T124SensitivityRecord>()
    fun sensitivity(axis: String, label: String, enumeration: SingleJunctionEnumeration) {
        val outcome = bestAlignedClosure(
            enumeration, backbone, allowance = allowance, solveCap = 40,
            gridSteps = SEARCH_GRID, refinements = SEARCH_REFINEMENTS
        )
        sensitivities += T124SensitivityRecord(
            axis = axis,
            label = label,
            covalent = enumeration.covalent,
            reachFeasible = enumeration.feasible.size,
            inAlignmentBand = enumeration.withinBand(allowance).size,
            bestFeasibleMisalignmentDegrees = degrees(enumeration.bestFeasibleMisalignment),
            closes = outcome.closes,
            bestMisalignmentDegrees = outcome.bestMisalignmentDegrees,
            verdictMoves = outcome.closes != alignedIndependent.closes
        )
    }
    sensitivity("reference", "C-0029's geometry, 120° groove, r_P = 1.00 nm", independent)
    listOf(
        "phosphate radius" to DuplexBackbone(phosphateRadius = 0.90),
        "phosphate radius" to DuplexBackbone(phosphateRadius = 0.8901),
        "groove convention" to DuplexBackbone(minorGrooveAngle = 154.0),
        "groove convention" to DuplexBackbone(minorGrooveAngle = 180.0)
    ).forEach { (axis, variant) ->
        val label = "r_P = %.4f nm, groove %.0f°".format(
            variant.phosphateRadius, variant.minorGrooveAngle
        )
        sensitivity(
            axis, label,
            SingleJunctionFeasibleSet(variant).enumerate(RoutingTopology.INDEPENDENT_STAPLES)
        )
    }
    // the wanted axis itself: what if the design wanted its chord ALONG the seat duplex instead
    sensitivity(
        "wanted axis", "chord along the seat duplex rather than across it",
        SingleJunctionFeasibleSet(backbone, wantedChordAzimuth = 0.0)
            .enumerate(RoutingTopology.INDEPENDENT_STAPLES)
    )
    sensitivities.forEach {
        println(
            "  %-18s %-46s band %4d best %5.1f° closes %s".format(
                it.axis, it.label, it.inAlignmentBand, it.bestMisalignmentDegrees, it.closes
            )
        )
    }

    // ---- convergence
    val convergence = ArrayList<T124ConvergenceRecord>()
    val azimuthLevels = listOf(60, 120, 240)
    val finestBand = SingleJunctionFeasibleSet(backbone, azimuthSteps = azimuthLevels.last())
        .enumerate(RoutingTopology.INDEPENDENT_STAPLES).bestFeasibleMisalignment
    azimuthLevels.forEach { steps ->
        val value = SingleJunctionFeasibleSet(backbone, azimuthSteps = steps)
            .enumerate(RoutingTopology.INDEPENDENT_STAPLES).bestFeasibleMisalignment
        convergence += T124ConvergenceRecord(
            quantity = "best feasible misalignment [deg]",
            control = "azimuth steps",
            level = steps,
            value = degrees(value),
            departureFromFinest = degrees(abs(value - finestBand))
        )
    }
    val axialLevels = listOf(32, 64, 128)
    val finestAxial = SingleJunctionFeasibleSet(backbone, axialSteps = axialLevels.last())
        .enumerate(RoutingTopology.INDEPENDENT_STAPLES).bestFeasibleMisalignment
    axialLevels.forEach { steps ->
        val value = SingleJunctionFeasibleSet(backbone, axialSteps = steps)
            .enumerate(RoutingTopology.INDEPENDENT_STAPLES).bestFeasibleMisalignment
        convergence += T124ConvergenceRecord(
            quantity = "best feasible misalignment [deg]",
            control = "axial steps",
            level = steps,
            value = degrees(value),
            departureFromFinest = degrees(abs(value - finestAxial))
        )
    }
    // the torsion grid, on the placement the search returns
    val probe = independent.feasible.first()
    val probeLinks = junctionLinks(backbone, probe.closure)
    val gridLevels = listOf(30, 60, 120, 180)
    val finestZ = torsionVerdict(
        probeLinks, gridSteps = gridLevels.last(), refinements = SEARCH_REFINEMENTS
    ).worstCovalentZ
    gridLevels.forEach { steps ->
        val value = torsionVerdict(
            probeLinks, gridSteps = steps, refinements = SEARCH_REFINEMENTS
        ).worstCovalentZ
        convergence += T124ConvergenceRecord(
            quantity = "worst covalent z at the best-aligned feasible placement",
            control = "torsion grid steps",
            level = steps,
            value = value,
            departureFromFinest = abs(value - finestZ)
        )
    }
    convergence.forEach {
        println(
            "  %-56s %-22s %4d  %10.6f  %.3e".format(
                it.quantity, it.control, it.level, it.value, it.departureFromFinest
            )
        )
    }

    // ---- upstream reproductions
    val reproductions = ArrayList<T124ReproductionRecord>()
    fun reproduce(quantity: String, published: Double, derived: Double) {
        reproductions += T124ReproductionRecord(
            quantity = quantity,
            published = published,
            derived = derived,
            relativeDeparture = if (published == 0.0) abs(derived)
            else abs(derived - published) / abs(published)
        )
    }
    reproduce("C-0057 census, covalent placements (independent staples)", 3546.0, independent.covalent.toDouble())
    reproduce("C-0057 census, reach-feasible placements (independent staples)", 1855.0, independent.feasible.size.toDouble())
    reproduce("C-0057 census, covalent placements (scaffold excursion)", 280.0, excursion.covalent.toDouble())
    reproduce("C-0057 census, reach-feasible placements (scaffold excursion)", 137.0, excursion.feasible.size.toDouble())
    val reported = bestTwoLinkClosure(backbone, RoutingTopology.INDEPENDENT_STAPLES)
    val reportedVerdict = torsionVerdict(
        junctionLinks(backbone, reported), gridSteps = VERDICT_GRID,
        refinements = VERDICT_REFINEMENTS
    )
    reproduce("C-0029 binding link gap [nm]", 0.600047126, reported.worstGap)
    reproduce("C-0057 worst covalent z at C-0029's optimum", 4.55315176, reportedVerdict.worstCovalentZ)
    reproduce("C-0057 closing links at C-0029's optimum", 0.0, reportedVerdict.closingLinks.toDouble())
    val recommended = capDesign(legLength = 7.0, separationBasePairs = 7)
    reproduce("C-0048 recommended frame couple [pN nm/rad]", 71.31, recommended.frameCouple)
    reproduce("C-0048 recommended critical load, CanDo [pN]", 8.95, recommended.criticalLoad)
    reproduce("C-0048 recommended margin, CanDo", 1.95, recommended.marginCanDo)
    reproduce("C-0048 recommended margin, Fields", 1.46, recommended.marginFields)
    reproduce("C-0048 recommended span [nm]", 28.25, recommended.span)
    reproduce("C-0052 chord budget at 21 steps [deg]", 78.53, legBudgetDegrees(21))
    reproduce("C-0029 alignment allowance [deg]", 16.87, degrees(allowance))
    reproduce("C-0029 allowance as a couple fraction", 0.9158, couplePhaseProjection(allowance))
    reproduce(
        "C-0042 conserved chord budget [pN nm/rad]", 91.76,
        chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0).total
    )
    println("  worst upstream departure: %.3e".format(reproductions.maxOf { it.relativeDeparture }))

    val bounds = listOf(
        T124BoundRecord(
            "the alignment band", degrees(allowance), "deg",
            "half the sheet's azimuthal quantum — C-0029's own ±16.87°, whose cos² is its 8.4 % " +
                    "allowance on the base couple"
        ),
        T124BoundRecord(
            "the alignment band as a share of the azimuth circle",
            2.0 * 2.0 * allowance / (2.0 * PI), "1",
            "two intervals of 2×16.87°, because a chord is a line"
        ),
        T124BoundRecord(
            "reach interval, tolerant lower", PhosphodiesterGeometry.reachMinimumTolerant, "nm",
            "C-0057's bound 2, at three measured sigma on every bond and angle"
        ),
        T124BoundRecord(
            "reach interval, tolerant upper", PhosphodiesterGeometry.reachMaximumTolerant, "nm",
            "a link outside the interval closes at NO torsion whatever"
        ),
        T124BoundRecord(
            "azimuth values carrying a reach-feasible placement",
            azimuths.count { it.topology == "INDEPENDENT_STAPLES" }.toDouble(), "count",
            "of ${set.azimuthSteps} on the grid — the falsifier declared in the Plan section"
        )
    )

    val result = T124Result(
        task = "T-124",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "medium" to "aqueous 2 mM MgCl2",
            "lattice" to "single-layer square-lattice Rothemund sheet, 2.69 nm interhelical",
            "backbone model" to "B-form phosphate helix, 10.67 bp/turn, 0.34 nm rise, " +
                    "r_P = 1.00 nm, groove 120° for the closure — C-0029's own; the MECHANICS " +
                    "on the hard, convention-free 180° chord, as C-0037/C-0042/C-0048 adopt",
            "torsion convention" to "IUPAC, degrees, folded to (-180, 180]",
            "chord convention" to "chord azimuth = standoff azimuth + groove/2 + pi/2; the " +
                    "flexure axis is pi/2; misalignment folded into [0, pi/2] because a chord " +
                    "is a line",
            "search grid" to "$SEARCH_GRID torsion steps, $SEARCH_REFINEMENTS refinements",
            "verdict grid" to "$VERDICT_GRID torsion steps, $VERDICT_REFINEMENTS refinements " +
                    "— C-0057's own",
            "rigidities" to "CanDo EI = ${Gen1Tile.DUPLEX_BENDING_RIGIDITY} pN nm^2 and Fields " +
                    "et al.'s implied ${FIELDS_BENDING_RIGIDITY.roundedForProse()} pN nm^2",
            "units" to "nm, pN, pN nm, pN/nm, k_BT = 4.141947 pN nm"
        ),
        bounds = bounds,
        census = census,
        azimuths = azimuths,
        aligned = aligned,
        pairs = pairs,
        trios = trios,
        designs = designs,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        findings = mapOf(
            "the cheap bound" to ("the reach-feasible set occupies %d of %d azimuth values, and " +
                    "the best alignment ANY reach-feasible placement offers is %.1f° — so the " +
                    "band bound %s the design before any torsion solve").format(
                azimuths.count { it.topology == "INDEPENDENT_STAPLES" }, set.azimuthSteps,
                degrees(independent.bestFeasibleMisalignment),
                if (independent.bestFeasibleMisalignment > allowance) "EXCLUDES" else "admits"
            ),
            "single junction" to alignedIndependent.verdict,
            "scaffold excursion" to alignedExcursion.verdict,
            "pair" to pairs.joinToString("; ") { "${it.separationBasePairs} bp: ${it.verdict}" },
            "trio" to trios.joinToString("; ") { "${it.crossbarBasePairs} bp: ${it.verdict}" },
            "what the mechanics become" to ("at the alignment the feasible set delivers the " +
                    "base couple keeps %.3f of itself in the loaded plane, and the best design " +
                    "over the 12–26 step envelope carries a buckling margin of %.2f on CanDo's " +
                    "rigidity and %.2f on Fields et al.'s, against C-0048's aligned 1.95 / 1.46")
                .format(
                    designs.drop(2).maxOf { it.loadedCoupleFraction },
                    designs.drop(2).maxOf { it.marginCanDo },
                    designs.drop(2).maxOf { it.marginFields }
                ),
            "what this cannot establish" to "A torsion check is a NECESSARY condition and never " +
                    "a sufficient one, exactly as C-0029 and C-0057 both said. And every " +
                    "negative here is bounded by its solve cap: it is a 'not found within the " +
                    "budget', never a 'does not exist'."
        )
    )

    val json = Json { prettyPrint = true }
    val file = File("gpd/results/T-124-torsion-feasible-routing.json")
    file.parentFile?.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)) + "\n")
    println("wrote ${file.path}")
}
