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

import com.xemantic.nano.plentyofroom.actuator.ActuatorGeometry
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Tasks `T-75` and `T-78` — **which body carries the standoffs**, and **what sits under the
 * flexure's midspan**.
 *
 * ## The question `C-0030` left open, and why it is not the right question
 *
 * `C-0030` shows that the coupled flexure's law is signed but **not odd** — `Φδ` is odd where the
 * arc-length demand `e(δ)` is even — so one sense of the midspan deflection relieves the beam and
 * the other loads it, and the two differ by a whole design window (`ℓ = 5–10 nm` against no
 * admissible length at all). It names the deciding variable *"which body carries the standoffs"*,
 * calls it free to a builder, and files it as a specification gap.
 *
 * **It is not free, and it is not that variable.** The flexure's midspan is tied to one body and
 * its ends stand on standoffs rooted in the other, so the midspan's deflection *relative to its own
 * ends* is exactly the change in the two bodies' separation. Differentiating that chain along the
 * stroke gives, with no free parameter,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`dδ/ds = (v_base − v_driven)/n`**,
 * &nbsp;&nbsp;`v_TILE = −1`, `v_SUPERSTRUCTURE = 0`, `n = ±1`,
 *
 * where `δ` is counted positive **toward the standoff base plane** (`C-0030`'s favourable sense)
 * and `n` is the direction the standoffs point out of that plane. Three consequences, each of them
 * a test in `FlexureMountingSenseTest` rather than a remark:
 *
 * 1. **No length appears.** Neither the tie, nor the standoff, nor the span can move the sign.
 * 2. **The sign is a PRODUCT of two binaries.** Flipping the base body flips it; flipping the
 *    standoff normal flips it. So *"which body carries the standoffs"* decides nothing on its own —
 *    it is half of the variable, and `C-0030` named that half.
 * 3. **`|dδ/ds| = 1` exactly**, so the flexure's deflection *is* the stroke.
 *
 * ## What the sign is equivalent to, geometrically and mechanically
 *
 * The favourable sense turns out to be the same statement three ways over, and the equivalences are
 * asserted rather than assumed:
 *
 * - the **driven body lies on the far side of the standoff base plane from the beam**, i.e. the
 *   midspan's tie **crosses that plane** ([MountingStack.tieCrossesBasePlane]);
 * - the flexure is mounted **outboard** of its own ground rather than **inboard** between the two
 *   bodies;
 * - the standoff's axial duty is **compression** ([StandoffAxialSense]) — which is why `C-0028`'s
 *   and `C-0030`'s buckling predicate exists at all. **A standoff in tension does not buckle**, and
 *   the adverse mounting puts it in tension.
 *
 * ## And what the favourable sense then costs — `T-78`
 *
 * If the tie crosses the base plane, the body the standoffs stand on is *underneath the midspan*,
 * necessarily and by construction. So `C-0030`'s clearance ceiling `ℓ − d` is not a property of a
 * body §3 failed to specify: it is a property of the only topology that delivers the favourable
 * sense at all. What is a design choice is the **aperture** — and the tie has to pass through one
 * anyway, at exactly the place the midspan descends toward. [apertureLength] prices it from the
 * beam's own solved shape.
 */

// ---------------------------------------------------------------- the stack's two bodies

/**
 * One of the two bodies the output coupling joins, with the velocity of its own plane per unit
 * **stroke**.
 *
 * §1 fixes this and nothing else in this file is physics: *"positive bias on the electrode applies
 * a downward force on the tile"*, so the tile descends by exactly the stroke and the superstructure
 * — `C-0017`'s output lever, the coupling's far ground — does not move. Every sign in `T-75`
 * follows from these two numbers.
 */
enum class MountingBody(val strokeVelocity: Double) {

    /** §1's origami tile. It descends by exactly the stroke. */
    TILE(-1.0),

    /** `C-0017`'s output superstructure — the coupling's far ground. */
    SUPERSTRUCTURE(0.0);

    /** The other body of the pair. */
    val other: MountingBody get() = if (this == TILE) SUPERSTRUCTURE else TILE
}

/** Which way the standoffs point out of their base body's plane, along `ActuatorGeometry`'s `z`. */
enum class StandoffNormal(val sense: Double) {

    /** Away from the electrode. */
    UPWARD(1.0),

    /** Toward the electrode. */
    DOWNWARD(-1.0);

    val reversed: StandoffNormal get() = if (this == UPWARD) DOWNWARD else UPWARD
}

/**
 * Whether the flexure's end shear puts the standoff into compression — where it can buckle, which
 * is `C-0028`'s and `C-0030`'s `P6` — or into tension, where it cannot.
 */
enum class StandoffAxialSense { COMPRESSION, TENSION }

/**
 * A **mounting**: which body the standoff bases stand on, and which way the standoffs point out of
 * that body's plane. These two binaries are the whole of the design variable `C-0030` half-named.
 */
data class FlexureMounting(
    val baseBody: MountingBody,
    val standoffNormal: StandoffNormal
) {

    /** The body the midspan is tied to — the one the standoffs do **not** stand on. */
    val drivenBody: MountingBody get() = baseBody.other

    /**
     * **`dδ/ds`** — the rate at which the midspan deflects **toward** its own standoff base plane
     * per unit stroke. Exactly `±1`, and it contains no length.
     */
    val deflectionRate: Double
        get() = (baseBody.strokeVelocity - drivenBody.strokeVelocity) / standoffNormal.sense

    /** `C-0030`'s orientation, **derived** from [deflectionRate] rather than chosen. */
    val orientation: FlexureOrientation
        get() = if (deflectionRate > 0.0) FlexureOrientation.FAVOURABLE else FlexureOrientation.ADVERSE

    /**
     * The standoff's axial duty over the stroke. The beam's end shear acts **along the standoff's
     * own axis**, and it points toward the base exactly when the midspan does — so this is the same
     * binary as [orientation], and it is why the buckling predicate belongs to one mounting only.
     */
    val standoffAxialSense: StandoffAxialSense
        get() = if (deflectionRate > 0.0) StandoffAxialSense.COMPRESSION else StandoffAxialSense.TENSION

    /**
     * Whether the flexure ends up **below the tile**, i.e. inside the actuation gap the polymer
     * layer occupies. True only for standoffs pointing down off the tile.
     */
    val putsFlexureUnderTheTile: Boolean
        get() = baseBody == MountingBody.TILE && standoffNormal == StandoffNormal.DOWNWARD

    /** Whether the beam lies **between** the two bodies (inboard) rather than outboard of its ground. */
    val inboard: Boolean get() = deflectionRate < 0.0

    /** A stable label, used by the study's own reporting and by the result file. */
    val id: String
        get() = "${if (baseBody == MountingBody.TILE) "T" else "S"}${if (standoffNormal == StandoffNormal.UPWARD) "u" else "d"}"

    /**
     * The smallest height above the tile's **top** face at which the load can enter the
     * superstructure, in nm — §3's *effort point*, as this mounting is able to realise it with a
     * non-negative tie.
     *
     * The inboard topologies stack the standoff and the tie in series between the two bodies, so
     * their floor is the **standoff length**; the outboard ones fold the tie back past the base
     * plane and have no floor at all.
     */
    fun minimumEffortHeightAboveTileTop(
        standoffLength: Double,
        tileThickness: Double = ActuatorGeometry().tileThickness
    ): Double {
        require(standoffLength > 0.0) { "standoffLength must be positive, was: $standoffLength" }
        require(tileThickness > 0.0) { "tileThickness must be positive, was: $tileThickness" }
        return max(0.0, effortHeightAboveTileTop(0.0, standoffLength, tileThickness))
    }

    /**
     * The height of the effort point above the tile's top face, in nm, for a tie of length
     * [tieLength] — `ℓ + m` inboard, and `m − ℓ` (less the tile's own thickness where the tie has to
     * cross it) outboard.
     */
    fun effortHeightAboveTileTop(
        tieLength: Double,
        standoffLength: Double,
        tileThickness: Double = ActuatorGeometry().tileThickness
    ): Double = when {
        inboard -> standoffLength + tieLength
        putsFlexureUnderTheTile -> tieLength - standoffLength - tileThickness
        else -> tieLength - standoffLength
    }

    /** The tie length in nm that puts the effort point [effortHeight] above the tile's top face. */
    fun tieLengthForEffortHeight(
        effortHeight: Double,
        standoffLength: Double,
        tileThickness: Double = ActuatorGeometry().tileThickness
    ): Double {
        val tie = when {
            inboard -> effortHeight - standoffLength
            putsFlexureUnderTheTile -> effortHeight + standoffLength + tileThickness
            else -> effortHeight + standoffLength
        }
        require(tie >= 0.0) {
            "mounting $id cannot place the effort point $effortHeight nm above the tile with a " +
                    "$standoffLength nm standoff: it needs a tie of $tie nm"
        }
        return tie
    }

    /**
     * The longest standoff in nm this mounting can carry at [layerHeight] without pushing the
     * effort point above [effortCeiling] nm over the electrode — §3's *"~20–25 nm"*, read
     * **loosely**, i.e. as a band the effort point must lie in at every layer height rather than
     * as one constant attachment height.
     *
     * `+∞` for the outboard mountings, which fold the tie back past their own base plane and
     * therefore put no floor under the effort point at all.
     */
    fun maximumStandoffLengthUnderEffortCeiling(
        layerHeight: Double,
        effortCeiling: Double,
        tileThickness: Double = ActuatorGeometry().tileThickness
    ): Double {
        require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
        require(effortCeiling > 0.0) { "effortCeiling must be positive, was: $effortCeiling" }
        if (!inboard) return Double.POSITIVE_INFINITY
        return max(0.0, effortCeiling - (layerHeight + tileThickness))
    }

    companion object {

        /** All four mountings, in a fixed order so that result files are reproducible. */
        val ALL: List<FlexureMounting> = listOf(
            FlexureMounting(MountingBody.TILE, StandoffNormal.UPWARD),
            FlexureMounting(MountingBody.TILE, StandoffNormal.DOWNWARD),
            FlexureMounting(MountingBody.SUPERSTRUCTURE, StandoffNormal.DOWNWARD),
            FlexureMounting(MountingBody.SUPERSTRUCTURE, StandoffNormal.UPWARD)
        )
    }
}

// ---------------------------------------------------------------- the built stack

/**
 * A [FlexureMounting] placed in the §1 stack, so that every plane it implies has an actual `z`
 * above the electrode and the sign can be recovered a **second**, independent way — by comparing
 * three coordinates rather than by differentiating a chain.
 *
 * @param layerHeight the polymer layer height in nm, which is also the tile's bottom face.
 * @param standoffLength `ℓ` in nm.
 * @param tieLength `m` in nm — the midspan-to-driven-body link, at zero deflection.
 */
class MountingStack(
    val mounting: FlexureMounting,
    val layerHeight: Double,
    val standoffLength: Double,
    val tieLength: Double,
    val geometry: ActuatorGeometry = ActuatorGeometry()
) {

    init {
        require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
        require(standoffLength > 0.0) { "standoffLength must be positive, was: $standoffLength" }
        require(tieLength >= 0.0) { "tieLength must not be negative, was: $tieLength" }
    }

    /** The tile's bottom face, in nm above the electrode. */
    val tileBottomFace: Double get() = geometry.tileBottomFace(layerHeight)

    /** The tile's top face, in nm above the electrode. */
    val tileTopFace: Double get() = geometry.tileTopFace(layerHeight)

    /** The plane the standoff bases stand on, in nm above the electrode. */
    val basePlane: Double
        get() = when {
            mounting.baseBody == MountingBody.TILE ->
                if (mounting.standoffNormal == StandoffNormal.UPWARD) tileTopFace else tileBottomFace
            else -> superstructurePlane
        }

    /** The plane the flexure's ends sit in, in nm above the electrode. */
    val beamPlane: Double get() = basePlane + mounting.standoffNormal.sense * standoffLength

    /** Where the load enters the superstructure — §3's effort point, in nm above the electrode. */
    val superstructurePlane: Double
        get() = tileTopFace + mounting.effortHeightAboveTileTop(
            tieLength, standoffLength, geometry.tileThickness
        )

    /** The effort point's height above the tile's top face, in nm. */
    val effortHeightAboveTileTop: Double
        get() = mounting.effortHeightAboveTileTop(tieLength, standoffLength, geometry.tileThickness)

    /** The plane the tie's far end attaches to, in nm above the electrode. */
    val drivenAttachmentPlane: Double
        get() = if (mounting.drivenBody == MountingBody.SUPERSTRUCTURE) superstructurePlane
        else tileTopFace

    /** The midspan's height in nm above the electrode at a deflection [deflection] toward the base. */
    fun midspanHeight(deflection: Double): Double =
        beamPlane - mounting.standoffNormal.sense * deflection

    /**
     * Whether the tie from the midspan to the driven body **crosses the standoff base plane** — the
     * geometric statement of the favourable sense, computed here from three `z` coordinates and
     * nowhere from [FlexureMounting.deflectionRate].
     */
    val tieCrossesBasePlane: Boolean
        get() {
            val midspan = midspanHeight(0.0)
            return (midspan - basePlane) * (drivenAttachmentPlane - basePlane) < 0.0
        }

    /** `dδ/ds`, re-exposed from the mounting so that the stack can be asserted against it. */
    val deflectionRate: Double get() = mounting.deflectionRate

    /** Whether the flexure's own plane lies below the tile, i.e. inside the actuation gap. */
    val beamInsideActuationGap: Boolean get() = beamPlane < tileBottomFace

    /** Whether the flexure's own duplex clears the electrode surface. */
    fun beamClearsElectrode(duplexRadius: Double = 0.5 * OrigamiDuplex.DIAMETER): Boolean =
        beamPlane - duplexRadius > 0.0

    /** Whether the tie has to pass through the tile's own slab. */
    val tieCrossesTile: Boolean
        get() {
            val low = min(midspanHeight(0.0), drivenAttachmentPlane)
            val high = max(midspanHeight(0.0), drivenAttachmentPlane)
            return low < tileBottomFace && high > tileTopFace
        }

    /** Whether the tie has to pass through the superstructure's own plane. */
    val tieCrossesSuperstructure: Boolean
        get() = mounting.baseBody == MountingBody.SUPERSTRUCTURE && tieCrossesBasePlane
}

/** The one duplex dimension this file needs, kept beside the mounting model rather than re-quoted. */
object OrigamiDuplex {

    /** B-DNA's steric diameter in nm — the phosphate backbone IS the surface (`CLAUDE.md`). */
    const val DIAMETER: Double = 2.0

    /** The SAXS-measured single-layer interhelical distance in nm — Fischer et al. (2016). */
    const val INTERHELICAL: Double = Gen1Tile.INTERHELICAL_SHEET
}

// ---------------------------------------------------------------- T-78 — the beam's own shape

/**
 * The normalised deflected shape `w(u)/δ` of `C-0025`'s partially restrained flexure under its
 * midspan load, at [position] `u = x/L ∈ [0, 1]`:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`w(u)/δ = (24u + 12ρu² − 16(2+ρ)u³)/(8+ρ)` for `u ≤ ½`, symmetric about `½`.
 *
 * This is one further integration of the beam `C-0025` already solves, and it is verifiable at three
 * points that were not used to build it: it is `3u − 4u³` at `ρ = 0`, `12u² − 16u³` at `ρ → ∞`, and
 * its end slope is `24/(8+ρ)`, which is `L θ₀/δ` read off the beam's own solution.
 *
 * `T-78` needs it because the clearance question is not *"how deep does the midspan go"* but
 * *"how long is the hole it needs"*.
 */
fun restrainedBeamShape(restraint: Double, position: Double): Double {
    require(restraint >= 0.0) { "restraint must not be negative, was: $restraint" }
    require(position in 0.0..1.0) { "position must lie in [0, 1], was: $position" }
    val u = if (position <= 0.5) position else 1.0 - position
    if (restraint.isInfinite()) return 12.0 * u * u - 16.0 * u * u * u
    return (24.0 * u + 12.0 * restraint * u * u - 16.0 * (2.0 + restraint) * u * u * u) /
            (8.0 + restraint)
}

/**
 * The fraction `u*`, i.e. `x* over L`, in `(0, ½]` at which [restrainedBeamShape] first reaches [level] — the
 * inboard edge of the aperture the sagging beam demands, in units of the span.
 *
 * The shape is strictly increasing on `[0, ½]` (its derivative vanishes only at the midspan, by
 * symmetry), so a bisection on the **bracket width** is safe and exits at machine precision.
 */
fun apertureHalfPositionFraction(restraint: Double, level: Double, scanSteps: Int = 256): Double {
    require(restraint >= 0.0) { "restraint must not be negative, was: $restraint" }
    require(level in 0.0..1.0) { "level must lie in [0, 1], was: $level" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    if (level <= 0.0) return 0.0
    if (level >= 1.0) return 0.5
    var low = 0.0
    var high = 0.5
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (restrainedBeamShape(restraint, middle) < level) low = middle else high = middle
        if (high - low <= 1.0e-16) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The largest stroke in nm the favourable mounting has room for before the midspan reaches the body
 * its own standoffs stand on — `C-0030`'s [favourableStrokeClearance], re-exported here because
 * `T-78` shows that body is not optional.
 */
fun midspanClearance(
    standoffLength: Double,
    contactDistance: Double = OrigamiDuplex.INTERHELICAL
): Double = favourableStrokeClearance(standoffLength, contactDistance)

/** How far in nm the midspan would have to go **past** the base body's contact plane at [stroke]. */
fun midspanPenetration(
    stroke: Double,
    standoffLength: Double,
    contactDistance: Double = OrigamiDuplex.INTERHELICAL
): Double {
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    return max(0.0, stroke - midspanClearance(standoffLength, contactDistance))
}

/**
 * The length in nm of the aperture the standoff-carrying body needs under one flexure's midspan, so
 * that the beam can reach [stroke] — zero wherever the clearance already covers the stroke.
 *
 * The beam is a curve, not a point, so the hole is not the penetration: it is the part of the span
 * over which the deflection exceeds the clearance, `L(1 − 2u*)` with `u*` from
 * [apertureHalfPositionFraction].
 */
fun apertureLength(
    span: Double,
    restraint: Double,
    stroke: Double,
    standoffLength: Double,
    contactDistance: Double = OrigamiDuplex.INTERHELICAL,
    scanSteps: Int = 256
): Double {
    require(span > 0.0) { "span must be positive, was: $span" }
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    val clearance = midspanClearance(standoffLength, contactDistance)
    if (stroke <= clearance) return 0.0
    if (stroke <= 0.0) return 0.0
    val level = clearance / stroke
    val u = apertureHalfPositionFraction(restraint, max(0.0, level), scanSteps)
    return span * (1.0 - 2.0 * u)
}

/**
 * The plan area in nm² of the [count] apertures the **ties** need, whatever the stroke — one hole
 * of one duplex pitch square per load path.
 *
 * The favourable topology's tie has to cross the standoff base plane by construction, and
 * `C-0023`'s two-sidedness makes that tie a duplex rather than a strand, so it needs a full
 * duplex-omission hole in the base body. This is the floor of `T-78`'s cost: it is present at
 * **every** stroke, including the one where [apertureLength] is zero.
 */
fun tieApertureArea(count: Int, width: Double = OrigamiDuplex.INTERHELICAL): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(width > 0.0) { "width must be positive, was: $width" }
    return count * width * width
}

/** The plan area in nm² of [count] apertures of [length] and [width]. */
fun apertureArea(count: Int, length: Double, width: Double = OrigamiDuplex.INTERHELICAL): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(length >= 0.0) { "length must not be negative, was: $length" }
    require(width > 0.0) { "width must be positive, was: $width" }
    return count * length * width
}

// ---------------------------------------------------------------- the flexure array in the layer

/**
 * The volume in nm³ of a [count]-flexure array of span [span] on standoffs of [standoffLength] —
 * beams and standoffs alike treated as cylinders of radius [duplexRadius].
 *
 * It exists to price the one favourable mounting that puts the array **under** the tile: the number
 * that decides it is the fraction of the polymer layer's own volume the array would occupy, and
 * that is a division rather than a model.
 */
fun flexureArrayVolume(
    count: Int,
    span: Double,
    standoffLength: Double,
    duplexRadius: Double = 0.5 * OrigamiDuplex.DIAMETER
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(span > 0.0) { "span must be positive, was: $span" }
    require(standoffLength > 0.0) { "standoffLength must be positive, was: $standoffLength" }
    require(duplexRadius > 0.0) { "duplexRadius must be positive, was: $duplexRadius" }
    return count * PI * duplexRadius * duplexRadius * (span + 2.0 * standoffLength)
}

/** The fraction of a layer of [layerHeight] over [footprintArea] that [volume] would occupy. */
fun layerVolumeFraction(volume: Double, footprintArea: Double, layerHeight: Double): Double {
    require(volume >= 0.0) { "volume must not be negative, was: $volume" }
    require(footprintArea > 0.0) { "footprintArea must be positive, was: $footprintArea" }
    require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
    return volume / (footprintArea * layerHeight)
}

// ---------------------------------------------------------------- the pre-bow escape

/**
 * The force in pN with which one flexure **resists** the two bodies separating, at a built rise
 * [preBow] and an unsigned [stroke].
 *
 * The separation is the coupling's own coordinate and `δ = δ₀ + (dδ/ds)s`, so the resistance is
 * `R(δ)·dδ/ds`. Negative values mean the flexure is *assisting* the stroke, which is what a
 * pre-bowed adverse mounting does at zero stroke — and that is exactly the preload it costs.
 */
fun couplingResistance(
    flexure: CoupledJointFlexure,
    mounting: FlexureMounting,
    preBow: Double,
    stroke: Double
): Double {
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    return mounting.deflectionRate *
            flexure.reaction(preBow + mounting.deflectionRate * stroke)
}

/**
 * The force in pN one flexure **delivers to the load over** [stroke] — the incremental quantity
 * §3's placement condition is written on (`C-0017`: *the force delivered to a load over a stroke is
 * `k_c Δs`*), which at zero pre-bow is `C-0030`'s own [CoupledJointFlexure.strokeReaction].
 */
fun preBowDeliveredForce(
    flexure: CoupledJointFlexure,
    mounting: FlexureMounting,
    preBow: Double,
    stroke: Double
): Double = couplingResistance(flexure, mounting, preBow, stroke) -
        couplingResistance(flexure, mounting, preBow, 0.0)

/** `dF/ds` in pN/nm at [stroke] — the tangent `C-0023`'s compliance ceiling is read on. */
fun preBowTangentStiffness(
    flexure: CoupledJointFlexure,
    mounting: FlexureMounting,
    preBow: Double,
    stroke: Double
): Double {
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    return flexure.tangentStiffness(preBow + mounting.deflectionRate * stroke)
}

/**
 * The **preload** in pN one pre-bowed flexure puts on the tile before any bias is applied, positive
 * when it pushes the tile toward the electrode.
 *
 * This is the price of the pre-bow escape, and it is a *length* — `C-0023`'s lesson — quantised at
 * the 0.34 nm base-pair rise.
 */
fun preBowPreload(
    flexure: CoupledJointFlexure,
    mounting: FlexureMounting,
    preBow: Double
): Double = -couplingResistance(flexure, mounting, preBow, 0.0)

/** The largest assembled tangent in pN/nm anywhere in `[0, stroke]`, over [samples] probes. */
fun maximumAssembledTangent(
    flexure: CoupledJointFlexure,
    count: Int,
    mounting: FlexureMounting,
    preBow: Double,
    stroke: Double,
    samples: Int = 2000
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    require(samples >= 16) { "samples must be at least 16, was: $samples" }
    var peak = Double.NEGATIVE_INFINITY
    for (i in 0..samples) {
        val tangent = count * preBowTangentStiffness(flexure, mounting, preBow, stroke * i / samples)
        if (tangent > peak) peak = tangent
    }
    return peak
}

/**
 * The span in nm at which [count] flexures deliver a **secant** of [targetStiffness] over
 * [workingStroke] in [mounting] at a built rise of [preBow] — §3's placement condition, generalised
 * from `C-0030`'s [coupledFlexureSpan] to a pre-bowed element, and reproducing it exactly at
 * `preBow = 0`.
 *
 * Scans then bisects on the **bracket width**: the coupled law is not monotone in the deflection, so
 * the first sign change is taken rather than a monotone bisection assumed.
 */
fun preBowedFlexureSpan(
    bendingRigidity: Double,
    flexibility: StandoffTipFlexibility,
    count: Int,
    targetStiffness: Double,
    workingStroke: Double,
    mounting: FlexureMounting,
    preBow: Double,
    stretchModulus: Double = AnchorMaterials.DUPLEX_STRETCH_MODULUS,
    drawInModel: DrawInModel = DrawInModel.CHORD,
    scanSteps: Int = 256
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingStroke > 0.0) { "workingStroke must be positive, was: $workingStroke" }
    require(preBow >= 0.0) { "preBow must not be negative, was: $preBow" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    fun assembled(span: Double): Double = count * preBowDeliveredForce(
        CoupledJointFlexure(bendingRigidity, span, flexibility, stretchModulus, drawInModel),
        mounting, preBow, workingStroke
    ) / workingStroke
    var low = max(workingStroke * 1.0e-3, 1.0e-3)
    var high = low
    var grown = 0
    while (assembled(high) >= targetStiffness && grown < 400) {
        high *= 1.5
        grown++
    }
    require(assembled(high) < targetStiffness) {
        "no span reaches a stiffness as low as $targetStiffness"
    }
    val step = (high - low) / scanSteps
    var scan = low
    for (i in 1..scanSteps) {
        val next = scan + step
        if (assembled(next) < targetStiffness) {
            low = scan
            high = next
            break
        }
        scan = next
    }
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (assembled(middle) > targetStiffness) low = middle else high = middle
        if (high - low <= 1.0e-15 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The smallest built rise in nm — rounded **up** to the 0.34 nm base-pair quantum — at which a
 * pre-bowed [mounting] keeps its assembled tangent under [ceiling] over the whole of [stroke], with
 * the span re-placed at every trial rise.
 *
 * Returns `+∞` if no rise up to [maximumPreBow] does, which is the escape being **closed** rather
 * than merely expensive.
 */
fun minimumPreBowForCeiling(
    bendingRigidity: Double,
    flexibility: StandoffTipFlexibility,
    count: Int,
    targetStiffness: Double,
    stroke: Double,
    mounting: FlexureMounting,
    ceiling: Double,
    stretchModulus: Double = AnchorMaterials.DUPLEX_STRETCH_MODULUS,
    maximumPreBow: Double = 30.0,
    quantum: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Double {
    require(ceiling > 0.0) { "ceiling must be positive, was: $ceiling" }
    require(quantum > 0.0) { "quantum must be positive, was: $quantum" }
    require(maximumPreBow > 0.0) { "maximumPreBow must be positive, was: $maximumPreBow" }
    val steps = (maximumPreBow / quantum).toInt()
    for (i in 0..steps) {
        val preBow = i * quantum
        val span = preBowedFlexureSpan(
            bendingRigidity, flexibility, count, targetStiffness, stroke,
            mounting, preBow, stretchModulus
        )
        val flexure = CoupledJointFlexure(bendingRigidity, span, flexibility, stretchModulus)
        if (maximumAssembledTangent(flexure, count, mounting, preBow, stroke) <= ceiling) {
            return preBow
        }
    }
    return Double.POSITIVE_INFINITY
}

/**
 * The shortest standoff in nm at which [count] flexures in [mounting], each placed at
 * [targetStiffness] over [stroke], present an assembled **tangent** no larger than [ceiling] —
 * scanned on [scanSteps] over `(0, maximumLength]` and returning `+∞` if none does.
 *
 * `C-0030` swept `ℓ = 3–10 nm` and found the adverse mounting past `C-0023`'s ceiling at every one.
 * That is a statement about the swept interval, not about the mounting: the adverse tangent falls
 * monotonically with the standoff length, so the honest form of *"no window at all"* is **a length
 * the window would need**, quoted against `C-0017`'s envelope.
 */
fun standoffLengthForCompliance(
    bendingRigidity: Double,
    baseRotationalStiffness: Double,
    count: Int,
    targetStiffness: Double,
    stroke: Double,
    mounting: FlexureMounting,
    ceiling: Double,
    stretchModulus: Double = AnchorMaterials.DUPLEX_STRETCH_MODULUS,
    maximumLength: Double = 60.0,
    scanSteps: Int = 5700
): Double {
    require(ceiling > 0.0) { "ceiling must be positive, was: $ceiling" }
    require(maximumLength > 0.0) { "maximumLength must be positive, was: $maximumLength" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    val step = maximumLength / scanSteps
    for (i in 1..scanSteps) {
        val length = i * step
        val flexibility = standoffTipFlexibility(bendingRigidity, length, baseRotationalStiffness)
        val span = preBowedFlexureSpan(
            bendingRigidity, flexibility, count, targetStiffness, stroke, mounting, 0.0,
            stretchModulus
        )
        val flexure = CoupledJointFlexure(bendingRigidity, span, flexibility, stretchModulus)
        if (count * flexure.strokeTangentStiffness(stroke, mounting.orientation) <= ceiling) {
            return length
        }
    }
    return Double.POSITIVE_INFINITY
}

/** `|value|`, exposed so a study can report a signed quantity beside its magnitude. */
internal fun magnitude(value: Double): Double = abs(value)
