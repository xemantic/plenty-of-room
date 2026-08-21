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
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The largest `|H| L²/EI` [TwoSpringElastica] will accept — the dimensionless group that fixes how
 * far a shooting solve of an **axially** loaded beam amplifies its own initial condition,
 * `exp(√(|H|/EI) L)`. At 40 that is `e^6.3 ≈ 550`, comfortably inside double precision; the
 * primary reading of `T-79` is `H = 0` and needs none of it.
 */
const val AXIAL_CONDITIONING_LIMIT: Double = 40.0

/**
 * `max_s|φ|` in radians past which a solved shape is **not** on the branch this class enumerates.
 *
 * Past a right angle the tip force's moment arm reverses, the far-end moment residual stops being
 * monotone in the shooting parameter, and the elastica acquires branches that are not continuously
 * connected to the unloaded state. `C-0092` measured those branches: there are up to 39 of them at
 * 5000 pN on the Gen-1 arm, and **every one reaches a smaller stroke** than the primary root at the
 * same force, because a curled shape's `∫sin φ` cancels against itself. So a solve that lands past
 * this limit is not a deeper answer — it is a different question, and [TwoSpringElastica] refuses
 * rather than returning it (`T-159`).
 */
const val SMALL_ROTATION_BRANCH_LIMIT: Double = 0.5 * PI

/** Cells the branch's shooting scan may take at one tip force before it gives up. */
private const val BRANCH_SCAN_CELLS: Int = 400

/** The relative width below which the shooting scan stops halving and refuses. */
private const val BRANCH_SCAN_FLOOR: Double = 1.0e-14

/** Steps the tip-force continuation may take before it gives up. */
private const val CONTINUATION_STEPS: Int = 400

/**
 * How [TwoSpringElastica.forceForDisplacement] finds the branch it reports a force on.
 *
 * There are two, and the second one is a **defect kept on purpose**.
 */
enum class BranchStrategy {

    /**
     * `T-159`'s branch continuation — the default, and the only one a design number may use.
     */
    CONTINUATION,

    /**
     * `C-0039`'s original **blind doubling** ladder, which brackets a tip force by doubling from
     * `0.5 δ k_small` and calls [TwoSpringElastica.stateAtForce] inside whatever that produces.
     *
     * It is exact while the far-end residual has one root and it **loses the branch** once it has
     * two — `C-0092`/`CH-0107` measured that happening at about 100 pN on the Gen-1 arm, 0.2414 nm
     * of stroke early. It is retained, opt-in and named, for exactly one reason: `C-0092` is a
     * standing claim whose evidence is a **measurement of this artefact**, and a repair that makes
     * its own predecessor unmeasurable replaces one unfalsifiable number with another. Never
     * select it for anything but a reproduction.
     */
    DOUBLING_LADDER
}

/**
 * Task `T-79` — a **large-rotation two-spring elastica** for `E5`'s arm.
 *
 * ## What this exists to settle
 *
 * `E5a16`/`E5g16` is placed by two compositions that are each exact in one respect and wrong in
 * the other, and they bracket the arm at **11.03–12.50 nm**:
 *
 * - `C-0029`'s **series** form `1/k = r²/(n k_θ) + r³/(c EI)` has the exact rotation (`δ = r sin θ`,
 *   `CH-0040`) but is the **`ρ_f = 0` corner** of the boundary-value problem it is meant to
 *   approximate (`CH-0044`), and it charges the hinge the whole tip moment;
 * - `C-0034`'s **two-spring BVP** `c(ρ_n, ρ_f)` has the exact end condition but is a
 *   **small-deflection** result, evaluated where the arm turns 21°.
 *
 * This class is the composition that is exact in both: a planar **inextensible elastica** with a
 * rotational spring at each end.
 *
 * ## Geometry and signs, fixed before deriving (`gpd/tasks/T-79-two-spring-elastica.md`)
 *
 * Arc length `s ∈ [0, L]` from the **near** (hinge) end; `φ(s)` the tangent angle measured
 * counter-clockwise from the undeformed axis `+x` toward the stroke direction `+z`;
 * `x(s) = ∫cos φ`, `z(s) = ∫sin φ`. The near end sits on `k_n` grounded on the tile, the far end
 * on `k_f` grounded on the other body, and **neither body rotates**. The far body applies a
 * transverse force `F` along `+z`, an axial force `H` along `+x` (**positive in tension**) and an
 * optional external tip moment `M₀`. Then
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`EI φ′(s) = M₀ − k_f φ(L) + F(x(L) − x(s)) − H(z(L) − z(s))`
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;→ &nbsp; **`EI φ″ = −F cos φ + H sin φ`**, &nbsp;
 * **`EI φ′(0) = k_n φ(0)`**, &nbsp; **`EI φ′(L) = M₀ − k_f φ(L)`**.
 *
 * ## The free verification asset
 *
 * At vanishing load this **is** the boundary-value problem `C-0034` condenses, so
 * [twoSpringArmFactor] is a limiting case that pins the field equation, both boundary conditions
 * and every sign at once — exactly 12 at a guided pair, exactly 3 at either cantilever ordering,
 * and 0 at the free-free **mechanism**, which is why a beam on two free ends cannot be built here
 * at all.
 *
 * ## And what the exact solve adds is GEOMETRIC
 *
 * The stroke `δ = z(L)` and the **draw-in** `e = L − x(L)` are not independent: the arm is
 * inextensible, so its ends can never be further apart than `L`, and therefore
 * [chordDrawInBound] `e ≥ L − √(L² − δ²)` — a statement of pure geometry that needs no elastica
 * and that no joint design can relax.
 */
class TwoSpringElastica(
    val bendingRigidity: Double,
    val length: Double,
    val nearStiffness: Double,
    val farStiffness: Double,
    val steps: Int = 400,
    val strategy: BranchStrategy = BranchStrategy.CONTINUATION
) : SignedCouplingElement {

    init {
        require(bendingRigidity > 0.0 && bendingRigidity.isFinite()) {
            "bendingRigidity must be positive and finite, was: $bendingRigidity"
        }
        require(length > 0.0 && length.isFinite()) {
            "length must be positive and finite, was: $length"
        }
        require(nearStiffness >= 0.0) {
            "nearStiffness must not be negative, was: $nearStiffness"
        }
        require(farStiffness >= 0.0) { "farStiffness must not be negative, was: $farStiffness" }
        require(nearStiffness > 0.0 || farStiffness > 0.0) {
            "a beam free to rotate at BOTH ends is a mechanism, not a soft beam: it carries no " +
                    "transverse load at any deflection (c(0,0) = 0)"
        }
        require(steps >= 8) { "steps must be at least 8, was: $steps" }
    }

    /** `ρ_n = k_near L/EI`, dimensionless — [Double.POSITIVE_INFINITY] for a rigid hinge. */
    val nearRestraint: Double = nearStiffness * length / bendingRigidity

    /** `ρ_f = k_far L/EI`, dimensionless — [Double.POSITIVE_INFINITY] for a perfect guide. */
    val farRestraint: Double = farStiffness * length / bendingRigidity

    /** `C-0034`'s closed-form end-condition factor, which is this solver's vanishing-load limit. */
    val smallRotationArmFactor: Double = twoSpringArmFactor(nearRestraint, farRestraint)

    /** `c(ρ_n, ρ_f) EI/L³` in `pN/nm` — one arm, at vanishing load. */
    val smallRotationStiffness: Double
        get() = smallRotationArmFactor * bendingRigidity / (length * length * length)

    private val nearRigid: Boolean = nearStiffness.isInfinite()

    private val farRigid: Boolean = farStiffness.isInfinite()

    /**
     * RK4 sweeps taken since the last [resetSweepCount] — **diagnostic only**, not thread safe,
     * and read by no physics.
     *
     * It exists because `C-0031` is the precedent for repairing a root finder: *"a defect that is
     * invisible in the answer is invisible to every check written on the answer"*, and the thing
     * this class's solve strategy is chosen for is the **cost** of finding the root, not only the
     * root. A strategy change that silently degenerated into bisection would move no number.
     */
    var sweepCount: Long = 0L
        private set

    /** Zeroes [sweepCount]. */
    fun resetSweepCount() {
        sweepCount = 0L
    }

    // ------------------------------------------------------------------ the integrator

    private class Trace(
        val farRotation: Double,
        val maximumRotation: Double,
        val farCurvature: Double,
        val nearRotation: Double,
        val nearCurvature: Double,
        val tipAxial: Double,
        val displacement: Double,
        val bendingEnergy: Double,
        val firstIntegralSpread: Double
    )

    /**
     * One RK4 sweep of `(φ, φ′, x, z, ∫EIφ′²/2)` from the near end, for a shooting parameter
     * [parameter] — the near-end **rotation** for a compliant hinge, and the near-end **curvature**
     * for a rigid one.
     */
    private fun integrate(
        parameter: Double,
        force: Double,
        axialForce: Double
    ): Trace {
        sweepCount++
        val h = length / steps
        var phi = if (nearRigid) 0.0 else parameter
        var psi = if (nearRigid) parameter else nearStiffness * parameter / bendingRigidity
        val nearRotation = phi
        val nearCurvature = psi
        var x = 0.0
        var z = 0.0
        var energy = 0.0
        fun firstIntegral(a: Double, b: Double): Double =
            0.5 * bendingRigidity * b * b + force * sin(a) + axialForce * cos(a)
        var lowest = firstIntegral(phi, psi)
        var highest = lowest
        var largestRotation = abs(phi)
        // derivatives of the state (phi, psi, x, z, energy)
        fun dPsi(a: Double): Double = (-force * cos(a) + axialForce * sin(a)) / bendingRigidity
        repeat(steps) {
            val k1a = psi
            val k1b = dPsi(phi)
            val k1c = cos(phi)
            val k1d = sin(phi)
            val k1e = 0.5 * bendingRigidity * psi * psi

            val phi2 = phi + 0.5 * h * k1a
            val psi2 = psi + 0.5 * h * k1b
            val k2a = psi2
            val k2b = dPsi(phi2)
            val k2c = cos(phi2)
            val k2d = sin(phi2)
            val k2e = 0.5 * bendingRigidity * psi2 * psi2

            val phi3 = phi + 0.5 * h * k2a
            val psi3 = psi + 0.5 * h * k2b
            val k3a = psi3
            val k3b = dPsi(phi3)
            val k3c = cos(phi3)
            val k3d = sin(phi3)
            val k3e = 0.5 * bendingRigidity * psi3 * psi3

            val phi4 = phi + h * k3a
            val psi4 = psi + h * k3b
            val k4a = psi4
            val k4b = dPsi(phi4)
            val k4c = cos(phi4)
            val k4d = sin(phi4)
            val k4e = 0.5 * bendingRigidity * psi4 * psi4

            phi += h / 6.0 * (k1a + 2.0 * k2a + 2.0 * k3a + k4a)
            psi += h / 6.0 * (k1b + 2.0 * k2b + 2.0 * k3b + k4b)
            x += h / 6.0 * (k1c + 2.0 * k2c + 2.0 * k3c + k4c)
            z += h / 6.0 * (k1d + 2.0 * k2d + 2.0 * k3d + k4d)
            energy += h / 6.0 * (k1e + 2.0 * k2e + 2.0 * k3e + k4e)
            val integral = firstIntegral(phi, psi)
            if (integral < lowest) lowest = integral
            if (integral > highest) highest = integral
            if (abs(phi) > largestRotation) largestRotation = abs(phi)
        }
        val scale = max(
            1.0e-30,
            0.5 * bendingRigidity * nearCurvature * nearCurvature + abs(force) + abs(axialForce)
        )
        return Trace(
            farRotation = phi,
            maximumRotation = largestRotation,
            farCurvature = psi,
            nearRotation = nearRotation,
            nearCurvature = nearCurvature,
            tipAxial = x,
            displacement = z,
            bendingEnergy = energy,
            firstIntegralSpread = (highest - lowest) / scale
        )
    }

    // ------------------------------------------------------------------ the shooting solve

    private fun residual(trace: Trace, tipMoment: Double): Double =
        if (farRigid) trace.farRotation
        else bendingRigidity * trace.farCurvature + farStiffness * trace.farRotation - tipMoment

    /**
     * The arm's state under a transverse tip [force], an axial tip [axialForce] (positive in
     * tension) and an external tip moment [tipMoment].
     *
     * Solved by shooting on the near-end rotation: the residual is the far end's own moment
     * boundary condition, which is strictly increasing in the shooting parameter, so the bracket
     * `[0, ·]` is guaranteed and the root is found by a safeguarded Illinois iteration.
     *
     * **That monotonicity is a small-load property and this routine does not check it** (`T-159`).
     * The seed is a *linear* estimate, `0.25 F L²/EI`, which at 112 pN on the Gen-1 arm is already
     * **8.14 rad** — five right angles — so the very first cell can contain an even number of roots
     * and the doubling then brackets a later one. That is why the stroke-driven entry points
     * ([forceForDisplacement], [stateAtDisplacement]) no longer come through here: they continue
     * the branch instead, anchoring each root on the previous one. A caller that reaches this
     * function directly should stay where the residual has one root, and should read
     * [ElasticaState.maximumRotation] to see which branch it was answered on.
     */
    fun stateAtForce(
        force: Double,
        axialForce: Double = 0.0,
        tipMoment: Double = 0.0
    ): ElasticaState {
        require(force >= 0.0) { "force must not be negative, was: $force" }
        require(tipMoment >= 0.0) { "tipMoment must not be negative, was: $tipMoment" }
        // Shooting a beam under an AXIAL load is conditioned by exp(sqrt(|H|/EI) L): under
        // tension the linearised field equation is hyperbolic, so the residual's dependence on
        // the shooting parameter is amplified by that factor and loses monotonicity. Under
        // compression the same group past the Euler load makes the solution non-unique. The
        // primary reading of this task is H = 0 and needs none of it, so the range is DECLARED
        // rather than worked around.
        require(
            abs(axialForce) * length * length / bendingRigidity <= AXIAL_CONDITIONING_LIMIT
        ) {
            "an axial load of $axialForce pN on a $length nm arm of rigidity $bendingRigidity " +
                    "pN nm^2 is outside the shooting solver's conditioning range " +
                    "(|H| L^2/EI must not exceed $AXIAL_CONDITIONING_LIMIT)"
        }
        if (force == 0.0 && tipMoment == 0.0) {
            return ElasticaState(
                force = 0.0,
                axialForce = axialForce,
                tipMoment = 0.0,
                nearRotation = 0.0,
                farRotation = 0.0,
                nearMoment = 0.0,
                farMoment = 0.0,
                displacement = 0.0,
                tipAxial = length,
                drawIn = 0.0,
                strainEnergy = 0.0,
                momentBalanceResidual = 0.0,
                firstIntegralSpread = 0.0,
                maximumRotation = 0.0
            )
        }
        val seed = seedParameter(force, tipMoment)
        // Scan for the FIRST sign change above zero rather than assuming one. At small load the
        // residual is monotone in the shooting parameter and `[0, seed]` already brackets; once
        // the arm curls past a right angle the moment reverses and monotonicity is lost, so an
        // assumed sign at either end can be wrong. `C-0012`'s discipline — scan for the first
        // sign change and bisect on THAT bracket — in a new place.
        var low = 0.0
        var atLow = residual(integrate(0.0, force, axialForce), tipMoment)
        var high = seed
        var atHigh = residual(integrate(high, force, axialForce), tipMoment)
        var expansions = 0
        while ((atLow < 0.0) == (atHigh < 0.0) && atLow != 0.0 && atHigh != 0.0) {
            low = high
            atLow = atHigh
            high *= 2.0
            atHigh = residual(integrate(high, force, axialForce), tipMoment)
            expansions++
            require(expansions < 400) {
                "the far-end moment condition never changes sign below a near-end rotation of " +
                        "$high: the arm folds under $force pN and its elastica has no " +
                        "small-rotation branch"
            }
        }
        val root = illinoisRoot(low, high, atLow, atHigh) {
            residual(integrate(it, force, axialForce), tipMoment)
        }
        return stateOf(integrate(root, force, axialForce), force, axialForce, tipMoment)
    }

    /** The linear estimate of the shooting parameter, and this solver's scan scale. */
    private fun seedParameter(force: Double, tipMoment: Double): Double =
        if (nearRigid) {
            max(1.0e-30, 0.25 * (force * length + tipMoment) / bendingRigidity)
        } else {
            max(1.0e-30, 0.25 * (force * length * length + tipMoment * length) / bendingRigidity)
        }

    private fun stateOf(
        trace: Trace,
        force: Double,
        axialForce: Double,
        tipMoment: Double
    ): ElasticaState {
        val nearMoment = bendingRigidity * trace.nearCurvature
        val farMoment = bendingRigidity * trace.farCurvature
        val springEnergy = 0.5 * nearMoment * trace.nearRotation +
                0.5 * (tipMoment - farMoment) * trace.farRotation
        return ElasticaState(
            force = force,
            axialForce = axialForce,
            tipMoment = tipMoment,
            nearRotation = trace.nearRotation,
            farRotation = trace.farRotation,
            nearMoment = nearMoment,
            farMoment = farMoment,
            displacement = trace.displacement,
            tipAxial = trace.tipAxial,
            drawIn = length - trace.tipAxial,
            strainEnergy = trace.bendingEnergy + springEnergy,
            momentBalanceResidual = (nearMoment - farMoment) -
                    (force * trace.tipAxial - axialForce * trace.displacement),
            firstIntegralSpread = trace.firstIntegralSpread,
            maximumRotation = trace.maximumRotation
        )
    }

    // --------------------------------------------------- the branch continuation (T-159)

    private class BranchPoint(val parameter: Double, val trace: Trace)

    /**
     * The shooting root at [force] **immediately above** [parameterFloor], or `null` where the scan
     * reaches [SMALL_ROTATION_BRANCH_LIMIT] without one.
     *
     * The scan grows **geometrically** and takes the **first** sign change (`CLAUDE.md`), and a
     * trial whose sweep turns past a right angle is not a candidate at all: the step is halved
     * toward the last accepted parameter instead, so the bracket can only ever close on a root the
     * small-rotation branch owns. Where it cannot, this **refuses** — a root off the branch is a
     * different question, not a deeper answer.
     *
     * [parameterFloor] is legitimate as a negative endpoint whenever it is a root at a *smaller*
     * force: at a fixed shooting parameter the far-end moment residual is decreasing in the tip
     * force. That is **checked** here rather than assumed — a floor whose residual is not negative
     * returns `null`, and the caller shrinks its force step.
     */
    private fun branchPointAt(
        force: Double,
        axialForce: Double,
        parameterFloor: Double
    ): BranchPoint? {
        val atFloorTrace = integrate(parameterFloor, force, axialForce)
        if (atFloorTrace.maximumRotation >= SMALL_ROTATION_BRANCH_LIMIT) return null
        val atFloor = residual(atFloorTrace, 0.0)
        if (atFloor == 0.0) return BranchPoint(parameterFloor, atFloorTrace)
        if (atFloor > 0.0) return null
        var low = parameterFloor
        var atLow = atFloor
        var step = max(1.0e-300, 0.25 * seedParameter(force, 0.0))
        repeat(BRANCH_SCAN_CELLS) {
            val high = low + step
            val trace = integrate(high, force, axialForce)
            if (trace.maximumRotation >= SMALL_ROTATION_BRANCH_LIMIT) {
                step *= 0.5
                if (step <= BRANCH_SCAN_FLOOR * max(abs(low), 1.0e-300)) return null
                return@repeat
            }
            val atHigh = residual(trace, 0.0)
            if (atHigh >= 0.0) {
                val root = illinoisRoot(low, high, atLow, atHigh) {
                    residual(integrate(it, force, axialForce), 0.0)
                }
                val rootTrace = integrate(root, force, axialForce)
                return if (rootTrace.maximumRotation >= SMALL_ROTATION_BRANCH_LIMIT) null
                else BranchPoint(root, rootTrace)
            }
            low = high
            atLow = atHigh
            step *= 2.0
        }
        return null
    }

    /**
     * The arm's state at a stroke of [displacement] nm, reached by **continuing** the branch that
     * is connected to the unloaded state rather than by doubling a tip force blind.
     *
     * `C-0092`/`CH-0107`: a doubling ladder does not report a branch end, it reports having lost
     * the branch — on the Gen-1 arm the far-end residual acquires a second root at about 100 pN,
     * three decades of force below the right angle this class's own KDoc warns about, and a
     * doubling step then brackets the wrong one. Here every force step is solved with the previous
     * accepted root as its shooting floor, so the search cannot leave the branch; a step that fails
     * a branch test **shrinks** instead of doubling on; and where no smaller step keeps the branch,
     * the stroke is **refused** with the deepest stroke the branch did reach.
     */
    private fun branchStateAtDisplacement(
        displacement: Double,
        axialForce: Double
    ): ElasticaState {
        require(displacement > 0.0) { "displacement must be positive, was: $displacement" }
        // ROUNDED, deliberately, where every other `require` message in this repository is not
        // (`C-0153` §5): this refusal is DESIGNED to be caught and catalogued -- `C-0092`'s
        // branch taxonomy, `T-108`'s `catalogue[*].note` -- so it is a RESULT and not a
        // diagnostic, and it reaches a committed result file as text.
        require(displacement < length) {
            "an inextensible arm of ${length.roundedForProse()} nm cannot lift its tip " +
                    "${displacement.roundedForProse()} nm"
        }
        require(
            abs(axialForce) * length * length / bendingRigidity <= AXIAL_CONDITIONING_LIMIT
        ) {
            "an axial load of $axialForce pN on a $length nm arm of rigidity $bendingRigidity " +
                    "pN nm^2 is outside the shooting solver's conditioning range " +
                    "(|H| L^2/EI must not exceed $AXIAL_CONDITIONING_LIMIT)"
        }
        var lowForce = 0.0
        var lowStroke = 0.0
        var lowParameter = 0.0
        var trial = max(1.0e-30, 0.5 * displacement * smallRotationStiffness)
        var reached: BranchPoint? = null
        var steps = 0
        while (reached == null) {
            steps++
            require(steps < CONTINUATION_STEPS) {
                "the arm's small-rotation branch does not reach a stroke of " +
                        "${displacement.roundedForProse()} nm: the continuation reached " +
                        "${lowStroke.roundedForProse()} nm at ${lowForce.roundedForProse()} pN " +
                        "on a ${length.roundedForProse()} nm arm and exhausted its " +
                        "$CONTINUATION_STEPS force steps there"
            }
            val point = branchPointAt(trial, axialForce, lowParameter)
            if (point == null || point.trace.displacement <= lowStroke) {
                val shrunk = lowForce + 0.5 * (trial - lowForce)
                require(shrunk > lowForce && shrunk < trial) {
                    "the arm's small-rotation branch does not reach a stroke of $displacement " +
                            "nm: it reaches $lowStroke nm at $lowForce pN, and no larger tip " +
                            "force keeps the far-end moment condition on that branch"
                }
                trial = shrunk
                continue
            }
            if (point.trace.displacement >= displacement) {
                reached = point
            } else {
                lowForce = trial
                lowStroke = point.trace.displacement
                lowParameter = point.parameter
                trial *= 2.0
            }
        }
        val floor = lowParameter
        val root = illinoisRoot(
            lowForce, trial, lowStroke - displacement, reached.trace.displacement - displacement
        ) { force ->
            val here = branchPointAt(force, axialForce, floor)
            checkNotNull(here) {
                "the branch is undefined at $force pN inside a bracket it was found on"
            }.trace.displacement - displacement
        }
        val point = checkNotNull(branchPointAt(root, axialForce, floor)) {
            "the branch is undefined at its own root, $root pN"
        }
        return stateOf(point.trace, root, axialForce, 0.0)
    }

    /**
     * `C-0039`'s original strategy, retained under [BranchStrategy.DOUBLING_LADDER] and used
     * nowhere else: a blind doubling ladder in the tip force, each rung solved by the unanchored
     * [stateAtForce]. Kept so that `C-0092`'s measurement of its artefact stays reproducible.
     */
    private fun ladderStateAtDisplacement(
        displacement: Double,
        axialForce: Double
    ): ElasticaState {
        require(displacement > 0.0) { "displacement must be positive, was: $displacement" }
        // ROUNDED for the same reason as its twin above: a catalogued refusal is a result.
        require(displacement < length) {
            "an inextensible arm of ${length.roundedForProse()} nm cannot lift its tip " +
                    "${displacement.roundedForProse()} nm"
        }
        var low = 0.0
        var atLow = -displacement
        var high = max(1.0e-30, 0.5 * displacement * smallRotationStiffness)
        var atHigh = stateAtForce(high, axialForce).displacement - displacement
        var expansions = 0
        while (atHigh < 0.0) {
            low = high
            atLow = atHigh
            high *= 2.0
            atHigh = stateAtForce(high, axialForce).displacement - displacement
            expansions++
            require(expansions < 200) {
                "no transverse force below $high pN reaches a stroke of $displacement nm"
            }
        }
        val root = illinoisRoot(low, high, atLow, atHigh) {
            stateAtForce(it, axialForce).displacement - displacement
        }
        return stateAtForce(root, axialForce)
    }

    private fun solveAtDisplacement(displacement: Double, axialForce: Double): ElasticaState =
        when (strategy) {
            BranchStrategy.CONTINUATION -> branchStateAtDisplacement(displacement, axialForce)
            BranchStrategy.DOUBLING_LADDER -> ladderStateAtDisplacement(displacement, axialForce)
        }

    /** The transverse tip force in pN that drives the arm to a stroke of [displacement] nm. */
    fun forceForDisplacement(displacement: Double, axialForce: Double = 0.0): Double =
        solveAtDisplacement(displacement, axialForce).force

    /** The arm's state at a stroke of [displacement] nm — signed, and odd in the stroke. */
    fun stateAtDisplacement(displacement: Double, axialForce: Double = 0.0): ElasticaState =
        solveAtDisplacement(abs(displacement), axialForce)

    override fun reaction(displacement: Double): Double {
        if (displacement == 0.0) return 0.0
        return sign(displacement) * forceForDisplacement(abs(displacement))
    }

    override fun secantStiffness(displacement: Double): Double =
        if (displacement == 0.0) smallRotationStiffness
        else abs(reaction(displacement)) / abs(displacement)

    override fun tangentStiffness(displacement: Double): Double {
        val step = 1.0e-4 * max(1.0, abs(displacement))
        return (reaction(displacement + step) - reaction(displacement - step)) / (2.0 * step)
    }

    /** The moment in `pN·nm` carried by **one** of [hingeCount] crossovers at [displacement]. */
    fun hingeMomentPerCrossover(displacement: Double, hingeCount: Int): Double {
        require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
        return abs(stateAtDisplacement(displacement).nearMoment) / hingeCount
    }

    /** The force in pN on one crossover's backbone bonds, over a lever of [leverSeparation] nm. */
    fun hingeBondForce(
        displacement: Double,
        hingeCount: Int,
        leverSeparation: Double = Gen1Tile.INTERHELICAL_SHEET
    ): Double {
        require(leverSeparation > 0.0) {
            "leverSeparation must be positive, was: $leverSeparation"
        }
        return hingeMomentPerCrossover(displacement, hingeCount) / leverSeparation
    }
}

/**
 * The state of a [TwoSpringElastica] under one load.
 *
 * @property nearMoment `EI φ′(0)`, the bending moment at the hinge — which the hinge spring reacts.
 * @property farMoment `EI φ′(L)`, the bending moment at the anchorage; with no external tip moment
 *   the anchorage's own couple is its magnitude.
 * @property drawIn `L − x(L)` in nm — the **in-plane approach** the two attachment points demand of
 *   each other. It is not optional: see [chordDrawInBound].
 * @property momentBalanceResidual `EI(φ′(0) − φ′(L)) − (F x(L) − H z(L))`, which the beam's own
 *   global moment equilibrium makes zero — a conservation check on two independently computed
 *   quantities, the shot end curvatures against the integrated end position.
 * @property maximumRotation `max_s |φ(s)|` in radians — the validity flag of the solve. Past
 *   `π/2` the tip force's moment arm reverses, the shooting residual stops being monotone in the
 *   near-end rotation, and the elastica acquires branches this solver does not enumerate.
 */
data class ElasticaState(
    val force: Double,
    val axialForce: Double,
    val tipMoment: Double,
    val nearRotation: Double,
    val farRotation: Double,
    val nearMoment: Double,
    val farMoment: Double,
    val displacement: Double,
    val tipAxial: Double,
    val drawIn: Double,
    val strainEnergy: Double,
    val momentBalanceResidual: Double,
    val firstIntegralSpread: Double,
    val maximumRotation: Double
)

// ------------------------------------------------------------------ the geometric bounds

/**
 * **The draw-in an inextensible arm of [length] nm cannot avoid** at a stroke of [displacement] nm:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`e ≥ L − √(L² − δ²)`**,
 *
 * because the chord `√(x(L)² + δ²)` can never exceed the contour `L`. Pure geometry — no elastica,
 * no constitutive law, and no joint design can relax it. At §3's desired 10 nm stroke on the
 * 11.03–12.50 nm arms the two standing compositions place, it is **4.4–4.9 nm**, i.e. 13–14 base
 * pairs of in-plane give.
 */
fun chordDrawInBound(length: Double, displacement: Double): Double {
    require(length > 0.0) { "length must be positive, was: $length" }
    require(abs(displacement) <= length) {
        "an arm of contour $length nm cannot reach a stroke of $displacement nm at all"
    }
    return length - sqrt(length * length - displacement * displacement)
}

/**
 * **The axial strain an arm whose ends CANNOT draw in must carry** at a stroke of [displacement]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`ε ≥ √(1 + (δ/L)²) − 1`**,
 *
 * because holding the ends at their original axial separation `L` while offsetting them by `δ`
 * puts the chord at `√(L² + δ²)`, which the contour must at least reach.
 *
 * So `C-0023`'s *"free to draw in"* / *"held axially"* pair is **not** a free binary for a hinged
 * arm: the free reading is the only inextensible one, and the held reading costs a tension that is
 * bounded below by [restrainedTensionBound].
 */
fun restrainedAxialStrainBound(length: Double, displacement: Double): Double {
    require(length > 0.0) { "length must be positive, was: $length" }
    val ratio = displacement / length
    return sqrt(1.0 + ratio * ratio) - 1.0
}

/** [restrainedAxialStrainBound] times the duplex stretch modulus, in pN. */
fun restrainedTensionBound(
    length: Double,
    displacement: Double,
    stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS
): Double {
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    return stretchModulus * restrainedAxialStrainBound(length, displacement)
}

// ------------------------------------------------------------------ placement and the cap

/**
 * The arm length in nm at which [count] two-spring **elasticas** present [targetStiffness] as a
 * **secant** at [workingDisplacement] — `C-0017`'s placement condition, on the composition that is
 * exact in the rotation *and* in the end condition.
 *
 * Both standing readings are approximations to this one from opposite sides, and neither is it.
 */
fun elasticaArmForStiffness(
    hingeStiffness: Double,
    hingeCount: Int,
    farStiffness: Double,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    count: Int = 45,
    targetStiffness: Double = 100.0 / 3.0,
    workingDisplacement: Double = 3.0,
    steps: Int = 400,
    maximumArm: Double = 120.0
): Double {
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
    return elasticaArmForNearStiffness(
        hingeCount * hingeStiffness, farStiffness, bendingRigidity, count, targetStiffness,
        workingDisplacement, steps, maximumArm
    )
}

/**
 * **The arm cap, re-solved under exact rotation**: the length at which [count] arms on a **rigid**
 * hinge already present [targetStiffness], so that no hinge count whatever can place a longer one.
 *
 * `C-0029` evaluated `(c n EI/k)^(1/3)` at an asserted `c`; `C-0034` made it the fixed point `ρ_f`
 * being a function of the arm forces it to be; this adds the geometry, and the cap moves again —
 * always **outward**, because the exact arm stiffens.
 */
fun elasticaArmCeiling(
    farStiffness: Double,
    count: Int = 45,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    targetStiffness: Double = 100.0 / 3.0,
    workingDisplacement: Double = 3.0,
    steps: Int = 400,
    maximumArm: Double = 120.0
): Double = elasticaArmForNearStiffness(
    Double.POSITIVE_INFINITY, farStiffness, bendingRigidity, count, targetStiffness,
    workingDisplacement, steps, maximumArm
)

private fun elasticaArmForNearStiffness(
    nearStiffness: Double,
    farStiffness: Double,
    bendingRigidity: Double,
    count: Int,
    targetStiffness: Double,
    workingDisplacement: Double,
    steps: Int,
    maximumArm: Double
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingDisplacement > 0.0) {
        "workingDisplacement must be positive, was: $workingDisplacement"
    }
    fun assembled(arm: Double): Double = count * TwoSpringElastica(
        bendingRigidity, arm, nearStiffness, farStiffness, steps
    ).secantStiffness(workingDisplacement)
    val low = 1.5 * workingDisplacement
    require(assembled(low) > targetStiffness) {
        "even a $low nm arm is softer than $targetStiffness pN/nm"
    }
    require(assembled(maximumArm) < targetStiffness) {
        "no arm as long as $maximumArm nm is softer than $targetStiffness pN/nm"
    }
    return illinoisRoot(
        low, maximumArm,
        assembled(low) - targetStiffness, assembled(maximumArm) - targetStiffness
    ) { assembled(it) - targetStiffness }
}

// ------------------------------------------------------------------ the root finder

/**
 * A safeguarded **Illinois** root of [f] on a bracket that is known to change sign, exiting on the
 * **bracket width** rather than on a residual (`P-15`).
 *
 * Every secant step is checked to lie strictly inside the current bracket and replaced by a
 * bisection step when it does not — which is the repair `C-0031` made to `bracketedRoot`, whose
 * sign test on a *product* underflows when both factors are tiny.
 */
internal fun illinoisRoot(
    lowBound: Double,
    highBound: Double,
    atLowBound: Double,
    atHighBound: Double,
    tolerance: Double = 1.0e-14,
    f: (Double) -> Double
): Double {
    require(atLowBound <= 0.0 && atHighBound >= 0.0 || atLowBound >= 0.0 && atHighBound <= 0.0) {
        "the bracket [$lowBound, $highBound] does not change sign: " +
                "$atLowBound and $atHighBound"
    }
    var low = lowBound
    var high = highBound
    var atLow = atLowBound
    var atHigh = atHighBound
    if (atLow == 0.0) return low
    if (atHigh == 0.0) return high
    var estimate = 0.5 * (low + high)
    repeat(200) {
        if (high - low <= tolerance * max(abs(high), 1.0e-300)) return 0.5 * (low + high)
        var next = high - atHigh * (high - low) / (atHigh - atLow)
        if (!next.isFinite() || next <= low || next >= high) next = 0.5 * (low + high)
        val step = abs(next - estimate)
        estimate = next
        val atNext = f(next)
        if (atNext == 0.0) return next
        if ((atNext < 0.0) == (atHigh < 0.0)) {
            high = next
            atHigh = atNext
            atLow *= 0.5
        } else {
            low = high
            atLow = atHigh
            high = next
            atHigh = atNext
        }
        if (low > high) {
            val swapValue = low
            low = high
            high = swapValue
            val swapResidual = atLow
            atLow = atHigh
            atHigh = swapResidual
        }
        // A secant iteration exits on its own STEP, not on a residual: once the problem is
        // locally linear the first secant step is already exact and the bracket stops shrinking,
        // so a bracket-width-only exit stalls and returns the midpoint of a bracket whose root
        // sits at one end. CLAUDE.md's "an unreachable convergence tolerance is silent", in the
        // one place where it would have been silently WRONG rather than merely slow.
        if (step <= tolerance * max(abs(next), 1.0e-300)) return next
    }
    return estimate
}
