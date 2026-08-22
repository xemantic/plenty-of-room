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

package com.xemantic.nano.plentyofroom.window

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.structure.RESULT_ABSOLUTE_FLOOR
import com.xemantic.nano.plentyofroom.structure.RESULT_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.roundForResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

/**
 * `T-214`/`C-0138` — the **fourth** rounding implementation the departure rule could not reach.
 *
 * `T-118` is on this path and carries five of `T-214`'s 351 residue fields. Unlike `coupling/`'s,
 * this implementation already passes an integral JSON number through untouched, so its delegation
 * to `structure/` is observably identical apart from the departure rule itself — which is asserted
 * here rather than assumed, because `T-118` emits 25 integers and a rendering change in any of
 * them would be a non-departure movement in a sweep whose whole claim is that there are none.
 *
 * [roundedDecision] is deliberately **not** delegated: it is a *decision* precision, taken before
 * a comparison against a threshold, and `CLAUDE.md` records that a decision must be rounded
 * coarser than the number it is taken on.
 */
class WindowResultRoundingTest {

    @Test
    fun `gate 3 conservation - CH-0207's parameter-block exemption should be inherited, not passed`() {
        // `T-268`. The rule "round outputs, never inputs" is a DEFAULT of
        // `structure/ResultRounding.kt`, so this entry point obeys it with no edit of its own and
        // no argument at its call sites — which is the whole of `C-0138`'s lesson, five
        // per-call-site repairs of the departure rule before it moved into the one place every
        // study goes through. The files on this path (`T-2`, `T-25`, `T-118`) are covered by construction.
        val exact = -0.3986652379247042
        val document = Json.parseToJsonElement(
            """{"runParameters":{"wallCharge":$exact},"forces":{"wallCharge":$exact}}"""
        )
        val rounded = document.roundedForWindowResult().jsonObject
        assert(
            rounded.getValue("runParameters").jsonObject.getValue("wallCharge")
                .jsonPrimitive.content.toDouble() == exact
        )
        assert(
            rounded.getValue("forces").jsonObject.getValue("wallCharge")
                .jsonPrimitive.content.toDouble() == -0.398665238
        )
    }

    @Test
    fun `gate 1 dimensional consistency - the window constants should be the tree's constants`() {
        assert(WINDOW_RESULT_SIGNIFICANT_DIGITS == RESULT_SIGNIFICANT_DIGITS)
        assert(WINDOW_RESULT_ABSOLUTE_FLOOR == RESULT_ABSOLUTE_FLOOR)
    }

    @Test
    fun `gate 3 symmetry - the scalar rounder should agree with the tree's rounder`() {
        for (value in listOf(1.23456789012, -9.87654321e-7, 0.0, 1e-12, 42.0)) {
            assert(roundWindowResult(value) == roundForResult(value))
        }
    }

    @Test
    fun `gate 3 symmetry - a departure inside a departure record should carry two digits`() {
        val document = Json.parseToJsonElement(
            """{"reproductions":[{"relativeDeparture":8.79e-7,"carried":1.23456789012}],""" +
                    """"convergence":[{"departureFromFinest":5.36821841e-6}]}"""
        )
        val rounded = document.roundedForWindowResult().jsonObject
        val reproduction = rounded.getValue("reproductions").jsonArray[0].jsonObject
        assert(reproduction.getValue("relativeDeparture") == JsonPrimitive(8.8e-7))
        assert(reproduction.getValue("carried") == JsonPrimitive(1.23456789))
        assert(
            rounded.getValue("convergence").jsonArray[0]
                .jsonObject.getValue("departureFromFinest") == JsonPrimitive(5.4e-6)
        )
    }

    @Test
    fun `gate 2 limiting case - an integer and a decision precision should be untouched`() {
        val document = Json.parseToJsonElement("""{"lowerEdgeIndex":26,"height":1.23456789012}""")
        val rounded = document.roundedForWindowResult().jsonObject
        assert(rounded.getValue("lowerEdgeIndex") == JsonPrimitive(26))
        assert(rounded.getValue("height") == JsonPrimitive(1.23456789))
        assert(WINDOW_DECISION_SIGNIFICANT_DIGITS == 6)
        assert(roundedDecision(1.23456789012) == 1.23457)
    }
}
