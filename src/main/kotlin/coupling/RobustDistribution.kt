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

import com.xemantic.nano.plentyofroom.structure.CholeskyDecomposition
import com.xemantic.nano.plentyofroom.structure.GrillageDeflection
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateDeflection
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-123` — is **any** distribution of `C-0017`'s mandated total flat at every one of `C-0022`'s
 * solved operating states, or is flatness intrinsically a single-state property?
 *
 * ## What this adds to `C-0058`
 *
 * `C-0058` freed the distribution and made the Gen-1 tile flat at one state. Its own Deliverable 4
 * then reports that the flat design is flat at three of `C-0022`'s five solved states and *worse
 * than uniform* at the 2 nm gap, and that a minimax over all five reached only 0.1587 of the
 * stroke — which it labels a **"not found"**, because the search was a cyclic coordinate descent
 * from three starts.
 *
 * That label is exactly right, and the reason is structural: **a maximum of smooth functions is
 * not smooth**, and cyclic coordinate descent is the one classical method that provably stalls on
 * a non-differentiable ridge — at a point where no single coordinate direction descends although a
 * combination of two does. A minimax value reported from a coordinate descent is therefore not
 * evidence of a floor, and re-running it with more starts of the same method would not settle
 * anything either.
 *
 * Three things are new here.
 *
 * 1. **A multi-state surrogate.** The influence functions of the stations depend on the model and
 *    the geometry and **not on the load**, so one factorisation and `n + S` load cases price every
 *    distribution at every state — where `C-0058` built one whole [InfluenceSurrogate] per state
 *    and paid `S(n + 1)`. At five states and 45 paths that is 50 solves against 230.
 * 2. **An analytic gradient through the Woodbury solve.** With `A = M + diag(1/k)` and
 *    `F = A⁻¹ w`, differentiating `A F = w` gives `∂F/∂k_j = (F_j/k_j²) A⁻¹ e_j` exactly, so the
 *    gradient of any linear functional of the dishing costs one extra triangular solve per state
 *    on the factorisation the objective has already built. Gate 4 checks it against a central
 *    finite difference; without that check every search step below is unverified.
 * 3. **A smoothed minimax with continuation.** `max` is replaced by a log-sum-exp over the signed
 *    dishing samples, which is smooth, is an **upper** bound on the true maximum, and converges to
 *    it as `μ → 0` — so a nonlinear conjugate-gradient method applies, and the sequence of
 *    smoothing levels is a homotopy from a soft problem to the real one.
 *
 * ## Determinism
 *
 * `CLAUDE.md`'s standing trap is that rounding at the serialisation boundary does not make a
 * result file reproducible if it contains an **argmin**, and `C-0058` met a new instance of it:
 * its optimiser's evaluation count, sweep count and winning start all differed between runs while
 * every objective agreed to nine significant digits. Here every comparison the search makes — the
 * Armijo acceptance test, the "is this iterate better" test, the choice between starts, and the
 * objective handed to `C-0058`'s optimiser as a polish — is taken on **[searchDecision]-rounded**
 * values with the earlier candidate winning any tie. The decision is rounded, not only the number,
 * and it is rounded **coarser** than the number: see [SEARCH_DECISION_DIGITS], which is the half of
 * this discipline that nine digits at the serialisation boundary does not supply.
 */

// ------------------------------------------------------------------ the search's own precision

/**
 * The number of significant digits every comparison **inside** a search is taken at.
 *
 * `roundCouplingResult`'s nine digits are the right precision to *emit* a number at and the wrong
 * one to *decide* at. A last-ulp difference is `1e-15` relative, so the chance that one value of a
 * pair straddles a nine-digit boundary is about `1e-6` per comparison — and this study takes of order
 * `1e6` of them, so a nine-digit decision rule flips somewhere in **every** run. It did: two runs of
 * this study differing only in an unused local agreed on every objective to five digits and disagreed
 * in the sixth, because one Armijo acceptance had gone the other way and the descent finished in a
 * neighbouring basin.
 *
 * At **six** digits the same estimate is `1e-9` per comparison, i.e. `1e-3` over the whole study,
 * while the finest quantity reported is four digits and the search's own convergence tolerances are
 * `1e-5`. So the precision thrown away is precision the answer never had.
 */
internal const val SEARCH_DECISION_DIGITS: Int = 6

/**
 * [value] rounded to [SEARCH_DECISION_DIGITS] significant digits — the quantisation at which the
 * line search accepts, the descent keeps and the starts are ranked.
 *
 * Ties are then genuinely equal and every tie-break in this file keeps the **earlier** candidate, so
 * the search path is a function of the inputs and not of the JIT's compilation schedule.
 */
internal fun searchDecision(value: Double): Double {
    if (!value.isFinite() || value == 0.0) return value
    val scale = Math.pow(
        10.0, (SEARCH_DECISION_DIGITS - 1 - Math.floor(Math.log10(abs(value))))
    )
    return Math.round(value * scale) / scale
}

// ------------------------------------------------------------------ the states

/** One operating state: a named load field, which is all a state is to a linear model. */
class LoadState(

    /** The `(concentration, gap, bias)` label of `C-0022`'s solved profile. */
    val name: String,

    /** The applied pressure field in `pN/nm²`. */
    val pressure: PressureField

)

/** The value of a smoothed objective and its gradient with respect to the path stiffnesses. */
class SmoothedObjective(

    /** The smoothed worst dishing in nm — an **upper** bound on the true one. */
    val value: Double,

    /** `∂value/∂k_j` in `nm/(pN/nm)`, one per path. */
    val gradient: List<Double>

)

// ------------------------------------------------------------------ the surrogate

/**
 * The exact Woodbury surrogate of one model and one attachment grid over **several** load states.
 *
 * Construct through [multiStateSurrogate] or [multiStatePlateSurrogate].
 *
 * The station influence matrix `M` and the sampled dishing influence fields `G` are shared by
 * every state — they are properties of the structure — and only the free response `(w_s, d_s)`
 * differs. That is the whole economy of this class, and it is also why a *minimax* is affordable
 * at all: one `n × n` Cholesky serves all `S` states of one candidate distribution.
 */
class MultiStateSurrogate internal constructor(

    /** The attachment stations, in nm from the tile centre. */
    val grid: List<Pair<Double, Double>>,

    /** The number of samples per edge of the dishing grid — 81, as everywhere upstream. */
    val samples: Int,

    /** The state labels, in the order every per-state list here uses. */
    val stateNames: List<String>,

    /** `M[j][k]` — the deflection at station `j` under a unit downward load at station `k`. */
    private val stationInfluence: Array<DoubleArray>,

    /** `G[k]` — the sampled dishing field of a unit downward load at station `k`. */
    private val dishingInfluence: Array<DoubleArray>,

    /** `w[s]` — the deflection at each station under state `s`'s load alone, in nm. */
    private val stationFree: Array<DoubleArray>,

    /** `d[s]` — the sampled dishing field of state `s`'s load alone, in nm. */
    private val dishingFree: Array<DoubleArray>

) {

    /** The number of attachments. */
    val pathCount: Int get() = grid.size

    /** The number of operating states. */
    val stateCount: Int get() = stateNames.size

    /**
     * The largest asymmetry of the influence matrix relative to its own scale.
     *
     * `M` is a compliance matrix, so **Maxwell-Betti makes it symmetric**, and nothing in the
     * assembly enforces that: it is measured between two different quadratures.
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

    private val symmetric: Array<DoubleArray> = Array(pathCount) { j ->
        DoubleArray(pathCount) { k -> 0.5 * (stationInfluence[j][k] + stationInfluence[k][j]) }
    }

    private fun checkStiffnesses(stiffnesses: List<Double>) {
        require(stiffnesses.size == pathCount) {
            "expected $pathCount stiffnesses, one per attachment, was: ${stiffnesses.size}"
        }
        require(stiffnesses.all { it > 0.0 && it.isFinite() }) {
            "every path stiffness must be positive and finite"
        }
    }

    private fun checkStates(states: List<Int>) {
        require(states.isNotEmpty()) { "at least one state must be named" }
        require(states.all { it in 0 until stateCount }) {
            "every state index must be within 0 until $stateCount, were: $states"
        }
    }

    /**
     * The Cholesky factor of `A = M + diag(1/k)`, which every state of one candidate shares.
     *
     * This is the Woodbury system: the attachment forces of state `s` are `A⁻¹ w_s`.
     */
    private fun factorise(stiffnesses: List<Double>): CholeskyDecomposition {
        val matrix = F64Array(pathCount, pathCount)
        for (j in 0 until pathCount) {
            for (k in 0 until pathCount) matrix[j, k] = symmetric[j][k]
            matrix[j, j] += 1.0 / stiffnesses[j]
        }
        return CholeskyDecomposition(matrix)
    }

    private fun forces(factor: CholeskyDecomposition, state: Int): DoubleArray {
        val right = F64Array(pathCount) { stationFree[state][it] }
        val solution = factor.solve(right)
        return DoubleArray(pathCount) { solution[it] }
    }

    /**
     * The dishing field of [state] under a coupling carrying [forces], sampled.
     *
     * The accumulation is an in-place `axpy` over `samples²` values per station, written as an
     * explicit loop rather than through `F64Array` because viktor exposes no scaled in-place add
     * and a temporary per station would allocate `n · samples²` doubles per candidate — the same
     * choice, for the same reason, as `InfluenceSurrogate.solve`.
     */
    private fun dishingField(state: Int, forces: DoubleArray): DoubleArray {
        val free = dishingFree[state]
        val dishing = DoubleArray(free.size) { free[it] }
        for (k in 0 until pathCount) {
            val force = forces[k]
            if (force == 0.0) continue
            val influence = dishingInfluence[k]
            for (g in dishing.indices) dishing[g] -= force * influence[g]
        }
        return dishing
    }

    /** The upward force in pN carried by each attachment at [state], exactly. */
    fun supportForces(stiffnesses: List<Double>, state: Int): List<Double> {
        checkStiffnesses(stiffnesses)
        checkStates(listOf(state))
        return forces(factorise(stiffnesses), state).toList()
    }

    /** The peak absolute dishing in nm at every state, in the order of [stateNames]. */
    fun peakDishing(stiffnesses: List<Double>): List<Double> {
        checkStiffnesses(stiffnesses)
        val factor = factorise(stiffnesses)
        return (0 until stateCount).map { state ->
            var peak = 0.0
            for (value in dishingField(state, forces(factor, state))) peak = max(peak, abs(value))
            peak
        }
    }

    /** The largest peak dishing over [states] — the minimax objective, unsmoothed. */
    fun worstDishing(
        stiffnesses: List<Double>,
        states: List<Int> = (0 until stateCount).toList()
    ): Double {
        checkStiffnesses(stiffnesses)
        checkStates(states)
        val factor = factorise(stiffnesses)
        var worst = 0.0
        states.forEach { state ->
            for (value in dishingField(state, forces(factor, state))) worst = max(worst, abs(value))
        }
        return worst
    }

    /**
     * The log-sum-exp smoothing of [worstDishing] at scale [smoothing], and its exact gradient.
     *
     * `J_μ = μ ln Σ_{s,g} [exp(d/μ) + exp(−d/μ)]` over the signed samples, which is smooth,
     * satisfies `max|d| ≤ J_μ ≤ max|d| + μ ln(2N)`, and tends to the maximum as `μ → 0`.
     *
     * The gradient is assembled by the adjoint route: with `r = ∂J/∂d`, `u_s = Gᵀ r_s` and
     * `y_s = A⁻¹ u_s`, one has `∂J/∂k_j = −Σ_s F_{s,j} y_{s,j} / k_j²` **exactly**. The extra cost
     * over the value alone is one triangular solve and one pass over the fields per state.
     */
    fun smoothedObjective(
        stiffnesses: List<Double>,
        smoothing: Double,
        states: List<Int> = (0 until stateCount).toList()
    ): SmoothedObjective {
        checkStiffnesses(stiffnesses)
        checkStates(states)
        require(smoothing > 0.0 && smoothing.isFinite()) {
            "smoothing must be positive and finite, was: $smoothing"
        }
        val factor = factorise(stiffnesses)
        val forcesByState = states.map { forces(factor, it) }
        val fields = states.mapIndexed { index, state -> dishingField(state, forcesByState[index]) }
        var peak = 0.0
        fields.forEach { field -> for (value in field) peak = max(peak, abs(value)) }
        var total = 0.0
        val weights = fields.map { field ->
            DoubleArray(field.size) { g ->
                val plus = exp((field[g] - peak) / smoothing)
                val minus = exp((-field[g] - peak) / smoothing)
                total += plus + minus
                plus - minus
            }
        }
        val value = peak + smoothing * ln(total)
        val gradient = DoubleArray(pathCount)
        states.indices.forEach { index ->
            val residual = weights[index]
            val adjoint = F64Array(pathCount) { j ->
                var sum = 0.0
                val influence = dishingInfluence[j]
                for (g in residual.indices) sum += residual[g] * influence[g]
                -sum / total
            }
            val y = factor.solve(adjoint)
            val force = forcesByState[index]
            for (j in 0 until pathCount) {
                gradient[j] += force[j] * y[j] / (stiffnesses[j] * stiffnesses[j])
            }
        }
        return SmoothedObjective(value, gradient.toList())
    }

    /** `GᵀG`, the Gram matrix of the sampled influence fields — shared by every state. */
    private val influenceGram: CholeskyDecomposition by lazy {
        val matrix = F64Array(pathCount, pathCount)
        for (j in 0 until pathCount) {
            for (k in j until pathCount) {
                var sum = 0.0
                val a = dishingInfluence[j]
                val b = dishingInfluence[k]
                for (g in a.indices) sum += a[g] * b[g]
                matrix[j, k] = sum
                matrix[k, j] = sum
            }
        }
        var trace = 0.0
        for (j in 0 until pathCount) trace += matrix[j, j]
        for (j in 0 until pathCount) matrix[j, j] += 1e-12 * trace / pathCount
        CholeskyDecomposition(matrix)
    }

    /**
     * **The cheap bound, per state.** The smallest root-mean-square dishing any set of attachment
     * forces can leave at [state] — and therefore a rigorous lower bound on the peak dishing of
     * *every* stiffness distribution whatever, since dishing is affine in the forces, every
     * distribution produces some force vector, and the peak of a sampled field is never below its
     * own root mean square.
     *
     * The maximum of this over a set of states is a rigorous lower bound on the **minimax**,
     * because the relaxation lets each state choose its own forces.
     */
    fun reachableDishingFloor(state: Int): Double {
        checkStates(listOf(state))
        val free = dishingFree[state]
        val right = F64Array(pathCount) { j ->
            var sum = 0.0
            val influence = dishingInfluence[j]
            for (g in influence.indices) sum += influence[g] * free[g]
            sum
        }
        val solution = influenceGram.solve(right)
        val residual = DoubleArray(free.size) { free[it] }
        for (k in 0 until pathCount) {
            val force = solution[k]
            val influence = dishingInfluence[k]
            for (g in residual.indices) residual[g] -= force * influence[g]
        }
        var square = 0.0
        for (value in residual) square += value * value
        return sqrt(square / residual.size)
    }

    /**
     * The cosine between the free-tile dishing fields of two states — **do these two states want
     * the same correction?**
     *
     * A cosine near `+1` means one distribution can serve both; a negative cosine means the two
     * loads dish the free tile in opposite senses, and a force vector that flattens one deepens
     * the other. It costs one pass over two precomputed fields and needs no optimiser at all.
     */
    fun freeFieldCosine(first: Int, second: Int): Double {
        checkStates(listOf(first, second))
        val a = dishingFree[first]
        val b = dishingFree[second]
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (g in a.indices) {
            dot += a[g] * b[g]
            normA += a[g] * a[g]
            normB += b[g] * b[g]
        }
        return if (normA <= 0.0 || normB <= 0.0) 0.0 else dot / sqrt(normA * normB)
    }

    /** The peak absolute value of state [state]'s free-tile dishing field, in nm. */
    fun freeFieldPeak(state: Int): Double {
        checkStates(listOf(state))
        var peak = 0.0
        for (value in dishingFree[state]) peak = max(peak, abs(value))
        return peak
    }

}

private fun sampledDishing(
    samples: Int,
    halfX: Double,
    halfY: Double,
    dishingAt: (Double, Double) -> Double
): DoubleArray {
    val field = DoubleArray(samples * samples)
    for (i in 0 until samples) {
        val x = -halfX + 2.0 * halfX * i / (samples - 1)
        for (j in 0 until samples) {
            val y = -halfY + 2.0 * halfY * j / (samples - 1)
            field[i * samples + j] = dishingAt(x, y)
        }
    }
    return field
}

/** [MultiStateSurrogate] over `C-0009`'s beam-and-hinge grillage, which must carry no supports. */
fun multiStateSurrogate(
    lattice: OrigamiGrillage,
    grid: List<Pair<Double, Double>>,
    states: List<LoadState>,
    samples: Int = 81
): MultiStateSurrogate {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    require(states.isNotEmpty()) { "at least one load state is required" }
    require(samples >= 2) { "samples must be at least 2, was: $samples" }
    require(lattice.supports.isEmpty()) {
        "the surrogate carries the supports itself, so the lattice must be assembled without " +
                "any: it had ${lattice.supports.size}"
    }
    val halfX = lattice.lengthX / 2.0
    val halfY = lattice.lengthY / 2.0
    val influence: List<GrillageDeflection> = grid.map { (x, y) ->
        lattice.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0)))
    }
    val free: List<GrillageDeflection> = states.map { lattice.solve(it.pressure) }
    return MultiStateSurrogate(
        grid = grid,
        samples = samples,
        stateNames = states.map { it.name },
        stationInfluence = Array(grid.size) { j ->
            DoubleArray(grid.size) { k -> influence[k].deflection(grid[j].first, grid[j].second) }
        },
        dishingInfluence = Array(grid.size) { k ->
            sampledDishing(samples, halfX, halfY) { x, y -> influence[k].dishing(x, y) }
        },
        stationFree = Array(states.size) { s ->
            DoubleArray(grid.size) { j -> free[s].deflection(grid[j].first, grid[j].second) }
        },
        dishingFree = Array(states.size) { s ->
            sampledDishing(samples, halfX, halfY) { x, y -> free[s].dishing(x, y) }
        }
    )
}

/** [MultiStateSurrogate] over `C-0006`'s continuum plate, which must carry no supports. */
fun multiStatePlateSurrogate(
    plate: PlateOnFoundation,
    grid: List<Pair<Double, Double>>,
    states: List<LoadState>,
    samples: Int = 81
): MultiStateSurrogate {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    require(states.isNotEmpty()) { "at least one load state is required" }
    require(samples >= 2) { "samples must be at least 2, was: $samples" }
    require(plate.supports.isEmpty()) {
        "the surrogate carries the supports itself, so the plate must be assembled without " +
                "any: it had ${plate.supports.size}"
    }
    val halfX = plate.plate.lengthX / 2.0
    val halfY = plate.plate.lengthY / 2.0
    val influence: List<PlateDeflection> = grid.map { (x, y) ->
        plate.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0)))
    }
    val free: List<PlateDeflection> = states.map { plate.solve(it.pressure) }
    return MultiStateSurrogate(
        grid = grid,
        samples = samples,
        stateNames = states.map { it.name },
        stationInfluence = Array(grid.size) { j ->
            DoubleArray(grid.size) { k -> influence[k].deflection(grid[j].first, grid[j].second) }
        },
        dishingInfluence = Array(grid.size) { k ->
            sampledDishing(samples, halfX, halfY) { x, y -> influence[k].dishing(x, y) }
        },
        stationFree = Array(states.size) { s ->
            DoubleArray(grid.size) { j -> free[s].deflection(grid[j].first, grid[j].second) }
        },
        dishingFree = Array(states.size) { s ->
            sampledDishing(samples, halfX, halfY) { x, y -> free[s].dishing(x, y) }
        }
    )
}

// ------------------------------------------------------------------ the search

/** What a minimax search found, with no property of the search PATH in it. */
data class MinimaxOptimum(
    val stiffnesses: List<Double>,
    val worstDishing: Double,
    val perStateDishing: List<Double>,
    /** The states whose dishing is within one part in `1e4` of the worst — the active set. */
    val bindingStates: List<String>,
    /**
     * How many starts reached, at the end of their own conjugate-gradient homotopy and before the
     * single polish, within one part in a million of the best of them.
     *
     * This is the honest measure of whether the search is start-limited: a count of one says the
     * answer rests on a single basin, and a count near [startsUsed] says the whole ensemble agrees.
     * It is a comparison at a coarse, stated threshold, not a `Double` equality, so it survives
     * the ulp jitter that made `C-0058`'s winning-start index irreproducible.
     */
    val startsWithinOnePartInAMillion: Int,
    val startsUsed: Int
)

/**
 * The stiffness distribution minimising the **worst** peak dishing over [states], at fixed
 * [totalStiffness] and under an optional per-path [ceiling].
 *
 * Each start is run through a homotopy: for every level of [smoothingLevels] in the order given,
 * [iterationsPerLevel] nonlinear conjugate-gradient steps (Polak-Ribière with restarts, Armijo
 * backtracking) on the log-weights, where the mandate is enforced exactly by a softmax and the
 * positivity constraint does not exist. The **true**, unsmoothed objective is tracked throughout
 * and the best iterate is kept, so the smoothing can never make the reported answer worse than
 * the start. `C-0058`'s own coordinate descent then polishes the result on the true objective for
 * [polishSweeps] sweeps, so the two searches are composed rather than compared.
 *
 * The [ceiling] is applied through `cappedStiffnesses`, whose water-filling is not differentiable;
 * the gradient path therefore sees the plain normalisation and the **acceptance** tests see the
 * capped one, which makes this a descent with an approximate direction rather than an exact one.
 * Where the uncapped answer turns out to be admissible the distinction is void, and that is the
 * case worth reporting.
 *
 * This is a **descent**: it reports the best point it found, never a global optimum, which is why
 * [MultiStateSurrogate.reachableDishingFloor] is computed beside it.
 */
fun minimaxStiffnessDistribution(
    surrogate: MultiStateSurrogate,
    states: List<Int>,
    totalStiffness: Double,
    starts: List<List<Double>>,
    ceiling: Double = Double.POSITIVE_INFINITY,
    smoothingLevels: List<Double> = listOf(0.3, 0.1, 0.03, 0.01, 3e-3, 1e-3),
    iterationsPerLevel: Int = 25,
    polishSweeps: Int = 4
): MinimaxOptimum {
    require(starts.isNotEmpty()) { "starts must not be empty" }
    require(smoothingLevels.isNotEmpty()) { "at least one smoothing level is required" }
    require(smoothingLevels.all { it > 0.0 }) { "every smoothing level must be positive" }
    require(iterationsPerLevel >= 1) { "iterationsPerLevel must be at least 1" }
    require(polishSweeps >= 1) { "polishSweeps must be at least 1, was: $polishSweeps" }
    val paths = surrogate.pathCount

    fun stiffnessesOf(weights: DoubleArray): List<Double> =
        cappedStiffnesses(weights.toList(), totalStiffness, ceiling)

    fun truth(weights: DoubleArray): Double =
        surrogate.worstDishing(stiffnessesOf(weights), states)

    val results = starts.mapIndexed { index, start ->
        require(start.size == paths) {
            "every start must carry one weight per attachment, was: ${start.size} for $paths"
        }
        require(start.all { it > 0.0 && it.isFinite() }) { "every start weight must be positive" }
        // The search runs on the log-weights; the softmax makes the mandate exact by construction.
        val theta = DoubleArray(paths) { ln(start[it]) }
        var bestWeights = DoubleArray(paths) { exp(theta[it]) }
        var bestTruth = truth(bestWeights)

        smoothingLevels.forEach { smoothing ->
            fun evaluate(point: DoubleArray): Pair<Double, DoubleArray> {
                val weights = DoubleArray(paths) { exp(point[it]) }
                val stiffnesses = normalisedStiffnesses(weights.toList(), totalStiffness)
                val objective = surrogate.smoothedObjective(stiffnesses, smoothing, states)
                var dot = 0.0
                for (j in 0 until paths) dot += objective.gradient[j] * stiffnesses[j]
                val gradient = DoubleArray(paths) { j ->
                    stiffnesses[j] * (objective.gradient[j] - dot / totalStiffness)
                }
                return objective.value to gradient
            }

            var evaluated = evaluate(theta)
            var value = evaluated.first
            var gradient = evaluated.second
            var direction = DoubleArray(paths) { -gradient[it] }
            var step = 0.1
            for (iteration in 1..iterationsPerLevel) {
                var slope = 0.0
                for (j in 0 until paths) slope += gradient[j] * direction[j]
                if (slope >= 0.0) {
                    direction = DoubleArray(paths) { -gradient[it] }
                    slope = 0.0
                    for (j in 0 until paths) slope -= gradient[j] * gradient[j]
                }
                if (slope == 0.0) break
                var trial = step
                var trialPoint: DoubleArray? = null
                for (backtrack in 1..30) {
                    val candidate = DoubleArray(paths) { theta[it] + trial * direction[it] }
                    val candidateValue = evaluate(candidate).first
                    // The acceptance test is taken on ROUNDED values: an ulp of jitter in a hot
                    // reduction must not be able to flip a search decision (CLAUDE.md).
                    if (searchDecision(candidateValue) <=
                        searchDecision(value + 1e-4 * trial * slope)
                    ) {
                        trialPoint = candidate
                        break
                    }
                    trial *= 0.5
                }
                val accepted = trialPoint ?: break
                val previousGradient = gradient
                evaluated = evaluate(accepted)
                for (j in 0 until paths) theta[j] = accepted[j]
                value = evaluated.first
                gradient = evaluated.second
                var numerator = 0.0
                var denominator = 0.0
                for (j in 0 until paths) {
                    numerator += gradient[j] * (gradient[j] - previousGradient[j])
                    denominator += previousGradient[j] * previousGradient[j]
                }
                val beta = if (denominator > 0.0) max(0.0, numerator / denominator) else 0.0
                direction = DoubleArray(paths) { -gradient[it] + beta * direction[it] }
                step = max(trial * 2.0, 1e-6)
                val weights = DoubleArray(paths) { exp(theta[it]) }
                val here = truth(weights)
                if (searchDecision(here) < searchDecision(bestTruth)) {
                    bestTruth = here
                    bestWeights = weights
                }
            }
        }

        Triple(index, bestWeights.toList(), bestTruth)
    }

    // Only the BEST of the starts is polished. `C-0058`'s coordinate descent costs about
    // `sweeps · n · (scan + refinements)` evaluations — two orders more than one conjugate-
    // gradient homotopy — so polishing every start would spend the whole budget on the losers.
    val bestStart = results.minWithOrNull(
        compareBy({ searchDecision(it.third) }, { it.first })
    )!!
    val polished = optimiseStiffnessDistribution(
        totalStiffness = totalStiffness,
        starts = listOf(bestStart.second),
        ceiling = ceiling,
        sweeps = polishSweeps,
        tolerance = 1e-5,
        searchHalfWidth = 1.0,
        scanPoints = 7,
        refinements = 8
    ) { searchDecision(surrogate.worstDishing(it, states)) }
    val stiffnesses =
        if (searchDecision(polished.objective) < searchDecision(bestStart.third)) {
            polished.stiffnesses
        } else {
            cappedStiffnesses(bestStart.second, totalStiffness, ceiling)
        }
    val worst = surrogate.worstDishing(stiffnesses, states)
    val perState = surrogate.peakDishing(stiffnesses)
    return MinimaxOptimum(
        stiffnesses = stiffnesses,
        worstDishing = worst,
        perStateDishing = states.map { perState[it] },
        bindingStates = states.filter { perState[it] >= worst * (1.0 - 1e-4) }
            .map { surrogate.stateNames[it] },
        startsWithinOnePartInAMillion =
            results.count { searchDecision(it.third) <= searchDecision(bestStart.third * (1.0 + 1e-6)) },
        startsUsed = starts.size
    )
}

// ------------------------------------------------------------------ buildability

/**
 * [stiffnesses] quantised onto at most [levels] distinct values, at the same [totalStiffness].
 *
 * The partition is the **optimal** contiguous one in the sorted order — a dynamic program over
 * within-cluster sums of squares, `O(n² L)` and exact, not a seeded `k`-means whose answer would
 * depend on its initialisation — and each cluster takes its own mean, which conserves the sum
 * identically. A final exact renormalisation removes the rounding.
 *
 * This is what `C-0060` prices: it builds a **two-level** coupling, so a robust distribution that
 * needs more levels, or a ratio outside `C-0060`'s measured `3.5 ≤ R ≤ 20` window, is a design
 * that its catalogue does not deliver.
 */
fun quantiseToLevels(
    stiffnesses: List<Double>,
    levels: Int,
    totalStiffness: Double
): List<Double> {
    require(stiffnesses.isNotEmpty()) { "stiffnesses must not be empty" }
    require(levels >= 1) { "levels must be at least 1, was: $levels" }
    require(totalStiffness > 0.0) { "totalStiffness must be positive, was: $totalStiffness" }
    require(stiffnesses.all { it > 0.0 && it.isFinite() }) { "every stiffness must be positive" }
    val order = stiffnesses.indices.sortedBy { stiffnesses[it] }
    val sorted = order.map { stiffnesses[it] }
    val count = sorted.size
    val groups = minOf(levels, count)
    val prefix = DoubleArray(count + 1)
    val square = DoubleArray(count + 1)
    for (i in 0 until count) {
        prefix[i + 1] = prefix[i] + sorted[i]
        square[i + 1] = square[i] + sorted[i] * sorted[i]
    }
    fun cost(from: Int, to: Int): Double {
        val n = to - from
        if (n <= 0) return 0.0
        val sum = prefix[to] - prefix[from]
        return square[to] - square[from] - sum * sum / n
    }
    val best = Array(groups + 1) { DoubleArray(count + 1) { Double.POSITIVE_INFINITY } }
    val cut = Array(groups + 1) { IntArray(count + 1) }
    best[0][0] = 0.0
    for (g in 1..groups) {
        for (end in g..count) {
            for (start in g - 1 until end) {
                val candidate = best[g - 1][start] + cost(start, end)
                if (candidate < best[g][end]) {
                    best[g][end] = candidate
                    cut[g][end] = start
                }
            }
        }
    }
    val quantised = DoubleArray(count)
    var end = count
    for (g in groups downTo 1) {
        val start = cut[g][end]
        val mean = (prefix[end] - prefix[start]) / (end - start)
        for (i in start until end) quantised[i] = mean
        end = start
    }
    val result = DoubleArray(count)
    order.forEachIndexed { position, original -> result[original] = quantised[position] }
    val sum = result.sum()
    return result.map { it * totalStiffness / sum }
}
