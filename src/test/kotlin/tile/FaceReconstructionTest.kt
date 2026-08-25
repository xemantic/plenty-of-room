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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.kotlin.test.assert
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test

/**
 * `T-326` — the reconstruction the dishing fit is taken in, against the one it is sampled in.
 *
 * A honeycomb face carries its field on beam axes, and off an axis the class must reconstruct
 * it. There are two reconstructions in the shipped code and they disagree:
 *
 *  * **owning beam** — inside `[y_r − p/2, y_r + p/2]` the field is `W_r + Φ_r(y − y_r)`; this
 *    is what [HoneycombGrillage.faceFunctional] pairs with, hence what
 *    [HoneycombDeflection.rigidPlaneCoefficients] FITS in at an even raster-row count;
 *  * **nearest beam** — [HoneycombGrillage.evaluate], hence what every reported dishing SAMPLES.
 *
 * `CH-0284` measures the gap and refuses to close it. This suite carries the closed form of it,
 * the third convention that removes it, and the quadrature defect that under-reports it.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class FaceReconstructionTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    /** A block whose face geometry is `m × n`'s at a fraction of the unknowns. */
    private fun face(
        rows: Int,
        columns: Int = 2,
        rowBasePairs: Int = 42,
        faceColumn: Int = 0
    ) = HoneycombGrillage(
        block = HoneycombBlock(rows, columns),
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        faceColumn = faceColumn
    )

    /** A reproducible pseudo-random face field — not a solve, so no physics is asserted on it. */
    private fun field(lattice: HoneycombGrillage, seed: Int): F64Array {
        val random = Random(seed)
        val v = F64Array(lattice.degreesOfFreedom)
        for (i in 0 until lattice.degreesOfFreedom) v[i] = random.nextDouble() - 0.5
        return v
    }

    /** `⟨mode, nearestRecon(u)⟩ − ⟨mode, owningRecon(u)⟩`, both taken exactly. */
    private fun measuredGap(lattice: HoneycombGrillage, mode: Int, u: F64Array): Double {
        val m = lattice.faceRigidModes[mode]
        val nearest = lattice.integrateOverFaceSplit { s, y ->
            lattice.evaluate(m, s, y) * lattice.evaluate(u, s, y)
        }
        return nearest - lattice.faceFunctional(m).dot(u)
    }

    /** The one scale in the problem, so a departure between two cancelling readings is absolute. */
    private fun scaleOf(lattice: HoneycombGrillage, u: F64Array): Double {
        var peak = 0.0
        for (i in 0 until lattice.degreesOfFreedom) peak = maxOf(peak, abs(u[i]))
        return lattice.area * peak * (1.0 + lattice.lengthY)
    }

    // ------------------------------------------------------------------ gate 1, dimensional

    @Test
    fun `gate 1 - dimensional - the sampled Gram's leading entry is the face area in nm squared`() {
        val lattice = face(10)
        assert(abs(lattice.faceSampledGram[0][0] - lattice.area) < 1e-9 * lattice.area)
    }

    @Test
    fun `gate 1 - dimensional - the sampled Gram is the exact rectangle Gram`() {
        for (m in listOf(3, 10, 15)) {
            val lattice = face(m)
            val a = lattice.area
            val expected = listOf(
                a, a * lattice.lengthS * lattice.lengthS / 12.0,
                a * lattice.lengthY * lattice.lengthY / 12.0
            )
            expected.forEachIndexed { i, e ->
                assert(abs(lattice.faceSampledGram[i][i] - e) < 1e-9 * e)
            }
        }
    }

    @Test
    fun `gate 1 - dimensional - a reconstruction gap dual paired with a field is an nm cubed volume`() {
        // ⟨1, u⟩ is a deflection integrated over an area, so the dual of the piston mode paired
        // with a field of unit deflection everywhere must return exactly zero (a rigid piston has
        // no relative roll) rather than something with the wrong dimension.
        val lattice = face(10)
        assert(abs(lattice.reconstructionGapDual(0).dot(lattice.pistonMode)) < 1e-9 * lattice.area)
    }

    // ------------------------------------------------------------------ gate 2, limiting cases

    @Test
    fun `gate 2 - limiting - F7 the owning strips are not a partition, they overlap and gap by d over two`() {
        // `CH-0284` §4's first remedy — make `evaluate` use the tributary the load is assembled
        // over — is NOT WELL POSED, and this is why: at a point in an overlap two beams own the
        // field and at a point in a gap none does.
        val lattice = face(10)
        val axes = lattice.faceBeams.map { lattice.beamY[it] }
        var overlaps = 0
        var gaps = 0
        axes.zipWithNext { lower, upper ->
            val slack = (upper - lower) - lattice.rowPitch
            if (slack < 0.0) overlaps++ else if (slack > 0.0) gaps++
            assert(abs(abs(slack) - d / 2.0) < 1e-12)
        }
        assert(overlaps == 5)
        assert(gaps == 4)
        // and their total measure is still exactly the face width, which is what makes the
        // uniform-load falsifier exact
        assert(abs(axes.size * lattice.rowPitch - lattice.lengthY) < 1e-12)
    }

    @Test
    fun `gate 2 - limiting - the reconstruction gap is exactly zero on all three rigid modes`() {
        for (m in listOf(3, 4, 10, 15)) {
            val lattice = face(m)
            for (mode in 0..2) {
                lattice.faceRigidModes.forEach { probe ->
                    val gap = lattice.reconstructionGapDual(mode).dot(probe)
                    assert(abs(gap) < 1e-9 * lattice.area * lattice.lengthY)
                }
            }
        }
    }

    @Test
    fun `gate 2 - limiting - a one-beam face has no vertical bond and therefore no gap at all`() {
        val lattice = face(1, columns = 1)
        assert(lattice.faceVerticalBondPairs.isEmpty())
        val u = field(lattice, 4)
        for (mode in 0..2) assert(abs(lattice.reconstructionGapDual(mode).dot(u)) < 1e-12)
    }

    @Test
    fun `gate 2 - limiting - the split and unsplit face integrals agree on a field smooth in y`() {
        // A pressure that does not go through `evaluate` has no jump, so splitting must change
        // nothing: that is what says the split is about the RECONSTRUCTION and not about the rule.
        val lattice = face(10)
        val smooth = lattice.integrateOverFace { s, y -> 1.0 + 0.3 * s + 0.2 * y + 0.01 * y * y }
        val split = lattice.integrateOverFaceSplit { s, y -> 1.0 + 0.3 * s + 0.2 * y + 0.01 * y * y }
        assert(abs(split - smooth) < 1e-9 * abs(smooth))
    }

    // ------------------------------------------------------------------ gate 3, symmetry

    @Test
    fun `gate 3 - symmetry - the face's vertical bond pairs are its d-gaps and they are m over two`() {
        for (m in 3..16) {
            for (column in 0..1) {
                val lattice = face(m, faceColumn = column)
                lattice.faceVerticalBondPairs.forEach { (lower, upper) ->
                    val gap = lattice.beamY[lattice.faceBeams[upper]] -
                            lattice.beamY[lattice.faceBeams[lower]]
                    assert(abs(gap - d) < 1e-12)
                    assert(upper == lower + 1)
                }
            }
        }
    }

    @Test
    fun `gate 3 - symmetry - P2 the sampled Gram is diagonal at EVERY m and both face columns`() {
        // Convention C DISSOLVES `CH-0282`'s parity rather than repairing it: `∫y dA` over a
        // rectangle symmetric about its own centre is zero whatever the ladder does.
        for (m in 3..16) {
            for (column in 0..1) {
                val lattice = face(m, faceColumn = column)
                assert(lattice.worstSampledFaceNonOrthogonality < 1e-12)
            }
        }
    }

    @Test
    fun `gate 3 - symmetry - the sampled decomposition annihilates each of its own basis modes`() {
        for (m in listOf(3, 4, 10, 11, 15)) {
            val lattice = face(m)
            lattice.faceRigidModes.forEachIndexed { index, mode ->
                lattice.sampledFaceRigidCoefficients(mode).forEachIndexed { j, c ->
                    assert(abs(c - (if (j == index) 1.0 else 0.0)) < 1e-9)
                }
            }
        }
    }

    // ------------------------------------------------------- gate 4, numerical convergence

    @Test
    fun `gate 4 - convergence - P1 the closed form reproduces the measured gap on the piston mode`() {
        var worst = 0.0
        for (m in 3..16) {
            for (column in 0..1) {
                val lattice = face(m, faceColumn = column)
                for (seed in 1..3) {
                    val u = field(lattice, seed * 100 + m * 3 + column)
                    val departure = abs(lattice.reconstructionGapDual(0).dot(u) - measuredGap(lattice, 0, u))
                    worst = maxOf(worst, departure / scaleOf(lattice, u))
                }
            }
        }
        assert(worst < 1e-10)
    }

    @Test
    fun `gate 4 - convergence - P1 the closed form reproduces the measured gap on both tilt modes`() {
        var worst = 0.0
        for (m in 3..16) {
            for (column in 0..1) {
                val lattice = face(m, faceColumn = column)
                for (seed in 1..3) {
                    val u = field(lattice, seed * 977 + m * 7 + column)
                    for (mode in 1..2) {
                        val departure =
                            abs(lattice.reconstructionGapDual(mode).dot(u) - measuredGap(lattice, mode, u))
                        worst = maxOf(worst, departure / scaleOf(lattice, u))
                    }
                }
            }
        }
        assert(worst < 1e-10)
    }

    @Test
    fun `gate 4 - convergence - the split matters only where the integrand JUMPS`() {
        // The discriminating pair. On a field that does not go through `evaluate` the two rules
        // agree to machine precision; on `evaluate`'s own discontinuous reconstruction they do
        // not, and that difference is the defect `P11` measures. A split that changed the smooth
        // case would be a broken quadrature; one that did not change the jump case would be no
        // split at all.
        val lattice = face(10)
        val u = field(lattice, 31)
        val smooth = { s: Double, y: Double -> 1.0 + 0.3 * s + 0.2 * y + 0.01 * y * y }
        val onSmooth = abs(lattice.integrateOverFaceSplit(smooth) - lattice.integrateOverFace(smooth))
        assert(onSmooth < 1e-9 * abs(lattice.integrateOverFace(smooth)))
        val jumpy = { s: Double, y: Double -> lattice.evaluate(u, s, y) * lattice.evaluate(u, s, y) }
        val onJump = abs(lattice.integrateOverFaceSplit(jumpy) - lattice.integrateOverFace(jumpy))
        assert(onJump > 1e-6 * abs(lattice.integrateOverFaceSplit(jumpy)))
    }

    // ------------------------------------------------- gate 5, cross-check against the corpus

    @Test
    fun `gate 5 - cross-check - P11 the shipped rule under-reports the gap by a constant factor`() {
        // `integrateOverFace` lays 6-point Gauss across a whole strip, and `evaluate` JUMPS a
        // quarter of a bond inside each strip's end. The ratio is a pure number because both
        // readings are linear functionals the bond pairing reduces to multiples of one scalar.
        val ratios = ArrayList<Double>()
        for (m in listOf(4, 6, 10, 14, 15)) {
            for (column in 0..1) {
                val lattice = face(m, faceColumn = column)
                val u = field(lattice, m * 31 + column)
                val piston = lattice.faceRigidModes[0]
                val owning = lattice.faceFunctional(piston).dot(u)
                val unsplit = lattice.integrateOverFace { s, y ->
                    lattice.evaluate(piston, s, y) * lattice.evaluate(u, s, y)
                } - owning
                ratios += unsplit / measuredGap(lattice, 0, u)
            }
        }
        val low = ratios.minOrNull()!!
        val high = ratios.maxOrNull()!!
        assert(high - low < 1e-6)
        assert(abs(low - 0.819694) < 1e-6)
    }

    @Test
    fun `gate 5 - cross-check - P3 the three conventions are collinear at exactly six on the piston`() {
        for (m in listOf(4, 6, 10, 14, 16)) {
            val lattice = face(m)
            val u = field(lattice, m * 17)
            val piston = lattice.faceRigidModes[0]
            val owning = lattice.faceFunctional(piston).dot(u)
            val b = measuredGap(lattice, 0, u)
            val c = lattice.integrateOverFaceRectangle { s, y ->
                lattice.evaluate(piston, s, y) * lattice.evaluate(u, s, y)
            } - owning
            assert(abs(c / b - 6.0) < 1e-8)
        }
    }

    @Test
    fun `gate 5 - cross-check - the retained convention is untouched by everything added here`() {
        // `P9`: the addition is inert. Nothing in the shipped decomposition may move.
        for (m in listOf(4, 10, 15)) {
            val lattice = face(m)
            val u = field(lattice, m + 5)
            val standing = lattice.faceRigidCoefficients(u)
            val expected = if (m % 2 == 0) listOf(
                lattice.pistonDual.dot(u) / lattice.area,
                lattice.tiltSDual.dot(u) / lattice.tiltSNorm,
                lattice.tiltYDual.dot(u) / lattice.tiltYNorm
            ) else lattice.unconditionalFaceRigidCoefficients(u)
            standing.forEachIndexed { i, c -> assert(c == expected[i]) }
        }
    }
}
