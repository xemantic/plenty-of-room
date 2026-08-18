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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-160` gate tests for the **cheap bound** on the collar's dependence on the tile's own
 * half-width — the closed form that runs before the 2-D Poisson-Boltzmann edge solve is spent
 * at `C-0086`'s buildable 38.08 nm footprint.
 *
 * The bound's whole content is that a collar cannot know how wide its tile is *physically* —
 * the two rims of a 38 nm tile are eighteen decay lengths apart — but `fitEdgeTaper` **can**,
 * because it references the profile to the centre-line and truncates both of its moments there.
 * So the entire width-dependence of the taper depth, the taper width and the rim residual is one
 * exponentially small number, `τ(a) = p(a) − Π∞`, and these tests pin that claim in three ways:
 * exactly at the calibration point, exactly at `τ = 0`, and — the one that matters — against
 * `fitEdgeTaper` itself run on a synthetic profile at two different half-widths.
 */
class EdgeWidthDependenceTest {

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - the predicted depth and width are invariant under a rescaling of the load`() {
        val base = referenceModel()
        val scaled = CollarTailModel(
            referenceHalfWidth = base.referenceHalfWidth,
            standoff = base.standoff,
            decayLength = base.decayLength,
            asymptoticLoad = 1000.0 * base.asymptoticLoad,
            centrelineExcess = 1000.0 * base.centrelineExcess,
            loadDeficit = 1000.0 * base.loadDeficit,
            firstMoment = 1000.0 * base.firstMoment,
            totalDeficit = 1000.0 * base.totalDeficit
        )
        val a = base.at(19.04)
        val b = scaled.at(19.04)
        assert(b.taperDepth.isCloseTo(a.taperDepth, 1e-12))
        assert(b.taperWidth.isCloseTo(a.taperWidth, 1e-12))
        assert(b.rimResidualDepth.isCloseTo(a.rimResidualDepth, 1e-12))
        assert(b.loadDeficit.isCloseTo(1000.0 * a.loadDeficit, 1e-12))
        assert(b.totalDeficit.isCloseTo(1000.0 * a.totalDeficit, 1e-12))
    }

    @Test
    fun `gate 1 - unphysical arguments are refused at every entry point`() {
        assertFailsWith<IllegalArgumentException> { referenceModel().at(0.0) }
        assertFailsWith<IllegalArgumentException> { referenceModel().at(0.5) }
        assertFailsWith<IllegalArgumentException> {
            referenceModel().copy(decayLength = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            referenceModel().copy(standoff = -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            fitExponentialTail(doubleArrayOf(1.0, 2.0), doubleArrayOf(1.0), 1.0, 2.0)
        }
        assertFailsWith<IllegalArgumentException> {
            fitExponentialTail(doubleArrayOf(2.0, 1.0), doubleArrayOf(1.0, 1.0), 1.0, 2.0)
        }
        assertFailsWith<IllegalArgumentException> {
            // fewer than three usable samples cannot determine three parameters
            fitExponentialTail(
                doubleArrayOf(1.0, 2.0, 3.0), doubleArrayOf(1.0, 1.0, 1.0), 2.5, 3.0
            )
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - at its own calibration half-width the model returns the reference exactly`() {
        val model = referenceModel()
        val at = model.at(model.referenceHalfWidth)
        assert(at.loadDeficit.isCloseTo(model.loadDeficit, 1e-12))
        assert(at.firstMoment.isCloseTo(model.firstMoment, 1e-12))
        assert(at.totalDeficit.isCloseTo(model.totalDeficit, 1e-12))
        assert(at.interiorLoad.isCloseTo(model.asymptoticLoad + model.centrelineExcess, 1e-12))
    }

    @Test
    fun `gate 2 - a vanishing centre-line excess makes every term exactly width-independent`() {
        val model = referenceModel().copy(centrelineExcess = 0.0)
        val wide = model.at(20.0)
        val narrow = model.at(19.04)
        assert(narrow.loadDeficit.isCloseTo(wide.loadDeficit, 1e-15))
        assert(narrow.taperDepth.isCloseTo(wide.taperDepth, 1e-15))
        assert(narrow.taperWidth.isCloseTo(wide.taperWidth, 1e-15))
        assert(narrow.rimResidual.isCloseTo(wide.rimResidual, 1e-15))
    }

    @Test
    fun `gate 2 - at an infinitely wide tile the deficit is the untruncated one`() {
        val model = referenceModel()
        val far = model.at(400.0)
        assert(far.loadDeficit.isCloseTo(model.untruncatedDeficit, 1e-9))
        assert(far.totalDeficit.isCloseTo(model.untruncatedTotalDeficit, 1e-9))
    }

    // ------------------------------------------- gate 3: symmetry, monotonicity and the fit tie

    @Test
    fun `gate 3 - the model reproduces fitEdgeTaper itself at a second half-width`() {
        // A synthetic profile with the design point's own scales: an interior load, a near-rim
        // enhancement and an exponential tail. The model is calibrated on the fit at a = 20 and
        // must then predict the fit at a = 19.04 — which is the whole cheap bound, tested against
        // the very routine C-0022 uses rather than against its own algebra.
        val asymptote = 0.0390274
        val amplitude = 0.0274430
        val decay = 2.0764
        val standoff = 1.0
        fun load(s: Double) = asymptote + amplitude * exp(-s / decay)

        // Both grids are laid at exactly 0.001 nm, so the standoff falls ON a node at BOTH
        // half-widths. It has to: `fitEdgeTaper` starts its quadrature at the first sample at or
        // beyond the standoff, so a grid that straddles it moves the lower limit of the integral
        // by up to one spacing — worth 3.7e−5 of the deficit at a 0.0005 nm spacing, which is
        // thirty times the movement this model exists to predict. The model is exact about the
        // integral; a grid that samples a different integral is not a test of it.
        fun sampled(halfWidth: Double): Pair<DoubleArray, DoubleArray> {
            val samples = Math.round(halfWidth * 1000.0).toInt() + 1
            val distance = DoubleArray(samples) { halfWidth * it / (samples - 1.0) }
            return distance to DoubleArray(samples) { load(distance[it]) }
        }

        val (wideS, wideP) = sampled(20.0)
        val wideFit = fitEdgeTaper(wideS, wideP, wideP.last(), standoff)
        val (narrowS, narrowP) = sampled(19.04)
        val narrowFit = fitEdgeTaper(narrowS, narrowP, narrowP.last(), standoff)

        val model = CollarTailModel(
            referenceHalfWidth = 20.0,
            standoff = standoff,
            decayLength = decay,
            asymptoticLoad = asymptote,
            centrelineExcess = load(20.0) - asymptote,
            loadDeficit = wideFit.loadDeficit,
            firstMoment = wideFit.firstMoment,
            totalDeficit = -amplitude * decay * (1.0 - exp(-20.0 / decay)) +
                    (load(20.0) - asymptote) * 20.0
        )
        val predicted = model.at(19.04)

        assert(predicted.loadDeficit.isCloseTo(narrowFit.loadDeficit, 1e-6))
        assert(predicted.firstMoment.isCloseTo(narrowFit.firstMoment, 1e-6))
        assert(predicted.taperWidth.isCloseTo(narrowFit.equivalentWidth, 1e-6))
        assert(predicted.taperDepth.isCloseTo(narrowFit.depth, 1e-6))
        // And the movement itself, which is what the bound is for, is small and signed:
        // the narrower tile fits a SHALLOWER-CENTROID, hence NARROWER, collar.
        assert(narrowFit.equivalentWidth < wideFit.equivalentWidth)
        assert(abs(narrowFit.equivalentWidth / wideFit.equivalentWidth - 1.0) < 0.01)
    }

    @Test
    fun `gate 3 - the deficit is monotone in the half-width above the standoff`() {
        val model = referenceModel()
        var previous = Double.NEGATIVE_INFINITY
        var width = 2.0
        while (width <= 40.0) {
            val magnitude = abs(model.at(width).loadDeficit)
            assert(magnitude > previous)
            previous = magnitude
            width += 0.5
        }
    }

    // ------------------------------------------------- gate 3b: the standoff is not a mesh node

    @Test
    fun `gate 3b - an exact standoff makes the fit independent of where the mesh nodes fall`() {
        val asymptote = 0.0390274
        val amplitude = 0.0274430
        val decay = 2.0764
        val standoff = 1.0
        val halfWidth = 20.0
        fun load(s: Double) = asymptote + amplitude * exp(-s / decay)

        // Two meshes over the SAME domain and the same profile, differing only in whether the
        // standoff lands on a node. `fitEdgeTaper` starts at the first node at or beyond it, so
        // the raw fits integrate two different intervals; the exact-standoff fit integrates one.
        fun mesh(intervals: Int): Pair<DoubleArray, DoubleArray> {
            val distance = DoubleArray(intervals + 1) { halfWidth * it / intervals }
            return distance to DoubleArray(intervals + 1) { load(distance[it]) }
        }

        val (aligned, alignedLoad) = mesh(2000)        // spacing 0.01 nm — the standoff is a node
        val (offset, offsetLoad) = mesh(1997)          // spacing 0.010015… — it is not
        val rawAligned = fitEdgeTaper(aligned, alignedLoad, alignedLoad.last(), standoff)
        val rawOffset = fitEdgeTaper(offset, offsetLoad, offsetLoad.last(), standoff)
        val exactAligned = taperFitAtExactStandoff(
            aligned, alignedLoad, alignedLoad.last(), standoff
        )
        val exactOffset = taperFitAtExactStandoff(offset, offsetLoad, offsetLoad.last(), standoff)

        // The raw pair disagree at the level this whole task is trying to measure ...
        val rawDeparture = abs(rawOffset.depth / rawAligned.depth - 1.0)
        assert(rawDeparture > 1e-5)
        // ... and the exact-standoff pair agree two decades better.
        val exactDeparture = abs(exactOffset.depth / exactAligned.depth - 1.0)
        assert(exactDeparture < 0.01 * rawDeparture)
    }

    @Test
    fun `gate 3b - where the standoff IS a node the exact fit is the ordinary one`() {
        val samples = 2001
        val distance = DoubleArray(samples) { 20.0 * it / (samples - 1.0) }
        val load = DoubleArray(samples) { 0.039 + 0.027 * exp(-distance[it] / 2.0764) }
        val raw = fitEdgeTaper(distance, load, load.last(), 1.0)
        val exact = taperFitAtExactStandoff(distance, load, load.last(), 1.0)
        assert(exact.depth.isCloseTo(raw.depth, 1e-14))
        assert(exact.equivalentWidth.isCloseTo(raw.equivalentWidth, 1e-14))
        assert(exact.loadDeficit.isCloseTo(raw.loadDeficit, 1e-14))
    }

    @Test
    fun `gate 3b - the exact-standoff fit refuses a standoff outside its own samples`() {
        val distance = doubleArrayOf(0.0, 1.0, 2.0, 3.0)
        val load = doubleArrayOf(1.0, 1.0, 1.0, 1.0)
        assertFailsWith<IllegalArgumentException> {
            taperFitAtExactStandoff(distance, load, 1.0, 3.0)
        }
        assertFailsWith<IllegalArgumentException> {
            taperFitAtExactStandoff(distance, load, 1.0, -1.0)
        }
    }

    // ------------------------------------------------------------------ gate 4: the tail fit

    @Test
    fun `gate 4 - the tail fit recovers a synthetic exponential to the last digits`() {
        val asymptote = 0.039
        val amplitude = 0.027
        val decay = 2.0764
        val samples = 601
        val distance = DoubleArray(samples) { 6.0 + 12.0 * it / (samples - 1.0) }
        val load = DoubleArray(samples) { asymptote + amplitude * exp(-distance[it] / decay) }
        val fit = fitExponentialTail(distance, load, 6.0, 18.0)
        assert(fit.decayLength.isCloseTo(decay, 1e-6))
        assert(fit.asymptoticLoad.isCloseTo(asymptote, 1e-6))
        assert(fit.amplitude.isCloseTo(amplitude, 1e-6))
        assert(fit.relativeResidual < 1e-9)
    }

    @Test
    fun `gate 4 - the tail fit uses only the samples inside its own window`() {
        val asymptote = 0.039
        val amplitude = 0.027
        val decay = 2.0764
        val samples = 801
        val distance = DoubleArray(samples) { 0.5 + 19.5 * it / (samples - 1.0) }
        // Outside the window the profile is deliberately nothing like an exponential tail.
        val load = DoubleArray(samples) {
            if (distance[it] < 6.0) -3.0 * distance[it]
            else asymptote + amplitude * exp(-distance[it] / decay)
        }
        val fit = fitExponentialTail(distance, load, 6.0, 18.0)
        assert(fit.decayLength.isCloseTo(decay, 1e-5))
        assert(fit.asymptoticLoad.isCloseTo(asymptote, 1e-5))
    }

    // ------------------------------------------------------------------ gate 5: the departure

    @Test
    fun `gate 5 - a collar departure is relative and signed and is zero against itself`() {
        val a = referenceModel().at(20.0)
        val b = referenceModel().at(19.04)
        assert(collarDeparture(a, a) == 0.0)
        assert(collarDeparture(b, a) > 0.0)
        // the largest of the three relative movements, which is what a verdict is taken on
        val expected = maxOf(
            abs(b.taperDepth / a.taperDepth - 1.0),
            abs(b.taperWidth / a.taperWidth - 1.0),
            abs(b.rimResidualDepth / a.rimResidualDepth - 1.0)
        )
        assert(collarDeparture(b, a).isCloseTo(expected, 1e-12))
    }

    /**
     * `C-0022`'s own design point, at 300 K in aqueous 2 mM MgCl₂, a 10 nm gap and `C-0012`'s
     * located 0.192 V — read as literals here rather than from the result file, because a test
     * that reads its own fixture from a file the study writes is not a test.
     */
    private fun referenceModel() = CollarTailModel(
        referenceHalfWidth = 20.0,
        standoff = DEFAULT_RIM_STANDOFF,
        decayLength = 2.0764,
        asymptoticLoad = 0.0390274,
        centrelineExcess = 1.7973e-6,
        loadDeficit = -0.0528408705,
        firstMoment = -0.0528408705 * 2.65822321,
        totalDeficit = -0.0644310883
    )
}
