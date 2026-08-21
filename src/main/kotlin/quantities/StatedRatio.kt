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

package com.xemantic.nano.plentyofroom.quantities

/**
 * The comparison [ratioOf] refuses, quoted with the two states it spans.
 *
 * [ratioOf]'s own refusal message names the escape — *"read both at the same key, or quote the
 * comparison with the two states it spans"* — and the package did not provide it, so a caller
 * whose ratio is legitimate had no way to take it except by reaching past the type. `T-3a`'s
 * `decayOverBulkDebye` and `T-3b`'s `decayLengthOverDebye` are both exactly this comparison: a
 * decay length measured in a gap over the bulk one, which is a *finding* rather than a slip.
 *
 * The kind is still refused, because two quantities of different kinds do not compare at all
 * however carefully their states are written down.
 */
data class StatedRatio(
    /** The plain number. */
    val value: Double,
    /** What kind of quantity was divided. */
    val kind: String,
    /** The numerator's state, rendered. */
    val numeratorState: String,
    /** The denominator's state, rendered. */
    val denominatorState: String
) {

    /** The ratio and both states, in one string that cannot omit either. */
    fun quote(): String =
        "$value ($kind) at [$numeratorState] against [$denominatorState]"

}

/**
 * The ratio of two quantities of one kind read at **different** states, carrying both.
 *
 * Use it where the difference of state *is* the finding. Where the two states are meant to be the
 * same, use [ratioOf] instead and let it refuse.
 */
fun statedRatio(numerator: StatedQuantity, denominator: StatedQuantity): StatedRatio {
    require(numerator.kind == denominator.kind) {
        "a ratio is not defined across two kinds of quantity: '${numerator.kind}' against " +
            "'${denominator.kind}'. They are different quantities that happen to share a unit, " +
            "and writing both states down does not make them comparable."
    }
    require(numerator.unit == denominator.unit) {
        "a ratio is not defined across two units: '${numerator.unit}' against " +
            "'${denominator.unit}'."
    }
    require(denominator.value != 0.0) { "a ratio against an exactly zero denominator" }
    return StatedRatio(
        value = numerator.value / denominator.value,
        kind = numerator.kind,
        numeratorState = numerator.state.entries.joinToString(", ") { "${it.key} = ${it.value}" },
        denominatorState = denominator.state.entries.joinToString(", ") { "${it.key} = ${it.value}" }
    )
}
