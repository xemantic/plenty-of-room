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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * The **cheap bound** on the tile-edge load non-uniformity, and the taper fit that turns a
 * solved lateral profile into the pair `C-0006`/`C-0009` consume — task `T-3b`, leaf `A7.4`.
 *
 * ## Why a cheap bound exists here at all
 *
 * `SESSION-PROMPT.md` requires the cheap bound before the expensive calculation, and this is
 * one of the cases where the cheap bound is not merely cheaper but *differently* one-sided,
 * so running it changes what the expensive calculation is allowed to claim.
 *
 * The bound has two halves:
 *
 * 1. **The width.** Linearise the 1-D solution and look for a lateral mode
 *    `δy(x, z) = φ(z) e^{−qx}`. It obeys `−φ'' + κ_loc²(z) φ = q² φ` with `φ(0) = 0` at the
 *    electrode, which is held at a potential, and `φ'(h) = 0` at the tile, which carries a
 *    fixed charge. Because `κ_loc² ≥ κ²` everywhere — counterion accumulation only strengthens
 *    the screening — the lowest eigenvalue obeys
 *    `q₀² ≥ κ² + (π/2h)²` and the taper's decay length is **at most**
 *    [transverseDecayRateBound]'s reciprocal. That is a *rigorous* upper bound within linear
 *    theory, and it already contradicts the 4 nm rim width `C-0006` assumed.
 * 2. **The depth.** A semi-infinite uniformly charged plane produces, on the plane and at its
 *    own edge, exactly **half** the potential of a complete plane — an exact superposition
 *    identity, since the screened Green's function is even about the edge line. So the rim
 *    behaves as if the tile carried `σ/2`, and `T-3a`'s closed-form linear mixed-boundary
 *    pressure [linearMixedDisjoiningPressure] evaluated at `σ/2` gives a depth.
 *
 * **The expected error is stated in advance, per §5**: the width bound is one-sided and tight
 * only in the linear theory; the depth is out by about a factor of two, because the
 * superposition ignores the electrode's image (which deepens the taper) and the linear theory
 * ignores the tile's charge saturation (which shallows it), and no argument here says which wins.
 */

/**
 * Returns the closed-form **lower** bound `√(κ² + (π/2h)²)` on the lateral decay rate of a
 * load perturbation at the tile edge, in `nm⁻¹`.
 *
 * Hence `1/`this is an **upper** bound on the taper width. Both terms are needed: at every §3
 * working gap the geometric term `(π/2h)²` is comparable to `κ²`, so the taper is narrower than
 * the Debye length and — the counter-intuitive half — it **narrows as the gap closes**, because
 * a thinner slit supports a faster-decaying lateral mode.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun transverseDecayRateBound(inverseDebyeLength: Double, gapHeight: Double): Double {
    require(inverseDebyeLength > 0.0) {
        "inverseDebyeLength must be positive, was: $inverseDebyeLength"
    }
    require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
    val geometric = PI / (2.0 * gapHeight)
    return sqrt(inverseDebyeLength * inverseDebyeLength + geometric * geometric)
}

/**
 * Returns the **local** squared screening length `κ_loc²(y) = −4π l_B (ε_ref/ε) dρ/dy` in
 * `nm⁻²` — the coefficient the linearised operator carries at reduced potential
 * [reducedPotential].
 *
 * At `y = 0` this is `4π l_B (4c + 2c) = 24π l_B c`, which **is** `κ²` for a 2:1 electrolyte.
 * Asserting it through that identity rather than through its formula is what would catch a
 * valency dropped from a Boltzmann factor, and it is the reason this function exists rather
 * than a literal `κ²` being written into the eigenproblem.
 */
fun localScreening(
    reducedPotential: Double,
    ionModel: IonModel,
    medium: GapMedium,
    bjerrumLength: Double,
    referencePermittivity: Double = WATER_RELATIVE_PERMITTIVITY
): Double = -4.0 * PI * bjerrumLength * referencePermittivity /
        medium.relativePermittivity * ionModel.chargeDensitySlope(reducedPotential, medium)

/**
 * Returns [localScreening] at every node of a solved 1-D gap, in `nm⁻²`.
 *
 * Read through the **public** surface of [GapSolution] rather than recomputed from a formula,
 * so that the cheap bound is linearised about the profile `T-3a` actually produced.
 */
fun localScreeningProfile(
    solution: GapSolution,
    ionModel: IonModel,
    medium: GapMedium,
    bjerrumLength: Double,
    referencePermittivity: Double = WATER_RELATIVE_PERMITTIVITY
): DoubleArray = DoubleArray(solution.reducedPotential.size) {
    localScreening(
        solution.reducedPotential[it], ionModel, medium, bjerrumLength, referencePermittivity
    )
}

/**
 * Returns the lowest lateral decay rate `q₀` in `nm⁻¹` of the transverse eigenproblem
 * `−φ'' + κ_loc²(z) φ = q² φ`, `φ(0) = 0`, `φ'(h) = 0`, discretised on the (possibly graded)
 * mesh [height] with [localScreening] at its nodes.
 *
 * Conservative finite volumes give a symmetric tridiagonal pencil `Aφ = λMφ` with `M` the cell
 * widths, and `A` is positive definite because the screening is, so **inverse iteration** is
 * unconditionally convergent onto the smallest eigenvalue. The separation is large — the next
 * mode sits at `κ² + (3π/2h)²`, a factor of three up at the working gaps — so the iteration
 * converges geometrically at about `0.3` per step.
 *
 * @throws IllegalArgumentException if the mesh is shorter than three nodes, not ascending,
 *         or does not start at zero, or if the arrays disagree in length.
 */
fun transverseDecayRate(height: DoubleArray, localScreening: DoubleArray): Double {
    require(height.size >= 3) { "height must have at least three nodes, was: ${height.size}" }
    require(height.size == localScreening.size) {
        "height and localScreening must agree in length, were: " +
                "${height.size} and ${localScreening.size}"
    }
    require(height[0] == 0.0) { "height must start at the electrode, was: ${height[0]}" }
    for (i in 1 until height.size) {
        require(height[i] > height[i - 1]) { "height must ascend, breaks at index $i" }
    }
    val nodes = height.size - 1
    val spacing = DoubleArray(nodes) { height[it + 1] - height[it] }
    // Unknowns 1..nodes; node 0 is the Dirichlet electrode. Cell widths are the dual boxes.
    val cell = DoubleArray(nodes + 1) { i ->
        when (i) {
            0 -> 0.0 // the electrode node is a Dirichlet datum and carries no cell
            nodes -> 0.5 * spacing[nodes - 1]
            else -> 0.5 * (spacing[i - 1] + spacing[i])
        }
    }
    val diagonal = DoubleArray(nodes + 1)
    val offDiagonal = DoubleArray(nodes + 1)
    for (i in 1..nodes) {
        val left = 1.0 / spacing[i - 1]
        val right = if (i < nodes) 1.0 / spacing[i] else 0.0
        diagonal[i] = left + right + localScreening[i] * cell[i]
        offDiagonal[i] = -right
    }
    var vector = DoubleArray(nodes + 1) { if (it == 0) 0.0 else 1.0 }
    var eigenvalue = 0.0
    repeat(400) {
        val rightHandSide = DoubleArray(nodes + 1) { if (it == 0) 0.0 else cell[it] * vector[it] }
        val next = solveSymmetricTridiagonal(diagonal, offDiagonal, rightHandSide, nodes)
        var numerator = 0.0
        var denominator = 0.0
        for (i in 1..nodes) {
            val neighbour = if (i < nodes) next[i + 1] else 0.0
            numerator += next[i] * (diagonal[i] * next[i] + offDiagonal[i] * neighbour +
                    (if (i > 1) offDiagonal[i - 1] * next[i - 1] else 0.0))
            denominator += cell[i] * next[i] * next[i]
        }
        val quotient = numerator / denominator
        var largest = 0.0
        for (i in 1..nodes) largest = maxOf(largest, abs(next[i]))
        for (i in 1..nodes) next[i] /= largest
        vector = next
        if (abs(quotient - eigenvalue) <= 1e-15 * abs(quotient)) {
            eigenvalue = quotient
            return sqrt(eigenvalue)
        }
        eigenvalue = quotient
    }
    return sqrt(eigenvalue)
}

/** Thomas algorithm for the symmetric tridiagonal pencil of [transverseDecayRate]. */
private fun solveSymmetricTridiagonal(
    diagonal: DoubleArray,
    offDiagonal: DoubleArray,
    rightHandSide: DoubleArray,
    nodes: Int
): DoubleArray {
    val sweep = DoubleArray(nodes + 1)
    val value = DoubleArray(nodes + 1)
    sweep[1] = offDiagonal[1] / diagonal[1]
    value[1] = rightHandSide[1] / diagonal[1]
    for (i in 2..nodes) {
        val pivot = diagonal[i] - offDiagonal[i - 1] * sweep[i - 1]
        sweep[i] = offDiagonal[i] / pivot
        value[i] = (rightHandSide[i] - offDiagonal[i - 1] * value[i - 1]) / pivot
    }
    val solution = DoubleArray(nodes + 1)
    solution[nodes] = value[nodes]
    for (i in nodes - 1 downTo 1) solution[i] = value[i] - sweep[i] * solution[i + 1]
    return solution
}

/**
 * Returns the cheap **depth** of the edge taper — the fraction by which the downward load is
 * reduced at the rim — from the exact half-plane superposition anchor.
 *
 * A semi-infinite uniformly charged plane produces exactly half the potential of a complete one
 * at its own edge, so the rim is evaluated at half the tile charge in the closed-form linear
 * mixed-boundary pressure. Zero when the tile is uncharged, because a rim with nothing to lose
 * loses nothing.
 *
 * **Expected error: about a factor of two**, one-sided in neither direction. Stated in advance.
 */
fun halfPlaneSuperpositionDepth(
    gapHeight: Double,
    electrodeReducedPotential: Double,
    tileSurfaceChargeDensity: Double,
    inverseDebyeLength: Double,
    bjerrumLength: Double
): Double {
    val interior = linearMixedDisjoiningPressure(
        gapHeight, electrodeReducedPotential, tileSurfaceChargeDensity,
        inverseDebyeLength, bjerrumLength
    )
    if (interior == 0.0) return 0.0
    val rim = linearMixedDisjoiningPressure(
        gapHeight, electrodeReducedPotential, 0.5 * tileSurfaceChargeDensity,
        inverseDebyeLength, bjerrumLength
    )
    return 1.0 - rim / interior
}

/**
 * The edge taper, in exactly the parameterisation
 * [com.xemantic.nano.plentyofroom.structure.edgeTaperedPressure] consumes — a **depth** and a
 * **width** — plus the raw quantities it was fitted from, so a downstream re-fit needs no re-run.
 *
 * @param interiorLoad the downward load far from the rim, in `pN/nm²`.
 * @param edgeLoad the downward load at the rim itself, in `pN/nm²`.
 * @param edgeDepth `1 − edgeLoad/interiorLoad`, read at the rim node.
 * @param depth the fitted taper depth, dimensionless, in `0..1`.
 * @param equivalentWidth the fitted rim width in nm.
 * @param loadDeficit `∫(interior − load) ds` in `pN/nm` per unit length of edge.
 * @param firstMoment `∫s(interior − load) ds` in `pN` per unit length of edge.
 * @param decayLength `firstMoment/loadDeficit` in nm — the deficit's own centroid distance from
 *        the rim, which is `1/q` exactly for an exponential taper and is therefore the quantity
 *        comparable against [transverseDecayRateBound].
 */
@Serializable
data class EdgeTaperFit(
    val interiorLoad: Double,
    val edgeLoad: Double,
    val edgeDepth: Double,
    val depth: Double,
    val equivalentWidth: Double,
    val loadDeficit: Double,
    val firstMoment: Double,
    val decayLength: Double
)

/**
 * `0.5/(1/4 − 1/π²) = 3.36288` — the constant turning the deficit's centroid into the width of
 * the raised cosine with the same two moments.
 *
 * Derived: for `q(s) = q∞[1 − d(1 + cos(πs/W))/2]` the zeroth moment of the deficit is
 * `d q∞ W/2` and the first is `d q∞ W²(1/4 − 1/π²)`, so `W` follows from their ratio alone
 * and `d` from either.
 */
const val RAISED_COSINE_MOMENT_RATIO: Double = 3.3628790424198723

/**
 * Fits the raised-cosine edge taper to a solved lateral load profile by **matching its first
 * two moments**, and returns the pair `C-0006`/`C-0009` consume.
 *
 * ## Why two moments and not the endpoint
 *
 * A 90° re-entrant corner at the rim carries a genuine field singularity — the tangential field
 * goes as `r^{−1/3}`, so the traction goes as `r^{−2/3}` — which is integrable but makes the
 * traction *at the rim node* mesh-dependent. The two things a plate on a foundation actually
 * responds to are the **total** edge load deficit and its **lever arm**, and both are moments,
 * so both are insensitive to a spike confined to a tenth of a nanometre. The endpoint reading is
 * emitted alongside as [EdgeTaperFit.edgeDepth] rather than suppressed.
 *
 * ## Why there is a standoff
 *
 * The corner traction is not merely mesh-dependent, it is mesh-**divergent**: refining `1 → 2 → 4`
 * takes the rim-node load through 10.8, 32.5, 90.8 `pN/nm²` while every other quantity converges
 * at second order. That is the `r^(−2/3)` traction of a 90-degree re-entrant corner, differentiated
 * once more by the shear-transfer step — and a real origami rim is a row of 2 nm duplex ends, not
 * a knife edge. Everything within [standoff] of the rim is therefore left out of the fit and
 * accounted for separately, against the **global momentum-flux** total which owes the corner nothing.
 *
 * @param distanceFromEdge ascending distances from the rim in nm, starting at 0.
 * @param load the downward load in `pN/nm²` at those distances.
 * @param interiorLoad the load far from the rim, in `pN/nm²`.
 * @param standoff the distance from the rim below which the profile is not used, in nm.
 * @throws IllegalArgumentException if the arrays disagree in length or hold fewer than two points.
 */
fun fitEdgeTaper(
    distanceFromEdge: DoubleArray,
    load: DoubleArray,
    interiorLoad: Double,
    standoff: Double = 0.0
): EdgeTaperFit {
    require(distanceFromEdge.size == load.size) {
        "distanceFromEdge and load must agree in length, were: " +
                "${distanceFromEdge.size} and ${load.size}"
    }
    require(distanceFromEdge.size >= 2) {
        "at least two samples are needed, was: ${distanceFromEdge.size}"
    }
    require(standoff >= 0.0) { "standoff cannot be negative, was: $standoff" }
    var zeroth = 0.0
    var first = 0.0
    var start = 0
    while (start < distanceFromEdge.size - 1 && distanceFromEdge[start] < standoff) start++
    for (i in start + 1 until distanceFromEdge.size) {
        val step = distanceFromEdge[i] - distanceFromEdge[i - 1]
        val left = interiorLoad - load[i - 1]
        val right = interiorLoad - load[i]
        zeroth += 0.5 * step * (left + right)
        first += 0.5 * step * (distanceFromEdge[i - 1] * left + distanceFromEdge[i] * right)
    }
    val scale = abs(interiorLoad) * (distanceFromEdge.last() - distanceFromEdge.first())
    if (abs(zeroth) <= 1e-14 * maxOf(scale, 1e-300)) {
        return EdgeTaperFit(
            interiorLoad = interiorLoad,
            edgeLoad = load[start],
            edgeDepth = 0.0,
            depth = 0.0,
            equivalentWidth = 0.0,
            loadDeficit = 0.0,
            firstMoment = 0.0,
            decayLength = 0.0
        )
    }
    val centroid = first / zeroth
    val width = RAISED_COSINE_MOMENT_RATIO * centroid
    val depth = 2.0 * zeroth / (interiorLoad * width)
    return EdgeTaperFit(
        interiorLoad = interiorLoad,
        edgeLoad = load[start],
        edgeDepth = 1.0 - load[start] / interiorLoad,
        depth = depth,
        equivalentWidth = width,
        loadDeficit = zeroth,
        firstMoment = first,
        decayLength = centroid
    )
}

/**
 * Returns the fraction of the total downward force a square tile of side [edgeLength] loses to
 * its four edges, given an [EdgeTaperFit] whose deficit is [EdgeTaperFit.loadDeficit] per unit
 * length of edge.
 *
 * The taper is mapped onto the square by the **minimum margin** to the boundary, which is what
 * [com.xemantic.nano.plentyofroom.structure.edgeTaperedPressure] does. Integrating that field by
 * layer cake — the level set at margin `m` has perimeter `4(L − 2m)` — gives the deficit exactly
 * as `4L·M₀ − 8·M₁` in the taper's own two moments, so the four corner squares are counted once
 * rather than twice and no separate corner term has to be invented.
 *
 * That mapping *understates* the corner deficit, because at a corner the load leaks in two
 * directions at once; the additive-deficit mapping overstates it. The two bracket the 3-D answer
 * this task does not solve.
 */
fun edgeForceFraction(fit: EdgeTaperFit, edgeLength: Double): Double {
    require(edgeLength > 0.0) { "edgeLength must be positive, was: $edgeLength" }
    if (fit.interiorLoad == 0.0) return 0.0
    return (4.0 * edgeLength * fit.loadDeficit - 8.0 * fit.firstMoment) /
            (edgeLength * edgeLength * fit.interiorLoad)
}
