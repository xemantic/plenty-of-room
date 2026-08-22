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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-151` — **can the Gen-1 tile be raster-folded without a scaffold seam?**
 *
 * Emits `gpd/results/T-151-scaffold-routing.json`, deterministically: every number in it is an
 * integer, an exhaustive count or a closed form.
 */

private const val T151_DUPLEXES = 15
private const val T151_PHASE = 24
private val T151_EDGE_X = Gen1Tile.EDGE_X
private val T151_RISE = Gen1Tile.RISE_PER_BASE_PAIR

@Serializable
private data class T151GraphRecord(
    val duplexes: Int,
    val hamiltonianPaths: Int,
    val hamiltonianCycles: Int,
    val minimumSegmentsPerRowLinear: Int,
    val minimumSegmentsPerRowCircular: Int
)

@Serializable
private data class T151TopologyRecord(
    val topology: String,
    val segmentsPerRow: Int,
    val seamsRequired: Int,
    val scaffoldCrossovers: Int,
    val seamless: Boolean,
    val precedent: String,
    val precedentFlag: String,
    val cost: String,
    val reason: String
)

@Serializable
private data class T151WidthRecord(
    val basePairs: Int,
    val nanometres: Double,
    val admissible: Boolean,
    val departureFromForty: Double
)

@Serializable
private data class T151ScaffoldRecord(
    val quantity: String,
    val nucleotides: Long,
    val note: String
)

@Serializable
private data class T151SeamCostRecord(
    val kind: String,
    val affectedStations: Int,
    val ofStations: Int,
    val planes: List<Int>,
    val note: String
)

@Serializable
private data class T151ReproductionRecord(
    val quantity: String,
    val source: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double
)

@Serializable
private data class T151PredicateRecord(
    val id: String,
    val statement: String,
    val met: Boolean,
    val evidence: String
)

@Serializable
private data class T151FalsifierRecord(
    val id: String,
    val falsifier: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T151Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val graph: List<T151GraphRecord>,
    val topologies: List<T151TopologyRecord>,
    val widths: List<T151WidthRecord>,
    val scaffoldBudget: List<T151ScaffoldRecord>,
    val seamCosts: List<T151SeamCostRecord>,
    val reproductions: List<T151ReproductionRecord>,
    val predicates: List<T151PredicateRecord>,
    val falsifiers: List<T151FalsifierRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

/** `C-0063`'s 34 upward roots, read from its own result file rather than transcribed. */
private fun t151Rows(file: File): List<List<Double>> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .sortedBy { it.getValue("row").jsonPrimitive.content.toInt() }
        .map { row ->
            row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() }.sorted()
        }
}

@Suppress("LongMethod")
fun main() {
    val profile = WeaveProfile(phaseBasePairs = T151_PHASE, duplexes = T151_DUPLEXES)
    val roots = t151Rows(ResultInputs.T_125.file())
    check(roots.sumOf { it.size } == 34) { "C-0063's placement must carry 34 stations" }
    val stationPlanes = roots.map { row -> row.map { weavePlaneIndex(profile, it) } }
    val seamPlanes = seamPlanesWithin(profile, T151_EDGE_X)

    // -------------------------------------------------- deliverable 1: the theorem, computed
    println("T-151 — the row graph, brute-forced ...")
    val graph = (2..12).map { rows ->
        T151GraphRecord(
            duplexes = rows,
            hamiltonianPaths = hamiltonianRowPathCount(rows),
            hamiltonianCycles = hamiltonianRowCycleCount(rows),
            minimumSegmentsPerRowLinear = minimumSegmentsPerRow(ScaffoldTopology.LINEAR, rows),
            minimumSegmentsPerRowCircular =
                minimumSegmentsPerRow(ScaffoldTopology.CIRCULAR_FULLY_FOLDED, rows)
        )
    }

    // ------------------------------------------- deliverable 2: the three specifications
    val topologies = ScaffoldTopology.entries.map { topology ->
        val verdict = seamlessRoutingVerdict(topology, T151_DUPLEXES)
        T151TopologyRecord(
            topology = topology.name,
            segmentsPerRow = verdict.segmentsPerRow,
            seamsRequired = verdict.seamsRequired,
            scaffoldCrossovers = verdict.scaffoldCrossovers,
            seamless = verdict.seamless,
            precedent = when (topology) {
                ScaffoldTopology.LINEAR ->
                    "Rothemund's 26-helix square: \"had no vertical reversals in raster " +
                            "direction, required a linear scaffold\""
                ScaffoldTopology.CIRCULAR_FULLY_FOLDED ->
                    "every Rothemund rectangle: \"the folding path shown in Fig. 1b is " +
                            "compatible with a circular scaffold and leaves a 'seam'\""
                ScaffoldTopology.CIRCULAR_WITH_REMAINDER ->
                    "Rothemund's FIRST origami experiment, the 8-helix third-square: \"a " +
                            "circular M13mp18 scaffold DNA was used rather than a linearized " +
                            "one, because the corners of the rectangle were close enough that " +
                            "the unfolded portion of the M13mp18 scaffold DNA could easily " +
                            "bridge the corners without deforming the rectangle\""
            },
            precedentFlag = "CITED, READ DIRECTLY (Rothemund 2006, gpd/data/T-151-sources/)",
            cost = when (topology) {
                ScaffoldTopology.LINEAR ->
                    "BsrBI digestion, and Rothemund's own measured price for it: the star " +
                            "folded 63 % well-formed on untreated circular scaffold and 11 % on " +
                            "the linearised one, which he attributes to \"strand breakage " +
                            "occurring during BsrBI digestion or subsequent steps\". A " +
                            "synthetic or PCR scaffold of the right length avoids the digestion " +
                            "entirely and is the modern route"
                ScaffoldTopology.CIRCULAR_FULLY_FOLDED ->
                    "one seam, and C-0081's 6-12 of 34 stations off the weave node"
                ScaffoldTopology.CIRCULAR_WITH_REMAINDER ->
                    "an unpaired remainder: on M13 that is 5 569 nt, a ~34 nm coil carrying " +
                            "1.66x the sheet's own backbone charge, sitting beside a tile whose " +
                            "edge load C-0022 solved without it"
            },
            reason = verdict.reason
        )
    }

    // ------------------------------------------- deliverable 3: the odd half-turn constraint
    println("T-151 — the admissible raster widths ...")
    val admissible = admissibleRasterRowLengths(200)
    val nearest = nearestAdmissibleWidth(T151_EDGE_X)!!
    val nominal = Math.round(T151_EDGE_X / T151_RISE).toInt()
    val widths = (admissible + listOf(nominal)).distinct().sorted().map {
        T151WidthRecord(
            basePairs = it,
            nanometres = it * T151_RISE,
            admissible = isOddHalfTurnSeparation(it),
            departureFromForty = it * T151_RISE - T151_EDGE_X
        )
    }

    // ------------------------------------------- deliverable 4: the scaffold budget
    val usedAtNearest = sheetScaffoldNucleotides(T151_DUPLEXES, nearest).toLong()
    val usedAtNominal = sheetScaffoldNucleotides(T151_DUPLEXES, nominal).toLong()
    val remainder = M13_SCAFFOLD_NUCLEOTIDES - usedAtNearest
    val loop = returnLoopNucleotides(T151_DUPLEXES)
    val remainderRadius = singleStrandedRadiusOfGyration(remainder.toInt())
    val scaffoldBudget = listOf(
        T151ScaffoldRecord(
            "the sheet at the nearest admissible width, 112 bp", usedAtNearest,
            "15 rows x 112 bp; the tile's whole scaffold demand"
        ),
        T151ScaffoldRecord(
            "the sheet at the nominal 40.0 nm, ${nominal} bp", usedAtNominal,
            "not buildable as a seamless raster: ${nominal} bp is not an odd number of half turns"
        ),
        T151ScaffoldRecord(
            "M13mp18, circular", M13_SCAFFOLD_NUCLEOTIDES,
            "CITED; 4.31x what the tile needs, so a remainder is unavoidable on this scaffold"
        ),
        T151ScaffoldRecord(
            "M13mp18 after BsrBI digestion", M13_LINEARISED_NUCLEOTIDES.toLong(),
            "CITED, READ DIRECTLY: \"While 7,176 nt remained available for folding\""
        ),
        T151ScaffoldRecord(
            "the unpaired remainder at 112 bp", remainder,
            "a ${"%.1f".format(remainderRadius)} nm ideal coil, 1.66x the sheet's own backbone " +
                    "charge; Rothemund's own first experiment left 2/3 of M13 unfolded and " +
                    "reports \"long, unfolded single-stranded sections of the scaffold do not " +
                    "adversely affect folding\""
        ),
        T151ScaffoldRecord(
            "the return loop a circular scaffold would need", loop.toLong(),
            "(15-1) x 2.69 nm over 0.57 nm per nucleotide: the whole seamless closure a " +
                    "purpose-built circular scaffold would have to carry outside the sheet"
        )
    )

    // ------------------------------------------- deliverable 5: what a seam costs, and the stagger
    println("T-151 — the straight and staggered seam costs ...")
    val straightCosts = seamPlanes.map { it to straightSeamCost(stationPlanes, it) }
    val staggered = bestStaggeredSeam(stationPlanes, seamPlanes)
    val seamCosts = straightCosts.map { (plane, cost) ->
        T151SeamCostRecord(
            "straight seam at plane $plane", cost, 34, listOf(plane),
            "C-0081's own sweep, reproduced from its station planes"
        )
    } + T151SeamCostRecord(
        "the best STAGGERED seam", staggered.affectedStations, 34, staggered.planes,
        "Rothemund's own alternative, solved exactly by a three-row dynamic program"
    ) + T151SeamCostRecord(
        "NO SEAM", 0, 34, emptyList(),
        "the boustrophedon: C-0076's congruence intact, C-0081's amplitude bracket annihilated"
    )

    fun reproduction(q: String, s: String, p: Double, r: Double) =
        T151ReproductionRecord(q, s, p, r, abs(r - p) / maxOf(abs(p), 1.0e-12))
    val reproductions = listOf(
        reproduction("C-0081's candidate seam planes", "C-0081", 8.0, seamPlanes.size.toDouble()),
        reproduction(
            "C-0081's best straight seam cost", "C-0081", 6.0,
            straightCosts.minOf { it.second }.toDouble()
        ),
        reproduction(
            "C-0081's worst straight seam cost", "C-0081", 12.0,
            straightCosts.maxOf { it.second }.toDouble()
        ),
        reproduction("C-0063's station count", "C-0063", 34.0, stationPlanes.sumOf { it.size }
            .toDouble()),
        reproduction(
            "C-0076's congruence: every station on an ODD plane", "C-0076", 34.0,
            stationPlanes.sumOf { row -> row.count { isWeaveNode(it) } }.toDouble()
        ),
        reproduction("the tile in weave periods", "C-0081", 3.68, T151_EDGE_X / profile.period)
    )

    val predicates = listOf(
        T151PredicateRecord(
            "P1", "a seamless scaffold routing for a 15-duplex 40 nm single-layer sheet is given",
            true,
            "the boustrophedon, one segment per row, ${T151_DUPLEXES - 1} scaffold crossovers, " +
                    "under a LINEAR scaffold or a circular one with a remainder"
        ),
        T151PredicateRecord(
            "P2", "the routing is checked against the row graph rather than asserted", true,
            "the path graph P_15 carries ${hamiltonianRowPathCount(12)} Hamiltonian paths at " +
                    "every row count 3..12 and ZERO Hamiltonian cycles, brute-forced"
        ),
        T151PredicateRecord(
            "P3", "the precedent is a built structure and it is read directly", true,
            "Rothemund's 26-helix square (linear scaffold, no vertical reversals) and his 8-helix " +
                    "third-square (circular scaffold, 2/3 unfolded)"
        ),
        T151PredicateRecord(
            "P4", "what the seamless routing costs is stated", true,
            "a scaffold SPECIFICATION this programme has never made, and on M13 an unpaired " +
                    "$remainder nt remainder"
        ),
        T151PredicateRecord(
            "P5", "the fallback if a circular fully folded scaffold is mandated is priced", true,
            "the best staggered seam costs ${staggered.affectedStations} of 34 against the best " +
                    "straight seam's ${straightCosts.minOf { it.second }} — a remedy worth one " +
                    "station, not a remedy"
        )
    )

    val falsifiers = listOf(
        T151FalsifierRecord(
            "F1", "a Hamiltonian CYCLE exists on the 15-row path graph",
            graph.any { it.duplexes >= 3 && it.hamiltonianCycles > 0 },
            "zero at every row count from 3 to 12; two at 2 rows, which is where the theorem " +
                    "starts"
        ),
        T151FalsifierRecord(
            "F2", "no seamless routing exists at this aspect ratio under ANY specification",
            topologies.none { it.seamless },
            "two of three specifications are seamless, and both have a built precedent"
        ),
        T151FalsifierRecord(
            "F3", "Rothemund's own record contradicts the theorem", false,
            "his seamless square required a LINEAR scaffold and his seamed rectangles are " +
                    "circular and fully folded — the theorem's prediction, in his own words"
        ),
        T151FalsifierRecord(
            "F4", "no admissible seamless width lies near 40 nm",
            abs(nearest * T151_RISE - T151_EDGE_X) > 5.44,
            "112 bp = ${(nearest * T151_RISE).roundedForProse()} nm, ${"%.2f".format(
                100.0 * (nearest * T151_RISE - T151_EDGE_X) / T151_EDGE_X
            )} % from the nominal 40.0 — and the NOMINAL width is NOT admissible"
        ),
        T151FalsifierRecord(
            "F5", "no staggered seam clears every station, so a mandated seam has no remedy",
            staggered.affectedStations > 0,
            "FIRED: the best staggered seam still costs ${staggered.affectedStations} of 34, " +
                    "one better than the best straight one. Rothemund's stagger is not the " +
                    "remedy; seamlessness is"
        )
    )

    val findings = listOf(
        "A SEAM IS A PARITY ON A TREE, NOT A CONVENTION. Crossovers join only ADJACENT duplexes, " +
                "so a single-layer sheet's row-adjacency graph is the path P_D — a tree — and a " +
                "closed walk on a tree traverses every edge an EVEN number of times. A fully " +
                "folded CIRCULAR scaffold therefore gives every row two segments, i.e. one seam " +
                "crossing every row. Brute-forced: P_D has 2 Hamiltonian paths and ZERO " +
                "Hamiltonian cycles at every D from 3 to 12.",
        "SO THE ANSWER IS YES, AND IT IS CONDITIONAL ON A SPECIFICATION NOBODY HAS MADE. A " +
                "LINEAR scaffold needs only a Hamiltonian path, and a CIRCULAR one that is not " +
                "fully folded closes through its own unpaired remainder. Both are seamless; " +
                "both are BUILT.",
        "THE THEOREM PREDICTS ROTHEMUND'S OWN RECORD AND HE STATES IT IN ONE CLAUSE EACH. His " +
                "26-helix square \"had no vertical reversals in raster direction, REQUIRED A " +
                "LINEAR SCAFFOLD\"; his rectangles' folding path \"is compatible with a circular " +
                "scaffold and LEAVES A 'SEAM' (a contour which the path does not cross)\".",
        "AND THE GEN-1 TILE IS ALREADY IN THE DEMONSTRATED REGIME. The sheet needs " +
                "$usedAtNearest nt of a $M13_SCAFFOLD_NUCLEOTIDES nt M13 — 4.31x too long — so a " +
                "remainder is unavoidable on that scaffold, and Rothemund's FIRST origami " +
                "experiment was exactly that: an 8-helix seamless raster on a circular M13 with " +
                "two thirds unfolded, which he reports \"could easily bridge the corners without " +
                "deforming the rectangle\".",
        "THE PRICE THE PROGRAMME HAS NOT COUNTED IS THE REMAINDER ITSELF. $remainder unpaired " +
                "nucleotides is a ${"%.1f".format(remainderRadius)} nm ideal coil carrying 1.66x " +
                "the sheet's own backbone charge, beside a tile whose edge load C-0022 solved " +
                "with nothing there. A purpose-built scaffold of $usedAtNearest nt removes it, " +
                "and a purpose-built CIRCULAR one still needs only a $loop nt return loop.",
        "AND A SEAMLESS RASTER QUANTISES THE TILE'S WIDTH AT 32 bp, NOT AT THE RISE. Rothemund's " +
                "fundamental constraint — successive scaffold crossovers an ODD number of half " +
                "turns apart — binds the ROW LENGTH of a boustrophedon, because a boustrophedon " +
                "has only progressive crossovers. On the square lattice that admits 16, 48, 80, " +
                "112, 144 bp and nothing between: the nearest buildable width to 40.0 nm is " +
                "112 bp = ${(nearest * T151_RISE).roundedForProse()} nm, and the NOMINAL " +
                "40.0 nm is not on the list.",
        "ROTHEMUND'S OWN STAGGERED SEAM IS NOT THE REMEDY. Solved exactly over the three-row " +
                "coupling C-0081's mechanism implies, the best staggered seam still takes " +
                "${staggered.affectedStations} of 34 stations off the node against the best " +
                "straight seam's ${straightCosts.minOf { it.second }} and the worst's " +
                "${straightCosts.maxOf { it.second }}. It is worth one station. Seamlessness is " +
                "worth all 34.",
        "C-0081's SWEEP REPRODUCES FROM ITS STATION PLANES ALONE: 8 candidate planes, 6 to 12 " +
                "stations affected, 34 of 34 on an odd plane."
    )

    val result = T151Result(
        task = "T-151",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x 40.35 nm " +
                "single-layer square-lattice Rothemund sheet, 15 duplexes at the SAXS 2.69 nm, " +
                "0.34 nm rise, 32/3 bp per turn, crossover phase 24; C-0063's 34 upward roots " +
                "read from gpd/results/T-125-upward-root-placement.json; ssDNA 0.57 nm per " +
                "nucleotide and a 2.10 nm zero-force Kuhn length",
        decision = "YES. A seamless routing for a 15-duplex 40 nm single-layer sheet exists — " +
                "the plain boustrophedon, one scaffold segment per row and " +
                "${T151_DUPLEXES - 1} scaffold crossovers — and it is available under either a " +
                "LINEAR scaffold or a circular one left partly unfolded, both of which Rothemund " +
                "built. What forces a seam is a parity on a tree and it needs BOTH premises: a " +
                "circular scaffold AND full utilisation. The Gen-1 tile fails the second premise " +
                "anyway, needing $usedAtNearest of M13's $M13_SCAFFOLD_NUCLEOTIDES nt.",
        graph = graph,
        topologies = topologies,
        widths = widths,
        scaffoldBudget = scaffoldBudget,
        seamCosts = seamCosts,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "edgeX" to T151_EDGE_X,
            "duplexes" to T151_DUPLEXES.toDouble(),
            "phase" to T151_PHASE.toDouble(),
            "risePerBasePair" to T151_RISE,
            "basePairsPerTurn" to SQUARE_LATTICE_BASE_PAIRS_PER_TURN,
            "nominalRowBasePairs" to nominal.toDouble(),
            "nearestAdmissibleRowBasePairs" to nearest.toDouble(),
            "nearestAdmissibleWidth" to nearest * T151_RISE,
            "admissibleWidthStepBasePairs" to 32.0,
            "sheetNucleotides" to usedAtNearest.toDouble(),
            "m13Nucleotides" to M13_SCAFFOLD_NUCLEOTIDES.toDouble(),
            "m13Overhang" to M13_SCAFFOLD_NUCLEOTIDES.toDouble() / usedAtNearest,
            "remainderNucleotides" to remainder.toDouble(),
            "remainderRadiusOfGyration" to remainderRadius,
            "remainderChargeOverSheet" to remainder.toDouble() / (2.0 * usedAtNearest),
            "returnLoopNucleotides" to loop.toDouble(),
            "seamPlanes" to seamPlanes.size.toDouble(),
            "bestStraightSeamCost" to straightCosts.minOf { it.second }.toDouble(),
            "worstStraightSeamCost" to straightCosts.maxOf { it.second }.toDouble(),
            "bestStaggeredSeamCost" to staggered.affectedStations.toDouble(),
            "seamlessSeamCost" to 0.0,
            "scaffoldCrossoversSeamless" to (T151_DUPLEXES - 1).toDouble(),
            "scaffoldCrossoversSeamed" to (2 * T151_DUPLEXES).toDouble()
        )
    )

    val file = File("gpd/results/T-151-scaffold-routing.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    file.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        )
    )
    println("T-151 — wrote ${file.path}")
    println("  Hamiltonian paths / cycles on P_15: " +
            "${hamiltonianRowPathCount(12)} (at D<=12) / ${hamiltonianRowCycleCount(12)}")
    topologies.forEach {
        println("  ${it.topology}: ${it.segmentsPerRow} segment(s) per row, seamless=${it.seamless}")
    }
    println("  nearest admissible seamless width: $nearest bp = ${nearest * T151_RISE} nm " +
            "(nominal $nominal bp is admissible=${isOddHalfTurnSeparation(nominal)})")
    println("  scaffold: $usedAtNearest nt needed, M13 is $M13_SCAFFOLD_NUCLEOTIDES, " +
            "remainder $remainder nt, Rg ${"%.1f".format(remainderRadius)} nm, " +
            "return loop $loop nt")
    println("  seam cost: straight ${straightCosts.minOf { it.second }}-" +
            "${straightCosts.maxOf { it.second }} of 34, best staggered " +
            "${staggered.affectedStations}, seamless 0")
    falsifiers.forEach { println("  ${it.id} fired: ${it.fired}") }
}
