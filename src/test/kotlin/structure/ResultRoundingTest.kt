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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test

/**
 * The reproducibility layer. `gpd/README.md` requires that a re-run which changes nothing
 * produces no diff, and a bare `Double` does not satisfy that: the JIT compiles a hot reduction
 * part-way through a run and moves the last units in the last place.
 */
class ResultRoundingTest {

    @Test
    fun `gate 4 numerical convergence - two values differing only in the last units in the last place should round together`() {
        val a = 25.20605369709609
        val b = 25.206053697096074
        assert(a != b)
        assert(roundForResult(a) == roundForResult(b))
        assert(roundForResult(a).isCloseTo(a, 1e-9))
    }

    @Test
    fun `gate 4 numerical convergence - roundoff-level magnitudes should be reported as exactly zero`() {
        assert(roundForResult(7.839215619919013e-14) == 0.0)
        assert(roundForResult(-7.911646801649766e-14) == 0.0)
        assert(roundForResult(RESULT_ABSOLUTE_FLOOR * 10.0) != 0.0)
    }

    @Test
    fun `gate 1 dimensional consistency - rounding should preserve the value to the stated digits`() {
        // 9 significant digits, so the relative error can reach half a unit in the ninth
        listOf(1.2345678901234e-5, 0.0126256250, 4.9504950495, 14310.78123456).forEach {
            assert(roundForResult(it).isCloseTo(it, 5e-9))
        }
        assert(roundForResult(1.0).isCloseTo(1.0))
        assert(roundForResult(0.0) == 0.0)
    }

    @Test
    fun `gate 2 limiting cases - integers booleans and strings should pass through a result tree untouched`() {
        val json = Json.parseToJsonElement(
            """{"count":4,"flag":true,"name":"LP-crossover","force":2.132266648617e-14,
               |"nested":[{"x":25.20605369709609}]}""".trimMargin()
        ).roundedForResult()
        assert(json.jsonObject["count"] == JsonPrimitive(4))
        assert(json.jsonObject["flag"] == JsonPrimitive(true))
        assert(json.jsonObject["name"] == JsonPrimitive("LP-crossover"))
        assert(json.jsonObject["force"] == JsonPrimitive(0.0))
        assert(
            json.jsonObject.getValue("nested").jsonArray[0].jsonObject["x"] ==
                    JsonPrimitive(roundForResult(25.20605369709609))
        )
    }

    @Test
    fun `gate 3 symmetry - rounding should be odd in the sign of its argument`() {
        listOf(3.14159265358979, 1e-3, 987654321.123).forEach {
            assert(roundForResult(-it) == -roundForResult(it))
        }
    }

}
