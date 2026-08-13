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

package com.xemantic.nano.plentyofroom.actuator

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
 * The reproducibility layer for `T-3`'s result file.
 *
 * This is a deliberate **copy** of the pattern `structure/ResultRounding.kt` established for
 * `T-5`, not an import of it: `T-3` owns `actuator/` and does not own `structure/`, and two
 * agents were live in `structure/` while this ran. The rule it enforces is `gpd/README.md`'s —
 * "a re-run that changes nothing produces no diff" — and a bare `Double` does not satisfy it,
 * because the JIT compiles a hot reduction part-way through a run, which changes the summation
 * order, which moves the last units in the last place.
 *
 * Nine significant digits is seven more than any number in this programme is worth at TRL 1–3.
 */
const val ACTUATOR_RESULT_SIGNIFICANT_DIGITS: Int = 9

/**
 * The magnitude below which a result is reported as exactly zero, in the locked units.
 *
 * `T-3`'s smallest interesting quantity is a stroke of order 1e−3 nm and a force of order
 * 1e−3 pN, so a nano-unit floor is six orders below anything load-bearing, and reporting
 * `0.0` for a residual whose digits are pure roundoff is both more reproducible and more
 * honest than reporting fifteen digits of noise.
 */
const val ACTUATOR_RESULT_ABSOLUTE_FLOOR: Double = 1e-9

/** Rounds [value] to [ACTUATOR_RESULT_SIGNIFICANT_DIGITS], flooring tiny magnitudes to zero. */
fun roundActuatorResult(value: Double): Double {
    if (!value.isFinite()) return value
    if (abs(value) < ACTUATOR_RESULT_ABSOLUTE_FLOOR) return 0.0
    val scale = 10.0.pow(ACTUATOR_RESULT_SIGNIFICANT_DIGITS - 1 - floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

/**
 * Returns this element with every non-integral number rounded by [roundActuatorResult].
 *
 * Applied to the whole tree at the serialisation boundary rather than at each construction
 * site, so no result can be emitted unrounded by omission.
 */
fun JsonElement.roundedForActuatorResult(): JsonElement = when (this) {
    is JsonObject -> JsonObject(mapValues { (_, value) -> value.roundedForActuatorResult() })
    is JsonArray -> JsonArray(map { it.roundedForActuatorResult() })
    is JsonPrimitive -> when {
        isString -> this
        content.none { it == '.' || it == 'e' || it == 'E' } -> this
        else -> doubleOrNull?.let { JsonPrimitive(roundActuatorResult(it)) } ?: this
    }
}
