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
import com.xemantic.nano.plentyofroom.structure.HONEYCOMB_BOND_OFFSETS
import com.xemantic.nano.plentyofroom.structure.HoneycombSublattice
import com.xemantic.nano.plentyofroom.structure.honeycombAzimuthDegrees
import kotlin.test.Test

class HoneycombBondClassResiduesTest {

    // ------------------------------------------------------------------ the residue map itself

    @Test
    fun `the class-zero azimuth is 330 on A and 150 on B, and they are the same bond`() {
        assert(honeycombBondClass(HoneycombSublattice.A, 330.0) == 0)
        assert(honeycombBondClass(HoneycombSublattice.A, 210.0) == 1)
        assert(honeycombBondClass(HoneycombSublattice.A, 90.0) == 2)
        assert(honeycombBondClass(HoneycombSublattice.B, 150.0) == 0)
        assert(honeycombBondClass(HoneycombSublattice.B, 30.0) == 1)
        assert(honeycombBondClass(HoneycombSublattice.B, 270.0) == 2)
    }

    @Test
    fun `a bond has ONE class, read from either of its two ends`() {
        HoneycombSublattice.entries.forEach { sublattice ->
            HONEYCOMB_BOND_OFFSETS.getValue(sublattice).forEach { (dx, dy) ->
                val here = honeycombAzimuthDegrees(dx, dy)
                val there = honeycombAzimuthDegrees(-dx, -dy)
                val other =
                    if (sublattice == HoneycombSublattice.A) HoneycombSublattice.B
                    else HoneycombSublattice.A
                assert(honeycombBondClass(sublattice, here) == honeycombBondClass(other, there))
            }
        }
    }

    @Test
    fun `both sublattices carry three azimuths and ONE residue each - a parity says WHICH`() {
        val census = HoneycombSublattice.entries.associateWith { sublattice ->
            HONEYCOMB_BOND_OFFSETS.getValue(sublattice)
                .map { (dx, dy) -> honeycombBondClass(sublattice, honeycombAzimuthDegrees(dx, dy)) }
        }
        census.values.forEach { classes ->
            assert(classes.size == 3)
            assert(classes.toSet().size == 3)
        }
        assert(census.getValue(HoneycombSublattice.A).toSet() ==
                census.getValue(HoneycombSublattice.B).toSet())
    }

    @Test
    fun `a staple residue advances 7 bp per class step and repeats at 21`() {
        assert(honeycombStapleResidue(HoneycombSublattice.A, 330.0, 5) == 5)
        assert(honeycombStapleResidue(HoneycombSublattice.A, 210.0, 5) == 12)
        assert(honeycombStapleResidue(HoneycombSublattice.A, 90.0, 5) == 19)
        assert(honeycombStapleResidue(HoneycombSublattice.B, 30.0, 5) == 12)
        assert(honeycombStapleResidue(HoneycombSublattice.B, 270.0, 16) == 9)
    }

    @Test
    fun `a scaffold crossover sits five base pairs either side of its staple position`() {
        assert(honeycombScaffoldResidues(HoneycombSublattice.A, 330.0, 5) == setOf(0, 10))
        assert(honeycombScaffoldResidues(HoneycombSublattice.B, 270.0, 16) == setOf(4, 14))
    }

    @Test
    fun `the residue map reproduces C-0136's own row-length rule term for term`() {
        (1..2).forEach { effectiveSense ->
            assert(
                admissibleRowLengthResidues(effectiveSense) ==
                        setOf(0, 10, 11).map { Math.floorMod(7 * effectiveSense + it, 21) }.toSet()
            )
        }
        assert(admissibleRowLengthResidues(1) == setOf(7, 17, 18))
        assert(admissibleRowLengthResidues(2) == setOf(14, 3, 4))
    }

    // ------------------------------------------------------------------ the raster it is read on

    @Test
    fun `the residue walk reproduces C-0140's own helix windows exactly`() {
        listOf(10 to 6, 15 to 4).forEach { (rows, per) ->
            val mine = HoneycombRasterResidues(rows, per, 112, 108)
            val theirs = TwoLengthRaster(rows, per, 112, 108)
            assert(mine.helixWindows == theirs.helixSpans.mapValues {
                AxialWindow(it.value.first, it.value.second)
            })
        }
    }

    @Test
    fun `C-0140's recommended 112 over 108 raster does NOT close on the scaffold lattice`() {
        val r = HoneycombRasterResidues(10, 6, 112, 108)
        assert(r.distinctReducedResidues == listOf(0, 10, 11))
        assert(!r.closes)
        assert(r.classZeroResidueCandidates.isEmpty())
        assert(r.offRuleCrossovers == 10)
        assert(r.rasterCrossovers == 59)
    }

    @Test
    fun `102 over 109 is the one pair of C-0140's five that closes`() {
        val closing = listOf(112 to 108, 101 to 109, 102 to 109, 112 to 109, 122 to 119)
            .filter { (a, b) -> HoneycombRasterResidues(10, 6, a, b).closes }
        assert(closing == listOf(102 to 109))
        val r = HoneycombRasterResidues(10, 6, 102, 109)
        assert(r.distinctReducedResidues == listOf(0, 10))
        assert(r.classZeroResidueCandidates == listOf(5))
        assert(r.offRuleCrossovers == 0)
    }

    @Test
    fun `no UNIFORM row length closes, which is C-0140's own negative from a new direction`() {
        listOf(102, 108, 109, 112, 119).forEach { length ->
            assert(!HoneycombRasterResidues(10, 6, length, length).closes)
        }
    }

    @Test
    fun `the closure verdict does not move with the axial sign, the mirror or the datum`() {
        listOf(true, false).forEach { mirrored ->
            listOf(1, -1).forEach { sign ->
                listOf(true, false).forEach { reversed ->
                    assert(!HoneycombRasterResidues(10, 6, 112, 108, sign, mirrored, reversed).closes)
                    assert(HoneycombRasterResidues(10, 6, 102, 109, sign, mirrored, reversed).closes)
                }
            }
        }
    }

    // ------------------------------------------------------------------ the inter-row offset

    @Test
    fun `the inter-row ladder offset is 14 bp on every buildable reading`() {
        listOf(10 to 6, 15 to 4).forEach { (rows, per) ->
            listOf(112 to 108, 102 to 109).forEach { (a, b) ->
                listOf(1, -1).forEach { sign ->
                    listOf(1, -1).forEach { normal ->
                        val r = HoneycombRasterResidues(rows, per, a, b, sign)
                        assert(r.interRowOffsetBasePairs(normal) == 14)
                    }
                }
            }
        }
    }

    @Test
    fun `a cross-section mirror returns 7 only when the axial datum is not mirrored with it`() {
        val improper = HoneycombRasterResidues(10, 6, 102, 109, mirrored = true)
        assert(improper.interRowOffsetBasePairs(1) == 7)
        val proper = HoneycombRasterResidues(10, 6, 102, 109, mirrored = true, axialReversed = true)
        assert(proper.interRowOffsetBasePairs(1) == 14)
    }

    // ------------------------------------------------------------------ the determined phase

    @Test
    fun `at the closing pair the ladder phase is determined and it is 16`() {
        listOf(10 to 6, 15 to 4).forEach { (rows, per) ->
            val r = HoneycombRasterResidues(rows, per, 102, 109)
            assert(r.determinedLadderPhaseBasePairs(1) == 16)
        }
    }

    @Test
    fun `the determined phase carries 55 of 60 stations and 82 of 90`() {
        assert(HoneycombRasterResidues(10, 6, 102, 109).stationsOnFace(1) == 55)
        assert(HoneycombRasterResidues(15, 4, 102, 109).stationsOnFace(1) == 82)
        assert(HoneycombRasterResidues(10, 6, 102, 109).stationsPerRow(1) ==
                listOf(5, 6, 5, 6, 5, 6, 5, 6, 5, 6))
    }

    // ------------------------------------------------------------------ the axial windows

    @Test
    fun `every x-raster row spans 112 bp and every interface window is 108`() {
        val r = HoneycombRasterResidues(10, 6, 112, 108)
        assert(r.rowWindows.map { it.basePairs }.toSet() == setOf(112))
        assert(r.interfaceWindows.toSet() == setOf(AxialWindow(-112, -4)))
        assert(r.blockWindow == AxialWindow(-116, 0))
        assert(r.allHelixWindow == AxialWindow(-108, -4))
    }

    @Test
    fun `the twelfth crossover column belongs to the bounding box and to no row`() {
        val r = HoneycombRasterResidues(10, 6, 112, 108)
        val pitch = 21 * 0.34 / 2.0
        listOf(0.05, 0.17, 0.34).forEach { margin ->
            assert(crossoverColumnsIn(r.rowWindows.first().nm(0.34), pitch, margin) == 11)
            assert(crossoverColumnsIn(r.interfaceWindows.first().nm(0.34), pitch, margin) == 11)
        }
        assert(crossoverColumnsIn(r.blockWindow.nm(0.34), pitch, 0.05) == 12)
        assert(crossoverColumnsIn(r.blockWindow.nm(0.34), pitch, 0.17) == 11)
    }

    @Test
    fun `the guard is inert exactly when the slack past the last pitch exceeds its range`() {
        val pitch = 21 * 0.34 / 2.0
        assert(guardIsInert(38.08, pitch, listOf(0.05, 0.17, 0.34)))
        assert(guardIsInert(36.72, pitch, listOf(0.05, 0.17, 0.34)))
        assert(!guardIsInert(39.44, pitch, listOf(0.05, 0.17, 0.34)))
    }
}
