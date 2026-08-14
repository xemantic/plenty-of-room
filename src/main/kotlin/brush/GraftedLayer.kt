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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A grafted chain, described by the quantities `C-0002` actually measured rather than by a single
 * "monomer size" standing in for three different lengths.
 *
 * The elasticity here is Gaussian on the **Kuhn** scale, `b = 1.1 nm` and `n_K = 3.11`, and that
 * choice is not free. `C-0002`'s own numbers say the chain contains far less than one thermal blob
 * ([PegWater.thermalBlobKuhnSegments]), so it is not swollen and Gaussian statistics on the Kuhn
 * scale are the correct ones — which is also what removes the blob construction, and with it the
 * semidilute premise, from underneath the height relation.
 *
 * @param monomersPerChain `N`, continuous because chain length is inverted from the layer height.
 * @param graftingDensity `σ` in `nm⁻²`.
 * @param monomerVolume `v₀` in nm³ — what a volume fraction is measured against.
 * @param kuhnLength `b` in nm — what chain elasticity is measured against.
 * @param monomersPerKuhnSegment `n_K` — the conversion between the two.
 */
@Serializable
data class GraftedChain(
    val monomersPerChain: Double,
    val graftingDensity: Double,
    val monomerVolume: Double,
    val kuhnLength: Double,
    val monomersPerKuhnSegment: Double
) {

    init {
        require(monomersPerChain >= 1.0) {
            "monomersPerChain must be at least 1, was: $monomersPerChain"
        }
        require(graftingDensity > 0.0) {
            "graftingDensity must be positive, was: $graftingDensity"
        }
        require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
        require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
        require(monomersPerKuhnSegment > 0.0) {
            "monomersPerKuhnSegment must be positive, was: $monomersPerKuhnSegment"
        }
    }

    /** `N_K = N/n_K`, the number of statistical segments. */
    val kuhnSegments: Double get() = monomersPerChain / monomersPerKuhnSegment

    /** `Γ = N σ`, the grafted coverage in monomers per nm². */
    val coverage: Double get() = monomersPerChain * graftingDensity

    /**
     * `N σ v₀` in nm — the thickness the grafted polymer would occupy if it were dry.
     *
     * It is the conserved quantity of every compression here, and a hard lower bound on the
     * layer height: at `h = N σ v₀` the volume fraction is 1.
     */
    val occupiedThickness: Double get() = coverage * monomerVolume

    /** The mean grafting spacing `s = σ^(−1/2)` in nm. */
    val graftingSpacing: Double get() = 1.0 / sqrt(graftingDensity)

    /** `R₀ = b √N_K` in nm, the unperturbed end-to-end distance of the same chain. */
    val idealEndToEnd: Double get() = kuhnLength * sqrt(kuhnSegments)

    /** `φ = N σ v₀ / h`, the mean physical volume fraction when the layer is held at [height]. */
    fun meanVolumeFraction(height: Double): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        return occupiedThickness / height
    }

    /**
     * `L/R₀` — how far the layer is stretched beyond the chain's own unperturbed size.
     *
     * The premise of every model in this file is that this is large. It is not, anywhere in the
     * Gen-1 design space, and that is reported as a validity bound rather than hidden.
     */
    fun stretchingRatio(height: Double): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        return height / idealEndToEnd
    }

    /**
     * `A = 3π²k_BT n_K/(8 N² b²)` in `pN/nm`, the curvature of the strong-stretching potential,
     * **per monomer**.
     *
     * The parabola is a property of the chain elasticity and of the equal-time condition on
     * chain trajectories, not of the interactions — which is exactly why the same potential can
     * be used with an arbitrary [InteractionFreeEnergy].
     */
    fun parabolicCurvature(temperature: Double = ROOM_TEMPERATURE): Double =
        3.0 * PI * PI * thermalEnergy(temperature) * monomersPerKuhnSegment /
                (8.0 * monomersPerChain * monomersPerChain * kuhnLength * kuhnLength)

    /**
     * `3 k_BT σ n_K/(N b²)` in `pN/nm³` — the box model's elastic pull-back per unit height.
     *
     * `F_el/chain = (3/2) k_BT h²/(N_K b²)`, so the pressure it removes is `−σ dF/dh`, linear
     * in `h`, and this is its slope.
     */
    fun boxElasticCoefficient(temperature: Double = ROOM_TEMPERATURE): Double =
        3.0 * thermalEnergy(temperature) * graftingDensity * monomersPerKuhnSegment /
                (monomersPerChain * kuhnLength * kuhnLength)

}

/** Returns the [GraftedChain] of [monomersPerChain] monomers grafted at [graftingDensity]. */
fun PegWater.graftedChain(
    monomersPerChain: Double,
    graftingDensity: Double
): GraftedChain = GraftedChain(
    monomersPerChain = monomersPerChain,
    graftingDensity = graftingDensity,
    monomerVolume = monomerVolume,
    kuhnLength = kuhnLength,
    monomersPerKuhnSegment = monomersPerKuhnSegment
)

/**
 * A model of the grafted layer's mechanical response, built on an [InteractionFreeEnergy]
 * rather than on a fixed osmotic exponent.
 *
 * ## Sign conventions, restated
 *
 * `z` is normal to the electrode, positive away from it, origin at the electrode surface.
 * Chains are grafted at `z = 0`; the tile is a rigid non-adsorbing wall at height `h`;
 * compression means `h < L₀`. [disjoiningPressure] is positive when the layer pushes the tile
 * along `+z`, and [stiffnessPerArea] is `−∂P/∂h`, positive for a restoring layer.
 *
 * ## What the two implementations differ in, and what they do not
 *
 * Both minimise the *same* free energy — the same interaction term, the same Gaussian elasticity
 * on the same measured Kuhn parameters. They differ only in the family of density profiles they
 * are allowed to minimise over: a box for [AlexanderBoxLayer], the strong-stretching solution for
 * [StrongStretchingLayer]. The spread between them is therefore profile uncertainty and nothing
 * else, which is what makes it usable as an error bar.
 */
sealed interface GraftedLayerModel {

    /** Stable identifier of the model, emitted with every machine-readable result. */
    val name: String

    /** The interaction free energy this model minimises against chain elasticity. */
    val interaction: InteractionFreeEnergy

    /** `L₀` in nm — the unperturbed layer height of [chain] under this model. */
    fun equilibriumHeight(chain: GraftedChain): Double

    /**
     * The pressure in `pN/nm²` that [chain] exerts on a rigid wall held at [height] nm.
     *
     * @throws IllegalArgumentException if [height] is outside `(N σ v₀, L₀]`.
     */
    fun disjoiningPressure(chain: GraftedChain, height: Double): Double

    /**
     * `−∂P/∂h` in `pN/nm³` for [chain] against a rigid wall at [height] nm.
     *
     * @throws IllegalArgumentException if [height] is outside `(N σ v₀, L₀]`.
     */
    fun stiffnessPerArea(chain: GraftedChain, height: Double): Double

    /** The layer free energy per unit area in `pN/nm` when [chain] is held at [height] nm. */
    fun freeEnergyPerArea(chain: GraftedChain, height: Double): Double

}

/**
 * A single-entry memo for a layer's equilibrium height.
 *
 * `L₀` is a pure function of the chain, but the validity check on **every** pressure and stiffness
 * evaluation needs it, and for an interaction with no closed form it costs a bracketed root solve
 * over profile quadratures. Recomputing it per call turns an `O(n)` sweep into an `O(n²)` one —
 * measured at over 600 s for a 123-point sweep before this was added, against seconds after.
 * One entry is enough because every caller works through one chain at a time.
 */
private class EquilibriumHeightMemo {

    private var chain: GraftedChain? = null

    private var height: Double = 0.0

    fun of(candidate: GraftedChain, compute: (GraftedChain) -> Double): Double {
        if (candidate != chain) {
            height = compute(candidate)
            chain = candidate
        }
        return height
    }

}

/**
 * The Alexander box profile: uniform volume fraction across the layer, every chain end at the
 * outer surface, Gaussian elasticity on the measured Kuhn parameters.
 *
 * `F/chain = (h/σ) f_int(N σ v₀/h) + (3/2) k_BT h²/(N_K b²)`, minimised over `h`, giving
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`P(h) = Π_int(φ) − 3 k_BT σ h n_K/(N b²)`, &nbsp; `φ = N σ v₀ / h`.
 *
 * The elastic term appears in the **pressure** here, unlike in [StrongStretchingLayer], because
 * the box profile forces every chain end onto the wall with finite tension. That difference is
 * the whole of why the two models disagree about the stiffness at first contact.
 *
 * This is the **cheap bound** of `T-1c`, run before the strong-stretching calculation as §5 of the
 * problem definition requires: for a power-law interaction it is closed form throughout.
 */
@Serializable
data class AlexanderBoxLayer(
    override val interaction: InteractionFreeEnergy
) : GraftedLayerModel {

    override val name: String get() = "alexander-box(${interaction.name})"

    @Transient
    private val memo = EquilibriumHeightMemo()

    /**
     * `h^(m+1) = K (N σ v₀)^m / c_el` in closed form for a power law, and the root of `P(h) = 0`
     * otherwise.
     */
    override fun equilibriumHeight(chain: GraftedChain): Double = memo.of(chain) { candidate ->
        if (interaction is PowerLawInteraction) {
            val m = interaction.exponent
            (
                    interaction.coefficient * candidate.occupiedThickness.pow(m) /
                            candidate.boxElasticCoefficient(interaction.temperature)
                    ).pow(1.0 / (m + 1.0))
        } else equilibriumHeightByRoot(candidate)
    }

    /**
     * [equilibriumHeight] solved as the root of `P(h) = 0` rather than in closed form.
     *
     * Bisection is unconditionally convergent here because `Π_int(Nσv₀/h)` falls and the elastic
     * pull-back rises with `h`, so `P` is strictly decreasing.
     */
    fun equilibriumHeightByRoot(chain: GraftedChain): Double {
        val elastic = chain.boxElasticCoefficient(interaction.temperature)
        return bracketedRoot(
            chain.occupiedThickness, chain.occupiedThickness * HEIGHT_BRACKET_DECADES
        ) { height ->
            interaction.osmoticPressure(chain.occupiedThickness / height) - elastic * height
        }
    }

    override fun disjoiningPressure(chain: GraftedChain, height: Double): Double {
        requireWithinLayer(chain, height)
        return interaction.osmoticPressure(chain.meanVolumeFraction(height)) -
                chain.boxElasticCoefficient(interaction.temperature) * height
    }

    override fun stiffnessPerArea(chain: GraftedChain, height: Double): Double {
        requireWithinLayer(chain, height)
        val volumeFraction = chain.meanVolumeFraction(height)
        return interaction.osmoticPressureSlope(volumeFraction) * volumeFraction / height +
                chain.boxElasticCoefficient(interaction.temperature)
    }

    override fun freeEnergyPerArea(chain: GraftedChain, height: Double): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        return height * interaction.freeEnergyDensity(chain.meanVolumeFraction(height)) +
                0.5 * chain.boxElasticCoefficient(interaction.temperature) * height * height
    }

}

/**
 * The Milner-Witten-Cates strong-stretching brush, generalised to an arbitrary local
 * [InteractionFreeEnergy].
 *
 * ## The construction
 *
 * The self-consistent potential a chain feels is parabolic, `μ(φ(z)) = λ − A z²`, because that is
 * the only potential in which every chain takes the same contour length to travel from its free
 * end to the grafting plane. The parabola belongs to the **elasticity**, not to the interactions,
 * so it survives replacing the mean-field interaction with any other local one. `λ` is fixed by
 * conserving the grafted coverage `∫φ dz = N σ v₀`.
 *
 * ## The wall pressure
 *
 * `P(h) = Π_int(φ(h))` — the contact-value theorem, and here a theorem rather than a definition.
 * Normal stress must be uniform through the layer; at the wall the only chains present are those
 * whose free end is there, and a free end carries no tension, so the whole normal stress at the
 * wall is the local interaction pressure. `GraftedLayerTest` verifies it thermodynamically
 * against `−∂F/∂h`, with `F` assembled independently from the profile through the identity
 * `F_el = ∫ A z² φ(z)/v₀ dz`.
 *
 * ## Validity
 *
 * The theory is a *strong*-stretching theory and the Gen-1 layer is not strongly stretched —
 * [GraftedChain.stretchingRatio] stays below 2 across the whole design space. Under compression
 * the truncated parabola also develops the known free-end "dead zone" near the wall, which this
 * implementation does not resolve. Both are stated in the claim, not worked around.
 *
 * @param panels the number of Simpson panels used for the profile quadrature, in the `θ` variable
 *          of the substitution `z = h sinθ` that removes the outer-edge singularity.
 */
@Serializable
data class StrongStretchingLayer(
    override val interaction: InteractionFreeEnergy,
    val panels: Int = 1024
) : GraftedLayerModel {

    init {
        require(panels >= 4 && panels % 2 == 0) {
            "panels must be even and at least 4, was: $panels"
        }
    }

    override val name: String get() = "strong-stretching(${interaction.name})"

    @Transient
    private val memo = EquilibriumHeightMemo()

    /**
     * Closed form for a power-law interaction — evaluated through the Beta integral
     * `∫₀^1 (1−u²)^p du` — and [equilibriumHeightByCoverage] otherwise.
     */
    override fun equilibriumHeight(chain: GraftedChain): Double = memo.of(chain) { candidate ->
        if (interaction is PowerLawInteraction) {
            val m = interaction.exponent
            val p = 1.0 / (m - 1.0)
            val curvature = candidate.parabolicCurvature(interaction.temperature) * (m - 1.0) /
                    (interaction.monomerVolume * interaction.coefficient * m)
            (
                    candidate.occupiedThickness / (curvature.pow(p) * halfCircleMoment(p))
                    ).pow(1.0 / (2.0 * p + 1.0))
        } else equilibriumHeightByCoverage(candidate)
    }

    /**
     * [equilibriumHeight] solved from the coverage constraint at `λ = A L²` rather than in
     * closed form. Monotone in `L`, hence bisection.
     */
    fun equilibriumHeightByCoverage(chain: GraftedChain): Double {
        val curvature = chain.parabolicCurvature(interaction.temperature)
        return bracketedRoot(
            chain.occupiedThickness, chain.occupiedThickness * HEIGHT_BRACKET_DECADES
        ) { height ->
            profile(chain, height, curvature * height * height).occupied - chain.occupiedThickness
        }
    }

    /** The volume fraction at [z] nm when [chain] is compressed to [height] nm. */
    fun volumeFractionAt(chain: GraftedChain, height: Double, z: Double): Double {
        requireWithinLayer(chain, height)
        require(z in 0.0..height) { "z must be within [0.0, $height], was: $z" }
        val curvature = chain.parabolicCurvature(interaction.temperature)
        val lambda = solveLambda(chain, height)
        return interaction.volumeFractionAtChemicalPotential(lambda - curvature * z * z)
    }

    /** The volume fraction at the wall — the quantity the contact-value theorem needs. */
    fun wallVolumeFraction(chain: GraftedChain, height: Double): Double =
        volumeFractionAt(chain, height, height)

    /** `∫φ dz / v₀` in monomers per nm², which must equal `N σ` at every compression. */
    fun coverage(chain: GraftedChain, height: Double): Double {
        requireWithinLayer(chain, height)
        return profile(chain, height, solveLambda(chain, height)).occupied /
                interaction.monomerVolume
    }

    override fun disjoiningPressure(chain: GraftedChain, height: Double): Double {
        requireWithinLayer(chain, height)
        val wall = wallVolumeFractionAt(chain, height, solveLambda(chain, height))
        return interaction.osmoticPressure(wall)
    }

    /**
     * `k/A = (φ_w/v₀)(2 A h + φ_w/J)` with `J = ∫₀^h dz/μ'(φ(z))`.
     *
     * Derived by differentiating the coverage constraint for `dλ/dh`, and simplified with the
     * identity `Π'(φ)/μ'(φ) = φ/v₀`, which holds for **any** local free energy and is asserted
     * as a test rather than assumed. The form is manifestly positive and stays finite as the
     * wall density vanishes, where a naive `Π'·dφ_w/dh` would be `0 × ∞`.
     */
    override fun stiffnessPerArea(chain: GraftedChain, height: Double): Double {
        requireWithinLayer(chain, height)
        val curvature = chain.parabolicCurvature(interaction.temperature)
        val lambda = solveLambda(chain, height)
        val profile = profile(chain, height, lambda)
        val wall = interaction.volumeFractionAtChemicalPotential(
            lambda - curvature * height * height
        )
        if (wall <= 0.0) return 0.0
        return wall / interaction.monomerVolume *
                (2.0 * curvature * height + wall / profile.inverseSlope)
    }

    /**
     * `F/A = ∫₀^h [ f_int(φ(z)) + A z² φ(z)/v₀ ] dz`.
     *
     * The second term is the elastic energy: a chain whose free end is at `z₀` costs
     * `A N z₀²/2`, and summing that over the end distribution is *identical* to integrating
     * `A z²` against the segment density, because the trajectory `z(n) = z₀ cos(πn/2N)` spends
     * exactly the right contour length at each height. That identity is what makes the free
     * energy computable from the profile alone, with no end distribution to invert.
     */
    override fun freeEnergyPerArea(chain: GraftedChain, height: Double): Double {
        requireWithinLayer(chain, height)
        return profile(
            chain, height, solveLambda(chain, height), withFreeEnergy = true
        ).freeEnergy
    }

    private fun wallVolumeFractionAt(
        chain: GraftedChain,
        height: Double,
        lambda: Double
    ): Double = interaction.volumeFractionAtChemicalPotential(
        lambda - chain.parabolicCurvature(interaction.temperature) * height * height
    )

    /**
     * Returns `λ` such that the profile conserves the grafted coverage, by a safeguarded Newton
     * iteration on the bracket `[μ(Nσv₀/h), μ(Nσv₀/h) + A h²]`.
     *
     * Both ends of that bracket are proved rather than guessed: the profile never exceeds
     * `μ⁻¹(λ)` nor falls below `μ⁻¹(λ − A h²)`, so the mean volume fraction is trapped between
     * them. The Newton step uses `dΓ/dλ = ∫dz/μ'(φ)`, which the stiffness needs anyway.
     */
    private fun solveLambda(chain: GraftedChain, height: Double): Double {
        val curvature = chain.parabolicCurvature(interaction.temperature)
        val target = chain.occupiedThickness
        val mean = interaction.exchangeChemicalPotential(target / height)
        var low = mean
        var high = mean + curvature * height * height
        var lambda = 0.5 * (low + high)
        repeat(NEWTON_ITERATIONS) {
            val profile = profile(chain, height, lambda)
            val residual = profile.occupied - target
            if (abs(residual) <= CONVERGENCE * target) return lambda
            if (residual > 0.0) high = lambda else low = lambda
            val step = if (profile.inverseSlope > 0.0) {
                lambda - residual / profile.inverseSlope
            } else Double.NaN
            lambda = if (step > low && step < high) step else 0.5 * (low + high)
        }
        return lambda
    }

    /**
     * Integrates the profile once, returning everything that depends on it.
     *
     * The substitution is `z = h sinθ`, which turns the outer-edge behaviour `(L²−z²)^p` into
     * `cos^(2p+1)θ` and removes the endpoint singularity of the uncompressed profile.
     */
    private fun profile(
        chain: GraftedChain,
        height: Double,
        lambda: Double,
        withFreeEnergy: Boolean = false
    ): ProfileIntegrals {
        val curvature = chain.parabolicCurvature(interaction.temperature)
        val step = 0.5 * PI / panels
        var occupied = 0.0
        var inverseSlope = 0.0
        var freeEnergy = 0.0
        for (i in 0..panels) {
            val weight = when {
                i == 0 || i == panels -> 1.0
                i % 2 == 1 -> 4.0
                else -> 2.0
            }
            val angle = i * step
            val sine = sin(angle)
            val cosine = cos(angle)
            val z = height * sine
            val potential = lambda - curvature * z * z
            val volumeFraction = interaction.volumeFractionAtChemicalPotential(potential)
            val measure = weight * height * cosine
            if (volumeFraction > 0.0) {
                occupied += measure * volumeFraction
                val slope = interaction.exchangeChemicalPotentialSlope(volumeFraction)
                if (slope > 0.0) inverseSlope += measure / slope
                if (withFreeEnergy) {
                    freeEnergy += measure * (
                            interaction.freeEnergyDensity(volumeFraction.coerceAtMost(1.0)) +
                                    curvature * z * z * volumeFraction / interaction.monomerVolume
                            )
                }
            }
        }
        val scale = step / 3.0
        return ProfileIntegrals(
            occupied = occupied * scale,
            inverseSlope = inverseSlope * scale,
            freeEnergy = freeEnergy * scale
        )
    }

}

/** The three quadratures over a strong-stretching profile, taken in one pass. */
private data class ProfileIntegrals(
    /** `∫φ dz` in nm — the coverage constraint, in units of dry thickness. */
    val occupied: Double,
    /** `∫dz/μ'(φ)` in `nm/(pN·nm)` — `dΓ/dλ`, needed by both the λ solve and the stiffness. */
    val inverseSlope: Double,
    /** `∫[f_int + A z² φ/v₀] dz` in `pN/nm` — the layer free energy per unit area. */
    val freeEnergy: Double
)

/** The load in pN that [chain] carries when a tile of footprint [area] nm² is held at [height] nm. */
fun GraftedLayerModel.load(
    chain: GraftedChain,
    height: Double,
    area: Double
): Double {
    require(area > 0.0) { "area must be positive, was: $area" }
    return disjoiningPressure(chain, height) * area
}

/** The stiffness in `pN/nm` that a tile of footprint [area] nm² sees from [chain] at [height] nm. */
fun GraftedLayerModel.stiffness(
    chain: GraftedChain,
    height: Double,
    area: Double
): Double {
    require(area > 0.0) { "area must be positive, was: $area" }
    return stiffnessPerArea(chain, height) * area
}

/**
 * Returns the height in nm at which [chain] balances a compressive [load] in pN over [area] nm².
 *
 * Bisection, for the same reason `T-1` chose it: the pressure is strictly decreasing in the
 * height for every model here, so it is unconditionally convergent and needs no derivative,
 * and it cannot step outside the validity range near `L₀` where the pressure is flat.
 * The lower bracket is the dry thickness `N σ v₀`, below which the volume fraction would exceed 1.
 */
fun GraftedLayerModel.heightUnderLoad(
    chain: GraftedChain,
    load: Double,
    area: Double
): Double {
    require(load >= 0.0) { "load must not be tensile, was: $load" }
    require(area > 0.0) { "area must be positive, was: $area" }
    val unperturbed = equilibriumHeight(chain)
    if (load == 0.0) return unperturbed
    return bracketedRoot(chain.occupiedThickness * (1.0 + 1e-12), unperturbed) { height ->
        load - disjoiningPressure(chain, height) * area
    }
}

/**
 * Returns the chain length whose equilibrium height under this model is [height] nm at
 * [graftingDensity].
 *
 * This is the inversion `CH-0001` says cannot rest on the Alexander-de Gennes relation any more,
 * done instead against a free energy that is valid across the crossover. It is solved
 * numerically rather than in closed form so that the additive crossover interaction — for which
 * `L₀` is *not* exactly linear in `N` — is inverted by the same code as the pure limbs, for which
 * it is.
 */
fun GraftedLayerModel.chainLengthForHeight(
    peg: PegWater,
    height: Double,
    graftingDensity: Double
): Double {
    require(height > 0.0) { "height must be positive, was: $height" }
    require(graftingDensity > 0.0) { "graftingDensity must be positive, was: $graftingDensity" }
    // L0 is EXACTLY proportional to N for a power-law interaction, and very nearly so for a sum
    // of them, so the fixed point N <- N * L0_target/L0(N) lands on the answer in a single pass in
    // the first case and contracts hard in the second. Bracketing is the fallback, not the method,
    // because every evaluation of L0 can cost a profile quadrature.
    var length = CHAIN_LENGTH_SEED
    repeat(FIXED_POINT_ITERATIONS) {
        val achieved = equilibriumHeight(peg.graftedChain(length, graftingDensity))
        val next = (length * height / achieved).coerceIn(1.0, CHAIN_LENGTH_BRACKET)
        if (abs(next - length) <= CONVERGENCE * next) return next
        length = next
    }
    return bracketedRoot(1.0, CHAIN_LENGTH_BRACKET) { candidate ->
        equilibriumHeight(peg.graftedChain(candidate, graftingDensity)) - height
    }
}

/**
 * Returns the root of [f] inside the bracket `[low, high]`, by the Illinois variant of regula falsi.
 *
 * Every root solved in this file is bracketed and monotone, so bisection would work and is what
 * `T-1` used. Illinois is chosen instead because here each evaluation can cost a whole profile
 * quadrature: it keeps bisection's guarantee that the bracket is never lost — the sign change is
 * retained at every step — while converging superlinearly, which is roughly an eightfold saving
 * in evaluations. The halving of the stagnant endpoint's value is what prevents the one-sided
 * stalling that plain regula falsi suffers on convex functions.
 *
 * ## The halving is conditional, and that is the whole of the saving (`P-15`)
 *
 * An endpoint's residual is halved only when the **same** endpoint has been retained twice in a
 * row, which is what "the stagnant endpoint" means. Halving unconditionally — as this routine did
 * — deflates *both* residuals once the estimate starts alternating sides, and two deflated
 * residuals of nearly equal magnitude interpolate to the midpoint. The method silently degenerates
 * into bisection while still paying for a secant, and it is then **worse** than bisection, not
 * eight times better: 52 evaluations against bisection's ~52 for `x² − 2`, and 73 against 55 for
 * `x⁵ − 10⁻⁸`. With the halving made conditional the same two roots cost **11** and **35**.
 *
 * The defect is invisible in every answer — the bracket is retained throughout and the root is
 * correct to the last ulp either way. It shows up only in the evaluation count, which is precisely
 * the quantity this routine was chosen for.
 *
 * ## Sign tests are written on signs, never on products (`P-15`)
 *
 * Both tests here compare *signs*. Writing either as a product — the obvious and idiomatic
 * `atLeft * atEstimate < 0.0` — is a defect, and it was one: `C-0019` (`S-143`) observed this
 * routine evaluating `f` **outside its own bracket**. A product of two doubles underflows long
 * before either factor does, so when both residuals are tiny and of opposite sign the product
 * becomes `−0.0`, the test reads `false`, the *left* endpoint is replaced instead of the right,
 * and it is replaced by a value of the **same sign as the right**. The bracket is then gone, the
 * interpolation is an extrapolation, and the next step leaves `[low, high]`. The entry test
 * `require(atLeft * atRight <= 0.0)` fails the same way in the opposite direction: two tiny
 * residuals of the *same* sign multiply to `+0.0`, so a bracket containing no root is accepted.
 *
 * The trigger is a residual spanning decades, which is the ordinary shape of a disjoining
 * pressure — at a 30 nm gap it is four orders of magnitude below the two terms it is the
 * difference of. It is not a tolerance question and no iteration budget repairs it.
 *
 * The secant step is additionally required to land strictly inside the live bracket, falling back
 * to bisection when it does not. That covers the residual cases the sign test alone cannot: a
 * `NaN` step from an endpoint pair whose values have both been halved into zero, and a step
 * pushed onto an endpoint by rounding.
 *
 * ## Termination
 *
 * On the **bracket width**, not on the residual. A residual test cannot be satisfied below the
 * noise floor of a quadrature of ~10³ terms, and an unreachable tolerance is silent: the loop
 * returns the right answer having run its full iteration cap every time.
 *
 * @throws IllegalArgumentException if [low] and [high] do not bracket a sign change.
 */
internal inline fun bracketedRoot(
    low: Double,
    high: Double,
    tolerance: Double = 1e-15,
    iterations: Int = 200,
    f: (Double) -> Double
): Double {
    var left = low
    var right = high
    var atLeft = f(left)
    var atRight = f(right)
    if (atLeft == 0.0) return left
    if (atRight == 0.0) return right
    // The sign of the left endpoint's residual. It is carried separately because the Illinois
    // halving mutates `atLeft`'s magnitude — and can eventually flush it to zero — while the
    // endpoint it describes has not moved. Where the endpoint *does* move, it moves to a point
    // of this same sign, so this flag is an invariant of the whole iteration.
    val leftIsNegative = atLeft < 0.0
    require(leftIsNegative != (atRight < 0.0)) {
        "f must change sign over [$low, $high], was: $atLeft .. $atRight"
    }
    var estimate = left
    // Which endpoint the previous step replaced: −1 the right, +1 the left, 0 not yet.
    var replaced = 0
    repeat(iterations) {
        val secant = (left * atRight - right * atLeft) / (atRight - atLeft)
        // Also rejects NaN, which is what the secant becomes if both residuals underflow to zero.
        estimate = if (secant > left && secant < right) secant else 0.5 * (left + right)
        // The bracket is two adjacent doubles: no interior point exists and this is the answer.
        if (estimate <= left || estimate >= right) return estimate
        val atEstimate = f(estimate)
        if (atEstimate == 0.0) return estimate
        if ((atEstimate < 0.0) != leftIsNegative) {
            right = estimate
            atRight = atEstimate
            if (replaced == -1) atLeft *= 0.5
            replaced = -1
        } else {
            left = estimate
            atLeft = atEstimate
            if (replaced == 1) atRight *= 0.5
            replaced = 1
        }
        if (right - left <= tolerance * (if (estimate == 0.0) 1.0 else abs(estimate))) {
            return estimate
        }
    }
    return estimate
}

/**
 * Returns `∫₀^1 (1 − u²)^p du = (√π/2) Γ(p+1)/Γ(p+3/2)`, the shape factor of a
 * strong-stretching profile with osmotic exponent `m = 1 + 1/p`.
 *
 * `2/3` at `p = 1` (the mean-field parabola) and `0.70898` at `p = 4/5` (des Cloizeaux).
 */
internal fun halfCircleMoment(p: Double): Double {
    require(p > 0.0) { "p must be positive, was: $p" }
    return 0.5 * sqrt(PI) * exp(logGamma(p + 1.0) - logGamma(p + 1.5))
}

/** The Lanczos approximation to `ln Γ(x)` for `x > 0`, accurate to ~1e-15 relative. */
internal fun logGamma(x: Double): Double {
    require(x > 0.0) { "x must be positive, was: $x" }
    val z = x - 1.0
    var series = LANCZOS[0]
    for (i in 1 until LANCZOS.size) series += LANCZOS[i] / (z + i)
    val t = z + LANCZOS.size - 1.5
    return 0.5 * ln(2.0 * PI) + (z + 0.5) * ln(t) - t + ln(series)
}

private fun GraftedLayerModel.requireWithinLayer(chain: GraftedChain, height: Double) {
    val unperturbed = equilibriumHeight(chain)
    require(height > chain.occupiedThickness && height <= unperturbed) {
        "height must be within (${chain.occupiedThickness}, $unperturbed], was: $height"
    }
}

/** Relative width at which every bracket in this file is considered converged. */
private const val CONVERGENCE = 1e-15

/** Passes of the `N ← N · L₀_target/L₀(N)` fixed point before falling back to bracketing. */
private const val FIXED_POINT_ITERATIONS = 40

/** A chain length in the middle of the Gen-1 design space, used to seed that fixed point. */
private const val CHAIN_LENGTH_SEED = 200.0

private const val NEWTON_ITERATIONS = 60

/** How far above the dry thickness the height brackets reach — 1e6 is far beyond any brush. */
private const val HEIGHT_BRACKET_DECADES = 1e6

private const val CHAIN_LENGTH_BRACKET = 1e7

private val LANCZOS = doubleArrayOf(
    0.99999999999980993,
    676.5203681218851,
    -1259.1392167224028,
    771.32342877765313,
    -176.61502916214059,
    12.507343278686905,
    -0.13857109526572012,
    9.9843695780195716e-6,
    1.5056327351493116e-7
)
