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

import com.xemantic.nano.plentyofroom.anchoring.TwoSpringElastica
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Task `T-157` — **does `C-0069`'s `Q5` fold on the large-rotation branch?** Leaf `A2.2`.
 *
 * ## The theorem that settles the unbounded half, and it costs nothing
 *
 * One arm's stroke is `δ = z(L) = ∫₀^L sin φ(s) ds`, so `δ ≤ L` with equality only if `φ ≡ π/2`
 * almost everywhere. But `φ ≡ π/2` forces `φ′ ≡ 0`, and the near-end condition
 * `EI φ′(0) = k_n φ(0)` then gives `φ(0) = 0 ≠ π/2` for any finite `k_n > 0`.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`δ < L` strictly, on every branch, at every tip force, at every rotation.**
 *
 * So **no equilibrium — and therefore no pull-in fold — exists at any stroke at or above the arm's
 * own contour**, 8.16439083 nm for `C-0069`'s `Q5`. `C-0084` bounded its *"no fold"* at 7.9097 nm
 * of stroke; this bounds the remaining question at **0.2547 nm**, before any code runs.
 *
 * ## What the expensive half is then for
 *
 * `C-0084` reads its ceiling off `C-0039`'s [TwoSpringElastica.forceForDisplacement], which
 * **doubles** a tip force from `0.5 δ k_small` until the stroke reaches its target, and off
 * `stateAtForce`, which brackets the shooting parameter by doubling from a seed and runs Illinois
 * inside whatever that produces. Both are exact while the far-end residual has **one** root. A
 * second root appears at a tip force well below the right angle `C-0039`'s KDoc warns about, and a
 * doubling ladder that steps over the last single-root force does not report a branch end — **it
 * reports having lost the branch.**
 *
 * This class therefore does two things `C-0039` does not:
 *
 * 1. [branchesAt] enumerates **every** sign change of the far-end moment residual on a declared
 *    shooting range, integrating each root so a reader can see which branch a state is on —
 *    `δ`, `max_s|φ|`, the first-integral spread and the moment-balance residual per root;
 * 2. [branchTable] **continues** the branch connected to the unloaded state, by marching the
 *    near-end rotation `φ0` upward in small steps and taking, at each, the **first** sign change of
 *    the residual in the tip force above the previous one — `CLAUDE.md`'s *"scan for the first sign
 *    change and bisect on that bracket"*, applied **along** a branch rather than across one — and
 *    stopping where the **first integral** stops being conserved to
 *    [BRANCH_FIRST_INTEGRAL_TOLERANCE]. That is a **measured** validity limit, not a ladder artefact.
 *
 * ## Geometry and signs, `C-0039`'s, restated rather than inherited
 *
 * Arc length `s ∈ [0, L]` from the hinge; `φ(s)` the tangent angle from the undeformed axis toward
 * the stroke; `x(s) = ∫cos φ`, `z(s) = ∫sin φ`; the far body applies a transverse tip force `F`
 * along `+z` and no axial force and no external tip moment, so
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`EI φ″ = −F cos φ`, &nbsp; `EI φ′(0) = k_n φ(0)`, &nbsp;
 * `EI φ′(L) = −k_f φ(L)`,
 *
 * and the conserved first integral of the field equation is `½ EI φ′² + F sin φ`.
 *
 * The RK4 integrator here is **written independently** of `TwoSpringElastica`'s, which is what makes
 * the small-load agreement between them (gate 2) an independent check rather than a tautology.
 */

/**
 * The relative drift of `½ EI φ′² + F sin φ` a solved shape may carry and still be reported.
 *
 * The first integral is conserved **exactly** by the field equation, so its measured spread along an
 * RK4 sweep is the integrator's own error, reported in the same units the residual is judged in.
 * `1e−9` is four decades above the `~1e−13` the branch carries where it is well conditioned and four
 * decades below the `~1e−5` at which the shooting problem has visibly stiffened.
 */
const val BRANCH_FIRST_INTEGRAL_TOLERANCE: Double = 1.0e-9

/** One solved shape of one arm: a root of the far-end moment residual, integrated. */
@Serializable
data class ArmBranchPoint(
    val force: Double,
    val nearRotation: Double,
    val farRotation: Double,
    val farCurvature: Double,
    val stroke: Double,
    val tipAxial: Double,
    val drawIn: Double,
    val maximumRotation: Double,
    val firstIntegralSpread: Double,
    val momentBalanceResidual: Double
) {

    /** `max_s|φ| < π/2` — the branch `C-0039`'s shooting solve is written for. */
    val onTheSmallRotationBranch: Boolean get() = maximumRotation < 0.5 * PI
}

/**
 * **The theorem, as a predicate**: an inextensible arm of [contour] nm cannot reach [stroke].
 *
 * Pure geometry — no elastica, no integrator, no convergence parameter — and it holds on every
 * branch of the boundary-value problem at every tip force.
 */
fun strokeIsBelowContour(contour: Double, stroke: Double): Boolean {
    require(contour > 0.0 && contour.isFinite()) {
        "contour must be positive and finite, was: $contour"
    }
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    return stroke < contour
}

/**
 * The multi-branch elastica of one hinge-rooted arm.
 *
 * @param rotationStep the near-end rotation increment the branch is continued in, in rad.
 */
class LargeRotationArmBranch(
    val bendingRigidity: Double,
    val contour: Double,
    val nearStiffness: Double,
    val farStiffness: Double,
    val steps: Int = 800,
    val rotationStep: Double = 2.0e-3
) {

    init {
        require(bendingRigidity > 0.0) { "bendingRigidity must be positive" }
        require(contour > 0.0) { "contour must be positive" }
        require(nearStiffness > 0.0) { "nearStiffness must be positive" }
        require(farStiffness > 0.0) { "farStiffness must be positive" }
        require(steps >= 16) { "steps must be at least 16, was: $steps" }
        require(rotationStep > 0.0 && rotationStep < 0.1) {
            "rotationStep must lie in (0, 0.1) rad, was: $rotationStep"
        }
    }

    /** `C-0034`'s closed-form vanishing-load stiffness of ONE arm, in pN/nm. */
    val smallRotationStiffnessPerArm: Double =
        TwoSpringElastica(bendingRigidity, contour, nearStiffness, farStiffness, steps)
            .smallRotationStiffness

    // ------------------------------------------------------------------ the integrator

    private fun sweep(nearRotation: Double, force: Double): ArmBranchPoint {
        val h = contour / steps
        var phi = nearRotation
        var psi = nearStiffness * nearRotation / bendingRigidity
        val nearCurvature = psi
        var x = 0.0
        var z = 0.0
        var largest = abs(phi)
        fun firstIntegral(a: Double, b: Double): Double =
            0.5 * bendingRigidity * b * b + force * sin(a)
        var lowest = firstIntegral(phi, psi)
        var highest = lowest
        fun dPsi(a: Double): Double = -force * cos(a) / bendingRigidity
        repeat(steps) {
            val k1a = psi
            val k1b = dPsi(phi)
            val k1c = cos(phi)
            val k1d = sin(phi)
            val phi2 = phi + 0.5 * h * k1a
            val psi2 = psi + 0.5 * h * k1b
            val k2a = psi2
            val k2b = dPsi(phi2)
            val k2c = cos(phi2)
            val k2d = sin(phi2)
            val phi3 = phi + 0.5 * h * k2a
            val psi3 = psi + 0.5 * h * k2b
            val k3a = psi3
            val k3b = dPsi(phi3)
            val k3c = cos(phi3)
            val k3d = sin(phi3)
            val phi4 = phi + h * k3a
            val psi4 = psi + h * k3b
            val k4a = psi4
            val k4b = dPsi(phi4)
            val k4c = cos(phi4)
            val k4d = sin(phi4)
            phi += h / 6.0 * (k1a + 2.0 * k2a + 2.0 * k3a + k4a)
            psi += h / 6.0 * (k1b + 2.0 * k2b + 2.0 * k3b + k4b)
            x += h / 6.0 * (k1c + 2.0 * k2c + 2.0 * k3c + k4c)
            z += h / 6.0 * (k1d + 2.0 * k2d + 2.0 * k3d + k4d)
            val integral = firstIntegral(phi, psi)
            if (integral < lowest) lowest = integral
            if (integral > highest) highest = integral
            if (abs(phi) > largest) largest = abs(phi)
        }
        val scale = max(
            1.0e-30,
            0.5 * bendingRigidity * nearCurvature * nearCurvature + abs(force)
        )
        return ArmBranchPoint(
            force = force,
            nearRotation = nearRotation,
            farRotation = phi,
            farCurvature = psi,
            stroke = z,
            tipAxial = x,
            drawIn = contour - x,
            maximumRotation = largest,
            firstIntegralSpread = (highest - lowest) / scale,
            momentBalanceResidual =
                bendingRigidity * (nearCurvature - psi) - force * x
        )
    }

    /** The far end's own moment boundary condition, `EI φ′(L) + k_f φ(L)`. */
    private fun residual(nearRotation: Double, force: Double): Double {
        val point = sweep(nearRotation, force)
        return bendingRigidity * point.farCurvature + farStiffness * point.farRotation
    }

    // ------------------------------------------------------------------ multi-branch enumeration

    /**
     * **Every** root of the far-end moment residual at [force], on `[0, shootingCeiling]`.
     *
     * A scan, not a bracket: the residual is not monotone in the shooting parameter once the arm
     * curls, so an assumed sign at either end is unsafe (`CLAUDE.md`). Roots closer together than
     * one scan cell are missed and the count is therefore a **lower bound**, which is stated rather
     * than hidden — a search over a continuum returns a density, and the density is the grid's.
     */
    fun branchesAt(
        force: Double,
        shootingCeiling: Double = 4.0 * PI,
        scanSteps: Int = 4000
    ): List<ArmBranchPoint> {
        require(force >= 0.0) { "force must not be negative, was: $force" }
        require(shootingCeiling > 0.0) {
            "shootingCeiling must be positive, was: $shootingCeiling"
        }
        require(scanSteps >= 8) { "scanSteps must be at least 8, was: $scanSteps" }
        val roots = mutableListOf<ArmBranchPoint>()
        var previous = 0.0
        var atPrevious = residual(0.0, force)
        for (i in 1..scanSteps) {
            val here = shootingCeiling * i / scanSteps
            val atHere = residual(here, force)
            if ((atHere < 0.0) != (atPrevious < 0.0)) {
                roots += sweep(bisectRotation(previous, here, atPrevious, force), force)
            }
            previous = here
            atPrevious = atHere
        }
        return roots
    }

    private fun bisectRotation(
        low: Double,
        high: Double,
        atLow: Double,
        force: Double
    ): Double {
        var lower = low
        var upper = high
        var signLow = atLow < 0.0
        repeat(200) {
            val middle = 0.5 * (lower + upper)
            val atMiddle = residual(middle, force)
            if ((atMiddle < 0.0) == signLow) lower = middle else upper = middle
            signLow = residual(lower, force) < 0.0
            if (upper - lower <= 1.0e-14 * max(abs(upper), 1.0)) return 0.5 * (lower + upper)
        }
        return 0.5 * (lower + upper)
    }

    // ------------------------------------------------------------------ the continuation

    /**
     * The branch continuously connected to the unloaded state, marched in the near-end rotation.
     *
     * Ascending in both the stroke and the tip force, and truncated at the first row whose first
     * integral drifts past [BRANCH_FIRST_INTEGRAL_TOLERANCE], whose stroke fails to increase, or
     * whose `max_s|φ|` reaches a right angle.
     */
    val branchTable: List<ArmBranchPoint> by lazy { continueBranch() }

    /** The deepest stroke in nm the continued branch reaches — **strictly below [contour]**. */
    val strokeSupremum: Double get() = branchTable.last().stroke

    private fun continueBranch(): List<ArmBranchPoint> {
        val table = mutableListOf<ArmBranchPoint>()
        var force = 1.0e-9
        var rotation = rotationStep
        while (rotation < 0.5 * PI) {
            val located = firstCrossingAbove(rotation, force) ?: break
            val point = sweep(rotation, located)
            if (point.firstIntegralSpread > BRANCH_FIRST_INTEGRAL_TOLERANCE) break
            if (point.maximumRotation >= 0.5 * PI) break
            if (table.isNotEmpty() && point.stroke <= table.last().stroke) break
            if (point.stroke >= contour) break
            table += point
            force = located
            rotation += rotationStep
        }
        require(table.size >= 8) {
            "the branch continuation produced only ${table.size} rows, which is not a branch"
        }
        return table
    }

    /** The first tip force above [lowForce] at which the residual changes sign, at [nearRotation]. */
    private fun firstCrossingAbove(
        nearRotation: Double,
        lowForce: Double,
        ratio: Double = 1.05,
        cap: Double = 1.0e9
    ): Double? {
        var low = max(1.0e-12, lowForce * 0.999)
        var atLow = residual(nearRotation, low)
        while (low < cap) {
            val high = low * ratio
            val atHigh = residual(nearRotation, high)
            if ((atHigh < 0.0) != (atLow < 0.0)) return bisectForce(nearRotation, low, high, atLow)
            low = high
            atLow = atHigh
        }
        return null
    }

    private fun bisectForce(
        nearRotation: Double,
        lowForce: Double,
        highForce: Double,
        atLowForce: Double
    ): Double {
        var low = lowForce
        var high = highForce
        val signLow = atLowForce < 0.0
        repeat(200) {
            val middle = 0.5 * (low + high)
            if ((residual(nearRotation, middle) < 0.0) == signLow) low = middle else high = middle
            if (high - low <= 1.0e-13 * max(abs(high), 1.0e-30)) return 0.5 * (low + high)
        }
        return 0.5 * (low + high)
    }

    // ------------------------------------------------------------------ the load line

    /**
     * The transverse tip force in pN that drives **one** arm to [stroke] nm on the continued branch.
     *
     * Two nested bisections, each on a bracket the table **guarantees**: the tip force inside the
     * table cell that spans the stroke, and the near-end rotation inside the same cell's rotation
     * span. Neither can escape onto another branch, which is the whole point of building the table.
     */
    fun forceForStroke(stroke: Double): Double {
        require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
        require(strokeIsBelowContour(contour, stroke)) {
            "an inextensible arm of $contour nm cannot lift its tip $stroke nm"
        }
        require(stroke <= strokeSupremum) {
            "the continued branch reaches $strokeSupremum nm and was asked for $stroke nm"
        }
        val index = branchTable.indexOfFirst { it.stroke >= stroke }
        if (index == 0) return branchTable.first().force * stroke / branchTable.first().stroke
        val below = branchTable[index - 1]
        val above = branchTable[index]
        var low = below.force
        var high = above.force
        repeat(200) {
            val middle = 0.5 * (low + high)
            val here = strokeOnBranchAt(middle, below.nearRotation, above.nearRotation)
            if (here < stroke) low = middle else high = middle
            if (high - low <= 1.0e-12 * max(abs(high), 1.0)) return 0.5 * (low + high)
        }
        return 0.5 * (low + high)
    }

    private fun strokeOnBranchAt(
        force: Double,
        rotationLow: Double,
        rotationHigh: Double
    ): Double {
        var low = rotationLow
        var high = rotationHigh
        val atLow = residual(low, force)
        val signLow = atLow < 0.0
        repeat(200) {
            val middle = 0.5 * (low + high)
            if ((residual(middle, force) < 0.0) == signLow) low = middle else high = middle
            if (high - low <= 1.0e-14 * max(abs(high), 1.0)) return sweep(0.5 * (low + high), force).stroke
        }
        return sweep(0.5 * (low + high), force).stroke
    }

    /** The reaction of [count] arms in parallel at [stroke] nm, in pN. */
    fun reaction(stroke: Double, count: Int = GEN1_RECOMMENDED_PATH_COUNT): Double {
        require(count > 0) { "count must be positive, was: $count" }
        if (stroke == 0.0) return 0.0
        return count * forceForStroke(stroke)
    }
}

// ---------------------------------------------------------------- the recommended arm

private val defaultRecommendedArmBranch: LargeRotationArmBranch by lazy {
    buildRecommendedArmBranch(800, 2.0e-3)
}

/**
 * `C-0069`'s `Q5` as a multi-branch elastica — the arm length re-derived through `C-0039`'s own
 * placement solve, exactly as `C-0084` re-derives it, and never read from a result file.
 */
fun recommendedArmBranches(
    steps: Int = 800,
    rotationStep: Double = 2.0e-3
): LargeRotationArmBranch =
    if (steps == 800 && rotationStep == 2.0e-3) defaultRecommendedArmBranch
    else buildRecommendedArmBranch(steps, rotationStep)

private fun buildRecommendedArmBranch(
    steps: Int,
    rotationStep: Double
): LargeRotationArmBranch {
    val placed = recommendedArmLine("LQ5 recommended hinge-rooted arm (C-0071)")
    return LargeRotationArmBranch(
        bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
        contour = placed.length,
        nearStiffness = GEN1_ARM_ROOT_STIFFNESS,
        farStiffness = GEN1_ARM_TIP_STIFFNESS,
        steps = steps,
        rotationStep = rotationStep
    )
}

/**
 * **`C-0084`'s ceiling, reproduced**: the largest stroke in nm at which `C-0039`'s own doubling
 * force ladder still answers for the recommended arm.
 *
 * Read on the same object `C-0084` read it on — `recommendedArmLine` through
 * [com.xemantic.nano.plentyofroom.stability.loadLineStrokeCeiling] — so that the difference between
 * this and [LargeRotationArmBranch.strokeSupremum] is a property of the **solver** and not of two
 * different arms.
 */
fun ladderRefusalStroke(resolution: Double = 1.0e-6): Double {
    val line = recommendedArmLine("LQ5 recommended hinge-rooted arm (C-0071)")
    return loadLineStrokeCeiling(
        line = line,
        low = 3.0,
        high = min(line.length - 1.0e-9, line.length),
        resolution = resolution
    )
}
