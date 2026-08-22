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
 * `T-214`/`C-0138` — the **third** rounding implementation the departure rule could not reach.
 *
 * `CH-0154` measured the defect on `actuator/` and named it there; the same shape stands in
 * `coupling/` and `window/`, whose `roundedFor…Result()` also take **no arguments at all**. Six of
 * `T-214`'s 31 residue files — `T-16`, `T-17`, `T-101`, `T-113`, `T-122`, `T-123` — are on this
 * path, so they could not have obeyed the rule by any edit at their own emission sites either.
 *
 * The delegation is a refactoring rather than a precision change because the constants are the
 * same, which is asserted below so the equality cannot lapse silently — and because the one
 * observable that differs, the rendering of an integral JSON number, is carried as a parameter
 * rather than dropped.
 */
class CouplingResultRoundingTest {

    @Test
    fun `gate 3 conservation - CH-0207's parameter-block exemption should be inherited, not passed`() {
        // `T-268`. The rule "round outputs, never inputs" is a DEFAULT of
        // `structure/ResultRounding.kt`, so this entry point obeys it with no edit of its own and
        // no argument at its call sites — which is the whole of `C-0138`'s lesson, five
        // per-call-site repairs of the departure rule before it moved into the one place every
        // study goes through. The files on this path (`T-16`, `T-17`, `T-101`, `T-113`, `T-122`, `T-123`) are covered by construction.
        val exact = -0.3986652379247042
        val document = Json.parseToJsonElement(
            """{"runParameters":{"wallCharge":$exact},"forces":{"wallCharge":$exact}}"""
        )
        val rounded = document.roundedForCouplingResult().jsonObject
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
    fun `gate 1 dimensional consistency - the coupling constants should be the tree's constants`() {
        assert(COUPLING_RESULT_SIGNIFICANT_DIGITS == RESULT_SIGNIFICANT_DIGITS)
        assert(COUPLING_RESULT_ABSOLUTE_FLOOR == RESULT_ABSOLUTE_FLOOR)
    }

    @Test
    fun `gate 3 symmetry - the scalar rounder should agree with the tree's rounder`() {
        for (value in listOf(1.23456789012, -9.87654321e-7, 0.0, 1e-12, 42.0)) {
            assert(roundCouplingResult(value) == roundForResult(value))
        }
    }

    @Test
    fun `gate 2 limiting case - a non-departure field should keep nine significant digits`() {
        val document = Json.parseToJsonElement("""{"peakDishing":1.23456789012}""")
        assert(
            document.roundedForCouplingResult().jsonObject
                .getValue("peakDishing") == JsonPrimitive(1.23456789)
        )
    }

    @Test
    fun `gate 3 symmetry - a departure inside a departure record should carry two digits`() {
        val document = Json.parseToJsonElement(
            """{"reproductions":[{"relativeDeparture":5.36821841e-6,"carried":1.23456789012}],""" +
                    """"convergence":[{"departureFromFinest":5.36821841e-6}]}"""
        )
        val rounded = document.roundedForCouplingResult().jsonObject
        val reproduction = rounded.getValue("reproductions").jsonArray[0].jsonObject
        assert(reproduction.getValue("relativeDeparture") == JsonPrimitive(5.4e-6))
        assert(reproduction.getValue("carried") == JsonPrimitive(1.23456789))
        assert(
            rounded.getValue("convergence").jsonArray[0]
                .jsonObject.getValue("departureFromFinest") == JsonPrimitive(5.4e-6)
        )
    }

    @Test
    fun `gate 4 numerical convergence - a departure outside a departure record should be untouched`() {
        val document = Json.parseToJsonElement("""{"identity":[{"departure":5.36821841e-6}]}""")
        assert(
            document.roundedForCouplingResult().jsonObject
                .getValue("identity").jsonArray[0]
                .jsonObject.getValue("departure") == JsonPrimitive(5.36821841e-6)
        )
    }

    @Test
    fun `gate 2 limiting case - an integral number should keep this package's rendering`() {
        // Every committed `coupling/` result file renders a count as `45.0`, because this
        // implementation coerces an integral JSON number to a Double where `structure/`'s passes
        // it through. That is a rendering convention, not a precision one, and preserving it is
        // what makes the delegation move DEPARTURE FIELDS AND NOTHING ELSE in the six files.
        val document = Json.parseToJsonElement("""{"paths":45,"flag":true,"note":"45"}""")
        val rounded = document.roundedForCouplingResult().jsonObject
        assert(rounded.getValue("paths") == JsonPrimitive(45.0))
        assert(rounded.getValue("flag") == JsonPrimitive(true))
        assert(rounded.getValue("note") == JsonPrimitive("45"))
    }
}
