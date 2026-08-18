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
import com.xemantic.nano.plentyofroom.structure.CrossoverSite
import com.xemantic.nano.plentyofroom.structure.CrossoverSoftening
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs

/**
 * `T-164` — how stiff is a **row-end** crossover, and does the 38.08 nm tile's flatness verdict
 * change inside the range that stiffness can take?
 *
 * ## What is settled and what is not
 *
 * `C-0095` settled that the crossover **exists**: it is the raster turn, caDNAno automates it, and
 * Rothemund's 24-helix rectangle is 288 bp = 18 column pitches exactly, both edges on the crossover
 * lattice, 90 % well-formed. `C-0090` then gives all 14 of them `C-0009`'s interior `k_θ` **and**
 * vertical link. What is **not** settled is the stiffness: Rothemund states in print that at an
 * edge on the crossover lattice *"a crossover involving staple strands is in tension with an
 * adjacent crossover involving the scaffold strand"* and that *"how the strain is actually relieved
 * is unknown, the final base pairs of each helix may be distorted"*.
 *
 * ## The cheap bound, which is a count and a distinction and runs before any solve
 *
 * 1. **A ceiling that is a count.** `Gen1Tile.crossoverHingeStiffness(α) = 2 α B/(100 a)` is
 *    Chen et al.'s **two** softened phosphate bonds in parallel, and `C-0029`'s counting theorem
 *    says a duplex end has exactly two strand termini and no force field can add a third. So a
 *    row-end crossover cannot carry **more** bonds than an interior one and
 *    [softeningOfBondCount] `≤ 1` — exactly, and independently of any elasticity model. The only
 *    other integer rung of the same ladder is a single-strand turn, `s = 1/2`.
 * 2. **A floor on the OTHER element, which is a distinction rather than a number.** A crossover is
 *    two elements (`CLAUDE.md`, `C-0009`) and only one of them is elastic; the vertical link is a
 *    *constraint* expressing that the backbone is covalently continuous across the interface.
 *    Rothemund's own remedy — *"one or two scaffold bases could be left unpaired and allowed to
 *    form a hairpin that should relax the crossover"* — adds slack to the **torsion**, not to the
 *    connectivity. So the physically reachable set is [CrossoverSoftening.ofHinge] over `s ∈ [0, 1]`
 *    with the link intact, and `C-0090`'s *refused* reading, which deletes the link and the node
 *    as well, is **outside** it.
 * 3. **An unrelieved strain is a PRESTRAIN, not a compliance.** Rothemund's passage describes a
 *    static configuration; it does not by itself move a linear elastic constant. Which is why the
 *    deliverable is `P-6`'s ceiling-plus-threshold and not a value.
 *
 * Conventions: lengths **nm**, rotational stiffness **pN·nm/rad**, `s` dimensionless;
 * `x` along the helices, `y` across them, `z` normal and positive **upward**.
 */

/** The phosphate bonds one antiparallel crossover carries — Chen et al.'s **two**, in parallel. */
const val CROSSOVER_PHOSPHATE_BONDS: Int = 2

/**
 * The dihedral spring in `pN·nm/rad` of a crossover carrying [bonds] softened phosphate bonds.
 *
 * Chen et al. (*JACS* **136**:6995, SI §S2) put `k₂ = α B/(100 a)` on **one** crossover phosphate
 * bond and an antiparallel crossover carries two of them in parallel, which is
 * `Gen1Tile.crossoverHingeStiffness`. Written here as a function of the **count**, because the
 * count is the only thing about a row-end crossover that a bond census can bound.
 */
fun hingeStiffnessOfBondCount(bonds: Int, alpha: Double = 1.0): Double {
    require(bonds >= 0) { "bonds must not be negative, was: $bonds" }
    require(alpha > 0.0) { "alpha must be positive, was: $alpha" }
    return bonds * alpha * Gen1Tile.DUPLEX_BENDING_RIGIDITY /
            (100.0 * Gen1Tile.RISE_PER_BASE_PAIR)
}

/**
 * The softening `s = k_θ(bonds)/k_θ(interior)` a bond count implies — `bonds/2`, exactly.
 *
 * `α` cancels, and so does every elastic constant: this is the ratio of two counts.
 */
fun softeningOfBondCount(bonds: Int): Double {
    require(bonds >= 0) { "bonds must not be negative, was: $bonds" }
    return bonds.toDouble() / CROSSOVER_PHOSPHATE_BONDS
}

/**
 * The indices of the columns of [layout] that sit **on** the row end of a tile of edge [edgeX].
 *
 * `rasterJunctionPlanes` insets a plane lying on the row end by [inset] so that it cannot seed a
 * zero-length beam element, so a row-end column is one at `|x| = edgeX/2 − inset`. Empty whenever
 * the row-end column is refused, and empty at any width that is not a whole number of column
 * pitches — which is `C-0090`'s congruence, read off the layout rather than recomputed.
 */
fun rowEndColumnIndices(
    layout: CrossoverLayout,
    edgeX: Double,
    inset: Double = CrossoverLayout.EDGE_MARGIN,
    tolerance: Double = 1.0e-9
): List<Int> {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(inset > 0.0) { "inset must be positive, was: $inset" }
    val onEnd = edgeX / 2.0 - inset
    return layout.positions.indices.filter { abs(abs(layout.positions[it]) - onEnd) <= tolerance }
}

/**
 * The crossover sites of the row-end columns of [layout] on a sheet of [duplexes] duplexes.
 *
 * The parity rule is `OrigamiGrillage`'s own — interface `b` carries the columns whose parity
 * matches `b mod 2` — so this returns exactly the subset of that lattice's crossovers which sit at
 * a duplex terminus. At 38.08 nm and phase 8 or 24 it is **14**, one per interface, and `C-0095`
 * shows every one of them is a scaffold raster turn.
 */
fun rowEndCrossoverSites(
    layout: CrossoverLayout,
    edgeX: Double,
    duplexes: Int,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): List<CrossoverSite> {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    val columns = rowEndColumnIndices(layout, edgeX, inset)
    return (0 until duplexes - 1).flatMap { beam ->
        columns.filter { (layout.parities[it] + beam) % 2 == 0 }
            .map { CrossoverSite(beam, it) }
    }
}

/** [sites] all softened by the same [softening] — the map an [OrigamiGrillage] takes. */
fun softeningMap(
    sites: List<CrossoverSite>,
    softening: CrossoverSoftening
): Map<CrossoverSite, CrossoverSoftening> = sites.associateWith { softening }

/**
 * Rebuilds the placement an [UpwardArmPlacement.key] names, so that `C-0090`'s published optimum
 * can be re-evaluated rather than re-searched.
 *
 * The key is `row:x₁,x₂;row:…` with each `x` in nanometres times `1e6`, rounded — which is
 * `C-0063`'s own canonical form and therefore exact on the 0.34 nm lattice the roots live on.
 */
fun placementFromKey(
    key: String,
    phaseBasePairs: Int,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): UpwardArmPlacement {
    require(key.isNotBlank()) { "key must not be blank" }
    val rows = key.split(";").map { entry ->
        val (row, roots) = entry.split(":", limit = 2)
        val positions = roots.split(",").map { it.trim().toDouble() / 1.0e6 }
        UpwardArmRow(
            row = row.trim().toInt(),
            roots = positions,
            towardPositiveX = requireNotNull(armDirections(positions, arm, edgeX, width)) {
                "the placement $key names row $row roots $positions, which admit no arm directions"
            }
        )
    }
    return UpwardArmPlacement(phaseBasePairs, rows)
}

/** The bracket [bisectedCrossing] returns: the last softening below the target and the first above. */
data class SofteningCrossing(
    val below: Double,
    val above: Double,
    val valueBelow: Double,
    val valueAbove: Double,
    val evaluations: Int
) {

    /** The midpoint of the bracket, in units of an interior crossover's dihedral spring. */
    val midpoint: Double get() = (below + above) / 2.0

    /** The bracket width, which is what a crossing is determined to. */
    val width: Double get() = abs(above - below)

}

/**
 * Brackets the softening at which [value] crosses [target], by plain bisection over
 * `[low, high]` in a declared number of [steps].
 *
 * Bisection rather than a secant method **on purpose**: [value] here is a minimum over an
 * exhaustively enumerated family, so it is continuous but not smooth, and `CLAUDE.md` records that
 * a safeguarded secant exiting on bracket width stalls. A declared step count also makes the cost
 * of the search predictable, which matters when one evaluation is 163 296 placements.
 *
 * [value] must be **decreasing** through the crossing — a stiffer row-end crossover cannot dish
 * more — which is checked at the two endpoints and reported rather than assumed; where the
 * endpoints do not straddle [target] this throws, because there is no crossing to quote.
 */
fun bisectedCrossing(
    low: Double,
    high: Double,
    target: Double,
    steps: Int,
    value: (Double) -> Double
): SofteningCrossing {
    require(high > low) { "the bracket must have positive width, was: [$low, $high]" }
    require(steps >= 1) { "steps must be at least one, was: $steps" }
    val atLow = value(low)
    val atHigh = value(high)
    require((atLow - target) * (atHigh - target) <= 0.0) {
        "the bracket [$low, $high] does not straddle $target: $atLow and $atHigh"
    }
    var lower = low
    var upper = high
    var valueLower = atLow
    var valueUpper = atHigh
    var evaluations = 2
    repeat(steps) {
        val middle = (lower + upper) / 2.0
        val here = value(middle)
        evaluations++
        if ((valueLower - target) * (here - target) <= 0.0) {
            upper = middle
            valueUpper = here
        } else {
            lower = middle
            valueLower = here
        }
    }
    return SofteningCrossing(lower, upper, valueLower, valueUpper, evaluations)
}
