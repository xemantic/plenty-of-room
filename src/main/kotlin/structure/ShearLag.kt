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

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The shear-lag lengths of a single-layer DNA-origami sheet loaded **in its own plane**,
 * derived for this lattice rather than taken from a fibre-composite formula.
 *
 * ## The derivation, in one line of algebra
 *
 * Duplex `n` is a bar of stretch modulus `S` running along `x`. Its only in-plane coupling
 * to its two neighbours is through the crossovers, which recur every `p` along **one**
 * interface (32 bp for a Rothemund sheet, not 16 — the per-helix figure would double the
 * coupling) and each of which resists relative sliding with an in-plane shear stiffness
 * `k_s`. Smearing the crossovers of one interface into a distributed coupling `γ = k_s/p`
 * per unit length, the axial equilibrium of bar `n` is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`S u_n'' + γ (u_{n+1} − 2u_n + u_{n−1}) = 0`,
 *
 * so a disturbance of across-helix wavenumber `q` decays along the helices as
 * `exp(−x/Λ(q))` with `Λ(q) = sqrt( S / (2γ (1 − cos(q d))) )`. Two special values of `q`
 * name themselves:
 *
 * - `q d = π` — alternate duplexes moving oppositely, the shortest wavelength the lattice
 *   carries. There `Λ = sqrt(S p /(4 k_s))`, which is [shearLagNeighbourLength] over `√2`.
 * - `q d = π/N` — the longest non-uniform mode of a free strip of `N` duplexes, and hence
 *   the length over which a point load becomes an **equal share**. That is
 *   [shearLagSharingLength], and for the Gen-1 tile it is longer than the tile.
 *
 * ## Why the number matters
 *
 * A tether that pulls in the plane of the sheet enters one duplex. How much of its tension
 * the *neighbouring* duplexes take before the far edge is reached is decided entirely by
 * these lengths, and the sheet is 25.6× stiffer along the helices than across them in
 * bending for the same reason it is shear-lag-limited here: the crossovers are the only
 * across-helix load path there is.
 */

/** The distributed in-plane shear coupling `γ = k_s/p` of one interface, in `pN/nm²`. */
fun interfaceShearCoupling(
    crossoverShearStiffness: Double,
    crossoverSpacing: Double
): Double {
    require(crossoverShearStiffness > 0.0) {
        "crossoverShearStiffness must be positive, was: $crossoverShearStiffness"
    }
    require(crossoverSpacing > 0.0) {
        "crossoverSpacing must be positive, was: $crossoverSpacing"
    }
    return crossoverShearStiffness / crossoverSpacing
}

/**
 * The composite-convention shear-lag transfer length `Λ = sqrt(S p / k_s)` in nm.
 *
 * The form the classical shear-lag literature writes as `sqrt(EA·s/G_eff)`, with the
 * lattice's own ingredients substituted: `EA` is the duplex stretch modulus `S`, `s` the
 * per-interface crossover spacing `p`, and `G_eff` the crossover's in-plane shear stiffness.
 * It is `√2` times [shearLagNeighbourLength], which is the length that actually governs the
 * exchange between two adjacent duplexes; both are reported so that neither convention can
 * be mistaken for the other.
 */
fun shearLagTransferLength(
    stretchModulus: Double,
    crossoverSpacing: Double,
    crossoverShearStiffness: Double
): Double {
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    return sqrt(
        stretchModulus / interfaceShearCoupling(crossoverShearStiffness, crossoverSpacing)
    )
}

/**
 * The neighbour-exchange length `Λ_nn = sqrt(S p /(2 k_s))` in nm — the decay length over
 * which one duplex sheds load to the one beside it.
 *
 * This is the zone-boundary mode `q d = π` scaled by `√2`, i.e. the two-duplex problem, and
 * it is the length a lattice solve actually reproduces. Asserted against the lattice rather
 * than quoted.
 */
fun shearLagNeighbourLength(
    stretchModulus: Double,
    crossoverSpacing: Double,
    crossoverShearStiffness: Double
): Double = shearLagTransferLength(
    stretchModulus, crossoverSpacing, crossoverShearStiffness
) / sqrt(2.0)

/**
 * The decay length in nm of the across-helix mode of wavenumber [waveNumber] in `nm⁻¹`,
 * **on the lattice** — `sqrt( S / (2γ (1 − cos(q d))) )`.
 *
 * @throws IllegalArgumentException if any argument is not positive.
 */
fun shearLagModeDecayLength(
    stretchModulus: Double,
    crossoverSpacing: Double,
    crossoverShearStiffness: Double,
    interhelicalDistance: Double,
    waveNumber: Double
): Double {
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(waveNumber > 0.0) { "waveNumber must be positive, was: $waveNumber" }
    val coupling = interfaceShearCoupling(crossoverShearStiffness, crossoverSpacing)
    val argument = waveNumber * interhelicalDistance
    // 1 - cos loses digits below ~1e-4; the half-angle form does not
    val stiffness = 4.0 * coupling * sin(argument / 2.0) * sin(argument / 2.0)
    return sqrt(stretchModulus / stiffness)
}

/**
 * The decay length in nm of the same mode in the **continuum** membrane,
 * `(1/q) sqrt(S p /(k_s d²))`.
 *
 * The two agree to `(q d)²/12` at long wavelength and differ by exactly `π/2` at the zone
 * boundary — the lattice decays faster, because a continuum does not know the duplex
 * spacing exists. Quoting the continuum length where the lattice one belongs would
 * overstate the load-sharing distance of the shortest mode by 57 %.
 */
fun shearLagContinuumModeDecayLength(
    stretchModulus: Double,
    crossoverSpacing: Double,
    crossoverShearStiffness: Double,
    interhelicalDistance: Double,
    waveNumber: Double
): Double {
    require(waveNumber > 0.0) { "waveNumber must be positive, was: $waveNumber" }
    return shearLagAspectRatio(
        stretchModulus, crossoverSpacing, crossoverShearStiffness, interhelicalDistance
    ) / waveNumber
}

/**
 * The load-sharing length in nm of a free strip of [duplexes] duplexes — the decay length
 * of its longest non-uniform across-helix mode, `q d = π/N`.
 *
 * **The number that decides the regime.** If it is short against the footprint, an in-plane
 * point load has become an equal share by the time it reaches the far edge, and the
 * concentration factor is near one on that account. If it is long, the duplex the tether
 * lands on keeps nearly all of the load, and the concentration is whatever the attachment
 * itself carries.
 */
fun shearLagSharingLength(
    stretchModulus: Double,
    crossoverSpacing: Double,
    crossoverShearStiffness: Double,
    interhelicalDistance: Double,
    duplexes: Int
): Double {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    return shearLagModeDecayLength(
        stretchModulus, crossoverSpacing, crossoverShearStiffness, interhelicalDistance,
        PI / (duplexes * interhelicalDistance)
    )
}

/**
 * The dimensionless shear-lag aspect ratio `sqrt(S p /(k_s d²))` — the transfer length
 * measured in interhelical distances.
 *
 * The membrane problem `A u_xx + B u_yy = 0` has **no intrinsic length**: a point load
 * spreads over an ellipse whose axes stand in exactly this ratio, and every decay length
 * above is this number divided by an across-helix wavenumber. It is the in-plane analogue
 * of `C-0006`'s bending anisotropy `D_∥/D_⊥ = 25.6`, and it is what makes a tether pulling
 * along the helices a different problem from one pulling across them.
 */
fun shearLagAspectRatio(
    stretchModulus: Double,
    crossoverSpacing: Double,
    crossoverShearStiffness: Double,
    interhelicalDistance: Double
): Double {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    return shearLagTransferLength(
        stretchModulus, crossoverSpacing, crossoverShearStiffness
    ) / interhelicalDistance
}

/**
 * A self-equilibrated in-plane tether pair: [force] pN applied at ([toX], [toY]) along the
 * chord, and the same force applied at ([fromX], [fromY]) in the opposite direction.
 *
 * The chord is **moment-free by construction** — the two forces are collinear with the line
 * joining their application points — which is what lets it be solved without inventing a
 * support that would itself carry load. It is the statics of a tile held by one pair of
 * opposed surface-parallel tethers, which is `C-0014`'s `S4` scheme reduced to its
 * load-bearing pair.
 */
data class ChordLoad(
    val fromX: Double,
    val fromY: Double,
    val toX: Double,
    val toY: Double,
    val force: Double
) {

    /** The chord length in nm. */
    val span: Double get() = sqrt(
        (toX - fromX) * (toX - fromX) + (toY - fromY) * (toY - fromY)
    )

    init {
        require(span > 0.0) { "a chord must have two distinct ends" }
    }

}

/**
 * The **continuum** orthotropic shear-lag membrane the in-plane lattice discretises,
 * solved in closed form as a cosine series across the helices.
 *
 * ## The reduction, and why it is the right continuum here
 *
 * `A ∂²u/∂x² + B ∂²u/∂y² + f = 0` with `A = S/d` the along-helix membrane stiffness per unit
 * width and `B = k_s d/p` the in-plane shear stiffness per unit width. This is the classical
 * shear-lag reduction of plane stress: the across-helix *direct* stress is dropped, because
 * across the helices there is no continuous material at all — only crossovers — and it is
 * exactly the reduction whose `n = 0` mode is the **equal share**. That is what makes the
 * concentration factor readable off the series rather than asserted: everything above the
 * `n = 0` term *is* the concentration.
 *
 * ## Boundary conditions
 *
 * Free edges everywhere: `∂u/∂y = 0` at `y = ±L_y/2` (no shear flow off the rim) and
 * `∂u/∂x = 0` at `x = ±L_x/2` (no axial traction off the rim). The eigenfunctions are
 * therefore `cos(mπ(y + L_y/2)/L_y)`, and each mode is a one-dimensional two-point problem
 * with a hyperbolic Green's function.
 *
 * ## What it cannot do, stated rather than hidden
 *
 * The series **does not converge at the load point** — a point load on a two-dimensional
 * elliptic problem has a logarithmic singularity, exactly as `C-0006`'s plate could not
 * resolve the peak force at a discrete anchor. That is not a defect of this implementation;
 * it is why the lattice is needed, and it is asserted as a test rather than worked around.
 *
 * @param modes the number of across-helix cosine modes retained beyond the uniform one.
 */
class ShearLagMembrane(
    val stretchModulus: Double,
    val interhelicalDistance: Double,
    val crossoverSpacing: Double,
    val crossoverShearStiffness: Double,
    val lengthX: Double,
    val duplexes: Int,
    val modes: Int = DEFAULT_MODES
) {

    init {
        require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
        require(interhelicalDistance > 0.0) {
            "interhelicalDistance must be positive, was: $interhelicalDistance"
        }
        require(crossoverSpacing > 0.0) {
            "crossoverSpacing must be positive, was: $crossoverSpacing"
        }
        require(crossoverShearStiffness > 0.0) {
            "crossoverShearStiffness must be positive, was: $crossoverShearStiffness"
        }
        require(lengthX > 0.0) { "lengthX must be positive, was: $lengthX" }
        require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
        require(modes >= 1) { "modes must be at least 1, was: $modes" }
    }

    /** The footprint across the helices in nm. */
    val lengthY: Double = duplexes * interhelicalDistance

    /** `A = S/d`, the along-helix membrane stiffness per unit width, in pN/nm. */
    val alongHelixMembraneStiffness: Double = stretchModulus / interhelicalDistance

    /** `B = k_s d/p`, the in-plane shear stiffness per unit width, in pN/nm. */
    val shearMembraneStiffness: Double =
        crossoverShearStiffness * interhelicalDistance / crossoverSpacing

    /** The `y` of duplex [beam] in nm, centred on the footprint — the lattice's own axes. */
    fun duplexY(beam: Int): Double =
        (beam - (duplexes - 1) / 2.0) * interhelicalDistance

    /** The axial force in pN one duplex carries if [force] is shared perfectly — the `n = 0` mode. */
    fun equalShareDuplexAxialForce(force: Double): Double = force / duplexes

    private fun mode(m: Int, y: Double): Double =
        cos(m * PI * (y + lengthY / 2.0) / lengthY)

    private fun modeSlope(m: Int, y: Double): Double =
        -(m * PI / lengthY) * sin(m * PI * (y + lengthY / 2.0) / lengthY)

    private fun waveNumber(m: Int): Double =
        (m * PI / lengthY) * sqrt(shearMembraneStiffness / alongHelixMembraneStiffness)

    /**
     * `cosh(κa) cosh(κb) / sinh(κL)` with `a, b ≥ 0` and `a + b ≤ L`, in a form whose every
     * exponent is non-positive.
     *
     * `cosh/sinh` above an argument of ~20 overflows to `NaN` and does **not** fall back to
     * the 1.0 it tends to — `CLAUDE.md` records that trap three times over, and a stiff
     * crossover puts `κL` in the hundreds here.
     */
    private fun coshCoshOverSinh(kappa: Double, a: Double, b: Double): Double {
        val l = lengthX
        val numerator = exp(kappa * (a + b - l)) + exp(kappa * (a - b - l)) +
                exp(kappa * (b - a - l)) + exp(-kappa * (a + b + l))
        return numerator / (2.0 * (1.0 - exp(-2.0 * kappa * l)))
    }

    /** `sinh(κa) cosh(κb) / sinh(κL)`, in the same stable form. */
    private fun sinhCoshOverSinh(kappa: Double, a: Double, b: Double): Double {
        val l = lengthX
        val numerator = exp(kappa * (a + b - l)) + exp(kappa * (a - b - l)) -
                exp(kappa * (b - a - l)) - exp(-kappa * (a + b + l))
        return numerator / (2.0 * (1.0 - exp(-2.0 * kappa * l)))
    }

    /** The Green's function of mode [m] at [x] for a unit source at [source]. */
    private fun green(m: Int, x: Double, source: Double): Double {
        val kappa = waveNumber(m)
        val lower = minOf(x, source)
        val upper = maxOf(x, source)
        return coshCoshOverSinh(kappa, lower + lengthX / 2.0, lengthX / 2.0 - upper) /
                (alongHelixMembraneStiffness * kappa)
    }

    /** `∂G/∂x` of mode [m] at [x] for a unit source at [source]. */
    private fun greenSlope(m: Int, x: Double, source: Double): Double {
        val kappa = waveNumber(m)
        return if (x <= source) {
            sinhCoshOverSinh(kappa, x + lengthX / 2.0, lengthX / 2.0 - source) /
                    alongHelixMembraneStiffness
        } else {
            -sinhCoshOverSinh(kappa, lengthX / 2.0 - x, source + lengthX / 2.0) /
                    alongHelixMembraneStiffness
        }
    }

    private fun requireAxial(load: ChordLoad) {
        require(load.fromY == load.toY) {
            "the shear-lag reduction carries the along-helix displacement only, so its " +
                    "chord must run along the helices; was ${load.fromY} .. ${load.toY}"
        }
    }

    /**
     * The uniform-mode axial slope: `F/(A L_y)` strictly between the two load stations and
     * zero outside them. Its contribution to the duplex axial force is exactly the equal
     * share, whatever the crossover stiffness.
     */
    private fun uniformSlope(load: ChordLoad, x: Double): Double {
        val lower = minOf(load.fromX, load.toX)
        val upper = maxOf(load.fromX, load.toX)
        if (x <= lower || x >= upper) return 0.0
        val sign = if (load.toX > load.fromX) 1.0 else -1.0
        return sign * load.force / (alongHelixMembraneStiffness * lengthY)
    }

    /** The along-helix displacement `u` in nm at ([x], [y]) under [load]. */
    fun displacement(load: ChordLoad, x: Double, y: Double): Double {
        requireAxial(load)
        var total = 0.0
        for (m in 1..modes) {
            val amplitude = 2.0 / lengthY * load.force * (
                    mode(m, load.toY) * green(m, x, load.toX) -
                            mode(m, load.fromY) * green(m, x, load.fromX)
                    )
            total += amplitude * mode(m, y)
        }
        return total
    }

    /** `∫ cos(mπ(y+L_y/2)/L_y) dy` over duplex [beam]'s tributary strip. */
    private fun modeOverStrip(m: Int, beam: Int): Double {
        val scale = lengthY / (m * PI)
        return scale * (
                sin(m * PI * (beam + 1.0) / duplexes) - sin(m * PI * beam.toDouble() / duplexes)
                )
    }

    /**
     * The axial force in pN carried by duplex [beam] at station [x] — the axial stress
     * resultant `(S/d) ∂u/∂x` integrated over that duplex's **tributary strip**.
     *
     * The strip integral rather than the value on the axis, and the difference is not
     * cosmetic: every non-uniform mode integrates to exactly zero over the whole width, so
     * the fifteen duplex forces sum to the applied force at every cut **identically**,
     * whatever the crossover stiffness and however many modes are retained. Sampling `∂u/∂x`
     * on the duplex axes instead leaves an aliasing residual that reaches 130 % of the
     * applied force for a soft crossover — a sum rule silently broken by a quadrature.
     *
     * Positive in tension, in the locked sign convention.
     */
    fun duplexAxialForce(load: ChordLoad, x: Double, beam: Int): Double {
        requireAxial(load)
        require(beam in 0 until duplexes) {
            "beam must be within 0 until $duplexes, was: $beam"
        }
        var integral = uniformSlope(load, x) * interhelicalDistance
        for (m in 1..modes) {
            val amplitude = 2.0 / lengthY * load.force * (
                    mode(m, load.toY) * greenSlope(m, x, load.toX) -
                            mode(m, load.fromY) * greenSlope(m, x, load.fromX)
                    )
            integral += amplitude * modeOverStrip(m, beam)
        }
        return alongHelixMembraneStiffness * integral
    }

    /**
     * The in-plane force in pN one crossover at ([x], [y]) transmits — the shear flow
     * `(k_s d/p) ∂u/∂y` times the tributary length `p` of one crossover, i.e. `k_s d ∂u/∂y`.
     */
    fun crossoverForce(load: ChordLoad, x: Double, y: Double): Double {
        requireAxial(load)
        var slope = 0.0
        for (m in 1..modes) {
            val amplitude = 2.0 / lengthY * load.force * (
                    mode(m, load.toY) * green(m, x, load.toX) -
                            mode(m, load.fromY) * green(m, x, load.fromX)
                    )
            slope += amplitude * modeSlope(m, y)
        }
        return crossoverShearStiffness * interhelicalDistance * slope
    }

    companion object {

        /**
         * The default number of across-helix modes.
         *
         * The series converges geometrically at a distance from the load point, at a rate
         * set by the mode decay length `Λ(q) ∝ 1/q`, so a station one crossover spacing away
         * needs `O(L_y/p)` modes and 600 is two orders beyond that. At the load point itself
         * it does not converge at all, which is a property of the continuum and is reported.
         */
        const val DEFAULT_MODES: Int = 600
    }

}
