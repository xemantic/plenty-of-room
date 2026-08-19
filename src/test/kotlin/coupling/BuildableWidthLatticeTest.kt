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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.BUILDABLE_RASTER_WIDTH
import com.xemantic.nano.plentyofroom.anchoring.endOfRowColumnPhases
import com.xemantic.nano.plentyofroom.anchoring.upwardRootLattice
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-188` — the count/phase grid at `C-0086`'s **buildable** 38.08 nm.
 *
 * This file is the **cheap bound**, and every one of its assertions runs before any solve.
 * `T-188`'s acceptance predicates `P1` and `P2` and its falsifier `F3` are discharged here in
 * arithmetic: the census of the two widths' lattices, and whether `CrossoverLayout.EDGE_MARGIN`'s
 * **value** moves the station set at either of them.
 *
 * The disciplines from `CLAUDE.md` that govern this file: a numerical guard's justification is a
 * statement about a **state** and expires when the geometry moves, so it is re-asserted at both
 * widths rather than inherited; and two lattices claimed identical are compared **absolutely**,
 * position by position, not by a count.
 */
class BuildableWidthLatticeTest {

    private val duplexes = 15

    private val nominal = Gen1Tile.EDGE_X

    private val buildable = BUILDABLE_RASTER_WIDTH

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    private val insets = listOf(CrossoverLayout.EDGE_MARGIN, rise / 2.0, rise)

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    // ------------------------------------------------------------------ gate 1: dimensions

    @Test
    fun `gate 1 - a signature carries positions in nm and counts as integers`() {
        val signature = upwardLatticeSignature(24, nominal, duplexes)
        assert(signature.columns == 8)
        assert(signature.upwardSites == 53)
        assert(signature.rows.size == duplexes)
        assert(signature.rows.sumOf { it.size } == 53)
        // every station is inside the footprint, in nm
        assert(signature.rows.all { row -> row.all { abs(it) < nominal / 2.0 } })
        assert(signature.columnPositions.size == 8)
    }

    @Test
    fun `gate 1 - a signature refuses a non-positive width, an empty sheet and a bad inset`() {
        assertFailsWith<IllegalArgumentException> {
            upwardLatticeSignature(0, -1.0, duplexes)
        }
        assertFailsWith<IllegalArgumentException> {
            upwardLatticeSignature(0, nominal, 1)
        }
        assertFailsWith<IllegalArgumentException> {
            upwardLatticeSignature(0, nominal, duplexes, inset = 0.0)
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - refusing the row end is CrossoverLayout's own truncation at both widths`() {
        (0 until 32).forEach { phase ->
            listOf(nominal, buildable).forEach { width ->
                val signature = upwardLatticeSignature(
                    phase, width, duplexes, admitRowEnd = false
                )
                val published = CrossoverLayout.atBasePairPhase(phase, sheet, width)
                assert(signature.columns == published.positions.size)
                signature.columnPositions.zip(published.positions).forEach { (a, b) ->
                    assert(abs(a - b) < 1e-12)
                }
                val lattice = upwardRootLattice(phase, width, duplexes)
                assert(signature.rows.size == lattice.size)
                signature.rows.zip(lattice).forEach { (mine, theirs) ->
                    assert(mine.size == theirs.size)
                    mine.zip(theirs).forEach { (a, b) -> assert(abs(a - b) < 1e-12) }
                }
            }
        }
    }

    @Test
    fun `gate 2 - at 40 nm no plane lands on the row end, so the two conventions coincide`() {
        assert(endOfRowColumnPhases(118).isEmpty())
        (0 until 32).forEach { phase ->
            assert(
                latticesAgree(
                    upwardLatticeSignature(phase, nominal, duplexes, admitRowEnd = false),
                    upwardLatticeSignature(phase, nominal, duplexes, admitRowEnd = true)
                )
            )
        }
    }

    // ------------------------------------------------------------------ gate 3: the congruences

    @Test
    fun `gate 3 - C-0102's congruence - the end plane is a COLUMN only at phases 8 and 24`() {
        assert(endOfRowColumnPhases(112) == listOf(8, 24))
        val eightColumn = (0 until 32).filter {
            upwardLatticeSignature(it, buildable, duplexes, admitRowEnd = true).columns == 8
        }
        assert(eightColumn == listOf(8, 24))
        // and refusing the row end takes those same two phases to SIX, not to seven
        assert(
            (0 until 32).filter {
                upwardLatticeSignature(it, buildable, duplexes, admitRowEnd = false).columns == 6
            } == listOf(8, 24)
        )
        assert(
            (0 until 32).none {
                upwardLatticeSignature(it, buildable, duplexes, admitRowEnd = false).columns == 8
            }
        )
    }

    @Test
    fun `gate 3 - the 2 x 2's own corners carry IDENTICAL stations at the two widths`() {
        listOf(8, 24).forEach { phase ->
            assert(
                latticesAgree(
                    upwardLatticeSignature(phase, nominal, duplexes, admitRowEnd = false),
                    upwardLatticeSignature(phase, buildable, duplexes, admitRowEnd = true),
                    columnsToo = false
                )
            )
        }
        // and the columns are NOT identical there, which is what makes the comparison matched
        // on stations and unmatched on hosts
        assert(
            !latticesAgree(
                upwardLatticeSignature(8, nominal, duplexes, admitRowEnd = false),
                upwardLatticeSignature(8, buildable, duplexes, admitRowEnd = true)
            )
        )
    }

    // ------------------------------------------------------------------ gate 4: the guard sweep

    @Test
    fun `gate 4 - F3 - the EDGE_MARGIN VALUE is exactly inert at the buildable width`() {
        val sweep = insetSensitivity(buildable, duplexes, admitRowEnd = false, insets = insets)
        assert(sweep.distinctSignatures == 1)
        assert(sweep.worstStationDisplacement == 0.0)
        assert(sweep.worstColumnCountChange == 0)
    }

    @Test
    fun `gate 4 - and it is NOT inert at 40 nm, where one rise deletes four phases' columns`() {
        val sweep = insetSensitivity(nominal, duplexes, admitRowEnd = false, insets = insets)
        assert(sweep.distinctSignatures > 1)
        assert(sweep.worstColumnCountChange == 1)
        // the guard's own KDoc: no phase brings a column within 0.28 nm of a 40 nm edge, so
        // 0.05 and half a rise agree and one rise does not
        val inertPair = insetSensitivity(
            nominal, duplexes, admitRowEnd = false, insets = insets.take(2)
        )
        assert(inertPair.distinctSignatures == 1)
    }

    @Test
    fun `gate 4 - admitting the row end makes the guard a POSITION, and it moves by the inset`() {
        val sweep = insetSensitivity(buildable, duplexes, admitRowEnd = true, insets = insets)
        assert(sweep.distinctSignatures == 3)
        assert(sweep.worstColumnCountChange == 0)
        assert(sweep.worstStationDisplacement.isCloseTo(rise - CrossoverLayout.EDGE_MARGIN))
    }

    // ------------------------------------------------------------------ gate 5: conservation

    @Test
    fun `gate 5 - admitting the row end can only ADD stations, never move or remove one`() {
        (0 until 32).forEach { phase ->
            listOf(nominal, buildable).forEach { width ->
                val refused = upwardLatticeSignature(phase, width, duplexes, admitRowEnd = false)
                val admitted = upwardLatticeSignature(phase, width, duplexes, admitRowEnd = true)
                assert(admitted.upwardSites >= refused.upwardSites)
                refused.rows.zip(admitted.rows).forEach { (before, after) ->
                    assert(before.all { site -> after.any { abs(it - site) < 1e-12 } })
                }
            }
        }
    }

    @Test
    fun `gate 5 - the inventory is 15 sites richer at exactly the phases whose end plane is odd`() {
        val gained = (0 until 32).filter { phase ->
            upwardLatticeSignature(phase, buildable, duplexes, admitRowEnd = true).upwardSites -
                    upwardLatticeSignature(
                        phase, buildable, duplexes, admitRowEnd = false
                    ).upwardSites == duplexes
        }
        assert(gained == listOf(0, 16))
        // the two end-of-row COLUMN phases gain no station at all, because their end plane is
        // even and an even plane carries no upward azimuth
        listOf(8, 24).forEach { phase ->
            assert(
                upwardLatticeSignature(
                    phase, buildable, duplexes, admitRowEnd = true
                ).upwardSites == upwardLatticeSignature(
                    phase, buildable, duplexes, admitRowEnd = false
                ).upwardSites
            )
        }
    }
}
