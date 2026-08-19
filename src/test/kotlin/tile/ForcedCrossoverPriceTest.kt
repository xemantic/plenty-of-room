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
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.maximumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.minimumTurnPhosphateSpan
import kotlin.math.abs
import kotlin.test.Test

class ForcedCrossoverPriceTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS

    // -------------------------------------------------------------- the azimuth of a departure

    @Test
    fun `an angle folds to the half-open interval that has plus 180 in it`() {
        assert(abs(foldedDegrees(0.0)) < 1e-12)
        assert(abs(foldedDegrees(350.0) + 10.0) < 1e-12)
        assert(abs(foldedDegrees(-350.0) - 10.0) < 1e-12)
        assert(abs(foldedDegrees(180.0) - 180.0) < 1e-12)
        assert(abs(foldedDegrees(-180.0) - 180.0) < 1e-12)
        assert(abs(foldedDegrees(720.0)) < 1e-12)
    }

    @Test
    fun `21 base pairs is exactly two turns, so the residue lattice closes on the azimuth`() {
        assert(abs(HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP * AZIMUTH_PER_BASE_PAIR - 720.0) < 1e-12)
        assert(abs(azimuthalDepartureDegrees(21)) < 1e-12)
        assert(abs(azimuthalDepartureDegrees(0)) < 1e-12)
        assert(abs(azimuthalDepartureDegrees(1) - 240.0 / 7.0) < 1e-12)
    }

    /**
     * `F2`. The smallest nonzero azimuthal departure the lattice offers is **half** a base-pair
     * step, because 10.5 bp is one turn: 10 bp is `−17.14°` and 11 bp is `+17.14°`.
     */
    @Test
    fun `the smallest nonzero azimuthal departure is 120 over 7 degrees, at 10 and at 11 bp`() {
        val minimal = (1 until 21).minOf { abs(azimuthalDepartureDegrees(it)) }
        assert(abs(minimal - 120.0 / 7.0) < 1e-12)
        val achieving = (1 until 21).filter { abs(abs(azimuthalDepartureDegrees(it)) - minimal) < 1e-12 }
        assert(achieving == listOf(10, 11))
        assert(azimuthalDepartureDegrees(10) < 0.0)
        assert(azimuthalDepartureDegrees(11) > 0.0)
    }

    @Test
    fun `every residue departure has a distinct azimuth, so the map is injective`() {
        val azimuths = (0 until 21).map { azimuthalDepartureDegrees(it) }
        assert(azimuths.distinct().size == 21)
    }

    // -------------------------------------------------------------------------- the span, F1

    /**
     * `F1`. The span identity must reproduce `C-0147`'s two endpoints exactly, or the geometry is
     * being read differently from the standing turn-slack claim.
     */
    @Test
    fun `the span reproduces C-0147's two endpoints exactly`() {
        val aligned = forcedCrossoverSpan(d, rP, 0.0)
        val opposed = forcedCrossoverSpan(d, rP, 180.0)
        assert(abs(aligned - minimumTurnPhosphateSpan(d, rP)) / aligned < 1e-12)
        assert(abs(opposed - maximumTurnPhosphateSpan(d, rP)) / opposed < 1e-12)
        assert(abs(aligned - (d - 2.0 * rP)) / aligned < 1e-12)
        assert(abs(opposed - (d + 2.0 * rP)) / opposed < 1e-12)
    }

    @Test
    fun `the span rises monotonically from aligned to opposed`() {
        val spans = (0..180).map { forcedCrossoverSpan(d, rP, it.toDouble()) }
        spans.zipWithNext().forEach { (a, b) -> assert(b > a) }
    }

    @Test
    fun `the span is even in the azimuth, so the sign of a departure cannot matter`() {
        listOf(5.0, 17.142857142857142, 34.285714285714285, 90.0).forEach {
            assert(abs(forcedCrossoverSpan(d, rP, it) - forcedCrossoverSpan(d, rP, -it)) < 1e-12)
        }
    }

    /**
     * `F3`, written the favourable way round: if a forced crossover's span fell inside the
     * measured phosphodiester step there would be nothing to price at all.
     */
    @Test
    fun `a forced crossover does NOT close as a bond at ideal rigid geometry`() {
        val span = forcedCrossoverSpan(d, rP, 120.0 / 7.0)
        assert(span > MeasuredBackbone.STEP_SOUTH_P99)
        assert(span > MeasuredBackbone.STEP_ALL_P99)
        val sigma = (span - MeasuredBackbone.STEP_SOUTH) / MeasuredBackbone.STEP_SOUTH_SD
        assert(sigma > 8.0)
    }

    @Test
    fun `an ALLOWED crossover does close, which is C-0147's own n equals zero check`() {
        val span = forcedCrossoverSpan(d, rP, 0.0)
        assert(span < MeasuredBackbone.STEP_SOUTH_P99)
    }

    /** Bringing the axes together cannot close it either — not even at backbone contact. */
    @Test
    fun `no interhelical approach down to the steric floor closes a forced crossover`() {
        val floor = 2.0 * rP
        val closest = (0..200).minOf {
            forcedCrossoverSpan(floor + (d - floor) * it / 200.0, rP, 120.0 / 7.0)
        }
        assert(closest < MeasuredBackbone.STEP_SOUTH_P99)
        assert(forcedCrossoverSpan(floor, rP, 120.0 / 7.0) < MeasuredBackbone.STEP_SOUTH)
    }

    /**
     * The minimum of the span over **every** separation is `2 r_P |sin θ|`, at `d = 2 r_P cos θ` —
     * a closed form, so *"can the axes close it at all"* needs no search.
     */
    @Test
    fun `the smallest span any approach can reach is two r_P sin theta, exactly`() {
        listOf(5.0, 120.0 / 7.0, 240.0 / 7.0, 60.0).forEach { theta ->
            val exact = smallestReachableSpan(rP, theta)
            val scanned = (0..40000).minOf {
                forcedCrossoverSpan(0.001 + 5.0 * it / 40000.0, rP, theta)
            }
            assert(abs(scanned - exact) / exact < 1e-6)
        }
        assert(abs(smallestReachableSpan(rP, 0.0)) < 1e-12)
        assert(abs(smallestReachableSpan(rP, 90.0) - 2.0 * rP) < 1e-12)
    }

    @Test
    fun `beyond a threshold azimuth no approach whatever closes the bond, and it is a null`() {
        // 2 r_P sin(theta) = step has a root; above it the target is unreachable at every
        // separation, and a root-finder handed such a target must return a VERDICT.
        assert(interhelicalDistanceClosingSpanOrNull(
            MeasuredBackbone.STEP_SOUTH, rP, 240.0 / 7.0
        ) == null)
        assert(interhelicalDistanceClosingSpanOrNull(
            MeasuredBackbone.STEP_SOUTH, rP, 120.0 / 7.0
        ) != null)
        val threshold = smallestReachableSpan(rP, 120.0 / 7.0)
        assert(threshold < MeasuredBackbone.STEP_SOUTH)
        assert(smallestReachableSpan(rP, 240.0 / 7.0) > MeasuredBackbone.STEP_SOUTH_P99)
    }

    @Test
    fun `the approach a forced crossover needs is a root of the span, and it is inside the floor`() {
        val target = MeasuredBackbone.STEP_SOUTH
        val approached = interhelicalDistanceClosingSpan(target, rP, 120.0 / 7.0)
        assert(abs(forcedCrossoverSpan(approached, rP, 120.0 / 7.0) - target) < 1e-9)
        assert(approached < d)
        assert(approached > 2.0 * rP)
    }

    // --------------------------------------------------------------- the departure of a raster

    @Test
    fun `a crossover on an allowed residue carries no departure at all`() {
        assert(abs(minimumAzimuthalDeparture(5, setOf(5, 15))) < 1e-12)
        assert(minimumResidueDeparture(5, setOf(5, 15)) == 0)
    }

    @Test
    fun `the minimum is taken over the allowed set, not over the nearer residue`() {
        // residue 11 against allowed {0, 10}: 1 bp away from 10 (34.29 deg) and 11 bp
        // away from 21 = 0 (17.14 deg). The AZIMUTH decides, so the answer is 17.14.
        assert(abs(abs(minimumAzimuthalDeparture(11, setOf(0, 10))) - 120.0 / 7.0) < 1e-12)
        assert(abs(minimumResidueDeparture(11, setOf(0, 10))) == 10)
    }

    /** `CH-0188`'s published count, reproduced by this file's own census before anything new. */
    @Test
    fun `the 112 over 108 raster forces ten of its fifty-nine crossovers on the ten by six block`() {
        val census = forcedCrossoverCensus(
            HoneycombRasterResidues(
                rasterRows = 10, helicesPerRow = 6, senseOneBasePairs = 112, senseTwoBasePairs = 108
            )
        )
        assert(census.rasterCrossovers == 59)
        assert(census.forcedCrossovers == 10)
        assert(census.azimuthalDeparturesDegrees.size == 10)
    }

    @Test
    fun `every forced crossover of the 112 over 108 raster carries the SAME minimal departure`() {
        val census = forcedCrossoverCensus(
            HoneycombRasterResidues(
                rasterRows = 10, helicesPerRow = 6, senseOneBasePairs = 112, senseTwoBasePairs = 108
            )
        )
        census.azimuthalDeparturesDegrees.forEach {
            assert(abs(abs(it) - 120.0 / 7.0) < 1e-12)
        }
        assert(abs(census.worstAzimuthalDepartureDegrees - 120.0 / 7.0) < 1e-12)
    }

    @Test
    fun `the closing 102 over 109 raster forces nothing and therefore costs nothing`() {
        val census = forcedCrossoverCensus(
            HoneycombRasterResidues(
                rasterRows = 10, helicesPerRow = 6, senseOneBasePairs = 102, senseTwoBasePairs = 109
            )
        )
        assert(census.forcedCrossovers == 0)
        assert(census.azimuthalDeparturesDegrees.isEmpty())
        assert(census.worstAzimuthalDepartureDegrees == 0.0)
    }

    // --------------------------------------------------------------------- the elastic ceiling

    @Test
    fun `the twist-relieved stiffness is a SERIES and therefore below the hinge alone`() {
        val k = Gen1Tile.crossoverHingeStiffness()
        val relieved = twistRelievedHingeStiffness(k, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, 2.38)
        assert(relieved < k)
        assert(relieved > 0.0)
    }

    @Test
    fun `a rigid duplex recovers the hinge exactly, which is the ceiling's own limit`() {
        val k = Gen1Tile.crossoverHingeStiffness()
        val stiff = twistRelievedHingeStiffness(k, 1e12, 2.38)
        assert(abs(stiff - k) / k < 1e-5)
    }

    @Test
    fun `a duplex with no torsional rigidity carries nothing, so the crossover is free`() {
        val k = Gen1Tile.crossoverHingeStiffness()
        assert(twistRelievedHingeStiffness(k, 1e-12, 2.38) < 1e-5 * k)
    }

    @Test
    fun `the energy is quadratic in the departure and vanishes at zero`() {
        val k = Gen1Tile.crossoverHingeStiffness()
        assert(abs(forcedCrossoverEnergy(k, 0.0, 0.0)) < 1e-15)
        val one = forcedCrossoverEnergy(k, 10.0, 0.0)
        val two = forcedCrossoverEnergy(k, 20.0, 0.0)
        assert(abs(two / one - 4.0) < 1e-12)
    }

    @Test
    fun `a baseline the allowed crossover already carries is SUBTRACTED, never added`() {
        val k = Gen1Tile.crossoverHingeStiffness()
        val bare = forcedCrossoverEnergy(k, 25.714285714285715, 0.0)
        val excess = forcedCrossoverEnergy(k, 25.714285714285715, 8.571428571428571)
        assert(excess < bare)
        assert(excess > 0.0)
    }

    @Test
    fun `the ceiling is the rigid-duplex limit, so it bounds the relieved reading`() {
        val k = Gen1Tile.crossoverHingeStiffness()
        val relieved = twistRelievedHingeStiffness(k, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, 2.38)
        assert(
            forcedCrossoverEnergy(relieved, 120.0 / 7.0, 0.0) <
                    forcedCrossoverEnergy(k, 120.0 / 7.0, 0.0)
        )
    }

    /** `F4`. Ten forced crossovers against the host sheet's own standing cost per column. */
    @Test
    fun `ten forced crossovers cost less than one crossover column of the host sheet`() {
        val kMax = Gen1Tile.crossoverHingeStiffness(Gen1Tile.CROSSOVER_ALPHA_MAX)
        val perCrossover = forcedCrossoverEnergy(kMax, 25.714285714285715, 0.0)
        assert(10.0 * perCrossover < HOST_SHEET_COLUMN_ENERGY_KT * 4.142)
    }
}
