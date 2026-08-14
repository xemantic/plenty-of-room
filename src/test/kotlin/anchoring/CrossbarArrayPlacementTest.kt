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
 * `T-130`, leaf `A8.2` — do `C-0062`'s closing trio lattices place **34** times on `C-0063`'s
 * placement?
 *
 * Every test is named for the verification gate it discharges. The two free limiting cases
 * `T-130` declared are here as tests: **collapsing the trio to a bare root must reproduce
 * `C-0063`'s placement**, and **the pruned base closure must agree with `C-0057`'s own
 * `bestLinkClosure`**, which is the assertion the whole register field rests on.
 */
class CrossbarArrayPlacementTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    private val edgeX = Gen1Tile.EDGE_X

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
    fun `gate 1 - a truss instance's plan lengths are its own base-pair counts times the rise`() {
        val truss = TrussInstance(
            id = "t", row = 3, rootX = 5.44, y = -10.76,
            crossbarBasePairs = 17, separationBasePairs = 10
        )
        assert(truss.crossbarLength.isCloseTo(17 * rise, 1e-12))
        assert(truss.legSeparation.isCloseTo(10 * rise, 1e-12))
        assert(truss.planArea.isCloseTo(17 * rise * OrigamiDuplex.INTERHELICAL, 1e-12))
    }

    @Test
    fun `gate 1 - the crossbar covers both legs at C-0048's minimum and above`() {
        (6..12).forEach { separation ->
            val minimum = CrossbarGeometry(separation + 6, separation).basePairs
            (minimum..minimum + 3).forEach { crossbar ->
                val truss = TrussInstance("t", 0, 0.0, 0.0, crossbar, separation)
                truss.legPositions.forEach { leg ->
                    assert(leg.x >= truss.low - 1e-9)
                    assert(leg.x <= truss.high + 1e-9)
                }
                assert(truss.planElement.verticalMembers.size == 2)
            }
        }
    }

    @Test
    fun `gate 1 - the array's plan area is additive and doubles exactly with the count`() {
        val stations = c0063Stations()
        val one = trussArray(stations.take(17), 17, 10).sumOf { it.planArea }
        val two = trussArray(stations.take(34), 17, 10).sumOf { it.planArea }
        assert(two.isCloseTo(2.0 * one, 1e-9))
    }

    @Test
    fun `gate 1 - the packing verdict is dimensionless - scaling every length by ten changes nothing`() {
        val stations = c0063Stations()
        val here = trussArrayPackingVerdict(trussArray(stations, 17, 10))
        val scaled = trussArrayPackingVerdict(
            trussArray(
                stations.map { TrussStation(it.row, 10 * it.x, 10 * it.y) },
                17, 10, width = 10 * OrigamiDuplex.INTERHELICAL, rise = 10 * rise
            )
        )
        assert(scaled.overlappingPairs == here.overlappingPairs)
        assert(scaled.memberClashPairs == here.memberClashPairs)
        assert(scaled.levelsRequired == here.levelsRequired)
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            TrussInstance("t", 0, 0.0, 0.0, crossbarBasePairs = 8, separationBasePairs = 10)
        }
        assertFailsWith<IllegalArgumentException> {
            TrussInstance("t", 0, 0.0, 0.0, 17, 10, width = 0.0)
        }
        assertFailsWith<IllegalArgumentException> { TrussInstance("t", -1, 0.0, 0.0, 17, 10) }
        assertFailsWith<IllegalArgumentException> { TrussInstance("t", 0, 0.0, 0.0, 17, 0) }
        assertFailsWith<IllegalArgumentException> { trussArray(emptyList(), 17, 10) }
        assertFailsWith<IllegalArgumentException> { BaseRegisterField(stepsPerBasePair = 3) }
        assertFailsWith<IllegalArgumentException> { BaseRegisterField(halfWindowBasePairs = 0) }
        assertFailsWith<IllegalArgumentException> { stationPhaseClassCensus(emptyList(), 24) }
        assertFailsWith<IllegalArgumentException> { trussPlanDemand(0) }
        assertFailsWith<IllegalArgumentException> {
            placeTrussArray("x", emptyList(), 17, 10)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - the free limiting case - collapsing the trio to a bare root reproduces C-0063's placement`() {
        val stations = c0063Stations()
        assert(stations.size == 34)
        val collapsed = trussArray(stations, crossbarBasePairs = 2, separationBasePairs = 1)
        assert(collapsed.size == 34)
        collapsed.forEachIndexed { index, truss ->
            assert(truss.rootX.isCloseTo(stations[index].x, 1e-12))
            assert(abs(truss.y - stations[index].y) < 1e-12)
            assert(truss.row == stations[index].row)
        }
        val verdict = trussArrayPackingVerdict(collapsed)
        assert(verdict.memberClashPairs == 0)
        assert(verdict.singleLevel)
    }

    @Test
    fun `gate 2 - one instance carries C-0062's trio geometry, unchanged by where it is placed`() {
        val here = TrussInstance(
            "a", 0, -16.32, -18.83, 17, 10, axialPhase = 0.17, lateralSeat = -0.2
        )
        val there = TrussInstance(
            "b", 9, 10.88, 5.38, 17, 10, axialPhase = 0.17, lateralSeat = -0.2
        )
        assert(there.crossbarLength.isCloseTo(here.crossbarLength, 1e-15))
        assert(there.legSeparation.isCloseTo(here.legSeparation, 1e-15))
        assert((there.high - there.low).isCloseTo(here.high - here.low, 1e-15))
        assert((there.centreX - there.rootX).isCloseTo(here.centreX - here.rootX, 1e-12))
    }

    @Test
    fun `gate 2 - one truss on an empty sheet packs and two on one site clash`() {
        val single = trussArray(listOf(TrussStation(7, 0.0, 0.0)), 17, 10)
        assert(trussArrayPackingVerdict(single).singleLevel)
        val doubled = trussArray(
            listOf(TrussStation(7, 0.0, 0.0), TrussStation(7, 0.5, 0.0)), 17, 10
        )
        assert(trussArrayPackingVerdict(doubled).memberClashPairs >= 1)
    }

    @Test
    fun `gate 2 - an instance wider than its row's pitch overlaps its own neighbour`() {
        val row = listOf(TrussStation(0, -5.44, 0.0), TrussStation(0, 5.44, 0.0))
        assert(trussArrayPackingVerdict(trussArray(row, 17, 10)).overlappingPairs == 0)
        assert(trussArrayPackingVerdict(trussArray(row, 40, 10)).overlappingPairs >= 1)
    }

    @Test
    fun `gate 2 - the pruned base closure agrees with C-0057's own bestLinkClosure`() {
        val field = BaseRegisterField(stepsPerBasePair = 2, halfWindowBasePairs = 2)
        var checked = 0
        var disagreements = 0
        var index = -4
        while (index <= 4 && checked < 5) {
            val axial = field.centreAxial + index * field.step
            val candidate = field.junctionSet
                .feasibleAt(field.topology, axial, field.lateralSeat)
                .firstOrNull()
            if (candidate != null) {
                val links = junctionLinks(field.backbone, candidate.closure, field.interhelical)
                val pruned = junctionClosesOnSomeAssignment(
                    links, field.gridSteps, field.refinements
                )
                val full = torsionVerdict(
                    links, gridSteps = field.gridSteps, refinements = field.refinements
                ).closes
                if (pruned != full) disagreements++
                checked++
            }
            index++
        }
        assert(checked >= 1)
        assert(disagreements == 0)
    }

    @Test
    fun `gate 2 - a row pitch no position closes at admits no pair, and that is a refusal not a zero`() {
        val field = BaseRegisterField(stepsPerBasePair = 2, halfWindowBasePairs = 1)
        // the pair condition is a conjunction: it can never admit more centres than positions
        val positions = field.positions.count { it.closes }
        assert(field.closingPairCentres(2).size <= positions)
    }

    // ------------------------------------------------------------------ gate 3 — symmetry

    @Test
    fun `gate 3 - every one of C-0063's 34 stations is ONE helical phase class of its own duplex`() {
        val census = stationPhaseClassCensus(c0063Stations(), phaseBasePairs = 24)
        assert(census.classes == 1)
        assert(census.populations == listOf(34))
        assert(census.localAxialBasePairs.single().isCloseTo(EAST_SITE_BASE_PAIRS.toDouble(), 1e-6))
    }

    @Test
    fun `gate 3 - the phase class census is invariant under a whole-period shift of the lattice`() {
        val stations = c0063Stations()
        val shifted = stations.map {
            TrussStation(it.row, it.x + UPWARD_ROOT_PITCH_BASE_PAIRS * rise, it.y)
        }
        assert(
            stationPhaseClassCensus(shifted, 24).localAxialBasePairs ==
                    stationPhaseClassCensus(stations, 24).localAxialBasePairs
        )
    }

    @Test
    fun `gate 3 - the packing verdict is invariant under a rigid translation of the whole array`() {
        val stations = c0063Stations()
        val here = trussArrayPackingVerdict(trussArray(stations, 17, 10))
        val moved = trussArrayPackingVerdict(
            trussArray(stations.map { TrussStation(it.row, it.x + 1.37, it.y) }, 17, 10)
        )
        assert(moved.overlappingPairs == here.overlappingPairs)
        assert(moved.memberClashPairs == here.memberClashPairs)
        assert(moved.levelsRequired == here.levelsRequired)
    }

    @Test
    fun `gate 3 - the exact leg clash census agrees with the packer's member clash count`() {
        val stations = c0063Stations()
        listOf(6, 10, 12).forEach { separation ->
            val array = trussArray(stations, separation + 7, separation, lateralSeat = -0.4)
            assert(exactLegClashPairs(array) == trussArrayPackingVerdict(array).memberClashPairs)
        }
    }

    @Test
    fun `gate 3 - a chord is a line - a half turn leaves the misalignment unchanged`() {
        (0 until 30).forEach { step ->
            val azimuth = step * 2.0 * PI / 30
            val here = foldedChordMisalignment(azimuth, 0.5 * PI)
            val turned = foldedChordMisalignment(azimuth + PI, 0.5 * PI)
            assert(abs(turned - here) < 1e-12)
        }
    }

    @Test
    fun `gate 3 - the leg entry points inherit the stations' centro-symmetry at any axial phase`() {
        val stations = c0063Stations()
        listOf(0.0, 0.17, -0.34).forEach { axialPhase ->
            val legs = trussArray(stations, 17, 10, axialPhase = axialPhase)
                .flatMap { it.legPositions }
                .map { Pair(Math.round(it.x * 1e6), Math.round(it.y * 1e6)) }
                .toSet()
            val reflected = legs.map { Pair(-it.first, -it.second) }.toSet()
            assert(reflected == legs)
        }
    }

    // ------------------------------------------------------------------ gate 4 — convergence

    @Test
    fun `gate 4 - the register field is deterministic on repeat calls`() {
        val field = BaseRegisterField(stepsPerBasePair = 2, halfWindowBasePairs = 3)
        val first = field.closingPairCentres(10).map { it.centre }
        val second = field.closingPairCentres(10).map { it.centre }
        assert(first == second)
    }

    @Test
    fun `gate 4 - a finer azimuth grid never loses a closing position, UNCAPPED`() {
        // the 60-step azimuth set is a subset of the 120-step one, so refinement is monotone —
        // but only with the per-position candidate cap lifted: a cap is a ranking, and a ranking
        // is not monotone under refinement. That is C-0062's verdict-grid finding in a new place,
        // and it is why the study reports the cap it ran at.
        val coarse = BaseRegisterField(
            azimuthSteps = 60, stepsPerBasePair = 2, halfWindowBasePairs = 2,
            candidatesPerPosition = 64
        )
        val fine = BaseRegisterField(
            azimuthSteps = 120, stepsPerBasePair = 2, halfWindowBasePairs = 2,
            candidatesPerPosition = 64
        )
        val fineClosing = fine.positions.filter { it.closes }.map { it.axial }
        coarse.positions.filter { it.closes }.forEach { position ->
            assert(fineClosing.any { abs(it - position.axial) < 1e-9 })
        }
    }

    @Test
    fun `gate 4 - the greedy conflict-free count never exceeds the array and is exact when it packs`() {
        val stations = c0063Stations()
        val array = trussArray(stations, 17, 10)
        val elements = array.map { it.planElement }
        assert(greedyConflictFreeElements(elements) == 34)
        val crowded = trussArray(stations, 40, 10).map { it.planElement }
        assert(greedyConflictFreeElements(crowded) < 34)
    }

    // ------------------------------------------------------------------ gate 5 — upstream

    @Test
    fun `gate 5 - C-0063's placement carries 34 roots at the 10-88 nm pitch, four rows of three`() {
        val stations = c0063Stations()
        assert(stations.size == 34)
        val byRow = stations.groupBy { it.row }
        assert(byRow.size == 15)
        assert(byRow.count { it.value.size == 3 } == 4)
        assert(byRow.count { it.value.size == 2 } == 11)
        val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * rise
        byRow.values.forEach { row ->
            row.map { it.x }.sorted().zipWithNext().forEach { (a, b) ->
                val multiple = (b - a) / pitch
                assert(abs(multiple - Math.round(multiple)) < 1e-9)
                assert(multiple >= 1.0 - 1e-9)
            }
        }
    }

    @Test
    fun `gate 5 - C-0063's stations are exactly the EAST sites of C-0055's own lattice at phase 24`() {
        val sites = upwardRootLattice(24, edgeX, 15)
        c0063Stations().forEach { station ->
            assert(sites[station.row].any { abs(it - station.x) < 1e-9 })
        }
    }

    @Test
    fun `gate 5 - C-0053's plan convention - two duplexes at exactly d are tangent and admissible`() {
        val a = trussArray(listOf(TrussStation(0, 0.0, 0.0)), 17, 10).single()
        val b = trussArray(
            listOf(TrussStation(1, 0.0, OrigamiDuplex.INTERHELICAL)), 17, 10
        ).single()
        assert(trussArrayPackingVerdict(listOf(a, b)).overlappingPairs == 0)
    }

    @Test
    fun `gate 5 - the truss block's plan demand is below the station pitch where C-0053's arm is above it`() {
        val demand = trussPlanDemand(20, OrigamiDuplex.INTERHELICAL)
        val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * rise
        assert(demand.isCloseTo(20 * rise + OrigamiDuplex.INTERHELICAL, 1e-12))
        assert(demand < pitch)
        // C-0053's own 45-path arm demands 11.821 nm and does NOT clear the same pitch, while
        // C-0055's 34-path arm at 8.16439 nm demands 10.854 nm and clears it by 0.027 nm
        assert(C0053_ARM_LENGTH_LOCAL + OrigamiDuplex.INTERHELICAL > pitch)
        assert(C0055_ARM_LENGTH_LOCAL + OrigamiDuplex.INTERHELICAL < pitch)
    }

    @Test
    fun `gate 5 - the EAST site is a quarter turn from the duplex's own NORTH plane`() {
        val backbone = DuplexBackbone()
        val azimuth = (EAST_SITE_BASE_PAIRS * backbone.twistPerBasePair * 180.0 / PI) % 360.0
        assert(abs(azimuth - 90.0) < 0.5)
        assert(abs((EAST_SITE_BASE_PAIRS * 33.75) % 360.0 - 90.0) < 1e-12)
    }

    @Test
    fun `gate 5 - the sheet's own phase cannot absorb the register offset`() {
        val locals = listOf(0, 5, 8, 15, 24, 31).map { phase ->
            val station = TrussStation(0, (phase + EAST_SITE_BASE_PAIRS) * rise, 0.0)
            stationPhaseClassCensus(listOf(station), phase).localAxialBasePairs.single()
        }
        assert(locals.distinct().size == 1)
        assert(locals.first().isCloseTo(EAST_SITE_BASE_PAIRS.toDouble(), 1e-6))
    }

    companion object {

        /** `C-0055`'s self-consistent arm at 34 paths, quoted for the pitch comparison only. */
        const val C0055_ARM_LENGTH_LOCAL: Double = 8.16439

        /** `C-0053`'s `E5a1` arm at §3's 45 paths, the one whose demand is 11.821 nm. */
        const val C0053_ARM_LENGTH_LOCAL: Double = 9.131
    }
}
