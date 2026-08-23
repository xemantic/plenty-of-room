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
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.minimumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.turnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-297` — the crossover's **common mode is the vertical link**, and the lattice sits at the
 * stiff end of it.
 *
 * Written before `tile/CrossoverCommonMode.kt` and before
 * `HoneycombGrillage.turnLinkOffsetResponse`, and watched fail.
 *
 * The gates each test names are `T-297`'s own `P1`–`P6` and the five verification gates.
 */
class CrossoverCommonModeTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS

    private val kTheta = Gen1Tile.crossoverHingeStiffness()

    private val delta = allowedScaffoldCrossoverDepartureDegrees()

    private val block = HoneycombBlock(4, 2)

    private fun lattice(
        ties: List<HoneycombScaffoldTurnTie> = emptyList(),
        foundation: Double = Gen1Tile.FOUNDATION_SECANT,
        subdivisions: Int = 1,
        linkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS
    ) = HoneycombGrillage(
        block = block,
        rowBasePairs = 116,
        foundationStiffness = foundation,
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        scaffoldTurnTies = ties
    )

    private fun tied(
        linkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS,
        subdivisions: Int = 1
    ): HoneycombGrillage {
        val nodes = lattice(subdivisions = subdivisions).nodesPerBeam
        return lattice(
            ties = honeycombScaffoldTurnTies(block, nodes),
            subdivisions = subdivisions,
            linkStiffness = linkStiffness
        )
    }

    private fun phi(l: HoneycombGrillage, node: Int, beam: Int): Int =
        (node * l.beamCount + beam) * HoneycombGrillage.DOF_PER_NODE + HoneycombGrillage.PHI

    // --------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 - the three common-mode span ratios are dimensionless`() {
        // every one is a ratio of two span excesses, so scaling the whole geometry cannot move it
        listOf(0.5, 2.0, 7.0).forEach { s ->
            assert(abs(commonModeSpanRatio(s * d, s * rP) - commonModeSpanRatio(d, rP)) < 1e-12)
            assert(
                abs(
                    linearisedCommonModeSpanRatio(s * d, s * rP) -
                            linearisedCommonModeSpanRatio(d, rP)
                ) < 1e-12
            )
            assert(
                abs(
                    geometricCommonModeSpanRatio(s * d, s * rP) -
                            geometricCommonModeSpanRatio(d, rP)
                ) < 1e-12
            )
        }
    }

    @Test
    fun `gate 1 - the implied bond tension is a force and the link stiffness a force per length`() {
        // T = 2 k_theta / r_P is pN*nm/rad over nm, i.e. pN; k_R = T / g is pN/nm.
        val t = impliedCrossoverBondTension(kTheta, rP)
        assert(abs(t - 2.0 * kTheta / rP) < 1e-12)
        val kR = spanDerivedLinkStiffness(kTheta, rP, d)
        assert(abs(kR - t / (d - 2.0 * rP)) < 1e-12)
        // doubling the hinge stiffness doubles both, so neither carries a hidden angle
        assert(abs(impliedCrossoverBondTension(2.0 * kTheta, rP) - 2.0 * t) < 1e-12)
        assert(abs(spanDerivedLinkStiffness(2.0 * kTheta, rP, d) - 2.0 * kR) < 1e-12)
    }

    // --------------------------------------------------------------- gate 2: limiting cases

    @Test
    fun `gate 2 - the linearised ratio less the geometric term IS the raw span ratio, exactly`() {
        // d^2/(2 g r_P) - d/(2 r_P) = d/g, identically. It is the whole of the difference between
        // the frame-indifferent element a linear lattice may carry and CH-0242's raw expansion.
        listOf(2.0 to 0.9, 2.536 to rP, 2.69 to 1.0, 3.0 to 0.5).forEach { (dd, rr) ->
            val residue = linearisedCommonModeSpanRatio(dd, rr) - geometricCommonModeSpanRatio(dd, rr)
            assert(abs(residue - commonModeSpanRatio(dd, rr)) < 1e-12)
        }
    }

    @Test
    fun `gate 2 - a vanishing phosphate radius makes the two eigenmodes cost the same`() {
        // with the phosphates on the axes the span excess is isotropic in the two azimuths
        assert(abs(commonModeSpanRatio(d, 1e-9) - 1.0) < 1e-8)
    }

    @Test
    fun `gate 2 - the raw ratio reproduces the exact span at a small common-mode roll`() {
        val g = minimumTurnPhosphateSpan(d, rP)
        val theta = 1e-3
        val degrees = Math.toDegrees(theta)
        val exact = turnPhosphateSpan(d, rP, degrees, 180.0 + degrees) - g
        val predicted = rP * theta * theta * commonModeSpanRatio(d, rP)
        assert(abs(exact - predicted) / predicted < 1e-5)
    }

    @Test
    fun `gate 2 - a zero roll leaves no link offset and an empty map returns the zero field`() {
        assert(abs(turnLinkOffset(d, 1.0, 0.0)) < 1e-18)
        val l = tied()
        val response = l.turnLinkOffsetResponse(emptyMap<Int, Double>())
        (0 until l.degreesOfFreedom).forEach { assert(abs(response.coefficients[it]) < 1e-18) }
    }

    @Test
    fun `gate 2 - a turn index outside the tie list and a non-finite offset are refused`() {
        val l = tied()
        assertFailsWith<IllegalArgumentException> { l.turnLinkOffsetResponse(mapOf(-1 to 0.1)) }
        assertFailsWith<IllegalArgumentException> {
            l.turnLinkOffsetResponse(mapOf(l.turnElements.size to 0.1))
        }
        assertFailsWith<IllegalArgumentException> {
            l.turnLinkOffsetResponse(mapOf(0 to Double.NaN))
        }
    }

    @Test
    fun `gate 2 - the link offset load touches only the W and roll coordinates`() {
        // which is what makes `load[pinnedDof] = 0.0` provably inert for this load: the axial
        // rigid mode's pin is a U coordinate and no link offset ever reaches one.
        val l = tied()
        val load = l.turnLinkOffsetLoad(
            l.turnElements.indices.associateWith {
                turnLinkOffset(d, l.turnElements[it].unitY, Math.toRadians(delta))
            }
        )
        (0 until l.degreesOfFreedom).forEach {
            val component = it % HoneycombGrillage.DOF_PER_NODE
            if (component == HoneycombGrillage.THETA || component == HoneycombGrillage.U) {
                assert(abs(load[it]) < 1e-18)
            }
        }
    }

    @Test
    fun `gate 2 - the link offset field converges as the penalty stiffens`() {
        // P6: an offset in a CONSTRAINT converges to the constrained solution, so the stiff end
        // of the sweep is penalty-independent and the two ends are a bracket on one parameter.
        fun peak(k: Double): Double {
            val l = tied(linkStiffness = k)
            val offsets = l.turnElements.indices.associateWith {
                turnLinkOffset(d, l.turnElements[it].unitY, Math.toRadians(delta))
            }
            return l.turnLinkOffsetResponse(offsets).peakDishing(41)
        }
        val a = peak(1e4)
        val b = peak(1e5)
        val c = peak(1e6)
        assert(abs(c - b) < abs(b - a))
        assert(abs(c - b) / abs(c) < 1e-2)
    }

    // --------------------------------------------- gate 3: symmetry, conservation, falsifiers

    @Test
    fun `gate 3 - P1 the link residual is a function of the SUM of the two rolls`() {
        // CH-0242 section 3 says the bond and tie carry the relative roll "and nothing else on the
        // azimuthal coordinates". The link's own gradient is (1, armY, -1, armY): a COMMON roll at
        // fixed axes stores zero hinge energy and the closed-form link energy.
        val l = lattice()
        val theta = 1e-3
        val field = l.nodalField({ _, _, _ -> 0.0 }, { _, _, _ -> 0.0 }, { _, _, _ -> theta },
            { _, _, _ -> 0.0 })
        assert(l.hingeEnergy(field) < 1e-18)
        val expected = l.bonds.sumOf { bond ->
            val residual = turnLinkOffset(l.bondLength, bond.unitY, theta)
            0.5 * l.linkStiffness * residual * residual
        }
        assert(expected > 1e-9)
        assert(abs(l.linkEnergy(field) - expected) / expected < 1e-12)
    }

    @Test
    fun `gate 3 - P2 half the interhelical distance is the ONLY frame-indifferent connector arm`() {
        // a rigid roll of the whole block is W = alpha*y, Phi = alpha: the residual an arm `a`
        // leaves is alpha*unitY*(2a - d), zero for every bond direction iff a = d/2 and for no
        // other arm. So the link's arm is a theorem, not a fitted parameter.
        val alpha = 1e-3
        listOf(1.0, 0.5, -0.5).forEach { unitY ->
            assert(abs(rigidRollLinkResidual(d, unitY, frameIndifferentLinkArm(d), alpha)) < 1e-18)
            listOf(0.4 * d, 0.6 * d, rP).forEach { arm ->
                assert(abs(rigidRollLinkResidual(d, unitY, arm, alpha)) > 1e-9)
            }
        }
        assert(abs(frameIndifferentLinkArm(d) - d / 2.0) < 1e-18)
    }

    @Test
    fun `gate 3 - a rigid roll stores no hinge, link or slip energy in the assembled lattice`() {
        val l = tied()
        val alpha = 1e-3
        val field = l.nodalField({ _, y, _ -> alpha * y }, { _, _, _ -> 0.0 },
            { _, _, _ -> alpha }, { _, _, _ -> 0.0 })
        assert(l.hingeEnergy(field) < 1e-18)
        assert(l.linkEnergy(field) < 1e-18)
        assert(l.slipEnergy(field) < 1e-18)
        l.turnElements.forEach { assert(abs(l.turnLinkExtension(field, it)) < 1e-15) }
    }

    @Test
    fun `gate 3 - a uniform load on the free tied lattice dishes exactly zero at both link ends`() {
        // CLAUDE.md's standing falsifier, re-taken because a link stiffness is a stiffness and
        // moves every entry of the matrix.
        listOf(spanDerivedLinkStiffness(kTheta, rP, d), HoneycombGrillage.RIGID_LINK_STIFFNESS)
            .forEach { k ->
                val l = tied(linkStiffness = k)
                assert(l.solve(uniformPressure(1e-3)).peakDishing(41) < 1e-9)
            }
    }

    @Test
    fun `gate 3 - P5 the link offset load is orthogonal to the relative roll at every tie`() {
        // the mirror of C-0190's F2, which found the RELATIVE prestrain's projection on the
        // common mode to be exactly zero: a common-mode load has no image on the relative one.
        val l = tied()
        val offsets = l.turnElements.indices.associateWith {
            turnLinkOffset(d, l.turnElements[it].unitY, Math.toRadians(delta))
        }
        val load = l.turnLinkOffsetLoad(offsets)
        var any = false
        (0 until l.degreesOfFreedom).forEach { if (abs(load[it]) > 1e-12) any = true }
        assert(any)
        l.turnElements.forEach { element ->
            val upper = phi(l, element.node, element.tie.upperBeam)
            val lower = phi(l, element.node, element.tie.lowerBeam)
            // the relative-roll direction at this tie: +1 on the upper roll, -1 on the lower
            assert(abs(load[upper] - load[lower]) < 1e-12)
        }
    }

    @Test
    fun `gate 3 - P5 the same load is NOT orthogonal to the demanded common-mode roll`() {
        val l = tied()
        val offsets = l.turnElements.indices.associateWith {
            turnLinkOffset(d, l.turnElements[it].unitY, Math.toRadians(delta))
        }
        val load = l.turnLinkOffsetLoad(offsets)
        val worst = l.turnElements.maxOf { element ->
            val upper = phi(l, element.node, element.tie.upperBeam)
            val lower = phi(l, element.node, element.tie.lowerBeam)
            abs(load[upper] + load[lower])
        }
        assert(worst > 1e-9)
    }

    @Test
    fun `gate 3 - a link offset changes no entry of the stiffness matrix`() {
        // an eigenstrain is a LOAD (C-0104), so the field is exactly linear in it and one
        // factorisation serves the whole ladder.
        val l = tied()
        val offsets = l.turnElements.indices.associateWith { 0.01 }
        val half = l.turnElements.indices.associateWith { 0.005 }
        val full = l.turnLinkOffsetResponse(offsets)
        val part = l.turnLinkOffsetResponse(half)
        (0 until l.degreesOfFreedom).forEach {
            assert(abs(full.coefficients[it] - 2.0 * part.coefficients[it]) < 1e-9)
        }
    }

    // --------------------------------------------------------------- gate 4: convergence

    @Test
    fun `gate 4 - the link offset field converges under beam subdivision`() {
        fun peak(subdivisions: Int): Double {
            val l = tied(subdivisions = subdivisions)
            val offsets = l.turnElements.indices.associateWith {
                turnLinkOffset(d, l.turnElements[it].unitY, Math.toRadians(delta))
            }
            return l.turnLinkOffsetResponse(offsets).peakDishing(41)
        }
        val one = peak(1)
        val two = peak(2)
        val four = peak(4)
        assert(abs(four - two) < abs(two - one))
    }

    // --------------------------------------------------------------- gate 5: cross-check

    @Test
    fun `gate 5 - CH-0242's ratio re-derived, and it is not the value in circulation`() {
        val ratio = commonModeSpanRatio(d, rP)
        // the challenge, C-0190's headline, the challenges index and two prose strings of
        // T-291's own result file all carry 3.52810239; T-291's openQuestions block emitted
        // 3.52847408 from this same expression, and that is the right one.
        assert(abs(ratio - 3.528474075216736) < 1e-12)
        assert(abs(ratio - 3.52810239) > 1e-4)
    }

    @Test
    fun `gate 5 - P3 the lattice's own common-mode stiffness EXCEEDS the physical one`() {
        val physical = commonModeSpanRatio(d, rP) * kTheta
        val inPlane = latticeCommonModeAzimuthalStiffness(
            HoneycombGrillage.RIGID_LINK_STIFFNESS, d, 1.0
        )
        val interlayer = latticeCommonModeAzimuthalStiffness(
            HoneycombGrillage.RIGID_LINK_STIFFNESS, d, 0.5
        )
        assert(inPlane > physical)
        assert(interlayer > physical)
        // and the span law's own link stiffness reproduces the LINEARISED ratio exactly
        val spanDerived = latticeCommonModeAzimuthalStiffness(
            spanDerivedLinkStiffness(kTheta, rP, d), d, 1.0
        )
        val expected = linearisedCommonModeSpanRatio(d, rP) * kTheta
        assert(abs(spanDerived - expected) / expected < 1e-12)
    }

    @Test
    fun `gate 5 - the lattice's bonds carry exactly the two unit-Y magnitudes the honeycomb has`() {
        val l = tied()
        val magnitudes = (l.bonds.map { abs(it.unitY) } + l.turnElements.map { abs(it.unitY) })
            .map { Math.round(it * 1e6) / 1e6 }
            .toSortedSet()
        assert(magnitudes.size == 2)
        assert(abs(magnitudes.first() - 0.5) < 1e-6)
        assert(abs(magnitudes.last() - 1.0) < 1e-6)
    }
}
