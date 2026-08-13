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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.nano.plentyofroom.anchoring.FreelyJointedChain
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * Task `T-16` — the **supply** side: composing DNA elements into one output coupling, and
 * finding which of them is the compliance that decides it. Leaf `A8.2`'s explicit ask,
 * *"identify the dominant compliance term … and budget stiffness at the joints"*.
 *
 * The elements themselves are `C-0014`'s and are consumed from `anchoring/` unchanged — this
 * task owns none of them. What is added here is the **series composition**, because a
 * coupling is a chain from the tile to ground and `C-0014`'s `S5` already showed what one
 * soft element in such a chain does: a factor of 36.
 */

// ---------------------------------------------------------------- composition

/** The series stiffness of [parts] in `pN/nm` — compliances add, so the softest dominates. */
fun seriesStiffness(parts: List<Double>): Double {
    require(parts.isNotEmpty()) { "parts must not be empty" }
    parts.forEach { require(it > 0.0) { "every part must be positive, was: $it" } }
    return 1.0 / parts.sumOf { 1.0 / it }
}

/** The parallel stiffness of [parts] in `pN/nm` — stiffnesses add. */
fun parallelStiffness(parts: List<Double>): Double {
    require(parts.isNotEmpty()) { "parts must not be empty" }
    parts.forEach { require(it > 0.0) { "every part must be positive, was: $it" } }
    return parts.sum()
}

/** Each element's share of the series chain's total compliance — dimensionless, sums to 1. */
fun complianceShares(parts: List<Double>): List<Double> {
    require(parts.isNotEmpty()) { "parts must not be empty" }
    parts.forEach { require(it > 0.0) { "every part must be positive, was: $it" } }
    val total = parts.sumOf { 1.0 / it }
    return parts.map { (1.0 / it) / total }
}

/**
 * The element of [chain] carrying the largest share of the compliance — leaf `A8.2`'s answer
 * for whatever chain it is applied to.
 *
 * This is an **argmax**, which `CLAUDE.md` records as the one thing rounding at the
 * serialisation boundary does not make reproducible. The comparison is therefore made on
 * *already rounded* compliances, with the **first** index winning any tie.
 */
fun dominantCompliance(chain: List<Pair<String, Double>>): Pair<String, Double> {
    require(chain.isNotEmpty()) { "chain must not be empty" }
    var best = chain[0]
    var bestCompliance = decisionRounded(1.0 / chain[0].second)
    chain.drop(1).forEach { candidate ->
        val compliance = decisionRounded(1.0 / candidate.second)
        if (compliance > bestCompliance) {
            best = candidate
            bestCompliance = compliance
        }
    }
    return best
}

/** Six significant digits, the level a *decision* is taken at — far above any FP noise. */
private fun decisionRounded(value: Double): Double {
    if (!value.isFinite() || value == 0.0) return value
    val scale = 10.0.pow(5 - floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

// ---------------------------------------------------------------- the lever

/**
 * The stiffness in `pN/nm` an output load of [outputStiffness] presents **at the tile** through
 * a lever of ratio [ratio] = (output travel)/(input travel): `k = r² k_out`.
 *
 * The lever is a stiffness transformer, so a soft external load seen through a large ratio is
 * a stiff coupling and vice versa. §1's effort point sits 20–25 nm above the electrode against
 * a 5–10 nm layer, so `r` is of order 2–5 and `r²` of order 4–25.
 */
fun leverReflectedStiffness(outputStiffness: Double, ratio: Double): Double {
    require(outputStiffness >= 0.0) {
        "outputStiffness must not be negative, was: $outputStiffness"
    }
    require(ratio > 0.0) { "ratio must be positive, was: $ratio" }
    return ratio * ratio * outputStiffness
}

/**
 * The linear stiffness in `pN/nm` a rotational joint of [rotational] `pN·nm/rad` presents at
 * a lever arm of [radius] nm: `k = k_θ/r²`.
 *
 * The fulcrum is a **joint**, and leaf `A8.2` says the joints are where the compliance lives.
 */
fun jointStiffnessAtRadius(rotational: Double, radius: Double): Double {
    require(rotational > 0.0) { "rotational must be positive, was: $rotational" }
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    return rotational / (radius * radius)
}

/**
 * The rotational stiffness in `pN·nm/rad` of a fulcrum built from [count] duplexes of stretch
 * modulus [stretchModulus] pN and length [length] nm, their axes offset [offset] nm either
 * side of the pivot: `k_θ = n S d²/L`.
 *
 * This is a **tension-member** model of a hinge and it deliberately avoids the crossover
 * torsional constant `k_θ` that `C-0009` fits and `T-9` has not produced: a fulcrum built as
 * duplexes in tension and compression about a pivot needs no crossover bending at all.
 */
fun tensionFulcrumRotationalStiffness(
    count: Int,
    stretchModulus: Double,
    offset: Double,
    length: Double
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(offset > 0.0) { "offset must be positive, was: $offset" }
    require(length > 0.0) { "length must be positive, was: $length" }
    return count * stretchModulus * offset * offset / length
}

// ---------------------------------------------------------------- placement

/**
 * The attachment positions in nm, measured from the tile's centre, of a [columns] × [rows]
 * grid filling a [edgeX] × [edgeY] tile with equal tributary areas.
 *
 * `C-0015`'s flatness scheme is **45 as 3 × 15** — three stations along each of the fifteen
 * duplexes — and the shape matters, not the count: the sheet is 25.6× stiffer along the
 * helices than across them.
 */
fun attachmentGrid(
    columns: Int,
    rows: Int,
    edgeX: Double,
    edgeY: Double
): List<Pair<Double, Double>> {
    require(columns > 0) { "columns must be positive, was: $columns" }
    require(rows > 0) { "rows must be positive, was: $rows" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(edgeY > 0.0) { "edgeY must be positive, was: $edgeY" }
    return (0 until rows).flatMap { row ->
        (0 until columns).map { column ->
            val x = edgeX * (column + 0.5) / columns - edgeX / 2.0
            val y = edgeY * (row + 0.5) / rows - edgeY / 2.0
            x to y
        }
    }
}

/** `k_yaw = Σ k_i r_i²` in `pN·nm/rad` for anchors of equal [perAnchor] stiffness at [grid]. */
fun yawStiffness(perAnchor: Double, grid: List<Pair<Double, Double>>): Double {
    require(perAnchor >= 0.0) { "perAnchor must not be negative, was: $perAnchor" }
    require(grid.isNotEmpty()) { "grid must not be empty" }
    return perAnchor * grid.sumOf { (x, y) -> x * x + y * y }
}

// ---------------------------------------------------------------- the spacer

/**
 * The contour length in nm of an ssDNA spacer such that [count] of them, each stretched by
 * [targetStroke], carry [targetForce] in total.
 *
 * **Exact in one evaluation, with no root find**: at fixed tension the freely-jointed chain's
 * extension is exactly proportional to its contour length, so `L_c = δ / L(u)` with `L` the
 * Langevin function evaluated on a unit-contour chain at the per-path tension.
 */
fun spacerContourForTarget(
    kuhnLength: Double,
    count: Int,
    targetForce: Double,
    targetStroke: Double
): Double {
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(count > 0) { "count must be positive, was: $count" }
    require(targetForce > 0.0) { "targetForce must be positive, was: $targetForce" }
    require(targetStroke > 0.0) { "targetStroke must be positive, was: $targetStroke" }
    val perPath = targetForce / count
    val reduced = FreelyJointedChain(1.0, kuhnLength).extension(perPath)
    return targetStroke / reduced
}

/**
 * The longest contour in nm at which [count] Gaussian spacers still reach [requiredStiffness]:
 * `L_c b ≤ 3 N k_BT/k_req`.
 *
 * `C-0014`'s design rule, restated for the normal direction. It is a **ceiling**, not a
 * "short and stiff" instruction, and the chain's own strain stiffening makes the true ceiling
 * slightly longer than this Gaussian one.
 */
fun gaussianContourCeiling(
    kuhnLength: Double,
    count: Int,
    requiredStiffness: Double
): Double {
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(count > 0) { "count must be positive, was: $count" }
    require(requiredStiffness > 0.0) {
        "requiredStiffness must be positive, was: $requiredStiffness"
    }
    return 3.0 * count * thermalEnergy() / (requiredStiffness * kuhnLength)
}

// ---------------------------------------------------------------- the lever beam

/**
 * The three end conditions a lever beam spanning the tile can plausibly have, with the `c` in
 * `k = c EI/L³`.
 *
 * Carried as a set rather than chosen, for the same reason `C-0014` carries both of its strut
 * end conditions: an origami-to-frame joint is not obviously any one of them, and here they
 * span a factor of **25.6**, which is larger than every other spread in this task.
 */
enum class LeverSupport(val stiffnessFactor: Double, val description: String) {

    /** Cantilever, load at the tip: `3EI/L³`. The softest reading, and the conservative one. */
    CANTILEVER_TIP_LOAD(3.0, "cantilever, load concentrated at the tip"),

    /** Cantilever under a uniformly distributed load, tip deflection `qL⁴/8EI`. */
    CANTILEVER_DISTRIBUTED_LOAD(8.0, "cantilever, uniformly distributed load"),

    /** Simply supported at both ends under a UDL, centre deflection `5qL⁴/384EI`. */
    SIMPLY_SUPPORTED_DISTRIBUTED_LOAD(384.0 / 5.0, "simply supported, uniformly distributed load")
}

/**
 * The bending rigidity in `pN·nm²` a lever beam of [span] nm needs to present [stiffness]
 * `pN/nm` at the tile under [support]: `EI = k L³/c`.
 *
 * This is leaf `A8.2`'s *"budget stiffness at the joints"*, written as a **section
 * requirement** rather than as a number, because no lever geometry is specified anywhere in
 * §1 or §3 and inventing one would be the failure mode `C-0016` warns about.
 */
fun requiredBendingRigidity(
    stiffness: Double,
    span: Double,
    support: LeverSupport
): Double {
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    require(span > 0.0) { "span must be positive, was: $span" }
    return stiffness * span * span * span / support.stiffnessFactor
}

/**
 * The number of duplex **layers** an origami block of [columns] helix columns needs so that its
 * bending rigidity reaches [rigidity], at the measured [interhelicalDistance].
 *
 * The parallel-axis term dominates completely (`C-0014`: one helix at 2.69 nm contributes
 * `S(d/2)² = 1989 pN·nm²` against its own `EI₁ = 230`), so the count is driven by `Σy²` and
 * grows only as the **cube root** of the required rigidity.
 */
fun layersForBendingRigidity(
    rigidity: Double,
    columns: Int,
    interhelicalDistance: Double,
    helixBendingRigidity: Double,
    stretchModulus: Double,
    maximumLayers: Int = 40
): Int? {
    require(rigidity > 0.0) { "rigidity must be positive, was: $rigidity" }
    require(columns > 0) { "columns must be positive, was: $columns" }
    (1..maximumLayers).forEach { layers ->
        val offsets = (0 until layers).flatMap { layer ->
            val y = interhelicalDistance * (layer - (layers - 1) / 2.0)
            List(columns) { y }
        }
        val ei = offsets.size * helixBendingRigidity +
                stretchModulus * offsets.sumOf { it * it }
        if (ei >= rigidity) return layers
    }
    return null
}

// ---------------------------------------------------------------- forces

/**
 * The thermal force in pN carried by one of [count] anchors sharing a total [stiffness]:
 * `√(k_BT k)/N` — `C-0014`'s result, and the reason over-stiffening is not free.
 */
fun perAnchorThermalForce(stiffness: Double, count: Int): Double {
    require(stiffness >= 0.0) { "stiffness must not be negative, was: $stiffness" }
    require(count > 0) { "count must be positive, was: $count" }
    return sqrt(thermalEnergy() * stiffness) / count
}
