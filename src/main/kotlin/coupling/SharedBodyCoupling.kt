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

import com.xemantic.nano.plentyofroom.brush.bracketedRoot
import com.xemantic.nano.plentyofroom.structure.CholeskyDecomposition
import com.xemantic.nano.plentyofroom.structure.OrthotropicPlate
import com.xemantic.nano.plentyofroom.structure.gaussLegendreRule
import com.xemantic.nano.plentyofroom.structure.legendreJet
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow

/**
 * `T-162` — a coupling that is **not an array**: the tile tied at many stations to one **shared
 * body**, which is itself grounded.
 *
 * ## The one line that separates the two topologies
 *
 * Write `M` for the free tile's compliance at the stations, `T = diag(t)` for the tie stiffnesses
 * and `w_free` for the tile's own deflection there under the load. An **array** of independent
 * paths to ground and a **shared body** differ by exactly one term:
 *
 * ```
 * array          (T⁻¹ + M) f = w_free
 * shared body    (T⁻¹ + M + F_b) f = w_free,     F_b = Φ H⁻¹ Φᵀ
 * ```
 *
 * `Φ` is the body's shape functions at the stations and `H` its own stiffness — bending plus
 * ground — in those coordinates. **The shared body is an additive compliance in series with the
 * ties, and nothing else**, so the array is its `F_b = 0` corner and one code path carries both.
 * Everything `C-0058`, `C-0063`, `C-0087` and `C-0089` publish is that corner and is reproduced
 * bit for bit by [InfluenceSurrogate.solveWithDropout], which now delegates here.
 *
 * ## Why that changes the count argument
 *
 * `C-0017`'s mandate is an equality on the coupling's **heave secant**. In an array that secant
 * is `Σ tᵢ`, so at 34 paths every station is supported at `33.3333/34 = 0.98 pN/nm` and the
 * mandate is a *per-station* budget. Under a shared body the same secant is
 * `series(Σ tᵢ, ground)`: the compliance the mandate demands lives in the body's **ground**, which
 * is a rigid-body mode of the tile, and rigid-body modes are exactly what dishing projects out.
 * The ties are then capped by the per-path **force** allowable instead of by the mandate — a
 * different number, and a much larger one.
 *
 * `Σᵢ series(tᵢ, gᵢ)` against `series(Σᵢ tᵢ, g)`. The same total; a different place to put the
 * compliance.
 *
 * ## The rigid limit, which is a rank statement
 *
 * Condensing the body out gives the coupling stiffness the tile sees,
 * [sharedBodyCouplingMatrix]:
 *
 * ```
 * K_c = T − T Φ (Φᵀ T Φ + H)⁻¹ Φᵀ T
 * ```
 *
 * For a **free** rigid body (`Φ = [1, x, y]`, `H = 0`) this is `T − TA(AᵀTA)⁻¹AᵀT`, of rank
 * `max(n − 3, 0)`: it annihilates every affine motion of the tile, so it can carry **no net force
 * and no net moment** and is **exactly zero at `n = 1, 2, 3`**. That is `CLAUDE.md`'s
 * *"a body attached at ONE point and otherwise free adds EXACTLY ZERO stiffness there"*,
 * *"two ties on a straight host still add no bending, because two points determine a line"* and
 * *"the lever's own `EI` is engaged only at three ties"* — three recorded facts, one formula, and
 * a closed form to check the solver against before any Monte Carlo.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, forces **pN**, stiffness **pN/nm**, plate rigidity **pN·nm**.
 * `x` runs **along** the helices, `y` **across** them; the origin is the footprint centre; `w` is
 * positive **downward**. A tie force is positive **upward on the tile**, therefore downward on
 * the body. The body's shape functions are **dimensionless**, so its modal stiffnesses are in
 * `pN/nm` and its modal compliances in `nm/pN`, exactly as a station compliance is.
 */

// ------------------------------------------------------------------ the body's shape functions

/** The relative ridge every small dense solve here carries, for the reason `reachableForces` does. */
private const val SHARED_BODY_RIDGE: Double = 1e-12

/**
 * A Ritz mode set for a shared body of footprint [lengthX] × [lengthY] nm — the Legendre products
 * `P_a(2x/L_x) P_b(2y/L_y)` with `a + b ≤ maxTotalDegree`, ordered by total degree.
 *
 * `maxTotalDegree = 1` is **exactly the three rigid-body modes** `1`, `ξ`, `η`, in that order, so
 * the rigid body is not a separate model but the first member of the family. The basis is the one
 * `PlateOnFoundation` uses, so a body and a tile are described in the same coordinates.
 *
 * Rayleigh-Ritz truncation **overestimates** a body's stiffness, i.e. it runs in the shared
 * body's favour, which is why [SharedBodyModes] carries the degree and why the degree is a
 * declared convergence axis rather than a constant.
 */
class SharedBodyModes internal constructor(

    /** The footprint the reduced coordinates are taken over, in nm. */
    val lengthX: Double,

    /** The footprint the reduced coordinates are taken over, in nm. */
    val lengthY: Double,

    /** The highest total degree retained. */
    val maxTotalDegree: Int,

    /** The `x` degree of each mode. */
    val degreeX: IntArray,

    /** The `y` degree of each mode. */
    val degreeY: IntArray
) {

    /** The number of modes. */
    val modeCount: Int get() = degreeX.size

    /** The Legendre moments `∫P_a P_c`, `∫P'_a P'_c` and `∫P''_a P''_c` on `[−1, 1]`. */
    private val moments: Array<Array<DoubleArray>> by lazy {
        val n = maxTotalDegree + 1
        val rule = gaussLegendreRule(maxTotalDegree + 3)
        val p0 = Array(n) { DoubleArray(n) }
        val p1 = Array(n) { DoubleArray(n) }
        val p2 = Array(n) { DoubleArray(n) }
        for (q in 0 until rule.points) {
            val jet = legendreJet(maxTotalDegree, rule.nodes[q])
            val weight = rule.weights[q]
            for (i in 0 until n) for (j in 0 until n) {
                p0[i][j] += weight * jet.value[i] * jet.value[j]
                p1[i][j] += weight * jet.firstDerivative[i] * jet.firstDerivative[j]
                p2[i][j] += weight * jet.secondDerivative[i] * jet.secondDerivative[j]
            }
        }
        arrayOf(p0, p1, p2)
    }

    /** The mode shapes at [stations], as an `n × modeCount` matrix — dimensionless. */
    fun shapesAt(stations: List<Pair<Double, Double>>): Array<DoubleArray> {
        require(stations.isNotEmpty()) { "stations must not be empty" }
        return Array(stations.size) { index ->
            val (x, y) = stations[index]
            val jetX = legendreJet(maxTotalDegree, 2.0 * x / lengthX)
            val jetY = legendreJet(maxTotalDegree, 2.0 * y / lengthY)
            DoubleArray(modeCount) { mode ->
                jetX.value[degreeX[mode]] * jetY.value[degreeY[mode]]
            }
        }
    }

    /**
     * The body's own **bending** stiffness in these coordinates, in `pN/nm` — the Huber
     * orthotropic energy `½∫[D_x w_xx² + 2D_1 w_xx w_yy + 4D_k w_xy² + D_y w_yy²]`, assembled
     * exactly as `PlateOnFoundation` assembles it.
     *
     * The three rigid modes carry **identically zero** bending energy, which gate 4 asserts.
     */
    fun bendingStiffness(plate: OrthotropicPlate): Array<DoubleArray> {
        val (p0, p1, p2) = moments
        val jacobian = lengthX * lengthY / 4.0
        val sx = 2.0 / lengthX
        val sy = 2.0 / lengthY
        return Array(modeCount) { m ->
            DoubleArray(modeCount) { n ->
                val ax = degreeX[m]
                val ay = degreeY[m]
                val bx = degreeX[n]
                val by = degreeY[n]
                jacobian * (
                        plate.rigidityX * sx.pow(4) * p2[ax][bx] * p0[ay][by] +
                                plate.rigidityY * sy.pow(4) * p0[ax][bx] * p2[ay][by] +
                                4.0 * plate.twistingRigidity * sx * sx * sy * sy *
                                p1[ax][bx] * p1[ay][by]
                        )
            }
        }
    }

    /**
     * The ground of a body held by a **distributed** compliant element of total stiffness
     * [totalStiffness] pN/nm over its own footprint — a Winkler ground.
     *
     * Legendre orthogonality makes it diagonal and closed form,
     * `G_mm = K/((2a+1)(2b+1))`: the heave mode sees the whole mandate and each tilt mode a
     * third of it. This is the natural reading of `C-0017`, whose 33.3333 pN/nm is a **sum**.
     */
    fun distributedGroundStiffness(totalStiffness: Double): Array<DoubleArray> {
        require(totalStiffness > 0.0) {
            "totalStiffness must be positive, was: $totalStiffness"
        }
        return Array(modeCount) { m ->
            DoubleArray(modeCount) { n ->
                if (m != n) 0.0
                else totalStiffness / ((2 * degreeX[m] + 1) * (2 * degreeY[m] + 1))
            }
        }
    }

    /**
     * The ground of a body held at [points], each by an element of [perPoint] pN/nm —
     * `G = Σ_p κ φ(x_p) φ(x_p)ᵀ`, which is **singular in the tilts at one point** and is the
     * conservative end of the ground axis.
     */
    fun pointGroundStiffness(
        points: List<Pair<Double, Double>>,
        perPoint: Double
    ): Array<DoubleArray> {
        require(points.isNotEmpty()) { "points must not be empty" }
        require(perPoint > 0.0) { "perPoint must be positive, was: $perPoint" }
        val shapes = shapesAt(points)
        val matrix = Array(modeCount) { DoubleArray(modeCount) }
        shapes.forEach { shape ->
            for (m in 0 until modeCount) for (n in 0 until modeCount) {
                matrix[m][n] += perPoint * shape[m] * shape[n]
            }
        }
        return matrix
    }

}

/** [SharedBodyModes] of every Legendre product with `a + b ≤ [maxTotalDegree]`. */
fun sharedBodyModes(
    lengthX: Double,
    lengthY: Double,
    maxTotalDegree: Int
): SharedBodyModes {
    require(lengthX > 0.0) { "lengthX must be positive, was: $lengthX" }
    require(lengthY > 0.0) { "lengthY must be positive, was: $lengthY" }
    require(maxTotalDegree >= 1) {
        "maxTotalDegree must be at least 1 — the three rigid modes — was: $maxTotalDegree"
    }
    val x = ArrayList<Int>()
    val y = ArrayList<Int>()
    for (degree in 0..maxTotalDegree) for (a in degree downTo 0) {
        x += a
        y += degree - a
    }
    return SharedBodyModes(lengthX, lengthY, maxTotalDegree, x.toIntArray(), y.toIntArray())
}

// ------------------------------------------------------------------ the body, condensed

/**
 * A shared body reduced to what the tile can feel of it: its mode [shapes] at the stations and
 * its own [modalStiffness] there, bending **and** ground.
 *
 * [modalStiffness] may be **singular** — a body grounded at one point has free tilts, and a
 * completely free body has `H = 0` — which is why [InfluenceSurrogate.solveWithSharedBody]
 * eliminates the body rather than inverting it.
 */
class SharedBody internal constructor(

    /** `n × m`, dimensionless. */
    val shapes: Array<DoubleArray>,

    /** `m × m`, in `pN/nm`. */
    val modalStiffness: Array<DoubleArray>
) {

    /** The number of stations the body is tied at. */
    val stationCount: Int get() = shapes.size

    /** The number of modes retained. */
    val modeCount: Int get() = modalStiffness.size

    init {
        require(shapes.isNotEmpty()) { "a shared body must be tied at at least one station" }
        require(shapes.all { it.size == modeCount }) {
            "every station must carry one shape per mode"
        }
        require(modalStiffness.all { it.size == modeCount }) {
            "the modal stiffness must be square"
        }
    }

}

/** A [SharedBody] from [shapes] and a modal stiffness assembled as `bending + ground`. */
fun sharedBody(
    shapes: Array<DoubleArray>,
    bendingStiffness: Array<DoubleArray>,
    groundStiffness: Array<DoubleArray>
): SharedBody {
    require(bendingStiffness.size == groundStiffness.size) {
        "the bending and the ground must be in the same modal coordinates: " +
                "${bendingStiffness.size} against ${groundStiffness.size}"
    }
    val size = bendingStiffness.size
    return SharedBody(
        shapes,
        Array(size) { m -> DoubleArray(size) { n -> bendingStiffness[m][n] + groundStiffness[m][n] } }
    )
}

// ------------------------------------------------------------------ the condensation

/** Solves `A x = b` for a symmetric positive definite [a] with a relative ridge. */
private fun solveWithRidge(
    a: Array<DoubleArray>,
    b: Array<DoubleArray>
): Array<DoubleArray> {
    val size = a.size
    val matrix = F64Array(size, size)
    var trace = 0.0
    for (j in 0 until size) {
        for (k in 0 until size) matrix[j, k] = 0.5 * (a[j][k] + a[k][j])
        trace += abs(a[j][j])
    }
    val ridge = if (trace > 0.0) SHARED_BODY_RIDGE * trace / size else SHARED_BODY_RIDGE
    for (j in 0 until size) matrix[j, j] += ridge
    val decomposition = CholeskyDecomposition(matrix)
    val columns = b[0].size
    val solution = Array(size) { DoubleArray(columns) }
    for (c in 0 until columns) {
        val right = F64Array(size) { b[it][c] }
        val x = decomposition.solve(right)
        for (r in 0 until size) solution[r][c] = x[r]
    }
    return solution
}

/**
 * `F_b = Φ H⁻¹ Φᵀ` — the shared body's own compliance at its tie stations, in `nm/pN`.
 *
 * Defined only where `H` is non-singular, i.e. where the body is grounded in every mode. It is
 * the **series** reading of the same physics [sharedBodyCouplingMatrix] condenses, and the two
 * are cross-checked against each other rather than one being derived from the other.
 */
fun sharedBodyCompliance(
    shapes: Array<DoubleArray>,
    modalStiffness: Array<DoubleArray>
): Array<DoubleArray> {
    require(shapes.isNotEmpty()) { "shapes must not be empty" }
    val modes = modalStiffness.size
    require(shapes.all { it.size == modes }) {
        "every station must carry one shape per mode: ${shapes[0].size} against $modes"
    }
    val transposed = Array(modes) { m -> DoubleArray(shapes.size) { i -> shapes[i][m] } }
    val solved = solveWithRidge(modalStiffness, transposed)
    return Array(shapes.size) { i ->
        DoubleArray(shapes.size) { j ->
            var total = 0.0
            for (m in 0 until modes) total += shapes[i][m] * solved[m][j]
            total
        }
    }
}

/**
 * **The condensation.** `K_c = T − T Φ (Φᵀ T Φ + H)⁻¹ Φᵀ T` over the stations where [present] is
 * `true`, and exactly zero at the absent ones — the coupling stiffness the tile sees, in `pN/nm`.
 *
 * Symmetric and positive **semi**-definite: a free rigid body makes it rank `n − 3`, and at
 * `n ≤ 3` it is identically zero.
 */
fun sharedBodyCouplingMatrix(
    tieStiffnesses: List<Double>,
    body: SharedBody,
    present: List<Boolean> = List(tieStiffnesses.size) { true }
): Array<DoubleArray> {
    val count = tieStiffnesses.size
    require(body.stationCount == count) {
        "the body is tied at ${body.stationCount} stations and ${count} tie stiffnesses were given"
    }
    require(present.size == count) {
        "expected one presence flag per tie, was: ${present.size} for $count"
    }
    val live = (0 until count).filter { present[it] }
    require(live.all { tieStiffnesses[it] > 0.0 && tieStiffnesses[it].isFinite() }) {
        "every surviving tie stiffness must be positive and finite"
    }
    val matrix = Array(count) { DoubleArray(count) }
    if (live.isEmpty()) return matrix
    val modes = body.modeCount
    // `Φᵀ T Φ + H`, and `Φᵀ T` as the right-hand sides.
    val hessian = Array(modes) { m ->
        DoubleArray(modes) { n ->
            var total = body.modalStiffness[m][n]
            live.forEach { i -> total += body.shapes[i][m] * tieStiffnesses[i] * body.shapes[i][n] }
            total
        }
    }
    val right = Array(modes) { m ->
        DoubleArray(live.size) { j -> body.shapes[live[j]][m] * tieStiffnesses[live[j]] }
    }
    val solved = solveWithRidge(hessian, right)
    for (a in live.indices) for (b in live.indices) {
        var correction = 0.0
        for (m in 0 until modes) {
            correction += tieStiffnesses[live[a]] * body.shapes[live[a]][m] * solved[m][b]
        }
        matrix[live[a]][live[b]] =
            (if (a == b) tieStiffnesses[live[a]] else 0.0) - correction
    }
    // Symmetry is exact in the algebra and only nearly so in floating point.
    for (a in live.indices) for (b in a until live.indices.last + 1) {
        val i = live[a]
        val j = live[b]
        val mean = 0.5 * (matrix[i][j] + matrix[j][i])
        matrix[i][j] = mean
        matrix[j][i] = mean
    }
    return matrix
}

/** `K_c = (T⁻¹ + F_b)⁻¹` — the same condensation reached through the **series** compliance. */
fun couplingStiffnessMatrix(
    tieStiffnesses: List<Double>,
    seriesCompliance: Array<DoubleArray>,
    present: List<Boolean> = List(tieStiffnesses.size) { true }
): Array<DoubleArray> {
    val count = tieStiffnesses.size
    require(seriesCompliance.size == count && seriesCompliance.all { it.size == count }) {
        "the series compliance must be $count × $count"
    }
    require(present.size == count) {
        "expected one presence flag per tie, was: ${present.size} for $count"
    }
    val live = (0 until count).filter { present[it] }
    require(live.all { tieStiffnesses[it] > 0.0 && tieStiffnesses[it].isFinite() }) {
        "every surviving tie stiffness must be positive and finite"
    }
    val matrix = Array(count) { DoubleArray(count) }
    if (live.isEmpty()) return matrix
    val size = live.size
    val assembled = Array(size) { j ->
        DoubleArray(size) { k ->
            seriesCompliance[live[j]][live[k]] +
                    (if (j == k) 1.0 / tieStiffnesses[live[j]] else 0.0)
        }
    }
    val identity = Array(size) { j -> DoubleArray(size) { k -> if (j == k) 1.0 else 0.0 } }
    val inverse = solveWithRidge(assembled, identity)
    for (j in 0 until size) for (k in 0 until size) {
        matrix[live[j]][live[k]] = 0.5 * (inverse[j][k] + inverse[k][j])
    }
    return matrix
}

/** The coupling's **heave secant** in `pN/nm` — `1ᵀ K_c 1`, which is what `C-0017` fixes. */
fun couplingHeaveSecant(couplingStiffness: Array<DoubleArray>): Double {
    var total = 0.0
    couplingStiffness.forEach { row -> row.forEach { total += it } }
    return total
}

// ------------------------------------------------------------------ placing the mandate

/** What it took to place `C-0017`'s equality on the **body's ground** rather than on the ties. */
data class SharedBodyGroundPlacement(

    /** The multiple of the supplied unit ground that the mandate demands. */
    val groundScale: Double,

    /** The coupling's heave secant at that scale, in `pN/nm` — the placed mandate. */
    val heaveSecant: Double,

    /** The heave secant the ties alone could deliver, `Σ tᵢ`, in `pN/nm`. */
    val tieSecantCeiling: Double,

    /** The fraction of the coupling's own compliance that lives in the body's ground. */
    val groundComplianceShare: Double
)

/**
 * Scales the body's [unitGround] until the whole coupling's heave secant is exactly
 * [targetSecant] pN/nm — `C-0017`'s mandate **placed on the body's ground**, which is the whole
 * point of the topology.
 *
 * The secant is monotone in the scale and runs from **0** at a free body (which carries no net
 * force at all: the rank statement) to `Σ tᵢ` at a rigidly grounded one, so the root exists and is
 * unique whenever `Σ tᵢ > targetSecant`. Bisected on the logarithm of the scale.
 */
fun placeSharedBodyGround(
    tieStiffnesses: List<Double>,
    shapes: Array<DoubleArray>,
    bendingStiffness: Array<DoubleArray>,
    unitGround: Array<DoubleArray>,
    targetSecant: Double,
    tolerance: Double = 1e-12
): SharedBodyGroundPlacement {
    require(targetSecant > 0.0) { "targetSecant must be positive, was: $targetSecant" }
    val ceiling = tieStiffnesses.sum()
    require(ceiling > targetSecant) {
        "the ties alone deliver $ceiling pN/nm, which is at or below the mandate $targetSecant: " +
                "no ground can place it"
    }
    fun secantAt(scale: Double): Double {
        val size = unitGround.size
        val ground = Array(size) { m -> DoubleArray(size) { n -> scale * unitGround[m][n] } }
        return couplingHeaveSecant(
            sharedBodyCouplingMatrix(
                tieStiffnesses, sharedBody(shapes, bendingStiffness, ground)
            )
        )
    }

    val logScale = bracketedRoot(ln(1e-9), ln(1e12), tolerance) { secantAt(kotlin.math.exp(it)) - targetSecant }
    val scale = kotlin.math.exp(logScale)
    return SharedBodyGroundPlacement(
        groundScale = scale,
        heaveSecant = secantAt(scale),
        tieSecantCeiling = ceiling,
        groundComplianceShare = (1.0 / targetSecant - 1.0 / ceiling) / (1.0 / targetSecant)
    )
}

// ------------------------------------------------------------------ the mandate arithmetic

/** The per-station support stiffness of each topology, in `pN/nm` — the cheap bound, no solve. */
data class MandatePlacementArithmetic(

    /** The path count. */
    val pathCount: Int,

    /** `C-0017`'s mandate in `pN/nm`. */
    val mandate: Double,

    /** The array's per-station support stiffness, `K/n`. */
    val arrayPerStation: Double,

    /** The tie stiffness a shared body may carry, capped by the per-path force allowable. */
    val sharedBodyPerStation: Double,

    /** How much more locally stiff the shared body's station is. */
    val ratio: Double
)

/**
 * The whole escape, in one division and before any solve.
 *
 * Under an array `C-0017`'s [mandate] is the **sum** of the path stiffnesses, so each station is
 * supported at `K/n`. Under a shared body the mandate is supplied by the body's ground, and what
 * caps a tie is the per-path **force** allowable at the stroke — `C-0049`'s `a/s`, which is a
 * bound on a force read as a bound on a stiffness.
 */
fun mandatePlacementArithmetic(
    pathCount: Int,
    mandate: Double,
    allowable: Double,
    stroke: Double
): MandatePlacementArithmetic {
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    require(mandate > 0.0) { "mandate must be positive, was: $mandate" }
    val perStation = mandate / pathCount
    val tie = perPathStiffnessCeiling(allowable, stroke)
    return MandatePlacementArithmetic(
        pathCount = pathCount,
        mandate = mandate,
        arrayPerStation = perStation,
        sharedBodyPerStation = tie,
        ratio = tie / perStation
    )
}

/** The largest departure between two square matrices, relative to the larger's own scale. */
fun matrixDeparture(a: Array<DoubleArray>, b: Array<DoubleArray>): Double {
    require(a.size == b.size) { "the two matrices must be the same size" }
    var scale = 0.0
    var worst = 0.0
    a.indices.forEach { i ->
        require(a[i].size == b[i].size) { "the two matrices must be the same shape" }
        a[i].indices.forEach { j ->
            scale = max(scale, max(abs(a[i][j]), abs(b[i][j])))
            worst = max(worst, abs(a[i][j] - b[i][j]))
        }
    }
    return if (scale > 0.0) worst / scale else 0.0
}
