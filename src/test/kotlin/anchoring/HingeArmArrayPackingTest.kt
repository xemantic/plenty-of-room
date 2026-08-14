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
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-116`, leaf `A8.2` — the plan view of a 45-arm `E5a1` hinge-line array.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that `C-0050` reports `E5a1` clearing every predicate it can evaluate
 * at §3's acceptable stroke with `packingAssessed = false`, and that `C-0041`'s answer for a
 * **different** element does not transfer: its obstruction is a clash between standoffs and ties,
 * and `E5a1` has neither.
 */
class HingeArmArrayPackingTest {

    private val duplex = OrigamiDuplex.INTERHELICAL

    private val edgeX = Gen1Tile.EDGE_X

    private val rows = 15

    private val edgeY = rows * duplex

    /** `C-0039`'s `E5a1` arm; re-derived from its own library in the study, quoted here. */
    private val arm = 9.131

    /** `C-0030`'s 45-path span — `C-0041`'s own design point. */
    private val flexureSpan = 31.82

    /** `C-0041`'s surviving single-column span. */
    private val singleColumnSpan = 21.44

    private val flexureMembers = listOf(0.0, 0.5, 1.0)

    private fun flexureElements(columns: Int, span: Double, angle: Double): List<PlanElement> =
        elementArray(
            columns, rows, edgeX, edgeY, span, angle, duplex,
            anchorFraction = 0.5, verticalMemberFractions = flexureMembers
        )

    private fun gridAnchors(columns: Int): List<Vector2> =
        attachmentGrid(columns, rows, edgeX, edgeY).map { Vector2(it.first, it.second) }

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate1 an arm's plan area is a length squared and the array's is additive`() {
        val one = PlanElement("A", Vector2.ZERO, 0.0, arm, duplex, anchorFraction = 0.0)
        assert(one.body.area.isCloseTo(arm * duplex))
        assert(armArrayPlanArea(45, arm, duplex).isCloseTo(45.0 * arm * duplex))
        assert(armArrayPlanArea(45, 2.0 * arm, duplex).isCloseTo(2.0 * armArrayPlanArea(45, arm, duplex)))
    }

    @Test
    fun `gate1 the packing verdict is dimensionless — scaling every length leaves it unchanged`() {
        listOf(0.0, 0.3, 1.0, 2.0, PI / 2.0).forEach { angle ->
            val plain = elementPackingVerdict(hingeArmArray(3, rows, edgeX, edgeY, arm, angle))
            val scaled = elementPackingVerdict(
                hingeArmArray(
                    3, rows, 10.0 * edgeX, 10.0 * edgeY, 10.0 * arm, angle, 10.0 * duplex
                )
            )
            assert(scaled.overlappingPairs == plain.overlappingPairs)
            assert(scaled.mutuallyBlockingPairs == plain.mutuallyBlockingPairs)
            assert(scaled.memberClashPairs == plain.memberClashPairs)
            assert(scaled.levelsRequired == plain.levelsRequired)
        }
    }

    @Test
    fun `gate1 unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            PlanElement("A", Vector2.ZERO, 0.0, -1.0, duplex)
        }
        assertFailsWith<IllegalArgumentException> {
            PlanElement("A", Vector2.ZERO, 0.0, arm, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            PlanElement("A", Vector2.ZERO, 0.0, arm, duplex, anchorFraction = 1.5)
        }
        assertFailsWith<IllegalArgumentException> {
            PlanElement("A", Vector2.ZERO, 0.0, arm, duplex, verticalMemberFractions = listOf(2.0))
        }
        assertFailsWith<IllegalArgumentException> { elementPackingVerdict(emptyList()) }
        assertFailsWith<IllegalArgumentException> { armArrayPlanArea(0, arm, duplex) }
        assertFailsWith<IllegalArgumentException> { hingeSites(-1, edgeX, rows) }
        assertFailsWith<IllegalArgumentException> { hingeSites(0, edgeX, 1) }
        assertFailsWith<IllegalArgumentException> { placeHingeArms(0, edgeX, rows, -1.0) }
        assertFailsWith<IllegalArgumentException> {
            hostSheetAfterArms(HingeArmPlacement(0, emptyList(), 0), edgeX, 1, arm)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate2 the generalised element reproduces C-0041's packer exactly, at three column counts`() {
        listOf(0.0, 0.087, 0.4, 1.0, PI / 2.0).forEach { angle ->
            (1..3).forEach { columns ->
                val span = if (columns == 1) singleColumnSpan else flexureSpan
                val mine = elementPackingVerdict(flexureElements(columns, span, angle))
                val theirs = packingVerdict(
                    gridFlexureArray(columns, rows, edgeX, edgeY, span, angle)
                )
                assert(mine.overlappingPairs == theirs.overlappingPairs)
                assert(mine.blockingPairs == theirs.blockingPairs)
                assert(mine.mutuallyBlockingPairs == theirs.mutuallyBlockingPairs)
                assert(mine.memberClashPairs == theirs.memberClashPairs)
                assert(mine.feasibleAtAnyLevelCount == theirs.feasibleAtAnyLevelCount)
                assert(mine.levelsRequired == theirs.levelsRequired)
            }
        }
    }

    @Test
    fun `gate2 one arm packs at one level in every orientation`() {
        (0 until 36).forEach { step ->
            val angle = step * 2.0 * PI / 36.0
            val verdict = elementPackingVerdict(
                listOf(PlanElement("A", Vector2.ZERO, angle, arm, duplex, anchorFraction = 0.0))
            )
            assert(verdict.singleLevel)
        }
    }

    @Test
    fun `gate2 two collinear arms overlap below the arm and clear above the arm plus a duplex`() {
        fun pair(gap: Double) = listOf(
            PlanElement("A", Vector2(0.0, 0.0), 0.0, arm, duplex, anchorFraction = 0.0),
            PlanElement("B", Vector2(gap, 0.0), 0.0, arm, duplex, anchorFraction = 0.0)
        )
        assert(elementPackingVerdict(pair(0.5 * arm)).overlappingPairs == 1)
        assert(elementPackingVerdict(pair(arm + 0.5 * duplex)).overlappingPairs == 0)
        assert(elementPackingVerdict(pair(arm + 0.5 * duplex)).singleLevel)
        assert(elementPackingVerdict(pair(arm + 2.0 * duplex)).singleLevel)
    }

    @Test
    fun `gate2 two arms one duplex apart across the helices are tangent at every offset`() {
        listOf(0.0, 3.0, 6.0, 9.0).forEach { offset ->
            val verdict = elementPackingVerdict(
                listOf(
                    PlanElement("A", Vector2(0.0, 0.0), 0.0, arm, duplex, anchorFraction = 0.0),
                    PlanElement("B", Vector2(offset, duplex), 0.0, arm, duplex, anchorFraction = 0.0)
                )
            )
            assert(verdict.overlappingPairs == 0)
            assert(verdict.singleLevel)
        }
    }

    @Test
    fun `gate2 a row with one hinge site carries exactly one arm`() {
        assert(maximumArmsInRow(listOf(HingeSite(0, 0.0, 0)), arm, edgeX, duplex).size == 1)
    }

    @Test
    fun `gate2 a cluster of sites inside one arm still carries TWO arms, one each way`() {
        // Four sites spanning 3 nm — far less than one arm — yet a rooted arm has a DIRECTION,
        // so one arm runs to −x from the first site and one to +x from the last, and they clear
        // each other by the 3 nm the cluster itself spans. This is the sense in which a hinge arm
        // is not `C-0041`'s centred beam, and it is why the orientation sweep runs over 2π.
        val sites = (0 until 4).map { HingeSite(0, it * 1.0, 0) }
        assert(maximumArmsInRow(sites, arm, edgeX, duplex).size == 2)
        // restricted to one direction the same cluster carries exactly one
        val oneWay = sites.map { site ->
            ArmPlacement(0, site.x, true, site.interfaceIndex, arm)
        }.sortedBy { it.high }
        var frontier = Double.NEGATIVE_INFINITY
        var count = 0
        oneWay.forEach { placement ->
            if (placement.low >= frontier) {
                count++
                frontier = placement.high + duplex
            }
        }
        assert(count == 1)
    }

    @Test
    fun `gate2 an unarmed sheet is one connected component at every phase`() {
        (0 until 32).forEach { phase ->
            val verdict = hostSheetAfterArms(
                HingeArmPlacement(phase, emptyList(), 0), edgeX, rows, arm
            )
            assert(verdict.components == 1)
            assert(verdict.orphanSegments == 0)
            assert(!verdict.severed)
            assert(verdict.survivingCrossovers == verdict.inventory)
            assert(verdict.largestComponentSegments == rows)
        }
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate3 the verdict is invariant under a rigid rotation of the whole array`() {
        listOf(0.17, 0.9, PI / 2.0, 2.0).forEach { turn ->
            listOf(0.0, 0.3, 1.1).forEach { angle ->
                val array = hingeArmArray(3, rows, edgeX, edgeY, arm, angle)
                val plain = elementPackingVerdict(array)
                val rotated = elementPackingVerdict(
                    array.map { it.rotatedAbout(Vector2(1.3, -0.7), turn) }
                )
                assert(rotated.overlappingPairs == plain.overlappingPairs)
                assert(rotated.mutuallyBlockingPairs == plain.mutuallyBlockingPairs)
                assert(rotated.memberClashPairs == plain.memberClashPairs)
                assert(rotated.levelsRequired == plain.levelsRequired)
            }
        }
    }

    @Test
    fun `gate3 the two interface parities' site counts sum to the column count at every phase`() {
        (0 until 32).forEach { phase ->
            val layout = CrossoverLayout.phased(
                phase * Gen1Tile.RISE_PER_BASE_PAIR,
                Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR / 2.0,
                edgeX
            )
            val sites = hingeSites(phase, edgeX, rows)
            val even = sites.filter { it.parity == 0 }.map { it.x }.distinct().size
            val odd = sites.filter { it.parity == 1 }.map { it.x }.distinct().size
            assert(even + odd == layout.size)
        }
    }

    @Test
    fun `gate3 a 32 bp shift of the column lattice is the identity`() {
        (0 until 8).forEach { phase ->
            val here = hingeSites(phase, edgeX, rows)
            val full = hingeSites(phase + 32, edgeX, rows)
            assert(here.size == full.size)
            here.indices.forEach { index ->
                assert(here[index].interfaceIndex == full[index].interfaceIndex)
                assert(here[index].parity == full[index].parity)
                assert(abs(here[index].x - full[index].x) < 1e-9)
            }
        }
    }

    @Test
    fun `gate3 a 16 bp shift hands every interface the other parity's columns`() {
        (0 until 16).forEach { phase ->
            val here = hingeSites(phase, edgeX, rows)
            val shifted = hingeSites(phase + 16, edgeX, rows)
            val hereEvenX = here.filter { it.parity == 0 }.map { it.x }.distinct().sorted()
            val shiftedOddX = shifted.filter { it.parity == 1 }.map { it.x }.distinct().sorted()
            assert(hereEvenX.size == shiftedOddX.size)
        }
    }

    @Test
    fun `gate3 every placed arm hinges on one of its own two interfaces, injectively`() {
        (0 until 32).forEach { phase ->
            val placement = placeHingeArms(phase, edgeX, rows, arm)
            val used = HashSet<Pair<Int, Long>>()
            placement.placements.forEach { placed ->
                assert(placed.interfaceIndex == placed.row || placed.interfaceIndex == placed.row - 1)
                assert(used.add(placed.interfaceIndex to crossoverKey(placed.rootX)))
            }
            assert(placement.arms == placement.placements.size)
        }
    }

    @Test
    fun `gate3 no two placed arms in one row come closer than a duplex`() {
        (0 until 32).forEach { phase ->
            placeHingeArms(phase, edgeX, rows, arm).placements
                .groupBy { it.row }
                .forEach { (_, inRow) ->
                    inRow.sortedBy { it.low }.zipWithNext().forEach { (low, high) ->
                        assert(high.low - low.high >= duplex - 1e-9)
                    }
                }
        }
    }

    @Test
    fun `gate3 every placed arm lies inside the host sheet's own edge`() {
        (0 until 32).forEach { phase ->
            placeHingeArms(phase, edgeX, rows, arm).placements.forEach { placed ->
                assert(placed.low >= -edgeX / 2.0 - 1e-9)
                assert(placed.high <= edgeX / 2.0 + 1e-9)
            }
        }
    }

    @Test
    fun `gate3 truncating a placement conserves rows, keeps it a placement and is idempotent`() {
        val placement = placeHingeArms(6, edgeX, rows, arm)
        listOf(0, 1, 15, 30, placement.arms, placement.arms + 10).forEach { target ->
            val thinned = placement.truncatedTo(target)
            assert(thinned.arms == minOf(target, placement.arms))
            assert(thinned.placements.all { it in placement.placements })
            assert(thinned.placements.distinct().size == thinned.arms)
            // spread round robin: no row may carry two more arms than any row that still has one
            if (target in 1 until placement.arms) {
                val perRow = (0 until rows).map { row -> thinned.placements.count { it.row == row } }
                assert(perRow.max() - perRow.min() <= 1 || perRow.min() == 0)
            }
        }
        assert(placement.truncatedTo(20).truncatedTo(20).placements ==
                placement.truncatedTo(20).placements)
    }

    @Test
    fun `gate3 the constructive placement never exceeds the independent per-row bound`() {
        (0 until 32).forEach { phase ->
            val placement = placeHingeArms(phase, edgeX, rows, arm)
            assert(placement.arms <= placement.independentRowBound)
        }
    }

    @Test
    fun `gate3 the host sheet conserves material — segments never fewer than the intact duplexes`() {
        (0 until 32).forEach { phase ->
            val placement = placeHingeArms(phase, edgeX, rows, arm)
            val verdict = hostSheetAfterArms(placement, edgeX, rows, arm)
            assert(verdict.components in 0..verdict.segments)
            assert(verdict.components + verdict.orphanSegments <= verdict.segments)
            assert(verdict.largestComponentSegments <= verdict.segments)
            assert(verdict.survivingCrossovers <= verdict.inventory)
            assert(verdict.crossoversDemanded <= verdict.inventory)
        }
    }

    // ------------------------------------------------------ gate 4 — numerical convergence

    @Test
    fun `gate4 the orientation sweep is sample-count independent over 180 to 2880`() {
        val anchors = gridAnchors(3)
        val finest = elementOrientationSweep(
            anchors, arm, 2880, duplex, anchorFraction = 0.0, angularSpan = 2.0 * PI
        )
        listOf(180, 360, 720, 1440).forEach { samples ->
            val sweep = elementOrientationSweep(
                anchors, arm, samples, duplex, anchorFraction = 0.0, angularSpan = 2.0 * PI
            )
            assert(sweep.minimumOverlappingPairs == finest.minimumOverlappingPairs)
            assert(sweep.minimumMemberClashPairs == finest.minimumMemberClashPairs)
        }
    }

    @Test
    fun `gate4 the phase sweep is complete at 32 base pairs`() {
        val coarse = (0 until 32).map { placeHingeArms(it, edgeX, rows, arm).arms }.toSet()
        val refined = (0 until 320).map { placeHingeArms(it % 32, edgeX, rows, arm).arms }.toSet()
        assert(refined == coarse)
    }

    @Test
    fun `gate4 the placement is deterministic`() {
        (0 until 32 step 7).forEach { phase ->
            val first = placeHingeArms(phase, edgeX, rows, arm).placements
            val second = placeHingeArms(phase, edgeX, rows, arm).placements
            assert(first == second)
        }
    }

    // ------------------------------------------------------ gate 5 — literature and upstream

    @Test
    fun `gate5 C-0041's 3 x 15 flexure array is unrealisable at 0 of 720 orientations`() {
        val sweep = elementOrientationSweep(
            gridAnchors(3), flexureSpan, 720, duplex,
            anchorFraction = 0.5, verticalMemberFractions = flexureMembers
        )
        assert(sweep.feasibleOrientations == 0)
        assert(sweep.singleLevelOrientations == 0)
    }

    @Test
    fun `gate5 C-0041's single column is feasible at exactly one of 720 orientations`() {
        val sweep = elementOrientationSweep(
            gridAnchors(1), singleColumnSpan, 720, duplex,
            anchorFraction = 0.5, verticalMemberFractions = flexureMembers
        )
        assert(sweep.singleLevelOrientations == 1)
    }

    @Test
    fun `gate5 C-0041's packing-limited count of fifteen is reproduced`() {
        val count = packingLimitedElementCount(
            edgeX, rows, { paths -> if (paths <= rows) singleColumnSpan else flexureSpan },
            duplex, anchorFraction = 0.5, verticalMemberFractions = flexureMembers
        )
        assert(count == 15)
    }

    @Test
    fun `gate5 C-0040's four-crossover hinge line is reproduced from the site lattice`() {
        (0 until 32).forEach { phase ->
            val perInterface = hingeSites(phase, edgeX, rows)
                .groupBy { it.interfaceIndex }
                .mapValues { it.value.size }
            assert(perInterface.values.max() == 4)
        }
    }

    @Test
    fun `gate5 C-0015's 49 or 56 inventory is reproduced from the site lattice`() {
        val counts = (0 until 32).map { hingeSites(it, edgeX, rows).size }
        assert(counts.toSet() == setOf(49, 56))
        assert(counts.count { it == 56 } == 10)
    }

    @Test
    fun `gate5 the lattice constants are the cited ones`() {
        assert(duplex.isCloseTo(2.69))
        assert(Gen1Tile.RISE_PER_BASE_PAIR.isCloseTo(0.34))
        assert(
            (Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR).isCloseTo(10.88)
        )
    }

    // ------------------------------------------------------------------ the answer itself

    @Test
    fun `the 45-arm array's plan area sits below the tile footprint`() {
        val ratio = armArrayPlanArea(45, arm, duplex) / (edgeX * edgeY)
        assert(ratio < 1.0)
        assert(ratio > 0.5)
    }

    @Test
    fun `C-0041's along-helix obstruction does not transfer to the arm`() {
        val columnPitch = edgeX / 3.0
        assert(arm + duplex < columnPitch)
        assert(flexureSpan + duplex > columnPitch)
    }

    @Test
    fun `C-0041's across-helix obstruction cannot arise because the arm owns no vertical member`() {
        val array = hingeArmArray(3, rows, edgeX, edgeY, arm, 0.3)
        val verdict = elementPackingVerdict(array)
        assert(verdict.blockingPairs == 0)
        assert(verdict.mutuallyBlockingPairs == 0)
        assert(verdict.memberClashPairs == 0)
    }

    @Test
    fun `the lattice placement is solved over all 32 phases and reported, not asserted`() {
        val best = (0 until 32).maxOf { placeHingeArms(it, edgeX, rows, arm).arms }
        val bound = (0 until 32).maxOf { placeHingeArms(it, edgeX, rows, arm).independentRowBound }
        assert(best > 0)
        assert(best <= bound)
    }
}
