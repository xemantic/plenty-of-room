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

package com.xemantic.nano.plentyofroom.brush

import com.xemantic.kotlin.test.assert
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `P-15` — the repair of [bracketedRoot]'s sign tests.
 *
 * ## What was wrong
 *
 * Both sign tests in the original were written on a **product**, and a product of two doubles
 * underflows long before either factor does. `C-0019` (`S-143`) observed the consequence: the
 * solver evaluating its function *outside its own bracket*.
 *
 * - The **step** test, `atLeft * atEstimate < 0.0`, underflows to `−0.0` when both factors are
 *   tiny and of opposite sign. `−0.0 < 0.0` is `false`, so the branch that should have moved the
 *   *right* endpoint moves the *left* one instead — and the left endpoint is replaced by a value
 *   of the **same sign as the right**. The bracket is gone, the invariant that makes regula falsi
 *   an interpolation no longer holds, and the next secant step is an **extrapolation** that
 *   leaves `[low, high]` entirely.
 * - The **entry** test, `require(atLeft * atRight <= 0.0)`, underflows the other way: two tiny
 *   values of the *same* sign multiply to `+0.0`, which satisfies `<= 0.0`, so a bracket with no
 *   sign change in it is accepted and the solver returns a number that is not a root of anything.
 *
 * Neither is a tolerance question, and neither is fixed by more iterations: the magnitudes that
 * trigger them are perfectly representable and every individual value is exact. It is only their
 * *product* that is not, which is why the failure is invisible in the residual.
 *
 * ## Why it bites this project specifically
 *
 * `CLAUDE.md` records the condition under which it fires — a residual spanning decades — and the
 * disjoining pressure of a grafted layer is exactly that: at a 30 nm gap it is four orders of
 * magnitude below the two terms it is the difference of. The three claims that consume this
 * routine (`C-0003`, `C-0011`, `C-0016`) all invert a height or a chain length against it.
 *
 * The fix is to test **signs**, never products, and to keep the interpolant honest by falling
 * back to bisection whenever the secant step lands outside the live bracket. The scale-invariance
 * test below is the one that states the property in the form it should always have had: the root
 * a solver finds must not depend on the units the residual is expressed in.
 */
class BracketedRootTest {

    /**
     * The scale that makes a product of two residuals underflow while every residual is itself
     * an ordinary, exactly-representable double. `1e-170 * 1e-170 = 1e-340`, below the smallest
     * normal double (`~2.2e-308`) and into the subnormals or straight to zero.
     */
    private val underflowingScale = 1e-170

    @Test
    fun `should find the root of a residual small enough to underflow a product of two of them`() {
        val root = bracketedRoot(0.01, 1.0) { x ->
            underflowingScale * (x * x * x - 0.027)
        }
        assert(abs(root - 0.3) < 1e-9)
    }

    @Test
    fun `should never evaluate the function outside the bracket it was given`() {
        val low = 0.01
        val high = 1.0
        val visited = mutableListOf<Double>()
        bracketedRoot(low, high) { x ->
            visited += x
            underflowingScale * (x * x * x - 0.027)
        }
        assert(visited.isNotEmpty())
        visited.forEach { x ->
            assert(x in low..high) { "evaluated outside [$low, $high]: $x" }
        }
    }

    /**
     * The property the product test destroys, stated directly: multiplying the residual by a
     * positive constant cannot move its root. The solver is the same solver, the function is the
     * same function up to units, and only the exponent range differs.
     */
    @Test
    fun `should return the same root whatever positive scale the residual carries`() {
        val reference = bracketedRoot(0.01, 1.0) { x -> x * x * x - 0.027 }
        listOf(1e-170, 1e-80, 1e-8, 1e8, 1e80).forEach { scale ->
            val scaled = bracketedRoot(0.01, 1.0) { x -> scale * (x * x * x - 0.027) }
            assert(abs(scaled - reference) < 1e-9) {
                "root moved from $reference to $scaled when the residual was scaled by $scale"
            }
        }
    }

    /**
     * The entry test's own underflow. Two residuals of the *same* sign, small enough that their
     * product underflows to `+0.0`, must still be rejected: there is no sign change in `[low, high]`
     * and no root to find.
     */
    @Test
    fun `should reject a bracket whose endpoints share a sign however small they are`() {
        assertFailsWith<IllegalArgumentException> {
            bracketedRoot(0.0, 1.0) { _ -> 1e-200 }
        }
        assertFailsWith<IllegalArgumentException> {
            bracketedRoot(0.0, 1.0) { x -> 1e-200 * (1.0 + x) }
        }
        assertFailsWith<IllegalArgumentException> {
            bracketedRoot(0.0, 1.0) { x -> -1e-200 * (1.0 + x) }
        }
    }

    /**
     * The property that closes the causal chain in `S-143`, and the reason the diagnosis there can
     * be confirmed without reproducing the run that produced it.
     *
     * A secant through two points whose ordinates have **opposite signs** crosses zero *between*
     * them: the interpolated abscissa is a convex combination of the two, with weights
     * `|f_right|/(|f_left| + |f_right|)` and `|f_left|/(|f_left| + |f_right|)`, both in `[0, 1]`.
     * So while the bracket holds, an escape is arithmetically impossible.
     *
     * Therefore **an evaluation outside `[low, high]` implies the bracket was lost**, and the only
     * thing in this routine that can lose it is the sign test. `C-0019` observed the escape — "an
     * evaluation a fifth of the way below the dry thickness" — which is the observation, and this
     * is the theorem that turns it into the diagnosis.
     *
     * Worth recording honestly: the product underflows only once **both** residuals are below
     * ~`1.5e-154`, and residuals shaped like this project's — pressures in pN/nm², lengths in nm,
     * spanning at most ~30 decades — do not reach that in a direct probe of the broken routine.
     * The escape `C-0019` saw was real and is reproduced here at `1e-170`; which physical inversion
     * drove its residual that small was not re-identified, and is not claimed to be.
     */
    @Test
    fun `should keep every secant step interior while the bracket holds`() {
        // A deliberately lopsided bracket, where the interpolation weight is ~1e-6 : 1.
        listOf(1.0, 1e-6, 1e6, 1e-170, 1e170).forEach { scale ->
            val low = 0.25
            val high = 400.0
            val visited = mutableListOf<Double>()
            bracketedRoot(low, high) { x ->
                visited += x
                scale * (1.0 / x - 1.0 / 300.0)
            }
            visited.forEach { x ->
                assert(x in low..high) { "escaped [$low, $high] at scale $scale: $x" }
            }
        }
    }

    @Test
    fun `should solve an ordinary root to the requested tolerance in both orientations`() {
        val increasing = bracketedRoot(0.0, 2.0) { x -> x * x - 2.0 }
        assert(abs(increasing - 1.4142135623730951) < 1e-12)
        val decreasing = bracketedRoot(0.0, 2.0) { x -> 2.0 - x * x }
        assert(abs(decreasing - 1.4142135623730951) < 1e-12)
    }

    @Test
    fun `should return an endpoint that is itself the root`() {
        assert(bracketedRoot(1.0, 3.0) { x -> x - 1.0 } == 1.0)
        assert(bracketedRoot(1.0, 3.0) { x -> x - 3.0 } == 3.0)
    }

    /**
     * The stagnation regula falsi is famous for, and the reason Illinois is used here at all: a
     * strongly convex residual keeps one endpoint fixed, so the bracket must be forced to shrink
     * from that side. The evaluation budget is the point — every evaluation in this project can
     * cost a profile quadrature, and the doc comment claims "roughly an eightfold saving".
     *
     * This is the test that caught the **unconditional** halving (`P-15`). Deflating both
     * residuals once the estimate alternates sides makes the secant interpolate to the midpoint,
     * so the method becomes bisection while still paying for a secant — and the defect is
     * invisible in the answer, which is correct to the last ulp either way. Bisection needs
     * `log₂(1 / (1e-15 · root))` ≈ 55 evaluations here and the broken form took 73; the repair
     * takes 35. The bound below is set to fail if it ever degenerates back.
     */
    @Test
    fun `should converge on a strongly convex residual well inside a bisection budget`() {
        var evaluations = 0
        val root = bracketedRoot(0.0, 1.0) { x ->
            evaluations++
            x * x * x * x * x - 1e-8
        }
        // x⁵ = 1e-8 at x = 10^(−8/5).
        assert(abs(root - 0.025118864315095801) < 1e-15)
        assert(evaluations < 45) { "took $evaluations evaluations, bisection would take ~55" }
    }

    /**
     * The same budget property on the well-conditioned root every textbook uses, where the
     * degeneration is starkest: 52 evaluations broken against 11 repaired, for an answer that is
     * bit-identical in both.
     */
    @Test
    fun `should converge on a well-conditioned root in far fewer evaluations than bisection`() {
        var evaluations = 0
        val root = bracketedRoot(0.0, 2.0) { x ->
            evaluations++
            x * x - 2.0
        }
        assert(abs(root - 1.4142135623730951) < 1e-15)
        assert(evaluations < 20) { "took $evaluations evaluations, bisection would take ~51" }
    }

    /**
     * A residual that is flat to machine precision over most of the bracket — the shape
     * `CLAUDE.md` warns about, where the answer is a difference four orders of magnitude below
     * the terms it comes from. It must terminate on the bracket width rather than on a residual
     * test that can never be satisfied.
     */
    @Test
    fun `should terminate on the bracket width when the residual has a noise floor`() {
        val root = bracketedRoot(1.0, 1e4) { x ->
            val exact = 1.0 / x - 1.0 / 300.0
            // quantised to a floor no smaller than 1e-18, as a quadrature of ~1e3 terms would be
            Math.round(exact * 1e18).toDouble() * 1e-18
        }
        assert(abs(root - 300.0) < 1e-6)
    }
}
