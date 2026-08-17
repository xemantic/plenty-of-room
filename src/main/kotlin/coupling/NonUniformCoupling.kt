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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.structure.CholeskyDecomposition
import com.xemantic.nano.plentyofroom.structure.GrillageDeflection
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateDeflection
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * `T-113` — the **distribution** of a coupling's stiffness over its attachments, which every claim
 * in this corpus has so far taken to be uniform.
 *
 * ## What is a design variable here and what is not
 *
 * `C-0017`'s mandate fixes the **total** coupling stiffness at `100 pN / 3 nm = 33.3333 pN/nm`
 * — it is a *placement* condition, an equality on the secant of the whole coupling — and nothing
 * upstream says how that total is shared between the `n` attachments. `C-0026`, `CH-0034`,
 * `C-0015` and `C-0047` all share it equally, and `C-0047` names the non-uniform alternative as
 * *"the last unexplored axis, and the only one that could attack `CH-0034`'s floor"*.
 *
 * So the design variable is a vector of `n` positive stiffnesses summing to the mandate, and the
 * three things this file adds are the distributions themselves ([normalisedStiffnesses],
 * [cappedStiffnesses], [rimStiffenedWeights], [loadMatchedWeights]), the price of using them
 * ([perPathStiffnessCeiling], [admissibleStiffnessRatio], [perPathThermalForces]), and the
 * machinery that makes an optimisation over them affordable ([InfluenceSurrogate]).
 *
 * ## Why a surrogate rather than thousands of assembled solves
 *
 * A support enters the lattice stiffness as the rank-one term `k_j b_j b_jᵀ` — which is exactly
 * what `OrigamiGrillage.solveWithAnchor` exploits for *one* anchor. For `n` of them the Woodbury
 * identity gives, **exactly**,
 *
 * ```
 * q = q_free − R (D⁻¹ + BᵀR)⁻¹ Bᵀ q_free,    R = K⁻¹B,   D = diag(k)
 * ```
 *
 * so one factorisation of the **unsupported** model plus `n + 1` load cases prices *every*
 * stiffness distribution at the cost of an `n × n` solve. The dishing projector and the grid
 * sampling are linear in the solution too, so the dishing field of a candidate is a linear
 * combination of `n + 1` precomputed grids. A candidate then costs microseconds where an
 * assembled solve costs a Cholesky factorisation, which is what makes a per-attachment
 * optimisation possible at all. Gate 5 asserts the surrogate against the assembled solve.
 *
 * The construction is model-agnostic — it needs only a free solution and one unit-point-load
 * solution per station — so the **same** surrogate runs on `C-0009`'s grillage and on `C-0006`'s
 * continuum plate, and the two are compared on identical arithmetic.
 */

// ------------------------------------------------------------------ the distributions

/**
 * The [weights] rescaled to stiffnesses in `pN/nm` summing exactly to [totalStiffness].
 *
 * The mandate is a condition on the **sum**, so a distribution is a direction and not a
 * magnitude: only the ratios of the weights carry information.
 */
fun normalisedStiffnesses(weights: List<Double>, totalStiffness: Double): List<Double> {
    require(weights.isNotEmpty()) { "weights must not be empty" }
    require(totalStiffness > 0.0) { "totalStiffness must be positive, was: $totalStiffness" }
    require(weights.all { it > 0.0 && it.isFinite() }) {
        "every weight must be positive and finite, were: $weights"
    }
    val sum = weights.sum()
    return weights.map { totalStiffness * it / sum }
}

/**
 * [normalisedStiffnesses] projected onto the per-path [ceiling] in `pN/nm`, by water-filling:
 * the paths that exceed the ceiling are set to it and the remaining budget is redistributed over
 * the rest in proportion to their weights, repeatedly until nothing exceeds it.
 *
 * The projection **conserves the mandate exactly** — it is a redistribution, not a truncation —
 * and it reduces to [normalisedStiffnesses] at an infinite ceiling and to the uniform
 * distribution at `ceiling = totalStiffness/n`, both of which are gate 2.
 *
 * A ceiling below `totalStiffness/n` admits no distribution at all and throws: that is not a
 * numerical edge case but the physically important one, because at 15 paths and §3's **desired**
 * 10 nm stroke the 10 pN unzip allowable puts the ceiling at 1.0 pN/nm against a uniform share of
 * 2.22 (`C-0049`).
 */
fun cappedStiffnesses(
    weights: List<Double>,
    totalStiffness: Double,
    ceiling: Double
): List<Double> {
    val plain = normalisedStiffnesses(weights, totalStiffness)
    require(ceiling > 0.0) { "ceiling must be positive, was: $ceiling" }
    if (ceiling.isInfinite()) return plain
    require(ceiling * weights.size >= totalStiffness * (1.0 - 1e-12)) {
        "a per-path ceiling of $ceiling pN/nm over ${weights.size} paths cannot carry a total " +
                "of $totalStiffness pN/nm — the mandate is infeasible at this path count"
    }
    val stiffnesses = plain.toMutableList()
    val capped = BooleanArray(weights.size)
    repeat(weights.size) {
        val over = stiffnesses.indices.filter {
            !capped[it] && stiffnesses[it] > ceiling * (1.0 + 1e-15)
        }
        if (over.isEmpty()) return stiffnesses
        over.forEach { capped[it] = true; stiffnesses[it] = ceiling }
        val free = stiffnesses.indices.filter { !capped[it] }
        if (free.isEmpty()) return stiffnesses
        val budget = totalStiffness - capped.count { it } * ceiling
        val freeWeight = free.sumOf { weights[it] }
        free.forEach { stiffnesses[it] = budget * weights[it] / freeWeight }
    }
    return stiffnesses
}

/**
 * The stiffness in `pN/nm` at which one path's own force reaches the per-path [allowable] pN at a
 * stroke of [stroke] nm — `a/s`.
 *
 * `C-0049`'s per-path secant ceiling `n·a/s` divided by the path count: **a bound on a force,
 * therefore a bound on a stiffness that tightens as `1/s`** where the declared ceiling is
 * constant.
 */
fun perPathStiffnessCeiling(allowable: Double, stroke: Double): Double {
    require(allowable > 0.0) { "allowable must be positive, was: $allowable" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    return allowable / stroke
}

/**
 * The largest multiple of the **uniform share** any single path may carry —
 * `R_max = n·a / (s·K)` — which is [perPathStiffnessCeiling] over `K/n`.
 *
 * This is the whole non-uniformity axis, bounded in one line of arithmetic before any solve:
 * **1.5** at `C-0041`'s 15 paths and **4.5** at `C-0015`'s 45, on the 10 pN unzip allowable at
 * §3's acceptable 3 nm; **0.45** at 15 paths and §3's desired 10 nm, i.e. below one, where not
 * even the uniform distribution is admissible.
 */
fun admissibleStiffnessRatio(
    allowable: Double,
    stroke: Double,
    totalStiffness: Double,
    pathCount: Int
): Double {
    require(totalStiffness > 0.0) { "totalStiffness must be positive, was: $totalStiffness" }
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    return perPathStiffnessCeiling(allowable, stroke) * pathCount / totalStiffness
}

/**
 * Weights of [ratio] for every attachment whose distance to the nearest tile edge is at most
 * [collarWidth] nm, and 1 for the rest — the *"stiffen the rim springs"* distribution `T-113` was
 * raised to test, with `C-0022`'s solved collar width as the natural value of [collarWidth].
 *
 * `ratio = 1` returns all ones identically, which is gate 2.
 */
fun rimStiffenedWeights(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double,
    collarWidth: Double,
    ratio: Double
): List<Double> {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    require(edgeX > 0.0 && edgeY > 0.0) { "the tile edges must be positive" }
    require(collarWidth > 0.0) { "collarWidth must be positive, was: $collarWidth" }
    require(ratio > 0.0) { "ratio must be positive, was: $ratio" }
    return grid.map { (x, y) ->
        val distance = min(edgeX / 2.0 - abs(x), edgeY / 2.0 - abs(y))
        require(distance >= 0.0) { "an attachment at ($x, $y) lies outside the tile" }
        if (distance <= collarWidth) ratio else 1.0
    }
}

/**
 * Weights proportional to the applied [pressure] at each station — the **load-matched**
 * distribution, which is knowable before any solve and which the cheap bound predicts should be
 * near the optimum if the dishing were a local response to a local load.
 *
 * Whether it is anywhere near the optimum is exactly the question, because a plate is *not* a
 * local response: the dishing at a free edge is set by what the sheet can carry to it, not by
 * what is applied there.
 */
fun loadMatchedWeights(
    grid: List<Pair<Double, Double>>,
    pressure: PressureField
): List<Double> {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    return grid.map { (x, y) ->
        val local = pressure.at(x, y)
        require(local > 0.0) { "the load at ($x, $y) is not positive: $local" }
        local
    }
}

/**
 * The thermal force in pN carried by each path of an **unequal** distribution:
 * `F_i = k_i √(k_BT/K)`, with `K` the total.
 *
 * `C-0014`'s `√(k_BT K)/n` is the equal-path case of this and is reproduced by it exactly (gate
 * 5). The generalisation matters because the scaling is **linear** in the path's share, not the
 * square root of it: a path stiffened `R×` carries `R×` the thermal force, so over-stiffening a
 * rim spring is priced at first order and not at half order.
 *
 * The derivation is one line: the tile's rigid-body coordinate has variance `k_BT/K` by
 * equipartition against the whole coupling, and every path sees that same displacement.
 */
fun perPathThermalForces(
    stiffnesses: List<Double>,
    temperature: Double = ROOM_TEMPERATURE
): List<Double> {
    require(stiffnesses.isNotEmpty()) { "stiffnesses must not be empty" }
    require(stiffnesses.all { it > 0.0 }) { "every stiffness must be positive" }
    val amplitude = sqrt(thermalEnergy(temperature) / stiffnesses.sum())
    return stiffnesses.map { it * amplitude }
}

// ------------------------------------------------------------------ the surrogate

/** Anything that can report a deflection and a dishing at a point — a grillage or a plate. */
interface DishingSolution {

    /** The deflection in nm at ([x], [y]), positive downward. */
    fun deflectionAt(x: Double, y: Double): Double

    /** The deflection with the best-fit rigid plane removed, in nm. */
    fun dishingAt(x: Double, y: Double): Double

}

/** The response of a coupling of given per-path stiffnesses, under one load case. */
class NonUniformDeflection internal constructor(

    /** The upward force in pN carried by each attachment, in the order of the grid. */
    val supportForces: List<Double>,

    /** The downward deflection in nm at each attachment. */
    val stationDeflections: List<Double>,

    /** The largest absolute dishing in nm over the sampling grid. */
    val peakDishing: Double,

    /** The root-mean-square dishing in nm over the same samples. */
    val rmsDishing: Double

)

/**
 * The exact Woodbury surrogate of one model, one attachment grid and one load case.
 *
 * Construct through [latticeInfluenceSurrogate] or [plateInfluenceSurrogate]; both hand this
 * class the free solution and one unit-point-load solution per station, which is all the
 * superposition needs.
 */
class InfluenceSurrogate internal constructor(

    /** The attachment stations, in nm from the tile centre. */
    val grid: List<Pair<Double, Double>>,

    /** The number of samples per edge of the dishing grid — 81, as everywhere upstream. */
    val samples: Int,

    /** The deflection at each station under the load alone, in nm. */
    private val stationFree: DoubleArray,

    /** `M[j][k]` — the deflection at station `j` under a unit downward load at station `k`. */
    private val stationInfluence: Array<DoubleArray>,

    /** The dishing field of the load alone, sampled. */
    private val dishingFree: DoubleArray,

    /** `dishingInfluence[k]` — the dishing field of a unit downward load at station `k`. */
    private val dishingInfluence: Array<DoubleArray>

) {

    /** The number of attachments. */
    val pathCount: Int get() = grid.size

    /**
     * The largest asymmetry of the influence matrix relative to its own scale.
     *
     * `M` is a compliance matrix, so **Maxwell-Betti makes it symmetric**, and nothing in the
     * assembly enforces that: it is measured between two different quadratures — the deflection
     * at `j` under a load at `k` against the deflection at `k` under a load at `j` — which is
     * what makes it informative rather than tautological.
     */
    val reciprocityResidual: Double by lazy {
        var scale = 0.0
        var worst = 0.0
        for (j in 0 until pathCount) for (k in 0 until pathCount) {
            scale = max(scale, abs(stationInfluence[j][k]))
            worst = max(worst, abs(stationInfluence[j][k] - stationInfluence[k][j]))
        }
        if (scale > 0.0) worst / scale else 0.0
    }

    /** The symmetrised influence matrix, which is what the Woodbury solve factorises. */
    private val symmetric: Array<DoubleArray> = Array(pathCount) { j ->
        DoubleArray(pathCount) { k ->
            0.5 * (stationInfluence[j][k] + stationInfluence[k][j])
        }
    }

    /** The response of a coupling whose paths carry [stiffnesses] pN/nm, exactly. */
    fun solve(stiffnesses: List<Double>): NonUniformDeflection {
        require(stiffnesses.size == pathCount) {
            "expected $pathCount stiffnesses, one per attachment, was: ${stiffnesses.size}"
        }
        require(stiffnesses.all { it > 0.0 && it.isFinite() }) {
            "every path stiffness must be positive and finite"
        }
        return solveWithDropout(stiffnesses, allPresent)
    }

    /**
     * The response of the same coupling with only the paths at which [present] is `true`
     * attached — `T-148`'s **dropout**, solved exactly.
     *
     * A missing staple does not perturb a load path's stiffness, it **removes** the path
     * (`CH-0084`), so an absent station is solved as absent rather than as a small stiffness: the
     * Woodbury system is assembled over the **surviving** stations alone, which is exact
     * superposition and not a limit. An absent path reports a support force and a station
     * deflection of exactly `0.0`, and a realisation in which **no** path survives returns the
     * free tile's own dishing — both of which are gate 2 of `T-148`.
     *
     * [solve] is this method at full presence, so the two agree bit for bit by construction and
     * nothing published on the surrogate can move.
     */
    fun solveWithDropout(
        stiffnesses: List<Double>,
        present: List<Boolean>
    ): NonUniformDeflection {
        require(stiffnesses.size == pathCount) {
            "expected $pathCount stiffnesses, one per attachment, was: ${stiffnesses.size}"
        }
        require(present.size == pathCount) {
            "expected one presence flag per attachment, was: ${present.size} for $pathCount"
        }
        val live = (0 until pathCount).filter { present[it] }
        require(live.all { stiffnesses[it] > 0.0 && stiffnesses[it].isFinite() }) {
            "every surviving path stiffness must be positive and finite"
        }
        val forces = DoubleArray(pathCount)
        if (live.isNotEmpty()) {
            val size = live.size
            val matrix = F64Array(size, size)
            for (j in 0 until size) {
                for (k in 0 until size) matrix[j, k] = symmetric[live[j]][live[k]]
                matrix[j, j] += 1.0 / stiffnesses[live[j]]
            }
            val right = F64Array(size) { stationFree[live[it]] }
            val solution = CholeskyDecomposition(matrix).solve(right)
            for (j in 0 until size) forces[live[j]] = solution[j]
        }
        val dishing = DoubleArray(dishingFree.size) { dishingFree[it] }
        for (k in live) {
            val force = forces[k]
            if (force == 0.0) continue
            val influence = dishingInfluence[k]
            for (g in dishing.indices) dishing[g] -= force * influence[g]
        }
        var peak = 0.0
        var square = 0.0
        for (value in dishing) {
            peak = max(peak, abs(value))
            square += value * value
        }
        return NonUniformDeflection(
            supportForces = (0 until pathCount).map { forces[it] },
            stationDeflections = (0 until pathCount).map {
                if (present[it]) forces[it] / stiffnesses[it] else 0.0
            },
            peakDishing = peak,
            rmsDishing = sqrt(square / dishing.size)
        )
    }

    /** Every path attached — allocated once, because a Monte Carlo asks for it per realisation. */
    private val allPresent: List<Boolean> = List(pathCount) { true }

    /**
     * The forces that minimise the root-mean-square dishing over the **whole** of `ℝⁿ`, ignoring
     * any relation between a force and a stiffness — an ordinary linear least squares.
     */
    val reachableForces: List<Double> by lazy {
        val matrix = F64Array(pathCount, pathCount)
        val right = F64Array(pathCount)
        for (j in 0 until pathCount) {
            for (k in j until pathCount) {
                var total = 0.0
                val a = dishingInfluence[j]
                val b = dishingInfluence[k]
                for (g in a.indices) total += a[g] * b[g]
                matrix[j, k] = total
                matrix[k, j] = total
            }
            var total = 0.0
            val a = dishingInfluence[j]
            for (g in a.indices) total += a[g] * dishingFree[g]
            right[j] = total
        }
        // A ridge of relative size 1e-12 on the diagonal, which moves the residual by less than
        // the rounding of the fields it is computed from, but keeps the Cholesky positive
        // definite when two influence functions are nearly parallel.
        var trace = 0.0
        for (j in 0 until pathCount) trace += matrix[j, j]
        for (j in 0 until pathCount) matrix[j, j] += 1e-12 * trace / pathCount
        val solution = CholeskyDecomposition(matrix).solve(right)
        (0 until pathCount).map { solution[it] }
    }

    /** The dishing field left by [reachableForces], sampled. */
    private val reachableField: DoubleArray by lazy {
        val dishing = DoubleArray(dishingFree.size) { dishingFree[it] }
        reachableForces.forEachIndexed { k, force ->
            val influence = dishingInfluence[k]
            for (g in dishing.indices) dishing[g] -= force * influence[g]
        }
        dishing
    }

    /**
     * **The bound.** The smallest root-mean-square dishing any set of forces at these stations can
     * leave — and therefore a rigorous **lower bound on the peak dishing of every stiffness
     * distribution whatever**, because the peak of a sampled field is never below its own root
     * mean square and every distribution produces *some* force vector.
     *
     * Four linear-algebra operations, no optimiser, no mandate: this is the cheap bound that says
     * in advance whether the expensive search can possibly reach `T-5b`'s tolerance.
     */
    val reachableDishingFloor: Double by lazy {
        var square = 0.0
        for (value in reachableField) square += value * value
        sqrt(square / reachableField.size)
    }

    /** The peak dishing of the same least-squares-optimal force vector — not a bound, a reading. */
    val reachablePeakDishing: Double by lazy {
        var peak = 0.0
        for (value in reachableField) peak = max(peak, abs(value))
        peak
    }

    /**
     * The Gram matrix of the sampled dishing influence fields, `G[j][k] = Σ_g d_j[g] d_k[g]`,
     * with the free field's projection `c[j] = Σ_g d_j[g] f[g]` and its own square sum.
     *
     * Built **once** so that [reachableDishingFloorAt] costs `O(|S|³)` per subset rather than
     * `O(|S|² N)`: that is the whole difference between a bound that can be evaluated over a
     * Monte Carlo ensemble and one that cannot. `T-155`.
     */
    private val dishingGram: Triple<Array<DoubleArray>, DoubleArray, Double> by lazy {
        val gram = Array(pathCount) { DoubleArray(pathCount) }
        val cross = DoubleArray(pathCount)
        for (j in 0 until pathCount) {
            val a = dishingInfluence[j]
            for (k in j until pathCount) {
                val b = dishingInfluence[k]
                var total = 0.0
                for (g in a.indices) total += a[g] * b[g]
                gram[j][k] = total
                gram[k][j] = total
            }
            var total = 0.0
            for (g in a.indices) total += a[g] * dishingFree[g]
            cross[j] = total
        }
        var square = 0.0
        for (value in dishingFree) square += value * value
        Triple(gram, cross, square)
    }

    /**
     * **`T-155`'s cheap bound.** The smallest root-mean-square dishing any set of forces at the
     * stations where [present] is `true` can leave — [reachableDishingFloor] restricted to a
     * dropout realisation's **surviving** support set.
     *
     * Because the peak of a sampled field is never below its own root mean square, and because
     * *every* stiffness distribution produces *some* force vector at those stations, this is a
     * rigorous lower bound on that realisation's peak dishing **for every distribution
     * whatever** — indeed for an oracle allowed to choose a different distribution per
     * realisation. Percentiles are monotone under a pointwise bound, so a 90th percentile of
     * this quantity above `T-5b`'s tolerance settles a station set with no search at all.
     *
     * A realisation in which no path survives returns the free field's own root mean square,
     * which is the `|S| = 0` case of the same formula and not a special case in the physics.
     */
    fun reachableDishingFloorAt(present: List<Boolean>): Double {
        require(present.size == pathCount) {
            "expected one presence flag per attachment, was: ${present.size} for $pathCount"
        }
        val (gram, cross, freeSquare) = dishingGram
        val live = (0 until pathCount).filter { present[it] }
        val samples = dishingFree.size
        if (live.isEmpty()) return sqrt(freeSquare / samples)
        val size = live.size
        val matrix = F64Array(size, size)
        var trace = 0.0
        for (j in 0 until size) {
            for (k in 0 until size) matrix[j, k] = gram[live[j]][live[k]]
            trace += gram[live[j]][live[j]]
        }
        // The same relative ridge `reachableForces` carries, for the same reason: two influence
        // functions of neighbouring stations are nearly parallel.
        for (j in 0 until size) matrix[j, j] += 1e-12 * trace / size
        val right = F64Array(size) { cross[live[it]] }
        val solution = CholeskyDecomposition(matrix).solve(right)
        // The residual as a quadratic form rather than as a field, which is what makes this
        // affordable: `‖f − Σ a_k d_k‖² = ‖f‖² − 2 a·c + aᵀ G a`, exactly.
        var linear = 0.0
        var quadratic = 0.0
        for (j in 0 until size) {
            linear += solution[j] * cross[live[j]]
            var row = 0.0
            for (k in 0 until size) row += gram[live[j]][live[k]] * solution[k]
            quadratic += solution[j] * row
        }
        val residual = freeSquare - 2.0 * linear + quadratic
        return sqrt(max(0.0, residual) / samples)
    }

}

private fun GrillageDeflection.asDishingSolution(): DishingSolution = object : DishingSolution {
    override fun deflectionAt(x: Double, y: Double) = deflection(x, y)
    override fun dishingAt(x: Double, y: Double) = dishing(x, y)
}

private fun PlateDeflection.asDishingSolution(): DishingSolution = object : DishingSolution {
    override fun deflectionAt(x: Double, y: Double) = deflection(x, y)
    override fun dishingAt(x: Double, y: Double) = dishing(x, y)
}

/**
 * The surrogate assembled from a [free] solution and one [influence] solution per station, each
 * the response to a **unit downward point load** there.
 *
 * The sampling grid is `samples × samples` over `[−halfX, halfX] × [−halfY, halfY]`, which is the
 * grid `OrigamiGrillage.peakDishing` and `PlateOnFoundation.peakDishing` use, so a surrogate peak
 * is comparable with an assembled one sample for sample.
 */
fun influenceSurrogate(
    grid: List<Pair<Double, Double>>,
    halfX: Double,
    halfY: Double,
    samples: Int,
    free: DishingSolution,
    influence: List<DishingSolution>
): InfluenceSurrogate {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    require(samples >= 2) { "samples must be at least 2, was: $samples" }
    require(influence.size == grid.size) {
        "expected one influence solution per station, was: ${influence.size} for ${grid.size}"
    }
    fun sampled(solution: DishingSolution): DoubleArray {
        val field = DoubleArray(samples * samples)
        for (i in 0 until samples) {
            val x = -halfX + 2.0 * halfX * i / (samples - 1)
            for (j in 0 until samples) {
                val y = -halfY + 2.0 * halfY * j / (samples - 1)
                field[i * samples + j] = solution.dishingAt(x, y)
            }
        }
        return field
    }
    return InfluenceSurrogate(
        grid = grid,
        samples = samples,
        stationFree = DoubleArray(grid.size) { free.deflectionAt(grid[it].first, grid[it].second) },
        stationInfluence = Array(grid.size) { j ->
            DoubleArray(grid.size) { k ->
                influence[k].deflectionAt(grid[j].first, grid[j].second)
            }
        },
        dishingFree = sampled(free),
        dishingInfluence = Array(grid.size) { sampled(influence[it]) }
    )
}

/** [influenceSurrogate] over `C-0009`'s beam-and-hinge grillage, which must carry no supports. */
fun latticeInfluenceSurrogate(
    lattice: OrigamiGrillage,
    grid: List<Pair<Double, Double>>,
    pressure: PressureField,
    samples: Int = 81
): InfluenceSurrogate {
    require(lattice.supports.isEmpty()) {
        "the surrogate carries the supports itself, so the lattice must be assembled without " +
                "any: it had ${lattice.supports.size}"
    }
    val free = lattice.solve(pressure).asDishingSolution()
    val influence = grid.map { (x, y) ->
        lattice.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0))).asDishingSolution()
    }
    return influenceSurrogate(
        grid, lattice.lengthX / 2.0, lattice.lengthY / 2.0, samples, free, influence
    )
}

/** [influenceSurrogate] over `C-0006`'s continuum plate, which must carry no supports. */
fun plateInfluenceSurrogate(
    plate: PlateOnFoundation,
    grid: List<Pair<Double, Double>>,
    pressure: PressureField,
    samples: Int = 81
): InfluenceSurrogate {
    require(plate.supports.isEmpty()) {
        "the surrogate carries the supports itself, so the plate must be assembled without " +
                "any: it had ${plate.supports.size}"
    }
    val free = plate.solve(pressure).asDishingSolution()
    val influence = grid.map { (x, y) ->
        plate.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0))).asDishingSolution()
    }
    return influenceSurrogate(
        grid, plate.plate.lengthX / 2.0, plate.plate.lengthY / 2.0, samples, free, influence
    )
}

// ------------------------------------------------------------------ the optimisation

/** The best distribution a search found, and what it cost to find it. */
data class StiffnessOptimum(
    val stiffnesses: List<Double>,
    val objective: Double,
    val startIndex: Int,
    val evaluations: Int,
    val sweeps: Int,
    val lastImprovement: Double
)

/**
 * A deterministic cyclic coordinate descent over the per-path stiffnesses, at fixed
 * [totalStiffness] and under an optional per-path [ceiling].
 *
 * Each coordinate is searched on its **logarithm** — the natural parametrisation of a positive
 * weight, and the one in which the uniform distribution is the origin — by a coarse scan of
 * [scanPoints] over `[−h, h]` followed by a golden-section refinement inside the best bracket.
 * The coarse scan is there because the peak of a field is a *maximum of smooth functions* and
 * therefore piecewise smooth rather than unimodal; the golden section alone would stall in the
 * wrong basin.
 *
 * This is a **descent** and reports the best point it found, never a global optimum. That is why
 * `InfluenceSurrogate.reachableDishingFloor` is computed beside it: the gap between them is the
 * honest statement of how much room the search may have left.
 */
fun optimiseStiffnessDistribution(
    totalStiffness: Double,
    starts: List<List<Double>>,
    ceiling: Double = Double.POSITIVE_INFINITY,
    sweeps: Int = 30,
    tolerance: Double = 1e-6,
    searchHalfWidth: Double = 2.0,
    scanPoints: Int = 9,
    refinements: Int = 12,
    objective: (List<Double>) -> Double
): StiffnessOptimum {
    require(starts.isNotEmpty()) { "starts must not be empty" }
    require(sweeps >= 1) { "sweeps must be at least 1, was: $sweeps" }
    require(searchHalfWidth > 0.0) { "searchHalfWidth must be positive, was: $searchHalfWidth" }
    require(scanPoints >= 3) { "scanPoints must be at least 3, was: $scanPoints" }
    var evaluations = 0
    var bestOverall: StiffnessOptimum? = null
    starts.forEachIndexed { startIndex, start ->
        require(start.size == starts[0].size) { "every start must have the same path count" }
        val weights = start.map { it }.toMutableList()
        fun evaluate(): Double {
            evaluations++
            return objective(cappedStiffnesses(weights, totalStiffness, ceiling))
        }
        var best = evaluate()
        var lastImprovement = Double.POSITIVE_INFINITY
        var used = 0
        for (sweep in 1..sweeps) {
            val before = best
            for (index in weights.indices) {
                val base = weights[index]
                fun at(shift: Double): Double {
                    weights[index] = base * exp(shift)
                    val value = evaluate()
                    weights[index] = base
                    return value
                }
                var bestShift = 0.0
                var bestValue = best
                val step = 2.0 * searchHalfWidth / (scanPoints - 1)
                for (point in 0 until scanPoints) {
                    val shift = -searchHalfWidth + point * step
                    if (shift == 0.0) continue
                    val value = at(shift)
                    if (value < bestValue) {
                        bestValue = value
                        bestShift = shift
                    }
                }
                var low = bestShift - step
                var high = bestShift + step
                val phi = (sqrt(5.0) - 1.0) / 2.0
                var left = high - phi * (high - low)
                var right = low + phi * (high - low)
                var leftValue = at(left)
                var rightValue = at(right)
                repeat(refinements) {
                    if (leftValue < rightValue) {
                        high = right; right = left; rightValue = leftValue
                        left = high - phi * (high - low); leftValue = at(left)
                    } else {
                        low = left; left = right; leftValue = rightValue
                        right = low + phi * (high - low); rightValue = at(right)
                    }
                }
                val refined = if (leftValue < rightValue) left to leftValue else right to rightValue
                if (refined.second < bestValue) {
                    bestValue = refined.second
                    bestShift = refined.first
                }
                if (bestValue < best - 1e-15) {
                    best = bestValue
                    weights[index] = base * exp(bestShift)
                }
            }
            used = sweep
            lastImprovement = if (before > 0.0) (before - best) / before else 0.0
            if (lastImprovement <= tolerance) break
        }
        val candidate = StiffnessOptimum(
            stiffnesses = cappedStiffnesses(weights, totalStiffness, ceiling),
            objective = best,
            startIndex = startIndex,
            evaluations = evaluations,
            sweeps = used,
            lastImprovement = lastImprovement
        )
        if (bestOverall == null || candidate.objective < bestOverall!!.objective) {
            bestOverall = candidate
        }
    }
    return bestOverall!!.copy(evaluations = evaluations)
}
