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
import com.xemantic.nano.plentyofroom.electrostatics.MengMagnesium
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-310` — a bond's normal link is two mechanisms, and `HoneycombGrillage` carries one scalar.
 *
 * Written before `tile/CrossoverLinkResolution.kt` and before `HoneycombGrillage`'s
 * `radialLinkStiffness` exist, and watched fail.
 *
 * The gates each test names are `T-310`'s own `P1`–`P6` and `F1`–`F10`.
 */
class CrossoverLinkResolutionTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS

    private val g = crossoverSpanFloor(d, rP)

    private val eos = MengMagnesium.equationOfState

    /** `C-0205`'s own ceiling, the transverse constant every resolution below is read at. */
    private val shearCeiling = 254.80809548301096

    /** The `21 bp` of interface one honeycomb crossover owns, in nm. */
    private val contact = 21.0 * Gen1Tile.RISE_PER_BASE_PAIR

    private val block = HoneycombBlock(4, 2)

    // ------------------------------------------------- gate 2, the resolution's limiting cases

    @Test
    fun `gate 2 -- the resolution is EXACTLY the transverse constant at an in-plane bond`() {
        assert(resolvedLinkStiffness(1e6, shearCeiling, unitY = 1.0, unitZ = 0.0) == shearCeiling)
        assert(resolvedLinkStiffness(1e6, shearCeiling, unitY = -1.0, unitZ = 0.0) == shearCeiling)
    }

    @Test
    fun `gate 2 -- the resolution is EXACTLY the radial constant along the line of centres`() {
        assert(resolvedLinkStiffness(700.0, shearCeiling, unitY = 0.0, unitZ = 1.0) == 700.0)
    }

    @Test
    fun `gate 2 -- the resolution of two equal constants returns that constant on a unit vector`() {
        val uz = kotlin.math.sqrt(0.75)
        val uy = 0.5
        assert(abs(resolvedLinkStiffness(300.0, 300.0, uy, uz) - 300.0) < 1e-12 * 300.0)
    }

    @Test
    fun `gate 2 -- the resolution refuses a non-positive constant`() {
        assertFailsWith<IllegalArgumentException> { resolvedLinkStiffness(0.0, 1.0, 0.5, 0.5) }
        assertFailsWith<IllegalArgumentException> { resolvedLinkStiffness(1.0, -1.0, 0.5, 0.5) }
    }

    // ------------------------------------------------------------------ gate 1, dimensional

    @Test
    fun `gate 1 -- the resolution is homogeneous of degree one in both of its constants`() {
        val uz = kotlin.math.sqrt(0.75)
        val base = resolvedLinkStiffness(700.0, shearCeiling, 0.5, uz)
        listOf(0.25, 3.0, 11.0).forEach { s ->
            val scaled = resolvedLinkStiffness(700.0 * s, shearCeiling * s, 0.5, uz)
            assert(abs(scaled - s * base) < 1e-12 * s * base)
        }
    }

    @Test
    fun `gate 1 -- the pair radial stiffness is linear in the amplitude and in the contact length`() {
        val base = centralPairRadialStiffness(eos, d, contact)
        val twiceAmplitude = centralPairRadialStiffness(
            eos.copy(repulsionAmplitude = 2.0 * eos.repulsionAmplitude), d, contact
        )
        assert(abs(twiceAmplitude - 2.0 * base) < 1e-9 * base)
        assert(abs(centralPairRadialStiffness(eos, d, 2.0 * contact) - 2.0 * base) < 1e-9 * base)
    }

    // ------------------------------- gate 3, the central-force decomposition, against its source

    @Test
    fun `gate 3 -- the pair radial stiffness is minus the derivative of the pair's own force law`() {
        val h = 1e-6
        val numeric = -(eos.parallelPairForcePerLength(d + h) -
                eos.parallelPairForcePerLength(d - h)) / (2.0 * h)
        val closed = centralPairRadialStiffness(eos, d, 1.0)
        assert(abs(closed - numeric) < 1e-6 * abs(closed))
    }

    @Test
    fun `gate 3 -- the pair transverse term is minus the force over the separation, C-0205's own form`() {
        val f = eos.parallelPairForcePerLength(d)
        val closed = centralPairTransverseStiffness(eos, d, contact)
        assert(abs(closed - centralPairForceTransverseStiffness(f, d, contact)) < 1e-12 * abs(closed))
        // and it is NEGATIVE, which is what C-0205 carried it for
        assert(closed < 0.0)
    }

    @Test
    fun `gate 2 -- the pair radial stiffness is exactly zero where the separation equals the decay length`() {
        val here = centralPairRadialStiffness(eos, eos.decayLength, 1.0)
        assert(abs(here) < 1e-12 * eos.repulsionAmplitude)
    }

    @Test
    fun `F7 -- the pair radial term is positive at the honeycomb's own separation`() {
        assert(centralPairRadialStiffness(eos, d, contact) > 0.0)
    }

    @Test
    fun `F7 -- the honeycomb separation is above the fit's own data floor`() {
        assert(d > MengMagnesium.DATA_FLOOR)
    }

    // ------------------------------------------------- gate 4, the finite-difference convergence

    @Test
    fun `gate 4 -- the closed-form radial stiffness converges under refinement of its own check`() {
        val closed = centralPairRadialStiffness(eos, d, 1.0)
        val coarse = 1e-4
        val fine = 1e-5
        fun numeric(h: Double) = -(eos.parallelPairForcePerLength(d + h) -
                eos.parallelPairForcePerLength(d - h)) / (2.0 * h)
        assert(abs(numeric(fine) - closed) < abs(numeric(coarse) - closed))
    }

    // ------------------------------------------------ gate 5, the corpus's own published numbers

    @Test
    fun `gate 5 -- the implied phosphodiester-step stiffness reproduces C-0194's 548 point 995464`() {
        val implied = impliedPhosphodiesterStepStiffness(
            Gen1Tile.crossoverHingeStiffness(), rP, d, MeasuredBackbone.STEP_SOUTH
        )
        assert(abs(implied - 548.995464) < 1e-6)
    }

    @Test
    fun `gate 5 -- the duplex stretch modulus over the span reproduces 1530 point 48954`() {
        assert(abs(Gen1Tile.DUPLEX_STRETCH_MODULUS / g - 1530.48954) < 1e-5)
    }

    @Test
    fun `P1 -- the resolution reproduces CH-0259's own two published through-thickness readings`() {
        val implied = impliedPhosphodiesterStepStiffness(
            Gen1Tile.crossoverHingeStiffness(), rP, d, MeasuredBackbone.STEP_SOUTH
        )
        val duplex = Gen1Tile.DUPLEX_STRETCH_MODULUS / g
        val uz = kotlin.math.sqrt(0.75)
        val uy = 0.5
        assert(abs(resolvedLinkStiffness(implied, shearCeiling, uy, uz) - 475.448622) < 1e-5)
        assert(abs(resolvedLinkStiffness(duplex, shearCeiling, uy, uz) - 1211.56918) < 1e-4)
    }

    // ---------------------------------------------------- P1 / F4, the lattice's own bond census

    @Test
    fun `F4 -- the recommended block splits 135 in plane and 300 through the thickness`() {
        val lattice = honeycombTiedLatticeAtLinkStiffness(
            HoneycombBlock(10, 6), 116, 1.0, tied = false
        )
        val inPlane = lattice.bonds.filter { it.inPlane }
        val through = lattice.bonds.filter { !it.inPlane }
        assert(lattice.bonds.size == 435)
        assert(inPlane.size == 135)
        assert(through.size == 300)
        assert(inPlane.all { abs(it.unitZ) < 1e-12 })
        assert(through.all { abs(it.unitZ * it.unitZ - 0.75) < 1e-12 })
    }

    // ------------------------------------- F1, the default is the standing object, bit for bit

    @Test
    fun `F1 -- the default per-bond lattice reports the scalar link stiffness by identity`() {
        val lattice = HoneycombGrillage(block, 56, Gen1Tile.FOUNDATION_SECANT)
        assert(lattice.radialLinkStiffness == null)
        lattice.bonds.forEach {
            assert(lattice.linkStiffnessOf(it) == lattice.linkStiffness)
        }
    }

    @Test
    fun `F1 -- the default lattice's stiffness matrix is BIT-IDENTICAL to the standing object`() {
        val standing = HoneycombGrillage(block, 56, Gen1Tile.FOUNDATION_SECANT)
        val defaulted = HoneycombGrillage(
            block, 56, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = null
        )
        assert(standing.degreesOfFreedom == defaulted.degreesOfFreedom)
        var compared = 0
        for (i in 0 until standing.degreesOfFreedom) {
            for (j in maxOf(0, i - standing.bandwidth)..i) {
                assert(standing.stiffnessEntry(i, j) == defaulted.stiffnessEntry(i, j))
                compared++
            }
        }
        assert(compared > 0)
    }

    @Test
    fun `F1 -- the default lattice's load vector is BIT-IDENTICAL over every degree of freedom`() {
        val standing = HoneycombGrillage(block, 56, Gen1Tile.FOUNDATION_SECANT)
        val defaulted = HoneycombGrillage(
            block, 56, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = null
        )
        val a = standing.assembleLoad(uniformPressure(0.01))
        val b = defaulted.assembleLoad(uniformPressure(0.01))
        for (i in 0 until standing.degreesOfFreedom) assert(a[i] == b[i])
    }

    @Test
    fun `F1 -- the crossover SITE SET is identical, which a load vector cannot show`() {
        val standing = HoneycombGrillage(block, 56, Gen1Tile.FOUNDATION_SECANT)
        val resolved = HoneycombGrillage(
            block, 56, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = 700.0
        )
        assert(standing.bonds.map { it.site } == resolved.bonds.map { it.site })
        assert(standing.bonds.map { it.unitZ } == resolved.bonds.map { it.unitZ })
    }

    @Test
    fun `F1 -- a resolution whose two constants are equal reproduces the scalar lattice`() {
        val k = 900.0
        val scalar = HoneycombGrillage(block, 56, Gen1Tile.FOUNDATION_SECANT, linkStiffness = k)
        val resolved = HoneycombGrillage(
            block, 56, Gen1Tile.FOUNDATION_SECANT, linkStiffness = k, radialLinkStiffness = k
        )
        val load = listOf(PointLoad(0.0, 0.0, 1.0))
        val a = scalar.solve(uniformPressure(0.0), load).peakDishing(21)
        val b = resolved.solve(uniformPressure(0.0), load).peakDishing(21)
        assert(abs(a - b) < 1e-10 * abs(a))
    }

    // ------------------------------------------------------ F2, the standing uniform falsifier

    @Test
    fun `F2 -- a uniform pressure on the free per-bond lattice dishes exactly zero`() {
        val lattice = HoneycombGrillage(
            block, 56, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = 700.0
        )
        val solved = lattice.solve(uniformPressure(0.02))
        assert(abs(solved.peakDishing(21)) < 1e-9 * abs(solved.meanDeflection))
    }

    // -------------------------------------------------------------- the element's own behaviour

    @Test
    fun `P3 -- a resolved lattice leaves an in-plane bond at the transverse constant`() {
        val lattice = HoneycombGrillage(
            HoneycombBlock(4, 2), 56, Gen1Tile.FOUNDATION_SECANT,
            linkStiffness = 254.0, radialLinkStiffness = 900.0
        )
        lattice.bonds.filter { it.inPlane }.forEach {
            assert(abs(lattice.linkStiffnessOf(it) - 254.0) < 1e-9 * 254.0)
        }
        lattice.bonds.filter { !it.inPlane }.forEach {
            assert(abs(lattice.linkStiffnessOf(it) - (0.75 * 900.0 + 0.25 * 254.0)) < 1e-9)
        }
    }

    @Test
    fun `P3 -- the link energy is the per-bond sum, and it rises with the radial constant`() {
        val soft = HoneycombGrillage(
            block, 56, Gen1Tile.FOUNDATION_SECANT, linkStiffness = 254.0,
            radialLinkStiffness = 254.0
        )
        val stiff = HoneycombGrillage(
            block, 56, Gen1Tile.FOUNDATION_SECANT, linkStiffness = 254.0,
            radialLinkStiffness = 2540.0
        )
        val field = soft.solve(
            uniformPressure(0.0), listOf(PointLoad(0.0, 0.0, 1.0))
        ).coefficients
        val explicit = 0.5 * soft.bonds.sumOf {
            val gap = soft.linkExtension(field, it)
            soft.linkStiffnessOf(it) * gap * gap
        }
        assert(abs(soft.linkEnergy(field) - explicit) < 1e-12 * explicit)
        assert(stiff.linkEnergy(field) > soft.linkEnergy(field))
    }

    @Test
    fun `P3 -- the radial link stiffness must be positive when it is given`() {
        assertFailsWith<IllegalArgumentException> {
            HoneycombGrillage(block, 56, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            HoneycombGrillage(block, 56, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = -1.0)
        }
    }

    @Test
    fun `P3 -- withoutPrestrain carries the radial link stiffness through`() {
        val ties = honeycombScaffoldTurnTies(
            block, HoneycombGrillage(block, 56, Gen1Tile.FOUNDATION_SECANT).nodesPerBeam, 1, 0.01
        )
        val lattice = HoneycombGrillage(
            block, 56, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = 700.0,
            scaffoldTurnTies = ties
        )
        assert(lattice.withoutPrestrain.radialLinkStiffness == 700.0)
        assert(lattice.withoutPrestrain.linkStiffness == lattice.linkStiffness)
    }

    @Test
    fun `F1 -- the resolved builder at a null radial constant is T-303's builder bit for bit`() {
        val standing = honeycombTiedLatticeAtLinkStiffness(block, 56, 1.0, tied = true)
        val resolved = honeycombTiedLatticeAtResolvedLink(block, 56, 1.0, tied = true)
        assert(standing.degreesOfFreedom == resolved.degreesOfFreedom)
        assert(standing.bonds.map { it.site } == resolved.bonds.map { it.site })
        for (i in 0 until standing.degreesOfFreedom) {
            for (j in maxOf(0, i - standing.bandwidth)..i) {
                assert(standing.stiffnessEntry(i, j) == resolved.stiffnessEntry(i, j))
            }
        }
    }

    @Test
    fun `P2 -- the radial bracket is the connector candidates in PARALLEL with the measured pair term`() {
        val bracket = crossoverRadialLinkBracket(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            phosphateRadius = rP,
            interhelicalDistance = d,
            relaxedStep = MeasuredBackbone.STEP_SOUTH,
            stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS,
            equationOfState = eos,
            contactLength = contact
        )
        // the pair term is ADDED on the radial axis, where C-0205 section 1b's transverse
        // eigenvalue of the same tensor is negative and was reported rather than added
        assert(
            abs(bracket.floor - (bracket.connectorAtImpliedStep + bracket.pairRadial)) <
                    1e-12 * bracket.floor
        )
        assert(
            abs(bracket.ceiling - (bracket.connectorAtDuplexStretch + bracket.pairRadial)) <
                    1e-12 * bracket.ceiling
        )
        assert(bracket.floor > bracket.connectorAtImpliedStep)
        assert(bracket.ceiling > bracket.connectorAtDuplexStretch)
        assert(abs(bracket.floor - 754.005141) < 1e-5)
        assert(abs(bracket.ceiling - 1735.49922) < 1e-4)
    }

    @Test
    fun `P3 -- the resolved builder carries its radial argument into the lattice`() {
        val lattice = honeycombTiedLatticeAtResolvedLink(
            block, 56, 1.0, tied = true,
            transverseLinkStiffness = 254.0, radialLinkStiffness = 900.0
        )
        assert(lattice.radialLinkStiffness == 900.0)
        assert(lattice.linkStiffness == 254.0)
        lattice.bonds.filter { !it.inPlane }.forEach {
            assert(abs(lattice.linkStiffnessOf(it) - (0.75 * 900.0 + 0.25 * 254.0)) < 1e-9)
        }
    }

    @Test
    fun `P3 -- the assembled TIE link carries the resolution, at a node no bond shares`() {
        val lattice = honeycombTiedLatticeAtResolvedLink(
            HoneycombBlock(4, 2), 56, 1.0, tied = true,
            transverseLinkStiffness = 254.0, radialLinkStiffness = 2000.0
        )
        fun dof(node: Int, beam: Int, component: Int) =
            (node * lattice.beamCount + beam) * HoneycombGrillage.DOF_PER_NODE + component
        // the band stores the LOWER triangle only, so the entry is read row >= column
        fun entryAt(a: Int, b: Int) = lattice.stiffnessEntry(maxOf(a, b), minOf(a, b))
        // a tie whose (node, beam pair) no BOND shares, so the entry below is the tie's alone
        val alone = lattice.turnElements.filter { element ->
            lattice.bonds.none {
                it.node == element.node &&
                        it.site.lowerBeam == element.tie.lowerBeam &&
                        it.site.upperBeam == element.tie.upperBeam
            }
        }
        assert(alone.isNotEmpty())
        alone.forEach { element ->
            // the link gradient is (1, armY, -1, armY) over (W_a, Phi_a, W_b, Phi_b), so the
            // (W_a, W_b) entry of the assembled matrix is exactly minus the link stiffness
            val entry = entryAt(
                dof(element.node, element.tie.lowerBeam, HoneycombGrillage.W),
                dof(element.node, element.tie.upperBeam, HoneycombGrillage.W)
            )
            assert(abs(entry + lattice.linkStiffnessOf(element)) < 1e-9 * abs(entry))
        }
        // and at a through-thickness tie that is NOT the unresolved transverse scalar
        val through = alone.filter { !it.inPlane }
        assert(through.isNotEmpty())
        through.forEach { element ->
            val entry = entryAt(
                dof(element.node, element.tie.lowerBeam, HoneycombGrillage.W),
                dof(element.node, element.tie.upperBeam, HoneycombGrillage.W)
            )
            assert(abs(entry + 254.0) > 1.0)
        }
    }

    @Test
    fun `P3 -- a stiffer radial constant stiffens the tile under a point load`() {
        val load = listOf(PointLoad(0.0, 0.0, 1.0))
        val ladder = listOf(300.0, 1000.0, 10000.0).map { radial ->
            HoneycombGrillage(
                block, 56, Gen1Tile.FOUNDATION_SECANT, linkStiffness = 254.0,
                radialLinkStiffness = radial
            ).solve(uniformPressure(0.0), load).peakDishing(21)
        }
        ladder.zipWithNext().forEach { (soft, stiff) -> assert(stiff < soft) }
    }
}
