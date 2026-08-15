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
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-134`, leaf `A8.2` — a tolerance model for the two knife edges `C-0069` and `C-0066` carry.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * **The strong free limiting case this task declared is here as a test: at ZERO scatter the model
 * must reproduce `C-0066`'s and `C-0069`'s nominal clearances exactly, and it must reproduce them
 * as the SAME number** — the two claims report 0.0256 nm independently and this task's first
 * assertion is that they are one lattice quantity, `p − d − L`, seen twice.
 */
class PlanToleranceTest {

    private val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR

    private val width = Gen1Tile.INTERHELICAL_SHEET

    private val arm = C0055_ARM_LENGTH

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    private val kT = thermalEnergy()

    /** `C-0063`'s phase-24 placement, as `C-0066` publishes it. */
    private val phase24Rows: List<StationRow> = listOf(
        listOf(-16.32, -5.44, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 5.44, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, 16.32),
        listOf(-10.88, 10.88),
        listOf(-16.32, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, -5.44, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, 5.44, 16.32)
    ).mapIndexed { row, roots -> StationRow(row, (row - 7) * 2.69, roots.sorted()) }

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - a margin is a length and scales with every length`() {
        val scale = 10.0
        assert(
            planMargin(pitch * scale, width * scale, arm * scale)
                .isCloseTo(planMargin(pitch, width, arm) * scale)
        )
    }

    @Test
    fun `gate 1 - a relative threshold is dimensionless and invariant under rescaling`() {
        val scale = 7.0
        val plain = relativeThreshold(planMargin(pitch, width, arm), width)
        val scaled = relativeThreshold(planMargin(pitch, width, arm) * scale, width * scale)
        assert(scaled.isCloseTo(plain))
    }

    @Test
    fun `gate 1 - an axial fluctuation is a square root of length over a stretch modulus`() {
        val one = axialFluctuation(4.0, 1100.0, kT)
        val four = axialFluctuation(16.0, 1100.0, kT)
        assert(four.isCloseTo(2.0 * one))
        // and it halves when the modulus quadruples
        assert(axialFluctuation(4.0, 4400.0, kT).isCloseTo(0.5 * one))
    }

    @Test
    fun `gate 1 - a cantilever tip fluctuation goes as the three halves power of the arm`() {
        val one = cantileverTipFluctuation(2.0, 230.0, kT)
        val two = cantileverTipFluctuation(4.0, 230.0, kT)
        assert((two / one).isCloseTo(sqrt(8.0)))
    }

    @Test
    fun `gate 1 - the stiffness a margin demands is an inverse square of it`() {
        val coarse = stiffnessForMargin(0.2, kT)
        val fine = stiffnessForMargin(0.1, kT)
        assert((fine / coarse).isCloseTo(4.0))
        assert(rotationalStiffnessForMargin(0.1, 3.0, kT).isCloseTo(fine * 9.0))
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { planMargin(-1.0, width, arm) }
        assertFailsWith<IllegalArgumentException> { planMargin(pitch, -1.0, arm) }
        assertFailsWith<IllegalArgumentException> { planMargin(pitch, width, -1.0) }
        assertFailsWith<IllegalArgumentException> { relativeThreshold(0.1, 0.0) }
        assertFailsWith<IllegalArgumentException> { axialFluctuation(-1.0, 1100.0, kT) }
        assertFailsWith<IllegalArgumentException> { axialFluctuation(1.0, 0.0, kT) }
        assertFailsWith<IllegalArgumentException> { hingeTipFluctuation(1.0, 0.0, kT) }
        assertFailsWith<IllegalArgumentException> { cantileverTipFluctuation(0.0, 230.0, kT) }
        assertFailsWith<IllegalArgumentException> { stiffnessForMargin(0.0, kT) }
        assertFailsWith<IllegalArgumentException> { rotationalStiffnessForMargin(0.1, 0.0, kT) }
        assertFailsWith<IllegalArgumentException> { riseCoefficient(0, 24, rise, RiseCorrelation.COMMON) }
        assertFailsWith<IllegalArgumentException> { riseCoefficient(32, -1, rise, RiseCorrelation.COMMON) }
        assertFailsWith<IllegalArgumentException> { riseCoefficient(32, 24, 0.0, RiseCorrelation.COMMON) }
        assertFailsWith<IllegalArgumentException> { nullRiseRatio(32, 0) }
        assertFailsWith<IllegalArgumentException> { basePairsNearest(-1.0, rise) }
        assertFailsWith<IllegalArgumentException> { basePairsNearest(1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { builtLength(-1, rise) }
        assertFailsWith<IllegalArgumentException> { dropoutRateForRelativeScatter(-0.1) }
        assertFailsWith<IllegalArgumentException> { relativeScatterForDropoutRate(1.0) }
        assertFailsWith<IllegalArgumentException> { perStepRiseSigmaThreshold(0.03, 0, 24) }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - THE FREE LIMITING CASE - zero scatter reproduces both published clearances`() {
        val margin = planMargin(pitch, width, arm)
        // C-0069's Q5 arm margin against its own 8.19 nm plan budget
        assert(rowOfThreeLengthCeiling(pitch, width).isCloseTo(8.19, 1e-9))
        assert((rowOfThreeLengthCeiling(pitch, width) - arm).isCloseTo(margin, 1e-9))
        // C-0066's bound 4: the root pitch minus the arm, against a duplex
        assert((pitch - arm).isCloseTo(2.71561, 4e-6))
        assert(((pitch - arm) - width).isCloseTo(margin, 1e-9))
        // and both are the published 0.0256 nm
        assert(margin.isCloseTo(0.0256, 5e-3))
    }

    @Test
    fun `gate 2 - zero amplitude moves no margin on any channel`() {
        val margin = planMargin(pitch, width, arm)
        RiseCorrelation.entries.forEach { correlation ->
            val moved = margin - 0.0 * riseCoefficient(32, 24, rise, correlation)
            assert(moved.isCloseTo(margin))
        }
    }

    @Test
    fun `gate 2 - a built length is the rise times a whole number of base pairs`() {
        assert(basePairsNearest(arm, rise) == 24)
        assert(builtLength(24, rise).isCloseTo(8.16))
        assert(builtLength(0, rise).isCloseTo(0.0))
        // quantising the arm LENGTHENS the margin, because 24 bp is shorter than the solved arm
        assert(planMargin(pitch, width, builtLength(24, rise)) > planMargin(pitch, width, arm))
    }

    @Test
    fun `gate 2 - an infinitely stiff channel has no thermal excursion`() {
        assert(hingeTipFluctuation(arm, Double.POSITIVE_INFINITY, kT).isCloseTo(0.0))
        assert(axialFluctuation(arm, Double.POSITIVE_INFINITY, kT).isCloseTo(0.0))
        assert(cantileverTipFluctuation(arm, Double.POSITIVE_INFINITY, kT).isCloseTo(0.0))
    }

    @Test
    fun `gate 2 - a zero dropout rate is a zero scatter and the two are exact inverses`() {
        assert(relativeScatterForDropoutRate(0.0).isCloseTo(0.0))
        assert(dropoutRateForRelativeScatter(0.0).isCloseTo(0.0))
        listOf(0.01, 0.05, 0.17, 0.346, 0.5).forEach { scatter ->
            assert(
                relativeScatterForDropoutRate(dropoutRateForRelativeScatter(scatter))
                    .isCloseTo(scatter, 1e-12)
            )
        }
    }

    // ------------------------------------------------ gate 3 — symmetry and conservation

    @Test
    fun `gate 3 - the two knife edges are ONE quantity, identically, at every lattice constant`() {
        listOf(2.54, 2.69, 2.73, 3.0).forEach { d ->
            listOf(21.0, 32.0).forEach { spacing ->
                val p = spacing * rise
                listOf(4.0, 8.16439083, 12.0).forEach { length ->
                    val c0069 = rowOfThreeLengthCeiling(p, d) - length
                    val c0066 = (p - length) - d
                    assert(abs(c0069 - c0066) < 1e-12)
                    assert(abs(c0069 - planMargin(p, d, length)) < 1e-12)
                }
            }
        }
    }

    @Test
    fun `gate 3 - the rise sensitivity has an EXACT null direction and it is the count ratio`() {
        val ratio = nullRiseRatio(32, 24)
        assert(ratio.isCloseTo(32.0 / 24.0))
        // a perturbation in that ratio leaves the margin stationary to machine precision
        val epsilon = 1e-3
        val moved = planMargin(
            32 * rise * (1.0 + epsilon), width, 24 * rise * (1.0 + ratio * epsilon)
        )
        assert(abs(moved - planMargin(32 * rise, width, 24 * rise)) < 1e-14)
    }

    @Test
    fun `gate 3 - the common-mode coefficient is the DIFFERENCE of the two base-pair counts`() {
        val common = riseCoefficient(32, 24, rise, RiseCorrelation.COMMON)
        assert(common.isCloseTo((32 - 24) * rise))
        val opposed = riseCoefficient(32, 24, rise, RiseCorrelation.OPPOSED)
        assert(opposed.isCloseTo((32 + 24) * rise))
        val independent = riseCoefficient(32, 24, rise, RiseCorrelation.INDEPENDENT)
        assert(independent.isCloseTo(rise * sqrt(32.0 * 32.0 + 24.0 * 24.0)))
        val fixed = riseCoefficient(32, 24, rise, RiseCorrelation.FIXED_ELEMENT)
        assert(fixed.isCloseTo(32.0 * rise))
        // the whole point: the SAME amplitude is worth 7x between the best and the worst structure
        assert((opposed / common).isCloseTo(56.0 / 8.0))
        assert(common < independent && independent < opposed)
    }

    @Test
    fun `gate 3 - a common-mode rise perturbation on equal counts moves the margin exactly zero`() {
        assert(riseCoefficient(24, 24, rise, RiseCorrelation.COMMON).isCloseTo(0.0))
        assert(riseCoefficient(32, 32, rise, RiseCorrelation.COMMON).isCloseTo(0.0))
    }

    @Test
    fun `gate 3 - THE TWIST DOES NOT PROPAGATE across the band this project disputes`() {
        // A crossover pitch is an INTEGER base-pair count, arrived at by rounding three turns of
        // the helix; an arm is an integer base-pair count too. So the twist enters the margin only
        // through that integer, and over the whole band between the two readings this project
        // carries — 10.5 bp/turn and the square lattice's 10.67 — the integer does not move.
        assert(crossoverSpacingBasePairs(SQUARE_LATTICE_BASE_PAIRS_PER_TURN) == 32)
        assert(crossoverSpacingBasePairs(10.5) == 32)
        val reference = planMargin(
            crossoverSpacingBasePairs(SQUARE_LATTICE_BASE_PAIRS_PER_TURN) * rise,
            width, builtLength(24, rise)
        )
        (0..100).forEach { step ->
            val twist = 10.5 + step * (SQUARE_LATTICE_BASE_PAIRS_PER_TURN - 10.5) / 100.0
            val margin = planMargin(
                crossoverSpacingBasePairs(twist) * rise, width, builtLength(24, rise)
            )
            assert(abs(margin - reference) < 1e-15)
        }
        // and the coefficient is a step function, not a slope: it moves only where the integer does
        assert(crossoverSpacingBasePairs(10.0) == 30)
        assert(crossoverSpacingBasePairs(11.0) == 33)
    }

    @Test
    fun `gate 3 - the margin is linear in every channel and the channels superpose`() {
        val dRise = 1e-4
        val dWidth = 1e-4
        val direct = planMargin(32 * (rise + dRise), width + dWidth, 24 * (rise + dRise))
        val superposed = planMargin(32 * rise, width, 24 * rise) +
                (32 - 24) * dRise - dWidth
        assert(abs(direct - superposed) < 1e-14)
    }

    // ------------------------------------------ the design that has margin

    @Test
    fun `gate 2 - dropping zero roots returns the rows unchanged`() {
        assert(rowsWithoutInteriorRoots(phase24Rows, 0) == phase24Rows)
    }

    @Test
    fun `gate 3 - dropping four roots dissolves EVERY row of three and no row loses two`() {
        val reduced = rowsWithoutInteriorRoots(phase24Rows, 4)
        assert(reduced.sumOf { it.roots.size } == 30)
        assert(reduced.none { it.roots.size > 2 })
        assert(reduced.all { it.roots.isNotEmpty() })
        // C-0063's bound 1: 3a + 2(15 - a) = 34 forces exactly four rows of three
        assert(phase24Rows.count { it.roots.size == 3 } == 4)
        // the roots that survive are a subset of the roots that started
        reduced.forEachIndexed { index, row ->
            assert(phase24Rows[index].roots.containsAll(row.roots))
        }
    }

    @Test
    fun `gate 3 - the reduction is deterministic and monotone in the number dropped`() {
        assert(rowsWithoutInteriorRoots(phase24Rows, 4) == rowsWithoutInteriorRoots(phase24Rows, 4))
        (0..10).forEach { drop ->
            val reduced = rowsWithoutInteriorRoots(phase24Rows, drop)
            assert(reduced.sumOf { it.roots.size } == 34 - drop)
            if (drop > 0) {
                val previous = rowsWithoutInteriorRoots(phase24Rows, drop - 1)
                reduced.forEachIndexed { index, row ->
                    assert(previous[index].roots.containsAll(row.roots))
                }
            }
        }
    }

    @Test
    fun `gate 2 - dissolving the rows of three raises the length ceiling far above the arm`() {
        val full = rootedLengthCeiling(phase24Rows, Gen1Tile.EDGE_X, width)
        val reduced = rootedLengthCeiling(
            rowsWithoutInteriorRoots(phase24Rows, 4), Gen1Tile.EDGE_X, width
        )
        assert(full.isCloseTo(8.19, 1e-6))
        assert(reduced > full)
        // and the whole knife edge is inside the improvement
        assert(reduced - full > planMargin(pitch, width, arm) * 10.0)
    }

    @Test
    fun `gate 1 - the reduction refuses to empty a row`() {
        assertFailsWith<IllegalArgumentException> { rowsWithoutInteriorRoots(phase24Rows, -1) }
        assertFailsWith<IllegalArgumentException> { rowsWithoutInteriorRoots(phase24Rows, 20) }
    }

    // ------------------------------------------------ gate 4 — numerical convergence

    @Test
    fun `gate 4 - a threshold is a closed form and is resolution independent`() {
        val margin = planMargin(pitch, width, arm)
        val closed = relativeThreshold(margin, width)
        // a bisection on the same condition must land on it
        var low = 0.0
        var high = 1.0
        repeat(200) {
            val middle = 0.5 * (low + high)
            if (planMargin(pitch, width * (1.0 + middle), arm) > 0.0) low = middle else high = middle
        }
        assert(low.isCloseTo(closed, 1e-9))
    }

    @Test
    fun `gate 4 - the per-step rise threshold is the margin over the root of the total count`() {
        val margin = 0.03
        assert(perStepRiseSigmaThreshold(margin, 32, 24).isCloseTo(margin / sqrt(56.0)))
        // and it is the same thing an explicit variance sum returns
        val sigma = perStepRiseSigmaThreshold(margin, 32, 24)
        assert(sqrt(32.0 * sigma * sigma + 24.0 * sigma * sigma).isCloseTo(margin))
    }

    // ------------------------------------------------ gate 5 — literature and upstream

    @Test
    fun `gate 5 - the arm, the pitch and the count reproduce their own claims`() {
        assert(C0055_ARM_COUNT == 34)
        assert(arm.isCloseTo(8.16439, 1e-6))
        assert(pitch.isCloseTo(10.88, 1e-12))
        assert(width.isCloseTo(2.69, 1e-12))
        assert(rise.isCloseTo(0.34, 1e-12))
    }

    @Test
    fun `gate 5 - the square-lattice reading exceeds the margin and the honeycomb one does not`() {
        val margin = planMargin(pitch, width, arm)
        assert(SQUARE_LATTICE_INTERHELICAL - width > margin)
        assert(SQUARE_LATTICE_INTERHELICAL.isCloseTo(2.73, 1e-12))
        // the honeycomb value is SMALLER, so it opens the margin rather than closing it
        assert(Gen1Tile.INTERHELICAL_HONEYCOMB < width)
    }

    @Test
    fun `gate 5 - every floor this task computes exceeds the margin`() {
        val margin = planMargin(pitch, width, arm)
        assert(rise / margin > 1.0)
        assert((SQUARE_LATTICE_INTERHELICAL - width) / margin > 1.0)
        val axial = sqrt(
            axialFluctuation(pitch, Gen1Tile.DUPLEX_STRETCH_MODULUS, kT).let { it * it } +
                    axialFluctuation(arm, Gen1Tile.DUPLEX_STRETCH_MODULUS, kT).let { it * it }
        )
        assert(axial / margin > 1.0)
        assert(cantileverTipFluctuation(arm, Gen1Tile.DUPLEX_BENDING_RIGIDITY, kT) / margin > 1.0)
    }

    @Test
    fun `gate 5 - the MEASURED sheet lattice width exceeds every threshold this model computes`() {
        // Fischer et al., Nano Lett. 16:4282 (2016), SI Table S5: a_mean = 27.41 A, w_a = 2.5 A
        val measuredWidth = 0.25
        val measuredMean = 2.741
        val builtMargin = planMargin(pitch, width, builtLength(24, rise))
        // absolute: the measured width against the margin itself
        assert(measuredWidth / builtMargin > 8.0)
        // relative: against the LOOSEST threshold this model computes
        val loosest = RiseCorrelation.entries
            .maxOf { relativeThreshold(builtMargin, riseCoefficient(32, 24, rise, it)) }
            .coerceAtLeast(relativeThreshold(builtMargin, width))
        assert(measuredWidth / measuredMean > loosest)
        // and the measured mean is the lattice constant this project already uses, to 2 %
        assert(abs(measuredMean - width) / width < 0.02)
    }

    @Test
    fun `gate 5 - the measured WEAVE brackets the placement verdict from both sides`() {
        // Bai et al., PNAS 109:20012 (2012): <d_min> = 18.5 A at a crossover, <d_max> = 36 A
        assert(planMargin(pitch, 1.85, arm) > 0.0)
        assert(pitch - 3.60 - arm < 0.0)
        // the 2.69 nm lattice constant lies between them, which is what makes it a MEAN
        assert(width > 1.85 && width < 3.60)
    }

    @Test
    fun `gate 5 - the measured staple incorporation exceeds C-0060's flatness threshold`() {
        // Strauss et al., Nat. Commun. 9:1600 (2018): 48-95 %, mean 84 %
        val implied = relativeScatterForDropoutRate(1.0 - 0.84)
        assert(implied > 0.346)
        assert(implied > 0.17)
        // the edge sites are far worse, and C-0058 puts its stiff level there
        assert(relativeScatterForDropoutRate(1.0 - 0.48) > implied)
        // and the incorporation that would just hold C-0060's threshold
        assert((1.0 - dropoutRateForRelativeScatter(0.346)) > 0.84)
    }

    @Test
    fun `gate 5 - C-0026's and C-0060's thresholds map onto a dropout rate below one`() {
        val break_even = dropoutRateForRelativeScatter(0.17)
        val flatness = dropoutRateForRelativeScatter(0.346)
        assert(break_even > 0.0 && break_even < 1.0)
        assert(flatness > break_even)
        assert(break_even.isCloseTo(0.17 * 0.17 / (1.0 + 0.17 * 0.17)))
    }
}
