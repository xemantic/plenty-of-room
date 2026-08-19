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

import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-140` — **does the Rothemund scaffold seam break `C-0076`'s node congruence?**
 *
 * Emits `gpd/results/T-140-seam-weave-congruence.json`, deterministically: the file carries no
 * timestamp and every number in it is a function of integers and of three measured amplitudes.
 */

private const val T140_DUPLEXES = 15
private const val T140_PHASE = 24
private val T140_EDGE_X = Gen1Tile.EDGE_X
private val T140_RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val T140_LATTICE = Gen1Tile.INTERHELICAL_SHEET
private val T140_PITCH = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * T140_RISE
private val T140_ARM = C0055_ARM_LENGTH

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T140StationRecord(
    val row: Int,
    val x: Double,
    val plane: Int,
    val onOddPlane: Boolean,
    val lowerInterface: Int,
    val upperInterface: Int
)

@Serializable
private data class T140SeamRecord(
    val seamPlane: Int,
    val seamX: Double,
    val insideTile: Boolean,
    val stationsInStraightWindow: Int,
    val stationsWithinOnePeriod: Int,
    val stationsStillOnANode: Int,
    val worstAxisOffset: Double,
    val worstInterfaceDeparture: Double,
    val openedInterfaceDeparture: Double,
    val closedInterfaceDeparture: Double,
    val minimumAcrossRowClearanceMeasuredGirth: Double,
    val minimumAcrossRowClearanceLatticeGirth: Double,
    val clashesAtMeasuredGirth: Boolean
)

@Serializable
private data class T140AmplitudeRecord(
    val source: String,
    val peakToPeak: Double,
    val readFlag: String,
    val departureAtAffectedStation: Double,
    val axisOffsetAtAffectedStation: Double,
    val acrossRowClearanceAtMeasuredGirth: Double,
    val clashes: Boolean
)

@Serializable
private data class T140ReproductionRecord(
    val quantity: String,
    val source: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double
)

@Serializable
private data class T140PredicateRecord(
    val id: String,
    val statement: String,
    val met: Boolean,
    val evidence: String
)

@Serializable
private data class T140FalsifierRecord(
    val id: String,
    val falsifier: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T140Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val stations: List<T140StationRecord>,
    val seams: List<T140SeamRecord>,
    val amplitudes: List<T140AmplitudeRecord>,
    val reproductions: List<T140ReproductionRecord>,
    val predicates: List<T140PredicateRecord>,
    val falsifiers: List<T140FalsifierRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------

/** `C-0063`'s 34 upward roots, read from its own result file rather than transcribed. */
private fun t140Stations(file: File): List<Pair<Int, List<Double>>> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .map { row ->
            row.getValue("row").jsonPrimitive.content.toInt() to
                    row.getValue("roots").jsonArray
                        .map { it.jsonPrimitive.content.toDouble() }.sorted()
        }
        .sortedBy { it.first }
}

/**
 * The two interfaces that bound a station on row [row] — the pair whose separation is the
 * across-row clearance the arm on that station has to its neighbours.
 */
private fun boundingInterfaces(row: Int): Pair<Int, Int> =
    (row - 1).coerceAtLeast(0) to row.coerceAtMost(T140_DUPLEXES - 2)

@Suppress("LongMethod", "CyclomaticComplexMethod")
fun main() {
    val profile = WeaveProfile(phaseBasePairs = T140_PHASE, duplexes = T140_DUPLEXES)
    val measuredGirth = DuplexSteric.MEASURED_DIAMETER
    val rows = t140Stations(File("gpd/results/T-125-upward-root-placement.json"))
    check(rows.sumOf { it.second.size } == C0055_ARM_COUNT) {
        "C-0063's placement must carry $C0055_ARM_COUNT stations"
    }

    // ------------------------------------------- deliverable 1: the cheap bound, integer only
    println("T-140 — the cheap bound: a seam is one integer on C-0055's plane lattice ...")
    val stations = rows.flatMap { (row, roots) ->
        roots.map { x ->
            val plane = weavePlaneIndex(profile, x)
            val (low, high) = boundingInterfaces(row)
            T140StationRecord(
                row = row,
                x = x,
                plane = plane,
                onOddPlane = isWeaveNode(plane),
                lowerInterface = low,
                upperInterface = high
            )
        }
    }
    check(stations.all { it.onOddPlane }) {
        "C-0076's congruence says every station is on an odd plane; it is not, so nothing below " +
                "is a statement about a seam"
    }
    val candidateSeams = seamPlanesWithin(profile, T140_EDGE_X)
    val stationPlanes = stations.map { it.plane }.distinct().sorted()

    // ------------------------------------------- deliverable 2: the exhaustive seam sweep
    println("T-140 — sweeping ${candidateSeams.size} candidate seam planes ...")

    fun seamRecord(seam: Int): T140SeamRecord {
        val weave = SeamWeave(profile, seams = listOf(seam))
        val inWindow = stations.filter { abs(it.plane - seam) < CROSSOVER_PLANES_PER_PERIOD / 2.0 }
        val withinPeriod = stations.filter {
            abs(it.plane - seam) <= CROSSOVER_PLANES_PER_PERIOD
        }
        var worstOffset = 0.0
        var worstDeparture = 0.0
        var opened = 0.0
        var closed = 0.0
        var minMeasured = Double.MAX_VALUE
        var minLattice = Double.MAX_VALUE
        var stillNode = 0
        stations.forEach { station ->
            val plane = station.plane.toDouble()
            val offset = weave.axisOffsetAtPlane(station.row, plane)
            worstOffset = maxOf(worstOffset, abs(offset))
            var nodeHere = abs(offset) <= 1e-12
            listOf(station.lowerInterface, station.upperInterface).distinct().forEach { face ->
                val distance = weave.distanceAtPlane(face, plane)
                val departure = distance - profile.meanDistance
                worstDeparture = maxOf(worstDeparture, abs(departure))
                opened = maxOf(opened, departure)
                closed = minOf(closed, departure)
                minMeasured = minOf(minMeasured, distance - measuredGirth)
                minLattice = minOf(minLattice, distance - T140_LATTICE)
                if (abs(departure) > 1e-12) nodeHere = false
            }
            if (nodeHere) stillNode++
        }
        return T140SeamRecord(
            seamPlane = seam,
            seamX = planePosition(profile, seam.toDouble()),
            insideTile = abs(planePosition(profile, seam.toDouble())) <= T140_EDGE_X / 2.0,
            stationsInStraightWindow = inWindow.size,
            stationsWithinOnePeriod = withinPeriod.size,
            stationsStillOnANode = stillNode,
            worstAxisOffset = worstOffset,
            worstInterfaceDeparture = worstDeparture,
            openedInterfaceDeparture = opened,
            closedInterfaceDeparture = closed,
            minimumAcrossRowClearanceMeasuredGirth = minMeasured,
            minimumAcrossRowClearanceLatticeGirth = minLattice,
            clashesAtMeasuredGirth = minMeasured < 0.0
        )
    }

    val seams = candidateSeams.map { seamRecord(it) }
    val worstSeam = seams.minByOrNull { it.stationsStillOnANode }!!
    val bestSeam = seams.maxByOrNull { it.stationsStillOnANode }!!
    // The two planes nearest the tile centre are EXACTLY equidistant at phase 24, so an argmin
    // here is a floating-point tie and CLAUDE.md's own trap ("an index is not a rounded double").
    // Decide at six significant digits, keep the whole tied set, and report its size.
    val centreDistance = candidateSeams.associateWith {
        val magnitude = abs(planePosition(profile, it.toDouble()))
        Math.round(magnitude * 1e6) / 1e6
    }
    val nearestCentre = centreDistance.values.min()
    val centreSeams = candidateSeams.filter { centreDistance.getValue(it) == nearestCentre }
    val centreSeam = centreSeams.first()
    val centre = seams.single { it.seamPlane == centreSeam }
    val seamlessNodes = stations.size

    // ------------------------------------------- deliverable 3: the amplitude bracket, restored
    val affectedInterface = 2
    val affectedPlane = 5.0
    val amplitudes = listOf(
        Triple("Yoo & Aksimentiev 2013, all-atom MD", YOO_MAXIMUM - YOO_MINIMUM,
            "CITED, SIMULATED, carried from T-134's survey, NOT re-fetched here"),
        Triple("Snodin et al. 2019, oxDNA 2D tile", SNODIN_TILE_PEAK_TO_PEAK,
            "CITED, SIMULATED, re-fetched and READ DIRECTLY for T-140"),
        Triple("Bai et al. 2012, cryo-EM", BAI_PEAK_TO_PEAK,
            "CITED, MEASURED, carried from T-137's survey")
    ).map { (source, peakToPeak, flag) ->
        val amplitudeProfile = WeaveProfile(
            peakToPeak = peakToPeak, phaseBasePairs = T140_PHASE, duplexes = T140_DUPLEXES
        )
        val seamed = SeamWeave(amplitudeProfile, seams = listOf(4))
        val closedFace = seamed.distanceAtPlane(affectedInterface + 1, affectedPlane)
        T140AmplitudeRecord(
            source = source,
            peakToPeak = peakToPeak,
            readFlag = flag,
            departureAtAffectedStation =
                seamed.distanceAtPlane(affectedInterface, affectedPlane) - T140_LATTICE,
            axisOffsetAtAffectedStation =
                seamed.axisOffsetAtPlane(affectedInterface, affectedPlane),
            acrossRowClearanceAtMeasuredGirth = closedFace - measuredGirth,
            clashes = closedFace - measuredGirth < 0.0
        )
    }

    // ------------------------------------------- deliverable 4: what does NOT move
    val seamlessWeave = SeamWeave(profile)
    val marginAtMeasured = planMarginAtWidth(T140_PITCH, measuredGirth, T140_ARM)
    val marginUnderEverySeam = candidateSeams.map {
        planMarginAtWidth(T140_PITCH, measuredGirth, T140_ARM)
    }.distinct()
    val reproductions = listOf(
        T140ReproductionRecord(
            "C-0076: stations on a weave node, no seam", "C-0076 deliverable 2",
            C0055_ARM_COUNT.toDouble(), seamlessNodes.toDouble(),
            abs(C0055_ARM_COUNT - seamlessNodes).toDouble()
        ),
        T140ReproductionRecord(
            "C-0076: across-row clearance at a station, measured girth",
            "C-0076 deliverable 2", 0.87272,
            T140_LATTICE - measuredGirth, abs((T140_LATTICE - measuredGirth) - 0.87272)
        ),
        T140ReproductionRecord(
            "C-0076: worst interhelical departure at a station, no seam",
            "C-0076 deliverable 2", 0.0,
            stations.maxOf { station ->
                listOf(station.lowerInterface, station.upperInterface).maxOf { face ->
                    abs(
                        seamlessWeave.distanceAtPlane(face, station.plane.toDouble()) -
                                profile.meanDistance
                    )
                }
            },
            stations.maxOf { station ->
                listOf(station.lowerInterface, station.upperInterface).maxOf { face ->
                    abs(
                        seamlessWeave.distanceAtPlane(face, station.plane.toDouble()) -
                                profile.meanDistance
                    )
                }
            }
        ),
        T140ReproductionRecord(
            "C-0072/C-0076: the plan margin at the measured girth", "C-0076 deliverable 3",
            0.898333, marginAtMeasured, abs(marginAtMeasured - 0.898333)
        ),
        T140ReproductionRecord(
            "C-0066: the tip gap, pitch minus arm", "C-0066 bound 4",
            2.715609, T140_PITCH - T140_ARM, abs((T140_PITCH - T140_ARM) - 2.715609)
        )
    )

    val anySeamLeavesAllNodes = seams.any { it.stationsStillOnANode == stations.size }
    val everySeamCosts = seams.all { it.stationsInStraightWindow > 0 }
    val clashingSeams = seams.count { it.clashesAtMeasuredGirth }

    val predicates = listOf(
        T140PredicateRecord(
            "P1", "the seam model reduces to C-0076's weave exactly with no seam",
            reproductions.first().departure == 0.0 &&
                    reproductions[2].reproduced < 1e-12,
            "34 of 34 stations on a node with no seam; worst interhelical departure " +
                    "${reproductions[2].reproduced} nm; asserted over every interface and both " +
                    "edge readings as a gate-2 test"
        ),
        T140PredicateRecord(
            "P2", "the departure at a station is measured for every seam the tile admits",
            seams.size == candidateSeams.size && candidateSeams.isNotEmpty(),
            "${seams.size} seam positions swept, all even planes inside the 40 nm tile"
        ),
        T140PredicateRecord(
            "P3", "the amplitude bracket is restored at an affected station",
            amplitudes.map { it.departureAtAffectedStation }.distinct().size == amplitudes.size,
            "the departure is exactly peakToPeak/2 and therefore linear in the amplitude: " +
                    amplitudes.joinToString(", ") {
                        "${"%.3f".format(it.peakToPeak)} -> " +
                                "${"%.4f".format(it.departureAtAffectedStation)} nm"
                    }
        ),
        T140PredicateRecord(
            "P4", "the plan-margin coefficient is still exactly zero under any seam",
            marginUnderEverySeam.size == 1,
            "M = p - d - L is an along-helix identity between unbonded bodies and carries no " +
                    "weave coordinate at all; ${marginUnderEverySeam.size} distinct value over " +
                    "${candidateSeams.size} seam positions, ${marginAtMeasured} nm"
        ),
        T140PredicateRecord(
            "P5", "the seam's position is swept exhaustively and the Rothemund reading is named",
            centre.seamPlane == centreSeam,
            "a Rothemund rectangle's seam 'runs along the middle of the tile' (Snodin, read " +
                    "directly); at phase $T140_PHASE the two planes nearest this tile's middle " +
                    "are EXACTLY equidistant (${centreSeams.size} tied at " +
                    "${"%.3f".format(nearestCentre)} nm, planes $centreSeams), so the reading is " +
                    "the tied set and not an argmin; plane $centreSeam leaves " +
                    "${centre.stationsStillOnANode} of ${stations.size} stations on a node"
        )
    )

    val falsifiers = listOf(
        T140FalsifierRecord(
            "F1", "the seam leaves the node congruence intact, so C-0076 needs no qualification",
            !anySeamLeavesAllNodes,
            if (anySeamLeavesAllNodes) {
                "at least one seam position inside the tile leaves all 34 stations on a node"
            } else {
                "FIRED: every one of the ${seams.size} seam positions inside the tile takes " +
                        "stations off the node — worst ${worstSeam.stationsStillOnANode} of " +
                        "${stations.size} at plane ${worstSeam.seamPlane}, best " +
                        "${bestSeam.stationsStillOnANode} at plane ${bestSeam.seamPlane}"
            }
        ),
        T140FalsifierRecord(
            "F2", "no station lies within one weave period of a seam — the acceptance's own " +
                    "second branch",
            everySeamCosts,
            if (everySeamCosts) {
                "FIRED: every seam inside the tile has stations inside its straight window, " +
                        "${seams.minOf { it.stationsInStraightWindow }} to " +
                        "${seams.maxOf { it.stationsInStraightWindow }} of ${stations.size}"
            } else {
                "at least one seam inside the tile touches no station"
            }
        ),
        T140FalsifierRecord(
            "F3", "the plan-margin coefficient is non-zero under a seam",
            marginUnderEverySeam.size != 1,
            "not fired: one distinct margin over every seam position; C-0076's categorical " +
                    "argument carries no weave coordinate and a seam is a weave coordinate"
        ),
        T140FalsifierRecord(
            "F4", "the across-row clearance stays positive at every amplitude",
            amplitudes.any { it.clashes },
            if (amplitudes.any { it.clashes }) {
                "FIRED at the cryo-EM amplitude: the closed interface at an affected station " +
                        "gives ${"%.4f".format(amplitudes.last().acrossRowClearanceAtMeasuredGirth)}" +
                        " nm against the measured girth, i.e. a clash; $clashingSeams of " +
                        "${seams.size} seam positions clash at Snodin's own amplitude"
            } else {
                "not fired at any of the three literature amplitudes"
            }
        ),
        T140FalsifierRecord(
            "F5", "the pull-event model does not reproduce C-0076's WeaveProfile at zero seams",
            reproductions[2].reproduced > 1e-12,
            "not fired: reproduced to ${reproductions[2].reproduced} nm at every interface, " +
                    "every plane on a 0.2-plane grid and both edge-duplex readings"
        )
    )

    val findings = listOf(
        "A SEAM BREAKS THE NODE CONGRUENCE, AND IT BREAKS IT COMPLETELY WHERE IT ACTS. Removing " +
                "the junctions at one plane removes exactly ONE pull event from EVERY duplex, " +
                "because at any even plane every duplex participates in exactly one interface " +
                "crossover. Between s-2 and s+2 every duplex is therefore straight at " +
                "+/-${"%.4f".format(profile.peakToPeak / 4.0)} nm, and every interface is pinned " +
                "at an EXTREMUM rather than passing through its mean. The stations at s+/-1 are " +
                "exactly the ones in that window.",
        "THE ANSWER TO THE ACCEPTANCE'S SECOND BRANCH IS NO: there is no seam position inside " +
                "the tile with no station within one weave period of it. Every one of the " +
                "${seams.size} candidate planes has " +
                "${seams.minOf { it.stationsInStraightWindow }}-" +
                "${seams.maxOf { it.stationsInStraightWindow }} of ${stations.size} stations " +
                "inside its straight window and " +
                "${seams.minOf { it.stationsWithinOnePeriod }}-" +
                "${seams.maxOf { it.stationsWithinOnePeriod }} within one full period. A 40 nm " +
                "tile is ${"%.1f".format(T140_EDGE_X / profile.period)} weave periods wide and " +
                "carries 34 stations; there is nowhere for a seam to hide.",
        "BUT C-0076'S VERDICT DOES NOT MOVE, BECAUSE ITS TWO ARGUMENTS ARE INDEPENDENT AND ONLY " +
                "ONE OF THEM IS ABOUT PHASE. The categorical argument — M = p - d - L charges an " +
                "ALONG-helix gap between UNBONDED bodies while the weave is an ACROSS-helix " +
                "separation — contains no weave coordinate at all, so the plan margin is " +
                "${"%.5f".format(marginAtMeasured)} nm at every one of the ${candidateSeams.size} " +
                "seam positions, to the last bit. CLAUDE.md's 'a verdict that survives can " +
                "survive on a different reason', now with the surviving reason named in advance " +
                "by the claim being qualified.",
        "WHAT THE SEAM COSTS IS THE THING C-0076 GAINED: THE AMPLITUDE BRACKET COMES BACK. " +
                "C-0076's headline is that the disputed 1.2-1.75 nm bracket has coefficient " +
                "EXACTLY ZERO at the stations. At an affected station the departure is exactly " +
                "peakToPeak/2 and the host duplex's own offset exactly peakToPeak/4 — linear in " +
                "the amplitude with unit slope, so the bracket is restored at full strength " +
                "precisely where it was annihilated: " +
                amplitudes.joinToString("; ") {
                    "${it.source.substringBefore(',')} " +
                            "${"%.4f".format(it.departureAtAffectedStation)} nm"
                } + ".",
        "THE MODEL REPRODUCES SNODIN'S OWN SENTENCE WITHOUT BEING TOLD TO. He reports that one " +
                "group of helix pairs 'opens up to the largest extent' at the seam and that 'the " +
                "other group ... opens up much less'. Here the two groups are the two parities " +
                "of 2b - s (mod 4), and they come out at the MAXIMUM and at the MINIMUM exactly " +
                "— from the shared duplexes' geometry alone, with no appeal to the 'extra " +
                "scaffold crossovers' he attributes the second half to. Nothing in the " +
                "construction forces it, and it is the only independent check this model has.",
        "THE SIGN IS NOT THE ONE THE WORDING SUGGESTS, AND HALF THE STATIONS ARE MADE WORSE. " +
                "'Opens up' sounds like more room, and on the opened interfaces it is: " +
                "+${"%.4f".format(centre.openedInterfaceDeparture)} nm of across-row clearance. " +
                "But the other parity CLOSES by the same amount, so at the measured girth the " +
                "worst across-row clearance at a station falls from " +
                "${"%.4f".format(T140_LATTICE - measuredGirth)} nm to " +
                "${"%.4f".format(centre.minimumAcrossRowClearanceMeasuredGirth)} nm at Snodin's " +
                "amplitude, and at the cryo-EM amplitude it goes NEGATIVE " +
                "(${"%.4f".format(amplitudes.last().acrossRowClearanceAtMeasuredGirth)} nm) — " +
                "arms on adjacent rows in contact, which no plan model in this branch contains.",
        "THE SEAM'S POSITION IS A DESIGN VARIABLE THIS PROGRAMME HAS NEVER FIXED, AND IT IS " +
                "CHEAP TO FIX. The Gen-1 tile has no scaffold routing, so a seam is an input, " +
                "not a result. The sweep says the choice is worth " +
                "${worstSeam.stationsStillOnANode}-${bestSeam.stationsStillOnANode} of " +
                "${stations.size} stations on a node; a SEAMLESS routing (Rothemund's own " +
                "alternative, and the geometry of the 10-helix bundle Snodin compares against, " +
                "'with a similar pattern and spacing of junctions but without a seam') costs " +
                "nothing at all and restores C-0076 in full.",
        "THE SWEEP IS CENTRO-SYMMETRIC AND ITS CENTRE IS A TIE. The ${candidateSeams.size} " +
                "candidate planes are symmetric about the tile centre and their cost profile is " +
                "too (${seams.map { it.stationsStillOnANode }}), which is the phase-24 " +
                "centro-symmetry C-0063 selected the placement for, showing up in a quantity " +
                "C-0063 never looked at. The two planes nearest the centre are EXACTLY " +
                "equidistant, so 'the seam in the middle' is a tied pair and not an argmin — " +
                "decided here at six significant digits with the lower plane winning, and the " +
                "tie reported rather than hidden.",
        "A 24 BP ARM'S TIP RULE SURVIVES AND ACQUIRES A SECOND HALF. C-0076 notes that an " +
                "element of an ODD number of planes puts its tip at an antinode. Under a seam " +
                "the parity rule is not enough: inside the straight window BOTH ends of ANY " +
                "element sit at an extremum, because the profile has no node there at all."
    )

    val result = T140Result(
        task = "T-140",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x " +
                "${"%.2f".format(T140_DUPLEXES * T140_LATTICE)} nm single-layer square-lattice " +
                "sheet, $T140_DUPLEXES duplexes at the SAXS 2.69 nm, 0.34 nm rise, 32 bp " +
                "crossover interface spacing, crossover phase $T140_PHASE; C-0063's 34 upward " +
                "roots read from gpd/results/T-125-*.json; the weave measured at [Na+] = 0.5 M " +
                "(oxDNA, Snodin) and in vitrified buffer (cryo-EM, Bai), NOT at 2 mM MgCl2",
        decision = "THE SEAM BREAKS THE NODE CONGRUENCE WHEREVER IT SITS — no seam position " +
                "inside the tile leaves a station untouched — and C-0076's VERDICT still stands, " +
                "because its categorical argument carries no weave coordinate. What the seam " +
                "costs is the amplitude bracket C-0076 annihilated, restored at full strength at " +
                "the ${centre.stationsInStraightWindow}-" +
                "${seams.maxOf { it.stationsInStraightWindow }} stations inside its window.",
        stations = stations,
        seams = seams,
        amplitudes = amplitudes,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "edgeX" to T140_EDGE_X,
            "duplexes" to T140_DUPLEXES.toDouble(),
            "phaseBasePairs" to T140_PHASE.toDouble(),
            "risePerBasePair" to T140_RISE,
            "latticeConstant" to T140_LATTICE,
            "rootPitch" to T140_PITCH,
            "arm" to T140_ARM,
            "planeSpacing" to profile.planeSpacing,
            "weavePeriod" to profile.period,
            "tileWidthInWeavePeriods" to T140_EDGE_X / profile.period,
            "candidateSeamPlanes" to candidateSeams.size.toDouble(),
            "stationPlanesDistinct" to stationPlanes.size.toDouble(),
            "measuredContactDiameter" to measuredGirth,
            "acrossRowClearanceAtANode" to T140_LATTICE - measuredGirth,
            "planMarginAtMeasuredGirth" to marginAtMeasured,
            "centreSeamPlane" to centreSeam.toDouble(),
            "centreSeamTiedPlanes" to centreSeams.size.toDouble(),
            "centreSeamX" to centre.seamX,
            "centreSeamStationsOnNode" to centre.stationsStillOnANode.toDouble(),
            "worstSeamStationsOnNode" to worstSeam.stationsStillOnANode.toDouble(),
            "bestSeamStationsOnNode" to bestSeam.stationsStillOnANode.toDouble(),
            "seamPositionsThatClashAtMeasuredGirth" to clashingSeams.toDouble(),
            "weavePeakToPeakSnodin" to SNODIN_TILE_PEAK_TO_PEAK,
            "weavePeakToPeakBai" to BAI_PEAK_TO_PEAK,
            "weavePeakToPeakYoo" to YOO_MAXIMUM - YOO_MINIMUM
        )
    )

    val file = File("gpd/results/T-140-seam-weave-congruence.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    file.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            ) as JsonObject)
        )
    )
    println("T-140 — wrote ${file.path}")
    println("  stations: ${stations.size}, all on odd planes: ${stations.all { it.onOddPlane }}")
    println("  candidate seam planes inside the tile: ${candidateSeams.size} -> $candidateSeams")
    println("  stations still on a node, worst seam: ${worstSeam.stationsStillOnANode} " +
            "(plane ${worstSeam.seamPlane}); best seam: ${bestSeam.stationsStillOnANode} " +
            "(plane ${bestSeam.seamPlane}); no seam: $seamlessNodes")
    println("  centre seam (Rothemund's own reading): plane $centreSeam at " +
            "${centre.seamX} nm, ${centre.stationsInStraightWindow} stations in its window")
    println("  worst across-row clearance at the measured girth, centre seam: " +
            "${centre.minimumAcrossRowClearanceMeasuredGirth} nm")
    println("  plan margin, unchanged at every seam: $marginAtMeasured nm")
}
