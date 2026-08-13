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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Task `T-13` — **what holds the tile down at zero bias, and where it then sits.**
 *
 * ## The premise, which is three claims agreeing
 *
 * A non-adsorbing grafted layer exerts **no upward force above `L₀`** (`C-0010`), three of
 * `C-0003`'s six layer models have **exactly zero** stiffness at `L₀`, and `C-0017` took every
 * candidate coupling **unpreloaded**. So at zero bias the tile is unconfined in the `+z`
 * direction and the resting position is not a property of the §3 stack at all: it is a property
 * of whatever mechanism supplies a downward force, and this file is the enumeration of those.
 *
 * ## The sign convention, fixed once
 *
 * `U_net(h) = P(h)·A − F_down(h)`, **positive upward**. A *hold-down* is any mechanism
 * contributing to `F_down > 0`. A stable equilibrium is a root of `U_net` with `dU_net/dh < 0`,
 * and the stiffness there is `k₀ = −dU_net/dh`, so a mechanism whose magnitude **grows as the
 * gap closes** contributes **negative** stiffness — the same structure as §1's `k_es < 0`.
 *
 * ## The topology argument, which decides three of the six before any arithmetic
 *
 * A taut flexible link pulls its two ends together. A link grounded on the **substrate** pulls
 * the tile *down*; the same link grounded on a lever **above** the tile pulls it *up*. Only a
 * two-sided element — one that carries compression as well as tension — can be mounted with a
 * preload of either sign, and `C-0017`'s `K2` path puts **99.6 % of its compliance** in an
 * ssDNA spacer, which carries no compression at all.
 */

// ---------------------------------------------------------------- the scale to beat

/**
 * The downward force in pN a hold-down must supply for the tile's **mean** upward excursion to
 * stay inside [bound] nm: `k_BT/bound`.
 *
 * ## Why a force and not a stiffness
 *
 * `C-0010` writes the positional requirement as `k ≥ k_BT/σ²`, which is the right currency for
 * a *harmonic* coordinate. Above `L₀` the coordinate is not harmonic: the layer contributes
 * nothing at all, so the confining potential is **linear** and the excursion is exponentially
 * distributed, with mean `k_BT/F` and RMS `√2 k_BT/F`. The requirement is therefore a **force**,
 * and the two statements are the same one power of the bound apart —
 * `holdDownForceScale(σ)/σ = k_BT/σ²`, asserted as a gate-5 test.
 *
 * At 3.0 nm and 300 K this is **1.3807 pN**, and it is the bar every mechanism in this file is
 * measured against.
 */
fun holdDownForceScale(bound: Double, temperature: Double = ROOM_TEMPERATURE): Double {
    require(bound > 0.0) { "bound must be positive, was: $bound" }
    return thermalEnergy(temperature) / bound
}

// ---------------------------------------------------------------- van der Waals

/**
 * A Hamaker constant in this project's locked units, from one in joule.
 *
 * **`1 zJ = 10⁻²¹ J = 10⁻¹² N × 10⁻⁹ m = 1 pN·nm` exactly**, so the whole van der Waals
 * calculation runs in the units the rest of the programme already uses with no conversion
 * factor anywhere except this one function. A Hamaker constant of `5 × 10⁻²⁰ J` is **50 pN·nm**.
 */
fun hamakerFromJoule(joule: Double): Double = joule * 1.0e21

/**
 * The **across-water** combining relation, `A₁w₂ = √(A₁w₁ · A₂w₂)` — the form this task uses.
 *
 * ## Why this form and not the other one
 *
 * There are two combining relations in circulation and they are not interchangeable:
 *
 * - the **vacuum** form [combinedHamakerConstant], `A₁₃₂ = (√A₁₁ − √A₃₃)(√A₂₂ − √A₃₃)`, which
 *   reconstructs a medium interaction from vacuum constants;
 * - **this** form, which mixes two constants that are already *across water*. Tolias
 *   (arXiv:2003.00571) validates it to **1 % in vacuum and 2 % across water** over ~100 pairs
 *   and proves by Cauchy-Schwarz that it **always overestimates**.
 *
 * So the vacuum form survives here only as a **sign diagnostic** — it is the only one of the
 * two that can be negative, and therefore the only one that can answer "could the polymer in
 * the gap make this repulsive" — while every number carried into the force balance uses this
 * one. Both are kept in one file precisely so the distinction is auditable.
 *
 * **The caveat that travels with it, stated rather than hidden:** Tolias validates this form
 * for **metal/water/metal**, and the pair here is low-dielectric/water/metal. No sourced
 * statement about where the relation fails was obtained — a search returned one, and it did
 * not survive checking. The relation is therefore used as an **upper bound**, which is what
 * the Cauchy-Schwarz argument makes it, and never as an estimate.
 */
fun combinedHamakerAcrossWater(bodyOne: Double, bodyTwo: Double): Double {
    require(bodyOne >= 0.0) { "bodyOne must not be negative, was: $bodyOne" }
    require(bodyTwo >= 0.0) { "bodyTwo must not be negative, was: $bodyTwo" }
    return sqrt(bodyOne * bodyTwo)
}

/**
 * The **vacuum** combining relation for a body-medium-body Hamaker constant:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`A₁₃₂ = (√A₁₁ − √A₃₃)(√A₂₂ − √A₃₃)`.
 *
 * **Not used for any number in this task's force balance** — see
 * [combinedHamakerAcrossWater] for why. It is retained as the **sign diagnostic**, because it
 * is the only form that can be negative, and it encodes two structural facts asserted as
 * gate-2 tests:
 *
 * - index-matching **either** body to the medium gives exactly zero;
 * - the interaction is **repulsive** when the medium's constant lies between the two bodies',
 *   which is the only route by which the polymer in the gap could change the *sign* rather
 *   than the magnitude. It cannot here: water is below both DNA and every candidate electrode,
 *   so the polymer moves the medium term the *right* way but nowhere near far enough to cross
 *   either body.
 */
fun combinedHamakerConstant(bodyOne: Double, bodyTwo: Double, medium: Double): Double {
    require(bodyOne >= 0.0) { "bodyOne must not be negative, was: $bodyOne" }
    require(bodyTwo >= 0.0) { "bodyTwo must not be negative, was: $bodyTwo" }
    require(medium >= 0.0) { "medium must not be negative, was: $medium" }
    return (sqrt(bodyOne) - sqrt(medium)) * (sqrt(bodyTwo) - sqrt(medium))
}

/**
 * The effective Hamaker constant of a gap medium that is [volumeFraction] polymer and the rest
 * solvent, on the same `√A` mixing the combining relation itself uses:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`√A_mix = (1 − φ)√A_solvent + φ√A_polymer`.
 *
 * The Gen-1 layer is 1–5 % polymer by volume (`C-0011`), so this is a **few-per-cent**
 * correction and it runs toward *less* attraction, PEG's polarisability being higher than
 * water's. It is computed rather than waved away because §4(c) has already been caught with
 * the sign of a polymer-layer effect backwards once (`C-0005`).
 */
fun mediumHamakerWithPolymer(
    solvent: Double,
    polymer: Double,
    volumeFraction: Double
): Double {
    require(solvent >= 0.0) { "solvent must not be negative, was: $solvent" }
    require(polymer >= 0.0) { "polymer must not be negative, was: $polymer" }
    require(volumeFraction in 0.0..1.0) {
        "volumeFraction must be in [0, 1], was: $volumeFraction"
    }
    val root = (1.0 - volumeFraction) * sqrt(solvent) + volumeFraction * sqrt(polymer)
    return root * root
}

/**
 * The **fully-screened end** of the electrolyte screening bracket for the zero-frequency
 * (entropic) term of a Hamaker constant at gap [gap] nm: `e^(−2κd)`.
 *
 * ## Why this is one end of a bracket and not a value
 *
 * The `ν = 0` term is a classical, purely electrostatic contribution and mobile ions screen it,
 * while the dispersion terms sit at optical frequencies where the ions cannot follow and are
 * untouched. That much is standard. **The exact screening expression is not sourced here.**
 * A literature search returned one, with a citation and numbers; the citation did not survive
 * checking and the expression was withdrawn. Rather than substitute a recollection, the term is
 * carried as a **bracket between fully screened and unscreened**, which is what
 * `CLAUDE.md`'s research practice prescribes when a coefficient cannot be sourced.
 *
 * The bracket is affordable, and that is why it was not chased further — but the share is **not**
 * the one a symmetric constant suggests. For gold **across water** the zero-frequency term is
 * 1.5 % of `A_Au|w|Au` (Tolias, arXiv:2003.00571); for the **cross** constant this task actually
 * uses it is **10 % (metal) to 25 % (oxide)**, because the DNA half of the geometric mean is
 * itself only ~5 zJ and its own static term is a large fraction of that. Against an
 * electrode-material bracket that is already **2.6×** wide, narrowing a 25 % uncertainty buys
 * nothing — but quoting 1.5 % for it would have been wrong by an order of magnitude.
 *
 * The exponent is `2κd` and not `κd` because the interaction is second order in the fluctuating
 * field — the same exponent, and for the same reason, as the zero-bias image attraction in
 * `C-0008`.
 */
fun zeroFrequencyScreeningFactor(gap: Double, inverseDebyeLength: Double): Double {
    require(gap > 0.0) { "gap must be positive, was: $gap" }
    require(inverseDebyeLength >= 0.0) {
        "inverseDebyeLength must not be negative, was: $inverseDebyeLength"
    }
    return exp(-2.0 * inverseDebyeLength * gap)
}

/**
 * The Hamaker constant at gap [gap] nm at the **fully-screened** end of the bracket:
 * `A(d) = A_ν>0 + A_ν=0 e^(−2κd)`. The unscreened end is simply `A_ν>0 + A_ν=0`.
 *
 * @see zeroFrequencyScreeningFactor for why both ends are carried.
 */
fun screenedHamakerConstant(
    zeroFrequencyTerm: Double,
    dispersionTerm: Double,
    gap: Double,
    inverseDebyeLength: Double
): Double = dispersionTerm +
        zeroFrequencyTerm * zeroFrequencyScreeningFactor(gap, inverseDebyeLength)

/**
 * The factor by which **retardation** reduces the plate-plate van der Waals *pressure* at gap
 * [gap] nm in water.
 *
 * A two-point interpolation of a sourced bracket, not a formula: from Tolias
 * (arXiv:2202.09159) Tables VII–IX the gold/water/gold energy retardation factor is 73.5–76.1 %
 * at 5 nm and 59.8–63.1 % at 10 nm, and the corresponding **pressure** factors — a derivation
 * from the printed energy fit, not a printed number — are **82.4–84.6 %** and **70.2–73.3 %**.
 *
 * **Sourced for gold only.** No retardation figure for a dielectric face was obtained, so the
 * gold factor is applied across the whole electrode bracket and that substitution is stated
 * rather than justified. Applying it makes the result a **lower** bound and omitting it an
 * **upper** one; both are reported, because the honest object here is a bracket.
 */
fun retardationPressureFactor(gap: Double): Double {
    require(gap > 0.0) { "gap must be positive, was: $gap" }
    val near = 0.835
    val far = 0.7175
    val fraction = ((gap - 5.0) / 5.0).coerceIn(0.0, 1.0)
    return near + (far - near) * fraction
}

/**
 * The **magnitude** of the non-retarded van der Waals pressure in `pN/nm²` (= MPa) between a
 * slab of thickness [slabThickness] nm — the tile — and a half-space — the electrode —
 * separated by [gap] nm:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`P = (A/6π)[d⁻³ − (d + t)⁻³]`.
 *
 * Attractive for a positive [hamaker], which is the only case that arises here; the sign lives
 * in the caller, per this file's convention that a hold-down is reported positive.
 *
 * The finite-thickness term is not cosmetic. §3 gives the tile as *"~10 nm (single-layer
 * honeycomb)"*, which is two different structures — a single-layer sheet is one duplex
 * diameter, ~2 nm — and the two readings differ by **2.1× in pressure at a 10 nm gap**. Both
 * are carried.
 */
fun vanDerWaalsPressure(
    hamaker: Double,
    gap: Double,
    slabThickness: Double = Double.POSITIVE_INFINITY
): Double {
    require(gap > 0.0) { "gap must be positive, was: $gap" }
    require(slabThickness > 0.0) { "slabThickness must be positive, was: $slabThickness" }
    val far = if (slabThickness.isInfinite()) 0.0 else 1.0 / cube(gap + slabThickness)
    return hamaker / (6.0 * PI) * (1.0 / cube(gap) - far)
}

/**
 * The **magnitude** of `dP/dh` in `pN/nm³` for the same geometry:
 * `(A/2π)[d⁻⁴ − (d + t)⁻⁴]`.
 *
 * Positive, and it enters the equilibrium stiffness with a **minus** sign: the van der Waals
 * attraction grows as the gap closes, so it is a **negative** spring, exactly like `k_es`. For
 * a half-space the ratio to the pressure is `3/d` identically, which is a gate-1 test.
 */
fun vanDerWaalsPressureSlopeMagnitude(
    hamaker: Double,
    gap: Double,
    slabThickness: Double = Double.POSITIVE_INFINITY
): Double {
    require(gap > 0.0) { "gap must be positive, was: $gap" }
    require(slabThickness > 0.0) { "slabThickness must be positive, was: $slabThickness" }
    val far = if (slabThickness.isInfinite()) 0.0 else 1.0 / square(square(gap + slabThickness))
    return hamaker / (2.0 * PI) * (1.0 / square(square(gap)) - far)
}

private fun cube(value: Double): Double = value * value * value

private fun square(value: Double): Double = value * value

// ---------------------------------------------------------------- gravity

/**
 * The buoyant weight in pN of a body of [volume] nm³ and density [bodyDensity] `g/cm³` in a
 * fluid of [fluidDensity] `g/cm³`: `(ρ_b − ρ_f) V g`.
 *
 * Reported for one reason only: §7 rewards saying which terms were checked. It comes out **nine
 * orders of magnitude** below the thermal scale, and stating that as a computed number rather
 * than as an assumption costs one line.
 */
fun buoyantWeight(
    volume: Double,
    bodyDensity: Double,
    fluidDensity: Double,
    gravity: Double = STANDARD_GRAVITY
): Double {
    require(volume > 0.0) { "volume must be positive, was: $volume" }
    // 1 g/cm^3 = 1000 kg/m^3 and 1 nm^3 = 1e-27 m^3, so the product is 1e-24 N = 1e-12 pN
    return (bodyDensity - fluidDensity) * volume * gravity * 1.0e-12
}

/** Standard gravity in `m/s²` — **CITED**, the SI defining value. */
const val STANDARD_GRAVITY: Double = 9.80665

// ---------------------------------------------------------------- bridging

/**
 * The largest downward force in pN that [chainCount] grafted chains could exert on the tile by
 * **bridging** — adsorbing on it — if each gained [energyPerChain] `pN·nm` of adsorption energy
 * released over a range of [range] nm: `F ≤ n ε/ℓ`.
 *
 * A ceiling, not a value. The §3 layer is *non-adsorbing* by premise, and that premise is what
 * makes `C-0010`'s lateral restoring stiffness **exactly** zero — so it is load-bearing and it
 * has never been tested. The ceiling is enormous (a 10 nm layer puts 38 chains under the tile
 * and one `k_BT` each would be 159 pN), which is precisely why the useful object is the
 * **threshold** below.
 */
fun bridgingForceCeiling(
    chainCount: Double,
    energyPerChain: Double,
    range: Double
): Double {
    require(chainCount > 0.0) { "chainCount must be positive, was: $chainCount" }
    require(range > 0.0) { "range must be positive, was: $range" }
    return chainCount * energyPerChain / range
}

/**
 * The adsorption energy per chain in `pN·nm` at which bridging would reach [targetForce] —
 * the exact inverse of [bridgingForceCeiling], and the number a single published measurement
 * would falsify.
 */
fun bridgingEnergyThreshold(
    targetForce: Double,
    chainCount: Double,
    range: Double
): Double {
    require(chainCount > 0.0) { "chainCount must be positive, was: $chainCount" }
    require(range > 0.0) { "range must be positive, was: $range" }
    return targetForce * range / chainCount
}

// ---------------------------------------------------------------- the two tether topologies

/**
 * The downward force in pN from [count] entropic tethers grounded on the **substrate** and
 * reaching the tile at [height] nm — `C-0014`'s `S3`, read as a hold-down.
 *
 * Positive, always: the geometry stretches the tether to the layer height whether or not it is
 * taut (`CH-0013`), and a taut chain pulls its ends together.
 */
fun substrateTetherHoldDown(
    chain: FreelyJointedChain,
    count: Int,
    height: Double
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    return count * chain.tension(height)
}

/**
 * The downward force in pN from [count] tethers grounded on a lever **above** the tile at a
 * stroke of [stroke] nm — `C-0017`'s `K2` topology, read in the same currency.
 *
 * **Negative or zero, always**, and zero at zero stroke. That is the topology argument as one
 * function: the same element, the same parameters, the other ground point, the opposite sign —
 * and at the zero-bias state (`stroke = 0`) it contributes **exactly nothing**.
 */
fun leverTetherHoldDown(
    chain: FreelyJointedChain,
    count: Int,
    stroke: Double
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    return if (stroke <= 0.0) 0.0 else -count * chain.tension(stroke)
}

/**
 * The **downward** preload in pN a linear coupling of [stiffness] must carry at zero stroke for
 * its operating point to sit at [targetStroke] while the actuator delivers the mandated force
 * there: `F = (k_c − k_c*)·δ*`.
 *
 * Algebraically identical to `C-0017`'s `placementPreload`, and asserted equal to it as a gate-3
 * test — but written in the sign this task needs and with the mandate factored out, because the
 * relation is the whole of the connection between `T-16` and `T-13`:
 *
 * > **every `pN/nm` by which the output coupling exceeds §3's own `33.333 pN/nm` mandate is
 * > exactly 3 pN of hold-down, and a coupling *at* the mandate supplies none.**
 */
fun couplingPreloadForStiffness(
    stiffness: Double,
    mandatedStiffness: Double,
    targetStroke: Double
): Double {
    require(targetStroke > 0.0) { "targetStroke must be positive, was: $targetStroke" }
    return (stiffness - mandatedStiffness) * targetStroke
}

// ---------------------------------------------------------------- the equilibrium

/**
 * The zero-bias resting height in nm: the first root of [netUpwardForce], scanning **downward**
 * from [ceiling] toward [floor] and bisecting inside the first bracket.
 *
 * Returns `null` when the tile is not pulled down at the ceiling at all — which is not an edge
 * case but **the answer in the absence of any hold-down**: a non-adsorbing layer exerts no
 * upward force above `L₀`, so with `F_down = 0` every height above `L₀` is a neutral
 * equilibrium and the resting position is undefined rather than large.
 *
 * Scanned rather than bisected over the whole interval, for `C-0012`'s reason: the net force is
 * not monotone once several mechanisms with different gap dependences are added. Exits on the
 * **bracket width**, never on a residual (`CLAUDE.md`).
 */
fun zeroBiasRestingHeight(
    netUpwardForce: (Double) -> Double,
    ceiling: Double,
    floor: Double,
    scanSteps: Int = 2048
): Double? {
    require(ceiling > floor) { "ceiling must exceed floor, was: $ceiling vs $floor" }
    require(floor > 0.0) { "floor must be positive, was: $floor" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    var high = ceiling
    var atHigh = netUpwardForce(high)
    if (atHigh >= 0.0) return null
    val step = (ceiling - floor) / scanSteps
    for (i in 1..scanSteps) {
        val low = if (i == scanSteps) floor else ceiling - i * step
        val atLow = netUpwardForce(low)
        if (atLow >= 0.0) {
            var left = low
            var right = high
            repeat(200) {
                val middle = 0.5 * (left + right)
                if (netUpwardForce(middle) >= 0.0) left = middle else right = middle
                if (right - left <= 1e-14 * max(right, 1.0)) return 0.5 * (left + right)
            }
            return 0.5 * (left + right)
        }
        high = low
        atHigh = atLow
    }
    return null
}

/**
 * `k₀ = −dU_net/dh` in `pN/nm` at [height], by a central difference of [netUpwardForce] with
 * step [step] nm.
 *
 * Positive means a restoring equilibrium. The layer contributes `+k_layer`; van der Waals and
 * the residual field contribute **negatively**, because their magnitudes grow as the gap
 * closes; a substrate tether contributes `+f′(h)`, its own tangent stiffness, because it
 * *relaxes* as the tile descends.
 */
fun equilibriumStiffness(
    netUpwardForce: (Double) -> Double,
    height: Double,
    step: Double = 1.0e-5
): Double {
    require(step > 0.0) { "step must be positive, was: $step" }
    require(height - step > 0.0) { "height must exceed the step, was: $height vs $step" }
    return -(netUpwardForce(height + step) - netUpwardForce(height - step)) / (2.0 * step)
}

// ---------------------------------------------------------------- the statistics

/**
 * The tile's positional statistics about its zero-bias rest, in nm.
 *
 * @property mean `⟨h⟩` — **not** the equilibrium height, because the potential is asymmetric.
 * @property rms the standard deviation `√(⟨h²⟩ − ⟨h⟩²)`, which is what §6 task 8's `σ_RMS` means.
 * @property meanExcursionAbove `⟨max(h − reference, 0)⟩`, the upward half of the same
 *   distribution — the half the layer does not confine at all.
 * @property probabilityAbove the fraction of the time the tile spends above `reference`.
 */
data class ZeroBiasPositionStatistics(
    val mean: Double,
    val rms: Double,
    val meanExcursionAbove: Double,
    val probabilityAbove: Double,
    /**
     * `[Φ(upper) − Φ_min]/k_BT` — the depth of the well the tile sits in, at the top of the
     * quadrature domain.
     *
     * **This is the number that says whether the other four mean anything.** A hold-down whose
     * force falls faster than `1/h` has a *convergent* potential and therefore a **finite**
     * well: van der Waals goes as `h⁻³`, so its potential is bounded and the tile escapes to
     * infinity given time. The distribution is then not normalisable on an infinite domain and
     * every moment above is a property of the domain, not of the physics. Reported so that such
     * a case is caught rather than quoted.
     */
    val escapeBarrier: Double,
    /** The top of the quadrature domain in nm, so the domain dependence above is auditable. */
    val domainUpper: Double
)

/**
 * The exact Boltzmann statistics of the tile's height over `[lower, upper]`, from the potential
 * `Φ(h) = −∫U_net dh` built by cumulative trapezoid on a uniform grid of [panels] panels.
 *
 * ## Why not equipartition
 *
 * Because the potential is not harmonic and is not even symmetric: **linear** above `L₀`, where
 * the layer contributes nothing and only the hold-down acts, and steeply nonlinear below the
 * rest height. `σ² = k_BT/k` is a statement about a quadratic well and using it here would
 * assume away the one feature that makes the zero-bias state different from the operating
 * point. Equipartition is instead asserted as a **limiting case** — over a harmonic
 * `U_net = −k(h − h₀)` this function must return `√(k_BT/k)` — and so is the exponential limit,
 * where a constant hold-down `F` must return a mean excursion of exactly `k_BT/F`.
 *
 * [energyOffset] exists only to check that an additive constant in the potential is
 * unobservable, which is a conservation statement rather than a parameter.
 *
 * [reference] defaults to [lower]; the excursion statistics are measured from it.
 */
fun boltzmannPositionStatistics(
    netUpwardForce: (Double) -> Double,
    lower: Double,
    upper: Double,
    panels: Int,
    temperature: Double = ROOM_TEMPERATURE,
    energyOffset: Double = 0.0,
    reference: Double = lower
): ZeroBiasPositionStatistics {
    require(upper > lower) { "upper must exceed lower, was: $upper vs $lower" }
    require(panels >= 8) { "panels must be at least 8, was: $panels" }
    val energy = thermalEnergy(temperature)
    val width = (upper - lower) / panels
    val height = DoubleArray(panels + 1) { lower + it * width }
    val force = DoubleArray(panels + 1) { netUpwardForce(height[it]) }
    // Phi(h) = -integral U_net dh, by cumulative trapezoid from the lower end
    val potential = DoubleArray(panels + 1)
    potential[0] = energyOffset
    for (i in 1..panels) {
        potential[i] = potential[i - 1] - 0.5 * (force[i] + force[i - 1]) * width
    }
    var lowest = Double.MAX_VALUE
    for (value in potential) lowest = min(lowest, value)
    val weight = DoubleArray(panels + 1) { exp(-(potential[it] - lowest) / energy) }
    fun integrate(value: (Int) -> Double): Double {
        var total = 0.0
        for (i in 0 until panels) {
            total += 0.5 * (value(i) + value(i + 1)) * width
        }
        return total
    }
    val norm = integrate { weight[it] }
    val first = integrate { weight[it] * height[it] } / norm
    val second = integrate { weight[it] * height[it] * height[it] } / norm
    val above = integrate { weight[it] * max(0.0, height[it] - reference) } / norm
    val fraction = integrate { if (height[it] > reference) weight[it] else 0.0 } / norm
    return ZeroBiasPositionStatistics(
        mean = first,
        rms = sqrt(max(0.0, second - first * first)),
        meanExcursionAbove = above,
        probabilityAbove = fraction,
        escapeBarrier = (potential[panels] - lowest) / energy,
        domainUpper = upper
    )
}

/**
 * The depth in `pN·nm` of the van der Waals well a slab of thickness [slabThickness] sits in at
 * gap [gap] over an area [area], measured against infinite separation:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`ΔΦ = ∫_h^∞ F dh = (A·S/12π)[h⁻² − (h + t)⁻²]`.
 *
 * **Finite**, and that is the whole point. A `1/h³` force integrates to a bounded potential, so
 * van der Waals traps the tile rather than confining it: the well has a depth, the tile escapes
 * over it at a rate `∝ e^(−ΔΦ/k_BT)`, and any "positional variance" quoted inside such a well
 * is a property of the domain the quadrature was run on.
 */
fun vanDerWaalsWellDepth(
    hamaker: Double,
    gap: Double,
    slabThickness: Double,
    area: Double
): Double {
    require(gap > 0.0) { "gap must be positive, was: $gap" }
    require(slabThickness > 0.0) { "slabThickness must be positive, was: $slabThickness" }
    require(area > 0.0) { "area must be positive, was: $area" }
    val far = if (slabThickness.isInfinite()) 0.0
    else 1.0 / square(gap + slabThickness)
    return hamaker * area / (12.0 * PI) * (1.0 / square(gap) - far)
}

/**
 * The mean upward excursion in nm of a tile held by a **constant** downward force [force]
 * against a layer that pushes back on nothing above `L₀`: `k_BT/F`, exactly.
 *
 * The closed form [boltzmannPositionStatistics] is graded against, and the inverse of
 * [holdDownForceScale].
 */
fun meanExcursionUnderConstantHoldDown(
    force: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(force > 0.0) { "force must be positive, was: $force" }
    return thermalEnergy(temperature) / force
}

/**
 * The Lorentzian corner frequency in Hz of an overdamped coordinate of [stiffness] `pN/nm`
 * against a drag of [drag] `pN·s/nm`: `f_c = k/(2πγ)`.
 *
 * Reproduced here rather than imported from `structure` so that this package can be compiled
 * and verified while a sibling agent is mid-TDD in that one; it is asserted equal to
 * `structure`'s own function nowhere, because the expression is one line and the *value* is
 * checked against `C-0010`'s published corner instead, which is the cross-check that matters.
 */
fun lorentzianCorner(stiffness: Double, drag: Double): Double {
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    require(drag > 0.0) { "drag must be positive, was: $drag" }
    return stiffness / (2.0 * PI * drag)
}

/**
 * The fraction of an overdamped mode's variance lying below [frequency] Hz, for a corner at
 * [cornerFrequency]: `(2/π) arctan(f/f_c)`.
 *
 * `C-0010`'s treatment, and the reason a broadband `σ_RMS` is not the measured quantity:
 * only 0.55–3.07 % of this tile's variance lies below 1 kHz.
 */
fun varianceFractionInBand(frequency: Double, cornerFrequency: Double): Double {
    require(frequency > 0.0) { "frequency must be positive, was: $frequency" }
    require(cornerFrequency > 0.0) {
        "cornerFrequency must be positive, was: $cornerFrequency"
    }
    return 2.0 / PI * kotlin.math.atan(frequency / cornerFrequency)
}

// ---------------------------------------------------------------- material constants

/**
 * Hamaker constants in `pN·nm` (= zJ = 10⁻²¹ J), each with its provenance.
 *
 * **§1 says "patterned electrode" and never says of what**, and that — not the physics — is the
 * largest single uncertainty in the van der Waals term. The set below is therefore a **bracket
 * over plausible electrodes**, and the answer is reported across it rather than for one.
 */
object HamakerConstants {

    /**
     * **DNA across water**, `A_DNA|w|DNA`, low end — **CITED, COMPUTED (Lifshitz)**, Dryden,
     * Hopkins, Denoyer, Poudel, Steinmetz, Ching, Podgornik, Parsegian & French, *Langmuir*
     * **31**:10145 (2015): 4.33 zJ for (AT)₁₀ and up to 5.90 zJ for (AT-GC)₅, at `ℓ = 5 nm`,
     * **already retarded and already `ν = 0`-screened**, cylinder-cylinder.
     *
     * Three things travel with this number and all three are stated rather than absorbed:
     *
     * 1. **the widely quoted `10⁻²⁰ J` for DNA is not a measurement.** Rau & Parsegian,
     *    *Biophys. J.* **61**:246 (1992) introduce it as *"if we assume a large value … then an
     *    **overestimate**"*, to prove van der Waals is too weak to matter; and the `2 × 10⁻²⁰ J`
     *    in the AFM literature is a **protein** value, substituted knowingly (Li et al.,
     *    *Nanomaterials* **9**:561, 2019, says so in as many words).
     * 2. **no planar `A_DNA` exists.** Every published value is cylinder-cylinder, and Dryden
     *    et al.'s own bound for the whole family is `A⁽⁰⁾ < 10 zJ`.
     * 3. it is already retarded, so re-applying [retardationPressureFactor] to a constant built
     *    from it retards that half **twice** — which makes the retarded reading a lower bound
     *    rather than an estimate. Both ends are reported.
     */
    const val DNA_ACROSS_WATER_LOW: Double = 4.33

    /** The high end of Dryden et al.'s sequence range, (AT-GC)₅. */
    const val DNA_ACROSS_WATER_HIGH: Double = 5.90

    /** Dryden et al.'s own ceiling for the whole family, `A⁽⁰⁾ < 10 zJ`. */
    const val DNA_ACROSS_WATER_CEILING: Double = 10.0

    /**
     * **Gold across water** — **CITED, COMPUTED (Lifshitz)**, Tolias, arXiv:2003.00571
     * Table VI: 238.6 / 248.1 / 267.9 zJ on three optical-data sets. The low end is used and
     * the spread (12 %) is inside every other bracket here.
     */
    const val GOLD_ACROSS_WATER: Double = 238.6

    /** The high end of the same three-way spread. */
    const val GOLD_ACROSS_WATER_HIGH: Double = 267.9

    /** **Platinum across water** — same source: 281.7 / 292.5 / 313.2 zJ. */
    const val PLATINUM_ACROSS_WATER: Double = 281.7

    /**
     * **Alumina across water** — **CITED**, Bell & Dimos, *MRS Proc.* **624**:275 (2000)
     * Table 3, 36.9 zJ; independently 8.86 `k_BT` = 36.7 zJ from Bergström parameters via
     * Prange et al., arXiv:2606.04331 Table 2. Two routes, 0.5 % apart.
     */
    const val ALUMINA_ACROSS_WATER: Double = 36.9

    /**
     * **Rutile titania across water** — same Prange source: 12.8 `k_BT` (Bergström) to
     * 22.3 `k_BT` (TDDFT), i.e. 53.0–92.3 zJ. The low end is used and the high end reported.
     */
    const val TITANIA_ACROSS_WATER: Double = 53.0

    /** The TDDFT end of the titania bracket. */
    const val TITANIA_ACROSS_WATER_HIGH: Double = 92.3

    /**
     * **Water in vacuum** — **CITED, COMPUTED (Lifshitz)**, Tolias arXiv:2003.00571:
     * 38.90 (Parsegian-Weiss) / 49.98 (Roth-Lenhoff) / 53.78 (Fiedler) zJ. The familiar
     * "≈ 37 zJ" sits at the **bottom** of a 38 % spread.
     *
     * Used **only** in the vacuum-form sign diagnostic, never in a force, which is why that
     * spread is not load-bearing here.
     */
    const val WATER: Double = 38.90

    /**
     * **Poly(ethylene oxide) in vacuum** — **NOT SOURCED.** An exact-phrase search of EuropePMC
     * full text returns **zero** hits for a Hamaker constant of PEG or PEO; the primary optical
     * constants exist (Shah et al., *Surf. Sci. Spectra* **27**:016001, 2020) but were not read.
     *
     * The value below is a **placeholder for the sign diagnostic only**. The question it would
     * answer is settled another way: Lorentz-Lorenz mixing at `n_PEG ≈ 1.46` raises the medium
     * index by **0.82 %** at `φ = 0.09`, cutting the optical contrast by **4.7 %** — far inside
     * every other bracket in this task, and in the direction of *less* attraction.
     */
    const val POLY_ETHYLENE_OXIDE: Double = 60.0

    /**
     * The zero-frequency (entropic) term of a Hamaker constant, in `pN·nm`:
     * **`(3/4)ζ(3) k_BT = 3.7345 zJ`** at 300 K.
     *
     * The familiar *"(3/4)k_BT"* = 3.106 zJ is the `s = 1` truncation of the sum and is **20 %
     * low**. For gold across water this term is only 1.4–1.6 % of the total; for a
     * low-dielectric face it is ~11 %, and Roth, Neal & Lenhoff, *Biophys. J.* **70**:977 (1996)
     * state independently that for low-dielectric materials across water it *"is roughly
     * constant at a value we calculate to be approximately 0.75 kT"*.
     */
    val ZERO_FREQUENCY_TERM: Double = 0.75 * APERY_CONSTANT * thermalEnergy()

    /** The zero-frequency term of a low-dielectric body across water, `(3/4)k_BT` (Roth 1996). */
    val ZERO_FREQUENCY_TERM_LOW_DIELECTRIC: Double = 0.75 * thermalEnergy()
}

/** `ζ(3)`, Apéry's constant — the sum the zero-frequency Hamaker term is a `(3/4)ζ(3)` of. */
const val APERY_CONSTANT: Double = 1.2020569031595943
