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

import com.xemantic.nano.plentyofroom.coupling.DishingSolution
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.influenceSurrogate
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure

/**
 * `T-263` — `C-0058`'s coupling surrogate over `C-0154`'s **honeycomb grillage**.
 *
 * ## Why this is fifteen lines and not a model
 *
 * Every coupled cell in this repository — `C-0058`, `C-0063`, `C-0087`, `C-0089`, `C-0118`,
 * `C-0142`, `C-0146`, `C-0151` — is graded on an `OrigamiGrillage` over a **smeared equivalent
 * sheet**, and `C-0154` has since measured what that sheet gets wrong on a honeycomb block:
 * `OrigamiSheet.acrossHelixRigidity = layers × k_θ d / p` is **`24/7 = 3.42857×` overstated**,
 * because only half the in-plane adjacent pairs are bonded and an interlayer bond carries half
 * the lever arm — while the *same* function reproduces `D_∥` at `2.8e−15`. One layer of the block
 * is not a sheet but a set of **dimers**, so the across-helix load path necessarily traverses the
 * thickness, and no single-layer sheet has a parameter that can see it.
 *
 * What makes the correction a **port** rather than a rewrite is that
 * [com.xemantic.nano.plentyofroom.coupling.influenceSurrogate] was already written against the
 * model-agnostic [DishingSolution] interface: `latticeInfluenceSurrogate` and
 * `plateInfluenceSurrogate` are its two existing adapters and this is the third. Nothing any of
 * those claims published can move, because no source they run through is edited — the only new
 * thing on the lattice side is [HoneycombGrillage.pointLoadDual].
 *
 * ## Conventions
 *
 * A station is `(s, y)` in nm from the face centre — `s` **along** the helices, `y` **across**
 * them — which is the `(x, y)` every `attachmentGrid`, `honeycombSnappedGrid` and
 * `twoLengthSnappedGrid` in this repository emits. Deflections are positive **downward**.
 */
fun honeycombInfluenceSurrogate(
    lattice: HoneycombGrillage,
    grid: List<Pair<Double, Double>>,
    pressure: PressureField,
    samples: Int = 81
): InfluenceSurrogate {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    val free = lattice.solve(pressure).asHoneycombDishingSolution()
    val influence = grid.map { (s, y) ->
        lattice.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0)))
            .asHoneycombDishingSolution()
    }
    return influenceSurrogate(
        grid, lattice.lengthS / 2.0, lattice.lengthY / 2.0, samples, free, influence
    )
}

/**
 * The stroke in nm a **free** block travels under a uniform [pressure].
 *
 * It is `p/k_f` identically — a free body on a uniform Winkler foundation translates rigidly,
 * whatever its rigidities — which is why the honeycomb re-grade of a smeared cell is a
 * **controlled** comparison and not merely a similar one: the normalising stroke every dishing
 * verdict is divided by cannot move between the two models. That identity is asserted rather
 * than assumed.
 */
fun honeycombFreeStroke(lattice: HoneycombGrillage, pressure: Double): Double =
    lattice.solve(uniformPressure(pressure)).meanDeflection

private fun HoneycombDeflection.asHoneycombDishingSolution(): DishingSolution =
    object : DishingSolution {
        override fun deflectionAt(x: Double, y: Double) = deflection(x, y)
        override fun dishingAt(x: Double, y: Double) = dishing(x, y)
    }
