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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.test.Test

/**
 * `T-3b` gate tests for the **2-D** nonlinear 2:1 Poisson-Boltzmann solve of the tile edge.
 *
 * ## The geometry, restated because the tests enforce it
 *
 * `z` is normal to the electrode, positive away from it, electrode at `z = 0`.
 * `x = 0` is the tile **centre-line** and a symmetry plane; the rim is at `x = a`; the domain
 * runs out to `x = a + outerWidth`. The tile is an impermeable obstacle occupying
 * `0 ≤ x ≤ a`, `h ≤ z ≤ h + t`, with a fixed charge on its bottom face, its top face and its rim.
 * The traction is reported **as a downward load**, positive when it pushes the tile toward the
 * electrode, which is the sign `C-0006`'s plate consumes.
 *
 * The single most important test in this file is the one that says the centre-line reproduces
 * `T-3a`'s 1-D disjoining pressure: the two solvers share only `IonModel`, so agreement there
 * is a genuine cross-implementation check and disagreement kills everything downstream.
 */
class PoissonBoltzmannEdgeTest {

    private val lb = bjerrumLength()
    private val buffer = MagnesiumChlorideBuffer(2.0)
    private val ions = IonModel(buffer.magnesiumNumberDensity)
    private val freeBuffer = GapMedium()

    /** The Manning-renormalised tile charge facing the gap — `C-0005`/`C-0008`. */
    private val tileCharge = -1276.0 / 2.0 / 1600.0

    private fun edge(
        gapHeight: Double = 10.0,
        refinement: Int = 1,
        outerWidth: Double = 40.0,
        farFieldDirichlet: Boolean = true
    ) = PoissonBoltzmannEdge(
        gapHeight = gapHeight,
        ionModel = ions,
        medium = freeBuffer,
        bjerrumLength = lb,
        outerWidth = outerWidth,
        refinement = refinement,
        farFieldDirichlet = farFieldDirichlet
    )

    private fun oneDimensionalLoad(gapHeight: Double, electrodeReducedPotential: Double): Double =
        -PoissonBoltzmannGap(gapHeight, ions, uniformMedium(freeBuffer), lb, nodes = 4000)
            .solve(electrodeReducedPotential, tileCharge)
            .disjoiningPressureInPiconewtonPerSquareNanometre

    // gate 1 — dimensional consistency

    @Test
    fun `gate 1 should report the load in the same pN per square nm as the 1-D solve`() {
        // The 2-D traction is assembled from its own stress tensor and its own mesh; the only
        // thing it shares with T-3a is the ion model. If the unit chain k_BT/nm^3 -> pN/nm^2
        // were dropped anywhere the two would differ by 4.142, which no tolerance would hide.
        val solution = edge(refinement = 2).solve(2.0, tileCharge)
        val ratio = solution.centrelineLoad / oneDimensionalLoad(10.0, 2.0)
        assert(ratio > 0.9)
        assert(ratio < 1.1)
    }

    @Test
    fun `gate 1 should make the rim line force a force per unit length of edge`() {
        // The rim face is the only boundary whose vertical traction is a LINE force rather than
        // a pressure, so it must grow with the height of rim there is to integrate over.
        val thin = PoissonBoltzmannEdge(
            gapHeight = 10.0, ionModel = ions, medium = freeBuffer, bjerrumLength = lb,
            tileThickness = 2.0, refinement = 1
        ).solve(2.0, tileCharge, rimChargeDensity = tileCharge)
        val thick = PoissonBoltzmannEdge(
            gapHeight = 10.0, ionModel = ions, medium = freeBuffer, bjerrumLength = lb,
            tileThickness = 10.0, refinement = 1
        ).solve(2.0, tileCharge, rimChargeDensity = tileCharge)
        assert(abs(thick.rimLineForce) > abs(thin.rimLineForce))
    }

    @Test
    fun `gate 1 should sample the load profile from the rim inward to the centre line`() {
        val solution = edge().solve(2.0, tileCharge)
        assert(solution.distanceFromEdge[0].isCloseTo(0.0, 1e-12))
        assert(solution.distanceFromEdge.last().isCloseTo(20.0, 1e-9))
        assert(solution.distanceFromEdge.size == solution.downwardLoad.size)
        for (i in 1 until solution.distanceFromEdge.size) {
            assert(solution.distanceFromEdge[i] > solution.distanceFromEdge[i - 1])
        }
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should give exactly no load when nothing is charged`() {
        val solution = edge(refinement = 1).solve(0.0, 0.0)
        assert(abs(solution.centrelineLoad) < 1e-12)
        assert(solution.downwardLoad.all { abs(it) < 1e-12 })
        assert(abs(solution.rimLineForce) < 1e-12)
    }

    @Test
    fun `gate 2 should leave the tile top face unloaded deep under the tile`() {
        // An isolated charged plate in bulk has a vanishing first integral — that is the
        // contact-value theorem — so far from the rim the top face contributes nothing at all,
        // and the whole net load is the gap's. This is why the 1-D model was allowed to ignore it.
        val solution = edge(refinement = 2).solve(2.0, tileCharge)
        val centre = solution.topTraction.last()
        assert(abs(centre) < 0.02 * abs(solution.centrelineLoad))
    }

    @Test
    fun `gate 2 should return an edge effect bounded in depth and narrower than the half tile`() {
        // Falsifiers 3 and 4 of the T-3b plan, asserted rather than inspected: a taper wider than
        // the tile half-width is not an edge effect at all and the raised-cosine parameterisation
        // C-0006 consumes could not represent it, and a depth past 1 would leave the range over
        // which C-0006's dishing is demonstrated linear. The SIGN is deliberately not asserted.
        val solution = edge(refinement = 2).solve(2.0, tileCharge)
        val fit = solution.taperFit()
        assert(abs(fit.depth) > 0.0)
        assert(abs(fit.depth) < 1.0)
        assert(fit.equivalentWidth > 0.0)
        assert(fit.equivalentWidth < solution.tileHalfWidth)
    }

    @Test
    fun `gate 2 should find the corner traction mesh divergent, which is why there is a standoff`() {
        // Not a defect being tolerated but a property being measured: a 90-degree re-entrant
        // corner has an r^(-2/3) traction, and one more lateral derivative takes it past
        // integrability on this stencil. Everything else here converges at second order; this
        // does not, and that is exactly the reason the fit carries a standoff.
        val rim = listOf(1, 2, 4).map { abs(edge(refinement = it).solve(2.0, tileCharge).downwardLoad[0]) }
        assert(rim[1] > 2.0 * rim[0])
        assert(rim[2] > 2.0 * rim[1])
    }

    @Test
    fun `gate 2 should weaken the load as the gap widens`() {
        val near = edge(gapHeight = 5.0, refinement = 1).solve(2.0, tileCharge)
        val far = edge(gapHeight = 10.0, refinement = 1).solve(2.0, tileCharge)
        assert(near.centrelineLoad > far.centrelineLoad)
    }

    // gate 3 — symmetry and conservation

    @Test
    fun `gate 3 should conserve charge over the whole 2-D domain`() {
        // The tile's charge, the electrode's induced charge and the bulk boundaries' induced
        // charge must be cancelled by the space charge. The surface charges are read from
        // one-sided derivatives of the converged field, which is a DIFFERENT discrete operator
        // from the flux balance the assembly uses, so this is not an identity.
        val solution = edge(refinement = 2).solve(2.0, tileCharge)
        assert(solution.chargeBalance < 1e-2)
    }

    @Test
    fun `gate 3 should flatten the load profile at the symmetry plane`() {
        // x = 0 is a symmetry plane, so the lateral derivative of everything vanishes there and
        // the last two samples of the profile must be indistinguishable.
        val solution = edge(refinement = 2).solve(2.0, tileCharge)
        val n = solution.downwardLoad.size
        assert(solution.downwardLoad[n - 1].isCloseTo(solution.downwardLoad[n - 2], 1e-4))
    }

    @Test
    fun `gate 3 should carry the whole force through one plane by the global momentum balance`() {
        // The fluid above a horizontal plane in the gap has no vertical momentum entering it
        // through the symmetry plane (T_zx vanishes there exactly), the far field or the bulk
        // cap, so the entire force on the tile is the flux through that plane. It is a different
        // integral, over a different surface, from the pointwise profile, and beyond the corner
        // standoff the two must tell the same story about the interior.
        val solution = edge(refinement = 2).solve(2.0, tileCharge)
        assert(solution.momentumFluxLoadPerUnitEdge > solution.noEdgeLoadPerUnitEdge * 0.5)
        assert(solution.momentumFluxLoadPerUnitEdge < solution.noEdgeLoadPerUnitEdge * 2.0)
        assert(
            solution.rimResidualPerUnitEdge()
                .isCloseTo(solution.totalDeficitPerUnitEdge - solution.taperFit().loadDeficit, 1e-9)
        )
    }

    @Test
    fun `gate 3 should recover the centre line load from the vertical first integral as well`() {
        // Deep under the tile the lateral shear stress has no gradient, so vertical momentum
        // balance forces T_zz to be constant in z — and the mid-gap first integral is then a
        // second, independent route to the same traction. Near the rim it is not, and that
        // departure is the taper rather than an error.
        val solution = edge(refinement = 2).solve(2.0, tileCharge)
        assert(solution.centrelineRouteSpread < 0.02)
        assert(solution.numericallyResolved)
    }

    @Test
    fun `gate 3 should give the rim no vertical force at all when it carries no charge`() {
        // f_z on the rim is eps E_z E_x, and E_x there is fixed by the rim's own Neumann
        // condition, so an uncharged rim contributes EXACTLY zero — not approximately.
        val solution = edge(refinement = 1).solve(2.0, tileCharge, rimChargeDensity = 0.0)
        assert(solution.rimLineForce == 0.0)
    }

    // gate 4 — numerical convergence

    @Test
    fun `gate 4 should converge the taper depth on nested refinements`() {
        // Nested 1 -> 2 -> 4, never 1/2/3/4: a non-nested subdivision moves a feature off a node
        // and breaks monotonicity for reasons that have nothing to do with the discretisation.
        val depths = listOf(1, 2, 4).map { edge(refinement = it).solve(2.0, tileCharge).taperFit().depth }
        val first = abs(depths[1] - depths[0])
        val second = abs(depths[2] - depths[1])
        assert(second < first)
        assert(second < 0.05)
    }

    @Test
    fun `gate 4 should not depend on the lateral far field boundary condition`() {
        val dirichlet = edge(refinement = 2, farFieldDirichlet = true).solve(2.0, tileCharge)
        val neumann = edge(refinement = 2, farFieldDirichlet = false).solve(2.0, tileCharge)
        assert(dirichlet.taperFit().depth.isCloseTo(neumann.taperFit().depth, 5e-2))
        assert(
            dirichlet.totalDeficitPerUnitEdge
                .isCloseTo(neumann.totalDeficitPerUnitEdge, 5e-2)
        )
    }

    @Test
    fun `gate 4 should not depend on how far the domain extends beyond the tile`() {
        val near = edge(refinement = 2, outerWidth = 20.0).solve(2.0, tileCharge)
        val far = edge(refinement = 2, outerWidth = 40.0).solve(2.0, tileCharge)
        assert(near.taperFit().depth.isCloseTo(far.taperFit().depth, 5e-2))
        assert(near.totalDeficitPerUnitEdge.isCloseTo(far.totalDeficitPerUnitEdge, 5e-2))
    }

    @Test
    fun `gate 4 should converge the global edge deficit on nested refinements`() {
        // The global momentum-flux route is the one every downstream number rests on, so its
        // convergence is asserted as an ORDER and not against a tolerance.
        val deficits = listOf(1, 2, 4).map {
            edge(refinement = it).solve(2.0, tileCharge).totalDeficitPerUnitEdge
        }
        val first = abs(deficits[1] - deficits[0])
        val second = abs(deficits[2] - deficits[1])
        assert(second < 0.5 * first)
    }

    @Test
    fun `gate 4 should drive Newton to a reported residual rather than an assumed one`() {
        val solution = edge(refinement = 1).solve(2.0, tileCharge)
        assert(solution.newtonCorrection < 1e-8)
        assert(solution.newtonIterations < 60)
    }

    // gate 5 — upstream cross-check

    @Test
    fun `gate 5 should reproduce the T-3a 1-D disjoining pressure at the centre line`() {
        // The strongest falsifier this task declared in advance. Two independent
        // discretisations, two independent traction routes, one shared ion model.
        for (height in listOf(5.0, 10.0)) {
            for (potential in listOf(1.0, 3.0)) {
                val solution = edge(gapHeight = height, refinement = 2).solve(potential, tileCharge)
                assert(solution.centrelineLoad.isCloseTo(oneDimensionalLoad(height, potential), 0.02))
            }
        }
    }

    @Test
    fun `gate 5 should keep the taper narrower than the transverse eigenvalue bound allows`() {
        // The cheap bound is a rigorous upper bound on the decay length within linear theory,
        // and the nonlinear solve must respect it because kappa_loc exceeds kappa everywhere.
        val solution = edge(gapHeight = 10.0, refinement = 2).solve(2.0, tileCharge)
        val bound = 1.0 / transverseDecayRateBound(buffer.inverseDebyeLength(), 10.0)
        assert(solution.taperFit().decayLength < bound)
    }

    @Test
    fun `gate 5 should bracket the edge effect by the rim charge the project cannot source`() {
        // Falsifier 5 of the T-3b plan FIRED, and it is recorded rather than tuned away. An
        // uncharged rim and a rim at the face density are the two defensible readings — the tile's
        // charge is volumetric and the surface it is smeared onto is a convention — and they
        // differ by more than 40% in the fitted depth. So the rim charge IS load-bearing for the
        // taper, even though it is exactly irrelevant to the rim's own vertical force. What
        // survives is the sign and the ordering, and those are what is asserted.
        val bare = edge(refinement = 2).solve(2.0, tileCharge, rimChargeDensity = 0.0)
        val charged = edge(refinement = 2).solve(2.0, tileCharge, rimChargeDensity = tileCharge)
        assert(bare.taperFit().depth < 0.0)
        assert(charged.taperFit().depth < 0.0)
        assert(abs(charged.taperFit().depth) < abs(bare.taperFit().depth))
        assert(abs(bare.taperFit().depth) < 2.0 * abs(charged.taperFit().depth))
    }

    @Test
    fun `gate 5 should carry the thermal energy of the project rather than its own`() {
        assert(thermalEnergy().isCloseTo(4.142, 1e-3))
    }

}
