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

import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.ceil
import kotlin.math.floor

/**
 * `T-81` — how many antiparallel crossovers a **hinge line** on a single-layer Rothemund sheet
 * can hold, and what that does to `C-0023`/`C-0029`/`C-0034`'s asserted count of sixteen.
 *
 * ## What a hinge line is
 *
 * A **hinge line** is a maximal set of crossovers that share **one interface** and **one pair of
 * bodies**. They are collinear along the helices, they turn through the same angle, and their
 * `k_θ` therefore add in **parallel**, which is the only reading under which `n k_θ` is the right
 * torsional spring. `k_θ` is the *interhelical dihedral* constant: it resists rotation of duplex
 * `b+1` relative to duplex `b` **about their common interface line**, which runs along `x`. That
 * is how `C-0009`'s grillage uses it and how `C-0015` recovers `D_⊥` from it, and it is why a
 * hinge whose axis runs any other way cannot be priced with this constant at all.
 *
 * ## Why the count is bounded before any model runs
 *
 * Crossovers recur every 16 bp along a *helix* but **alternate between its two neighbours**, so a
 * given *interface* is linked every `p = 32 bp = 10.88 nm`. A hinge line of `n` crossovers
 * therefore needs `(n − 1) p` of collinear interface — `163.2 nm` for sixteen, against a 40 nm
 * tile. No force field, sequence or lattice can move a pitch, and no simulation can move a count.
 *
 * The functions here do the counting on `C-0015`'s own lattice rather than on a restatement of
 * it: [hingeLineCensus] is asserted equal to [CrossoverLayout.atBasePairPhase] at every one of
 * the 32 phases.
 */

// ------------------------------------------------------------------ the lattice, counted

/**
 * The pitch in nm at which crossovers recur on **one** interface — `32 bp × 0.34 nm/bp`.
 *
 * Not `16 bp`. The per-helix number belongs to a helix and the per-interface number to an
 * interface, and using one where the other belongs doubles every count here.
 */
fun perInterfacePitch(
    spacingBasePairs: Double = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Double {
    require(spacingBasePairs > 0.0) {
        "spacingBasePairs must be positive, was: $spacingBasePairs"
    }
    require(risePerBasePair > 0.0) {
        "risePerBasePair must be positive, was: $risePerBasePair"
    }
    return spacingBasePairs * risePerBasePair
}

/** The collinear interface in nm a hinge line of [count] crossovers demands: `(n − 1) p`. */
fun hingeLineLengthForCount(count: Int, pitch: Double): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    return (count - 1) * pitch
}

/** The largest crossover count a hinge line of [lineLength] can hold: `⌊L/p⌋ + 1`. */
fun maximumHingeCount(lineLength: Double, pitch: Double): Int {
    require(lineLength >= 0.0) { "lineLength must not be negative, was: $lineLength" }
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    return floor(lineLength / pitch + 1.0e-12).toInt() + 1
}

/**
 * The crossovers of **one** parity inside a hinge line of [lineLength] nm centred on the origin,
 * whose lattice stations sit at `phase + k·pitch`.
 *
 * The window is `CrossoverLayout`'s own — `±(L/2 − EDGE_MARGIN)` — so that this census and
 * `C-0015`'s layout are counting the same lattice rather than two similar ones.
 */
fun crossoversInLine(
    lineLength: Double,
    phase: Double,
    pitch: Double,
    edgeMargin: Double = CrossoverLayout.EDGE_MARGIN
): Int {
    require(lineLength > 0.0) { "lineLength must be positive, was: $lineLength" }
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    require(edgeMargin >= 0.0) { "edgeMargin must not be negative, was: $edgeMargin" }
    val half = lineLength / 2.0 - edgeMargin
    if (half <= 0.0) return 0
    val first = ceil((-half - phase) / pitch).toInt()
    val last = floor((half - phase) / pitch).toInt()
    return if (last < first) 0 else last - first + 1
}

/**
 * One phase of the census: how many crossovers each of the two interface parities carries on a
 * hinge line of the stated length.
 *
 * @param phaseBasePairs the column lattice's phase, `0 until 32`.
 * @param columns the total number of crossover columns inside the line, both parities.
 * @param evenInterfaces the count on an interface of parity `0`.
 * @param oddInterfaces the count on an interface of parity `1`.
 */
data class HingeLineCount(
    val phaseBasePairs: Int,
    val columns: Int,
    val evenInterfaces: Int,
    val oddInterfaces: Int
) {

    /** The count a designer gets by choosing the better parity — what a hinge line can hold. */
    val largest: Int get() = maxOf(evenInterfaces, oddInterfaces)

    /** The count the other parity is left with, which no choice can improve. */
    val smallest: Int get() = minOf(evenInterfaces, oddInterfaces)
}

/**
 * The **complete** census of a hinge line of [lineLength] nm over all [periodBasePairs] phases.
 *
 * The phase is quantised to base pairs and its period is `32 bp`, not `16` — a half-period shift
 * leaves every column position unchanged and hands every interface the **other** parity's
 * columns. Sweeping `[0, 16 bp)` would cover half the design space while looking complete.
 */
fun hingeLineCensus(
    lineLength: Double,
    pitch: Double = perInterfacePitch(),
    periodBasePairs: Int = Gen1Tile.CROSSOVER_SPACING_SHEET_BP.toInt(),
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<HingeLineCount> {
    require(periodBasePairs > 0) { "periodBasePairs must be positive, was: $periodBasePairs" }
    return (0 until periodBasePairs).map { phase ->
        val offset = phase * risePerBasePair
        val even = crossoversInLine(lineLength, offset, pitch)
        val odd = crossoversInLine(lineLength, offset + pitch / 2.0, pitch)
        HingeLineCount(phase, even + odd, even, odd)
    }
}

/**
 * The crossovers a **transverse** line — one at a time across the helices, `TASKS.md`'s own guess
 * at where sixteen might come from — can hold on a sheet of [duplexes].
 *
 * A transverse line sits at one `x`, so it serves only the interfaces of **one** [parity]: of the
 * `D − 1` interfaces it reaches every second one. Its crossovers also restrain the *wrong* axis —
 * each is a dihedral spring about the interface line, which runs along the helices — so this
 * count is reported as an upper bound on a topology that does not supply the spring it needs.
 */
fun transverseHingeCount(duplexes: Int, parity: Int): Int {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(parity == 0 || parity == 1) { "parity must be 0 or 1, was: $parity" }
    return (0 until duplexes - 1).count { it % 2 == parity }
}

/** The duplexes a transverse line of [count] crossovers would need: `2n + 1`. */
fun duplexesForTransverseCount(count: Int): Int {
    require(count > 0) { "count must be positive, was: $count" }
    return 2 * count + 1
}

/**
 * The crossovers a sheet of [duplexes] holds, given the per-parity counts of one column layout.
 *
 * Reproduces `C-0015`'s 56 at the ten eight-column phases and 49 at the other twenty-two.
 */
fun tileCrossoverInventory(duplexes: Int, evenInterfaces: Int, oddInterfaces: Int): Int {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(evenInterfaces >= 0 && oddInterfaces >= 0) {
        "interface counts must not be negative, were: $evenInterfaces and $oddInterfaces"
    }
    return transverseHingeCount(duplexes, 0) * evenInterfaces +
            transverseHingeCount(duplexes, 1) * oddInterfaces
}

/**
 * `C-0015`'s parity rule: the crossover lattice is centro-symmetric exactly when
 * `columns + duplexes` is odd, and has **no** symmetry at all otherwise.
 */
fun isCentroSymmetric(columns: Int, duplexes: Int): Boolean = (columns + duplexes) % 2 == 1

// ------------------------------------------------------------------ the fan

/**
 * The sum `Σ_{i=1}^{m}(i − ½)²  =  m(4m² − 1)/12` — the lever-squared sum of a fan of [interfaces]
 * equally spaced hinge lines, in units of the interhelical distance squared.
 */
fun fanLeverSum(interfaces: Int): Double {
    require(interfaces > 0) { "interfaces must be positive, was: $interfaces" }
    val m = interfaces.toDouble()
    return m * (4.0 * m * m - 1.0) / 12.0
}

/** The lever in nm from a fan's **root** hinge line to the outermost duplex axis: `(m − ½)d`. */
fun fanLever(interfaces: Int, interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET): Double {
    require(interfaces > 0) { "interfaces must be positive, was: $interfaces" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    return (interfaces - 0.5) * interhelicalDistance
}

/**
 * The equivalent **single-hinge** count of a raft hinged on [interfaces] parallel lines of
 * [perInterface] crossovers each, read at the lever [fanLever] from its root line.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`n_eff = n_i · 3(2m − 1)/(m(2m + 1))`**, exactly `n_i` at `m = 1`.
 *
 * This is the whole content of the question *"where else could sixteen crossovers come from?"*
 * They can be **assembled** — four interfaces of four is sixteen crossovers — but the interfaces
 * are in **series**, not in parallel, because each one carries only the moment of what is outboard
 * of it and turns through its own angle. Sixteen crossovers arranged that way are worth **2.33**
 * of hinge, not sixteen.
 */
fun fanEffectiveHingeCount(interfaces: Int, perInterface: Int): Double {
    require(interfaces > 0) { "interfaces must be positive, was: $interfaces" }
    require(perInterface > 0) { "perInterface must be positive, was: $perInterface" }
    val m = interfaces.toDouble()
    return perInterface * 3.0 * (2.0 * m - 1.0) / (m * (2.0 * m + 1.0))
}

/**
 * The same quantity for an arbitrary fan: hinge lines at [positions] nm from the root, carrying
 * [counts] crossovers each, with the load at [lever] nm from the root.
 *
 * `n_eff = r² / Σ_j (r − y_j)²/n_j`, which reduces to [fanEffectiveHingeCount] on a uniform fan
 * and to `n` exactly for a single line at the root.
 */
fun generalFanEffectiveCount(
    lever: Double,
    positions: List<Double>,
    counts: List<Int>
): Double {
    require(lever > 0.0) { "lever must be positive, was: $lever" }
    require(positions.isNotEmpty()) { "a fan needs at least one hinge line" }
    require(positions.size == counts.size) {
        "positions and counts must have the same size, were: " +
                "${positions.size} and ${counts.size}"
    }
    require(counts.all { it > 0 }) { "every hinge line must carry a crossover, were: $counts" }
    require(positions.all { it < lever }) {
        "every hinge line must lie inboard of the load, were: $positions against $lever"
    }
    val compliance = positions.indices.sumOf { j ->
        val arm = lever - positions[j]
        arm * arm / counts[j]
    }
    return lever * lever / compliance
}

/**
 * The bending rigidity in `pN·nm²` of the continuum strip a fan approximates: a plate strip of
 * `C-0009`'s across-helix rigidity `D_⊥ = k_θ d/p` per unit width, `perInterface` pitches wide.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`EI_⊥ = n_i k_θ d`
 */
fun continuumStripRigidity(
    perInterface: Int,
    hingeStiffness: Double,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET
): Double {
    require(perInterface > 0) { "perInterface must be positive, was: $perInterface" }
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    return perInterface * hingeStiffness * interhelicalDistance
}

/**
 * The continuum control `CLAUDE.md` requires beside every lattice claim: the fan's tip compliance
 * over that of the cantilever strip it approximates, both rooted at the same line.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`2m(2m + 1)/(2m − 1)²`** — `6.00` at one interface, `1.47` at four,
 * `1 + 3/(2m)` asymptotically, and **always above one**: the lattice fan is the *softer* of the
 * two, which is not the direction a discretisation is usually assumed to run.
 *
 * It is independent of `n_i`, of `k_θ` and of `d`, so the discreteness of a fan is a pure function
 * of how many interfaces it spans.
 */
fun fanOverContinuum(interfaces: Int): Double {
    require(interfaces > 0) { "interfaces must be positive, was: $interfaces" }
    val m = interfaces.toDouble()
    val odd = 2.0 * m - 1.0
    return 2.0 * m * (2.0 * m + 1.0) / (odd * odd)
}

/**
 * The tip stiffness in `pN/nm` of one fan flexure — its hinge lines in series, read at [fanLever].
 *
 * There is no separate arm-bending term: the raft's own across-helix bending **is** the fan, which
 * is what makes this reading different from `C-0023`'s series composition rather than a case of it.
 */
fun fanFlexureStiffness(
    interfaces: Int,
    perInterface: Int,
    hingeStiffness: Double,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET
): Double {
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    val lever = fanLever(interfaces, interhelicalDistance)
    return fanEffectiveHingeCount(interfaces, perInterface) * hingeStiffness / (lever * lever)
}
