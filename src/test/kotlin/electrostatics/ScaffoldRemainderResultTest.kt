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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.test.Test

/**
 * `T-195`'s gates 4 and 5, taken on the **emitted** result file rather than on the study's own
 * variables — so that what a reader downloads is what was checked.
 *
 * Tolerances are derived from the file's emission precision (nine significant digits, departures
 * at two), never from what happens to pass.
 */
class ScaffoldRemainderResultTest {

    private val result: JsonObject =
        Json.parseToJsonElement(
            File("gpd/results/T-195-scaffold-remainder.json").readText()
        ).jsonObject

    private fun JsonObject.number(key: String): Double =
        getValue(key).jsonPrimitive.content.toDouble()

    private val parameters = result["parameters"]!!.jsonObject
    private val budgets = result["budgets"]!!.jsonArray.map { it.jsonObject }
    private val saturation = result["saturation"]!!.jsonArray.map { it.jsonObject }
    private val confinement = result["confinement"]!!.jsonArray.map { it.jsonObject }
    private val biasReReads = result["biasReReads"]!!.jsonArray.map { it.jsonObject }
    private val reproductions = result["reproductions"]!!.jsonArray.map { it.jsonObject }
    private val slack = 1e-8

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - every budget conserves the scaffold`() {
        budgets.forEach {
            val scaffold = it.number("scaffoldNucleotides")
            val paired = it.number("pairedNucleotides")
            assert((paired + it.number("remainderNucleotides")).isCloseTo(scaffold, slack))
            assert(it.number("occupancy").isCloseTo(paired / scaffold, slack))
        }
    }

    @Test
    fun `gate 1 - the added charge is a remainder over an area and nothing else`() {
        // e/nm^2 = e / nm^2. A dropped Manning fraction or a dropped halving would show here.
        saturation.forEach {
            assert(
                it.number("addedOverBare")
                    .isCloseTo(it.number("addedCharge") / it.number("bareGapFacingCharge"), slack)
            )
        }
    }

    // ------------------------------------------------------------ gate 2 and 3: limits, order

    @Test
    fun `gate 2 - no effective charge density anywhere reaches its own saturated ceiling`() {
        saturation.forEach {
            assert(it.number("effectiveNominal") < it.number("saturatedCeiling"))
            assert(it.number("effectivePerturbed") < it.number("saturatedCeiling"))
            assert(it.number("effectivePerturbed") > it.number("effectiveNominal"))
        }
    }

    @Test
    fun `gate 3 - adding charge to a saturated wall is strongly sublinear, everywhere`() {
        // The whole cheap bound in one assertion: the relative movement of the effective charge
        // is far smaller than the relative movement of the bare charge that produced it.
        saturation.forEach {
            assert(it.number("effectiveRelativeMovement") < it.number("addedOverBare"))
        }
    }

    @Test
    fun `gate 3 - the penetrating count never exceeds the remainder it is drawn from`() {
        confinement.forEach {
            assert(it.number("penetratingNucleotides") < it.number("remainderNucleotides") + slack)
            assert(it.number("penetratingFraction") < 1.0 + slack)
        }
    }

    // ------------------------------------------------------------------- gate 4: convergence

    @Test
    fun `gate 4 - the load movement settles under mesh refinement`() {
        val records = result["convergence"]!!.jsonArray.map { it.jsonObject }
        assert(records.size > 1)
        val finest = records.last()
        assert(finest.number("relativeDeparture") < 1e-3)
        // and the departure must FALL as the mesh refines, or it is not converging
        assert(finest.number("relativeDeparture") < records.first().number("relativeDeparture"))
    }

    // --------------------------------------------------------- gate 5: upstream reproduction

    @Test
    fun `gate 5 - all of C-0022's one-dimensional loads reproduce`() {
        val loads = reproductions.filter { it["source"]!!.jsonPrimitive.content == "C-0022" }
        assert(loads.size > 20)
        loads.forEach { assert(it.number("relativeDeparture") < 1e-6) }
    }

    @Test
    fun `gate 5 - C-0086's coil and C-0109's budget reproduce`() {
        val upstream = reproductions.filter {
            it["source"]!!.jsonPrimitive.content != "C-0022"
        }
        assert(upstream.size > 4)
        // C-0086 and C-0119 publish their headline numbers to three or four significant digits,
        // so the reproduction cannot be tighter than that rounding.
        upstream.forEach { assert(it.number("relativeDeparture") < 2e-3) }
    }

    // ------------------------------------------------------------------------- the answer

    @Test
    fun `the four-layer tile bounds the remainder where the single-layer sheet does not`() {
        val single = parameters.number("singleLayerWorstEffectiveMovement")
        val recommended = parameters.number("recommendedWorstEffectiveMovement")
        assert(single > 0.5)
        assert(recommended < 0.072)
        assert(parameters.number("exposureReduction").isCloseTo(single / recommended, 1e-6))
    }

    @Test
    fun `the coil is expelled from the gap at every state of the ssDNA bracket`() {
        assert(parameters.number("weakestConfinementFreeEnergy") > 1.0)
        confinement.forEach { assert(it.number("weakerFreeEnergy") > 1.0) }
    }

    @Test
    fun `the collar width carries no surface charge and therefore does not move`() {
        biasReReads.forEach {
            assert(
                abs(
                    it.number("collarWidthCeiling") - it.number("collarWidthCeilingPerturbed")
                ) < 1e-12
            )
        }
    }

    @Test
    fun `every predicate passes and no falsifier fired`() {
        result["predicates"]!!.jsonArray.forEach {
            assert(it.jsonObject["verdict"]!!.jsonPrimitive.content == "PASS")
        }
        result["falsifiers"]!!.jsonArray.forEach {
            assert(it.jsonObject["fired"]!!.jsonPrimitive.content == "false")
        }
    }

    @Test
    fun `no emitted prose carries an unformatted conversion`() {
        // CLAUDE.md: two `settles` strings reached a committed result file carrying raw %.4f.
        // tools/check-kotlin-format-strings.py catches it at the source; this catches it here.
        val text = File("gpd/results/T-195-scaffold-remainder.json").readText()
        assert(!Regex("%[-#+0,(]*[0-9]*(\\.[0-9]+)?[a-zA-Z]").containsMatchIn(text))
    }
}
