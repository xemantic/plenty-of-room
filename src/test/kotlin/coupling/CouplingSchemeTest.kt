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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.FreelyJointedChain
import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.test.Test

/**
 * `T-16` — the candidate couplings, and the two ways they fail.
 *
 * A coupling fails by being **too soft** (it does not stabilise, and it does not place the
 * operating point) or by being **too stiff** (it places the operating point at a stroke far
 * below §3's 3 nm, and it puts the whole output force on too few load paths). `C-0014` only
 * ever met the first failure mode, because it was sizing an *anchor*; a **load** meets the
 * second, and these tests keep the two apart.
 */
class CouplingSchemeTest {

    private val mandated = mandatedCouplingStiffness(100.0, 3.0)

    /** `C-0015`'s flatness scheme: 45 attachments as 3 × 15, one row per duplex. */
    private val flatnessGrid = attachmentGrid(3, 15, 40.0, 40.35)

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - an entropic coupling reaction is zero at zero stroke and monotone above it`() {
        val chain = FreelyJointedChain(8.0, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        val coupling = EntropicCoupling(45, chain)
        assert(coupling.reaction(0.0).isCloseTo(0.0, 1e-12))
        val values = listOf(0.5, 1.0, 2.0, 3.0, 4.0).map { coupling.reaction(it) }
        values.zipWithNext { a, b -> assert(b > a) }
        // and it is a genuine spring: its secant over the stroke is a stiffness
        assert((coupling.reaction(3.0) / 3.0) > 0.0)
    }

    @Test
    fun `gate 2 limiting cases - a coupling designed to deliver the target force does so exactly`() {
        val contour = spacerContourForTarget(SsDnaTether.KUHN_LENGTH_ZERO_FORCE, 45, 100.0, 3.0)
        val coupling = EntropicCoupling(45, FreelyJointedChain(contour, SsDnaTether.KUHN_LENGTH_ZERO_FORCE))
        assert(coupling.reaction(3.0).isCloseTo(100.0, 1e-7))
        // its secant over the §3 stroke is therefore §3's own mandated stiffness, exactly
        assert((coupling.reaction(3.0) / 3.0).isCloseTo(mandated, 1e-7))
        // and its TANGENT there is larger, because the chain strain-stiffens — the stroke and
        // the stability use different stiffnesses, exactly as C-0010 found for noise
        assert(coupling.tangentStiffness(3.0) > mandated)
    }

    @Test
    fun `gate 2 limiting cases - a duplex coupling is too stiff to stroke and an over-long spacer too soft to hold`() {
        val characteristic = OutputCharacteristic { 34.46 + 15.0 * it }
        // 45 duplex links of 10 nm: 4950 pN/nm, which takes essentially the whole stroke away
        val stiff = firstOperatingStroke(characteristic, LinearCoupling(45 * 110.0), 9.0)!!
        assert(stiff < 0.01)
        // 45 slack 80-nt spacers: 1.1 pN/nm, softer than the characteristic's own slope, so
        // there is no crossing at all — the tile runs away
        val slackChain = FreelyJointedChain(80 * SsDnaTether.CONTOUR_PER_NUCLEOTIDE, 2.10)
        assert(firstOperatingStroke(characteristic, EntropicCoupling(45, slackChain), 9.0) == null)
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - a distributed coupling shares the load equally and a concentrated one does not`() {
        val distributed = CouplingScheme(
            name = "45 x 12 nt spacer to a rigid superstructure",
            attachmentCount = 45,
            path = listOf(CouplingPathElement("ssDNA spacer", 0.95)),
            loadPathCrossesLattice = false
        )
        assert(distributed.perPathStaticForce(100.0).isCloseTo(100.0 / 45.0))
        // C-0015: one attachment row per duplex zeroes the per-load-path CROSSOVER force
        // exactly, so C-0009's 7.6x concentration does not apply to it
        assert(distributed.concentratedPathForce(100.0, 7.6).isCloseTo(100.0 / 45.0))
        val concentrated = CouplingScheme(
            name = "one lever",
            attachmentCount = 1,
            path = listOf(CouplingPathElement("duplex, axial", 110.0)),
            loadPathCrossesLattice = true
        )
        assert(concentrated.concentratedPathForce(100.0, 7.6).isCloseTo(760.0))
    }

    @Test
    fun `gate 3 symmetry - the yaw and translation by-products stand in the exact ratio of the placement radii`() {
        // C-0014's exact r-squared cancellation, re-derived on C-0015's own 3 x 15 grid
        val perAnchor = 0.95
        val lateral = flatnessGrid.size * perAnchor
        val yaw = yawStiffness(perAnchor, flatnessGrid)
        val meanSquaredRadius = flatnessGrid.sumOf { (x, y) -> x * x + y * y } / flatnessGrid.size
        assert((yaw / lateral).isCloseTo(meanSquaredRadius))
    }

    @Test
    fun `gate 3 symmetry - a normal coupling supplies at most as much lateral stiffness as normal`() {
        val contour = spacerContourForTarget(SsDnaTether.KUHN_LENGTH_ZERO_FORCE, 45, 100.0, 3.0)
        val chain = FreelyJointedChain(contour, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        val tension = 100.0 / 45.0
        val lateral = 45 * chain.transverseStiffness(tension)
        val normal = 45 * chain.tangentStiffness(tension)
        assert(lateral <= normal * (1.0 + 1e-9))
        // and "at most" is still two orders of magnitude above C-0014's 0.4602 pN/nm bound
        assert(lateral > 20.0 * 0.460216)
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 cross-check - C-0014's ssDNA design rule is reproduced by this task's own solver`() {
        // C-0014: eight tethers, 10 nm layer, b = 2.10 nm, longest admissible contour 103.4 nm
        // for k_lat >= 0.460216 pN/nm — the Gaussian rule L_c b <= 3 N k_BT / k_req
        val required = 0.460216
        val contour = gaussianContourCeiling(
            kuhnLength = 2.10, count = 8, requiredStiffness = required
        )
        assert(contour.isCloseTo(3.0 * 8 * thermalEnergy() / (required * 2.10), 1e-12))
        // C-0014 publishes 103.4 nm, solved on the full freely-jointed chain rather than on
        // its Gaussian limit; the two agree to 0.6 %, which is the size of the strain
        // stiffening at 1.17 pN and is the direction C-0014 states (the chain is stiffer)
        assert(contour.isCloseTo(103.4, 1e-2))
        assert(contour < 103.4)
    }
}
