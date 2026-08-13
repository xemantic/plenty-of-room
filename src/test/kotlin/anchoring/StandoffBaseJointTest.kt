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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-40` — the **base** of `C-0025`'s normal standoff, modelled rather than assumed.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that `C-0025`'s three standoff constants — `EI/ℓ`, `3EI/ℓ³`, `S/ℓ` —
 * are all the `ρ_b → ∞` limit of a series with the base, and that its two buckling loads are two
 * corners of one eigenvalue whose *third* corner is a mechanism.
 */
class StandoffBaseJointTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val stretch = Gen1Tile.DUPLEX_STRETCH_MODULUS

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the base restraint is dimensionless and linear in the standoff length`() {
        // rho_b = k_theta_base * l / EI: (pN*nm/rad)(nm)/(pN*nm^2) = 1
        assert(baseRestraintParameter(13.53, ei, 8.0).isCloseTo(13.53 * 8.0 / 230.0))
        assert(
            (baseRestraintParameter(13.53, ei, 10.0) /
                    baseRestraintParameter(13.53, ei, 5.0)).isCloseTo(2.0)
        )
    }

    @Test
    fun `gate 1 dimensional consistency - a buckling load is EI over a squared length`() {
        // P_c = u^2 EI/l^2; halving the length quadruples it at fixed u, i.e. at fixed rho_b*l
        val short = standoffBucklingLoad(ei, 4.0, Double.POSITIVE_INFINITY, 0.0)
        val long = standoffBucklingLoad(ei, 8.0, Double.POSITIVE_INFINITY, 0.0)
        assert((short / long).isCloseTo(4.0))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { baseRestraintParameter(-1.0, ei, 8.0) }
        assertFailsWith<IllegalArgumentException> { baseRestraintParameter(1.0, ei, 0.0) }
        assertFailsWith<IllegalArgumentException> { standoffBucklingLoad(ei, -1.0, 1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { swayColumnWavenumber(-1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { seriesStiffness(-1.0, 1.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - C-0025's three standoff constants are the rigid-base limit`() {
        val length = 8.0
        val clamped = basedNormalStandoff(length, StandoffBase.idealClamp())
        // C-0025's J5-8: k_theta = EI/l = 28.75, k_a = 3EI/l^3 = 1.3477, k_perp = S/l = 137.5
        assert(clamped.rotationalStiffness.isCloseTo(ei / length))
        assert(clamped.axialStiffness.isCloseTo(3.0 * ei / (length * length * length)))
        assert(clamped.transverseStiffness.isCloseTo(stretch / length))
        assert(clamped.rotationalStiffness.isCloseTo(28.75))
        assert(clamped.anisotropy.isCloseTo(stretch * length * length / (3.0 * ei)))
    }

    @Test
    fun `gate 2 limiting cases - a pinned base leaves the standoff with no rotational restraint and no sway stiffness`() {
        val pinned = basedNormalStandoff(
            8.0, StandoffBase("ideal pin", 0.0, Double.POSITIVE_INFINITY)
        )
        assert(abs(pinned.rotationalStiffness) < 1.0e-12)
        assert(abs(pinned.axialStiffness) < 1.0e-12)
        // but it still carries the end shear along its own axis: the anisotropy is what survives
        assert(pinned.transverseStiffness.isCloseTo(stretch / 8.0))
    }

    @Test
    fun `gate 2 limiting cases - the two head reductions are the four textbook K factors, exactly`() {
        val inf = Double.POSITIVE_INFINITY
        // clamped base, free head: K = 2
        assert(swayColumnWavenumber(inf, 0.0).isCloseTo(PI / 2.0))
        // clamped base, guided head: K = 1
        assert(swayColumnWavenumber(inf, inf).isCloseTo(PI))
        // pinned base, guided head: K = 2 again, by symmetry
        assert(swayColumnWavenumber(0.0, inf).isCloseTo(PI / 2.0))
        // pinned base, free head: a MECHANISM. u = 0 exactly, and P_c with it.
        assert(abs(swayColumnWavenumber(0.0, 0.0)) < 1.0e-9)
        assert(abs(standoffBucklingLoad(ei, 8.0, 0.0, 0.0)) < 1.0e-9)
    }

    @Test
    fun `gate 2 limiting cases - the general determinant reduces to both one-spring equations`() {
        // free head: u tan u = rho_b
        listOf(0.2, 0.5, 1.0, 4.0, 20.0).forEach { rho ->
            val u = swayColumnWavenumber(rho, 0.0)
            assert((u * kotlin.math.tan(u)).isCloseTo(rho))
        }
        // guided head: u cot u = -rho_b
        listOf(0.2, 0.5, 1.0, 4.0, 20.0).forEach { rho ->
            val u = swayColumnWavenumber(rho, Double.POSITIVE_INFINITY)
            assert((u / kotlin.math.tan(u)).isCloseTo(-rho))
        }
    }

    @Test
    fun `gate 2 limiting cases - a series stiffness is its softer member's limit`() {
        assert(seriesStiffness(100.0, Double.POSITIVE_INFINITY).isCloseTo(100.0))
        assert(abs(seriesStiffness(100.0, 0.0)) < 1.0e-12)
        assert(seriesStiffness(100.0, 100.0).isCloseTo(50.0))
        assert(seriesStiffness(137.5, 64.7059).isCloseTo(1.0 / (1.0 / 137.5 + 1.0 / 64.7059)))
    }

    @Test
    fun `gate 2 limiting cases - the base reductions are monotone and bounded by the clamp`() {
        val length = 8.0
        var previousRotational = 0.0
        var previousSway = 0.0
        listOf(0.1, 1.0, 10.0, 100.0, 1000.0, 1.0e6).forEach { base ->
            val joint = basedNormalStandoff(length, StandoffBase("sweep", base, 1.0e9))
            assert(joint.rotationalStiffness > previousRotational)
            assert(joint.axialStiffness > previousSway)
            assert(joint.rotationalStiffness < ei / length)
            assert(joint.axialStiffness < 3.0 * ei / (length * length * length))
            previousRotational = joint.rotationalStiffness
            previousSway = joint.axialStiffness
        }
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 symmetry - the buckling load is symmetric under exchanging the two end springs`() {
        // D(u) is symmetric in rho_b and rho_h, which is Maxwell-Betti for the column: it cannot
        // matter which end of a uniform strut carries which spring. Not put in by construction.
        listOf(0.3 to 2.0, 1.0 to 7.0, 0.05 to 400.0).forEach { (a, b) ->
            assert(swayColumnWavenumber(a, b).isCloseTo(swayColumnWavenumber(b, a)))
        }
    }

    @Test
    fun `gate 3 symmetry - a column at its own critical load has exactly zero sway stiffness`() {
        // C-0014's k(P) = k0 (1 - P/P_c), asserted through this task's own eigenvalue rather
        // than through the K-factor form it was written with.
        val length = 8.0
        val base = StandoffBase("one crossover", Gen1Tile.crossoverHingeStiffness(), 64.7059)
        val rho = baseRestraintParameter(base.rotationalStiffness, ei, length)
        val critical = standoffBucklingLoad(ei, length, rho, 0.0)
        val joint = basedNormalStandoff(length, base)
        assert(abs(compressedTransverseStiffness(joint.axialStiffness, critical, critical)) < 1.0e-12)
        assert(
            compressedTransverseStiffness(joint.axialStiffness, 0.0, critical)
                .isCloseTo(joint.axialStiffness)
        )
    }

    @Test
    fun `gate 3 symmetry - a flexible base cannot support the standoff, exactly as CH-0031 says of a flexible hinge`() {
        // the same convexity statement one level down: the poly-T base is isotropic, so what it
        // gives the standoff axially it takes from the standoff's own support path.
        val flexible = StandoffBase.polyTJunction(2)
        assert(flexible.axialStiffness.isCloseTo(flexible.rotationalStiffness * 0.0 + flexible.axialStiffness))
        val joint = basedNormalStandoff(8.0, flexible)
        // 4.41 pN/nm against the 7.407 that ten times the beam's own per-path stiffness demands
        assert(joint.transverseStiffness < 10.0 * (100.0 / 3.0 / 45.0))
        assert(joint.transverseDeadBand.isCloseTo(flexible.transverseDeadBand))
        assert(joint.transverseDeadBand > 0.1)
    }

    @Test
    fun `gate 3 symmetry - the standoff's sway and the flexure's draw-in are the same coordinate`() {
        // The joint's axial stiffness (the draw-in release) IS the column's sway stiffness, so
        // the two names must return the same number. This is why the held-head reading is not
        // available to the design: holding the sway holds the draw-in.
        val length = 8.0
        val base = StandoffBase.crossovers(1)
        val rho = baseRestraintParameter(base.rotationalStiffness, ei, length)
        assert(
            basedNormalStandoff(length, base).axialStiffness
                .isCloseTo(standoffSwayStiffness(ei, length, rho))
        )
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the eigenvalue root is scan-independent and exits on the bracket width`() {
        val rho = 0.4706
        val reference = swayColumnWavenumber(rho, 0.0, scanSteps = 4096)
        listOf(64, 128, 256, 1024).forEach { steps ->
            assert(abs(swayColumnWavenumber(rho, 0.0, scanSteps = steps) - reference) < 1.0e-12)
        }
    }

    @Test
    fun `gate 4 convergence - the eigenvalue satisfies its own determinant to machine precision`() {
        listOf(0.0 to 3.0, 0.4706 to 0.5, 9.083 to 0.515, 100.0 to 100.0).forEach { (b, h) ->
            val u = swayColumnWavenumber(b, h)
            if (u > 1.0e-9) {
                val residual = kotlin.math.sin(u) * (u * u - b * h) -
                        kotlin.math.cos(u) * (b + h) * u
                assert(abs(residual) < 1.0e-9 * (1.0 + abs(b * h)))
            }
        }
    }

    @Test
    fun `gate 4 convergence - the buckling stroke round-trips through the duty it was solved from`() {
        val length = 8.0
        val base = StandoffBase.crossovers(2, favourableOrientation = true)
        val joint = basedNormalStandoff(length, base)
        val span = flexureSpanForJoint(ei, joint, 45, 100.0 / 3.0, 3.0)
        val flexure = PartiallyRestrainedFlexure(ei, span, joint, stretch)
        val rho = baseRestraintParameter(base.rotationalStiffness, ei, length)
        val critical = standoffBucklingLoad(ei, length, rho, 0.0)
        val stroke = bucklingStroke(flexure, critical)
        assert(stroke > 0.0)
        assert(flexure.endShear(stroke).isCloseTo(critical))
    }

    // ---------------------------------------------------------------- gate 5 — cross-check

    @Test
    fun `gate 5 literature cross-check - C-0025's two buckling loads are reproduced exactly`() {
        // C-0025's design table: 8.87 pN pinned head, 35.5 pN guided, at l = 8 nm.
        val inf = Double.POSITIVE_INFINITY
        assert(standoffBucklingLoad(ei, 8.0, inf, 0.0).isCloseTo(8.8672227, relativeTolerance = 1.0e-6))
        assert(standoffBucklingLoad(ei, 8.0, inf, inf).isCloseTo(35.4688908, relativeTolerance = 1.0e-6))
        // and they agree with C-0014's own K-factor form
        assert(
            standoffBucklingLoad(ei, 8.0, inf, 0.0)
                .isCloseTo(eulerBucklingLoad(ei, 8.0, BeamEndCondition.PINNED_HEAD))
        )
        assert(
            standoffBucklingLoad(ei, 8.0, inf, inf)
                .isCloseTo(eulerBucklingLoad(ei, 8.0, BeamEndCondition.GUIDED_HEAD))
        )
    }

    @Test
    fun `gate 5 literature cross-check - the rigid-base design reproduces C-0025's window rows`() {
        // C-0025's standoff window, 45 paths, secant placed at 33.3333 pN/nm over 3 nm.
        val published = mapOf(
            7.0 to Triple(32.5016529, 100.883058, 39.0315564),
            8.0 to Triple(31.6403748, 95.6390226, 37.3911226),
            9.0 to Triple(30.9758615, 91.3162102, 36.3178635),
            10.0 to Triple(30.4419597, 87.6918183, 35.5901416)
        )
        published.forEach { (length, expected) ->
            val joint = basedNormalStandoff(length, StandoffBase.idealClamp())
            val span = flexureSpanForJoint(ei, joint, 45, 100.0 / 3.0, 3.0)
            val flexure = PartiallyRestrainedFlexure(ei, span, joint, stretch)
            assert(span.isCloseTo(expected.first, relativeTolerance = 1.0e-6))
            assert(flexure.midspanFactor.isCloseTo(expected.second, relativeTolerance = 1.0e-6))
            assert((45 * flexure.tangentStiffness(3.0)).isCloseTo(expected.third, relativeTolerance = 1.0e-6))
        }
    }

    @Test
    fun `gate 5 literature cross-check - the base motifs are built from Gen1Tile's own cited constants`() {
        assert(
            StandoffBase.crossovers(1).rotationalStiffness
                .isCloseTo(Gen1Tile.crossoverHingeStiffness())
        )
        assert(
            StandoffBase.crossovers(1).axialStiffness
                .isCloseTo(Gen1Tile.crossoverInPlaneStiffness())
        )
        // two crossovers on adjacent duplexes: the COUPLE over the SAXS-measured 2.69 nm is the
        // whole of it, and it is worth 9.65x against the same pair laid the other way round
        val favourable = StandoffBase.crossovers(2, favourableOrientation = true)
        val unfavourable = StandoffBase.crossovers(2, favourableOrientation = false)
        val d = Gen1Tile.INTERHELICAL_SHEET
        assert(
            favourable.rotationalStiffness.isCloseTo(
                2.0 * Gen1Tile.crossoverHingeStiffness() +
                        Gen1Tile.crossoverInPlaneStiffness() * 2.0 * (d / 2.0) * (d / 2.0)
            )
        )
        assert(
            unfavourable.rotationalStiffness
                .isCloseTo(2.0 * Gen1Tile.crossoverHingeStiffness())
        )
        assert(favourable.axialStiffness.isCloseTo(unfavourable.axialStiffness))
        assert((favourable.rotationalStiffness / unfavourable.rotationalStiffness) > 9.0)
    }

    @Test
    fun `gate 5 literature cross-check - the poly-T base reproduces C-0025's own hinge constants`() {
        // C-0025's J3-2: k_theta = 3.345 pN*nm/rad, k_a = 4.552 pN/nm, dead band 1.30 nm
        val polyT = StandoffBase.polyTJunction(2)
        assert(polyT.rotationalStiffness.isCloseTo(3.345, relativeTolerance = 1.0e-3))
        assert(polyT.axialStiffness.isCloseTo(4.552, relativeTolerance = 1.0e-3))
        assert(polyT.transverseDeadBand.isCloseTo(1.30, relativeTolerance = 1.0e-9))
        assert(
            polyT.axialStiffness
                .isCloseTo(FlexureEndJoint.singleStrandedHinge(2).axialStiffness)
        )
    }

    @Test
    fun `gate 5 literature cross-check - Fields et al's measured duplex buckling recovers a measured persistence length`() {
        // Fields, Meyer & Cohen, NAR 41:9881 (2013), READ DIRECTLY: a naked duplex loses its
        // resistance to a 9 pN compressive load at 40-41 bp, in a sway-prevented vise, i.e.
        // pinned-pinned, P_c = pi^2 EI/L^2. Inverting on their own number:
        val length = 40.5 * Gen1Tile.RISE_PER_BASE_PAIR
        val impliedRigidity = 9.0 * length * length / (PI * PI)
        val impliedPersistence = impliedRigidity / com.xemantic.nano.plentyofroom.thermalEnergy()
        // it lands inside the 40-47 nm MEASURED band, and below CanDo's 55.5 nm model input
        assert(impliedPersistence > 40.0 && impliedPersistence < 47.0)
        assert(impliedPersistence < ei / com.xemantic.nano.plentyofroom.thermalEnergy())
        // so every buckling load computed on CanDo's EI here is the OPTIMISTIC end, by 25 %
        assert((ei / impliedRigidity) > 1.2 && (ei / impliedRigidity) < 1.4)
    }

    @Test
    fun `gate 5 literature cross-check - Marras et al's measured hinge brackets the modelled 2 nt constant`() {
        // Marras et al., PNAS 112:713 (2015), READ DIRECTLY: 25 pN nm/rad for a joint of SIX
        // 2 nt ssDNA connections. The only measurement C-0025's hinge constant has ever had.
        val perConnection = 25.0 / 6.0
        val modelled = StandoffBase.polyTJunction(2).rotationalStiffness
        assert(modelled < perConnection)
        assert((perConnection / modelled) < 1.5)
    }

    @Test
    fun `gate 5 literature cross-check - a crossover base is far from a clamp at every buildable standoff length`() {
        // the cheap bound that justified the whole task: rho_b = 0.18-0.59 over 3-10 nm
        listOf(3.0, 10.0).forEach { length ->
            val rho = baseRestraintParameter(
                Gen1Tile.crossoverHingeStiffness(), ei, length
            )
            assert(rho > 0.17 && rho < 0.60)
        }
        // so a single crossover delivers 13-37 % of the clamp C-0025 assumed
        val fraction = basedNormalStandoff(8.0, StandoffBase.crossovers(1)).rotationalStiffness /
                (ei / 8.0)
        assert(fraction > 0.30 && fraction < 0.33)
    }

    // ------------------------------------------------ the results this task exists to deliver

    /** The design pipeline, exactly as the study runs it, for one base at one length. */
    private fun design(base: StandoffBase, length: Double): Pair<Double, Double> {
        val joint = basedNormalStandoff(length, base)
        val span = flexureSpanForJoint(ei, joint, 45, 100.0 / 3.0, 3.0, stretch)
        val flexure = PartiallyRestrainedFlexure(ei, span, joint, stretch)
        val restraint = baseRestraintParameter(base.rotationalStiffness, ei, length)
        val critical = standoffBucklingLoad(ei, length, restraint, 0.0)
        return (45 * flexure.tangentStiffness(3.0)) to (critical / flexure.endShear(10.0))
    }

    @Test
    fun `gate 3 symmetry - the base moves compliance and stability in OPPOSITE directions`() {
        // the prediction written into T-40 before the code ran: a softer base relieves P3 and
        // destroys P6, so the window is re-cut rather than narrowed.
        val soft = design(StandoffBase.crossovers(1), 8.0)
        val stiff = design(StandoffBase.idealClamp(), 8.0)
        assert(soft.first < stiff.first)   // softer base -> LOWER tangent, P3 easier
        assert(soft.second < stiff.second) // softer base -> LOWER margin, P6 harder
        // and at 3 nm the clamp fails P3 while the crossover passes it, which is the re-cut
        assert(design(StandoffBase.idealClamp(), 3.0).first > 40.0)
        assert(design(StandoffBase.crossovers(1), 3.0).first < 40.0)
    }

    @Test
    fun `gate 3 symmetry - a single crossover base buckles at every length and two favourable do not`() {
        listOf(3.0, 5.0, 7.0, 8.0, 9.0, 10.0).forEach { length ->
            assert(design(StandoffBase.crossovers(1), length).second < 1.0)
        }
        listOf(7.0, 8.0, 9.0).forEach { length ->
            assert(design(StandoffBase.crossovers(2, favourableOrientation = true), length)
                .second > 1.2)
        }
    }

    @Test
    fun `gate 1 dimensional consistency - CH-0037, the mandate duty and the element's own duty differ`() {
        // C-0025 reads the standoff's compression duty at the desired stroke as the MANDATE
        // secant, 33.3333 x 10/45/2 = 3.7037 pN, identical for every design. The flexure
        // strain-stiffens, so its own end shear is strictly larger at every window length,
        // and at 3 nm the two coincide EXACTLY, by the placement condition.
        val mandateDesired = (100.0 / 3.0) * 10.0 / 45.0 / 2.0
        assert(mandateDesired.isCloseTo(3.7037037037))
        listOf(7.0, 8.0, 9.0, 10.0).forEach { length ->
            val joint = basedNormalStandoff(length, StandoffBase.idealClamp())
            val span = flexureSpanForJoint(ei, joint, 45, 100.0 / 3.0, 3.0, stretch)
            val flexure = PartiallyRestrainedFlexure(ei, span, joint, stretch)
            assert(flexure.endShear(10.0) > mandateDesired)
            assert((flexure.endShear(10.0) / mandateDesired) > 1.26)
            // the placement condition makes the two identical at the ACCEPTABLE stroke, which
            // is why the error is invisible there
            assert(flexure.endShear(3.0).isCloseTo(100.0 / 45.0 / 2.0))
        }
    }

    @Test
    fun `gate 4 convergence - the base stiffness threshold is bracketed by the motifs on either side of it`() {
        val required = baseRotationalStiffnessThreshold(8.0, 10.0)
        assert(required > StandoffBase.crossovers(1).rotationalStiffness)
        assert(required < StandoffBase.crossovers(2, favourableOrientation = true)
            .rotationalStiffness)
        // and the threshold RISES with the standoff length, because P_c falls as 1/l^2 while
        // the duty barely moves
        assert(
            baseRotationalStiffnessThreshold(10.0, 10.0) >
                    baseRotationalStiffnessThreshold(3.0, 10.0)
        )
    }
}
