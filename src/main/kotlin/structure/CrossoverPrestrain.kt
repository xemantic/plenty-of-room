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

package com.xemantic.nano.plentyofroom.structure

import kotlin.math.PI
import kotlin.math.abs

/**
 * `T-172` — Rothemund's *"crossovers in tension"* as a term `C-0009`'s lattice can carry.
 *
 * ## Why this is not [CrossoverSoftening]
 *
 * `C-0099` swept the row-end crossover's **elastic constants** and found the flatness verdict
 * insensitive to all of them; it says so in its own validity range: *"the decomposition is linear
 * and carries no prestrain"*. A prestrain is a different object. Rothemund (2006, Suppl. Note S2,
 * read directly, `gpd/data/T-151-sources/`) writes, of a seam or an edge:
 *
 * > *"because DNA has a major and minor groove, a crossover involving staple strands is in tension
 * > with an adjacent crossover involving the scaffold strand … How the strain is actually relieved
 * > is unknown, the final base pairs of each helix may be distorted."*
 *
 * That describes a **static configuration** the structure is built into, not a compliance — an
 * *initial stress*, in the continuum-mechanics sense, and it is the one route `C-0099` left open.
 *
 * ## What it is, mechanically
 *
 * The strain is **angular**: Rothemund's own design program scores it as *"the sum of the squared
 * angular deviation"* over the strands passing through a crossover, and the mismatch it measures
 * is between the twist the lattice imposes and the twist the duplex prefers. In `C-0009`'s lattice
 * the matching coordinate already exists — the crossover's dihedral spring resists the **relative
 * roll** of the two duplexes it joins — so a prestrain is a preferred relative roll `θ₀`:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`U = ½ k_θ (Δφ − θ₀)²` instead of `½ k_θ Δφ²`.
 *
 * ## The consequence that makes the whole question cheap
 *
 * Expanding, `U = ½ k_θ Δφ² − k_θ θ₀ Δφ + ½ k_θ θ₀²`. **The quadratic term is untouched**, so a
 * prestrain changes no entry of the stiffness matrix; it contributes a fixed couple `± k_θ θ₀` to
 * the two roll degrees of freedom, i.e. a **load vector**. Three things follow before any solve:
 *
 * 1. the host's factorisation is unchanged, so `C-0058`'s influence bank and every placement
 *    surrogate built on it remain exact;
 * 2. the deflection field is **linear** in `θ₀`, so one unit-prestrain solve gives the whole
 *    axis; and
 * 3. peak dishing being an absolute value, the triangle inequality gives a rigorous ceiling,
 *    `D(θ₀) ≤ D_load + |θ₀| D_unit`, for the price of that one solve.
 *
 * ## What a uniform prestrain does, which is NOT what a uniform load does
 *
 * `CLAUDE.md`'s standing falsifier — *"a uniform load on a uniform Winkler foundation produces no
 * dishing at all"* — does **not** transfer. A uniform load is equilibrated by a rigid translation;
 * a uniform prestrain is an **eigenstrain**, and the state that relaxes every hinge and every
 * vertical link at once is a cylinder of curvature `θ₀/d`, not a translation. A tile under a
 * uniform prestrain curls, and `T-172`'s gate 3 measures the sagitta rather than asserting a zero.
 *
 * Conventions: angles **rad** (degrees only where a source quotes them), couples **pN·nm**,
 * `k_θ` **pN·nm/rad**; `x` along the helices, `y` across them, `w` positive **downward**.
 */

/** The couple in `pN·nm` a crossover of dihedral spring [hinge] carries at a prestrain [angle]. */
fun hingePrestrainCouple(angle: Double, hinge: Double): Double {
    require(angle.isFinite()) { "angle must be finite, was: $angle" }
    require(hinge >= 0.0 && hinge.isFinite()) {
        "hinge must be a non-negative finite stiffness, was: $hinge"
    }
    return hinge * angle
}

/**
 * The residual roll in **radians** that [basePairs] of helix accumulate between a lattice built at
 * [designTwistPerBase] degrees per base and a duplex that prefers [naturalTwistPerBase].
 *
 * `CLAUDE.md`, from `C-0015`: *"the register departure from a design twist is LINEAR in the
 * base-pair offset"* — 4.286° at the 8 bp out-of-plane site and 8.571° at 16 bp, on the square
 * lattice's `32/3` bp per turn against the preferred 10.5. This is that statement as a function,
 * so the ladder of candidate prestrains is derived rather than transcribed.
 */
fun registerPrestrain(
    basePairs: Double,
    designTwistPerBase: Double,
    naturalTwistPerBase: Double
): Double {
    require(basePairs >= 0.0) { "basePairs must not be negative, was: $basePairs" }
    require(designTwistPerBase > 0.0) {
        "designTwistPerBase must be positive, was: $designTwistPerBase"
    }
    require(naturalTwistPerBase > 0.0) {
        "naturalTwistPerBase must be positive, was: $naturalTwistPerBase"
    }
    return abs(basePairs * (naturalTwistPerBase - designTwistPerBase)) * PI / 180.0
}

/** [sites] all built at the same prestrain [angle] — the map an [OrigamiGrillage] takes. */
fun prestrainMap(
    sites: List<CrossoverSite>,
    angle: Double
): Map<CrossoverSite, Double> {
    require(angle.isFinite()) { "angle must be finite, was: $angle" }
    return sites.associateWith { angle }
}
