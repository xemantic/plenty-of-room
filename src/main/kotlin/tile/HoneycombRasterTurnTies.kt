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
 * `T-254` — the raster's own turns, as the mechanical ties they are.
 *
 * A honeycomb x-raster runs `+s` along one helix, turns at that helix's end, and runs `−s` along
 * the next. A turn carrying **zero** unpaired nucleotides *is* a scaffold crossover — a covalent
 * tie between two duplexes at their ends — so it sits at `s = ±L/2`, alternating ends along the
 * path, and it is an element the **staple** crossover lattice does not contain: `C-0154`'s bonds
 * are the 7 bp plane ladder, and a turn lies past the last of them.
 *
 * The census this file derives is a fact about **which** of a block's interfaces the scaffold
 * loads. Within a raster row the path steps through the block's **thickness** (the `column`
 * index), and at a row transition it steps **in plane** (the `rasterRow` index) — so on a
 * `10 × 6` block the 59 turns are **50** through-thickness ties, which is *every* interlayer
 * interface `C-0154` counts, and **9** in-plane ones, which is 9 of its 27.
 */

/** One raster turn: the tie between raster helix [index] and helix `index + 1`. */
data class HoneycombRasterTurn(

    /** The position of the turn along the raster path, `0` until `helices − 1`. */
    val index: Int,

    /** The lower of the two beam indices the turn joins, `beam = rasterRow · n + column`. */
    val lowerBeam: Int,

    /** The upper of the two. */
    val upperBeam: Int,

    /** `true` where the turn steps in **plane** — the honeycomb's vertical bond, a row transition. */
    val inPlane: Boolean,

    /** `true` where the turn sits at `s = +L/2`, the block's high axial rim. */
    val atHighEnd: Boolean
)

/**
 * The block's helices in the order an x-raster visits them.
 *
 * Row `r` is traversed `n − 1 … 0` when `r` is even and `0 … n − 1` when it is odd — which is
 * `honeycombXRasterPath`'s own construction read in the block's `(rasterRow, column)` coordinates
 * **through the column mirror `c = n − 1 − x`**, and the mirror is not optional. `HoneycombBlock`
 * puts its vertical bond up when `(rasterRow + column)` is even and `HoneycombCell` uses the
 * opposite parity (`CLAUDE.md`), so the naive identification `c = x` makes the raster's row
 * transition join two helices `2d` apart — a pair the lattice does not bond at all. The two
 * constructions are asserted equal under the mirror.
 */
fun honeycombRasterOrder(block: HoneycombBlock): List<HoneycombSite> =
    (0 until block.rasterRows).flatMap { r ->
        val order =
            if (r % 2 == 0) (block.helicesPerRow - 1 downTo 0) else (0 until block.helicesPerRow)
        order.map { HoneycombSite(r, it) }
    }

/**
 * Every turn of [block]'s x-raster.
 *
 * @param firstAxialSign the direction the scaffold traverses the **first** helix in, `+1` or `−1`.
 *   Helix `k` is traversed in `firstAxialSign · (−1)^k`, so turn `k` sits at the high rim exactly
 *   when that sign is positive — the alternation `C-0140`'s turn-sense machinery carries.
 */
fun honeycombRasterTurnList(
    block: HoneycombBlock,
    firstAxialSign: Int = 1
): List<HoneycombRasterTurn> {
    require(firstAxialSign == 1 || firstAxialSign == -1) {
        "firstAxialSign must be +1 or -1, was: $firstAxialSign"
    }
    val n = block.helicesPerRow
    val order = honeycombRasterOrder(block)
    return (0 until order.size - 1).map { k ->
        val here = order[k]
        val next = order[k + 1]
        val a = here.rasterRow * n + here.column
        val b = next.rasterRow * n + next.column
        HoneycombRasterTurn(
            index = k,
            lowerBeam = minOf(a, b),
            upperBeam = maxOf(a, b),
            inPlane = here.column == next.column,
            atHighEnd = firstAxialSign * (if (k % 2 == 0) 1 else -1) > 0
        )
    }
}

/**
 * The turns of [block] as ties on a lattice with [nodesPerBeam] nodes per beam: a turn at the low
 * rim sits at node `0` and one at the high rim at the last node, which is `s = ±L/2` exactly.
 *
 * @param prestrainRadians the relative roll every tie is built at. `C-0152` measures
 *   [allowedScaffoldCrossoverDepartureDegrees] at **every** allowed scaffold crossover and twice
 *   that at a forced one; the default here is zero, so that the ties enter as pure **stiffness**
 *   unless a load is asked for.
 */
fun honeycombScaffoldTurnTies(
    block: HoneycombBlock,
    nodesPerBeam: Int,
    firstAxialSign: Int = 1,
    prestrainRadians: Double = 0.0
): List<HoneycombScaffoldTurnTie> {
    require(nodesPerBeam >= 2) { "nodesPerBeam must be at least two, was: $nodesPerBeam" }
    return honeycombRasterTurnList(block, firstAxialSign).map {
        HoneycombScaffoldTurnTie(
            lowerBeam = it.lowerBeam,
            upperBeam = it.upperBeam,
            node = if (it.atHighEnd) nodesPerBeam - 1 else 0,
            prestrainRadians = prestrainRadians
        )
    }
}

/**
 * The azimuthal departure an **allowed** honeycomb scaffold crossover carries, in degrees.
 *
 * caDNAno writes its scaffold rule as *"five base pairs, or half a turn"*, and at 10.5 bp/turn the
 * exact half turn is **5.25** bp — so an allowed crossover sits a quarter of a base pair off the
 * line of centres, `(240/7)/4 = 8.5714286°` (`C-0152` §5, `CH-0197`). It is carried by **every**
 * scaffold crossover of **every** honeycomb origami ever folded, which is what makes it a
 * calibration; it is also a **prestrain**, on every raster turn of every raster, forced or not.
 */
fun allowedScaffoldCrossoverDepartureDegrees(): Double = 240.0 / 7.0 / 4.0
