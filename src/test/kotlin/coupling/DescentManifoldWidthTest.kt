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
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-215` — the width of the optimal set a minimax descent reports its answer from.
 *
 * `C-0131` found `gpd/results/T-129-range-robust-placement.json` moving between runs of
 * identical code, and `CLAUDE.md` already names the cause: *"a descent on an optimal MANIFOLD
 * has no isolated answer to be reproducible about"*. What this file tests is the **instrument**
 * that turns that explanation into a measurement — the two widths, on the VALUE and on the
 * POINT, and the amplification between them.
 *
 * The distinction the tests are built on: a **VALUE** width is a spread of the objective, a
 * **POINT** width is a spread of a functional of the argmin. `CLAUDE.md`'s claim is that on a
 * manifold the second exceeds the first, and the limit of that claim — a value width of exactly
 * zero beside a non-zero point width — is a manifold in the strict sense and must not be
 * reported as an infinite amplification dressed up as a number.
 */
class DescentManifoldWidthTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val lengthY = duplexes * sheet.interhelicalDistance

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    private fun lattice() = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = 2,
        supports = emptyList()
    )

    private val interiorPressure =
        Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lengthY)

    private fun field(depth: Double, width: Double, rim: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lengthY,
            listOf(CollarTerm(depth, width), CollarTerm(rim, 1.0))
        )

    /** Two of `C-0022`'s solved states, transcribed as `RobustDistributionTest` transcribes them. */
    private val states = listOf(
        LoadState("2 mM, 10 nm, 0.192 V", field(-0.302887367, 8.93928311, -0.593889278)),
        LoadState("10 mM, 10 nm, 0.192 V", field(0.419998636, 2.39768412, -2.73316696))
    )

    private fun surrogate() = multiStateSurrogate(
        lattice(), attachmentGrid(3, duplexes, Gen1Tile.EDGE_X, lengthY), states, 41
    )

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional - a relative width carries no units, so scaling every member leaves it`() {
        val values = listOf(0.0364754519, 0.0365712568, 0.0365712568)
        val scaled = values.map { it * 1.0e6 }
        assert(ensembleWidth(scaled).isCloseTo(ensembleWidth(values), 1e-12))
    }

    @Test
    fun `gate 1 dimensional - the width of T-129's own two ranges 1 readings is 2 6197e-3`() {
        // (0.0365712568 - 0.0364754519) / 0.0365712568, the number this task has to quote.
        assert(
            ensembleWidth(listOf(0.0364754519, 0.0365712568)).isCloseTo(2.619677539e-3, 1e-9)
        )
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting - an ensemble of identical readings has width exactly zero`() {
        assert(ensembleWidth(listOf(0.25, 0.25, 0.25)) == 0.0)
    }

    @Test
    fun `gate 2 limiting - one member has width exactly zero and no amplification`() {
        val width = manifoldWidth(listOf(0.25), listOf(1.7))
        assert(width.members == 1)
        assert(width.valueWidth == 0.0)
        assert(width.pointWidth == 0.0)
        assert(width.amplification == null)
    }

    @Test
    fun `gate 2 limiting - a strict manifold is a zero value width beside a moving point`() {
        // The limit CLAUDE.md describes: the answer is unique and the place it was found is not.
        val width = manifoldWidth(listOf(0.25, 0.25), listOf(1.7, 1.9))
        assert(width.valueWidth == 0.0)
        assert(width.pointWidth > 0.1)
        // An amplification is a ratio to something that is exactly zero; it is not a number.
        assert(width.amplification == null)
    }

    @Test
    fun `gate 2 limiting - a moving value and a fixed point amplify by exactly zero`() {
        val width = manifoldWidth(listOf(0.25, 0.30), listOf(1.7, 1.7))
        assert(width.pointWidth == 0.0)
        assert(width.amplification!!.isCloseTo(0.0, 1e-15))
    }

    @Test
    fun `gate 2 limiting - a zero offset perturbation reproduces the unperturbed descent exactly`() {
        val here = surrogate()
        val starts = listOf(List(45) { 1.0 }, List(45) { 1.0 + 0.01 * it })
        val direct = minimaxStiffnessDistribution(
            surrogate = here, states = listOf(0, 1), totalStiffness = mandate, starts = starts
        )
        val measured = descentDegeneracy(
            surrogate = here, states = listOf(0, 1), totalStiffness = mandate,
            starts = starts, ulpOffsets = listOf(0)
        )
        assert(measured.members == 1)
        assert(measured.values.single().isCloseTo(direct.worstDishing, 1e-15))
        assert(measured.points.single().isCloseTo(direct.stiffnesses.max(), 1e-15))
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 symmetry - a width is a set statistic and does not depend on member order`() {
        val values = listOf(0.0364754519, 0.0365712568, 0.0365712568)
        val points = listOf(1.72260028, 1.7123471, 1.7123471)
        val forward = manifoldWidth(values, points)
        val reversed = manifoldWidth(values.reversed(), points.reversed())
        assert(forward.valueWidth.isCloseTo(reversed.valueWidth, 1e-15))
        assert(forward.pointWidth.isCloseTo(reversed.pointWidth, 1e-15))
        assert(forward.distinctValues == reversed.distinctValues)
    }

    @Test
    fun `gate 3 symmetry - distinct readings are counted, not members`() {
        val width = manifoldWidth(
            listOf(0.0364754519, 0.0365712568, 0.0365712568),
            listOf(1.72260028, 1.7123471, 1.7123471)
        )
        assert(width.members == 3)
        assert(width.distinctValues == 2)
    }

    // ---------------------------------------------------------------- gate 4 — numerical

    @Test
    fun `gate 4 numerical - the amplification is the ratio of the two widths, exactly`() {
        val width = manifoldWidth(
            listOf(0.0364754519, 0.0365712568), listOf(1.72260028, 1.7123471)
        )
        assert(
            width.amplification!!.isCloseTo(width.pointWidth / width.valueWidth, 1e-14)
        )
        // T-129's own ranges[1]: the POINT moves 2.27 times as far as the VALUE.
        assert(width.amplification!!.isCloseTo(2.27209367, 1e-8))
    }

    @Test
    fun `gate 4 numerical - a whole-ulp perturbation is a perturbation of the last bit`() {
        val start = listOf(1.0, 2.0, 3.0)
        val nudged = start.perturbedByUlps(1)
        assert(nudged[0] > start[0])
        assert(nudged[0] == Math.nextUp(start[0]))
        assert(nudged[1] == start[1])
        assert(nudged[2] == start[2])
        assert(start.perturbedByUlps(0) == start)
    }

    // ---------------------------------------------------------------- gate 5 — guards

    @Test
    fun `gate 4 numerical - every start is run on its own and the best of them is the ensemble's`() {
        // The identity that makes nearOptimalSpread the right instrument: the winner of the
        // per-start readings IS what minimaxStiffnessDistribution reports over the whole set.
        val here = surrogate()
        val starts = listOf(
            List(45) { 1.0 },
            List(45) { 1.0 + 0.02 * it },
            List(45) { 1.0 + 0.01 * (44 - it) }
        )
        val spread = nearOptimalSpread(
            surrogate = here, states = listOf(0, 1), totalStiffness = mandate, starts = starts
        )
        assert(spread.startsUsed == 3)
        assert(spread.readings.size == 3)
        // The all-start width is over a set no threshold can make marginal, so it is the wider
        // of the two by construction — a containment, not a measurement.
        assert(spread.allStartsWidth.pointWidth > spread.width.pointWidth - 1e-15)
        assert(spread.allStartsWidth.valueWidth > spread.width.valueWidth - 1e-15)
        val together = minimaxStiffnessDistribution(
            surrogate = here, states = listOf(0, 1), totalStiffness = mandate, starts = starts
        )
        // The ensemble's answer is no worse than the best single start, and the polish can only
        // improve it — so the ensemble is bounded above by the per-start minimum.
        assert(together.worstDishing < spread.readings.minOf { it.objective } * (1.0 + 1e-9))
    }

    @Test
    fun `gate 5 guard - an empty ensemble has no width to report`() {
        assertFailsWith<IllegalArgumentException> { ensembleWidth(emptyList()) }
    }

    @Test
    fun `gate 5 guard - values and points must be the same ensemble`() {
        assertFailsWith<IllegalArgumentException> {
            manifoldWidth(listOf(0.1, 0.2), listOf(1.0))
        }
    }
}
