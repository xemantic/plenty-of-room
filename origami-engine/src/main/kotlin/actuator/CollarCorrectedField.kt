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

/**
 * Task `T-60` — `C-0022`'s finite-tile collar carried into `C-0018`'s equilibrium path, as a
 * gap-dependent multiplier on the electrostatic force and nothing else.
 *
 * ## Why a multiplier and not a re-run
 *
 * `CH-0026`'s own instruction: *"carry a multiplier, not a re-run."* The edge enters the
 * actuator problem only through the footprint-integrated force — `C-0008`'s `F_es` and every
 * force downstream of it is a 1-D disjoining pressure times 1600 nm² — so the whole 2-D
 * correction is one number per gap. Solving the 2-D edge problem *inside* the fold search
 * would be ~1300 two-dimensional solves per state and would answer the same question.
 *
 * ## What the multiplier does NOT touch
 *
 * The applied bias. `C-0022` solves the Stern series in one dimension and imposes the
 * diffuse-layer drop laterally uniformly, so the electrode's compact layer is not re-solved
 * near the rim; the mapping from diffuse drop to applied bias is therefore the 1-D one, exactly
 * as it is upstream. That is a stated validity condition of `C-0022` and it is inherited here
 * rather than repaired.
 *
 * The consequence is the whole of `T-60`: because the bias is untouched and the *force* is
 * scaled, a **constant** multiplier moves the bias that holds a pinned load and moves nothing
 * else — `CH-0035`'s identity — while a gap-dependent one changes `d ln|F_es|/dh`, which is
 * the only channel into `k_es`.
 */

/**
 * Returns this field with its **force** multiplied by [multiplier] evaluated at the gap, and
 * every potential left exactly as it was.
 *
 * @param multiplier `μ(h)`, which must be strictly positive — a non-positive collar would
 *        reverse the sign of the force, which is not a correction but a different problem.
 */
fun DiffuseParametrisedField.withCollar(
    multiplier: (Double) -> Double
): DiffuseParametrisedField = DiffuseParametrisedField { gap, diffusePotential ->
    val scale = multiplier(gap)
    require(scale > 0.0) { "the collar multiplier must be positive, was: $scale at gap $gap nm" }
    val plain = sample(gap, diffusePotential)
    plain.copy(force = plain.force * scale)
}
