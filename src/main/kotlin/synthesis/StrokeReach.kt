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

package com.xemantic.nano.plentyofroom.synthesis

import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Task `T-108` — **is §3's desired ~10 nm stroke reachable by any coupling this programme has?**
 * Leaf `A8.2`.
 *
 * ## The cheap bound that decides the answer, and it is not about couplings at all
 *
 * The stroke is `s = L₀ − h`, so **`s < L₀`, identically** — a stroke of 10 nm on a 10 nm layer
 * is the statement `h = 0`, i.e. the polymer crushed to a melt of zero thickness. §3 names three
 * layer heights, 5, 7 and 10 nm, and none of them is above 10.
 *
 * Three ceilings follow, each strictly below the last, and **none of them contains a coupling**:
 *
 * 1. [kinematicStrokeCeiling] `= L₀ − Nσv₀`, the dry-thickness floor — 9.45–9.50 nm at `L₀` = 10;
 * 2. [validityStrokeCeiling] `= L₀ − Nσv₀/φ_max`, `C-0002`'s concentrated crossover at
 *    `φ = 0.2`, which is `C-0018`'s own binding bias ceiling at 10 nm — 7.29–8.37 nm;
 * 3. [deadLoadStroke] at §3's 100 pN, which is the stroke the *device* delivers to a load —
 *    3.50–5.35 nm at `C-0001`'s 10 nm design point.
 *
 * And a coupling can only make it **worse**: `C-0017`'s own gate-2 theorem is that the delivered
 * stroke is monotone decreasing in the coupling stiffness, so the *free* stroke is the supremum
 * over every coupling that could ever be designed. **That is what lets one bound cover a whole
 * catalogue**, and it is why `T-108`'s answer is not a search.
 *
 * ## What is therefore established, and what is not
 *
 * *"Unreachable with this catalogue"* and *"unreachable in physics"* are different statements and
 * only a third one is established here: **unreachable on §3's own stack**. [restingHeightForStroke]
 * prices the escape — the layer height at which the 100 pN dead-load stroke reaches 10 nm — and
 * `C-0001` already recorded the direction: *"the reason to go outside the 5–10 nm range is
 * upward"*.
 */

// ---------------------------------------------------------------- the kinematic ceilings

/**
 * The largest stroke in nm a layer of resting height [restingHeight] can ever deliver: the tile
 * cannot pass the layer's own dry thickness [dryThickness] `= Nσv₀`, below which the volume
 * fraction would exceed one.
 *
 * A geometric identity, not a model result. No bias, no coupling and no origami can move it.
 */
fun kinematicStrokeCeiling(restingHeight: Double, dryThickness: Double): Double {
    require(restingHeight > 0.0) { "restingHeight must be positive, was: $restingHeight" }
    require(dryThickness > 0.0) { "dryThickness must be positive, was: $dryThickness" }
    require(dryThickness < restingHeight) {
        "dryThickness must be below restingHeight, was: $dryThickness against $restingHeight"
    }
    return restingHeight - dryThickness
}

/**
 * The largest stroke in nm that keeps the layer inside `C-0002`'s concentrated crossover:
 * `φ = Nσv₀/h ≤ φ_max`, so `h ≥ Nσv₀/φ_max` and `s ≤ L₀ − Nσv₀/φ_max`.
 *
 * This is the ceiling `C-0018` finds **binding** on the usable bias at `L₀` = 10 nm at every one
 * of `C-0003`'s six models and at every buffer: past it the des Cloizeaux exponent, the salt
 * partitioning and the layer's whole equation of state are extrapolations.
 */
fun validityStrokeCeiling(
    restingHeight: Double,
    dryThickness: Double,
    crossoverVolumeFraction: Double = 0.2
): Double {
    require(restingHeight > 0.0) { "restingHeight must be positive, was: $restingHeight" }
    require(dryThickness > 0.0) { "dryThickness must be positive, was: $dryThickness" }
    require(crossoverVolumeFraction > 0.0 && crossoverVolumeFraction <= 1.0) {
        "crossoverVolumeFraction must be in (0, 1], was: $crossoverVolumeFraction"
    }
    val floor = dryThickness / crossoverVolumeFraction
    require(floor <= restingHeight) {
        "the crossover volume fraction $crossoverVolumeFraction is already exceeded at the " +
                "resting height: $floor against $restingHeight"
    }
    return restingHeight - floor
}

/** How far short a [ceiling] falls of [desiredStroke] — a ratio, quoted as `desired/ceiling`. */
fun strokeShortfall(desiredStroke: Double, ceiling: Double): Double {
    require(desiredStroke > 0.0) { "desiredStroke must be positive, was: $desiredStroke" }
    require(ceiling > 0.0) { "ceiling must be positive, was: $ceiling" }
    return desiredStroke / ceiling
}

// ---------------------------------------------------------------- the dead-load stroke

/**
 * The stroke in nm at which the layer alone carries [targetLoad] pN — `L₀ − h` with `P(h)A = F`.
 *
 * Solved as a **root**, never as a force over a stiffness (`C-0012`): three of `C-0003`'s six
 * models have exactly zero stiffness at `L₀`. Scanned downward from [restingHeight] to
 * [floorHeight] for the first sign change and bisected inside that bracket, exiting on the
 * **bracket width** (`CLAUDE.md`, `C-0031`).
 *
 * If the load is never reached above [floorHeight] the ceiling `L₀ − floorHeight` is returned:
 * the layer is crushed onto its own floor, which is a real boundary rather than a solver failure.
 */
fun deadLoadStroke(
    restingHeight: Double,
    floorHeight: Double,
    targetLoad: Double,
    scanSteps: Int = 512,
    load: (Double) -> Double
): Double {
    require(restingHeight > floorHeight) {
        "restingHeight must exceed floorHeight, was: $restingHeight against $floorHeight"
    }
    require(floorHeight > 0.0) { "floorHeight must be positive, was: $floorHeight" }
    require(targetLoad > 0.0) { "targetLoad must be positive, was: $targetLoad" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    fun residual(height: Double): Double = load(height) - targetLoad
    var high = restingHeight
    var atHigh = residual(high)
    if (atHigh >= 0.0) return 0.0
    val step = (restingHeight - floorHeight) / scanSteps
    for (i in 1..scanSteps) {
        val low = if (i == scanSteps) floorHeight else restingHeight - i * step
        val atLow = residual(low)
        if (atLow >= 0.0) {
            var left = low
            var right = high
            repeat(400) {
                val middle = 0.5 * (left + right)
                if (residual(middle) >= 0.0) left = middle else right = middle
                if (right - left <= 1.0e-15 * right) return restingHeight - 0.5 * (left + right)
            }
            return restingHeight - 0.5 * (left + right)
        }
        high = low
        atHigh = atLow
    }
    return restingHeight - floorHeight
}

/**
 * The resting height in nm at which [strokeAt] delivers [targetStroke] — the layer §3 would have
 * to specify for a stroke it does not currently reach.
 *
 * Bisection on a bracket, exiting on the **bracket width**. [strokeAt] must be increasing in the
 * height, which it is: a taller layer strokes further (`C-0001`'s own gate-2 monotonicity).
 */
fun restingHeightForStroke(
    targetStroke: Double,
    low: Double,
    high: Double,
    strokeAt: (Double) -> Double
): Double {
    require(targetStroke > 0.0) { "targetStroke must be positive, was: $targetStroke" }
    require(high > low && low > 0.0) { "need 0 < low < high, was: $low, $high" }
    require(strokeAt(low) < targetStroke) {
        "the bracket's lower end already delivers the target stroke"
    }
    require(strokeAt(high) > targetStroke) {
        "the bracket's upper end does not deliver the target stroke"
    }
    var left = low
    var right = high
    repeat(200) {
        val middle = 0.5 * (left + right)
        if (strokeAt(middle) < targetStroke) left = middle else right = middle
        if (right - left <= 1.0e-13 * right) return 0.5 * (left + right)
    }
    return 0.5 * (left + right)
}

// ---------------------------------------------------------------- the catalogue predicates

/** The name reported when nothing binds. */
const val NO_BINDING_CONSTRAINT: String = "none — every predicate clears"

/**
 * The eight predicates one catalogue element is judged on at one stroke.
 *
 * Declaration order **is** the tie-break for [bindingConstraint], so it is fixed here and never
 * inferred from a map: `CLAUDE.md`'s rule that an argmin must round and tie-break at the decision
 * point, applied to a set of booleans.
 */
@Serializable
data class ElementPredicates(
    val placesAtMandate: Boolean,
    val insideComplianceCeiling: Boolean,
    val insidePerPathAllowable: Boolean,
    val latticeSupplies: Boolean,
    val packs: Boolean,
    val stableAtTwoMillimolar: Boolean,
    val stableAtHalfMillimolar: Boolean,
    val reachesTheStroke: Boolean
) {

    /** `true` only when every predicate clears. */
    val clears: Boolean
        get() = placesAtMandate && insideComplianceCeiling && insidePerPathAllowable &&
                latticeSupplies && packs && stableAtTwoMillimolar && stableAtHalfMillimolar &&
                reachesTheStroke
}

/**
 * The **first** failing predicate in declaration order, which is what a synthesis table's
 * *"binding constraint"* column has to name.
 *
 * A first-failure rule rather than a worst-margin rule on purpose: the predicates are of
 * different kinds — a count on a lattice, a plan-view packing, a cited allowable, a stiffness —
 * and there is no common axis on which their margins could be compared. `CLAUDE.md`'s
 * *"not every constraint resolves in the axis the window is drawn on"*, in a new place.
 */
fun bindingConstraint(predicates: ElementPredicates): String = when {
    !predicates.placesAtMandate -> "placement (C-0017)"
    !predicates.insideComplianceCeiling -> "compliance ceiling (C-0023, at the placement stroke)"
    !predicates.insidePerPathAllowable -> "per-path unzip allowable (C-0006, CH-0029)"
    !predicates.latticeSupplies -> "hinge inventory (C-0040)"
    !predicates.packs -> "packing (C-0041)"
    !predicates.stableAtTwoMillimolar -> "static stability at 2 mM (C-0017, C-0032)"
    !predicates.stableAtHalfMillimolar -> "static stability at 0.5 mM (C-0017, C-0032)"
    !predicates.reachesTheStroke -> "stroke reach (T-108: s < L0 <= 10 nm)"
    else -> NO_BINDING_CONSTRAINT
}

/**
 * `min_s k_tangent(s)` over `[from, to]` — `CH-0047`'s **fallback convention**, made executable.
 *
 * `CH-0042` prescribed a minimum over `[0, 10 nm]`; `CH-0047` showed that range is not well posed,
 * because the stability requirement is identically zero at `s = 0`, and named `[s_placement,
 * s_desired]` as the reading to use failing a full coupled-fold analysis. That is what this is,
 * and for a row read *at* the placement stroke it degenerates to the tangent there.
 */
fun minimumTangent(
    from: Double,
    to: Double,
    steps: Int = 2048,
    tangent: (Double) -> Double
): Double {
    require(to >= from && from >= 0.0) { "need 0 <= from <= to, was: $from, $to" }
    require(steps >= 2) { "steps must be at least 2, was: $steps" }
    if (to == from) return tangent(from)
    return (0..steps).minOf { tangent(from + (to - from) * it / steps) }
}

/** One row of `T-108`'s catalogue table — one element, read at one stroke. */
@Serializable
@Suppress("LongParameterList")
data class CatalogueRow(
    val element: String,
    val owner: String,
    val readAtStroke: Double,
    val pathCount: Int,
    val elementSpan: Double,
    val secant: Double,
    val tangent: Double,
    val assembledForce: Double,
    val perPathForce: Double,
    val stabilityTangent: Double,
    val lawEvaluable: Boolean,
    val packingAssessed: Boolean,
    val complianceCeiling: Double,
    val perPathSecantCeiling: Double,
    val predicates: ElementPredicates,
    val bindingConstraint: String,
    val note: String
)

/**
 * Builds one [CatalogueRow] from an element's own law and the lattice facts that travel with it.
 *
 * Every mechanical number is supplied by the caller from the element's **own** library, re-run
 * rather than tabulated; every lattice fact ([latticeSupplies], [packs]) is supplied from
 * `C-0040`'s and `C-0041`'s libraries. Nothing here invents a number.
 */
@Suppress("LongParameterList")
fun catalogueRow(
    element: String,
    owner: String,
    readAtStroke: Double,
    pathCount: Int,
    elementSpan: Double,
    secant: Double,
    tangent: Double,
    mandate: Double,
    complianceCeiling: Double,
    unzipAllowable: Double,
    stabilityFloorTwoMillimolar: Double,
    stabilityFloorHalfMillimolar: Double,
    latticeSupplies: Boolean,
    packs: Boolean,
    reachesTheStroke: Boolean,
    stabilityTangent: Double = tangent,
    packingAssessed: Boolean = true,
    placementIsEquality: Boolean = true,
    placementTolerance: Double = 1.0e-6,
    note: String = ""
): CatalogueRow {
    val assembled = secant * readAtStroke
    val perPath = perPathReaction(assembled, pathCount)
    val pathCeiling = perPathSecantCeiling(unzipAllowable, pathCount, readAtStroke)
    val predicates = ElementPredicates(
        placesAtMandate =
            if (placementIsEquality) abs(secant - mandate) <= placementTolerance * mandate
            else secant >= mandate * (1.0 - placementTolerance),
        insideComplianceCeiling = tangent <= complianceCeiling,
        insidePerPathAllowable = perPath <= unzipAllowable,
        latticeSupplies = latticeSupplies,
        packs = packs,
        stableAtTwoMillimolar = stabilityTangent > stabilityFloorTwoMillimolar,
        stableAtHalfMillimolar = stabilityTangent > stabilityFloorHalfMillimolar,
        reachesTheStroke = reachesTheStroke
    )
    return CatalogueRow(
        element = element,
        owner = owner,
        readAtStroke = readAtStroke,
        pathCount = pathCount,
        elementSpan = elementSpan,
        secant = secant,
        tangent = tangent,
        assembledForce = assembled,
        perPathForce = perPath,
        stabilityTangent = stabilityTangent,
        lawEvaluable = true,
        packingAssessed = packingAssessed,
        complianceCeiling = complianceCeiling,
        perPathSecantCeiling = pathCeiling,
        predicates = predicates,
        bindingConstraint = bindingConstraint(predicates),
        note = note
    )
}

/**
 * The row for an element whose law **cannot be evaluated** at the stroke asked for — `C-0039`'s
 * *"the arm folds before reaching the desired stroke"*, which its own solver refuses rather than
 * approximating.
 *
 * Every mechanical field is reported as zero and every predicate as failing, because there is no
 * law to read: a refusal is not a small number.
 */
@Suppress("LongParameterList")
fun infeasibleCatalogueRow(
    element: String,
    owner: String,
    readAtStroke: Double,
    pathCount: Int,
    elementSpan: Double,
    complianceCeiling: Double,
    unzipAllowable: Double,
    reason: String
): CatalogueRow = CatalogueRow(
    element = element,
    owner = owner,
    readAtStroke = readAtStroke,
    pathCount = pathCount,
    elementSpan = elementSpan,
    secant = 0.0,
    tangent = 0.0,
    assembledForce = 0.0,
    perPathForce = 0.0,
    stabilityTangent = 0.0,
    lawEvaluable = false,
    packingAssessed = false,
    complianceCeiling = complianceCeiling,
    perPathSecantCeiling = perPathSecantCeiling(unzipAllowable, pathCount, readAtStroke),
    predicates = ElementPredicates(
        placesAtMandate = false, insideComplianceCeiling = false, insidePerPathAllowable = false,
        latticeSupplies = false, packs = false, stableAtTwoMillimolar = false,
        stableAtHalfMillimolar = false, reachesTheStroke = false
    ),
    bindingConstraint = "the element's own law does not reach this stroke",
    note = reason
)

/** One bound on the stroke, with what it bounds and whether it alone settles §3's desired clause. */
@Serializable
data class ReachBound(
    val name: String,
    val value: Double,
    val shortfall: Double,
    val settlesTheQuestion: Boolean,
    val containsACoupling: Boolean,
    val note: String
)
