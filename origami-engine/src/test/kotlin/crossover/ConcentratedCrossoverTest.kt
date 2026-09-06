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

package com.xemantic.nano.plentyofroom.crossover

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.brush.kuhnExcludedVolume
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.thermalBlobKuhnSegmentsCorrected
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.material.WATER_MASS_DENSITY_AT_300K
import com.xemantic.nano.plentyofroom.material.correlationBlobSize
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-21` — the **upper** crossover of the semidilute regime, derived for PEG in water at 300 K
 * from `C-0002`'s own measured parameters instead of read off a cited `0.2–0.3` band.
 *
 * The five gates are named in the test names. Gate 3 is the one that decides the task:
 * `φ* over φ** = √(g_T/N_K)` exactly, so the des Cloizeaux window is non-empty **iff** the chain is
 * longer than a thermal blob — and for every Gen-1 chain it is not.
 */
class ConcentratedCrossoverTest {

    private val peg = PegWater()

    /** `C-0003`'s measured osmotic second virial coefficient `A₂`, in mol·cm³/g². */
    private val secondVirial = 1.9e-3

    private val correlation = peg.semidiluteCorrelation(secondVirial)

    // -------------------------------------------------------------- gate 1, dimensional

    @Test
    fun `should give a correlation length in nm that scales as one over phi on the ideal branch`() {
        // gate 1: xi_theta = v_K / (b^2 phi) — a volume over a length squared is a length,
        // and the product xi*phi is a constant of the material.
        val product = (0.01..0.5 step 7).map { correlation.idealBlobSize(it) * it }
        product.forEach { assert(it.isCloseTo(product.first(), relativeTolerance = 1e-12)) }
        assert(product.first().isCloseTo(peg.kuhnSegmentVolume / (peg.kuhnLength * peg.kuhnLength)))
    }

    @Test
    fun `should count segments per blob as a dimensionless square of the crossover ratio`() {
        // gate 1: n(phi) = (v_K / (b^3 phi))^2, dimensionless, and the inverse of phiForSegments
        listOf(1.0, 5.0, 126.3, 1160.0).forEach { n ->
            val phi = correlation.volumeFractionAtSegmentsPerBlob(n)
            assert(correlation.segmentsPerBlob(phi).isCloseTo(n, relativeTolerance = 1e-12))
        }
    }

    @Test
    fun `should reject unphysical arguments`() {
        assertFailsWith<IllegalArgumentException> { correlation.idealBlobSize(0.0) }
        assertFailsWith<IllegalArgumentException> { correlation.segmentsPerBlob(-0.1) }
        assertFailsWith<IllegalArgumentException> { correlation.volumeFractionAtSegmentsPerBlob(0.0) }
        assertFailsWith<IllegalArgumentException> { weightToVolumeFraction(1.1, 1.21, 0.997) }
    }

    // -------------------------------------------------------------- gate 2, limiting cases

    @Test
    fun `should cross the two correlation branches exactly where the blob is thermal`() {
        // gate 2: the swollen and the unswollen branch are the same length at, and only at,
        // the volume fraction whose correlation blob holds g_T segments.
        val gT = correlation.thermalBlobSegments
        val phi = correlation.volumeFractionAtSegmentsPerBlob(gT)
        assert(
            correlation.idealBlobSize(phi)
                .isCloseTo(correlation.swollenBlobSize(phi), relativeTolerance = 1e-12)
        )
        // and the crossing length is the thermal blob itself
        assert(
            correlation.idealBlobSize(phi)
                .isCloseTo(peg.kuhnLength * sqrt(gT), relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `should return the textbook unity crossover for a space filling athermal segment`() {
        // gate 2 and gate 5 at once, and it is the premise check the whole task rests on:
        // the textbook statement "the semidilute regime extends to phi ~ 1" is the special case
        // v = v_K = b^3. PEG is not that polymer — its Kuhn segment is a thin rod, b^3/v_K = 7.09 —
        // and that ratio IS the whole departure from the textbook number.
        val athermal = SemidiluteCorrelation(
            kuhnLength = 1.0,
            kuhnSegmentVolume = 1.0,
            kuhnPairExcludedVolume = 1.0,
            monomerVolume = 1.0
        )
        assert(athermal.thermalBlobSegments.isCloseTo(1.0))
        assert(athermal.excludedVolumeCrossover.isCloseTo(1.0))
        assert(athermal.segmentPerBlobCrossover.isCloseTo(1.0))
    }

    @Test
    fun `should put the one segment per blob crossover at the reciprocal Kuhn aspect ratio`() {
        // gate 2: phi_c(n = 1) = v_K/b^3, i.e. exactly 1/7.09 for PEG — C-0002's own derived
        // aspect ratio, inverted. Nothing else enters it.
        assert(
            correlation.segmentPerBlobCrossover
                .isCloseTo(1.0 / peg.kuhnSegmentAspectRatio, relativeTolerance = 1e-12)
        )
        assert(correlation.segmentPerBlobCrossover.isCloseTo(0.1411, relativeTolerance = 1e-3))
        // and at that volume fraction the correlation length IS the Kuhn length
        assert(
            correlation.idealBlobSize(correlation.segmentPerBlobCrossover)
                .isCloseTo(peg.kuhnLength, relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `should place the excluded volume crossover far below the layer's own volume fraction`() {
        // gate 2: the derived upper crossover of the des Cloizeaux limb, in BOTH thermal-blob
        // conventions, sits below 0.013 — an order of magnitude under the cited 0.2-0.3 band
        // and below every volume fraction the Gen-1 layer occupies.
        assert(correlation.excludedVolumeCrossover.isCloseTo(0.01254, relativeTolerance = 2e-3))
        assert(correlation.exact.excludedVolumeCrossover.isCloseTo(0.004139, relativeTolerance = 2e-3))
        assert(correlation.exact.excludedVolumeCrossover < correlation.excludedVolumeCrossover)
    }

    // -------------------------------------------------------------- gate 3, the identity

    @Test
    fun `should make the des Cloizeaux window width exactly the square root of the blob ratio`() {
        // gate 3: phi* / phi** = sqrt(g_T / N_K), EXACTLY, for any material and any chain length.
        // Proved as an identity rather than observed: phi* = (v_K/b^3) N_K^(-1/2) is the ideal-coil
        // overlap fraction and phi** = (v_K/b^3) g_T^(-1/2) the thermal-blob crossover, so the
        // material prefactor cancels and only the two segment counts survive.
        listOf(4.0, 32.0, 72.3, 500.0, 4000.0).forEach { kuhnSegments ->
            val window = correlation.desCloizeauxWindow(kuhnSegments)
            assert(
                (window.lower / window.upper)
                    .isCloseTo(sqrt(correlation.thermalBlobSegments / kuhnSegments), 1e-12)
            )
            assert(window.widthRatio.isCloseTo(window.upper / window.lower, 1e-12))
            assert(window.isEmpty == (kuhnSegments < correlation.thermalBlobSegments))
        }
    }

    @Test
    fun `should find the des Cloizeaux window empty at every Gen-1 chain length`() {
        // gate 3, applied: C-0002's design points run N = 100-225 monomers, i.e. N_K = 32-72
        // Kuhn segments, against g_T = 126 in the SCALING convention and 1160 in Yamakawa's.
        // The window is empty in both, so the exponent the layer is "entitled to" is never 9/4.
        listOf(100.0, 120.0, 199.44, 225.0).forEach { monomers ->
            val kuhn = monomers / peg.monomersPerKuhnSegment
            assert(correlation.desCloizeauxWindow(kuhn).isEmpty)
            assert(correlation.exact.desCloizeauxWindow(kuhn).isEmpty)
        }
    }

    @Test
    fun `should agree with the independently derived corrected thermal blob count`() {
        // gate 3, conservation of the upstream number: this class must not carry its own
        // thermal blob. CH-0020's corrected count is the one it uses.
        assert(
            correlation.thermalBlobSegments.isCloseTo(
                peg.thermalBlobKuhnSegmentsCorrected(
                    peg.reducedSecondVirialCoefficient(secondVirial)
                ),
                relativeTolerance = 1e-12
            )
        )
        assert(
            correlation.kuhnPairExcludedVolume
                .isCloseTo(
                    peg.kuhnExcludedVolume(peg.reducedSecondVirialCoefficient(secondVirial)),
                    relativeTolerance = 1e-12
                )
        )
    }

    // -------------------------------------------------------------- gate 4, convergence

    @Test
    fun `should reproduce the closed form crossover by bisection on the two branches`() {
        // gate 4: locate the branch crossing numerically and check it against the closed form.
        var low = 1e-6
        var high = 1.0
        repeat(200) {
            val mid = sqrt(low * high)
            val difference = correlation.idealBlobSize(mid) - correlation.swollenBlobSize(mid)
            // the ideal branch is the LARGER of the two below the crossing
            if (difference > 0.0) low = mid else high = mid
        }
        assert(sqrt(low * high).isCloseTo(correlation.excludedVolumeCrossover, 1e-9))
    }

    @Test
    fun `should recover the two branch log slopes by Richardson extrapolation`() {
        // gate 4: d ln xi / d ln phi is exactly -1 on the ideal branch and -3/4 on the swollen
        // one, recovered here by a central difference in log-phi refined twice.
        val phi = 0.05
        fun slope(f: (Double) -> Double, step: Double): Double =
            (ln(f(phi * kotlin.math.exp(step))) - ln(f(phi / kotlin.math.exp(step)))) / (2.0 * step)
        listOf(
            (correlation::idealBlobSize) to -1.0,
            (correlation::swollenBlobSize) to -0.75
        ).forEach { (f, expected) ->
            val coarse = slope(f, 1e-2)
            val fine = slope(f, 5e-3)
            val richardson = (4.0 * fine - coarse) / 3.0
            assert(abs(richardson - expected) < 1e-12)
            // both branches are EXACT power laws, so a central difference in log-phi carries no
            // truncation error at all: coarse and fine agree with the analytic slope to roundoff,
            // and the Richardson combination cannot improve on that. Asserting the exactness is
            // the informative statement; asserting that the fine step is the better of two
            // roundoff-limited numbers is not, and it fails at the fifteenth digit.
            assert(abs(coarse - expected) < 1e-12)
            assert(abs(fine - expected) < 1e-12)
        }
    }

    // -------------------------------------------------------------- gate 5, cross-checks

    @Test
    fun `should reproduce the independently written reduced convention blob size`() {
        // gate 5: `material.correlationBlobSize` was written for P-9 in the REDUCED convention
        // (phi = c b^3). This class is in the PHYSICAL one (phi = c v_K). They must agree once
        // the b^3/v_K = 7.09 conversion is applied — and that conversion is the single most
        // likely way to get this material wrong, so it is asserted rather than assumed.
        listOf(0.005, 0.03, 0.1, 0.3).forEach { physical ->
            val reduced = physical * peg.kuhnSegmentAspectRatio
            assert(
                correlation.swollenBlobSize(physical).isCloseTo(
                    correlationBlobSize(reduced, peg.kuhnLength, correlation.kuhnPairExcludedVolume),
                    relativeTolerance = 1e-12
                )
            )
        }
    }

    @Test
    fun `should convert a weight fraction into a physical volume fraction and back`() {
        // gate 5: the equation of state's own fitted range is quoted in weight percent, and the
        // ceiling C-0018 consumes is a VOLUME fraction. The conversion inverts exactly.
        val density = peg.massDensity
        val water = WATER_MASS_DENSITY_AT_300K
        listOf(0.05, 0.3, 0.5, 0.9).forEach { weight ->
            val phi = weightToVolumeFraction(weight, density, water)
            assert(volumeToWeightFraction(phi, density, water).isCloseTo(weight, 1e-12))
        }
        assert(weightToVolumeFraction(0.5, density, water).isCloseTo(0.45122, 1e-4))
        assert(weightToVolumeFraction(1.0, density, water).isCloseTo(1.0, 1e-12))
    }

    @Test
    fun `should place the mis coarse grained monomer crossover on top of the cited band`() {
        // gate 5, and the finding that explains where 0.2 came from: reading the thermal-blob
        // criterion on MONOMERS instead of Kuhn segments — v_m/v0, which is what C-0007's
        // parameter sheet reports — gives 0.203, i.e. the floor of the cited 0.2-0.3 band, and
        // it is wrong by n_K^2 (b^3/v_K)... in short, by a factor of 16 against the Kuhn reading.
        val monomerLevel = correlation.monomerLevelCrossover
        assert(monomerLevel.isCloseTo(0.2029, relativeTolerance = 2e-3))
        assert(
            (monomerLevel / correlation.excludedVolumeCrossover)
                .isCloseTo(16.18, relativeTolerance = 2e-3)
        )
    }

    @Test
    fun `should keep the monomer to Kuhn crossover ratio independent of the excluded volume`() {
        // gate 5, and the ground of the challenge this task raises: the factor by which reading
        // the thermal-blob criterion on MONOMERS overstates it is b^6/(v0 v_K n_K^2) = 16.17,
        // and it does not depend on the excluded volume at all — so it applies unchanged to the
        // osmometry route (v_m = 0.01225) and to the Flory-Huggins route (v_m = 0.03114) alike.
        listOf(0.005, 0.01225, 0.03114, 0.08).forEach { monomerExcludedVolume ->
            val c = peg.semidiluteCorrelationFromExcludedVolume(monomerExcludedVolume)
            assert(
                (c.monomerLevelCrossover / c.excludedVolumeCrossover)
                    .isCloseTo(16.172, relativeTolerance = 1e-3)
            )
        }
    }

    @Test
    fun `should agree between the osmotic and the explicit excluded volume constructors`() {
        // gate 1: the two ways of building the same object are the same object.
        val explicit = peg.semidiluteCorrelationFromExcludedVolume(
            peg.reducedSecondVirialCoefficient(secondVirial) * peg.monomerVolume
        )
        assert(
            explicit.kuhnPairExcludedVolume
                .isCloseTo(correlation.kuhnPairExcludedVolume, relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `should find the des Cloizeaux window non-empty in exactly one of the four corners`() {
        // gate 2, and the honesty check the claim rests on: the window's emptiness is NOT robust
        // across the 2 x 2 of (thermal-blob normalisation) x (excluded-volume route). With the
        // Flory-Huggins excluded volume in the SCALING normalisation g_T falls to ~20, below
        // every Gen-1 chain, and the window EXISTS. It is still not reached, because the layer's
        // own volume fraction is above its upper edge — but that is a different argument and it
        // must be made rather than assumed.
        val longest = 225.0 / peg.monomersPerKuhnSegment
        val shortest = 60.0 / peg.monomersPerKuhnSegment
        val osmotic = correlation
        val floryHuggins = peg.semidiluteCorrelationFromExcludedVolume(0.03114)
        val corners = listOf(
            osmotic to true, osmotic.exact to true,
            floryHuggins.exact to true, floryHuggins to false
        )
        corners.forEach { (c, expectedEmptyAtTheLongestChain) ->
            assert(c.desCloizeauxWindow(longest).isEmpty == expectedEmptyAtTheLongestChain)
            // the shortest chain is emptier than the longest in every corner
            assert(c.desCloizeauxWindow(shortest).widthRatio < c.desCloizeauxWindow(longest).widthRatio)
        }
    }

    @Test
    fun `should order the whole crossover family monotonically in its own segment count`() {
        // gate 5: phi_c(n) is strictly decreasing in n, so the family is ordered by how many
        // segments the definition insists the correlation blob keeps. Naming n IS naming the
        // convention, which is what CLAUDE.md asks a crossover to carry.
        val counts = listOf(1.0, 2.0, 10.0, 126.3, 1160.0)
        val values = counts.map { correlation.volumeFractionAtSegmentsPerBlob(it) }
        assert(values.zipWithNext().all { (a, b) -> a > b })
        assert(values.first().isCloseTo(correlation.segmentPerBlobCrossover, 1e-12))
    }

    // -------------------------------------------------------------- the layer, not the solution

    @Test
    fun `should carry no chain length in the upper crossover at all`() {
        // P3: the LOWER crossover is a solution property that grafting removes (CH-0001/CH-0002);
        // the upper one contains no N, exactly as C-0019's Ginzburg parameter contains none, so a
        // grafted layer inherits it unchanged at its own local volume fraction. Executable form:
        // nothing in phi_c depends on a chain, which is asserted by construction here.
        val short = correlation.desCloizeauxWindow(10.0)
        val long = correlation.desCloizeauxWindow(10_000.0)
        assert(short.upper.isCloseTo(long.upper, relativeTolerance = 1e-12))
        assert(short.lower > long.lower)
    }

    @Test
    fun `should report a layer gap at which a stated crossover is reached`() {
        // P4's only coupling to the electrostatics: the crossover enters C-0018 exclusively as
        // the gap h_c = dry thickness / phi_c, which is monotone decreasing in phi_c.
        val dry = 0.5422
        assert(gapAtVolumeFraction(dry, 0.2).isCloseTo(2.711, relativeTolerance = 1e-3))
        assert(gapAtVolumeFraction(dry, 0.4) < gapAtVolumeFraction(dry, 0.2))
        assertFailsWith<IllegalArgumentException> { gapAtVolumeFraction(dry, 0.0) }
    }
}

private infix fun ClosedFloatingPointRange<Double>.step(count: Int): List<Double> =
    (0 until count).map { start + (endInclusive - start) * it / (count - 1.0) }
