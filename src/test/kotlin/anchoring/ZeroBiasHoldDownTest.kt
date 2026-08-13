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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.coupling.gaussianContourCeiling
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
import com.xemantic.nano.plentyofroom.coupling.placementPreload
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-13` — the mechanisms that could hold the tile down at zero bias, and the equilibrium
 * they do or do not produce.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem
 * definition. Gate 3 deliberately checks things that are **not** restatements of the
 * construction: an exact Boltzmann quadrature against the two closed forms it must
 * reproduce, and the sign structure the topology argument fixes before any arithmetic.
 */
class ZeroBiasHoldDownTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the hold-down force scale should be an energy over a length`() {
        // k_BT/sigma, the force analogue of C-0010's k_BT/sigma^2
        assert(holdDownForceScale(3.0).isCloseTo(thermalEnergy() / 3.0))
        assert(holdDownForceScale(3.0).isCloseTo(1.380649, 1e-6))
        // halving the bound must double the required force, exactly
        assert((holdDownForceScale(1.5) / holdDownForceScale(3.0)).isCloseTo(2.0))
    }

    @Test
    fun `gate 1 dimensional consistency - a Hamaker constant in pN nm is exactly a zeptojoule`() {
        // 1 zJ = 1e-21 J = 1e-12 N x 1e-9 m = 1 pN.nm, so the conversion is the identity
        assert(hamakerFromJoule(5.0e-20).isCloseTo(50.0))
        assert(hamakerFromJoule(1.0e-21).isCloseTo(1.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the van der Waals pressure should be an energy over a cubed length`() {
        // A/(6 pi d^3): energy over volume is a pressure, and doubling the gap divides by 8
        val near = vanDerWaalsPressure(hamaker = 50.0, gap = 10.0)
        assert(near.isCloseTo(50.0 / (6.0 * PI * 1000.0)))
        assert((near / vanDerWaalsPressure(50.0, 20.0)).isCloseTo(8.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the van der Waals stiffness should be the pressure over a length`() {
        // |dP/dh| = 3 P/d for a half-space, so the ratio is exactly 3/d and carries 1/nm
        val gap = 7.0
        val pressure = vanDerWaalsPressure(50.0, gap)
        val slope = vanDerWaalsPressureSlopeMagnitude(50.0, gap)
        assert((slope / pressure).isCloseTo(3.0 / gap))
    }

    @Test
    fun `gate 1 dimensional consistency - the bridging ceiling and its threshold should invert each other`() {
        val chains = 38.4
        val range = 1.0
        val ceiling = bridgingForceCeiling(chains, energyPerChain = 4.142, range = range)
        assert(bridgingEnergyThreshold(ceiling, chains, range).isCloseTo(4.142))
        assert(ceiling.isCloseTo(38.4 * 4.142))
    }

    @Test
    fun `gate 1 dimensional consistency - a buoyant weight should be a density times a volume times g`() {
        // 0.7 g/cm3 over 3200 nm3 — nine orders below the thermal scale, and that is the point
        val weight = buoyantWeight(volume = 3200.0, bodyDensity = 1.7, fluidDensity = 1.0)
        assert(weight.isCloseTo(0.7 * 3200.0 * 9.80665e-12))
        // doubling the volume doubles the weight, exactly
        assert((buoyantWeight(6400.0, 1.7, 1.0) / weight).isCloseTo(2.0))
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - an infinitely thick slab should reduce to the half-space pressure`() {
        val halfSpace = vanDerWaalsPressure(50.0, 10.0)
        assert(vanDerWaalsPressure(50.0, 10.0, slabThickness = Double.POSITIVE_INFINITY).isCloseTo(halfSpace))
        // a slab as thick as the gap keeps exactly 7/8 of it
        assert(vanDerWaalsPressure(50.0, 10.0, slabThickness = 10.0).isCloseTo(halfSpace * 0.875))
        // and a vanishingly thin slab keeps nothing
        assert(vanDerWaalsPressure(50.0, 10.0, slabThickness = 1e-9) < halfSpace * 1e-8)
    }

    @Test
    fun `gate 2 limiting cases - the combining relation should vanish when the medium matches a body`() {
        // A_132 = (sqrt A_11 - sqrt A_33)(sqrt A_22 - sqrt A_33): index-matching one body kills it
        assert(combinedHamakerConstant(37.0, 100.0, medium = 37.0).isCloseTo(0.0))
        // and it changes SIGN — becomes repulsive — when the medium sits between the two bodies
        assert(combinedHamakerConstant(10.0, 400.0, medium = 37.0) < 0.0)
        assert(combinedHamakerConstant(100.0, 400.0, medium = 37.0) > 0.0)
    }

    @Test
    fun `gate 2 limiting cases - a polymer-free medium should leave the medium Hamaker constant unchanged`() {
        assert(mediumHamakerWithPolymer(solvent = 37.0, polymer = 66.0, volumeFraction = 0.0).isCloseTo(37.0))
        assert(mediumHamakerWithPolymer(37.0, 66.0, 1.0).isCloseTo(66.0))
        // a polymer of higher index raises the medium and therefore LOWERS the attraction
        val neat = combinedHamakerConstant(100.0, 40.0, 37.0)
        val laden = combinedHamakerConstant(100.0, 40.0, mediumHamakerWithPolymer(37.0, 66.0, 0.05))
        assert(laden < neat)
    }

    @Test
    fun `gate 2 limiting cases - screening the zero-frequency term should vanish at large gap and be inert at zero salt`() {
        val zero = 3.7345
        val dispersion = 31.0
        // at kappa = 0 nothing is screened, and the (1 + 2 kappa d) prefactor is exactly 1
        assert(zeroFrequencyScreeningFactor(gap = 10.0, inverseDebyeLength = 0.0).isCloseTo(1.0))
        assert(
            screenedHamakerConstant(zero, dispersion, gap = 10.0, inverseDebyeLength = 0.0)
                .isCloseTo(zero + dispersion)
        )
        // at a large gap only the dispersion term survives
        assert(
            screenedHamakerConstant(zero, dispersion, gap = 200.0, inverseDebyeLength = 1.0 / 3.93)
                .isCloseTo(dispersion)
        )
        // the exponent is 2 kappa d, not kappa d — the interaction is second order in the
        // fluctuating field, exactly as C-0008's zero-bias image attraction is
        val kappa = 1.0 / 3.93
        assert(zeroFrequencyScreeningFactor(5.0, kappa).isCloseTo(exp(-2.0 * kappa * 5.0)))
        // and the whole bracket this end belongs to is worth only a few per cent of a metal's
        // Hamaker constant: 3.73 zJ of 238.6+ is under 1.6%
        val share = HamakerConstants.ZERO_FREQUENCY_TERM / HamakerConstants.GOLD_ACROSS_WATER
        assert(share < 0.016)
    }

    @Test
    fun `gate 2 limiting cases - the across-water combining relation should be a geometric mean`() {
        // A_1w2 = sqrt(A_1w1 A_2w2): identical bodies return the body's own constant exactly
        assert(combinedHamakerAcrossWater(250.0, 250.0).isCloseTo(250.0))
        assert(combinedHamakerAcrossWater(5.0, 245.0).isCloseTo(35.0))
        // it can never be negative, which is exactly why the vacuum form is kept as the sign
        // diagnostic beside it
        assert(combinedHamakerAcrossWater(1e-6, 1e6) > 0.0)
        // and a 1.36x spread in one factor is only 1.17x in the mean — the square root is what
        // makes the DNA bracket affordable
        val ratio = combinedHamakerAcrossWater(5.90, 238.6) / combinedHamakerAcrossWater(4.33, 238.6)
        assert(ratio.isCloseTo(sqrt(5.90 / 4.33), 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - the retardation factor should sit inside its sourced bracket and decrease with the gap`() {
        // Tolias arXiv:2202.09159, pressure factors derived from the printed energy fit
        assert(retardationPressureFactor(5.0) in 0.824..0.846)
        assert(retardationPressureFactor(10.0) in 0.702..0.733)
        assert(retardationPressureFactor(7.0) < retardationPressureFactor(5.0))
        assert(retardationPressureFactor(7.0) > retardationPressureFactor(10.0))
        // clamped outside the sourced range rather than extrapolated
        assert(retardationPressureFactor(1.0).isCloseTo(retardationPressureFactor(5.0)))
        assert(retardationPressureFactor(40.0).isCloseTo(retardationPressureFactor(10.0)))
    }

    @Test
    fun `gate 2 limiting cases - a Boltzmann quadrature over a harmonic well should reproduce equipartition`() {
        val stiffness = 5.0
        val centre = 8.0
        val statistics = boltzmannPositionStatistics(
            netUpwardForce = { height -> -stiffness * (height - centre) },
            lower = centre - 12.0,
            upper = centre + 12.0,
            panels = 40000
        )
        assert(statistics.mean.isCloseTo(centre, 1e-9))
        assert(statistics.rms.isCloseTo(sqrt(thermalEnergy() / stiffness), 1e-6))
    }

    @Test
    fun `gate 2 limiting cases - a Boltzmann quadrature over a one-sided linear potential should give k_BT over F`() {
        // above L0 the layer contributes nothing, so the confining potential is LINEAR and the
        // excursion is exponentially distributed: mean k_BT/F, RMS sqrt(2) k_BT/F
        val force = 2.0
        val statistics = boltzmannPositionStatistics(
            netUpwardForce = { -force },
            lower = 0.0,
            upper = 60.0 * thermalEnergy() / force,
            panels = 200000
        )
        assert(statistics.mean.isCloseTo(thermalEnergy() / force, 1e-6))
        assert(statistics.rms.isCloseTo(thermalEnergy() / force, 1e-5))
    }

    @Test
    fun `gate 2 limiting cases - a hold-down of zero should leave no equilibrium at all`() {
        // the whole of C-0010's finding, as an executable statement: a non-adsorbing layer
        // exerts no upward force above L0, so with nothing pulling down every height above L0
        // is a neutral equilibrium and the resting position is undefined
        val restingHeight = zeroBiasRestingHeight(
            netUpwardForce = { height -> if (height >= 10.0) 0.0 else 30.0 * (10.0 - height) },
            ceiling = 12.0,
            floor = 1.0
        )
        assert(restingHeight == null)
    }

    @Test
    fun `gate 2 limiting cases - a constant hold-down should park the tile where the layer carries it`() {
        // P(h)A = 30 (L0 - h), so a 9 pN pull-down rests at exactly 0.3 nm of compression
        val restingHeight = zeroBiasRestingHeight(
            netUpwardForce = { height -> (if (height >= 10.0) 0.0 else 30.0 * (10.0 - height)) - 9.0 },
            ceiling = 12.0,
            floor = 1.0
        )
        assert(restingHeight != null)
        assert(restingHeight!!.isCloseTo(9.7, 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - the resting height should be monotone decreasing in the hold-down force`() {
        val heights = listOf(0.1, 1.0, 10.0, 50.0).map { load ->
            zeroBiasRestingHeight(
                netUpwardForce = { height ->
                    (if (height >= 10.0) 0.0 else 30.0 * (10.0 - height)) - load
                },
                ceiling = 12.0,
                floor = 0.5
            )!!
        }
        heights.zipWithNext().forEach { (higher, lower) -> assert(lower < higher) }
    }

    @Test
    fun `gate 2 limiting cases - an unphysical argument should throw rather than return a number`() {
        assertFailsWith<IllegalArgumentException> { vanDerWaalsPressure(50.0, gap = 0.0) }
        assertFailsWith<IllegalArgumentException> { vanDerWaalsPressure(50.0, gap = -1.0) }
        assertFailsWith<IllegalArgumentException> { holdDownForceScale(0.0) }
        assertFailsWith<IllegalArgumentException> { bridgingForceCeiling(0.0, 4.142, 1.0) }
        assertFailsWith<IllegalArgumentException> {
            boltzmannPositionStatistics({ -1.0 }, lower = 5.0, upper = 5.0, panels = 100)
        }
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - the topology argument fixes every sign before any arithmetic`() {
        // a link grounded BELOW the tile pulls it DOWN; the same link grounded ABOVE pulls it UP.
        // The magnitudes are identical — only the ground point differs — so this is a statement
        // about topology and not about the element.
        val chain = FreelyJointedChain(contourLength = 51.4, kuhnLength = 2.10)
        val tension = chain.tension(10.0)
        assert(substrateTetherHoldDown(chain, count = 8, height = 10.0).isCloseTo(8.0 * tension))
        // and grounded above, at zero stroke, it carries nothing at all
        assert(leverTetherHoldDown(chain, count = 8, stroke = 0.0).isCloseTo(0.0))
        assert(leverTetherHoldDown(chain, count = 8, stroke = 3.0) < 0.0)
    }

    @Test
    fun `gate 3 conservation - the Boltzmann quadrature should be invariant to the potential's zero`() {
        // an additive constant in the potential is unobservable; the quadrature must not see it
        val force: (Double) -> Double = { height -> -4.0 * (height - 6.0) }
        val direct = boltzmannPositionStatistics(force, 0.0, 12.0, panels = 20000)
        val shifted = boltzmannPositionStatistics(force, 0.0, 12.0, panels = 20000, energyOffset = 500.0)
        assert(shifted.mean.isCloseTo(direct.mean, 1e-12))
        assert(shifted.rms.isCloseTo(direct.rms, 1e-12))
    }

    @Test
    fun `gate 3 conservation - the equilibrium stiffness should be the derivative the root is found on`() {
        // the analytic assembly and a central difference of the same net force must agree
        val net: (Double) -> Double = { height ->
            (if (height >= 10.0) 0.0 else 30.0 * (10.0 - height)) - 9.0
        }
        val height = 9.7
        val step = 1e-5
        val difference = -(net(height + step) - net(height - step)) / (2.0 * step)
        assert(equilibriumStiffness(net, height, step).isCloseTo(difference, 1e-9))
        assert(equilibriumStiffness(net, height, step).isCloseTo(30.0, 1e-6))
    }

    @Test
    fun `gate 3 conservation - the coupling preload relation should agree with the coupling package exactly`() {
        // (k_c - k_c*) delta is algebraically identical to C-0017's placementPreload; the two are
        // computed by different code paths and must agree to the last bit
        val mandated = mandatedCouplingStiffness(100.0, 3.0)
        // compared ABSOLUTELY, in pN: near the mandate the two forms are a catastrophic
        // cancellation of each other — (k - k*)d against kd - F — and comparing two quantities
        // that are both meant to be zero relatively compares their noise (`CLAUDE.md`)
        listOf(20.0, 33.3333333, 39.01, 70.0, 440.0).forEach { stiffness ->
            assert(
                abs(
                    couplingPreloadForStiffness(stiffness, mandated, 3.0) -
                            placementPreload(stiffness, 3.0, 100.0)
                ) < 1e-9
            )
        }
        // and the placement-matched coupling needs exactly none
        assert(couplingPreloadForStiffness(mandated, mandated, 3.0).isCloseTo(0.0, 1e-9))
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 convergence - the resting-height root should exit on the bracket width and be scan-independent`() {
        val net: (Double) -> Double = { height ->
            (if (height >= 10.0) 0.0 else 12.0 * (10.0 - height) * (10.0 - height)) - 9.4
        }
        val coarse = zeroBiasRestingHeight(net, ceiling = 12.0, floor = 1.0, scanSteps = 64)!!
        val fine = zeroBiasRestingHeight(net, ceiling = 12.0, floor = 1.0, scanSteps = 8192)!!
        assert(abs(coarse - fine) < 1e-9)
        // and it is the analytic root: 12 d^2 = 9.4 gives d = 0.885061...
        assert(fine.isCloseTo(10.0 - sqrt(9.4 / 12.0), 1e-9))
    }

    @Test
    fun `gate 4 convergence - the Boltzmann quadrature should converge in the panel count`() {
        val force: (Double) -> Double = { height -> -3.0 * (height - 7.0) }
        val values = listOf(2000, 8000, 32000).map {
            boltzmannPositionStatistics(force, 0.0, 14.0, panels = it).rms
        }
        val first = abs(values[1] - values[0])
        val second = abs(values[2] - values[1])
        assert(second < first)
        assert(second < 1e-7)
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 2 limiting cases - the van der Waals well should be finite and match its closed form`() {
        // integral of (A S/6pi)[h^-3 - (h+t)^-3] from h to infinity is (A S/12pi)[h^-2 - (h+t)^-2]
        val hamaker = 30.0
        val area = 1600.0
        val gap = 5.0
        val thickness = 2.0
        val depth = vanDerWaalsWellDepth(hamaker, gap, thickness, area)
        assert(
            depth.isCloseTo(
                hamaker * area / (12.0 * PI) * (1.0 / (gap * gap) - 1.0 / ((gap + thickness) * (gap + thickness)))
            )
        )
        // and the same number is what a Boltzmann quadrature over that force reports as its barrier
        val statistics = boltzmannPositionStatistics(
            netUpwardForce = { h -> -vanDerWaalsPressure(hamaker, h, thickness) * area },
            lower = gap,
            upper = 4000.0,
            panels = 400000
        )
        assert(statistics.escapeBarrier.isCloseTo(depth / thermalEnergy(), 1e-3))
    }

    @Test
    fun `gate 2 limiting cases - the barrier of a linear potential should be the force times the span`() {
        val force = 2.0
        val span = 12.0
        val statistics = boltzmannPositionStatistics(
            netUpwardForce = { -force }, lower = 0.0, upper = span, panels = 20000
        )
        assert(statistics.escapeBarrier.isCloseTo(force * span / thermalEnergy(), 1e-9))
        assert(statistics.domainUpper.isCloseTo(span))
    }

    @Test
    fun `gate 5 literature - C-0014's entropic tether preload should be reproduced from its own design rule`() {
        // C-0014 S3: eight tethers, b = 2.10 nm (Chen 2012 zero-force), contour half the
        // admissible ceiling — 4.6 pN at the 5 nm layer and 9.4 pN at the 10 nm one
        val contour = 0.5 * gaussianContourCeiling(
            kuhnLength = 2.10, count = 8, requiredStiffness = 0.460216
        )
        val chain = FreelyJointedChain(contour, 2.10)
        assert(substrateTetherHoldDown(chain, 8, 10.0).isCloseTo(9.4, 0.03))
        assert(substrateTetherHoldDown(chain, 8, 5.0).isCloseTo(4.6, 0.03))
    }

    @Test
    fun `gate 5 literature - the assembled van der Waals pressure should reproduce the sourced bracket`() {
        // Dryden 2015 DNA across water x Tolias gold across water, geometric mean, slab factor
        // for a 2 nm tile, retardation: 7.2-8.9 kPa at 5 nm and 515-637 Pa at 10 nm.
        // 1 kPa = 1e-6 pN/nm^2.
        listOf(
            5.0 to (7.2e-3 to 8.9e-3),
            10.0 to (0.515e-3 to 0.637e-3)
        ).forEach { (gap, bracket) ->
            listOf(
                HamakerConstants.DNA_ACROSS_WATER_LOW to HamakerConstants.GOLD_ACROSS_WATER,
                HamakerConstants.DNA_ACROSS_WATER_HIGH to HamakerConstants.GOLD_ACROSS_WATER_HIGH
            ).forEach { (dna, gold) ->
                val pressure = vanDerWaalsPressure(
                    combinedHamakerAcrossWater(dna, gold), gap, slabThickness = 2.0
                ) * retardationPressureFactor(gap)
                assert(pressure > bracket.first * 0.9)
                assert(pressure < bracket.second * 1.1)
            }
        }
    }

    @Test
    fun `gate 5 literature - the slab factor should reproduce the sourced 0-636 and 0-421`() {
        // 1 - d^3/(d+t)^3 for a 2 nm tile: 0.636 at 5 nm and 0.421 at 10 nm
        assert(
            (vanDerWaalsPressure(50.0, 5.0, 2.0) / vanDerWaalsPressure(50.0, 5.0))
                .isCloseTo(0.636, 1e-3)
        )
        assert(
            (vanDerWaalsPressure(50.0, 10.0, 2.0) / vanDerWaalsPressure(50.0, 10.0))
                .isCloseTo(0.421, 1e-3)
        )
    }

    @Test
    fun `gate 5 literature - the zero-frequency term should be three quarters of zeta three k_BT`() {
        // (3/4) zeta(3) k_BT = 3.7345 zJ; the familiar (3/4) k_BT is the s = 1 truncation and is
        // 20% low, which is the trap this constant exists to avoid
        assert(HamakerConstants.ZERO_FREQUENCY_TERM.isCloseTo(3.7345, 1e-4))
        assert(
            (HamakerConstants.ZERO_FREQUENCY_TERM /
                    HamakerConstants.ZERO_FREQUENCY_TERM_LOW_DIELECTRIC).isCloseTo(APERY_CONSTANT)
        )
    }

    @Test
    fun `gate 5 literature - the thermal force scale should agree with C-0010's stiffness bound`() {
        // C-0010 requires k >= k_BT/sigma^2 for a harmonic coordinate; the linear-potential
        // analogue is F >= k_BT/sigma, and the two are the same statement one power apart
        val bound = 3.0
        assert((holdDownForceScale(bound) / bound).isCloseTo(thermalEnergy() / (bound * bound)))
        assert((holdDownForceScale(bound) / bound).isCloseTo(0.460216, 1e-6))
    }
}
