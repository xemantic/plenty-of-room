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

package com.xemantic.nano.plentyofroom.window

import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.json.JsonElement
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * The reproducibility layer for `T-2`'s result file.
 *
 * A deliberate **copy** of the pattern `structure/ResultRounding.kt` established for `T-5`
 * and repeated by `actuator/` for `T-3`, not an import of it: `T-2` owns `window/` and does
 * not own either of those packages. `gpd/README.md`'s rule is that "a re-run that changes
 * nothing produces no diff", and a bare `Double` does not satisfy it — the JIT compiles a
 * hot reduction part-way through a run, which changes the summation order and moves the
 * last units in the last place.
 *
 * `CLAUDE.md` records the trap this does **not** cover: rounding at the serialisation
 * boundary does not make a file reproducible if it contains an **argmin**, because an index
 * is not a rounded double. `T-2` avoids that by construction — every window edge is an
 * *index* on the grafting-density grid, every constraint flag is a comparison made against
 * an already-rounded quantity by [roundedDecision], and every edge attribution is an integer
 * comparison with the constraint list's own order as the tie-break.
 */
const val WINDOW_RESULT_SIGNIFICANT_DIGITS: Int = 9

/** The magnitude below which a `T-2` result is reported as exactly zero, in locked units. */
const val WINDOW_RESULT_ABSOLUTE_FLOOR: Double = 1e-9

/** Rounds [value] to [WINDOW_RESULT_SIGNIFICANT_DIGITS], flooring tiny magnitudes to zero. */
fun roundWindowResult(value: Double): Double = roundForResult(
    value, WINDOW_RESULT_SIGNIFICANT_DIGITS, WINDOW_RESULT_ABSOLUTE_FLOOR
)

/**
 * The number of significant digits a **decision** is taken at, before it is compared with a
 * threshold.
 *
 * Six, not nine: a constraint flag that flipped because a drainage corner moved in its
 * twelfth digit would put a `true` in one run and a `false` in the next, and no amount of
 * rounding *after* the flag was computed would repair it. Rounding at the decision point is
 * what `CLAUDE.md` requires, and six digits is far coarser than any floating-point noise
 * while being far finer than any threshold in this task is known to.
 */
const val WINDOW_DECISION_SIGNIFICANT_DIGITS: Int = 6

/** Rounds [value] to [WINDOW_DECISION_SIGNIFICANT_DIGITS] — apply before comparing to a threshold. */
fun roundedDecision(value: Double): Double {
    if (!value.isFinite()) return value
    if (value == 0.0) return 0.0
    val scale = 10.0.pow(WINDOW_DECISION_SIGNIFICANT_DIGITS - 1 - floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

/**
 * Returns this element with every non-integral number rounded by [roundWindowResult].
 *
 * **Delegated to `structure/`** by `T-214`, for `CH-0154`'s reason: an entry point with no
 * `digitsByKey` parameter cannot carry the departure rule by any edit at its own emission site,
 * and `T-118` — five of `T-214`'s 351 residue fields — sits on this path. The delegation is
 * observably identical apart from the rule itself: this implementation already passes an integral
 * JSON number through untouched, which matters because `T-118` emits 25 of them.
 *
 * [roundedDecision] is deliberately **not** delegated. It is a *decision* precision taken before a
 * comparison, not a serialisation precision, and the two must stay separately nameable.
 */
fun JsonElement.roundedForWindowResult(): JsonElement = roundedForResult(
    digits = WINDOW_RESULT_SIGNIFICANT_DIGITS,
    floor = WINDOW_RESULT_ABSOLUTE_FLOOR
)
