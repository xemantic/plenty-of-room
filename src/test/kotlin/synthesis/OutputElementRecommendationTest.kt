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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.ArmAnchorage
import com.xemantic.nano.plentyofroom.anchoring.OrigamiDuplex
import com.xemantic.nano.plentyofroom.anchoring.UPWARD_ROOT_PITCH_BASE_PAIRS
import com.xemantic.nano.plentyofroom.anchoring.axialLengthForStiffness
import com.xemantic.nano.plentyofroom.anchoring.bendingFactorForLength
import com.xemantic.nano.plentyofroom.anchoring.bendingLengthForStiffness
import com.xemantic.nano.plentyofroom.anchoring.elasticaArmForStiffness
import com.xemantic.nano.plentyofroom.anchoring.farRestraintCeiling
import com.xemantic.nano.plentyofroom.anchoring.hingeLeverForStiffness
import com.xemantic.nano.plentyofroom.anchoring.nearRestraintCeiling
import com.xemantic.nano.plentyofroom.anchoring.perPathStiffness
import com.xemantic.nano.plentyofroom.anchoring.rowOfThreeLengthCeiling
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-135` — **which output element does the Gen-1 programme recommend, and on what premises?**
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The spine of this task is that a *recommendation* is a claim like any other and must therefore
 * carry its provenance: this suite checks the ledger arithmetic (which is a set of identities),
 * the decidability bound (which decides whether a recommendation can be made at all), and the
 * reproduction of every number the recommendation writes out of the library that owns it.
 */
class OutputElementRecommendationTest {

    private val mandate = mandatedCouplingStiffness(Gen1Tile.TARGET_FORCE, Gen1Tile.ACCEPTABLE_STROKE)

    private val perPath = perPathStiffness(mandate, ARM_COUNT)

    private val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * Gen1Tile.RISE_PER_BASE_PAIR

    private val budget = rowOfThreeLengthCeiling(pitch, OrigamiDuplex.INTERHELICAL)

    private val arm = elasticaArmForStiffness(
        hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
        hingeCount = 1,
        farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
        bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
        count = ARM_COUNT,
        targetStiffness = mandate,
        workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
    )

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - a margin is a ratio and is invariant under a common rescaling`() {
        val ceiling = marginRatio(value = 8.16439083, limit = 8.19, sense = MarginSense.CEILING)
        val floor = marginRatio(value = 30.028762, limit = 27.9133262, sense = MarginSense.FLOOR)
        assert(ceiling.isCloseTo(8.19 / 8.16439083))
        assert(floor.isCloseTo(30.028762 / 27.9133262))
        listOf(1e-6, 1.0, 1e6).forEach { scale ->
            assert(
                marginRatio(8.16439083 * scale, 8.19 * scale, MarginSense.CEILING).isCloseTo(ceiling)
            )
            assert(
                marginRatio(30.028762 * scale, 27.9133262 * scale, MarginSense.FLOOR).isCloseTo(floor)
            )
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the plan margin is a LENGTH and scales linearly with the lattice`() {
        val margin = budget - arm
        assert(margin > 0.0)
        listOf(2.0, 10.0).forEach { scale ->
            val scaled = rowOfThreeLengthCeiling(
                pitch * scale, OrigamiDuplex.INTERHELICAL * scale
            )
            assert((scaled / budget).isCloseTo(scale))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            marginRatio(0.0, 1.0, MarginSense.CEILING)
        }
        assertFailsWith<IllegalArgumentException> {
            marginRatio(1.0, -1.0, MarginSense.FLOOR)
        }
        assertFailsWith<IllegalArgumentException> {
            recommendationMargin(
                quantity = "", owner = "C-0069", axis = "plan",
                value = 1.0, limit = 2.0, sense = MarginSense.CEILING, note = ""
            )
        }
        assertFailsWith<IllegalArgumentException> {
            tieBreakAxis("empty", betterIsLower = true, values = emptyMap())
        }
        assertFailsWith<IllegalArgumentException> { decidability(emptyList(), 0) }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - a ONE-survivor set is unanimous trivially and needs no axis`() {
        val only = candidate("Q5", survives = true)
        val bound = decidability(listOf(only, candidate("Q1", survives = false)), 2)
        assert(bound.survivors == 1)
        assert(bound.unanimous)
        assert(bound.winner == "Q5")
        assert(!bound.falsifierFired)
    }

    @Test
    fun `gate 2 limiting cases - a set tied on every axis has NO winner and the falsifier fires`() {
        val a = candidate("A", survives = true, motifs = 1, floors = 6, compression = 0)
        val b = candidate("B", survives = true, motifs = 1, floors = 6, compression = 0)
        val bound = decidability(listOf(a, b), 2)
        assert(bound.survivors == 2)
        assert(!bound.unanimous)
        assert(bound.winner == null)
        assert(bound.falsifierFired)
    }

    @Test
    fun `gate 2 limiting cases - axes that DISAGREE fire the declared falsifier`() {
        val a = candidate("A", survives = true, motifs = 1, floors = 4, compression = 0)
        val b = candidate("B", survives = true, motifs = 2, floors = 6, compression = 0)
        val bound = decidability(listOf(a, b), 2)
        assert(!bound.unanimous)
        assert(bound.winner == null)
        assert(bound.falsifierFired)
    }

    @Test
    fun `gate 2 limiting cases - a strictly dominated candidate never wins`() {
        val good = candidate("good", survives = true, motifs = 1, floors = 6, compression = 0)
        val bad = candidate("bad", survives = true, motifs = 2, floors = 4, compression = 1)
        val bound = decidability(listOf(bad, good), 2)
        assert(bound.unanimous)
        assert(bound.winner == "good")
        assert(bound.axes.all { it.winner == "good" })
    }

    @Test
    fun `gate 2 limiting cases - an EMPTY survivor set cannot recommend`() {
        val bound = decidability(listOf(candidate("Q1", survives = false)), 1)
        assert(bound.survivors == 0)
        assert(bound.winner == null)
        assert(bound.falsifierFired)
    }

    @Test
    fun `gate 2 limiting cases - a margin exactly at its limit is NONE and never VIOLATED`() {
        assert(classifyMargin(1.0) == MarginClass.NONE)
        assert(classifyMargin(1.0 - 1e-12) == MarginClass.VIOLATED)
        assert(classifyMargin(NO_MARGIN_THRESHOLD) == MarginClass.THIN)
        assert(classifyMargin(THIN_MARGIN_THRESHOLD) == MarginClass.COMFORTABLE)
    }

    // ------------------------------------------------ gate 3 — symmetry and conservation

    @Test
    fun `gate 3 conservation - the funnel counts conserve`() {
        val catalogue = listOf(
            candidate("Q5", survives = true), candidate("Q7", survives = true),
            candidate("Q11", survives = false), candidate("Q1", survives = false)
        )
        val bound = decidability(catalogue, placeAtOneLevel = 3)
        assert(bound.catalogueSize == 4)
        assert(bound.survivors + bound.rejected == bound.catalogueSize)
        assert(bound.placeAtOneLevel >= bound.survivors)
    }

    @Test
    fun `gate 3 conservation - the premise statuses PARTITION the ledger`() {
        val ledger = listOf(
            premise("P1", PremiseStatus.DERIVED), premise("P2", PremiseStatus.UNDEMONSTRATED),
            premise("P3", PremiseStatus.CITED_FITTED), premise("P4", PremiseStatus.SPECIFICATION)
        )
        val counted = PremiseStatus.entries.sumOf { status -> ledger.count { it.status == status } }
        assert(counted == ledger.size)
        assert(ledger.count { it.status == PremiseStatus.UNDEMONSTRATED } == 1)
    }

    @Test
    fun `gate 3 conservation - every failure route carries exactly one effect and one decider`() {
        val route = failureRoute(
            id = "R1", statement = "a", owner = "C-0069",
            effect = RouteEffect.REMOVES_THE_ELEMENT, decidedBy = RouteDecider.MEASUREMENT,
            insidePublishedBracket = true, consequence = "b"
        )
        assert(RouteEffect.entries.count { it == route.effect } == 1)
        assert(RouteDecider.entries.count { it == route.decidedBy } == 1)
    }

    @Test
    fun `gate 3 conservation - the classification is MONOTONE in the margin`() {
        val ladder = listOf(0.5, 0.99, 1.0, 1.04, 1.05, 1.49, 1.5, 10.0)
        val ordinals = ladder.map { classifyMargin(it).ordinal }
        assert(ordinals == ordinals.sorted())
    }

    @Test
    fun `gate 3 symmetry - the two knife edges are ONE lattice quantity, and only one of them binds`() {
        // C-0069's plan margin is `pitch - d - L`; C-0066's free-standing tie gap is
        // `(pitch - L) - d`. They are the same number, and the design uses neither the
        // free-standing tie nor anything else that spends it twice.
        val planMargin = budget - arm
        val tieGap = (pitch - arm) - OrigamiDuplex.INTERHELICAL
        assert(abs(planMargin - tieGap) < 1e-12)
        // the registration the design actually uses — a tie on the arm's own tip — needs only
        // half a duplex, and is not a knife edge at all
        val tipRegistration = marginRatio(
            value = OrigamiDuplex.INTERHELICAL / 2.0, limit = pitch - arm, sense = MarginSense.CEILING
        )
        assert(classifyMargin(tipRegistration) == MarginClass.COMFORTABLE)
        assert(classifyMargin(marginRatio(arm, budget, MarginSense.CEILING)) == MarginClass.NONE)
    }

    // ------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the elastica arm is RK4-step independent`() {
        fun armAt(steps: Int) = elasticaArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(), hingeCount = 1,
            farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
            bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY, count = ARM_COUNT,
            targetStiffness = mandate, workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE,
            steps = steps
        )
        val coarse = armAt(200)
        val fine = armAt(800)
        assert(abs(fine - coarse) / fine < 1e-6)
    }

    @Test
    fun `gate 4 convergence - the restraint ceilings are bisection-resolution independent`() {
        val coarse = nearRestraintCeiling(
            farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
            lengthCeiling = budget, resolution = 1e-4
        )
        val fine = nearRestraintCeiling(
            farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
            lengthCeiling = budget, resolution = 1e-7
        )
        assert(coarse != null && fine != null)
        assert(abs(fine!! - coarse!!) / fine < 1e-4)
    }

    // ------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 upstream - C-0017's mandate and the per-path secant at 34 paths`() {
        assert(mandate.isCloseTo(33.3333333, 1e-6))
        assert(perPath.isCloseTo(0.980392157, 1e-6))
    }

    @Test
    fun `gate 5 upstream - C-0069's plan budget is the bare pitch minus one duplex`() {
        assert(pitch.isCloseTo(10.88))
        assert(budget.isCloseTo(8.19))
    }

    @Test
    fun `gate 5 upstream - C-0055 and C-0063's arm reproduces at 8_16439 nm`() {
        assert(arm.isCloseTo(8.16439083, 1e-6))
        assert((budget - arm).isCloseTo(0.0256091734, 1e-4))
    }

    @Test
    fun `gate 5 upstream - C-0069's end-condition budget is c less than 2_3416`() {
        val c = bendingFactorForLength(budget, Gen1Tile.DUPLEX_BENDING_RIGIDITY, perPath)
        assert(c.isCloseTo(2.3416, 1e-4))
        // and it is an exact inverse of the length law
        val back = bendingLengthForStiffness(c, Gen1Tile.DUPLEX_BENDING_RIGIDITY, perPath)
        assert(back.isCloseTo(budget, 1e-12))
    }

    @Test
    fun `gate 5 upstream - the two-support family's own floor is 22_414 nm, 2_74x the budget`() {
        val floor = bendingLengthForStiffness(48.0, Gen1Tile.DUPLEX_BENDING_RIGIDITY, perPath)
        assert(floor.isCloseTo(22.414, 1e-4))
        assert((floor / budget).isCloseTo(2.737, 1e-3))
    }

    @Test
    fun `gate 5 upstream - the normal direction is AXIAL and asks for 1122 nm`() {
        val axial = axialLengthForStiffness(Gen1Tile.DUPLEX_STRETCH_MODULUS, perPath)
        assert(axial.isCloseTo(1122.0, 1e-4))
    }

    @Test
    fun `gate 5 upstream - the hinge lever is the most compact mechanism at 3_715 nm`() {
        val lever = hingeLeverForStiffness(Gen1Tile.crossoverHingeStiffness(), perPath)
        assert(lever.isCloseTo(3.715, 1e-3))
    }

    @Test
    fun `gate 5 upstream - C-0069's two restraint ceilings, and the design sits inside both`() {
        val tipCeiling = farRestraintCeiling(
            nearStiffness = Gen1Tile.crossoverHingeStiffness(), lengthCeiling = budget
        )
        val rootCeiling = nearRestraintCeiling(
            farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness, lengthCeiling = budget
        )
        assert(tipCeiling != null && rootCeiling != null)
        assert(tipCeiling!!.isCloseTo(79.678, 1e-3))
        assert(rootCeiling!!.isCloseTo(13.930, 1e-3))
        assert(ArmAnchorage.twoTerminus().rotationalStiffness < tipCeiling)
        assert(Gen1Tile.crossoverHingeStiffness() < rootCeiling)
        // and neither is a margin: both are inside 3 %
        assert(classifyMargin(tipCeiling / ArmAnchorage.twoTerminus().rotationalStiffness) == MarginClass.NONE)
        assert(classifyMargin(rootCeiling / Gen1Tile.crossoverHingeStiffness()) == MarginClass.NONE)
    }

    @Test
    fun `gate 5 upstream - C-0009's crossover hinge and C-0034's A2 couple`() {
        assert(Gen1Tile.crossoverHingeStiffness().isCloseTo(13.5294118, 1e-6))
        assert(ArmAnchorage.twoTerminus().rotationalStiffness.isCloseTo(78.2352941, 1e-6))
    }

    @Test
    fun `gate 5 upstream - a reproduction record reports its own departure`() {
        val exact = recommendationReproduction("C-0017", "the mandate", 33.3333333, mandate)
        assert(exact.departure < 1e-6)
        val off = recommendationReproduction("x", "y", 100.0, 101.0)
        assert(off.departure.isCloseTo(0.01))
    }

    // ------------------------------------------------------------------ helpers

    private fun candidate(
        id: String,
        survives: Boolean,
        motifs: Int = 1,
        floors: Int = 6,
        compression: Int = 0
    ) = RecommendationCandidate(
        id = id, name = id, undemonstratedMotifs = motifs, stabilityFloorsCleared = floors,
        compressionMembers = compression, placesInFull = survives, singleLevel = survives,
        twoSided = survives
    )

    private fun premise(id: String, status: PremiseStatus) = RecommendationPremise(
        id = id, statement = "s", owner = "C-0069", status = status, worth = "w"
    )
}
