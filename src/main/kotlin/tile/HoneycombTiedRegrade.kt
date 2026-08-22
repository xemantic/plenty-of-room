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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure

/**
 * `T-279` — `C-0167`'s coupled cells over the **tied** honeycomb lattice.
 *
 * ## What the two functions here are for
 *
 * `CH-0227` established that `HoneycombGrillage.bonds` is the **staple** crossover ladder and
 * that a Rothemund-style raster also **turns**, `H − 1 = 59` times on a 60-helix block, each turn
 * a scaffold crossover with zero unpaired nucleotides and therefore a covalent tie at `s = ±L/2`,
 * past the last plane of the ladder. The split is `435 + 59`, which is `C-0099`'s square-lattice
 * `56 = 42 + 14` read on the honeycomb.
 *
 * [honeycombTiedLattice] is the one constructor argument that difference costs, in the shape a
 * paired study needs: **one** function returning either state of one object, so that nothing but
 * the tie set can differ between the two halves of a comparison.
 *
 * [honeycombTiedSurrogate] is `C-0167`'s [honeycombInfluenceSurrogate] with `C-0104`'s rule made
 * structural rather than remembered: **the free field is taken on the lattice as built and every
 * influence function on `withoutPrestrain`.** A raster turn is the only element of this block
 * that carries a built-in prestrain (`CH-0228`: every *allowed* honeycomb scaffold crossover sits
 * `8.57142857°` off the line of centres), and a prestrain is a **load** — so an influence taken
 * on the prestrained lattice is that influence *plus* the prestrain's own response, the Woodbury
 * matrix stops being a compliance, and `C-0104` records that it then fails **silently** at
 * exactly the departures that matter. Where there is no prestrain `withoutPrestrain` returns the
 * same object, so this function is [honeycombInfluenceSurrogate] bit for bit — which is asserted
 * as a test rather than argued.
 */

/**
 * The recommended block's grillage in either tie state.
 *
 * @param tied whether the raster's own `H − 1` turns are assembled as ties. `false` is exactly
 *   the object `C-0154` and `C-0167` measured — asserted bit-identical, not claimed.
 * @param prestrainRadians the relative roll every tie is built at, zero unless a load is asked
 *   for. It is ignored when [tied] is `false`, because an untied lattice has nowhere to put it.
 */
fun honeycombTiedLattice(
    block: HoneycombBlock,
    rowBasePairs: Int,
    enhancement: Double,
    tied: Boolean,
    prestrainRadians: Double = 0.0,
    subdivisions: Int = 1,
    firstAxialSign: Int = 1
): HoneycombGrillage {
    require(prestrainRadians.isFinite()) {
        "prestrainRadians must be finite, was: $prestrainRadians"
    }
    fun build(ties: List<HoneycombScaffoldTurnTie>) = HoneycombGrillage(
        block = block,
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions,
        scaffoldTurnTies = ties
    )
    val bare = build(emptyList())
    return if (!tied) bare else build(
        honeycombScaffoldTurnTies(block, bare.nodesPerBeam, firstAxialSign, prestrainRadians)
    )
}

/**
 * `C-0058`'s exact Woodbury coupling surrogate over [lattice], with the influence bank taken on
 * `withoutPrestrain`.
 *
 * A station is `(s, y)` in nm from the face centre — `s` **along** the helices, `y` **across**
 * them — and deflections are positive **downward**, which is `C-0167`'s convention unchanged.
 */
fun honeycombTiedSurrogate(
    lattice: HoneycombGrillage,
    grid: List<Pair<Double, Double>>,
    pressure: PressureField,
    samples: Int = 81
): InfluenceSurrogate {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    val structure = lattice.withoutPrestrain
    val free = lattice.solve(pressure).asDishingSolution()
    val influence = grid.map { (s, y) ->
        structure.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0))).asDishingSolution()
    }
    return influenceSurrogate(
        grid, lattice.lengthS / 2.0, lattice.lengthY / 2.0, samples, free, influence
    )
}

private fun HoneycombDeflection.asDishingSolution(): DishingSolution =
    object : DishingSolution {
        override fun deflectionAt(x: Double, y: Double) = deflection(x, y)
        override fun dishingAt(x: Double, y: Double) = dishing(x, y)
    }
