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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.anchoring.singleStrandedRadiusOfGyration
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-195` gate tests for the unpaired scaffold remainder.
 *
 * The remainder is the scaffold nucleotides no staple pairs. Under NDI's decision 5
 * (*"M13, circular ~7–8 K nucleotides"*) it is the **default**, so the question is what a
 * coil of it does to `C-0022`'s edge load — and the two cheap bounds tested here are what
 * decide whether a field solve is owed at all.
 */
class ScaffoldRemainderTest {

    private val lb = bjerrumLength()

    // ------------------------------------------------ gate 1 — dimensional consistency

    @Test
    fun `gate 1 should make the remainder the scaffold less what the tile pairs`() {
        assert(unpairedRemainder(M13MP18_NUCLEOTIDES, 60 * 112) == 529)
        assert(unpairedRemainder(P7560_NUCLEOTIDES, 60 * 112) == 840)
        assert(unpairedRemainder(P8064_NUCLEOTIDES, 60 * 112) == 1344)
        // the single-layer sheet C-0086 measured
        assert(unpairedRemainder(M13MP18_NUCLEOTIDES, 15 * 112) == 5569)
    }

    @Test
    fun `gate 1 should refuse a tile that does not fit its scaffold`() {
        assertFailsWith<IllegalArgumentException> { unpairedRemainder(7249, 8000) }
        assertFailsWith<IllegalArgumentException> { unpairedRemainder(0, 0) }
    }

    @Test
    fun `gate 1 should make the smeared remainder charge density go as one over the area`() {
        val one = smearedRemainderChargeDensity(840, 0.4, 1000.0)
        val two = smearedRemainderChargeDensity(840, 0.4, 2000.0)
        assert(one.isCloseTo(840 * 0.4 / 1000.0, 1e-14))
        assert((one / two).isCloseTo(2.0, 1e-14))
    }

    @Test
    fun `gate 1 should make the slit confinement free energy go as one over the gap squared`() {
        val near = idealSlitConfinementFreeEnergy(10.0, 5.0)
        val far = idealSlitConfinementFreeEnergy(10.0, 10.0)
        assert(near.isCloseTo(PI * PI * 100.0 / 25.0, 1e-12))
        assert((near / far).isCloseTo(4.0, 1e-12))
    }

    @Test
    fun `gate 1 should make the penetration count go as the gap squared`() {
        val two = slitPenetratingNucleotides(2.0, 2.10, 0.57, 100000)
        val four = slitPenetratingNucleotides(4.0, 2.10, 0.57, 100000)
        assert((four / two).isCloseTo(4.0, 1e-12))
    }

    // -------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 should condense nothing when the charge spacing exceeds the Bjerrum length over the valency`() {
        // q xi = q l_B/b <= 1 means no condensation at all: Manning's own criterion.
        assert(manningSurvivingFractionOfSpacing(2.0 * lb, 2, lb).isCloseTo(1.0, 1e-14))
        assert(manningSurvivingFractionOfSpacing(10.0, 2, lb).isCloseTo(1.0, 1e-14))
        // duplex DNA: b = 0.17 nm, q = 2 -> 11.90 %
        assert(manningSurvivingFractionOfSpacing(0.17, 2, lb).isCloseTo(0.11902984614507807, 1e-12))
    }

    @Test
    fun `gate 2 should make single-stranded DNA condense three times less than duplex DNA`() {
        // The load-bearing asymmetry: one charge per 0.57-0.70 nm of ssDNA contour against one
        // per 0.17 nm of duplex axis. C-0086 compared the two bodies on BARE charge.
        val duplex = manningSurvivingFractionOfSpacing(0.17, 2, lb)
        val single = manningSurvivingFractionOfSpacing(SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN, 2, lb)
        assert(single > 3.0 * duplex)
        assert(single < 4.5 * duplex)
    }

    @Test
    fun `gate 2 should send the confinement free energy to zero as the slit opens`() {
        assert(idealSlitConfinementFreeEnergy(10.0, 1e6) < 1e-9)
        assert(swollenSlitConfinementFreeEnergy(10.0, 1e6) < 1e-7)
    }

    @Test
    fun `gate 2 should cap the penetration count at the remainder itself`() {
        assert(slitPenetratingNucleotides(1000.0, 2.10, 0.57, 840).isCloseTo(840.0, 1e-12))
    }

    @Test
    fun `gate 2 should saturate the effective charge density against a large added charge`() {
        // The whole cheap bound: on a saturated wall, a 100 % increase in the bare charge is a
        // few per cent in sigma_eff. Point-ion PB, 2:1, negative wall.
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        val bare = 0.8282833977436311
        val base = abs(asymmetricEffectiveChargeDensity(
            asymmetricReducedSurfacePotential(-bare, kappa, lb), kappa, lb
        ))
        val doubled = abs(asymmetricEffectiveChargeDensity(
            asymmetricReducedSurfacePotential(-2.0 * bare, kappa, lb), kappa, lb
        ))
        assert(doubled > base)
        assert(doubled / base < 1.05)
    }

    // --------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 should conserve the scaffold across every cross-section`() {
        for (scaffold in listOf(M13MP18_NUCLEOTIDES, P7560_NUCLEOTIDES, P8064_NUCLEOTIDES)) {
            for (paired in listOf(15 * 112, 60 * 112, 64 * 112)) {
                if (paired > scaffold) continue
                assert(unpairedRemainder(scaffold, paired) + paired == scaffold)
            }
        }
    }

    @Test
    fun `gate 3 should make the penetration count the subchain that costs exactly one kT`() {
        // Round trip: the penetrating subchain, fed back through the ideal confinement law
        // at the same gap, must cost exactly 1 k_BT. Two independently written expressions.
        val gap = 7.0
        val kuhn = 2.10
        val contour = 0.57
        val nucleotides = slitPenetratingNucleotides(gap, kuhn, contour, 1_000_000)
        val radius = kuhn * kotlin.math.sqrt(nucleotides * contour / kuhn / 6.0)
        assert(idealSlitConfinementFreeEnergy(radius, gap).isCloseTo(1.0, 1e-10))
    }

    @Test
    fun `gate 3 should keep the effective charge density monotone and under saturation`() {
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        val ceiling = asymmetricSaturatedEffectiveChargeDensity(kappa, lb, negativeSurface = true)
        var previous = 0.0
        for (step in 1..40) {
            val bare = 0.05 * step
            val value = abs(asymmetricEffectiveChargeDensity(
                asymmetricReducedSurfacePotential(-bare, kappa, lb), kappa, lb
            ))
            assert(value > previous)
            assert(value < ceiling)
            previous = value
        }
    }

    // ------------------------------------- gate 5 — literature and upstream reproduction

    @Test
    fun `gate 5 should reproduce C-0086's 33 point 3 nm coil and its 1 point 66 charge ratio`() {
        val remainder = unpairedRemainder(M13MP18_NUCLEOTIDES, 15 * 112)
        val radius = singleStrandedRadiusOfGyration(
            remainder, SsDnaTether.KUHN_LENGTH_ZERO_FORCE, SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN
        )
        assert(radius.isCloseTo(33.332, 1e-4))
        val ratio = bareChargeRatio(remainder, 2 * 15 * 112)
        assert(ratio.isCloseTo(1.657, 1e-3))
    }

    @Test
    fun `gate 5 should reproduce C-0109's four-layer scaffold budget`() {
        assert(60 * 112 == 6720)
        assert(unpairedRemainder(M13MP18_NUCLEOTIDES, 6720) == 529)
        assert((6720.0 / M13MP18_NUCLEOTIDES).isCloseTo(0.927, 1e-3))
    }

    @Test
    fun `gate 5 should reproduce C-0008's saturated effective charge density at 2 mM`() {
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        assert(
            asymmetricSaturatedEffectiveChargeDensity(kappa, lb, negativeSurface = true)
                .isCloseTo(0.04562, 1e-4)
        )
    }

    // ----------------------------------------------------------- the answer, as a test

    @Test
    fun `the four-layer tile should bound the remainder where the single-layer sheet does not`() {
        // F5: if the thicker tile does not reduce the exposure, the original 1.66x stands.
        val singleLayer = bareChargeRatio(unpairedRemainder(M13MP18_NUCLEOTIDES, 15 * 112), 2 * 15 * 112)
        val fourLayer = bareChargeRatio(unpairedRemainder(M13MP18_NUCLEOTIDES, 60 * 112), 2 * 60 * 112)
        assert(singleLayer / fourLayer > 40.0)
        assert(fourLayer < 0.05)
    }
}
