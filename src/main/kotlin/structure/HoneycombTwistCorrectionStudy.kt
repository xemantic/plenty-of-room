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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-217` — can the four-layer **honeycomb** tile be twist-corrected?
 *
 * Emits `gpd/results/T-217-honeycomb-twist-correction.json`. Reads
 * `gpd/results/T-198-honeycomb-raster-width.json` (`C-0119`'s honeycomb design rules, for the
 * reproduction gate).
 *
 * The whole study is exact integer and residue arithmetic over a period of 21 (honeycomb) or 32
 * (square). No solve is justified: this repository's lattice machinery is single-layer
 * square-lattice throughout (`CLAUDE.md`: *"`OrigamiGrillage` never reads `layers`"*), so a
 * dishing number computed on it would be a square-lattice number wearing a honeycomb label.
 *
 * See [HelixCrossoverLattice] for the model and [halfTurnBasePairs] for the one arithmetic this
 * task shares with `T-216`.
 */
private const val T217_NOMINAL_WIDTH = 40.0
private const val T217_C0104_THRESHOLD_DEGREES = 15.4497275
private const val T217_C0133_RESIDUAL_BASE_PAIRS = 0.25

@Serializable
private data class T217LatticeRecord(
    val name: String,
    val azimuthsPerHelix: Int,
    val basePairsPerAzimuthStep: Int,
    val azimuthPeriodBasePairs: Int,
    val turnsPerAzimuthPeriod: Int,
    val halfTurnsPerAzimuthPeriod: Int,
    val azimuthPeriodIsIntegralAtBDna: Boolean,
    val designTwistPerBase: Double,
    val basePairsPerTurn: Double,
    val mismatchPerBaseAgainstBDna: Double,
    val accumulatedOver112BasePairs: Double,
    val scaffoldOffsetBasePairs: Int,
    val scaffoldOffsetResidualBasePairs: Double,
    val scaffoldOffsetResidualDegrees: Double,
    val admissibleRowResidues: String,
    val residuesPerPeriod: Int,
    val basePairsPerAdmissibleWidth: Double
)

@Serializable
private data class T217HalfTurnRecord(
    val halfTurns: Int,
    val basePairs: Double,
    val distanceToNearestInteger: Double,
    val integral: Boolean,
    val residueClassModFour: Int,
    val role: String
)

@Serializable
private data class T217WidthRecord(
    val lattice: String,
    val turnSense: String,
    val basePairs: Int,
    val widthNm: Double,
    val departureFromNominalPercent: Double,
    val accumulatedTwistDegrees: Double,
    val isC0119Row: Boolean
)

@Serializable
private data class T217SensitivityRecord(
    val basePairsPerTurn: Double,
    val source: String,
    val mismatchPerBase: Double,
    val accumulatedOver112BasePairs: Double,
    val fractionOfSquareLatticeDriver: Double
)

@Serializable
private data class T217Reproduction(
    val what: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val source: String
)

@Serializable
private data class T217Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T217Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T217Result(
    val task: String,
    val leaf: String,
    val question: String,
    val conditions: Map<String, String>,
    val lattices: List<T217LatticeRecord>,
    val halfTurns: List<T217HalfTurnRecord>,
    val widths: List<T217WidthRecord>,
    val sensitivity: List<T217SensitivityRecord>,
    val reproductions: List<T217Reproduction>,
    val predicates: List<T217Predicate>,
    val falsifiers: List<T217Falsifier>,
    val findings: List<String>,
    val parameters: Map<String, String>
)

private fun t217Rule(file: File, key: String): Double {
    require(file.exists()) { "C-0119's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("honeycombRules").jsonObject
        .getValue(key).jsonPrimitive.content.toDouble()
}

private fun t217ScaffoldOffsetsOn112(file: File): List<Int> =
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("honeycombRules").jsonObject
        .getValue("scaffoldOffsetsOnA112bpRow").jsonArray
        .map { it.jsonPrimitive.content.toInt() }

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val naturalBasePairsPerTurn = 10.5
    val square = HelixCrossoverLattice.SQUARE_SHEET
    val honeycomb = HelixCrossoverLattice.HONEYCOMB

    // ------------------------------------------------ Deliverable 1: the cheap bound, one division
    println("T-217 — the cheap bound, BEFORE anything else ...")
    println("  honeycomb design twist  %.10f deg/bp".format(honeycomb.designTwistPerBase))
    println("  B-DNA (this repository) %.10f deg/bp".format(B_DNA_TWIST_PER_BASE))
    println("  mismatch                %.3e deg/bp".format(honeycomb.mismatchPerBase()))
    println("  square sheet mismatch   %+.6f deg/bp".format(square.mismatchPerBase()))

    val lattices = listOf(square, honeycomb).map { lattice ->
        val residues = lattice.admissibleRowResidues()
        T217LatticeRecord(
            name = lattice.name,
            azimuthsPerHelix = lattice.azimuthsPerHelix,
            basePairsPerAzimuthStep = lattice.basePairsPerAzimuthStep,
            azimuthPeriodBasePairs = lattice.azimuthPeriodBasePairs,
            turnsPerAzimuthPeriod = lattice.turnsPerAzimuthPeriod,
            halfTurnsPerAzimuthPeriod = lattice.halfTurnsPerAzimuthPeriod,
            azimuthPeriodIsIntegralAtBDna = distanceToNearestInteger(
                halfTurnBasePairs(lattice.halfTurnsPerAzimuthPeriod, naturalBasePairsPerTurn)
            ) < 1e-12,
            designTwistPerBase = lattice.designTwistPerBase,
            basePairsPerTurn = 360.0 / lattice.designTwistPerBase,
            mismatchPerBaseAgainstBDna = lattice.mismatchPerBase(),
            accumulatedOver112BasePairs = lattice.accumulatedMismatchDegrees(112),
            scaffoldOffsetBasePairs = lattice.scaffoldOffsetBasePairs,
            scaffoldOffsetResidualBasePairs =
                lattice.scaffoldOffsetResidualBasePairs(naturalBasePairsPerTurn),
            scaffoldOffsetResidualDegrees =
                lattice.scaffoldOffsetResidualDegrees(naturalBasePairsPerTurn),
            admissibleRowResidues = residues.sorted().joinToString(","),
            residuesPerPeriod = residues.size,
            basePairsPerAdmissibleWidth =
                lattice.azimuthPeriodBasePairs.toDouble() / residues.size
        )
    }

    // ------------------------------------------ Deliverable 2: the half-turn integrality theorem
    println("T-217 — the half-turn integrality theorem ...")
    var oddIntegral = 0
    var quadrupleNonIntegral = 0
    (1..2001).forEach { h ->
        val distance = distanceToNearestInteger(halfTurnBasePairs(h, naturalBasePairsPerTurn))
        if (h % 2 == 1 && distance < 1e-12) oddIntegral++
        if (h % 4 == 0 && distance > 1e-12) quadrupleNonIntegral++
    }
    val halfTurns = listOf(
        1 to "caDNAno's scaffold offset, \"five base pairs, or half a turn\"",
        2 to "one turn",
        3 to "the square lattice's own 1.5-turn domain",
        4 to "THE HONEYCOMB AZIMUTH PERIOD — 21 bp, two turns",
        6 to "the square lattice's 32 bp azimuth period, three turns",
        21 to "C-0133's construction — 21 half turns of B-DNA, whose nearest integer is 110"
    ).map { (h, role) ->
        val basePairs = halfTurnBasePairs(h, naturalBasePairsPerTurn)
        T217HalfTurnRecord(
            halfTurns = h,
            basePairs = basePairs,
            distanceToNearestInteger = distanceToNearestInteger(basePairs),
            integral = distanceToNearestInteger(basePairs) < 1e-12,
            residueClassModFour = h % 4,
            role = role
        )
    }
    halfTurns.forEach {
        println("  %2d half turns = %8.3f bp  distance %.4f  %s".format(
            it.halfTurns, it.basePairs, it.distanceToNearestInteger, it.role))
    }

    // -------------------------------------------- Deliverable 3: the admissible widths, derived
    println("T-217 — the admissible row lengths ...")
    val senses = listOf(
        "Delta = 1 (the raster turns to the NEXT neighbour class)" to
                honeycomb.turnPairResidues(0, 1),
        "Delta = 2 (the raster turns to the PREVIOUS neighbour class)" to
                honeycomb.turnPairResidues(0, 2)
    )
    val widths = ArrayList<T217WidthRecord>()
    senses.forEach { (sense, residues) ->
        honeycomb.admissibleRowLengths(90, 150, residues).forEach { n ->
            widths += T217WidthRecord(
                lattice = honeycomb.name,
                turnSense = sense,
                basePairs = n,
                widthNm = n * rise,
                departureFromNominalPercent =
                    100.0 * (n * rise - T217_NOMINAL_WIDTH) / T217_NOMINAL_WIDTH,
                accumulatedTwistDegrees = honeycomb.accumulatedMismatchDegrees(n),
                isC0119Row = n == 112
            )
        }
    }
    square.admissibleRowLengths(90, 150).forEach { n ->
        widths += T217WidthRecord(
            lattice = square.name,
            turnSense = "the only sense a two-neighbour lattice has",
            basePairs = n,
            widthNm = n * rise,
            departureFromNominalPercent =
                100.0 * (n * rise - T217_NOMINAL_WIDTH) / T217_NOMINAL_WIDTH,
            accumulatedTwistDegrees = square.accumulatedMismatchDegrees(n),
            isC0119Row = n == 112
        )
    }
    widths.sortedBy { abs(it.departureFromNominalPercent) }.take(8).forEach {
        println("  %-58s %3d bp  %6.2f nm  %+6.2f %%".format(
            it.turnSense.take(58), it.basePairs, it.widthNm, it.departureFromNominalPercent))
    }
    val nearestUnion = widths
        .filter { it.lattice == honeycomb.name }
        .minByOrNull { abs(it.departureFromNominalPercent) }!!
    val nearestDelta1 = widths
        .filter { it.turnSense.startsWith("Delta = 1") }
        .minByOrNull { abs(it.departureFromNominalPercent) }!!
    val nearestSquare = widths
        .filter { it.lattice == square.name }
        .minByOrNull { abs(it.departureFromNominalPercent) }!!

    // ------------------------------------------------------- Deliverable 4: the sensitivity
    println("T-217 — the sensitivity to the one constant this rests on ...")
    val squareDriver = abs(square.accumulatedMismatchDegrees(112))
    val sensitivity = listOf(
        10.34 to "the twist at which the honeycomb's own driver equals the square sheet's",
        10.44 to "the low end of the solution B-DNA band in circulation",
        10.45 to "a 0.5 % departure from the honeycomb's design constant",
        10.5 to "THE LOCKED VALUE — the honeycomb's design twist and B-DNA's, one number",
        10.55 to "a 0.5 % departure the other way",
        10.6667 to "the SQUARE lattice's design twist, for scale"
    ).map { (bp, source) ->
        val natural = 360.0 / bp
        T217SensitivityRecord(
            basePairsPerTurn = bp,
            source = source,
            mismatchPerBase = honeycomb.mismatchPerBase(natural),
            accumulatedOver112BasePairs = honeycomb.accumulatedMismatchDegrees(112, natural),
            fractionOfSquareLatticeDriver =
                abs(honeycomb.accumulatedMismatchDegrees(112, natural)) / squareDriver
        )
    }
    sensitivity.forEach {
        println("  %7.4f bp/turn  %+9.5f deg/bp  %+8.3f deg over 112 bp  %.3f of square".format(
            it.basePairsPerTurn, it.mismatchPerBase, it.accumulatedOver112BasePairs,
            it.fractionOfSquareLatticeDriver))
    }

    // ------------------------------------------------------------------ the reproduction gates
    val t198 = File("gpd/results/T-198-honeycomb-raster-width.json")
    val publishedOffsets = t217ScaffoldOffsetsOn112(t198)
    val hereOffsets = (0..111)
        .filter { Math.floorMod(it, honeycomb.azimuthPeriodBasePairs) in
                (0..2).flatMap { j -> honeycomb.scaffoldCrossoverResidues(j) }.toSet() }
    val offsetDeparture =
        if (publishedOffsets == hereOffsets) 0.0
        else publishedOffsets.zip(hereOffsets).count { (a, b) -> a != b }.toDouble()
    val reproductions = listOf(
        T217Reproduction(
            "C-0119's honeycomb staple-crossover period, base pairs",
            t217Rule(t198, "stapleCrossoverPeriodBasePairs"),
            honeycomb.azimuthPeriodBasePairs.toDouble(),
            abs(t217Rule(t198, "stapleCrossoverPeriodBasePairs") -
                    honeycomb.azimuthPeriodBasePairs),
            "gpd/results/T-198-honeycomb-raster-width.json"
        ),
        T217Reproduction(
            "C-0119's honeycomb staple-crossover step, base pairs",
            t217Rule(t198, "stapleCrossoverStepBasePairs"),
            honeycomb.basePairsPerAzimuthStep.toDouble(),
            abs(t217Rule(t198, "stapleCrossoverStepBasePairs") -
                    honeycomb.basePairsPerAzimuthStep),
            "gpd/results/T-198-honeycomb-raster-width.json"
        ),
        T217Reproduction(
            "C-0119's honeycomb scaffold offset from the staple lattice, base pairs",
            t217Rule(t198, "scaffoldOffsetFromStapleBasePairs"),
            honeycomb.scaffoldOffsetBasePairs.toDouble(),
            abs(t217Rule(t198, "scaffoldOffsetFromStapleBasePairs") -
                    honeycomb.scaffoldOffsetBasePairs),
            "gpd/results/T-198-honeycomb-raster-width.json"
        ),
        T217Reproduction(
            "C-0119's honeycomb bp per turn",
            t217Rule(t198, "basePairsPerTurn"),
            360.0 / honeycomb.designTwistPerBase,
            abs(t217Rule(t198, "basePairsPerTurn") - 360.0 / honeycomb.designTwistPerBase),
            "gpd/results/T-198-honeycomb-raster-width.json"
        ),
        T217Reproduction(
            "C-0119's 32 scaffold-crossover offsets on a 112 bp row, mismatching entries",
            0.0, offsetDeparture, offsetDeparture,
            "gpd/results/T-198-honeycomb-raster-width.json"
        ),
        T217Reproduction(
            "C-0086's square-lattice admissible widths, base pairs, largest below 200",
            176.0,
            square.admissibleRowLengths(1, 200).last().toDouble(),
            abs(176.0 - square.admissibleRowLengths(1, 200).last()),
            "C-0086 — the odd multiples of 16 bp, re-derived from the azimuths"
        ),
        T217Reproduction(
            "C-0133's residual invariant, base pairs, recovered as the honeycomb scaffold offset",
            T217_C0133_RESIDUAL_BASE_PAIRS,
            honeycomb.scaffoldOffsetResidualBasePairs(naturalBasePairsPerTurn),
            abs(T217_C0133_RESIDUAL_BASE_PAIRS -
                    honeycomb.scaffoldOffsetResidualBasePairs(naturalBasePairsPerTurn)),
            "C-0133 — 0.25 bp = 8.5714 deg"
        )
    )
    reproductions.forEach {
        println("  reproduce %-64s %.6f vs %.6f".format(it.what.take(64), it.published, it.here))
    }

    // ------------------------------------------------------------------ predicates, falsifiers
    val densityRatio = (honeycomb.azimuthPeriodBasePairs.toDouble() /
            honeycomb.turnPairResidues(0, 1).size) .let {
        (square.azimuthPeriodBasePairs.toDouble() / square.admissibleRowResidues().size) / it
    }
    val predicates = listOf(
        T217Predicate("P1",
            "the twist-correction question is settled by exact arithmetic before any search",
            abs(honeycomb.mismatchPerBase()) < 1e-13 &&
                    abs(honeycomb.accumulatedMismatchDegrees(112)) < 1e-11),
        T217Predicate("P2",
            "the connectivity condition is DERIVED, and the same construction reproduces " +
                    "C-0086's odd multiples of 16 bp on the square sheet",
            square.admissibleRowResidues() == setOf(16) &&
                    square.admissibleRowLengths(1, 200) ==
                    listOf(16, 48, 80, 112, 144, 176)),
        T217Predicate("P3",
            "the admissible widths near 40 nm are listed, with the density against the square " +
                    "sheet's and C-0119's own 112 bp row checked against them",
            widths.any { it.isC0119Row } && densityRatio > 1.0),
        T217Predicate("P4",
            "the residual is quoted: whether C-0133's quarter base pair survives, and where",
            abs(honeycomb.scaffoldOffsetResidualBasePairs(naturalBasePairsPerTurn) -
                    T217_C0133_RESIDUAL_BASE_PAIRS) < 1e-12),
        T217Predicate("P5",
            "the sensitivity to the one constant the favourable result rests on is quoted",
            sensitivity.size >= 5)
    )
    val falsifiers = listOf(
        T217Falsifier("F1",
            "the honeycomb's design twist differs from B-DNA's 360/10.5",
            abs(honeycomb.mismatchPerBase()) > 1e-13,
            "mismatch %.3e deg/bp; 720/21 and 360/10.5 are the same number"
                .format(honeycomb.mismatchPerBase())),
        T217Falsifier("F2",
            "some ODD multiple of a half turn at 10.5 bp/turn is an integer number of base " +
                    "pairs, or some multiple of FOUR half turns is not",
            oddIntegral > 0 || quadrupleNonIntegral > 0,
            ("0 of 1001 odd half-turn counts integral and 0 of 500 quadruple ones " +
                    "non-integral, over h = 1..2001")),
        T217Falsifier("F3",
            "the neighbour-azimuth construction does not reproduce C-0086's odd multiples of 16",
            square.admissibleRowLengths(1, 200) != listOf(16, 48, 80, 112, 144, 176),
            "16, 48, 80, 112, 144, 176 — C-0086's list exactly"),
        T217Falsifier("F4",
            "no admissible honeycomb row length lies within 5 % of Sec 3's 40.0 nm",
            abs(nearestUnion.departureFromNominalPercent) > 5.0,
            "%d bp = %.2f nm, %+.2f %% of nominal"
                .format(nearestUnion.basePairs, nearestUnion.widthNm,
                    nearestUnion.departureFromNominalPercent)),
        T217Falsifier("F5",
            "the honeycomb admissible list is not denser than the square sheet's",
            densityRatio <= 1.0,
            ("one admissible width every %.2f bp at a fixed turn sense against the square " +
                    "sheet's every %.0f — %.3fx")
                .format(honeycomb.azimuthPeriodBasePairs.toDouble() /
                        honeycomb.turnPairResidues(0, 1).size,
                    square.azimuthPeriodBasePairs.toDouble() /
                            square.admissibleRowResidues().size, densityRatio)),
        T217Falsifier("F6",
            "the scaffold half-turn quantisation residual is not exactly a quarter base pair",
            abs(honeycomb.scaffoldOffsetResidualBasePairs(naturalBasePairsPerTurn) - 0.25) > 1e-12,
            "5 bp against the exact 5.25 — %.4f bp = %.4f deg"
                .format(honeycomb.scaffoldOffsetResidualBasePairs(naturalBasePairsPerTurn),
                    honeycomb.scaffoldOffsetResidualDegrees(naturalBasePairsPerTurn))),
        T217Falsifier("F7",
            "C-0119's own 112 bp row is inadmissible at EVERY turn sense",
            !widths.any { it.isC0119Row && it.lattice == honeycomb.name },
            "112 mod 21 = 7, which is in the Delta = 1 triple {7,17,18} and NOT in the " +
                    "Delta = 2 triple {3,4,14}")
    )
    falsifiers.forEach { println("  %-4s fired=%-5s %s".format(it.id, it.fired, it.outcome)) }

    val findings = listOf(
        ("THE HONEYCOMB NEEDS NO TWIST CORRECTION AT ALL, AND IT IS ONE DIVISION. Its azimuth " +
                "period is 21 bp and caDNAno lays it out as TWO TURNS, so its design twist is " +
                "720/21 = %.10f deg/bp — which IS B-DNA's 360/10.5, the same number. The " +
                "mismatch is %.3e deg/bp and the accumulated register across a 112 bp row is " +
                "%.3e deg, against the square sheet's %+.1f. C-0107's boundary layer, C-0104's " +
                "threshold and every number in C-0133 are SQUARE-LATTICE numbers with driver " +
                "zero here. F1 did not fire.").format(
            honeycomb.designTwistPerBase, honeycomb.mismatchPerBase(),
            honeycomb.accumulatedMismatchDegrees(112), square.accumulatedMismatchDegrees(112)),
        ("AND THE REASON UNIFIES BOTH TASKS IN ONE LINE: 10.5 = 21/2, SO A HALF TURN IS 5.25 " +
                "BASE PAIRS. h half turns is an integer number of base pairs iff h = 0 (mod 4), " +
                "and the distance to the nearest integer is EXACTLY 0.25 for odd h and EXACTLY " +
                "0.5 for h = 2 (mod 4). C-0133's theorem is the ODD case — a square-lattice " +
                "boustrophedon needs an odd number of half turns across its row. The honeycomb " +
                "is the h = 4 case: its azimuth period is FOUR half turns, which is 21 bp " +
                "exactly. The two results are one fact read at two half-turn parities. F2 did " +
                "not fire over 2001 counts."),
        ("THE CONNECTIVITY HALF DOES NOT TRANSFER AND HAD TO BE DERIVED, AND THE DERIVATION " +
                "REPRODUCES C-0086 ON THE SQUARE SHEET. Scaffold crossovers to neighbour class " +
                "j sit at 7j +- 5 (mod 21), so a raster row that receives the scaffold from " +
                "neighbour a and passes it to b is admissible iff N = 7*Delta + {0,10,11} " +
                "(mod 21) with Delta = (b-a) mod 3 nonzero. Run on the square sheet — four " +
                "azimuths at 8 bp, the two in-plane neighbours two classes apart, no scaffold " +
                "offset — the SAME construction returns N = 16 (mod 32), i.e. 16, 48, 80, 112, " +
                "144, 176: C-0086's odd multiples of 16 bp, exactly. F3 did not fire."),
        ("THE HONEYCOMB'S WIDTH LIST IS %.2fx DENSER AND IT REACHES Sec 3's 40 nm FAR CLOSER. " +
                "At a fixed turn sense the honeycomb admits one width every %.2f bp against the " +
                "square sheet's every %.0f. The nearest admissible honeycomb width to the " +
                "nominal 40.0 nm is %d bp = %.2f nm, %+.2f %%, against the square sheet's " +
                "%d bp = %.2f nm at %+.2f %% and C-0133's twist-corrected 110 bp = 37.40 nm at " +
                "-6.50 %%. F4 and F5 did not fire.").format(
            densityRatio,
            honeycomb.azimuthPeriodBasePairs.toDouble() / honeycomb.turnPairResidues(0, 1).size,
            square.azimuthPeriodBasePairs.toDouble() / square.admissibleRowResidues().size,
            nearestUnion.basePairs, nearestUnion.widthNm,
            nearestUnion.departureFromNominalPercent,
            nearestSquare.basePairs, nearestSquare.widthNm,
            nearestSquare.departureFromNominalPercent),
        ("BUT THE TWO TURN SENSES ARE DISJOINT, AND THAT IS THE ONE THING C-0119 DID NOT " +
                "CHECK. Delta = 1 admits N = {7,17,18} (mod 21) and Delta = 2 admits {3,4,14}; " +
                "their intersection is EMPTY. C-0119 establishes that the honeycomb scaffold " +
                "lattice is INTEGRAL — necessary — and does not check that a crossover landing " +
                "on it points at the neighbour the raster needs. Its own 112 bp row has " +
                "residue 7: admissible at Delta = 1 and INADMISSIBLE at Delta = 2, where the " +
                "nearest widths are %d and %d bp. Which Delta a 15 x 4 honeycomb raster carries " +
                "is a property of the honeycomb path geometry that this repository has not " +
                "derived. F7 did not fire — the row is admissible at one sense — but the " +
                "verdict is now conditional where C-0119 states it flat.").format(
            widths.filter { it.turnSense.startsWith("Delta = 2") && it.basePairs < 112 }
                .maxOf { it.basePairs },
            widths.filter { it.turnSense.startsWith("Delta = 2") && it.basePairs > 112 }
                .minOf { it.basePairs }),
        ("THE QUARTER BASE PAIR DOES NOT VANISH — IT RELOCATES, AND IT STOPS ACCUMULATING. " +
                "caDNAno's scaffold crossover sits \"five base pairs, or half a turn\" from the " +
                "staple lattice, and an exact half turn is 5.25 bp: the offset is %.4f bp " +
                "short, i.e. %.4f deg — C-0133's invariant, to the last digit. But on the " +
                "square sheet that quarter base pair is a property of the WHOLE ROW and drives " +
                "C-0107's boundary layer; here it is a fixed departure at each scaffold " +
                "crossover and the STAPLE lattice, which carries every crossover column, every " +
                "register field and every attachment station, is at B-DNA's own twist exactly. " +
                "A local, non-accumulating 8.57 deg against a global 60.0. F6 did not fire.").format(
            honeycomb.scaffoldOffsetResidualBasePairs(naturalBasePairsPerTurn),
            honeycomb.scaffoldOffsetResidualDegrees(naturalBasePairsPerTurn)),
        ("THE WHOLE FAVOURABLE RESULT RESTS ON ONE CONSTANT, AND IT IS A CONVENTION SHARED " +
                "BETWEEN THE LATTICE AND THE MATERIAL. The honeycomb is laid out at 10.5 " +
                "bp/turn BECAUSE that is taken to be B-DNA's; if the duplex's own twist is " +
                "10.44 the honeycomb accumulates %.2f deg over 112 bp, which is %.0f %% of the " +
                "square sheet's 60.0, and at 10.34 it equals it. A 0.57 %% error in the assumed " +
                "twist therefore costs 37 %% of C-0086's uncorrected driver — so the honeycomb's " +
                "advantage is exactly as good as the constant, and nothing in this repository " +
                "measures it. Quote the advantage with the twist it is read at.").format(
            abs(honeycomb.accumulatedMismatchDegrees(112, 360.0 / 10.44)),
            100.0 * abs(honeycomb.accumulatedMismatchDegrees(112, 360.0 / 10.44)) / squareDriver),
        ("NO SOLVE IS JUSTIFIED AND THAT IS A RESULT, NOT AN OMISSION. This repository's " +
                "lattice machinery is single-layer square-lattice throughout — OrigamiGrillage " +
                "never reads `layers` or `interlayerCoupling`, and CrossoverLayout's two-parity " +
                "alternation IS the square lattice's combinatorics — so a dishing number " +
                "computed on it would be a square-lattice number wearing a honeycomb label. " +
                "What the honeycomb four-layer tile needs is a station lattice, a plan ceiling " +
                "and a placement family of its own, and none of them exists here.")
    )

    val result = T217Result(
        task = "T-217",
        leaf = "A8.2",
        question = "Can the four-layer honeycomb tile be twist-corrected, and what does its own " +
                "connectivity quantisation cost instead?",
        conditions = mapOf(
            "temperature" to "300 K",
            "kBT" to "4.141947 pN nm",
            "rise" to "$rise nm per base pair",
            "naturalTwist" to ("B-DNA at $naturalBasePairsPerTurn bp per turn, " +
                    "$B_DNA_TWIST_PER_BASE deg per base — this repository's locked value"),
            "honeycombRules" to ("Douglas et al., NAR 37:5001 (caDNAno), PMC2731887: three " +
                    "azimuths 7 bp apart, the same pair every 21 bp at 10.5 bp per turn, " +
                    "scaffold crossovers 5 bp — \"or half a turn\" — from the staple ones"),
            "squareRules" to ("Ke et al., JACS 131:15903 and Rothemund 2006: four azimuths 8 bp " +
                    "apart, the same pair every 32 bp, the two in-plane neighbours two classes " +
                    "apart, scaffold and staple crossovers on one plane lattice"),
            "nominalWidth" to "$T217_NOMINAL_WIDTH nm, Sec 3's tile",
            "c0104Threshold" to "$T217_C0104_THRESHOLD_DEGREES deg, quoted for scale only",
            "solve" to "NONE — this study is exact residue arithmetic and a literature reading"
        ),
        lattices = lattices,
        halfTurns = halfTurns,
        widths = widths,
        sensitivity = sensitivity,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "honeycombDesignTwistPerBase" to honeycomb.designTwistPerBase.toString(),
            "squareDesignTwistPerBase" to square.designTwistPerBase.toString(),
            "bDnaTwistPerBase" to B_DNA_TWIST_PER_BASE.toString(),
            "honeycombMismatchPerBase" to honeycomb.mismatchPerBase().toString(),
            "honeycombResiduesDelta1" to
                    honeycomb.turnPairResidues(0, 1).sorted().joinToString(","),
            "honeycombResiduesDelta2" to
                    honeycomb.turnPairResidues(0, 2).sorted().joinToString(","),
            "honeycombResiduesSameNeighbour" to
                    honeycomb.turnPairResidues(0, 0).sorted().joinToString(","),
            "squareResidues" to square.admissibleRowResidues().sorted().joinToString(","),
            "nearestHoneycombWidthBasePairs" to nearestUnion.basePairs.toString(),
            "nearestHoneycombWidthNm" to nearestUnion.widthNm.toString(),
            "nearestDelta1WidthBasePairs" to nearestDelta1.basePairs.toString(),
            "nearestSquareWidthBasePairs" to nearestSquare.basePairs.toString(),
            "admissibleWidthDensityRatio" to densityRatio.toString(),
            "oddHalfTurnCountsChecked" to "2001",
            "sources" to "gpd/results/T-198-honeycomb-raster-width.json"
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-217-honeycomb-twist-correction.json")
    out.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).roundedForResult(floor = 1e-15)
        )
    )
    println("T-217 — wrote ${out.path}")
    findings.forEach { println("  * $it") }
}
