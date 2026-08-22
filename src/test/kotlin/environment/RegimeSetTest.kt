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
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-286` / [`CH-0224`](../../../../../../../gpd/challenges/CH-0224-a-regime-cannot-name-a-swept-buffer.md):
 * a [Regime] describes a **solve** and a result file is a **bag of solves**.
 *
 * A study that sweeps the buffer does not widen one regime — the molarity is a *constructor*
 * argument of the environment, where the height and the bias are *arguments of its `pressure` and
 * `force`. So a sweep instantiates several environments, and the honest object at file granularity
 * is a **set** of regimes rather than a regime with a wider buffer field.
 *
 * The three states this file asserts, and they are three because `CLAUDE.md` requires it —
 * *a `null` that means "no requirement" and a `null` that means "not stated" are different values*:
 *
 *  * **not stated** — a `null` [RegimeSet]. The residue a `P4` gate must **count**, never silently
 *    admit;
 *  * **no environment coordinate** — [RegimeSet.noEnvironment], the empty set. A lattice census, a
 *    plan packing. A claim;
 *  * **solved at these states** — one member, or several.
 */
class RegimeSetTest {

    private val gapAt = { millimolar: Double ->
        Regime.magnesiumChloride(
            name = "the tile-electrode gap at $millimolar mM",
            concentrationMillimolar = millimolar,
            lowestHeightNm = 3.0,
            highestHeightNm = 30.0,
            lowestBiasVolts = 0.0,
            highestBiasVolts = 2.0
        )
    }

    private val sweep = RegimeSet.of(gapAt(0.5), gapAt(2.0), gapAt(10.0))

    private val layer = Regime.neutralLayer(
        name = "the grafted PEG layer",
        lowestHeightNm = 1.0,
        highestHeightNm = 10.0
    )

    // --- gate 1: F2, the three states are three VALUES ------------------------------------------

    @Test
    fun `an empty set is a claim that no environment coordinate enters this result`() {
        assert(RegimeSet.noEnvironment.states.isEmpty())
        assert(RegimeSet.noEnvironment.isWithoutEnvironmentCoordinate)
        assert(!sweep.isWithoutEnvironmentCoordinate)
    }

    @Test
    fun `a stated regime whose buffer is null is not the same value as an empty set`() {
        val stated = RegimeSet.of(layer)
        assert(!stated.isWithoutEnvironmentCoordinate)
        assert(stated.states.single().bufferMillimolar == null)
        assert(stated != RegimeSet.noEnvironment)
    }

    @Test
    fun `not stated is a distinct reading, never a silent admission`() {
        val notStated: RegimeSet? = null
        assert(notStated.readFor(gapAt(2.0)).verdict == RegimeVerdict.NOT_STATED)
        assert(RegimeSet.noEnvironment.readFor(gapAt(2.0)).verdict == RegimeVerdict.ADMITTED)
        assert(sweep.readFor(gapAt(5.0)).verdict == RegimeVerdict.REFUSED)
    }

    // --- gate 2: containment, not equality, in the coordinate the corpus sweeps -----------------

    @Test
    fun `a swept set admits every state some member was solved at`() {
        assert(sweep.reasonToRefuse(gapAt(0.5)) == null)
        assert(sweep.reasonToRefuse(gapAt(2.0)) == null)
        assert(sweep.reasonToRefuse(gapAt(10.0)) == null)
        assert(sweep.admits(gapAt(2.0)))
    }

    @Test
    fun `a swept set refuses a state no member was solved at, naming every member`() {
        val reason = sweep.reasonToRefuse(gapAt(5.0))
        assert(reason != null)
        assert("0.5" in reason!!)
        assert("2.0" in reason)
        assert("10.0" in reason)
        assert("3 state" in reason)
    }

    @Test
    fun `a result with no environment coordinate is consumable in any regime`() {
        assert(RegimeSet.noEnvironment.reasonToRefuse(gapAt(2.0)) == null)
        assert(RegimeSet.noEnvironment.reasonToRefuse(layer) == null)
    }

    @Test
    fun `a one-member set is exactly the regime it holds`() {
        val single = RegimeSet.of(gapAt(2.0))
        assert(single.reasonToRefuse(gapAt(2.0)) == null)
        assert(single.reasonToRefuse(gapAt(0.5)) != null)
        assert(single.reasonToRefuse(gapAt(0.5)) == gapAt(0.5).reasonToRefuse(gapAt(2.0)))
    }

    // --- gate 3: the refusal is a SENTENCE, and it names the coordinate that refused -------------

    @Test
    fun `the refusal of a set names the reason each member gave`() {
        val reason = RegimeSet.of(gapAt(2.0)).reasonToRefuse(layer)
        assert(reason != null)
        assert("no electrolyte at all" in reason!! || "2.0 mM" in reason)
    }

    @Test
    fun `a reading carries the reason it refused on`() {
        val reading = sweep.readFor(gapAt(5.0))
        assert(reading.verdict == RegimeVerdict.REFUSED)
        assert(reading.reason != null)
        assert(RegimeSet.noEnvironment.readFor(gapAt(5.0)).reason == null)
        val notStated: RegimeSet? = null
        assert(notStated.readFor(gapAt(5.0)).reason != null)
    }

    // --- gate 4: a duplicate member is a bookkeeping error, not a wider set ----------------------

    @Test
    fun `a repeated state is refused rather than deduplicated`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            RegimeSet.of(gapAt(2.0), gapAt(2.0))
        }
        assert(failure.message!!.contains("twice"))
    }

    // --- gate 5: the falsifier CH-0224's repair 2 has to survive ---------------------------------

    /**
     * `T-286`'s declared falsifier 1: *if the buffer can be widened to a set inside [Regime]
     * without the height and bias intervals becoming a union that admits a pair no record was
     * solved at, then a second type is waste.*
     *
     * `actuator/TallGapDeviceBStudy` is the counterexample and it is in the corpus: it solves
     * `{0.5, 1.0, 2.0} mM` over its **tall** heights and `{0.5, 2.0} mM` over its **fold** heights.
     * A single regime carrying the union of both would admit `1.0 mM` at a fold height, which no
     * record of that file carries — and a set of two regimes does not.
     */
    @Test
    fun `a set separates two sub-sweeps that a widened single regime would merge`() {
        val tall = Regime.magnesiumChloride(
            name = "device B, the tall reach sweep",
            concentrationMillimolar = 1.0,
            lowestHeightNm = 17.0,
            highestHeightNm = 26.0,
            lowestBiasVolts = 0.0,
            highestBiasVolts = 2.0
        )
        val fold = Regime.magnesiumChloride(
            name = "device B, the fold sweep",
            concentrationMillimolar = 0.5,
            lowestHeightNm = 5.0,
            highestHeightNm = 16.0,
            lowestBiasVolts = 0.0,
            highestBiasVolts = 2.0
        )
        val set = RegimeSet.of(tall, fold)
        // the pair a UNION would admit and no record carries: 1.0 mM at a fold height
        val phantom = Regime.magnesiumChloride(
            name = "1.0 mM at a fold height",
            concentrationMillimolar = 1.0,
            lowestHeightNm = 5.0,
            highestHeightNm = 16.0,
            lowestBiasVolts = 0.0,
            highestBiasVolts = 2.0
        )
        assert(set.reasonToRefuse(phantom) != null)
        assert(set.reasonToRefuse(tall) == null)
        assert(set.reasonToRefuse(fold) == null)
    }

    // --- gate 6: symmetry — a set of one behaves as the regime, at every coordinate --------------

    @Test
    fun `a singleton set agrees with its member on every refusal the member states`() {
        val consumers = listOf(
            gapAt(0.5), gapAt(2.0), layer,
            Regime.magnesiumChloride(
                name = "a narrower gap",
                concentrationMillimolar = 2.0,
                lowestHeightNm = 5.0,
                highestHeightNm = 7.0,
                lowestBiasVolts = 0.0,
                highestBiasVolts = 1.0
            )
        )
        val member = gapAt(2.0)
        consumers.forEach { consumer ->
            assert(
                (RegimeSet.of(member).reasonToRefuse(consumer) == null) ==
                    (consumer.reasonToRefuse(member) == null)
            )
        }
    }
}
