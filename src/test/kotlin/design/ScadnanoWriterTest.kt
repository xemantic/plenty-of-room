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

package com.xemantic.nano.plentyofroom.design

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The other half of the interchange boundary: a **writer**.
 *
 * `ScadnanoDesignTest` proves the reader against the `.sc` file `C-0157`'s oxDNA run actually
 * simulated. This file proves the inverse map, and the gate that makes it an inverse rather than a
 * plausible second implementation is the **round trip**: read the committed fixture, write it, read
 * what was written, and require every lattice fact the reader derives to come back at departure
 * `0.0`. Base-pair counts and helix indices are integers; no tolerance is admissible on any of them.
 *
 * A writer that were lossy would not fail loudly — it would emit a design somebody could open, fold
 * and measure, whose counts were not the counts this corpus graded. That is why the round trip is
 * asserted on the *derived* facts and on the *bytes*, rather than on either alone.
 */
class ScadnanoWriterTest {

    private val original = ScadnanoDesign.fromResource("/gen1-tile.sc")
    private val written = original.toScadnanoText()
    private val reread = ScadnanoDesign.fromText(written)

    // --- W1: the round trip reproduces every lattice fact the reader derives, exactly ------------

    @Test
    fun `the round trip reproduces the raster, exactly`() {
        assert(reread.grid == original.grid)
        assert(reread.helixCount == original.helixCount)
        assert(reread.rowBasePairs() == original.rowBasePairs())
        assert(reread.strands.size == original.strands.size)
        assert(reread.staples().size == original.staples().size)
    }

    @Test
    fun `the round trip reproduces every domain of every strand, exactly`() {
        // integers, so equality is the predicate and no tolerance is admissible
        assert(reread.strands.map { it.domains } == original.strands.map { it.domains })
        assert(reread.strands.map { it.isScaffold } == original.strands.map { it.isScaffold })
    }

    @Test
    fun `the round trip reproduces the crossover census, exactly`() {
        assert(reread.crossoverCount() == original.crossoverCount())
        assert(reread.crossoverColumns() == original.crossoverColumns())
        assert(reread.crossoverPhase() == original.crossoverPhase())
        assert(reread.crossoversPerInterface() == original.crossoversPerInterface())
        assert(reread.scaffoldTurns().size == original.scaffoldTurns().size)
        assert(reread.crossovers() == original.crossovers())
    }

    @Test
    fun `the round trip reproduces the corpus's own counts for the Gen-1 tile`() {
        // the same numbers ScadnanoDesignTest derives from the file C-0157 simulated
        assert(reread.helixCount == 15)
        assert(reread.rowBasePairs() == 112)
        assert(reread.crossoverPhase() == 8)
        assert(reread.crossoverColumns().size == 7)
        assert(reread.crossoverCount() == 49)
        assert(reread.scaffoldTurns().size == 14)
        assert(reread.crossoversPerInterface().count { it == 4 } == 7)
        assert(reread.crossoversPerInterface().count { it == 3 } == 7)
        assert(reread.edgeAlongHelicesNm().isCloseTo(38.08))
        assert(reread.accumulatedRegisterDepartureDegrees().isCloseTo(-60.0))
    }

    // --- W2: what was written is a scadnano file, in scadnano's own field names ------------------

    @Test
    fun `the written text carries scadnano's own keys, not this repository's`() {
        listOf(
            "\"version\"", "\"grid\"", "\"helices\"", "\"strands\"",
            "\"grid_position\"", "\"is_scaffold\"", "\"forward\"", "\"start\"", "\"end\"", "\"domains\""
        ).forEach { key -> assert(key in written) }
    }

    @Test
    fun `the written design keeps the helix grid positions it was read with`() {
        assert(reread.helices == original.helices)
        assert(reread.helices.size == 15)
        assert(reread.helices.map { it.gridPosition } == (0..14).map { listOf(0, it) })
    }

    @Test
    fun `the written design keeps the geometry it was read with`() {
        val geometry = requireNotNull(reread.geometry)
        assert(geometry.risePerBasePair!!.isCloseTo(0.34))
        // caDNAno's SQUARE-lattice design twist, 32 bp = 3 turns -- deliberately NOT B-DNA's 10.5,
        // which is what makes the imported raster carry C-0086's -60 degrees of register error
        assert(geometry.basesPerTurn!!.isCloseTo(10.67))
        assert(geometry.interHelixGap!!.isCloseTo(0.69))
    }

    // --- W3: writing is idempotent, so the file is a fixed point and diffable -------------------

    @Test
    fun `writing what was read back is byte-identical, so the map is a fixed point`() {
        assert(reread.toScadnanoText() == written)
    }

    // --- W4: a design that cannot be drawn is REFUSED, never guessed ---------------------------

    @Test
    fun `a design with no helix positions is refused rather than laid out on a guess`() {
        // the reader refuses to guess a lattice from an unknown grid; the writer must equally
        // refuse to guess a grid POSITION, because a guessed one silently lays a honeycomb
        // design out on a square grid
        val failure = assertFailsWith<IllegalArgumentException> {
            ScadnanoDesign(grid = "square", helixCount = 2, strands = original.strands)
                .toScadnanoText()
        }
        assert("grid position" in failure.message!!)
    }

    @Test
    fun `a design whose helix count and helix records disagree is refused`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            ScadnanoDesign(
                grid = "square",
                helixCount = 15,
                strands = original.strands,
                helices = original.helices.take(3)
            ).toScadnanoText()
        }
        assert("3" in failure.message!!)
    }

    // --- W5: a design this repository emits satisfies the rules this repository checks ----------

    @Test
    fun `the written design passes this repository's own buildability rules`() {
        val report = reread.checkBuildability()
        assert(report.violations.isEmpty())
        assert(report.seamlessRowWidthIsAdmissible)
        assert(report.everyCrossoverJoinsAdjacentDuplexes)
        assert(report.noSiteIsCrossedTwice)
        assert(!report.carriesInsertionsOrDeletions)
    }
}
