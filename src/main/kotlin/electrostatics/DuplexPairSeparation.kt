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
import com.xemantic.nano.plentyofroom.anchoring.DuplexSteric
import com.xemantic.nano.plentyofroom.brush.bracketedRoot
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * `T-139` — the pair interaction of two **unbonded** B-DNA duplexes in 2 mM MgCl₂, in the three
 * geometries the plan model's single symbol `d` is actually used in.
 *
 * ## Why three geometries and not one
 *
 * `C-0076` separates the three roles the symbol `d = 2.69 nm` plays — `ROW_PITCH`, `BODY_WIDTH`
 * and `COLLINEAR_CLEARANCE` — and shows that only the first is what the SAXS lattice constant
 * measures. On the actual bodies those roles are three *different* pair geometries, and their
 * closed forms do not differ by a constant:
 *
 * | role | geometry | closed form |
 * |---|---|---|
 * | `ROW_PITCH` | **parallel** cylinders | `E/L = 2 τ² l_B k_BT K₀(κD)` |
 * | `C-0066` bound 4 | **crossed** at 90° | `E = 2π τ² l_B k_BT e^(−κD)/κ` |
 * | `C-0069` `Q5` | **coaxial**, end to end | `E = τ² l_B k_BT [e^(−κg)/κ − g E₁(κg)]` |
 *
 * All three follow from the same screened Coulomb kernel `τ² l_B k_BT e^(−κr)/r` between line
 * elements; only the double integral over the two bodies' arc lengths differs. Each is verified
 * against a direct numerical quadrature of that kernel — two independently written routes to one
 * number, which is this file's gate 3.
 *
 * ## The convention that must not slip
 *
 * `D` is **axis to axis** everywhere; `D_s` is **surface to surface**, and the surface is the
 * **phosphate locus** at `T-71`'s measured radius, not the 2.0 nm hard-body diameter the
 * osmotic-stress literature fits its hydration curves against. The two differ by 0.183 nm, which
 * is 41 % of the quantity `T-139` is asked to resolve.
 *
 * ## What this file does NOT claim
 *
 * Debye-Hückel is a linearised theory and B-DNA at 2 mM MgCl₂ violates its premise —
 * [DuplexPair.reducedSurfacePotential] is above one even after Manning renormalisation, and
 * `C-0005` puts the one-loop correction on the *mean field itself* at 123–214 % with `Ξ = 17–24`
 * for Mg²⁺ at a DNA surface and **no systematic theory**. Every number here is therefore an
 * order-of-magnitude bracket, and the claim it supports rests on the **range** of the interaction
 * rather than on its magnitude.
 */

// ---------------------------------------------------------------- special functions

private const val EULER_MASCHERONI: Double = 0.5772156649015329

/**
 * The modified Bessel function of the second kind, order zero — Abramowitz & Stegun 9.8.5/9.8.6.
 *
 * Absolute error `< 1e−8` for `x ≤ 2` and `< 1.9e−7` in `√x e^x K₀(x)` above it. Kotlin's standard
 * library has no Bessel functions and this project has no numerics dependency that supplies them,
 * so the polynomial approximations are transcribed here and checked against A&S Table 9.8 in
 * `DuplexPairSeparationTest` (gate 5).
 *
 * @throws IllegalArgumentException if [x] is not positive — `K₀` diverges at the origin.
 */
fun modifiedBesselK0(x: Double): Double {
    require(x > 0.0) { "x must be positive, was: $x" }
    return if (x <= 2.0) {
        val t = x * x / 4.0
        -ln(x / 2.0) * modifiedBesselI0(x) - EULER_MASCHERONI +
            t * (0.42278420 + t * (0.23069756 + t * (0.03488590 +
                t * (0.00262698 + t * (0.00010750 + t * 0.00000740)))))
    } else {
        val y = 2.0 / x
        (1.25331414 + y * (-0.07832358 + y * (0.02189568 + y * (-0.01062446 +
            y * (0.00587872 + y * (-0.00251540 + y * 0.00053208)))))) / (sqrt(x) * exp(x))
    }
}

/**
 * The modified Bessel function of the second kind, order one — A&S 9.8.7/9.8.8.
 *
 * @throws IllegalArgumentException if [x] is not positive.
 */
fun modifiedBesselK1(x: Double): Double {
    require(x > 0.0) { "x must be positive, was: $x" }
    return if (x <= 2.0) {
        val t = x * x / 4.0
        (x * ln(x / 2.0) * modifiedBesselI1(x) + 1.0 +
            t * (0.15443144 + t * (-0.67278579 + t * (-0.18156897 +
                t * (-0.01919402 + t * (-0.00110404 + t * (-0.00004686))))))) / x
    } else {
        val y = 2.0 / x
        (1.25331414 + y * (0.23498619 + y * (-0.03655620 + y * (0.01504268 +
            y * (-0.00780353 + y * (0.00325614 + y * (-0.00068245))))))) / (sqrt(x) * exp(x))
    }
}

/** The modified Bessel function of the first kind, order zero — A&S 9.8.1/9.8.2. */
private fun modifiedBesselI0(x: Double): Double {
    val a = kotlin.math.abs(x)
    return if (a < 3.75) {
        val u = (x / 3.75) * (x / 3.75)
        1.0 + u * (3.5156229 + u * (3.0899424 + u * (1.2067492 +
            u * (0.2659732 + u * (0.0360768 + u * 0.0045813)))))
    } else {
        val y = 3.75 / a
        (exp(a) / sqrt(a)) * (0.39894228 + y * (0.01328592 + y * (0.00225319 +
            y * (-0.00157565 + y * (0.00916281 + y * (-0.02057706 + y * (0.02635537 +
                y * (-0.01647633 + y * 0.00392377))))))))
    }
}

/** The modified Bessel function of the first kind, order one — A&S 9.8.3/9.8.4. */
private fun modifiedBesselI1(x: Double): Double {
    val a = kotlin.math.abs(x)
    val value = if (a < 3.75) {
        val u = (x / 3.75) * (x / 3.75)
        a * (0.5 + u * (0.87890594 + u * (0.51498869 + u * (0.15084934 +
            u * (0.02658733 + u * (0.00301532 + u * 0.00032411))))))
    } else {
        val y = 3.75 / a
        val head = 0.02282967 + y * (-0.02895312 + y * (0.01787654 + y * (-0.00420059)))
        val tail = 0.39894228 + y * (-0.03988024 + y * (-0.00362018 +
            y * (0.00163801 + y * (-0.01031555 + y * head))))
        (exp(a) / sqrt(a)) * tail
    }
    return if (x < 0.0) -value else value
}

/**
 * The exponential integral `E₁(x) = ∫_x^∞ e^(−t)/t dt` — A&S 5.1.53 and 5.1.56.
 *
 * Absolute error `< 2e−7` below one, relative `< 2e−8` above it. It is what closes the **coaxial**
 * end-to-end geometry, whose double integral over two half-lines produces `g E₁(κg)` beside the
 * ordinary exponential.
 *
 * @throws IllegalArgumentException if [x] is not positive.
 */
fun exponentialIntegralE1(x: Double): Double {
    require(x > 0.0) { "x must be positive, was: $x" }
    return if (x <= 1.0) {
        -ln(x) + (-0.57721566 + x * (0.99999193 + x * (-0.24991055 +
            x * (0.05519968 + x * (-0.00976004 + x * 0.00107857)))))
    } else {
        val numerator = x * x * x * x + 8.5733287401 * x * x * x +
            18.059016973 * x * x + 8.6347608925 * x + 0.2677737343
        val denominator = x * x * x * x + 9.5733223454 * x * x * x +
            25.6329561486 * x * x + 21.0996530827 * x + 3.9584969228
        numerator / (denominator * x * exp(x))
    }
}

// ---------------------------------------------------------------- the charged pair

/**
 * The factor by which a **cylinder** of reduced radius `κR` interacts more strongly than a line
 * charge of the same total density — `1/(κR K₁(κR))²`.
 *
 * A uniformly charged cylinder's exterior potential in Debye-Hückel is `A K₀(κr)` with
 * `A = σ_s/(εκK₁(κR))`, which is exactly a line charge of density `τ/(κR K₁(κR))`. So a pair
 * energy computed on line charges is multiplied by the square. It tends to **one** as `κR → 0`
 * and is `1.12` for B-DNA at 2 mM MgCl₂: a fat cylinder's charge sits closer to its neighbour
 * than a line at its axis would.
 *
 * @throws IllegalArgumentException if [reducedRadius] is not positive.
 */
fun finiteRadiusChargeFactor(reducedRadius: Double): Double {
    require(reducedRadius > 0.0) { "reducedRadius must be positive, was: $reducedRadius" }
    val equivalent = reducedRadius * modifiedBesselK1(reducedRadius)
    return 1.0 / (equivalent * equivalent)
}

/**
 * Two identical B-DNA duplexes, as charged cylinders in a 1:1-screened Debye-Hückel electrolyte.
 *
 * @param helixRadius the phosphate radius in nm — `T-71`'s **MEASURED** 0.9086 nm by default,
 *          because that is the locus the exclusion width is a statement about.
 * @param bareLinearChargeDensity `τ = 2/rise` in e/nm — 5.88 for B-DNA, **DERIVED** from the rise.
 * @param counterionValency the valency the Manning condensation is computed at; 2 for Mg²⁺.
 * @param bjerrumLength `l_B` in nm.
 * @param temperature in K.
 */
data class DuplexPair(
    val helixRadius: Double = DuplexSteric.MEASURED_RADIUS,
    val bareLinearChargeDensity: Double = 2.0 / 0.34,
    val counterionValency: Int = 2,
    val bjerrumLength: Double = bjerrumLength(),
    val temperature: Double = ROOM_TEMPERATURE
) {

    init {
        require(helixRadius > 0.0) { "helixRadius must be positive, was: $helixRadius" }
        require(bareLinearChargeDensity > 0.0) {
            "bareLinearChargeDensity must be positive, was: $bareLinearChargeDensity"
        }
        require(counterionValency > 0) {
            "counterionValency must be positive, was: $counterionValency"
        }
        require(bjerrumLength > 0.0) { "bjerrumLength must be positive, was: $bjerrumLength" }
        require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
    }

    /** `k_BT` in pN·nm at this pair's temperature. */
    val thermalEnergy: Double get() = thermalEnergy(temperature)

    /**
     * The Manning parameter `ξ_M = l_B/b`, `b` being the axial charge spacing — **4.20** for
     * B-DNA in water at 300 K, in Manning's own **valency-free** convention (`DnaOrigamiTile`
     * carries the same warning: Naji et al. fold the valency in and quote 8.2 for the same DNA).
     */
    val manningParameter: Double get() = bjerrumLength * bareLinearChargeDensity

    /**
     * The line charge that survives counterion condensation, in e/nm — `τ/(q ξ_M)`, i.e.
     * **0.700 e/nm** for Mg²⁺ against a bare 5.88. Exactly half the monovalent value, because the
     * surviving fraction goes as `1/q`.
     */
    val effectiveLinearChargeDensity: Double
        get() {
            val product = counterionValency * manningParameter
            return if (product <= 1.0) bareLinearChargeDensity
            else bareLinearChargeDensity / product
        }

    /**
     * The Debye-Hückel surface potential in units of `k_BT/e` —
     * `y₀ = 2 τ l_B K₀(κR)/(κR K₁(κR))`.
     *
     * **It is above one for this material**, which is the premise check Debye-Hückel fails here.
     * Reported rather than suppressed: it is why this file's numbers are a bracket.
     */
    fun reducedSurfacePotential(inverseDebyeLength: Double): Double {
        require(inverseDebyeLength > 0.0) {
            "inverseDebyeLength must be positive, was: $inverseDebyeLength"
        }
        val kr = inverseDebyeLength * helixRadius
        return 2.0 * effectiveLinearChargeDensity * bjerrumLength *
            modifiedBesselK0(kr) / (kr * modifiedBesselK1(kr))
    }

    private fun kernelAmplitude(finiteRadius: Boolean, inverseDebyeLength: Double): Double {
        val tau = effectiveLinearChargeDensity
        val bare = tau * tau * bjerrumLength * thermalEnergy
        return if (finiteRadius) {
            bare * finiteRadiusChargeFactor(inverseDebyeLength * helixRadius)
        } else bare
    }

    /**
     * The interaction energy per unit length of two **parallel** duplexes at axis separation
     * [separation], in pN·nm per nm — `2 τ² l_B k_BT K₀(κD)`.
     *
     * This is the `ROW_PITCH` geometry, and it is the only one the SAXS lattice constant is a
     * measurement of. It is used here to **calibrate what the host sheet demonstrably pays**:
     * the sheet holds fifteen duplexes at 2.69 nm over 40 nm, so the energy density at 2.69 nm is
     * an empirical statement about what a crossover can afford.
     */
    fun parallelScreenedCoulombEnergyPerLength(
        separation: Double,
        inverseDebyeLength: Double,
        finiteRadius: Boolean = true
    ): Double {
        require(separation > 0.0) { "separation must be positive, was: $separation" }
        require(inverseDebyeLength > 0.0) {
            "inverseDebyeLength must be positive, was: $inverseDebyeLength"
        }
        return 2.0 * kernelAmplitude(finiteRadius, inverseDebyeLength) *
            modifiedBesselK0(inverseDebyeLength * separation)
    }

    /**
     * The total interaction energy of two duplexes **crossed at 90°** whose axes pass at closest
     * distance [separation], in pN·nm — `2π τ² l_B k_BT e^(−κD)/κ`.
     *
     * The double integral over the two axes reduces exactly: with `r² = D² + s² + t²` the
     * transverse coordinates go to polar and `∫₀^∞ e^(−κ√(D²+ρ²))/√(D²+ρ²) ρ dρ = e^(−κD)/κ`.
     * **This is `C-0066`'s bound-4 geometry** — a vertical tie's flank against a horizontal arm.
     */
    fun crossedScreenedCoulombEnergy(
        separation: Double,
        inverseDebyeLength: Double,
        finiteRadius: Boolean = true
    ): Double {
        require(separation > 0.0) { "separation must be positive, was: $separation" }
        require(inverseDebyeLength > 0.0) {
            "inverseDebyeLength must be positive, was: $inverseDebyeLength"
        }
        return 2.0 * PI * kernelAmplitude(finiteRadius, inverseDebyeLength) *
            exp(-inverseDebyeLength * separation) / inverseDebyeLength
    }

    /**
     * The total interaction energy of two **coaxial** duplexes lying end to end with an axial gap
     * [endGap] between their terminal charges, in pN·nm —
     * `τ² l_B k_BT [e^(−κg)/κ − g E₁(κg)]`.
     *
     * **This is `C-0069`'s `Q5` geometry**, and it is the one nobody has priced. Substituting
     * `u = s + t` collapses the double integral over two half-lines to
     * `∫₀^∞ u e^(−κ(g+u))/(g+u) du`, which is elementary.
     *
     * **At contact it is FINITE** — `τ² l_B k_BT/κ`, because `g E₁(κg) → 0` — so two duplexes
     * brought blunt end to blunt end pay a bounded electrostatic price. That is the whole reason
     * a coaxial gap is not a steric exclusion problem.
     *
     * @param endGap the axial gap in nm; **zero is admissible** and returns the contact value.
     */
    fun coaxialScreenedCoulombEnergy(
        endGap: Double,
        inverseDebyeLength: Double
    ): Double {
        require(endGap >= 0.0) { "endGap must not be negative, was: $endGap" }
        require(inverseDebyeLength > 0.0) {
            "inverseDebyeLength must be positive, was: $inverseDebyeLength"
        }
        val tau = effectiveLinearChargeDensity
        val amplitude = tau * tau * bjerrumLength * thermalEnergy
        val reduced = inverseDebyeLength * endGap
        val correction = if (endGap == 0.0) 0.0 else endGap * exponentialIntegralE1(reduced)
        return amplitude * (exp(-reduced) / inverseDebyeLength - correction)
    }
}

// ---------------------------------------------------------------- van der Waals

/**
 * The van der Waals energy of two **crossed** cylinders at surface separation [surfaceSeparation],
 * in pN·nm — `−A √(R₁R₂)/(6 D_s)`, negative because it attracts.
 *
 * `1 zJ = 1 pN·nm` exactly, so a Hamaker constant in zJ goes in unconverted (`C-0021`).
 *
 * @throws IllegalArgumentException if [surfaceSeparation] or [radius] is not positive.
 */
fun crossedCylinderVanDerWaalsEnergy(
    hamaker: Double,
    radius: Double,
    surfaceSeparation: Double
): Double {
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    require(surfaceSeparation > 0.0) {
        "surfaceSeparation must be positive, was: $surfaceSeparation"
    }
    return -hamaker * radius / (6.0 * surfaceSeparation)
}

/**
 * The van der Waals energy per unit length of two **parallel** cylinders of equal radius, in
 * pN·nm per nm — `−A √R/(24 D_s^(3/2))`.
 */
fun parallelCylinderVanDerWaalsEnergyPerLength(
    hamaker: Double,
    radius: Double,
    surfaceSeparation: Double
): Double {
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    require(surfaceSeparation > 0.0) {
        "surfaceSeparation must be positive, was: $surfaceSeparation"
    }
    return -hamaker * sqrt(radius) / (24.0 * surfaceSeparation * sqrt(surfaceSeparation))
}

// ---------------------------------------------------------------- the measured short-range force

/**
 * The **measured** DNA–DNA osmotic-stress equation of state, in **interaxial** coordinates.
 *
 * `Π(d) = Π_R e^(−d/λ)`, the repulsion-only reading of Meng, Timsina, Bull, Andresen & Qiu,
 * *Biophys. J.* **118**:3019 (2020) — **CITED, MEASURED, read directly**, with the verbatim passage
 * and the read flag in `gpd/data/T-139-dna-dna-force-literature.md`.
 *
 * ## Why this and not a Derjaguin hydration fit in surface coordinates
 *
 * The osmotic-stress literature reports `Π` against the **interaxial** spacing of a hexagonal
 * array, which needs no hard-diameter convention at all. Converting it into a surface separation
 * would require choosing between the field's 2.0 nm and `T-71`'s measured 1.8173 nm — a 0.183 nm
 * difference, 41 % of the quantity `T-139` is asked to resolve. Working in `d` avoids the choice.
 *
 * ## Array to pair
 *
 * A hexagonal array of lattice constant `d` has area `(√3/2)d²` per molecule and three pair
 * interactions per molecule, so `Π = −∂F/∂A` gives the **exact** conversion
 * `f_∥(d) = Π(d) d/√3` for the nearest-neighbour pair force per unit length, and integrating,
 * `g_∥(d) = (Π_R/√3) λ (d + λ) e^(−d/λ)`. Both are verified against numerical routes in gate 3.
 *
 * @param repulsionAmplitude `Π_R` in pN/nm² (= MPa), extrapolated to `d = 0`.
 * @param decayLength `λ` in nm.
 */
data class OsmoticStressEquationOfState(
    val repulsionAmplitude: Double,
    val decayLength: Double
) {

    init {
        require(repulsionAmplitude >= 0.0) {
            "repulsionAmplitude must not be negative, was: $repulsionAmplitude"
        }
        require(decayLength > 0.0) { "decayLength must be positive, was: $decayLength" }
    }

    /** The array osmotic pressure at interaxial spacing [separation], in pN/nm². */
    fun arrayPressure(separation: Double): Double {
        require(separation > 0.0) { "separation must be positive, was: $separation" }
        return repulsionAmplitude * exp(-separation / decayLength)
    }

    /**
     * The nearest-neighbour pair force per unit length in a hexagonal array, `Π d/√3`, in pN/nm.
     * Positive is repulsive.
     */
    fun parallelPairForcePerLength(separation: Double): Double =
        arrayPressure(separation) * separation / sqrt(3.0)

    /**
     * The parallel pair interaction energy per unit length, `∫_d^∞ f_∥`, in pN·nm per nm —
     * `(Π_R/√3) λ (d + λ) e^(−d/λ)`.
     */
    fun parallelPairEnergyPerLength(separation: Double): Double {
        require(separation > 0.0) { "separation must be positive, was: $separation" }
        return repulsionAmplitude / sqrt(3.0) * decayLength *
            (separation + decayLength) * exp(-separation / decayLength)
    }

    /**
     * The Derjaguin factor turning a parallel energy per length into a **crossed**-cylinder energy,
     * `2√(πRλ)`, in nm.
     *
     * It is independent of the separation because both geometries carry the same exponential, so
     * the whole crossed/parallel conversion is one length. Valid while `λ ≪ R`, which for
     * `λ = 0.24 nm` against `R = 1.0 nm` it is.
     */
    fun crossingLength(hardRadius: Double): Double {
        require(hardRadius > 0.0) { "hardRadius must be positive, was: $hardRadius" }
        return 2.0 * sqrt(PI * hardRadius * decayLength)
    }

    /** The crossed-cylinder pair energy at interaxial closest approach [separation], in pN·nm. */
    fun crossedPairEnergy(separation: Double, hardRadius: Double): Double =
        crossingLength(hardRadius) * parallelPairEnergyPerLength(separation)

    /**
     * The flat-flat pressure implied by [parallelPairForcePerLength] through Derjaguin,
     * `f_∥/√(πRλ)`, as a function of the **surface** separation on a body of radius [hardRadius].
     *
     * The one place a surface convention is unavoidable, because a **coaxial** pair presents two
     * flat end faces and there is no interaxial coordinate at all.
     */
    fun flatPressure(surfaceSeparation: Double, hardRadius: Double): Double {
        require(hardRadius > 0.0) { "hardRadius must be positive, was: $hardRadius" }
        val interaxialEquivalent = surfaceSeparation + 2.0 * hardRadius
        return parallelPairForcePerLength(interaxialEquivalent) /
            sqrt(PI * hardRadius * decayLength)
    }

    /**
     * The energy of two coaxial duplexes' **blunt end faces** across an axial gap, in pN·nm —
     * the flat-flat energy per area times one duplex cross-section.
     *
     * At `T-139`'s separations this is `1e−4` pN·nm and utterly negligible, which is the point:
     * the short-range force has `λ = 0.24 nm` and a coaxial gap of 2.7 nm is eleven decay lengths.
     */
    fun coaxialFaceEnergy(axialGap: Double, hardRadius: Double): Double =
        flatPressure(axialGap, hardRadius) * decayLength * PI * hardRadius * hardRadius
}

// ---------------------------------------------------------------- independent quadratures

/**
 * The crossed-rod energy by **direct two-dimensional quadrature** of the screened Coulomb kernel.
 *
 * Deliberately written from the kernel rather than from the closed form: gate 3 asserts the two
 * agree, and nothing in the construction forces them to. Composite Simpson on `[−L, L]²`.
 */
fun crossedRodQuadrature(
    pair: DuplexPair,
    separation: Double,
    inverseDebyeLength: Double,
    halfLength: Double,
    steps: Int
): Double {
    require(steps > 0 && steps % 2 == 0) { "steps must be positive and even, was: $steps" }
    val tau = pair.effectiveLinearChargeDensity
    val amplitude = tau * tau * pair.bjerrumLength * pair.thermalEnergy
    val h = 2.0 * halfLength / steps
    var total = 0.0
    for (i in 0..steps) {
        val s = -halfLength + i * h
        val wi = simpsonWeight(i, steps)
        for (j in 0..steps) {
            val t = -halfLength + j * h
            val r = sqrt(separation * separation + s * s + t * t)
            total += wi * simpsonWeight(j, steps) * exp(-inverseDebyeLength * r) / r
        }
    }
    return amplitude * total * h * h / 9.0
}

/**
 * The coaxial end-to-end energy by direct two-dimensional quadrature over the two half-lines.
 * The independent route to [DuplexPair.coaxialScreenedCoulombEnergy].
 */
fun coaxialRodQuadrature(
    pair: DuplexPair,
    endGap: Double,
    inverseDebyeLength: Double,
    halfLength: Double,
    steps: Int
): Double {
    require(steps > 0 && steps % 2 == 0) { "steps must be positive and even, was: $steps" }
    require(endGap > 0.0) { "endGap must be positive for the quadrature, was: $endGap" }
    val tau = pair.effectiveLinearChargeDensity
    val amplitude = tau * tau * pair.bjerrumLength * pair.thermalEnergy
    val h = halfLength / steps
    var total = 0.0
    for (i in 0..steps) {
        val s = i * h
        val wi = simpsonWeight(i, steps)
        for (j in 0..steps) {
            val t = j * h
            val r = endGap + s + t
            total += wi * simpsonWeight(j, steps) * exp(-inverseDebyeLength * r) / r
        }
    }
    return amplitude * total * h * h / 9.0
}

/**
 * The parallel pair energy per unit length by **numerical integration of the pair force** — the
 * independent route to [OsmoticStressEquationOfState.parallelPairEnergyPerLength].
 */
fun parallelPairEnergyQuadrature(
    state: OsmoticStressEquationOfState,
    separation: Double,
    upper: Double,
    steps: Int
): Double {
    require(steps > 0 && steps % 2 == 0) { "steps must be positive and even, was: $steps" }
    require(upper > separation) { "upper must exceed separation, was: $upper" }
    val h = (upper - separation) / steps
    var total = 0.0
    for (i in 0..steps) {
        total += simpsonWeight(i, steps) *
            state.parallelPairForcePerLength(separation + i * h)
    }
    return total * h / 3.0
}

/**
 * The array osmotic pressure recovered from the pair free energy by `Π = −∂F/∂A` with
 * `F = 3 g_∥(d)` and `A = (√3/2)d²`, differenced numerically — the independent route to the
 * array-to-pair conversion, which is the one step in this file that could silently carry a wrong
 * lattice factor.
 */
fun arrayPressureFromPairEnergy(
    state: OsmoticStressEquationOfState,
    separation: Double,
    halfStep: Double = 1e-6
): Double {
    val sampleSeparation = 2.0 * halfStep
    fun freeEnergy(d: Double) = 3.0 * state.parallelPairEnergyPerLength(d)
    fun area(d: Double) = sqrt(3.0) / 2.0 * d * d
    val dF = (freeEnergy(separation + halfStep) - freeEnergy(separation - halfStep)) /
        sampleSeparation
    val dA = (area(separation + halfStep) - area(separation - halfStep)) / sampleSeparation
    return -dF / dA
}

/**
 * The crossed-cylinder energy by direct quadrature of the flat-flat energy per area over the
 * paraboloidal gap `h = D_s + (x² + y²)/(2R)` — the independent route to
 * [OsmoticStressEquationOfState.crossedPairEnergy].
 */
fun derjaguinCrossedQuadrature(
    state: OsmoticStressEquationOfState,
    separation: Double,
    hardRadius: Double,
    half: Double,
    steps: Int
): Double {
    require(steps > 0 && steps % 2 == 0) { "steps must be positive and even, was: $steps" }
    val surfaceGap = separation - 2.0 * hardRadius
    val h = 2.0 * half / steps
    var total = 0.0
    for (i in 0..steps) {
        val x = -half + i * h
        val wi = simpsonWeight(i, steps)
        for (j in 0..steps) {
            val y = -half + j * h
            val gap = surfaceGap + (x * x + y * y) / (2.0 * hardRadius)
            total += wi * simpsonWeight(j, steps) *
                state.flatPressure(gap, hardRadius) * state.decayLength
        }
    }
    return total * h * h / 9.0
}

private fun simpsonWeight(index: Int, steps: Int): Double = when {
    index == 0 || index == steps -> 1.0
    index % 2 == 1 -> 4.0
    else -> 2.0
}

// ---------------------------------------------------------------- the Gen-1 lattice constants

/**
 * `C-0076`'s placement threshold — `pitch − arm`, the exclusion width at which the placed count
 * of `C-0063`'s upward roots steps from 34 to 22.
 *
 * A lattice quantity with no fitted parameter in it: 32 bp of crossover pitch minus `C-0039`'s
 * exact elastica arm. It is simultaneously `C-0066`'s bound-4 tip gap and the `d` at which
 * `C-0069`'s `Q5` margin reaches zero — `C-0072`'s *"the two knife edges are ONE lattice quantity"*.
 */
const val C0076_PLACEMENT_THRESHOLD: Double = 32 * 0.34 - C0055_ARM_LENGTH

/** `T-71`'s **MEASURED** phosphate-backbone contact distance, `2 × 0.9086 nm`. */
val T71_STERIC_FLOOR: Double get() = DuplexSteric.MEASURED_DIAMETER

/** The SAXS single-layer Bragg lattice constant, the standing plan convention — **CITED**. */
const val SAXS_SHEET_LATTICE_CONSTANT: Double = 2.69

/**
 * The separation in nm above which no plan question in this branch is asked — 20 nm, half the
 * tile's own edge and 7.4× the placement threshold.
 *
 * The bound matters because the *unretarded* Lifshitz `1/D_s` is a power law and every screened
 * term is an exponential, so far enough out the attraction always wins and a secondary minimum
 * always exists. Here it is `0.006 k_BT` at ~30 nm — inside the retardation regime the unretarded
 * law overstates, 170× below thermal energy, and **not a confinement** in `C-0021`'s sense.
 */
const val PLAN_RELEVANT_RANGE: Double = 20.0

/**
 * The **measured** Mg²⁺ DNA–DNA equation of state — Meng, Timsina, Bull, Andresen & Qiu,
 * *Biophys. J.* **118**:3019 (2020), osmotic stress plus X-ray diffraction at **20 mM MgCl₂**.
 *
 * **CITED, MEASURED, read directly**; verbatim passage and read flag in
 * `gpd/data/T-139-dna-dna-force-literature.md`.
 */
object MengMagnesium {

    /**
     * `λ`, the decay length of the short-range repulsion, in nm — **CITED**, *"a universal decay
     * length of 2.4 Å"*.
     *
     * **This is the whole reason the exclusion width is a sharp quantity.** The Debye length at
     * 2 mM MgCl₂ is 3.93 nm, longer than the entire disputed 1.82–3.60 nm bracket, so
     * electrostatics cannot place an edge inside it; a 0.24 nm exponential can, and does.
     */
    const val DECAY_LENGTH: Double = 0.24

    /** `Π_R` in pN/nm² (= MPa) — **CITED**, 201.8 GPa on the Mg²⁺-only curve. */
    const val REPULSION_AMPLITUDE: Double = 201.8e3

    /**
     * `Π_A` in pN/nm² — **CITED**, *"slightly negative"*, −0.3 GPa, and **not used**.
     *
     * Taken literally it puts a zero crossing at 3.125 nm, which contradicts the same paper's own
     * prose that the Mg²⁺-only force curve *"extends to infinity because zero force can only be
     * achieved at infinite DNA-DNA spacing"*. It is quoted to one decimal and its sign is not
     * resolved by the data. Carried here so the bound on how wrong the repulsion-only reading can
     * be is in the source rather than only in the prose.
     */
    const val ATTRACTION_AMPLITUDE: Double = -0.3e3

    /** The lowest interaxial spacing the fit's own data reach, in nm — **CITED**, 24.5 Å. */
    const val DATA_FLOOR: Double = 2.45

    /** The bath the fit was made at, in mM MgCl₂ — **CITED**. */
    const val FITTED_CONCENTRATION: Double = 20.0

    /** The repulsion-only equation of state, which is the working one. */
    val equationOfState: OsmoticStressEquationOfState
        get() = OsmoticStressEquationOfState(REPULSION_AMPLITUDE, DECAY_LENGTH)
}

/**
 * **Blunt-end coaxial stacking** — the interaction that actually lives in `C-0069`'s `Q5` gap.
 *
 * Two duplexes lying end to end on a common axis do not merely fail to repel: their terminal base
 * pairs **stack**, and that is an established DNA-origami motif rather than a speculation. Every
 * constant here is **CITED**, with read flags and verbatim passages in
 * `gpd/data/T-139-blunt-end-stacking-literature.md`.
 *
 * **The consequence for the plan model is a reversal.** The collinear clearance is not charging a
 * steric exclusion — the coaxial pair's electrostatic energy is *finite* at contact and the
 * measured short-range repulsion is eleven decay lengths away at 2.7 nm. What the gap must be
 * long enough to prevent is a **stacking bond between consecutive arms**, and stacking is a
 * contact interaction whose whole range is one to two base-pair rises.
 */
object BluntEndStacking {

    /**
     * The hard cutoff of oxDNA2's coaxial-stacking radial term, in nm — **CITED, read directly**,
     * 5.1108 Å (Henrich, Gutiérrez Fosado, Curk & Ouldridge, *Eur. Phys. J. E* **41**:57, and the
     * LAMMPS `pair_oxdna2` listing). Its minimum is at 3.4072 Å.
     */
    const val OXDNA2_CUTOFF: Double = 0.51108

    /**
     * The end-to-end separation at which the all-atom PMF's force turns **repulsive**, in nm —
     * **CITED, read directly**, *"becomes slightly repulsive after ∼13 Å"* (Maffeo, Luan &
     * Aksimentiev, *NAR* **40**:3812, 2012). The generous end of the range bracket.
     */
    const val ALL_ATOM_REPULSIVE_ONSET: Double = 1.3

    /**
     * The free energy of one blunt-end stack between two **separate origami bodies**, in
     * kcal/mol — **CITED, read directly**, Woo & Rothemund, *Nature Chem.* **3**:620 (2011),
     * SI Table S4, at 1×TAE + 12.5 mM Mg²⁺ and 22 °C. Negative: it **binds**.
     */
    const val WOO_ROTHEMUND_PER_HELIX: Double = -2.63

    /** kcal/mol into pN·nm at 300 K: `4184 J/mol / N_A` in zJ, and `1 zJ = 1 pN·nm`. */
    const val KCAL_PER_MOLE: Double = 4184.0 / AVOGADRO_CONSTANT * 1e21

    /** [WOO_ROTHEMUND_PER_HELIX] in pN·nm. */
    val perStackEnergy: Double get() = WOO_ROTHEMUND_PER_HELIX * KCAL_PER_MOLE
}

/**
 * The Hamaker constant of DNA across water — **CITED**, Dryden et al., *Langmuir* **31**:10145
 * (2015), Lifshitz, **cylinder-cylinder**, read directly for `C-0021`.
 *
 * `CLAUDE.md`: *no planar `A_DNA` exists*, and the `10⁻²⁰ J` in circulation is Rau & Parsegian's
 * own explicit **overestimate**, introduced to prove van der Waals is too weak.
 */
const val DNA_HAMAKER_LOW: Double = 4.33

/** The upper end of [DNA_HAMAKER_LOW]'s bracket, in zJ = pN·nm; the pessimistic reading. */
const val DNA_HAMAKER_HIGH: Double = 5.90

/** The `T-139` reference state: `T-71`'s charge radius, 2 mM MgCl₂, the measured short-range law. */
fun gen1PairState(
    hamaker: Double = DNA_HAMAKER_HIGH,
    shortRange: OsmoticStressEquationOfState = MengMagnesium.equationOfState,
    concentration: Double = 2.0
): Gen1DuplexPairState = Gen1DuplexPairState(
    pair = DuplexPair(),
    buffer = MagnesiumChlorideBuffer(concentration = concentration),
    hamaker = hamaker,
    shortRange = shortRange
)

// ---------------------------------------------------------------- the assembled pair state

/**
 * The three-term pair interaction at `T-139`'s conditions: screened electrostatics, van der Waals
 * and the measured short-range repulsion, in the **crossed** geometry `C-0066`'s bound 4 asks about.
 *
 * The crossed geometry is the one used for the assembled total because it is the *stiffest* of the
 * three — a point contact between two finite bodies — so a width read on it is the conservative
 * one. The parallel and coaxial forms are exposed separately.
 *
 * ## Two radii, and they are not the same number
 *
 * The **charge** sits on the phosphate locus at `T-71`'s measured 0.9086 nm, and that is the radius
 * [DuplexPair] carries. The **hard body** the osmotic-stress hydration fits and Dryden's Hamaker
 * constant are written against is the field's conventional 1.0 nm. The two disagree about where
 * contact is by **0.183 nm**, which is 41 % of the quantity `T-139` is asked to resolve, so they
 * are carried separately and never substituted for one another — `CLAUDE.md`'s *"`a` is three
 * different quantities"* in a new place.
 *
 * @param hardRadius the steric/dielectric radius the Derjaguin transform and the Hamaker constant
 *          assume,
 *          in nm — **CITED**, 1.0 nm, i.e. the 2.0 nm hard diameter the osmotic-stress literature
 *          defines its surface separation against.
 * @param modelFloor the surface separation in nm below which the continuum terms are not carried.
 *          A Lifshitz `1/D_s` and a fitted hydration exponential are both meaningless at a
 *          fraction of a water diameter, and the `1/D_s` divergence would otherwise manufacture a
 *          spurious minimum at contact.
 */
data class Gen1DuplexPairState(
    val pair: DuplexPair = DuplexPair(),
    val buffer: MagnesiumChlorideBuffer = MagnesiumChlorideBuffer(concentration = 2.0),
    val hamaker: Double = 5.90,
    val shortRange: OsmoticStressEquationOfState,
    val hardRadius: Double = 1.0,
    val modelFloor: Double = 0.10
) {

    init {
        require(hamaker >= 0.0) { "hamaker must not be negative, was: $hamaker" }
        require(hardRadius > 0.0) { "hardRadius must be positive, was: $hardRadius" }
        require(modelFloor > 0.0) { "modelFloor must be positive, was: $modelFloor" }
    }

    val inverseDebyeLength: Double get() = buffer.inverseDebyeLength()

    /** The smallest axis separation at which [totalCrossedEnergy] is defined. */
    val minimumSeparation: Double get() = 2.0 * hardRadius + modelFloor

    /** The screened electrostatic term alone, crossed geometry, pN·nm. */
    fun electrostaticCrossedEnergy(separation: Double): Double =
        pair.crossedScreenedCoulombEnergy(separation, inverseDebyeLength)

    /** The van der Waals term alone, crossed geometry, pN·nm — negative. */
    fun vanDerWaalsCrossedEnergy(separation: Double): Double =
        crossedCylinderVanDerWaalsEnergy(
            hamaker, hardRadius, surfaceSeparation(separation)
        )

    /** The measured short-range term alone, crossed geometry, pN·nm. */
    fun shortRangeCrossedEnergy(separation: Double): Double {
        surfaceSeparation(separation)
        return shortRange.crossedPairEnergy(separation, hardRadius)
    }

    /**
     * The assembled pair energy in the crossed geometry, pN·nm — screened electrostatics at the
     * device's own 2 mM, plus the measured short-range law, plus van der Waals.
     *
     * @throws IllegalArgumentException below [minimumSeparation].
     */
    fun totalCrossedEnergy(separation: Double): Double {
        require(separation >= minimumSeparation) {
            "separation must be at least $minimumSeparation nm, was: $separation"
        }
        return electrostaticCrossedEnergy(separation) +
            vanDerWaalsCrossedEnergy(separation) +
            shortRangeCrossedEnergy(separation)
    }

    /**
     * `dE/dD` in pN·nm per nm by a central difference — **negative** wherever the pair repels.
     *
     * The divisor is the **separation of the two samples**, `2 × halfStep`, named once here
     * rather than written at three call sites: `CLAUDE.md` records that reading it off the wrong
     * axis makes the gradient exactly half and that no dimensional check catches it.
     */
    fun energyGradient(separation: Double, halfStep: Double = 1e-6): Double {
        val sampleSeparation = 2.0 * halfStep
        return (totalCrossedEnergy(separation + halfStep) -
            totalCrossedEnergy(separation - halfStep)) / sampleSeparation
    }

    /**
     * The separation of the DLVO **barrier maximum**, in nm, or `null` if the energy is monotone
     * decreasing from [minimumSeparation].
     *
     * Continuum DLVO always has a formal *primary minimum at contact*, because the Lifshitz
     * `−A R/(6 D_s)` diverges where the exponential repulsions do not. **That minimum is an
     * artefact of extrapolating two continuum laws below a water diameter, not a separation the
     * pair holds** — this state's [modelFloor] is where it stops being carried. What matters for
     * `T-139` is the region *above* the barrier, and there the energy is monotone: there is **no
     * secondary minimum**, so there is no separation an unbonded pair sits at.
     */
    fun barrierSeparation(scanStep: Double = 0.002, scanTo: Double = 40.0): Double? {
        // Start just above the floor, not one scan step above it: at a coarse step the barrier
        // can sit inside the first interval and a scan that begins past it reports none.
        var previous = minimumSeparation + 1e-4
        var previousGradient = energyGradient(previous)
        var separation = previous + scanStep
        while (separation < scanTo) {
            val gradient = energyGradient(separation)
            if (previousGradient > 0.0 && gradient <= 0.0) {
                return bracketedRoot(previous, separation, tolerance = 1e-12) {
                    energyGradient(it)
                }
            }
            previous = separation
            previousGradient = gradient
            separation += scanStep
        }
        return null
    }

    /**
     * The far **secondary minimum** — the separation in nm and the energy in pN·nm — or `null` if
     * the energy is monotone out to [scanTo].
     *
     * It always exists in an unretarded DLVO model, because every repulsive term here is an
     * exponential and the Lifshitz attraction is a power law. Reporting it is the honest thing;
     * quoting it as an equilibrium separation would not be, and `C-0021`'s *"a stable equilibrium
     * is not a confinement"* says exactly why.
     */
    fun secondaryMinimum(scanStep: Double = 0.05, scanTo: Double = 200.0): Pair<Double, Double>? {
        var previous = PLAN_RELEVANT_RANGE
        var previousGradient = energyGradient(previous, halfStep = 1e-4)
        var separation = previous + scanStep
        while (separation < scanTo) {
            val gradient = energyGradient(separation, halfStep = 1e-4)
            if (previousGradient < 0.0 && gradient >= 0.0) {
                val root = bracketedRoot(previous, separation, tolerance = 1e-9) {
                    energyGradient(it, halfStep = 1e-4)
                }
                return root to totalCrossedEnergy(root)
            }
            previous = separation
            previousGradient = gradient
            separation += scanStep
        }
        return null
    }

    /** The share of the repulsion the attractive term cancels, at [separation]. */
    fun vanDerWaalsShare(separation: Double): Double {
        val repulsion = electrostaticCrossedEnergy(separation) + shortRangeCrossedEnergy(separation)
        return -vanDerWaalsCrossedEnergy(separation) / repulsion
    }

    private fun surfaceSeparation(separation: Double): Double {
        val gap = separation - 2.0 * hardRadius
        require(gap >= modelFloor) {
            "surface separation must be at least $modelFloor nm, was: $gap"
        }
        return gap
    }

    /**
     * The axis separation at which [totalCrossedEnergy] equals [energy] — the **soft** exclusion
     * width at a stated energy threshold.
     *
     * There is no equilibrium separation for two unbonded duplexes at this buffer (the energy is
     * monotone decreasing over the whole range), so a hard-body width is not a property of the
     * pair at all: it is a property of the pair **and a threshold**. This function is the map
     * between them, and the eighth instance in this project of a quantity that is not well posed
     * without the state it is read at.
     *
     * Returns `null` when [energy] exceeds the pair energy at the barrier, i.e. when the budget
     * is larger than anything the pair can charge above the continuum model's own floor — which
     * is not a failure but a verdict: **the affordable width is at or below the floor.**
     */
    fun exclusionWidthAtEnergy(energy: Double): Double? {
        require(energy > 0.0) { "energy must be positive, was: $energy" }
        // Start at the barrier, not at the model floor: below the barrier the energy is not
        // monotone and a bracket that spans it can hold three roots.
        val low = barrierSeparation() ?: minimumSeparation
        val high = 200.0
        if (totalCrossedEnergy(low) <= energy) return null
        return bracketedRoot(low, high, tolerance = 1e-12) { totalCrossedEnergy(it) - energy }
    }
}
