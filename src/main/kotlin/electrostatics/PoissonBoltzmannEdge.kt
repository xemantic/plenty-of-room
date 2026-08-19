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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * A dimensionless shape multiplying a smeared surface charge along its own wall — `P-14`.
 *
 * The coordinate is **distance inward from the rim** for a face and **height above the tile's
 * bottom face** for the rim, so a shape is written once and reads the same on either. A `null`
 * shape is the constant 1 and leaves the solve bit-identical.
 */
fun interface EdgeChargeShape {
    /** The multiplier at [coordinate] nm. */
    fun factor(coordinate: Double): Double
}

/**
 * The **2-D** nonlinear 2:1 Poisson-Boltzmann solve of the Gen-1 tile **edge** — task `T-3b`,
 * leaf `A7.4`. It supplies the one thing `C-0008` said a 1-D treatment cannot: the lateral
 * profile of the electrostatic load, which `C-0006` makes the dishing exactly linear in.
 *
 * ## Geometry and sign conventions, fixed before deriving
 *
 * - `z` is normal to the electrode, **positive away from it**, origin at the electrode surface.
 * - `x` is lateral. `x = 0` is the tile **centre-line**, a symmetry plane; the rim is at
 *   `x = a` ([tileHalfWidth]); the domain runs out to `x = a + `[outerWidth].
 * - The **electrode** is the whole plane `z = 0`, held at a fixed potential (Dirichlet). It is a
 *   macroscopic electrode, not a counter-pad the size of the tile.
 * - The **tile** is an impermeable obstacle occupying `0 ≤ x ≤ a`, `h ≤ z ≤ h + t`, with fixed
 *   charge densities on its bottom face, its top face and its rim (Neumann, no regulation).
 * - The traction is reported as a **downward load**, positive when it pushes the tile toward the
 *   electrode — the sign `C-0006`'s plate consumes. It is minus the disjoining pressure `T-3a`
 *   reports, and it equals it deep under the tile, which is asserted as a test.
 *
 * ## The traction, from the stress tensor rather than from the first integral
 *
 * In one dimension the disjoining pressure is the first integral, constant across the gap. In
 * two dimensions it is **not** constant: vertical momentum balance reads
 * `∂_z T_zz + ∂_x T_zx = 0`, so `T_zz` is `z`-independent only where the lateral shear stress
 * has no gradient — which is exactly the interior, and exactly not the rim. The traction is
 * therefore taken from the **wall** value of the total stress tensor,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`f_z = Π_osm(y_w) − ε[(∂_z y)² − (∂_x y)²]/(8π l_B ε_ref)`,
 *
 * on the bottom face, with `∂_z y` **exact from the Neumann condition** and `∂_x y` differenced
 * along the wall, and minus the same expression on the top face. With `∂_x y = 0` this is
 * `T-3a`'s contact-value form term for term — the 2-D expression *contains* the 1-D one rather
 * than resembling it, and that is asserted rather than remarked.
 *
 * The rim contributes `f_z = ε(∂_z y)(∂_x y)/(4π l_B ε_ref)` as a **line** force, and because
 * `∂_x y` at the rim is fixed by its own Neumann condition, **an uncharged rim exerts exactly no
 * vertical force** — which is why the rim charge, which no source in this project supplies, is
 * not a load-bearing unknown.
 *
 * ## Why the discretisation is what it is
 *
 * A tensor-product mesh with `tanh` grading at every wall, with mesh lines placed **exactly** on
 * `z = h`, `z = h + t` and `x = a`, so the obstacle is resolved with no staircase error at all.
 * Each node's dual cell is assembled from its four quadrants, each of which is either entirely
 * fluid or entirely solid — which is what makes the re-entrant corner at the rim conservative
 * rather than approximate: the corner node's cell is L-shaped, and the assembly says so.
 *
 * The resulting Jacobian is symmetric (the face conductance `εA/d` is the same seen from either
 * side) and, because `dρ/dy < 0` everywhere, an M-matrix — so its negation is **SPD** and
 * conjugate gradients is the correct method rather than a lucky one. The preconditioner solves
 * each `x`-column's tridiagonal exactly in a symmetric forward-backward sweep, which removes the
 * five orders of magnitude of `z`-grading from the condition number.
 */
class PoissonBoltzmannEdge(
    /** The tile-electrode separation in nm. */
    val gapHeight: Double,
    /** The 2:1 buffer statistics — `T-3a`'s, reused unchanged. */
    val ionModel: IonModel,
    /** The medium filling the whole domain; uniform here, see the validity range of `C-0022`. */
    val medium: GapMedium = GapMedium(),
    /** `l_B` in nm at [referencePermittivity]. */
    val bjerrumLength: Double,
    /** Half the tile's lateral extent in nm; `x = 0` is its centre-line. */
    val tileHalfWidth: Double = 20.0,
    /** The tile's thickness in nm — §3's 10 nm. */
    val tileThickness: Double = 10.0,
    /** How far beyond the rim the domain runs, in nm. */
    val outerWidth: Double = 40.0,
    /** How far above the tile the domain runs, in nm. */
    val headroom: Double = 24.0,
    /** The mesh multiplier; every block's node count is this times its base. */
    val refinement: Int = 1,
    /** The wall-clustering parameter `β` of the graded mesh, normal to the walls. */
    val grading: Double = DEFAULT_EDGE_MESH_GRADING,
    /**
     * The rim-clustering parameter `β` of the **lateral** mesh, which is deliberately milder.
     *
     * The `z` grading has to resolve a 0.09 nm Gouy-Chapman layer inside a 10 nm gap and it costs
     * nothing in conditioning, because the preconditioner solves each `z`-line exactly. The `x`
     * grading is not preconditioned away, so its spacing ratio lands directly in the condition
     * number — at `β = 6` it is 3.6e4 and the conjugate gradients do not converge in any useful
     * number of iterations. The lateral feature to resolve is a taper of order a nanometre, not a
     * Gouy-Chapman layer, so `β = 2.5` (a ratio of 38) resolves what is there and costs nothing.
     */
    val lateralGrading: Double = DEFAULT_EDGE_LATERAL_GRADING,
    /** Whether the far lateral boundary is bulk (Dirichlet) or reflecting (Neumann). */
    val farFieldDirichlet: Boolean = true,
    /** The permittivity [bjerrumLength] was evaluated at. */
    val referencePermittivity: Double = WATER_RELATIVE_PERMITTIVITY,
    private val temperature: Double = ROOM_TEMPERATURE
) {

    init {
        require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
        require(tileHalfWidth > 0.0) { "tileHalfWidth must be positive, was: $tileHalfWidth" }
        require(tileThickness > 0.0) { "tileThickness must be positive, was: $tileThickness" }
        require(outerWidth > 0.0) { "outerWidth must be positive, was: $outerWidth" }
        require(headroom > 0.0) { "headroom must be positive, was: $headroom" }
        require(refinement >= 1) { "refinement must be at least 1, was: $refinement" }
        require(grading > 0.0) { "grading must be positive, was: $grading" }
        require(lateralGrading > 0.0) { "lateralGrading must be positive, was: $lateralGrading" }
        require(bjerrumLength > 0.0) { "bjerrumLength must be positive, was: $bjerrumLength" }
    }

    private val coupling = 4.0 * PI * bjerrumLength * referencePermittivity
    private val permittivity = medium.relativePermittivity

    /** The 2:1 inverse Debye length of the filling medium, in `nm⁻¹`. */
    val inverseDebyeLength: Double = sqrt(
        24.0 * PI * bjerrumLength * ionModel.bulkMagnesiumDensity *
                referencePermittivity / permittivity
    )

    /** The mesh lines normal to the electrode, from 0 to `h + t + headroom`. */
    val height: DoubleArray = buildHeightMesh()

    /** The mesh lines along the electrode, from the centre-line to `a + outerWidth`. */
    val lateral: DoubleArray = buildLateralMesh()

    private val verticalCount = height.size - 1
    private val lateralCount = lateral.size - 1
    private val stride = height.size

    /** The index of the mesh line at the tile's bottom face. */
    private val bottomIndex = GAP_NODES * refinement

    /** The index of the mesh line at the tile's top face. */
    private val topIndex = bottomIndex + TILE_NODES * refinement

    /** The index of the mesh line at the rim. */
    private val rimIndex = INNER_NODES * refinement

    private fun buildHeightMesh(): DoubleArray {
        val gap = twoSidedMesh(0.0, gapHeight, GAP_NODES * refinement, grading)
        val band = twoSidedMesh(
            gapHeight, gapHeight + tileThickness, TILE_NODES * refinement, grading
        )
        val above = oneSidedMesh(
            gapHeight + tileThickness, gapHeight + tileThickness + headroom,
            ABOVE_NODES * refinement, grading, clusteredAtStart = true
        )
        return (gap + band.drop(1) + above.drop(1)).toDoubleArray()
    }

    private fun buildLateralMesh(): DoubleArray {
        val inner = oneSidedMesh(
            0.0, tileHalfWidth, INNER_NODES * refinement, lateralGrading, clusteredAtStart = false
        )
        val outer = oneSidedMesh(
            tileHalfWidth, tileHalfWidth + outerWidth, OUTER_NODES * refinement, lateralGrading,
            clusteredAtStart = true
        )
        return (inner + outer.drop(1)).toDoubleArray()
    }

    private fun index(lateralIndex: Int, verticalIndex: Int): Int =
        lateralIndex * stride + verticalIndex

    /** True where the point lies strictly inside the tile. */
    private fun solid(x: Double, z: Double): Boolean =
        x < tileHalfWidth && z > gapHeight && z < gapHeight + tileThickness

    private fun halfBefore(mesh: DoubleArray, i: Int): Double =
        if (i == 0) 0.0 else 0.5 * (mesh[i] - mesh[i - 1])

    private fun halfAfter(mesh: DoubleArray, i: Int): Double =
        if (i == mesh.size - 1) 0.0 else 0.5 * (mesh[i + 1] - mesh[i])

    /** `true` where the node is a Dirichlet datum rather than an unknown. */
    private fun dirichlet(lateralIndex: Int, verticalIndex: Int): Boolean =
        verticalIndex == 0 || verticalIndex == verticalCount ||
                (farFieldDirichlet && lateralIndex == lateralCount)

    /**
     * The far-field datum: the **isolated electrode's own** 2:1 Gouy-Chapman profile, not zero.
     *
     * Setting `y = 0` on the outer boundaries is wrong and it is wrong in a way that is invisible
     * in the load: it puts a spurious boundary layer along the last mesh column, and the induced
     * charge it fabricates there is an order of magnitude larger than the tile's own. The
     * electrode is macroscopic and held at a potential, so far from the tile the solution IS its
     * single-plate profile, and using that closes the charge balance instead of breaking it.
     */
    private fun farFieldPotential(
        electrodeReducedPotential: Double,
        verticalIndex: Int
    ): Double = asymmetricPotentialProfile(
        height[verticalIndex], electrodeReducedPotential, inverseDebyeLength
    )

    /**
     * Solves the edge at electrode reduced potential [electrodeReducedPotential] and the given
     * **signed** face charge densities in `e/nm²`.
     */
    fun solve(
        electrodeReducedPotential: Double,
        bottomChargeDensity: Double,
        topChargeDensity: Double = bottomChargeDensity,
        rimChargeDensity: Double = 0.0,
        faceShape: EdgeChargeShape? = null,
        rimShape: EdgeChargeShape? = null
    ): EdgeSolution {
        // A shape multiplies its wall's density; a null shape is a factor of exactly 1.0, and
        // `x * 1.0` is exact in IEEE arithmetic, so an unshaped solve is bit-identical to the one
        // this method performed before `P-14` added the arguments. That is asserted as a test.
        val faceFactor = { x: Double -> faceShape?.factor(tileHalfWidth - x) ?: 1.0 }
        val rimFactor = { z: Double -> rimShape?.factor(z - gapHeight) ?: 1.0 }
        val nodes = stride * (lateralCount + 1)
        var assignedTileCharge = 0.0
        val active = BooleanArray(nodes)
        val volume = DoubleArray(nodes)
        val north = DoubleArray(nodes)
        val south = DoubleArray(nodes)
        val east = DoubleArray(nodes)
        val west = DoubleArray(nodes)
        val wall = DoubleArray(nodes)
        for (j in 0..lateralCount) {
            val dxm = halfBefore(lateral, j)
            val dxp = halfAfter(lateral, j)
            for (i in 0..verticalCount) {
                val k = index(j, i)
                val dzm = halfBefore(height, i)
                val dzp = halfAfter(height, i)
                val ne = dxp > 0.0 && dzp > 0.0 &&
                        !solid(lateral[j] + 0.5 * dxp, height[i] + 0.5 * dzp)
                val nw = dxm > 0.0 && dzp > 0.0 &&
                        !solid(lateral[j] - 0.5 * dxm, height[i] + 0.5 * dzp)
                val se = dxp > 0.0 && dzm > 0.0 &&
                        !solid(lateral[j] + 0.5 * dxp, height[i] - 0.5 * dzm)
                val sw = dxm > 0.0 && dzm > 0.0 &&
                        !solid(lateral[j] - 0.5 * dxm, height[i] - 0.5 * dzm)
                val cell = (if (ne) dxp * dzp else 0.0) + (if (nw) dxm * dzp else 0.0) +
                        (if (se) dxp * dzm else 0.0) + (if (sw) dxm * dzm else 0.0)
                if (cell <= 0.0 || dirichlet(j, i)) continue
                active[k] = true
                volume[k] = cell
                val northArea = (if (ne) dxp else 0.0) + (if (nw) dxm else 0.0)
                val southArea = (if (se) dxp else 0.0) + (if (sw) dxm else 0.0)
                val eastArea = (if (ne) dzp else 0.0) + (if (se) dzm else 0.0)
                val westArea = (if (nw) dzp else 0.0) + (if (sw) dzm else 0.0)
                if (northArea > 0.0) {
                    north[k] = permittivity * northArea / (height[i + 1] - height[i])
                }
                if (southArea > 0.0) {
                    south[k] = permittivity * southArea / (height[i] - height[i - 1])
                }
                if (eastArea > 0.0) {
                    east[k] = permittivity * eastArea / (lateral[j + 1] - lateral[j])
                }
                if (westArea > 0.0) {
                    west[k] = permittivity * westArea / (lateral[j] - lateral[j - 1])
                }
                // Wall segments: a side is a wall where the cell is fluid and its neighbour
                // quadrant across that side is solid. The charge follows from where the wall is.
                val northWallEast = if (!ne && se) dxp else 0.0
                val northWallWest = if (!nw && sw) dxm else 0.0
                val southWallEast = if (!se && ne) dxp else 0.0
                val southWallWest = if (!sw && nw) dxm else 0.0
                val westWallUp = if (!nw && ne) dzp else 0.0
                val westWallDown = if (!sw && se) dzm else 0.0
                val northWall = northWallEast * faceFactor(lateral[j] + 0.5 * dxp) +
                        northWallWest * faceFactor(lateral[j] - 0.5 * dxm)
                val southWall = southWallEast * faceFactor(lateral[j] + 0.5 * dxp) +
                        southWallWest * faceFactor(lateral[j] - 0.5 * dxm)
                val westWall = westWallUp * rimFactor(height[i] + 0.5 * dzp) +
                        westWallDown * rimFactor(height[i] - 0.5 * dzm)
                var flux = 0.0
                if (i == bottomIndex) flux += bottomChargeDensity * northWall
                if (i == topIndex) flux += topChargeDensity * southWall
                if (j == rimIndex) flux += rimChargeDensity * westWall
                assignedTileCharge += flux
                wall[k] = coupling * flux
            }
        }
        val potential = initialGuess(
            electrodeReducedPotential, bottomChargeDensity, topChargeDensity
        )
        var iterations = 0
        var correction = Double.MAX_VALUE
        var linear = 0
        val residual = DoubleArray(nodes)
        val diagonal = DoubleArray(nodes)
        while (iterations < 80 && correction > 1e-11) {
            var largestResidual = 0.0
            for (k in 0 until nodes) {
                if (!active[k]) continue
                // Every neighbour access is guarded by its own conductance, which is exactly zero
                // wherever the face does not exist — at the symmetry plane, at the far field, and
                // against the tile. Guarding on the coefficient rather than on the index keeps the
                // stencil and the geometry in one place.
                var balance = wall[k] +
                        coupling * ionModel.chargeDensity(potential[k], medium) * volume[k]
                if (north[k] != 0.0) balance += north[k] * (potential[k + 1] - potential[k])
                if (south[k] != 0.0) balance += south[k] * (potential[k - 1] - potential[k])
                if (east[k] != 0.0) balance += east[k] * (potential[k + stride] - potential[k])
                if (west[k] != 0.0) balance += west[k] * (potential[k - stride] - potential[k])
                // The assembled matrix is A = −J, with a positive diagonal and negative
                // off-diagonals, so that it is SPD and conjugate gradients applies. Newton's
                // `J δ = −F` is therefore `A δ = +F`, and F is the balance itself — the negation
                // lives in the matrix, not in the right-hand side, and putting it in both is
                // silent: it does not diverge, it converges to the reflection of the answer.
                residual[k] = balance
                diagonal[k] = north[k] + south[k] + east[k] + west[k] -
                        coupling * ionModel.chargeDensitySlope(potential[k], medium) * volume[k]
                largestResidual = max(largestResidual, abs(residual[k]))
            }
            val step = conjugateGradient(
                active, diagonal, north, south, east, west, residual
            )
            linear += step.iterations
            var largest = 0.0
            for (k in 0 until nodes) if (active[k]) largest = max(largest, abs(step.solution[k]))
            val damping = if (largest > 1.5) 1.5 / largest else 1.0
            for (k in 0 until nodes) if (active[k]) potential[k] += damping * step.solution[k]
            correction = largest * damping
            iterations++
        }
        return report(
            potential, active, volume, electrodeReducedPotential,
            bottomChargeDensity, topChargeDensity, rimChargeDensity, rimFactor,
            // With no shape the closed form is kept verbatim, so no emitted charge balance moves
            // by a summation order; with a shape it is the assembly's own total, which is what
            // makes the conservation gate a statement about the discretisation.
            if (faceShape == null && rimShape == null) {
                (bottomChargeDensity + topChargeDensity) * tileHalfWidth +
                        rimChargeDensity * tileThickness
            } else assignedTileCharge,
            iterations, correction, linear
        )
    }

    /**
     * The linear superposition of the three exact 2:1 Gouy-Chapman profiles — one per wall.
     *
     * Superposition is not the answer (that is the whole point of `T-3a`) but it is an excellent
     * *starting* point, and damped Newton on an M-matrix Jacobian is globally convergent from
     * any bounded start, so the guess buys iterations rather than correctness.
     */
    private fun initialGuess(
        electrodeReducedPotential: Double,
        bottomChargeDensity: Double,
        topChargeDensity: Double
    ): DoubleArray {
        val bottomPotential = asymmetricReducedSurfacePotential(
            bottomChargeDensity, inverseDebyeLength, bjerrumLength
        )
        val topPotential = asymmetricReducedSurfacePotential(
            topChargeDensity, inverseDebyeLength, bjerrumLength
        )
        val guess = DoubleArray(stride * (lateralCount + 1))
        for (j in 0..lateralCount) {
            val underTile = lateral[j] <= tileHalfWidth
            for (i in 0..verticalCount) {
                var value = asymmetricPotentialProfile(
                    height[i], electrodeReducedPotential, inverseDebyeLength
                )
                if (underTile && height[i] <= gapHeight) {
                    value += asymmetricPotentialProfile(
                        gapHeight - height[i], bottomPotential, inverseDebyeLength
                    )
                }
                if (underTile && height[i] >= gapHeight + tileThickness) {
                    value += asymmetricPotentialProfile(
                        height[i] - gapHeight - tileThickness, topPotential, inverseDebyeLength
                    )
                }
                guess[index(j, i)] = min(25.0, max(-25.0, value))
            }
        }
        // The Dirichlet data are exact, not guessed.
        for (j in 0..lateralCount) {
            guess[index(j, 0)] = electrodeReducedPotential
            guess[index(j, verticalCount)] =
                farFieldPotential(electrodeReducedPotential, verticalCount)
        }
        if (farFieldDirichlet) {
            for (i in 1..verticalCount) {
                guess[index(lateralCount, i)] = farFieldPotential(electrodeReducedPotential, i)
            }
            guess[index(lateralCount, 0)] = electrodeReducedPotential
        }
        return guess
    }

    private class LinearSolution(val solution: DoubleArray, val iterations: Int)

    /**
     * Conjugate gradients on the SPD negated Jacobian, preconditioned by a symmetric block
     * Gauss-Seidel sweep whose blocks are the `x`-columns.
     */
    private fun conjugateGradient(
        active: BooleanArray,
        diagonal: DoubleArray,
        north: DoubleArray,
        south: DoubleArray,
        east: DoubleArray,
        west: DoubleArray,
        rightHandSide: DoubleArray
    ): LinearSolution {
        val nodes = diagonal.size
        val solution = DoubleArray(nodes)
        val residual = DoubleArray(nodes)
        var norm = 0.0
        for (k in 0 until nodes) if (active[k]) {
            residual[k] = rightHandSide[k]
            norm += residual[k] * residual[k]
        }
        norm = sqrt(norm)
        if (norm == 0.0) return LinearSolution(solution, 0)
        val preconditioned = DoubleArray(nodes)
        val forward = DoubleArray(nodes)
        val scaled = DoubleArray(nodes)
        val sweepScratch = DoubleArray(stride)
        val valueScratch = DoubleArray(stride)
        precondition(
            active, diagonal, north, south, east, west, residual, preconditioned,
            forward, scaled, sweepScratch, valueScratch
        )
        val direction = preconditioned.copyOf()
        val product = DoubleArray(nodes)
        var rho = dot(active, residual, preconditioned)
        var iterations = 0
        while (iterations < 3000) {
            apply(active, diagonal, north, south, east, west, direction, product)
            val denominator = dot(active, direction, product)
            if (denominator == 0.0) break
            val alpha = rho / denominator
            var current = 0.0
            for (k in 0 until nodes) if (active[k]) {
                solution[k] += alpha * direction[k]
                residual[k] -= alpha * product[k]
                current += residual[k] * residual[k]
            }
            iterations++
            // Inexact Newton: the linear solve only has to be accurate enough not to limit the
            // quadratic convergence of the outer iteration, and 1e-9 relative is far past that.
            if (sqrt(current) <= 1e-9 * norm) break
            precondition(
                active, diagonal, north, south, east, west, residual, preconditioned,
                forward, scaled, sweepScratch, valueScratch
            )
            val next = dot(active, residual, preconditioned)
            val beta = next / rho
            rho = next
            for (k in 0 until nodes) if (active[k]) {
                direction[k] = preconditioned[k] + beta * direction[k]
            }
        }
        return LinearSolution(solution, iterations)
    }

    private fun dot(active: BooleanArray, a: DoubleArray, b: DoubleArray): Double {
        var sum = 0.0
        for (k in a.indices) if (active[k]) sum += a[k] * b[k]
        return sum
    }

    private fun apply(
        active: BooleanArray,
        diagonal: DoubleArray,
        north: DoubleArray,
        south: DoubleArray,
        east: DoubleArray,
        west: DoubleArray,
        vector: DoubleArray,
        result: DoubleArray
    ) {
        for (k in vector.indices) {
            if (!active[k]) {
                result[k] = 0.0
                continue
            }
            var value = diagonal[k] * vector[k]
            if (north[k] != 0.0 && active[k + 1]) value -= north[k] * vector[k + 1]
            if (south[k] != 0.0 && active[k - 1]) value -= south[k] * vector[k - 1]
            if (east[k] != 0.0 && active[k + stride]) value -= east[k] * vector[k + stride]
            if (west[k] != 0.0 && active[k - stride]) value -= west[k] * vector[k - stride]
            result[k] = value
        }
    }

    /**
     * Symmetric block Gauss-Seidel: a forward sweep over the `x`-columns, a diagonal scaling,
     * then a backward sweep. Each column is a tridiagonal system solved exactly, which is what
     * removes the `z`-grading from the condition number.
     */
    private fun precondition(
        active: BooleanArray,
        diagonal: DoubleArray,
        north: DoubleArray,
        south: DoubleArray,
        east: DoubleArray,
        west: DoubleArray,
        rightHandSide: DoubleArray,
        result: DoubleArray,
        forward: DoubleArray,
        scaled: DoubleArray,
        sweepScratch: DoubleArray,
        valueScratch: DoubleArray
    ) {
        for (j in 0..lateralCount) {
            solveColumn(j, active, diagonal, north, south, forward, sweepScratch, valueScratch) { k ->
                var value = rightHandSide[k]
                if (west[k] != 0.0 && active[k - stride]) value += west[k] * forward[k - stride]
                value
            }
        }
        for (j in 0..lateralCount) {
            for (i in 0..verticalCount) {
                val k = index(j, i)
                if (!active[k]) continue
                var value = diagonal[k] * forward[k]
                if (north[k] != 0.0 && active[k + 1]) value -= north[k] * forward[k + 1]
                if (south[k] != 0.0 && active[k - 1]) value -= south[k] * forward[k - 1]
                scaled[k] = value
            }
        }
        for (j in lateralCount downTo 0) {
            solveColumn(j, active, diagonal, north, south, result, sweepScratch, valueScratch) { k ->
                var value = scaled[k]
                if (east[k] != 0.0 && active[k + stride]) value += east[k] * result[k + stride]
                value
            }
        }
    }

    /** Solves the tridiagonal system of one `x`-column, run by run over its active nodes. */
    private inline fun solveColumn(
        lateralIndex: Int,
        active: BooleanArray,
        diagonal: DoubleArray,
        north: DoubleArray,
        south: DoubleArray,
        out: DoubleArray,
        sweep: DoubleArray,
        value: DoubleArray,
        source: (Int) -> Double
    ) {
        var i = 0
        while (i <= verticalCount) {
            if (!active[index(lateralIndex, i)]) {
                i++
                continue
            }
            var last = i
            while (last + 1 <= verticalCount && active[index(lateralIndex, last + 1)]) last++
            val size = last - i + 1
            for (m in 0 until size) {
                val k = index(lateralIndex, i + m)
                val pivot = if (m == 0) diagonal[k] else diagonal[k] + south[k] * sweep[m - 1]
                sweep[m] = -north[k] / pivot
                value[m] = if (m == 0) source(k) / pivot
                else (source(k) + south[k] * value[m - 1]) / pivot
            }
            out[index(lateralIndex, last)] = value[size - 1]
            for (m in size - 2 downTo 0) {
                out[index(lateralIndex, i + m)] =
                    value[m] - sweep[m] * out[index(lateralIndex, i + m + 1)]
            }
            i = last + 1
        }
    }

    private fun report(
        potential: DoubleArray,
        active: BooleanArray,
        volume: DoubleArray,
        electrodeReducedPotential: Double,
        bottomChargeDensity: Double,
        topChargeDensity: Double,
        rimChargeDensity: Double,
        rimFactor: (Double) -> Double,
        assignedTileCharge: Double,
        iterations: Int,
        correction: Double,
        linearIterations: Int
    ): EdgeSolution {
        val energy = thermalEnergy(temperature)
        val samples = rimIndex + 1
        // The reference planes. The contact-value theorem is the WORST-conditioned way to read a
        // disjoining pressure at a working gap — at 10 nm the answer is 1/127 of the two terms it
        // is the difference of, so a 1e-3 error in the wall potential is a 13% error in the load.
        // T-3a learnt this in one dimension and moved to the best-conditioned interior node. The
        // same move in two dimensions is not free, because the first integral is NOT constant:
        // vertical momentum balance gives dT_zz/dz = -dT_zx/dx, so carrying the interior value to
        // the wall costs one lateral derivative of the integrated shear stress — a smooth,
        // well-scaled quantity that vanishes identically where the load is uniform.
        val gapPlane = bestConditionedPlane(potential, 1, bottomIndex - 1)
        val checkPlane = bestConditionedPlane(potential, 1, bottomIndex - 1, exclude = gapPlane)
        val bulkPlane = bulkReferencePlane()
        val gapShear = integratedShear(potential, gapPlane, bottomIndex)
        val bulkShear = integratedShear(potential, topIndex, bulkPlane)
        val distance = DoubleArray(samples)
        val bottom = DoubleArray(samples)
        val top = DoubleArray(samples)
        val load = DoubleArray(samples)
        for (m in 0 until samples) {
            val j = rimIndex - m
            distance[m] = tileHalfWidth - lateral[j]
            bottom[m] = energy * (
                    planeFirstIntegral(potential, j, gapPlane) + lateralDerivative(gapShear, j)
                    )
            top[m] = energy * (
                    -planeFirstIntegral(potential, j, bulkPlane) + lateralDerivative(bulkShear, j)
                    )
            load[m] = -(bottom[m] + top[m])
        }
        // The rim: f_z = eps (dy/dz)(dy/dx)/(4 pi l_B eps_ref), and dy/dx is EXACT from its own
        // Neumann condition, so an uncharged rim contributes exactly zero.
        val rimSlope = -coupling * rimChargeDensity / permittivity
        var rim = 0.0
        for (i in bottomIndex until topIndex) {
            val lower = verticalSlope(potential, rimIndex, i) * rimFactor(height[i])
            val upper = verticalSlope(potential, rimIndex, i + 1) * rimFactor(height[i + 1])
            rim += 0.5 * (height[i + 1] - height[i]) * (lower + upper)
        }
        rim *= energy * permittivity * rimSlope / (4.0 * PI * bjerrumLength * referencePermittivity)
        var integratedLoad = 0.0
        for (m in 1 until samples) {
            integratedLoad += 0.5 * (distance[m] - distance[m - 1]) * (load[m] + load[m - 1])
        }
        // The INDEPENDENT global route to the same total, and the one that owes nothing to the
        // corner. Take the fluid between the plane z = z* and the top of the domain: its vertical
        // momentum balance has no contribution from the symmetry plane (where T_zx vanishes
        // exactly), none from the far field and none from the bulk cap, so the whole force on the
        // tile is the momentum flux through that one plane, `-∫T_zz dx` over the WHOLE width.
        var flux = 0.0
        for (j in 1..lateralCount) {
            val left = -planeFirstIntegral(potential, j - 1, gapPlane)
            val right = -planeFirstIntegral(potential, j, gapPlane)
            flux += 0.5 * (lateral[j] - lateral[j - 1]) * (left + right)
        }
        flux *= energy
        var spaceCharge = 0.0
        for (k in potential.indices) if (active[k]) {
            spaceCharge += ionModel.chargeDensity(potential[k], medium) * volume[k]
        }
        return EdgeSolution(
            gapHeight = gapHeight,
            tileHalfWidth = tileHalfWidth,
            electrodeReducedPotential = electrodeReducedPotential,
            height = height,
            lateral = lateral,
            reducedPotential = potential,
            distanceFromEdge = distance,
            bottomTraction = bottom,
            topTraction = top,
            downwardLoad = load,
            centrelineCheckLoad = -energy * (
                    planeFirstIntegral(potential, 0, checkPlane) +
                            lateralDerivative(gapShear, 0) -
                            planeFirstIntegral(potential, 0, bulkPlane) +
                            lateralDerivative(bulkShear, 0)
                    ),
            centrelineContactLoad = -energy * (
                    contactTraction(
                        potential[index(0, bottomIndex)], bottomChargeDensity, 0.0
                    ) - contactTraction(
                        potential[index(0, topIndex)], topChargeDensity, 0.0
                    )
                    ),
            rimLineForce = rim,
            integratedLoadPerUnitEdge = integratedLoad,
            momentumFluxLoadPerUnitEdge = flux,
            tileChargePerLength = assignedTileCharge,
            electrodeChargePerLength = electrodeCharge(potential),
            outerBoundaryChargePerLength = outerBoundaryCharge(potential),
            spaceChargePerLength = spaceCharge,
            newtonIterations = iterations,
            newtonCorrection = correction,
            linearIterations = linearIterations
        )
    }

    /** `Π_osm(y) − ε[(∂_z y)² − (∂_x y)²]/(8π l_B ε_ref)` at a wall, in `k_BT/nm³`. */
    private fun contactTraction(
        wallPotential: Double,
        chargeDensity: Double,
        tangential: Double
    ): Double = ionModel.osmoticPressureExcess(wallPotential, medium) -
            2.0 * PI * bjerrumLength * referencePermittivity / permittivity *
            chargeDensity * chargeDensity +
            permittivity * tangential * tangential /
            (8.0 * PI * bjerrumLength * referencePermittivity)

    /** `∂y/∂z` at a node, second order on the graded vertical mesh. */
    private fun verticalSlope(
        potential: DoubleArray,
        lateralIndex: Int,
        verticalIndex: Int
    ): Double {
        val i = verticalIndex.coerceIn(1, verticalCount - 1)
        val left = height[i] - height[i - 1]
        val right = height[i + 1] - height[i]
        return -right / (left * (left + right)) * potential[index(lateralIndex, i - 1)] +
                (right - left) / (left * right) * potential[index(lateralIndex, i)] +
                left / (right * (left + right)) * potential[index(lateralIndex, i + 1)]
    }

    /** `∂y/∂x` at a node, second order on the graded lateral mesh; zero at the symmetry plane. */
    private fun lateralSlope(potential: DoubleArray, lateralIndex: Int, verticalIndex: Int): Double {
        if (lateralIndex == 0) return 0.0
        val j = lateralIndex.coerceAtMost(lateralCount - 1)
        val left = lateral[j] - lateral[j - 1]
        val right = lateral[j + 1] - lateral[j]
        return -right / (left * (left + right)) * potential[index(j - 1, verticalIndex)] +
                (right - left) / (left * right) * potential[index(j, verticalIndex)] +
                left / (right * (left + right)) * potential[index(j + 1, verticalIndex)]
    }

    /** `−T_zz = Π_osm − ε[(∂_z y)² − (∂_x y)²]/(8π l_B ε_ref)` at a node, in `k_BT/nm³`. */
    private fun planeFirstIntegral(
        potential: DoubleArray,
        lateralIndex: Int,
        verticalIndex: Int
    ): Double {
        val vertical = verticalSlope(potential, lateralIndex, verticalIndex)
        val horizontal = lateralSlope(potential, lateralIndex, verticalIndex)
        return ionModel.osmoticPressureExcess(potential[index(lateralIndex, verticalIndex)], medium) -
                permittivity * (vertical * vertical - horizontal * horizontal) /
                (8.0 * PI * bjerrumLength * referencePermittivity)
    }

    /**
     * The plane on which the first integral is least a difference of large numbers, exactly as
     * `T-3a` chooses its evaluation node — the one minimising `|Π_osm| + |Maxwell|`, read on the
     * centre-line column where the problem is one-dimensional.
     */
    private fun bestConditionedPlane(
        potential: DoubleArray,
        from: Int,
        to: Int,
        exclude: Int = -1
    ): Int {
        var best = from
        var bestScale = Double.MAX_VALUE
        for (i in from..to) {
            if (i == exclude) continue
            val slope = verticalSlope(potential, 0, i)
            val scale = abs(ionModel.osmoticPressureExcess(potential[index(0, i)], medium)) +
                    permittivity * slope * slope / (8.0 * PI * bjerrumLength * referencePermittivity)
            if (scale < bestScale) {
                bestScale = scale
                best = i
            }
        }
        return best
    }

    /** The reference plane above the tile — four screening lengths out, where the field is gone. */
    private fun bulkReferencePlane(): Int {
        val target = gapHeight + tileThickness + 4.0 / inverseDebyeLength
        var best = topIndex + 1
        for (i in topIndex + 1 until verticalCount) {
            if (abs(height[i] - target) < abs(height[best] - target)) best = i
        }
        return best.coerceIn(topIndex + 1, verticalCount - 1)
    }

    /**
     * `G(x) = ∫ T_zx dz` between two planes, at every lateral node up to the rim, in `k_BT/nm²`.
     *
     * `T_zx = ε(∂_z y)(∂_x y)/(4π l_B ε_ref)` is a product of two first derivatives — no
     * cancellation, no small difference of large numbers — so this is the well-conditioned half
     * of carrying an interior first integral out to the wall. It vanishes identically wherever
     * the solution is laterally uniform, which is why the correction is invisible in the interior
     * and is the whole story at the rim.
     */
    private fun integratedShear(potential: DoubleArray, from: Int, to: Int): DoubleArray {
        val shear = DoubleArray(rimIndex + 2)
        val factor = permittivity / (4.0 * PI * bjerrumLength * referencePermittivity)
        for (j in 0..rimIndex + 1) {
            var total = 0.0
            for (i in from until to) {
                val lower = verticalSlope(potential, j, i) * lateralSlope(potential, j, i)
                val upper = verticalSlope(potential, j, i + 1) * lateralSlope(potential, j, i + 1)
                total += 0.5 * (height[i + 1] - height[i]) * (lower + upper)
            }
            shear[j] = factor * total
        }
        return shear
    }

    /** `dG/dx` at a lateral node — zero at the symmetry plane, one-sided at the rim. */
    private fun lateralDerivative(shear: DoubleArray, lateralIndex: Int): Double {
        if (lateralIndex == 0) return 0.0
        if (lateralIndex == rimIndex) {
            // Only data from under the tile: beyond the rim the identity being applied — that the
            // tile's bottom face is above — simply does not hold.
            val near = lateral[rimIndex] - lateral[rimIndex - 1]
            val far = lateral[rimIndex] - lateral[rimIndex - 2]
            return (near + far) / (near * far) * shear[rimIndex] -
                    far / (near * (far - near)) * shear[rimIndex - 1] +
                    near / (far * (far - near)) * shear[rimIndex - 2]
        }
        val left = lateral[lateralIndex] - lateral[lateralIndex - 1]
        val right = lateral[lateralIndex + 1] - lateral[lateralIndex]
        return -right / (left * (left + right)) * shear[lateralIndex - 1] +
                (right - left) / (left * right) * shear[lateralIndex] +
                left / (right * (left + right)) * shear[lateralIndex + 1]
    }

    /** `σ_e = −ε (∂y/∂z)|₀/(4π l_B ε_ref)`, integrated along the electrode, in `e/nm`. */
    private fun electrodeCharge(potential: DoubleArray): Double {
        var total = 0.0
        for (j in 0..lateralCount) {
            val width = halfBefore(lateral, j) + halfAfter(lateral, j)
            val near = height[1] - height[0]
            val far = height[2] - height[0]
            val slope = -(near + far) / (near * far) * potential[index(j, 0)] +
                    far / (near * (far - near)) * potential[index(j, 1)] -
                    near / (far * (far - near)) * potential[index(j, 2)]
            total += width * -permittivity * slope / coupling
        }
        return total
    }

    /** The induced charge on the bulk boundaries — reported so that "far enough" is a number. */
    private fun outerBoundaryCharge(potential: DoubleArray): Double {
        var total = 0.0
        val n = verticalCount
        for (j in 0..lateralCount) {
            val width = halfBefore(lateral, j) + halfAfter(lateral, j)
            val near = height[n] - height[n - 1]
            val far = height[n] - height[n - 2]
            val slope = (near + far) / (near * far) * potential[index(j, n)] -
                    far / (near * (far - near)) * potential[index(j, n - 1)] +
                    near / (far * (far - near)) * potential[index(j, n - 2)]
            total += width * permittivity * slope / coupling
        }
        val m = lateralCount
        for (i in 1 until verticalCount) {
            val heightExtent = halfBefore(height, i) + halfAfter(height, i)
            val near = lateral[m] - lateral[m - 1]
            val far = lateral[m] - lateral[m - 2]
            val slope = (near + far) / (near * far) * potential[index(m, i)] -
                    far / (near * (far - near)) * potential[index(m - 1, i)] +
                    near / (far * (far - near)) * potential[index(m - 2, i)]
            total += heightExtent * permittivity * slope / coupling
        }
        return total
    }

}

/** The base number of mesh intervals across the gap, before [PoissonBoltzmannEdge.refinement]. */
const val GAP_NODES: Int = 64

/** The base number of mesh intervals across the tile's own thickness. */
const val TILE_NODES: Int = 20

/** The base number of mesh intervals above the tile. */
const val ABOVE_NODES: Int = 28

/** The base number of lateral mesh intervals under the tile. */
const val INNER_NODES: Int = 48

/** The base number of lateral mesh intervals beyond the rim. */
const val OUTER_NODES: Int = 40

/** The default wall-clustering parameter `β` of the 2-D graded mesh, normal to the walls. */
const val DEFAULT_EDGE_MESH_GRADING: Double = 6.0

/**
 * The distance from the rim inside which the pointwise traction is not used, in nm.
 *
 * One duplex radius. A Rothemund sheet's rim is a row of B-DNA helix ends, so the 90-degree
 * re-entrant corner this model puts there — and the mesh-divergent traction singularity that goes
 * with it — is a property of the idealisation and not of the tile.
 */
const val DEFAULT_RIM_STANDOFF: Double = 1.0

/** The default rim-clustering parameter `β` of the lateral mesh — see [PoissonBoltzmannEdge]. */
const val DEFAULT_EDGE_LATERAL_GRADING: Double = 2.5

/** A `tanh`-graded mesh clustered at **both** ends of `[start, end]`. */
internal fun twoSidedMesh(start: Double, end: Double, intervals: Int, grading: Double): List<Double> =
    (0..intervals).map { i ->
        val reduced = i.toDouble() / intervals - 0.5
        start + (end - start) * 0.5 * (1.0 + tanh(grading * reduced) / tanh(0.5 * grading))
    }

/**
 * A `tanh`-graded mesh clustered at one end of `[start, end]`.
 *
 * The direction is easy to invert and the inversion is silent — it leaves a perfectly
 * well-formed mesh that simply resolves the wrong wall — so it is written the way it is checked:
 * `1 − tanh(β(1−ξ))/tanh(β)` has its *smallest* increments at `ξ = 0`, hence clusters at the
 * **start**, and `tanh(βξ)/tanh(β)` has them at `ξ = 1`, hence clusters at the **end**.
 */
internal fun oneSidedMesh(
    start: Double,
    end: Double,
    intervals: Int,
    grading: Double,
    clusteredAtStart: Boolean
): List<Double> = (0..intervals).map { i ->
    val reduced = i.toDouble() / intervals
    val fraction = if (clusteredAtStart) 1.0 - tanh(grading * (1.0 - reduced)) / tanh(grading)
    else tanh(grading * reduced) / tanh(grading)
    start + (end - start) * fraction
}

/**
 * The solved edge, and the lateral load profile read off it.
 *
 * Not `@Serializable` — the study builds its own records, so that a result file carries the
 * numbers a claim quotes rather than a hundred thousand grid nodes.
 */
class EdgeSolution internal constructor(
    /** The tile-electrode separation in nm. */
    val gapHeight: Double,
    /** Half the tile's lateral extent in nm. */
    val tileHalfWidth: Double,
    /** The imposed diffuse-layer potential at the electrode, reduced. */
    val electrodeReducedPotential: Double,
    /** The mesh lines normal to the electrode, in nm. */
    val height: DoubleArray,
    /** The mesh lines along the electrode, in nm. */
    val lateral: DoubleArray,
    /** `y = eψ/k_BT` at every node, laid out with a stride of [height]`.size`. */
    val reducedPotential: DoubleArray,
    /** Distance inward from the rim in nm, ascending from 0 to the tile half-width. */
    val distanceFromEdge: DoubleArray,
    /** The **upward** traction from the tile's bottom face, `pN/nm²`. */
    val bottomTraction: DoubleArray,
    /** The **upward** contribution of the tile's top face, `pN/nm²`; zero deep under the tile. */
    val topTraction: DoubleArray,
    /** The **downward** load on the tile, `pN/nm²` — what `C-0006`'s plate consumes. */
    val downwardLoad: DoubleArray,
    /** The same load at the centre-line, from a SECOND reference plane — a discretisation check. */
    val centrelineCheckLoad: Double,
    /** The same load from the contact-value theorem, which is the ill-conditioned route. */
    val centrelineContactLoad: Double,
    /** The **upward** line force from the rim face, `pN/nm` of edge; exactly 0 for an uncharged rim. */
    val rimLineForce: Double,
    /** `∫downwardLoad dx` from the rim to the centre-line, `pN/nm` of edge. */
    val integratedLoadPerUnitEdge: Double,
    /**
     * The same total by an **independent global route**, `pN/nm` of edge.
     *
     * The momentum flux through one horizontal plane in the gap, integrated over the whole width.
     * It uses no wall value, no lateral derivative and nothing from the re-entrant corner, so
     * comparing it against [integratedLoadPerUnitEdge] tests the pointwise profile — including
     * its corner — against a route that cannot be wrong there.
     */
    val momentumFluxLoadPerUnitEdge: Double,
    /** The tile's own charge per unit length of edge, `e/nm`. */
    val tileChargePerLength: Double,
    /** The induced electrode charge per unit length of edge, `e/nm`. */
    val electrodeChargePerLength: Double,
    /** The induced charge on the bulk boundaries, `e/nm` — small by construction, reported anyway. */
    val outerBoundaryChargePerLength: Double,
    /** `∫∫ρ dA` over the fluid domain, `e/nm`. */
    val spaceChargePerLength: Double,
    /** How many Newton steps were taken. */
    val newtonIterations: Int,
    /** The largest correction of the final Newton step. */
    val newtonCorrection: Double,
    /** How many conjugate-gradient iterations were taken in total. */
    val linearIterations: Int
) {

    /** The downward load at the tile centre-line, `pN/nm²` — the 1-D answer, and asserted to be. */
    val centrelineLoad: Double get() = downwardLoad[downwardLoad.size - 1]

    /** The downward force per unit length of edge on half the tile, `pN/nm`, rim included. */
    val verticalForcePerUnitEdge: Double get() = integratedLoadPerUnitEdge - rimLineForce

    /**
     * How far the two reference planes disagree about the centre-line load, relative.
     *
     * The first integral is exactly constant across the gap wherever the solution is laterally
     * uniform, so two different interior planes must return the same traction; that they do not,
     * exactly, is the discretisation error, and the solve grades itself without a reference
     * solution. Compare [centrelineContactLoad], which reads the same number off the wall and is
     * an order of magnitude worse for the reason `CLAUDE.md` records.
     */
    val centrelineRouteSpread: Double
        get() = if (centrelineLoad == 0.0) abs(centrelineCheckLoad)
        else abs(centrelineCheckLoad - centrelineLoad) / abs(centrelineLoad)

    /** How far the contact-value route disagrees — reported to show why it is not the one used. */
    val contactRouteSpread: Double
        get() = if (centrelineLoad == 0.0) abs(centrelineContactLoad)
        else abs(centrelineContactLoad - centrelineLoad) / abs(centrelineLoad)

    /** True when the two routes agree well enough for the taper to be a measurement. */
    val numericallyResolved: Boolean get() = centrelineRouteSpread < 0.02

    /** The relative closure of the 2-D charge balance — tile + electrode + boundaries + space. */
    val chargeBalance: Double
        get() {
            val total = tileChargePerLength + electrodeChargePerLength +
                    outerBoundaryChargePerLength + spaceChargePerLength
            val scale = abs(tileChargePerLength) + abs(electrodeChargePerLength)
            return if (scale == 0.0) abs(total) else abs(total) / scale
        }

    /** What the load would integrate to over the half-tile with no edge at all, `pN/nm` of edge. */
    val noEdgeLoadPerUnitEdge: Double get() = tileHalfWidth * centrelineLoad

    /**
     * The total load the edge **adds** over the half-tile, `pN/nm` of edge, from the global route.
     *
     * Negative means the finite tile carries *more* than a 1-D extrapolation of its own centre,
     * which is the sign a capacitor's fringing field has and the opposite of the sign `C-0006`
     * assumed. Taken from [momentumFluxLoadPerUnitEdge] because that route owes nothing to the
     * unresolvable corner.
     */
    val totalDeficitPerUnitEdge: Double
        get() = noEdgeLoadPerUnitEdge - momentumFluxLoadPerUnitEdge

    /** The taper in exactly the parameterisation `C-0006` and `C-0009` consume. */
    fun taperFit(standoff: Double = DEFAULT_RIM_STANDOFF): EdgeTaperFit =
        fitEdgeTaper(distanceFromEdge, downwardLoad, centrelineLoad, standoff)

    /**
     * The part of the edge effect that lives inside the standoff, as a **line** load on the rim
     * in `pN/nm` of edge — the difference between the global total and what the fitted taper
     * accounts for.
     *
     * This is where the unresolvable corner goes. It is a line load and not a pressure, which is
     * also the honest way for a plate model to receive a sub-resolution edge effect.
     */
    fun rimResidualPerUnitEdge(standoff: Double = DEFAULT_RIM_STANDOFF): Double =
        totalDeficitPerUnitEdge - taperFit(standoff).loadDeficit

}
