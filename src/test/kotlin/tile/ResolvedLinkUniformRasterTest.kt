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
import com.xemantic.nano.plentyofroom.electrostatics.MengMagnesium
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-315` — route B's uniform raster at the **resolved per-bond** link.
 *
 * Written before `tile/ResolvedLinkUniformRaster.kt` exists, and watched fail.
 *
 * The gates each test names are `T-315`'s own `P1`–`P5` and `F1`–`F10`, declared in
 * `gpd/tasks/T-315-the-uniform-raster-at-the-resolved-link.md` before this file was written.
 */
class ResolvedLinkUniformRasterTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS

    private val kBT = thermalEnergy(ROOM_TEMPERATURE)

    private val block = HoneycombBlock(10, 6)

    /** `C-0205`'s shear ceiling, the TRANSVERSE constant every resolved rung is read at. */
    private val shearCeiling = 254.80809548301096

    /** `C-0208`'s radial bracket floor and ceiling, in pN/nm. */
    private val radialFloor = 754.005141

    private val radialCeiling = 1735.49922

    /** Route B's three uniform paired rows, `scaffoldNucleotides / 60 − 28`. */
    private val rows = listOf(92, 98, 106)

    private fun tethersAt(pairedRowBasePairs: Int, phase: Int = 0) = UniformRasterTethers(
        block = block,
        pairedRowBasePairs = pairedRowBasePairs,
        interhelicalDistance = d,
        phosphateRadius = rP,
        classZeroResidue = phase,
        lowRimNucleotides = 28,
        highRimNucleotides = 28,
        kuhnLength = 2.10,
        contourPerNucleotide = 0.65,
        thermalEnergy = kBT
    )

    private val penalty = ResolvedLinkRung(
        name = "the penalty",
        ground = "HoneycombGrillage.RIGID_LINK_STIFFNESS, C-0207's own reading",
        transverseLinkStiffness = HoneycombGrillage.RIGID_LINK_STIFFNESS,
        radialLinkStiffness = null
    )

    private val resolvedFloor = ResolvedLinkRung(
        name = "the resolved floor",
        ground = "C-0208's radial bracket floor",
        transverseLinkStiffness = shearCeiling,
        radialLinkStiffness = radialFloor
    )

    private val resolvedCeiling = ResolvedLinkRung(
        name = "the resolved ceiling",
        ground = "C-0208's radial bracket ceiling",
        transverseLinkStiffness = shearCeiling,
        radialLinkStiffness = radialCeiling
    )

    private val uniformShear = ResolvedLinkRung(
        name = "the uniform shear ceiling",
        ground = "C-0208's own first radial rung -- the softest defensible lattice",
        transverseLinkStiffness = shearCeiling,
        radialLinkStiffness = shearCeiling
    )

    // --------------------------------------------------------------- gate 1, dimensional

    @Test
    fun `gate 1 -- a rung's two readings are stiffnesses in pN per nm and are ordered by the radial constant`() {
        assert(resolvedFloor.inPlaneLinkStiffness == shearCeiling)
        assert(resolvedFloor.throughThicknessLinkStiffness > resolvedFloor.inPlaneLinkStiffness)
        assert(resolvedCeiling.throughThicknessLinkStiffness > resolvedFloor.throughThicknessLinkStiffness)
        // C-0208's own two published readings of the through-thickness link
        assert(abs(resolvedFloor.throughThicknessLinkStiffness - 629.20588) < 1e-5)
        assert(abs(resolvedCeiling.throughThicknessLinkStiffness - 1365.32644) < 1e-4)
    }

    @Test
    fun `gate 1 -- the through-thickness reading is three quarters radial and one quarter transverse`() {
        val expected = 0.75 * radialFloor + 0.25 * shearCeiling
        assert(abs(resolvedFloor.throughThicknessLinkStiffness - expected) < 1e-9 * expected)
    }

    // --------------------------------------------------------------- gate 2, limiting cases

    @Test
    fun `gate 2 -- the PENALTY rung's two readings are one number, by identity`() {
        assert(penalty.inPlaneLinkStiffness == HoneycombGrillage.RIGID_LINK_STIFFNESS)
        assert(penalty.throughThicknessLinkStiffness == HoneycombGrillage.RIGID_LINK_STIFFNESS)
        assert(penalty.isSingleScalar)
    }

    /**
     * The `null` default returns the scalar **by identity**, and `k = 1` is where that shows.
     *
     * `unitY² + unitZ²` is not exactly one in floating point — `C-0208`'s own reason for
     * branching `linkStiffnessAt` — but at most constants the product rounds back, so the branch
     * has no observable behaviour there. At `k = 1` it does, which is what makes it testable.
     */
    @Test
    fun `gate 2 -- the null default is returned by IDENTITY, and at k = 1 that is the only way`() {
        val standing = ResolvedLinkRung("probe", "the unit link, no radial", 1.0, null)
        assert(standing.inPlaneLinkStiffness == 1.0)
        assert(standing.throughThicknessLinkStiffness == 1.0)
        val resolved = ResolvedLinkRung("probe", "the unit link, radial = transverse", 1.0, 1.0)
        assert(resolved.throughThicknessLinkStiffness != 1.0)
        assert(abs(resolved.throughThicknessLinkStiffness - 1.0) < 1e-15)
    }

    @Test
    fun `gate 2 -- a rung at radial equal to transverse is a UNIFORM link to 1e-12 and NOT bit for bit`() {
        // resolvedLinkStiffness(k, k, unitY, unitZ) is k(unitY^2 + unitZ^2), and that sum is not
        // exactly one in floating point -- C-0208's own reason for branching linkStiffnessAt.
        assert(uniformShear.inPlaneLinkStiffness == shearCeiling)
        assert(!uniformShear.isSingleScalar)
        val lattice = tethersAt(92).latticeAtRung(uniformShear, enhancement = 1.0)
        lattice.bonds.forEach {
            assert(abs(lattice.linkStiffnessOf(it) - shearCeiling) < 1e-12 * shearCeiling)
        }
    }

    /**
     * The rung's own guards, asserted on the MESSAGE and not only on the throw.
     *
     * `resolvedLinkStiffness` carries the same two requirements, so a rung that refused nothing
     * would still throw wherever it evaluates one — `C-0207` §8's *a guard whose only observable
     * behaviour is duplicated downstream is a guard no mutation of it can reach*, met again. The
     * two messages differ by one word (`radialLinkStiffness` against `radialStiffness`), and the
     * `null`-radial case reaches no downstream call at all.
     */
    @Test
    fun `gate 2 -- a rung refuses a non-positive or non-finite constant, at its OWN guard`() {
        assert(
            assertFailsWith<IllegalArgumentException> {
                ResolvedLinkRung("bad", "zero transverse", 0.0, 700.0)
            }.message!!.contains("transverseLinkStiffness must be positive and finite")
        )
        assert(
            assertFailsWith<IllegalArgumentException> {
                ResolvedLinkRung("bad", "infinite transverse", Double.POSITIVE_INFINITY, 700.0)
            }.message!!.contains("transverseLinkStiffness must be positive and finite")
        )
        // the null-radial rung evaluates no resolution at all, so only the rung's guard can refuse
        assertFailsWith<IllegalArgumentException> {
            ResolvedLinkRung("bad", "zero transverse, no radial", 0.0, null)
        }
        assert(
            assertFailsWith<IllegalArgumentException> {
                ResolvedLinkRung("bad", "negative radial", 254.0, -1.0)
            }.message!!.contains("radialLinkStiffness must be positive and finite")
        )
        assert(
            assertFailsWith<IllegalArgumentException> {
                ResolvedLinkRung("bad", "NaN radial", 254.0, Double.NaN)
            }.message!!.contains("radialLinkStiffness must be positive and finite")
        )
    }

    @Test
    fun `gate 2 -- a stiffness override of zero leaves the element list inert at a RESOLVED link`() {
        val subject = tethersAt(98)
        val tethered = subject.latticeAtRung(
            resolvedFloor, enhancement = 1.0, withPreload = false, stiffness = 0.0
        )
        val bare = honeycombTiedLatticeAtResolvedLink(
            block, 98, 1.0, tied = false,
            transverseLinkStiffness = resolvedFloor.transverseLinkStiffness,
            radialLinkStiffness = resolvedFloor.radialLinkStiffness
        )
        assert(tethered.degreesOfFreedom == bare.degreesOfFreedom)
        for (i in 0 until bare.degreesOfFreedom) {
            for (j in maxOf(0, i - bare.bandwidth)..i) {
                assert(tethered.stiffnessEntry(i, j) == bare.stiffnessEntry(i, j))
            }
        }
    }

    @Test
    fun `gate 2 -- the builder propagates its subdivision count`() {
        val one = tethersAt(92).latticeAtRung(resolvedFloor, enhancement = 1.0)
        val two = tethersAt(92).latticeAtRung(resolvedFloor, enhancement = 1.0, subdivisions = 2)
        assert(two.nodesPerBeam > one.nodesPerBeam)
        assert(two.subdivisions == 2)
    }

    @Test
    fun `gate 2 -- the builder carries its rung into the lattice`() {
        val lattice = tethersAt(106).latticeAtRung(resolvedCeiling, enhancement = 1.0)
        assert(lattice.linkStiffness == shearCeiling)
        assert(lattice.radialLinkStiffness == radialCeiling)
        val penaltyLattice = tethersAt(106).latticeAtRung(penalty, enhancement = 1.0)
        assert(penaltyLattice.radialLinkStiffness == null)
    }

    // ------------------------------------------------- F3, the default lattice is bit-identical

    @Test
    fun `F3 -- the penalty rung is UniformRasterTethers own lattice bit for bit on assembleLoad`() {
        rows.forEach { row ->
            val subject = tethersAt(row)
            val standing = subject.lattice(enhancement = 1.0)
            val resolved = subject.latticeAtRung(penalty, enhancement = 1.0)
            assert(standing.degreesOfFreedom == resolved.degreesOfFreedom)
            val pressure = uniformPressure(0.05)
            val a = standing.assembleLoad(pressure)
            val b = resolved.assembleLoad(pressure)
            for (i in 0 until standing.degreesOfFreedom) {
                assert(a[i] == b[i])
            }
        }
    }

    @Test
    fun `F3 -- and the crossover SITE SET beside it, at all three row lengths`() {
        rows.forEach { row ->
            val subject = tethersAt(row)
            val standing = subject.lattice(enhancement = 1.0)
            val resolved = subject.latticeAtRung(penalty, enhancement = 1.0)
            assert(standing.bonds.map { it.site } == resolved.bonds.map { it.site })
            assert(standing.bonds.size == resolved.bonds.size)
        }
    }

    @Test
    fun `F3 -- and every band entry of the assembled stiffness matrix, at all three row lengths`() {
        rows.forEach { row ->
            val subject = tethersAt(row)
            val standing = subject.lattice(enhancement = 1.0)
            val resolved = subject.latticeAtRung(penalty, enhancement = 1.0)
            for (i in 0 until standing.degreesOfFreedom) {
                for (j in maxOf(0, i - standing.bandwidth)..i) {
                    assert(standing.stiffnessEntry(i, j) == resolved.stiffnessEntry(i, j))
                }
            }
        }
    }

    // ------------------------------------------------------------------- F4, the bond census

    @Test
    fun `F4 -- an in-plane bond reads the transverse constant and unitZ is EXACTLY zero`() {
        val lattice = tethersAt(92).latticeAtRung(resolvedCeiling, enhancement = 1.0)
        val census = ResolvedLinkBondCensus(lattice, resolvedCeiling)
        assert(census.inPlaneBonds > 0)
        assert(census.meanSquaredUnitZInPlane == 0.0)
        assert(census.worstInPlaneDeparture < 1e-9)
    }

    @Test
    fun `F4 -- a through-thickness bond sits at three quarters, and the census is two-valued`() {
        val lattice = tethersAt(92).latticeAtRung(resolvedCeiling, enhancement = 1.0)
        val census = ResolvedLinkBondCensus(lattice, resolvedCeiling)
        assert(census.throughThicknessBonds > 0)
        assert(abs(census.meanSquaredUnitZThroughThickness - 0.75) < 1e-12)
        assert(census.worstThroughThicknessDeparture < 1e-9)
        assert(census.distinctLinkStiffnessCount == 2)
        assert(census.totalBonds == census.inPlaneBonds + census.throughThicknessBonds)
    }

    @Test
    fun `F4 -- the PENALTY rung's census is ONE-valued, at every row length`() {
        rows.forEach { row ->
            val lattice = tethersAt(row).latticeAtRung(penalty, enhancement = 1.0)
            val census = ResolvedLinkBondCensus(lattice, penalty)
            assert(census.distinctLinkStiffnessCount == 1)
            assert(census.worstInPlaneDeparture == 0.0)
            assert(census.worstThroughThicknessDeparture == 0.0)
        }
    }

    @Test
    fun `F4 -- the uniform widths carry FEWER bonds than the 116 bp block's 435`() {
        // the crossover planes are every 7 bp, so a shorter row carries fewer of them -- the
        // census C-0208 took at 116 bp does not transfer to 92 / 98 / 106 bp
        val atBlockExtent = ResolvedLinkBondCensus(
            honeycombTiedLatticeAtResolvedLink(block, 116, 1.0, tied = false), penalty
        )
        assert(atBlockExtent.totalBonds == 435)
        rows.forEach { row ->
            val census = ResolvedLinkBondCensus(tethersAt(row).latticeAtRung(penalty, 1.0), penalty)
            assert(census.totalBonds < 435)
        }
    }

    // ------------------------------------------------- gate 3, the standing falsifiers

    @Test
    fun `F1 -- a uniform pressure on the free tethered lattice at the RESOLVED link dishes zero`() {
        rows.forEach { row ->
            val lattice = tethersAt(row).latticeAtRung(
                resolvedFloor, enhancement = 1.0, withPreload = false
            )
            val field = lattice.solve(uniformPressure(0.05))
            assert(abs(field.peakDishing(81) / field.meanDeflection) < 1e-9)
        }
    }

    @Test
    fun `F10 -- the link resolution does not reach the chain, at any rung`() {
        val subject = tethersAt(98)
        val nodes = subject.latticeAtRung(penalty, enhancement = 1.0).nodesPerBeam
        val reference = subject.elements(nodes)
        listOf(resolvedFloor, resolvedCeiling, uniformShear).forEach { rung ->
            val lattice = subject.latticeAtRung(rung, enhancement = 1.0)
            assert(lattice.nodesPerBeam == nodes)
            val here = subject.elements(lattice.nodesPerBeam)
            assert(here.size == reference.size)
            here.indices.forEach { k ->
                assert(here[k].secantStiffness == reference[k].secantStiffness)
                assert(here[k].tangentStiffness == reference[k].tangentStiffness)
                assert(here[k].tension == reference[k].tension)
                assert(here[k].node == reference[k].node)
            }
        }
    }

    @Test
    fun `gate 3 -- the resolved lattice is stiffer in its links than the uniform shear one, bond by bond`() {
        val soft = tethersAt(106).latticeAtRung(uniformShear, enhancement = 1.0)
        val stiff = tethersAt(106).latticeAtRung(resolvedFloor, enhancement = 1.0)
        assert(soft.bonds.size == stiff.bonds.size)
        soft.bonds.indices.forEach { k ->
            assert(
                stiff.linkStiffnessOf(stiff.bonds[k]) >
                        soft.linkStiffnessOf(soft.bonds[k]) - 1e-9 * shearCeiling
            )
        }
        assert(stiff.bonds.any { stiff.linkStiffnessOf(it) > 2.0 * shearCeiling })
    }

    // ------------------------------------------------- gate 5, the constants are the corpus's own

    @Test
    fun `gate 5 -- the two constants this task is written on are the corpus's own functions`() {
        val ceiling = crossoverLinkStiffnessBracket(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            phosphateRadius = rP,
            interhelicalDistance = d,
            thermalEnergy = kBT,
            softestPersistenceLength = 1.34 / 2.0,
            stiffestPersistenceLength = 2.84 / 2.0
        ).ceiling
        assert(abs(ceiling - shearCeiling) < 1e-9)
        val radial = crossoverRadialLinkBracket(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            phosphateRadius = rP,
            interhelicalDistance = d,
            relaxedStep = MeasuredBackbone.STEP_SOUTH,
            stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS,
            equationOfState = MengMagnesium.equationOfState,
            contactLength = 21.0 * Gen1Tile.RISE_PER_BASE_PAIR
        )
        assert(abs(radial.floor - radialFloor) < 1e-5)
        assert(abs(radial.ceiling - radialCeiling) < 1e-4)
    }
}
