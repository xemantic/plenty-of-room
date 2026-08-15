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
 *
 * **It is a ceiling, not a tree-wide constant** (`P-18`). It is the right count only where every
 * solver on the path to the number is tighter than `1e−9` — analytic models and closed-form
 * geometry, where `C-0031` measured movements of one ulp. Anything downstream of a solved height
 * is determined to [SOLVED_HEIGHT_SIGNIFICANT_DIGITS] or fewer, and printing nine of those made a
 * re-run diff certify the code **path** rather than the **answer** (`CH-0043`).
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

/**
 * The number of significant digits a quantity downstream of a **solved SCF height** is determined to.
 *
 * `P-18`, measured rather than assumed — `brush.DeterminedPrecisionStudyKt` and
 * `gpd/results/P-18-determined-precision.json`. `SelfConsistentFieldLayer.heightAtPressure` closes
 * its bracket at a *relative* `HEIGHT_TOLERANCE = 1e-6`, and `CH-0043` measured `T-1f` relocating
 * inside exactly that when `P-15` changed the solver's path without changing its answer. Printing
 * nine digits of a six-digit number is what made a re-run diff a property of the **code path**
 * rather than of the **answer**.
 *
 * This is a ceiling, not a promise: a quantity read as a *second difference* of free energies over
 * a spacing that itself depends on the solved height is determined to fewer digits still, and those
 * are declared per key at their own emission site.
 */
const val SOLVED_HEIGHT_SIGNIFICANT_DIGITS: Int = 6

/**
 * The digits of a quantity whose largest relative movement under a legitimate change of the solve
 * path is [relativeMovement] — `floor(−log₁₀ m)`, clamped to `[1, RESULT_SIGNIFICANT_DIGITS]`.
 *
 * A movement of `9.0e−7` — `CH-0043`'s measured median for `T-1f` — gives **six**. A movement of
 * `4.3e−16`, `C-0031`'s one-ulp majority in `T-1c`, saturates the clamp and asks for nothing: an
 * analytic model determines far more digits than this project has any use for.
 *
 * Zero and non-finite movements return the ceiling rather than an infinite digit count, because a
 * quantity that did not move under the probe is not thereby known exactly — it is known to at least
 * what the probe could resolve.
 */
fun determinedDigits(relativeMovement: Double): Int {
    if (!relativeMovement.isFinite() || relativeMovement <= 0.0) return RESULT_SIGNIFICANT_DIGITS
    val digits = floor(-log10(relativeMovement)).toInt()
    return digits.coerceIn(1, RESULT_SIGNIFICANT_DIGITS)
}

/**
 * Rounds [value] to [digits] significant digits, flooring magnitudes below [floor] to zero.
 *
 * @param floor the magnitude below which the result is reported as exactly zero. The default
 *          [RESULT_ABSOLUTE_FLOOR] is stated **in the locked units** — it is a statement that no
 *          force below a nanopiconewton is of interest — so a study emitting **dimensionless**
 *          quantities must lower it. `P-18` found its own determined-precision measurement
 *          flattened to `0.0` by the default, a relative movement of `3.3e−13` being exactly the
 *          kind of number the floor was written to suppress and exactly the number the study is
 *          about. Same shape as `C-0031`'s floored `layerStiffness` beside an unfloored
 *          `√(k_BT/k)`: **an absolute floor is a claim about units, and it does not travel.**
 */
fun roundForResult(
    value: Double,
    digits: Int = RESULT_SIGNIFICANT_DIGITS,
    floor: Double = RESULT_ABSOLUTE_FLOOR
): Double {
    require(digits in 1..RESULT_SIGNIFICANT_DIGITS) {
        "digits must be within 1..$RESULT_SIGNIFICANT_DIGITS, was: $digits"
    }
    require(floor >= 0.0) { "floor must not be negative, was: $floor" }
    if (!value.isFinite()) return value
    if (abs(value) < floor) return 0.0
    val scale = 10.0.pow(digits - 1 - kotlin.math.floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

/**
 * Returns this element with every non-integral number rounded by [roundForResult].
 *
 * Applied to the whole tree at the serialisation boundary rather than at each construction
 * site, so no result can be emitted unrounded by omission. Integers, booleans and strings pass
 * through untouched — a path count of `4` stays `4`.
 *
 * @param digits the precision the study's answers are determined to. The default is the ceiling
 *          [RESULT_SIGNIFICANT_DIGITS], which is correct only where every solver on the path to the
 *          number is tighter than `1e−9` — analytic models and closed-form geometry.
 * @param digitsByKey per-key overrides, applied to the **whole subtree** under that key. `P-18`'s
 *          measurement is that the determined precision is not one number even within one study:
 *          a stiffness at deep compression is a second difference of free energies and moves ~10⁴×
 *          further than the height it is evaluated at.
 * @param floor see [roundForResult] — lower it for a study emitting dimensionless quantities.
 */
fun JsonElement.roundedForResult(
    digits: Int = RESULT_SIGNIFICANT_DIGITS,
    digitsByKey: Map<String, Int> = emptyMap(),
    floor: Double = RESULT_ABSOLUTE_FLOOR
): JsonElement = when (this) {
    is JsonObject -> JsonObject(
        mapValues { (key, value) ->
            value.roundedForResult(digitsByKey[key] ?: digits, digitsByKey, floor)
        }
    )
    is JsonArray -> JsonArray(map { it.roundedForResult(digits, digitsByKey, floor) })
    is JsonPrimitive -> when {
        isString -> this
        content.none { it == '.' || it == 'e' || it == 'E' } -> this
        else -> doubleOrNull?.let { JsonPrimitive(roundForResult(it, digits, floor)) } ?: this
    }
    else -> this
}
