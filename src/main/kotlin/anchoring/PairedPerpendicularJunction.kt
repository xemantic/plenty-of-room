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
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Task `T-97` — whether **two** 90° junctions can close on **one** sheet duplex, 6–8 bp apart.
 *
 * ## Why this is not `C-0029`'s question with a bigger grid
 *
 * `C-0029` places **one** standoff and reports one argmin: a scaffold excursion whose two links
 * both close at 0.600 nm, inside the measured `[0.60, 0.70]` nm phosphodiester step, with zero
 * unpaired nucleotides. `C-0037`'s recommended design needs **two**, in a row **along one sheet
 * duplex**, and it reports the row's pitch as *free* — *"the draw-in cost of a cross row is the
 * leg COUNT, not the leg SPACING"*. That statement is about the **frame**, and it was derived on a
 * mechanics model that never asks whether the second junction exists.
 *
 * The seat duplex is a **helix**. The sheet phosphates the second junction has to reach are
 * rotated by `n × 33.74°` about the seat duplex's own axis relative to the first junction's — so
 * if the second junction were the first one's **screw image**, its base chord would be rotated by
 * that angle too, and at `n = 8` — `C-0037`'s recommended pitch — that is **89.9°**, i.e. the
 * entire couple moved onto the wrong plane.
 *
 * **But a standoff must stand NORMAL to the sheet**, and a screw rotation about a horizontal axis
 * does not preserve that. So the second junction is not the first one's image at all: its own
 * azimuth about its own axis, and its axial position, are free, and it may reach a *different*
 * target pair. Whether that freedom is enough to put its chord back on the flexure's axis is a
 * two-parameter search against two 0.1 nm windows, and it is what this file does.
 *
 * ## Geometry and sign conventions
 *
 * The sheet is the `x–y` plane and `z` its normal. **The seat duplex runs along `x̂`** with its
 * axis at `y = 0, z = 0`; its neighbours sit at `y = ±2.69 nm`. **The flexure's own axis is `ŷ`**
 * — this is `C-0037`'s `x`, and the mapping is that claim's own: its *"across the flexure axis"*
 * row is *"along one sheet helix, quantised at the 0.34 nm rise"*. So the **loaded** plane is
 * `y–z`, the two legs are offset along `x̂`, and `C-0037`'s `Σx_i² = 0` is this file's
 * `Σ(Δy_i)² = 0` — which is why both legs are required to share one lateral seat.
 *
 * A base chord aligned with `ŷ` puts the base's strong couple in the **loaded** plane. The
 * misalignment `ψ` is the angle between the chord and `ŷ`, folded into `[0, π/2]` because **a
 * chord is a line, not a vector**.
 *
 * ## What is modelled and what is not
 *
 * Exactly `C-0029`'s admissibility test, applied twice: a phosphate pair inside the **measured**
 * step with no van der Waals overlap. It is a **necessary** condition and never a sufficient one —
 * no backbone torsion angle is checked and no sequence is designed — so a *"closes"* verdict is an
 * **upper bound on buildability**, and only a *"does not close"* verdict would be a proof of
 * impossibility. `T-71` is the torsion check and it can only make the answer worse.
 */

// ---------------------------------------------------------------- the cheap bounds

/**
 * **Cheap bound 1.** The smallest axial separation, in **base pairs**, at which two duplexes can
 * both stand on one seat duplex: `⌈2R/rise⌉`.
 *
 * At the B-form radius and the 0.34 nm rise this is **6 bp = 2.04 nm**, which puts `C-0037`'s
 * `L2a6` row exactly *on* the floor and leaves its `L2a8` row 0.72 nm of clearance.
 */
fun pairStericFloorBasePairs(
    standoffRadius: Double = BForm.DUPLEX_RADIUS,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Int {
    require(standoffRadius > 0.0) { "standoffRadius must be positive, was: $standoffRadius" }
    require(risePerBasePair > 0.0) { "risePerBasePair must be positive, was: $risePerBasePair" }
    return ceil(2.0 * standoffRadius / risePerBasePair).toInt()
}

/**
 * The **length of the line contact** a flat duplex end face of radius [standoffRadius] makes with
 * the top line of the cylinder it is seated on, when its axis sits [lateralOffset] nm to the side
 * of that cylinder's axis: `2√(R_s² − y_c²)`, and **zero** beyond the rim.
 *
 * It does **not** depend on the sheet duplex's radius, which is why it is a separate quantity from
 * [seatFaceHeight]: that function returns a face *height* for every offset out to the valley, and
 * a placement whose face height is legal can still be a standoff balanced on the very edge of its
 * seat. `C-0029` describes the on-duplex seat as *"a **line** of length `2R_s` along the helix"*;
 * this is that length, and it is the number a closure search has to be bounded by so that its
 * optimum is not an artefact.
 */
fun seatContactLength(
    lateralOffset: Double,
    standoffRadius: Double = BForm.DUPLEX_RADIUS
): Double {
    require(standoffRadius > 0.0) { "standoffRadius must be positive, was: $standoffRadius" }
    val squared = standoffRadius * standoffRadius - lateralOffset * lateralOffset
    return if (squared <= 0.0) 0.0 else 2.0 * sqrt(squared)
}

/** Folds an angle into `[−π/2, π/2]` modulo `π` — the fold a **line** obeys and a vector does not. */
private fun foldToLine(angle: Double): Double {
    var folded = angle % PI
    if (folded > 0.5 * PI) folded -= PI
    if (folded < -0.5 * PI) folded += PI
    return folded
}

/**
 * The misalignment in radians, in `[0, π/2]`, between a base chord at [chordAzimuth] and the
 * flexure's own axis [flexureAxis] (`ŷ` by default, i.e. `π/2` from the seat duplex's axis).
 *
 * Invariant under a half turn of the chord, because a chord is a **line**.
 */
fun foldedChordMisalignment(chordAzimuth: Double, flexureAxis: Double = 0.5 * PI): Double =
    abs(foldToLine(chordAzimuth - flexureAxis))

/**
 * **Cheap bound 2.** The chord rotation, in radians folded into `[0, π/2]`, that the second
 * junction would inherit if it were the first junction's **screw image** — the first translated
 * [separationBasePairs] along the seat duplex and rotated with it.
 *
 * | `n` bp | 6 | 7 | **8** | 9 | 10 | 11 | 16 |
 * |---|---|---|---|---|---|---|---|
 * | rotation | 22.4° | 56.2° | **89.9°** | 56.4° | 22.6° | 11.1° | 0.2° |
 *
 * **`C-0037`'s recommended 8 bp is the single worst separation in the 6–8 bp band on this bound**,
 * and it is the bound that says where to look. It binds only if the second standoff is *forced*
 * onto the screw image — which it is not, because a standoff must stand **normal** to the sheet
 * and a screw rotation about a horizontal axis does not preserve that. Its failure to bind is
 * therefore itself a result, and is what makes the expensive search worth running.
 */
fun screwImageChordRotation(
    separationBasePairs: Int,
    backbone: DuplexBackbone = DuplexBackbone()
): Double {
    require(separationBasePairs >= 0) {
        "separationBasePairs must not be negative, was: $separationBasePairs"
    }
    return abs(foldToLine(separationBasePairs * backbone.twistPerBasePair))
}

/**
 * The **sheet-phase residual**, in radians folded into `[0, π/2]`: how far from repeating itself
 * the seat duplex's phosphate azimuth is, [separationBasePairs] along, once the second junction is
 * allowed the two free moves the screw image does not have —
 *
 * - a **swap to the other backbone**, worth `±Δ` (the minor-groove angle), and
 * - a **half turn of the standoff about its own axis**, worth `π`, which is free because *a chord
 *   is a line*: `ψ` and `ψ + π` give the same chord direction and therefore the same couple.
 *
 * | `n` bp | 6 | **7** | 8 | 9 | 10 | 11 |
 * |---|---|---|---|---|---|---|
 * | screw image | 22.4° | 56.2° | 89.9° | 56.3° | 22.6° | 11.1° |
 * | **this residual** | 22.4° | **3.8°** | 29.9° | 3.7° | 22.6° | 11.1° |
 *
 * It is an **explanatory** quantity and not a bound: at 7 and 8 bp the search's two standoffs come
 * out as literal translates at one azimuth, which is what a small residual buys, but at 6 and
 * 11 bp they come out a half turn apart and at every separation the axial position, the seat and
 * the choice of target pair absorb whatever is left. Reported so that the mechanism is visible,
 * never used to decide anything.
 */
fun sheetPhaseResidual(
    separationBasePairs: Int,
    backbone: DuplexBackbone = DuplexBackbone()
): Double {
    require(separationBasePairs >= 0) {
        "separationBasePairs must not be negative, was: $separationBasePairs"
    }
    val groove = backbone.minorGrooveAngle * PI / 180.0
    var smallest = Double.MAX_VALUE
    for (strandSwap in -1..1) {
        val shift = separationBasePairs * backbone.twistPerBasePair + strandSwap * groove
        val folded = abs(foldToLine(shift))
        if (folded < smallest) smallest = folded
    }
    return smallest
}

/**
 * The fraction of a chord's couple that lands in the **loaded** plane at a misalignment of
 * [misalignment] radians — `cos²`, because a couple is a rank-one tensor `k a aᵀ`.
 *
 * The same function as `C-0029`'s [couplePhaseProjection], named for what it does here.
 */
fun loadedPlaneCoupleFraction(misalignment: Double): Double = couplePhaseProjection(misalignment)

/**
 * A two-link base's rotational stiffness read on **both** planes at once, at a chord misalignment
 * of `ψ` from the flexure's axis.
 *
 * @property loaded `2k_bond,θ + 2k_bond,s a² cos²ψ` — the plane the flexure loads.
 * @property free `2k_bond,θ + 2k_bond,s a² sin²ψ` — the plane the column would otherwise buckle in.
 */
data class ChordBaseAxes(val loaded: Double, val free: Double) {

    init {
        require(loaded > 0.0) { "loaded must be positive, was: $loaded" }
        require(free > 0.0) { "free must be positive, was: $free" }
    }

    /**
     * **The conserved budget.** `4k_bond,θ + 2k_bond,s a²`, independent of the azimuth — `C-0037`'s
     * rank-one identity `Σx_i² + Σy_i² = w²/2` one level down, on the *base* rather than on the
     * *frame*, and asserted as a gate-3 test.
     */
    val total: Double get() = loaded + free
}

/**
 * `C-0029`'s realisable two-link base, read on both its planes at a chord [misalignment] from the
 * flexure's axis.
 *
 * At `ψ = 0` this is exactly [realisablePerpendicularBase]'s favourable and unfavourable readings;
 * at `ψ = π/2` the two have **exchanged**, which is the whole content of this task.
 */
fun chordBaseAxes(
    backbone: DuplexBackbone = DuplexBackbone(),
    misalignment: Double,
    alpha: Double = 1.0,
    inPlaneMultiplier: Double = 1.0
): ChordBaseAxes {
    require(misalignment >= 0.0) { "misalignment must not be negative, was: $misalignment" }
    val hinge = 2.0 * bondHingeStiffness(alpha)
    val arm = backbone.leverArm
    val couple = 2.0 * bondSlideStiffness(alpha) * inPlaneMultiplier * arm * arm
    val c = cos(misalignment)
    val s = sin(misalignment)
    return ChordBaseAxes(hinge + couple * c * c, hinge + couple * s * s)
}

/**
 * **Cheap bound 3.** How many of the seat duplex's [phasePeriod] crossover phases (`C-0015`: 32 bp
 * per *interface*, so [crossoverPeriod] `= 16` bp along one duplex, alternating between the two
 * neighbours) leave **every** base pair in [targetIndices] free of a crossover.
 *
 * A junction's targets and a crossover cannot occupy the same backbone position, and the crossover
 * phase is a **design variable** with a 32 bp period quantised to base pairs, not a convergence
 * parameter — so the right output is a count of admissible phases, not a yes/no.
 */
fun crossoverFreePhaseCount(
    targetIndices: List<Int>,
    crossoverPeriod: Int = 16,
    phasePeriod: Int = 32
): Int {
    require(targetIndices.isNotEmpty()) { "targetIndices must not be empty" }
    require(crossoverPeriod > 0) { "crossoverPeriod must be positive, was: $crossoverPeriod" }
    require(phasePeriod > 0) { "phasePeriod must be positive, was: $phasePeriod" }
    val occupied = targetIndices.map { ((it % crossoverPeriod) + crossoverPeriod) % crossoverPeriod }
        .toSet()
    return (0 until phasePeriod).count { it % crossoverPeriod !in occupied }
}

// ---------------------------------------------------------------- one placement

/** A sheet phosphate, named by which duplex, which backbone and which base pair it belongs to. */
data class SheetTarget(val duplex: Int, val strand: Int, val index: Int)

/**
 * One standoff seated on the sheet, with the two sheet phosphates its two termini reach.
 *
 * The **chord azimuth is a function of the standoff's own azimuth alone** —
 * `ψ₀ + Δ/2 + π/2` — and of nothing else: not of where it sits, and not of what it links to. That
 * is what makes the alignment a one-parameter matter, and it is asserted as a gate-3 test.
 */
data class StandoffPlacement(
    val centreX: Double,
    val centreY: Double,
    val faceHeight: Double,
    val azimuth: Double,
    val chordAzimuth: Double,
    val firstGap: Double,
    val secondGap: Double,
    val firstTarget: SheetTarget,
    val secondTarget: SheetTarget,
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

    /** The chord's angle from the flexure's axis, in `[0, π/2]`. */
    val misalignment: Double get() = foldedChordMisalignment(chordAzimuth)

    /** `cos²` of [misalignment] — the share of the base couple that reaches the loaded plane. */
    val loadedCoupleFraction: Double get() = loadedPlaneCoupleFraction(misalignment)

    /** The two sheet targets, which `Q3` requires to be distinct. */
    val targetsDistinct: Boolean get() = firstTarget != secondTarget
}

/**
 * A pair of standoffs seated on one sheet duplex, [separationBasePairs] apart along it.
 *
 * Every predicate `T-97` is written on is a property of this object, so a verdict is a conjunction
 * and never an inspection.
 */
data class JunctionPairClosure(
    val first: StandoffPlacement,
    val second: StandoffPlacement,
    val separationBasePairs: Int
) {

    /** The axial pitch of the leg row, in nm. */
    val axialSeparation: Double get() = abs(second.centreX - first.centreX)

    /**
     * The lateral offset between the two legs, in nm. **`Q4` requires it to be exactly zero**:
     * `C-0037`'s cross row has `Σx_i² = 0` only if the row is straight, and a row that is not
     * straight spends frame couple on the plane that carries the draw-in.
     */
    val lateralSeparation: Double get() = abs(second.centreY - first.centreY)

    /** The plan-view distance between the two standoff axes, in nm. */
    val axisSeparation: Double get() = hypot(axialSeparation, lateralSeparation)

    /** The closest approach of any terminus of one junction to any terminus of the other, in nm. */
    val minimumTerminusSeparation: Double
        get() {
            val a = listOf(first.firstTerminus, first.secondTerminus)
            val b = listOf(second.firstTerminus, second.secondTerminus)
            return a.minOf { p -> b.minOf { q -> (p - q).length } }
        }

    /** `Q3`: all four sheet targets are different phosphates. */
    val distinctTargets: Boolean
        get() = setOf(
            first.firstTarget, first.secondTarget, second.firstTarget, second.secondTarget
        ).size == 4

    /** `Q1`: the two bodies clear each other and no two terminal phosphates are in contact. */
    val stericallyClear: Boolean
        get() = axisSeparation >= 2.0 * BForm.DUPLEX_RADIUS - 1.0e-9 &&
                minimumTerminusSeparation >= BForm.PHOSPHATE_HARD_SEPARATION

    /** `Q2`: both links of both junctions close covalently. */
    val bothCovalent: Boolean get() = first.covalent && second.covalent

    /** The **weaker** leg's share of its base couple in the loaded plane — the design number. */
    val worstLoadedCoupleFraction: Double
        get() = min(first.loadedCoupleFraction, second.loadedCoupleFraction)

    /** The larger of the two chords' misalignments, in radians. */
    val worstMisalignment: Double get() = max(first.misalignment, second.misalignment)

    /**
     * Every base pair either junction consumes, on whichever duplex.
     *
     * A crossover **column** sits at one base pair index across the whole sheet — along any one
     * duplex the columns recur every 16 bp, alternating between its two neighbours (`C-0015`) — so
     * a target on a *neighbour* duplex conflicts with the same phases as one on the seat duplex.
     */
    val targetBasePairs: List<Int>
        get() = listOf(
            first.firstTarget, first.secondTarget, second.firstTarget, second.secondTarget
        ).map { it.index }
}

// ---------------------------------------------------------------- the search

private const val RESIDUAL_WEIGHT = 1.0e4

/**
 * The closure search over a **pair** of standoffs on one seat duplex.
 *
 * It is `C-0029`'s search with two changes, and both are the task:
 *
 * 1. it returns the **best-aligned** covalent placement at each axial position rather than the
 *    single best-residual one — the alignment is a *field*, not an argmin, and a pair needs the
 *    field;
 * 2. it enforces one shared lateral seat for both legs (`Q4`), a steric floor and a terminal
 *    phosphate clearance between the two junctions (`Q1`), and four distinct targets (`Q3`).
 *
 * Deterministic by construction: fixed grids, a fixed local refinement, strict comparisons so the
 * lowest index wins every tie, and no floating-point tolerance anywhere in the search's control
 * flow.
 *
 * @property lateralSeats the shared seats `y_c` to try, in nm from the seat duplex's axis. The
 *   default keeps the flat-face line contact [seatContactLength] above 1.6 nm, i.e. it excludes
 *   the degenerate optima that stand a standoff on the very rim of its seat.
 */
class PairedJunctionSearch(
    val backbone: DuplexBackbone = DuplexBackbone(),
    val topology: RoutingTopology = RoutingTopology.INDEPENDENT_STAPLES,
    val interhelical: Double = Gen1Tile.INTERHELICAL_SHEET,
    val axialStepsPerBasePair: Int = 4,
    val azimuthSteps: Int = 180,
    val refinements: Int = 2,
    val lateralSeats: List<Double> = listOf(-0.6, -0.3, 0.0, 0.3, 0.6),
    val helicalRepeats: Int = 1,
    /**
     * Which sheet duplexes a link may reach. The default admits the seat duplex and its two
     * neighbours; `listOf(0)` is the **strict** reading of `T-97`'s own question — both junctions
     * grounded on **one** sheet duplex and nothing else.
     */
    val targetDuplexes: List<Int> = listOf(-1, 0, 1),
    /**
     * A tie-break weight on `|gap − 0.65 nm|`, i.e. on how far from the **middle** of the measured
     * phosphodiester step the links sit.
     *
     * Zero by default, so the search reports *a* covalent aligned solution and not a centred one.
     * Turning it on answers a question the headline would otherwise leave open: whether the
     * alignment is bought at the stretched (C2′-endo) end of the window or is available in its
     * interior.
     */
    val centringWeight: Double = 0.0
) {

    init {
        require(axialStepsPerBasePair >= 1) {
            "axialStepsPerBasePair must be at least one, was: $axialStepsPerBasePair"
        }
        require(azimuthSteps >= 8) { "azimuthSteps must be at least eight, was: $azimuthSteps" }
        require(refinements >= 0) { "refinements must not be negative, was: $refinements" }
        require(lateralSeats.isNotEmpty()) { "lateralSeats must not be empty" }
        require(helicalRepeats >= 1) {
            "helicalRepeats must be at least one, was: $helicalRepeats"
        }
        require(targetDuplexes.isNotEmpty()) { "targetDuplexes must not be empty" }
        require(centringWeight >= 0.0) {
            "centringWeight must not be negative, was: $centringWeight"
        }
    }

    /** The steric floor in base pairs, from [pairStericFloorBasePairs]. */
    val stericFloorBasePairs: Int =
        pairStericFloorBasePairs(backbone.duplexRadius, backbone.risePerBasePair)

    private val grooveRadians = backbone.minorGrooveAngle * PI / 180.0

    /** The axial grid step in nm — an exact submultiple of the rise, so `n` bp is `n·k` steps. */
    val axialStep: Double get() = backbone.risePerBasePair / axialStepsPerBasePair

    /** How many axial grid points one sweep carries. */
    private val sweepBasePairs: Int = helicalRepeats * 32

    private val fields = HashMap<Int, Array<StandoffPlacement?>>()

    /**
     * The best-aligned covalent placement at ([centreX], [centreY]), or `null` if no azimuth on the
     * grid closes both links inside the measured step without a clash.
     */
    fun bestAlignedPlacement(centreX: Double, centreY: Double): StandoffPlacement? {
        val faceHeight = seatFaceHeight(
            centreY, backbone.duplexRadius, backbone.duplexRadius, interhelical
        )
        val contact = seatContactLength(centreY, backbone.duplexRadius)
        val lattice = localLattice(centreX)
        var best: StandoffPlacement? = null
        var bestScore = Double.MAX_VALUE
        var bestAzimuth = 0.0
        var low = 0.0
        var high = 2.0 * PI
        repeat(refinements + 1) { round ->
            val step = (high - low) / azimuthSteps
            for (a in 0 until azimuthSteps) {
                val azimuth = low + a * step
                val candidate = placementAt(centreX, centreY, faceHeight, contact, azimuth, lattice)
                    ?: continue
                val score = RESIDUAL_WEIGHT * candidate.worstResidual + candidate.misalignment +
                        centringWeight * abs(
                    candidate.worstGap -
                            0.5 * (BForm.PHOSPHODIESTER_STEP_MIN + BForm.PHOSPHODIESTER_STEP)
                )
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
        val winner = best ?: return null
        return if (winner.covalent) winner else null
    }

    /**
     * The best pair at a separation of [separationBasePairs] base pairs along the seat duplex, or
     * `null` if no admissible pair exists at that separation on this grid.
     *
     * The objective is the **weaker** leg's loaded-plane couple fraction, because a truss is only
     * as restrained as its softer base — the mean is not the design number, which is `C-0020`'s
     * lesson and `C-0037`'s `P9` in a new place.
     */
    fun bestPair(separationBasePairs: Int): JunctionPairClosure? {
        require(separationBasePairs >= stericFloorBasePairs) {
            "two standoffs on one seat duplex cannot be closer than the steric floor of " +
                    "$stericFloorBasePairs bp, was: $separationBasePairs"
        }
        require(separationBasePairs <= 32) {
            "a separation beyond one helical repeat is outside the swept field, was: " +
                    separationBasePairs
        }
        val shift = separationBasePairs * axialStepsPerBasePair
        var best: JunctionPairClosure? = null
        var bestScore = -1.0
        lateralSeats.forEach { seat ->
            val field = fieldFor(seat)
            for (i in 0 until sweepBasePairs * axialStepsPerBasePair) {
                val a = field[i] ?: continue
                val b = field[i + shift] ?: continue
                val pair = JunctionPairClosure(a, b, separationBasePairs)
                if (!pair.bothCovalent || !pair.distinctTargets || !pair.stericallyClear) continue
                if (pair.worstLoadedCoupleFraction > bestScore) {
                    bestScore = pair.worstLoadedCoupleFraction
                    best = pair
                }
            }
        }
        return best
    }

    /** The alignment field along the seat duplex at one shared lateral seat, memoised. */
    private fun fieldFor(centreY: Double): Array<StandoffPlacement?> =
        fields.getOrPut(lateralSeats.indexOf(centreY)) {
            val count = (sweepBasePairs + 32) * axialStepsPerBasePair
            Array(count) { bestAlignedPlacement(it * axialStep, centreY) }
        }

    // ------------------------------------------------------------ the geometry, once per centreX

    /**
     * The phosphates within reach of one axial position.
     *
     * [eligible] separates the two jobs the lattice does: **every** phosphate of **every** duplex
     * takes part in the van der Waals clash test, while only those on an admitted duplex may be a
     * link target. Restricting the lattice itself would silently drop the clash test, which is the
     * half of the admissibility condition that can only refuse.
     */
    private class LocalLattice(
        val x: DoubleArray,
        val y: DoubleArray,
        val z: DoubleArray,
        val duplex: IntArray,
        val strand: IntArray,
        val index: IntArray,
        val eligible: BooleanArray
    )

    private fun localLattice(centreX: Double): LocalLattice {
        val reach = backbone.phosphateRadius + BForm.PHOSPHODIESTER_STEP + 0.05
        val lowIndex = floor((centreX - reach) / backbone.risePerBasePair).toInt()
        val highIndex = ceil((centreX + reach) / backbone.risePerBasePair).toInt()
        val indices = highIndex - lowIndex + 1
        val count = indices * 3 * 2
        val x = DoubleArray(count)
        val y = DoubleArray(count)
        val z = DoubleArray(count)
        val duplex = IntArray(count)
        val strand = IntArray(count)
        val index = IntArray(count)
        val eligible = BooleanArray(count)
        var at = 0
        for (i in lowIndex..highIndex) {
            for (d in -1..1) {
                for (s in 0..1) {
                    val p = backbone.sheetPhosphate(d * interhelical, s, i)
                    x[at] = p.x
                    y[at] = p.y
                    z[at] = p.z
                    duplex[at] = d
                    strand[at] = s
                    index[at] = i
                    eligible[at] = d in targetDuplexes
                    at++
                }
            }
        }
        return LocalLattice(x, y, z, duplex, strand, index, eligible)
    }

    private fun placementAt(
        centreX: Double,
        centreY: Double,
        faceHeight: Double,
        contact: Double,
        azimuth: Double,
        lattice: LocalLattice
    ): StandoffPlacement? {
        val first = backbone.standoffTerminus(centreX, centreY, faceHeight, azimuth, 0)
        val second = backbone.standoffTerminus(centreX, centreY, faceHeight, azimuth, 1)
        var firstResidual = Double.MAX_VALUE
        var firstGap = Double.MAX_VALUE
        var firstAt = -1
        for (i in lattice.x.indices) {
            val gap = distance(lattice, i, first)
            if (gap < BForm.PHOSPHATE_HARD_SEPARATION) return null
            if (distance(lattice, i, second) < BForm.PHOSPHATE_HARD_SEPARATION) return null
            if (!lattice.eligible[i]) continue
            val residual = linkWindowResidual(gap)
            if (residual < firstResidual) {
                firstResidual = residual
                firstGap = gap
                firstAt = i
            }
        }
        if (firstAt < 0) return null
        val firstTarget =
            SheetTarget(lattice.duplex[firstAt], lattice.strand[firstAt], lattice.index[firstAt])

        var secondResidual = Double.MAX_VALUE
        var secondGap = Double.MAX_VALUE
        var secondAt = -1
        for (i in lattice.x.indices) {
            if (i == firstAt || !lattice.eligible[i]) continue
            if (topology == RoutingTopology.SCAFFOLD_EXCURSION) {
                if (lattice.duplex[i] != firstTarget.duplex ||
                    lattice.strand[i] != firstTarget.strand ||
                    abs(lattice.index[i] - firstTarget.index) != 1
                ) continue
            }
            val gap = distance(lattice, i, second)
            val residual = linkWindowResidual(gap)
            if (residual < secondResidual) {
                secondResidual = residual
                secondGap = gap
                secondAt = i
            }
        }
        if (secondAt < 0) return null

        val chord = second - first
        return StandoffPlacement(
            centreX = centreX,
            centreY = centreY,
            faceHeight = faceHeight,
            azimuth = azimuth,
            chordAzimuth = kotlin.math.atan2(chord.y, chord.x),
            firstGap = firstGap,
            secondGap = secondGap,
            firstTarget = firstTarget,
            secondTarget = SheetTarget(
                lattice.duplex[secondAt], lattice.strand[secondAt], lattice.index[secondAt]
            ),
            firstTerminus = first,
            secondTerminus = second,
            seatContact = contact
        )
    }

    private fun distance(lattice: LocalLattice, i: Int, point: Vector3): Double {
        val dx = lattice.x[i] - point.x
        val dy = lattice.y[i] - point.y
        val dz = lattice.z[i] - point.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /** The chord azimuth a standoff at [azimuth] has, whatever it links to — `ψ₀ + Δ/2 + π/2`. */
    fun chordAzimuthOf(azimuth: Double): Double = azimuth + 0.5 * grooveRadians + 0.5 * PI
}

// ---------------------------------------------------------------- the mixed-base truss

/**
 * The critical **total** axial load in pN of a truss whose legs no longer share one base constant.
 *
 * `C-0028`'s sway determinant `sin u(u² − ρ_bρ_h) − cos u(ρ_b + ρ_h)u` is written for **one**
 * column, and `C-0037` multiplies its root by the leg count — legitimate only because every leg
 * there has the same base. Two chords at different azimuths give two different `ρ_b`, and the
 * assembly is then a genuine two-degree-of-freedom eigenproblem.
 *
 * It is solved here as a **beam-column finite element**: [elementsPerLeg] Hermite-cubic elements
 * per leg with the consistent geometric stiffness, each leg's base pinned in translation and held
 * by its own rotational spring, one shared head node carrying `(u, φ)` and [frameCouple] on `φ`,
 * and every leg carrying the same share `P/n` of the load. The critical load is the smallest total
 * `P` at which the assembled matrix stops being positive definite, found by bisection on the sign
 * of the first non-positive pivot of an `LDLᵀ` factorisation — which is Sylvester's criterion and
 * is exact, not a tolerance.
 *
 * It reproduces [trussBucklingLoad] in the equal-base case through a completely different route,
 * which is the gate-2 assertion that licenses everything downstream of it.
 *
 * Returns **exactly zero** when the assembly is a mechanism at zero load — `C-0028`'s *"a pinned
 * base under a sway column is a MECHANISM, not a weaker strut"*.
 */
fun mixedBaseTrussBucklingLoad(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffnesses: List<Double>,
    frameCouple: Double,
    elementsPerLeg: Int = 32
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(baseRotationalStiffnesses.isNotEmpty()) { "a truss must have at least one leg" }
    require(baseRotationalStiffnesses.all { it >= 0.0 }) {
        "a base rotational stiffness must not be negative: $baseRotationalStiffnesses"
    }
    require(frameCouple >= 0.0) { "frameCouple must not be negative, was: $frameCouple" }
    require(elementsPerLeg >= 1) { "elementsPerLeg must be at least one, was: $elementsPerLeg" }

    val legs = baseRotationalStiffnesses.size
    val perLeg = 2 * elementsPerLeg - 1
    val size = 2 + legs * perLeg
    val elastic = Array(size) { DoubleArray(size) }
    val geometric = Array(size) { DoubleArray(size) }
    val h = length / elementsPerLeg

    val ke = beamElasticMatrix(bendingRigidity, h)
    val kg = beamGeometricMatrix(h)

    baseRotationalStiffnesses.forEachIndexed { leg, baseStiffness ->
        val offset = 2 + leg * perLeg
        fun dof(node: Int, component: Int): Int = when {
            node == 0 -> if (component == 0) -1 else offset
            node == elementsPerLeg -> component
            else -> offset + 2 * node - 1 + component
        }
        for (e in 0 until elementsPerLeg) {
            val map = intArrayOf(dof(e, 0), dof(e, 1), dof(e + 1, 0), dof(e + 1, 1))
            for (a in 0..3) {
                val ga = map[a]
                if (ga < 0) continue
                for (b in 0..3) {
                    val gb = map[b]
                    if (gb < 0) continue
                    elastic[ga][gb] += ke[a][b]
                    geometric[ga][gb] += kg[a][b]
                }
            }
        }
        elastic[offset][offset] += baseStiffness
    }
    elastic[1][1] += frameCouple

    val lower = Array(size) { DoubleArray(size) }
    val pivots = DoubleArray(size)
    fun stable(total: Double): Boolean =
        positiveDefinite(elastic, geometric, total, legs, lower, pivots)

    if (!stable(0.0)) return 0.0
    var high = legs * PI * PI * bendingRigidity / (length * length)
    var doubled = 0
    while (stable(high) && doubled < 40) {
        high *= 2.0
        doubled++
    }
    var low = 0.0
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (stable(middle)) low = middle else high = middle
        if (high - low <= 1.0e-14 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The assembled 2 × 2 tip flexibility of a truss whose legs no longer share one base constant —
 * `C-0037`'s [trussTipFlexibility] with the leg count replaced by a **list of bases**.
 *
 * Legs sharing a rigid cap act in parallel, so their tip *stiffnesses* add and the frame couple
 * adds to the rotation-rotation entry alone. With every base equal this returns
 * [trussTipFlexibility] entry by entry, which is asserted as a gate-2 test.
 */
fun mixedBaseTrussTipFlexibility(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffnesses: List<Double>,
    frameCouple: Double
): StandoffTipFlexibility {
    require(baseRotationalStiffnesses.isNotEmpty()) { "a truss must have at least one leg" }
    require(frameCouple >= 0.0) { "frameCouple must not be negative, was: $frameCouple" }
    var k11 = 0.0
    var k12 = 0.0
    var k21 = 0.0
    var k22 = 0.0
    baseRotationalStiffnesses.forEach { base ->
        val leg = standoffTipFlexibility(bendingRigidity, length, base)
        val determinant = leg.translationUnderForce * leg.rotationUnderMoment -
                leg.translationUnderMoment * leg.rotationUnderForce
        require(determinant > 0.0) { "a leg's flexibility is not invertible: $determinant" }
        k11 += leg.rotationUnderMoment / determinant
        k12 += -leg.translationUnderMoment / determinant
        k21 += -leg.rotationUnderForce / determinant
        k22 += leg.translationUnderForce / determinant
    }
    k22 += frameCouple
    val determinant = k11 * k22 - k12 * k21
    require(determinant > 0.0) { "the assembled truss is not invertible: $determinant" }
    return StandoffTipFlexibility(
        translationUnderForce = k22 / determinant,
        translationUnderMoment = -k12 / determinant,
        rotationUnderForce = -k21 / determinant,
        rotationUnderMoment = k11 / determinant
    )
}

/** The Hermite-cubic beam element's elastic stiffness, DOFs `(w_i, θ_i, w_j, θ_j)`. */
private fun beamElasticMatrix(bendingRigidity: Double, h: Double): Array<DoubleArray> {
    val c = bendingRigidity / (h * h * h)
    return arrayOf(
        doubleArrayOf(12.0 * c, 6.0 * h * c, -12.0 * c, 6.0 * h * c),
        doubleArrayOf(6.0 * h * c, 4.0 * h * h * c, -6.0 * h * c, 2.0 * h * h * c),
        doubleArrayOf(-12.0 * c, -6.0 * h * c, 12.0 * c, -6.0 * h * c),
        doubleArrayOf(6.0 * h * c, 2.0 * h * h * c, -6.0 * h * c, 4.0 * h * h * c)
    )
}

/** The consistent geometric stiffness **per unit axial compression**. */
private fun beamGeometricMatrix(h: Double): Array<DoubleArray> {
    val c = 1.0 / (30.0 * h)
    return arrayOf(
        doubleArrayOf(36.0 * c, 3.0 * h * c, -36.0 * c, 3.0 * h * c),
        doubleArrayOf(3.0 * h * c, 4.0 * h * h * c, -3.0 * h * c, -h * h * c),
        doubleArrayOf(-36.0 * c, -3.0 * h * c, 36.0 * c, -3.0 * h * c),
        doubleArrayOf(3.0 * h * c, -h * h * c, -3.0 * h * c, 4.0 * h * h * c)
    )
}

/**
 * Whether `elastic − (total/legs)·geometric` is positive definite, by `LDLᵀ` with no pivoting —
 * Sylvester's criterion on the leading principal minors, which is exact.
 */
private fun positiveDefinite(
    elastic: Array<DoubleArray>,
    geometric: Array<DoubleArray>,
    total: Double,
    legs: Int,
    l: Array<DoubleArray>,
    d: DoubleArray
): Boolean {
    val size = elastic.size
    val share = total / legs
    for (j in 0 until size) {
        var pivot = elastic[j][j] - share * geometric[j][j]
        for (k in 0 until j) pivot -= l[j][k] * l[j][k] * d[k]
        if (pivot <= 0.0) return false
        d[j] = pivot
        for (i in j + 1 until size) {
            var value = elastic[i][j] - share * geometric[i][j]
            for (k in 0 until j) value -= l[i][k] * l[j][k] * d[k]
            l[i][j] = value / pivot
        }
    }
    return true
}
