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

import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-132`, leaf `A8.2` — does the leg's own length budget survive the **pinned** base
 * misalignment, at **one** leg length shared by all 34 instances?
 *
 * Every test is named for the verification gate it discharges. The two free limiting cases
 * `T-132` declared are here as tests: **an unpinned base must reproduce `C-0052`'s budget**
 * (and `C-0062`'s design table through it), and **a single instance must reproduce `C-0065`'s
 * register placement**.
 */
class PinnedLegBudgetTest {

    private val degrees = 180.0 / PI

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    /**
     * One shared register, at the smallest window that still contains the 9 bp row's nearest
     * closing pair centre — the legs of that pair sit at −4.0 and +5.0 base pairs from the
     * station, so ±6 bp is enough and the field costs a fraction of the study's.
     */
    private val register: PinnedBaseRegister by lazy {
        PinnedBaseRegister(halfWindowBasePairs = 6)
    }

    /** `C-0063`'s phase-24 placement, as its claim and its result file publish it. */
    private fun c0063Stations(): List<TrussStation> {
        val odd = listOf(-16.32, -5.44, 5.44, 16.32)
        val even = listOf(-10.88, 0.0, 10.88)
        val roots = listOf(
            listOf(-16.32, -5.44, 16.32), listOf(0.0, 10.88), listOf(-16.32, 5.44, 16.32),
            listOf(0.0, 10.88), listOf(-16.32, 16.32), listOf(-10.88, 0.0),
            listOf(-16.32, 16.32), listOf(-10.88, 10.88), listOf(-16.32, 16.32),
            listOf(0.0, 10.88), listOf(-16.32, 16.32), listOf(-10.88, 0.0),
            listOf(-16.32, -5.44, 16.32), listOf(-10.88, 0.0), listOf(-16.32, 5.44, 16.32)
        )
        return roots.flatMapIndexed { row, xs ->
            val sites = if (row % 2 == 0) odd else even
            xs.forEach { require(it in sites) { "row $row has no upward site at $it" } }
            xs.map { TrussStation(row, it, (row - 7) * OrigamiDuplex.INTERHELICAL) }
        }
    }

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - a signed chord deviation is a line coordinate in the half open interval`() {
        for (i in -40..40) {
            val azimuth = i * PI / 17.0
            val signed = signedChordDeviation(azimuth)
            assert(signed > -0.5 * PI - 1e-12 && signed <= 0.5 * PI + 1e-12) {
                "signed deviation $signed out of (-pi/2, pi/2]"
            }
            // a chord is a line: adding half a turn changes nothing, and the two endpoints of
            // the fold interval are the SAME line, so the comparison is itself a folded one
            assert(
                abs(foldChordAngleToLine(signed - signedChordDeviation(azimuth + PI))) < 1e-12
            )
            // and its magnitude IS C-0042's folded misalignment
            assert(abs(abs(signed) - foldedChordMisalignment(azimuth, 0.5 * PI)) < 1e-12)
        }
    }

    @Test
    fun `gate 1 - the shared cap floor is an angle in the first octant and is symmetric`() {
        assert((sharedCapFloor(18.0 / degrees, 9.0 / degrees) * degrees).isCloseTo(4.5, 1e-9))
        assert((sharedCapFloor(9.0 / degrees, 18.0 / degrees) * degrees).isCloseTo(4.5, 1e-9))
        assert(sharedCapFloor(0.3, 0.3) < 1e-12)
        for (i in -20..20) {
            for (j in -20..20) {
                val floor = sharedCapFloor(i * PI / 41.0, j * PI / 41.0)
                assert(floor >= -1e-12 && floor <= 0.25 * PI + 1e-12) { "floor $floor out of [0, pi/4]" }
            }
        }
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { PinnedBaseRegister(halfWindowBasePairs = 0) }
        assertFailsWith<IllegalArgumentException> { PinnedBaseRegister(stepsPerBasePair = 3) }
        assertFailsWith<IllegalArgumentException> { PinnedBaseRegister(candidatesPerPosition = 0) }
        assertFailsWith<IllegalArgumentException> { chordSampleSpacing(IntRange.EMPTY) }
        assertFailsWith<IllegalArgumentException> {
            pinnedTrussDesign(0, 0.0, 0.0, 0.0, 0.0, 9)
        }
        assertFailsWith<IllegalArgumentException> {
            pinnedTrussDesign(12, 0.0, 0.0, -0.1, 0.0, 9)
        }
        assertFailsWith<IllegalArgumentException> {
            legBaseClassCensus(emptyList(), 9, 0.0, 24)
        }
        assertFailsWith<IllegalArgumentException> {
            legBaseClassCensus(c0063Stations(), 0, 0.0, 24)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - THE FREE CASE - an unpinned base reproduces C-0052's own leg budget exactly`() {
        for (steps in 12..26) {
            val design = pinnedTrussDesign(steps, 0.0, 0.0, 0.0, 0.0, 9)
            assert(design.baseDegrees < 1e-9)
            assert(design.capGeometricDegrees.isCloseTo(legBudgetDegrees(steps), 1e-9)) {
                "at $steps steps the pinned cap ${design.capGeometricDegrees} is not " +
                        "C-0052's budget ${legBudgetDegrees(steps)}"
            }
            assert(design.spentDegrees.isCloseTo(design.budgetDegrees, 1e-9))
            assert(abs(design.overspendDegrees) < 1e-9)
        }
    }

    @Test
    fun `gate 2 - THE FREE CASE - the unpinned composition reproduces C-0062's design table`() {
        // C-0062's best representable design: the 10 bp row, base 6.0 deg, cap 27.0 deg,
        // flexure 18.0 deg, best at 12 steps, carrying 2.446 on CanDo and 1.839 on Fields.
        var best: FeasibleTrussDesign? = null
        for (steps in 12..26) {
            val design = feasibleTrussDesign(
                legSteps = steps,
                baseFloor = 6.0 / degrees,
                capFloor = 27.0 / degrees,
                flexureFloor = 18.0 / degrees,
                separationBasePairs = 10
            )
            if (!design.representable) continue
            val incumbent = best
            if (incumbent == null || design.marginCanDo > incumbent.marginCanDo) best = design
        }
        val found = requireNotNull(best)
        assert(found.legSteps == 12) { "C-0062's best is 12 steps, got ${found.legSteps}" }
        assert(found.marginCanDo.isCloseTo(2.44607976, 1e-6))
        assert(found.marginFields.isCloseTo(1.83888014, 1e-6))
        assert(found.budgetDegrees.isCloseTo(45.126523, 1e-5))
    }

    @Test
    fun `gate 2 - THE FREE CASE - one instance reproduces C-0065's register at the 9 bp row`() {
        val pair = requireNotNull(register.nearestPair(9)) { "the 9 bp row admits no pair" }
        assert(pair.offsetFromStation.isCloseTo(0.17, 1e-6)) {
            "C-0065 registers the 9 bp row at +0.17 nm, got ${pair.offsetFromStation}"
        }
        assert((pair.worstMisalignment * degrees).isCloseTo(18.0, 1e-6)) {
            "C-0065 reads 18.0 deg there, got ${pair.worstMisalignment * degrees}"
        }
        // and the two legs are NOT the same: C-0065's table publishes the WORSE of them
        assert((pair.legA.first().misalignment * degrees).isCloseTo(9.0, 1e-6))
        assert((pair.legB.first().misalignment * degrees).isCloseTo(18.0, 1e-6))
    }

    @Test
    fun `gate 2 - the register's winner is BaseRegisterField's winner, position for position`() {
        val field = BaseRegisterField(halfWindowBasePairs = 3)
        val mine = PinnedBaseRegister(halfWindowBasePairs = 3)
        val theirs = field.positions
        val ours = mine.positions
        assert(theirs.size == ours.size)
        theirs.indices.forEach { i ->
            assert(theirs[i].closes == ours[i].closes) { "position $i disagrees on closure" }
            assert(
                theirs[i].misalignmentDegrees.isCloseTo(
                    (ours[i].winner?.misalignment ?: 0.0) * degrees, 1e-9
                )
            ) { "position $i disagrees on misalignment" }
        }
    }

    @Test
    fun `gate 2 - a leg with no admissible base has no design at all`() {
        val outcome = bestPinnedDesign(
            PinnedLegPair(9, 0.0, 0.0, emptyList(), emptyList()),
            capFloor = 0.0, flexureFloor = 0.0
        )
        assert(outcome.best == null)
        assert(outcome.candidatePairs == 0)
        assert(outcome.verdict.startsWith("NO"))
    }

    @Test
    fun `gate 2 - a base past the half right angle is not representable at any leg length`() {
        for (steps in 12..26) {
            val design = pinnedTrussDesign(steps, 57.0 / degrees, 57.0 / degrees, 0.0, 0.0, 10)
            assert(!design.representable)
            assert(design.marginCanDo == 0.0)
        }
    }

    // ------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 - a pinned base can only OVERSPEND the budget, never underspend it`() {
        for (steps in 12..26) {
            for (i in -30..30) {
                val delta = i * 0.25 * PI / 30.0
                val design = pinnedTrussDesign(steps, delta, delta, 0.0, 0.0, 9)
                assert(design.spentDegrees >= design.budgetDegrees - 1e-9) {
                    "at $steps steps and $delta rad the sum ${design.spentDegrees} " +
                            "underspends the budget ${design.budgetDegrees}"
                }
            }
        }
    }

    @Test
    fun `gate 3 - equality holds exactly when the pinned deviation opposes the budget's sense`() {
        // for a leg whose relative chord exceeds a right angle the reducing sense is negative
        var equalities = 0
        for (steps in 12..26) {
            for (sign in listOf(-1.0, 1.0)) {
                val delta = sign * 0.1 / degrees
                val design = pinnedTrussDesign(steps, delta, delta, 0.0, 0.0, 9)
                if (abs(design.overspendDegrees) < 1e-9) equalities++
            }
        }
        // exactly one of the two signs reduces at every length
        assert(equalities == 15) { "expected one reducing sign per length, got $equalities" }
    }

    @Test
    fun `gate 3 - the two legs' cap chords differ by a constant independent of the length`() {
        val a = 12.0 / degrees
        val b = -21.0 / degrees
        val expected = abs(signedChordDeviation(a - b, 0.0)) * degrees
        assert(expected.isCloseTo(33.0, 1e-9))
        for (steps in 12..26) {
            val design = pinnedTrussDesign(steps, a, b, 0.0, 0.0, 9)
            val gap = abs(
                foldChordAngleToLine(
                    (design.capASignedDegrees - design.capBSignedDegrees) / degrees
                )
            ) * degrees
            assert(gap.isCloseTo(expected, 1e-9)) {
                "at $steps steps the cap chords differ by $gap, not by $expected"
            }
        }
    }

    @Test
    fun `gate 3 - C-0037's frame couple conserves the sum of squared leg offsets at w over 2`() {
        val w = 9 * rise
        for (i in 0..12) {
            val theta = i * 0.5 * PI / 12.0
            val layout = TrussLayout(
                name = "two legs at $theta",
                legs = listOf(
                    LegOffset(0.5 * w * kotlin.math.cos(theta), 0.5 * w * kotlin.math.sin(theta)),
                    LegOffset(-0.5 * w * kotlin.math.cos(theta), -0.5 * w * kotlin.math.sin(theta))
                )
            )
            val along = layout.legs.sumOf { it.alongFlexure * it.alongFlexure }
            val across = layout.legs.sumOf { it.acrossFlexure * it.acrossFlexure }
            assert((along + across).isCloseTo(w * w / 2.0, 1e-9)) {
                "the frame couple budget is not conserved at $theta"
            }
        }
    }

    @Test
    fun `gate 3 - a chord is a line, so a half turn of either base moves nothing`() {
        for (steps in 12..26) {
            val plain = pinnedTrussDesign(steps, 0.3, -0.2, 0.1, 0.2, 9)
            val turned = pinnedTrussDesign(steps, 0.3 + PI, -0.2 - PI, 0.1, 0.2, 9)
            assert(plain.capDegrees.isCloseTo(turned.capDegrees, 1e-9))
            assert(plain.baseDegrees.isCloseTo(turned.baseDegrees, 1e-9))
            assert(plain.criticalLoadCanDo.isCloseTo(turned.criticalLoadCanDo, 1e-9))
        }
    }

    @Test
    fun `gate 3 - CHEAP BOUND 1 - the 68 leg bases occupy exactly two classes of thirty four`() {
        val census = legBaseClassCensus(c0063Stations(), 9, 0.5, 24)
        assert(census.legBases == 68) { "expected 68 leg bases, got ${census.legBases}" }
        assert(census.classes == 2) { "expected 2 phase classes, got ${census.classes}" }
        assert(census.populations.all { it == 34 }) { "classes are ${census.populations}" }
        assert(census.oneLengthServesAll)
    }

    @Test
    fun `gate 3 - CHEAP BOUND 2 - no leg length beats the two legs' own chord difference`() {
        val a = 18.0 / degrees
        val b = -9.0 / degrees
        val floor = sharedCapFloor(a, b)
        var bestWorst = Double.MAX_VALUE
        for (steps in 12..26) {
            val design = pinnedTrussDesign(steps, a, b, 0.0, 0.0, 9)
            bestWorst = minOf(bestWorst, design.capGeometricDegrees / degrees)
        }
        assert(bestWorst >= floor - 1e-12) {
            "the best worst cap ${bestWorst * degrees} is below the floor ${floor * degrees}"
        }
        assert(bestWorst <= floor + 0.5 * chordSampleSpacing(12..26) + 1e-9) {
            "the best worst cap ${bestWorst * degrees} is more than half a sample spacing " +
                    "above the floor ${floor * degrees}"
        }
    }

    // ------------------------------------------------------------------ gate 4 — convergence

    @Test
    fun `gate 4 - the leg envelope samples the chord circle at a known coarseness`() {
        assert((chordSampleSpacing(12..26) * degrees).isCloseTo(22.4367, 1e-3)) {
            "the 12-26 envelope samples at ${chordSampleSpacing(12..26) * degrees} deg"
        }
        // a wider envelope can only sample more finely
        assert(chordSampleSpacing(12..40) <= chordSampleSpacing(12..26) + 1e-12)
        assert((chordSampleSpacing(12..40) * degrees).isCloseTo(11.134, 1e-2))
    }

    @Test
    fun `gate 4 - the register is deterministic and memoised`() {
        val field = PinnedBaseRegister(halfWindowBasePairs = 2)
        val first = field.positions.map { it.closers.size }
        val solves = field.solves
        val second = field.positions.map { it.closers.size }
        assert(first == second)
        assert(field.solves == solves) { "a memoised field must not re-solve" }
    }

    @Test
    fun `gate 4 - a wider candidate cap never loses a closing position`() {
        val narrow = PinnedBaseRegister(halfWindowBasePairs = 2, candidatesPerPosition = 4)
        val wide = PinnedBaseRegister(halfWindowBasePairs = 2, candidatesPerPosition = 12)
        narrow.positions.indices.forEach { i ->
            if (narrow.positions[i].closers.isNotEmpty()) {
                assert(wide.positions[i].closers.isNotEmpty()) {
                    "position $i closes at 4 candidates and not at 12"
                }
            }
        }
    }

    // ------------------------------------------------------ gate 5 — literature and upstream

    @Test
    fun `gate 5 - C-0052's published leg budgets come back through this composition`() {
        assert(pinnedTrussDesign(21, 0.0, 0.0, 0.0, 0.0, 7).budgetDegrees.isCloseTo(78.53, 1e-2))
        assert(pinnedTrussDesign(24, 0.0, 0.0, 0.0, 0.0, 7).budgetDegrees.isCloseTo(0.25, 2e-2))
        assert(pinnedTrussDesign(16, 0.0, 0.0, 0.0, 0.0, 7).budgetDegrees.isCloseTo(89.8, 1e-2))
    }

    @Test
    fun `gate 5 - C-0048's cap terms are the row's, not the crossbar's`() {
        val w = 10 * rise
        assert(
            capBendingStiffness(Gen1Tile.DUPLEX_BENDING_RIGIDITY, w, 12.0)
                .isCloseTo(811.764706, 1e-5)
        )
        assert(
            capTorsionalStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, w)
                .isCloseTo(541.176471, 1e-5)
        )
    }

    @Test
    fun `gate 5 - the pinned design carries the same cap terms as C-0062's composition`() {
        val pinned = pinnedTrussDesign(12, 6.0 / degrees, 6.0 / degrees, 27.0 / degrees, 18.0 / degrees, 10)
        assert(pinned.capBending.isCloseTo(811.764706, 1e-5))
        assert(pinned.capTorsion.isCloseTo(541.176471, 1e-5))
    }
}
