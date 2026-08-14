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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import org.openrndr.math.Matrix33
import org.openrndr.math.Vector3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-71` — the backbone-torsion check of `C-0029`'s closed routing.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The one that licenses all the others is **gate 2's free limiting case**: a step *inside* an ideal
 * B-form duplex must return the canonical torsions and near-ideal covalent geometry, because
 * otherwise a junction's residual would be measuring the template placement rather than the
 * junction.
 */
class BackboneTorsionTest {

    private val backbone = DuplexBackbone()

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 dimensional - a torsion is an angle, zero at cis and 180 at trans`() {
        val a = Vector3(1.0, 1.0, 0.0)
        val b = Vector3(0.0, 1.0, 0.0)
        val c = Vector3(0.0, 0.0, 0.0)
        assert(torsionDegrees(a, b, c, Vector3(1.0, 0.0, 0.0)).isCloseTo(0.0, 1e-9))
        assert(abs(torsionDegrees(a, b, c, Vector3(-1.0, 0.0, 0.0))).isCloseTo(180.0))
        assert(bondAngleDegrees(a, b, c).isCloseTo(90.0))
    }

    @Test
    fun `gate 1 dimensional - a torsion is ODD under reflection and an angle is EVEN`() {
        val a = Vector3(1.0, 1.0, 0.2)
        val b = Vector3(0.0, 1.0, 0.0)
        val c = Vector3(0.0, 0.0, 0.0)
        val d = Vector3(0.6, -0.3, 0.8)
        val mirror = { v: Vector3 -> Vector3(v.x, v.y, -v.z) }
        assert(
            torsionDegrees(a, b, c, d)
                .isCloseTo(-torsionDegrees(mirror(a), mirror(b), mirror(c), mirror(d)))
        )
        assert(bondAngleDegrees(a, b, c).isCloseTo(bondAngleDegrees(mirror(a), mirror(b), mirror(c))))
    }

    @Test
    fun `gate 1 dimensional - an atom placed by bond, angle and torsion reproduces all three`() {
        val a = Vector3(1.3, 0.4, -0.2)
        val b = Vector3(0.0, 0.9, 0.1)
        val c = Vector3(0.0, 0.0, 0.0)
        val placed = placeAtom(a, b, c, bond = 0.1593, angleDegrees = 103.25, torsionDegrees = -71.0)
        assert((placed - c).length.isCloseTo(0.1593, 1e-12))
        assert(bondAngleDegrees(b, c, placed).isCloseTo(103.25))
        assert(torsionDegrees(a, b, c, placed).isCloseTo(-71.0))
    }

    @Test
    fun `gate 1 dimensional - the phosphodiester reach is a length, even in its torsion`() {
        val shortest = PhosphodiesterGeometry.reachAt(0.0)
        val longest = PhosphodiesterGeometry.reachAt(180.0)
        assert(shortest > 0.0)
        assert(longest > shortest)
        assert(PhosphodiesterGeometry.reachMinimum.isCloseTo(shortest, 1e-12))
        assert(PhosphodiesterGeometry.reachMaximum.isCloseTo(longest, 1e-12))
        // a distance cannot know the sign of a torsion
        assert(PhosphodiesterGeometry.reachAt(60.0).isCloseTo(PhosphodiesterGeometry.reachAt(-60.0)))
        // the tolerant reading is strictly wider on both sides
        assert(PhosphodiesterGeometry.reachMinimumTolerant < shortest)
        assert(PhosphodiesterGeometry.reachMaximumTolerant > longest)
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw at five entry points`() {
        assertFailsWith<IllegalArgumentException> {
            placeAtom(Vector3.ZERO, Vector3.UNIT_X, Vector3.UNIT_Y, -1.0, 100.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            placeAtom(Vector3.ZERO, Vector3.UNIT_X, Vector3.UNIT_Y, 0.15, 200.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            DuplexSite(Vector3.ZERO, Vector3.ZERO, Vector3.ZERO, 1)
        }
        assertFailsWith<IllegalArgumentException> {
            DuplexSite(Vector3.UNIT_X, Vector3.ZERO, Vector3.UNIT_X, 0)
        }
        assertFailsWith<IllegalArgumentException> {
            // a phosphorus on the duplex axis has no radial direction, so no frame
            PlacedResidue(
                DuplexSite(Vector3.ZERO, Vector3.ZERO, Vector3.UNIT_Z, 1),
                NucleotideTemplate.B_SOUTH
            ).atoms
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 limiting - THE FREE LIMITING CASE, a real B-DNA step returns its own torsions`() {
        val step = measuredStepClosure()
        val donor = NucleotideTemplate.B_SOUTH.torsions
        val acceptor = NucleotideTemplate.B_SOUTH_NEXT.torsions
        // alpha, beta and gamma belong to the acceptor; epsilon and zeta to the donor.
        assert(angularDistance(step.torsions.alpha, acceptor.alpha) < 6.0)
        assert(angularDistance(step.torsions.beta, acceptor.beta) < 6.0)
        assert(angularDistance(step.torsions.gamma, acceptor.gamma) < 6.0)
        assert(angularDistance(step.torsions.epsilon, donor.epsilon) < 6.0)
        assert(angularDistance(step.torsions.zeta, donor.zeta) < 6.0)
        assert(step.minimumStrainZ < 1.0)
        assert(step.worstCovalentZ < PhosphodiesterGeometry.STRAIN_CEILING)
        assert(step.torsionsPopulated)
        assert(step.closes)
        // and it is exact even under the PINNED reading, because both phosphorus atoms are real
        val pinned = measuredStepClosure(PhosphateReading.PINNED)
        assert(pinned.minimumStrainZ < 1.0)
        assert(pinned.closes)
    }

    @Test
    fun `gate 2 limiting - the real step's own conformer is the most populated one there is`() {
        val step = measuredStepClosure()
        // K1 is the largest class in the survey, and it IS canonical BI B-DNA.
        assert(step.conformer == "K1")
        assert(BDnaConformerSurvey.classes.first().name == "K1")
        assert(BDnaConformerSurvey.classes.first().fraction > 0.3)
        assert(step.minimumOccupancy > 10.0 * BDnaTorsionOccupancy.FLOOR)
    }

    @Test
    fun `gate 2 limiting - a C3'-endo dinucleotide closes at its own, DIFFERENT, torsions`() {
        val north = MeasuredDinucleotide.A_NORTH.residues()
        val step = closePhosphodiester(north.first, north.second)
        assert(step.minimumStrainZ < 1.0)
        assert(step.closes)
        assert(angularDistance(step.torsions.delta, NucleotideTemplate.A_NORTH_NEXT.torsions.delta) < 2.0)
        // delta CARRIES the pucker, so a north step's delta is far from a south step's
        assert(angularDistance(step.torsions.delta, measuredStepClosure().torsions.delta) > 30.0)
    }

    @Test
    fun `gate 2 limiting - a rigid residue carries its own pucker, so delta and chi are the template's`() {
        val residue = PlacedResidue(
            DuplexSite(Vector3(1.0, 0.0, 0.0), Vector3.ZERO, Vector3.UNIT_Z, 1),
            NucleotideTemplate.B_SOUTH
        )
        assert(residue.delta.isCloseTo(NucleotideTemplate.B_SOUTH.torsions.delta, 1e-6))
        assert(residue.chi.isCloseTo(NucleotideTemplate.B_SOUTH.torsions.chi, 1e-6))
        // delta CARRIES the pucker: the north template's is ~50 degrees smaller.
        assert(NucleotideTemplate.A_NORTH.torsions.delta < NucleotideTemplate.B_SOUTH.torsions.delta)
        assert(NucleotideTemplate.A_NORTH.phase in NucleotideTemplate.A_NORTH.pucker.phaseRange)
        assert(NucleotideTemplate.B_SOUTH.phase in NucleotideTemplate.B_SOUTH.pucker.phaseRange)
    }

    @Test
    fun `gate 2 limiting - the pinned reading adds two residuals and both close a real step`() {
        val pinned = measuredStepClosure(PhosphateReading.PINNED)
        val free = measuredStepClosure(PhosphateReading.FREE)
        // PINNED holds the bridging phosphorus where the closure search marked it, which adds the
        // P-O5' bond and the P-O5'-C5' angle to the three residuals FREE carries.
        assert(pinned.covalentZ.size == free.covalentZ.size + 2)
        assert(free.covalentZ.size == 3)
        // A REAL dinucleotide has both phosphorus atoms in their real places, so both readings
        // close it. Nothing weaker would license using either as an instrument.
        assert(free.closes)
        assert(pinned.closes)
    }

    @Test
    fun `gate 2 limiting - a link beyond the chain's reach is excluded with no solve at all`() {
        val donor = PlacedResidue(
            DuplexSite(Vector3(1.0, 0.0, 0.0), Vector3.ZERO, Vector3.UNIT_Z, 1),
            NucleotideTemplate.B_SOUTH
        )
        val far = PlacedResidue(
            DuplexSite(Vector3(51.0, 0.0, 0.0), Vector3(50.0, 0.0, 0.0), Vector3.UNIT_Z, 1),
            NucleotideTemplate.B_SOUTH
        )
        val bound = linkReach(donor, far)
        assert(!bound.freeFeasible)
        assert(!bound.pinnedFeasible)
        assert(bound.o3ToC5 > PhosphodiesterGeometry.reachMaximumTolerant)
    }

    @Test
    fun `gate 2 limiting - a real step passes both cheap bounds, which is what makes them bounds`() {
        val (donor, acceptor) = MeasuredDinucleotide.B_SOUTH.residues()
        val bound = linkReach(donor, acceptor)
        assert(bound.freeFeasible)
        assert(bound.pinnedFeasible)
        assert(bound.o3ToC5 >= PhosphodiesterGeometry.reachMinimumTolerant)
        assert(bound.o3ToC5 <= PhosphodiesterGeometry.reachMaximumTolerant)
    }

    // ------------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 symmetry - every torsion and every residual is invariant under a rigid motion`() {
        val rotation = rotationAbout(Vector3(0.31, -0.77, 0.55).normalized, 1.234)
        val shift = Vector3(-3.1, 7.7, 0.9)
        val plain = measuredStepClosure()
        val moved = measuredStepClosure(transform = { v -> rotation * v + shift })
        assert(moved.torsions.alpha.isCloseTo(plain.torsions.alpha, 1e-7))
        assert(moved.torsions.beta.isCloseTo(plain.torsions.beta, 1e-7))
        assert(moved.torsions.gamma.isCloseTo(plain.torsions.gamma, 1e-7))
        assert(moved.torsions.epsilon.isCloseTo(plain.torsions.epsilon, 1e-7))
        assert(moved.torsions.zeta.isCloseTo(plain.torsions.zeta, 1e-7))
        assert(moved.worstCovalentZ.isCloseTo(plain.worstCovalentZ, 1e-7))
    }

    @Test
    fun `gate 3 symmetry - a placed residue keeps the template's own internal geometry exactly`() {
        val site = DuplexSite(Vector3(0.0, 1.0, 0.4), Vector3(0.0, 0.0, 0.4), Vector3.UNIT_X, -1)
        val residue = PlacedResidue(site, NucleotideTemplate.B_SOUTH)
        val template = NucleotideTemplate.B_SOUTH
        val internal = { a: String, b: String ->
            (template.atoms.getValue(a).toVector() - template.atoms.getValue(b).toVector()).length
        }
        assert((residue["P"] - residue["O5'"]).length.isCloseTo(internal("P", "O5'"), 1e-12))
        assert((residue["C3'"] - residue["O3'"]).length.isCloseTo(internal("C3'", "O3'"), 1e-12))
        // and the phosphorus lands exactly where the closure search put it
        assert((residue["P"] - site.phosphate).length.isCloseTo(0.0, 1e-12))
        // the template is a REAL nucleotide, so its internal bonds are a molecule's
        assert(template.o5c5Bond > 0.138 && template.o5c5Bond < 0.148)
        assert(internal("P", "O5'") > 0.155 && internal("P", "O5'") < 0.165)
    }

    @Test
    fun `gate 3 symmetry - the local frame is right-handed for BOTH strand polarities`() {
        listOf(1, -1).forEach { polarity ->
            val frame =
                DuplexSite(Vector3(1.0, 0.0, 0.0), Vector3.ZERO, Vector3.UNIT_Z, polarity).frame
            assert(frame.radial.dot(frame.tangential).isCloseTo(0.0, 1e-12))
            assert(frame.tangential.dot(frame.axial).isCloseTo(0.0, 1e-12))
            assert(frame.radial.cross(frame.tangential).dot(frame.axial).isCloseTo(1.0))
            assert(frame.axial.dot(Vector3.UNIT_Z).isCloseTo(polarity.toDouble()))
        }
    }

    @Test
    fun `gate 3 symmetry - the conformer metric is a metric on the circle`() {
        val survey = BDnaConformerSurvey
        val first = survey.classes.first().centre
        val last = survey.classes.last().centre
        assert(survey.distance(first, first).isCloseTo(0.0, 1e-12))
        assert(survey.distance(first, last).isCloseTo(survey.distance(last, first), 1e-12))
        assert(survey.distance(first, first.shiftedBy(360.0)).isCloseTo(0.0, 1e-9))
        assert(survey.distance(first, first.shiftedBy(-720.0)).isCloseTo(0.0, 1e-9))
        // the classes partition the residues, so their fractions sum to one
        assert(survey.classes.sumOf { it.fraction }.isCloseTo(1.0, 1e-6))
        // and the metric is the WORST torsion, never an average
        assert(survey.distance(first, first.shiftedBy(0.0)) <= survey.distance(first, last))
    }

    @Test
    fun `gate 3 symmetry - wrapping is idempotent and folds into the half-open turn`() {
        listOf(0.0, 179.9, 180.0, 180.1, -180.0, 359.0, -721.0, 1234.5).forEach { angle ->
            val folded = wrapDegrees(angle)
            assert(folded > -180.0 && folded <= 180.0)
            assert(wrapDegrees(folded).isCloseTo(folded, 1e-12))
            assert(angularDistance(angle, folded).isCloseTo(0.0, 1e-9))
        }
    }

    // ------------------------------------------------------- gate 4: numerical convergence

    @Test
    fun `gate 4 convergence - the closure solve is converged in its grid and repeats identically`() {
        val coarse = measuredStepClosure(gridSteps = 60)
        val fine = measuredStepClosure(gridSteps = 240)
        val repeat = measuredStepClosure(gridSteps = 240)
        assert(abs(fine.worstCovalentZ - coarse.worstCovalentZ) < 0.05)
        assert(fine.worstCovalentZ == repeat.worstCovalentZ)
        assert(fine.torsions.gamma == repeat.torsions.gamma)
    }

    @Test
    fun `gate 4 convergence - the survey behind every constant is large enough to have percentiles`() {
        assert(PhosphodiesterGeometry.surveyLinkages > 1000)
        assert(BDnaConformerSurvey.residues > 1000)
        assert(NucleotideTemplate.B_SOUTH.population > 30)
        assert(NucleotideTemplate.A_NORTH.population > 30)
        assert(MeasuredBackbone.ENTRIES_USED > 100)
    }

    // ------------------------------------------------------- gate 5: literature and upstream

    @Test
    fun `gate 5 literature - the measured linkage reproduces the refinement restraint library`() {
        // Parkinson et al. (1996), read from Kowiel, Brzezinski & Jaskolski, NAR 44:8479 (2016)
        // Table 3, which reproduces it verbatim: P-O3' 1.607(12) A, P-O5' 1.593(10) A,
        // C3'-O3'-P 119.7(12) deg, O3'-P-O5' 104.0(19) deg, P-O5'-C5' 120.9(16) deg.
        assert(abs(PhosphodiesterGeometry.O3_P_BOND - 0.1607) < 0.0012)
        assert(abs(PhosphodiesterGeometry.P_O5_BOND - 0.1593) < 0.0010)
        assert(abs(PhosphodiesterGeometry.ANGLE_C3_O3_P - 119.7) < 2.0)
        assert(abs(PhosphodiesterGeometry.ANGLE_O3_P_O5 - 104.0) < 2.0)
        assert(abs(PhosphodiesterGeometry.ANGLE_P_O5_C5 - 120.9) < 2.0)
    }

    @Test
    fun `gate 5 literature - the canonical BI backbone lands where Svozil's survey says it does`() {
        // Svozil, Kalina, Omelka & Schneider, NAR 36:3690 (2008) Table 3, BI of 118 naked
        // B-DNA structures, folded from their 0..360 convention: alpha -61.0, beta -180.7,
        // gamma 48.4, delta 132.8, epsilon -178.3, zeta -96.8, chi -109.7.
        val k1 = BDnaConformerSurvey.classes.first().centre
        assert(angularDistance(k1.alpha, -61.0) < 10.0)
        assert(angularDistance(k1.beta, -180.7) < 10.0)
        assert(angularDistance(k1.gamma, 48.4) < 10.0)
        assert(angularDistance(k1.delta, 132.8) < 10.0)
        assert(angularDistance(k1.epsilon, -178.3) < 10.0)
        assert(angularDistance(k1.zeta, -96.8) < 10.0)
        assert(angularDistance(k1.chi, -109.7) < 10.0)
    }

    @Test
    fun `gate 5 literature - the pucker to P-P coupling is MEASURED here, not inherited`() {
        // `C-0029` cites Bosco et al. for "C3-endo 0.6 nm to C2-endo 0.7 nm" — a pair whose own
        // primary source is a textbook. Measured on 13 084 linkages, the ordering and the scale
        // are reproduced: north below south, both inside the cited window's neighbourhood.
        assert(PhosphodiesterGeometry.stepNorth < PhosphodiesterGeometry.stepSouth)
        assert(abs(PhosphodiesterGeometry.stepNorth - BForm.PHOSPHODIESTER_STEP_MIN) < 0.02)
        assert(abs(PhosphodiesterGeometry.stepSouth - BForm.PHOSPHODIESTER_STEP) < 0.05)
        // and delta carries the pucker: the north template's delta is 40-60 degrees below.
        val drop = NucleotideTemplate.B_SOUTH.torsions.delta - NucleotideTemplate.A_NORTH.torsions.delta
        assert(drop > 35.0 && drop < 65.0)
    }

    @Test
    fun `gate 5 literature - the measured phosphate radius sits inside C-0029's own bracket`() {
        // `C-0029` adopts 1.00 nm (Hedley et al.) and carries 0.90 nm as the other end.
        assert(NucleotideTemplate.B_SOUTH.phosphateRadius > BForm.PHOSPHATE_RADIUS_NARROW - 0.02)
        assert(NucleotideTemplate.B_SOUTH.phosphateRadius < BForm.PHOSPHATE_RADIUS)
        assert(MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS < BForm.PHOSPHATE_RADIUS)
    }

    @Test
    fun `gate 5 upstream - the stylised duplex's own intrastrand step, which every search assumes`() {
        assert(stylisedIntrastrandStep(backbone).isCloseTo(0.67265, 1e-4))
        // it is sqrt(rise^2 + chord^2), so it grows with both
        assert(
            stylisedIntrastrandStep(backbone).isCloseTo(
                sqrt(
                    backbone.risePerBasePair * backbone.risePerBasePair +
                            (2.0 * backbone.phosphateRadius *
                                    sin(PI / backbone.basePairsPerTurn)).let { it * it }
                )
            )
        )
    }

    @Test
    fun `gate 5 upstream - C-0029's own closure still reproduces at 0-600 nm`() {
        val single = bestTwoLinkClosure(topology = RoutingTopology.SCAFFOLD_EXCURSION)
        assert(single.covalent)
        assert(abs(single.worstGap - 0.600) < 5e-4)
    }

    // ------------------------------------------------------------------------- helpers

    /** The measured dinucleotide, optionally moved rigidly, as the closure sees it. */
    private fun measuredStepClosure(
        reading: PhosphateReading = PhosphateReading.FREE,
        gridSteps: Int = 180,
        transform: (Vector3) -> Vector3 = { it }
    ): LinkClosure {
        val step = MeasuredDinucleotide.B_SOUTH
        val (plainDonor, plainAcceptor) = step.residues()
        fun moved(residue: PlacedResidue): PlacedResidue {
            val site = residue.site
            return PlacedResidue(
                DuplexSite(
                    transform(site.phosphate), transform(site.axisPoint),
                    transform(site.axisPoint + site.axisDirection) - transform(site.axisPoint),
                    site.polarity
                ),
                residue.template
            )
        }
        return closePhosphodiester(moved(plainDonor), moved(plainAcceptor), reading, gridSteps)
    }

    private fun rotationAbout(axis: Vector3, angle: Double): Matrix33 {
        val c = cos(angle)
        val s = sin(angle)
        val t = 1.0 - c
        val x = axis.x
        val y = axis.y
        val z = axis.z
        return Matrix33(
            t * x * x + c, t * x * y - s * z, t * x * z + s * y,
            t * x * y + s * z, t * y * y + c, t * y * z - s * x,
            t * x * z - s * y, t * y * z + s * x, t * z * z + c
        )
    }
}
