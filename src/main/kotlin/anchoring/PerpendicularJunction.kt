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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import org.openrndr.math.Vector3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Task `T-67` — whether a 90° scaffold or staple routing between a sheet duplex and a **normal
 * standoff** exists at all, at the level of which strand goes where.
 *
 * ## What `C-0028` left standing on nothing
 *
 * `C-0028` closed `T-40` with a design whose base is *"two antiparallel crossovers to the two
 * adjacent sheet duplexes, the pair laid ACROSS the flexure axis"*, worth
 * `k_θ_base = 2k_θ + 2k_s(d/2)² = 261.2 pN·nm/rad`. The `d/2 = 1.345 nm` in that expression is
 * **half the sheet's interhelical distance** — it is a statement about *where the two links are*,
 * and nothing in `T-40` checked that a duplex standing on end can put a covalent link there.
 *
 * ## The counting theorem, which is the whole method
 *
 * **A B-form duplex has two backbones, so a duplex END presents exactly two strand termini**, at
 * the two backbone positions of its terminal base pair. Every covalent link grounding the
 * standoff has to start at one of them. Therefore:
 *
 * 1. a base joint has **at most two** links, whatever the routing;
 * 2. their separation is the **terminal chord** `2 r_P sin(Δ/2)`, bounded above by the duplex's
 *    own diameter — so the lever arm is at most `r_P = 1.0 nm`, against the 1.345 nm `C-0028`'s
 *    `B2` assumes;
 * 3. two links on a chord react a moment as a **couple about the chord's perpendicular bisector
 *    only** — about the chord itself nothing is left but the two bonds' own hinge constants,
 *    which is `C-0028`'s `B1` exactly.
 *
 * No simulation can overturn a count. An atomistic or coarse-grained study could only find the
 * junction *additionally* frustrated; it cannot add a third backbone.
 *
 * ## What is modelled here and what is not
 *
 * The geometry below tests a **necessary** condition for a link — a phosphate pair inside the
 * phosphodiester step with no van der Waals overlap — and never a sufficient one: no torsion
 * angle is checked. A *"closes"* verdict is therefore an **upper bound on buildability** and a
 * *"does not close"* verdict is a proof of impossibility.
 */
object BForm {

    /**
     * The radius at which the phosphorus atoms of a B-form duplex sit, in nm — **CITED,
     * READ DIRECTLY**: Hedley, Coshic, Aksimentiev & Kornyshev, *Phys. Rev. X* **14**:031042
     * (2024), *"Phosphates (red circles) sit at a radius of a_DNA ≈ 10 Å, where φ_s is the
     * azimuthal width of the minor groove."*
     *
     * It coincides with the duplex's own steric radius [DUPLEX_RADIUS], which is not a
     * coincidence: B-form DNA's 2 nm diameter **is** the phosphate backbone. The narrower fibre
     * reading [PHOSPHATE_RADIUS_NARROW] is carried as the other end of the bracket.
     */
    const val PHOSPHATE_RADIUS: Double = 1.00

    /** The narrow fibre-model reading of [PHOSPHATE_RADIUS], carried as a bracket. */
    const val PHOSPHATE_RADIUS_NARROW: Double = 0.90

    /** The steric radius of a B-form duplex in nm — **CITED**, the standard 2 nm diameter. */
    const val DUPLEX_RADIUS: Double = 1.00

    /**
     * The azimuthal separation of the two backbones of one base pair, in degrees.
     *
     * This is the parameter the terminal chord — and therefore the whole base couple — is most
     * sensitive to, and it is a **convention** as much as a measurement: the two backbones are
     * separated by the minor groove on one side and the major groove on the other, and different
     * duplex models place the phosphate pair of a single base pair anywhere from ~120° to ~154°
     * apart. Both ends are carried and every result is reported across them.
     */
    const val MINOR_GROOVE_BACKBONE_ANGLE: Double = 120.0

    /** The wide reading of [MINOR_GROOVE_BACKBONE_ANGLE]. */
    const val MINOR_GROOVE_BACKBONE_ANGLE_WIDE: Double = 154.0

    /**
     * The largest intrastrand P–P distance one phosphodiester step spans, in nm — **CITED,
     * READ DIRECTLY**: Bosco, Camunas-Soler & Ritort, *Nucleic Acids Res.* **42**:2064 (2014),
     * *"a fraction of the deoxyriboses could interconvert from C3-endo (interphosphate distance
     * 0.6 nm) to C2-endo conformation (interphosphate distance 0.7 nm)"*.
     *
     * A gap beyond this needs unpaired nucleotides, and an unpaired nucleotide turns a covalent
     * link into a **chain** — which is `C-0028`'s `B5`, and `B5` fails `P1` at every length.
     */
    const val PHOSPHODIESTER_STEP: Double = 0.70

    /**
     * The **shortest** intrastrand P–P distance one phosphodiester step spans, in nm — the
     * C3′-endo end of the same measured pair.
     *
     * A link is not a distance but a **window**: a phosphate pair closer than this cannot be
     * joined by one step any more than a pair further apart than [PHOSPHODIESTER_STEP] can, and
     * treating the closure test as *"≤ 0.70 nm"* alone lets the search park on the van der Waals
     * floor, where no backbone exists.
     */
    const val PHOSPHODIESTER_STEP_MIN: Double = 0.60

    /** The closest two phosphates of different strands may approach, in nm — van der Waals. */
    const val PHOSPHATE_HARD_SEPARATION: Double = 0.35

    /** Base pairs per turn of the **square** lattice — `CLAUDE.md`, and not the honeycomb's. */
    const val BASE_PAIRS_PER_TURN_SQUARE: Double = 10.67

    /** Base pairs per turn of the **honeycomb** lattice. */
    const val BASE_PAIRS_PER_TURN_HONEYCOMB: Double = 10.5

    /**
     * **The counting theorem's constant.** A duplex has two backbones, so a duplex end has two
     * strand termini — no more, under any routing, in any lattice, at any sequence.
     */
    const val TERMINI_PER_DUPLEX_END: Int = 2
}

/**
 * The B-form backbone as a pair of coaxial phosphate helices, which is all the geometry the
 * counting theorem needs.
 *
 * @property risePerBasePair axial rise in nm.
 * @property basePairsPerTurn 10.67 on the square lattice, 10.5 on the honeycomb.
 * @property phosphateRadius radius of the phosphate helix in nm.
 * @property duplexRadius steric radius in nm.
 * @property minorGrooveAngle azimuthal separation of the two backbones of one base pair, degrees.
 */
data class DuplexBackbone(
    val risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    val basePairsPerTurn: Double = BForm.BASE_PAIRS_PER_TURN_SQUARE,
    val phosphateRadius: Double = BForm.PHOSPHATE_RADIUS,
    val duplexRadius: Double = BForm.DUPLEX_RADIUS,
    val minorGrooveAngle: Double = BForm.MINOR_GROOVE_BACKBONE_ANGLE
) {

    init {
        require(risePerBasePair > 0.0) {
            "risePerBasePair must be positive, was: $risePerBasePair"
        }
        require(basePairsPerTurn > 0.0) {
            "basePairsPerTurn must be positive, was: $basePairsPerTurn"
        }
        require(phosphateRadius >= 0.0) {
            "phosphateRadius must not be negative, was: $phosphateRadius"
        }
        require(duplexRadius > 0.0) { "duplexRadius must be positive, was: $duplexRadius" }
        require(minorGrooveAngle >= 0.0 && minorGrooveAngle <= 360.0) {
            "minorGrooveAngle must be an angle in degrees, was: $minorGrooveAngle"
        }
    }

    /** The twist per base pair in radians — and the **azimuthal quantum** of any junction. */
    val twistPerBasePair: Double get() = 2.0 * PI / basePairsPerTurn

    /**
     * The quantum in which a junction's azimuth can be chosen, in radians.
     *
     * The base chord's direction is set by **which base pair of the standoff carries the
     * junction**, and base pairs are integers, so the chord can be placed only on this lattice of
     * directions. `C-0015`'s 32 bp interface period is exactly three turns of it.
     */
    val azimuthQuantum: Double get() = twistPerBasePair

    /**
     * The chord between the **two strand termini of one duplex end**, in nm.
     *
     * `2 r_P sin(Δ/2)` — the single most consequential length in this task, because half of it
     * is the largest lever arm any base couple can have.
     */
    val terminalChord: Double
        get() = 2.0 * phosphateRadius * sin(0.5 * minorGrooveAngle * PI / 180.0)

    /** Half [terminalChord] — the base couple's lever arm. */
    val leverArm: Double get() = 0.5 * terminalChord

    /** The helical repeat in base pairs — 32 exactly for the square lattice's 10.67. */
    val helicalRepeatBasePairs: Double get() = 3.0 * basePairsPerTurn

    /**
     * The phosphate of [strand] at index [index] of a duplex whose axis runs along `x` at
     * `y = centreY`, `z = 0`, with axial offset [axialPhase] nm and azimuthal offset
     * [azimuthPhase] radians.
     */
    fun sheetPhosphate(
        centreY: Double,
        strand: Int,
        index: Int,
        axialPhase: Double = 0.0,
        azimuthPhase: Double = 0.0
    ): Vector3 {
        val azimuth = azimuthPhase + index * twistPerBasePair +
                strand * minorGrooveAngle * PI / 180.0
        return Vector3(
            axialPhase + index * risePerBasePair,
            centreY + phosphateRadius * cos(azimuth),
            phosphateRadius * sin(azimuth)
        )
    }

    /**
     * The [terminus] (0 or 1) of a standoff standing along `+z` with its axis at
     * ([centreX], [centreY]) and its terminal base pair in the plane `z = ` [faceHeight],
     * the first terminus at azimuth [azimuth].
     */
    fun standoffTerminus(
        centreX: Double,
        centreY: Double,
        faceHeight: Double,
        azimuth: Double,
        terminus: Int
    ): Vector3 {
        val angle = azimuth + terminus * minorGrooveAngle * PI / 180.0
        return Vector3(
            centreX + phosphateRadius * cos(angle),
            centreY + phosphateRadius * sin(angle),
            faceHeight
        )
    }
}

// ---------------------------------------------------------------- the softened bond, per bond

/**
 * The rotational constant of **one** softened backbone bond in `pN·nm/rad` — Chen et al.'s
 * `α B/(100 a)`, which `C-0009` uses two of to make one antiparallel crossover.
 *
 * This task needs the **per-bond** constant, because a perpendicular junction is not a crossover:
 * it is up to two independent bonds that need not be paired.
 */
fun bondHingeStiffness(alpha: Double = 1.0): Double =
    alpha * Gen1Tile.DUPLEX_BENDING_RIGIDITY / (100.0 * Gen1Tile.RISE_PER_BASE_PAIR)

/** The sliding constant of **one** softened backbone bond in `pN/nm` — `C-0020`'s `α S/(100 a)`. */
fun bondSlideStiffness(alpha: Double = 1.0): Double =
    alpha * Gen1Tile.DUPLEX_STRETCH_MODULUS / (100.0 * Gen1Tile.RISE_PER_BASE_PAIR)

/**
 * **The ceiling this task exists to deliver.** The largest base rotational stiffness in
 * `pN·nm/rad` that two covalent links on a chord of half-width [leverArm] can supply.
 *
 * `2 k_bond,θ + 2 k_bond,s · leverArm²`. Because a duplex end has exactly two termini and they
 * sit on the backbone radius, `leverArm ≤ r_P ≤ R`, and **no routing whatever can exceed the
 * value at `leverArm = R`.**
 */
fun maximumBaseRotationalStiffness(
    leverArm: Double,
    alpha: Double = 1.0,
    inPlaneMultiplier: Double = 1.0
): Double {
    require(leverArm >= 0.0) { "leverArm must not be negative, was: $leverArm" }
    return 2.0 * bondHingeStiffness(alpha) +
            2.0 * bondSlideStiffness(alpha) * inPlaneMultiplier * leverArm * leverArm
}

/**
 * The fraction of a couple that survives a misalignment of [misalignment] radians between the
 * chord's perpendicular bisector and the axis the flexure demands — `cos²`, because a couple is
 * a rank-one tensor `k a a^T`.
 */
fun couplePhaseProjection(misalignment: Double): Double {
    val c = cos(misalignment)
    return c * c
}

/**
 * The base a **realisable** perpendicular junction supplies: two covalent links on the terminal
 * chord of [backbone], read about the axis the couple restrains ([favourable] `= true`) or about
 * the chord itself ([favourable] `= false`).
 *
 * The unfavourable reading reproduces `C-0028`'s `B1` — one antiparallel crossover — **exactly**,
 * which is not a coincidence: two softened bonds with no lever arm *are* a crossover's `k_θ`.
 */
fun realisablePerpendicularBase(
    backbone: DuplexBackbone = DuplexBackbone(),
    favourable: Boolean = true,
    alpha: Double = 1.0,
    inPlaneMultiplier: Double = 1.0,
    misalignment: Double = 0.0
): StandoffBase {
    val arm = backbone.leverArm
    val couple = 2.0 * bondSlideStiffness(alpha) * inPlaneMultiplier * arm * arm *
            (if (favourable) couplePhaseProjection(misalignment) else 0.0)
    return StandoffBase(
        name = "two-terminus perpendicular junction, chord ${"%.3f".format(backbone.terminalChord)}" +
                " nm, ${if (favourable) "restrained" else "free"} axis",
        rotationalStiffness = 2.0 * bondHingeStiffness(alpha) + couple,
        axialStiffness = 2.0 * bondSlideStiffness(alpha) * inPlaneMultiplier,
        provenance = "T-67 counting theorem: a duplex end has exactly two strand termini, on the " +
                "backbone radius; bond constants from Chen et al. (2014) via C-0009/C-0020, the " +
                "in-plane one DERIVED and NOT measured"
    )
}

// ---------------------------------------------------------------- the seat

/**
 * The lowest the standoff's terminal base pair can sit above the sheet's mid-plane, in nm, with
 * its axis at lateral offset [lateralOffset] from a sheet duplex.
 *
 * The standoff is a half-infinite cylinder with a flat end face; the sheet is a row of cylinders
 * spaced [interhelical] apart. The closest approach of the standoff's body to a sheet axis is at
 * the rim of its own end face, so the seat condition is
 * `max(0, |Δy| − R_s)² + z_e² ≥ R_h²` for every sheet duplex.
 *
 * Two seats matter: **on** a duplex, where the face rests on the cylinder's top line and the
 * contact is a **line** of length `2R_s` along the helix; and in the **valley**, where the face
 * dips between two duplexes and touches both at a point.
 */
fun seatFaceHeight(
    lateralOffset: Double,
    standoffRadius: Double = BForm.DUPLEX_RADIUS,
    sheetRadius: Double = BForm.DUPLEX_RADIUS,
    interhelical: Double = Gen1Tile.INTERHELICAL_SHEET
): Double {
    require(standoffRadius > 0.0) { "standoffRadius must be positive, was: $standoffRadius" }
    require(sheetRadius > 0.0) { "sheetRadius must be positive, was: $sheetRadius" }
    require(interhelical > 0.0) { "interhelical must be positive, was: $interhelical" }
    var height = 0.0
    for (k in -2..2) {
        val separation = abs(lateralOffset - k * interhelical)
        val clearance = max(0.0, separation - standoffRadius)
        val squared = sheetRadius * sheetRadius - clearance * clearance
        if (squared > 0.0) height = max(height, sqrt(squared))
    }
    return height
}

/**
 * The angular free play in radians of a flat end face of radius [standoffRadius] rocking
 * **across** the line contact it makes with a sheet cylinder of radius [sheetRadius], before its
 * far rim would enter the cylinder.
 *
 * Rocking **along** the contact line is blocked to first order — that is what a line contact is
 * for — but rocking across it is blocked only by the links. The number is a **dead band in
 * angle**, and at the standoff's head it becomes `ℓ·sin θ` of transverse dead band, which is what
 * `P1` is written against.
 */
fun stericTiltFreedom(
    standoffRadius: Double = BForm.DUPLEX_RADIUS,
    sheetRadius: Double = BForm.DUPLEX_RADIUS
): Double {
    require(standoffRadius > 0.0) { "standoffRadius must be positive, was: $standoffRadius" }
    require(sheetRadius > 0.0) { "sheetRadius must be positive, was: $sheetRadius" }
    // rim at (±R_s, z_e) rotated by θ about the contact point: the descending rim reaches
    // sqrt(R_s² cos²θ + (z_e − R_s sinθ)²) = R_h with z_e = R_h
    val ratio = (standoffRadius * standoffRadius) / (2.0 * standoffRadius * sheetRadius)
    return if (ratio >= 1.0) PI / 2.0 else asin(ratio)
}

/**
 * The largest moment in `pN·nm` a **one-sided** line contact of half-width [halfWidth] can react
 * under a normal compression of [normalForce] — `N·halfWidth`, past which the face lifts off and
 * the joint is carried by its two links alone.
 */
fun contactMomentCapacity(normalForce: Double, halfWidth: Double = BForm.DUPLEX_RADIUS): Double {
    require(normalForce >= 0.0) { "normalForce must not be negative, was: $normalForce" }
    require(halfWidth >= 0.0) { "halfWidth must not be negative, was: $halfWidth" }
    return normalForce * halfWidth
}

// ---------------------------------------------------------------- the routing closure

/**
 * How far a phosphate pair at [gap] nm is from being joinable by **one** phosphodiester step —
 * zero inside the measured `[0.60, 0.70]` nm window, and the shortfall or excess outside it.
 *
 * This is the objective the closure search minimises. Minimising the bare distance instead parks
 * the search on the van der Waals floor, which is not a link but a clash avoided by a hair.
 */
fun linkWindowResidual(
    gap: Double,
    low: Double = BForm.PHOSPHODIESTER_STEP_MIN,
    high: Double = BForm.PHOSPHODIESTER_STEP
): Double {
    require(gap >= 0.0) { "gap must not be negative, was: $gap" }
    require(low > 0.0 && high >= low) { "the link window must be positive and ordered" }
    return max(0.0, gap - high) + max(0.0, low - gap)
}

/** How many unpaired nucleotides a link across [gap] nm needs — zero is a covalent junction. */
fun unpairedNucleotidesForGap(
    gap: Double,
    step: Double = BForm.PHOSPHODIESTER_STEP,
    contourPerNucleotide: Double = SsDnaTether.CONTOUR_PER_NUCLEOTIDE
): Int {
    require(gap >= 0.0) { "gap must not be negative, was: $gap" }
    require(step > 0.0) { "step must be positive, was: $step" }
    require(contourPerNucleotide > 0.0) {
        "contourPerNucleotide must be positive, was: $contourPerNucleotide"
    }
    if (gap <= step) return 0
    return ceil((gap - step) / contourPerNucleotide).toInt()
}

/** The four candidate routings named in `T-67`, as a constraint on where the links may land. */
enum class RoutingTopology(val description: String) {

    /** `R1` — two independent staples, each terminating in the sheet and continuing as a
     * standoff strand. The two sheet targets are unconstrained. */
    INDEPENDENT_STAPLES("two staples, each terminating in the sheet and continuing as one strand"),

    /** `R2` — a scaffold excursion: one strand leaves the sheet at base pair `m`, forms the whole
     * standoff as a hairpin and returns at `m+1`, so the two sheet targets are **consecutive
     * phosphates of one strand**. */
    SCAFFOLD_EXCURSION("a scaffold excursion, out at bp m and back at bp m+1 of one strand"),

    /** `R3`/`R4` — one link only: a hairpin label, a sticky-ended separate duplex, or the
     * literature's pin. The azimuth stays free and the base is a ball joint. */
    SINGLE_LINK("one link only — a hairpin overhang, a sticky end, or the literature's pin")
}

/**
 * A solved junction configuration: where the standoff sits, which sheet phosphates its two
 * termini reach, and what each link costs in unpaired nucleotides.
 */
data class JunctionClosure(
    val topology: RoutingTopology,
    val centreX: Double,
    val centreY: Double,
    val faceHeight: Double,
    val azimuth: Double,
    val firstGap: Double,
    val secondGap: Double,
    val firstDuplex: Int,
    val firstStrand: Int,
    val firstIndex: Int,
    val secondDuplex: Int,
    val secondStrand: Int,
    val secondIndex: Int,
    val chordAzimuth: Double,
    val firstTerminusRadius: Double,
    val secondTerminusRadius: Double
) {

    /** The binding link — the objective the search minimises. */
    val worstGap: Double get() = max(firstGap, secondGap)

    /** The binding link's distance from the `[0.60, 0.70]` nm phosphodiester window. */
    val worstResidual: Double
        get() = max(linkWindowResidual(firstGap), linkWindowResidual(secondGap))

    val firstUnpaired: Int get() = unpairedNucleotidesForGap(firstGap)

    val secondUnpaired: Int get() = unpairedNucleotidesForGap(secondGap)

    /** `P7`: both load-bearing links sit inside the phosphodiester window, with no nucleotide. */
    val covalent: Boolean get() = worstResidual <= 0.0
}

private const val NO_PHOSPHATE = Double.MAX_VALUE

/**
 * Searches the standoff's placement — axial position, lateral seat and azimuth — for the
 * configuration whose **worse** link is shortest, i.e. the routing most likely to close
 * covalently.
 *
 * Deterministic by construction: a fixed grid, then a fixed local refinement, with the index
 * triple as tie-break so that a flat optimum cannot return a different argmin on a re-run
 * (`CLAUDE.md`). Exits on the grid, not on a tolerance.
 */
fun bestTwoLinkClosure(
    backbone: DuplexBackbone = DuplexBackbone(),
    topology: RoutingTopology = RoutingTopology.INDEPENDENT_STAPLES,
    interhelical: Double = Gen1Tile.INTERHELICAL_SHEET,
    azimuthSteps: Int = 180,
    axialSteps: Int = 128,
    lateralSteps: Int = 9,
    refinements: Int = 2
): JunctionClosure {
    require(azimuthSteps > 0) { "azimuthSteps must be positive, was: $azimuthSteps" }
    require(axialSteps > 0) { "axialSteps must be positive, was: $axialSteps" }
    require(lateralSteps > 0) { "lateralSteps must be positive, was: $lateralSteps" }
    require(refinements >= 0) { "refinements must not be negative, was: $refinements" }

    val axialPeriod = backbone.helicalRepeatBasePairs * backbone.risePerBasePair
    val table = SheetPhosphateTable(backbone, interhelical, axialPeriod)
    var bestAzimuth = 0.0
    var bestAxial = 0.0
    var bestLateral = 0.0
    var best: JunctionClosure? = null

    var azimuthLow = 0.0
    var azimuthHigh = 2.0 * PI
    var axialLow = 0.0
    var axialHigh = axialPeriod
    var lateralLow = 0.0
    var lateralHigh = 0.5 * interhelical

    repeat(refinements + 1) { round ->
        val azimuthStep = (azimuthHigh - azimuthLow) / azimuthSteps
        val axialStep = (axialHigh - axialLow) / axialSteps
        val lateralStep =
            if (lateralSteps <= 1) 0.0 else (lateralHigh - lateralLow) / (lateralSteps - 1)
        for (a in 0 until azimuthSteps) {
            val azimuth = azimuthLow + a * azimuthStep
            for (b in 0 until axialSteps) {
                val axial = axialLow + b * axialStep
                for (c in 0 until lateralSteps) {
                    val lateral = lateralLow + c * lateralStep
                    val candidate =
                        closureAt(backbone, table, topology, interhelical, axial, lateral, azimuth)
                            ?: continue
                    val incumbent = best
                    if (incumbent == null ||
                        candidate.worstResidual < incumbent.worstResidual ||
                        (candidate.worstResidual == incumbent.worstResidual &&
                                candidate.worstGap < incumbent.worstGap)
                    ) {
                        best = candidate
                        bestAzimuth = azimuth
                        bestAxial = axial
                        bestLateral = lateral
                    }
                }
            }
        }
        if (round < refinements) {
            azimuthLow = bestAzimuth - azimuthStep
            azimuthHigh = bestAzimuth + azimuthStep
            axialLow = bestAxial - axialStep
            axialHigh = bestAxial + axialStep
            lateralLow = max(0.0, bestLateral - lateralStep)
            lateralHigh = min(0.5 * interhelical, bestLateral + lateralStep)
            if (lateralHigh <= lateralLow) {
                lateralLow = 0.0
                lateralHigh = 0.5 * interhelical
            }
        }
    }
    return requireNotNull(best) { "no admissible junction configuration in the search box" }
}

/**
 * Evaluates one placement, returning `null` when it is inadmissible — a phosphate pair inside van
 * der Waals contact, or a topology whose two sheet targets cannot be consecutive.
 */
/**
 * Every sheet phosphate inside the search box, flattened into primitive arrays so that the
 * placement sweep costs no trigonometry and no allocation.
 */
private class SheetPhosphateTable(
    val backbone: DuplexBackbone,
    val interhelical: Double,
    axialPeriod: Double
) {

    private val margin = 2.5

    val count: Int

    val x: DoubleArray
    val y: DoubleArray
    val z: DoubleArray
    val duplex: IntArray
    val strand: IntArray
    val index: IntArray

    init {
        val lowIndex = kotlin.math.floor(-margin / backbone.risePerBasePair).toInt()
        val highIndex =
            kotlin.math.ceil((axialPeriod + margin) / backbone.risePerBasePair).toInt()
        val indices = highIndex - lowIndex + 1
        count = 3 * 2 * indices
        x = DoubleArray(count)
        y = DoubleArray(count)
        z = DoubleArray(count)
        duplex = IntArray(count)
        strand = IntArray(count)
        index = IntArray(count)
        var at = 0
        for (d in -1..1) {
            for (s in 0..1) {
                for (i in lowIndex..highIndex) {
                    val p = backbone.sheetPhosphate(d * interhelical, s, i)
                    x[at] = p.x
                    y[at] = p.y
                    z[at] = p.z
                    duplex[at] = d
                    strand[at] = s
                    index[at] = i
                    at++
                }
            }
        }
    }

    /** The phosphate at [duplexIndex], [strandIndex], [basePairIndex], or `null` if outside. */
    fun at(duplexIndex: Int, strandIndex: Int, basePairIndex: Int): Vector3? {
        for (i in 0 until count) {
            if (duplex[i] == duplexIndex && strand[i] == strandIndex && index[i] == basePairIndex) {
                return Vector3(x[i], y[i], z[i])
            }
        }
        return null
    }
}

private fun closureAt(
    backbone: DuplexBackbone,
    table: SheetPhosphateTable,
    topology: RoutingTopology,
    interhelical: Double,
    centreX: Double,
    centreY: Double,
    azimuth: Double
): JunctionClosure? {
    val faceHeight = seatFaceHeight(
        centreY, backbone.duplexRadius, backbone.duplexRadius, interhelical
    )
    val first = backbone.standoffTerminus(centreX, centreY, faceHeight, azimuth, 0)
    val second = backbone.standoffTerminus(centreX, centreY, faceHeight, azimuth, 1)

    var firstGap = NO_PHOSPHATE
    var firstResidual = NO_PHOSPHATE
    var firstDuplex = 0
    var firstStrand = 0
    var firstIndex = 0
    var secondGap = NO_PHOSPHATE
    var secondResidual = NO_PHOSPHATE
    var secondDuplex = 0
    var secondStrand = 0
    var secondIndex = 0

    val independent = topology == RoutingTopology.INDEPENDENT_STAPLES
    for (i in 0 until table.count) {
        val dx1 = table.x[i] - first.x
        if (dx1 > 2.0 || dx1 < -2.0) continue
        val dy1 = table.y[i] - first.y
        val dz1 = table.z[i] - first.z
        val toFirst = sqrt(dx1 * dx1 + dy1 * dy1 + dz1 * dz1)
        if (toFirst < BForm.PHOSPHATE_HARD_SEPARATION) return null
        val dx2 = table.x[i] - second.x
        val dy2 = table.y[i] - second.y
        val dz2 = table.z[i] - second.z
        val toSecond = sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2)
        if (toSecond < BForm.PHOSPHATE_HARD_SEPARATION) return null
        val residualFirst = linkWindowResidual(toFirst)
        if (residualFirst < firstResidual) {
            firstResidual = residualFirst
            firstGap = toFirst
            firstDuplex = table.duplex[i]
            firstStrand = table.strand[i]
            firstIndex = table.index[i]
        }
        val residualSecond = linkWindowResidual(toSecond)
        if (independent && residualSecond < secondResidual) {
            secondResidual = residualSecond
            secondGap = toSecond
            secondDuplex = table.duplex[i]
            secondStrand = table.strand[i]
            secondIndex = table.index[i]
        }
    }
    if (firstResidual == NO_PHOSPHATE) return null

    when (topology) {
        RoutingTopology.SINGLE_LINK -> {
            secondGap = firstGap
            secondDuplex = firstDuplex
            secondStrand = firstStrand
            secondIndex = firstIndex
        }

        RoutingTopology.SCAFFOLD_EXCURSION -> {
            // the second sheet target must be the NEXT phosphate of the same strand of the same
            // duplex — the scaffold leaves at bp m and returns at bp m+1
            var bestNeighbour = NO_PHOSPHATE
            var bestResidual = NO_PHOSPHATE
            var bestIndex = firstIndex
            listOf(firstIndex - 1, firstIndex + 1).forEach { candidate ->
                val phosphate = table.at(firstDuplex, firstStrand, candidate) ?: return@forEach
                val distance = (second - phosphate).length
                val residual = linkWindowResidual(distance)
                if (residual < bestResidual) {
                    bestResidual = residual
                    bestNeighbour = distance
                    bestIndex = candidate
                }
            }
            if (bestResidual == NO_PHOSPHATE) return null
            if (bestNeighbour < BForm.PHOSPHATE_HARD_SEPARATION) return null
            secondGap = bestNeighbour
            secondDuplex = firstDuplex
            secondStrand = firstStrand
            secondIndex = bestIndex
        }

        RoutingTopology.INDEPENDENT_STAPLES -> {
            if (secondResidual == NO_PHOSPHATE) return null
        }
    }

    val chord = second - first
    return JunctionClosure(
        topology = topology,
        centreX = centreX,
        centreY = centreY,
        faceHeight = faceHeight,
        azimuth = azimuth,
        firstGap = firstGap,
        secondGap = secondGap,
        firstDuplex = firstDuplex,
        firstStrand = firstStrand,
        firstIndex = firstIndex,
        secondDuplex = secondDuplex,
        secondStrand = secondStrand,
        secondIndex = secondIndex,
        chordAzimuth = kotlin.math.atan2(chord.y, chord.x),
        firstTerminusRadius = sqrt(
            (first.x - centreX) * (first.x - centreX) + (first.y - centreY) * (first.y - centreY)
        ),
        secondTerminusRadius = sqrt(
            (second.x - centreX) * (second.x - centreX) +
                    (second.y - centreY) * (second.y - centreY)
        )
    )
}

// ---------------------------------------------------------------- E5 under exact rotation

/**
 * `C-0023`'s **`E5`** crossover-hinge flexure with its rotation solved **exactly** instead of
 * linearised — because at `C-0023`'s own working point the arm has already turned 47°.
 *
 * An arm of length `r` on a torsional spring `k` carrying a transverse tip force `F` satisfies
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k θ = F r cos θ`, &nbsp;&nbsp; `δ_hinge = r sin θ`, &nbsp;&nbsp;
 * `Δx = r (1 − cos θ)`,
 *
 * and the arm's own bending `F r³/(c EI)` is added in series at the tip. Two things follow that
 * the linear reading cannot see:
 *
 * 1. **`δ_hinge < r`, identically** — the tip of an arm of length `r` cannot rise more than `r`,
 *    whatever the hinge constant. This is geometry and needs no constitutive law.
 * 2. **The law stiffens**, because `cos θ` shrinks the moment arm as the tip rises, so the
 *    tangent/secant ratio at a finite stroke is well above one — which is exactly what
 *    `C-0023`'s 40 pN/nm compliance ceiling is written on.
 *
 * The rotational constant is still Chen et al.'s **small-angle** fit, extrapolated here; that is
 * flagged in the claim's validity range and is why the *geometric* ceiling is quoted separately.
 */
class RotatingHingeArm(
    val hingeStiffness: Double,
    val armLength: Double,
    val armBendingRigidity: Double,
    val hingeCount: Int = 1,
    val armFactor: Double = 3.0
) : SignedCouplingElement {

    init {
        require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
        require(armLength > 0.0) { "armLength must be positive, was: $armLength" }
        require(armBendingRigidity > 0.0) {
            "armBendingRigidity must be positive, was: $armBendingRigidity"
        }
        require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
        require(armFactor > 0.0) { "armFactor must be positive, was: $armFactor" }
    }

    /** `n k_θ` in `pN·nm/rad`. */
    val totalHingeStiffness: Double get() = hingeCount * hingeStiffness

    /** `c EI/r³` in `pN/nm` — the arm's own bending, in series at the tip. */
    val armStiffness: Double
        get() = armFactor * armBendingRigidity / (armLength * armLength * armLength)

    /** **The geometric ceiling**: the hinge branch cannot lift the tip past the arm's length. */
    val maximumHingeStroke: Double get() = armLength

    /** `C-0023`'s own linear reading, which this class reduces to as the rotation vanishes. */
    val smallRotationStiffness: Double
        get() = 1.0 / (armLength * armLength / totalHingeStiffness + 1.0 / armStiffness)

    /** The rotation in radians at which the hinge balances a tip force of [force]. */
    fun rotationForForce(force: Double): Double {
        require(force >= 0.0) { "force must not be negative, was: $force" }
        if (force == 0.0) return 0.0
        var low = 0.0
        var high = 0.5 * PI
        repeat(200) {
            val middle = 0.5 * (low + high)
            val residual = totalHingeStiffness * middle - force * armLength * cos(middle)
            if (residual < 0.0) low = middle else high = middle
            if (high - low <= 1.0e-15) return 0.5 * (low + high)
        }
        return 0.5 * (low + high)
    }

    /** The tip rise in nm contributed by the hinge alone at [force] — strictly below [armLength]. */
    fun hingeDisplacement(force: Double): Double = armLength * sin(rotationForForce(force))

    /** The total tip rise in nm at [force]: the hinge's rotation plus the arm's own bending. */
    fun displacement(force: Double): Double = hingeDisplacement(force) + force / armStiffness

    /** The horizontal draw-in in nm the rotation demands of the tile's attachment. */
    fun horizontalDrawIn(displacement: Double): Double {
        val force = abs(reaction(displacement))
        return armLength * (1.0 - cos(rotationForForce(force)))
    }

    /** The signed reaction in pN at [displacement], odd by construction. */
    override fun reaction(displacement: Double): Double {
        if (displacement == 0.0) return 0.0
        val magnitude = abs(displacement)
        var low = 0.0
        var high = 1.0
        while (displacement(high) < magnitude) high *= 2.0
        repeat(300) {
            val middle = 0.5 * (low + high)
            if (displacement(middle) < magnitude) low = middle else high = middle
            if (high - low <= 1.0e-14 * max(1.0, high)) {
                val force = 0.5 * (low + high)
                return if (displacement < 0.0) -force else force
            }
        }
        val force = 0.5 * (low + high)
        return if (displacement < 0.0) -force else force
    }

    /** The secant `|R|/δ` in `pN/nm`. */
    override fun secantStiffness(displacement: Double): Double =
        if (displacement == 0.0) smallRotationStiffness
        else abs(reaction(displacement)) / abs(displacement)

    /**
     * The tangent `d|R|/dδ` in `pN/nm`, **analytically**: differentiating `kθ = F r cos θ` gives
     * `dθ/dF = r cos θ/(k + F r sin θ)`, so
     * `dδ/dF = r² cos²θ/(k + F r sin θ) + 1/k_arm` and the tangent is its reciprocal.
     */
    override fun tangentStiffness(displacement: Double): Double {
        val force = abs(reaction(displacement))
        val theta = rotationForForce(force)
        val compliance = armLength * armLength * cos(theta) * cos(theta) /
                (totalHingeStiffness + force * armLength * sin(theta)) + 1.0 / armStiffness
        return 1.0 / compliance
    }

    /** The moment in `pN·nm` carried by **one** hinge at [displacement]. */
    fun hingeMoment(displacement: Double): Double =
        totalHingeStiffness * rotationForForce(abs(reaction(displacement))) / hingeCount

    /** The force in pN on one crossover's backbone bonds, over a lever of [leverSeparation] nm. */
    fun hingeBondForce(
        displacement: Double,
        leverSeparation: Double = Gen1Tile.INTERHELICAL_SHEET
    ): Double {
        require(leverSeparation > 0.0) {
            "leverSeparation must be positive, was: $leverSeparation"
        }
        return hingeMoment(displacement) / leverSeparation
    }
}

/**
 * **The arm a hinge flexure can never exceed**, in nm, whatever its hinge constant or hinge
 * count: the arm's own bending is in **series** with the hinge, so the assembled stiffness can
 * never exceed `count·c EI/r³`, and the placement condition therefore caps
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`r ≤ (c·count·EI/k_target)^(1/3)`.
 *
 * At `C-0023`'s own numbers — a cantilever arm (`c = 3`), 45 paths, `EI = 230 pN·nm²` and the
 * 33.3333 pN/nm mandate — that is **9.77 nm**, which is **below §3's desired 10 nm stroke**. So
 * `E5` cannot deliver the desired stroke with a single-duplex cantilever arm at *any* hinge
 * count: not because the hinge is too soft, but because the arm long enough to reach is too soft
 * to place. A **guided** arm (`c = 12`) lifts the cap to 15.50 nm, and that is the redesign.
 */
fun hingeArmCeiling(
    armFactor: Double = 3.0,
    count: Int = 45,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    targetStiffness: Double = 100.0 / 3.0
): Double {
    require(armFactor > 0.0) { "armFactor must be positive, was: $armFactor" }
    require(count > 0) { "count must be positive, was: $count" }
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    return Math.cbrt(armFactor * count * bendingRigidity / targetStiffness)
}

/**
 * The arm length in nm at which [count] [RotatingHingeArm]s present [targetStiffness] as a
 * **secant** at [workingDisplacement] — `C-0023`'s placement condition, re-solved under exact
 * rotation. Exits on the bracket width.
 */
fun rotatingArmForStiffness(
    hingeStiffness: Double,
    armBendingRigidity: Double,
    count: Int,
    targetStiffness: Double,
    workingDisplacement: Double,
    hingeCount: Int = 1,
    armFactor: Double = 3.0,
    maximumArm: Double = 200.0
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingDisplacement > 0.0) {
        "workingDisplacement must be positive, was: $workingDisplacement"
    }
    fun assembled(arm: Double): Double = count * RotatingHingeArm(
        hingeStiffness, arm, armBendingRigidity, hingeCount, armFactor
    ).secantStiffness(workingDisplacement)
    // the assembled secant falls with the arm, so bisect on a decreasing function
    var low = 1.001 * workingDisplacement
    var high = maximumArm
    require(assembled(high) < targetStiffness) {
        "no arm as long as $maximumArm nm reaches a stiffness as low as $targetStiffness"
    }
    require(assembled(low) > targetStiffness) {
        "even the shortest admissible arm is softer than $targetStiffness"
    }
    repeat(300) {
        val middle = 0.5 * (low + high)
        if (assembled(middle) > targetStiffness) low = middle else high = middle
        if (high - low <= 1.0e-13 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}
