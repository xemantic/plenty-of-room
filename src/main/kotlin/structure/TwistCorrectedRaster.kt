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

import com.xemantic.nano.plentyofroom.anchoring.CrossoverAzimuth
import com.xemantic.nano.plentyofroom.anchoring.OrigamiDuplex
import com.xemantic.nano.plentyofroom.anchoring.UpwardArmPlacement
import com.xemantic.nano.plentyofroom.anchoring.UpwardArmRow
import com.xemantic.nano.plentyofroom.anchoring.armDirections
import com.xemantic.nano.plentyofroom.anchoring.rowRootOptions
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * `T-189` — can `C-0086`'s 112 bp seamless raster row be **twist-corrected**?
 *
 * ## The two demands
 *
 * `C-0086` quantises the row length on **connectivity**. Rothemund's constraint, read directly,
 * is that *"for the scaffold to raster progressively from one helix to another and onto a third,
 * the distance between successive scaffold crossovers must be an **odd number of half turns**"*;
 * a boustrophedon has only progressive crossovers and its successive scaffold crossovers are the
 * two ends of **one row**, so the constraint binds the row length.
 *
 * A **twist correction** wants the crossover lattice to be laid out at the duplex's *own* twist.
 * Snodin et al. (2019), read directly: the measured tile *"included a suitable number of sections
 * with **31 base pairs** between equivalent junctions in order to remove this net twist"*, against
 * the square lattice's nominal 32. Rothemund's own program changes *"helical domain lengths … by
 * single bases until the strain energy is minimized"*.
 *
 * ## The theorem, which is the whole cheap bound
 *
 * Write `q` for the number of half turns across a row and `N` for its length in base pairs. The
 * two demands are `q` **odd** and `N·ω = 180 q` with `ω` B-DNA's `360/10.5 = 34.2857…` degrees per
 * base pair. Together they give
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`N = 180 q/ω = 5.25 q = 21 q/4`,
 *
 * and `21 q` is **odd** for every odd `q`, so `21q/4` is **never an integer**.
 * **A seamless boustrophedon row cannot be exactly twist-corrected, at any width, at any domain
 * mix.** The two quantisations are incompatible as an identity, not as a search.
 *
 * ## What is left, and it is an invariant
 *
 * Because `21q ≡ 1` or `3 (mod 4)`, the nearest integer to `21q/4` is **exactly a quarter of a
 * base pair away**, for every odd `q`. So the residual global twist of the best twist-corrected
 * seamless row is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`0.25 × 34.2857° = 8.5714°`,
 *
 * **independently of the row length** — a quarter base pair of rotation across the whole tile.
 * `C-0086`'s own 112 bp row sits `1.75` base pairs away from its nearest odd half-turn count, so
 * it carries `60.0°`: **exactly seven times** the floor, and that seven is a ratio of two integers
 * rather than a computed number.
 *
 * Units: lengths **nm**, angles **degrees** at the interface and **radians** in the fields,
 * torsional rigidity **pN·nm²**, hinge stiffness **pN·nm/rad**.
 */
const val B_DNA_TWIST_PER_BASE: Double = 360.0 / 10.5

/** The 8 bp out-of-plane plane offset of the square lattice — `C-0055`'s `EAST` azimuth. */
const val OUT_OF_PLANE_OFFSET_BASE_PAIRS: Int = 8

/**
 * One raster row, as the sequence of its inter-column **domains** in base pairs.
 *
 * A domain is nominally 1.5 turns on the square lattice ([halfTurnsPerDomain] = 3); Rothemund's
 * 26-helix square used 2.5-turn spacing, which is `halfTurnsPerDomain = 5`.
 */
data class RasterRow(
    val domains: List<Int>,
    val halfTurnsPerDomain: Int = 3
) {

    init {
        require(domains.isNotEmpty()) { "a row must carry at least one domain" }
        require(domains.all { it > 0 }) { "every domain must be a positive base pair count, were: $domains" }
        require(halfTurnsPerDomain > 0) {
            "halfTurnsPerDomain must be positive, was: $halfTurnsPerDomain"
        }
    }

    /** `N`, the row length in base pairs. */
    val basePairs: Int = domains.sum()

    /** `D`, the number of inter-column domains. */
    val domainCount: Int = domains.size

    /** `q`, the half turns the crossover lattice lays across the whole row. */
    val halfTurns: Int = halfTurnsPerDomain * domainCount

    /**
     * Rothemund's rule: the distance between the two scaffold crossovers of a progressive raster
     * must be an **odd** number of half turns.
     */
    val seamlessAdmissible: Boolean = halfTurns % 2 == 1

    /** `ω_d`, the degrees of azimuthal advance per base pair the crossover lattice imposes. */
    val designTwistPerBase: Double = 180.0 * halfTurns / basePairs

    /** The row width in nm at a rise of [risePerBase]. */
    fun width(risePerBase: Double): Double {
        require(risePerBase > 0.0) { "risePerBase must be positive, was: $risePerBase" }
        return basePairs * risePerBase
    }

    /** `ω_n − ω_d` in degrees per base pair; positive means the duplex wants **more** twist. */
    fun mismatchPerBase(naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE): Double =
        naturalTwistPerBase - designTwistPerBase

    /** The whole row's accumulated register error in degrees, `N (ω_n − ω_d)`. */
    fun totalMismatchDegrees(naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE): Double =
        basePairs * mismatchPerBase(naturalTwistPerBase)

    /** [totalMismatchDegrees] in radians. */
    fun totalMismatchRadians(naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE): Double =
        totalMismatchDegrees(naturalTwistPerBase) * PI / 180.0

    /**
     * The distance in **base pairs** between this row and the length that would make it an exact
     * odd half-turn count at [naturalTwistPerBase] — the quantity the theorem bounds below by 1/4.
     */
    fun residualBasePairs(naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE): Double =
        abs(basePairs - exactHalfTurnBasePairs(halfTurns, naturalTwistPerBase))

    /** `C-0107`'s signed twist-rate mismatch in rad/nm for this row. */
    fun twistRateMismatch(
        risePerBase: Double,
        naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE
    ): Double = twistRateMismatch(designTwistPerBase, naturalTwistPerBase, risePerBase)

    /** The base pairs between two crossovers to the **same** neighbour — Snodin's 31 against 32. */
    val perInterfaceSpacing: List<Int> get() = domains.zipWithNext { a, b -> a + b }

}

/** The exact, generally non-integer, base pair count of [halfTurns] half turns at [twistPerBase]. */
fun exactHalfTurnBasePairs(
    halfTurns: Int,
    twistPerBase: Double = B_DNA_TWIST_PER_BASE
): Double {
    require(halfTurns > 0) { "halfTurns must be positive, was: $halfTurns" }
    require(twistPerBase > 0.0) { "twistPerBase must be positive, was: $twistPerBase" }
    return 180.0 * halfTurns / twistPerBase
}

/**
 * [basePairs] split over [domainCount] domains as evenly as an integer split allows — Rothemund's
 * *"single bases"* and Snodin's 31/32 mix, arranged **centro-symmetrically** so that the short
 * domains sit symmetrically about the row centre.
 */
fun evenDomainMix(basePairs: Int, domainCount: Int): List<Int> {
    require(domainCount > 0) { "domainCount must be positive, was: $domainCount" }
    require(basePairs >= domainCount) {
        "a row of $basePairs bp cannot carry $domainCount domains of at least one base pair"
    }
    val short = basePairs / domainCount
    val long = short + 1
    val longCount = basePairs - short * domainCount
    var remaining = domainCount - longCount
    val slots = MutableList(domainCount) { long }
    // the short domains are placed **centro-symmetrically**, innermost first: a pair at a time,
    // and the middle slot only when an odd number of them is left over
    val order = (0 until domainCount).sortedWith(
        compareBy({ abs(it - (domainCount - 1) / 2.0) }, { it })
    )
    order.forEach { i ->
        if (remaining > 0 && slots[i] == long) {
            val mirror = domainCount - 1 - i
            if (i == mirror) {
                if (remaining % 2 == 1) {
                    slots[i] = short
                    remaining--
                }
            } else if (remaining >= 2) {
                slots[i] = short
                slots[mirror] = short
                remaining -= 2
            }
        }
    }
    order.forEach { i ->
        if (remaining > 0 && slots[i] == long) {
            slots[i] = short
            remaining--
        }
    }
    require(remaining == 0) { "could not place every short domain of $basePairs bp over $domainCount" }
    return slots
}

/**
 * Every seamless-admissible row that is as close to twist-corrected as an integer row can be, from
 * one domain up to [maximumDomains].
 *
 * `q = halfTurnsPerDomain · D` must be **odd**, so `D` must be odd whenever `halfTurnsPerDomain`
 * is; the row length is then the integer nearest `180 q/ω`, which the theorem puts exactly a
 * quarter of a base pair away.
 */
fun seamlessTwistCorrectedRows(
    maximumDomains: Int,
    halfTurnsPerDomain: Int = 3,
    naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE
): List<RasterRow> {
    require(maximumDomains >= 1) { "maximumDomains must be at least one, was: $maximumDomains" }
    return (1..maximumDomains)
        .filter { (halfTurnsPerDomain * it) % 2 == 1 }
        .map { count ->
            val exact = exactHalfTurnBasePairs(halfTurnsPerDomain * count, naturalTwistPerBase)
            RasterRow(evenDomainMix(exact.roundToInt(), count), halfTurnsPerDomain)
        }
}

/**
 * The azimuthal register error in **radians** at each of a row's crossover columns.
 *
 * A generalisation of [EdgeTwistRelief.discreteEndResidual] to **non-uniform** domains: the
 * lattice imposes `180·halfTurnsPerDomain` of advance across every domain whatever its length, so
 * a domain of `n` base pairs carries a per-domain mismatch `n ω_n − 180 h` that the duplex's own
 * torsion must absorb. The energy minimised is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`Σᵢ (C/ℓᵢ)(u_{i+1} − uᵢ − δᵢ)²/2 + Σⱼ (k_θ/p) wⱼ uⱼ²/2`,
 *
 * with `wⱼ` the tributary contour of column `j`. Both row ends are **free**, which is the natural
 * boundary condition `C-0107` derives, and on equal domains this reproduces
 * [EdgeTwistRelief.discreteEndResidual] to the last bit.
 *
 * [subdivisions] splits each domain into equal sub-segments, which spreads the hinge stiffness
 * along the contour rather than lumping it at the columns — the **smearing convention**, and the
 * axis on which this discrete lattice approaches `C-0107`'s continuum. `1` is the physical model:
 * a crossover is at a column and nowhere else.
 *
 * [driverScale] multiplies the whole per-domain mismatch vector. The system is linear, so the
 * field scales with it **exactly** — which is what makes `C-0107`'s twelve-cell bracket re-read by
 * multiplication rather than by a sweep, and it is asserted rather than asserted-about.
 *
 * Solved by Thomas elimination on a symmetric positive-definite tridiagonal system.
 */
@Suppress("LongParameterList")
fun columnRegisterField(
    domains: List<Int>,
    torsionalRigidity: Double,
    hingeStiffness: Double,
    crossoverSpacing: Double,
    risePerBase: Double,
    naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE,
    halfTurnsPerDomain: Int = 3,
    subdivisions: Int = 1,
    driverScale: Double = 1.0
): DoubleArray {
    require(domains.isNotEmpty()) { "domains must not be empty" }
    require(domains.all { it > 0 }) { "every domain must be positive, were: $domains" }
    require(subdivisions >= 1) { "subdivisions must be at least one, was: $subdivisions" }
    require(driverScale.isFinite()) { "driverScale must be finite, was: $driverScale" }
    require(torsionalRigidity > 0.0) { "torsionalRigidity must be positive, was: $torsionalRigidity" }
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(crossoverSpacing > 0.0) { "crossoverSpacing must be positive, was: $crossoverSpacing" }
    require(risePerBase > 0.0) { "risePerBase must be positive, was: $risePerBase" }
    val n = domains.size * subdivisions
    val lengths = DoubleArray(n) { domains[it / subdivisions] * risePerBase / subdivisions }
    val delta = DoubleArray(n) {
        driverScale *
                (domains[it / subdivisions] * naturalTwistPerBase - 180.0 * halfTurnsPerDomain) *
                PI / 180.0 / subdivisions
    }
    val kappa = hingeStiffness / crossoverSpacing
    val diagonal = DoubleArray(n + 1)
    val off = DoubleArray(n) { -torsionalRigidity / lengths[it] }
    val rhs = DoubleArray(n + 1)
    for (j in 0..n) {
        val left = if (j > 0) torsionalRigidity / lengths[j - 1] else 0.0
        val right = if (j < n) torsionalRigidity / lengths[j] else 0.0
        val lumped = (if (j > 0) lengths[j - 1] / 2.0 else 0.0) +
                (if (j < n) lengths[j] / 2.0 else 0.0)
        diagonal[j] = left + right + kappa * lumped
        rhs[j] = (if (j > 0) (torsionalRigidity / lengths[j - 1]) * delta[j - 1] else 0.0) -
                (if (j < n) (torsionalRigidity / lengths[j]) * delta[j] else 0.0)
    }
    val cPrime = DoubleArray(n)
    val dPrime = DoubleArray(n + 1)
    cPrime[0] = off[0] / diagonal[0]
    dPrime[0] = rhs[0] / diagonal[0]
    for (j in 1..n) {
        val denominator = diagonal[j] - off[j - 1] * cPrime[j - 1]
        if (j < n) cPrime[j] = off[j] / denominator
        dPrime[j] = (rhs[j] - off[j - 1] * dPrime[j - 1]) / denominator
    }
    val u = DoubleArray(n + 1)
    u[n] = dPrime[n]
    for (j in n - 1 downTo 0) u[j] = dPrime[j] - cPrime[j] * u[j + 1]
    // return the field at the COLUMNS, which is where the crossovers are
    return DoubleArray(domains.size + 1) { u[it * subdivisions] }
}

/** The `x` of every crossover column of a row of [domains], measured from the row centre, in nm. */
fun rasterColumnPositions(domains: List<Int>, risePerBase: Double): List<Double> {
    require(domains.isNotEmpty()) { "domains must not be empty" }
    require(risePerBase > 0.0) { "risePerBase must be positive, was: $risePerBase" }
    val half = domains.sum() * risePerBase / 2.0
    var x = -half
    val positions = ArrayList<Double>(domains.size + 1)
    positions += x
    domains.forEach { n ->
        x += n * risePerBase
        positions += x
    }
    return positions
}

/**
 * The crossover columns of a row of [domains] as a [CrossoverLayout], with the two row-end columns
 * inset by [inset] exactly as `rasterColumnLayout` insets them.
 *
 * Reproduces `anchoring.rasterColumnLayout(8, sheet, 38.08, true)` to the last bit on seven 16 bp
 * domains, which is the gate that makes this a generalisation rather than a second lattice.
 */
fun twistCorrectedColumnLayout(
    domains: List<Int>,
    risePerBase: Double,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): CrossoverLayout {
    require(inset > 0.0) { "inset must be positive, was: $inset" }
    val raw = rasterColumnPositions(domains, risePerBase)
    val half = domains.sum() * risePerBase / 2.0
    val positions = raw.map {
        when {
            it <= -half + 1.0e-12 -> -(half - inset)
            it >= half - 1.0e-12 -> half - inset
            else -> it
        }
    }
    return CrossoverLayout(
        positions = positions,
        parities = positions.indices.map { it % 2 }
    )
}

/**
 * The **upward** (`EAST`) junction sites of a row of [domains], one list per duplex.
 *
 * The square lattice's four azimuths advance one per 8 bp plane and the out-of-plane `EAST` site
 * of duplex `d` sits at plane `k ≡ 2d+3 (mod 4)` (`C-0055`, `C-0015`). Between two columns there
 * is exactly one odd plane, at [OUT_OF_PLANE_OFFSET_BASE_PAIRS] past the column that precedes it —
 * which is the nearest whole base pair to the 270° azimuth at B-DNA's own twist (7.875 bp), and is
 * unchanged by the domain length. With the row-end column as plane index `−2D` the domain `i`
 * carries an `EAST` site for duplex `d` exactly when `i ≡ d+1 (mod 2)`.
 */
fun twistCorrectedUpwardSites(
    domains: List<Int>,
    duplexes: Int,
    risePerBase: Double,
    outOfPlaneOffsetBasePairs: Int = OUT_OF_PLANE_OFFSET_BASE_PAIRS,
    mirrorOffsets: Boolean = false
): List<List<Double>> {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(domains.all { it > outOfPlaneOffsetBasePairs }) {
        "every domain must be longer than the out-of-plane offset, were: $domains"
    }
    val columns = rasterColumnPositions(domains, risePerBase)
    val offsets = outOfPlaneOffsets(domains, outOfPlaneOffsetBasePairs, mirrorOffsets)
    return (0 until duplexes).map { duplex ->
        domains.indices
            .filter { Math.floorMod(it - duplex - 1, 2) == 0 }
            .map { columns[it] + offsets[it] * risePerBase }
            .sorted()
    }
}

/**
 * The base pairs from a domain's **left** column at which its out-of-plane station sits.
 *
 * `8` everywhere is the square lattice's own plane, and the nearest whole base pair to the 270°
 * `EAST` azimuth at B-DNA's twist (7.875 bp). It is **not** mirror-symmetric inside a domain whose
 * length is odd, so on a twist-corrected row it breaks the station lattice's centro-symmetry —
 * which `C-0063`'s exhaustive family and every placement claim in this repository assume. With
 * [mirror] the offsets right of the row centre become `n − 8`, which restores the symmetry exactly
 * and costs azimuth: 7 bp is 240° against 270°, a **30.0°** departure where 8 bp is **4.286°**.
 */
fun outOfPlaneOffsets(
    domains: List<Int>,
    outOfPlaneOffsetBasePairs: Int = OUT_OF_PLANE_OFFSET_BASE_PAIRS,
    mirror: Boolean = false
): List<Int> = domains.indices.map { i ->
    if (mirror && 2 * i > domains.size - 1) domains[i] - outOfPlaneOffsetBasePairs
    else outOfPlaneOffsetBasePairs
}

/** The azimuth departure in degrees of a station [offsetBasePairs] past its column, from `EAST`. */
fun azimuthDeparture(
    offsetBasePairs: Int,
    naturalTwistPerBase: Double = B_DNA_TWIST_PER_BASE
): Double = offsetBasePairs * naturalTwistPerBase - 270.0

/** The azimuth of the plane [planeIndex] on duplex [duplex], the rule `C-0055` publishes. */
fun planeAzimuth(planeIndex: Int, duplex: Int): CrossoverAzimuth =
    CrossoverAzimuth.entries[Math.floorMod(planeIndex - 2 * duplex, CrossoverAzimuth.entries.size)]


/**
 * Every centro-symmetric placement of [count] roots on an **explicit** station [lattice].
 *
 * `C-0063`'s `centroSymmetricPlacements` builds its lattice from a phase and a width, which a
 * twist-corrected row — whose column pitch is not uniform — does not have. This is the same
 * enumeration over a lattice handed in, and it is gated by reproducing that routine's family
 * exactly on the uniform 112 bp lattice.
 */
@Suppress("LongParameterList")
fun centroSymmetricPlacementsOn(
    lattice: List<List<Double>>,
    edgeX: Double,
    arm: Double,
    count: Int,
    phaseTag: Int = 0,
    minimumPerRow: Int = 1,
    maximumPerRow: Int = 3,
    width: Double = OrigamiDuplex.INTERHELICAL,
    tolerance: Double = 1e-9
): Sequence<UpwardArmPlacement> {
    require(count >= 1) { "count must be at least one, was: $count" }
    require(lattice.size >= 2) { "a lattice needs at least two rows, had ${lattice.size}" }
    val duplexes = lattice.size
    val half = duplexes / 2
    val middle = if (duplexes % 2 == 1) half else -1
    val options: List<Map<Int, List<List<Double>>>> = (0 until duplexes).map { row ->
        (minimumPerRow..maximumPerRow).associateWith { size ->
            val all = rowRootOptions(lattice[row], size, arm, edgeX, width)
            if (row == middle) all.filter { roots ->
                roots.zip(roots.map { -it }.sorted()).all { (a, b) -> abs(a - b) <= tolerance }
            } else all
        }
    }
    val free = (0 until half).toList()

    fun mirrored(row: UpwardArmRow, partner: Int): UpwardArmRow {
        val roots = row.roots.map { -it }.sorted()
        val directions = requireNotNull(armDirections(roots, arm, edgeX, width)) {
            "the reflection of a feasible row must itself be feasible on a symmetric lattice, " +
                    "and $roots in row $partner is not"
        }
        return UpwardArmRow(partner, roots, directions)
    }

    fun expand(index: Int, remaining: Int, taken: List<UpwardArmRow>): Sequence<UpwardArmPlacement> {
        if (index == free.size) {
            if (middle < 0) {
                return if (remaining != 0) emptySequence()
                else sequenceOf(
                    UpwardArmPlacement(
                        phaseTag,
                        (taken + taken.map { mirrored(it, duplexes - 1 - it.row) })
                            .sortedBy { it.row }
                    )
                )
            }
            val candidates = options[middle][remaining] ?: return emptySequence()
            return candidates.asSequence().map { roots ->
                UpwardArmPlacement(
                    phaseTag,
                    (taken + taken.map { mirrored(it, duplexes - 1 - it.row) } +
                            UpwardArmRow(middle, roots, armDirections(roots, arm, edgeX, width)!!))
                        .sortedBy { it.row }
                )
            }
        }
        val row = free[index]
        return (minimumPerRow..maximumPerRow).asSequence().flatMap { size ->
            val left = remaining - 2 * size
            if (left < 0) emptySequence()
            else (options[row][size] ?: emptyList()).asSequence().flatMap { roots ->
                expand(
                    index + 1, left,
                    taken + UpwardArmRow(row, roots, armDirections(roots, arm, edgeX, width)!!)
                )
            }
        }
    }
    return expand(0, count, emptyList())
}
