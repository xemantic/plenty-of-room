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

package com.xemantic.nano.plentyofroom.actuator

import com.xemantic.nano.plentyofroom.brush.GraftedChain
import com.xemantic.nano.plentyofroom.brush.GraftedLayerModel
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.brush.stiffness
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * The coupled operating point of the Gen-1 actuator — task `T-3`, leaf `A2.2`.
 *
 * ## The two quantities, which are not the same quantity
 *
 * - The **blocking force** is the force the actuator delivers at **zero displacement**. At `h = L₀`
 *   the layer carries nothing, so the whole of `|F_es(L₀, V)|` is available to an external load.
 * - The **stroke** is the displacement the actuator reaches against **no external load**, where
 *   `|F_es(h, V)|` is balanced by the layer's own restoring force `P(h)·A`.
 *
 * They are routinely conflated, and a stroke is routinely obtained by dividing a force by a
 * stiffness. That is wrong here by more than a factor of two: `C-0003`'s `P(h)` is strongly
 * nonlinear — the strong-stretching profile opens with *zero* stiffness at `L₀` and stiffens
 * steeply under compression — so `F/k(L₀)` is either infinite or a large overestimate. The
 * stroke has to be a **root** of a force balance, and this class solves it.
 *
 * ## The actuator characteristic, in one function
 *
 * Let `W` be an external load holding the tile up. The balance is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`|F_es(h, V)| = P(h)·A + W`
 *
 * so the actuator's **output force** at height `h` is `W(h) = |F_es(h, V)| − P(h)·A`, which is
 * [outputForce]. Its value at `h = L₀` is the blocking force and its root is the free stroke,
 * so the whole force-displacement characteristic is one function and the two headline numbers
 * are its two ends. The maximum of `W·(L₀ − h)` along it is the work the actuator can deliver
 * into a matched constant-force load, which is what leaf `A2.2` means by "work-stroke".
 *
 * ## Why the root find is unconditionally convergent, and why the root it finds is the stable one
 *
 * `d(outputForce)/dh = d|F_es|/dh − dP·A/dh = −|k_es| + k_brush = k_eff`, exactly. So the sign of
 * `k_eff` is the sign of the slope of the characteristic, and the characteristic is **not**
 * monotone in general — that non-monotonicity *is* `T-4`'s pull-in. Bisection on the whole
 * interval would therefore not be safe.
 *
 * What is safe, and what is done here:
 *
 * 1. `outputForce(L₀) = |F_es(L₀, V)| > 0`, because the layer carries nothing at `L₀`.
 * 2. `outputForce` at the dry-thickness end is large and **negative**, because `Π_int(φ→1)` is
 *    tens of thousands of pN over the footprint against a few hundred pN of `F_es`.
 * 3. Scanning down from `L₀`, the **first** sign change is bracketed, and bisection inside that
 *    bracket is unconditionally convergent — the same argument `C-0001` gave for its bisection,
 *    made on a bracket rather than assumed over the whole interval.
 * 4. At that first root the characteristic goes from positive above to negative below, so its
 *    slope there is positive, so **`k_eff > 0`**: the first root below `L₀` is *always* the
 *    stable one, and that is a theorem about the construction rather than a check on the answer.
 *    `ActuatorForceBalanceTest` asserts it over a family of fields.
 *
 * Any *further* roots below it are counted and reported as [ActuatorState.furtherEquilibria]: a
 * non-zero count means an unstable root and a stable one exist below the operating point, i.e. a
 * fold is nearby and the bias is approaching pull-in. That is the number `T-4` wants.
 */
class ActuatorForceBalance(

    /** The layer model — one of `C-0003`'s six (profile × interaction) pairs. */
    val model: GraftedLayerModel,

    /** The chain whose equilibrium height under [model] is the design point's layer height. */
    val chain: GraftedChain,

    /** The stack geometry; supplies the footprint and the gap-height identity. */
    val geometry: ActuatorGeometry,

    /**
     * How many points the downward scan for the first sign change uses.
     *
     * Only the **bracket** depends on it — the root itself is then bisected to the
     * double-precision floor — so this sets how finely two nearby equilibria can be told
     * apart, not the accuracy of the answer. `ActuatorForceBalanceTest` shows 500 and 16 000
     * give the same operating height to 1e−9, and the study emits the same check over the
     * whole pipeline. It is kept modest because a strong-stretching evaluation of `P(h)` costs
     * a Newton-solved profile quadrature.
     */
    val scanSteps: Int = 600

) {

    init {
        require(scanSteps >= 64) { "scanSteps must be at least 64, was: $scanSteps" }
    }

    /** `L₀` in nm — the unperturbed layer height, and the height at which the stroke is zero. */
    val restingHeight: Double = model.equilibriumHeight(chain)

    /** `N σ v₀` in nm — the dry thickness, below which the volume fraction would exceed 1. */
    val dryThickness: Double get() = chain.occupiedThickness

    /** The layer's restoring force in pN over the footprint, at layer height [height]. */
    fun layerLoad(height: Double): Double =
        model.load(chain, height, geometry.footprintArea)

    /** `k_brush` in `pN/nm` over the footprint, at layer height [height]. */
    fun layerStiffness(height: Double): Double =
        model.stiffness(chain, height, geometry.footprintArea)

    /**
     * The actuator's output force in pN at layer height [height] — what is left over for an
     * external load once the layer has been pushed back.
     *
     * `W(h) = |F_es(h, V)| − P(h)·A`. Positive means the actuator can still push.
     */
    fun outputForce(field: ElectrostaticForceCurve, height: Double): Double =
        field.magnitudeAt(geometry.electrostaticGap(height)) - layerLoad(height)

    /** The blocking force in pN — `|F_es(L₀, V)|`, the force at **zero** displacement. */
    fun blockingForce(field: ElectrostaticForceCurve): Double =
        field.magnitudeAt(geometry.electrostaticGap(restingHeight))

    /** Solves the coupled balance against [field] and returns everything read off it. */
    fun solve(field: ElectrostaticForceCurve): ActuatorState {
        require(field.maximumGap >= restingHeight) {
            "the force curve must reach the resting height $restingHeight, " +
                    "but stops at ${field.maximumGap}"
        }
        val floor = maxOf(dryThickness * (1.0 + DRY_MARGIN), field.minimumGap)
        require(floor < restingHeight) {
            "the force curve must reach below the resting height $restingHeight, " +
                    "but starts at ${field.minimumGap} against a dry thickness of $dryThickness"
        }
        val blocking = blockingForce(field)
        val step = (restingHeight - floor) / scanSteps
        var upper = restingHeight
        var atUpper = outputForce(field, upper)
        var bracketLow = Double.NaN
        var bracketHigh = Double.NaN
        var crossings = 0
        for (i in 1..scanSteps) {
            val lower = if (i == scanSteps) floor else restingHeight - i * step
            val atLower = outputForce(field, lower)
            if (atUpper > 0.0 && atLower <= 0.0 || atUpper < 0.0 && atLower >= 0.0) {
                crossings++
                if (crossings == 1) {
                    bracketLow = lower
                    bracketHigh = upper
                }
            }
            upper = lower
            atUpper = atLower
        }
        if (crossings == 0) {
            return collapsed(field, floor, blocking)
        }
        val operating = bisect(bracketLow, bracketHigh) { outputForce(field, it) }
        return report(field, operating, blocking, crossings - 1, floor, converged = true)
    }

    /**
     * The state reported when the characteristic never crosses zero — the tile is driven onto
     * the dry-thickness floor and the model has left its own validity range.
     *
     * `C-0003` enforces `N σ v₀ < h ≤ L₀` in code, so this is a real boundary and not a
     * numerical accident. It has not been observed anywhere in the §3 box.
     */
    private fun collapsed(
        field: ElectrostaticForceCurve,
        floor: Double,
        blocking: Double
    ): ActuatorState = report(field, floor, blocking, 0, floor, converged = false)

    private fun report(
        field: ElectrostaticForceCurve,
        operating: Double,
        blocking: Double,
        further: Int,
        floor: Double,
        converged: Boolean
    ): ActuatorState {
        val gap = geometry.electrostaticGap(operating)
        val brush = layerStiffness(operating)
        val electrostatic = field.stiffnessAt(gap)
        var bestWork = 0.0
        var bestStroke = 0.0
        var peakForce = blocking
        var peakForceStroke = 0.0
        val steps = scanSteps
        for (i in 0..steps) {
            val height = restingHeight - i * (restingHeight - floor) / steps
            if (height < operating) break
            val output = outputForce(field, height)
            val work = output * (restingHeight - height)
            if (work > bestWork) {
                bestWork = work
                bestStroke = restingHeight - height
            }
            if (output > peakForce) {
                peakForce = output
                peakForceStroke = restingHeight - height
            }
        }
        return ActuatorState(
            restingHeight = restingHeight,
            operatingHeight = operating,
            stroke = restingHeight - operating,
            blockingForce = blocking,
            electrostaticForce = field.forceAt(gap),
            layerRestoringForce = layerLoad(operating),
            brushStiffness = brush,
            electrostaticStiffness = electrostatic,
            effectiveStiffness = brush + electrostatic,
            forceDecayLength = field.decayLengthAt(gap),
            volumeFraction = chain.meanVolumeFraction(operating),
            compressionRatio = operating / restingHeight,
            peakOutputForce = peakForce,
            peakOutputForceStroke = peakForceStroke,
            maximumOutputWork = bestWork,
            workStroke = bestStroke,
            furtherEquilibria = further,
            converged = converged
        )
    }

    /**
     * Bisection inside a bracket that is known to contain a sign change.
     *
     * Unconditionally convergent because the bracket is retained at every step — the same
     * guarantee `C-0001` argued for its own bisection — and chosen over a secant method because
     * the characteristic's slope is `k_eff`, which is small near a fold and would make a
     * derivative-based step leave the bracket exactly where the answer matters most.
     */
    private inline fun bisect(low: Double, high: Double, f: (Double) -> Double): Double {
        var left = low
        var right = high
        val atLeft = f(left)
        repeat(BISECTION_STEPS) {
            val middle = 0.5 * (left + right)
            if (f(middle) * atLeft > 0.0) left = middle else right = middle
        }
        return 0.5 * (left + right)
    }

}

/** One solved operating point of the actuator. */
@Serializable
data class ActuatorState(

    /** `L₀` in nm — the layer height at zero bias, where the stroke is zero. */
    val restingHeight: Double,

    /** `h*` in nm — the layer height where the two forces balance. */
    val operatingHeight: Double,

    /** `L₀ − h*` in nm — the **free** stroke, against no external load. */
    val stroke: Double,

    /** `|F_es(L₀, V)|` in pN — the **blocking** force, at zero displacement. */
    val blockingForce: Double,

    /** The signed `F_es` in pN at the operating point; negative means toward the electrode. */
    val electrostaticForce: Double,

    /** `P(h*)·A` in pN — the layer's restoring force there, equal to `|F_es|` by construction. */
    val layerRestoringForce: Double,

    /** `k_brush(h*)` in `pN/nm` over the footprint. */
    val brushStiffness: Double,

    /** `k_es(h*, V)` in `pN/nm` — negative, per §1. */
    val electrostaticStiffness: Double,

    /** `k_eff = k_brush + k_es` in `pN/nm` — positive at any stable operating point. */
    val effectiveStiffness: Double,

    /** `ℓ = F_es/k_es` in nm at the operating point — the force's own decay length. */
    val forceDecayLength: Double,

    /** `φ = N σ v₀/h*` — the layer's mean volume fraction at the operating point. */
    val volumeFraction: Double,

    /** The compression ratio `h* over L₀` — the variable `C-0003` quotes its stiffnesses at. */
    val compressionRatio: Double,

    /**
     * `max W(s)` in pN over the characteristic — the largest force the actuator can deliver,
     * which is **not** the blocking force.
     *
     * The blocking force is `W(0)`, and `dW/dh = k_eff`, so wherever `k_eff` is small the
     * characteristic is nearly flat and wherever `|k_es|` exceeds `k_brush` it *rises* with
     * stroke. The output force is therefore maximal at a finite displacement, not at zero, and
     * quoting the blocking force as "the force the actuator can deliver" understates it. That
     * is the electrostatic-softening signature in the force-displacement plane, and it is the
     * same physics `T-4` reads as pull-in.
     */
    val peakOutputForce: Double,

    /** The stroke in nm at which [peakOutputForce] is delivered; zero when `k_eff > 0` throughout. */
    val peakOutputForceStroke: Double,

    /** `max W·(L₀−h)` in `pN·nm` — the work into a matched constant-force load. */
    val maximumOutputWork: Double,

    /** The stroke in nm at which [maximumOutputWork] is delivered. */
    val workStroke: Double,

    /**
     * How many further equilibria lie **below** the operating point.
     *
     * Zero is the healthy case. A non-zero count means the characteristic re-crosses, so an
     * unstable root and a deeper stable one exist and the bias is approaching a fold — `T-4`'s
     * pull-in, detected here rather than assumed absent.
     */
    val furtherEquilibria: Int,

    /** False when no equilibrium exists above the dry thickness — the tile has collapsed. */
    val converged: Boolean
) {

    /** `k_eff/k_brush` — how much of the layer's stiffness the field has eaten. */
    val stiffnessRatio: Double get() = if (brushStiffness > 0.0) effectiveStiffness / brushStiffness else 0.0

    /** The residual of the balance in pN, reported so the solve grades itself. */
    val balanceResidual: Double get() = abs(layerRestoringForce - abs(electrostaticForce))

}

/** How far above the dry thickness the downward scan is allowed to reach. */
private const val DRY_MARGIN: Double = 1e-9

/** Bisection steps — 80 takes any bracket in this problem below the double-precision floor. */
private const val BISECTION_STEPS: Int = 80
