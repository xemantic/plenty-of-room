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
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannGap
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.diffusePotentialOfAppliedBias
import com.xemantic.nano.plentyofroom.electrostatics.sternChargeDensityPerVolt
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.electrostatics.uniformMedium
import kotlin.test.Test

/**
 * `CH-0011`, as an executable check rather than as prose.
 *
 * The challenge says `C-0008`'s *"`k_es < 0` everywhere"* is a universal drawn from a sample
 * whose smallest gap is 3 nm, that `|F_es|` is **not monotone** below that, and that past its
 * maximum the electrostatics **stiffens** the layer instead of softening it — so that what
 * arrests the collapse after pull-in is an *electrostatic stopper* and not §1's osmotic
 * divergence. `T-3` reported those numbers; nothing asserted them.
 *
 * These four tests assert them, on the real Poisson-Boltzmann solver at `C-0008`'s own tile
 * charge and buffer, with **every sign quoted at the gap it applies to**.
 */
class ElectrostaticStopperTest {

    private val bjerrum = bjerrumLength()

    private val tile = DnaOrigamiTile()

    private val charge = -(tile.projectedChargeDensity * tile.manningSurvivingFraction(2, bjerrum) / 2.0)

    private val ions = IonModel(MagnesiumChlorideBuffer(2.0).magnesiumNumberDensity)

    private val medium = uniformMedium(GapMedium())

    private val stern = sternChargeDensityPerVolt(20.0)

    /** The **signed** force in pN over the 40 x 40 nm footprint, at a fixed **applied** bias. */
    private fun force(gap: Double, bias: Double): Double {
        val diffuse = diffusePotentialOfAppliedBias(
            gap, bias, charge, stern, ions, medium, bjerrum, nodes = 2000
        )
        return PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = 2000)
            .solve(diffuse / thermalVoltage(), charge)
            .forceOnTile(1600.0)
    }

    /** `k_es = −∂F_z/∂h`, centrally differenced through the full re-solve — `C-0008`'s method. */
    private fun electrostaticStiffness(gap: Double, bias: Double, delta: Double = 1e-3): Double =
        -(force(gap + delta, bias) - force(gap - delta, bias)) / (2.0 * delta)

    @Test
    fun `gate 3 symmetry - k_es is negative at the working gap, which is what C-0008 sampled`() {
        listOf(5.0, 7.0, 10.0).forEach { gap ->
            assert(electrostaticStiffness(gap, 0.25) < 0.0)
        }
    }

    @Test
    fun `gate 2 limiting cases - the attraction is NOT monotone, it peaks and the peak is below 3 nm`() {
        val peak = forceMaximumGap(0.35, 6.0, coarseSteps = 24, tolerance = 1e-4) {
            -force(it, 0.05)
        }
        assert(peak != null)
        // C-0008's sweep starts at 3 nm, and this is why "everywhere" was a universal drawn from
        // a bounded sample: the turning point is inside the region it never looked at
        assert(peak!! < 3.0)
        assert(peak > 0.35)
    }

    @Test
    fun `gate 3 symmetry - k_es changes sign at the force maximum, softening above it and stiffening below`() {
        val bias = 0.05
        val peak = forceMaximumGap(0.35, 6.0, coarseSteps = 24, tolerance = 1e-4) { -force(it, bias) }
        assert(peak != null)
        // above the maximum the force decays with the gap, so k_es < 0 — §1's softening
        assert(electrostaticStiffness(peak!! + 0.3, bias) < 0.0)
        // below it the force falls as the gap closes, so k_es > 0 — the electrostatics STIFFENS
        assert(electrostaticStiffness(peak - 0.1, bias) > 0.0)
    }

    @Test
    fun `gate 5 upstream - the force turns outright repulsive below a gap C-0012 puts at 0_55 to 1_58 nm`() {
        // 0.02 V, 2 mM: C-0012 reports the sign change at 1.107 nm
        val onset = repulsionOnsetGap(0.35, 6.0, coarseSteps = 24, tolerance = 1e-6) { force(it, 0.02) }
        assert(onset != null)
        assert(onset!! > 0.55 && onset < 1.58)
        // and it IS a sign change: repulsive below, attractive above
        assert(force(onset - 0.05, 0.02) > 0.0)
        assert(force(onset + 0.05, 0.02) < 0.0)
    }
}
