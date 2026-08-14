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
import org.openrndr.math.Vector3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-124` — the truss branch's three junctions re-derived on `C-0057`'s **torsion-feasible** set
 * rather than on the phosphate distance all three of them optimised.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The spine of the task is that `C-0057` invalidated the three reported routings while proving the
 * search space contains feasible placements, and that the open question is the **conjunction**:
 * a placement that is covalent, reach-feasible, torsion-closing *and* aligned on the axis the
 * design loads.
 */
class TorsionFeasibleRoutingTest {

    private val backbone = DuplexBackbone()

    private val hard = DuplexBackbone(minorGrooveAngle = 180.0)

    /** `C-0029`'s reported optimum, computed once — the search itself is not cheap. */
    private val reportedOptimum: JunctionClosure by lazy {
        bestTwoLinkClosure(backbone, RoutingTopology.INDEPENDENT_STAPLES)
    }

    private val reportedLinks: List<JunctionLinkEnds> by lazy {
        junctionLinks(backbone, reportedOptimum)
    }

    /** `C-0057`'s census grid, enumerated once. */
    private val independent: SingleJunctionEnumeration by lazy {
        SingleJunctionFeasibleSet(backbone).enumerate(RoutingTopology.INDEPENDENT_STAPLES)
    }

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 dimensional - a chord misalignment is an angle in the closed quarter turn`() {
        for (i in 0..360) {
            val psi = foldedChordMisalignment(i * PI / 180.0)
            assert(psi >= 0.0)
            assert(psi <= 0.5 * PI + 1e-12)
        }
    }

    @Test
    fun `gate 1 dimensional - the alignment band is half the sheet's azimuthal quantum`() {
        assert(alignmentAllowance(backbone).isCloseTo(0.5 * backbone.azimuthQuantum))
        // C-0029's own cheap bound 3, in degrees and as a couple fraction
        assert((alignmentAllowance(backbone) * 180.0 / PI).isCloseTo(16.8697, 1e-4))
        assert(couplePhaseProjection(alignmentAllowance(backbone)).isCloseTo(0.91583, 1e-4))
    }

    @Test
    fun `gate 1 dimensional - the aligning standoff azimuth inverts the chord relation exactly`() {
        listOf(0.0, 0.25 * PI, 0.5 * PI, 1.3).forEach { wanted ->
            val psi = alignedStandoffAzimuth(wanted, backbone)
            val chord = chordAzimuthOfStandoff(psi, backbone)
            assert(foldedChordMisalignment(chord, wanted) < 1e-12)
        }
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw at every entry point`() {
        assertFailsWith<IllegalArgumentException> { reachVerdict(emptyList()) }
        assertFailsWith<IllegalArgumentException> { torsionVerdict(emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            SingleJunctionFeasibleSet(backbone, azimuthSteps = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            SingleJunctionFeasibleSet(backbone, lateralSteps = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            SingleJunctionFeasibleSet(backbone, interhelical = -1.0)
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 limiting - C-0029's reported optimum reproduces C-0057's failure verdict exactly`() {
        val links = reportedLinks
        assert(links.size == 2)
        links.forEach { assert(it.phosphateGap.isCloseTo(0.600047126, 1e-5)) }
        // both links pass the closed-form reach bound, and neither closes at torsion level
        assert(reachVerdict(links).feasible)
        val verdict = torsionVerdict(links)
        assert(!verdict.closes)
        assert(verdict.closingLinks == 0)
        assert(verdict.worstCovalentZ.isCloseTo(4.55315176, 1e-3))
        // the two torsions C-0057 names as the binding ones
        assert(verdict.closures.any { abs(it.torsions.epsilon - (-22.9)) < 0.6 })
        assert(verdict.closures.any { abs(it.torsions.beta - 27.4) < 0.6 })
        assert(verdict.minimumOccupancy == 0.0)
    }

    @Test
    fun `gate 2 limiting - a link fifty nanometres away is excluded by the reach bound with no solve`() {
        val links = reportedLinks.map { link ->
            link.copy(
                standoff = link.standoff.copy(
                    phosphate = link.standoff.phosphate + Vector3(0.0, 0.0, 50.0),
                    axisPoint = link.standoff.axisPoint + Vector3(0.0, 0.0, 50.0)
                )
            )
        }
        val verdict = reachVerdict(links)
        assert(!verdict.feasible)
        assert(verdict.violation > 40.0)
    }

    @Test
    fun `gate 2 limiting - a zero misalignment reproduces the favourable realisable base exactly`() {
        val axes = chordBaseAxes(backbone, 0.0)
        assert(
            axes.loaded.isCloseTo(
                realisablePerpendicularBase(backbone, favourable = true).rotationalStiffness
            )
        )
        assert(
            axes.free.isCloseTo(
                realisablePerpendicularBase(backbone, favourable = false).rotationalStiffness
            )
        )
    }

    @Test
    fun `gate 2 limiting - the rigid cap limit reproduces C-0037's truss`() {
        val axes = chordBaseAxes(hard, 0.0)
        val base = TwoLinkBase(
            name = "aligned two-terminus base",
            restrainedAxis = axes.loaded,
            freeAxis = axes.free,
            axial = 2.0 * bondSlideStiffness(),
            provenance = "T-124 gate 2"
        )
        val cap = SolvedTrussCap(
            separationBasePairs = 7, legLength = 7.0, base = base, asserted = true
        )
        val truss = TriangulatedStandoff(
            layout = cap.layout,
            length = 7.0,
            base = base,
            headTieStiffness = 2.0 * bondSlideStiffness() * cap.layout.acrossSecondMoment
        )
        // C-0037's own frame couple and its own free-plane critical load, to the last digit
        assert(cap.frameCouple.isCloseTo(truss.freeFrameCouple))
        assert(cap.assertedFreeCriticalLoad.isCloseTo(truss.freeCriticalLoad))
        // and the whole cap reduces to C-0037's truss in the free plane it exists to stiffen
        assert(cap.rigidHeight == 0.0)
    }

    @Test
    fun `gate 2 limiting - an alignment band of a full quarter turn admits the whole feasible set`() {
        val set = SingleJunctionFeasibleSet(backbone, azimuthSteps = 24, axialSteps = 16)
        val enumeration = set.enumerate(RoutingTopology.INDEPENDENT_STAPLES)
        assert(enumeration.withinBand(0.5 * PI).size == enumeration.feasible.size)
        assert(enumeration.withinBand(0.0).size <= enumeration.feasible.size)
    }

    // ------------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 symmetry - the base couple budget is conserved under the azimuth`() {
        val budget = chordBaseAxes(backbone, 0.0).total
        for (i in 0..90) {
            assert(chordBaseAxes(backbone, i * PI / 180.0).total.isCloseTo(budget))
        }
    }

    @Test
    fun `gate 3 symmetry - a chord is a line so a half turn of the standoff is the same design`() {
        for (i in 0..36) {
            val chord = chordAzimuthOfStandoff(i * PI / 18.0, backbone)
            // compared ABSOLUTELY: both are meant to vanish where the chord IS the flexure axis,
            // and comparing two quantities that are both zero relatively compares their noise
            assert(
                abs(foldedChordMisalignment(chord) - foldedChordMisalignment(chord + PI)) < 1e-12
            )
        }
    }

    @Test
    fun `gate 3 symmetry - the chord azimuth is a function of the standoff azimuth and nothing else`() {
        val set = SingleJunctionFeasibleSet(backbone)
        val wanted = chordAzimuthOfStandoff(0.7, backbone)
        listOf(0.0, 1.0, 2.7, 5.3).forEach { axial ->
            listOf(0.0, 0.336, 1.345).forEach { lateral ->
                // `JunctionClosure` reports the chord through `atan2`, so the comparison is
                // modulo a turn — and modulo a HALF turn, because a chord is a line
                set.placementAt(RoutingTopology.INDEPENDENT_STAPLES, axial, lateral, 0.7)
                    ?.let { assert(foldedChordMisalignment(it.chordAzimuth, wanted) < 1e-12) }
            }
        }
    }

    @Test
    fun `gate 3 symmetry - a reach verdict is invariant under a rigid motion of the whole junction`() {
        val links = reportedLinks
        val shift = Vector3(3.1, -0.7, 2.2)
        val moved = links.map { link ->
            link.copy(
                seat = link.seat.copy(
                    phosphate = link.seat.phosphate + shift,
                    axisPoint = link.seat.axisPoint + shift
                ),
                standoff = link.standoff.copy(
                    phosphate = link.standoff.phosphate + shift,
                    axisPoint = link.standoff.axisPoint + shift
                )
            )
        }
        assert(reachVerdict(links).worstReach.isCloseTo(reachVerdict(moved).worstReach, 1e-12))
    }

    // ------------------------------------------------------- gate 4: numerical convergence

    @Test
    fun `gate 4 convergence - the verdict of C-0029's optimum is unchanged on a finer torsion grid`() {
        val coarse = torsionVerdict(reportedLinks, gridSteps = 30, refinements = 3)
        val fine = torsionVerdict(reportedLinks, gridSteps = 72, refinements = 5)
        assert(coarse.closes == fine.closes)
        assert(coarse.closingLinks == fine.closingLinks)
    }

    @Test
    fun `gate 4 convergence - a finer azimuth grid does not lose the feasible placements a coarse one finds`() {
        val coarse = SingleJunctionFeasibleSet(backbone, azimuthSteps = 60, axialSteps = 32)
            .enumerate(RoutingTopology.INDEPENDENT_STAPLES)
        val fine = independent
        assert(fine.placements == 4 * coarse.placements)
        assert(fine.feasible.size >= coarse.feasible.size)
        // and the best attainable alignment cannot get worse on the finer grid
        assert(fine.bestFeasibleMisalignment <= coarse.bestFeasibleMisalignment + 1e-12)
    }

    // ------------------------------------------------------- gate 5: literature and upstream

    @Test
    fun `gate 5 upstream - the reach-feasible census reproduces C-0057's counts`() {
        assert(independent.placements == 69120)
        assert(independent.covalent == 3546)
        assert(independent.feasible.size == 1855)
        val excursion =
            SingleJunctionFeasibleSet(backbone).enumerate(RoutingTopology.SCAFFOLD_EXCURSION)
        assert(excursion.placements == 69120)
        assert(excursion.covalent == 280)
        assert(excursion.feasible.size == 137)
    }

    @Test
    fun `gate 5 upstream - the alignment band is a proper subset of the reach-feasible set`() {
        val inBand = independent.withinBand(alignmentAllowance(backbone)).size
        assert(inBand > 0)
        assert(inBand < independent.feasible.size)
    }

    @Test
    fun `gate 5 upstream - C-0048's recommended design point reproduces its whole row`() {
        val design = capDesign(legLength = 7.0, separationBasePairs = 7)
        assert(design.frameCouple.isCloseTo(71.31, 1e-4))
        assert(design.criticalLoad.isCloseTo(8.95, 6e-4))
        assert(design.freeCriticalLoad.isCloseTo(9.24, 4e-4))
        assert(design.marginCanDo.isCloseTo(1.95, 2e-3))
        assert(design.marginFields.isCloseTo(1.46, 3e-3))
        assert(design.span.isCloseTo(28.25, 1e-4))
        assert(design.supplyToDemand.isCloseTo(1.81, 3e-3))
        assert(design.verdict == "PASS")
    }

    @Test
    fun `gate 5 upstream - C-0052's chord twist budget reproduces at twenty-one and twenty-four steps`() {
        assert((chordPairMisalignment(21) * 180.0 / PI).isCloseTo(78.53, 1e-3))
        assert(chordPairMisalignment(24) * 180.0 / PI < 0.3)
    }
}
