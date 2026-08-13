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

package com.xemantic.nano.plentyofroom.brush

import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import org.jetbrains.bio.viktor.F64Array
import org.jetbrains.bio.viktor.asF64Array
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Task `T-1d` / leaf `A2.1` — a **numerical self-consistent-field density profile** for the
 * Gen-1 grafted layer, which is the one thing `T-1c` could not supply and the one thing that
 * decides whether the 10 nm design window exists.
 *
 * ## Why this file exists
 *
 * `T-1c` carried two profile *ansätze* — an Alexander box and the Milner-Witten-Cates strong
 * stretching parabola — crossed with three measurement-anchored interaction free energies. It
 * found that the three interaction laws differ by only **1.45×** in `Π_int` at the layer's own
 * volume fraction, while the **profile model decides whether the 10 nm window exists at all**.
 * Neither profile model has its premise met: the box is a trial function, and strong stretching
 * wants `L₀ ≫ R₀` where the Gen-1 layer has `L₀/R₀ ≈ 1`. So the profile is solved here instead
 * of assumed, with the *same* interaction free energies, so that the difference between the
 * answers is profile and nothing else — the same discipline `C-0001` used when it calibrated
 * Milner-Witten-Cates against de Gennes on a shared `L₀`.
 *
 * ## The model, and why it is a propagator rather than a lattice
 *
 * A continuous-chain (Edwards) propagator, not a Scheutjens-Fleer lattice. Both are defensible
 * and the choice rests on two things. First, `C-0003` **measured** the chain to be nearly ideal —
 * PEG in water is a marginal solvent, the thermal blob is 1222 Kuhn segments (167 kDa) and a
 * Gen-1 chain carries 0.02–0.10 of one — so a Gaussian propagator on the measured Kuhn
 * parameters (`b = 1.1 nm`, `n_K = 3.11`, `C-0002`) is the *right* chain model here rather than
 * a convenient one, which is a licence SCF usually does not get. Second, a lattice would have to
 * re-express `f_int(φ)` as a Flory `χ` on a lattice-site convention worth a factor of 2.010
 * (`C-0007`), throwing away the anchoring in measurement that made this calculation worth buying
 * at all; the continuum propagator consumes [InteractionFreeEnergy] unchanged.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`∂q/∂n = (b²/6n_K) ∂²q/∂z² − w(z) q`, &nbsp;
 * `w(z) = μ(φ(z))/k_BT`, &nbsp; `μ = v₀ ∂f_int/∂φ`
 *
 * with `q(z,0)` a source at the grafting surface and `q†(z,0) = 1` for the free end. The density
 * is `φ(z) = σ v₀ ∫₀^N q(z,n) q†(z,N−n) dn / Q`, which conserves `∫φ dz = N σ v₀` identically.
 *
 * ## Boundary conditions, stated rather than chosen silently
 *
 * **Absorbing (Dirichlet) at both surfaces** is the default, and it is what a rigid impenetrable
 * tile is: it removes every chain conformation that would cross the wall. [ScfWallCondition]
 * carries the alternative — a reflecting wall, which is the mid-plane between two identical
 * brushes and costs the chains no conformational entropy — and it is run as a stated sensitivity
 * because it is exactly the assumption under which `T-1c`'s contact-value theorem
 * `P = Π_int(φ(h))` is literally true. Under the absorbing condition the volume fraction
 * **vanishes at the wall**, so that theorem's naive form returns zero and the whole normal stress
 * at the wall is conformational. Its correct continuum form is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`P(h) = k_BT (b²/6n_K) · lim_{z→h} φ(z)/[v₀ (h−z)²] + Π_int(φ(h))`
 *
 * and [ScfProfile.contactPressure] is that, checked against `−∂F/∂h` in the tests.
 *
 * ## What has no counterpart in `T-1c`
 *
 * **There is no height at which an SCF layer exerts exactly zero pressure.** Its outer edge is a
 * real, decaying end distribution rather than the truncation of a trial function, so `L₀` has to
 * be *defined* by a stated resting load and that definition travels with every number derived
 * from it — see [SelfConsistentFieldLayer.restingPressure].
 */

/** Which condition the chain propagator obeys at the confining wall. */
@Serializable
enum class ScfWallCondition {

    /** Dirichlet — a rigid impenetrable tile, which removes every crossing conformation. */
    ABSORBING,

    /** Neumann — the mid-plane of two identical brushes, carried only as a sensitivity. */
    REFLECTING
}

/**
 * The discretisation of one SCF solve, and the tolerances it is held to.
 *
 * Every field here is a convergence knob and every one of them is swept in
 * `SelfConsistentFieldTest`, because gate 4 is not discharged by asserting that a solver
 * converged — it is discharged by exhibiting the order at which it does.
 *
 * @param nodeSpacing the target `Δz` in nm. The actual spacing is `h/M` with
 *          `M = round(h/Δz)`, so that a wall height is represented exactly.
 * @param contourStepsPerMonomer how many contour steps each monomer is resolved with,
 *          i.e. `Δn = 1/this`.
 * @param tolerance the largest change in the field `w` (in `k_BT`) between successive iterations
 *          at which self-consistency is declared.
 * @param maximumIterations the cap. Reaching it is reported through [ScfProfile.converged]
 *          rather than swallowed — `CLAUDE.md` records the failure mode where it is not.
 * @param mixing the simple-mixing fraction of `w ← (1−λ)w + λ w_new`. The measured optimum for
 *          this material is near 0.5; above ~0.7 the iteration rings and takes longer.
 * @param maximumDiffusionRatio the largest `r = D Δn/(2Δz²)` a contour step may run at; the
 *          contour step count is raised until it holds. This is the one knob that is **not**
 *          free. The grafted source is a delta and the free-end source is a step, and
 *          Crank-Nicolson rings on both, into negative densities, once `r` exceeds about one.
 *          The textbook cure — a Rannacher start-up, backward Euler for the first steps —
 *          **cannot be used here**: the density normalisation rests on the identity
 *          `∫q(n)q†(N−n)dz = Q` holding for every `n`, and that identity requires every contour
 *          step to apply the *same* operator. Damping only the first steps of both sweeps breaks
 *          it, and breaks it silently — the coverage stops being conserved. Bounding `r` instead
 *          removes the ringing (`|g| ≤ 1/3` per step at `r = 0.5`) with one operator throughout.
 * @param minimumNodes the fewest spatial nodes a solve may use, so that a strongly compressed
 *          layer cannot silently degrade into a three-point stencil.
 */
@Serializable
data class ScfDiscretisation(
    val nodeSpacing: Double = 0.2,
    val contourStepsPerMonomer: Double = 2.0,
    val tolerance: Double = 1e-11,
    val maximumIterations: Int = 8000,
    val mixing: Double = 0.5,
    val maximumDiffusionRatio: Double = 0.5,
    val minimumNodes: Int = 24
) {

    init {
        require(nodeSpacing > 0.0) { "nodeSpacing must be positive, was: $nodeSpacing" }
        require(contourStepsPerMonomer > 0.0) {
            "contourStepsPerMonomer must be positive, was: $contourStepsPerMonomer"
        }
        require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
        require(maximumIterations > 0) {
            "maximumIterations must be positive, was: $maximumIterations"
        }
        require(mixing > 0.0 && mixing <= 1.0) { "mixing must be within (0, 1], was: $mixing" }
        require(maximumDiffusionRatio > 0.0) {
            "maximumDiffusionRatio must be positive, was: $maximumDiffusionRatio"
        }
        require(minimumNodes >= 4) { "minimumNodes must be at least 4, was: $minimumNodes" }
    }

}

/**
 * A converged self-consistent-field solution for one grafted layer against a wall at one height.
 *
 * Node `i` sits at `z = (i+1)Δz`. Under [ScfWallCondition.ABSORBING] the last node is one
 * spacing *below* the wall and the volume fraction at the wall itself is exactly zero; under
 * [ScfWallCondition.REFLECTING] the last node **is** the wall.
 */
class ScfProfile internal constructor(

    /** The wall height `h` in nm this solution was obtained at. */
    val height: Double,

    /** The actual node spacing `Δz = h/M` in nm. */
    val nodeSpacing: Double,

    /** The condition the propagator obeyed at the wall. */
    val wallCondition: ScfWallCondition,

    /** `φ(z)` at the nodes — the physical volume fraction, resolved in `z`. */
    val volumeFraction: F64Array,

    /** `w(z) = μ(φ(z))/k_BT` at the nodes — dimensionless, per monomer. */
    val field: F64Array,

    /** `ln Q`, the single-chain partition function of a grafted chain in that field. */
    val logPartitionFunction: Double,

    /** `F/A` in `pN/nm` — `−σ k_BT lnQ − ∫Π_int(φ) dz`. */
    val freeEnergyPerArea: Double,

    /** `∫φ dz/v₀` in monomers per nm², which must equal `N σ`. */
    val coverage: Double,

    /** The wall pressure in `pN/nm²` from the contact-value route, independent of `−∂F/∂h`. */
    val contactPressure: Double,

    /** `∫q(z,n)q†(z,N−n)dz` sampled at three contour splits — all must equal `Q`. */
    val partitionFunctionAtSplit: DoubleArray,

    /** How many self-consistency iterations were taken. */
    val iterations: Int,

    /** Whether [ScfDiscretisation.tolerance] was reached before the cap. */
    val converged: Boolean,

    /** The final `max|w_new − w|` in `k_BT`. */
    val residual: Double
) {

    /** How many spatial nodes the profile carries. */
    val nodes: Int get() = volumeFraction.length

    /** `φ(h)` — exactly zero for an absorbing wall, finite for a reflecting one. */
    val wallVolumeFraction: Double
        get() = if (wallCondition == ScfWallCondition.REFLECTING) volumeFraction[nodes - 1]
        else 0.0

    /** `2⟨z⟩` in nm — the first-moment thickness, which is exactly `L` for a box profile. */
    val firstMomentHeight: Double
        get() {
            var moment = 0.0
            var total = 0.0
            for (i in 0 until nodes) {
                val weight = quadratureWeight(i)
                moment += weight * (i + 1) * nodeSpacing * volumeFraction[i]
                total += weight * volumeFraction[i]
            }
            return 2.0 * moment / total
        }

    /** The largest volume fraction anywhere in the profile. */
    val peakVolumeFraction: Double get() = volumeFraction.max()

    /** `φ(z)` by linear interpolation between nodes; zero outside `(0, h)`. */
    fun volumeFractionAt(z: Double): Double {
        if (z <= 0.0 || z >= height) return 0.0
        val position = z / nodeSpacing - 1.0
        if (position <= 0.0) return volumeFraction[0] * (z / nodeSpacing)
        val lower = position.toInt()
        if (lower >= nodes - 1) {
            val last = volumeFraction[nodes - 1]
            return if (wallCondition == ScfWallCondition.REFLECTING) last
            else last * (height - z) / nodeSpacing
        }
        val fraction = position - lower
        return volumeFraction[lower] * (1.0 - fraction) + volumeFraction[lower + 1] * fraction
    }

    internal fun quadratureWeight(node: Int): Double =
        if (wallCondition == ScfWallCondition.REFLECTING && node == nodes - 1) 0.5 else 1.0

}

/**
 * The grafted layer whose density profile is solved rather than assumed.
 *
 * It implements the same [GraftedLayerModel] contract as `T-1c`'s [AlexanderBoxLayer] and
 * [StrongStretchingLayer], so that the extension functions those models are consumed through —
 * [load], [stiffness], [heightUnderLoad], [chainLengthForHeight] — apply here unchanged and the
 * three answers are like for like.
 *
 * @param interaction the interaction free energy, from `C-0003`'s measurement-anchored bracket.
 * @param discretisation the grid and the tolerances.
 * @param restingPressure the disjoining pressure in `pN/nm²` at which the layer is declared to be
 *          at its resting height. **This is a definition, not a result**: an SCF layer's outer
 *          edge is a real decaying tail, so `P = 0` is reached only asymptotically and `L₀` does
 *          not exist without a threshold. The default is 1 pN over the §3 40 × 40 nm tile.
 * @param wallCondition see [ScfWallCondition]; absorbing unless a sensitivity is being run.
 */
class SelfConsistentFieldLayer(
    override val interaction: InteractionFreeEnergy,
    val discretisation: ScfDiscretisation = ScfDiscretisation(),
    val restingPressure: Double = 1.0 / 1600.0,
    val wallCondition: ScfWallCondition = ScfWallCondition.ABSORBING
) : GraftedLayerModel {

    init {
        require(restingPressure > 0.0) {
            "restingPressure must be positive, was: $restingPressure"
        }
    }

    override val name: String
        get() = "scf-${wallCondition.name.lowercase()}(${interaction.name})"

    private val cache = LinkedHashMap<ScfKey, ScfProfile>(CACHE_ENTRIES, 0.75f, true)

    private var warmField: DoubleArray? = null

    private var restingHeightChain: GraftedChain? = null

    private var restingHeight: Double = 0.0

    /** The converged profile of [chain] against a wall at [height] nm. */
    fun profile(chain: GraftedChain, height: Double): ScfProfile {
        require(height > 0.0) { "height must be positive, was: $height" }
        val layers = layerCount(height)
        return solve(chain, layers, height / layers)
    }

    /**
     * `P(h)` in `pN/nm²` from `−∂F/∂h`, with the derivative taken by moving the wall exactly one
     * node layer either way so that both solves share **one** node spacing.
     *
     * That last point is not a detail. The free energy carries an additive constant from the
     * normalisation of the grafted source, and that constant depends on the node spacing; a
     * derivative taken across two different spacings measures the constant rather than the layer.
     */
    fun pressureAt(chain: GraftedChain, height: Double): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        val layers = layerCount(height)
        val spacing = height / layers
        val below = solve(chain, layers - 1, spacing).freeEnergyPerArea
        val above = solve(chain, layers + 1, spacing).freeEnergyPerArea
        return -(above - below) / (2.0 * spacing)
    }

    /** `k/A = ∂²F/∂h²` in `pN/nm³`, on the same three-rung ladder [pressureAt] uses. */
    fun stiffnessPerAreaAt(chain: GraftedChain, height: Double): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        val layers = layerCount(height)
        val spacing = height / layers
        val below = solve(chain, layers - 1, spacing).freeEnergyPerArea
        val here = solve(chain, layers, spacing).freeEnergyPerArea
        val above = solve(chain, layers + 1, spacing).freeEnergyPerArea
        return (above - 2.0 * here + below) / (spacing * spacing)
    }

    /**
     * `F/A` in `pN/nm` at a wall height of [height] nm, forced onto a node spacing of exactly
     * [spacing] nm so that a whole ladder of heights is comparable to the last digit.
     */
    fun freeEnergyPerAreaOnGrid(
        chain: GraftedChain,
        height: Double,
        spacing: Double
    ): Double {
        require(spacing > 0.0) { "spacing must be positive, was: $spacing" }
        val layers = (height / spacing).roundToInt()
        require(layers >= 4) { "height must span at least four layers, was: $layers" }
        return solve(chain, layers, spacing).freeEnergyPerArea
    }

    /**
     * The height in nm at which the layer's disjoining pressure is [pressure] `pN/nm²`.
     *
     * Both ends of the bracket are searched for rather than assumed: the ceiling is grown until
     * the pressure has fallen through the target, because how far an SCF layer reaches is not
     * known in advance — which is the whole point of solving for the profile instead of assuming
     * one — and the floor is lowered until it is above it, stopping at [saturationHeight] where
     * the mean volume fraction would be [SATURATION_FRACTION].
     */
    fun heightAtPressure(chain: GraftedChain, pressure: Double): Double {
        require(pressure > 0.0) { "pressure must be positive, was: $pressure" }
        val limit = saturationHeight(chain)
        var floor = max(limit, FLOOR_SEED * chain.idealEndToEnd)
        var guard = 0
        while (!(resolvedPressure(chain, floor) > pressure)) {
            floor *= FLOOR_SHRINK
            require(floor > limit && guard++ < HEIGHT_BRACKET_STEPS) {
                "the layer cannot reach $pressure pN/nm^2 above its saturation height $limit nm"
            }
        }
        var ceiling = max(4.0 * chain.idealEndToEnd, 4.0 * floor)
        guard = 0
        while (resolvedPressure(chain, ceiling) > pressure) {
            ceiling *= 1.6
            require(guard++ < HEIGHT_BRACKET_STEPS) {
                "the layer pressure did not fall through $pressure pN/nm^2 below $ceiling nm"
            }
        }
        // in log-log, because P(h) falls through five decades over the bracket and a root finder
        // working on the raw values spends most of its evaluations in the flat tail
        return exp(
            bracketedRoot(
                ln(floor), ln(ceiling), tolerance = HEIGHT_TOLERANCE, iterations = 60
            ) { logHeight -> ln(resolvedPressure(chain, exp(logHeight)) / pressure) }
        )
    }

    /**
     * [pressureAt], floored at a pressure no design question can care about.
     *
     * `P(h)` is a difference of two free energies, so far out in the layer's tail it is
     * roundoff — and roundoff has no sign. The floor keeps the root finders monotone there
     * instead of handing them a negative number to take a logarithm of. It sits ten orders of
     * magnitude below one piconewton over the tile, so nothing in the design space touches it.
     */
    private fun resolvedPressure(chain: GraftedChain, height: Double): Double {
        val pressure = pressureAt(chain, height)
        return if (pressure.isFinite() && pressure > PRESSURE_FLOOR) pressure else PRESSURE_FLOOR
    }

    /**
     * The chain length whose resting height is [height] nm at [graftingDensity].
     *
     * The generic [chainLengthForHeight] iterates the fixed point `N ← N L₀_target/L₀(N)`, which
     * is exact in one pass when `L₀ ∝ N` — as it is for both of `T-1c`'s profile models and any
     * pure power law. **It is not, here.** The SCF resting height goes as roughly `N^0.55` at
     * Gen-1 grafting densities, so that fixed point contracts by only a factor of two per pass
     * and never reaches the `1e−15` it is asked for. Bracketing in `ln N` against `ln L₀` does,
     * in a handful of evaluations, because that relation is very nearly a straight line.
     */
    fun chainLengthAtRestingHeight(
        peg: PegWater,
        height: Double,
        graftingDensity: Double
    ): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        require(graftingDensity > 0.0) {
            "graftingDensity must be positive, was: $graftingDensity"
        }
        fun restingHeight(length: Double): Double =
            equilibriumHeight(peg.graftedChain(length, graftingDensity))
        var low = CHAIN_LENGTH_SEED
        var high = CHAIN_LENGTH_SEED
        var guard = 0
        while (restingHeight(low) > height) {
            low /= CHAIN_LENGTH_STEP
            require(low >= 1.0 && guard++ < CHAIN_LENGTH_BRACKET_STEPS) {
                "no chain short enough reaches a resting height of $height nm"
            }
        }
        guard = 0
        while (restingHeight(high) < height) {
            high *= CHAIN_LENGTH_STEP
            require(guard++ < CHAIN_LENGTH_BRACKET_STEPS) {
                "no chain long enough reaches a resting height of $height nm"
            }
        }
        if (high == low) return low
        return exp(
            bracketedRoot(
                ln(low), ln(high), tolerance = HEIGHT_TOLERANCE, iterations = 60
            ) { logLength -> ln(restingHeight(exp(logLength)) / height) }
        )
    }

    /** `N σ v₀ / φ_max` in nm — the height at which the layer would be a melt on average. */
    fun saturationHeight(chain: GraftedChain): Double =
        chain.occupiedThickness / SATURATION_FRACTION

    /**
     * The resting height, **defined** as the height at which the layer carries [restingPressure].
     *
     * `T-1c`'s models both have a sharp `L₀` because their profiles are trial functions that
     * terminate. This one does not, and reporting a threshold-defined height with the threshold
     * attached is the honest version of the same quantity.
     */
    override fun equilibriumHeight(chain: GraftedChain): Double {
        if (chain != restingHeightChain) {
            restingHeight = heightAtPressure(chain, restingPressure)
            restingHeightChain = chain
        }
        return restingHeight
    }

    override fun disjoiningPressure(chain: GraftedChain, height: Double): Double {
        requireWithinLayer(chain, height)
        return pressureAt(chain, height)
    }

    override fun stiffnessPerArea(chain: GraftedChain, height: Double): Double {
        requireWithinLayer(chain, height)
        return stiffnessPerAreaAt(chain, height)
    }

    override fun freeEnergyPerArea(chain: GraftedChain, height: Double): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        return profile(chain, height).freeEnergyPerArea
    }

    private fun requireWithinLayer(chain: GraftedChain, height: Double) {
        val resting = equilibriumHeight(chain)
        val floor = saturationHeight(chain)
        require(height > floor && height <= resting * (1.0 + 1e-12)) {
            "height must be within ($floor, $resting], was: $height"
        }
    }

    private fun layerCount(height: Double): Int = max(
        (height / discretisation.nodeSpacing).roundToInt(), discretisation.minimumNodes
    )

    private fun solve(chain: GraftedChain, layers: Int, spacing: Double): ScfProfile {
        val key = ScfKey(chain, layers, spacing)
        cache[key]?.let { return it }
        val solution = solveScf(
            chain, interaction, layers, spacing, wallCondition, discretisation, warmField
        )
        warmField = solution.field.toDoubleArray()
        if (cache.size >= CACHE_ENTRIES) {
            cache.remove(cache.keys.first())
        }
        cache[key] = solution
        return solution
    }

}

/**
 * The largest mean volume fraction a solve is taken to.
 *
 * Below the corresponding height the layer is a melt on average, `f_int(φ)` is being evaluated
 * an order of magnitude outside the 0–50 wt % range `C-0002`'s equation of state was fitted over,
 * and the incompressibility a real polymer would enforce is absent from this free energy
 * altogether. Nothing in the Gen-1 design space comes near it — the working volume fractions are
 * 0.02–0.10 — so this is a guard rail on the root finders, not a physical statement.
 */
private const val SATURATION_FRACTION = 0.8

/** Where the lower bracket of [SelfConsistentFieldLayer.heightAtPressure] starts, in `R₀`. */
private const val FLOOR_SEED = 0.35

private const val FLOOR_SHRINK = 0.7

private const val HEIGHT_BRACKET_STEPS = 40

/** A chain length in the middle of the Gen-1 design space, used to seed the length bracket. */
private const val CHAIN_LENGTH_SEED = 120.0

private const val CHAIN_LENGTH_STEP = 3.0

private const val CHAIN_LENGTH_BRACKET_STEPS = 12

/** The floor the adaptive damping backs off to before giving up on self-consistency. */
private const val MINIMUM_MIXING = 1e-4

/**
 * The relative width at which the resting-height and working-height brackets are closed.
 *
 * Every evaluation costs two SCF solves, so a `1e−15` bracket — which the analytic models of
 * `T-1c` can afford — would spend eighty of them to sharpen a height by fifteen digits that the
 * ±15 % on `A₂` knows to one.
 */
private const val HEIGHT_TOLERANCE = 1e-6

private const val CACHE_ENTRIES = 128

/** The pressure in `pN/nm²` below which the tail of `P(h)` is roundoff rather than physics. */
private const val PRESSURE_FLOOR = 1e-14

private data class ScfKey(
    val chain: GraftedChain,
    val layers: Int,
    val spacing: Double
)

/**
 * Returns `b²/(6 n_K)` in `nm²` per monomer — the diffusion coefficient of the Edwards equation
 * when the contour is counted in **monomers** and the elasticity is Gaussian on the measured
 * Kuhn parameters, so that `⟨R²⟩ = 6 D N = N_K b²` exactly.
 */
val GraftedChain.contourDiffusion: Double
    get() = kuhnLength * kuhnLength / (6.0 * monomersPerKuhnSegment)

/**
 * Contour steps, rounded up to an even number so that Simpson's rule applies, and raised until
 * the Crank-Nicolson diffusion number `r = D Δn/(2Δz²)` is within
 * [ScfDiscretisation.maximumDiffusionRatio].
 *
 * The raise is not cosmetic: above `r ≈ 1` the scheme rings on the singular grafted source hard
 * enough to make the volume fraction negative next to it.
 */
internal fun contourSteps(
    chain: GraftedChain,
    spacing: Double,
    discretisation: ScfDiscretisation
): Int {
    val requested = chain.monomersPerChain * discretisation.contourStepsPerMonomer
    val stable = chain.monomersPerChain * chain.contourDiffusion /
            (2.0 * spacing * spacing * discretisation.maximumDiffusionRatio)
    val raw = ceil(max(requested, stable)).toInt()
    val even = if (raw % 2 == 0) raw else raw + 1
    return max(even, MINIMUM_CONTOUR_STEPS)
}

private const val MINIMUM_CONTOUR_STEPS = 16

private const val SPLIT_SAMPLES = 3

/**
 * Solves the self-consistent field for [chain] confined by a wall [layers] node spacings above
 * the grafting plane, warm-starting from [initialField] where one is supplied.
 *
 * The contour integration is Strang-split — half a step of the field, one Crank-Nicolson
 * diffusion step, half a step of the field — which is second order in `Δn`, and the first
 * [ScfDiscretisation.rannacherSteps] steps are taken as pairs of backward-Euler half steps to
 * damp the two singular initial conditions. The tridiagonal sweep is the one place in this file
 * written as an explicit indexed loop rather than through `viktor`: the Thomas algorithm is
 * inherently sequential and has no vector form, while the field exponential, the density
 * accumulation, the quadratures and the residual are all `F64Array` expressions.
 */
internal fun solveScf(
    chain: GraftedChain,
    interaction: InteractionFreeEnergy,
    layers: Int,
    spacing: Double,
    wallCondition: ScfWallCondition,
    discretisation: ScfDiscretisation,
    initialField: DoubleArray?
): ScfProfile {
    require(layers >= 4) { "layers must be at least 4, was: $layers" }
    val nodes = if (wallCondition == ScfWallCondition.REFLECTING) layers else layers - 1
    val steps = contourSteps(chain, spacing, discretisation)
    val solver = ScfSolver(
        chain, interaction, layers, spacing, nodes, steps, wallCondition, discretisation
    )
    return solver.run(initialField)
}

/**
 * One SCF solve, with every buffer and every `viktor` view allocated once.
 *
 * The views matter: a contour sweep touches four vectors per step and there are `steps` of them
 * per propagation, per iteration, so creating an `F64Array` view inside the loop would allocate
 * billions of wrappers over a sweep. They are therefore hoisted here and the loops below reuse
 * them.
 */
private class ScfSolver(
    private val chain: GraftedChain,
    private val interaction: InteractionFreeEnergy,
    layers: Int,
    private val spacing: Double,
    private val nodes: Int,
    private val steps: Int,
    private val wallCondition: ScfWallCondition,
    private val discretisation: ScfDiscretisation
) {

    private val height = layers * spacing

    private val contourStep = chain.monomersPerChain / steps

    private val diffusion = chain.contourDiffusion

    private val thermal = thermalEnergy(interaction.temperature)

    private val crankNicolson = Tridiagonal(
        nodes, diffusion * contourStep / (2.0 * spacing * spacing), wallCondition
    )

    private val field = DoubleArray(nodes)
    private val nextField = DoubleArray(nodes)
    private val half = DoubleArray(nodes)
    private val scratch = DoubleArray(nodes)
    private val forward = DoubleArray(nodes)
    private val density = DoubleArray(nodes)
    private val volumeFraction = DoubleArray(nodes)
    private val stored = DoubleArray((steps + 1) * nodes)
    private val storedLogScale = DoubleArray(steps + 1)
    private val grafted = DoubleArray((steps + 1) * nodes)
    private val graftedLogScale = DoubleArray(steps + 1)

    private val halfView = half.asF64Array()
    private val forwardView = forward.asF64Array()
    private val densityView = density.asF64Array()
    private val fieldView = field.asF64Array()
    private val nextFieldView = nextField.asF64Array()
    private val scratchView = scratch.asF64Array()
    private val storedHead = stored.asF64Array(0, nodes)

    /**
     * The grafted source: a delta at the first node with amplitude `1/Δz²`.
     *
     * The amplitude is what makes the free energy comparable across node spacings. A `1/Δz`
     * source gives `q → Δz ∂G/∂z′`, which puts an explicit `ln Δz` into `ln Q`; `1/Δz²` gives
     * `q → ∂G/∂z′` with an `O(Δz²)` error and no logarithm at all.
     */
    private val sourceAmplitude = 1.0 / (spacing * spacing)

    fun run(initialField: DoubleArray?): ScfProfile {
        resampleInto(initialField, field)
        var iterations = 0
        var converged = false
        var residual = Double.MAX_VALUE
        var mixing = discretisation.mixing
        var previousResidual = Double.MAX_VALUE
        while (iterations < discretisation.maximumIterations) {
            sweep(collectSplits = false)
            for (i in 0 until nodes) {
                nextField[i] = interaction.exchangeChemicalPotential(
                    volumeFraction[i].coerceIn(0.0, 1.0)
                ) / thermal
            }
            nextFieldView.copyTo(scratchView)
            scratchView -= fieldView
            residual = scratchView.max().coerceAtLeast(-scratchView.min())
            iterations++
            if (residual <= discretisation.tolerance) {
                nextField.copyInto(field)
                converged = true
                break
            }
            // Adaptive damping. Simple mixing at a fixed fraction converges for a dilute layer
            // and DIVERGES for a dense one — the Gen-1 design space needs 0.5 and the
            // strong-stretching cross-check at phi = 0.29 needs two orders of magnitude less.
            // Backing off whenever the residual grows, and creeping back up when it falls, makes
            // one solver serve both without the caller having to know which regime it is in.
            mixing = if (residual > previousResidual) max(0.5 * mixing, MINIMUM_MIXING)
            else min(1.05 * mixing, discretisation.mixing)
            previousResidual = residual
            fieldView *= 1.0 - mixing
            nextFieldView *= mixing
            fieldView += nextFieldView
        }
        // one last pass on the converged field, so that every reported quantity comes from ONE
        // field rather than from the field of one iteration and the density of the next
        val splits = sweep(collectSplits = true)
        var coverage = 0.0
        var pressureIntegral = 0.0
        for (i in 0 until nodes) {
            val weight = quadratureWeight(i)
            val value = volumeFraction[i].coerceIn(0.0, 1.0)
            coverage += weight * value
            pressureIntegral += weight * interaction.osmoticPressure(value)
        }
        coverage *= spacing / interaction.monomerVolume
        pressureIntegral *= spacing
        return ScfProfile(
            height = height,
            nodeSpacing = spacing,
            wallCondition = wallCondition,
            volumeFraction = volumeFraction.copyOf().asF64Array(),
            field = field.copyOf().asF64Array(),
            logPartitionFunction = logPartition,
            freeEnergyPerArea = -chain.graftingDensity * thermal * logPartition -
                    pressureIntegral,
            coverage = coverage,
            contactPressure = contactPressure(),
            partitionFunctionAtSplit = splits,
            iterations = iterations,
            converged = converged,
            residual = residual
        )
    }

    private var logPartition = 0.0

    /**
     * One full pass: both propagators, the density, and `ln Q`.
     *
     * Every contour step is renormalised to a peak of one and the logarithm of the factor is
     * carried alongside. Without it a layer squeezed to a fraction of its coil size underflows
     * the propagator to exactly zero — `q ~ exp(−D π² N/h²)` reaches `1e−390` at `h = R₀/8` —
     * and the free energy silently becomes `−∞` rather than large. The scales cancel out of the
     * density through `∫q(n)q†(N−n)dz = Q`, which is what [partitionFunctionAtSplit] verifies.
     */
    private fun sweep(collectSplits: Boolean): DoubleArray {
        exponentials()
        storedHead.fill(1.0)
        stored.copyInto(scratch, 0, 0, nodes)
        storedLogScale[0] = 0.0
        for (step in 0 until steps) {
            advance(scratch, scratchView)
            storedLogScale[step + 1] = storedLogScale[step] + renormalise(scratch, scratchView)
            scratch.copyInto(stored, (step + 1) * nodes, 0, nodes)
        }
        forwardView.fill(0.0)
        forward[0] = sourceAmplitude
        forward.copyInto(grafted, 0, 0, nodes)
        graftedLogScale[0] = 0.0
        for (step in 0 until steps) {
            advance(forward, forwardView)
            graftedLogScale[step + 1] = graftedLogScale[step] + renormalise(forward, forwardView)
            forward.copyInto(grafted, (step + 1) * nodes, 0, nodes)
        }
        logPartition = graftedLogScale[steps] + ln(quadrature(forward))
        // The density is accumulated in a SECOND pass, against `ln Q` as the reference. Doing it
        // in the first pass would need a reference fixed before `ln Q` is known, and the only
        // candidate — the free-end scale — differs from `ln Q` by an amount that is O(1) for a
        // dilute layer and hundreds of nats for a dense one, where its exponential overflows and
        // the whole profile silently becomes NaN.
        densityView.fill(0.0)
        val splits = DoubleArray(SPLIT_SAMPLES)
        for (step in 0..steps) {
            accumulate(step)
            if (collectSplits) {
                val sample = splitSample(step)
                if (sample >= 0) splits[sample] = overlapAtSplit(step)
            }
        }
        densityView *= chain.graftingDensity * interaction.monomerVolume
        density.copyInto(volumeFraction)
        return splits
    }

    /** `∫q(n)q†(N−n)dz / Q` at contour index [step] — one, for every split, exactly. */
    private fun overlapAtSplit(step: Int): Double {
        val weight = exp(graftedLogScale[step] + storedLogScale[steps - step] - logPartition)
        val graftedOffset = step * nodes
        val storedOffset = (steps - step) * nodes
        var total = 0.0
        for (i in 0 until nodes) {
            total += quadratureWeight(i) * grafted[graftedOffset + i] * stored[storedOffset + i]
        }
        return total * spacing * weight
    }

    /** Scales [values] to a peak of one, returning the logarithm of the factor removed. */
    private fun renormalise(values: DoubleArray, view: F64Array): Double {
        val peak = view.max()
        if (!(peak > 0.0) || !peak.isFinite()) return 0.0
        view /= peak
        return ln(peak)
    }

    private fun exponentials() {
        fieldView.copyTo(halfView)
        halfView *= -0.5 * contourStep
        halfView.expInPlace()
    }

    /**
     * One contour step, in place: half a step of the field, one Crank-Nicolson diffusion step,
     * half a step of the field.
     *
     * The **same** operator at every step, which is what makes `∫q(n)q†(N−n)dz` independent of
     * `n` and therefore what makes the grafted coverage conserved exactly rather than nearly.
     */
    private fun advance(values: DoubleArray, view: F64Array) {
        view *= halfView
        crankNicolson.explicitInPlace(values)
        crankNicolson.solveInPlace(values)
        view *= halfView
    }

    /**
     * Adds the Simpson-weighted `q q†/Q` contribution of one contour index to the density.
     *
     * The weight is `exp(S_n + T_{N−n} − lnQ)`, and it is bounded for every `n` **because** the
     * propagator identity `∫q(n)q†(N−n)dz = Q` holds: it is the reciprocal of a renormalised
     * overlap integral, which is of order `1/h` whatever the chain length or the layer density.
     */
    private fun accumulate(step: Int) {
        val simpson = when {
            step == 0 || step == steps -> 1.0
            step % 2 == 1 -> 4.0
            else -> 2.0
        } * contourStep / 3.0 *
                exp(graftedLogScale[step] + storedLogScale[steps - step] - logPartition)
        val graftedOffset = step * nodes
        val storedOffset = (steps - step) * nodes
        for (i in 0 until nodes) {
            density[i] += simpson * grafted[graftedOffset + i] * stored[storedOffset + i]
        }
    }

    private fun quadratureWeight(node: Int): Double =
        if (wallCondition == ScfWallCondition.REFLECTING && node == nodes - 1) 0.5 else 1.0

    private fun quadrature(values: DoubleArray): Double {
        var total = 0.0
        for (i in 0 until nodes) total += quadratureWeight(i) * values[i]
        return total * spacing
    }

    private fun splitSample(step: Int): Int = when (step) {
        steps / 4 -> 0
        steps / 2 -> 1
        (3 * steps) / 4 -> 2
        else -> -1
    }

    /**
     * The contact-value pressure, the route independent of `−∂F/∂h`.
     *
     * For an absorbing wall the volume fraction vanishes as `(h−z)²` and the whole normal stress
     * there is conformational: `P = k_BT D lim φ/(v₀(h−z)²)`, taken from the two nodes nearest
     * the wall with the leading `O(h−z)` correction eliminated. For a reflecting wall nothing is
     * lost, the density at the wall is finite, and the pressure is the classical `Π_int(φ(h))`
     * that `T-1c` used.
     */
    private fun contactPressure(): Double =
        if (wallCondition == ScfWallCondition.REFLECTING) {
            interaction.osmoticPressure(volumeFraction[nodes - 1].coerceIn(0.0, 1.0))
        } else {
            val near = volumeFraction[nodes - 1] / (spacing * spacing)
            val far = volumeFraction[nodes - 2] / (4.0 * spacing * spacing)
            thermal * diffusion * (2.0 * near - far) / interaction.monomerVolume
        }

    /** Linearly resamples a converged field from a previous solve onto this grid. */
    private fun resampleInto(source: DoubleArray?, target: DoubleArray) {
        target.fill(0.0)
        if (source == null || source.size < 2) return
        if (source.size == target.size) {
            source.copyInto(target)
            return
        }
        val ratio = (source.size - 1).toDouble() / (target.size - 1).toDouble()
        for (i in target.indices) {
            val position = i * ratio
            val lower = position.toInt().coerceAtMost(source.size - 2)
            val fraction = position - lower
            target[i] = source[lower] * (1.0 - fraction) + source[lower + 1] * fraction
        }
    }

}

/**
 * A constant-coefficient tridiagonal operator `I ± r L` and its Thomas factorisation.
 *
 * `L` is the second-difference Laplacian with a Dirichlet node below the first row and, at the
 * last row, either a Dirichlet node above ([ScfWallCondition.ABSORBING]) or a mirror image
 * ([ScfWallCondition.REFLECTING], whose ghost node doubles the last off-diagonal).
 */
private class Tridiagonal(
    private val nodes: Int,
    private val ratio: Double,
    private val wallCondition: ScfWallCondition
) {

    private val gamma = DoubleArray(nodes)

    private val upper = DoubleArray(nodes)

    private val work = DoubleArray(nodes)

    private val mirrored = wallCondition == ScfWallCondition.REFLECTING

    init {
        val diagonal = 1.0 + 2.0 * ratio
        gamma[0] = 1.0 / diagonal
        upper[0] = -ratio * gamma[0]
        for (i in 1 until nodes) {
            val lower = if (i == nodes - 1 && mirrored) -2.0 * ratio else -ratio
            gamma[i] = 1.0 / (diagonal - lower * upper[i - 1])
            upper[i] = if (i == nodes - 1) 0.0 else -ratio * gamma[i]
        }
    }

    /** Replaces `x` by `(I + r L) x`. */
    fun explicitInPlace(x: DoubleArray) {
        val centre = 1.0 - 2.0 * ratio
        for (i in 0 until nodes) {
            var value = centre * x[i]
            if (i > 0) value += ratio * x[i - 1]
            if (i < nodes - 1) value += ratio * x[i + 1]
            else if (mirrored) value += ratio * x[i - 1]
            work[i] = value
        }
        work.copyInto(x, 0, 0, nodes)
    }

    /** Solves `(I − r L) x = b` in place, by the Thomas algorithm on the stored factorisation. */
    fun solveInPlace(b: DoubleArray) {
        b[0] *= gamma[0]
        for (i in 1 until nodes) {
            val lower = if (i == nodes - 1 && mirrored) -2.0 * ratio else -ratio
            b[i] = (b[i] - lower * b[i - 1]) * gamma[i]
        }
        for (i in nodes - 2 downTo 0) {
            b[i] -= upper[i] * b[i + 1]
        }
    }

}
