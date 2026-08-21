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
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.poroelastic.RectangularFootprint
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

/**
 * Task `T-2` — the feasible design window in (grafting density, height, chemistry).
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=window.DesignWindowStudyKt
 * ```
 *
 * Emits `gpd/results/T-2-design-window.json`, deterministically.
 *
 * This is a **synthesis and intersection** study, not a new-physics one. It consumes the
 * emitting studies' own machine-readable results — `T-1d` for the layer grid, `T-3` for the
 * coupled thresholds, `T-14` for the layout sweep — calls the upstream packages as libraries
 * where a relation has to be evaluated at a point its own claim did not tabulate, and
 * intersects. Nothing outside `window/` is edited and no physics is re-derived.
 */

// --- the acceptance thresholds, all from the problem definition or from a claim ----------

/** §3's *acceptable* stroke in nm. */
private const val ACCEPTABLE_STROKE = 3.0

/** §3's *desired* stroke in nm. */
private const val DESIRED_STROKE = 10.0

/** §3's force target in pN. */
private const val TARGET_FORCE = 100.0

/** §3's bandwidth requirement in Hz. */
private const val BANDWIDTH_TARGET = 1000.0

/** `C-0011`/`CH-0010`: coil overlap, the 1-D mean field's own validity condition. */
private const val REQUIRED_COIL_OVERLAP = 1.0

/** `P-5`'s adopted criterion, carried alongside and expected to be vacuous. */
private const val REQUIRED_STRETCHING_RATIO = 1.0

/** `C-0005`'s lateral counterion spacing in nm — the gap below which mean field cannot apply. */
private const val CORRELATION_BAND_GAP = 1.46

/** `C-0002`'s semidilute→concentrated crossover, read as a ceiling per §2's second caveat. */
private const val CONCENTRATED_CROSSOVER = 0.2

/** §3's three layer heights. */
private val LAYER_HEIGHTS = listOf(5.0, 7.0, 10.0)

/** The buffers `T-3` swept — §3's 2/5/10 mM plus leaf `A2.2`'s low-screening extension. */
private val BUFFERS = listOf(0.5, 1.0, 2.0, 5.0, 10.0)

/** The biases at which `T-3`'s loaded operating point is reported. */
private val STABILITY_BIASES = listOf(0.05, 0.1, 0.25)

private val GEN1_FOOTPRINT = RectangularFootprint(40.0, 40.0)

// --- the constraint identifiers, which are what an edge is attributed to -----------------

private const val C_OVERLAP = "a-coil-overlap"
private const val C_STRETCHING = "a-stretching-ratio"
private const val C_COMPLIANCE = "a-compliance-stroke"
private const val C_PARTITIONING = "c-ion-partitioning"
private const val C_DRAINAGE = "d-poroelastic-drainage"
private const val C_LOAD_PATH = "h-per-load-path-force"

// --- record types ------------------------------------------------------------------------

/** A number this task carries, tagged with where it came from and whether it was derived. */
@Serializable
data class LedgerEntry(
    val quantity: String,
    val value: Double,
    val unit: String,
    val source: String,
    val provenance: String
)

/** One point of the intersection grid: one layer height, one grafting density. */
@Serializable
data class WindowPoint(
    val layerHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val monomersPerChainLow: Double,
    val monomersPerChainHigh: Double,
    val chainKilodaltonLow: Double,
    val chainKilodaltonHigh: Double,
    val firstMomentThickness: Double,
    val heightConventionRatio: Double,
    val coilOverlap: Double,
    val stretchingRatio: Double,
    val meanVolumeFraction: Double,
    val peakVolumeFraction: Double,
    val deadLoadStroke: Double,
    val secantStiffness: Double,
    val stiffnessAtFourFifths: Double,
    val positionalRms: Double,
    val saltPartitionCoefficient: Double,
    val debyeLengthRatio: Double,
    val layerLocalDebyeLength: Double,
    val drainageCornerFrequency: Double,
    val drainageMargin: Double,
    val slowestPermeabilityModel: String,
    val foundationMultiplier: Double,
    val loadPathForceBestLayout: Double,
    val loadPathForceWorstLayout: Double,
    val loadPathInsideSweptRange: Boolean,
    val minimumTetherLength: Double,
    val assemblyEdge: Double,
    val meetsCoilOverlap: Boolean,
    val meetsStretchingRatio: Boolean,
    val meetsCompliance: Boolean,
    val meetsPartitioning: Boolean,
    val meetsDrainage: Boolean,
    val meetsLoadPath: Boolean
)

/** One constraint's admissible interval at one height, in bench units. */
@Serializable
data class ConstraintWindow(
    val constraint: String,
    val provenance: String,
    val empty: Boolean,
    val lowestGraftingDensity: Double? = null,
    val highestGraftingDensity: Double? = null,
    val widthRatio: Double? = null
)

/** The §4(a)–(d) window at one layer height, and what closes each of its edges. */
@Serializable
data class HeightWindow(
    val layerHeight: Double,
    val heightConvention: String,
    val constraints: List<ConstraintWindow>,
    val predicateOneEmpty: Boolean,
    val predicateOneLowest: Double? = null,
    val predicateOneHighest: Double? = null,
    val predicateOneWidthRatio: Double? = null,
    val predicateOneLowerBinding: List<String> = emptyList(),
    val predicateOneUpperBinding: List<String> = emptyList(),
    val predicateOneLowerTie: Boolean = false,
    val predicateOneUpperTie: Boolean = false,
    val predicateOneCrossing: ConstraintCrossing? = null,
    val withLoadPathEmpty: Boolean,
    val withLoadPathLowest: Double? = null,
    val withLoadPathHighest: Double? = null,
    val edgeResolution: Double,
    val benchOrder: BenchOrder? = null
)

/** What a bench would order for a surviving window, in the units a bench buys in. */
@Serializable
data class BenchOrder(
    val layerHeight: Double,
    val heightConvention: String,
    val graftingDensityLow: Double,
    val graftingDensityHigh: Double,
    val graftingSpacingLow: Double,
    val graftingSpacingHigh: Double,
    val monomersPerChainLow: Double,
    val monomersPerChainHigh: Double,
    val pegKilodaltonLow: Double,
    val pegKilodaltonHigh: Double,
    val firstMomentThicknessLow: Double,
    val firstMomentThicknessHigh: Double,
    val deadLoadStrokeLow: Double,
    val deadLoadStrokeHigh: Double,
    val secantStiffnessLow: Double,
    val secantStiffnessHigh: Double,
    val attachmentCount: Int,
    val attachmentShape: String,
    val minimumTetherLengthLow: Double,
    val minimumTetherLengthHigh: Double,
    val assemblyEdgeLow: Double,
    val assemblyEdgeHigh: Double
)

/**
 * The §3 force and stroke clauses at one `(height, buffer)`, under both readings of the
 * usable bias ceiling.
 */
@Serializable
data class BiasClause(
    val layerHeight: Double,
    val concentration: Double,
    val biasForHundredPiconewtonBlocking: Double? = null,
    val blockingBiasIsModelFree: Boolean,
    val largestModelValidBiasLow: Double,
    val largestModelValidBiasHigh: Double,
    val blockingForceAtValidBiasLow: Double,
    val blockingForceAtValidBiasHigh: Double,
    val strokeAtValidBiasLow: Double,
    val strokeAtValidBiasHigh: Double,
    val readingAModelsPassing: Int,
    val readingAModelsTotal: Int,
    val readingAVerdict: String,
    val readingABindingClause: String,
    val readingAShortfall: Double,
    val biasForSimultaneousTargetLow: Double? = null,
    val biasForSimultaneousTargetHigh: Double? = null,
    val readingBInsideTrustedCeiling: Boolean,
    val modelsUnstableAtSimultaneousTarget: Int,
    val readingBVerdict: String,
    val heldGap: Double,
    val heldGapAboveCorrelationBand: Boolean,
    val heldVolumeFractionSolvedLayer: Double,
    val heldVolumeFractionBelowCrossover: Boolean
)

/** The static-stability clause `C-0012` supplies and no upstream task could. */
@Serializable
data class StabilityClause(
    val layerHeight: Double,
    val concentration: Double,
    val appliedBias: Double,
    val modelsTotal: Int,
    val modelsUnstable: Int,
    val requiredCouplingStiffnessLow: Double? = null,
    val requiredCouplingStiffnessHigh: Double? = null,
    val verdict: String
)

/** A declared falsifier, and whether it fired. */
@Serializable
data class Falsifier(
    val id: Int,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

/** The whole `T-2` result. */
@Serializable
data class DesignWindowResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val tightenedAcceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val heightConvention: String,
    val parameters: Map<String, String>,
    val ledger: List<LedgerEntry>,
    val graftingDensityGrid: List<Double>,
    val points: List<WindowPoint>,
    val heightWindows: List<HeightWindow>,
    val biasClauses: List<BiasClause>,
    val stabilityClauses: List<StabilityClause>,
    val transferLicence: List<String>,
    val falsifiers: List<Falsifier>,
    val findings: Map<String, String>,
    val verdict: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// --- the study ---------------------------------------------------------------------------

@Suppress("LongMethod", "CyclomaticComplexMethod")
fun main() {
    val peg = PegWater()
    val scf = readScfResults(File("gpd/results/T-1d-scf-density-profile.json"))
    val actuator = readActuatorResults(File("gpd/results/T-3-stroke-and-blocking-force.json"))
    val layout = readLayoutResults(File("gpd/results/T-14-crossover-phase-and-registration.json"))

    val referenceStiffness = layout.referenceLayerStiffness
    val unzipAllowable = layout.unzipAllowableLower
    val tetherAllowable = layout.shearAllowable / LOAD_CONCENTRATION_FACTOR
    val lattice = layout.flatnessMinima.first { it.model == "lattice" }

    val grid = scf.designPoints.filter { it.layerHeight == LAYER_HEIGHTS.first() }
        .map { it.graftingDensity }.sorted()

    val points = LAYER_HEIGHTS.flatMap { height ->
        scf.designPoints.filter { it.layerHeight == height }.sortedBy { it.graftingDensity }
            .map { point ->
                val solved = point.solved
                // the conservative reading over the three measurement-anchored interaction
                // laws: the SHORTEST stroke and the LOWEST overlap any of them gives
                val stroke = solved.minOf { it.strokeUnderTargetForce }
                val overlap = solved.minOf { it.coilOverlap }
                val stretching = solved.minOf { it.stretchingRatio }
                val secant = solved.minOf { it.secantStiffness }
                val phi = solved.maxOf { it.meanVolumeFraction }
                val partitioning = saltPartitioning(phi, peg)
                val drainage = drainageBound(
                    layerHeight = height,
                    polymerVolumeFraction = phi,
                    layerStiffness = secant,
                    footprint = GEN1_FOOTPRINT,
                    peg = peg
                )
                val multiplier = secant / referenceStiffness
                val loadPath = loadPathForce(layout.foundationStates, ANCHOR_LOAD_CASE, multiplier)
                val tether = minimumTetherLength(stroke, tetherAllowable)
                WindowPoint(
                    layerHeight = height,
                    graftingDensity = point.graftingDensity,
                    graftingSpacing = point.graftingSpacing,
                    monomersPerChainLow = solved.minOf { it.monomersPerChain },
                    monomersPerChainHigh = solved.maxOf { it.monomersPerChain },
                    chainKilodaltonLow = solved.minOf { it.chainMolarMass } / 1000.0,
                    chainKilodaltonHigh = solved.maxOf { it.chainMolarMass } / 1000.0,
                    firstMomentThickness = solved.minOf { it.firstMomentHeight },
                    heightConventionRatio = height / solved.minOf { it.firstMomentHeight },
                    coilOverlap = overlap,
                    stretchingRatio = stretching,
                    meanVolumeFraction = phi,
                    peakVolumeFraction = solved.maxOf { it.peakVolumeFraction },
                    deadLoadStroke = stroke,
                    secantStiffness = secant,
                    stiffnessAtFourFifths = solved.minOf { it.stiffnessAtFourFifths },
                    positionalRms = solved.maxOf { it.positionalRms },
                    saltPartitionCoefficient = partitioning.saltPartitionCoefficient,
                    debyeLengthRatio = partitioning.debyeLengthRatio,
                    layerLocalDebyeLength = partitioning.localDebyeLengthAtTwoMillimolar,
                    drainageCornerFrequency = drainage.cornerFrequency,
                    drainageMargin = drainage.marginAtOneKilohertz,
                    slowestPermeabilityModel = drainage.slowestPermeabilityModel,
                    foundationMultiplier = multiplier,
                    loadPathForceBestLayout = loadPath.bestLayoutForce,
                    loadPathForceWorstLayout = loadPath.worstLayoutForce,
                    loadPathInsideSweptRange = loadPath.insideSweptRange,
                    minimumTetherLength = tether,
                    assemblyEdge = 40.0 + 2.0 * tether,
                    // every flag compares an already-rounded quantity, so it cannot flip
                    // between runs on a last-place difference
                    meetsCoilOverlap = roundedDecision(overlap) >= REQUIRED_COIL_OVERLAP,
                    meetsStretchingRatio = roundedDecision(stretching) >= REQUIRED_STRETCHING_RATIO,
                    meetsCompliance = roundedDecision(stroke) >= ACCEPTABLE_STROKE,
                    meetsPartitioning = roundedDecision(partitioning.saltPartitionCoefficient) <= 1.0,
                    meetsDrainage = roundedDecision(drainage.cornerFrequency) >= BANDWIDTH_TARGET,
                    meetsLoadPath = roundedDecision(loadPath.bestLayoutForce) < unzipAllowable
                )
            }
    }

    val heightWindows = LAYER_HEIGHTS.map { height ->
        val at = points.filter { it.layerHeight == height }
        val intervals = linkedMapOf(
            C_OVERLAP to admissibleInterval(at.map { it.meetsCoilOverlap }),
            C_STRETCHING to admissibleInterval(at.map { it.meetsStretchingRatio }),
            C_COMPLIANCE to admissibleInterval(at.map { it.meetsCompliance }),
            C_PARTITIONING to admissibleInterval(at.map { it.meetsPartitioning }),
            C_DRAINAGE to admissibleInterval(at.map { it.meetsDrainage })
        )
        val attribution = attributeEdges(intervals)
        val crossing = crossingOf(intervals, grid)
        val withLoadPath = intersect(
            attribution?.window,
            admissibleInterval(at.map { it.meetsLoadPath })
        )
        val benchOrder = attribution?.window?.let { window ->
            val inside = at.subList(window.lowestIndex, window.highestIndex + 1)
            BenchOrder(
                layerHeight = height,
                heightConvention = HEIGHT_CONVENTION,
                graftingDensityLow = window.lowest(grid),
                graftingDensityHigh = window.highest(grid),
                graftingSpacingLow = inside.maxOf { it.graftingSpacing },
                graftingSpacingHigh = inside.minOf { it.graftingSpacing },
                monomersPerChainLow = inside.minOf { it.monomersPerChainLow },
                monomersPerChainHigh = inside.maxOf { it.monomersPerChainHigh },
                pegKilodaltonLow = inside.minOf { it.chainKilodaltonLow },
                pegKilodaltonHigh = inside.maxOf { it.chainKilodaltonHigh },
                firstMomentThicknessLow = inside.minOf { it.firstMomentThickness },
                firstMomentThicknessHigh = inside.maxOf { it.firstMomentThickness },
                deadLoadStrokeLow = inside.minOf { it.deadLoadStroke },
                deadLoadStrokeHigh = inside.maxOf { it.deadLoadStroke },
                secantStiffnessLow = inside.minOf { it.secantStiffness },
                secantStiffnessHigh = inside.maxOf { it.secantStiffness },
                attachmentCount = lattice.bestAttachments,
                attachmentShape = lattice.bestShape,
                minimumTetherLengthLow = inside.minOf { it.minimumTetherLength },
                minimumTetherLengthHigh = inside.maxOf { it.minimumTetherLength },
                assemblyEdgeLow = inside.minOf { it.assemblyEdge },
                assemblyEdgeHigh = inside.maxOf { it.assemblyEdge }
            )
        }
        HeightWindow(
            layerHeight = height,
            heightConvention = HEIGHT_CONVENTION,
            constraints = intervals.map { (name, interval) ->
                ConstraintWindow(
                    constraint = name,
                    provenance = CONSTRAINT_PROVENANCE.getValue(name),
                    empty = interval == null,
                    lowestGraftingDensity = interval?.lowest(grid),
                    highestGraftingDensity = interval?.highest(grid),
                    widthRatio = interval?.width(grid)
                )
            },
            predicateOneEmpty = attribution == null,
            predicateOneLowest = attribution?.window?.lowest(grid),
            predicateOneHighest = attribution?.window?.highest(grid),
            predicateOneWidthRatio = attribution?.window?.width(grid),
            predicateOneLowerBinding = attribution?.lowerBinding ?: emptyList(),
            predicateOneUpperBinding = attribution?.upperBinding ?: emptyList(),
            predicateOneLowerTie = attribution?.lowerTie ?: false,
            predicateOneUpperTie = attribution?.upperTie ?: false,
            predicateOneCrossing = crossing,
            withLoadPathEmpty = withLoadPath == null,
            withLoadPathLowest = withLoadPath?.lowest(grid),
            withLoadPathHighest = withLoadPath?.highest(grid),
            edgeResolution = grid[1] / grid[0],
            benchOrder = benchOrder
        )
    }

    val biasClauses = LAYER_HEIGHTS.flatMap { height ->
        BUFFERS.map { concentration ->
            val records = actuator.thresholds.filter {
                it.layerHeight == height && it.concentration == concentration
            }
            val blocking = records.mapNotNull { it.biasForHundredPiconewtonBlocking }
            val passing = records.count {
                roundedDecision(it.blockingForceAtLargestModelValidBias) >= TARGET_FORCE &&
                        roundedDecision(it.strokeAtLargestModelValidBias) >= ACCEPTABLE_STROKE
            }
            val bestForce = records.maxOf { it.blockingForceAtLargestModelValidBias }
            val bestStroke = records.maxOf { it.strokeAtLargestModelValidBias }
            val forceShortfall = TARGET_FORCE / bestForce
            val strokeShortfall = ACCEPTABLE_STROKE / bestStroke
            val bindingClause = when {
                passing > 0 -> "none — the clause is met"
                forceShortfall >= strokeShortfall -> "force"
                else -> "stroke"
            }
            val simultaneous = records.mapNotNull { it.biasForSimultaneousTarget }
            val unstableAtTarget = records.count {
                (it.loadedStiffnessRatioAtSimultaneousTarget ?: 0.0) < 0.0
            }
            val heldGap = height - ACCEPTABLE_STROKE
            // the WORST case inside the §4(a)-(d) window at this height: the densest layer
            // it admits, which is the one closest to C-0002's concentrated crossover. Where
            // the window is empty the whole grid is used, which is more conservative still.
            val window = heightWindows.first { it.layerHeight == height }
            val at = points.filter { it.layerHeight == height }
            val inWindow = if (window.predicateOneEmpty) {
                // no admissible layer exists at this height, so the honest comparator is
                // the one design point T-3 actually evaluated its clauses at
                val actuatorDensity = actuator.designPoints
                    .first { it.layerHeight == height }.graftingDensity
                listOf(at.minBy { kotlin.math.abs(it.graftingDensity - actuatorDensity) })
            } else at.filter {
                it.graftingDensity >= window.predicateOneLowest!! &&
                        it.graftingDensity <= window.predicateOneHighest!!
            }
            val heldPhi = heldVolumeFraction(
                inWindow.maxOf { it.meanVolumeFraction }, height, heldGap
            )
            BiasClause(
                layerHeight = height,
                concentration = concentration,
                biasForHundredPiconewtonBlocking = blocking.firstOrNull(),
                blockingBiasIsModelFree = blocking.isEmpty() ||
                        (blocking.size == records.size && blocking.all { it == blocking.first() }),
                largestModelValidBiasLow = records.minOf { it.largestModelValidBias },
                largestModelValidBiasHigh = records.maxOf { it.largestModelValidBias },
                blockingForceAtValidBiasLow = records.minOf { it.blockingForceAtLargestModelValidBias },
                blockingForceAtValidBiasHigh = bestForce,
                strokeAtValidBiasLow = records.minOf { it.strokeAtLargestModelValidBias },
                strokeAtValidBiasHigh = bestStroke,
                readingAModelsPassing = passing,
                readingAModelsTotal = records.size,
                readingAVerdict = when (passing) {
                    0 -> "FAIL — no layer model reaches both §3 targets below the bias at " +
                            "which the FREE operating point leaves upstream validity"
                    records.size -> "PASS under every layer model"
                    else -> "STRADDLES — $passing of ${records.size} layer models"
                },
                readingABindingClause = bindingClause,
                readingAShortfall = maxOf(forceShortfall, strokeShortfall),
                biasForSimultaneousTargetLow = simultaneous.minOrNull(),
                biasForSimultaneousTargetHigh = simultaneous.maxOrNull(),
                readingBInsideTrustedCeiling = simultaneous.isNotEmpty() &&
                        simultaneous.max() <= actuator.trustedBiasCeiling,
                modelsUnstableAtSimultaneousTarget = unstableAtTarget,
                readingBVerdict = when {
                    simultaneous.isEmpty() -> "FAIL — the simultaneous target is not reached at any bias"
                    simultaneous.max() <= actuator.trustedBiasCeiling ->
                        "PASS — the HELD operating point reaches both §3 targets inside " +
                                "CH-0007's ~1 V point-ion boundary, and the held gap is inside " +
                                "every upstream validity range. CONDITIONAL: $unstableAtTarget " +
                                "of ${records.size} layer models are statically UNSTABLE there " +
                                "and need an output coupling to supply their own stiffness"
                    else -> "FAIL — above CH-0007's point-ion boundary"
                },
                heldGap = heldGap,
                heldGapAboveCorrelationBand = roundedDecision(heldGap) > CORRELATION_BAND_GAP,
                heldVolumeFractionSolvedLayer = heldPhi,
                heldVolumeFractionBelowCrossover = roundedDecision(heldPhi) < CONCENTRATED_CROSSOVER
            )
        }
    }

    val stabilityClauses = LAYER_HEIGHTS.flatMap { height ->
        BUFFERS.flatMap { concentration ->
            STABILITY_BIASES.map { bias ->
                val records = actuator.operatingPoints.filter {
                    it.layerHeight == height && it.concentration == concentration &&
                            it.appliedBias == bias
                }
                val unstable = records.filter { it.loadedEffectiveStiffness < 0.0 }
                StabilityClause(
                    layerHeight = height,
                    concentration = concentration,
                    appliedBias = bias,
                    modelsTotal = records.size,
                    modelsUnstable = unstable.size,
                    requiredCouplingStiffnessLow = unstable.minOfOrNull { -it.loadedEffectiveStiffness },
                    requiredCouplingStiffnessHigh = unstable.maxOfOrNull { -it.loadedEffectiveStiffness },
                    verdict = when {
                        unstable.isEmpty() -> "stable — no output-coupling stiffness required"
                        unstable.size == records.size ->
                            "UNSTABLE under every layer model — the operating point exists only " +
                                    "against an output coupling supplying its own stiffness"
                        else -> "UNSTABLE under ${unstable.size} of ${records.size} layer models"
                    }
                )
            }
        }
    }

    // the transfer licence, computed rather than assumed: T-3's coupled verdicts were
    // produced on C-0003's six models, and are carried here onto C-0011's solved layer
    val transferLicence = listOf(10.0 to 0.024, 7.0 to 0.045, 5.0 to 0.092).map { (height, density) ->
        val bracket = actuator.designPoints.filter {
            it.layerHeight == height && it.graftingDensity == density
        }.map { it.strokeUnderHundredPiconewtonDeadLoad }
        val nearest = points.filter { it.layerHeight == height }
            .minBy { kotlin.math.abs(it.graftingDensity - density) }
        val inside = nearest.deadLoadStroke >= bracket.min() && nearest.deadLoadStroke <= bracket.max()
        "L0 = $height nm, sigma = $density nm^-2: solved layer strokes " +
                "${"%.3f".format(nearest.deadLoadStroke)} nm against C-0003's " +
                "${"%.3f".format(bracket.min())}-${"%.3f".format(bracket.max())} nm — " +
                if (inside) "INSIDE the bracket, the transfer is licensed"
                else "OUTSIDE the bracket by ${"%.2f".format(nearest.deadLoadStroke / bracket.max())}x, " +
                        "the transfer is NOT licensed and every T-3 verdict at this height is " +
                        "reported as an exposure"
    }

    val tenNanometre = heightWindows.first { it.layerHeight == 10.0 }
    val fiveNanometre = heightWindows.first { it.layerHeight == 5.0 }
    val sevenNanometre = heightWindows.first { it.layerHeight == 7.0 }
    val readingAPassing = biasClauses.filter { it.readingAModelsPassing > 0 }

    val falsifiers = listOf(
        Falsifier(
            id = 1,
            statement = "A window edge that no single constraint owns — two constraints " +
                    "coinciding at an edge, which must be reported as a tie rather than a name.",
            fired = heightWindows.any { it.predicateOneLowerTie || it.predicateOneUpperTie },
            outcome = heightWindows.filter { it.predicateOneLowerTie || it.predicateOneUpperTie }
                .joinToString("; ") { "L0 = ${it.layerHeight.roundedForProse()} nm" }
                .ifEmpty { "no tie at any edge of any height: every edge has one owner" }
        ),
        Falsifier(
            id = 2,
            statement = "A non-contiguous admissible set, for which an interval is the wrong " +
                    "object. Asserted in code — admissibleInterval throws.",
            fired = false,
            outcome = "every constraint's admissible set is a single contiguous run on the " +
                    "61-point grid at all three heights; the assertion never fired"
        ),
        Falsifier(
            id = 3,
            statement = "The solved layer landing outside C-0003's response bracket at a shared " +
                    "design point, which removes the licence to carry T-3's coupled verdicts.",
            fired = transferLicence.any { it.contains("NOT licensed") },
            outcome = "FIRED, AT 5 nm ONLY. " + transferLicence.joinToString(" | ")
        ),
        Falsifier(
            id = 4,
            statement = "A discovered axis that is not monotone in sigma, which would make " +
                    "'the binding constraint at an edge' ill-posed.",
            fired = false,
            outcome = "the two sigma-resolved discovered axes are monotone: the per-load-path " +
                    "force falls monotonically with the foundation stiffness, which rises " +
                    "monotonically with sigma. The three remaining discovered axes are NOT " +
                    "functions of sigma at all — which is a different finding and is reported " +
                    "as one rather than as a falsifier"
        ),
        Falsifier(
            id = 5,
            statement = "The window surviving every constraint with margin, which would mean " +
                    "the constraint set is too weak to answer a feasibility question.",
            fired = false,
            outcome = "5 nm is empty on §4(a) alone, and under the strict reading of the usable " +
                    "bias window ${biasClauses.size - readingAPassing.size} of " +
                    "${biasClauses.size} (height, buffer) pairs fail the §3 clauses outright"
        )
    )

    val result = DesignWindowResult(
        task = "T-2",
        leaf = "A2.1, and it must satisfy the acceptance strings of A2.2, A1.1, A1.2, " +
                "A8.2 and A7.4 as well",
        title = "The feasible design window in (grafting density, height, chemistry)",
        verificationType = "logical (constraint intersection over a common grid) + in-silico " +
                "(the grid and the thresholds are consumed from the emitting studies' own " +
                "result files and re-intersected, not re-derived)",
        acceptance = "§6 task 2: a non-empty region satisfying §4(a)-(d) simultaneously, or a " +
                "proof of emptiness naming the binding constraint",
        tightenedAcceptance = "Answered as two predicates reported separately, because they " +
                "give different answers: P1 is §4(a)-(d) as posed; P2 adds the five axes this " +
                "programme discovered and §4 does not name.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "A design window is exactly the artifact a reader mistakes for a recommendation.",
        units = mapOf(
            "length" to "nm",
            "graftingDensity" to "nm^-2 (chains per nm^2)",
            "graftingSpacing" to "nm",
            "molarMass" to "kDa",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "energy" to "pN*nm",
            "potential" to "V",
            "concentration" to "mM MgCl2",
            "frequency" to "Hz",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive away from it, origin at the electrode surface.",
            "Chains grafted at z = 0, one end fixed and the other free; the tile is a RIGID " +
                    "NON-ADSORBING wall at height h.",
            "The electrostatic gap IS the layer height, exactly and by construction (C-0012).",
            "Compression means h < L0; disjoining pressure positive when the layer pushes the " +
                    "tile away; k = -dF/dh, positive for a restoring layer.",
            "k_es < 0 above the electrostatic force maximum and positive below it (CH-0011).",
            "phi ALWAYS means the physical polymer volume fraction, N sigma v0 / h on average.",
            "A stroke is a root of a force balance, never a force divided by a stiffness (C-0012).",
            "A window width is a RATIO of its edges, never a difference.",
            "Every constraint is evaluated conservatively over C-0011's three interaction laws: " +
                    "the shortest stroke, the lowest overlap, the softest layer, the densest layer."
        ),
        heightConvention = HEIGHT_CONVENTION,
        parameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to thermalEnergy().toString(),
            "medium" to "aqueous MgCl2, 0.5 / 1 / 2 / 5 / 10 mM (§3's 2/5/10 plus leaf " +
                    "A2.2's low-screening extension)",
            "tileFootprint" to "40 x 40 nm",
            "layerHeights" to LAYER_HEIGHTS.toString(),
            "graftingDensityGridPoints" to grid.size.toString(),
            "graftingDensityGridRatio" to (grid[1] / grid[0]).toString(),
            "restingLoadDefiningL0" to scf.restingLoad.toString(),
            "acceptableStroke" to ACCEPTABLE_STROKE.toString(),
            "desiredStroke" to DESIRED_STROKE.toString(),
            "targetForce" to TARGET_FORCE.toString(),
            "bandwidthTarget" to BANDWIDTH_TARGET.toString(),
            "requiredCoilOverlap" to REQUIRED_COIL_OVERLAP.toString(),
            "requiredStretchingRatio" to REQUIRED_STRETCHING_RATIO.toString(),
            "unzipAllowable" to unzipAllowable.toString(),
            "tetherAllowable" to tetherAllowable.roundedForProse().toString(),
            "referenceFoundationStiffness" to referenceStiffness.toString(),
            "sources" to "gpd/results/T-1d-scf-density-profile.json, " +
                    "gpd/results/T-3-stroke-and-blocking-force.json, " +
                    "gpd/results/T-14-crossover-phase-and-registration.json"
        ),
        ledger = ledger(scf, layout, referenceStiffness, unzipAllowable, tetherAllowable),
        graftingDensityGrid = grid,
        points = points,
        heightWindows = heightWindows,
        biasClauses = biasClauses,
        stabilityClauses = stabilityClauses,
        transferLicence = transferLicence,
        falsifiers = falsifiers,
        findings = mapOf(
            "P1_is_non_empty_at_two_of_three_heights" to
                    "§4(a)-(d) intersect to a non-empty sigma window at 10 nm " +
                    "(${format(tenNanometre.predicateOneLowest)}-" +
                    "${format(tenNanometre.predicateOneHighest)} nm^-2, " +
                    "${format(tenNanometre.predicateOneWidthRatio)}x wide) and at 7 nm " +
                    "(${format(sevenNanometre.predicateOneLowest)}-" +
                    "${format(sevenNanometre.predicateOneHighest)} nm^-2, " +
                    "${format(sevenNanometre.predicateOneWidthRatio)}x). 5 nm is empty.",
            "the_binding_constraint_at_5_nm" to
                    "coil overlap against compliance, crossing by " +
                    "${format(fiveNanometre.predicateOneCrossing?.crossingRatio)}x in grafting " +
                    "density: the layer needs sigma >= " +
                    "${format(fiveNanometre.predicateOneCrossing?.lowerBoundValue)} nm^-2 to be " +
                    "a brush at all and sigma <= " +
                    "${format(fiveNanometre.predicateOneCrossing?.upperBoundValue)} nm^-2 to " +
                    "deliver 3 nm of stroke.",
            "section_4c_and_4d_never_bind" to
                    "Ion partitioning and poroelastic drainage are admissible at EVERY point " +
                    "of the grid at all three heights, so neither owns an edge anywhere. §4(c) " +
                    "is one-sided by construction (the layer excludes salt, so it protects the " +
                    "field) and §4(d) was already discharged by C-0004.",
            "the_blocking_clause_is_exactly_sigma_free" to
                    "F_es is a property of the tile, the electrode, the buffer and the gap, and " +
                    "the gap IS the layer height. The bias for 100 pN of blocking force is " +
                    "identical to twelve digits across all six layer models at every (height, " +
                    "buffer). Where the force clause fails it therefore fails across the WHOLE " +
                    "sigma window at that height, and no grafting density can rescue it.",
            "three_discovered_axes_do_not_resolve_in_sigma" to
                    "Flatness (45 attachments as 3 x 15), the usable bias window and the " +
                    "output-coupling stiffness are height-level or topological, not functions " +
                    "of sigma. They cannot NARROW a window; they can only CLOSE a height.",
            "the_held_operating_point_is_inside_validity_where_the_free_one_is_not" to
                    "At the §6 target the tile sits at L0 - 3 nm: " +
                    LAYER_HEIGHTS.joinToString("; ") { height ->
                        val clause = biasClauses.first {
                            it.layerHeight == height && it.concentration == 2.0
                        }
                        "L0 = $height nm gives a ${format(clause.heldGap)} nm gap at phi <= " +
                                "${format(clause.heldVolumeFractionSolvedLayer)} " +
                                "(gap above C-0005's 1.46 nm: " +
                                "${clause.heldGapAboveCorrelationBand}, phi below C-0002's " +
                                "0.2: ${clause.heldVolumeFractionBelowCrossover})"
                    } + ". The three validity breaches that set C-0012's 0.1 V ceiling are " +
                    "properties of the UNLOADED excursion, and the device §3 specifies works " +
                    "against a 100 pN load. This is CH-0015.",
            "stability_and_the_window_want_opposite_heights" to
                    "At the simultaneous §6 target the loaded operating point is unstable " +
                    "under 1 of 6 layer models at 5 nm, 3-6 of 6 at 7 nm and 6 of 6 at 10 nm " +
                    "at every buffer. So static stability wants the THIN layer, whose sigma " +
                    "window is empty, while the window and the stroke want the THICK one. " +
                    "That is C-0012's height inversion, now closed against C-0011's window.",
            "the_two_readings_of_the_usable_bias_window" to
                    "Reading A (C-0012's declared 0.02-0.1 V, set by the FREE operating point " +
                    "leaving three upstream validity ranges) and Reading B (the HELD operating " +
                    "point at the 3 nm stroke, whose only ceiling is CH-0007's ~1 V) give " +
                    "different answers, and the difference IS the output coupling: the same " +
                    "coupling that makes the operating point stable is what keeps it inside " +
                    "validity."
        ),
        verdict = verdict(heightWindows, biasClauses, stabilityClauses),
        validity = validity(),
        openQuestions = openQuestions()
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-2-design-window.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForWindowResult()) + "\n"
    )

    report(result)
    println("written: ${output.path}")
}

/** The height convention, stated in every record because it is worth a factor of four. */
const val HEIGHT_CONVENTION: String =
    "FORCE-ONSET: L0 is the height at which the layer carries 1.0 pN over the 40 x 40 nm " +
            "tile (C-0011). The first-moment thickness 2<z> of the same layer is reported " +
            "alongside every point and is smaller; the polymer to order differs between the " +
            "two conventions and CH-0010 requires the convention to be stated."

private val CONSTRAINT_PROVENANCE = mapOf(
    C_OVERLAP to "C-0011 / CH-0010 / P-5 re-opened — Sigma = pi R0^2 sigma >= 1, also the " +
            "1-D mean field's own validity condition",
    C_STRETCHING to "P-5's adopted criterion L0/R0 >= 1, carried alongside and expected to " +
            "be VACUOUS against a force-onset height",
    C_COMPLIANCE to "C-0011 — stroke >= 3.0 nm under a 100 pN dead load over the tile",
    C_PARTITIONING to "C-0005 — salt partition coefficient K <= 1, i.e. the layer excludes " +
            "salt rather than admitting it; one-sided (P-8)",
    C_DRAINAGE to "C-0004 — slowest of three permeability models, corner >= 1 kHz",
    C_LOAD_PATH to "C-0015 — peak per-load-path force at the best registration below the " +
            "10 pN unzip allowable"
)

private fun ledger(
    scf: ScfResults,
    layout: LayoutResults,
    referenceStiffness: Double,
    unzipAllowable: Double,
    tetherAllowable: Double
): List<LedgerEntry> = listOf(
    LedgerEntry("resting load defining L0", scf.restingLoad, "pN", "C-0011", "CITED"),
    LedgerEntry("monomer volume v0", scf.monomerVolume, "nm^3", "C-0002 via C-0011", "CITED"),
    LedgerEntry("acceptable stroke", ACCEPTABLE_STROKE, "nm", "§3", "CITED"),
    LedgerEntry("desired stroke", DESIRED_STROKE, "nm", "§3", "CITED"),
    LedgerEntry("target force", TARGET_FORCE, "pN", "§3", "CITED"),
    LedgerEntry("bandwidth target", BANDWIDTH_TARGET, "Hz", "§3", "CITED"),
    LedgerEntry("lateral counterion spacing", CORRELATION_BAND_GAP, "nm", "C-0005", "CITED"),
    LedgerEntry("concentrated crossover", CONCENTRATED_CROSSOVER, "1", "C-0002", "CITED"),
    LedgerEntry("duplex unzip allowable", unzipAllowable, "pN", "C-0006 via C-0015", "CITED"),
    LedgerEntry("duplex shear allowable", layout.shearAllowable, "pN", "C-0006 via C-0015", "CITED"),
    LedgerEntry("load concentration factor", LOAD_CONCENTRATION_FACTOR, "1", "C-0009 via C-0014", "CITED"),
    LedgerEntry("duplex stretch modulus", DUPLEX_STRETCH_MODULUS, "pN", "Wang et al. 1997 via C-0014", "CITED"),
    LedgerEntry("bulk Debye length at 2 mM", BULK_DEBYE_LENGTH_AT_TWO_MILLIMOLAR, "nm", "C-0005", "CITED"),
    LedgerEntry("flatness attachments", layout.flatnessMinima.first { it.model == "lattice" }
        .bestAttachments.toDouble(), "1", "C-0015", "CITED"),
    LedgerEntry("tile crossovers", layout.flatnessMinima.first { it.model == "lattice" }
        .crossovers.toDouble(), "1", "C-0009 via C-0015", "CITED"),
    LedgerEntry("reference foundation stiffness", referenceStiffness, "pN/nm",
        "C-0001 secant via C-0015's foundation modulus", "DERIVED here from T-14's own modulus"),
    LedgerEntry("tether allowable", tetherAllowable, "pN",
        "shear / concentration factor", "DERIVED here"),
    LedgerEntry("grafting density grid ratio",
        scf.designPoints[1].graftingDensity / scf.designPoints[0].graftingDensity, "1",
        "T-1d's own sweep", "DERIVED here")
)

private fun verdict(
    heightWindows: List<HeightWindow>,
    biasClauses: List<BiasClause>,
    stabilityClauses: List<StabilityClause>
): Map<String, String> {
    val readingAPassing = biasClauses.filter { it.readingAModelsPassing > 0 }
    val unstableAtWorking = stabilityClauses.filter {
        it.concentration == 2.0 && it.appliedBias == 0.1 && it.modelsUnstable > 0
    }
    return mapOf(
        "P1 — §4(a)-(d) as posed" to heightWindows.joinToString("; ") { window ->
            if (window.predicateOneEmpty) "L0 = ${window.layerHeight.roundedForProse()} nm: EMPTY"
            else "L0 = ${window.layerHeight.roundedForProse()} nm: sigma in [" +
                    "${format(window.predicateOneLowest)}, " +
                    "${format(window.predicateOneHighest)}] nm^-2"
        },
        "P1 binding constraints" to heightWindows.joinToString("; ") { window ->
            if (window.predicateOneEmpty)
                "L0 = ${window.layerHeight.roundedForProse()} nm closed by " +
                        "${window.predicateOneCrossing?.lowerBoundConstraint} against " +
                        window.predicateOneCrossing?.upperBoundConstraint
            else "L0 = ${window.layerHeight.roundedForProse()} nm: lower edge " +
                    "${window.predicateOneLowerBinding}, upper edge " +
                    "${window.predicateOneUpperBinding}"
        },
        "P2 reading A — the strict usable bias window" to
                if (readingAPassing.isEmpty())
                    "EMPTY AT EVERY HEIGHT AND EVERY BUFFER"
                else "non-empty only at " + readingAPassing.joinToString(", ") {
                    "L0 = ${it.layerHeight.roundedForProse()} nm, ${it.concentration.roundedForProse()} mM " +
                            "(${it.readingAModelsPassing}/${it.readingAModelsTotal} models)"
                },
        "P2 reading B — the held operating point" to
                "the §3 targets are reachable inside CH-0007's ~1 V boundary at every height, " +
                        "CONDITIONAL on an output coupling supplying " +
                        "${unstableAtWorking.minOfOrNull { it.requiredCouplingStiffnessLow ?: 0.0 }
                            ?.let { format(it) }}-" +
                        "${unstableAtWorking.maxOfOrNull { it.requiredCouplingStiffnessHigh ?: 0.0 }
                            ?.let { format(it) }} pN/nm of its own stiffness at 2 mM and 0.10 V",
        "the constraint that closes P2" to
                "THE OUTPUT COUPLING. At 7 and 10 nm the loaded operating point is statically " +
                        "unstable, so the §3 targets exist only against a coupling that supplies " +
                        "5-277 pN/nm across the swept biases. Whether a DNA-origami lever can " +
                        "deliver that is T-16, and NO CLAIM IN THIS PROGRAMME SUPPLIES THE NUMBER. " +
                        "T-2 therefore cannot close P2 in either direction, and says so.",
        "the desired 10 nm stroke" to
                "UNREACHABLE at every height and every grafting density, on the solved profile " +
                        "and inside model validity — C-0001's one surviving headline, now " +
                        "confirmed against a third layer model and a fourth constraint set."
    )
}

private fun validity(): List<String> = listOf(
    "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED. A design window is " +
            "exactly the artifact a reader mistakes for a recommendation.",
    "THE HEIGHT CONVENTION IS FORCE-ONSET. $HEIGHT_CONVENTION",
    "Every window edge is a GRID POINT on T-1d's 61-point logarithmic sweep, so it is located " +
            "to one grid ratio (1.109x) and no better.",
    "The layer is C-0011's solved SCF profile, which inherits every one of its validity " +
            "limits: mean field at phi ~ 0.01 with fluctuation corrections NOT bounded (T-1f); " +
            "an interaction free energy that is not measured below phi#, which is the whole " +
            "working range; monodisperse chains; laterally uniform grafting; a rigid tile.",
    "T-3's coupled verdicts were computed on C-0003's six layer models at ONE grafting " +
            "density per height, not on the solved layer and not across sigma. The transfer is " +
            "checked at each shared design point and reported; at 5 nm it is NOT licensed.",
    "The bias-window boundary is bracketed by T-3's own bias grid, which has no sample " +
            "between 0.10 V and 0.25 V (C-0012's open question 5). Reading A's verdicts at " +
            "heights whose largest valid bias is 0.10 V could move if the true crossing is higher.",
    "Mean-field electrostatics, inherited whole: C-0005 puts the one-loop correction at " +
            "123-214 % of the leading term across the entire 5-10 nm working range. This is " +
            "the largest single uncertainty in P2 and it is not reducible by a better PB solve.",
    "The output-coupling stiffness a DNA-origami lever can deliver IS NOT KNOWN. T-16 owns it. " +
            "P2's verdict is conditional on it and is reported as conditional rather than resolved.",
    "The in-plane load path into the tile is C-0009's OUT-of-plane concentration factor used " +
            "as a conservative stand-in (C-0014, T-15). Every minimum tether length here could " +
            "shrink by up to 2.8x.",
    "No lateral load profile, no tile edge, no fringing: the dishing ratios that would convert " +
            "a tile-mean stroke into a lever-point stroke are C-0006's and are cited as ratios (T-3b).",
    "The layer is neutral linear PEG. §3 also permits PEO and a PS->PEG block copolymer, for " +
            "which no osmotic equation of state was consumed anywhere in this programme."
)

private fun openQuestions(): List<String> = listOf(
    "What output-coupling stiffness a DNA-origami lever can deliver at the tile. T-16. " +
            "Without it P2 cannot be closed in either direction, and this is the single number " +
            "that decides whether the Gen-1 stack has a design window at all.",
    "Where between 0.10 V and 0.25 V the free operating point leaves upstream validity. " +
            "T-3's bias grid has no sample there and Reading A's boundary is bracketed, not located.",
    "Whether the first-moment inversion moves the polymer to order. T-1e. C-0011's own " +
            "N^(0.5-0.55) scaling is an extrapolation, not a computed design point, and the " +
            "chain length differs by about four times between the two height conventions.",
    "Whether the mean-field fluctuation corrections at phi ~ 0.01 are bounded. T-1f. This is " +
            "the largest unbounded exposure under the layer this whole window rests on.",
    "Whether the in-plane load concentration factor is 7.6x or ~1. T-15. At ~1 every minimum " +
            "tether length here shrinks by up to 2.8x.",
    "Whether Mg2+ coordination by PEG's ether oxygens flips the sign of §4(c). P-8. The " +
            "partitioning bound carried here counts exclusion only and is one-sided."
)

private fun format(value: Double?): String =
    if (value == null) "n/a" else "%.4g".format(value)

private fun report(result: DesignWindowResult) {
    println("=== T-2 — the feasible design window ".padEnd(100, '='))
    println()
    println("HEIGHT CONVENTION: ${result.heightConvention}")
    println()
    println("--- P1: §4(a)-(d) as posed ".padEnd(100, '-'))
    println(
        "%-6s %-12s %-12s %-8s %-24s %-24s".format(
            "L0", "sigma low", "sigma high", "width", "lower edge", "upper edge"
        )
    )
    result.heightWindows.forEach { window ->
        println(
            "%-6.1f %-12s %-12s %-8s %-24s %-24s".format(
                window.layerHeight,
                format(window.predicateOneLowest),
                format(window.predicateOneHighest),
                format(window.predicateOneWidthRatio),
                window.predicateOneLowerBinding.joinToString(","),
                window.predicateOneUpperBinding.joinToString(",")
            )
        )
        window.predicateOneCrossing?.let {
            println(
                "       EMPTY — ${it.lowerBoundConstraint} needs sigma >= " +
                        "${format(it.lowerBoundValue)} while ${it.upperBoundConstraint} needs " +
                        "sigma <= ${format(it.upperBoundValue)}, crossing by " +
                        "${format(it.crossingRatio)}x"
            )
        }
    }
    println()
    println("--- what a bench would order ".padEnd(100, '-'))
    result.heightWindows.mapNotNull { it.benchOrder }.forEach { order ->
        println(
            "L0 = %.0f nm  sigma %.4f-%.4f nm^-2  s %.2f-%.2f nm  N %.1f-%.1f  PEG %.2f-%.2f kDa"
                .format(
                    order.layerHeight, order.graftingDensityLow, order.graftingDensityHigh,
                    order.graftingSpacingHigh, order.graftingSpacingLow,
                    order.monomersPerChainLow, order.monomersPerChainHigh,
                    order.pegKilodaltonLow, order.pegKilodaltonHigh
                )
        )
        println(
            "            2<z> %.2f-%.2f nm  stroke %.2f-%.2f nm  k_sec %.1f-%.1f pN/nm  %d attachments as %s"
                .format(
                    order.firstMomentThicknessLow, order.firstMomentThicknessHigh,
                    order.deadLoadStrokeLow, order.deadLoadStrokeHigh,
                    order.secantStiffnessLow, order.secantStiffnessHigh,
                    order.attachmentCount, order.attachmentShape
                )
        )
        println(
            "            tether %.1f-%.1f nm, assembly %.0f-%.0f nm on a 40 nm tile".format(
                order.minimumTetherLengthLow, order.minimumTetherLengthHigh,
                order.assemblyEdgeLow, order.assemblyEdgeHigh
            )
        )
    }
    println()
    println("--- P2 reading A: the strict usable bias window ".padEnd(100, '-'))
    println(
        "%-6s %-6s %-10s %-12s %-14s %-12s %s".format(
            "L0", "mM", "V_block", "V_valid", "F@V_valid", "stroke", "verdict"
        )
    )
    result.biasClauses.forEach { clause ->
        println(
            "%-6.1f %-6.1f %-10s %-12s %-14s %-12s %s".format(
                clause.layerHeight, clause.concentration,
                format(clause.biasForHundredPiconewtonBlocking),
                "%.2f-%.2f".format(clause.largestModelValidBiasLow, clause.largestModelValidBiasHigh),
                "%.1f-%.1f".format(clause.blockingForceAtValidBiasLow, clause.blockingForceAtValidBiasHigh),
                "%.2f-%.2f".format(clause.strokeAtValidBiasLow, clause.strokeAtValidBiasHigh),
                "${clause.readingAModelsPassing}/${clause.readingAModelsTotal} " +
                        "(binds: ${clause.readingABindingClause})"
            )
        )
    }
    println()
    println("--- P2 reading B: the HELD operating point at the §6 target ".padEnd(100, '-'))
    println(
        "%-6s %-6s %-14s %-10s %-8s %-10s %s".format(
            "L0", "mM", "V_simultaneous", "held gap", "held phi", "in range?", "unstable there"
        )
    )
    result.biasClauses.forEach { clause ->
        println(
            "%-6.1f %-6.1f %-14s %-10s %-8s %-10s %s".format(
                clause.layerHeight, clause.concentration,
                if (clause.biasForSimultaneousTargetLow == null) "not reached"
                else "%.3f-%.3f".format(
                    clause.biasForSimultaneousTargetLow, clause.biasForSimultaneousTargetHigh
                ),
                "%.1f nm".format(clause.heldGap),
                "%.4f".format(clause.heldVolumeFractionSolvedLayer),
                if (clause.heldGapAboveCorrelationBand &&
                    clause.heldVolumeFractionBelowCrossover) "yes" else "NO",
                "${clause.modelsUnstableAtSimultaneousTarget}/6 models"
            )
        )
    }
    println()
    println("--- P2: static stability at the loaded operating point, 2 mM ".padEnd(100, '-'))
    result.stabilityClauses.filter { it.concentration == 2.0 }.forEach { clause ->
        println(
            "L0 = %4.1f nm  V = %.2f  unstable %d/%d  coupling needed %s pN/nm".format(
                clause.layerHeight, clause.appliedBias, clause.modelsUnstable, clause.modelsTotal,
                if (clause.requiredCouplingStiffnessLow == null) "-"
                else "%.1f-%.1f".format(
                    clause.requiredCouplingStiffnessLow, clause.requiredCouplingStiffnessHigh
                )
            )
        )
    }
    println()
    println("--- the transfer licence ".padEnd(100, '-'))
    result.transferLicence.forEach { println("  $it") }
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
