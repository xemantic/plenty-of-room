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

import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.json.JsonElement

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
fun roundActuatorResult(value: Double): Double = roundForResult(
    value, ACTUATOR_RESULT_SIGNIFICANT_DIGITS, ACTUATOR_RESULT_ABSOLUTE_FLOOR
)

/**
 * Returns this element with every non-integral number rounded by [roundActuatorResult], and every
 * **departure** rounded by `C-0093`'s two-significant-digit rule.
 *
 * Applied to the whole tree at the serialisation boundary rather than at each construction
 * site, so no result can be emitted unrounded by omission.
 *
 * **`T-212`/`CH-0154` — this used to be a hand-copy of `structure/ResultRounding.kt` and is now a
 * delegation to it.** The copy was deliberate and was correct when it was written (`T-3` owned
 * `actuator/` and two agents were live in `structure/`), and it is what made `C-0129`'s
 * *"the rule now lives once, by name"* untrue on this path: `roundedForResult` carries a
 * `digitsByKey` parameter to express the departure rule and this function had **no parameter at
 * all**, so the six files emitted through it — `T-3`, `T-4`, `T-60`, `T-76`, `T-149`, `T-157` —
 * could not have obeyed the rule by any edit at their own emission sites. The two constants are
 * asserted equal to the tree's in `ActuatorResultRoundingTest`, which is what makes the
 * delegation a refactoring rather than a precision change.
 */
fun JsonElement.roundedForActuatorResult(): JsonElement = roundedForResult(
    digits = ACTUATOR_RESULT_SIGNIFICANT_DIGITS,
    digitsByKey = DEPARTURE_DIGITS_BY_KEY,
    floor = ACTUATOR_RESULT_ABSOLUTE_FLOOR
)
