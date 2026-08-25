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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test

/**
 * `T-337`'s gate: **a flatness verdict must be emitted beside the datum it is a function of.**
 *
 * `C-0223` derives, and checks at `1 440` of `1 440` committed booleans, that a `flatAt*P90`
 * verdict is *exactly* the binomial statement `exceedance <= 0.10`. So a record that writes the
 * boolean and not the exceedance has published a hypothesis test and withheld its sample: `87`
 * of the corpus's `106` positive flatness verdicts could not be tested against their own
 * sampling error at all, which is the refusal `T-337` lifts.
 *
 * The gate is scoped to the files this task re-emitted, per `C-0083`'s *a gate that cannot come
 * clean is not a gate*; the residue outside that scope is printed, ungated, by
 * `tools/T-337-verdict-exceedance-census.py`, which is the other half of the same rule.
 */
class VerdictExceedanceTest {

    /**
     * The re-emitted files, by tag. Every verdict-bearing record in these must carry the datum.
     *
     * This list is the gate's SCOPE and it is deliberately a literal: a file that leaves it has
     * to be taken out by hand, in a diff, rather than dropping out of a glob unnoticed. It must
     * agree with `GATED` in `tools/T-337-verdict-exceedance-census.py`, which gates the same
     * scope from the corpus side.
     *
     * `T-299` is here for a reason that is not its yield -- it has **none**: `T-303`'s `routeB`
     * block TRANSCRIBES `T-299`'s verdicts rather than grading its own, so without `T-299`
     * carrying the datum `T-303` cannot publish one either.
     */
    private val gated = listOf(
        "T-279", "T-284", "T-297", "T-299", "T-303", "T-316", "T-322"
    )

    private val tolerance = 0.10

    /** `C-0087`'s ensemble size, backed out of the record rather than assumed. */
    private val realisations = 4000

    private class Record(val tag: String, val path: String, val record: JsonObject)

    private fun documents(): List<Pair<String, JsonObject>> = gated.map { tag ->
        val file = File("gpd/results").listFiles { candidate ->
            candidate.name.startsWith("$tag-") && candidate.name.endsWith(".json")
        }?.sortedBy { it.name }?.firstOrNull()
        assert(file != null) { "no committed result file for $tag" }
        tag to Json.parseToJsonElement(file!!.readText()) as JsonObject
    }

    private fun records(tag: String, path: String, node: Any?, out: MutableList<Record>) {
        when (node) {
            is JsonObject -> {
                out += Record(tag, path, node)
                node.forEach { (key, value) -> records(tag, "$path/$key", value, out) }
            }
            is JsonArray -> node.forEachIndexed { index, value ->
                records(tag, "$path/$index", value, out)
            }
            else -> Unit
        }
    }

    /** `C-0223`'s own predicate: a boolean whose key starts `flat` and contains `p90`. */
    private fun verdicts(record: JsonObject): Map<String, Boolean> = record.entries
        .mapNotNull { (key, value) ->
            val boolean = (value as? JsonPrimitive)?.booleanOrNull
            if (boolean != null
                && key.lowercase().startsWith("flat")
                && key.lowercase().contains("p90")
            ) key to boolean else null
        }.toMap()

    private fun exceedanceOf(record: JsonObject): Double? =
        (record["exceedance"] as? JsonPrimitive)?.doubleOrNull

    private fun verdictBearing(): List<Record> {
        val out = mutableListOf<Record>()
        documents().forEach { (tag, document) -> records(tag, "", document, out) }
        return out.filter { verdicts(it.record).isNotEmpty() }
    }

    // --- gate 1: the datum is there -----------------------------------------------------------

    @Test
    fun `every verdict-bearing record of a re-emitted file carries its own exceedance`() {
        val naked = verdictBearing().filter { exceedanceOf(it.record) == null }
        assert(naked.isEmpty()) {
            "${naked.size} verdict-bearing record(s) carry no exceedance, so their verdict " +
                    "cannot be tested against its own sampling error: " +
                    naked.take(6).joinToString { "${it.tag}${it.path}" }
        }
    }

    // --- gate 2: and it agrees with the verdict, which is `C-0223`'s identity -------------------

    @Test
    fun `a flat-at-p90 verdict is exactly its own exceedance against the tolerance`() {
        val disagreeing = verdictBearing().mapNotNull { row ->
            val exceedance = exceedanceOf(row.record) ?: return@mapNotNull null
            val implied = exceedance < tolerance + 1e-12
            verdicts(row.record).entries
                .firstOrNull { it.value != implied }
                ?.let { "${row.tag}${row.path}/${it.key}=${it.value} against $exceedance" }
        }
        assert(disagreeing.isEmpty()) {
            "${disagreeing.size} verdict(s) disagree with their own exceedance: " +
                    disagreeing.take(6).joinToString()
        }
    }

    // --- gate 3: the ensemble size is BACKED OUT, never assumed ---------------------------------

    @Test
    fun `the realisation count backs out of every carried exceedance and its standard error`() {
        val wrong = verdictBearing().mapNotNull { row ->
            val exceedance = exceedanceOf(row.record) ?: return@mapNotNull null
            val error = (row.record["exceedanceStandardError"] as? JsonPrimitive)?.doubleOrNull
                ?: return@mapNotNull "${row.tag}${row.path} carries no exceedanceStandardError"
            if (exceedance <= 0.0 || exceedance >= 1.0 || error <= 0.0) return@mapNotNull null
            val backed = (exceedance * (1.0 - exceedance) / (error * error)).roundToInt()
            if (backed != realisations) "${row.tag}${row.path} backs out $backed" else null
        }
        assert(wrong.isEmpty()) {
            "${wrong.size} record(s) do not back out $realisations realisations: " +
                    wrong.take(6).joinToString()
        }
    }

    // --- gate 4: an exceedance is a COUNT over the ensemble, so it lands on a lattice ------------

    @Test
    fun `every carried exceedance is a whole number of realisations`() {
        val offLattice = verdictBearing().mapNotNull { row ->
            val exceedance = exceedanceOf(row.record) ?: return@mapNotNull null
            val counted = exceedance * realisations
            // Nine-significant-digit emission of `x / 4000` cannot move it off the lattice by
            // more than half a unit in the ninth digit, so a departure above `1e-4` counts is a
            // statement about the ensemble size and not about the rounding.
            if (abs(counted - counted.roundToInt()) > 1e-4) {
                "${row.tag}${row.path} = $exceedance is $counted realisations"
            } else null
        }
        assert(offLattice.isEmpty()) {
            "${offLattice.size} exceedance(s) are not a whole count of $realisations: " +
                    offLattice.take(6).joinToString()
        }
    }

    // --- gate 5: the scope is non-empty and every gated file is really read ----------------------

    @Test
    fun `the gate reads every file it claims to gate and finds verdicts in each`() {
        val perFile = verdictBearing().groupingBy { it.tag }.eachCount()
        assert(perFile.keys == gated.toSet()) {
            "gated ${gated.toSet()} but found verdicts in ${perFile.keys}"
        }
        assert(perFile.values.all { it > 0 })
    }
}
