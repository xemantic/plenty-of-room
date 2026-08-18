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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-206` — what does an **oblique** attachment root cost against a perpendicular one?
 *
 * `C-0122` censused the honeycomb's attachment lattice and could not price it: half a top face's
 * helices carry a free azimuth pointing straight out of the slab and half carry two oblique ones,
 * and every station in that census is treated as equivalent.
 *
 * These tests pin the decomposition **before** any constant is read, because the cheap bound is
 * the whole method: `κ(ψ) = cos²ψ + sin²ψ·A` with `A = k_radial/k_tangential`, so the entire
 * question is one anisotropy and the answer is monotone in it.
 */
class ObliqueRootTest {

    // --- gate 2 (limiting cases) + gate 1 (dimensions): the decomposition ----------------------

    @Test
    fun `the oblique azimuth is half the lattice separation, and it is 60 degrees`() {
        // NOT asserted as a constant: sublattice B's azimuths are sublattice A's rotated by half
        // the separation, so the free directions of a top-face B helix are at +- 60 degrees.
        assert(obliqueAzimuthDegrees().isCloseTo(60.0))
        assert(obliqueAzimuthDegrees()
            .isCloseTo(HoneycombLattice.azimuthSeparationDegrees() / 2.0))
    }

    @Test
    fun `a perpendicular root reproduces the radial axis exactly`() {
        val root = ObliqueRootModel("test", radial = 64.0, tangential = 8.0)
        assert(root.normalStiffness(0.0).isCloseTo(64.0))
        assert(root.costFactor(0.0).isCloseTo(1.0))
    }

    @Test
    fun `a 90 degree root is the pure in-plane case, the tangential axis alone`() {
        val root = ObliqueRootModel("test", radial = 64.0, tangential = 8.0)
        assert(root.normalStiffness(PI / 2.0).isCloseTo(8.0))
        assert(root.costFactor(PI / 2.0).isCloseTo(8.0))
    }

    @Test
    fun `the cost factor is cos squared plus A sin squared, and MONOTONE in A`() {
        val psi = PI / 3.0
        val soft = ObliqueRootModel("soft", radial = 10.0, tangential = 10.0)
        val hard = ObliqueRootModel("hard", radial = 100.0, tangential = 10.0)
        assert(soft.costFactor(psi).isCloseTo(1.0))
        assert(hard.costFactor(psi).isCloseTo(0.25 + 0.75 * 10.0))
        assert(soft.costFactor(psi) < hard.costFactor(psi))
    }

    // --- gate 3 (symmetry) --------------------------------------------------------------------

    @Test
    fun `the two oblique azimuths of one helix are mirror images and cost the same`() {
        val root = ObliqueRootModel("test", radial = 64.0, tangential = 8.0)
        assert(root.normalStiffness(PI / 3.0).isCloseTo(root.normalStiffness(-PI / 3.0)))
    }

    @Test
    fun `an ISOTROPIC root costs exactly nothing at every azimuth`() {
        // FlexureEndJoint's own invariant: `k_perp/k_axial` is exactly 1 for any isotropic
        // element, and for any covalent tie on a softened bond. That is a SYMMETRY, not a small
        // number, so it must hold at every azimuth to the last ulp of the arithmetic.
        val root = ObliqueRootModel("isotropic", radial = 42.0, tangential = 42.0)
        listOf(0.0, 0.3, PI / 6.0, PI / 3.0, PI / 2.0).forEach {
            assert(root.costFactor(it).isCloseTo(1.0))
            assert(root.normalStiffness(it).isCloseTo(42.0))
        }
    }

    @Test
    fun `the stiffness tensor is diagonal in the root's own two axes`() {
        // The falsifier of the whole approach: if the two axes are not eigenvectors, `cos + sin`
        // is the wrong decomposition. Built from the tensor rather than assumed.
        val root = ObliqueRootModel("test", radial = 64.0, tangential = 8.0)
        val psi = 0.4
        assert(root.normalStiffnessFromTensor(psi).isCloseTo(root.normalStiffness(psi)))
    }

    // --- the paired root, where the scalar reading is WRONG ------------------------------------

    @Test
    fun `two oblique roots on one rigid head add as STIFFNESS TENSORS, not as normal stiffnesses`() {
        val root = ObliqueRootModel("test", radial = 64.0, tangential = 8.0)
        val psi = PI / 3.0
        val paired = root.pairedNormalStiffness(psi)
        // The symmetric pair's normal-normal entry is `2(cos^2 k_r + sin^2 k_t)` — a sum of
        // STIFFNESSES — and the naive `2 * k_z(psi)` is a sum of the free single roots' normal
        // stiffnesses, which discards the lateral coupling the shared head removes.
        assert(paired.isCloseTo(2.0 * (0.25 * 64.0 + 0.75 * 8.0)))
        assert(paired > 2.0 * root.normalStiffness(psi))
    }

    @Test
    fun `a paired root is still softer than a perpendicular one when the radial axis is stiffer`() {
        val root = ObliqueRootModel("test", radial = 64.0, tangential = 8.0)
        assert(root.pairedNormalStiffness(PI / 3.0) < root.normalStiffness(0.0))
    }

    // --- the three root models the corpus supplies ---------------------------------------------

    @Test
    fun `the flexible tie root is isotropic by the corpus's own invariant`() {
        assert(flexibleTieRoot().anisotropy.isCloseTo(1.0))
        assert(flexibleTieRoot().costFactor(obliqueAzimuthRadians()).isCloseTo(1.0))
    }

    @Test
    fun `the crossover-hinged root is the link against the dihedral spring on the d over 2 lever`() {
        val root = crossoverHingedRoot()
        // radial: two softened backbone bonds, `2 alpha S/(100 a)`.
        assert(root.radial.isCloseTo(2.0 * 1100.0 / (100.0 * 0.34)))
        // tangential: `C-0009`'s crossover dihedral spring on the frame-indifferent lever `d/2`.
        val lever = 2.536 / 2.0
        assert(root.tangential
            .isCloseTo((2.0 * 230.0 / (100.0 * 0.34)) / (lever * lever)))
        assert(root.anisotropy > 1.0)
    }

    @Test
    fun `the constraint reading refuses a RATIO and still delivers an absolute stiffness`() {
        val root = constrainedLinkRoot()
        assert(root.radial.isInfinite())
        assert(root.costFactor(obliqueAzimuthRadians()).isInfinite())
        assert(root.normalStiffness(0.0).isInfinite())
        // But the oblique reading is finite, because the tangential axis carries three quarters
        // of the load path whatever the link is.
        assert(root.normalStiffness(obliqueAzimuthRadians()).isFinite())
        assert(root.normalStiffness(obliqueAzimuthRadians())
            .isCloseTo(root.tangential / 0.75))
    }

    // --- the path consequence -------------------------------------------------------------------

    @Test
    fun `an oblique path with an UNCHANGED series partner delivers a fraction below one`() {
        val root = crossoverHingedRoot()
        val demand = MANDATED_TOTAL_STIFFNESS / 10.0
        val fraction = obliquePathFraction(root, obliqueAzimuthRadians(), demand)
        assert(fraction < 1.0)
        assert(fraction > 0.0)
    }

    @Test
    fun `the path cost FALLS as the coupling gets denser, because the demand per path falls`() {
        val root = crossoverHingedRoot()
        val sparse = obliquePathFraction(root, obliqueAzimuthRadians(), MANDATED_TOTAL_STIFFNESS / 10.0)
        val dense = obliquePathFraction(root, obliqueAzimuthRadians(), MANDATED_TOTAL_STIFFNESS / 75.0)
        assert(sparse < dense)
    }

    @Test
    fun `an isotropic root costs a path exactly nothing`() {
        val fraction = obliquePathFraction(
            flexibleTieRoot(), obliqueAzimuthRadians(), MANDATED_TOTAL_STIFFNESS / 10.0
        )
        assert(fraction.isCloseTo(1.0))
    }

    @Test
    fun `a series partner cannot be sized when the root is softer than the demand`() {
        val weak = ObliqueRootModel("weak", radial = 1.0, tangential = 1.0)
        assertFailsWith<IllegalArgumentException> {
            obliquePathFraction(weak, obliqueAzimuthRadians(), 2.0)
        }
    }

    // --- the alternation the lattice imposes -----------------------------------------------------

    @Test
    fun `the top face alternates perpendicular and oblique, and a renormalised share keeps the sum`() {
        val share = alternatingShareOfMandate(rows = 10, columns = 1, obliqueFraction = 0.8)
        assert(share.size == 10)
        assert(share.sum().isCloseTo(MANDATED_TOTAL_STIFFNESS))
        // Alternating along the row, starting perpendicular, and the oblique ones are softer.
        assert(share[0] > share[1])
        assert(share[0].isCloseTo(share[2]))
        assert((share[1] / share[0]).isCloseTo(0.8))
    }

    @Test
    fun `an oblique fraction of one reproduces the equal share exactly`() {
        val share = alternatingShareOfMandate(rows = 10, columns = 3, obliqueFraction = 1.0)
        val equal = equalShareOfMandate(30)
        assert(share.size == equal.size)
        share.indices.forEach { assert(share[it].isCloseTo(equal[it])) }
    }

    // --- the census the two free azimuths correct ------------------------------------------------

    @Test
    fun `a perpendicular top-face helix has ONE free azimuth and an oblique one has TWO`() {
        // The sublattice that points an azimuth straight out spends its other two on the
        // neighbours below; the sublattice that points one straight DOWN has two pointing out.
        assert(freeAzimuthsOnTopFace(column = 0) == 1)
        assert(freeAzimuthsOnTopFace(column = 1) == 2)
        assert(freeAzimuthsOnTopFace(column = 2) == 1)
    }

    @Test
    fun `counting both free azimuths raises C-0122's census by exactly half on an even row`() {
        // `10 x 6`: five perpendicular helices at one azimuth and five oblique at two.
        val corrected = topFaceStationsCountingBothAzimuths(rasterRows = 10, rowBasePairs = 112)
        val censused = honeycombStationCensus(10, 6, 112).stations
        assert(censused == 60)
        assert(corrected == 90)
        assert((corrected.toDouble() / censused).isCloseTo(1.5))
    }

    @Test
    fun `and by less than half on an odd row, because the perpendicular sublattice gets the extra`() {
        val corrected = topFaceStationsCountingBothAzimuths(rasterRows = 15, rowBasePairs = 112)
        val censused = honeycombStationCensus(15, 4, 112).stations
        assert(censused == 90)
        assert(corrected == 132)
    }

    @Test
    fun `the two azimuths of one helix INTERLEAVE along it and do not collide`() {
        // Each azimuth carries its own 21 bp ladder, and consecutive positions over all azimuths
        // are 7 bp apart — so a helix's two free ladders are offset by 7 bp and never coincide.
        assert(HoneycombLattice.SAME_PAIR_PERIOD_BP %
                HoneycombLattice.ANY_AZIMUTH_STEP_BP == 0)
        assert(HoneycombLattice.SAME_PAIR_PERIOD_BP /
                HoneycombLattice.ANY_AZIMUTH_STEP_BP == HoneycombLattice.AZIMUTHS)
    }

    @Test
    fun `the guard refuses a non-positive stiffness`() {
        assertFailsWith<IllegalArgumentException> {
            ObliqueRootModel("bad", radial = 0.0, tangential = 1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            ObliqueRootModel("bad", radial = 1.0, tangential = 0.0)
        }
    }
}
