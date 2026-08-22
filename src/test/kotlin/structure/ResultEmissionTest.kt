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
import com.xemantic.nano.plentyofroom.environment.Regime
import com.xemantic.nano.plentyofroom.environment.RegimeSet
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-272`'s `P3` and `P4`: a lattice tag and a regime block on every emitted record.
 *
 * Both are **declarations about the run**, not results of it, which decides three things that
 * are asserted here rather than described: they go on at the emission boundary, they may not
 * silently overwrite anything the study already emitted, and they are **not rounded** — a regime
 * bound is a number the study was handed, which is `CH-0207`/`C-0162`'s rule verbatim.
 */
class ResultEmissionTest {

    private val json = Json { prettyPrint = true }

    private val body = JsonObject(
        mapOf(
            "parameters" to JsonObject(mapOf("gapNm" to JsonPrimitive(7.0))),
            "answer" to JsonPrimitive(1.23456789012345)
        )
    )

    private val gap = Regime.magnesiumChloride(
        name = "the tile-electrode gap",
        concentrationMillimolar = 2.0,
        lowestHeightNm = 3.0,
        highestHeightNm = 30.0,
        lowestBiasVolts = 0.0,
        highestBiasVolts = 2.0
    )

    // --- gate 1: the two keys are there, first, and everything else is untouched ----------------

    @Test
    fun `the header is one first key and the body follows unchanged`() {
        val tagged = body.withEmissionHeader(LatticeTag.SQUARE, gap) as JsonObject
        assert(tagged.keys.toList() == listOf("emission", "parameters", "answer"))
        assert((tagged["emission"] as JsonObject)["lattice"] == JsonPrimitive("square"))
        assert(tagged["parameters"] == body["parameters"])
        assert(tagged["answer"] == body["answer"])
    }

    @Test
    fun `removing the header returns the body exactly`() {
        val tagged = body.withEmissionHeader(LatticeTag.HONEYCOMB, gap) as JsonObject
        assert(JsonObject(tagged - "emission") == body)
    }

    /**
     * The key is namespaced because the corpus already owns both of its sub-keys. `T-152` carries
     * a **top-level** `lattice` — a list of the lattice quantities it tabulates — so a header
     * written there would have thrown on the one study closest to the tag's own subject.
     */
    @Test
    fun `a body carrying its own top-level lattice still takes the header`() {
        val t152 = JsonObject(mapOf("lattice" to JsonPrimitive("a table this study emits")))
        val tagged = t152.withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject
        assert(tagged.keys.toList() == listOf("emission", "lattice"))
        assert(tagged["lattice"] == JsonPrimitive("a table this study emits"))
        assert((tagged["emission"] as JsonObject)["lattice"] == JsonPrimitive("square"))
    }

    // --- gate 2: an absent regime is a CLAIM, so it is an explicit null -------------------------

    @Test
    fun `a study with no solved range emits an explicit null rather than omitting the key`() {
        val emission = (body.withEmissionHeader(LatticeTag.NONE, regime = null)
                as JsonObject)["emission"] as JsonObject
        assert("regime" in emission.keys)
        assert(emission["regime"] == JsonNull)
    }

    @Test
    fun `a stated regime carries the tuple a consumer is refused on`() {
        val tagged = body.withEmissionHeader(LatticeTag.NONE, gap) as JsonObject
        val states = (tagged["emission"] as JsonObject)["regime"] as JsonArray
        assert(states.size == 1)
        val regime = states.single() as JsonObject
        assert(regime["bufferMillimolar"] == JsonPrimitive(2.0))
        assert(regime["counterionValency"] == JsonPrimitive(2))
        assert(regime["highestHeightNm"] == JsonPrimitive(30.0))
        assert(regime["bandwidthHz"] == JsonNull)
    }

    // --- gate 2b: T-286/CH-0224, a FILE is a bag of solves, so the block is a SET ---------------

    /**
     * `CH-0224`: 17 of the 22 studies naming `MagnesiumChlorideBuffer` declare a **list** of
     * molarities and solve every state at each, so a block that can hold one molarity is `null`
     * on exactly the results a `P4` gate exists to refuse. The block is therefore an array.
     */
    @Test
    fun `a swept study emits every molarity it solved`() {
        val swept = RegimeSet.of(
            gap,
            Regime.magnesiumChloride(
                name = "the same gap at NDI's reserve",
                concentrationMillimolar = 0.5,
                lowestHeightNm = 3.0,
                highestHeightNm = 30.0,
                lowestBiasVolts = 0.0,
                highestBiasVolts = 2.0
            )
        )
        val states = ((body.withEmissionHeader(LatticeTag.NONE, swept) as JsonObject)["emission"]
                as JsonObject)["regime"] as JsonArray
        assert(states.size == 2)
        assert((states[0] as JsonObject)["bufferMillimolar"] == JsonPrimitive(2.0))
        assert((states[1] as JsonObject)["bufferMillimolar"] == JsonPrimitive(0.5))
    }

    /**
     * `CLAUDE.md`: *a `null` that means "no requirement" and a `null` that means "not stated" are
     * different values.* An empty array is the study saying **no environment coordinate enters
     * this result**; a JSON `null` is the study not having said. Today's corpus carries `null` on
     * all 136 headed files and means the first by KDoc and the second in fact.
     */
    @Test
    fun `an empty set and a null regime are different JSON`() {
        val declared = ((body.withEmissionHeader(LatticeTag.NONE, RegimeSet.noEnvironment)
                as JsonObject)["emission"] as JsonObject)["regime"]
        val notStated = ((body.withEmissionHeader(LatticeTag.NONE, regime = null)
                as JsonObject)["emission"] as JsonObject)["regime"]
        assert(declared == JsonArray(emptyList()))
        assert(notStated == JsonNull)
        assert(declared != notStated)
    }

    /**
     * And the third value: a **stated** regime whose buffer is `null`, which `Regime`'s own KDoc
     * documents as a claim — ideal mobile salt cancels out of a neutral grafted layer exactly.
     */
    @Test
    fun `a stated regime with no electrolyte is a third value, not the empty set`() {
        val layer = RegimeSet.of(
            Regime.neutralLayer(
                name = "the grafted PEG layer",
                lowestHeightNm = 1.0,
                highestHeightNm = 10.0
            )
        )
        val states = ((body.withEmissionHeader(LatticeTag.NONE, layer) as JsonObject)["emission"]
                as JsonObject)["regime"] as JsonArray
        assert(states.size == 1)
        assert((states.single() as JsonObject)["bufferMillimolar"] == JsonNull)
    }

    // --- gate 3: it cannot overwrite, and it cannot be applied twice ----------------------------

    @Test
    fun `a body that already carries the emission key is refused by name`() {
        val clash = JsonObject(mapOf("emission" to JsonPrimitive("mine")))
        val failure = assertFailsWith<IllegalArgumentException> {
            clash.withEmissionHeader(LatticeTag.SQUARE, gap)
        }
        assert(failure.message!!.contains("emission"))
    }

    @Test
    fun `applying the header twice is refused`() {
        val once = body.withEmissionHeader(LatticeTag.SQUARE, gap)
        assertFailsWith<IllegalArgumentException> { once.withEmissionHeader(LatticeTag.SQUARE, gap) }
    }

    @Test
    fun `an array is not a result record`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            json.parseToJsonElement("[1, 2]").withEmissionHeader(LatticeTag.NONE, null)
        }
        assert(failure.message!!.contains("object"))
    }

    // --- gate 4: a regime bound is an INPUT, and the rounding layer must not reach it -----------

    /**
     * `C-0162`'s rule read on a block this task adds: **round outputs, never inputs.** A regime
     * bound is a number the study was handed, so a nine-significant-digit rounding of it is the
     * same defect `CH-0207` was filed on. The order of the two calls at an emission site must
     * therefore not matter, which is what makes this a test rather than a convention.
     */
    @Test
    fun `a regime block survives the rounding layer whichever order it is applied in`() {
        val awkward = Regime.magnesiumChloride(
            name = "an awkward one",
            concentrationMillimolar = 0.5000000004999999,
            lowestHeightNm = 3.0,
            highestHeightNm = 30.0,
            lowestBiasVolts = 0.0,
            highestBiasVolts = 2.0
        )
        val headerFirst = body.withEmissionHeader(LatticeTag.SQUARE, awkward).roundedForResult()
        val roundedFirst = body.roundedForResult().withEmissionHeader(LatticeTag.SQUARE, awkward)
        assert(headerFirst == roundedFirst)
        val states = ((headerFirst as JsonObject)["emission"] as JsonObject)["regime"] as JsonArray
        val regime = states.single() as JsonObject
        assert(regime["bufferMillimolar"].toString() == "0.5000000004999999")
    }

    @Test
    fun `the emission block is one of the parameter records, by census`() {
        assert(EMISSION_KEY in PARAMETER_RECORDS)
        // and its SUB-keys deliberately are not: `lattice` names 101 numeric result leaves in the
        // corpus and `regime` a string leaf in five files.
        assert(LATTICE_KEY !in PARAMETER_RECORDS)
        assert(REGIME_KEY !in PARAMETER_RECORDS)
    }

    // --- gate 5: the answer beside it is still rounded ------------------------------------------

    @Test
    fun `the header does not exempt the study's own results from rounding`() {
        val tagged = body.withEmissionHeader(LatticeTag.SQUARE, gap).roundedForResult() as JsonObject
        assert(tagged["answer"] == JsonPrimitive(1.23456789))
    }
}
