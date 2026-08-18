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

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * `T-190` — the **interior** crossovers of `C-0107`'s graded corrugated field.
 *
 * ## Why this is arithmetic and not a model
 *
 * A crossover prestrain is an **initial stress**, so `½k_θ(Δφ − θ₀)²` leaves the quadratic term
 * untouched and enters the lattice as a *load vector* (`C-0104`). The solved field is therefore
 * **exactly linear** in the prestrain map, and a partition of the sites into two disjoint sets is
 * a partition of the response into two fields that sum to it identically. Nothing here is an
 * approximation: what needs measuring is not whether the split exists but what the two halves do
 * to a quantity that is **not** additive — peak dishing, which is a seminorm of the field.
 *
 * ## What the interior sites carry
 *
 * `C-0107`'s boundary layer is a **field** `u(x)`, odd about the row centre, and its corrugated
 * form `(−1)^b u(x)` is defined at *every* crossover station, not only at the row ends. The row
 * ends are simply where `|u|` is largest. The 42 interior sites are therefore not an unmodelled
 * addition to `C-0107`'s model — they are the same model read at the stations it already covers,
 * and it is the **14-site idealisation** that has nothing behind it.
 *
 * Units: angles **rad**, couples **pN·nm**, stiffness **pN·nm/rad**, lengths **nm**.
 */

/** A lattice's crossover inventory split into the sites at a duplex terminus and the rest. */
data class CrossoverPartition(
    val rowEnd: List<CrossoverSite>,
    val interior: List<CrossoverSite>
)

/**
 * [all] split into the [rowEnd] sites and everything else, order preserved.
 *
 * @throws IllegalArgumentException if [rowEnd] names a site the inventory does not have.
 */
fun partitionRowEnd(
    all: List<CrossoverSite>,
    rowEnd: Collection<CrossoverSite>
): CrossoverPartition {
    val inventory = all.toSet()
    require(inventory.containsAll(rowEnd)) {
        "every row-end site must be in the inventory; missing: ${rowEnd - inventory}"
    }
    val ends = rowEnd.toSet()
    return CrossoverPartition(
        rowEnd = all.filter { it in ends },
        interior = all.filterNot { it in ends }
    )
}

/**
 * The graded corrugated prestrain `sign · (−1)^b u(x)` over [sites], in radians.
 *
 * [x] gives each site's station measured from the **row centre**; [sign] is the overall sign of
 * the corrugation, which nothing in this repository fixes — the glide symmetry says the field
 * alternates with the interface index and does not say which parity folds which way — so it is a
 * swept axis and never a default.
 */
fun corrugatedPrestrainField(
    model: EdgeTwistRelief,
    mismatch: Double,
    sites: Iterable<CrossoverSite>,
    x: (CrossoverSite) -> Double,
    sign: Double = 1.0
): Map<CrossoverSite, Double> {
    require(sign == 1.0 || sign == -1.0) { "sign must be +1 or -1, was: $sign" }
    return sites.associateWith { site ->
        sign * corrugatedPrestrain(model, mismatch, site.lowerBeam, x(site))
    }
}

/** [field] restricted to [sites] — every other site carries nothing, which is not the same map. */
fun restrictPrestrains(
    field: Map<CrossoverSite, Double>,
    sites: Collection<CrossoverSite>
): Map<CrossoverSite, Double> {
    val kept = sites.toSet()
    return field.filterKeys { it in kept }
}

/**
 * What a prestrain map applies to a lattice: how many sites, the **net** couple (which a rigid
 * translation cannot react and a cylinder can), the **absolute** couple (the eigenstrain's own
 * size, indifferent to cancellation) and the largest angle in degrees.
 */
data class PrestrainLedger(
    val sites: Int,
    val netCouple: Double,
    val absoluteCouple: Double,
    val peakDegrees: Double
)

/** [field]'s ledger at a hinge stiffness of [hingeStiffness] pN·nm/rad. */
fun prestrainLedger(
    field: Map<CrossoverSite, Double>,
    hingeStiffness: Double
): PrestrainLedger {
    require(hingeStiffness > 0.0 && hingeStiffness.isFinite()) {
        "hingeStiffness must be a positive finite pN nm/rad, was: $hingeStiffness"
    }
    return PrestrainLedger(
        sites = field.size,
        netCouple = hingeStiffness * field.values.sum(),
        absoluteCouple = hingeStiffness * field.values.sumOf { abs(it) },
        peakDegrees = (field.values.maxOfOrNull { abs(it) } ?: 0.0) * 180.0 / PI
    )
}

/**
 * The uniform angle [field] carries, or `null` if its entries do not all agree to [tolerance].
 *
 * A **measurement**, not an assertion: whether the row-end restriction of a graded field is one of
 * `C-0104`'s uniform distributions is a lattice fact that has to come out of the arithmetic, and
 * it is exactly the fact `C-0107`'s prose and `C-0107`'s solve disagree about.
 */
fun uniformValueOrNull(
    field: Map<CrossoverSite, Double>,
    tolerance: Double = 1.0e-12
): Double? {
    require(tolerance >= 0.0) { "tolerance must not be negative, was: $tolerance" }
    if (field.isEmpty()) return null
    val first = field.values.first()
    return if (field.values.all { abs(it - first) <= tolerance * (1.0 + abs(first)) }) first
    else null
}

/**
 * The centro-symmetric partner of [site] on a lattice of [duplexes] duplexes and [columns]
 * columns — `b → duplexes − 2 − b`, `c → columns − 1 − c`.
 *
 * A Rothemund sheet is **centro-symmetric, not mirror-symmetric** (`CLAUDE.md`), so this is the
 * only reflection its crossover lattice admits, and it is the one under which the graded field is
 * invariant: `u` is odd in `x` and `(−1)^(D−2−b) = −(−1)^b` whenever `D` is odd.
 */
fun centroSymmetricPartner(
    site: CrossoverSite,
    duplexes: Int,
    columns: Int
): CrossoverSite {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(columns >= 1) { "columns must be at least 1, was: $columns" }
    require(site.lowerBeam in 0..(duplexes - 2)) {
        "the interface index must lie in 0..${duplexes - 2}, was: ${site.lowerBeam}"
    }
    require(site.column in 0 until columns) {
        "the column index must lie in 0 until $columns, was: ${site.column}"
    }
    return CrossoverSite(duplexes - 2 - site.lowerBeam, columns - 1 - site.column)
}

/**
 * `⟨a, b⟩ / (‖a‖ ‖b‖)` from the three inner products, in `[−1, 1]`.
 *
 * Peak dishing is a **seminorm** and does not add; the area inner product does, exactly, and
 * `‖a + b‖² = ‖a‖² + 2⟨a, b⟩ + ‖b‖²`. So the only convention-free statement of a *cancellation*
 * between two load-path contributions is the cosine of their two fields, and `CLAUDE.md` already
 * asks for it in the other place where two responses have to be served at once: *"run the cosine
 * before the optimiser"*.
 */
fun cosineFromInnerProducts(aa: Double, bb: Double, ab: Double): Double {
    require(aa > 0.0 && aa.isFinite()) { "aa must be a positive finite norm, was: $aa" }
    require(bb > 0.0 && bb.isFinite()) { "bb must be a positive finite norm, was: $bb" }
    require(ab.isFinite()) { "ab must be finite, was: $ab" }
    return ab / sqrt(aa * bb)
}
