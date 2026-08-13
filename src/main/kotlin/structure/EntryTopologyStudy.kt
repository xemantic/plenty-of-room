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

package com.xemantic.nano.plentyofroom.structure

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Task `T-19` / leaf `A8.2` — **the attachment's entry topology**: what a tether actually
 * bonds to, and what that does to `C-0020`'s in-plane transfer ratio of exactly one.
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.EntryTopologyStudyKt
 * ```
 *
 * Emits `gpd/results/T-19-attachment-entry-topology.json`, deterministically.
 */

// --------------------------------------------------------------------------- records

/** Every parameter the run consumed, logged so the result is reproducible from the file alone. */
@Serializable
data class EntryTopologyParameters(
    val temperature: Double,
    val medium: String,
    val footprintAlong: Double,
    val footprintAcross: Double,
    val duplexes: Int,
    val interhelicalDistance: Double,
    val crossoverSpacingBasePairs: Double,
    val risePerBasePair: Double,
    val stretchModulus: Double,
    val bendingRigidity: Double,
    val crossoverInPlaneStiffness: Double,
    val connectorArm: Double,
    val regularisation: Double,
    val subdivisions: Int,
    val nominalPhaseBasePairs: Int,
    val appliedTetherForce: Double,
    val unzipAllowable: Double,
    val shearAllowable: Double,
    val overstretchingCeiling: Double,
    val jointSeparationOffset: Double,
    val jointSeparationPerBasePair: Double,
    val jointOffRateIntercept: Double,
    val jointOffRateSlope: Double,
    val jointReferenceLoadingRate: Double,
    val provenance: Map<String, String>
)

/** The bounds that hold before any matrix is assembled. */
@Serializable
data class EntryBoundRecord(
    val name: String,
    val value: Double,
    val statement: String
)

/** The per-load-path forces of one solved entry topology, per pN of applied tether force. */
@Serializable
data class TopologyForces(
    val label: String,
    val bondsPerEnd: Int,
    val duplexSpan: Int,
    val largestShare: Double,
    val entryForce: Double,
    val transferRatioDuplexAxial: Double,
    val transferRatioCrossover: Double,
    val transferRatioDuplexInPlaneShear: Double,
    val peakOverLargestShare: Double,
    val concentrationDuplexAxial: Double,
    val effectiveAllowable: Double,
    val bindingPath: String
)

/** One design of the complete band ladder. */
@Serializable
data class BandLadderPoint(
    val bandWidth: Int,
    val firstDuplex: Int,
    val phaseBasePairs: Int,
    val transferRatioDuplexAxial: Double,
    val transferRatioCrossover: Double,
    val transferRatioDuplexInPlaneShear: Double,
    val effectiveAllowable: Double
)

/** The band ladder at one band width, over every position and every column phase. */
@Serializable
data class BandLadderSummary(
    val bandWidth: Int,
    val designs: Int,
    val equalShare: Double,
    val worstTransferRatioDuplexAxial: Double,
    val bestTransferRatioDuplexAxial: Double,
    val worstOverEqualShare: Double,
    val worstTransferRatioCrossover: Double,
    val worstEffectiveAllowable: Double,
    val bestEffectiveAllowable: Double,
    val bindingPathAtWorst: String,
    val layoutSpanDuplexAxial: Double,
    val layoutSpanCrossover: Double,
    val positionSpanDuplexAxial: Double,
    val worst: BandLadderPoint
)

/** The rigid-bond split against the equal one, at one design. */
@Serializable
data class SplitPoint(
    val bandWidth: Int,
    val firstDuplex: Int,
    val phaseBasePairs: Int,
    val equalShare: Double,
    val largestCompatibleShare: Double,
    val smallestCompatibleShare: Double,
    val compatibleOverEqual: Double,
    val equalSplitPeak: Double,
    val compatibleSplitPeak: Double,
    val peakRatio: Double
)

/** A tether bonded onto a crossover, against a one-point control at the same station. */
@Serializable
data class CrossoverBondPoint(
    val lowerBeam: Int,
    val phaseBasePairs: Int,
    val nearStation: Double,
    val farStation: Double,
    val chordLength: Double,
    val bondTransferRatioDuplexAxial: Double,
    val bondTransferRatioCrossover: Double,
    val bondEffectiveAllowable: Double,
    val controlTransferRatioDuplexAxial: Double,
    val controlTransferRatioCrossover: Double,
    val controlEffectiveAllowable: Double,
    val gainOverControl: Double
)

/** A bond spread over a footprint of consecutive base pairs on one duplex. */
@Serializable
data class FootprintPoint(
    val bases: Int,
    val footprintLength: Double,
    val distribution: String,
    val phaseBasePairs: Int,
    val degreesOfFreedom: Int,
    val transferRatioDuplexAxial: Double,
    val transferRatioCrossover: Double,
    val effectiveAllowable: Double,
    /**
     * The axial force a **single-point** attachment carries at the inboard end of the same
     * footprint — the load shed over that length, which is the whole of what a footprint can
     * relieve. The footprint's own peak must lie between it and one.
     */
    val singlePointAtFootprintEnd: Double,
    val jointAllowable: Double,
    /** False where Evans-Ritchie's logarithm has changed sign, i.e. below the model's range. */
    val jointModelValid: Boolean
)

/** An oblique chord, entered through a band instead of a point. */
@Serializable
data class ObliquePoint(
    val bandWidth: Int,
    val fromDuplex: Int,
    val toDuplex: Int,
    val duplexOffset: Int,
    val phaseBasePairs: Int,
    val angleDegrees: Double,
    val transferRatioDuplexAxial: Double,
    val transferRatioCrossover: Double,
    val effectiveAllowable: Double,
    val overEqualShare: Double,
    val reliefOverSinglePoint: Double
)

/** The lattice against the continuum shear-lag membrane it discretises, at one station. */
@Serializable
data class EntryContinuumStation(
    val bandWidth: Int,
    val crossoverShearStiffness: Double,
    val x: Double,
    val duplex: Int,
    val lattice: Double,
    val continuum: Double,
    val difference: Double,
    val excess: Double
)

/** The joint's own allowable at one bonded length and loading rate. */
@Serializable
data class JointLengthPoint(
    val basePairs: Int,
    val loadingRate: Double,
    val ruptureForce: Double,
    val splitGainTwoWays: Double,
    val splitGainThreeWays: Double
)

/** Where splitting a bond stops losing and starts winning, at one loading rate. */
@Serializable
data class JointBreakEven(
    val ways: Int,
    val loadingRate: Double,
    val breakEvenBasePairs: Double,
    val saturationForce: Double
)

/** One design point, carried all the way through to `C-0014`'s tether geometry. */
@Serializable
data class EntryDesignPoint(
    val name: String,
    val bondsPerEnd: Int,
    val bondedBasePairsPerBond: Int,
    val totalBondedBasePairs: Int,
    val geometry: String,
    val largestShare: Double,
    val transferRatioDuplexAxial: Double,
    val transferRatioCrossover: Double,
    val jointAllowablePerBond: Double,
    val effectiveAllowable: Double,
    val bindingPath: String,
    val concentrationFactor: Double,
    val minimumLengthAtAcceptableStroke: Double,
    val minimumLengthAtDesiredStroke: Double,
    val assemblyFootprintAtDesiredStroke: Double,
    val tetherTension: Double,
    val normalPreloadTotal: Double,
    val normalPreloadFractionOfTarget: Double,
    val holdDownLengthAtDesiredStroke: Double
)

/** The convergence record of one swept numerical parameter. */
@Serializable
data class EntryConvergencePoint(
    val parameter: String,
    val value: Double,
    val degreesOfFreedom: Int,
    val peakDuplexAxial: Double,
    val peakCrossover: Double,
    val regularisationForce: Double
)

@Serializable
data class EntryTopologyResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: EntryTopologyParameters,
    val bounds: List<EntryBoundRecord>,
    val topologies: List<TopologyForces>,
    val bandLadder: List<BandLadderSummary>,
    val bandLadderAtNominalPhase: List<BandLadderPoint>,
    val splitComparison: List<SplitPoint>,
    val crossoverBonds: List<CrossoverBondPoint>,
    val footprints: List<FootprintPoint>,
    val oblique: List<ObliquePoint>,
    val continuumStations: List<EntryContinuumStation>,
    val jointLengths: List<JointLengthPoint>,
    val jointBreakEven: List<JointBreakEven>,
    val designPoints: List<EntryDesignPoint>,
    val convergence: List<EntryConvergencePoint>,
    val verdict: Map<String, String>
)

// --------------------------------------------------------------------------- constants

private const val ENTRY_BEAM_COUNT = 15

private const val ENTRY_NOMINAL_PHASE = 8

private const val ENTRY_FORCE = 1.0

private const val ENTRY_TETHER_COUNT = 4

/** `C-0021`'s mean-excursion hold-down scale, in pN — `k_BT / 3.0 nm`. */
private const val HOLD_DOWN_SCALE = 1.380649

/** The bonded length of one 32-nt staple domain budget, in base pairs. */
private const val STAPLE_BUDGET_BASE_PAIRS = 32

private val entryTopologySheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

private val entryShear = Gen1Tile.crossoverInPlaneStiffness()

private val joint = ShearJointAllowable()

private val edge = Gen1Tile.EDGE_X / 2.0

private fun entryLattice(
    phase: Int,
    crossoverStiffness: Double = entryShear,
    subdivisions: Int = OrigamiMembrane.DEFAULT_SUBDIVISIONS,
    connectorArm: Double = entryTopologySheet.interhelicalDistance / 2.0,
    extraStations: List<Double> = emptyList()
): OrigamiMembrane = OrigamiMembrane(
    sheet = entryTopologySheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = ENTRY_BEAM_COUNT,
    columns = CrossoverLayout.atBasePairPhase(
        phase, entryTopologySheet, Gen1Tile.EDGE_X
    ),
    crossoverShearStiffness = crossoverStiffness,
    crossoverNormalStiffness = crossoverStiffness,
    subdivisions = subdivisions,
    connectorArm = connectorArm,
    extraStations = extraStations
)

// --------------------------------------------------------------------------- reporting

private fun effectiveAllowable(axial: Double, crossover: Double, shear: Double): Double = minOf(
    Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / axial,
    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / crossover,
    Gen1Tile.OVERSTRETCHING_CEILING / shear
)

private fun bindingPathOf(axial: Double, crossover: Double, shear: Double): String = when (
    effectiveAllowable(axial, crossover, shear)
) {
    Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / axial -> "duplex axial force at the attachment"
    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / crossover -> "crossover in-plane force"
    else -> "duplex in-plane shear"
}

private fun topologyForces(
    label: String,
    lattice: OrigamiMembrane,
    near: EntryTopology,
    far: EntryTopology
): TopologyForces {
    val solution = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
    val axial = solution.peakDuplexAxialForce / ENTRY_FORCE
    val crossover = solution.peakCrossoverForce / ENTRY_FORCE
    val shear = solution.peakDuplexInPlaneShear / ENTRY_FORCE
    val entry = near.bonds.maxOf { bond ->
        abs(lattice.axialForceAt(solution, bond.duplex, bond.x + ENTRY_PROBE))
    }
    return TopologyForces(
        label = label,
        bondsPerEnd = near.bonds.size,
        duplexSpan = near.duplexSpan,
        largestShare = near.largestShare,
        entryForce = entry,
        transferRatioDuplexAxial = axial,
        transferRatioCrossover = crossover,
        transferRatioDuplexInPlaneShear = shear,
        peakOverLargestShare = axial / near.largestShare,
        concentrationDuplexAxial = axial * ENTRY_BEAM_COUNT,
        effectiveAllowable = effectiveAllowable(axial, crossover, shear),
        bindingPath = bindingPathOf(axial, crossover, shear)
    )
}

/** A hair inboard of a bond station, so that its own entry element is the one evaluated. */
private const val ENTRY_PROBE = 1e-6

/**
 * The index of the extremum of [values], chosen on the **rounded** value with the index as
 * tie-break — the argmin trap `CLAUDE.md` records, and `C-0015` was bitten by.
 */
private fun entryArgExtremum(values: List<Double>, largest: Boolean): Int {
    var best = 0
    var bestValue = roundForResult(values[0])
    for (i in 1 until values.size) {
        val candidate = roundForResult(values[i])
        if (if (largest) candidate > bestValue else candidate < bestValue) {
            best = i
            bestValue = candidate
        }
    }
    return best
}

private fun entryElapsed(started: Long): String =
    "%.1f s".format((System.currentTimeMillis() - started) / 1000.0)

/** The exact minimum surface-parallel tether length in nm for [stroke] at [allowable]. */
private fun minimumTetherLength(stroke: Double, allowable: Double): Double {
    val modulus = entryTopologySheet.duplex.stretchModulus
    val ratio = sqrt((1.0 + allowable / modulus) * (1.0 + allowable / modulus) - 1.0)
    return stroke / ratio
}

/** The downward normal preload in pN of [count] minimum-length tethers at [allowable]. */
private fun normalPreload(allowable: Double, count: Int): Double {
    val modulus = entryTopologySheet.duplex.stretchModulus
    val ratio = sqrt((1.0 + allowable / modulus) * (1.0 + allowable / modulus) - 1.0)
    return count * allowable * ratio / sqrt(1.0 + ratio * ratio)
}

/**
 * The tether length in nm at which [count] surface-parallel tethers at [stroke] supply
 * exactly [force] pN of downward preload — `C-0021`'s hold-down, arriving from `C-0014`'s
 * geometry rather than from a new element.
 *
 * Bisected on the **bracket**, not on a residual: the exit test is the bracket width, per
 * `CLAUDE.md`'s record of an unreachable tolerance running its full iteration cap in silence.
 */
private fun holdDownLength(stroke: Double, force: Double, count: Int): Double {
    val modulus = entryTopologySheet.duplex.stretchModulus
    fun preloadAt(length: Double): Double {
        val ratio = stroke / length
        val tension = modulus * (sqrt(1.0 + ratio * ratio) - 1.0)
        return count * tension * ratio / sqrt(1.0 + ratio * ratio)
    }
    var low = 1e-3
    var high = 1e6
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (preloadAt(middle) > force) low = middle else high = middle
    }
    return 0.5 * (low + high)
}

// --------------------------------------------------------------------------- study

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val started = System.currentTimeMillis()
    val nominal = entryLattice(ENTRY_NOMINAL_PHASE)
    val mid = ENTRY_BEAM_COUNT / 2

    // ---------------------------------------------------------------- the cheap bounds
    val bounds = listOf(
        EntryBoundRecord(
            "pigeonhole floor on the transfer ratio",
            1.0 / ENTRY_BEAM_COUNT,
            "the duplex axial forces on a cut sum to the applied force (C-0020 gate 3), so " +
                    "on a tile of ${ENTRY_BEAM_COUNT} duplexes SOME duplex carries at least " +
                    "1/D. No entry topology whatsoever can beat it"
        ),
        EntryBoundRecord(
            "ceiling on the duplex-path effective allowable",
            ENTRY_BEAM_COUNT * Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
            "the pigeonhole floor turned into an allowable: entry-topology design cannot buy " +
                    "more than D times the single-path allowable, and only a bond to EVERY " +
                    "duplex reaches it, which is an edge clamp and not a tether"
        ),
        EntryBoundRecord(
            "short-bond limit for a band of m duplexes",
            0.5,
            "no crossover sits on the rim, so the entry element of each bonded duplex carries " +
                    "exactly its own share: an m-duplex bond with an equal split enters at " +
                    "1/m, quoted here for m = 2. C-0020's 'halving' is an upper bound on the " +
                    "benefit, not an estimate"
        ),
        EntryBoundRecord(
            "the footprint is the m = 1 case",
            1.0,
            "spreading a bond over k bases of ONE duplex leaves m = 1, so that duplex must " +
                    "still carry the whole tension inboard of the footprint and the peak " +
                    "cannot fall. Whatever a footprint buys is bought on the JOINT, not on " +
                    "the sheet"
        ),
        EntryBoundRecord(
            "the joint allowable is concave above its break-even",
            joint.splitBreakEven(2, ShearJointAllowable.REFERENCE_LOADING_RATE),
            "Strunz's own scaling makes the shear rupture force saturate with domain length, " +
                    "so m domains of n/m outperform one of n above this total bonded length " +
                    "in base pairs — and UNDERperform it below, because the barrier " +
                    "separation has an n-independent offset"
        )
    )

    // ---------------------------------------------------------------- the named topologies
    val nearPoint = EntryTopology.singlePoint("one point on one duplex", mid, -edge)
    val farPoint = EntryTopology.singlePoint("one point on one duplex", mid, edge)
    val topologies = mutableListOf<TopologyForces>()
    topologies += topologyForces(
        "E1 one point on one duplex (C-0020's model, reproduced)", nominal, nearPoint, farPoint
    )
    listOf(2, 3, 4).forEach { width ->
        topologies += topologyForces(
            "E2 a band of $width adjacent duplexes, equal split",
            nominal,
            EntryTopology.duplexBand("near", mid, width, -edge),
            EntryTopology.duplexBand("far", mid, width, edge)
        )
    }
    run {
        val near = EntryTopology.duplexBand("near", mid, 2, -edge)
        val far = EntryTopology.duplexBand("far", mid, 2, edge)
        val shares = nominal.compatibleShares(near, far)
        topologies += topologyForces(
            "E2 a band of 2 adjacent duplexes, RIGID staple (compatible split)",
            nominal, near.withShares(shares), far.withShares(shares)
        )
    }
    println("named topologies done at ${entryElapsed(started)}")

    // ---------------------------------------------------------------- the band ladder
    val ladderPoints = mutableListOf<BandLadderPoint>()
    for (phase in 0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD) {
        val lattice = entryLattice(phase)
        for (width in 1..ENTRY_BEAM_COUNT) {
            for (first in 0..ENTRY_BEAM_COUNT - width) {
                val near = EntryTopology.duplexBand("near", first, width, -edge)
                val far = EntryTopology.duplexBand("far", first, width, edge)
                val solution = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
                val axial = solution.peakDuplexAxialForce / ENTRY_FORCE
                val crossover = solution.peakCrossoverForce / ENTRY_FORCE
                val shear = solution.peakDuplexInPlaneShear / ENTRY_FORCE
                ladderPoints += BandLadderPoint(
                    bandWidth = width,
                    firstDuplex = first,
                    phaseBasePairs = phase,
                    transferRatioDuplexAxial = axial,
                    transferRatioCrossover = crossover,
                    transferRatioDuplexInPlaneShear = shear,
                    effectiveAllowable = effectiveAllowable(axial, crossover, shear)
                )
            }
        }
    }
    println("band ladder (${ladderPoints.size} designs) done at ${entryElapsed(started)}")

    val bandLadder = (1..ENTRY_BEAM_COUNT).map { width ->
        val group = ladderPoints.filter { it.bandWidth == width }
        val axials = group.map { it.transferRatioDuplexAxial }
        val allowables = group.map { it.effectiveAllowable }
        val worst = group[entryArgExtremum(allowables, largest = false)]
        val best = group[entryArgExtremum(allowables, largest = true)]
        // the layout span is taken at a FIXED band position, so that it measures the column
        // phase and not the position; the position span is its complement at a fixed phase
        val atFirst = group.filter { it.firstDuplex == 0 }
        val atNominalPhase = group.filter { it.phaseBasePairs == ENTRY_NOMINAL_PHASE }
        BandLadderSummary(
            bandWidth = width,
            designs = group.size,
            equalShare = 1.0 / width,
            worstTransferRatioDuplexAxial = axials.max(),
            bestTransferRatioDuplexAxial = axials.min(),
            worstOverEqualShare = axials.max() * width,
            worstTransferRatioCrossover = group.maxOf { it.transferRatioCrossover },
            worstEffectiveAllowable = worst.effectiveAllowable,
            bestEffectiveAllowable = best.effectiveAllowable,
            bindingPathAtWorst = bindingPathOf(
                worst.transferRatioDuplexAxial, worst.transferRatioCrossover,
                worst.transferRatioDuplexInPlaneShear
            ),
            layoutSpanDuplexAxial = atFirst.maxOf { it.transferRatioDuplexAxial } /
                    atFirst.minOf { it.transferRatioDuplexAxial },
            layoutSpanCrossover = atFirst.maxOf { it.transferRatioCrossover } /
                    atFirst.minOf { it.transferRatioCrossover },
            positionSpanDuplexAxial = atNominalPhase.maxOf { it.transferRatioDuplexAxial } /
                    atNominalPhase.minOf { it.transferRatioDuplexAxial },
            worst = worst
        )
    }

    // ---------------------------------------------------------------- the two split limits
    val splitComparison = mutableListOf<SplitPoint>()
    for (phase in 0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD) {
        val lattice = entryLattice(phase)
        for (first in 0 until ENTRY_BEAM_COUNT - 1) {
            val near = EntryTopology.duplexBand("near", first, 2, -edge)
            val far = EntryTopology.duplexBand("far", first, 2, edge)
            val shares = lattice.compatibleShares(near, far)
            val equal = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
            val rigid = lattice.solve(
                lattice.tetherLoads(near.withShares(shares), far.withShares(shares), ENTRY_FORCE)
            )
            splitComparison += SplitPoint(
                bandWidth = 2,
                firstDuplex = first,
                phaseBasePairs = phase,
                equalShare = 0.5,
                largestCompatibleShare = shares.max(),
                smallestCompatibleShare = shares.min(),
                compatibleOverEqual = shares.max() / 0.5,
                equalSplitPeak = equal.peakDuplexAxialForce / ENTRY_FORCE,
                compatibleSplitPeak = rigid.peakDuplexAxialForce / ENTRY_FORCE,
                peakRatio = rigid.peakDuplexAxialForce / equal.peakDuplexAxialForce
            )
        }
    }
    listOf(3, 4, 5, 6).forEach { width ->
        val near = EntryTopology.duplexBand("near", 0, width, -edge)
        val far = EntryTopology.duplexBand("far", 0, width, edge)
        val shares = nominal.compatibleShares(near, far)
        val equal = nominal.solve(nominal.tetherLoads(near, far, ENTRY_FORCE))
        val rigid = nominal.solve(
            nominal.tetherLoads(near.withShares(shares), far.withShares(shares), ENTRY_FORCE)
        )
        splitComparison += SplitPoint(
            bandWidth = width,
            firstDuplex = 0,
            phaseBasePairs = ENTRY_NOMINAL_PHASE,
            equalShare = 1.0 / width,
            largestCompatibleShare = shares.max(),
            smallestCompatibleShare = shares.min(),
            compatibleOverEqual = shares.max() * width,
            equalSplitPeak = equal.peakDuplexAxialForce / ENTRY_FORCE,
            compatibleSplitPeak = rigid.peakDuplexAxialForce / ENTRY_FORCE,
            peakRatio = rigid.peakDuplexAxialForce / equal.peakDuplexAxialForce
        )
    }
    println("split comparison done at ${entryElapsed(started)}")

    // ---------------------------------------------------------------- bonded onto a crossover
    val crossoverBonds = mutableListOf<CrossoverBondPoint>()
    for (phase in 0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD) {
        val lattice = entryLattice(phase)
        for (lower in 0 until ENTRY_BEAM_COUNT - 1) {
            val onInterface = lattice.crossovers.filter { it.lowerBeam == lower }
            if (onInterface.size < 2) continue
            val nearCrossover = onInterface.minBy { it.x }
            val farCrossover = onInterface.maxBy { it.x }
            val near = EntryTopology.onCrossover("near", nearCrossover)
            val far = EntryTopology.onCrossover("far", farCrossover)
            val bond = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
            // the control isolates the TOPOLOGY from the station: one point on one duplex,
            // at exactly the same two stations
            val controlNear = EntryTopology.singlePoint("control", lower, nearCrossover.x)
            val controlFar = EntryTopology.singlePoint("control", lower, farCrossover.x)
            val control = lattice.solve(
                lattice.tetherLoads(controlNear, controlFar, ENTRY_FORCE)
            )
            val bondAllowable = effectiveAllowable(
                bond.peakDuplexAxialForce, bond.peakCrossoverForce, bond.peakDuplexInPlaneShear
            )
            val controlAllowable = effectiveAllowable(
                control.peakDuplexAxialForce, control.peakCrossoverForce,
                control.peakDuplexInPlaneShear
            )
            crossoverBonds += CrossoverBondPoint(
                lowerBeam = lower,
                phaseBasePairs = phase,
                nearStation = nearCrossover.x,
                farStation = farCrossover.x,
                chordLength = farCrossover.x - nearCrossover.x,
                bondTransferRatioDuplexAxial = bond.peakDuplexAxialForce / ENTRY_FORCE,
                bondTransferRatioCrossover = bond.peakCrossoverForce / ENTRY_FORCE,
                bondEffectiveAllowable = bondAllowable,
                controlTransferRatioDuplexAxial = control.peakDuplexAxialForce / ENTRY_FORCE,
                controlTransferRatioCrossover = control.peakCrossoverForce / ENTRY_FORCE,
                controlEffectiveAllowable = controlAllowable,
                gainOverControl = bondAllowable / controlAllowable
            )
        }
    }
    println("crossover bonds (${crossoverBonds.size}) done at ${entryElapsed(started)}")

    // ---------------------------------------------------------------- the base-pair footprint
    val footprints = mutableListOf<FootprintPoint>()
    val rise = Gen1Tile.RISE_PER_BASE_PAIR

    fun footprintPoint(
        bases: Int,
        distribution: String,
        near: EntryTopology,
        far: EntryTopology,
        phase: Int = ENTRY_NOMINAL_PHASE
    ): FootprintPoint {
        val lattice = entryLattice(
            phase, subdivisions = 1,
            extraStations = near.stations + far.stations
        )
        val solution = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
        val axial = solution.peakDuplexAxialForce / ENTRY_FORCE
        val crossover = solution.peakCrossoverForce / ENTRY_FORCE
        val shear = solution.peakDuplexInPlaneShear / ENTRY_FORCE
        // the control that turns "the footprint relieves the peak by 14 %" into a statement
        // about SHEDDING rather than about topology: the same single-point attachment, read
        // at the inboard end of the same footprint
        val control = lattice.solve(
            lattice.tetherLoads(
                EntryTopology.singlePoint("control", mid, -edge),
                EntryTopology.singlePoint("control", mid, edge),
                ENTRY_FORCE
            )
        )
        val shedEnd = abs(
            lattice.axialForceAt(control, mid, -edge + (bases - 1) * rise + ENTRY_PROBE)
        )
        val allowable = joint.ruptureForce(
            bases.toDouble(), ShearJointAllowable.REFERENCE_LOADING_RATE
        )
        return FootprintPoint(
            bases = bases,
            footprintLength = (bases - 1) * rise,
            distribution = distribution,
            phaseBasePairs = phase,
            degreesOfFreedom = lattice.degreesOfFreedom,
            transferRatioDuplexAxial = axial,
            transferRatioCrossover = crossover,
            effectiveAllowable = effectiveAllowable(axial, crossover, shear),
            singlePointAtFootprintEnd = shedEnd,
            jointAllowable = allowable,
            jointModelValid = allowable > 0.0
        )
    }

    (1..20).forEach { bases ->
        footprints += footprintPoint(
            bases, "uniform over the footprint",
            EntryTopology.baseFootprint("near", mid, -edge, bases, rise, inward = true),
            EntryTopology.baseFootprint("far", mid, edge, bases, rise, inward = false)
        )
    }
    listOf(8, 20).forEach { bases ->
        footprints += footprintPoint(
            bases, "at the two ends of the footprint (the joint's own shear lag)",
            EntryTopology.endLoadedFootprint("near", mid, -edge, bases, rise, inward = true),
            EntryTopology.endLoadedFootprint("far", mid, edge, bases, rise, inward = false)
        )
    }
    // The apparent relief above is an accident of WHERE THE FIRST CROSSOVER COLUMN FALLS: at
    // the nominal phase it sits 0.96 nm from the rim, so most of a footprint lies past it and
    // sheds. The column phase is a design variable (C-0015), so the complete 32-phase sweep
    // is what decides whether a footprint is worth anything at all — and it is run for both
    // ends of the realistic 8-20 bp range.
    val footprintPhaseSweep = listOf(8, 20).flatMap { bases ->
        (0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD).map { phase ->
            footprintPoint(
                bases, "uniform, complete column-phase sweep",
                EntryTopology.baseFootprint("near", mid, -edge, bases, rise, inward = true),
                EntryTopology.baseFootprint("far", mid, edge, bases, rise, inward = false),
                phase
            )
        }
    }
    footprints += footprintPhaseSweep
    println("footprints done at ${entryElapsed(started)}")

    // ---------------------------------------------------------------- the oblique case
    val obliquePoints = mutableListOf<ObliquePoint>()
    val singlePointOblique = mutableMapOf<Int, Double>()
    for (phase in 0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD) {
        val lattice = entryLattice(phase)
        listOf(1, 2, 3).forEach { width ->
            for (from in 0..ENTRY_BEAM_COUNT - width) {
                for (to in 0..ENTRY_BEAM_COUNT - width) {
                    val near = EntryTopology.duplexBand("near", from, width, -edge)
                    val far = EntryTopology.duplexBand("far", to, width, edge)
                    val solution = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
                    val axial = solution.peakDuplexAxialForce / ENTRY_FORCE
                    val crossover = solution.peakCrossoverForce / ENTRY_FORCE
                    val shear = solution.peakDuplexInPlaneShear / ENTRY_FORCE
                    if (width == 1) singlePointOblique[to - from] = maxOf(
                        singlePointOblique[to - from] ?: 0.0, axial
                    )
                    obliquePoints += ObliquePoint(
                        bandWidth = width,
                        fromDuplex = from,
                        toDuplex = to,
                        duplexOffset = to - from,
                        phaseBasePairs = phase,
                        angleDegrees = atan2(
                            (to - from) * entryTopologySheet.interhelicalDistance,
                            Gen1Tile.EDGE_X
                        ) * 180.0 / PI,
                        transferRatioDuplexAxial = axial,
                        transferRatioCrossover = crossover,
                        effectiveAllowable = effectiveAllowable(axial, crossover, shear),
                        overEqualShare = axial * width,
                        reliefOverSinglePoint = 0.0
                    )
                }
            }
        }
    }
    val oblique = obliquePoints.map { point ->
        point.copy(
            reliefOverSinglePoint = (singlePointOblique[point.duplexOffset] ?: 1.0) /
                    point.transferRatioDuplexAxial
        )
    }.let { all ->
        // keep the worst design at each (band width, offset) — the complete ladder, summarised
        all.groupBy { it.bandWidth to it.duplexOffset }
            .map { (_, group) ->
                group[
                    entryArgExtremum(
                        group.map { it.transferRatioDuplexAxial }, largest = true
                    )
                ]
            }
            .sortedWith(compareBy({ it.bandWidth }, { it.duplexOffset }))
    }
    println("oblique ladder done at ${entryElapsed(started)}")

    // ---------------------------------------------------------------- the continuum control
    val continuumStations = mutableListOf<EntryContinuumStation>()
    listOf(2.0, entryShear).forEach { stiffness ->
        val lattice = entryLattice(
            ENTRY_NOMINAL_PHASE, crossoverStiffness = stiffness, connectorArm = 0.0
        )
        val continuum = ShearLagMembrane(
            stretchModulus = entryTopologySheet.duplex.stretchModulus,
            interhelicalDistance = entryTopologySheet.interhelicalDistance,
            crossoverSpacing = entryTopologySheet.crossoverSpacing,
            crossoverShearStiffness = stiffness,
            lengthX = Gen1Tile.EDGE_X,
            duplexes = ENTRY_BEAM_COUNT,
            modes = 1200
        )
        listOf(1, 2, 4).forEach { width ->
            val near = EntryTopology.duplexBand("near", mid, width, -edge)
            val far = EntryTopology.duplexBand("far", mid, width, edge)
            val solution = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
            listOf(0.0, 5.0, -8.0).forEach { x ->
                listOf(mid, mid + 1, 0).forEach { duplex ->
                    val latticeForce = lattice.axialForceAt(solution, duplex, x)
                    // the continuum takes the same band as a superposition of chords, each
                    // integrated over its own duplex's TRIBUTARY STRIP — never sampled on the
                    // axis, which broke the sum rule by 130 % in T-15
                    val continuumForce = (0 until width).sumOf { i ->
                        continuum.duplexAxialForce(
                            ChordLoad(
                                -edge, continuum.duplexY(mid + i), edge,
                                continuum.duplexY(mid + i), ENTRY_FORCE / width
                            ),
                            x, duplex
                        )
                    }
                    continuumStations += EntryContinuumStation(
                        bandWidth = width,
                        crossoverShearStiffness = stiffness,
                        x = x,
                        duplex = duplex,
                        lattice = latticeForce,
                        continuum = continuumForce,
                        difference = latticeForce - continuumForce,
                        excess = if (abs(continuumForce) > 0.01 * ENTRY_FORCE &&
                            abs(latticeForce) > 0.01 * ENTRY_FORCE
                        ) latticeForce / continuumForce else 0.0
                    )
                }
            }
        }
    }
    println("continuum control done at ${entryElapsed(started)}")

    // ---------------------------------------------------------------- the joint's allowable
    val rates = listOf(
        ShearJointAllowable.SLOWEST_MEASURED_LOADING_RATE,
        ShearJointAllowable.REFERENCE_LOADING_RATE,
        ShearJointAllowable.FASTEST_MEASURED_LOADING_RATE
    )
    val jointLengths = rates.flatMap { rate ->
        listOf(4, 6, 8, 10, 12, 14, 16, 20, 24, 30, 32, 40).map { bases ->
            JointLengthPoint(
                basePairs = bases,
                loadingRate = rate,
                ruptureForce = joint.ruptureForce(bases.toDouble(), rate),
                splitGainTwoWays = joint.splitGain(bases.toDouble(), 2, rate),
                splitGainThreeWays = joint.splitGain(bases.toDouble(), 3, rate)
            )
        }
    }
    val jointBreakEven = rates.flatMap { rate ->
        listOf(2, 3).map { ways ->
            JointBreakEven(
                ways = ways,
                loadingRate = rate,
                breakEvenBasePairs = joint.splitBreakEven(ways, rate),
                saturationForce = joint.saturationForce
            )
        }
    }

    // ---------------------------------------------------------------- the design points
    val referenceRate = ShearJointAllowable.REFERENCE_LOADING_RATE
    val singleForces = topologies[0]
    val bandTwo = topologies[1]
    val bandFour = topologies[3]

    fun designPoint(
        name: String,
        forces: TopologyForces,
        bondsPerEnd: Int,
        basePairsPerBond: Int,
        geometry: String
    ): EntryDesignPoint {
        val jointAllowable = if (geometry == "unzip") ShearJointAllowable.UNZIP_ALLOWABLE
        else joint.ruptureForce(basePairsPerBond.toDouble(), referenceRate)
        // the tension the JOINT permits: each bond carries its own share of it
        val jointLimited = jointAllowable / forces.largestShare
        // the tension the SHEET permits, in C-0020's own convention
        val sheetLimited = forces.effectiveAllowable
        val effective = minOf(jointLimited, sheetLimited)
        val binding = if (jointLimited < sheetLimited) {
            "the tether's own $geometry joint, $basePairsPerBond bp per bond"
        } else forces.bindingPath
        return EntryDesignPoint(
            name = name,
            bondsPerEnd = bondsPerEnd,
            bondedBasePairsPerBond = basePairsPerBond,
            totalBondedBasePairs = bondsPerEnd * basePairsPerBond,
            geometry = geometry,
            largestShare = forces.largestShare,
            transferRatioDuplexAxial = forces.transferRatioDuplexAxial,
            transferRatioCrossover = forces.transferRatioCrossover,
            jointAllowablePerBond = jointAllowable,
            effectiveAllowable = effective,
            bindingPath = binding,
            concentrationFactor = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / effective,
            minimumLengthAtAcceptableStroke = minimumTetherLength(
                Gen1Tile.ACCEPTABLE_STROKE, effective
            ),
            minimumLengthAtDesiredStroke = minimumTetherLength(
                Gen1Tile.DESIRED_STROKE, effective
            ),
            assemblyFootprintAtDesiredStroke = Gen1Tile.EDGE_X +
                    2.0 * minimumTetherLength(Gen1Tile.DESIRED_STROKE, effective),
            tetherTension = effective,
            normalPreloadTotal = normalPreload(effective, ENTRY_TETHER_COUNT),
            normalPreloadFractionOfTarget =
                normalPreload(effective, ENTRY_TETHER_COUNT) / Gen1Tile.TARGET_FORCE,
            holdDownLengthAtDesiredStroke = holdDownLength(
                Gen1Tile.DESIRED_STROKE, HOLD_DOWN_SCALE, ENTRY_TETHER_COUNT
            )
        )
    }

    val designPoints = listOf(
        designPoint(
            "C-0020 as filed: one point on one duplex, 48 pN taken as the allowable",
            singleForces, 1, 30, "shear (30 bp, the length the 48 pN was measured at)"
        ),
        designPoint(
            "one point on one duplex, a realistic 16 bp staple extension",
            singleForces, 1, 16, "shear"
        ),
        designPoint(
            "one point on one duplex, the whole 32 bp staple on one duplex",
            singleForces, 1, STAPLE_BUDGET_BASE_PAIRS, "shear"
        ),
        designPoint(
            "two duplexes, the same 32 bp staple SPLIT into two 16 bp domains",
            bandTwo, 2, STAPLE_BUDGET_BASE_PAIRS / 2, "shear"
        ),
        designPoint(
            "two duplexes, 30 bp on each (the length the 48 pN was measured at)",
            bandTwo, 2, 30, "shear"
        ),
        designPoint(
            "four duplexes, the same 32 bp staple split into four 8 bp domains",
            bandFour, 4, STAPLE_BUDGET_BASE_PAIRS / 4, "shear"
        ),
        designPoint(
            "four duplexes, 16 bp on each (four times the staple budget)",
            bandFour, 4, 16, "shear"
        ),
        designPoint(
            "one point on one duplex, joint presented in UNZIP geometry",
            singleForces, 1, 16, "unzip"
        ),
        designPoint(
            "two duplexes, both joints presented in UNZIP geometry",
            bandTwo, 2, 16, "unzip"
        ),
        designPoint(
            "four duplexes, all joints presented in UNZIP geometry",
            bandFour, 4, 16, "unzip"
        )
    )
    println("design points done at ${entryElapsed(started)}")

    // ---------------------------------------------------------------- convergence
    val convergence = mutableListOf<EntryConvergencePoint>()
    listOf(1, 2, 4).forEach { subdivisions ->
        val near = EntryTopology.duplexBand("near", mid, 2, -edge)
        val far = EntryTopology.duplexBand("far", mid, 2, edge)
        val lattice = entryLattice(ENTRY_NOMINAL_PHASE, subdivisions = subdivisions)
        val solution = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
        convergence += EntryConvergencePoint(
            parameter = "subdivisions, two-duplex band",
            value = subdivisions.toDouble(),
            degreesOfFreedom = lattice.degreesOfFreedom,
            peakDuplexAxial = solution.peakDuplexAxialForce,
            peakCrossover = solution.peakCrossoverForce,
            regularisationForce = solution.regularisationForceAlong
        )
    }
    listOf(1, 2, 4).forEach { subdivisions ->
        val near = EntryTopology.baseFootprint("near", mid, -edge, 8, rise, inward = true)
        val far = EntryTopology.baseFootprint("far", mid, edge, 8, rise, inward = false)
        val lattice = entryLattice(
            ENTRY_NOMINAL_PHASE, subdivisions = subdivisions,
            extraStations = near.stations + far.stations
        )
        val solution = lattice.solve(lattice.tetherLoads(near, far, ENTRY_FORCE))
        convergence += EntryConvergencePoint(
            parameter = "subdivisions, 8 bp footprint",
            value = subdivisions.toDouble(),
            degreesOfFreedom = lattice.degreesOfFreedom,
            peakDuplexAxial = solution.peakDuplexAxialForce,
            peakCrossover = solution.peakCrossoverForce,
            regularisationForce = solution.regularisationForceAlong
        )
    }
    println("convergence done at ${entryElapsed(started)}")

    // ---------------------------------------------------------------- verdict
    val ladderTwo = bandLadder[1]
    val crossoverGain = crossoverBonds.map { it.gainOverControl }
    val splitTwo = splitComparison.filter { it.bandWidth == 2 }
    val worstSplit = splitTwo.maxOf { it.compatibleOverEqual }
    val worstSplitPoint = splitTwo[
        entryArgExtremum(splitTwo.map { it.compatibleOverEqual }, largest = true)
    ]
    // how close the crossover path ever gets to becoming the binding one, over the whole
    // ladder: the ratio of the crossover-limited tension to the duplex-limited one
    val crossoverMargin = ladderPoints.filter { it.transferRatioCrossover > 1e-9 }.minOf {
        (Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / it.transferRatioCrossover) /
                (Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / it.transferRatioDuplexAxial)
    }
    val validFootprints = footprints.filter {
        it.distribution.startsWith("uniform over") && it.jointModelValid
    }
    val footprintPeak = footprints.filter { it.distribution.startsWith("uniform over") }
    val breakEvenReference = jointBreakEven.first {
        it.ways == 2 && it.loadingRate == referenceRate
    }
    val verdict = linkedMapOf(
        "the headline survives, and it was never the sheet's doing" to (
                "C-0020's eta = 1.0000 is reproduced exactly by the general load introduction " +
                        "(%.4f), and every entry topology obeys the pigeonhole floor 1/D = " +
                        "%.4f. But the sheet has almost nothing to say about the entry " +
                        "topology: a bond spanning m duplexes enters at exactly 1/m and the " +
                        "peak exceeds that by at most %.2f %%, so the ladder is arithmetic, " +
                        "not mechanics. What the entry topology really decides is the " +
                        "allowable of the JOINT, and that is a sequence-design choice with a " +
                        "measured law behind it."
                ).format(
                singleForces.transferRatioDuplexAxial, 1.0 / ENTRY_BEAM_COUNT,
                100.0 * (bandLadder.maxOf { it.worstOverEqualShare } - 1.0)
            ),
        "the halving is exact to a per cent, and it barely depends on which pair" to (
                "A two-duplex bond with an equal split gives eta = %.4f against the exact 1/2 " +
                        "at the best of the %d (pair, phase) designs and %.4f at the worst — " +
                        "a %.1f %% spread over every pair and every column phase. A RIGID " +
                        "staple splits %.4f / %.4f instead of 50/50 at worst (duplexes %d and " +
                        "%d, phase %d bp), which is x%.4f on the peak. So the two split " +
                        "limits — a compliant staple and a rigid one — bracket the answer to " +
                        "under %.1f %%, and the halving needs no model of the staple's own " +
                        "elasticity, which is the one thing nothing in the literature supplies."
                ).format(
                ladderTwo.bestTransferRatioDuplexAxial,
                splitTwo.size,
                ladderTwo.worstTransferRatioDuplexAxial,
                100.0 * (ladderTwo.worstTransferRatioDuplexAxial /
                        ladderTwo.bestTransferRatioDuplexAxial - 1.0),
                worstSplitPoint.largestCompatibleShare,
                worstSplitPoint.smallestCompatibleShare,
                worstSplitPoint.firstDuplex, worstSplitPoint.firstDuplex + 1,
                worstSplitPoint.phaseBasePairs,
                splitTwo.maxOf { it.peakRatio },
                100.0 * (worstSplit - 1.0)
            ),
        "what it costs in the crossover path: nothing, at any band width" to (
                "It PAYS. Both bonded duplexes move together, so the interface between them " +
                        "slides less rather than more: the peak crossover force falls from " +
                        "%.4f to %.4f per pN of tension going from one duplex to two. Over " +
                        "the whole ladder of %d designs the crossover path never becomes the " +
                        "binding one — its closest approach is x%.2f the duplex-limited " +
                        "tension, and it recedes as the band widens, because splitting the " +
                        "load across duplexes relieves the interfaces FASTER than it relieves " +
                        "the duplexes. The binding path is the duplex axial force at every " +
                        "one of them."
                ).format(
                singleForces.transferRatioCrossover, bandTwo.transferRatioCrossover,
                ladderPoints.size, crossoverMargin
            ),
        "bonding onto a crossover buys nothing the station does not buy" to (
                "A tether bonded onto a crossover is a two-duplex bond that is forced to sit " +
                        "at an interior station, and the control that isolates the two — one " +
                        "point on one duplex at the SAME station — shows the topology worth " +
                        "x%.3f to x%.3f in the effective allowable over %d designs, against " +
                        "the x2.00 the two-duplex bond is worth at the rim. The crossover " +
                        "adds nothing mechanically: what it costs is the chord, %.1f nm " +
                        "instead of 40, because the columns sit strictly inside the footprint."
                ).format(
                crossoverGain.min(), crossoverGain.max(), crossoverBonds.size,
                crossoverBonds.first().chordLength
            ),
        "the footprint is not a sheet variable: nothing at 8 bp, 8.6 % at 20 bp" to (
                "Spreading the bond over a footprint leaves m = 1, so the duplex must still " +
                        "carry the whole tension inboard of it. At the nominal phase the peak " +
                        "does fall, from %.4f at 1 bp to %.4f at 20 bp — but the SAME " +
                        "single-point attachment read at the inboard end of the same " +
                        "footprint carries %.4f, so that relief is the load SHED past the " +
                        "first crossover column and not a property of the entry topology at " +
                        "all. The column phase decides where that column falls, and over the " +
                        "complete 32-phase sweep the worst case is eta = %.4f at 8 bp and " +
                        "%.4f at 20 bp: at the phases that put the first column further from " +
                        "the rim than the footprint is long, the whole bond enters before " +
                        "anything can shed and the footprint buys NOTHING. A footprint is not " +
                        "a sheet variable. What it moves is the JOINT: 8 to 20 bp takes the " +
                        "shear allowable from %.1f to %.1f pN, x%.2f, and THAT is the whole " +
                        "of what it buys."
                ).format(
                footprintPeak.first().transferRatioDuplexAxial,
                footprintPeak.last().transferRatioDuplexAxial,
                footprintPeak.last().singlePointAtFootprintEnd,
                footprintPhaseSweep.filter { it.bases == 8 }
                    .maxOf { it.transferRatioDuplexAxial },
                footprintPhaseSweep.filter { it.bases == 20 }
                    .maxOf { it.transferRatioDuplexAxial },
                validFootprints.first { it.bases == 8 }.jointAllowable,
                validFootprints.first { it.bases == 20 }.jointAllowable,
                validFootprints.first { it.bases == 20 }.jointAllowable /
                        validFootprints.first { it.bases == 8 }.jointAllowable
            ),
        "splitting a bond has a break-even length, and a realistic staple straddles it" to (
                "On the JOINT side the two effects run against each other: splitting a bond " +
                        "of n base pairs into m domains of n/m divides the load by m but also " +
                        "shortens each domain. Strunz's own scaling — the barrier separation " +
                        "linear in n with a 7 A offset, ln(off-rate) linear in n — makes the " +
                        "rupture force concave and saturating at %.1f pN, so splitting WINS " +
                        "above a total bonded length of %.1f bp and LOSES below it. At the " +
                        "reference 100 pN/s a 32 bp staple split into two 16 bp domains gains " +
                        "x%.3f on the joint AND x2.00 on the sheet; an 8 bp staple split into " +
                        "two 4 bp domains loses x%.3f on the joint and cannot be recovered by " +
                        "any sheet effect."
                ).format(
                joint.saturationForce, breakEvenReference.breakEvenBasePairs,
                joint.splitGain(32.0, 2, referenceRate),
                1.0 / joint.splitGain(8.0, 2, referenceRate)
            ),
        "the 48 pN is a 30 bp number, and that is the load-bearing discovery" to (
                "C-0020, C-0014 and C-0009 all consume 48 pN as the per-path shear allowable. " +
                        "It is Strunz's measurement on a 30 bp duplex, and the entry topology " +
                        "is exactly what fixes the length: a realistic 16 bp staple extension " +
                        "gives %.1f pN and an 8 bp one %.1f pN. So the one-point model is not " +
                        "conservative about the attachment — it is optimistic by x%.2f unless " +
                        "the tether's joint is 30 bp long, which nothing in the programme has " +
                        "ever said it is. This is CH-0029."
                ).format(
                joint.ruptureForce(16.0, referenceRate),
                joint.ruptureForce(8.0, referenceRate),
                joint.ruptureForce(30.0, referenceRate) / joint.ruptureForce(16.0, referenceRate)
            ),
        "layout is still worth exactly nothing, and now for a second reason" to (
                "Over the complete 32 base-pair phase sweep the binding duplex-axial path " +
                        "moves by x%.4f at a band width of one and x%.4f at two — the " +
                        "prescribed split makes the entered share a matter of arithmetic, and " +
                        "no arrangement of crossovers can touch it. The RIGID split does " +
                        "depend on the layout, because the compliance matrix does, and it is " +
                        "worth x%.4f in the largest share: below C-0015's x1.43-1.60 out of " +
                        "plane, and well below the topology's own x2.00. C-0020's 'exactly " +
                        "nothing' is exact at a band width of one and holds to a per cent " +
                        "everywhere else."
                ).format(
                bandLadder[0].layoutSpanDuplexAxial, ladderTwo.layoutSpanDuplexAxial,
                splitComparison.filter { it.bandWidth == 2 }.maxOf { it.largestCompatibleShare } /
                        splitComparison.filter { it.bandWidth == 2 }
                            .minOf { it.largestCompatibleShare }
            ),
        "the oblique overshoot is relieved, but not by the topology alone" to (
                "C-0020's worst aligned-family overshoot — an oblique chord whose moment the " +
                        "crossovers react as an axial couple — reaches eta = %.4f at a single " +
                        "point and %.4f through a two-duplex bond, a relief of x%.2f against " +
                        "the x2.00 the same bond buys when aligned. The couple is applied by " +
                        "the chord's transverse component and a band at one station barely " +
                        "changes its arm; what the band does is divide the axial share, and " +
                        "only that part of the overshoot follows."
                ).format(
                oblique.filter { it.bandWidth == 1 }.maxOf { it.transferRatioDuplexAxial },
                oblique.filter { it.bandWidth == 2 }.maxOf { it.transferRatioDuplexAxial },
                oblique.filter { it.bandWidth == 1 }.maxOf { it.transferRatioDuplexAxial } /
                        oblique.filter { it.bandWidth == 2 }.maxOf { it.transferRatioDuplexAxial }
            ),
        "which way the net design moves" to (
                "A topology that raises the effective allowable shortens the minimum tether " +
                        "and RAISES the preload at that minimum, because F_z = n A sqrt(2A/S) " +
                        "goes as A^1.5. The 32 bp staple split across two duplexes takes " +
                        "A_eff from %.1f to %.1f pN, L_min at the desired 10 nm stroke from " +
                        "%.1f to %.1f nm, and the four-tether preload from %.1f to %.1f pN — " +
                        "i.e. from %.0f %% to %.0f %% of the §3 100 pN target. C-0021 wants a " +
                        "downward preload but only %.2f pN of it, so everything above that is " +
                        "a tax on the actuator, and the design should sit at whatever tether " +
                        "length delivers the wanted preload rather than at L_min: %.1f nm at " +
                        "the 10 nm stroke. The entry topology's value is that it makes MORE " +
                        "of that length axis admissible, not that the shortest tether is the " +
                        "one to build."
                ).format(
                designPoints[2].effectiveAllowable, designPoints[3].effectiveAllowable,
                designPoints[2].minimumLengthAtDesiredStroke,
                designPoints[3].minimumLengthAtDesiredStroke,
                designPoints[2].normalPreloadTotal, designPoints[3].normalPreloadTotal,
                100.0 * designPoints[2].normalPreloadFractionOfTarget,
                100.0 * designPoints[3].normalPreloadFractionOfTarget,
                HOLD_DOWN_SCALE, designPoints[3].holdDownLengthAtDesiredStroke
            ),
        "maturity" to "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "No in-plane force in a loaded origami sheet has ever been measured; the " +
                "crossover's in-plane stiffness is not in the literature in any form; and the " +
                "joint allowable is an Evans-Ritchie extrapolation of a 1999 AFM measurement " +
                "on free oligonucleotides, not on a staple in a sheet."
    )

    val result = EntryTopologyResult(
        task = "T-19",
        leaf = "A8.2",
        title = "The attachment's entry topology: what a tether actually bonds to",
        verificationType = "in-silico (C-0020's in-plane membrane lattice, loaded through " +
                "four entry topologies instead of one, with the orthotropic shear-lag " +
                "membrane run beside it) + logical (a cut-equilibrium pigeonhole that bounds " +
                "every topology before a matrix is assembled) + literature (the joint's own " +
                "allowable as a function of bonded length, from C-0006's own primary source)",
        acceptance = "The peak per-load-path force for the entry topologies an origami " +
                "attachment actually has, against the one-point-on-one-duplex model, " +
                "propagated into C-0014's tether geometry and C-0021's preload",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= mN/m)",
            "stretchModulus" to "pN",
            "bendingRigidity" to "pN*nm^2",
            "loadingRate" to "pN/s",
            "transferRatio" to "dimensionless, per pN of applied tether force",
            "temperature" to "K"
        ),
        conventions = listOf(
            "x along the helices, y across them; the origin of (x, y) is the centre of the " +
                    "footprint; T-15's conventions, unchanged",
            "an ENTRY TOPOLOGY is a set of bonds, each (duplex, station, share), with the " +
                    "shares summing to one; C-0020's attachment is one bond with share 1",
            "a tether pair's pull direction is the line joining the two ends' CENTROIDS, " +
                    "which keeps the resultants collinear and the load case moment-free",
            "TRANSFER RATIO eta = peak force in one load path / applied tether force; " +
                    "CONCENTRATION FACTOR C = the same peak / the equal share",
            "the PRESCRIBED split gives every bond 1/m of the tension (a compliant staple); " +
                    "the COMPATIBLE split makes all bonds move together (a rigid staple) and " +
                    "is m springs in parallel between two rigid ends",
            "a bond station is an exact multiple of the 0.34 nm rise and is made a node, so " +
                    "the axial force it introduces is resolved rather than averaged"
        ),
        validity = listOf(
            "TRL 1-3, model-consistent and traceable; nothing here is measured",
            "everything structural is C-0020's, unchanged — the same sheet, the same " +
                    "crossover stiffness, the same phase machinery, the same allowables — so " +
                    "any difference reported here is the ENTRY TOPOLOGY and nothing else",
            "the compatible split is defined for an ALIGNED pull only: an unequal split at " +
                    "one end of an oblique chord carries a couple that nothing in this model " +
                    "reacts, C-0010's lateral stiffness being exactly zero",
            "the staple's own elasticity is not modelled; the two split limits bracket it",
            "the joint allowable is Strunz et al.'s single-barrier model with THEIR published " +
                    "constants, used inside their measured 16-4000 pN/s and quoted as a RATIO " +
                    "wherever a design decision rests on it; the Evans-Ritchie form has no " +
                    "equilibrium plateau and must not be extrapolated to zero loading rate",
            "the joint allowable is measured on free oligonucleotides pulled at opposite " +
                    "5' ends, not on a staple domain inside a sheet, and no measurement of " +
                    "the latter exists",
            "no out-of-plane coupling, on C-0020's flat-sheet argument and with its caveat",
            "single layer, static, 300 K, aqueous buffer with Mg2+"
        ),
        parameters = EntryTopologyParameters(
            temperature = 300.0,
            medium = "aqueous buffer with Mg2+",
            footprintAlong = Gen1Tile.EDGE_X,
            footprintAcross = nominal.lengthY,
            duplexes = ENTRY_BEAM_COUNT,
            interhelicalDistance = entryTopologySheet.interhelicalDistance,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
            risePerBasePair = Gen1Tile.RISE_PER_BASE_PAIR,
            stretchModulus = entryTopologySheet.duplex.stretchModulus,
            bendingRigidity = entryTopologySheet.duplex.bendingRigidity,
            crossoverInPlaneStiffness = entryShear,
            connectorArm = entryTopologySheet.interhelicalDistance / 2.0,
            regularisation = OrigamiMembrane.DEFAULT_REGULARISATION,
            subdivisions = OrigamiMembrane.DEFAULT_SUBDIVISIONS,
            nominalPhaseBasePairs = ENTRY_NOMINAL_PHASE,
            appliedTetherForce = ENTRY_FORCE,
            unzipAllowable = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            shearAllowable = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
            overstretchingCeiling = Gen1Tile.OVERSTRETCHING_CEILING,
            jointSeparationOffset = joint.separationOffset,
            jointSeparationPerBasePair = joint.separationPerBasePair,
            jointOffRateIntercept = joint.offRateExponentIntercept,
            jointOffRateSlope = joint.offRateExponentSlope,
            jointReferenceLoadingRate = referenceRate,
            provenance = mapOf(
                "structural constants" to "C-0020 / C-0009 / C-0015, unchanged",
                "joint allowable" to "CITED, MEASURED — Strunz, Oroszlan, Schaefer & " +
                        "Guentherodt, PNAS 96:11277 (1999), Eqs. 1-3 with their own fitted " +
                        "alpha = 3 +/- 1, beta = 0.5 +/- 0.1 and a barrier separation of " +
                        "0.7 A per base pair with a 7 A offset. The 48 +/- 2 pN this " +
                        "programme carries is their 30 bp point at 50 nm/s",
                "unzip allowable" to "CITED, MEASURED — Essevaz-Roulet et al., PNAS 94:11935 " +
                        "(1997); length-INDEPENDENT, which is why splitting multiplies it by m",
                "nicked ceiling" to "CITED, MEASURED — van Mameren et al., PNAS 106:18231 (2009)",
                "hold-down scale" to "C-0021, k_BT / 3.0 nm read as a mean excursion",
                "strokes, target force, footprint" to "the problem definition, §3"
            )
        ),
        bounds = bounds,
        topologies = topologies,
        bandLadder = bandLadder,
        bandLadderAtNominalPhase = ladderPoints.filter {
            it.phaseBasePairs == ENTRY_NOMINAL_PHASE
        },
        splitComparison = splitComparison,
        crossoverBonds = crossoverBonds.filter { it.phaseBasePairs == ENTRY_NOMINAL_PHASE },
        footprints = footprints,
        oblique = oblique,
        continuumStations = continuumStations,
        jointLengths = jointLengths,
        jointBreakEven = jointBreakEven,
        designPoints = designPoints,
        convergence = convergence,
        verdict = verdict
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-19-attachment-entry-topology.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n"
    )

    // ---------------------------------------------------------------- console
    println()
    println("--- the cheap bounds ".padEnd(110, '-'))
    bounds.forEach { println("%14.6f  %s".format(it.value, it.name)) }
    println()
    println("--- the named entry topologies, per pN of applied tether force ".padEnd(110, '-'))
    println(
        "%56s %8s %8s %8s %8s %8s".format(
            "topology", "share", "eta_ax", "eta_xo", "peak/sh", "A_eff"
        )
    )
    topologies.forEach {
        println(
            "%56s %8.4f %8.4f %8.4f %8.4f %8.2f".format(
                it.label.take(56), it.largestShare, it.transferRatioDuplexAxial,
                it.transferRatioCrossover, it.peakOverLargestShare, it.effectiveAllowable
            )
        )
    }
    println()
    println("--- the complete band ladder ".padEnd(110, '-'))
    println(
        "%3s %8s %9s %9s %9s %9s %9s %38s".format(
            "m", "designs", "1/m", "worst", "worst/1/m", "A_eff", "layout", "binding path"
        )
    )
    bandLadder.forEach {
        println(
            "%3d %8d %9.4f %9.4f %9.4f %9.2f %9.4f %38s".format(
                it.bandWidth, it.designs, it.equalShare, it.worstTransferRatioDuplexAxial,
                it.worstOverEqualShare, it.worstEffectiveAllowable, it.layoutSpanDuplexAxial,
                it.bindingPathAtWorst.take(38)
            )
        )
    }
    println()
    println("--- the base-pair footprint ".padEnd(110, '-'))
    println("%6s %10s %9s %9s %10s %10s".format("bases", "length", "eta_ax", "eta_xo", "A_joint", "dofs"))
    footprints.filter { !it.distribution.contains("phase sweep") }.forEach {
        println(
            "%6d %10.3f %9.4f %9.4f %10.2f %10d".format(
                it.bases, it.footprintLength, it.transferRatioDuplexAxial,
                it.transferRatioCrossover, it.jointAllowable, it.degreesOfFreedom
            )
        )
    }
    listOf(8, 20).forEach { bases ->
        val sweep = footprintPhaseSweep.filter { it.bases == bases }
        println(
            "%6d bp over all %d column phases: eta_ax %.4f .. %.4f".format(
                bases, sweep.size, sweep.minOf { it.transferRatioDuplexAxial },
                sweep.maxOf { it.transferRatioDuplexAxial }
            )
        )
    }
    println()
    println("--- bonded onto a crossover, against a one-point control at the same station ".padEnd(110, '-'))
    println(
        "%6s %8s %8s %9s %9s %9s %9s %8s".format(
            "iface", "nearX", "chord", "eta_ax", "eta_xo", "A_eff", "A_ctrl", "gain"
        )
    )
    crossoverBonds.filter { it.phaseBasePairs == ENTRY_NOMINAL_PHASE }.forEach {
        println(
            "%6d %8.2f %8.2f %9.4f %9.4f %9.2f %9.2f %8.3f".format(
                it.lowerBeam, it.nearStation, it.chordLength,
                it.bondTransferRatioDuplexAxial, it.bondTransferRatioCrossover,
                it.bondEffectiveAllowable, it.controlEffectiveAllowable, it.gainOverControl
            )
        )
    }
    println()
    println("--- the oblique ladder, worst design at each (band width, offset) ".padEnd(110, '-'))
    println(
        "%3s %8s %9s %9s %9s %9s %9s".format(
            "m", "offset", "angle", "eta_ax", "eta_xo", "A_eff", "relief"
        )
    )
    oblique.filter { it.duplexOffset >= 0 }.forEach {
        println(
            "%3d %8d %9.2f %9.4f %9.4f %9.2f %9.3f".format(
                it.bandWidth, it.duplexOffset, it.angleDegrees,
                it.transferRatioDuplexAxial, it.transferRatioCrossover,
                it.effectiveAllowable, it.reliefOverSinglePoint
            )
        )
    }
    println()
    println("--- the joint's own allowable ".padEnd(110, '-'))
    println("%6s %10s %10s %10s %10s".format("bp", "rate", "F [pN]", "split x2", "split x3"))
    jointLengths.filter { it.loadingRate == referenceRate }.forEach {
        println(
            "%6d %10.0f %10.2f %10.4f %10.4f".format(
                it.basePairs, it.loadingRate, it.ruptureForce, it.splitGainTwoWays,
                it.splitGainThreeWays
            )
        )
    }
    jointBreakEven.forEach {
        println(
            "break-even at %d ways, %.0f pN/s: %.2f bp (saturation %.1f pN)".format(
                it.ways, it.loadingRate, it.breakEvenBasePairs, it.saturationForce
            )
        )
    }
    println()
    println("--- the design points, carried into C-0014's geometry ".padEnd(110, '-'))
    println(
        "%62s %8s %8s %8s %8s %8s".format(
            "design", "A_eff", "L(3)", "L(10)", "F_z", "%target"
        )
    )
    designPoints.forEach {
        println(
            "%62s %8.2f %8.2f %8.2f %8.1f %8.0f".format(
                it.name.take(62), it.effectiveAllowable,
                it.minimumLengthAtAcceptableStroke, it.minimumLengthAtDesiredStroke,
                it.normalPreloadTotal, 100.0 * it.normalPreloadFractionOfTarget
            )
        )
    }
    println()
    println("--- convergence ".padEnd(110, '-'))
    convergence.forEach {
        println(
            "%28s %6.0f  dofs %6d  axial %10.7f  crossover %10.7f  bed %10.2e".format(
                it.parameter, it.value, it.degreesOfFreedom, it.peakDuplexAxial,
                it.peakCrossover, it.regularisationForce
            )
        )
    }
    println()
    verdict.forEach { (key, value) -> println("$key: $value"); println() }
    println("written: ${output.path} in ${entryElapsed(started)}")

    // ------------------------------------------------- the falsifiers, as runtime checks
    check(abs(singleForces.transferRatioDuplexAxial - 1.0) < 1e-4) {
        "the general entry-topology load introduction must reproduce C-0020's one point on " +
                "one duplex exactly, or nothing in T-19 is a comparison with it"
    }
    check(ladderPoints.all { it.transferRatioDuplexAxial >= 1.0 / ENTRY_BEAM_COUNT - 1e-5 }) {
        "no entry topology can carry less than the pigeonhole floor 1/D in its worst duplex: " +
                "the axial forces on a cut sum to the applied force"
    }
    check(
        bandLadder.all {
            it.worstOverEqualShare >= 1.0 - 1e-4 && it.worstOverEqualShare < 1.1
        }
    ) {
        "an m-duplex bond with an equal split must enter at 1/m and may exceed it only by " +
                "the connector arm's rotation coupling, which is a few per cent"
    }
    check(splitComparison.all { it.compatibleOverEqual >= 1.0 - 1e-9 }) {
        "a rigid bond loads its stiffest path harder than an equal share, by convexity of " +
                "the parallel-spring split; a value below one means the compliance matrix is " +
                "not what it is claimed to be"
    }
    check(convergence.all { abs(it.regularisationForce) < 1e-9 }) {
        "the regularising bed must carry none of the load; there is no in-plane foundation"
    }
    check(
        footprints.all {
            it.transferRatioDuplexAxial <= 1.0 + 1e-4 &&
                    it.transferRatioDuplexAxial >= it.singlePointAtFootprintEnd - 1e-4
        }
    ) {
        "a footprint on ONE duplex can relieve it by exactly the load shed over its own " +
                "length and no more: the peak must lie between the applied tension and what " +
                "a single-point attachment carries at the inboard end of the same footprint"
    }
}
