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
    fun `gate 2 limiting cases - an exact zero survives a zero floor`() {
        // `CLAUDE.md`: an absolute floor is a claim about UNITS and does not travel, so a study
        // emitting dimensionless quantities has to lower it — and at `floor = 0.0` the floor test
        // no longer catches an exact zero, `log10(0)` is `-Infinity`, and `roundToLong` is handed
        // a `NaN`. `T-190` hit it on a convergence departure that is exactly zero because two
        // sample grids agree to the last bit. A zero is exactly representable at every precision.
        assert(roundForResult(0.0, floor = 0.0) == 0.0)
        assert(roundForResult(-0.0, floor = 0.0) == 0.0)
        assert(roundForResult(0.0, digits = 2, floor = 0.0) == 0.0)
        // and the value the zero floor exists to preserve is still preserved
        assert(roundForResult(2.1e-15, digits = 2, floor = 0.0).isCloseTo(2.1e-15))
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


    // ---------------------------------------------------------------------------------------
    // `T-208`/`C-0129` — a DEPARTURE is a record type, not a file
    // ---------------------------------------------------------------------------------------

    @Test
    fun `gate 4 numerical convergence - a departure should be emitted at two significant digits`() {
        // `C-0093`'s own instance: two runs of identical code agreeing on every number in a
        // 100 kB file and disagreeing in the eleventh decimal of one convergence departure.
        val runA = 3.19469867e-11
        val runB = 3.19472365e-11
        assert(roundForResult(runA, floor = 0.0) != roundForResult(runB, floor = 0.0))
        assert(
            roundForResult(runA, digits = DEPARTURE_SIGNIFICANT_DIGITS, floor = 0.0) ==
                    roundForResult(runB, digits = DEPARTURE_SIGNIFICANT_DIGITS, floor = 0.0)
        )
        assert(DEPARTURE_SIGNIFICANT_DIGITS == 2)
    }

    @Test
    fun `gate 3 symmetry - the departure key map should carry every spelling of the record`() {
        // `C-0101` cured the trap in `convergence` records and `C-0127` found it alive in
        // `reproductions` ones. The rule is about the QUANTITY, so the map is keyed on every
        // spelling the corpus uses for it, not on the one file that last went wrong.
        assert(DEPARTURE_DIGITS_BY_KEY["departure"] == DEPARTURE_SIGNIFICANT_DIGITS)
        assert(DEPARTURE_DIGITS_BY_KEY["relativeDeparture"] == DEPARTURE_SIGNIFICANT_DIGITS)
        assert(DEPARTURE_DIGITS_BY_KEY["departureFromFinest"] == DEPARTURE_SIGNIFICANT_DIGITS)
        assert(DEPARTURE_DIGITS_BY_KEY["strokeOverStroke"] == null)
    }

    @Test
    fun `gate 2 limiting case - the departure map should reach a departure nested in an array`() {
        val document = Json.parseToJsonElement(
            """{"reproductions":[{"quantity":"a","departure":5.36821841e-6,"published":1.23456789}]}"""
        )
        val rounded = document.roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY, floor = 0.0
        ).jsonObject
        val record = rounded.getValue("reproductions").jsonArray[0].jsonObject
        assert(record.getValue("departure") == JsonPrimitive(5.4e-6))
        // and it must NOT reach the sibling it sits beside
        assert(record.getValue("published") == JsonPrimitive(1.23456789))
    }

    @Test
    fun `gate 3 symmetry - an exact zero departure should survive the two-digit rule unchanged`() {
        // `T-190` emits a convergence departure that is exactly zero because two sample grids
        // agree to the last bit; `roundForResult` must not hand `roundToLong` a `NaN`.
        assert(roundForResult(0.0, digits = DEPARTURE_SIGNIFICANT_DIGITS, floor = 0.0) == 0.0)
    }
}
