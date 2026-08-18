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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * `T-196` — where the four-layer tile stops being flat.
 *
 * These tests are the **pure** half of the task: the algebra that makes the threshold a
 * one-dimensional inversion rather than a plate sweep, and the crossing search that `CLAUDE.md`
 * insists must scan for a **first sign change** before it bisects. The plate solves live in
 * `CompositeFractionThresholdStudy` and are graded there.
 */
class CompositeFractionThresholdTest {

    // --- gate 1: dimensional consistency, and the identity that makes this cheap --------------
    //
    // `f` enters `multiLayerRigidities` only through `realised = 1 + f(factor - 1)`, and that ONE
    // number multiplies BOTH rigidities. So `f` is a pure SCALE on the plate, and the threshold
    // can be found by inverting a scalar instead of sweeping a two-dimensional design.

    @Test
    fun `the realised enhancement is affine in the composite fraction`() {
        val factor = 39.4
        assert(enhancementForFraction(0.0, factor).isCloseTo(1.0))
        assert(enhancementForFraction(1.0, factor).isCloseTo(factor))
        // Affine: the midpoint of the fractions maps to the midpoint of the enhancements.
        val low = enhancementForFraction(0.2, factor)
        val high = enhancementForFraction(0.4, factor)
        assert(enhancementForFraction(0.3, factor).isCloseTo((low + high) / 2.0))
    }

    @Test
    fun `the inversion returns the fraction the enhancement was built from`() {
        val factor = 39.4
        for (fraction in listOf(0.0, 0.05, 0.26, 0.30, 0.33, 1.0)) {
            val enhancement = enhancementForFraction(fraction, factor)
            assert(fractionForEnhancement(enhancement, factor).isCloseTo(fraction))
        }
    }

    @Test
    fun `both rigidities scale by the same factor, so f is a pure scale`() {
        fun rigidities(fraction: Double) = multiLayerRigidities(
            layers = 4,
            interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = LayerCoupling.CALIBRATED,
            compositeFraction = fraction
        )
        val base = rigidities(0.0)
        for (fraction in listOf(0.1, 0.26, 0.30, 0.5, 1.0)) {
            val scaled = rigidities(fraction)
            val along = scaled.alongHelixRigidity / base.alongHelixRigidity
            val across = scaled.acrossHelixRigidity / base.acrossHelixRigidity
            // The same scale in both directions, to the last ulp the arithmetic allows.
            assert(abs(along - across) < 1e-12 * along)
            // And it IS the affine enhancement, which is what licenses inverting a scalar
            // instead of sweeping the plate: `parallelAxisFactor` is pure geometry and does not
            // depend on `f`, so the same `factor` reads off the `f = 0` rigidities.
            assert(along.isCloseTo(enhancementForFraction(fraction, base.parallelAxisFactor)))
        }
    }

    @Test
    fun `a fraction outside the unit interval is refused`() {
        assertFailsWith<IllegalArgumentException> { enhancementForFraction(-0.01, 39.4) }
        assertFailsWith<IllegalArgumentException> { enhancementForFraction(1.01, 39.4) }
        // A factor of one is a single layer: there is no excess to take a fraction of.
        assertFailsWith<IllegalArgumentException> { fractionForEnhancement(1.0, 1.0) }
    }

    // --- gate 2: limiting cases ----------------------------------------------------------------

    @Test
    fun `the crossing of a strictly decreasing function is found and bracketed`() {
        // A stand-in with a known root: dishing(f) = 0.31 - 0.8 f crosses 0.10 at f = 0.2625.
        val bisectionTolerance = 1e-9
        val crossing = firstCrossing(0.0, 1.0, 40, 0.10, tolerance = bisectionTolerance) {
            0.31 - 0.8 * it
        }
        assert(crossing != null)
        // The root is asserted to the precision the BISECTION promises -- an absolute bracket
        // width on the abscissa -- and not to `isCloseTo`'s 1e-9 RELATIVE default, which is a
        // tighter statement the search never made. `CLAUDE.md`: derive such a tolerance from the
        // precision the producer guarantees, never from what happens to pass.
        assert(abs(crossing!!.root - 0.2625) <= bisectionTolerance)
        assert(crossing.monotone)
        // CONTAINMENT, inclusive. A bisection converges its bracket ONTO the root, so an endpoint
        // landing exactly on it is the expected terminal state, not a defect -- and a strict
        // comparison here would report that tie as a finding (`CLAUDE.md`, and it did on the
        // first run of this very test: bracketHigh came back exactly 0.2625).
        assert(crossing.bracketLow <= crossing.root)
        assert(crossing.root <= crossing.bracketHigh)
        assert(crossing.bracketLow <= 0.2625)
        assert(0.2625 <= crossing.bracketHigh)
        assert(crossing.signChanges == 1)
    }

    @Test
    fun `a function that never reaches the target returns null rather than a clamped root`() {
        // `C-0031`'s rule: a root-finder handed a target it never reaches returns null, and the
        // null is the VERDICT -- here it would mean "no coupling in [0,1] makes the tile flat".
        assertNull(firstCrossing(0.0, 1.0, 40, 0.10) { 0.5 - 0.1 * it })
        // And the other way: already below the target at the low end means the crossing is not
        // in the interval either, which is a different verdict and must not be confused with it.
        assertNull(firstCrossing(0.0, 1.0, 40, 0.10) { 0.05 - 0.01 * it })
    }

    @Test
    fun `a non-monotone function is reported as such rather than bisected silently`() {
        // `CLAUDE.md`: a verdict that is not monotone in a swept variable has no threshold, and
        // sweeping it finer finds more alternation rather than less. The search must SAY so.
        val crossing = firstCrossing(0.0 , 1.0, 40, 0.10) { 0.11 - 0.4 * it * (1.0 - it) }
        assert(crossing != null)
        assert(!crossing!!.monotone)
        assert(crossing.signChanges > 1)
    }

    // --- gate 3: symmetry and conservation ----------------------------------------------------

    @Test
    fun `the crossing is invariant under a reparametrisation that preserves the ordering`() {
        // The same physics read on the ENHANCEMENT axis rather than the FRACTION axis must give
        // the same design point, which is what licenses the inversion in the study.
        val factor = 39.4
        val bisectionTolerance = 1e-9
        val onFraction = firstCrossing(0.0, 1.0, 200, 0.10, tolerance = bisectionTolerance) {
            0.31 - 0.8 * it
        }!!.root
        // On the enhancement axis the same absolute bracket is `factor - 1` times FINER in
        // fraction, so the comparison is owed only the coarser of the two.
        val onEnhancement = firstCrossing(
            enhancementForFraction(0.0, factor), enhancementForFraction(1.0, factor), 200, 0.10,
            tolerance = bisectionTolerance
        ) { 0.31 - 0.8 * fractionForEnhancement(it, factor) }!!.root
        assert(
            abs(fractionForEnhancement(onEnhancement, factor) - onFraction) <= bisectionTolerance
        )
    }

    // --- gate 4: numerical convergence ---------------------------------------------------------

    @Test
    fun `refining the scan does not move the root, because the bisection owns the precision`() {
        val roots = listOf(20, 40, 80, 160).map {
            firstCrossing(0.0, 1.0, it, 0.10) { f -> 0.31 - 0.8 * f }!!.root
        }
        roots.forEach { assert(abs(it - 0.2625) < 1e-9) }
    }

    @Test
    fun `the bisection tolerance is honoured and is reported`() {
        val loose = firstCrossing(0.0, 1.0, 40, 0.10, tolerance = 1e-3) { 0.31 - 0.8 * it }!!
        val tight = firstCrossing(0.0, 1.0, 40, 0.10, tolerance = 1e-12) { 0.31 - 0.8 * it }!!
        assert(loose.bracketHigh - loose.bracketLow < 1e-3)
        assert(tight.bracketHigh - tight.bracketLow < 1e-12)
        assert(abs(loose.root - tight.root) < 1e-3)
    }
}
