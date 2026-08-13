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
 * The reproducibility layer for `T-16`'s result file.
 *
 * A deliberate **copy** of the pattern `structure/ResultRounding.kt` established for `T-5` and
 * repeated by `actuator/` and `window/`, not an import: `T-16` owns `coupling/` and owns none
 * of those packages. `gpd/README.md`'s rule is that a re-run which changes nothing produces no
 * diff, and a bare `Double` does not satisfy it — the JIT compiles a hot reduction part-way
 * through a run, which changes the summation order and moves the last units in the last place.
 *
 * `CLAUDE.md` records the trap this does **not** cover: rounding at the serialisation boundary
 * does not make a file reproducible if it contains an **argmin**. `T-16` has exactly one, the
 * dominant compliance term of leaf `A8.2`, and [dominantCompliance] takes it on *already
 * rounded* compliances with the first index winning any tie.
 */
const val COUPLING_RESULT_SIGNIFICANT_DIGITS: Int = 9

/** The magnitude below which a `T-16` result is reported as exactly zero, in locked units. */
const val COUPLING_RESULT_ABSOLUTE_FLOOR: Double = 1e-9

/** Rounds [value] to [COUPLING_RESULT_SIGNIFICANT_DIGITS], flooring tiny magnitudes to zero. */
fun roundCouplingResult(value: Double): Double {
    if (!value.isFinite()) return value
    if (abs(value) < COUPLING_RESULT_ABSOLUTE_FLOOR) return 0.0
    val scale = 10.0.pow(COUPLING_RESULT_SIGNIFICANT_DIGITS - 1 - floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

/** Rounds every `Double` in the tree, so nothing can be emitted unrounded by omission. */
fun JsonElement.roundedForCouplingResult(): JsonElement = when (this) {
    is JsonPrimitive -> {
        val value = if (isString) null else doubleOrNull
        if (value == null) this else JsonPrimitive(roundCouplingResult(value))
    }

    is JsonArray -> JsonArray(map { it.roundedForCouplingResult() })
    is JsonObject -> JsonObject(mapValues { it.value.roundedForCouplingResult() })
}
