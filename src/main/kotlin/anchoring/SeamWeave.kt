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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.math.floor

/**
 * `T-140`, leaf `A8.2` — the weave with a **scaffold seam** in it.
 *
 * ## Why a second weave model
 *
 * [WeaveProfile] is a closed-form **periodic** triangular wave, and `C-0076`'s congruence — every
 * one of `C-0063`'s 34 upward roots on a node, at all 32 phases, independently of the amplitude —
 * is a property of that periodicity. `C-0076` names its own counter-case and does not price it:
 *
 * > *"In the middle of the plot (around base-pair index 150), a different pattern is evident. This
 * > is due to the presence of the origami's seam (a series of junctions where the scaffold strand
 * > is exchanged), which runs along the middle of the tile. In this region, one group of
 * > double-helix pairs has a particularly large section without any junctions and so opens up to
 * > the largest extent here … By contrast, the other group of double-helix pairs has a shorter
 * > distance between junctions due to the extra scaffold crossovers, and opens up much less."*
 *
 * — Snodin et al., *NAR* **47**:1585 (2019), **read directly** for `T-140`, `PMC6379721`.
 *
 * A periodic wave cannot express that. What can is the representation Snodin's *other* sentence
 * hands over: *"the bending that creates the weave pattern is mostly localized at the junctions
 * with the intervening sections basically straight"*. So a duplex is a **chain of pull events**:
 * at each of its crossover planes it is pulled a quarter-amplitude toward the neighbour it crosses
 * to, and between events it is straight. A seam is then one integer — the plane whose junctions
 * are absent — and everything else follows by arithmetic.
 *
 * ## What the representation buys, and what it costs
 *
 * It reproduces [WeaveProfile] **exactly** with no seams, at every interface and under both
 * edge-duplex readings, which is this task's declared free limiting case and a gate-2 test.
 *
 * It costs one honest approximation: with no junction between planes `s−2` and `s+2` the model
 * makes the duplex perfectly straight there, i.e. it predicts a **plateau**, where Snodin's Figure
 * 3 shows residual modulation inside the opened region *"reflect[ing] the presence of junctions on
 * adjacent pairs of helices"*. The plateau is the extremum, so the model is the **conservative**
 * reading of the departure at a station: a real profile relaxes toward the mean, never past it.
 *
 * ## Conventions
 *
 * Lengths **nm**; `x` along the helices, `y` across them, origin at the tile centre. A plane index
 * `k` is `C-0055`'s 8 bp crossover plane. Duplex `b` crosses to `b+1` at `k ≡ 2b (mod 4)` and to
 * `b−1` at `k ≡ 2b+2 (mod 4)`. A positive axis offset is toward `+y`; a positive interface
 * departure is an **opening**. T = 300 K, aqueous buffer — but see the claim's validity range: the
 * weave itself is measured at `[Na⁺] = 0.5 M` and in vitrified buffer, not at 2 mM MgCl₂.
 */
data class SeamWeave(
    val profile: WeaveProfile = WeaveProfile(),
    val seams: List<Int> = emptyList()
) {

    init {
        seams.forEach {
            require(Math.floorMod(it, 2) == 0) {
                "a seam is a crossover plane and every crossover plane is even, was: $it"
            }
        }
        require(seams.distinct().size == seams.size) {
            "a seam plane may not be listed twice, were: $seams"
        }
        seams.sorted().zipWithNext().forEach { (low, high) ->
            require(high - low >= CROSSOVER_PLANES_PER_PERIOD) {
                "two seams $low and $high are closer than one weave period " +
                        "($CROSSOVER_PLANES_PER_PERIOD planes); a duplex would then lose two " +
                        "consecutive pull events and the straight-section reading is a statement " +
                        "about a span no measurement covers"
            }
        }
    }

    private val seamPlanes: Set<Int> = seams.toSet()

    /** Half the quarter-amplitude a single pull event carries, in nm. */
    val pullDisplacement: Double get() = profile.peakToPeak / 4.0

    /**
     * Duplex [duplex]'s excursion in nm from its ideal lattice position at plane coordinate
     * [plane] — linear interpolation between its surviving pull events.
     */
    fun axisOffsetAtPlane(duplex: Int, plane: Double): Double {
        require(duplex >= 0) { "duplex must not be negative, was: $duplex" }
        if (profile.edgeDuplexesStraight &&
            (duplex == 0 || duplex == profile.duplexes - 1)
        ) {
            return 0.0
        }
        val lower = 2 * floor(plane / 2.0).toInt()
        var before = lower
        while (before in seamPlanes) before -= 2
        var after = if (lower.toDouble() == plane) lower else lower + 2
        while (after in seamPlanes) after += 2
        if (before == after) return pullAt(duplex, before)
        val low = pullAt(duplex, before)
        val high = pullAt(duplex, after)
        return low + (high - low) * (plane - before) / (after - before)
    }

    /** The interhelical distance in nm on interface [interfaceIndex] at plane [plane]. */
    fun distanceAtPlane(interfaceIndex: Int, plane: Double): Double {
        require(interfaceIndex >= 0) {
            "interfaceIndex must not be negative, was: $interfaceIndex"
        }
        return profile.meanDistance +
                axisOffsetAtPlane(interfaceIndex + 1, plane) -
                axisOffsetAtPlane(interfaceIndex, plane)
    }

    /** [distanceAtPlane] at a position [x] in nm along the helices. */
    fun distanceAt(interfaceIndex: Int, x: Double): Double =
        distanceAtPlane(interfaceIndex, profile.planeCoordinate(x))

    /** [axisOffsetAtPlane] at a position [x] in nm along the helices. */
    fun axisOffset(duplex: Int, x: Double): Double =
        axisOffsetAtPlane(duplex, profile.planeCoordinate(x))

    /** Whether [plane] lies inside a seam's straight window, `|plane − s| < 2`. */
    fun insideSeamWindow(plane: Double): Boolean =
        seams.any { abs(plane - it) < CROSSOVER_PLANES_PER_PERIOD / 2.0 }

    /** How many planes [plane] lies from the nearest seam; `null` when there is no seam. */
    fun planesFromNearestSeam(plane: Double): Double? =
        seams.minOfOrNull { abs(plane - it) }

    /**
     * Where duplex [duplex] is pulled at even plane [plane]: `+Δ/4` toward `b+1` at its own
     * crossovers, `−Δ/4` toward `b−1` at the other interface's.
     */
    private fun pullAt(duplex: Int, plane: Int): Double =
        if (Math.floorMod(plane - 2 * duplex, CROSSOVER_PLANES_PER_PERIOD) == 0) {
            pullDisplacement
        } else {
            -pullDisplacement
        }
}

/** The position in nm along the helices of plane index [plane] on [profile]'s lattice. */
fun planePosition(profile: WeaveProfile, plane: Double): Double =
    profile.phaseBasePairs * profile.risePerBasePair + plane * profile.planeSpacing

/**
 * Every plane a seam could sit at on a tile of edge [edgeX] — the **even** planes whose position
 * lies inside the tile. A seam is a design coordinate of the scaffold routing, and this programme
 * has never fixed one, so the deliverable is a sweep over this set rather than a single answer.
 */
fun seamPlanesWithin(
    profile: WeaveProfile = WeaveProfile(),
    edgeX: Double = Gen1Tile.EDGE_X
): List<Int> {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    val half = edgeX / 2.0
    val lowest = floor((-half - profile.phaseBasePairs * profile.risePerBasePair) /
            profile.planeSpacing).toInt() - 2
    val highest = lowest + 2 * (2 + (edgeX / profile.planeSpacing).toInt())
    return (lowest..highest)
        .filter { Math.floorMod(it, 2) == 0 }
        .filter { abs(planePosition(profile, it.toDouble())) <= half }
}
