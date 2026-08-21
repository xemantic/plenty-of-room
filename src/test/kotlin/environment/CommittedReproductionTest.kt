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

package com.xemantic.nano.plentyofroom.environment

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.brush.ScfDiscretisation
import com.xemantic.nano.plentyofroom.brush.SelfConsistentFieldLayer
import com.xemantic.nano.plentyofroom.brush.desCloizeauxInteraction
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.RESULT_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.SOLVED_HEIGHT_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.roundForResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test

/**
 * `T-265`'s `P2`: the interface is a **re-expression** of what is already committed, and anything
 * else is a finding.
 *
 * Each assertion below takes a number out of a committed result file, re-derives it through the
 * `environment` interface, and requires the departure to be **exactly zero at the file's own
 * emission precision** — nine significant digits for `T-3a` and `T-3b`, six for `T-1d`, which
 * `P-18` set to its solved-height precision.
 *
 * ## The one finding, recorded rather than tolerated
 *
 * [SelfConsistentFieldLayer] warm-starts each self-consistency solve from the field the **previous**
 * solve converged to, so its answer at one height depends on which heights were solved before it.
 * `T-1d`'s `pressureRoutes` calls `pressureAt` on a virgin layer; the `GraftedLayerModel` contract
 * method [SelfConsistentFieldLayer.disjoiningPressure] — which is what an interface honouring the
 * package's own contract must call — first solves the resting height, because that is its validity
 * check. The two therefore differ, reproducibly and deterministically, by ~`2e−13` relative:
 * `0.229620432589 668` against `0.229620432589 618` at 6 nm. Both round to `0.22962`, which is
 * what the committed file carries and is why nothing moves. It is measured here rather than
 * asserted away — see [the two SCF routes differ in the thirteenth digit].
 */
class CommittedReproductionTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun result(name: String) =
        json.parseToJsonElement(File("gpd/results/$name").readText()).jsonObject

    /** Exactly zero departure **at the file's own emission precision**. */
    private fun reproduces(derived: Double, committed: Double, digits: Int): Boolean =
        roundForResult(derived, digits) == committed

    /**
     * Within **one unit in the last emitted place**, which is what a rounding *tie* costs.
     *
     * A committed number can sit exactly on its own rounding boundary — `T-3a`'s force at 5 nm and
     * 2 V is `-938.23249050…`, and the ninth significant digit is decided by accumulation in the
     * fifteenth. Two runs of the same expression on the same inputs then round to two adjacent
     * emitted values with **no difference in the answer at all**. Naming that case is honest;
     * widening the exact test to cover it silently would not be.
     */
    private fun reproducesWithinLastPlace(
        derived: Double,
        committed: Double,
        digits: Int
    ): Boolean {
        val lastPlace = abs(committed) * 10.0.pow(1 - digits)
        return abs(roundForResult(derived, digits) - committed) < 1.5 * lastPlace
    }

    // --- the SCF brush, against T-1d ------------------------------------------------------------

    private val peg = PegWater()

    private fun scfEnvironment() = GraftedLayerEnvironment(
        model = SelfConsistentFieldLayer(
            desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume),
            ScfDiscretisation(nodeSpacing = 0.2, contourStepsPerMonomer = 2.0)
        ),
        chain = peg.graftedChain(250.0, 0.024),
        referenceHeightNm = 10.0
    )

    @Test
    fun `the interface reproduces T-1d's five solved SCF pressures`() {
        val committed = result("T-1d-scf-density-profile.json")["pressureRoutes"]!!.jsonArray
        val environment = scfEnvironment()
        assert(committed.size == 5)
        committed.forEach { entry ->
            val record = entry.jsonObject
            val height = record["layerHeight"]!!.jsonPrimitive.double
            val expected = record["thermodynamicPressure"]!!.jsonPrimitive.double
            val derived = environment.pressure(height)
            assert(reproduces(derived, expected, SOLVED_HEIGHT_SIGNIFICANT_DIGITS)) {
                "T-1d at $height nm: $derived against $expected"
            }
        }
    }

    @Test
    fun `the two SCF routes differ in the thirteenth digit, and the cause is the warm start`() {
        val virgin = scfEnvironment().model as SelfConsistentFieldLayer
        val chain = peg.graftedChain(250.0, 0.024)
        val raw = virgin.pressureAt(chain, 6.0)
        val warmed = scfEnvironment().pressure(6.0)
        val departure = abs(warmed / raw - 1.0)
        assert(departure > 0.0)
        assert(departure < 1e-11)
        // and it is invisible at the precision the file is emitted to, which is why nothing moves
        assert(
            roundForResult(raw, SOLVED_HEIGHT_SIGNIFICANT_DIGITS)
                == roundForResult(warmed, SOLVED_HEIGHT_SIGNIFICANT_DIGITS)
        )
    }

    @Test
    fun `the interface is the package, bit for bit, on the same layer instance`() {
        val environment = scfEnvironment()
        listOf(6.0, 10.0, 16.0).forEach { height ->
            val throughInterface = environment.pressure(height)
            val throughPackage = environment.model.disjoiningPressure(environment.chain, height)
            assert(throughInterface == throughPackage)
        }
    }

    // --- the 1-D electrode gap, against T-3a ------------------------------------------------------

    /**
     * `T-3a`'s own wall charge, **derived** rather than read out of its `runParameters`.
     *
     * The committed parameter block says `-0.398665238`, which is that number rounded at the
     * emission boundary like every other number in the file. Feeding the rounded literal back in
     * is worth one unit in the last emitted place of the answer — see
     * [a committed runParameters value cannot re-run the study it came from].
     */
    private val wallCharge: Double = DnaOrigamiTile().let { tile ->
        -tile.projectedChargeDensity * tile.manningSurvivingFraction(2, bjerrumLength()) / 2.0
    }

    private fun gapEnvironment(gapNm: Double, biasVolts: Double) = ElectrodeGapEnvironment(
        buffer = MagnesiumChlorideBuffer(2.0),
        wallChargeDensity = wallCharge,
        referenceArea = 1600.0,
        referenceGapNm = gapNm,
        referenceBiasVolts = biasVolts
    )

    @Test
    fun `the interface reproduces T-3a's forces at 2 mM over the whole bias ladder`() {
        val committed = result("T-3a-nonlinear-pb-profile.json")["forces"]!!.jsonArray
            .map { it.jsonObject }
            .filter {
                it["concentration"]!!.jsonPrimitive.double == 2.0 &&
                    it["gapHeight"]!!.jsonPrimitive.double == 5.0
            }
        assert(committed.size == 6)
        val environment = gapEnvironment(5.0, 0.0)
        committed.forEach { record ->
            val bias = record["appliedBias"]!!.jsonPrimitive.double
            val expected = record["forceOnTile"]!!.jsonPrimitive.double
            val derived = environment.force(5.0, bias)
            assert(reproduces(derived, expected, RESULT_SIGNIFICANT_DIGITS)) {
                "T-3a at 5 nm, $bias V: $derived against $expected"
            }
        }
    }

    @Test
    fun `a committed runParameters value cannot re-run the study it came from`() {
        // T-3a's parameter block carries the wall charge at the file's own nine significant
        // digits. That is 1.9e-10 relative in the INPUT, and it is worth exactly one unit in the
        // last emitted place of the OUTPUT: the same environment reproduces the 2 V force at 5 nm
        // when it is fed the derived charge and misses it when it is fed the committed literal.
        val rounded = roundForResult(wallCharge, RESULT_SIGNIFICANT_DIGITS)
        assert(rounded != wallCharge)
        assert(abs(rounded / wallCharge - 1.0) < 1e-9)
        val committed = result("T-3a-nonlinear-pb-profile.json")["forces"]!!.jsonArray
            .map { it.jsonObject }
            .first {
                it["concentration"]!!.jsonPrimitive.double == 2.0 &&
                    it["gapHeight"]!!.jsonPrimitive.double == 5.0 &&
                    it["appliedBias"]!!.jsonPrimitive.double == 2.0
            }["forceOnTile"]!!.jsonPrimitive.double
        val fromRounded = ElectrodeGapEnvironment(
            buffer = MagnesiumChlorideBuffer(2.0),
            wallChargeDensity = rounded,
            referenceArea = 1600.0,
            referenceGapNm = 5.0,
            referenceBiasVolts = 0.0
        ).force(5.0, 2.0)
        assert(reproduces(gapEnvironment(5.0, 0.0).force(5.0, 2.0), committed, RESULT_SIGNIFICANT_DIGITS))
        assert(!reproduces(fromRounded, committed, RESULT_SIGNIFICANT_DIGITS))
        assert(reproducesWithinLastPlace(fromRounded, committed, RESULT_SIGNIFICANT_DIGITS))
    }

    @Test
    fun `the interface reproduces T-3a's diffuse-layer potential and bulk Debye length`() {
        val record = result("T-3a-nonlinear-pb-profile.json")["forces"]!!.jsonArray
            .map { it.jsonObject }
            .first {
                it["concentration"]!!.jsonPrimitive.double == 2.0 &&
                    it["gapHeight"]!!.jsonPrimitive.double == 5.0 &&
                    it["appliedBias"]!!.jsonPrimitive.double == 0.25
            }
        val environment = gapEnvironment(5.0, 0.25)
        assert(
            reproduces(
                environment.diffuseLayerPotential(5.0, 0.25),
                record["diffuseLayerPotential"]!!.jsonPrimitive.double,
                RESULT_SIGNIFICANT_DIGITS
            )
        )
        assert(
            reproduces(
                environment.bulkScreeningLength.nanometres,
                record["bulkDebyeLength"]!!.jsonPrimitive.double,
                RESULT_SIGNIFICANT_DIGITS
            )
        )
    }

    @Test
    fun `the interface reproduces T-3a's own force decay length at 3 nm`() {
        val record = result("T-3a-nonlinear-pb-profile.json")["forces"]!!.jsonArray
            .map { it.jsonObject }
            .first {
                it["concentration"]!!.jsonPrimitive.double == 2.0 &&
                    it["gapHeight"]!!.jsonPrimitive.double == 3.0 &&
                    it["appliedBias"]!!.jsonPrimitive.double == 0.0
            }
        val environment = gapEnvironment(3.0, 0.0)
        assert(
            reproduces(
                environment.decayLength.nanometres,
                record["forceDecayLength"]!!.jsonPrimitive.double,
                RESULT_SIGNIFICANT_DIGITS
            )
        ) { "${environment.decayLength.quote()}" }
    }

    // --- the 2-D electrode edge, against T-3b ------------------------------------------------------

    @Test
    fun `the interface reproduces T-3b's refinement-1 centre-line load and taper width`() {
        val record = result("T-3b-tile-edge-load-profile.json")["convergence"]!!.jsonArray
            .map { it.jsonObject }
            .first { it["setting"]!!.jsonPrimitive.content == "refinement 1" }
        val environment = ElectrodeEdgeEnvironment(
            buffer = MagnesiumChlorideBuffer(2.0),
            faceChargeDensity = wallCharge,
            halfWidthNm = 20.0,
            thicknessNm = 10.0,
            referenceGapNm = 10.0,
            referenceBiasVolts = 0.192,
            refinement = 1
        )
        val solution = environment.solve(10.0, 0.192)
        assert(
            reproduces(
                solution.centrelineLoad,
                record["centrelineLoad"]!!.jsonPrimitive.double,
                RESULT_SIGNIFICANT_DIGITS
            )
        ) { "centreline: ${solution.centrelineLoad}" }
        assert(
            reproduces(
                solution.taperFit().equivalentWidth,
                record["taperWidth"]!!.jsonPrimitive.double,
                RESULT_SIGNIFICANT_DIGITS
            )
        ) { "taper width: ${solution.taperFit().equivalentWidth}" }
    }

    @Test
    fun `the edge interface reports the centre-line load with the disjoining sign`() {
        val environment = ElectrodeEdgeEnvironment(
            buffer = MagnesiumChlorideBuffer(2.0),
            faceChargeDensity = wallCharge,
            halfWidthNm = 20.0,
            thicknessNm = 10.0,
            referenceGapNm = 10.0,
            referenceBiasVolts = 0.192,
            refinement = 1
        )
        val solution = environment.solve(10.0, 0.192)
        assert(
            environment.centrelinePressure(10.0, 0.192) == -solution.centrelineLoad
        )
    }

}
