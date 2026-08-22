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

package com.xemantic.nano.plentyofroom.lattice

import kotlinx.serialization.Serializable

/**
 * Which crossover lattice a result is **on**, as a tag a query can read.
 *
 * `T-272`'s `P3`, and [ARCHITECTURE.md](../../../../../../../ARCHITECTURE.md)'s step 6: *"the
 * honeycomb correction of iterations 33–34 would have been a query rather than an audit"*. That
 * correction had to establish, claim by claim and by hand, that every placement, phase, plan
 * ceiling and flatness number in this corpus is a **single-layer square-lattice** result and does
 * not transfer to a honeycomb face.
 *
 * ## Why it is a declaration and not a derivation
 *
 * It was measured before it was written. A regex over each emitting study's own source —
 * `Gen1Tile|CrossoverLayout|OrigamiGrillage|OrigamiSheet` against `[Hh]oneycomb` — classifies
 * **66 square, 6 honeycomb, 26 none and 29 both** over the 127 studies that write a committed
 * result file. The 29 are not a lattice fact: a honeycomb study imports a square-lattice constant
 * to compare against, and a square-lattice study names the honeycomb in a `findings` sentence. So
 * a derived tag would be **23 % noise**, which is worse than no tag, because a wrong lattice tag
 * is exactly the error the tag exists to prevent.
 *
 * ## Why four states and not a boolean
 *
 * [BOTH] and [NONE] are different answers and neither is a default:
 *
 *  * [NONE] is a **claim** — no crossover lattice enters the result at all, which is true of every
 *    `brush/`, `material/` and `poroelastic/` study and of the 1-D electrostatics. `Regime` makes
 *    the same move with a `null` buffer, and for the same reason: an omission and a statement of
 *    absence read alike in JSON and are not the same fact.
 *  * [BOTH] is a comparison — one file carrying square-lattice numbers and honeycomb ones. The
 *    query *"which results are single-layer square-lattice"* must **admit** it, which is what
 *    [includes] says and what a boolean could not.
 */
@Serializable
enum class LatticeTag(

    /** The word a result file carries and a query asks for. */
    val tag: String
) {

    /** Four azimuths at 8 bp, 32 bp per interface — `SquareCrossoverLattice`. */
    SQUARE("square"),

    /** Three azimuths at 7 bp, 21 bp per interface — `HoneycombCrossoverLattice`. */
    HONEYCOMB("honeycomb"),

    /** Both, in one file: a comparison, a re-grade, or a rule run against every lattice. */
    BOTH("both"),

    /** Neither: no crossover lattice enters this result, and that is a statement. */
    NONE("none");

    /** Whether any crossover lattice enters this result at all. */
    val isOnACrossoverLattice: Boolean get() = this != NONE

    /** Whether a result carrying this tag contains numbers on [lattice]. */
    fun includes(lattice: CrossoverLattice): Boolean = when (this) {
        BOTH -> true
        NONE -> false
        else -> tag == lattice.name
    }

    companion object {

        /** The tag of a single lattice, taken from the lattice's own [CrossoverLattice.name]. */
        fun of(lattice: CrossoverLattice): LatticeTag = ofTag(lattice.name)

        /**
         * The tag a result file's word denotes.
         *
         * Refuses an unknown word rather than defaulting to [NONE]: a default here would make a
         * misspelling read as *"no lattice enters this result"*, which is the one answer that
         * silently exempts a file from the query the tag exists to serve.
         */
        fun ofTag(tag: String): LatticeTag = entries.firstOrNull { it.tag == tag }
            ?: throw IllegalArgumentException(
                "no lattice tag is spelled \"$tag\"; this project knows " +
                    entries.joinToString(", ") { it.tag }
            )
    }
}
