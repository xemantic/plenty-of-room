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
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln1p
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tanh

/**
 * The 1-D nonlinear Poisson-Boltzmann solve of the Gen-1 gap — task `T-3a`, leaf `A7.4`.
 *
 * ## Geometry and sign conventions, fixed before deriving
 *
 * - `z` is normal to the electrode, **positive away from it**, origin at the electrode surface.
 * - The **electrode** sits at `z = 0` and is held at a fixed potential (Dirichlet).
 * - The **tile** sits at `z = h` and carries a fixed, Manning-renormalised **negative**
 *   charge (Neumann). "Gap" always means the tile-electrode separation `h`.
 * - A **positive** electrode bias therefore pulls the tile toward `−z`, so `F_es,z < 0`
 *   and `k_es = −∂F_es,z/∂z < 0`, per §1 of the problem definition.
 * - `y = eψ/k_BT` is the **valency-free** reduced potential. Valencies live in the Boltzmann
 *   factors — `e^{−2y}` for `Mg²⁺`, `e^{+y}` for `Cl⁻` — never in `y`.
 *
 * ## Two dissimilar surfaces, and why the mixed problem is the right one
 *
 * This is **not** the constant-charge/constant-charge problem and **not** the
 * constant-potential/constant-potential one. The tile's charge is fixed by chemistry
 * (phosphate `pKa ≈ 1`, so it does not regulate), while the electrode is a metal held at a
 * bias by an external source and its charge is whatever the double layer demands. The two
 * canonical cases bracket this one and neither equals it; at `V = 0` the difference is
 * qualitative, because a constant-potential conductor **attracts** a nearby charge through
 * its induced countercharge while a neutral constant-charge wall does not.
 *
 * The electrode condition is further put in series with a compact (Stern) layer — see
 * [diffusePotentialOfAppliedBias] — because `C-0005` shows the diffuse layer cannot carry
 * more than ~0.2 V before point-ion PB fails, while §3 asks for up to 2 V.
 *
 * ## The force, and why it is not read off a superposition formula
 *
 * `Π(z) = k_BT Σ n_i(z) − ε ε₀ ψ'(z)²/2` is the **first integral** of Poisson-Boltzmann and
 * is exactly constant across a uniform gap. The force per unit area on the tile is
 * `Π − Π_bulk`, evaluated anywhere. It is evaluated here **at the tile contact**, where the
 * Maxwell term is known in closed form from the boundary condition
 * (`2π l_B σ_t²`, no numerical differentiation at all), and its constancy across the gap is
 * then reported as [GapSolution.firstIntegralRelativeSpread] — the solve grading its own
 * discretisation error without a reference solution.
 */

/**
 * The local medium at one height in the gap.
 *
 * Carries the two things `C-0005` says the PEG layer changes about the electrolyte inside
 * it: it **excludes salt** (steric + Born, partition coefficients below 1, so the local
 * screening length is `1/√K` times longer) and it lowers the permittivity slightly
 * (Maxwell-Garnett, 78 → 75 at `φ ≈ 0.03`, a 4% correction and not a mechanism).
 *
 * Defaults are free bulk buffer, so a caller that ignores the layer gets the uniform problem.
 */
@Serializable
data class GapMedium(
    val relativePermittivity: Double = WATER_RELATIVE_PERMITTIVITY,
    val magnesiumPartitionCoefficient: Double = 1.0,
    val chloridePartitionCoefficient: Double = 1.0
) {

    init {
        require(relativePermittivity > 0.0) {
            "relativePermittivity must be positive, was: $relativePermittivity"
        }
        require(magnesiumPartitionCoefficient > 0.0) {
            "magnesiumPartitionCoefficient must be positive, was: $magnesiumPartitionCoefficient"
        }
        require(chloridePartitionCoefficient > 0.0) {
            "chloridePartitionCoefficient must be positive, was: $chloridePartitionCoefficient"
        }
    }

}

/** The medium as a function of height above the electrode. */
fun interface GapMediumProfile {

    /** Returns the medium at [height] nm above the electrode. */
    fun mediumAt(height: Double): GapMedium

}

/** A gap filled with a single medium — free buffer, or a layer that fills it completely. */
fun uniformMedium(medium: GapMedium): GapMediumProfile = GapMediumProfile { medium }

/**
 * A gap holding a grafted layer of [layerThickness] nm on the electrode and free buffer above it.
 *
 * The two-region treatment `C-0005` asks for. Where the tile compresses the layer the two
 * regions degenerate into one and [uniformMedium] is the same thing.
 */
fun layeredMedium(
    layerThickness: Double,
    layer: GapMedium,
    freeBuffer: GapMedium = GapMedium()
): GapMediumProfile = GapMediumProfile { height ->
    if (height < layerThickness) layer else freeBuffer
}

/**
 * The ion statistics of the 2:1 buffer — point ions by default, size-modified (Bikerman) when
 * a finite [maximumIonDensity] is given.
 *
 * ## Bikerman folded in as the `T-6b` bracket
 *
 * The lattice-gas correction is a single denominator,
 * `D(y) = 1 + Σ_j (n_j/n_max)(e^{−z_j y} − 1)`, and both the charge density and the osmotic
 * pressure follow from it. Two things make it worth carrying rather than deferring:
 *
 * 1. it is the **cheap** step `C-0005` names as mandatory before any explicit-ion simulation;
 * 2. the point-ion model is exactly its `n_max → ∞` limit, so the bracket is an executable
 *    limiting case rather than a second implementation.
 *
 * The osmotic excess is written as `n_max ln(1 + Δ/n_max)` with `Δ` the point-ion excess,
 * which *is* the point-ion expression as `n_max → ∞` and never suffers the cancellation the
 * `−n_max ln(1 − Φ)` form would.
 *
 * @param bulkMagnesiumDensity the reservoir `Mg²⁺` density in `nm⁻³`.
 * @param maximumIonDensity the lattice site density in `nm⁻³`; infinite means point ions.
 */
@Serializable
data class IonModel(
    val bulkMagnesiumDensity: Double,
    val maximumIonDensity: Double = Double.POSITIVE_INFINITY
) {

    init {
        require(bulkMagnesiumDensity > 0.0) {
            "bulkMagnesiumDensity must be positive, was: $bulkMagnesiumDensity"
        }
        require(maximumIonDensity > 0.0) {
            "maximumIonDensity must be positive, was: $maximumIonDensity"
        }
    }

    /** Twice the magnesium density, by electroneutrality of the reservoir. */
    val bulkChlorideDensity: Double get() = 2.0 * bulkMagnesiumDensity

    /** True when the model is the point-ion (classical Poisson-Boltzmann) one. */
    val pointIon: Boolean get() = maximumIonDensity.isInfinite()

    private fun magnesium(reducedPotential: Double, medium: GapMedium): Double =
        bulkMagnesiumDensity * medium.magnesiumPartitionCoefficient * exp(-2.0 * reducedPotential)

    private fun chloride(reducedPotential: Double, medium: GapMedium): Double =
        bulkChlorideDensity * medium.chloridePartitionCoefficient * exp(reducedPotential)

    /**
     * Returns the point-ion osmotic excess `Σ n_i(y) − Σ n_i(0)` in `nm⁻³`, before any
     * lattice-gas correction.
     */
    private fun idealOsmoticExcess(reducedPotential: Double, medium: GapMedium): Double =
        magnesium(reducedPotential, medium) + chloride(reducedPotential, medium) -
                bulkMagnesiumDensity * medium.magnesiumPartitionCoefficient -
                bulkChlorideDensity * medium.chloridePartitionCoefficient

    /** The lattice-gas denominator `D(y)`; exactly 1 for point ions. */
    private fun packingDenominator(reducedPotential: Double, medium: GapMedium): Double =
        if (pointIon) 1.0
        else 1.0 + idealOsmoticExcess(reducedPotential, medium) / maximumIonDensity

    /**
     * Returns the net charge density in `e/nm³`, `ρ = 2 n_Mg − n_Cl`.
     *
     * Negative where `Cl⁻` dominates, positive where `Mg²⁺` does, and zero in the reservoir —
     * which is `2c − 2c = 0`, the 2:1 electroneutrality condition.
     */
    fun chargeDensity(reducedPotential: Double, medium: GapMedium): Double =
        (2.0 * magnesium(reducedPotential, medium) - chloride(reducedPotential, medium)) /
                packingDenominator(reducedPotential, medium)

    /** Returns `dρ/dy`, which is strictly negative — the reason Newton converges here. */
    fun chargeDensitySlope(reducedPotential: Double, medium: GapMedium): Double {
        val magnesium = magnesium(reducedPotential, medium)
        val chloride = chloride(reducedPotential, medium)
        val numerator = 2.0 * magnesium - chloride
        val denominator = packingDenominator(reducedPotential, medium)
        val ideal = (-4.0 * magnesium - chloride) / denominator
        return if (pointIon) ideal
        else ideal + numerator * numerator / (maximumIonDensity * denominator * denominator)
    }

    /**
     * Returns the osmotic pressure **excess over the local reservoir**, `Π(y) − Π(0)`,
     * in `k_BT/nm³`.
     *
     * Referencing to the *local* medium is deliberate: it removes the pure salt-exclusion
     * (depletion) term of the polymer layer, which is a property of the layer's own free
     * energy and belongs to `T-1`/`T-1c`, and leaves exactly the electrostatic part — which
     * is what `F_es` means.
     */
    fun osmoticPressureExcess(reducedPotential: Double, medium: GapMedium): Double {
        val ideal = idealOsmoticExcess(reducedPotential, medium)
        return if (pointIon) ideal else maximumIonDensity * ln1p(ideal / maximumIonDensity)
    }

}

/**
 * The solved profile and everything read off it.
 *
 * Not `@Serializable` — the study builds its own records so that a result file carries the
 * numbers a claim quotes rather than ten thousand grid nodes.
 */
class GapSolution internal constructor(
    /** The tile-electrode separation in nm. */
    val gapHeight: Double,
    /** The node positions in nm, from the electrode at 0 up to the tile at [gapHeight]. */
    val height: DoubleArray,
    /** The reduced potential `y = eψ/k_BT` at every node, from the electrode up to the tile. */
    val reducedPotential: DoubleArray,
    /** The **signed** tile charge density in `e/nm²` that was imposed. */
    val tileSurfaceChargeDensity: Double,
    /** The **signed** electrode charge density in `e/nm²` the solve produced. */
    val electrodeSurfaceChargeDensity: Double,
    /**
     * The disjoining pressure in `k_BT/nm³`, evaluated at the **best-conditioned** node.
     *
     * The first integral is constant, so any node is exact in principle; they are not equally
     * accurate in floating point. At a wall — and especially at a biased electrode — the
     * osmotic and Maxwell terms are each orders of magnitude larger than their difference, and
     * at a 30 nm gap the contact-value form subtracts two numbers of order 0.7 to obtain 6e−5.
     * The node minimising `|Π_osm| + |Maxwell|` is the one where that cancellation is mildest,
     * and choosing it is what keeps the large-gap decay length measurable at all.
     */
    val disjoiningPressure: Double,
    /**
     * The same pressure from the **contact-value theorem** at the tile, where the Maxwell term
     * is exact from the Neumann condition and no derivative is taken.
     *
     * The better-conditioned route at small gaps and under weak bias; the worse one at large
     * gaps, for the cancellation reason above. Both are emitted so the choice is auditable.
     */
    val disjoiningPressureAtContact: Double,
    /** The same pressure evaluated at the midplane — a discretisation check, not a second model. */
    val disjoiningPressureAtMidplane: Double,
    /** `(max − min)/|P|` of the first integral over **all** interior nodes. */
    val firstIntegralRelativeSpread: Double,
    /**
     * The same spread over the middle half of the gap only — the **core** spread.
     *
     * The full spread is dominated by the nodes at the walls, where the osmotic and Maxwell
     * terms are each orders of magnitude above their difference and the cancellation swamps
     * everything; it therefore measures the conditioning of the diagnostic rather than the
     * accuracy of the answer. The core spread measures the accuracy of the answer, because the
     * pressure is evaluated inside that window, and it is the quantity
     * [numericallyResolved] is read from.
     */
    val firstIntegralCoreSpread: Double,
    /** `∫ρ dz` over the gap in `e/nm²`, by Simpson — the charge-conservation route. */
    val integratedSpaceCharge: Double,
    /** How many Newton steps were taken. */
    val newtonIterations: Int,
    /** The largest correction of the final Newton step. */
    val newtonCorrection: Double,
    private val thermalEnergy: Double
) {

    /** The reduced potential at the tile surface. */
    val tileReducedPotential: Double get() = reducedPotential[reducedPotential.size - 1]

    /** The reduced potential at the electrode surface — the imposed Dirichlet value. */
    val electrodeReducedPotential: Double get() = reducedPotential[0]

    /** The disjoining pressure in `pN/nm²`, which is exactly `MPa`. */
    val disjoiningPressureInPiconewtonPerSquareNanometre: Double
        get() = disjoiningPressure * thermalEnergy

    /**
     * The relative spread of the first integral — the solve's own estimate of its
     * discretisation error on the pressure, and the flag `T-3a` uses to decide whether a
     * state point is numerically resolved at all.
     */
    val numericallyResolved: Boolean get() = firstIntegralCoreSpread < 0.02

    /**
     * Returns the **signed** `z` component of the electrostatic force on the tile in `pN`
     * over [footprintArea] `nm²`.
     *
     * Negative means toward the electrode, per the sign convention of this file.
     */
    fun forceOnTile(footprintArea: Double): Double =
        disjoiningPressureInPiconewtonPerSquareNanometre * footprintArea

}

/**
 * The two-point boundary-value problem, discretised conservatively on a **graded** mesh and
 * solved by damped Newton.
 *
 * ## Why the mesh is graded, and why that is not a detail
 *
 * The Gouy-Chapman length at the tile's Manning-renormalised charge is `μ ≈ 0.09 nm`, and at
 * a biased electrode it is shorter still, while the gap runs to 30 nm and the far field
 * decays on `λ_D ≈ 3.9 nm`. A uniform mesh able to resolve `μ` across a 30 nm gap would need
 * millions of nodes; one that does not resolve it leaves an **absolute** error in the tile
 * surface potential which propagates into the contact-value pressure as a constant offset —
 * and a constant offset destroys the decay-length measurement at large gaps, which is the
 * whole of `CH-0004`. So the nodes are placed at
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`z_i = h·[1 + tanh(β(ξ_i − ½))/tanh(β/2)]/2`, `ξ_i = i/N`
 *
 * which clusters smoothly at **both** walls — the grading is analytic, so the discretisation
 * stays second order, which gate 4 checks as an order rather than assuming.
 *
 * The Jacobian is tridiagonal and, because `dρ/dy < 0` everywhere, strictly diagonally
 * dominant — so the Thomas algorithm needs no pivoting and Newton cannot leave the physical
 * branch. That is the same argument that made bisection the right choice in `T-6`, one
 * dimension up.
 *
 * @param gapHeight the tile-electrode separation in nm.
 * @param ionModel the 2:1 buffer statistics.
 * @param mediumProfile the medium as a function of height — free buffer, or layer plus buffer.
 * @param bjerrumLength `l_B` in nm, at the reference permittivity.
 * @param referencePermittivity the permittivity `bjerrumLength` was evaluated at.
 * @param nodes the number of mesh intervals.
 * @param grading the wall-clustering parameter `β`; 0 would be a uniform mesh.
 */
class PoissonBoltzmannGap(
    val gapHeight: Double,
    val ionModel: IonModel,
    val mediumProfile: GapMediumProfile,
    val bjerrumLength: Double,
    val referencePermittivity: Double = WATER_RELATIVE_PERMITTIVITY,
    val nodes: Int = DEFAULT_GAP_MESH_NODES,
    val grading: Double = DEFAULT_GAP_MESH_GRADING,
    private val temperature: Double = ROOM_TEMPERATURE
) {

    init {
        require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
        require(nodes >= 8) { "nodes must be at least 8, was: $nodes" }
        require(bjerrumLength > 0.0) { "bjerrumLength must be positive, was: $bjerrumLength" }
        require(grading > 0.0) { "grading must be positive, was: $grading" }
    }

    private val coupling = 4.0 * PI * bjerrumLength * referencePermittivity

    /** The node positions, clustered at both walls. */
    val height: DoubleArray = DoubleArray(nodes + 1) { i ->
        val reduced = i.toDouble() / nodes - 0.5
        gapHeight * 0.5 * (1.0 + tanh(grading * reduced) / tanh(0.5 * grading))
    }

    private val spacing = DoubleArray(nodes) { height[it + 1] - height[it] }

    /** The finite-volume cell width of each node. */
    private val cell = DoubleArray(nodes + 1) { i ->
        when (i) {
            0 -> 0.5 * spacing[0]
            nodes -> 0.5 * spacing[nodes - 1]
            else -> 0.5 * (spacing[i - 1] + spacing[i])
        }
    }

    private val nodeMedium = Array(nodes + 1) { mediumProfile.mediumAt(height[it]) }
    private val facePermittivity = DoubleArray(nodes) {
        mediumProfile.mediumAt(0.5 * (height[it] + height[it + 1])).relativePermittivity
    }

    /**
     * Solves the gap at electrode reduced potential [electrodeReducedPotential] and
     * **signed** tile charge density [tileSurfaceChargeDensity] in `e/nm²`.
     */
    fun solve(
        electrodeReducedPotential: Double,
        tileSurfaceChargeDensity: Double
    ): GapSolution {
        val flux = coupling * tileSurfaceChargeDensity
        val potential = initialGuess(electrodeReducedPotential, tileSurfaceChargeDensity)
        var iterations = 0
        var correction = Double.MAX_VALUE
        val residual = DoubleArray(nodes + 1)
        val lower = DoubleArray(nodes + 1)
        val diagonal = DoubleArray(nodes + 1)
        val upper = DoubleArray(nodes + 1)
        while (iterations < 200 && correction > 1e-13) {
            assemble(potential, flux, residual, lower, diagonal, upper)
            val step = solveTridiagonal(lower, diagonal, upper, residual)
            var largest = 0.0
            for (value in step) largest = max(largest, abs(value))
            val damping = if (largest > 1.5) 1.5 / largest else 1.0
            for (i in potential.indices) potential[i] += damping * step[i]
            correction = largest * damping
            iterations++
        }
        return report(potential, tileSurfaceChargeDensity, iterations, correction)
    }

    private fun initialGuess(
        electrodeReducedPotential: Double,
        tileSurfaceChargeDensity: Double
    ): DoubleArray {
        // Linear ramp between the two boundary data, clipped: any starting point in the basin
        // will do, because the Jacobian is an M-matrix and damped Newton is globally convergent.
        val tile = 4.0 * PI * bjerrumLength * tileSurfaceChargeDensity * gapHeight
        return DoubleArray(nodes + 1) { i ->
            val fraction = height[i] / gapHeight
            val value = electrodeReducedPotential * (1.0 - fraction) + tile * fraction
            min(20.0, max(-20.0, value))
        }
    }

    private fun assemble(
        potential: DoubleArray,
        flux: Double,
        residual: DoubleArray,
        lower: DoubleArray,
        diagonal: DoubleArray,
        upper: DoubleArray
    ) {
        residual[0] = 0.0
        diagonal[0] = 1.0
        upper[0] = 0.0
        lower[0] = 0.0
        for (i in 1 until nodes) {
            val left = facePermittivity[i - 1] / (spacing[i - 1] * cell[i])
            val right = facePermittivity[i] / (spacing[i] * cell[i])
            residual[i] = -(
                    right * (potential[i + 1] - potential[i]) -
                            left * (potential[i] - potential[i - 1]) +
                            coupling * ionModel.chargeDensity(potential[i], nodeMedium[i])
                    )
            lower[i] = left
            upper[i] = right
            diagonal[i] = -(left + right) +
                    coupling * ionModel.chargeDensitySlope(potential[i], nodeMedium[i])
        }
        val face = facePermittivity[nodes - 1] / (spacing[nodes - 1] * cell[nodes])
        residual[nodes] = -(
                flux / cell[nodes] - face * (potential[nodes] - potential[nodes - 1]) +
                        coupling * ionModel.chargeDensity(potential[nodes], nodeMedium[nodes])
                )
        lower[nodes] = face
        upper[nodes] = 0.0
        diagonal[nodes] = -face +
                coupling * ionModel.chargeDensitySlope(potential[nodes], nodeMedium[nodes])
    }

    private fun solveTridiagonal(
        lower: DoubleArray,
        diagonal: DoubleArray,
        upper: DoubleArray,
        right: DoubleArray
    ): DoubleArray {
        val size = diagonal.size
        val sweep = DoubleArray(size)
        val value = DoubleArray(size)
        sweep[0] = upper[0] / diagonal[0]
        value[0] = right[0] / diagonal[0]
        for (i in 1 until size) {
            val pivot = diagonal[i] - lower[i] * sweep[i - 1]
            sweep[i] = upper[i] / pivot
            value[i] = (right[i] - lower[i] * value[i - 1]) / pivot
        }
        val solution = DoubleArray(size)
        solution[size - 1] = value[size - 1]
        for (i in size - 2 downTo 0) solution[i] = value[i] - sweep[i] * solution[i + 1]
        return solution
    }

    private fun report(
        potential: DoubleArray,
        tileSurfaceChargeDensity: Double,
        iterations: Int,
        correction: Double
    ): GapSolution {
        // The electrode charge, from the half-cell flux balance at node 0 — second order, and
        // independent of the quadrature that the charge-conservation gate checks it against.
        val electrode = -(
                facePermittivity[0] * (potential[1] - potential[0]) / spacing[0] +
                        cell[0] * coupling * ionModel.chargeDensity(potential[0], nodeMedium[0])
                ) / coupling
        // The tile-side pressure, from the contact-value theorem: the Maxwell term is exact,
        // straight out of the Neumann condition, with no numerical derivative anywhere.
        val tileMedium = nodeMedium[nodes]
        val maxwell = 2.0 * PI * bjerrumLength * referencePermittivity /
                tileMedium.relativePermittivity *
                tileSurfaceChargeDensity * tileSurfaceChargeDensity
        val contact = ionModel.osmoticPressureExcess(potential[nodes], tileMedium) - maxwell
        var lowest = Double.MAX_VALUE
        var highest = -Double.MAX_VALUE
        var coreLowest = Double.MAX_VALUE
        var coreHighest = -Double.MAX_VALUE
        var bestConditioning = Double.MAX_VALUE
        var pressure = contact
        for (i in 1 until nodes) {
            val local = firstIntegral(potential, i)
            lowest = min(lowest, local)
            highest = max(highest, local)
            if (height[i] > 0.25 * gapHeight && height[i] < 0.75 * gapHeight) {
                coreLowest = min(coreLowest, local)
                coreHighest = max(coreHighest, local)
            }
            val conditioning = firstIntegralScale(potential, i)
            if (conditioning < bestConditioning) {
                bestConditioning = conditioning
                pressure = local
            }
        }
        val middle = firstIntegral(potential, nodes / 2)
        var integral = 0.0
        for (i in 0 until nodes) {
            integral += 0.5 * spacing[i] * (
                    ionModel.chargeDensity(potential[i], nodeMedium[i]) +
                            ionModel.chargeDensity(potential[i + 1], nodeMedium[i + 1])
                    )
        }
        return GapSolution(
            gapHeight = gapHeight,
            height = height,
            reducedPotential = potential,
            tileSurfaceChargeDensity = tileSurfaceChargeDensity,
            electrodeSurfaceChargeDensity = electrode,
            disjoiningPressure = pressure,
            disjoiningPressureAtContact = contact,
            disjoiningPressureAtMidplane = middle,
            firstIntegralRelativeSpread =
                if (abs(pressure) > 0.0) (highest - lowest) / abs(pressure) else highest - lowest,
            firstIntegralCoreSpread =
                if (abs(pressure) > 0.0) (coreHighest - coreLowest) / abs(pressure)
                else coreHighest - coreLowest,
            integratedSpaceCharge = integral,
            newtonIterations = iterations,
            newtonCorrection = correction,
            thermalEnergy = thermalEnergy(temperature)
        )
    }

    private fun slopeAt(potential: DoubleArray, index: Int): Double {
        val left = spacing[index - 1]
        val right = spacing[index]
        return -right / (left * (left + right)) * potential[index - 1] +
                (right - left) / (left * right) * potential[index] +
                left / (right * (left + right)) * potential[index + 1]
    }

    private fun firstIntegral(potential: DoubleArray, index: Int): Double {
        val slope = slopeAt(potential, index)
        val medium = nodeMedium[index]
        return ionModel.osmoticPressureExcess(potential[index], medium) -
                medium.relativePermittivity * slope * slope /
                (8.0 * PI * bjerrumLength * referencePermittivity)
    }

    /** `|Π_osm| + |Maxwell|` at a node — the magnitude the first integral cancels down from. */
    private fun firstIntegralScale(potential: DoubleArray, index: Int): Double {
        val slope = slopeAt(potential, index)
        val medium = nodeMedium[index]
        return abs(ionModel.osmoticPressureExcess(potential[index], medium)) +
                medium.relativePermittivity * slope * slope /
                (8.0 * PI * bjerrumLength * referencePermittivity)
    }

}

/** The default number of mesh intervals — see the grading discussion in [PoissonBoltzmannGap]. */
const val DEFAULT_GAP_MESH_NODES: Int = 4000

/** The default wall-clustering parameter `β` of the graded mesh. */
const val DEFAULT_GAP_MESH_GRADING: Double = 10.0

/**
 * Returns the **diffuse-layer** potential in volt that an applied bias of [appliedBias] volt
 * produces at the electrode, with a compact (Stern) layer of [sternChargeDensityPerVolt]
 * `e/(V·nm²)` in series.
 *
 * ## Why the series capacitor is not optional
 *
 * `C-0005` establishes that point-ion PB at the electrode fails above ≈ 0.197 V **of
 * diffuse-layer drop**, and compares that against the §3 target of 2 V as "a factor of ten".
 * Those are two different quantities. The diffuse layer and the compact layer are in series,
 * so `V = ψ_d + σ_e(ψ_d)/C_S`, and because `σ_e` grows exponentially in `ψ_d` the compact
 * layer takes almost all of a large bias: **2 V of applied bias is only ≈ 0.24 V of diffuse
 * drop**. The point-ion boundary is crossed at about 1 V of *applied* bias, not at 0.2 V.
 *
 * Bisected on `ψ_d`, which is safe because `ψ_d + σ_e(ψ_d)/C_S` is strictly increasing.
 */
fun diffusePotentialOfAppliedBias(
    gapHeight: Double,
    appliedBias: Double,
    tileSurfaceChargeDensity: Double,
    sternChargeDensityPerVolt: Double,
    ionModel: IonModel,
    mediumProfile: GapMediumProfile,
    bjerrumLength: Double,
    nodes: Int = DEFAULT_GAP_MESH_NODES,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    val volt = thermalVoltage(temperature)
    val gap = PoissonBoltzmannGap(
        gapHeight, ionModel, mediumProfile, bjerrumLength, nodes = nodes, temperature = temperature
    )
    fun excess(diffuse: Double): Double {
        val solution = gap.solve(diffuse / volt, tileSurfaceChargeDensity)
        val compact =
            if (sternChargeDensityPerVolt.isInfinite()) 0.0
            else solution.electrodeSurfaceChargeDensity / sternChargeDensityPerVolt
        return diffuse + compact - appliedBias
    }
    var low = -0.3
    var high = 0.35
    repeat(34) {
        val middle = 0.5 * (low + high)
        if (excess(middle) < 0.0) low = middle else high = middle
    }
    return 0.5 * (low + high)
}

/**
 * Returns the applied bias in volt that produces a diffuse-layer drop of [diffusePotential] —
 * the inverse of [diffusePotentialOfAppliedBias], and the direction in which `C-0005`'s
 * 0.197 V boundary has to be read to be compared against §3's 2 V.
 */
fun appliedBiasOfDiffusePotential(
    gapHeight: Double,
    diffusePotential: Double,
    tileSurfaceChargeDensity: Double,
    sternChargeDensityPerVolt: Double,
    ionModel: IonModel,
    mediumProfile: GapMediumProfile,
    bjerrumLength: Double,
    nodes: Int = DEFAULT_GAP_MESH_NODES,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    val solution = PoissonBoltzmannGap(
        gapHeight, ionModel, mediumProfile, bjerrumLength, nodes = nodes, temperature = temperature
    ).solve(diffusePotential / thermalVoltage(temperature), tileSurfaceChargeDensity)
    return diffusePotential + solution.electrodeSurfaceChargeDensity / sternChargeDensityPerVolt
}
