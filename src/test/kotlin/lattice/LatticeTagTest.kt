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

import com.xemantic.kotlin.test.assert
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-272`'s `P3`: which lattice a result is **on** is a tag, not an audit.
 *
 * The honeycomb correction of iterations 33–34 had to establish, claim by claim, that every
 * placement, phase, plan ceiling and flatness number in this corpus was a single-layer
 * square-lattice result. A grep cannot do it — measured on the tree this tag was written for,
 * a regex over each emitting study's source calls **29 of 127** studies *both*, because a
 * honeycomb study imports a square-lattice constant for comparison and a square-lattice study
 * names the honeycomb in a sentence. So the tag is a **declaration**, and the point of the enum
 * is that [LatticeTag.BOTH] and [LatticeTag.NONE] are different answers and neither is a default.
 */
class LatticeTagTest {

    // --- gate 1: the four states, and what each one asserts -------------------------------------

    @Test
    fun `every tag renders as the word a query would ask for`() {
        assert(LatticeTag.SQUARE.tag == "square")
        assert(LatticeTag.HONEYCOMB.tag == "honeycomb")
        assert(LatticeTag.BOTH.tag == "both")
        assert(LatticeTag.NONE.tag == "none")
    }

    @Test
    fun `the tag of a lattice is the lattice's own name`() {
        assert(LatticeTag.of(SquareCrossoverLattice).tag == SquareCrossoverLattice.name)
        assert(LatticeTag.of(HoneycombCrossoverLattice).tag == HoneycombCrossoverLattice.name)
    }

    @Test
    fun `every lattice this project knows has a tag`() {
        crossoverLattices.forEach { LatticeTag.of(it) }
    }

    // --- gate 2: `none` is a claim and `both` is a different one --------------------------------

    @Test
    fun `only NONE says no crossover lattice enters the result`() {
        assert(!LatticeTag.NONE.isOnACrossoverLattice)
        assert(LatticeTag.SQUARE.isOnACrossoverLattice)
        assert(LatticeTag.HONEYCOMB.isOnACrossoverLattice)
        assert(LatticeTag.BOTH.isOnACrossoverLattice)
    }

    /**
     * The query the tag exists for: *"which results are single-layer square-lattice"*.
     *
     * [LatticeTag.BOTH] answers **yes** to it and [LatticeTag.HONEYCOMB] answers no, which is why
     * a single boolean would have been the wrong shape — a comparison study carries square-lattice
     * numbers and honeycomb ones in one file.
     */
    @Test
    fun `a square-lattice query admits BOTH and refuses HONEYCOMB`() {
        assert(LatticeTag.SQUARE.includes(SquareCrossoverLattice))
        assert(LatticeTag.BOTH.includes(SquareCrossoverLattice))
        assert(!LatticeTag.HONEYCOMB.includes(SquareCrossoverLattice))
        assert(!LatticeTag.NONE.includes(SquareCrossoverLattice))
        assert(LatticeTag.BOTH.includes(HoneycombCrossoverLattice))
        assert(LatticeTag.HONEYCOMB.includes(HoneycombCrossoverLattice))
        assert(!LatticeTag.SQUARE.includes(HoneycombCrossoverLattice))
    }

    // --- gate 3: parsing back, because the artifact is what a query reads -----------------------

    @Test
    fun `a tag round-trips through the word a result file carries`() {
        LatticeTag.entries.forEach { assert(LatticeTag.ofTag(it.tag) == it) }
    }

    @Test
    fun `an unknown word is refused rather than defaulted`() {
        val failure = assertFailsWith<IllegalArgumentException> { LatticeTag.ofTag("cubic") }
        assert(failure.message!!.contains("cubic"))
    }
}
