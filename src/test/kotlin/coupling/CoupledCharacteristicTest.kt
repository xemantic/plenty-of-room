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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import java.io.File
import kotlin.math.abs
import kotlin.test.Test

/**
 * `T-16` gate 5 — the upstream cross-check, and the one that decides whether this task is
 * entitled to disagree with `C-0012` about the *scope* of its coupling table.
 *
 * The rule is `gpd/README.md`'s: a number that decides a verdict is read from the **emitting
 * study's own result file**, never re-typed out of a claim's prose. Everything here comes
 * from `gpd/results/T-3-stroke-and-blocking-force.json`.
 */
class CoupledCharacteristicTest {

    private val records = readCoupledOperatingPoints(
        File("gpd/results/T-3-stroke-and-blocking-force.json")
    ).filter { it.medium == "free bulk buffer" && it.concentration == 2.0 }

    private val thresholds = readCoupledThresholds(
        File("gpd/results/T-3-stroke-and-blocking-force.json")
    ).filter { it.concentration == 2.0 }

    private fun at(height: Double, bias: Double) =
        records.filter { it.layerHeight == height && it.appliedBias == bias }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 cross-check - C-0012's own simultaneous-target bias is an interpolation across its 0-10 to 0-25 V gap`() {
        // The bias the coupling requirement has to be read at is C-0012's own
        // `biasForSimultaneousTarget`. At 10 nm every one of the six models puts it strictly
        // inside (0.10, 0.25) — the interval its grid does not sample — and C-0012 obtains it
        // by interpolating ACROSS that interval rather than by locating a root in it.
        val ten = thresholds.filter { it.layerHeight == 10.0 }
        assert(ten.size == 6)
        ten.forEach {
            val bias = it.biasForSimultaneousTarget!!
            assert(bias > 0.10)
            assert(bias < 0.25)
            assert(it.biasBracketForSimultaneousTarget == "[0.1, 0.25]")
        }
        // at 7 nm, exactly ONE of the six falls below 0.10 V — not two
        val seven = thresholds.filter { it.layerHeight == 7.0 }
        assert(seven.count { it.biasForSimultaneousTarget!! < 0.10 } == 1)
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 cross-check - C-0012's blocking forces at 2 mM are reproduced from its own file`() {
        assert(at(5.0, 0.10).first().blockingForce.isCloseTo(167.2, 1e-3))
        assert(at(7.0, 0.10).first().blockingForce.isCloseTo(86.7, 1e-3))
        assert(at(10.0, 0.10).first().blockingForce.isCloseTo(34.5, 2e-3))
        assert(at(5.0, 0.25).first().blockingForce.isCloseTo(490.4, 1e-3))
        assert(at(7.0, 0.25).first().blockingForce.isCloseTo(214.7, 1e-3))
        assert(at(10.0, 0.25).first().blockingForce.isCloseTo(73.6, 1e-3))
    }

    @Test
    fun `gate 5 cross-check - C-0012's coupling table is reproduced as the stability floor`() {
        // "5.3 - 16.0 pN/nm at 10 nm / 0.10 V"
        val ten = at(10.0, 0.10).mapNotNull { it.stabilityFloor }.filter { it > 0.0 }
        assert(ten.min().isCloseTo(5.31, 1e-2))
        assert(ten.max().isCloseTo(15.99, 1e-2))
        // "47.6 - 71.5 pN/nm at 10 nm / 0.25 V"
        val tenHigh = at(10.0, 0.25).mapNotNull { it.stabilityFloor }.filter { it > 0.0 }
        assert(tenHigh.min().isCloseTo(47.63, 1e-2))
        assert(tenHigh.max().isCloseTo(71.54, 1e-2))
        // "85.6 - 276.6 pN/nm at 7 nm / 0.25 V"
        val seven = at(7.0, 0.25).mapNotNull { it.stabilityFloor }.filter { it > 0.0 }
        assert(seven.min().isCloseTo(85.57, 1e-2))
        assert(seven.max().isCloseTo(276.58, 1e-2))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - neither bias C-0012's coupling table is quoted at is an operating bias`() {
        // §3 asks for 100 pN AT a 3 nm stroke. The bias that delivers exactly that is the one
        // the coupling requirement has to be read at, and C-0012's grid does not sample it:
        // at 7 and 10 nm, at both 0.10 V and 0.25 V and under every one of the six layer
        // models, W(3 nm) misses 100 pN by more than 10 %.
        listOf(7.0, 10.0).forEach { height ->
            listOf(0.10, 0.25).forEach { bias ->
                at(height, bias).forEach {
                    val delivered = it.outputForceAtThreeNanometres!!
                    assert(abs(delivered / 100.0 - 1.0) > 0.10)
                }
            }
        }
        // and at 10 nm the crossing is bracketed strictly between the two, under all six
        at(10.0, 0.10).forEach { assert(it.outputForceAtThreeNanometres!! < 100.0) }
        at(10.0, 0.25).forEach { assert(it.outputForceAtThreeNanometres!! > 100.0) }
    }

    @Test
    fun `gate 3 symmetry - the unpreloaded coupling window is empty at four of six models at 10 nm and 0-25 V`() {
        // The chord W(3)/3 against the tangent |k_eff(3)|: where the chord is flatter, no
        // unpreloaded linear coupling is simultaneously placed at 3 nm and stable there.
        val empty = at(10.0, 0.25).count { record ->
            val window = CouplingWindow(
                targetStroke = 3.0,
                outputForceAtTarget = record.outputForceAtThreeNanometres!!,
                effectiveStiffnessAtTarget = record.loadedEffectiveStiffness!!,
                mandatedStiffness = mandatedCouplingStiffness(100.0, 3.0)
            )
            window.unpreloadedWindowIsEmpty
        }
        assert(empty == 4)
        // and this is an over-driven state, not an operating point: every one of those six
        // delivers 1.5x to 2.0x §3's own force target at the 3 nm stroke
        at(10.0, 0.25).forEach {
            assert(it.outputForceAtThreeNanometres!! > 1.4 * 100.0)
        }
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - the stability floor falls to zero at 5 nm where C-0012 reports the point stable`() {
        // C-0012: "PASS at 5 nm" — k_eff is positive there at 0.10 V under every model
        assert(at(5.0, 0.10).all { it.loadedEffectiveStiffness!! > 0.0 })
        assert(at(5.0, 0.10).all { it.stabilityFloor == 0.0 })
    }

    @Test
    fun `gate 2 limiting cases - k_eff is the sum of its two parts at every record in the file`() {
        records.forEach { record ->
            val brush = record.loadedBrushStiffness
            val field = record.loadedElectrostaticStiffness
            val effective = record.loadedEffectiveStiffness
            if (field != null && effective != null) {
                assert(abs(effective - (brush + field)) <= 1e-6 * abs(effective).coerceAtLeast(1.0))
            }
        }
    }
}
