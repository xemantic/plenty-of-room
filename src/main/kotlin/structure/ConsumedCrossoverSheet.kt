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

/**
 * `T-110` — what is left of a single-layer sheet once a flexure array has spent its crossovers
 * as hinges.
 *
 * ## The geometric decision this file encodes, stated rather than assumed
 *
 * `C-0046`'s surviving `E5a` designs spend **45, 50 or 56** of the tile's **49–56** crossovers as
 * flexure hinges, and nothing has priced what the sheet has left. Whether that price is payable
 * turns on a question no upstream claim asks: **is a crossover used as a hinge still an interface
 * crossover for the sheet?**
 *
 * `C-0040` fixes the answer, and it does so before any model runs. A hinge line is *"a maximal
 * set of crossovers that share **one interface** and **one pair of bodies**"*, and `n k_θ` is the
 * right torsional spring *"for a hinge whose axis runs along `x`, and for no other axis"* —
 * because `k_θ` is the **interhelical dihedral** constant, resisting rotation of duplex `b+1`
 * relative to duplex `b` about their common interface line. A hinge that turns is therefore a
 * hinge between **two different bodies**: whatever is outboard of the line has left the sheet.
 * A crossover cannot simultaneously join duplex `b` to duplex `b+1` and join duplex `b` to a
 * flexure arm — a reciprocal exchange has two strands and two partners, and the counting theorem
 * `C-0029` established at a duplex *end* applies to a junction *site* just as hard.
 *
 * So the two uses are **exclusive at the site**, and this file models exactly that: a consumed
 * crossover supplies **neither** the dihedral spring **nor** the vertical link
 * ([OrigamiGrillage.consumedCrossovers]).
 *
 * ## Why the consumed sheet is not just a softer sheet
 *
 * Bending a sheet across the helices is fourteen hinge lines in **series**, so the effective
 * across-helix rigidity is a *harmonic* mean over the interfaces and not an arithmetic one. The
 * two conventions ([uniformCurvatureRigidity], [uniformMomentRigidity]) agree on a uniform
 * lattice up to `(D/(D−1))²` and part company completely on a depleted one: the arithmetic
 * average degrades linearly, the harmonic one goes to **exactly zero** the moment any interface
 * empties. The lattice sees the second and a smeared continuum plate can only express the first,
 * which is why `CLAUDE.md`'s *"run the continuum plate beside it and quote the excess"* is not a
 * formality here.
 */

// ---------------------------------------------------------------------------- the site

/**
 * One crossover of the lattice, as a **site** rather than as geometry: which interface it serves
 * and which crossover column it sits in.
 *
 * Two integers, because that is what a design chooses. The `x` and `y` follow from the layout
 * and are held by [OrigamiGrillage.Crossover].
 *
 * @param lowerBeam the index of the lower-`y` duplex of the interface it joins.
 * @param column the index of the crossover column it sits in.
 */
@Serializable
data class CrossoverSite(
    val lowerBeam: Int,
    val column: Int
)

// ---------------------------------------------------------------------------- the pigeonhole

/**
 * The largest number of crossovers that can be spent as hinges while leaving the sheet in **one
 * piece**: `N − (D − 1)`.
 *
 * The interfaces of a single-layer sheet form a **path** graph on its duplexes — duplex `b` is
 * adjacent to `b−1` and `b+1` and to nothing else — so a connected sheet needs at least one
 * retained crossover on **every** interface, and there are `D − 1` of them. That is the whole
 * derivation, it is a pigeonhole, and no force field, sequence or lattice can move it.
 *
 * At the Gen-1 tile's 56 crossovers and 15 duplexes it is **42**, i.e. **75.0 %**; at the 49 of
 * the twenty-two seven-column phases it is **35**, i.e. **71.4 %**.
 *
 * @param inventory the crossover count of the whole sheet.
 * @param duplexes the number of duplexes, at least two.
 */
fun maximumConsumedForConnectivity(inventory: Int, duplexes: Int): Int {
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    require(inventory >= duplexes - 1) {
        "an inventory of $inventory cannot connect $duplexes duplexes at all"
    }
    return inventory - (duplexes - 1)
}

/** [maximumConsumedForConnectivity] as a fraction of the inventory. */
fun maximumConsumedFractionForConnectivity(inventory: Int, duplexes: Int): Double =
    maximumConsumedForConnectivity(inventory, duplexes).toDouble() / inventory

// ---------------------------------------------------------------------------- the patterns

/**
 * Where the consumed crossovers are taken from.
 *
 * `C-0015`'s lesson is to sweep **shapes** rather than counts, and consumption has a shape: the
 * same number of hinges can be drawn from the sheet in ways that differ by whether the sheet
 * survives at all.
 */
enum class ConsumptionPattern(val label: String) {

    /**
     * Round-robin over the interfaces — one crossover retained on each interface before any
     * interface keeps a second. The design that keeps the sheet connected for as long as
     * arithmetic allows, and the pattern that makes [maximumConsumedForConnectivity] **tight**.
     */
    SPREAD("round robin over the interfaces (the connectivity-optimal design)"),

    /**
     * Whole interfaces at a time, from the low-`y` edge inward — `C-0040`'s `L3` reading, in
     * which one flexure owns a whole hinge line of four.
     */
    INTERFACE_FIRST("whole hinge lines, one interface at a time (C-0040's L3)"),

    /**
     * Whole crossover columns at a time, from the low-`x` edge inward — the reading in which a
     * flexure array is laid out along the helices.
     */
    COLUMN_FIRST("whole crossover columns, one column at a time")
}

/**
 * The sites **retained** when [consumed] of [inventory] are spent as hinges under [pattern].
 *
 * Deterministic in every case: no random draw enters this project's result files.
 */
fun retainedSites(
    inventory: List<CrossoverSite>,
    consumed: Int,
    pattern: ConsumptionPattern
): Set<CrossoverSite> {
    require(consumed >= 0) { "consumed must not be negative, was: $consumed" }
    require(consumed <= inventory.size) {
        "cannot consume $consumed of an inventory of ${inventory.size}"
    }
    val retainedCount = inventory.size - consumed
    return when (pattern) {
        ConsumptionPattern.SPREAD -> {
            val rank = mutableMapOf<Int, Int>()
            inventory
                .sortedWith(compareBy({ it.lowerBeam }, { it.column }))
                .map { site ->
                    val position = rank.getOrDefault(site.lowerBeam, 0)
                    rank[site.lowerBeam] = position + 1
                    Triple(position, site.lowerBeam, site)
                }
                .sortedWith(compareBy({ it.first }, { it.second }))
                .take(retainedCount)
                .map { it.third }
        }
        ConsumptionPattern.INTERFACE_FIRST -> inventory
            .sortedWith(compareBy({ it.lowerBeam }, { it.column }))
            .takeLast(retainedCount)
        ConsumptionPattern.COLUMN_FIRST -> inventory
            .sortedWith(compareBy({ it.column }, { it.lowerBeam }))
            .takeLast(retainedCount)
    }.toSet()
}

/** The complement of [retainedSites] — the sites a design has spent. */
fun consumedSites(
    inventory: List<CrossoverSite>,
    consumed: Int,
    pattern: ConsumptionPattern
): Set<CrossoverSite> = inventory.toSet() - retainedSites(inventory, consumed, pattern)

// ---------------------------------------------------------------------------- connectivity

/** How many of [retained] sit on each of the `D − 1` interfaces, in interface order. */
fun retainedPerInterface(
    retained: Collection<CrossoverSite>,
    duplexes: Int
): List<Int> {
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    val counts = MutableList(duplexes - 1) { 0 }
    retained.forEach { site ->
        require(site.lowerBeam in 0 until duplexes - 1) {
            "site $site is not an interface of a $duplexes-duplex sheet"
        }
        counts[site.lowerBeam] += 1
    }
    return counts
}

/**
 * The number of connected components of the sheet under [retained], by **union-find** over the
 * duplexes.
 *
 * Written as a union-find rather than as `1 + (empty interfaces)` on purpose: the closed form is
 * a theorem about the interfaces forming a path graph, and the two are asserted equal as a gate-3
 * test at every consumption level of every pattern. Nothing in the union-find knows about the
 * path.
 */
fun sheetComponents(retained: Collection<CrossoverSite>, duplexes: Int): Int {
    require(duplexes >= 1) { "duplexes must be at least one, was: $duplexes" }
    val parent = IntArray(duplexes) { it }
    fun find(node: Int): Int {
        var root = node
        while (parent[root] != root) root = parent[root]
        var walk = node
        while (parent[walk] != root) {
            val next = parent[walk]
            parent[walk] = root
            walk = next
        }
        return root
    }
    retained.forEach { site ->
        require(site.lowerBeam in 0 until duplexes - 1) {
            "site $site is not an interface of a $duplexes-duplex sheet"
        }
        val a = find(site.lowerBeam)
        val b = find(site.lowerBeam + 1)
        if (a != b) parent[a] = b
    }
    return (0 until duplexes).count { find(it) == it }
}

// ---------------------------------------------------------------------------- the rigidity

/**
 * The across-helix bending rigidity in `pN·nm` on the **uniform-curvature** (Voigt) convention:
 * the energy an imposed `w = ½κy²` field costs, divided by `½κ²` and by the footprint.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`D_⊥ = k_θ d² N_ret / A`
 *
 * This is `C-0009`'s own gate-2 identity — it returns **3.397 pN·nm** at the Gen-1 tile's 56
 * crossovers, exactly `56/55.147` of the continuum `k_θ d/p` — and it is **linear in the retained
 * count**. It is what a continuum plate can express, and it is an **upper** bound: an imposed
 * uniform curvature is a kinematic restriction, so the real depleted sheet is softer.
 */
fun uniformCurvatureRigidity(
    retainedCount: Int,
    hingeStiffness: Double,
    interhelicalDistance: Double,
    area: Double
): Double {
    require(retainedCount >= 0) { "retainedCount must not be negative, was: $retainedCount" }
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(area > 0.0) { "area must be positive, was: $area" }
    return hingeStiffness * interhelicalDistance * interhelicalDistance * retainedCount / area
}

/**
 * The across-helix bending rigidity in `pN·nm` on the **uniform-moment** (Reuss) convention —
 * the interfaces in **series**, which is how a sheet actually bends across the helices.
 *
 * Under a moment `m` per unit length along `x`, interface `i` turns through
 * `Δφ_i = m L_x/(n_i k_θ)`, and the total rotation across the sheet is `κ L_y`. Hence
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`D_⊥ = L_y k_θ /(L_x Σ_i 1/n_i)`**,
 *
 * which for a uniform lattice is exactly `(D/(D−1))²` times [uniformCurvatureRigidity] — the same
 * `(n−1)/n` duplex-count residual `C-0009` isolated, squared, and asserted rather than tolerated.
 *
 * **An empty interface is a free hinge**, so a single `n_i = 0` makes the series compliance
 * infinite and the rigidity **exactly zero**. That is returned as `0.0` and not as an infinity or
 * a `NaN`: `kotlinx.serialization` refuses both, and zero is the physically correct value of the
 * rigidity itself.
 */
fun uniformMomentRigidity(
    retainedPerInterface: List<Int>,
    hingeStiffness: Double,
    lengthX: Double,
    lengthY: Double
): Double {
    require(retainedPerInterface.isNotEmpty()) { "retainedPerInterface must not be empty" }
    require(retainedPerInterface.all { it >= 0 }) {
        "a retained count must not be negative, were: $retainedPerInterface"
    }
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(lengthX > 0.0) { "lengthX must be positive, was: $lengthX" }
    require(lengthY > 0.0) { "lengthY must be positive, was: $lengthY" }
    if (retainedPerInterface.any { it == 0 }) return 0.0
    val compliance = retainedPerInterface.sumOf { 1.0 / it }
    return lengthY * hingeStiffness / (lengthX * compliance)
}

/**
 * The sheet's bending anisotropy `D_∥/D_⊥`, with the **sentinel** [ANISOTROPY_UNBOUNDED] where
 * the across-helix rigidity has gone to zero.
 *
 * `CLAUDE.md`: `kotlinx.serialization` refuses `Infinity` as well as `NaN`, and *"a margin of
 * `Infinity` is not a margin, it is the absence of a requirement"* — here it is the absence of a
 * sheet, which is worth saying in a sentinel rather than hiding in a large number.
 */
fun bendingAnisotropy(alongHelices: Double, acrossHelices: Double): Double {
    require(alongHelices > 0.0) { "alongHelices must be positive, was: $alongHelices" }
    require(acrossHelices >= 0.0) { "acrossHelices must not be negative, was: $acrossHelices" }
    return if (acrossHelices <= 0.0) ANISOTROPY_UNBOUNDED else alongHelices / acrossHelices
}

/** Reported for `D_∥/D_⊥` where `D_⊥` is exactly zero — the sheet has no across-helix rigidity. */
const val ANISOTROPY_UNBOUNDED: Double = -1.0
