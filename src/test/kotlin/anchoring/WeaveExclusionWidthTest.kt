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
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-137`, leaf `A8.2` — the plan model against the **measured** weave.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * **The free limiting case this task declared is here as a test**: at zero weave amplitude the
 * position-dependent exclusion width must reproduce `C-0053`'s and `C-0063`'s single-width packer
 * **bit for bit**, so every standing placement result is reproduced at departure `0.0` rather than
 * re-derived.
 */
class WeaveExclusionWidthTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    private val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * rise

    private val lattice = Gen1Tile.INTERHELICAL_SHEET

    private val arm = C0055_ARM_LENGTH

    private val edgeX = Gen1Tile.EDGE_X

    private val duplexes = 15

    private val phase = 24

    private val profile = WeaveProfile()

    /** `C-0063`'s phase-24 placement, as `C-0066` and `C-0072` publish it. */
    private val phase24Roots: List<List<Double>> = listOf(
        listOf(-16.32, -5.44, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 5.44, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, 16.32),
        listOf(-10.88, 10.88),
        listOf(-16.32, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, -5.44, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, 5.44, 16.32)
    )

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - a weave distance is a length and scales with every length`() {
        val scale = 3.7
        val scaled = WeaveProfile(
            meanDistance = lattice * scale,
            peakToPeak = profile.peakToPeak * scale,
            risePerBasePair = rise * scale
        )
        (0..40).forEach { step ->
            val x = -18.0 + 0.9 * step
            assert(
                scaled.distanceAt(3, x * scale)
                    .isCloseTo(profile.distanceAt(3, x) * scale, 1e-12)
            )
        }
    }

    @Test
    fun `gate 1 - an axis offset is a length and the mean lattice carries no offset`() {
        assert(WeaveProfile(peakToPeak = 0.0).axisOffset(7, 3.21).isCloseTo(0.0, 1e-15))
        assert(profile.axisPosition(0, 0.0).isCloseTo(profile.axisOffset(0, 0.0), 1e-15))
    }

    @Test
    fun `gate 1 - unphysical weave arguments throw`() {
        assertFailsWith<IllegalArgumentException> { WeaveProfile(meanDistance = 0.0) }
        assertFailsWith<IllegalArgumentException> { WeaveProfile(peakToPeak = -0.1) }
        assertFailsWith<IllegalArgumentException> { WeaveProfile(peakToPeak = 6.0) }
        assertFailsWith<IllegalArgumentException> { WeaveProfile(risePerBasePair = 0.0) }
        assertFailsWith<IllegalArgumentException> { WeaveProfile(planeBasePairs = 0) }
        assertFailsWith<IllegalArgumentException> { profile.distanceAtPlane(-1, 0.0) }
        assertFailsWith<IllegalArgumentException> { profile.axisOffset(-1, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            armDirectionsWithClearance(emptyList(), arm, edgeX) { lattice }
        }
        assertFailsWith<IllegalArgumentException> {
            armDirectionsWithClearance(listOf(0.0), -1.0, edgeX) { lattice }
        }
        assertFailsWith<IllegalArgumentException> {
            armDirectionsWithClearance(listOf(0.0), arm, edgeX) { -1.0 }
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - THE FREE LIMITING CASE - a constant clearance reproduces armDirections exactly`() {
        listOf(2.0, 2.69, 2.725, 2.73, 3.6).forEach { width ->
            listOf(6.0, arm, 8.19, 9.131).forEach { length ->
                phase24Roots.forEach { roots ->
                    val standing = armDirections(roots, length, edgeX, width)
                    val general = armDirectionsWithClearance(roots, length, edgeX) { width }
                    assert(general == standing)
                }
            }
        }
    }

    @Test
    fun `gate 2 - a zero amplitude weave is the lattice constant everywhere`() {
        val flat = WeaveProfile(peakToPeak = 0.0)
        (0 until duplexes - 1).forEach { interfaceIndex ->
            (0..120).forEach { step ->
                val x = -19.0 + 38.0 * step / 120.0
                assert(flat.distanceAt(interfaceIndex, x).isCloseTo(lattice, 1e-15))
            }
        }
    }

    @Test
    fun `gate 2 - the weave minimum sits at the interface's own crossovers and the maximum at its neighbours'`() {
        (0 until duplexes - 1).forEach { b ->
            // duplex b faces NORTH — the crossover to b+1 — at plane k with (k - 2b) mod 4 == 0
            (-8..8).forEach { m ->
                val minimumPlane = (2 * b + 4 * m).toDouble()
                val maximumPlane = (2 * b + 2 + 4 * m).toDouble()
                assert(
                    profile.distanceAtPlane(b, minimumPlane)
                        .isCloseTo(profile.minimumDistance, 1e-12)
                )
                assert(
                    profile.distanceAtPlane(b, maximumPlane)
                        .isCloseTo(profile.maximumDistance, 1e-12)
                )
            }
        }
    }

    @Test
    fun `gate 2 - a plan margin at the lattice constant reproduces C-0069's knife edge`() {
        assert(planMarginAtWidth(pitch, lattice, arm).isCloseTo(0.0256, 1e-3))
        assert((pitch - lattice).isCloseTo(8.19, 1e-9))
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 - THE CONGRUENCE - every upward root sits at the weave's node, at every phase`() {
        (0 until 32).forEach { phaseBasePairs ->
            val sites = upwardHingeSites(phaseBasePairs, edgeX, duplexes, rise)
            val weave = WeaveProfile(phaseBasePairs = phaseBasePairs)
            assert(sites.isNotEmpty())
            sites.forEach { site ->
                val duplex = site.interfaceIndex
                assert(weave.axisOffset(duplex, site.x).isCloseTo(0.0, 1e-12))
                if (duplex < duplexes - 1) {
                    assert(weave.distanceAt(duplex, site.x).isCloseTo(lattice, 1e-12))
                }
                if (duplex > 0) {
                    assert(weave.distanceAt(duplex - 1, site.x).isCloseTo(lattice, 1e-12))
                }
            }
        }
    }

    @Test
    fun `gate 3 - the congruence is independent of the weave amplitude`() {
        val sites = upwardHingeSites(phase, edgeX, duplexes, rise)
        listOf(0.0, 0.5, 1.2, 1.5, 1.75, 2.5).forEach { amplitude ->
            val weave = WeaveProfile(peakToPeak = amplitude)
            sites.forEach { site ->
                assert(weave.axisOffset(site.interfaceIndex, site.x).isCloseTo(0.0, 1e-12))
            }
        }
    }

    @Test
    fun `gate 3 - the axis offsets reproduce the interface distances, which nothing forces`() {
        (0..200).forEach { step ->
            val x = -19.0 + 38.0 * step / 200.0
            (0 until duplexes - 1).forEach { b ->
                val fromAxes = profile.axisPosition(b + 1, x) - profile.axisPosition(b, x)
                assert(fromAxes.isCloseTo(profile.distanceAt(b, x), 1e-12))
            }
        }
    }

    @Test
    fun `gate 3 - the lattice mean is conserved - the axis offsets of a period sum to zero`() {
        (0 until duplexes).forEach { b ->
            val over = (0 until 4).sumOf { plane ->
                profile.axisOffsetAtPlane(b, (2 * b + plane).toDouble())
            }
            assert(over.isCloseTo(0.0, 1e-12))
        }
    }

    @Test
    fun `gate 3 - the weave averages to the lattice constant over its own period`() {
        (0 until duplexes - 1).forEach { b ->
            val samples = 4096
            val mean = (0 until samples).sumOf { step ->
                profile.distanceAtPlane(b, 4.0 * step / samples)
            } / samples
            assert(mean.isCloseTo(lattice, 1e-6))
        }
    }

    @Test
    fun `gate 3 - the weave amplitude has coefficient exactly zero on the across-row clearance`() {
        val sites = upwardHingeSites(phase, edgeX, duplexes, rise)
        val body = DuplexSteric.MEASURED_DIAMETER
        val reference = sites.map {
            acrossRowClearance(WeaveProfile(peakToPeak = 0.0), it.interfaceIndex, it.x, body)
        }
        listOf(0.5, 1.2, 1.5, 1.75, 2.5).forEach { amplitude ->
            val moved = sites.map {
                acrossRowClearance(
                    WeaveProfile(peakToPeak = amplitude), it.interfaceIndex, it.x, body
                )
            }
            reference.zip(moved).forEach { (a, b) -> assert(abs(a - b) <= 1e-14) }
        }
        assert(reference.all { it.isCloseTo(lattice - body, 1e-12) })
    }

    @Test
    fun `gate 3 - an arm of an ODD number of planes puts its tip at an antinode and an EVEN one at a node`() {
        val root = upwardHingeSites(phase, edgeX, duplexes, rise).first { it.interfaceIndex == 4 }
        val planeLength = profile.planeSpacing
        val odd = profile.distanceAt(4, root.x + 3 * planeLength)
        val even = profile.distanceAt(4, root.x + 4 * planeLength)
        assert(odd.isCloseTo(profile.maximumDistance, 1e-12))
        assert(even.isCloseTo(lattice, 1e-12))
    }

    // ------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 - the placed count under a position-dependent clearance is resolution independent`() {
        val counts = listOf(0.1, 0.01, 0.001, 0.0001).map { resolution ->
            phase24Roots.withIndex().sumOf { (row, roots) ->
                val snapped = armDirectionsWithClearance(roots, arm, edgeX) { x ->
                    profile.distanceAt(
                        row.coerceAtMost(duplexes - 2), Math.round(x / resolution) * resolution
                    )
                }
                if (snapped != null) roots.size else 0
            }
        }
        assert(counts.distinct().size == 1)
        val atOneWidth = phase24Roots.sumOf { roots ->
            if (armDirections(roots, arm, edgeX, lattice) != null) roots.size else 0
        }
        assert(atOneWidth == C0055_ARM_COUNT)
        assert(counts.first() < atOneWidth)
    }

    @Test
    fun `gate 4 - the weave is Lipschitz in x, so a snapped evaluation converges linearly`() {
        val slope = profile.peakToPeak / (2.0 * profile.planeSpacing)
        listOf(0.1, 0.01, 0.001).forEach { resolution ->
            var worst = 0.0
            (0..4000).forEach { step ->
                val x = -19.0 + 38.0 * step / 4000.0
                val snapped = Math.round(x / resolution) * resolution
                worst = maxOf(
                    worst, abs(profile.distanceAt(4, x) - profile.distanceAt(4, snapped))
                )
            }
            assert(worst <= slope * resolution * (1.0 + 1e-9))
        }
    }

    // ------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 - the measured phosphate diameter admits the measured weave minimum`() {
        val diameter = DuplexSteric.MEASURED_DIAMETER
        assert(diameter < WeaveProfile(peakToPeak = BAI_PEAK_TO_PEAK, meanDistance = BAI_MEAN)
            .minimumDistance)
        assert(diameter.isCloseTo(1.8172757169416848, 1e-12))
    }

    @Test
    fun `gate 5 - Bai's sawtooth midpoint reproduces the square-lattice Bragg constant`() {
        assert(BAI_MEAN.isCloseTo(2.725, 1e-12))
        assert(
            abs(BAI_MEAN - INTERHELICAL_SQUARE_LATTICE) / INTERHELICAL_SQUARE_LATTICE < 0.01
        )
    }

    @Test
    fun `gate 5 - C-0072's weave bracket is reproduced as arithmetic and is on the wrong axis`() {
        assert(planMarginAtWidth(pitch, 1.85, arm).isCloseTo(0.866, 1e-3))
        assert(planMarginAtWidth(pitch, 3.60, arm).isCloseTo(-0.884, 1e-3))
    }

    @Test
    fun `gate 5 - the placement threshold in the exclusion width is exactly pitch minus arm`() {
        val threshold = pitch - arm
        fun placed(width: Double) = phase24Roots.sumOf { roots ->
            if (armDirections(roots, arm, edgeX, width) != null) roots.size else 0
        }
        listOf(DuplexSteric.MEASURED_DIAMETER, 2.0, lattice, threshold - 1e-6).forEach {
            assert(placed(it) == C0055_ARM_COUNT)
        }
        listOf(2.725, INTERHELICAL_SQUARE_LATTICE, threshold + 1e-3).forEach {
            assert(placed(it) < C0055_ARM_COUNT)
        }
        assert(threshold.isCloseTo(2.71561, 1e-5))
    }
}
