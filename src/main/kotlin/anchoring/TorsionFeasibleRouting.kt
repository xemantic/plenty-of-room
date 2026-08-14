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
import org.openrndr.math.Vector3
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * `T-124` — the truss branch's junctions re-derived on `C-0057`'s **torsion-feasible** set.
 *
 * `C-0029`, `C-0042` and `C-0052` all minimise a **phosphate-distance** residual, and `C-0057`
 * showed that objective is blind to the quantity that decides the answer: pushing a phosphate pair
 * to the edge of the measured `[0.60, 0.70]` nm window is exactly what pushes the backbone out of
 * the conformations DNA has ever been observed in. All three reported optima therefore fail — and
 * yet the same search space contains placements that close.
 *
 * What this file adds is the **conjunction**. A placement is useful to the design only if it is
 *
 * 1. **covalent** — both links of every junction inside the measured window, no van der Waals
 *    contact (`C-0029`'s own test, unchanged);
 * 2. **reach-feasible** — every link inside the closed-form `O3′···C5′` interval, which is a proof
 *    of exclusion and costs three atom placements (`C-0057`'s bound 2);
 * 3. **torsion-closing** — every link's covalent geometry within three measured σ *and* every
 *    torsion in a populated bin (`C-0057`'s solve, re-run as a library);
 * 4. **aligned** — its chord on the axis the design loads.
 *
 * The fourth is the one nobody has been able to ask, because `C-0057`'s census ranked its
 * placements by **reach margin** and reports its two best chords at 159.0° and −51.0°, against the
 * 90.0° `C-0042` needs. Ranking the *same* feasible set by **misalignment** instead is what this
 * file does, and it is the whole method: the first closing placement met in that order **is** the
 * best-aligned closing placement on the grid.
 *
 * ### Conventions, restated
 *
 * Lengths nm, angles radians unless a name says degrees. The sheet duplexes run along `x̂` at
 * `z = 0`, spaced `INTERHELICAL` in `ŷ`; a standoff stands along `+ẑ`. **The chord azimuth is
 * `ψ₀ + Δ/2 + π/2`**, a function of the standoff's own azimuth alone — `CH-0056`'s point, which
 * this file upholds and then qualifies: the *chord* inherits no lattice phase, but **feasibility
 * does**, because it is a relation between two bodies.
 */

// ---------------------------------------------------------------- the chord, as a design variable

/**
 * The chord azimuth a standoff at [azimuth] presents — `ψ₀ + Δ/2 + π/2`.
 *
 * It is `CrossbarTrioSearch.chordAzimuthOf` on the sheet's own frame, and it depends on the
 * standoff's azimuth and on **nothing else**: not on where it sits, not on what it links to.
 */
fun chordAzimuthOfStandoff(
    azimuth: Double,
    backbone: DuplexBackbone = DuplexBackbone()
): Double = azimuth + 0.5 * backbone.minorGrooveAngle * PI / 180.0 + 0.5 * PI

/** The inverse of [chordAzimuthOfStandoff] — the standoff azimuth whose chord lies at [wanted]. */
fun alignedStandoffAzimuth(
    wanted: Double,
    backbone: DuplexBackbone = DuplexBackbone()
): Double = wanted - 0.5 * backbone.minorGrooveAngle * PI / 180.0 - 0.5 * PI

/**
 * **The alignment band**, in radians — half the sheet's azimuthal quantum, `C-0029`'s own
 * ±16.87° at 10.67 bp/turn, whose `cos²` is its 8.4 % allowance on the base couple.
 *
 * `CH-0056` established that the quantum does not *quantise* a free standoff's chord. It is used
 * here for the only thing it is: the **width of the misalignment the design has already declared
 * itself willing to pay**, and therefore the natural band in which to ask whether a feasible
 * placement exists at all.
 */
fun alignmentAllowance(backbone: DuplexBackbone = DuplexBackbone()): Double =
    0.5 * backbone.azimuthQuantum

// ---------------------------------------------------------------- one covalent link, made public

/**
 * One covalent link of a 90° junction, as two [ResidueAnchor]s — one on the seat duplex and one on
 * the standing duplex's terminal base pair.
 *
 * This is `T-71`'s own private `JunctionLink` promoted to the API, because `T-124` has to build
 * the same links at placements `T-71` never visited.
 */
data class JunctionLinkEnds(
    val junction: String,
    val link: String,
    val seat: ResidueAnchor,
    val standoff: ResidueAnchor,
    val phosphateGap: Double
)

/** A sheet phosphate's anchor: the sheet duplex runs along `x` at `y = centreY`, `z = 0`. */
fun sheetLinkAnchor(
    backbone: DuplexBackbone,
    centreY: Double,
    strand: Int,
    index: Int,
    name: String = "sheet"
): ResidueAnchor {
    val p = backbone.sheetPhosphate(centreY, strand, index)
    return ResidueAnchor(name, p, Vector3(p.x, centreY, 0.0), Vector3.UNIT_X)
}

/** A crossbar phosphate's anchor: the crossbar runs along `x` through the origin (`C-0052`). */
fun crossbarLinkAnchor(
    backbone: DuplexBackbone,
    target: CrossbarTarget,
    crossbarBasePairs: Int,
    helicalPhase: Double,
    axialPhase: Double,
    name: String = "crossbar"
): ResidueAnchor {
    val groove = backbone.minorGrooveAngle * PI / 180.0
    val angle = helicalPhase + target.index * backbone.twistPerBasePair + target.strand * groove
    val x = axialPhase + (target.index - 0.5 * (crossbarBasePairs - 1)) * backbone.risePerBasePair
    val p = Vector3(
        x, backbone.phosphateRadius * cos(angle), backbone.phosphateRadius * sin(angle)
    )
    return ResidueAnchor(name, p, Vector3(x, 0.0, 0.0), Vector3.UNIT_X)
}

/** A standing duplex's terminus: its axis runs along [axisDirection] from its own end face. */
fun standoffLinkAnchor(
    terminus: Vector3,
    axisPoint: Vector3,
    axisDirection: Vector3,
    name: String = "standoff"
): ResidueAnchor = ResidueAnchor(name, terminus, axisPoint, axisDirection)

/** `C-0029`'s single junction, as its two links. */
fun junctionLinks(
    backbone: DuplexBackbone,
    closure: JunctionClosure,
    interhelical: Double = Gen1Tile.INTERHELICAL_SHEET,
    junction: String = "junction"
): List<JunctionLinkEnds> {
    val axisPoint = Vector3(closure.centreX, closure.centreY, closure.faceHeight)
    val first = backbone.standoffTerminus(
        closure.centreX, closure.centreY, closure.faceHeight, closure.azimuth, 0
    )
    val second = backbone.standoffTerminus(
        closure.centreX, closure.centreY, closure.faceHeight, closure.azimuth, 1
    )
    return listOf(
        JunctionLinkEnds(
            junction, "link 1",
            sheetLinkAnchor(
                backbone, closure.firstDuplex * interhelical, closure.firstStrand, closure.firstIndex
            ),
            standoffLinkAnchor(first, axisPoint, Vector3.UNIT_Z),
            closure.firstGap
        ),
        JunctionLinkEnds(
            junction, "link 2",
            sheetLinkAnchor(
                backbone, closure.secondDuplex * interhelical, closure.secondStrand,
                closure.secondIndex
            ),
            standoffLinkAnchor(second, axisPoint, Vector3.UNIT_Z),
            closure.secondGap
        )
    )
}

/** `C-0042`'s standoff placement, as its two links. */
fun junctionLinks(
    backbone: DuplexBackbone,
    placement: StandoffPlacement,
    interhelical: Double = Gen1Tile.INTERHELICAL_SHEET,
    junction: String = "junction"
): List<JunctionLinkEnds> {
    val axisPoint = Vector3(placement.centreX, placement.centreY, placement.faceHeight)
    return listOf(
        JunctionLinkEnds(
            junction, "link 1",
            sheetLinkAnchor(
                backbone, placement.firstTarget.duplex * interhelical, placement.firstTarget.strand,
                placement.firstTarget.index
            ),
            standoffLinkAnchor(placement.firstTerminus, axisPoint, Vector3.UNIT_Z),
            placement.firstGap
        ),
        JunctionLinkEnds(
            junction, "link 2",
            sheetLinkAnchor(
                backbone, placement.secondTarget.duplex * interhelical,
                placement.secondTarget.strand, placement.secondTarget.index
            ),
            standoffLinkAnchor(placement.secondTerminus, axisPoint, Vector3.UNIT_Z),
            placement.secondGap
        )
    )
}

/** `C-0052`'s trio placement on a lone crossbar, as its two links. */
fun junctionLinks(
    backbone: DuplexBackbone,
    crossbarBasePairs: Int,
    helicalPhase: Double,
    axialPhase: Double,
    placement: TrioPlacement
): List<JunctionLinkEnds> {
    val direction = when (placement.kind) {
        TrioJunctionKind.LEG -> Vector3(0.0, 0.0, -1.0)
        TrioJunctionKind.FLEXURE -> Vector3(0.0, -1.0, 0.0)
    }
    val axisPoint = when (placement.kind) {
        TrioJunctionKind.LEG -> Vector3(
            placement.axialOffset, placement.lateralOffset, placement.firstTerminus.z
        )

        TrioJunctionKind.FLEXURE -> Vector3(placement.axialOffset, placement.firstTerminus.y, 0.0)
    }
    return listOf(
        JunctionLinkEnds(
            placement.name, "link 1",
            crossbarLinkAnchor(
                backbone, placement.firstTarget, crossbarBasePairs, helicalPhase, axialPhase
            ),
            standoffLinkAnchor(placement.firstTerminus, axisPoint, direction, "junction"),
            placement.firstGap
        ),
        JunctionLinkEnds(
            placement.name, "link 2",
            crossbarLinkAnchor(
                backbone, placement.secondTarget, crossbarBasePairs, helicalPhase, axialPhase
            ),
            standoffLinkAnchor(placement.secondTerminus, axisPoint, direction, "junction"),
            placement.secondGap
        )
    )
}

// ---------------------------------------------------------------- the two verdicts

/**
 * **The cheap bound**, over a whole junction or a whole assembly.
 *
 * @property feasible every link lies inside the tolerant `O3′···C5′` reach interval, at the best
 *   assignment of donor end, strand polarity and sugar pucker the design may choose.
 * @property violation how far, in nm summed over the links, the assembly is outside it — zero when
 *   feasible, and a **proof of exclusion** when positive.
 */
data class ReachVerdict(
    val feasible: Boolean,
    val violation: Double,
    val worstReach: Double,
    val links: Int
)

/** [ReachVerdict] over [links], at the assignment that violates the interval least. */
fun reachVerdict(
    links: List<JunctionLinkEnds>,
    templates: List<NucleotideTemplate> = NucleotideTemplate.ALL
): ReachVerdict {
    require(links.isNotEmpty()) { "links must not be empty" }
    var feasible = true
    var violation = 0.0
    var worst = 0.0
    links.forEach { link ->
        val bound = bestLinkReach(link.seat, link.standoff, templates)
        if (!bound.freeFeasible) feasible = false
        violation += max(
            max(0.0, PhosphodiesterGeometry.reachMinimumTolerant - bound.o3ToC5),
            max(0.0, bound.o3ToC5 - PhosphodiesterGeometry.reachMaximumTolerant)
        )
        worst = max(worst, bound.o3ToC5)
    }
    return ReachVerdict(feasible, violation, worst, links.size)
}

/** **The expensive test**, over a whole junction or a whole assembly. */
data class TorsionVerdict(
    val closes: Boolean,
    val links: Int,
    val closingLinks: Int,
    val worstCovalentZ: Double,
    val minimumOccupancy: Double,
    val leastPopulatedTorsion: String,
    val closures: List<LinkClosure>
)

/** [TorsionVerdict] over [links], each solved by `C-0057`'s inverse-kinematic closure. */
fun torsionVerdict(
    links: List<JunctionLinkEnds>,
    reading: PhosphateReading = PhosphateReading.FREE,
    gridSteps: Int = 180,
    refinements: Int = 6,
    templates: List<NucleotideTemplate> = NucleotideTemplate.ALL
): TorsionVerdict {
    require(links.isNotEmpty()) { "links must not be empty" }
    val closures = links.map {
        bestLinkClosure(it.seat, it.standoff, reading, templates, gridSteps, refinements)
    }
    val rarest = closures.minByOrNull { it.minimumOccupancy }
    return TorsionVerdict(
        closes = closures.all { it.closes },
        links = closures.size,
        closingLinks = closures.count { it.closes },
        worstCovalentZ = closures.maxOf { it.worstCovalentZ },
        minimumOccupancy = closures.minOf { it.minimumOccupancy },
        leastPopulatedTorsion = rarest?.leastPopulatedTorsion ?: "",
        closures = closures
    )
}

// ------------------------------------------------- the single junction's feasible set

/** One placement of `C-0029`'s single junction that is covalent **and** reach-feasible. */
data class SingleJunctionCandidate(
    val closure: JunctionClosure,
    val reachViolation: Double,
    val wantedChordAzimuth: Double = 0.5 * PI
) {

    val chordAzimuth: Double get() = closure.chordAzimuth

    /** The chord's departure from the axis the flexure loads, folded into `[0, π/2]`. */
    val misalignment: Double
        get() = foldedChordMisalignment(closure.chordAzimuth, wantedChordAzimuth)

    /** `cos²ψ` — the share of the base couple that reaches the loaded plane. */
    val loadedCoupleFraction: Double get() = couplePhaseProjection(misalignment)

    val worstGap: Double get() = closure.worstGap
}

/**
 * The result of enumerating `C-0029`'s whole placement grid under `C-0057`'s two filters.
 *
 * @property feasible the placements that are covalent **and** reach-feasible — `C-0057`'s 1 855
 *   for independent staples and 137 for the scaffold excursion, in **misalignment** order rather
 *   than in reach-margin order, which is the whole difference between this task and that one.
 */
data class SingleJunctionEnumeration(
    val topology: RoutingTopology,
    val placements: Int,
    val covalent: Int,
    val feasible: List<SingleJunctionCandidate>
) {

    /** The feasible placements whose chord is within [allowance] of the axis the design wants. */
    fun withinBand(allowance: Double): List<SingleJunctionCandidate> {
        require(allowance >= 0.0) { "allowance must not be negative, was: $allowance" }
        return feasible.filter { it.misalignment <= allowance + 1e-12 }
    }

    /** **The cheap bound's answer**: the best alignment any reach-feasible placement offers. */
    val bestFeasibleMisalignment: Double
        get() = feasible.minOfOrNull { it.misalignment } ?: PI
}

/**
 * `C-0029`'s placement grid — azimuth, axial position and lateral seat — enumerated exactly as
 * `C-0057`'s census enumerates it, so that the feasible set this task optimises over **is** the
 * feasible set `CH-0070` was raised on.
 *
 * The defaults reproduce `C-0057`'s census: 120 × 64 × 9 = 69 120 placements, of which 3 546 are
 * covalent and 1 855 reach-feasible for `INDEPENDENT_STAPLES`.
 */
class SingleJunctionFeasibleSet(
    val backbone: DuplexBackbone = DuplexBackbone(),
    val interhelical: Double = Gen1Tile.INTERHELICAL_SHEET,
    val azimuthSteps: Int = 120,
    val axialSteps: Int = 64,
    val lateralSteps: Int = 9,
    /**
     * Which sheet duplexes a **link** may reach. `listOf(0)` is `C-0042`'s strict reading — one
     * seat duplex and nothing else. Every duplex takes part in the clash test regardless, because
     * restricting the lattice itself would silently drop the half of the test that can only refuse.
     */
    val targetDuplexes: List<Int> = listOf(-1, 0, 1),
    val wantedChordAzimuth: Double = 0.5 * PI,
    val templates: List<NucleotideTemplate> = NucleotideTemplate.ALL
) {

    init {
        require(azimuthSteps >= 1) { "azimuthSteps must be positive, was: $azimuthSteps" }
        require(axialSteps >= 1) { "axialSteps must be positive, was: $axialSteps" }
        require(lateralSteps >= 2) { "lateralSteps must be at least two, was: $lateralSteps" }
        require(interhelical > 0.0) { "interhelical must be positive, was: $interhelical" }
        require(targetDuplexes.isNotEmpty()) { "targetDuplexes must not be empty" }
    }

    val axialPeriod: Double get() = backbone.helicalRepeatBasePairs * backbone.risePerBasePair

    /**
     * One placement, evaluated exactly as `C-0029`'s `closureAt` and `C-0057`'s census evaluate it
     * — the same seat height, the same nearest-window-residual target rule, the same van der Waals
     * floor and the same topology constraint.
     */
    fun placementAt(
        topology: RoutingTopology,
        axial: Double,
        lateral: Double,
        azimuth: Double
    ): JunctionClosure? {
        val faceHeight = seatFaceHeight(
            lateral, backbone.duplexRadius, backbone.duplexRadius, interhelical
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
        val lowIndex = floor((axial - 2.5) / backbone.risePerBasePair).toInt()
        val highIndex = ceil((axial + 2.5) / backbone.risePerBasePair).toInt()
        for (d in -1..1) {
            val eligible = d in targetDuplexes
            for (strand in 0..1) {
                for (index in lowIndex..highIndex) {
                    val p = backbone.sheetPhosphate(d * interhelical, strand, index)
                    val toFirst = (p - first).length
                    val toSecond = (p - second).length
                    if (toFirst < BForm.PHOSPHATE_HARD_SEPARATION) return null
                    if (toSecond < BForm.PHOSPHATE_HARD_SEPARATION) return null
                    if (!eligible) continue
                    val residualFirst = linkWindowResidual(toFirst)
                    if (residualFirst < firstResidual) {
                        firstResidual = residualFirst
                        firstGap = toFirst
                        firstDuplex = d
                        firstStrand = strand
                        firstIndex = index
                    }
                    if (topology == RoutingTopology.INDEPENDENT_STAPLES) {
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
                val p = backbone.sheetPhosphate(firstDuplex * interhelical, firstStrand, candidate)
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

    /** Every covalent, reach-feasible placement at one axial position and lateral seat. */
    fun feasibleAt(
        topology: RoutingTopology,
        axial: Double,
        lateral: Double
    ): List<SingleJunctionCandidate> {
        val out = ArrayList<SingleJunctionCandidate>()
        for (a in 0 until azimuthSteps) {
            val azimuth = a * 2.0 * PI / azimuthSteps
            val closure = placementAt(topology, axial, lateral, azimuth) ?: continue
            if (!closure.covalent) continue
            val verdict = reachVerdict(
                junctionLinks(backbone, closure, interhelical), templates
            )
            if (verdict.feasible) {
                out += SingleJunctionCandidate(closure, verdict.violation, wantedChordAzimuth)
            }
        }
        return out
    }

    /** The whole grid, under both cheap filters. */
    fun enumerate(topology: RoutingTopology): SingleJunctionEnumeration {
        var placements = 0
        var covalent = 0
        val feasible = ArrayList<SingleJunctionCandidate>()
        for (a in 0 until azimuthSteps) {
            val azimuth = a * 2.0 * PI / azimuthSteps
            for (b in 0 until axialSteps) {
                val axial = b * axialPeriod / axialSteps
                for (c in 0 until lateralSteps) {
                    val lateral = c * 0.5 * interhelical / (lateralSteps - 1)
                    placements++
                    val closure = placementAt(topology, axial, lateral, azimuth) ?: continue
                    if (!closure.covalent) continue
                    covalent++
                    val verdict = reachVerdict(
                        junctionLinks(backbone, closure, interhelical), templates
                    )
                    if (verdict.feasible) {
                        feasible += SingleJunctionCandidate(
                            closure, verdict.violation, wantedChordAzimuth
                        )
                    }
                }
            }
        }
        return SingleJunctionEnumeration(
            topology = topology,
            placements = placements,
            covalent = covalent,
            feasible = feasible.sortedWith(
                compareBy(
                    { it.misalignment }, { it.reachViolation }, { it.closure.azimuth },
                    { it.closure.centreX }, { it.closure.centreY }
                )
            )
        )
    }
}

// ------------------------------------------------- the re-optimisation, at one junction

/** What the torsion-aware re-optimisation of one scale returns. */
data class AlignedClosureOutcome(
    val scale: String,
    val topology: String,
    val placements: Int,
    val covalent: Int,
    val reachFeasible: Int,
    val inAlignmentBand: Int,
    val solved: Int,
    val closing: Int,
    /** The cheap bound's answer: the best alignment any **reach-feasible** placement offers. */
    val bestFeasibleMisalignmentDegrees: Double,
    /**
     * How deep into the alignment ordering the solve cap reached — the misalignment of the LAST
     * placement solved. A *"does not close"* verdict is bounded by this and by nothing else.
     */
    val deepestSolvedMisalignmentDegrees: Double,
    val closes: Boolean,
    val bestMisalignmentDegrees: Double,
    val bestChordAzimuthDegrees: Double,
    val bestAzimuthDegrees: Double,
    val bestCentreX: Double,
    val bestCentreY: Double,
    val bestWorstGap: Double,
    val bestWorstCovalentZ: Double,
    val bestMinimumOccupancy: Double,
    val bestLoadedCoupleFraction: Double,
    val verdict: String
)

/**
 * `C-0029`'s single junction re-optimised on the feasible set, **ranked by alignment**.
 *
 * The candidates arrive in misalignment order, so the first one that closes at torsion level is
 * the best-aligned closing placement on the grid — which is the quantity `C-0057` names as its
 * largest open item and could not report, because its own ranking was by reach margin.
 */
fun bestAlignedClosure(
    enumeration: SingleJunctionEnumeration,
    backbone: DuplexBackbone = DuplexBackbone(),
    interhelical: Double = Gen1Tile.INTERHELICAL_SHEET,
    allowance: Double = alignmentAllowance(backbone),
    solveCap: Int = 120,
    gridSteps: Int = 60,
    refinements: Int = 4,
    scale: String = "C-0029 single junction"
): AlignedClosureOutcome {
    require(solveCap >= 0) { "solveCap must not be negative, was: $solveCap" }
    val ranked = enumeration.feasible.take(solveCap)
    var closing = 0
    var best: SingleJunctionCandidate? = null
    var bestVerdict: TorsionVerdict? = null
    ranked.forEach { candidate ->
        val verdict = torsionVerdict(
            junctionLinks(backbone, candidate.closure, interhelical),
            gridSteps = gridSteps, refinements = refinements
        )
        if (verdict.closes) {
            closing++
            if (best == null) {
                best = candidate
                bestVerdict = verdict
            }
        }
    }
    val found = best
    val verdict = bestVerdict
    return AlignedClosureOutcome(
        scale = scale,
        topology = enumeration.topology.name,
        placements = enumeration.placements,
        covalent = enumeration.covalent,
        reachFeasible = enumeration.feasible.size,
        inAlignmentBand = enumeration.withinBand(allowance).size,
        solved = ranked.size,
        closing = closing,
        bestFeasibleMisalignmentDegrees = enumeration.bestFeasibleMisalignment * 180.0 / PI,
        deepestSolvedMisalignmentDegrees = (ranked.lastOrNull()?.misalignment ?: 0.0) * 180.0 / PI,
        closes = found != null,
        bestMisalignmentDegrees = (found?.misalignment ?: 0.0) * 180.0 / PI,
        bestChordAzimuthDegrees = (found?.chordAzimuth ?: 0.0) * 180.0 / PI,
        bestAzimuthDegrees = (found?.closure?.azimuth ?: 0.0) * 180.0 / PI,
        bestCentreX = found?.closure?.centreX ?: 0.0,
        bestCentreY = found?.closure?.centreY ?: 0.0,
        bestWorstGap = found?.worstGap ?: 0.0,
        bestWorstCovalentZ = verdict?.worstCovalentZ ?: 0.0,
        bestMinimumOccupancy = verdict?.minimumOccupancy ?: 0.0,
        bestLoadedCoupleFraction = found?.loadedCoupleFraction ?: 0.0,
        verdict = when {
            found != null -> "AN ALIGNED TORSION-FEASIBLE PLACEMENT EXISTS — chord at " +
                    "%.1f° from the loaded axis, %d of %d solved close".format(
                        found.misalignment * 180.0 / PI, closing, ranked.size
                    )

            ranked.isEmpty() -> "NO placement is even covalent and reach-feasible"
            else -> "NO aligned placement closes — 0 of ${ranked.size} solved, " +
                    "best alignment offered by the feasible set " +
                    "%.1f°".format(enumeration.bestFeasibleMisalignment * 180.0 / PI)
        }
    )
}

// ------------------------------------------------- `C-0042`'s pair on one seat duplex

/** What the pair re-optimisation returns at one separation. */
data class AlignedPairOutcome(
    val separationBasePairs: Int,
    val axialPositions: Int,
    val closingPositions: Int,
    val solvedPositions: Int,
    val closes: Boolean,
    val worstMisalignmentDegrees: Double,
    val firstChordDegrees: Double,
    val secondChordDegrees: Double,
    val worstGap: Double,
    val worstCovalentZ: Double,
    val minimumOccupancy: Double,
    val worstLoadedCoupleFraction: Double,
    val firstCentreX: Double,
    val secondCentreX: Double,
    val lateralSeat: Double,
    val verdict: String
)

/**
 * `C-0042`'s pair of 90° junctions on **one** sheet duplex, re-optimised on the feasible set.
 *
 * The pair is strictly harder than two independent junctions: both legs share one lateral seat,
 * sit at a fixed separation along the seat duplex, and must take four **distinct** targets. What
 * makes it tractable is that at each axial position the candidates can be ranked by alignment and
 * solved in that order under a per-position cap — so the field carries, at each position, the
 * best-aligned placement that closes, and the pairing is then arithmetic.
 */
class TorsionFeasiblePairSearch(
    val backbone: DuplexBackbone = DuplexBackbone(),
    val interhelical: Double = Gen1Tile.INTERHELICAL_SHEET,
    val topology: RoutingTopology = RoutingTopology.INDEPENDENT_STAPLES,
    val azimuthSteps: Int = 120,
    val axialStepsPerBasePair: Int = 2,
    val sweepBasePairs: Int = 32,
    val lateralSeats: List<Double> = listOf(0.0),
    val candidatesPerPosition: Int = 4,
    val maxSeparationBasePairs: Int = 12,
    val gridSteps: Int = 60,
    val refinements: Int = 4,
    val wantedChordAzimuth: Double = 0.5 * PI
) {

    init {
        require(azimuthSteps >= 1) { "azimuthSteps must be positive, was: $azimuthSteps" }
        require(axialStepsPerBasePair >= 1) {
            "axialStepsPerBasePair must be positive, was: $axialStepsPerBasePair"
        }
        require(sweepBasePairs >= 1) { "sweepBasePairs must be positive, was: $sweepBasePairs" }
        require(lateralSeats.isNotEmpty()) { "lateralSeats must not be empty" }
        require(candidatesPerPosition >= 1) {
            "candidatesPerPosition must be positive, was: $candidatesPerPosition"
        }
    }

    private val set = SingleJunctionFeasibleSet(
        backbone = backbone,
        interhelical = interhelical,
        azimuthSteps = azimuthSteps,
        targetDuplexes = listOf(0),
        wantedChordAzimuth = wantedChordAzimuth
    )

    val axialStep: Double get() = backbone.risePerBasePair / axialStepsPerBasePair

    /** How many solves the field has consumed — reported, because a cap is part of the verdict. */
    var solves: Int = 0
        private set

    private val fields = HashMap<Double, Array<SingleJunctionCandidate?>>()

    /** The axial span of the memoised field, in grid steps — fixed, so one field serves every separation. */
    val span: Int
        get() = (sweepBasePairs + maxSeparationBasePairs + 1) * axialStepsPerBasePair

    private fun field(seat: Double): Array<SingleJunctionCandidate?> =
        fields.getOrPut(seat) {
            Array(span) { index ->
                val axial = index * axialStep
                val candidates = set.feasibleAt(topology, axial, seat)
                    .sortedWith(
                        compareBy({ it.misalignment }, { it.reachViolation }, { it.closure.azimuth })
                    )
                    .take(candidatesPerPosition)
                candidates.firstOrNull { candidate ->
                    solves++
                    torsionVerdict(
                        junctionLinks(backbone, candidate.closure, interhelical),
                        gridSteps = gridSteps, refinements = refinements
                    ).closes
                }
            }
        }

    /** The best aligned, torsion-closing pair at [separationBasePairs], or a "not found" verdict. */
    fun bestPair(separationBasePairs: Int): AlignedPairOutcome {
        require(separationBasePairs >= 1) {
            "separationBasePairs must be positive, was: $separationBasePairs"
        }
        require(separationBasePairs <= maxSeparationBasePairs) {
            "separationBasePairs must not exceed maxSeparationBasePairs, was: $separationBasePairs"
        }
        val shift = separationBasePairs * axialStepsPerBasePair
        var best: Pair<SingleJunctionCandidate, SingleJunctionCandidate>? = null
        var bestSeat = 0.0
        var bestScore = Double.MAX_VALUE
        var closingPositions = 0
        var positions = 0
        lateralSeats.forEach { seat ->
            val f = field(seat)
            positions += f.size
            closingPositions += f.count { it != null }
            for (i in 0 until f.size - shift) {
                val a = f[i] ?: continue
                val b = f[i + shift] ?: continue
                if (!distinctTargets(a.closure, b.closure)) continue
                val score = max(a.misalignment, b.misalignment)
                if (score < bestScore) {
                    bestScore = score
                    best = a to b
                    bestSeat = seat
                }
            }
        }
        val found = best
        if (found == null) {
            return AlignedPairOutcome(
                separationBasePairs = separationBasePairs,
                axialPositions = positions,
                closingPositions = closingPositions,
                solvedPositions = solves,
                closes = false,
                worstMisalignmentDegrees = 0.0,
                firstChordDegrees = 0.0,
                secondChordDegrees = 0.0,
                worstGap = 0.0,
                worstCovalentZ = 0.0,
                minimumOccupancy = 0.0,
                worstLoadedCoupleFraction = 0.0,
                firstCentreX = 0.0,
                secondCentreX = 0.0,
                lateralSeat = 0.0,
                verdict = "NO torsion-closing pair at $separationBasePairs bp — " +
                        "$closingPositions of $positions axial positions close at all"
            )
        }
        val (a, b) = found
        val verdictA = torsionVerdict(
            junctionLinks(backbone, a.closure, interhelical),
            gridSteps = gridSteps, refinements = refinements
        )
        val verdictB = torsionVerdict(
            junctionLinks(backbone, b.closure, interhelical),
            gridSteps = gridSteps, refinements = refinements
        )
        val worst = max(a.misalignment, b.misalignment)
        return AlignedPairOutcome(
            separationBasePairs = separationBasePairs,
            axialPositions = positions,
            closingPositions = closingPositions,
            solvedPositions = solves,
            closes = true,
            worstMisalignmentDegrees = worst * 180.0 / PI,
            firstChordDegrees = a.chordAzimuth * 180.0 / PI,
            secondChordDegrees = b.chordAzimuth * 180.0 / PI,
            worstGap = max(a.worstGap, b.worstGap),
            worstCovalentZ = max(verdictA.worstCovalentZ, verdictB.worstCovalentZ),
            minimumOccupancy = min(verdictA.minimumOccupancy, verdictB.minimumOccupancy),
            worstLoadedCoupleFraction = couplePhaseProjection(worst),
            firstCentreX = a.closure.centreX,
            secondCentreX = b.closure.centreX,
            lateralSeat = bestSeat,
            verdict = "AN ALIGNED TORSION-CLOSING PAIR EXISTS at $separationBasePairs bp — " +
                    "worst chord %.1f° off the loaded axis".format(worst * 180.0 / PI)
        )
    }

    private fun distinctTargets(a: JunctionClosure, b: JunctionClosure): Boolean {
        val targets = listOf(
            Triple(a.firstDuplex, a.firstStrand, a.firstIndex),
            Triple(a.secondDuplex, a.secondStrand, a.secondIndex),
            Triple(b.firstDuplex, b.firstStrand, b.firstIndex),
            Triple(b.secondDuplex, b.secondStrand, b.secondIndex)
        )
        return targets.toSet().size == 4
    }
}

// ------------------------------------------------- `C-0052`'s trio on the lone crossbar

/** What the trio re-optimisation returns. */
data class AlignedTrioOutcome(
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val lattices: Int,
    val feasibleLattices: Int,
    val solvedLattices: Int,
    /** The best alignment the **reach-feasible** lattices offer at their worst junction. */
    val bestFeasibleMisalignmentDegrees: Double,
    val closes: Boolean,
    val worstMisalignmentDegrees: Double,
    val legMisalignmentDegrees: Double,
    val flexureMisalignmentDegrees: Double,
    val worstGap: Double,
    val worstCovalentZ: Double,
    val minimumOccupancy: Double,
    val helicalPhaseDegrees: Double,
    val axialPhase: Double,
    val lateralSeat: Double,
    val verdict: String
)

/**
 * `C-0052`'s three junctions on one **lone** 13 bp crossbar, re-optimised on the feasible set.
 *
 * The crossbar's helical phase, its axial phase and the shared lateral seat are swept exactly as
 * `CrossbarTrioSearch` sweeps them; what changes is the objective. At each lattice the junctions'
 * candidates are filtered on the reach bound and ranked by their own misalignment, the lattices
 * are then ranked by the **worst** junction's best attainable misalignment, and only then are the
 * torsions solved — in that order, under a stated cap.
 */
class TorsionFeasibleTrioSearch(
    val backbone: DuplexBackbone = DuplexBackbone(),
    val crossbarBasePairs: Int = 13,
    val separationBasePairs: Int = 7,
    val junctions: List<TrioJunctionSpec> = TrioJunctionSpec.cap(7),
    val azimuthSteps: Int = 120,
    val phaseSteps: Int = 90,
    val axialSteps: Int = 4,
    val lateralSeats: List<Double> = listOf(-0.4, -0.2, 0.0, 0.2, 0.4),
    val contactFloor: Double = 1.6,
    val candidatesPerJunction: Int = 2,
    val gridSteps: Int = 60,
    val refinements: Int = 4
) {

    init {
        require(crossbarBasePairs >= 2) {
            "crossbarBasePairs must be at least two, was: $crossbarBasePairs"
        }
        require(junctions.isNotEmpty()) { "junctions must not be empty" }
        require(azimuthSteps >= 1) { "azimuthSteps must be positive, was: $azimuthSteps" }
        require(phaseSteps >= 1) { "phaseSteps must be positive, was: $phaseSteps" }
        require(axialSteps >= 1) { "axialSteps must be positive, was: $axialSteps" }
        require(lateralSeats.isNotEmpty()) { "lateralSeats must not be empty" }
        require(candidatesPerJunction >= 1) {
            "candidatesPerJunction must be positive, was: $candidatesPerJunction"
        }
    }

    val geometry: CrossbarGeometry =
        CrossbarGeometry(crossbarBasePairs, separationBasePairs, backbone.risePerBasePair)

    /** How many junction solves the search has consumed — a cap is part of the verdict. */
    var solves: Int = 0
        private set

    private val groove = backbone.minorGrooveAngle * PI / 180.0

    private fun phosphates(phase: Double, axial: Double): List<Pair<CrossbarTarget, Vector3>> {
        val out = ArrayList<Pair<CrossbarTarget, Vector3>>(2 * crossbarBasePairs)
        for (index in 0 until crossbarBasePairs) {
            for (strand in 0..1) {
                val angle = phase + index * backbone.twistPerBasePair + strand * groove
                out += CrossbarTarget(strand, index) to Vector3(
                    axial + (index - 0.5 * (crossbarBasePairs - 1)) * backbone.risePerBasePair,
                    backbone.phosphateRadius * cos(angle),
                    backbone.phosphateRadius * sin(angle)
                )
            }
        }
        return out
    }

    private fun termini(
        spec: TrioJunctionSpec,
        lateral: Double,
        azimuth: Double
    ): Pair<Vector3, Vector3> {
        val radius = backbone.phosphateRadius
        return when (spec.kind) {
            TrioJunctionKind.LEG -> {
                val height =
                    loneSeatFaceHeight(lateral, backbone.duplexRadius, backbone.duplexRadius)
                Pair(
                    Vector3(
                        spec.axialOffset + radius * cos(azimuth),
                        lateral + radius * sin(azimuth),
                        -height
                    ),
                    Vector3(
                        spec.axialOffset + radius * cos(azimuth + groove),
                        lateral + radius * sin(azimuth + groove),
                        -height
                    )
                )
            }

            TrioJunctionKind.FLEXURE -> {
                val height = loneSeatFaceHeight(0.0, backbone.duplexRadius, backbone.duplexRadius)
                Pair(
                    Vector3(
                        spec.axialOffset + radius * cos(azimuth), -height, radius * sin(azimuth)
                    ),
                    Vector3(
                        spec.axialOffset + radius * cos(azimuth + groove),
                        -height,
                        radius * sin(azimuth + groove)
                    )
                )
            }
        }
    }

    /** One junction's placement at a stated azimuth, on `C-0052`'s own admissibility test. */
    fun placementAt(
        spec: TrioJunctionSpec,
        lateral: Double,
        azimuth: Double,
        lattice: List<Pair<CrossbarTarget, Vector3>>
    ): TrioPlacement? {
        val (first, second) = termini(spec, lateral, azimuth)
        var firstResidual = Double.MAX_VALUE
        var firstGap = Double.MAX_VALUE
        var firstAt = -1
        var secondResidual = Double.MAX_VALUE
        var secondGap = Double.MAX_VALUE
        var secondAt = -1
        lattice.forEachIndexed { i, (_, p) ->
            val toFirst = (p - first).length
            val toSecond = (p - second).length
            if (toFirst < BForm.PHOSPHATE_HARD_SEPARATION) return null
            if (toSecond < BForm.PHOSPHATE_HARD_SEPARATION) return null
            val residualFirst = linkWindowResidual(toFirst)
            if (residualFirst < firstResidual) {
                firstResidual = residualFirst
                firstGap = toFirst
                firstAt = i
            }
            val residualSecond = linkWindowResidual(toSecond)
            if (residualSecond < secondResidual) {
                secondResidual = residualSecond
                secondGap = toSecond
                secondAt = i
            }
        }
        if (firstAt < 0 || secondAt < 0 || firstAt == secondAt) return null
        val contact = when (spec.kind) {
            TrioJunctionKind.LEG -> boundedSeatContactLength(
                spec.axialOffset, geometry.length, lateral, backbone.duplexRadius
            )

            TrioJunctionKind.FLEXURE -> boundedSeatContactLength(
                spec.axialOffset, geometry.length, 0.0, backbone.duplexRadius
            )
        }
        if (contact < contactFloor) return null
        return TrioPlacement(
            name = spec.name,
            kind = spec.kind,
            axialOffset = spec.axialOffset,
            lateralOffset = if (spec.kind == TrioJunctionKind.LEG) lateral else 0.0,
            azimuth = azimuth,
            chordAzimuth = chordAzimuthOfStandoff(azimuth, backbone),
            wantedChordAzimuth = spec.wantedChordAzimuth,
            firstGap = firstGap,
            secondGap = secondGap,
            firstTarget = lattice[firstAt].first,
            secondTarget = lattice[secondAt].first,
            firstTerminus = first,
            secondTerminus = second,
            seatContact = contact
        )
    }

    private data class Lattice(
        val phase: Double,
        val axial: Double,
        val lateral: Double,
        val candidates: List<List<TrioPlacement>>
    ) {

        /** The best alignment this lattice can offer at its **worst** junction, before any solve. */
        val floor: Double get() = candidates.maxOf { it.first().misalignment }
    }

    /** The best aligned, torsion-closing trio, or a "not found within the budget" verdict. */
    fun best(solveCap: Int = 24): AlignedTrioOutcome {
        require(solveCap >= 0) { "solveCap must not be negative, was: $solveCap" }
        var lattices = 0
        val admissible = ArrayList<Lattice>()
        val axialLimit = backbone.risePerBasePair
        lateralSeats.forEach { lateral ->
            for (p in 0 until phaseSteps) {
                val phase = 2.0 * PI * p / phaseSteps
                for (a in 0 until axialSteps) {
                    val axial = axialLimit * a / axialSteps
                    lattices++
                    val lattice = phosphates(phase, axial)
                    val perJunction = junctions.map { spec ->
                        val covalent = ArrayList<TrioPlacement>()
                        for (i in 0 until azimuthSteps) {
                            val azimuth = i * 2.0 * PI / azimuthSteps
                            val placement = placementAt(spec, lateral, azimuth, lattice) ?: continue
                            if (!placement.covalent) continue
                            covalent += placement
                        }
                        // the reach bound is cheap but not free, so it is spent in ALIGNMENT order
                        // and only until the per-junction quota is filled
                        val out = ArrayList<TrioPlacement>(candidatesPerJunction)
                        covalent.sortedWith(compareBy({ it.misalignment }, { it.azimuth }))
                            .forEach { placement ->
                                if (out.size >= candidatesPerJunction) return@forEach
                                val links = junctionLinks(
                                    backbone, crossbarBasePairs, phase, axial, placement
                                )
                                if (reachVerdict(links).feasible) out += placement
                            }
                        out
                    }
                    if (perJunction.any { it.isEmpty() }) continue
                    admissible += Lattice(phase, axial, lateral, perJunction)
                }
            }
        }
        val ranked = admissible.sortedWith(
            compareBy({ it.floor }, { it.lateral }, { it.phase }, { it.axial })
        ).take(solveCap)
        val bestFeasibleFloor = admissible.minOfOrNull { it.floor } ?: PI
        ranked.forEach { lattice ->
            val chosen = lattice.candidates.map { candidates ->
                candidates.firstOrNull { placement ->
                    solves++
                    torsionVerdict(
                        junctionLinks(
                            backbone, crossbarBasePairs, lattice.phase, lattice.axial, placement
                        ),
                        gridSteps = gridSteps, refinements = refinements
                    ).closes
                }
            }
            if (chosen.any { it == null }) return@forEach
            val placements = chosen.filterNotNull()
            val closure = CrossbarTrioClosure(
                placements = placements,
                crossbarBasePairs = crossbarBasePairs,
                separationBasePairs = separationBasePairs,
                helicalPhase = lattice.phase,
                axialPhase = lattice.axial,
                lateralOffset = lattice.lateral,
                legFlexureClearance = geometry.legFlexureClearance,
                minimumSeatContact = placements.minOf { it.seatContact }
            )
            if (!closure.distinctTargets || !closure.terminiClear) return@forEach
            val verdicts = placements.map {
                torsionVerdict(
                    junctionLinks(
                        backbone, crossbarBasePairs, lattice.phase, lattice.axial, it
                    ),
                    gridSteps = gridSteps, refinements = refinements
                )
            }
            val legs = placements.filter { it.kind == TrioJunctionKind.LEG }
            val flexure = placements.firstOrNull { it.kind == TrioJunctionKind.FLEXURE }
            return AlignedTrioOutcome(
                crossbarBasePairs = crossbarBasePairs,
                separationBasePairs = separationBasePairs,
                lattices = lattices,
                feasibleLattices = admissible.size,
                solvedLattices = ranked.size,
                bestFeasibleMisalignmentDegrees = bestFeasibleFloor * 180.0 / PI,
                closes = true,
                worstMisalignmentDegrees = closure.worstMisalignment * 180.0 / PI,
                legMisalignmentDegrees =
                    (legs.maxOfOrNull { it.misalignment } ?: 0.0) * 180.0 / PI,
                flexureMisalignmentDegrees = (flexure?.misalignment ?: 0.0) * 180.0 / PI,
                worstGap = closure.worstGap,
                worstCovalentZ = verdicts.maxOf { it.worstCovalentZ },
                minimumOccupancy = verdicts.minOf { it.minimumOccupancy },
                helicalPhaseDegrees = lattice.phase * 180.0 / PI,
                axialPhase = lattice.axial,
                lateralSeat = lattice.lateral,
                verdict = "AN ALIGNED TORSION-CLOSING TRIO EXISTS — worst chord " +
                        "%.1f° off the direction its own body wants".format(
                            closure.worstMisalignment * 180.0 / PI
                        )
            )
        }
        return AlignedTrioOutcome(
            crossbarBasePairs = crossbarBasePairs,
            separationBasePairs = separationBasePairs,
            lattices = lattices,
            feasibleLattices = admissible.size,
            solvedLattices = ranked.size,
            bestFeasibleMisalignmentDegrees = bestFeasibleFloor * 180.0 / PI,
            closes = false,
            worstMisalignmentDegrees = 0.0,
            legMisalignmentDegrees = 0.0,
            flexureMisalignmentDegrees = 0.0,
            worstGap = 0.0,
            worstCovalentZ = 0.0,
            minimumOccupancy = 0.0,
            helicalPhaseDegrees = 0.0,
            axialPhase = 0.0,
            lateralSeat = 0.0,
            verdict = if (admissible.isEmpty()) {
                "NO lattice admits a reach-feasible placement of all ${junctions.size} junctions"
            } else {
                "NO torsion-closing trio in the ${ranked.size} best-aligned of " +
                        "${admissible.size} reach-feasible lattices"
            }
        )
    }
}

// ------------------------------------------------- the mechanics at the alignment feasibility gives

/**
 * The truss design at the misalignments the **feasible** set delivers, with `C-0052`'s
 * leg-is-one-body constraint imposed.
 *
 * @property baseFloorDegrees the smallest base misalignment a torsion-closing placement on the
 *   sheet offers.
 * @property capFloorDegrees the same on the crossbar.
 * @property budgetDegrees `chordPairMisalignment(m)` — what the leg's own length costs, which no
 *   rotation can reduce, and which the two ends must share.
 */
data class FeasibleTrussDesign(
    val legSteps: Int,
    val legLength: Double,
    val separationBasePairs: Int,
    val baseFloorDegrees: Double,
    val capFloorDegrees: Double,
    val budgetDegrees: Double,
    val baseMisalignmentDegrees: Double,
    val capMisalignmentDegrees: Double,
    val flexureMisalignmentDegrees: Double,
    val loadedCoupleFraction: Double,
    val frameCouple: Double,
    val capBending: Double,
    val capTorsion: Double,
    val duty: Double,
    val criticalLoadCanDo: Double,
    val criticalLoadFields: Double,
    val marginCanDo: Double,
    val marginFields: Double,
    val governingPlane: String,
    val span: Double,
    val tangent: Double,
    val representable: Boolean,
    val verdict: String
)

/**
 * `C-0048`'s design at a leg of [legSteps] steps, with the base and cap misalignments **floored**
 * by what the torsion-feasible set offers and their **sum** floored by the leg's own quantised
 * budget.
 *
 * The composition is deliberately **conservative**: it takes the two feasibility floors and the
 * quantisation budget as independent constraints, which bounds the achievable design from the
 * favourable side. Saying anything sharper would need a joint search over the sheet's lattice, the
 * crossbar's continuous phase and the leg's length at once, and that is named as an open item
 * rather than asserted.
 */
fun feasibleTrussDesign(
    legSteps: Int,
    baseFloor: Double,
    capFloor: Double,
    flexureFloor: Double = 0.0,
    separationBasePairs: Int = 7,
    rotationSteps: Int = 24,
    backbone: DuplexBackbone = DuplexBackbone(),
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): FeasibleTrussDesign {
    require(legSteps >= 1) { "legSteps must be positive, was: $legSteps" }
    require(baseFloor >= 0.0 && capFloor >= 0.0 && flexureFloor >= 0.0) {
        "a misalignment floor must not be negative"
    }
    require(rotationSteps >= 1) { "rotationSteps must be positive, was: $rotationSteps" }
    val legLength = legSteps * rise
    val budget = chordPairMisalignment(legSteps, 0.5 * PI, backbone)
    var best: CapDesignPoint? = null
    var bestBase = 0.0
    var bestCap = 0.0
    var representable = false
    for (i in 0..rotationSteps) {
        val rotation = i * 0.5 * PI / rotationSteps
        val split = legAzimuthSplit(legSteps, rotation, backbone)
        val base = max(split.baseMisalignment, baseFloor)
        val cap = max(split.capMisalignment, capFloor)
        if (base > 0.25 * PI) continue
        representable = true
        val design = capDesign(
            legLength = legLength,
            separationBasePairs = separationBasePairs,
            baseMisalignment = base,
            capMisalignment = cap,
            flexureMisalignment = flexureFloor,
            legSteps = legSteps,
            legRotation = rotation
        )
        val incumbent = best
        if (incumbent == null || design.criticalLoad > incumbent.criticalLoad) {
            best = design
            bestBase = base
            bestCap = cap
        }
    }
    val found = best
    if (found == null) {
        return FeasibleTrussDesign(
            legSteps = legSteps,
            legLength = legLength,
            separationBasePairs = separationBasePairs,
            baseFloorDegrees = baseFloor * 180.0 / PI,
            capFloorDegrees = capFloor * 180.0 / PI,
            budgetDegrees = budget * 180.0 / PI,
            baseMisalignmentDegrees = baseFloor * 180.0 / PI,
            capMisalignmentDegrees = capFloor * 180.0 / PI,
            flexureMisalignmentDegrees = flexureFloor * 180.0 / PI,
            loadedCoupleFraction = couplePhaseProjection(baseFloor),
            frameCouple = 0.0,
            capBending = 0.0,
            capTorsion = 0.0,
            duty = 0.0,
            criticalLoadCanDo = 0.0,
            criticalLoadFields = 0.0,
            marginCanDo = 0.0,
            marginFields = 0.0,
            governingPlane = "none",
            span = 0.0,
            tangent = 0.0,
            representable = false,
            verdict = "NOT REPRESENTABLE — the feasible base misalignment exceeds a half right " +
                    "angle, past which C-0037's TwoLinkBase invariant cannot represent the base"
        )
    }
    return FeasibleTrussDesign(
        legSteps = legSteps,
        legLength = legLength,
        separationBasePairs = separationBasePairs,
        baseFloorDegrees = baseFloor * 180.0 / PI,
        capFloorDegrees = capFloor * 180.0 / PI,
        budgetDegrees = budget * 180.0 / PI,
        baseMisalignmentDegrees = bestBase * 180.0 / PI,
        capMisalignmentDegrees = bestCap * 180.0 / PI,
        flexureMisalignmentDegrees = flexureFloor * 180.0 / PI,
        loadedCoupleFraction = couplePhaseProjection(bestBase),
        frameCouple = found.frameCouple,
        capBending = capBendingStiffness(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, separationBasePairs * rise, 12.0
        ),
        capTorsion = capTorsionalStiffness(
            Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, separationBasePairs * rise
        ),
        duty = found.duty,
        criticalLoadCanDo = found.criticalLoad,
        criticalLoadFields = found.criticalLoadFields,
        marginCanDo = found.marginCanDo,
        marginFields = found.marginFields,
        governingPlane = found.governingPlane,
        span = found.span,
        tangent = found.tangent,
        representable = representable,
        verdict = found.verdict
    )
}

/** The unavoidable misalignment budget of a leg [legSteps] base-pair steps long, in degrees. */
fun legBudgetDegrees(
    legSteps: Int,
    backbone: DuplexBackbone = DuplexBackbone()
): Double = chordPairMisalignment(legSteps, 0.5 * PI, backbone) * 180.0 / PI
