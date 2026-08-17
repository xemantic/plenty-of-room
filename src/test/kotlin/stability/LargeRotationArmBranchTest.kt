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

package com.xemantic.nano.plentyofroom.stability

import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Task `T-157` — the gates of the multi-branch elastica for `C-0069`'s `Q5` (claim `C-0092`).
 *
 * Gate 3 carries the theorem the whole task rests on: `δ = ∫sin φ < L`, on **every** branch.
 */
class LargeRotationArmBranchTest {

    private val arm = recommendedArmBranches()

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - the contour bound is a length and it is the arm's own`() {
        assert(arm.contour.isCloseTo(8.16439083, 1e-6))
        assertTrue(arm.contour > 0.0)
    }

    @Test
    fun `gate 1 - unphysical entry points throw`() {
        assertFailsWith<IllegalArgumentException> { arm.branchesAt(-1.0) }
        assertFailsWith<IllegalArgumentException> { arm.branchesAt(1.0, scanSteps = 1) }
        assertFailsWith<IllegalArgumentException> { arm.branchesAt(1.0, shootingCeiling = 0.0) }
        assertFailsWith<IllegalArgumentException> { arm.forceForStroke(-0.1) }
        assertFailsWith<IllegalArgumentException> { arm.forceForStroke(arm.contour) }
        assertFailsWith<IllegalArgumentException> { arm.forceForStroke(2.0 * arm.contour) }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - at vanishing load the branch is C-0034's closed form`() {
        val small = arm.branchTable.first { it.stroke > 1.0e-3 }
        val secant = small.force / small.stroke
        assert(secant.isCloseTo(arm.smallRotationStiffnessPerArm, 2.0e-3))
    }

    @Test
    fun `gate 2 - the branch reproduces C-0069's placement at the acceptable stroke`() {
        val force = arm.forceForStroke(3.0)
        assert((GEN1_RECOMMENDED_PATH_COUNT * force / 3.0).isCloseTo(100.0 / 3.0, 1.0e-6))
    }

    @Test
    fun `gate 2 - a single root at small load, and more than one at large load`() {
        assertEquals(1, arm.branchesAt(5.0).size)
        assertTrue(arm.branchesAt(1000.0).size > 1)
    }

    // ------------------------------------------------------------- gate 3 — the theorem, and conservation

    @Test
    fun `gate 3 - the stroke is BELOW the contour on every enumerated branch`() {
        val everyBranch = listOf(1.0, 10.0, 100.0, 1000.0, 10000.0).flatMap { arm.branchesAt(it) }
        assertTrue(everyBranch.isNotEmpty())
        everyBranch.forEach {
            assertTrue(
                it.stroke < arm.contour,
                "a branch at F = ${it.force} reaches ${it.stroke} nm on a ${arm.contour} nm arm"
            )
        }
        // and on the continued branch too, at every table row
        arm.branchTable.forEach { assertTrue(it.stroke < arm.contour) }
    }

    @Test
    fun `gate 3 - the geometric bound needs no solver and is exact`() {
        // sin is bounded by one, so the integral cannot exceed the contour; equality would need
        // phi identically pi over two, which the near-end spring condition forbids
        assertTrue(strokeIsBelowContour(arm.contour, arm.strokeSupremum))
        assertFailsWith<IllegalArgumentException> {
            strokeIsBelowContour(-1.0, 0.5)
        }
    }

    @Test
    fun `gate 3 - the first integral is conserved along the continued branch`() {
        arm.branchTable.forEach {
            assertTrue(
                it.firstIntegralSpread <= BRANCH_FIRST_INTEGRAL_TOLERANCE,
                "first integral drifts by ${it.firstIntegralSpread} at a stroke of ${it.stroke}"
            )
        }
    }

    @Test
    fun `gate 3 - the branch's own moment equilibrium closes at every table row`() {
        arm.branchTable.forEach {
            assertTrue(
                abs(it.momentBalanceResidual) <= 1.0e-6 * (1.0 + abs(it.force) * arm.contour),
                "moment balance is ${it.momentBalanceResidual} at a stroke of ${it.stroke}"
            )
        }
    }

    @Test
    fun `gate 3 - the continued branch stays on the small-rotation side of a right angle`() {
        arm.branchTable.forEach {
            assertTrue(
                it.maximumRotation < 0.5 * PI,
                "max|phi| = ${it.maximumRotation} past a right angle at a stroke of ${it.stroke}"
            )
        }
        // it approaches it, which is why the reaction diverges as the stroke reaches the contour
        assertTrue(arm.branchTable.last().maximumRotation > 0.999 * 0.5 * PI)
    }

    @Test
    fun `gate 3 - the stroke is strictly increasing along the continued branch`() {
        arm.branchTable.zipWithNext().forEach { (a, b) ->
            assertTrue(b.stroke > a.stroke, "stroke ${b.stroke} after ${a.stroke}")
            assertTrue(b.force > a.force, "force ${b.force} after ${a.force}")
        }
    }

    // ------------------------------------------------------------------ gate 4 — convergence

    @Test
    fun `gate 4 - the deepest branch point is converged in the RK4 step count`() {
        val target = arm.strokeSupremum - 1.0e-4
        val coarse = recommendedArmBranches(steps = 400).forceForStroke(target)
        val fine = recommendedArmBranches(steps = 1600).forceForStroke(target)
        assert(coarse.isCloseTo(fine, 5.0e-3))
    }

    @Test
    fun `gate 4 - the supremum is converged in the continuation step`() {
        val coarse = recommendedArmBranches(rotationStep = 4.0e-3).strokeSupremum
        val fine = recommendedArmBranches(rotationStep = 1.0e-3).strokeSupremum
        // both must sit inside the contour and agree to well under a base-pair rise
        assertTrue(coarse < arm.contour && fine < arm.contour)
        assertTrue(abs(coarse - fine) < 0.01)
    }

    // ------------------------------------------------------------------ gate 5 — upstream

    @Test
    fun `gate 5 - C-0084's refusal is REPRODUCED, and it is a force-ladder artefact`() {
        // the doubling ladder in C-0039's own forceForDisplacement refuses near here
        val refusal = ladderRefusalStroke()
        assert(refusal.isCloseTo(7.91968584, 1.0e-4))
        // and the branch answers well past it
        assertTrue(arm.strokeSupremum > refusal)
        assertTrue(arm.strokeSupremum - refusal > 0.2)
        // the reaction at a stroke the ladder refuses is finite and positive
        val beyond = 0.5 * (refusal + arm.strokeSupremum)
        assertTrue(arm.forceForStroke(beyond) > 0.0)
    }

    @Test
    fun `gate 5 - the window the contour bound leaves unexplored is quoted as a length`() {
        val window = arm.contour - arm.strokeSupremum
        assertTrue(window > 0.0)
        assertTrue(window < 0.34, "the unexplored window is $window nm, more than one base-pair rise")
    }

    // ------------------------------------------------- the falsifiers, as executable tests

    @Test
    fun `falsifier F3 - no branch reaches the contour, and it CANNOT`() {
        val deepest = (arm.branchTable.maxOf { it.stroke })
        assertTrue(deepest < arm.contour)
        listOf(1.0, 50.0, 500.0, 5000.0, 50000.0).forEach { force ->
            arm.branchesAt(force).forEach {
                assertTrue(it.stroke < arm.contour, "F = $force reached ${it.stroke}")
            }
        }
    }
}
