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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

private val peg = PegWater()

/** §3: the 40 × 40 nm tile footprint. */
private const val TILE_AREA = 1600.0

/** `C-0003`'s `B = 2 A₂ M₀/V̄` from the measured `A₂ = 1.9e-3 mol·cm³/g²`. */
private val measuredSecondVirial = peg.reducedSecondVirialCoefficient(1.9e-3)

private val desCloizeaux = desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)

/** The Kuhn-segment reading of the correlation, which is the one every `b` formula needs. */
private val kuhnCorrelation = peg.edwardsCorrelation(measuredSecondVirial)

/** The same physics written on monomers — the convention check of `CH-0020`. */
private val monomerCorrelation = peg.monomerEdwardsCorrelation(measuredSecondVirial)

/** A grid coarse enough to keep the suite fast; the convergence gate is what justifies it. */
private val testGrid = ScfDiscretisation(nodeSpacing = 0.25, contourStepsPerMonomer = 1.0)

class FluctuationCorrectionTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the Ginzburg number should be invariant under the choice of segment`() {
        // A physical expansion parameter cannot depend on how the chain is chopped into segments.
        // Written on Kuhn segments it needs v_K = n_K^2 v_m; written on monomers it needs
        // b_m = b/sqrt(n_K). Getting the n_K^2 wrong shows up HERE and nowhere else, which is why
        // this gate exists at all — it is the executable form of CH-0020.
        listOf(0.001, 0.005, 0.009, 0.0246, 0.05, 0.2).forEach { volumeFraction ->
            assert(
                monomerCorrelation.ginzburgNumber(volumeFraction)
                    .isCloseTo(kuhnCorrelation.ginzburgNumber(volumeFraction), 1e-12)
            )
            assert(
                monomerCorrelation.screeningLength(volumeFraction)
                    .isCloseTo(kuhnCorrelation.screeningLength(volumeFraction), 1e-12)
            )
            assert(
                monomerCorrelation.oneLoopPressure(volumeFraction)
                    .isCloseTo(kuhnCorrelation.oneLoopPressure(volumeFraction), 1e-12)
            )
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the mean-field pressure should be C-0003's own two-body interaction`() {
        // The denominator of the Ginzburg ratio must be the SAME object T-1c and T-1d minimise
        // against, or the ratio compares two different theories rather than one theory with itself.
        val twoBody = twoBodyInteraction(measuredSecondVirial, peg.monomerVolume)
        listOf(0.005, 0.009, 0.05).forEach { volumeFraction ->
            assert(
                kuhnCorrelation.meanFieldPressure(volumeFraction)
                    .isCloseTo(twoBody.osmoticPressure(volumeFraction), 1e-12)
            )
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the excluded volume must coarse-grain quadratically`() {
        // v_K = n_K^2 v_m, because the interaction is (v/2) integral c^2 and c_K = c_m/n_K.
        val monomerExcludedVolume = measuredSecondVirial * peg.monomerVolume
        val nK = peg.monomersPerKuhnSegment
        assert(
            peg.kuhnExcludedVolume(measuredSecondVirial)
                .isCloseTo(nK * nK * monomerExcludedVolume, 1e-12)
        )
        // and PegWater.thermalBlobKuhnSegments coarse-grains it LINEARLY, so the two thermal-blob
        // counts differ by exactly n_K^2. Asserted, not narrated — this is CH-0020.
        val incumbent = peg.thermalBlobKuhnSegments(measuredSecondVirial)
        val corrected = peg.thermalBlobKuhnSegmentsCorrected(measuredSecondVirial)
        assert((incumbent / corrected).isCloseTo(nK * nK, 1e-12))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments should throw`() {
        assertFailsWith<IllegalArgumentException> { kuhnCorrelation.screeningLength(0.0) }
        assertFailsWith<IllegalArgumentException> { kuhnCorrelation.ginzburgNumber(-0.1) }
        assertFailsWith<IllegalArgumentException> { peg.kuhnExcludedVolume(0.0) }
        assertFailsWith<IllegalArgumentException> {
            ChainSwelling(kuhnLength = 1.1, kuhnExcludedVolume = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            ChainSwelling(kuhnLength = 1.1, kuhnExcludedVolume = 0.1).fixmanParameter(0.0)
        }
        assertFailsWith<IllegalArgumentException> { desCloizeaux.scaled(0.0) }
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - the Ginzburg number should fall as the inverse square root of the volume fraction`() {
        // Gi = (12)^(3/2)/(12 pi) sqrt(v/(c b^6)) exactly, so the log-log slope is -1/2 with no
        // correction term anywhere. That is what makes phi** a definition rather than a fitted knee.
        val low = kuhnCorrelation.ginzburgNumber(1e-4)
        val high = kuhnCorrelation.ginzburgNumber(1e-2)
        val slope = ln(high / low) / ln(1e-2 / 1e-4)
        assert(slope.isCloseTo(-0.5, 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - the Ginzburg volume fraction should be the root of Gi equals one`() {
        assert(
            kuhnCorrelation.ginzburgNumber(kuhnCorrelation.ginzburgVolumeFraction)
                .isCloseTo(1.0, 1e-12)
        )
    }

    @Test
    fun `gate 2 limiting cases - the one-loop pressure correction should be half the free energy correction`() {
        // Delta f goes as c^(3/2), so Pi = c f' - f collapses it by exactly one half. The factor is
        // invisible to any dimensional check and it is the difference between Gi = 1.3 and Gi = 2.6.
        listOf(0.002, 0.009, 0.04).forEach { volumeFraction ->
            assert(
                kuhnCorrelation.oneLoopPressure(volumeFraction)
                    .isCloseTo(0.5 * kuhnCorrelation.oneLoopFreeEnergyDensity(volumeFraction), 1e-12)
            )
            // and it is NEGATIVE: fluctuations reduce the pressure, as in Debye-Huckel
            assert(kuhnCorrelation.oneLoopPressure(volumeFraction) < 0.0)
        }
    }

    @Test
    fun `gate 2 limiting cases - chain swelling should vanish in a theta solvent and be bounded by the free chain`() {
        val excludedVolume = peg.kuhnExcludedVolume(measuredSecondVirial)
        val swelling = ChainSwelling(peg.kuhnLength, excludedVolume)
        val weak = ChainSwelling(peg.kuhnLength, excludedVolume * 1e-12)
        assert(weak.expansionFactor(20.0).isCloseTo(1.0, 1e-9))
        // screening can only remove swelling, never add it
        val screened = swelling.screenedExpansionFactor(20.0, screeningLength = 4.0)
        assert(screened <= swelling.expansionFactor(20.0))
        assert(screened >= 1.0)
        // an infinite screening length is the free chain, exactly
        assert(
            swelling.screenedExpansionFactor(20.0, screeningLength = 1e9)
                .isCloseTo(swelling.expansionFactor(20.0), 1e-12)
        )
    }

    @Test
    fun `gate 2 limiting cases - the interaction-free layer should be the limit of a vanishing interaction`() {
        // Falsifier 4: if the K -> 0 layer did not hold the tile up at all, the interaction channel
        // would be unbounded below and this task would have no floor to report.
        val faint = SelfConsistentFieldLayer(desCloizeaux.scaled(1e-4), testGrid)
        val fainter = SelfConsistentFieldLayer(desCloizeaux.scaled(1e-6), testGrid)
        val chain = peg.graftedChain(monomersPerChain = 100.0, graftingDensity = 0.024)
        val here = faint.equilibriumHeight(chain)
        val there = fainter.equilibriumHeight(chain)
        assert(here > 0.0)
        assert(here.isCloseTo(there, 5e-3))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - coil overlap should scale exactly as the square of the expansion factor`() {
        // C-0016's lower window edge is coil overlap Sigma = pi R0^2 sigma at every height, so a
        // swollen coil moves that edge by exactly 1/alpha^2. The propagation is an identity and is
        // asserted as one rather than recomputed.
        val alpha = 1.0839293331240791
        val swollen = peg.copy(kuhnLength = peg.kuhnLength * alpha)
        val plain = peg.graftedChain(monomersPerChain = 62.1, graftingDensity = 0.024)
        val swollenChain = swollen.graftedChain(monomersPerChain = 62.1, graftingDensity = 0.024)
        val overlap = PI * plain.idealEndToEnd * plain.idealEndToEnd * plain.graftingDensity
        val swollenOverlap = PI * swollenChain.idealEndToEnd * swollenChain.idealEndToEnd *
                swollenChain.graftingDensity
        assert((swollenOverlap / overlap).isCloseTo(alpha * alpha, 1e-12))
    }

    @Test
    fun `gate 3 symmetry - scaling an interaction should scale its pressure and leave its exponent alone`() {
        val scaled = desCloizeaux.scaled(3.0)
        assert(scaled.exponent == desCloizeaux.exponent)
        listOf(0.005, 0.05).forEach { volumeFraction ->
            assert(
                scaled.osmoticPressure(volumeFraction)
                    .isCloseTo(3.0 * desCloizeaux.osmoticPressure(volumeFraction), 1e-12)
            )
        }
    }

    @Test
    fun `gate 3 symmetry - the grafted coverage should be conserved under every perturbation run here`() {
        val chain = peg.graftedChain(monomersPerChain = 62.1, graftingDensity = 0.024)
        listOf(desCloizeaux.scaled(1e-4), desCloizeaux, desCloizeaux.scaled(2.0)).forEach {
            val profile = SelfConsistentFieldLayer(it, testGrid).profile(chain, 8.0)
            assert(profile.converged)
            assert(profile.coverage.isCloseTo(chain.coverage, 1e-9))
        }
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 convergence - the interaction sensitivity exponent should be grid-converged`() {
        // d ln k / d ln K measured on the solved layer, at two node spacings. If this moved with the
        // grid, falsifier 3's verdict would be a discretisation artefact rather than physics.
        val coarse = interactionSensitivityExponent(ScfDiscretisation(0.4, 1.0))
        val fine = interactionSensitivityExponent(ScfDiscretisation(0.2, 1.0))
        assert(abs(fine - coarse) < 0.02)
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature - the thermal blob should agree between the monomer and Kuhn conventions`() {
        // g_T in monomers, computed two ways: n_K (b^3/v_K)^2 and (b_m^3/v_m)^2. They agree only
        // when the excluded volume coarse-grains quadratically, and C-0003's 1222 Kuhn segments
        // fails this check by n_K^2.
        val monomerLength = peg.kuhnLength / sqrt(peg.monomersPerKuhnSegment)
        val monomerExcludedVolume = measuredSecondVirial * peg.monomerVolume
        val fromMonomers = (monomerLength.pow(3.0) / monomerExcludedVolume).pow(2.0)
        val fromKuhn = peg.thermalBlobKuhnSegmentsCorrected(measuredSecondVirial) *
                peg.monomersPerKuhnSegment
        assert(fromKuhn.isCloseTo(fromMonomers, 1e-9))
    }

    @Test
    fun `gate 5 literature - the pressure ratio should be the published multiple of the bare Ginzburg parameter`() {
        // Wittmer et al., arXiv:1107.4454 (J. Stat. Phys. 145, 1017) Eq. (48) defines the bare
        // Ginzburg parameter Gz, and Eq. (69) the free energy this file integrates. The three
        // ratios that follow are 4sqrt(3)/pi for the free energy, 2sqrt(3)/pi for the pressure,
        // and 3sqrt(3)/2pi for the inverse compressibility — and the LAST of those is their own
        // Eq. (99), checked there against bond-fluctuation simulation over three decades. It is
        // therefore the one number in this family with a published numerical verification, and
        // reproducing it from Delta f is the strongest available check on the whole construction.
        listOf(0.005, 0.009, 0.0246, 0.05).forEach { volumeFraction ->
            val bare = kuhnCorrelation.ginzburgParameter(volumeFraction)
            val mean = kuhnCorrelation.meanFieldPressure(volumeFraction)
            assert(
                (kuhnCorrelation.oneLoopFreeEnergyDensity(volumeFraction) / mean)
                    .isCloseTo(-4.0 * sqrt(3.0) / PI * bare, 1e-12)
            )
            assert(
                (kuhnCorrelation.oneLoopPressure(volumeFraction) / mean)
                    .isCloseTo(-2.0 * sqrt(3.0) / PI * bare, 1e-12)
            )
            assert(
                kuhnCorrelation.oneLoopCompressibilityCorrection(volumeFraction)
                    .isCloseTo(-1.5 * sqrt(3.0) / PI * bare, 1e-12)
            )
            // and the two Ginzburg readings differ by exactly that 2sqrt(3)/pi = 1.1027
            assert(
                kuhnCorrelation.ginzburgNumber(volumeFraction)
                    .isCloseTo(2.0 * sqrt(3.0) / PI * bare, 1e-12)
            )
        }
    }

    @Test
    fun `gate 5 literature - the thermal blob prefactor should be a convention worth 9 not a number`() {
        // CH-0020's second ground. C-0003's 1222 Kuhn segments is (b^3/(n_K v_m))^2 — the SCALING
        // normalisation with the excluded volume coarse-grained LINEARLY. Yamakawa's exact
        // normalisation z(g_T) = 1 with the excluded volume coarse-grained CORRECTLY gives a
        // number within a few per cent of it, because 1/0.32992^2 = 9.185 and n_K^2 = 9.672 are
        // nearly equal for this material. Two errors that nearly cancel is not a number that is
        // right; it is a convention doing a measurement's work.
        val swelling = ChainSwelling(peg.kuhnLength, peg.kuhnExcludedVolume(measuredSecondVirial))
        assert(swelling.fixmanParameter(swelling.thermalBlobKuhnSegments).isCloseTo(1.0, 1e-12))
        assert((1.0 / (FIXMAN_PREFACTOR * FIXMAN_PREFACTOR)).isCloseTo(9.187045, 1e-6))
        val nK = peg.monomersPerKuhnSegment
        assert((nK * nK).isCloseTo(9.671418, 1e-6))
        val incumbent = peg.thermalBlobKuhnSegments(measuredSecondVirial)
        assert(abs(incumbent / swelling.thermalBlobKuhnSegments - 1.0) < 0.06)
    }

    @Test
    fun `gate 5 literature - the Gen-1 layer should sit below the Ginzburg volume fraction`() {
        // The premise check, against PEG in water at 300 K rather than against a textbook. If the
        // layer sat ABOVE phi**, mean field would be controlled and this task would be a formality.
        // C-0011's solved mean volume fraction at the 10 nm design point is 0.00900.
        assert(kuhnCorrelation.ginzburgNumber(0.00900) > 1.0)
        assert(kuhnCorrelation.ginzburgVolumeFraction > 0.00900)
    }

    @Test
    fun `gate 5 literature - the Edwards screening length should be comparable to the coil at the design point`() {
        // The geometric statement of the same marginality: at phi = 0.009 the correlation length is
        // essentially the whole coil, so there is no scale separation for a semidilute description
        // to live in.
        val chain = peg.graftedChain(monomersPerChain = 62.1, graftingDensity = 0.024)
        val ratio = kuhnCorrelation.screeningLength(0.00900) / chain.idealEndToEnd
        assert(ratio > 0.5)
        assert(ratio < 2.0)
    }
}

/**
 * `d ln k(0.8 L₀)/d ln K` on the SOLVED layer at the 10 nm design point, by a symmetric difference
 * over a factor of two either way at fixed height and grafting density — exactly the sensitivity
 * `C-0003` derives as `1/(m+1)` for its two ansatz profiles.
 */
private fun interactionSensitivityExponent(grid: ScfDiscretisation): Double {
    val height = 10.0
    val graftingDensity = 0.024
    fun stiffness(scale: Double): Double {
        val layer = SelfConsistentFieldLayer(desCloizeaux.scaled(scale), grid)
        val length = layer.chainLengthAtRestingHeight(peg, height, graftingDensity)
        val chain = peg.graftedChain(length, graftingDensity)
        return layer.stiffness(chain, 0.8 * height, TILE_AREA)
    }
    return ln(stiffness(2.0) / stiffness(0.5)) / ln(4.0)
}
