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

import kotlin.math.floor

/**
 * Where a per-load-path force sits relative to the structural-survival bands
 * §4(f) of the problem definition names for a jointed DNA-origami structure.
 *
 * The bands are **cited**, not derived here, and they are loading-rate dependent —
 * a rupture force is not a material constant. They are used as reporting thresholds,
 * which is exactly what the `T-5` acceptance predicate asks for.
 */
enum class StructuralBand {

    /** Below 10 pN — under the reversible-isomerisation band. */
    BELOW_ISOMERISATION,

    /** 10–35 pN — the reversible-isomerisation band. */
    REVERSIBLE_ISOMERISATION,

    /** 35–60 pN — the irreversible-disassembly band. */
    IRREVERSIBLE_DISASSEMBLY,

    /** Above 60 pN — above every reported band, and above the B-DNA overstretching plateau. */
    ABOVE_DISASSEMBLY
}

/** The lower edge of the reversible-isomerisation band, in pN — §4(f), cited. */
const val ISOMERISATION_THRESHOLD: Double = 10.0

/** The lower edge of the irreversible-disassembly band, in pN — §4(f), cited. */
const val DISASSEMBLY_THRESHOLD: Double = 35.0

/** The upper edge of the irreversible-disassembly band, in pN — §4(f), cited. */
const val DISASSEMBLY_CEILING: Double = 60.0

/** Returns the band [force] pN falls into. */
fun structuralBand(force: Double): StructuralBand = when {
    force < ISOMERISATION_THRESHOLD -> StructuralBand.BELOW_ISOMERISATION
    force < DISASSEMBLY_THRESHOLD -> StructuralBand.REVERSIBLE_ISOMERISATION
    force <= DISASSEMBLY_CEILING -> StructuralBand.IRREVERSIBLE_DISASSEMBLY
    else -> StructuralBand.ABOVE_DISASSEMBLY
}

/**
 * Returns the smallest number of equally loaded parallel load paths that keeps the
 * per-path force from [totalForce] pN **strictly** below [limit] pN.
 *
 * The interesting number of `T-5`: not the peak force at some assumed attachment count,
 * but the attachment count the force target *demands*. Strict, so a limit that divides the
 * load exactly still costs one more path — a design sitting exactly on a rupture threshold
 * is not a design.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun minimumLoadPaths(totalForce: Double, limit: Double): Int {
    require(totalForce > 0.0) { "totalForce must be positive, was: $totalForce" }
    require(limit > 0.0) { "limit must be positive, was: $limit" }
    return floor(totalForce / limit).toInt() + 1
}

/** One load path class of the tile, its population, and the force each member carries. */
data class LoadPath(
    val name: String,
    val description: String,
    val paths: Double,
    val totalForce: Double
) {

    init {
        require(paths > 0.0) { "paths must be positive, was: $paths" }
    }

    /** The force in pN carried by one member of this path class. */
    val forcePerPath: Double get() = totalForce / paths

    /** The band [forcePerPath] falls into. */
    val band: StructuralBand get() = structuralBand(forcePerPath)

}
