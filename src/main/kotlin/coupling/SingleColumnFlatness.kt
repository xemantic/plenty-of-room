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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.math.pow

/**
 * `T-101` — the two things `C-0026`'s pipeline does not already have, so that the flatness of
 * `C-0041`'s fifteen-attachment scheme can be evaluated **without writing a second lattice**.
 *
 * ## What is here and what deliberately is not
 *
 * `C-0026` already reads `C-0022`'s solved edge profile, already assembles `C-0017`'s mandate as
 * `n` equal springs, and already runs `C-0006`'s plate beside `C-0009`'s grillage. Reimplementing
 * any of that would produce a third lattice whose numbers are not comparable with `CH-0034`'s
 * table, which is the table this task exists to extend downward. So exactly two things are added:
 *
 * 1. [staggeredAttachmentGrid] — `C-0041`'s connectivity remedy as a placement. `C-0041` finds
 *    that a collinear tie column severs the superstructure into two components and prescribes a
 *    stagger of **8 bp = 2.72 nm**, alternating `±1.36 nm` row to row, checked there *"for
 *    connectivity and for fit, not for flatness"*. This is what lets flatness be checked too.
 * 2. [winklerBendingLength] — the cheap bound, four arithmetic operations against a
 *    1665-degree-of-freedom Cholesky factorisation, which settles the *structure* of the answer
 *    before the expensive part runs.
 */

// ---------------------------------------------------------------------------- the placement

/**
 * The attachment positions in nm of a [columns] × [rows] grid on a [edgeX] × [edgeY] tile, with
 * successive rows displaced **along the helices** by `±[stagger]/2` in alternation.
 *
 * `stagger = 0.0` returns [attachmentGrid] identically, which gate 2 asserts point by point.
 *
 * ## Why the displacement is along `x` and alternates row by row
 *
 * `C-0026` fixes the attachment **rows** — one per duplex — and says nothing about where *along*
 * a row an attachment sits, so a displacement along `x` is free of every upstream claim while a
 * displacement along `y` would leave the one-row-per-duplex scheme and its exact zero. The
 * alternation is what `C-0041`'s union-find needs: a **collinear** column of tie apertures removes
 * a whole line of material from the superstructure, because the attachment grid's across-helix
 * pitch is exactly one duplex.
 *
 * @param stagger the **peak-to-peak** displacement in nm, i.e. `+stagger/2` on even rows and
 *          `−stagger/2` on odd ones. `C-0041`'s remedy is `stagger = 2.72 nm`.
 */
fun staggeredAttachmentGrid(
    columns: Int,
    rows: Int,
    edgeX: Double,
    edgeY: Double,
    stagger: Double = 0.0
): List<Pair<Double, Double>> {
    require(stagger >= 0.0) { "stagger must not be negative, was: $stagger" }
    val base = attachmentGrid(columns, rows, edgeX, edgeY)
    if (stagger == 0.0) return base
    val staggered = base.mapIndexed { index, (x, y) ->
        val row = index / columns
        (x + if (row % 2 == 0) stagger / 2.0 else -stagger / 2.0) to y
    }
    require(staggered.all { abs(it.first) <= edgeX / 2.0 }) {
        "a stagger of $stagger nm puts an attachment outside the ${edgeX} nm tile"
    }
    return staggered
}

/**
 * A stagger of [basePairs] base pairs in nm, at the B-DNA rise.
 *
 * A DNA design cannot place an attachment between base pairs, so a stagger is **quantised**, and
 * `C-0041`'s *"2.72 nm = 8 bp — one duplex pitch, quantised up to the rise"* is this function at
 * eight.
 */
fun staggerOfBasePairs(basePairs: Double): Double {
    require(basePairs >= 0.0) { "basePairs must not be negative, was: $basePairs" }
    return basePairs * Gen1Tile.RISE_PER_BASE_PAIR
}

// ---------------------------------------------------------------------------- the cheap bound

/**
 * The largest stagger in nm, peak to peak, that keeps a flexure of span [span] inside a body of
 * edge [edgeX] — `2(edgeX − span)/2`, i.e. `edgeX − span`.
 *
 * ## Why a stagger has a ceiling that an attachment does not
 *
 * A staggered *attachment* only has to stay on the tile, which admits `edgeX` of stagger. A
 * staggered **flexure** has to stay on the body, and a flexure is a beam of `C-0030`'s span
 * centred on its own midspan, which is where the tie — and therefore the attachment — sits
 * (`C-0035`'s `Su`, `C-0041`'s pinning condition). So the half-stagger is capped at
 * `edgeX/2 − span/2` and not at `edgeX/2`.
 *
 * At `C-0041`'s design — 21.44 nm of span on a 40 nm edge — that is **18.56 nm peak to peak, 54
 * base pairs**, and it is what stops the unconstrained flatness optimum of the stagger from being
 * a design.
 */
fun maximumStaggerForSpan(edgeX: Double, span: Double): Double {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(span > 0.0) { "span must be positive, was: $span" }
    require(span <= edgeX) { "a span of $span nm does not fit a $edgeX nm edge" }
    return edgeX - span
}

/**
 * The bending length in nm of a beam of flexural rigidity [rigidityPerLength] on a Winkler
 * foundation of [foundationPerLength], `ℓ = (4 EI / k)^(1/4)`.
 *
 * ## What it is for
 *
 * `ℓ` is the decay length of the beam's own influence patch: a point support flattens the sheet
 * over roughly `±ℓ` and does essentially nothing beyond it, because the foundation — not the
 * neighbouring supports — carries the load out there. So an attachment column pitch measured in
 * units of `ℓ` says whether adding columns can still buy flatness *before* any matrix is
 * assembled.
 *
 * For the Gen-1 tile the two directions are 2.2× apart in `ℓ` and 15× apart in the pitch they are
 * compared against, which is why a single column of fifteen is dense across the helices and
 * hopeless along them. That asymmetry is the sheet's 25.6× rigidity anisotropy seen through a
 * fourth root, which is exactly why `C-0015` had to search grid **shapes** rather than counts.
 *
 * @param rigidityPerLength `EI` in `pN·nm²` for a beam, or the plate rigidity `D` in `pN·nm`
 *          times the width the foundation is taken over — the two enter identically.
 * @param foundationPerLength the foundation stiffness in `pN/nm` per nm of beam, i.e. the areal
 *          `k_f` in `pN/nm³` times the tributary width in nm.
 */
fun winklerBendingLength(
    rigidityPerLength: Double,
    foundationPerLength: Double
): Double {
    require(rigidityPerLength > 0.0) {
        "rigidityPerLength must be positive, was: $rigidityPerLength"
    }
    require(foundationPerLength > 0.0) {
        "foundationPerLength must be positive, was: $foundationPerLength"
    }
    return (4.0 * rigidityPerLength / foundationPerLength).pow(0.25)
}
