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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.test.Test

/**
 * `T-3a` gate tests for the 1-D nonlinear Poisson-Boltzmann solve of the Gen-1 gap —
 * **tile and electrode as one system**, in the actual 2:1 buffer.
 *
 * ## The sign convention, restated here because the tests enforce it
 *
 * `z` is normal to the electrode, **positive away from it**, origin at the electrode.
 * The tile is at `z = h`, carries **negative** charge, and a **positive** electrode bias
 * pulls it toward `−z`. So `F_es,z < 0` under bias, and
 * `k_es = −∂F_es,z/∂z < 0` — §1 of the problem definition, and `T-6` before this.
 */
class PoissonBoltzmannGapTest {

    private val lb = bjerrumLength()
    private val buffer = MagnesiumChlorideBuffer(2.0)
    private val kappa = buffer.inverseDebyeLength()
    private val ions = IonModel(buffer.magnesiumNumberDensity)
    private val freeBuffer = uniformMedium(GapMedium())

    /** The Manning-renormalised tile charge facing the gap — `C-0005`: 1276 e over 1600 nm², halved. */
    private val tileCharge = -1276.0 / 2.0 / 1600.0

    private fun gap(height: Double, nodes: Int = 4000, model: IonModel = ions) =
        PoissonBoltzmannGap(height, model, freeBuffer, lb, nodes = nodes)

    // gate 1 — dimensional consistency

    @Test
    fun `gate 1 should reduce the contact value theorem to the isolated plate at a large gap`() {
        // Exact statement of the contact theorem: for a single plate the osmotic excess at the wall
        // is exactly the Maxwell stress 2 pi l_B sigma^2, so the disjoining pressure vanishes.
        // Reproducing it from the two-plate solve at a large gap is the sharpest dimensional check
        // available: it ties [1/nm^3] of ion density to [1/nm^2]^2 x [nm] of charge density.
        val solution = gap(80.0).solve(0.0, tileCharge)
        // relative to the natural scale of the problem, 2 pi l_B sigma^2, not absolutely
        assert(abs(solution.disjoiningPressureAtContact) /
                (2.0 * PI * lb * tileCharge * tileCharge) < 1e-5)
        assert(
            (2.0 * PI * lb * tileCharge * tileCharge)
                .isCloseTo(
                    ions.osmoticPressureExcess(solution.tileReducedPotential, GapMedium()),
                    1e-5
                )
        )
    }

    @Test
    fun `gate 1 should convert the disjoining pressure into the locked pressure unit`() {
        // k_BT/nm^3 -> pN/nm^2 is exactly k_BT in pN nm, and pN/nm^2 is exactly 1 MPa.
        val solution = gap(5.0).solve(4.0, tileCharge)
        assert(
            solution.disjoiningPressureInPiconewtonPerSquareNanometre
                .isCloseTo(solution.disjoiningPressure * thermalEnergy(), 1e-12)
        )
        assert(
            solution.forceOnTile(1600.0)
                .isCloseTo(solution.disjoiningPressureInPiconewtonPerSquareNanometre * 1600.0, 1e-12)
        )
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should reproduce the linearised mixed boundary value problem at vanishing charge`() {
        // The cheap bound IS the linear limit, so it doubles as a limiting case: as both surface
        // charges go to zero the nonlinear solve must return the closed-form Debye-Huckel pressure.
        listOf(1e-3, 1e-4).forEach { scale ->
            val solution = gap(6.0).solve(scale, tileCharge * scale)
            val linear = linearMixedDisjoiningPressure(6.0, scale, tileCharge * scale, kappa, lb)
            assert(solution.disjoiningPressure.isCloseTo(linear, 2e-2))
        }
        // and the departure is first order in the amplitude — the nonlinearity is a correction,
        // not a different equation
        fun error(scale: Double): Double {
            val solution = gap(6.0).solve(scale, tileCharge * scale)
            val linear = linearMixedDisjoiningPressure(6.0, scale, tileCharge * scale, kappa, lb)
            return abs(solution.disjoiningPressure / linear - 1.0)
        }
        assert((error(1e-3) / error(5e-4)).isCloseTo(2.0, 0.1))
    }

    @Test
    fun `gate 2 should reduce the Bikerman model to the point ion model as the site density grows`() {
        // Size-modified (Bikerman) PB is the T-6b bracket folded in here. Its whole content is the
        // lattice-gas denominator, which must disappear as the lattice spacing goes to zero.
        val reference = gap(5.0).solve(7.0, tileCharge)
        listOf(1e4, 1e6).forEach { maximum ->
            val modified = gap(5.0, model = IonModel(buffer.magnesiumNumberDensity, maximum))
                .solve(7.0, tileCharge)
            assert(
                modified.disjoiningPressure
                    .isCloseTo(reference.disjoiningPressure, 2e4 / maximum)
            )
        }
        // and at a physical site density it must STRENGTHEN the answer, not weaken it: an ion that
        // cannot be packed into the contact layer screens from further out, so the double layer is
        // thicker and the interaction larger. The point-ion force is therefore a LOWER bound on
        // |F_es|, which is the opposite of the direction one would guess from "steric exclusion".
        val physical = gap(5.0, model = IonModel(buffer.magnesiumNumberDensity, closePackedNumberDensity(0.428)))
            .solve(7.0, tileCharge)
        assert(abs(physical.disjoiningPressure) > abs(reference.disjoiningPressure))
        assert(abs(physical.disjoiningPressure / reference.disjoiningPressure) < 1.2)
    }

    @Test
    fun `gate 2 should attract the tile to a grounded electrode at zero bias`() {
        // A constant-POTENTIAL conductor at bulk potential is not a neutral wall: the tile's field
        // induces a countercharge on it, and the tile is attracted. This is the V = 0 baseline, and
        // it is the reason a constant-charge/constant-charge treatment would be qualitatively wrong.
        val solution = gap(5.0).solve(0.0, tileCharge)
        assert(solution.disjoiningPressure < 0.0)
        assert(solution.electrodeSurfaceChargeDensity > 0.0)
    }

    @Test
    fun `gate 2 should halve the decay length of the force at zero bias`() {
        // Image interaction carries e^{-2 kappa h}: decay length lambda_D / 2. Asserted against the
        // nonlinear solve, not against the linear closed form, because that is the claim CH-0004 needs.
        fun force(h: Double) = gap(h).solve(0.0, tileCharge).disjoiningPressure
        val decay = -3.0 / ln(force(15.0) / force(12.0))
        assert(decay.isCloseTo(0.5 / kappa, 5e-2))
        // it is nowhere near the bulk lambda_D, and nowhere near the ~0.84 nm counterion length
        // CH-0004 proposed for the gap either — both are wrong by a factor of two
        assert(decay < 0.75 / kappa)
        assert(decay > 1.5)
    }

    @Test
    fun `gate 2 should give the biased force the full bulk Debye decay length far from the tile`() {
        // And under bias it is e^{-kappa h}: decay length lambda_D. The two limits differ by a factor
        // of two, so "the decay length of the electrostatic force" is not one number even in mean field.
        fun force(h: Double) = gap(h).solve(7.6, tileCharge).disjoiningPressure
        val decay = -5.0 / ln(force(25.0) / force(20.0))
        assert(decay.isCloseTo(1.0 / kappa, 5e-2))
        assert(decay > 2.0 * (-3.0 / ln(gap(15.0).solve(0.0, tileCharge).disjoiningPressure /
                gap(12.0).solve(0.0, tileCharge).disjoiningPressure)) * 0.8)
    }

    @Test
    fun `gate 2 should vanish when both surfaces are uncharged`() {
        val solution = gap(5.0).solve(0.0, 0.0)
        assert(abs(solution.disjoiningPressure) < 1e-14)
        assert(abs(solution.electrodeSurfaceChargeDensity) < 1e-14)
    }

    // gate 3 — symmetry and conservation

    @Test
    fun `gate 3 should hold the first integral constant across the gap`() {
        // Osmotic minus Maxwell is the first integral of PB and is EXACTLY constant in z for a uniform
        // medium. That is the conservation law behind "evaluate the pressure at the midplane", and
        // measuring its spread is how the solve reports its own error without a reference solution.
        //
        // The thresholds are loose, and the reason is worth stating: near a biased electrode the
        // osmotic and Maxwell terms are each two to three orders of magnitude larger than their
        // difference, so any evaluation away from a wall inherits that cancellation. It is a property
        // of the DIAGNOSTIC. The pressure actually used is the contact-value one at the tile, in which
        // the Maxwell term is exact from the Neumann condition and no derivative is taken at all.
        // What makes this a gate rather than a tolerance is that both measures CONVERGE with the mesh.
        listOf(3.0, 5.0, 10.0).forEach { height ->
            val solution = gap(height).solve(7.6, tileCharge)
            val refined = gap(height, nodes = 8000).solve(7.6, tileCharge)
            assert(solution.firstIntegralRelativeSpread < 2e-3)
            assert(refined.firstIntegralRelativeSpread < solution.firstIntegralRelativeSpread)
            // three independent evaluations of the same constant — the contact-value theorem at
            // the tile, the midplane, and the best-conditioned interior node — must agree
            assert(
                solution.disjoiningPressureAtMidplane
                    .isCloseTo(solution.disjoiningPressureAtContact, 1e-3)
            )
            assert(
                solution.disjoiningPressure
                    .isCloseTo(solution.disjoiningPressureAtContact, 1e-3)
            )
            val coarse = abs(
                solution.disjoiningPressureAtMidplane / solution.disjoiningPressureAtContact - 1.0
            )
            val fine = abs(
                refined.disjoiningPressureAtMidplane / refined.disjoiningPressureAtContact - 1.0
            )
            assert((coarse / fine).isCloseTo(4.0, 0.3))
        }
    }

    @Test
    fun `gate 3 should conserve charge between the two surfaces and the gap`() {
        // sigma_electrode + sigma_tile + integral(rho) = 0. The electrode charge is read from the
        // half-cell flux balance and the integral from Simpson, so the two routes are independent.
        listOf(0.0, 3.0, 7.6).forEach { potential ->
            val solution = gap(6.0).solve(potential, tileCharge)
            assert(
                (solution.electrodeSurfaceChargeDensity + tileCharge + solution.integratedSpaceCharge)
                    .isCloseTo(0.0, 1e-8)
            )
        }
    }

    @Test
    fun `gate 3 should make the electrostatic stiffness negative under bias`() {
        // §1: k_es = -dF_es,z/dz < 0. The tile is pulled harder the closer it gets, which is the
        // spring-softening term T-4 consumes. Asserted as a sign, at every working gap.
        listOf(5.0, 7.0, 10.0).forEach { height ->
            val stiffness = electrostaticStiffness(height, 7.6)
            assert(stiffness < 0.0)
        }
    }

    @Test
    fun `gate 3 should reach the Donnan potential inside an uncharged partitioning layer`() {
        // C-0005 combines the ion partition coefficients by the stoichiometric geometric mean, which
        // is Donnan equilibrium for a layer with no fixed charge. The solve must reproduce the Donnan
        // potential (1/3) ln(K+/K-) in the interior of a thick uncharged layer, independently.
        val layer = GapMedium(magnesiumPartitionCoefficient = 0.693, chloridePartitionCoefficient = 0.808)
        val solution = PoissonBoltzmannGap(60.0, ions, uniformMedium(layer), lb).solve(0.0, 0.0)
        val donnan = ln(0.693 / 0.808) / 3.0
        assert(solution.tileReducedPotential.isCloseTo(donnan, 1e-5))
    }

    // gate 4 — numerical convergence

    @Test
    fun `gate 4 should converge second order in the mesh spacing`() {
        // The discretisation is conservative and centred, so the error must fall as dz^2.
        // Checked as an ORDER across three meshes, not against a tolerance.
        fun pressure(nodes: Int) =
            gap(5.0, nodes = nodes).solve(7.6, tileCharge).disjoiningPressureAtContact
        val fine = pressure(16000)
        val coarse = abs(pressure(1000) - fine)
        val medium = abs(pressure(2000) - fine)
        val finer = abs(pressure(4000) - fine)
        assert((coarse / medium).isCloseTo(4.0, 0.2))
        assert((medium / finer).isCloseTo(4.0, 0.2))
    }

    @Test
    fun `gate 4 should drive the Newton correction below the round off floor`() {
        listOf(0.0, 3.0, 9.1).forEach { potential ->
            val solution = gap(5.0).solve(potential, tileCharge)
            assert(solution.newtonCorrection < 1e-11)
            assert(solution.newtonIterations < 60)
        }
    }

    @Test
    fun `gate 4 should make the electrostatic stiffness independent of the differencing step`() {
        // k_es is a numerical derivative of a numerical solve, which is where a plausible-looking
        // wrong number would come from. Two steps a factor of four apart must agree.
        val coarse = electrostaticStiffness(5.0, 7.6, step = 0.04)
        val fine = electrostaticStiffness(5.0, 7.6, step = 0.01)
        assert(coarse.isCloseTo(fine, 1e-3))
        // the residual difference is the O(step^2) truncation of the central difference, and it
        // falls by 16 between the two steps — asserted as an order so a wrong derivative shows up
        val middle = electrostaticStiffness(5.0, 7.6, step = 0.02)
        assert((abs(coarse - fine) / abs(middle - fine)).isCloseTo(5.0, 0.15))
    }

    // gate 5 — cross-check against the closed forms and against C-0005

    @Test
    fun `gate 5 should reproduce the closed form surface potential of the isolated tile`() {
        // At a large gap and a grounded far electrode the tile's own surface potential must be the
        // one the 2:1 Grahame relation gives. This is the check that the Neumann condition carries
        // the factor 4 pi l_B and not 2 pi l_B — the single most likely sign-and-factor error here.
        val solution = gap(80.0).solve(0.0, tileCharge)
        assert(
            solution.tileReducedPotential
                .isCloseTo(asymmetricReducedSurfacePotential(tileCharge, kappa, lb), 1e-6)
        )
    }

    @Test
    fun `gate 5 should reproduce the closed form single plate profile from the gap solve`() {
        // The solver and the analytics are independent implementations of the same 2:1 problem —
        // one a graded finite-volume Newton solve, the other a quadrature done by hand. In a gap
        // wide enough that the electrode is out of range they must agree node by node.
        val solution = gap(60.0, nodes = 8000).solve(0.0, tileCharge)
        val surface = asymmetricReducedSurfacePotential(tileCharge, kappa, lb)
        listOf(1.0, 3.0, 6.0, 10.0).forEach { distance ->
            val index = solution.height.indexOfFirst { it >= 60.0 - distance }
            val analytic = asymmetricPotentialProfile(60.0 - solution.height[index], surface, kappa)
            assert(solution.reducedPotential[index].isCloseTo(analytic, 1e-3))
        }
        // and the effective charge the solved profile carries is the asymmetric one — BELOW
        // C-0005's symmetric z:z ceiling of 0.0568 e/nm^2, which is what that ceiling claimed to be
        val effective = asymmetricEffectiveChargeDensity(surface, kappa, lb)
        assert(abs(effective) < saturatedEffectiveChargeDensity(kappa, 2, lb))
        assert(effective.isCloseTo(-0.0424939, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 5 should keep the tile contact density below close packing at the Manning charge`() {
        // C-0005 found point-ion PB puts Mg2+ 1.75x past close packing at the BARE duplex charge.
        // At the Manning-renormalised charge this solve must not — otherwise the force computed here
        // would be resting on an unphysical contact layer. Reported as a premise check, not assumed.
        val solution = gap(5.0).solve(7.6, tileCharge)
        val contact = buffer.magnesiumNumberDensity * exp(-2.0 * solution.tileReducedPotential)
        assert(contact < closePackedNumberDensity(HYDRATED_MAGNESIUM_RADIUS))
    }

    @Test
    fun `gate 5 should place the point ion boundary of C-0005 at the diffuse layer potential not the bias`() {
        // C-0005's 0.197 V is a DIFFUSE-LAYER drop. In series with a 20 uF/cm^2 compact layer the
        // applied bias that produces it is about a volt, and 2 V produces only ~0.24 V of diffuse drop.
        // This is CH-0007's ground, asserted as a number.
        val stern = sternChargeDensityPerVolt(20.0)
        val boundary = stericSaturationPotential(1, buffer.chlorideNumberDensity, HYDRATED_CHLORIDE_RADIUS)
        assert(boundary.isCloseTo(0.196568, relativeTolerance = 1e-4))
        val biasAtBoundary = appliedBiasOfDiffusePotential(5.0, boundary, tileCharge, stern, ions, freeBuffer, lb)
        assert(biasAtBoundary > 0.9)
        val diffuseAtTwoVolts = diffusePotentialOfAppliedBias(5.0, 2.0, tileCharge, stern, ions, freeBuffer, lb)
        assert(diffuseAtTwoVolts.isCloseTo(0.235, relativeTolerance = 3e-2))
    }

    @Test
    fun `gate 5 should keep the force within a factor of two across three readings of the tile charge`() {
        // The finding, locked as a test: the tile is charge-SATURATED, so a factor of three in the
        // assumed gap-facing charge (bottom helix row / half the tile / all of it) moves the force
        // far less than proportionally. This is what makes T-3's force robust to a choice C-0005
        // could not make for it.
        val forces = listOf(-0.2694, -0.39875, -0.7975).map { charge ->
            abs(gap(5.0).solve(7.6, charge).disjoiningPressure)
        }
        assert((forces[2] / forces[0]) < 2.0)
        assert((forces[2] / forces[0]) > 1.0)
    }

    private fun electrostaticStiffness(
        height: Double,
        electrodeReducedPotential: Double,
        step: Double = 0.02
    ): Double {
        fun force(h: Double) =
            PoissonBoltzmannGap(h, ions, freeBuffer, lb)
                .solve(electrodeReducedPotential, tileCharge).forceOnTile(1600.0)
        return -(force(height + step) - force(height - step)) / (2.0 * step)
    }

}
