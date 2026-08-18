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
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-182` — what prestrain does a row-end crossover actually carry?
 *
 * `C-0104` fixed the **threshold** at 15.4497275° and left the **value** open. Its ladder is a
 * ladder of **per-crossover register offsets** — the phase error of one 8, 16 or 32 bp domain.
 * But every domain's error has the **same sign**, because 16 bp represents 1.5 turns at the square
 * lattice's 33.75°/bp against B-DNA's 34.29°/bp, so it *accumulates* along a duplex and what limits
 * the accumulation is the duplex's own torsion. That is a **boundary-layer** problem with a closed
 * form, and its free-end value is the residual a row-end crossover carries.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class EdgeTwistReliefTest {

    private val hinge = Gen1Tile.crossoverHingeStiffness()
    private val model = EdgeTwistRelief(
        torsionalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
        hingeStiffness = hinge,
        crossoverSpacing = 10.2,
        rowLength = 38.08
    )
    private val mismatch = twistRateMismatch(
        designTwistPerBase = 360.0 / (32.0 / 3.0),
        naturalTwistPerBase = 360.0 / 10.5,
        risePerBase = Gen1Tile.RISE_PER_BASE_PAIR
    )

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 dimensional the decay length is a length and scales as the square root`() {
        assert(model.decayLength.isCloseTo(Math.sqrt(460.0 * 10.2 / hinge)))
        val stiffer = model.copy(hingeStiffness = hinge * 4.0)
        assert(stiffer.decayLength.isCloseTo(model.decayLength / 2.0))
    }

    @Test
    fun `gate 1 dimensional the twist rate mismatch is radians per nm and positive`() {
        // 34.2857 - 33.75 = 0.5357 deg per base, over a 0.34 nm rise
        assert(mismatch.isCloseTo((360.0 / 10.5 - 360.0 / (32.0 / 3.0)) * PI / 180.0 / 0.34))
        assert(mismatch > 0.0)
    }

    @Test
    fun `gate 1 dimensional unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { model.copy(torsionalRigidity = 0.0) }
        assertFailsWith<IllegalArgumentException> { model.copy(hingeStiffness = -1.0) }
        assertFailsWith<IllegalArgumentException> { model.copy(crossoverSpacing = 0.0) }
        assertFailsWith<IllegalArgumentException> { model.copy(rowLength = -1.0) }
        assertFailsWith<IllegalArgumentException> { model.endResidual(Double.NaN) }
        assertFailsWith<IllegalArgumentException> {
            twistRateMismatch(33.75, 34.2857, 0.0)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 limits a vanishing hinge leaves the whole accumulation at the free end`() {
        // k_theta -> 0 is a duplex nothing holds in register: u' = mismatch everywhere.
        val free = model.copy(hingeStiffness = 1e-12)
        assert(free.endResidual(mismatch).isCloseTo(mismatch * model.rowLength / 2.0, 1e-6))
    }

    @Test
    fun `gate 2 limits an infinitely stiff hinge leaves nothing at the free end`() {
        val pinned = model.copy(hingeStiffness = 1e16)
        assert(abs(pinned.endResidual(mismatch)) < 1e-6)
    }

    @Test
    fun `gate 2 limits a torsionally rigid duplex cannot relieve and reaches the rigid limit`() {
        // C -> infinity: lambda -> infinity, u -> mismatch * L/2, the un-relieved accumulation.
        val rigid = model.copy(torsionalRigidity = 1e14)
        assert(rigid.endResidual(mismatch).isCloseTo(mismatch * model.rowLength / 2.0, 1e-6))
    }

    @Test
    fun `gate 2 limits a zero mismatch is a zero residual everywhere`() {
        assert(model.endResidual(0.0) == 0.0)
        assert(model.residualAt(3.7, 0.0) == 0.0)
    }

    @Test
    fun `gate 2 limits the relieved residual never exceeds the rigid limit`() {
        assert(model.endResidual(mismatch) < mismatch * model.rowLength / 2.0)
    }

    // ------------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 symmetry the register error is exactly odd about the row centre`() {
        assert(model.residualAt(0.0, mismatch) == 0.0)
        listOf(1.0, 5.5, 19.04).forEach { x ->
            assert(model.residualAt(-x, mismatch).isCloseTo(-model.residualAt(x, mismatch)))
        }
        assert(model.residualAt(model.rowLength / 2.0, mismatch)
            .isCloseTo(model.endResidual(mismatch)))
    }

    @Test
    fun `gate 3 symmetry the corrugated row-end sign composition is UNIFORM`() {
        // u is odd in x; Rothemund's glide flips the crossover type with the interface parity;
        // a boustrophedon's raster turns alternate ends. The two flips compose to +1 at every
        // interface, which is C-0104's UNIFORM distribution and not its opposed-ends one.
        (0 until 14).forEach { b ->
            val endX = if (b % 2 == 0) model.rowLength / 2.0 else -model.rowLength / 2.0
            assert(
                corrugatedPrestrain(model, mismatch, interfaceIndex = b, x = endX)
                    .isCloseTo(model.endResidual(mismatch))
            )
        }
    }

    @Test
    fun `gate 3 symmetry the corrugated field alternates sign across the interfaces`() {
        val x = 12.0
        assert(
            corrugatedPrestrain(model, mismatch, interfaceIndex = 1, x = x)
                .isCloseTo(-corrugatedPrestrain(model, mismatch, interfaceIndex = 0, x = x))
        )
    }

    // ------------------------------------------------------- gate 4 — numerical convergence

    @Test
    fun `gate 4 convergence the discrete chain converges to the closed form`() {
        val exact = model.endResidual(mismatch)
        val coarse = abs(model.discreteEndResidual(mismatch, 16) - exact)
        val fine = abs(model.discreteEndResidual(mismatch, 64) - exact)
        val finer = abs(model.discreteEndResidual(mismatch, 256) - exact)
        assert(fine < coarse)
        assert(finer < fine)
        assert(finer / exact < 1e-4)
    }

    // ------------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 upstream C-0104's register ladder is the per-domain slope of the same mismatch`() {
        // C-0104's 16 bp rung is the register error of ONE domain; this model's u' is that error
        // per unit length, so the two agree exactly at 16 bp of contour.
        val perDomain = registerPrestrain(16.0, 360.0 / (32.0 / 3.0), 360.0 / 10.5)
        assert((mismatch * 16.0 * Gen1Tile.RISE_PER_BASE_PAIR).isCloseTo(perDomain))
        assert((perDomain * 180.0 / PI).isCloseTo(8.5714286, 1e-7))
    }

    @Test
    fun `gate 5 upstream the rupture ceiling inverts C-0029's two-bond count`() {
        // k_theta = 2 k_bond a^2 on a chord of the phosphate radius, so F = k_theta theta / (2a).
        assert(prestrainAtBondForce(10.0, hinge, 1.0).isCloseTo(2.0 * 1.0 * 10.0 / hinge))
        assert(bondForceAtPrestrain(prestrainAtBondForce(10.0, hinge, 1.0), hinge, 1.0)
            .isCloseTo(10.0))
        assertFailsWith<IllegalArgumentException> { prestrainAtBondForce(10.0, hinge, 0.0) }
    }

    @Test
    fun `gate 5 upstream one unpaired base is one base pair of torsional slack`() {
        assert(unpairedBaseRelief(1.0, 360.0 / 10.5).isCloseTo(360.0 / 10.5 * PI / 180.0))
        assert(unpairedBaseRelief(2.0, 360.0 / 10.5)
            .isCloseTo(2.0 * unpairedBaseRelief(1.0, 360.0 / 10.5)))
    }

    @Test
    fun `gate 5 upstream the derived residual is above C-0104's 15_4497275 degree threshold`() {
        // The finding, asserted: over the whole parameter bracket the boundary layer leaves more
        // at a free duplex end than the threshold C-0104 measured.
        val degrees = model.endResidual(mismatch) * 180.0 / PI
        assert(degrees > 15.4497275)
        assert(degrees < 30.0) // and below the un-relieved rigid limit
    }
}
