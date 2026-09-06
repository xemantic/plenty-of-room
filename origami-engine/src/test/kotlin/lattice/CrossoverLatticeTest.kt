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
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.tile.HoneycombLattice
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * One interface over both crossover lattices, and the laws that only become visible from it.
 *
 * This repository discovered the asymmetry the hard way: its **placement** machinery is
 * lattice-generic and took the honeycomb unmodified, while its **site generators** —
 * `CrossoverLayout`'s two parities, `upwardRootLattice`, `centroSymmetricUpwardPhases` — are
 * hard square-lattice. That is what made a late honeycomb correction invalidate a corpus of
 * placement results rather than re-run it.
 *
 * These tests do not restate either lattice's constants. They **derive** them from one another and
 * assert that both objects satisfy the same laws, so that a third lattice — or a corrected
 * constant — cannot be added without meeting them:
 *
 *  * `samePairPeriod = azimuths × step`, which is 4 × 8 = 32 and 3 × 7 = 21;
 *  * the step exists **because** it lands on the neighbouring azimuth, which is what makes the
 *    number 8 rather than 7 on the square lattice;
 *  * the register departure per period, which is 8.571° on the square sheet and **exactly zero**
 *    on the honeycomb — `CLAUDE.md`'s *"the honeycomb has no twist to correct"*, derived here
 *    rather than asserted.
 */
class CrossoverLatticeTest {

    private val lattices = listOf(SquareCrossoverLattice, HoneycombCrossoverLattice)

    // --- gate 1: the period is a PRODUCT, on both lattices ---------------------------------------

    @Test
    fun `the same-pair period is the azimuth count times the step, on every lattice`() {
        lattices.forEach { lattice ->
            assert(lattice.samePairPeriodBasePairs
                == lattice.azimuths * lattice.anyAzimuthStepBasePairs)
        }
    }

    @Test
    fun `the square lattice is four azimuths at eight base pairs, so thirty-two`() {
        assert(SquareCrossoverLattice.azimuths == 4)
        assert(SquareCrossoverLattice.anyAzimuthStepBasePairs == 8)
        assert(SquareCrossoverLattice.samePairPeriodBasePairs == 32)
    }

    @Test
    fun `the honeycomb is three azimuths at seven base pairs, so twenty-one`() {
        assert(HoneycombCrossoverLattice.azimuths == 3)
        assert(HoneycombCrossoverLattice.anyAzimuthStepBasePairs == 7)
        assert(HoneycombCrossoverLattice.samePairPeriodBasePairs == 21)
    }

    @Test
    fun `the honeycomb lattice reproduces the constants tile HoneycombLattice already carries`() {
        // A reproduction, not a re-assertion: if the two ever disagree the corpus has two lattices.
        assert(HoneycombCrossoverLattice.azimuths == HoneycombLattice.AZIMUTHS)
        assert(HoneycombCrossoverLattice.anyAzimuthStepBasePairs
            == HoneycombLattice.ANY_AZIMUTH_STEP_BP)
        assert(HoneycombCrossoverLattice.samePairPeriodBasePairs
            == HoneycombLattice.SAME_PAIR_PERIOD_BP)
        assert(HoneycombCrossoverLattice.azimuthSeparationDegrees()
            .isCloseTo(HoneycombLattice.azimuthSeparationDegrees()))
    }

    // --- gate 2: the step is what it is BECAUSE it lands on the next azimuth ----------------------

    @Test
    fun `one step advances the helical phase by exactly one azimuth separation`() {
        lattices.forEach { lattice ->
            val advance = lattice.azimuthAdvanceDegrees()
            assert(abs(abs(advance) - lattice.azimuthSeparationDegrees()) < 1e-9)
        }
    }

    @Test
    fun `the square step advances 270 degrees, which is minus one azimuth`() {
        // 8 bp x 33.75 deg/bp = 270 deg exactly, at caDNAno's 32/3 bases per turn.
        assert(SquareCrossoverLattice.stepAdvanceDegrees().isCloseTo(270.0))
        assert(SquareCrossoverLattice.azimuthAdvanceDegrees().isCloseTo(-90.0))
    }

    @Test
    fun `the honeycomb step advances 240 degrees, which is minus one azimuth`() {
        // 7 bp x 360/10.5 = 240 deg exactly.
        assert(HoneycombCrossoverLattice.stepAdvanceDegrees().isCloseTo(240.0))
        assert(HoneycombCrossoverLattice.azimuthAdvanceDegrees().isCloseTo(-120.0))
    }

    // --- gate 3: the register departure, derived rather than asserted -----------------------------

    @Test
    fun `the square sheet is undertwisted by 8_571 degrees per period`() {
        // A 32 bp period of B-DNA is 1097.14 deg against the 1080 the lattice draws.
        assert(SquareCrossoverLattice.registerDepartureDegreesPerPeriod().isCloseTo(-17.1428571428571))
        assert(SquareCrossoverLattice.registerDepartureDegreesPerDomain().isCloseTo(-8.57142857142857))
    }

    @Test
    fun `the honeycomb has NO twist to correct, exactly`() {
        // 21 bp = 2 turns of B-DNA exactly, so the departure is zero and not merely small.
        assert(abs(HoneycombCrossoverLattice.registerDepartureDegreesPerPeriod()) < 1e-9)
    }

    @Test
    fun `the departure accumulates linearly along a row`() {
        val oneRow = SquareCrossoverLattice.registerDepartureDegrees(112)
        val halfRow = SquareCrossoverLattice.registerDepartureDegrees(56)
        assert(oneRow.isCloseTo(2.0 * halfRow))
        // C-0086's 112 bp row: seven 16 bp domains, each 8.571 deg short, all the same sign.
        assert(oneRow.isCloseTo(-60.0))
    }

    // --- gate 4: the station ladder is ONE azimuth's period, never the step ------------------------

    @Test
    fun `a station ladder steps by the same-pair period, not by the azimuth step`() {
        val honeycomb = HoneycombCrossoverLattice.stationLadder(rowBasePairs = 112, phaseBasePairs = 8)
        assert(honeycomb == listOf(8, 29, 50, 71, 92))
        val square = SquareCrossoverLattice.stationLadder(rowBasePairs = 112, phaseBasePairs = 8)
        assert(square == listOf(8, 40, 72, 104))
    }

    @Test
    fun `a ladder never leaves the row`() {
        lattices.forEach { lattice ->
            val ladder = lattice.stationLadder(rowBasePairs = 112, phaseBasePairs = 20)
            assert(ladder.all { it > -1 })
            assert(ladder.all { it < 112 })
        }
    }

    @Test
    fun `a phase outside the period is refused rather than folded silently`() {
        lattices.forEach { lattice ->
            assertFailsWith<IllegalArgumentException> {
                lattice.stationLadder(rowBasePairs = 112, phaseBasePairs = lattice.samePairPeriodBasePairs)
            }
        }
    }

    // --- gate 5: what the interface deliberately does NOT promise ----------------------------------

    @Test
    fun `every lattice declares whether a centro-symmetric phase congruence exists for it`() {
        // C-0141's P4 finding, made mechanical. `centroSymmetricUpwardPhases` is a congruence that
        // assumes row `r` and row `D-1-r` share a sublattice, which is true of the square sheet and
        // FALSE of a honeycomb face: there the 7 bp stagger between adjacent station rows is forced,
        // and whether a symmetric family exists at all is decided by the rooting-helix PARITY.
        // A caller that asks this before reusing a phase result cannot inherit the wrong lattice's.
        assert(SquareCrossoverLattice.hasCentroSymmetricPhaseCongruence)
        assert(!HoneycombCrossoverLattice.hasCentroSymmetricPhaseCongruence)
    }
}
