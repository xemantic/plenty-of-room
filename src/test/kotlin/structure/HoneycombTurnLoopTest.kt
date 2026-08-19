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
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-230` / `T-231` — the honeycomb raster turn's unpaired slack, and what a ragged face costs.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * Both tasks are closed forms over exact integer lattices; there is no mesh, so gate 4 is
 * discharged as **exactness over whole families** plus the convergence of the one iterative
 * routine here, the inverse Langevin.
 */
class HoneycombTurnLoopTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    private val step = MeasuredBackbone.STEP_SOUTH
    private val kT = thermalEnergy(ROOM_TEMPERATURE)
    private val turns = honeycombRasterTurns(honeycombXRasterPath(rows = 15, helicesPerRow = 4))

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 dimensional a span is a length and its extremes are the line of centres`() {
        assert(turnPhosphateSpan(d, rP, 0.0, 180.0).isCloseTo(d - 2.0 * rP))
        assert(turnPhosphateSpan(d, rP, 180.0, 0.0).isCloseTo(d + 2.0 * rP))
        assert(minimumTurnPhosphateSpan(d, rP).isCloseTo(d - 2.0 * rP))
        assert(maximumTurnPhosphateSpan(d, rP).isCloseTo(d + 2.0 * rP))
    }

    @Test
    fun `gate 1 dimensional an axial offset enters the span in quadrature`() {
        val flat = turnPhosphateSpan(d, rP, 0.0, 180.0)
        val offset = turnPhosphateSpan(d, rP, 0.0, 180.0, axialOffset = 1.0)
        assert(offset.isCloseTo(kotlin.math.sqrt(flat * flat + 1.0)))
    }

    @Test
    fun `gate 1 dimensional the guards refuse a non-physical geometry`() {
        assertFailsWith<IllegalArgumentException> { turnPhosphateSpan(0.0, rP, 0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { turnPhosphateSpan(d, -0.1, 0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { maximumBackboneSpan(-1, step) }
        assertFailsWith<IllegalArgumentException> { maximumBackboneSpan(3, 0.0) }
        assertFailsWith<IllegalArgumentException> { minimumUnpairedNucleotides(-1.0, step) }
        assertFailsWith<IllegalArgumentException> { maximumUniformRowLength(7249, 0, 28) }
        assertFailsWith<IllegalArgumentException> { maximumUniformRowLength(7249, 60, -1) }
    }

    // ------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting the zero-slack crossover span is inside the MEASURED step`() {
        // THE CHEAP BOUND, and it is a check on the geometry rather than on the design: a
        // scaffold crossover is n = 0, so its span must be reachable by ONE phosphodiester step.
        val span = minimumTurnPhosphateSpan(d, rP)
        assert(span < MeasuredBackbone.STEP_SOUTH_P99)
        assert(span > MeasuredBackbone.STEP_SOUTH_P1)
        assert(minimumUnpairedNucleotides(span, MeasuredBackbone.STEP_SOUTH_P99) == 0)
        val sigma = (span - MeasuredBackbone.STEP_SOUTH) / MeasuredBackbone.STEP_SOUTH_SD
        assert(abs(sigma) < 3.0)
    }

    @Test
    fun `gate 2 limiting n unpaired nucleotides make n plus one phosphodiester steps`() {
        assert(maximumBackboneSpan(0, step).isCloseTo(step))
        assert(maximumBackboneSpan(1, step).isCloseTo(2.0 * step))
        assert(maximumBackboneSpan(27, step).isCloseTo(28.0 * step))
    }

    @Test
    fun `gate 2 limiting the reach bound is the exact inverse of the reach`() {
        (0..40).forEach { n ->
            val span = maximumBackboneSpan(n, step)
            assert(minimumUnpairedNucleotides(span, step) == n)
            assert(minimumUnpairedNucleotides(span - 1e-9, step) == n)
            assert(minimumUnpairedNucleotides(span + 1e-9, step) == n + 1)
        }
    }

    @Test
    fun `gate 2 limiting the Langevin function has both of its limits`() {
        assert(langevin(0.0).isCloseTo(0.0))
        assert(langevin(1e-8).isCloseTo(1e-8 / 3.0))
        assert(langevin(1e-3).isCloseTo(1e-3 / 3.0, 1e-6))
        assert(langevin(1e6) > 0.99999)
        assert(langevin(1e6) < 1.0)
        // the trap CLAUDE.md records three times: cosh/sinh overflow above u ~ 20
        assert(langevin(700.0).isFinite())
        assert(langevin(1e12).isFinite())
    }

    @Test
    fun `gate 2 limiting the FJC tension vanishes at zero extension and diverges at contour`() {
        val loose = turnLoopState(d, 200, 2.1, 0.65, kT)
        assert(loose.tension < 0.2)
        val taut = turnLoopState(d, 5, 2.1, 0.65, kT)
        assert(taut.tension > 5.0)
        assert(taut.extensionRatio > 0.7)
        assertFailsWith<IllegalArgumentException> { turnLoopState(d, 3, 2.1, 0.65, kT) }
    }

    // ------------------------------------------------- gate 3 — symmetry and reproduction

    @Test
    fun `gate 3 symmetry the inverse Langevin inverts the Langevin`() {
        listOf(1e-6, 0.01, 0.1, 0.3, 0.5, 0.7, 0.9, 0.99, 0.999).forEach { x ->
            val u = inverseLangevin(x)
            assert(langevin(u).isCloseTo(x, 1e-9))
        }
        assert(inverseLangevin(0.0).isCloseTo(0.0))
    }

    @Test
    fun `gate 3 symmetry the FJC tension reduces to the Gaussian spring at small extension`() {
        val b = 2.1
        val c = 0.65
        val n = 4000
        val state = turnLoopState(d, n, b, c, kT)
        val gaussian = 3.0 * kT * d / (b * n * c)
        assert(state.tension.isCloseTo(gaussian, 1e-3))
        val gaussianEnergy = 3.0 * kT * d * d / (2.0 * b * n * c)
        assert(state.freeEnergy.isCloseTo(gaussianEnergy, 1e-3))
    }

    @Test
    fun `gate 3 symmetry the FJC free energy is the integral of its own tension`() {
        val b = 2.84
        val c = 0.70
        val n = 12
        val state = turnLoopState(d, n, b, c, kT)
        val steps = 20000
        var integral = 0.0
        (0 until steps).forEach { i ->
            val r = d * (i + 0.5) / steps
            integral += turnLoopTension(r, n * c, b, kT) * d / steps
        }
        assert(state.freeEnergy.isCloseTo(integral, 1e-4))
    }

    @Test
    fun `gate 3 reproduction the built allowance reproduces C-0140's three ceilings`() {
        assert(maximumUniformRowLength(7249, 60, 28) == 92)
        assert(maximumUniformRowLength(7560, 60, 28) == 98)
        assert(maximumUniformRowLength(8064, 60, 28) == 106)
        // and the identity that identifies the scaffold: 60 x (98 + 28) = 7560, exactly
        assert(60 * (98 + 28) == 7560)
    }

    @Test
    fun `gate 3 reproduction the two-length raster reproduces C-0140's faces and extent`() {
        val ragged = twoLengthRaster(turns, senseOneRowLength = 112, senseTwoRowLength = 108)
        assert(ragged.frontSpreadBasePairs == 4)
        assert(ragged.rearSpreadBasePairs == 8)
        assert(ragged.axialExtentBasePairs == 116)
        assert(ragged.scaffoldNucleotides == 6596)
        val tight = twoLengthRaster(turns, senseOneRowLength = 112, senseTwoRowLength = 109)
        assert(tight.frontSpreadBasePairs == 3)
        assert(tight.rearSpreadBasePairs == 6)
        assert(tight.axialExtentBasePairs == 115)
    }

    @Test
    fun `gate 3 symmetry a uniform row length leaves both faces flat`() {
        val flat = twoLengthRaster(turns, senseOneRowLength = 112, senseTwoRowLength = 112)
        assert(flat.frontSpreadBasePairs == 0)
        assert(flat.rearSpreadBasePairs == 0)
        assert(flat.axialExtentBasePairs == 112)
        assert(flat.scaffoldNucleotides == 60 * 112)
    }

    @Test
    fun `gate 3 symmetry the front face raggedness IS the stagger`() {
        listOf(3, 4, 6, 7, 8).forEach { stagger ->
            val ragged = twoLengthRaster(turns, 112, 112 - stagger)
            assert(ragged.frontSpreadBasePairs == stagger)
            assert(ragged.rearSpreadBasePairs == 2 * stagger)
        }
    }

    @Test
    fun `gate 3 symmetry every helix carries one of exactly two lengths`() {
        val ragged = twoLengthRaster(turns, 112, 108)
        assert(ragged.helixRowLength.size == 58)
        assert(ragged.helixRowLength.values.toSet() == setOf(112, 108))
        assert(ragged.crossoverLevels.size == 59)
    }

    // ------------------------------------------------------- gate 4 — exactness and periods

    @Test
    fun `gate 4 exactness the period of a constant sequence is one and of an alternation two`() {
        assert(sequencePeriod(listOf(5, 5, 5, 5)) == 1)
        assert(sequencePeriod(listOf(1, 2, 1, 2, 1, 2)) == 2)
        assert(sequencePeriod(listOf(1, 2, 3, 1, 2, 3)) == 3)
        assert(sequencePeriod(listOf(1, 2, 3, 4)) == 4)
        assertFailsWith<IllegalArgumentException> { sequencePeriod(emptyList()) }
    }

    @Test
    fun `gate 4 exactness the gap-facing rim alternates row to row`() {
        val ragged = twoLengthRaster(turns, 112, 108)
        val rim = gapFacingRimLevels(turns, ragged, column = 0)
        assert(sequencePeriod(rim.map { it.second }) == 2)
        assert(rim.size > 10)
        val spread = rim.maxOf { it.second } - rim.minOf { it.second }
        assert(spread == 4)
    }

    @Test
    fun `gate 4 exactness the reach bound is monotone in the span and in the step`() {
        var previous = -1
        listOf(0.5, 1.0, 2.0, 2.536, 3.0, 4.353).forEach { span ->
            val n = minimumUnpairedNucleotides(span, step)
            assert(n >= previous)
            previous = n
        }
        assert(minimumUnpairedNucleotides(4.353, MeasuredBackbone.STEP_NORTH) >=
                minimumUnpairedNucleotides(4.353, MeasuredBackbone.STEP_SOUTH))
    }

    @Test
    fun `gate 4 convergence the inverse Langevin settles to machine precision`() {
        listOf(0.05, 0.5, 0.95).forEach { x ->
            val coarse = inverseLangevin(x, iterations = 40)
            val fine = inverseLangevin(x, iterations = 200)
            assert(abs(langevin(fine) - x) < 1e-12)
            assert(abs(langevin(coarse) - x) < 1e-6)
        }
    }

    // ------------------------------------------------------- gate 5 — literature and corpus

    @Test
    fun `gate 4 exactness the 10 x 6 raster carries the SAME 4 and 8 base pair faces`() {
        val other = honeycombRasterTurns(honeycombXRasterPath(rows = 10, helicesPerRow = 6))
        val ragged = twoLengthRaster(other, 112, 108)
        assert(ragged.frontSpreadBasePairs == 4)
        assert(ragged.rearSpreadBasePairs == 8)
        assert(sequencePeriod(gapFacingRimLevels(other, ragged, 0).map { it.second }) == 2)
    }

    @Test
    fun `gate 1 dimensional a square wave enters a sinusoidal transfer by its fundamental`() {
        assert(squareWaveFundamentalAmplitude(0.0).isCloseTo(0.0))
        assert(squareWaveFundamentalAmplitude(kotlin.math.PI).isCloseTo(2.0))
        assertFailsWith<IllegalArgumentException> { squareWaveFundamentalAmplitude(-1.0) }
    }

    @Test
    fun `gate 1 dimensional a departure degrades to absolute at a zero reference`() {
        assert(relativeDeparture(4.0, 4.0).isCloseTo(0.0))
        assert(relativeDeparture(5.0, 4.0).isCloseTo(0.25))
        assert(relativeDeparture(3.0, 0.0).isCloseTo(3.0))
    }

    @Test
    fun `gate 5 literature the ripple transfer and the slit decay are the corpus's own`() {
        // T-231 leans on two closed forms already in the tree; assert they are being called,
        // not re-implemented, by checking their own limiting behaviour here.
        assert(loadRippleTransmission(5.71, 7.608) < 0.01)
        assert(loadRippleTransmission(5.71, 400.0) > 0.99)
    }

    @Test
    fun `gate 5 literature a four base pair relief clears the blunt-end stacking range`() {
        val rise = Gen1Tile.RISE_PER_BASE_PAIR
        assert(4 * rise > 1.30)
        assert(3 * rise < 1.30)
        // and the margin the clearing pair carries is BELOW the design language's own quantum
        assert(4 * rise - 1.30 < rise)
    }
}
