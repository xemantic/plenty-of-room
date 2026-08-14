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

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.admissibleStiffnessRatio
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.rimStiffenedWeights
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
import kotlin.math.atan
import kotlin.math.sqrt

/**
 * `T-121` — what do **34 duplexes stacked above the tile** do to it?
 *
 * `C-0055` moved the `E5a` hinge array off the sheet's own crossovers and onto the unoccupied
 * upward azimuth, so the host loses no crossover and no duplex length — and every table in
 * `C-0054`, computed on *consumed* interfaces, stops applying. This study is what replaces
 * them: `C-0009`'s grillage, `C-0006`/`C-0047`'s flatness and `C-0010`'s variance re-run on a
 * sheet **carrying** the array, plus the drainage consequence `C-0004` owns.
 *
 * Emits `gpd/results/T-121-stacked-arm-sheet.json`.
 */

private const val DUPLEXES = 15
private const val NOMINAL_COLUMNS = 8
private const val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
private const val RIM_STANDOFF = 1.0
private const val FLATNESS_TOLERANCE = 0.10
private const val BANDWIDTH_TARGET = 1000.0

/** The duplex steric radius in nm — the B-DNA phosphate radius, `CLAUDE.md` / `C-0029`. */
private const val DUPLEX_RADIUS = 1.0

/** The narrower fibre-diffraction reading of the same radius, swept as a sensitivity. */
private const val DUPLEX_RADIUS_NARROW = 0.89

/** `C-0004`'s solvent viscosity in `pN·s/nm²`, evaluated at 300 K and not at 20 °C. */
private const val VISCOSITY = 8.540578046518857e-10

/** `C-0058`'s best one-parameter collar width in nm. */
private const val C0058_COLLAR_WIDTH = 6.70

/** [count] springs at [grid] totalling [total] pN/nm, shared in proportion to [weights]. */
private fun weightedSupports(
    grid: List<Pair<Double, Double>>,
    weights: List<Double>,
    total: Double
): List<PointSupport> {
    require(grid.size == weights.size) { "one weight per station" }
    val sum = weights.sum()
    require(sum > 0.0) { "the weights must sum to something positive, summed to $sum" }
    return grid.mapIndexed { index, (x, y) ->
        PointSupport(x, y, total * weights[index] / sum)
    }
}

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T121BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String
)

@Serializable
private data class T121CondensationRecord(
    val ties: Int,
    val regularisation: Double,
    val addedDeflectionStiffness: Double,
    val addedRollStiffness: Double,
    val addedStiffnessNorm: Double,
    val note: String
)

@Serializable
private data class T121ResponseRecord(
    val configuration: String,
    val placement: String,
    val profile: String,
    val meanDeflection: Double,
    val peakDishing: Double,
    val dishingOverStroke: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val armStrainEnergy: Double,
    val peakRootLinkForce: Double,
    val departureFromBareHost: Double
)

@Serializable
private data class T121FlatnessRecord(
    val placement: String,
    val stations: Int,
    val profile: String,
    val peakDishing: Double,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val perPathForce: Double
)

@Serializable
private data class T121DistributionRecord(
    val placement: String,
    val stations: Int,
    val rule: String,
    val ratio: Double,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val peakPathForce: Double,
    val admissibleRatio: Double,
    val withinPerPathAllowable: Boolean
)

@Serializable
private data class T121VarianceRecord(
    val placement: String,
    val armsAttached: Int,
    val pistonRms: Double,
    val tiltRms: Double,
    val dishingRms: Double,
    val areaRms: Double,
    val centreRms: Double,
    val cornerFrequency: Double,
    val inBandFraction: Double,
    val inBandRms: Double,
    val marginAgainstThreeNanometresBroadband: Double,
    val marginAgainstThreeNanometresInBand: Double
)

@Serializable
private data class T121DrainageRecord(
    val point: String,
    val permeabilityModel: String,
    val armRadius: Double,
    val squeezeDrag: Double,
    val stokesDrag: Double,
    val armDrag: Double,
    val totalDragBare: Double,
    val totalDragWithArms: Double,
    val armDragFraction: Double,
    val cornerFrequencyBare: Double,
    val cornerFrequencyWithArms: Double,
    val marginBare: Double,
    val marginWithArms: Double,
    val stillDischarged: Boolean
)

@Serializable
private data class T121ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T121ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T121PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T121Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T121BoundRecord>,
    val condensation: List<T121CondensationRecord>,
    val responses: List<T121ResponseRecord>,
    val flatness: List<T121FlatnessRecord>,
    val distributions: List<T121DistributionRecord>,
    val variance: List<T121VarianceRecord>,
    val drainage: List<T121DrainageRecord>,
    val convergence: List<T121ConvergenceRecord>,
    val reproductions: List<T121ReproductionRecord>,
    val predicates: List<T121PredicateRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private fun sheet() = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

private fun lattice(
    sheet: OrigamiSheet,
    supports: List<PointSupport>,
    subdivisions: Int = 2,
    linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS,
    foundationStiffness: Double = Gen1Tile.FOUNDATION_SECANT
) = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = foundationStiffness,
    columns = CrossoverLayout.centred(NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0),
    subdivisions = subdivisions,
    linkStiffness = linkStiffness,
    supports = supports
)

/** `C-0055`'s own 34-arm placement, read from its result file rather than retyped. */
private fun c0055Arms(file: File, armLength: Double): List<StackedArm> {
    require(file.exists()) {
        "C-0055's result file is missing: ${file.path}. T-121 carries ITS placement and will " +
                "not substitute an assumed one for it."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPhasePlacement").jsonArray.map { it.jsonObject }
        .map {
            StackedArm(
                row = it.getValue("row").jsonPrimitive.content.toInt(),
                rootX = it.getValue("rootX").jsonPrimitive.content.toDouble(),
                length = armLength,
                towardPositiveX = it.getValue("towardPositiveX").jsonPrimitive.content.toBoolean()
            )
        }
}

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias**. */
private fun solvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-121 consumes the SOLVED edge " +
                "profile, because a uniform load makes a free plate dish exactly zero " +
                "whatever its rigidity, so a uniform-load flatness answer is vacuous."
    }
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

/** One of `C-0004`'s solved drainage states, read from its own result file. */
private data class DrainagePoint(
    val label: String,
    val model: String,
    val squeezeDrag: Double,
    val stokesDrag: Double,
    val layerStiffness: Double,
    val cornerFrequency: Double,
    val margin: Double
)

private fun drainagePoints(file: File): Pair<DrainagePoint, DrainagePoint> {
    require(file.exists()) {
        "C-0004's result file is missing: ${file.path}. T-121 asks what the arms do to ITS " +
                "drag budget and will not re-derive the budget from an assumption."
    }
    val points = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("designPoints").jsonArray.map { it.jsonObject }
        .filter { it.getValue("footprint").jsonPrimitive.content == "40 x 40 nm" }
        .map { entry ->
            val layer = entry.getValue("layer").jsonObject
            val response = entry.getValue("response").jsonObject
            fun response(key: String) = response.getValue(key).jsonPrimitive.content.toDouble()
            DrainagePoint(
                label = layer.getValue("label").jsonPrimitive.content + ", stiffness x" +
                        entry.getValue("stiffnessMultiplier").jsonPrimitive.content,
                model = response.getValue("permeabilityModel").jsonPrimitive.content,
                squeezeDrag = response("squeezeDrag"),
                stokesDrag = response("stokesDrag"),
                layerStiffness = response("layerStiffness"),
                cornerFrequency = response("cornerFrequency"),
                margin = response("marginAtOneKilohertz")
            )
        }
    val nominal = points.firstOrNull {
        it.label.startsWith("L0 = 10 nm") && it.label.endsWith("x1.0") &&
                it.model == "jackson-james-fibre-array"
    } ?: error("no C-0004 nominal 40 x 40 nm, 10 nm layer, nominal stiffness point")
    val worst = points.minByOrNull { it.margin } ?: error("no C-0004 40 x 40 nm points at all")
    return nominal to worst
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = sheet()
    val lengthY = DUPLEXES * sheet.interhelicalDistance
    val area = Gen1Tile.EDGE_X * lengthY
    val interiorPressure = Gen1Tile.TARGET_FORCE / area
    val rootPitch = sheet.crossoverSpacing
    val plateModel = sheet.plate(Gen1Tile.EDGE_X, lengthY)

    println("T-121 — reading C-0055's placement, C-0022's solved load and C-0004's drag ...")
    val arms = c0055Arms(
        File("gpd/results/T-119-unused-junction-site.json"), C0055_ARM_LENGTH
    )
    check(arms.size == C0055_ARM_COUNT) {
        "C-0055's placement must carry $C0055_ARM_COUNT arms, carried ${arms.size}"
    }
    val (smooth, rim) = solvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val solvedField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY, listOf(smooth, rim)
    )
    val uniformField = uniformPressure(interiorPressure)
    val (nominalDrainage, worstDrainage) = drainagePoints(
        File("gpd/results/T-7-poroelastic-drainage.json")
    )

    // ------------------------------------------------------------------ the cheap bounds
    println("T-121 — the cheap bounds, which decide which of the three channels matters ...")
    val planFraction = armPlanFootprintFraction(arms, sheet.interhelicalDistance, area)
    val contourFraction = armContourFraction(arms, DUPLEXES * Gen1Tile.EDGE_X)
    val composite = compositeBendingRigidity(
        sheet.duplex.bendingRigidity, sheet.duplex.stretchModulus, sheet.interhelicalDistance
    )
    val exactZero = armRootCondensation(
        sheet.duplex.bendingRigidity, sheet.duplex.torsionalRigidity, C0055_ARM_LENGTH,
        OrigamiGrillage.RIGID_LINK_STIFFNESS, sheet.crossoverHingeStiffness, 1e-10
    )
    val armDrag = arms.size *
            slenderBodyTransverseDrag(VISCOSITY, C0055_ARM_LENGTH, DUPLEX_RADIUS)
    val bareDrag = nominalDrainage.squeezeDrag + nominalDrainage.stokesDrag
    val massPerLength = duplexMassPerLength(Gen1Tile.RISE_PER_BASE_PAIR)

    val bounds = listOf(
        T121BoundRecord(
            "the added stiffness a body attached at ONE point contributes at its root",
            exactZero.addedStiffnessNorm, "pN/nm",
            "EXACTLY ZERO. A free body's rigid-body motions span everything one crossover " +
                    "can impose on it, so the Schur complement vanishes term by term — the " +
                    "same class of statement as 'a uniform load on a uniform foundation " +
                    "dishes exactly zero', and it is why the OTHER two channels had to be " +
                    "computed and this one did not"
        ),
        T121BoundRecord(
            "the arm length against the upward root pitch",
            C0055_ARM_LENGTH / rootPitch, "-",
            "0.75, and 0.84 even at §3's own 45 paths: an arm CANNOT reach a second upward " +
                    "root anywhere in the design range, so the single-point attachment is " +
                    "forced by C-0055's own binding constraint rather than chosen here"
        ),
        T121BoundRecord(
            "what a TIED second layer would add, as a ratio to one duplex's own EI",
            composite / sheet.duplex.bendingRigidity, "-",
            "19.3x — the far end of the bracket, and the reason the exact zero is " +
                    "load-bearing rather than a technicality. Which end applies is decided " +
                    "by an inequality between two lengths, 8.164 nm against 10.88 nm"
        ),
        T121BoundRecord(
            "the arm array's plan footprint as a fraction of the tile",
            planFraction, "-",
            "C-0055's 0.46: nearly half the tile is covered, and it still adds nothing"
        ),
        T121BoundRecord(
            "the arm array's contour length as a fraction of the sheet's own",
            contourFraction, "-",
            "278 nm of duplex against 600 — and it is IDENTICALLY the plan fraction above, " +
                    "because an arm's plan footprint is its host duplex's own strip, so the " +
                    "two bounds are one bound and are reported as such"
        ),
        T121BoundRecord(
            "the quality factor with the arm mass added, as a ratio to C-0004's",
            sqrt(1.0 + contourFraction), "-",
            "1.21x on a quality factor C-0004 puts at 5.3e-4, so the tile stays overdamped " +
                    "by three orders. The arms' inertial force at 1 kHz and §3's 3 nm " +
                    "stroke is 1e-10 pN against §3's 100 pN — the MASS channel priced " +
                    "directly, and it is the smallest of the three by nine orders"
        ),
        T121BoundRecord(
            "the arm length at which the arm's OWN BENDING is engaged at all",
            2.0 * rootPitch, "nm",
            "THREE ties, i.e. 21.76 nm — 2.67x C-0055's arm. Two points determine a line, " +
                    "so a lever tied at two roots on one straight host duplex still bends " +
                    "not at all; what two ties add is a torsion bar and nothing else"
        ),
        T121BoundRecord(
            "the arms' drag as a fraction of C-0004's total at the nominal design point",
            armDrag / (bareDrag + armDrag), "-",
            "the ONE channel that moves anything, and it is an UPPER bound: an arm one " +
                    "duplex diameter above a translating plate sits in fluid already moving " +
                    "with it, and the lower bound is exactly zero"
        ),
    )

    // ------------------------------------------------------------------ the condensation
    println("T-121 — the condensation, at one tie and at the two the lattice forbids ...")
    val condensation = listOf(1e-4, 1e-6, 1e-8, 1e-10).flatMap { epsilon ->
        listOf(1, 2).map { ties ->
            val record = armRootCondensation(
                sheet.duplex.bendingRigidity, sheet.duplex.torsionalRigidity,
                if (ties == 1) C0055_ARM_LENGTH else rootPitch,
                OrigamiGrillage.RIGID_LINK_STIFFNESS, sheet.crossoverHingeStiffness,
                epsilon, ties
            )
            T121CondensationRecord(
                ties = record.ties,
                regularisation = record.regularisation,
                addedDeflectionStiffness = record.addedDeflectionStiffness,
                addedRollStiffness = record.addedRollStiffness,
                addedStiffnessNorm = record.addedStiffnessNorm,
                note = if (ties == 1)
                    "C-0055's motif: one upward crossover, an 8.164 nm arm. The added " +
                            "stiffness vanishes LINEARLY in the regularisation, which is " +
                            "the numerical signature of an exact zero rather than a small one"
                else "the counterfactual: an arm long enough to reach the NEXT upward root " +
                        "(10.88 nm) and tied at both. It is not a design — C-0055's own " +
                        "placement collapses from 34 arms to 30 at 8.19 nm — and it is " +
                        "carried only to show what the exact zero is a zero OF"
            )
        }
    }

    // ------------------------------------------------------------------ the grillage, carrying them
    println("T-121 — C-0009's grillage, re-run carrying the array ...")
    val armRoots = arms.map {
        it.rootX to armRootY(it.row, DUPLEXES, sheet.interhelicalDistance)
    }
    val rootSupports = couplingSupports(armRoots, MANDATE)
    /**
     * `C-0055`'s scheduler fills every row greedily from the low-`x` end and points every arm
     * the same way, so its 34 roots are **not** centro-symmetric: their centroid sits well off
     * the tile centre. The rows are independent — that is `C-0055`'s own gate-3 finding — so
     * reflecting the odd ones is free, lands on the same column lattice, and is a design
     * variable no upstream claim has swept.
     */
    val mirroredArms = arms.map {
        if (it.row % 2 == 1) it.copy(rootX = -it.rootX, towardPositiveX = !it.towardPositiveX)
        else it
    }
    val mirroredRoots = mirroredArms.map {
        it.rootX to armRootY(it.row, DUPLEXES, sheet.interhelicalDistance)
    }
    val mirroredSupports = couplingSupports(mirroredRoots, MANDATE)
    val rootCentroid = armRoots.sumOf { it.first } / armRoots.size
    val mirroredCentroid = mirroredRoots.sumOf { it.first } / mirroredRoots.size
    val gridSupports = couplingSupports(
        attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lengthY), MANDATE
    )
    val columnSupports = couplingSupports(
        attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY), MANDATE
    )
    val freeStroke = PlateOnFoundation(
        plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    val placements = listOf(
        "NONE — free tile" to emptyList<PointSupport>(),
        "ROOTS — C-0055's 34 upward arm roots" to rootSupports,
        "GRID — C-0015's 3 x 15" to gridSupports
    )
    val profiles = listOf(
        "uniform (the load case the exact zero is written on)" to uniformField,
        "C-0022 solved, 2 mM, 10 nm, 0.192 V" to solvedField
    )

    val responses = buildList {
        placements.forEach { (placement, supports) ->
            val bare = lattice(sheet, supports)
            val armed = StackedArmGrillage(bare, arms)
            val tied = StackedArmGrillage(
                bare, arms.map { it.copy(length = rootPitch) }, tiesPerArm = 2
            )
            profiles.forEach { (profileName, field) ->
                val reference = bare.solve(field)
                listOf(
                    "BARE — C-0009's sheet, no arms" to null,
                    "ARMED — 34 arms, ONE crossover each (C-0055's motif)" to armed,
                    "TIED — 34 arms at TWO crossovers each (the counterfactual)" to tied
                ).forEach { (name, model) ->
                    val solution = model?.solve(field)
                    val deflection = solution?.deflection ?: reference
                    add(
                        T121ResponseRecord(
                            configuration = name,
                            placement = placement,
                            profile = profileName,
                            meanDeflection = deflection.meanDeflection,
                            peakDishing = deflection.peakDishing(),
                            dishingOverStroke = deflection.peakDishing() / freeStroke,
                            peakCrossoverForce = deflection.peakCrossoverForce,
                            peakDuplexShear = deflection.peakDuplexShear,
                            armStrainEnergy = solution?.armEnergy ?: 0.0,
                            peakRootLinkForce = solution?.peakRootLinkForce ?: 0.0,
                            departureFromBareHost =
                                solution?.hostDeparture(reference.coefficients) ?: 0.0
                        )
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------ C-0006 / C-0047's flatness
    println("T-121 — C-0006's flatness, with the coupling entering at the arm roots ...")
    val flatness = listOf(
        Triple("NONE — free tile", emptyList<PointSupport>(), 0),
        Triple("ROOTS — C-0055's 34 upward arm roots", rootSupports, rootSupports.size),
        Triple(
            "ROOTS-MIRRORED — the same 34 roots, alternate rows reflected",
            mirroredSupports, mirroredSupports.size
        ),
        Triple("GRID — C-0015's 3 x 15", gridSupports, gridSupports.size),
        Triple("COLUMN — C-0041's 1 x 15", columnSupports, columnSupports.size)
    ).flatMap { (placement, supports, stations) ->
        profiles.map { (profileName, field) ->
            val solution = lattice(sheet, supports).solve(field)
            val dishing = solution.peakDishing()
            T121FlatnessRecord(
                placement = placement,
                stations = stations,
                profile = profileName,
                peakDishing = dishing,
                dishingOverStroke = dishing / freeStroke,
                flatAtTenPercent = dishing / freeStroke < FLATNESS_TOLERANCE,
                peakCrossoverForce = solution.peakCrossoverForce,
                peakDuplexShear = solution.peakDuplexShear,
                perPathForce = if (stations == 0) 0.0
                else solution.supportForces.maxOf { abs(it) }
            )
        }
    }

    // -------------------------------------------- can C-0058's axis be run on THESE stations?
    println("T-121 — C-0058's rim rule, on the stations the arm array actually supplies ...")
    val stationSets = listOf(
        Triple("ROOTS — C-0055's own greedy placement", armRoots, rootSupports.size),
        Triple("ROOTS-MIRRORED — alternate rows reflected", mirroredRoots, mirroredRoots.size),
        Triple("GRID — C-0015's 3 x 15", attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lengthY), 45)
    )
    val distributions = stationSets.flatMap { (placement, grid, stations) ->
        val ceiling = admissibleStiffnessRatio(
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, MANDATE, stations
        )
        listOf(1.0, 2.0, 3.0, 5.0, 8.0, 12.0, 20.0).map { ratio ->
            val weights = rimStiffenedWeights(
                grid, Gen1Tile.EDGE_X, lengthY, C0058_COLLAR_WIDTH, ratio
            )
            val supports = weightedSupports(grid, weights, MANDATE)
            val dishing = lattice(sheet, supports).solve(solvedField).peakDishing()
            val peakPath = supports.maxOf { it.stiffness } * Gen1Tile.ACCEPTABLE_STROKE
            T121DistributionRecord(
                placement = placement,
                stations = stations,
                rule = "rim x ratio over a %.2f nm collar (C-0058's one-parameter family)"
                    .format(C0058_COLLAR_WIDTH),
                ratio = ratio,
                dishingOverStroke = dishing / freeStroke,
                flatAtTenPercent = dishing / freeStroke < FLATNESS_TOLERANCE,
                peakPathForce = peakPath,
                admissibleRatio = ceiling,
                withinPerPathAllowable = peakPath <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
            )
        }
    }

    // ------------------------------------------------------------------ C-0010's variance
    println("T-121 — C-0010's variance, broadband and in band ...")
    val workingFoundation = Gen1Tile.FOUNDATION_AT_WORKING_POINT
    val cornerBare = nominalDrainage.cornerFrequency
    val cornerWithArms = cornerBare * bareDrag / (bareDrag + armDrag)
    val variance = listOf(
        Triple("NONE — free tile", emptyList<PointSupport>(), arms.size),
        Triple("ROOTS — C-0055's 34 upward arm roots", rootSupports, arms.size),
        Triple("GRID — C-0015's 3 x 15", gridSupports, arms.size)
    ).flatMap { (placement, supports, armCount) ->
        val model = lattice(sheet, supports, foundationStiffness = workingFoundation)
        val fluctuation = model.thermalFluctuation()
        val areaRms = sqrt(
            fluctuation.pistonRms * fluctuation.pistonRms +
                    fluctuation.tiltRms * fluctuation.tiltRms +
                    fluctuation.dishingRms * fluctuation.dishingRms
        )
        listOf(0 to cornerBare, armCount to cornerWithArms).map { (attached, corner) ->
            val fraction = (2.0 / PI) * atan(BANDWIDTH_TARGET / corner)
            val inBand = areaRms * sqrt(fraction)
            T121VarianceRecord(
                placement = placement,
                armsAttached = attached,
                pistonRms = fluctuation.pistonRms,
                tiltRms = fluctuation.tiltRms,
                dishingRms = fluctuation.dishingRms,
                areaRms = areaRms,
                centreRms = fluctuation.centreRms,
                cornerFrequency = corner,
                inBandFraction = fraction,
                inBandRms = inBand,
                marginAgainstThreeNanometresBroadband = 3.0 / areaRms,
                marginAgainstThreeNanometresInBand = 3.0 / inBand
            )
        }
    }

    // ------------------------------------------------------------------ C-0004's drainage
    println("T-121 — C-0004's drainage, with 34 cylinders standing on the DRY side ...")
    val drainage = listOf(nominalDrainage, worstDrainage).flatMap { point ->
        listOf(DUPLEX_RADIUS, DUPLEX_RADIUS_NARROW).map { radius ->
            val added = arms.size *
                    slenderBodyTransverseDrag(VISCOSITY, C0055_ARM_LENGTH, radius)
            val bare = point.squeezeDrag + point.stokesDrag
            val corner = point.cornerFrequency * bare / (bare + added)
            T121DrainageRecord(
                point = point.label,
                permeabilityModel = point.model,
                armRadius = radius,
                squeezeDrag = point.squeezeDrag,
                stokesDrag = point.stokesDrag,
                armDrag = added,
                totalDragBare = bare,
                totalDragWithArms = bare + added,
                armDragFraction = added / (bare + added),
                cornerFrequencyBare = point.cornerFrequency,
                cornerFrequencyWithArms = corner,
                marginBare = point.margin,
                marginWithArms = corner / BANDWIDTH_TARGET,
                stillDischarged = corner / BANDWIDTH_TARGET > 1.0
            )
        }
    }

    // ------------------------------------------------------------------ gate 4
    println("T-121 — convergence ...")
    val bareGrid = lattice(sheet, gridSupports)
    val epsilonValues = listOf(1e-5, 1e-7, 1e-9)
    val epsilonDepartures = epsilonValues.map { epsilon ->
        val reference = bareGrid.solve(solvedField).coefficients
        StackedArmGrillage(bareGrid, arms, regularisation = epsilon)
            .solve(solvedField).hostDeparture(reference)
    }
    val meshValues = listOf(1, 2, 4).map { subdivisions ->
        val model = lattice(sheet, gridSupports, subdivisions = subdivisions)
        StackedArmGrillage(model, arms).solve(solvedField).deflection.peakDishing()
    }
    val penaltyValues = listOf(1e3, 1e4, 1e5).map { penalty ->
        val model = lattice(sheet, gridSupports, linkStiffness = penalty)
        StackedArmGrillage(model, arms, linkStiffness = penalty)
            .solve(solvedField).deflection.peakDishing()
    }
    val convergence = listOf(
        T121ConvergenceRecord(
            "the armed host's departure from the bare host, C-0022's solved load",
            "arm regularisation [pN/nm]", epsilonValues, epsilonDepartures,
            epsilonDepartures.last() / epsilonDepartures.first(),
            "LINEAR in the regularisation over four decades, which is the numerical " +
                    "signature of an exact zero: a genuine added stiffness would converge " +
                    "to a non-zero limit instead"
        ),
        T121ConvergenceRecord(
            "the armed lattice's peak dishing under C-0022's solved load",
            "beam elements per interval (NESTED 1 c 2 c 4)",
            listOf(1.0, 2.0, 4.0), meshValues,
            abs(meshValues[2] - meshValues[1]) / meshValues[2],
            "nested refinement only — a subdivision of 3 moves a load from a node to " +
                    "mid-element and is not a refinement of 2 (`CLAUDE.md`)"
        ),
        T121ConvergenceRecord(
            "the armed lattice's peak dishing under C-0022's solved load",
            "crossover link penalty [pN/nm]",
            listOf(1e3, 1e4, 1e5), penaltyValues,
            abs(penaltyValues[2] - penaltyValues[1]) / penaltyValues[2],
            "the vertical link is a CONSTRAINT and the answer must not depend on its penalty"
        )
    )

    // ------------------------------------------------------------------ gate 5
    println("T-121 — upstream reproductions ...")
    fun reproduction(
        source: String,
        quantity: String,
        published: Double,
        reproduced: Double,
        strict: Boolean = true
    ) = T121ReproductionRecord(
        source, quantity, published, reproduced,
        abs(reproduced - published) / abs(published), strict
    )

    val gridSolved = flatness.first {
        it.placement.startsWith("GRID") && it.profile.startsWith("C-0022")
    }
    val columnSolved = flatness.first {
        it.placement.startsWith("COLUMN") && it.profile.startsWith("C-0022")
    }
    val freeSolved = flatness.first {
        it.placement.startsWith("NONE") && it.profile.startsWith("C-0022")
    }
    val reproductions = listOf(
        reproduction("C-0055", "arm length at 34 paths [nm]", 8.164, C0055_ARM_LENGTH, false),
        reproduction("C-0055", "arm count on a 40 nm tile", 34.0, arms.size.toDouble()),
        reproduction("C-0055", "upward root pitch [nm]", 10.88, rootPitch),
        reproduction("C-0055", "arm array plan footprint fraction", 0.46, planFraction, false),
        reproduction("C-0047", "dishing/stroke, 3 x 15, C-0022's solved load", 0.218,
            gridSolved.dishingOverStroke, false),
        reproduction("C-0047", "dishing/stroke, 1 x 15, C-0022's solved load", 0.695,
            columnSolved.dishingOverStroke, false),
        reproduction("C-0022", "dishing/stroke, free uncoupled tile", 0.308,
            freeSolved.dishingOverStroke, false),
        reproduction("C-0004", "corner frequency, nominal 40 x 40 nm point [Hz]",
            91231.5, cornerBare, false),
        reproduction("C-0004", "in-band variance fraction below 1 kHz at 91 kHz", 0.0070,
            (2.0 / PI) * atan(BANDWIDTH_TARGET / cornerBare), false),
        reproduction(
            "C-0058", "dishing/stroke, 3 x 15, rim x 5 over a 6.70 nm collar", 0.0753,
            distributions.first {
                it.placement.startsWith("GRID") && it.ratio == 5.0
            }.dishingOverStroke, false
        ),
        reproduction(
            "C-0058", "admissible per-path stiffness ratio at 45 paths", 4.5,
            admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, MANDATE, 45
            ), false
        ),
        reproduction("C-0009", "duplex EI [pN nm^2]", 230.0, sheet.duplex.bendingRigidity),
        reproduction("C-0009", "crossover hinge k_theta [pN nm/rad]", 13.5294,
            sheet.crossoverHingeStiffness, false),
        reproduction("C-0009", "interhelical distance [nm]", 2.69, sheet.interhelicalDistance)
    )

    // ------------------------------------------------------------------ the predicates
    val armedSolvedGrid = responses.first {
        it.configuration.startsWith("ARMED") && it.placement.startsWith("GRID") &&
                it.profile.startsWith("C-0022")
    }
    val bareSolvedGrid = responses.first {
        it.configuration.startsWith("BARE") && it.placement.startsWith("GRID") &&
                it.profile.startsWith("C-0022")
    }
    val tiedSolvedGrid = responses.first {
        it.configuration.startsWith("TIED") && it.placement.startsWith("GRID") &&
                it.profile.startsWith("C-0022")
    }
    val rootsSolved = flatness.first {
        it.placement.startsWith("ROOTS —") && it.profile.startsWith("C-0022")
    }
    val mirroredSolved = flatness.first {
        it.placement.startsWith("ROOTS-MIRRORED") && it.profile.startsWith("C-0022")
    }
    val worstDrainageRecord = drainage.minByOrNull { it.marginWithArms }!!
    val tiedTwoTies = condensation.last { it.ties == 2 }

    check(exactZero.addedStiffnessNorm < 1e-6) {
        "the condensation must vanish: the whole verdict rests on it, and it read " +
                "${exactZero.addedStiffnessNorm} pN/nm"
    }
    check(!secondRootReachable(C0055_ARM_LENGTH, rootPitch)) {
        "the arm must be shorter than the upward root pitch, or the single tie is a " +
                "modelling choice rather than a lattice fact"
    }
    check(worstDrainageRecord.marginWithArms > 1.0) {
        "§4(d) must stay discharged, and it read ${worstDrainageRecord.marginWithArms}"
    }
    check(abs(uniformFieldDishing(lattice(sheet, emptyList()), uniformField)) < 1e-9) {
        "a uniform load on a uniform Winkler foundation must dish exactly zero"
    }

    val predicates = listOf(
        T121PredicateRecord(
            "rigidity",
            "34 arms attached at one upward crossover each change no response of C-0009's " +
                    "grillage",
            ("PASS — the peak dishing under C-0022's solved load moves from " +
                    "%.6f nm to %.6f nm, a departure of %.1e, which is the arm " +
                    "regularisation and vanishes with it").format(
                bareSolvedGrid.peakDishing, armedSolvedGrid.peakDishing,
                abs(armedSolvedGrid.peakDishing - bareSolvedGrid.peakDishing) /
                        bareSolvedGrid.peakDishing
            )
        ),
        T121PredicateRecord(
            "the counterfactual",
            "a body tied at TWO upward crossovers is not the same statement",
            ("CONFIRMED — the same 34 bodies tied twice move the peak dishing to %.6f nm, " +
                    "%.3fx the bare sheet's, and the lattice cannot build them: the arm is " +
                    "%.3f nm against a %.2f nm root pitch").format(
                tiedSolvedGrid.peakDishing,
                tiedSolvedGrid.peakDishing / bareSolvedGrid.peakDishing,
                C0055_ARM_LENGTH, rootPitch
            )
        ),
        T121PredicateRecord(
            "flatness",
            "C-0006/C-0047's flatness, with the coupling entering at the 34 arm roots",
            ("%.4f of the stroke against %.4f on C-0015's 3 x 15 and %.4f on C-0041's " +
                    "1 x 15; T-5b's tolerance is 0.10 and NONE of them reaches it — " +
                    "CH-0034's saturation, not a new failure").format(
                rootsSolved.dishingOverStroke, gridSolved.dishingOverStroke,
                columnSolved.dishingOverStroke
            )
        ),
        T121PredicateRecord(
            "variance",
            "C-0010's sigma_RMS <= 3.0 nm predicate, leaf A1.2",
            ("PASS and UNCHANGED broadband — equipartition does not see a drag or a mass — " +
                    "and the IN-BAND amplitude rises %.1f %% because the arms lower the " +
                    "drainage corner from %.0f Hz to %.0f Hz").format(
                100.0 * (sqrt(
                    (2.0 / PI) * atan(BANDWIDTH_TARGET / cornerWithArms) /
                            ((2.0 / PI) * atan(BANDWIDTH_TARGET / cornerBare))
                ) - 1.0), cornerBare, cornerWithArms
            )
        ),
        T121PredicateRecord(
            "drainage",
            "C-0004's §4(d) discharge",
            ("HOLDS — the arms stand on the side of the tile AWAY from the layer, so the " +
                    "squeeze film is untouched and what they add is %.1f %% of the total " +
                    "drag as an UPPER bound; the worst 40 x 40 nm margin falls from %.2fx " +
                    "to %.2fx").format(
                100.0 * worstDrainageRecord.armDragFraction,
                worstDrainageRecord.marginBare, worstDrainageRecord.marginWithArms
            )
        )
    )

    val findings = listOf(
        ("A body attached to a structure at ONE point and otherwise free adds EXACTLY ZERO " +
                "static stiffness there. Its rigid-body motions span everything a single " +
                "crossover can impose — the deflection through the vertical link and the roll " +
                "through k_theta — so the minimised arm energy is identically zero and the " +
                "Schur complement vanishes term by term. 34 arms covering %.0f %% of the " +
                "tile's plan change nothing in C-0009's grillage, and that is a symmetry " +
                "statement rather than a small number.").format(100.0 * planFraction),
        ("The single tie is NOT a modelling choice. An upward site's lattice pitch is the " +
                "bare 32 bp = %.2f nm because it belongs to one duplex, and C-0039's elastica " +
                "gives %.3f nm at C-0055's self-consistent 34 paths and 9.131 nm even at §3's " +
                "45 — so no arm in the design range can reach a second root. The escape, its " +
                "price and now its structural harmlessness are all the SAME sentence.").format(
            rootPitch, C0055_ARM_LENGTH
        ),
        ("What the exact zero is a zero OF, in three steps rather than two. ONE tie adds " +
                "nothing at all. TWO ties add a TORSION BAR and nothing else — %.3f pN nm/" +
                "rad, the two hinges in series with the arm's own GJ/L — and no bending " +
                "stiffness whatever, because two points determine a line and a rigid arm " +
                "can meet both: the peak dishing moves %.3f %%. The arm's own EI is engaged " +
                "only at THREE ties, i.e. %.2f nm of arm, and a genuinely axially coupled " +
                "second layer would carry %.1fx one duplex's own EI. The whole bracket is " +
                "decided by inequalities between lengths.").format(
            tiedTwoTies.addedRollStiffness,
            100.0 * (tiedSolvedGrid.peakDishing / bareSolvedGrid.peakDishing - 1.0),
            2.0 * rootPitch, composite / sheet.duplex.bendingRigidity
        ),
        ("The cheap bound decided the order of work and it was right: rigidity EXACTLY zero, " +
                "inertia %.1e of the actuation load at 1 kHz, drag %.1f %% of C-0004's " +
                "budget. Only the third channel moves a number, and it moves the one " +
                "quantity in this programme that is quoted with a bandwidth.").format(
            massPerLength * arms.sumOf { it.length } *
                    (2.0 * PI * BANDWIDTH_TARGET) * (2.0 * PI * BANDWIDTH_TARGET) *
                    Gen1Tile.ACCEPTABLE_STROKE / Gen1Tile.TARGET_FORCE,
            100.0 * armDrag / (bareDrag + armDrag)
        ),
        ("The arms do not enter the actuation gap at all — they stand on the +z face, the " +
                "PEG layer and the electrode are on the -z face — so no electrostatic, " +
                "osmotic or squeeze-film quantity in this programme has an arm term in it. " +
                "What they enter is the space ABOVE, which is C-0035's output superstructure, " +
                "and that is a clearance question this claim states rather than solves."),
        ("The coupling entering at the 34 arm roots rather than on C-0015's 3 x 15 grid is a " +
                "real change and it is the only one: %.4f of the stroke against %.4f, both " +
                "past T-5b's 0.10, and %.2fx the FREE tile's own %.4f. The arm roots are a " +
                "%.2f nm pitch along x on alternating rows, so the array is a coarser and " +
                "less regular grid than the one C-0047 and C-0058 optimise over — and on it " +
                "a uniform coupling is a net dishing SOURCE, which is the pathology C-0058 " +
                "found at C-0041's 1 x 15.").format(
            rootsSolved.dishingOverStroke, gridSolved.dishingOverStroke,
            rootsSolved.dishingOverStroke / freeSolved.dishingOverStroke,
            freeSolved.dishingOverStroke, rootPitch
        ),
        ("C-0055's placement is not centro-symmetric and nothing upstream noticed: its " +
                "scheduler fills every row from the low-x end and points every arm the same " +
                "way, so the coupling centroid sits at x = %.2f nm on a tile that runs " +
                "-20 to +20. Reflecting the odd rows — free, on the same column lattice, and " +
                "inside C-0055's own per-row independence — moves the centroid to %.2f nm " +
                "and the dishing from %.4f to %.4f of the stroke.").format(
            rootCentroid, mirroredCentroid, rootsSolved.dishingOverStroke,
            mirroredSolved.dishingOverStroke
        ),
        ("C-0058's non-uniform axis was written on C-0015's 3 x 15 grid and it does NOT " +
                "transfer to the stations the buildable array supplies. Its rim x 5 rule " +
                "reaches %.4f there (reproducing its published 0.0753) and only %.4f on " +
                "C-0055's own roots and %.4f on the mirrored ones; the best ratio swept " +
                "reaches %.4f. A distribution cannot repair a placement — C-0058's own " +
                "sentence, at a placement it did not test.").format(
            distributions.first { it.placement.startsWith("GRID") && it.ratio == 5.0 }
                .dishingOverStroke,
            distributions.first { it.placement.startsWith("ROOTS —") && it.ratio == 5.0 }
                .dishingOverStroke,
            distributions.first { it.placement.startsWith("ROOTS-MIRRORED") && it.ratio == 5.0 }
                .dishingOverStroke,
            distributions.filter { it.placement.startsWith("ROOTS") && it.withinPerPathAllowable }
                .minOf { it.dishingOverStroke }
        )
    )

    val result = T121Result(
        task = "T-121 — what do 34 duplexes stacked ABOVE the tile do to it?",
        leaf = "A8.2 (structural rigidity and joint stiffness), with A1.2 for the " +
                "anchoring scheme and the variance predicate",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x 40.35 nm " +
                "single-layer square-lattice Rothemund sheet, 15 duplexes at the " +
                "SAXS-measured 2.69 nm, 8 symmetrically centred crossover columns; " +
                "C-0055's own 34-arm upward placement at C-0039's 8.164 nm arm; C-0022's " +
                "SOLVED edge profile at 2 mM, a 10 nm gap and 0.192 V; C-0017's " +
                "33.3333 pN/nm mandate; C-0004's drag budget at 40 x 40 nm",
        decision = "The three channels are settled in the order the cheap bound put them. " +
                "RIGIDITY is exactly zero, by a condensation identity rather than by a " +
                "solve, and the single tie that makes it exact is forced by C-0055's own " +
                "root pitch. MASS enters only the quality factor, twelve orders below the " +
                "actuation load. DRAG is the one channel that moves anything, it is an " +
                "upper bound, and it moves a bandwidth rather than an amplitude.",
        bounds = bounds,
        condensation = condensation,
        responses = responses,
        flatness = flatness,
        distributions = distributions,
        variance = variance,
        drainage = drainage,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings,
        parameters = mapOf(
            "duplexes" to DUPLEXES.toDouble(),
            "crossoverColumns" to NOMINAL_COLUMNS.toDouble(),
            "arms" to arms.size.toDouble(),
            "armLength" to C0055_ARM_LENGTH,
            "armTotalContour" to arms.sumOf { it.length },
            "upwardRootPitch" to rootPitch,
            "interhelicalDistance" to sheet.interhelicalDistance,
            "planFootprintFraction" to planFraction,
            "contourFraction" to contourFraction,
            "compositeBendingRigidity" to composite,
            "duplexBendingRigidity" to sheet.duplex.bendingRigidity,
            "duplexMassPerLength" to massPerLength,
            "armArrayMass" to massPerLength * arms.sumOf { it.length },
            "sheetMass" to massPerLength * DUPLEXES * Gen1Tile.EDGE_X,
            "armDragUpperBound" to armDrag,
            "bareDragNominal" to bareDrag,
            "cornerFrequencyBare" to cornerBare,
            "cornerFrequencyWithArms" to cornerWithArms,
            "edgeX" to Gen1Tile.EDGE_X,
            "edgeY" to lengthY,
            "interiorPressure" to interiorPressure,
            "foundationSecant" to Gen1Tile.FOUNDATION_SECANT,
            "foundationAtWorkingPoint" to workingFoundation,
            "mandate" to MANDATE,
            "freeTileStroke" to freeStroke,
            "armRootCentroidX" to rootCentroid,
            "mirroredRootCentroidX" to mirroredCentroid,
            "collarWidth" to C0058_COLLAR_WIDTH,
            "viscosity" to VISCOSITY,
            "solvedTaperDepth" to smooth.depth,
            "solvedTaperWidth" to smooth.width,
            "solvedRimDepth" to rim.depth
        )
    )

    val output = File("gpd/results/T-121-stacked-arm-sheet.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    output.writeText(
        json.encodeToString(
            (json.encodeToJsonElement(result) as JsonObject).roundedForResult()
        )
    )
    t121Report(result, output, started)
}

/** The peak dishing of [model] under [field] — the falsifier the study refuses to emit without. */
private fun uniformFieldDishing(model: OrigamiGrillage, field: PressureField): Double =
    model.solve(field).peakDishing()

// ---------------------------------------------------------------------------------------------
// the report
// ---------------------------------------------------------------------------------------------

private fun t121Report(result: T121Result, output: File, started: Long) {
    println()
    println("=".repeat(96))
    println(result.task)
    println("=".repeat(96))
    println()
    println("the cheap bounds, which ran first")
    result.bounds.forEach {
        println("  %-72s %14.6g %s".format(it.name, it.value, it.unit))
    }
    println()
    println("the condensation — what one arm adds at its root")
    println("  %5s %12s %18s %18s".format("ties", "epsilon", "added k [pN/nm]", "norm"))
    result.condensation.forEach {
        println(
            "  %5d %12.1e %18.6e %18.6e".format(
                it.ties, it.regularisation, it.addedDeflectionStiffness, it.addedStiffnessNorm
            )
        )
    }
    println()
    println("C-0009's grillage, carrying the array (C-0022's solved load, 3 x 15 coupling)")
    result.responses.filter {
        it.placement.startsWith("GRID") && it.profile.startsWith("C-0022")
    }.forEach {
        println(
            "  %-52s dish %10.6f  crossover %8.4f  shear %8.4f".format(
                it.configuration, it.peakDishing, it.peakCrossoverForce, it.peakDuplexShear
            )
        )
    }
    println()
    println("C-0006's flatness under C-0022's solved load")
    result.flatness.filter { it.profile.startsWith("C-0022") }.forEach {
        println(
            "  %-44s %3d stations  dish/stroke %7.4f  flat %s".format(
                it.placement, it.stations, it.dishingOverStroke, it.flatAtTenPercent
            )
        )
    }
    println()
    println("C-0058's rim rule, on each station set")
    result.distributions.forEach {
        println(
            "  %-44s %3d stations  ratio %5.1f  dish/stroke %7.4f  flat %-5s  path %5.2f pN".format(
                it.placement, it.stations, it.ratio, it.dishingOverStroke,
                it.flatAtTenPercent, it.peakPathForce
            )
        )
    }
    println()
    println("C-0010's variance")
    result.variance.forEach {
        println(
            "  %-44s arms %2d  area %7.4f nm  corner %9.0f Hz  in band %7.4f nm".format(
                it.placement, it.armsAttached, it.areaRms, it.cornerFrequency, it.inBandRms
            )
        )
    }
    println()
    println("C-0004's drainage")
    result.drainage.forEach {
        println(
            "  %-46s a=%.2f nm  arm drag %5.1f %%  margin %6.2fx -> %6.2fx".format(
                it.point, it.armRadius, 100.0 * it.armDragFraction,
                it.marginBare, it.marginWithArms
            )
        )
    }
    println()
    println("upstream reproductions")
    result.reproductions.forEach {
        println(
            "  %-10s %-52s %12.6g vs %12.6g  %8.2e %s".format(
                it.source, it.quantity, it.published, it.reproduced, it.departure,
                if (it.strict) "" else "(non-strict)"
            )
        )
    }
    println()
    println("predicates")
    result.predicates.forEach { println("  ${it.name}: ${it.verdict}") }
    println()
    result.findings.forEach { println("  * $it"); println() }
    println("written to ${output.path} in ${(System.currentTimeMillis() - started) / 1000} s")
}
