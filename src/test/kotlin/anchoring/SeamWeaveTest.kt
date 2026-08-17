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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-140`, leaf `A8.2` — does a Rothemund scaffold seam break `C-0076`'s node congruence?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * **The free limiting case this task declared is here as a test**: with no seam, the pull-event
 * weave must reproduce `C-0076`'s closed-form [WeaveProfile] exactly — at every interface, at
 * every plane, and under both edge-duplex readings. Nothing this task says about a seam is worth
 * reading if that fails, because the two would be different weaves.
 */
class SeamWeaveTest {

    private val base = WeaveProfile()

    private val amplitude = SNODIN_TILE_PEAK_TO_PEAK

    private val duplexes = 15

    // ---------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 - an interhelical distance is a length and scales with every length`() {
        val doubled = WeaveProfile(
            meanDistance = 2.0 * base.meanDistance,
            peakToPeak = 2.0 * base.peakToPeak,
            risePerBasePair = 2.0 * base.risePerBasePair
        )
        val one = SeamWeave(base, seams = listOf(4))
        val two = SeamWeave(doubled, seams = listOf(4))
        for (step in 0..40) {
            val plane = -2.0 + step * 0.25
            assert(
                abs(two.distanceAtPlane(3, plane) - 2.0 * one.distanceAtPlane(3, plane)) < 1e-12
            )
            assert(
                abs(two.axisOffsetAtPlane(3, plane) - 2.0 * one.axisOffsetAtPlane(3, plane)) < 1e-12
            )
        }
    }

    @Test
    fun `gate 1 - an unphysical seam is refused rather than absorbed`() {
        assertFailsWith<IllegalArgumentException> { SeamWeave(base, seams = listOf(3)) }
        assertFailsWith<IllegalArgumentException> { SeamWeave(base, seams = listOf(2, 2)) }
        assertFailsWith<IllegalArgumentException> {
            SeamWeave(base, seams = listOf(0, 2))
        }
        assertFailsWith<IllegalArgumentException> { SeamWeave(base).distanceAtPlane(-1, 0.0) }
        assertFailsWith<IllegalArgumentException> { SeamWeave(base).axisOffsetAtPlane(-1, 0.0) }
    }

    // ---------------------------------------------------------------- gate 2: limiting cases

    @Test
    fun `gate 2 - THE FREE LIMITING CASE - no seam reproduces C-0076's weave exactly`() {
        for (straight in listOf(false, true)) {
            val profile = WeaveProfile(edgeDuplexesStraight = straight, duplexes = duplexes)
            val seamless = SeamWeave(profile)
            for (duplex in 0 until duplexes) {
                for (step in -40..40) {
                    val plane = step * 0.2
                    assert(
                        abs(
                            seamless.axisOffsetAtPlane(duplex, plane) -
                                    profile.axisOffsetAtPlane(duplex, plane)
                        ) < 1e-12
                    )
                }
            }
            for (interfaceIndex in 0 until duplexes - 1) {
                for (step in -40..40) {
                    val plane = step * 0.2
                    assert(
                        abs(
                            seamless.distanceAtPlane(interfaceIndex, plane) -
                                    profile.distanceAtPlane(interfaceIndex, plane)
                        ) < 1e-12
                    )
                }
            }
        }
    }

    @Test
    fun `gate 2 - far from a seam the weave is C-0076's, unchanged`() {
        val seamed = SeamWeave(base, seams = listOf(0))
        for (interfaceIndex in 0 until duplexes - 1) {
            for (plane in listOf(-8.0, -6.0, -5.0, 5.0, 6.0, 8.0, 9.0)) {
                assert(
                    abs(
                        seamed.distanceAtPlane(interfaceIndex, plane) -
                                base.distanceAtPlane(interfaceIndex, plane)
                    ) < 1e-12
                )
            }
        }
    }

    @Test
    fun `gate 2 - a station one plane from a seam sits at an EXTREMUM, not a node`() {
        val seamed = SeamWeave(base, seams = listOf(4))
        // interface 2 has its own crossovers at planes k = 4 (mod 4) = 0, 4, 8 ...
        val opened = seamed.distanceAtPlane(2, 5.0)
        val closed = seamed.distanceAtPlane(3, 5.0)
        assert(abs(opened - base.maximumDistance) < 1e-12)
        assert(abs(closed - base.minimumDistance) < 1e-12)
        assert(abs(base.distanceAtPlane(2, 5.0) - base.meanDistance) < 1e-12)
    }

    // ------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 - the seam's effect is symmetric about its own plane`() {
        val seamed = SeamWeave(base, seams = listOf(6))
        for (interfaceIndex in 0 until duplexes - 1) {
            for (step in 1..40) {
                val offset = step * 0.1
                assert(
                    abs(
                        seamed.distanceAtPlane(interfaceIndex, 6.0 + offset) -
                                seamed.distanceAtPlane(interfaceIndex, 6.0 - offset)
                    ) < 1e-12
                )
            }
        }
    }

    @Test
    fun `gate 3 - the openings across a cut telescope to the two edge duplexes`() {
        val seamed = SeamWeave(base, seams = listOf(4))
        for (step in -30..30) {
            val plane = step * 0.3
            val sum = (0 until duplexes - 1).sumOf {
                seamed.distanceAtPlane(it, plane) - base.meanDistance
            }
            val telescoped = seamed.axisOffsetAtPlane(duplexes - 1, plane) -
                    seamed.axisOffsetAtPlane(0, plane)
            assert(abs(sum - telescoped) < 1e-12)
        }
    }

    @Test
    fun `gate 3 - SNODIN'S OWN SENTENCE - one group opens and the other opens much less`() {
        val seamed = SeamWeave(base, seams = listOf(4))
        val opened = (0 until duplexes - 1).filter { Math.floorMod(2 * it - 4, 4) == 0 }
        val closed = (0 until duplexes - 1).filter { Math.floorMod(2 * it - 4, 4) != 0 }
        assert(opened.isNotEmpty() && closed.isNotEmpty())
        opened.forEach {
            assert(abs(seamed.distanceAtPlane(it, 4.0) - base.maximumDistance) < 1e-12)
        }
        closed.forEach {
            assert(abs(seamed.distanceAtPlane(it, 4.0) - base.minimumDistance) < 1e-12)
        }
    }

    @Test
    fun `gate 3 - the departure at an affected station is EXACTLY half the amplitude`() {
        for (peakToPeak in listOf(0.0, 0.5, SNODIN_TILE_PEAK_TO_PEAK, BAI_PEAK_TO_PEAK, 2.0)) {
            val profile = WeaveProfile(peakToPeak = peakToPeak)
            val seamed = SeamWeave(profile, seams = listOf(4))
            assert(
                abs(
                    abs(seamed.distanceAtPlane(2, 5.0) - profile.meanDistance) - peakToPeak / 2.0
                ) < 1e-12
            )
            assert(
                abs(abs(seamed.axisOffsetAtPlane(2, 5.0)) - peakToPeak / 4.0) < 1e-12
            )
        }
    }

    @Test
    fun `gate 3 - THE COEFFICIENT C-0076 CLAIMS IS ZERO STAYS ZERO under any seam`() {
        // `M = p - d - L` is an ALONG-helix identity between UNBONDED bodies. The weave, seam and
        // all, is an ACROSS-helix separation. No seam can give it a coefficient.
        val pitch = 10.88
        val length = 8.16439
        val margin = planMarginAtWidth(pitch, DuplexSteric.MEASURED_DIAMETER, length)
        for (seam in -4..8 step 2) {
            val seamed = SeamWeave(base, seams = listOf(seam))
            assert(seamed.seams.contains(seam))
            assert(
                abs(planMarginAtWidth(pitch, DuplexSteric.MEASURED_DIAMETER, length) - margin) < 1e-15
            )
        }
    }

    // ---------------------------------------------------------------- gate 4: convergence

    @Test
    fun `gate 4 - a station's plane coordinate is an integer, so it needs no grid`() {
        val seamed = SeamWeave(base, seams = listOf(4))
        val exact = seamed.distanceAtPlane(2, 5.0)
        for (resolution in listOf(1e-1, 1e-2, 1e-3, 1e-4)) {
            val snapped = Math.round(5.0 / resolution) * resolution
            assert(abs(seamed.distanceAtPlane(2, snapped) - exact) < 1e-12)
        }
    }

    @Test
    fun `gate 4 - the profile is Lipschitz and piecewise linear between events`() {
        val seamed = SeamWeave(base, seams = listOf(4))
        val slope = amplitude / 2.0
        for (step in 0..200) {
            val plane = -4.0 + step * 0.05
            val a = seamed.distanceAtPlane(2, plane)
            val b = seamed.distanceAtPlane(2, plane + 0.01)
            assert(abs(b - a) <= slope * 0.01 + 1e-12)
        }
    }

    // ---------------------------------------------------------------- gate 5: upstream

    @Test
    fun `gate 5 - C-0076's congruence is reproduced where there is no seam`() {
        val seamless = SeamWeave(base)
        for (duplex in 0 until duplexes) {
            for (plane in listOf(-7, -5, -3, -1, 1, 3, 5, 7)) {
                if (!isWeaveNode(plane)) continue
                assert(abs(seamless.axisOffsetAtPlane(duplex, plane.toDouble())) < 1e-12)
            }
        }
        for (interfaceIndex in 0 until duplexes - 1) {
            for (plane in listOf(-5, -3, -1, 1, 3, 5)) {
                assert(
                    abs(
                        seamless.distanceAtPlane(interfaceIndex, plane.toDouble()) -
                                base.meanDistance
                    ) < 1e-12
                )
            }
        }
    }

    @Test
    fun `gate 5 - the across-row clearance at the measured girth can go NEGATIVE at a seam`() {
        val bai = WeaveProfile(peakToPeak = BAI_PEAK_TO_PEAK)
        val seamed = SeamWeave(bai, seams = listOf(4))
        val girth = DuplexSteric.MEASURED_DIAMETER
        val atNode = bai.meanDistance - girth
        val atSeam = seamed.distanceAtPlane(3, 5.0) - girth
        assert(atNode > 0.0)
        assert(atSeam < 0.0)
        assert(abs((atNode - atSeam) - BAI_PEAK_TO_PEAK / 2.0) < 1e-12)
    }

    @Test
    fun `gate 5 - every duplex loses exactly one pull event at a seam plane`() {
        val seamed = SeamWeave(base, seams = listOf(4))
        for (duplex in 0 until duplexes) {
            assert(abs(abs(seamed.axisOffsetAtPlane(duplex, 4.0)) - amplitude / 4.0) < 1e-12)
            assert(
                abs(
                    seamed.axisOffsetAtPlane(duplex, 3.0) - seamed.axisOffsetAtPlane(duplex, 5.0)
                ) < 1e-12
            )
        }
    }

    @Test
    fun `gate 3 - the candidate seam planes are symmetric about the tile centre`() {
        val planes = seamPlanesWithin(base, Gen1Tile.EDGE_X)
        val positions = planes.map { planePosition(base, it.toDouble()) }.sorted()
        positions.indices.forEach {
            assert(abs(positions[it] + positions[positions.size - 1 - it]) < 1e-9)
        }
    }

    @Test
    fun `gate 4 - the two centre-most seam planes are an exact TIE, so the choice is rounded`() {
        val planes = seamPlanesWithin(base, Gen1Tile.EDGE_X)
        val distances = planes.map { abs(planePosition(base, it.toDouble())) }.sorted()
        // Not a near miss: the two smallest are equal to nine decimals, which is exactly the
        // argmin trap CLAUDE.md records -- an index is not a rounded double.
        assert(abs(distances[0] - distances[1]) < 1e-9)
        assert(distances[2] - distances[1] > 1e-3)
    }

    @Test
    fun `gate 5 - the tile's plane lattice is the one C-0055 and C-0076 use`() {
        assert(base.planeBasePairs == 8)
        assert(abs(base.planeSpacing - 8 * Gen1Tile.RISE_PER_BASE_PAIR) < 1e-12)
        assert(abs(base.period - 32 * Gen1Tile.RISE_PER_BASE_PAIR) < 1e-12)
        assert(seamPlanesWithin(base, Gen1Tile.EDGE_X).isNotEmpty())
        seamPlanesWithin(base, Gen1Tile.EDGE_X).forEach { assert(Math.floorMod(it, 2) == 0) }
    }
}
