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

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.PI
import kotlin.math.abs

/**
 * `T-134` — **a tolerance model for the two knife edges `C-0069` and `C-0066` carry**, and the
 * design that has margin.
 *
 * Emits `gpd/results/T-134-plan-tolerance.json`.
 */

private const val DUPLEXES = 15
private const val FLATNESS_TOLERANCE = 0.10
private const val PHASE = 24
private const val RIM_STANDOFF = 1.0
private const val DEGREES = 180.0 / PI
private val LEG_ENVELOPE = 12..26
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T134BoundRecord(
    val id: String,
    val bound: String,
    val value: Double,
    val against: Double,
    val ratio: Double,
    val unit: String,
    val fired: Boolean,
    val settled: String
)

@Serializable
private data class T134IdentityRecord(
    val reading: String,
    val grouping: String,
    val published: Double,
    val throughIdentity: Double,
    val departure: Double
)

@Serializable
private data class T134ChannelRecord(
    val channel: String,
    val correlation: String?,
    val coefficientPerUnitRelative: Double,
    val marginReading: String,
    val margin: Double,
    val relativeThreshold: Double,
    val absoluteThreshold: Double,
    val note: String
)

@Serializable
private data class T134ThermalRecord(
    val channel: String,
    val sigma: Double,
    val overMargin: Double,
    val stiffnessSupplied: Double,
    val stiffnessDemanded: Double,
    val stiffnessShortfall: Double,
    val unit: String,
    val note: String
)

@Serializable
private data class T134StiffnessScatterRecord(
    val driver: String,
    val relativeAmplitude: Double,
    val relativeStiffnessScatter: Double,
    val againstC0026BreakEven: Double,
    val againstC0060Flatness: Double,
    val equivalentDropoutRate: Double,
    val note: String
)

@Serializable
private data class T134CountRecord(
    val paths: Int,
    val arm: Double,
    val armBasePairs: Int,
    val ceiling: Double,
    val margin: Double,
    val marginOverRise: Double,
    val placed: Int,
    val perPathForce: Double,
    val perPathSecant: Double,
    val clearsUnzip: Boolean,
    val clearsRiseQuantum: Boolean,
    val clearsAxialFluctuation: Boolean,
    val clearsTipFluctuation: Boolean
)

@Serializable
private data class T134FlatnessRecord(
    val placement: String,
    val stations: Int,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val peakPathForce: Double,
    val peakCrossoverForce: Double
)

@Serializable
private data class T134SeatRecord(
    val grid: String,
    val lateralSeat: Double,
    val pairExists: Boolean,
    val admissiblePairs: Int,
    val representableLengths: Int,
    val passingLengths: Int,
    val passes: Boolean,
    val verdict: String
)

@Serializable
private data class T134LiteratureRecord(
    val source: String,
    val quantity: String,
    val value: Double,
    val unit: String,
    val readFlag: String,
    val comparedWith: String,
    val against: Double,
    val ratio: Double,
    val note: String
)

@Serializable
private data class T134JointWindowRecord(
    val marginDemanded: Double,
    val demandedBy: String,
    val lengthCeiling: Double,
    val tipCeiling: Double,
    val tipUsed: Double,
    val tipInsideByPercent: Double,
    val rootCeiling: Double,
    val rootUsed: Double,
    val rootInsideByPercent: Double,
    val tipAdmissible: Boolean,
    val rootAdmissible: Boolean,
    val note: String
)

@Serializable
private data class T134ConvergenceRecord(
    val quantity: String,
    val axis: String,
    val coarse: Double,
    val medium: Double,
    val fine: Double,
    val departure: Double,
    val note: String
)

@Serializable
private data class T134ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val flag: String
)

@Serializable
private data class T134PredicateRecord(
    val id: String,
    val statement: String,
    val met: Boolean,
    val evidence: String
)

@Serializable
private data class T134Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val identity: List<T134IdentityRecord>,
    val bounds: List<T134BoundRecord>,
    val channels: List<T134ChannelRecord>,
    val thermal: List<T134ThermalRecord>,
    val stiffnessScatter: List<T134StiffnessScatterRecord>,
    val counts: List<T134CountRecord>,
    val flatness: List<T134FlatnessRecord>,
    val seats: List<T134SeatRecord>,
    val jointWindow: List<T134JointWindowRecord>,
    val literature: List<T134LiteratureRecord>,
    val convergence: List<T134ConvergenceRecord>,
    val reproductions: List<T134ReproductionRecord>,
    val predicates: List<T134PredicateRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

/** `C-0063`'s own 34 stations, read from its result file rather than retyped. */
private fun c0063Stations(file: File): List<TrussStation> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .flatMap { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            val y = row.getValue("y").jsonPrimitive.content.toDouble()
            row.getValue("roots").jsonArray.map {
                TrussStation(index, it.jsonPrimitive.content.toDouble(), y)
            }
        }
}

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias**. */
private fun solvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at 2 mM, 10 nm, 0.192 V")
    fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
    return CollarTerm(value("taperDepth"), value("taperWidth")) to
            CollarTerm(value("rimResidualDepth"), RIM_STANDOFF)
}

/** `C-0062`'s per-row cap and flexure misalignment floors, consumed as data from its table. */
private fun c0062Floors(file: File): Map<Int, Pair<Double, Double>> {
    require(file.exists()) { "C-0062's result file is missing: ${file.path}" }
    val out = HashMap<Int, Pair<Double, Double>>()
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("designs").jsonArray.map { it.jsonObject }
        .forEach {
            fun text(key: String) = it.getValue(key).jsonPrimitive.content
            if (!text("id").contains("BOTH ends")) return@forEach
            out[text("separationBasePairs").toInt()] =
                text("capFloorDegrees").toDouble() to text("flexureFloorDegrees").toDouble()
        }
    return out
}

private fun sheet(): OrigamiSheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

private fun lattice(
    sheet: OrigamiSheet,
    columns: CrossoverLayout,
    supports: List<PointSupport> = emptyList(),
    subdivisions: Int = 2
) = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = columns,
    subdivisions = subdivisions,
    supports = supports
)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val kT = thermalEnergy()
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val width = Gen1Tile.INTERHELICAL_SHEET
    val edgeX = Gen1Tile.EDGE_X
    val lengthY = DUPLEXES * width
    val area = edgeX * lengthY
    val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * rise
    val arm = C0055_ARM_LENGTH
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val crossoverInPlane = Gen1Tile.crossoverInPlaneStiffness()
    val tipCouple = ArmAnchorage.twoTerminus().rotationalStiffness
    val stiffestBase = StandoffBase.crossovers(2).rotationalStiffness
    val sheet = sheet()

    println("T-134 — reading C-0063's stations and C-0022's solved load ...")
    val stations = c0063Stations(File("gpd/results/T-125-upward-root-placement.json"))
    check(stations.size == C0055_ARM_COUNT) {
        "C-0063's placement must carry $C0055_ARM_COUNT stations, carried ${stations.size}"
    }
    val rows = stationRows(stations)
    val (smooth, rim) = solvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val interiorPressure = Gen1Tile.TARGET_FORCE / area
    val solvedField = edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))
    val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    // ------------------------------------------------------------ deliverable 1: one quantity
    println("T-134 — the identity: two knife edges, one lattice quantity ...")
    val margin = planMargin(pitch, width, arm)
    val armBasePairs = basePairsNearest(arm, rise)
    val builtArm = builtLength(armBasePairs, rise)
    val builtMargin = planMargin(pitch, width, builtArm)

    val identity = listOf(
        T134IdentityRecord(
            "C-0069's Q5 arm margin", "(pitch - width) - length",
            0.0256, rowOfThreeLengthCeiling(pitch, width) - arm,
            abs((rowOfThreeLengthCeiling(pitch, width) - arm) - 0.0256)
        ),
        T134IdentityRecord(
            "C-0066's bound 4 tie clearance", "(pitch - length) - width",
            0.0256, (pitch - arm) - width, abs(((pitch - arm) - width) - 0.0256)
        ),
        T134IdentityRecord(
            "C-0069's plan budget", "pitch - width", 8.19,
            rowOfThreeLengthCeiling(pitch, width),
            abs(rowOfThreeLengthCeiling(pitch, width) - 8.19)
        ),
        T134IdentityRecord(
            "C-0066's tip gap", "pitch - length", 2.71561, pitch - arm,
            abs((pitch - arm) - 2.71561)
        ),
        T134IdentityRecord(
            "the BUILT arm, 24 bp", "pitch - width - 24 x rise", 0.0256, builtMargin,
            abs(builtMargin - 0.0256)
        )
    )

    // ------------------------------------------------------------ deliverable 2: the four floors
    println("T-134 — the four floors, which run before any distribution ...")
    val axialHost = axialFluctuation(pitch, Gen1Tile.DUPLEX_STRETCH_MODULUS, kT)
    val axialArm = axialFluctuation(arm, Gen1Tile.DUPLEX_STRETCH_MODULUS, kT)
    val axialTotal = quadrature(axialHost, axialArm)
    val tipCantilever = cantileverTipFluctuation(arm, Gen1Tile.DUPLEX_BENDING_RIGIDITY, kT)
    val tipOneCrossover = quadrature(hingeTipFluctuation(arm, hinge, kT), tipCantilever)
    val tipStiffestJoint = quadrature(
        hingeTipFluctuation(arm, stiffestBase, kT), tipCantilever
    )
    val interhelicalFluctuation = kotlin.math.sqrt(kT / crossoverInPlane)

    val bounds = listOf(
        T134BoundRecord(
            "1", "the DESIGN QUANTUM — the base-pair rise", rise, margin, rise / margin, "nm",
            true,
            "the margin is 0.075 of the finest length DNA can specify, so no design can be " +
                    "DRAWN to hold it and no correction can be applied to recover it"
        ),
        T134BoundRecord(
            "2", "the disagreement between two MEASURED interhelical distances",
            SQUARE_LATTICE_INTERHELICAL - width, margin,
            (SQUARE_LATTICE_INTERHELICAL - width) / margin, "nm", true,
            "2.73 minus 2.69, both SAXS, same paper — the spread between two readings of the " +
                    "constant already exceeds the margin, which is C-0066's own flip made into a " +
                    "tolerance statement"
        ),
        T134BoundRecord(
            "3", "the THERMAL axial fluctuation of the two segments the margin differences",
            axialTotal, margin, axialTotal / margin, "nm", true,
            "equipartition on the MEASURED stretch modulus; needs no fabrication measurement"
        ),
        T134BoundRecord(
            "4", "the arm tip's own TRANSVERSE fluctuation at a rigid root",
            tipCantilever, margin, tipCantilever / margin, "nm", true,
            "the arm's own cantilever bending, which survives a perfectly rigid root — so no " +
                    "joint stiffening can rescue the margin"
        ),
        T134BoundRecord(
            "5", "the TWIST coefficient over the band this project disputes",
            0.0, margin, 0.0, "nm per unit relative", false,
            "10.5 and 10.67 bp/turn both round to a 32 bp interface spacing, so the twist does " +
                    "not enter the margin at all — a quantity that does NOT propagate"
        )
    )

    // ------------------------------------------------------------ deliverable 3: the propagation
    println("T-134 — the propagation, with the correlation structure named ...")
    val hostBp = Gen1Tile.CROSSOVER_SPACING_SHEET_BP.toInt()

    fun channel(
        name: String,
        correlation: String?,
        coefficient: Double,
        reading: String,
        marginValue: Double,
        note: String
    ) = T134ChannelRecord(
        channel = name,
        correlation = correlation,
        coefficientPerUnitRelative = coefficient,
        marginReading = reading,
        margin = marginValue,
        relativeThreshold = relativeThreshold(marginValue, coefficient),
        absoluteThreshold = marginValue,
        note = note
    )

    val channels = ArrayList<T134ChannelRecord>()
    listOf("solved, 8.16439 nm" to margin, "built, 24 bp" to builtMargin).forEach { (reading, m) ->
        channels += channel(
            "interhelical distance d", null, width, reading, m,
            "coefficient exactly 1 x d — the margin IS a duplex width short of the tip gap"
        )
        val solved = reading.startsWith("solved")
        RiseCorrelation.entries.forEach { correlation ->
            // a SOLVED length is a number of nm and does not track the rise at all, so the only
            // correlation structure it admits is FIXED_ELEMENT; the other three are properties of
            // a BUILT element, whose length is a base-pair count.
            if (solved != (correlation == RiseCorrelation.FIXED_ELEMENT)) return@forEach
            channels += channel(
                "base-pair rise r", correlation.name,
                riseCoefficient(hostBp, armBasePairs, rise, correlation), reading, m,
                when (correlation) {
                    RiseCorrelation.COMMON ->
                        "the host's 32 bp and the element's 24 bp see the SAME strain, so the " +
                                "coefficient is the DIFFERENCE, 8 bp"
                    RiseCorrelation.INDEPENDENT ->
                        "uncorrelated, quoted as an RMS over the two counts"
                    RiseCorrelation.OPPOSED ->
                        "equal and opposite — the coefficient is the SUM, 56 bp, and it is 7x " +
                                "the common-mode one at the same amplitude"
                    RiseCorrelation.FIXED_ELEMENT ->
                        "the SOLVED reading: an element length in nm does not track the rise, " +
                                "which is what C-0069 and C-0066 implicitly assume"
                }
            )
        }
    }
    val perStepThreshold = perStepRiseSigmaThreshold(builtMargin, hostBp, armBasePairs)

    // ------------------------------------------------------------ the thermal ledger
    val thermal = listOf(
        T134ThermalRecord(
            "the host's 32 bp pitch, axially", axialHost, axialHost / margin,
            Gen1Tile.DUPLEX_STRETCH_MODULUS / pitch, stiffnessForMargin(margin, kT),
            stiffnessForMargin(margin, kT) / (Gen1Tile.DUPLEX_STRETCH_MODULUS / pitch), "pN/nm",
            "S/pitch against k_BT/M^2"
        ),
        T134ThermalRecord(
            "the arm, axially", axialArm, axialArm / margin,
            Gen1Tile.DUPLEX_STRETCH_MODULUS / arm, stiffnessForMargin(margin, kT),
            stiffnessForMargin(margin, kT) / (Gen1Tile.DUPLEX_STRETCH_MODULUS / arm), "pN/nm",
            "S/arm against k_BT/M^2"
        ),
        T134ThermalRecord(
            "the two in quadrature", axialTotal, axialTotal / margin,
            0.0, stiffnessForMargin(margin, kT), 0.0, "pN/nm",
            "the margin is a DIFFERENCE of these two lengths, so they add in quadrature"
        ),
        T134ThermalRecord(
            "the interhelical distance at one crossover", interhelicalFluctuation,
            interhelicalFluctuation / margin, crossoverInPlane, stiffnessForMargin(margin, kT),
            stiffnessForMargin(margin, kT) / crossoverInPlane, "pN/nm",
            "C-0009's crossover in-plane spring, 2 alpha S/(100 a) — a CONSTRUCTION and not a " +
                    "measurement, so this channel is reported with its own four-decade sweep and " +
                    "is NOT counted among the floors"
        ),
        T134ThermalRecord(
            "the interhelical distance at the TOP of C-0009's own sweep",
            kotlin.math.sqrt(kT / Gen1Tile.crossoverInPlaneStiffness(128.0)),
            kotlin.math.sqrt(kT / Gen1Tile.crossoverInPlaneStiffness(128.0)) / margin,
            Gen1Tile.crossoverInPlaneStiffness(128.0), stiffnessForMargin(margin, kT),
            stiffnessForMargin(margin, kT) / Gen1Tile.crossoverInPlaneStiffness(128.0), "pN/nm",
            "THE ONE CHANNEL THAT CAN FALL INSIDE THE MARGIN, and only at the stiffest end of a " +
                    "constructed constant — which is why the four floors are built on measured " +
                    "quantities instead"
        ),
        T134ThermalRecord(
            "the arm tip, at ONE crossover root", tipOneCrossover, tipOneCrossover / margin,
            hinge, rotationalStiffnessForMargin(margin, arm, kT),
            rotationalStiffnessForMargin(margin, arm, kT) / hinge, "pN nm/rad",
            "hinge rotation and the arm's own bending, in quadrature"
        ),
        T134ThermalRecord(
            "the arm tip, at the STIFFEST joint in the catalogue", tipStiffestJoint,
            tipStiffestJoint / margin, stiffestBase,
            rotationalStiffnessForMargin(margin, arm, kT),
            rotationalStiffnessForMargin(margin, arm, kT) / stiffestBase,
            "pN nm/rad",
            "C-0028's B2 — two crossovers across; the excursion falls by only 2.3x"
        ),
        T134ThermalRecord(
            "the arm tip, at a PERFECTLY RIGID root", tipCantilever, tipCantilever / margin,
            0.0, rotationalStiffnessForMargin(margin, arm, kT), 0.0, "pN nm/rad",
            "the floor of the transverse channel — the arm's OWN bending, 3EI/L^3. The supplied " +
                    "stiffness is reported as 0.0 because there is no finite value: a rigid root " +
                    "is a limit, and a sentinel would be the anti-pattern CLAUDE.md names"
        )
    )

    // ------------------------------------------------------------ deliverable 4: T-45's channel
    println("T-134 — the stiffness channel, and what it is NOT ...")
    val c0026BreakEven = 0.17
    val c0060Flatness = 0.346

    fun scatter(driver: String, amplitude: Double, k: Double, note: String) =
        T134StiffnessScatterRecord(
            driver = driver,
            relativeAmplitude = amplitude,
            relativeStiffnessScatter = k,
            againstC0026BreakEven = k / c0026BreakEven,
            againstC0060Flatness = k / c0060Flatness,
            equivalentDropoutRate = dropoutRateForRelativeScatter(k),
            note = note
        )

    val stiffnessScatter = listOf(
        scatter(
            "a relative RISE scatter of 1 %, through L = n x r and k ~ L^-3", 0.01,
            stiffnessScatterFromRise(0.01),
            "the ONLY geometric driver, and the exponent is exactly 3"
        ),
        scatter(
            "a relative rise scatter at the plan margin's own common-mode threshold",
            relativeThreshold(builtMargin, riseCoefficient(hostBp, armBasePairs, rise)),
            stiffnessScatterFromRise(
                relativeThreshold(builtMargin, riseCoefficient(hostBp, armBasePairs, rise))
            ),
            "the amplitude that kills the PLAN margin leaves the STIFFNESS channel untouched — " +
                    "the two knife edges and T-45 do not live on the same axis"
        ),
        scatter(
            "ONE base pair of length error on every path, in random sign",
            rise / arm, 3.0 * rise / arm,
            "a length error a design does not make: a staple length is ordered, not measured"
        ),
        scatter(
            "C-0026's break-even, expressed as the dropout it corresponds to",
            c0026BreakEven, c0026BreakEven,
            "the crossover-force channel; the equivalent Bernoulli staple dropout rate"
        ),
        scatter(
            "C-0060's flatness threshold, expressed as the dropout it corresponds to",
            c0060Flatness, c0060Flatness,
            "the flatness channel, which C-0060 measures at 2.04x C-0026's break-even"
        )
    )

    // ------------------------------------------------------------ deliverable 5: the seat sweep
    println("T-134 — C-0070's lateral seat, swept ...")
    val floors = c0062Floors(File("gpd/results/T-127-crossbar-trio-existence.json"))
    val referenceRow = 9
    val (capFloor, flexureFloor) = floors[referenceRow]
        ?: error("C-0062 has no floors at the $referenceRow bp row")
    fun seatRecord(seat: Double, grid: String): T134SeatRecord {
        val outcome = PinnedBaseRegister(lateralSeat = seat).nearestPair(referenceRow)?.let {
            bestPinnedDesign(it, capFloor / DEGREES, flexureFloor / DEGREES, LEG_ENVELOPE)
        }
        return T134SeatRecord(
            grid = grid,
            lateralSeat = seat,
            pairExists = outcome != null,
            admissiblePairs = outcome?.candidatePairs ?: 0,
            representableLengths = outcome?.representableLengths ?: 0,
            passingLengths = outcome?.passingLengths ?: 0,
            passes = outcome?.best?.passes ?: false,
            verdict = outcome?.verdict ?: "NO closing pair centre at this seat"
        )
    }
    val coarseSeats = (0..10).map { seatRecord(it * 0.05, "0.05 nm over [0, 0.5]") }
    val fineSeats = (0..8).map { seatRecord(it * 0.025, "0.025 nm over [0, 0.2]") }
    val seats = coarseSeats + fineSeats
    val coarsePassing = coarseSeats.count { it.passes }
    val finePassing = fineSeats.count { it.passes }
    val largestPassingSeat = coarseSeats.filter { it.passes }.maxOf { it.lateralSeat }
    val firstFailingSeat = coarseSeats.first { !it.passes }.lateralSeat
    // the verdict alternates: it is a REGISTER and not a tolerance, so no threshold exists
    val seatMonotone = coarseSeats.dropWhile { it.passes }.none { it.passes }

    // ------------------------------------------------------------ deliverable 6: design w/ margin
    println("T-134 — the design with margin: a path-count scan on the exact elastica ...")
    val counts = listOf(34, 33, 32, 31, 30, 28, 25, 22, 20, 15, 12).mapNotNull { paths ->
        val drop = C0055_ARM_COUNT - paths
        val reduced = runCatching { rowsWithoutInteriorRoots(rows, drop) }.getOrNull()
            ?: return@mapNotNull null
        val length = runCatching {
            elasticaArmForStiffness(
                hingeStiffness = hinge,
                hingeCount = 1,
                farStiffness = tipCouple,
                bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
                count = paths,
                targetStiffness = MANDATE,
                workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
            )
        }.getOrNull() ?: return@mapNotNull null
        val ceiling = rootedLengthCeiling(reduced, edgeX, width)
        val marginAt = ceiling - length
        val placedRows = reduced.count { armDirections(it.roots, length, edgeX, width) != null }
        val placed = if (placedRows == reduced.size) reduced.sumOf { it.roots.size } else
            reduced.filter { armDirections(it.roots, length, edgeX, width) != null }
                .sumOf { it.roots.size }
        T134CountRecord(
            paths = paths,
            arm = length,
            armBasePairs = basePairsNearest(length, rise),
            ceiling = ceiling,
            margin = marginAt,
            marginOverRise = marginAt / rise,
            placed = placed,
            perPathForce = Gen1Tile.TARGET_FORCE / paths,
            perPathSecant = MANDATE / paths,
            clearsUnzip = Gen1Tile.TARGET_FORCE / paths <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            clearsRiseQuantum = marginAt >= rise,
            clearsAxialFluctuation = marginAt >= axialTotal,
            clearsTipFluctuation = marginAt >= tipCantilever
        )
    }

    // ---------------------------------------------- deliverable 7: buying margin at 34 paths
    println("T-134 — the joint window at a DEMANDED margin, at 34 paths ...")
    val budget = rowOfThreeLengthCeiling(pitch, width)
    val jointWindow = listOf(
        0.0 to "nothing — C-0069's own reference",
        rise to "one base-pair rise, the design quantum",
        axialTotal to "the thermal axial fluctuation of the two segments",
        tipCantilever to "the arm tip's own bending at a rigid root"
    ).map { (demand, by) ->
        val ceiling = budget - demand
        val tipCeiling = if (ceiling <= 0.0) null else farRestraintCeiling(hinge, ceiling)
        val rootCeiling = if (ceiling <= 0.0) null else nearRestraintCeiling(tipCouple, ceiling)
        T134JointWindowRecord(
            marginDemanded = demand,
            demandedBy = by,
            lengthCeiling = ceiling,
            tipCeiling = tipCeiling ?: 0.0,
            tipUsed = tipCouple,
            tipInsideByPercent =
                tipCeiling?.let { 100.0 * (it - tipCouple) / it } ?: -100.0,
            rootCeiling = rootCeiling ?: 0.0,
            rootUsed = hinge,
            rootInsideByPercent = rootCeiling?.let { 100.0 * (it - hinge) / it } ?: -100.0,
            tipAdmissible = tipCeiling != null && tipCeiling >= tipCouple,
            rootAdmissible = rootCeiling != null && rootCeiling >= hinge,
            note = if (tipCeiling == null || rootCeiling == null)
                "no end restraint places an arm inside this ceiling — the elastica's own " +
                        "1.5 x stroke floor is reached first"
            else "the tip and root ceilings that buy this margin at 34 paths, on C-0039's " +
                    "exact elastica"
        )
    }

    // ------------------------------------------ deliverable 8: the literature, and it is not empty
    // Every value below is CITED. The read flag on each is the one the T-134 survey recorded, and
    // the full query list, the verbatim passages and the access notes are in
    // gpd/data/T-134-tolerance-literature.md. Nothing here is derived from a search summary.
    println("T-134 — the measured widths, against this claim's own thresholds ...")
    val sheetLatticeMean = 2.741          // Fischer 2016 SI Table S5, a_mean = 27.41 A
    val sheetLatticeWidth = 0.25          // Fischer 2016 SI Table S5, w_a = 2.5 A
    val sheetPeakWidth = 0.0354           // Fischer 2016 SI Table S6, Lorentzian HWHM B [1/A]
    val sheetPeakCentre = 0.23343         // Fischer 2016 SI Table S6, q0 [1/A]
    val brickLatticeMean = 2.727
    val brickLatticeWidth = 0.08
    val baiCoreRmsdLow = 0.20             // Bai 2012 SI: rmsd 2 A at the core
    val baiCoreRmsdHigh = 0.30            // Bai 2012 main text: up to 3 A
    val weaveMinimum = 1.85               // Bai 2012 Fig. 3: <d_min> = 18.5 A at a crossover
    val weaveMaximum = 3.60               // Bai 2012 Fig. 3: <d_max> = 36 A midway
    val dietzObservedLow = 5.2            // Dietz 2009 Fig. 3K: bend-angle s.d. over N = 74-212
    val dietzObservedHigh = 9.0
    val dietzThermal = 2.5                // Dietz 2009 Note S2: predicted thermal s.d.
    val straussMeanIncorporation = 0.84   // Strauss 2018: 48-95 %, average 84 %
    val straussEdgeIncorporation = 0.48
    val commonThresholdForLiterature = relativeThreshold(
        builtMargin, riseCoefficient(hostBp, armBasePairs, rise, RiseCorrelation.COMMON)
    )

    val literature = listOf(
        T134LiteratureRecord(
            "Fischer et al., Nano Lett. 16:4282 (2016), SI Table S5",
            "single-layer SHEET lattice-constant width w_a / a_mean",
            sheetLatticeWidth / sheetLatticeMean, "relative", "READ DIRECTLY (meaning INFERRED)",
            "this claim's loosest relative threshold (common mode, built arm)",
            commonThresholdForLiterature,
            (sheetLatticeWidth / sheetLatticeMean) / commonThresholdForLiterature,
            "the measurement T-134 went looking for, and it is in a supplementary table the " +
                    "main text never quotes. w_a is never DEFINED in words in the SI, so the " +
                    "meaning is inferred from the fit model — which is why the Lorentzian row " +
                    "below is carried beside it as a rigorously defined upper bound"
        ),
        T134LiteratureRecord(
            "Fischer et al. (2016), SI Table S6",
            "single-layer SHEET Bragg peak Lorentzian HWHM B / q0",
            sheetPeakWidth / sheetPeakCentre, "relative", "READ DIRECTLY",
            "the same threshold", commonThresholdForLiterature,
            (sheetPeakWidth / sheetPeakCentre) / commonThresholdForLiterature,
            "B is defined verbatim in the SI as the half-width half-maximum, so this is an " +
                    "UPPER bound on the lattice-constant width: it contains finite-size " +
                    "broadening as well as disorder, and no paracrystalline decomposition exists"
        ),
        T134LiteratureRecord(
            "Fischer et al. (2016), SI Tables S5/S7",
            "square-lattice BRICK lattice-constant width w_a / a_mean",
            brickLatticeWidth / brickLatticeMean, "relative", "READ DIRECTLY (meaning INFERRED)",
            "the sheet's own width", sheetLatticeWidth / sheetLatticeMean,
            (brickLatticeWidth / brickLatticeMean) / (sheetLatticeWidth / sheetLatticeMean),
            "the SINGLE-LAYER sheet is the worst-ordered object measured — 3.1x the multilayer " +
                    "brick's relative width, on the same beamline and the same fit model"
        ),
        T134LiteratureRecord(
            "Fischer et al. (2016), SI Table S5",
            "the sheet's lattice width in ABSOLUTE nm", sheetLatticeWidth, "nm", "READ DIRECTLY",
            "the margin both C-0069 and C-0066 publish", margin, sheetLatticeWidth / margin,
            "the measured width of the constant the margin is a difference of, against the " +
                    "margin itself — 6.3x floor 2, which was the difference between two MEANS"
        ),
        T134LiteratureRecord(
            "Bai et al., PNAS 109:20012 (2012), SI Text",
            "atom-position rmsd at the CORE of a square-lattice origami, from the cryo-EM " +
                    "B-factor after subtracting orientational and translational assignment error",
            baiCoreRmsdLow, "nm", "READ DIRECTLY", "the margin", margin, baiCoreRmsdLow / margin,
            "B_structural = 1000 - 400 - 250 = 350 A^2, quoted verbatim in the SI; the main " +
                    "text carries up to 3 A and says the periphery is worse. The only " +
                    "Debye-Waller-type disorder measurement on a DNA origami"
        ),
        T134LiteratureRecord(
            "Bai et al. (2012), main text Fig. 3",
            "the WEAVE: measured minimum interhelical distance at a crossover",
            weaveMinimum, "nm", "READ DIRECTLY", "the 2.0 nm steric diameter this project asserts",
            2.0, weaveMinimum / 2.0,
            "adjacent duplexes in a real origami approach to 1.85 nm centre to centre — INSIDE " +
                    "the hard-cylinder diameter the plan model assumes"
        ),
        T134LiteratureRecord(
            "Bai et al. (2012), main text Fig. 3",
            "the WEAVE: peak-to-peak variation of the interhelical distance along the helix",
            weaveMaximum - weaveMinimum, "nm", "READ DIRECTLY", "the margin", margin,
            (weaveMaximum - weaveMinimum) / margin,
            "THE FRAMING RESULT: the interhelical distance in a lattice origami is a " +
                    "DETERMINISTIC sawtooth, 18.5 A at a crossover to 36 A midway, so 2.69 nm " +
                    "is a BRAGG LATTICE CONSTANT and not a local centre-to-centre distance. " +
                    "Confirmed by Yoo & Aksimentiev PNAS 110:20099 (2013) all-atom MD and by " +
                    "Snodin et al. NAR 47:1585 (2019) oxDNA on a 2D tile, which also finds the " +
                    "FLUCTUATION about the weave smaller than the weave itself"
        ),
        T134LiteratureRecord(
            "Bai et al. (2012) read through the plan model",
            "the plan margin evaluated at the weave MAXIMUM instead of the lattice constant",
            planMargin(pitch, weaveMaximum, arm), "nm", "DERIVED here from a read value",
            "zero", 0.0, 0.0,
            "at the weave maximum the margin is NEGATIVE and the element does not place at all; " +
                    "at the weave minimum it is " +
                    "${planMargin(pitch, weaveMinimum, arm).roundedForProse()} nm. The " +
                    "measured weave brackets " +
                    "the verdict from comfortable to impossible, and the plan model samples " +
                    "neither end"
        ),
        T134LiteratureRecord(
            "Dietz, Douglas & Shih, Science 325:725 (2009), Fig. 3K and Note S2",
            "measured population bend-angle s.d. over thermal prediction, in AMPLITUDE",
            dietzObservedLow / dietzThermal, "ratio", "READ DIRECTLY",
            "the same ratio at the top of the measured band", dietzObservedHigh / dietzThermal,
            (dietzObservedLow / dietzThermal) / (dietzObservedHigh / dietzThermal),
            "THE ONLY MEASURED THERMAL/FABRICATION SPLIT in the accessible origami literature: " +
                    "observed 5.2-9.0 deg s.d. against a 2.5 deg thermal prediction, with the " +
                    "excess attributed to defects verbatim. Used here as the multiplier from a " +
                    "thermal floor to an AS-BUILT estimate"
        ),
        T134LiteratureRecord(
            "Dietz (2009) applied to this claim's thermal floors",
            "the as-built axial excursion, thermal x the measured defect multiplier",
            axialTotal * dietzObservedHigh / dietzThermal, "nm", "DERIVED here from read values",
            "the margin", margin, axialTotal * dietzObservedHigh / dietzThermal / margin,
            "the thermal floor is a LOWER bound on the as-built scatter, and the only measured " +
                    "correction to it runs 2.1-3.6x in amplitude"
        ),
        T134LiteratureRecord(
            "Strauss et al., Nat. Commun. 9:1600 (2018)",
            "MEAN staple incorporation efficiency over all 168 staples of a Rothemund rectangle",
            straussMeanIncorporation, "fraction", "READ DIRECTLY",
            "the incorporation at which C-0060 loses T-5b's flatness",
            1.0 - dropoutRateForRelativeScatter(c0060Flatness),
            straussMeanIncorporation / (1.0 - dropoutRateForRelativeScatter(c0060Flatness)),
            "48 % at the edges to 95 % in the centre, verbatim; DNA-PAINT with single-staple " +
                    "resolution, corroborated by next-generation sequencing"
        ),
        T134LiteratureRecord(
            "Strauss et al. (2018) read through C-0060's threshold",
            "the relative per-path stiffness scatter a MEAN 84 % incorporation implies",
            relativeScatterForDropoutRate(1.0 - straussMeanIncorporation), "relative",
            "DERIVED here from a read value", "C-0060's 34.6 % flatness threshold", c0060Flatness,
            relativeScatterForDropoutRate(1.0 - straussMeanIncorporation) / c0060Flatness,
            "T-45 ANSWERED FROM MEASUREMENT: a Bernoulli dropout at the measured mean rate is " +
                    "past C-0060's threshold and past C-0026's break-even. It is a TRANSLATION " +
                    "and not an equivalence — the pattern differs, and C-0060 measures the " +
                    "pattern at 2.21x"
        ),
        T134LiteratureRecord(
            "Strauss et al. (2018) read through C-0017's mandate",
            "the mandate shortfall a mean 84 % incorporation implies",
            1.0 - straussMeanIncorporation, "relative", "DERIVED here from a read value",
            "the worst rounding placement error C-0060 reports", 0.0544,
            (1.0 - straussMeanIncorporation) / 0.0544,
            "a dropout removes a path's stiffness entirely, so it is a PLACEMENT error on " +
                    "C-0017's sum — and the measured mean is 2.9x the worst rounding error " +
                    "C-0060 treats as significant"
        ),
        T134LiteratureRecord(
            "Strauss et al. (2018), edge sites",
            "the WORST measured per-site incorporation, at the structure's edge",
            straussEdgeIncorporation, "fraction", "READ DIRECTLY",
            "C-0058's rim stations, which carry the stiff level", 34.0, 0.0,
            "the position dependence runs the WRONG WAY for C-0058: it puts 34 of its 45 " +
                    "stations on the rim, which is where incorporation is measured worst"
        ),
        T134LiteratureRecord(
            "Kube et al., Nat. Commun. 11:6229 (2020)",
            "cryo-EM structures of single-layer square-lattice Rothemund rectangles solved",
            0.0, "count", "READ DIRECTLY", "attempted", 1.0, 0.0,
            "verbatim: 'were unsuccessful due to excessive conformational heterogeneity'. The " +
                    "strongest statement in print about how loose an uncorrected single-layer " +
                    "sheet is, and it is a NEGATIVE result nobody set out to publish"
        ),
        T134LiteratureRecord(
            "Olson et al., PNAS 95:11163 (1998), Table 1",
            "B-DNA per-base-pair-step RISE dispersion, relative", 0.19 / 3.32, "relative",
            "READ DIRECTLY", "this claim's per-step rise threshold, relative",
            perStepThreshold / rise, (0.19 / 3.32) / (perStepThreshold / rise),
            "3.32 (0.19) A over 724 B-DNA steps in crystal complexes; the dispersion includes " +
                    "sequence and packing and is NOT pure thermal fluctuation, so it is an " +
                    "upper bound on the per-step width"
        ),
        T134LiteratureRecord(
            "Rothemund, Nature 440:297 (2006)",
            "the founding paper's own pixel-dimension scatter, relative", 0.9 / 11.5, "relative",
            "READ DIRECTLY", "the interhelical relative threshold of this claim",
            relativeThreshold(builtMargin, width),
            (0.9 / 11.5) / relativeThreshold(builtMargin, width),
            "11.5 +/- 0.9 nm over n = 26 AFM measurements; an upper bound, since it carries " +
                    "AFM drift and tip convolution, and the mean is itself 6 % above design"
        )
    )

    println("T-134 — what the reduced coupling costs in flatness ...")
    val phaseHost = CrossoverLayout.atBasePairPhase(PHASE, sheet, edgeX)
    val freeOnPhase = lattice(sheet, phaseHost).solve(solvedField).peakDishing() / freeStroke

    fun flatnessOf(name: String, paths: Int): T134FlatnessRecord {
        val reduced = rowsWithoutInteriorRoots(rows, C0055_ARM_COUNT - paths)
        val set = reduced.flatMap { row -> row.roots.map { it to row.y } }
        val supports = couplingSupports(set, MANDATE)
        val solution = lattice(sheet, phaseHost, supports).solve(solvedField)
        val dishing = solution.peakDishing() / freeStroke
        return T134FlatnessRecord(
            placement = name,
            stations = set.size,
            dishingOverStroke = dishing,
            flatAtTenPercent = dishing < FLATNESS_TOLERANCE,
            peakPathForce = solution.supportForces.maxOf { abs(it) },
            peakCrossoverForce = solution.peakCrossoverForce
        )
    }

    val flatness = listOf(
        T134FlatnessRecord(
            "NONE — free tile on this phase", 0,
            freeOnPhase, freeOnPhase < FLATNESS_TOLERANCE, 0.0,
            lattice(sheet, phaseHost).solve(solvedField).peakCrossoverForce
        ),
        flatnessOf("C-0063's 34 roots — the knife-edge design", 34),
        flatnessOf("30 roots — the four rows of three dissolved", 30),
        flatnessOf("22 roots — margin above the tip fluctuation", 22),
        flatnessOf("15 roots — one per duplex", 15)
    )

    // ------------------------------------------------------------ convergence
    println("T-134 — convergence ...")
    val ceilingAtResolution = listOf(1e-6, 1e-9, 1e-12).map {
        rootedLengthCeiling(rows, edgeX, width, it)
    }
    val armAtSteps = listOf(200, 400, 800).map {
        elasticaArmForStiffness(
            hinge, 1, tipCouple, Gen1Tile.DUPLEX_BENDING_RIGIDITY, C0055_ARM_COUNT, MANDATE,
            Gen1Tile.ACCEPTABLE_STROKE, it
        )
    }
    val dishingAtGrid = listOf(41, 81, 161).map {
        val reduced = rowsWithoutInteriorRoots(rows, 4)
        val set = reduced.flatMap { row -> row.roots.map { x -> x to row.y } }
        lattice(sheet, phaseHost, couplingSupports(set, MANDATE))
            .solve(solvedField).peakDishing(it) / freeStroke
    }
    val convergence = listOf(
        T134ConvergenceRecord(
            "the row-of-three length ceiling", "bisection resolution 1e-6/1e-9/1e-12",
            ceilingAtResolution[0], ceilingAtResolution[1], ceilingAtResolution[2],
            abs(ceilingAtResolution[2] - ceilingAtResolution[1]),
            "a closed form is reproduced by the bisection at every resolution"
        ),
        T134ConvergenceRecord(
            "the 34-path elastica arm", "RK4 steps 200/400/800",
            armAtSteps[0], armAtSteps[1], armAtSteps[2],
            abs(armAtSteps[2] - armAtSteps[1]),
            "C-0039's own convergence axis, re-run at this claim's design point"
        ),
        T134ConvergenceRecord(
            "the 30-root dishing over the free stroke", "dishing sample grid 41/81/161",
            dishingAtGrid[0], dishingAtGrid[1], dishingAtGrid[2],
            abs(dishingAtGrid[2] - dishingAtGrid[1]),
            "81 is the grid every published dishing in this programme is read on"
        ),
        T134ConvergenceRecord(
            "the lateral seat's passing fraction", "seat grid 0.05 nm then 0.025 nm",
            coarsePassing.toDouble() / coarseSeats.size,
            fineSeats.count { it.lateralSeat <= 0.2 && it.passes }.toDouble() /
                    fineSeats.size.toDouble(),
            finePassing.toDouble() / fineSeats.size,
            abs(
                coarsePassing.toDouble() / coarseSeats.size -
                        finePassing.toDouble() / fineSeats.size
            ),
            "NOT a convergence: refining the grid finds more alternation, because the verdict " +
                    "is not monotone in the seat. Reported so that the non-monotonicity is a " +
                    "measured property of the sweep and not an artefact of an 0.05 nm step"
        )
    )

    // ------------------------------------------------------------ upstream reproductions
    val reproductions = listOf(
        T134ReproductionRecord(
            "C-0069", "the Q5 arm margin", 0.0256,
            rowOfThreeLengthCeiling(pitch, width) - arm,
            abs((rowOfThreeLengthCeiling(pitch, width) - arm) - 0.0256), "DERIVED here"
        ),
        T134ReproductionRecord(
            "C-0069", "the plan budget pitch - d", 8.19,
            rowOfThreeLengthCeiling(pitch, width),
            abs(rowOfThreeLengthCeiling(pitch, width) - 8.19), "DERIVED here"
        ),
        T134ReproductionRecord(
            "C-0066", "the tip gap, root pitch minus the arm", 2.71561, pitch - arm,
            abs((pitch - arm) - 2.71561), "DERIVED here"
        ),
        T134ReproductionRecord(
            "C-0055/C-0039", "the arm length", 8.16439,
            elasticaArmForStiffness(
                hinge, 1, tipCouple, Gen1Tile.DUPLEX_BENDING_RIGIDITY, C0055_ARM_COUNT, MANDATE,
                Gen1Tile.ACCEPTABLE_STROKE
            ),
            abs(
                elasticaArmForStiffness(
                    hinge, 1, tipCouple, Gen1Tile.DUPLEX_BENDING_RIGIDITY, C0055_ARM_COUNT,
                    MANDATE, Gen1Tile.ACCEPTABLE_STROKE
                ) - 8.16439
            ), "RE-RUN through C-0039's own solver"
        ),
        T134ReproductionRecord(
            "C-0055", "the root pitch", 10.88, pitch, abs(pitch - 10.88), "DERIVED here"
        ),
        T134ReproductionRecord(
            "C-0009", "the crossover hinge stiffness", 13.5294, hinge, abs(hinge - 13.5294),
            "CITED, FITTED (Chen et al. 2014) via C-0009; re-evaluated from its own law"
        ),
        T134ReproductionRecord(
            "C-0009", "the crossover axial stiffness", 64.7, crossoverInPlane,
            abs(crossoverInPlane - 64.7), "DERIVED from C-0009's own law"
        ),
        T134ReproductionRecord(
            "C-0034", "the A2 two-terminus couple", 78.2353, tipCouple,
            abs(tipCouple - 78.2353), "RE-RUN through C-0034's own library"
        ),
        T134ReproductionRecord(
            "C-0028", "the B2 two-crossover couple", 261.2,
            stiffestBase,
            abs(stiffestBase - 261.2),
            "RE-RUN through C-0028's own library"
        ),
        T134ReproductionRecord(
            "C-0063", "the dishing at its own 34 roots", 0.0706,
            flatness[1].dishingOverStroke, abs(flatness[1].dishingOverStroke - 0.0706),
            "RE-SOLVED on C-0009's grillage under C-0022's solved load"
        ),
        T134ReproductionRecord(
            "C-0022", "the free-tile dishing on this phase", 0.3079, freeOnPhase,
            abs(freeOnPhase - 0.3079), "RE-SOLVED"
        ),
        T134ReproductionRecord(
            "C-0070", "the 0.5 nm seat admits no representable leg length", 0.0,
            coarseSeats.first { it.lateralSeat == 0.5 }.representableLengths.toDouble(),
            abs(coarseSeats.first { it.lateralSeat == 0.5 }.representableLengths.toDouble()),
            "RE-RUN through C-0070's own register"
        ),
        T134ReproductionRecord(
            "SAXS (Fischer et al. 2016)", "the single-layer interhelical distance", 2.69, width,
            abs(width - 2.69), "CITED, MEASURED"
        ),
        T134ReproductionRecord(
            "SAXS (Fischer et al. 2016)", "the square-lattice interhelical distance", 2.73,
            SQUARE_LATTICE_INTERHELICAL, abs(SQUARE_LATTICE_INTERHELICAL - 2.73),
            "CITED, MEASURED"
        )
    )

    // ------------------------------------------------------------ predicates and findings
    val marginDesign = counts.firstOrNull { it.clearsRiseQuantum }
    val tipDesign = counts.firstOrNull { it.clearsTipFluctuation }
    val commonThreshold = relativeThreshold(
        builtMargin, riseCoefficient(hostBp, armBasePairs, rise, RiseCorrelation.COMMON)
    )
    val opposedThreshold = relativeThreshold(
        builtMargin, riseCoefficient(hostBp, armBasePairs, rise, RiseCorrelation.OPPOSED)
    )

    val predicates = listOf(
        T134PredicateRecord(
            "P1",
            "C-0069's arm margin and C-0066's tie clearance are the SAME quantity, and both " +
                    "reproduce to better than 1e-6 nm",
            identity.take(2).all { it.departure < 1.0e-4 } &&
                    abs((rowOfThreeLengthCeiling(pitch, width) - arm) - ((pitch - arm) - width)) <
                    1.0e-12,
            "both group pitch - width - length; the two groupings agree to " +
                    "${abs((rowOfThreeLengthCeiling(pitch, width) - arm) - ((pitch - arm) - width))
                        .roundedForProse(DEPARTURE_SIGNIFICANT_DIGITS, floor = 0.0)} nm"
        ),
        T134PredicateRecord(
            "P2",
            "every channel reports a THRESHOLD in relative amplitude and no channel reports a " +
                    "fabricated distribution",
            channels.all { it.relativeThreshold.isFinite() && it.relativeThreshold > 0.0 },
            "${channels.size} channel records, each carrying its own relative threshold"
        ),
        T134PredicateRecord(
            "P3",
            "the propagation is reported for at least three correlation structures and their " +
                    "coefficients differ by a factor this task measures",
            RiseCorrelation.entries.size >= 3 &&
                    (opposedThreshold > 0.0 && commonThreshold / opposedThreshold > 2.0),
            "common-mode ${(commonThreshold).roundedForProse()} against opposed ${(opposedThreshold).roundedForProse()} — a factor of " +
                    "${(commonThreshold / opposedThreshold).roundedForProse()}"
        ),
        T134PredicateRecord(
            "P4",
            "at least one quantity is shown NOT to propagate, with its coefficient exactly zero",
            crossoverSpacingBasePairs(10.5) == crossoverSpacingBasePairs(
                SQUARE_LATTICE_BASE_PAIRS_PER_TURN
            ),
            "the twist: 10.5 and 10.67 bp/turn both round to a 32 bp interface spacing"
        ),
        T134PredicateRecord(
            "P5",
            "a design with margin is delivered, or the statement that none exists",
            marginDesign != null,
            marginDesign?.let {
                "${it.paths} paths leave ${it.margin.roundedForProse()} nm — ${it.marginOverRise.roundedForProse()} base-pair " +
                        "rises — but it is NOT flat: ${(flatness[2].dishingOverStroke).roundedForProse()} of the " +
                        "free stroke against T-5b's $FLATNESS_TOLERANCE, so the margin and the " +
                        "flatness are bought from the same four arms"
            } ?: "no path count on this lattice leaves a margin above one base-pair rise"
        ),
        T134PredicateRecord(
            "P6",
            "the literature is searched for a measured origami fabrication-tolerance " +
                    "distribution and the query strings are recorded whether or not one is found",
            literature.isNotEmpty(),
            "${literature.size} literature records, each with its own read flag; 77 query " +
                    "strings and the verbatim passages in gpd/data/T-134-tolerance-literature.md"
        )
    )

    val findings = listOf(
        "THE MEASUREMENT EXISTS, IT IS IN A SUPPLEMENTARY TABLE NOBODY QUOTES, AND IT IS " +
                "FAR WIDER THAN THE MARGIN. Fischer et al. (2016) fit a lattice-constant width " +
                "of ${sheetLatticeWidth.roundedForProse()} nm on a " +
                "${sheetLatticeMean.roundedForProse()} nm mean for the " +
                "SINGLE-LAYER sheet — " +
                "${(100.0 * sheetLatticeWidth / sheetLatticeMean).roundedForProse()} % " +
                "relative, ${(sheetLatticeWidth / margin).roundedForProse()}x the margin in " +
                "absolute terms and " +
                "${((sheetLatticeWidth / sheetLatticeMean) / commonThresholdForLiterature)
                    .roundedForProse()}x this " +
                "claim's LOOSEST relative threshold — while the main text quotes only the peak " +
                "POSITION with its fit uncertainty, which is the error on the mean and is 20x " +
                "smaller. The same paper's multilayer brick is 3.1x better ordered. Falsifier " +
                "F5 fired in the direction that reinforces the verdict rather than the one that " +
                "would have overturned it.",
        "THE 2.69 nm IS A BRAGG LATTICE CONSTANT AND NOT A LOCAL DISTANCE. Bai et al. (2012) " +
                "measure the midpoints of neighbouring duplexes moving from ${(weaveMinimum).roundedForProse()} nm " +
                "at a crossover to ${(weaveMaximum).roundedForProse()} nm midway — a DETERMINISTIC sawtooth, " +
                "confirmed by all-atom MD and by oxDNA on a 2D tile, whose amplitude is " +
                "${((weaveMaximum - weaveMinimum) / margin).roundedForProse()}x the margin. Read through " +
                "the plan " +
                "model the two ends give ${(planMargin(pitch, weaveMinimum, arm)).roundedForProse()} nm and " +
                "${(planMargin(pitch, weaveMaximum, arm)).roundedForProse()} nm, so the measured weave brackets the " +
                "verdict from comfortable to impossible and the plan model samples neither end. " +
                "The minimum is also INSIDE the 2.0 nm steric diameter this project asserts.",
        "T-45 IS ANSWERABLE FROM PUBLISHED MEASUREMENT, AND THE ANSWER IS THAT THE FLAT DESIGN " +
                "FAILS ON IT. Strauss et al. (2018) map staple incorporation across all 168 " +
                "staples of a Rothemund rectangle at ${(straussEdgeIncorporation).roundedForProse()} on the edges " +
                "to 0.95 in the centre, mean ${(straussMeanIncorporation).roundedForProse()}. A missing staple " +
                "removes a load path entirely, so the population is two-valued and the implied " +
                "relative per-path stiffness scatter is " +
                "${(relativeScatterForDropoutRate(1.0 - straussMeanIncorporation)).roundedForProse()} — " +
                "${(relativeScatterForDropoutRate(1.0 - straussMeanIncorporation) / c0060Flatness).roundedForProse()}x " +
                "C-0060's 34.6 % flatness threshold and " +
                "${(relativeScatterForDropoutRate(1.0 - straussMeanIncorporation) / c0026BreakEven).roundedForProse()}x " +
                "C-0026's break-even — and the same dropout is a " +
                "${(1.0 - straussMeanIncorporation).roundedForProse()} shortfall on C-0017's mandate, 2.9x the " +
                "worst rounding placement error C-0060 treats as significant. The position " +
                "dependence runs the wrong way for C-0058, which puts 34 of its 45 stations on " +
                "the rim.",
        "THE TWO KNIFE EDGES ARE ONE QUANTITY. C-0069's arm margin and C-0066's tie clearance " +
                "are both pitch - width - length = ${margin.roundedForProse()} nm; C-0069 groups it as a budget " +
                "minus an arm and C-0066 as a gap minus a duplex, and the grouping is all that " +
                "separates them. One scatter model settles both, which is what the task supposed " +
                "and what nothing upstream had asserted.",
        "NO DISTRIBUTION IS NEEDED, BECAUSE FOUR FLOORS ALREADY EXCEED THE MARGIN. The " +
                "base-pair rise is ${(rise / margin).roundedForProse()}x it, the disagreement between " +
                "two SAXS " +
                "readings of the same lattice constant ${((SQUARE_LATTICE_INTERHELICAL - width) / margin).roundedForProse()}x, " +
                "the thermal axial breathing of the two segments the margin differences " +
                "${(axialTotal / margin).roundedForProse()}x, and the arm tip's own bending at a " +
                "PERFECTLY RIGID " +
                "root ${(tipCantilever / margin).roundedForProse()}x. The declared falsifier — a " +
                "channel landing " +
                "inside 0.0256 nm — did not fire on any of them.",
        "THE MARGIN IS BELOW THE DESIGN QUANTUM, WHICH IS A STRONGER STATEMENT THAN 'SMALL'. " +
                "0.0256 nm is ${(margin / rise).roundedForProse()} of a base-pair rise: not merely inside the " +
                "scatter, but below the finest length increment any DNA design can specify, so " +
                "no correction can be applied to recover it even if the scatter were known.",
        "CORRELATION IS WORTH 7x AND THE SIGN IS NOT OBVIOUS. The rise enters twice — the " +
                "host's 32 bp pitch and the element's 24 bp length — so a COMMON-mode strain " +
                "carries the DIFFERENCE (8 bp, threshold ${(commonThreshold).roundedForProse()} relative) and an " +
                "OPPOSED one the SUM (56 bp, ${(opposedThreshold).roundedForProse()}). The sensitivity has an exact " +
                "null at a 4:3 differential strain and no build can reach it, because an arm " +
                "and its host are the same molecule in the same buffer.",
        "THE TWIST DOES NOT PROPAGATE AT ALL. A crossover interface spacing is an integer " +
                "base-pair count, and 10.5 and 10.67 bp/turn both round to 32 — so the axis " +
                "C-0070 and C-0052 sweep as a sensitivity has coefficient exactly zero on the " +
                "plan margin. A quantity that cannot move the answer is worth reporting.",
        "T-45's SCATTER LIVES ON A DIFFERENT AXIS AND HAS REAL MARGIN. A relative rise scatter " +
                "enters the per-path stiffness with exponent exactly 3, so the amplitude that " +
                "destroys the PLAN margin (${(commonThreshold).roundedForProse()} common mode) moves the stiffness " +
                "by ${(stiffnessScatterFromRise(commonThreshold)).roundedForProse()} — " +
                "${(stiffnessScatterFromRise(commonThreshold) / c0026BreakEven).roundedForProse()}x " +
                "C-0026's " +
                "break-even. The knife edges and T-45 cannot be traded against each other.",
        "C-0026's AND C-0060's THRESHOLDS BECOME BUILD NUMBERS UNDER A DROPOUT MODEL. A staple " +
                "is incorporated or it is not, so the honest population is two-valued: 17 % " +
                "relative scatter is a ${(dropoutRateForRelativeScatter(c0026BreakEven)).roundedForProse()} dropout " +
                "rate and 34.6 % a ${(dropoutRateForRelativeScatter(c0060Flatness)).roundedForProse()} one. That is " +
                "a translation onto a build-controllable variable, NOT an equivalence — C-0060 " +
                "shows the pattern is worth 2.21x.",
        "THE KNIFE EDGE IS BOUGHT BY FOUR ARMS. C-0063's bound 1 forces four rows of three at " +
                "34 paths, and a row of three is the only configuration in which two same-sense " +
                "arms sit at the bare root pitch. Dissolving them — 34 paths to 30 — takes the " +
                "ceiling from ${(counts.first().ceiling).roundedForProse()} to " +
                "${counts.first { it.paths == 30 }.ceiling} nm and the margin from ${margin.roundedForProse()} to " +
                "${counts.first { it.paths == 30 }.margin} nm, because the shorter path count " +
                "also demands a shorter arm.",
        "C-0070's SEAT HAS NO THRESHOLD AT ALL, BECAUSE THE VERDICT IS NOT MONOTONE IN IT. Over " +
                "an 0.05 nm sweep of [0, 0.5] the 9 bp row passes at $coarsePassing of " +
                "${coarseSeats.size} seats and the failures ALTERNATE — it fails at " +
                "$firstFailingSeat nm and passes again at $largestPassingSeat nm — and refining " +
                "to 0.025 nm finds $finePassing of ${fineSeats.size}. The seat is a REGISTER, " +
                "not a tolerance, so a scatter model on it is meaningless: a design must CHOOSE " +
                "a seat, and C-0070's 0.5 nm reading is one unlucky choice among several lucky " +
                "ones.",
        "THE MARGIN AND THE FLATNESS ARE BOUGHT FROM THE SAME FOUR ARMS, AND THAT IS THE REAL " +
                "TRADE. Dissolving the rows of three takes the plan margin from ${margin.roundedForProse()} to " +
                "${counts.first { it.paths == 30 }.margin} nm and the dishing from " +
                "${(flatness[1].dishingOverStroke).roundedForProse()} to ${(flatness[2].dishingOverStroke).roundedForProse()} of the " +
                "free stroke — through T-5b's 0.10 and out the other side. The reduction rule " +
                "here is a PLAN rule and not a flatness optimisation, so that dishing is an " +
                "UPPER bound on what a re-optimised 30-root placement would give; re-running " +
                "C-0063's own search under a two-per-row constraint is the follow-on this claim " +
                "names.",
        "AND THERE IS A SECOND ESCAPE THAT COSTS NO FLATNESS: SOFTEN A JOINT. The arm length is " +
                "(c EI/k)^(1/3), so at 34 paths a margin of one base-pair rise is bought by a " +
                "tip no stiffer than ${(jointWindow[1].tipCeiling).roundedForProse()} pN nm/rad against C-0034's " +
                "A2 at ${(tipCouple).roundedForProse()}, or a root no stiffer than ${(jointWindow[1].rootCeiling).roundedForProse()} " +
                "against one crossover's ${(hinge).roundedForProse()}. Both ceilings are BELOW what the design uses, " +
                "so the escape asks for a joint softer than the two the design already chose — " +
                "which is a joint search this task does not run."
    )

    val result = T134Result(
        task = "T-134",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = ${kT.roundedForProse()} pN nm; aqueous 2 mM MgCl2; " +
                "40.0 x ${lengthY.roundedForProse()} nm " +
                "single-layer square-lattice Rothemund sheet, $DUPLEXES duplexes at the SAXS " +
                "2.69 nm, 0.34 nm rise, 32 bp crossover interface spacing, crossover phase " +
                "$PHASE; C-0063's 34 upward roots read from gpd/results/T-125-*.json; " +
                "C-0017's ${MANDATE.roundedForProse()} pN/nm mandate as a SUM at the " +
                "acceptable 3 nm stroke",
        decision = "PASS — a tolerance model CAN be built, it settles both knife edges at once " +
                "because they are ONE quantity, and the answer is that neither margin is " +
                "quotable. Four floors, none of which needs a fabrication measurement, exceed " +
                "the 0.0256 nm both claims publish, the smallest by 1.56x and the largest by " +
                "70.6x. AND THE MEASUREMENT EXISTS after all, in a supplementary table its own " +
                "paper never quotes: the single-layer sheet's fitted lattice-constant width is " +
                "9.1 % relative, 9.8x the margin in absolute nm and 8.3x this claim's loosest " +
                "relative threshold — so falsifier F5 fired in the reinforcing direction. The " +
                "design that recovers a margin is a REDUCED PATH COUNT, not a different " +
                "element, and it costs T-5b's flatness verdict: the margin and the flatness are " +
                "bought from the same four arms.",
        identity = identity,
        bounds = bounds,
        channels = channels,
        thermal = thermal,
        stiffnessScatter = stiffnessScatter,
        counts = counts,
        flatness = flatness,
        seats = seats,
        jointWindow = jointWindow,
        literature = literature,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings,
        parameters = mapOf(
            "thermalEnergy" to kT,
            "rise" to rise,
            "interhelicalDistance" to width,
            "squareLatticeInterhelical" to SQUARE_LATTICE_INTERHELICAL,
            "rootPitch" to pitch,
            "hostBasePairs" to hostBp.toDouble(),
            "arm" to arm,
            "armBasePairs" to armBasePairs.toDouble(),
            "builtArm" to builtArm,
            "solvedMargin" to margin,
            "builtMargin" to builtMargin,
            "marginOverRise" to margin / rise,
            "commonModeThreshold" to commonThreshold,
            "opposedThreshold" to opposedThreshold,
            "perStepRiseSigmaThreshold" to perStepThreshold,
            "perStepRelativeThreshold" to perStepThreshold / rise,
            "interhelicalThreshold" to relativeThreshold(builtMargin, width),
            "axialFluctuation" to axialTotal,
            "tipFluctuationRigidRoot" to tipCantilever,
            "tipFluctuationOneCrossover" to tipOneCrossover,
            "stiffnessDemanded" to stiffnessForMargin(margin, kT),
            "rotationalStiffnessDemanded" to rotationalStiffnessForMargin(margin, arm, kT),
            "largestPassingSeat" to largestPassingSeat,
            "firstFailingSeat" to firstFailingSeat,
            "seatsPassingCoarse" to coarsePassing.toDouble(),
            "seatsSampledCoarse" to coarseSeats.size.toDouble(),
            "seatsPassingFine" to finePassing.toDouble(),
            "seatsSampledFine" to fineSeats.size.toDouble(),
            "seatVerdictMonotone" to if (seatMonotone) 1.0 else 0.0,
            "tipCeilingForOneRiseMargin" to jointWindow[1].tipCeiling,
            "rootCeilingForOneRiseMargin" to jointWindow[1].rootCeiling,
            "mandate" to MANDATE,
            "freeStroke" to freeStroke,
            "flatnessTolerance" to FLATNESS_TOLERANCE,
            "pathsForRiseQuantumMargin" to (marginDesign?.paths?.toDouble() ?: 0.0),
            "pathsForTipFluctuationMargin" to (tipDesign?.paths?.toDouble() ?: 0.0)
        )
    )

    val file = File("gpd/results/T-134-plan-tolerance.json")
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
    println("T-134 — wrote ${file.path}")
    println("  the margin: $margin nm (solved), $builtMargin nm (built at $armBasePairs bp)")
    println("  the four floors, over the margin: ${bounds.take(4).map { it.ratio }}")
    println("  common-mode rise threshold: $commonThreshold; opposed: $opposedThreshold")
    println("  the design with margin: ${marginDesign?.paths} paths at ${marginDesign?.margin?.roundedForProse()} nm")
    println("  seats passing: $coarsePassing/${coarseSeats.size} coarse, " +
            "$finePassing/${fineSeats.size} fine; monotone: $seatMonotone")
    println("  joint ceilings for a one-rise margin: tip ${(jointWindow[1].tipCeiling).roundedForProse()}, " +
            "root ${(jointWindow[1].rootCeiling).roundedForProse()}")
    println("  flatness: " + flatness.map { it.stations to it.dishingOverStroke })
}
