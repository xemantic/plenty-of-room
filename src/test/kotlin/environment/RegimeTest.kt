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

package com.xemantic.nano.plentyofroom.environment

import com.xemantic.kotlin.test.assert
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-265`'s `P4`: the regime is **data**, not a validity sentence.
 *
 * A validity range written in prose is respected by whoever reads it. The tuple a downstream
 * consumer has to be *refused* on is a different object, and this repository has paid for the
 * difference more than once — `CH-0004` substituted one of three correct Debye lengths for
 * another, and `CH-0007` compared a diffuse-layer drop against an applied bias.
 *
 * [Regime] therefore answers two questions mechanically: may **this** environment be asked about
 * this state, and may a number solved in **that** regime be consumed in this one. The second
 * returns a *reason*, because a bare boolean is what a reader ignores.
 */
class RegimeTest {

    private val gap = Regime.magnesiumChloride(
        name = "the tile-electrode gap",
        concentrationMillimolar = 2.0,
        lowestHeightNm = 3.0,
        highestHeightNm = 30.0,
        lowestBiasVolts = 0.0,
        highestBiasVolts = 2.0
    )

    // --- gate 1: a state outside the declared range is refused, and the message names it ---------

    @Test
    fun `a height inside the declared range is admitted`() {
        assert(gap.admitsHeight(3.0))
        assert(gap.admitsHeight(10.0))
        assert(gap.admitsHeight(30.0))
    }

    @Test
    fun `a height outside the declared range is refused by name`() {
        assert(!gap.admitsHeight(2.999))
        assert(!gap.admitsHeight(30.001))
        val failure = assertFailsWith<IllegalArgumentException> { gap.requireAdmits(31.0, 0.0) }
        assert(failure.message!!.contains("height"))
        assert(failure.message!!.contains("the tile-electrode gap"))
    }

    @Test
    fun `a bias outside the declared range is refused by name`() {
        assert(!gap.admitsBias(2.5))
        val failure = assertFailsWith<IllegalArgumentException> { gap.requireAdmits(10.0, 2.5) }
        assert(failure.message!!.contains("bias"))
    }

    // --- gate 2: consuming a number solved somewhere else --------------------------------------

    @Test
    fun `a regime consumes a number solved in itself`() {
        assert(gap.reasonToRefuse(gap) == null)
    }

    @Test
    fun `a regime refuses a number solved at another buffer concentration`() {
        val other = Regime.magnesiumChloride(
            name = "the same gap at 10 mM",
            concentrationMillimolar = 10.0,
            lowestHeightNm = 3.0, highestHeightNm = 30.0,
            lowestBiasVolts = 0.0, highestBiasVolts = 2.0
        )
        val reason = gap.reasonToRefuse(other)
        assert(reason != null)
        assert(reason!!.contains("10"))
    }

    @Test
    fun `a regime refuses a number solved with no electrolyte in it at all`() {
        val neutral = Regime.neutralLayer("a grafted layer", 1.0, 21.0)
        assert(gap.reasonToRefuse(neutral) != null)
        assert(neutral.reasonToRefuse(gap) != null)
    }

    @Test
    fun `a regime refuses a number solved outside its own height range`() {
        val shallow = Regime.magnesiumChloride(
            name = "a shallower gap", concentrationMillimolar = 2.0,
            lowestHeightNm = 0.5, highestHeightNm = 2.0,
            lowestBiasVolts = 0.0, highestBiasVolts = 2.0
        )
        assert(gap.reasonToRefuse(shallow) != null)
    }

    @Test
    fun `a regime refuses a broadband number where it declares a band, and says so`() {
        val banded = gap.copy(bandwidthHz = 1000.0)
        val reason = banded.reasonToRefuse(gap)
        assert(reason != null)
        assert(reason!!.contains("band"))
    }

    // --- gate 3: it is DATA, which is what T-268 has to serialise -------------------------------

    @Test
    fun `a regime round-trips through JSON`() {
        val json = Json.encodeToString(Regime.serializer(), gap)
        assert(Json.decodeFromString(Regime.serializer(), json) == gap)
        assert(json.contains("bufferMillimolar"))
        assert(json.contains("counterionValency"))
        assert(json.contains("bandwidthHz"))
    }

    @Test
    fun `the magnesium chloride factory carries the 2 to 1 stoichiometry`() {
        assert(gap.counterionValency == 2)
        assert(gap.electrolyte.contains("2:1"))
        assert(gap.bufferMillimolar == 2.0)
    }

    @Test
    fun `an inverted range is refused at construction`() {
        assertFailsWith<IllegalArgumentException> {
            Regime.neutralLayer("inverted", 10.0, 1.0)
        }
    }

}
