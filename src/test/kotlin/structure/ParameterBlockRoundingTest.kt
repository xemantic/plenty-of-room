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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

/**
 * `T-268` / `CH-0207` — **an input is not a result**, so a parameter block is not rounded.
 *
 * `gpd/README.md`'s `results/` row promises that *"every parameter of the run is in the file, so
 * the result is reproducible from it alone"*, and the emission layer was breaking it: it dispatches
 * on the JSON **type**, so a parameter emitted as a `Double` was rounded to the file's own output
 * precision along with every result beside it. `CH-0207` measured the consequence on `T-3a` — a
 * wall charge committed as `−0.398665238` against the `−0.3986652379247042` the study solved with,
 * `1.9e−10` relative, enough to miss that file's own 2 V force **by one unit in the last emitted
 * place** when the committed literal is fed back.
 *
 * The channel is live rather than latent: seven call sites in `src/main/kotlin` read a parameter
 * block back as an **input**, `T-136`'s two of them feeding a rounded number into a solve.
 *
 * The rule is a **default of the layer**, not an argument each study remembers to pass — `C-0138`'s
 * lesson, five per-call-site repairs of the departure rule before it moved into the one place every
 * study goes through. Every one of the tree's six rounding entry points therefore obeys it by
 * construction, which is asserted here and in each of their own tests.
 */
class ParameterBlockRoundingTest {

    private fun round(json: String, digits: Int = RESULT_SIGNIFICANT_DIGITS): JsonElement =
        Json.parseToJsonElement(json).roundedForResult(digits = digits)

    /** The exact literal `CH-0207` measured, and the exact rounding that broke it. */
    private val wallCharge = -0.3986652379247042

    @Test
    fun `gate 5 literature cross-check - CH-0207's own T-3a wall charge should survive a round trip`() {
        val rounded = round("""{"runParameters":{"nominalTileChargeDensity":$wallCharge}}""")
        val out = rounded.jsonObject["runParameters"]!!.jsonObject["nominalTileChargeDensity"]!!
        assert(out.jsonPrimitive.content.toDouble() == wallCharge)
        // and the defect it replaces: the same number as a RESULT is still rounded, to the very
        // value `T-3a` committed.
        val asResult = round("""{"forces":{"nominalTileChargeDensity":$wallCharge}}""")
        assert(
            asResult.jsonObject["forces"]!!.jsonObject["nominalTileChargeDensity"]!!
                .jsonPrimitive.content.toDouble() == -0.398665238
        )
    }

    @Test
    fun `gate 3 conservation - every parameter-block spelling the corpus uses should pass through`() {
        // The census (`T-268`): `parameters` 95, `citedInputs` 41, `runParameters` 19 occurrences
        // in the 148 committed result files, every one of them at top level.
        assert(
            PARAMETER_RECORDS
                    == setOf("parameters", "runParameters", "citedInputs", "emission")
        )
        PARAMETER_RECORDS.forEach { record ->
            val out = round("""{"$record":{"x":$wallCharge}}""")
            assert(
                out.jsonObject[record]!!.jsonObject["x"]!!.jsonPrimitive.content.toDouble()
                        == wallCharge
            ) { "$record must pass its numbers through" }
        }
    }

    @Test
    fun `gate 3 conservation - the exemption should cover the WHOLE subtree, not the direct children`() {
        // A parameter block is a record TYPE, so nesting inside it is still input. `roundedForResult`
        // already carries the nearest enclosing key as `record`; that is not sufficient here,
        // because under `parameters.buffer.debyeLength` the nearest key is `buffer`.
        val out = round("""{"parameters":{"buffer":{"debyeLength":$wallCharge,"ions":[$wallCharge]}}}""")
        val buffer = out.jsonObject["parameters"]!!.jsonObject["buffer"]!!.jsonObject
        assert(buffer["debyeLength"]!!.jsonPrimitive.content.toDouble() == wallCharge)
        assert(
            buffer["ions"]!!.let { it.toString() }.contains(wallCharge.toString())
        ) { "an array inside a parameter block is input too" }
    }

    @Test
    fun `gate 2 limiting case - a singular 'parameter' leaf is a SWEPT COORDINATE and is still rounded`() {
        // The scope boundary, in the costly direction. `parameter` occurs 180 times in the corpus
        // and is a swept axis coordinate, not an input block — 152 of them strings, 28 `Double`s.
        // Widening the set to every key whose name contains "parameter" would silently stop
        // rounding those 28 outputs, which is `CLAUDE.md`'s own "a census that stops" read the
        // other way: a named set may not be widened by pattern.
        val out = round("""{"sweep":{"parameter":$wallCharge}}""")
        assert(
            out.jsonObject["sweep"]!!.jsonObject["parameter"]!!.jsonPrimitive.content.toDouble()
                    == -0.398665238
        )
    }

    @Test
    fun `gate 2 limiting case - an empty parameterRecords should restore the pre-repair behaviour exactly`() {
        // What makes the movement ATTRIBUTABLE: the repair is one argument, and turning it off
        // reproduces every committed file's own convention.
        val out = Json.parseToJsonElement("""{"runParameters":{"x":$wallCharge}}""")
            .roundedForResult(parameterRecords = emptySet())
        assert(
            out.jsonObject["runParameters"]!!.jsonObject["x"]!!.jsonPrimitive.content.toDouble()
                    == -0.398665238
        )
    }

    @Test
    fun `gate 3 conservation - roundIntegralNumbers should not coerce a parameter count`() {
        // `coupling/` and two `brush/` studies coerce an integral JSON number to a `Double`
        // (`T-214`/`C-0138`), a rendering convention frozen by their committed files. An input is exempt
        // from the precision rule, so it must be exempt from the rendering one too — otherwise the
        // exemption would itself move a field it is not about.
        val out = Json.parseToJsonElement("""{"parameters":{"paths":45},"results":{"paths":45}}""")
            .roundedForResult(roundIntegralNumbers = true)
        assert(out.jsonObject["parameters"]!!.jsonObject["paths"]!!.jsonPrimitive.content == "45")
        assert(out.jsonObject["results"]!!.jsonObject["paths"]!!.jsonPrimitive.content == "45.0")
    }

    @Test
    fun `gate 3 conservation - a string parameter is unchanged, which is why 64 files were never exposed`() {
        // `roundedForResult` dispatches on the JSON type, so the 64 files that interpolate their
        // parameters into strings already satisfied the contract — by a per-study rendering
        // convention with no rule behind it, which is the half of `CH-0207` this repair removes.
        val out = round("""{"parameters":{"x":"$wallCharge"}}""")
        assert(out.jsonObject["parameters"]!!.jsonObject["x"]!!.jsonPrimitive.content
                == wallCharge.toString())
    }

    @Test
    fun `gate 3 conservation - a departure inside a parameter block is an input, so the departure rule loses`() {
        // Precedence, stated rather than discovered: the departure rule is a PRECISION and this is
        // an EXEMPTION from precision. A number a study was handed is emitted as it was handed over,
        // whatever it is called.
        val out = round("""{"parameters":{"departure":$wallCharge}}""")
        assert(out.jsonObject["parameters"]!!.jsonObject["departure"]!!
            .jsonPrimitive.content.toDouble() == wallCharge)
        // and outside one, the departure rule still applies, at two significant digits
        val diagnostic = round("""{"convergence":{"departure":$wallCharge}}""")
        assert(diagnostic.jsonObject["convergence"]!!.jsonObject["departure"]!!
            .jsonPrimitive.content.toDouble() == -0.4)
    }
}
