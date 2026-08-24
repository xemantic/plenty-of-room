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
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-303` — what link stiffness `C-0180`'s coupled recovery needs, and what a crossover
 * connector can supply.
 *
 * Written before `tile/CrossoverLinkStiffness.kt` exists, and watched fail.
 *
 * The gates each test names are `T-303`'s own `P1`–`P5` and `F1`–`F10`.
 */
class CrossoverLinkStiffnessTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS

    private val g = crossoverSpanFloor(d, rP)

    private val kT = thermalEnergy(ROOM_TEMPERATURE)

    private val block = HoneycombBlock(4, 2)

    // ------------------------------------------------------- gate 2, the end-condition continuum

    @Test
    fun `gate 2 -- the guided end-condition factor is exactly 0 at a pin and exactly 12 at a clamp`() {
        assert(guidedEndConditionFactor(0.0) == 0.0)
        assert(abs(guidedEndConditionFactor(1e12) - 12.0) < 1e-9)
        // and it is exactly 6 at rho = 6, which is the one interior point the closed form pins
        assert(abs(guidedEndConditionFactor(6.0) - 6.0) < 1e-12)
    }

    @Test
    fun `gate 2 -- the guided end-condition factor rises monotonically and is refused below zero`() {
        val ladder = listOf(0.0, 0.5, 2.0, 6.0, 50.0, 1e6)
        ladder.zipWithNext().forEach { (low, high) ->
            assert(guidedEndConditionFactor(high) > guidedEndConditionFactor(low))
        }
        assertFailsWith<IllegalArgumentException> { guidedEndConditionFactor(-1e-9) }
    }

    @Test
    fun `gate 2 -- a connector with pinned ends supplies exactly zero bending stiffness`() {
        assert(connectorBendingLinkStiffness(1.42, kT, g, 0.0) == 0.0)
    }

    @Test
    fun `gate 2 -- the connector bending stiffness refuses a non-positive span`() {
        assertFailsWith<IllegalArgumentException> {
            connectorBendingLinkStiffness(1.42, kT, 0.0, 12.0)
        }
        assertFailsWith<IllegalArgumentException> {
            connectorBendingLinkStiffness(-1.0, kT, g, 12.0)
        }
    }

    // ------------------------------------------------------------------ gate 1, dimensional

    @Test
    fun `gate 1 -- the connector bending stiffness scales as one over the square of a common length rescaling`() {
        val base = connectorBendingLinkStiffness(1.42, kT, g, 12.0)
        listOf(0.5, 2.0, 7.0).forEach { lambda ->
            val scaled = connectorBendingLinkStiffness(1.42 * lambda, kT, g * lambda, 12.0)
            assert(abs(scaled * lambda * lambda - base) < 1e-12 * base)
        }
    }

    @Test
    fun `gate 1 -- the end-condition factor is invariant under a common rescaling of its own arguments`() {
        // rho = k_r g / EI is already dimensionless, so c depends on nothing but rho
        listOf(0.3, 3.0, 30.0).forEach { rho ->
            assert(abs(guidedEndConditionFactor(rho) - 12.0 * rho / (6.0 + rho)) < 1e-15)
        }
    }

    // -------------------------------------------------- gate 5, the two independent routes

    @Test
    fun `gate 5 -- the span-law route reproduces C-0194's 41 point 4338953 pN per nm`() {
        assert(abs(spanDerivedLinkStiffness(Gen1Tile.crossoverHingeStiffness(), rP, d) -
                41.4338953) < 1e-7)
    }

    @Test
    fun `gate 5 -- the softened-bond route is Chen et al's own construction on the displacement axis`() {
        // the same two phosphate bonds, the stretch modulus substituted for the bending rigidity
        assert(abs(transverseSoftenedBondLinkStiffness() -
                Gen1Tile.crossoverInPlaneStiffness()) < 1e-12)
        assert(abs(transverseSoftenedBondLinkStiffness() - 64.7058824) < 1e-6)
        // and it is linear in alpha, so Chen et al's own 0.6-1.2 bracket transfers unchanged
        assert(
            abs(transverseSoftenedBondLinkStiffness(Gen1Tile.CROSSOVER_ALPHA_MAX) -
                    2.0 * transverseSoftenedBondLinkStiffness(Gen1Tile.CROSSOVER_ALPHA_MAX / 2.0)) <
                    1e-12
        )
    }

    @Test
    fun `F6 -- the two k theta independent routes agree within one order of magnitude`() {
        val tension = spanDerivedLinkStiffness(Gen1Tile.crossoverHingeStiffness(), rP, d)
        val bond = transverseSoftenedBondLinkStiffness()
        val ratio = bond / tension
        assert(ratio > 0.1)
        assert(ratio < 10.0)
    }

    // --------------------------------------------------------------- the bracket itself

    @Test
    fun `P1 -- the bracket's floor is at or below its ceiling and the ceiling carries the bending term`() {
        val bracket = crossoverLinkStiffnessBracket(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            phosphateRadius = rP,
            interhelicalDistance = d,
            thermalEnergy = kT,
            softestPersistenceLength = 1.34 / 2.0,
            stiffestPersistenceLength = 2.84 / 2.0
        )
        assert(bracket.floor < bracket.ceiling)
        assert(bracket.floor == spanDerivedLinkStiffness(Gen1Tile.crossoverHingeStiffness(), rP, d))
        val bending = connectorBendingLinkStiffness(2.84 / 2.0, kT, g, 12.0)
        assert(
            abs(bracket.ceiling - (transverseSoftenedBondLinkStiffness() + bending)) <
                    1e-9 * bracket.ceiling
        )
    }

    @Test
    fun `P1 -- a central repulsive pair force contributes a NEGATIVE transverse stiffness`() {
        assert(centralPairForceTransverseStiffness(3.0, d, 7.14) < 0.0)
        assert(centralPairForceTransverseStiffness(0.0, d, 7.14) == 0.0)
        // and it is exactly minus f L / d
        assert(
            abs(centralPairForceTransverseStiffness(3.0, d, 7.14) + 3.0 * 7.14 / d) <
                    1e-12
        )
    }

    // ------------------------------------------------- the lattice builder, and F2's premise

    @Test
    fun `F2 -- the link-stiffness lattice at the default penalty is the object C-0180 measured`() {
        val ties = honeycombTiedLatticeAtLinkStiffness(
            block, 116, 1.0, tied = true,
            linkStiffness = HoneycombGrillage.RIGID_LINK_STIFFNESS
        )
        val reference = honeycombTiedLattice(block, 116, 1.0, tied = true)
        assert(ties.linkStiffness == reference.linkStiffness)
        assert(ties.turnElements.size == reference.turnElements.size)
        assert(ties.degreesOfFreedom == reference.degreesOfFreedom)
        // CLAUDE.md: a bare uniform pressure's peak dishing is its own conditioning noise, so
        // an identity between two lattices has to be taken on a WELL-CONDITIONED load case.
        // A unit point load at the face centre is what the coupling surrogate asks for anyway.
        val load = listOf(PointLoad(0.0, 0.0, 1.0))
        val a = ties.solve(uniformPressure(0.0), load).peakDishing(21)
        val b = reference.solve(uniformPressure(0.0), load).peakDishing(21)
        assert(abs(b) > 1e-6)
        assert(abs(a - b) < 1e-12 * abs(b))
    }

    @Test
    fun `F2 -- an untied lattice ignores the tie set at every link stiffness`() {
        listOf(41.4338953, 1e4).forEach { link ->
            val bare = honeycombTiedLatticeAtLinkStiffness(
                block, 116, 1.0, tied = false, linkStiffness = link
            )
            assert(bare.turnElements.isEmpty())
            assert(bare.linkStiffness == link)
        }
    }

    // -------------------------------------------- gate 3, the standing falsifier, at both ends

    @Test
    fun `F1 -- a uniform pressure on the tied lattice dishes exactly zero at both ends of the ladder`() {
        listOf(41.4338953, 1e4).forEach { link ->
            val lattice = honeycombTiedLatticeAtLinkStiffness(
                block, 116, 1.0, tied = true, linkStiffness = link
            )
            val solution = lattice.solve(uniformPressure(1e-4))
            assert(abs(solution.peakDishing(41)) < 1e-9 * abs(solution.meanDeflection))
        }
    }

    // -------------------------------------------------------------------- F4, monotonicity

    @Test
    fun `F4 -- the tied tile's peak dishing falls monotonically as the link stiffens`() {
        val ladder = listOf(41.4338953, 1e2, 1e3, 1e4)
        val readings = ladder.map { link ->
            val lattice = honeycombTiedLatticeAtLinkStiffness(
                block, 116, 1.0, tied = true, linkStiffness = link
            )
            val solution = lattice.solve(
                edgeCollarPressure(
                    1e-4, lattice.lengthS, lattice.lengthY, listOf(CollarTerm(0.5, 4.0))
                )
            )
            abs(solution.peakDishing(41)) / solution.meanDeflection
        }
        readings.zipWithNext().forEach { (soft, stiff) -> assert(stiff < soft + 1e-12) }
    }

    // -------------------------------------------------------------------- the bisector

    @Test
    fun `P2 -- the log bisector finds a known root and refuses a bracket that does not straddle`() {
        // f(k) = 1/k - 1/500 has its only root at k = 500
        val root = bisectLogLinkStiffnessThreshold(10.0, 1e4, 60) { 1.0 / it - 1.0 / 500.0 }
        assert(abs(root - 500.0) < 1e-6 * 500.0)
        assertFailsWith<IllegalArgumentException> {
            bisectLogLinkStiffnessThreshold(600.0, 1e4, 40) { 1.0 / it - 1.0 / 500.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            bisectLogLinkStiffnessThreshold(0.0, 1e4, 40) { 1.0 / it - 1.0 / 500.0 }
        }
    }

    @Test
    fun `P2 -- the bisector's bracket width in decades falls as two to the minus iteration count`() {
        val iterations = 12
        val root = bisectLogLinkStiffnessThreshold(10.0, 1e4, iterations) {
            1.0 / it - 1.0 / 500.0
        }
        val decades = 3.0 / Math.pow(2.0, iterations.toDouble())
        assert(abs(Math.log10(root) - Math.log10(500.0)) < decades)
    }
}
