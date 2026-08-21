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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs

/**
 * Task `T-119`, leaf `A8.2` — **can a flexure hinge be built on a junction site the single-layer
 * sheet does not use?**
 *
 * ```shell
 * tools/study.sh anchoring.UnusedJunctionSiteStudyKt
 * ```
 *
 * Emits `gpd/results/T-119-unused-junction-site.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val ROWS = 15
private const val DESIGN_PATHS = 45
private val EDGE_X = Gen1Tile.EDGE_X
private val DUPLEX = OrigamiDuplex.INTERHELICAL
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val HINGE = Gen1Tile.crossoverHingeStiffness()
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
private val ACCEPTABLE = Gen1Tile.ACCEPTABLE_STROKE

/** `C-0034`'s `A2` — the arm's own duplex end, two strand termini at the phosphate radius. */
private val ANCHORAGE = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS)

/**
 * `C-0039`'s placed arm at a given path count, **re-derived** from its own library.
 *
 * **Memoised**, because it is a pure function of the count and a nested root solve over an RK
 * integration underneath it: the self-consistency scan asks for the same handful of counts several
 * hundred times, and `CLAUDE.md`'s own note applies — memoise a pure function a scan re-enters.
 */
private val armCache = HashMap<Int, Double>()

private fun armFor(paths: Int): Double = armCache.getOrPut(paths) {
    elasticaArmForStiffness(
        hingeStiffness = HINGE,
        hingeCount = 1,
        farStiffness = ANCHORAGE.rotationalStiffness,
        bendingRigidity = EI,
        count = paths,
        targetStiffness = MANDATE,
        workingDisplacement = ACCEPTABLE
    )
}

@Serializable
private data class T119Bound(
    val name: String,
    val value: Double,
    val against: Double,
    val ratio: Double,
    val settles: String
)

@Serializable
private data class T119AzimuthRecord(
    val azimuth: String,
    val basePairOffset: Int,
    val designAzimuthDegrees: Double,
    val outOfPlane: Boolean,
    val occupiedBySingleLayerSheet: Boolean,
    val registerDepartureDegrees: Double,
    val departureFromNearestOccupiedDegrees: Double
)

@Serializable
private data class T119PhaseRecord(
    val phaseBasePairs: Int,
    val planes: Int,
    val interfaceSites: Int,
    val outwardFacingSites: Int,
    val upwardSites: Int,
    val downwardSites: Int,
    val totalSites: Int,
    val unusedSites: Int,
    val usedFraction: Double,
    val inPlaneHingeCeiling: Int,
    val outOfPlaneHingeCeiling: Int,
    val armsPlaced: Int,
    val independentRowBound: Int
)

@Serializable
private data class T119RowRecord(
    val row: Int,
    val upwardSites: Int,
    val sitePitch: Double,
    val inPlaneSites: Int,
    val inPlaneSitePitch: Double,
    val armsPlaced: Int
)

@Serializable
private data class T119CountRecord(
    val pathCount: Int,
    val arm: Double,
    val armBasePairs: Double,
    val demand: Double,
    val armsPlacedUpward: Int,
    val armsPlacedInPlane: Int,
    val selfConsistentUpward: Boolean,
    val selfConsistentInPlane: Boolean,
    val interfaceCrossoversRetained: Int,
    val componentsUpward: Int,
    val duplexesBondedInPlane: Int,
    val eightBasePairDomains: Int
)

@Serializable
private data class T119BudgetRecord(
    val reading: String,
    val hingeCeiling: Int,
    val interfacesIntact: Boolean,
    val components: Int,
    val clearsFortyFive: Boolean,
    val note: String
)

@Serializable
private data class T119Placement(
    val row: Int,
    val rootX: Double,
    val towardPositiveX: Boolean,
    val low: Double,
    val high: Double
)

@Serializable
private data class T119Sensitivity(
    val axis: String,
    val reading: String,
    val value: Double,
    val upwardSitesBest: Int,
    val armsPlacedBest: Int,
    val selfConsistentCount: Int
)

@Serializable
private data class T119Convergence(
    val quantity: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double
)

@Serializable
private data class T119Reproduction(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private data class T119LiteratureNumber(
    val quantity: String,
    val value: String,
    val source: String,
    val access: String
)

@Serializable
private data class T119Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val cheapBounds: List<T119Bound>,
    val azimuths: List<T119AzimuthRecord>,
    val phases: List<T119PhaseRecord>,
    val rows: List<T119RowRecord>,
    val counts: List<T119CountRecord>,
    val budgets: List<T119BudgetRecord>,
    val bestPhasePlacement: List<T119Placement>,
    val sensitivities: List<T119Sensitivity>,
    val convergence: List<T119Convergence>,
    val reproductions: List<T119Reproduction>,
    val literature: List<T119LiteratureNumber>,
    val scaffold: Map<String, Double>,
    val predicates: Map<String, String>
)

fun main() {
    // ------------------------------------------------------------------ the cheap bounds
    val quarterTurn = azimuthDegrees(CROSSOVER_PLANE_BASE_PAIRS.toDouble())
    val outOfPlaneDeparture = registerDeparture(CROSSOVER_PLANE_BASE_PAIRS)
    val inPlaneDeparture = registerDeparture(2 * CROSSOVER_PLANE_BASE_PAIRS)
    val bestUpward = (0 until 32).maxOf { junctionSiteInventory(it, EDGE_X, ROWS).upwardSites }
    val cheapBounds = listOf(
        T119Bound(
            "the azimuth advance over one crossover plane, 8 bp x 33.75 deg/bp",
            quarterTurn, 270.0, quarterTurn / 270.0,
            "EXACTLY a quarter turn: the unoccupied azimuths point out of the sheet plane. " +
                    "Falsifier 1 did not fire"
        ),
        T119Bound(
            "the register departure of the UNUSED site at the preferred 10.5 bp/turn",
            outOfPlaneDeparture, inPlaneDeparture, outOfPlaneDeparture / inPlaneDeparture,
            "HALF that of the in-plane site the sheet does use, because the departure is " +
                    "linear in the offset. Falsifier 2 did not fire"
        ),
        T119Bound(
            "the upward inventory against the interfaces it would have to spare",
            bestUpward.toDouble(), (ROWS - 1).toDouble(), bestUpward.toDouble() / (ROWS - 1),
            "the escape is larger than the constraint it removes. Falsifier 4 did not fire"
        ),
        T119Bound(
            "the upward site pitch against the in-plane root pitch of an interior row",
            Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE,
            Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE / 2.0, 2.0,
            "an upward line belongs to ONE duplex, so its roots are TWICE as sparse " +
                    "as an interior row's — the price of the same fact that removes the " +
                    "connectivity cost"
        )
    )

    // ------------------------------------------------------------------ the four azimuths
    val azimuths = CrossoverAzimuth.entries.map {
        T119AzimuthRecord(
            azimuth = it.name,
            basePairOffset = it.basePairOffset,
            designAzimuthDegrees = it.designAzimuthDegrees,
            outOfPlane = it.outOfPlane,
            occupiedBySingleLayerSheet = !it.outOfPlane,
            registerDepartureDegrees = registerDeparture(it.basePairOffset),
            // the physically relevant one: the sheet's own crossovers fix the local twist, so an
            // unoccupied site is strained by its distance to the NEAREST occupied azimuth of the
            // same duplex, which is 8 bp for both out-of-plane azimuths and 0 for both in-plane
            departureFromNearestOccupiedDegrees = registerDeparture(
                minOf(
                    it.basePairOffset % (2 * CROSSOVER_PLANE_BASE_PAIRS),
                    2 * CROSSOVER_PLANE_BASE_PAIRS -
                            it.basePairOffset % (2 * CROSSOVER_PLANE_BASE_PAIRS)
                )
            )
        )
    }

    // ------------------------------------------------------------------ the census, all 32 phases
    val designArm = armFor(DESIGN_PATHS)
    val phases = (0 until 32).map { phase ->
        val inventory = junctionSiteInventory(phase, EDGE_X, ROWS)
        val placement = placeUpwardArms(phase, EDGE_X, ROWS, designArm)
        check(placement.arms == placement.independentRowBound) {
            "an upward site is not shared, so the construction must meet the bound at phase $phase"
        }
        check(inventory.interfaceSites == 49 || inventory.interfaceSites == 56) {
            "the interface inventory must be C-0015's 49 or 56, was ${inventory.interfaceSites}"
        }
        T119PhaseRecord(
            phaseBasePairs = phase,
            planes = inventory.planes,
            interfaceSites = inventory.interfaceSites,
            outwardFacingSites = inventory.outwardFacingSites,
            upwardSites = inventory.upwardSites,
            downwardSites = inventory.downwardSites,
            totalSites = inventory.totalSites,
            unusedSites = inventory.unusedSites,
            usedFraction = inventory.usedFraction,
            inPlaneHingeCeiling = inventory.inPlaneHingeCeiling,
            outOfPlaneHingeCeiling = inventory.outOfPlaneHingeCeiling,
            armsPlaced = placement.arms,
            independentRowBound = placement.independentRowBound
        )
    }
    val nominalPhase = 6
    val bestUpwardPhase = phases.maxBy { it.upwardSites }.phaseBasePairs

    // ------------------------------------------------------------------ the rows of the best phase
    val upward = upwardHingeSites(bestUpwardPhase, EDGE_X, ROWS)
    val inPlane = hingeSites(bestUpwardPhase, EDGE_X, ROWS)
    val bestPlacement = placeUpwardArms(bestUpwardPhase, EDGE_X, ROWS, designArm)
    val rows = (0 until ROWS).map { row ->
        val mine = upward.filter { it.interfaceIndex == row }.map { it.x }.sorted()
        val theirs = rowHingeSites(row, inPlane).map { it.x }.sorted()
        T119RowRecord(
            row = row,
            upwardSites = mine.size,
            sitePitch = if (mine.size < 2) 0.0 else mine.zipWithNext { a, b -> b - a }.min(),
            inPlaneSites = theirs.size,
            inPlaneSitePitch =
                if (theirs.size < 2) 0.0 else theirs.zipWithNext { a, b -> b - a }.min(),
            armsPlaced = bestPlacement.placements.count { it.row == row }
        )
    }

    // ------------------------------------------------------------------ the self-consistent count
    val countCandidates = listOf(10, 15, 20, 25, 28, 30, 31, 32, 33, 34, 35, 40, 42, 43, 45, 50, 56)
    val counts = countCandidates.map { paths ->
        val arm = armFor(paths)
        val upwardArms = (0 until 32).maxOf { placeUpwardArms(it, EDGE_X, ROWS, arm).arms }
        val inPlaneBest = (0 until 32).map { placeHingeArms(it, EDGE_X, ROWS, arm) }.maxBy { it.arms }
        val host = hostSheetAfterArms(inPlaneBest.truncatedTo(paths), EDGE_X, ROWS, arm)
        val inventory = junctionSiteInventory(bestUpwardPhase, EDGE_X, ROWS)
        T119CountRecord(
            pathCount = paths,
            arm = arm,
            armBasePairs = arm / RISE,
            demand = arm + DUPLEX,
            armsPlacedUpward = upwardArms,
            armsPlacedInPlane = inPlaneBest.arms,
            selfConsistentUpward = upwardArms >= paths && paths <= inventory.upwardSites,
            selfConsistentInPlane = inPlaneBest.arms >= paths,
            interfaceCrossoversRetained = inventory.interfaceSites,
            componentsUpward = 1,
            duplexesBondedInPlane = host.largestComponentSegments,
            eightBasePairDomains = eightBasePairDomains(paths)
        )
    }
    val selfConsistent = selfConsistentUpwardArmCount(EDGE_X, ROWS, minimumCount = 10) { armFor(it) }

    // ------------------------------------------------------------------ the budgets, side by side
    val nominal = junctionSiteInventory(nominalPhase, EDGE_X, ROWS)
    val best = junctionSiteInventory(bestUpwardPhase, EDGE_X, ROWS)
    val budgets = listOf(
        T119BudgetRecord(
            "C-0054 — hinges drawn from the sheet's own in-plane crossovers",
            nominal.inPlaneHingeCeiling, false, 1, nominal.inPlaneHingeCeiling >= DESIGN_PATHS,
            "56 - 14 = 42; every interface is left with exactly one crossover, and 45 severs " +
                    "the tile into at least four pieces"
        ),
        T119BudgetRecord(
            "T-119 — hinges rooted on the UNOCCUPIED upward azimuth, nominal phase",
            nominal.outOfPlaneHingeCeiling, true, 1,
            nominal.outOfPlaneHingeCeiling >= DESIGN_PATHS,
            "no interface crossover is consumed at all, so the sheet keeps its whole inventory"
        ),
        T119BudgetRecord(
            "T-119 — the same at the phase that maximises the upward inventory",
            best.outOfPlaneHingeCeiling, true, 1, best.outOfPlaneHingeCeiling >= DESIGN_PATHS,
            "the phase that maximises the upward inventory carries 49 interface crossovers, " +
                    "not 56 — the two inventories are maximised at different phases"
        ),
        T119BudgetRecord(
            "T-119 — both unoccupied azimuths, if a downward arm were admissible",
            best.upwardSites + best.downwardSites, true, 1,
            best.upwardSites + best.downwardSites >= DESIGN_PATHS,
            "NOT adopted: the downward azimuth points into the grafted layer"
        ),
        T119BudgetRecord(
            "the placement, which is what actually binds",
            selfConsistent, true, 1, selfConsistent >= DESIGN_PATHS,
            "the self-consistent count on the upward root lattice, against C-0053's 25 in plane"
        )
    )

    // ------------------------------------------------------------------ the plan view
    val placementAtCount = placeUpwardArms(
        bestUpwardPhase, EDGE_X, ROWS, armFor(selfConsistent)
    ).truncatedTo(selfConsistent)
    val bestPhasePlacement = placementAtCount.placements.map {
        T119Placement(it.row, it.rootX, it.towardPositiveX, it.low, it.high)
    }

    // ------------------------------------------------------------------ sensitivities
    val sensitivities = listOf(
        Triple("interhelical distance", "the 2.0 nm steric diameter rather than 2.69 nm SAXS", 2.0),
        Triple("interhelical distance", "the SAXS single-layer 2.69 nm, as adopted", DUPLEX),
        Triple("interhelical distance", "the 2.73 nm square-lattice SAXS value", 2.73)
    ).map { (axis, reading, width) ->
        val count = selfConsistentUpwardArmCount(
            EDGE_X, ROWS, minimumCount = 10, width = width
        ) { armFor(it) }
        T119Sensitivity(
            axis = axis, reading = reading, value = width,
            upwardSitesBest = bestUpward,
            armsPlacedBest = (0 until 32).maxOf {
                placeUpwardArms(it, EDGE_X, ROWS, designArm, width).arms
            },
            selfConsistentCount = count
        )
    } + listOf(
        T119Sensitivity(
            axis = "duplex count",
            reading = "a 16-duplex host, C-0053's own named escape",
            value = 16.0,
            upwardSitesBest = (0 until 32).maxOf { junctionSiteInventory(it, EDGE_X, 16).upwardSites },
            armsPlacedBest = (0 until 32).maxOf { placeUpwardArms(it, EDGE_X, 16, designArm).arms },
            selfConsistentCount = selfConsistentUpwardArmCount(EDGE_X, 16, minimumCount = 10) { armFor(it) }
        ),
        T119Sensitivity(
            axis = "tile edge",
            reading = "a 49.25 nm edge, C-0053's own named escape",
            value = 49.25,
            upwardSitesBest = (0 until 32).maxOf { junctionSiteInventory(it, 49.25, ROWS).upwardSites },
            armsPlacedBest = (0 until 32).maxOf { placeUpwardArms(it, 49.25, ROWS, designArm).arms },
            selfConsistentCount = selfConsistentUpwardArmCount(49.25, ROWS, minimumCount = 10) { armFor(it) }
        )
    )

    // ------------------------------------------------------------------ convergence
    val coarseUpward = (0 until 32).map { junctionSiteInventory(it, EDGE_X, ROWS).upwardSites }.toSet()
    val fineUpward = (0 until 320).map { junctionSiteInventory(it, EDGE_X, ROWS).upwardSites }.toSet()
    check(coarseUpward == fineUpward) { "the 32 bp phase sweep must be complete" }
    val minClearance = (0 until 32).minOf { phase ->
        junctionPlanes(phase, EDGE_X).minOf { abs(abs(it) - (EDGE_X / 2.0 - CrossoverLayoutMargin)) }
    }
    val convergence = listOf(
        T119Convergence(
            "the upward inventory over a ten-fold refined phase grid", coarseUpward.size.toDouble(),
            fineUpward.size.toDouble(), 0.0
        ),
        T119Convergence(
            "the closest a crossover plane comes to the footprint truncation [nm]",
            minClearance, CrossoverLayoutMargin, minClearance / CrossoverLayoutMargin
        ),
        T119Convergence(
            "the self-consistent count under a 1e-6 nm nudge of the tile edge",
            selfConsistent.toDouble(),
            selfConsistentUpwardArmCount(EDGE_X + 1e-6, ROWS, minimumCount = 10) { armFor(it) }.toDouble(),
            0.0
        )
    )

    // ------------------------------------------------------------------ reproductions
    val nominalInventory = junctionSiteInventory(nominalPhase, EDGE_X, ROWS)
    val reproductions = listOf(
        T119Reproduction(
            "Ke et al.'s square-lattice twist [deg per bp]", 33.75,
            SQUARE_LATTICE_DEGREES_PER_BASE_PAIR, 0.0, "Ke et al. 2009, read directly"
        ),
        T119Reproduction(
            "Ke et al.'s preferred B-DNA twist [deg per bp]", 34.3,
            azimuthDegrees(1.0, PREFERRED_BASE_PAIRS_PER_TURN), 0.0,
            "Ke et al. 2009, read directly (quoted to three digits)"
        ),
        T119Reproduction(
            "Ke et al.'s 0.75 turns per 8 bp", 0.75,
            azimuthDegrees(8.0) / 360.0, 0.0, "Ke et al. 2009, read directly"
        ),
        T119Reproduction(
            "Ke et al.'s 3.0 turns per 32 bp", 3.0,
            azimuthDegrees(32.0) / 360.0, 0.0, "Ke et al. 2009, read directly"
        ),
        T119Reproduction(
            "caDNAno's honeycomb two-thirds turn per 7 bp", 240.0,
            azimuthDegrees(7.0, 10.5), 0.0, "Douglas et al. 2009 NAR, read directly"
        ),
        T119Reproduction(
            "C-0015's inventory at the ten eight-column phases", 56.0,
            nominalInventory.interfaceSites.toDouble(), 0.0, "C-0015, re-derived from the azimuth"
        ),
        T119Reproduction(
            "C-0015's inventory at the other twenty-two phases", 49.0,
            junctionSiteInventory(0, EDGE_X, ROWS).interfaceSites.toDouble(), 0.0,
            "C-0015, re-derived from the azimuth"
        ),
        T119Reproduction(
            "C-0040's crossovers on one interface", 4.0,
            junctionSites(nominalPhase, EDGE_X, ROWS)
                .count { it.azimuth == CrossoverAzimuth.NORTH && it.duplex == 0 }.toDouble(),
            0.0, "C-0040, re-derived"
        ),
        T119Reproduction(
            "C-0054's in-plane hinge ceiling", 42.0,
            nominalInventory.inPlaneHingeCeiling.toDouble(), 0.0, "C-0054, re-derived"
        ),
        T119Reproduction(
            "C-0039's E5a1 arm at 45 paths [nm]", 9.131, designArm, 0.0, "C-0039, re-run as a library"
        ),
        T119Reproduction(
            "C-0053's arm demand at 45 paths [nm]", 11.821, designArm + DUPLEX, 0.0, "C-0053"
        ),
        T119Reproduction(
            "C-0053's in-plane placement at 45 paths", 43.0,
            (0 until 32).maxOf { placeHingeArms(it, EDGE_X, ROWS, designArm).arms }.toDouble(),
            0.0, "C-0053, re-run as a library"
        ),
        T119Reproduction(
            "the per-interface pitch [nm]", 10.88,
            Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE, 0.0, "C-0040/C-0015"
        )
    ).map { it.copy(relativeDeparture = abs(it.derived - it.published) / abs(it.published)) }

    // ------------------------------------------------------------------ literature provenance
    val literature = listOf(
        T119LiteratureNumber(
            "four nearest neighbours, crossover planes at 8 bp, same pair every 32 bp",
            "\"every 8 bp, that staple strand is positioned to cross over to one of its four " +
                    "neighbors … adjacent helices share crossovers every 32 bp\"",
            "Ke, Douglas, Liedl, Shih, JACS 131:15903 (2009), PMC2821935",
            "READ DIRECTLY"
        ),
        T119LiteratureNumber(
            "the two crossover-plane families are orthogonal",
            "\"crossovers in i and iii sectional slices are parallel to the xz-plane, while the " +
                    "crossovers in ii and iv sectional slices are parallel to the yz-plane\"",
            "Ke et al. 2009, PMC2821935", "READ DIRECTLY"
        ),
        T119LiteratureNumber(
            "the designed and preferred twist", "33.75 deg/bp (32 bp per 3 turns); 34.3 deg/bp",
            "Ke et al. 2009, PMC2821935", "READ DIRECTLY"
        ),
        T119LiteratureNumber(
            "the published COST of a crossover 8 bp from another",
            "\"some staple breaks must be implemented between crossovers 8 bp apart … " +
                    "Introducing these breaks may be destabilizing for the structure\", and " +
                    "omitting them gave \"a high yield of well-folded structures\"",
            "Ke et al. 2009, PMC2821935", "READ DIRECTLY"
        ),
        T119LiteratureNumber(
            "caDNAno's honeycomb analogue of the same rule",
            "\"potential staple-crossover positions occur every seven base pairs, or two-thirds " +
                    "of a turn\" and repeat \"every 21 base pairs\" at 10.5 bp/turn",
            "Douglas et al., NAR 37:5001 (2009), PMC2731887", "READ DIRECTLY"
        ),
        T119LiteratureNumber(
            "a FREE lever held to a single-layer sheet by one crossover at an unoccupied azimuth",
            "no instance found in 37 + 25 recorded EuropePMC queries across fifteen families",
            "see gpd/results/T-119-unused-junction-site.json and the claim's query table",
            "NOT FOUND"
        )
    )

    // ------------------------------------------------------------------ the scaffold ledger
    val sheetScaffold = scaffoldBasePairs(ROWS, EDGE_X)
    val armScaffold = armScaffoldBasePairs(selfConsistent, armFor(selfConsistent))
    val scaffold = mapOf(
        "sheetBasePairs" to sheetScaffold.toDouble(),
        "armBasePairs" to armScaffold.toDouble(),
        "totalBasePairs" to (sheetScaffold + armScaffold).toDouble(),
        "m13Nucleotides" to M13_SCAFFOLD_NUCLEOTIDES.toDouble(),
        "fractionOfM13" to (sheetScaffold + armScaffold).toDouble() / M13_SCAFFOLD_NUCLEOTIDES
    )

    // ------------------------------------------------------------------ the predicates
    val predicates = mapOf(
        "P1 does an unoccupied junction site exist?" to
                ("YES. Two of the square lattice's four azimuths are unoccupied by a single-layer " +
                        "sheet, and they lie at EXACTLY ${"%.1f".format(quarterTurn)} deg and " +
                        "${"%.1f".format(360.0 - quarterTurn)} deg from the occupied pair — out of " +
                        "the sheet plane. Ke et al. (2009), read directly."),
        "P2 is it in register?" to
                ("YES, and it is in BETTER register than the site the sheet does use: at the " +
                        "preferred 10.5 bp/turn the 8 bp out-of-plane site departs by " +
                        "${"%.3f".format(outOfPlaneDeparture)} deg against " +
                        "${"%.3f".format(inPlaneDeparture)} deg for the sheet's own next in-plane " +
                        "crossover, because the departure is linear in the offset."),
        "P3 what is the unused inventory?" to
                ("${phases.minOf { it.unusedSites }}-${phases.maxOf { it.unusedSites }} sites " +
                        "against the ${phases.minOf { it.interfaceSites }}-" +
                        "${phases.maxOf { it.interfaceSites }} the sheet builds: the sheet occupies " +
                        "${"%.1f".format(100.0 * phases.minOf { it.usedFraction })}-" +
                        "${"%.1f".format(100.0 * phases.maxOf { it.usedFraction })} % of its own " +
                        "lattice, under a third at every phase."),
        "P4 what does C-0054's budget become?" to
                ("its ${nominal.inPlaneHingeCeiling} of ${nominal.interfaceSites} becomes " +
                        "${nominal.outOfPlaneHingeCeiling} upward hinges at the same phase and " +
                        "${best.outOfPlaneHingeCeiling} at the best one, with EVERY interface " +
                        "crossover retained and the sheet in ONE piece at every count. The " +
                        "pigeonhole does not bind."),
        "P5 what does C-0053's count become?" to
                ("25 becomes $selfConsistent — the host is untouched, so what binds is the " +
                        "upward root pitch of ${"%.2f".format(Gen1Tile.CROSSOVER_SPACING_SHEET_BP * RISE)} nm " +
                        "against a demand of ${"%.2f".format(designArm + DUPLEX)} nm."),
        "P6 does §3's 45 place?" to
                (if (selfConsistent >= DESIGN_PATHS) "YES" else
                    "NO. $selfConsistent places; 45 does not, for a NEW reason — the root pitch, " +
                            "not the host's survival."),
        "P7 is the motif demonstrated?" to
                ("NO. The SITE and the crossover at it are published square-lattice geometry; a " +
                        "FREE LEVER held by one crossover at that site is not, and Ke et al. " +
                        "report the 8 bp staple break it forces as a yield cost.")
    )

    val result = T119Result(
        task = "T-119",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "medium" to "aqueous 2 mM MgCl2",
            "sheet" to "single-layer square-lattice Rothemund, $ROWS duplexes at $DUPLEX nm",
            "edgeX" to "$EDGE_X nm",
            "risePerBasePair" to "$RISE nm",
            "designedTwist" to
                    "${SQUARE_LATTICE_BASE_PAIRS_PER_TURN.roundedForProse()} bp/turn (33.75 deg/bp)",
            "preferredTwist" to "$PREFERRED_BASE_PAIRS_PER_TURN bp/turn",
            "crossoverPlaneSpacing" to "$CROSSOVER_PLANE_BASE_PAIRS bp",
            "arm" to "C-0039's E5a1, placed at the mandate ${MANDATE.roundedForProse()} pN/nm " +
                    "at $ACCEPTABLE nm"
        ),
        cheapBounds = cheapBounds,
        azimuths = azimuths,
        phases = phases,
        rows = rows,
        counts = counts,
        budgets = budgets,
        bestPhasePlacement = bestPhasePlacement,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        literature = literature,
        scaffold = scaffold,
        predicates = predicates
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-119-unused-junction-site.json")
    output.parentFile.mkdirs()
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")

    println("=== T-119 — a junction site the single-layer sheet does not use ".padEnd(110, '='))
    println()
    println("--- the cheap bounds ".padEnd(110, '-'))
    cheapBounds.forEach {
        println("%-64s %10.4f against %8.4f  (%.3f)".format(it.name.take(64), it.value, it.against, it.ratio))
        println("    ${it.settles}")
    }
    println()
    println("--- the four azimuths ".padEnd(110, '-'))
    azimuths.forEach {
        println(
            ("%-6s %2d bp  %6.1f deg  %-13s %-10s departure from NORTH %6.3f deg, " +
                    "from the nearest occupied %5.3f deg").format(
                it.azimuth, it.basePairOffset, it.designAzimuthDegrees,
                if (it.outOfPlane) "OUT OF PLANE" else "in plane",
                if (it.occupiedBySingleLayerSheet) "OCCUPIED" else "empty",
                it.registerDepartureDegrees, it.departureFromNearestOccupiedDegrees
            )
        )
    }
    println()
    println("--- the census, all 32 phases ".padEnd(110, '-'))
    phases.forEach {
        println(
            ("phase %2d bp  planes %2d  interface %2d  upward %2d  downward %2d  outward %d  " +
                    "total %3d  used %.4f  ceilings %2d / %2d  arms %2d").format(
                it.phaseBasePairs, it.planes, it.interfaceSites, it.upwardSites, it.downwardSites,
                it.outwardFacingSites, it.totalSites, it.usedFraction, it.inPlaneHingeCeiling,
                it.outOfPlaneHingeCeiling, it.armsPlaced
            )
        )
    }
    println()
    println("--- the rows at the best upward phase ($bestUpwardPhase bp) ".padEnd(110, '-'))
    rows.forEach {
        println(
            "row %2d  upward sites %d at %5.2f nm   in-plane sites %2d at %5.2f nm   arms %d".format(
                it.row, it.upwardSites, it.sitePitch, it.inPlaneSites, it.inPlaneSitePitch,
                it.armsPlaced
            )
        )
    }
    println()
    println("--- the self-consistent count ".padEnd(110, '-'))
    counts.forEach {
        println(
            ("n %2d  arm %6.3f (%5.1f bp)  demand %6.3f  upward %2d %-16s in-plane %2d %-9s " +
                    "interfaces kept %2d  bonded in plane %2d").format(
                it.pathCount, it.arm, it.armBasePairs, it.demand, it.armsPlacedUpward,
                if (it.selfConsistentUpward) "SELF-CONSISTENT" else "short",
                it.armsPlacedInPlane, if (it.selfConsistentInPlane) "consistent" else "short",
                it.interfaceCrossoversRetained, it.duplexesBondedInPlane
            )
        )
    }
    println()
    println("    self-consistent upward count: $selfConsistent")
    println()
    println("--- the budgets ".padEnd(110, '-'))
    budgets.forEach {
        println(
            "%-62s ceiling %3d  interfaces intact %-5s  45 clears %s".format(
                it.reading.take(62), it.hingeCeiling, it.interfacesIntact, it.clearsFortyFive
            )
        )
        println("    ${it.note}")
    }
    println()
    println("--- sensitivities ".padEnd(110, '-'))
    sensitivities.forEach {
        println(
            "%-22s %-46s %6.2f  upward %2d  arms %2d  self-consistent %2d".format(
                it.axis, it.reading.take(46), it.value, it.upwardSitesBest, it.armsPlacedBest,
                it.selfConsistentCount
            )
        )
    }
    println()
    println("--- reproductions ".padEnd(110, '-'))
    reproductions.forEach {
        println(
            "%-58s published %10.4f  derived %10.4f  departure %.2e".format(
                it.quantity.take(58), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    println("--- literature ".padEnd(110, '-'))
    literature.forEach { println("[${it.access}] ${it.quantity}\n    ${it.source}") }
    println()
    println("--- predicates ".padEnd(110, '-'))
    predicates.forEach { (key, value) -> println("$key\n    $value\n") }
    println("written: ${output.path}")
}

/** [com.xemantic.nano.plentyofroom.structure.CrossoverLayout]'s own footprint margin. */
private const val CrossoverLayoutMargin: Double =
    com.xemantic.nano.plentyofroom.structure.CrossoverLayout.EDGE_MARGIN
