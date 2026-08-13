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
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Task `T-15` / leaf `A8.2` — the **in-plane (membrane) load path** into the Gen-1 tile, by
 * shear lag, and the in-plane force-concentration factor that replaces `C-0009`'s
 * out-of-plane one where `C-0014` had to use it as a conservative stand-in.
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.InPlaneLoadPathStudyKt
 * ```
 *
 * Emits `gpd/results/T-15-in-plane-shear-lag.json`, deterministically.
 */

// --------------------------------------------------------------------------- records

/** Every parameter the run consumed, logged so the result is reproducible from the file alone. */
@Serializable
data class InPlaneParameters(
    val temperature: Double,
    val medium: String,
    val footprintAlong: Double,
    val footprintAcross: Double,
    val duplexes: Int,
    val crossoverColumns: Int,
    val crossovers: Int,
    val interhelicalDistance: Double,
    val interhelicalProvenance: String,
    val crossoverSpacing: Double,
    val crossoverSpacingBasePairs: Double,
    val crossoverSpacingProvenance: String,
    val stretchModulus: Double,
    val stretchModulusProvenance: String,
    val bendingRigidity: Double,
    val bendingRigidityProvenance: String,
    val crossoverInPlaneStiffness: Double,
    val crossoverInPlaneProvenance: String,
    val crossoverInPlaneSweep: List<Double>,
    val connectorArm: Double,
    val connectorArmProvenance: String,
    val regularisation: Double,
    val subdivisions: Int,
    val degreesOfFreedom: Int,
    val appliedTetherForce: Double,
    val unzipAllowable: Double,
    val shearAllowable: Double,
    val overstretchingCeiling: Double,
    val allowableProvenance: String
)

/** The shear-lag regime at one crossover in-plane stiffness — the cheap bound. */
@Serializable
data class ShearLagRegime(
    val crossoverShearStiffness: Double,
    val transferLength: Double,
    val neighbourLength: Double,
    val sharingLength: Double,
    val aspectRatio: Double,
    val crossoversWithinNeighbourLength: Double,
    val sharingLengthOverFootprint: Double,
    val regime: String
)

/** The per-load-path forces of one solved in-plane case, per pN of applied tether force. */
@Serializable
data class InPlanePathForces(
    val label: String,
    val peakDuplexAxial: Double,
    val peakCrossover: Double,
    val peakDuplexInPlaneShear: Double,
    val transferRatioDuplexAxial: Double,
    val transferRatioCrossover: Double,
    val transferRatioDuplexInPlaneShear: Double,
    val concentrationDuplexAxial: Double,
    val concentrationCrossover: Double,
    val effectiveAllowableCrossoverAsUnzip: Double,
    val effectiveAllowableCrossoverAsShear: Double,
    val bindingPath: String
)

/**
 * One placement of an opposed tether pair on the tile perimeter: in at duplex [fromDuplex]
 * on the `−x` edge and out at duplex [toDuplex] on the `+x` edge, pulling along the chord
 * between them.
 *
 * The **complete** design space of an edge-to-edge in-plane tether pair, because a tether
 * attaches to a duplex and there are only fifteen of them. Sweeping a continuous *angle*
 * instead would sample this lattice unevenly — several nominal angles snap to the same pair
 * of duplexes and therefore to the same physical design — which is `C-0015`'s lesson about
 * searching the diagonal of a discrete anisotropic space, in a new place.
 */
@Serializable
data class ChordPlacement(
    val phaseBasePairs: Int,
    val columns: Int,
    val fromDuplex: Int,
    val toDuplex: Int,
    val duplexOffset: Int,
    val effectiveAngleDegrees: Double,
    val transferRatioDuplexAxial: Double,
    val transferRatioCrossover: Double,
    val transferRatioDuplexInPlaneShear: Double,
    val effectiveAllowable: Double
)

/** The extreme placements of the edge-to-edge sweep, over every column phase. */
@Serializable
data class PlacementExtremes(
    val loadClass: String,
    val points: Int,
    val bestByAllowable: ChordPlacement,
    val worstByAllowable: ChordPlacement,
    val allowableSpan: Double,
    val worstByDuplexAxial: ChordPlacement,
    val worstByCrossover: ChordPlacement
)

/** The worst placement at each across-helix offset — the design rule as a table. */
@Serializable
data class OffsetSummary(
    val duplexOffset: Int,
    val angleDegrees: Double,
    val placements: Int,
    val worstTransferRatioDuplexAxial: Double,
    val worstTransferRatioCrossover: Double,
    val worstEffectiveAllowable: Double,
    val minimumTetherLengthAtDesiredStroke: Double
)

/** How the oblique overshoot depends on the one undetermined input. */
@Serializable
data class ObliqueOvershoot(
    val crossoverShearStiffness: Double,
    val alongHelixTransferRatio: Double,
    val obliqueTransferRatioDuplexAxial: Double,
    val obliqueTransferRatioCrossover: Double,
    val acrossHelixTransferRatioCrossover: Double
)

/** One layout — a crossover column phase and an attachment station within the unit cell. */
@Serializable
data class InPlaneLayoutPoint(
    val phaseBasePairs: Int,
    val columns: Int,
    val crossovers: Int,
    val stationIndex: Int,
    val stationAlong: Double,
    val stationDuplex: Int,
    val transferRatioDuplexAxial: Double,
    val transferRatioCrossover: Double,
    val transferRatioDuplexInPlaneShear: Double
)

/** The extreme layouts of one load class, over the complete phase × station sweep. */
@Serializable
data class InPlaneLayoutExtremes(
    val loadClass: String,
    val points: Int,
    val bestCrossover: InPlaneLayoutPoint,
    val worstCrossover: InPlaneLayoutPoint,
    val crossoverSpan: Double,
    val bestDuplexAxial: InPlaneLayoutPoint,
    val worstDuplexAxial: InPlaneLayoutPoint,
    val duplexAxialSpan: Double
)

/** The lattice against the continuum it discretises, at one station. */
@Serializable
data class ContinuumStation(
    val crossoverShearStiffness: Double,
    val x: Double,
    val duplex: Int,
    val lattice: Double,
    val continuum: Double,
    val difference: Double,
    val differenceOverApplied: Double,
    /**
     * `lattice / continuum`, or **exactly zero** where both are below 1 % of the applied
     * force. Two quantities that are both meant to be near zero compare their *noise* if
     * compared relatively — `CLAUDE.md`'s rule — so those stations are compared absolutely
     * through [difference] and carry no ratio at all.
     */
    val excess: Double
)

/** What the connector arm — the term classical shear lag drops — is worth. */
@Serializable
data class ConnectorArmEffect(
    val crossoverShearStiffness: Double,
    val loadedDuplexShareWithArm: Double,
    val loadedDuplexShareWithoutArm: Double,
    val ratio: Double,
    val peakCrossoverWithArm: Double,
    val peakCrossoverWithoutArm: Double,
    val rigidRotationEnergyWithArm: Double,
    val rigidRotationEnergyWithoutArm: Double
)

/** One minimum-tether-length design point, recomputed from the in-plane factor. */
@Serializable
data class TetherLengthPoint(
    val stroke: Double,
    val allowableName: String,
    val allowable: Double,
    val concentrationFactor: Double,
    val effectiveAllowable: Double,
    val minimumLengthApproximate: Double,
    val minimumLength: Double,
    val tetherTension: Double,
    val normalPreloadPerTether: Double,
    val normalPreloadTotal: Double,
    val normalPreloadFractionOfTarget: Double,
    val assemblyFootprint: Double,
    val previousMinimumLength: Double,
    val shrinkFactor: Double
)

/** The convergence record of one swept numerical parameter. */
@Serializable
data class InPlaneConvergencePoint(
    val parameter: String,
    val value: Double,
    val degreesOfFreedom: Int,
    val peakCrossover: Double,
    val peakDuplexAxial: Double,
    val regularisationForce: Double
)

/** The anchored load class: a distributed lateral drive reacted at discrete in-plane tethers. */
@Serializable
data class AnchoredSchemePoint(
    val anchors: Int,
    val anchorStiffness: Double,
    val driveDirection: String,
    val peakAnchorForce: Double,
    val forces: InPlanePathForces
)

@Serializable
data class InPlaneLoadPathResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: InPlaneParameters,
    val shearLagRegime: List<ShearLagRegime>,
    val nominalAlong: InPlanePathForces,
    val nominalAcross: InPlanePathForces,
    val edgeChordMap: List<ChordPlacement>,
    val placementExtremes: List<PlacementExtremes>,
    val offsetSummary: List<OffsetSummary>,
    val obliqueOvershoot: List<ObliqueOvershoot>,
    val layoutExtremes: List<InPlaneLayoutExtremes>,
    val crossoverStiffnessSweep: List<InPlanePathForces>,
    val continuumStations: List<ContinuumStation>,
    val connectorArm: List<ConnectorArmEffect>,
    val anchoredSchemes: List<AnchoredSchemePoint>,
    val tetherLengths: List<TetherLengthPoint>,
    val convergence: List<InPlaneConvergencePoint>,
    val verdict: Map<String, String>
)

// --------------------------------------------------------------------------- constants

private const val BEAM_COUNT = 15

private const val NOMINAL_PHASE_BASE_PAIRS = 8

private const val APPLIED_FORCE = 1.0

private val ANCHOR_STIFFNESSES = listOf(5.0, 55.0, 550.0)

/** `C-0014`'s tether counts. */
private const val TETHER_COUNT = 4

/**
 * The ceiling the oblique transfer ratio is asserted against.
 *
 * `T-15` declared "the lattice returning `η > 1` anywhere" as its primary falsifier, on an
 * equilibrium argument, and **the falsifier fired**: an obliquely pulled tether applies a
 * *moment* to the duplex it lands on, and the crossovers react that moment as an axial
 * couple, because they act on the interface line and not on the duplex axis. Equilibrium
 * bounds the **cut total**, not the per-duplex peak, and the argument was too strong.
 *
 * What survives is a *bracket*: the overshoot saturates at ~2.48 as the crossover stiffens
 * through four decades, so 3.0 is a ceiling the model must respect, and a value above it
 * would mean something other than the lever mechanism is being measured.
 */
private const val OBLIQUE_BOUND: Double = 3.0

private val sheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

private val nominalShear = Gen1Tile.crossoverInPlaneStiffness()

private fun membraneAtPhase(
    basePairs: Int,
    crossoverStiffness: Double = nominalShear,
    connectorArm: Double = sheet.interhelicalDistance / 2.0,
    subdivisions: Int = OrigamiMembrane.DEFAULT_SUBDIVISIONS,
    regularisation: Double = OrigamiMembrane.DEFAULT_REGULARISATION,
    supports: List<InPlanePointSupport> = emptyList(),
    extraStations: List<Double> = emptyList()
): OrigamiMembrane = OrigamiMembrane(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = BEAM_COUNT,
    columns = CrossoverLayout.atBasePairPhase(basePairs, sheet, Gen1Tile.EDGE_X),
    crossoverShearStiffness = crossoverStiffness,
    crossoverNormalStiffness = crossoverStiffness,
    subdivisions = subdivisions,
    connectorArm = connectorArm,
    regularisation = regularisation,
    supports = supports,
    extraStations = extraStations
)

// --------------------------------------------------------------------------- load cases

/** The along-helix tether chord on duplex [duplex]: in at one edge, out at the opposite one. */
private fun alongChord(lattice: OrigamiMembrane, duplex: Int): List<InPlanePointLoad> = listOf(
    InPlanePointLoad(-lattice.lengthX / 2.0, lattice.duplexY(duplex), -APPLIED_FORCE, 0.0),
    InPlanePointLoad(lattice.lengthX / 2.0, lattice.duplexY(duplex), APPLIED_FORCE, 0.0)
)

/** The across-helix tether chord at station [x]: in at one rim duplex, out at the other. */
private fun acrossChord(lattice: OrigamiMembrane, x: Double): List<InPlanePointLoad> = listOf(
    InPlanePointLoad(x, lattice.duplexY(0), 0.0, -APPLIED_FORCE),
    InPlanePointLoad(x, lattice.duplexY(lattice.beamCount - 1), 0.0, APPLIED_FORCE)
)

/**
 * The edge-to-edge tether chord: in at duplex [fromDuplex] on the `−x` edge and out at
 * duplex [toDuplex] on the `+x` edge, pulling **along the chord between them** so that the
 * force pair is collinear and therefore moment-free by construction.
 */
private fun edgeChord(
    lattice: OrigamiMembrane,
    fromDuplex: Int,
    toDuplex: Int
): List<InPlanePointLoad> {
    val dx = lattice.lengthX
    val dy = lattice.duplexY(toDuplex) - lattice.duplexY(fromDuplex)
    val span = hypot(dx, dy)
    val along = APPLIED_FORCE * dx / span
    val across = APPLIED_FORCE * dy / span
    return listOf(
        InPlanePointLoad(
            -lattice.lengthX / 2.0, lattice.duplexY(fromDuplex), -along, -across
        ),
        InPlanePointLoad(lattice.lengthX / 2.0, lattice.duplexY(toDuplex), along, across)
    )
}

/** The angle in degrees an edge chord makes with the helices. */
private fun chordAngle(lattice: OrigamiMembrane, fromDuplex: Int, toDuplex: Int): Double =
    atan2(
        lattice.duplexY(toDuplex) - lattice.duplexY(fromDuplex), lattice.lengthX
    ) * 180.0 / PI

// --------------------------------------------------------------------------- reporting

private val duplexesOnCut = sheet.duplexesOnCut(BEAM_COUNT * sheet.interhelicalDistance)

private val crossoversOnCut = sheet.crossoversOnCut(Gen1Tile.EDGE_X)

private fun pathForces(label: String, solution: MembraneDeflection): InPlanePathForces {
    val axial = solution.peakDuplexAxialForce
    val crossover = solution.peakCrossoverForce
    val shear = solution.peakDuplexInPlaneShear
    val ratioAxial = axial / APPLIED_FORCE
    val ratioCrossover = crossover / APPLIED_FORCE
    val ratioShear = shear / APPLIED_FORCE
    fun effective(crossoverAllowable: Double): Double = minOf(
        Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / ratioAxial,
        crossoverAllowable / ratioCrossover,
        Gen1Tile.OVERSTRETCHING_CEILING / ratioShear
    )
    val unzipEffective = effective(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE)
    val binding = when (unzipEffective) {
        Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / ratioAxial -> "duplex axial force at the attachment"
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / ratioCrossover -> "crossover in-plane force"
        else -> "duplex in-plane shear"
    }
    return InPlanePathForces(
        label = label,
        peakDuplexAxial = axial,
        peakCrossover = crossover,
        peakDuplexInPlaneShear = shear,
        transferRatioDuplexAxial = ratioAxial,
        transferRatioCrossover = ratioCrossover,
        transferRatioDuplexInPlaneShear = ratioShear,
        concentrationDuplexAxial = ratioAxial * duplexesOnCut,
        concentrationCrossover = ratioCrossover * crossoversOnCut,
        effectiveAllowableCrossoverAsUnzip = unzipEffective,
        effectiveAllowableCrossoverAsShear = effective(Gen1Tile.DUPLEX_SHEAR_ALLOWABLE),
        bindingPath = binding
    )
}

/**
 * The index of the extremum of [values], chosen on the **rounded** value with the index as
 * tie-break.
 *
 * Rounding at the serialisation boundary does not make a result file reproducible if it
 * contains an argmin: where a sweep is flat, two entries tie to the last unit in the last
 * place and the winner depends on summation order, so a re-run diffs in one integer while
 * every number is identical. `C-0015` was bitten by exactly this.
 */
private fun argExtremum(values: List<Double>, largest: Boolean): Int {
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

private fun elapsed(started: Long): String =
    "%.1f s".format((System.currentTimeMillis() - started) / 1000.0)

// --------------------------------------------------------------------------- study

fun main() {
    val started = System.currentTimeMillis()
    val nominal = membraneAtPhase(NOMINAL_PHASE_BASE_PAIRS)

    // ---------------------------------------------------------------- the cheap bound
    val regime = (Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.map { it * nominalShear } + 1e4).map { ks ->
        val neighbour = shearLagNeighbourLength(
            sheet.duplex.stretchModulus, sheet.crossoverSpacing, ks
        )
        val sharing = shearLagSharingLength(
            sheet.duplex.stretchModulus, sheet.crossoverSpacing, ks,
            sheet.interhelicalDistance, BEAM_COUNT
        )
        ShearLagRegime(
            crossoverShearStiffness = ks,
            transferLength = shearLagTransferLength(
                sheet.duplex.stretchModulus, sheet.crossoverSpacing, ks
            ),
            neighbourLength = neighbour,
            sharingLength = sharing,
            aspectRatio = shearLagAspectRatio(
                sheet.duplex.stretchModulus, sheet.crossoverSpacing, ks,
                sheet.interhelicalDistance
            ),
            crossoversWithinNeighbourLength = neighbour / sheet.crossoverSpacing,
            sharingLengthOverFootprint = sharing / Gen1Tile.EDGE_X,
            regime = if (sharing > Gen1Tile.EDGE_X) {
                "LONG SHEAR LAG — the tile is too small to share an in-plane point load"
            } else "short shear lag — the load becomes an equal share inside the footprint"
        )
    }

    // ---------------------------------------------------------------- the two directions
    val nominalAlong = pathForces(
        "along the helices, chord on the mid duplex, phase $NOMINAL_PHASE_BASE_PAIRS bp",
        nominal.solve(alongChord(nominal, BEAM_COUNT / 2))
    )
    val nominalAcross = pathForces(
        "across the helices, chord at the tile centre, phase $NOMINAL_PHASE_BASE_PAIRS bp",
        nominal.solve(acrossChord(nominal, 0.0))
    )

    // ------------------------------------------- the complete edge-to-edge placement sweep
    val placements = mutableListOf<ChordPlacement>()
    for (phase in 0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD) {
        val lattice = membraneAtPhase(phase)
        for (from in 0 until BEAM_COUNT) {
            for (to in 0 until BEAM_COUNT) {
                val solution = lattice.solve(edgeChord(lattice, from, to))
                val axial = solution.peakDuplexAxialForce / APPLIED_FORCE
                val crossover = solution.peakCrossoverForce / APPLIED_FORCE
                val shear = solution.peakDuplexInPlaneShear / APPLIED_FORCE
                placements += ChordPlacement(
                    phaseBasePairs = phase,
                    columns = lattice.crossoverColumns,
                    fromDuplex = from,
                    toDuplex = to,
                    duplexOffset = to - from,
                    effectiveAngleDegrees = chordAngle(lattice, from, to),
                    transferRatioDuplexAxial = axial,
                    transferRatioCrossover = crossover,
                    transferRatioDuplexInPlaneShear = shear,
                    effectiveAllowable = minOf(
                        Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / axial,
                        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / crossover,
                        Gen1Tile.OVERSTRETCHING_CEILING / shear
                    )
                )
            }
        }
    }
    println("edge-chord placement sweep done at ${elapsed(started)}")

    // ---------------------------------------------------------------- layout sweep
    val alongPoints = mutableListOf<InPlaneLayoutPoint>()
    val acrossPoints = mutableListOf<InPlaneLayoutPoint>()
    val stations = (0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD).map {
        it * Gen1Tile.RISE_PER_BASE_PAIR - sheet.crossoverSpacing / 2.0
    }
    for (phase in 0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD) {
        val lattice = membraneAtPhase(phase)
        for (duplex in 0 until BEAM_COUNT) {
            val solution = lattice.solve(alongChord(lattice, duplex))
            alongPoints += InPlaneLayoutPoint(
                phaseBasePairs = phase,
                columns = lattice.crossoverColumns,
                crossovers = lattice.crossovers.size,
                stationIndex = duplex,
                stationAlong = 0.0,
                stationDuplex = duplex,
                transferRatioDuplexAxial = solution.peakDuplexAxialForce / APPLIED_FORCE,
                transferRatioCrossover = solution.peakCrossoverForce / APPLIED_FORCE,
                transferRatioDuplexInPlaneShear = solution.peakDuplexInPlaneShear / APPLIED_FORCE
            )
        }
        stations.forEachIndexed { index, station ->
            val solution = lattice.solve(acrossChord(lattice, station))
            acrossPoints += InPlaneLayoutPoint(
                phaseBasePairs = phase,
                columns = lattice.crossoverColumns,
                crossovers = lattice.crossovers.size,
                stationIndex = index,
                stationAlong = station,
                stationDuplex = -1,
                transferRatioDuplexAxial = solution.peakDuplexAxialForce / APPLIED_FORCE,
                transferRatioCrossover = solution.peakCrossoverForce / APPLIED_FORCE,
                transferRatioDuplexInPlaneShear = solution.peakDuplexInPlaneShear / APPLIED_FORCE
            )
        }
    }
    println("layout sweep done at ${elapsed(started)}")

    fun extremes(name: String, points: List<InPlaneLayoutPoint>): InPlaneLayoutExtremes {
        val crossover = points.map { it.transferRatioCrossover }
        val axial = points.map { it.transferRatioDuplexAxial }
        val bestCrossover = argExtremum(crossover, largest = false)
        val worstCrossover = argExtremum(crossover, largest = true)
        val bestAxial = argExtremum(axial, largest = false)
        val worstAxial = argExtremum(axial, largest = true)
        return InPlaneLayoutExtremes(
            loadClass = name,
            points = points.size,
            bestCrossover = points[bestCrossover],
            worstCrossover = points[worstCrossover],
            crossoverSpan = crossover[worstCrossover] / crossover[bestCrossover],
            bestDuplexAxial = points[bestAxial],
            worstDuplexAxial = points[worstAxial],
            duplexAxialSpan = axial[worstAxial] / axial[bestAxial]
        )
    }

    val layoutExtremes = listOf(
        extremes("along the helices", alongPoints),
        extremes("across the helices", acrossPoints)
    )

    fun placementExtremes(name: String, points: List<ChordPlacement>): PlacementExtremes {
        val allowables = points.map { it.effectiveAllowable }
        val best = argExtremum(allowables, largest = true)
        val worst = argExtremum(allowables, largest = false)
        return PlacementExtremes(
            loadClass = name,
            points = points.size,
            bestByAllowable = points[best],
            worstByAllowable = points[worst],
            allowableSpan = allowables[best] / allowables[worst],
            worstByDuplexAxial =
                points[argExtremum(points.map { it.transferRatioDuplexAxial }, largest = true)],
            worstByCrossover =
                points[argExtremum(points.map { it.transferRatioCrossover }, largest = true)]
        )
    }

    val placementExtremes = listOf(
        placementExtremes(
            "edge-to-edge tether pair, all 15 x 15 duplex pairs x 32 phases", placements
        ),
        placementExtremes(
            "edge-to-edge tether pair ALIGNED with the helices (from = to)",
            placements.filter { it.duplexOffset == 0 }
        )
    )

    val offsetSummary = (0 until BEAM_COUNT).mapNotNull { offset ->
        val group = placements.filter { abs(it.duplexOffset) == offset }
        if (group.isEmpty()) null else {
            val worst = group[argExtremum(group.map { it.effectiveAllowable }, largest = false)]
            val ratio = sqrt(
                (1.0 + worst.effectiveAllowable / sheet.duplex.stretchModulus) *
                        (1.0 + worst.effectiveAllowable / sheet.duplex.stretchModulus) - 1.0
            )
            OffsetSummary(
                duplexOffset = offset,
                angleDegrees = atan2(
                    offset * sheet.interhelicalDistance, Gen1Tile.EDGE_X
                ) * 180.0 / PI,
                placements = group.size,
                worstTransferRatioDuplexAxial =
                    group.maxOf { it.transferRatioDuplexAxial },
                worstTransferRatioCrossover = group.maxOf { it.transferRatioCrossover },
                worstEffectiveAllowable = worst.effectiveAllowable,
                minimumTetherLengthAtDesiredStroke = Gen1Tile.DESIRED_STROKE / ratio
            )
        }
    }

    // the one undetermined input, against the one number that exceeds the equilibrium bound
    val worstPair = placementExtremes[0].worstByDuplexAxial
    val obliqueOvershoot = Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.map { multiplier ->
        val ks = multiplier * nominalShear
        val lattice = membraneAtPhase(worstPair.phaseBasePairs, crossoverStiffness = ks)
        val oblique = lattice.solve(
            edgeChord(lattice, worstPair.fromDuplex, worstPair.toDuplex)
        )
        ObliqueOvershoot(
            crossoverShearStiffness = ks,
            alongHelixTransferRatio = lattice.solve(edgeChord(lattice, BEAM_COUNT / 2, BEAM_COUNT / 2))
                .peakDuplexAxialForce / APPLIED_FORCE,
            obliqueTransferRatioDuplexAxial = oblique.peakDuplexAxialForce / APPLIED_FORCE,
            obliqueTransferRatioCrossover = oblique.peakCrossoverForce / APPLIED_FORCE,
            acrossHelixTransferRatioCrossover =
                lattice.solve(acrossChord(lattice, 0.0)).peakCrossoverForce / APPLIED_FORCE
        )
    }
    println("oblique overshoot sweep done at ${elapsed(started)}")

    // ---------------------------------------------------------------- crossover stiffness
    val stiffnessSweep = Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.flatMap { multiplier ->
        val ks = multiplier * nominalShear
        val lattice = membraneAtPhase(NOMINAL_PHASE_BASE_PAIRS, crossoverStiffness = ks)
        listOf(
            pathForces(
                "along, k_s = %.3f pN/nm (x %.5f)".format(ks, multiplier),
                lattice.solve(alongChord(lattice, BEAM_COUNT / 2))
            ),
            pathForces(
                "across, k_s = %.3f pN/nm (x %.5f)".format(ks, multiplier),
                lattice.solve(acrossChord(lattice, 0.0))
            )
        )
    }
    println("crossover stiffness sweep done at ${elapsed(started)}")

    // ---------------------------------------------------------------- continuum control
    val continuumStations = mutableListOf<ContinuumStation>()
    listOf(2.0, nominalShear, 32.0 * nominalShear).forEach { ks ->
        val lattice = membraneAtPhase(
            NOMINAL_PHASE_BASE_PAIRS, crossoverStiffness = ks, connectorArm = 0.0
        )
        val solution = lattice.solve(alongChord(lattice, BEAM_COUNT / 2))
        val continuum = ShearLagMembrane(
            stretchModulus = sheet.duplex.stretchModulus,
            interhelicalDistance = sheet.interhelicalDistance,
            crossoverSpacing = sheet.crossoverSpacing,
            crossoverShearStiffness = ks,
            lengthX = Gen1Tile.EDGE_X,
            duplexes = BEAM_COUNT,
            modes = 1200
        )
        val load = ChordLoad(
            -Gen1Tile.EDGE_X / 2.0, 0.0, Gen1Tile.EDGE_X / 2.0, 0.0, APPLIED_FORCE
        )
        listOf(0.0, 5.0, -8.0).forEach { x ->
            listOf(0, 4, BEAM_COUNT / 2).forEach { duplex ->
                val latticeForce = lattice.axialForceAt(solution, duplex, x)
                val continuumForce = continuum.duplexAxialForce(load, x, duplex)
                continuumStations += ContinuumStation(
                    crossoverShearStiffness = ks,
                    x = x,
                    duplex = duplex,
                    lattice = latticeForce,
                    continuum = continuumForce,
                    difference = latticeForce - continuumForce,
                    differenceOverApplied = (latticeForce - continuumForce) / APPLIED_FORCE,
                    excess = if (abs(continuumForce) > 0.01 * APPLIED_FORCE &&
                        abs(latticeForce) > 0.01 * APPLIED_FORCE
                    ) latticeForce / continuumForce else 0.0
                )
            }
        }
    }

    // ---------------------------------------------------------------- the connector arm
    val omega = 1e-3
    val armEffect = listOf(2.0, nominalShear, 32.0 * nominalShear).map { ks ->
        val withArm = membraneAtPhase(NOMINAL_PHASE_BASE_PAIRS, crossoverStiffness = ks)
        val withoutArm = membraneAtPhase(
            NOMINAL_PHASE_BASE_PAIRS, crossoverStiffness = ks, connectorArm = 0.0
        )
        val withSolution = withArm.solve(alongChord(withArm, BEAM_COUNT / 2))
        val withoutSolution = withoutArm.solve(alongChord(withoutArm, BEAM_COUNT / 2))
        fun rotationEnergy(lattice: OrigamiMembrane): Double = lattice.structuralEnergy(
            lattice.nodalField({ _, y -> -omega * y }, { x, _ -> omega * x }, { _, _ -> omega })
        )
        val share = withArm.axialForceAt(withSolution, BEAM_COUNT / 2, 0.0)
        val shareWithout = withoutArm.axialForceAt(withoutSolution, BEAM_COUNT / 2, 0.0)
        ConnectorArmEffect(
            crossoverShearStiffness = ks,
            loadedDuplexShareWithArm = share,
            loadedDuplexShareWithoutArm = shareWithout,
            ratio = share / shareWithout,
            peakCrossoverWithArm = withSolution.peakCrossoverForce,
            peakCrossoverWithoutArm = withoutSolution.peakCrossoverForce,
            rigidRotationEnergyWithArm = rotationEnergy(withArm),
            rigidRotationEnergyWithoutArm = rotationEnergy(withoutArm)
        )
    }
    println("continuum control done at ${elapsed(started)}")

    // ---------------------------------------------------------------- the anchored class
    val halfY = (BEAM_COUNT - 1) / 2.0 * sheet.interhelicalDistance
    val anchoredSchemes = ANCHOR_STIFFNESSES.flatMap { stiffness ->
        val corners = listOf(
            InPlanePointSupport(-Gen1Tile.EDGE_X / 2.0, -halfY, stiffness, stiffness),
            InPlanePointSupport(-Gen1Tile.EDGE_X / 2.0, halfY, stiffness, stiffness),
            InPlanePointSupport(Gen1Tile.EDGE_X / 2.0, -halfY, stiffness, stiffness),
            InPlanePointSupport(Gen1Tile.EDGE_X / 2.0, halfY, stiffness, stiffness)
        )
        val lattice = membraneAtPhase(NOMINAL_PHASE_BASE_PAIRS, supports = corners)
        listOf("along" to true, "across" to false).map { (name, along) ->
            val drive = (0 until BEAM_COUNT).flatMap { beam ->
                lattice.nodeX.map { x ->
                    val share = APPLIED_FORCE / (BEAM_COUNT * lattice.nodesPerBeam)
                    InPlanePointLoad(
                        x, lattice.duplexY(beam),
                        if (along) share else 0.0, if (along) 0.0 else share
                    )
                }
            }
            val solution = lattice.solve(drive)
            AnchoredSchemePoint(
                anchors = 4,
                anchorStiffness = stiffness,
                driveDirection = name,
                peakAnchorForce = (solution.supportForcesAlong.zip(solution.supportForcesAcross))
                    .maxOf { (a, b) -> hypot(a, b) },
                forces = pathForces(
                    "4 corner anchors at %.0f pN/nm, uniform lateral drive $name".format(
                        stiffness
                    ),
                    solution
                )
            )
        }
    }
    println("anchored schemes done at ${elapsed(started)}")

    // ---------------------------------------------------------------- tether lengths
    val modulus = sheet.duplex.stretchModulus
    fun tetherPoint(
        stroke: Double,
        allowableName: String,
        allowable: Double,
        factor: Double,
        previous: Double
    ): TetherLengthPoint {
        val effective = allowable / factor
        // exact: T = S (sqrt(L^2 + d^2) - L)/L = S (sqrt(1 + r^2) - 1) with r = stroke/L
        val ratio = sqrt((1.0 + effective / modulus) * (1.0 + effective / modulus) - 1.0)
        val length = stroke / ratio
        val normal = effective * ratio / sqrt(1.0 + ratio * ratio)
        return TetherLengthPoint(
            stroke = stroke,
            allowableName = allowableName,
            allowable = allowable,
            concentrationFactor = factor,
            effectiveAllowable = effective,
            minimumLengthApproximate = stroke * sqrt(modulus * factor / (2.0 * allowable)),
            minimumLength = length,
            tetherTension = effective,
            normalPreloadPerTether = normal,
            normalPreloadTotal = TETHER_COUNT * normal,
            normalPreloadFractionOfTarget =
                TETHER_COUNT * normal / Gen1Tile.TARGET_FORCE,
            assemblyFootprint = Gen1Tile.EDGE_X + 2.0 * length,
            previousMinimumLength = previous,
            shrinkFactor = previous / length
        )
    }

    // The concentration factor `L_min` needs is the one that makes the SHEAR allowable the
    // reference: n = A_shear / A_eff, so that L_min = delta sqrt(S n /(2 A_shear)) is
    // C-0014's own formula with n replaced. A_eff already takes the minimum over the three
    // path classes and their three different allowables.
    fun factorOf(effective: Double): Double = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE / effective

    val alongFactor = factorOf(nominalAlong.effectiveAllowableCrossoverAsUnzip)
    val alignedWorstFactor = factorOf(placementExtremes[1].worstByAllowable.effectiveAllowable)
    val placementWorstFactor = factorOf(placementExtremes[0].worstByAllowable.effectiveAllowable)
    val acrossFactor = factorOf(nominalAcross.effectiveAllowableCrossoverAsUnzip)

    val tetherLengths = listOf(
        Gen1Tile.ACCEPTABLE_STROKE to 28.0,
        Gen1Tile.DESIRED_STROKE to 93.3
    ).flatMap { (stroke, previous) ->
        listOf(
            tetherPoint(
                stroke, "C-0009's out-of-plane 7.6x, as C-0014 applied it",
                Gen1Tile.DUPLEX_SHEAR_ALLOWABLE, 7.6, previous
            ),
            tetherPoint(
                stroke, "in-plane, ALIGNED with the helices, nominal phase",
                Gen1Tile.DUPLEX_SHEAR_ALLOWABLE, alongFactor, previous
            ),
            tetherPoint(
                stroke, "in-plane, ALIGNED with the helices, worst of 480 placements",
                Gen1Tile.DUPLEX_SHEAR_ALLOWABLE, alignedWorstFactor, previous
            ),
            tetherPoint(
                stroke, "in-plane, across the helices",
                Gen1Tile.DUPLEX_SHEAR_ALLOWABLE, acrossFactor, previous
            ),
            tetherPoint(
                stroke, "in-plane, WORST of all 7200 placements (oblique)",
                Gen1Tile.DUPLEX_SHEAR_ALLOWABLE, placementWorstFactor, previous
            ),
            tetherPoint(
                stroke, "in-plane, aligned, but the joint in UNZIP geometry",
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, alongFactor, previous
            )
        )
    }

    // ---------------------------------------------------------------- convergence
    val convergence = mutableListOf<InPlaneConvergencePoint>()
    listOf(1, 2, 4, 8).forEach { subdivisions ->
        val lattice = membraneAtPhase(worstPair.phaseBasePairs, subdivisions = subdivisions)
        val solution = lattice.solve(
            edgeChord(lattice, worstPair.fromDuplex, worstPair.toDuplex)
        )
        convergence += InPlaneConvergencePoint(
            parameter = "subdivisions, worst oblique placement",
            value = subdivisions.toDouble(),
            degreesOfFreedom = lattice.degreesOfFreedom,
            peakCrossover = solution.peakCrossoverForce,
            peakDuplexAxial = solution.peakDuplexAxialForce,
            regularisationForce = solution.regularisationForceAlong
        )
    }
    listOf(1, 2, 4).forEach { subdivisions ->
        val lattice = membraneAtPhase(NOMINAL_PHASE_BASE_PAIRS, subdivisions = subdivisions)
        val solution = lattice.solve(alongChord(lattice, BEAM_COUNT / 2))
        convergence += InPlaneConvergencePoint(
            parameter = "subdivisions",
            value = subdivisions.toDouble(),
            degreesOfFreedom = lattice.degreesOfFreedom,
            peakCrossover = solution.peakCrossoverForce,
            peakDuplexAxial = solution.peakDuplexAxialForce,
            regularisationForce = solution.regularisationForceAlong
        )
    }
    listOf(1e-6, 1e-5, 1e-4, 1e-3, 1e-2).forEach { bed ->
        val lattice = membraneAtPhase(NOMINAL_PHASE_BASE_PAIRS, regularisation = bed)
        val solution = lattice.solve(alongChord(lattice, BEAM_COUNT / 2))
        convergence += InPlaneConvergencePoint(
            parameter = "regularisation",
            value = bed,
            degreesOfFreedom = lattice.degreesOfFreedom,
            peakCrossover = solution.peakCrossoverForce,
            peakDuplexAxial = solution.peakDuplexAxialForce,
            regularisationForce = solution.regularisationForceAlong
        )
    }
    println("convergence done at ${elapsed(started)}")

    // ---------------------------------------------------------------- verdict
    val sharing = regime.first { it.crossoverShearStiffness == nominalShear }
    val verdict = linkedMapOf(
        "deliverable" to (
                "The in-plane transfer ratio — the peak force in ONE load path over the force " +
                        "the tether applies, which is the quantity C-0014's L_min contains — is " +
                        "eta = %.4f for a tether ALIGNED with the helices, %.4f (crossover) " +
                        "across them, and at worst %.4f over all 7200 placements. The binding " +
                        "number is the effective allowable A_eff = min over path classes of " +
                        "(allowable / eta): %.1f pN aligned, %.1f pN across, %.1f pN at the " +
                        "worst placement, against C-0014's 48/7.6 = 6.3 pN."
                ).format(
                nominalAlong.transferRatioDuplexAxial,
                nominalAcross.transferRatioCrossover,
                placementExtremes[0].worstByDuplexAxial.transferRatioDuplexAxial,
                nominalAlong.effectiveAllowableCrossoverAsUnzip,
                nominalAcross.effectiveAllowableCrossoverAsUnzip,
                placementExtremes[0].worstByAllowable.effectiveAllowable
            ),
        "against C-0009" to (
                "C-0009's 2.3-7.6x is a peak over an EQUAL SHARE of a reaction the tile " +
                        "collected from its foundation over an l-sized patch, and C-0014 " +
                        "applied it as a peak over the APPLIED force. In plane the layer's " +
                        "lateral stiffness is exactly zero (C-0010), so nothing is collected: " +
                        "the tether's own tension is the whole load. Expressed in C-0009's own " +
                        "currency the in-plane concentration is %.2f on the duplex path and " +
                        "%.2f on the crossover path for an aligned pull — the duplex figure is " +
                        "large only because the equal share is 1/15 of a load that never gets " +
                        "shared."
                ).format(
                nominalAlong.concentrationDuplexAxial, nominalAlong.concentrationCrossover
            ),
        "the declared falsifier fired, and it is a result" to (
                "T-15 declared 'the lattice returning eta > 1 anywhere' as its primary " +
                        "falsifier, on the equilibrium argument that a tether collects nothing. " +
                        "It fires: an OBLIQUE tether reaches eta = %.4f on the duplex-axial " +
                        "path and %.4f on the crossover path. The argument was too strong. A " +
                        "tether that does not pull along a duplex applies a MOMENT to it, and " +
                        "the crossovers react that moment as an axial COUPLE, because they act " +
                        "on the interface line and not on the duplex axis. Equilibrium bounds " +
                        "the sum of the duplex axial forces on a cut — checked, to 1e-4 — and " +
                        "not the per-duplex peak. The overshoot is mesh-converged (%.5f to " +
                        "%.5f pN on the peak crossover over nested subdivisions 1 to 8) and " +
                        "SATURATES at %.3f as the crossover stiffens through four decades, so " +
                        "it is a bracket rather than an unbounded exposure."
                ).format(
                placementExtremes[0].worstByDuplexAxial.transferRatioDuplexAxial,
                placementExtremes[0].worstByCrossover.transferRatioCrossover,
                convergence.first { it.parameter.startsWith("subdivisions, worst") }.peakCrossover,
                convergence.last { it.parameter.startsWith("subdivisions, worst") }.peakCrossover,
                obliqueOvershoot.maxOf { it.obliqueTransferRatioDuplexAxial }
            ),
        "regime" to (
                "LONG SHEAR LAG. The across-strip sharing length is %.1f nm against a %.0f nm " +
                        "footprint (%.2fx), so an in-plane point load never becomes an equal " +
                        "share anywhere on a Gen-1 tile. That is why an aligned eta is 1 and " +
                        "not 1/15: the duplex the tether lands on keeps the whole tension."
                ).format(
                sharing.sharingLength, Gen1Tile.EDGE_X, sharing.sharingLengthOverFootprint
            ),
        "the design rule" to (
                "ALIGN THE TETHER WITH THE HELICES. Aligned, eta on the duplex-axial path is " +
                        "exactly 1.0000 at every one of the 480 (phase, duplex) placements and " +
                        "at every crossover stiffness in the four-decade sweep, the crossovers " +
                        "see only %.3f of the tension each, and A_eff is the full 48 pN " +
                        "single-duplex shear allowable. Misaligned, A_eff falls to %.1f pN — a " +
                        "factor of %.2f, bought or lost by the direction the tether runs in and " +
                        "nothing else. This is the in-plane restatement of C-0014's own finding " +
                        "that an anchor's orientation decides everything and its material " +
                        "almost nothing."
                ).format(
                nominalAlong.transferRatioCrossover,
                placementExtremes[0].worstByAllowable.effectiveAllowable,
                Gen1Tile.DUPLEX_SHEAR_ALLOWABLE /
                        placementExtremes[0].worstByAllowable.effectiveAllowable
            ),
        "C-0014 propagation" to (
                "Aligned with the helices the minimum tether length falls from 93.3 nm to " +
                        "%.1f nm at the desired 10 nm stroke (%.2fx) and from 28.0 nm to " +
                        "%.1f nm at the acceptable 3 nm stroke. The assembly around a 40 nm " +
                        "tile falls from ~227 nm to ~%.0f nm. But this is CONDITIONAL ON THE " +
                        "ALIGNMENT: at the worst of the 7200 placements — a rim-to-rim chord " +
                        "at 43 degrees — L_min is %.1f nm, i.e. WORSE than the 93.3 nm " +
                        "C-0014's conservative stand-in produced. The gain is bought by a " +
                        "design rule, not by the physics being kinder."
                ).format(
                tetherLengths[7].minimumLength,
                tetherLengths[7].shrinkFactor,
                tetherLengths[1].minimumLength,
                tetherLengths[7].assemblyFootprint,
                tetherLengths[10].minimumLength
            ),
        "the price of the shorter tether" to (
                "At the minimum length the tether's NORMAL preload is n A sqrt(2A/S) to " +
                        "leading order, which is INDEPENDENT OF THE STROKE: %.1f pN for four " +
                        "tethers, %.0f %% of the §3 100 pN target force, at BOTH strokes. " +
                        "C-0014's L_min formula does not contain it, and it is the reason the " +
                        "incompatibility does not simply disappear: it changes currency, from " +
                        "footprint to preload, and the preload is T-13's problem."
                ).format(
                tetherLengths[7].normalPreloadTotal,
                100.0 * tetherLengths[7].normalPreloadFractionOfTarget
            ),
        "layout" to (
                "Registration and column phase are worth x%.3f on the crossover path along the " +
                        "helices and x%.3f across them, against C-0015's x1.43-1.60 out of " +
                        "plane. On the binding path — the duplex axial force at the attachment " +
                        "— they are worth x%.4f: EXACTLY NOTHING. Layout cannot help here, " +
                        "because the attachment carries the tension whatever the crossovers do, " +
                        "and that is the structural difference between the in-plane and " +
                        "out-of-plane problems stated in one number."
                ).format(
                layoutExtremes[0].crossoverSpan, layoutExtremes[1].crossoverSpan,
                layoutExtremes[0].duplexAxialSpan
            ),
        "the continuum beside it" to (
                "Where the continuum's own premise holds — a transfer length well above the " +
                        "crossover spacing — the lattice and the shear-lag membrane agree to " +
                        "better than 2 %% of the applied force, which is what licenses " +
                        "attributing the disagreement at the Gen-1 crossover stiffness to " +
                        "discreteness. Two things the continuum cannot do: it converges only " +
                        "logarithmically at the load point, so it cannot produce a peak " +
                        "per-path force at all; and it is NOT FRAME-INDIFFERENT — classical " +
                        "shear lag drops dv/dx from the shear strain, which charges energy to " +
                        "a rigid rotation. That dropped term is worth up to x%.2f in the loaded " +
                        "duplex's share, and frame indifference fixes the connector arm at " +
                        "exactly d/2 rather than leaving it free."
                ).format(armEffect.maxOf { it.ratio }),
        "maturity" to "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "No in-plane force in a loaded origami sheet has ever been measured, and the " +
                "crossover's in-plane stiffness is not in the literature in any form."
    )

    val result = InPlaneLoadPathResult(
        task = "T-15",
        leaf = "A8.2",
        title = "The in-plane (membrane) load path into the Gen-1 tile, by shear lag",
        verificationType = "in-silico (in-plane beam-bar-connector grillage, with the " +
                "orthotropic shear-lag membrane it discretises run beside it) + logical " +
                "(an equilibrium bound that fixes the sign of the answer before any solve)",
        acceptance = "The in-plane force-concentration factor, per path class and per " +
                "direction, worst over anchor placement, judged against C-0006's per-path " +
                "allowables, and propagated into C-0014's minimum tether lengths",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm",
            "stiffness" to "pN/nm (= mN/m)",
            "stretchModulus" to "pN",
            "bendingRigidity" to "pN*nm^2",
            "membraneStiffness" to "pN/nm",
            "transferRatio" to "dimensionless, per pN of applied tether force",
            "temperature" to "K"
        ),
        conventions = listOf(
            "x along the helices, y across them, z normal to the electrode; the origin of " +
                    "(x, y) is the centre of the footprint",
            "u is the in-plane displacement along x and v the one along y, both positive in " +
                    "the positive axis direction; there is no w in this model, because for a " +
                    "flat sheet the membrane and bending problems decouple at linear order",
            "a duplex axial force is positive in tension",
            "a crossover's in-plane force is the vector the connector exerts on the lower-y " +
                    "duplex, and its MAGNITUDE is what an allowable is judged on",
            "the connector attaches at the interface line, d/2 from each duplex axis, so the " +
                    "sliding it resists is (u_upper + c*theta_upper) - (u_lower - c*theta_lower)",
            "a tether attaches to ONE duplex at a base-pair station, so the across-helix " +
                    "registration variable is the duplex index and not a continuous coordinate",
            "TRANSFER RATIO eta = peak force in one load path / applied tether force; " +
                    "CONCENTRATION FACTOR C = the same peak / the equal share over the paths " +
                    "available. C-0009 reports the second and C-0014 applied it as the first"
        ),
        validity = listOf(
            "TRL 1-3, model-consistent and traceable; nothing here is measured",
            "the crossover's in-plane stiffness k_s is DERIVED from Chen et al.'s softened-bond " +
                    "construction with the stretch modulus in place of the bending rigidity, " +
                    "and is not measured in any form; swept over four decades and the answer " +
                    "reported as a function of it",
            "linear elasticity and small displacements; the tether's own geometric stiffening " +
                    "(C-0014's cable term) is a separate, finite-displacement effect and is " +
                    "consumed here rather than re-derived",
            "no out-of-plane coupling: valid for a flat sheet at linear order, and the tile is " +
                    "NOT flat under load (C-0006 rejects the rigid-plate assumption), so a " +
                    "dished tile would couple the two problems at second order",
            "no in-plane foundation, on C-0010's exact symmetry zero; the regularising bed is " +
                    "shown to carry below 1e-9 of the applied force",
            "the crossover is a two-spring connector with no rotational restraint; a crossover " +
                    "that resisted relative in-plane rotation would stiffen the sheet and " +
                    "spread the load further, which lowers every force reported here",
            "single layer, static, 300 K, aqueous buffer with Mg2+",
            "rupture allowables are quasi-static extrapolations of loading-rate-dependent " +
                    "measurements; the 35-60 pN band of §4(f) is NOT used, being a " +
                    "whole-cross-section number (C-0006)"
        ),
        parameters = InPlaneParameters(
            temperature = 300.0,
            medium = "aqueous buffer with Mg2+",
            footprintAlong = Gen1Tile.EDGE_X,
            footprintAcross = nominal.lengthY,
            duplexes = BEAM_COUNT,
            crossoverColumns = nominal.crossoverColumns,
            crossovers = nominal.crossovers.size,
            interhelicalDistance = sheet.interhelicalDistance,
            interhelicalProvenance = "CITED, MEASURED — Fischer et al., Nano Lett. 16:4282 " +
                    "(2016), SAXS, single-layer sheet",
            crossoverSpacing = sheet.crossoverSpacing,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
            crossoverSpacingProvenance = "CITED — Rothemund, Nature 440:297 (2006); 32 bp per " +
                    "INTERFACE, not the 16 bp per helix",
            stretchModulus = sheet.duplex.stretchModulus,
            stretchModulusProvenance = "CITED, MEASURED — Wang et al., Biophys. J. 72:1335 (1997)",
            bendingRigidity = sheet.duplex.bendingRigidity,
            bendingRigidityProvenance = "CITED — CanDo (Kim et al., NAR 40:2862, 2012); a model " +
                    "input in that paper, not a measurement",
            crossoverInPlaneStiffness = nominalShear,
            crossoverInPlaneProvenance = "DERIVED from Chen et al., JACS 136:6995 (2014) SI, by " +
                    "substituting the stretch modulus for the bending rigidity in their own " +
                    "softened-bond construction k = 2 alpha X /(100 a). NOT MEASURED, and " +
                    "nothing in the accessible literature gives it in any form",
            crossoverInPlaneSweep = Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.map { it * nominalShear },
            connectorArm = sheet.interhelicalDistance / 2.0,
            connectorArmProvenance = "DERIVED, and not a free parameter: frame indifference " +
                    "fixes it at exactly d/2, because only then does a rigid in-plane rotation " +
                    "of the whole sheet cost no energy",
            regularisation = OrigamiMembrane.DEFAULT_REGULARISATION,
            subdivisions = OrigamiMembrane.DEFAULT_SUBDIVISIONS,
            degreesOfFreedom = nominal.degreesOfFreedom,
            appliedTetherForce = APPLIED_FORCE,
            unzipAllowable = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            shearAllowable = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
            overstretchingCeiling = Gen1Tile.OVERSTRETCHING_CEILING,
            allowableProvenance = "CITED, MEASURED — Essevaz-Roulet et al. PNAS 94:11935 " +
                    "(1997) unzip; Strunz et al. PNAS 96:11277 (1999) shear; van Mameren et al. " +
                    "PNAS 106:18231 (2009) nicked ceiling. C-0006's trace, unchanged"
        ),
        shearLagRegime = regime,
        nominalAlong = nominalAlong,
        nominalAcross = nominalAcross,
        edgeChordMap = placements.filter { it.phaseBasePairs == NOMINAL_PHASE_BASE_PAIRS },
        placementExtremes = placementExtremes,
        offsetSummary = offsetSummary,
        obliqueOvershoot = obliqueOvershoot,
        layoutExtremes = layoutExtremes,
        crossoverStiffnessSweep = stiffnessSweep,
        continuumStations = continuumStations,
        connectorArm = armEffect,
        anchoredSchemes = anchoredSchemes,
        tetherLengths = tetherLengths,
        convergence = convergence,
        verdict = verdict
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-15-in-plane-shear-lag.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n"
    )

    // ---------------------------------------------------------------- console
    println()
    println("--- the cheap bound: shear-lag regime ".padEnd(110, '-'))
    println(
        "%12s %10s %10s %10s %9s %10s".format(
            "k_s[pN/nm]", "Lambda", "Lambda_nn", "Lambda_1", "aspect", "L1/40nm"
        )
    )
    regime.forEach {
        println(
            "%12.3f %10.3f %10.3f %10.3f %9.3f %10.3f".format(
                it.crossoverShearStiffness, it.transferLength, it.neighbourLength,
                it.sharingLength, it.aspectRatio, it.sharingLengthOverFootprint
            )
        )
    }
    println()
    println("--- the two directions, per pN of applied tether force ".padEnd(110, '-'))
    println(
        "%42s %9s %9s %9s %9s".format(
            "case", "eta_ax", "eta_xo", "eta_sh", "A_eff"
        )
    )
    listOf(nominalAlong, nominalAcross).forEach {
        println(
            "%42s %9.4f %9.4f %9.4f %9.2f".format(
                it.label.take(42), it.transferRatioDuplexAxial, it.transferRatioCrossover,
                it.transferRatioDuplexInPlaneShear, it.effectiveAllowableCrossoverAsUnzip
            )
        )
    }
    println()
    println("--- the complete edge-to-edge placement sweep ".padEnd(110, '-'))
    placementExtremes.forEach {
        println("%58s  %5d points".format(it.loadClass.take(58), it.points))
        listOf(
            "best by A_eff" to it.bestByAllowable,
            "worst by A_eff" to it.worstByAllowable,
            "worst duplex axial" to it.worstByDuplexAxial,
            "worst crossover" to it.worstByCrossover
        ).forEach { (name, p) ->
            println(
                "    %20s  bp %2d, duplex %2d -> %2d (%6.2f deg)  eta_ax %7.4f  eta_xo %7.4f  A_eff %7.2f"
                    .format(
                        name, p.phaseBasePairs, p.fromDuplex, p.toDuplex,
                        p.effectiveAngleDegrees, p.transferRatioDuplexAxial,
                        p.transferRatioCrossover, p.effectiveAllowable
                    )
            )
        }
    }
    println()
    println("--- the worst placement at each across-helix offset ".padEnd(110, '-'))
    println("%8s %9s %8s %10s %10s %9s %9s".format(
        "offset", "angle", "points", "eta_ax", "eta_xo", "A_eff", "L_min(10)"
    ))
    offsetSummary.forEach {
        println("%8d %9.2f %8d %10.4f %10.4f %9.2f %9.1f".format(
            it.duplexOffset, it.angleDegrees, it.placements,
            it.worstTransferRatioDuplexAxial, it.worstTransferRatioCrossover,
            it.worstEffectiveAllowable, it.minimumTetherLengthAtDesiredStroke
        ))
    }
    println()
    println("--- the oblique overshoot against the one undetermined input ".padEnd(110, '-'))
    println("%12s %10s %10s %10s %10s".format("k_s", "eta along", "eta obl ax", "eta obl xo", "eta across"))
    obliqueOvershoot.forEach {
        println(
            "%12.3f %10.4f %10.4f %10.4f %10.4f".format(
                it.crossoverShearStiffness, it.alongHelixTransferRatio,
                it.obliqueTransferRatioDuplexAxial, it.obliqueTransferRatioCrossover,
                it.acrossHelixTransferRatioCrossover
            )
        )
    }
    println()
    println("--- layout extremes over the complete 32-phase sweep ".padEnd(110, '-'))
    layoutExtremes.forEach {
        println(
            "%20s  %4d points  crossover %.4f .. %.4f (x%.3f)  axial %.4f .. %.4f (x%.4f)"
                .format(
                    it.loadClass, it.points,
                    it.bestCrossover.transferRatioCrossover,
                    it.worstCrossover.transferRatioCrossover, it.crossoverSpan,
                    it.bestDuplexAxial.transferRatioDuplexAxial,
                    it.worstDuplexAxial.transferRatioDuplexAxial, it.duplexAxialSpan
                )
        )
    }
    println()
    println("--- C-0014's minimum tether lengths, recomputed ".padEnd(110, '-'))
    println(
        "%6s %52s %8s %9s %9s %9s".format(
            "stroke", "basis", "factor", "L_min", "shrink", "F_z[pN]"
        )
    )
    tetherLengths.forEach {
        println(
            "%6.1f %52s %8.3f %9.2f %9.2f %9.1f".format(
                it.stroke, it.allowableName.take(52), it.concentrationFactor,
                it.minimumLength, it.shrinkFactor, it.normalPreloadTotal
            )
        )
    }
    println()
    println("--- convergence ".padEnd(110, '-'))
    convergence.forEach {
        println(
            "%16s %10.2e  dofs %5d  peak crossover %10.7f  bed %10.2e".format(
                it.parameter, it.value, it.degreesOfFreedom, it.peakCrossover,
                it.regularisationForce
            )
        )
    }
    println()
    verdict.forEach { (key, value) -> println("$key: $value"); println() }
    println("written: ${output.path} in ${elapsed(started)}")

    // the falsifier this task declared first, wired in as a runtime check
    check(placements.filter { it.duplexOffset == 0 }.all {
        abs(it.transferRatioDuplexAxial - 1.0) < 1e-5 && it.transferRatioCrossover < 1.0
    }) {
        "a tether pulling ALONG the helices must put exactly its own tension into the duplex " +
                "it attaches to, at every phase and every duplex: there is nothing else for " +
                "the load to enter through and nothing inside the tile to share it with"
    }
    check(placements.all { it.transferRatioDuplexAxial < OBLIQUE_BOUND }) {
        "the oblique overshoot must stay inside the bracket the crossover-stiffness sweep " +
                "shows it saturating at; a larger value means the couple mechanism is not " +
                "what is being measured"
    }
    // the equilibrium statement that DOES hold: the duplex axial forces on a cut sum to the
    // net applied force crossing it, whatever the direction of pull
    listOf(BEAM_COUNT / 2 to BEAM_COUNT / 2, worstPair.fromDuplex to worstPair.toDuplex)
        .forEach { (from, to) ->
            val solution = nominal.solve(edgeChord(nominal, from, to))
            val cut = (0 until BEAM_COUNT).sumOf { nominal.axialForceAt(solution, it, 0.0) }
            val applied = edgeChord(nominal, from, to).filter { it.x > 0.0 }
                .sumOf { it.forceAlong }
            check(abs(cut - applied) < 1e-4) {
                "the duplex axial forces on a cut must sum to the applied force crossing it, " +
                        "which is the equilibrium statement the per-duplex peak does NOT obey"
            }
        }
    check(alongPoints.size == CrossoverLayout.BASE_PAIRS_PER_PERIOD * BEAM_COUNT) {
        "the along-helix layout sweep must be complete over the 32 bp phase period and " +
                "every duplex"
    }
    check(convergence.all { abs(it.regularisationForce) < 1e-9 }) {
        "the regularising bed must carry none of the load; there is no in-plane foundation"
    }
}
