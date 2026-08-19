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

import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.json.JsonElement

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
fun roundCouplingResult(value: Double): Double = roundForResult(
    value, COUPLING_RESULT_SIGNIFICANT_DIGITS, COUPLING_RESULT_ABSOLUTE_FLOOR
)

/**
 * Rounds every `Double` in the tree, so nothing can be emitted unrounded by omission.
 *
 * **Delegated to `structure/`** by `T-214`. `CH-0154` measured that a rounding entry point with no
 * `digitsByKey` parameter cannot carry the departure rule *by any edit at its own emission sites*,
 * and named `actuator/`; the same shape stood here, so `T-16`, `T-17`, `T-101`, `T-113`, `T-122`
 * and `T-123` — six of that task's 31 residue files — were in exactly `T-60`'s position.
 *
 * The one observable this implementation had that `structure/`'s does not is that it coerces an
 * **integral** JSON number to a `Double`, so every committed `coupling/` result file renders a
 * count as `45.0`. That is a rendering convention frozen by those files rather than a precision
 * choice, and it is preserved through `roundIntegralNumbers` so that the delegation moves
 * departure fields and nothing else.
 */
fun JsonElement.roundedForCouplingResult(): JsonElement = roundedForResult(
    digits = COUPLING_RESULT_SIGNIFICANT_DIGITS,
    floor = COUPLING_RESULT_ABSOLUTE_FLOOR,
    roundIntegralNumbers = true
)
