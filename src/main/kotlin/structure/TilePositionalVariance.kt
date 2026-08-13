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
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cosh
import kotlin.math.expm1
import kotlin.math.sinh
import kotlin.math.sqrt

/**
 * The thermal position fluctuation of the Gen-1 tile at 300 K, resolved **by mode** rather
 * than as the single piston number `C-0001` reported.
 *
 * ## Why one number is not enough
 *
 * `C-0001` quoted `σ_RMS = √(k_BT/k)` against the layer stiffness, which is the fluctuation
 * of a **rigid body with one degree of freedom**. [`C-0006`] rejected that premise: the tile
 * is a plate on a compliant foundation with `ℓ/L ≈ 0.2–0.5`, so its own bending modes are
 * soft too, and at 300 K they carry *more* amplitude than the piston mode does.
 *
 * The budget below therefore separates:
 *
 * - [pistonRms] — rigid translation normal to the electrode. This, and only this, is the
 *   fluctuation of the tile's **mean height**, because the tilts and the dishing modes have
 *   zero area average by construction. It is what an ideal area-averaging sensor would see,
 *   and what the layer's total reaction responds to.
 * - [tiltRms] — the area-averaged contribution of the two rigid tilts, exactly `√2` pistons
 *   whenever the plate is rigid, because each tilt has stiffness `k_f A/3`.
 * - [dishingRms] — everything that is not a rigid-body mode: the tile's own shape.
 * - [areaRms] — the RMS over *both* the ensemble and the footprint, `√(piston² + tilt² + dishing²)`.
 * - [centreRms], [edgeMidpointRms], [cornerRms] — the fluctuation of a **material point**,
 *   which is what a point-coupled lever samples. It varies over the footprint by a factor of
 *   `√7` even for a perfectly rigid tile, because the tilts are at full lever at a corner.
 *
 * ## Sign and geometry conventions
 *
 * Inherited unchanged from `T-5`/`T-5b`: `x` along the helices, `y` across them, the origin
 * at the centre of the footprint, `w` positive **downward**, compressing the polymer layer.
 * Every amplitude here is a root-mean-square in nm and therefore unsigned.
 */
@Serializable
data class PositionalVarianceBudget(
    val pistonRms: Double,
    val tiltRms: Double,
    val dishingRms: Double,
    val rigidBodyRms: Double,
    val areaRms: Double,
    val centreRms: Double,
    val edgeMidpointRms: Double,
    val cornerRms: Double,
    val dishingOverPiston: Double
)

/**
 * The RMS fluctuation in nm of the deflection at the **material point** ([x], [y]),
 * from fluctuation-dissipation rather than from the modal covariance:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`⟨w(x)²⟩ = k_BT · C(x, x)`,
 *
 * where `C(x, x)` is the deflection at `x` produced by a **unit point load at `x`** —
 * the reciprocal compliance. Written this way on purpose: it reaches the same number as
 * `k_BT b(x)ᵀK⁻¹b(x)` through the solver's *load-assembly and back-substitution* path
 * rather than through its inverse-diagonal path, so agreeing with
 * [PlateOnFoundation.thermalFluctuation] is a real check and not a tautology.
 *
 * A Rayleigh-Ritz restriction can only stiffen the plate, so this is a **lower** bound on
 * the true point fluctuation and rises monotonically with the basis degree — which is what
 * makes gate 4 a one-sided convergence statement rather than a hope.
 */
fun PlateOnFoundation.pointFluctuationRms(
    x: Double,
    y: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    val compliance = solve(pointLoads = listOf(PointLoad(x, y, 1.0))).deflection(x, y)
    check(compliance > 0.0) {
        "the point compliance must be positive, was: $compliance at ($x, $y)"
    }
    return sqrt(thermalEnergy(temperature) * compliance)
}

/**
 * The RMS fluctuation in nm of the tile's **area-averaged** height — the piston mode —
 * obtained as `k_BT (K⁻¹)₀₀` through the static response to a unit *uniform* load.
 *
 * The Legendre basis is orthogonal in the area inner product, so a uniform pressure loads
 * only the `P₀P₀` mode: the mean deflection under a total force `F` is exactly `F (K⁻¹)₀₀`,
 * and the compliance falls out of one solve. Independent route to
 * [PlateThermalFluctuation.pistonRms]; gate 3.
 */
fun PlateOnFoundation.pistonComplianceRms(
    temperature: Double = ROOM_TEMPERATURE
): Double {
    val compliance = solve(uniformPressure(1.0 / plate.area)).meanDeflection
    check(compliance > 0.0) { "the piston compliance must be positive, was: $compliance" }
    return sqrt(thermalEnergy(temperature) * compliance)
}

/**
 * The full modal budget of the tile's thermal position fluctuation at [temperature].
 *
 * Equipartition on the Ritz functional — the coefficient covariance is `k_BT K⁻¹` — plus
 * three point evaluations through [pointFluctuationRms]. Exact for a harmonic functional:
 * there is no sampling here and therefore no sampling uncertainty, which is precisely why
 * the honest uncertainty on this number is the **model bracket** and not a confidence
 * interval. See the `T-8` task file for why that distinction is a deliverable rather than
 * a caveat.
 */
fun PlateOnFoundation.positionalVarianceBudget(
    temperature: Double = ROOM_TEMPERATURE
): PositionalVarianceBudget {
    val fluctuation = thermalFluctuation(temperature)
    val piston = fluctuation.pistonRms
    val tilt = fluctuation.tiltRms
    val dishing = fluctuation.dishingRms
    val halfX = plate.lengthX / 2.0
    val halfY = plate.lengthY / 2.0
    return PositionalVarianceBudget(
        pistonRms = piston,
        tiltRms = tilt,
        dishingRms = dishing,
        rigidBodyRms = sqrt(piston * piston + tilt * tilt),
        areaRms = sqrt(piston * piston + tilt * tilt + dishing * dishing),
        centreRms = pointFluctuationRms(0.0, 0.0, temperature),
        edgeMidpointRms = pointFluctuationRms(halfX, 0.0, temperature),
        cornerRms = pointFluctuationRms(halfX, halfY, temperature),
        dishingOverPiston = dishing / piston
    )
}

// ------------------------------------------------------------------ bandwidth

/**
 * The corner frequency in Hz of an overdamped first-order coordinate of [stiffness] in
 * `pN/nm` against [drag] in `pN·s/nm`: `f_c = k/(2πγ)`.
 *
 * Named separately from `poroelastic.cornerFrequency`, which takes a relaxation time,
 * because the quantity `T-8` needs is a function of the **stiffness bracket** it is
 * sweeping and the drag is held fixed across that sweep.
 *
 * `C-0004` establishes the premise this rests on: the tile is overdamped by six orders
 * (`Q = 7e−4`), so the response is first-order and the spectrum is a single Lorentzian
 * rather than a resonance.
 */
fun lorentzianCornerFrequency(stiffness: Double, drag: Double): Double {
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    require(drag > 0.0) { "drag must be positive, was: $drag" }
    return stiffness / (2.0 * PI * drag)
}

/**
 * The one-sided displacement power spectral density in `nm²/Hz` of an overdamped
 * coordinate, from the fluctuation-dissipation theorem:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`S(f) = 4 k_BT γ / (k² + (2πfγ)²)`.
 *
 * Integrating it over all frequencies returns `k_BT/k`, which is gate 3 — the bandwidth
 * split below is only meaningful if the spectrum it splits carries the equipartition
 * variance and nothing else.
 */
fun lorentzianSpectralDensity(
    frequency: Double,
    drag: Double,
    stiffness: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(frequency >= 0.0) { "frequency must not be negative, was: $frequency" }
    require(drag > 0.0) { "drag must be positive, was: $drag" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    val reactance = 2.0 * PI * frequency * drag
    return 4.0 * thermalEnergy(temperature) * drag / (stiffness * stiffness + reactance * reactance)
}

/**
 * The fraction of an overdamped coordinate's variance that lies below [frequency],
 * `(2/π) arctan(f/f_c)` — the integral of [lorentzianSpectralDensity] in closed form.
 *
 * This is what turns "σ_RMS = 1.4 nm" into a statement about a **measurement**. A variance
 * quoted without a bandwidth is the `f → ∞` limit, and for this system almost all of it
 * sits far above the ≥ 1 kHz band §3 asks for: the layer's drainage corner is 91 kHz at
 * the nominal design point (`C-0004`), so only `(2/π) arctan(1/91) = 0.70 %` of the
 * variance is in band.
 *
 * The function is monotone **decreasing** in [cornerFrequency], which is what licenses
 * using the *slowest* mode's corner for the whole budget: the slowest mode gives the
 * largest possible in-band share, so the result is an upper bound.
 */
fun varianceFractionBelow(frequency: Double, cornerFrequency: Double): Double {
    require(frequency >= 0.0) { "frequency must not be negative, was: $frequency" }
    require(cornerFrequency > 0.0) {
        "cornerFrequency must be positive, was: $cornerFrequency"
    }
    return (2.0 / PI) * atan(frequency / cornerFrequency)
}

// ------------------------------------------------------------------ the lateral mode

/**
 * The lateral (in-plane) drag coefficient in `pN·s/nm` of a tile sliding on a Brinkman
 * layer of [thickness] nm and hydrodynamic screening length [screeningLength] nm
 * (`√k`, from `C-0004`'s permeability models), over a footprint of [area] nm²:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`γ_∥ = η A coth(h/√k) / √k`.
 *
 * Obtained from the Brinkman equation `η u'' = (η/k) u` between a fixed electrode at
 * `z = 0` and the tile at `z = h`, whose solution is `u = U sinh(z/√k)/sinh(h/√k)`; the
 * wall stress is `ηU coth(h/√k)/√k`. It has both limits a lateral drag must have:
 * `ηA/√k` for a deep screening layer, and the free-film Couette value `ηA/h` when the
 * polymer stops screening at all.
 *
 * **This is a drag, not a stiffness.** A laterally homogeneous grafted layer exerts *no*
 * lateral restoring force on a non-adsorbing tile — the layer free energy is invariant
 * under lateral translation — so this coefficient sets how fast the tile wanders, not how
 * far. See [freeDiffusionRms].
 */
fun brinkmanShearDrag(
    viscosity: Double,
    area: Double,
    screeningLength: Double,
    thickness: Double
): Double {
    require(viscosity > 0.0) { "viscosity must be positive, was: $viscosity" }
    require(area > 0.0) { "area must be positive, was: $area" }
    require(screeningLength > 0.0) {
        "screeningLength must be positive, was: $screeningLength"
    }
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    val ratio = thickness / screeningLength
    // coth is expanded at both ends rather than evaluated as cosh/sinh throughout: below
    // 1e-3 the quotient loses the leading cancellation, and above ~20 both cosh and sinh
    // overflow to infinity and the quotient returns NaN instead of the 1.0 it tends to
    val cotangent = when {
        ratio < 1e-3 -> 1.0 / ratio + ratio / 3.0
        ratio > 20.0 -> 1.0
        else -> cosh(ratio) / sinh(ratio)
    }
    return viscosity * area * cotangent / screeningLength
}

/** The Einstein diffusivity `D = k_BT/γ` in `nm²/s` for a [drag] in `pN·s/nm`. */
fun einsteinDiffusivity(drag: Double, temperature: Double = ROOM_TEMPERATURE): Double {
    require(drag > 0.0) { "drag must be positive, was: $drag" }
    return thermalEnergy(temperature) / drag
}

/**
 * The RMS excursion in nm of one **unconfined** coordinate after [time] s of free
 * diffusion at [diffusivity] `nm²/s`: `√(2Dt)`.
 *
 * There is no equipartition amplitude for such a coordinate, because there is no
 * stiffness: the excursion grows without bound as `√t`, and the only thing that makes it
 * a finite number is naming an observation time. That is the honest form of the answer for
 * the tile's lateral position, which nothing in the §3 stack confines.
 */
fun freeDiffusionRms(diffusivity: Double, time: Double): Double {
    require(diffusivity > 0.0) { "diffusivity must be positive, was: $diffusivity" }
    require(time >= 0.0) { "time must not be negative, was: $time" }
    return sqrt(2.0 * diffusivity * time)
}

/**
 * The RMS excursion in nm after [time] s of a coordinate that diffuses at [diffusivity]
 * *and* is confined by a linear [stiffness] in `pN/nm` — an Ornstein-Uhlenbeck process:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`σ²(t) = (k_BT/k)(1 − e^(−2t/τ))`, &nbsp; `τ = γ/k = k_BT/(D k)`.
 *
 * The **two** in the exponent is the whole content of the formula and the easiest thing in
 * it to get wrong: the coordinate relaxes at `1/τ` but its *variance* relaxes at `2/τ`, and
 * dropping the factor makes the short-time limit `√(Dt)` instead of `√(2Dt)` — a `√2` error
 * that no dimensional check would catch.
 *
 * It exists here to make the two descriptions of the lateral mode one description:
 * it reduces to [freeDiffusionRms] for `t ≪ τ` and to `√(k_BT/k)` for `t ≫ τ`, so the
 * free-diffusion bound is the `k → 0` limit of equipartition rather than a rival to it.
 */
fun ornsteinUhlenbeckRms(
    diffusivity: Double,
    stiffness: Double,
    time: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(diffusivity > 0.0) { "diffusivity must be positive, was: $diffusivity" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    require(time >= 0.0) { "time must not be negative, was: $time" }
    val energy = thermalEnergy(temperature)
    val relaxation = energy / (diffusivity * stiffness)
    return sqrt(-(energy / stiffness) * expm1(-2.0 * time / relaxation))
}

/**
 * The transverse stiffness in `pN/nm` at the free end of a clamped strut of bending
 * rigidity [bendingRigidity] in `pN·nm²` and [length] nm: `3EI/L³`.
 *
 * The cheapest defensible bound on what an anchoring scheme could contribute laterally,
 * and it is quoted as a **bound**, not as a design: a duplex strut is the stiff extreme —
 * a flexible single-stranded tether contributes essentially nothing at zero tension,
 * because a chain's transverse stiffness is `F/L` and vanishes with the tension.
 */
fun cantileverTransverseStiffness(bendingRigidity: Double, length: Double): Double {
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    require(length > 0.0) { "length must be positive, was: $length" }
    return 3.0 * bendingRigidity / (length * length * length)
}
