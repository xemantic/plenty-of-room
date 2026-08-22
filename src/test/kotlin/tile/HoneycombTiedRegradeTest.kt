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

import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.kotlin.test.assert
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-279` — `C-0167`'s 64 coupled cells re-graded on the **tied** honeycomb lattice.
 *
 * Written before `tile/HoneycombTiedRegrade.kt` and watched fail.
 *
 * `C-0175` states that the scaffold-tie extension to `HoneycombGrillage` is strictly additive
 * with an empty default, and asserts it at the level a **free tile** needs: the bond count, the
 * crossover site set, `assembleLoad` and one solved field. This file does **not** inherit that.
 * A coupled cell is built out of four further objects — the **point-load dual**, the sampled
 * **influence bank**, the **station influence matrix** and the graded **dropout sample** — and
 * none of them is what the existing test asserts. `CLAUDE.md`: *verify the claim rather than
 * inheriting it*, and *a shared Kotlin source is a dependency edge*.
 */
class HoneycombTiedRegradeTest {

    private val block = HoneycombBlock(10, 6)
    private val rowBasePairs = 116

    private fun lattice(
        enhancement: Double = 21.1851817,
        prestrainRadians: Double = 0.0,
        tied: Boolean = true
    ) = honeycombTiedLattice(
        block = block,
        rowBasePairs = rowBasePairs,
        enhancement = enhancement,
        tied = tied,
        prestrainRadians = prestrainRadians
    )

    private val edgeX = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    private val edgeY = 10 * HoneycombCrossSectionGeometry.rowPitch()

    private fun grid(columns: Int) = attachmentGrid(columns, 10, edgeX, edgeY)

    private val pressure = uniformPressure(0.02)

    // ------------------------------------------------- gate 1: dimensions and the tie census

    @Test
    fun `gate 1 - the tied lattice carries exactly 59 turn ties and the untied one none`() {
        assert(lattice(tied = true).turnElements.size == 59)
        assert(lattice(tied = false).turnElements.isEmpty())
        // and the bonds are the STAPLE ladder either way: 435 on a 10 x 6 block.
        assert(lattice(tied = true).bonds.size == 435)
        assert(lattice(tied = false).bonds.size == 435)
    }

    @Test
    fun `gate 1 - a tie sits at an axial rim node and nowhere else`() {
        val armed = lattice(tied = true)
        val last = armed.nodesPerBeam - 1
        assert(armed.turnElements.all { it.node == 0 || it.node == last })
        assert(armed.turnElements.count { it.node == last } == 30)
        assert(armed.turnElements.count { it.node == 0 } == 29)
    }

    @Test
    fun `gate 1 - the tie prestrain is refused unless finite`() {
        assertFailsWith<IllegalArgumentException> {
            honeycombTiedLattice(block, rowBasePairs, 1.0, true, Double.NaN)
        }
    }

    // ------------------------------- gate 2 and 3: the empty-tie limit is BIT-IDENTICAL

    /**
     * `F3`. The limiting case the whole pairing rests on, taken on the objects a **coupled**
     * cell is made of rather than on the ones a free tile is.
     */
    @Test
    fun `gate 2 - an untied lattice reproduces C-0167's object bit-for-bit`() {
        val plain = HoneycombGrillage(
            block = block,
            rowBasePairs = rowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = 21.1851817
        )
        val untied = lattice(tied = false)
        // the crossover SITE SET, because a load-vector identity is not a lattice identity
        assert(untied.bonds.map { it.site } == plain.bonds.map { it.site })
        // assembleLoad, which is a fixed-order scatter-add and therefore exactly assertable
        val a = plain.assembleLoad(pressure)
        val b = untied.assembleLoad(pressure)
        for (i in 0 until plain.degreesOfFreedom) assert(a[i] == b[i])
        // the POINT-LOAD DUAL, which C-0175's test does not reach at all
        val da = plain.pointLoadDual(3.0, 5.0, 1.0)
        val db = untied.pointLoadDual(3.0, 5.0, 1.0)
        for (i in 0 until plain.degreesOfFreedom) assert(da[i] == db[i])
        // and the solved field at 1e-10, which is where CLAUDE.md says exactness stops
        assert(abs(plain.solve(pressure).peakDishing(41) -
                untied.solve(pressure).peakDishing(41)) < 1e-10)
    }

    /**
     * `F3`, one level up: the whole **surrogate** — station free deflections, the station
     * influence matrix and the sampled dishing bank — must agree, or the pairing compares two
     * models rather than two states of one.
     */
    @Test
    fun `gate 2 - an untied surrogate reproduces C-0167's surrogate to 1e-10`() {
        val plain = HoneycombGrillage(
            block = block,
            rowBasePairs = rowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = 21.1851817
        )
        val stations = grid(2)
        val here = honeycombTiedSurrogate(lattice(tied = false), stations, pressure, 41)
        val there = honeycombInfluenceSurrogate(plain, stations, pressure, 41)
        val mine = here.solve(equalShareOfMandate(stations.size)).peakDishing
        val theirs = there.solve(equalShareOfMandate(stations.size)).peakDishing
        assert(abs(mine - theirs) < 1e-10 * maxOf(1e-30, abs(theirs)) + 1e-14)
    }

    // ------------------------------- gate 3: the standing falsifier, on the TIED coupled lattice

    /**
     * `F1`. `CLAUDE.md`'s sharpest falsifier — a uniform load on a uniform Winkler foundation
     * dishes exactly zero — re-taken with 59 covalent rim ties added, on the raw lattice and
     * through the **coupled** surrogate at vanishing coupling.
     *
     * The first draft of this test asserted it at the **full** `C-0017` mandate and it duly read
     * `0.0132`, which is not a defect: `C-0058`'s paths run to **ground**, so under a uniform
     * pressure they pull the tile out of its own rigid translation — *an attachment coupling can
     * be a NET DISHING SOURCE*, which is this corpus's own finding and not a broken solver. The
     * falsifier belongs on the **free** field, which is where `C-0167` takes it, and the tied
     * lattice must satisfy it as exactly as the untied one does.
     */
    @Test
    fun `gate 3 - a uniform pressure on the TIED lattice dishes exactly zero`() {
        val armed = lattice(tied = true)
        val magnitude = 0.0666534426
        val field = armed.solve(uniformPressure(magnitude))
        val stroke = field.meanDeflection
        // the free stroke is p/k_f identically, ties or no ties -- a free body on a uniform
        // Winkler foundation translates rigidly whatever its rigidities
        assert(abs(stroke - magnitude / Gen1Tile.FOUNDATION_SECANT) < 1e-9 * stroke)
        assert(field.peakDishing(41) / stroke < 1e-9)
        // and through the coupled surrogate at vanishing coupling, which is C-0167's own form
        val stations = grid(3)
        val surrogate = honeycombTiedSurrogate(armed, stations, uniformPressure(0.03), 21)
        assert(surrogate.solve(List(stations.size) { 1e-9 }).peakDishing < 1e-9)
    }

    /**
     * `CLAUDE.md`: the ties cannot **soften** the block under a fixed load — a Loewner statement
     * `K_tied ⪰ K_untied`, read where it is a statement at all: the deflection **at** a unit
     * point load is `fᵀK⁻¹f`, the strain energy, and it must fall. Peak dishing is a **seminorm**
     * of the field and is bounded by none of this, which is exactly why the study is owed.
     *
     * A uniform pressure is the one load case on which the statement is empty: the mean
     * deflection is `p/k_f` for both lattices, so the two agree to the last ulp and the test
     * would be a test of nothing.
     */
    @Test
    fun `gate 3 - the ties cannot soften the block under a fixed point load`() {
        val s = 4.0
        val y = 5.0
        fun complianceOf(tied: Boolean) = lattice(tied = tied)
            .solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0))).deflection(s, y)
        val plain = complianceOf(tied = false)
        val armed = complianceOf(tied = true)
        assert(plain > 0.0)
        assert(armed < plain)
    }

    // ------------------------------- gate 3: a prestrain is a LOAD, never a stiffness

    /**
     * `C-0104`'s trap, at the surrogate level: an influence function taken on a **prestrained**
     * lattice is that influence plus the prestrain's own response, and the Woodbury compliance
     * then stops being a compliance. [honeycombTiedSurrogate] must take its free field from the
     * prestrained lattice and every influence from `withoutPrestrain`.
     *
     * The probe is the single-path compliance recovered from public quantities alone:
     * `w_free = d + f M`, so `M = (w_free − d)/f`, and `M` may not move with the prestrain.
     */
    @Test
    fun `gate 3 - the prestrain moves the free field and not the compliance`() {
        val station = listOf(2.0 to 3.0)
        fun complianceAt(angle: Double): Pair<Double, Double> {
            val armed = lattice(tied = true, prestrainRadians = angle)
            val surrogate = honeycombTiedSurrogate(armed, station, pressure, 41)
            val response = surrogate.solve(listOf(1.0))
            val free = armed.solve(pressure).deflection(2.0, 3.0)
            val force = response.supportForces[0]
            return free to (free - response.stationDeflections[0]) / force
        }
        val (freeZero, mZero) = complianceAt(0.0)
        val (freeLoaded, mLoaded) = complianceAt(0.05)
        // the compliance is the structure and may not move
        assert(abs(mLoaded - mZero) < 1e-9 * abs(mZero))
        // the free field must move, or the prestrain is not entering as a load at all
        assert(abs(freeLoaded - freeZero) > 1e-9)
    }

    /** A prestrain is a load, so the coupled field is exactly **linear** in it. */
    @Test
    fun `gate 3 - the coupled field is exactly linear in the tie prestrain`() {
        val stations = grid(2)
        val share = equalShareOfMandate(stations.size)
        fun peakAt(angle: Double) = honeycombTiedSurrogate(
            lattice(tied = true, prestrainRadians = angle), stations, pressure, 41
        ).solve(share).peakDishing
        val zero = peakAt(0.0)
        val one = peakAt(0.01)
        val two = peakAt(0.02)
        assert(abs((two - zero) - 2.0 * (one - zero)) < 1e-9 * maxOf(1.0, abs(two - zero)))
    }

    // ------------------------------- gate 5: the two upstream reproductions

    /** `F4` — the tied lattice is the object `C-0175` built: `435 + 59`. */
    @Test
    fun `gate 5 - the tied lattice is C-0175's 435 plus 59`() {
        val armed = lattice(tied = true)
        assert(armed.bonds.size == 435)
        assert(armed.turnElements.size == 59)
        assert(armed.turnElements.count { !it.inPlane } == 50)
        assert(armed.turnElements.count { it.inPlane } == 9)
    }

    /** `F2` — the untied cell the study re-grades is `C-0167`'s cell. */
    @Test
    fun `gate 5 - the untied and tied lattices differ only in the tie set`() {
        val untied = lattice(tied = false)
        val tied = lattice(tied = true)
        assert(untied.degreesOfFreedom == tied.degreesOfFreedom)
        assert(untied.bandwidth == tied.bandwidth)
        assert(untied.nodesPerBeam == tied.nodesPerBeam)
        assert(untied.bonds.map { it.site } == tied.bonds.map { it.site })
    }
}
