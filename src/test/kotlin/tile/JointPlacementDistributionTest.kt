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
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-323` — the placement and the distribution searched **together**.
 *
 * Written before `tile/JointPlacementDistribution.kt` exists, and watched fail.
 *
 * The gates each test names are `T-323`'s own `P1`–`P8` and `F1`–`F23`.
 */
class JointPlacementDistributionTest {

    /** `C-0205`'s transverse ceiling, the constant every resolution in this task is read at. */
    private val shearCeiling = 254.80809548301096

    /** `C-0208`'s radial bracket **floor**, the rung its tightest cell lives at. */
    private val radialFloor = 754.005141

    private val block = HoneycombBlock(4, 2)

    private val rowBasePairs = 56

    private val edgeX = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR

    private val rowPitch =
        HoneycombCrossSectionGeometry.rowPitch(Gen1Tile.INTERHELICAL_HONEYCOMB)

    private val edgeY = block.rasterRows * rowPitch

    private val pressure = uniformPressure(Gen1Tile.TARGET_FORCE / (edgeX * edgeY))

    private fun lattice(radial: Double? = radialFloor) = honeycombTiedLatticeAtResolvedLink(
        block = block,
        rowBasePairs = rowBasePairs,
        enhancement = 1.0,
        tied = true,
        transverseLinkStiffness =
            if (radial == null) HoneycombGrillage.RIGID_LINK_STIFFNESS else shearCeiling,
        radialLinkStiffness = radial
    )

    /** A small, deliberately ASYMMETRIC family: rows of 3 and 4 stations at two `y`. */
    private fun smallFamily(columns: Int) = JointPlacementFamily(
        rowStations = listOf(
            listOf(-4.0, 0.0, 4.0),
            listOf(-5.0, -1.0, 3.0, 7.0),
            listOf(-4.0, 0.0, 4.0),
            listOf(-5.0, -1.0, 3.0, 7.0)
        ),
        rowY = listOf(-6.0, -2.0, 2.0, 6.0),
        columns = columns
    )

    /** A family that IS centro-symmetric, so the census can be tested in both directions. */
    private fun symmetricFamily(columns: Int) = JointPlacementFamily(
        rowStations = listOf(
            listOf(-4.0, 0.0, 4.0),
            listOf(-4.0, 0.0, 4.0)
        ),
        rowY = listOf(-3.0, 3.0),
        columns = columns
    )

    /** `C-0140`'s drawable raster of the recommended `10 x 6` block. */
    private fun recommendedRaster() = twoLengthRaster(10, 6, 102, 109)

    // ------------------------------------- gate 1: the family is a product, and it is exact

    @Test
    fun `gate 1 -- a row option count is n choose k and the family size is their product`() {
        val family = smallFamily(2)
        assert(family.rowOptionCounts == listOf(3, 6, 3, 6))
        assert(family.size == 324L)
        assert(family.rasterRows == 4)
        assert(family.stationCount == 14)
        assert(family.pathCount == 8)
    }

    @Test
    fun `gate 1 -- every enumerated placement carries the columns count per row at its own y`() {
        val family = smallFamily(2)
        val all = family.enumerate().toList()
        assert(all.size == 324)
        all.forEach { placement ->
            assert(placement.key.size == 4)
            placement.key.forEachIndexed { row, chosen ->
                assert(chosen.size == 2)
                assert(chosen.distinct().size == 2)
                assert(chosen == chosen.sorted())
            }
            assert(placement.grid.size == 8)
            placement.grid.forEachIndexed { index, (_, y) ->
                assert(y == family.rowY[index / 2])
            }
        }
        assert(all.map { it.label }.distinct().size == 324)
    }

    @Test
    fun `gate 1 -- the enumeration is lexicographic and its first member is the lowest key`() {
        val family = smallFamily(2)
        val all = family.enumerate().toList()
        assert(all.first().key == listOf(listOf(0, 1), listOf(0, 1), listOf(0, 1), listOf(0, 1)))
        assert(all.last().key == listOf(listOf(1, 2), listOf(2, 3), listOf(1, 2), listOf(2, 3)))
        val labels = all.map { it.label }
        assert(labels == labels.sorted())
    }

    @Test
    fun `gate 1 -- the bank index is a bijection onto the whole candidate station set`() {
        val family = smallFamily(3)
        val indices = (0 until family.rasterRows).flatMap { row ->
            family.rowStations[row].indices.map { family.bankIndex(row, it) }
        }
        assert(indices.sorted() == (0 until family.stationCount).toList())
        assert(family.stations.size == family.stationCount)
        assert(family.stations[family.bankIndex(1, 2)] == (3.0 to -2.0))
    }

    @Test
    fun `gate 1 -- the paired median ratio is not the ratio of two order statistics`() {
        // A deliberately constructed pair: B wins on the 90th percentile and loses on most
        // realisations, which is `CLAUDE.md`'s own trap and the reason both are emitted.
        val a = doubleArrayOf(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 10.0)
        val b = doubleArrayOf(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 3.0)
        assert(abs(pairedMedianRatio(a, b) - 0.5) < 1e-15)
        assertFailsWith<IllegalArgumentException> { pairedMedianRatio(a, doubleArrayOf(1.0)) }
        assertFailsWith<IllegalArgumentException> {
            pairedMedianRatio(doubleArrayOf(1.0), doubleArrayOf(0.0))
        }
    }

    // ------------------------------------- gate 2: limiting cases and refusals

    @Test
    fun `gate 2 -- a saturated family has exactly one member and it is the whole ladder`() {
        val family = JointPlacementFamily(
            rowStations = listOf(listOf(-1.0, 1.0), listOf(-2.0, 2.0)),
            rowY = listOf(-1.0, 1.0),
            columns = 2
        )
        assert(family.size == 1L)
        val only = family.enumerate().toList()
        assert(only.size == 1)
        assert(only.single().key == listOf(listOf(0, 1), listOf(0, 1)))
    }

    @Test
    fun `gate 2 -- the family refuses a column count no row can supply and a ragged datum`() {
        assertFailsWith<IllegalArgumentException> { smallFamily(4) }
        assertFailsWith<IllegalArgumentException> { smallFamily(0) }
        assertFailsWith<IllegalArgumentException> {
            JointPlacementFamily(listOf(listOf(1.0, 2.0)), listOf(0.0, 1.0), 1)
        }
        assertFailsWith<IllegalArgumentException> {
            JointPlacementFamily(emptyList(), emptyList(), 1)
        }
        assertFailsWith<IllegalArgumentException> {
            JointPlacementFamily(listOf(listOf(2.0, 1.0)), listOf(0.0), 1)
        }
    }

    @Test
    fun `gate 2 -- a placement refuses a key of the wrong shape or an unsorted one`() {
        val family = smallFamily(2)
        assertFailsWith<IllegalArgumentException> { family.placementAt(listOf(listOf(0, 1))) }
        assertFailsWith<IllegalArgumentException> {
            family.placementAt(listOf(listOf(0, 1), listOf(1, 0), listOf(0, 1), listOf(0, 1)))
        }
        assertFailsWith<IllegalArgumentException> {
            family.placementAt(listOf(listOf(0, 9), listOf(0, 1), listOf(0, 1), listOf(0, 1)))
        }
        assertFailsWith<IllegalArgumentException> {
            family.placementAt(listOf(listOf(0, 0), listOf(0, 1), listOf(0, 1), listOf(0, 1)))
        }
    }

    @Test
    fun `gate 2 -- the descent refuses a zero-sweep search and a foreign start`() {
        val family = smallFamily(2)
        val start = family.enumerate().first()
        assertFailsWith<IllegalArgumentException> {
            descendJointPlacement(start, 0) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            descendJointPlacement(start, 2, starts = emptyList()) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            descendJointPlacement(
                start, 2, starts = listOf(smallFamily(3).enumerate().first())
            ) { 1.0 }
        }
    }

    // ------------------------------------- gate 3: symmetry, the bank identity, the falsifiers

    @Test
    fun `gate 3 -- the honeycomb station bank sliced to a placement equals its own surrogate`() {
        val host = lattice()
        val candidates = attachmentGrid(3, block.rasterRows, edgeX, edgeY)
        val bank = HoneycombStationBank(host, candidates, pressure, samples = 21)
        val indices = listOf(0, 2, 4, 6, 8, 10)
        val sliced = bank.surrogateFor(indices)
        val alone = honeycombTiedSurrogate(host, indices.map { candidates[it] }, pressure, 21)
        val stiffnesses = List(indices.size) { MANDATED_TOTAL_STIFFNESS / indices.size }
        val a = sliced.solve(stiffnesses).peakDishing
        val b = alone.solve(stiffnesses).peakDishing
        assert(abs(a - b) < 1e-10 * abs(b))
        assert(sliced.grid == indices.map { candidates[it] })
        assertFailsWith<IllegalArgumentException> { bank.surrogateFor(emptyList()) }
        assertFailsWith<IllegalArgumentException> { bank.surrogateFor(listOf(0, 0)) }
        assertFailsWith<IllegalArgumentException> { bank.surrogateFor(listOf(99)) }
    }

    @Test
    fun `gate 3 -- the bank's sliced one-state surrogate equals honeycombMultiStateSurrogate`() {
        val host = lattice()
        val candidates = attachmentGrid(3, block.rasterRows, edgeX, edgeY)
        val bank = HoneycombStationBank(host, candidates, pressure, samples = 21)
        val indices = listOf(1, 3, 5, 7, 9, 11)
        val sliced = bank.multiStateFor(indices, "a load state")
        val alone = honeycombMultiStateSurrogate(
            host, indices.map { candidates[it] },
            singleLoadState("a load state", pressure), 21
        )
        val stiffnesses = List(indices.size) { MANDATED_TOTAL_STIFFNESS / indices.size }
        val a = sliced.peakDishing(stiffnesses).single()
        val b = alone.peakDishing(stiffnesses).single()
        assert(abs(a - b) < 1e-10 * abs(b))
        assert(sliced.stateNames == listOf("a load state"))
        assertFailsWith<IllegalArgumentException> { bank.multiStateFor(emptyList(), "x") }
    }

    @Test
    fun `gate 3 -- a uniform pressure on the free lattice at the resolved link dishes zero`() {
        val host = lattice()
        val stroke = host.solve(uniformPressure(Gen1Tile.TARGET_FORCE / (edgeX * edgeY)))
            .meanDeflection
        val dishing = host.solve(uniformPressure(Gen1Tile.TARGET_FORCE / (edgeX * edgeY)))
            .peakDishing(41) / stroke
        assert(dishing < 1e-9)
    }

    @Test
    fun `gate 3 -- the bank at full presence reproduces the assembled solve`() {
        val host = lattice()
        val candidates = attachmentGrid(2, block.rasterRows, edgeX, edgeY)
        val bank = HoneycombStationBank(host, candidates, pressure, samples = 21)
        val indices = candidates.indices.toList()
        val surrogate = bank.surrogateFor(indices)
        val stiffnesses = List(indices.size) { MANDATED_TOTAL_STIFFNESS / indices.size }
        val response = surrogate.solve(stiffnesses)
        val loads = candidates.mapIndexed { index, (s, y) ->
            PointLoad(s, y, -response.supportForces[index])
        }
        val assembled = host.solve(pressure, loads)
        val peak = assembled.peakDishing(21)
        assert(abs(peak - response.peakDishing) < 1e-9 * abs(response.peakDishing))
    }

    @Test
    fun `gate 3 -- centro-symmetry is measured in BOTH directions on the same predicate`() {
        val symmetric = symmetricFamily(2)
        assert(symmetric.centroSymmetricRowPairs == 2)
        assert(symmetric.admitsCentroSymmetry)
        assert(symmetric.enumerate().any { it.isCentroSymmetric() })
        val asymmetric = smallFamily(2)
        assert(asymmetric.centroSymmetricRowPairs == 0)
        assert(!asymmetric.admitsCentroSymmetry)
        assert(asymmetric.enumerate().none { it.isCentroSymmetric() })
    }

    @Test
    fun `gate 3 -- the two-by-two split's two orderings agree and its interaction is their gap`() {
        val split = placementDistributionSplit(
            fixedPlacementTransferred = 0.106508519,
            searchedPlacementTransferred = 0.09,
            fixedPlacementSearched = 0.078544978,
            searchedPlacementSearched = 0.07
        )
        assert(split.pathDisagreement < 1e-12)
        assert(abs(split.total - ln(0.07 / 0.106508519)) < 1e-12)
        assert(abs(split.countTermAtFromPhase - ln(0.09 / 0.106508519)) < 1e-12)
        assert(abs(split.phaseTermAtFromCount - ln(0.078544978 / 0.106508519)) < 1e-12)
        val byPlacement = split.countTermAtToPhase - split.countTermAtFromPhase
        val byDistribution = split.phaseTermAtToCount - split.phaseTermAtFromCount
        assert(abs(byPlacement - byDistribution) < 1e-12)
        assert(abs(split.interaction - byPlacement) < 1e-12)
    }

    @Test
    fun `gate 3 -- a separable two-by-two has EXACTLY zero interaction`() {
        val split = placementDistributionSplit(1.0, 0.5, 0.25, 0.125)
        assert(abs(split.interaction) < 1e-15)
        assert(split.separableWithin(1e-12))
    }

    /**
     * The three centro-symmetry fixtures the first mutation run said were missing.
     *
     * `C-0161`'s standard read from the other side: a mutation that fails nothing is a
     * measurement of the FIXTURE. The census has three independent clauses — the two rows must
     * be antisymmetric in `y`, the intersection must be non-empty, and it must hold `columns`
     * stations — and the first draft's families satisfied or violated all three at once, so two
     * of the three could be deleted with no test noticing.
     */
    @Test
    fun `gate 3 -- the centro-symmetry census needs all three of its clauses`() {
        // (a) the ladders mirror and the two rows' y do NOT: the census must refuse.
        val skewed = JointPlacementFamily(
            rowStations = listOf(listOf(-4.0, 0.0, 4.0), listOf(-4.0, 0.0, 4.0)),
            rowY = listOf(-3.0, 5.0),
            columns = 2
        )
        assert(skewed.centroSymmetricRowPairs == 0)
        assert(!skewed.admitsCentroSymmetry)
        // (b) exactly ONE matched station per row pair, against a two-column placement: the row
        // pair admits a symmetric STATION and the family admits no symmetric PLACEMENT.
        val thin = JointPlacementFamily(
            rowStations = listOf(listOf(0.0, 1.0, 2.0), listOf(0.0, 3.0, 5.0)),
            rowY = listOf(-3.0, 3.0),
            columns = 2
        )
        assert(thin.centroSymmetricRowPairs == 2)
        assert(!thin.admitsCentroSymmetry)
        assert(JointPlacementFamily(thin.rowStations, thin.rowY, 1).admitsCentroSymmetry)
        // (c) a rotation is not a mirror: a family whose two rows carry DIFFERENT ladders
        // separates (s, y) -> (-s, -y) from (s, y) -> (-s, y).
        val chiral = JointPlacementFamily(
            rowStations = listOf(listOf(-4.0, 0.0), listOf(0.0, 4.0)),
            rowY = listOf(-3.0, 3.0),
            columns = 1
        )
        val rotated = chiral.placementAt(listOf(listOf(0), listOf(1)))
        assert(rotated.grid == listOf(-4.0 to -3.0, 4.0 to 3.0))
        assert(rotated.isCentroSymmetric())
        val mirrored = chiral.placementAt(listOf(listOf(0), listOf(0)))
        assert(mirrored.grid == listOf(-4.0 to -3.0, 0.0 to 3.0))
        assert(!mirrored.isCentroSymmetric())
    }

    /**
     * A guard whose only observable behaviour is duplicated downstream is a guard no mutation of
     * it can reach (`C-0207`), and two of this file's are: widening the family's own column
     * check lets `ascendingSubsets` throw instead, and widening the descent's family check lets
     * `placementAt` throw instead. Both then raise the same EXCEPTION TYPE, so only the MESSAGE
     * separates the guard from its understudy.
     */
    @Test
    fun `gate 2 -- two guards are distinguishable from their understudies only by message`() {
        val tooMany = assertFailsWith<IllegalArgumentException> { smallFamily(4) }
        assert(tooMany.message!!.contains("change of the path COUNT"))
        val foreign = assertFailsWith<IllegalArgumentException> {
            descendJointPlacement(
                smallFamily(2).enumerate().first(), 2,
                starts = listOf(smallFamily(3).enumerate().first())
            ) { 1.0 }
        }
        assert(foreign.message!!.contains("same placement family"))
    }

    @Test
    fun `gate 2 -- a key with the wrong number of stations in ONE row is refused`() {
        val family = smallFamily(2)
        assertFailsWith<IllegalArgumentException> {
            family.placementAt(
                listOf(listOf(0, 1, 2), listOf(0, 1), listOf(0, 1), listOf(0, 1))
            )
        }
        assertFailsWith<IllegalArgumentException> {
            family.placementAt(listOf(listOf(0), listOf(0, 1), listOf(0, 1), listOf(0, 1)))
        }
    }

    @Test
    fun `gate 2 -- nearest refuses two columns that snap onto ONE ladder station`() {
        val family = JointPlacementFamily(
            rowStations = listOf(listOf(-4.0, 0.0, 4.0), listOf(-4.0, 0.0, 4.0)),
            rowY = listOf(-3.0, 3.0),
            columns = 2
        )
        val collided = assertFailsWith<IllegalArgumentException> {
            family.nearest(listOf(0.1 to -3.0, -0.1 to -3.0, -4.0 to 3.0, 4.0 to 3.0))
        }
        assert(collided.message!!.contains("collide"))
        // and the same call one station apart is admitted, so the guard is not simply refusing
        assert(
            family.nearest(listOf(-3.9 to -3.0, 0.1 to -3.0, -4.0 to 3.0, 4.0 to 3.0)).key ==
                    listOf(listOf(0, 1), listOf(0, 2))
        )
    }

    /**
     * The decision precision, tested on the predicate itself rather than through a descent.
     *
     * A candidate better by less than six significant digits must NOT displace an incumbent whose
     * key sorts earlier; one better by more than that must. Testing it through the descent alone
     * cannot see the difference, because a perturbation in the adverse direction is refused by a
     * raw comparison too — which is why the first mutation run left this rule held open by
     * nothing.
     */
    @Test
    fun `gate 4 -- jointPlacementBetter decides at six significant digits, both ways`() {
        assert(!jointPlacementBetter(1.0 - 1e-12, "zz", 1.0, "aa"))
        assert(jointPlacementBetter(1.0 - 1e-3, "zz", 1.0, "aa"))
        assert(jointPlacementBetter(1.0 - 1e-12, "aa", 1.0, "zz"))
        assert(!jointPlacementBetter(1.0, "zz", 1.0, "aa"))
        assert(!jointPlacementBetter(1.0 + 1e-3, "aa", 1.0, "zz"))
    }

    /**
     * `C-0104`'s trap at the bank: an influence taken on a **prestrained** lattice is that
     * influence *plus* the prestrain's own response, and the Woodbury matrix then stops being a
     * compliance. The free field must move with the prestrain and the compliance must not.
     *
     * At one station the compliance is recoverable from public quantities alone:
     * `f = w_free/(M + 1/k)`, so `M = w_free/f - 1/k`.
     */
    @Test
    fun `gate 3 -- the prestrain moves the free field and not the bank's compliance`() {
        val station = listOf(2.0 to 3.0)
        val k = 4.0
        fun complianceAt(angle: Double): Pair<Double, Double> {
            val armed = honeycombTiedLattice(
                block, rowBasePairs, 1.0, tied = true, prestrainRadians = angle
            )
            val bank = HoneycombStationBank(armed, station, pressure, 21)
            val surrogate = bank.surrogateFor(listOf(0))
            val free = armed.solve(pressure).deflection(2.0, 3.0)
            val force = surrogate.solve(listOf(k)).supportForces[0]
            return free to (free / force - 1.0 / k)
        }
        val (freeZero, mZero) = complianceAt(0.0)
        val (freeLoaded, mLoaded) = complianceAt(0.05)
        assert(abs(freeLoaded - freeZero) > 1e-9)
        assert(abs(mLoaded - mZero) < 1e-7 * abs(mZero))
    }

    // ------------------------------------- gate 4: determinism of the search

    @Test
    fun `gate 4 -- the descent is deterministic and its tie-break keeps the smaller key`() {
        val family = smallFamily(2)
        val start = family.enumerate().first()
        val objective = { placement: JointPlacement ->
            placement.grid.sumOf { (s, _) -> s * s }
        }
        val a = descendJointPlacement(start, 3, objective = objective)
        val b = descendJointPlacement(start, 3, objective = objective)
        assert(a.label == b.label)
        // A constant objective must not move at all: every candidate ties, and the incumbent
        // start has the smallest key of its own family.
        val flat = descendJointPlacement(start, 3) { 1.0 }
        assert(flat.label == start.label)
    }

    @Test
    fun `gate 4 -- the descent decides at SIX significant digits and not at sixteen`() {
        val family = smallFamily(2)
        val start = family.enumerate().first()
        // A perturbation below the decision precision cannot move the incumbent.
        val objective = { placement: JointPlacement ->
            if (placement.label == start.label) 1.0 else 1.0 + 1e-12
        }
        assert(descendJointPlacement(start, 2, objective = objective).label == start.label)
        val coarse = { placement: JointPlacement ->
            if (placement.label == start.label) 1.0 else 0.9
        }
        assert(descendJointPlacement(start, 2, objective = coarse).label != start.label)
    }

    @Test
    fun `gate 4 -- the exhaustive optimum is the enumeration's own minimum under the same rule`() {
        val family = smallFamily(2)
        val objective = { placement: JointPlacement ->
            placement.grid.sumOf { (s, _) -> abs(s) }
        }
        val best = exhaustiveJointPlacement(family, objective)
        val byHand = family.enumerate().toList()
            .sortedWith(compareBy({ objective(it) }, { it.label })).first()
        assert(best.label == byHand.label)
        // And the descent, on a family this small, must reach it.
        val descended = descendJointPlacement(family.enumerate().first(), 6, objective = objective)
        assert(abs(objective(descended) - objective(best)) < 1e-12)
    }


    // ------------------------------------- gate 4: the decision precision at EVERY call site

    /**
     * `T-328`. The discriminating case is a candidate better by **less** than the decision
     * precision: rounded it ties and cannot move the incumbent, unrounded it wins. A test that
     * perturbs the objective only *upward* holds nothing open, because a raw comparison refuses
     * an upward perturbation too.
     */
    @Test
    fun `gate 4 -- decidesBetter is the decision precision and nothing else`() {
        assert(!decidesBetter(1.0 - 1e-12, 1.0))
        assert(!decidesBetter(1.0, 1.0 - 1e-12))
        assert(decidesBetter(1.0 - 1e-3, 1.0))
        assert(!decidesBetter(1.0 + 1e-3, 1.0))
        // It is a RELATIVE precision: the SAME absolute perturbation decides at magnitude one
        // and ties at magnitude 1e6.
        assert(!decidesBetter(1e6 - 1e-3, 1e6))
    }

    @Test
    fun `gate 4 -- an argmin refuses a candidate better by less than the precision`() {
        val tied = listOf("zz" to 1.0 - 1e-12, "aa" to 1.0)
        // Unrounded, the larger-labelled candidate wins; at the decision precision it ties and
        // the label refuses it.
        assert(tied.minByOrNull { it.second }!!.first == "zz")
        assert(decisionArgmin(tied, { it.first }, { it.second }).first == "aa")
        val decided = listOf("zz" to 1.0 - 1e-3, "aa" to 1.0)
        assert(decisionArgmin(decided, { it.first }, { it.second }).first == "zz")
        assertFailsWith<IllegalArgumentException> {
            decisionArgmin(emptyList<Pair<String, Double>>(), { it.first }, { it.second })
        }
    }

    @Test
    fun `gate 4 -- the decision comparator sorts on the ROUNDED key and then on the label`() {
        val candidates = listOf("zz" to 1.0 - 1e-12, "aa" to 1.0, "mm" to 0.5)
        assert(
            candidates.sortedWith(byDecisionThenLabel({ it.first }, { it.second }))
                .map { it.first } == listOf("mm", "aa", "zz")
        )
        assert(
            candidates.sortedWith(compareBy({ it.second }, { it.first }))
                .map { it.first } == listOf("mm", "zz", "aa")
        )
    }

    /**
     * The contract that makes the repair affordable at the two sites whose key is a whole
     * dropout ensemble: `screenObjective` and `t323P90` are solves, not lookups.
     */
    @Test
    fun `gate 4 -- an argmin evaluates its key EXACTLY once per candidate`() {
        var calls = 0
        val chosen = decisionArgmin((0 until 7).toList(), { "c" + it }) {
            calls++
            (it - 3.0) * (it - 3.0)
        }
        assert(chosen == 3)
        assert(calls == 7)
    }

    /**
     * Which is what makes routing the two sites that were ALREADY rounded — they consume
     * `T-316`'s `percentileObjective`, which rounds — provably inert rather than a second
     * rounding.
     */
    @Test
    fun `gate 4 -- the decision key is idempotent, so an already-rounded objective is inert`() {
        listOf(
            1.0, 0.1, 9.999995, 9.9999949, 1e-9, 1.0 / 3.0, 0.107990116, 1e6 + 0.4999, -2.7182818
        ).forEach {
            val once = searchDecisionKey(it)
            assert(searchDecisionKey(once) == once)
        }
        assert(searchDecisionKey(0.0) == 0.0)
        assert(searchDecisionKey(-1.0000004) == -1.0)
    }

    @Test
    fun `gate 4 -- jointPlacementBetter defers to decidesBetter and ties on the label`() {
        assert(jointPlacementBetter(0.9, "zz", 1.0, "aa") == decidesBetter(0.9, 1.0))
        assert(jointPlacementBetter(1.1, "aa", 1.0, "zz") == decidesBetter(1.1, 1.0))
        assert(jointPlacementBetter(1.0 - 1e-12, "aa", 1.0, "zz"))
        assert(!jointPlacementBetter(1.0 - 1e-12, "zz", 1.0, "aa"))
    }

    /**
     * `determinedRankFromBest` and `jointWinnerRankInThisScreen` are the same decision read as a
     * COUNT, and a raw `<` over 7 776 near-equal placements moves by an ulp.
     */
    @Test
    fun `gate 4 -- a RANK is a decision too and counts betterment at the same precision`() {
        val values = doubleArrayOf(1.0, 1.0 - 1e-12, 1.0 - 1e-3, 1.0 + 1e-3)
        assert(values.count { decidesBetter(it, 1.0) } == 1)
        assert(values.count { it < 1.0 } == 2)
    }

    // ------------------------------------- gate 3: an identity is a THRESHOLD and a BOOLEAN

    /**
     * `T-329`. `F9`'s and `F10`'s residuals are quantities whose true value is **zero**, so every
     * digit of them is machine noise and one such field makes a whole result file permanently
     * un-diffable. What is emitted is the tolerance the identity is asserted at and whether it
     * holds; this is the rule, in one place, so a mutation of it is visible.
     */
    @Test
    fun `gate 3 -- an identity reports a TOLERANCE and a boolean and never its residual`() {
        assert(identityHolds(9.6e-16, 1e-10))
        assert(identityHolds(3.8e-16, 1e-10))
        assert(identityHolds(-9.6e-16, 1e-10))
        assert(!identityHolds(2e-9, 1e-9))
        assert(!identityHolds(-2e-9, 1e-9))
        assert(!identityHolds(1e-9, 1e-9))
        assert(!identityHolds(Double.NaN, 1e-9))
        assert(!identityHolds(Double.POSITIVE_INFINITY, 1e-9))
        assertFailsWith<IllegalArgumentException> { identityHolds(0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { identityHolds(0.0, -1e-9) }
    }

    // ------------------------------------- gate 5: the corpus's own lattice, reproduced

    @Test
    fun `gate 5 -- the recommended raster's ladder is 55 stations and 5-6 by row`() {
        val raster = recommendedRaster()
        val ladder = raster.stationLattice(16, 14, Gen1Tile.RISE_PER_BASE_PAIR)
        assert(ladder.size == 10)
        assert(ladder.map { it.size } == listOf(5, 6, 5, 6, 5, 6, 5, 6, 5, 6))
        assert(ladder.sumOf { it.size } == 55)
    }

    @Test
    fun `gate 5 -- the family size at five columns is six to the fifth and it is exhaustible`() {
        val raster = recommendedRaster()
        val ladder = raster.stationLattice(16, 14, Gen1Tile.RISE_PER_BASE_PAIR)
        val y = (0 until 10).map { (it - 4.5) * rowPitch }
        assert(JointPlacementFamily(ladder, y, 5).size == 7776L)
        assert(JointPlacementFamily(ladder, y, 1).size == 24_300_000L)
        assert(JointPlacementFamily(ladder, y, 2).size == 75_937_500_000L)
        assert(JointPlacementFamily(ladder, y, 3).size == 320_000_000_000L)
    }

    @Test
    fun `gate 5 -- the recommended family admits NO centro-symmetric member at any column count`() {
        val raster = recommendedRaster()
        val ladder = raster.stationLattice(16, 14, Gen1Tile.RISE_PER_BASE_PAIR)
        val y = (0 until 10).map { (it - 4.5) * rowPitch }
        listOf(1, 2, 3, 5).forEach { columns ->
            val family = JointPlacementFamily(ladder, y, columns)
            assert(family.centroSymmetricRowPairs == 0)
            assert(!family.admitsCentroSymmetry)
        }
    }

    @Test
    fun `gate 5 -- C-0167's own determined placement is a MEMBER of the searched family`() {
        val raster = recommendedRaster()
        val ladder = raster.stationLattice(16, 14, Gen1Tile.RISE_PER_BASE_PAIR)
        val y = (0 until 10).map { (it - 4.5) * rowPitch }
        val edgeYHere = 10 * rowPitch
        listOf(1, 2, 3, 5).forEach { columns ->
            val family = JointPlacementFamily(ladder, y, columns)
            val determined = twoLengthSnappedGrid(raster, columns, edgeYHere, 16, 14)
                .mapIndexed { index, (x, _) -> x to y[index / columns] }
            val member = family.nearest(determined)
            assert(member.grid.size == determined.size)
            member.grid.forEachIndexed { index, (s, yy) ->
                assert(abs(s - determined[index].first) < 1e-9)
                assert(abs(yy - determined[index].second) < 1e-9)
            }
        }
    }
}
