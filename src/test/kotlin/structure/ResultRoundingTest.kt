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
        //
        // `T-212`/`CH-0154`: and it is keyed on the RECORD as well as on the spelling. A bare
        // `departure` is not a departure wherever it appears -- `T-193` emits one in VOLTS.
        for (record in DEPARTURE_RECORDS) {
            for (spelling in DEPARTURE_SPELLINGS) {
                assert(
                    DEPARTURE_DIGITS_BY_KEY["$record/$spelling"] == DEPARTURE_SIGNIFICANT_DIGITS
                )
            }
        }
        assert(DEPARTURE_DIGITS_BY_KEY["strokeOverStroke"] == null)
        assert(DEPARTURE_DIGITS_BY_KEY.size == DEPARTURE_RECORDS.size * DEPARTURE_SPELLINGS.size)
    }

    @Test
    fun `gate 4 numerical convergence - the rule should not reach a departure that is not dimensionless`() {
        // `T-212`'s `F4`, and the measurement that raised `CH-0154`. `C-0129` describes the rule
        // as being about a RECORD TYPE and then keys it on a LEAF NAME -- and the corpus carries
        // `departure` under nine other parents, one of which is
        // `T-193`'s `potentialOfZeroCharge`, where the quantity is a difference of two electrode
        // potentials in VOLTS. Two significant digits there would discard determined information
        // about a literature comparison, which is the opposite of what the rule is for.
        val document = Json.parseToJsonElement(
            """{"potentialOfZeroCharge":[{"departure":0.001420712}],""" +
                    """"convergence":[{"departure":0.001420712}]}"""
        )
        val rounded = document.roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY, floor = 0.0
        ).jsonObject
        assert(
            rounded.getValue("potentialOfZeroCharge").jsonArray[0]
                .jsonObject.getValue("departure") == JsonPrimitive(0.001420712)
        )
        assert(
            rounded.getValue("convergence").jsonArray[0]
                .jsonObject.getValue("departure") == JsonPrimitive(0.0014)
        )
    }

    @Test
    fun `gate 3 symmetry - a record-qualified key should beat an unqualified one`() {
        // `T-160` is the file where the two disagree INSIDE ONE STUDY: the same spelling carries
        // the study's ANSWER in `departures[*]`, declared at six digits with a reason, and a
        // DIAGNOSTIC in `convergence[*]`. A map keyed on the leaf name alone cannot say both.
        val document = Json.parseToJsonElement(
            """{"departures":[{"relativeDeparture":1.23456789e-4}],""" +
                    """"convergence":[{"relativeDeparture":1.23456789e-4}]}"""
        )
        val rounded = document.roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY + mapOf("relativeDeparture" to 6),
            floor = 0.0
        ).jsonObject
        assert(
            rounded.getValue("departures").jsonArray[0]
                .jsonObject.getValue("relativeDeparture") == JsonPrimitive(1.23457e-4)
        )
        assert(
            rounded.getValue("convergence").jsonArray[0]
                .jsonObject.getValue("relativeDeparture") == JsonPrimitive(1.2e-4)
        )
    }

    @Test
    fun `gate 2 limiting case - an unqualified key should still apply where no qualified one does`() {
        // The qualified form is an ADDITION, not a replacement: `P-18`'s per-key precisions are
        // unqualified and must keep working exactly as they did.
        val document = Json.parseToJsonElement(
            """{"layer":{"height":1.23456789,"stiffness":1.23456789}}"""
        )
        val rounded = document.roundedForResult(
            digitsByKey = mapOf("height" to 6), floor = 0.0
        ).jsonObject.getValue("layer").jsonObject
        assert(rounded.getValue("height") == JsonPrimitive(1.23457))
        assert(rounded.getValue("stiffness") == JsonPrimitive(1.23456789))
    }

    @Test
    fun `gate 2 limiting case - the qualifier should be the nearest OBJECT ancestor, not the array index`() {
        // Every departure in this corpus sits inside an array of records, so a qualifier that
        // counted the array as a level would match nothing at all -- which would be a silent
        // no-op, the worst failure mode a rounding rule has.
        val document = Json.parseToJsonElement(
            """{"outer":{"convergence":[{"departure":1.23456789e-4}]}}"""
        )
        val rounded = document.roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY, floor = 0.0
        ).jsonObject.getValue("outer").jsonObject
        assert(
            rounded.getValue("convergence").jsonArray[0]
                .jsonObject.getValue("departure") == JsonPrimitive(1.2e-4)
        )
    }

    @Test
    fun `gate 3 symmetry - a qualified key should apply to its whole subtree like an unqualified one`() {
        // `roundedForResult` documents `digitsByKey` as applying to the WHOLE SUBTREE under the
        // key. The qualified form inherits that, so a departure emitted as a nested object rather
        // than as a leaf is reached too.
        val document = Json.parseToJsonElement(
            """{"convergence":[{"departure":{"absolute":1.23456789e-4}}]}"""
        )
        val rounded = document.roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY, floor = 0.0
        ).jsonObject
        assert(
            rounded.getValue("convergence").jsonArray[0].jsonObject
                .getValue("departure").jsonObject.getValue("absolute") == JsonPrimitive(1.2e-4)
        )
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

    // -----------------------------------------------------------------------------------------
    // `T-214`/`C-0138` — the rule as a BASELINE, so a study obeys it by construction
    //
    // `C-0131` refused `DEPARTURE_DIGITS_BY_KEY` as the default of `roundedForResult` on a
    // measurement: keyed on a LEAF NAME it would have rounded `T-193`'s electrode potentials, in
    // volts, to two digits. That refusal was right about the map it was written against and it
    // does not survive the re-keying `C-0131` performed in the same task — a `record/spelling`
    // map cannot reach `potentialOfZeroCharge/departure` at all. What was left was 351 fields in
    // 31 files whose only defect is that their emission sites did not remember to pass the map.

    @Test
    fun `gate 3 symmetry - a departure record should obey the rule without the caller passing the map`() {
        val document = Json.parseToJsonElement(
            """{"reproductions":[{"relativeDeparture":5.36821841e-6,"carried":1.23456789012}],""" +
                    """"convergence":[{"departureFromFinest":5.36821841e-6}]}"""
        )
        val rounded = document.roundedForResult(floor = 0.0).jsonObject
        val reproduction = rounded.getValue("reproductions").jsonArray[0].jsonObject
        assert(reproduction.getValue("relativeDeparture") == JsonPrimitive(5.4e-6))
        // and the baseline must not reach the sibling it sits beside
        assert(reproduction.getValue("carried") == JsonPrimitive(1.23456789))
        assert(
            rounded.getValue("convergence").jsonArray[0]
                .jsonObject.getValue("departureFromFinest") == JsonPrimitive(5.4e-6)
        )
    }

    @Test
    fun `gate 4 numerical convergence - the baseline should not reach a departure that is not dimensionless`() {
        // `CH-0154`'s measurement, re-asserted against the BASELINE rather than against a map the
        // caller passes. This is the assertion that makes the default safe, and it is the exact
        // statement `C-0131`'s bound 2 refused for the leaf-keyed map.
        val document = Json.parseToJsonElement(
            """{"potentialOfZeroCharge":[{"departure":0.001420712}],""" +
                    """"upstreamChecks":[{"departure":5.36821841e-6}],""" +
                    """"stationLattice":[{"departure":0.34123456789}]}"""
        )
        val rounded = document.roundedForResult(floor = 0.0).jsonObject
        assert(
            rounded.getValue("potentialOfZeroCharge").jsonArray[0]
                .jsonObject.getValue("departure") == JsonPrimitive(0.001420712)
        )
        assert(
            rounded.getValue("upstreamChecks").jsonArray[0]
                .jsonObject.getValue("departure") == JsonPrimitive(5.36821841e-6)
        )
        assert(
            rounded.getValue("stationLattice").jsonArray[0]
                .jsonObject.getValue("departure") == JsonPrimitive(0.341234568)
        )
    }

    @Test
    fun `gate 3 symmetry - a caller's own qualified entry should beat the baseline`() {
        // The baseline is a floor on the rule, not a ceiling on the study: a study that has
        // MEASURED its own departure precision must still be able to declare it.
        val document = Json.parseToJsonElement(
            """{"convergence":[{"relativeDeparture":1.23456789012e-3}]}"""
        )
        val rounded = document.roundedForResult(
            digitsByKey = mapOf("convergence/relativeDeparture" to 5), floor = 0.0
        ).jsonObject
        assert(
            rounded.getValue("convergence").jsonArray[0]
                .jsonObject.getValue("relativeDeparture") == JsonPrimitive(1.2346e-3)
        )
    }

    @Test
    fun `gate 2 limiting case - passing the map explicitly should be a no-op now that it is the baseline`() {
        // The 34 studies that already pass it must be bit-identical after the change, which is
        // what lets this task re-emit 31 files and not 65.
        val document = Json.parseToJsonElement(
            """{"reproductions":[{"departure":5.36821841e-6}],""" +
                    """"convergence":[{"relativeDeparture":3.19469867e-11}],""" +
                    """"answer":{"relativeDeparture":3.19469867e-11}}"""
        )
        assert(
            document.roundedForResult(floor = 0.0) ==
                    document.roundedForResult(digitsByKey = DEPARTURE_DIGITS_BY_KEY, floor = 0.0)
        )
    }

    @Test
    fun `gate 2 limiting case - the integral-number convention should be a parameter, not a fork`() {
        // `CLAUDE.md` records six independent rounding implementations. Three of them differ from
        // this one in exactly one observable: `coupling/` and `brush/`'s coerce an integral JSON
        // number to a Double, so a count of 4 is emitted as `4.0`. That is a RENDERING convention
        // frozen by the files already committed, not a precision choice — carrying it as a
        // parameter is what lets those packages delegate without moving a single emitted count.
        val document = Json.parseToJsonElement("""{"paths":45,"stiffness":1.23456789012}""")
        val default = document.roundedForResult(floor = 0.0).jsonObject
        assert(default.getValue("paths") == JsonPrimitive(45))
        val coerced = document.roundedForResult(
            floor = 0.0, roundIntegralNumbers = true
        ).jsonObject
        assert(coerced.getValue("paths") == JsonPrimitive(45.0))
        assert(coerced.getValue("stiffness") == JsonPrimitive(1.23456789))
    }
}
