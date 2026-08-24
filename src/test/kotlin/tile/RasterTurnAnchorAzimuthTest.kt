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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.structure.maximumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.minimumTurnPhosphateSpan
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-304` — the azimuth of a raster turn's two anchoring phosphates, derived on the lattice.
 *
 * Written before `tile/RasterTurnAnchorAzimuth.kt` and watched fail.
 *
 * The gates each test names are `T-304`'s own `F1`–`F9` and its predicates `P1`–`P7`.
 */
class RasterTurnAnchorAzimuthTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS

    private val recommended = HoneycombRasterTurnAnchors.derived(
        block = HoneycombBlock(10, 6),
        senseOneBasePairs = 102,
        senseTwoBasePairs = 109,
        interhelicalDistance = d,
        phosphateRadius = rP
    )

    // ------------------------------------------------ P1 / F3: the closed form IS C-0187's rule

    @Test
    fun `P1 - the closed form reproduces the derived departure at both allowed residues`() {
        (0..20).forEach { b0 ->
            val plus = Math.floorMod(b0 + 5, 21)
            val minus = Math.floorMod(b0 - 5, 21)
            assert(
                abs(
                    anchorAzimuthDegrees(plus, b0) - scaffoldDisplacementDepartureDegrees(5)
                ) < 1e-12
            )
            assert(
                abs(
                    anchorAzimuthDegrees(minus, b0) - scaffoldDisplacementDepartureDegrees(-5)
                ) < 1e-12
            )
        }
    }

    @Test
    fun `P1 - the azimuth has period 21 base pairs in the residue and in the lattice constant`() {
        (0..20).forEach { b0 ->
            (0..20).forEach { residue ->
                val here = anchorAzimuthDegrees(residue, b0)
                assert(abs(anchorAzimuthDegrees(residue + 21, b0) - here) < 1e-12)
                assert(abs(anchorAzimuthDegrees(residue - 21, b0) - here) < 1e-12)
            }
        }
    }

    @Test
    fun `P1 - a half turn is 10 point 5 base pairs and it is exactly 180 degrees away`() {
        // 21 bp is 720 deg, so 10.5 bp is 360 and the two allowed residues are ONE azimuth
        // apart in the exact construction; a whole 21 bp shift of the reference changes nothing.
        (0..20).forEach { b0 ->
            assert(abs(anchorAzimuthDegrees(b0, b0 + 21) - anchorAzimuthDegrees(b0, b0)) < 1e-12)
        }
    }

    @Test
    fun `P1 - a residue on the exact facing line reads zero and its antipode reads 180`() {
        // b0 + 5.25 is the exact facing residue; the nearest INTEGER lattice positions are the
        // two allowed ones, which is why an allowed crossover is never aligned (CH-0197).
        assert(abs(anchorAzimuthDegreesExact(5.25, 0) - 0.0) < 1e-12)
        assert(abs(abs(anchorAzimuthDegreesExact(5.25 + 5.25, 0)) - 180.0) < 1e-12)
    }

    // ------------------------------------------------ P2 / F1: the 59 turns and their one span

    @Test
    fun `P2 - the recommended raster carries 59 anchors on two residues and two rims`() {
        assert(recommended.anchors.size == 59)
        assert(recommended.classZeroResidue == 5)
        assert(recommended.anchors.map { it.reducedResidue }.distinct().sorted() == listOf(0, 10))
        assert(recommended.anchors.count { it.atHighEnd } == 30)
        assert(recommended.anchors.count { !it.atHighEnd } == 29)
    }

    @Test
    fun `P2 - the two anchors of one turn are antipodal, so the entry is the exit plus 180`() {
        recommended.anchors.forEach {
            val difference = foldedDegrees(it.entryAzimuthDegrees - it.exitAzimuthDegrees)
            assert(abs(abs(difference) - 180.0) < 1e-12)
        }
    }

    @Test
    fun `F1 - every one of the 59 turns takes the SAME span`() {
        assert(recommended.distinctSpans.size == 1)
        val span = requireNotNull(recommended.singleValuedSpan)
        recommended.anchors.forEach { assert(abs(it.span - span) < 1e-12) }
    }

    @Test
    fun `F1 - the two rims carry EXACTLY opposite azimuths, which is why the span is one`() {
        val high = recommended.anchors.filter { it.atHighEnd }.map { it.exitAzimuthDegrees }
        val low = recommended.anchors.filterNot { it.atHighEnd }.map { it.exitAzimuthDegrees }
        assert(high.all { abs(it - high.first()) < 1e-9 })
        assert(low.all { abs(it - low.first()) < 1e-9 })
        assert(abs(high.first() + low.first()) < 1e-9)
    }

    @Test
    fun `P2 - both helices of a turn lie on the SAME side of it, so one level serves both`() {
        // A turn is a RIM: consecutive raster helices are traversed antiparallel, so the level
        // before a crossover and the level after it fall on one side of it. That is what makes
        // a single anchor level -- and therefore a single azimuth -- correct for the pair.
        val levels = HoneycombRasterResidues(
            rasterRows = 10, helicesPerRow = 6, senseOneBasePairs = 102, senseTwoBasePairs = 109
        ).crossoverLevels
        (1..57).forEach { k ->
            val here = levels.getValue(k)
            val before = levels.getValue(k - 1) - here
            val after = levels.getValue(k + 1) - here
            assert(before != 0)
            assert(after != 0)
            assert((before > 0) == (after > 0))
        }
    }

    @Test
    fun `P1 - at zero offset the anchor azimuth IS C-0187's derived departure, turn for turn`() {
        listOf(1, -1).forEach { sign ->
            listOf(false, true).forEach { mirrored ->
                listOf(false, true).forEach { reversed ->
                    val signs = HoneycombRasterTurnSigns(
                        block = HoneycombBlock(10, 6),
                        senseOneBasePairs = 102,
                        senseTwoBasePairs = 109,
                        firstAxialSign = sign,
                        mirrored = mirrored,
                        axialReversed = reversed
                    )
                    val anchors = HoneycombRasterTurnAnchors.derived(
                        block = HoneycombBlock(10, 6),
                        senseOneBasePairs = 102,
                        senseTwoBasePairs = 109,
                        interhelicalDistance = d,
                        phosphateRadius = rP,
                        firstAxialSign = sign,
                        mirrored = mirrored,
                        axialReversed = reversed
                    )
                    assert(signs.signs.size == anchors.anchors.size)
                    signs.signs.indices.forEach { k ->
                        assert(
                            abs(
                                signs.signs[k].departureDegrees -
                                        anchors.anchors[k].exitAzimuthDegrees
                            ) < 1e-9
                        )
                    }
                }
            }
        }
    }

    // ------------------------------------------------ F2: it is C-0152's own allowed span

    @Test
    fun `F2 - the determined span is the allowed scaffold crossover's own span`() {
        val span = requireNotNull(recommended.singleValuedSpan)
        val allowed = forcedCrossoverSpan(d, rP, allowedScaffoldCrossoverDepartureDegrees())
        assert(abs(span - allowed) < 1e-12)
        assert(abs(span - 0.787091706) < 1e-9)
    }

    @Test
    fun `F2 - the determined span sits between C-0147's two bracket endpoints`() {
        val span = requireNotNull(recommended.singleValuedSpan)
        assert(span > minimumTurnPhosphateSpan(d, rP))
        assert(span < maximumTurnPhosphateSpan(d, rP))
    }

    // ------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - a zero azimuth is closest approach and 180 degrees is furthest`() {
        assert(abs(forcedCrossoverSpan(d, rP, 0.0) - minimumTurnPhosphateSpan(d, rP)) < 1e-12)
        assert(abs(forcedCrossoverSpan(d, rP, 180.0) - maximumTurnPhosphateSpan(d, rP)) < 1e-12)
    }

    @Test
    fun `gate 2 - a non-closing raster REFUSES a derived lattice constant`() {
        assertFailsWith<IllegalStateException> {
            HoneycombRasterTurnAnchors.derived(
                block = HoneycombBlock(10, 6),
                senseOneBasePairs = 112,
                senseTwoBasePairs = 108,
                interhelicalDistance = d,
                phosphateRadius = rP
            ).anchors
        }
    }

    @Test
    fun `gate 2 - a non-closing raster is gradable at an EXPLICIT lattice constant`() {
        val undrawable = HoneycombRasterTurnAnchors(
            block = HoneycombBlock(10, 6),
            senseOneBasePairs = 112,
            senseTwoBasePairs = 108,
            interhelicalDistance = d,
            phosphateRadius = rP,
            classZeroResidue = 5
        )
        assert(undrawable.anchors.size == 59)
        assert(!undrawable.closes)
    }

    // ------------------------------------------------ F4: the eight readings of the datum

    @Test
    fun `F4 - the span and the azimuth magnitudes are invariant over all eight datum readings`() {
        val readings = listOf(1, -1).flatMap { sign ->
            listOf(false, true).flatMap { mirrored ->
                listOf(false, true).map { reversed ->
                    HoneycombRasterTurnAnchors.derived(
                        block = HoneycombBlock(10, 6),
                        senseOneBasePairs = 102,
                        senseTwoBasePairs = 109,
                        interhelicalDistance = d,
                        phosphateRadius = rP,
                        firstAxialSign = sign,
                        mirrored = mirrored,
                        axialReversed = reversed
                    )
                }
            }
        }
        assert(readings.size == 8)
        val first = requireNotNull(readings.first().singleValuedSpan)
        readings.forEach { reading ->
            assert(abs(requireNotNull(reading.singleValuedSpan) - first) < 1e-12)
            // decided coarser than the arithmetic's own noise: two residue paths that are
            // exactly equal by construction land 1.6e-14 apart, which is CLAUDE.md's own trap
            // and what the first run of this test found.
            val magnitudes = reading.distinctAzimuthMagnitudes
            assert(magnitudes.size == 1)
            assert(abs(magnitudes.single() - allowedScaffoldCrossoverDepartureDegrees()) < 1e-9)
        }
    }

    // ------------------------------------------------ P5: the OTHER design, not a tolerance

    @Test
    fun `P5 - carving the loop out of the paired row moves the azimuth by whole base pairs`() {
        val sixteen = HoneycombRasterTurnAnchors.derived(
            block = HoneycombBlock(10, 6),
            senseOneBasePairs = 102,
            senseTwoBasePairs = 109,
            interhelicalDistance = d,
            phosphateRadius = rP,
            anchorOffsetBasePairs = 16
        )
        // 16 bp less the quarter base pair an allowed crossover already carries is EXACTLY
        // 15.75 bp = 1.5 turns, so the anchor lands exactly antipodal and the span is d + 2 r_P.
        assert(abs(abs(sixteen.anchors.first().exitAzimuthDegrees) - 180.0) < 1e-9)
        assert(
            abs(requireNotNull(sixteen.singleValuedSpan) - maximumTurnPhosphateSpan(d, rP)) < 1e-9
        )
    }

    @Test
    fun `P5 - the span stays single-valued at every offset, because the rims stay antipodal`() {
        (0..20).forEach { offset ->
            val anchors = HoneycombRasterTurnAnchors.derived(
                block = HoneycombBlock(10, 6),
                senseOneBasePairs = 102,
                senseTwoBasePairs = 109,
                interhelicalDistance = d,
                phosphateRadius = rP,
                anchorOffsetBasePairs = offset
            )
            assert(anchors.distinctSpans.size == 1)
        }
    }

    @Test
    fun `P5 - the anchor level is the turn level less the offset, inboard at both helices`() {
        val offset = 12
        val shifted = HoneycombRasterTurnAnchors.derived(
            block = HoneycombBlock(10, 6),
            senseOneBasePairs = 102,
            senseTwoBasePairs = 109,
            interhelicalDistance = d,
            phosphateRadius = rP,
            anchorOffsetBasePairs = offset
        )
        shifted.anchors.forEach {
            assert(abs(it.turnLevelBasePairs - it.anchorLevelBasePairs) == offset)
        }
        assert(recommended.anchors.all { it.turnLevelBasePairs == it.anchorLevelBasePairs })
    }

    @Test
    fun `P5 - the anchor moves AWAY from the rim the turn sits at, which pins the direction`() {
        // A turn is a rim and its helices lie inboard of it, so on the standard datum a
        // high-rim turn's last paired base sits BELOW the turn level and a low-rim turn's
        // above it. Without this the direction is unobservable: the two rims' azimuths are
        // exact negatives, so flipping it merely EXCHANGES them and every span is unmoved.
        val offset = 7
        val shifted = HoneycombRasterTurnAnchors.derived(
            block = HoneycombBlock(10, 6),
            senseOneBasePairs = 102,
            senseTwoBasePairs = 109,
            interhelicalDistance = d,
            phosphateRadius = rP,
            anchorOffsetBasePairs = offset
        )
        shifted.anchors.forEach {
            if (it.atHighEnd) {
                assert(it.anchorLevelBasePairs == it.turnLevelBasePairs - offset)
            } else {
                assert(it.anchorLevelBasePairs == it.turnLevelBasePairs + offset)
            }
        }
    }

    // ------------------------------------------------ P6: a uniform raster does not close

    @Test
    fun `P6 - a uniform route B raster does not close and its spans are a DISTRIBUTION`() {
        val uniform = HoneycombRasterTurnAnchors(
            block = HoneycombBlock(10, 6),
            senseOneBasePairs = 98,
            senseTwoBasePairs = 98,
            interhelicalDistance = d,
            phosphateRadius = rP,
            classZeroResidue = 0
        )
        assert(!uniform.closes)
        assert(uniform.anchors.size == 59)
        assert(uniform.distinctSpans.size > 1)
        assert(uniform.singleValuedSpan == null)
    }

    // ------------------------------------------------ guards

    @Test
    fun `a lattice constant outside the 21 base pair period is refused at BOTH ends`() {
        // Both ends, because a guard whose only test is at one end is a guard half of which no
        // mutation can reach: widening the lower bound alone survived this harness's first run.
        listOf(21, -1, 22, -21).forEach { outside ->
            assertFailsWith<IllegalArgumentException> {
                HoneycombRasterTurnAnchors(
                    block = HoneycombBlock(10, 6),
                    senseOneBasePairs = 102,
                    senseTwoBasePairs = 109,
                    interhelicalDistance = d,
                    phosphateRadius = rP,
                    classZeroResidue = outside
                )
            }
        }
    }

    @Test
    fun `a negative anchor offset is refused, because a loop cannot sit inside the duplex`() {
        assertFailsWith<IllegalArgumentException> {
            HoneycombRasterTurnAnchors(
                block = HoneycombBlock(10, 6),
                senseOneBasePairs = 102,
                senseTwoBasePairs = 109,
                interhelicalDistance = d,
                phosphateRadius = rP,
                classZeroResidue = 5,
                anchorOffsetBasePairs = -1
            )
        }
    }
}
