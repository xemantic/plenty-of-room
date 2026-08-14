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

import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import kotlin.math.abs
import kotlin.math.min

/**
 * Task `T-122` — can a **5:1 per-path coupling stiffness ratio** be BUILT?
 *
 * `C-0058` made the Gen-1 tile flat for the first time by giving the 34 rim stations
 * **0.921 pN/nm** and the 11 interior ones **0.184**, at `C-0017`'s unchanged mandated total.
 * Its own validity range names the gap this file closes: *"nothing here says a per-path
 * stiffness can be BUILT to a prescribed value"*.
 *
 * ## The question is about a QUANTUM, and this project has met it before
 *
 * Every element of the catalogue is set by a length a DNA design chooses in **whole base pairs**
 * (`C-0023`'s flexure span and hinge arm, `C-0030`'s coupled span, `C-0039`'s elastica arm) or in
 * whole **nucleotides** (`C-0023`'s antagonistic pair), or by a whole number of **crossovers**.
 * So a prescribed stiffness is reachable only up to the ladder those integers make.
 *
 * `C-0023` ran exactly this test on a *preload* and it came out **negative**: a two-sided
 * preload is a mounting offset, i.e. a length; the requirement was **0.0409 nm**; the smallest
 * offset a design can build is one base-pair rise, **0.34 nm**; and the quantum therefore
 * delivered **9.3× too much**. *A design cannot set the preload it would need* — which is the
 * shape of answer this file has to be able to return.
 *
 * ## What decides it, in one division
 *
 * A bending element has `k ∝ p^(−3)` and a hinge `k ∝ p^(−2)`, so one quantum of the design
 * parameter is a **fractional** stiffness step of [powerLawGranularity], `|e|·q/p`. That number
 * is compared against the width of `C-0058`'s own **flat window in the ratio** (`5 ≤ R ≤ 20` at
 * the 6.70 nm collar) — not against the ratio itself, because a ratio that lands anywhere inside
 * that window builds a flat tile.
 *
 * ## Two conventions this file fixes
 *
 * 1. **A ladder is enumerated, never searched.** Every buildable setting between two integer
 *    bounds is evaluated and the nearest to the target is reported with the *step* around it, so
 *    deliverables 1–3 of `T-122` have no convergence parameter at all.
 * 2. **Scatter does not renormalise.** An assembly tolerance does not know the mandate, so
 *    [scatteredStiffnesses] multiplies the nominal per-path stiffnesses and lets the total drift;
 *    the drift is reported rather than removed.
 */

// ------------------------------------------------------------------ the quanta

/** The lengths a DNA design can actually set, and the sources they are cited from. */
object DesignQuanta {

    /**
     * The rise per base pair in nm — **CITED, MEASURED** (Douglas et al. 2009), via
     * `Gen1Tile.RISE_PER_BASE_PAIR`. The quantum of every duplex span and every hinge arm.
     */
    const val BASE_PAIR_RISE: Double = 0.34

    /**
     * The contour per nucleotide in nm, inextensible convention — **CITED, MEASURED**
     * (Sim et al. 2012; Bosco et al. 2014), via [SsDnaTether.CONTOUR_PER_NUCLEOTIDE].
     * The quantum of `C-0023`'s antagonistic ssDNA pair.
     *
     * **The convention travels with the number**: an extensible fit needs 0.57 nm instead, and
     * mixing the two double-counts the extension.
     */
    const val NUCLEOTIDE_CONTOUR: Double = SsDnaTether.CONTOUR_PER_NUCLEOTIDE
}

// ------------------------------------------------------------------ the ladder

/** One buildable setting of one element: an integer count of quanta and what it realises. */
data class BuildableSetting(

    /** The integer the builder sets — base pairs, nucleotides or crossovers. */
    val units: Int,

    /** `units × quantum` in nm (or the bare count, for a quantum of one). */
    val parameter: Double,

    /** The per-path stiffness in `pN/nm` the element realises there. */
    val stiffness: Double
)

/**
 * Every buildable setting of an element between [units].first and [units].last, at a design
 * parameter of [quantum] per unit, with [stiffnessOf] the element's own stiffness law.
 *
 * Enumerated rather than searched: the ladder is the design space, and reporting the *step*
 * around a target is the whole point.
 */
fun buildableLadder(
    quantum: Double,
    units: IntRange,
    stiffnessOf: (Double) -> Double
): List<BuildableSetting> {
    require(quantum > 0.0) { "quantum must be positive, was: $quantum" }
    require(units.first >= 1) { "the first unit count must be at least one, was: ${units.first}" }
    require(units.last >= units.first) {
        "the unit range must be non-empty, was: ${units.first}..${units.last}"
    }
    return units.map { count ->
        val parameter = count * quantum
        val stiffness = stiffnessOf(parameter)
        require(stiffness > 0.0 && stiffness.isFinite()) {
            "the stiffness law returned $stiffness at $parameter nm ($count units)"
        }
        BuildableSetting(count, parameter, stiffness)
    }
}

/** What a ladder can do about one prescribed stiffness. */
data class QuantisationVerdict(

    /** The prescribed per-path stiffness in `pN/nm`. */
    val target: Double,

    /** The buildable setting nearest to it. */
    val nearest: BuildableSetting,

    /** `|k_built − k_target|/k_target`, dimensionless. */
    val relativeError: Double,

    /**
     * The fractional stiffness step of the ladder at [nearest] — the mean of the steps to its
     * two neighbours, or the one step available at an end. **Exactly `0.0` when the ladder has a
     * single rung**, which is the honest report of "there is no granularity here", never a
     * sentinel infinity (`CLAUDE.md`).
     */
    val relativeGranularity: Double,

    /** Whether the target lies **between** two rungs, i.e. whether the ladder actually reaches it. */
    val bracketed: Boolean
)

/**
 * The rung of [ladder] nearest [target] in **relative** stiffness, with the ladder's own step
 * there.
 *
 * Relative rather than absolute because the two levels of `C-0058`'s design differ by 5× and an
 * absolute nearest would be biased toward the stiff one — the same discipline `CLAUDE.md`
 * records for comparing two quantities of different scale.
 */
fun nearestBuildable(ladder: List<BuildableSetting>, target: Double): QuantisationVerdict {
    require(ladder.isNotEmpty()) { "ladder must not be empty" }
    require(target > 0.0) { "target must be positive, was: $target" }
    val index = ladder.indices.minByOrNull {
        abs(ladder[it].stiffness - target) / target
    }!!
    val nearest = ladder[index]
    val below = ladder.getOrNull(index - 1)
    val above = ladder.getOrNull(index + 1)
    val steps = listOfNotNull(below, above).map {
        abs(it.stiffness - nearest.stiffness) / nearest.stiffness
    }
    val bracketed = ladder.any { it.stiffness <= target } && ladder.any { it.stiffness >= target }
    return QuantisationVerdict(
        target = target,
        nearest = nearest,
        relativeError = abs(nearest.stiffness - target) / target,
        relativeGranularity = if (steps.isEmpty()) 0.0 else steps.average(),
        bracketed = bracketed
    )
}

/**
 * The cheap bound: the fractional stiffness step of a pure power law `k ∝ p^exponent` at
 * parameter [parameter] with a quantum of [quantum] — `|e|·q/p`.
 *
 * One division per element, and it is what the declared falsifier is written on. It is exact in
 * the limit `q ≪ p` and is asserted against the enumerated ladder rather than trusted.
 */
fun powerLawGranularity(exponent: Double, parameter: Double, quantum: Double): Double {
    require(parameter > 0.0) { "parameter must be positive, was: $parameter" }
    require(quantum > 0.0) { "quantum must be positive, was: $quantum" }
    return abs(exponent) * quantum / parameter
}

/**
 * The fractional granularity of the **total** when one path of a coupling summing to [total] can
 * be moved by [stiffnessStep] `pN/nm`.
 *
 * The second cheap bound, and it is why `C-0017`'s mandate survives quantisation: the mandate is
 * an equality on a **sum**, one path may be re-cut independently of the others, so the total's
 * relative granularity is the per-path one **divided by the path count** — 45× finer here.
 */
fun relativeTotalGranularity(stiffnessStep: Double, total: Double): Double {
    require(stiffnessStep >= 0.0) { "stiffnessStep must not be negative, was: $stiffnessStep" }
    require(total > 0.0) { "total must be positive, was: $total" }
    return stiffnessStep / total
}

// ------------------------------------------------------------------ the two-level design

/**
 * Which stations of [grid] are **rim** stations under a collar of [collarWidth] nm — `true` where
 * the distance to the nearest tile edge is at most the collar.
 *
 * The boolean form of [rimStiffenedWeights], so that a two-level design can carry two *built*
 * stiffnesses rather than two weights.
 */
fun rimMask(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double,
    collarWidth: Double
): List<Boolean> {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    require(edgeX > 0.0 && edgeY > 0.0) { "the tile edges must be positive" }
    require(collarWidth > 0.0) { "collarWidth must be positive, was: $collarWidth" }
    return grid.map { (x, y) ->
        val distance = min(edgeX / 2.0 - abs(x), edgeY / 2.0 - abs(y))
        require(distance >= 0.0) { "an attachment at ($x, $y) lies outside the tile" }
        distance <= collarWidth
    }
}

/**
 * The per-path stiffnesses of a **built** two-level coupling: [rimStiffness] where [mask] is
 * `true` and [interiorStiffness] elsewhere.
 *
 * Unlike [normalisedStiffnesses] this does **not** rescale to the mandate — the two levels are
 * what the ladder realised, and whether their sum still meets `C-0017`'s equality is a result.
 */
fun twoLevelStiffnesses(
    mask: List<Boolean>,
    rimStiffness: Double,
    interiorStiffness: Double
): List<Double> {
    require(mask.isNotEmpty()) { "mask must not be empty" }
    require(rimStiffness > 0.0) { "rimStiffness must be positive, was: $rimStiffness" }
    require(interiorStiffness > 0.0) {
        "interiorStiffness must be positive, was: $interiorStiffness"
    }
    return mask.map { if (it) rimStiffness else interiorStiffness }
}

/** A per-path choice of rungs, and how close its sum lands to a prescribed total. */
data class TrimmedDesign(

    /** The rung chosen for each path, in the order of the targets. */
    val settings: List<BuildableSetting>,

    /** `Σ k_i` in `pN/nm`. */
    val total: Double,

    /** `|Σ k_i − total|/total`, dimensionless. */
    val relativeError: Double,

    /** How many single-rung moves the trim made away from the per-path nearest. */
    val moves: Int,

    /** The same error before any move — the untrimmed, level-by-level rounding. */
    val relativeErrorBeforeTrimming: Double
)

/**
 * Per-path rungs of one [ladder] chosen so that their **sum** lands as close to [total] as single
 * moves allow, starting from each path's own nearest rung and never moving any path more than
 * [maximumSteps] rungs away from it.
 *
 * This is the second cheap bound, made executable. `C-0017`'s mandate is an equality on a **sum**
 * and a builder cuts each path separately, so the total is settable on a lattice `n` times finer
 * than any single path's: rounding 45 paths independently misses the mandate by up to half a
 * path-step, and moving one path by one rung recovers it.
 *
 * Deterministic by construction — the first strictly improving move in path order wins any tie —
 * because `CLAUDE.md` records that an argmin over a flat objective is not reproducible otherwise.
 */
fun trimmedToTotal(
    targets: List<Double>,
    ladder: List<BuildableSetting>,
    total: Double,
    maximumSteps: Int = 1
): TrimmedDesign {
    require(targets.isNotEmpty()) { "targets must not be empty" }
    require(targets.all { it > 0.0 }) { "every target must be positive, were: $targets" }
    require(ladder.isNotEmpty()) { "ladder must not be empty" }
    require(total > 0.0) { "total must be positive, was: $total" }
    require(maximumSteps >= 0) { "maximumSteps must not be negative, was: $maximumSteps" }
    val start = targets.map { target ->
        ladder.indices.minByOrNull { abs(ladder[it].stiffness - target) / target }!!
    }
    val chosen = start.toMutableList()
    fun sum() = chosen.sumOf { ladder[it].stiffness }
    val before = abs(sum() - total) / total
    var moves = 0
    repeat(targets.size * (2 * maximumSteps + 1)) {
        var bestPath = -1
        var bestIndex = -1
        var bestError = abs(sum() - total)
        targets.indices.forEach { path ->
            listOf(-1, 1).forEach { direction ->
                val candidate = chosen[path] + direction
                if (candidate in ladder.indices &&
                    abs(candidate - start[path]) <= maximumSteps
                ) {
                    val trial = sum() - ladder[chosen[path]].stiffness +
                            ladder[candidate].stiffness
                    val error = abs(trial - total)
                    if (error < bestError * (1.0 - 1e-15)) {
                        bestError = error
                        bestPath = path
                        bestIndex = candidate
                    }
                }
            }
        }
        if (bestPath < 0) return TrimmedDesign(
            settings = chosen.map { ladder[it] },
            total = sum(),
            relativeError = abs(sum() - total) / total,
            moves = moves,
            relativeErrorBeforeTrimming = before
        )
        chosen[bestPath] = bestIndex
        moves++
    }
    return TrimmedDesign(
        settings = chosen.map { ladder[it] },
        total = sum(),
        relativeError = abs(sum() - total) / total,
        moves = moves,
        relativeErrorBeforeTrimming = before
    )
}

/**
 * The relative scatter amplitude at which two populations differing by [ratio] first **overlap**:
 * `(R − 1)/(R + 1)`, exactly.
 *
 * The stiff population's floor is `k(1 − ε)` and the soft one's ceiling `k(1 + ε)/R`; they meet
 * at `(R − 1)/(R + 1)`. **Two thirds at `R = 5`** — which is why the flatness verdict, and not
 * the ordering of the two populations, is what a build tolerance actually threatens.
 */
fun populationOverlapScatter(ratio: Double): Double {
    require(ratio >= 1.0) { "ratio must be at least one, was: $ratio" }
    return (ratio - 1.0) / (ratio + 1.0)
}

// ------------------------------------------------------------------ the scatter

/**
 * [nominal] multiplied path by path by `C-0026`'s own [pattern] at relative amplitude [epsilon]
 * on a grid of [columns] columns.
 *
 * **The total is not renormalised**: a build tolerance does not know the mandate, and the drift
 * it causes is a result rather than a nuisance. `C-0026`'s finding that a scatter alternating
 * *along* the helices restores exactly zero crossover force where one alternating *across* them
 * restores 0.088 pN is a statement about this same multiplier, and it is re-asserted here on an
 * **unequal** nominal.
 */
fun scatteredStiffnesses(
    nominal: List<Double>,
    columns: Int,
    pattern: ScatterPattern,
    epsilon: Double
): List<Double> {
    require(nominal.isNotEmpty()) { "nominal must not be empty" }
    require(columns > 0) { "columns must be positive, was: $columns" }
    require(epsilon >= 0.0 && epsilon < 1.0) {
        "epsilon must be in [0, 1) — at 1 a path's stiffness vanishes, was: $epsilon"
    }
    return nominal.mapIndexed { index, stiffness ->
        stiffness * pattern.multiplier(index, columns, epsilon)
    }
}

/** Where a metric first reaches a limit as the scatter amplitude grows, and how well it is known. */
data class ScatterThreshold(

    /**
     * The amplitude at which the metric first reaches the limit — or the scan's own [ceiling]
     * when it never does, in which case [reachesTheLimit] is `false`.
     *
     * **Never `Infinity`** (`CLAUDE.md`): "no threshold in range" is reported by the flag, not by
     * a value `kotlinx.serialization` refuses to encode.
     */
    val threshold: Double,

    /** Whether the metric reached the limit anywhere inside the scan. */
    val reachesTheLimit: Boolean,

    /** The scan's upper end. */
    val ceiling: Double,

    /** The width of the final bracket — the bisection's own convergence report. */
    val bracketWidth: Double,

    /** The metric at zero scatter. */
    val metricAtZero: Double,

    /** The metric at the scan's ceiling. */
    val metricAtCeiling: Double
)

/**
 * The scatter amplitude at which [metric] first reaches [limit], scanned over `[0, maximum]` in
 * [scanSteps] intervals and then bisected on the **bracket width**.
 *
 * Monotonicity is **not** assumed — the first sign change is taken, exactly as `C-0018` and
 * `C-0030` do, because a dishing field need not be monotone in a scatter amplitude and a design
 * that first crosses the tolerance and later falls back below it has still failed at the crossing.
 */
fun scatterThreshold(
    maximum: Double,
    scanSteps: Int,
    limit: Double,
    metric: (Double) -> Double
): ScatterThreshold {
    require(maximum > 0.0) { "maximum must be positive, was: $maximum" }
    require(scanSteps >= 4) { "scanSteps must be at least 4, was: $scanSteps" }
    val atZero = metric(0.0)
    val atCeiling = metric(maximum)
    fun residual(epsilon: Double) = metric(epsilon) - limit
    if (residual(0.0) >= 0.0) return ScatterThreshold(
        threshold = 0.0,
        reachesTheLimit = true,
        ceiling = maximum,
        bracketWidth = 0.0,
        metricAtZero = atZero,
        metricAtCeiling = atCeiling
    )
    var low = 0.0
    var high = maximum
    var found = false
    val step = maximum / scanSteps
    var scan = 0.0
    for (i in 1..scanSteps) {
        val next = scan + step
        if (residual(next) >= 0.0) {
            low = scan
            high = next
            found = true
            break
        }
        scan = next
    }
    if (!found) return ScatterThreshold(
        threshold = maximum,
        reachesTheLimit = false,
        ceiling = maximum,
        bracketWidth = 0.0,
        metricAtZero = atZero,
        metricAtCeiling = atCeiling
    )
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (residual(middle) < 0.0) low = middle else high = middle
        if (high - low <= 1e-12 * maximum) return ScatterThreshold(
            threshold = 0.5 * (low + high),
            reachesTheLimit = true,
            ceiling = maximum,
            bracketWidth = high - low,
            metricAtZero = atZero,
            metricAtCeiling = atCeiling
        )
    }
    return ScatterThreshold(
        threshold = 0.5 * (low + high),
        reachesTheLimit = true,
        ceiling = maximum,
        bracketWidth = high - low,
        metricAtZero = atZero,
        metricAtCeiling = atCeiling
    )
}

/**
 * The realised **secant** ratio `k_stiff(s)/k_soft(s)` of two elements of the same family cut to
 * two different design parameters, at each stroke of [strokes].
 *
 * `C-0058`'s springs are linear and `C-0030`'s realised coupling **strain-softens**
 * (`CH-0042`, `C-0032`), so two spans do not stay in a fixed ratio over the stroke. The drift is
 * the price of realising a distribution with a nonlinear element, and it is reported rather than
 * linearised away.
 */
fun realisedSecantRatio(
    stiff: (Double) -> Double,
    soft: (Double) -> Double,
    strokes: List<Double>
): List<Double> {
    require(strokes.isNotEmpty()) { "strokes must not be empty" }
    require(strokes.all { it > 0.0 }) { "every stroke must be positive, were: $strokes" }
    return strokes.map { stroke ->
        val denominator = soft(stroke)
        require(denominator > 0.0) {
            "the soft element's secant is not positive at $stroke nm: $denominator"
        }
        stiff(stroke) / denominator
    }
}
