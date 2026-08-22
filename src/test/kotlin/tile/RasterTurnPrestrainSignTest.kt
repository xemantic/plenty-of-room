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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-284` — what sets the SIGN of a raster turn's `8.57142857°` departure.
 *
 * Written before `tile/RasterTurnPrestrainSign.kt` and watched fail.
 *
 * The gates each test names are `T-284`'s own `F1`–`F9`; the solve-based ones are taken on a small
 * block here and on the recommended `10 × 6` one in the study, where the reproduction against
 * `C-0175` §8's committed number lives.
 */
class RasterTurnPrestrainSignTest {

    private val recommended = HoneycombRasterTurnSigns(
        block = HoneycombBlock(10, 6),
        senseOneBasePairs = 102,
        senseTwoBasePairs = 109
    )

    private val undrawable = HoneycombRasterTurnSigns(
        block = HoneycombBlock(10, 6),
        senseOneBasePairs = 112,
        senseTwoBasePairs = 108
    )

    // ---------------------------------------------------------------- F1: the residues pin b0

    @Test
    fun `F1 - the recommended raster takes exactly two reduced residues ten apart`() {
        assert(recommended.closes)
        assert(recommended.reducedResidues.size == 59)
        val distinct = recommended.reducedResidues.distinct().sorted()
        assert(distinct.size == 2)
        assert(Math.floorMod(distinct[1] - distinct[0], 21) == 10)
        assert(recommended.classZeroResidueCandidates.size == 1)
        assert(recommended.classZeroResidue == 5)
    }

    @Test
    fun `F1 - every derived displacement is plus or minus the rule's own five base pairs`() {
        assert(recommended.signs.size == 59)
        recommended.signs.forEach {
            assert(abs(it.displacementBasePairs) == HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP)
        }
    }

    // ---------------------------------------------------- F2: the assignment is NOT uniform

    @Test
    fun `F2 - the derived displacement is not constant over the 59 turns`() {
        val values = recommended.signs.map { it.displacementBasePairs }.distinct()
        assert(values.size == 2)
        assert(recommended.isAlternating)
        val plus = recommended.signs.count { it.displacementBasePairs > 0 }
        val minus = recommended.signs.count { it.displacementBasePairs < 0 }
        assert(plus == 29)
        assert(minus == 30)
    }

    @Test
    fun `F2 - the alternation is the per-helix residue and not a coincidence`() {
        // C-0136: a helix's length residue is (L - 7*Delta) mod 21 in {0, 10, 11}; 0 carries the
        // sign THROUGH the helix and 10 or 11 FLIP it. 102 / 109 is 11 at every interior helix.
        assert(recommended.perHelixLengthResidues.size == 58)
        assert(recommended.perHelixLengthResidues.distinct() == listOf(11))
        assert(recommended.perHelixLengthResidues.all { it != 0 })
    }

    // ------------------------------------------------- F3: the partition survives the conventions

    @Test
    fun `F3 - the axial rim decides the displacement at every free convention`() {
        listOf(false, true).forEach { mirrored ->
            listOf(1, -1).forEach { firstAxialSign ->
                val signs = HoneycombRasterTurnSigns(
                    block = HoneycombBlock(10, 6),
                    senseOneBasePairs = 102,
                    senseTwoBasePairs = 109,
                    firstAxialSign = firstAxialSign,
                    mirrored = mirrored
                )
                assert(signs.closes)
                // a turn at the block's HIGH axial rim sits five base pairs BELOW its pair's
                // staple position, and one at the low rim five above it.
                assert(signs.highRimDisplacementBasePairs == -5)
                signs.signs.forEach {
                    assert(it.displacementBasePairs == (if (it.atHighEnd) -5 else 5))
                }
            }
        }
    }

    @Test
    fun `F3 - reversing the axial datum alone inverts it, which an improper flip must`() {
        val reversed = HoneycombRasterTurnSigns(
            block = HoneycombBlock(10, 6),
            senseOneBasePairs = 102,
            senseTwoBasePairs = 109,
            axialReversed = true
        )
        assert(reversed.closes)
        assert(reversed.highRimDisplacementBasePairs == 5)
        // and the DEPARTURE is invariant, because the datum's handedness travels with it.
        assert(reversed.signs.map { it.departureDegrees } == recommended.signs.map { it.departureDegrees })
    }

    // --------------------------------------------------- the departure: magnitude and sense

    @Test
    fun `the departure magnitude is C-0152's own allowed one at every turn`() {
        val allowed = allowedScaffoldCrossoverDepartureDegrees()
        recommended.signs.forEach { assert(abs(abs(it.departureDegrees) - allowed) < 1e-12) }
    }

    @Test
    fun `a crossover five base pairs ABOVE the staple position falls SHORT of the half turn`() {
        // the exact half turn is 5.25 bp, so +5 is 0.25 bp short and the azimuth is NEGATIVE.
        assert(scaffoldDisplacementDepartureDegrees(5) < 0.0)
        assert(scaffoldDisplacementDepartureDegrees(-5) > 0.0)
        assert(
            abs(
                scaffoldDisplacementDepartureDegrees(5) +
                        scaffoldDisplacementDepartureDegrees(-5)
            ) < 1e-15
        )
        assert(
            abs(
                abs(scaffoldDisplacementDepartureDegrees(5)) -
                        allowedScaffoldCrossoverDepartureDegrees()
            ) < 1e-12
        )
    }

    // ------------------------------------------------------------- the bond-graph alignment

    @Test
    fun `every derived turn is a bonded pair, indexed the same way as the tie list`() {
        val block = HoneycombBlock(10, 6)
        val pairs = honeycombBondPairs(block).toSet()
        val ties = honeycombRasterTurnList(block)
        val path = honeycombXRasterPath(10, 6)
        assert(ties.size == recommended.signs.size)
        ties.indices.forEach { k ->
            assert((ties[k].lowerBeam to ties[k].upperBeam) in pairs)
            // the residue walk and the tie list must be the SAME step k: a row transition is an
            // in-plane bond in one and a shared column in the other.
            assert(ties[k].inPlane == (path[k].x == path[k + 1].x))
            assert(ties[k].atHighEnd == recommended.signs[k].atHighEnd)
        }
    }

    // ------------------------------------------------------------------- F9: the control

    @Test
    fun `F9 - the undrawable 112 by 108 raster determines no assignment at all`() {
        assert(!undrawable.closes)
        assert(undrawable.reducedResidues.distinct().size == 3)
        assert(undrawable.classZeroResidueCandidates.isEmpty())
        assertFailsWith<IllegalStateException> { undrawable.signs }
        assertFailsWith<IllegalStateException> { undrawable.classZeroResidue }
    }

    /**
     * The state no raster this repository owns is in — **constructed**, because a mutation that
     * relaxes `classZeroResidue`'s uniqueness check to a non-emptiness one failed **nothing**
     * against the corpus's own two rasters: `102 / 109` has exactly one candidate and `112 / 108`
     * has none, so both refuse either way.
     *
     * A raster whose every helix carries `C-0136`'s residue **0** carries the sign THROUGH every
     * helix, so all 59 crossovers take **one** reduced residue, and `{r}` is admitted by both
     * `b₀ = r + 5` and `b₀ = r − 5`. Its assignment is genuinely **uniform** and its sign is
     * genuinely **one free binary** — which is `C-0180`'s sweep, on a raster it was not taken on.
     * `112 / 119` is such a raster on the `10 × 6` block: `112 ≡ 7` and `119 ≡ 14 (mod 21)`, which
     * are `7Δ` at the two effective senses this block puts on its helices.
     */
    @Test
    fun `F11 - a raster carrying ONE residue leaves TWO b0 candidates and the class refuses`() {
        val uniform = HoneycombRasterTurnSigns(HoneycombBlock(10, 6), 112, 119)
        assert(uniform.closes)
        assert(uniform.reducedResidues.distinct() == listOf(0))
        assert(uniform.perHelixLengthResidues.distinct() == listOf(0))
        assert(uniform.classZeroResidueCandidates == listOf(5, 16))
        assertFailsWith<IllegalStateException> { uniform.classZeroResidue }
        assertFailsWith<IllegalStateException> { uniform.signs }
    }

    // ---------------------------------------------------------- the ties carry the assignment

    @Test
    fun `the derived ties carry the assignment and the global phase negates it`() {
        val block = HoneycombBlock(4, 2)
        val signs = HoneycombRasterTurnSigns(block, 102, 109)
        val nodes = 5
        val plus = signs.ties(nodes, phase = 1)
        val minus = signs.ties(nodes, phase = -1)
        assert(plus.size == signs.signs.size)
        plus.indices.forEach {
            assert(abs(plus[it].prestrainRadians + minus[it].prestrainRadians) < 1e-18)
            assert(plus[it].lowerBeam == minus[it].lowerBeam)
            assert(plus[it].node == minus[it].node)
        }
        // and a zero-departure tie list is the pure-stiffness one honeycombScaffoldTurnTies gives
        assert(signs.ties(nodes, phase = 1, departureDegrees = 0.0) ==
                honeycombScaffoldTurnTies(block, nodes))
        assertFailsWith<IllegalArgumentException> { signs.ties(nodes, phase = 0) }
    }

    // ------------------------------------------ F5 and F6: the solver gates, on a small block

    private fun smallLattice(ties: List<HoneycombScaffoldTurnTie>) = HoneycombGrillage(
        block = HoneycombBlock(4, 2),
        rowBasePairs = 116,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        scaffoldTurnTies = ties
    )

    @Test
    fun `F5 - the free field is exactly linear in the assignment vector`() {
        val block = HoneycombBlock(4, 2)
        val signs = HoneycombRasterTurnSigns(block, 102, 109)
        val nodes = smallLattice(emptyList()).nodesPerBeam
        val pressure = uniformPressure(0.0)
        val zero = smallLattice(signs.ties(nodes, phase = 1, departureDegrees = 0.0))
        val zeroField = zero.solve(pressure)
        val plus = smallLattice(signs.ties(nodes, phase = 1)).solve(pressure)
        val minus = smallLattice(signs.ties(nodes, phase = -1)).solve(pressure)
        var worst = 0.0
        (0..20).forEach { i ->
            (0..20).forEach { j ->
                val s = zero.lengthS * (i / 20.0 - 0.5)
                val y = zero.lengthY * (j / 20.0 - 0.5)
                val residual = plus.deflection(s, y) + minus.deflection(s, y) -
                        2.0 * zeroField.deflection(s, y)
                worst = maxOf(worst, abs(residual))
            }
        }
        assert(worst < 1e-9)
    }

    @Test
    fun `F6 - a uniform pressure on the tied zero-prestrain lattice dishes exactly zero`() {
        val block = HoneycombBlock(4, 2)
        val nodes = smallLattice(emptyList()).nodesPerBeam
        val tied = smallLattice(honeycombScaffoldTurnTies(block, nodes))
        assert(tied.solve(uniformPressure(0.02)).peakDishing(41) < 1e-9)
    }
}
