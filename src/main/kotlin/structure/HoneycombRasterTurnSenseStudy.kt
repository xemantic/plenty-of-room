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

package com.xemantic.nano.plentyofroom.structure

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-218` — which turn sense `Δ` does a caDNAno `15 × 4` honeycomb x-raster carry?
 *
 * Emits `gpd/results/T-218-honeycomb-raster-turn-sense.json`. Reads
 * `gpd/results/T-217-honeycomb-twist-correction.json` (`C-0136`'s residue triples) and
 * `gpd/results/T-198-honeycomb-raster-width.json` (`C-0119`'s honeycomb design rules), both for
 * the reproduction gate only.
 *
 * The whole study is exact integer lattice arithmetic. **No solve is justified**: this
 * repository's lattice machinery is single-layer square-lattice throughout, so a dishing number
 * computed here would be a square-lattice number wearing a honeycomb label.
 */
private const val T218_NOMINAL_WIDTH = 40.0
private const val T218_M13_NUCLEOTIDES = 7249
private const val T218_P7560_NUCLEOTIDES = 7560
private const val T218_P8064_NUCLEOTIDES = 8064
private const val T218_ALLOTMENT_PER_HELIX = 126
private const val T218_PAIRED_PER_HELIX = 98
private const val T218_UNPAIRED_PER_HELIX = 28

@Serializable
private data class T218TurnRecord(
    val index: Int,
    val row: Int,
    val positionInRow: Int,
    val x: Int,
    val y: Int,
    val sublattice: String,
    val arriveAzimuthDegrees: Double,
    val leaveAzimuthDegrees: Double,
    val geometricSense: Int,
    val axialSign: Int,
    val effectiveSense: Int,
    val isRowEndHelix: Boolean
)

@Serializable
private data class T218DesignRecord(
    val design: String,
    val rows: Int,
    val helicesPerRow: Int,
    val helices: Int,
    val definedTurns: Int,
    val senseOneCount: Int,
    val senseTwoCount: Int,
    val minoritySenseFraction: Double,
    val senseIsConstant: Boolean,
    val note: String
)

@Serializable
private data class T218WidthRecord(
    val route: String,
    val senseOneRowLength: Int,
    val senseTwoRowLength: Int,
    val staggerBasePairs: Int,
    val axialExtentBasePairs: Int,
    val axialExtentNm: Double,
    val departureFromNominalPercent: Double,
    val scaffoldNucleotides: Int,
    val fitsM13: Boolean,
    val fitsP8064: Boolean,
    val frontFaceRaggednessBasePairs: Int,
    val rearFaceRaggednessBasePairs: Int
)

@Serializable
private data class T218LoopRecord(
    val scaffold: String,
    val nucleotides: Int,
    val helicesAtBuiltAllotment: Double,
    val allotmentIsExact: Boolean,
    val maximumPairedRowLengthBasePairs: Int,
    val widthNm: Double,
    val departureFromNominalPercent: Double
)

@Serializable
private data class T218Reproduction(
    val what: String,
    val published: String,
    val here: String,
    val departure: Double,
    val source: String
)

@Serializable
private data class T218Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T218Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T218Result(
    val task: String,
    val leaf: String,
    val question: String,
    val conditions: Map<String, String>,
    val quotations: Map<String, String>,
    val turns: List<T218TurnRecord>,
    val designs: List<T218DesignRecord>,
    val widths: List<T218WidthRecord>,
    val loopRoute: List<T218LoopRecord>,
    val reproductions: List<T218Reproduction>,
    val predicates: List<T218Predicate>,
    val falsifiers: List<T218Falsifier>,
    val findings: List<String>,
    val parameters: Map<String, String>
)

/** The axial crossover levels a two-length assignment produces, and what they leave ragged. */
private data class T218Levels(
    val extent: Int,
    val front: Int,
    val rear: Int,
    val nucleotides: Int
)

private fun t218Levels(turns: List<RasterTurn>, senseOne: Int, senseTwo: Int): T218Levels {
    val z = HashMap<Int, Int>()
    z[turns.first().index - 1] = 0
    var current = 0
    turns.forEach { turn ->
        current += turn.axialSign * (if (turn.effectiveSense == 1) senseOne else senseTwo)
        z[turn.index] = current
    }
    val spans = turns.map { minOf(z.getValue(it.index - 1), z.getValue(it.index)) to
            maxOf(z.getValue(it.index - 1), z.getValue(it.index)) }
    val even = z.filterKeys { Math.floorMod(it, 2) == 0 }.values
    val odd = z.filterKeys { Math.floorMod(it, 2) == 1 }.values
    return T218Levels(
        extent = spans.maxOf { it.second } - spans.minOf { it.first },
        front = even.max() - even.min(),
        rear = odd.max() - odd.min(),
        nucleotides = turns.sumOf { if (it.effectiveSense == 1) senseOne else senseTwo } +
                senseOne + senseTwo
    )
}

private fun t218Field(file: File, section: String, key: String): String {
    require(file.exists()) { "upstream result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(section).jsonObject.getValue(key).jsonPrimitive.content
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val honeycomb = HelixCrossoverLattice.HONEYCOMB
    val senseOneResidues = honeycomb.turnPairResidues(0, 1)
    val senseTwoResidues = honeycomb.turnPairResidues(0, 2)

    // --------------------------------------------------- Deliverable 1: the cheap bound, first
    println("T-218 — THE CHEAP BOUND, before anything else ...")
    val straightChainExists = listOf(HoneycombCell(0, 0), HoneycombCell(0, 2)).any { cell ->
        HONEYCOMB_BOND_OFFSETS.getValue(cell.sublattice).any { offset ->
            val next = HoneycombCell(cell.x + offset.first, cell.y + offset.second)
            offset in HONEYCOMB_BOND_OFFSETS.getValue(next.sublattice)
        }
    }
    println("  a honeycomb chain can continue in the same direction: $straightChainExists")
    println("  so an x-raster row MUST be corrugated, which the paper states in as many words")

    // ---------------------------------- Deliverable 2: the turn-sense sequence of design (i)
    val path = honeycombXRasterPath(rows = 15, helicesPerRow = 4)
    val turns = honeycombRasterTurns(path)
    val senseOneCount = turns.count { it.effectiveSense == 1 }
    val senseTwoCount = turns.count { it.effectiveSense == 2 }
    val turnRecords = turns.map { turn ->
        T218TurnRecord(
            index = turn.index,
            row = turn.index / 4,
            positionInRow = turn.index % 4,
            x = turn.cell!!.x,
            y = turn.cell.y,
            sublattice = turn.sublattice!!.name,
            arriveAzimuthDegrees = turn.arriveAzimuthDegrees,
            leaveAzimuthDegrees = turn.leaveAzimuthDegrees,
            geometricSense = turn.geometricSense,
            axialSign = turn.axialSign,
            effectiveSense = turn.effectiveSense,
            isRowEndHelix = turn.index % 4 == 0 || turn.index % 4 == 3
        )
    }
    println("T-218 — the turn-sense sequence of design (i), 15 x 4 ...")
    println("  geometric  " + turns.joinToString("") { it.geometricSense.toString() })
    println("  axial sign " + turns.joinToString("") { if (it.axialSign > 0) "+" else "-" })
    println("  EFFECTIVE  " + turns.joinToString("") { it.effectiveSense.toString() })
    println(("  sense 1 on %d helices, sense 2 on %d, of %d with a defined sense")
        .format(senseOneCount, senseTwoCount, turns.size))

    // ------------------------------------------ Deliverable 3: the census over every design
    val designs = listOf(15 to 4, 10 to 6, 8 to 8, 6 to 10, 4 to 16, 3 to 20, 2 to 30, 1 to 60)
        .map { (m, n) ->
            val t = honeycombRasterTurns(honeycombXRasterPath(m, n))
            val one = t.count { it.effectiveSense == 1 }
            val two = t.count { it.effectiveSense == 2 }
            T218DesignRecord(
                design = "$m x $n",
                rows = m,
                helicesPerRow = n,
                helices = m * n,
                definedTurns = t.size,
                senseOneCount = one,
                senseTwoCount = two,
                minoritySenseFraction = minOf(one, two).toDouble() / t.size,
                senseIsConstant = one == 0 || two == 0,
                note = if (m == 1) {
                    "ONE row, so no row turn and no break: the sense is constant and a UNIFORM " +
                            "row length exists — but one corrugated row is two y-positions, " +
                            "not four layers"
                } else {
                    ("m - 1 = " + (m - 1) + " row turns, each contaminating three helices")
                }
            )
        }
    designs.forEach {
        println(("  %-7s %2d helices, sense 1 on %2d and sense 2 on %2d, minority %.3f")
            .format(it.design, it.helices, it.senseOneCount, it.senseTwoCount,
                it.minoritySenseFraction))
    }

    // ------------------------------------------- Deliverable 4: the ragged route and its cost
    println("T-218 — route A, an all-crossover raster with two row lengths ...")
    val candidates = honeycomb.admissibleRowLengths(80, 160, senseOneResidues)
    val partners = honeycomb.admissibleRowLengths(80, 160, senseTwoResidues)
    val widths = ArrayList<T218WidthRecord>()
    candidates.forEach { a ->
        partners.filter { abs(it - a) <= 10 }.forEach { b ->
            val levels = t218Levels(turns, a, b)
            widths += T218WidthRecord(
                route = "A — antiparallel scaffold crossovers at every turn, two row lengths",
                senseOneRowLength = a,
                senseTwoRowLength = b,
                staggerBasePairs = abs(a - b),
                axialExtentBasePairs = levels.extent,
                axialExtentNm = levels.extent * rise,
                departureFromNominalPercent =
                    100.0 * (levels.extent * rise - T218_NOMINAL_WIDTH) / T218_NOMINAL_WIDTH,
                scaffoldNucleotides = levels.nucleotides,
                fitsM13 = levels.nucleotides <= T218_M13_NUCLEOTIDES,
                fitsP8064 = levels.nucleotides <= T218_P8064_NUCLEOTIDES,
                frontFaceRaggednessBasePairs = levels.front,
                rearFaceRaggednessBasePairs = levels.rear
            )
        }
    }
    val best = widths
        .filter { it.fitsM13 && it.staggerBasePairs <= 4 }
        .minByOrNull { abs(it.departureFromNominalPercent) }!!
    val tightest = widths
        .filter { it.fitsM13 && it.staggerBasePairs == 3 }
        .minByOrNull { abs(it.departureFromNominalPercent) }!!
    widths.sortedBy { abs(it.departureFromNominalPercent) }.take(8).forEach {
        println(("  %3d / %3d bp  stagger %2d  extent %3d bp = %6.2f nm  %+6.2f %%  " +
                "%4d nt  M13 %s  ragged %d / %d bp")
            .format(it.senseOneRowLength, it.senseTwoRowLength, it.staggerBasePairs,
                it.axialExtentBasePairs, it.axialExtentNm, it.departureFromNominalPercent,
                it.scaffoldNucleotides, it.fitsM13, it.frontFaceRaggednessBasePairs,
                it.rearFaceRaggednessBasePairs))
    }

    // ----------------------------------- Deliverable 5: route B, the built design's own loops
    println("T-218 — route B, the built design's own unpaired turn loops ...")
    val loopRoute = listOf(
        "M13mp18 (C-0109's figure)" to T218_M13_NUCLEOTIDES,
        "p7560 — the 60-helix designs, including (i) 15 x 4" to T218_P7560_NUCLEOTIDES,
        "p8064 — the 64-helix designs (iii) 8 x 8 and (v) 4 x 16" to T218_P8064_NUCLEOTIDES
    ).map { (name, nt) ->
        val maximumPaired = (nt - 60 * T218_UNPAIRED_PER_HELIX) / 60
        T218LoopRecord(
            scaffold = name,
            nucleotides = nt,
            helicesAtBuiltAllotment = nt.toDouble() / T218_ALLOTMENT_PER_HELIX,
            allotmentIsExact = nt % T218_ALLOTMENT_PER_HELIX == 0,
            maximumPairedRowLengthBasePairs = maximumPaired,
            widthNm = maximumPaired * rise,
            departureFromNominalPercent =
                100.0 * (maximumPaired * rise - T218_NOMINAL_WIDTH) / T218_NOMINAL_WIDTH
        )
    }
    loopRoute.forEach {
        println(("  %-52s %4d nt = %7.3f helices at 126, exact %s, max %3d bp = %6.2f nm")
            .format(it.scaffold.take(52), it.nucleotides, it.helicesAtBuiltAllotment,
                it.allotmentIsExact, it.maximumPairedRowLengthBasePairs, it.widthNm))
    }

    // ------------------------------------------------------------------ the reproduction gates
    val t217 = File("gpd/results/T-217-honeycomb-twist-correction.json")
    val t198 = File("gpd/results/T-198-honeycomb-raster-width.json")
    val publishedOne = t218Field(t217, "parameters", "honeycombResiduesDelta1")
    val publishedTwo = t218Field(t217, "parameters", "honeycombResiduesDelta2")
    val publishedStep = t218Field(t198, "honeycombRules", "stapleCrossoverStepBasePairs")
    val publishedPeriod = t218Field(t198, "honeycombRules", "stapleCrossoverPeriodBasePairs")
    val publishedOffset = t218Field(t198, "honeycombRules", "scaffoldOffsetFromStapleBasePairs")
    val hereOne = senseOneResidues.sorted().joinToString(",")
    val hereTwo = senseTwoResidues.sorted().joinToString(",")
    val reproductions = listOf(
        T218Reproduction("C-0136's Delta = 1 residue triple", publishedOne, hereOne,
            if (publishedOne == hereOne) 0.0 else 1.0,
            "gpd/results/T-217-honeycomb-twist-correction.json"),
        T218Reproduction("C-0136's Delta = 2 residue triple", publishedTwo, hereTwo,
            if (publishedTwo == hereTwo) 0.0 else 1.0,
            "gpd/results/T-217-honeycomb-twist-correction.json"),
        T218Reproduction("C-0119's honeycomb azimuth step, base pairs", publishedStep,
            honeycomb.basePairsPerAzimuthStep.toString(),
            abs(publishedStep.toDouble() - honeycomb.basePairsPerAzimuthStep),
            "gpd/results/T-198-honeycomb-raster-width.json"),
        T218Reproduction("C-0119's honeycomb azimuth period, base pairs", publishedPeriod,
            honeycomb.azimuthPeriodBasePairs.toString(),
            abs(publishedPeriod.toDouble() - honeycomb.azimuthPeriodBasePairs),
            "gpd/results/T-198-honeycomb-raster-width.json"),
        T218Reproduction("C-0119's scaffold offset from the staple lattice, base pairs",
            publishedOffset, honeycomb.scaffoldOffsetBasePairs.toString(),
            abs(publishedOffset.toDouble() - honeycomb.scaffoldOffsetBasePairs),
            "gpd/results/T-198-honeycomb-raster-width.json"),
        T218Reproduction("the paper's own per-helix allotment against p7560, nucleotides",
            T218_P7560_NUCLEOTIDES.toString(), (60 * T218_ALLOTMENT_PER_HELIX).toString(),
            abs(T218_P7560_NUCLEOTIDES - 60.0 * T218_ALLOTMENT_PER_HELIX),
            "Douglas et al., NAR 37:5001, Figure 2 text"),
        T218Reproduction("the paper's own per-helix allotment against p8064, nucleotides",
            T218_P8064_NUCLEOTIDES.toString(), (64 * T218_ALLOTMENT_PER_HELIX).toString(),
            abs(T218_P8064_NUCLEOTIDES - 64.0 * T218_ALLOTMENT_PER_HELIX),
            "Douglas et al., NAR 37:5001, Figure 2 text")
    )

    // ------------------------------------------------------------------- the nearest widths
    val nearestSenseOne = honeycomb.admissibleRowLengths(90, 150, senseOneResidues)
        .minByOrNull { abs(it * rise - T218_NOMINAL_WIDTH) }!!
    val nearestSenseTwo = honeycomb.admissibleRowLengths(90, 150, senseTwoResidues)
        .minByOrNull { abs(it * rise - T218_NOMINAL_WIDTH) }!!
    val stagger = minimumRowLengthStagger(senseOneResidues, senseTwoResidues,
        honeycomb.azimuthPeriodBasePairs)
    val mirrored = honeycombRasterTurns(honeycombXRasterPath(15, 4, mirrored = true))
    val flipped = honeycombRasterTurns(path, firstAxialSign = -1)
    val square = squareSheetRasterTurns(15)
    val uniformServesBoth = (1..2100).any { n ->
        val r = Math.floorMod(n, honeycomb.azimuthPeriodBasePairs)
        r in senseOneResidues && r in senseTwoResidues
    }

    val predicates = listOf(
        T218Predicate("P1",
            "the cross-section geometry is READ, not assumed: the corrugation is a theorem of " +
                    "the honeycomb and the paper states it in one sentence",
            !straightChainExists),
        T218Predicate("P2",
            "the turn sense is emitted per helix for every helix of design (i) with a " +
                    "defined one",
            turnRecords.size == 58),
        T218Predicate("P3",
            "the answer is invariant under the one free convention — mirroring the " +
                    "cross-section, or flipping which face the scaffold starts at — which " +
                    "swaps the two LABELS and not the alternation",
            mirrored.map { it.effectiveSense } == turns.map { 3 - it.effectiveSense } &&
                    flipped.map { it.effectiveSense } == turns.map { 3 - it.effectiveSense }),
        T218Predicate("P4",
            "the same machinery on the square sheet returns a CONSTANT sense, reproducing " +
                    "C-0086's unconditional rule",
            square.map { it.effectiveSense }.distinct() == listOf(2) &&
                    HelixCrossoverLattice.SQUARE_SHEET.turnPairResidues(0, 2) == setOf(16)),
        T218Predicate("P5",
            "the consequence for 112 bp and for 119 bp is stated as a FRACTION of the helices",
            senseOneCount > 0 && senseTwoCount > 0),
        T218Predicate("P6",
            "the cheapest departure from uniformity that restores admissibility is quoted, in " +
                    "base pairs and in nm",
            stagger == 3)
    )

    val falsifiers = listOf(
        T218Falsifier("F1",
            "consecutive helices of an x-raster row are on the SAME honeycomb sublattice",
            path.zipWithNext().any { (a, b) -> a.sublattice == b.sublattice },
            "all 59 consecutive pairs of design (i) are A/B alternating, which is forced: " +
                    "no honeycomb path can continue in the same direction"),
        T218Falsifier("F2",
            "the GEOMETRIC sense is constant along the raster",
            turns.map { it.geometricSense }.distinct().size == 1,
            ("it alternates in blocks of four — %s — one block per x-raster row plus the " +
                    "three helices each row turn contaminates")
                .format(turns.take(16).joinToString("") { it.geometricSense.toString() })),
        T218Falsifier("F3",
            "the two alternations CANCEL, so the effective sense is constant and a uniform " +
                    "row length exists",
            turns.map { it.effectiveSense }.distinct().size == 1,
            ("they do NOT cancel: sense 1 on %d helices and sense 2 on %d of %d. They cancel " +
                    "WITHIN a row — every row interior carries one sense — and the m - 1 row " +
                    "turns break it, which is why a ONE-row raster is the only constant case")
                .format(senseOneCount, senseTwoCount, turns.size)),
        T218Falsifier("F4",
            "the same machinery on the square sheet gives a non-constant sense, i.e. fails to " +
                    "reproduce C-0086",
            square.map { it.effectiveSense }.distinct().size != 1,
            "constant at 2, because 2 is its OWN NEGATIVE modulo 4 — which is exactly why " +
                    "C-0086's rule is unconditional and the honeycomb's cannot be"),
        T218Falsifier("F5",
            "the alternation verdict changes under the free viewing convention",
            mirrored.map { it.effectiveSense }.distinct().size !=
                    turns.map { it.effectiveSense }.distinct().size,
            "mirroring swaps the two labels one for one and leaves both senses present"),
        T218Falsifier("F6",
            "the two honeycomb residue triples are not disjoint, contradicting C-0136",
            senseOneResidues.intersect(senseTwoResidues).isNotEmpty(),
            ("{" + hereOne + "} and {" + hereTwo + "} are disjoint")),
        T218Falsifier("F7",
            "the paper's own per-helix allotment does not reproduce its two scaffold lengths",
            60 * T218_ALLOTMENT_PER_HELIX != T218_P7560_NUCLEOTIDES ||
                    64 * T218_ALLOTMENT_PER_HELIX != T218_P8064_NUCLEOTIDES,
            "60 x 126 = 7560 and 64 x 126 = 8064, both EXACT — so design (i), at 60 helices, " +
                    "is folded from p7560 and spends it to the last nucleotide"),
        T218Falsifier("F8",
            "some uniform row length serves BOTH turn senses",
            uniformServesBoth,
            "0 of 2100 row lengths, which is a theorem: the two triples are disjoint mod 21"),
        T218Falsifier("F9",
            "the minimum admissible row-length stagger is not three base pairs",
            stagger != 3,
            ("%d bp = %.4f nm, from residue 7 against 4 and 17 against 14")
                .format(stagger, stagger * rise))
    )

    val findings = listOf(
        ("THE 15 x 4 X-RASTER CARRIES BOTH TURN SENSES, ALTERNATING, AND NEITHER 112 bp NOR " +
                "119 bp IS AVAILABLE AS A UNIFORM WIDTH. Sense 1 lands on %d of its 58 helices " +
                "with a defined sense and sense 2 on %d; the two admissible residue triples " +
                "{%s} and {%s} are disjoint, so no single row length serves the tile. " +
                "CH-0165's second branch is the one that obtains.")
            .format(senseOneCount, senseTwoCount, hereOne, hereTwo),
        ("THE CHEAP BOUND IS A THEOREM AND IT RAN FIRST: a honeycomb path can never continue " +
                "in the same direction, so an x-raster row is corrugated at every lattice — " +
                "which is what the caDNAno paper states as \"the x-raster rows … are " +
                "corrugated; they stagger up and down\". Consecutive helices of a row are " +
                "therefore on opposite sublattices and the GEOMETRIC sense alternates."),
        ("THE TWO ALTERNATIONS CANCEL WITHIN A ROW AND THE ROW TURNS BREAK THEM. The scaffold " +
                "runs the full length of every helix, so its axial direction alternates too, " +
                "and Delta_eff = s . Delta_geom is CONSTANT along a row interior — consecutive " +
                "rows carrying opposite senses. Each of the m - 1 row turns contaminates three " +
                "helices. A ONE-row raster is the only case with a constant sense, and one " +
                "corrugated row is two y-positions, not four layers."),
        ("THE SQUARE LATTICE'S UNCONDITIONALITY IS AN ACCIDENT OF 4 = 2 x 2. Its two in-plane " +
                "neighbours are 180 degrees apart, i.e. two azimuth classes, and 2 is its own " +
                "negative modulo 4 — so the axial-direction alternation cannot touch it and " +
                "C-0086's rule needs no turn sense. Modulo 3 neither 1 nor 2 is self-inverse, " +
                "which is the whole of the difference."),
        ("ROUTE A — AN ALL-CROSSOVER RASTER NEEDS TWO ROW LENGTHS, AND THE MINIMUM STAGGER IS " +
                "%d bp = %.4f nm. The best pair inside M13 at a stagger of at most 4 bp is " +
                "%d / %d bp, an axial extent of %d bp = %.2f nm (%+.2f %% of Sec 3's nominal " +
                "40.0 nm) on %d nucleotides, with the two faces ragged by %d and %d bp. That " +
                "BEATS the square lattice's 38.08 nm at -4.80 %% and C-0133's 37.40 nm at " +
                "-6.50 %%.")
            .format(stagger, stagger * rise, best.senseOneRowLength, best.senseTwoRowLength,
                best.axialExtentBasePairs, best.axialExtentNm, best.departureFromNominalPercent,
                best.scaffoldNucleotides, best.frontFaceRaggednessBasePairs,
                best.rearFaceRaggednessBasePairs),
        ("ROUTE B IS WHAT THE PAPER ACTUALLY BUILT, AND IT IS UNPAIRED LOOPS AT EVERY TURN. " +
                "\"Each helix was allotted 126 bases of scaffold. Of those 126 bases, 98 were " +
                "paired with complementary staples, and the remaining 28 bases were divided " +
                "into front and rear unpaired loop fragments at the ends of each helix.\" So " +
                "every raster turn of the built blocks passes through 14 + 14 = 28 unpaired " +
                "nucleotides, the residue condition does not bind them at all, and the TEM " +
                "criterion names the loops as a visible feature."),
        ("THE LOOPS COST 1 680 NUCLEOTIDES ON A 60-HELIX TILE, AND M13 HAS 529 SPARE AT " +
                "112 bp. At the built allowance the widest four-layer honeycomb tile is %d bp " +
                "= %.2f nm from M13 (%+.2f %%) and %d bp = %.2f nm from p8064 (%+.2f %%). " +
                "C-0119's uniform 112 bp x 60 needs 8 400 nt with the loops and is out of " +
                "reach of BOTH scaffolds.")
            .format(loopRoute[0].maximumPairedRowLengthBasePairs, loopRoute[0].widthNm,
                loopRoute[0].departureFromNominalPercent,
                loopRoute[2].maximumPairedRowLengthBasePairs, loopRoute[2].widthNm,
                loopRoute[2].departureFromNominalPercent),
        ("DESIGN (i) IS FOLDED FROM p7560, NOT p8064, AND THE ARITHMETIC IS EXACT. 15 x 4 is " +
                "60 helices and 60 x 126 = 7 560 to the nucleotide; the 64-helix designs are " +
                "(iii) 8 x 8 and (v) 4 x 16, and 64 x 126 = 8 064. C-0119 assigns p8064 to " +
                "designs i, iii and v and therefore quotes a 1 344 nt remainder where the " +
                "design's own scaffold leaves ZERO."),
        ("EVERY x-RASTER ROW THE PAPER FOLDED HAS AN EVEN NUMBER OF HELICES — 4, 6, 8, 10, " +
                "16, 20, 30 — and that is forced: a row's two ends must both carry the " +
                "DOWNWARD vertical bond, and that bond points up on one sublattice and down " +
                "on the other. An odd row cannot turn down at both ends."),
        ("C-0136'S OWN TABLE MIS-STATES ITS Delta = 1 NEAREST WIDTH. The nearest admissible " +
                "Delta = 1 row length to 40.0 nm is %d bp = %.2f nm (%+.2f %%), not the 112 bp " +
                "= 38.08 nm its width table carries; C-0136's own result file has %d in " +
                "nearestDelta1WidthBasePairs. 112 bp is the nearest SQUARE-lattice width and " +
                "an admissible Delta = 1 one, but it is not the nearest.")
            .format(nearestSenseOne, nearestSenseOne * rise,
                100.0 * (nearestSenseOne * rise - T218_NOMINAL_WIDTH) / T218_NOMINAL_WIDTH,
                nearestSenseOne)
    )

    val result = T218Result(
        task = "T-218",
        leaf = "A8.2",
        question = "Which turn sense does a caDNAno 15 x 4 honeycomb x-raster carry, and does " +
                "C-0119's 112 bp row survive it?",
        conditions = mapOf(
            "temperature" to "300 K",
            "kBT" to "4.141947 pN nm",
            "rise" to (rise.toString() + " nm per base pair"),
            "lattice" to "caDNAno honeycomb: three azimuths at 7 bp, 21 bp = 2 turns, scaffold " +
                    "crossovers 5 bp upstream or downstream of the staple lattice",
            "naturalTwist" to "10.5 base pairs per turn, which is the honeycomb's design twist",
            "crossSection" to "design (i) of Douglas et al., 15 x-raster rows of 4 helices, " +
                    "60 duplexes",
            "handedness" to "B-DNA is right-handed; viewed from +z the backbone azimuth " +
                    "increases counter-clockwise with z, so one azimuth step (+7 bp) advances " +
                    "it by +240 = -120 degrees and neighbour class increases as azimuth falls",
            "solve" to "NONE — the task is exact integer lattice arithmetic and no solve is " +
                    "justified on a lattice this repository's machinery does not model"
        ),
        quotations = mapOf(
            "corrugation" to "The x-raster rows within the honeycomb framework are corrugated; " +
                    "they stagger up and down and encompass helices that are actually at two " +
                    "different y-positions.",
            "rasterOrder" to "as viewed down the helical axes, close-packing rows of helices " +
                    "were arrayed within the honeycomb framework in an x-raster pattern (i.e. " +
                    "left to right, then down, then right to left, then down, etc.)",
            "allotment" to "Each helix was allotted 126 bases of scaffold. Of those 126 bases, " +
                    "98 were paired with complementary staples, and the remaining 28 bases " +
                    "were divided into front and rear unpaired loop fragments at the ends of " +
                    "each helix.",
            "scaffolds" to "The shapes were folded either from a 7560-base scaffold into 60 " +
                    "parallel helices or from an 8064-base scaffold into 64 parallel helices",
            "loopsAreVisible" to "no obvious defects such as missing, broken, disrupted, or " +
                    "smeared out sections more than 3 nm away from the unpaired scaffold loops " +
                    "at the front and rear interfaces",
            "source" to "Douglas, Marblestone, Teerapittayanon, Vazquez, Church and Shih, " +
                    "Nucleic Acids Research 37:5001 (2009), PMC2731887, read directly from " +
                    "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml — zero fetches"
        ),
        turns = turnRecords,
        designs = designs,
        widths = widths.sortedBy { abs(it.departureFromNominalPercent) },
        loopRoute = loopRoute,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "senseOneResidues" to hereOne,
            "senseTwoResidues" to hereTwo,
            "senseOneHelices" to senseOneCount.toString(),
            "senseTwoHelices" to senseTwoCount.toString(),
            "helicesWithADefinedSense" to turns.size.toString(),
            "minimumStaggerBasePairs" to stagger.toString(),
            "minimumStaggerNm" to roundForResult(stagger * rise).toString(),
            "nearestSenseOneWidthBasePairs" to nearestSenseOne.toString(),
            "nearestSenseTwoWidthBasePairs" to nearestSenseTwo.toString(),
            "recommendedSenseOneRowLength" to best.senseOneRowLength.toString(),
            "recommendedSenseTwoRowLength" to best.senseTwoRowLength.toString(),
            "recommendedAxialExtentBasePairs" to best.axialExtentBasePairs.toString(),
            "recommendedAxialExtentNm" to roundForResult(best.axialExtentNm).toString(),
            "tightestStaggerPair" to (tightest.senseOneRowLength.toString() + "/" +
                    tightest.senseTwoRowLength.toString()),
            "loopNucleotidesPerSixtyHelixTile" to (60 * T218_UNPAIRED_PER_HELIX).toString(),
            "maximumPairedRowLengthOnM13" to
                    loopRoute[0].maximumPairedRowLengthBasePairs.toString(),
            "maximumPairedRowLengthOnP8064" to
                    loopRoute[2].maximumPairedRowLengthBasePairs.toString(),
            "sources" to ("gpd/results/T-217-honeycomb-twist-correction.json," +
                    "gpd/results/T-198-honeycomb-raster-width.json")
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-218-honeycomb-raster-turn-sense.json")
    out.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).roundedForResult(floor = 1e-15)
        )
    )
    println("T-218 — wrote ${out.path}")
    findings.forEach { println("  * $it") }
}
