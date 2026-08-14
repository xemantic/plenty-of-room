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
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * `T-99`, leaf `A8.2` — the **`(path count, hinge count, hinge-line length)`** trade for the
 * crossover-hinge flexure, and whether *fewer, longer* flexures close where 45 short ones do not.
 *
 * ## Why the three variables are not independent
 *
 * A **hinge line** is a maximal set of crossovers sharing one interface and one pair of bodies
 * (`C-0040`). A flexure of [TradePoint.hingeCount] crossovers therefore demands
 * `(h − 1) p` nm of **collinear interface**, `p = 32 bp = 10.88 nm`, and `h` crossovers out of the
 * tile's own **counted** inventory — `C-0015`'s 56 at the ten eight-column phases and 49 at the
 * other twenty-two. So
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`n · h ≤ N_inv`** &nbsp;and&nbsp; **`n (h − 1) p ≤ L_line`**,
 *
 * and *"fewer flexures, each on a longer hinge line"* spends **one** budget twice rather than
 * escaping it. That is the whole structure of the question, and it is settled by two divisions.
 *
 * ## What the cheap bounds can and cannot do
 *
 * [hingeSuppliedArmCeiling] is the arm a rigid-armed flexure array can place **if its hinges are
 * the only restraint**. It is a function of the **product** `n·h` and of nothing else, so along
 * the trade curve `n·h = N_inv` it is *constant* — the degeneracy the task exists to test.
 *
 * [rigidArmCeiling] with the far anchorage restored is **not** degenerate, because
 * `C-0034`'s `A2` couple is a **per-flexure** stiffness: the array's total rotational restraint is
 * `N_inv k_θ + n k_far`, which grows with `n`. **That is what decides which way the trade runs**,
 * and it runs *against* the task's premise: the anchorage, not the hinge, carries a short-hinge
 * flexure, so more paths on fewer crossovers each place a **longer** arm than fewer paths on
 * longer hinge lines.
 *
 * ## Geometry and signs, restated (`gpd/tasks/T-99-fewer-longer-flexures.md`)
 *
 * `x` runs **along** the tile's helices, `y` **across** them, `z` positive **upward**. The element
 * is `C-0034`'s `E5a` solved as `C-0039`'s inextensible two-spring elastica: a hinge of `h`
 * crossovers grounded on the tile, a one-duplex arm of length `r`, the arm's own duplex end as far
 * anchorage, free to draw in (`H = 0`). Positive `δ` is the stroke the coupling delivers.
 */

// ------------------------------------------------------------------ the two ledgers

/** The crossovers `pathCount` flexures of `hingeCount` each take out of the tile's inventory. */
fun hingeCrossoverDemand(pathCount: Int, hingeCount: Int): Int {
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
    return pathCount * hingeCount
}

/**
 * The **collinear interface** in nm that `pathCount` hinge lines of `hingeCount` crossovers demand:
 * `n (h − 1) p`. A single crossover needs no line at all, which is why `h = 1` is free of this
 * ledger and bound only by the crossover count.
 */
fun collinearLineDemand(pathCount: Int, hingeCount: Int, pitch: Double): Double {
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    return pathCount * (hingeCount - 1) * pitch
}

/**
 * The collinear interface a sheet of [duplexes] and edge [edge] supplies: `(D + 1) · edge` —
 * `D − 1` interior interfaces plus the **2 free edges**, 640 nm for `C-0015`'s 15-duplex 40 nm
 * tile.
 *
 * It is an **upper** bound on what a design may use: `C-0040` records that every crossover in the
 * inventory is already a structural load path in `C-0009`'s grillage, so converting an interface
 * into a free hinge line removes it from the sheet.
 */
fun interfaceLineSupply(duplexes: Int, edge: Double): Double {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(edge > 0.0) { "edge must be positive, was: $edge" }
    return (duplexes + 1) * edge
}

/** The largest hinge count `pathCount` flexures can each own out of [inventory] — `⌊N/n⌋`. */
fun maximumHingeCountForInventory(pathCount: Int, inventory: Int): Int {
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    require(inventory >= 0) { "inventory must not be negative, was: $inventory" }
    return inventory / pathCount
}

/** The largest path count that can each own [hingeCount] crossovers out of [inventory]. */
fun maximumPathCountForInventory(hingeCount: Int, inventory: Int): Int {
    require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
    require(inventory >= 0) { "inventory must not be negative, was: $inventory" }
    return inventory / hingeCount
}

/**
 * The path count a coupling **placed** at [targetStiffness] needs to deliver a stroke of [stroke]
 * without any load path exceeding [allowable] — `⌈k_c · δ / A⌉`.
 *
 * On `C-0017`'s mandate secant at §3's desired stroke this is `33.333 × 10 / 10 = 34` exactly,
 * which is the floor `C-0041` and `C-0040` quote. It is read on the **placement**, so it moves
 * with the placement and not with the element.
 */
fun minimumPathCountForAllowable(
    targetStiffness: Double,
    stroke: Double,
    allowable: Double
): Int {
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    require(allowable > 0.0) { "allowable must be positive, was: $allowable" }
    return ceil(targetStiffness * stroke / allowable - 1.0e-12).toInt()
}

// ------------------------------------------------------------------ the rigid-arm ceilings

/**
 * The arm in nm at which a **rigid** arm array of total rotational restraint [restraint]
 * `pN·nm/rad` presents [targetStiffness] as a secant at [workingDisplacement], at **exact**
 * rotation.
 *
 * The array's moment balance is `Σk · θ = n F r cos θ` with `δ = r sin θ`, so the placement
 * condition collapses to
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`θ tan θ = k_target δ²/Σk`**, &nbsp;&nbsp; `r = δ/sin θ`,
 *
 * which needs no elastica and carries no constitutive law beyond the springs. **It bounds the
 * placed arm from above**, because putting the arm's own bending compliance in makes the element
 * softer and a softer element must be shorter to place at the same stiffness.
 */
fun rigidArmCeiling(
    restraint: Double,
    targetStiffness: Double,
    workingDisplacement: Double
): Double {
    require(restraint > 0.0) { "restraint must be positive, was: $restraint" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingDisplacement > 0.0) {
        "workingDisplacement must be positive, was: $workingDisplacement"
    }
    val target = targetStiffness * workingDisplacement * workingDisplacement / restraint
    // theta tan theta is strictly increasing on (0, pi/2) from 0 to infinity, so the bracket is
    // guaranteed and a bisection on the BRACKET WIDTH is exact (`P-15`).
    var low = 0.0
    var high = 0.5 * Math.PI - 1.0e-12
    repeat(300) {
        val middle = 0.5 * (low + high)
        if (middle * tan(middle) < target) low = middle else high = middle
        if (high - low <= 1.0e-15) return workingDisplacement / sin(0.5 * (low + high))
    }
    return workingDisplacement / sin(0.5 * (low + high))
}

/**
 * The same ceiling at **small** rotation — `r = √(Σk/k_target)`, the `δ → 0` limit of
 * [rigidArmCeiling], which it therefore bounds from below.
 */
fun smallRotationArmCeiling(restraint: Double, targetStiffness: Double): Double {
    require(restraint > 0.0) { "restraint must be positive, was: $restraint" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    return sqrt(restraint / targetStiffness)
}

/**
 * **The arm the crossover inventory alone can place** — [rigidArmCeiling] on `N k_θ`, with the far
 * anchorage contributing nothing.
 *
 * It is a function of the **product** `n·h` and of nothing else, so it is *constant* along the
 * whole `n·h = N` trade curve. That is the degeneracy `T-99` exists to test, and it is the reason
 * *"fewer, longer"* cannot buy an arm on the hinge budget alone.
 */
fun hingeSuppliedArmCeiling(
    inventory: Int,
    hingeStiffness: Double,
    targetStiffness: Double,
    workingDisplacement: Double
): Double {
    require(inventory > 0) { "inventory must be positive, was: $inventory" }
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    return rigidArmCeiling(inventory * hingeStiffness, targetStiffness, workingDisplacement)
}

/**
 * The same with `C-0034`'s far anchorage restored: the array's restraint is `N k_θ + n k_far`,
 * because the anchorage couple is a **per-flexure** quantity while the hinge inventory is not.
 *
 * This is the bound that says which way the trade runs.
 */
fun combinedArmCeiling(
    inventory: Int,
    hingeStiffness: Double,
    pathCount: Int,
    farStiffness: Double,
    targetStiffness: Double,
    workingDisplacement: Double
): Double {
    require(inventory > 0) { "inventory must be positive, was: $inventory" }
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    require(farStiffness >= 0.0) { "farStiffness must not be negative, was: $farStiffness" }
    return rigidArmCeiling(
        inventory * hingeStiffness + pathCount * farStiffness,
        targetStiffness, workingDisplacement
    )
}

/**
 * **The stroke ceiling stability alone imposes**, at a fixed delivered force: `δ ≤ F/|k_eff|`.
 *
 * `C-0017`'s placement is `k_c = F/δ` and its stability clause is `k_c > |k_eff|`, so the two
 * together bound the stroke without any reference to what the coupling is made of. At the 10 nm
 * layer in 2 mM MgCl₂, `|k_eff| = 23.41–27.91 pN/nm` and §3's 100 pN give **3.58–4.27 nm**.
 */
fun strokeCeilingFromStability(targetForce: Double, stabilityFloor: Double): Double {
    require(targetForce > 0.0) { "targetForce must be positive, was: $targetForce" }
    require(stabilityFloor > 0.0) { "stabilityFloor must be positive, was: $stabilityFloor" }
    return targetForce / stabilityFloor
}

// ------------------------------------------------------------------ the usable stroke

/**
 * The largest stroke at which [count] of [beam] still hold their assembled **tangent** inside
 * [ceiling] pN/nm — `C-0023`'s compliance clause read over the whole stroke rather than at the
 * working point.
 *
 * Scanned from **below** and bisected on the first sign change (`C-0012`'s discipline): past a
 * right angle the elastica's shooting residual stops being monotone, and on a stiffening element
 * the ceiling is crossed long before that.
 */
fun usableStrokeInsideCeiling(
    beam: TwoSpringElastica,
    count: Int,
    ceiling: Double,
    maximumStroke: Double,
    samples: Int = 90
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(ceiling > 0.0) { "ceiling must be positive, was: $ceiling" }
    require(maximumStroke > 0.0) { "maximumStroke must be positive, was: $maximumStroke" }
    require(samples >= 8) { "samples must be at least 8, was: $samples" }
    val top = min(0.9 * beam.length, maximumStroke)
    fun excess(stroke: Double): Double = count * beam.tangentStiffness(stroke) - ceiling
    var lastGood = 1.0e-3
    var atLastGood = excess(lastGood)
    if (atLastGood >= 0.0) return 0.0
    for (i in 1..samples) {
        val stroke = top * i / samples
        val here = runCatching { excess(stroke) }.getOrNull() ?: return lastGood
        if (here >= 0.0) return illinoisRoot(lastGood, stroke, atLastGood, here) { excess(it) }
        lastGood = stroke
        atLastGood = here
    }
    return top
}

// ------------------------------------------------------------------ placement, widened

/**
 * The arm in nm at which [count] two-spring elasticas present [targetStiffness] as a **secant** at
 * [workingDisplacement] — `C-0039`'s [elasticaArmForStiffness] with the search floor lowered from
 * `1.5 δ` to `[floorFactor] δ`.
 *
 * The wider floor is needed and only needed by the `P10` reading, which places at §3's **desired**
 * 10 nm stroke: `C-0039`'s floor is 15 nm there, and an arm between the stroke and 15 nm is
 * physically admissible (it is a rotation past 41°, not an impossibility). **Above `1.5 δ` the two
 * agree to the last digit**, which is asserted as a gate-5 test rather than assumed.
 *
 * Throws when the array is softer than [targetStiffness] even at the shortest admissible arm —
 * which is a *statement*, not a failure: it says the placement cannot be discharged at that path
 * count at all.
 */
fun elasticaPlacement(
    hingeStiffness: Double,
    hingeCount: Int,
    farStiffness: Double,
    bendingRigidity: Double,
    count: Int,
    targetStiffness: Double,
    workingDisplacement: Double,
    steps: Int = 400,
    maximumArm: Double = 120.0,
    floorFactor: Double = 1.02
): Double {
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingDisplacement > 0.0) {
        "workingDisplacement must be positive, was: $workingDisplacement"
    }
    require(floorFactor > 1.0) { "floorFactor must exceed one, was: $floorFactor" }
    fun assembled(arm: Double): Double = count * TwoSpringElastica(
        bendingRigidity, arm, hingeCount * hingeStiffness, farStiffness, steps
    ).secantStiffness(workingDisplacement)
    val floor = floorFactor * workingDisplacement
    require(assembled(maximumArm) < targetStiffness) {
        "no arm as long as $maximumArm nm is softer than $targetStiffness pN/nm"
    }
    // Walked DOWN from the long end rather than up from a fixed floor: the assembled secant is
    // monotonically decreasing in the arm, but a short arm at a large stroke folds — the
    // elastica's shooting residual stops changing sign — and an evaluation there throws rather
    // than returning a number. Walking down meets the target before it meets the fold whenever a
    // placement exists at all, and reports the fold as a *statement* when it does not.
    var high = maximumArm
    var atHigh = assembled(high) - targetStiffness
    var low = high
    var atLow = atHigh
    var steps2 = 0
    while (atLow < 0.0) {
        high = low
        atHigh = atLow
        low *= 0.98
        require(low > floor) {
            "no arm above $floor nm places $count flexures on $hingeCount crossovers at " +
                    "$targetStiffness pN/nm and a $workingDisplacement nm stroke: the placement " +
                    "cannot be discharged at this path count"
        }
        atLow = runCatching { assembled(low) - targetStiffness }.getOrElse {
            throw IllegalArgumentException(
                "the arm folds at $low nm before reaching $targetStiffness pN/nm: no placeable " +
                        "arm exists for $count flexures on $hingeCount crossovers at a " +
                        "$workingDisplacement nm stroke", it
            )
        }
        steps2++
        require(steps2 < 400) { "the placement walk did not terminate" }
    }
    return illinoisRoot(low, high, atLow, atHigh) { assembled(it) - targetStiffness }
}

// ------------------------------------------------------------------ one point of the trade

/** One `(path count, hinge count)` point, placed and priced. */
data class TradePoint(
    val pathCount: Int,
    val hingeCount: Int,
    val hingeLineLength: Double,
    val crossoverDemand: Int,
    val lineDemand: Double,
    val armLength: Double,
    val armLengthBasePairs: Double,
    val rigidHingeCap: Double,
    val rigidArmBound: Double,
    val secantAtWorking: Double,
    val tangentAtWorking: Double,
    val secantAtTarget: Double,
    val tangentAtTarget: Double,
    val forcePerPathAtWorking: Double,
    val forcePerPathAtTarget: Double,
    val hingeBondForceAtWorking: Double,
    val usableStroke: Double,
    val reachesTargetGeometrically: Boolean,
    val insideCeilingAtWorking: Boolean,
    val insideCeilingOverStroke: Boolean
)

/** The sentinel reported for a quantity a design cannot reach at all. */
const val T99_UNREACHABLE: Double = -1.0

/**
 * Places `pathCount` `E5a` flexures of `hingeCount` crossovers each on `C-0039`'s elastica, and
 * prices them at [workingDisplacement] and at [targetStroke].
 */
fun tradePoint(
    pathCount: Int,
    hingeCount: Int,
    farStiffness: Double,
    targetStiffness: Double,
    workingDisplacement: Double,
    targetStroke: Double,
    hingeStiffness: Double = Gen1Tile.crossoverHingeStiffness(),
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    ceiling: Double = 40.0,
    pitch: Double = perInterfacePitch(),
    steps: Int = 400,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): TradePoint {
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
    val arm = elasticaPlacement(
        hingeStiffness, hingeCount, farStiffness, bendingRigidity, pathCount,
        targetStiffness, workingDisplacement, steps
    )
    val beam = TwoSpringElastica(bendingRigidity, arm, hingeCount * hingeStiffness, farStiffness, steps)
    val reaches = arm > targetStroke
    val secantTarget = if (reaches) pathCount * beam.secantStiffness(targetStroke)
    else T99_UNREACHABLE
    val tangentTarget = if (reaches)
        runCatching { pathCount * beam.tangentStiffness(targetStroke) }.getOrDefault(T99_UNREACHABLE)
    else T99_UNREACHABLE
    val forceTarget = if (reaches) beam.forceForDisplacement(targetStroke) else T99_UNREACHABLE
    val usable = usableStrokeInsideCeiling(beam, pathCount, ceiling, targetStroke)
    val tangentWorking = pathCount * beam.tangentStiffness(workingDisplacement)
    return TradePoint(
        pathCount = pathCount,
        hingeCount = hingeCount,
        hingeLineLength = (hingeCount - 1) * pitch,
        crossoverDemand = hingeCrossoverDemand(pathCount, hingeCount),
        lineDemand = collinearLineDemand(pathCount, hingeCount, pitch),
        armLength = arm,
        armLengthBasePairs = arm / rise,
        rigidHingeCap = runCatching {
            elasticaArmCeiling(
                farStiffness, pathCount, bendingRigidity, targetStiffness, workingDisplacement,
                steps
            )
        }.getOrDefault(T99_UNREACHABLE),
        rigidArmBound = rigidArmCeiling(
            hingeCount * hingeStiffness * pathCount + pathCount * farStiffness,
            targetStiffness, workingDisplacement
        ),
        secantAtWorking = pathCount * beam.secantStiffness(workingDisplacement),
        tangentAtWorking = tangentWorking,
        secantAtTarget = secantTarget,
        tangentAtTarget = tangentTarget,
        forcePerPathAtWorking = beam.forceForDisplacement(workingDisplacement),
        forcePerPathAtTarget = forceTarget,
        hingeBondForceAtWorking = beam.hingeBondForce(workingDisplacement, hingeCount),
        usableStroke = usable,
        reachesTargetGeometrically = reaches,
        insideCeilingAtWorking = tangentWorking <= ceiling,
        insideCeilingOverStroke = usable >= targetStroke - 1.0e-9
    )
}

// ------------------------------------------------------------------ the feasible region

/** Which reading of `C-0023`'s declared compliance ceiling a verdict is taken under (`T-107`). */
enum class CeilingReading {

    /** The ceiling binds only where the actuator works — `n k_tangent(δ_work) ≤ 40 pN/nm`. */
    WORKING_POINT,

    /** The ceiling binds over the whole stroke — `usableStroke ≥ δ_target`. */
    WHOLE_STROKE
}

/** The constraint set a `(path count, hinge count)` point is judged against. */
data class TradeConstraints(
    val inventory: Int,
    val maximumHingeLineCount: Int,
    val lineSupply: Double,
    val unzipAllowable: Double,
    val ceiling: Double,
    val ceilingReading: CeilingReading,
    val targetStroke: Double,
    val stabilityFloor: Double,
    val targetStiffness: Double
)

/**
 * The constraints a [TradePoint] **violates**, named. Empty means the point is feasible.
 *
 * The names are the deliverable: `T-99` asks for the binding constraint at every boundary of the
 * region, and an intersection that reports only a boolean cannot supply one.
 */
fun bindingConstraints(point: TradePoint, constraints: TradeConstraints): List<String> = buildList {
    if (point.crossoverDemand > constraints.inventory) {
        add("crossover inventory (n h = ${point.crossoverDemand} > ${constraints.inventory})")
    }
    if (point.hingeCount > constraints.maximumHingeLineCount) {
        add(
            "hinge-line census (h = ${point.hingeCount} > " +
                    "${constraints.maximumHingeLineCount} on a 40 nm line)"
        )
    }
    if (point.lineDemand > constraints.lineSupply) {
        add("collinear interface line")
    }
    if (point.forcePerPathAtWorking > constraints.unzipAllowable) {
        add("unzip allowable at the working point")
    }
    if (!point.reachesTargetGeometrically) {
        add("geometric reach (the tip cannot rise past its own arm)")
    } else if (point.forcePerPathAtTarget > constraints.unzipAllowable) {
        add("unzip allowable at the target stroke")
    }
    when (constraints.ceilingReading) {
        CeilingReading.WORKING_POINT ->
            if (!point.insideCeilingAtWorking) add("compliance ceiling at the working point")

        CeilingReading.WHOLE_STROKE ->
            if (!point.insideCeilingOverStroke) add("compliance ceiling over the stroke")
    }
    if (constraints.targetStiffness < constraints.stabilityFloor) {
        add("C-0017 stability floor (the placement itself is below |k_eff|)")
    }
}

/** The geometric rotation in radians a rigid arm of [arm] turns through at a stroke of [stroke]. */
fun armRotation(arm: Double, stroke: Double): Double {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(abs(stroke) <= arm) { "an arm of $arm nm cannot reach a stroke of $stroke nm" }
    return asin(stroke / arm)
}

/** `δ = r sin θ` and the restoring lever `r cos θ` — `CH-0040`'s geometry, used as a check. */
fun rigidArmSecant(restraint: Double, arm: Double, stroke: Double): Double {
    val theta = armRotation(arm, stroke)
    return restraint * theta / (arm * arm * sin(theta) * cos(theta))
}
