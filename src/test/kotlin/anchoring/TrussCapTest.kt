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
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-106` — the truss **cap** as a solved body rather than a series spring.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The spine of the task is a **count and a length**: two legs must stand at least one duplex
 * diameter apart (`C-0042`'s steric floor), and a leg is seated on a duplex only if its axis lies
 * within one radius of that duplex's axis — so no duplex laid **across** the leg row can seat both,
 * the flexure cannot be the cap, and the cap is a separate crossbar. Once it is a body it has a
 * bending stiffness, a torsional stiffness, a **height**, and three more two-link junctions, and
 * `C-0037` carries only the axial path of one of them.
 */
class TrussCapTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY
    private val stretch = Gen1Tile.DUPLEX_STRETCH_MODULUS
    private val rise = Gen1Tile.RISE_PER_BASE_PAIR
    private val radius = BForm.DUPLEX_RADIUS

    /** `C-0029`'s realisable two-link base on the hard, convention-free 180° chord. */
    private val hardBase = TwoLinkBase.realisable()

    private val hardBackbone = DuplexBackbone(minorGrooveAngle = 180.0)

    /** `C-0037`'s recommended row: two legs 8 bp apart ACROSS the flexure axis. */
    private fun crossRow(basePairs: Int) =
        TrussLayout.row(2, basePairs * rise, PI / 2.0, "$basePairs bp cross row")

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 dimensional — the cap's bending stiffness is a rigidity over a length`() {
        val w = 8 * rise
        val base = capBendingStiffness(ei, w)
        assert(base.isCloseTo(12.0 * ei / w))
        // doubling the rigidity doubles it; doubling the span halves it
        assert(capBendingStiffness(2.0 * ei, w).isCloseTo(2.0 * base))
        assert(capBendingStiffness(ei, 2.0 * w).isCloseTo(0.5 * base))
        // the end-condition factor is a pure multiplier, 12 pinned to 16 clamped
        assert(capBendingStiffness(ei, w, endFactor = 16.0).isCloseTo(base * 16.0 / 12.0))
    }

    @Test
    fun `gate 1 dimensional — the cap's torsional stiffness is 4C over the row width`() {
        val w = 8 * rise
        val c = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY
        assert(capTorsionalStiffness(c, w).isCloseTo(4.0 * c / w))
        assert(capTorsionalStiffness(2.0 * c, w).isCloseTo(2.0 * capTorsionalStiffness(c, w)))
        assert(capTorsionalStiffness(c, 2.0 * w).isCloseTo(0.5 * capTorsionalStiffness(c, w)))
    }

    @Test
    fun `gate 1 dimensional — the rigid height enters as a congruence of unit determinant`() {
        val leg = standoffTipFlexibility(ei, 8.0, hardBase.restrainedAxis)
        val e = radius
        val lifted = offsetFlexibility(leg, e)
        // C22 is untouched, C12 gains e C22, C11 gains 2e C12 + e^2 C22 — exactly
        assert(lifted.rotationUnderMoment.isCloseTo(leg.rotationUnderMoment))
        assert(
            lifted.translationUnderMoment
                .isCloseTo(leg.translationUnderMoment + e * leg.rotationUnderMoment)
        )
        assert(
            lifted.translationUnderForce.isCloseTo(
                leg.translationUnderForce + 2.0 * e * leg.translationUnderMoment +
                        e * e * leg.rotationUnderMoment
            )
        )
        // the determinant is invariant, because the congruence has unit determinant
        val before = leg.translationUnderForce * leg.rotationUnderMoment -
                leg.translationUnderMoment * leg.rotationUnderForce
        val after = lifted.translationUnderForce * lifted.rotationUnderMoment -
                lifted.translationUnderMoment * lifted.rotationUnderForce
        assert(after.isCloseTo(before, 1e-12))
    }

    @Test
    fun `gate 1 dimensional — a cap length is a width over a rise and never below the floor`() {
        val geometry = TrussCapGeometry(legSeparation = 7 * rise)
        assert(geometry.minimumLength.isCloseTo(7 * rise + 2.0 * radius))
        assert(geometry.minimumBasePairs == 13)
        // halving the rise doubles the base-pair count
        val fine = TrussCapGeometry(legSeparation = 7 * rise, rise = 0.5 * rise)
        assert(fine.minimumBasePairs == 26)
        assert(geometry.rigidHeight.isCloseTo(radius))
    }

    @Test
    fun `gate 1 dimensional — unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { capBendingStiffness(-1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { capBendingStiffness(ei, 0.0) }
        assertFailsWith<IllegalArgumentException> { capBendingStiffness(ei, 1.0, endFactor = 0.0) }
        assertFailsWith<IllegalArgumentException> { capTorsionalStiffness(-1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { capTorsionalStiffness(1.0, -1.0) }
        assertFailsWith<IllegalArgumentException> { TrussCapGeometry(legSeparation = 0.0) }
        assertFailsWith<IllegalArgumentException> { offsetFlexibility(
            standoffTipFlexibility(ei, 8.0, 78.0), -1.0
        ) }
        assertFailsWith<IllegalArgumentException> {
            cappedTrussBucklingLoad(ei, 8.0, listOf(78.0), listOf(78.0, 78.0), 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            cappedTrussBucklingLoad(ei, 8.0, listOf(78.0), listOf(78.0), -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            cappedTrussBucklingLoad(ei, 8.0, emptyList(), emptyList(), 0.0)
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 limiting — no duplex laid ACROSS the row can seat both legs, at any separation`() {
        // the steric floor is one duplex diameter, so w/2 >= R at every admissible separation,
        // and a leg's flat face then makes no line contact at all with a seat through the centroid
        (6..12).forEach { bp ->
            val w = bp * rise
            assert(w >= 2.0 * radius)
            assert(capSeatContactAcrossRow(w).isCloseTo(0.0))
            assert(TrussCapGeometry(legSeparation = w).separateBodyRequired)
        }
        // and it is the floor that does it: a hypothetical narrower row would be seatable
        assert(capSeatContactAcrossRow(1.0) > 0.0)
    }

    @Test
    fun `gate 2 limiting — a cap laid ALONG the row seats each leg on its full diameter`() {
        (6..12).forEach { bp ->
            assert(
                TrussCapGeometry(legSeparation = bp * rise).parallelSeatContact
                    .isCloseTo(2.0 * radius)
            )
        }
    }

    @Test
    fun `gate 2 limiting — an infinitely stiff cap returns C-0037's frame couple exactly`() {
        val layout = crossRow(8)
        val legAxial = legAxialStiffness(8.0, hardBase, stretch)
        val link = 2.0 * bondSlideStiffness()
        val moment = layout.acrossSecondMoment
        val published = trussFrameCouple(moment, legAxial, link * moment)
        assert(
            solvedFrameCouple(legAxial, link, moment, Double.POSITIVE_INFINITY)
                .isCloseTo(published, 1e-12)
        )
        // and a rigid link on top of that is the bare frame couple
        assert(
            solvedFrameCouple(
                legAxial, Double.POSITIVE_INFINITY, moment, Double.POSITIVE_INFINITY
            ).isCloseTo(legAxial * moment, 1e-12)
        )
    }

    @Test
    fun `gate 2 limiting — a rigid cap of zero height returns C-0037's head flexibility`() {
        listOf(5.0, 8.0, 10.0).forEach { length ->
            listOf(0.0, 40.0, 96.88).forEach { frame ->
                listOf(1, 2, 3).forEach { legs ->
                    val published = trussTipFlexibility(
                        ei, length, hardBase.restrainedAxis, legs, frame
                    )
                    val capped = cappedHeadFlexibility(
                        bendingRigidity = ei,
                        length = length,
                        baseRotationalStiffness = hardBase.restrainedAxis,
                        legCount = legs,
                        frameCouple = frame
                    )
                    assert(
                        capped.translationUnderForce
                            .isCloseTo(published.translationUnderForce, 1e-12)
                    )
                    assert(
                        capped.translationUnderMoment
                            .isCloseTo(published.translationUnderMoment, 1e-12)
                    )
                    assert(
                        capped.rotationUnderForce.isCloseTo(published.rotationUnderForce, 1e-12)
                    )
                    assert(
                        capped.rotationUnderMoment.isCloseTo(published.rotationUnderMoment, 1e-12)
                    )
                }
            }
        }
    }

    @Test
    fun `gate 2 limiting — rigid junctions and zero height return C-0042's mixed-base element`() {
        listOf(0.0, 30.0, 96.88).forEach { frame ->
            listOf(listOf(78.24), listOf(13.53, 13.53), listOf(78.24, 13.53)).forEach { bases ->
                val published = mixedBaseTrussBucklingLoad(ei, 8.0, bases, frame, elementsPerLeg = 16)
                val capped = cappedTrussBucklingLoad(
                    ei, 8.0, bases, bases.map { Double.POSITIVE_INFINITY }, frame,
                    elementsPerLeg = 16
                )
                assert(capped.isCloseTo(published, 1e-9))
            }
        }
    }

    @Test
    fun `gate 2 limiting — a pinned cap junction reduces the truss to independent legs`() {
        // a head junction of zero rotational stiffness cannot deliver the frame couple to a leg
        val bases = listOf(13.53, 13.53)
        val free = 2.0 * standoffBucklingLoad(
            ei, 8.0, baseRestraintParameter(13.53, ei, 8.0), 0.0
        )
        val capped = cappedTrussBucklingLoad(
            ei, 8.0, bases, listOf(0.0, 0.0), frameCouple = 1.0e6, elementsPerLeg = 32
        )
        assert(capped.isCloseTo(free, 2e-4))
    }

    @Test
    fun `gate 2 limiting — a pinned base with no frame couple is a mechanism, exactly zero`() {
        assert(
            cappedTrussBucklingLoad(
                ei, 8.0, listOf(0.0, 0.0), listOf(78.24, 78.24), 0.0, elementsPerLeg = 8
            ) == 0.0
        )
    }

    @Test
    fun `gate 2 limiting — a finite head junction is strictly softer than a rigid one`() {
        val bases = listOf(13.53, 13.53)
        val rigid = cappedTrussBucklingLoad(
            ei, 8.0, bases, bases.map { Double.POSITIVE_INFINITY }, 70.0, elementsPerLeg = 16
        )
        val strong = cappedTrussBucklingLoad(
            ei, 8.0, bases, listOf(78.24, 78.24), 70.0, elementsPerLeg = 16
        )
        val weak = cappedTrussBucklingLoad(
            ei, 8.0, bases, listOf(13.53, 13.53), 70.0, elementsPerLeg = 16
        )
        assert(strong < rigid)
        assert(weak < strong)
    }

    @Test
    fun `gate 2 limiting — the rigid cap height strictly lowers the critical load`() {
        val bases = listOf(13.53, 13.53)
        val flat = cappedTrussBucklingLoad(
            ei, 8.0, bases, listOf(78.24, 78.24), 70.0, rigidHeight = 0.0, elementsPerLeg = 16
        )
        val raised = cappedTrussBucklingLoad(
            ei, 8.0, bases, listOf(78.24, 78.24), 70.0, rigidHeight = 1.0, elementsPerLeg = 16
        )
        assert(raised < flat)
    }

    // ------------------------------------------------------------------ gate 3: symmetry

    @Test
    fun `gate 3 symmetry — Maxwell-Betti on the assembled capped head, two quadratures`() {
        listOf(5.0, 8.0, 10.0).forEach { length ->
            listOf(1, 2, 3).forEach { legs ->
                listOf(0.0, 70.0).forEach { frame ->
                    val closed = cappedHeadFlexibility(
                        ei, length, hardBase.restrainedAxis, legs, frame,
                        headJunctionRotational = 78.24, headJunctionShear = 64.71,
                        capSeriesRotational = 689.0,
                        flexureJunctionRotational = 78.24, flexureJunctionShear = 64.71,
                        rigidHeight = 1.0
                    )
                    val integrated = cappedHeadFlexibilityByIntegration(
                        ei, length, hardBase.restrainedAxis, legs, frame,
                        headJunctionRotational = 78.24, headJunctionShear = 64.71,
                        capSeriesRotational = 689.0,
                        flexureJunctionRotational = 78.24, flexureJunctionShear = 64.71,
                        rigidHeight = 1.0
                    )
                    assert(
                        closed.translationUnderMoment.isCloseTo(closed.rotationUnderForce, 1e-12)
                    )
                    assert(
                        integrated.translationUnderMoment
                            .isCloseTo(integrated.rotationUnderForce, 1e-12)
                    )
                    assert(
                        integrated.translationUnderMoment
                            .isCloseTo(closed.translationUnderMoment, 1e-9)
                    )
                    assert(
                        integrated.translationUnderForce
                            .isCloseTo(closed.translationUnderForce, 1e-9)
                    )
                }
            }
        }
    }

    @Test
    fun `gate 3 symmetry — the cap junction's two axes are a conserved rank-one budget`() {
        val budget = 4.0 * bondHingeStiffness() + 2.0 * bondSlideStiffness() * 1.0 * 1.0
        listOf(0.0, 0.3, PI / 6.0, PI / 4.0, PI / 3.0, PI / 2.0).forEach { psi ->
            val axes = chordBaseAxes(hardBackbone, psi)
            assert(axes.total.isCloseTo(budget, 1e-12))
        }
        // and the two ends of the budget are C-0029's two constants
        val aligned = chordBaseAxes(hardBackbone, 0.0)
        assert(aligned.loaded.isCloseTo(maximumBaseRotationalStiffness(1.0), 1e-12))
        assert(aligned.free.isCloseTo(2.0 * bondHingeStiffness(), 1e-12))
    }

    @Test
    fun `gate 3 symmetry — the capped element does not care in which order its legs are listed`() {
        val a = cappedTrussBucklingLoad(
            ei, 8.0, listOf(78.24, 13.53), listOf(13.53, 78.24), 70.0, 1.0, elementsPerLeg = 16
        )
        val b = cappedTrussBucklingLoad(
            ei, 8.0, listOf(13.53, 78.24), listOf(78.24, 13.53), 70.0, 1.0, elementsPerLeg = 16
        )
        assert(a.isCloseTo(b, 1e-9))
    }

    @Test
    fun `gate 3 symmetry — the solved frame couple is symmetric in its three series members`() {
        val moment = crossRow(7).acrossSecondMoment
        val a = solvedFrameCouple(44.0, 64.71, moment, 1159.7)
        val direct = 1.0 / (1.0 / (44.0 * moment) + 1.0 / (64.71 * moment) + 1.0 / 1159.7)
        assert(a.isCloseTo(direct, 1e-12))
        assert(a < seriesStiffness(44.0 * moment, 64.71 * moment))
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 convergence — the capped element converges on nested meshes`() {
        val bases = listOf(13.53, 13.53)
        val junctions = listOf(78.24, 78.24)
        val loads = listOf(8, 16, 32, 64).map {
            cappedTrussBucklingLoad(ei, 8.0, bases, junctions, 70.0, 1.0, elementsPerLeg = it)
        }
        // monotone on nested refinements while the discretisation error is above the bisection's
        // own floor, and settled to it between the last two
        assert(loads[1] <= loads[0])
        assert(loads[2] <= loads[1])
        assert(abs(loads[3] - loads[2]) / loads[3] < 1e-8)
        assert(abs(loads[2] - loads[1]) / loads[2] < 1e-7)
    }

    @Test
    fun `gate 4 convergence — the cap end-condition bracket is worth under two per cent`() {
        val moment = crossRow(7).acrossSecondMoment
        val legAxial = legAxialStiffness(7.0, hardBase, stretch)
        val link = 2.0 * bondSlideStiffness()
        val w = 7 * rise
        val pinned = solvedFrameCouple(legAxial, link, moment, capBendingStiffness(ei, w, 12.0))
        val clamped = solvedFrameCouple(legAxial, link, moment, capBendingStiffness(ei, w, 16.0))
        assert(clamped > pinned)
        assert((clamped - pinned) / pinned < 0.02)
    }

    // ------------------------------------------------------------------ gate 5: upstream

    @Test
    fun `gate 5 upstream — C-0037's L2a8 frame couple and both critical loads reproduce`() {
        val layout = crossRow(8)
        val legAxial = legAxialStiffness(8.0, hardBase, stretch)
        val link = 2.0 * bondSlideStiffness()
        assert(legAxial.isCloseTo(44.0, 2e-3))
        val frame = trussFrameCouple(layout.acrossSecondMoment, legAxial, link * layout.acrossSecondMoment)
        assert(frame.isCloseTo(96.88, 2e-3))
        val head = TriangulatedStandoff(
            layout, 8.0, hardBase, headTieStiffness = link * layout.totalSecondMoment
        )
        assert(head.loadedCriticalLoad.isCloseTo(9.7715, 1e-3))
        assert(head.freeCriticalLoad.isCloseTo(11.7021, 1e-3))
    }

    @Test
    fun `gate 5 upstream — C-0029's two chord constants and C-0042's steric floor reproduce`() {
        assert(maximumBaseRotationalStiffness(1.0).isCloseTo(78.24, 1e-3))
        assert((2.0 * bondHingeStiffness()).isCloseTo(13.53, 1e-3))
        assert(pairStericFloorBasePairs() == 6)
        assert((2.0 * bondSlideStiffness()).isCloseTo(64.71, 1e-3))
    }

    @Test
    fun `gate 5 literature — the torsional constant is carried on both its readings`() {
        // CanDo's model input, and the measured torsional persistence length
        assert(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY.isCloseTo(460.0))
        val measured = Gen1Tile.DUPLEX_TORSIONAL_PERSISTENCE * 4.141947
        assert(measured.isCloseTo(414.19, 1e-3))
        // Kriegel et al.'s 103 nm is inside the bracket the two readings span
        val kriegel = 103.0 * 4.141947
        assert(kriegel > measured && kriegel < Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY)
    }

    // ------------------------------------------------------------------ the assembled cap

    @Test
    fun `gate 2 limiting — the solved cap is strictly softer than C-0037's series spring`() {
        val cap = SolvedTrussCap(separationBasePairs = 7, legLength = 7.0, base = hardBase)
        assert(cap.frameCouple < cap.assertedFrameCouple)
        assert(cap.frameCouple > 0.0)
        // and the flexibility it hands the flexure is strictly more compliant in rotation
        assert(cap.flexibility.rotationUnderMoment > cap.assertedFlexibility.rotationUnderMoment)
    }

    @Test
    fun `gate 5 upstream — the adopted design's two critical loads, against C-0037's`() {
        // C-0037 and C-0042 at 7 bp: a rigid cap, rigidly bonded, legs the full 8 nm
        val asserted = SolvedTrussCap(7, 8.0, hardBase, asserted = true)
        assert(asserted.loadedCriticalLoad.isCloseTo(9.7715, 1e-3))
        assert(asserted.assertedFreeCriticalLoad.isCloseTo(10.3020, 1e-3))
        assert(asserted.governingPlane == "loaded")
        // the solved cap, chord ACROSS the flexure axis, legs shortened by the cap's own radius
        val solved = SolvedTrussCap(7, 7.0, hardBase, capJunctionMisalignment = PI / 2.0)
        assert(solved.governingPlane == "loaded")
        assert(solved.freeCriticalLoad.isCloseTo(9.2365, 1e-3))
        assert(solved.loadedCriticalLoad.isCloseTo(8.9528, 1e-3))
        assert(solved.criticalLoad < asserted.criticalLoad)
    }

    @Test
    fun `gate 2 limiting — the cap chord laid ALONG the flexure axis never hands over the plane`() {
        // the head junction caps the free plane's head restraint at its own constant, so with
        // the strong axis spent on the loaded plane no row width can buy the crossing back
        (6..16).forEach { bp ->
            val cap = SolvedTrussCap(bp, 7.0, hardBase, capJunctionMisalignment = 0.0)
            assert(cap.governingPlane == "free")
        }
        // laid across it, seven base pairs is the smallest that does
        assert(SolvedTrussCap(6, 7.0, hardBase, capJunctionMisalignment = PI / 2.0)
            .governingPlane == "free")
        (7..16).forEach { bp ->
            assert(
                SolvedTrussCap(bp, 7.0, hardBase, capJunctionMisalignment = PI / 2.0)
                    .governingPlane == "loaded"
            )
        }
    }

    @Test
    fun `gate 3 symmetry — the cap junction azimuth trades one plane against the other`() {
        val alongFlexure = SolvedTrussCap(7, 7.0, hardBase, capJunctionMisalignment = 0.0)
        val alongRow = SolvedTrussCap(7, 7.0, hardBase, capJunctionMisalignment = PI / 2.0)
        assert(alongFlexure.headJunctionLoaded > alongRow.headJunctionLoaded)
        assert(alongFlexure.headJunctionFree < alongRow.headJunctionFree)
        assert(
            (alongFlexure.headJunctionLoaded + alongFlexure.headJunctionFree)
                .isCloseTo(alongRow.headJunctionLoaded + alongRow.headJunctionFree, 1e-12)
        )
    }
}
