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
import com.xemantic.nano.plentyofroom.actuator.DiffuseParametrisedField
import com.xemantic.nano.plentyofroom.actuator.EquilibriumPath
import com.xemantic.nano.plentyofroom.actuator.FieldSample
import com.xemantic.nano.plentyofroom.anchoring.TwoSpringElastica
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-149` — the **recommended** element's pull-in fold, which `CH-0083` records has never been
 * searched. Every test is named for the verification gate it discharges, per §5 of the problem
 * definition.
 *
 * `C-0018` located §6 task 4's fold for the affine mandate and `C-0032` for `C-0030`'s
 * strain-**softening** flexure; `C-0071` recommends a **third** law. The spine of this suite is
 * therefore the **cheap bound** — that at a fold the composition of two corrections is exact, so
 * the *sign* of a load-line substitution is available for one evaluation of each law — and it is
 * asserted here against a synthetic field whose fold has a **closed form**, so the bound is graded
 * rather than described. The real Poisson-Boltzmann sweep is the study's business: one fold search
 * is ~1700 field solves and belongs in `tools/study.sh`.
 */
class RecommendedElementFoldTest {

    private val arm = recommendedArmLine("Q5 recommended arm")

    private val mandate = AffineLoadLine("linear mandate", GEN1_MANDATE_STIFFNESS)

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - a reaction over a stroke IS the secant, identically`() {
        listOf(0.5, 1.0, 3.0, 4.0).forEach { stroke ->
            assert(arm.secant(stroke).isCloseTo(arm.reaction(stroke) / stroke))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the path count multiplies reaction secant and tangent alike`() {
        val doubled = arm.withCount(2 * GEN1_RECOMMENDED_PATH_COUNT)
        listOf(1.0, 3.0, 4.0).forEach { stroke ->
            assert((doubled.reaction(stroke) / arm.reaction(stroke)).isCloseTo(2.0))
            assert((doubled.secant(stroke) / arm.secant(stroke)).isCloseTo(2.0))
            assert((doubled.tangent(stroke) / arm.tangent(stroke)).isCloseTo(2.0, 1e-6))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { arm.withCount(0) }
        assertFailsWith<IllegalArgumentException> { arm.reaction(-1.0) }
        assertFailsWith<IllegalArgumentException> { arm.secant(0.0) }
        assertFailsWith<IllegalArgumentException> {
            loadLineStrokeCeiling(arm, low = 5.0, high = 1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            foldPerturbation(mandate, arm, baselineFoldStroke = -1.0)
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - the placed array delivers exactly 100 pN at 3 nm`() {
        assert(arm.secant(GEN1_ACCEPTABLE_STROKE).isCloseTo(GEN1_MANDATE_STIFFNESS, 1e-6))
        assert(arm.reaction(GEN1_ACCEPTABLE_STROKE).isCloseTo(Gen1Tile.TARGET_FORCE, 1e-6))
    }

    @Test
    fun `gate 2 limiting cases - the tangent at vanishing stroke IS the small-rotation stiffness`() {
        // c(rho_n, rho_f) EI/L^3 per arm, which is C-0034's closed form and shares no code with
        // the elastica integrator
        assert(arm.tangent(1e-5).isCloseTo(arm.smallRotationStiffness, 1e-5))
        assert(arm.secant(1e-5).isCloseTo(arm.smallRotationStiffness, 1e-5))
    }

    @Test
    fun `gate 2 limiting cases - the recommended element is strain-STIFFENING over the traversed range`() {
        // C-0017's theorem has a SIGN and C-0030 flipped it; this element is on the other side.
        // t/s > 1 at the placement stroke, and the tangent rises monotonically from the
        // small-rotation limit, so the minimum over [0, s*] is at ZERO STROKE and is a BOUNDARY
        // minimum, not CH-0042's interior one.
        assert(arm.tangentToSecant(GEN1_ACCEPTABLE_STROKE) > 1.0)
        val minimum = arm.tangentMinimum(low = 0.0, high = GEN1_ACCEPTABLE_STROKE)
        assert(!minimum.interior)
        assert(minimum.stroke.isCloseTo(0.0, 1e-6))
        assert(minimum.stiffness.isCloseTo(arm.smallRotationStiffness, 1e-4))
    }

    @Test
    fun `gate 2 limiting cases - an inextensible arm cannot be asked past its own contour`() {
        assert(!arm.answersAt(arm.length))
        assert(!arm.answersAt(2.0 * arm.length))
        assert(arm.answersAt(GEN1_ACCEPTABLE_STROKE))
        assertFailsWith<IllegalArgumentException> { arm.reaction(arm.length + 1.0) }
    }

    @Test
    fun `gate 2 limiting cases - substituting a line into ITSELF moves nothing`() {
        val same = foldPerturbation(mandate, AffineLoadLine("copy", GEN1_MANDATE_STIFFNESS), 3.5)
        assert(same.tangentChange.isCloseTo(0.0, 1e-12))
        assert(same.reactionChange.isCloseTo(0.0, 1e-12))
        assert(same.predictedDirection == FoldDirection.UNMOVED.name)
    }

    @Test
    fun `gate 2 limiting cases - a SOFTER substitute predicts a shallower fold and a stiffer one deeper`() {
        val softer = foldPerturbation(mandate, AffineLoadLine("soft", 20.0), 3.5)
        assert(softer.predictedDirection == FoldDirection.SHALLOWER.name)
        val stiffer = foldPerturbation(mandate, AffineLoadLine("stiff", 50.0), 3.5)
        assert(stiffer.predictedDirection == FoldDirection.DEEPER.name)
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 conservation - PLACEMENT is an identity, so both lines locate the same operating bias`() {
        val biases = listOf(mandate, arm).map {
            syntheticPath(it).at(GEN1_ACCEPTABLE_STROKE)!!.appliedBias
        }
        assert(biases[1].isCloseTo(biases[0], 1e-12))
    }

    @Test
    fun `gate 3 conservation - the tangency identity holds at the fold of the recommended line`() {
        // On the synthetic field k_es = -attraction/lambda and k_brush = 0, so the coupled tangent
        // at the fold is k_c(s) - R(s)/lambda. The path search never sees that expression: it
        // maximises V_eq(s). The two routes share no code.
        val fold = syntheticPath(arm).fold().fold
        assert(fold != null)
        val stroke = fold!!.stroke
        val residual = arm.tangent(stroke) - arm.reaction(stroke) / SYNTHETIC_DECAY
        val scale = arm.tangent(stroke) + arm.reaction(stroke) / SYNTHETIC_DECAY
        assert(abs(residual) / scale < 1e-4)
    }

    @Test
    fun `gate 3 conservation - THE DECLARED FALSIFIER, run on a field with a closed-form fold`() {
        // Any line through the origin folds at exactly lambda on this field, whatever its
        // stiffness, so the baseline fold stroke is known analytically and the substitution's
        // effect is isolated. The cheap bound reads the SLOPE term at that stroke; the search
        // re-solves the whole path. They must agree on the direction, and if they do not, the
        // "exact composition at a fold" rule does not carry a nonlinear load line.
        val baseline = syntheticPath(mandate).fold().fold!!
        assert(baseline.stroke.isCloseTo(SYNTHETIC_DECAY, 1e-3))
        val bound = foldPerturbation(mandate, arm, baseline.stroke)
        assert(bound.predictedDirection == FoldDirection.DEEPER.name)
        val solved = syntheticPath(arm).fold().fold!!
        assert(solved.stroke > baseline.stroke)
    }

    @Test
    fun `gate 3 conservation - and the falsifier fires the OTHER way for a softening substitute`() {
        val baseline = syntheticPath(mandate).fold().fold!!
        val softer = AffineLoadLine("soft", 0.6 * GEN1_MANDATE_STIFFNESS, preload = 40.0)
        val bound = foldPerturbation(mandate, softer, baseline.stroke)
        assert(bound.predictedDirection == FoldDirection.SHALLOWER.name)
        val solved = syntheticPath(softer).fold().fold!!
        assert(solved.stroke < baseline.stroke)
    }

    @Test
    fun `gate 3 conservation - refusal is MONOTONE in the stroke, so the ceiling is a threshold`() {
        val ceiling = loadLineStrokeCeiling(arm, low = 0.1, high = arm.length)
        assert(ceiling < arm.length)
        assert(ceiling > GEN1_ACCEPTABLE_STROKE)
        assert(strokeCeilingIsMonotone(arm, ceiling, arm.length))
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 numerical convergence - the placed arm is RK4-step independent`() {
        val finest = recommendedArmLine("finest", steps = 800).length
        listOf(200, 400).forEach { steps ->
            assert(recommendedArmLine("coarse", steps = steps).length.isCloseTo(finest, 1e-6))
        }
    }

    @Test
    fun `gate 4 numerical convergence - the stroke ceiling is bisection-resolution independent`() {
        val finest = loadLineStrokeCeiling(arm, 0.1, arm.length, resolution = 1e-8)
        listOf(1e-4, 1e-6).forEach { resolution ->
            assert(
                abs(loadLineStrokeCeiling(arm, 0.1, arm.length, resolution) - finest) <=
                        2.0 * resolution
            )
        }
    }

    @Test
    fun `gate 4 numerical convergence - the located fold is coarse-scan independent`() {
        val finest = syntheticPath(arm).fold(coarseSteps = 48, strokeTolerance = 1e-8)
        listOf(8, 12, 24).forEach { steps ->
            val coarse = syntheticPath(arm).fold(coarseSteps = steps, strokeTolerance = 1e-6)
            assert(coarse.fold!!.appliedBias.isCloseTo(finest.fold!!.appliedBias, 1e-6))
        }
    }

    // ---------------------------------------------- gate 5 — upstream cross-check

    @Test
    fun `gate 5 upstream cross-check - C-0069's Q5 reproduced from its own libraries`() {
        assert(GEN1_ARM_ROOT_STIFFNESS.isCloseTo(13.5294118, 1e-6))
        assert(GEN1_ARM_TIP_STIFFNESS.isCloseTo(78.2352941, 1e-6))
        assert(arm.length.isCloseTo(8.16439083, 1e-6))
        assert((arm.length / Gen1Tile.RISE_PER_BASE_PAIR).isCloseTo(24.0129142, 1e-6))
        assert(arm.secant(GEN1_ACCEPTABLE_STROKE).isCloseTo(33.3333333, 1e-6))
        assert(arm.tangent(GEN1_ACCEPTABLE_STROKE).isCloseTo(40.8120233, 1e-5))
        assert(
            arm.tangentMinimum(low = 0.0, high = GEN1_ACCEPTABLE_STROKE)
                .stiffness.isCloseTo(30.028762, 1e-4)
        )
    }

    @Test
    fun `gate 5 upstream cross-check - the cheap bound against C-0017's six 2 mM floors`() {
        // C-0071's tie-break axis 2: the assembled tangent minimum over the TRAVERSED range
        // clears all six of C-0017's stability floors at 10 nm / 2 mM, where C-0030's 22.88 clears
        // none. This is the STATIC reading, and CH-0083 is that it says nothing about the fold.
        val minimum = arm.tangentMinimum(low = 0.0, high = GEN1_ACCEPTABLE_STROKE).stiffness
        assert(minimum > 27.9133262)
        assert(minimum < GEN1_MANDATE_STIFFNESS)
    }

    @Test
    fun `gate 5 upstream cross-check - the rotation limit is where C-0039's own branch ends`() {
        // C-0039: past pi/2 the tip force's moment arm reverses and the elastica acquires branches
        // the shooting solver does not enumerate. The arm is well inside it at the placement point.
        assert(arm.maximumRotation(GEN1_ACCEPTABLE_STROKE) < 0.5 * PI)
        val limit = rotationLimitStroke(arm, low = 0.1, high = arm.length)
        assert(limit > GEN1_ACCEPTABLE_STROKE)
        assert(arm.maximumRotation(limit) <= 0.5 * PI)
        // the branch ends by FOLDING, not by turning past a right angle: the reaction solve still
        // closes at 0.9 of pi/2 and refuses immediately above, so pi/2 is never actually reached
        assert(arm.maximumRotation(limit) > 0.9 * 0.5 * PI)
        // and the TANGENT — a forward difference of the same law — refuses FIRST, which is why a
        // path's stroke ceiling must take the smaller of the two and not the rotation limit alone
        val refusal = loadLineStrokeCeiling(arm, low = 0.1, high = arm.length)
        assert(refusal < limit)
        assert(limit - refusal < Gen1Tile.RISE_PER_BASE_PAIR)
    }

    // ---------------------------------------------------------------- the synthetic field

    private fun syntheticPath(line: StrokeLoadLine): EquilibriumPath {
        val ceiling = min(SYNTHETIC_RESTING - 0.5, 0.95 * ARM_CEILING)
        return EquilibriumPath(
            restingHeight = SYNTHETIC_RESTING,
            strokeCeiling = ceiling,
            field = SYNTHETIC_FIELD
        ) { stroke -> line.reaction(stroke) }
    }

    private companion object {

        const val SYNTHETIC_DECAY = 2.0

        const val SYNTHETIC_RESTING = 10.0

        /** The recommended arm's own kinematic ceiling, so the synthetic path stays inside it. */
        val ARM_CEILING: Double = loadLineStrokeCeiling(
            recommendedArmLine("ceiling probe"), 0.1,
            TwoSpringElastica(
                Gen1Tile.DUPLEX_BENDING_RIGIDITY,
                recommendedArmLine("ceiling probe").length,
                GEN1_ARM_ROOT_STIFFNESS, GEN1_ARM_TIP_STIFFNESS
            ).length
        )

        /**
         * `|F_es| = A ψ² exp(−h/λ)`, with the applied bias taken equal to the diffuse drop.
         *
         * Its equilibrium path is `V_eq(s) = √(R(s) e^{(L₀−s)/λ}/A)`, whose stationary point is
         * `R'(s)/R(s) = 1/λ` — i.e. `k_c + k_es = 0` with `k_es = −|F|/λ`. Closed form, so the
         * fold search and the cheap bound can both be graded rather than described.
         */
        val SYNTHETIC_FIELD = DiffuseParametrisedField { gap, psi ->
            val attraction = 4.0e5 * psi * psi * exp(-gap / SYNTHETIC_DECAY)
            FieldSample(gap, psi, psi, -attraction)
        }
    }
}
