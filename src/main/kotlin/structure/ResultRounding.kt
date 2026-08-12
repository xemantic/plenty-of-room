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

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * The number of significant digits every floating-point number in a result file is rounded to.
 *
 * Not a display choice — a **reproducibility** one. `gpd/README.md` requires that "a re-run
 * that changes nothing produces no diff", and a bare `Double` does not satisfy that: the JIT
 * compiles a hot reduction part-way through a run, which changes the summation order, which
 * moves the last one or two units in the last place. Rounding well above that noise floor makes
 * the file a function of the model rather than of the JVM's warm-up schedule.
 *
 * Nine digits is roughly seven more than any number in this programme is worth at TRL 1–3.
 */
const val RESULT_SIGNIFICANT_DIGITS: Int = 9

/**
 * The magnitude in the locked units below which a result is reported as exactly zero.
 *
 * `T-5`'s central finding is that the internal shear under a uniform load is zero; the solver
 * returns it as `1e−14 pN`, whose *digits* are pure roundoff and differ run to run. Reporting
 * `0.0` for anything under a nanopiconewton is both more reproducible and more honest than
 * reporting fifteen digits of noise — the smallest force of any interest here is `1e−3 pN`.
 */
const val RESULT_ABSOLUTE_FLOOR: Double = 1e-9

/** Rounds [value] to [RESULT_SIGNIFICANT_DIGITS], flooring magnitudes below [RESULT_ABSOLUTE_FLOOR] to zero. */
fun roundForResult(value: Double): Double {
    if (!value.isFinite()) return value
    if (abs(value) < RESULT_ABSOLUTE_FLOOR) return 0.0
    val scale = 10.0.pow(RESULT_SIGNIFICANT_DIGITS - 1 - floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

/**
 * Returns this element with every non-integral number rounded by [roundForResult].
 *
 * Applied to the whole tree at the serialisation boundary rather than at each construction
 * site, so no result can be emitted unrounded by omission. Integers, booleans and strings pass
 * through untouched — a path count of `4` stays `4`.
 */
fun JsonElement.roundedForResult(): JsonElement = when (this) {
    is JsonObject -> JsonObject(mapValues { (_, value) -> value.roundedForResult() })
    is JsonArray -> JsonArray(map { it.roundedForResult() })
    is JsonPrimitive -> when {
        isString -> this
        content.none { it == '.' || it == 'e' || it == 'E' } -> this
        else -> doubleOrNull?.let { JsonPrimitive(roundForResult(it)) } ?: this
    }
    else -> this
}
