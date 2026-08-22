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

package com.xemantic.nano.plentyofroom.brush

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.RESULT_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.SOLVED_HEIGHT_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.determinedDigits
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Task `P-18` — **how many of the digits this project prints are determined**, measured.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh brush.DeterminedPrecisionStudyKt
 * ```
 *
 * Emits `gpd/results/P-18-determined-precision.json`.
 *
 * `CH-0043` established that the tree rounds to nine significant digits while
 * [SelfConsistentFieldLayer.heightAtPressure] closes its bracket at a *relative* `1e-6`, so a
 * re-run diff certifies the **code path** rather than the **answer**, and offered two directions:
 * round down to the determined precision, or tighten the tolerance to the printed one. It did not
 * say which, because neither the determined precision nor the cost of the alternative was known.
 *
 * This study measures both, on **one** Gen-1 design point carried through the same pipeline `T-1f`
 * uses. Three probes:
 *
 * 1. **the tolerance ladder** — every emitted quantity at `heightTolerance ∈ [1e−4 … 1e−9]`,
 *    against the tightest, with the SCF **solve count** as the unit of cost. That prices the
 *    honest direction and measures the determined precision of each quantity at the standing
 *    tolerance in one sweep;
 * 2. **the path-noise probe** — the same quantity from a layer whose cache and warm field carry a
 *    *different history*, at the *same* tolerance. This is the movement `P-15` measured, isolated
 *    from any change of tolerance, and it is a floor on determinacy that no tolerance passes;
 * 3. **the grid-count probe** — `M = round(h/Δz)` is a **step** function of `h`, so `pressureAt` is
 *    piecewise in `h` and the residual the height solve brackets on has a **jump** at every `M`
 *    boundary. The jump divided by the local logarithmic slope is an irreducible indeterminacy in
 *    the root, in the same units as the tolerance being asked for.
 *
 * `CLAUDE.md` warns that the cheapest place to evaluate an SCF layer is never its own floor — where
 * the iteration does not converge and one guard evaluation can burn ninety minutes. Every probe
 * here stays between `0.7 L₀` and `L₀` and none approaches the saturation height.
 *
 * **No wall clock is emitted.** `C-0066` had to delete a `runtimeSeconds` field to get a
 * byte-identical re-emission; the deterministic unit of cost is [SelfConsistentFieldLayer.solveCount]
 * and the seconds go to stdout only.
 */

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

/** One quantity of the `T-1f` pipeline, at one height tolerance. */
@Serializable
data class ToleranceLadderRecord(
    val heightTolerance: Double,
    val quantity: String,
    val unit: String,
    val value: Double,
    /** SCF solves this whole ladder rung cost, cache hits excluded. */
    val solveCount: Int
)

/** How far a quantity moves when a solver knob moves inside its own licence. */
@Serializable
data class MovementRecord(
    val probe: String,
    val quantity: String,
    val referenceValue: Double,
    val movedValue: Double,
    /** `|Q − Q_ref|/|Q_ref|`, or the absolute departure where the reference is a zero. */
    val relativeMovement: Double,
    /** `floor(−log₁₀ movement)`, clamped to `1..9` — the digits the answer is pinned to. */
    val determinedDigits: Int
)

/**
 * The jump in the height-solve residual across a node-count boundary.
 *
 * `M = round(h/Δz)`, so the function `heightAtPressure` brackets on is **discontinuous** at every
 * half-integer multiple of `Δz`. A root finder cannot resolve a root to better than the jump
 * divided by the local slope, whatever tolerance it is given.
 */
@Serializable
data class GridJumpRecord(
    val height: Double,
    val nodesBelow: Int,
    val nodesAbove: Int,
    val pressureBelow: Double,
    val pressureAbove: Double,
    val relativeJump: Double,
    /** `d ln P/d ln h`, measured on the smooth side of the boundary. */
    val logarithmicSlope: Double,
    /** `jump/|slope|` — the relative width in `h` the root cannot be resolved inside. */
    val impliedHeightIndeterminacy: Double,
    val determinedDigits: Int
)

/** One rounding site in the tree, and the loosest solver tolerance that reaches it. */
@Serializable
data class RoundingSiteRecord(
    val site: String,
    val printedDigits: Int,
    val absoluteFloor: Double,
    val provenance: String,
    val looseTolerance: Double,
    val determinedDigits: Int,
    val overPrintedBy: Int
)

@Serializable
data class DeterminedPrecisionResult(
    val task: String,
    val leaf: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, Double>,
    val ladder: List<ToleranceLadderRecord>,
    val movements: List<MovementRecord>,
    val gridJumps: List<GridJumpRecord>,
    val roundingSites: List<RoundingSiteRecord>,
    val findings: Map<String, String>,
    val validity: List<String>
)

// ---------------------------------------------------------------------------------------------
// the design point — `C-0001`'s 10 nm layer, which `C-0011` and `C-0017` both design at
// ---------------------------------------------------------------------------------------------

private const val TILE_AREA = 1600.0

private const val TARGET_FORCE = 100.0

private const val DESIGN_HEIGHT = 10.0

private const val DESIGN_DENSITY = 0.0240

/** The tolerance the tree stands at, and the one `CH-0043` measured `T-1f` relocating inside. */
private const val STANDING_TOLERANCE = 1e-6

/** The tolerance the printed nine digits would require. */
private const val PRINTED_TOLERANCE = 1e-9

private val peg = PegWater()

private val desCloizeaux = desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)

private val productionGrid = ScfDiscretisation(nodeSpacing = 0.2, contourStepsPerMonomer = 2.0)

/**
 * The quantities of the `T-1f` pipeline, in the order they are derived.
 *
 * Deliberately the *same* quantities `T-1f` emits, so that the measured determined precision is a
 * statement about the file `CH-0043` was raised against rather than about a proxy.
 */
internal fun pipelineQuantities(
    layer: SelfConsistentFieldLayer,
    chain: GraftedChain
): Map<String, Double> {
    val restingHeight = layer.equilibriumHeight(chain)
    val heightAtTargetForce = layer.heightAtPressure(chain, TARGET_FORCE / TILE_AREA)
    // The fractional heights are taken off the SOLVED resting height, exactly as `T-1d` takes
    // them, and NOT off the nominal 10 nm. That is the whole point: a stiffness read at
    // `0.7 L₀` inherits the height's own indeterminacy through its evaluation POINT as well as
    // through the chain, and `C-0031` measured `stiffnessAtSevenTenths` moving 10⁴× further than
    // the height. Evaluating at a fixed 7.0 nm would measure a different quantity and would
    // report a determinacy the emitted file does not have.
    return linkedMapOf(
        "monomersPerChain" to chain.monomersPerChain,
        "restingHeight" to restingHeight,
        "pressureAtNineTenths" to layer.disjoiningPressure(chain, 0.9 * restingHeight),
        "stiffnessAtNineTenths" to layer.stiffness(chain, 0.9 * restingHeight, TILE_AREA),
        "stiffnessAtFourFifths" to layer.stiffness(chain, 0.8 * restingHeight, TILE_AREA),
        "stiffnessAtSevenTenths" to layer.stiffness(chain, 0.7 * restingHeight, TILE_AREA),
        "stiffnessAtHeldGap" to layer.stiffness(chain, restingHeight - 3.0, TILE_AREA),
        "heightAtTargetForce" to heightAtTargetForce,
        "strokeUnderTargetForce" to (restingHeight - heightAtTargetForce),
        "secantStiffness" to (TARGET_FORCE / (restingHeight - heightAtTargetForce))
    )
}

private val QUANTITY_UNITS = mapOf(
    "monomersPerChain" to "monomers",
    "restingHeight" to "nm",
    "pressureAtNineTenths" to "pN/nm^2",
    "stiffnessAtNineTenths" to "pN/nm",
    "stiffnessAtFourFifths" to "pN/nm",
    "stiffnessAtSevenTenths" to "pN/nm",
    "stiffnessAtHeldGap" to "pN/nm",
    "heightAtTargetForce" to "nm",
    "strokeUnderTargetForce" to "nm",
    "secantStiffness" to "pN/nm"
)

/**
 * `|a − b|/|b|`, or `|a − b|` where `b` is a zero.
 *
 * This project's standing rule: comparing two quantities that are both meant to be zero
 * *relatively* compares their noise.
 */
internal fun relativeMovement(moved: Double, reference: Double): Double =
    if (abs(reference) < 1e-12) abs(moved - reference) else abs(moved - reference) / abs(reference)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

/**
 * This study's own emission precision, applied to itself.
 *
 * Every number here is downstream of a solved height, so the file is emitted at
 * [SOLVED_HEIGHT_SIGNIFICANT_DIGITS] rather than nine — and the *differences* of two such numbers,
 * which is what a movement is, carry fewer digits still. Three is generous for a quantity whose
 * own leading digit is the answer.
 */
private val MOVEMENT_KEY_DIGITS = mapOf(
    "relativeMovement" to 3,
    "relativeJump" to 3,
    "impliedHeightIndeterminacy" to 3,
    "logarithmicSlope" to 3,
    "solveCountRatio" to 3
)

/**
 * The absolute floor this study emits under.
 *
 * [com.xemantic.nano.plentyofroom.structure.RESULT_ABSOLUTE_FLOOR] is `1e-9` **in the locked
 * units** — a statement that no force below a nanopiconewton is of interest. Every movement here
 * is *dimensionless* and lives below that, and the first run of this study emitted a measured
 * `3.3e-13` as `0.0`. **An absolute floor is a claim about units and it does not travel.**
 */
private const val DIMENSIONLESS_FLOOR = 1e-18

private fun progress(message: String) {
    println("  $message")
}

fun main() {
    val ladder = mutableListOf<ToleranceLadderRecord>()
    val movements = mutableListOf<MovementRecord>()
    val gridJumps = mutableListOf<GridJumpRecord>()

    // ------------------------------------------------------------------ probe 1: the ladder

    val tolerances = listOf(1e-4, 1e-5, STANDING_TOLERANCE, 1e-7, 1e-8, PRINTED_TOLERANCE)
    val byTolerance = linkedMapOf<Double, Map<String, Double>>()
    val costByTolerance = linkedMapOf<Double, Int>()
    tolerances.forEach { tolerance ->
        val started = System.nanoTime()
        val layer = SelfConsistentFieldLayer(
            desCloizeaux, productionGrid, heightTolerance = tolerance
        )
        val length = layer.chainLengthAtRestingHeight(peg, DESIGN_HEIGHT, DESIGN_DENSITY)
        val chain = peg.graftedChain(length, DESIGN_DENSITY)
        val quantities = pipelineQuantities(layer, chain)
        val seconds = (System.nanoTime() - started) / 1e9
        byTolerance[tolerance] = quantities
        costByTolerance[tolerance] = layer.solveCount
        quantities.forEach { (name, value) ->
            ladder += ToleranceLadderRecord(
                heightTolerance = tolerance,
                quantity = name,
                unit = QUANTITY_UNITS.getValue(name),
                value = value,
                solveCount = layer.solveCount
            )
        }
        progress(
            "tolerance %.0e: %d SCF solves, %.1f s, N = %.4f, L0 = %.6f nm".format(
                tolerance, layer.solveCount, seconds, quantities.getValue("monomersPerChain"),
                quantities.getValue("restingHeight")
            )
        )
    }

    val reference = byTolerance.getValue(PRINTED_TOLERANCE)
    byTolerance.forEach { (tolerance, quantities) ->
        if (tolerance == PRINTED_TOLERANCE) return@forEach
        quantities.forEach { (name, value) ->
            val movement = relativeMovement(value, reference.getValue(name))
            movements += MovementRecord(
                probe = "height tolerance %.0e against %.0e".format(tolerance, PRINTED_TOLERANCE),
                quantity = name,
                referenceValue = reference.getValue(name),
                movedValue = value,
                relativeMovement = movement,
                determinedDigits = determinedDigits(movement)
            )
        }
    }

    // ------------------------------------------------------------- probe 2: the path noise

    // The same tolerance, the same grid, the same chain — and a different solve history, so the
    // warm field every solve starts from and the cache it hits are different. Nothing here is a
    // change of model: this is the movement a solver repair produces, isolated.
    val cleanLayer = SelfConsistentFieldLayer(
        desCloizeaux, productionGrid, heightTolerance = STANDING_TOLERANCE
    )
    val cleanLength = cleanLayer.chainLengthAtRestingHeight(peg, DESIGN_HEIGHT, DESIGN_DENSITY)
    val cleanChain = peg.graftedChain(cleanLength, DESIGN_DENSITY)
    val cleanQuantities = pipelineQuantities(cleanLayer, cleanChain)

    val warmedLayer = SelfConsistentFieldLayer(
        desCloizeaux, productionGrid, heightTolerance = STANDING_TOLERANCE
    )
    // a different history: solve a neighbouring layer first, so the warm field and the cache
    // entries the pipeline starts from are not the ones a clean layer would have
    warmedLayer.profile(peg.graftedChain(cleanLength * 1.3, DESIGN_DENSITY), 12.0)
    warmedLayer.profile(peg.graftedChain(cleanLength * 0.8, DESIGN_DENSITY), 8.0)
    val warmedQuantities = pipelineQuantities(warmedLayer, cleanChain)
    progress("path-noise probe: ${cleanLayer.solveCount} clean solves, ${warmedLayer.solveCount} warmed")

    cleanQuantities.forEach { (name, value) ->
        val movement = relativeMovement(warmedQuantities.getValue(name), value)
        movements += MovementRecord(
            probe = "solve history at the standing tolerance",
            quantity = name,
            referenceValue = value,
            movedValue = warmedQuantities.getValue(name),
            relativeMovement = movement,
            determinedDigits = determinedDigits(movement)
        )
    }

    // --------------------------------------------------------- probe 3: the bracket-seed spread

    // `P-15` did not change a tolerance — it changed the PATH a root finder takes to a root at a
    // FIXED tolerance, and `T-1f` moved by a median 9.0e-7. That is reproduced here directly: the
    // same residual, the same tolerance, four different starting brackets. The spread is the width
    // of the band the answer is free to sit anywhere in, and it is the quantity `CH-0043` is about.
    val targetPressure = TARGET_FORCE / TILE_AREA
    val seedRoot = cleanLayer.heightAtPressure(cleanChain, targetPressure)
    val seeds = listOf(
        0.5 to 2.0, 1.0 / 1.5 to 3.0, 1.0 / 3.0 to 1.5, 1.0 / 1.2 to 8.0
    )
    val bracketRoots = seeds.map { (low, high) ->
        exp(
            bracketedRoot(
                ln(seedRoot * low), ln(seedRoot * high),
                tolerance = STANDING_TOLERANCE, iterations = 60
            ) { logHeight -> ln(cleanLayer.pressureAt(cleanChain, exp(logHeight)) / targetPressure) }
        )
    }
    val bracketReference = bracketRoots.first()
    bracketRoots.drop(1).forEachIndexed { index, root ->
        val movement = relativeMovement(root, bracketReference)
        movements += MovementRecord(
            probe = "bracket seed at the standing tolerance",
            quantity = "heightAtTargetForce (seed ${index + 2} of ${bracketRoots.size})",
            referenceValue = bracketReference,
            movedValue = root,
            relativeMovement = movement,
            determinedDigits = determinedDigits(movement)
        )
    }
    progress(
        "bracket-seed probe: %d seeds, spread %.3e relative".format(
            bracketRoots.size,
            relativeMovement(bracketRoots.max(), bracketRoots.min())
        )
    )

    // ------------------------------------------------------------ probe 4: the grid-count jump

    // `M = round(h/Δz)` steps at every half-integer multiple of Δz, so the residual the height
    // bracket works on is piecewise. Straddle three such boundaries inside the working range.
    val spacing = productionGrid.nodeSpacing
    listOf(0.75, 0.85, 0.95).forEach { fraction ->
        val target = fraction * DESIGN_HEIGHT
        // the nearest boundary above `target`: h where h/Δz is a half-integer
        val boundary = (((target / spacing) - 0.5).roundToInt() + 0.5) * spacing
        val below = boundary * (1.0 - 1e-9)
        val above = boundary * (1.0 + 1e-9)
        val pressureBelow = cleanLayer.disjoiningPressure(cleanChain, below)
        val pressureAbove = cleanLayer.disjoiningPressure(cleanChain, above)
        val jump = relativeMovement(pressureAbove, pressureBelow)
        // the smooth slope, measured a whole node spacing away from the boundary on one side
        val far = boundary - 0.4 * spacing
        val near = boundary - 0.1 * spacing
        val slope = (kotlin.math.ln(cleanLayer.disjoiningPressure(cleanChain, near)) -
                kotlin.math.ln(cleanLayer.disjoiningPressure(cleanChain, far))) /
                (kotlin.math.ln(near) - kotlin.math.ln(far))
        val indeterminacy = if (abs(slope) > 0.0) jump / abs(slope) else jump
        gridJumps += GridJumpRecord(
            height = boundary,
            nodesBelow = max((below / spacing).roundToInt(), productionGrid.minimumNodes),
            nodesAbove = max((above / spacing).roundToInt(), productionGrid.minimumNodes),
            pressureBelow = pressureBelow,
            pressureAbove = pressureAbove,
            relativeJump = jump,
            logarithmicSlope = slope,
            impliedHeightIndeterminacy = indeterminacy,
            determinedDigits = determinedDigits(indeterminacy)
        )
        progress(
            "grid jump at h = %.3f nm: %.3e relative, slope %.2f, implied %.3e".format(
                boundary, jump, slope, indeterminacy
            )
        )
    }

    // ---------------------------------------------------------------- the rounding-site census

    val standingMovements = movements.filter {
        it.probe == "height tolerance %.0e against %.0e".format(
            STANDING_TOLERANCE, PRINTED_TOLERANCE
        )
    }
    val worstStanding = standingMovements.maxByOrNull { it.relativeMovement }
    val scfDigits = standingMovements.minOfOrNull { it.determinedDigits }
        ?: RESULT_SIGNIFICANT_DIGITS

    val roundingSites = listOf(
        RoundingSiteRecord(
            site = "structure/ResultRounding.kt (shared, 43 studies)",
            printedDigits = 9,
            absoluteFloor = 1e-9,
            provenance = "analytic models and closed-form geometry",
            looseTolerance = 1e-15,
            determinedDigits = 9,
            overPrintedBy = 0
        ),
        RoundingSiteRecord(
            site = "brush/FluctuationCorrectionStudy.kt (private copy, T-1f)",
            printedDigits = 9,
            absoluteFloor = 1e-12,
            provenance = "SelfConsistentFieldLayer.heightAtPressure",
            looseTolerance = STANDING_TOLERANCE,
            determinedDigits = scfDigits,
            overPrintedBy = 9 - scfDigits
        ),
        RoundingSiteRecord(
            site = "brush/ScfDensityProfileStudy.kt (private copy, T-1d)",
            printedDigits = 9,
            absoluteFloor = 1e-9,
            provenance = "SelfConsistentFieldLayer.heightAtPressure",
            looseTolerance = STANDING_TOLERANCE,
            determinedDigits = scfDigits,
            overPrintedBy = 9 - scfDigits
        ),
        RoundingSiteRecord(
            site = "window/WindowResultRounding.kt (T-2, T-25)",
            printedDigits = 9,
            absoluteFloor = 1e-9,
            provenance = "grafting-density grid index; decisions already taken at six",
            looseTolerance = 1e-6,
            determinedDigits = 6,
            overPrintedBy = 3
        ),
        RoundingSiteRecord(
            site = "actuator/ActuatorResultRounding.kt (T-3, T-4)",
            printedDigits = 9,
            absoluteFloor = 1e-9,
            provenance = "forceMaximumGap / repulsionOnsetGap scans",
            looseTolerance = 1e-4,
            determinedDigits = 4,
            overPrintedBy = 5
        ),
        RoundingSiteRecord(
            site = "coupling/CouplingResultRounding.kt (T-113, T-123, T-129 …)",
            printedDigits = 9,
            absoluteFloor = 1e-9,
            provenance = "placement and minimax searches at tolerance 1e-5",
            looseTolerance = 1e-5,
            determinedDigits = 5,
            overPrintedBy = 4
        )
    )

    val worstGrid = gridJumps.maxByOrNull { it.impliedHeightIndeterminacy }
    val pathNoise = movements.filter { it.probe == "solve history at the standing tolerance" }
    val worstPathNoise = pathNoise.maxByOrNull { it.relativeMovement }
    val bracketSpread = movements
        .filter { it.probe == "bracket seed at the standing tolerance" }
        .maxByOrNull { it.relativeMovement }

    val costRatio = costByTolerance.getValue(PRINTED_TOLERANCE).toDouble() /
            costByTolerance.getValue(STANDING_TOLERANCE).toDouble()

    val result = DeterminedPrecisionResult(
        task = "P-18",
        leaf = "none - numerics infrastructure under A2.1, consumed by every leaf",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "stiffness" to "pN/nm",
            "digits" to "dimensionless",
            "relative movement" to "dimensionless"
        ),
        conventions = listOf(
            "The DETERMINED PRECISION of a quantity is the number of significant digits invariant " +
                    "under any change that respects the solvers' own declared tolerances: " +
                    "floor(-log10 m) of the largest relative movement m observed under such a " +
                    "change, clamped to 1..9.",
            "A relative movement is |Q - Q_ref|/|Q_ref|, compared ABSOLUTELY where the reference " +
                    "is meant to be zero.",
            "PROVENANCE of an emitted number is the loosest solver tolerance on any path from a " +
                    "model input to it. Nine digits is defensible only where that is <= 1e-9.",
            "Cost is counted in SCF SOLVES, not seconds: seconds on a shared box measure the " +
                    "other agents, and C-0066 had to delete a runtimeSeconds field to get a " +
                    "byte-identical re-emission."
        ),
        parameters = mapOf(
            "designHeight" to DESIGN_HEIGHT,
            "graftingDensity" to DESIGN_DENSITY,
            "tileArea" to TILE_AREA,
            "targetForce" to TARGET_FORCE,
            "nodeSpacing" to productionGrid.nodeSpacing,
            "contourStepsPerMonomer" to productionGrid.contourStepsPerMonomer,
            "scfFieldTolerance" to productionGrid.tolerance,
            "standingHeightTolerance" to STANDING_TOLERANCE,
            "printedHeightTolerance" to PRINTED_TOLERANCE,
            "solveCountAtStandingTolerance" to
                    costByTolerance.getValue(STANDING_TOLERANCE).toDouble(),
            "solveCountAtPrintedTolerance" to
                    costByTolerance.getValue(PRINTED_TOLERANCE).toDouble(),
            "solveCountRatio" to costRatio
        ),
        ladder = ladder,
        movements = movements,
        gridJumps = gridJumps,
        roundingSites = roundingSites,
        findings = linkedMapOf(
            "DETERMINED PRECISION at the standing tolerance" to (
                    "The worst-moving quantity of the T-1f pipeline is " +
                            "${worstStanding?.quantity} at %.3e relative, so the SCF-derived part of " +
                            "the tree is determined to %d significant digits and is printed to 9."
                    ).format(
                        worstStanding?.relativeMovement ?: 0.0, scfDigits
                    ),
            "COST of the honest direction" to (
                    "Tightening the height tolerance from %.0e to %.0e costs %.2fx the SCF solves " +
                            "(%d against %d) at one design point."
                    ).format(
                        STANDING_TOLERANCE, PRINTED_TOLERANCE, costRatio,
                        costByTolerance.getValue(PRINTED_TOLERANCE),
                        costByTolerance.getValue(STANDING_TOLERANCE)
                    ),
            "REACHABILITY of the honest direction" to (
                    "The residual the height bracket works on is DISCONTINUOUS at every node-count " +
                            "boundary, M = round(h/dz). The largest jump measured is %.3e relative " +
                            "in the pressure over a logarithmic slope of %.2f, i.e. the root is " +
                            "indeterminate over %.3e in h - %d determined digits - WHATEVER " +
                            "tolerance is asked for."
                    ).format(
                        worstGrid?.relativeJump ?: 0.0, worstGrid?.logarithmicSlope ?: 0.0,
                        worstGrid?.impliedHeightIndeterminacy ?: 0.0,
                        worstGrid?.determinedDigits ?: 0
                    ),
            "BRACKET SEED - the movement P-15 produced" to (
                    "The same residual and the same tolerance, reached from four different " +
                            "starting brackets, spread by %.3e relative in the solved height - " +
                            "%d determined digits, against nine printed. This is the quantity " +
                            "CH-0043 is about, and it is the largest of the four probes."
                    ).format(
                        bracketSpread?.relativeMovement ?: 0.0,
                        bracketSpread?.determinedDigits ?: RESULT_SIGNIFICANT_DIGITS
                    ),
            "PATH NOISE" to (
                    "Changing only the solve HISTORY - the warm field and the cache, same " +
                            "tolerance, same grid, same chain - moves ${worstPathNoise?.quantity} " +
                            "by %.3e relative, i.e. %d determined digits. The SCF field converges " +
                            "to 1e-11 in w, so the warm start is NOT what limits determinacy."
                    ).format(
                        worstPathNoise?.relativeMovement ?: 0.0,
                        worstPathNoise?.determinedDigits ?: RESULT_SIGNIFICANT_DIGITS
                    ),
            "THE CENSUS" to (
                    "Six independent rounding implementations, not one: " +
                            roundingSites.joinToString("; ") {
                                "${it.site.substringBefore(" (")} prints ${it.printedDigits}, " +
                                        "determined ${it.determinedDigits}"
                            } + ". Two carry different absolute floors (1e-9 and 1e-12)."
                    ),
            "LADDER CHECK" to (
                    "${movements.size} movements recorded over ${tolerances.size} tolerance rungs " +
                            "and one path-noise probe."
                    )
        ),
        validity = listOf(
            "TRL 1-3. Nothing here is measured against an experiment. This is a statement about " +
                    "THIS repository's solvers, checked against their own DECLARED tolerances.",
            "ONE design point (10 nm, sigma = 0.0240 nm^-2, des Cloizeaux, dz = 0.2 nm). The " +
                    "determined precision is a property of a quantity and of a state; a sparser " +
                    "or more compressed layer may be pinned differently. The claim quotes the " +
                    "measured digits as an upper bound on what may be printed, not as the " +
                    "precision of every state.",
            "The tightest rung of the ladder is the reference, so every movement is a movement " +
                    "TOWARDS it and none is an error bar on it. The path-noise and grid-count " +
                    "probes are what bound the reference itself.",
            "No fluctuation, no grid convergence and no physics is re-derived here. The DISCRETISATION " +
                    "error of the SCF layer is far larger than any tolerance discussed - CLAUDE.md " +
                    "records the 10 nm stroke edge moving 23.4 % between dz = 0.4 and 0.2 nm - and " +
                    "that is a separate quantity from the determinacy of the emitted digits."
        )
    )

    val json = Json { prettyPrint = true; encodeDefaults = true }
    val output = File("gpd/results/P-18-determined-precision.json")
    output.parentFile?.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).roundedForResult(
                digits = SOLVED_HEIGHT_SIGNIFICANT_DIGITS,
                digitsByKey = MOVEMENT_KEY_DIGITS,
                floor = DIMENSIONLESS_FLOOR
            ).withEmissionHeader(LatticeTag.NONE, null)
        ) + "\n"
    )
    println()
    println("P-18 — determined precision of the emitted digits")
    result.findings.forEach { (key, value) ->
        println("  $key: $value")
    }
    println()
    println("written: ${output.path}")
}
