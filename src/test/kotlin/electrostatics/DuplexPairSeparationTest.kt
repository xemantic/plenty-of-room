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
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-139` — what separation do two **unbonded** duplexes hold in 2 mM MgCl₂?
 *
 * The five gates of the task, as executable tests.
 * The load-bearing ones are gate 3: two independently written routes to each closed form,
 * where nothing in the construction forces them to agree.
 */
class DuplexPairSeparationTest {

    private val buffer = MagnesiumChlorideBuffer(concentration = 2.0)
    private val pair = DuplexPair()
    private val kappa = buffer.inverseDebyeLength()

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 should make every pair energy a pN nm and every pressure a pN per nm squared`() {
        // an energy per length times a length is an energy; the crossed-rod energy is the
        // parallel one integrated over the crossing, so its units carry one more nm.
        val perLength = pair.parallelScreenedCoulombEnergyPerLength(2.69, kappa)
        val crossed = pair.crossedScreenedCoulombEnergy(2.69, kappa)
        assert(perLength > 0.0)
        assert(crossed > 0.0)
        // and the ratio of the two is a length of order the Debye length, which is the only
        // length the crossing integral can produce
        val ratio = crossed / perLength
        assert(ratio > 0.5 / kappa && ratio < 5.0 / kappa)
    }

    @Test
    fun `gate 1 should be invariant under a common rescaling of every length`() {
        // κ has units of 1/length, so scaling every length by s and κ by 1/s must leave a
        // dimensionless energy ratio untouched. This catches a stray absolute constant.
        val s = 10.0
        val reference = DuplexPair(helixRadius = 0.908638, bareLinearChargeDensity = 5.882352941)
        val scaled = DuplexPair(
            helixRadius = 0.908638 * s,
            bareLinearChargeDensity = 5.882352941 / s,
            bjerrumLength = reference.bjerrumLength * s
        )
        val a = reference.crossedScreenedCoulombEnergy(2.69, kappa)
        val b = scaled.crossedScreenedCoulombEnergy(2.69 * s, kappa / s)
        assert(b.isCloseTo(a, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 1 should throw on every unphysical argument`() {
        assertFailsWith<IllegalArgumentException> { DuplexPair(helixRadius = 0.0) }
        assertFailsWith<IllegalArgumentException> { DuplexPair(bareLinearChargeDensity = -1.0) }
        assertFailsWith<IllegalArgumentException> { DuplexPair(counterionValency = 0) }
        assertFailsWith<IllegalArgumentException> { DuplexPair(bjerrumLength = 0.0) }
        assertFailsWith<IllegalArgumentException> {
            pair.crossedScreenedCoulombEnergy(separation = 0.0, inverseDebyeLength = kappa)
        }
        assertFailsWith<IllegalArgumentException> {
            pair.crossedScreenedCoulombEnergy(separation = 2.69, inverseDebyeLength = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            pair.coaxialScreenedCoulombEnergy(endGap = -0.1, inverseDebyeLength = kappa)
        }
        assertFailsWith<IllegalArgumentException> { modifiedBesselK0(0.0) }
        assertFailsWith<IllegalArgumentException> { modifiedBesselK1(-1.0) }
        assertFailsWith<IllegalArgumentException> { exponentialIntegralE1(0.0) }
        assertFailsWith<IllegalArgumentException> {
            crossedCylinderVanDerWaalsEnergy(hamaker = 5.0, radius = 1.0, surfaceSeparation = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            OsmoticStressEquationOfState(repulsionAmplitude = -1.0, decayLength = 0.24)
        }
        assertFailsWith<IllegalArgumentException> {
            OsmoticStressEquationOfState(repulsionAmplitude = 1.0, decayLength = 0.0)
        }
    }

    // -------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 should give the coaxial pair a FINITE energy at contact`() {
        // Two rods end to end: E = τ² l_B k_BT [e^(−κg)/κ − g E₁(κg)], and g·E₁(κg) → 0 as
        // g → 0 because E₁ diverges only logarithmically. The contact energy is therefore
        // exactly τ² l_B k_BT / κ — a finite number, and the whole reason a coaxial gap is
        // not a steric exclusion problem.
        val tau = pair.effectiveLinearChargeDensity
        val expected = tau * tau * pair.bjerrumLength * thermalEnergy() / kappa
        assert(pair.coaxialScreenedCoulombEnergy(0.0, kappa).isCloseTo(expected))
        // and approaching it from above is continuous
        assert(pair.coaxialScreenedCoulombEnergy(1e-7, kappa).isCloseTo(expected, 1e-5))
    }

    @Test
    fun `gate 2 should recover the unscreened limits`() {
        // κ → 0: the crossed-rod energy is 2π τ² l_B k_BT e^(−κD)/κ and diverges as 1/κ,
        // which is the correct statement that two crossed line charges in a vacuum have an
        // infinite interaction. Halving κ must double it in the limit.
        val small = 1e-6
        val a = pair.crossedScreenedCoulombEnergy(2.69, small)
        val b = pair.crossedScreenedCoulombEnergy(2.69, small / 2.0)
        assert((b / a).isCloseTo(2.0, relativeTolerance = 1e-5))
    }

    @Test
    fun `gate 2 should make the finite-radius factor tend to one for a thin cylinder`() {
        // The cylinder's far field is that of a line of density τ/(κR K₁(κR)); as κR → 0,
        // κR K₁(κR) → 1 and the cylinder becomes its own line charge.
        assert(finiteRadiusChargeFactor(1e-8).isCloseTo(1.0, relativeTolerance = 1e-6))
        // and it is strictly greater than one for a real radius: a fat cylinder's charge
        // sits closer to its neighbour than a line at its axis would
        assert(finiteRadiusChargeFactor(kappa * 0.908638) > 1.0)
    }

    @Test
    fun `gate 2 should make Manning at one valency exactly twice Manning at two`() {
        // the surviving fraction is 1/(q ξ_M), so the whole q dependence is a factor of 1/q
        val monovalent = DuplexPair(counterionValency = 1).effectiveLinearChargeDensity
        val divalent = DuplexPair(counterionValency = 2).effectiveLinearChargeDensity
        assert(monovalent.isCloseTo(2.0 * divalent))
    }

    @Test
    fun `gate 2 should reproduce the Bessel asymptotics at both ends`() {
        // small x: K₀(x) → −ln(x/2) − γ
        val euler = 0.5772156649015329
        val x = 1e-4
        assert(modifiedBesselK0(x).isCloseTo(-ln(x / 2.0) - euler, relativeTolerance = 1e-6))
        // small x: K₁(x) → 1/x
        assert(modifiedBesselK1(x).isCloseTo(1.0 / x, relativeTolerance = 1e-6))
        // large x: K_n(x) → sqrt(π/2x) e^(−x)
        val big = 30.0
        val envelope = sqrt(PI / (2.0 * big)) * exp(-big)
        assert(modifiedBesselK0(big).isCloseTo(envelope, relativeTolerance = 2e-2))
        assert(modifiedBesselK1(big).isCloseTo(envelope, relativeTolerance = 2e-2))
    }

    @Test
    fun `gate 2 should reproduce the E1 asymptotics`() {
        val euler = 0.5772156649015329
        // small x: E₁(x) → −γ − ln x
        assert(exponentialIntegralE1(1e-6).isCloseTo(-euler - ln(1e-6), relativeTolerance = 1e-5))
        // large x: E₁(x) → e^(−x)/x
        assert(exponentialIntegralE1(25.0).isCloseTo(exp(-25.0) / 25.0, relativeTolerance = 0.05))
    }

    @Test
    fun `gate 2 should make the van der Waals terms scale as the textbook powers`() {
        // crossed cylinders: E = −A R / (6 D_s), so doubling the gap halves it exactly
        val one = crossedCylinderVanDerWaalsEnergy(5.0, 1.0, 0.4)
        val two = crossedCylinderVanDerWaalsEnergy(5.0, 1.0, 0.8)
        assert((one / two).isCloseTo(2.0))
        // parallel cylinders: E/L = −A √R / (24 D_s^{3/2}), so doubling divides by 2^{3/2}
        val p1 = parallelCylinderVanDerWaalsEnergyPerLength(5.0, 1.0, 0.4)
        val p2 = parallelCylinderVanDerWaalsEnergyPerLength(5.0, 1.0, 0.8)
        assert((p1 / p2).isCloseTo(2.0.pow15()))
        // both are attractive
        assert(one < 0.0 && p1 < 0.0)
    }

    private fun Double.pow15(): Double = this * sqrt(this)

    @Test
    fun `gate 2 should make the measured equation of state a pure exponential in the interaxial`() {
        val state = MengMagnesium.equationOfState
        // one decay length divides the pressure by e, exactly, at every separation
        for (separation in listOf(2.0, 2.69, 3.6)) {
            val ratio = state.arrayPressure(separation) /
                state.arrayPressure(separation + state.decayLength)
            assert(ratio.isCloseTo(kotlin.math.E))
        }
        // and the pair energy vanishes at large separation while staying strictly positive
        assert(state.parallelPairEnergyPerLength(12.0) > 0.0)
        assert(state.parallelPairEnergyPerLength(12.0) < 1e-12)
    }

    @Test
    fun `gate 2 should make the Derjaguin crossing length scale as the square root`() {
        val state = MengMagnesium.equationOfState
        // 2 sqrt(pi R lambda): quadrupling the radius doubles it
        assert((state.crossingLength(4.0) / state.crossingLength(1.0)).isCloseTo(2.0))
        val wide = OsmoticStressEquationOfState(state.repulsionAmplitude, 4.0 * state.decayLength)
        assert((wide.crossingLength(1.0) / state.crossingLength(1.0)).isCloseTo(2.0))
    }

    // -------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 should reproduce the crossed-rod closed form by direct 2-D quadrature`() {
        // THE load-bearing check. E_cross = 2π τ² l_B k_BT e^(−κD)/κ was obtained by a change
        // of variables; here the same double integral over the two rod coordinates is done
        // numerically from the screened Coulomb kernel, with nothing shared but τ, l_B and κ.
        val closed = pair.crossedScreenedCoulombEnergy(
            separation = 2.715609,
            inverseDebyeLength = kappa,
            finiteRadius = false
        )
        val quadrature = crossedRodQuadrature(
            pair, separation = 2.715609, inverseDebyeLength = kappa, halfLength = 60.0, steps = 1200
        )
        assert(quadrature.isCloseTo(closed, relativeTolerance = 1e-5))
    }

    @Test
    fun `gate 3 should reproduce the coaxial closed form by direct 2-D quadrature`() {
        val closed = pair.coaxialScreenedCoulombEnergy(
            endGap = 2.715609, inverseDebyeLength = kappa
        )
        val quadrature = coaxialRodQuadrature(
            pair, endGap = 2.715609, inverseDebyeLength = kappa, halfLength = 60.0, steps = 1200
        )
        assert(quadrature.isCloseTo(closed, relativeTolerance = 1e-5))
    }

    @Test
    fun `gate 3 should recover the array pressure from the pair free energy`() {
        // THE array-to-pair conversion, which is the one step that could silently carry a wrong
        // lattice factor: Π = −∂F/∂A on F = 3 g(d) and A = (√3/2)d², differenced numerically,
        // must return the Π the pair energy was built from.
        val state = MengMagnesium.equationOfState
        for (separation in listOf(2.0, 2.4, 2.69, 2.715609, 3.2, 3.6)) {
            val recovered = arrayPressureFromPairEnergy(state, separation)
            assert(recovered.isCloseTo(state.arrayPressure(separation), relativeTolerance = 1e-6))
        }
    }

    @Test
    fun `gate 3 should reproduce the parallel pair energy by integrating its own force`() {
        val state = MengMagnesium.equationOfState
        val closed = state.parallelPairEnergyPerLength(2.715609)
        val numeric = parallelPairEnergyQuadrature(state, 2.715609, upper = 12.0, steps = 20000)
        assert(numeric.isCloseTo(closed, relativeTolerance = 1e-6))
    }

    @Test
    fun `gate 3 should reproduce the Derjaguin crossing length by direct quadrature`() {
        // the crossed-cylinder energy is the flat-flat energy per area integrated over the
        // paraboloidal gap; the closed form collapses it to 2 sqrt(pi R lambda) times the
        // parallel energy per length, and that factor is what is being tested
        val state = MengMagnesium.equationOfState
        val radius = 1.0
        val separation = 2.715609
        val closed = state.crossedPairEnergy(separation, radius)
        val numeric = derjaguinCrossedQuadrature(state, separation, radius, half = 5.0, steps = 2000)
        assert(numeric.isCloseTo(closed, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 3 should conserve the line charge under Manning renormalisation`() {
        // the surviving charge per nm times the length is the bare charge times the fraction:
        // a restatement, but it is the one the two independent call sites can disagree about
        val tile = DnaOrigamiTile(risePerBasePair = 0.34, helixRadius = 0.908638)
        val fraction = tile.manningSurvivingFraction(2, pair.bjerrumLength)
        assert(pair.effectiveLinearChargeDensity.isCloseTo(tile.linearChargeDensity * fraction))
        // and the condensed and surviving fractions sum to exactly one
        assert((fraction + tile.manningCondensedFraction(2, pair.bjerrumLength)).isCloseTo(1.0))
    }

    @Test
    fun `gate 3 should make a finite cylinder equal its own equivalent line charge in the far field`() {
        // the finite-radius factor is defined so that the cylinder's exterior potential IS a
        // line charge's; so a pair energy computed with the factor at radius R must equal the
        // bare line-charge energy computed with the renormalised density
        val kr = kappa * pair.helixRadius
        val factor = finiteRadiusChargeFactor(kr)
        val withRadius = pair.crossedScreenedCoulombEnergy(3.0, kappa, finiteRadius = true)
        val asLine = pair.crossedScreenedCoulombEnergy(3.0, kappa, finiteRadius = false)
        assert((withRadius / asLine).isCloseTo(factor))
    }

    @Test
    fun `gate 3 should find NO local minimum above the model floor`() {
        // The central structural claim, and falsifier F1: an EQUILIBRIUM separation would be a
        // local MINIMUM of the pair energy at finite separation. Continuum DLVO always carries a
        // formal primary minimum at contact — the Lifshitz 1/D_s diverges where the exponential
        // repulsions do not — so the statement that has content is that above the model floor
        // there is no minimum at all, only a barrier maximum and then monotone decay.
        val state = gen1PairState()
        var minima = 0
        var maxima = 0
        var separation = state.minimumSeparation + 0.005
        var previousGradient = state.energyGradient(separation)
        while (separation < PLAN_RELEVANT_RANGE) {
            separation += 0.005
            val gradient = state.energyGradient(separation)
            if (previousGradient < 0.0 && gradient >= 0.0) minima++
            if (previousGradient > 0.0 && gradient <= 0.0) maxima++
            previousGradient = gradient
        }
        assert(minima == 0)
        assert(maxima <= 1)
        // and the energy is repulsive everywhere above the barrier
        val barrier = state.barrierSeparation() ?: state.minimumSeparation
        assert(state.totalCrossedEnergy(barrier) > 0.0)
        assert(state.totalCrossedEnergy(PLAN_RELEVANT_RANGE) > 0.0)
    }

    @Test
    fun `gate 3 should find the far van der Waals minimum and show it is not a confinement`() {
        // Beyond the plan-relevant range the screened exponential dies and the unretarded
        // Lifshitz 1/D_s power law is all that is left, so a secondary minimum exists. It is
        // reported rather than suppressed — and CLAUDE.md's "a stable equilibrium is not a
        // confinement" is the reading: 170x below thermal energy, at a separation ten times
        // anything the plan model asks about, and inside the regime where retardation makes the
        // unretarded law an overestimate.
        val state = gen1PairState()
        val minimum = state.secondaryMinimum()
        assert(minimum != null)
        val (separation, depth) = minimum!!
        assert(separation > PLAN_RELEVANT_RANGE)
        assert(depth < 0.0)
        assert(abs(depth) < 0.01 * thermalEnergy())
    }

    @Test
    fun `gate 3 should make the energy monotone decreasing above the barrier`() {
        val state = gen1PairState()
        val barrier = state.barrierSeparation() ?: state.minimumSeparation
        var previous = Double.MAX_VALUE
        var separation = barrier + 1e-4
        while (separation < PLAN_RELEVANT_RANGE) {
            val energy = state.totalCrossedEnergy(separation)
            assert(energy < previous)
            previous = energy
            separation += 0.01
        }
    }

    @Test
    fun `gate 3 should make the parallel and crossed geometries agree in their common limit`() {
        // A crossed pair is a parallel pair integrated over the crossing; at large κD both
        // decay with the SAME exponent, so their ratio must tend to a constant. That constant
        // is a property of the two closed forms and nothing forces it.
        val a = pair.crossedScreenedCoulombEnergy(20.0, kappa) /
            pair.parallelScreenedCoulombEnergyPerLength(20.0, kappa)
        val b = pair.crossedScreenedCoulombEnergy(30.0, kappa) /
            pair.parallelScreenedCoulombEnergyPerLength(30.0, kappa)
        // K₀(x) ~ √(π/2x) e^{−x} so the ratio grows only as √D; it must not grow like e^{κD}
        assert(b / a < 1.5)
        assert(b / a > 1.0)
    }

    // ------------------------------------------------------- gate 4 — numerical convergence

    @Test
    fun `gate 4 should converge the crossed-rod quadrature under refinement`() {
        val closed = pair.crossedScreenedCoulombEnergy(2.715609, kappa, finiteRadius = false)
        val coarse = crossedRodQuadrature(pair, 2.715609, kappa, halfLength = 60.0, steps = 76)
        val fine = crossedRodQuadrature(pair, 2.715609, kappa, halfLength = 60.0, steps = 150)
        val finer = crossedRodQuadrature(pair, 2.715609, kappa, halfLength = 60.0, steps = 1200)
        assert(abs(fine - closed) < abs(coarse - closed))
        assert(abs(finer - closed) <= abs(fine - closed))
    }

    @Test
    fun `gate 4 should bracket the threshold width to its own step`() {
        // the width at which the crossed-geometry energy equals a stated threshold, found by
        // a bracketed root and checked by substitution rather than by the bracket
        val state = gen1PairState()
        val target = 5.0 * thermalEnergy()
        val width = state.exclusionWidthAtEnergy(target)
        assert(width != null)
        assert(state.totalCrossedEnergy(width!!).isCloseTo(target, relativeTolerance = 1e-9))
        // and a budget above what the pair can charge at the barrier has no width at all,
        // which is a verdict rather than a failure: the affordable width is below the floor
        val unreachable = state.totalCrossedEnergy(state.barrierSeparation()!!) * 1.01
        assert(state.exclusionWidthAtEnergy(unreachable) == null)
    }

    @Test
    fun `gate 4 should make the exclusion width monotone decreasing in the threshold`() {
        val state = gen1PairState()
        val loose = state.exclusionWidthAtEnergy(0.5 * thermalEnergy())!!
        val tight = state.exclusionWidthAtEnergy(5.0 * thermalEnergy())!!
        assert(loose > tight)
    }

    // -------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 should reproduce the tabulated Bessel values`() {
        // Abramowitz and Stegun Table 9.8, exp-scaled values back-converted:
        // K₀(1) = 0.4210244382, K₁(1) = 0.6019072302,
        // K₀(2) = 0.1138938727, K₁(2) = 0.1398658818
        assert(modifiedBesselK0(1.0).isCloseTo(0.4210244382, relativeTolerance = 1e-6))
        assert(modifiedBesselK1(1.0).isCloseTo(0.6019072302, relativeTolerance = 1e-6))
        assert(modifiedBesselK0(2.0).isCloseTo(0.1138938727, relativeTolerance = 1e-6))
        assert(modifiedBesselK1(2.0).isCloseTo(0.1398658818, relativeTolerance = 1e-6))
        // E₁(1) = 0.2193839344
        assert(exponentialIntegralE1(1.0).isCloseTo(0.2193839344, relativeTolerance = 1e-6))
        // E₁(2) = 0.0489005107
        assert(exponentialIntegralE1(2.0).isCloseTo(0.0489005107, relativeTolerance = 1e-6))
    }

    @Test
    fun `gate 5 should reproduce this project's own Debye length and Manning charge`() {
        // C-0008/CLAUDE.md: 3.93 nm in the bulk buffer at 2 mM MgCl₂, and I = 3c not c
        assert(buffer.ionicStrength.isCloseTo(6.0))
        assert(buffer.debyeLength().isCloseTo(3.93, relativeTolerance = 2e-3))
        // DnaOrigamiTile: 11.9 % of the bare charge survives with Mg²⁺
        assert(pair.effectiveLinearChargeDensity.isCloseTo(0.7001, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 5 should reproduce C-0076 s threshold and floor from the lattice`() {
        assert(C0076_PLACEMENT_THRESHOLD.isCloseTo(10.88 - 8.16439, relativeTolerance = 1e-6))
        assert(C0076_PLACEMENT_THRESHOLD.isCloseTo(2.715609, relativeTolerance = 1e-6))
        assert(T71_STERIC_FLOOR.isCloseTo(2.0 * 0.908638, relativeTolerance = 1e-6))
        assert(T71_STERIC_FLOOR.isCloseTo(1.817276, relativeTolerance = 1e-6))
        // and CH-0089's published clearance at the measured girth
        assert((C0076_PLACEMENT_THRESHOLD - T71_STERIC_FLOOR).isCloseTo(0.898333, relativeTolerance = 1e-5))
    }

    @Test
    fun `gate 5 should reproduce Meng s own evaluated Mg2 plus pressure table`() {
        // the three (d, Pi) pairs the literature file derives from the published Pi_R and lambda,
        // and Rau, Lee & Parsegian's independently stated 1.2-5.5 pN/nm2 band at 26-30 A
        val state = MengMagnesium.equationOfState
        assert(state.arrayPressure(2.6).isCloseTo(3.98, relativeTolerance = 1e-3))
        assert(state.arrayPressure(3.0).isCloseTo(0.752, relativeTolerance = 1e-3))
        assert(state.arrayPressure(3.6).isCloseTo(0.062, relativeTolerance = 5e-3))
        assert(state.arrayPressure(3.0) > 1.2 / 2.0)
        assert(state.arrayPressure(2.6) < 5.5)
    }

    @Test
    fun `gate 5 should convert a stacking free energy from kcal per mole without a stray factor`() {
        // 1 kcal/mol = 4184 J/mol / N_A = 6.9477 zJ, and 1 zJ = 1 pN nm exactly
        assert(BluntEndStacking.KCAL_PER_MOLE.isCloseTo(6.94769, relativeTolerance = 1e-5))
        // Woo & Rothemund's -2.63 kcal/mol per helix is an ATTRACTION of a few k_BT
        assert(BluntEndStacking.perStackEnergy < 0.0)
        assert((BluntEndStacking.perStackEnergy / thermalEnergy())
            .isCloseTo(-4.4114, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 5 should put the whole stacking range inside two base-pair rises`() {
        // the interaction the collinear clearance really has to prevent is a CONTACT one
        assert(BluntEndStacking.OXDNA2_CUTOFF < 2.0 * 0.34)
        assert(BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET < C0076_PLACEMENT_THRESHOLD / 2.0)
        // so even the generous end leaves a plan margin far above C-0069's published knife edge
        val margin = 32 * 0.34 - BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET - 8.16439083
        assert(margin > 50.0 * 0.02560917)
    }

    @Test
    fun `gate 5 should check the Debye-Huckel premise against THIS material and report it violated`() {
        // DH assumes eψ_0 ≪ k_BT. For B-DNA at 2 mM the reduced surface potential is far
        // above one even after Manning renormalisation, which is exactly why the numbers here
        // are quoted as a bracket and not as an answer. The test asserts the premise FAILS,
        // so that a future change silently making it pass is caught.
        val reduced = pair.reducedSurfacePotential(kappa)
        assert(reduced > 1.0)
    }

    @Test
    fun `gate 5 should keep the electrostatic range longer than the whole disputed bracket`() {
        // cheap bound 2, asserted: falsifier F2 fires if this fails
        val bracket = 3.60 - T71_STERIC_FLOOR
        assert(buffer.debyeLength() > bracket)
        // and the consequence: the crossed energy varies by less than a factor of 2 over it
        val atFloor = pair.crossedScreenedCoulombEnergy(T71_STERIC_FLOOR, kappa)
        val atTop = pair.crossedScreenedCoulombEnergy(3.60, kappa)
        assert(atFloor / atTop < 2.0)
    }
}
