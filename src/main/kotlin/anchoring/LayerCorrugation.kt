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

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The two **anchorless** routes to lateral confinement, each closed as a ceiling with a
 * threshold rather than as a design.
 *
 * `C-0010`'s zero is a symmetry statement about a **laterally homogeneous** layer under a
 * **laterally homogeneous** tile. Break either homogeneity and the zero goes away — and the
 * §3 stack already contains one broken symmetry, because §1 says the electrode is *patterned*.
 * `C-0010` itself names lateral patterning of the grafting as "a design lever nobody has
 * costed".
 *
 * Neither can be *solved* at this level of theory: the first needs a laterally resolved layer
 * free energy and the second is `T-3b`'s two-dimensional Poisson-Boltzmann solve, which
 * `C-0008` states plainly a one-dimensional treatment cannot supply. So both are closed the
 * way `P-6` closed — **the largest value the mechanism can reach, and the value it would have
 * to reach for the answer to change** — which is falsified by a single calculation of the
 * quantity itself, and is honest in the meantime.
 */

// ---------------------------------------------------------------- the grafting pad

/**
 * The largest lateral **force** in pN a grafting-density pad can exert on the tile, from
 * [storedEnergy] `pN·nm` of compression energy stored in the layer under a tile of edge
 * [tileEdge] nm.
 *
 * Sliding the tile by `x` off a pad of its own size exposes an area `x·W` of it to the
 * surrounding, denser layer, at an areal free-energy cost `Δf`:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`F = W Δf`, and `Δf ≤ U/A = U/W²`, so **`F ≤ U/W`**.
 *
 * The bound on `Δf` is energy conservation and nothing else: the tile cannot gain by leaving
 * the pad more than the entire energy it put into the layer by being pressed onto it.
 * **This is a ceiling, not a value** — the realised `Δf` depends on the density contrast
 * between pad and surround, which is not solved here.
 */
fun graftingPadLateralForceCeiling(storedEnergy: Double, tileEdge: Double): Double {
    require(storedEnergy >= 0.0) { "storedEnergy must not be negative, was: $storedEnergy" }
    require(tileEdge > 0.0) { "tileEdge must be positive, was: $tileEdge" }
    return storedEnergy / tileEdge
}

/**
 * The same ceiling expressed as a **stiffness** in `pN/nm`, `k ≤ U/(W ℓ)`, where [healingLength]
 * `ℓ` is the distance over which the layer's density step is smeared.
 *
 * A grafted layer cannot follow a step in grafting density more sharply than its own height —
 * a chain of extension `h` leans by of order `h` — so `ℓ ≈ h` is used, and the direction of
 * that assumption is stated wherever the number appears: **a shorter healing length raises the
 * ceiling and a longer one lowers it, in exact inverse proportion.**
 *
 * The whole expression carries one consequence that no design can escape: `U` is the energy
 * the *actuator* stored, so **the pad's lateral stiffness is proportional to the load the tile
 * is already carrying, and it is exactly zero at zero bias.**
 */
fun graftingPadStiffnessCeiling(
    storedEnergy: Double,
    tileEdge: Double,
    healingLength: Double
): Double {
    require(healingLength > 0.0) { "healingLength must be positive, was: $healingLength" }
    return graftingPadLateralForceCeiling(storedEnergy, tileEdge) / healingLength
}

/**
 * The threshold that goes with the ceiling: the compression energy in `pN·nm` the tile would
 * have to have stored in the layer for a pad to reach [requiredStiffness].
 *
 * `U ≥ k W ℓ` — a dimensionally transparent statement of the criterion, and at the §3
 * geometry it says the tile must store at least `W ℓ k_BT/σ²` = 44.4 `k_BT`.
 */
fun graftingPadEnergyThreshold(
    tileEdge: Double,
    healingLength: Double,
    requiredStiffness: Double
): Double {
    require(tileEdge > 0.0) { "tileEdge must be positive, was: $tileEdge" }
    require(healingLength > 0.0) { "healingLength must be positive, was: $healingLength" }
    require(requiredStiffness > 0.0) {
        "requiredStiffness must be positive, was: $requiredStiffness"
    }
    return requiredStiffness * tileEdge * healingLength
}

// ---------------------------------------------------------------- the patterned electrode

/**
 * The tile's form factor against a sinusoidal surface pattern of [period] nm:
 * `sinc(qW/2) = sin(qW/2)/(qW/2)` with `q = 2π/period`, returned as a magnitude.
 *
 * A uniformly charged tile averages the pattern over its own footprint. It feels **nothing at
 * all** when its width is an exact multiple of the period, and the full modulation only when
 * the period is much larger than the tile — which is precisely the limit in which `q²`, and
 * hence the stiffness, is small. That competition is what makes the optimum interior.
 */
fun tileFormFactor(period: Double, tileEdge: Double): Double {
    require(period > 0.0) { "period must be positive, was: $period" }
    require(tileEdge > 0.0) { "tileEdge must be positive, was: $tileEdge" }
    val argument = PI * tileEdge / period
    return if (argument < 1e-8) 1.0 - argument * argument / 6.0 else abs(sin(argument) / argument)
}

/**
 * The attenuation of a lateral Fourier component of wavevector `q` across a gap of [height] nm
 * in an electrolyte of screening length [decayLength] nm:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`exp[−(√(κ² + q²) − κ) z]`, `κ = 1/λ`.
 *
 * The mean component decays as `e^{−κz}` and is already accounted for in the interaction
 * energy the modulation is a fraction of, so only the **excess** decay of the modulated part
 * appears here. It is negligible while `q ≪ κ` and it is what kills every short-period pattern.
 */
fun lateralGapAttenuation(period: Double, decayLength: Double, height: Double): Double {
    require(period > 0.0) { "period must be positive, was: $period" }
    require(decayLength > 0.0) { "decayLength must be positive, was: $decayLength" }
    require(height >= 0.0) { "height must not be negative, was: $height" }
    val wavevector = 2.0 * PI / period
    val screening = 1.0 / decayLength
    return exp(-(sqrt(screening * screening + wavevector * wavevector) - screening) * height)
}

/**
 * The lateral stiffness in `pN/nm` a patterned electrode can exert on the tile, from
 * [modulatedEnergy] `pN·nm` of laterally modulated interaction energy at [period] nm:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k = U_mod q² · |sinc(qW/2)| · exp[−(√(κ²+q²) − κ) z]`.
 *
 * `U_mod` is a **ceiling input**, not a solved quantity: the whole tile-electrode interaction
 * energy is of order `F_es × λ_force` (`C-0008`), and no more of it than that can be modulated.
 *
 * **Valid for `period ≥ tileEdge` only.** Below that the tile spans more than one full period
 * and the answer is set entirely by how sharply the tile's charge terminates at its own edge —
 * a detail this level of theory does not resolve, and one on which the sharp-edged form factor
 * above is optimistic. [optimalElectrodePeriod] searches only that domain and says so.
 */
fun patternedElectrodeStiffness(
    modulatedEnergy: Double,
    period: Double,
    tileEdge: Double,
    decayLength: Double,
    height: Double
): Double {
    require(modulatedEnergy >= 0.0) {
        "modulatedEnergy must not be negative, was: $modulatedEnergy"
    }
    val wavevector = 2.0 * PI / period
    return modulatedEnergy * wavevector * wavevector *
            tileFormFactor(period, tileEdge) *
            lateralGapAttenuation(period, decayLength, height)
}

/**
 * The period in nm that maximises [patternedElectrodeStiffness], searched over the domain
 * where the expression is valid — periods at least as long as the tile itself.
 *
 * A golden-section refinement of a log-spaced scan. The maximiser does not depend on
 * `U_mod`, which is a multiplicative constant, so it is a property of the geometry alone.
 */
fun optimalElectrodePeriod(
    tileEdge: Double,
    decayLength: Double,
    height: Double
): Double {
    require(tileEdge > 0.0) { "tileEdge must be positive, was: $tileEdge" }
    fun value(period: Double) =
        patternedElectrodeStiffness(1.0, period, tileEdge, decayLength, height)

    val samples = 2000
    val lower = tileEdge
    val upper = 200.0 * tileEdge
    var best = lower
    var bestValue = 0.0
    repeat(samples) { index ->
        val period = lower * (upper / lower).pow(index.toDouble() / (samples - 1))
        val candidate = value(period)
        if (candidate > bestValue) {
            bestValue = candidate
            best = period
        }
    }
    val step = (upper / lower).pow(1.0 / (samples - 1))
    var low = best / step
    var high = best * step
    val ratio = 0.5 * (sqrt(5.0) - 1.0)
    repeat(200) {
        val first = high - ratio * (high - low)
        val second = low + ratio * (high - low)
        if (value(first) < value(second)) low = first else high = second
        if (high - low <= 1e-12 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The dishing a lateral load modulation of fractional depth [depth] costs, as a fraction of
 * the stroke — **CITED**, `C-0006`, which demonstrated the response to be *exactly* linear in
 * the depth (0.2651 nm at 10 % against 1.3256 nm at 50 %, a ratio of exactly 5.000, over a
 * 4.95 nm stroke).
 *
 * This is the price of every field-based lateral confinement scheme and it is what makes them
 * expensive: `C-0006` rejects the rigid-plate assumption above ~19 % modulation depth, and a
 * corrugation deep enough to confine the tile is far deeper than that.
 */
fun dishingFromModulation(depth: Double): Double {
    require(depth in 0.0..1.0) { "depth must be a fraction between zero and one, was: $depth" }
    return DISHING_PER_UNIT_DEPTH * depth
}

/** `C-0006`'s edge-taper coefficient: 1.3256 nm of dishing per 4.95 nm of stroke at 50 % depth. */
const val DISHING_PER_UNIT_DEPTH: Double = (1.3256 / 4.95) / 0.5

/**
 * `C-0006`'s **interior** ripple transfer function, `1/(1 + (2πℓ/λ)⁴)`, with `ℓ = (D/k_f)^(1/4)`
 * the plate's bending length and `λ` the wavelength of the load non-uniformity.
 *
 * A plate on a foundation is a low-pass filter: it follows a long-wavelength load exactly and
 * ignores a short-wavelength one, crossing over at exactly one half when `λ = 2πℓ`.
 *
 * `C-0006` records that this form **does not apply at a free edge** — where it was wrong by
 * 50× — so it is used here only for the *interior* ripple a patterned electrode would impose,
 * which is precisely the case `C-0006` says it is valid for. It matters because the ripple a
 * lateral corrugation scheme needs has `λ ≈ 60 nm` against `ℓ_⊥ ≈ 4 nm`, i.e. a transfer of
 * essentially unity: **the tile follows such a ripple almost perfectly, so the dishing cost of
 * an electrode-patterned scheme is close to the full modulation depth times the stroke.**
 */
fun rippleTransfer(bendingLength: Double, wavelength: Double): Double {
    require(bendingLength > 0.0) { "bendingLength must be positive, was: $bendingLength" }
    require(wavelength > 0.0) { "wavelength must be positive, was: $wavelength" }
    val ratio = 2.0 * PI * bendingLength / wavelength
    return 1.0 / (1.0 + ratio * ratio * ratio * ratio)
}
