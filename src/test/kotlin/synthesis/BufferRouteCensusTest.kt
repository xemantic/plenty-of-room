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

package com.xemantic.nano.plentyofroom.synthesis

import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Task `T-156` — the gates of the 0.5 mM route census (claim `C-0091`).
 *
 * Gate 5 is the one this task exists for: **every figure the claim quotes is re-derived from the
 * emitting study's own result file**, never transcribed from a claim's prose.
 */
class BufferRouteCensusTest {

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - a buffer advantage is dimensionless and is a ratio of two readings of ONE quantity`() {
        // bias needed for a target: SMALLER is better, so the advantage is high/low
        assert(
            bufferAdvantage(
                lowSaltReading = 0.140845447,
                highSaltReading = 0.699378112,
                smallerIsBetter = true
            ).isCloseTo(0.699378112 / 0.140845447, 1e-15)
        )
        // a stability margin: LARGER is better, so the advantage is low/high
        assert(
            bufferAdvantage(
                lowSaltReading = 8.64521213,
                highSaltReading = 1.42361942,
                smallerIsBetter = false
            ).isCloseTo(8.64521213 / 1.42361942, 1e-15)
        )
    }

    @Test
    fun `gate 1 - unphysical entry points throw rather than returning a number`() {
        assertFailsWith<IllegalArgumentException> { bufferAdvantage(0.0, 1.0, true) }
        assertFailsWith<IllegalArgumentException> { bufferAdvantage(1.0, -1.0, false) }
        assertFailsWith<IllegalArgumentException> { marginOnTangent(1.0, 0.0, 30.0) }
        assertFailsWith<IllegalArgumentException> { marginOnTangent(1.0, 33.3, -1.0) }
        assertFailsWith<IllegalArgumentException> { marginOnTangent(-1.0, 33.3, 30.0) }
        assertFailsWith<IllegalArgumentException> { isTransfer(1.0, 1.0, 0.0) }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - rescaling a mandate margin onto the mandate itself is the identity`() {
        val margin = 1.19417694
        assert(marginOnTangent(margin, 100.0 / 3.0, 100.0 / 3.0).isCloseTo(margin, 1e-15))
    }

    @Test
    fun `gate 2 - a route compared against itself is a transfer at departure exactly zero`() {
        assertTrue(isTransfer(0.699378112, 0.699378112, EMITTED_FIELD_SLACK))
        assertEquals(0.0, transferDeparture(0.699378112, 0.699378112))
        // two absent readings agree; one absent reading does not
        assertTrue(isTransfer(null, null, EMITTED_FIELD_SLACK))
        assertFalse(isTransfer(null, 1.0, EMITTED_FIELD_SLACK))
        assertNull(transferDeparture(null, 1.0))
    }

    @Test
    fun `gate 2 - the buffer advantage of a quantity that does not move is exactly one`() {
        assert(bufferAdvantage(0.25, 0.25, true).isCloseTo(1.0, 1e-15))
        assert(bufferAdvantage(0.25, 0.25, false).isCloseTo(1.0, 1e-15))
    }

    // ------------------------------------------------------------------ gate 3 — the partition

    @Test
    fun `gate 3 - the census is a PARTITION of the six named routes`() {
        val census = gen1BufferRouteCensus()
        assertEquals(6, census.size)
        assertEquals(
            listOf("C-0012", "C-0016", "C-0017", "C-0018", "C-0027", "C-0032"),
            census.map { it.claim }.sorted()
        )
        // every route lands in exactly one verdict class, and the classes cover all six
        assertEquals(6, census.count { it.verdict in RouteVerdict.entries })
        assertEquals(
            census.size,
            census.count { it.verdict == RouteVerdict.WITHDRAWN } +
                    census.count { it.verdict == RouteVerdict.SURVIVES_SAME_GROUND } +
                    census.count { it.verdict == RouteVerdict.SURVIVES_DIFFERENT_GROUND }
        )
        // and exactly one withdrawal, which is C-0032's — CH-0098
        assertEquals(
            listOf("C-0032"),
            census.filter { it.verdict == RouteVerdict.WITHDRAWN }.map { it.claim }
        )
    }

    @Test
    fun `gate 3 - independence is a relation and a transfer names the route it transfers FROM`() {
        val census = gen1BufferRouteCensus()
        val transfers = census.filter { it.independence == RouteIndependence.TRANSFER }
        assertTrue(transfers.isNotEmpty(), "the cheap bound found no transfer at all")
        transfers.forEach {
            assertTrue(it.transferOf.isNotEmpty(), "${it.claim} is a transfer of nothing")
            assertTrue(
                it.transferOf.all { source -> census.any { r -> r.claim == source } },
                "${it.claim} transfers from a route outside the census: ${it.transferOf}"
            )
            assertFalse(it.transferOf.contains(it.claim), "${it.claim} transfers from itself")
        }
        // a route nobody transfers from and that transfers from nobody is independent
        census.filter { it.independence == RouteIndependence.INDEPENDENT }.forEach {
            assertTrue(it.transferOf.isEmpty())
        }
    }

    // ------------------------------------------------------------- gate 4 — decision precision

    @Test
    fun `gate 4 - a transfer verdict is taken at the EMISSION precision and never at equality`() {
        // T-16 emits nine significant digits, T-25 eight: the same number, printed twice
        val inSixteen = 2.09105359
        val inTwentyFive = 2.0910536
        assertFalse(inSixteen == inTwentyFive, "the two files print the same number identically")
        assertTrue(isTransfer(inSixteen, inTwentyFive, EMITTED_FIELD_SLACK))
        assertFalse(isTransfer(inSixteen, inTwentyFive, 1e-12))
        // and a genuinely different number is not a transfer at any sane tolerance
        assertFalse(isTransfer(2.09105359, 2.16304201, EMITTED_FIELD_SLACK))
    }

    @Test
    fun `gate 4 - the slack is DERIVED from the emission precision, not chosen to pass`() {
        // nine significant digits leaves 5e-9 of relative slack in one field; the census compares
        // two, so the slack must be at least twice that and is not allowed to be large
        assertTrue(EMITTED_FIELD_SLACK >= 1e-8)
        assertTrue(EMITTED_FIELD_SLACK <= 1e-5)
    }

    // ------------------------------------------------- gate 5 — provenance: the numbers are READ

    @Test
    fun `gate 5 - C-0012's force clause is read out of T-3 and is model-free`() {
        val readings = blockingBiasAtTenNanometres()
        // 5 and 10 mM never reach 100 pN at 10 nm below 2 V, so they carry no threshold at all
        assertEquals(setOf(0.5, 1.0, 2.0), readings.keys.map { it.second }.toSet())
        // the clause is exactly sigma-free and model-free: one value per buffer over six models
        listOf(0.5, 2.0).forEach { buffer ->
            val values = readings.filterKeys { it.second == buffer }.values.toSet()
            assertEquals(1, values.size, "the blocking bias is not model-free at $buffer mM")
        }
    }

    @Test
    fun `gate 5 - C-0016's bias window is C-0012's OWN number, at 15 of 15 states`() {
        val census = gen1BufferRouteCensus()
        val route = census.single { it.claim == "C-0016" }
        assertEquals(RouteIndependence.TRANSFER, route.independence)
        assertEquals(listOf("C-0012"), route.transferOf)
        val checks = blockingBiasTransferChecks()
        assertEquals(15, checks.size)
        assertTrue(
            checks.all { it.transfer },
            "not a transfer at: " + checks.filterNot { it.transfer }.map { it.state }
        )
    }

    @Test
    fun `gate 5 - C-0027's two halves are C-0017's and C-0018's own numbers`() {
        val checks = correctedMarginTransferChecks()
        assertTrue(checks.isNotEmpty())
        assertTrue(
            checks.all { it.transfer },
            "not a transfer at: " + checks.filterNot { it.transfer }.map { it.state }
        )
        val route = gen1BufferRouteCensus().single { it.claim == "C-0027" }
        assertEquals(RouteIndependence.TRANSFER, route.independence)
        assertEquals(listOf("C-0017", "C-0018"), route.transferOf.sorted())
    }

    @Test
    fun `gate 5 - the recommended element's tangent minimum is read from T-149, not asserted`() {
        val tangent = recommendedTangentMinimum()
        // C-0069's Q5 row, re-derived there from C-0039's library
        assert(tangent.isCloseTo(30.028762, EMITTED_FIELD_SLACK))
        // and it is BELOW the mandated secant, which is why the margin moves at all
        assertTrue(tangent < GEN1_MANDATED_SECANT)
    }

    @Test
    fun `gate 5 - the stability floor is element-independent and the margin is not`() {
        val floors = stabilityFloorsAtTenNanometres()
        val lowSalt = floors.filterKeys { it.second == 0.5 }.values
        val highSalt = floors.filterKeys { it.second == 2.0 }.values
        assertEquals(6, lowSalt.size)
        assertEquals(6, highSalt.size)
        // the floor at 0.5 mM is below the floor at 2 mM at every one of the six models
        assertTrue(lowSalt.max() < highSalt.min())
        // and rescaling C-0017's margin onto Q5's tangent moves every one of them DOWN
        floors.forEach { (_, floor) ->
            val mandate = GEN1_MANDATED_SECANT / floor
            val onQ5 = marginOnTangent(mandate, GEN1_MANDATED_SECANT, recommendedTangentMinimum())
            assertTrue(onQ5 < mandate)
            assert(onQ5.isCloseTo(recommendedTangentMinimum() / floor, 1e-12))
        }
    }

    @Test
    fun `gate 5 - the force clause read at the DEVICE's own operating point is 3x weaker`() {
        val zeroStroke = gen1BufferRouteCensus().single { it.claim == "C-0012" }
        val held = heldOperatingBiasAdvantageAtTenNanometres()
        assertEquals(6, held.size)
        // every per-model held advantage is well below the zero-stroke one
        assertTrue(held.values.max() < zeroStroke.advantage!!)
        assertTrue(zeroStroke.advantage!! / held.values.max() > 3.0)
    }

    // ------------------------------------------------------- the falsifiers, as executable tests

    @Test
    fun `falsifier F1 - no surviving route points at 2 mM`() {
        val census = gen1BufferRouteCensus()
        census.filter { it.verdict != RouteVerdict.WITHDRAWN }.forEach {
            assertTrue(
                (it.advantage ?: 1.0) > 1.0,
                "${it.claim} does not favour 0.5 mM: advantage ${it.advantage}"
            )
        }
    }

    @Test
    fun `falsifier F2 - the six are NOT six, and the cheap bound is what says so`() {
        val census = gen1BufferRouteCensus()
        val independentSurvivors = census.count {
            it.verdict != RouteVerdict.WITHDRAWN &&
                    it.independence == RouteIndependence.INDEPENDENT
        }
        assertTrue(independentSurvivors < 6, "F2 did not fire: the six really are six")
        assertEquals(3, independentSurvivors)
    }
}
