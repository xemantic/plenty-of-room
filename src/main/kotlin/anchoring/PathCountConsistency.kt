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

package com.xemantic.nano.plentyofroom.anchoring

/**
 * `T-138` — `C-0017`'s mandate is a stiffness on a **sum**, so a path count sizes the element *and*
 * counts the instances; `C-0069`'s Deliverable 5 changes the first while holding the second at 34.
 *
 * ## The three counts, which the published table conflates into two
 *
 * | symbol | meaning |
 * |---|---|
 * | `n` | the path count the element is **sized** at, setting the per-path secant `k_total/n` |
 * | `N_demanded` | how many instances the array asks the lattice for |
 * | `N_placed` | how many the hard-body plan model actually admits |
 *
 * The load is carried by what is **built**, so the delivered total is `N_placed · k_total/n` and
 * `C-0017`'s equality holds only where `N_placed = n`. That makes [mandateRatio] a **column** of
 * any sensitivity table on the path count, and `C-0069` has it in neither of its two readings:
 * its 15-path row places 34 instances of a 15-path arm (**2.27×** the mandate) and its 45-path row
 * places 24 of a 45-path arm (**0.53×**).
 *
 * Nothing here is a model. It is one division, it runs before any solve, and it is what says
 * whether the defect is a presentation error or a physical one.
 */

/**
 * The total coupling stiffness in pN/nm an array actually delivers: [placedInstances] elements each
 * sized at the per-path secant `mandate/pathCount`.
 *
 * @throws IllegalArgumentException on a non-positive mandate or path count, or a negative count.
 */
fun deliveredTotalStiffness(mandate: Double, pathCount: Int, placedInstances: Int): Double {
    require(mandate > 0.0) { "mandate must be positive, was: $mandate" }
    require(pathCount >= 1) { "pathCount must be at least one, was: $pathCount" }
    require(placedInstances >= 0) {
        "placedInstances must not be negative, was: $placedInstances"
    }
    return placedInstances * mandate / pathCount
}

/**
 * `k_delivered / k_mandate = N_placed / n` — dimensionless, independent of the mandate, and exactly
 * `1` when the array places as many elements as the element was sized for.
 */
fun mandateRatio(pathCount: Int, placedInstances: Int): Double {
    require(pathCount >= 1) { "pathCount must be at least one, was: $pathCount" }
    require(placedInstances >= 0) {
        "placedInstances must not be negative, was: $placedInstances"
    }
    return placedInstances.toDouble() / pathCount.toDouble()
}
