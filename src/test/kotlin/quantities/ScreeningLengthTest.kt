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

package com.xemantic.nano.plentyofroom.quantities

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The one quantity `CLAUDE.md` names three separate correct values for, given a type.
 *
 * *"'The Debye length' is three different numbers in this project, and all three are correct in
 * their own place — 3.93 nm in the bulk buffer at 2 mM MgCl₂; 0.84–1.18 nm in the tile-electrode
 * gap, which is counterion-dominated 3–33× so its ion content is set by the tile's charge and not
 * by the buffer; and 4.5–5.5 nm inside the PEG layer, which excludes 23–48 % of the salt.
 * Substituting one for another is `CH-0004`."*
 *
 * A [ScreeningLength] cannot be built without saying **where** it was read and on **which axis**,
 * and [ratioOf] then refuses the substitution outright. The escape — a ratio that is legitimate
 * once it is quoted with the two states it spans, which is what `T-3a`'s own `decayOverBulkDebye`
 * is — is [statedRatio], and it renders both.
 */
class ScreeningLengthTest {

    private val bulk = ScreeningLength(
        3.92687853, ScreeningLength.BULK_RESERVOIR,
        readAt = mapOf("concentrationMillimolar" to "2.0")
    )

    private val inTheGap = ScreeningLength(
        0.520486334, ScreeningLength.CONFINED_GAP,
        readAt = mapOf("concentrationMillimolar" to "2.0", "gapNm" to "3.0", "biasVolts" to "0.0")
    )

    // --- gate 1: the state cannot be omitted ---------------------------------------------------

    @Test
    fun `a screening length quotes where and on which axis it was read`() {
        val quoted = bulk.quote()
        assert(quoted.contains("nm"))
        assert(quoted.contains(ScreeningLength.BULK_RESERVOIR))
        assert(quoted.contains(ScreeningLength.NORMAL))
        assert(quoted.contains("concentrationMillimolar = 2.0"))
    }

    @Test
    fun `a screening length is a length in nanometres of one kind`() {
        assert(bulk.unit == "nm")
        assert(bulk.kind == "screening length")
        assert(bulk.value == bulk.nanometres)
    }

    @Test
    fun `a non-positive screening length is refused`() {
        assertFailsWith<IllegalArgumentException> {
            ScreeningLength(0.0, ScreeningLength.BULK_RESERVOIR)
        }
        assertFailsWith<IllegalArgumentException> {
            ScreeningLength(1.0, "  ")
        }
    }

    // --- gate 2: CH-0004, made unrepresentable --------------------------------------------------

    @Test
    fun `a ratio between the bulk length and the confined one is refused`() {
        val failure = assertFailsWith<IllegalArgumentException> { ratioOf(inTheGap, bulk) }
        assert(failure.message!!.contains("where"))
    }

    @Test
    fun `a ratio between two lengths on different axes is refused`() {
        val lateral = ScreeningLength(
            1.76027566, ScreeningLength.CONFINED_GAP, axis = ScreeningLength.LATERAL,
            readAt = inTheGap.readAt
        )
        assertFailsWith<IllegalArgumentException> { ratioOf(lateral, inTheGap) }
    }

    @Test
    fun `a ratio between two lengths read at one state is a plain number`() {
        val doubled = inTheGap.scaledBy(2.0)
        assert(ratioOf(doubled, inTheGap).isCloseTo(2.0))
    }

    // --- gate 3: the escape the StatedQuantity KDoc names and did not provide --------------------

    @Test
    fun `a ratio across two states carries both of them`() {
        val ratio = statedRatio(inTheGap, bulk)
        assert(ratio.value.isCloseTo(0.520486334 / 3.92687853))
        assert(ratio.numeratorState.contains(ScreeningLength.CONFINED_GAP))
        assert(ratio.denominatorState.contains(ScreeningLength.BULK_RESERVOIR))
        assert(ratio.quote().contains("against"))
    }

    @Test
    fun `a ratio across two KINDS is refused even when it is quoted with both states`() {
        val notALength = statedQuantity(1.0, "pN", "force", "gapNm" to "3.0")
        assertFailsWith<IllegalArgumentException> { statedRatio(inTheGap, notALength) }
    }

    // --- gate 4: scaling does not move the state ------------------------------------------------

    @Test
    fun `scaling a screening length leaves its state untouched`() {
        val scaled = bulk.scaledBy(0.5)
        assert(scaled.state == bulk.state)
        assert(scaled.nanometres.isCloseTo(0.5 * bulk.nanometres))
    }

}
