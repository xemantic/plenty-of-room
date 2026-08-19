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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.test.Test

/**
 * `P-14` gates on the **solver** side: that giving `C-0022`'s 2-D edge solve a laterally graded
 * face charge and a vertically graded rim charge is an extension and not a change.
 *
 * The load-bearing one is the last: the charge the assembly actually applies to the tile's
 * boundary equals `ρ t a` — the tile's own charge per unit edge — at **every** member of the
 * conserving family, which is a statement about the discretisation and not about the algebra.
 */
class CutRimSmearingSolveTest {

    private val faceCharge = -0.3986652379247042
    private val thickness = 10.0
    private val halfWidth = 20.0
    private val rho = tileVolumetricChargeDensity(faceCharge, thickness)

    private fun solver() = PoissonBoltzmannEdge(
        gapHeight = 10.0,
        ionModel = IonModel(MagnesiumChlorideBuffer(2.0).magnesiumNumberDensity),
        bjerrumLength = bjerrumLength(),
        tileHalfWidth = halfWidth,
        tileThickness = thickness,
        refinement = 1
    )

    private val electrode = 0.5

    // ------------------------------------------------ gate 1 — the null shape is the old solve

    @Test
    fun `gate 1 should leave the unshaped solve untouched by the new arguments`() {
        val bare = solver().solve(electrode, faceCharge)
        val explicit = solver().solve(
            electrode, faceCharge, faceCharge, 0.0, faceShape = null, rimShape = null
        )
        assert(explicit.tileChargePerLength == bare.tileChargePerLength)
        assert(explicit.centrelineLoad.isCloseTo(bare.centrelineLoad, 1e-10))
        assert(explicit.rimLineForce == bare.rimLineForce)
    }

    @Test
    fun `gate 1 should make a constant unit shape the same solve as no shape`() {
        val bare = solver().solve(electrode, faceCharge, faceCharge, 0.5 * faceCharge)
        val unit = solver().solve(
            electrode, faceCharge, faceCharge, 0.5 * faceCharge,
            faceShape = EdgeChargeShape { 1.0 }, rimShape = EdgeChargeShape { 1.0 }
        )
        assert(unit.centrelineLoad.isCloseTo(bare.centrelineLoad, 1e-10))
        assert(unit.rimLineForce.isCloseTo(bare.rimLineForce, 1e-10))
        assert(unit.tileChargePerLength.isCloseTo(bare.tileChargePerLength, 1e-12))
    }

    // ------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 should make a zero rim shape the uncharged rim exactly`() {
        val uncharged = solver().solve(electrode, faceCharge)
        val shapedToZero = solver().solve(
            electrode, faceCharge, faceCharge, faceCharge, rimShape = EdgeChargeShape { 0.0 }
        )
        assert(abs(shapedToZero.rimLineForce) < 1e-300)
        assert(shapedToZero.centrelineLoad.isCloseTo(uncharged.centrelineLoad, 1e-10))
        assert(shapedToZero.taperFit().depth.isCloseTo(uncharged.taperFit().depth, 1e-8))
    }

    @Test
    fun `gate 2 should leave the centre-line untouched by a collar-confined face taper`() {
        val bare = solver().solve(electrode, faceCharge)
        val medial = CutRimSmearing.medial(rho, thickness, halfWidth)
        val shaped = solveSmearing(solver(), electrode, medial)
        // the taper is 5 nm deep on a 20 nm half-width: the centre-line cannot feel it beyond
        // what C-0022 already reports against T-3a
        assert(shaped.centrelineLoad.isCloseTo(bare.centrelineLoad, 2e-3))
    }

    // ------------------------------------------------ gate 3 — conservation, on the ASSEMBLY

    @Test
    fun `gate 3 should apply the tile's own charge at every conserving member`() {
        // The ALGEBRA is exact and asserted at 1e-12 in CutRimChargeTest; what is asserted here
        // is the ASSEMBLY, which samples the shape at the midpoint of each wall segment. The
        // shapes have a kink — at s = l for the face, at mid-height for the medial rim — so the
        // quadrature is second order there and the residue is a mesh error, not a partition one.
        val own = rho * thickness * halfWidth
        for (taper in listOf(0.0, 1.0, 2.5, 5.0, 10.0)) {
            val smearing = CutRimSmearing.taperedFace(rho, thickness, halfWidth, taper)
            val solved = solveSmearing(solver(), electrode, smearing)
            assert(solved.tileChargePerLength.isCloseTo(own, 1e-4))
        }
        val medial = CutRimSmearing.medial(rho, thickness, halfWidth)
        assert(solveSmearing(solver(), electrode, medial).tileChargePerLength.isCloseTo(own, 1e-4))
        // and the untapered member is exact, because a null shape applies no quadrature at all
        val flat = CutRimSmearing.taperedFace(rho, thickness, halfWidth, 0.0)
        assert(solveSmearing(solver(), electrode, flat).tileChargePerLength.isCloseTo(own, 1e-15))
    }

    @Test
    fun `gate 4 should refine the taper quadrature away`() {
        val own = rho * thickness * halfWidth
        val medial = CutRimSmearing.medial(rho, thickness, halfWidth)
        val coarse = abs(solveSmearing(solver(), electrode, medial).tileChargePerLength / own - 1.0)
        val fine = abs(
            solveSmearing(
                PoissonBoltzmannEdge(
                    gapHeight = 10.0,
                    ionModel = IonModel(MagnesiumChlorideBuffer(2.0).magnesiumNumberDensity),
                    bjerrumLength = bjerrumLength(),
                    tileHalfWidth = halfWidth,
                    tileThickness = thickness,
                    refinement = 2
                ),
                electrode, medial
            ).tileChargePerLength / own - 1.0
        )
        assert(coarse < 1e-4)
        assert(fine < coarse)
    }

    @Test
    fun `gate 3 should report C-0022's falsifier as applying more charge than the tile has`() {
        val own = rho * thickness * halfWidth
        val falsifier = solver().solve(electrode, faceCharge, faceCharge, faceCharge)
        // 1.25 on the 2-D half-tile: two faces of a, one rim of t, at one density
        assert((falsifier.tileChargePerLength / own).isCloseTo(1.25, 1e-12))
    }
}
