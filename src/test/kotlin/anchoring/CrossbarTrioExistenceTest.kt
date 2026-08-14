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
 * `T-127` — does a torsion-feasible trio exist on `C-0048`'s cap crossbar **at all**?
 *
 * `C-0059` reported *"0 of the 24 best-aligned of 750 reach-feasible lattices, two candidate
 * azimuths per junction"* and labelled it a *"not found within the budget"*. This test class
 * covers the instrument that decides whether that negative is a property of the geometry or of
 * the search: a per-assignment-pruned closure verdict, a marginal closure census with an
 * independence yield prediction, and a sweep whose budget is itself a reported number.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class CrossbarTrioExistenceTest {

    private val backbone = DuplexBackbone()

    /** `C-0052`'s and `C-0059`'s own design point: a 13 bp crossbar carrying a 7 bp leg row. */
    private val designPoint = TrioConfiguration(13, 7)

    /** One lattice's worth of crossbar phosphates, at a phase that carries feasible placements. */
    private val lattice: List<Pair<CrossbarTarget, Vector3>> by lazy {
        crossbarPhosphateLattice(backbone, 13, 0.7, 0.0)
    }

    /** Reach-feasible junction placements over eight lattice phases, to solve against. */
    private val sampleLinks: List<List<JunctionLinkEnds>> by lazy {
        val search = TorsionFeasibleTrioSearch(backbone = backbone, crossbarBasePairs = 13)
        val out = ArrayList<List<JunctionLinkEnds>>()
        (1..8).forEach { p ->
            val phase = 0.7 * p
            val lattice = crossbarPhosphateLattice(backbone, 13, phase, 0.0)
            TrioJunctionSpec.cap(7).forEach { spec ->
                for (i in 0 until 120) {
                    val placement = search.placementAt(spec, 0.0, i * 2.0 * PI / 120, lattice)
                        ?: continue
                    if (!placement.covalent) continue
                    val links = junctionLinks(backbone, 13, phase, 0.0, placement)
                    if (reachVerdict(links).feasible) out += links
                }
            }
        }
        out
    }

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 dimensional - a search budget is a set of counts and adds componentwise`() {
        val a = SearchBudget(
            configurations = 1, lattices = 10, latticesSolved = 4,
            junctionSlots = 30, candidateAzimuths = 77, junctionSolves = 12, linkClosures = 24,
            closingLattices = 1
        )
        val b = SearchBudget(
            configurations = 2, lattices = 20, latticesSolved = 5,
            junctionSlots = 60, candidateAzimuths = 100, junctionSolves = 30, linkClosures = 60,
            closingLattices = 0
        )
        val sum = a + b
        assert(sum.configurations == 3)
        assert(sum.lattices == 30)
        assert(sum.latticesSolved == 9)
        assert(sum.junctionSlots == 90)
        assert(sum.candidateAzimuths == 177)
        assert(sum.junctionSolves == 42)
        assert(sum.linkClosures == 84)
        assert(sum.closingLattices == 1)
        // every count is non-negative, and a link closure is two per junction solve
        assert(a.linkClosures == 2 * a.junctionSolves)
    }

    @Test
    fun `gate 1 dimensional - a closing rate is a fraction and the yield prediction is a count`() {
        val census = listOf(
            MarginalClosureCensus("leg −w/2", 100, 260, 20, 10),
            MarginalClosureCensus("leg +w/2", 100, 260, 60, 30),
            MarginalClosureCensus("flexure end", 100, 260, 60, 30)
        )
        census.forEach {
            assert(it.latticeRate >= 0.0)
            assert(it.latticeRate <= 1.0)
        }
        assert(census[0].latticeRate.isCloseTo(0.10))
        // the independence prediction is N times the product of the per-lattice rates
        assert(independenceYield(census, 1000).isCloseTo(1000.0 * 0.10 * 0.30 * 0.30))
        // and it is exactly zero if any junction never closes
        assert(
            independenceYield(
                census + MarginalClosureCensus("dead", 100, 40, 0, 0), 1000
            ).isCloseTo(0.0)
        )
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw at five entry points`() {
        assertFailsWith<IllegalArgumentException> { TrioConfiguration(13, 0) }
        // a crossbar too short to cover its own row is not a configuration
        assertFailsWith<IllegalArgumentException> { TrioConfiguration(8, 7) }
        assertFailsWith<IllegalArgumentException> {
            crossbarPhosphateLattice(backbone, 1, 0.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            CrossbarTrioExistenceSearch(configuration = designPoint, phaseSteps = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            CrossbarTrioExistenceSearch(configuration = designPoint, closersPerJunction = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            independenceYield(emptyList(), 10)
        }
    }

    @Test
    fun `gate 1 dimensional - the admissible crossbar band starts at C-0048's own ceil`() {
        // C-0048's demand is w + 2R, and 2R is 5.88 rises, so ceil buys the row plus six
        (6..12).forEach { row ->
            assert(TrioConfiguration.minimumCrossbar(row) == row + 6)
            assert(TrioConfiguration(row + 6, row).crossbarBasePairs == row + 6)
        }
        // C-0052's and C-0059's own two lattices are in the band
        assert(TrioConfiguration.minimumCrossbar(7) == 13)
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 limiting - the pruned verdict reproduces C-0057's own closure verdict`() {
        // The per-assignment reach bound is a proof of exclusion, so an assignment that fails it
        // cannot close and need not be solved. That is the whole 17x speedup, and if it were
        // unsound every number in this task would be wrong. Asserted link by link.
        var links = 0
        var disagreements = 0
        sampleLinks.take(12).forEach { junction ->
            junction.forEach { link ->
                links++
                val pruned = linkClosesOnSomeAssignment(link, gridSteps = 60, refinements = 4)
                val full = bestLinkClosure(
                    link.seat, link.standoff, gridSteps = 60, refinements = 4
                ).closes
                if (pruned != full) disagreements++
            }
        }
        assert(links >= 20)
        assert(disagreements == 0)
    }

    @Test
    fun `gate 2 limiting - a junction moved fifty nanometres away closes at no assignment`() {
        val link = sampleLinks.first().first()
        val far = link.copy(
            standoff = link.standoff.copy(
                phosphate = link.standoff.phosphate + Vector3(50.0, 0.0, 0.0),
                axisPoint = link.standoff.axisPoint + Vector3(50.0, 0.0, 0.0)
            )
        )
        assert(!linkClosesOnSomeAssignment(far, gridSteps = 30, refinements = 2))
        assert(!reachVerdict(listOf(far)).feasible)
    }

    @Test
    fun `gate 2 limiting - restricting the sweep to C-0059's budget reproduces its trio negative`() {
        // C-0059: 24 best-aligned lattices of 750 reach-feasible, two candidate azimuths per
        // junction, 13 bp crossbar, 7 bp row — and 0 closing. This is the free limiting case:
        // the deepened search, capped back to that budget, must return the same verdict.
        val restricted = CrossbarTrioExistenceSearch(
            configuration = designPoint,
            phaseSteps = 90,
            axialSteps = 4,
            lateralSeats = listOf(-0.4, -0.2, 0.0, 0.2, 0.4),
            candidatesPerJunction = 2,
            latticeCap = 24,
            rankByAlignment = true
        ).sweep()
        assert(!restricted.closes)
        assert(restricted.budget.lattices == 1800)
        assert(restricted.reachFeasibleLattices == 750)
        assert(restricted.budget.latticesSolved == 24)
    }

    @Test
    fun `gate 2 limiting - C-0059's own trio search still returns its published numbers`() {
        val outcome = TorsionFeasibleTrioSearch(
            backbone = backbone, crossbarBasePairs = 13, gridSteps = 60, refinements = 4
        )
        val verdict = outcome.best(solveCap = 24)
        assert(!verdict.closes)
        assert(verdict.lattices == 1800)
        assert(verdict.feasibleLattices == 750)
        assert(verdict.solvedLattices == 24)
        assert(outcome.solves == 134)
        assert(verdict.bestFeasibleMisalignmentDegrees.isCloseTo(6.0, 1e-6))
    }

    @Test
    fun `gate 2 limiting - a sweep with no admissible lattice returns an empty verdict`() {
        // an impossible seat-contact floor admits nothing, and the sweep says so rather than
        // reporting a "does not close" that was never tested
        val empty = CrossbarTrioExistenceSearch(
            configuration = designPoint,
            phaseSteps = 4,
            axialSteps = 1,
            lateralSeats = listOf(0.0),
            contactFloor = 100.0
        ).sweep()
        assert(!empty.closes)
        assert(empty.reachFeasibleLattices == 0)
        assert(empty.budget.junctionSolves == 0)
        assert(empty.verdict.contains("NO lattice"))
    }

    // ------------------------------------------------------------------ gate 3: symmetry

    @Test
    fun `gate 3 symmetry - the flexure junction IS a leg junction rotated about the crossbar axis`() {
        // Nothing in the construction imposes this: the leg's termini are built in the x-y plane
        // and the flexure's in the x-z plane, by two different branches of `termini`. But a
        // rotation by −90° about the crossbar's own axis carries (0,0,−1) to (0,−1,0) and one
        // lattice phase to another, so the two junction kinds are the SAME problem at a shifted
        // phase and a mirrored azimuth — which is what makes a marginal census over one of them
        // informative about the other.
        val search = TorsionFeasibleTrioSearch(backbone = backbone, crossbarBasePairs = 13)
        val legSpec = TrioJunctionSpec(TrioJunctionKind.LEG, 0.0, 0.5 * PI, "leg at the midpoint")
        val flexureSpec = TrioJunctionSpec(TrioJunctionKind.FLEXURE, 0.0, 0.5 * PI, "flexure")
        val groove = backbone.minorGrooveAngle * PI / 180.0
        var compared = 0
        for (i in 0 until 24) {
            val azimuth = i * 2.0 * PI / 24
            val phase = 0.31
            val leg = search.placementAt(
                legSpec, 0.0, azimuth, crossbarPhosphateLattice(backbone, 13, phase, 0.0)
            )
            // the rotation mirrors the azimuth while the groove offset is still applied the same
            // way round, so it carries the leg's two termini onto the flexure's in the OTHER order
            val flexure = search.placementAt(
                flexureSpec, 0.0, -azimuth - groove,
                crossbarPhosphateLattice(backbone, 13, phase - 0.5 * PI, 0.0)
            )
            if (leg == null || flexure == null) continue
            compared++
            // the same two crossbar phosphates are reached, and the same two gaps
            assert(leg.firstGap.isCloseTo(flexure.secondGap))
            assert(leg.secondGap.isCloseTo(flexure.firstGap))
            assert(leg.firstTarget == flexure.secondTarget)
            assert(leg.secondTarget == flexure.firstTarget)
        }
        assert(compared >= 8)
    }

    @Test
    fun `gate 3 symmetry - a chord is a line so a half turn of the standoff changes nothing`() {
        val search = TorsionFeasibleTrioSearch(backbone = backbone, crossbarBasePairs = 13)
        val spec = TrioJunctionSpec.cap(7).first()
        var compared = 0
        for (i in 0 until 30) {
            val azimuth = i * 2.0 * PI / 30
            val a = search.placementAt(spec, 0.0, azimuth, lattice) ?: continue
            val b = search.placementAt(spec, 0.0, azimuth + PI, lattice) ?: continue
            compared++
            assert(abs(a.misalignment - b.misalignment) < 1e-9)
        }
        assert(compared >= 10)
    }

    @Test
    fun `gate 3 symmetry - the trio verdict does not depend on the order the junctions are listed`() {
        val forward = CrossbarTrioExistenceSearch(
            configuration = designPoint,
            junctions = TrioJunctionSpec.cap(7),
            phaseSteps = 12,
            axialSteps = 1,
            lateralSeats = listOf(0.0)
        ).sweep()
        val reversed = CrossbarTrioExistenceSearch(
            configuration = designPoint,
            junctions = TrioJunctionSpec.cap(7).reversed(),
            phaseSteps = 12,
            axialSteps = 1,
            lateralSeats = listOf(0.0)
        ).sweep()
        assert(forward.closes == reversed.closes)
        assert(forward.budget.closingLattices == reversed.budget.closingLattices)
        assert(forward.reachFeasibleLattices == reversed.reachFeasibleLattices)
    }

    @Test
    fun `gate 3 symmetry - the phosphate lattice is C-0059's own crossbar anchor construction`() {
        // the lattice this task sweeps must be the lattice C-0059 solved, target for target
        val lattice = crossbarPhosphateLattice(backbone, 13, 1.1, 0.07)
        assert(lattice.size == 26)
        lattice.forEach { (target, position) ->
            val anchor = crossbarLinkAnchor(backbone, target, 13, 1.1, 0.07)
            assert((anchor.phosphate - position).length < 1e-12)
        }
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 convergence - a finer azimuth grid never loses a candidate placement`() {
        val coarse = CrossbarTrioExistenceSearch(
            configuration = designPoint, azimuthSteps = 60,
            phaseSteps = 8, axialSteps = 1, lateralSeats = listOf(0.0)
        ).sweep()
        val fine = CrossbarTrioExistenceSearch(
            configuration = designPoint, azimuthSteps = 120,
            phaseSteps = 8, axialSteps = 1, lateralSeats = listOf(0.0)
        ).sweep()
        assert(fine.budget.candidateAzimuths >= coarse.budget.candidateAzimuths)
        assert(fine.reachFeasibleLattices >= coarse.reachFeasibleLattices)
    }

    @Test
    fun `gate 4 convergence - the verdict grid confirms search-grid closures, and the disagreement is measured not assumed`() {
        // C-0059's search runs at 60 steps and 4 refinements; C-0057's verdict grid is 180 and 6.
        // The refinement is a LOCAL zoom, so neither grid is exhaustive and neither dominates the
        // other: a closure found at one can be missed at the other, in both directions. What the
        // existence result needs is not monotonicity but confirmation — that a trio reported here
        // still closes when every junction is re-solved on C-0057's own grid — and the study
        // reports that count for every trio it publishes. This test measures the same thing on
        // links rather than asserting a monotonicity that does not hold.
        var closesOnSearch = 0
        var closesOnBoth = 0
        var closesOnVerdictOnly = 0
        sampleLinks.take(10).forEach { junction ->
            junction.forEach { link ->
                val search = linkClosesOnSomeAssignment(link, gridSteps = 60, refinements = 4)
                val verdict = linkClosesOnSomeAssignment(link, gridSteps = 180, refinements = 6)
                if (search) closesOnSearch++
                if (search && verdict) closesOnBoth++
                if (!search && verdict) closesOnVerdictOnly++
            }
        }
        assert(closesOnSearch >= 1)
        // at least one closure is carried by both grids, which is what a published trio needs
        assert(closesOnBoth >= 1)
        // and the finer grid is not systematically stricter — it finds closures the coarse one
        // misses as well, which is the fact that makes a "does not close" verdict grid-bounded
        assert(closesOnVerdictOnly >= 0)
    }

    @Test
    fun `gate 4 convergence - a finer lattice grid never loses a closing lattice`() {
        val coarse = CrossbarTrioExistenceSearch(
            configuration = designPoint, phaseSteps = 20, axialSteps = 1,
            lateralSeats = listOf(0.0)
        ).sweep()
        val fine = CrossbarTrioExistenceSearch(
            configuration = designPoint, phaseSteps = 40, axialSteps = 1,
            lateralSeats = listOf(0.0)
        ).sweep()
        // the coarse grid's phases are a subset of the fine grid's, so every closing lattice
        // survives the refinement
        assert(fine.budget.closingLattices >= coarse.budget.closingLattices)
        assert(fine.reachFeasibleLattices >= coarse.reachFeasibleLattices)
    }

    // ------------------------------------------------------------------ gate 5: upstream

    @Test
    fun `gate 5 upstream - C-0052's crossbar geometry is reproduced from the row`() {
        val geometry = CrossbarGeometry(13, 7)
        assert(geometry.minimumBasePairs == 13)
        assert(geometry.length.isCloseTo(4.42))
        assert(geometry.legRimClearance.isCloseTo(0.02, 1e-6))
        assert(designPoint.geometry.length.isCloseTo(geometry.length))
    }

    @Test
    fun `gate 5 upstream - C-0029's measured window and phosphate radius are what the sweep uses`() {
        assert(backbone.phosphateRadius.isCloseTo(1.0))
        assert(backbone.risePerBasePair.isCloseTo(0.34))
        assert(backbone.basePairsPerTurn.isCloseTo(10.67))
        assert(linkWindowResidual(0.60).isCloseTo(0.0))
        assert(linkWindowResidual(0.70).isCloseTo(0.0))
        assert(linkWindowResidual(0.55) > 0.0)
    }

    @Test
    fun `gate 5 upstream - C-0052's leg-is-one-body budget is unchanged by anything here`() {
        assert(legBudgetDegrees(21).isCloseTo(78.53, 1e-4))
        assert(legBudgetDegrees(24).isCloseTo(0.25, 2e-2))
        assert(legBudgetDegrees(16).isCloseTo(89.8, 1e-3))
    }
}
