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

package com.xemantic.nano.plentyofroom.quantities

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The dominant error class in this corpus, made unrepresentable.
 *
 * `CLAUDE.md` records eleven instances of one mistake under one name — *"a quantity is not well
 * posed without the state it is read at"*: a stiffness quoted without its compression, a variance
 * without its bandwidth, a rupture force without its bonded length and loading rate, a margin
 * without its load line, a flatness without its operating state, a placement ceiling without its
 * occupancy. Every one of them was caught by a person reading prose, and each cost an iteration.
 *
 * It is a type-system problem being solved by discipline. These tests pin the three things a type
 * can do that prose cannot:
 *
 *  1. **a quantity cannot be built without its state** — the state is a constructor parameter, so
 *     the omission is a compile error rather than a review finding;
 *  2. **a quantity cannot be QUOTED without its state** — [StatedQuantity.quote] renders both;
 *  3. **two quantities read at DIFFERENT states cannot be compared** — which is the half that
 *     actually moved answers here, because `CLAUDE.md`'s *"a buffer advantage is a quantity that
 *     needs the state it is read at"* is a ratio taken across two states, and it was overstated
 *     3.16–3.35× exactly that way.
 */
class StatedQuantityTest {

    // --- gate 1: a quantity carries its state, and renders it -----------------------------------

    @Test
    fun `a stiffness quotes the compression it was read at`() {
        val stiffness = LayerStiffness(
            pnPerNm = 12.5,
            atCompressionNm = 3.0,
            model = "scf-absorbing"
        )
        val quoted = stiffness.quote()
        assert("12.5" in quoted)
        assert("pN/nm" in quoted)
        assert("compression" in quoted)
        assert("3" in quoted)
        assert("scf-absorbing" in quoted)
    }

    @Test
    fun `every stated quantity names at least one state key`() {
        val quantities = listOf(
            LayerStiffness(12.5, atCompressionNm = 3.0, model = "scf-absorbing"),
            FluctuationAmplitude(0.42, bandwidthHz = 1e3, cornerHz = 3e5),
            RuptureForce(34.8, bondedBasePairs = 16, loadingRatePnPerSecond = 100.0),
            ElectrostaticStiffness(-8.4, gapNm = 7.0, biasVolts = 0.192)
        )
        quantities.forEach { quantity ->
            assert(quantity.state.isNotEmpty())
            assert(quantity.unit.isNotEmpty())
            quantity.state.forEach { (key, value) ->
                assert(key in quantity.quote())
                assert(value in quantity.quote())
            }
        }
    }

    // --- gate 2: a comparison is itself a quantity, and needs one state ---------------------------

    @Test
    fun `a ratio of two quantities at the same state is a plain number`() {
        val soft = LayerStiffness(4.0, atCompressionNm = 3.0, model = "scf-absorbing")
        val stiff = LayerStiffness(12.0, atCompressionNm = 3.0, model = "scf-absorbing")
        assert(ratioOf(stiff, soft).isCloseTo(3.0))
    }

    @Test
    fun `a ratio across two DIFFERENT states is refused, and the message names the key`() {
        val atRest = LayerStiffness(4.0, atCompressionNm = 0.0, model = "scf-absorbing")
        val compressed = LayerStiffness(12.0, atCompressionNm = 3.0, model = "scf-absorbing")
        val failure = assertFailsWith<IllegalArgumentException> { ratioOf(compressed, atRest) }
        assert("compressionNm" in failure.message!!)
        assert("0" in failure.message!!)
        assert("3" in failure.message!!)
    }

    @Test
    fun `a difference across two different states is refused the same way`() {
        val twoMillimolar = ElectrostaticStiffness(-8.4, gapNm = 7.0, biasVolts = 0.192)
        val tenMillimolar = ElectrostaticStiffness(-5.1, gapNm = 10.0, biasVolts = 0.192)
        assertFailsWith<IllegalArgumentException> { differenceOf(tenMillimolar, twoMillimolar) }
    }

    @Test
    fun `two quantities of different KINDS never compare, even at the same state`() {
        val stiffness = LayerStiffness(4.0, atCompressionNm = 3.0, model = "scf-absorbing")
        val electrostatic = ElectrostaticStiffness(4.0, gapNm = 3.0, biasVolts = 0.0)
        val failure = assertFailsWith<IllegalArgumentException> {
            ratioOf(stiffness, electrostatic)
        }
        assert("kind" in failure.message!!)
    }

    // --- gate 3: the bandwidth law, which is the state a variance is quoted at --------------------

    @Test
    fun `the fraction of an overdamped variance below the corner frequency is one half`() {
        val fluctuation = FluctuationAmplitude(1.0, bandwidthHz = 3e5, cornerHz = 3e5)
        assert(fluctuation.fractionOfVarianceInBand().isCloseTo(0.5))
    }

    @Test
    fun `a broadband amplitude is the infinite-bandwidth limit`() {
        val broadband = FluctuationAmplitude(
            1.0, bandwidthHz = Double.POSITIVE_INFINITY, cornerHz = 3e5
        )
        assert(broadband.fractionOfVarianceInBand().isCloseTo(1.0))
    }

    @Test
    fun `narrowing the band scales the amplitude by the square root of the fraction`() {
        // C-0004's own reading: with the drainage corner the Gen-1 tile puts a few per cent of its
        // variance below 1 kHz, and a broadband sigma is the f -> infinity limit, not a measurement.
        val broadband = FluctuationAmplitude(
            1.0, bandwidthHz = Double.POSITIVE_INFINITY, cornerHz = 3e5
        )
        val instrument = broadband.within(1e3)
        assert(instrument.bandwidthHz.isCloseTo(1e3))
        assert(instrument.rms < broadband.rms)
        assert((instrument.rms * instrument.rms)
            .isCloseTo(instrument.fractionOfVarianceInBand()))
        // and the broadband limit of the narrowed quantity is the quantity it came from
        assert(broadband.within(Double.POSITIVE_INFINITY).rms.isCloseTo(1.0))
    }

    // --- gate 4: a rupture force is a function of the bond, not a material constant ---------------

    @Test
    fun `a rupture force carries its bonded length and its loading rate`() {
        val force = RuptureForce(48.0, bondedBasePairs = 30, loadingRatePnPerSecond = 100.0)
        assert("30" in force.quote())
        assert("100" in force.quote())
        // CLAUDE.md: the 48 pN in circulation is Strunz's 30 bp number, and quoting it for a
        // domain of unstated length is optimistic by up to 2.6x. Two lengths never compare.
        val eightBasePairs = RuptureForce(18.8, bondedBasePairs = 8, loadingRatePnPerSecond = 100.0)
        assertFailsWith<IllegalArgumentException> { ratioOf(force, eightBasePairs) }
    }

    // --- gate 5: the state survives arithmetic that does not change it -----------------------------

    @Test
    fun `scaling a quantity keeps its state`() {
        val stiffness = LayerStiffness(4.0, atCompressionNm = 3.0, model = "scf-absorbing")
        val doubled = stiffness.scaledBy(2.0)
        assert(doubled.value.isCloseTo(8.0))
        assert(doubled.state == stiffness.state)
        assert(ratioOf(doubled, stiffness).isCloseTo(2.0))
    }
}
