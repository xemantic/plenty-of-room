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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import org.openrndr.math.Vector3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-117` — whether **three** 90° junctions close on **one** crossbar duplex.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The spine of the task is that `C-0048`'s cap is a **lone, short** seat: it has no lattice
 * neighbours and no crossover phase, but it does have an **axial rim**, its own **continuous**
 * helical phase, and three junctions arriving in two different directions. And a leg is **one body
 * with two junctions**, so the azimuths of its base chord and its cap chord are not independent.
 */
class CrossbarJunctionTrioTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR
    private val backbone = DuplexBackbone()

    /** A small, fast grid, so the gates run in the suite rather than in a study. */
    private fun search(
        crossbarBasePairs: Int = 15,
        separationBasePairs: Int = 7,
        junctions: List<TrioJunctionSpec> = TrioJunctionSpec.cap(7, rise),
        azimuthSteps: Int = 120,
        refinements: Int = 2,
        phaseSteps: Int = 90,
        axialSteps: Int = 4,
        lateralSeats: List<Double> = listOf(-0.4, -0.2, 0.0, 0.2, 0.4)
    ) = CrossbarTrioSearch(
        backbone = backbone,
        crossbarBasePairs = crossbarBasePairs,
        separationBasePairs = separationBasePairs,
        junctions = junctions,
        azimuthSteps = azimuthSteps,
        refinements = refinements,
        phaseSteps = phaseSteps,
        axialSteps = axialSteps,
        lateralSeats = lateralSeats
    )

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 dimensional - a truncated seat contact is a length and never exceeds the untruncated one`() {
        // far from the rim it IS C-0042's contact
        assert(boundedSeatContactLength(0.0, 10.0, 0.0, 1.0).isCloseTo(2.0))
        assert(boundedSeatContactLength(0.0, 10.0, 0.6, 1.0).isCloseTo(2.0 * sqrt(1.0 - 0.36)))
        // the standoff radius scales it
        assert(boundedSeatContactLength(0.0, 10.0, 0.0, 2.0).isCloseTo(4.0))
        // and it never exceeds the seat's own length
        assert(boundedSeatContactLength(0.0, 1.0, 0.0, 1.0).isCloseTo(1.0))
    }

    @Test
    fun `gate 1 dimensional - the chord twist is an angle per base pair and doubles when the twist does`() {
        val square = DuplexBackbone(basePairsPerTurn = 10.67)
        val half = DuplexBackbone(basePairsPerTurn = 5.335)
        assert(relativeChordAzimuth(1, square).isCloseTo(square.twistPerBasePair))
        assert(relativeChordAzimuth(1, half).isCloseTo(2.0 * square.twistPerBasePair))
        assert(relativeChordAzimuth(0, square).isCloseTo(0.0))
    }

    @Test
    fun `gate 1 dimensional - a duplex free energy is a step energy times steps plus one initiation`() {
        val one = duplexFreeEnergy(13, -1.42, 1.03)
        assert(one.isCloseTo(12.0 * -1.42 + 1.03))
        // doubling the step energy doubles the propagation term and leaves the initiation alone
        val two = duplexFreeEnergy(13, -2.84, 1.03)
        assert((two - 1.03).isCloseTo(2.0 * (one - 1.03)))
        // one base pair is initiation and no propagation at all
        assert(duplexFreeEnergy(1, -1.42, 1.03).isCloseTo(1.03))
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw at every entry point`() {
        assertFailsWith<IllegalArgumentException> { boundedSeatContactLength(0.0, -1.0, 0.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { boundedSeatContactLength(0.0, 1.0, 0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { loneSeatFaceHeight(0.0, -1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { relativeChordAzimuth(-1, backbone) }
        assertFailsWith<IllegalArgumentException> { duplexFreeEnergy(0, -1.42, 1.03) }
        assertFailsWith<IllegalArgumentException> { duplexFreeEnergy(13, 1.42, 1.03) }
        assertFailsWith<IllegalArgumentException> { CrossbarGeometry(3, 7) }
        assertFailsWith<IllegalArgumentException> { CrossbarTrioSearch(crossbarBasePairs = 0) }
        // a "target" that destabilises is not a target
        assertFailsWith<IllegalArgumentException> {
            basePairsForFreeEnergy(16.0, -1.42, 1.03)
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 limiting - a lone seat's face height is the seat radius everywhere inside its rim`() {
        assert(loneSeatFaceHeight(0.0, 1.0, 1.0).isCloseTo(1.0))
        assert(loneSeatFaceHeight(0.9, 1.0, 1.0).isCloseTo(1.0))
        // and it is C-0042's seatFaceHeight with the neighbours pushed to infinity
        listOf(0.0, 0.3, 0.6, 0.9, 1.4).forEach { offset ->
            assert(
                loneSeatFaceHeight(offset, 1.0, 1.0)
                    .isCloseTo(seatFaceHeight(offset, 1.0, 1.0, 1.0e6))
            )
        }
    }

    @Test
    fun `gate 2 limiting - the truncated contact collapses to zero at the rim and is even in the offset`() {
        val geometry = CrossbarGeometry(13, 7)
        // a junction whose axis sits one radius outside the rim touches nothing
        assert(boundedSeatContactLength(geometry.halfLength + 1.0, geometry.length, 0.0, 1.0) == 0.0)
        // exactly on the rim it keeps half its contact
        assert(boundedSeatContactLength(geometry.halfLength, geometry.length, 0.0, 1.0).isCloseTo(1.0))
        // and it is even in the lateral offset, as C-0042's is
        assert(
            boundedSeatContactLength(0.0, 10.0, 0.5, 1.0)
                .isCloseTo(boundedSeatContactLength(0.0, 10.0, -0.5, 1.0))
        )
    }

    @Test
    fun `gate 2 limiting - two coaxial solids separated along their common axis are exactly apart`() {
        val lower = SolidCylinder(Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, -1.0), 1.0)
        val upper = SolidCylinder(Vector3(0.0, 0.0, 2.0), Vector3(0.0, 0.0, 1.0), 1.0)
        assert(minimumSolidSeparation(lower, upper).isCloseTo(2.0, 1e-6))
        // touching faces give exactly zero
        val touching = SolidCylinder(Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, 1.0), 1.0)
        assert(abs(minimumSolidSeparation(lower, touching)) <= 1e-6)
        // and two parallel cylinders side by side are apart by their axis distance less two radii
        val left = SolidCylinder(Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, -1.0), 1.0)
        val right = SolidCylinder(Vector3(3.0, 0.0, 0.0), Vector3(0.0, 0.0, -1.0), 1.0)
        assert(minimumSolidSeparation(left, right).isCloseTo(1.0, 1e-6))
    }

    @Test
    fun `gate 2 limiting - the chord twist folds like a line and returns to zero after a helical repeat`() {
        // a chord is a LINE, so the relative azimuth lives in [0, pi)
        (0..40).forEach { steps ->
            val value = relativeChordAzimuth(steps, backbone)
            assert(value >= 0.0 && value < PI + 1e-12)
        }
        // 32 bp is three turns of the square lattice, so the relative chord is back where it started
        assert(chordPairMisalignment(32, 0.0, backbone) < 0.01)
        // and the misalignment never exceeds a right angle
        (0..40).forEach { steps ->
            assert(chordPairMisalignment(steps, 0.5 * PI, backbone) <= 0.5 * PI + 1e-12)
        }
    }

    @Test
    fun `gate 2 limiting - a leg whose chord twist is a right angle delivers C-0048's recommended azimuth pair`() {
        // C-0048 wants the base chord ALONG the flexure axis and the cap chord ACROSS it — 90 apart
        val best = bestChordPairSteps(12..26, 0.5 * PI, backbone)
        assert(chordPairMisalignment(best, 0.5 * PI, backbone) < 0.01)
        // and C-0037's own 7.00 nm leg, rounded to base pairs, is nowhere near it
        assert(chordPairMisalignment(21, 0.5 * PI, backbone) > 1.3)
    }

    @Test
    fun `gate 2 limiting - the trio search reduces to C-0042's pair when the flexure is removed`() {
        // two legs, no flexure, a long crossbar, and C-0042's own wanted chord — along the FLEXURE
        // axis rather than along the seat — must reproduce its perfect alignment and its verdict
        val pair = search(
            crossbarBasePairs = 33,
            junctions = TrioJunctionSpec.legRowOnly(7, rise, wantedChordAzimuth = 0.5 * PI)
        ).best()
        assert(pair != null)
        assert(pair!!.allCovalent)
        assert(pair.worstMisalignment < 1.0e-3)
        assert(pair.worstGap <= BForm.PHOSPHODIESTER_STEP + 1e-12)
        assert(pair.worstGap >= BForm.PHOSPHODIESTER_STEP_MIN - 1e-12)
    }

    @Test
    fun `gate 2 limiting - a crossbar too short for the row does not exist`() {
        assertFailsWith<IllegalArgumentException> { CrossbarGeometry(6, 7) }
        // and C-0048's own minimum is recovered from the row
        assert(CrossbarGeometry(13, 7).minimumBasePairs == 13)
    }

    @Test
    fun `gate 2 limiting - the flexure clears the legs by a positive margin at every admissible row`() {
        (6..12).forEach { separation ->
            val clearance = CrossbarGeometry(separation + 6, separation).legFlexureClearance
            assert(clearance > 0.0)
        }
        // and a wider row clears more than a narrow one
        assert(
            CrossbarGeometry(18, 12).legFlexureClearance >
                    CrossbarGeometry(12, 6).legFlexureClearance
        )
    }

    // ------------------------------------------------------------------ gate 3: symmetry

    @Test
    fun `gate 3 symmetry - the chord azimuth is a function of the junction's own azimuth alone`() {
        val trio = search().best()
        assert(trio != null)
        trio!!.placements.forEach { placement ->
            val expected = placement.azimuth + 0.5 * backbone.minorGrooveAngle * PI / 180.0 + 0.5 * PI
            assert(foldedChordMisalignment(placement.chordAzimuth, expected) < 1.0e-9)
        }
    }

    @Test
    fun `gate 3 symmetry - the relative chord azimuth is additive in the base pair steps`() {
        // the twist between two ends is a group action, so m + n steps is m steps then n steps
        listOf(3 to 5, 7 to 11, 13 to 8).forEach { (m, n) ->
            val direct = relativeChordAzimuth(m + n, backbone)
            val composed = (relativeChordAzimuth(m, backbone) + relativeChordAzimuth(n, backbone)) % PI
            assert(abs(direct - composed) < 1.0e-9 || abs(abs(direct - composed) - PI) < 1.0e-9)
        }
    }

    @Test
    fun `gate 3 symmetry - the base and cap misalignments trade one for one and their sum is conserved`() {
        // rotating a leg about its own axis moves both chords together, so only their DIFFERENCE
        // is quantised — this is the conservation statement the design's azimuth choice rests on
        val steps = 21
        val budget = chordPairMisalignment(steps, 0.5 * PI, backbone)
        assert(budget > 1.3)
        // rotating in the sense that takes error OFF the cap puts exactly as much ON the base
        listOf(-0.1, -0.2, -0.5, -0.7).forEach { rotation ->
            val split = legAzimuthSplit(steps, rotation, backbone)
            assert(split.budget.isCloseTo(budget, 1e-9))
        }
        // and rotating the other way only spends more
        assert(legAzimuthSplit(steps, 0.2, backbone).budget > budget)
        // and at zero rotation the whole budget sits on the cap
        val none = legAzimuthSplit(steps, 0.0, backbone)
        assert(none.baseMisalignment.isCloseTo(0.0))
        assert(none.capMisalignment.isCloseTo(budget))
    }

    @Test
    fun `gate 3 symmetry - the trio does not care in which order its junctions are listed`() {
        val forward = search().best()
        val reversed = search(junctions = TrioJunctionSpec.cap(7, rise).reversed()).best()
        assert(forward != null && reversed != null)
        assert(forward!!.worstResidual.isCloseTo(reversed!!.worstResidual, 1e-12))
        assert(abs(forward.worstMisalignment - reversed.worstMisalignment) < 1e-12)
    }

    @Test
    fun `gate 3 symmetry - a chord is a line, so a half turn of a junction leaves its misalignment alone`() {
        val spec = TrioJunctionSpec.cap(7, rise).first()
        listOf(0.3, 1.1, 2.4, 5.0).forEach { azimuth ->
            val one = chordMisalignmentOf(azimuth, spec.wantedChordAzimuth, backbone)
            val other = chordMisalignmentOf(azimuth + PI, spec.wantedChordAzimuth, backbone)
            assert(one.isCloseTo(other, 1e-12))
        }
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 convergence - the search returns the identical trio on a repeat call`() {
        val engine = search()
        val first = engine.best()
        val second = engine.best()
        assert(first != null && second != null)
        assert(first!!.helicalPhase.isCloseTo(second!!.helicalPhase, 0.0))
        assert(first.axialPhase.isCloseTo(second.axialPhase, 0.0))
        assert(first.worstResidual.isCloseTo(second.worstResidual, 0.0))
        assert(first.worstMisalignment.isCloseTo(second.worstMisalignment, 0.0))
    }

    @Test
    fun `gate 4 convergence - the verdict does not move when both continuous grids are refined`() {
        val coarse = search(azimuthSteps = 90, phaseSteps = 45, axialSteps = 2).best()
        val fine = search(azimuthSteps = 180, phaseSteps = 180, axialSteps = 8).best()
        assert((coarse != null) == (fine != null))
        if (coarse != null && fine != null) {
            assert(coarse.allCovalent == fine.allCovalent)
            // a finer grid can only find an at-least-as-good optimum
            assert(fine.worstMisalignment <= coarse.worstMisalignment + 1.0e-9)
        }
    }

    @Test
    fun `gate 4 convergence - the solid separation converges and is symmetric in its arguments`() {
        val leg = SolidCylinder(Vector3(1.19, 0.0, -1.0), Vector3(0.0, 0.0, -1.0), 1.0)
        val flexure = SolidCylinder(Vector3(0.0, -1.0, 0.0), Vector3(0.0, -1.0, 0.0), 1.0)
        val coarse = minimumSolidSeparation(leg, flexure, iterations = 200)
        val fine = minimumSolidSeparation(leg, flexure, iterations = 4000)
        assert(abs(coarse - fine) < 1.0e-6)
        assert(minimumSolidSeparation(flexure, leg, iterations = 4000).isCloseTo(fine, 1e-6))
    }

    // ------------------------------------------------------------------ gate 5: upstream

    @Test
    fun `gate 5 upstream - C-0029's window, radius and step are what this search actually uses`() {
        assert(BForm.PHOSPHODIESTER_STEP_MIN.isCloseTo(0.60))
        assert(BForm.PHOSPHODIESTER_STEP.isCloseTo(0.70))
        assert(BForm.PHOSPHATE_RADIUS.isCloseTo(1.00))
        assert(Gen1Tile.RISE_PER_BASE_PAIR.isCloseTo(0.34))
        assert(backbone.twistPerBasePair.isCloseTo(2.0 * PI / 10.67))
    }

    @Test
    fun `gate 5 upstream - C-0048's crossbar geometry is reproduced from the row`() {
        val geometry = CrossbarGeometry(13, 7)
        assert(geometry.legSeparation.isCloseTo(2.38))
        assert(geometry.minimumLength.isCloseTo(4.38, 1e-9))
        assert(geometry.minimumBasePairs == 13)
        assert(geometry.junctionCount == 3)
        assert(geometry.covalentLinkCount == 6)
        // C-0048 quotes "13 bp = 4.38 nm"; 13 bp is 4.42 nm and 4.38 is the DEMAND
        assert(geometry.length.isCloseTo(4.42, 1e-9))
    }

    @Test
    fun `gate 5 upstream - C-0042's chord budget is conserved at every cap azimuth`() {
        val hard = DuplexBackbone(minorGrooveAngle = 180.0)
        val budget = chordBaseAxes(hard, 0.0).total
        assert(budget.isCloseTo(91.76, 1e-3))
        listOf(0.0, 0.3, 0.7, 1.1, 0.5 * PI).forEach { misalignment ->
            assert(chordBaseAxes(hard, misalignment).total.isCloseTo(budget, 1e-12))
        }
    }

    @Test
    fun `gate 5 upstream - SantaLucia's unified parameters are the ones tabulated here`() {
        // SantaLucia (1998) PNAS 95:1460, Tables 1 and 2, READ DIRECTLY from PMC19045
        assert(UnifiedNearestNeighbour.AA_TT.isCloseTo(-1.00))
        assert(UnifiedNearestNeighbour.TA_AT.isCloseTo(-0.58))
        assert(UnifiedNearestNeighbour.GC_CG.isCloseTo(-2.24))
        assert(UnifiedNearestNeighbour.AVERAGE.isCloseTo(-1.42))
        assert(UnifiedNearestNeighbour.INITIATION_TERMINAL_GC.isCloseTo(0.98))
        assert(UnifiedNearestNeighbour.INITIATION_TERMINAL_AT.isCloseTo(1.03))
        // the ten steps average to the tabulated average, which is a check on the transcription
        assert(UnifiedNearestNeighbour.STEPS.average().isCloseTo(-1.42, 2e-3))
        // and one kcal/mol is 1.678 k_BT at 300 K, derived and not quoted
        assert(KCAL_PER_MOL_IN_KT.isCloseTo(6.94769e-21 / 4.141947e-21, 1e-4))
    }

    @Test
    fun `gate 5 upstream - C-0048's recommended design is reproduced through this task's own assembly`() {
        // leg 7.00 nm, 7 bp row, base chord ALONG the flexure axis and cap chord ACROSS it —
        // C-0048's recommended row, which this assembly must return without being told the answer
        // the values are `Sy7`/`H8` of gpd/results/T-106-truss-cap.json, to that file's own rounding
        val design = capDesign(7.00, 7, 0.0, 0.0, 0.0)
        assert(design.frameCouple.isCloseTo(71.3131298, 1e-8))
        assert(design.span.isCloseTo(28.2512884, 1e-8))
        assert(design.tangent.isCloseTo(30.9319239, 1e-8))
        assert(design.supplyToDemand.isCloseTo(1.81394143, 1e-8))
        assert(design.duty.isCloseTo(4.59624041, 1e-8))
        assert(design.loadedCriticalLoad.isCloseTo(8.94739526, 1e-8))
        assert(design.freeCriticalLoad.isCloseTo(9.23647708, 1e-8))
        assert(design.marginCanDo.isCloseTo(1.94667695, 1e-8))
        assert(design.marginFields.isCloseTo(1.46344598, 1e-8))
        assert(design.verdict == "PASS")
    }

    @Test
    fun `gate 5 upstream - C-0048's other cap azimuth is reproduced too, and it is the worse corner`() {
        // the cap chord laid ALONG the flexure axis: C-0048's sensitivity row, 6.20 pN free
        val along = capDesign(7.00, 7, 0.0, 0.5 * PI, 0.0)
        assert(along.freeCriticalLoad.isCloseTo(6.20025918, 1e-8))
        assert(along.marginCanDo.isCloseTo(1.48333669, 1e-8))
        assert(along.marginFields.isCloseTo(1.11512242, 1e-8))
        // and it is strictly worse than the across-axis one, which is the whole design choice
        assert(along.marginCanDo < capDesign(7.00, 7, 0.0, 0.0, 0.0).marginCanDo)
    }

    @Test
    fun `gate 2 limiting - a leg's quantised design is the free one only where the twist permits`() {
        // C-0037's own 7.00 nm leg rounds to 21 steps, whose chord budget is nearly a right angle
        val worst = quantisedCapDesign(21, 7)
        val bestSteps = bestChordPairSteps(12..26, 0.5 * PI, backbone)
        assert(chordPairMisalignment(bestSteps, 0.5 * PI, backbone) < 0.01)
        assert(chordPairMisalignment(21, 0.5 * PI, backbone) > 1.3)
        // the optimal split spends the whole budget and no more — it cannot make it go away
        assert(
            (worst.baseMisalignment + worst.capMisalignment)
                .isCloseTo(chordPairMisalignment(21, 0.5 * PI, backbone), 1e-6)
        )
        // and every quantised design is still a design: it returns a verdict, not a failure
        assert(worst.verdict.isNotEmpty())
        assert(quantisedCapDesign(bestSteps, 7).verdict.isNotEmpty())
    }

    @Test
    fun `gate 3 symmetry - the flexure's own two ends carry the same budget and split it evenly`() {
        // both ends of one flexure want a VERTICAL chord, so their demand is parallel chords and
        // the price is even in the split — the optimum is therefore half each, by symmetry
        val steps = 76
        val budget = chordPairMisalignment(steps, 0.0, backbone)
        val even = legAzimuthSplit(steps, 0.0, backbone)
        assert(even.budget.isCloseTo(budget, 1e-9) || even.budget >= budget - 1e-9)
        // a chord lattice of 33.74 degrees per base pair always has a step within half a quantum
        assert(chordPairMisalignment(bestChordPairSteps(70..85, 0.0, backbone), 0.0, backbone) < 0.3)
    }

    @Test
    fun `gate 5 upstream - Huguet's magnesium parameters are the ones tabulated here`() {
        // Huguet et al., NAR 45:12921 (2017), Table 1, unzipping column, 298 K, READ DIRECTLY
        assert(MagnesiumNearestNeighbour.AA_TT.isCloseTo(-1.69))
        assert(MagnesiumNearestNeighbour.TA_AT.isCloseTo(-1.38))
        assert(MagnesiumNearestNeighbour.GC_CG.isCloseTo(-2.74))
        assert(MagnesiumNearestNeighbour.CC_GG.isCloseTo(-2.18))
        assert(MagnesiumNearestNeighbour.saltFactor(MagnesiumNearestNeighbour.TA_AT)
            .isCloseTo(0.087))
        // every magnesium step is MORE stabilising than SantaLucia's 1 M NaCl reading, which is
        // the paper's own headline — the UO set underestimates duplex stability in magnesium
        assert(MagnesiumNearestNeighbour.STEPS.average() < UnifiedNearestNeighbour.STEPS.average())
    }

    @Test
    fun `gate 2 limiting - the salt correction destabilises below one molar and vanishes at it`() {
        val step = MagnesiumNearestNeighbour.AA_TT
        val m = MagnesiumNearestNeighbour.saltFactor(step)
        // at the reference concentration the correction is exactly zero, on both log conventions
        assert(saltCorrectedStepFreeEnergy(step, m, 1.0, true).isCloseTo(step))
        assert(saltCorrectedStepFreeEnergy(step, m, 1.0, false).isCloseTo(step))
        // and below it the step is strictly less stabilising, more so on the natural logarithm
        val natural = saltCorrectedStepFreeEnergy(step, m, 0.002, true)
        val decimal = saltCorrectedStepFreeEnergy(step, m, 0.002, false)
        assert(natural > step)
        assert(decimal > step)
        assert(natural > decimal)
        assertFailsWith<IllegalArgumentException> { saltCorrectedStepFreeEnergy(step, m, 0.0, true) }
    }

    @Test
    fun `gate 5 upstream - the length a weak sequence needs is longer than the one an average needs`() {
        val target = duplexFreeEnergy(13, UnifiedNearestNeighbour.AVERAGE, 1.03)
        val weak = basePairsForFreeEnergy(target, UnifiedNearestNeighbour.TA_AT, 1.03)
        val strong = basePairsForFreeEnergy(target, UnifiedNearestNeighbour.GC_CG, 1.03)
        assert(weak > 13)
        assert(strong < 13)
        assert(duplexFreeEnergy(weak, UnifiedNearestNeighbour.TA_AT, 1.03) <= target)
    }
}
