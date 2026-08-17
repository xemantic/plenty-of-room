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

package com.xemantic.nano.plentyofroom.brush

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.material.PegWater
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test

/**
 * Task `T-1e` — the gates on the **first-moment** height convention and on the chain-length
 * inversion that goes with it.
 *
 * `C-0011` inverts `N` on a **force-onset** height and reports `2⟨z⟩` beside it; `CH-0010` says in
 * as many words that inverting on `2⟨z⟩` too *"would separate the definitional part of this
 * challenge from the physical part exactly rather than by scaling"*. These tests are what make the
 * new functional trustworthy enough for that separation to mean anything.
 *
 * The one that matters most is gate 4. `CLAUDE.md` records that an SCF window edge is not
 * grid-converged where a stiffness is, and that **convergence is a property of the quantity**: a
 * first moment is a ratio of two quadratures over a node count that itself steps with the solved
 * wall height, and it has to earn its own order rather than inherit the pressure's.
 */
private val t1ePeg = PegWater()

/** §3: the 40 × 40 nm tile footprint. */
private const val T1E_TILE_AREA = 1600.0

/** `T-1d`'s primary resting load — 1 pN over the tile, in `pN/nm²`. */
private const val T1E_RESTING_PRESSURE = 1.0 / T1E_TILE_AREA

private val t1eDesCloizeaux =
    desCloizeauxInteraction(t1ePeg.crossoverIndex, t1ePeg.monomerVolume)

private val t1eTwoBody = twoBodyInteraction(
    t1ePeg.reducedSecondVirialCoefficient(1.9e-3), t1ePeg.monomerVolume
)

/** A mean-field parabola, `m = 2`, whose first-moment ratio is exactly `3/4`. */
private val t1eMeanField = PowerLawInteraction(
    name = "mean-field", coefficient = 1.0, exponent = 2.0, monomerVolume = t1ePeg.monomerVolume
)

/** A grid coarse enough to keep the suite fast; gate 4 is what justifies it. */
private val t1eTestGrid = ScfDiscretisation(
    nodeSpacing = 0.25,
    contourStepsPerMonomer = 1.0
)

/** `T-1d`'s production grid, needed where a standing emitted number is reproduced. */
private val t1eProductionGrid = ScfDiscretisation(
    nodeSpacing = 0.2,
    contourStepsPerMonomer = 2.0
)

/** The `C-0001`/`C-0003`/`C-0011` 10 nm design point. */
private const val T1E_REFERENCE_HEIGHT = 10.0

private const val T1E_REFERENCE_DENSITY = 0.024

/**
 * The relative slack an identity checked against an **emitted** field of `T-1d` must be allowed.
 *
 * `T-1d` emits at `SOLVED_HEIGHT_SIGNIFICANT_DIGITS = 6` (`C-0073`), so one field carries up to
 * `5e-6` relative and a ratio of two carries `1e-5`. **Asserting tighter than this is not a
 * stronger test, it is a test of the printed digits.**
 */
private const val T1E_EMITTED_FIELD_SLACK = 5e-5

class FirstMomentThicknessTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - a first moment is a length in nm and a box profile's is its own height`() {
        // 2<z> = 2 * (integral z phi dz)/(integral phi dz): nm^4 over nm^3, a length. For a box
        // profile <z> = L/2 identically, at every chain length and every grafting density, which
        // is the whole reason the box is the model that quotes its own height honestly.
        val box = AlexanderBoxLayer(t1eDesCloizeaux)
        listOf(40.0, 120.0, 400.0).forEach { length ->
            listOf(0.01, 0.024, 0.2).forEach { density ->
                val chain = t1ePeg.graftedChain(length, density)
                val height = box.equilibriumHeight(chain)
                assert(box.firstMomentThickness(chain, height).isCloseTo(height, 1e-14))
                assert(box.restingFirstMomentThickness(chain).isCloseTo(height, 1e-14))
            }
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the inverted chain length reproduces the requested thickness`() {
        // The inversion is a root find and its residual is what says so. Every model, so that the
        // analytic pair and the solved layer are held to one contract.
        val target = 6.0
        val models = listOf(
            AlexanderBoxLayer(t1eDesCloizeaux),
            StrongStretchingLayer(t1eDesCloizeaux),
            SelfConsistentFieldLayer(t1eDesCloizeaux, t1eTestGrid, T1E_RESTING_PRESSURE)
        )
        models.forEach { model ->
            val length = model.chainLengthForFirstMomentThickness(
                t1ePeg, target, T1E_REFERENCE_DENSITY
            )
            val achieved = model.restingFirstMomentThickness(
                t1ePeg.graftedChain(length, T1E_REFERENCE_DENSITY)
            )
            assert(achieved.isCloseTo(target, 1e-5))
        }
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - a box layer's two inversions are the same inversion`() {
        // The box's first-moment thickness IS its edge, so its two conventions coincide exactly
        // and the new root find must return the standing one to the last digit. If it does not,
        // the bracket or the tolerance is wrong and nothing below is worth reading.
        val box = AlexanderBoxLayer(t1eDesCloizeaux)
        listOf(5.0, 7.0, 10.0).forEach { height ->
            val onMoment = box.chainLengthForFirstMomentThickness(
                t1ePeg, height, T1E_REFERENCE_DENSITY
            )
            val onHeight = box.chainLengthForHeight(t1ePeg, height, T1E_REFERENCE_DENSITY)
            assert(onMoment.isCloseTo(onHeight, 1e-9))
        }
    }

    @Test
    fun `gate 2 limiting cases - a box layer's two inversions agree over the whole grid, including where the seed IS the answer`() {
        // The three-point version of the test above passed and the sweep did not. The box's seed
        // is its own answer at EVERY point, so every one of these lands on a tie: `achieved` can
        // exceed the target by one ulp while `ln(achieved/target)` rounds to a small NEGATIVE
        // number, and a bracket walk written on the first spelling then hands `bracketedRoot` a
        // bracket it reads as having no sign change. Whether the ulp falls that way is a property
        // of the point, so only a sweep finds it — 61 grafting densities times three heights did,
        // at sigma = 0.0027 and 5 nm, after a three-point check said the routine was fine.
        val box = AlexanderBoxLayer(t1eDesCloizeaux)
        val sst = StrongStretchingLayer(t1eDesCloizeaux)
        val densities = logarithmicSweep(0.002, 1.0, 61)
        listOf(5.0, 7.0, 10.0).forEach { height ->
            densities.forEach { density ->
                assert(
                    box.chainLengthForFirstMomentThickness(t1ePeg, height, density)
                        .isCloseTo(box.chainLengthForHeight(t1ePeg, height, density), 1e-5)
                )
                // and strong stretching, whose seed is NOT its answer, still lands on its own
                // closed-form ratio: N is exactly linear in L for a pure power law
                assert(
                    sst.chainLengthForFirstMomentThickness(t1ePeg, height, density)
                        .isCloseTo(
                            sst.chainLengthForHeight(t1ePeg, height, density) /
                                    strongStretchingFirstMomentRatio(2.25),
                            1e-4
                        )
                )
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the strong-stretching first moment is the closed-form Beta ratio`() {
        // For a PURE power law the uncompressed strong-stretching profile is exactly
        // phi(z) = phi0 (1 - z^2/L^2)^p with p = 1/(m-1), so
        //     2<z>/L = 1 / [ (p+1) * halfCircleMoment(p) ]
        // which is 3/4 at m = 2 and 0.783596 at m = 9/4. A Beta-function identity, no fitted
        // number anywhere, and it is the check that the quadrature resolves the outer edge —
        // where (L^2 - z^2)^0.8 has an infinite derivative and a uniform-z Simpson rule does not.
        listOf(t1eMeanField to 2.0, t1eDesCloizeaux to 2.25).forEach { (interaction, exponent) ->
            val sst = StrongStretchingLayer(interaction)
            val expected = strongStretchingFirstMomentRatio(exponent)
            val chain = t1ePeg.graftedChain(200.0, T1E_REFERENCE_DENSITY)
            val height = sst.equilibriumHeight(chain)
            val ratio = sst.firstMomentThickness(chain, height) / height
            assert(ratio.isCloseTo(expected, 1e-6))
        }
        assert(strongStretchingFirstMomentRatio(2.0).isCloseTo(0.75, 1e-12))
    }

    @Test
    fun `gate 2 limiting cases - the solved layer's first moment is below its force-onset height and its inverted chain is longer`() {
        // The two statements are one statement. The layer's bulk sits below the height at which it
        // FIRST resists, so 2<z> < L0; therefore a chain asked to put its first moment where the
        // force onset used to be has to be longer. Both directions are asserted because a sign
        // error in the moment quadrature would flip the second and not the first.
        val scf = SelfConsistentFieldLayer(t1eDesCloizeaux, t1eTestGrid, T1E_RESTING_PRESSURE)
        val onForce = scf.chainLengthAtRestingHeight(
            t1ePeg, T1E_REFERENCE_HEIGHT, T1E_REFERENCE_DENSITY
        )
        val chain = t1ePeg.graftedChain(onForce, T1E_REFERENCE_DENSITY)
        val moment = scf.restingFirstMomentThickness(chain)
        assert(moment < T1E_REFERENCE_HEIGHT)
        assert(moment > 0.4 * T1E_REFERENCE_HEIGHT)
        val onMoment = scf.chainLengthForFirstMomentThickness(
            t1ePeg, T1E_REFERENCE_HEIGHT, T1E_REFERENCE_DENSITY, seed = onForce
        )
        assert(onMoment > onForce)
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the accessor reproduces the solved profile's own first moment at departure zero`() {
        // T-1e adds a functional to a file it does not edit, so the standing quantity must come
        // back BIT-IDENTICAL rather than merely close: the two studies have to be reading the same
        // number for their conventions to be comparable field by field.
        val scf = SelfConsistentFieldLayer(t1eDesCloizeaux, t1eTestGrid, T1E_RESTING_PRESSURE)
        val chain = t1ePeg.graftedChain(80.0, T1E_REFERENCE_DENSITY)
        listOf(6.0, 9.0, 14.0).forEach { height ->
            val departure = scf.firstMomentThickness(chain, height) -
                    scf.profile(chain, height).firstMomentHeight
            assert(departure == 0.0)
        }
    }

    @Test
    fun `gate 3 conservation - a first moment is scale-free, so it is blind to the normalisation its denominator conserves`() {
        // 2<z> is a RATIO of two quadratures over the same profile, so multiplying phi by any
        // constant leaves it unchanged — which is exactly why a wrong overall normalisation is
        // invisible in it. For a pure power law the strong-stretching profile is self-similar, so
        // chains three times apart in length have the same shape at different amplitudes and must
        // return the same ratio; the coverage identity is asserted separately, because the first
        // moment cannot see it.
        val sst = StrongStretchingLayer(t1eDesCloizeaux)
        val ratios = listOf(100.0, 300.0, 900.0).map { length ->
            val chain = t1ePeg.graftedChain(length, T1E_REFERENCE_DENSITY)
            val height = sst.equilibriumHeight(chain)
            sst.firstMomentThickness(chain, height) / height
        }
        ratios.forEach { assert(it.isCloseTo(ratios.first(), 1e-9)) }
        // and the normalisation the ratio is blind to, asserted in its own right
        val scf = SelfConsistentFieldLayer(t1eDesCloizeaux, t1eTestGrid, T1E_RESTING_PRESSURE)
        val chain = t1ePeg.graftedChain(80.0, T1E_REFERENCE_DENSITY)
        val profile = scf.profile(chain, 9.0)
        assert(profile.coverage.isCloseTo(chain.coverage, 1e-9))
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the first-moment thickness converges in the node spacing at its own order`() {
        // A first moment is not a contact pressure and does not inherit its order. It is a ratio
        // of two quadratures over M = round(h/dz) nodes, and the node COUNT steps discontinuously
        // with the wall height (C-0073) — so the leading effect may or may not cancel between
        // numerator and denominator, and the only honest thing is to measure.
        val chain = t1ePeg.graftedChain(80.0, T1E_REFERENCE_DENSITY)
        val moments = listOf(0.4, 0.2, 0.1).map { spacing ->
            SelfConsistentFieldLayer(
                t1eDesCloizeaux,
                ScfDiscretisation(nodeSpacing = spacing, contourStepsPerMonomer = 2.0),
                T1E_RESTING_PRESSURE
            ).firstMomentThickness(chain, 9.0)
        }
        val coarse = abs(moments[1] - moments[0])
        val fine = abs(moments[2] - moments[1])
        val order = ln(coarse / fine) / ln(2.0)
        // at least first order, and the successive differences shrink — the two statements a
        // convergence gate owes, exhibited rather than asserted
        assert(fine < coarse)
        assert(order > 0.9)
    }

    @Test
    fun `gate 4 numerical convergence - the inverted chain length converges in the node spacing`() {
        // The deliverable is N, not 2<z>, and a root find can amplify what it is a root of. The
        // target is deliberately modest so that the finest grid stays affordable: the contour step
        // count goes as 1/dz^2 through the diffusion-number cap.
        val target = 4.0
        val lengths = listOf(0.4, 0.2, 0.1).map { spacing ->
            SelfConsistentFieldLayer(
                t1eDesCloizeaux,
                ScfDiscretisation(nodeSpacing = spacing, contourStepsPerMonomer = 2.0),
                T1E_RESTING_PRESSURE
            ).chainLengthForFirstMomentThickness(t1ePeg, target, 0.05)
        }
        val coarse = abs(lengths[1] - lengths[0])
        val fine = abs(lengths[2] - lengths[1])
        assert(fine < coarse)
        assert(ln(coarse / fine) / ln(2.0) > 0.9)
        // and the grid error at the production spacing is well inside the interaction bracket
        assert(abs(lengths[1] - lengths[2]) / lengths[2] < 0.02)
    }

    @Test
    fun `gate 4 numerical convergence - the first-moment thickness converges in the contour step`() {
        // The node spacing is deliberately COARSE here. `contourSteps` takes the larger of the
        // requested count and the count the Crank-Nicolson diffusion cap `r = D dn/(2 dz^2) <= 0.5`
        // demands, and at dz = 0.2 the cap alone asks for 130 steps on this chain — so requesting
        // 40 and 80 gives the SAME solve twice and a "convergence" test that measures nothing.
        // At dz = 0.4 the cap asks for 33 and the requested count is what varies.
        val chain = t1ePeg.graftedChain(80.0, T1E_REFERENCE_DENSITY)
        val moments = listOf(0.5, 1.0, 2.0).map { perMonomer ->
            SelfConsistentFieldLayer(
                t1eDesCloizeaux,
                ScfDiscretisation(nodeSpacing = 0.4, contourStepsPerMonomer = perMonomer),
                T1E_RESTING_PRESSURE
            ).firstMomentThickness(chain, 9.0)
        }
        val coarse = abs(moments[1] - moments[0])
        val fine = abs(moments[2] - moments[1])
        assert(fine < coarse)
        assert(ln(coarse / fine) / ln(2.0) > 0.9)
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature cross-check - the two trial-function models agree on the first moment and disagree on the edge`() {
        // The headline of this task in one assertion. In their OWN conventions the box and strong
        // stretching disagree about N by tens of per cent; read on the same functional they agree
        // to under two per cent, because what separates them is where each declares its profile to
        // stop and not how much polymer it holds.
        val box = AlexanderBoxLayer(t1eDesCloizeaux)
        val sst = StrongStretchingLayer(t1eDesCloizeaux)
        val onEdge = listOf(box, sst).map {
            it.chainLengthForHeight(t1ePeg, T1E_REFERENCE_HEIGHT, T1E_REFERENCE_DENSITY)
        }
        val onMoment = listOf(box, sst).map {
            it.chainLengthForFirstMomentThickness(
                t1ePeg, T1E_REFERENCE_HEIGHT, T1E_REFERENCE_DENSITY
            )
        }
        assert(abs(onEdge[0] / onEdge[1] - 1.0) > 0.2)
        assert(abs(onMoment[0] / onMoment[1] - 1.0) < 0.02)
    }

    @Test
    fun `gate 5 literature cross-check - the first moment reproduces C-0011's emitted value at the design point`() {
        // Read out of the committed result file rather than recalled, per SESSION-PROMPT. The
        // tolerance is the file's EMISSION precision — T-1d emits six significant digits — and
        // asserting tighter would be a test of the printed digits (C-0073).
        val emitted = emittedDesignPoint()
        val scf = SelfConsistentFieldLayer(
            t1eDesCloizeaux, t1eProductionGrid, T1E_RESTING_PRESSURE
        )
        val length = scf.chainLengthAtRestingHeight(
            t1ePeg, T1E_REFERENCE_HEIGHT, emitted.first
        )
        assert(length.isCloseTo(emitted.second, T1E_EMITTED_FIELD_SLACK))
        val moment = scf.restingFirstMomentThickness(
            t1ePeg.graftedChain(length, emitted.first)
        )
        assert(moment.isCloseTo(emitted.third, T1E_EMITTED_FIELD_SLACK))
    }

    /** `(σ, N, 2⟨z⟩)` of `T-1d`'s 10 nm des Cloizeaux response nearest `σ = 0.024 nm⁻²`. */
    private fun emittedDesignPoint(): Triple<Double, Double, Double> {
        val root = Json.parseToJsonElement(
            File("gpd/results/T-1d-scf-density-profile.json").readText()
        ).jsonObject
        val point = root.getValue("designPoints").jsonArray
            .map { it.jsonObject }
            .filter { it.getValue("layerHeight").jsonPrimitive.double == T1E_REFERENCE_HEIGHT }
            .minByOrNull {
                abs(
                    ln(
                        it.getValue("graftingDensity").jsonPrimitive.double /
                                T1E_REFERENCE_DENSITY
                    )
                )
            }!!
        val response = point.getValue("responses").jsonArray
            .map { it.jsonObject }
            .first {
                it.getValue("profile").jsonPrimitive.content == "scf" &&
                        it.getValue("interaction").jsonPrimitive.content == "des-Cloizeaux"
            }
        return Triple(
            point.getValue("graftingDensity").jsonPrimitive.double,
            response.getValue("monomersPerChain").jsonPrimitive.double,
            response.getValue("firstMomentHeight").jsonPrimitive.double
        )
    }

}
