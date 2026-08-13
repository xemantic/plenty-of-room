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
import com.xemantic.nano.plentyofroom.coupling.SeriesEntropicCoupling
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
import com.xemantic.nano.plentyofroom.equipartitionStiffness
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-23` — a **two-sided** compliant DNA coupling: an element that carries load in both
 * directions, or a demonstration that DNA offers none.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem
 * definition. **Sidedness is tested by evaluating the element's law at negative argument**,
 * never by inspecting its geometry — that is the whole methodological point of the task, and
 * it is what makes `E1`'s pass and `E2`'s failure results rather than assumptions.
 */
class TwoSidedCouplingTest {

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - two-sidedness is worth exactly one power of the position bound`() {
        // the force requirement of a ONE-SIDED stack (C-0021) is k_BT/sigma;
        // the stiffness requirement of a TWO-SIDED one is k_BT/sigma^2.
        // They are the same statement one power of the bound apart, and that identity is the
        // whole of this task's cheap bound.
        listOf(0.5, 1.0, 3.0, 7.5).forEach { bound ->
            assert(holdDownForceScale(bound).isCloseTo(equipartitionStiffness(bound) * bound))
        }
        assert(equipartitionStiffness(3.0).isCloseTo(0.460216, 1e-5))
        assert(holdDownForceScale(3.0).isCloseTo(1.380649, 1e-6))
    }

    @Test
    fun `gate 1 dimensional consistency - a transverse flexure stiffness is EI over a cubed length`() {
        val short = TransverseDuplexFlexure(230.0, 20.0, FlexureEndCondition.PINNED_ENDS, false)
        val long = TransverseDuplexFlexure(230.0, 40.0, FlexureEndCondition.PINNED_ENDS, false)
        // doubling the span divides the stiffness by exactly 8
        assert((short.bendingStiffness / long.bendingStiffness).isCloseTo(8.0))
        assert(short.bendingStiffness.isCloseTo(48.0 * 230.0 / (20.0 * 20.0 * 20.0)))
    }

    @Test
    fun `gate 1 dimensional consistency - a hinge stiffness is a torsional constant over a squared arm`() {
        val near = CrossoverHingeFlexure(13.53, 4.0, 230.0)
        val far = CrossoverHingeFlexure(13.53, 8.0, 230.0)
        // k = k_theta/r^2 in series with the arm's own bending 3EI/r^3; the hinge term quarters
        assert((13.53 / 16.0).isCloseTo(near.hingeTermStiffness))
        assert((near.hingeTermStiffness / far.hingeTermStiffness).isCloseTo(4.0))
    }

    @Test
    fun `gate 1 dimensional consistency - a mounting offset is a length and its preload is a force`() {
        // F = k q with k = F_target/(delta_target - q): a length in, a force out
        assert(mountingOffsetPreload(0.0, 100.0, 3.0).isCloseTo(0.0))
        assert(mountingOffsetPreload(0.34, 100.0, 3.0).isCloseTo(100.0 * 0.34 / 2.66))
        assert(mountingOffsetStiffness(0.0, 100.0, 3.0).isCloseTo(mandatedCouplingStiffness(100.0, 3.0)))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            TransverseDuplexFlexure(230.0, -1.0, FlexureEndCondition.PINNED_ENDS, false)
        }
        assertFailsWith<IllegalArgumentException> { CrossoverHingeFlexure(13.53, 0.0, 230.0) }
        assertFailsWith<IllegalArgumentException> { mountingOffsetPreload(3.0, 100.0, 3.0) }
        assertFailsWith<IllegalArgumentException> { oneSidedExcursionRms(0.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - sidedness is decided at negative argument and nowhere else`() {
        val probe = 0.5
        // E3, a transverse flexure: two-sided
        val flexure = TransverseDuplexFlexure(230.0, 25.0, FlexureEndCondition.PINNED_ENDS, false)
        assert(carriesCompression(flexure, probe))
        // E5, a crossover hinge: two-sided
        assert(carriesCompression(CrossoverHingeFlexure(13.53, 4.3, 230.0), probe))
        // E1, an axial duplex standoff: two-sided, and that is the point — it is excluded on
        // stiffness (C-0017's K1), not on sidedness
        assert(carriesCompression(AxialDuplexStandoff(1100.0, 5.0), probe))
        // E2, the committed ssDNA spacer path: ONE-SIDED, exactly
        val spacer = OneSidedSpacer(
            SeriesEntropicCoupling(1, 220.0, FreelyJointedChain(8.61, 2.10))
        )
        assert(!carriesCompression(spacer, probe))
        assert(spacer.reaction(-probe).isCloseTo(0.0))
    }

    @Test
    fun `gate 2 limiting cases - a symmetric flexure has an odd law`() {
        listOf(true, false).forEach { restrained ->
            val flexure = TransverseDuplexFlexure(
                230.0, 30.0, FlexureEndCondition.CLAMPED_ENDS, restrained
            )
            listOf(0.1, 1.0, 3.0).forEach { d ->
                assert(flexure.reaction(-d).isCloseTo(-flexure.reaction(d)))
                assert(flexure.tangentStiffness(-d).isCloseTo(flexure.tangentStiffness(d)))
            }
            assert(flexure.reaction(0.0).isCloseTo(0.0))
        }
    }

    @Test
    fun `gate 2 limiting cases - the two end conditions differ by exactly four`() {
        val pinned = TransverseDuplexFlexure(230.0, 30.0, FlexureEndCondition.PINNED_ENDS, false)
        val clamped = TransverseDuplexFlexure(230.0, 30.0, FlexureEndCondition.CLAMPED_ENDS, false)
        assert((clamped.bendingStiffness / pinned.bendingStiffness).isCloseTo(4.0))
    }

    @Test
    fun `gate 2 limiting cases - the membrane term vanishes at zero deflection and grows as the cube`() {
        val flexure = TransverseDuplexFlexure(230.0, 40.0, FlexureEndCondition.PINNED_ENDS, true)
        val free = TransverseDuplexFlexure(230.0, 40.0, FlexureEndCondition.PINNED_ENDS, false)
        // at zero deflection an axially restrained beam is indistinguishable from a free one
        assert(flexure.tangentStiffness(0.0).isCloseTo(free.tangentStiffness(0.0)))
        assert(flexure.membraneForce(0.0).isCloseTo(0.0))
        // and the membrane force is cubic: doubling the deflection multiplies it by ~8
        val ratio = flexure.membraneForce(0.2) / flexure.membraneForce(0.1)
        assert(abs(ratio - 8.0) < 0.05)
    }

    @Test
    fun `gate 2 limiting cases - an axially free flexure is linear and a restrained one is convex`() {
        val free = TransverseDuplexFlexure(230.0, 25.0, FlexureEndCondition.PINNED_ENDS, false)
        // linear: secant equals tangent, so it discharges placement and stability with one number
        assert(free.secantStiffness(3.0).isCloseTo(free.tangentStiffness(3.0)))
        val restrained = TransverseDuplexFlexure(230.0, 50.0, FlexureEndCondition.PINNED_ENDS, true)
        // convex: C-0017's theorem, and the whole tangent-over-secant ratio is free stability margin
        assert(restrained.tangentStiffness(3.0) > restrained.secantStiffness(3.0))
    }

    @Test
    fun `gate 2 limiting cases - the end draw-in vanishes as the square of the deflection`() {
        val flexure = TransverseDuplexFlexure(230.0, 25.0, FlexureEndCondition.PINNED_ENDS, false)
        assert(flexure.endDrawIn(0.0).isCloseTo(0.0))
        assert((flexure.endDrawIn(2.0) / flexure.endDrawIn(1.0)).isCloseTo(4.0))
        // 2.4 delta^2/L for BOTH end conditions — the shape factor cancels, which is not obvious
        assert(flexure.endDrawIn(3.0).isCloseTo(2.4 * 9.0 / 25.0))
        val clamped = TransverseDuplexFlexure(230.0, 25.0, FlexureEndCondition.CLAMPED_ENDS, false)
        assert(clamped.endDrawIn(3.0).isCloseTo(flexure.endDrawIn(3.0)))
    }

    @Test
    fun `gate 2 limiting cases - an antagonistic pair adds its stiffnesses and differences its preloads`() {
        val chain = FreelyJointedChain(40.0, 2.10)
        val balanced = AntagonisticSpacerPair(
            upCount = 4, upChain = chain, upPreExtension = 5.0,
            downCount = 4, downChain = chain, downSpan = 5.0
        )
        // identical parts, identical extensions: the pair carries no preload at all
        assert(balanced.reaction(0.0).isCloseTo(0.0))
        // and its stiffness is the SUM, which is what makes the preload independent of it
        val single = 4.0 * chain.tangentStiffness(chain.tension(5.0))
        assert(balanced.tangentStiffness(0.0).isCloseTo(2.0 * single))
        // an unbalanced pair pulls down, and the pair is two-sided even though neither part is
        val unbalanced = AntagonisticSpacerPair(
            upCount = 4, upChain = chain, upPreExtension = 4.0,
            downCount = 4, downChain = chain, downSpan = 6.0
        )
        assert(unbalanced.reaction(0.0) < 0.0)
        assert(carriesCompression(unbalanced, 0.2))
    }

    @Test
    fun `gate 2 limiting cases - a zero mounting offset is an unpreloaded coupling`() {
        val flexure = TransverseDuplexFlexure(230.0, 25.0, FlexureEndCondition.PINNED_ENDS, false)
        val unpreloaded = TwoSidedCoupling(45, flexure, 0.0)
        assert(unpreloaded.reaction(0.0).isCloseTo(0.0))
        // and it still resists an UPWARD excursion, which is the whole difference from K2
        assert(unpreloaded.reaction(-0.5) < 0.0)
        val preloaded = TwoSidedCoupling(45, flexure, 0.34)
        assert(preloaded.reaction(0.0) < 0.0)
        assert(preloaded.reaction(0.34).isCloseTo(0.0))
    }

    @Test
    fun `gate 2 limiting cases - the preload diverges as the mounting offset approaches the stroke`() {
        val near = mountingOffsetPreload(2.9, 100.0, 3.0)
        val nearer = mountingOffsetPreload(2.99, 100.0, 3.0)
        assert(nearer > near)
        assert(near > 1000.0)
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 conservation - the mounting offset and the preload invert each other exactly`() {
        listOf(0.001, 0.0414, 0.34, 1.0, 2.5).forEach { offset ->
            val preload = mountingOffsetPreload(offset, 100.0, 3.0)
            assert(offsetForPreload(preload, 100.0, 3.0).isCloseTo(offset))
        }
    }

    @Test
    fun `gate 3 conservation - the mounting offset reproduces C-0021's preload relation exactly`() {
        // F_down = (k_c - k_c*) delta*, computed from the OTHER end — a geometry rather than a
        // stiffness — and compared ABSOLUTELY, in pN, because near the mandate the two are a
        // catastrophic cancellation of each other (CLAUDE.md).
        val mandate = mandatedCouplingStiffness(100.0, 3.0)
        listOf(0.0, 0.01, 0.0414, 0.34, 1.0, 2.0).forEach { offset ->
            val stiffness = mountingOffsetStiffness(offset, 100.0, 3.0)
            val fromGeometry = mountingOffsetPreload(offset, 100.0, 3.0)
            val fromStiffness = couplingPreloadForStiffness(stiffness, mandate, 3.0)
            assert(abs(fromGeometry - fromStiffness) < 1e-9)
        }
    }

    @Test
    fun `gate 3 conservation - the analytic tangent matches a central difference of the law`() {
        listOf(true, false).forEach { restrained ->
            FlexureEndCondition.entries.forEach { end ->
                val flexure = TransverseDuplexFlexure(230.0, 35.0, end, restrained)
                listOf(0.5, 1.5, 3.0).forEach { d ->
                    val step = 1e-6
                    val numeric = (flexure.reaction(d + step) - flexure.reaction(d - step)) /
                            (2.0 * step)
                    assert(flexure.tangentStiffness(d).isCloseTo(numeric, 1e-6))
                }
            }
        }
    }

    @Test
    fun `gate 3 conservation - a two-sided coupling makes the excursion Gaussian and a one-sided one exponential`() {
        // the SAME quadrature C-0021 uses, over the two potentials, each reproducing its own
        // closed form: sqrt(k_BT/k) for a two-sided element, sqrt(2) k_BT/F for a one-sided one
        val stiffness = 33.333333333333336
        val twoSided = boltzmannPositionStatistics(
            netUpwardForce = { h -> -stiffness * (h - 10.0) },
            lower = 10.0 - 6.0, upper = 10.0 + 6.0, panels = 8000, reference = 10.0
        )
        assert(twoSided.rms.isCloseTo(sqrt(thermalEnergy() / stiffness), 1e-6))
        assert(twoSidedExcursionRms(stiffness).isCloseTo(twoSided.rms, 1e-6))
        val force = 1.380649
        val oneSided = boltzmannPositionStatistics(
            netUpwardForce = { h -> if (h >= 10.0) -force else 1.0e4 },
            lower = 10.0 - 0.01, upper = 10.0 + 400.0, panels = 200000, reference = 10.0
        )
        assert(oneSided.meanExcursionAbove.isCloseTo(thermalEnergy() / force, 1e-3))
        assert(oneSidedExcursionRms(force).isCloseTo(sqrt(2.0) * thermalEnergy() / force))
    }

    @Test
    fun `gate 3 conservation - a two-sided well is unbounded and a one-sided one is not`() {
        // the escape barrier is what separates a CONFINEMENT from a TRAP (C-0021), and it is the
        // property two-sidedness buys: the same coupling stiffness, mounted so it can push,
        // takes the barrier from a fixed number of k_BT to a quantity that grows with the domain
        val stiffness = 33.333333333333336
        fun barrier(domain: Double, twoSided: Boolean) = boltzmannPositionStatistics(
            netUpwardForce = { h ->
                if (h >= 10.0) (if (twoSided) -stiffness * (h - 10.0) else -0.5) else 1.0e4
            },
            lower = 9.99, upper = 10.0 + domain, panels = 20000, reference = 10.0
        ).escapeBarrier
        // quadratic against linear: doubling the domain quadruples the two-sided barrier and
        // exactly doubles the one-sided one
        assert((barrier(20.0, true) / barrier(10.0, true)).isCloseTo(4.0, 1e-3))
        assert((barrier(20.0, false) / barrier(10.0, false)).isCloseTo(2.0, 1e-3))
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the design span reproduces its own target secant stiffness`() {
        listOf(true, false).forEach { restrained ->
            FlexureEndCondition.entries.forEach { end ->
                val span = flexureSpanForStiffness(
                    bendingRigidity = 230.0, endCondition = end, axiallyRestrained = restrained,
                    stretchModulus = 1100.0, count = 45, targetStiffness = 100.0 / 3.0,
                    workingDisplacement = 3.0
                )
                val flexure = TransverseDuplexFlexure(230.0, span, end, restrained)
                assert((45.0 * flexure.secantStiffness(3.0)).isCloseTo(100.0 / 3.0, 1e-8))
            }
        }
    }

    @Test
    fun `gate 4 convergence - the design span is independent of the scan resolution`() {
        val spans = listOf(64, 256, 1024, 4096).map {
            flexureSpanForStiffness(
                230.0, FlexureEndCondition.PINNED_ENDS, true, 1100.0, 45, 100.0 / 3.0, 3.0,
                scanSteps = it
            )
        }
        spans.forEach { assert(it.isCloseTo(spans.first(), 1e-8)) }
    }

    @Test
    fun `gate 4 convergence - the design arm reproduces its own target stiffness`() {
        val arm = hingeArmForStiffness(
            hingeStiffness = 13.53, armBendingRigidity = 230.0, count = 45,
            targetStiffness = 100.0 / 3.0
        )
        val hinge = CrossoverHingeFlexure(13.53, arm, 230.0)
        assert((45.0 * hinge.tangentStiffness(0.0)).isCloseTo(100.0 / 3.0, 1e-8))
    }

    // ---------------------------------------------------------------- gate 5 — cross-check

    @Test
    fun `gate 5 cross-check - the crossover hinge constant is C-0009's own fitted value`() {
        // k_theta = 2 alpha B/(100 a) — CITED, fitted: Chen et al., JACS 136:6995 (2014) SI S2
        assert(Gen1Tile.crossoverHingeStiffness(1.0).isCloseTo(2.0 * 230.0 / (100.0 * 0.34)))
        assert(Gen1Tile.crossoverHingeStiffness(1.0).isCloseTo(13.529411764705884, 1e-9))
        // and its own experimental bracket is a factor of exactly 2
        assert(
            (Gen1Tile.crossoverHingeStiffness(1.2) / Gen1Tile.crossoverHingeStiffness(0.6))
                .isCloseTo(2.0)
        )
    }

    @Test
    fun `gate 5 cross-check - the axial standoff reproduces C-0017's K1`() {
        // C-0017: 45 duplex standoffs of 5 nm are 9900 pN nm, 297x the mandate
        val standoff = AxialDuplexStandoff(1100.0, 5.0)
        assert(standoff.tangentStiffness(0.0).isCloseTo(220.0))
        assert((45.0 * standoff.tangentStiffness(0.0)).isCloseTo(9900.0))
        assert(
            (45.0 * standoff.tangentStiffness(0.0) / mandatedCouplingStiffness(100.0, 3.0))
                .isCloseTo(297.0, 1e-3)
        )
    }

    @Test
    fun `gate 5 cross-check - the committed K2 path supplies exactly zero preload`() {
        // C-0021's M2, re-evaluated here through the signed interface rather than assumed
        val k2 = OneSidedSpacer(SeriesEntropicCoupling(45, 220.0, FreelyJointedChain(8.61, 2.10)))
        assert(k2.reaction(0.0).isCloseTo(0.0))
        assert(k2.reaction(-3.0).isCloseTo(0.0))
        assert(k2.tangentStiffness(-3.0).isCloseTo(0.0))
        // while it is emphatically present below L0
        assert(k2.reaction(3.0) > 90.0)
    }

    @Test
    fun `gate 5 cross-check - one base pair of mounting offset is nine times the thermal requirement`() {
        // the preload a design can actually SET is quantised by the rise per base pair, and the
        // smallest non-zero one is far above the requirement — which is why zero is the answer
        val quantum = mountingOffsetPreload(Gen1Tile.RISE_PER_BASE_PAIR, 100.0, 3.0)
        assert(quantum.isCloseTo(12.78195488721804, 1e-9))
        assert((quantum / holdDownForceScale(3.0)).isCloseTo(9.257, 1e-3))
        // and the offset the requirement actually asks for is a twelfth of a base pair
        val required = offsetForPreload(holdDownForceScale(3.0), 100.0, 3.0)
        assert(required.isCloseTo(0.0408554, 1e-5))
        assert((Gen1Tile.RISE_PER_BASE_PAIR / required).isCloseTo(8.3216, 1e-4))
    }
}
