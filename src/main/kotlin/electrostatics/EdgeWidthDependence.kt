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

package com.xemantic.nano.plentyofroom.electrostatics

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * The **cheap bound** on how much `C-0022`'s solved collar depends on the tile's own half-width —
 * task `T-160`, leaf `A2.2`, raised by `C-0090`'s *Still open* item 1.
 *
 * ## The question this answers before a solve is spent
 *
 * `C-0086`/`C-0095` quantise the tile's along-helix width to a whole number of scaffold-crossover
 * pitches, so §3's nominal 40.0 nm becomes **38.08 nm** and the half-width `a` moves
 * `20.00 → 19.04 nm`. `C-0090` re-read the whole anchoring branch at that width and **carried
 * `C-0022`'s collar terms unchanged**, on the argument that the collar is a sub-Debye rim feature.
 *
 * That argument is right about the *physics* and silent about the *fit*.
 *
 * - **Physically** the collar cannot move: the solved deficit centroid is 2.66 nm and the far tail
 *   decays over about 2.1 nm, so the two rims of a 38 nm tile are eighteen decay lengths apart and
 *   their interaction is `e^{−18}`.
 * - **But [fitEdgeTaper] references the profile to the centre-line load and truncates both of its
 *   moments there**, and `width = 3.3629 × M₁/M₀` carries `a` in all three places. A narrower tile
 *   puts its centre-line further up the tail, which raises the reference and shortens the domain.
 *
 * So the whole width-dependence of the taper depth, the taper width and the rim residual is one
 * exponentially small number — the centre-line's own excess over the true asymptote,
 * `τ(a) = p(a) − Π∞` — and it has a closed form.
 *
 * ## The closed form
 *
 * Model the profile beyond the near-rim structure as `p(s) = Π∞ + A e^{−s/ℓ}`, with `s` measured
 * **inward from the rim**. Then, with `σ` the fit's rim standoff,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`M₀(a) = E + τ(a)(a − σ + ℓ)`,&nbsp;&nbsp;`E = −A ℓ e^{−σ/ℓ}`
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`M₁(a) = F + τ(a)[(a² − σ²)/2 + ℓ(a + ℓ)]`,&nbsp;&nbsp;`F = −A ℓ (σ + ℓ) e^{−σ/ℓ}`
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`D(a) = E₀ + τ(a)(a + ℓ)`,&nbsp;&nbsp;`E₀ = −A ℓ`
 *
 * where `D` is the **global** deficit (the same integral with `σ = 0`), so that the rim residual is
 * `D(a) − M₀(a)`. `E`, `F` and `E₀` contain no `a` **by construction**, which is the statement that
 * the collar is width-independent; everything that moves does so through `τ(a)`, and `τ` is
 * calibrated once from the reference solve rather than assumed.
 *
 * ## Why it is written as executable code rather than as a paragraph
 *
 * Because it is falsifiable. Calibrate [CollarTailModel] on the fit at one half-width, predict the
 * fit at another, and compare against [fitEdgeTaper] itself — which is what the `T-160` gate tests
 * do on a synthetic profile, and what the 2-D solve does on the real one.
 */

/** The far-field exponential tail of a solved lateral load profile, fitted rather than assumed. */
@Serializable
data class EdgeTailFit(
    /** `Π∞`, the load the profile relaxes to far from the rim, in `pN/nm²`. */
    val asymptoticLoad: Double,
    /** `A`, the tail's amplitude extrapolated back to the rim, in `pN/nm²`. */
    val amplitude: Double,
    /** `ℓ`, the tail's decay length in nm. */
    val decayLength: Double,
    /** The rms fit residual relative to `|Π∞|` — dimensionless, and a convergence measure. */
    val relativeResidual: Double
)

/**
 * The number of `ℓ` values scanned before the golden-section refinement.
 *
 * A scan and then a refinement, rather than a Newton iteration: the objective is a sum of squares
 * of a *nonlinear* parameter and it is not convex in `ℓ`, so a local method started anywhere can
 * land on the flat branch where `e^{−s/ℓ}` is indistinguishable from a constant.
 */
private const val TAIL_SCAN_POINTS: Int = 512

/**
 * Fits `p(s) = Π∞ + A e^{−s/ℓ}` to the samples of [load] whose [distanceFromEdge] lies in
 * `[from, to]`, by a deterministic scan in `ℓ` with a linear least squares for `(Π∞, A)` inside it.
 *
 * The window exists because the near-rim structure of a solved 2-D edge is **not** an exponential
 * tail — inside about half a nanometre of a sharp 90° rim `C-0022`'s load reverses sign — and a
 * fit that includes it measures the corner rather than the tail.
 *
 * @throws IllegalArgumentException if the arrays disagree in length, if [distanceFromEdge] does not
 *         ascend, if the window is empty or inverted, or if it holds fewer than three samples.
 */
fun fitExponentialTail(
    distanceFromEdge: DoubleArray,
    load: DoubleArray,
    from: Double,
    to: Double,
    decayLengthLow: Double = 0.2,
    decayLengthHigh: Double = 20.0
): EdgeTailFit {
    require(distanceFromEdge.size == load.size) {
        "distanceFromEdge and load must agree in length, were: " +
                "${distanceFromEdge.size} and ${load.size}"
    }
    for (i in 1 until distanceFromEdge.size) {
        require(distanceFromEdge[i] > distanceFromEdge[i - 1]) {
            "distanceFromEdge must ascend, breaks at index $i"
        }
    }
    require(to > from) { "the window must be ascending, was: [$from, $to]" }
    require(decayLengthHigh > decayLengthLow && decayLengthLow > 0.0) {
        "the decay-length bracket must be positive and ascending, was: " +
                "[$decayLengthLow, $decayLengthHigh]"
    }
    val indices = distanceFromEdge.indices.filter {
        distanceFromEdge[it] >= from && distanceFromEdge[it] <= to
    }
    require(indices.size >= 3) {
        "at least three samples must fall in [$from, $to], there were ${indices.size}"
    }
    val origin = distanceFromEdge[indices.first()]
    val s = DoubleArray(indices.size) { distanceFromEdge[indices[it]] - origin }
    val p = DoubleArray(indices.size) { load[indices[it]] }

    // The residual sum of squares at one decay length, minimised over the two LINEAR parameters.
    fun residual(decay: Double): Double {
        var sumB = 0.0
        var sumBB = 0.0
        var sumP = 0.0
        var sumBP = 0.0
        for (i in s.indices) {
            val b = exp(-s[i] / decay)
            sumB += b
            sumBB += b * b
            sumP += p[i]
            sumBP += b * p[i]
        }
        val n = s.size.toDouble()
        val determinant = n * sumBB - sumB * sumB
        if (determinant == 0.0) return Double.MAX_VALUE
        val amplitude = (n * sumBP - sumB * sumP) / determinant
        val asymptote = (sumP - amplitude * sumB) / n
        var total = 0.0
        for (i in s.indices) {
            val error = p[i] - asymptote - amplitude * exp(-s[i] / decay)
            total += error * error
        }
        return total
    }

    val logLow = ln(decayLengthLow)
    val logHigh = ln(decayLengthHigh)
    var bestIndex = 0
    var best = Double.MAX_VALUE
    for (i in 0 until TAIL_SCAN_POINTS) {
        val decay = exp(logLow + (logHigh - logLow) * i / (TAIL_SCAN_POINTS - 1.0))
        val value = residual(decay)
        // Strictly less: the EARLIER candidate wins a tie, per CLAUDE.md's decision-precision rule.
        if (value < best) {
            best = value
            bestIndex = i
        }
    }
    val step = (logHigh - logLow) / (TAIL_SCAN_POINTS - 1.0)
    var low = logLow + step * maxOf(0, bestIndex - 1)
    var high = logLow + step * minOf(TAIL_SCAN_POINTS - 1, bestIndex + 1)
    val phi = 0.5 * (sqrt(5.0) - 1.0)
    var c = high - phi * (high - low)
    var d = low + phi * (high - low)
    var fc = residual(exp(c))
    var fd = residual(exp(d))
    repeat(200) {
        if (high - low <= 1e-12 * (abs(low) + abs(high) + 1.0)) return@repeat
        if (fc <= fd) {
            high = d
            d = c
            fd = fc
            c = high - phi * (high - low)
            fc = residual(exp(c))
        } else {
            low = c
            c = d
            fc = fd
            d = low + phi * (high - low)
            fd = residual(exp(d))
        }
    }
    val decay = exp(0.5 * (low + high))

    var sumB = 0.0
    var sumBB = 0.0
    var sumP = 0.0
    var sumBP = 0.0
    for (i in s.indices) {
        val b = exp(-s[i] / decay)
        sumB += b
        sumBB += b * b
        sumP += p[i]
        sumBP += b * p[i]
    }
    val n = s.size.toDouble()
    val determinant = n * sumBB - sumB * sumB
    val shiftedAmplitude = (n * sumBP - sumB * sumP) / determinant
    val asymptote = (sumP - shiftedAmplitude * sumB) / n
    var total = 0.0
    for (i in s.indices) {
        val error = p[i] - asymptote - shiftedAmplitude * exp(-s[i] / decay)
        total += error * error
    }
    val scale = if (asymptote == 0.0) 1.0 else abs(asymptote)
    return EdgeTailFit(
        asymptoticLoad = asymptote,
        amplitude = shiftedAmplitude * exp(origin / decay),
        decayLength = decay,
        relativeResidual = sqrt(total / n) / scale
    )
}

/**
 * [fitEdgeTaper] with the rim standoff placed **exactly** at [standoff] by linear interpolation,
 * rather than snapped to the first mesh node at or beyond it.
 *
 * ## Why this exists, and it is a `T-160` finding rather than a convenience
 *
 * `fitEdgeTaper` starts its quadrature at the first sample at or beyond the standoff. On the
 * graded lateral mesh of [PoissonBoltzmannEdge] the node nearest 1 nm from the rim sits ~0.04 nm
 * away, and the integrand there is the **peak** of the edge enhancement — `1.88 ×` the interior
 * load — so one node of slack moves the deficit by a few per cent. The tile half-width rescales
 * the whole graded mesh, so **two tile widths snap to two different standoffs**, and the partition
 * of the edge effect between `C-0022`'s smooth term and its rim residual then moves by more than
 * the physics does.
 *
 * The sum of the two is untouched — it is the global momentum flux, which owes nothing to any of
 * this — so a consumer that applies **both** collar terms is safe and one that applies **one** of
 * them is reading a mesh. This function is how that is measured rather than argued.
 *
 * @throws IllegalArgumentException if [standoff] is negative or is not strictly inside the samples.
 */
fun taperFitAtExactStandoff(
    distanceFromEdge: DoubleArray,
    load: DoubleArray,
    interiorLoad: Double,
    standoff: Double
): EdgeTaperFit {
    require(distanceFromEdge.size == load.size) {
        "distanceFromEdge and load must agree in length, were: " +
                "${distanceFromEdge.size} and ${load.size}"
    }
    require(distanceFromEdge.size >= 2) {
        "at least two samples are needed, was: ${distanceFromEdge.size}"
    }
    require(standoff >= 0.0) { "standoff cannot be negative, was: $standoff" }
    require(standoff < distanceFromEdge.last()) {
        "standoff must lie inside the samples, was $standoff against " +
                "${distanceFromEdge.last()}"
    }
    var upper = 0
    while (upper < distanceFromEdge.size - 1 && distanceFromEdge[upper] < standoff) upper++
    if (distanceFromEdge[upper] == standoff) {
        return fitEdgeTaper(distanceFromEdge, load, interiorLoad, standoff)
    }
    val lower = upper - 1
    val span = distanceFromEdge[upper] - distanceFromEdge[lower]
    val fraction = (standoff - distanceFromEdge[lower]) / span
    val interpolated = load[lower] + fraction * (load[upper] - load[lower])
    val kept = distanceFromEdge.size - upper
    val distance = DoubleArray(kept + 1)
    val values = DoubleArray(kept + 1)
    distance[0] = standoff
    values[0] = interpolated
    for (i in 0 until kept) {
        distance[i + 1] = distanceFromEdge[upper + i]
        values[i + 1] = load[upper + i]
    }
    return fitEdgeTaper(distance, values, interiorLoad, standoff)
}

/**
 * `C-0022`'s taper fit and global deficit, predicted at **any** tile half-width from a single
 * solved reference — the closed form derived in this file's header, and the cheap bound `T-160`
 * runs before it spends a second 2-D solve.
 *
 * Every field is in the locked units: lengths nm, loads `pN/nm²`, line loads `pN/nm`.
 *
 * @param referenceHalfWidth the `a` the reference quantities were solved at, nm.
 * @param standoff the fit's rim standoff `σ`, nm.
 * @param decayLength the far tail's `ℓ`, nm — from [fitExponentialTail], or from the transverse
 *        eigenvalue's rigorous **ceiling** when a pessimistic anchoring is wanted.
 * @param asymptoticLoad `Π∞`, the load far from the rim.
 * @param centrelineExcess `τ(a₀) = p(a₀) − Π∞`, the reference centre-line's own excess.
 * @param loadDeficit `M₀(a₀)`, `∫_σ^{a₀}(I − p) ds`, negative for an enhancement.
 * @param firstMoment `M₁(a₀)`.
 * @param totalDeficit `D(a₀)`, the global deficit including the unresolvable corner.
 */
@Serializable
data class CollarTailModel(
    val referenceHalfWidth: Double,
    val standoff: Double,
    val decayLength: Double,
    val asymptoticLoad: Double,
    val centrelineExcess: Double,
    val loadDeficit: Double,
    val firstMoment: Double,
    val totalDeficit: Double
) {

    init {
        require(referenceHalfWidth > standoff) {
            "referenceHalfWidth must exceed the standoff, were: " +
                    "$referenceHalfWidth and $standoff"
        }
        require(standoff >= 0.0) { "standoff cannot be negative, was: $standoff" }
        require(decayLength > 0.0) { "decayLength must be positive, was: $decayLength" }
    }

    /** `E = M₀ − τ(a₀)(a₀ − σ + ℓ)` — the untruncated deficit, width-independent by construction. */
    val untruncatedDeficit: Double =
        loadDeficit - centrelineExcess * (referenceHalfWidth - standoff + decayLength)

    /** `F` — the untruncated first moment, width-independent by construction. */
    val untruncatedFirstMoment: Double = firstMoment - centrelineExcess * (
            0.5 * (referenceHalfWidth * referenceHalfWidth - standoff * standoff) +
                    decayLength * (referenceHalfWidth + decayLength)
            )

    /** `E₀` — the untruncated GLOBAL deficit, the same integral taken from the rim itself. */
    val untruncatedTotalDeficit: Double =
        totalDeficit - centrelineExcess * (referenceHalfWidth + decayLength)

    /** `τ(a) = τ(a₀) e^{(a₀ − a)/ℓ}` — the only thing in the whole construction that carries `a`. */
    fun centrelineExcessAt(halfWidth: Double): Double =
        centrelineExcess * exp((referenceHalfWidth - halfWidth) / decayLength)

    /**
     * The collar as `C-0022` would report it for a tile of half-width [halfWidth].
     *
     * @throws IllegalArgumentException if [halfWidth] does not exceed the standoff.
     */
    fun at(halfWidth: Double): PredictedCollar {
        require(halfWidth > standoff) {
            "halfWidth must exceed the standoff $standoff, was: $halfWidth"
        }
        val excess = centrelineExcessAt(halfWidth)
        val interior = asymptoticLoad + excess
        val zeroth = untruncatedDeficit + excess * (halfWidth - standoff + decayLength)
        val first = untruncatedFirstMoment + excess * (
                0.5 * (halfWidth * halfWidth - standoff * standoff) +
                        decayLength * (halfWidth + decayLength)
                )
        val global = untruncatedTotalDeficit + excess * (halfWidth + decayLength)
        val centroid = first / zeroth
        val width = RAISED_COSINE_MOMENT_RATIO * centroid
        val rim = global - zeroth
        return PredictedCollar(
            halfWidth = halfWidth,
            interiorLoad = interior,
            loadDeficit = zeroth,
            firstMoment = first,
            centroid = centroid,
            taperWidth = width,
            taperDepth = 2.0 * zeroth / (interior * width),
            totalDeficit = global,
            rimResidual = rim,
            rimResidualDepth = 2.0 * rim / (interior * standoff)
        )
    }

}

/** `C-0022`'s three collar deliverables, at one tile half-width. */
@Serializable
data class PredictedCollar(
    val halfWidth: Double,
    val interiorLoad: Double,
    val loadDeficit: Double,
    val firstMoment: Double,
    val centroid: Double,
    val taperWidth: Double,
    val taperDepth: Double,
    val totalDeficit: Double,
    val rimResidual: Double,
    val rimResidualDepth: Double
)

/**
 * The largest of the three relative movements between two collars — the number a width-independence
 * verdict is taken on, against `C-0090`'s declared 0.32 % placement sensitivity.
 *
 * The three are exactly `C-0090`'s three consumed quantities: the smooth term's depth and width,
 * and the rim residual's depth. Taken as a **maximum** rather than as an average, because a
 * dishing is linear in each of them separately and an average would let one hide behind another.
 */
fun collarDeparture(collar: PredictedCollar, reference: PredictedCollar): Double = maxOf(
    relativeMovement(collar.taperDepth, reference.taperDepth),
    relativeMovement(collar.taperWidth, reference.taperWidth),
    relativeMovement(collar.rimResidualDepth, reference.rimResidualDepth)
)

private fun relativeMovement(value: Double, reference: Double): Double =
    if (reference == 0.0) abs(value) else abs(value / reference - 1.0)
