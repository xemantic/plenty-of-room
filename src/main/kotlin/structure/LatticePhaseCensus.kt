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

import kotlin.math.abs
import kotlin.math.round

/**
 * `T-216` — the crossover **phase lattice** of a mixed-domain row — and `T-217` — can the
 * four-layer **honeycomb** tile be twist-corrected?
 *
 * ## The one arithmetic both tasks share
 *
 * B-DNA's preferred twist in this repository is `10.5` base pairs per turn (`C-0015`, `C-0107`,
 * `C-0133`), so **a half turn is 5.25 base pairs**. Therefore `h` half turns is an integer number
 * of base pairs **iff `h ≡ 0 (mod 4)`**, and the distance to the nearest integer is exactly
 * `0.25` for **odd** `h` and exactly `0.5` for `h ≡ 2 (mod 4)`.
 *
 * - `C-0133`'s theorem is the **odd** case: a square-lattice boustrophedon needs an *odd* number of
 *   half turns across its row, so no integer row length is ever exactly twist-corrected and the
 *   residual is exactly a quarter base pair — `8.5714°`.
 * - The **honeycomb** is the `h = 4` case: its azimuth period is 21 bp = **two turns** = four half
 *   turns, which is an integer, so the honeycomb lattice can be — and by construction **is** — laid
 *   out at exactly `10.5` bp/turn. Its twist mismatch is **zero**, identically, and there is
 *   nothing to correct.
 * - The quarter base pair does not disappear from the honeycomb; it **relocates** to the one place
 *   the honeycomb does use an odd half turn — caDNAno's *"five base pairs, or half a turn"*
 *   scaffold offset, which is `0.25` bp short of the exact `5.25`.
 *
 * Units: lengths **nm**, angles **degrees**, base pair counts dimensionless.
 */

// ---------------------------------------------------------------------------------------------
// half turns, and the integrality that decides both tasks
// ---------------------------------------------------------------------------------------------

/** The base pairs spanned by [halfTurns] half turns of a duplex of [basePairsPerTurn]. */
fun halfTurnBasePairs(halfTurns: Int, basePairsPerTurn: Double = 10.5): Double {
    require(halfTurns > 0) { "halfTurns must be positive, was: $halfTurns" }
    require(basePairsPerTurn > 0.0) {
        "basePairsPerTurn must be positive, was: $basePairsPerTurn"
    }
    return halfTurns * basePairsPerTurn / 2.0
}

/** How far [value] is from the nearest integer — the residual `C-0133` bounds below by 1/4. */
fun distanceToNearestInteger(value: Double): Double = abs(value - round(value))

// ---------------------------------------------------------------------------------------------
// T-217 — a helix crossover lattice, described by its own azimuth arithmetic
// ---------------------------------------------------------------------------------------------

/**
 * A DNA-origami crossover lattice, described by the arithmetic of its own **azimuths**.
 *
 * A helix carries [azimuthsPerHelix] crossover directions, one per neighbour it may bond to; they
 * recur along the helix every [basePairsPerAzimuthStep] base pairs, so the same direction recurs
 * every `azimuthsPerHelix × basePairsPerAzimuthStep` base pairs — the **azimuth period** — which
 * the lattice lays out as exactly [turnsPerAzimuthPeriod] turns.
 *
 * - **Square lattice, single-layer sheet** — four azimuths 8 bp apart, the same pair every 32 bp,
 *   laid out as 3 turns (10.67 bp/turn). Ke et al., *JACS* **131**:15903, and `C-0055`.
 *   The two **in-plane** neighbours are two azimuth classes apart, i.e. 180°; the raster may only
 *   use those, because the other two point out of the sheet.
 * - **Honeycomb** — three azimuths 7 bp apart, the same pair every 21 bp, laid out as 2 turns
 *   (10.5 bp/turn). Douglas et al., *NAR* **37**:5001, quoted verbatim in `C-0119`:
 *   *"antiparallel crossovers between adjacent staple helices … repeat every 21 base pairs if the
 *   helical twist is fixed at 10.5 base pairs per turn. Thus for a given staple helix, potential
 *   staple-crossover positions occur every seven base pairs, or two-thirds of a turn. Our default
 *   rules allow antiparallel crossovers between adjacent scaffold helices to occur five base pairs,
 *   or half a turn, upstream or downstream of allowed crossover positions for the associated staple
 *   helices."* All three neighbours are usable by the scaffold.
 *
 * [scaffoldOffsetBasePairs] is that *"five base pairs, or half a turn"*: the **scaffold** crossover
 * to a given neighbour sits that far upstream **or** downstream of the staple crossover to the same
 * neighbour. On Rothemund's square sheet the scaffold and staple crossovers share the plane
 * lattice, so the offset is zero.
 */
data class HelixCrossoverLattice(
    val name: String,
    val azimuthsPerHelix: Int,
    val basePairsPerAzimuthStep: Int,
    val turnsPerAzimuthPeriod: Int,
    val scaffoldOffsetBasePairs: Int,
    val rasterNeighbourClasses: List<Int>
) {

    init {
        require(azimuthsPerHelix >= 2) {
            "azimuthsPerHelix must be at least 2, was: $azimuthsPerHelix"
        }
        require(basePairsPerAzimuthStep > 0) {
            "basePairsPerAzimuthStep must be positive, was: $basePairsPerAzimuthStep"
        }
        require(turnsPerAzimuthPeriod > 0) {
            "turnsPerAzimuthPeriod must be positive, was: $turnsPerAzimuthPeriod"
        }
        require(scaffoldOffsetBasePairs >= 0) {
            "scaffoldOffsetBasePairs must not be negative, was: $scaffoldOffsetBasePairs"
        }
        require(rasterNeighbourClasses.size >= 2) {
            "a raster needs at least two usable neighbours, had: $rasterNeighbourClasses"
        }
        require(rasterNeighbourClasses.all { it in 0 until azimuthsPerHelix }) {
            "every neighbour class must be an azimuth index below $azimuthsPerHelix, " +
                    "were: $rasterNeighbourClasses"
        }
        require(rasterNeighbourClasses.distinct().size == rasterNeighbourClasses.size) {
            "the neighbour classes must be distinct, were: $rasterNeighbourClasses"
        }
    }

    /** `P` — the base pairs after which the same neighbour is reachable again. */
    val azimuthPeriodBasePairs: Int = azimuthsPerHelix * basePairsPerAzimuthStep

    /** The half turns the azimuth period lays out — 6 on the square sheet, **4** on honeycomb. */
    val halfTurnsPerAzimuthPeriod: Int = 2 * turnsPerAzimuthPeriod

    /** `ω_d`, degrees of azimuthal advance per base pair the lattice imposes. */
    val designTwistPerBase: Double = 360.0 * turnsPerAzimuthPeriod / azimuthPeriodBasePairs

    /** The azimuth advanced by one crossover step — 270° square, 240° honeycomb. */
    val azimuthStepDegrees: Double = designTwistPerBase * basePairsPerAzimuthStep

    /** `ω_n − ω_d`; positive means the duplex wants **more** twist than the lattice gives it. */
    fun mismatchPerBase(naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE): Double =
        naturalTwistPerBase - designTwistPerBase

    /** The register error a row of [basePairs] accumulates, in degrees. */
    fun accumulatedMismatchDegrees(
        basePairs: Int,
        naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE
    ): Double {
        require(basePairs > 0) { "basePairs must be positive, was: $basePairs" }
        return basePairs * mismatchPerBase(naturalTwistPerBase)
    }

    /** How far [scaffoldOffsetBasePairs] is from an exact half turn, in base pairs. */
    fun scaffoldOffsetResidualBasePairs(basePairsPerTurn: Double = 10.5): Double =
        if (scaffoldOffsetBasePairs == 0) 0.0
        else abs(scaffoldOffsetBasePairs - basePairsPerTurn / 2.0)

    /** [scaffoldOffsetResidualBasePairs] read as an azimuth, at the lattice's own design twist. */
    fun scaffoldOffsetResidualDegrees(basePairsPerTurn: Double = 10.5): Double =
        scaffoldOffsetResidualBasePairs(basePairsPerTurn) * designTwistPerBase

    /**
     * The base-pair positions **modulo the azimuth period** at which the **scaffold** may cross to
     * the neighbour in azimuth class [neighbourClass].
     */
    fun scaffoldCrossoverResidues(neighbourClass: Int): Set<Int> {
        require(neighbourClass in 0 until azimuthsPerHelix) {
            "neighbourClass must be an azimuth index below $azimuthsPerHelix, " +
                    "was: $neighbourClass"
        }
        val staple = neighbourClass * basePairsPerAzimuthStep
        return setOf(
            Math.floorMod(staple + scaffoldOffsetBasePairs, azimuthPeriodBasePairs),
            Math.floorMod(staple - scaffoldOffsetBasePairs, azimuthPeriodBasePairs)
        )
    }

    /**
     * The admissible row lengths **modulo the azimuth period** for a raster helix that receives the
     * scaffold from neighbour [from] and passes it to neighbour [to].
     *
     * A row is the stretch of one helix between its two scaffold crossovers, so its length is the
     * difference of a position in `scaffoldCrossoverResidues(to)` and one in
     * `scaffoldCrossoverResidues(from)`.
     */
    fun turnPairResidues(from: Int, to: Int): Set<Int> {
        val entries = scaffoldCrossoverResidues(from)
        val exits = scaffoldCrossoverResidues(to)
        return exits.flatMap { q ->
            entries.map { p -> Math.floorMod(q - p, azimuthPeriodBasePairs) }
        }.toSet()
    }

    /** Every admissible row-length residue, over every ordered pair of **distinct** neighbours. */
    fun admissibleRowResidues(): Set<Int> = rasterNeighbourClasses.flatMap { a ->
        rasterNeighbourClasses.filter { it != a }.flatMap { b -> turnPairResidues(a, b) }
    }.toSet()

    /** The admissible row lengths in `[minimum, maximum]` base pairs. */
    fun admissibleRowLengths(minimum: Int, maximum: Int): List<Int> =
        admissibleRowLengths(minimum, maximum, admissibleRowResidues())

    /** The row lengths in `[minimum, maximum]` whose residue lies in [residues]. */
    fun admissibleRowLengths(minimum: Int, maximum: Int, residues: Set<Int>): List<Int> {
        require(minimum >= 1) { "minimum must be at least one, was: $minimum" }
        require(maximum >= minimum) { "maximum must be at least minimum, was: $maximum" }
        return (minimum..maximum)
            .filter { Math.floorMod(it, azimuthPeriodBasePairs) in residues }
    }

    companion object {

        /**
         * Rothemund's single-layer square-lattice sheet: four azimuths at 8 bp, the same pair every
         * 32 bp = 3 turns, scaffold and staple crossovers on one plane lattice, and the two
         * **in-plane** neighbours two azimuth classes apart (`C-0055`: `EAST`/`WEST` point out of
         * the sheet). Its [admissibleRowResidues] must reproduce `C-0086`'s odd multiples of 16 bp.
         */
        val SQUARE_SHEET: HelixCrossoverLattice = HelixCrossoverLattice(
            name = "single-layer square lattice (Rothemund / Ke et al.)",
            azimuthsPerHelix = 4,
            basePairsPerAzimuthStep = 8,
            turnsPerAzimuthPeriod = 3,
            scaffoldOffsetBasePairs = 0,
            rasterNeighbourClasses = listOf(0, 2)
        )

        /** caDNAno's honeycomb: three azimuths at 7 bp, 21 bp = 2 turns, scaffold offset 5 bp. */
        val HONEYCOMB: HelixCrossoverLattice = HelixCrossoverLattice(
            name = "honeycomb (Douglas et al., caDNAno)",
            azimuthsPerHelix = 3,
            basePairsPerAzimuthStep = 7,
            turnsPerAzimuthPeriod = 2,
            scaffoldOffsetBasePairs = 5,
            rasterNeighbourClasses = listOf(0, 1, 2)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// T-216 — the phase lattice of a mixed-domain row
// ---------------------------------------------------------------------------------------------

/**
 * Every arrangement of a row of [basePairs] into [domainCount] domains whose lengths lie in
 * `[shortest, longest]`.
 *
 * The design family of a twist-corrected row is Rothemund's own remedy — *"helical domain lengths …
 * by single bases"* — so `[15, 16]` at a nominal 16. The wider shell is available as a sensitivity.
 */
fun domainArrangements(
    basePairs: Int,
    domainCount: Int,
    shortest: Int,
    longest: Int
): List<List<Int>> {
    require(domainCount >= 1) { "domainCount must be positive, was: $domainCount" }
    require(shortest >= 1) { "shortest must be positive, was: $shortest" }
    require(longest >= shortest) {
        "longest must be at least shortest, were: $shortest and $longest"
    }
    val out = ArrayList<List<Int>>()
    val current = IntArray(domainCount)
    fun walk(index: Int, remaining: Int) {
        if (index == domainCount) {
            if (remaining == 0) out += current.toList()
            return
        }
        val left = domainCount - index - 1
        (shortest..longest).forEach { n ->
            val rest = remaining - n
            if (rest >= left * shortest && rest <= left * longest) {
                current[index] = n
                walk(index + 1, rest)
            }
        }
    }
    walk(0, basePairs)
    return out
}

/** Whether a domain sequence is a palindrome, which is exactly a centro-symmetric column set. */
fun isCentroSymmetricDomains(domains: List<Int>): Boolean = domains == domains.reversed()

/** How many of [arrangements] are distinct up to reflection about the row centre. */
fun reflectionClassCount(arrangements: List<List<Int>>): Int = arrangements.map {
    val forward = it.joinToString(",")
    val backward = it.reversed().joinToString(",")
    if (forward <= backward) forward else backward
}.distinct().size

/**
 * The rigid translations, in base pairs, that carry a seamless raster row's column pattern onto an
 * admissible column pattern of the **same** row.
 *
 * A seamless raster row's two ends *are* the tile edges along the helix axis and both carry a
 * scaffold crossover (`C-0095`, `C-0086`), so both end columns are pinned. Translating by `t`
 * requires `0` and `N` to remain columns, i.e. `−t` and `N − t` to have been columns; since `0` is
 * the smallest column and `N` the largest, that forces `t = 0`.
 *
 * **The answer is `[0]` for every seamless row, uniform or mixed** — the translational phase
 * variable `C-0015` sweeps is a freedom of a tile whose row ends are *not* crossovers, and
 * `C-0086`'s seamlessness requirement has already spent it. Returned as an enumeration rather than
 * asserted, so a counterexample would show up as an extra entry.
 */
fun admissibleColumnTranslations(domains: List<Int>): List<Int> {
    require(domains.isNotEmpty()) { "domains must not be empty" }
    require(domains.all { it > 0 }) { "every domain must be positive, were: $domains" }
    val n = domains.sum()
    val columns = ArrayList<Int>(domains.size + 1)
    var x = 0
    columns += x
    domains.forEach { x += it; columns += x }
    val set = columns.toHashSet()
    return (-n + 1 until n).filter { t ->
        columns.all { it + t in 0..n } && (0 - t) in set && (n - t) in set
    }
}

/**
 * The **upward** junction sites of a row of [domains], one list per duplex, at column/interface
 * [parity].
 *
 * `C-0015`'s two admissible phases on a seamless uniform row — 8 and 24 at 38.08 nm — give
 * **identical column positions** and **opposite parities**, so what a seamless row retains of
 * `C-0015`'s phase variable is a **binary**, not a translation. `C-0090` measures it: 0.0621 of the
 * stroke at one parity and 0.0707 at the other, because shifting by one column pitch is two 8 bp
 * planes and therefore exchanges a duplex's `EAST` and `WEST` azimuths — `CLAUDE.md`'s *reflecting
 * an out-of-plane array moves it to the other face of the sheet*.
 *
 * [parity] `0` reproduces [twistCorrectedUpwardSites] exactly; `1` is the other sheet.
 */
@Suppress("LongParameterList")
fun mixedDomainUpwardSites(
    domains: List<Int>,
    duplexes: Int,
    risePerBase: Double,
    outOfPlaneOffsetBasePairs: Int = OUT_OF_PLANE_OFFSET_BASE_PAIRS,
    mirrorOffsets: Boolean = false,
    parity: Int = 0
): List<List<Double>> {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(parity == 0 || parity == 1) { "parity must be 0 or 1, was: $parity" }
    require(domains.all { it > outOfPlaneOffsetBasePairs }) {
        "every domain must be longer than the out-of-plane offset, were: $domains"
    }
    val columns = rasterColumnPositions(domains, risePerBase)
    val offsets = outOfPlaneOffsets(domains, outOfPlaneOffsetBasePairs, mirrorOffsets)
    return (0 until duplexes).map { duplex ->
        domains.indices
            .filter { Math.floorMod(it - duplex - 1 + parity, 2) == 0 }
            .map { columns[it] + offsets[it] * risePerBase }
            .sorted()
    }
}
