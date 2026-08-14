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

import org.openrndr.math.Vector3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Task `T-71` — **whether the DIHEDRALS of `C-0029`'s closed routing close**, and not merely the
 * phosphate distances.
 *
 * ## What every closure result in this programme has tested, and what it has not
 *
 * `C-0029`, `C-0042` and `C-0052` all test one **necessary** condition and all three say so: a
 * phosphate pair inside the measured `[0.60, 0.70]` nm phosphodiester step, with no van der Waals
 * overlap. **No backbone torsion angle is checked.** A *"closes"* verdict is an upper bound on
 * buildability; only a *"does not close"* verdict is a proof of impossibility.
 *
 * This file supplies the missing half. Both residues of a link are rigid bodies placed by the
 * junction's own solved geometry; the phosphodiester between them is closed by exact inverse
 * kinematics; and the seven torsions that result are judged against a **measured** distribution
 * taken from crystallographic coordinates ([MeasuredBackbone]) rather than against a force field.
 *
 * ## The counting that makes the residual inevitable
 *
 * The relative placement of two rigid residues is **6** degrees of freedom. The phosphodiester
 * chain between them carries **5** torsions — `ε, ζ, α, β, γ` — with ideal bond lengths and
 * angles. So a closure is **over-determined by exactly one**, and some residual is expected even
 * for a perfectly good junction. That is why the verdict is quoted against a **baseline**: the
 * same pipeline run on a step *inside* an ideal duplex.
 *
 * Two readings are carried and they answer different questions.
 *
 * - [PhosphateReading.PINNED] takes the closure searches' own criterion literally: the two
 *   phosphorus atoms the search matched **are** the two phosphorus atoms of one step. Then the
 *   bridging phosphorus is fixed, only `γ` is free, and five residuals must be met.
 * - [PhosphateReading.FREE] is the generous reading: the bridging phosphorus sits where chemistry
 *   puts it, not where the search marked it. Then `γ` and `β` are free and three residuals remain.
 *   **This is the reading a verdict of "does not close" must be based on**, because it is the one
 *   that cannot be blamed on the marker.
 *
 * ## Units and conventions
 *
 * Lengths **nm**, angles **degrees**, IUPAC sign convention, torsions folded to `(−180, 180]`.
 * A residue's local frame is `(ê_r, ê_t, ê_z)` on its own phosphorus, `ê_z` along the duplex axis
 * oriented **5′→3′ for that strand**, `ê_r` radially outward, `ê_t = ê_z × ê_r`. The two strands
 * of a duplex differ by the sign of `ê_z`, which flips `ê_t` with it — a proper rotation, so one
 * template serves both, and **polarity is swept rather than assumed**.
 */

// ---------------------------------------------------------------- small angular geometry

/** Folds an angle in degrees into `(−180, 180]`. */
fun wrapDegrees(angle: Double): Double {
    var folded = angle % 360.0
    if (folded > 180.0) folded -= 360.0
    if (folded <= -180.0) folded += 360.0
    return folded
}

/** The unsigned separation of two angles in degrees, in `[0, 180]`. */
fun angularDistance(first: Double, second: Double): Double = abs(wrapDegrees(first - second))

/** The bond angle `a–b–c` in degrees. */
fun bondAngleDegrees(a: Vector3, b: Vector3, c: Vector3): Double {
    val first = a - b
    val second = c - b
    require(first.length > 0.0 && second.length > 0.0) {
        "a bond angle needs three distinct points"
    }
    val cosine = first.dot(second) / (first.length * second.length)
    return acos(min(1.0, max(-1.0, cosine))) * 180.0 / PI
}

/** The signed dihedral `a–b–c–d` in degrees, IUPAC sign convention. */
fun torsionDegrees(a: Vector3, b: Vector3, c: Vector3, d: Vector3): Double {
    val b0 = a - b
    val b1 = c - b
    val b2 = d - c
    require(b1.length > 0.0) { "a torsion needs a non-degenerate central bond" }
    val axis = b1.normalized
    val v = b0 - axis * b0.dot(axis)
    val w = b2 - axis * b2.dot(axis)
    return atan2(axis.cross(v).dot(w), v.dot(w)) * 180.0 / PI
}

/**
 * Places a fourth atom `d` on the chain `a–b–c–d` at the given bond length from `c`, bond angle
 * `b–c–d` and torsion `a–b–c–d`. Natural extension reference frame, exact.
 */
fun placeAtom(
    a: Vector3,
    b: Vector3,
    c: Vector3,
    bond: Double,
    angleDegrees: Double,
    torsionDegrees: Double
): Vector3 {
    require(bond > 0.0) { "bond must be positive, was: $bond" }
    require(angleDegrees > 0.0 && angleDegrees < 180.0) {
        "angleDegrees must lie strictly inside (0, 180), was: $angleDegrees"
    }
    val bc = (c - b)
    require(bc.length > 0.0) { "b and c must be distinct" }
    val unitBc = bc.normalized
    val ab = b - a
    var normal = ab.cross(unitBc)
    require(normal.length > 1.0e-12) { "a, b and c must not be collinear" }
    normal = normal.normalized
    val inPlane = normal.cross(unitBc)
    val theta = angleDegrees * PI / 180.0
    val phi = torsionDegrees * PI / 180.0
    // d = c + bond * ( -cosθ · û_bc + sinθ·cosφ · û_inPlane + sinθ·sinφ · û_normal )
    return c + (unitBc * (-cos(theta)) + inPlane * (sin(theta) * cos(phi)) +
            normal * (sin(theta) * sin(phi))) * bond
}

// ---------------------------------------------------------------- the measured covalent geometry

/**
 * The covalent geometry of one phosphodiester linkage, **measured** on 13 084 linkages of
 * crystallographic DNA rather than cited.
 *
 * It agrees with the restraint library the field refines against — Parkinson et al. (1996) give
 * `P–O3′ 1.607(12) Å`, `P–O5′ 1.593(10) Å`, `C3′–O3′–P 119.7(12)°`, `O3′–P–O5′ 104.0(19)°`,
 * `P–O5′–C5′ 120.9(16)°`, read here from Kowiel, Brzezinski & Jaskolski, *NAR* **44**:8479 (2016)
 * Table 3, which reproduces them — and the agreement is asserted as a gate-5 test.
 */
object PhosphodiesterGeometry {

    /** `O3′–P` bond, nm. */
    const val O3_P_BOND: Double = MeasuredBackbone.O3_P_BOND
    const val O3_P_BOND_SD: Double = MeasuredBackbone.O3_P_BOND_SD

    /** `P–O5′` bond, nm. */
    const val P_O5_BOND: Double = MeasuredBackbone.P_O5_BOND
    const val P_O5_BOND_SD: Double = MeasuredBackbone.P_O5_BOND_SD

    const val ANGLE_C3_O3_P: Double = MeasuredBackbone.ANGLE_C3_O3_P
    const val ANGLE_C3_O3_P_SD: Double = MeasuredBackbone.ANGLE_C3_O3_P_SD
    const val ANGLE_O3_P_O5: Double = MeasuredBackbone.ANGLE_O3_P_O5
    const val ANGLE_O3_P_O5_SD: Double = MeasuredBackbone.ANGLE_O3_P_O5_SD
    const val ANGLE_P_O5_C5: Double = MeasuredBackbone.ANGLE_P_O5_C5
    const val ANGLE_P_O5_C5_SD: Double = MeasuredBackbone.ANGLE_P_O5_C5_SD

    /** `O5′–C5′`, taken from the template's own medoid, nm. */
    const val O5_C5_BOND: Double = 0.14098

    /** How many measured linkages the constants rest on. */
    const val surveyLinkages: Int = MeasuredBackbone.LINKAGES

    /** The measured intrastrand `P···P` step at the **C2′-endo (south)** pucker, nm. */
    const val stepSouth: Double = MeasuredBackbone.STEP_SOUTH

    /** The measured intrastrand `P···P` step at the **C3′-endo (north)** pucker, nm. */
    const val stepNorth: Double = MeasuredBackbone.STEP_NORTH

    /** How many standard deviations of covalent strain a link may carry and still be called closed. */
    const val STRAIN_CEILING: Double = 3.0

    /**
     * The distance from `O3′` to `C5′` across the chain `O3′–P–O5′–C5′` at torsion `α`, in nm —
     * **the cheap bound, closed form**. Every bond and both intervening angles are at their
     * measured means, so the only freedom left is `α`, and a link whose own `O3′···C5′` distance
     * lies outside `[reachMinimum, reachMaximum]` **cannot close at any torsion whatever**.
     */
    fun reachAt(alphaDegrees: Double): Double {
        val o3 = Vector3.ZERO
        val p = Vector3(O3_P_BOND, 0.0, 0.0)
        val theta = ANGLE_O3_P_O5 * PI / 180.0
        val o5 = p + Vector3(-cos(theta), sin(theta), 0.0) * P_O5_BOND
        val c5 = placeAtom(o3, p, o5, O5_C5_BOND, ANGLE_P_O5_C5, alphaDegrees)
        return (c5 - o3).length
    }

    /** The `α = 0` (cis) end of [reachAt]. */
    val reachMinimum: Double by lazy { reachAt(0.0) }

    /** The `α = 180` (trans) end of [reachAt]. */
    val reachMaximum: Double by lazy { reachAt(180.0) }

    /**
     * [reachMinimum] widened by [STRAIN_CEILING] standard deviations on every bond and angle it is
     * built from — the reading the exclusion is taken on, so that no link is excluded by a
     * tolerance the crystallography itself does not respect.
     */
    val reachMinimumTolerant: Double by lazy { tolerantReach(0.0, -1.0) }

    /** [reachMaximum] widened the same way. */
    val reachMaximumTolerant: Double by lazy { tolerantReach(180.0, 1.0) }

    private fun tolerantReach(alphaDegrees: Double, sign: Double): Double {
        val k = sign * STRAIN_CEILING
        val o3 = Vector3.ZERO
        val p = Vector3(O3_P_BOND + k * O3_P_BOND_SD, 0.0, 0.0)
        // Opening both angles lengthens the trans reach and closing both shortens the cis one.
        val theta = (ANGLE_O3_P_O5 + k * ANGLE_O3_P_O5_SD) * PI / 180.0
        val o5 = p + Vector3(-cos(theta), sin(theta), 0.0) * (P_O5_BOND + k * P_O5_BOND_SD)
        val c5 = placeAtom(
            o3, p, o5, O5_C5_BOND, ANGLE_P_O5_C5 + k * ANGLE_P_O5_C5_SD, alphaDegrees
        )
        return (c5 - o3).length
    }
}

// ---------------------------------------------------------------- the nucleotide template

/** Which side of the pseudorotation wheel a sugar sits on. */
enum class SugarPucker(val description: String, val phaseRange: ClosedFloatingPointRange<Double>) {

    /** C2′-endo, `144° ≤ P ≤ 190°` (Kowiel et al. 2020) — the B-form pucker. */
    SOUTH("C2′-endo", 144.0..190.0),

    /** C3′-endo, `0° ≤ P ≤ 36°` — the A-form pucker, and the one the 0.60 nm link demands. */
    NORTH("C3′-endo", 0.0..36.0)
}

/** One atom of a template, in its residue's local frame, in nm. */
data class TemplateAtom(val radial: Double, val tangential: Double, val axial: Double) {

    fun toVector(): Vector3 = Vector3(radial, tangential, axial)
}

/** The seven torsions that describe a nucleotide's backbone and its glycosidic bond, in degrees. */
data class BackboneTorsions(
    val alpha: Double,
    val beta: Double,
    val gamma: Double,
    val delta: Double,
    val epsilon: Double,
    val zeta: Double,
    val chi: Double
) {

    val values: List<Double> get() = listOf(alpha, beta, gamma, delta, epsilon, zeta, chi)

    fun shiftedBy(degrees: Double): BackboneTorsions = BackboneTorsions(
        alpha + degrees, beta + degrees, gamma + degrees, delta + degrees,
        epsilon + degrees, zeta + degrees, chi + degrees
    )

    fun folded(): BackboneTorsions = BackboneTorsions(
        wrapDegrees(alpha), wrapDegrees(beta), wrapDegrees(gamma), wrapDegrees(delta),
        wrapDegrees(epsilon), wrapDegrees(zeta), wrapDegrees(chi)
    )

    companion object {

        val NAMES: List<String> = listOf("alpha", "beta", "gamma", "delta", "epsilon", "zeta", "chi")
    }
}

/**
 * A rigid nucleotide, in the local frame of its own phosphorus.
 *
 * **It is one real measured nucleotide, not an average**: the medoid of its population. An average
 * of coordinates carried in noisy frames contracts every internal bond, and a template whose
 * `C5′–O5′` bond is short by 3 % is not a molecule.
 */
data class NucleotideTemplate(
    val label: String,
    val pucker: SugarPucker,
    val population: Int,
    val source: String,
    val phase: Double,
    val twist: Double,
    val rise: Double,
    val phosphateRadius: Double,
    val atoms: Map<String, TemplateAtom>,
    val torsions: BackboneTorsions
) {

    /** `C5′–O5′` in this template, nm — the bond `γ` rotates about lies next to it. */
    val o5c5Bond: Double
        get() = (atoms.getValue("O5'").toVector() - atoms.getValue("C5'").toVector()).length

    /** The `C4′–C5′–O5′` angle in this template, degrees. */
    val c4c5o5Angle: Double
        get() = bondAngleDegrees(
            atoms.getValue("C4'").toVector(),
            atoms.getValue("C5'").toVector(),
            atoms.getValue("O5'").toVector()
        )

    companion object {

        private fun atomsOf(raw: Map<String, Triple<Double, Double, Double>>) =
            raw.mapValues { (_, v) -> TemplateAtom(v.first, v.second, v.third) }

        /** The **B-form, C2′-endo** template — the pucker `C-0042` and `C-0052` are pinned at. */
        val B_SOUTH: NucleotideTemplate = NucleotideTemplate(
            label = "B-form, C2′-endo (south)",
            pucker = SugarPucker.SOUTH,
            population = MeasuredBackbone.B_SOUTH_POPULATION,
            source = MeasuredBackbone.B_SOUTH_SOURCE,
            phase = MeasuredBackbone.B_SOUTH_PHASE,
            twist = MeasuredBackbone.B_SOUTH_TWIST,
            rise = MeasuredBackbone.B_SOUTH_RISE,
            phosphateRadius = MeasuredBackbone.B_SOUTH_PHOSPHATE_RADIUS,
            atoms = atomsOf(MeasuredBackbone.B_SOUTH_ATOMS),
            torsions = BackboneTorsions(
                MeasuredBackbone.B_SOUTH_ALPHA, MeasuredBackbone.B_SOUTH_BETA,
                MeasuredBackbone.B_SOUTH_GAMMA, MeasuredBackbone.B_SOUTH_DELTA,
                MeasuredBackbone.B_SOUTH_EPSILON, MeasuredBackbone.B_SOUTH_ZETA,
                MeasuredBackbone.B_SOUTH_CHI
            )
        )

        /** The **C3′-endo** template — the pucker `C-0029`'s 0.600 nm links demand. */
        val A_NORTH: NucleotideTemplate = NucleotideTemplate(
            label = "A-form, C3′-endo (north)",
            pucker = SugarPucker.NORTH,
            population = MeasuredBackbone.A_NORTH_POPULATION,
            source = MeasuredBackbone.A_NORTH_SOURCE,
            phase = MeasuredBackbone.A_NORTH_PHASE,
            twist = MeasuredBackbone.A_NORTH_TWIST,
            rise = MeasuredBackbone.A_NORTH_RISE,
            phosphateRadius = MeasuredBackbone.A_NORTH_PHOSPHATE_RADIUS,
            atoms = atomsOf(MeasuredBackbone.A_NORTH_ATOMS),
            torsions = BackboneTorsions(
                MeasuredBackbone.A_NORTH_ALPHA, MeasuredBackbone.A_NORTH_BETA,
                MeasuredBackbone.A_NORTH_GAMMA, MeasuredBackbone.A_NORTH_DELTA,
                MeasuredBackbone.A_NORTH_EPSILON, MeasuredBackbone.A_NORTH_ZETA,
                MeasuredBackbone.A_NORTH_CHI
            )
        )

        /** The residue that actually follows [B_SOUTH] in its own crystal structure. */
        val B_SOUTH_NEXT: NucleotideTemplate = B_SOUTH.copy(
            label = "B-form, C2′-endo (south), successor",
            phase = MeasuredBackbone.B_SOUTH_NEXT_PHASE,
            phosphateRadius = MeasuredBackbone.B_SOUTH_NEXT_RADIUS,
            atoms = atomsOf(MeasuredBackbone.B_SOUTH_NEXT_ATOMS),
            torsions = BackboneTorsions(
                MeasuredBackbone.B_SOUTH_NEXT_ALPHA, MeasuredBackbone.B_SOUTH_NEXT_BETA,
                MeasuredBackbone.B_SOUTH_NEXT_GAMMA, MeasuredBackbone.B_SOUTH_NEXT_DELTA,
                MeasuredBackbone.B_SOUTH_NEXT_EPSILON, MeasuredBackbone.B_SOUTH_NEXT_ZETA,
                MeasuredBackbone.B_SOUTH_NEXT_CHI
            )
        )

        /** The residue that actually follows [A_NORTH] in its own crystal structure. */
        val A_NORTH_NEXT: NucleotideTemplate = A_NORTH.copy(
            label = "A-form, C3′-endo (north), successor",
            phase = MeasuredBackbone.A_NORTH_NEXT_PHASE,
            phosphateRadius = MeasuredBackbone.A_NORTH_NEXT_RADIUS,
            atoms = atomsOf(MeasuredBackbone.A_NORTH_NEXT_ATOMS),
            torsions = BackboneTorsions(
                MeasuredBackbone.A_NORTH_NEXT_ALPHA, MeasuredBackbone.A_NORTH_NEXT_BETA,
                MeasuredBackbone.A_NORTH_NEXT_GAMMA, MeasuredBackbone.A_NORTH_NEXT_DELTA,
                MeasuredBackbone.A_NORTH_NEXT_EPSILON, MeasuredBackbone.A_NORTH_NEXT_ZETA,
                MeasuredBackbone.A_NORTH_NEXT_CHI
            )
        )

        /** The two templates a junction may be built from. The successors are baseline only. */
        val ALL: List<NucleotideTemplate> = listOf(B_SOUTH, A_NORTH)
    }
}

/**
 * **The free limiting case**, and it is a real molecule: the two residues of one measured
 * dinucleotide, placed exactly as the crystal has them.
 *
 * Reapplying a single template at a fitted screw does *not* reproduce a real step — the local
 * helical axis is only an approximation and the reconstructed `O3′···P` is out by ~0.015 nm, which
 * is eight bond-length standard deviations. So the calibration carries the medoid's **actual
 * successor**, in its own local frame about the same axis, at the actual azimuth and rise between
 * the two phosphorus atoms. The closure must then return that dinucleotide's own torsions at
 * essentially no strain, and if it does not the instrument is broken and no junction verdict may be
 * read off it.
 */
data class MeasuredDinucleotide(
    val first: NucleotideTemplate,
    val second: NucleotideTemplate,
    val firstRadius: Double,
    val secondRadius: Double,
    val stepTwist: Double,
    val stepRise: Double
) {

    /** The two residues, on a duplex axis along `+z` through the origin. */
    fun residues(): Pair<PlacedResidue, PlacedResidue> {
        val twist = stepTwist * PI / 180.0
        val donor = PlacedResidue(
            DuplexSite(
                Vector3(firstRadius, 0.0, 0.0), Vector3.ZERO, Vector3.UNIT_Z, 1
            ),
            first
        )
        val acceptor = PlacedResidue(
            DuplexSite(
                Vector3(secondRadius * cos(twist), secondRadius * sin(twist), stepRise),
                Vector3(0.0, 0.0, stepRise), Vector3.UNIT_Z, 1
            ),
            second
        )
        return Pair(donor, acceptor)
    }

    companion object {

        val B_SOUTH: MeasuredDinucleotide = MeasuredDinucleotide(
            NucleotideTemplate.B_SOUTH, NucleotideTemplate.B_SOUTH_NEXT,
            NucleotideTemplate.B_SOUTH.phosphateRadius, MeasuredBackbone.B_SOUTH_NEXT_RADIUS,
            MeasuredBackbone.B_SOUTH_STEP_TWIST, MeasuredBackbone.B_SOUTH_STEP_RISE
        )

        val A_NORTH: MeasuredDinucleotide = MeasuredDinucleotide(
            NucleotideTemplate.A_NORTH, NucleotideTemplate.A_NORTH_NEXT,
            NucleotideTemplate.A_NORTH.phosphateRadius, MeasuredBackbone.A_NORTH_NEXT_RADIUS,
            MeasuredBackbone.A_NORTH_STEP_TWIST, MeasuredBackbone.A_NORTH_STEP_RISE
        )
    }
}

// ---------------------------------------------------------------- placing a residue

/** The orthonormal local frame of a residue. */
data class ResidueFrame(val radial: Vector3, val tangential: Vector3, val axial: Vector3)

/**
 * Where one residue sits: its phosphorus, the axis of the duplex it belongs to, and which way that
 * strand runs.
 *
 * @property polarity `+1` when the strand's 5′→3′ direction is [axisDirection], `−1` when it is the
 *   other way. The two strands of one duplex are the two values, and the design may use either, so
 *   this is **swept** and never assumed.
 */
data class DuplexSite(
    val phosphate: Vector3,
    val axisPoint: Vector3,
    val axisDirection: Vector3,
    val polarity: Int
) {

    init {
        require(axisDirection.length > 1.0e-12) {
            "axisDirection must be a non-zero direction, was: $axisDirection"
        }
        require(polarity == 1 || polarity == -1) {
            "polarity must be +1 or -1, was: $polarity"
        }
    }

    /** `(ê_r, ê_t, ê_z)`, right-handed for **both** polarities. */
    val frame: ResidueFrame
        get() {
            val axial = axisDirection.normalized * polarity.toDouble()
            val offset = phosphate - axisPoint
            val radialVector = offset - axial * offset.dot(axial)
            require(radialVector.length > 1.0e-9) {
                "the phosphorus sits on the duplex axis, so the residue has no radial direction"
            }
            val radial = radialVector.normalized
            return ResidueFrame(radial, axial.cross(radial), axial)
        }
}

/** A [NucleotideTemplate] placed at a [DuplexSite]. */
data class PlacedResidue(val site: DuplexSite, val template: NucleotideTemplate) {

    /** The residue's frame, computed once. */
    val frame: ResidueFrame by lazy { site.frame }

    val atoms: Map<String, Vector3> by lazy {
        template.atoms.mapValues { (_, atom) -> place(atom) }
    }

    private fun place(atom: TemplateAtom): Vector3 =
        site.phosphate + frame.radial * atom.radial + frame.tangential * atom.tangential +
                frame.axial * atom.axial

    /**
     * One atom, without building the whole map.
     *
     * The cheap reach bound needs three atoms and is swept over hundreds of thousands of
     * placements, so it must not allocate a thirteen-entry map each time.
     */
    fun atom(name: String): Vector3 = place(template.atoms.getValue(name))

    operator fun get(name: String): Vector3 = atoms.getValue(name)

    /** `δ = C5′–C4′–C3′–O3′` — the torsion that **carries the pucker**, and a rigid residue's own. */
    val delta: Double get() = torsionDegrees(this["C5'"], this["C4'"], this["C3'"], this["O3'"])

    /** `χ`, the glycosidic torsion — also the residue's own, and untouched by any junction. */
    val chi: Double get() = torsionDegrees(this["O4'"], this["C1'"], this["NGLY"], this["CGLY"])
}

// ---------------------------------------------------------------- the populated regions

/** One measured conformer class of the DNA backbone. */
data class ConformerClass(
    val name: String,
    val population: Int,
    val fraction: Double,
    val centre: BackboneTorsions,
    val radius95: Double,
    val radius99: Double,
    val radiusMax: Double
)

/** How far a torsion set sits from the nearest observed conformer. */
data class ConformerMatch(
    val conformer: ConformerClass,
    val distance: Double,
    val inside: Boolean
)

/**
 * **The populated-region test**, marginal and non-parametric: how large a share of the
 * [MeasuredBackbone.RESIDUES] crystallographic residues have actually been *seen* with each torsion
 * near the value in question.
 *
 * A k-means class radius will not do this job. The diffuse classes have 99th-percentile radii above
 * 150°, so "inside the nearest class" admits almost any septet, and the baseline demonstrates it.
 * A ten-degree occupancy histogram instead lets the verdict make the statement it wants to make:
 * *"ε = −22° lies in a bin holding 0.02 % of 13 084 observed linkages."*
 */
object BDnaTorsionOccupancy {

    /**
     * The share of observed residues a torsion must have in its own ten-degree bin to count as
     * populated. Uniform occupancy would be 2.78 % per bin, so **0.1 % is permissive by 28×** —
     * deliberately, because the verdict must not rest on where a threshold was put.
     */
    const val FLOOR: Double = 0.001

    private val bins: Int = MeasuredBackbone.HISTOGRAM_BINS

    private val width: Double = 360.0 / bins

    /**
     * The seven histograms as shares rather than counts, resolved once into flat arrays.
     *
     * This lookup sits inside the closure solve's innermost loop — millions of evaluations per
     * link — so it must not go through a map or allocate a list.
     */
    private val shares: Array<DoubleArray> = BackboneTorsions.NAMES.map { name ->
        val counts = MeasuredBackbone.TORSION_HISTOGRAM.getValue(name)
        val total = MeasuredBackbone.TORSION_HISTOGRAM_TOTAL.getValue(name).toDouble()
        DoubleArray(counts.size) { counts[it] / total }
    }.toTypedArray()

    private fun share(index: Int, value: Double): Double {
        val folded = ((value + 180.0) % 360.0 + 360.0) % 360.0
        return shares[index][min(bins - 1, (folded / width).toInt())]
    }

    /** The share of observed residues whose [name] torsion lies in the same bin as [value]. */
    fun occupancy(name: String, value: Double): Double =
        share(BackboneTorsions.NAMES.indexOf(name), value)

    /** The occupancy of each of the seven torsions, in [BackboneTorsions.NAMES] order. */
    fun occupancies(torsions: BackboneTorsions): List<Double> =
        torsions.values.mapIndexed { index, value -> share(index, value) }

    /** The **least** populated of the seven — a conformation is unobserved if any torsion is. */
    fun minimumOccupancy(torsions: BackboneTorsions): Double = minimumOccupancy(
        torsions.alpha, torsions.beta, torsions.gamma, torsions.delta, torsions.epsilon,
        torsions.zeta, torsions.chi
    )

    /** The allocation-free form the solve's inner loop calls. */
    fun minimumOccupancy(
        alpha: Double,
        beta: Double,
        gamma: Double,
        delta: Double,
        epsilon: Double,
        zeta: Double,
        chi: Double
    ): Double {
        var least = share(0, alpha)
        least = min(least, share(1, beta))
        least = min(least, share(2, gamma))
        least = min(least, share(3, delta))
        least = min(least, share(4, epsilon))
        least = min(least, share(5, zeta))
        return min(least, share(6, chi))
    }

    /** Which torsion is the least populated one. */
    fun leastPopulated(torsions: BackboneTorsions): String {
        val values = occupancies(torsions)
        return BackboneTorsions.NAMES[values.indexOf(values.min())]
    }

    fun populated(torsions: BackboneTorsions): Boolean = minimumOccupancy(torsions) >= FLOOR
}

/**
 * The **populated regions** of the DNA backbone, as k-means classes over the observed septets of
 * `(α, β, γ, δ, ε, ζ, χ)` in [MeasuredBackbone.RESIDUES] crystallographic residues.
 *
 * The metric is the **largest** angular departure over the seven torsions, which is the honest one:
 * a conformation is unobserved if *any* of its torsions is unobserved, and averaging would let six
 * canonical angles hide one impossible one.
 */
object BDnaConformerSurvey {

    val residues: Int = MeasuredBackbone.RESIDUES

    val classes: List<ConformerClass> = MeasuredBackbone.CONFORMERS.mapIndexed { index, row ->
        ConformerClass(
            name = "K${index + 1}",
            population = row[0].toInt(),
            fraction = row[1],
            centre = BackboneTorsions(row[2], row[3], row[4], row[5], row[6], row[7], row[8]),
            radius95 = row[9],
            radius99 = row[10],
            radiusMax = row[11]
        )
    }

    /** The largest angular departure over the seven torsions, in degrees. */
    fun distance(first: BackboneTorsions, second: BackboneTorsions): Double =
        first.values.zip(second.values).maxOf { (a, b) -> angularDistance(a, b) }

    /** The nearest observed class, and whether the septet falls inside its 99th-percentile radius. */
    fun nearest(torsions: BackboneTorsions): ConformerMatch {
        var best = classes.first()
        var bestDistance = Double.MAX_VALUE
        classes.forEach { candidate ->
            val d = distance(torsions, candidate.centre)
            if (d < bestDistance) {
                bestDistance = d
                best = candidate
            }
        }
        return ConformerMatch(best, bestDistance, bestDistance <= best.radius99)
    }
}

// ---------------------------------------------------------------- the closure

/** Which of the two readings of the closure searches' own criterion a solve takes. */
enum class PhosphateReading(val description: String) {

    /** The searches' criterion taken literally: both matched phosphorus atoms are real. */
    PINNED("the bridging phosphorus is where the closure search marked it"),

    /** The generous reading: the bridging phosphorus sits where chemistry puts it. */
    FREE("the bridging phosphorus is free, only the two residues are placed")
}

/** One solved phosphodiester link. */
data class LinkClosure(
    val reading: PhosphateReading,
    val donorTemplate: String,
    val acceptorTemplate: String,
    val donorPolarity: Int,
    val acceptorPolarity: Int,
    val donorIsFirst: Boolean,
    val o3pBond: Double,
    val angleC3O3P: Double,
    val angleO3PO5: Double,
    val po5Bond: Double,
    val anglePO5C5: Double,
    val torsions: BackboneTorsions,
    val donorDelta: Double,
    val donorChi: Double,
    val covalentZ: List<Double>,
    val minimumStrainZ: Double,
    val occupancies: List<Double>,
    val leastPopulatedTorsion: String,
    val conformer: String,
    val conformerDistance: Double,
    val conformerRadius: Double
) {

    /**
     * The largest covalent departure **at the placement the solve returned**, in measured standard
     * deviations. It is not in general the smallest achievable — see [minimumStrainZ] — because the
     * solve looks for the best-*populated* closure under the ceiling, not the least-strained one.
     */
    val worstCovalentZ: Double get() = covalentZ.maxOf { abs(it) }

    val covalentAcceptable: Boolean
        get() = worstCovalentZ <= PhosphodiesterGeometry.STRAIN_CEILING

    /** The share of observed residues carrying the **rarest** of the seven torsions. */
    val minimumOccupancy: Double get() = occupancies.min()

    val torsionsPopulated: Boolean get() = minimumOccupancy >= BDnaTorsionOccupancy.FLOOR

    /** **The verdict**: the covalent geometry holds *and* the torsions are in a populated region. */
    val closes: Boolean get() = covalentAcceptable && torsionsPopulated
}

/** The cheap bound, which runs before any solve. */
data class LinkReachBound(
    val o3ToP: Double,
    val o3ToC5: Double,
    val pinnedFeasible: Boolean,
    val freeFeasible: Boolean
)

/**
 * **Bound 1 and bound 2, closed form.** `o3ToP` is what the pinned reading demands be one `O3′–P`
 * bond; `o3ToC5` is what the chain `O3′–P–O5′–C5′` must span, and a link outside
 * `[reachMinimumTolerant, reachMaximumTolerant]` cannot close **at any torsion**.
 */
fun linkReach(donor: PlacedResidue, acceptor: PlacedResidue): LinkReachBound {
    val o3 = donor.atom("O3'")
    val o3ToP = (acceptor.site.phosphate - o3).length
    val o3ToC5 = (acceptor.atom("C5'") - o3).length
    return LinkReachBound(
        o3ToP = o3ToP,
        o3ToC5 = o3ToC5,
        pinnedFeasible = abs(o3ToP - PhosphodiesterGeometry.O3_P_BOND) <=
                PhosphodiesterGeometry.STRAIN_CEILING * PhosphodiesterGeometry.O3_P_BOND_SD,
        freeFeasible = o3ToC5 >= PhosphodiesterGeometry.reachMinimumTolerant &&
                o3ToC5 <= PhosphodiesterGeometry.reachMaximumTolerant
    )
}

/**
 * Closes the phosphodiester from [donor]'s `O3′` to [acceptor]'s phosphorus.
 *
 * Deterministic by construction: a fixed grid over the free torsions, then a fixed number of
 * bracket halvings, with strict comparison so the lowest index wins every tie. No tolerance appears
 * in any control flow.
 */
fun closePhosphodiester(
    donor: PlacedResidue,
    acceptor: PlacedResidue,
    reading: PhosphateReading = PhosphateReading.FREE,
    gridSteps: Int = 180,
    refinements: Int = 6,
    donorIsFirst: Boolean = true
): LinkClosure {
    require(gridSteps >= 8) { "gridSteps must be at least eight, was: $gridSteps" }
    require(refinements >= 0) { "refinements must not be negative, was: $refinements" }

    val c4d = donor["C4'"]
    val c3d = donor["C3'"]
    val o3d = donor["O3'"]
    val c3a = acceptor["C3'"]
    val c4a = acceptor["C4'"]
    val c5a = acceptor["C5'"]
    val o5Bond = acceptor.template.o5c5Bond
    val o5Angle = acceptor.template.c4c5o5Angle
    val acceptorDelta = acceptor.delta
    val acceptorChi = acceptor.chi

    fun o5At(gamma: Double): Vector3 = placeAtom(c3a, c4a, c5a, o5Bond, o5Angle, gamma)

    fun phosphorusAt(gamma: Double, beta: Double): Vector3 = when (reading) {
        PhosphateReading.PINNED -> acceptor["P"]
        PhosphateReading.FREE -> placeAtom(
            c4a, c5a, o5At(gamma),
            PhosphodiesterGeometry.P_O5_BOND, PhosphodiesterGeometry.ANGLE_P_O5_C5, beta
        )
    }

    fun residualsAt(gamma: Double, beta: Double): List<Double> {
        val o5 = o5At(gamma)
        val p = phosphorusAt(gamma, beta)
        val common = listOf(
            ((p - o3d).length - PhosphodiesterGeometry.O3_P_BOND) /
                    PhosphodiesterGeometry.O3_P_BOND_SD,
            (bondAngleDegrees(c3d, o3d, p) - PhosphodiesterGeometry.ANGLE_C3_O3_P) /
                    PhosphodiesterGeometry.ANGLE_C3_O3_P_SD,
            (bondAngleDegrees(o3d, p, o5) - PhosphodiesterGeometry.ANGLE_O3_P_O5) /
                    PhosphodiesterGeometry.ANGLE_O3_P_O5_SD
        )
        return when (reading) {
            PhosphateReading.FREE -> common
            PhosphateReading.PINNED -> common + listOf(
                ((p - o5).length - PhosphodiesterGeometry.P_O5_BOND) /
                        PhosphodiesterGeometry.P_O5_BOND_SD,
                (bondAngleDegrees(p, o5, c5a) - PhosphodiesterGeometry.ANGLE_P_O5_C5) /
                        PhosphodiesterGeometry.ANGLE_P_O5_C5_SD
            )
        }
    }

    fun torsionsAt(gamma: Double, beta: Double): BackboneTorsions {
        val o5 = o5At(gamma)
        val p = phosphorusAt(gamma, beta)
        return BackboneTorsions(
            alpha = torsionDegrees(o3d, p, o5, c5a),
            beta = torsionDegrees(p, o5, c5a, c4a),
            gamma = torsionDegrees(o5, c5a, c4a, c3a),
            delta = acceptor.delta,
            epsilon = torsionDegrees(c4d, c3d, o3d, p),
            zeta = torsionDegrees(c3d, o3d, p, o5),
            chi = acceptor.chi
        ).folded()
    }

    /**
     * The objective is **lexicographic**, and it has to be.
     *
     * Ranking by covalent strain alone leaves the torsions undetermined wherever the residual has a
     * near-degenerate branch — a mirror pair of `(γ, β)` scores identically and the grid returns
     * whichever it meets first, which is not a physical statement. So: first drive the strain under
     * the ceiling; **then**, among the placements that satisfy it, take the one whose rarest
     * torsion is the most populated; and only then break the remaining tie on strain.
     *
     * This is also the right *question*. `T-71` asks whether a **populated** conformation closes
     * the junction, not whether the least-strained one happens to be populated.
     */
    fun scoreAt(gamma: Double, beta: Double): Triple<Double, Double, Double> {
        val o5 = o5At(gamma)
        val p = phosphorusAt(gamma, beta)
        var strain = abs(
            ((p - o3d).length - PhosphodiesterGeometry.O3_P_BOND) /
                    PhosphodiesterGeometry.O3_P_BOND_SD
        )
        strain = max(
            strain,
            abs(
                (bondAngleDegrees(c3d, o3d, p) - PhosphodiesterGeometry.ANGLE_C3_O3_P) /
                        PhosphodiesterGeometry.ANGLE_C3_O3_P_SD
            )
        )
        strain = max(
            strain,
            abs(
                (bondAngleDegrees(o3d, p, o5) - PhosphodiesterGeometry.ANGLE_O3_P_O5) /
                        PhosphodiesterGeometry.ANGLE_O3_P_O5_SD
            )
        )
        if (reading == PhosphateReading.PINNED) {
            strain = max(
                strain,
                abs(
                    ((p - o5).length - PhosphodiesterGeometry.P_O5_BOND) /
                            PhosphodiesterGeometry.P_O5_BOND_SD
                )
            )
            strain = max(
                strain,
                abs(
                    (bondAngleDegrees(p, o5, c5a) - PhosphodiesterGeometry.ANGLE_P_O5_C5) /
                            PhosphodiesterGeometry.ANGLE_P_O5_C5_SD
                )
            )
        }
        val excess = max(0.0, strain - PhosphodiesterGeometry.STRAIN_CEILING)
        val occupancy = BDnaTorsionOccupancy.minimumOccupancy(
            torsionDegrees(o3d, p, o5, c5a),
            torsionDegrees(p, o5, c5a, c4a),
            torsionDegrees(o5, c5a, c4a, c3a),
            acceptorDelta,
            torsionDegrees(c4d, c3d, o3d, p),
            torsionDegrees(c3d, o3d, p, o5),
            acceptorChi
        )
        return Triple(excess, -occupancy, strain)
    }

    fun better(candidate: Triple<Double, Double, Double>, incumbent: Triple<Double, Double, Double>):
            Boolean = when {
        candidate.first != incumbent.first -> candidate.first < incumbent.first
        candidate.second != incumbent.second -> candidate.second < incumbent.second
        else -> candidate.third < incumbent.third
    }

    var bestGamma = 0.0
    var bestBeta = 0.0
    var best = Triple(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)
    // Tracked beside the lexicographic optimum, because "the least strain this link can carry" and
    // "the least strain a POPULATED closure of this link carries" are different questions and the
    // claim needs both.
    var leastStrain = Double.MAX_VALUE
    var gammaHalf = 180.0
    var betaHalf = if (reading == PhosphateReading.PINNED) 0.0 else 180.0
    var gammaCentre = 0.0
    var betaCentre = 0.0
    val betaSteps = if (reading == PhosphateReading.PINNED) 1 else gridSteps

    repeat(refinements + 1) { round ->
        val gammaStep = 2.0 * gammaHalf / gridSteps
        val betaStep = if (betaSteps <= 1) 0.0 else 2.0 * betaHalf / betaSteps
        for (i in 0 until gridSteps) {
            val gamma = gammaCentre - gammaHalf + i * gammaStep
            for (j in 0 until betaSteps) {
                val beta = betaCentre - betaHalf + j * betaStep
                val value = scoreAt(gamma, beta)
                if (value.third < leastStrain) leastStrain = value.third
                if (better(value, best)) {
                    best = value
                    bestGamma = gamma
                    bestBeta = beta
                }
            }
        }
        if (round < refinements) {
            gammaCentre = bestGamma
            betaCentre = bestBeta
            gammaHalf = gammaStep
            betaHalf = betaStep
        }
    }

    val o5 = o5At(bestGamma)
    val p = phosphorusAt(bestGamma, bestBeta)
    val torsions = torsionsAt(bestGamma, bestBeta)
    val match = BDnaConformerSurvey.nearest(torsions)
    return LinkClosure(
        reading = reading,
        donorTemplate = donor.template.label,
        acceptorTemplate = acceptor.template.label,
        donorPolarity = donor.site.polarity,
        acceptorPolarity = acceptor.site.polarity,
        donorIsFirst = donorIsFirst,
        o3pBond = (p - o3d).length,
        angleC3O3P = bondAngleDegrees(c3d, o3d, p),
        angleO3PO5 = bondAngleDegrees(o3d, p, o5),
        po5Bond = (p - o5).length,
        anglePO5C5 = bondAngleDegrees(p, o5, c5a),
        torsions = torsions,
        donorDelta = donor.delta,
        donorChi = donor.chi,
        covalentZ = residualsAt(bestGamma, bestBeta),
        minimumStrainZ = leastStrain,
        occupancies = BDnaTorsionOccupancy.occupancies(torsions),
        leastPopulatedTorsion = BDnaTorsionOccupancy.leastPopulated(torsions),
        conformer = match.conformer.name,
        conformerDistance = match.distance,
        conformerRadius = match.conformer.radius99
    )
}

/** One end of a link, before the design has chosen a strand polarity or a pucker for it. */
data class ResidueAnchor(
    val name: String,
    val phosphate: Vector3,
    val axisPoint: Vector3,
    val axisDirection: Vector3
)

/**
 * The best closure of the link between [first] and [second], over **every** freedom the design
 * actually has: which body carries the donor `O3′`, which way each strand runs, and which sugar
 * pucker each residue takes.
 *
 * Nothing here is assumed. A 5′→3′ strand may cross the junction in either direction, both strands
 * of both duplexes are available, and the pucker is a per-residue choice — so a *"does not close"*
 * verdict has to survive all of them.
 */
/**
 * The same lexicographic order the solve uses, one level up: first get the covalent strain under
 * the ceiling, then take the best-populated torsions, then the least strain.
 */
private fun betterClosure(candidate: LinkClosure, incumbent: LinkClosure): Boolean {
    val ceiling = PhosphodiesterGeometry.STRAIN_CEILING
    val a = max(0.0, candidate.worstCovalentZ - ceiling)
    val b = max(0.0, incumbent.worstCovalentZ - ceiling)
    if (a != b) return a < b
    if (candidate.minimumOccupancy != incumbent.minimumOccupancy) {
        return candidate.minimumOccupancy > incumbent.minimumOccupancy
    }
    return candidate.worstCovalentZ < incumbent.worstCovalentZ
}

/**
 * The cheap bound over **every** assignment the design may choose — donor end, both polarities and
 * both puckers — reported at the assignment that violates the free-phosphorus reach interval least.
 *
 * Evaluating the bound only at the assignment the *solve* happened to pick would make a
 * *"cannot close"* verdict an artefact of the solve's own ranking. This makes the exclusion a
 * property of the geometry.
 */
fun bestLinkReach(
    first: ResidueAnchor,
    second: ResidueAnchor,
    templates: List<NucleotideTemplate> = NucleotideTemplate.ALL
): LinkReachBound {
    require(templates.isNotEmpty()) { "templates must not be empty" }
    var best: LinkReachBound? = null
    var bestViolation = Double.MAX_VALUE
    var anyPinned = false
    for (donorIsFirst in listOf(true, false)) {
        val donorAnchor = if (donorIsFirst) first else second
        val acceptorAnchor = if (donorIsFirst) second else first
        for (donorPolarity in listOf(1, -1)) {
            for (acceptorPolarity in listOf(1, -1)) {
                for (donorTemplate in templates) {
                    for (acceptorTemplate in templates) {
                        val bound = linkReach(
                            PlacedResidue(
                                DuplexSite(
                                    donorAnchor.phosphate, donorAnchor.axisPoint,
                                    donorAnchor.axisDirection, donorPolarity
                                ),
                                donorTemplate
                            ),
                            PlacedResidue(
                                DuplexSite(
                                    acceptorAnchor.phosphate, acceptorAnchor.axisPoint,
                                    acceptorAnchor.axisDirection, acceptorPolarity
                                ),
                                acceptorTemplate
                            )
                        )
                        if (bound.pinnedFeasible) anyPinned = true
                        val violation = max(
                            max(0.0, PhosphodiesterGeometry.reachMinimumTolerant - bound.o3ToC5),
                            max(0.0, bound.o3ToC5 - PhosphodiesterGeometry.reachMaximumTolerant)
                        )
                        if (violation < bestViolation) {
                            bestViolation = violation
                            best = bound
                        }
                    }
                }
            }
        }
    }
    val found = requireNotNull(best) { "no reach candidate was evaluated" }
    return found.copy(pinnedFeasible = anyPinned)
}

fun bestLinkClosure(
    first: ResidueAnchor,
    second: ResidueAnchor,
    reading: PhosphateReading = PhosphateReading.FREE,
    templates: List<NucleotideTemplate> = NucleotideTemplate.ALL,
    gridSteps: Int = 180,
    refinements: Int = 6
): LinkClosure {
    require(templates.isNotEmpty()) { "templates must not be empty" }
    var best: LinkClosure? = null
    for (donorIsFirst in listOf(true, false)) {
        val donorAnchor = if (donorIsFirst) first else second
        val acceptorAnchor = if (donorIsFirst) second else first
        for (donorPolarity in listOf(1, -1)) {
            for (acceptorPolarity in listOf(1, -1)) {
                for (donorTemplate in templates) {
                    for (acceptorTemplate in templates) {
                        val donor = PlacedResidue(
                            DuplexSite(
                                donorAnchor.phosphate, donorAnchor.axisPoint,
                                donorAnchor.axisDirection, donorPolarity
                            ),
                            donorTemplate
                        )
                        val acceptor = PlacedResidue(
                            DuplexSite(
                                acceptorAnchor.phosphate, acceptorAnchor.axisPoint,
                                acceptorAnchor.axisDirection, acceptorPolarity
                            ),
                            acceptorTemplate
                        )
                        val candidate = closePhosphodiester(
                            donor, acceptor, reading, gridSteps, refinements, donorIsFirst
                        )
                        val incumbent = best
                        if (incumbent == null || betterClosure(candidate, incumbent)) {
                            best = candidate
                        }
                    }
                }
            }
        }
    }
    return requireNotNull(best) { "no closure candidate was evaluated" }
}

/**
 * The intrastrand `P···P` step of one of this project's **stylised** duplexes, in nm.
 *
 * `√(rise² + (2 r_P sin(twist/2))²)` — 0.6728 nm at the square lattice's 10.67 bp/turn, a 0.34 nm
 * rise and the 1.00 nm phosphate radius `C-0029` adopts. Every closure search in this programme is
 * written on this geometry, so this is the number a junction's links must be read against.
 */
fun stylisedIntrastrandStep(backbone: DuplexBackbone = DuplexBackbone()): Double {
    val chord = 2.0 * backbone.phosphateRadius * sin(0.5 * backbone.twistPerBasePair)
    return sqrt(backbone.risePerBasePair * backbone.risePerBasePair + chord * chord)
}
