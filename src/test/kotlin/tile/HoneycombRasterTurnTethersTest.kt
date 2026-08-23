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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.langevin
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.maximumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.turnLoopTension
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-299` — the raster turn as the entropic **tether** the built precedent's allowance makes it.
 *
 * Written before `tile/HoneycombRasterTurnTethers.kt` and the `HoneycombGrillage` addition, and
 * watched fail.
 *
 * `C-0193` reads the built `10 × 6` block's own strand diagram: the covalent link sits `14 bp`
 * outboard of the duplex end on **each** helix, so what stands between the two rim nodes is
 * `28` nucleotides of single-stranded scaffold and **not** a bond. A freely-jointed chain
 * transmits a **force** and no **moment**, so route B's turn carries neither the dihedral spring
 * nor the covalent slip spring route A's tie does — and it carries a **preload**, because a chain
 * held at any `x > 0` is in tension.
 *
 * No lambda appears inside an `assert(...)`: the power-assert macro rewrites its argument and
 * loses `it`, which is why every predicate below is evaluated into a local first.
 */
class HoneycombRasterTurnTethersTest {

    private val block = HoneycombBlock(10, 6)
    private val rowBasePairs = 116
    private val enhancement = 21.1851817
    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    private val worstSpan =
        maximumTurnPhosphateSpan(d, MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS)
    private val kBT = thermalEnergy(ROOM_TEMPERATURE)

    private fun state(
        nucleotides: Int = 28,
        span: Double = worstSpan,
        kuhn: Double = 2.10,
        contour: Double = 0.65
    ) = freelyJointedTetherState(span, nucleotides, kuhn, contour, kBT)

    private fun untied() = honeycombTiedLattice(
        block = block, rowBasePairs = rowBasePairs, enhancement = enhancement, tied = false
    )

    private fun tethered(
        tetherState: HoneycombTetherState = state(),
        withPreload: Boolean = true
    ) = honeycombTetheredLattice(
        block = block, rowBasePairs = rowBasePairs, enhancement = enhancement,
        state = tetherState, withPreload = withPreload
    )

    /**
     * The well-conditioned probe every same-lattice comparison is taken on: a unit point load at
     * the face centre. A bare uniform pressure leaves a free tile's dishing at its own `1e−11`
     * conditioning noise, so a departure read there measures the solver and not the lattice.
     */
    private fun probeDishing(lattice: HoneycombGrillage): Double =
        lattice.solve(uniformPressure(0.0), listOf(PointLoad(0.0, 0.0, 1.0))).peakDishing(41)

    private fun bareGrillage() = HoneycombGrillage(
        block = block, rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT
    )

    // ------------------------------------------------------- gate 1: dimensions and the census

    @Test
    fun `gate 1 - the tethered lattice carries 59 tethers and no turn ties`() {
        val lattice = tethered()
        assert(lattice.tetherElements.size == 59)
        assert(lattice.turnElements.isEmpty())
        assert(lattice.bonds.size == untied().bonds.size)
    }

    @Test
    fun `gate 1 - the tethers sit on the same 59 sites the ties do`() {
        val lattice = tethered()
        val ties = honeycombScaffoldTurnTies(block, lattice.nodesPerBeam)
            .map { listOf(it.lowerBeam, it.upperBeam, it.node) }
        val tethers = lattice.tetherElements
            .map { listOf(it.tether.lowerBeam, it.tether.upperBeam, it.node) }
        val same = tethers == ties
        assert(same)
    }

    @Test
    fun `gate 1 - the element resolves onto the two grillage gradients as the chain's own decomposition`() {
        val worstNormal = tethered().tetherElements.maxOf {
            abs(
                it.normalStiffness - (it.tether.tangentStiffness * it.unitZ * it.unitZ +
                        it.tether.secantStiffness * it.unitY * it.unitY)
            )
        }
        val worstAxial = tethered().tetherElements.maxOf {
            abs(it.axialStiffness - it.tether.secantStiffness)
        }
        assert(worstNormal < 1e-12)
        assert(worstAxial < 1e-12)
    }

    @Test
    fun `gate 1 - a tether refuses a negative stiffness, a foreign node and a non-adjacent pair`() {
        assertFailsWith<IllegalArgumentException> {
            HoneycombScaffoldTurnTether(0, 1, 0, -1.0, 1.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            HoneycombGrillage(
                block = block, rowBasePairs = rowBasePairs,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                scaffoldTurnTethers = listOf(
                    HoneycombScaffoldTurnTether(0, 1, 9_999, 1.0, 1.0, 0.0)
                )
            ).tetherElements
        }
        assertFailsWith<IllegalArgumentException> {
            HoneycombGrillage(
                block = block, rowBasePairs = rowBasePairs,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                scaffoldTurnTethers = listOf(
                    HoneycombScaffoldTurnTether(0, 8, 0, 1.0, 1.0, 0.0)
                )
            ).tetherElements
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - the Langevin derivative is one third at the origin and matches a difference`() {
        val atZero = abs(langevinDerivative(0.0) - 1.0 / 3.0)
        assert(atZero < 1e-15)
        val worst = listOf(0.05, 0.3, 0.49, 0.51, 1.0, 5.0, 25.0).maxOf { u ->
            val h = 1e-6 * maxOf(1.0, u)
            val numeric = (langevin(u + h) - langevin(u - h)) / (2.0 * h)
            abs(langevinDerivative(u) - numeric) / maxOf(1e-12, abs(numeric))
        }
        assert(worst < 1e-5)
    }

    @Test
    fun `gate 2 - the large-argument branch is the asymptote and joins the exact form smoothly`() {
        // `1/sinh(u)^2` overflows above u ~ 355, so above the guard the derivative IS `1/u^2`.
        // Written as a mutation-test finding: no state this study occupies reaches u > 1.6, so
        // nothing else in this file can see the branch at all.
        assert(langevinDerivative(400.0) == 1.0 / (400.0 * 400.0))
        assert(langevinDerivative(1e6) == 1.0 / 1e12)
        // Continuity is asserted at ONE argument on each side of the guard against the same
        // closed form, never between two different arguments: `1/u^2` itself moves by 1.1e-3
        // between 349.9 and 350.1, which is the whole width of the check and none of its content.
        val exact = langevinDerivative(349.9)
        assert(abs(exact - 1.0 / (349.9 * 349.9)) < 1e-12 / (349.9 * 349.9))
        val asymptote = langevinDerivative(350.1)
        assert(asymptote == 1.0 / (350.1 * 350.1))
    }

    @Test
    fun `gate 2 - an empty tether list leaves the lattice bit-identical`() {
        val bare = untied()
        val withEmpty = HoneycombGrillage(
            block = block, rowBasePairs = rowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = enhancement,
            scaffoldTurnTethers = emptyList()
        )
        val pressure = uniformPressure(0.02)
        val a = bare.assembleLoad(pressure)
        val b = withEmpty.assembleLoad(pressure)
        val loadIsBitIdentical = (0 until a.length).all { a[it] == b[it] }
        assert(loadIsBitIdentical)
        val sameSites = withEmpty.bonds.map { it.site } == bare.bonds.map { it.site }
        assert(sameSites)
        // A well-conditioned load case: under a BARE uniform pressure the dishing of a free tile
        // is its own conditioning noise (`CLAUDE.md`), so the comparison would be a test of
        // nothing. A point load is what the coupling surrogate actually asks the lattice for.
        val da = probeDishing(bare)
        val db = probeDishing(withEmpty)
        assert(da > 1e-6)
        assert(abs(da - db) < 1e-10 * da)
    }

    @Test
    fun `gate 2 - a vanishing tether stiffness and preload reproduce the untied lattice`() {
        val bare = untied()
        val vanishing = honeycombTetheredLattice(
            block = block, rowBasePairs = rowBasePairs, enhancement = enhancement,
            state = state().withStiffness(0.0), withPreload = false
        )
        assert(vanishing.tetherElements.size == 59)
        val pressure = uniformPressure(0.02)
        val a = bare.assembleLoad(pressure)
        val b = vanishing.assembleLoad(pressure)
        val loadIsBitIdentical = (0 until a.length).all { a[it] == b[it] }
        assert(loadIsBitIdentical)
        val da = probeDishing(bare)
        val db = probeDishing(vanishing)
        assert(da > 1e-6)
        assert(abs(da - db) < 1e-10 * da)
    }

    @Test
    fun `gate 2 - a stiffening tether drives its own link residual to zero as one over k`() {
        val pressure = uniformPressure(0.02)
        val residuals = listOf(1e2, 1e3, 1e4).map { stiffness ->
            val lattice = honeycombTetheredLattice(
                block = block, rowBasePairs = rowBasePairs, enhancement = enhancement,
                state = state().withStiffness(stiffness), withPreload = false
            )
            val field = lattice.solve(pressure).coefficients
            lattice.tetherElements.maxOf { abs(lattice.tetherLinkResidual(field, it)) }
        }
        assert(residuals[1] < residuals[0] / 5.0)
        assert(residuals[2] < residuals[1] / 5.0)
    }

    @Test
    fun `gate 2 - the tether state reproduces the corpus's own freely jointed tension`() {
        val worst = listOf(15, 20, 28).flatMap { nucleotides ->
            listOf(2.10, 2.84).flatMap { kuhn ->
                listOf(0.65, 0.70).map { contour ->
                    val mine = state(nucleotides, worstSpan, kuhn, contour)
                    val theirs = turnLoopTension(worstSpan, nucleotides * contour, kuhn, kBT)
                    maxOf(
                        abs(mine.tension - theirs) / theirs,
                        abs(mine.secantStiffness - theirs / worstSpan) / mine.secantStiffness
                    )
                }
            }
        }.max()
        assert(worst < 1e-12)
    }

    @Test
    fun `gate 2 - the tangent stiffness is the derivative of the tension in the span`() {
        val worst = listOf(15, 28).maxOf { nucleotides ->
            val h = 1e-6
            val up = state(nucleotides, worstSpan + h).tension
            val down = state(nucleotides, worstSpan - h).tension
            val numeric = (up - down) / (2.0 * h)
            abs(state(nucleotides).tangentStiffness - numeric) / abs(numeric)
        }
        assert(worst < 1e-6)
    }

    // ------------------------------------------ gate 3: symmetry, conservation and the falsifier

    @Test
    fun `gate 3 - a uniform pressure on the free tethered lattice dishes zero`() {
        val free = HoneycombGrillage(
            block = block, rowBasePairs = rowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = enhancement,
            scaffoldTurnTethers = honeycombScaffoldTurnTethers(
                block, bareGrillage().nodesPerBeam, state(), withPreload = false
            )
        )
        val solution = free.solve(uniformPressure(0.02))
        assert(solution.peakDishing(81) < 1e-9)
        val stroke = 0.02 / Gen1Tile.FOUNDATION_SECANT
        assert(abs(solution.meanDeflection - stroke) < 1e-9 * stroke)
    }

    @Test
    fun `gate 3 - a rigid roll stores exactly zero tether energy and does zero preload work`() {
        val lattice = tethered()
        val field = F64Array(lattice.degreesOfFreedom)
        val alpha = 1e-3
        for (node in 0 until lattice.nodesPerBeam) {
            for (beam in 0 until lattice.beamCount) {
                val base = (node * lattice.beamCount + beam) * HoneycombGrillage.DOF_PER_NODE
                field[base + HoneycombGrillage.W] = alpha * lattice.beamY[beam]
                field[base + HoneycombGrillage.PHI] = alpha
            }
        }
        assert(lattice.tetherEnergy(field) < 1e-18)
        val preload = lattice.tetherPreloadLoad()
        var work = 0.0
        for (i in 0 until lattice.degreesOfFreedom) work += preload[i] * field[i]
        assert(abs(work) < 1e-12)
    }

    @Test
    fun `gate 3 - the preload alone shortens every chain it can reach`() {
        val lattice = tethered()
        val load = lattice.tetherPreloadLoad()
        val someLoad = (0 until load.length).any { load[it] != 0.0 }
        assert(someLoad)
        val field = lattice.solve(uniformPressure(0.0)).coefficients
        val throughThickness = lattice.tetherElements.filter { !it.inPlane }
        assert(throughThickness.size == 50)
        assert(lattice.tetherElements.count { it.inPlane } == 9)
        val worstExtension = throughThickness.maxOf { lattice.tetherChainExtension(field, it) }
        assert(worstExtension < 0.0)
        val inPlaneLoad = lattice.tetherElements.filter { it.inPlane }
            .maxOf { abs(it.tether.tension * it.unitZ) }
        assert(inPlaneLoad < 1e-18)
    }

    @Test
    fun `gate 3 - a lattice of only in-plane tethers applies exactly no preload`() {
        // The chain's pull at an in-plane turn is entirely along `y`, a direction this lattice
        // has no coordinate for, so its preload must be identically zero. Taken on a lattice of
        // ONLY in-plane tethers, because on the full one the through-thickness turns' own load
        // hides a defect at the in-plane ones.
        val turns = honeycombRasterTurnList(block)
        val all = honeycombScaffoldTurnTethers(block, tethered().nodesPerBeam, state())
        val inPlaneOnly = all.filterIndexed { index, _ -> turns[index].inPlane }
        assert(inPlaneOnly.size == 9)
        val lattice = HoneycombGrillage(
            block = block, rowBasePairs = rowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = enhancement,
            scaffoldTurnTethers = inPlaneOnly
        )
        assert(lattice.tetherElements.size == 9)
        val worstTension = lattice.tetherElements.maxOf { it.tether.tension }
        assert(worstTension > 0.1)
        val load = lattice.tetherPreloadLoad()
        val worstEntry = (0 until load.length).maxOf { abs(load[it]) }
        assert(worstEntry == 0.0)
    }

    @Test
    fun `gate 3 - EACH tether's own preload is annihilated by a rigid roll`() {
        // The whole-lattice statement above is a SUM, and the honeycomb's two through-thickness
        // azimuths carry opposite `unitY`, so a per-element sign defect cancels in it exactly.
        // Found by a surviving mutation; asserted here one element at a time.
        val turns = honeycombRasterTurnList(block)
        val all = honeycombScaffoldTurnTethers(block, tethered().nodesPerBeam, state())
        val alpha = 1e-3
        var worst = 0.0
        var probes = 0
        all.indices.forEach { index ->
            if (turns[index].inPlane) return@forEach
            val lattice = HoneycombGrillage(
                block = block, rowBasePairs = rowBasePairs,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                hingeStiffnessEnhancement = enhancement,
                scaffoldTurnTethers = listOf(all[index])
            )
            val load = lattice.tetherPreloadLoad()
            var work = 0.0
            for (node in 0 until lattice.nodesPerBeam) {
                for (beam in 0 until lattice.beamCount) {
                    val base = (node * lattice.beamCount + beam) * HoneycombGrillage.DOF_PER_NODE
                    work += load[base + HoneycombGrillage.W] * alpha * lattice.beamY[beam]
                    work += load[base + HoneycombGrillage.PHI] * alpha
                }
            }
            worst = maxOf(worst, abs(work))
            probes++
        }
        assert(probes == 50)
        assert(worst < 1e-15)
    }

    @Test
    fun `gate 3 - the preload is a load and changes no entry of the stiffness matrix`() {
        val loaded = tethered(withPreload = true)
        val unloaded = tethered(withPreload = false)
        val step = maxOf(1, loaded.degreesOfFreedom / 400)
        var worst = 0.0
        var probes = 0
        for (i in 0 until loaded.degreesOfFreedom step step) {
            for (j in maxOf(0, i - loaded.bandwidth)..i step 37) {
                worst = maxOf(worst, abs(loaded.stiffnessEntry(i, j) - unloaded.stiffnessEntry(i, j)))
                probes++
            }
        }
        assert(probes > 100)
        assert(worst == 0.0)
    }

    @Test
    fun `gate 3 - the field is exactly linear in the tension`() {
        fun peakAt(scale: Double): Double {
            val base = state()
            return honeycombTetheredLattice(
                block = block, rowBasePairs = rowBasePairs, enhancement = enhancement,
                state = base.copy(tension = base.tension * scale), withPreload = true
            ).solve(uniformPressure(0.0)).peakDishing(41)
        }
        val one = peakAt(1.0)
        val three = peakAt(3.0)
        assert(one > 0.0)
        assert(abs(three - 3.0 * one) < 1e-9 * three)
    }

    @Test
    fun `gate 3 - the tethered lattice conserves force globally`() {
        val lattice = tethered()
        val solution = lattice.solve(uniformPressure(0.02))
        val departure = abs(solution.foundationForce - solution.appliedForce) /
                abs(solution.appliedForce)
        assert(departure < 1e-9)
    }

    @Test
    fun `gate 3 - withoutPrestrain removes the preload and nothing else`() {
        val lattice = tethered(withPreload = true)
        val bare = lattice.withoutPrestrain
        assert(bare.tetherElements.size == lattice.tetherElements.size)
        val worstTension = bare.tetherElements.maxOf { abs(it.tether.tension) }
        assert(worstTension == 0.0)
        val load = bare.assembleLoad(uniformPressure(0.0))
        val quiet = (0 until load.length).all { load[it] == 0.0 }
        assert(quiet)
        val step = maxOf(1, lattice.degreesOfFreedom / 400)
        val worst = (0 until lattice.degreesOfFreedom step step).maxOf {
            abs(bare.stiffnessEntry(it, it) - lattice.stiffnessEntry(it, it))
        }
        assert(worst == 0.0)
    }

    @Test
    fun `gate 3 - the two rim chains land on the two rims and swap with the axial sign`() {
        val low = state(24)
        val high = state(32)
        val lattice = honeycombTetheredLattice(
            block = block, rowBasePairs = rowBasePairs, enhancement = enhancement,
            lowRimState = low, highRimState = high
        )
        val nodes = lattice.nodesPerBeam
        val turns = honeycombRasterTurnList(block)
        val wrong = lattice.tetherElements.indices.count {
            val expected = if (turns[it].atHighEnd) high else low
            lattice.tetherElements[it].tether.tension != expected.tension ||
                    lattice.tetherElements[it].node != (if (turns[it].atHighEnd) nodes - 1 else 0)
        }
        assert(wrong == 0)
        // the two halves are not equal, so the assignment is not vacuous
        assert(abs(low.tension - high.tension) > 0.1)
        val highCount = lattice.tetherElements.count { it.node == nodes - 1 }
        assert(highCount == 30)
        // reversing the axial datum exchanges the two rims, which is the free convention
        val reversed = honeycombScaffoldTurnTethers(block, nodes, low, high, firstAxialSign = -1)
        val exchanged = reversed.indices.count {
            reversed[it].tension == lattice.tetherElements[it].tether.tension
        }
        assert(exchanged == 0)
    }

    @Test
    fun `gate 3 - equal rim chains reproduce the single-chain overload exactly`() {
        val one = honeycombScaffoldTurnTethers(block, tethered().nodesPerBeam, state())
        val two = honeycombScaffoldTurnTethers(
            block, tethered().nodesPerBeam, state(), state()
        )
        val same = one == two
        assert(same)
    }

    // -------------------------------------------------------- gate 5: the literature comparand

    @Test
    fun `gate 5 - the fifteen nucleotide turn reproduces C-0193's own published tension band`() {
        val stiff = state(15, worstSpan, 2.10, 0.65).tension
        val soft = state(15, worstSpan, 2.84, 0.70).tension
        assert(abs(stiff - 3.03288672) < 5e-8)
        assert(abs(soft - 2.03800431) < 5e-8)
    }

    @Test
    fun `gate 5 - the built turn is far softer than every element it replaces`() {
        val built = state(28)
        val hingeOnTheRimArm = Gen1Tile.crossoverHingeStiffness() / ((d / 2.0) * (d / 2.0))
        assert(built.tangentStiffness < 0.1 * hingeOnTheRimArm)
        assert(built.tangentStiffness < 1e-3 * HoneycombGrillage.RIGID_LINK_STIFFNESS)
        assert(built.tangentStiffness < 0.05 * Gen1Tile.crossoverInPlaneStiffness())
    }
}
