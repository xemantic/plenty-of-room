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

import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.ln
import kotlin.math.pow

/**
 * The shear rupture force of a hybridised staple domain, as a function of the number of base
 * pairs it spans and of the loading rate — Strunz et al.'s own single-barrier model, with the
 * scaling constants they publish.
 *
 * ## Why `T-19` needs this and `C-0020` did not
 *
 * `C-0006` traces the per-load-path shear allowable to Strunz, Oroszlan, Schäfer &
 * Güntherodt, *PNAS* **96**:11277 (1999), and records two design consequences of it, one of
 * which is that *"shear rupture saturates with domain length (~70 pN asymptote)"*. The
 * **48 ± 2 pN** that `C-0009`, `C-0014` and `C-0020` all consume is that paper's measurement
 * on a **30 bp** duplex at a 50 nm/s retract velocity. It is therefore not a constant of the
 * material: it is the capacity of a bond of a stated length, and **how long the bond is** is
 * exactly the sequence-design choice `T-19` exists to price.
 *
 * ## The model, entirely the paper's own
 *
 * - Eq. 1, the Evans-Ritchie single-barrier form: `F = (k_BT/x) ln(r x/(ν k_BT))` with `r` the
 *   loading rate. Strunz measures the loading rate as the retract velocity times an elasticity
 *   for which *"we have adopted a constant value of 2 pN/nm"*, so 50 nm/s is 100 pN/s and the
 *   measured span 8–2000 nm/s is the 16–4000 pN/s the abstract quotes.
 * - Eq. 2, the thermal off-rate: `ν = 10^(α − β n) s⁻¹` with *"α = 3 ± 1 and β = 0.5 ± 0.1
 *   from a linear regression"*.
 * - Eq. 3, the barrier separation: linear in `n`, with the paper's two quoted lengths — a
 *   **0.7 Å per base pair** slope and a **7 Å** offset (*"the offset length of 7 Å in the
 *   linear regression (Eq. 3)"*; the slope is the one their own saturation figure
 *   `1.2 k_BT/0.7 Å ≈ 70 pN` is written on).
 *
 * The saturation is then `k_BT β ln10 / x₁` exactly, because the `β n` in the off-rate and the
 * `x₁ n` in the separation both grow linearly and their ratio is a force.
 *
 * **CITED, MEASURED, and not re-fitted here.** The only check applied is that the assembled
 * model reproduces the paper's own two headline numbers — 48 ± 2 pN at 30 bp, and the ≈70 pN
 * asymptote — which it does, and which is what licenses using it at the lengths *between*
 * the three the paper measured.
 *
 * ## What it is used for, and what it is not
 *
 * Used as a **relative** instrument: the ratio `m A(n/m) / A(n)` decides whether splitting a
 * bond across `m` duplexes wins or loses on the joint, and that ratio is far less exposed to
 * the loading-rate extrapolation than either force is. The Evans-Ritchie form has no
 * equilibrium plateau, so it must not be extrapolated to `r → 0`; every number here is quoted
 * at a loading rate inside the measured 16–4000 pN/s, as `C-0006` requires.
 *
 * @param separationOffset the `n`-independent part of the barrier separation, in nm.
 * @param separationPerBasePair the per-base-pair barrier separation, in nm.
 * @param offRateExponentIntercept `α` of `ν = 10^(α − β n)`.
 * @param offRateExponentSlope `β` of the same.
 */
class ShearJointAllowable(
    val separationOffset: Double = STRUNZ_SEPARATION_OFFSET,
    val separationPerBasePair: Double = STRUNZ_SEPARATION_PER_BASE_PAIR,
    val offRateExponentIntercept: Double = STRUNZ_OFF_RATE_INTERCEPT,
    val offRateExponentSlope: Double = STRUNZ_OFF_RATE_SLOPE,
    val temperature: Double = 300.0
) {

    init {
        require(separationOffset >= 0.0) {
            "separationOffset must not be negative, was: $separationOffset"
        }
        require(separationPerBasePair > 0.0) {
            "separationPerBasePair must be positive, was: $separationPerBasePair"
        }
        require(offRateExponentSlope > 0.0) {
            "offRateExponentSlope must be positive, was: $offRateExponentSlope"
        }
    }

    private val kT = thermalEnergy(temperature)

    /** The barrier separation `x` in nm of a domain of [basePairs] base pairs — Eq. 3. */
    fun barrierSeparation(basePairs: Double): Double {
        require(basePairs > 0.0) { "basePairs must be positive, was: $basePairs" }
        return separationOffset + separationPerBasePair * basePairs
    }

    /** The thermal off-rate `ν` in s⁻¹ of a domain of [basePairs] base pairs — Eq. 2. */
    fun thermalOffRate(basePairs: Double): Double {
        require(basePairs > 0.0) { "basePairs must be positive, was: $basePairs" }
        return 10.0.pow(offRateExponentIntercept - offRateExponentSlope * basePairs)
    }

    /**
     * The most probable shear rupture force in pN of a domain of [basePairs] base pairs at
     * [loadingRate] pN/s — Eq. 1.
     *
     * Negative below the loading rate at which the logarithm changes sign, which is the
     * Evans-Ritchie form having no equilibrium plateau rather than a physical statement; keep
     * inside the 16–4000 pN/s the paper measured.
     */
    fun ruptureForce(basePairs: Double, loadingRate: Double): Double {
        require(loadingRate > 0.0) { "loadingRate must be positive, was: $loadingRate" }
        val separation = barrierSeparation(basePairs)
        // in LOG space: 10^(alpha - beta n) underflows to exactly zero past ~320 base pairs,
        // and the quotient then returns an infinity rather than the saturation it tends to —
        // the same family of trap `CLAUDE.md` records for cosh/sinh
        return (kT / separation) * (
                ln(loadingRate * separation / kT) -
                        (offRateExponentIntercept - offRateExponentSlope * basePairs) * ln(10.0)
                )
    }

    /**
     * The force in pN a domain of any length saturates at — `k_BT β ln10 / x₁`, independent of
     * the loading rate, and the paper's own *"≈70 pN"*.
     */
    val saturationForce: Double
        get() = kT * offRateExponentSlope * ln(10.0) / separationPerBasePair

    /**
     * What splitting a bond of [basePairs] total bonded length into [ways] equal domains does
     * to the tension the *joint* can carry: `ways · A(n/ways) / A(n)`.
     *
     * Above one, splitting wins; below one, it loses. Both happen, and the crossover between
     * them is [splitBreakEven].
     */
    fun splitGain(basePairs: Double, ways: Int, loadingRate: Double): Double {
        require(ways >= 1) { "ways must be at least one, was: $ways" }
        return ways * ruptureForce(basePairs / ways, loadingRate) /
                ruptureForce(basePairs, loadingRate)
    }

    /**
     * The total bonded length in base pairs at which [splitGain] is exactly one — below it,
     * keep the bond on one duplex; above it, split it.
     *
     * Bisected on the bracket rather than on a residual tolerance, per `CLAUDE.md`'s record of
     * an unreachable convergence tolerance running its full iteration cap in silence.
     */
    fun splitBreakEven(
        ways: Int,
        loadingRate: Double,
        lower: Double = 1.0,
        upper: Double = 200.0
    ): Double {
        require(ways >= 2) { "ways must be at least two, was: $ways" }
        var low = lower
        var high = upper
        repeat(BISECTION_STEPS) {
            val middle = 0.5 * (low + high)
            if (splitGain(middle, ways, loadingRate) < 1.0) low = middle else high = middle
        }
        return 0.5 * (low + high)
    }

    companion object {

        /**
         * The per-base-pair barrier separation in nm — **CITED**, Strunz et al. (1999), the
         * `0.7 Å` their own saturation figure `1.2 k_BT/0.7 Å ≈ 70 pN` is written on.
         */
        const val STRUNZ_SEPARATION_PER_BASE_PAIR: Double = 0.07

        /**
         * The `n`-independent offset of the barrier separation in nm — **CITED**, Strunz et
         * al. (1999): *"the offset length of 7 Å in the linear regression (Eq. 3)"*.
         */
        const val STRUNZ_SEPARATION_OFFSET: Double = 0.7

        /** `α` of `ν = 10^(α − β n)` — **CITED**, Strunz et al. (1999), `3 ± 1`. */
        const val STRUNZ_OFF_RATE_INTERCEPT: Double = 3.0

        /** `β` of the same — **CITED**, Strunz et al. (1999), `0.5 ± 0.1`. */
        const val STRUNZ_OFF_RATE_SLOPE: Double = 0.5

        /** The elasticity Strunz adopts to turn a retract velocity into a loading rate, pN/nm. */
        const val STRUNZ_LINKER_ELASTICITY: Double = 2.0

        /** The loading rate in pN/s at which the 48 ± 2 pN was measured — 50 nm/s × 2 pN/nm. */
        const val REFERENCE_LOADING_RATE: Double = 100.0

        /** The slowest loading rate Strunz measured, in pN/s. */
        const val SLOWEST_MEASURED_LOADING_RATE: Double = 16.0

        /** The fastest loading rate Strunz measured, in pN/s. */
        const val FASTEST_MEASURED_LOADING_RATE: Double = 4000.0

        /**
         * The unzip allowable in pN — **CITED, MEASURED**, Essevaz-Roulet et al. (1997), and
         * **length-independent**, because unzipping opens one base pair at a time and the
         * force is a property of the fork rather than of the domain. That asymmetry is why
         * splitting a bond multiplies its capacity by exactly `m` in unzip geometry and by
         * `m A(n/m)/A(n)` in shear.
         */
        const val UNZIP_ALLOWABLE: Double = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE

        private const val BISECTION_STEPS: Int = 80
    }

}
