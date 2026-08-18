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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-161`, leaf `A8.2` — can a crossover be drawn at the LAST base pair of a duplex?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The substance of this task is a **reading**, so the code here carries only what a verdict
 * must not be allowed to assert without proof: the covalent count at a duplex end, the parity
 * congruence that decides whether a row-end column *can* be a boustrophedon's raster turns, and
 * `C-0090`'s two readings **recomputed from its own result file** rather than transcribed.
 */
class RowEndCrossoverTest {

    private val duplexes = 15

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    private val sheet =
        origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

    private val buildableEdgeX = BUILDABLE_RASTER_ROW_BASE_PAIRS * rise

    // ---------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 - a turn census is a partition of the interfaces and carries no length`() {
        val census = rasterTurns(duplexes)
        val all = (census.negativeEdgeInterfaces + census.positiveEdgeInterfaces).sorted()
        assert(all == (0 until duplexes - 1).toList())
        assert(census.negativeEdgeInterfaces.intersect(
            census.positiveEdgeInterfaces.toSet()
        ).isEmpty())
    }

    @Test
    fun `gate 1 - unphysical arguments throw at every entry point`() {
        assertFailsWith<IllegalArgumentException> { rasterTurns(1) }
        assertFailsWith<IllegalArgumentException> { interfacesServedByColumnParity(2, 15) }
        assertFailsWith<IllegalArgumentException> { interfacesServedByColumnParity(0, 1) }
        assertFailsWith<IllegalArgumentException> { rowEndColumnsAreComplementary(0) }
        assertFailsWith<IllegalArgumentException> { rowEndColumnsAreComplementary(112, 0) }
        assertFailsWith<IllegalArgumentException> {
            crossoverBudgetOfDuplexEnd(azimuthsPerPeriod = 0)
        }
    }

    // ---------------------------------------------------------------- gate 2: limiting cases

    @Test
    fun `gate 2 - a duplex end has two strand termini and ONE reachable neighbour`() {
        // CLAUDE.md: a duplex END has exactly two strand termini, and no force field adds a third.
        assert(STRAND_TERMINI_AT_DUPLEX_END == 2)
        // the square lattice's four azimuths, of which a single-layer sheet occupies the two
        // in-plane ones — but ONE base pair points at ONE of them, so a terminal base pair can
        // reach exactly one neighbour.
        assert(crossoverBudgetOfDuplexEnd() == 1)
        // doubling the azimuth count does not buy a second crossover at one base pair
        assert(crossoverBudgetOfDuplexEnd(azimuthsPerPeriod = 8) == 1)
    }

    @Test
    fun `gate 2 - a boustrophedon demands exactly one crossover at each row end it uses`() {
        for (d in 2..24) {
            val census = rasterTurns(d)
            assert(maximumTurnsPerRowEnd(census) == 1)
            assert(maximumTurnsPerRowEnd(census) <= crossoverBudgetOfDuplexEnd())
            // exactly two free row ends — the scaffold's own two termini
            val free = census.freeEndRowsAtNegativeX + census.freeEndRowsAtPositiveX
            assert(free.size == 2)
            assert(free.all { it in 0 until d })
            // and on an ODD row count they fall one per edge, on an even one both on one edge
            assert((census.freeEndRowsAtNegativeX.size == 1) == (d % 2 == 1))
            assert(census.negativeEdgeInterfaces.size + census.positiveEdgeInterfaces.size == d - 1)
        }
    }

    @Test
    fun `gate 2 - reversing the raster sense swaps the two edges and nothing else`() {
        val forward = rasterTurns(duplexes, entryAtNegativeX = true)
        val reverse = rasterTurns(duplexes, entryAtNegativeX = false)
        assert(forward.negativeEdgeInterfaces == reverse.positiveEdgeInterfaces)
        assert(forward.positiveEdgeInterfaces == reverse.negativeEdgeInterfaces)
    }

    @Test
    fun `gate 2 - a column of one parity serves exactly the interfaces of that parity`() {
        assert(interfacesServedByColumnParity(0, 15) == listOf(0, 2, 4, 6, 8, 10, 12))
        assert(interfacesServedByColumnParity(1, 15) == listOf(1, 3, 5, 7, 9, 11, 13))
        // and the two are a partition, at every duplex count
        for (d in 2..24) {
            val union = (interfacesServedByColumnParity(0, d) +
                    interfacesServedByColumnParity(1, d)).sorted()
            assert(union == (0 until d - 1).toList())
        }
    }

    @Test
    fun `gate 2 - the complementarity congruence is the parity of the pitch count`() {
        assert(rowEndColumnsAreComplementary(112))        // 7 pitches — odd
        assert(rowEndColumnsAreComplementary(16))         // 1
        assert(rowEndColumnsAreComplementary(48))         // 3
        assert(rowEndColumnsAreComplementary(144))        // 9
        assert(!rowEndColumnsAreComplementary(128))       // 8 pitches — even
        assert(!rowEndColumnsAreComplementary(288))       // Rothemund's own rectangle, 18
        assert(!rowEndColumnsAreComplementary(118))       // not a whole pitch count at all
    }

    @Test
    fun `gate 2 - C-0086's odd-half-turn rule IS the complementarity condition`() {
        // Every admissible seamless raster row length is an odd number of column pitches, and
        // every inadmissible one is not — the two congruences are the same statement.
        for (basePairs in 1..400) {
            assert(
                isOddHalfTurnSeparation(basePairs) == rowEndColumnsAreComplementary(basePairs)
            ) { "$basePairs disagrees" }
        }
    }

    // ------------------------------------------------- gate 3: symmetry and reconstruction

    @Test
    fun `gate 3 - the lattice's own parities agree with the closed-form congruence`() {
        // Two independently written quantities: one reads `rasterColumnLayout`'s parity list,
        // the other is arithmetic in the row length. Nothing forces them to agree.
        for (phase in endOfRowColumnPhases(BUILDABLE_RASTER_ROW_BASE_PAIRS)) {
            val (negative, positive) = rowEndColumnParities(phase, sheet, buildableEdgeX)
            assert((negative != positive) ==
                    rowEndColumnsAreComplementary(BUILDABLE_RASTER_ROW_BASE_PAIRS))
        }
    }

    @Test
    fun `gate 3 - exactly one raster sense lands its turns on the two row-end columns`() {
        for (phase in endOfRowColumnPhases(BUILDABLE_RASTER_ROW_BASE_PAIRS)) {
            val match = rasterTurnsOnRowEndColumns(duplexes, phase, sheet, buildableEdgeX)
            assert(match.matches)
            assert(match.entryAtNegativeX != null)
            assert(match.turnsAtNegativeEdge == 7)
            assert(match.turnsAtPositiveEdge == 7)
        }
    }

    @Test
    fun `gate 3 - a tile an EVEN number of pitches wide cannot be a boustrophedon`() {
        // 128 bp = 8 pitches: both row-end columns carry the same parity, so one edge would have
        // to host seven turns on interfaces its own base pairs cannot reach.
        val evenEdgeX = 128 * rise
        val phases = endOfRowColumnPhases(128)
        assert(phases.isNotEmpty())
        for (phase in phases) {
            val match = rasterTurnsOnRowEndColumns(duplexes, phase, sheet, evenEdgeX)
            assert(!match.matches)
            assert(match.entryAtNegativeX == null)
        }
    }

    @Test
    fun `gate 3 - the two row-end columns together carry one crossover per interface`() {
        for (phase in endOfRowColumnPhases(BUILDABLE_RASTER_ROW_BASE_PAIRS)) {
            val inventory = rowEndInventory(duplexes, phase, sheet, buildableEdgeX)
            assert(inventory.columns == 8)
            assert(inventory.interfaceCrossovers == 56)
            assert(inventory.scaffoldCrossovers == duplexes - 1)
            assert(inventory.stapleCrossovers == 56 - (duplexes - 1))
        }
    }

    @Test
    fun `gate 3 - admitting the row-end column adds no UPWARD station`() {
        // C-0090 deliverable 3: an end plane has an even index, and the upward azimuth needs an
        // odd one, so the row-end crossover can never be a station. Re-checked here, not cited.
        for (phase in endOfRowColumnPhases(BUILDABLE_RASTER_ROW_BASE_PAIRS)) {
            val admitted = rasterUpwardSites(phase, buildableEdgeX, duplexes, admitRowEnd = true)
            val refused = rasterUpwardSites(phase, buildableEdgeX, duplexes, admitRowEnd = false)
            assert(admitted.size == refused.size)
            val worst = admitted.zip(refused).maxOf { (a, b) ->
                if (a.size != b.size) Double.MAX_VALUE
                else a.zip(b).maxOf { (x, y) -> abs(x - y) }
            }
            assert(worst == 0.0)
        }
    }

    // ---------------------------------------------------- gate 4: convergence / invariance

    @Test
    fun `gate 4 - the parity verdict is invariant under the numerical edge inset`() {
        for (inset in listOf(0.01, CrossoverLayout.EDGE_MARGIN, 0.5 * rise, rise - 1e-6)) {
            for (phase in endOfRowColumnPhases(BUILDABLE_RASTER_ROW_BASE_PAIRS)) {
                val (negative, positive) =
                    rowEndColumnParities(phase, sheet, buildableEdgeX, inset)
                assert(negative != positive)
            }
        }
    }

    @Test
    fun `gate 4 - the congruence is a property of the lattice, not of the tile size`() {
        // every admissible seamless width, not just 112 bp
        for (basePairs in admissibleRasterRowLengths(400)) {
            if (basePairs < 32) continue
            val edgeX = basePairs * rise
            val phases = endOfRowColumnPhases(basePairs)
            assert(phases.size == 2)
            for (phase in phases) {
                assert(rasterTurnsOnRowEndColumns(duplexes, phase, sheet, edgeX).matches)
            }
        }
    }

    // ------------------------------------------------ gate 5: literature and upstream

    @Test
    fun `gate 5 - Rothemund's own rectangle has its edges ON the column lattice`() {
        // Supplementary Fig. S19: "27 turns wide at 10.666 bases / turn -> 288 nt", 24 helices
        // tall. 288 bp is 18 column pitches EXACTLY, so both vertical edges of the structure
        // Rothemund folded at 90 % well-formed yield carry a crossover at the last base pair.
        assert(ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS == 288)
        assert(ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS % COLUMN_PITCH_BASE_PAIRS == 0)
        assert(ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS / COLUMN_PITCH_BASE_PAIRS == 18)
        // and its two half-rows, which are what its seam makes the scaffold-crossover
        // separation, are 9 pitches — odd, i.e. Rothemund's own progressive-raster condition
        assert(isOddHalfTurnSeparation(ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS / 2))
        // an even pitch count is exactly the DOUBLE raster it is, not a boustrophedon
        assert(!rowEndColumnsAreComplementary(ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS))
    }

    @Test
    fun `gate 5 - the double raster serves ONE parity at both outer edges, as its width demands`() {
        // Rothemund's rectangle is 24 helices tall and 288 bp = 18 pitches wide. An even pitch
        // count puts the SAME parity on both row-end columns, and a double raster's outer edges
        // demand exactly that — two independent constructions agreeing on a folded structure.
        for (d in 2..24 step 2) {
            val (left, right) = doubleRasterOuterEdgeInterfaces(d)
            assert(left == right)
            assert(left == interfacesServedByColumnParity(0, d))
        }
        assert(!rowEndColumnsAreComplementary(ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS))
        assertFailsWith<IllegalArgumentException> { doubleRasterOuterEdgeInterfaces(15) }
    }

    @Test
    fun `gate 5 - C-0090's two readings are recomputed from its own result file`() {
        val file = File("gpd/results/T-153-buildable-raster-width.json")
        assert(file.exists())
        val readings = c0090RowEndReadings(file)
        val admitted = readings.single { it.admitRowEnd && it.phaseBasePairs == 8 }
        val refused = readings.single { !it.admitRowEnd && it.phaseBasePairs == 8 }
        assert(abs(admitted.bestDishingOverStroke - 0.0621469105) < 1e-9)
        assert(abs(refused.bestDishingOverStroke - 0.168371808) < 1e-9)
        assert(admitted.columns == 8)
        assert(refused.columns == 6)
        assert(admitted.flatAtTenPercent)
        assert(!refused.flatAtTenPercent)
    }

    @Test
    fun `gate 5 - the recommended reading is the admitted one and it is inside T-5b`() {
        val readings = c0090RowEndReadings(File("gpd/results/T-153-buildable-raster-width.json"))
        val verdict = rowEndVerdict(readings)
        assert(verdict.admitRowEnd)
        assert(abs(verdict.dishingOverStroke - 0.0621469105) < 1e-9)
        assert(verdict.dishingOverStroke < FLATNESS_CONVENTION)
        assert(verdict.rejectedDishingOverStroke > FLATNESS_CONVENTION)
        assert(abs(verdict.ratio - verdict.rejectedDishingOverStroke /
                verdict.dishingOverStroke) < 1e-12)
        // C-0090's headline pair is the SAME-PHASE one and it is not the best-of-convention
        // pair — 0.168371808 at phase 8 against 0.156510532 at phase 24. Both are carried,
        // and the study must not quote one as the other.
        assert(abs(verdict.rejectedAtSamePhase - 0.168371808) < 1e-9)
        assert(abs(verdict.rejectedDishingOverStroke - 0.156510532) < 1e-9)
        assert(verdict.rejectedAtSamePhase > verdict.rejectedDishingOverStroke)
    }
}
