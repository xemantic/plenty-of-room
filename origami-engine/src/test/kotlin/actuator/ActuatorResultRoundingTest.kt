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
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.RESULT_ABSOLUTE_FLOOR
import com.xemantic.nano.plentyofroom.structure.RESULT_SIGNIFICANT_DIGITS
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

/**
 * `T-212`/`CH-0154` — the second rounding implementation, and why the departure rule could not
 * "live once" while it existed.
 *
 * `CLAUDE.md` records that there is no tree-wide digit count but **six independent rounding
 * implementations**, and that a change to one silently leaves the other five. `C-0129` put the
 * departure rule in `structure/ResultRounding.kt`, where `roundedForResult` has a `digitsByKey`
 * parameter to carry it; `roundedForActuatorResult` had **no such parameter at all**, so the six
 * result files on that path — `T-3`, `T-4`, `T-60`, `T-76`, `T-149`, `T-157` — could not obey the
 * rule by any edit at their own emission sites.
 */
class ActuatorResultRoundingTest {

    @Test
    fun `gate 3 conservation - CH-0207's parameter-block exemption should be inherited, not passed`() {
        // `T-268`. The rule "round outputs, never inputs" is a DEFAULT of
        // `structure/ResultRounding.kt`, so this entry point obeys it with no edit of its own and
        // no argument at its call sites — which is the whole of `C-0138`'s lesson, five
        // per-call-site repairs of the departure rule before it moved into the one place every
        // study goes through. The files on this path (`T-3`, `T-4`, `T-60`, `T-76`, `T-149`, `T-157`) are covered by construction.
        val exact = -0.3986652379247042
        val document = Json.parseToJsonElement(
            """{"runParameters":{"wallCharge":$exact},"forces":{"wallCharge":$exact}}"""
        )
        val rounded = document.roundedForActuatorResult().jsonObject
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
    fun `gate 1 dimensional consistency - the actuator constants should be the tree's constants`() {
        // The delegation below is only sound because the two implementations were the same
        // arithmetic with the same two constants. Asserted rather than assumed: if either side
        // moves, this fails before any result file is re-emitted against the wrong precision.
        assert(ACTUATOR_RESULT_SIGNIFICANT_DIGITS == RESULT_SIGNIFICANT_DIGITS)
        assert(ACTUATOR_RESULT_ABSOLUTE_FLOOR == RESULT_ABSOLUTE_FLOOR)
    }

    @Test
    fun `gate 2 limiting case - a non-departure field should keep nine significant digits`() {
        // The whole obligation this change takes on: the six files on this path must move in
        // their DEPARTURE fields and nowhere else.
        val document = Json.parseToJsonElement("""{"folds":[{"strokeAtFold":1.23456789012}]}""")
        val rounded = document.roundedForActuatorResult().jsonObject
        assert(
            rounded.getValue("folds").jsonArray[0]
                .jsonObject.getValue("strokeAtFold") == JsonPrimitive(1.23456789)
        )
    }

    @Test
    fun `gate 3 symmetry - a departure inside a reproduction record should carry two digits`() {
        val document = Json.parseToJsonElement(
            """{"reproductions":[{"departure":5.36821841e-6,"carried":1.23456789012}],""" +
                    """"convergence":[{"departureFromFinest":5.36821841e-6}]}"""
        )
        val rounded = document.roundedForActuatorResult().jsonObject
        val reproduction = rounded.getValue("reproductions").jsonArray[0].jsonObject
        assert(reproduction.getValue("departure") == JsonPrimitive(5.4e-6))
        // and it must not reach the sibling it sits beside
        assert(reproduction.getValue("carried") == JsonPrimitive(1.23456789))
        assert(
            rounded.getValue("convergence").jsonArray[0]
                .jsonObject.getValue("departureFromFinest") == JsonPrimitive(5.4e-6)
        )
        assert(DEPARTURE_SIGNIFICANT_DIGITS == 2)
    }

    @Test
    fun `gate 4 numerical convergence - a departure outside a departure record should be untouched`() {
        // `T-4` carries 288 `upstreamChecks[*].departure` fields, which are a comparison against a
        // carried upstream number rather than a residual between two refinements of one solve.
        // The record qualifier is what keeps them out of the rule.
        val document = Json.parseToJsonElement(
            """{"upstreamChecks":[{"departure":5.36821841e-6}]}"""
        )
        val rounded = document.roundedForActuatorResult().jsonObject
        assert(
            rounded.getValue("upstreamChecks").jsonArray[0]
                .jsonObject.getValue("departure") == JsonPrimitive(5.36821841e-6)
        )
    }

    @Test
    fun `gate 3 symmetry - the scalar rounder should agree with the tree's rounder`() {
        for (value in listOf(1.23456789012, -9.87654321e-7, 0.0, 1e-12, 42.0)) {
            assert(
                roundActuatorResult(value) ==
                        com.xemantic.nano.plentyofroom.structure.roundForResult(value)
            )
        }
    }
}
