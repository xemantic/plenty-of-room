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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-72` (covering `T-66`) — the triangulated standoff, priced as a **stability** remedy.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that a frame couple `k_a Σd_i²` is a **rank-one tensor on the leg
 * offsets**, so the restraint a truss supplies and the draw-in release it costs live on
 * **orthogonal** axes and their sum is a conserved budget the azimuth spends — which is why a
 * *partially* triangulated head exists at all.
 */
class TriangulatedStandoffTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val stretch = Gen1Tile.DUPLEX_STRETCH_MODULUS

    private val d = Gen1Tile.INTERHELICAL_SHEET

    /** `C-0029`'s realisable two-link base — hard 180° reading, the bound no convention can move. */
    private val hardBase = TwoLinkBase.realisable()

    /** `C-0028`'s `B2`, carried only so that `C-0030`'s filed design can be reproduced. */
    private val c0028Base = TwoLinkBase.c0028TwoCrossovers()

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - a frame couple is a stiffness times a squared offset`() {
        // k_a Sigma d^2 carries pN/nm * nm^2 = pN nm/rad, so doubling every offset quadruples it
        val near = TrussLayout.row(2, d, PI / 2.0, "near")
        val far = TrussLayout.row(2, 2.0 * d, PI / 2.0, "far")
        assert((far.acrossSecondMoment / near.acrossSecondMoment).isCloseTo(4.0))
        val axial = 44.0
        assert(
            (trussFrameCouple(far.acrossSecondMoment, axial) /
                    trussFrameCouple(near.acrossSecondMoment, axial)).isCloseTo(4.0)
        )
        // and a zero offset gives exactly zero couple, at any leg axial stiffness
        assert(trussFrameCouple(0.0, axial) == 0.0)
    }

    @Test
    fun `gate 1 dimensional consistency - a leg's axial stiffness is a modulus over a length in series with its base`() {
        // S/l alone halves when the length doubles
        val base = TwoLinkBase.realisable().copy(axial = Double.POSITIVE_INFINITY)
        assert(legAxialStiffness(4.0, base, stretch).isCloseTo(stretch / 4.0))
        assert(legAxialStiffness(8.0, base, stretch).isCloseTo(stretch / 8.0))
        // and a real base is strictly softer than either member — C-0028's series discipline
        val real = legAxialStiffness(8.0, hardBase, stretch)
        assert(real < stretch / 8.0)
        assert(real < hardBase.axial)
        assert(real.isCloseTo(1.0 / (8.0 / stretch + 1.0 / hardBase.axial)))
    }

    @Test
    fun `gate 1 dimensional consistency - a truss critical load is a rigidity over a squared length and scales with the leg count`() {
        // at FIXED dimensionless restraints — rho_b = k l/EI held by scaling k with 1/l —
        // halving the length quadruples the critical load
        val a = trussBucklingLoad(ei, 8.0, 100.0 / 8.0, 1, 0.0)
        val b = trussBucklingLoad(ei, 4.0, 100.0 / 4.0, 1, 0.0)
        assert((b / a).isCloseTo(4.0))
        // n identical legs carry exactly n times the load
        val one = trussBucklingLoad(ei, 8.0, 78.24, 1, 0.0)
        val three = trussBucklingLoad(ei, 8.0, 78.24, 3, 0.0)
        assert((three / one).isCloseTo(3.0))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { TrussLayout("none", emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            TrussLayout("off centre", listOf(LegOffset(0.0, 0.0), LegOffset(1.0, 3.0)))
        }
        assertFailsWith<IllegalArgumentException> { TrussLayout.row(2, -1.0, 0.0, "negative") }
        assertFailsWith<IllegalArgumentException> { trussFrameCouple(-1.0, 44.0) }
        assertFailsWith<IllegalArgumentException> { trussFrameCouple(1.0, -44.0) }
        assertFailsWith<IllegalArgumentException> {
            trussTipFlexibility(ei, 8.0, 78.24, 0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            trussTipFlexibility(ei, -8.0, 78.24, 1, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            TriangulatedStandoff(TrussLayout.single(), -1.0, hardBase)
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting case - one leg with no offset IS C-0030's single standoff, entry by entry`() {
        for (length in listOf(5.0, 7.0, 8.0, 10.0)) {
            for (base in listOf(hardBase, c0028Base)) {
                val truss = trussTipFlexibility(ei, length, base.restrainedAxis, 1, 0.0)
                val single = standoffTipFlexibility(ei, length, base.restrainedAxis)
                assert(truss.translationUnderForce.isCloseTo(single.translationUnderForce, 1e-12))
                assert(truss.translationUnderMoment.isCloseTo(single.translationUnderMoment, 1e-12))
                assert(truss.rotationUnderForce.isCloseTo(single.rotationUnderForce, 1e-12))
                assert(truss.rotationUnderMoment.isCloseTo(single.rotationUnderMoment, 1e-12))
            }
        }
    }

    @Test
    fun `gate 2 limiting case - legs collapsed onto one axis are n springs in parallel, exactly`() {
        // the free and strong check the task file declares: with every offset zero the assembly
        // is n identical legs in parallel, so every flexibility entry is the single leg's over n
        val single = standoffTipFlexibility(ei, 8.0, hardBase.restrainedAxis)
        for (n in 1..4) {
            val truss = trussTipFlexibility(ei, 8.0, hardBase.restrainedAxis, n, 0.0)
            assert(truss.translationUnderForce.isCloseTo(single.translationUnderForce / n, 1e-12))
            assert(truss.translationUnderMoment.isCloseTo(single.translationUnderMoment / n, 1e-12))
            assert(truss.rotationUnderMoment.isCloseTo(single.rotationUnderMoment / n, 1e-12))
            // and the correlation — a shape, not a magnitude — is untouched by the leg count
            assert(truss.correlation.isCloseTo(single.correlation, 1e-12))
        }
    }

    @Test
    fun `gate 2 limiting case - a cross row adds NOTHING to the loaded plane, exactly`() {
        // this is the whole of the task's pre-registered prediction: legs laid across the flexure
        // axis have Sigma x^2 = 0 identically, so the loaded plane inherits no frame stiffness
        val across = TrussLayout.row(2, d, PI / 2.0, "cross row")
        assert(across.alongSecondMoment == 0.0)
        assert(across.acrossSecondMoment.isCloseTo(d * d / 2.0, 1e-12))
        val truss = TriangulatedStandoff(across, 8.0, hardBase)
        val parallel = trussTipFlexibility(ei, 8.0, hardBase.restrainedAxis, 2, 0.0)
        assert(truss.flexibility.translationUnderForce.isCloseTo(parallel.translationUnderForce, 1e-12))
        assert(truss.flexibility.translationUnderMoment.isCloseTo(parallel.translationUnderMoment, 1e-12))
        assert(truss.loadedFrameCouple == 0.0)
        assert(truss.freeFrameCouple > 0.0)
    }

    @Test
    fun `gate 2 limiting case - a rigid fully triangulated head supplies NO draw-in`() {
        // as the loaded-plane frame couple grows without bound the off-diagonal goes to zero and
        // the sway stiffness goes to the ROTATION-FIXED reading, exactly 4x stiffer at a clamp
        val single = standoffTipFlexibility(ei, 8.0, Double.POSITIVE_INFINITY)
        val huge = trussTipFlexibility(ei, 8.0, Double.POSITIVE_INFINITY, 1, 1.0e12)
        assert(abs(huge.translationUnderMoment) < 1e-10 * single.translationUnderMoment)
        assert(huge.translationUnderForce.isCloseTo(1.0 / single.swayStiffnessRotationFixed, 1e-6))
        assert((single.translationUnderForce / huge.translationUnderForce).isCloseTo(4.0, 1e-6))
    }

    @Test
    fun `gate 2 limiting case - a pinned head tie reduces the truss to independent legs`() {
        // k_tie = 0 is a hinge at the cap: the frame couple vanishes whatever the offsets are
        val along = TrussLayout.row(2, d, 0.0, "along row")
        val tied = TriangulatedStandoff(along, 8.0, hardBase, headTieStiffness = 0.0)
        val free = trussTipFlexibility(ei, 8.0, hardBase.restrainedAxis, 2, 0.0)
        assert(tied.loadedFrameCouple == 0.0)
        assert(tied.freeFrameCouple == 0.0)
        assert(tied.flexibility.translationUnderForce.isCloseTo(free.translationUnderForce, 1e-12))
    }

    @Test
    fun `gate 2 limiting case - a single leg truss reproduces C-0029's two readings of the same joint`() {
        // the strong axis is the two-link couple, the free axis is 2 k_bond_theta = C-0028's B1
        assert(hardBase.restrainedAxis.isCloseTo(maximumBaseRotationalStiffness(1.0), 1e-12))
        assert(hardBase.freeAxis.isCloseTo(2.0 * bondHingeStiffness(), 1e-12))
        assert(hardBase.freeAxis.isCloseTo(StandoffBase.crossovers(1).rotationalStiffness, 1e-12))
        // and the single standoff buckles about the FREE axis, at every length
        for (length in listOf(5.0, 6.0, 7.0, 8.0, 9.0, 10.0)) {
            val truss = TriangulatedStandoff(TrussLayout.single(), length, hardBase)
            assert(truss.freeCriticalLoad < truss.loadedCriticalLoad)
            assert(truss.criticalLoad.isCloseTo(truss.freeCriticalLoad, 1e-12))
            assert(truss.governingPlane == "free")
        }
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 conservation - the truss's frame couple is a conserved budget the azimuth spends`() {
        // Sigma x^2 + Sigma y^2 = Sigma d^2 at EVERY azimuth: a rank-one tensor on the offsets
        val separation = 2.72
        val budget = separation * separation / 2.0
        // the interior of the sweep, where neither component is snapped to the lattice's zero
        for (i in 0..5) {
            val azimuth = i * PI / 12.0
            val layout = TrussLayout.row(2, separation, azimuth, "sweep")
            assert(
                (layout.alongSecondMoment + layout.acrossSecondMoment).isCloseTo(budget, 1e-12)
            )
            assert(
                layout.alongSecondMoment
                    .isCloseTo(budget * cos(azimuth) * cos(azimuth), 1e-12)
            )
            assert(
                layout.acrossSecondMoment
                    .isCloseTo(budget * sin(azimuth) * sin(azimuth), 1e-12)
            )
        }
        // and the two ends of it, where the budget lands entirely on one plane
        val along = TrussLayout.row(2, separation, 0.0, "along")
        assert(along.alongSecondMoment.isCloseTo(budget, 1e-12))
        assert(along.acrossSecondMoment == 0.0)
        val across = TrussLayout.row(2, separation, PI / 2.0, "across")
        assert(across.acrossSecondMoment.isCloseTo(budget, 1e-12))
        assert(across.alongSecondMoment == 0.0)
    }

    @Test
    fun `gate 3 symmetry - Maxwell-Betti holds on the ASSEMBLED truss, between two different quadratures`() {
        // each leg's flexibility is built by a DOUBLE cumulative-Simpson integration for C12 and a
        // SINGLE one for C21; the legs are then inverted, summed with the frame couple and
        // inverted back. Nothing in that route makes the assembled off-diagonals equal
        for (length in listOf(5.0, 8.0, 10.0)) {
            for (couple in listOf(0.0, 40.0, 400.0)) {
                for (n in listOf(1, 2, 3)) {
                    val quadrature =
                        trussTipFlexibilityByIntegration(ei, length, hardBase.restrainedAxis, n, couple)
                    val departure = abs(
                        quadrature.translationUnderMoment - quadrature.rotationUnderForce
                    ) / quadrature.translationUnderMoment
                    assert(departure < 1e-12)
                }
            }
        }
    }

    @Test
    fun `gate 3 symmetry - the assembled flexibility is positive definite and its correlation stays below one`() {
        for (couple in listOf(0.0, 10.0, 100.0, 1000.0)) {
            val truss = trussTipFlexibility(ei, 8.0, hardBase.restrainedAxis, 2, couple)
            assert(truss.determinant > 0.0)
            assert(truss.correlation > 0.0 && truss.correlation < 1.0)
        }
    }

    @Test
    fun `gate 3 symmetry - swapping the layout's two axes swaps the two planes exactly`() {
        val across = TrussLayout.row(2, d, PI / 2.0, "across")
        val along = TrussLayout.row(2, d, 0.0, "along")
        assert(across.alongSecondMoment.isCloseTo(along.acrossSecondMoment, 1e-12))
        assert(across.acrossSecondMoment.isCloseTo(along.alongSecondMoment, 1e-12))
        // a layout is invariant under leg permutation and under reflection through the centroid
        val ordered = TrussLayout("ordered", listOf(LegOffset(0.0, -1.5), LegOffset(0.0, 1.5)))
        val reversed = TrussLayout("reversed", listOf(LegOffset(0.0, 1.5), LegOffset(0.0, -1.5)))
        assert(ordered.acrossSecondMoment.isCloseTo(reversed.acrossSecondMoment, 1e-15))
    }

    @Test
    fun `gate 3 conservation - the head moment is shared between the frame and the legs' own bending`() {
        // the axial share of a head moment is exactly k_frame * C22_assembled, so it is zero when
        // there is no frame and tends to the whole moment as the frame stiffens
        val along = TrussLayout.row(2, d, 0.0, "along")
        val soft = TriangulatedStandoff(along, 8.0, hardBase, headTieStiffness = 0.0)
        assert(soft.peakLegCompression(4.0, 0.1).isCloseTo(2.0, 1e-12))
        val stiff = TriangulatedStandoff(along, 8.0, hardBase)
        assert(stiff.peakLegCompression(4.0, 0.1) > 2.0)
        // and with the legs collinear across, a head moment loads no leg axially at all
        val across = TriangulatedStandoff(TrussLayout.row(2, d, PI / 2.0, "across"), 8.0, hardBase)
        assert(across.peakLegCompression(4.0, 0.1).isCloseTo(2.0, 1e-12))
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the assembled flexibility from quadrature is mesh-independent to round-off`() {
        // Simpson is EXACT for this integrand — the curvature is linear, so the rotation is
        // quadratic and the translation cubic — so the departure sits at the round-off floor at
        // every mesh rather than falling with one. Asserting a convergence RATE here would be
        // asserting a property of the round-off, which is C-0030's own reading of the same object
        val closed = trussTipFlexibility(ei, 8.0, hardBase.restrainedAxis, 2, 120.0)
        for (steps in listOf(64, 256, 1024)) {
            val quadrature =
                trussTipFlexibilityByIntegration(ei, 8.0, hardBase.restrainedAxis, 2, 120.0, steps)
            val departure =
                abs(quadrature.translationUnderForce - closed.translationUnderForce) /
                        closed.translationUnderForce
            assert(departure < 1e-12)
            val offDiagonal =
                abs(quadrature.translationUnderMoment - closed.translationUnderMoment) /
                        closed.translationUnderMoment
            assert(offDiagonal < 1e-12)
        }
    }

    @Test
    fun `gate 4 convergence - the placed span is scan-step independent`() {
        val truss = TriangulatedStandoff(TrussLayout.row(2, d, PI / 2.0, "cross row"), 8.0, hardBase)
        val finest = coupledFlexureSpan(
            ei, truss.flexibility, 45, 100.0 / 3.0, 3.0, FlexureOrientation.FAVOURABLE, stretch,
            DrawInModel.CHORD, scanSteps = 2048
        )
        for (steps in listOf(64, 256, 1024)) {
            val span = coupledFlexureSpan(
                ei, truss.flexibility, 45, 100.0 / 3.0, 3.0, FlexureOrientation.FAVOURABLE, stretch,
                DrawInModel.CHORD, scanSteps = steps
            )
            assert(abs(span - finest) / finest < 1e-12)
        }
    }

    @Test
    fun `gate 4 convergence - the truss buckling load is scan-step independent`() {
        val finest = trussBucklingLoad(ei, 8.0, 78.24, 2, 160.0, scanSteps = 4096)
        for (steps in listOf(64, 256, 1024)) {
            val value = trussBucklingLoad(ei, 8.0, 78.24, 2, 160.0, scanSteps = steps)
            assert(abs(value - finest) / finest < 1e-12)
        }
    }

    // ---------------------------------------------------------------- gate 5 — upstream

    @Test
    fun `gate 5 upstream - C-0029's ceiling, free axis and weak-axis critical loads are reproduced`() {
        assert(hardBase.restrainedAxis.isCloseTo(78.24, 1e-3))
        assert(hardBase.freeAxis.isCloseTo(13.53, 1e-3))
        // C-0029's T1 table: the weak-axis P_c is 2.46 pN at 5 nm, 1.69 at 7 and 1.46 at 8
        val at5 = TriangulatedStandoff(TrussLayout.single(), 5.0, hardBase)
        val at7 = TriangulatedStandoff(TrussLayout.single(), 7.0, hardBase)
        val at8 = TriangulatedStandoff(TrussLayout.single(), 8.0, hardBase)
        assert(at5.criticalLoad.isCloseTo(2.46, 5e-3))
        assert(at7.criticalLoad.isCloseTo(1.69, 5e-3))
        assert(at8.criticalLoad.isCloseTo(1.46, 5e-3))
    }

    @Test
    fun `gate 5 upstream - the single-leg truss reproduces C-0030's filed B2 design at 8 nm`() {
        val truss = TriangulatedStandoff(TrussLayout.single(), 8.0, c0028Base)
        val span = coupledFlexureSpan(
            ei, truss.flexibility, 45, 100.0 / 3.0, 3.0, FlexureOrientation.FAVOURABLE, stretch
        )
        assert(span.isCloseTo(31.82, 1e-3))
        val flexure = CoupledJointFlexure(ei, span, truss.flexibility, stretch)
        assert(
            (45.0 * flexure.strokeTangentStiffness(3.0, FlexureOrientation.FAVOURABLE))
                .isCloseTo(25.23, 1e-3)
        )
        assert(
            flexure.strokeEndShear(10.0, FlexureOrientation.FAVOURABLE).isCloseTo(3.313, 1e-3)
        )
        // and C-0030's own free-head critical load, 7.21 pN on the loaded (restrained) axis
        assert(truss.loadedCriticalLoad.isCloseTo(7.21, 1e-3))
    }

    @Test
    fun `gate 5 upstream - the cheap bound the task file declared, asserted as a test`() {
        // a two-leg cross row at the SAXS interhelical distance beats the free axis's own bond
        // couple by more than an order of magnitude, which is why the task was worth running
        val axial = legAxialStiffness(8.0, hardBase, stretch)
        val layout = TrussLayout.row(2, d, PI / 2.0, "cross row")
        val couple = trussFrameCouple(layout.acrossSecondMoment, axial)
        assert(couple > 10.0 * hardBase.freeAxis)
        assert(axial.isCloseTo(44.0, 5e-3))
        assert(couple.isCloseTo(159.2, 5e-3))
    }

    @Test
    fun `gate 5 literature - Pumm et al's set of spacers is TWO, and its geometry is out of this envelope`() {
        // "a set of two spacer oligonucleotide strands was added ... to mount the obstacles on
        // the triangular platform" — read directly and re-verified verbatim from the Methods and
        // the SI strand table. The recommended layout has exactly that leg count
        val pummSpacerCount = 2
        assert(TrussLayout.row(2, d, PI / 2.0, "recommended").legCount == pummSpacerCount)
        // and each spacer is 39 bp, which is 13.26 nm — PAST C-0017's 10 nm envelope, so the
        // precedent is for the MECHANISM and not for the geometry
        val pummSpacerLength = 39 * Gen1Tile.RISE_PER_BASE_PAIR
        assert(pummSpacerLength.isCloseTo(13.26, 1e-3))
        assert(pummSpacerLength > 10.0)
    }

    @Test
    fun `gate 5 upstream - the SAXS interhelical distance and Fields' rigidity are what they are cited as`() {
        assert(d.isCloseTo(2.69, 1e-12))
        assert(FIELDS_BENDING_RIGIDITY.isCloseTo(172.906, 1e-4))
    }

    // ---------------------------------------------------------------- the verdict itself

    @Test
    fun `the answer - a two-leg CROSS row restores the free axis and the loaded plane then governs`() {
        for (length in listOf(6.0, 7.0, 8.0)) {
            val single = TriangulatedStandoff(TrussLayout.single(), length, hardBase)
            val truss = TriangulatedStandoff(
                TrussLayout.row(2, d, PI / 2.0, "cross row"), length, hardBase
            )
            // the free axis stops governing, and the critical load rises by more than 4x
            assert(truss.governingPlane == "loaded")
            assert(truss.criticalLoad > 4.0 * single.criticalLoad)
            // and the loaded plane is exactly twice the single standoff's — no frame, no penalty
            assert(truss.loadedCriticalLoad.isCloseTo(2.0 * single.loadedCriticalLoad, 1e-12))
        }
    }

    @Test
    fun `the answer - an ALONG row buys nothing on the free axis and kills the draw-in supply`() {
        val single = TriangulatedStandoff(TrussLayout.single(), 8.0, hardBase)
        val along = TriangulatedStandoff(TrussLayout.row(2, d, 0.0, "along row"), 8.0, hardBase)
        val across = TriangulatedStandoff(TrussLayout.row(2, d, PI / 2.0, "across row"), 8.0, hardBase)
        // the along row leaves the free axis with the bond couple alone, so it still governs
        assert(along.governingPlane == "free")
        assert(along.freeCriticalLoad.isCloseTo(2.0 * single.freeCriticalLoad, 1e-12))
        // and it destroys the off-diagonal the draw-in supply is made of
        assert(along.flexibility.translationUnderMoment < 0.5 * across.flexibility.translationUnderMoment)
    }

    @Test
    fun `the answer - the draw-in that survives a cross row still exceeds the demand at the placement stroke`() {
        val truss = TriangulatedStandoff(TrussLayout.row(2, d, PI / 2.0, "cross row"), 8.0, hardBase)
        val span = coupledFlexureSpan(
            ei, truss.flexibility, 45, 100.0 / 3.0, 3.0, FlexureOrientation.FAVOURABLE, stretch
        )
        val flexure = CoupledJointFlexure(ei, span, truss.flexibility, stretch)
        val supply = flexure.couplingFactor * 3.0
        val demand = flexure.chordExtension(3.0)
        assert(supply > demand)
        // the beam is therefore still in COMPRESSION at the placement point, as in C-0030
        assert(flexure.strokeAxialForce(3.0, FlexureOrientation.FAVOURABLE) < 0.0)
    }
}
