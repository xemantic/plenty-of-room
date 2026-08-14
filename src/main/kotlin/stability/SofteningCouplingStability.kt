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

package com.xemantic.nano.plentyofroom.stability

import com.xemantic.nano.plentyofroom.anchoring.CoupledJointFlexure
import com.xemantic.nano.plentyofroom.anchoring.DrawInModel
import com.xemantic.nano.plentyofroom.anchoring.FlexureOrientation
import com.xemantic.nano.plentyofroom.anchoring.StandoffBase
import com.xemantic.nano.plentyofroom.anchoring.coupledFlexureSpan
import com.xemantic.nano.plentyofroom.anchoring.standoffTipFlexibility
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlinx.serialization.Serializable

/**
 * Task `T-76` — `C-0017`'s stability condition read on a coupling that **strain-softens**.
 *
 * ## The one idea in this file
 *
 * `C-0017` records a theorem and the programme has banked it four times:
 *
 * > *"Placement is written on the coupling's SECANT and stability on its TANGENT, so a
 * > strain-stiffening element discharges both with one part and the whole `tangent/secant` ratio
 * > is free stability margin at zero placement cost."*
 *
 * **The theorem has a sign, and `C-0030` flipped it.** Once the standoff's tip is one 2 × 2 rather
 * than two independent springs the assembled flexure is strain-**softening**: `t/s` = 0.757 at the
 * placement point, and the tangent is not even monotone — it has an interior minimum inside §3's
 * own operating range. So two things must be read differently:
 *
 * 1. the stability condition is `min_s k_tangent(s) > |k_eff|` over the strokes the device
 *    **traverses**, not `k_tangent(s*) > |k_eff|` at the one it is held at ([tangentMinimum]);
 * 2. the pull-in fold must be **re-located**, because a nonlinear load line differs from the affine
 *    mandate in *two* ways at once — its slope everywhere, and its **level** at every stroke other
 *    than the placement point. `C-0030`'s element delivers 298 pN at the 10 nm stroke where the
 *    mandate delivers 460, so the bias that reaches a given depth is lower and the whole
 *    equilibrium path moves.
 *
 * Only the second sees both, and `actuator/PullInStability.kt`'s `EquilibriumPath` already takes
 * its load as an arbitrary function of the stroke — so `C-0018`'s solver is re-used **unchanged**
 * and the comparison is state by state.
 *
 * ## Conventions, restated because this file is where two claims' sign conventions meet
 *
 * - The **stroke** `s = L₀ − h` is positive **downward**, and every load line here is a function
 *   of the *unsigned* stroke: the sense in which the flexure is driven is
 *   [FlexureOrientation], `C-0030`'s mounting choice, and it is carried by the element and not by
 *   the sign of `s`.
 * - A **load line** `R(s)` is positive **upward**, i.e. resisting descent, in pN over the whole
 *   45-path array. Its **secant** `R(s)/s` is what §3's placement clause is written on and its
 *   **tangent** `dR/ds` is what the stability clause is written on. They are the same number only
 *   for a line through the origin.
 * - Stability is `k_c + k_eff > 0` with `k_eff = k_brush + k_es`; `k_es < 0` above the force
 *   maximum (`CH-0011`). Where `k_eff ≥ 0` there is **no requirement at all** and
 *   [stabilityMargin] returns `null` rather than an infinity — `CLAUDE.md`'s rule, because a
 *   margin of `Infinity` is the absence of a requirement and not a large one.
 */

// ---------------------------------------------------------------- the load line

/**
 * A coupling's reaction law read on the **unsigned stroke** — the object `C-0018` had as two
 * numbers (`preload`, `stiffness`) and which `C-0030` makes a function.
 */
interface StrokeLoadLine {

    /** How this line is named in the result file. */
    val name: String

    /** `R(s)` in pN over the array, positive **upward**. */
    fun reaction(stroke: Double): Double

    /** `dR/ds` in pN/nm — what the **stability** condition is written on. */
    fun tangent(stroke: Double): Double

    /** `R(s)/s` in pN/nm — what the **placement** condition is written on. */
    fun secant(stroke: Double): Double {
        require(stroke > 0.0) { "a secant stiffness is undefined at zero stroke, was: $stroke" }
        return reaction(stroke) / stroke
    }

    /**
     * `t/s` at [stroke] — above one for a strain-stiffening element (`C-0017`'s premise) and
     * below one for a strain-softening one (`CH-0042`).
     */
    fun tangentToSecant(stroke: Double): Double = tangent(stroke) / secant(stroke)
}

/**
 * An **affine** load line `R = R₀ + k s` — `C-0018`'s three, unchanged.
 *
 * Its secant equals its tangent only when the preload is zero, which is why `C-0017`'s placement
 * arithmetic is preload-free and its stability arithmetic is not.
 */
data class AffineLoadLine(
    override val name: String,
    val stiffness: Double,
    val preload: Double = 0.0
) : StrokeLoadLine {

    init {
        require(stiffness >= 0.0) { "stiffness must not be negative, was: $stiffness" }
        require(preload >= 0.0) { "preload must not be negative, was: $preload" }
    }

    override fun reaction(stroke: Double): Double {
        require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
        return preload + stiffness * stroke
    }

    override fun tangent(stroke: Double): Double {
        require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
        return stiffness
    }
}

/**
 * `count` of `C-0030`'s coupled flexures in parallel, driven in one [FlexureOrientation] — the
 * nonlinear load line this task exists to read `C-0018` against.
 *
 * The element's own law is `C-0030`'s and is not re-derived here; what this class adds is the
 * assembly (parallel paths add stiffness) and the interface the equilibrium-path solver consumes.
 */
class AssembledFlexureLine(
    override val name: String,
    val flexure: CoupledJointFlexure,
    val count: Int,
    val orientation: FlexureOrientation
) : StrokeLoadLine {

    init {
        require(count > 0) { "count must be positive, was: $count" }
    }

    /** The flexure's span in nm — the design variable the placement condition was solved for. */
    val span: Double get() = flexure.span

    override fun reaction(stroke: Double): Double =
        count * flexure.strokeReaction(stroke, orientation)

    override fun tangent(stroke: Double): Double {
        require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
        return count * flexure.strokeTangentStiffness(stroke, orientation)
    }

    /** The same array with a different path count — a gate-1 lever, and nothing else. */
    fun withCount(count: Int): AssembledFlexureLine =
        AssembledFlexureLine(name, flexure, count, orientation)

    /** The **signed** axial force in one beam at [stroke], carried for `C-0030`'s `P4`. */
    fun axialForce(stroke: Double): Double = flexure.strokeAxialForce(stroke, orientation)
}

// ---------------------------------------------------------------- the tangent minimum

/**
 * The extremum of a tangent over a stroke range, with the one qualifier that decides whether it
 * is a stationary point at all.
 *
 * `CLAUDE.md`: *"a boundary maximum is not a stationary point"*. The same applies here in reverse —
 * a strain-*stiffening* element's tangent minimum is at zero stroke, where the membrane term has
 * not yet switched on, and it is a property of the interval rather than of the element. Only an
 * **interior** minimum is `CH-0042`'s object.
 */
@Serializable
data class TangentExtremum(

    /** The tangent stiffness in pN/nm at the located point. */
    val stiffness: Double,

    /** The stroke in nm it sits at. */
    val stroke: Double,

    /** True when the minimum is strictly inside the range — `CH-0042`'s case. */
    val interior: Boolean
)

/**
 * `min_s k_tangent(s)` over `[low, high]` — a coarse scan for the bracket, then golden section
 * inside it, exiting on the **bracket width** and never on a residual.
 *
 * The tangent is analytic (`CoupledJointFlexure.tangentStiffness`, graded against a central
 * difference in `C-0030`), so unlike `CLAUDE.md`'s golden-section warning there is no search noise
 * underneath this one and the located minimum is resolvable to the bracket. The convergence record
 * measures that rather than assuming it.
 */
fun StrokeLoadLine.tangentMinimum(
    low: Double,
    high: Double,
    coarseSteps: Int = 1024,
    tolerance: Double = 1e-9
): TangentExtremum {
    require(low >= 0.0) { "low must not be negative, was: $low" }
    require(high > low) { "the range must be ascending, was: [$low, $high]" }
    require(coarseSteps >= 4) { "coarseSteps must be at least 4, was: $coarseSteps" }
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    val step = (high - low) / coarseSteps
    var best = 0
    var bestValue = tangent(low)
    for (i in 1..coarseSteps) {
        val value = tangent(low + i * step)
        if (value < bestValue) {
            bestValue = value
            best = i
        }
    }
    if (best == 0 || best == coarseSteps) {
        return TangentExtremum(bestValue, low + best * step, interior = false)
    }
    var left = low + (best - 1) * step
    var right = low + (best + 1) * step
    val golden = 0.6180339887498949
    var a = right - golden * (right - left)
    var b = left + golden * (right - left)
    var fa = tangent(a)
    var fb = tangent(b)
    while (right - left > tolerance) {
        if (fa <= fb) {
            right = b
            b = a
            fb = fa
            a = right - golden * (right - left)
            fa = tangent(a)
        } else {
            left = a
            a = b
            fa = fb
            b = left + golden * (right - left)
            fb = tangent(b)
        }
    }
    val stroke = 0.5 * (left + right)
    return TangentExtremum(tangent(stroke), stroke, interior = true)
}

// ---------------------------------------------------------------- the stability reading

/**
 * `k_c/|k_eff|` — `C-0017`'s stability margin, or `null` where there is **no requirement**.
 *
 * `k_eff ≥ 0` means the actuator is already stable without any coupling at all, so the floor is
 * zero and the margin is not a number. `CLAUDE.md`: *"a margin of `Infinity` is not a margin, it
 * is the absence of a requirement"* — and `kotlinx.serialization` refuses to encode one besides.
 */
fun stabilityMargin(couplingTangent: Double, effectiveStiffness: Double): Double? {
    require(couplingTangent >= 0.0) {
        "couplingTangent must not be negative, was: $couplingTangent"
    }
    if (effectiveStiffness >= 0.0) return null
    return couplingTangent / -effectiveStiffness
}

// ---------------------------------------------------------------- the Gen-1 designs

/** §3's force target in pN. */
const val GEN1_TARGET_FORCE: Double = 100.0

/** §3's **acceptable** stroke in nm — the placement point. */
const val GEN1_ACCEPTABLE_STROKE: Double = 3.0

/** §3's **desired** stroke in nm — the far end of the operating range. */
const val GEN1_DESIRED_STROKE: Double = 10.0

/** `C-0017`'s mandate, `100 pN / 3 nm`, by arithmetic and with no physics in it. */
const val GEN1_MANDATE_STIFFNESS: Double = GEN1_TARGET_FORCE / GEN1_ACCEPTABLE_STROKE

/** `C-0015`'s attachment grid, 3 × 15. */
const val GEN1_PATH_COUNT: Int = 45

/** `C-0030`'s recommended standoff length in nm. */
const val GEN1_STANDOFF_LENGTH: Double = 8.0

/** `C-0023`'s compliance ceiling on the assembled tangent, in pN/nm. */
const val GEN1_COMPLIANCE_CEILING: Double = 40.0

/**
 * `C-0030`'s recommended element, assembled and **placed**: the span is solved so that the
 * assembled secant at [placementStroke] is exactly [targetStiffness].
 *
 * [coupled] `= false` is `C-0025`/`C-0028`'s reading — the same standoff with its tip flexibility's
 * off-diagonal removed — which is the strain-**stiffening** element `C-0017`'s theorem was banked
 * on, and which this task carries beside the coupled one so that the comparison is between two
 * readings of one design rather than between two designs.
 */
@Suppress("LongParameterList")
fun gen1CouplingLine(
    name: String,
    coupled: Boolean,
    orientation: FlexureOrientation,
    count: Int = GEN1_PATH_COUNT,
    standoffLength: Double = GEN1_STANDOFF_LENGTH,
    base: StandoffBase = StandoffBase.crossovers(2, favourableOrientation = true),
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS,
    drawInModel: DrawInModel = DrawInModel.CHORD,
    targetStiffness: Double = GEN1_MANDATE_STIFFNESS,
    placementStroke: Double = GEN1_ACCEPTABLE_STROKE
): AssembledFlexureLine {
    val full = standoffTipFlexibility(bendingRigidity, standoffLength, base.rotationalStiffness)
    val flexibility = if (coupled) full else full.decoupled()
    val span = coupledFlexureSpan(
        bendingRigidity, flexibility, count, targetStiffness, placementStroke, orientation,
        stretchModulus, drawInModel
    )
    return AssembledFlexureLine(
        name = name,
        flexure = CoupledJointFlexure(
            bendingRigidity, span, flexibility, stretchModulus, drawInModel
        ),
        count = count,
        orientation = orientation
    )
}
