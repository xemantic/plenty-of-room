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

import com.xemantic.nano.plentyofroom.environment.Regime
import com.xemantic.nano.plentyofroom.environment.RegimeSet
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

/**
 * The single top-level key a result file carries its emission header under.
 *
 * **Namespaced, and that is a census result rather than a preference.** The first draft of this
 * header put `lattice` and `regime` at the top level of the record, and a census of the committed
 * corpus refused it: `gpd/results/T-152-collinear-clearance.json` already carries a **top-level**
 * `lattice`, a *list* of the lattice quantities that study tabulates, so
 * [withEmissionHeader] would have thrown on the one study whose subject is closest to the tag's
 * own. Deeper, `lattice` names **101 numeric result leaves** in the corpus (`T-10`'s
 * `againstC0006[*].lattice`, `T-15`'s `continuumStations[*].lattice`) and `regime` names a string
 * leaf in five files (`GraftingRegime.MUSHROOM`, `SolutionRegime.CROSSOVER`, a shear-lag regime, a
 * coupling regime).
 *
 * That is `C-0162`'s own trap met from the other side. It excluded the singular `parameter` from
 * [PARAMETER_RECORDS] because *"it is a swept axis coordinate, so widening the set would silently
 * stop rounding 28 outputs"*; here the name a new schema field wants is **already a result**, and
 * the cure is the same shape — do not share a name with the corpus. `emission` occurs **nowhere**
 * in the 152 committed result files, at any depth.
 */
const val EMISSION_KEY: String = "emission"

/** The key inside [EMISSION_KEY] that carries the [LatticeTag]. */
const val LATTICE_KEY: String = "lattice"

/** The key inside [EMISSION_KEY] that carries the [Regime]. */
const val REGIME_KEY: String = "regime"

private val headerJson = Json { encodeDefaults = true }

/**
 * This result object with a **lattice tag** and a **regime block** in front of it.
 *
 * `T-272`'s `P3` and `P4`, which are step 6 of
 * [ARCHITECTURE.md](../../../../../../../ARCHITECTURE.md) — *"two schema fields pay for themselves
 * outright: a lattice tag on every result record … and a regime block (buffer, valency, gap,
 * bandwidth) — so consuming a result outside the range it was solved in is a gate rather than a
 * reading."*
 *
 * ## Why both are declarations rather than results
 *
 * Neither is computed by the run. [LatticeTag] cannot be derived from a study's source at all — a
 * regex over the 127 emitting studies calls 29 of them *both*, because a honeycomb study imports a
 * square-lattice constant and a square-lattice study names the honeycomb in a sentence — and a
 * [Regime] is precisely the tuple the study was **handed**. So:
 *
 *  * the header goes on at the emission boundary, where the study's own declaration is in scope;
 *  * it may not overwrite a key the study already emitted, because a silent shadow is how a query
 *    reads a wrong lattice as an authoritative one;
 *  * and [EMISSION_KEY] is a member of [PARAMETER_RECORDS], so the rounding layer cannot reach it
 *    whichever order the two calls are made in. That is `C-0162`'s **round outputs, never inputs**
 *    read on a block this task adds: a regime bound rounded to nine significant digits is the same
 *    defect `CH-0207` was filed on, one key along.
 *
 * ## Why the regime is a SET, and why a `null` one is written out
 *
 * **`T-286`, answering [`CH-0224`](../../../../../../../gpd/challenges/CH-0224-a-regime-cannot-name-a-swept-buffer.md).**
 * A [Regime] describes a **solve** and a result file is a **bag of solves**: of the 22 studies
 * naming `MagnesiumChlorideBuffer`, **17 declare a list of two to five molarities and solve every
 * state at each**, so a block that can hold one molarity was `null` on exactly the results a `P4`
 * gate exists to refuse — 136 of 136 headed files, measured. The block is therefore a
 * [RegimeSet], serialised as a JSON **array**, and the two absences are two values:
 *
 *  * **JSON `null`** — the study has not stated what it was solved at. The residue, countable,
 *    and read as `RegimeVerdict.NOT_STATED` rather than as an admission. This is what all 132
 *    existing call sites pass and it is what they mean;
 *  * **`[]`**, from [RegimeSet.noEnvironment] — a claim that no environment coordinate enters this
 *    result at all: a lattice census, a junction closure search, a plan packing.
 *
 * `CLAUDE.md`: *a `null` that means "no requirement" and a `null` that means "not stated" are
 * different values.* A third remains distinct from both — a **stated** regime whose buffer is
 * `null`, which is `Regime.neutralLayer`'s documented physical claim and serialises as a
 * one-member array.
 *
 * @param lattice which crossover lattice the numbers in this file are on.
 * @param regime the environment states some record of this result was solved at, or `null` where
 *          the study has not stated them.
 * @throws IllegalArgumentException if the receiver is not a JSON object, or already carries
 *          an [EMISSION_KEY].
 */
fun JsonElement.withEmissionHeader(lattice: LatticeTag, regime: RegimeSet?): JsonElement {
    require(this is JsonObject) {
        "a result record must be a JSON object to carry an emission header, was: " +
            this::class.simpleName
    }
    require(EMISSION_KEY !in keys) {
        "this record already carries an \"$EMISSION_KEY\" key; an emission header may not " +
            "overwrite what the study emitted"
    }
    val header = linkedMapOf<String, JsonElement>(
        EMISSION_KEY to JsonObject(
            mapOf(
                LATTICE_KEY to JsonPrimitive(lattice.tag),
                REGIME_KEY to (
                    regime?.let { set ->
                        JsonArray(set.states.map { headerJson.encodeToJsonElement(it) })
                    } ?: JsonNull
                )
            )
        )
    )
    header.putAll(this)
    return JsonObject(header)
}

/**
 * [withEmissionHeader] for a study solved at exactly **one** environment state.
 *
 * A convenience rather than a second rule: it is `RegimeSet.of(regime)` and nothing else, so a
 * single-state study reads as one and the file still carries an array. Deliberately **non-null**,
 * which is what keeps `withEmissionHeader(tag, null)` unambiguous — `null` is the study not having
 * stated, and it binds to the [RegimeSet] overload alone.
 */
fun JsonElement.withEmissionHeader(lattice: LatticeTag, regime: Regime): JsonElement =
    withEmissionHeader(lattice, RegimeSet.of(regime))
