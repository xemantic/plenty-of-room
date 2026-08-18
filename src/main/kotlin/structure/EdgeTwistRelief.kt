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
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * `T-182` — the **value** of the prestrain whose threshold `C-0104` fixed at 15.4497275°.
 *
 * ## Why `C-0104`'s ladder is not the answer
 *
 * `C-0104` derived a ladder of candidate prestrains from [registerPrestrain]: the phase error a
 * lattice built at `32/3` bp per turn accumulates over one 8, 16 or 32 bp domain against a duplex
 * that prefers 10.5. Those are **per-domain** offsets. But every domain's error carries the **same
 * sign** — Rothemund (2006, Suppl. Note S2, read directly): *"The use of 16 bases to represent 1.5
 * turns of DNA … means that the helical domains between crossovers are slightly overtwisted or
 * undertwisted"* — so the error **accumulates** along a duplex, and a per-domain offset is a lower
 * bound on what the far end of a row carries, not a candidate value for it.
 *
 * ## What limits the accumulation
 *
 * The duplex's own torsion. Let `u(x)` be the azimuthal phase by which the duplex lags the phase
 * the crossover lattice was laid out at, so `u = 0` is a duplex wound to the **design** twist and
 * `u′ = Δω` a duplex wound to its **natural** twist. Every crossover the duplex carries penalises
 * `u` at its own station, because a crossover whose backbones do not meet at the tangent point is
 * a crossover built at a relative roll. Smearing the crossovers at their mean contour spacing `p`,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`E = ∫ [½C(u′ − Δω)² + ½(k_θ/p) u²] dx`,
 *
 * whose Euler-Lagrange equation is `u″ = u/λ²` with **decay length** `λ = √(C p/k_θ)` and whose
 * *natural* boundary condition at a **free duplex end** — which is exactly what a row end is — is
 * `u′ = Δω`. On `[−L/2, +L/2]` that gives
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`u(x) = Δω λ sinh(x/λ)/cosh(L/2λ)`, &nbsp;&nbsp; `u(±L/2) = ±Δω λ tanh(L/2λ)`.
 *
 * `u` is **odd about the row centre** — a symmetry, not a fit — and it has the two limits the
 * problem must have: `Δω L/2` as `k_θ → 0` (nothing holds the duplex in register, the whole
 * accumulation surfaces at the end) and `0` as `k_θ → ∞`.
 *
 * ## The sign, which composes to something counter-intuitive
 *
 * Rothemund's **glide symmetry** — *"the local configuration of crossovers in one column is
 * identical to that of crossovers in the next column over after a translation and a 'flip'"* —
 * reverses the out-of-plane sense column to column, and a column of parity `p` serves the
 * interfaces of parity `p` (`C-0015`), so the fold a given azimuthal error induces carries a factor
 * `(−1)^b` in the interface index. That is the corrugation which makes a sheet *"on average, flat"*.
 * A boustrophedon's raster turns alternate ends, so the row-end crossover of interface `b` sits at
 * `x = (−1)^b L/2`, where `u = (−1)^b u_max`. **The two flips cancel**: every row-end crossover
 * carries the *same* sign, which is `C-0104`'s **uniform** distribution — its adverse one — and not
 * its opposed-ends one. See [corrugatedPrestrain].
 *
 * Units: `C` **pN·nm²**, `k_θ` **pN·nm/rad**, lengths **nm**, `Δω` **rad/nm**, angles **rad**.
 */
data class EdgeTwistRelief(
    /** `C`, the duplex torsional rigidity in `pN·nm²`. */
    val torsionalRigidity: Double,
    /** `k_θ`, the crossover dihedral spring in `pN·nm/rad`. */
    val hingeStiffness: Double,
    /** `p`, the mean contour in nm of one duplex per crossover it carries. */
    val crossoverSpacing: Double,
    /** `L`, the duplex length in nm — the row length, both of whose ends are free. */
    val rowLength: Double
) {

    init {
        require(torsionalRigidity > 0.0 && torsionalRigidity.isFinite()) {
            "torsionalRigidity must be a positive finite pN nm^2, was: $torsionalRigidity"
        }
        require(hingeStiffness > 0.0 && hingeStiffness.isFinite()) {
            "hingeStiffness must be a positive finite pN nm/rad, was: $hingeStiffness"
        }
        require(crossoverSpacing > 0.0 && crossoverSpacing.isFinite()) {
            "crossoverSpacing must be a positive finite nm, was: $crossoverSpacing"
        }
        require(rowLength > 0.0 && rowLength.isFinite()) {
            "rowLength must be a positive finite nm, was: $rowLength"
        }
    }

    /** `λ = √(C p/k_θ)` in nm — the contour over which a free end relieves its register error. */
    val decayLength: Double = sqrt(torsionalRigidity * crossoverSpacing / hingeStiffness)

    /** The register error in radians a **free duplex end** carries at a twist rate [mismatch]. */
    fun endResidual(mismatch: Double): Double {
        require(mismatch.isFinite()) { "mismatch must be finite, was: $mismatch" }
        return mismatch * decayLength * tanh(rowLength / (2.0 * decayLength))
    }

    /**
     * The register error in radians at [x], measured from the **row centre**, at a twist rate
     * [mismatch]. Exactly odd in [x], and equal to [endResidual] at `x = rowLength/2`.
     */
    fun residualAt(x: Double, mismatch: Double): Double {
        require(x.isFinite()) { "x must be finite, was: $x" }
        require(mismatch.isFinite()) { "mismatch must be finite, was: $mismatch" }
        val a = x / decayLength
        val b = rowLength / (2.0 * decayLength)
        // sinh(a)/cosh(b), written so that a large b cannot overflow either transcendental
        val ratio = (exp(a - b) - exp(-a - b)) / (1.0 + exp(-2.0 * b))
        return mismatch * decayLength * ratio
    }

    /** The un-relieved accumulation `Δω L/2` in radians — the `k_θ → 0` limit of [endResidual]. */
    fun rigidLimit(mismatch: Double): Double {
        require(mismatch.isFinite()) { "mismatch must be finite, was: $mismatch" }
        return mismatch * rowLength / 2.0
    }

    /**
     * [endResidual] recomputed by minimising the **discrete** chain energy over [segments]
     * elements, as the independent convergence check the closed form needs. Lumped crossover
     * springs, a tridiagonal symmetric positive-definite system, Thomas elimination.
     */
    fun discreteEndResidual(mismatch: Double, segments: Int): Double {
        require(mismatch.isFinite()) { "mismatch must be finite, was: $mismatch" }
        require(segments >= 2) { "segments must be at least 2, was: $segments" }
        val n = segments
        val h = rowLength / n
        val c = torsionalRigidity / h
        val kappa = hingeStiffness / crossoverSpacing
        val diagonal = DoubleArray(n + 1)
        val off = DoubleArray(n) { -c }
        val rhs = DoubleArray(n + 1)
        for (j in 0..n) {
            val lumped = if (j == 0 || j == n) h / 2.0 else h
            val neighbours = if (j == 0 || j == n) 1 else 2
            diagonal[j] = neighbours * c + kappa * lumped
        }
        rhs[0] = -torsionalRigidity * mismatch
        rhs[n] = torsionalRigidity * mismatch
        // Thomas elimination on a symmetric tridiagonal system
        val cPrime = DoubleArray(n)
        val dPrime = DoubleArray(n + 1)
        cPrime[0] = off[0] / diagonal[0]
        dPrime[0] = rhs[0] / diagonal[0]
        for (j in 1..n) {
            val denominator = diagonal[j] - off[j - 1] * cPrime[j - 1]
            if (j < n) cPrime[j] = off[j] / denominator
            dPrime[j] = (rhs[j] - off[j - 1] * dPrime[j - 1]) / denominator
        }
        val u = DoubleArray(n + 1)
        u[n] = dPrime[n]
        for (j in n - 1 downTo 0) u[j] = dPrime[j] - cPrime[j] * u[j + 1]
        return u[n]
    }
}

/**
 * The twist rate mismatch in **rad/nm** between a duplex that prefers [naturalTwistPerBase] and a
 * lattice laid out at [designTwistPerBase], both in degrees per base, at a rise of [risePerBase].
 *
 * Signed: positive means the duplex wants **more** twist than the lattice provides, which is the
 * square lattice's own case (34.2857 against 33.75 degrees per base).
 */
fun twistRateMismatch(
    designTwistPerBase: Double,
    naturalTwistPerBase: Double,
    risePerBase: Double
): Double {
    require(designTwistPerBase > 0.0) {
        "designTwistPerBase must be positive, was: $designTwistPerBase"
    }
    require(naturalTwistPerBase > 0.0) {
        "naturalTwistPerBase must be positive, was: $naturalTwistPerBase"
    }
    require(risePerBase > 0.0) { "risePerBase must be positive, was: $risePerBase" }
    return (naturalTwistPerBase - designTwistPerBase) * PI / 180.0 / risePerBase
}

/**
 * The prestrain in radians that the crossover of [interfaceIndex] at [x] is built at, under
 * [model]'s boundary layer and Rothemund's glide symmetry — `(−1)^b u(x)`.
 *
 * `x` is measured from the **row centre**. The alternation is the corrugation that makes the sheet
 * *"on average, flat"*; it is not a free choice.
 */
fun corrugatedPrestrain(
    model: EdgeTwistRelief,
    mismatch: Double,
    interfaceIndex: Int,
    x: Double
): Double {
    require(interfaceIndex >= 0) { "interfaceIndex must not be negative, was: $interfaceIndex" }
    val glide = if (interfaceIndex % 2 == 0) 1.0 else -1.0
    return glide * model.residualAt(x, mismatch)
}

/**
 * The prestrain in radians at which each of a crossover's two phosphate bonds carries [force] pN,
 * on a chord of half-width [leverArm].
 *
 * `k_θ = 2 k_bond a²` (`C-0009` from Chen et al.; `C-0029`'s two strand termini), so the bond
 * extension is `a θ₀` and the force `k_θ θ₀/(2a)`. Inverted, this is the **rupture ceiling** on a
 * prestrain: it is a bound on an angle written from a bound on a force.
 */
fun prestrainAtBondForce(force: Double, hinge: Double, leverArm: Double): Double {
    require(force >= 0.0 && force.isFinite()) { "force must be non-negative, was: $force" }
    require(hinge > 0.0 && hinge.isFinite()) { "hinge must be positive, was: $hinge" }
    require(leverArm > 0.0 && leverArm.isFinite()) { "leverArm must be positive, was: $leverArm" }
    return 2.0 * leverArm * force / hinge
}

/** The inverse of [prestrainAtBondForce]: the per-bond force in pN at a prestrain of [angle]. */
fun bondForceAtPrestrain(angle: Double, hinge: Double, leverArm: Double): Double {
    require(angle.isFinite()) { "angle must be finite, was: $angle" }
    require(hinge > 0.0 && hinge.isFinite()) { "hinge must be positive, was: $hinge" }
    require(leverArm > 0.0 && leverArm.isFinite()) { "leverArm must be positive, was: $leverArm" }
    return hinge * angle / (2.0 * leverArm)
}

/**
 * The torsional slack in radians that [bases] unpaired scaffold bases introduce, at a natural twist
 * of [naturalTwistPerBase] degrees per base.
 *
 * Rothemund's own remedy for an unrelieved edge strain — *"one or two scaffold bases could be left
 * unpaired and allowed to form a hairpin that should relax the crossover"* — read as a **scale**:
 * a remedy sized at one to two bases prices the strain it is meant to relieve at one to two bases
 * of twist.
 */
fun unpairedBaseRelief(bases: Double, naturalTwistPerBase: Double): Double {
    require(bases >= 0.0 && bases.isFinite()) { "bases must be non-negative, was: $bases" }
    require(naturalTwistPerBase > 0.0) {
        "naturalTwistPerBase must be positive, was: $naturalTwistPerBase"
    }
    return bases * naturalTwistPerBase * PI / 180.0
}
