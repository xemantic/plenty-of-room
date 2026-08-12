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
import com.xemantic.nano.plentyofroom.material.PegWater
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test

/**
 * §4(c) — how much of the buffer's mobile-ion population gets into the PEG layer,
 * and what that does to the screening length **inside** the layer, where the field is needed.
 *
 * This is the cheap bound: hard-sphere exclusion (Ogston) times a Born dielectric penalty
 * against a Maxwell-Garnett mixture, closed by Donnan for a neutral layer.
 * What it cannot bound is stated in the claim, not hidden here.
 */
class PolymerLayerPartitioningTest {

    private val peg = PegWater()

    /** The `C-0002` design point: `L₀ = 10 nm`, `σ = 0.024 nm⁻²`, `φ = 0.0289`. */
    private val designPoint = LayerPartitioning(
        polymerVolumeFraction = 0.0288872,
        fibreRadius = peg.kuhnSegmentDiameter / 2.0
    )

    // gate 1 — dimensional consistency

    @Test
    fun `gate 1 should make the Born transfer energy dimensionless in k_BT`() {
        // dG/k_BT = z^2 l_B(eps_w) eps_w / (2R) * (1/eps_in - 1/eps_out) — a length over a
        // length, times a pure number. Checked by the two invariances it must obey:
        // quadratic in valency, inverse in radius.
        val a = bornTransferEnergy(valency = 1, ionRadius = 0.4, permittivityInside = 70.0)
        val b = bornTransferEnergy(valency = 2, ionRadius = 0.4, permittivityInside = 70.0)
        val c = bornTransferEnergy(valency = 1, ionRadius = 0.8, permittivityInside = 70.0)
        assert((b / a).isCloseTo(4.0, relativeTolerance = 1e-12))
        assert((c / a).isCloseTo(0.5, relativeTolerance = 1e-12))
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should partition freely into an empty layer`() {
        // phi -> 0 must give a partition coefficient of exactly one and no change in
        // the screening length: no polymer, no exclusion, no dielectric decrement.
        val empty = LayerPartitioning(polymerVolumeFraction = 1e-12, fibreRadius = 0.233)
        assert(empty.magnesiumPartitionCoefficient.isCloseTo(1.0, relativeTolerance = 1e-9))
        assert(empty.chloridePartitionCoefficient.isCloseTo(1.0, relativeTolerance = 1e-9))
        assert(empty.saltPartitionCoefficient.isCloseTo(1.0, relativeTolerance = 1e-9))
        assert(empty.debyeLengthRatio.isCloseTo(1.0, relativeTolerance = 1e-9))
    }

    @Test
    fun `gate 2 should recover pure water as the polymer volume fraction vanishes`() {
        assert(maxwellGarnettPermittivity(0.0).isCloseTo(WATER_RELATIVE_PERMITTIVITY))
        // and the polymer's own permittivity in the melt limit
        assert(maxwellGarnettPermittivity(1.0).isCloseTo(PEG_RELATIVE_PERMITTIVITY, 1e-12))
    }

    @Test
    fun `gate 2 should exclude ions more strongly as the layer is compressed`() {
        // monotone in phi, and the compression a working actuator applies is precisely
        // what raises phi — so the layer screens LESS the harder it is squeezed.
        val loose = LayerPartitioning(0.0289, peg.kuhnSegmentDiameter / 2.0)
        val tight = LayerPartitioning(0.0708, peg.kuhnSegmentDiameter / 2.0)
        assert(tight.saltPartitionCoefficient < loose.saltPartitionCoefficient)
        assert(tight.debyeLengthRatio > loose.debyeLengthRatio)
    }

    @Test
    fun `gate 2 should exclude the larger ion more than the smaller one`() {
        // hydrated Mg2+ (4.28 A) against hydrated Cl- (3.32 A): the divalent cation is the
        // one the layer keeps out, which is the opposite of what a naive "the layer screens
        // the field" intuition assumes.
        assert(designPoint.magnesiumPartitionCoefficient < designPoint.chloridePartitionCoefficient)
    }

    // gate 3 — Donnan consistency

    @Test
    fun `gate 3 should combine the ion partition coefficients by the Donnan geometric mean`() {
        // For a NEUTRAL layer the salt partition coefficient is the stoichiometric geometric
        // mean, K_salt = (K_+ K_-^2)^(1/3) for MgCl2 — this is Donnan equilibrium with zero
        // fixed charge, and it is what conserves electroneutrality inside the layer.
        val expected = (
                designPoint.magnesiumPartitionCoefficient *
                        designPoint.chloridePartitionCoefficient.pow(2.0)
                ).pow(1.0 / 3.0)
        assert(designPoint.saltPartitionCoefficient.isCloseTo(expected, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 3 should tie the local screening length to the partitioned ionic strength`() {
        // lambda_in / lambda_bulk = 1/sqrt(K_salt), because the ionic strength scales with
        // the partitioned salt and lambda goes as I^(-1/2).
        assert(
            designPoint.debyeLengthRatio
                .isCloseTo(1.0 / kotlin.math.sqrt(designPoint.saltPartitionCoefficient), 1e-12)
        )
    }

    // gate 5 — the numbers, and their split between the two mechanisms

    @Test
    fun `gate 5 should show steric exclusion dominating the Born penalty at the design point`() {
        // The result that decides whether the expensive calculation is warranted:
        // at phi = 0.029 the layer is 97% water, so the dielectric decrement is only
        // 78 -> 75.0 and the Born penalty on Mg2+ is 0.135 k_BT (K = 0.874).
        // Hard-sphere exclusion contributes K = 0.793. Steric wins, and it is the cheap term.
        assert(designPoint.effectivePermittivity.isCloseTo(74.97471, relativeTolerance = 1e-4))
        assert(designPoint.magnesiumBornEnergy.isCloseTo(0.134666, relativeTolerance = 1e-3))
        assert(designPoint.magnesiumBornPartitionCoefficient.isCloseTo(0.87398, relativeTolerance = 1e-3))
        assert(designPoint.magnesiumStericPartitionCoefficient.isCloseTo(0.79253, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 5 should bound the salt partition coefficient across the whole design window`() {
        // The answer to §4(c), as a band rather than a number: over the C-0002 volume
        // fractions (0.0289 unperturbed at 10 nm, up to 0.0708 at the 5 nm brush onset)
        // the layer admits 77% down to 52% of the bulk salt, so the LOCAL Debye length is
        // 1.14x to 1.39x the bulk one. Screening inside the layer is WEAKER than outside.
        val radius = peg.kuhnSegmentDiameter / 2.0
        assert(LayerPartitioning(0.0288872, radius).saltPartitionCoefficient.isCloseTo(0.76754, 1e-3))
        assert(LayerPartitioning(0.0708, radius).saltPartitionCoefficient.isCloseTo(0.51913, 1e-3))
        assert(LayerPartitioning(0.0288872, radius).debyeLengthRatio.isCloseTo(1.14146, 1e-3))
        assert(LayerPartitioning(0.0708, radius).debyeLengthRatio.isCloseTo(1.38793, 1e-3))
    }

    @Test
    fun `gate 5 should reuse the Kuhn segment radius from C-0002 rather than inventing one`() {
        // The fibre radius the Ogston expression needs is the PEG Kuhn segment's own
        // effective radius, 0.233 nm — derived in C-0002 as a cylinder of length b and
        // volume v_K. Nothing about the polymer is re-cited here.
        assert((peg.kuhnSegmentDiameter / 2.0).isCloseTo(0.233048, relativeTolerance = 1e-4))
        assert(designPoint.fibreRadius.isCloseTo(0.233048, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 5 should keep the dielectric decrement small enough not to matter`() {
        // The premise check the problem definition asks for: §4(c) suggests the layer
        // "lowers the local dielectric constant". At this layer's volume fraction it does,
        // by 3.9% — which moves the Bjerrum length by the same amount and the coupling
        // parameter by 8%. It is a correction, not a mechanism.
        assert(abs(designPoint.effectivePermittivity - WATER_RELATIVE_PERMITTIVITY) /
                WATER_RELATIVE_PERMITTIVITY < 0.05)
    }

    @Test
    fun `gate 1 should reject an unphysical layer`() {
        try {
            LayerPartitioning(polymerVolumeFraction = 1.5, fibreRadius = 0.233)
            throw AssertionError("should have rejected a volume fraction above one")
        } catch (e: IllegalArgumentException) {
            assert(e.message!!.contains("polymerVolumeFraction"))
        }
        try {
            LayerPartitioning(polymerVolumeFraction = 0.03, fibreRadius = 0.0)
            throw AssertionError("should have rejected a zero fibre radius")
        } catch (e: IllegalArgumentException) {
            assert(e.message!!.contains("fibreRadius"))
        }
    }
}
