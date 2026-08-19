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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.test.Test

/**
 * The Netz electrostatic coupling parameter and the boundary of mean-field validity —
 * the core of task `T-6`.
 *
 * Every formula here is taken from **one** primary source, read rather than recalled:
 * A. Naji, S. Jungblut, A. G. Moreira and R. R. Netz,
 * *Electrostatic interactions in strongly-coupled soft matter*,
 * Physica A **352**:131 (2005), arXiv:cond-mat/0508767.
 * Equation numbers below are that paper's.
 */
class ChargedSurfaceTest {

    private val lb = bjerrumLength()

    /** The B-DNA duplex, the surface whose coupling parameter this task exists to compute. */
    private val dna = ChargedSurface(surfaceChargeDensity = 0.9362055476, counterionValency = 2)

    // gate 1 — dimensional consistency, expressed as exact identities between definitions

    @Test
    fun `gate 1 should satisfy the identity between coupling parameter and Gouy-Chapman length`() {
        // Naji Eq. (4): Xi = l_B~ / mu = q^2 l_B / mu, and Eq. (3): mu = 1/(2 pi q l_B sigma).
        // The two are written independently in the implementation, so their agreement is a
        // dimensional check and not a tautology.
        assert(
            dna.couplingParameter(lb)
                .isCloseTo(4.0 * lb / dna.gouyChapmanLength(lb), relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `gate 1 should satisfy the identity between plasma parameter and coupling parameter`() {
        // Naji Eq. (7): Gamma ~ Xi^(1/2). With the Wigner-Seitz radius a_WS = sqrt(q/(pi sigma))
        // the relation is EXACT and reads Gamma = sqrt(Xi/2) — which is what fixes the
        // otherwise free "geometrical prefactor of order one" of Eq. (5). See the gate 5 test
        // that recovers the paper's own Wigner-crystallisation threshold from it.
        assert(
            dna.plasmaParameter(lb)
                .isCloseTo(sqrt(dna.couplingParameter(lb) / 2.0), relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `gate 1 should keep the two lateral spacing conventions a factor of root pi apart`() {
        // The trap. Naji Eq. (5) writes a_perp ~ sqrt(q/sigma) and uses THAT in the
        // Rouzina-Bloomfield criterion Eq. (24); the Wigner-Seitz radius that makes the
        // plasma parameter exact is sqrt(q/(pi sigma)). They differ by sqrt(pi) = 1.772,
        // and substituting one for the other moves the attraction threshold by that factor.
        assert(
            (dna.lateralCounterionSpacing / dna.wignerSeitzRadius)
                .isCloseTo(sqrt(PI), relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `gate 1 should express the DNA surface charge density as 0 15 coulomb per square metre`() {
        // the textbook value for B-DNA, recovered from e/nm^2 by unit conversion alone
        assert(dna.surfaceChargeDensityInCoulombPerSquareMetre.isCloseTo(0.15, relativeTolerance = 2e-3))
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should scale the coupling parameter as the cube of the counterion valency`() {
        // Xi ∝ q^3 is the reason divalent Mg2+ is a different problem from monovalent Na+
        // at the SAME surface, and it is the single most important sensitivity in this task.
        val monovalent = ChargedSurface(dna.surfaceChargeDensity, counterionValency = 1)
        val trivalent = ChargedSurface(dna.surfaceChargeDensity, counterionValency = 3)
        assert(
            (dna.couplingParameter(lb) / monovalent.couplingParameter(lb))
                .isCloseTo(8.0, relativeTolerance = 1e-12)
        )
        assert(
            (trivalent.couplingParameter(lb) / monovalent.couplingParameter(lb))
                .isCloseTo(27.0, relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `gate 2 should reduce the mean-field pressure coefficient to its large separation asymptote`() {
        // Naji Eq. (14): beta P_PB / (2 pi l_B sigma^2) -> (pi mu / Delta)^2 for Delta/mu >> 1.
        // Asserted as a CONVERGENCE ORDER rather than a single tolerance: the relative
        // departure from the asymptote must fall as 1/r, which is a much stronger statement
        // than "it is small at one r", and would catch a wrong asymptote that happened to be
        // numerically close. (It converges to 4/r, the sub-leading term of the expansion.)
        val error = listOf(1e3, 1e4, 1e5).map { reducedGap ->
            val asymptote = (PI / reducedGap) * (PI / reducedGap)
            abs(poissonBoltzmannPressureCoefficient(reducedGap) - asymptote) / asymptote
        }
        // The tolerance tightens with r, which is itself the evidence of first-order
        // convergence: 9.973 then 9.997, approaching 10 as the second-order tail dies.
        assert(error[0] < 5e-3)
        assert((error[0] / error[1]).isCloseTo(10.0, relativeTolerance = 5e-3))
        assert((error[1] / error[2]).isCloseTo(10.0, relativeTolerance = 5e-4))
    }

    @Test
    fun `gate 2 should make the strong coupling pressure attractive beyond twice the Gouy-Chapman length`() {
        // Naji Eq. (15): beta P_SC / (2 pi l_B sigma^2) = -1 + 2 mu / Delta,
        // so the pressure changes sign at Delta* = 2 mu, Eq. (16).
        assert(strongCouplingPressureCoefficient(2.0).isCloseTo(0.0))
        assert(strongCouplingPressureCoefficient(1.0) > 0.0)
        assert(strongCouplingPressureCoefficient(4.0) < 0.0)
        // and saturates at -1 as the walls are pulled apart
        assert(strongCouplingPressureCoefficient(1e9).isCloseTo(-1.0, relativeTolerance = 1e-8))
    }

    @Test
    fun `gate 2 should make the mean-field deviation vanish in the weak coupling limit`() {
        // Xi -> 0 is the limit in which PB is exact, by construction of the loop expansion.
        assert(meanFieldDeviation(coupling = 1e-9, reducedGap = 50.0) < 1e-8)
        // and grow linearly in Xi at fixed geometry, because the loop parameter IS Xi
        val small = meanFieldDeviation(coupling = 0.1, reducedGap = 50.0)
        val large = meanFieldDeviation(coupling = 0.2, reducedGap = 50.0)
        assert((large / small).isCloseTo(2.0, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 2 should make the mean-field deviation fall as the walls are separated`() {
        val coupling = dna.couplingParameter(lb)
        val near = meanFieldDeviation(coupling, reducedGap = 40.0)
        val far = meanFieldDeviation(coupling, reducedGap = 400.0)
        assert(far < near)
    }

    // gate 3 — symmetry and conservation

    @Test
    fun `gate 3 should reproduce the exact contact value theorem for the counterion density`() {
        // Naji, after Eq. (9): rho(z = 0) = 2 pi l_B sigma^2, "an exact result within the
        // present model and valid beyond the mean-field level". So this one number is NOT
        // a mean-field artefact, and it is the number the steric check is run against.
        assert(
            dna.contactDensity(lb)
                .isCloseTo(2.0 * PI * lb * dna.surfaceChargeDensity * dna.surfaceChargeDensity, 1e-12)
        )
        assert(dna.contactDensity(lb).isCloseTo(3.9327, relativeTolerance = 1e-4))
        // it is independent of the counterion valency — the valency enters mu, not rho(0)
        val monovalent = ChargedSurface(dna.surfaceChargeDensity, counterionValency = 1)
        assert(monovalent.contactDensity(lb).isCloseTo(dna.contactDensity(lb), relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 3 should normalise the mean-field counterion profile to exactly neutralise the wall`() {
        // Naji Eq. (9): rho(z) = 2 pi l_B sigma^2 / (z/mu + 1)^2. Integrating over the
        // half-space must return sigma/q counterions per unit area — global electroneutrality,
        // which the analytic integral gives as 2 pi l_B sigma^2 mu = sigma/q exactly.
        val mu = dna.gouyChapmanLength(lb)
        val analytic = dna.contactDensity(lb) * mu
        assert(analytic.isCloseTo(dna.surfaceChargeDensity / 2.0, relativeTolerance = 1e-12))
        // and the same by Simpson quadrature of the profile itself over [0, 100 mu],
        // plus the analytic tail beyond it — the profile decays only as z^-2, so the tail
        // carries 1% of the coverage and dropping it would be the error, not the quadrature.
        val cutoff = 100.0 * mu
        val steps = 100_000
        val h = cutoff / steps
        var sum = 0.0
        for (i in 0..steps) {
            val weight = when {
                i == 0 || i == steps -> 1.0
                i % 2 == 1 -> 4.0
                else -> 2.0
            }
            sum += weight * dna.meanFieldDensity(i * h, lb)
        }
        val tail = dna.contactDensity(lb) * mu / (cutoff / mu + 1.0)
        val quadrature = sum * h / 3.0 + tail
        assert(abs(quadrature - analytic) / analytic < 1e-9)
    }

    // gate 4 — numerical convergence

    @Test
    fun `gate 4 should solve the transcendental pressure equation to machine precision`() {
        // Naji, after Eq. (13): Lambda is fixed by sqrt(L) tan(sqrt(L) Delta/(2 mu)) = 1.
        // The solver is bisection on the principal branch, which is unconditionally
        // convergent there because the left-hand side increases monotonically from 0 to
        // infinity across (0, (pi/r)^2). Verified by substitution, not by trusting the solver.
        listOf(3.0, 10.0, 42.0, 58.8, 84.0, 1000.0).forEach { r ->
            val lambda = poissonBoltzmannPressureCoefficient(r)
            val residual = sqrt(lambda) * tan(sqrt(lambda) * r / 2.0) - 1.0
            assert(abs(residual) < 1e-9)
            // and the root always sits below the pole of the tangent
            assert(sqrt(lambda) * r / 2.0 < PI / 2.0)
        }
    }

    @Test
    fun `gate 4 should place the mean-field validity gap where the loop correction equals the leading term`() {
        // The boundary is DEFINED as deviation == 1 and then found by bisection;
        // this asserts the definition is met at the returned point, and that the
        // deviation brackets it on both sides.
        val coupling = dna.couplingParameter(lb)
        val mu = dna.gouyChapmanLength(lb)
        val gap = meanFieldValidityGap(coupling, mu)!!
        assert(meanFieldDeviation(coupling, gap / mu).isCloseTo(1.0, relativeTolerance = 1e-6))
        assert(meanFieldDeviation(coupling, 0.9 * gap / mu) > 1.0)
        assert(meanFieldDeviation(coupling, 1.1 * gap / mu) < 1.0)
    }

    @Test
    fun `gate 4 should agree with the closed-form Netz loop-expansion criterion to within a fifth`() {
        // Two independent statements of the same boundary: Naji Eq. (20), (Delta/mu)/ln(Delta/mu) > Xi,
        // which drops the sub-leading terms of Eq. (19), against the full ratio of Eq. (19) to
        // Eq. (13). They must land in the same place or one of them is mis-transcribed.
        val coupling = dna.couplingParameter(lb)
        val mu = dna.gouyChapmanLength(lb)
        val full = meanFieldValidityGap(coupling, mu)!!
        val closedForm = loopExpansionValidityGap(coupling, mu)!!
        assert(full.isCloseTo(12.91, relativeTolerance = 1e-3))
        assert(closedForm.isCloseTo(13.52, relativeTolerance = 1e-3))
        assert(abs(full - closedForm) / closedForm < 0.2)
    }

    // gate 5 — literature cross-check against the primary source

    @Test
    fun `gate 5 should reproduce the Naji table I entry for DNA with divalent counterions`() {
        // Naji et al. (2005) Table I lists, for DNA at sigma_s = 0.9 e/nm^2 with q = 2 (Mn2+):
        // mu = 1.2 A, Xi = 22.4, xi = 8.2, computed with l_B = 7.1 A.
        // Reproduced here at THEIR parameters — this is the cross-check — and then reported
        // at ours (sigma = 0.936 e/nm^2 derived from the B-DNA rise, l_B = 0.7141 nm).
        val theirs = ChargedSurface(surfaceChargeDensity = 0.9, counterionValency = 2)
        assert(theirs.couplingParameter(0.71).isCloseTo(22.8, relativeTolerance = 0.02))
        assert(theirs.gouyChapmanLength(0.71).isCloseTo(0.125, relativeTolerance = 0.05))
        // ours, 7% away, which is the difference between sigma = 0.9 and 0.936
        assert(dna.couplingParameter(lb).isCloseTo(24.00, relativeTolerance = 1e-3))
        assert(dna.gouyChapmanLength(lb).isCloseTo(0.11902, relativeTolerance = 1e-3))
        assert(abs(dna.couplingParameter(lb) - 22.4) / 22.4 < 0.08)
    }

    @Test
    fun `gate 5 should recover the published Wigner crystallisation threshold from the plasma parameter`() {
        // Naji Section II: "Wigner crystallization of the 2D one-component plasma is known to
        // occur for Gamma > Gamma_c ~ 125, which corresponds to Xi > Xi_c ~ 3.1e4".
        // With Gamma = sqrt(Xi/2) that is Xi_c = 2 * 125^2 = 31250 — recovering their 3.1e4
        // and, in the other direction, confirming the sqrt(pi) prefactor convention of Eq. (5).
        assert(couplingParameterOfPlasmaParameter(WIGNER_CRYSTAL_PLASMA_PARAMETER).isCloseTo(31250.0))
        assert(
            abs(couplingParameterOfPlasmaParameter(WIGNER_CRYSTAL_PLASMA_PARAMETER) - 3.1e4) / 3.1e4 < 0.01
        )
        // our surface is nowhere near it: a strongly correlated liquid, not a crystal
        assert(dna.plasmaParameter(lb).isCloseTo(3.4641, relativeTolerance = 1e-4))
        assert(dna.plasmaParameter(lb) < WIGNER_CRYSTAL_PLASMA_PARAMETER / 30.0)
    }

    @Test
    fun `gate 5 should carry the published attraction thresholds as named constants`() {
        // Naji Fig. 5b and Section IV C: "Attraction sets in for Xi > 12 and a first-order
        // phase transition occurs at Xi ~ 17"; Section V: with a dielectric jump at the walls
        // "the onset of attraction is shifted to somewhat larger coupling parameters (Xi ~ 30)".
        // These are the numbers the verdict is read against, so they are constants with a
        // citation rather than magic numbers inside a conditional.
        assert(ATTRACTION_ONSET_COUPLING == 12.0)
        assert(UNBINDING_TRANSITION_COUPLING == 17.0)
        assert(IMAGE_CHARGE_ATTRACTION_ONSET_COUPLING == 30.0)
        // and the Gen-1 tile sits between the second and the third
        assert(dna.couplingParameter(lb) > UNBINDING_TRANSITION_COUPLING)
        assert(dna.couplingParameter(lb) < IMAGE_CHARGE_ATTRACTION_ONSET_COUPLING)
    }

    @Test
    fun `gate 5 should place the Rouzina-Bloomfield attraction range far below the working gap`() {
        // Naji Eq. (24): correlation attraction between two like-charged walls requires
        // Delta < a_perp. For the Gen-1 tile a_perp = 1.46 nm, while the polymer layer holds
        // the tile 5-10 nm off the electrode. The qualitative failure mode of mean field is
        // therefore OUT OF GEOMETRIC RANGE, however large Xi is.
        assert(dna.lateralCounterionSpacing.isCloseTo(1.46160, relativeTolerance = 1e-4))
        assert(dna.lateralCounterionSpacing < 5.0)
    }

    @Test
    fun `gate 5 should show the divalence and not the surface charge is what breaks mean field`() {
        // The same DNA surface with monovalent counterions: Xi = 3.0 instead of 24.0, and the
        // deviation at the 7 nm working gap falls from 163% to 36%. This is the sensitivity
        // that decides the buffer question, and it is a factor of q^3 in Xi combined with a
        // factor of q in mu.
        val monovalent = ChargedSurface(dna.surfaceChargeDensity, counterionValency = 1)
        assert(monovalent.couplingParameter(lb).isCloseTo(3.0000, relativeTolerance = 1e-3))
        val divalentDeviation = meanFieldDeviation(
            dna.couplingParameter(lb), 7.0 / dna.gouyChapmanLength(lb)
        )
        val monovalentDeviation = meanFieldDeviation(
            monovalent.couplingParameter(lb), 7.0 / monovalent.gouyChapmanLength(lb)
        )
        assert(divalentDeviation.isCloseTo(1.634, relativeTolerance = 1e-3))
        assert(monovalentDeviation.isCloseTo(0.3549, relativeTolerance = 1e-3))
        assert(divalentDeviation / monovalentDeviation > 4.0)
    }

    @Test
    fun `gate 5 should quantify the mean-field deviation across the whole 5 to 10 nm working range`() {
        // The number the acceptance predicate asks for. Xi = 24.0, mu = 0.119 nm:
        // the one-loop correction is 214% / 163% / 123% of the leading PB term at 5 / 7 / 10 nm.
        // Greater than unity means the loop expansion has broken down — PB is not merely
        // inaccurate here, it is uncontrolled.
        val coupling = dna.couplingParameter(lb)
        val mu = dna.gouyChapmanLength(lb)
        assert(meanFieldDeviation(coupling, 5.0 / mu).isCloseTo(2.137, relativeTolerance = 1e-3))
        assert(meanFieldDeviation(coupling, 7.0 / mu).isCloseTo(1.634, relativeTolerance = 1e-3))
        assert(meanFieldDeviation(coupling, 10.0 / mu).isCloseTo(1.228, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 1 should reject an unphysical surface`() {
        try {
            ChargedSurface(surfaceChargeDensity = 0.0, counterionValency = 2)
            throw AssertionError("should have rejected a zero surface charge density")
        } catch (e: IllegalArgumentException) {
            assert(e.message!!.contains("surfaceChargeDensity"))
        }
        try {
            ChargedSurface(surfaceChargeDensity = 1.0, counterionValency = 0)
            throw AssertionError("should have rejected a zero counterion valency")
        } catch (e: IllegalArgumentException) {
            assert(e.message!!.contains("counterionValency"))
        }
    }
}
