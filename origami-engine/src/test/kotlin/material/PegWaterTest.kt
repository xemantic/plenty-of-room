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
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.brush.brushOfHeight
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test

/**
 * The PEG/water parameter sheet of task `P-3`.
 *
 * Every assertion here either derives a number `C-0001` had to cite,
 * or pins the ratio between two quantities that the literature writes with the same letter.
 */
class PegWaterTest {

    private val peg = PegWater()

    @Test
    fun `should derive the monomer molar mass rather than cite it`() {
        assert(peg.monomerMolarMass.isCloseTo(44.053, relativeTolerance = 1e-4))
    }

    @Test
    fun `should derive the monomer volume from the measured partial specific volume`() {
        // 0.0604 nm^3 per ethylene oxide unit
        assert(peg.monomerVolume.isCloseTo(0.060350, relativeTolerance = 1e-4))
    }

    @Test
    fun `should derive a hydrated mass density above that of bulk amorphous PEO`() {
        // gate 5, literature cross-check with a twist: the partial specific volume in water
        // implies 1.212 g/cm^3, while bulk amorphous PEO is 1.12-1.13 g/cm^3. That 8% gap is
        // not an error — PEG contracts on hydration — but it means "the monomer volume"
        // is medium-dependent, and the hydrated value is the one an aqueous brush needs.
        assert(peg.massDensity.isCloseTo(1.21212, relativeTolerance = 1e-4))
        assert(peg.massDensity > 1.13)
    }

    @Test
    fun `should corroborate the cited effective monomer length against derived molecular geometry`() {
        // acceptance predicate (b): a = 0.35 nm was cited by C-0001 and flagged.
        // The all-trans contour length of the ethylene oxide unit, derived from bond
        // geometry alone, is 0.364 nm — 4% away, and inside the 0.330-0.356 nm band that
        // Hansen et al. (2003) obtain from unconstrained fits of the Alexander-de Gennes
        // form to PEG-brush compression. Cited, derived and fitted agree.
        assert(peg.allTransContourLength.isCloseTo(0.36395, relativeTolerance = 1e-3))
        assert(
            abs(peg.allTransContourLength - peg.effectiveMonomerLength) /
                    peg.effectiveMonomerLength < 0.10
        )
    }

    @Test
    fun `should separate the effective monomer length from the volumetric monomer size`() {
        // The trap this task exists to disarm. The Alexander-de Gennes effective monomer
        // length is a *contour* length; the volumetric monomer size is the cube root of the
        // monomer's actual volume. They differ by 12%, and their cubes — which is how they
        // enter a volume fraction — differ by 41%.
        assert(peg.volumetricMonomerSize.isCloseTo(0.392252, relativeTolerance = 1e-4))
        assert(peg.volumeFractionCorrection.isCloseTo(1.40758, relativeTolerance = 1e-4))
    }

    @Test
    fun `should show the Kuhn segment is not space-filling`() {
        // and the third quantity written with the same letter. The Kuhn segment carries
        // 3.11 monomers and is 1.1 nm long, but occupies only 0.188 nm^3: b^3 is seven
        // times its volume. A Kuhn segment of PEG is a thin rod, so no single-parameter
        // scaling picture can be right about its length and its volume at once.
        assert(peg.monomersPerKuhnSegment.isCloseTo(3.10989, relativeTolerance = 1e-4))
        assert(peg.kuhnSegmentVolume.isCloseTo(0.187683, relativeTolerance = 1e-4))
        assert(peg.kuhnSegmentAspectRatio.isCloseTo(7.09176, relativeTolerance = 1e-4))
        assert(peg.kuhnSegmentDiameter.isCloseTo(0.466096, relativeTolerance = 1e-4))
    }

    @Test
    fun `should place the working temperature well below the theta temperature`() {
        // PEG/water phase-separates near 375 K, so at 300 K the solvent is good —
        // but only by 20% in reduced temperature, which is why the excluded volume
        // is weak enough for the crossover to reach our operating point at all.
        assert(peg.reducedTemperature(ROOM_TEMPERATURE).isCloseTo(0.2))
        assert(peg.reducedTemperature(peg.thetaTemperature).isCloseTo(0.0))
    }

    @Test
    fun `should compute the physical volume fraction of the layer from the monomer volume`() {
        // the surviving T-1 design point: L0 = 10 nm, sigma = 0.024 nm^-2, N = 199.44
        assert(
            peg.volumeFraction(
                monomersPerChain = 199.44,
                graftingDensity = 0.024,
                layerHeight = 10.0
            ).isCloseTo(0.0288872, relativeTolerance = 1e-4)
        )
    }

    @Test
    fun `should hand the layer an equation of state carrying its own chain length`() {
        val eos = peg.equationOfState(monomersPerChain = 199.44)
        assert(eos.monomerVolume == peg.monomerVolume)
        assert(eos.crossoverIndex == peg.crossoverIndex)
        assert(eos.crossoverVolumeFraction.isCloseTo(0.025586, relativeTolerance = 1e-3))
    }

    @Test
    fun `should put the brush-onset convention at a fixed point of the measured crossover`() {
        // gate 3, symmetry — and the answer to task P-5. At fixed reduced grafting density
        // the ratio phi/phi# is *independent of layer height and chain length*:
        //
        //   Sigma = pi * L0^(6/5) * sigma^(3/5)     (the effective monomer length cancels)
        //   phi   = v0 * sigma^(2/3) / a^(5/3)      (independent of L0)
        //   phi#  = (alpha N)^(-4/5)
        //
        // so at Sigma = 5 every PEG layer, of any thickness, lands at the same point of the
        // equation of state. That makes the Sigma >= 5 convention a *material* statement,
        // and the statement it makes is "you are at the crossover" — not "you are semidilute".
        fun ratioAtBrushOnset(layerHeight: Double): Double {
            val density = (5.0 / (PI * layerHeight.pow(6.0 / 5.0))).pow(5.0 / 3.0)
            val brush = brushOfHeight(layerHeight, density, peg.effectiveMonomerLength)
            val phi = peg.volumeFraction(brush.monomersPerChain, density, layerHeight)
            return phi / peg.equationOfState(brush.monomersPerChain).crossoverVolumeFraction
        }
        assert(ratioAtBrushOnset(5.0).isCloseTo(ratioAtBrushOnset(10.0), 1e-9))
        assert(ratioAtBrushOnset(7.0).isCloseTo(ratioAtBrushOnset(25.0), 1e-9))
        assert(ratioAtBrushOnset(10.0).isCloseTo(1.085, relativeTolerance = 3e-3))
        // and it is nowhere near the des Cloizeaux domain, which begins at 5
        assert(ratioAtBrushOnset(10.0) < ScalingEquationOfState.DES_CLOIZEAUX_DOMAIN)
    }

    @Test
    fun `should discharge the chain-tension premise at the design point`() {
        // acceptance predicate (f). §2 of the problem definition reports that chain tension
        // above ~30 pN degrades PEG's solvent quality, and notes it is "within a factor of two
        // of the tension in a densely grafted brush". At our design point it is not:
        // the 100 pN target force is shared by 38 chains, and the brush's own stretching
        // tension adds 1.6 pN, for 4.2 pN total — a factor of 7 of margin.
        val applied = tensionPerChain(force = 100.0, graftingDensity = 0.024, area = 1600.0)
        assert(applied.isCloseTo(2.604167, relativeTolerance = 1e-4))
        val stretching = peg.stretchingTension(monomersPerChain = 199.44, extension = 10.0)
        assert(stretching.isCloseTo(1.601303, relativeTolerance = 1e-3))
        assert(applied + stretching < 30.0 / 5.0)
    }

    @Test
    fun `should keep the brush's own tension below the premise even at melt-like grafting`() {
        // gate 2, limiting case. The brush's intrinsic stretching tension is
        // 3 k_BT * (monomers per Kuhn segment) * a^(5/3) * sigma^(1/3) / b^2 —
        // independent of chain length, and only a cube root in the grafting density.
        // A 10 nm layer grafted at sigma = 1 nm^-2, which is melt-like and far denser than
        // anything §4(a) would tolerate, still pulls each chain with only 5.6 pN.
        // The 30 pN premise therefore cannot be violated by grafting density in this system.
        val meltLike = peg.stretchingTension(monomersPerChain = 57.53, extension = 10.0)
        assert(meltLike.isCloseTo(5.5513, relativeTolerance = 1e-3))
        assert(meltLike < 6.0)
    }

    @Test
    fun `should report the applied force at which the tension premise would bind`() {
        // the useful form of the same statement: the premise binds at a force per chain of
        // 30 pN, which over the 40 x 40 nm tile at the design grafting density is 1.1 nN —
        // eleven times the §3 target force. The premise is not near.
        val binding = (30.0 - peg.stretchingTension(199.44, 10.0)) * 0.024 * 1600.0
        assert(binding.isCloseTo(1090.7, relativeTolerance = 1e-3))
        assert(binding > 10.0 * 100.0)
    }

}
