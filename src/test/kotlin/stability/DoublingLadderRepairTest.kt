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

package com.xemantic.nano.plentyofroom.stability

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-159` — the repair of `C-0039`'s doubling force ladder, and the re-read of `C-0084`'s 108 fold
 * rows at the corrected element domain.
 *
 * Every test is named for the verification gate it discharges. The spine of the task is that the
 * expensive half was never needed for 90 of the 96 outstanding rows, because
 * `min(layer, element)` is unchanged **identically** when only the element argument rises — and
 * that a repair to a shared main source is not finished until its downstream diff is classified.
 */
class DoublingLadderRepairTest {

    private val elasticaLine = "LQ5"

    private val recommendedDevice = setOf(10.0 to 0.5, 10.0 to 2.0)

    private val upstream = File("gpd/results/T-149-recommended-element-fold.json")

    private val reader = Json { ignoreUnknownKeys = true }

    private fun publishedRows(): List<PublishedFoldRow> =
        reader.parseToJsonElement(upstream.readText()).jsonObject["folds"]!!.jsonArray
            .map { reader.decodeFromJsonElement<PublishedFoldRow>(it) }

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the element domain is a set of lengths, ordered`() {
        val domain = recommendedElementDomain()
        // every field is a stroke in nm on one arm, and the theorem bounds all of them
        assert(domain.contour > 0.0)
        assert(domain.refusalStrokeCeiling < domain.contour)
        assert(domain.branchValidityStrokeCeiling < domain.contour)
        assert(
            domain.pathStrokeCeiling.isCloseTo(
                min(domain.refusalStrokeCeiling, domain.branchValidityStrokeCeiling) -
                        ELEMENT_DOMAIN_SAFETY,
                1e-12
            )
        )
        assert(domain.windowBelowTheContour.isCloseTo(domain.contour - domain.refusalStrokeCeiling))
        assert(domain.refusalOverContour < 1.0 && domain.refusalOverContour > 0.99)
        // and the rotation is a rotation: below a right angle at the ceiling and at §3's stroke
        assert(domain.maximumRotationAtRefusal < PI / 2.0)
        assert(domain.maximumRotationAtAcceptableStroke < domain.maximumRotationAtRefusal)
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { recommendedElementDomain(steps = 4) }
        assertFailsWith<IllegalArgumentException> {
            classifyDomainSensitivity(emptyList(), elasticaLine, 8.0, recommendedDevice)
        }
        assertFailsWith<IllegalArgumentException> {
            classifyDomainSensitivity(publishedRows(), elasticaLine, -1.0, recommendedDevice)
        }
        assertFailsWith<IllegalArgumentException> {
            classifyDomainSensitivity(publishedRows(), elasticaLine, 8.0, recommendedDevice, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            collarCannotCreateAFold(emptyList(), 2.0, 1.9)
        }
        assertFailsWith<IllegalArgumentException> {
            collarCannotCreateAFold(listOf(0.01), -1.0, 1.9)
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - a domain correction of ZERO moves no row at all`() {
        val rows = publishedRows()
        val published = rows.filter { it.strokeCeilingOwner == "element model" }
            .map { it.strokeCeiling }.distinct()
        assert(published.size == 1)
        val classified =
            classifyDomainSensitivity(rows, elasticaLine, published.single(), recommendedDevice)
        assert(classified.none { it.canMove })
        assert(classified.all { it.ceilingMovement <= 1e-9 })
    }

    @Test
    fun `gate 2 limiting cases - a domain correction past every layer ceiling moves the element rows only`() {
        val rows = publishedRows()
        val classified = classifyDomainSensitivity(rows, elasticaLine, 1.0e6, recommendedDevice)
        val movable = classified.filter { it.canMove }
        assert(movable.all { it.sensitivity == DomainSensitivity.ELEMENT_OWNS_THE_MINIMUM.name })
        assert(movable.all { it.loadLine.startsWith(elasticaLine) })
        // and no corrected ceiling escapes its own layer: min() is a min
        assert(classified.all { it.correctedStrokeCeiling <= it.layerStrokeCeiling + 1e-9 })
    }

    @Test
    fun `gate 2 limiting cases - the collar argument fails when a measured gradient is not positive`() {
        val holds = collarCannotCreateAFold(listOf(0.03, 0.02, 0.01), 1.0, 2.0)
        assert(holds.holds)
        assert(holds.nonPositiveGradients == 0)
        val broken = collarCannotCreateAFold(listOf(0.03, -0.001), 1.0, 2.0)
        assert(!broken.holds)
        assert(broken.nonPositiveGradients == 1)
        // and it is conditional where the ceiling's gap is outside the measured range
        val extrapolated = collarCannotCreateAFold(listOf(0.03, 0.02), 2.0, 1.87)
        assert(!extrapolated.holds)
        assert(!extrapolated.gapIsInsideTheMeasuredRange)
        assert(extrapolated.statement.contains("BELOW"))
    }

    // ---------------------------------------------------------------- gate 3 — conservation

    @Test
    fun `gate 3 conservation - the containment is an identity, row by row`() {
        val rows = publishedRows()
        val domain = recommendedElementDomain()
        val classified =
            classifyDomainSensitivity(rows, elasticaLine, domain.pathStrokeCeiling, recommendedDevice)
        assert(classified.size == rows.size)
        classified.zip(rows).forEach { (after, before) ->
            // a corrected ceiling is never below the published one: the correction only RAISES
            // the element argument of a min
            assert(after.correctedStrokeCeiling >= before.strokeCeiling - 1e-12)
            val expected =
                if (before.loadLine.startsWith(elasticaLine))
                    min(before.layerStrokeCeiling, domain.pathStrokeCeiling)
                else before.strokeCeiling
            assert(after.correctedStrokeCeiling.isCloseTo(expected, 1e-12))
        }
    }

    @Test
    fun `gate 3 conservation - the census partitions all 108 rows and only 6 are outstanding`() {
        val rows = publishedRows()
        assert(rows.size == 108)
        val domain = recommendedElementDomain()
        val classified =
            classifyDomainSensitivity(rows, elasticaLine, domain.pathStrokeCeiling, recommendedDevice)
        val byKind = classified.groupingBy { it.sensitivity }.eachCount()
        // the partition is exhaustive and it is the one the cheap bound predicts
        assert(byKind.values.sum() == 108)
        assert(byKind[DomainSensitivity.NO_ELEMENT.name] == 54)
        assert(byKind[DomainSensitivity.LAYER_OWNS_THE_MINIMUM.name] == 36)
        assert(byKind[DomainSensitivity.ELEMENT_OWNS_THE_MINIMUM.name] == 18)
        // 18 can move, C-0092 re-read 12 of them, so 6 of the 96 outstanding rows are the whole
        // of what this task's expensive half is owed
        assert(classified.count { it.canMove } == 18)
        assert(classified.count { it.canMove && it.reReadByC0092 } == 12)
        assert(classified.count { it.canMove && !it.reReadByC0092 } == 6)
        assert(classified.filter { it.canMove && !it.reReadByC0092 }
            .all { it.layerHeight == 10.0 && it.concentration == 10.0 })
    }

    @Test
    fun `gate 3 conservation - a repricing preserves every published reading beside the corrected one`() {
        val rows = publishedRows()
        val identity = rows.associateBy { it.key }
        val repriced = repriceCeilings(rows, identity, "element model branch end")
        assert(repriced.size == rows.size)
        // substituting a table into ITSELF moves nothing — C-0084's own gate-2 discipline
        assert(repriced.all { it.marginMovement == null || it.marginMovement.isCloseTo(1.0, 1e-12) })
        assert(repriced.all { it.publishedBindingCeiling == it.correctedBindingCeiling })
        // The inflation `CH-0099` priced was carried by the 8 rows the element boundary bound at.
        // `C-0096`'s repair removes the boundary and `C-0101` re-emitted the file, so there is no
        // longer an inflated row to recover — which is the repair's whole point, asserted rather
        // than dropped. Every row that WAS inflated sat at the 10 nm layer, and none is left.
        val inflated = repriced.filter { (it.publishedInflation ?: 1.0) > 2.0 }
        assert(inflated.isEmpty())
        assert(inflated.all { it.layerHeight == 10.0 })
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the corrected domain is set by its BISECTION, not by the integrator`() {
        val coarse = recommendedElementDomain(steps = 200)
        val design = recommendedElementDomain(steps = 400)
        val fine = recommendedElementDomain(steps = 800)
        listOf(coarse, design, fine).forEach {
            // the theorem, at every resolution: delta < L strictly, on every branch (C-0092)
            assert(it.refusalStrokeCeiling < it.contour)
            assert(it.branchValidityStrokeCeiling < it.contour)
            // and the repair, at every resolution: past the doubling ladder's 7.9196867 nm
            assert(it.refusalStrokeCeiling > 7.9196867)
            assert(it.maximumRotationAtRefusal < PI / 2.0)
        }
        // the contour is a property of the arm, not of the integrator, to nine digits
        assert(coarse.contour.isCloseTo(fine.contour, 1e-9))
        // and NEITHER is the located domain, over 200 -> 800 RK4 steps: it agrees to well inside
        // loadLineStrokeCeiling's own 1e-6 nm bracket, so what sets it is the bisection and the
        // continuation's force-step budget, not the discretisation. Asserting that it MOVES
        // would be asserting on sub-1e-9 noise.
        assert(abs(coarse.refusalStrokeCeiling - fine.refusalStrokeCeiling) < 1e-6)
        assert(abs(design.refusalStrokeCeiling - fine.refusalStrokeCeiling) < 1e-6)
        // the rotation AT it does move, and by more, because it is a derived quantity read at a
        // located point (CLAUDE.md: a gradient converges more slowly than what it differences)
        assert(
            abs(coarse.maximumRotationAtRefusal - fine.maximumRotationAtRefusal) >
                    abs(coarse.refusalStrokeCeiling - fine.refusalStrokeCeiling)
        )
    }

    @Test
    fun `gate 4 convergence - the movement classification agrees with the python tool's thresholds`() {
        // tools/T-159-result-diff.py classifies the downstream diff and its output is read by the
        // study, so the two vocabularies have to be one vocabulary
        assert(classifyMovement(1.0, 1.0) == "identical")
        assert(classifyMovement(0.0, 1e-13) == "a quantity that is identically zero")
        assert(
            classifyMovement(40.4465365, 40.4465366) ==
                    "one unit in the last emitted significant digit"
        )
        assert(classifyMovement(1.0, 1.0 + 1e-6) == "inside a declared solver tolerance")
        assert(classifyMovement(7.90968584, 8.13040721) == "a real change")
        assert(
            classifyMovement(2.3078169e-9, 2.25412212e-9) ==
                    "a residual of a quantity that vanishes by construction"
        )
        assert(EMITTED_FIELD_MOVEMENT == 1.0e-8)
        assert(VANISHING_FIELD_MOVEMENT == 1.0e-9)
        // a number emitted as a string is not rounded, so a prose field can move while saying
        // exactly the same thing — and that is not a moved decision
        assert(
            classifyTextMovement("a margin of 0.6756091733686969 nm", "a margin of 0.6756091733686986 nm") ==
                    "a number carried inside an unrounded string"
        )
        assert(
            classifyTextMovement("the arm folds before reaching it", "places, past the ceiling") ==
                    "a decision"
        )
        assert(classifyTextMovement("same", "same") == "identical")
    }

    // ---------------------------------------------------------------- gate 5 — upstream

    @Test
    fun `gate 5 upstream - T-149's own safety constant and arm length are reproduced, not asserted`() {
        val parameters =
            reader.parseToJsonElement(upstream.readText()).jsonObject["runParameters"]!!.jsonObject
        assert(
            parameters["elementCeilingSafety"]!!.jsonPrimitive.content.toDouble()
                .isCloseTo(ELEMENT_DOMAIN_SAFETY, 1e-12)
        )
        val publishedArm = parameters["armLength"]!!.jsonPrimitive.content.toDouble()
        val domain = recommendedElementDomain()
        // C-0069's Q5 contour, re-derived through C-0039's own placement solve on the REPAIRED
        // solver: the placement is at a 3 nm stroke where the residual has one root, so the
        // repair must not move it beyond the last ulp
        assert(domain.contour.isCloseTo(publishedArm, 1e-14))
        assert(domain.contour.isCloseTo(8.16439083, 1e-9))
    }

    @Test
    fun `gate 5 upstream - the corrected domain lies between C-0084's ladder and C-0092's contour`() {
        val domain = recommendedElementDomain()
        // C-0084's published refusal and path ceiling — CITED
        assert(domain.refusalStrokeCeiling > 7.91968584)
        assert(domain.pathStrokeCeiling > 7.909685836937754)
        // C-0092's contour bound — a theorem, and the one thing no solver can move
        assert(domain.refusalStrokeCeiling < 8.164390826631303)
        // C-0092 continued to 8.1610821 nm on an INDEPENDENT integrator at 800 RK4 steps; this
        // one at its own 400 is shorter, and both are the same side of the contour
        assert(domain.refusalStrokeCeiling < 8.1610821)
    }

    @Test
    fun `gate 5 upstream - C-0084's element-boundary census is reproduced from its own file`() {
        val rows = publishedRows()
        // `CH-0099` priced the element boundary as the binding ceiling at 8 of 108 states — and
        // that was read on the DOUBLING LADDER's premature refusal. `C-0096`'s repair removes it
        // entirely, and `C-0101` re-emitted `T-149` so the file agrees with the code that makes
        // it, so the census reproduced here is now the repaired one: the ceiling binds NOWHERE,
        // and the 8 states it used to own are back with the layer and the field.
        assert(rows.count { it.bindingCeiling.startsWith("element model branch end") } == 0)
        // C-0084: at 18 of 108 the branch still ENDS on the element model — the branch end is a
        // real property of an inextensible arm; what it stopped being is a binding CEILING.
        assert(rows.count { it.strokeCeilingOwner == "element model" } == 18)
        assert(
            rows.count {
                it.strokeCeilingOwner == "element model" && it.layerHeight == 10.0 &&
                        it.concentration != 10.0
            } == 12
        )
    }
}
