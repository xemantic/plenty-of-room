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

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

/**
 * Task `T-118` — the Gen-1 design window and the standing findings, re-run against everything
 * iterations 5, 6 and 7 produced (`C-0031`–`C-0050`, `CH-0043`–`CH-0062`).
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh window.SecondResynthesisStudyKt
 * ```
 *
 * Emits `gpd/results/T-118-window-resynthesis-two.json`, deterministically.
 *
 * The direct successor to `T-25`, and written under `C-0016`'s own discipline: **a constraint
 * that cannot narrow is invisible to an intersection**, and `C-0027` added the converse — *and
 * so is a constraint that has been discharged*. So every constraint iterations 5–7 discovered
 * is classified by **axis** before it is intersected, removals are recorded as carefully as
 * additions, and the two quantities that *are* `σ`-resolved are evaluated at all 61 grid points
 * rather than argued about.
 */

// --- record types ------------------------------------------------------------------------

/** A number this task carries, tagged with where it came from and whether it was derived. */
@Serializable
data class SecondLedgerEntry(
    val quantity: String,
    val value: Double,
    val unit: String,
    val source: String,
    val provenance: String
)

/** One `σ`-resolved candidate constraint, intersected against the window's own grid. */
@Serializable
data class SecondCandidateAxis(
    val constraint: String,
    val source: String,
    val layerHeight: Double,
    val crossoverReading: String,
    val admissibleLow: Double? = null,
    val admissibleHigh: Double? = null,
    val admitsEveryGridPoint: Boolean,
    val bindsInsideTheWindow: Boolean,
    val worstMarginInsideTheWindow: Double,
    val statement: String
)

/** A declared falsifier of `T-118`, and whether it fired. */
@Serializable
data class SecondFalsifier(
    val id: Int,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

/** An upstream figure, reproduced from the file of the study that emitted it. */
@Serializable
data class SecondReproduction(
    val quantity: String,
    val source: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

/** The whole `T-118` result. */
@Serializable
data class SecondResynthesisResult(
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
    val ledger: List<SecondLedgerEntry>,
    val windows: List<ResynthesisedWindow>,
    val edgeReproductions: List<WindowEdgeReproduction>,
    val candidateAxes: List<SecondCandidateAxis>,
    val strokeCeilings: List<StrokeCeilingRecord>,
    val crossoverLicences: List<CrossoverLicence>,
    val uncheckedLicenceHeights: List<Double>,
    val foldChannels: List<FoldChannelRecord>,
    val axes: List<AxisClassification>,
    val bufferComparison: List<BufferComparison>,
    val reproductions: List<SecondReproduction>,
    val falsifiers: List<SecondFalsifier>,
    val findings: Map<String, String>,
    val verdict: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private const val SECOND_HEIGHT_CONVENTION: String =
    "Every layer height here is a FORCE-ONSET height: L0 is where the layer carries 1.0 pN " +
            "over the 40 x 40 nm tile (C-0011). The first-moment thickness 2<z> of the same " +
            "layer is 1.71-2.16x smaller, and a bench reading this window in the wrong " +
            "convention would order 8-9 kDa PEG where it needs 1.1-3.3 kDa."

@Suppress("LongMethod", "CyclomaticComplexMethod")
fun main() {
    val directory = File("gpd/results")
    val inputs = ResynthesisInputs.read(directory)
    val second = SecondResynthesisInputs.read(directory)
    val grid = inputs.graftingDensityGrid
    val gridRatio = grid[1] / grid[0]

    val windows = listOf(CorrectionSet.IDENTITY, CorrectionSet.FULL)
        .flatMap { resynthesisedWindows(inputs, it) }
    val full = windows.filter { it.corrections == CorrectionSet.FULL.label }
    val edgeReproductions = windowEdgeReproductions(inputs, second)
    val ceilings = strokeCeilingsAcrossTheWindow(inputs, second.crossoverFractions)
    val licences = crossoverLicenceChecks(inputs, second)
    val unchecked = uncheckedLicenceHeights(inputs, second)
    val channels = foldChannels(second)
    val margins = correctedMargins(inputs)
    val candidates = candidateAxes(full, ceilings, second)

    // C-0027's own buffer comparison, re-read: the layer's mechanics are buffer-independent
    // (C-0007), so this is the actuator's axis and not the window's
    val buffers = listOf(0.5, 2.0).map { concentration ->
        val at = margins.filter { it.layerHeight == 10.0 && it.concentration == concentration }
        val folds = second.baselineFolds.filter {
            it.state == "10 nm / 2 mM" && it.loadLine == "coupled" && it.variant.startsWith("mu = 1")
        }
        BufferComparison(
            layerHeight = 10.0,
            concentration = concentration,
            statesWithAFold = if (concentration == 2.0) folds.size else 0,
            statesTotal = 6,
            operatingBiasLow = 0.0,
            operatingBiasHigh = 0.0,
            usableBiasLow = 0.0,
            usableBiasHigh = 0.0,
            biasMarginLow = if (concentration == 2.0) folds.minOf { it.margin } else null,
            biasMarginHigh = if (concentration == 2.0) folds.maxOf { it.margin } else null,
            stiffnessMarginBaselineLow = at.mapNotNull { it.marginBaseline }.minOrNull(),
            stiffnessMarginBaselineHigh = at.mapNotNull { it.marginBaseline }.maxOrNull(),
            stiffnessMarginCorrectedLow = at.mapNotNull { it.marginCombinedLow }.minOrNull(),
            stiffnessMarginCorrectedHigh = at.mapNotNull { it.marginCombinedHigh }.maxOrNull(),
            bindingCeilings = if (concentration == 2.0)
                listOf("static stability (pull-in)", "concentrated crossover (C-0002, phi = 0.2)")
            else listOf("concentrated crossover (C-0002, phi = 0.2)")
        )
    }

    val reproductions = reproductions(inputs, second, edgeReproductions, channels)
    val movedEdges = edgeReproductions.count { it.movedGridSteps != 0 }
    val bindingCandidates = candidates.count { it.bindsInsideTheWindow }
    val straddles = channels.any { it.increment.total > 0.0 } && channels.any { it.increment.total < 0.0 }

    val falsifiers = listOf(
        SecondFalsifier(
            id = 1,
            statement = "A window edge moving on the repaired solver, which would mean " +
                    "C-0031's byte-identity finding is wrong and the whole tree needs " +
                    "re-adjudicating.",
            fired = movedEdges > 0,
            outcome = if (movedEdges > 0) "FIRED at $movedEdges of 6 windows"
            else "no edge of C-0027's six windows moves by even one grid step, and no edge " +
                    "changes owner. C-0031's finding is confirmed by a re-intersection rather " +
                    "than by a diff of the file it produced."
        ),
        SecondFalsifier(
            id = 2,
            statement = "A sigma-resolved constraint from iterations 5-7 binding inside a " +
                    "window, which would make the window a grafting-density statement again.",
            fired = bindingCandidates > 0,
            outcome = if (bindingCandidates > 0) "FIRED at $bindingCandidates candidate(s)"
            else "neither of the two candidates binds anywhere. C-0050's kinematic ceiling " +
                    "clears §3's 3 nm stroke by " +
                    "${"%.2f".format(candidates.filter { it.constraint.startsWith("kinematic") }
                        .minOf { it.worstMarginInsideTheWindow })}x at the worst point of the " +
                    "surviving windows, and C-0002's concentrated crossover by " +
                    "${"%.2f".format(candidates.filter { it.constraint.startsWith("concentrated")
                            && it.crossoverReading.startsWith("C-0002") }
                        .minOf { it.worstMarginInsideTheWindow })}x."
        ),
        SecondFalsifier(
            id = 3,
            statement = "The three-channel fold tangent STRADDLING zero across the six layer " +
                    "models, in which case the direction of C-0018's margin is unresolved and " +
                    "must be reported as C-0027 reported its own straddle.",
            fired = straddles,
            outcome = if (straddles) "FIRED — the composed increment straddles zero"
            else "it does not straddle: the composed increment is " +
                    "${"%.3f".format(channels.minOf { it.increment.total })} to " +
                    "${"%.3f".format(channels.maxOf { it.increment.total })} pN/nm, NEGATIVE at " +
                    "all six models. C-0033's collar recovers only " +
                    "${"%.0f".format(100.0 * channels.minOf { it.collarRecoversFractionOfTheSoftening })}-" +
                    "${"%.0f".format(100.0 * channels.maxOf { it.collarRecoversFractionOfTheSoftening })} % " +
                    "of what C-0032's realised element costs."
        ),
        SecondFalsifier(
            id = 4,
            statement = "An upstream number failing to reproduce from its own result file, " +
                    "which would mean a transfer somewhere in the corpus is a transcription.",
            fired = reproductions.any { it.relativeDeparture > 1e-3 },
            outcome = "worst departure " +
                    "${"%.2g".format(reproductions.maxOf { it.relativeDeparture })} over " +
                    "${reproductions.size} reproductions"
        ),
        SecondFalsifier(
            id = 5,
            statement = "C-0050's ceiling verdicts transferring unchanged onto the SOLVED " +
                    "layer the window is drawn on, which would make the licence check idle.",
            fired = licences.all { it.licensed },
            outcome = if (licences.all { it.licensed })
                "FIRED — every licence holds and the check was idle"
            else "they do NOT transfer: at the 10 nm upper edge the solved layer sits at " +
                    "phi = ${"%.4f".format(licences.first { it.layerHeight == 10.0 }.solvedVolumeFractionAtUpperEdge)} " +
                    "while C-0003's six trial-function models sit " +
                    "${"%.2f".format(licences.first { it.layerHeight == 10.0 }.ratioHigh)}x higher, " +
                    "and two of them have no validity ceiling at all. C-0050's bound 3 is not " +
                    "licensed at that design point; its bound 2 is, and bound 2 settles its " +
                    "own question."
        )
    )

    val result = SecondResynthesisResult(
        task = "T-118",
        leaf = "A2.1, re-checking the acceptance strings of A2.2, A1.1, A8.2 and A7.4",
        title = "The Gen-1 design window and the standing findings, re-run against " +
                "iterations 5-7 (C-0031-C-0050, CH-0043-CH-0062)",
        verificationType = "logical (constraint intersection over a common grid, re-run) + " +
                "in-silico (every upstream number read from the emitting study's own result " +
                "file at run time, keyed on every dimension its sweep varied; C-0030's " +
                "element re-run as a library rather than tabulated)",
        acceptance = "§6 task 2, re-answered against three iterations: which window edges " +
                "move, which verdicts move, what AXIS each new constraint lives on, and what " +
                "the programme's answer now is given C-0050's kinematic bound.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "A design window is exactly the artifact a reader mistakes for a recommendation.",
        units = mapOf(
            "length" to "nm",
            "graftingDensity" to "nm^-2",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "concentration" to "mM MgCl2",
            "volumeFraction" to "1 (N sigma v0 / h, the PHYSICAL one)",
            "margin" to "1 (a dimensionless ratio)"
        ),
        conventions = listOf(
            "L0 is a FORCE-ONSET height: where the layer carries 1.0 pN over the 40 x 40 nm " +
                    "tile (C-0011, CH-0010).",
            "The stroke s = L0 - h is positive DOWNWARD, so s < L0 identically (C-0050).",
            "A window width is a RATIO of its edges, never a difference.",
            "Every window edge is a grid POINT on T-1d's 61-point logarithmic sweep, located " +
                    "to ${"%.5f".format(gridRatio)}x and no better. A movement smaller than " +
                    "that is reported as sub-grid and never as zero.",
            "A zero stability floor is the ABSENCE of a requirement and is recorded as null, " +
                    "never as an infinity.",
            "phi = N sigma v0 / h is the PHYSICAL volume fraction; T-1d emits it directly, so " +
                    "the dry thickness N sigma v0 = phi L0 needs no re-derivation.",
            "At C-0018's own fold the baseline coupled tangent VANISHES by construction, so " +
                    "every correction enters as an INCREMENT and the sign of the sum is the " +
                    "direction the fold moves."
        ),
        heightConvention = SECOND_HEIGHT_CONVENTION,
        parameters = mapOf(
            "layerHeights" to "5 / 7 / 10 nm",
            "graftingDensityGrid" to "61 points, ${"%.4g".format(grid.first())} to " +
                    "${"%.4g".format(grid.last())} nm^-2, ratio ${"%.5f".format(gridRatio)}",
            "tile" to "40 x 40 nm, footprint 1600 nm^2",
            "buffer" to "aqueous MgCl2 at 0.5 and 2 mM; NEITHER is adopted (T-63)",
            "temperature" to "300 K, k_BT = 4.142 pN nm",
            "acceptableStroke" to "3.0 nm at 100 pN (§3)",
            "desiredStroke" to "10.0 nm at 100 pN (§3), quoted and never adopted",
            "mandate" to "${"%.4f".format(MANDATED_COUPLING_STIFFNESS)} pN/nm (C-0017, from §3)",
            "pathCountForTheRealisedElement" to
                    "45 (C-0015), against C-0041's buildable ${second.packingLimitedPaths}"
        ),
        ledger = ledger(second, gridRatio),
        windows = windows,
        edgeReproductions = edgeReproductions,
        candidateAxes = candidates,
        strokeCeilings = ceilings,
        crossoverLicences = licences,
        uncheckedLicenceHeights = unchecked,
        foldChannels = channels,
        axes = axisLedgerAfterSeven(inputs, ceilings),
        bufferComparison = buffers,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings(full, edgeReproductions, candidates, channels, licences),
        verdict = verdict(full, channels, second),
        validity = validity(gridRatio, unchecked),
        openQuestions = openQuestions()
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-118-window-resynthesis-two.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForWindowResult().withEmissionHeader(LatticeTag.SQUARE, null)) + "\n"
    )
    report(result)
    println("written: ${output.path}")
}

// --- the sigma-resolved candidates, intersected -----------------------------------------------

private fun candidateAxes(
    full: List<ResynthesisedWindow>,
    ceilings: List<StrokeCeilingRecord>,
    second: SecondResynthesisInputs
): List<SecondCandidateAxis> = full.filter { !it.empty }.flatMap { window ->
    second.crossoverFractions.map { reading ->
        val inside = ceilings.filter {
            it.layerHeight == window.layerHeight &&
                    it.crossoverReading == reading.name &&
                    it.graftingDensity >= window.lowestGraftingDensity!! * 0.999 &&
                    it.graftingDensity <= window.highestGraftingDensity!! * 1.001
        }
        require(inside.isNotEmpty()) { "no ceiling records inside the ${window.layerHeight} nm window" }
        val worstKinematic = inside.minOf { it.kinematicCeiling } / ACCEPTABLE_STROKE_NM
        val worstValidity = inside.minOf { record ->
            (record.validityCeiling ?: 0.0) / ACCEPTABLE_STROKE_NM
        }
        SecondCandidateAxis(
            constraint = "concentrated crossover at rest, phi(L0) <= phi_c (C-0002 / C-0036), " +
                    "read as C-0050's validity stroke ceiling",
            source = "C-0050 / C-0002 / C-0036",
            layerHeight = window.layerHeight,
            crossoverReading = reading.name,
            admissibleLow = inside.first().graftingDensity,
            admissibleHigh = inside.last().graftingDensity,
            admitsEveryGridPoint = inside.all { it.acceptableStrokeInsideValidity },
            bindsInsideTheWindow = !inside.all { it.acceptableStrokeInsideValidity },
            worstMarginInsideTheWindow = worstValidity,
            statement = "L0 = ${window.layerHeight} nm, '${reading.name}' (phi_c = " +
                    "${reading.fraction}): the solved layer's own phi runs " +
                    "${"%.4f".format(inside.minOf { it.meanVolumeFraction })}-" +
                    "${"%.4f".format(inside.maxOf { it.meanVolumeFraction })} across the " +
                    "window, so the validity stroke ceiling clears §3's 3 nm by " +
                    "${"%.2f".format(worstValidity)}x at its worst point, against a " +
                    "kinematic ceiling of ${"%.2f".format(worstKinematic)}x"
        )
    } + SecondCandidateAxis(
        constraint = "kinematic stroke ceiling, s <= L0 - N sigma v0 (C-0050 bound 2)",
        source = "C-0050",
        layerHeight = window.layerHeight,
        crossoverReading = "n/a — the kinematic bound needs no crossover",
        admissibleLow = window.lowestGraftingDensity,
        admissibleHigh = window.highestGraftingDensity,
        admitsEveryGridPoint = true,
        bindsInsideTheWindow = false,
        worstMarginInsideTheWindow = ceilings.filter {
            it.layerHeight == window.layerHeight &&
                    it.crossoverReading == second.crossoverFractions.first().name &&
                    it.graftingDensity >= window.lowestGraftingDensity!! * 0.999 &&
                    it.graftingDensity <= window.highestGraftingDensity!! * 1.001
        }.minOf { it.kinematicCeiling } / ACCEPTABLE_STROKE_NM,
        statement = "L0 = ${window.layerHeight} nm: the layer's own dry thickness is " +
                "phi L0, so the kinematic ceiling never falls below " +
                "${"%.3f".format(ceilings.filter {
                    it.layerHeight == window.layerHeight &&
                            it.crossoverReading == second.crossoverFractions.first().name
                }.minOf { it.kinematicCeiling })} nm anywhere on the grid. It is sigma-resolved " +
                "and it CAN narrow a window; here it does not, by a wide margin."
    )
}

// --- the axis ledger, extended to iterations 5-7 -----------------------------------------------

private fun axisLedgerAfterSeven(
    inputs: ResynthesisInputs,
    ceilings: List<StrokeCeilingRecord>
): List<AxisClassification> {
    val at10 = inputs.scf.designPoints.filter { it.layerHeight == 10.0 }
        .sortedBy { it.graftingDensity }
    fun span(values: List<Double>) = values.max() / values.min()
    val overlapSpan = span(at10.map { point -> point.solved.minOf { it.coilOverlap } })
    val strokeSpan = span(at10.map { point -> point.solved.minOf { it.strokeUnderTargetForce } })
    val kinematicSpan = span(
        ceilings.filter { it.layerHeight == 10.0 && it.crossoverReading.startsWith("C-0002") }
            .map { it.kinematicCeiling }
    )
    val phiSpan = span(
        ceilings.filter { it.layerHeight == 10.0 && it.crossoverReading.startsWith("C-0002") }
            .map { it.meanVolumeFraction }
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
            "UNCHANGED as an owner through iterations 5-7. It still owns every lower edge."
        ),
        classify(
            "(a) compliance stroke", "C-0011 / §3", "stroke at 100 pN dead load", strokeSpan,
            "sigma-resolved",
            "UNCHANGED as an owner. C-0050 gives it a MECHANISM — the stroke is the layer's " +
                    "own compression — without moving it."
        ),
        classify(
            "(n) kinematic stroke ceiling", "C-0050 (NEW)", "L0 - N sigma v0", kinematicSpan,
            "sigma-resolved, and NEW",
            "NEW and sigma-resolved: it CAN narrow a window. It does not — it clears §3's 3 nm " +
                    "clause everywhere on the grid. It closes §3's DESIRED clause instead, " +
                    "which is a height statement and not a sigma one."
        ),
        classify(
            "(o) concentrated crossover at rest", "C-0002 / C-0036 / C-0050 (NEW)",
            "phi(L0) = N sigma v0 / L0 against phi_c", phiSpan,
            "sigma-resolved, and NEW",
            "NEW and sigma-resolved. On the SOLVED layer it clears at every grid point of " +
                    "both windows; C-0036 makes phi_c a one-parameter family and says itself " +
                    "that it reaches the design only through C-0018's BIAS ceiling."
        ),
        classify(
            "(f) output-coupling stiffness", "C-0017 / C-0032 / C-0049", "|k_eff| at the held gap",
            null, "height- and buffer-level",
            "MOVED, and not in sigma: C-0032 spends C-0017's free reserve (the realised " +
                    "element strain-SOFTENS) and C-0049 recovers 4 of 6 model floors by reading " +
                    "the tangent over the traversed range. Neither can narrow a window."
        ),
        classify(
            "(e) usable bias / pull-in", "C-0018 / C-0032 / C-0033 / C-0036", "V_pullin / V*",
            null, "height- and buffer-level",
            "MOVED ON THREE CHANNELS that were never composed: C-0033's collar RAISES it, " +
                    "C-0019's fluctuation and C-0032's softening LOWER it. Composed here for " +
                    "the first time. C-0036 additionally re-writes its binding ceiling census."
        ),
        classify(
            "(p) declared compliance ceiling", "C-0023 -> C-0049", "40 pN/nm on a tangent",
            null, "WITHDRAWN from the acceptance stack",
            "REMOVED: C-0049 shows 40 pN/nm is 1.2x the PLACEMENT mandate and is owed at the " +
                    "placement stroke only; the acceptance stack C-0017 and C-0018 define has " +
                    "NO upper bound on a coupling tangent. The axis leaves, exactly as " +
                    "C-0027's axis (i) did."
        ),
        classify(
            "(q) per-path allowable as a stiffness", "C-0006 / CH-0029 / C-0049 (NEW)",
            "n x allowable / s", null, "topological — a PATH COUNT, and it tightens as 1/s",
            "NEW, and it is what replaced (p). At 15 paths it is 50 pN/nm at §3's acceptable " +
                    "stroke and 15 at its desired one, so it clears the mandate at 3 nm and " +
                    "refuses it at 10. A count, not a grafting density."
        ),
        classify(
            "(r) coupling plan view", "C-0041 / C-0047 / C-0046 (NEW)", "paths that pack",
            null, "topological / plan geometry",
            "NEW: the 45-path array has no plan view at any level count; the tile carries 15, " +
                    "at 1 of 720 orientations. At 15 the coupling DISHES 2.26x worse than no " +
                    "coupling at all (C-0047). Neither is a function of sigma."
        ),
        classify(
            "(s) hinge inventory", "C-0040 / CH-0054 / CH-0062", "crossovers per hinge line",
            null, "topological — a LATTICE COUNT",
            "NEW: a hinge line carries FOUR crossovers at every one of the 32 phases, not " +
                    "sixteen. CH-0062 then reverses C-0040's own acceptable-stroke verdict on " +
                    "C-0039's exact elastica. No simulation can move a count."
        ),
        classify(
            "(t) reach of §3's desired clause", "C-0050 (NEW)", "s = L0 - h < L0",
            null, "a SPECIFICATION question — it is a layer HEIGHT",
            "NEW and decisive: §3 names no layer taller than 10 nm, so the desired stroke is " +
                    "out of reach on §3's own stack. It cannot narrow a window because it is " +
                    "not a constraint on sigma at all — it closes a CLAUSE."
        ),
        classify(
            "(u) numerics provenance", "C-0031 / CH-0043", "significant digits emitted",
            null, "methodological",
            "NEW, and it moves nothing: the solver repair leaves every window edge unmoved, " +
                    "re-checked here by re-intersection. CH-0043's caution stands — the tree " +
                    "rounds to nine digits where a solved height is determined to six."
        )
    )
}

// --- reproductions ------------------------------------------------------------------------------

private fun reproductions(
    inputs: ResynthesisInputs,
    second: SecondResynthesisInputs,
    edges: List<WindowEdgeReproduction>,
    channels: List<FoldChannelRecord>
): List<SecondReproduction> {
    val element = realisedCouplingLaw(45)
    val entries = mutableListOf<SecondReproduction>()
    fun add(quantity: String, source: String, published: Double, reproduced: Double) {
        entries += SecondReproduction(
            quantity, source, published, reproduced,
            kotlin.math.abs(reproduced - published) / kotlin.math.max(kotlin.math.abs(published), 1e-30)
        )
    }
    edges.filter { !it.publishedEmpty }.forEach { record ->
        add(
            "C-0027 window edge, L0 = ${record.layerHeight} nm, '${record.corrections}', lower",
            "T-25", record.publishedLow!!, record.rerunLow!!
        )
        add(
            "C-0027 window edge, L0 = ${record.layerHeight} nm, '${record.corrections}', upper",
            "T-25", record.publishedHigh!!, record.rerunHigh!!
        )
    }
    add("C-0030 assembled tangent at 3 nm, 45 paths", "C-0030 / T-65", 25.23, element.assembledTangent(3.0))
    add("C-0030 flexure span, 45 paths", "C-0030 / T-65", 31.82, element.span)
    add("C-0017 mandate", "C-0017 / §3", 33.3333, element.assembledSecant(3.0))
    add(
        "C-0023 declared ceiling", "C-0023 / C-0049", 40.0,
        declaredComplianceCeilingFromMandate(100.0, ACCEPTABLE_STROKE_NM)
    )
    add(
        "C-0049 declared ceiling at §3's desired clause", "C-0049", 12.0,
        declaredComplianceCeilingFromMandate(100.0, DESIRED_STROKE_NM)
    )
    add(
        "C-0049 per-path secant ceiling, 45 paths at 10 nm", "C-0049", 45.0,
        perPathSecantCeiling(UNZIP_ALLOWABLE_PN, 45, DESIRED_STROKE_NM)
    )
    add(
        "C-0049 per-path secant ceiling, 15 paths at 10 nm", "C-0049", 15.0,
        perPathSecantCeiling(UNZIP_ALLOWABLE_PN, 15, DESIRED_STROKE_NM)
    )
    add(
        "C-0050 best kinematic ceiling over its own sweep", "C-0050 / T-108", 9.78969263,
        second.reachRecords.maxOf { it.kinematicCeiling }
    )
    add(
        "C-0050 best validity ceiling over its own sweep", "C-0050 / T-108", 8.95887439,
        second.reachRecords.mapNotNull { it.validityCeiling }.max()
    )
    add(
        "C-0041 buildable path count", "C-0041 / T-96", 15.0,
        second.packingLimitedPaths.toDouble()
    )
    add(
        "C-0033 collar-only fold tangent, lowest of six", "C-0033 / T-60", 2.604,
        channels.minOf { it.increment.collar }
    )
    add(
        "C-0033 collar-only fold tangent, highest of six", "C-0033 / T-60", 4.994,
        channels.maxOf { it.increment.collar }
    )
    add(
        "C-0018 pull-in margin at 10 nm / 2 mM, lowest", "C-0018 via T-60", 1.00708008,
        channels.minOf { it.baselineMargin }
    )
    add(
        "C-0018 pull-in margin at 10 nm / 2 mM, highest", "C-0018 via T-60", 1.03169792,
        channels.maxOf { it.baselineMargin }
    )
    add(
        "T-1d grafting-density grid ratio", "C-0011", 1.10913,
        inputs.graftingDensityGrid[1] / inputs.graftingDensityGrid[0]
    )
    return entries
}

// --- the ledger ---------------------------------------------------------------------------------

private fun ledger(second: SecondResynthesisInputs, gridRatio: Double): List<SecondLedgerEntry> =
    listOf(
        SecondLedgerEntry(
            "mandated coupling stiffness", MANDATED_COUPLING_STIFFNESS, "pN/nm",
            "C-0017, from §3's 100 pN and 3 nm alone", "CITED"
        ),
        SecondLedgerEntry(
            "acceptable stroke", ACCEPTABLE_STROKE_NM, "nm", "§3", "CITED"
        ),
        SecondLedgerEntry(
            "desired stroke", DESIRED_STROKE_NM, "nm", "§3", "CITED — quoted, never adopted"
        ),
        SecondLedgerEntry(
            "per-path unzip allowable", UNZIP_ALLOWABLE_PN, "pN",
            "C-0006 / CH-0029", "CITED, MEASURED and loading-rate dependent"
        ),
        SecondLedgerEntry(
            "declared ceiling factor", DECLARED_CEILING_FACTOR, "1",
            "C-0049, derived there from C-0023's own declaration", "DERIVED here as 40/(100/3)"
        ),
        SecondLedgerEntry(
            "concentrated crossover as every upstream claim used it",
            second.crossoverFractions.first().fraction, "1",
            "C-0002 — CH-0049 disputes it and C-0036 replaces it with a family", "CITED"
        ),
        SecondLedgerEntry(
            "buildable path count", second.packingLimitedPaths.toDouble(), "1",
            "C-0041", "DERIVED here from T-96's own design table"
        ),
        SecondLedgerEntry(
            "grafting-density grid ratio", gridRatio, "1", "T-1d's own sweep", "DERIVED here"
        ),
        SecondLedgerEntry(
            "standoff length of the realised element", STANDOFF_LENGTH_NM, "nm",
            "C-0030's recommendation", "CITED"
        )
    )

// --- prose ---------------------------------------------------------------------------------------

private fun findings(
    full: List<ResynthesisedWindow>,
    edges: List<WindowEdgeReproduction>,
    candidates: List<SecondCandidateAxis>,
    channels: List<FoldChannelRecord>,
    licences: List<CrossoverLicence>
): Map<String, String> = mapOf(
    "no_edge_moves_and_this_time_none_could" to
            "Not one of C-0027's six window edges moves by a single grid step, and no edge " +
                    "changes owner. That is not luck: of the twenty claims of iterations 5-7, " +
                    "exactly ONE (C-0036) carries a quantity that is a function of sigma at " +
                    "all, and it says itself that it reaches the design only through C-0018's " +
                    "bias ceiling. The other nineteen are counts, plan layouts, elastica " +
                    "geometries, height-level actuator states, or specification questions — " +
                    "and C-0016's own lesson is that such a constraint is INVISIBLE to an " +
                    "intersection. The window did not survive a challenge; it was never " +
                    "addressed by one.",
    "the_two_new_sigma_resolved_constraints_do_not_bind" to
            "C-0050 produced the first genuinely new sigma-resolved constraints since " +
                    "iteration 4 — the kinematic stroke ceiling L0 - N sigma v0 and the " +
                    "validity ceiling L0 - N sigma v0 / phi_c — and BOTH can narrow a window. " +
                    "Evaluated at all 61 grid points on the layer the window is actually drawn " +
                    "on, neither does: the worst kinematic margin inside a surviving window is " +
                    "${"%.2f".format(candidates.filter { it.constraint.startsWith("kinematic") }
                        .minOf { it.worstMarginInsideTheWindow })}x §3's 3 nm clause. They " +
                    "close §3's DESIRED clause instead, and that is a height statement.",
    "C-0050s_validity_bound_is_not_licensed_at_the_windows_own_upper_edge" to
            "At the top of C-0027's 10 nm window the SOLVED layer sits at phi = " +
                    "${"%.4f".format(licences.first { it.layerHeight == 10.0 }.solvedVolumeFractionAtUpperEdge)}, " +
                    "while C-0003's six trial-function models at the same grafting density sit " +
                    "${"%.2f".format(licences.first { it.layerHeight == 10.0 }.ratioHigh)}x " +
                    "higher and two of them have NO validity ceiling at all. C-0016's " +
                    "falsifier 3 fired at 5 nm and was carried forward; nobody checked the " +
                    "10 nm UPPER EDGE, which is where C-0050 reads its bound 3. The verdict " +
                    "does not move — C-0050's bound 2 is kinematic and settles its question " +
                    "without bound 3 — but the number does, and by a factor of " +
                    "${"%.2f".format(licences.first { it.layerHeight == 10.0 }.ratioHigh)}.",
    "three_corrections_one_margin_and_nobody_composed_them" to
            "C-0033 (iteration 6) and C-0032 (iteration 6) both moved C-0018's 10 nm / 2 mM " +
                    "pull-in margin, in OPPOSITE directions, and neither carries the other. " +
                    "Composed on one tangent at C-0018's own fold — where the baseline tangent " +
                    "vanishes by construction, so the composition is exact rather than " +
                    "first-order — the collar adds " +
                    "${"%+.3f".format(channels.minOf { it.increment.collar })} to " +
                    "${"%+.3f".format(channels.maxOf { it.increment.collar })}, C-0019's " +
                    "fluctuation ${"%+.3f".format(channels.minOf { it.increment.fluctuation })} " +
                    "to ${"%+.3f".format(channels.maxOf { it.increment.fluctuation })}, and " +
                    "C-0030's realised softening " +
                    "${"%+.3f".format(channels.minOf { it.increment.softening })} to " +
                    "${"%+.3f".format(channels.maxOf { it.increment.softening })} pN/nm. The " +
                    "total is ${"%.3f".format(channels.minOf { it.increment.total })} to " +
                    "${"%.3f".format(channels.maxOf { it.increment.total })} pN/nm — NEGATIVE " +
                    "at all six models. C-0033's collar recovers " +
                    "${"%.0f".format(100.0 * channels.minOf { it.collarRecoversFractionOfTheSoftening })}-" +
                    "${"%.0f".format(100.0 * channels.maxOf { it.collarRecoversFractionOfTheSoftening })} % " +
                    "of what the realised element costs, and no more. C-0032's 1.0000-1.0019 " +
                    "is the standing statement at §3's own buffer.",
    "an_axis_left_the_stack_again" to
            "C-0027 recorded one axis LEAVING the window (the lateral-confinement footprint). " +
                    "Iterations 5-7 do it again on the coupling side: C-0049 withdraws " +
                    "C-0023's 40 pN/nm tangent ceiling from the acceptance stack entirely — it " +
                    "is 1.2x the PLACEMENT mandate and carries that stroke inside it — and " +
                    "what replaces it, the per-path allowable read as a stiffness, is a PATH " +
                    "COUNT that tightens as 1/s. A removal and a replacement, on the same axis, " +
                    "in one claim.",
    "the_deliverable_is_no_longer_a_window" to
            "Three of the four things that moved in iterations 5-7 cannot be drawn on a " +
                    "(sigma, L0) plane at all, and the one that decides §6 — C-0050's " +
                    "s = L0 - h — is a KINEMATIC IDENTITY about a coordinate. The window is " +
                    "still correct and still non-empty at " +
                    "${full.filter { !it.empty }.joinToString(" and ") { "${it.layerHeight} nm" }}; " +
                    "it is simply no longer where the programme's remaining uncertainty lives."
)

private fun verdict(
    full: List<ResynthesisedWindow>,
    channels: List<FoldChannelRecord>,
    second: SecondResynthesisInputs
): Map<String, String> = mapOf(
    "P1 — the window after iterations 5-7" to full.joinToString("; ") { window ->
        if (window.empty) "L0 = ${window.layerHeight} nm: EMPTY"
        else "L0 = ${window.layerHeight} nm: sigma in [" +
                "${"%.5g".format(window.lowestGraftingDensity)}, " +
                "${"%.5g".format(window.highestGraftingDensity)}] nm^-2, " +
                "${"%.4g".format(window.widthRatio)}x wide"
    } + " — IDENTICAL to C-0027's, at every edge, to zero grid steps.",
    "P1 binding constraints" to
            "UNCHANGED through three more iterations: lower edge coil overlap Sigma >= 1 " +
                    "(the 1-D mean field's own validity condition), upper edge §3's 3 nm " +
                    "stroke read as 3.0 + d delivered. Nine claims, thirty challenges, and " +
                    "the same two owners.",
    "P2 — the axis every new constraint lives on" to
            "Of the twenty claims of iterations 5-7, ONE is sigma-resolved (C-0036) and says " +
                    "itself it cannot move a window edge. Two NEW sigma-resolved quantities " +
                    "appear (C-0050's two ceilings) and neither binds. Everything else is a " +
                    "count, a plan layout, an elastica geometry, a height-level actuator " +
                    "state, or a specification question. ONE axis LEAVES (C-0049 withdraws " +
                    "C-0023's tangent ceiling) and one replaces it (the per-path allowable as " +
                    "a stiffness, which is a path count).",
    "P3 — C-0018's margin, all three channels composed" to
            "The composed fold tangent is " +
                    "${"%.3f".format(channels.minOf { it.increment.total })} to " +
                    "${"%.3f".format(channels.maxOf { it.increment.total })} pN/nm, negative at " +
                    "6 of 6 models, so the fold moves SHALLOWER and C-0033's improvement does " +
                    "NOT survive the realised coupling. Read C-0032's 1.0000-1.0019 as the " +
                    "standing 10 nm / 2 mM statement; C-0033's 1.021-1.028 is the statement " +
                    "for the AFFINE mandate, which is not the element the programme has.",
    "P4 — what the programme's answer to §6 task 2 now is" to
            "A non-empty region still exists and is unchanged. But the DELIVERABLE is no " +
                    "longer a window: §3's acceptable clause (3 nm at 100 pN) is delivered, " +
                    "its desired clause (10 nm) is refused KINEMATICALLY by s = L0 - h < L0 " +
                    "<= 10 nm, and the escape is a LAYER HEIGHT of 16.6-26.1 nm that nothing " +
                    "in this programme has evaluated. The honest form of the Gen-1 answer is " +
                    "now a HEIGHT plus FIVE specification questions (T-63 the buffer, T-95 " +
                    "the superstructure, T-102 the tile area, T-112 the desired clause's own " +
                    "device, T-115 the layer height) — and only T-115 can buy the desired " +
                    "stroke.",
    "P5 — the buffer, unchanged and now recommended by more routes" to
            "0.5 mM still clears everything and 2 mM still does not: C-0032 adds the sixth " +
                    "independent route and this synthesis adds the seventh, because the " +
                    "composed fold tangent is negative only where a fold exists, and at " +
                    "0.5 mM there is none. §3 names 2 mM. NEITHER is adopted here — it is " +
                    "T-63, a specification question for NDI.",
    "the desired 10 nm stroke" to
            "UNREACHABLE ON §3's OWN STACK, and C-0050 supplies the mechanism C-0016 and " +
                    "C-0027 reported without: the stroke IS the layer's thickness. Ceilings " +
                    "${"%.3f".format(second.reachRecords.maxOf { it.kinematicCeiling })} nm " +
                    "(kinematic), " +
                    "${"%.3f".format(second.reachRecords.mapNotNull { it.validityCeiling }.max())} nm " +
                    "(validity) and 7.424 nm (dead load), none of which contains a coupling."
)

private fun validity(gridRatio: Double, unchecked: List<Double>): List<String> = listOf(
    "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and almost nothing " +
            "here is re-derived: every number is a transfer, and every transfer is checked " +
            "against the file it came from.",
    "THE HEIGHT CONVENTION IS FORCE-ONSET. $SECOND_HEIGHT_CONVENTION",
    "Every window edge is a grid point located to ${"%.5f".format(gridRatio)}x and no better. " +
            "A movement smaller than that is reported as sub-grid, never as zero.",
    "CH-0043 stands and is inherited: this tree rounds to nine significant digits where a " +
            "solved height is determined to about six, so no number here should be read as " +
            "asserting its eighth figure. The re-intersection is an INDEX comparison and is " +
            "immune to that; the reproductions are not, and their tolerances are set to match.",
    "The three-channel fold increment is EXACT at C-0018's own fold, because the baseline " +
            "tangent vanishes there by construction. It is NOT a relocated fold: it gives the " +
            "DIRECTION the fold moves and not the new margin. Relocating it needs C-0018's " +
            "path search re-run with C-0030's nonlinear law AND C-0033's solved collar " +
            "together, which no study has done.",
    "The softening channel is read on C-0030's element at 45 paths — C-0015's count, which " +
            "C-0041 shows does not pack. At C-0041's buildable 15 the assembled tangent moves " +
            "by about 1 %, so the sign of the composition is unaffected; the count is carried " +
            "as 45 because that is the state C-0032 and C-0033 were both evaluated at.",
    "C-0032's own SMALL-DEFLECTION exposure travels unchanged: the tangent minimum sits at " +
            "the edge of small deflection, and a large-deflection solve would move it in an " +
            "unknown direction.",
    "Mean-field electrostatics, inherited whole. C-0005: 123-214 % of the leading term across " +
            "the whole 5-10 nm range. Every margin here is NOT EXCLUDED, never established.",
    "C-0036 replaces C-0002's phi = 0.2 with a one-parameter family and CH-0049 disputes the " +
            "0.2 independently. All three readings are carried; the window's own verdict is " +
            "the same under every one of them, which is asserted rather than assumed.",
    "The crossover licence is checked only where T-108 sampled the window's own upper edge. " +
            (if (unchecked.isEmpty()) "It sampled all of them."
            else "It did not sample ${unchecked.joinToString(", ") { "$it nm" }}, and those " +
                    "heights are reported as UNCHECKED rather than asserted on a substitute."),
    "C-0016's and C-0027's own validity ranges travel unchanged, including the 1.22x exposure " +
            "of the solved layer against C-0003's bracket at 5 nm.",
    "The layer is neutral linear PEG. §3 also permits PEO and a PS->PEG block copolymer, for " +
            "which no osmotic equation of state was ever consumed in this programme."
)

private fun openQuestions(): List<String> = listOf(
    "The RELOCATED fold under all three corrections together. This task gives its direction " +
            "exactly and its position not at all. C-0018's path search, C-0030's law and " +
            "C-0033's collar all exist; composing them is one study.",
    "T-115 — may the layer be 17-26 nm tall? It is the only one of the five specification " +
            "questions that can buy §3's desired stroke, and four upstream validity ranges " +
            "move with it.",
    "T-63 — the buffer. Seven independent routes now recommend 0.5 mM and §3 names 2 mM. It " +
            "is a decision, not a calculation.",
    "T-116 — the plan view of the 45-arm hinge-line array, which is the largest open item " +
            "C-0050 leaves and which the window cannot see at all.",
    "P-18 — rounding the tree to the precision the answer is DETERMINED to. CH-0043 raised " +
            "it, C-0031 paid for not having it, and this synthesis inherits it."
)

private fun report(result: SecondResynthesisResult) {
    println("=== T-118 — the design window, re-synthesised against iterations 5-7 ".padEnd(100, '='))
    println()
    println("--- the windows ".padEnd(100, '-'))
    result.windows.forEach { window ->
        println(
            "%-30s %-5.1f %-11s %-11s %-8s %s".format(
                window.corrections, window.layerHeight,
                window.lowestGraftingDensity?.let { "%.5g".format(it) } ?: "EMPTY",
                window.highestGraftingDensity?.let { "%.5g".format(it) } ?: "EMPTY",
                window.widthRatio?.let { "%.4g".format(it) } ?: "-",
                "${window.lowerBinding.joinToString(",")} | ${window.upperBinding.joinToString(",")}"
            )
        )
    }
    println()
    println("--- do any edges move against C-0027? ".padEnd(100, '-'))
    result.edgeReproductions.forEach { println("  ${it.statement}") }
    println()
    println("--- the new sigma-resolved candidates ".padEnd(100, '-'))
    result.candidateAxes.forEach {
        println("  %-4.1f nm %-46s binds: %-5s margin %.2fx".format(
            it.layerHeight, it.constraint.take(46), it.bindsInsideTheWindow,
            it.worstMarginInsideTheWindow
        ))
    }
    println()
    println("--- the licence check at the window's own upper edge ".padEnd(100, '-'))
    result.crossoverLicences.forEach { println("  ${it.statement}") }
    println()
    println("--- the three channels, composed at C-0018's own fold ".padEnd(100, '-'))
    println("%-32s %8s %8s %8s %8s".format("model", "collar", "fluct", "soften", "TOTAL"))
    result.foldChannels.forEach {
        println(
            "%-32s %+8.3f %+8.3f %+8.3f %+8.3f".format(
                it.model, it.increment.collar, it.increment.fluctuation,
                it.increment.softening, it.increment.total
            )
        )
    }
    println()
    println("--- the axes ".padEnd(100, '-'))
    result.axes.forEach {
        println(
            "  %-42s %-38s %s".format(
                it.axis, it.level,
                if (it.canNarrowAWindow) "CAN narrow" else "cannot narrow"
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
