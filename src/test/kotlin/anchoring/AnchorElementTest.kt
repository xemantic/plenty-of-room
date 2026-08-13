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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-12` / leaf `A8.2` — the mechanics of one anchor element.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem
 * definition. The elements are textbook closed forms; what is *not* textbook, and what
 * these tests exist to pin down, is which closed form belongs to which end condition —
 * clamped-pinned and clamped-guided differ by exactly 4 in transverse stiffness and by
 * exactly 4 in buckling load, and an origami-to-substrate joint is not obviously either.
 */
class AnchorElementTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - a transverse beam stiffness should be a rigidity over a cubed length`() {
        // EI in pN.nm^2 over L^3 in nm^3 is pN/nm: doubling the length must divide by exactly 8
        val short = beamTransverseStiffness(230.0, 10.0, BeamEndCondition.PINNED_HEAD)
        val long = beamTransverseStiffness(230.0, 20.0, BeamEndCondition.PINNED_HEAD)
        assert((short / long).isCloseTo(8.0))
        // and quadrupling the rigidity must multiply by exactly 4
        assert(
            (beamTransverseStiffness(920.0, 10.0, BeamEndCondition.PINNED_HEAD) / short)
                .isCloseTo(4.0)
        )
    }

    @Test
    fun `gate 1 dimensional consistency - an axial rod stiffness should be a modulus over a length`() {
        assert(rodAxialStiffness(1100.0, 10.0).isCloseTo(110.0))
        assert(rodAxialStiffness(1100.0, 20.0).isCloseTo(55.0))
    }

    @Test
    fun `gate 1 dimensional consistency - a buckling load should be a rigidity over a squared length`() {
        val load = eulerBucklingLoad(230.0, 10.0, BeamEndCondition.GUIDED_HEAD)
        assert(load.isCloseTo(PI * PI * 230.0 / 100.0))
        assert((load / eulerBucklingLoad(230.0, 20.0, BeamEndCondition.GUIDED_HEAD)).isCloseTo(4.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the entropic tether stiffness should be an energy over an area`() {
        // 3 k_BT/(L_c b) has units of pN.nm / nm^2 = pN/nm, so doubling either length halves it
        val chain = FreelyJointedChain(contourLength = 20.0, kuhnLength = 1.5)
        val longer = FreelyJointedChain(contourLength = 40.0, kuhnLength = 1.5)
        val stiffer = FreelyJointedChain(contourLength = 20.0, kuhnLength = 0.75)
        assert((chain.gaussianStiffness / longer.gaussianStiffness).isCloseTo(2.0))
        assert((stiffer.gaussianStiffness / chain.gaussianStiffness).isCloseTo(2.0))
        assert(chain.gaussianStiffness.isCloseTo(3.0 * thermalEnergy() / (20.0 * 1.5)))
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - the two end conditions should differ by exactly four in both stiffness and buckling`() {
        val pinned = beamTransverseStiffness(230.0, 10.0, BeamEndCondition.PINNED_HEAD)
        val guided = beamTransverseStiffness(230.0, 10.0, BeamEndCondition.GUIDED_HEAD)
        assert((guided / pinned).isCloseTo(4.0))
        val pinnedLoad = eulerBucklingLoad(230.0, 10.0, BeamEndCondition.PINNED_HEAD)
        val guidedLoad = eulerBucklingLoad(230.0, 10.0, BeamEndCondition.GUIDED_HEAD)
        assert((guidedLoad / pinnedLoad).isCloseTo(4.0))
    }

    @Test
    fun `gate 2 limiting cases - a bundle should reduce to its helix count when the helices are coincident`() {
        // with every helix on the neutral axis the parallel-axis term vanishes and only n EI survives
        val coincident = bundleBendingRigidity(
            offsets = List(4) { 0.0 },
            helixBendingRigidity = 230.0,
            stretchModulus = 1100.0
        )
        assert(coincident.isCloseTo(4.0 * 230.0))
        // and separating them adds exactly S times the second moment of the offsets
        val square = bundleBendingRigidity(
            offsets = listOf(-1.345, -1.345, 1.345, 1.345),
            helixBendingRigidity = 230.0,
            stretchModulus = 1100.0
        )
        assert(square.isCloseTo(4.0 * 230.0 + 1100.0 * 4.0 * 1.345 * 1.345))
    }

    @Test
    fun `gate 2 limiting cases - a compressed strut should lose its transverse stiffness exactly at its buckling load`() {
        val unloaded = beamTransverseStiffness(230.0, 10.0, BeamEndCondition.GUIDED_HEAD)
        val critical = eulerBucklingLoad(230.0, 10.0, BeamEndCondition.GUIDED_HEAD)
        assert(compressedTransverseStiffness(unloaded, 0.0, critical).isCloseTo(unloaded))
        assert(compressedTransverseStiffness(unloaded, critical, critical).isCloseTo(0.0))
        assert(
            compressedTransverseStiffness(unloaded, critical / 2.0, critical)
                .isCloseTo(unloaded / 2.0)
        )
        // past the critical load the strut has buckled: the linear model is left, not extrapolated
        assertFailsWith<IllegalArgumentException> {
            compressedTransverseStiffness(unloaded, critical * 1.001, critical)
        }
    }

    @Test
    fun `gate 2 limiting cases - the freely jointed chain should be Gaussian at low force and taut at high force`() {
        val chain = FreelyJointedChain(contourLength = 40.0, kuhnLength = 1.5)
        // low force: x -> f L_c b/(3 k_BT), i.e. the tangent stiffness is the Gaussian one
        assert(chain.tangentStiffness(1e-4).isCloseTo(chain.gaussianStiffness, 1e-6))
        assert(chain.transverseStiffness(1e-4).isCloseTo(chain.gaussianStiffness, 1e-6))
        // high force: the extension approaches but never reaches the contour length
        assert(chain.tension(39.0) > chain.tension(30.0))
        assert(chain.extension(1e6) < chain.contourLength)
        assert(chain.extension(1e6) > 0.999 * chain.contourLength)
    }

    @Test
    fun `gate 2 limiting cases - the cable term should vanish as the cube of the offset`() {
        // an in-plane tether lifted by delta stretches by delta^2/2L, so its normal force is
        // S delta^3/(2 L^3) — a purely geometric stiffening that is zero at zero stroke
        val stiffness = 1100.0
        val length = 20.0
        assert(cableNormalForce(stiffness, length, 0.0).isCloseTo(0.0))
        val small = cableNormalForce(stiffness, length, 0.01)
        assert(small.isCloseTo(stiffness * 1e-6 / (2.0 * 8000.0), 1e-4))
        // and it is strongly nonlinear: tripling the offset multiplies the force by ~27
        val ratio = cableNormalForce(stiffness, length, 0.03) / small
        assert(ratio > 26.9 && ratio < 27.1)
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the chain force-extension law should invert to itself`() {
        val chain = FreelyJointedChain(contourLength = 40.0, kuhnLength = 1.5)
        listOf(0.01, 0.1, 1.0, 5.0, 20.0).forEach { force ->
            assert(chain.tension(chain.extension(force)).isCloseTo(force, 1e-9))
        }
    }

    @Test
    fun `gate 3 conservation - the cable tension and its normal force should satisfy the geometry exactly`() {
        // F_z = T sin(alpha) with sin(alpha) = delta/sqrt(L^2+delta^2): a statics identity,
        // and it is checked against the two quantities computed independently
        val stiffness = 1100.0
        val length = 20.0
        listOf(0.5, 1.0, 3.0, 6.0).forEach { offset ->
            val tension = cableTension(stiffness, length, offset)
            val force = cableNormalForce(stiffness, length, offset)
            val sine = offset / sqrt(length * length + offset * offset)
            assert(force.isCloseTo(tension * sine))
        }
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the chain inversion should converge to the bracket floor`() {
        val chain = FreelyJointedChain(contourLength = 40.0, kuhnLength = 1.5)
        // the inversion is a bisection on a monotone function, so its error is bounded by the
        // final bracket width rather than by a residual — the failure mode CLAUDE.md records
        listOf(0.001, 1.0, 39.9).forEach { extension ->
            val force = chain.tension(extension)
            assert(chain.extension(force).isCloseTo(extension, 1e-9))
        }
    }

    @Test
    fun `gate 4 numerical convergence - the chain should be smooth through the small-argument series`() {
        val chain = FreelyJointedChain(contourLength = 40.0, kuhnLength = 1.5)
        // the Langevin function loses every digit to cancellation below u ~ 1e-2 and is
        // series-expanded there; the join must not be visible in the tangent stiffness, which
        // is monotone NON-DECREASING in the force because a chain strain-stiffens
        val forces = listOf(1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 1e-1, 1.0)
        val stiffnesses = forces.map { chain.tangentStiffness(it) }
        stiffnesses.zipWithNext { first, second ->
            assert(second >= first * (1.0 - 1e-12))
            assert(second < first * 2.0)
        }
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature cross-check - the C-0010 duplex strut bracket should be reproduced exactly`() {
        // C-0010 quotes 3EI/L^3 = 0.69 pN/nm at 10 nm and 0.08625 pN/nm at 20 nm on the CanDo
        // EI = 230 pN.nm^2. Reproduced from EI and L rather than accepted.
        assert(
            beamTransverseStiffness(230.0, 10.0, BeamEndCondition.PINNED_HEAD).isCloseTo(0.69)
        )
        assert(
            beamTransverseStiffness(230.0, 20.0, BeamEndCondition.PINNED_HEAD).isCloseTo(0.08625)
        )
    }

    @Test
    fun `gate 5 literature cross-check - the CanDo rigidity should be the persistence length times k_BT`() {
        // EI = L_p k_BT is the only place the two are related; CanDo's 230 pN.nm^2 implies
        // L_p = 55.5 nm, against the ~40 nm measured with Mg2+ (Wang et al. 1997)
        assert((230.0 / thermalEnergy()).isCloseTo(55.5, 2e-3))
        assert(AnchorMaterials.MAGNESIUM_BENDING_RIGIDITY.isCloseTo(40.0 * thermalEnergy()))
    }

    @Test
    fun `gate 5 literature cross-check - the ssDNA Kuhn bracket should be method-systematic not noise`() {
        // Force spectroscopy in real MgCl2 (Bosco/Ritort 2014, fits over 10-40 pN) and
        // zero-force scattering (Chen et al. 2012, SAXS + smFRET) differ by ~2x, and the
        // tethers here carry ~1 pN, so the zero-force end is the applicable one AND the soft
        // one. Both are carried; the bracket is asserted to be the right way round.
        assert(SsDnaTether.KUHN_LENGTH_ZERO_FORCE > SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY)
        assert(
            SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR >
                    SsDnaTether.KUHN_LENGTH_ZERO_FORCE
        )
        // and lower ionic strength gives a longer Kuhn length in both methods, as it must
        assert(
            SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY_TWO_MILLIMOLAR >
                    SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY
        )
        // the soft end costs the design a factor of two in stiffness at a fixed contour length
        val stiff = FreelyJointedChain(40.0, SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY)
        val soft = FreelyJointedChain(40.0, SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR)
        assert(
            (stiff.gaussianStiffness / soft.gaussianStiffness).isCloseTo(
                SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR /
                        SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY
            )
        )
    }

    // ---------------------------------------------------------------- guards

    @Test
    fun `a chain extension should not be allowed to reach its contour length`() {
        val chain = FreelyJointedChain(contourLength = 40.0, kuhnLength = 1.5)
        assertFailsWith<IllegalArgumentException> { chain.tension(40.0) }
        assertFailsWith<IllegalArgumentException> { chain.tension(-1.0) }
    }
}
