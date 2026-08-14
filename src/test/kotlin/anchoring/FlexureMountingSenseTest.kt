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
import com.xemantic.nano.plentyofroom.actuator.ActuatorGeometry
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-75` / `T-78` — which body carries the standoffs, and what sits under the flexure's midspan.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that `C-0030`'s "free binary choice" is **not** free: the midspan
 * deflection is the change in the two bodies' separation, so its sign is a kinematic identity of
 * the stack, and it is decided by the **product** of two binaries rather than by the one
 * `C-0030` names.
 */
class FlexureMountingSenseTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val stretch = Gen1Tile.DUPLEX_STRETCH_MODULUS

    /** `C-0028`'s recommended base — two crossovers laid ACROSS the flexure. */
    private val base = StandoffBase.crossovers(2, favourableOrientation = true)

    private val designFlexibility = standoffTipFlexibility(ei, 8.0, base.rotationalStiffness)

    /** `C-0030`'s recommended design: `B2`, `ℓ = 8 nm`, span 31.82 nm, favourable. */
    private fun designFlexure(span: Double = 31.8151) =
        CoupledJointFlexure(ei, span, designFlexibility, stretch)

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the deflection rate is a dimensionless plus or minus one at every mounting`() {
        FlexureMounting.ALL.forEach {
            assert(abs(it.deflectionRate).isCloseTo(1.0))
        }
        assert(FlexureMounting.ALL.size == 4)
    }

    @Test
    fun `gate 1 dimensional consistency - the deflection rate contains neither the tie length nor the standoff length nor the span`() {
        // the identity dδ/ds = (v_base − v_driven)/n has no length in it at all, so the same
        // mounting must return the same rate however the stack around it is dimensioned
        FlexureMounting.ALL.forEach { mounting ->
            listOf(1.0, 5.0, 8.0, 40.0).forEach { standoffLength ->
                listOf(0.5, 12.0, 60.0).forEach { tieLength ->
                    val stack = MountingStack(mounting, 10.0, standoffLength, tieLength)
                    assert(stack.deflectionRate.isCloseTo(mounting.deflectionRate))
                }
            }
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the beam shape is dimensionless and is exactly one at the midspan`() {
        listOf(0.0, 1.0, 4.0, 20.0, Double.POSITIVE_INFINITY).forEach { restraint ->
            assert(restrainedBeamShape(restraint, 0.5).isCloseTo(1.0))
            assert(restrainedBeamShape(restraint, 0.0).isCloseTo(0.0))
            assert(restrainedBeamShape(restraint, 1.0).isCloseTo(0.0))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the aperture length is a length and scales with the span`() {
        val short = apertureLength(20.0, 4.0, stroke = 10.0, standoffLength = 8.0)
        val long = apertureLength(40.0, 4.0, stroke = 10.0, standoffLength = 8.0)
        assert(short > 0.0)
        assert((long / short).isCloseTo(2.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the array volume is a volume and goes as the square of the duplex radius`() {
        val thin = flexureArrayVolume(45, 31.82, 8.0, duplexRadius = 0.5)
        val thick = flexureArrayVolume(45, 31.82, 8.0, duplexRadius = 1.0)
        assert((thick / thin).isCloseTo(4.0))
        assert(thick.isCloseTo(45.0 * PI * (31.82 + 16.0)))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { MountingStack(FlexureMounting.ALL[0], 10.0, -1.0, 5.0) }
        assertFailsWith<IllegalArgumentException> { MountingStack(FlexureMounting.ALL[0], 10.0, 8.0, -0.1) }
        assertFailsWith<IllegalArgumentException> { restrainedBeamShape(-1.0, 0.25) }
        assertFailsWith<IllegalArgumentException> { restrainedBeamShape(4.0, 1.5) }
        assertFailsWith<IllegalArgumentException> { flexureArrayVolume(0, 31.0, 8.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - the beam shape reduces to the pinned and clamped textbook shapes`() {
        listOf(0.05, 0.2, 0.35, 0.5).forEach { u ->
            assert(restrainedBeamShape(0.0, u).isCloseTo(3.0 * u - 4.0 * u * u * u))
            assert(
                restrainedBeamShape(Double.POSITIVE_INFINITY, u)
                    .isCloseTo(12.0 * u * u - 16.0 * u * u * u)
            )
        }
    }

    @Test
    fun `gate 2 limiting cases - the shape's end slope reproduces the beam solution's own end rotation`() {
        // θ₀ = PL²/(8EI(2+ρ)) and δ = PL³(8+ρ)/(192EI(2+ρ)), so L·θ₀/δ = 24/(8+ρ) — derived from
        // the beam's solution, not from the shape polynomial
        listOf(0.0, 1.0, 4.0, 8.0, 25.0).forEach { restraint ->
            val step = 1.0e-7
            val slope = (restrainedBeamShape(restraint, step) - restrainedBeamShape(restraint, 0.0)) / step
            assert(slope.isCloseTo(24.0 / (8.0 + restraint), relativeTolerance = 1.0e-5))
        }
    }

    @Test
    fun `gate 2 limiting cases - a zero pre-bow reproduces C-0030's own signed stroke laws exactly`() {
        val flexure = designFlexure()
        FlexureMounting.ALL.forEach { mounting ->
            listOf(0.5, 3.0, 7.0, 10.0).forEach { stroke ->
                val delivered = preBowDeliveredForce(flexure, mounting, preBow = 0.0, stroke = stroke)
                assert(delivered.isCloseTo(flexure.strokeReaction(stroke, mounting.orientation)))
                assert(
                    preBowTangentStiffness(flexure, mounting, 0.0, stroke)
                        .isCloseTo(flexure.strokeTangentStiffness(stroke, mounting.orientation))
                )
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - a zero pre-bow costs no preload and a positive one costs a positive preload`() {
        val flexure = designFlexure()
        FlexureMounting.ALL.forEach { mounting ->
            assert(preBowPreload(flexure, mounting, 0.0).isCloseTo(0.0))
        }
        val adverse = FlexureMounting.ALL.first { it.orientation == FlexureOrientation.ADVERSE }
        assert(preBowPreload(flexure, adverse, 3.0) > 0.0)
    }

    @Test
    fun `gate 2 limiting cases - a standoff longer than the contact distance leaves clearance and a shorter one leaves none`() {
        assert(midspanPenetration(3.0, standoffLength = 8.0).isCloseTo(0.0))
        assert(midspanPenetration(10.0, standoffLength = 8.0).isCloseTo(10.0 - (8.0 - 2.69)))
        assert(midspanPenetration(3.0, standoffLength = 2.0).isCloseTo(3.0))
        assert(apertureLength(31.82, 4.0, stroke = 3.0, standoffLength = 8.0).isCloseTo(0.0))
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 symmetry - the four mountings split two-two and NEITHER variable alone decides the sign`() {
        val favourable = FlexureMounting.ALL.filter { it.orientation == FlexureOrientation.FAVOURABLE }
        assert(favourable.size == 2)
        // both bodies appear on both sides, and so do both normals: the sign is a PRODUCT
        assert(favourable.map { it.baseBody }.toSet().size == 2)
        assert(favourable.map { it.standoffNormal }.toSet().size == 2)
        // flipping either variable alone flips the sign, at every mounting
        FlexureMounting.ALL.forEach {
            assert(it.copy(baseBody = it.baseBody.other).deflectionRate.isCloseTo(-it.deflectionRate))
            assert(
                it.copy(standoffNormal = it.standoffNormal.reversed)
                    .deflectionRate.isCloseTo(-it.deflectionRate)
            )
        }
    }

    @Test
    fun `gate 3 symmetry - the tie crossing the base plane is EQUIVALENT to the favourable sense, on two independent constructions`() {
        // `deflectionRate` differentiates a kinematic chain; `tieCrossesBasePlane` compares three
        // z coordinates of the built stack. Nothing makes them agree.
        FlexureMounting.ALL.forEach { mounting ->
            listOf(5.0, 7.0, 10.0).forEach { layerHeight ->
                listOf(5.0, 8.0, 10.0).forEach { standoffLength ->
                    // every realisable effort height, i.e. every one the mounting can actually build
                    listOf(2.0, 6.0, 14.0).forEach { above ->
                        val effortHeight =
                            mounting.minimumEffortHeightAboveTileTop(standoffLength) + above
                        val stack = MountingStack(
                            mounting, layerHeight, standoffLength,
                            tieLength = mounting.tieLengthForEffortHeight(effortHeight, standoffLength)
                        )
                        assert(
                            stack.tieCrossesBasePlane ==
                                    (mounting.orientation == FlexureOrientation.FAVOURABLE)
                        )
                        assert(stack.effortHeightAboveTileTop.isCloseTo(effortHeight))
                    }
                }
            }
        }
    }

    @Test
    fun `gate 5 cross-check - only the outboard topology can put the effort point where section 3 puts it`() {
        // §3's band is 5 nm wide and its layer-height range is 5 nm wide, so a CONSTANT attachment
        // height is forced and it is 5 nm (C-0012, ActuatorGeometry). The inboard topologies stack
        // the standoff and the tie in series between the two bodies, so they cannot get under it.
        FlexureMounting.ALL.forEach { mounting ->
            if (mounting.inboard) {
                assertFailsWith<IllegalArgumentException> {
                    mounting.tieLengthForEffortHeight(5.0, 8.0)
                }
                assertFailsWith<IllegalArgumentException> {
                    mounting.tieLengthForEffortHeight(5.0, 5.0001)
                }
            } else {
                assert(mounting.tieLengthForEffortHeight(5.0, 8.0) > 0.0)
                assert(mounting.tieLengthForEffortHeight(5.0, 10.0) > 0.0)
            }
        }
    }

    @Test
    fun `gate 3 symmetry - the standoff is in compression exactly where the mounting is favourable`() {
        // the beam's end shear acts along the standoff's own axis; a standoff in TENSION cannot buckle
        FlexureMounting.ALL.forEach {
            assert(
                (it.standoffAxialSense == StandoffAxialSense.COMPRESSION) ==
                        (it.orientation == FlexureOrientation.FAVOURABLE)
            )
        }
    }

    @Test
    fun `gate 3 symmetry - the beam shape is symmetric about the midspan`() {
        listOf(0.0, 2.0, 4.0, 30.0).forEach { restraint ->
            listOf(0.03, 0.17, 0.28, 0.44).forEach { u ->
                assert(restrainedBeamShape(restraint, u).isCloseTo(restrainedBeamShape(restraint, 1.0 - u)))
            }
        }
    }

    @Test
    fun `gate 3 conservation - the midspan travel plus the penetration is the stroke, identically`() {
        listOf(3.0, 6.0, 10.0, 14.0).forEach { stroke ->
            listOf(4.0, 8.0, 12.0).forEach { standoffLength ->
                val clearance = favourableStrokeClearance(standoffLength)
                val penetration = midspanPenetration(stroke, standoffLength)
                assert((minOf(stroke, clearance) + penetration).isCloseTo(stroke))
            }
        }
    }

    @Test
    fun `gate 3 symmetry - the inboard topology's effort point cannot come closer to the tile than the standoff length`() {
        FlexureMounting.ALL.forEach { mounting ->
            listOf(3.0, 5.0, 8.0, 10.0).forEach { standoffLength ->
                val floor = mounting.minimumEffortHeightAboveTileTop(standoffLength, 10.0)
                if (mounting.orientation == FlexureOrientation.ADVERSE) {
                    assert(floor.isCloseTo(standoffLength))
                } else {
                    assert(floor.isCloseTo(0.0))
                }
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the tie's own aperture is present at every stroke including the one that needs no slot`() {
        // C-0023's two-sidedness makes the midspan tie a duplex, and the favourable topology makes
        // it cross the base plane by construction — so 45 duplex-omission holes are the FLOOR
        assert(tieApertureArea(45).isCloseTo(45.0 * 2.69 * 2.69))
        assert(apertureLength(31.82, 4.0, stroke = 3.0, standoffLength = 8.0).isCloseTo(0.0))
        assert(tieApertureArea(45) > 0.0)
        assertFailsWith<IllegalArgumentException> { tieApertureArea(0) }
    }

    @Test
    fun `gate 3 conservation - the adverse mounting's compliance is a LENGTH away, not absent`() {
        val adverse = FlexureMounting.ALL.first { it.orientation == FlexureOrientation.ADVERSE }
        val favourable = FlexureMounting.ALL.first { it.orientation == FlexureOrientation.FAVOURABLE }
        val adverseLength = standoffLengthForCompliance(
            ei, base.rotationalStiffness, 45, 100.0 / 3.0, 3.0, adverse, 40.0, stretch
        )
        val favourableLength = standoffLengthForCompliance(
            ei, base.rotationalStiffness, 45, 100.0 / 3.0, 3.0, favourable, 40.0, stretch
        )
        // C-0030 swept 3-10 nm and found the adverse mounting past the ceiling at every one; the
        // honest form of that is the length it would need, and it is outside C-0017's envelope
        assert(adverseLength > 10.0)
        assert(adverseLength.isFinite())
        assert(favourableLength < 10.0)
        assert(favourableLength < adverseLength)
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 numerical convergence - the aperture solve is independent of its own scan resolution`() {
        val coarse = apertureLength(31.82, 4.0, 10.0, 8.0, scanSteps = 64)
        val fine = apertureLength(31.82, 4.0, 10.0, 8.0, scanSteps = 4096)
        assert(coarse.isCloseTo(fine, relativeTolerance = 1.0e-12))
    }

    @Test
    fun `gate 4 numerical convergence - the aperture level solve inverts the shape function`() {
        listOf(0.0, 3.0, 12.0).forEach { restraint ->
            listOf(0.15, 0.4, 0.7, 0.95).forEach { level ->
                val u = apertureHalfPositionFraction(restraint, level)
                assert(restrainedBeamShape(restraint, u).isCloseTo(level, relativeTolerance = 1.0e-9))
            }
        }
    }

    @Test
    fun `gate 4 numerical convergence - the maximum assembled tangent is independent of its sample count`() {
        val flexure = designFlexure()
        val adverse = FlexureMounting.ALL.first { it.orientation == FlexureOrientation.ADVERSE }
        val coarse = maximumAssembledTangent(flexure, 45, adverse, 3.0, 3.0, samples = 200)
        val fine = maximumAssembledTangent(flexure, 45, adverse, 3.0, 3.0, samples = 20000)
        assert(coarse.isCloseTo(fine, relativeTolerance = 1.0e-4))
    }

    @Test
    fun `gate 4 numerical convergence - the pre-bowed span returns its own target secant`() {
        FlexureMounting.ALL.forEach { mounting ->
            listOf(0.0, 1.5, 3.0).forEach { preBow ->
                val span = preBowedFlexureSpan(
                    ei, designFlexibility, 45, 100.0 / 3.0, 3.0, mounting, preBow, stretch
                )
                val assembled = 45.0 * preBowDeliveredForce(
                    CoupledJointFlexure(ei, span, designFlexibility, stretch), mounting, preBow, 3.0
                ) / 3.0
                assert(assembled.isCloseTo(100.0 / 3.0, relativeTolerance = 1.0e-8))
            }
        }
    }

    // ---------------------------------------------------------------- gate 5 — cross-check

    @Test
    fun `gate 5 cross-check - section 3's 20-25 nm effort band is reproduced by a 5 nm attachment height at both ends`() {
        val geometry = ActuatorGeometry()
        assert(geometry.effortPointHeight(5.0).isCloseTo(20.0))
        assert(geometry.effortPointHeight(7.0).isCloseTo(22.0))
        assert(geometry.effortPointHeight(10.0).isCloseTo(25.0))
    }

    @Test
    fun `gate 5 cross-check - the loose reading of section 3's band and the constant one agree at the 10 nm layer`() {
        // loose: the effort point may lie anywhere in 20-25 nm at every layer height
        // constant: the attachment height is 5 nm, which is the only reading reproducing both ends
        FlexureMounting.ALL.filter { it.inboard }.forEach { mounting ->
            assert(mounting.maximumStandoffLengthUnderEffortCeiling(10.0, 25.0).isCloseTo(5.0))
            assert(mounting.maximumStandoffLengthUnderEffortCeiling(7.0, 25.0).isCloseTo(8.0))
            assert(mounting.maximumStandoffLengthUnderEffortCeiling(5.0, 25.0).isCloseTo(10.0))
            // the constant reading is the ceiling of the loose one at the 10 nm layer, exactly
            assert(mounting.minimumEffortHeightAboveTileTop(5.0).isCloseTo(5.0))
        }
        FlexureMounting.ALL.filterNot { it.inboard }.forEach {
            assert(it.maximumStandoffLengthUnderEffortCeiling(10.0, 25.0).isInfinite())
        }
    }

    @Test
    fun `gate 5 cross-check - the stack reproduces C-0030's favourable clearance table`() {
        // C-0030: largest stroke that fits = ℓ − 2.69 nm; 5.31 nm at ℓ = 8 nm
        assert(favourableStrokeClearance(8.0).isCloseTo(5.31))
        assert(favourableStrokeClearance(6.0).isCloseTo(3.31))
        assert(favourableStrokeClearance(10.0).isCloseTo(7.31))
        assert(favourableStrokeClearance(2.0).isCloseTo(0.0))
    }

    @Test
    fun `gate 5 cross-check - C-0030's favourable and adverse designs at 8 nm are reproduced`() {
        val favourable = FlexureMounting.ALL.first { it.orientation == FlexureOrientation.FAVOURABLE }
        val adverse = FlexureMounting.ALL.first { it.orientation == FlexureOrientation.ADVERSE }
        val favourableSpan = preBowedFlexureSpan(
            ei, designFlexibility, 45, 100.0 / 3.0, 3.0, favourable, 0.0, stretch
        )
        val adverseSpan = preBowedFlexureSpan(
            ei, designFlexibility, 45, 100.0 / 3.0, 3.0, adverse, 0.0, stretch
        )
        // C-0030's published B2 / 8 nm rows: span 31.82 / 40.14 nm, tangent 25.23 / 44.82 pN/nm
        assert(favourableSpan.isCloseTo(31.82, relativeTolerance = 1.0e-3))
        assert(adverseSpan.isCloseTo(40.14, relativeTolerance = 1.0e-3))
        assert(
            (45.0 * CoupledJointFlexure(ei, favourableSpan, designFlexibility, stretch)
                .strokeTangentStiffness(3.0, FlexureOrientation.FAVOURABLE))
                .isCloseTo(25.23, relativeTolerance = 1.0e-3)
        )
        assert(
            (45.0 * CoupledJointFlexure(ei, adverseSpan, designFlexibility, stretch)
                .strokeTangentStiffness(3.0, FlexureOrientation.ADVERSE))
                .isCloseTo(44.82, relativeTolerance = 1.0e-3)
        )
    }

    @Test
    fun `gate 5 cross-check - the one mounting that puts the flexure under the tile puts it inside the actuation gap`() {
        val underTile = FlexureMounting(MountingBody.TILE, StandoffNormal.DOWNWARD)
        val stack = MountingStack(underTile, layerHeight = 10.0, standoffLength = 8.0, tieLength = 20.0)
        assert(stack.beamInsideActuationGap)
        assert(!stack.beamClearsElectrode(duplexRadius = 1.0) == (stack.beamPlane - 1.0 <= 0.0))
        // and the array it would put there is a large fraction of the layer's own volume
        val fraction = layerVolumeFraction(
            flexureArrayVolume(45, 31.82, 8.0), footprintArea = 1600.0, layerHeight = 10.0
        )
        assert(fraction > 0.25)
    }

    @Test
    fun `gate 5 cross-check - the surviving favourable mounting is the one whose standoffs stand on the superstructure`() {
        val survivors = FlexureMounting.ALL.filter {
            it.orientation == FlexureOrientation.FAVOURABLE && !it.putsFlexureUnderTheTile
        }
        assert(survivors.size == 1)
        assert(survivors.single().baseBody == MountingBody.SUPERSTRUCTURE)
        assert(survivors.single().standoffNormal == StandoffNormal.UPWARD)
    }
}
