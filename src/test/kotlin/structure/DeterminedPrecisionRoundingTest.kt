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
 * `P-18`. A result file is rounded to make a re-run diff mean something, and what it means
 * depends on **how many of the printed digits the answer is determined to**. Nine everywhere
 * makes the diff a property of the code path rather than of the answer (`CH-0043`); a declared
 * per-provenance precision makes it a property of the answer.
 */
class DeterminedPrecisionRoundingTest {

    @Test
    fun `gate 2 limiting cases - the digit count should be honoured exactly`() {
        assert(roundForResult(1.23456789012, digits = 6) == 1.23457)
        assert(roundForResult(1.23456789012, digits = 3) == 1.23)
        assert(roundForResult(1.23456789012, digits = 1) == 1.0)
        assert(roundForResult(0.0, digits = 6) == 0.0)
    }

    @Test
    fun `gate 2 limiting cases - the default digit count should be the standing one`() {
        listOf(1.2345678901234e-5, 0.0126256250, 4.9504950495, 14310.78123456).forEach {
            assert(roundForResult(it) == roundForResult(it, digits = RESULT_SIGNIFICANT_DIGITS))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - rounding should commute with a decade rescaling`() {
        // a digit count is dimensionless, so changing the unit by a power of ten must not change
        // which digits survive
        listOf(3, 6, 9).forEach { digits ->
            listOf(1.23456789012, 9.87654321098e-3, 4.44444444444e7).forEach { value ->
                listOf(1e-6, 1e-3, 1.0, 1e3, 1e6).forEach { decade ->
                    assert(
                        roundForResult(value * decade, digits)
                            .isCloseTo(roundForResult(value, digits) * decade, 1e-12)
                    )
                }
            }
        }
    }

    @Test
    fun `gate 3 symmetry - rounding should be odd in its sign at every digit count`() {
        (1..9).forEach { digits ->
            listOf(3.14159265358979, 1e-3, 987654321.123).forEach {
                assert(roundForResult(-it, digits) == -roundForResult(it, digits))
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - a per-key override should apply to its whole subtree and nothing else`() {
        val json = Json.parseToJsonElement(
            """{"height":1.23456789012,
               |"stiffnessAtSevenTenths":1.23456789012,
               |"nested":{"stiffnessAtSevenTenths":[1.23456789012,9.87654321098]},
               |"other":{"height":1.23456789012}}""".trimMargin()
        ).roundedForResult(digits = 6, digitsByKey = mapOf("stiffnessAtSevenTenths" to 2))
        assert(json.jsonObject["height"] == JsonPrimitive(1.23457))
        assert(json.jsonObject["stiffnessAtSevenTenths"] == JsonPrimitive(1.2))
        val nested = json.jsonObject.getValue("nested").jsonObject
            .getValue("stiffnessAtSevenTenths").jsonArray
        assert(nested[0] == JsonPrimitive(1.2))
        assert(nested[1] == JsonPrimitive(9.9))
        assert(json.jsonObject.getValue("other").jsonObject["height"] == JsonPrimitive(1.23457))
    }

    @Test
    fun `gate 2 limiting cases - integers booleans and strings should still pass through untouched`() {
        val json = Json.parseToJsonElement(
            """{"count":4,"flag":true,"name":"LP-crossover","force":2.132266648617e-14}"""
        ).roundedForResult(digits = 6)
        assert(json.jsonObject["count"] == JsonPrimitive(4))
        assert(json.jsonObject["flag"] == JsonPrimitive(true))
        assert(json.jsonObject["name"] == JsonPrimitive("LP-crossover"))
        assert(json.jsonObject["force"] == JsonPrimitive(0.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the absolute floor is in LOCKED UNITS and must not be applied to a dimensionless quantity`() {
        // the defect the P-18 run exposed in its own first emission: `RESULT_ABSOLUTE_FLOOR` is
        // "the magnitude in the locked units below which a result is reported as exactly zero",
        // and a *relative movement* is not in locked units at all. A determined-precision
        // measurement lives entirely below `1e-9` and was flattened to `0.0` by it — the same
        // shape as `C-0031`'s floored `layerStiffness` beside an unfloored `√(k_BT/k)`.
        assert(roundForResult(3.322e-13) == 0.0)
        assert(roundForResult(3.322e-13, floor = 1e-18) == 3.322e-13)
        assert(roundForResult(1e-11, floor = 1e-18) == 1e-11)
        // and the floor still bites at the default
        assert(roundForResult(1e-11) == 0.0)
    }

    @Test
    fun `gate 2 limiting cases - a per-call floor should reach the whole tree`() {
        val json = Json.parseToJsonElement("""{"movement":3.322e-13,"nested":[1.0e-12]}""")
            .roundedForResult(digits = 3, floor = 1e-18)
        assert(json.jsonObject["movement"] == JsonPrimitive(3.32e-13))
        assert(json.jsonObject.getValue("nested").jsonArray[0] == JsonPrimitive(1.0e-12))
    }

    @Test
    fun `gate 2 limiting cases - determined digits should invert a relative movement`() {
        assert(determinedDigits(9.0e-7) == 6)
        assert(determinedDigits(1.0e-6) == 6)
        assert(determinedDigits(1.5e-2) == 1)
        assert(determinedDigits(4.3e-16) == RESULT_SIGNIFICANT_DIGITS)
        assert(determinedDigits(0.0) == RESULT_SIGNIFICANT_DIGITS)
        assert(determinedDigits(0.9) == 1)
        assert(determinedDigits(12.0) == 1)
    }

    @Test
    fun `gate 3 symmetry - a number rounded to its determined digits should be invariant under a movement inside its tolerance`() {
        // the whole point: a quantity known to `m` relative must not move a printed digit when it
        // is perturbed by less than `m`
        val movement = 9.0e-7
        val digits = determinedDigits(movement)
        var moved = 0
        var checked = 0
        // deterministic probe rather than a random one, so the test cannot flake
        (1..2000).forEach { i ->
            val value = 1.0 + i / 997.0
            val perturbed = value * (1.0 + (if (i % 2 == 0) 1.0 else -1.0) * movement * 0.1)
            checked++
            if (roundForResult(value, digits) != roundForResult(perturbed, digits)) moved++
        }
        assert(checked == 2000)
        // a perturbation a tenth of the determined precision may still straddle a rounding
        // boundary, but only rarely — this is the `1e-6` per comparison `C-0064` also measured
        assert(moved < checked / 20)
    }

    @Test
    fun `gate 4 numerical convergence - the decision precision and the emission precision must be the same number`() {
        // `S-166`'s trap: an index is not a rounded double. An argmin taken at nine digits and
        // emitted at six can name an entry that is not the smallest of the emitted ones.
        val values = listOf(1.000000400, 1.000000100, 1.000000900)
        val argminAtNine = values.indices.minByOrNull { roundForResult(values[it], 9) }
        val argminAtSix = values.indices.minByOrNull { roundForResult(values[it], 6) }
        // at six digits every entry is 1.00000, so the tie must be broken by index, not by value
        assert(values.map { roundForResult(it, 6) }.distinct().size == 1)
        assert(argminAtNine == 1)
        assert(argminAtSix == 0)
        assert(argminAtNine != argminAtSix)
    }

}
