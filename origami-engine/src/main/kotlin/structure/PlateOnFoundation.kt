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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A rectangular orthotropic Kirchhoff plate, in the Huber form.
 *
 * The bending energy density is
 * `½[D_x w_xx² + 2 D_1 w_xx w_yy + D_y w_yy² + 4 D_k w_xy²]`,
 * with `x` along the helices of the origami sheet and `w` measured downward.
 *
 * For an isotropic plate of rigidity `D` and Poisson ratio `ν` this reduces to
 * `D_x = D_y = D`, `D_1 = νD`, `D_k = D(1−ν)/2` — see [isotropicPlate], which exists
 * so the solver can be checked against the classical isotropic results.
 *
 * @param lengthX the footprint along the helices, in nm.
 * @param lengthY the footprint across the helices, in nm.
 * @param rigidityX `D_x` in `pN·nm`.
 * @param rigidityY `D_y` in `pN·nm`.
 * @param twistingRigidity `D_k` in `pN·nm`.
 * @param couplingRigidity `D_1` in `pN·nm`; zero for a grillage of independent duplexes,
 *          which have no Poisson coupling between them.
 */
data class OrthotropicPlate(
    val lengthX: Double,
    val lengthY: Double,
    val rigidityX: Double,
    val rigidityY: Double,
    val twistingRigidity: Double,
    val couplingRigidity: Double = 0.0
) {

    init {
        require(lengthX > 0.0) { "lengthX must be positive, was: $lengthX" }
        require(lengthY > 0.0) { "lengthY must be positive, was: $lengthY" }
        require(rigidityX > 0.0) { "rigidityX must be positive, was: $rigidityX" }
        require(rigidityY > 0.0) { "rigidityY must be positive, was: $rigidityY" }
        require(twistingRigidity >= 0.0) {
            "twistingRigidity cannot be negative, was: $twistingRigidity"
        }
    }

    /** The footprint area in nm². */
    val area: Double get() = lengthX * lengthY

    /**
     * The geometric-mean rigidity `√(D_x D_y)` in `pN·nm`.
     *
     * The standard reduction of an orthotropic plate to an equivalent isotropic one for
     * point-load and Winkler-length purposes, and the only defensible single number to
     * quote when a formula wants "the" flexural rigidity of an anisotropic sheet.
     */
    val effectiveRigidity: Double get() = sqrt(rigidityX * rigidityY)

}

/** Returns an isotropic plate of flexural rigidity [rigidity] and Poisson ratio [poissonRatio]. */
fun isotropicPlate(
    lengthX: Double,
    lengthY: Double,
    rigidity: Double,
    poissonRatio: Double = 0.3
): OrthotropicPlate = OrthotropicPlate(
    lengthX = lengthX,
    lengthY = lengthY,
    rigidityX = rigidity,
    rigidityY = rigidity,
    twistingRigidity = rigidity * (1.0 - poissonRatio) / 2.0,
    couplingRigidity = rigidity * poissonRatio
)

/**
 * The **Winkler characteristic length** `ℓ = (D/k_f)^(1/4)` in nm — the distance over which
 * a plate of flexural rigidity [rigidity] can bridge a foundation of stiffness
 * [foundationStiffness] per unit area.
 *
 * This is the governing dimensionless group of the whole problem once it is compared
 * against the tile half-width: `ℓ/L ≫ 1` is a rigid plate, `ℓ/L ≪ 1` is a membrane that
 * follows the foundation locally. Stating the answer in `ℓ/L` is what makes it survive a
 * change in the foundation stiffness, which is still in flux under `T-1c`.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun winklerLength(rigidity: Double, foundationStiffness: Double): Double {
    require(rigidity > 0.0) { "rigidity must be positive, was: $rigidity" }
    require(foundationStiffness > 0.0) {
        "foundationStiffness must be positive, was: $foundationStiffness"
    }
    return (rigidity / foundationStiffness).pow(0.25)
}

/**
 * The centre deflection in nm of an **infinite** isotropic plate on a Winkler foundation
 * under a point load [force], `w(0) = P/(8√(D k_f))`.
 *
 * The classical Hertz–Westergaard result, and the cheap bound this task runs before the
 * finite-plate solve: it needs no discretisation, and if it already says the deflection
 * exceeds the stroke then the concentrated-attachment case is dead without further work.
 * A finite plate is softer than this, never stiffer, so it is a lower bound on the dishing.
 *
 * The reciprocal `8√(D k_f)` is the **point stiffness** of the supported plate, and it is
 * finite — which is why a rigid anchor on a compliant tile can only ever collect the load
 * from an area of order `ℓ²` around itself, however large the tile is.
 */
fun pointLoadDeflection(
    force: Double,
    rigidity: Double,
    foundationStiffness: Double
): Double {
    require(rigidity > 0.0) { "rigidity must be positive, was: $rigidity" }
    require(foundationStiffness > 0.0) {
        "foundationStiffness must be positive, was: $foundationStiffness"
    }
    return force / (8.0 * sqrt(rigidity * foundationStiffness))
}

/**
 * The fraction of a sinusoidal load ripple of wavelength [wavelength] that survives into
 * the deflection of an infinite plate on a Winkler foundation, `1/(1 + (2πℓ/λ)⁴)`.
 *
 * Obtained by putting `w = ŵ sin(2πy/λ)` into `D w'''' + k_f w = q̂ sin(2πy/λ)`.
 * This is the transfer function that turns "the electrostatic load is not perfectly
 * uniform" into a bounded number, and it is why a load non-uniformity confined to within
 * a Debye length of the tile edge cannot dish the tile: the plate low-passes it.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun loadRippleTransmission(winklerLength: Double, wavelength: Double): Double {
    require(winklerLength > 0.0) { "winklerLength must be positive, was: $winklerLength" }
    require(wavelength > 0.0) { "wavelength must be positive, was: $wavelength" }
    return 1.0 / (1.0 + (2.0 * PI * winklerLength / wavelength).pow(4))
}

/** A distributed downward pressure in `pN/nm²` over the plate footprint. */
fun interface PressureField {

    /** The pressure at ([x], [y]), positive downward. */
    fun at(x: Double, y: Double): Double
}

/** Returns the uniform pressure field of magnitude [pressure] in `pN/nm²`. */
fun uniformPressure(pressure: Double): PressureField = PressureField { _, _ -> pressure }

/**
 * Returns the pressure field that is [pressure] in the interior and falls to
 * `pressure × (1 − depth)` at the footprint boundary, over a taper of width [edgeWidth] nm.
 *
 * The bounded perturbation `T-5b` uses to represent the electrostatic edge effect:
 * a finite charged tile at a gap comparable to the Debye length loses field lines off its
 * rim, so the downward pressure is lower there. The profile is a raised cosine rather than
 * a step, because a step would put energy at every wavelength and the point of the exercise
 * is to test which wavelengths the plate transmits.
 *
 * @throws IllegalArgumentException if [edgeWidth] is not positive or [depth] is outside `0..1`.
 */
fun edgeTaperedPressure(
    pressure: Double,
    plate: OrthotropicPlate,
    edgeWidth: Double,
    depth: Double
): PressureField {
    require(edgeWidth > 0.0) { "edgeWidth must be positive, was: $edgeWidth" }
    require(depth in 0.0..1.0) { "depth must be within 0..1, was: $depth" }
    return PressureField { x, y ->
        val margin = minOf(plate.lengthX / 2.0 - abs(x), plate.lengthY / 2.0 - abs(y))
        if (margin >= edgeWidth) pressure
        else pressure * (1.0 - depth * 0.5 * (1.0 + cos(PI * margin / edgeWidth)))
    }
}

/** A discrete anchor: a linear spring of [stiffness] in `pN/nm` tying the plate to ground at ([x], [y]). */
data class PointSupport(
    val x: Double,
    val y: Double,
    val stiffness: Double
) {

    init {
        require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    }

}

/** A concentrated load of [force] pN applied downward at ([x], [y]). */
data class PointLoad(
    val x: Double,
    val y: Double,
    val force: Double
)

/**
 * The deflected shape of a plate on an elastic foundation under one load case.
 *
 * Coordinates run over `[−lengthX/2, lengthX/2] × [−lengthY/2, lengthY/2]`,
 * and `w` is positive **downward**, compressing the polymer layer.
 */
class PlateDeflection internal constructor(
    internal val solver: PlateOnFoundation,
    val coefficients: F64Array,
    internal val pressure: PressureField,
    val pointLoads: List<PointLoad>
) {

    private val plate get() = solver.plate

    /** The deflection in nm at ([x], [y]). */
    fun deflection(x: Double, y: Double): Double =
        solver.basisAt(x, y).dot(coefficients)

    /**
     * The deflection in nm at ([x], [y]) of the **best-fit rigid plane** —
     * the piston and two tilt components, which a rigid tile would be restricted to.
     *
     * Best fit in the area-averaged least-squares sense, and exactly the first three
     * Legendre coefficients, because the basis is orthogonal in that inner product.
     */
    fun rigidPlaneDeflection(x: Double, y: Double): Double =
        coefficients[solver.index(0, 0)] +
                coefficients[solver.index(1, 0)] * (2.0 * x / plate.lengthX) +
                coefficients[solver.index(0, 1)] * (2.0 * y / plate.lengthY)

    /** [deflection] minus [rigidPlaneDeflection] — the dishing, in nm. */
    fun dishing(x: Double, y: Double): Double = deflection(x, y) - rigidPlaneDeflection(x, y)

    /**
     * The area-averaged deflection in nm.
     *
     * This is the number a rigid-plate model would report as *the* displacement,
     * and the number the polymer layer's total reaction is set by.
     */
    val meanDeflection: Double get() = coefficients[solver.index(0, 0)]

    /** The root-mean-square dishing over the footprint, in nm. */
    val dishingRms: Double
        get() = sqrt(
            (0 until solver.degreesOfFreedom).sumOf { i ->
                solver.dishingWeights[i] * coefficients[i] * coefficients[i]
            }
        )

    /** The largest absolute dishing over a [samples] × [samples] grid, in nm. */
    fun peakDishing(samples: Int = 81): Double = solver.overGrid(samples) { x, y ->
        abs(dishing(x, y))
    }

    /** The largest absolute deflection over a [samples] × [samples] grid, in nm. */
    fun peakDeflection(samples: Int = 81): Double = solver.overGrid(samples) { x, y ->
        abs(deflection(x, y))
    }

    /** The force in pN carried by each of the solver's supports, in the same order. */
    val supportForces: List<Double>
        get() = solver.supports.map { it.stiffness * deflection(it.x, it.y) }

    /** The total force in pN carried by the foundation. */
    val foundationForce: Double
        get() = solver.foundationStiffness * plate.area * meanDeflection

    /** The total applied force in pN — the pressure field over the footprint plus the point loads. */
    val appliedForce: Double
        get() = solver.integrate(
            -plate.lengthX / 2.0, plate.lengthX / 2.0,
            -plate.lengthY / 2.0, plate.lengthY / 2.0
        ) { x, y -> pressure.at(x, y) } + pointLoads.sumOf { it.force }

    /**
     * The transverse shear force in pN that must cross the cut `y = `[y] —
     * a line **parallel to the helices**, whose only connections are crossovers.
     *
     * Computed from the equilibrium of the part of the plate beyond the cut — applied load
     * minus foundation reaction minus support reactions — rather than from third
     * derivatives of the Ritz expansion, which converge far more slowly than the deflection.
     */
    fun shearAcrossCrossoverLine(y: Double): Double {
        val top = plate.lengthY / 2.0
        if (y >= top) return 0.0
        val load = solver.integrate(
            -plate.lengthX / 2.0, plate.lengthX / 2.0, y, top
        ) { px, py -> pressure.at(px, py) - solver.foundationStiffness * deflection(px, py) }
        val point = pointLoads.filter { it.y > y }.sumOf { it.force }
        val reactions = solver.supports.filter { it.y > y }
            .sumOf { it.stiffness * deflection(it.x, it.y) }
        return load + point - reactions
    }

    /**
     * The transverse shear force in pN that must cross the cut `x = `[x] —
     * a line **perpendicular to the helices**, whose connections are the duplexes themselves.
     */
    fun shearAcrossDuplexLine(x: Double): Double {
        val right = plate.lengthX / 2.0
        if (x >= right) return 0.0
        val load = solver.integrate(
            x, right, -plate.lengthY / 2.0, plate.lengthY / 2.0
        ) { px, py -> pressure.at(px, py) - solver.foundationStiffness * deflection(px, py) }
        val point = pointLoads.filter { it.x > x }.sumOf { it.force }
        val reactions = solver.supports.filter { it.x > x }
            .sumOf { it.stiffness * deflection(it.x, it.y) }
        return load + point - reactions
    }

    /** The largest [shearAcrossCrossoverLine] over [samples] cuts spanning the footprint. */
    fun peakCrossoverLineShear(samples: Int = 201): Double {
        val half = plate.lengthY / 2.0
        return (0 until samples).maxOf { i ->
            abs(shearAcrossCrossoverLine(-half + 2.0 * half * i / (samples - 1)))
        }
    }

    /** The largest [shearAcrossDuplexLine] over [samples] cuts spanning the footprint. */
    fun peakDuplexLineShear(samples: Int = 201): Double {
        val half = plate.lengthX / 2.0
        return (0 until samples).maxOf { i ->
            abs(shearAcrossDuplexLine(-half + 2.0 * half * i / (samples - 1)))
        }
    }

}

/**
 * The thermal fluctuation of a free plate on an elastic foundation at one temperature,
 * split into the rigid-body part a rigid-tile model can represent and the dishing part it cannot.
 *
 * All amplitudes are root-mean-square, in nm.
 *
 * @param pistonRms the rigid translation normal to the electrode.
 * @param tiltRms the area-averaged contribution of the two rigid tilts.
 * @param dishingRms the area-averaged contribution of everything that is not a rigid-body mode.
 * @param centreRms the total fluctuation of the deflection at the centre of the footprint.
 */
data class PlateThermalFluctuation(
    val pistonRms: Double,
    val tiltRms: Double,
    val dishingRms: Double,
    val centreRms: Double
)

/**
 * A Rayleigh-Ritz solver for a rectangular orthotropic plate on a Winkler foundation,
 * with free edges, discrete point supports and concentrated loads.
 *
 * ## Why Ritz rather than finite differences
 *
 * The plate has **free** edges on all four sides, which in a finite-difference biharmonic
 * solve means two rows of ghost nodes and a pair of natural boundary conditions
 * (`M_n = 0`, `V_n = 0`) that are awkward to discretise and easy to get subtly wrong.
 * In an energy method they are *natural*: they are satisfied by the stationarity of the
 * functional, and no boundary condition has to be imposed at all.
 * The basis is a tensor product of Legendre polynomials, which has three further
 * properties this task uses directly:
 *
 * - the rigid-body modes are **exactly** the first three basis functions
 *   (`P₀P₀`, `P₁P₀`, `P₀P₁`), so "dishing" is exactly "everything else" and needs no fitting;
 * - the basis is orthogonal in the area inner product, so the area-averaged deflection is
 *   one coefficient and the mean-square dishing is a weighted sum of squares of the others;
 * - the integrands are polynomials of known degree, so Gauss-Legendre quadrature is exact
 *   and the only approximation left is the truncation, which gate 4 then has to converge.
 *
 * @param basisDegree the highest Legendre degree in each direction; the basis has
 *          `(basisDegree + 1)²` members.
 */
class PlateOnFoundation(
    val plate: OrthotropicPlate,
    val foundationStiffness: Double,
    val supports: List<PointSupport> = emptyList(),
    val basisDegree: Int = 12
) {

    init {
        require(foundationStiffness > 0.0) {
            "foundationStiffness must be positive, was: $foundationStiffness"
        }
        require(basisDegree >= 1) { "basisDegree must be at least 1, was: $basisDegree" }
    }

    /** The number of Ritz degrees of freedom. */
    val degreesOfFreedom: Int get() = (basisDegree + 1) * (basisDegree + 1)

    internal fun index(degreeX: Int, degreeY: Int): Int = degreeX * (basisDegree + 1) + degreeY

    private val quadrature = gaussLegendreRule(basisDegree + 3)

    /**
     * The quadrature used for load integrals, which are **not** polynomial when the
     * pressure field is not — the edge-tapered field of [edgeTaperedPressure], for instance.
     * Resolved finely enough that the taper is integrated rather than sampled.
     */
    private val loadQuadrature = gaussLegendreRule(max(basisDegree + 3, 48))

    /** `∫P_m P_m'`, `∫P'_m P'_m'`, `∫P''_m P''_m'` and `∫P''_m P_m'` on `[−1, 1]`. */
    private val moments: Array<F64Array> = buildMoments()

    private fun buildMoments(): Array<F64Array> {
        val n = basisDegree + 1
        val p0 = F64Array(n, n)
        val p1 = F64Array(n, n)
        val p2 = F64Array(n, n)
        val mixed = F64Array(n, n)
        for (q in 0 until quadrature.points) {
            val jet = legendreJet(basisDegree, quadrature.nodes[q])
            val weight = quadrature.weights[q]
            for (i in 0 until n) {
                for (j in 0 until n) {
                    p0[i, j] += weight * jet.value[i] * jet.value[j]
                    p1[i, j] += weight * jet.firstDerivative[i] * jet.firstDerivative[j]
                    p2[i, j] += weight * jet.secondDerivative[i] * jet.secondDerivative[j]
                    mixed[i, j] += weight * jet.secondDerivative[i] * jet.value[j]
                }
            }
        }
        return arrayOf(p0, p1, p2, mixed)
    }

    /** The Ritz stiffness matrix, assembled once and reused by every load case. */
    private val stiffness: F64Array by lazy { assembleStiffness() }

    private val factorisation: CholeskyDecomposition by lazy { CholeskyDecomposition(stiffness) }

    private fun assembleStiffness(): F64Array {
        val n = basisDegree + 1
        val (p0, p1, p2, mixed) = moments
        val lx = plate.lengthX
        val ly = plate.lengthY
        val jacobian = lx * ly / 4.0
        val sx = 2.0 / lx
        val sy = 2.0 / ly
        val matrix = F64Array(degreesOfFreedom, degreesOfFreedom)
        for (mx in 0 until n) for (my in 0 until n) {
            val row = index(mx, my)
            for (nx in 0 until n) for (ny in 0 until n) {
                val column = index(nx, ny)
                var value = plate.rigidityX * sx.pow(4) * p2[mx, nx] * p0[my, ny] +
                        plate.rigidityY * sy.pow(4) * p0[mx, nx] * p2[my, ny] +
                        4.0 * plate.twistingRigidity * sx * sx * sy * sy *
                        p1[mx, nx] * p1[my, ny] +
                        foundationStiffness * p0[mx, nx] * p0[my, ny]
                if (plate.couplingRigidity != 0.0) {
                    value += plate.couplingRigidity * sx * sx * sy * sy *
                            (mixed[mx, nx] * mixed[ny, my] + mixed[nx, mx] * mixed[my, ny])
                }
                matrix[row, column] = value * jacobian
            }
        }
        supports.forEach { support ->
            val basis = basisAt(support.x, support.y)
            for (i in 0 until degreesOfFreedom) {
                for (j in 0 until degreesOfFreedom) {
                    matrix[i, j] += support.stiffness * basis[i] * basis[j]
                }
            }
        }
        return matrix
    }

    /** The basis functions evaluated at ([x], [y]). */
    internal fun basisAt(x: Double, y: Double): F64Array {
        val jetX = legendreJet(basisDegree, 2.0 * x / plate.lengthX)
        val jetY = legendreJet(basisDegree, 2.0 * y / plate.lengthY)
        return F64Array(degreesOfFreedom) { i ->
            jetX.value[i / (basisDegree + 1)] * jetY.value[i % (basisDegree + 1)]
        }
    }

    /**
     * The weight of each coefficient in the mean-square dishing:
     * `1/((2m+1)(2n+1))` for every mode that is not one of the three rigid-body ones,
     * and zero for those three.
     */
    internal val dishingWeights: F64Array by lazy { F64Array(degreesOfFreedom) { i ->
        val mx = i / (basisDegree + 1)
        val my = i % (basisDegree + 1)
        val rigid = (mx == 0 && my == 0) || (mx == 1 && my == 0) || (mx == 0 && my == 1)
        if (rigid) 0.0 else 1.0 / ((2 * mx + 1) * (2 * my + 1))
    } }

    /** Integrates [field] over the rectangle by tensor Gauss-Legendre quadrature. */
    internal fun integrate(
        fromX: Double,
        toX: Double,
        fromY: Double,
        toY: Double,
        field: (Double, Double) -> Double
    ): Double {
        if (toX <= fromX || toY <= fromY) return 0.0
        val halfX = (toX - fromX) / 2.0
        val halfY = (toY - fromY) / 2.0
        val centreX = (toX + fromX) / 2.0
        val centreY = (toY + fromY) / 2.0
        var total = 0.0
        for (i in 0 until loadQuadrature.points) {
            val x = centreX + halfX * loadQuadrature.nodes[i]
            for (j in 0 until loadQuadrature.points) {
                val y = centreY + halfY * loadQuadrature.nodes[j]
                total += loadQuadrature.weights[i] * loadQuadrature.weights[j] * field(x, y)
            }
        }
        return total * halfX * halfY
    }

    internal fun overGrid(samples: Int, field: (Double, Double) -> Double): Double {
        require(samples >= 2) { "samples must be at least 2, was: $samples" }
        val halfX = plate.lengthX / 2.0
        val halfY = plate.lengthY / 2.0
        var peak = 0.0
        for (i in 0 until samples) {
            val x = -halfX + 2.0 * halfX * i / (samples - 1)
            for (j in 0 until samples) {
                val y = -halfY + 2.0 * halfY * j / (samples - 1)
                peak = max(peak, field(x, y))
            }
        }
        return peak
    }

    /** Solves one load case: a [pressure] field downward, plus any [pointLoads]. */
    fun solve(
        pressure: PressureField = uniformPressure(0.0),
        pointLoads: List<PointLoad> = emptyList()
    ): PlateDeflection {
        val load = F64Array(degreesOfFreedom)
        val halfX = plate.lengthX / 2.0
        val halfY = plate.lengthY / 2.0
        for (i in 0 until loadQuadrature.points) {
            val x = halfX * loadQuadrature.nodes[i]
            val weightX = loadQuadrature.weights[i] * halfX
            for (j in 0 until loadQuadrature.points) {
                val y = halfY * loadQuadrature.nodes[j]
                val weight = weightX * loadQuadrature.weights[j] * halfY * pressure.at(x, y)
                if (weight == 0.0) continue
                val basis = basisAt(x, y)
                for (k in 0 until degreesOfFreedom) load[k] += weight * basis[k]
            }
        }
        pointLoads.forEach { point ->
            val basis = basisAt(point.x, point.y)
            for (k in 0 until degreesOfFreedom) load[k] += point.force * basis[k]
        }
        return PlateDeflection(this, factorisation.solve(load), pressure, pointLoads)
    }

    /**
     * The equilibrium thermal fluctuation of the unloaded plate at [temperature],
     * from equipartition on the Ritz functional: the coefficient covariance is
     * `k_BT K⁻¹`, so every mode gets `k_BT` of energy and the amplitudes follow.
     */
    fun thermalFluctuation(
        temperature: Double = ROOM_TEMPERATURE
    ): PlateThermalFluctuation {
        val energy = thermalEnergy(temperature)
        val inverse = factorisation.inverseDiagonal
        val dishing = (0 until degreesOfFreedom).sumOf { dishingWeights[it] * energy * inverse[it] }
        val tilt = energy * (inverse[index(1, 0)] + inverse[index(0, 1)]) / 3.0
        val centre = basisAt(0.0, 0.0)
        return PlateThermalFluctuation(
            pistonRms = sqrt(energy * inverse[index(0, 0)]),
            tiltRms = sqrt(tilt),
            dishingRms = sqrt(dishing),
            centreRms = sqrt(energy * centre.dot(factorisation.solve(centre)))
        )
    }

}
