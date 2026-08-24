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
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-307` — route B's own **uniform** rasters, whose turns take a span **distribution**.
 *
 * Written before `tile/UniformRasterTetherSpans.kt` and watched fail.
 *
 * The gates each test names are `T-307`'s own `F1`–`F9`.
 */
class UniformRasterTetherSpansTest {

    private val block = HoneycombBlock(10, 6)
    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    private val kT = thermalEnergy(ROOM_TEMPERATURE)

    private fun tethers(
        pairedRowBasePairs: Int = 92,
        classZeroResidue: Int = 0,
        lowRimNucleotides: Int = 28,
        highRimNucleotides: Int = 28,
        kuhnLength: Double = 2.10,
        contourPerNucleotide: Double = 0.65
    ) = UniformRasterTethers(
        block = block,
        pairedRowBasePairs = pairedRowBasePairs,
        interhelicalDistance = d,
        phosphateRadius = rP,
        classZeroResidue = classZeroResidue,
        lowRimNucleotides = lowRimNucleotides,
        highRimNucleotides = highRimNucleotides,
        kuhnLength = kuhnLength,
        contourPerNucleotide = contourPerNucleotide,
        thermalEnergy = kT
    )

    // ------------------------------------------- the census indexes the turn list, turn for turn

    @Test
    fun `the span census carries one entry per raster turn, in the turn list's own order`() {
        val turns = honeycombRasterTurnList(block)
        val subject = tethers()
        assert(subject.spans.size == turns.size)
        assert(subject.states.size == turns.size)
        assert(turns.size == block.helices - 1)
    }

    @Test
    fun `every span is the anchor census's own span at the same index`() {
        val subject = tethers(pairedRowBasePairs = 106, classZeroResidue = 13)
        val anchors = HoneycombRasterTurnAnchors(
            block = block,
            senseOneBasePairs = 106,
            senseTwoBasePairs = 106,
            interhelicalDistance = d,
            phosphateRadius = rP,
            classZeroResidue = 13
        )
        anchors.anchors.forEachIndexed { k, anchor ->
            assert(abs(subject.spans[k] - anchor.span) < 1e-15)
        }
    }

    // ------------------------------------------- F3: the summary reproduces T-304's own record

    @Test
    fun `a uniform route-B raster closes at no lattice phase, at all three widths`() {
        listOf(92, 98, 106).forEach { row ->
            (0 until 21).forEach { b0 ->
                assert(!tethers(pairedRowBasePairs = row, classZeroResidue = b0).closes)
            }
        }
    }

    @Test
    fun `the span is a distribution and not a value at every phase of every uniform width`() {
        listOf(92, 98, 106).forEach { row ->
            (0 until 21).forEach { b0 ->
                val subject = tethers(pairedRowBasePairs = row, classZeroResidue = b0)
                assert(subject.singleValuedSpan == null)
                assert(subject.distinctSpans.size > 1)
            }
        }
    }

    @Test
    fun `the 92 bp raster at phase 7 reproduces T-304's committed summary`() {
        val subject = tethers(pairedRowBasePairs = 92, classZeroResidue = 7)
        assert(subject.distinctSpans.size == 4)
        assert(abs(subject.minimumSpan - 1.19555701) < 1e-8)
        assert(abs(subject.maximumSpan - 4.16526273) < 1e-8)
        assert(abs(subject.meanSpan - 2.55220991) < 1e-8)
        assert(subject.turnsInsideTheAlignedHalf == 40)
    }

    @Test
    fun `the 98 bp raster at phase 7 reproduces T-304's committed summary`() {
        val subject = tethers(pairedRowBasePairs = 98, classZeroResidue = 7)
        assert(subject.turnsInsideTheAlignedHalf == 49)
        assert(subject.distinctSpans.size == 2)
    }

    // ------------------------------------------- the two rim chains, and which turn takes which

    @Test
    fun `a turn at the high rim takes the high rim chain and one at the low rim the low`() {
        val turns = honeycombRasterTurnList(block)
        val subject = tethers(lowRimNucleotides = 24, highRimNucleotides = 32)
        turns.forEachIndexed { k, turn ->
            assert(
                subject.states[k].unpairedNucleotides == (if (turn.atHighEnd) 32 else 24)
            )
        }
    }

    @Test
    fun `exchanging the two rim chains exchanges the two nucleotide counts and nothing else`() {
        val a = tethers(lowRimNucleotides = 24, highRimNucleotides = 32)
        val b = tethers(lowRimNucleotides = 32, highRimNucleotides = 24)
        a.spans.indices.forEach { assert(abs(a.spans[it] - b.spans[it]) < 1e-15) }
        assert(a.states.map { it.unpairedNucleotides }.toSet() == setOf(24, 32))
        assert(
            a.states.count { it.unpairedNucleotides == 24 } ==
                    b.states.count { it.unpairedNucleotides == 32 }
        )
    }

    // ------------------------------------------- the tension is a load, and it is monotone

    @Test
    fun `a longer span at one turn carries a larger tension there`() {
        val subject = tethers(pairedRowBasePairs = 92, classZeroResidue = 0)
        val ordered = subject.states.sortedBy { it.span }
        for (i in 0 until ordered.size - 1) {
            assert(ordered[i].tension < ordered[i + 1].tension + 1e-12)
        }
    }

    @Test
    fun `the softest corner carries a strictly smaller tension at every turn`() {
        val stiff = tethers(kuhnLength = 2.10, contourPerNucleotide = 0.65)
        val soft = tethers(kuhnLength = 2.84, contourPerNucleotide = 0.70)
        stiff.states.indices.forEach {
            assert(soft.states[it].tension < stiff.states[it].tension)
        }
    }

    // ------------------------------------------- F9: the reach bound

    @Test
    fun `a chain too short to reach its own span is refused rather than extrapolated`() {
        assertFailsWith<IllegalArgumentException> {
            tethers(lowRimNucleotides = 2, highRimNucleotides = 2).states
        }
    }

    @Test
    fun `the reach bound admits every corner of the declared bracket at all three widths`() {
        listOf(92, 98, 106).forEach { row ->
            (0 until 21).forEach { b0 ->
                listOf(24, 28, 32).forEach { nt ->
                    listOf(2.10, 2.84).forEach { b ->
                        listOf(0.65, 0.70).forEach { c ->
                            val subject = tethers(
                                pairedRowBasePairs = row, classZeroResidue = b0,
                                lowRimNucleotides = nt, highRimNucleotides = nt,
                                kuhnLength = b, contourPerNucleotide = c
                            )
                            assert(subject.states.all { it.extensionRatio < 1.0 })
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------- the elements, and the guards on them

    @Test
    fun `the element list carries one tether per turn at the turn's own rim node`() {
        val subject = tethers()
        val bare = HoneycombGrillage(
            block = block,
            rowBasePairs = 92,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT
        )
        val elements = subject.elements(bare.nodesPerBeam, withPreload = true)
        val turns = honeycombRasterTurnList(block)
        assert(elements.size == turns.size)
        elements.forEachIndexed { k, element ->
            assert(element.lowerBeam == turns[k].lowerBeam)
            assert(element.upperBeam == turns[k].upperBeam)
            assert(element.node == (if (turns[k].atHighEnd) bare.nodesPerBeam - 1 else 0))
            assert(element.tension > 0.0)
        }
    }

    @Test
    fun `dropping the preload leaves every stiffness where it was and every tension at zero`() {
        val subject = tethers()
        val bare = HoneycombGrillage(
            block = block,
            rowBasePairs = 92,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT
        )
        val loaded = subject.elements(bare.nodesPerBeam, withPreload = true)
        val free = subject.elements(bare.nodesPerBeam, withPreload = false)
        loaded.indices.forEach {
            assert(free[it].tension == 0.0)
            assert(free[it].secantStiffness == loaded[it].secantStiffness)
            assert(free[it].tangentStiffness == loaded[it].tangentStiffness)
        }
    }

    @Test
    fun `a stiffness-free element list carries the tensions and no stiffness at all`() {
        val subject = tethers()
        val bare = HoneycombGrillage(
            block = block,
            rowBasePairs = 92,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT
        )
        val loadOnly = subject.elements(bare.nodesPerBeam, withPreload = true, stiffness = 0.0)
        val loaded = subject.elements(bare.nodesPerBeam, withPreload = true)
        loadOnly.indices.forEach {
            assert(loadOnly[it].secantStiffness == 0.0)
            assert(loadOnly[it].tangentStiffness == 0.0)
            assert(loadOnly[it].tension == loaded[it].tension)
        }
    }

    @Test
    fun `the element carries the chain's own secant and tangent, and not each other's`() {
        val subject = tethers()
        val bare = HoneycombGrillage(
            block = block,
            rowBasePairs = 92,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT
        )
        val elements = subject.elements(bare.nodesPerBeam, withPreload = true)
        elements.indices.forEach {
            assert(elements[it].secantStiffness == subject.states[it].secantStiffness)
            assert(elements[it].tangentStiffness == subject.states[it].tangentStiffness)
            assert(elements[it].tangentStiffness != elements[it].secantStiffness)
        }
    }

    @Test
    fun `a unit-tension column carries one unit at its own turn and nothing anywhere else`() {
        val subject = tethers()
        val bare = HoneycombGrillage(
            block = block,
            rowBasePairs = 92,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT
        )
        listOf(0, 17, 58).forEach { index ->
            val column = subject.unitTensionElements(bare.nodesPerBeam, index)
            assert(column.size == 59)
            column.forEachIndexed { k, element ->
                assert(element.tension == (if (k == index) 1.0 else 0.0))
                assert(element.secantStiffness == 0.0)
                assert(element.tangentStiffness == 0.0)
            }
        }
        assertFailsWith<IllegalArgumentException> {
            subject.unitTensionElements(bare.nodesPerBeam, 59)
        }
    }

    @Test
    fun `an inboard anchor offset moves every span, and it is carried to the census`() {
        val plain = tethers(pairedRowBasePairs = 98, classZeroResidue = 3)
        val offset = UniformRasterTethers(
            block = block,
            pairedRowBasePairs = 98,
            interhelicalDistance = d,
            phosphateRadius = rP,
            classZeroResidue = 3,
            lowRimNucleotides = 28,
            highRimNucleotides = 28,
            kuhnLength = 2.10,
            contourPerNucleotide = 0.65,
            thermalEnergy = kT,
            anchorOffsetBasePairs = 3
        )
        assert(plain.spans.indices.any { abs(plain.spans[it] - offset.spans[it]) > 1e-6 })
    }

    @Test
    fun `reversing the traversal sense exchanges which rim each turn belongs to`() {
        val forward = tethers(lowRimNucleotides = 24, highRimNucleotides = 32)
        val reversed = UniformRasterTethers(
            block = block,
            pairedRowBasePairs = 92,
            interhelicalDistance = d,
            phosphateRadius = rP,
            classZeroResidue = 0,
            lowRimNucleotides = 24,
            highRimNucleotides = 32,
            kuhnLength = 2.10,
            contourPerNucleotide = 0.65,
            thermalEnergy = kT,
            firstAxialSign = -1
        )
        forward.states.indices.forEach {
            assert(
                forward.states[it].unpairedNucleotides !=
                        reversed.states[it].unpairedNucleotides
            )
        }
    }

    @Test
    fun `a phase outside the lattice period is refused`() {
        assertFailsWith<IllegalArgumentException> { tethers(classZeroResidue = 21).spans }
        assertFailsWith<IllegalArgumentException> { tethers(classZeroResidue = -1).spans }
    }

    @Test
    fun `a phase outside the period is refused at CONSTRUCTION, before any span is derived`() {
        // The anchor census carries the same guard and applies it LAZILY, at the first `.spans`.
        // So a test written on `.spans` cannot see this class's own guard at all: widening it
        // leaves every such test passing, because the downstream one then catches the same
        // value. `C-0204` section 8's *a guard tested at one end only*, met on a DUPLICATED
        // guard rather than on a half-tested one -- and found by a surviving mutation.
        listOf(-1, -21, 21, 22).forEach { phase ->
            assertFailsWith<IllegalArgumentException> { tethers(classZeroResidue = phase) }
        }
    }

    @Test
    fun `a non-positive paired row length is refused`() {
        assertFailsWith<IllegalArgumentException> { tethers(pairedRowBasePairs = 0).spans }
    }

    // ------------------------------------------- F4: the empty list is bit-identical

    @Test
    fun `a stiffness-free preload-free tether list is bit-identical to the untethered lattice`() {
        listOf(92, 98, 106).forEach { row ->
            val plain = HoneycombGrillage(
                block = block,
                rowBasePairs = row,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT
            )
            val subject = tethers(pairedRowBasePairs = row)
            val inert = HoneycombGrillage(
                block = block,
                rowBasePairs = row,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                scaffoldTurnTethers =
                    subject.elements(plain.nodesPerBeam, withPreload = false, stiffness = 0.0)
            )
            val a = plain.assembleLoad(uniformPressure(1.0))
            val b = inert.assembleLoad(uniformPressure(1.0))
            for (i in 0 until plain.degreesOfFreedom) assert(a[i] == b[i])
        }
    }

    // ------------------------------------------- F1: the standing uniform-load falsifier

    @Test
    fun `a uniform pressure on the free tethered lattice dishes zero at all three row lengths`() {
        listOf(92, 98, 106).forEach { row ->
            val lattice = tethers(pairedRowBasePairs = row).lattice(
                enhancement = 1.0, withPreload = false
            )
            val field = lattice.solve(uniformPressure(1e-3))
            assert(abs(field.peakDishing(81) / field.meanDeflection) < 1e-9)
        }
    }

    @Test
    fun `the beams reach the tile edge even where the row is not a multiple of the plane pitch`() {
        listOf(92 to 1, 98 to 0, 106 to 1).forEach { (row, remainder) ->
            assert(row % 7 == remainder)
            val lattice = HoneycombGrillage(
                block = block,
                rowBasePairs = row,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT
            )
            val half = row * Gen1Tile.RISE_PER_BASE_PAIR / 2.0
            assert(abs(lattice.nodeS.last() - half) < 1e-12)
            assert(abs(lattice.nodeS.first() + half) < 1e-12)
        }
    }

    // ------------------------------------------- the lattice the study grades

    @Test
    fun `the graded lattice carries the row length it was asked for and 59 tethers`() {
        val lattice = tethers(pairedRowBasePairs = 106).lattice(enhancement = 1.0)
        assert(lattice.rowBasePairs == 106)
        assert(lattice.scaffoldTurnTethers.size == 59)
    }

    @Test
    fun `the preload alone does zero work on a rigid roll, per element and not only in the sum`() {
        val subject = tethers()
        val whole = subject.lattice(enhancement = 1.0)
        fun work(lattice: HoneycombGrillage): Double {
            val load = lattice.tetherPreloadLoad()
            var total = 0.0
            for (node in 0 until lattice.nodesPerBeam) {
                for (beam in 0 until lattice.beamCount) {
                    val base = (node * lattice.beamCount + beam) * HoneycombGrillage.DOF_PER_NODE
                    total += load[base + HoneycombGrillage.W] * 1e-3 * lattice.beamY[beam]
                    total += load[base + HoneycombGrillage.PHI] * 1e-3
                }
            }
            return abs(total)
        }
        assert(work(whole) < 1e-12)
        whole.scaffoldTurnTethers.forEach { one ->
            assert(
                work(
                    HoneycombGrillage(
                        block = block,
                        rowBasePairs = 92,
                        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                        scaffoldTurnTethers = listOf(one)
                    )
                ) < 1e-12
            )
        }
    }
}
