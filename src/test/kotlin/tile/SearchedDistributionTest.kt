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
import com.xemantic.nano.plentyofroom.coupling.LoadState
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.oracleFloorSample
import com.xemantic.nano.plentyofroom.coupling.orderStatistic
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-316` — a distribution **searched** at the resolved per-bond link.
 *
 * Written before `tile/SearchedDistribution.kt` exists, and watched fail.
 *
 * The gates each test names are `T-316`'s own `P1`–`P6` and `F1`–`F13`.
 */
class SearchedDistributionTest {

    /** `C-0205`'s transverse ceiling, the constant every resolution in this task is read at. */
    private val shearCeiling = 254.80809548301096

    /** `C-0208`'s radial bracket **floor**, the rung its tightest cell lives at. */
    private val radialFloor = 754.005141

    private val block = HoneycombBlock(4, 2)

    private val rowBasePairs = 56

    private val edgeX = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR

    private val edgeY = block.rasterRows *
            HoneycombCrossSectionGeometry.rowPitch(Gen1Tile.INTERHELICAL_HONEYCOMB)

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

    private fun grid(columns: Int) =
        attachmentGrid(columns, block.rasterRows, edgeX, edgeY)

    // --------------------------------------------- gate 1: the ratio, which is a pure function

    @Test
    fun `gate 1 -- the stiffness ratio of an equal distribution is EXACTLY one`() {
        assert(stiffnessRatio(List(7) { 4.761904761904762 }) == 1.0)
        assert(stiffnessRatio(listOf(1.0)) == 1.0)
    }

    @Test
    fun `gate 1 -- the stiffness ratio is the max over the min`() {
        assert(abs(stiffnessRatio(listOf(1.0, 5.0, 2.0)) - 5.0) < 1e-15)
        assert(abs(stiffnessRatio(listOf(0.5, 10.0)) - 20.0) < 1e-15)
    }

    @Test
    fun `gate 1 -- the stiffness ratio refuses an empty or non-positive distribution`() {
        assertFailsWith<IllegalArgumentException> { stiffnessRatio(emptyList()) }
        assertFailsWith<IllegalArgumentException> { stiffnessRatio(listOf(1.0, 0.0)) }
        assertFailsWith<IllegalArgumentException> { stiffnessRatio(listOf(1.0, -2.0)) }
    }

    // ------------------------------- F9: the two surrogates must be the same object

    /**
     * The smoothed search runs on a one-state [com.xemantic.nano.plentyofroom.coupling
     * .MultiStateSurrogate] and the grading on an
     * [com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate]. If they disagree about the
     * peak dishing of one distribution, the two halves of the search are searching two objects
     * and nothing the study emits is admissible.
     */
    @Test
    fun `F9 -- the one-state multi-state surrogate reproduces the influence surrogate`() {
        val host = lattice()
        val stations = grid(3)
        val single = honeycombTiedSurrogate(host, stations, pressure, 41)
        val multi = honeycombMultiStateSurrogate(
            host, stations, listOf(LoadState("the only state", pressure)), 41
        )
        listOf(
            List(stations.size) { 33.3333 / stations.size },
            List(stations.size) { index -> 1.0 + index.toDouble() }
        ).forEach { candidate ->
            val a = single.solve(candidate).peakDishing
            val b = multi.peakDishing(candidate)[0]
            assert(abs(a - b) < 1e-10 * abs(a))
        }
    }

    // ------------------------------- F8: superposition is exact, so the bank must reproduce it

    @Test
    fun `F8 -- the surrogate at full presence reproduces the ASSEMBLED solve`() {
        val host = lattice()
        val stations = grid(2)
        val stiffnesses = List(stations.size) { 33.3333 / stations.size }
        val surrogate = honeycombTiedSurrogate(host, stations, pressure, 41)
        val response = surrogate.solve(stiffnesses)
        // a support force is UPWARD and W is positive downward, so it enters as its negative
        val assembled = host.solve(
            pressure,
            stations.mapIndexed { index, (s, y) ->
                PointLoad(s, y, -response.supportForces[index])
            }
        )
        val peak = assembled.peakDishing(41)
        assert(abs(peak - response.peakDishing) < 1e-9 * abs(peak))
    }

    // ------------------------------- F6: CLAUDE.md's standing falsifier, at the resolved link

    @Test
    fun `F6 -- a uniform pressure on the free resolved lattice dishes EXACTLY zero`() {
        val host = lattice()
        val field = host.solve(pressure)
        assert(field.peakDishing(41) / field.meanDeflection < 1e-9)
    }

    // ------------------------------- F7: this task edits no shared source, and asserts it

    @Test
    fun `F7 -- the default lattice's load vector is BIT-IDENTICAL over every degree of freedom`() {
        val standing = HoneycombGrillage(block, rowBasePairs, Gen1Tile.FOUNDATION_SECANT)
        val defaulted = HoneycombGrillage(
            block, rowBasePairs, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = null
        )
        val a = standing.assembleLoad(uniformPressure(0.01))
        val b = defaulted.assembleLoad(uniformPressure(0.01))
        assert(standing.degreesOfFreedom == defaulted.degreesOfFreedom)
        for (i in 0 until standing.degreesOfFreedom) assert(a[i] == b[i])
    }

    @Test
    fun `F7 -- the crossover SITE SET is identical, which a load vector cannot show`() {
        val standing = HoneycombGrillage(block, rowBasePairs, Gen1Tile.FOUNDATION_SECANT)
        val resolved = lattice()
        assert(standing.bonds.map { it.site } == resolved.bonds.map { it.site })
        assert(standing.bonds.map { it.unitZ } == resolved.bonds.map { it.unitZ })
    }

    // ------------------------------- gate 2: the percentile objective's limiting cases

    /**
     * At an incorporation of exactly one every realisation is full presence, so the 90th
     * percentile of the ensemble **is** the nominal dishing. It is the limiting case that says
     * the percentile objective is reading the same field the nominal one does.
     */
    @Test
    fun `gate 2 -- a fully present ensemble makes the percentile the NOMINAL dishing`() {
        val host = lattice()
        val stations = grid(2)
        val surrogate = honeycombTiedSurrogate(host, stations, pressure, 41)
        val stiffnesses = List(stations.size) { 33.3333 / stations.size }
        val full = dropoutEnsemble(List(stations.size) { 1.0 }, 16, 5L)
        val objective = percentileObjective(surrogate, full, freeStroke = 1.0, fraction = 0.90)
        val nominal = surrogate.solve(stiffnesses).peakDishing
        assert(abs(objective(stiffnesses) - nominal) < 1e-6 * nominal)
    }

    @Test
    fun `gate 2 -- the percentile objective refuses a non-positive free stroke`() {
        val host = lattice()
        val stations = grid(1)
        val surrogate = honeycombTiedSurrogate(host, stations, pressure, 41)
        val full = dropoutEnsemble(List(stations.size) { 1.0 }, 4, 5L)
        assertFailsWith<IllegalArgumentException> {
            percentileObjective(surrogate, full, freeStroke = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            percentileObjective(surrogate, full, freeStroke = 1.0, fraction = 1.5)
        }
    }

    // ------------------------------- F5: the oracle floor is a POINTWISE lower bound

    @Test
    fun `F5 -- the oracle floor is below the realised dishing at EVERY realisation`() {
        val host = lattice()
        val stations = grid(2)
        val surrogate = honeycombTiedSurrogate(host, stations, pressure, 41)
        val ensemble = dropoutEnsemble(List(stations.size) { 0.8 }, 32, 11L)
        val stiffnesses = List(stations.size) { 33.3333 / stations.size }
        val floors = oracleFloorSample(surrogate, ensemble)
        val realised = dropoutDishingSample(surrogate, stiffnesses, ensemble)
        // `com.xemantic.kotlin.test.assert` refuses `<=` inside its argument (`CLAUDE.md`),
        // so the pointwise inequality is written as a signed residual against zero.
        for (i in floors.indices) {
            assert(realised[i] - floors[i] > -1e-9 * maxOf(1.0, realised[i]))
        }
    }

    // ------------------------------- F4: the composition cannot lose to its own starts

    @Test
    fun `F4 -- the searched distribution is never worse IN SAMPLE than its best start`() {
        val host = lattice()
        val stations = grid(2)
        val single = honeycombTiedSurrogate(host, stations, pressure, 41)
        val multi = honeycombMultiStateSurrogate(
            host, stations, listOf(LoadState("the only state", pressure)), 41
        )
        val training = dropoutEnsemble(List(stations.size) { 0.85 }, 24, 13L)
        val equal = List(stations.size) { 33.3333 / stations.size }
        val graded = List(stations.size) { index -> if (index % 2 == 0) 5.0 else 1.0 }
            .let { w -> val sum = w.sum(); w.map { 33.3333 * it / sum } }
        val objective = percentileObjective(single, training, freeStroke = 1.0)
        val searched = searchedStiffnessDistribution(
            smooth = multi,
            percentile = single,
            training = training,
            freeStroke = 1.0,
            totalStiffness = 33.3333,
            transferred = listOf(equal, graded),
            percentileSweeps = 1
        )
        val bestStart = minOf(objective(equal), objective(graded))
        assert(searched.trainingObjective - bestStart < 1e-12 * maxOf(1.0, bestStart))
    }

    @Test
    fun `gate 3 -- a searched distribution meets C-0017's mandate on the SUM exactly`() {
        val host = lattice()
        val stations = grid(2)
        val single = honeycombTiedSurrogate(host, stations, pressure, 41)
        val multi = honeycombMultiStateSurrogate(
            host, stations, listOf(LoadState("the only state", pressure)), 41
        )
        val training = dropoutEnsemble(List(stations.size) { 0.85 }, 24, 13L)
        val equal = List(stations.size) { 33.3333 / stations.size }
        val searched = searchedStiffnessDistribution(
            smooth = multi,
            percentile = single,
            training = training,
            freeStroke = 1.0,
            totalStiffness = 33.3333,
            transferred = listOf(equal),
            percentileSweeps = 1
        )
        assert(abs(searched.stiffnesses.sum() - 33.3333) < 1e-9 * 33.3333)
        assert(abs(searched.nominalStiffnesses.sum() - 33.3333) < 1e-9 * 33.3333)
        assert(searched.stiffnesses.all { it > 0.0 })
    }

    // ------------------------------- F12: the search path is a function of its inputs

    @Test
    fun `F12 -- two identical searches return IDENTICAL stiffnesses`() {
        val host = lattice()
        val stations = grid(2)
        val single = honeycombTiedSurrogate(host, stations, pressure, 41)
        val multi = honeycombMultiStateSurrogate(
            host, stations, listOf(LoadState("the only state", pressure)), 41
        )
        val training = dropoutEnsemble(List(stations.size) { 0.85 }, 24, 17L)
        val equal = List(stations.size) { 33.3333 / stations.size }
        fun run() = searchedStiffnessDistribution(
            smooth = multi,
            percentile = single,
            training = training,
            freeStroke = 1.0,
            totalStiffness = 33.3333,
            transferred = listOf(equal),
            percentileSweeps = 1
        ).stiffnesses
        assert(run() == run())
    }

    // ------------------------------- gate 3: a prestrain is a LOAD, never a stiffness

    /**
     * `C-0104`'s trap, at the smoothed search's own bank: an influence taken on a **prestrained**
     * lattice is that influence *plus* the prestrain's own response, and the Woodbury matrix then
     * stops being a compliance. The free field must move with the prestrain and the compliance
     * must not.
     *
     * At one station the compliance is recoverable from public quantities alone:
     * `f = w_free/(M + 1/k)`, so `M = w_free/f − 1/k`.
     */
    @Test
    fun `gate 3 -- the prestrain moves the free field and not the smoothed bank's compliance`() {
        val station = listOf(2.0 to 3.0)
        val k = 4.0
        // ONE structure family at both angles. The first draft built the zero-prestrain arm
        // through `honeycombTiedLatticeAtResolvedLink` and the loaded one through
        // `honeycombTiedLattice`, which carries a different link entirely -- so the two
        // compliances were of two different lattices and differed by 0.18 %. The test failed on
        // its first real run and the defect was the test's.
        fun complianceAt(angle: Double): Pair<Double, Double> {
            val armed = honeycombTiedLattice(
                block, rowBasePairs, 1.0, tied = true, prestrainRadians = angle
            )
            val multi = honeycombMultiStateSurrogate(
                armed, station, listOf(LoadState("the only state", pressure)), 41
            )
            val free = armed.solve(pressure).deflection(2.0, 3.0)
            val force = multi.supportForces(listOf(k), 0)[0]
            return free to (free / force - 1.0 / k)
        }
        val (freeZero, mZero) = complianceAt(0.0)
        val (freeLoaded, mLoaded) = complianceAt(0.05)
        assert(abs(freeLoaded - freeZero) > 1e-9)
        assert(abs(mLoaded - mZero) < 1e-7 * abs(mZero))
    }

    // ------------------------------- gate 1: the percentile objective is the quantity it says

    @Test
    fun `gate 1 -- the percentile objective is the 90th order statistic over the free stroke`() {
        val host = lattice()
        val stations = grid(2)
        val surrogate = honeycombTiedSurrogate(host, stations, pressure, 41)
        val ensemble = dropoutEnsemble(List(stations.size) { 0.7 }, 64, 23L)
        val stroke = 3.75
        val stiffnesses = List(stations.size) { 33.3333 / stations.size }
        val objective = percentileObjective(surrogate, ensemble, stroke, 0.90)
        val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
        val expected = orderStatistic(sample, 0.90) / stroke
        // The objective is rounded at SIX significant digits by construction, so the tolerance
        // is one part in 1e5 and not one in 1e6: a tighter one measures the rounding rather than
        // the quantity, and the first draft's `1e-6` duly failed at a departure of 2.8e-6.
        assert(abs(objective(stiffnesses) - expected) < 1e-5 * expected)
        // and it is NOT the median: at a 0.7 incorporation the two must differ
        val median = orderStatistic(sample, 0.50) / stroke
        assert(abs(objective(stiffnesses) - median) > 1e-4 * expected)
    }

    /**
     * The objective is quantised at six significant digits, or a descent's acceptance test flips
     * on an ulp of jitter in a hot reduction and the answer moves basin (`C-0135`, `C-0177`).
     */
    @Test
    fun `gate 1 -- the percentile objective is ROUNDED at the decision precision`() {
        val host = lattice()
        val stations = grid(2)
        val surrogate = honeycombTiedSurrogate(host, stations, pressure, 41)
        val ensemble = dropoutEnsemble(List(stations.size) { 0.7 }, 64, 29L)
        val objective = percentileObjective(surrogate, ensemble, 1.0, 0.90)
        val value = objective(List(stations.size) { 33.3333 / stations.size })
        val scale = Math.pow(10.0, 5.0 - Math.floor(Math.log10(abs(value))))
        assert(abs(value - Math.round(value * scale) / scale) < 1e-18)
    }

    // ------------------------------- gate 1: the record reports what it says it reports

    @Test
    fun `gate 1 -- the searched record's own fields are the quantities they are named for`() {
        val host = lattice()
        val stations = grid(2)
        val single = honeycombTiedSurrogate(host, stations, pressure, 41)
        val multi = honeycombMultiStateSurrogate(
            host, stations, listOf(LoadState("the only state", pressure)), 41
        )
        val training = dropoutEnsemble(List(stations.size) { 0.85 }, 24, 31L)
        val stroke = 2.5
        val equal = List(stations.size) { 33.3333 / stations.size }
        val graded = List(stations.size) { index -> if (index % 2 == 0) 5.0 else 1.0 }
            .let { w -> val sum = w.sum(); w.map { 33.3333 * it / sum } }
        val objective = percentileObjective(single, training, stroke)
        val searched = searchedStiffnessDistribution(
            smooth = multi,
            percentile = single,
            training = training,
            freeStroke = stroke,
            totalStiffness = 33.3333,
            transferred = listOf(equal, graded),
            percentileSweeps = 1
        )
        assert(searched.ratio == stiffnessRatio(searched.stiffnesses))
        assert(searched.nominalRatio == stiffnessRatio(searched.nominalStiffnesses))
        assert(searched.trainingObjective == objective(searched.stiffnesses))
        assert(
            searched.bestTransferredTrainingObjective ==
                    minOf(objective(equal), objective(graded))
        )
        val nominalPeak = multi.peakDishing(searched.nominalStiffnesses)[0] / stroke
        assert(abs(searched.nominalObjective - nominalPeak) < 1e-9 * nominalPeak)
    }

    @Test
    fun `gate 2 -- the search refuses an empty transferred set and a mismatched start`() {
        val host = lattice()
        val stations = grid(1)
        val single = honeycombTiedSurrogate(host, stations, pressure, 41)
        val multi = honeycombMultiStateSurrogate(
            host, stations, listOf(LoadState("the only state", pressure)), 41
        )
        val training = dropoutEnsemble(List(stations.size) { 0.9 }, 8, 19L)
        // The MESSAGE, not only the type. Every one of these four guards is SHADOWED by a
        // downstream `require` that throws the same `IllegalArgumentException` a few frames
        // later — `optimiseStiffnessDistribution`'s own start check, `minimaxStiffness-
        // Distribution`'s, `dropoutDishingSample`'s path-count check — so a test that asserts
        // only the type passes with the guard deleted. Three mutations survived on exactly that
        // and none of them is a missing test; the test was not discriminating.
        fun refusal(block: () -> Unit): String =
            assertFailsWith<IllegalArgumentException> { block() }.message ?: ""
        assert(
            refusal {
                searchedStiffnessDistribution(
                    multi, single, training, 1.0, 33.3333, emptyList()
                )
            }.contains("at least one transferred distribution")
        )
        assert(
            refusal {
                searchedStiffnessDistribution(
                    multi, single, training, 1.0, 33.3333, listOf(List(stations.size + 1) { 1.0 })
                )
            }.contains("one stiffness per path")
        )
        assert(
            refusal {
                searchedStiffnessDistribution(
                    multi, single, training, 1.0, 33.3333,
                    listOf(List(stations.size) { 1.0 }), percentileSweeps = 0
                )
            }.contains("percentileSweeps must be at least 1")
        )
        assert(
            refusal {
                searchedStiffnessDistribution(
                    multi, single, training, 1.0, -1.0, listOf(List(stations.size) { 1.0 })
                )
            }.contains("the mandate is an EQUALITY on the sum")
        )
    }
}
