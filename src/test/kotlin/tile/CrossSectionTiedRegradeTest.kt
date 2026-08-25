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

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.kotlin.test.assert
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-294` — the `15 × 4` block graded coupled on the TIED lattice.
 *
 * Written before `tile/CrossSectionTiedRegrade.kt` and watched fail.
 *
 * The whole point of this task is that a census taken at `10 × 6` is **not** a census of
 * `15 × 4`: `CH-0270` and `CH-0276` are both instances of a count transferred onto a tile that
 * does not have it, and `C-0175`'s own lesson is that a lattice census must be asserted against
 * the **bond graph** rather than against the raster path. So every number this file pins is
 * pinned at **both** cross-sections, in one run, and the `10 × 6` column is the control.
 */
class CrossSectionTiedRegradeTest {

    private val tall = HoneycombBlock(15, 4)
    private val flat = HoneycombBlock(10, 6)
    private val rowBasePairs = 116

    private fun lattice(
        block: HoneycombBlock,
        enhancement: Double = 1.0,
        tied: Boolean = true,
        radial: Double? = null,
        transverse: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS
    ) = honeycombTiedLatticeAtResolvedLink(
        block = block,
        rowBasePairs = rowBasePairs,
        enhancement = enhancement,
        tied = tied,
        transverseLinkStiffness = transverse,
        radialLinkStiffness = radial
    )

    // ------------------------------------------------- gate 1: the censuses, at BOTH sections

    /** `P1`, and `F5`'s prediction: the bond count is a function of the cross-section. */
    @Test
    fun `gate 1 - the 15 x 4 lattice carries 410 bonds, 140 in plane and 270 through`() {
        val census = honeycombBondCensus(lattice(tall))
        assert(census.bonds == 410)
        assert(census.inPlane == 140)
        assert(census.throughThickness == 270)
        assert(census.inPlane + census.throughThickness == census.bonds)
    }

    /** The control, in the same run — `C-0208`'s own `135 / 300` of `435`. */
    @Test
    fun `gate 1 - the 10 x 6 lattice carries 435 bonds, 135 in plane and 300 through`() {
        val census = honeycombBondCensus(lattice(flat))
        assert(census.bonds == 435)
        assert(census.inPlane == 135)
        assert(census.throughThickness == 300)
    }

    /** `P1`: `H − 1 = 59` transfers and the SPLIT does not — `m(n−1)` and `m−1`. */
    @Test
    fun `gate 1 - the 15 x 4 raster carries 59 turns split 45 through and 14 in plane`() {
        val census = honeycombTieCensus(tall)
        assert(census.turns == 59)
        assert(census.throughThickness == 45)
        assert(census.inPlane == 14)
    }

    @Test
    fun `gate 1 - the 10 x 6 raster carries 59 turns split 50 through and 9 in plane`() {
        val census = honeycombTieCensus(flat)
        assert(census.turns == 59)
        assert(census.throughThickness == 50)
        assert(census.inPlane == 9)
    }

    /**
     * `C-0175`'s own lesson, re-taken at the other cross-section: a census keyed on the
     * traversal cannot see a raster turn landing on a pair the lattice does not bond, and one
     * keyed on *"is this pair actually bonded"* sees it immediately.
     */
    @Test
    fun `gate 1 - every raster turn joins a pair the lattice actually BONDS`() {
        assert(honeycombTieCensus(tall).everyTurnIsBonded)
        assert(honeycombTieCensus(flat).everyTurnIsBonded)
        assert(honeycombTieCensus(tall, firstAxialSign = -1).everyTurnIsBonded)
        assert(honeycombTurnsNotBonded(tall, honeycombRasterTurnList(tall)).isEmpty())
        assert(honeycombTurnsNotBonded(flat, honeycombRasterTurnList(flat)).isEmpty())
    }

    /**
     * And the check must be able to SAY NO. On a real honeycomb raster it never fires, so a test
     * that asserts only the empty answer cannot tell it from one that returns `emptyList()`
     * unconditionally — a mutation of exactly that shape survived this file's first harness run.
     * The fixture is `C-0175`'s own first-run defect: the naive `c = x` identification, which
     * joins two helices `2d` apart, on a pair the honeycomb does not bond at all.
     */
    @Test
    fun `gate 1 - the bond check reports a turn the lattice does NOT bond`() {
        val wrong = HoneycombRasterTurn(
            index = 0, lowerBeam = 0, upperBeam = 2, inPlane = false, atHighEnd = true
        )
        assert(honeycombTurnsNotBonded(tall, listOf(wrong)).size == 1)
        assert(honeycombTurnsNotBonded(tall, listOf(wrong)).first() === wrong)
        // and a pair the lattice DOES bond is not reported, on the same call
        val right = honeycombRasterTurnList(tall).first()
        assert(honeycombTurnsNotBonded(tall, listOf(right, wrong)).size == 1)
    }

    /** The rim split is a property of the turn COUNT, so it is `30 / 29` at both. */
    @Test
    fun `gate 1 - the tie rim split is 30 high and 29 low at both cross-sections`() {
        listOf(tall, flat).forEach {
            val census = honeycombTieCensus(it)
            assert(census.atHighRim == 30)
            assert(census.atLowRim == 29)
            assert(census.atHighRim + census.atLowRim == census.turns)
        }
        // and reversing the scaffold's first axial sign exchanges them, exactly
        val reversed = honeycombTieCensus(tall, firstAxialSign = -1)
        assert(reversed.atHighRim == 29)
        assert(reversed.atLowRim == 30)
    }

    @Test
    fun `gate 1 - the tie census refuses an axial sign that is neither plus nor minus one`() {
        assertFailsWith<IllegalArgumentException> { honeycombTieCensus(tall, firstAxialSign = 0) }
        assertFailsWith<IllegalArgumentException> { honeycombTieCensus(tall, firstAxialSign = 2) }
    }

    /**
     * `C-0204` §8: a range guard tested at one end only is a guard half of which no mutation can
     * reach. The fractional tolerance is bounded on both sides and both sides are exercised.
     */
    @Test
    fun `gate 1 - the normalisation refuses its arguments at BOTH ends of every range`() {
        assertFailsWith<IllegalArgumentException> { crossSectionNormalisation(tall, 0) }
        assertFailsWith<IllegalArgumentException> { crossSectionNormalisation(tall, -116) }
        assertFailsWith<IllegalArgumentException> {
            crossSectionNormalisation(tall, rowBasePairs, targetForce = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            crossSectionNormalisation(tall, rowBasePairs, foundationStiffness = -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            crossSectionNormalisation(tall, rowBasePairs, fractionalTolerance = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            crossSectionNormalisation(tall, rowBasePairs, fractionalTolerance = 1.5)
        }
    }

    @Test
    fun `gate 1 - the composite enhancement refuses a fraction outside zero to one, both ends`() {
        assertFailsWith<IllegalArgumentException> { honeycombCompositeEnhancement(tall, -0.01) }
        assertFailsWith<IllegalArgumentException> { honeycombCompositeEnhancement(tall, 1.01) }
        // and the two endpoints are ADMISSIBLE, so the guard cannot be widened into the interior
        assert(honeycombCompositeEnhancement(tall, 0.0) == 1.0)
        assert(honeycombCompositeEnhancement(tall, 1.0) > 29.0)
    }

    // ------------------------------------- gate 2: the limiting cases the pairing rests on

    /** `F2`. The empty-tie limit at the OTHER cross-section, on the objects a cell is made of. */
    @Test
    fun `gate 2 - an untied 15 x 4 lattice is bit-identical to the plain grillage`() {
        val plain = HoneycombGrillage(
            block = tall,
            rowBasePairs = rowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = 9.65079217
        )
        val untied = lattice(tall, enhancement = 9.65079217, tied = false)
        assert(untied.bonds.map { it.site } == plain.bonds.map { it.site })
        val pressure = uniformPressure(0.02)
        val a = plain.assembleLoad(pressure)
        val b = untied.assembleLoad(pressure)
        for (i in 0 until plain.degreesOfFreedom) assert(a[i] == b[i])
        val da = plain.pointLoadDual(3.0, 5.0, 1.0)
        val db = untied.pointLoadDual(3.0, 5.0, 1.0)
        for (i in 0 until plain.degreesOfFreedom) assert(da[i] == db[i])
    }

    /** `F2`, second half: `radialLinkStiffness = null` must be the penalty lattice exactly. */
    @Test
    fun `gate 2 - a null radial constant is the penalty lattice, entry for entry`() {
        val penalty = honeycombTiedLatticeAtLinkStiffness(tall, rowBasePairs, 1.0, tied = true)
        val resolved = lattice(tall, tied = true)
        assert(penalty.degreesOfFreedom == resolved.degreesOfFreedom)
        var identical = true
        for (i in 0 until penalty.degreesOfFreedom) {
            for (j in maxOf(0, i - penalty.bandwidth)..i) {
                if (penalty.stiffnessEntry(i, j) != resolved.stiffnessEntry(i, j)) identical = false
            }
        }
        assert(identical)
    }

    /** A single-layer block has its own mid-plane, so the parallel-axis factor is exactly 1. */
    @Test
    fun `gate 2 - a single-layer block has enhancement exactly one at every fraction`() {
        val single = HoneycombBlock(4, 1)
        assert(honeycombCompositeEnhancement(single, 0.0) == 1.0)
        assert(honeycombCompositeEnhancement(single, 0.26) == 1.0)
        assert(honeycombCompositeEnhancement(single, 1.0) == 1.0)
    }

    /** The two turn kinds are the two lattice steps, so each degenerates on its own. */
    @Test
    fun `gate 2 - a one-row block has no in-plane turn and a one-column block no through one`() {
        val oneRow = honeycombTieCensus(HoneycombBlock(1, 6))
        assert(oneRow.turns == 5)
        assert(oneRow.inPlane == 0)
        assert(oneRow.throughThickness == 5)
        val oneColumn = honeycombTieCensus(HoneycombBlock(6, 1))
        assert(oneColumn.turns == 5)
        assert(oneColumn.throughThickness == 0)
        assert(oneColumn.inPlane == 5)
    }

    // ------------------------- gate 3: the standing falsifier and the normalisation identity

    /**
     * `F1`, at the cross-section whose face carries fifteen beams and a `14 / 45` tie split —
     * **and it FIRED on the standing decomposition**, which is `CH-0282`.
     *
     * The solved field is *exactly* uniform: every face beam reads `p/k_f` to the last digit, and
     * the mean deflection assertion below passes on the standing object. What does not is the
     * **dishing**, which `HoneycombDeflection` removes by three independent projections and which
     * is therefore a least-squares fit only where the three modes are orthogonal. On a corrugated
     * face they are orthogonal iff the raster-row count is EVEN, so `10 × 6` reads `5e−12` and
     * `15 × 4` reads `0.062` of the stroke on a field that has no curvature at all.
     */
    @Test
    fun `gate 3 - a uniform pressure on the tied 15 x 4 lattice gives an exactly uniform field`() {
        val armed = lattice(tall, enhancement = 9.65079217, tied = true)
        val magnitude = 0.0444356284
        val field = armed.solve(uniformPressure(magnitude))
        val stroke = field.meanDeflection
        assert(abs(stroke - magnitude / Gen1Tile.FOUNDATION_SECANT) < 1e-9 * stroke)
        // the field itself: uniform at every face beam, to 1e-12 relative
        armed.faceBeams.forEach {
            assert(abs(field.deflection(0.0, armed.beamY[it]) - stroke) < 1e-10 * stroke)
        }
        // and the STANDING decomposition calls that curvature -- the defect, measured
        assert(field.peakDishing(41) / stroke > 0.05)
    }

    /** `F1` repaired: the least-squares fit annihilates a uniform field at BOTH cross-sections. */
    @Test
    fun `gate 3 - the CORRECTED dishing of a uniform pressure is zero at both cross-sections`() {
        listOf(tall to 9.65079217, flat to 21.1851817).forEach { (block, enhancement) ->
            val armed = lattice(block, enhancement = enhancement, tied = true)
            val magnitude = 0.0444356284
            val field = armed.solve(uniformPressure(magnitude))
            val corrected = FaceRigidBasis(armed).dishingOf(field)
            assert(corrected.peakDishing(41) / field.meanDeflection < 1e-9)
        }
    }

    /**
     * The mechanism, isolated: `⟨piston, tiltY⟩ = ∫y dA` over the face's tributaries, which is
     * zero iff the corrugated gap sequence `d, 2d, d, 2d, …` is palindromic — iff `m` is even.
     */
    @Test
    fun `gate 3 - the face rigid modes are orthogonal at even m and NOT at odd m`() {
        assert(FaceRigidBasis(lattice(flat)).modesAreOrthogonal)
        assert(FaceRigidBasis(HoneycombBlock(14, 4).let { lattice(it) }).modesAreOrthogonal)
        val odd = FaceRigidBasis(lattice(tall))
        assert(!odd.modesAreOrthogonal)
        assert(odd.worstNonOrthogonality > 1e-3)
        assert(!FaceRigidBasis(lattice(HoneycombBlock(11, 6))).modesAreOrthogonal)
        // and the offending entry is the piston/tilt-Y one, and only that one
        assert(abs(odd.gram[0][1]) < 1e-12 * sqrt(odd.gram[0][0] * odd.gram[1][1]))
        assert(abs(odd.gram[1][2]) < 1e-12 * sqrt(odd.gram[1][1] * odd.gram[2][2]))
        assert(abs(odd.gram[0][2]) > 1e-3 * sqrt(odd.gram[0][0] * odd.gram[2][2]))
    }

    /**
     * `C-0092`: a repair must be **measured** where the defect is absent, not merely asserted
     * inert. At `10 × 6` the basis is orthogonal, so the whole difference between the two
     * readings is the second, far smaller inconsistency — `HoneycombDeflection` fits with the
     * OWNING-beam reconstruction that `faceFunctional` builds and samples with the NEAREST-beam
     * one that `evaluate` uses, and on a corrugated face a `3d/2` tributary reaches past the
     * midpoint of a `d` gap. That is worth `< 1e−3` relative; the orthogonality defect at odd `m`
     * is worth `6e−2` of the stroke on a field with no curvature at all, i.e. two orders more.
     */
    @Test
    fun `gate 2 - at 10 x 6 the two dishing conventions agree to better than one in a thousand`() {
        val armed = lattice(flat, enhancement = 21.1851817, tied = true)
        val norm = crossSectionNormalisation(flat, rowBasePairs)
        val load = edgeCollarPressure(
            norm.interiorPressure, norm.edgeX, norm.edgeY,
            listOf(CollarTerm(0.2, 4.0), CollarTerm(0.5, 1.0))
        )
        val field = armed.solve(load)
        val corrected = FaceRigidBasis(armed).dishingOf(field)
        val standing = field.peakDishing(41)
        assert(standing > 1e-6)
        assert(abs(corrected.peakDishing(41) - standing) < 1e-3 * standing)
        assert(abs(corrected.peakDishing(41) - standing) > 1e-9 * standing)
    }

    /** Both surrogates come out of ONE set of solves and must agree with their own convention. */
    @Test
    fun `gate 2 - the paired surrogates carry the two conventions over one set of solves`() {
        val armed = lattice(flat, enhancement = 21.1851817, tied = true)
        val norm = crossSectionNormalisation(flat, rowBasePairs)
        val pressure = uniformPressure(norm.interiorPressure)
        val stations = attachmentGrid(1, 10, norm.edgeX, norm.edgeY)
        val pair = crossSectionSurrogates(armed, FaceRigidBasis(armed), stations, pressure, 41)
        val single = honeycombTiedSurrogate(armed, stations, pressure, 41)
        val share = equalShareOfMandate(stations.size)
        // the standing member IS honeycombTiedSurrogate, to 1e-12
        assert(abs(pair.standing.solve(share).peakDishing - single.solve(share).peakDishing) <
                1e-12 * maxOf(1e-30, abs(single.solve(share).peakDishing)) + 1e-15)
        // and the two members carry the same support forces, because the SOLVES are shared
        val a = pair.standing.solve(share).supportForces
        val b = pair.corrected.solve(share).supportForces
        a.indices.forEach { assert(abs(a[it] - b[it]) < 1e-12 * maxOf(1e-30, abs(a[it])) + 1e-15) }
    }

    /** The `3 × 3` solve refuses a singular matrix and reproduces a known solution. */
    @Test
    fun `gate 2 - the three by three solve is exact and refuses a singular matrix`() {
        val identity = listOf(
            listOf(2.0, 0.0, 0.0), listOf(0.0, 4.0, 0.0), listOf(0.0, 0.0, 5.0)
        )
        val solved = solveSymmetricThreeByThree(identity, listOf(2.0, 8.0, 15.0))
        assert(abs(solved[0] - 1.0) < 1e-15)
        assert(abs(solved[1] - 2.0) < 1e-15)
        assert(abs(solved[2] - 3.0) < 1e-15)
        assertFailsWith<IllegalArgumentException> {
            solveSymmetricThreeByThree(
                listOf(listOf(1.0, 1.0, 1.0), listOf(1.0, 1.0, 1.0), listOf(1.0, 1.0, 1.0)),
                listOf(1.0, 2.0, 3.0)
            )
        }
        assertFailsWith<IllegalArgumentException> {
            solveSymmetricThreeByThree(identity, listOf(1.0, 2.0))
        }
        // and a NON-singular matrix whose leading entry is zero, which only pivoting solves
        val needsPivoting = listOf(
            listOf(0.0, 1.0, 0.0), listOf(1.0, 0.0, 0.0), listOf(0.0, 0.0, 1.0)
        )
        val pivoted = solveSymmetricThreeByThree(needsPivoting, listOf(2.0, 3.0, 4.0))
        assert(abs(pivoted[0] - 3.0) < 1e-15)
        assert(abs(pivoted[1] - 2.0) < 1e-15)
        assert(abs(pivoted[2] - 4.0) < 1e-15)
    }

    /**
     * `C-0104`'s trap, at the surrogate level and at the cross-section nobody has graded: an
     * influence function taken on a **prestrained** lattice is that influence *plus* the
     * prestrain's own response, and the Woodbury compliance then stops being a compliance —
     * silently, at exactly the departures that matter. [crossSectionSurrogates] must take its
     * free field from the lattice as built and every influence from `withoutPrestrain`.
     *
     * The probe is `C-0180`'s: the single-path compliance recovered from public quantities
     * alone, `w_free = d + f M`, so `M = (w_free − d)/f`, and `M` may not move with the
     * prestrain. `honeycombTiedLatticeAtResolvedLink` hardcodes a zero prestrain, so the
     * prestrained lattice is built through `honeycombTiedLattice` — which is the entry point a
     * caller could hand this function.
     */
    @Test
    fun `gate 3 - the tie prestrain moves the free field and not the compliance at 15 x 4`() {
        val station = listOf(3.0 to 4.0)
        val norm = crossSectionNormalisation(tall, rowBasePairs)
        val pressure = uniformPressure(norm.interiorPressure)
        // d_i = w_free / (1 + M k_i) at two stiffnesses gives BOTH unknowns from public
        // quantities alone: M = (d1 - d2) / (d2 k2 - d1 k1), and w_free = d1 (1 + M k1).
        fun complianceAndFree(prestrain: Double): Pair<Double, Double> {
            val armed = honeycombTiedLattice(
                block = tall, rowBasePairs = rowBasePairs, enhancement = 9.65079217,
                tied = true, prestrainRadians = prestrain
            )
            val surrogate =
                crossSectionSurrogates(armed, FaceRigidBasis(armed), station, pressure, 21)
                    .corrected
            val one = 2.0
            val two = 8.0
            val first = surrogate.solve(listOf(one)).stationDeflections[0]
            val second = surrogate.solve(listOf(two)).stationDeflections[0]
            val compliance = (first - second) / (second * two - first * one)
            return compliance to first * (1.0 + compliance * one)
        }
        val (bare, freeBare) = complianceAndFree(0.0)
        val (loaded, freeLoaded) = complianceAndFree(0.05)
        assert(abs(loaded - bare) < 1e-9 * abs(bare))
        assert(abs(freeLoaded - freeBare) > 1e-9 * abs(freeBare))
    }

    /** A basis is a property of ONE lattice's geometry and may not be lent to another. */
    @Test
    fun `gate 1 - the surrogate refuses a basis built on a different lattice`() {
        val here = lattice(tall, enhancement = 9.65079217)
        val there = lattice(flat, enhancement = 21.1851817)
        val norm = crossSectionNormalisation(tall, rowBasePairs)
        assert(!FaceRigidBasis(there).belongsTo(here))
        assert(FaceRigidBasis(here).belongsTo(here))
        assertFailsWith<IllegalArgumentException> {
            crossSectionSurrogates(
                here, FaceRigidBasis(there), listOf(0.0 to 0.0),
                uniformPressure(norm.interiorPressure), 21
            )
        }
    }

    /**
     * `P2`. The stroke is `F/(edgeX · edgeY · k_f)` and `edgeX` is shared, so the ratio between
     * the two cross-sections is `10/15` **exactly** — a theorem, and the premise of this task's
     * own queue row that the lattice refuses.
     */
    @Test
    fun `gate 3 - the 15 x 4 stroke is exactly two thirds of the 10 x 6 one`() {
        val here = crossSectionNormalisation(tall, rowBasePairs)
        val there = crossSectionNormalisation(flat, rowBasePairs)
        assert(abs(here.edgeX - there.edgeX) < 1e-12)
        assert(abs(here.freeStroke / there.freeStroke - 2.0 / 3.0) < 1e-12)
        assert(abs(here.freeStroke -
                here.interiorPressure / Gen1Tile.FOUNDATION_SECANT) < 1e-12 * here.freeStroke)
    }

    /** `T-5b`'s `0.10` is a fraction of a stroke, so in nm it is `1.5×` tighter at `15 × 4`. */
    @Test
    fun `gate 3 - the absolute tolerance is 1_5 times tighter on the 15 x 4 block`() {
        val here = crossSectionNormalisation(tall, rowBasePairs)
        val there = crossSectionNormalisation(flat, rowBasePairs)
        assert(abs(here.absoluteToleranceNm - 0.10 * here.freeStroke) < 1e-15)
        assert(abs(there.absoluteToleranceNm / here.absoluteToleranceNm - 1.5) < 1e-12)
    }

    /**
     * `F3`. Superposition is exact, so the surrogate's own solution must be the lattice's under
     * the support forces it reports — the identity the whole 4 000-realisation ensemble rests on,
     * taken at the cross-section nobody has graded.
     */
    @Test
    fun `gate 3 - the surrogate reproduces the assembled 15 x 4 solve`() {
        val armed = lattice(tall, enhancement = 9.65079217, tied = true)
        val norm = crossSectionNormalisation(tall, rowBasePairs)
        val pressure = uniformPressure(norm.interiorPressure)
        val stations = attachmentGrid(1, 15, norm.edgeX, norm.edgeY)
        val basis = FaceRigidBasis(armed)
        val surrogate = crossSectionSurrogates(armed, basis, stations, pressure, 41).corrected
        val stiffnesses = equalShareOfMandate(stations.size)
        val solved = surrogate.solve(stiffnesses)
        val loads = stations.mapIndexed { i, (s, y) -> PointLoad(s, y, -solved.supportForces[i]) }
        val assembled = basis.dishingOf(armed.solve(pressure, loads))
        assert(abs(assembled.peakDishing(41) - solved.peakDishing) <
                1e-9 * abs(solved.peakDishing))
    }

    /** The Loewner statement, read where it IS a statement — `fᵀK⁻¹f` at a unit point load. */
    @Test
    fun `gate 3 - the ties cannot soften the 15 x 4 block under a fixed point load`() {
        fun complianceOf(tied: Boolean) = lattice(tall, enhancement = 9.65079217, tied = tied)
            .solve(uniformPressure(0.0), listOf(PointLoad(4.0, 6.0, 1.0))).deflection(4.0, 6.0)
        val plain = complianceOf(tied = false)
        val armed = complianceOf(tied = true)
        assert(plain > 0.0)
        assert(armed < plain)
    }

    // ------------------------------------------------- gate 5: the upstream reproductions

    /** `T-253` emits `realisedEnhancement15x4 = 9.65079217`, which is `f = 0.30` at four layers. */
    @Test
    fun `gate 5 - the 15 x 4 enhancement reproduces T-253's committed 9_65079217`() {
        assert(abs(honeycombCompositeEnhancement(tall, 0.30) - 9.65079217) < 1e-8 * 9.65079217)
    }

    /** `C-0180`'s own two, so the enhancement helper is the same object at both sections. */
    @Test
    fun `gate 5 - the 10 x 6 enhancements reproduce C-0180's 21_1851817 and 18_4938242`() {
        assert(abs(honeycombCompositeEnhancement(flat, 0.30) - 21.1851817) < 1e-8 * 21.1851817)
        assert(abs(honeycombCompositeEnhancement(flat, 0.26) - 18.4938242) < 1e-8 * 18.4938242)
    }

    /**
     * `P7`. `C-0146`'s own committed `15 × 4` geometry at this block extent —
     * `edgeY = 57.06`, `interiorPressure = 0.0444356284`, `freeStroke = 3.5194795` — so the
     * normalisation is a **reproduction** and not a derivation.
     */
    @Test
    fun `gate 5 - the 15 x 4 normalisation reproduces C-0146's committed geometry`() {
        val norm = crossSectionNormalisation(tall, rowBasePairs)
        assert(abs(norm.edgeX - 39.44) < 1e-9)
        assert(abs(norm.edgeY - 57.06) < 1e-9)
        assert(abs(norm.interiorPressure - 0.0444356284) < 1e-8 * 0.0444356284)
        assert(abs(norm.freeStroke - 3.5194795) < 1e-8 * 3.5194795)
    }

    /** and `C-0167`'s own `10 × 6` stroke, `5.27921926 nm`, in the same call. */
    @Test
    fun `gate 5 - the 10 x 6 normalisation reproduces C-0167's committed 5_27921926`() {
        val norm = crossSectionNormalisation(flat, rowBasePairs)
        assert(abs(norm.edgeY - 38.04) < 1e-9)
        assert(abs(norm.freeStroke - 5.27921926) < 1e-8 * 5.27921926)
    }
}
