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

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs

/**
 * Task `T-81` / leaf `A8.2` — **does a 16-crossover hinge line exist on a 40 nm tile?**
 *
 * ```shell
 * ./gradlew study -Pstudy=anchoring.HingeLineCensusStudyKt
 * ```
 *
 * Emits `gpd/results/T-81-hinge-line-census.json`, deterministically.
 */

// --------------------------------------------------------------------------- records

/** Every parameter the run consumed, logged so the result is reproducible from the file alone. */
@Serializable
data class T81Parameters(
    val temperature: Double,
    val medium: String,
    val footprintAlong: Double,
    val footprintAcross: Double,
    val duplexes: Int,
    val interhelicalDistance: Double,
    val risePerBasePair: Double,
    val crossoverSpacingBasePairs: Double,
    val perInterfacePitch: Double,
    val basePairsPerTurn: Double,
    val crossoverHingeStiffness: Double,
    val duplexBendingRigidity: Double,
    val loadPaths: Int,
    val mandateStiffness: Double,
    val acceptableStroke: Double,
    val desiredStroke: Double,
    val complianceCeiling: Double,
    val farAnchorageStiffness: Double,
    val assertedHingeCount: Int,
    val provenance: Map<String, String>
)

/** A bound that holds before any solve — the cheap arithmetic that decides the verdict. */
@Serializable
data class T81BoundRecord(
    val name: String,
    val value: Double,
    val statement: String
)

/** One phase of the complete 32 bp census of a hinge line. */
@Serializable
data class T81PhaseRecord(
    val phaseBasePairs: Int,
    val columns: Int,
    val evenInterfaces: Int,
    val oddInterfaces: Int,
    val largest: Int,
    val smallest: Int,
    val tileInventory: Int,
    val centroSymmetric: Boolean
)

/** One reading of *"one hinge line"*, with the line length it implies and the count it gets. */
@Serializable
data class T81TopologyRecord(
    val id: String,
    val name: String,
    val axis: String,
    val lineLength: Double,
    val largestCount: Int,
    val smallestCount: Int,
    val reachesSixteen: Boolean,
    val statement: String
)

/** A fan of several hinge lines in series — the only way sixteen crossovers can be assembled. */
@Serializable
data class T81FanRecord(
    val interfaces: Int,
    val perInterface: Int,
    val totalCrossovers: Int,
    val lever: Double,
    val effectiveHingeCount: Double,
    val effectiveOverTotal: Double,
    val flexureStiffness: Double,
    val assembledStiffness: Double,
    val fractionOfMandate: Double,
    val latticeOverContinuum: Double,
    val lineLengthDemanded: Double
)

/** `C-0034`'s placement pipeline re-run at one hinge count. */
@Serializable
data class T81DesignRecord(
    val hingeCount: Int,
    val arm: Double,
    val armBasePairs: Double,
    val restraint: Double,
    val endConditionFactor: Double,
    val tangentAtAcceptable: Double,
    val tangentAtDesired: Double,
    val rotationAtAcceptable: Double,
    val hingeShareOfCompliance: Double,
    val bondForceAtAcceptable: Double,
    val reachesDesiredByRotation: Boolean,
    val insideCeilingAtAcceptable: Boolean,
    val insideCeilingAtDesired: Boolean,
    val verdict: String
)

/** Supply against demand, on the two currencies a hinge consumes: crossovers and interface line. */
@Serializable
data class T81DemandRecord(
    val hingeCount: Int,
    val paths: Int,
    val crossoverDemand: Int,
    val tileInventory: Int,
    val demandOverInventory: Double,
    val lineLengthPerFlexure: Double,
    val lineLengthDemand: Double,
    val lineLengthAvailable: Double,
    val demandOverAvailable: Double
)

/** A premise moved, and the count that results. */
@Serializable
data class T81SensitivityRecord(
    val name: String,
    val pitch: Double,
    val largestCount: Int,
    val reachesSixteen: Boolean,
    val statement: String
)

/** An upstream number recomputed here rather than tabulated. */
@Serializable
data class T81ReproductionRecord(
    val name: String,
    val source: String,
    val expected: Double,
    val obtained: Double,
    val departure: Double
)

/** A convergence probe. */
@Serializable
data class T81ConvergenceRecord(
    val parameter: String,
    val value: Double,
    val quantity: Double,
    val statement: String
)

/** The whole result. */
@Serializable
data class T81Result(
    val task: String,
    val leaf: String,
    val question: String,
    val parameters: T81Parameters,
    val bounds: List<T81BoundRecord>,
    val census: List<T81PhaseRecord>,
    val topologies: List<T81TopologyRecord>,
    val fans: List<T81FanRecord>,
    val designs: List<T81DesignRecord>,
    val demands: List<T81DemandRecord>,
    val sensitivities: List<T81SensitivityRecord>,
    val reproductions: List<T81ReproductionRecord>,
    val convergence: List<T81ConvergenceRecord>,
    val verdict: Map<String, String>
)

// --------------------------------------------------------------------------- the run

private const val T81_MANDATE = 100.0 / 3.0

private const val T81_PATHS = 45

private const val T81_CEILING = 40.0

/** `C-0034`'s adopted `A2` anchorage: the arm's own duplex end, two strand termini on a chord. */
private const val T81_FAR_STIFFNESS = 78.2352941176

private const val T81_DUPLEXES = 15

private fun t81Element(hingeCount: Int, arm: Double): RotatingHingeArm {
    val factor = guidedArmFactor(
        armRestraintParameter(T81_FAR_STIFFNESS, arm, Gen1Tile.DUPLEX_BENDING_RIGIDITY)
    )
    return RotatingHingeArm(
        Gen1Tile.crossoverHingeStiffness(), arm, Gen1Tile.DUPLEX_BENDING_RIGIDITY,
        hingeCount, factor
    )
}

private fun t81Arm(hingeCount: Int): Double = anchoredArmForStiffness(
    hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
    hingeCount = hingeCount,
    farStiffness = T81_FAR_STIFFNESS,
    bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    count = T81_PATHS,
    targetStiffness = T81_MANDATE,
    workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
)

fun main() {
    val started = System.nanoTime()

    val pitch = perInterfacePitch()
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY
    val d = Gen1Tile.INTERHELICAL_SHEET
    val tile = Gen1Tile.EDGE_X
    val sheet = origamiSheet(d, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

    // ---------------------------------------------------------------- the cheap bounds

    val lineForSixteen = hingeLineLengthForCount(16, pitch)
    val duplexesForSixteen = duplexesForTransverseCount(16)
    val census = hingeLineCensus(tile)
    val richest = census.maxOf { tileCrossoverInventory(T81_DUPLEXES, it.evenInterfaces, it.oddInterfaces) }

    val bounds = listOf(
        T81BoundRecord(
            "collinear interface a 16-crossover hinge line demands", lineForSixteen,
            "(16 - 1) x 32 bp = 163.2 nm, against a 40 nm tile: 4.08 tiles, and the pitch is a " +
                    "measured lattice constant no model can move"
        ),
        T81BoundRecord(
            "that length as a multiple of the tile", lineForSixteen / tile,
            "the first of the three bounds, and on its own it settles the longitudinal reading"
        ),
        T81BoundRecord(
            "duplexes a 16-crossover TRANSVERSE line demands", duplexesForSixteen.toDouble(),
            "a transverse line serves one parity only, so it needs 2n+1 = 33 duplexes = " +
                    "${"%.2f".format(duplexesForSixteen * d)} nm, 2.22 tiles - and its crossovers " +
                    "restrain the wrong axis anyway"
        ),
        T81BoundRecord(
            "that width as a multiple of the tile", duplexesForSixteen * d / Gen1Tile.EDGE_Y,
            "the answer is bounded in BOTH directions of the sheet before any code runs"
        ),
        T81BoundRecord(
            "crossovers 45 paths at 16 demand", (T81_PATHS * 16).toDouble(),
            "against C-0015's whole-tile inventory of 49-56"
        ),
        T81BoundRecord(
            "that demand over the richest tile inventory", T81_PATHS * 16.0 / richest,
            "12.86x every crossover the tile has; even ONE flexure at 16 takes 28.6 % of them"
        )
    )

    // ---------------------------------------------------------------- the census

    val phases = census.map {
        T81PhaseRecord(
            phaseBasePairs = it.phaseBasePairs,
            columns = it.columns,
            evenInterfaces = it.evenInterfaces,
            oddInterfaces = it.oddInterfaces,
            largest = it.largest,
            smallest = it.smallest,
            tileInventory = tileCrossoverInventory(T81_DUPLEXES, it.evenInterfaces, it.oddInterfaces),
            centroSymmetric = isCentroSymmetric(it.columns, T81_DUPLEXES)
        )
    }

    // ---------------------------------------------------------------- the topology ladder

    val armAtSixteen = t81Arm(16)
    val topologyLengths = listOf(
        Triple("L1", "the flexure's own plan share at 45 paths (3 columns along x)", tile / 3.0),
        Triple("L2", "the adopted arm's own length, C-0034's E5a16", armAtSixteen),
        Triple("L3", "one full-length interface of the tile", tile),
        Triple("L4", "the tile dilated by the arm - the longest line any flexure attached to it can reach",
            tile + 2.0 * armAtSixteen),
        Triple(
            "L5", "an unbounded superstructure: whatever 16 demands",
            lineForSixteen + 2.0 * CrossoverLayout.EDGE_MARGIN
        )
    )
    val topologies = topologyLengths.map { (id, name, length) ->
        val local = hingeLineCensus(length)
        val largest = local.maxOf { it.largest }
        T81TopologyRecord(
            id = id,
            name = name,
            axis = "along the helices - the only axis a crossover's k_theta restrains",
            lineLength = length,
            largestCount = largest,
            smallestCount = local.minOf { it.smallest },
            reachesSixteen = largest >= 16,
            statement = when (id) {
                "L1" -> "what 45 independent flexures can each own, and it is one or two"
                "L2" -> "the arm is barely longer than one pitch, so its own interface holds one"
                "L3" -> "one flexure owning a whole tile edge - the most the TILE can give a hinge"
                "L4" -> "the geometric ceiling on this footprint, and it is six"
                else -> "priced and refused: 45 lines of 163.2 nm at a 2.69 nm pitch is " +
                        "${"%.0f".format(T81_PATHS * lineForSixteen * d)} nm2, " +
                        "${"%.1f".format(T81_PATHS * lineForSixteen * d / (tile * Gen1Tile.EDGE_Y))}x " +
                        "the tile footprint - T-96"
            }
        )
    }
    val transverse = T81TopologyRecord(
        id = "L6",
        name = "a transverse fold line across the tile - TASKS.md's own guess",
        axis = "across the helices - and a crossover does NOT restrain this axis",
        lineLength = T81_DUPLEXES * d,
        largestCount = transverseHingeCount(T81_DUPLEXES, 0),
        smallestCount = transverseHingeCount(T81_DUPLEXES, 1),
        reachesSixteen = false,
        statement = "15 duplexes give 14 interfaces and a transverse line serves 7 of them; " +
                "and each of those 7 is a dihedral spring about a line running the OTHER way, " +
                "so this topology does not supply n k_theta at all"
    )

    // ---------------------------------------------------------------- the fan

    val fans = listOf(3, 4, 8, 9).flatMap { perInterface ->
        (1..8).map { interfaces ->
            val lever = fanLever(interfaces, d)
            val effective = fanEffectiveHingeCount(interfaces, perInterface)
            val stiffness = fanFlexureStiffness(interfaces, perInterface, hinge, d)
            T81FanRecord(
                interfaces = interfaces,
                perInterface = perInterface,
                totalCrossovers = interfaces * perInterface,
                lever = lever,
                effectiveHingeCount = effective,
                effectiveOverTotal = effective / (interfaces * perInterface),
                flexureStiffness = stiffness,
                assembledStiffness = T81_PATHS * stiffness,
                fractionOfMandate = T81_PATHS * stiffness / T81_MANDATE,
                latticeOverContinuum = fanOverContinuum(interfaces),
                lineLengthDemanded = hingeLineLengthForCount(perInterface, pitch)
            )
        }
    }

    // ---------------------------------------------------------------- the design ladder

    val designs = (listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 16, 32)).map { n ->
        val arm = t81Arm(n)
        val element = t81Element(n, arm)
        val restraint = armRestraintParameter(T81_FAR_STIFFNESS, arm, ei)
        val factor = guidedArmFactor(restraint)
        val tangentAcceptable = T81_PATHS * element.tangentStiffness(Gen1Tile.ACCEPTABLE_STROKE)
        val tangentDesired = T81_PATHS * element.tangentStiffness(Gen1Tile.DESIRED_STROKE)
        val reaches = arm > Gen1Tile.DESIRED_STROKE
        val hingeTerm = n * hinge / (arm * arm)
        val armTerm = factor * ei / (arm * arm * arm)
        T81DesignRecord(
            hingeCount = n,
            arm = arm,
            armBasePairs = arm / Gen1Tile.RISE_PER_BASE_PAIR,
            restraint = restraint,
            endConditionFactor = factor,
            tangentAtAcceptable = tangentAcceptable,
            tangentAtDesired = tangentDesired,
            rotationAtAcceptable = Math.toDegrees(
                element.rotationForForce(abs(element.reaction(Gen1Tile.ACCEPTABLE_STROKE)))
            ),
            hingeShareOfCompliance = (1.0 / hingeTerm) / (1.0 / hingeTerm + 1.0 / armTerm),
            bondForceAtAcceptable = element.hingeBondForce(Gen1Tile.ACCEPTABLE_STROKE, 2.0),
            reachesDesiredByRotation = reaches,
            insideCeilingAtAcceptable = tangentAcceptable <= T81_CEILING,
            insideCeilingAtDesired = tangentDesired <= T81_CEILING,
            verdict = when {
                tangentAcceptable > T81_CEILING ->
                    "FAILS C-0023's compliance ceiling at the ACCEPTABLE stroke"
                !reaches -> "clears the acceptable stroke; CANNOT reach the desired one by rotation"
                tangentDesired > T81_CEILING -> "reaches the desired stroke, past the ceiling there"
                else -> "PASS - reaches the desired stroke inside the ceiling"
            }
        )
    }

    val ceilingThreshold = designs.first { it.insideCeilingAtAcceptable }.hingeCount
    val strokeThreshold = designs.first { it.reachesDesiredByRotation }.hingeCount
    val fullPassThreshold = designs.first {
        it.reachesDesiredByRotation && it.insideCeilingAtAcceptable && it.insideCeilingAtDesired
    }.hingeCount
    val available = topologies.first { it.id == "L3" }.largestCount
    val reachCeiling = topologies.first { it.id == "L4" }.largestCount
    val perFlexure = topologies.first { it.id == "L1" }.largestCount

    // ---------------------------------------------------------------- supply and demand

    val interiorLine = (T81_DUPLEXES - 1) * tile
    val outboardLine = 2.0 * tile
    val demands = listOf(1, 2, 4, 6, 8, 16).map { n ->
        val perLine = hingeLineLengthForCount(n, pitch)
        T81DemandRecord(
            hingeCount = n,
            paths = T81_PATHS,
            crossoverDemand = T81_PATHS * n,
            tileInventory = richest,
            demandOverInventory = T81_PATHS * n.toDouble() / richest,
            lineLengthPerFlexure = perLine,
            lineLengthDemand = T81_PATHS * perLine,
            lineLengthAvailable = interiorLine + outboardLine,
            demandOverAvailable = if (perLine == 0.0) 0.0
            else T81_PATHS * perLine / (interiorLine + outboardLine)
        )
    }

    // ---------------------------------------------------------------- sensitivities

    val sensitivities = listOf(
        T81SensitivityRecord(
            "the per-helix mis-reading: 16 bp per interface", pitch / 2.0,
            maximumHingeCount(tile, pitch / 2.0), maximumHingeCount(tile, pitch / 2.0) >= 16,
            "CLAUDE.md's own trap, taken deliberately: even doubling the crossover density " +
                    "reaches 8, half of what is asserted"
        ),
        T81SensitivityRecord(
            "honeycomb lattice, 21 bp per interface",
            perInterfacePitch(Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP),
            maximumHingeCount(tile, perInterfacePitch(Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP)),
            maximumHingeCount(tile, perInterfacePitch(Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP)) >= 16,
            "not the Gen-1 sheet, and it does not help either: 6"
        ),
        T81SensitivityRecord(
            "both together - honeycomb at the per-helix mis-reading",
            perInterfacePitch(Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP) / 2.0,
            maximumHingeCount(tile, perInterfacePitch(Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP) / 2.0),
            maximumHingeCount(tile, perInterfacePitch(Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP) / 2.0) >= 16,
            "the most optimistic lattice reading in circulation, and it still misses"
        ),
        T81SensitivityRecord(
            "the footprint a 16-crossover line would need", pitch,
            16, true,
            "163.2 nm along the helices, or 88.8 nm across: the tile would have to be " +
                    "4.08x longer or 2.22x wider than sec 3 specifies"
        )
    )

    // ---------------------------------------------------------------- reproductions

    fun reproduction(
        name: String, source: String, expected: Double, obtained: Double
    ): T81ReproductionRecord = T81ReproductionRecord(
        name, source, expected, obtained,
        if (expected == 0.0) abs(obtained) else abs(obtained - expected) / abs(expected)
    )

    val layoutAgreement = census.maxOf { record ->
        val layout = CrossoverLayout.atBasePairPhase(record.phaseBasePairs, sheet, tile)
        maxOf(
            abs(record.columns - layout.size).toDouble(),
            abs(record.evenInterfaces - layout.countOfParity(0)).toDouble(),
            abs(record.oddInterfaces - layout.countOfParity(1)).toDouble()
        )
    }
    val guidedSixteen = rotatingArmForStiffness(
        hinge, ei, T81_PATHS, T81_MANDATE, Gen1Tile.ACCEPTABLE_STROKE, 16, 12.0
    )
    val guidedEight = rotatingArmForStiffness(
        hinge, ei, T81_PATHS, T81_MANDATE, Gen1Tile.ACCEPTABLE_STROKE, 8, 12.0
    )
    val adopted = designs.first { it.hingeCount == 16 }
    val reproductions = listOf(
        reproduction(
            "C-0015: tile inventory at the eight-column phases", "C-0015", 56.0,
            phases.filter { it.columns == 8 }.map { it.tileInventory }.distinct().single().toDouble()
        ),
        reproduction(
            "C-0015: tile inventory at the seven-column phases", "C-0015", 49.0,
            phases.filter { it.columns == 7 }.map { it.tileInventory }.distinct().single().toDouble()
        ),
        reproduction(
            "C-0015: phases giving eight columns", "C-0015", 10.0,
            phases.count { it.columns == 8 }.toDouble()
        ),
        reproduction(
            "C-0015: centro-symmetric phases", "C-0015", 10.0,
            phases.count { it.centroSymmetric }.toDouble()
        ),
        reproduction(
            "C-0015: this census against CrossoverLayout itself, worst departure over 32 phases",
            "C-0015", 0.0, layoutAgreement
        ),
        reproduction(
            "C-0009: across-helix rigidity per unit width", "C-0009",
            sheet.acrossHelixRigidity, hinge * d / pitch
        ),
        reproduction("C-0029: E5g8 arm", "C-0029", 10.3056070, guidedEight),
        reproduction("C-0029: E5g16 arm", "C-0029", 12.2423721, guidedSixteen),
        reproduction(
            "C-0029: the cantilever hinge-arm ceiling", "C-0029", 9.76624511,
            hingeArmCeiling(3.0, T81_PATHS, ei, T81_MANDATE)
        ),
        reproduction("C-0034: E5a16 arm", "C-0034", 11.028, adopted.arm),
        reproduction("C-0034: E5a16 tangent at 3 nm", "C-0034", 33.56, adopted.tangentAtAcceptable),
        reproduction("C-0034: E5a16 tangent at 10 nm", "C-0034", 36.78, adopted.tangentAtDesired),
        reproduction(
            "C-0034: the 8-crossover arm that fails P4", "C-0034", 9.52,
            designs.first { it.hingeCount == 8 }.arm
        ),
        reproduction(
            "C-0034: E5a16 realised end-condition factor", "C-0034", 7.356, adopted.endConditionFactor
        ),
        reproduction(
            "literature: 32 bp is three turns of the square lattice", "Rothemund 2006 / Douglas 2009",
            3.0, 32.0 / 10.67
        ),
        reproduction(
            "literature: the per-interface pitch in nm", "Rothemund 2006", 10.88, pitch
        )
    )

    // ---------------------------------------------------------------- convergence

    val coarse = (0 until 32).map { crossoversInLine(tile, it * Gen1Tile.RISE_PER_BASE_PAIR, pitch) }
    val convergence = listOf(1, 10, 100).map { refinement ->
        val fine = (0 until 32 * refinement).map {
            crossoversInLine(tile, it * Gen1Tile.RISE_PER_BASE_PAIR / refinement, pitch)
        }
        T81ConvergenceRecord(
            "phase grid refinement", refinement.toDouble(), fine.max().toDouble(),
            "the base-pair sweep is COMPLETE: refining the phase ${refinement}x adds no count " +
                    "the 32 base-pair phases do not already contain (${fine.distinct().sorted()} " +
                    "against ${coarse.distinct().sorted()})"
        )
    } + listOf(1, 4, 16, 64, 256).map { m ->
        T81ConvergenceRecord(
            "fan interfaces", m.toDouble(), fanOverContinuum(m),
            "the lattice fan over the continuum strip it approximates, converging to 1 " +
                    "from above as 1 + 3/(2m)"
        )
    } + designs.map { design ->
        val element = t81Element(design.hingeCount, design.arm)
        T81ConvergenceRecord(
            "placed secant over the mandate", design.hingeCount.toDouble(),
            T81_PATHS * element.secantStiffness(Gen1Tile.ACCEPTABLE_STROKE) / T81_MANDATE,
            "the re-priced arm reproduces its own target secant at every hinge count"
        )
    }

    // ---------------------------------------------------------------- the verdict

    val fanSixteen = fans.first { it.interfaces == 4 && it.perInterface == 4 }
    val verdict = mapOf(
        "P1 the pitch and the demand" to
                "PASS. Crossovers serve one INTERFACE every 32 bp = 10.88 nm, so a hinge line of " +
                "n crossovers demands (n-1) x 10.88 nm of collinear interface. Sixteen demands " +
                "163.2 nm.",
        "P2 does a 16-crossover hinge line exist" to
                "FAIL, at every one of the 32 phases and in BOTH directions of the sheet. The " +
                "largest hinge line a 40 nm tile carries is FOUR, at every phase; the other " +
                "parity carries three at the 22 seven-column phases and four at the 10 " +
                "eight-column ones. Sixteen needs 163.2 nm of collinear interface (4.08 tiles) " +
                "longitudinally, or 33 duplexes = 88.8 nm (2.22 tiles) transversally - and the " +
                "transverse line restrains the wrong axis in any case. The absolute geometric " +
                "ceiling, a line spanning the tile dilated by its own arm, is SIX.",
        "the best and worst phases" to
                "The 10 centro-symmetric eight-column phases - ${
                    phases.filter { it.columns == 8 }.map { it.phaseBasePairs }
                } bp - carry FOUR on BOTH parities. The other 22 carry four on one parity and " +
                "three on the other, so a hinge line placed on the wrong parity there loses 25 %. " +
                "The phases that maximise the hinge line are exactly C-0015's centro-symmetric " +
                "ones, and nothing in either construction forced that.",
        "P3 upstream" to
                "PASS. The census reproduces C-0015 exactly: 56 crossovers at 10 phases, 49 at " +
                "22, 10 centro-symmetric phases, and it agrees with CrossoverLayout itself at " +
                "every phase with departure ${layoutAgreement}.",
        "P4 what the design does at the count that exists" to
                "FAIL. C-0034's pipeline needs $strokeThreshold crossovers to place an arm long " +
                "enough to lift 10 nm by rotation, $fullPassThreshold for a design that also " +
                "holds C-0023's ceiling at that stroke, and $ceilingThreshold to hold the 40 " +
                "pN/nm ceiling at the acceptable stroke. The lattice supplies $available on a " +
                "full-length tile interface, $reachCeiling at the absolute geometric ceiling, " +
                "and $perFlexure per flexure at 45 paths. At four crossovers the arm places at " +
                "${"%.3f".format(designs.first { it.hingeCount == 4 }.arm)} nm - sec 3's " +
                "acceptable 3 nm stroke clears with a tangent of " +
                "${"%.2f".format(designs.first { it.hingeCount == 4 }.tangentAtAcceptable)} pN/nm " +
                "inside the ceiling, and the desired 10 nm stroke is out of geometric reach. At " +
                "the one or two crossovers 45 independent flexures can each own, the tangent is " +
                "${"%.2f".format(designs.first { it.hingeCount == 2 }.tangentAtAcceptable)}-" +
                "${"%.2f".format(designs.first { it.hingeCount == 1 }.tangentAtAcceptable)} pN/nm, " +
                "past C-0023's own ceiling, so even the ACCEPTABLE stroke fails.",
        "P5 the conflict with C-0015's 45 attachments" to
                "FAIL, and it is arithmetic. The whole tile carries 49-56 crossovers; 45 paths at " +
                "16 demand 720, which is ${"%.2f".format(T81_PATHS * 16.0 / richest)}x every " +
                "crossover the sheet has. Even at ONE crossover per flexure the demand is 45 " +
                "against 49-56 - 80-92 % of the inventory - and every one of those crossovers is " +
                "already a structural load path in C-0009's and C-0015's grillage. The 3 x 15 " +
                "grid and a 16-crossover hinge cannot be built on the same sheet.",
        "P6 the continuum control" to
                "PASS, and it settles what kind of fact this is. A continuum hinge line of the " +
                "same length carries ${"%.3f".format(tile / pitch)} crossovers of density, so the " +
                "lattice quantisation is worth -18 % to +9 % - while the assertion is out by " +
                "4.35x. The verdict is a CONTINUUM fact about crossover density, not a " +
                "quantisation artefact. The fan's own discreteness is likewise bounded: " +
                "6.0x at one interface, 1.47x at four, 1 + 3/(2m) asymptotically.",
        "where sixteen crossovers CAN be found, and what they are worth" to
                "Sixteen crossovers can be ASSEMBLED into one flexure - four interfaces of four " +
                "is exactly sixteen - but interfaces compose in SERIES, not in parallel, because " +
                "each carries only the moment of what is outboard of it and turns through its " +
                "own angle. n_eff = n_i x 3(2m-1)/(m(2m+1)), so sixteen crossovers in that " +
                "arrangement are worth ${"%.3f".format(fanSixteen.effectiveHingeCount)} of hinge " +
                "- ${"%.1f".format(100.0 * fanSixteen.effectiveOverTotal)} % of their own count - " +
                "on a lever of ${"%.3f".format(fanSixteen.lever)} nm, and 45 of them assemble to " +
                "${"%.2f".format(fanSixteen.assembledStiffness)} pN/nm, " +
                "${"%.2f".format(1.0 / fanSixteen.fractionOfMandate)}x too soft for the mandate.",
        "maturity" to
                "TRL 1-3. A COUNT on a lattice whose pitch is cited and measured. Nothing here " +
                "is measured, no sheet has been built, and no routing here is a sequence design. " +
                "The rigid-raft census is an UPPER bound on participation: C-0009's own result " +
                "is that a point load is carried by its two nearest crossovers and essentially " +
                "nothing else, so the realised count runs lower, never higher."
    )

    // ---------------------------------------------------------------- emit

    val result = T81Result(
        task = "T-81",
        leaf = "A8.2",
        question = "Does a 16-crossover hinge line exist on a 40 nm tile?",
        parameters = T81Parameters(
            temperature = 300.0,
            medium = "aqueous buffer, 2 mM MgCl2",
            footprintAlong = tile,
            footprintAcross = Gen1Tile.EDGE_Y,
            duplexes = T81_DUPLEXES,
            interhelicalDistance = d,
            risePerBasePair = Gen1Tile.RISE_PER_BASE_PAIR,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
            perInterfacePitch = pitch,
            basePairsPerTurn = BForm.BASE_PAIRS_PER_TURN_SQUARE,
            crossoverHingeStiffness = hinge,
            duplexBendingRigidity = ei,
            loadPaths = T81_PATHS,
            mandateStiffness = T81_MANDATE,
            acceptableStroke = Gen1Tile.ACCEPTABLE_STROKE,
            desiredStroke = Gen1Tile.DESIRED_STROKE,
            complianceCeiling = T81_CEILING,
            farAnchorageStiffness = T81_FAR_STIFFNESS,
            assertedHingeCount = 16,
            provenance = mapOf(
                "crossover spacing 32 bp per interface" to
                        "CITED, Rothemund, Nature 440:297 (2006), via C-0015",
                "rise per base pair 0.34 nm" to "CITED, Douglas et al., Nature 459:414 (2009)",
                "interhelical distance 2.69 nm" to
                        "CITED, MEASURED by SAXS, Fischer et al., Nano Lett. 16:4282 (2016)",
                "base pairs per turn 10.67" to "CITED, square lattice",
                "crossover hinge stiffness" to
                        "CITED, FITTED, Chen et al., JACS 136:6995 (2014) SI, via C-0009",
                "duplex bending rigidity 230 pN nm2" to
                        "CITED, a CanDo MODEL INPUT (Kim et al., NAR 40:2862, 2012), not a measurement",
                "far anchorage 78.235 pN nm/rad" to
                        "C-0034's A2, from C-0029's counting theorem at the phosphate radius",
                "45 load paths on a 3 x 15 grid" to "C-0015",
                "100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM" to "sec 3 of the problem definition",
                "40 pN/nm compliance ceiling" to "C-0023"
            )
        ),
        bounds = bounds,
        census = phases,
        topologies = topologies + transverse,
        fans = fans,
        designs = designs,
        demands = demands,
        sensitivities = sensitivities,
        reproductions = reproductions,
        convergence = convergence,
        verdict = verdict
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-81-hinge-line-census.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY
        ).withEmissionHeader(LatticeTag.SQUARE, null)) + "\n"
    )

    // ---------------------------------------------------------------- console

    println("=== T-81 — does a 16-crossover hinge line exist on a 40 nm tile? ".padEnd(110, '='))
    println()
    println("--- the cheap bounds, which ran first ".padEnd(110, '-'))
    bounds.forEach { println("%54s %12.4f   %s".format(it.name.take(54), it.value, it.statement.take(38))) }
    println()
    println("--- the complete 32 bp census of a hinge line on the 40 nm tile ".padEnd(110, '-'))
    println("%6s %8s %8s %8s %8s %8s %10s %8s".format(
        "bp", "columns", "even", "odd", "best", "worst", "inventory", "centro"
    ))
    phases.forEach {
        println("%6d %8d %8d %8d %8d %8d %10d %8s".format(
            it.phaseBasePairs, it.columns, it.evenInterfaces, it.oddInterfaces,
            it.largest, it.smallest, it.tileInventory, it.centroSymmetric
        ))
    }
    println()
    println("--- the topology ladder ".padEnd(110, '-'))
    println("%4s %62s %9s %6s %6s".format("id", "reading", "line [nm]", "best", "worst"))
    (topologies + transverse).forEach {
        println("%4s %62s %9.3f %6d %6d".format(
            it.id, it.name.take(62), it.lineLength, it.largestCount, it.smallestCount
        ))
    }
    println()
    println("--- the fan: sixteen crossovers assembled, and what they are worth ".padEnd(110, '-'))
    println("%4s %4s %7s %8s %9s %9s %10s %9s".format(
        "m", "n_i", "total", "lever", "n_eff", "n_eff/n", "assembled", "of mandate"
    ))
    fans.forEach {
        println("%4d %4d %7d %8.3f %9.4f %9.4f %10.3f %9.4f".format(
            it.interfaces, it.perInterface, it.totalCrossovers, it.lever,
            it.effectiveHingeCount, it.effectiveOverTotal, it.assembledStiffness, it.fractionOfMandate
        ))
    }
    println()
    println("--- C-0034's design re-priced at every hinge count ".padEnd(110, '-'))
    println("%4s %8s %8s %7s %9s %9s %8s %s".format(
        "n", "arm", "bp", "c", "tan(3)", "tan(10)", "F/bond", "verdict"
    ))
    designs.forEach {
        println("%4d %8.4f %8.1f %7.3f %9.3f %9.3f %8.3f %s".format(
            it.hingeCount, it.arm, it.armBasePairs, it.endConditionFactor,
            it.tangentAtAcceptable, it.tangentAtDesired, it.bondForceAtAcceptable,
            it.verdict.take(46)
        ))
    }
    println()
    println("--- supply against demand ".padEnd(110, '-'))
    println("%4s %10s %10s %10s %12s %12s %10s".format(
        "n", "demand", "inventory", "ratio", "line/flexure", "line demand", "over avail"
    ))
    demands.forEach {
        println("%4d %10d %10d %10.3f %12.3f %12.1f %10.3f".format(
            it.hingeCount, it.crossoverDemand, it.tileInventory, it.demandOverInventory,
            it.lineLengthPerFlexure, it.lineLengthDemand, it.demandOverAvailable
        ))
    }
    println()
    println("--- sensitivities ".padEnd(110, '-'))
    sensitivities.forEach {
        println("%54s pitch %7.3f -> %3d".format(it.name.take(54), it.pitch, it.largestCount))
    }
    println()
    println("--- upstream reproductions ".padEnd(110, '-'))
    reproductions.forEach {
        println("%66s %12.6f %12.6f %10.2e".format(
            it.name.take(66), it.expected, it.obtained, it.departure
        ))
    }
    println()
    verdict.forEach { (key, value) -> println("$key: $value"); println() }
    println("written: ${output.path} in %.1f s".format((System.nanoTime() - started) / 1e9))

    // ------------------------------------------------- the falsifiers, as runtime checks
    check(census.all { it.largest < 16 }) {
        "if a hinge line on a 40 nm tile reached sixteen crossovers this whole task would be a " +
                "footnote, and the declared falsifier 2 would have fired"
    }
    check(layoutAgreement == 0.0) {
        "this census must count the SAME lattice as C-0015's CrossoverLayout at every one of " +
                "the 32 phases, or nothing here is a check against C-0015"
    }
    check(abs(adopted.arm - 11.028) / 11.028 < 1e-3) {
        "the design being re-priced must be C-0034's filed E5a16, or the re-pricing is of " +
                "something else"
    }
    check(abs(fanOverContinuum(1) - 6.0) < 1e-12 && designs.all { it.arm > 0.0 }) {
        "the continuum control must be exactly 6 at a single interface, and every placed arm " +
                "must be a positive length"
    }
    check(convergence.filter { it.parameter == "placed secant over the mandate" }
        .all { abs(it.quantity - 1.0) < 1e-7 }) {
        "every re-priced arm must reproduce the mandate secant it was solved for"
    }
    check(designs.zipWithNext().all { (a, b) -> b.arm > a.arm }) {
        "the placed arm must be strictly increasing in the hinge count, or neither threshold " +
                "is well posed"
    }
}
