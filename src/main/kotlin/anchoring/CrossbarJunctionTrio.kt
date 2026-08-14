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
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Task `T-117` — whether **three** 90° junctions close on **one** crossbar duplex, which is
 * `C-0048`'s open item 1 and `C-0042`'s own question at the other end of the same legs.
 *
 * ## Why this is not `C-0042`'s search with one more body
 *
 * `C-0042` seated two junctions on a **sheet** duplex: a seat with neighbours at ±2.69 nm, a 32 bp
 * crossover phase lattice, and an axial extent so long that its ends never entered the search.
 * `C-0048`'s cap has **none** of that — it is a **lone, free, 13 bp** duplex — and the three
 * constraints that made `C-0042`'s search tractable are replaced, one for one:
 *
 * | `C-0042`'s seat | this crossbar |
 * |---|---|
 * | neighbours at ±2.69 nm enter [seatFaceHeight] | none: [loneSeatFaceHeight] is `R` everywhere inside the rim, exactly |
 * | 32 bp crossover phase lattice, 24 of 32 phases free | **vacuous** — a lone crossbar carries no crossovers |
 * | one shared lateral seat, imposed as `Q4` | **automatic** — one straight crossbar gives both legs the same offset |
 * | the seat's helical and axial phase are the sheet's | **free and continuous**, because the crossbar is a free body (`CH-0056`) |
 * | no rim | an **axial rim** at `±L/2`, past which there is neither seat nor phosphate |
 *
 * ## Geometry and sign conventions
 *
 * The sheet is the `x–y` plane and `z` its normal. **The crossbar runs along `x̂`**, its axis at
 * `y = 0, z = 0`; **the flexure's own axis is `ŷ`**. This is `C-0042`'s frame unchanged, and it maps
 * onto `C-0048`'s by `x̂_here = ŷ_C-0048`.
 *
 * - the **legs** stand along `ẑ` at `(±w/2, y_c)`, their terminal base-pair planes at `z = −h_f`,
 *   their termini at azimuths `ψ`, `ψ + Δ` measured in `x–y` from `x̂`;
 * - the **flexure** arrives along `−ŷ` at `(0, z_c)`, its terminal plane at `y = −h_f`, its termini
 *   at azimuths `ψ_f`, `ψ_f + Δ` measured in `x–z` from `x̂`;
 * - a **chord is a line**, so every azimuth folds modulo `π` and every misalignment is in `[0, π/2]`;
 * - the design's demands (`C-0048`): leg-head chords **along `x̂`**, the flexure's own end chord
 *   **along `ẑ`**, and (`C-0042`) each leg's **base** chord along **`ŷ`**.
 *
 * ## What is modelled and what is not
 *
 * Exactly `C-0029`'s admissibility test, applied three times: a phosphate pair inside the
 * **measured** `[0.60, 0.70]` nm step with no van der Waals overlap. A **necessary** condition and
 * never a sufficient one, so a *"closes"* verdict is an **upper bound on buildability**; only a
 * *"does not close"* verdict is a proof of impossibility. `T-71` is the torsion check and can only
 * make the answer worse.
 */

// ---------------------------------------------------------------- cheap bound 1: the finite seat

/**
 * The line contact, in nm, a flat duplex end face of radius [standoffRadius] makes with a seat
 * cylinder of **finite** length [seatLength], when the junction's axis sits [lateralOffset] to the
 * side of the seat's axis and [axialOffset] along it from the seat's midpoint.
 *
 * `C-0042`'s [seatContactLength] is the `seatLength → ∞` limit. The truncation is the whole reason
 * this function exists: `C-0042` introduced the unbounded version to stop its search parking a
 * standoff on the **lateral** rim of a sheet duplex, where the contact `2√(R² − y_c²)` has collapsed
 * to a point; on a 4.42 nm crossbar the **axial** rim sits 1.02 nm from each leg's axis, so the same
 * degeneracy is available in the other direction and has to be excluded in the other direction too.
 */
fun boundedSeatContactLength(
    axialOffset: Double,
    seatLength: Double,
    lateralOffset: Double = 0.0,
    standoffRadius: Double = BForm.DUPLEX_RADIUS
): Double {
    require(seatLength > 0.0) { "seatLength must be positive, was: $seatLength" }
    require(standoffRadius > 0.0) { "standoffRadius must be positive, was: $standoffRadius" }
    val half = seatContactLength(lateralOffset, standoffRadius) / 2.0
    if (half <= 0.0) return 0.0
    val low = max(axialOffset - half, -0.5 * seatLength)
    val high = min(axialOffset + half, 0.5 * seatLength)
    return max(0.0, high - low)
}

/**
 * The face height of a **lone** seat duplex, in nm: how far below the crossbar's own axis a leg's
 * terminal base-pair plane sits.
 *
 * `C-0042`'s [seatFaceHeight] with the neighbour terms removed — which is what a crossbar has, and
 * the function is asserted equal to it in the `interhelical → ∞` limit. Inside the rim it is the
 * seat's radius **exactly**, at every offset, which is why the *height* cannot exclude a rim seat
 * and [boundedSeatContactLength] has to.
 */
fun loneSeatFaceHeight(
    lateralOffset: Double,
    standoffRadius: Double = BForm.DUPLEX_RADIUS,
    seatRadius: Double = BForm.DUPLEX_RADIUS
): Double {
    require(standoffRadius > 0.0) { "standoffRadius must be positive, was: $standoffRadius" }
    require(seatRadius > 0.0) { "seatRadius must be positive, was: $seatRadius" }
    val clearance = max(0.0, abs(lateralOffset) - standoffRadius)
    val squared = seatRadius * seatRadius - clearance * clearance
    return if (squared > 0.0) sqrt(squared) else 0.0
}

// ---------------------------------------------------------------- cheap bound 2: two solid bodies

/**
 * A duplex as a **half-infinite cylinder with a flat end face** — the same body `C-0029`'s seat
 * analysis assumes, and a **convex** set, being a cylinder intersected with a half-space.
 *
 * @property face a point on the axis, in the plane of the flat end face.
 * @property axis the unit vector from the face **into** the body.
 */
data class SolidCylinder(
    val face: Vector3,
    val axis: Vector3,
    val radius: Double = BForm.DUPLEX_RADIUS
) {

    init {
        require(radius > 0.0) { "radius must be positive, was: $radius" }
        require(axis.length > 0.0) { "axis must not be the zero vector" }
    }

    private val unit: Vector3 = axis.normalized

    /**
     * The orthogonal projection of [point] onto this solid.
     *
     * The cylinder constrains the two transverse coordinates and the half-space the axial one, and
     * they are orthogonal, so the projection is componentwise and **exact** — which is what makes
     * alternating projection between two of these converge to the true closest pair.
     */
    fun project(point: Vector3): Vector3 {
        val relative = point - face
        val along = relative.dot(unit)
        val across = relative - unit * along
        val distance = across.length
        val clampedAcross = if (distance <= radius) across else across * (radius / distance)
        return face + clampedAcross + unit * max(0.0, along)
    }
}

/**
 * The minimum separation in nm between two [SolidCylinder]s — **zero** where they touch or overlap.
 *
 * Both bodies are convex, so **alternating projection** converges to a closest pair (von Neumann
 * for two closed convex sets). Deterministic: a fixed start, a fixed iteration count, no tolerance
 * in the control flow.
 *
 * A capsule approximation — the distance between the two axis segments less two radii — is **not**
 * good enough here and reports a clash where the flat-ended bodies clear: it rounds the leg's flat
 * end face into a hemisphere exactly where the flexure's end face passes it.
 */
fun minimumSolidSeparation(
    first: SolidCylinder,
    second: SolidCylinder,
    iterations: Int = 400
): Double {
    require(iterations >= 1) { "iterations must be at least one, was: $iterations" }
    var a = first.project(second.face)
    var b = second.project(a)
    repeat(iterations) {
        a = first.project(b)
        b = second.project(a)
    }
    return (a - b).length
}

// ---------------------------------------------------------------- cheap bound 3: the chord twist

/**
 * **The bound this task was written to test.** The angle in radians, folded into `[0, π)`, between
 * the terminal chords of the **two ends of one duplex** [steps] base-pair steps long.
 *
 * A duplex end presents exactly two strand termini (`C-0029`'s counting theorem) and their chord's
 * direction is the terminal base pair's azimuth plus `Δ/2 + π/2` (`C-0042`'s gate-3 identity). Both
 * ends of one body carry that relation, so the **difference** of the two chord azimuths is
 * `steps × twist` — quantised at **33.74°/bp** on the square lattice — whatever the body's overall
 * rotation.
 *
 * `CH-0056` established that the azimuthal quantum belongs to the **sheet** and not to a free
 * standoff's chord, because a free duplex with **one** junction inherits no phase. That is upheld:
 * a free duplex with **two** junctions inherits no phase either, and its *absolute* azimuths are
 * still continuous. What is quantised is the **relation between its two ends** — which is a
 * different quantity on the same body, and it is the quantity a truss leg and a flexure both have.
 */
fun relativeChordAzimuth(steps: Int, backbone: DuplexBackbone = DuplexBackbone()): Double {
    require(steps >= 0) { "steps must not be negative, was: $steps" }
    val raw = steps * backbone.twistPerBasePair
    var folded = raw % PI
    if (folded < 0.0) folded += PI
    return folded
}

/**
 * How far, in radians and folded into `[0, π/2]`, a duplex of [steps] base-pair steps falls short of
 * presenting its two end chords at [wantedSeparation] to each other.
 *
 * `C-0048`'s recommended design wants the leg's **base** chord along the flexure axis and its
 * **cap** chord across it — `wantedSeparation = π/2` — and `C-0023`'s flexure wants its two end
 * chords **parallel**, i.e. `0`.
 */
fun chordPairMisalignment(
    steps: Int,
    wantedSeparation: Double = 0.5 * PI,
    backbone: DuplexBackbone = DuplexBackbone()
): Double {
    val delta = relativeChordAzimuth(steps, backbone) - wantedSeparation
    var folded = abs(delta % PI)
    if (folded > 0.5 * PI) folded = PI - folded
    return folded
}

/** The length in base-pair steps, within [range], whose end chords come closest to the demand. */
fun bestChordPairSteps(
    range: IntRange,
    wantedSeparation: Double = 0.5 * PI,
    backbone: DuplexBackbone = DuplexBackbone()
): Int {
    require(!range.isEmpty()) { "range must not be empty" }
    var best = range.first
    var bestValue = Double.MAX_VALUE
    range.forEach { steps ->
        val value = chordPairMisalignment(steps, wantedSeparation, backbone)
        if (value < bestValue) {
            bestValue = value
            best = steps
        }
    }
    return best
}

/**
 * How a leg's rotation about its own axis splits the quantised budget between its two junctions.
 *
 * Rotating the leg by [rotation] moves **both** chords together, so it takes misalignment off one
 * end and puts it onto the other: the two misalignments trade one for one and their **sum** is the
 * budget [chordPairMisalignment] fixes. The design chooses where to spend it, not whether to.
 */
data class LegAzimuthSplit(
    val steps: Int,
    val rotation: Double,
    val baseMisalignment: Double,
    val capMisalignment: Double
) {

    /** The budget the leg's length imposes, which no rotation can reduce. */
    val budget: Double get() = baseMisalignment + capMisalignment
}

/**
 * The base and cap misalignments a leg of [steps] steps has when rotated by [rotation] radians away
 * from the placement that puts its **base** chord exactly on the flexure axis.
 */
fun legAzimuthSplit(
    steps: Int,
    rotation: Double,
    backbone: DuplexBackbone = DuplexBackbone()
): LegAzimuthSplit {
    val base = foldedChordMisalignment(0.5 * PI + rotation, 0.5 * PI)
    val cap = foldedChordMisalignment(0.5 * PI + rotation + relativeChordAzimuth(steps, backbone), 0.0)
    return LegAzimuthSplit(steps, rotation, base, cap)
}

/** The misalignment in `[0, π/2]` of the chord of a junction at [azimuth] from [wanted]. */
fun chordMisalignmentOf(
    azimuth: Double,
    wanted: Double,
    backbone: DuplexBackbone = DuplexBackbone()
): Double = foldedChordMisalignment(
    azimuth + 0.5 * backbone.minorGrooveAngle * PI / 180.0 + 0.5 * PI, wanted
)

// ---------------------------------------------------------------- cheap bound 4: is it a duplex

/** `1 kcal/mol` in `k_BT` at 300 K — derived from the two constants, not quoted. */
const val KCAL_PER_MOL_IN_KT: Double = 4184.0 / 6.02214076e23 / 4.141947e-21

/**
 * SantaLucia's **unified** nearest-neighbour parameters, `ΔG°₃₇` in kcal/mol at 1 M NaCl —
 * **CITED, READ DIRECTLY** from *"A unified view of polymer, dumbbell, and oligonucleotide DNA
 * nearest-neighbor thermodynamics"*, *Proc. Natl. Acad. Sci. USA* **95**:1460 (1998), Table 1
 * (`PMC19045`), with the initiation parameters from the same table and the `ΔH°`/`ΔS°` set from
 * its Table 2.
 *
 * The **salt correction** is an equation rendered as an image in that article and is **NOT read
 * here**; every free energy in this task is therefore quoted at the parameters' own 1 M NaCl, which
 * is the **optimistic** end for a 2 mM MgCl₂ buffer, and is used only for the two things it can
 * carry without it: the **spread over sequence** and the **length threshold**, both of which are
 * ratios in which the correction largely cancels.
 */
object UnifiedNearestNeighbour {

    const val AA_TT: Double = -1.00
    const val AT_TA: Double = -0.88
    const val TA_AT: Double = -0.58
    const val CA_GT: Double = -1.45
    const val GT_CA: Double = -1.44
    const val CT_GA: Double = -1.28
    const val GA_CT: Double = -1.30
    const val CG_GC: Double = -2.17
    const val GC_CG: Double = -2.24
    const val GG_CC: Double = -1.84

    /** The tabulated average of the ten steps, which is what a sequence-blind estimate uses. */
    const val AVERAGE: Double = -1.42

    const val INITIATION_TERMINAL_GC: Double = 0.98
    const val INITIATION_TERMINAL_AT: Double = 1.03

    /** The ten Watson-Crick nearest-neighbour steps, in the order of the paper's Table 1. */
    val STEPS: List<Double> = listOf(
        AA_TT, AT_TA, TA_AT, CA_GT, GT_CA, CT_GA, GA_CT, CG_GC, GC_CG, GG_CC
    )

    /** The weakest step in the table — an all-`TA/AT` duplex is the worst case a designer can pick. */
    val WEAKEST: Double get() = TA_AT

    /** The strongest step in the table. */
    val STRONGEST: Double get() = GC_CG
}

/**
 * Huguet et al.'s nearest-neighbour free energies **in magnesium**, `ΔG` in kcal/mol at **298 K**
 * and a **1 M** reference, with their per-motif salt-correction factors — **CITED, READ DIRECTLY**
 * from *"Derivation of nearest-neighbor DNA parameters in magnesium from single molecule
 * experiments"*, *Nucleic Acids Res.* **45**:12921 (2017), Table 1, `PMC5728412`, unzipping column.
 *
 * This is the right set for this project: the buffer is **2 mM MgCl₂**, the temperature is 300 K
 * against their 298, and the salt correction is **measured per motif** rather than assumed
 * homogeneous — the paper's own headline is that a homogeneous correction is *"definitely
 * incompatible"* with the data, and that the unified-oligonucleotide set's assumption *"that the
 * salt correction for the monovalent ions is exactly twice the correction for the divalent ones"*
 * is disproved.
 *
 * **What is read and what is not.** The ten step energies, the ten correction factors and the
 * initiation factor are read verbatim from Table 1; the paper's own words for the correction are
 * *"a simple linear logarithmic dependency with salt concentration"* about *"the energy of motif at
 * the reference condition = 1 M"*. **The equation itself is rendered as an image and the base of
 * that logarithm was not read**, so [saltCorrectedStepFreeEnergy] carries **both** conventions and
 * every free energy here is quoted as a bracket. The natural-logarithm reading is the pessimistic
 * one and is what the verdict is taken on.
 */
object MagnesiumNearestNeighbour {

    const val AA_TT: Double = -1.69
    const val AC_TG: Double = -1.91
    const val AG_TC: Double = -1.81
    const val AT_TA: Double = -1.55
    const val CA_GT: Double = -2.17
    const val CC_GG: Double = -2.18
    const val CG_GC: Double = -2.65
    const val GA_CT: Double = -1.88
    const val GC_CG: Double = -2.74
    const val TA_AT: Double = -1.38

    /** The initiation factor, kcal/mol — small, and a different convention from SantaLucia's. */
    const val INITIATION: Double = -0.06

    /** The ten steps, in the order of the paper's Table 1. */
    val STEPS: List<Double> = listOf(
        AA_TT, AC_TG, AG_TC, AT_TA, CA_GT, CC_GG, CG_GC, GA_CT, GC_CG, TA_AT
    )

    /** The ten per-motif salt-correction factors `m_i`, in the same order. */
    val SALT_FACTORS: List<Double> = listOf(
        0.086, 0.073, 0.070, 0.092, 0.078, 0.032, 0.057, 0.075, 0.060, 0.087
    )

    /** The sequence-averaged step, which is what a sequence-blind estimate uses. */
    val AVERAGE: Double get() = STEPS.average()

    /** The sequence-averaged salt-correction factor. */
    val AVERAGE_SALT_FACTOR: Double get() = SALT_FACTORS.average()

    val WEAKEST: Double get() = TA_AT

    val STRONGEST: Double get() = GC_CG

    /** The salt-correction factor belonging to [step], by identity in the table. */
    fun saltFactor(step: Double): Double {
        val at = STEPS.indexOfFirst { abs(it - step) < 1.0e-12 }
        require(at >= 0) { "not a tabulated magnesium step: $step" }
        return SALT_FACTORS[at]
    }
}

/**
 * One nearest-neighbour step's free energy in kcal/mol at [saltMolar], from its 1 M
 * [referenceFreeEnergy] and its own correction factor [saltFactor].
 *
 * The correction is **destabilising below 1 M** — which is the direction the paper's own Figure 1D
 * establishes, *"the mean unzipping force of the DNA molecule increases with the concentration of
 * Mg²⁺, demonstrating that divalent cations stabilize the duplex"* — and the base of the logarithm
 * is carried as a bracket, because the equation is an image in the source. [naturalLogarithm] is
 * the **pessimistic** reading, 2.30× the decimal one.
 */
fun saltCorrectedStepFreeEnergy(
    referenceFreeEnergy: Double,
    saltFactor: Double,
    saltMolar: Double,
    naturalLogarithm: Boolean = true
): Double {
    require(saltMolar > 0.0) { "saltMolar must be positive, was: $saltMolar" }
    require(saltFactor >= 0.0) { "saltFactor must not be negative, was: $saltFactor" }
    val logarithm = if (naturalLogarithm) kotlin.math.ln(saltMolar)
    else kotlin.math.log10(saltMolar)
    return referenceFreeEnergy - saltFactor * logarithm
}

/**
 * The duplex free energy in kcal/mol of a [basePairs]-long duplex whose every nearest-neighbour step
 * is worth [stepFreeEnergy], with one [initiation] term — SantaLucia's Eq. 1 with a uniform sequence.
 *
 * A duplex of `n` base pairs has `n − 1` steps, which is the same off-by-one the chord twist has and
 * for the same reason: both count **steps between base pairs**, not base pairs.
 */
fun duplexFreeEnergy(
    basePairs: Int,
    stepFreeEnergy: Double = UnifiedNearestNeighbour.AVERAGE,
    initiation: Double = UnifiedNearestNeighbour.INITIATION_TERMINAL_AT
): Double {
    require(basePairs >= 1) { "basePairs must be at least one, was: $basePairs" }
    require(stepFreeEnergy < 0.0) {
        "a Watson-Crick step must be stabilising, was: $stepFreeEnergy"
    }
    return (basePairs - 1) * stepFreeEnergy + initiation
}

/** The shortest duplex whose free energy reaches [target] kcal/mol at [stepFreeEnergy] per step. */
fun basePairsForFreeEnergy(
    target: Double,
    stepFreeEnergy: Double = UnifiedNearestNeighbour.AVERAGE,
    initiation: Double = UnifiedNearestNeighbour.INITIATION_TERMINAL_AT
): Int {
    require(target < 0.0) { "target must be a stabilising free energy, was: $target" }
    require(stepFreeEnergy < 0.0) {
        "a Watson-Crick step must be stabilising, was: $stepFreeEnergy"
    }
    return 1 + ceil((target - initiation) / stepFreeEnergy).toInt()
}

// ---------------------------------------------------------------- the crossbar's plan geometry

/**
 * The cap crossbar's plan geometry: a lone duplex of [basePairs] laid along a leg row of
 * [legSeparationBasePairs] pitch, with the flexure's own end butting its side at the midpoint.
 *
 * `C-0048` quotes *"13 bp = 4.38 nm"*; those are two different quantities and both are carried here.
 * **4.38 nm is the demand** `w + 2R`, and **13 bp is 4.42 nm** of duplex — the smallest that covers
 * it, at `ceil`.
 */
data class CrossbarGeometry(
    val basePairs: Int,
    val legSeparationBasePairs: Int,
    val rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    val legRadius: Double = BForm.DUPLEX_RADIUS,
    val capRadius: Double = BForm.DUPLEX_RADIUS,
    val flexureRadius: Double = BForm.DUPLEX_RADIUS
) {

    init {
        require(basePairs >= 2) { "basePairs must be at least two, was: $basePairs" }
        require(legSeparationBasePairs >= 1) {
            "legSeparationBasePairs must be at least one, was: $legSeparationBasePairs"
        }
        require(rise > 0.0) { "rise must be positive, was: $rise" }
        require(legRadius > 0.0) { "legRadius must be positive, was: $legRadius" }
        require(capRadius > 0.0) { "capRadius must be positive, was: $capRadius" }
        require(flexureRadius > 0.0) { "flexureRadius must be positive, was: $flexureRadius" }
        require(basePairs >= minimumBasePairs) {
            "a crossbar of $basePairs bp does not cover a $legSeparationBasePairs bp row, which " +
                    "needs at least $minimumBasePairs bp"
        }
    }

    /** The row's pitch `w` in nm. */
    val legSeparation: Double get() = legSeparationBasePairs * rise

    /** `C-0048`'s crossbar length **demand**, `w + 2R`. */
    val minimumLength: Double get() = legSeparation + 2.0 * legRadius

    /** `C-0048`'s crossbar length in base pairs, `ceil(demand/rise)`. */
    val minimumBasePairs: Int
        get() = ceil((legSeparationBasePairs * rise + 2.0 * legRadius) / rise).toInt()

    /** The duplex the builder orders, in nm — `basePairs × rise`, which is **not** the demand. */
    val length: Double get() = basePairs * rise

    val halfLength: Double get() = 0.5 * length

    /** Where a leg's axis sits along the crossbar, from its midpoint. */
    val legAxialOffset: Double get() = 0.5 * legSeparation

    /** How much crossbar lies outboard of a leg's own footprint — negative if the leg overhangs. */
    val legRimClearance: Double get() = halfLength - legAxialOffset - legRadius

    /** A leg's **truncated** line contact on the crossbar, at the crossbar's own axis. */
    val legSeatContact: Double
        get() = boundedSeatContactLength(legAxialOffset, length, 0.0, legRadius)

    /** The flexure's line contact, at the midpoint, where the rim never truncates. */
    val flexureSeatContact: Double
        get() = boundedSeatContactLength(0.0, length, 0.0, flexureRadius)

    /** `e` — the flexure's axis sits one cap radius above the leg heads (`C-0048`). */
    val rigidHeight: Double get() = capRadius

    /** How many 90° junctions the crossbar hosts. */
    val junctionCount: Int get() = 3

    /** How many covalent links those junctions consume, at `C-0029`'s two per duplex end. */
    val covalentLinkCount: Int get() = junctionCount * BForm.TERMINI_PER_DUPLEX_END

    /**
     * The closest approach in nm between a **leg's** body and the **flexure's** body — the check the
     * capsule approximation gets wrong, and the one that decides whether the flexure's end can enter
     * between the legs at all.
     */
    val legFlexureClearance: Double
        get() {
            val faceHeight = loneSeatFaceHeight(0.0, legRadius, capRadius)
            val leg = SolidCylinder(
                Vector3(legAxialOffset, 0.0, -faceHeight), Vector3(0.0, 0.0, -1.0), legRadius
            )
            val flexure = SolidCylinder(
                Vector3(0.0, -loneSeatFaceHeight(0.0, flexureRadius, capRadius), 0.0),
                Vector3(0.0, -1.0, 0.0),
                flexureRadius
            )
            return minimumSolidSeparation(leg, flexure)
        }
}

// ---------------------------------------------------------------- one junction on the crossbar

/** Which body a junction belongs to, and therefore in which plane its terminal chord lies. */
enum class TrioJunctionKind(val description: String) {

    /** A truss leg, standing along `ẑ` and meeting the crossbar's **underside**; chord in `x–y`. */
    LEG("a leg head, from below"),

    /** The flexure's own end, arriving along `−ŷ` and meeting the crossbar's **flank**; chord in `x–z`. */
    FLEXURE("the flexure's own end, from the side")
}

/**
 * One junction's place in the design: which body it is, where along the crossbar it sits, and which
 * chord direction the design wants of it.
 *
 * @property axialOffset the junction's axis position along the crossbar, from its midpoint, in nm.
 * @property wantedChordAzimuth the chord azimuth the design demands, in the junction's own chord
 *   plane: `0` is along the crossbar (`x̂`) and `π/2` is across it — `ŷ` for a leg, `ẑ` for the
 *   flexure.
 */
data class TrioJunctionSpec(
    val kind: TrioJunctionKind,
    val axialOffset: Double,
    val wantedChordAzimuth: Double,
    val name: String
) {

    companion object {

        /**
         * `C-0048`'s cap: two leg heads from below at `±w/2` wanting their chords **along** the
         * crossbar, and the flexure's own end from the side at the midpoint wanting its chord
         * **vertical**, which is the only orientation that restrains the flexure's own bending.
         */
        fun cap(
            separationBasePairs: Int,
            rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
        ): List<TrioJunctionSpec> {
            val half = 0.5 * separationBasePairs * rise
            return listOf(
                TrioJunctionSpec(TrioJunctionKind.LEG, -half, 0.0, "leg −w/2"),
                TrioJunctionSpec(TrioJunctionKind.LEG, half, 0.0, "leg +w/2"),
                TrioJunctionSpec(TrioJunctionKind.FLEXURE, 0.0, 0.5 * PI, "flexure end")
            )
        }

        /** The two leg heads alone — the configuration that reduces to `C-0042`'s pair. */
        fun legRowOnly(
            separationBasePairs: Int,
            rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
            wantedChordAzimuth: Double = 0.0
        ): List<TrioJunctionSpec> {
            val half = 0.5 * separationBasePairs * rise
            return listOf(
                TrioJunctionSpec(TrioJunctionKind.LEG, -half, wantedChordAzimuth, "leg −w/2"),
                TrioJunctionSpec(TrioJunctionKind.LEG, half, wantedChordAzimuth, "leg +w/2")
            )
        }
    }
}

/** A crossbar phosphate, named by which backbone and which base pair it belongs to. */
data class CrossbarTarget(val strand: Int, val index: Int)

/** One junction seated on the crossbar, with the two crossbar phosphates its two termini reach. */
data class TrioPlacement(
    val name: String,
    val kind: TrioJunctionKind,
    val axialOffset: Double,
    val lateralOffset: Double,
    val azimuth: Double,
    val chordAzimuth: Double,
    val wantedChordAzimuth: Double,
    val firstGap: Double,
    val secondGap: Double,
    val firstTarget: CrossbarTarget,
    val secondTarget: CrossbarTarget,
    val firstTerminus: Vector3,
    val secondTerminus: Vector3,
    val seatContact: Double
) {

    /** The binding link. */
    val worstGap: Double get() = max(firstGap, secondGap)

    /** The binding link's distance from the measured `[0.60, 0.70]` nm step. */
    val worstResidual: Double
        get() = max(linkWindowResidual(firstGap), linkWindowResidual(secondGap))

    /** `C-0029`'s `P7`: both links inside the step, with no unpaired nucleotide. */
    val covalent: Boolean get() = worstResidual <= 0.0

    val firstUnpaired: Int get() = unpairedNucleotidesForGap(firstGap)

    val secondUnpaired: Int get() = unpairedNucleotidesForGap(secondGap)

    /** The chord's angle from the direction the design wants, in `[0, π/2]`. */
    val misalignment: Double get() = foldedChordMisalignment(chordAzimuth, wantedChordAzimuth)

    /** `cos²` of [misalignment] — the share of the junction's couple that reaches the wanted plane. */
    val wantedCoupleFraction: Double get() = couplePhaseProjection(misalignment)

    val targetsDistinct: Boolean get() = firstTarget != secondTarget
}

/** The whole trio on one crossbar, with every predicate `T-117` is written on as a property. */
data class CrossbarTrioClosure(
    val placements: List<TrioPlacement>,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val helicalPhase: Double,
    val axialPhase: Double,
    val lateralOffset: Double,
    val legFlexureClearance: Double,
    val minimumSeatContact: Double
) {

    /** `Q1`: every link of every junction closes covalently. */
    val allCovalent: Boolean get() = placements.all { it.covalent }

    val worstResidual: Double get() = placements.maxOf { it.worstResidual }

    val worstGap: Double get() = placements.maxOf { it.worstGap }

    /** `Q6`: the worst chord's departure from the direction the design wants, in radians. */
    val worstMisalignment: Double get() = placements.maxOf { it.misalignment }

    val unpairedNucleotides: Int
        get() = placements.sumOf { it.firstUnpaired + it.secondUnpaired }

    /** `Q2`: all six targets are different phosphates. */
    val distinctTargets: Boolean
        get() = placements.flatMap { listOf(it.firstTarget, it.secondTarget) }
            .toSet().size == 2 * placements.size

    /** The closest approach of any terminus of one junction to any terminus of another, in nm. */
    val minimumTerminusSeparation: Double
        get() {
            var smallest = Double.MAX_VALUE
            for (i in placements.indices) {
                for (j in i + 1 until placements.size) {
                    val a = listOf(placements[i].firstTerminus, placements[i].secondTerminus)
                    val b = listOf(placements[j].firstTerminus, placements[j].secondTerminus)
                    a.forEach { p -> b.forEach { q -> smallest = min(smallest, (p - q).length) } }
                }
            }
            return if (smallest == Double.MAX_VALUE) Double.POSITIVE_INFINITY else smallest
        }

    /** `Q3`: no two termini of different junctions are in van der Waals contact. */
    val terminiClear: Boolean
        get() = minimumTerminusSeparation >= BForm.PHOSPHATE_HARD_SEPARATION
}

// ---------------------------------------------------------------- the design, at a given azimuth

/**
 * `C-0048`'s design pipeline re-run at **stated** junction azimuths, so that what the chord-twist
 * quantisation costs is a subtraction and never a re-derivation.
 *
 * Every field is the quantity `C-0048`'s own `T106DesignRecord` carries under the same name, and
 * the assembly reproduces its recommended row from `(legLength = 7.00, w = 7 bp, 0, 0, 0)` — which
 * is the gate that licenses reading anything off the rest of the table.
 */
data class CapDesignPoint(
    val legSteps: Int,
    val legLength: Double,
    val flexureHeight: Double,
    val separationBasePairs: Int,
    val baseMisalignment: Double,
    val capMisalignment: Double,
    val flexureMisalignment: Double,
    val legRotation: Double,
    val headJunctionLoaded: Double,
    val headJunctionFree: Double,
    val flexureJunctionRotational: Double,
    val frameCouple: Double,
    val span: Double,
    val spanBasePairs: Double,
    val secant: Double,
    val tangent: Double,
    val minimumTangent: Double,
    val suppliedDrawIn: Double,
    val demandedDrawIn: Double,
    val supplyToDemand: Double,
    val duty: Double,
    val axialForce: Double,
    val loadedCriticalLoad: Double,
    val freeCriticalLoad: Double,
    val criticalLoad: Double,
    val criticalLoadFields: Double,
    val governingPlane: String,
    val marginCanDo: Double,
    val marginFields: Double,
    val transverseStiffness: Double,
    val verdict: String
) {

    val passes: Boolean get() = verdict == "PASS"
}

private const val CAP_TARGET_FORCE = 100.0
private const val CAP_ACCEPTABLE_STROKE = 3.0
private const val CAP_DESIRED_STROKE = 10.0
private const val CAP_MANDATE = CAP_TARGET_FORCE / CAP_ACCEPTABLE_STROKE
private const val CAP_PATH_COUNT = 45
private const val CAP_COMPLIANT_CEILING = 40.0
private const val CAP_SUPPORT_MARGIN = 10.0

/**
 * `C-0048`'s solved-cap design evaluated at a stated set of chord misalignments.
 *
 * @param baseMisalignment the leg's **base** chord, from the flexure's axis (`C-0042` achieves 0).
 * @param capMisalignment the leg's **head** chord, from **across** the flexure's axis — which is
 *   `C-0048`'s recommended orientation, so `0` is its recommended design and `π/2` its other corner.
 * @param flexureMisalignment the flexure's own end chord, from the **vertical**, which is the only
 *   orientation that restrains the flexure's own bending.
 */
fun capDesign(
    legLength: Double,
    separationBasePairs: Int = 7,
    baseMisalignment: Double = 0.0,
    capMisalignment: Double = 0.0,
    flexureMisalignment: Double = 0.0,
    legSteps: Int = 0,
    legRotation: Double = 0.0,
    orientation: FlexureOrientation = FlexureOrientation.FAVOURABLE,
    drawInModel: DrawInModel = DrawInModel.CHORD,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    fieldsRigidity: Double = FIELDS_BENDING_RIGIDITY,
    hard: DuplexBackbone = DuplexBackbone(minorGrooveAngle = 180.0)
): CapDesignPoint {
    require(legLength > 0.0) { "legLength must be positive, was: $legLength" }
    require(baseMisalignment >= 0.0 && baseMisalignment <= 0.25 * PI) {
        "baseMisalignment must lie in [0, pi/4] — past a half right angle the base's two axes " +
                "exchange and C-0037's TwoLinkBase invariant cannot represent it, was: " +
                baseMisalignment
    }
    val baseAxes = chordBaseAxes(hard, baseMisalignment)
    val base = TwoLinkBase(
        name = "two-terminus base at ${"%.1f".format(baseMisalignment * 180.0 / PI)}°",
        restrainedAxis = baseAxes.loaded,
        freeAxis = baseAxes.free,
        axial = 2.0 * bondSlideStiffness(),
        provenance = "C-0029's counting theorem via C-0042's chordBaseAxes, at a stated azimuth"
    )
    val cap = SolvedTrussCap(
        separationBasePairs = separationBasePairs,
        legLength = legLength,
        base = base,
        capJunctionMisalignment = 0.5 * PI - capMisalignment,
        flexureJunctionRotational = chordBaseAxes(hard, flexureMisalignment).loaded,
        bendingRigidity = bendingRigidity
    )
    val span = coupledFlexureSpan(
        bendingRigidity, cap.flexibility, CAP_PATH_COUNT, CAP_MANDATE, CAP_ACCEPTABLE_STROKE,
        orientation, cap.stretchModulus, drawInModel
    )
    val flexure = CoupledJointFlexure(
        bendingRigidity, span, cap.flexibility, cap.stretchModulus, drawInModel
    )
    val secant = CAP_PATH_COUNT * flexure.strokeSecantStiffness(CAP_ACCEPTABLE_STROKE, orientation)
    val tangent = CAP_PATH_COUNT * flexure.strokeTangentStiffness(CAP_ACCEPTABLE_STROKE, orientation)
    var minimumTangent = Double.MAX_VALUE
    for (i in 0..1000) {
        val value = CAP_PATH_COUNT *
                flexure.strokeTangentStiffness(i * CAP_DESIRED_STROKE / 1000.0, orientation)
        if (value < minimumTangent) minimumTangent = value
    }
    val duty = flexure.strokeEndShear(CAP_DESIRED_STROKE, orientation)
    val fields = cap.criticalLoad * fieldsRigidity / bendingRigidity
    val tension = flexure.strokeAxialForce(CAP_DESIRED_STROKE, orientation)
    val share = CAP_MANDATE * CAP_DESIRED_STROKE / CAP_PATH_COUNT
    val supplied = flexure.couplingFactor * CAP_ACCEPTABLE_STROKE
    val demanded = flexure.chordExtension(CAP_ACCEPTABLE_STROKE)
    val braced = bracedColumnBucklingLoad(bendingRigidity, span, flexure.restraint)
    val peakFlexure = peakFlexureCompression(flexure, orientation, CAP_DESIRED_STROKE)
    val flexureHeight = legLength + cap.geometry.rigidHeight

    val p1 = cap.transverseStiffness >= CAP_SUPPORT_MARGIN * CAP_MANDATE / CAP_PATH_COUNT &&
            cap.layout.stericallyRealisable
    val p3 = tangent <= CAP_COMPLIANT_CEILING
    val p4 = tension <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE && share <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    val p5 = flexureHeight <= 10.0 && span <= 60.0
    val p6 = cap.criticalLoad >= duty
    val p7 = peakFlexure <= braced
    val p8 = supplied >= demanded
    val p9 = duty / cap.legCount <= cap.criticalLoad / cap.legCount
    val verdict = when {
        !p1 -> "FAIL P1 — the legs do not support the flexure, or they overlap"
        !p3 -> "FAIL P3 — tangent past the 40 pN/nm compliance ceiling"
        !p4 -> "FAIL P4 — beam tension past the 10 pN unzip allowable at 10 nm"
        !p5 -> "FAIL P5 — outside C-0017's buildable envelope"
        !p6 -> "FAIL P6 — the truss buckles before the desired stroke"
        !p7 -> "FAIL P7 — the FLEXURE buckles under the compression the joint imposes"
        !p8 -> "FAIL P8 — the joint no longer supplies the draw-in it is charged for"
        !p9 -> "FAIL P9 — the head moment overloads one leg"
        else -> "PASS"
    }
    return CapDesignPoint(
        legSteps = legSteps,
        legLength = legLength,
        flexureHeight = flexureHeight,
        separationBasePairs = separationBasePairs,
        baseMisalignment = baseMisalignment,
        capMisalignment = capMisalignment,
        flexureMisalignment = flexureMisalignment,
        legRotation = legRotation,
        headJunctionLoaded = cap.headJunctionLoaded,
        headJunctionFree = cap.headJunctionFree,
        flexureJunctionRotational = cap.flexureJunctionRotational,
        frameCouple = cap.frameCouple,
        span = span,
        spanBasePairs = span / Gen1Tile.RISE_PER_BASE_PAIR,
        secant = secant,
        tangent = tangent,
        minimumTangent = minimumTangent,
        suppliedDrawIn = supplied,
        demandedDrawIn = demanded,
        supplyToDemand = supplied / demanded,
        duty = duty,
        axialForce = tension,
        loadedCriticalLoad = cap.loadedCriticalLoad,
        freeCriticalLoad = cap.freeCriticalLoad,
        criticalLoad = cap.criticalLoad,
        criticalLoadFields = fields,
        governingPlane = cap.governingPlane,
        marginCanDo = cap.criticalLoad / duty,
        marginFields = fields / duty,
        transverseStiffness = cap.transverseStiffness,
        verdict = verdict
    )
}

/**
 * The best design a leg of **[legSteps] base-pair steps** admits, once the chord-twist quantisation
 * is respected: the leg's own rotation is swept, which trades base misalignment against cap
 * misalignment one for one, and the rotation with the largest buckling margin is returned.
 *
 * The sweep is over `[0, π/4]`, which is where `C-0037`'s [TwoLinkBase] invariant holds — past a
 * half right angle the base's two axes exchange, and that is a modelling boundary, declared.
 */
fun quantisedCapDesign(
    legSteps: Int,
    separationBasePairs: Int = 7,
    flexureMisalignment: Double = 0.0,
    rotationSteps: Int = 16,
    backbone: DuplexBackbone = DuplexBackbone(),
    orientation: FlexureOrientation = FlexureOrientation.FAVOURABLE
): CapDesignPoint {
    require(legSteps >= 1) { "legSteps must be at least one, was: $legSteps" }
    require(rotationSteps >= 2) { "rotationSteps must be at least two, was: $rotationSteps" }
    val legLength = legSteps * backbone.risePerBasePair
    var best: CapDesignPoint? = null
    var bestRotation = 0.0
    var low = -0.25 * PI
    var high = 0.25 * PI
    // a two-level grid: the objective is smooth in the rotation, and a full sweep at the FE
    // solver's cost is minutes rather than seconds
    repeat(2) { round ->
        val step = (high - low) / rotationSteps
        for (i in 0..rotationSteps) {
            val rotation = low + i * step
            val split = legAzimuthSplit(legSteps, rotation, backbone)
            if (split.baseMisalignment > 0.25 * PI) continue
            val candidate = capDesign(
                legLength = legLength,
                separationBasePairs = separationBasePairs,
                baseMisalignment = split.baseMisalignment,
                capMisalignment = split.capMisalignment,
                flexureMisalignment = flexureMisalignment,
                legSteps = legSteps,
                legRotation = rotation,
                orientation = orientation
            )
            val incumbent = best
            if (incumbent == null || candidate.criticalLoad > incumbent.criticalLoad) {
                best = candidate
                bestRotation = rotation
            }
        }
        if (round == 0) {
            low = bestRotation - step
            high = bestRotation + step
        }
    }
    return requireNotNull(best)
}

// ---------------------------------------------------------------- the search

private const val RESIDUAL_WEIGHT = 1.0e4

/**
 * The closure search over **three** junctions on **one lone, finite** crossbar duplex.
 *
 * It is `C-0042`'s search with the three changes the geometry forces and no others:
 *
 * 1. the seat is finite, so its **rim** truncates both the contact and the phosphate lattice, and
 *    its helical phase `Φ` and axial phase `t` are **free**, because it is a free body;
 * 2. the junctions arrive in **two different directions**, so a leg's chord lives in `x–y` and the
 *    flexure's in `x–z`;
 * 3. the six links are checked for **mutual** distinctness and the whole assembly for van der Waals
 *    contact, rather than each junction alone.
 *
 * Deterministic by construction: fixed grids, a fixed local refinement, strict comparisons so the
 * lowest index wins every tie, and no floating-point tolerance anywhere in the control flow.
 *
 * @property lockedAzimuths if given, the junctions' own azimuths are **not** searched but taken from
 *   this list — which is the design question rather than the closure question, because a leg's cap
 *   azimuth is fixed by its base azimuth and its own length ([relativeChordAzimuth]).
 * @property contactFloor the smallest truncated seat contact a placement may have, in nm; `C-0042`
 *   adopted 1.6 nm on an unbounded seat and the same floor is kept here.
 */
class CrossbarTrioSearch(
    val backbone: DuplexBackbone = DuplexBackbone(),
    val crossbarBasePairs: Int = 13,
    val separationBasePairs: Int = 7,
    val junctions: List<TrioJunctionSpec> = TrioJunctionSpec.cap(7),
    val azimuthSteps: Int = 120,
    val refinements: Int = 2,
    val phaseSteps: Int = 90,
    val axialSteps: Int = 4,
    val lateralSeats: List<Double> = listOf(-0.4, -0.2, 0.0, 0.2, 0.4),
    val lockedAzimuths: List<Double?>? = null,
    val contactFloor: Double = 1.6
) {

    init {
        require(crossbarBasePairs >= 2) {
            "crossbarBasePairs must be at least two, was: $crossbarBasePairs"
        }
        require(separationBasePairs >= 1) {
            "separationBasePairs must be at least one, was: $separationBasePairs"
        }
        require(junctions.isNotEmpty()) { "junctions must not be empty" }
        require(azimuthSteps >= 8) { "azimuthSteps must be at least eight, was: $azimuthSteps" }
        require(refinements >= 0) { "refinements must not be negative, was: $refinements" }
        require(phaseSteps >= 1) { "phaseSteps must be at least one, was: $phaseSteps" }
        require(axialSteps >= 1) { "axialSteps must be at least one, was: $axialSteps" }
        require(lateralSeats.isNotEmpty()) { "lateralSeats must not be empty" }
        require(contactFloor >= 0.0) { "contactFloor must not be negative, was: $contactFloor" }
        require(lockedAzimuths == null || lockedAzimuths.size == junctions.size) {
            "lockedAzimuths must carry one azimuth per junction, was: ${lockedAzimuths?.size} " +
                    "for ${junctions.size}"
        }
    }

    val geometry: CrossbarGeometry =
        CrossbarGeometry(crossbarBasePairs, separationBasePairs, backbone.risePerBasePair)

    private val groove = backbone.minorGrooveAngle * PI / 180.0

    /**
     * The crossbar's phosphates at helical phase [phase] and axial phase [axial], flattened into
     * primitive arrays so the placement sweep costs no allocation and no trigonometry.
     */
    private class CrossbarLattice(count: Int) {
        val x = DoubleArray(count)
        val y = DoubleArray(count)
        val z = DoubleArray(count)
        val strand = IntArray(count)
        val index = IntArray(count)
    }

    private fun lattice(phase: Double, axial: Double): CrossbarLattice {
        val out = CrossbarLattice(2 * crossbarBasePairs)
        var at = 0
        for (index in 0 until crossbarBasePairs) {
            for (strand in 0..1) {
                val angle = phase + index * backbone.twistPerBasePair + strand * groove
                out.x[at] = axial + (index - 0.5 * (crossbarBasePairs - 1)) * backbone.risePerBasePair
                out.y[at] = backbone.phosphateRadius * cos(angle)
                out.z[at] = backbone.phosphateRadius * sin(angle)
                out.strand[at] = strand
                out.index[at] = index
                at++
            }
        }
        return out
    }

    /** The two termini of one junction at its own [azimuth]. */
    private fun termini(
        spec: TrioJunctionSpec,
        lateral: Double,
        azimuth: Double
    ): Pair<Vector3, Vector3> {
        val radius = backbone.phosphateRadius
        return when (spec.kind) {
            TrioJunctionKind.LEG -> {
                val height = loneSeatFaceHeight(lateral, backbone.duplexRadius, backbone.duplexRadius)
                Pair(
                    Vector3(
                        spec.axialOffset + radius * cos(azimuth),
                        lateral + radius * sin(azimuth),
                        -height
                    ),
                    Vector3(
                        spec.axialOffset + radius * cos(azimuth + groove),
                        lateral + radius * sin(azimuth + groove),
                        -height
                    )
                )
            }

            TrioJunctionKind.FLEXURE -> {
                val height = loneSeatFaceHeight(0.0, backbone.duplexRadius, backbone.duplexRadius)
                Pair(
                    Vector3(spec.axialOffset + radius * cos(azimuth), -height, radius * sin(azimuth)),
                    Vector3(
                        spec.axialOffset + radius * cos(azimuth + groove),
                        -height,
                        radius * sin(azimuth + groove)
                    )
                )
            }
        }
    }

    /** The chord azimuth of a junction at [azimuth], in its own chord plane. */
    fun chordAzimuthOf(azimuth: Double): Double = azimuth + 0.5 * groove + 0.5 * PI

    private fun placementAt(
        spec: TrioJunctionSpec,
        lateral: Double,
        azimuth: Double,
        phosphates: CrossbarLattice
    ): TrioPlacement? {
        val (first, second) = termini(spec, lateral, azimuth)
        var firstResidual = Double.MAX_VALUE
        var firstGap = Double.MAX_VALUE
        var firstAt = -1
        var secondResidual = Double.MAX_VALUE
        var secondGap = Double.MAX_VALUE
        var secondAt = -1
        for (i in phosphates.x.indices) {
            val px = phosphates.x[i]
            val py = phosphates.y[i]
            val pz = phosphates.z[i]
            var dx = px - first.x
            var dy = py - first.y
            var dz = pz - first.z
            val toFirst = sqrt(dx * dx + dy * dy + dz * dz)
            dx = px - second.x
            dy = py - second.y
            dz = pz - second.z
            val toSecond = sqrt(dx * dx + dy * dy + dz * dz)
            if (toFirst < BForm.PHOSPHATE_HARD_SEPARATION) return null
            if (toSecond < BForm.PHOSPHATE_HARD_SEPARATION) return null
            val residualFirst = linkWindowResidual(toFirst)
            if (residualFirst < firstResidual) {
                firstResidual = residualFirst
                firstGap = toFirst
                firstAt = i
            }
            val residualSecond = linkWindowResidual(toSecond)
            if (residualSecond < secondResidual) {
                secondResidual = residualSecond
                secondGap = toSecond
                secondAt = i
            }
        }
        if (firstAt < 0 || secondAt < 0 || firstAt == secondAt) return null
        val contact = when (spec.kind) {
            TrioJunctionKind.LEG -> boundedSeatContactLength(
                spec.axialOffset, geometry.length, lateral, backbone.duplexRadius
            )

            TrioJunctionKind.FLEXURE -> boundedSeatContactLength(
                spec.axialOffset, geometry.length, 0.0, backbone.duplexRadius
            )
        }
        if (contact < contactFloor) return null
        return TrioPlacement(
            name = spec.name,
            kind = spec.kind,
            axialOffset = spec.axialOffset,
            lateralOffset = if (spec.kind == TrioJunctionKind.LEG) lateral else 0.0,
            azimuth = azimuth,
            chordAzimuth = chordAzimuthOf(azimuth),
            wantedChordAzimuth = spec.wantedChordAzimuth,
            firstGap = firstGap,
            secondGap = secondGap,
            firstTarget = CrossbarTarget(phosphates.strand[firstAt], phosphates.index[firstAt]),
            secondTarget = CrossbarTarget(phosphates.strand[secondAt], phosphates.index[secondAt]),
            firstTerminus = first,
            secondTerminus = second,
            seatContact = contact
        )
    }

    /**
     * The best placement of one junction against a given crossbar, or `null` if no azimuth on the
     * grid closes both its links inside the measured step without a clash.
     *
     * The objective is `C-0042`'s: the window residual first, weighted so that no alignment can buy
     * a non-covalent link, and the design's own misalignment second.
     */
    private fun bestPlacement(
        spec: TrioJunctionSpec,
        index: Int,
        lateral: Double,
        phosphates: CrossbarLattice
    ): TrioPlacement? {
        // a null entry is a junction whose own azimuth is still free — the flexure's is, because
        // its other end is on a DIFFERENT crossbar and carries its own budget
        lockedAzimuths?.get(index)?.let { locked ->
            return placementAt(spec, lateral, locked, phosphates)
        }
        var best: TrioPlacement? = null
        var bestScore = Double.MAX_VALUE
        var bestAzimuth = 0.0
        var low = 0.0
        var high = 2.0 * PI
        repeat(refinements + 1) { round ->
            val step = (high - low) / azimuthSteps
            for (a in 0 until azimuthSteps) {
                val azimuth = low + a * step
                val candidate = placementAt(spec, lateral, azimuth, phosphates) ?: continue
                val score = RESIDUAL_WEIGHT * candidate.worstResidual + candidate.misalignment
                if (score < bestScore) {
                    bestScore = score
                    best = candidate
                    bestAzimuth = azimuth
                }
            }
            if (round < refinements) {
                low = bestAzimuth - step
                high = bestAzimuth + step
            }
        }
        return best
    }

    /**
     * The best admissible trio over the crossbar's helical phase, its axial phase and the shared
     * lateral seat, or `null` if none of them admits one.
     *
     * The objective is the **worst** junction: its residual first and its misalignment second,
     * because a truss is only as restrained as its softest junction — `C-0042`'s `worstLoadedCoupleFraction`
     * with one more body in the minimum.
     */
    fun best(): CrossbarTrioClosure? {
        var best: CrossbarTrioClosure? = null
        var bestScore = Double.MAX_VALUE
        val axialLimit = backbone.risePerBasePair
        lateralSeats.forEach { lateral ->
            for (p in 0 until phaseSteps) {
                val phase = 2.0 * PI * p / phaseSteps
                for (a in 0 until axialSteps) {
                    val axial = axialLimit * a / axialSteps
                    val phosphates = lattice(phase, axial)
                    val placements = junctions.mapIndexedNotNull { index, spec ->
                        bestPlacement(spec, index, lateral, phosphates)
                    }
                    if (placements.size < junctions.size) continue
                    val closure = CrossbarTrioClosure(
                        placements = placements,
                        crossbarBasePairs = crossbarBasePairs,
                        separationBasePairs = separationBasePairs,
                        helicalPhase = phase,
                        axialPhase = axial,
                        lateralOffset = lateral,
                        legFlexureClearance = geometry.legFlexureClearance,
                        minimumSeatContact = placements.minOf { it.seatContact }
                    )
                    if (!closure.distinctTargets || !closure.terminiClear) continue
                    val score = RESIDUAL_WEIGHT * closure.worstResidual + closure.worstMisalignment
                    if (score < bestScore) {
                        bestScore = score
                        best = closure
                    }
                }
            }
        }
        return best
    }
}
