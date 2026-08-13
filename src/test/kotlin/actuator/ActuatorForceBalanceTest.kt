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

package com.xemantic.nano.plentyofroom.actuator

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.brush.AlexanderBoxLayer
import com.xemantic.nano.plentyofroom.brush.GraftedChain
import com.xemantic.nano.plentyofroom.brush.StrongStretchingLayer
import com.xemantic.nano.plentyofroom.brush.chainLengthForHeight
import com.xemantic.nano.plentyofroom.brush.desCloizeauxInteraction
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.brush.heightUnderLoad
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.material.PegWater
import kotlin.math.abs
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The coupled solve — the thing `T-3` exists to do, and the thing that is routinely skipped.
 *
 * The blocking force and the stroke are **different quantities**: the blocking force is the
 * force at zero displacement, where the layer carries nothing; the stroke is where the
 * electrostatic force is balanced by the layer's own restoring force. A stroke is therefore
 * a root of `|F_es(h, V)| − F_layer(h)`, never a force divided by a stiffness — the layer's
 * `P(h)` is strongly nonlinear and `C-0003` supplies it directly.
 */
class ActuatorForceBalanceTest {

    private val peg = PegWater()

    private val geometry = ActuatorGeometry()

    private val interaction = desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)

    private val model = StrongStretchingLayer(interaction)

    private val chain: GraftedChain = peg.graftedChain(
        monomersPerChain = model.chainLengthForHeight(peg, 10.0, 0.024),
        graftingDensity = 0.024
    )

    private val balance = ActuatorForceBalance(model, chain, geometry)

    private fun curve(amplitude: Double, decayLength: Double) =
        electrostaticForceCurve(DoubleArray(121) { 0.5 + it * 0.1 }) {
            -amplitude * exp(-it / decayLength)
        }

    @Test
    fun `gate 1 dimensional consistency - the blocking force is the electrostatic force at zero displacement, in pN`() {
        val field = curve(400.0, 2.5)
        val blocking = balance.blockingForce(field)
        assert(blocking.isCloseTo(400.0 * exp(-balance.restingHeight / 2.5), 1e-10))
        // and it is exactly the output force of the actuator at zero stroke
        assert(balance.outputForce(field, balance.restingHeight).isCloseTo(blocking, 1e-10))
    }

    @Test
    fun `gate 2 limiting cases - a vanishing electrostatic force should leave the tile at the resting height with no stroke`() {
        val state = balance.solve(curve(1e-9, 2.5))
        assert(state.stroke < 1e-6)
        assert(state.operatingHeight.isCloseTo(balance.restingHeight, 1e-6))
        assert(state.converged)
    }

    @Test
    fun `gate 3 symmetry and conservation - at the operating point the layer load must equal the electrostatic force exactly`() {
        listOf(120.0, 400.0, 900.0).forEach { amplitude ->
            val field = curve(amplitude, 2.5)
            val state = balance.solve(field)
            assert(state.converged)
            val layer = model.load(chain, state.operatingHeight, geometry.footprintArea)
            assert(layer.isCloseTo(abs(state.electrostaticForce), 1e-9))
            // and the actuator's output force is exactly zero at its own free stroke
            assert(abs(balance.outputForce(field, state.operatingHeight)) < 1e-7 * layer)
        }
    }

    @Test
    fun `gate 3 symmetry and conservation - the first equilibrium below the resting height is stable, so k_eff there is positive`() {
        // proved rather than hoped: output force is positive at L0 and negative below the root,
        // so d(output)/dh > 0 there, and d(output)/dh IS k_eff = k_brush - |k_es|
        listOf(50.0, 200.0, 600.0, 1500.0, 4000.0).forEach { amplitude ->
            listOf(1.6, 2.5, 4.0).forEach { decayLength ->
                val state = balance.solve(curve(amplitude, decayLength))
                assert(state.converged)
                assert(state.effectiveStiffness > 0.0)
                assert(state.electrostaticStiffness < 0.0)
                assert(state.brushStiffness > 0.0)
                assert(
                    state.effectiveStiffness.isCloseTo(
                        state.brushStiffness + state.electrostaticStiffness, 1e-9
                    )
                )
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - a stronger field must give a larger stroke and a smaller operating height`() {
        val strokes = listOf(100.0, 300.0, 900.0, 2700.0).map { balance.solve(curve(it, 2.5)) }
        strokes.zipWithNext { weaker, stronger ->
            assert(stronger.stroke > weaker.stroke)
            assert(stronger.operatingHeight < weaker.operatingHeight)
        }
    }

    @Test
    fun `gate 2 limiting cases - dividing the blocking force by a stiffness is NOT the stroke, and the two must differ`() {
        // the failure mode this task exists to avoid. A linear reading F/k over-predicts, because
        // the layer stiffens as it is compressed; the check is that the solve does not reproduce it.
        val field = curve(900.0, 2.5)
        val state = balance.solve(field)
        val linear = balance.blockingForce(field) /
                model.stiffnessPerArea(chain, balance.restingHeight) / geometry.footprintArea
        assert(!linear.isFinite() || linear > 2.0 * state.stroke)
    }

    @Test
    fun `gate 5 cross-check - at zero bias the balance must reproduce C-0003's stroke under a 100 pN dead load`() {
        // C-0003 loads the layer with a CONSTANT 100 pN; the same number must come out of this
        // solver when the electrostatic curve is made constant, which is a genuinely independent
        // route: heightUnderLoad brackets on the pressure, this brackets on the output force.
        val constant = electrostaticForceCurve(DoubleArray(121) { 0.5 + it * 0.1 }) { -100.0 }
        val state = balance.solve(constant)
        val reference = balance.restingHeight -
                model.heightUnderLoad(chain, 100.0, geometry.footprintArea)
        assert(state.stroke.isCloseTo(reference, 1e-8))
        assert(state.stroke > 3.83 && state.stroke < 6.01) // C-0003's bracket at the 10 nm point
    }

    @Test
    fun `gate 5 cross-check - the box model must open with finite stiffness and the strong-stretching one with none`() {
        // C-0001's surprise S-1, carried into C-0003: this is why a stroke needs a force balance
        val box = AlexanderBoxLayer(interaction)
        val boxChain = peg.graftedChain(box.chainLengthForHeight(peg, 10.0, 0.024), 0.024)
        assert(box.stiffnessPerArea(boxChain, box.equilibriumHeight(boxChain)) > 0.0)
        assert(
            model.stiffnessPerArea(chain, balance.restingHeight) <
                    1e-6 * model.stiffnessPerArea(chain, 0.8 * balance.restingHeight)
        )
    }

    @Test
    fun `gate 4 numerical convergence - refining the scan grid must not move the operating height`() {
        val field = curve(900.0, 2.5)
        val coarse = ActuatorForceBalance(model, chain, geometry, scanSteps = 500).solve(field)
        val fine = ActuatorForceBalance(model, chain, geometry, scanSteps = 4000).solve(field)
        val finer = ActuatorForceBalance(model, chain, geometry, scanSteps = 16000).solve(field)
        assert(coarse.operatingHeight.isCloseTo(finer.operatingHeight, 1e-9))
        assert(fine.operatingHeight.isCloseTo(finer.operatingHeight, 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - a stronger interaction must shorten the stroke, and by C-0003's weak power`() {
        // C-0003's exact result: k goes as K^(1/(m+1)) and N as K^(-1/(m+1)), because the chain
        // length a specified height demands moves AGAINST the interaction and nearly cancels it.
        // At 16x in K it moves the 100 pN dead-load stroke from 5.81 nm to 4.38 nm — so a factor
        // of 16 here must shorten the stroke, but by far less than a factor of 16.
        val field = curve(900.0, 2.5)
        val soft = strokeAtInteractionScale(0.25, field)
        val nominal = strokeAtInteractionScale(1.0, field)
        val stiff = strokeAtInteractionScale(4.0, field)
        assert(stiff < nominal && nominal < soft)
        assert(soft / stiff < 2.0)
    }

    private fun strokeAtInteractionScale(
        scale: Double,
        field: ElectrostaticForceCurve
    ): Double {
        val scaled = StrongStretchingLayer(
            desCloizeauxInteraction(peg.crossoverIndex * scale, peg.monomerVolume)
        )
        val scaledChain = peg.graftedChain(scaled.chainLengthForHeight(peg, 10.0, 0.024), 0.024)
        return ActuatorForceBalance(scaled, scaledChain, geometry).solve(field).stroke
    }

    @Test
    fun `gate 3 symmetry and conservation - the output force must peak at a FINITE stroke, not at zero`() {
        // the electrostatic-softening signature in the force-displacement plane: dW/dh = k_eff,
        // so wherever |k_es| approaches k_brush the characteristic is flat and the blocking force
        // UNDERSTATES what the actuator can deliver. Quoting F(0) as "the force" is the error.
        val field = curve(900.0, 2.5)
        val state = balance.solve(field)
        assert(state.peakOutputForce > balance.blockingForce(field))
        assert(state.peakOutputForceStroke > 0.0)
        assert(state.peakOutputForceStroke < state.stroke)
    }

    @Test
    fun `gate 1 dimensional consistency - the output work must be a force times a length and vanish at both ends`() {
        val field = curve(900.0, 2.5)
        val state = balance.solve(field)
        assert(state.maximumOutputWork > 0.0)
        assert(state.maximumOutputWork <= state.peakOutputForce * state.stroke)
        assert(state.workStroke > 0.0 && state.workStroke < state.stroke)
        assert(abs(balance.outputForce(field, state.operatingHeight)) < 1e-9 * state.peakOutputForce)
    }

    @Test
    fun `gate 1 dimensional consistency - a balance must reject a curve that cannot reach the layer`() {
        val tooHigh = electrostaticForceCurve(DoubleArray(20) { 20.0 + it * 0.5 }) { -100.0 }
        assertFailsWith<IllegalArgumentException> { balance.solve(tooHigh) }
    }

}
