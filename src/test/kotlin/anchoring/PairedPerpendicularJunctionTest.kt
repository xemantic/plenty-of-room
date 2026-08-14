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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-97` — whether **two** 90° junctions close on **one** sheet duplex 6–8 bp apart.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The spine of the task is that a seat duplex is a **helix**, so the sheet phosphates the second
 * junction has to reach are rotated by `n × 33.74°` relative to the first's — while the standoff
 * itself must stay **normal** to the sheet. The second junction is therefore *not* a screw image of
 * the first, and whether it can be seated with its base chord still on the flexure's axis is a
 * two-parameter search against two 0.1 nm windows.
 */
class PairedPerpendicularJunctionTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY
    private val rise = Gen1Tile.RISE_PER_BASE_PAIR
    private val backbone = DuplexBackbone()

    /** A small, fast lattice and grid, so the gates run in the suite rather than in a study. */
    private fun search(
        axialStepsPerBasePair: Int = 4,
        azimuthSteps: Int = 180,
        refinements: Int = 2
    ) = PairedJunctionSearch(
        backbone = backbone,
        axialStepsPerBasePair = axialStepsPerBasePair,
        azimuthSteps = azimuthSteps,
        refinements = refinements,
        lateralSeats = listOf(-0.4, -0.2, 0.0, 0.2, 0.4)
    )

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 dimensional - the steric floor is a diameter over a rise, so halving the rise doubles it`() {
        assert(pairStericFloorBasePairs(1.0, 0.34) == 6)
        assert(pairStericFloorBasePairs(1.0, 0.17) == 12)
        assert(pairStericFloorBasePairs(0.5, 0.34) == 3)
        // it is a ceiling of 2R/rise, never below it
        assert(pairStericFloorBasePairs(1.0, 0.34) * 0.34 >= 2.0)
    }

    @Test
    fun `gate 1 dimensional - the seat contact is a length and scales with the standoff radius`() {
        assert(seatContactLength(0.0, 1.0).isCloseTo(2.0))
        assert(seatContactLength(0.0, 2.0).isCloseTo(4.0))
        assert(seatContactLength(0.5, 1.0).isCloseTo(2.0 * sqrt(0.75)))
        assert(seatContactLength(1.0, 2.0).isCloseTo(2.0 * sqrt(3.0)))
    }

    @Test
    fun `gate 1 dimensional - a chord couple is a slide stiffness times a squared lever arm`() {
        val one = chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0)
        val half = chordBaseAxes(
            DuplexBackbone(minorGrooveAngle = 180.0, phosphateRadius = 0.5), 0.0
        )
        val hinge = 2.0 * bondHingeStiffness()
        // the couple part alone quarters when the lever arm halves
        assert(((one.loaded - hinge) / (half.loaded - hinge)).isCloseTo(4.0, 1e-12))
    }

    @Test
    fun `gate 1 dimensional - a mixed-base critical load is a rigidity over a squared length`() {
        val bases = listOf(78.24, 13.53)
        // rho_b = k_b l / EI, so halving the length at FIXED restraint doubles every k_b
        val short = mixedBaseTrussBucklingLoad(ei, 4.0, bases.map { it * 2.0 }, 0.0, 32)
        val long = mixedBaseTrussBucklingLoad(ei, 8.0, bases, 0.0, 32)
        assert((short / long).isCloseTo(4.0, 1e-6))
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { pairStericFloorBasePairs(-1.0, 0.34) }
        assertFailsWith<IllegalArgumentException> { pairStericFloorBasePairs(1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { seatContactLength(0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { chordBaseAxes(backbone, -0.1) }
        assertFailsWith<IllegalArgumentException> {
            mixedBaseTrussBucklingLoad(ei, 8.0, emptyList(), 0.0, 32)
        }
        assertFailsWith<IllegalArgumentException> {
            mixedBaseTrussBucklingLoad(ei, 8.0, listOf(78.24), 0.0, 0)
        }
        assertFailsWith<IllegalArgumentException> {
            mixedBaseTrussBucklingLoad(ei, 0.0, listOf(78.24), 0.0, 32)
        }
        assertFailsWith<IllegalArgumentException> { crossoverFreePhaseCount(emptyList()) }
        assertFailsWith<IllegalArgumentException> { sheetPhaseResidual(-1) }
        assertFailsWith<IllegalArgumentException> { screwImageChordRotation(-1) }
        assertFailsWith<IllegalArgumentException> {
            PairedJunctionSearch(targetDuplexes = emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            PairedJunctionSearch(centringWeight = -1.0)
        }
    }

    @Test
    fun `the strict reading - both junctions grounded on ONE sheet duplex still fits`() {
        val strict = PairedJunctionSearch(
            backbone = backbone, axialStepsPerBasePair = 4, azimuthSteps = 180, refinements = 2,
            lateralSeats = listOf(-0.4, -0.2, 0.0, 0.2, 0.4), targetDuplexes = listOf(0)
        )
        val pair = strict.bestPair(7)
        assert(pair != null)
        assert(pair!!.bothCovalent && pair.stericallyClear && pair.distinctTargets)
        assert(
            listOf(
                pair.first.firstTarget, pair.first.secondTarget,
                pair.second.firstTarget, pair.second.secondTarget
            ).all { it.duplex == 0 }
        )
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 limiting - the screw image rotation vanishes at zero and at a full helical repeat`() {
        assert(screwImageChordRotation(0, backbone).isCloseTo(0.0))
        // 32 bp is three turns of the square lattice: the screw image comes back onto itself
        assert(abs(screwImageChordRotation(32, backbone)) < 0.01)
        // it never leaves [0, pi/2] — a chord is a line, not a vector
        (0..40).forEach {
            val fold = screwImageChordRotation(it, backbone)
            assert(fold >= 0.0 && fold <= 0.5 * PI + 1e-12)
        }
    }

    @Test
    fun `gate 2 limiting - the seat contact collapses to a point at the rim and is even in the offset`() {
        assert(seatContactLength(1.0, 1.0).isCloseTo(0.0))
        assert(seatContactLength(1.5, 1.0).isCloseTo(0.0))
        assert(seatContactLength(0.3, 1.0).isCloseTo(seatContactLength(-0.3, 1.0)))
    }

    @Test
    fun `gate 2 limiting - the loaded couple fraction is one on the axis and zero across it`() {
        assert(loadedPlaneCoupleFraction(0.0).isCloseTo(1.0))
        assert(loadedPlaneCoupleFraction(0.5 * PI).isCloseTo(0.0))
        assert(loadedPlaneCoupleFraction(0.3).isCloseTo(loadedPlaneCoupleFraction(-0.3)))
    }

    @Test
    fun `gate 2 limiting - one leg with no frame reproduces C-0028's own sway column`() {
        listOf(5.0, 7.0, 8.0, 10.0).forEach { length ->
            val restraint = baseRestraintParameter(78.24, ei, length)
            val analytic = standoffBucklingLoad(ei, length, restraint, 0.0)
            val finite = mixedBaseTrussBucklingLoad(ei, length, listOf(78.24), 0.0, 64)
            assert(finite.isCloseTo(analytic, 2e-4))
        }
    }

    @Test
    fun `gate 2 limiting - equal bases reproduce C-0037's own truss buckling load`() {
        val base = TwoLinkBase.realisable()
        listOf(1, 2, 3).forEach { legs ->
            listOf(0.0, 40.0, 200.0).forEach { frame ->
                val published = trussBucklingLoad(ei, 8.0, base.restrainedAxis, legs, frame)
                val finite = mixedBaseTrussBucklingLoad(
                    ei, 8.0, List(legs) { base.restrainedAxis }, frame, 64
                )
                assert(finite.isCloseTo(published, 2e-4))
            }
        }
    }

    @Test
    fun `gate 2 limiting - equal bases reproduce C-0037's assembled tip flexibility entry by entry`() {
        listOf(1, 2, 3).forEach { legs ->
            listOf(0.0, 96.88, 500.0).forEach { frame ->
                val published = trussTipFlexibility(ei, 8.0, 78.24, legs, frame)
                val mixed = mixedBaseTrussTipFlexibility(ei, 8.0, List(legs) { 78.24 }, frame)
                assert(mixed.translationUnderForce.isCloseTo(published.translationUnderForce, 1e-12))
                assert(
                    mixed.translationUnderMoment.isCloseTo(published.translationUnderMoment, 1e-12)
                )
                assert(mixed.rotationUnderForce.isCloseTo(published.rotationUnderForce, 1e-12))
                assert(mixed.rotationUnderMoment.isCloseTo(published.rotationUnderMoment, 1e-12))
            }
        }
    }

    @Test
    fun `gate 2 limiting - a mixed truss lies strictly between its all-weak and all-strong readings`() {
        val strong = 78.24
        val weak = 13.53
        val allStrong = mixedBaseTrussBucklingLoad(ei, 8.0, listOf(strong, strong), 0.0, 48)
        val allWeak = mixedBaseTrussBucklingLoad(ei, 8.0, listOf(weak, weak), 0.0, 48)
        val mixed = mixedBaseTrussBucklingLoad(ei, 8.0, listOf(strong, weak), 0.0, 48)
        assert(mixed > allWeak)
        assert(mixed < allStrong)
    }

    @Test
    fun `gate 2 limiting - pinned bases with no frame couple are a mechanism, not a strut`() {
        assert(mixedBaseTrussBucklingLoad(ei, 8.0, listOf(0.0, 0.0), 0.0, 32).isCloseTo(0.0, 1e-6))
    }

    @Test
    fun `gate 2 limiting - a chord on the flexure axis has no misalignment`() {
        assert(foldedChordMisalignment(0.5 * PI).isCloseTo(0.0))
        assert(foldedChordMisalignment(-0.5 * PI).isCloseTo(0.0))
        assert(foldedChordMisalignment(0.0).isCloseTo(0.5 * PI))
    }

    // --------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 conservation - the chord's two axes are a rank-one tensor and their sum is invariant`() {
        val hard = DuplexBackbone(minorGrooveAngle = 180.0)
        val total = chordBaseAxes(hard, 0.0).total
        listOf(0.0, 0.1, 0.4, 0.7, 1.0, 1.3, 0.5 * PI).forEach {
            val axes = chordBaseAxes(hard, it)
            assert(axes.total.isCloseTo(total, 1e-12))
            assert(axes.loaded.isCloseTo(2.0 * bondHingeStiffness() +
                    2.0 * bondSlideStiffness() * hard.leverArm * hard.leverArm * cos(it) * cos(it),
                1e-12))
        }
    }

    @Test
    fun `gate 3 conservation - a chord is a line, so its misalignment is invariant under a half turn`() {
        listOf(0.0, 0.3, 1.1, 2.5, -0.8).forEach {
            assert(foldedChordMisalignment(it).isCloseTo(foldedChordMisalignment(it + PI), 1e-12))
            assert(foldedChordMisalignment(it) <= 0.5 * PI + 1e-12)
        }
    }

    @Test
    fun `gate 3 symmetry - the chord azimuth is a function of the standoff's own azimuth alone`() {
        // it is not a function of where the standoff sits, nor of which phosphates it links to:
        // the two termini are on the standoff's own end face. That is what makes the alignment a
        // ONE-parameter matter, and nothing in the search imposes it.
        val s = search()
        var checked = 0
        listOf(0.0, 0.4, 1.1, 2.3, 3.7).forEach { x ->
            listOf(-0.4, 0.0, 0.4).forEach { y ->
                val placement = s.bestAlignedPlacement(x, y) ?: return@forEach
                val expected = s.chordAzimuthOf(placement.azimuth)
                assert(foldedChordMisalignment(placement.chordAzimuth, expected) < 1e-9)
                checked++
            }
        }
        assert(checked >= 6)
    }

    @Test
    fun `gate 3 symmetry - a mixed truss does not care in which order its legs are listed`() {
        // exact as physics; limited only by the order the LDL factorisation eliminates in
        val a = mixedBaseTrussBucklingLoad(ei, 8.0, listOf(78.24, 13.53, 40.0), 30.0, 32)
        val b = mixedBaseTrussBucklingLoad(ei, 8.0, listOf(40.0, 13.53, 78.24), 30.0, 32)
        assert(a.isCloseTo(b, 1e-9))
    }

    @Test
    fun `gate 3 symmetry - a junction pair is invariant under exchanging its two legs`() {
        val s = search()
        val pair = s.bestPair(6)
        assert(pair != null)
        val swapped = JunctionPairClosure(pair!!.second, pair.first, pair.separationBasePairs)
        assert(swapped.worstLoadedCoupleFraction.isCloseTo(pair.worstLoadedCoupleFraction, 1e-12))
        assert(swapped.axialSeparation.isCloseTo(pair.axialSeparation, 1e-12))
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 convergence - the finite element buckling load converges monotonically on nested meshes`() {
        val bases = listOf(78.24, 13.53)
        val coarse = mixedBaseTrussBucklingLoad(ei, 8.0, bases, 0.0, 8)
        val medium = mixedBaseTrussBucklingLoad(ei, 8.0, bases, 0.0, 16)
        val fine = mixedBaseTrussBucklingLoad(ei, 8.0, bases, 0.0, 32)
        val finest = mixedBaseTrussBucklingLoad(ei, 8.0, bases, 0.0, 64)
        // a displacement-based element is stiff, so refinement lowers the critical load
        assert(coarse >= medium && medium >= fine && fine >= finest)
        assert(abs(fine - finest) / finest < 1e-5)
    }

    @Test
    fun `gate 4 convergence - the closure search returns the identical configuration on a repeat call`() {
        val s = search()
        val first = s.bestPair(6)
        val second = s.bestPair(6)
        assert(first != null && second != null)
        assert(first!!.first.centreX == second!!.first.centreX)
        assert(first.first.azimuth == second.first.azimuth)
        assert(first.second.centreX == second.second.centreX)
        assert(first.worstLoadedCoupleFraction == second.worstLoadedCoupleFraction)
    }

    @Test
    fun `gate 4 convergence - the pair's alignment is stable when both continuous grids are tripled`() {
        val coarse = search(axialStepsPerBasePair = 2, azimuthSteps = 120).bestPair(6)
        val fine = search(axialStepsPerBasePair = 6, azimuthSteps = 360).bestPair(6)
        assert(coarse != null && fine != null)
        assert(abs(coarse!!.worstLoadedCoupleFraction - fine!!.worstLoadedCoupleFraction) < 5e-3)
    }

    // --------------------------------------------------- gate 5: literature and upstream

    @Test
    fun `gate 5 upstream - C-0029's counting constants are reproduced`() {
        val hard = DuplexBackbone(minorGrooveAngle = 180.0)
        assert(hard.terminalChord.isCloseTo(2.0, 1e-12))
        assert(hard.leverArm.isCloseTo(1.0, 1e-12))
        assert(maximumBaseRotationalStiffness(hard.leverArm).isCloseTo(78.24, 1e-4))
        assert((2.0 * bondHingeStiffness()).isCloseTo(13.53, 1e-3))
        // the azimuthal quantum, 360/10.67 degrees per base pair
        assert((backbone.azimuthQuantum * 180.0 / PI).isCloseTo(33.74, 1e-3))
    }

    @Test
    fun `gate 5 upstream - C-0037's L2a8 loaded and free critical loads are reproduced`() {
        val base = TwoLinkBase.realisable()
        val layout = TrussLayout.row(2, 8 * rise, 0.5 * PI)
        val truss = TriangulatedStandoff(
            layout, 8.0, base,
            headTieStiffness = 2.0 * bondSlideStiffness() * layout.totalSecondMoment
        )
        assert(truss.loadedCriticalLoad.isCloseTo(9.77, 2e-3))
        assert(truss.freeCriticalLoad.isCloseTo(11.70, 2e-3))
        // and the mixed solver reproduces the loaded plane through a completely different route
        val finite = mixedBaseTrussBucklingLoad(
            ei, 8.0, listOf(base.restrainedAxis, base.restrainedAxis), truss.loadedFrameCouple, 64
        )
        assert(finite.isCloseTo(truss.loadedCriticalLoad, 2e-4))
    }

    @Test
    fun `gate 5 literature - the measured phosphodiester step and the SAXS pitch are what is used`() {
        assert(BForm.PHOSPHODIESTER_STEP_MIN.isCloseTo(0.60))
        assert(BForm.PHOSPHODIESTER_STEP.isCloseTo(0.70))
        assert(BForm.PHOSPHATE_RADIUS.isCloseTo(1.00))
        assert(Gen1Tile.INTERHELICAL_SHEET.isCloseTo(2.69))
        assert(Gen1Tile.RISE_PER_BASE_PAIR.isCloseTo(0.34))
        // C-0015: 32 bp per interface, so 16 bp along one duplex, and 32 bp is three square turns
        assert((3.0 * BForm.BASE_PAIRS_PER_TURN_SQUARE).isCloseTo(32.0, 4e-4))
    }

    @Test
    fun `gate 5 upstream - the screw image says 8 bp would be the worst separation in the band`() {
        val at6 = screwImageChordRotation(6, backbone) * 180.0 / PI
        val at7 = screwImageChordRotation(7, backbone) * 180.0 / PI
        val at8 = screwImageChordRotation(8, backbone) * 180.0 / PI
        assert(at6.isCloseTo(22.44, 1e-3))
        assert(at7.isCloseTo(56.18, 1e-3))
        assert(at8.isCloseTo(89.92, 1e-3))
        assert(at8 > at7 && at7 > at6)
    }

    // ------------------------------------------------------------------ the task's own answer

    @Test
    fun `gate 2 limiting - the sheet phase residual never exceeds the screw image it refines`() {
        // it is a minimum over the screw image and two strand swaps, so it can only fall
        (0..32).forEach {
            assert(sheetPhaseResidual(it, backbone) <= screwImageChordRotation(it, backbone) + 1e-12)
            assert(sheetPhaseResidual(it, backbone) >= 0.0)
        }
        // and with a 180 degree groove a strand swap IS a half turn, so it buys exactly nothing
        val hard = DuplexBackbone(minorGrooveAngle = 180.0)
        (0..16).forEach {
            assert(
                sheetPhaseResidual(it, hard)
                    .isCloseTo(screwImageChordRotation(it, hard), 1e-12)
            )
        }
    }

    @Test
    fun `gate 5 upstream - the strand swap is what makes 7 bp the quiet separation`() {
        assert((sheetPhaseResidual(7, backbone) * 180.0 / PI).isCloseTo(3.821, 1e-3))
        assert((sheetPhaseResidual(6, backbone) * 180.0 / PI).isCloseTo(22.437, 1e-3))
        assert((sheetPhaseResidual(8, backbone) * 180.0 / PI).isCloseTo(29.917, 1e-3))
        // the screw image alone says the opposite: 7 bp is WORSE than 6 bp on that bound
        assert(screwImageChordRotation(7, backbone) > screwImageChordRotation(6, backbone))
        assert(sheetPhaseResidual(7, backbone) < sheetPhaseResidual(6, backbone))
    }

    @Test
    fun `the crossover phase count leaves room for both junctions`() {
        // four target base pairs spanning 8 bp, on a duplex whose crossovers recur every 16 bp
        val free = crossoverFreePhaseCount(listOf(9, 10, 15, 16))
        assert(free > 0)
        assert(free == 2 * (16 - 4))
        // a pair of junctions cannot use more than the 16 residues there are
        assert(crossoverFreePhaseCount((0..15).toList()) == 0)
    }

    @Test
    fun `both junctions of the best 6 bp pair close covalently with zero unpaired nucleotides`() {
        val pair = search().bestPair(6)
        assert(pair != null)
        assert(pair!!.first.covalent)
        assert(pair.second.covalent)
        assert(pair.first.firstUnpaired == 0 && pair.first.secondUnpaired == 0)
        assert(pair.second.firstUnpaired == 0 && pair.second.secondUnpaired == 0)
        assert(pair.stericallyClear)
        assert(pair.distinctTargets)
        assert(pair.axialSeparation.isCloseTo(6 * rise, 1e-9))
    }

    @Test
    fun `a pair closer than the steric floor is refused`() {
        assertFailsWith<IllegalArgumentException> { search().bestPair(5) }
    }
}
