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
 * `T-160`'s five verification gates, taken on the **emitted** result file rather than on the
 * study's own variables — so that what a reader downloads is what was checked.
 *
 * Every tolerance here is derived from the file's **emission** precision, not from what happens
 * to pass: `gpd/results/T-160-edge-width-dependence.json` is rounded at nine significant digits
 * with the departure keys at six, so an identity reconstructed from three emitted fields cannot
 * be asserted tighter than a few times [T160_EMITTED_FIELD_SLACK] (`CLAUDE.md`).
 */
class EdgeWidthDependenceResultTest {

    private val result: JsonObject =
        Json.parseToJsonElement(
            File("gpd/results/T-160-edge-width-dependence.json").readText()
        ).jsonObject

    private val collars = (result["collars"]!!.jsonArray + result["widthSweep"]!!.jsonArray)
        .map { it.jsonObject }

    private fun JsonObject.number(key: String): Double =
        getValue(key).jsonPrimitive.content.toDouble()

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - the effective collar width is a LENGTH, the deficit over the interior load`() {
        // pN/nm per pN/nm^2 is nm. A dropped k_BT or a dropped area would show here as a factor.
        collars.forEach {
            assert(
                it.number("effectiveCollarWidth").isCloseTo(
                    -it.number("totalDeficitPerUnitEdge") / it.number("centrelineLoad"),
                    T160_EMITTED_FIELD_SLACK
                )
            )
        }
    }

    @Test
    fun `gate 1 - the rim residual DEPTH is its line load over the interior load and a standoff`() {
        val standoff = result["parameters"]!!.jsonObject.number("rimStandoff")
        collars.forEach {
            assert(
                it.number("rimResidualDepth").isCloseTo(
                    2.0 * it.number("rimResidualPerUnitEdge") /
                            (it.number("centrelineLoad") * standoff),
                    T160_EMITTED_FIELD_SLACK
                )
            )
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - every solve is numerically resolved and every centre-line is the 1-D answer`() {
        collars.forEach {
            assert(it.getValue("numericallyResolved").jsonPrimitive.content == "true")
            // C-0022's own falsifier 1: the 2-D centre-line IS T-3a's 1-D disjoining pressure,
            // through a solver sharing only the ion model. It held there and it holds here.
            assert(abs(it.number("centrelineOverOneDimensional") - 1.0) < 0.005)
        }
    }

    @Test
    fun `gate 2 - the buildable half-width is exactly half C-0086's 112 base pairs`() {
        val parameters = result["parameters"]!!.jsonObject
        assert(parameters.number("buildableWidth").isCloseTo(112 * 0.34, 1e-12))
        assert(
            parameters.number("buildableHalfWidth")
                .isCloseTo(0.5 * parameters.number("buildableWidth"), 1e-12)
        )
    }

    // ---------------------------------------------------- gate 3: conservation — the SUM is safe

    @Test
    fun `gate 3 - the two collar terms sum to the GLOBAL deficit, at every solve`() {
        // This is the identity the whole verdict rests on: the mesh moves load BETWEEN the
        // smooth taper and the rim residual and creates none, because their sum is the momentum
        // flux through one plane, which owes nothing to the standoff or to the corner.
        collars.forEach {
            assert(
                it.number("totalDeficitPerUnitEdge").isCloseTo(
                    it.number("taperLoadDeficit") + it.number("rimResidualPerUnitEdge"),
                    T160_EMITTED_FIELD_SLACK
                )
            )
        }
    }

    @Test
    fun `gate 3 - the fit-free collar departure is inside C-0090's placement sensitivity`() {
        val sensitivity = result["parameters"]!!.jsonObject.number("placementSensitivity")
        val fitFree = result["departures"]!!.jsonArray.map { it.jsonObject }
            .filter { it.getValue("quantity").jsonPrimitive.content == "effectiveCollarWidth" }
        assert(fitFree.isNotEmpty())
        fitFree.forEach {
            assert(it.number("relativeDeparture") < sensitivity)
            assert(it.getValue("insidePlacementSensitivity").jsonPrimitive.content == "true")
        }
    }

    // ------------------------------------------------------------- gate 4: numerical convergence

    @Test
    fun `gate 4 - the FIT-FREE departure converges and the FITTED one does not`() {
        val records = result["convergence"]!!.jsonArray.map { it.jsonObject }
        val fitFree = records.single {
            it.getValue("quantity").jsonPrimitive.content.startsWith("the FIT-FREE")
        }
        val fitted = records.single {
            it.getValue("quantity").jsonPrimitive.content
                .startsWith("the worst collar departure")
        }
        val fitFreeValues = fitFree.getValue("results").jsonArray.map {
            it.jsonPrimitive.content.toDouble()
        }
        val fittedValues = fitted.getValue("results").jsonArray.map {
            it.jsonPrimitive.content.toDouble()
        }
        assert(fitFreeValues.size == 4)
        assert(fittedValues.size == 4)
        // The fit-free departure's whole scatter over refinements 1/2/3/4 is smaller than the
        // departure itself; the fitted triple's is LARGER, which is what "not converged" means.
        assert(fitFreeValues.max() - fitFreeValues.min() < fitFreeValues[2])
        assert(fittedValues.max() - fittedValues.min() > fittedValues[2])
    }

    @Test
    fun `gate 4 - the standoff snaps to a different node at the two half-widths`() {
        // The mechanism, asserted rather than asserted-about: this is why the fitted triple moves.
        val standoff = result["parameters"]!!.jsonObject.number("rimStandoff")
        val design = collars.filter {
            it.getValue("state").jsonPrimitive.content.startsWith("design point") &&
                    it.number("refinement") == 3.0
        }
        val nominal = design.single { it.number("tileHalfWidth") == 20.0 }
        val buildable = design.single { it.number("tileHalfWidth") == 19.04 }
        assert(nominal.number("standoffNode") >= standoff)
        assert(buildable.number("standoffNode") >= standoff)
        assert(abs(nominal.number("standoffNode") - buildable.number("standoffNode")) > 1e-6)
    }

    // ------------------------------------------------------- gate 5: the upstream reproductions

    @Test
    fun `gate 5 - every upstream number is reproduced, C-0022's collar and C-0090's flatness`() {
        val reproductions = result["reproductions"]!!.jsonArray.map { it.jsonObject }
        assert(reproductions.size >= 7)
        reproductions.forEach { assert(it.number("departure") < 1e-6) }
        assert(reproductions.any { it.getValue("source").jsonPrimitive.content == "C-0090" })
        assert(reproductions.count { it.getValue("source").jsonPrimitive.content == "C-0022" } >= 6)
    }

    @Test
    fun `gate 5 - C-0090's flatness verdict survives the re-solved collar`() {
        val sensitivity = result["parameters"]!!.jsonObject.number("placementSensitivity")
        val flatness = result["flatness"]!!.jsonArray.map { it.jsonObject }
        assert(flatness.size == 4)
        flatness.forEach {
            assert(it.getValue("flatAtTenPercent").jsonPrimitive.content == "true")
            assert(it.number("stations") == 34.0)
        }
        // The two WIDTH comparisons — carried against re-solved, in both partitions — are inside
        // C-0090's declared sensitivity. Those are rows 1 and 3, each read against the row above.
        listOf(1, 3).forEach {
            assert(flatness[it].number("movementAgainstCarried") < sensitivity)
            assert(
                flatness[it].getValue("insidePlacementSensitivity").jsonPrimitive.content == "true"
            )
        }
        // Row 2 is not a width comparison at all: it is the SAME 40 nm field re-partitioned by
        // placing the standoff exactly. That it lands OUTSIDE the sensitivity — further than the
        // tile's own width does — is this task's finding, so it is asserted rather than tolerated.
        assert(flatness[2].number("movementAgainstCarried") > sensitivity)
        assert(
            flatness[2].number("movementAgainstCarried") >
                    flatness[1].number("movementAgainstCarried")
        )
        assert(
            flatness[2].getValue("insidePlacementSensitivity").jsonPrimitive.content == "false"
        )
    }
}

/**
 * The slack an identity reconstructed from **emitted** fields is owed.
 *
 * `gpd/results/T-160-edge-width-dependence.json` rounds at nine significant digits, so a single
 * field carries `5e-10` relative and an identity over three of them a few times that. Asserting
 * tighter than this would be a test of the printed digits rather than of the physics
 * (`CLAUDE.md`); asserting looser would let a real movement through.
 */
private const val T160_EMITTED_FIELD_SLACK: Double = 1.0e-8
