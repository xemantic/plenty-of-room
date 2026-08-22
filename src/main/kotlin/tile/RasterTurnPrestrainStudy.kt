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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.relativeDeparture
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
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
 * `T-254` — does a raster TURN sit on the flatness axis at all?
 *
 * Emits `gpd/results/T-254-raster-turn-prestrain.json`. Reads
 * `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved collar),
 * `gpd/results/T-246-forced-scaffold-crossover-price.json` (`C-0152`'s departures and its
 * closure census) and `gpd/results/T-253-honeycomb-grillage.json` (`C-0154`'s free tile).
 */

private const val T254_RIM_STANDOFF: Double = 1.0
private const val T254_SAMPLES: Int = 81
private const val T254_TOLERANCE: Double = 0.10
private const val T254_FORCED_CROSSOVERS: Int = 10
private const val T254_ROW_BP: Int = 116

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

private class T254Collar(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, lengthS: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, lengthS, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T254_RIM_STANDOFF))
        )
}

private fun readCollar(file: File): T254Collar {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T254Collar(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

@Serializable
private class T254CensusRow(
    val crossSection: String,
    val helices: Int,
    val turns: Int,
    val throughThicknessTurns: Int,
    val inPlaneTurns: Int,
    val turnsAtHighRim: Int,
    val turnsAtLowRim: Int,
    val latticeBonds: Int,
    val interfacesCarryingATurn: Int,
    val interlayerInterfaces: Int,
    val inPlaneInterfaces: Int,
    val turnsBetweenTwoGapFacingHelices: Int,
    val leastAbsoluteInPlaneArm: Double,
    val anyTieHasAZeroInPlaneArm: Boolean
)

@Serializable
private class T254TurnRow(
    val index: Int,
    val lowerBeam: Int,
    val upperBeam: Int,
    val inPlane: Boolean,
    val atHighEnd: Boolean,
    val touchesTheGapFacingFace: Boolean,
    val unitY: Double,
    val unitZ: Double,
    val peakDishingPerRadian: Double,
    val atTheAllowedDeparture: Double
)

@Serializable
private class T254CeilingRow(
    val crossSection: String,
    val hingeStiffnessEnhancement: Double,
    val compositeFraction: String,
    val freeDishingOverStroke: Double,
    val largestUnitPerRadian: Double,
    val medianUnitPerRadian: Double,
    val allTurnsUnitSum: Double,
    val tenLargestUnitSum: Double,
    val allowedDepartureDegrees: Double,
    val allTurnsCeilingOverStroke: Double,
    val forcedExcessCeilingOverStroke: Double,
    val insideTolerance: Boolean,
    val departureThatWouldReachTheTolerance: Double?
)

@Serializable
private class T254FieldRow(
    val crossSection: String,
    val hingeStiffnessEnhancement: Double,
    val signAssignment: String,
    val departureDegrees: Double,
    val peakDishingOverStroke: Double,
    val overTheCeiling: Double,
    val insideTolerance: Boolean
)

@Serializable
private class T254StiffnessRow(
    val crossSection: String,
    val hingeStiffnessEnhancement: Double,
    val freeDishingWithoutTies: Double,
    val freeDishingWithTies: Double,
    val ratio: Double,
    val tiesSoftenTheBlock: Boolean
)

@Serializable
private class T254Convergence(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departure: Double
)

@Serializable
private class T254Reproduction(
    val source: String,
    val quantity: String,
    val published: String,
    val here: String,
    val departure: Double,
    val reproduced: Boolean
)

@Serializable
private class T254Falsifier(
    val id: String,
    val statement: String,
    val declaredOpen: Boolean,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private class T254Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val cheapBound: Map<String, String>,
    val census: List<T254CensusRow>,
    val turns: List<T254TurnRow>,
    val ceilings: List<T254CeilingRow>,
    val fields: List<T254FieldRow>,
    val stiffness: List<T254StiffnessRow>,
    val convergence: List<T254Convergence>,
    val reproductions: List<T254Reproduction>,
    val falsifiers: List<T254Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

private class T254Design(val rasterRows: Int, val helicesPerRow: Int) {

    val name: String = "$rasterRows x $helicesPerRow"
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)

    fun lattice(
        enhancement: Double = 1.0,
        subdivisions: Int = 1,
        rowBasePairs: Int = T254_ROW_BP,
        ties: List<HoneycombScaffoldTurnTie> = emptyList()
    ) = HoneycombGrillage(
        block = block,
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions,
        scaffoldTurnTies = ties
    )
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val collar = readCollar(ResultInputs.T_3B.file())
    val t246 = Json.parseToJsonElement(ResultInputs.T_246.readText()).jsonObject
    val forcedDeparture = t246.getValue("cheapBound").jsonObject
        .getValue("minimalAzimuthalDepartureDegrees").jsonPrimitive.content.toDouble()
    val allowedDeparture = t246.getValue("allowedCrossoverReadings").jsonArray
        .map { it.jsonObject }
        .first { it.getValue("azimuthDegrees").jsonPrimitive.content.toDouble() != 0.0 }
        .getValue("azimuthDegrees").jsonPrimitive.content.toDouble()
    val closure = t246.getValue("closure").jsonArray.map { it.jsonObject }
    fun forcedAt(one: Int, two: Int, cross: String) = closure.first {
        it.getValue("senseOneBasePairs").jsonPrimitive.content.toInt() == one &&
                it.getValue("senseTwoBasePairs").jsonPrimitive.content.toInt() == two &&
                it.getValue("crossSection").jsonPrimitive.content == cross
    }.getValue("forcedCrossovers").jsonPrimitive.content.toInt()
    val allowedRadians = Math.toRadians(allowedDeparture)
    val forcedExcessRadians = Math.toRadians(forcedDeparture - allowedDeparture)

    val designs = listOf(T254Design(10, 6), T254Design(15, 4))
    val enhancements = mapOf(
        "10 x 6" to listOf(1.0 to "the lattice's own lower bound", 18.4938242 to "f = 0.26",
            21.1851817 to "f = 0.30"),
        "15 x 4" to listOf(1.0 to "the lattice's own lower bound", 9.65079217 to "f = 0.26",
            12.7228458 to "f = 0.30")
    )

    // ------------------------------------------------------ the cheap bound, before any solve
    println("T-254 - THE CHEAP BOUND: a prestrain reaches w only through the covalent LINK,")
    println("  whose in-plane arm is (d/2) u_y. The coefficient is EXACTLY ZERO iff u_y = 0.")
    println("  The honeycomb's three azimuths give |u_y| = 1 (in plane) or 1/2 (through the")
    println("  thickness), so NO bond and NO turn tie has a zero arm: every raster turn is on")
    println("  the flatness axis, and C-0147's exact zero for the RAGGEDNESS does not transfer.")

    val census = designs.map { design ->
        val bare = design.lattice()
        val ties = honeycombScaffoldTurnTies(design.block, bare.nodesPerBeam)
        val armed = design.lattice(ties = ties)
        val turns = honeycombRasterTurnList(design.block)
        val n = design.helicesPerRow
        val faceBeams = (0 until design.rasterRows).map { it * n }.toSet()
        T254CensusRow(
            crossSection = design.name,
            helices = design.block.helices,
            turns = turns.size,
            throughThicknessTurns = turns.count { !it.inPlane },
            inPlaneTurns = turns.count { it.inPlane },
            turnsAtHighRim = turns.count { it.atHighEnd },
            turnsAtLowRim = turns.count { !it.atHighEnd },
            latticeBonds = bare.bonds.size,
            interfacesCarryingATurn = turns.map { it.lowerBeam to it.upperBeam }.distinct().size,
            interlayerInterfaces = honeycombBondPairs(design.block).count {
                it.first % n != it.second % n
            },
            inPlaneInterfaces = honeycombBondPairs(design.block).count {
                it.first % n == it.second % n
            },
            turnsBetweenTwoGapFacingHelices = turns.count {
                faceBeams.contains(it.lowerBeam) && faceBeams.contains(it.upperBeam)
            },
            leastAbsoluteInPlaneArm = armed.turnElements.minOf { abs(it.unitY) },
            anyTieHasAZeroInPlaneArm = armed.turnElements.any { abs(it.unitY) < 1e-12 }
        )
    }
    census.forEach {
        println(
            "  " + it.crossSection + ": " + it.turns + " turns = " + it.throughThicknessTurns +
                    " through the thickness + " + it.inPlaneTurns + " in plane, " +
                    it.turnsAtHighRim + " / " + it.turnsAtLowRim + " at the two rims; " +
                    it.turnsBetweenTwoGapFacingHelices + " join two gap-facing helices; " +
                    "least |u_y| = " + it.leastAbsoluteInPlaneArm.emitted(6)
        )
    }

    // ------------------------------------------------------ the influence bank and the ceiling
    val headline = designs.first()
    val turnRows = mutableListOf<T254TurnRow>()
    val ceilings = mutableListOf<T254CeilingRow>()
    val fields = mutableListOf<T254FieldRow>()
    val stiffness = mutableListOf<T254StiffnessRow>()
    designs.forEach { design ->
        val n = design.helicesPerRow
        val faceBeams = (0 until design.rasterRows).map { it * n }.toSet()
        val turns = honeycombRasterTurnList(design.block)
        enhancements.getValue(design.name).forEach { (factor, label) ->
            val bare = design.lattice(enhancement = factor)
            val ties = honeycombScaffoldTurnTies(design.block, bare.nodesPerBeam)
            val armed = design.lattice(enhancement = factor, ties = ties)
            val pressure = collar.field(
                Gen1Tile.TARGET_FORCE / armed.area, armed.lengthS, armed.lengthY
            )
            val stroke = Gen1Tile.TARGET_FORCE / armed.area / Gen1Tile.FOUNDATION_SECANT
            val free = armed.solve(pressure).peakDishing(T254_SAMPLES) / stroke
            val bareFree = bare.solve(pressure).peakDishing(T254_SAMPLES) / stroke
            // CLAUDE.md / C-0104: every influence is taken on the lattice WITHOUT prestrain.
            val unit = armed.withoutPrestrain.turnElements.map {
                armed.withoutPrestrain.unitTurnResponse(it).peakDishing(T254_SAMPLES) / stroke
            }
            if (design === headline && factor == 21.1851817) {
                armed.turnElements.forEachIndexed { i, element ->
                    turnRows += T254TurnRow(
                        index = turns[i].index,
                        lowerBeam = element.tie.lowerBeam,
                        upperBeam = element.tie.upperBeam,
                        inPlane = element.inPlane,
                        atHighEnd = turns[i].atHighEnd,
                        touchesTheGapFacingFace = faceBeams.contains(element.tie.lowerBeam) ||
                                faceBeams.contains(element.tie.upperBeam),
                        unitY = element.unitY,
                        unitZ = element.unitZ,
                        peakDishingPerRadian = unit[i],
                        atTheAllowedDeparture = unit[i] * allowedRadians
                    )
                }
            }
            val sorted = unit.sortedDescending()
            val allSum = unit.sum()
            val tenSum = sorted.take(T254_FORCED_CROSSOVERS).sum()
            val allCeiling = free + allSum * allowedRadians
            ceilings += T254CeilingRow(
                crossSection = design.name,
                hingeStiffnessEnhancement = factor,
                compositeFraction = label,
                freeDishingOverStroke = free,
                largestUnitPerRadian = sorted.first(),
                medianUnitPerRadian = sorted[sorted.size / 2],
                allTurnsUnitSum = allSum,
                tenLargestUnitSum = tenSum,
                allowedDepartureDegrees = allowedDeparture,
                allTurnsCeilingOverStroke = allCeiling,
                forcedExcessCeilingOverStroke = allCeiling + tenSum * forcedExcessRadians,
                insideTolerance = allCeiling < T254_TOLERANCE,
                departureThatWouldReachTheTolerance =
                    if (free >= T254_TOLERANCE) null
                    else Math.toDegrees((T254_TOLERANCE - free) / allSum)
            )
            stiffness += T254StiffnessRow(
                crossSection = design.name,
                hingeStiffnessEnhancement = factor,
                freeDishingWithoutTies = bareFree,
                freeDishingWithTies = free,
                ratio = free / bareFree,
                tiesSoftenTheBlock = free > bareFree * (1.0 + 1e-9)
            )
            // the sign is undetermined; sweep it (CLAUDE.md).
            listOf(
                "every turn the same way" to { _: Int -> 1.0 },
                "alternating along the raster path" to { k: Int -> if (k % 2 == 0) 1.0 else -1.0 },
                "by the rim the turn sits at" to { k: Int ->
                    if (turns[k].atHighEnd) 1.0 else -1.0
                }
            ).forEach { (assignment, sign) ->
                val loaded = design.lattice(
                    enhancement = factor,
                    ties = ties.mapIndexed { k, tie ->
                        tie.copy(prestrainRadians = sign(k) * allowedRadians)
                    }
                )
                val peak = loaded.solve(pressure).peakDishing(T254_SAMPLES) / stroke
                fields += T254FieldRow(
                    crossSection = design.name,
                    hingeStiffnessEnhancement = factor,
                    signAssignment = assignment,
                    departureDegrees = allowedDeparture,
                    peakDishingOverStroke = peak,
                    overTheCeiling = peak / allCeiling,
                    insideTolerance = peak < T254_TOLERANCE
                )
            }
        }
    }
    println("T-254 - the ceiling over every sign and every subset, at the allowed departure")
    ceilings.forEach {
        println(
            "  " + it.crossSection + "  enhancement " + it.hingeStiffnessEnhancement.emitted(6) +
                    "  free " + it.freeDishingOverStroke.emitted(6) + "  ceiling " +
                    it.allTurnsCeilingOverStroke.emitted(6) +
                    (if (it.insideTolerance) "  INSIDE T-5b" else "  OUTSIDE T-5b") +
                    "  threshold " +
                    (it.departureThatWouldReachTheTolerance?.emitted(6)
                        ?: "none - the free tile already exceeds it") + " deg"
        )
    }
    println("T-254 - the field at three sign assignments")
    fields.forEach {
        println(
            "  " + it.crossSection + "  enhancement " + it.hingeStiffnessEnhancement.emitted(6) +
                    "  " + it.signAssignment + " -> " + it.peakDishingOverStroke.emitted(6) +
                    " (" + it.overTheCeiling.emitted(6) + " of the ceiling)"
        )
    }
    println("T-254 - what the 59 ties are worth as STIFFNESS")
    stiffness.forEach {
        println(
            "  " + it.crossSection + "  enhancement " + it.hingeStiffnessEnhancement.emitted(6) +
                    "  without " + it.freeDishingWithoutTies.emitted(6) + "  with " +
                    it.freeDishingWithTies.emitted(6) + "  ratio " + it.ratio.emitted(6)
        )
    }

    // ------------------------------------------------------ convergence
    val convergence = mutableListOf<T254Convergence>()
    var previous: Double? = null
    listOf(1, 2).forEach { subdivisions ->
        val bare = headline.lattice(enhancement = 21.1851817, subdivisions = subdivisions)
        val ties = honeycombScaffoldTurnTies(headline.block, bare.nodesPerBeam)
        val armed = headline.lattice(
            enhancement = 21.1851817, subdivisions = subdivisions, ties = ties
        )
        val pressure = collar.field(
            Gen1Tile.TARGET_FORCE / armed.area, armed.lengthS, armed.lengthY
        )
        val stroke = Gen1Tile.TARGET_FORCE / armed.area / Gen1Tile.FOUNDATION_SECANT
        val value = armed.withoutPrestrain.turnElements.maxOf {
            armed.withoutPrestrain.unitTurnResponse(it).peakDishing(T254_SAMPLES) / stroke
        }
        convergence += T254Convergence(
            axis = "beam subdivisions", setting = subdivisions.toString(),
            quantity = "the largest unit turn response, per radian", value = value,
            departure = previous?.let { relativeDeparture(value, it) } ?: 0.0
        )
        previous = value
    }
    previous = null
    listOf(41, 81, 161).forEach { samples ->
        val bare = headline.lattice(enhancement = 21.1851817)
        val ties = honeycombScaffoldTurnTies(headline.block, bare.nodesPerBeam)
        val armed = headline.lattice(enhancement = 21.1851817, ties = ties)
        val stroke = Gen1Tile.TARGET_FORCE / armed.area / Gen1Tile.FOUNDATION_SECANT
        val value = armed.turnElements.maxOf {
            armed.unitTurnResponse(it).peakDishing(samples) / stroke
        }
        convergence += T254Convergence(
            axis = "dishing sample grid", setting = samples.toString(),
            quantity = "the largest unit turn response, per radian", value = value,
            departure = previous?.let { relativeDeparture(value, it) } ?: 0.0
        )
        previous = value
    }

    // ------------------------------------------------------ reproductions
    val uniform = headline.lattice(
        enhancement = 21.1851817,
        ties = honeycombScaffoldTurnTies(headline.block, headline.lattice().nodesPerBeam)
    ).solve(uniformPressure(0.02)).peakDishing(T254_SAMPLES)
    val c0154Free = run {
        val lattice = headline.lattice(enhancement = 21.1851817, rowBasePairs = 112)
        val pressure = collar.field(
            Gen1Tile.TARGET_FORCE / lattice.area, lattice.lengthS, lattice.lengthY
        )
        val stroke = Gen1Tile.TARGET_FORCE / lattice.area / Gen1Tile.FOUNDATION_SECANT
        lattice.solve(pressure).peakDishing(T254_SAMPLES) / stroke
    }
    val reproductions = listOf(
        T254Reproduction(
            "C-0154 / T-253", "the free 10 x 6 tile at the calibrated coupling, 112 bp row",
            "0.0449400126", c0154Free.emitted(9),
            relativeDeparture(c0154Free, 0.0449400126), relativeDeparture(c0154Free, 0.0449400126) < 1e-6
        ),
        T254Reproduction(
            "C-0152 / T-246", "the minimal forced azimuthal departure",
            "17.1428571", forcedDeparture.emitted(9),
            relativeDeparture(forcedDeparture, 17.1428571), relativeDeparture(forcedDeparture, 17.1428571) < 1e-8
        ),
        T254Reproduction(
            "C-0152 / T-246", "the departure an ALLOWED scaffold crossover carries",
            "8.57142857", allowedDeparture.emitted(9),
            relativeDeparture(allowedDeparture, allowedScaffoldCrossoverDepartureDegrees()),
            relativeDeparture(allowedDeparture, allowedScaffoldCrossoverDepartureDegrees()) < 1e-8
        ),
        T254Reproduction(
            "C-0152 / T-246", "forced crossovers at 112 / 108 on 10 x 6",
            "10", forcedAt(112, 108, "10 x 6").toString(),
            0.0, forcedAt(112, 108, "10 x 6") == T254_FORCED_CROSSOVERS
        ),
        T254Reproduction(
            "C-0151 / T-245 through C-0152", "forced crossovers at the DRAWABLE 102 / 109",
            "0", forcedAt(102, 109, "10 x 6").toString(),
            0.0, forcedAt(102, 109, "10 x 6") == 0
        ),
        T254Reproduction(
            "C-0154 / T-253", "raster crossovers of the block",
            "59", census.first().turns.toString(),
            0.0, census.all { it.turns == 59 }
        ),
        T254Reproduction(
            "C-0154 / T-253", "the interlayer interfaces of 10 x 6, all of them turn-loaded",
            "50", census.first().interlayerInterfaces.toString(),
            0.0, census.first().interlayerInterfaces == 50 &&
                    census.first().throughThicknessTurns == 50
        ),
        T254Reproduction(
            "C-0167 / T-263", "the UNCOUPLED 10 x 6 tile at f = 0.30 and f = 0.26, 116 bp row",
            "0.0501417315 and 0.0522223659",
            stiffness.filter { it.crossSection == "10 x 6" && it.hingeStiffnessEnhancement > 1.0 }
                .sortedByDescending { it.hingeStiffnessEnhancement }
                .joinToString(" and ") { it.freeDishingWithoutTies.emitted(9) },
            maxOf(
                relativeDeparture(
                    stiffness.first {
                        it.crossSection == "10 x 6" && it.hingeStiffnessEnhancement == 21.1851817
                    }.freeDishingWithoutTies, 0.0501417315
                ),
                relativeDeparture(
                    stiffness.first {
                        it.crossSection == "10 x 6" && it.hingeStiffnessEnhancement == 18.4938242
                    }.freeDishingWithoutTies, 0.0522223659
                )
            ),
            maxOf(
                relativeDeparture(
                    stiffness.first {
                        it.crossSection == "10 x 6" && it.hingeStiffnessEnhancement == 21.1851817
                    }.freeDishingWithoutTies, 0.0501417315
                ),
                relativeDeparture(
                    stiffness.first {
                        it.crossSection == "10 x 6" && it.hingeStiffnessEnhancement == 18.4938242
                    }.freeDishingWithoutTies, 0.0522223659
                )
            ) < 1e-6
        ),
        T254Reproduction(
            "C-0154 / T-253", "the lattice bonds and interfaces of both cross-sections",
            "435 bonds, 27 in plane + 50 interlayer on 10 x 6; 410, 28 + 45 on 15 x 4",
            census.joinToString("; ") {
                it.latticeBonds.toString() + " bonds, " + it.inPlaneInterfaces + " + " +
                        it.interlayerInterfaces
            },
            0.0,
            census[0].latticeBonds == 435 && census[0].inPlaneInterfaces == 27 &&
                    census[1].latticeBonds == 410 && census[1].inPlaneInterfaces == 28
        ),
        T254Reproduction(
            "CLAUDE.md's standing falsifier", "uniform pressure on the tied lattice, peak dishing",
            "0", uniform.emitted(3), abs(uniform), uniform < 1e-9
        )
    )

    // ------------------------------------------------------ falsifiers
    val headlineCeiling = ceilings.first {
        it.crossSection == "10 x 6" && it.hingeStiffnessEnhancement == 21.1851817
    }
    val falsifiers = listOf(
        T254Falsifier(
            "F1", "some honeycomb bond has u_y = 0, so the roll couple has no in-plane arm",
            false, census.any { it.anyTieHasAZeroInPlaneArm },
            "the least |u_y| over every tie of both cross-sections is " +
                    census.minOf { it.leastAbsoluteInPlaneArm }.emitted(6) +
                    "; the honeycomb's azimuths give 1 in plane and 1/2 through the thickness"
        ),
        T254Falsifier(
            "F2",
            "the turn census fails to reproduce honeycombXRasterPath, or the two turn kinds " +
                    "fail to sum to H - 1, or a consecutive path pair is not a honeycomb bond",
            false,
            census.any { it.throughThicknessTurns + it.inPlaneTurns != it.helices - 1 },
            "59 = 50 + 9 on 10 x 6 and 59 = 45 + 14 on 15 x 4, and the order reproduces the " +
                    "path under the COLUMN MIRROR, which is not optional"
        ),
        T254Falsifier(
            "F3", "adding the turn ties moves the uniform-load dishing off zero",
            false, uniform >= 1e-9,
            "peak dishing under a uniform pressure on the tied lattice is " + uniform.emitted(3)
        ),
        T254Falsifier(
            "F4",
            "the field is not linear in the prestrain, or an influence taken on the prestrained " +
                    "lattice differs from one taken on withoutPrestrain",
            false, false,
            "asserted as two named tests; every influence here is taken on withoutPrestrain"
        ),
        T254Falsifier(
            "F5",
            "the turn-set ceiling at the departure the recommended raster carries exceeds " +
                    "T-5b's 0.10",
            true, ceilings.any { it.hingeStiffnessEnhancement > 1.0 && !it.insideTolerance },
            "it fires at " +
                    ceilings.count { it.hingeStiffnessEnhancement > 1.0 && !it.insideTolerance } +
                    " of " + ceilings.count { it.hingeStiffnessEnhancement > 1.0 } +
                    " coupled states, ALL of them 15 x 4, and at every one of those the FREE " +
                    "tile already exceeds the tolerance - so the turns never DECIDE the " +
                    "verdict, which is C-0154's own F5 read at the turns. At the recommended " +
                    "10 x 6 the ceiling is " +
                    headlineCeiling.allTurnsCeilingOverStroke.emitted(9) +
                    " and the departure that would reach the tolerance is " +
                    (headlineCeiling.departureThatWouldReachTheTolerance?.emitted(6) ?: "none") +
                    " deg against the " + allowedDeparture.emitted(9) + " deg carried, a " +
                    "margin of " +
                    ((headlineCeiling.departureThatWouldReachTheTolerance ?: 0.0) /
                            allowedDeparture).emitted(6) + "x"
        ),
        T254Falsifier(
            "F6",
            "adding 59 covalent ties moves the free-tile dishing by more than the convergence " +
                    "departure",
            true,
            stiffness.any { abs(it.ratio - 1.0) > convergence.maxOf { c -> abs(c.departure) } },
            "the ratio runs " + stiffness.minOf { it.ratio }.emitted(6) + " to " +
                    stiffness.maxOf { it.ratio }.emitted(6) +
                    " against a worst convergence departure of " +
                    convergence.maxOf { abs(it.departure) }.emitted(6)
        ),
        T254Falsifier(
            "F7",
            "the recommended raster carries forced crossovers, or 112 / 108 carries none",
            false,
            forcedAt(102, 109, "10 x 6") != 0 || forcedAt(112, 108, "10 x 6") == 0,
            "0 at 102 / 109 and 10 at 112 / 108, read from C-0152's own closure census"
        ),
        T254Falsifier(
            "F8", "the largest single-turn coefficient is below the emitted solve residual",
            false,
            headlineCeiling.largestUnitPerRadian < 1e-9,
            "the largest is " + headlineCeiling.largestUnitPerRadian.emitted(9) +
                    " of the stroke per radian, four orders above anything numerical here"
        )
    )

    val turnsAtHigh = census.first().turnsAtHighRim
    val worstField = fields.filter { it.hingeStiffnessEnhancement > 1.0 }
        .maxByOrNull { it.peakDishingOverStroke }!!
    val findings = listOf(
        "THE ANSWER IS NO, AND IT IS A LEVER ARM. A prestrain's work conjugate is a ROLL, and a " +
                "roll reaches the deflection field only through the covalent link, whose " +
                "in-plane arm is (d/2) u_y - so the coefficient is exactly zero if and only if " +
                "u_y = 0. The honeycomb's three azimuths give |u_y| = 1 for the in-plane bond " +
                "and 1/2 for each of the two through-thickness ones, so NO bond and NO turn tie " +
                "has a zero arm and the least over both cross-sections is " +
                census.minOf { it.leastAbsoluteInPlaneArm }.emitted(6) + ". C-0147's exact zero " +
                "for the RAGGEDNESS does not transfer, and the reason is that a raggedness is a " +
                "GEOMETRY on an orthogonal coordinate while a prestrain is a LOAD - a load is " +
                "not confined to the coordinate it is applied on. One line of azimuth " +
                "arithmetic, no solve.",
        "AND THE LOAD IS NOT THE ONE THE QUESTION WAS RAISED ABOUT. C-0151's drawable raster " +
                "102 / 109 carries ZERO forced crossovers, so C-0152's ten-crossover forcing " +
                "does not exist on the recommended design at all. What does exist at EVERY " +
                "raster turn of EVERY honeycomb origami is C-0152's own calibration: caDNAno's " +
                "+-5 bp is an integer approximation to a 5.25 bp half turn, so an ALLOWED " +
                "scaffold crossover already sits " + allowedDeparture.emitted(9) + " deg off " +
                "the line of centres. That is a prestrain on all 59 turns, on either raster, " +
                "and no claim in this corpus has ever applied it as a load.",
        "THE COEFFICIENT, AND IT IS A DISTRIBUTION. At 10 x 6 and the calibrated coupling the " +
                "largest single turn is " + headlineCeiling.largestUnitPerRadian.emitted(9) +
                " of the free stroke per radian and the median " +
                headlineCeiling.medianUnitPerRadian.emitted(9) + "; at the allowed departure " +
                "the largest single turn is worth " +
                (headlineCeiling.largestUnitPerRadian * allowedRadians).emitted(9) + " of the " +
                "stroke. The triangle-inequality ceiling over ALL 59 turns and EVERY sign is " +
                headlineCeiling.allTurnsCeilingOverStroke.emitted(9) + " against a free tile of " +
                headlineCeiling.freeDishingOverStroke.emitted(9) + ", and the departure that " +
                "would reach T-5b's 0.10 is " +
                (headlineCeiling.departureThatWouldReachTheTolerance?.emitted(6) ?: "unreachable") +
                " deg.",
        "THE CENSUS IS THE OTHER HALF, AND IT SAYS WHICH INTERFACES THE SCAFFOLD LOADS. On a " +
                "10 x 6 block the 59 turns are 50 through-thickness ties - which is EVERY one " +
                "of the 50 interlayer interfaces C-0154 counts - and 9 in-plane ones, 9 of its " +
                "27; on 15 x 4 the split is 45 and 14. They alternate axial ends, " +
                "" + turnsAtHigh + " at one rim and " + census.first().turnsAtLowRim +
                " at the other, and only " +
                census.first().turnsBetweenTwoGapFacingHelices +
                " of them join two gap-facing helices.",
        "THE 59 TURN TIES ARE COVALENT ELEMENTS THE CORPUS'S HONEYCOMB LATTICE DOES NOT CARRY, " +
                "and putting them in is worth " + stiffness.minOf { it.ratio }.emitted(6) +
                " to " + stiffness.maxOf { it.ratio }.emitted(6) +
                "x of the free-tile dishing. C-0154's bonds are the STAPLE ladder at 7 bp " +
                "planes; a raster turn sits at s = +-L/2, past the last of them, and it is a " +
                "covalent crossover like any other. This is C-0099's square-lattice 56 = 42 + 14 " +
                "read on the honeycomb, where the split is 435 + 59.",
        "THE SIGN IS UNDETERMINED AND IT IS WORTH A FACTOR. Swept over three assignments the " +
                "realised field runs " +
                fields.filter { it.hingeStiffnessEnhancement > 1.0 }
                    .minOf { it.peakDishingOverStroke }.emitted(9) + " to " +
                worstField.peakDishingOverStroke.emitted(9) + " of the stroke, the worst being " +
                worstField.signAssignment + " at " + worstField.crossSection +
                " - all of them under the ceiling, which is what a ceiling is for."
    )

    val result = T254Result(
        task = "T-254",
        leaf = "A8.2",
        title = "does a raster TURN sit on the flatness axis at all?",
        verificationType = "logical - an exact census of the raster's turns against the " +
                "honeycomb bond lattice, and a lever-arm argument that costs no solve - " +
                "PLUS in-silico: C-0154's three-dimensional beam-and-bond lattice, a linear " +
                "prestrain influence bank and a triangle-inequality ceiling",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED on a " +
                "folded object. k_theta is Gen1Tile's square-lattice-fitted constant and the " +
                "departure is a lattice statement, not a measurement.",
        units = mapOf(
            "length" to "nm",
            "angle" to "degrees in prose, radians internally",
            "hinge stiffness" to "pN nm / rad",
            "dishing" to "dimensionless, a fraction of the free stroke",
            "temperature" to "K"
        ),
        conventions = mapOf(
            "beam index" to "rasterRow * helicesPerRow + column",
            "gap-facing face" to "column 0; the Winkler foundation acts on it alone",
            "s" to "along the helices, s = 0 at the block's centre, rims at +-L/2",
            "w" to "the deflection along the tile normal, positive downward",
            "phi" to "the roll about s - the coordinate a prestrain is conjugate to",
            "a raster turn" to "a consecutive pair of the x-raster path; with zero unpaired " +
                    "nucleotides it IS a scaffold crossover, a covalent tie at the two " +
                    "helices' ends, so it sits at s = +-L/2",
            "raster order in block coordinates" to "row r is traversed n-1..0 when r is even, " +
                    "which is honeycombXRasterPath under the COLUMN MIRROR c = n-1-x; " +
                    "HoneycombBlock and HoneycombCell use opposite vertical-bond parities",
            "prestrain" to "the relative roll a tie is built at; it enters as a couple pair " +
                    "+-k_theta theta_0 and changes no entry of the stiffness matrix (C-0104)",
            "dishing" to "T-5b's: the peak of the field with its mean and both rigid tilts " +
                    "removed, over the free stroke p / k_f; tolerance 0.10"
        ),
        parameters = mapOf(
            "cross-sections" to "10 x 6 (recommended) and 15 x 4 (control), 60 helices each",
            "row [bp]" to T254_ROW_BP.toString(),
            "honeycomb d [nm]" to Gen1Tile.INTERHELICAL_HONEYCOMB.emitted(9),
            "in-plane row pitch 3d/2 [nm]" to
                    HoneycombCrossSectionGeometry.rowPitch().emitted(9),
            "layer pitch d sqrt(3)/2 [nm]" to
                    HoneycombCrossSectionGeometry.columnPitch().emitted(9),
            "rise [nm/bp]" to Gen1Tile.RISE_PER_BASE_PAIR.emitted(9),
            "k_theta [pN nm/rad]" to Gen1Tile.crossoverHingeStiffness().emitted(9),
            "k_s [pN/nm]" to Gen1Tile.crossoverInPlaneStiffness().emitted(9),
            "link penalty [pN/nm]" to HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(9),
            "hinge stiffness enhancements, 10 x 6" to "1.0, 18.4938242, 21.1851817",
            "hinge stiffness enhancements, 15 x 4" to "1.0, 9.65079217, 12.7228458",
            "foundation secant [pN/nm per nm^2]" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "target force [pN]" to Gen1Tile.TARGET_FORCE.emitted(9),
            "collar" to "C-0022's solved profile at 2 mM / 10 nm / 0.192 V",
            "rim standoff [nm]" to T254_RIM_STANDOFF.emitted(9),
            "dishing sample grid" to T254_SAMPLES.toString(),
            "flatness tolerance (T-5b)" to T254_TOLERANCE.emitted(9),
            "allowed scaffold departure [deg]" to allowedDeparture.emitted(9),
            "minimal forced departure [deg]" to forcedDeparture.emitted(9),
            "temperature [K]" to "300"
        ),
        sources = listOf(
            "C-0152 (T-246) - the azimuthal departures, the closure census and the calibration",
            "C-0154 (T-253) - the honeycomb grillage, and the open question this answers",
            "C-0151 (T-245) - the drawable raster 102 / 109 and its zero forced crossovers",
            "C-0147 (T-231) - the raggedness argument this one does NOT inherit",
            "C-0104 (T-172) - a prestrain is a load, and the influence-function trap",
            "C-0022 (T-3b) - the solved edge collar",
            "C-0140 (T-224) - the x-raster path and its turn senses"
        ),
        citedInputs = mapOf(
            ResultInputs.T_3B.tag to ResultInputs.T_3B.path,
            ResultInputs.T_246.tag to ResultInputs.T_246.path
        ),
        cheapBound = mapOf(
            "question" to "does a prestrain at a raster turn reach w at all?",
            "the mechanism" to "a prestrain's work conjugate is a ROLL; a roll reaches w only " +
                    "through the covalent link, whose in-plane arm is (d/2) u_y",
            "the exact-zero condition" to "u_y = 0 - a tie stacked purely through the thickness " +
                    "would roll its two duplexes against each other and lift neither",
            "what the honeycomb supplies" to "|u_y| = 1 for the in-plane bond and 1/2 for each " +
                    "of the two through-thickness ones; no azimuth gives zero",
            "verdict" to "the coefficient is NOT zero, at any turn, on either cross-section - " +
                    "and C-0147's zero does not transfer because a raggedness is a GEOMETRY on " +
                    "an orthogonal coordinate and a prestrain is a LOAD",
            "cost" to "one line of azimuth arithmetic, before a matrix is assembled"
        ),
        census = census,
        turns = turnRows,
        ceilings = ceilings,
        fields = fields,
        stiffness = stiffness,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "The grillage carries ONE row length, so the two-length raster enters as its 116 bp " +
                    "block extent. The 7 bp stagger between the two lengths is 2.38 nm of " +
                    "axial extent and is not modelled; T-258 shows the coefficient of that " +
                    "raggedness on the flatness is exactly zero, so what is missing here is " +
                    "the AXIAL POSITION of a turn to within the stagger, not a flatness term.",
            "The turn tie is modelled at the block's own rim node, s = +-L/2. A scaffold " +
                    "crossover sits 5 bp from a staple position, so its true axial position is " +
                    "within 5 bp = 1.7 nm of that node; the influence bank is emitted per turn " +
                    "so the sensitivity to that placement is readable rather than assumed.",
            "The departure is read through C-0104's roll mapping, which is the mapping this " +
                    "corpus already uses for the same quantity. It is a rigid-duplex reading " +
                    "and therefore a CEILING on the true relative roll: a real crossover " +
                    "absorbs part of the azimuth in backbone strain.",
            "The sign of each turn's prestrain is undetermined by any source in this " +
                    "repository. Three assignments are swept and the triangle-inequality " +
                    "ceiling bounds all of them and every subset, which is why the ceiling and " +
                    "not the field is the quotable number.",
            "The lattice carries no across-helix parallel-axis term (C-0154), so its D_perp is " +
                    "the independent one and a LOWER bound; the bracket is run at three ends " +
                    "and the two that carry any interlayer coupling agree on every verdict.",
            "The block is FREE: no attachment coupling is applied, so every dishing number is " +
                    "C-0109's uncoupled reference and not a design.",
            "k_theta is Gen1Tile's square-lattice-fitted constant; no honeycomb measurement of " +
                    "it exists in this repository."
        ),
        openQuestions = listOf(
            "What the 59 turn ties do to a COUPLED honeycomb cell. C-0167 re-graded every cell " +
                    "on the tie-free lattice and found 0 of 64 flat at p90; the ties stiffen " +
                    "the block, so the re-grade is owed on the tied lattice.",
            "Whether the allowed departure is a prestrain the folded structure actually " +
                    "carries, or one it relaxes by unstacking a base pair. The rigid-duplex " +
                    "reading is a ceiling and nothing here bounds it from below.",
            "What the sign assignment of the turn prestrains is. It is a property of the " +
                    "raster's own turn senses and the crossover's displacement direction, and " +
                    "no source in this repository fixes it.",
            "Whether the same argument applies to the SQUARE lattice's row-end crossovers, " +
                    "where C-0104 measured the flatness effect but the lever-arm census has " +
                    "never been taken."
        ),
        proseFailure = "none"
    )

    val output = File("gpd/results/T-254-raster-turn-prestrain.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-254 - wrote " + output.path)
}
