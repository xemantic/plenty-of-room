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
 * `T-190` — what do the 42 **interior** crossovers carry, and does their cancellation hold?
 *
 * `C-0107` derived a boundary layer `u(x)` and read it two ways: as a prestrain on the **14**
 * row-end sites alone (0.1022820 of the free stroke, **not** flat) and as the graded corrugated
 * field `(−1)^b u(x)` over all **56** (0.0922622, flat). The difference is the 42 interior sites,
 * which no claim has posed as a question.
 *
 * A prestrain is a **load**, so the decomposition is an identity rather than a model. These tests
 * establish the arithmetic that makes it one, gate by gate, per §5 of the problem definition.
 */
class InteriorCrossoverPrestrainTest {

    private val hinge = Gen1Tile.crossoverHingeStiffness()
    private val model = EdgeTwistRelief(
        torsionalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
        hingeStiffness = hinge,
        crossoverSpacing = 10.2,
        rowLength = 38.08
    )
    private val mismatch = twistRateMismatch(
        designTwistPerBase = 360.0 / (32.0 / 3.0),
        naturalTwistPerBase = 360.0 / 10.5,
        risePerBase = Gen1Tile.RISE_PER_BASE_PAIR
    )

    /** Eight columns at the phase-8 lattice's own positions, the row-end pair inset by 0.05 nm. */
    private val columnX = listOf(-18.99, -13.6, -8.16, -2.72, 2.72, 8.16, 13.6, 18.99)

    /** Every site of a 15-duplex, 8-column lattice under the parity rule `(parity(c) + b) % 2`. */
    private val allSites: List<CrossoverSite> = (0 until 14).flatMap { b ->
        columnX.indices.filter { (it % 2 + b) % 2 == 0 }.map { CrossoverSite(b, it) }
    }

    private val rowEndSites: List<CrossoverSite> = allSites.filter { it.column == 0 || it.column == 7 }

    private fun x(site: CrossoverSite): Double = columnX[site.column]

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 dimensional the partition counts 14 and 42 and loses nothing`() {
        val partition = partitionRowEnd(allSites, rowEndSites)
        assert(allSites.size == 56)
        assert(partition.rowEnd.size == 14)
        assert(partition.interior.size == 42)
        assert((partition.rowEnd + partition.interior).toSet() == allSites.toSet())
        assert(partition.rowEnd.intersect(partition.interior.toSet()).isEmpty())
    }

    @Test
    fun `gate 1 dimensional a prestrain ledger is a couple in pN nm and odd in the angle`() {
        val field = corrugatedPrestrainField(model, mismatch, allSites, ::x)
        val ledger = prestrainLedger(field, hinge)
        assert(ledger.sites == 56)
        // the couple is k_theta x angle, so the absolute couple is the hinge times the sum of |θ|
        assert(ledger.absoluteCouple.isCloseTo(hinge * field.values.sumOf { abs(it) }))
        assert(ledger.netCouple.isCloseTo(hinge * field.values.sum()))
        val negated = prestrainLedger(field.mapValues { -it.value }, hinge)
        assert(negated.netCouple.isCloseTo(-ledger.netCouple))
        assert(negated.absoluteCouple.isCloseTo(ledger.absoluteCouple))
        assert(negated.peakDegrees.isCloseTo(ledger.peakDegrees))
    }

    @Test
    fun `gate 1 dimensional unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { prestrainLedger(emptyMap(), 0.0) }
        assertFailsWith<IllegalArgumentException> {
            corrugatedPrestrainField(model, mismatch, allSites, ::x, sign = 0.0)
        }
        assertFailsWith<IllegalArgumentException> { cosineFromInnerProducts(0.0, 1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { centroSymmetricPartner(allSites[0], 1, 8) }
    }

    // --------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting a vanishing decay length concentrates the field at the row ends`() {
        // lambda -> 0 is C-0104's row-end-only idealisation: u is exponentially small everywhere
        // but within lambda of an end, so the interior share of the absolute couple goes to zero.
        val stiff = model.copy(hingeStiffness = hinge * 1.0e8)
        val field = corrugatedPrestrainField(stiff, mismatch, allSites, ::x)
        val partition = partitionRowEnd(allSites, rowEndSites)
        val interior = prestrainLedger(restrictPrestrains(field, partition.interior), hinge)
        val end = prestrainLedger(restrictPrestrains(field, partition.rowEnd), hinge)
        assert(interior.absoluteCouple / end.absoluteCouple < 1.0e-6)
    }

    @Test
    fun `gate 2 limiting a vanishing hinge makes the field linear in x`() {
        // lambda -> infinity: u -> mismatch x, so the ratio of two columns is the ratio of their x
        val soft = model.copy(hingeStiffness = hinge * 1.0e-10)
        val field = corrugatedPrestrainField(soft, mismatch, allSites, ::x)
        val end = abs(field.getValue(CrossoverSite(0, 0)))
        val inner = abs(field.getValue(CrossoverSite(0, 2)))
        assert((inner / end).isCloseTo(8.16 / 18.99, relativeTolerance = 1e-6))
    }

    @Test
    fun `gate 2 limiting a zero mismatch is a zero field and an empty restriction is empty`() {
        val field = corrugatedPrestrainField(model, 0.0, allSites, ::x)
        assert(field.values.all { it == 0.0 })
        assert(restrictPrestrains(field, emptyList()).isEmpty())
        assert(prestrainLedger(emptyMap(), hinge).sites == 0)
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 symmetry the graded field is invariant under the tile centro-symmetry`() {
        // b -> D-2-b together with c -> C-1-c maps a site to a site and the field to itself,
        // because u is odd in x and (-1)^(13-b) = -(-1)^b. It is the ONE symmetry a Rothemund
        // sheet has (CLAUDE.md: centro-symmetric, not mirror-symmetric), and it fixes the field.
        val field = corrugatedPrestrainField(model, mismatch, allSites, ::x)
        allSites.forEach { site ->
            val partner = centroSymmetricPartner(site, duplexes = 15, columns = 8)
            assert(partner in field.keys)
            assert(field.getValue(partner).isCloseTo(field.getValue(site)))
        }
    }

    @Test
    fun `gate 3 symmetry the row-end restriction of the graded field is UNIFORM`() {
        // The lattice's own parity rule puts every even interface's row-end crossover at the SAME
        // end, so all 14 carry one angle. Measured, not asserted: uniformValueOrNull returns null
        // if they do not agree.
        val field = corrugatedPrestrainField(model, mismatch, allSites, ::x)
        val partition = partitionRowEnd(allSites, rowEndSites)
        val uniform = uniformValueOrNull(restrictPrestrains(field, partition.rowEnd))
            ?: error("the row-end restriction of the graded field is not uniform")
        assert(abs(uniform * 180.0 / PI).isCloseTo(
            abs(model.residualAt(18.99, mismatch)) * 180.0 / PI))
    }

    @Test
    fun `gate 3 conservation the field splits exactly into its two restrictions`() {
        val field = corrugatedPrestrainField(model, mismatch, allSites, ::x)
        val partition = partitionRowEnd(allSites, rowEndSites)
        val end = restrictPrestrains(field, partition.rowEnd)
        val interior = restrictPrestrains(field, partition.interior)
        assert(end.size + interior.size == field.size)
        field.forEach { (site, angle) ->
            val split = (end[site] ?: 0.0) + (interior[site] ?: 0.0)
            assert(split == angle)
        }
    }

    @Test
    fun `gate 3 conservation the cosine expands a squared norm exactly`() {
        // ||a+b||^2 = ||a||^2 + 2 cos ||a|| ||b|| + ||b||^2 — the identity the decomposition of a
        // seminorm rests on, since peak dishing is not additive but the area norm is quadratic.
        val aa = 4.0
        val bb = 9.0
        val ab = -5.0
        val cosine = cosineFromInnerProducts(aa, bb, ab)
        assert(cosine.isCloseTo(-5.0 / 6.0))
        assert((aa + 2.0 * ab + bb).isCloseTo(
            aa + bb + 2.0 * cosine * Math.sqrt(aa) * Math.sqrt(bb)))
    }

    // ------------------------------------------------------- gate 4 — numerical convergence

    @Test
    fun `gate 4 convergence the closed-form field settles against a discrete chain`() {
        // The whole decomposition is built on `residualAt`, so what has to converge is the field
        // the closed form asserts. `discreteEndResidual` minimises the same energy over a
        // tridiagonal chain and is an independent construction; the departure must FALL under
        // nested refinement, which is the only statement a convergence gate is entitled to.
        val exact = model.endResidual(mismatch)
        val coarse = abs(model.discreteEndResidual(mismatch, 16) - exact) / abs(exact)
        val fine = abs(model.discreteEndResidual(mismatch, 64) - exact) / abs(exact)
        val finer = abs(model.discreteEndResidual(mismatch, 256) - exact) / abs(exact)
        assert(fine < coarse)
        assert(finer < fine)
        assert(finer < 1.0e-4)
    }

    // ---------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 upstream the nominal end residual reproduces C-0107's 22 point 6184533 degrees`() {
        val derived = EdgeTwistRelief(
            torsionalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
            hingeStiffness = hinge,
            crossoverSpacing = 15.0 * 38.08 / 56.0,
            rowLength = 38.08
        )
        assert((derived.endResidual(mismatch) * 180.0 / PI).isCloseTo(22.6184533, 1e-7))
    }

    @Test
    fun `gate 5 upstream the graded row-end angle is within a per cent of the end residual`() {
        // The graded field is evaluated at the COLUMN, 18.99 nm, and C-0107's row-end-only states
        // are evaluated at the row end, 19.04 nm. That is a real difference and it is small; the
        // point of the test is that it is bounded, so it cannot be what moves a verdict.
        val derived = model.copy(crossoverSpacing = 15.0 * 38.08 / 56.0)
        val atColumn = abs(derived.residualAt(18.99, mismatch))
        val atEnd = abs(derived.endResidual(mismatch))
        assert(atColumn < atEnd)
        assert(1.0 - atColumn / atEnd < 0.01)
    }
}
