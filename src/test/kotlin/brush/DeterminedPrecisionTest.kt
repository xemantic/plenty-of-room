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
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.RESULT_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.SOLVED_HEIGHT_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.determinedDigits
import com.xemantic.nano.plentyofroom.structure.roundForResult
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test

/**
 * `P-18`. The digits this project prints are not the digits it determines, and the gap is
 * measurable rather than assumed (`CH-0043`).
 *
 * These tests run on a **deliberately coarse** grid and a short chain: the quantity under test is
 * a *property of the solver*, not of the Gen-1 layer, and `CLAUDE.md` records that the cheapest
 * place to evaluate an SCF layer is never its own floor. The production sweep lives in
 * `DeterminedPrecisionStudy`.
 */
class DeterminedPrecisionTest {

    private val peg = PegWater()

    private val interaction = desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)

    /** Coarse on purpose — a solver property, exhibited cheaply. */
    private val grid = ScfDiscretisation(nodeSpacing = 0.5, contourStepsPerMonomer = 1.0)

    private fun layer(tolerance: Double) =
        SelfConsistentFieldLayer(interaction, grid, heightTolerance = tolerance)

    private val chain = peg.graftedChain(120.0, 0.0240)

    @Test
    fun `gate 2 limiting cases - the standing height tolerance should be the default`() {
        val standing = SelfConsistentFieldLayer(interaction, grid)
        assert(standing.heightTolerance == 1e-6)
    }

    @Test
    fun `gate 1 dimensional consistency - a height tolerance is relative so it must be dimensionless in its effect`() {
        // the same relative tolerance applied to two different pressures must pin both roots to
        // the same RELATIVE width, not the same absolute one
        val solver = layer(1e-6)
        val loose = layer(1e-3)
        listOf(1.0 / 1600.0, 100.0 / 1600.0).forEach { pressure ->
            val tight = solver.heightAtPressure(chain, pressure)
            val coarse = loose.heightAtPressure(chain, pressure)
            assert(abs(coarse - tight) / tight < 1e-2)
        }
    }

    @Test
    fun `gate 4 numerical convergence - tightening the height tolerance should move the answer by no more than the tolerance it left`() {
        // the predicate CH-0043 rests on: a solved height is pinned to its own declared tolerance
        // and to nothing finer
        val reference = layer(1e-9).heightAtPressure(chain, 100.0 / 1600.0)
        val standing = layer(1e-6).heightAtPressure(chain, 100.0 / 1600.0)
        val movement = abs(standing - reference) / reference
        assert(movement < 1e-4)
        assert(determinedDigits(movement) < RESULT_SIGNIFICANT_DIGITS)
    }

    @Test
    fun `gate 4 numerical convergence - the solve count should rise when the tolerance is tightened`() {
        // the cost of the honest direction, exhibited rather than asserted
        val loose = layer(1e-4)
        val tight = layer(1e-9)
        loose.heightAtPressure(chain, 100.0 / 1600.0)
        tight.heightAtPressure(chain, 100.0 / 1600.0)
        assert(loose.solveCount > 0)
        assert(tight.solveCount >= loose.solveCount)
    }

    @Test
    fun `gate 3 symmetry - the residual the height bracket works on is discontinuous at a node-count boundary`() {
        // `M = round(h/dz)` is a STEP function of h, so no tolerance can resolve the root inside
        // the jump. This is what makes tightening HEIGHT_TOLERANCE unreachable rather than
        // merely expensive.
        val solver = layer(1e-6)
        val spacing = grid.nodeSpacing
        val resting = solver.equilibriumHeight(chain)
        val target = 0.8 * resting
        val boundary = (((target / spacing) - 0.5).roundToInt() + 0.5) * spacing
        val below = boundary * (1.0 - 1e-9)
        val above = boundary * (1.0 + 1e-9)
        // the node counts really do differ across the boundary
        assert((below / spacing).roundToInt() != (above / spacing).roundToInt())
        val jump = relativeMovement(
            solver.disjoiningPressure(chain, above), solver.disjoiningPressure(chain, below)
        )
        // a jump at all: the function is not continuous, so `determinedDigits` of it is finite
        assert(jump > 0.0)
        assert(determinedDigits(jump) <= RESULT_SIGNIFICANT_DIGITS)
    }

    @Test
    fun `gate 4 numerical convergence - the same root reached from two brackets should agree only to the tolerance`() {
        // this is what `P-15` perturbed: not the tolerance, but the PATH to the root at a fixed
        // tolerance. Two brackets, one residual, one tolerance — the spread is the width of the
        // band the answer is free to sit anywhere inside, and it is what makes nine printed
        // digits a statement about the code path.
        val solver = layer(1e-6)
        val target = 100.0 / 1600.0
        val seed = solver.heightAtPressure(chain, target)
        fun rootFrom(low: Double, high: Double): Double = kotlin.math.exp(
            bracketedRoot(
                kotlin.math.ln(seed * low), kotlin.math.ln(seed * high),
                tolerance = 1e-6, iterations = 60
            ) { logHeight ->
                kotlin.math.ln(solver.pressureAt(chain, kotlin.math.exp(logHeight)) / target)
            }
        )
        val spread = relativeMovement(rootFrom(1.0 / 3.0, 1.5), rootFrom(0.5, 2.0))
        // the roots agree to the tolerance and are not required to agree beyond it
        assert(spread < 1e-4)
        assert(determinedDigits(spread) >= SOLVED_HEIGHT_SIGNIFICANT_DIGITS - 2)
    }

    @Test
    fun `gate 3 symmetry - a movement against a zero reference should be compared absolutely`() {
        assert(relativeMovement(1e-14, 0.0) == 1e-14)
        assert(relativeMovement(2.0, 1.0) == 1.0)
        assert(relativeMovement(1.0, 1.0) == 0.0)
    }

    @Test
    fun `gate 2 limiting cases - the pipeline should emit every quantity the T-1f record carries`() {
        val solver = layer(1e-4)
        val quantities = pipelineQuantities(solver, chain)
        listOf(
            "monomersPerChain", "restingHeight", "pressureAtNineTenths",
            "stiffnessAtNineTenths", "stiffnessAtFourFifths", "stiffnessAtSevenTenths",
            "stiffnessAtHeldGap", "heightAtTargetForce", "strokeUnderTargetForce",
            "secantStiffness"
        ).forEach { assert(quantities.containsKey(it)) }
        assert(quantities.values.all { it.isFinite() })
    }

    @Test
    fun `gate 2 limiting cases - the declared SCF emission precision should not exceed the measured determinacy`() {
        // the constant the emitters are changed to must be no finer than the tolerance underneath
        assert(SOLVED_HEIGHT_SIGNIFICANT_DIGITS <= determinedDigits(1e-6))
        assert(SOLVED_HEIGHT_SIGNIFICANT_DIGITS < RESULT_SIGNIFICANT_DIGITS)
    }

    @Test
    fun `gate 3 symmetry - a quantity rounded to the SCF precision should be invariant under a tolerance change`() {
        // the acceptance predicate of P-18, as a test: after the change, moving the solver knob
        // inside its licence must not move a printed digit of the height
        val reference = layer(1e-9).equilibriumHeight(chain)
        val standing = layer(1e-6).equilibriumHeight(chain)
        assert(
            roundForResult(reference, SOLVED_HEIGHT_SIGNIFICANT_DIGITS) ==
                    roundForResult(standing, SOLVED_HEIGHT_SIGNIFICANT_DIGITS)
        )
    }

}
