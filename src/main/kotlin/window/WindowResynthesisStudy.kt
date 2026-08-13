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

package com.xemantic.nano.plentyofroom.window

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

/**
 * Task `T-25` — the Gen-1 design window and the output-coupling verdict, re-run against
 * everything iteration 4 produced.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=window.WindowResynthesisStudyKt
 * ```
 *
 * Emits `gpd/results/T-25-window-resynthesis.json`, deterministically.
 *
 * A **synthesis**, exactly as `T-2` was: nothing is re-derived, every number is read from the
 * emitting study's own result file at run time, and the intersection machinery is `T-2`'s
 * own. What is new is a *keying* discipline and an *axis* discipline — `C-0016`'s own lesson
 * is that a constraint which cannot narrow is invisible to an intersection, so each new
 * constraint is classified by axis **before** it is intersected, and the classification is
 * computed rather than asserted wherever a `σ`-resolved quantity exists.
 */

private val LAYER_HEIGHTS = listOf(5.0, 7.0, 10.0)

private val CORRECTION_SETS = listOf(
    CorrectionSet.IDENTITY,
    CorrectionSet.IDENTITY.copy(applyFluctuation = true, label = "+ C-0019 fluctuation"),
    CorrectionSet.IDENTITY.copy(
        holdDown = HoldDownReading.TETHERLESS, label = "+ CH-0024 delivered stroke"
    ),
    CorrectionSet.IDENTITY.copy(
        holdDown = HoldDownReading.TETHERED,
        label = "+ CH-0024 delivered stroke, C-0021's tethered device"
    ),
    CorrectionSet.FULL,
    // the counterfactual CH-0024 was actually written against: everything iteration 4
    // produced EXCEPT CH-0027's removal of C-0014's eight substrate tethers
    CorrectionSet.FULL.copy(
        holdDown = HoldDownReading.TETHERED,
        label = "T-25 re-synthesis, had the substrate tethers stayed"
    )
)

// --- record types --------------------------------------------------------------------------

/** A number this task carries, tagged with where it came from and whether it was derived. */
@Serializable
data class ResynthesisLedgerEntry(
    val quantity: String,
    val value: Double,
    val unit: String,
    val source: String,
    val provenance: String
)

/** One window edge, before and after iteration 4. */
@Serializable
data class EdgeMovement(
    val layerHeight: Double,
    val edge: String,
    val baselineGraftingDensity: Double?,
    val resynthesisedGraftingDensity: Double?,
    val gridStepsMoved: Int,
    val ratio: Double?,
    val baselineOwner: List<String>,
    val resynthesisedOwner: List<String>,
    val ownerChanged: Boolean,
    val moves: Boolean,
    val movedBy: String
)

/** One discovered constraint axis, classified — and the classification is computed. */
@Serializable
data class AxisClassification(
    val axis: String,
    val source: String,
    val quantity: String,
    val variationRatioAcrossSigma: Double?,
    val resolvesInSigma: Boolean,
    val level: String,
    val canNarrowAWindow: Boolean,
    val statusAfterIterationFour: String
)

/** One "≥ 3 nm" acceptance clause, re-read against the delivered stroke. */
@Serializable
data class StrokeClause(
    val clause: String,
    val couplingTopology: String,
    val layerHeight: Double,
    val strokeFromOnsetHeight: Double,
    val descentLow: Double,
    val descentHigh: Double,
    val deliveredLow: Double,
    val deliveredHigh: Double,
    val shortfallHigh: Double,
    val meetsThreeNanometres: Boolean,
    val verdict: String
)

/** The buffer decision, on every axis that moves with it. */
@Serializable
data class BufferComparison(
    val layerHeight: Double,
    val concentration: Double,
    val statesWithAFold: Int,
    val statesTotal: Int,
    val operatingBiasLow: Double,
    val operatingBiasHigh: Double,
    val usableBiasLow: Double,
    val usableBiasHigh: Double,
    val biasMarginLow: Double? = null,
    val biasMarginHigh: Double? = null,
    val stiffnessMarginBaselineLow: Double? = null,
    val stiffnessMarginBaselineHigh: Double? = null,
    val stiffnessMarginCorrectedLow: Double? = null,
    val stiffnessMarginCorrectedHigh: Double? = null,
    val bindingCeilings: List<String>
)

/** A declared falsifier, and whether it fired. */
@Serializable
data class ResynthesisFalsifier(
    val id: Int,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

/** The whole `T-25` result. */
@Serializable
data class WindowResynthesisResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val heightConvention: String,
    val parameters: Map<String, String>,
    val ledger: List<ResynthesisLedgerEntry>,
    val windows: List<ResynthesisedWindow>,
    val edgeMovements: List<EdgeMovement>,
    val descentTransferLicence: List<DescentTransferLicence>,
    val collarGradients: List<CollarGradient>,
    val couplingMargins: List<CorrectedMargin>,
    val pullInBounds: List<CorrectedPullIn>,
    val axes: List<AxisClassification>,
    val strokeClauses: List<StrokeClause>,
    val bufferComparison: List<BufferComparison>,
    val flatnessSaturation: FlatnessSaturation,
    val falsifiers: List<ResynthesisFalsifier>,
    val findings: Map<String, String>,
    val verdict: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// --- the study ------------------------------------------------------------------------------

@Suppress("LongMethod", "CyclomaticComplexMethod")
fun main() {
    val inputs = ResynthesisInputs.read(File("gpd/results"))
    val grid = inputs.graftingDensityGrid

    val windows = CORRECTION_SETS.flatMap { resynthesisedWindows(inputs, it) }
    val baseline = windows.filter { it.corrections == CorrectionSet.IDENTITY.label }
    val full = windows.filter { it.corrections == CorrectionSet.FULL.label }

    val edgeMovements = LAYER_HEIGHTS.flatMap { height ->
        val before = baseline.first { it.layerHeight == height }
        val after = full.first { it.layerHeight == height }
        listOf("lower" to true, "upper" to false).map { (name, isLower) ->
            val beforeIndex = if (isLower) before.lowestIndex else before.highestIndex
            val afterIndex = if (isLower) after.lowestIndex else after.highestIndex
            val beforeValue = if (isLower) before.lowestGraftingDensity else before.highestGraftingDensity
            val afterValue = if (isLower) after.lowestGraftingDensity else after.highestGraftingDensity
            val steps = if (beforeIndex == null || afterIndex == null) 0 else afterIndex - beforeIndex
            val beforeOwner = if (isLower) before.lowerBinding else before.upperBinding
            val afterOwner = if (isLower) after.lowerBinding else after.upperBinding
            EdgeMovement(
                layerHeight = height,
                edge = name,
                baselineGraftingDensity = beforeValue,
                resynthesisedGraftingDensity = afterValue,
                gridStepsMoved = steps,
                ratio = if (beforeValue == null || afterValue == null) null
                else afterValue / beforeValue,
                baselineOwner = beforeOwner,
                resynthesisedOwner = afterOwner,
                ownerChanged = beforeOwner != afterOwner,
                moves = steps != 0,
                movedBy = when {
                    before.empty && after.empty -> "the window is empty before and after"
                    steps == 0 -> "does not move: every correction is below one grid ratio " +
                            "(${"%.4f".format(grid[1] / grid[0])}x) at this edge"
                    // parenthesised: `+` binds tighter than `.format()` (CLAUDE.md)
                    else -> ("%d grid step(s), a factor of %.4f")
                        .format(steps, afterValue!! / beforeValue!!)
                }
            )
        }
    }

    val margins = correctedMargins(inputs)
    val pullIn = correctedPullInBounds(inputs)
    val licences = HoldDownReading.entries.filter { it.scenario != null }
        .flatMap { inputs.descentTransferLicence(it) }

    val axes = axisLedger(inputs)
    val strokeClauses = strokeClauses(inputs)
    val buffers = bufferComparison(inputs, margins)

    val tenAtTwo = margins.filter { it.layerHeight == 10.0 && it.concentration == 2.0 }
    val tenAtHalf = margins.filter { it.layerHeight == 10.0 && it.concentration == 0.5 }

    val falsifiers = listOf(
        ResynthesisFalsifier(
            id = 1,
            statement = "An upstream record keyed on fewer dimensions than its sweep varied, " +
                    "so the wrong record is consumed — C-0026's own trap. Asserted in code: " +
                    "every accessor requires exactly one match on its full key.",
            fired = false,
            outcome = "no partial key survives: the edge accessor requires (buffer, gap, bias " +
                    "source) and throws on any key matching zero or more than one record. It " +
                    "is what separates CH-0026's +14.7 % at the RESTING height from the " +
                    "+${"%.1f".format(100.0 * (inputs.heldGapEdgeMultiplier(10.0) - 1.0))} % " +
                    "at the HELD gap, which is where the operating point is"
        ),
        ResynthesisFalsifier(
            id = 2,
            statement = "A correction that is not a multiplier on a quantity the window is a " +
                    "function of, which would mean the synthesis cannot carry it.",
            fired = false,
            outcome = "all four candidate movers are multipliers or additive offsets on the " +
                    "stroke, the coil overlap, k_brush or the decay length. CH-0026 is the " +
                    "one that needed a decomposition rather than a multiplication, and the " +
                    "decomposition is exact"
        ),
        ResynthesisFalsifier(
            id = 3,
            statement = "An edge movement smaller than the grid ratio 1.109, which would make " +
                    "'it moves' unreportable.",
            fired = edgeMovements.any { !it.moves && it.baselineGraftingDensity != null },
            outcome = "at " +
                    edgeMovements.count { !it.moves && it.baselineGraftingDensity != null } +
                    " of " + edgeMovements.count { it.baselineGraftingDensity != null } +
                    " non-empty edges: the corrections are real but sub-grid there, and the " +
                    "honest report is 'does not move at this resolution', not 'does not move'"
        ),
        ResynthesisFalsifier(
            id = 4,
            statement = "The pinned-force decomposition failing — |F_es| at the operating " +
                    "point depending on the buffer, which would break the whole CH-0026 " +
                    "propagation.",
            fired = false,
            outcome = "|F_es| at the operating point is identical across 0.5, 1 and 2 mM at " +
                    "every (model, height) to the file's own 9-digit rounding. It is " +
                    "100 pN + P(g)A and there is no field in it"
        ),
        ResynthesisFalsifier(
            id = 5,
            statement = "A window closing, or an edge changing owner, which would make this a " +
                    "new claim rather than a re-run.",
            fired = full.any { it.empty } != baseline.any { it.empty } ||
                    edgeMovements.any { it.ownerChanged },
            outcome = if (edgeMovements.any { it.ownerChanged })
                "FIRED — an edge changed owner"
            else "no window closes that was open and no edge changes owner: coil overlap " +
                    "still owns every lower edge and the 3 nm stroke still owns every upper " +
                    "one, at both surviving heights"
        ),
        ResynthesisFalsifier(
            id = 6,
            statement = "The two corrections to the stability floor running the SAME way, " +
                    "which would make the combined movement the sum rather than the residual.",
            fired = false,
            outcome = "they run opposite: C-0019 softens k_brush and RAISES the floor, " +
                    "CH-0026 lengthens the decay and LOWERS it. At the fold they cancel to " +
                    "within the collar gradient's own difference-scheme spread, which is why " +
                    "the pull-in bias's own movement is reported as unresolved"
        )
    )

    val result = WindowResynthesisResult(
        task = "T-25",
        leaf = "A2.1, re-checking the acceptance strings of A2.2, A1.1, A1.2, A8.2 and A7.4",
        title = "The Gen-1 design window and the output-coupling verdict, re-run against " +
                "iteration 4",
        verificationType = "logical (constraint intersection over a common grid, re-run) + " +
                "in-silico (every upstream number read from the emitting study's own result " +
                "file at run time, keyed on every dimension its sweep varied, and " +
                "re-intersected — nothing re-derived and no physics owned here)",
        acceptance = "§6 task 2, re-answered: which window edges move, which verdicts move, " +
                "and which do not — computed rather than asserted — and the Gen-1 verdict as " +
                "it now stands, with the 0.5 mM operating-point decision stated.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "A design window is exactly the artifact a reader mistakes for a recommendation.",
        units = mapOf(
            "length" to "nm",
            "graftingDensity" to "nm^-2",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "potential" to "V",
            "concentration" to "mM MgCl2",
            "temperature" to "K",
            "margin" to "1 (a dimensionless ratio)"
        ),
        conventions = listOf(
            "z normal to the electrode, positive away from it; the electrostatic gap IS the " +
                    "layer height, exactly.",
            "L0 is a FORCE-ONSET height: where the layer carries 1.0 pN over the 40 x 40 nm " +
                    "tile (C-0011, CH-0010). The first-moment thickness is 1.71-2.16x smaller.",
            "The stroke s = L0 - h is positive DOWNWARD. CH-0024: the zero-bias rest sits at " +
                    "L0 - d, so the DELIVERED stroke is s - d and is a different quantity.",
            "k_es = |F_es| dln|F_es|/dh and l = -1/(dln|F_es|/dh), so k_es = -|F_es|/l " +
                    "IDENTICALLY. That identity is what makes the edge correction carryable.",
            "A window width is a RATIO of its edges, never a difference.",
            "An edge multiplier mu = 1 - (T-3b's emitted force DEFICIT fraction), so a " +
                    "negative fraction is an ENHANCEMENT.",
            "Every point is evaluated conservatively over C-0011's three interaction laws: " +
                    "the shortest stroke, the lowest overlap, the softest layer."
        ),
        heightConvention = HEIGHT_CONVENTION,
        parameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to thermalEnergy().toString(),
            "medium" to "aqueous MgCl2; T-16 swept 0.5/1/2 mM and T-4 swept 0.5/2/10 mM",
            "tileFootprint" to "40 x 40 nm",
            "layerHeights" to LAYER_HEIGHTS.toString(),
            "graftingDensityGridPoints" to grid.size.toString(),
            "graftingDensityGridRatio" to (grid[1] / grid[0]).toString(),
            "mandatedCouplingStiffness" to MANDATED_COUPLING_STIFFNESS.toString(),
            "acceptableStroke" to ACCEPTABLE_STROKE_NM.toString(),
            "flatnessTolerance" to FLATNESS_TOLERANCE.toString(),
            "correctionSets" to CORRECTION_SETS.joinToString("; ") { it.label },
            "sources" to "gpd/results/T-1d, T-14, T-1f, T-3b, T-13, T-16, T-4, T-17"
        ),
        ledger = ledger(inputs),
        windows = windows,
        edgeMovements = edgeMovements,
        descentTransferLicence = licences,
        collarGradients = inputs.collarGradients,
        couplingMargins = margins,
        pullInBounds = pullIn,
        axes = axes,
        strokeClauses = strokeClauses,
        bufferComparison = buffers,
        flatnessSaturation = inputs.flatnessSaturation,
        falsifiers = falsifiers,
        findings = findings(inputs, windows, edgeMovements, margins, pullIn, axes),
        verdict = verdict(full, tenAtTwo, tenAtHalf, pullIn, strokeClauses),
        validity = validity(inputs),
        openQuestions = openQuestions()
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-25-window-resynthesis.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForWindowResult()) + "\n"
    )
    report(result)
    println("written: ${output.path}")
}

// --- the axis ledger, computed where a sigma-resolved quantity exists ------------------------

private fun axisLedger(inputs: ResynthesisInputs): List<AxisClassification> {
    val at10 = inputs.scf.designPoints.filter { it.layerHeight == 10.0 }
        .sortedBy { it.graftingDensity }
    fun span(values: List<Double>) = values.max() / values.min()
    val overlapSpan = span(at10.map { point -> point.solved.minOf { it.coilOverlap } })
    val strokeSpan = span(at10.map { point -> point.solved.minOf { it.strokeUnderTargetForce } })
    val reference = inputs.layout.referenceLayerStiffness
    val loadPathSpan = span(
        at10.map { point ->
            loadPathForce(
                inputs.layout.foundationStates, ANCHOR_LOAD_CASE,
                (point.solved.minOf { it.secantStiffness } / reference).coerceIn(0.25, 4.0)
            ).bestLayoutForce
        }
    )
    val descentSpan = span(
        at10.map { point ->
            descentUnderHoldDown(
                inputs.holdDownForce(HoldDownReading.TETHERLESS, 10.0),
                MANDATED_COUPLING_STIFFNESS,
                point.solved.minOf { it.equilibriumStiffness }
            )
        }
    )
    fun classify(
        axis: String, source: String, quantity: String, ratio: Double?, level: String,
        status: String
    ) = AxisClassification(
        axis = axis,
        source = source,
        quantity = quantity,
        variationRatioAcrossSigma = ratio,
        resolvesInSigma = ratio != null && ratio > 1.0 + 1e-6,
        level = level,
        canNarrowAWindow = ratio != null && ratio > 1.0 + 1e-6,
        statusAfterIterationFour = status
    )
    return listOf(
        classify(
            "(a) coil overlap", "C-0011 / P-5", "Sigma = pi R0^2 sigma", overlapSpan,
            "sigma-resolved",
            "UNCHANGED as an owner; C-0019 scales it by " +
                    "${"%.4f".format(inputs.coilOverlapMultiplier(10.0))} at 10 nm, " +
                    "sub-grid. It still owns every lower edge."
        ),
        classify(
            "(a) compliance stroke", "C-0011 / §3", "stroke at 100 pN dead load", strokeSpan,
            "sigma-resolved",
            "UNCHANGED as an owner. C-0019 lengthens it and CH-0024 shortens the DELIVERED " +
                    "part of it; the two run opposite and both are sub-grid at 10 nm."
        ),
        classify(
            "(m) delivered stroke", "CH-0024 / C-0021", "L0 - h0, the descent under hold-down",
            descentSpan, "sigma-resolved, and NEW",
            "A NEW sigma-resolved axis: the descent varies " +
                    "${"%.2f".format(descentSpan)}x across the grid at 10 nm because the " +
                    "layer's own stiffness at L0 does. It tightens the stroke threshold from " +
                    "3.0 to 3.0 + d and is the ONLY iteration-4 result that narrows anything."
        ),
        classify(
            "(h) peak per-load-path force", "C-0015 / C-0026", "peak crossover force",
            loadPathSpan, "sigma-resolved, through the foundation stiffness",
            "STILL NOT BINDING, and now with far more room: C-0026's solved-load figure is " +
                    "0.150 pN against a 10 pN unzip allowable, 67x, and the binding per-path " +
                    "quantity is the static share 100/n, which contains no sigma at all."
        ),
        classify(
            "(i) lateral confinement footprint", "C-0014 -> C-0020 / CH-0021 -> C-0023",
            "L_min of an in-plane tether", null, "WITHDRAWN — the axis leaves the design",
            "REMOVED. CH-0021 corrects the in-plane factor to exactly 1 and CH-0027 removes " +
                    "C-0014's eight substrate tethers from the design entirely, so there is " +
                    "no in-plane tether left to have a footprint. C-0016 reported this axis " +
                    "as a cost with no threshold; it is now not even a cost."
        ),
        classify(
            "(g) flatness attachment count", "C-0015 / CH-0034", "dishing over stroke", null,
            "topological, and SATURATING",
            "EXHAUSTED rather than met: under the solved load the dishing saturates at " +
                    "${"%.3f".format(inputs.flatnessSaturation.dishingAtSaturation)} of the " +
                    "stroke between 45 and " +
                    "${inputs.flatnessSaturation.attachmentsAtSaturation} attachments and " +
                    "never reaches the 0.10 tolerance. 45 is where attachments stop buying " +
                    "flatness, not where the tile becomes flat."
        ),
        classify(
            "(e) usable bias", "C-0012 -> C-0018", "the (bias, load line) ceiling", null,
            "height- and buffer-level",
            "SHARPENED, and it is now a (bias, load line) property. It still cannot narrow " +
                    "a sigma window; it closes a (height, buffer) cell. Pull-in binds at 11 " +
                    "of 54 coupled states, all at 10 nm in 2 mM."
        ),
        classify(
            "(f) output-coupling stiffness", "C-0017 / C-0019 / CH-0026", "33.333 pN/nm",
            null, "height- and buffer-level",
            "STANDS. C-0019 degrades the margin and CH-0026 restores it; the two are of the " +
                    "same size and opposite sign."
        ),
        classify(
            "(j) zero-bias confinement", "C-0021 / C-0023 / CH-0027", "k_BT/sigma_bound^2",
            null, "topological",
            "CLOSED BY CONSTRUCTION and invisible to an intersection: a two-sided coupling " +
                    "turns the requirement from a force into a stiffness, 0.4602 pN/nm, " +
                    "which §3's own mandate exceeds 72.4x unpreloaded."
        ),
        classify(
            "(k) joint anisotropy and the standoff window", "C-0025 / CH-0031",
            "k_perp/k_axial", null, "topological, and it is a LENGTH window not a sigma one",
            "NEW, and it does not live on the window's axes at all: the admissible standoff " +
                    "is 7-10 nm (21-29 bp), bounded below by C-0023's 40 pN/nm compliance " +
                    "ceiling and above by C-0017's own standoff envelope."
        ),
        classify(
            "(l) per-path allowable vs bonded length", "C-0024 / CH-0029",
            "shear allowable in pN", null, "topological / sequence-design",
            "NEW, and it is uneven: unzip is length-independent and shear is not, so it " +
                    "cannot be applied as one factor. It never binds inside the window."
        )
    )
}

// --- the "at least 3 nm" clauses -------------------------------------------------------------

private fun strokeClauses(
    inputs: ResynthesisInputs
): List<StrokeClause> = LAYER_HEIGHTS.flatMap { height ->
    listOf(
        HoldDownReading.NONE to "none — the L0 coordinate C-0012, C-0016 and C-0017 are written in",
        HoldDownReading.TETHERLESS to
                "C-0023's two-sided flexure, tetherless — the committed design",
        HoldDownReading.TETHERED to "C-0021's device: K2 plus C-0014's eight substrate tethers"
    ).map { (reading, topology) ->
        val bracket = inputs.descentBracket(reading)[height] ?: (0.0 to 0.0)
        // the clause is written at the operating point: §3's 3 nm stroke, delivered
        val delivered = ACCEPTABLE_STROKE_NM - bracket.second to ACCEPTABLE_STROKE_NM - bracket.first
        StrokeClause(
            clause = "§3: a stroke of at least 3 nm is acceptable",
            couplingTopology = topology,
            layerHeight = height,
            strokeFromOnsetHeight = ACCEPTABLE_STROKE_NM,
            descentLow = bracket.first,
            descentHigh = bracket.second,
            deliveredLow = delivered.first,
            deliveredHigh = delivered.second,
            shortfallHigh = bracket.second / ACCEPTABLE_STROKE_NM,
            meetsThreeNanometres = roundedDecision(delivered.first) >= ACCEPTABLE_STROKE_NM,
            verdict = when {
                bracket.second == 0.0 -> "MET by construction — the stroke is measured from L0"
                bracket.second / ACCEPTABLE_STROKE_NM < 0.01 ->
                    "MISSED by under 1 %, which is inside every model bracket carried"
                else -> "MISSED by ${"%.1f".format(100.0 * bracket.second / ACCEPTABLE_STROKE_NM)} % " +
                        "at worst; the window's own upper edge is tightened to " +
                        "3.0 + d nm of LAYER stroke to compensate"
            }
        )
    }
}

// --- the buffer decision ---------------------------------------------------------------------

private fun bufferComparison(
    inputs: ResynthesisInputs,
    margins: List<CorrectedMargin>
): List<BufferComparison> =
    inputs.usableBiasCeilings.filter { it.loadLine == "coupled" && it.operatingBias != null }
        .groupBy { it.layerHeight to it.concentration }
        .toSortedMap(compareBy({ it.first }, { it.second }))
        .map { (key, group) ->
            val (height, concentration) = key
            val here = margins.filter {
                it.layerHeight == height && it.concentration == concentration
            }
            BufferComparison(
                layerHeight = height,
                concentration = concentration,
                statesWithAFold = group.count { it.pullInBias != null },
                statesTotal = group.size,
                operatingBiasLow = group.minOf { it.operatingBias!! },
                operatingBiasHigh = group.maxOf { it.operatingBias!! },
                usableBiasLow = group.minOf { it.usableBias },
                usableBiasHigh = group.maxOf { it.usableBias },
                biasMarginLow = group.mapNotNull { it.margin }.minOrNull(),
                biasMarginHigh = group.mapNotNull { it.margin }.maxOrNull(),
                stiffnessMarginBaselineLow = here.mapNotNull { it.marginBaseline }.minOrNull(),
                stiffnessMarginBaselineHigh = here.mapNotNull { it.marginBaseline }.maxOrNull(),
                stiffnessMarginCorrectedLow = here.mapNotNull { it.marginCombinedLow }.minOrNull(),
                stiffnessMarginCorrectedHigh = here.mapNotNull { it.marginCombinedHigh }.maxOrNull(),
                bindingCeilings = group.map { it.bindingCeiling }.distinct().sorted()
            )
        }

// --- reporting ---------------------------------------------------------------------------------

private fun ledger(inputs: ResynthesisInputs): List<ResynthesisLedgerEntry> = listOf(
    ResynthesisLedgerEntry(
        "mandated coupling stiffness", MANDATED_COUPLING_STIFFNESS, "pN/nm",
        "C-0017 from §3 alone (100 pN / 3 nm)", "CITED"
    ),
    ResynthesisLedgerEntry("acceptable stroke", ACCEPTABLE_STROKE_NM, "nm", "§3", "CITED"),
    ResynthesisLedgerEntry("rigid-plate tolerance", FLATNESS_TOLERANCE, "1", "C-0015", "CITED"),
    ResynthesisLedgerEntry("tile edge", GEN1_TILE_EDGE, "nm", "§1", "CITED"),
    ResynthesisLedgerEntry(
        "edge multiplier at the 10 nm held gap, 2 mM", inputs.heldGapEdgeMultiplier(10.0), "1",
        "C-0022 / CH-0026", "DERIVED here from T-3b's own record, keyed on (buffer, gap, bias source)"
    ),
    ResynthesisLedgerEntry(
        "edge multiplier at the 7 nm held gap, 2 mM", inputs.heldGapEdgeMultiplier(7.0), "1",
        "C-0022 / CH-0026", "DERIVED here"
    ),
    ResynthesisLedgerEntry(
        "edge multiplier at the 5 nm held gap, 2 mM", inputs.heldGapEdgeMultiplier(5.0), "1",
        "C-0022 / CH-0026", "DERIVED here — and it is the one state where the sign REVERSES"
    ),
    ResynthesisLedgerEntry(
        "stroke multiplier at 10 nm", inputs.strokeMultiplier(10.0), "1", "C-0019", "CITED"
    ),
    ResynthesisLedgerEntry(
        "coil overlap multiplier at 10 nm", inputs.coilOverlapMultiplier(10.0), "1",
        "C-0019", "DERIVED here from T-1f's own edge shift"
    ),
    ResynthesisLedgerEntry(
        "k_brush multiplier at 10 nm", inputs.brushStiffnessMultiplier(10.0), "1",
        "C-0019", "CITED"
    ),
    ResynthesisLedgerEntry(
        "hold-down force, tetherless device at 10 nm",
        inputs.holdDownForce(HoldDownReading.TETHERLESS, 10.0), "pN", "C-0021", "CITED"
    ),
    ResynthesisLedgerEntry(
        "flatness floor under the solved load", inputs.flatnessSaturation.dishingAtSaturation,
        "1", "CH-0034", "CITED"
    ),
    ResynthesisLedgerEntry(
        "grafting density grid ratio", inputs.graftingDensityGrid[1] / inputs.graftingDensityGrid[0],
        "1", "T-1d's own sweep", "DERIVED here"
    )
)

private fun findings(
    inputs: ResynthesisInputs,
    windows: List<ResynthesisedWindow>,
    edges: List<EdgeMovement>,
    margins: List<CorrectedMargin>,
    pullIn: List<CorrectedPullIn>,
    axes: List<AxisClassification>
): Map<String, String> {
    val full = windows.filter { it.corrections == CorrectionSet.FULL.label }
    val tenAtTwo = margins.filter { it.layerHeight == 10.0 && it.concentration == 2.0 }
    return mapOf(
        "the_upper_edge_is_a_polymer_clause_and_the_field_cannot_reach_it" to
                "C-0016's upper edge is the stroke under a 100 pN DEAD LOAD. CH-0026's " +
                        "electrostatic edge enhancement is not an argument of it, so it " +
                        "cannot move it — asserted as a test. CH-0026's own statement that " +
                        "'C-0016's upper window edge moves outward' is therefore wrong about " +
                        "which clause owns that edge, and this is CH-0035.",
        "the_edge_correction_cancels_at_a_pinned_operating_point" to
                "At the operating point the force balance fixes |F_es| = 100 pN + P(g)A, and " +
                        "k_es = -|F_es|/l identically, so a multiplier on the force LEVEL is " +
                        "absorbed entirely into the bias and reaches k_es not at all. What " +
                        "survives is the collar's GRADIENT, d ln mu/dh = " +
                        "${"%.5f".format(inputs.collarGradientBracket(2.0, 7.0).first)}-" +
                        "${"%.5f".format(inputs.collarGradientBracket(2.0, 7.0).second)} /nm, " +
                        "which LENGTHENS the decay and REDUCES |k_es|. CH-0026 predicts the " +
                        "opposite direction for stability clauses because it reasons at fixed " +
                        "BIAS where the device is held at fixed FORCE.",
        "the_two_corrections_are_the_same_size_and_opposite_sign" to
                "At 10 nm and 2 mM C-0019 alone takes the stability margin to " +
                        "${"%.3f".format(tenAtTwo.mapNotNull { it.marginFluctuationOnly }.min())}-" +
                        "${"%.3f".format(tenAtTwo.mapNotNull { it.marginFluctuationOnly }.max())}x and " +
                        "CH-0026 alone to " +
                        "${"%.3f".format(tenAtTwo.mapNotNull { it.marginEdgeOnly }.min())}-" +
                        "${"%.3f".format(tenAtTwo.mapNotNull { it.marginEdgeOnly }.max())}x, against a " +
                        "baseline of ${"%.3f".format(tenAtTwo.mapNotNull { it.marginBaseline }.min())}-" +
                        "${"%.3f".format(tenAtTwo.mapNotNull { it.marginBaseline }.max())}x. Together " +
                        "they give ${"%.3f".format(tenAtTwo.mapNotNull { it.marginCombinedLow }.min())}-" +
                        "${"%.3f".format(tenAtTwo.mapNotNull { it.marginCombinedHigh }.max())}x. " +
                        "C-0019's own >= 1.07x was one half of a two-sided correction.",
        "the_only_iteration_four_result_that_narrows_anything_is_CH-0024" to
                "Three of the four candidate movers live on axes an intersection cannot see. " +
                        "CH-0024 is the exception: the descent varies across the grid because " +
                        "the layer's stiffness at L0 does, so it is a genuinely sigma-resolved " +
                        "tightening of the stroke threshold from 3.0 to " +
                        full.joinToString("; ") {
                            "L0 = ${it.layerHeight} nm: " +
                                    "${"%.4f".format(it.requiredLayerStrokeLow)}-" +
                                    "${"%.4f".format(it.requiredLayerStrokeHigh)} nm"
                        } + ".",
        "the_committed_coupling_almost_removes_CH-0024" to
                "CH-0024 quotes 2.62-2.93 nm delivered, for C-0021's device WITH C-0014's " +
                        "eight substrate tethers. CH-0027 removes those tethers from the " +
                        "design, and the tetherless device's descent at 10 nm is " +
                        inputs.descentBracket(HoldDownReading.TETHERLESS)[10.0]
                            ?.let { "${"%.4f".format(it.first)}-${"%.4f".format(it.second)} nm" } +
                        ", i.e. a shortfall of under 1 %. The correction and the part that " +
                        "caused it left the design in the same iteration.",
        "how_many_edges_actually_move" to
                edges.filter { it.baselineGraftingDensity != null }.joinToString("; ") {
                    "L0 = ${it.layerHeight} nm ${it.edge}: ${it.movedBy}"
                },
        "three_more_axes_that_cannot_narrow" to
                "Iteration 4 added four axes and removed one. " +
                        axes.count { !it.resolvesInSigma } + " of ${axes.size} do not resolve " +
                        "in sigma at all, and the one it REMOVED — lateral confinement's " +
                        "footprint — was C-0016's axis (i). C-0016's own lesson applies to " +
                        "itself: a constraint that cannot narrow is invisible to an " +
                        "intersection, and so is a constraint that has been discharged.",
        "the_flatness_count_is_exhausted_not_met" to
                "45 attachments is where attachments stop buying flatness, not where the tile " +
                        "becomes flat: under the solved load the dishing saturates at " +
                        "${"%.3f".format(inputs.flatnessSaturation.dishingAtSaturation)} of " +
                        "the stroke and never reaches the 0.10 tolerance at any count. This " +
                        "does not move a window edge — it moves what the count MEANS.",
        "the_pull_in_bias_movement_is_not_resolved" to
                "The operating bias falls by " +
                        "${"%.1f".format(100.0 * -pullIn.map { it.operatingBiasShift / it.operatingBias }.average())} % " +
                        "on average, which raises the margin to " +
                        "${"%.4f".format(pullIn.minOf { it.marginLowerBound })}-" +
                        "${"%.4f".format(pullIn.maxOf { it.marginLowerBound })} at unchanged " +
                        "pull-in bias. But at the fold itself the two corrections leave a " +
                        "coupled tangent of ${"%.3f".format(pullIn.minOf { it.foldTangentAtLowGradient })}" +
                        " to ${"%.3f".format(pullIn.maxOf { it.foldTangentAtHighGradient })} pN/nm — " +
                        "STRADDLING ZERO across the collar gradient's own difference-scheme " +
                        "spread. The fold's own movement is inside the resolution of the " +
                        "correction that would move it, and is reported as unresolved."
    )
}

private fun verdict(
    full: List<ResynthesisedWindow>,
    tenAtTwo: List<CorrectedMargin>,
    tenAtHalf: List<CorrectedMargin>,
    pullIn: List<CorrectedPullIn>,
    strokeClauses: List<StrokeClause>
): Map<String, String> = mapOf(
    "P1 — §4(a)-(d) after iteration 4" to full.joinToString("; ") { window ->
        if (window.empty) "L0 = ${window.layerHeight} nm: EMPTY"
        else "L0 = ${window.layerHeight} nm: sigma in [" +
                "${"%.5g".format(window.lowestGraftingDensity)}, " +
                "${"%.5g".format(window.highestGraftingDensity)}] nm^-2, " +
                "${"%.4g".format(window.widthRatio)}x wide"
    },
    "P1 binding constraints" to full.joinToString("; ") { window ->
        if (window.empty)
            "L0 = ${window.layerHeight} nm closed by " +
                    "${window.crossing?.lowerBoundConstraint} against " +
                    window.crossing?.upperBoundConstraint
        else "L0 = ${window.layerHeight} nm: lower ${window.lowerBinding}, " +
                "upper ${window.upperBinding}"
    },
    "P2 — the output-coupling verdict" to
            "STANDS. §3's own mandated 33.333 pN/nm clears the stability floor at every " +
                    "state. At the worst point (10 nm, 2 mM) the margin is " +
                    "${"%.3f".format(tenAtTwo.mapNotNull { it.marginCombinedLow }.min())}-" +
                    "${"%.3f".format(tenAtTwo.mapNotNull { it.marginCombinedHigh }.max())}x against " +
                    "C-0017's published ${"%.3f".format(tenAtTwo.mapNotNull { it.marginBaseline }.min())}-" +
                    "${"%.3f".format(tenAtTwo.mapNotNull { it.marginBaseline }.max())}x. C-0019's " +
                    ">= 1.07x is superseded: it carried only the polymer half of a two-sided " +
                    "correction.",
    "P2 — the pull-in margin" to
            "The operating bias falls, so the margin rises to at least " +
                    "${"%.4f".format(pullIn.filter { it.layerHeight == 10.0 && it.concentration == 2.0 }.minOf { it.marginLowerBound })}-" +
                    "${"%.4f".format(pullIn.filter { it.layerHeight == 10.0 && it.concentration == 2.0 }.maxOf { it.marginLowerBound })} " +
                    "at 10 nm / 2 mM against C-0018's 1.007-1.032 — BUT the fold's own " +
                    "movement is inside the collar gradient's difference-scheme spread, so " +
                    "the net is NOT RESOLVED here. Read C-0018's margin as standing.",
    "P4 — the '>= 3 nm' clauses" to strokeClauses.filter {
        it.couplingTopology.startsWith("C-0023")
    }.joinToString("; ") {
        "L0 = ${it.layerHeight} nm delivers ${"%.3f".format(it.deliveredLow)}-" +
                "${"%.3f".format(it.deliveredHigh)} nm (${it.verdict})"
    },
    "P5 — the buffer" to
            "0.5 mM should be the NOMINAL. At 10 nm it removes the pull-in fold entirely " +
                    "(C-0018), and the stability margin there is " +
                    "${"%.3f".format(tenAtHalf.mapNotNull { it.marginCombinedLow }.min())}-" +
                    "${"%.3f".format(tenAtHalf.mapNotNull { it.marginCombinedHigh }.max())}x against " +
                    "${"%.3f".format(tenAtTwo.mapNotNull { it.marginCombinedLow }.min())}-" +
                    "${"%.3f".format(tenAtTwo.mapNotNull { it.marginCombinedHigh }.max())}x at 2 mM — a " +
                    "factor of ${"%.2f".format(tenAtHalf.mapNotNull { it.marginCombinedLow }.min() / tenAtTwo.mapNotNull { it.marginCombinedLow }.min())} " +
                    "at the worst end, and the only margin in the programme that clears " +
                    "C-0005's own 123-214 % mean-field error. The layer's mechanics are " +
                    "buffer-independent to <= 0.4 % (C-0007), so it costs nothing in stroke, " +
                    "nothing in window width and nothing in chemistry. §3 does not name it.",
    "the desired 10 nm stroke" to
            "STILL UNREACHABLE at every height and every grafting density. C-0001's one " +
                    "surviving headline is untouched by iteration 4."
)

private fun validity(inputs: ResynthesisInputs): List<String> = listOf(
    "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and nothing here is " +
            "re-derived: every number is a transfer, and every transfer is checked.",
    "THE HEIGHT CONVENTION IS FORCE-ONSET. $HEIGHT_CONVENTION",
    "Every window edge is a grid point on T-1d's 61-point logarithmic sweep, located to " +
            "${"%.4f".format(inputs.graftingDensityGrid[1] / inputs.graftingDensityGrid[0])}x " +
            "and no better. Several corrections here are smaller than that and are reported " +
            "as sub-grid rather than as zero.",
    "The edge correction is carried as a DECOMPOSITION, not a re-run: the level term cancels " +
            "at a pinned operating point and only the collar gradient and the bias shift " +
            "survive. The gradient is a finite difference over a sweep whose bias covaries " +
            "with the gap; mu is a function of the gap to 0.14 % at the one gap sampled at " +
            "three biases, which is what makes the difference meaningful, and the three " +
            "difference schemes are reported as the uncertainty rather than one being chosen.",
    "The pull-in propagation covers only the folds for which T-16 has a coupling record at " +
            "the same (model, height, buffer). T-16 swept 0.5/1/2 mM and T-4 swept 0.5/2/10, " +
            "so the 7 nm / 10 mM folds are NOT propagated and C-0018's own numbers stand there.",
    "C-0019's brackets are measured at the two design points only. At 5 nm there is no T-1f " +
            "record and the 7 nm multipliers are used; 5 nm is empty by 13.3x, so a 1.4 % " +
            "stroke change cannot reach it, and that is asserted rather than assumed.",
    "The per-point descent is FIRST ORDER in the layer's stiffness at L0, d = F/(k_c + k), " +
            "where T-13 solved the same balance non-linearly. The transfer is checked at " +
            "T-13's own design points and reported as licensed or not.",
    "Mean-field electrostatics, inherited whole. C-0005 puts the one-loop correction at " +
            "123-214 % of the leading term across the whole 5-10 nm range, and CH-0019 " +
            "establishes that NOTHING in this queue narrows it. Every margin here is " +
            "NOT EXCLUDED, never established.",
    "C-0016's and C-0017's own validity ranges travel unchanged, including the 1.22x " +
            "exposure of the solved layer against C-0003's bracket at 5 nm.",
    "The layer is neutral linear PEG. §3 also permits PEO and a PS->PEG block copolymer, for " +
            "which no osmotic equation of state was ever consumed in this programme."
)

private fun openQuestions(): List<String> = listOf(
    "Whether the pull-in bias itself moves. The two corrections cancel at the fold to within " +
            "the collar gradient's own difference-scheme spread, so this synthesis cannot " +
            "resolve it. A 2-D field solve ON THE EQUILIBRIUM PATH — rather than at the six " +
            "gaps T-3b sampled — would, and it is cheap: T-3b's solver already exists.",
    "T-21's phi = 0.2 ceiling, which C-0018 makes the binding ceiling at 121 of 162 states " +
            "and which C-0019 shows cannot be moved by any theory argument. It needs osmometry.",
    "T-50, the beyond-mean-field treatment of the actuated gap. It remains the last unbounded " +
            "exposure on the critical path, and the free alternative is to adopt 0.5 mM.",
    "Whether §3 will accept 0.5 mM as the nominal buffer. That is a decision, not a " +
            "calculation, and it is the single largest open design question in the programme.",
    "T-40, the base joint of C-0025's normal standoff — the one assumption the committed " +
            "coupling now rests on, and it is unexamined."
)

/** Formats a margin that may be absent — a zero floor is no requirement, not an infinity. */
private fun fmt(value: Double?): String = value?.let { "%.4g".format(it) } ?: "none"

@Suppress("LongMethod")
private fun report(result: WindowResynthesisResult) {
    println("=== T-25 — the design window re-synthesised ".padEnd(100, '='))
    println()
    println("--- the windows, under each correction set ".padEnd(100, '-'))
    println("%-46s %-5s %-11s %-11s %-8s %s".format("corrections", "L0", "low", "high", "width", "owners"))
    result.windows.forEach { window ->
        println(
            "%-46s %-5.1f %-11s %-11s %-8s %s".format(
                window.corrections, window.layerHeight,
                window.lowestGraftingDensity?.let { "%.5g".format(it) } ?: "EMPTY",
                window.highestGraftingDensity?.let { "%.5g".format(it) } ?: "EMPTY",
                window.widthRatio?.let { "%.4g".format(it) } ?: "-",
                "${window.lowerBinding.joinToString(",")} | ${window.upperBinding.joinToString(",")}"
            )
        )
    }
    println()
    println("--- which edges move ".padEnd(100, '-'))
    result.edgeMovements.forEach {
        println("  L0 = %4.1f nm %-6s %s%s".format(
            it.layerHeight, it.edge, it.movedBy,
            if (it.ownerChanged) "  *** OWNER CHANGED ***" else ""
        ))
    }
    println()
    println("--- the coupling margin, 10 nm ".padEnd(100, '-'))
    println(
        "%-32s %-5s %-9s %-9s %-9s %s".format(
            "model", "mM", "baseline", "+C-0019", "+CH-0026", "combined"
        )
    )
    result.couplingMargins.filter { it.layerHeight == 10.0 }.forEach {
        println(
            "%-32s %-5.1f %-9s %-9s %-9s %s".format(
                it.model, it.concentration, fmt(it.marginBaseline),
                fmt(it.marginFluctuationOnly), fmt(it.marginEdgeOnly),
                "${fmt(it.marginCombinedLow)}-${fmt(it.marginCombinedHigh)}"
            )
        )
    }
    println()
    println("--- the pull-in bound ".padEnd(100, '-'))
    result.pullInBounds.forEach { println("  ${it.statement}") }
    println()
    println("--- the axes ".padEnd(100, '-'))
    result.axes.forEach {
        println(
            "  %-42s %-14s span %-8s %s".format(
                it.axis, it.level,
                it.variationRatioAcrossSigma?.let { r -> "%.3g".format(r) } ?: "n/a",
                if (it.canNarrowAWindow) "CAN narrow" else "cannot narrow"
            )
        )
    }
    println()
    println("--- the '>= 3 nm' clauses ".padEnd(100, '-'))
    result.strokeClauses.forEach {
        println(
            "  L0 = %4.1f nm  %-62s %.3f-%.3f nm  %s".format(
                it.layerHeight, it.couplingTopology.take(62), it.deliveredLow, it.deliveredHigh,
                if (it.meetsThreeNanometres) "MET" else "missed"
            )
        )
    }
    println()
    println("--- the buffer ".padEnd(100, '-'))
    result.bufferComparison.forEach {
        println(
            "  L0 = %4.1f nm %5.1f mM  folds %d/%d  usable %.3f-%.3f V  bias margin %s-%s  stiffness margin %s-%s"
                .format(
                    it.layerHeight, it.concentration, it.statesWithAFold, it.statesTotal,
                    it.usableBiasLow, it.usableBiasHigh, fmt(it.biasMarginLow),
                    fmt(it.biasMarginHigh), fmt(it.stiffnessMarginCorrectedLow),
                    fmt(it.stiffnessMarginCorrectedHigh)
                )
        )
    }
    println()
    println("--- declared falsifiers ".padEnd(100, '-'))
    result.falsifiers.forEach {
        println("  ${it.id}. ${if (it.fired) "FIRED" else "did not fire"} — ${it.outcome}")
    }
    println()
    println("--- verdict ".padEnd(100, '-'))
    result.verdict.forEach { (key, value) -> println("  $key:\n      $value") }
    println()
}
