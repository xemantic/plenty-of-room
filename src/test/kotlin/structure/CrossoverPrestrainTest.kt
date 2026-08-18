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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-172` — Rothemund's *"crossovers in tension"* is a **prestrain**, and `C-0009`'s lattice had
 * no term for one.
 *
 * The term is an **initial dihedral angle** `θ₀` at a crossover: the hinge stores
 * `½ k_θ (Δφ − θ₀)²` rather than `½ k_θ Δφ²`, so it enters the assembled system as a fixed
 * **load vector** `± k_θ θ₀` on the two roll degrees of freedom and changes no stiffness at all.
 * That is what makes the whole `θ₀` axis one extra solve.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class CrossoverPrestrainTest {

    private val sheet: OrigamiSheet =
        origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

    private fun lattice(
        prestrains: Map<CrossoverSite, Double> = emptyMap(),
        foundation: Double = Gen1Tile.FOUNDATION_SECANT
    ) = OrigamiGrillage(
        sheet = sheet,
        lengthX = 40.0,
        beamCount = 5,
        foundationStiffness = foundation,
        columns = CrossoverLayout.centred(4, sheet.crossoverSpacing / 2.0),
        crossoverPrestrains = prestrains
    )

    private val uniform = uniformPressure(0.05)

    /**
     * A uniform pressure alone dishes only its own conditioning noise (~`1e-11` nm), and
     * `CLAUDE.md` records that a `Double` is not reproducible across two calls in one JVM — so a
     * bit-identity gate must be read on a WELL-CONDITIONED field. One off-centre point load makes
     * the dishing `O(1e-3)` nm, where identity is a statement about the code path and not noise.
     */
    private val point = listOf(PointLoad(5.0, 1.0, 10.0))

    // -------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 — a prestrain couple is a stiffness times an angle`() {
        val hinge = Gen1Tile.crossoverHingeStiffness()
        // pN·nm/rad × rad = pN·nm
        assert(hingePrestrainCouple(0.1, hinge).isCloseTo(0.1 * hinge))
        assert(hingePrestrainCouple(0.0, hinge).isCloseTo(0.0))
        // and it is odd in the angle, because a moment has a sign
        assert(hingePrestrainCouple(-0.1, hinge).isCloseTo(-hingePrestrainCouple(0.1, hinge)))
    }

    @Test
    fun `gate 1 — a register offset in base pairs becomes an angle in radians`() {
        // 16 bp at 10.5 bp per turn against the square lattice's 32-3 bp per turn: CLAUDE.md's
        // 8.571 degrees, derived here rather than quoted
        val offset = registerPrestrain(16.0, designTwistPerBase = 360.0 / (32.0 / 3.0), naturalTwistPerBase = 360.0 / 10.5)
        assert((offset * 180.0 / PI).isCloseTo(8.5714286, relativeTolerance = 1e-6))
        // and the 8 bp out-of-plane site is exactly half of it
        val half = registerPrestrain(8.0, 360.0 / (32.0 / 3.0), 360.0 / 10.5)
        assert((offset / half).isCloseTo(2.0))
    }

    @Test
    fun `gate 1 — an unphysical prestrain throws rather than returning a number`() {
        assertFailsWith<IllegalArgumentException> { hingePrestrainCouple(0.1, -1.0) }
        assertFailsWith<IllegalArgumentException> { hingePrestrainCouple(Double.NaN, 1.0) }
        assertFailsWith<IllegalArgumentException> {
            lattice(mapOf(CrossoverSite(0, 0) to Double.POSITIVE_INFINITY))
        }
    }

    // -------------------------------------------------------------- gate 2: limiting cases

    /**
     * The prestrain enters the **load vector** and nothing else, so that is where bit-identity is
     * a statement about the code path. Two identical *solves* of this lattice already differ by
     * ~4 ulp in one JVM — `CLAUDE.md`'s JIT-recompiled reduction — so a `==` on a solved field
     * would be a test of the tiering compiler, not of the term.
     */
    private fun assertSameLoad(a: OrigamiGrillage, b: OrigamiGrillage) {
        val left = a.assembleLoad(uniform, point)
        val right = b.assembleLoad(uniform, point)
        assert(left.length == right.length)
        for (i in 0 until left.length) assert(left[i] == right[i])
    }

    @Test
    fun `gate 2 — an empty prestrain map is bit-identical to the unmodified lattice`() {
        val bare = lattice()
        val declared = lattice(emptyMap())
        assertSameLoad(bare, declared)
        val a = bare.solve(uniform, point)
        val b = declared.solve(uniform, point)
        assert(a.peakDishing(41).isCloseTo(b.peakDishing(41), relativeTolerance = 1e-10))
        assert(a.peakDeflection(41).isCloseTo(b.peakDeflection(41), relativeTolerance = 1e-10))
    }

    @Test
    fun `gate 2 — an all-zero prestrain map is bit-identical to no map at all`() {
        val bare = lattice()
        val zeroed = lattice(bare.crossoverSites.associateWith { 0.0 })
        assertSameLoad(bare, zeroed)
    }

    @Test
    fun `gate 2 — a prestrain on a crossover whose hinge is dead does nothing`() {
        // k_θ = 0 makes the couple k_θ θ₀ zero, so the load vector is untouched
        val site = CrossoverSite(0, 0)
        val soft = OrigamiGrillage(
            sheet = sheet, lengthX = 40.0, beamCount = 5,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(4, sheet.crossoverSpacing / 2.0),
            softenedCrossovers = mapOf(site to CrossoverSoftening.ofHinge(0.0))
        )
        val strained = OrigamiGrillage(
            sheet = sheet, lengthX = 40.0, beamCount = 5,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(4, sheet.crossoverSpacing / 2.0),
            softenedCrossovers = mapOf(site to CrossoverSoftening.ofHinge(0.0)),
            crossoverPrestrains = mapOf(site to 0.3)
        )
        assertSameLoad(soft, strained)
    }

    // ------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 — a prestrain enters as a LOAD, so the response superposes exactly`() {
        val sites = listOf(CrossoverSite(0, 0), CrossoverSite(1, 1))
        val angle = 0.05
        val strained = lattice(sites.associateWith { angle })
        val bare = lattice()
        val loadOnly = bare.solve(uniform)
        val prestrainOnly = strained.solve(uniformPressure(0.0))
        val both = strained.solve(uniform)
        // exact superposition of the two FIELDS, sampled at nine interior points
        listOf(-15.0, 0.0, 15.0).forEach { x ->
            listOf(-2.0, 0.0, 2.0).forEach { y ->
                assert(
                    both.deflection(x, y).isCloseTo(
                        loadOnly.deflection(x, y) + prestrainOnly.deflection(x, y),
                        relativeTolerance = 1e-9
                    )
                )
            }
        }
    }

    @Test
    fun `gate 3 — a prestrain-only response is exactly odd in the prestrain`() {
        val sites = listOf(CrossoverSite(0, 0), CrossoverSite(1, 1))
        val positive = lattice(sites.associateWith { 0.05 }).solve(uniformPressure(0.0))
        val negative = lattice(sites.associateWith { -0.05 }).solve(uniformPressure(0.0))
        listOf(-15.0, 0.0, 15.0).forEach { x ->
            assert(
                positive.deflection(x, 0.0)
                    .isCloseTo(-negative.deflection(x, 0.0), relativeTolerance = 1e-9)
            )
        }
        // and therefore identical in PEAK dishing, which is an absolute value
        assert(positive.peakDishing(41).isCloseTo(negative.peakDishing(41), 1e-12))
    }

    @Test
    fun `gate 3 — a UNIFORM prestrain on a free tile does NOT dish zero, it curls it`() {
        // the analogy with a uniform LOAD fails: a uniform load is equilibrated by a rigid
        // translation, an eigenstrain is not — a uniform θ₀ across every interface is a uniform
        // curvature κ = θ₀/d and a free plate under uniform curvature is a cylinder.
        val soft = lattice(foundation = Gen1Tile.FOUNDATION_SECANT * 1e-4)
        val angle = 1.0e-3
        val curled = OrigamiGrillage(
            sheet = sheet, lengthX = 40.0, beamCount = 5,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT * 1e-4,
            columns = CrossoverLayout.centred(4, sheet.crossoverSpacing / 2.0),
            crossoverPrestrains = soft.crossoverSites.associateWith { angle }
        ).solve(uniformPressure(0.0))
        val curvature = angle / sheet.interhelicalDistance
        // sagitta of a cylinder over the across-helix span, with the best-fit plane removed:
        // ½κ(y² − ⟨y²⟩) peaks at ½κ(L²/4 − L²/12) = κL²/12
        val span = 5 * sheet.interhelicalDistance
        val sagitta = curvature * span * span / 12.0
        assert(curled.peakDishing(81) > 0.1 * sagitta)
        assert(abs(curled.peakDishing(81) / sagitta - 1.0) < 0.5)
    }

    @Test
    fun `gate 3 — the hinge moment a prestrained crossover carries is k times the RELATIVE angle`() {
        val site = CrossoverSite(0, 0)
        val angle = 0.05
        val strained = lattice(mapOf(site to angle))
        val solved = strained.solve(uniform)
        val hinge = Gen1Tile.crossoverHingeStiffness()
        val force = solved.crossoverForces.first { it.lowerBeam == 0 && it.column == 0 }
        val rotation = strained.hingeRotation(solved.coefficients, strained.crossovers.first {
            it.lowerBeam == 0 && it.column == 0
        })
        assert(force.hingeMoment.isCloseTo(hinge * (rotation - angle), relativeTolerance = 1e-9))
    }

    // ------------------------------------------------------- gate 4: numerical convergence

    @Test
    fun `gate 4 — the prestrain response is LINEAR in the angle, to machine precision`() {
        val sites = listOf(CrossoverSite(0, 0), CrossoverSite(1, 1))
        val unit = lattice(sites.associateWith { 1.0 }).solve(uniformPressure(0.0))
        listOf(1e-4, 1e-2, 0.25).forEach { angle ->
            val scaled = lattice(sites.associateWith { angle }).solve(uniformPressure(0.0))
            assert(
                scaled.peakDishing(41)
                    .isCloseTo(angle * unit.peakDishing(41), relativeTolerance = 1e-9)
            )
        }
    }

    // ------------------------------------------------------ gate 5: literature and upstream

    @Test
    fun `gate 5 — the couple a Rothemund-scale prestrain puts on a crossover is re-derived`() {
        val hinge = Gen1Tile.crossoverHingeStiffness()
        assert(hinge.isCloseTo(13.5294118, relativeTolerance = 1e-7))
        // the 16 bp register departure at the preferred 10.5 bp per turn — CLAUDE.md's 8.571°
        val angle = registerPrestrain(16.0, 360.0 / (32.0 / 3.0), 360.0 / 10.5)
        assert(hingePrestrainCouple(angle, hinge).isCloseTo(2.0240, relativeTolerance = 1e-3))
    }

}
