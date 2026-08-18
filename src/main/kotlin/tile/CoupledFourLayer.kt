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

package com.xemantic.nano.plentyofroom.tile

/**
 * `T-197` — the coupling budget a four-layer tile must spend, and how it may be distributed.
 *
 * ## Why the uncoupled tile is a reference and never a design
 *
 * `C-0109` reports every coupled four-layer cell as worse than the **uncoupled** tile, and that
 * comparison decides nothing on its own, because §3 requires the actuator to deliver 100 pN to a
 * load: `C-0017`'s mandate is an **equality on the SUM** of the coupling stiffnesses. The total is
 * therefore fixed and non-zero **by specification**, and what a design may choose is only how to
 * distribute it. That is why [equalShareOfMandate] refuses a zero total rather than quietly
 * returning the uncoupled tile — the uncoupled tile is not in the family being searched.
 */

/**
 * `C-0017`'s mandated coupling total, `pN/nm`.
 *
 * §3's **acceptable** clause: 100 pN over a 3 nm stroke. `CLAUDE.md` records that reading this at
 * the *desired* clause instead gives 10 pN/nm and a different device, so the clause is named here
 * rather than assumed.
 */
const val MANDATED_TOTAL_STIFFNESS: Double = 100.0 / 3.0

/**
 * The mandate shared equally over [paths] attachments.
 *
 * Refuses a zero total, and refuses zero paths: `C-0017`'s mandate is an equality, so a coupling
 * that spends none of it is not a member of the family.
 */
fun equalShareOfMandate(paths: Int, total: Double = MANDATED_TOTAL_STIFFNESS): List<Double> {
    require(paths > 0) { "paths must be positive, was: $paths" }
    require(total > 0.0) {
        "the mandate is an EQUALITY on the sum and cannot be met by no coupling: was $total"
    }
    return List(paths) { total / paths }
}

/**
 * The mandate distributed in proportion to [weights], summing to [total] exactly.
 *
 * A grading is a **redistribution of a fixed budget**, which is `C-0017`'s own reading of its
 * mandate and the axis `C-0058` opened: same total, different per-path shares. Ratios between
 * weights are preserved exactly.
 */
fun rimGradedShareOfMandate(
    weights: List<Double>,
    total: Double = MANDATED_TOTAL_STIFFNESS
): List<Double> {
    require(weights.isNotEmpty()) { "weights must not be empty" }
    require(total > 0.0) {
        "the mandate is an EQUALITY on the sum and cannot be met by no coupling: was $total"
    }
    require(weights.all { it > 0.0 }) {
        "every weight must be positive — a zero weight is a path that is not built, which " +
                "changes the path COUNT rather than the distribution: were $weights"
    }
    val sum = weights.sum()
    return weights.map { total * it / sum }
}
