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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.poroelastic.POROELASTIC_RESULT_FLOOR
import kotlin.test.Test

/**
 * `T-278` / `CH-0223` — the precision the seven previously unrounded emitters are given, and the
 * one of them for which the **floor** rather than the digit count is the judgement.
 *
 * `CH-0223` declines its own repair on the ground that *"the digit count is a judgement per
 * study"*, and names `T-1` and `T-1c` as determined to `SOLVED_HEIGHT_SIGNIFICANT_DIGITS` because
 * they are *"downstream of a solved SCF height"*. Neither study names `SelfConsistentField` or
 * `heightAtPressure` anywhere on its path — their layer models are the trial-function ones in
 * `brush/GraftedLayer.kt` and `brush/BrushCompression.kt`, whose roots close at `1e-15` and whose
 * `heightUnderLoad` is a hundred bisection halvings. By `P-18`'s own provenance rule — *"the
 * loosest solver tolerance on any path from a model input to it"* — all seven are the shared
 * `RESULT_SIGNIFICANT_DIGITS` site.
 *
 * What is **not** shared is the absolute floor, and that is `P-18`'s other rule: *an absolute
 * floor is a claim about UNITS and it does not travel.*
 */
class EmitterPrecisionTest {

    /** `T-7`'s smallest committed non-zero value: an inertial time, in **seconds**. */
    private val smallestInertialTime = 6.96645e-14

    /**
     * A strong-stretching layer's stiffness at its own resting height, in `pN/nm` — the quantity
     * `CLAUDE.md` records as **exactly zero** for the Milner-Witten-Cates form, "because the
     * brush's outer edge is diffuse".
     */
    private val strongStretchingEquilibriumStiffness = 2.1802043040789882e-13

    // --- gate 1: the floor is a claim in the locked units, and T-7 does not emit in them --------

    @Test
    fun `the default floor would flatten an inertial time that is not a force`() {
        // `RESULT_ABSOLUTE_FLOOR` is documented as a magnitude in the locked units — "no force
        // below a nanopiconewton is of interest". Half a picosecond is not a force.
        assert(roundForResult(smallestInertialTime) == 0.0)
    }

    @Test
    fun `T-7's own floor keeps it`() {
        assert(roundForResult(smallestInertialTime, floor = POROELASTIC_RESULT_FLOOR) > 0.0)
        assert(
            roundForResult(smallestInertialTime, floor = POROELASTIC_RESULT_FLOOR) ==
                    smallestInertialTime
        )
    }

    // --- gate 2: and where the physics says zero, the default floor is the honest reading -------

    @Test
    fun `the default floor states a strong-stretching resting stiffness as exactly zero`() {
        // Not a loss of information: the MWC pressure vanishes quadratically at `L0`, so this is
        // `RESULT_ABSOLUTE_FLOOR`'s own documented case — `T-5`'s zero internal shear, returned
        // by the solver as `1e-14 pN` whose digits are pure roundoff.
        assert(roundForResult(strongStretchingEquilibriumStiffness) == 0.0)
    }

    @Test
    fun `and a stiffness of any physical interest survives it`() {
        // The same study's compressed stiffnesses, four decades above the floor, are untouched.
        assert(roundForResult(0.24099739584151927) == 0.240997396)
    }

    // --- gate 3: nine digits is the ceiling and these seven are entitled to it ------------------

    @Test
    fun `nine digits is what a solver tolerance of 1e-15 determines`() {
        // `determinedDigits` saturates the clamp for anything at or below `1e-9`, which is
        // `P-18`'s rule applied to `bracketedRoot`'s own default and to `GraftedLayer`'s
        // `CONVERGENCE`.
        assert(determinedDigits(1e-15) == RESULT_SIGNIFICANT_DIGITS)
        assert(determinedDigits(1e-9) == RESULT_SIGNIFICANT_DIGITS)
        // And it does NOT for a solved SCF height, which is the hazard `CH-0223` names — correctly
        // as a rule, and about two studies that do not use that solver.
        assert(determinedDigits(1.2e-7) == SOLVED_HEIGHT_SIGNIFICANT_DIGITS)
    }
}
