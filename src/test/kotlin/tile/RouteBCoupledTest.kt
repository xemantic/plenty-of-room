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
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-322` — route B's own buildable widths, graded coupled, on stations derived at each row length.
 *
 * Written before `tile/RouteBCoupled.kt` exists, and watched fail.
 *
 * The gates each test names are `T-322`'s own `P1`–`P7` and `F1`–`F20`.
 */
class RouteBCoupledTest {

    /** `C-0205`'s transverse ceiling, the constant every resolution in this task is read at. */
    private val shearCeiling = 254.80809548301096

    /** `C-0208`'s radial bracket **floor**, this task's headline rung. */
    private val radialFloor = 754.005141

    private val floorRungName = "the resolved floor"

    // ------------------------------------------------ gate 1: the station ladder, pure arithmetic

    @Test
    fun `gate 1 -- the ladder delegates to the corpus's own station lattice`() {
        val ladder = RouteBStationLadder(rowBasePairs = 98, rootingHelices = 10)
        val expected = honeycombStationLattice(10, 98, 3, 7).map { it.size }
        assert(ladder.stationsAtPhase(3) == expected)
    }

    @Test
    fun `gate 1 -- route B's rows carry FIVE station columns where the block extent carries six`() {
        val stagger = 14
        assert(RouteBStationLadder(92, 10, stagger).minimumStationsPerRow == 5)
        assert(RouteBStationLadder(98, 10, stagger).minimumStationsPerRow == 5)
        assert(RouteBStationLadder(106, 10, stagger).minimumStationsPerRow == 5)
        assert(RouteBStationLadder(116, 10, stagger).minimumStationsPerRow == 6)
    }

    @Test
    fun `gate 1 -- the derived phase is 7 at 92 bp and 0 at 98 and 106, at the 14 bp stagger`() {
        assert(RouteBStationLadder(92, 10, 14).derivedPhase == 7)
        assert(RouteBStationLadder(98, 10, 14).derivedPhase == 0)
        assert(RouteBStationLadder(106, 10, 14).derivedPhase == 0)
    }

    @Test
    fun `gate 1 -- T-316's inherited phase 16 carries only FOUR stations at 92 and 98 bp`() {
        assert(RouteBStationLadder(92, 10, 14).minimumStationsAtPhase(16) == 4)
        assert(RouteBStationLadder(98, 10, 14).minimumStationsAtPhase(16) == 4)
        assert(RouteBStationLadder(106, 10, 14).minimumStationsAtPhase(16) == 5)
        assert(!RouteBStationLadder(92, 10, 14).carriesColumnsAtPhase(5, 16))
        assert(RouteBStationLadder(92, 10, 14).carriesColumns(5))
    }

    @Test
    fun `gate 1 -- the derived phase takes the EARLIER of two equal maximisers`() {
        // at the 7 bp stagger every one of 106 bp's 21 phases carries the same minimum, so the
        // rule's tie-break is the whole of the answer and it must be the earliest phase.
        val ladder = RouteBStationLadder(106, 10, 7)
        val best = (0 until 21).map { ladder.minimumStationsAtPhase(it) }.max()
        assert((0 until 21).count { ladder.minimumStationsAtPhase(it) == best } == 21)
        assert(ladder.derivedPhase == 0)
    }

    @Test
    fun `gate 1 -- a phase outside the period and a non-positive row are refused`() {
        // The message is asserted, not merely the type: `honeycombStationLattice` carries the
        // same two requirements verbatim and `derivedPhase`'s own initialiser reaches it, so a
        // widened guard here still throws -- from downstream, and only the wording tells them
        // apart (`C-0207` section 8, met on a third object; found by a SURVIVING mutation).
        assert(
            assertFailsWith<IllegalArgumentException> { RouteBStationLadder(0, 10) }
                .message!!.startsWith("a route-B station ladder needs a positive rowBasePairs")
        )
        assert(
            assertFailsWith<IllegalArgumentException> { RouteBStationLadder(92, 0) }
                .message!!.startsWith("a route-B station ladder needs at least one rooting helix")
        )
        assertFailsWith<IllegalArgumentException> { RouteBStationLadder(92, 10, -1) }
        assertFailsWith<IllegalArgumentException> {
            RouteBStationLadder(92, 10, 14, periodBasePairs = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            RouteBStationLadder(92, 10).minimumStationsAtPhase(21)
        }
        assertFailsWith<IllegalArgumentException> {
            RouteBStationLadder(92, 10).minimumStationsAtPhase(-1)
        }
        assertFailsWith<IllegalArgumentException> {
            RouteBStationLadder(92, 10).carriesColumns(0)
        }
    }

    // ---------------------------------------------- gate 2: the ratio transfer, a PREDICTION

    @Test
    fun `gate 2 -- a band predicts the product of its own two ends`() {
        val band = TransferredRatioBand(1.4438156198780696, 2.710658735801465)
        val predicted = band.predict(0.0521565503, 0.10)
        assert(abs(predicted.low - 0.0521565503 * 1.4438156198780696) < 1e-15)
        assert(abs(predicted.high - 0.0521565503 * 2.710658735801465) < 1e-15)
    }

    @Test
    fun `gate 2 -- a band excludes, guarantees or straddles, and exactly one of the three`() {
        val excluding = TransferredRatioBand(2.2359, 3.5094).predict(0.0576976711, 0.10)
        assert(excluding.excludesFlat)
        assert(!excluding.guaranteesFlat)
        assert(!excluding.straddles)
        val straddling = TransferredRatioBand(1.4438, 2.7107).predict(0.0521565503, 0.10)
        assert(straddling.straddles)
        assert(!straddling.excludesFlat)
        assert(!straddling.guaranteesFlat)
        val guaranteeing = TransferredRatioBand(1.0, 1.5).predict(0.02, 0.10)
        assert(guaranteeing.guaranteesFlat)
        assert(!guaranteeing.straddles)
    }

    @Test
    fun `gate 2 -- a band CONTAINS a reading, which is F20's own predicate`() {
        val predicted = TransferredRatioBand(1.4438, 2.7107).predict(0.0521565503, 0.10)
        assert(predicted.contains(0.09))
        assert(!predicted.contains(0.05))
        assert(!predicted.contains(0.20))
    }

    @Test
    fun `gate 2 -- a band refuses a non-positive or inverted pair`() {
        assertFailsWith<IllegalArgumentException> { TransferredRatioBand(0.0, 2.0) }
        assertFailsWith<IllegalArgumentException> { TransferredRatioBand(2.0, 1.0) }
        assertFailsWith<IllegalArgumentException> {
            TransferredRatioBand(1.0, 2.0).predict(0.0, 0.10)
        }
        assertFailsWith<IllegalArgumentException> {
            TransferredRatioBand(1.0, 2.0).predict(0.05, 0.0)
        }
    }

    // ------------------------------------- gate 3: the uncoupled reference, read out of C-0211

    @Test
    fun `gate 3 -- the reference reads C-0211's recommended b0 at each of the three widths`() {
        val references = routeBUncoupledReferences(ResultInputs.T_315.file(), floorRungName)
        assert(references.map { it.pairedRowBasePairs } == listOf(92, 98, 106))
        assert(references.map { it.classZeroResidue } == listOf(5, 16, 9))
    }

    @Test
    fun `gate 3 -- the reference is C-0211's own WORST corner at that phase`() {
        val file = ResultInputs.T_315.file()
        val references = routeBUncoupledReferences(file, floorRungName)
        val published = routeBPublishedBestWorstCorner(file, floorRungName)
        references.forEach { reference ->
            val expected = published.getValue(reference.pairedRowBasePairs)
            assert(abs(reference.freeTileWithPreload - expected) < 1e-12)
        }
    }

    @Test
    fun `gate 3 -- an unknown rung is refused rather than silently returning nothing`() {
        assertFailsWith<IllegalArgumentException> {
            routeBUncoupledReferences(ResultInputs.T_315.file(), "a rung nobody graded")
        }
    }

    // --------------------------------------- gate 4: the conjunction CH-0272 says must be stated

    @Test
    fun `gate 4 -- flat and admissible is a CONJUNCTION and all three needs all three`() {
        assert(RouteBAdmissibility(true, true, true).allThreeThresholds)
        assert(RouteBAdmissibility(true, true, false).flatAndAdmissible)
        assert(!RouteBAdmissibility(true, true, false).allThreeThresholds)
        assert(!RouteBAdmissibility(true, false, true).flatAndAdmissible)
        assert(!RouteBAdmissibility(false, true, true).flatAndAdmissible)
    }

    // ------------------------------------- gate 5: the lattice, at a row that is NOT 0 mod 7

    private val block = HoneycombBlock(4, 2)

    private val rowBasePairs = 30

    private val edgeY = block.rasterRows *
            HoneycombCrossSectionGeometry.rowPitch(Gen1Tile.INTERHELICAL_HONEYCOMB)

    private val edgeX = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    private val rung = ResolvedLinkRung(
        "the resolved floor", "C-0208's bracket floor", shearCeiling, radialFloor
    )

    private fun tethers() = UniformRasterTethers(
        block = block,
        pairedRowBasePairs = rowBasePairs,
        interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
        phosphateRadius = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS,
        classZeroResidue = 5,
        lowRimNucleotides = 28,
        highRimNucleotides = 28,
        kuhnLength = 2.10,
        contourPerNucleotide = 0.65,
        thermalEnergy = thermalEnergy(ROOM_TEMPERATURE)
    )

    @Test
    fun `gate 5 -- F1, a uniform pressure on the free route-B lattice dishes exactly zero`() {
        // 30 bp is 4 planes and a 2 bp remainder, so the lattice carries a `nodeS` overhang --
        // which is the precondition `CLAUDE.md` records a uniform-load falsifier once caught.
        val lattice = tethers().latticeAtRung(rung, enhancement = 1.0, withPreload = false)
        val stroke = lattice.solve(uniformPressure(interiorPressure)).meanDeflection
        val dishing = lattice.solve(uniformPressure(interiorPressure)).peakDishing(41) / stroke
        assert(dishing < 1e-9)
    }

    @Test
    fun `gate 5 -- F8, the tethered and untied free strokes agree`() {
        val tethered = tethers().latticeAtRung(rung, enhancement = 1.0)
        val untied = honeycombTiedLatticeAtResolvedLink(
            block = block,
            rowBasePairs = rowBasePairs,
            enhancement = 1.0,
            tied = false,
            transverseLinkStiffness = rung.transverseLinkStiffness,
            radialLinkStiffness = rung.radialLinkStiffness
        )
        val a = tethered.solve(uniformPressure(interiorPressure)).meanDeflection
        val b = untied.solve(uniformPressure(interiorPressure)).meanDeflection
        assert(abs(a - b) / b < 1e-9)
    }

    @Test
    fun `gate 5 -- F5, the two surrogates agree about one distribution's peak dishing`() {
        val lattice = tethers().latticeAtRung(rung, enhancement = 1.0)
        val grid = attachmentGrid(2, block.rasterRows, edgeX, edgeY)
        val pressure = uniformPressure(interiorPressure)
        val influence = honeycombTiedSurrogate(lattice, grid, pressure, 41)
        val multi = honeycombMultiStateSurrogate(
            lattice, grid, listOf(LoadState("uniform", pressure)), 41
        )
        val stiffnesses = List(grid.size) { 33.3333333 / grid.size }
        val one = influence.solve(stiffnesses).peakDishing
        val other = multi.peakDishing(stiffnesses)[0]
        assert(abs(one - other) / one < 1e-10)
    }

    @Test
    fun `gate 5 -- F3, the surrogate reproduces the assembled solve at full presence`() {
        val lattice = tethers().latticeAtRung(rung, enhancement = 1.0)
        val grid = attachmentGrid(2, block.rasterRows, edgeX, edgeY)
        val pressure = uniformPressure(interiorPressure)
        val surrogate = honeycombTiedSurrogate(lattice, grid, pressure, 41)
        val stiffnesses = List(grid.size) { 33.3333333 / grid.size }
        val solved = surrogate.solve(stiffnesses)
        val loads = grid.mapIndexed { k, (s, y) -> PointLoad(s, y, -solved.supportForces[k]) }
        val assembled = lattice.solve(pressure, loads).peakDishing(41)
        assert(abs(assembled - solved.peakDishing) / solved.peakDishing < 1e-9)
    }

    @Test
    fun `gate 5 -- the bond census is a function of the row length, and CH-0270 says so`() {
        val block = HoneycombBlock(10, 6)
        listOf(92 to 358, 98 to 385, 106 to 410, 116 to 435).forEach { (row, bonds) ->
            val lattice = honeycombTiedLatticeAtResolvedLink(
                block = block, rowBasePairs = row, enhancement = 1.0, tied = false,
                transverseLinkStiffness = shearCeiling, radialLinkStiffness = radialFloor
            )
            assert(ResolvedLinkBondCensus(lattice, rung).totalBonds == bonds)
        }
    }
}
