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
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.openrndr.math.Vector3
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Task `T-71` / leaf `A8.2` — **a backbone-torsion check of `C-0029`'s closed routing**, at all
 * three scales the programme now depends on: `C-0029`'s single junction, `C-0042`'s pair and
 * `C-0052`'s trio on a lone crossbar.
 *
 * ```shell
 * tools/study.sh anchoring.BackboneTorsionStudyKt
 * ```
 *
 * Emits `gpd/results/T-71-backbone-torsion-closure.json`, deterministically: fixed grids, strict
 * comparisons, no timestamp, and the whole tree rounded at the **serialisation boundary**.
 */

private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val INTERHELICAL = Gen1Tile.INTERHELICAL_SHEET

/** The grid the closure solve runs on everywhere but the convergence sweep. */
private const val GRID = 180
private const val REFINEMENTS = 6

// ---------------------------------------------------------------------------------------------

@Serializable
data class T71BoundRecord(
    val quantity: String,
    val value: Double,
    val units: String,
    val note: String
)

@Serializable
data class T71LinkRecord(
    val scale: String,
    val junction: String,
    val link: String,
    val reading: String,
    val phosphateGap: Double,
    val o3ToPhosphorus: Double,
    val o3ToC5: Double,
    val reachFeasible: Boolean,
    val pinnedFeasible: Boolean,
    val o3pBond: Double,
    val o3pBondZ: Double,
    val angleC3O3P: Double,
    val angleO3PO5: Double,
    val anglePO5C5: Double,
    val alpha: Double,
    val beta: Double,
    val gamma: Double,
    val delta: Double,
    val epsilon: Double,
    val zeta: Double,
    val chi: Double,
    val donorDelta: Double,
    val donorTemplate: String,
    val acceptorTemplate: String,
    val donorPolarity: Int,
    val acceptorPolarity: Int,
    val worstCovalentZ: Double,
    val minimumStrainZ: Double,
    val minimumOccupancy: Double,
    val leastPopulatedTorsion: String,
    val conformer: String,
    val conformerDistance: Double,
    val torsionsPopulated: Boolean,
    val covalentAcceptable: Boolean,
    val closes: Boolean
)

@Serializable
data class T71ScaleRecord(
    val scale: String,
    val claim: String,
    val junctions: Int,
    val links: Int,
    val worstPhosphateGap: Double,
    val reachFeasibleLinks: Int,
    val pinnedFeasibleLinks: Int,
    val bestWorstCovalentZ: Double,
    val worstWorstCovalentZ: Double,
    val worstMinimumStrainZ: Double,
    val covalentAcceptableLinks: Int,
    val populatedLinks: Int,
    val rarestOccupancy: Double,
    val closingLinks: Int,
    val closes: Boolean,
    val verdict: String
)

/**
 * The torsion-aware census of `C-0029`'s **whole** placement space, not only its argmin.
 *
 * `C-0029`'s search minimises a phosphate-distance residual and nothing else, so whether its
 * optimum is torsion-feasible is an accident of that objective. The honest question is whether
 * ANY placement in the same space closes, and the closed-form reach bound is cheap enough to ask
 * it of every one.
 */
@Serializable
data class T71CensusRecord(
    val topology: String,
    val placements: Int,
    val covalentPlacements: Int,
    val reachFeasiblePlacements: Int,
    val solved: Int,
    val closingPlacements: Int,
    val bestWorstCovalentZ: Double,
    val bestMinimumOccupancy: Double,
    val bestAzimuthDegrees: Double,
    val bestCentreX: Double,
    val bestCentreY: Double,
    val bestChordAzimuthDegrees: Double,
    val bestWorstGap: Double,
    val verdict: String
)

@Serializable
data class T71SensitivityRecord(
    val axis: String,
    val label: String,
    val worstCovalentZ: Double,
    val worstMinimumStrainZ: Double,
    val reachFeasibleLinks: Int,
    val covalentAcceptableLinks: Int,
    val populatedLinks: Int,
    val links: Int,
    val closes: Boolean,
    val verdictMoves: Boolean
)

@Serializable
data class T71ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T71ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class T71LiteratureRecord(
    val question: String,
    val answer: String,
    val flag: String,
    val source: String
)

@Serializable
data class T71Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val bounds: List<T71BoundRecord>,
    val baseline: List<T71LinkRecord>,
    val links: List<T71LinkRecord>,
    val scales: List<T71ScaleRecord>,
    val census: List<T71CensusRecord>,
    val sensitivities: List<T71SensitivityRecord>,
    val convergence: List<T71ConvergenceRecord>,
    val reproductions: List<T71ReproductionRecord>,
    val literature: List<T71LiteratureRecord>,
    val findings: Map<String, String>
)

// ------------------------------------------------------------------ the geometry of one junction

/**
 * One covalent link of a 90° junction, as two [ResidueAnchor]s — one on the seat duplex and one on
 * the standing duplex's terminal base pair.
 */
private data class JunctionLink(
    val junction: String,
    val link: String,
    val seat: ResidueAnchor,
    val standoff: ResidueAnchor,
    val phosphateGap: Double
)

/** A sheet phosphate's anchor: the sheet duplex runs along `x` at `y = centreY`, `z = 0`. */
private fun sheetAnchor(
    backbone: DuplexBackbone,
    centreY: Double,
    strand: Int,
    index: Int,
    name: String
): ResidueAnchor {
    val p = backbone.sheetPhosphate(centreY, strand, index)
    return ResidueAnchor(name, p, Vector3(p.x, centreY, 0.0), Vector3.UNIT_X)
}

/** A standoff terminus' anchor: the standoff's axis runs along [axisDirection] from its face. */
private fun standoffAnchor(
    terminus: Vector3,
    axisPoint: Vector3,
    axisDirection: Vector3,
    name: String
): ResidueAnchor = ResidueAnchor(name, terminus, axisPoint, axisDirection)

private fun linksOf(
    backbone: DuplexBackbone,
    closure: JunctionClosure,
    junction: String
): List<JunctionLink> {
    val axisPoint = Vector3(closure.centreX, closure.centreY, closure.faceHeight)
    val first = backbone.standoffTerminus(
        closure.centreX, closure.centreY, closure.faceHeight, closure.azimuth, 0
    )
    val second = backbone.standoffTerminus(
        closure.centreX, closure.centreY, closure.faceHeight, closure.azimuth, 1
    )
    return listOf(
        JunctionLink(
            junction, "link 1",
            sheetAnchor(
                backbone, closure.firstDuplex * INTERHELICAL, closure.firstStrand,
                closure.firstIndex, "sheet"
            ),
            standoffAnchor(first, axisPoint, Vector3.UNIT_Z, "standoff"),
            closure.firstGap
        ),
        JunctionLink(
            junction, "link 2",
            sheetAnchor(
                backbone, closure.secondDuplex * INTERHELICAL, closure.secondStrand,
                closure.secondIndex, "sheet"
            ),
            standoffAnchor(second, axisPoint, Vector3.UNIT_Z, "standoff"),
            closure.secondGap
        )
    )
}

private fun linksOf(
    backbone: DuplexBackbone,
    placement: StandoffPlacement,
    junction: String
): List<JunctionLink> {
    val axisPoint = Vector3(placement.centreX, placement.centreY, placement.faceHeight)
    return listOf(
        JunctionLink(
            junction, "link 1",
            sheetAnchor(
                backbone, placement.firstTarget.duplex * INTERHELICAL, placement.firstTarget.strand,
                placement.firstTarget.index, "sheet"
            ),
            standoffAnchor(placement.firstTerminus, axisPoint, Vector3.UNIT_Z, "standoff"),
            placement.firstGap
        ),
        JunctionLink(
            junction, "link 2",
            sheetAnchor(
                backbone, placement.secondTarget.duplex * INTERHELICAL,
                placement.secondTarget.strand, placement.secondTarget.index, "sheet"
            ),
            standoffAnchor(placement.secondTerminus, axisPoint, Vector3.UNIT_Z, "standoff"),
            placement.secondGap
        )
    )
}

/** A crossbar phosphate: the crossbar runs along `x` through the origin (`C-0052`'s frame). */
private fun crossbarAnchor(
    backbone: DuplexBackbone,
    target: CrossbarTarget,
    crossbarBasePairs: Int,
    helicalPhase: Double,
    axialPhase: Double
): ResidueAnchor {
    val groove = backbone.minorGrooveAngle * PI / 180.0
    val angle = helicalPhase + target.index * backbone.twistPerBasePair + target.strand * groove
    val x = axialPhase + (target.index - 0.5 * (crossbarBasePairs - 1)) * backbone.risePerBasePair
    val p = Vector3(
        x, backbone.phosphateRadius * cos(angle), backbone.phosphateRadius * sin(angle)
    )
    return ResidueAnchor("crossbar", p, Vector3(x, 0.0, 0.0), Vector3.UNIT_X)
}

private fun linksOf(
    backbone: DuplexBackbone,
    trio: CrossbarTrioClosure,
    placement: TrioPlacement
): List<JunctionLink> {
    val direction = when (placement.kind) {
        TrioJunctionKind.LEG -> Vector3(0.0, 0.0, -1.0)
        TrioJunctionKind.FLEXURE -> Vector3(0.0, -1.0, 0.0)
    }
    val axisPoint = when (placement.kind) {
        TrioJunctionKind.LEG -> Vector3(
            placement.axialOffset, placement.lateralOffset, placement.firstTerminus.z
        )

        TrioJunctionKind.FLEXURE -> Vector3(
            placement.axialOffset, placement.firstTerminus.y, 0.0
        )
    }
    return listOf(
        JunctionLink(
            placement.name, "link 1",
            crossbarAnchor(
                backbone, placement.firstTarget, trio.crossbarBasePairs, trio.helicalPhase,
                trio.axialPhase
            ),
            standoffAnchor(placement.firstTerminus, axisPoint, direction, "junction"),
            placement.firstGap
        ),
        JunctionLink(
            placement.name, "link 2",
            crossbarAnchor(
                backbone, placement.secondTarget, trio.crossbarBasePairs, trio.helicalPhase,
                trio.axialPhase
            ),
            standoffAnchor(placement.secondTerminus, axisPoint, direction, "junction"),
            placement.secondGap
        )
    )
}

// ---------------------------------------------------------------------------- solving one link

private fun solve(
    scale: String,
    link: JunctionLink,
    reading: PhosphateReading,
    templates: List<NucleotideTemplate> = NucleotideTemplate.ALL,
    gridSteps: Int = GRID
): T71LinkRecord {
    val closure = bestLinkClosure(link.seat, link.standoff, reading, templates, gridSteps, REFINEMENTS)
    // The cheap bound, over EVERY assignment the design may choose — not only the one the solve
    // happened to rank first, which would make an exclusion an artefact of the ranking.
    val bound = bestLinkReach(link.seat, link.standoff, templates)
    return T71LinkRecord(
        scale = scale,
        junction = link.junction,
        link = link.link,
        reading = reading.name,
        phosphateGap = link.phosphateGap,
        o3ToPhosphorus = bound.o3ToP,
        o3ToC5 = bound.o3ToC5,
        reachFeasible = bound.freeFeasible,
        pinnedFeasible = bound.pinnedFeasible,
        o3pBond = closure.o3pBond,
        o3pBondZ = (closure.o3pBond - PhosphodiesterGeometry.O3_P_BOND) /
                PhosphodiesterGeometry.O3_P_BOND_SD,
        angleC3O3P = closure.angleC3O3P,
        angleO3PO5 = closure.angleO3PO5,
        anglePO5C5 = closure.anglePO5C5,
        alpha = closure.torsions.alpha,
        beta = closure.torsions.beta,
        gamma = closure.torsions.gamma,
        delta = closure.torsions.delta,
        epsilon = closure.torsions.epsilon,
        zeta = closure.torsions.zeta,
        chi = closure.torsions.chi,
        donorDelta = closure.donorDelta,
        donorTemplate = closure.donorTemplate,
        acceptorTemplate = closure.acceptorTemplate,
        donorPolarity = closure.donorPolarity,
        acceptorPolarity = closure.acceptorPolarity,
        worstCovalentZ = closure.worstCovalentZ,
        minimumStrainZ = closure.minimumStrainZ,
        minimumOccupancy = closure.minimumOccupancy,
        leastPopulatedTorsion = closure.leastPopulatedTorsion,
        conformer = closure.conformer,
        conformerDistance = closure.conformerDistance,
        torsionsPopulated = closure.torsionsPopulated,
        covalentAcceptable = closure.covalentAcceptable,
        closes = closure.closes
    )
}

private fun scaleRecord(
    scale: String,
    claim: String,
    junctions: Int,
    records: List<T71LinkRecord>
): T71ScaleRecord {
    val closing = records.count { it.closes }
    return T71ScaleRecord(
        scale = scale,
        claim = claim,
        junctions = junctions,
        links = records.size,
        worstPhosphateGap = records.maxOf { it.phosphateGap },
        reachFeasibleLinks = records.count { it.reachFeasible },
        pinnedFeasibleLinks = records.count { it.pinnedFeasible },
        bestWorstCovalentZ = records.minOf { it.worstCovalentZ },
        worstWorstCovalentZ = records.maxOf { it.worstCovalentZ },
        worstMinimumStrainZ = records.maxOf { it.minimumStrainZ },
        covalentAcceptableLinks = records.count { it.covalentAcceptable },
        populatedLinks = records.count { it.torsionsPopulated },
        rarestOccupancy = records.minOf { it.minimumOccupancy },
        closingLinks = closing,
        closes = closing == records.size,
        verdict = if (closing == records.size) "CLOSES" else
            "DOES NOT CLOSE — $closing of ${records.size} links"
    )
}

// -------------------------------------------------------------------------- the baseline

/** Two consecutive residues of an ideal duplex of the given helical parameters. */
private fun duplexStep(
    twistDegrees: Double,
    rise: Double,
    radius: Double
): Pair<ResidueAnchor, ResidueAnchor> {
    val twist = twistDegrees * PI / 180.0
    fun anchor(index: Int, name: String): ResidueAnchor {
        val angle = index * twist
        return ResidueAnchor(
            name,
            Vector3(radius * cos(angle), radius * sin(angle), index * rise),
            Vector3(0.0, 0.0, index * rise),
            Vector3.UNIT_Z
        )
    }
    return Pair(anchor(0, "residue i"), anchor(1, "residue i+1"))
}

private fun baselineRecord(
    scale: String,
    junction: String,
    twistDegrees: Double,
    rise: Double,
    radius: Double,
    reading: PhosphateReading,
    templates: List<NucleotideTemplate> = listOf(NucleotideTemplate.B_SOUTH),
    gridSteps: Int = GRID
): T71LinkRecord {
    val (first, second) = duplexStep(twistDegrees, rise, radius)
    val link = JunctionLink(
        junction, "i to i+1", first, second, (second.phosphate - first.phosphate).length
    )
    return solve(scale, link, reading, templates, gridSteps)
}

// ---------------------------------------------------------------------------------------------


// ------------------------------------------------------------------ the torsion-aware census

/**
 * Enumerates `C-0029`'s placement grid — azimuth, axial position and lateral seat — and asks of
 * every placement, in order of cost: does it close on phosphate distance at all; does it pass the
 * closed-form reach bound on both links; and, for the most promising ones, does it close at torsion
 * level.
 *
 * The reach filter is what makes this affordable: it is three atom placements and a distance, and
 * a link outside the interval closes at **no** torsion, so nothing feasible is discarded by it.
 */
private fun census(
    backbone: DuplexBackbone,
    topology: RoutingTopology,
    azimuthSteps: Int = 120,
    axialSteps: Int = 64,
    lateralSteps: Int = 9,
    solveCap: Int = 100,
    gridSteps: Int = 60,
    refinements: Int = 4
): T71CensusRecord {
    val axialPeriod = backbone.helicalRepeatBasePairs * backbone.risePerBasePair
    var placements = 0
    var covalent = 0
    val feasible = ArrayList<Pair<Double, JunctionClosure>>()
    for (a in 0 until azimuthSteps) {
        val azimuth = a * 2.0 * PI / azimuthSteps
        for (b in 0 until axialSteps) {
            val axial = b * axialPeriod / axialSteps
            for (c in 0 until lateralSteps) {
                val lateral = c * 0.5 * INTERHELICAL / (lateralSteps - 1)
                placements++
                val closure = singlePlacement(backbone, topology, axial, lateral, azimuth)
                    ?: continue
                if (!closure.covalent) continue
                covalent++
                val links = linksOf(backbone, closure, "census")
                var violation = 0.0
                var admissible = true
                links.forEach { link ->
                    val bound = bestLinkReach(link.seat, link.standoff)
                    if (!bound.freeFeasible) admissible = false
                    violation += maxOf(
                        0.0,
                        PhosphodiesterGeometry.reachMinimumTolerant - bound.o3ToC5,
                        bound.o3ToC5 - PhosphodiesterGeometry.reachMaximumTolerant
                    )
                }
                if (admissible) feasible += violation to closure
            }
        }
    }
    // Deterministic order: by reach margin, then by the placement's own coordinates.
    val ranked = feasible.sortedWith(
        compareBy({ it.first }, { it.second.azimuth }, { it.second.centreX }, { it.second.centreY })
    ).take(solveCap)
    var closing = 0
    var bestZ = Double.MAX_VALUE
    var bestOccupancy = 0.0
    var best: JunctionClosure? = null
    ranked.forEach { (_, closure) ->
        val solved = linksOf(backbone, closure, "census").map { link ->
            bestLinkClosure(
                link.seat, link.standoff, PhosphateReading.FREE, NucleotideTemplate.ALL,
                gridSteps, refinements
            )
        }
        val worst = solved.maxOf { it.worstCovalentZ }
        val occupancy = solved.minOf { it.minimumOccupancy }
        if (solved.all { it.closes }) {
            closing++
            if (best == null || occupancy > bestOccupancy) {
                bestOccupancy = occupancy
                bestZ = worst
                best = closure
            }
        } else if (best == null && (worst < bestZ ||
                    (worst == bestZ && occupancy > bestOccupancy))
        ) {
            bestZ = worst
            bestOccupancy = occupancy
            best = closure
        }
    }
    val found = best
    return T71CensusRecord(
        topology = topology.name,
        placements = placements,
        covalentPlacements = covalent,
        reachFeasiblePlacements = feasible.size,
        solved = ranked.size,
        closingPlacements = closing,
        bestWorstCovalentZ = if (bestZ == Double.MAX_VALUE) 0.0 else bestZ,
        bestMinimumOccupancy = bestOccupancy,
        bestAzimuthDegrees = (found?.azimuth ?: 0.0) * 180.0 / PI,
        bestCentreX = found?.centreX ?: 0.0,
        bestCentreY = found?.centreY ?: 0.0,
        bestChordAzimuthDegrees = (found?.chordAzimuth ?: 0.0) * 180.0 / PI,
        bestWorstGap = found?.worstGap ?: 0.0,
        verdict = when {
            closing > 0 -> "A TORSION-FEASIBLE PLACEMENT EXISTS — $closing of ${ranked.size} solved"
            feasible.isEmpty() -> "NO placement passes even the reach bound"
            else -> "NO placement closes — 0 of ${ranked.size} solved, of ${feasible.size} reach-feasible"
        }
    )
}

/**
 * One placement, evaluated exactly as `C-0029`'s own `closureAt` does — the same seat height, the
 * same nearest-phosphate rule, the same van der Waals floor and the same topology constraint.
 */
private fun singlePlacement(
    backbone: DuplexBackbone,
    topology: RoutingTopology,
    axial: Double,
    lateral: Double,
    azimuth: Double
): JunctionClosure? {
    val faceHeight = seatFaceHeight(
        lateral, backbone.duplexRadius, backbone.duplexRadius, INTERHELICAL
    )
    val first = backbone.standoffTerminus(axial, lateral, faceHeight, azimuth, 0)
    val second = backbone.standoffTerminus(axial, lateral, faceHeight, azimuth, 1)
    var firstGap = Double.MAX_VALUE
    var firstResidual = Double.MAX_VALUE
    var firstDuplex = 0
    var firstStrand = 0
    var firstIndex = 0
    var secondGap = Double.MAX_VALUE
    var secondResidual = Double.MAX_VALUE
    var secondDuplex = 0
    var secondStrand = 0
    var secondIndex = 0
    val lowIndex = kotlin.math.floor((axial - 2.5) / backbone.risePerBasePair).toInt()
    val highIndex = kotlin.math.ceil((axial + 2.5) / backbone.risePerBasePair).toInt()
    for (d in -1..1) {
        for (strand in 0..1) {
            for (index in lowIndex..highIndex) {
                val p = backbone.sheetPhosphate(d * INTERHELICAL, strand, index)
                val toFirst = (p - first).length
                val toSecond = (p - second).length
                if (toFirst < BForm.PHOSPHATE_HARD_SEPARATION) return null
                if (toSecond < BForm.PHOSPHATE_HARD_SEPARATION) return null
                val residualFirst = linkWindowResidual(toFirst)
                if (residualFirst < firstResidual) {
                    firstResidual = residualFirst
                    firstGap = toFirst
                    firstDuplex = d
                    firstStrand = strand
                    firstIndex = index
                }
                if (topology == RoutingTopology.INDEPENDENT_STAPLES &&
                    residualFirst < Double.MAX_VALUE
                ) {
                    val residualSecond = linkWindowResidual(toSecond)
                    if (residualSecond < secondResidual) {
                        secondResidual = residualSecond
                        secondGap = toSecond
                        secondDuplex = d
                        secondStrand = strand
                        secondIndex = index
                    }
                }
            }
        }
    }
    if (firstResidual == Double.MAX_VALUE) return null
    if (topology == RoutingTopology.SCAFFOLD_EXCURSION) {
        var bestResidual = Double.MAX_VALUE
        var bestGap = Double.MAX_VALUE
        var bestIndex = firstIndex
        listOf(firstIndex - 1, firstIndex + 1).forEach { candidate ->
            val p = backbone.sheetPhosphate(firstDuplex * INTERHELICAL, firstStrand, candidate)
            val distance = (p - second).length
            val residual = linkWindowResidual(distance)
            if (residual < bestResidual) {
                bestResidual = residual
                bestGap = distance
                bestIndex = candidate
            }
        }
        if (bestResidual == Double.MAX_VALUE || bestGap < BForm.PHOSPHATE_HARD_SEPARATION) {
            return null
        }
        secondGap = bestGap
        secondDuplex = firstDuplex
        secondStrand = firstStrand
        secondIndex = bestIndex
    } else if (secondResidual == Double.MAX_VALUE) {
        return null
    }
    val chord = second - first
    return JunctionClosure(
        topology = topology,
        centreX = axial,
        centreY = lateral,
        faceHeight = faceHeight,
        azimuth = azimuth,
        firstGap = firstGap,
        secondGap = secondGap,
        firstDuplex = firstDuplex,
        firstStrand = firstStrand,
        firstIndex = firstIndex,
        secondDuplex = secondDuplex,
        secondStrand = secondStrand,
        secondIndex = secondIndex,
        chordAzimuth = kotlin.math.atan2(chord.y, chord.x),
        firstTerminusRadius = backbone.phosphateRadius,
        secondTerminusRadius = backbone.phosphateRadius
    )
}

fun main() {
    val backbone = DuplexBackbone()
    val template = NucleotideTemplate.B_SOUTH

    // ---- the cheap bounds, which run before any solve
    val bounds = listOf(
        T71BoundRecord(
            "phosphodiester reach, cis (alpha = 0)", PhosphodiesterGeometry.reachMinimum, "nm",
            "the shortest O3'···C5' the chain O3'–P–O5'–C5' can span at ideal geometry"
        ),
        T71BoundRecord(
            "phosphodiester reach, trans (alpha = 180)", PhosphodiesterGeometry.reachMaximum, "nm",
            "the longest, and a link outside this interval closes at NO torsion"
        ),
        T71BoundRecord(
            "reach, tolerant lower", PhosphodiesterGeometry.reachMinimumTolerant, "nm",
            "widened by three measured standard deviations on every bond and angle"
        ),
        T71BoundRecord(
            "reach, tolerant upper", PhosphodiesterGeometry.reachMaximumTolerant, "nm", "as above"
        ),
        T71BoundRecord(
            "O3'–P bond the PINNED reading demands", PhosphodiesterGeometry.O3_P_BOND, "nm",
            "measured on ${PhosphodiesterGeometry.surveyLinkages} crystallographic linkages"
        ),
        T71BoundRecord(
            "stylised duplex intrastrand P···P", stylisedIntrastrandStep(backbone), "nm",
            "the step every closure search in this programme is written on"
        ),
        T71BoundRecord(
            "measured intrastrand P···P, C2'-endo", PhosphodiesterGeometry.stepSouth, "nm",
            "the south end of the window `C-0029` cites from Bosco et al."
        ),
        T71BoundRecord(
            "measured intrastrand P···P, C3'-endo", PhosphodiesterGeometry.stepNorth, "nm",
            "the north end"
        ),
        T71BoundRecord(
            "measured phosphate radius, B-form", template.phosphateRadius, "nm",
            "against the 1.00 nm `C-0029` adopts and the 0.90 nm it carries as a bracket"
        )
    )

    // ---- the baseline: an ideal duplex step, at the measured geometry and at this
    // ---- project's own stylised one
    val baseline = buildList {
        PhosphateReading.entries.forEach { reading ->
            listOf(
                MeasuredDinucleotide.B_SOUTH to
                        "MEASURED dinucleotide, C2'-endo (${NucleotideTemplate.B_SOUTH.source})",
                MeasuredDinucleotide.A_NORTH to
                        "MEASURED dinucleotide, C3'-endo (${NucleotideTemplate.A_NORTH.source})"
            ).forEach { (step, name) ->
                val (donor, acceptor) = step.residues()
                val link = JunctionLink(
                    name, "i to i+1",
                    ResidueAnchor(
                        "residue i", donor.site.phosphate, donor.site.axisPoint,
                        donor.site.axisDirection
                    ),
                    ResidueAnchor(
                        "residue i+1", acceptor.site.phosphate, acceptor.site.axisPoint,
                        acceptor.site.axisDirection
                    ),
                    (acceptor.site.phosphate - donor.site.phosphate).length
                )
                add(
                    solve("baseline", link, reading, listOf(donor.template, acceptor.template))
                )
            }
            add(
                baselineRecord(
                    "baseline", "one template reapplied at its own fitted screw", template.twist,
                    template.rise, template.phosphateRadius, reading
                )
            )
            add(
                baselineRecord(
                    "baseline", "stylised duplex (10.67 bp/turn, 0.34 nm, r = 1.00 nm)",
                    360.0 / backbone.basePairsPerTurn, backbone.risePerBasePair,
                    backbone.phosphateRadius, reading, NucleotideTemplate.ALL
                )
            )
            add(
                baselineRecord(
                    "baseline", "stylised twist and rise at the measured radius",
                    360.0 / backbone.basePairsPerTurn, backbone.risePerBasePair,
                    template.phosphateRadius, reading, NucleotideTemplate.ALL
                )
            )
        }
    }

    // ---- C-0029: one junction, two links, at both its routings
    val singleLinks = buildList {
        listOf(
            RoutingTopology.INDEPENDENT_STAPLES to "R1 two independent staples",
            RoutingTopology.SCAFFOLD_EXCURSION to "R2 scaffold excursion"
        ).forEach { (topology, name) ->
            addAll(linksOf(backbone, bestTwoLinkClosure(topology = topology), name))
        }
    }
    val singleRecords = singleLinks.flatMap { link ->
        PhosphateReading.entries.map { solve("C-0029 single junction", link, it) }
    }

    // ---- C-0042: two junctions, four links, at its adopted 7 bp and at the 6 bp steric floor
    // `C-0042`'s own adopted search, parameter for parameter.
    val pairSearch = PairedJunctionSearch(
        backbone = backbone,
        axialStepsPerBasePair = 8,
        azimuthSteps = 360,
        refinements = 2,
        lateralSeats = (-6..6).map { it * 0.1 },
        targetDuplexes = listOf(0)
    )
    val pairLinks = buildList {
        listOf(6, 7).forEach { separation ->
            val pair = pairSearch.bestPair(separation)
            if (pair != null) {
                addAll(linksOf(backbone, pair.first, "$separation bp, leg 1"))
                addAll(linksOf(backbone, pair.second, "$separation bp, leg 2"))
            }
        }
    }
    val pairRecords = pairLinks.flatMap { link ->
        PhosphateReading.entries.map { solve("C-0042 junction pair", link, it) }
    }

    // ---- C-0052: three junctions, six links, on C-0048's own 13 bp crossbar
    // `C-0052`'s own `searchFor` defaults, parameter for parameter.
    val trio = CrossbarTrioSearch(
        backbone = backbone,
        crossbarBasePairs = 13,
        separationBasePairs = 7,
        junctions = TrioJunctionSpec.cap(7, backbone.risePerBasePair),
        azimuthSteps = 180,
        phaseSteps = 360,
        axialSteps = 8,
        lateralSeats = listOf(-0.4, -0.2, 0.0, 0.2, 0.4)
    ).best()
    val trioLinks = trio?.placements?.flatMap { linksOf(backbone, trio, it) } ?: emptyList()
    val trioRecords = trioLinks.flatMap { link ->
        PhosphateReading.entries.map { solve("C-0052 crossbar trio", link, it) }
    }

    val links = singleRecords + pairRecords + trioRecords
    val free = links.filter { it.reading == PhosphateReading.FREE.name }
    val scales = listOf(
        scaleRecord(
            "C-0029 single junction", "C-0029", 2,
            free.filter { it.scale == "C-0029 single junction" }
        ),
        scaleRecord(
            "C-0042 junction pair", "C-0042", 4,
            free.filter { it.scale == "C-0042 junction pair" }
        ),
        scaleRecord(
            "C-0052 crossbar trio", "C-0052", 3,
            free.filter { it.scale == "C-0052 crossbar trio" }
        )
    )

    // ---- the torsion-aware census over C-0029's WHOLE placement space
    val censusRecords = listOf(
        census(backbone, RoutingTopology.INDEPENDENT_STAPLES),
        census(backbone, RoutingTopology.SCAFFOLD_EXCURSION)
    )

    // ---- sensitivities: does the verdict belong to a convention?
    val reference = free.filter { it.scale == "C-0029 single junction" }
    val sensitivities = buildList {
        fun record(axis: String, label: String, rows: List<T71LinkRecord>) {
            add(
                T71SensitivityRecord(
                    axis = axis,
                    label = label,
                    worstCovalentZ = rows.maxOf { it.worstCovalentZ },
                    worstMinimumStrainZ = rows.maxOf { it.minimumStrainZ },
                    reachFeasibleLinks = rows.count { it.reachFeasible },
                    covalentAcceptableLinks = rows.count { it.covalentAcceptable },
                    populatedLinks = rows.count { it.torsionsPopulated },
                    links = rows.size,
                    closes = rows.all { it.closes },
                    verdictMoves = rows.all { it.closes } != reference.all { it.closes }
                )
            )
        }
        record("reference", "C-0029's own geometry, both templates, free phosphorus", reference)
        record(
            "sugar pucker", "C2'-endo (south) only — the pucker C-0042 and C-0052 demand",
            singleLinks.map {
                solve("pucker south", it, PhosphateReading.FREE, listOf(NucleotideTemplate.B_SOUTH))
            }
        )
        record(
            "sugar pucker", "C3'-endo (north) only — the pucker C-0029's 0.600 nm links demand",
            singleLinks.map {
                solve("pucker north", it, PhosphateReading.FREE, listOf(NucleotideTemplate.A_NORTH))
            }
        )
        listOf(
            BForm.PHOSPHATE_RADIUS to "phosphate radius 1.00 nm (C-0029's, Hedley et al.)",
            BForm.PHOSPHATE_RADIUS_NARROW to "phosphate radius 0.90 nm (C-0029's bracket)",
            template.phosphateRadius to "phosphate radius %.4f nm (measured here)".format(
                template.phosphateRadius
            )
        ).forEach { (radius, label) ->
            val bent = DuplexBackbone(phosphateRadius = radius)
            record(
                "phosphate radius", label,
                linksOf(
                    bent, bestTwoLinkClosure(backbone = bent, topology = RoutingTopology.SCAFFOLD_EXCURSION),
                    "R2"
                ).map { solve("radius", it, PhosphateReading.FREE) }
            )
        }
        listOf(
            BForm.MINOR_GROOVE_BACKBONE_ANGLE to "groove 120° (nominal)",
            BForm.MINOR_GROOVE_BACKBONE_ANGLE_WIDE to "groove 154° (wide)",
            180.0 to "groove 180° (the hard, convention-free chord)"
        ).forEach { (groove, label) ->
            val bent = DuplexBackbone(minorGrooveAngle = groove)
            record(
                "groove convention", label,
                linksOf(
                    bent, bestTwoLinkClosure(backbone = bent, topology = RoutingTopology.SCAFFOLD_EXCURSION),
                    "R2"
                ).map { solve("groove", it, PhosphateReading.FREE) }
            )
        }
        record(
            "reading", "the PINNED reading — the closure searches' own criterion, taken literally",
            links.filter {
                it.scale == "C-0029 single junction" && it.reading == PhosphateReading.PINNED.name
            }
        )
    }

    // ---- convergence
    val convergence = buildList {
        val (baseDonor, baseAcceptor) = MeasuredDinucleotide.B_SOUTH.residues()
        fun baselineAt(steps: Int) =
            closePhosphodiester(baseDonor, baseAcceptor, PhosphateReading.FREE, steps, REFINEMENTS)
                .worstCovalentZ
        val finestBaseline = baselineAt(720)
        listOf(45, 90, 180, 360, 720).forEach { steps ->
            val value = baselineAt(steps)
            add(
                T71ConvergenceRecord(
                    "baseline worst covalent z", "closure grid steps", steps.toDouble(), value,
                    abs(value - finestBaseline)
                )
            )
        }
        val worstLink = singleLinks.first()
        val finestLink =
            solve("convergence", worstLink, PhosphateReading.FREE, gridSteps = 720).worstCovalentZ
        listOf(45, 90, 180, 360, 720).forEach { steps ->
            val value =
                solve("convergence", worstLink, PhosphateReading.FREE, gridSteps = steps).worstCovalentZ
            add(
                T71ConvergenceRecord(
                    "junction worst covalent z", "closure grid steps", steps.toDouble(), value,
                    abs(value - finestLink)
                )
            )
        }
        val repeat = solve("convergence", worstLink, PhosphateReading.FREE).worstCovalentZ
        add(
            T71ConvergenceRecord(
                "junction worst covalent z, repeat call", "repeat", 1.0, repeat,
                abs(repeat - solve("convergence", worstLink, PhosphateReading.FREE).worstCovalentZ)
            )
        )
    }

    // ---- upstream reproductions
    val singleClosure = bestTwoLinkClosure(topology = RoutingTopology.SCAFFOLD_EXCURSION)
    val pairSeven = pairSearch.bestPair(7)
    val reproductions = buildList {
        fun add(quantity: String, published: Double, derived: Double) {
            add(
                T71ReproductionRecord(
                    quantity, published, derived,
                    if (published == 0.0) abs(derived) else abs(derived - published) / abs(published)
                )
            )
        }
        add("C-0029 R2 worst link gap [nm]", 0.600, singleClosure.worstGap)
        add(
            "C-0042 aligned pair binding link [nm]", 0.6969,
            pairSeven?.let { maxOf(it.first.worstGap, it.second.worstGap) } ?: 0.0
        )
        add("C-0052 trio binding link [nm]", 0.679, trio?.worstGap ?: 0.0)
        add("C-0029 stylised intrastrand step [nm]", 0.6728, stylisedIntrastrandStep(backbone))
        add("Parkinson P–O3' bond [nm]", 0.1607, PhosphodiesterGeometry.O3_P_BOND)
        add("Parkinson P–O5' bond [nm]", 0.1593, PhosphodiesterGeometry.P_O5_BOND)
        add("Parkinson C3'–O3'–P [deg]", 119.7, PhosphodiesterGeometry.ANGLE_C3_O3_P)
        add("Parkinson O3'–P–O5' [deg]", 104.0, PhosphodiesterGeometry.ANGLE_O3_P_O5)
        add("Parkinson P–O5'–C5' [deg]", 120.9, PhosphodiesterGeometry.ANGLE_P_O5_C5)
        val k1 = BDnaConformerSurvey.classes.first().centre
        add("Svozil BI alpha [deg]", -61.0, k1.alpha)
        // Svozil publishes 179.3 on a 0..360 axis; folded to this project's (-180, 180] it is
        // 179.3 and not -180.7, and comparing the two numerically rather than angularly is the
        // kind of error a wrap convention exists to prevent.
        add("Svozil BI beta [deg]", 179.3, wrapDegrees(k1.beta))
        add("Svozil BI gamma [deg]", 48.4, k1.gamma)
        add("Svozil BI delta [deg]", 132.8, k1.delta)
        add("Svozil BI epsilon [deg]", -178.3, k1.epsilon)
        add("Svozil BI zeta [deg]", -96.8, k1.zeta)
        add("Svozil BI chi [deg]", -109.7, k1.chi)
        add("Svozil AI delta (C3'-endo) [deg]", 82.1, BDnaConformerSurvey.classes[1].centre.delta)
        add("Bosco C3'-endo P···P [nm]", 0.60, PhosphodiesterGeometry.stepNorth)
        add("Bosco C2'-endo P···P [nm]", 0.70, PhosphodiesterGeometry.stepSouth)
        add("B-form rise [nm]", 0.34, template.rise)
    }

    val literature = listOf(
        T71LiteratureRecord(
            "What are the canonical BI and BII B-DNA backbone torsions?",
            "BI: alpha 299.0, beta 179.3, gamma 48.4, delta 132.8, epsilon 181.7, zeta 263.2, " +
                    "chi 250.3 (0–360 convention), on 418 dinucleotides of 118 naked B-DNA " +
                    "structures. The published ± are 95 % CONFIDENCE INTERVALS ON THE MEAN, not " +
                    "spreads — back-converting gives population SDs of 6–12°, an order of " +
                    "magnitude larger.",
            "read directly",
            "Svozil, Kalina, Omelka & Schneider, Nucleic Acids Res. 36:3690 (2008), Table 3, " +
                    "PMC2441783"
        ),
        T71LiteratureRecord(
            "What does the literature say about the backbone at a four-way JUNCTION?",
            "\"the junction site itself is formed by a sharp turn in the phosphodiester backbone. " +
                    "This sharp turn is captured mainly by a change in the ε, ζ, α + 1, β + 1, " +
                    "and γ + 1 torsions, which adopt unusual values… However, the scarcity of " +
                    "structural data did not allow to classify the junction-site step as a " +
                    "distinct conformation in any of these structures.\" And of 2CRX: the " +
                    "\"ζ, α + 1, β + 1 and γ + 1 torsions … adopt a rare combination g+/g+/g+/t, " +
                    "which has not observed among stable conformers even in the more variable RNA.\"",
            "read directly",
            "Svozil et al. (2008), as above"
        ),
        T71LiteratureRecord(
            "Which (alpha, gamma) combinations are unpopulated, and at what cost?",
            "gauche+ 89.7 %, trans 6.3 %, gauche− 4.0 % for gamma over 1245 steps of 64 " +
                    "protein–B-DNA structures; \"the three remaining possible combinations, " +
                    "namely t/g–, t/g+ and g–/g–, are located in high energy zones\"; \"The " +
                    "lowest energy barrier is >7 kcal/mol\"; and in free B-DNA \"∼2% of the " +
                    "dinucleotide steps … are non-canonical\". Svozil adds that the trans/trans " +
                    "alpha/gamma combination \"is never observed in the B-form\".",
            "read directly",
            "Várnai, Djuranovic, Lavery & Hartmann, Nucleic Acids Res. 30:5398 (2002), Table 2, " +
                    "PMC140057; Svozil et al. (2008)"
        ),
        T71LiteratureRecord(
            "What are the refinement restraint targets for the phosphodiester?",
            "P–O3' 1.607(12) Å, P–O5' 1.593(10) Å, C3'–O3'–P 119.7(12)°, O3'–P–O5' 104.0(19)°, " +
                    "P–O5'–C5' 120.9(16)°. And the apex is NOT a constant: O3'–P–O5' closes from " +
                    "104.2(15)° to 99.9(7)° between the BI-like and BII-like classes.",
            "read directly (Parkinson et al. 1996 read from Kowiel et al. Table 3, which " +
                    "reproduces it verbatim; the IUCr original returns HTTP 403)",
            "Kowiel, Brzezinski & Jaskolski, Nucleic Acids Res. 44:8479 (2016), Tables 3–4, " +
                    "PMC5041494"
        ),
        T71LiteratureRecord(
            "What are the pucker ranges and their delta values?",
            "\"the C3′-endo state was defined for the pseudorotation angle P in the range " +
                    "0° ≤ P ≤ 36° and the C2′-endo state for 144° ≤ P ≤ 190°\"; delta is " +
                    "82.1±0.7° at C3'-endo (AI) and 132.8±1.0° (BI) / 143.0±0.9° (BII) at " +
                    "C2'-endo. So delta CARRIES the pucker and is not independent of it.",
            "read directly",
            "Kowiel, Brzezinski, Gilski & Jaskolski, Nucleic Acids Res. 48:962 (2020), PMC6954431; " +
                    "Svozil et al. (2008) Table 3"
        ),
        T71LiteratureRecord(
            "Does the 0.60/0.70 nm phosphodiester window this project cites have a primary source?",
            "NO. Bosco et al.'s sentence is verified verbatim — \"a fraction of the deoxyriboses " +
                    "could interconvert from C3-endo (interphosphate distance 0.6 nm) to C2-endo " +
                    "conformation (interphosphate distance 0.7 nm)\" — but its own references are " +
                    "two TEXTBOOKS (Saenger 1984; Bloomfield, Crothers & Tinoco 1999), neither " +
                    "reachable. This task therefore MEASURES the pair instead: 0.607 nm north " +
                    "and 0.664 nm south over 13 084 crystallographic linkages.",
            "read directly (the sentence), not found (its primary source)",
            "Bosco, Camunas-Soler & Ritort, Nucleic Acids Res. 42:2064 (2014), PMC3919573"
        ),
        T71LiteratureRecord(
            "Has anyone published a torsion-level check of a 90° out-of-plane origami junction?",
            "NOT FOUND. 33 EuropePMC queries and 6 arXiv queries. Zero hits for \"scaffold " +
                    "excursion\", \"DNA origami\" AND \"perpendicular duplex\", \"origami\" AND " +
                    "\"vertical duplex\", \"DNA origami\" AND \"helix perpendicular to the " +
                    "plane\", \"DNA nanostructure\" AND \"backbone torsion\" AND simulation. And " +
                    "the atomistic origami literature stops at the HELIX level: Maffeo, Yoo & " +
                    "Aksimentiev (2016) characterises a crossover by three helix angles and " +
                    "contains \"torsion\" 0 times and \"dihedral\" 0 times; Adendorff et al. " +
                    "(2022), all-atom MD of the immobile four-way junction itself, contains " +
                    "\"torsion\" 0 and \"BII\" 0.",
            "not found",
            "EuropePMC REST search and the arXiv API, ~9 s apart; the control query all:oxDNA " +
                    "returned 31, so the endpoint was live"
        )
    )

    val worstFree = free.maxByOrNull { it.worstCovalentZ }
    val baselineStylised = baseline.first {
        it.junction.startsWith("stylised duplex") && it.reading == PhosphateReading.FREE.name
    }
    val result = T71Result(
        task = "T-71",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "medium" to "aqueous 2 mM MgCl2",
            "lattice" to "single-layer square-lattice Rothemund sheet, 2.69 nm interhelical",
            "backbone model" to "B-form phosphate helix, 10.67 bp/turn, 0.34 nm rise, " +
                    "r_P = 1.00 nm, groove 120° — `C-0029`'s own",
            "torsion convention" to "IUPAC, degrees, folded to (-180, 180]",
            "covalent restraints" to "measured on ${PhosphodiesterGeometry.surveyLinkages} " +
                    "crystallographic phosphodiester linkages",
            "populated regions" to "${BDnaConformerSurvey.classes.size} k-means conformer " +
                    "classes over ${BDnaConformerSurvey.residues} crystallographic residues",
            "strain ceiling" to "${PhosphodiesterGeometry.STRAIN_CEILING} measured standard " +
                    "deviations on every bond and angle"
        ),
        bounds = bounds,
        baseline = baseline,
        links = links,
        scales = scales,
        census = censusRecords,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        literature = literature,
        findings = mapOf(
            "verdict" to scales.joinToString("; ") { "${it.scale}: ${it.verdict}" },
            "baseline" to ("an ideal measured B-form step closes at worst covalent z = %.2f, and " +
                    "this project's own stylised duplex at %.2f — so the instrument discriminates " +
                    "and a junction's residual is not the template's").format(
                baseline.first {
                    it.junction.startsWith("MEASURED dinucleotide, C2") &&
                            it.reading == PhosphateReading.FREE.name
                }.worstCovalentZ,
                baselineStylised.worstCovalentZ
            ),
            "census" to censusRecords.joinToString("; ") {
                "${it.topology}: ${it.covalentPlacements} of ${it.placements} placements close on " +
                        "phosphate distance, ${it.reachFeasiblePlacements} of those pass the " +
                        "closed-form reach bound, and ${it.closingPlacements} of ${it.solved} " +
                        "solved close at torsion level"
            },
            "what the census settles" to "A torsion-feasible placement EXISTS in `C-0029`'s own " +
                    "search space, and none of the three claims' reported optima is one. The " +
                    "phosphate-distance objective is BLIND to torsion feasibility: it selects the " +
                    "extremes of the measured window, and those are exactly the placements a " +
                    "backbone cannot make. The routing has to be searched for on the torsion " +
                    "criterion, not filtered by it afterwards.",
            "worst link" to ("the worst link anywhere is %s / %s at z = %.1f, against a ceiling " +
                    "of %.1f").format(
                worstFree?.scale, worstFree?.junction, worstFree?.worstCovalentZ,
                PhosphodiesterGeometry.STRAIN_CEILING
            ),
            "what this cannot establish" to "A torsion check is still a NECESSARY condition and " +
                    "never a sufficient one. It does not establish that a junction assembles, " +
                    "folds, or survives a buffer; it establishes only that the backbone is not " +
                    "excluded by its own covalent geometry and by the conformations DNA is " +
                    "observed to adopt. Exactly as `C-0029` said of the phosphate distances."
        )
    )

    val json = Json { prettyPrint = true }
    val file = File("gpd/results/T-71-backbone-torsion-closure.json")
    file.parentFile?.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")

    println("T-71 — backbone torsions of the closed 90° routing")
    println()
    println("cheap bounds")
    result.bounds.forEach { println("  %-46s %10.5f %-4s  %s".format(it.quantity.take(46), it.value, it.units, it.note)) }
    println()
    println("baseline (junction, reading, O3'-P [nm], worst z, conformer, dist, populated, closes)")
    result.baseline.forEach {
        println(
            ("  %-54s %-6s %7.4f %8.2f %7.2f  %7.1f %7.1f %7.1f %7.1f %7.1f %7.1f %7.1f  " +
                    "%8.5f %-8s %5s %5s").format(
                it.junction.take(54), it.reading, it.o3pBond, it.worstCovalentZ,
                it.minimumStrainZ, it.alpha, it.beta, it.gamma, it.delta, it.epsilon, it.zeta, it.chi,
                it.minimumOccupancy, it.leastPopulatedTorsion, it.torsionsPopulated, it.closes
            )
        )
    }
    println()
    println("links, FREE reading (scale, junction, link, gap, O3'-P, reach, z, alpha..chi, closes)")
    free.forEach {
        println(
            ("  %-24s %-16s %-7s %6.4f %7.4f %5s %8.2f %7.2f  " +
                    "%7.1f %7.1f %7.1f %7.1f %7.1f %7.1f %7.1f  %8.5f %-8s %5s %5s").format(
                it.scale.take(24), it.junction.take(16), it.link, it.phosphateGap, it.o3pBond,
                it.reachFeasible, it.worstCovalentZ, it.minimumStrainZ, it.alpha, it.beta, it.gamma, it.delta,
                it.epsilon, it.zeta, it.chi, it.minimumOccupancy,
                it.leastPopulatedTorsion, it.torsionsPopulated, it.closes
            )
        )
    }
    println()
    println("scales")
    result.scales.forEach {
        println(
            ("  %-24s junctions %d links %2d reach %2d pinned %2d z %6.2f–%6.2f minStrain %6.2f " +
                    "covalent %2d populated %2d rarest %8.5f  %s").format(
                it.scale.take(24), it.junctions, it.links, it.reachFeasibleLinks,
                it.pinnedFeasibleLinks, it.bestWorstCovalentZ, it.worstWorstCovalentZ,
                it.worstMinimumStrainZ, it.covalentAcceptableLinks, it.populatedLinks,
                it.rarestOccupancy, it.verdict
            )
        )
    }
    println()
    println("census over C-0029's whole placement space")
    result.census.forEach {
        println(
            ("  %-20s placements %6d covalent %5d reach-feasible %5d solved %4d closing %4d " +
                    "best z %7.2f occupancy %8.5f  %s").format(
                it.topology, it.placements, it.covalentPlacements, it.reachFeasiblePlacements,
                it.solved, it.closingPlacements, it.bestWorstCovalentZ, it.bestMinimumOccupancy,
                it.verdict
            )
        )
        println(
            "      best placement: azimuth %6.1f deg, x %6.3f, y %6.3f, chord %7.1f deg, gap %6.4f".format(
                it.bestAzimuthDegrees, it.bestCentreX, it.bestCentreY,
                it.bestChordAzimuthDegrees, it.bestWorstGap
            )
        )
    }
    println()
    println("sensitivities")
    result.sensitivities.forEach {
        println(
            "  %-20s %-58s z %8.2f min %7.2f reach %d covalent %d populated %d of %d closes %5s moves %s"
                .format(
                    it.axis.take(20), it.label.take(58), it.worstCovalentZ,
                    it.worstMinimumStrainZ, it.reachFeasibleLinks,
                    it.covalentAcceptableLinks, it.populatedLinks, it.links, it.closes,
                    it.verdictMoves
                )
        )
    }
    println()
    println("convergence")
    result.convergence.forEach {
        println(
            "  %-40s %-22s %6.0f %14.9f %10.2e".format(
                it.quantity.take(40), it.control, it.level, it.value, it.departureFromFinest
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    result.reproductions.forEach {
        println(
            "  %-44s %12.6f %12.6f %10.2e".format(
                it.quantity.take(44), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
