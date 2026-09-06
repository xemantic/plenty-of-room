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

package com.xemantic.nano.plentyofroom.environment

import kotlinx.serialization.Serializable

/**
 * How an emitted regime answers a consumer that names its own state.
 *
 * Three values, because there are three facts and a boolean can carry two.
 * [NOT_STATED] is the one a gate must **count** rather than silently admit: a result whose solved
 * range nobody has written down cannot be gated, and saying so is `SESSION-PROMPT.md`'s *when a
 * question cannot be answered with the available methods, say so plainly* read at the schema.
 */
enum class RegimeVerdict {
    /** Some state this result was solved at admits the consumer's. */
    ADMITTED,

    /** This result states its solved states and none of them admits the consumer's. */
    REFUSED,

    /** This result does not state what it was solved at, so nothing can be refused on it. */
    NOT_STATED
}

/**
 * A [RegimeVerdict] with the sentence it was reached by, `null` where there is nothing to explain.
 *
 * `C-0159`'s rule verbatim — a bare boolean is what a reader ignores — so a [RegimeVerdict.REFUSED]
 * and a [RegimeVerdict.NOT_STATED] both carry prose and an [RegimeVerdict.ADMITTED] does not.
 */
@Serializable
data class RegimeReading(
    val verdict: RegimeVerdict,
    val reason: String?
)

/**
 * The environment states a **result file** was solved at — none, one, or several.
 *
 * ## Why the arity and not the field
 *
 * [Regime] describes a **solve** and a result file is a **bag of solves**, and
 * [`CH-0224`](../../../../../../../gpd/challenges/CH-0224-a-regime-cannot-name-a-swept-buffer.md)
 * measured what that costs: of the 22 studies naming `MagnesiumChlorideBuffer`, **17 declare a
 * list of two to five molarities and solve every state at each**, so a block that can hold one
 * molarity is `null` on exactly the results a gate exists to refuse.
 *
 * The asymmetry inside [Regime] is not an oversight and it is the reason a **set** is the repair
 * rather than a wider field. `Environment.pressure(heightNm)` and `force(heightNm, biasVolts)`
 * take the height and the bias as **arguments**, so a solve is a function over them and an
 * interval is the honest statement — which is why [Regime] already carries those two as intervals
 * with containment, and the band as a value. The molarity is a **constructor** argument: it
 * identifies *which* environment. A study that sweeps it therefore instantiates several
 * environments rather than widening one, and each keeps its own correct interval.
 *
 * Widening [Regime.bufferMillimolar] to a set instead would be wrong and not merely expensive,
 * and the corpus supplies the counterexample: `actuator/TallGapDeviceBStudy` solves
 * `{0.5, 1.0, 2.0} mM` over its tall heights and `{0.5, 2.0} mM` over its fold heights, so one
 * widened regime would have to carry the **union** of both height ranges and would then admit
 * `1.0 mM` at a fold height, which no record of that file carries. A set of two regimes does not.
 *
 * ## The two absences, which are different values
 *
 * `CLAUDE.md`: *a `null` that means "no requirement" and a `null` that means "not stated" are
 * different values.* There are in fact three facts here and this type separates all three:
 *
 *  * a **`null` `RegimeSet`** — the study has not declared. Emitted as JSON `null`, countable,
 *    and read as [RegimeVerdict.NOT_STATED];
 *  * **[noEnvironment]**, the empty set — a claim that no environment coordinate enters this
 *    result at all. A lattice census, a junction closure search, a plan packing. Emitted as `[]`,
 *    and it admits every consumer, because a number that is a function of no buffer, no gap, no
 *    bias and no band may be read in any of them;
 *  * **a stated regime whose [Regime.bufferMillimolar] is `null`** — the physical claim
 *    [Regime.neutralLayer] documents, that ideal mobile salt cancels out of a neutral grafted
 *    layer exactly. Emitted as a one-member array whose member's buffer is `null`, and it does
 *    **not** admit an electrolyte consumer, because it is a statement about a solved environment
 *    rather than about the absence of one.
 *
 * ## What this cannot do
 *
 * A file-granular set is a **necessary** condition and not a sufficient one. It refuses a consumer
 * asking a file for a state no record of it carries; it cannot refuse a consumer that picks the
 * **wrong record** inside a file whose set contains the state — which is the defect `CLAUDE.md`
 * already records against the most-read file in this corpus: *"`gpd/results/T-3b-*.json` carries
 * two solved profiles per `(concentration, gap)` — one per operating bias — so
 * `firstOrNull { c && h }` silently takes whichever is listed first."* Only a regime on the
 * **record** closes that, and it is `T-272`'s sweep with a wider edit.
 *
 * @param states the environment states some record of this result was solved at, in the order the
 *          study swept them. Empty is a claim; see [noEnvironment].
 */
data class RegimeSet(val states: List<Regime>) {

    init {
        val repeated = states.groupBy { it }.filterValues { it.size > 1 }.keys.firstOrNull()
        require(repeated == null) {
            "a regime set states which environments a result was solved in, and " +
                "\"${repeated?.name}\" appears twice; a duplicate is a bookkeeping error rather " +
                "than a wider set"
        }
    }

    /**
     * Whether this result is a function of no environment coordinate at all — a **claim**, and
     * not the same fact as a `null` [RegimeSet], which is the study not having said.
     */
    val isWithoutEnvironmentCoordinate: Boolean get() = states.isEmpty()

    /**
     * Why a number from this result may not be consumed in [consumer]'s regime, or `null` if it
     * may.
     *
     * Containment rather than equality: one member admitting is enough, because the file holds a
     * record solved there. The empty set admits everything, for the reason in the class KDoc.
     *
     * The refusal names **every** member's own reason, because on a set the reader needs to know
     * which of the solved states came closest — a bare *"outside the range"* on a three-buffer
     * sweep hides that two of the three coordinates matched. A set of **one** returns its member's
     * sentence verbatim, so the commonest case reads exactly as [Regime.reasonToRefuse] does.
     */
    fun reasonToRefuse(consumer: Regime): String? {
        if (states.isEmpty()) return null
        val reasons = states.map { it to consumer.reasonToRefuse(it) }
        if (reasons.any { it.second == null }) return null
        // A set of one is the regime it holds, verbatim: composing prose around a single refusal
        // would put a count in front of the sentence `Regime` already writes.
        reasons.singleOrNull()?.let { return it.second }
        return "no state this result was solved at admits ${consumer.name}; " +
            "${states.size} states were solved and every one refuses — " +
            reasons.joinToString("; ") { (state, reason) -> "${state.name}: $reason" }
    }

    /** Whether [consumer] may read a number out of this result. */
    fun admits(consumer: Regime): Boolean = reasonToRefuse(consumer) == null

    companion object {

        /**
         * A result that is a function of no environment coordinate — no buffer, no gap, no bias,
         * no band. A **claim**, and the value a lattice census, a junction closure search or a
         * plan packing states.
         */
        val noEnvironment: RegimeSet = RegimeSet(emptyList())

        /** The states a study solved, in the order it swept them. */
        fun of(vararg states: Regime): RegimeSet = RegimeSet(states.toList())
    }
}

/**
 * What a `P4` gate learns when it asks an emitted regime about [consumer]'s own state.
 *
 * An **extension on a nullable receiver**, deliberately: the third state of the emitted block is
 * its own absence, and a gate that cannot see the difference between *"no environment coordinate
 * enters this result"* and *"this study has not said"* reports the second as the first — which is
 * exactly the reading `CH-0224` measured across all 136 headed files.
 */
fun RegimeSet?.readFor(consumer: Regime): RegimeReading = when {
    this == null -> RegimeReading(
        RegimeVerdict.NOT_STATED,
        "this result does not state the environment it was solved in, so ${consumer.name} " +
            "cannot be refused on it; the reading is a residue to be counted, not an admission"
    )
    else -> reasonToRefuse(consumer)
        ?.let { RegimeReading(RegimeVerdict.REFUSED, it) }
        ?: RegimeReading(RegimeVerdict.ADMITTED, null)
}
