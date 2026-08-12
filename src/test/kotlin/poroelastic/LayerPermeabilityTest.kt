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

package com.xemantic.nano.plentyofroom.poroelastic

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.material.PegWater
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The single most uncertain input to `T-7`: how permeable a 3 %-polymer PEG layer is.
 *
 * Three models are carried rather than one, because they disagree by a factor of forty
 * and the disagreement is *structural* — the segment-scale models resolve individual
 * Kuhn rods, the blob-scale model resolves correlation volumes, and at `φ/φ# ≈ 1`
 * (claim `C-0002`) those two descriptions of the same layer are not close.
 * The `T-7` bound is quoted from the slowest of them, so which one is right
 * changes the margin but not the verdict.
 */
class LayerPermeabilityTest {

    private val peg = PegWater()

    /** The `C-0001` design point at `L₀ = 10 nm`, `σ = 0.024 nm⁻²`: `N = 199.4`, `φ = 0.0289`. */
    private val designVolumeFraction = 0.0288872

    private val freeDraining = FreeDrainingSegments(
        segmentLength = peg.kuhnLength,
        segmentDiameter = peg.kuhnSegmentDiameter,
        segmentVolume = peg.kuhnSegmentVolume
    )

    private val fiberArray = FiberArrayPermeability(fiberRadius = peg.kuhnSegmentDiameter / 2.0)

    private val correlationLength = CorrelationLengthScreening(
        volumetricMonomerSize = peg.volumetricMonomerSize
    )

    private val models = listOf(freeDraining, fiberArray, correlationLength)

    @Test
    fun `should return a permeability whose square root is a length`() {
        // gate 1, dimensional consistency: Darcy permeability is an area, and its square
        // root is the hydrodynamic screening length that the Brinkman equation contains
        models.forEach { model ->
            val k = model.permeability(designVolumeFraction)
            assert(model.screeningLength(designVolumeFraction).isCloseTo(sqrt(k)))
        }
    }

    @Test
    fun `should diverge in the dilute limit and fall monotonically with volume fraction`() {
        // gate 2, limiting cases: pure water offers no resistance, and adding polymer
        // can only ever make the layer less permeable
        models.forEach { model ->
            val values = (0..30).map { model.permeability(1e-4 * 1.2.pow(it)) }
            assert(values.zipWithNext().all { (dilute, dense) -> dense < dilute })
            assert(model.permeability(1e-8) > model.permeability(1e-2))
        }
    }

    @Test
    fun `should give the correlation-length model the de Gennes exponent of minus three halves`() {
        // gate 3, an exact scaling symmetry rather than a fitted number:
        // xi ∝ φ^(-3/4) so k = xi^2 ∝ φ^(-3/2), for every φ and every prefactor.
        // Offeddu et al., AIP Adv. 8:105006 (2018), measure exactly this exponent on
        // PEG hydrogels: "a power relationship with exponent -1.5 is expected ... which
        // was again seen to apply to the materials" (R^2 = 0.96).
        assert(logDerivative(correlationLength, 0.03).isCloseTo(-1.5, 1e-6))
        assert(logDerivative(correlationLength, 0.30).isCloseTo(-1.5, 1e-6))
        assert(correlationLength.volumeFractionExponent.isCloseTo(-1.5))
    }

    @Test
    fun `should give the free-draining model an exponent of exactly minus one`() {
        // gate 3: without hydrodynamic interaction the drag per volume is simply the
        // segment number density times a fixed friction, so 1/k is linear in φ
        assert(logDerivative(freeDraining, 0.01).isCloseTo(-1.0, 1e-6))
        assert(logDerivative(freeDraining, 0.20).isCloseTo(-1.0, 1e-6))
    }

    @Test
    fun `should converge the log derivative under step refinement`() {
        // gate 4, numerical convergence: the central difference in log space must
        // approach the closed-form exponent quadratically in the step
        val coarse = abs(logDerivative(correlationLength, 0.03, 1e-2) + 1.5)
        val fine = abs(logDerivative(correlationLength, 0.03, 1e-3) + 1.5)
        assert(fine <= coarse / 50.0 + 1e-12)
    }

    @Test
    fun `should place the two segment-scale models within a factor of two of each other`() {
        // gate 5: the derived free-draining estimate and the cited fibrous-media
        // correlation are independent constructions of the same sub-nanometre screening
        // length; agreement to a factor of two is what makes either usable as a bound
        val free = freeDraining.permeability(designVolumeFraction)
        val fiber = fiberArray.permeability(designVolumeFraction)
        assert(free.isCloseTo(0.9719, relativeTolerance = 1e-3))
        assert(fiber.isCloseTo(0.7370, relativeTolerance = 1e-3))
        assert(free / fiber < 2.0)
    }

    @Test
    fun `should make the blob-scale screening length forty times more permeable`() {
        // The finding, not a check: the segment-scale and blob-scale pictures of the SAME
        // layer differ by a factor of 40 in permeability and 6.4 in screening length.
        // This is what CH-0001 predicts should happen at φ/φ# ≈ 1 — there are only a
        // few blobs per chain, so "monomer" and "blob" are not separated scales.
        val blob = correlationLength.permeability(designVolumeFraction)
        assert(blob.isCloseTo(31.337, relativeTolerance = 1e-3))
        assert((blob / freeDraining.permeability(designVolumeFraction)).isCloseTo(32.2, 1e-2))
    }

    @Test
    fun `should reconcile the correlation length with the Flory radius at coil overlap`() {
        // gate 5, and the justification for taking the prefactor of xi = v0^(1/3) φ^(-3/4)
        // as unity: at the overlap volume fraction φ* = N^(-4/5) the correlation length
        // must BE the coil size. For PEG the two constructions differ only by
        // v0^(1/3)/a = 0.392/0.35, i.e. 12 %, which is the prefactor's own uncertainty.
        val monomers = 199.44
        val overlap = monomers.pow(-4.0 / 5.0)
        val floryRadius = peg.effectiveMonomerLength * monomers.pow(0.6)
        val xi = correlationLength.screeningLength(overlap)
        assert((xi / floryRadius).isCloseTo(peg.volumetricMonomerSize / peg.effectiveMonomerLength))
        assert(abs(xi / floryRadius - 1.0) < 0.13)
    }

    @Test
    fun `should match the Alexander brush blob size to the grafting spacing`() {
        // gate 5, second anchor on the same prefactor: in the Alexander-de Gennes picture
        // the blob size of a brush IS the grafting spacing s = σ^(-1/2), and de Gennes
        // argues hydrodynamic and excluded-volume screening share that length.
        // At the design point s = 6.45 nm against xi = 5.60 nm — 13 % apart, from two
        // completely unrelated routes.
        val graftingSpacing = 1.0 / sqrt(0.024)
        val xi = correlationLength.screeningLength(designVolumeFraction)
        assert(graftingSpacing.isCloseTo(6.455, relativeTolerance = 1e-3))
        assert(xi.isCloseTo(5.598, relativeTolerance = 1e-3))
        assert(abs(xi / graftingSpacing - 1.0) < 0.15)
    }

    @Test
    fun `should bracket the measured hydrogel permeability range`() {
        // gate 5, against measurement rather than against theory: Gao & Cho
        // (arXiv:2209.14382, Table 1) measure the Darcy permeability of 21 hydrogels
        // across four polymer families at 2.8-23.1 x 10^-18 m^2, i.e. 2.8-23.1 nm^2.
        // Our blob-scale model lands inside that band at the working volume fractions
        // and our segment-scale models land an order of magnitude below it —
        // which is the direction a conservative bound has to err in.
        val measuredLow = 2.8
        val measuredHigh = 23.1
        val densest = correlationLength.permeability(0.0708)
        assert(densest > measuredLow && densest < measuredHigh)
        assert(freeDraining.permeability(0.0708) < measuredLow)
        assert(fiberArray.permeability(0.0708) < measuredLow)
    }

    @Test
    fun `should reject a volume fraction the fibre correlation cannot represent`() {
        // the Jackson-James logarithm changes sign at φ = exp(-0.931) = 0.394;
        // above it the correlation returns a negative permeability, so it is refused
        // rather than returned to a downstream task
        assert(FiberArrayPermeability.DILUTE_LIMIT.isCloseTo(0.3941594, 1e-6))
        assertFailsWith<IllegalArgumentException> {
            fiberArray.permeability(0.5)
        } should {
            have(
                message == "volumeFraction must be below " +
                        "${FiberArrayPermeability.DILUTE_LIMIT} for the fibre correlation, " +
                        "was: 0.5"
            )
        }
    }

    @Test
    fun `should reject an unphysical volume fraction`() {
        models.forEach { model ->
            assertFailsWith<IllegalArgumentException> { model.permeability(0.0) }
            assertFailsWith<IllegalArgumentException> { model.permeability(1.5) }
        }
    }

    private fun logDerivative(
        model: LayerPermeability,
        volumeFraction: Double,
        step: Double = 1e-4
    ): Double = (
            ln(model.permeability(volumeFraction * (1.0 + step))) -
                    ln(model.permeability(volumeFraction * (1.0 - step)))
            ) / (ln(1.0 + step) - ln(1.0 - step))

}
