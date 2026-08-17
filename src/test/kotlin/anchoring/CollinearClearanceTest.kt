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
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.electrostatics.BluntEndStacking
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-152`, leaf `A8.2` — what stacking-prevention clearance should the collinear slot carry?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * **The free limiting case this task declared is here as a test**: fed the standing 2.69 nm
 * allowance, the identity must return `C-0069`'s own arithmetic to the last digit — an 8.19 nm
 * budget, a 0.02560917 nm margin and a 2.34165925 end-condition ceiling. Nothing this task says
 * about a wider clearance is worth reading if that fails.
 */
class CollinearClearanceTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    private val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * rise

    private val arm = elasticaArmForStiffness(
        hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
        hingeCount = 1,
        farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
        count = 34,
        targetStiffness = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
        workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
    )

    private val perPath = perPathStiffness(
        Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE, 34
    )

    private val state = CollinearClearanceState()

    /**
     * `C-0063`'s winning placement — phase 24, 34 roots, centro-symmetric — transcribed from
     * `gpd/results/T-125-upward-root-placement.json`, exactly as `C-0068`'s own test does.
     * **CITED**, and checked against the phase-24 upward lattice by the gate-5 test below rather
     * than trusted.
     */
    private val c0063Roots: List<Pair<Int, List<Double>>> = listOf(
        0 to listOf(-16.32, -5.44, 16.32),
        1 to listOf(0.0, 10.88),
        2 to listOf(-16.32, 5.44, 16.32),
        3 to listOf(0.0, 10.88),
        4 to listOf(-16.32, 16.32),
        5 to listOf(-10.88, 0.0),
        6 to listOf(-16.32, 16.32),
        7 to listOf(-10.88, 10.88),
        8 to listOf(-16.32, 16.32),
        9 to listOf(0.0, 10.88),
        10 to listOf(-16.32, 16.32),
        11 to listOf(-10.88, 0.0),
        12 to listOf(-16.32, -5.44, 16.32),
        13 to listOf(-10.88, 0.0),
        14 to listOf(-16.32, 5.44, 16.32)
    )

    private fun gen1StationRows(): List<StationRow> = c0063Roots.map { (row, roots) ->
        StationRow(row, (row - 7.0) * OrigamiDuplex.INTERHELICAL, roots)
    }

    // ---------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 - a clearance and a margin are lengths and scale with every length`() {
        val one = collinearMargin(pitch, 2.69, arm)
        val two = collinearMargin(2.0 * pitch, 2.0 * 2.69, 2.0 * arm)
        assert(abs(two - 2.0 * one) < 1e-12)
    }

    @Test
    fun `gate 1 - a base-pair count is invariant under a common rescaling of length and rise`() {
        for (length in listOf(0.51108, 1.3, 2.69, 8.16439083)) {
            assert(
                basePairsForLength(length, rise) ==
                        basePairsForLength(10.0 * length, 10.0 * rise)
            )
        }
    }

    @Test
    fun `gate 1 - doubling the closure stiffness divides the closing distance by root two`() {
        val soft = stackSuppressionGap(
            stackFreeEnergy = state.stackFreeEnergy,
            closureStiffness = 30.0,
            contactSeparation = state.contactSeparation
        )
        val stiff = stackSuppressionGap(
            stackFreeEnergy = state.stackFreeEnergy,
            closureStiffness = 60.0,
            contactSeparation = state.contactSeparation
        )
        val softClosing = soft - state.contactSeparation
        val stiffClosing = stiff - state.contactSeparation
        assert(abs(softClosing / stiffClosing - Math.sqrt(2.0)) < 1e-12)
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { basePairsForLength(1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { basePairsForLength(-1.0, rise) }
        assertFailsWith<IllegalArgumentException> { seriesStiffness(listOf()) }
        assertFailsWith<IllegalArgumentException> { seriesStiffness(listOf(1.0, 0.0)) }
        assertFailsWith<IllegalArgumentException> { axialSpringConstant(0.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { axialSpringConstant(1100.0, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            stackSuppressionGap(-1.0, 30.0, 0.34072)
        }
        assertFailsWith<IllegalArgumentException> {
            stackSuppressionGap(18.0, 30.0, 0.34072, occupancy = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            stackOccupancyAtGap(18.0, 30.0, 0.34072, gap = 0.1)
        }
        assertFailsWith<IllegalArgumentException> { collinearMarginBasePairs(32, -1, 24) }
        assertFailsWith<IllegalArgumentException> { collinearMarginBasePairs(32, 4, 0) }
    }

    // ---------------------------------------------------------------- gate 2: limiting cases

    @Test
    fun `gate 2 - THE FREE LIMITING CASE - the standing allowance returns C-0069's arithmetic`() {
        val budget = rowOfThreeLengthCeiling(pitch, OrigamiDuplex.INTERHELICAL)
        assert(abs(budget - 8.19) < 1e-9)
        assert(abs(collinearMargin(pitch, OrigamiDuplex.INTERHELICAL, arm) - 0.02560917) < 1e-7)
        assert(abs(bendingFactorForLength(budget, 230.0, perPath) - 2.34165925) < 1e-7)
    }

    @Test
    fun `gate 2 - at zero clearance the budget is the bare root pitch`() {
        assert(abs(collinearBudget(pitch, 0.0) - pitch) < 1e-12)
    }

    @Test
    fun `gate 2 - an infinitely stiff closure path leaves only the contact separation`() {
        val gap = stackSuppressionGap(state.stackFreeEnergy, 1.0e12, state.contactSeparation)
        assert(abs(gap - state.contactSeparation) < 1e-5)
    }

    @Test
    fun `gate 2 - unit occupancy is the bare balance against the bond energy`() {
        val gap = stackSuppressionGap(state.stackFreeEnergy, 30.5, state.contactSeparation)
        val work = 0.5 * 30.5 * (gap - state.contactSeparation) * (gap - state.contactSeparation)
        assert(abs(work - state.stackFreeEnergy) < 1e-9)
    }

    @Test
    fun `gate 2 - a series of one is itself and of two equal springs is half`() {
        assert(abs(seriesStiffness(listOf(64.0)) - 64.0) < 1e-12)
        assert(abs(seriesStiffness(listOf(64.0, 64.0)) - 32.0) < 1e-12)
    }

    @Test
    fun `gate 2 - a stiffer closure path never demands a wider gap`() {
        var previous = Double.MAX_VALUE
        for (multiple in listOf(0.25, 0.5, 1.0, 2.0, 4.0, 16.0)) {
            val gap = stackSuppressionGap(
                state.stackFreeEnergy, 30.0 * multiple, state.contactSeparation
            )
            assert(gap < previous)
            previous = gap
        }
    }

    // ---------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 - the margin is an INTEGER count of rises once every term is on the lattice`() {
        for (clearanceBp in 1..7) {
            val armBp = basePairsWithin(arm, rise)
            val counted = collinearMarginBasePairs(
                UPWARD_ROOT_PITCH_BASE_PAIRS, clearanceBp, armBp
            )
            val measured = collinearMargin(pitch, clearanceBp * rise, armBp * rise)
            assert(abs(measured - counted * rise) < 1e-12)
        }
    }

    @Test
    fun `gate 3 - the bending length and the bending factor stay exact inverses at every budget`() {
        for (clearanceBp in 1..8) {
            val budget = collinearBudget(pitch, clearanceBp * rise)
            val factor = bendingFactorForLength(budget, 230.0, perPath)
            val back = bendingLengthForStiffness(factor, 230.0, perPath)
            assert(abs(back - budget) < 1e-12)
        }
    }

    @Test
    fun `gate 3 - the occupancy inverts the suppression gap`() {
        for (occupancy in listOf(1.0, 0.1, 0.01, 0.001)) {
            val gap = stackSuppressionGap(
                state.stackFreeEnergy, 30.5178, state.contactSeparation, occupancy
            )
            val back = stackOccupancyAtGap(
                state.stackFreeEnergy, 30.5178, state.contactSeparation, gap
            )
            assert(abs(ln(back) - ln(occupancy)) < 1e-9)
        }
    }

    @Test
    fun `gate 3 - a series closure path is never stiffer than its softest member`() {
        val members = listOf(134.7, 101.1, 64.7)
        assert(seriesStiffness(members) < members.min())
    }

    @Test
    fun `gate 3 - the placed count is invariant under a rigid translation of the array`() {
        val rows = gen1StationRows()
        val here = placeCollinearRootedArray(
            "reference", rows, arm, 2.04, OrigamiDuplex.INTERHELICAL, 40.0, 40.35
        )
        val shifted = placeCollinearRootedArray(
            "shifted",
            rows.map { StationRow(it.row, it.y + 7.0, it.roots) },
            arm, 2.04, OrigamiDuplex.INTERHELICAL, 40.0, 40.35
        )
        assert(here.placed == shifted.placed)
        assert(here.levelsRequired == shifted.levelsRequired)
    }

    @Test
    fun `gate 3 - splitting the two widths reproduces the conflated packer when they are equal`() {
        val rows = gen1StationRows()
        val split = placeCollinearRootedArray(
            "split", rows, arm, OrigamiDuplex.INTERHELICAL, OrigamiDuplex.INTERHELICAL, 40.0, 40.35
        )
        val conflated = placeRootedOutputElement(
            "conflated", rows, arm, 40.0, 40.35, OrigamiDuplex.INTERHELICAL
        )
        assert(split.placed == conflated.placed)
        assert(split.levelsRequired == conflated.levelsRequired)
        assert(split.overlappingPairs == conflated.overlappingPairs)
        assert(abs(split.planAreaFraction - conflated.planAreaFraction) < 1e-12)
    }

    // ---------------------------------------------------------------- gate 4: convergence

    @Test
    fun `gate 4 - the restraint ceilings are resolution independent`() {
        val budget = collinearBudget(pitch, 6 * rise)
        val coarse = farRestraintCeiling(
            Gen1Tile.crossoverHingeStiffness(), budget, resolution = 1.0e-6
        )!!
        val fine = farRestraintCeiling(
            Gen1Tile.crossoverHingeStiffness(), budget, resolution = 1.0e-9
        )!!
        assert(abs(fine / coarse - 1.0) < 1e-5)
    }

    @Test
    fun `gate 4 - the placed arm is RK4 step independent`() {
        val four = elasticaArmForStiffness(
            Gen1Tile.crossoverHingeStiffness(), 1, ArmAnchorage.twoTerminus().rotationalStiffness,
            count = 34, targetStiffness = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE, steps = 400
        )
        val eight = elasticaArmForStiffness(
            Gen1Tile.crossoverHingeStiffness(), 1, ArmAnchorage.twoTerminus().rotationalStiffness,
            count = 34, targetStiffness = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE, steps = 800
        )
        assert(abs(eight - four) < 1e-6)
    }

    @Test
    fun `gate 4 - the thirty-root capacity ceiling is resolution independent`() {
        val lattice = upwardRootLattice(24, 40.0, 15)
        val coarse = maximumPlanCeilingForCount(
            lattice, 30, 40.0, 6 * rise, maximumPerRow = 2, resolution = 1.0e-6
        )!!
        val fine = maximumPlanCeilingForCount(
            lattice, 30, 40.0, 6 * rise, maximumPerRow = 2, resolution = 1.0e-9
        )!!
        assert(abs(fine - coarse) < 1e-5)
    }

    // ---------------------------------------------------------------- gate 5: upstream

    @Test
    fun `gate 5 - C-0069's two joint ceilings reproduce at the standing budget`() {
        val far = farRestraintCeiling(Gen1Tile.crossoverHingeStiffness(), 8.19)!!
        val near = nearRestraintCeiling(ArmAnchorage.twoTerminus().rotationalStiffness, 8.19)!!
        assert(abs(far - 79.6781387) < 1e-4)
        assert(abs(near - 13.9303697) < 1e-4)
    }

    @Test
    fun `gate 5 - C-0079's blunt-end stack is 4_41146 k_BT from Woo and Rothemund's kcal`() {
        val kbt = thermalEnergy(ROOM_TEMPERATURE)
        assert(abs(-BluntEndStacking.perStackEnergy / kbt - 4.41146) < 1e-4)
        assert(abs(BluntEndStacking.OXDNA2_CUTOFF - 0.51108) < 1e-12)
        assert(abs(BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET - 1.3) < 1e-12)
        assert(abs(OXDNA2_STACK_SITE_MINIMUM - 0.34072) < 1e-12)
    }

    @Test
    fun `gate 5 - C-0074's 30-root plan ceiling reproduces at the standing allowance`() {
        val lattice = upwardRootLattice(8, 40.0, 15)
        val ceiling = maximumPlanCeilingForCount(
            lattice, 30, 40.0, OrigamiDuplex.INTERHELICAL, maximumPerRow = 2
        )!!
        assert(abs(ceiling - 9.5350) < 1e-3)
    }

    @Test
    fun `gate 5 - C-0053's 43 of 45 reproduces at the standing allowance`() {
        val armFor45 = elasticaArmForStiffness(
            Gen1Tile.crossoverHingeStiffness(), 1, ArmAnchorage.twoTerminus().rotationalStiffness,
            count = 45, targetStiffness = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
        )
        assert(abs(armFor45 - 9.131) < 1e-3)
        val best = (0 until 32).maxOf {
            placeHingeArms(it, 40.0, 15, armFor45, OrigamiDuplex.INTERHELICAL).arms
        }
        assert(best == 43)
    }

    @Test
    fun `gate 5 - the transcribed C-0063 stations all lie on the phase-24 upward lattice`() {
        val lattice = upwardRootLattice(24, 40.0, 15)
        assert(c0063Roots.sumOf { it.second.size } == 34)
        c0063Roots.forEach { (row, roots) ->
            roots.forEach { root ->
                assert(lattice[row].any { abs(it - root) < 1e-9 })
            }
        }
    }

    @Test
    fun `gate 5 - C-0072's design quantum is the rise and the margin is 0_075 of it`() {
        assert(abs(rise - 0.34) < 1e-12)
        assert(abs(collinearMargin(pitch, OrigamiDuplex.INTERHELICAL, arm) / rise - 0.0753) < 1e-4)
    }

    // ------------------------------------------------- the findings, asserted as tests

    @Test
    fun `finding - the standing 2_69 nm allowance is NOT on the base-pair lattice`() {
        val counts = OrigamiDuplex.INTERHELICAL / rise
        assert(abs(counts - Math.round(counts).toDouble()) > 1e-3)
        // quantised UP to the nearest whole rise the margin goes NEGATIVE
        val quantised = basePairsForLength(OrigamiDuplex.INTERHELICAL, rise)
        assert(quantised == 8)
        assert(collinearMargin(pitch, quantised * rise, arm) < 0.0)
    }

    @Test
    fun `finding - the whole design space is four integers and every one clears the knife edge`() {
        val ladder = state.integerSweep()
        assert(ladder.size == 8)
        val plausible = ladder.filter { it.clearance >= BluntEndStacking.OXDNA2_CUTOFF }
        assert(plausible.first().clearanceBasePairs == 2)
        ladder.filter { it.clearanceBasePairs in 2..6 }.forEach {
            assert(it.margin > 20.0 * 0.02560917)
        }
    }

    @Test
    fun `finding - the recommended clearance is six base pairs on the softest closure path`() {
        val recommended = state.recommendation()
        assert(recommended.clearanceBasePairs == 6)
        assert(abs(recommended.clearance - 2.04) < 1e-12)
        assert(abs(recommended.budget - 8.84) < 1e-12)
        assert(abs(recommended.margin - 0.67560917) < 1e-7)
        // the buildable margin is an integer count of rises, not a residue
        assert(recommended.marginBasePairs == 2)
    }

    @Test
    fun `finding - the recommended clearance places all 34 at one level`() {
        val outcome = placeCollinearRootedArray(
            "recommended", gen1StationRows(), arm, 2.04, OrigamiDuplex.INTERHELICAL, 40.0, 40.35
        )
        assert(outcome.placed == 34)
        assert(outcome.singleLevel)
        assert(outcome.memberClashPairs == 0)
    }

    @Test
    fun `finding - a rigid root places at four base pairs of clearance and not at five or six`() {
        val rigid = elasticaArmCeiling(
            farStiffness = 0.0, count = 34,
            targetStiffness = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
        )
        assert(abs(rigid - 9.247) < 1e-2)
        assert(rigid <= collinearBudget(pitch, 4 * rise))
        assert(rigid > collinearBudget(pitch, 5 * rise))
        assert(rigid > collinearBudget(pitch, 6 * rise))
    }

    @Test
    fun `finding - the joint window at the recommended budget clears both joints by over 10 pct`() {
        val budget = collinearBudget(pitch, 6 * rise)
        val far = farRestraintCeiling(Gen1Tile.crossoverHingeStiffness(), budget)!!
        val near = nearRestraintCeiling(ArmAnchorage.twoTerminus().rotationalStiffness, budget)!!
        assert(far / ArmAnchorage.twoTerminus().rotationalStiffness > 1.10)
        assert(near / Gen1Tile.crossoverHingeStiffness() > 1.10)
    }

    @Test
    fun `finding - the midspan flexure family is refused at EVERY clearance including zero`() {
        val floor = bendingLengthForStiffness(48.0, 230.0, perPath)
        assert(floor > collinearBudget(pitch, 0.0))
    }
}
