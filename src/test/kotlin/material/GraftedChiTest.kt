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

package com.xemantic.nano.plentyofroom.material

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Task `P-9` — is the effective `χ` of a *grafted* PEG layer the bulk one?
 *
 * Two fits to two different grafted PEO layers are reconstructed here, and the whole point of the
 * file is that **they are not the same kind of object**:
 *
 * - [ScfBrushChiFit] is a `χ` fitted *inside* a self-consistent field model, and it is only
 *   interpretable against **that model's own theta point**. Lee et al.'s model puts its theta at
 *   `χ = 0.696`, not at `½`, and the two ways of moving its `χ` onto the Flory-Huggins axis
 *   disagree — which is the executable statement that the parameter does not transfer.
 * - [AlexanderDeGennesBrushFit] is a `χ` inferred from a *measured compression isotherm* of a
 *   grafted layer through a form whose only repulsion is excluded volume. That one does transfer,
 *   because the same convention was fitted to bulk osmometry in the same paper.
 *
 * The assertions are arranged as the five gates, and the ones that decide the task are the
 * limiting cases: a des Cloizeaux amplitude is a **positive** power of a **positive** excluded
 * volume, so a `χ` at or above `½` has no representation in it at all.
 */
class GraftedChiTest {

    private val peg = PegWater()
    private val waterSite = waterMoleculeVolume()
    private val bulkChi = ReciprocalTemperatureChi().chi()

    /**
     * Lee, Kim, Witte, Ohn, Choi, Akgun, Satija & Won, *J. Phys. Chem. B* **116**:7367 (2012),
     * Table/§3.3 — the two area-per-chain conditions at which the `χ` fit was performed.
     */
    private val lee = listOf(
        ScfBrushChiFit(
            label = "Lee et al. 2012, alpha = 1350 A^2/chain",
            areaPerChain = 1350.0,
            fittedChi = 0.789,
            fittedChiUncertainty = 0.066,
            modelThetaChi = 0.696
        ),
        ScfBrushChiFit(
            label = "Lee et al. 2012, alpha = 2200 A^2/chain",
            areaPerChain = 2200.0,
            fittedChi = 0.852,
            fittedChiUncertainty = 0.051,
            modelThetaChi = 0.696
        )
    )

    /**
     * Hansen, Cohen, Podgornik & Parsegian, *Biophys. J.* **84**:350 (2003), Fig. 3 — the two
     * unconstrained two-parameter Alexander-de Gennes fits to the DSPC:PEG-5000 osmotic-stress
     * data of Kenworthy et al., the only data in that literature that met their brush criterion.
     */
    private val hansen = listOf(
        AlexanderDeGennesBrushFit(
            label = "Hansen et al. 2003, nominal f = 0.10",
            fittedMonomerLength = 3.56,
            fittedMonomerLengthUncertainty = 0.07,
            restingHeight = 105.0
        ),
        AlexanderDeGennesBrushFit(
            label = "Hansen et al. 2003, nominal f = 0.20",
            fittedMonomerLength = 3.30,
            fittedMonomerLengthUncertainty = 0.15,
            restingHeight = 109.0
        )
    )

    // ------------------------------------------------------------- gate 1: units and dimensions

    @Test
    fun `should convert an area per chain into a grafting density in inverse square nanometres`() {
        // 1350 A^2/chain = 13.50 nm^2/chain. The only unit conversion Lee et al. need.
        assert(lee[0].graftingDensity.isCloseTo(100.0 / 1350.0, relativeTolerance = 1e-12))
        assert(lee[1].graftingDensity.isCloseTo(100.0 / 2200.0, relativeTolerance = 1e-12))
        assert(lee[0].graftingDensity.isCloseTo(0.074074, relativeTolerance = 1e-5))
        assert(lee[1].graftingDensity.isCloseTo(0.045455, relativeTolerance = 1e-5))
    }

    @Test
    fun `should keep the reduced grafting density dimensionless`() {
        // Sigma = sigma * pi * Rg^2 is nm^-2 * nm^2. Scaling every length by a factor must
        // therefore leave it unchanged — a dimensional check run as an experiment.
        val scale = 1.7
        val plain = lee[0].reducedGraftingDensity
        val scaled = lee[0].copy(
            areaPerChain = 1350.0 * scale.pow(2.0),
            radiusOfGyration = lee[0].radiusOfGyration * scale
        ).reducedGraftingDensity
        assert(scaled.isCloseTo(plain, relativeTolerance = 1e-12))
    }

    @Test
    fun `should keep the Alexander-de Gennes grafting spacing a length`() {
        // D = (N a^(5/3) / L0)^(3/2) is (nm^(5/3) / nm)^(3/2) = nm. Scale a and L0 together
        // and D must scale by the same factor.
        val scale = 1.7
        val plain = hansen[0].graftingSpacing
        val scaled = hansen[0].copy(
            fittedMonomerLength = 3.56 * scale,
            restingHeight = 105.0 * scale
        ).graftingSpacing
        assert(scaled.isCloseTo(plain * scale, relativeTolerance = 1e-12))
    }

    // ----------------------------------------------- the des Cloizeaux amplitude, derived not cited

    @Test
    fun `should derive the amplitude exponent of the effective monomer length rather than assert it`() {
        // In the Alexander-de Gennes convention a single length does all three jobs, so
        //     Pi = alpha (kT/a^3) phi^(9/4),  phi = n a^3   =>   Pi = alpha kT n^(9/4) a^(15/4).
        // At fixed PHYSICAL number density the des Cloizeaux amplitude therefore goes as a^(15/4).
        // Verified as a log-slope of the two-step composition, not read off the algebra.
        val density = 1.5
        fun pressure(a: Double) = 0.8 * (1.0 / a.pow(3.0)) * (density * a.pow(3.0)).pow(2.25)
        val slope = ln(pressure(3.6) / pressure(3.4)) / ln(3.6 / 3.4)
        assert(slope.isCloseTo(DES_CLOIZEAUX_AMPLITUDE_EXPONENT, relativeTolerance = 1e-9))
        assert(DES_CLOIZEAUX_AMPLITUDE_EXPONENT.isCloseTo(15.0 / 4.0, relativeTolerance = 1e-15))
    }

    @Test
    fun `should derive the excluded volume exponent of the des Cloizeaux amplitude from the blob`() {
        // The correlation blob is space-filling and self-avoiding inside, which fixes
        //     xi = b (v/b^3)^(-1/4) phi^(-3/4)   and   Pi = kT/xi^3 ∝ v^(3/4) phi^(9/4).
        // The 3/4 is what converts an interaction-strength ratio into an excluded-volume ratio,
        // so it is derived here rather than quoted.
        val slope = ln(
            desCloizeauxPressureFromBlob(0.03, peg.kuhnLength, 0.0622) /
                    desCloizeauxPressureFromBlob(0.03, peg.kuhnLength, 0.0311)
        ) / ln(2.0)
        assert(slope.isCloseTo(DES_CLOIZEAUX_EXCLUDED_VOLUME_EXPONENT, relativeTolerance = 1e-12))
        // and the 9/4 in the volume fraction comes out of the same construction
        val phiSlope = ln(
            desCloizeauxPressureFromBlob(0.06, peg.kuhnLength, 0.0311) /
                    desCloizeauxPressureFromBlob(0.03, peg.kuhnLength, 0.0311)
        ) / ln(2.0)
        assert(phiSlope.isCloseTo(2.25, relativeTolerance = 1e-12))
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `should return the bulk chi when the brush interaction equals the bulk one`() {
        listOf(DES_CLOIZEAUX_EXCLUDED_VOLUME_EXPONENT, MEAN_FIELD_EXCLUDED_VOLUME_EXPONENT)
            .forEach { exponent ->
                val same = effectiveChiFromInteractionRatio(
                    interactionRatio = 1.0,
                    bulkChi = bulkChi,
                    excludedVolumeExponent = exponent
                )
                assert(same.isCloseTo(bulkChi, relativeTolerance = 1e-12))
            }
    }

    @Test
    fun `should never reach the theta point from any positive interaction ratio`() {
        // THE decisive limiting case. A des Cloizeaux amplitude is a positive power of a positive
        // excluded volume, so no positive interaction strength — however small — can represent a
        // chi at or above 1/2. "Poor solvent" is not a value this family of free energies has.
        listOf(1e-6, 0.01, 0.5, 1.0, 100.0).forEach { ratio ->
            val chi = effectiveChiFromInteractionRatio(ratio, bulkChi)
            assert(chi < THETA_CHI)
        }
        // and theta is a LIMIT, approached only as the interaction vanishes. Below a ratio of
        // about 1e-12 the difference stops being representable in a double and the value rounds
        // onto 1/2 — which is the floating-point statement of the same physics, not a violation
        // of it, and is asserted as such rather than with a strict inequality (see CLAUDE.md on
        // not asserting exact equilibrium values in floating point).
        assert(effectiveChiFromInteractionRatio(1e-30, bulkChi).isCloseTo(THETA_CHI, 1e-12))
        assert(effectiveChiFromInteractionRatio(1e-12, bulkChi) <= THETA_CHI)
        // monotone: a weaker interaction is always a poorer effective solvent
        val ladder = listOf(0.1, 0.5, 1.0, 2.0, 10.0).map {
            effectiveChiFromInteractionRatio(it, bulkChi)
        }
        assert(ladder.zipWithNext().all { (a, b) -> b < a })
    }

    @Test
    fun `should refuse to invert a chi at or above the theta point into an interaction ratio`() {
        // The same statement from the other side, and it is the reason P-9 closes: the value in
        // circulation for a grafted layer (chi ~ 0.60) has NO representation as an Alexander-de
        // Gennes fit at all. There is no effective monomer length that produces it.
        assertFailsWith<IllegalArgumentException> {
            interactionRatioFromEffectiveChi(effectiveChi = 0.612, bulkChi = bulkChi)
        }
        assertFailsWith<IllegalArgumentException> {
            interactionRatioFromEffectiveChi(effectiveChi = THETA_CHI, bulkChi = bulkChi)
        }
        // just below theta it is representable, and the interaction is nearly extinguished
        val barely = interactionRatioFromEffectiveChi(effectiveChi = 0.4999, bulkChi = bulkChi)
        assert(barely > 0.0 && barely < 0.01)
    }

    @Test
    fun `should leave the layer response unmoved when the interaction is unmoved`() {
        assert(stiffnessRatioFromInteractionRatio(1.0).isCloseTo(1.0, relativeTolerance = 1e-15))
        assert(strokeRatioFromInteractionRatio(1.0).isCloseTo(1.0, relativeTolerance = 1e-15))
    }

    // --------------------------------------------- gate 3: symmetry — the transfers are reciprocal

    @Test
    fun `should invert the effective chi map exactly`() {
        listOf(0.35, 0.802, 1.0, 1.0658, 3.0).forEach { ratio ->
            val chi = effectiveChiFromInteractionRatio(ratio, bulkChi)
            assert(interactionRatioFromEffectiveChi(chi, bulkChi).isCloseTo(ratio, 1e-12))
        }
    }

    @Test
    fun `should make the stiffness and stroke exposures reciprocal in the interaction ratio`() {
        // k ∝ K^(+1/(m+1)) and the stroke ∝ K^(negative): halving and doubling the interaction
        // must give exactly reciprocal factors, because both are pure power laws (C-0003).
        listOf(2.0, 4.0, 16.0).forEach { span ->
            assert(
                (stiffnessRatioFromInteractionRatio(span) *
                        stiffnessRatioFromInteractionRatio(1.0 / span))
                    .isCloseTo(1.0, relativeTolerance = 1e-12)
            )
            assert(
                (strokeRatioFromInteractionRatio(span) *
                        strokeRatioFromInteractionRatio(1.0 / span))
                    .isCloseTo(1.0, relativeTolerance = 1e-12)
            )
        }
    }

    // ------------------------------------------------- gate 4: convergence of the reconstruction

    @Test
    fun `should reproduce the grafting spacing by bisection on the height relation`() {
        // D is closed form here, but the relation it inverts (L0 = N a^(5/3) D^(-2/3)) is the one
        // C-0003 replaced, so the inversion is checked numerically rather than trusted.
        val fit = hansen[0]
        var low = 1.0
        var high = 1000.0
        repeat(200) {
            val mid = 0.5 * (low + high)
            val height = fit.monomersPerChain * fit.fittedMonomerLength.pow(5.0 / 3.0) /
                    mid.pow(2.0 / 3.0)
            if (height > fit.restingHeight) low = mid else high = mid
        }
        assert((0.5 * (low + high)).isCloseTo(fit.graftingSpacing, relativeTolerance = 1e-9))
    }

    @Test
    fun `should recover the volume fraction from the grafting density and the height`() {
        // phi = N sigma v0 / L0 is the conservation statement — polymer volume per unit area is
        // fixed by the grafting. Reaching it by a second route catches a slipped factor of 10
        // between angstroms and nanometres, which is the only real hazard in this reconstruction.
        hansen.forEach { fit ->
            val viaInventory = fit.monomersPerChain * fit.graftingDensity * peg.monomerVolume /
                    (fit.restingHeight / 10.0)
            assert(
                fit.physicalVolumeFraction(peg.monomerVolume)
                    .isCloseTo(viaInventory, relativeTolerance = 1e-12)
            )
        }
    }

    // ----------------------------------------- the SCF fit: a parameter of a model, not of PEG

    @Test
    fun `should read the SCF chi against the model's own theta and not against one half`() {
        // The paper's own quantity. 0.852 / 0.696 = 1.22 is the "~1.2" of the abstract; against
        // the Flory-Huggins 1/2 the same number would read 1.70, which is not what was reported.
        assert(lee[1].chiRatioToModelTheta.isCloseTo(1.224138, relativeTolerance = 1e-5))
        assert(lee[0].chiRatioToModelTheta.isCloseTo(1.133621, relativeTolerance = 1e-5))
        assert(lee[1].chiPastModelTheta.isCloseTo(0.156, relativeTolerance = 1e-12))
        assert(lee[0].chiPastModelTheta.isCloseTo(0.093, relativeTolerance = 1e-12))
    }

    @Test
    fun `should show the two transfers onto the Flory-Huggins axis disagreeing`() {
        // There are exactly two defensible linear ways to carry a chi off a model whose theta sits
        // at 0.696 and onto an axis whose theta sits at 1/2: preserve the RATIO to theta, or
        // preserve the DISTANCE past theta. They are not the same map, and the gap between them
        // is the size of the non-transferability.
        assert(lee[1].floryHugginsByRatio.isCloseTo(0.612069, relativeTolerance = 1e-5))
        assert(lee[1].floryHugginsByOffset.isCloseTo(0.656, relativeTolerance = 1e-12))
        assert(lee[0].floryHugginsByRatio.isCloseTo(0.566810, relativeTolerance = 1e-5))
        assert(lee[0].floryHugginsByOffset.isCloseTo(0.593, relativeTolerance = 1e-12))
        // the spread across both conditions and both transfers
        val all = lee.flatMap { listOf(it.floryHugginsByRatio, it.floryHugginsByOffset) }
        assert((all.max() - all.min()).isCloseTo(0.089190, relativeTolerance = 1e-4))
        // which is 39% of the shift the ratio transfer alone would claim against the bulk chi
        assert(((all.max() - all.min()) / (0.612069 - bulkChi)).isCloseTo(0.37100, 1e-3))
    }

    @Test
    fun `should confirm the SCF model's segment volumes against the independently derived ones`() {
        // gate 5, and it is the one that establishes the 0.696 as a CONVENTION rather than a fit
        // artefact: Lee et al.'s model carries v_PEO = 59.2 A^3 and v_water = 29.9 A^3, a ratio of
        // 1.980, against the 2.010 that C-0007 derives from the partial specific volume and the
        // mass density of water without reference to that paper. Same lattice trap, 1.5% apart.
        assert(lee[0].modelSiteVolumeRatio.isCloseTo(1.979933, relativeTolerance = 1e-5))
        assert(
            (abs(lee[0].modelSiteVolumeRatio - peg.monomerVolume / waterSite) /
                    (peg.monomerVolume / waterSite)) < 0.02
        )
    }

    @Test
    fun `should place the SCF fit inside the Gen-1 grafting window and not above it`() {
        // The escape route that is NOT available. The paper's title says "densely grafted", but
        // both of its chi conditions sit inside the Gen-1 window of 0.018-0.092 nm^-2, and its
        // chain length of 113 monomers sits inside the Gen-1 60-375. P-9 cannot be closed by
        // saying the system is the wrong one; it has to be closed on the parameter itself.
        lee.forEach { fit ->
            assert(fit.graftingDensity in GEN1_GRAFTING_DENSITY_LOW..GEN1_GRAFTING_DENSITY_HIGH)
            assert(fit.monomersPerChain in 60.0..375.0)
        }
        // and by the paper's own reduced measure it is at coil overlap, not dense: Sigma ~ 1
        assert(lee[0].reducedGraftingDensity.isCloseTo(1.537030, relativeTolerance = 1e-5))
        assert(lee[1].reducedGraftingDensity.isCloseTo(0.943178, relativeTolerance = 1e-5))
        assert(lee.all { it.reducedGraftingDensity < 5.0 })
    }

    // ------------------------------- the compression fit: the independent, right-geometry bound

    @Test
    fun `should reconstruct the grafting densities of the compression-fitted brushes`() {
        assert(hansen[0].graftingSpacing.isCloseTo(26.69677, relativeTolerance = 1e-5))
        assert(hansen[0].graftingDensity.isCloseTo(0.1403080, relativeTolerance = 1e-5))
        assert(hansen[1].graftingSpacing.isCloseTo(20.88155, relativeTolerance = 1e-5))
        assert(hansen[1].graftingDensity.isCloseTo(0.2293372, relativeTolerance = 1e-5))
        // both ABOVE the Gen-1 window, which is what makes this a conservative test: if a
        // brush-specific many-body attraction switches on with density, it bites hardest here
        assert(hansen.all { it.graftingDensity > GEN1_GRAFTING_DENSITY_HIGH })
        // at 1.7-5.0x the Gen-1 volume fraction, and at the Gen-1 chain length and height
        assert(hansen[0].physicalVolumeFraction(peg.monomerVolume).isCloseTo(0.0911277, 1e-5))
        assert(hansen[1].physicalVolumeFraction(peg.monomerVolume).isCloseTo(0.1434845, 1e-5))
        assert(hansen.all { it.restingHeight / 10.0 in 10.0..11.0 })
    }

    @Test
    fun `should bound the brush interaction strength against the bulk one`() {
        // The fits hold alpha at the value fitted to BULK osmometry in the same paper and let the
        // effective monomer length float; it lands within 2% and 6% of the bulk/structural 3.5 A.
        assert(hansen[0].interactionStrengthRatio().isCloseTo(1.065816, relativeTolerance = 1e-5))
        assert(hansen[1].interactionStrengthRatio().isCloseTo(0.801996, relativeTolerance = 1e-5))
        // and across both coverages with their 1-sigma bands
        val band = hansen.flatMap { it.interactionStrengthRatioBand() }
        assert(band.min().isCloseTo(0.6736114, relativeTolerance = 1e-5))
        assert(band.max().isCloseTo(1.1465544, relativeTolerance = 1e-5))
    }

    @Test
    fun `should put the compression-fitted effective chi within 0 053 of the bulk value`() {
        // The answer to P-9's cheap bound. Every one of these is a chi inferred from a MEASURED
        // normal-compression isotherm of a grafted PEG layer, through a form whose only repulsion
        // is excluded volume, on the convention its own bulk fit was made in.
        assert(hansen[0].effectiveChi(bulkChi).isCloseTo(0.3602830, relativeTolerance = 1e-5))
        assert(hansen[1].effectiveChi(bulkChi).isCloseTo(0.4043755, relativeTolerance = 1e-5))
        val band = hansen.flatMap { it.effectiveChiBand(bulkChi) }
        assert(band.min().isCloseTo(0.3459959, relativeTolerance = 1e-5))
        assert(band.max().isCloseTo(0.4242205, relativeTolerance = 1e-5))
        // |d chi| <= 0.053, against the 0.228 the ratio transfer of the SCF fit would claim
        assert(band.maxOf { abs(it - bulkChi) } < 0.053)
        assert(band.all { it < THETA_CHI })
    }

    @Test
    fun `should keep the compression bound below the shift claimed for the SCF fit`() {
        val compression = hansen.flatMap { it.effectiveChiBand(bulkChi) }
            .maxOf { abs(it - bulkChi) }
        val scfRatioTransfer = lee.maxOf { abs(it.floryHugginsByRatio - bulkChi) }
        assert(scfRatioTransfer / compression > 4.0)
        assert(scfRatioTransfer.isCloseTo(0.240402, relativeTolerance = 1e-4))
    }

    @Test
    fun `should report the density trend the compression fits do contain`() {
        // Honest reading: the denser of the two layers IS the poorer solvent, the same SIGN as the
        // SCF result, and the two 1-sigma bands only just fail to overlap. Attributing the whole
        // drift in a to solvent quality is an UPPER bound — Hansen et al. call the two values
        // "nearly constant" and treat the difference as fit scatter.
        val sparse = hansen[0].effectiveChi(bulkChi)
        val dense = hansen[1].effectiveChi(bulkChi)
        assert(dense > sparse)
        assert((dense - sparse).isCloseTo(0.0440925, relativeTolerance = 1e-4))
        assert(hansen[0].effectiveChiBand(bulkChi).max() < hansen[1].effectiveChiBand(bulkChi).min())
    }

    // ------------------------------------------------- the exposure, propagated through C-0003

    @Test
    fun `should propagate the interaction bound into the stiffness and the stroke`() {
        // C-0003, exactly: k ∝ K^(1/(m+1)) and the chain length a specified height demands moves
        // against the interaction. For the des Cloizeaux exponent 1/(m+1) = 4/13.
        assert(DES_CLOIZEAUX_STIFFNESS_EXPONENT.isCloseTo(4.0 / 13.0, relativeTolerance = 1e-15))
        val band = hansen.flatMap { it.interactionStrengthRatioBand() }
        val stiffness = band.map { stiffnessRatioFromInteractionRatio(it) }
        val stroke = band.map { strokeRatioFromInteractionRatio(it) }
        assert(stiffness.min().isCloseTo(0.8855292, relativeTolerance = 1e-5))
        assert(stiffness.max().isCloseTo(1.0429783, relativeTolerance = 1e-5))
        assert(stroke.min().isCloseTo(0.9861604, relativeTolerance = 1e-5))
        assert(stroke.max().isCloseTo(1.0410831, relativeTolerance = 1e-5))
        // -11.4% / +4.3% in stiffness and -1.4% / +4.1% in stroke: inside C-0003's own six-model
        // bracket at 10 nm (3.83-6.01 nm, i.e. +/-22% about its midpoint), so nothing moves
        assert(stroke.all { abs(it - 1.0) < 0.22 })
    }

    @Test
    fun `should recover C-0003's own sixteen-fold sensitivity study`() {
        // gate 5 against a standing claim: C-0003 scaled the interaction free energy over a 16x
        // range at the 10 nm design point and reported the stroke moving 5.81 -> 4.38 nm. The
        // stroke exponent carried here is defined by exactly that pair, so it must reproduce it.
        assert(strokeRatioFromInteractionRatio(16.0).isCloseTo(4.38 / 5.81, relativeTolerance = 1e-9))
        // and the same 16x is only 2.35x in stiffness — which C-0003 reports independently, as
        // k(0.8 L0) moving from 7.58 to 17.79 pN/nm. Two numbers we did not fit reproducing the
        // exponent to four figures is the strongest cross-check available on this exposure.
        assert(stiffnessRatioFromInteractionRatio(16.0).isCloseTo(16.0.pow(4.0 / 13.0), 1e-12))
        assert(stiffnessRatioFromInteractionRatio(16.0).isCloseTo(17.79 / 7.58, relativeTolerance = 1e-3))
    }

    // ---------------------------------------------------------------------- guards on the inputs

    @Test
    fun `should reject a fit that could not have been made`() {
        assertFailsWith<IllegalArgumentException> {
            ScfBrushChiFit("bad", areaPerChain = 0.0, fittedChi = 0.8, modelThetaChi = 0.696)
        }
        assertFailsWith<IllegalArgumentException> {
            ScfBrushChiFit("bad", areaPerChain = 1350.0, fittedChi = 0.8, modelThetaChi = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            AlexanderDeGennesBrushFit("bad", fittedMonomerLength = 0.0, restingHeight = 105.0)
        }
        assertFailsWith<IllegalArgumentException> {
            AlexanderDeGennesBrushFit("bad", fittedMonomerLength = 3.5, restingHeight = -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            desCloizeauxPressureFromBlob(0.03, kuhnLength = 1.1, excludedVolume = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            effectiveChiFromInteractionRatio(interactionRatio = -1.0, bulkChi = 0.372)
        }
        assertFailsWith<IllegalArgumentException> {
            // a bulk chi already at theta leaves nothing to scale
            effectiveChiFromInteractionRatio(interactionRatio = 1.0, bulkChi = THETA_CHI)
        }
    }
}
