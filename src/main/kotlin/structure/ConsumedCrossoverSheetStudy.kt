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

import com.xemantic.nano.plentyofroom.anchoring.ArmAnchorage
import com.xemantic.nano.plentyofroom.anchoring.BForm
import com.xemantic.nano.plentyofroom.anchoring.tradePoint
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * `T-110` — what spending 80–100 % of the tile's crossovers on hinges does to the sheet.
 *
 * ```shell
 * tools/study.sh structure.ConsumedCrossoverSheetStudyKt
 * ```
 *
 * Emits `gpd/results/T-110-consumed-crossover-sheet.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val DUPLEXES = 15
private const val NOMINAL_COLUMNS = 8
private const val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
private const val RIM_STANDOFF = 1.0
private const val FLATNESS_TOLERANCE = 0.10

/** `C-0015`'s inventory: 56 at the ten eight-column phases, 49 at the other 22. */
private const val INVENTORY_BEST = 56
private const val INVENTORY_WORST = 49

/** `C-0046`'s three surviving `E5a` designs, in crossovers spent. */
private val SURVIVING_DESIGNS = listOf(45, 50, 56)

/** The consumption levels swept — the ceiling, the three designs, and the way there. */
private val CONSUMPTION_LEVELS = listOf(0, 14, 28, 42, 45, 50, 56)

/** A plate whose across-helix rigidity has gone to zero cannot be built, so it is floored. */
private const val PLATE_RIGIDITY_FLOOR = 1e-9

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T110BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String
)

@Serializable
private data class T110RigidityRecord(
    val pattern: String,
    val consumed: Int,
    val consumedFraction: Double,
    val retained: Int,
    val emptyInterfaces: Int,
    val minimumPerInterface: Int,
    val maximumPerInterface: Int,
    val uniformCurvatureRigidity: Double,
    val uniformMomentRigidity: Double,
    val latticeImposedFieldRigidity: Double,
    val anisotropyUniformCurvature: Double,
    val anisotropyUniformMoment: Double,
    val components: Int,
    val connected: Boolean
)

@Serializable
private data class T110FlatnessRecord(
    val pattern: String,
    val placement: String,
    val profile: String,
    val consumed: Int,
    val consumedFraction: Double,
    val attachments: Int,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverPlate: Double,
    val freeTileStroke: Double,
    val dishingOverStroke: Double,
    val flat: Boolean,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val peakInterfaceForce: Double,
    val unzipMargin: Double,
    val components: Int
)

@Serializable
private data class T110VarianceRecord(
    val pattern: String,
    val consumed: Int,
    val consumedFraction: Double,
    val latticePistonRms: Double,
    val latticeDishingRms: Double,
    val latticeCentreRms: Double,
    val latticeDishingOverPiston: Double,
    val platePerpendicularRigidity: Double,
    val plateAreaRms: Double,
    val plateCornerRms: Double,
    val plateDishingOverPiston: Double,
    val latticeDishingOverIntact: Double
)

@Serializable
private data class T110CeilingRecord(
    val pathCount: Int,
    val consumedFraction: Double,
    val keepsSheetConnected: Boolean,
    val armLength: Double,
    val armLengthBasePairs: Double,
    val tangentAtWorking: Double,
    val usableStroke: Double,
    val forcePerPathAtWorking: Double,
    val clearsAcceptableStroke: Boolean
)

@Serializable
private data class T110ConvergenceRecord(
    val axis: String,
    val setting: String,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
private data class T110ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

@Serializable
private data class T110Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T110BoundRecord>,
    val rigidity: List<T110RigidityRecord>,
    val flatness: List<T110FlatnessRecord>,
    val variance: List<T110VarianceRecord>,
    val ceiling: List<T110CeilingRecord>,
    val convergence: List<T110ConvergenceRecord>,
    val reproductions: List<T110ReproductionRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the model, assembled from the standing pipelines
// ---------------------------------------------------------------------------------------------

private fun sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun lattice(
    sheet: OrigamiSheet,
    consumed: Set<CrossoverSite>,
    supports: List<PointSupport>,
    subdivisions: Int = 2,
    linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS
) = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = CrossoverLayout.centred(NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0),
    subdivisions = subdivisions,
    linkStiffness = linkStiffness,
    supports = supports,
    consumedCrossovers = consumed
)

/**
 * `C-0022`'s solved edge profile, read from its own result file and keyed on concentration, gap
 * **and bias** — `CLAUDE.md`: an upstream result file may hold more than one record per state.
 */
private fun solvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-110 consumes the SOLVED edge profile " +
                "and will not substitute an assumed one for it."
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

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = sheet()
    val lengthY = DUPLEXES * sheet.interhelicalDistance
    val area = Gen1Tile.EDGE_X * lengthY
    val interiorPressure = Gen1Tile.TARGET_FORCE / area
    val hinge = sheet.crossoverHingeStiffness
    val alongHelices = sheet.duplex.bendingRigidity / sheet.interhelicalDistance

    val inventory = lattice(sheet, emptySet(), emptyList()).crossoverSites
    check(inventory.size == INVENTORY_BEST) {
        "the nominal layout must carry $INVENTORY_BEST crossovers, has ${inventory.size}"
    }

    println("T-110 — reading C-0022's solved edge profile ...")
    val (smooth, rim) = solvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val solvedField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY, listOf(smooth, rim)
    )
    val uniformField = uniformPressure(interiorPressure)
    val profiles = listOf(
        "uniform (the load case C-0015's exact zero is written on)" to uniformField,
        "C-0022 solved, 2 mM, 10 nm, 0.192 V" to solvedField
    )

    // ------------------------------------------------------------------ the cheap bounds
    println("T-110 — the cheap bounds ...")
    val ceilingBest = maximumConsumedForConnectivity(INVENTORY_BEST, DUPLEXES)
    val ceilingWorst = maximumConsumedForConnectivity(INVENTORY_WORST, DUPLEXES)
    val bounds = listOf(
        T110BoundRecord(
            "the crossovers a CONNECTED sheet can spend, 56-crossover phases",
            ceilingBest.toDouble(), "crossovers",
            "a pigeonhole: 14 interfaces need one retained crossover each, so 56 - 14 = 42"
        ),
        T110BoundRecord(
            "the same as a fraction of the inventory",
            maximumConsumedFractionForConnectivity(INVENTORY_BEST, DUPLEXES), "-",
            "75.0 %, against C-0046's admissible region of 80-100 % — EVERY point of it is above"
        ),
        T110BoundRecord(
            "the same at the 49-crossover phases",
            maximumConsumedFractionForConnectivity(INVENTORY_WORST, DUPLEXES), "-",
            "71.4 %, i.e. the twenty-two seven-column phases are worse, not better"
        ),
        T110BoundRecord(
            "the pieces C-0046's best design leaves, by pigeonhole alone",
            (1 + (DUPLEXES - 1) - (INVENTORY_BEST - 45)).toDouble(), "components",
            "45 spent leaves 11 retained over 14 interfaces, so at least 3 empty and 4 pieces"
        ),
        T110BoundRecord(
            "the pieces C-0046's 100 % design leaves",
            DUPLEXES.toDouble(), "components",
            "no crossover survives, so the sheet is fifteen unconnected duplexes"
        ),
        T110BoundRecord(
            "C-0009's across-helix rigidity at zero consumption, uniform curvature",
            uniformCurvatureRigidity(INVENTORY_BEST, hinge, sheet.interhelicalDistance, area),
            "pN nm", "the baseline every number below is a fraction of"
        ),
        T110BoundRecord(
            "the sheet's bending anisotropy at zero consumption",
            alongHelices / (hinge * sheet.interhelicalDistance / sheet.crossoverSpacing), "-",
            "C-0009's 25.6x, reproduced on the continuum reading of D_perp"
        )
    )

    // ------------------------------------------------------------------ P1/P2: rigidity, connectivity
    println("T-110 — the rigidity and the connectivity, over 3 patterns x 7 levels ...")
    val curvature = 1e-3
    val rigidity = ConsumptionPattern.entries.flatMap { pattern ->
        CONSUMPTION_LEVELS.map { consumed ->
            val retained = retainedSites(inventory, consumed, pattern)
            val perInterface = retainedPerInterface(retained, DUPLEXES)
            val model = lattice(sheet, inventory.toSet() - retained, emptyList())
            // the lattice's own reading: the energy of an imposed uniform across-helix curvature
            val imposed = 2.0 * model.hingeEnergy(model.curvatureFieldAcrossHelices(curvature)) /
                    (curvature * curvature * area)
            val voigt = uniformCurvatureRigidity(
                retained.size, hinge, sheet.interhelicalDistance, area
            )
            val reuss = uniformMomentRigidity(perInterface, hinge, Gen1Tile.EDGE_X, lengthY)
            T110RigidityRecord(
                pattern = pattern.label,
                consumed = consumed,
                consumedFraction = consumed.toDouble() / inventory.size,
                retained = retained.size,
                emptyInterfaces = perInterface.count { it == 0 },
                minimumPerInterface = perInterface.min(),
                maximumPerInterface = perInterface.max(),
                uniformCurvatureRigidity = voigt,
                uniformMomentRigidity = reuss,
                latticeImposedFieldRigidity = imposed,
                anisotropyUniformCurvature = bendingAnisotropy(alongHelices, voigt),
                anisotropyUniformMoment = bendingAnisotropy(alongHelices, reuss),
                components = sheetComponents(retained, DUPLEXES),
                connected = sheetComponents(retained, DUPLEXES) == 1
            )
        }
    }

    // ------------------------------------------------------------------ P3/P4: flatness and load
    println("T-110 — the flatness and the load distribution, lattice and plate ...")
    val gridSupports = couplingSupports(
        attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lengthY), MANDATE
    )
    val plateModel = sheet.plate(Gen1Tile.EDGE_X, lengthY)
    val freeStroke = PlateOnFoundation(
        plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    fun smearedPlate(consumedFraction: Double) = plateModel.copy(
        rigidityY = max(PLATE_RIGIDITY_FLOOR, (1.0 - consumedFraction) * plateModel.rigidityY)
    )

    val siteGeometry = lattice(sheet, emptySet(), emptyList()).crossovers
        .associateBy { CrossoverSite(it.lowerBeam, it.column) }

    val flatness = buildList {
        ConsumptionPattern.entries.forEach { pattern ->
            CONSUMPTION_LEVELS.forEach { consumed ->
                val retained = retainedSites(inventory, consumed, pattern)
                val spent = inventory.toSet() - retained
                val fraction = consumed.toDouble() / inventory.size
                val hingeGrid = spent.sortedWith(compareBy({ it.lowerBeam }, { it.column }))
                    .map { site -> siteGeometry.getValue(site).let { it.x to it.y } }
                val placements = buildList {
                    add("NONE — no coupling at all" to emptyList<PointSupport>())
                    add("GRID — C-0015's 3 x 15 attachments" to gridSupports)
                    if (hingeGrid.isNotEmpty()) {
                        add(
                            "AT_HINGE — the coupling attaches where it consumed" to
                                    couplingSupports(hingeGrid, MANDATE)
                        )
                    }
                }
                placements.forEach { (placement, supports) ->
                    val model = lattice(sheet, spent, supports)
                    val plate = PlateOnFoundation(
                        smearedPlate(fraction), Gen1Tile.FOUNDATION_SECANT, supports,
                        basisDegree = 12
                    )
                    profiles.forEach { (name, field) ->
                        val solution = model.solve(field)
                        val plateDishing = plate.solve(field).peakDishing()
                        val latticeDishing = solution.peakDishing()
                        val peak = if (model.crossovers.isEmpty()) 0.0
                        else solution.peakCrossoverForce
                        val interfaceForces = (0 until DUPLEXES - 1)
                            .map { abs(solution.shearAcrossInterface(it)) }
                        add(
                            T110FlatnessRecord(
                                pattern = pattern.label,
                                placement = placement,
                                profile = name,
                                consumed = consumed,
                                consumedFraction = fraction,
                                attachments = supports.size,
                                latticePeakDishing = latticeDishing,
                                platePeakDishing = plateDishing,
                                latticeOverPlate = if (plateDishing > 1e-12)
                                    latticeDishing / plateDishing else 0.0,
                                freeTileStroke = freeStroke,
                                dishingOverStroke = latticeDishing / freeStroke,
                                flat = latticeDishing / freeStroke < FLATNESS_TOLERANCE,
                                peakCrossoverForce = peak,
                                peakDuplexShear = solution.peakDuplexShear,
                                peakInterfaceForce = interfaceForces.max(),
                                unzipMargin = if (peak > 1e-9)
                                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / peak else 0.0,
                                components = sheetComponents(retained, DUPLEXES)
                            )
                        )
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ P5: C-0010's variance
    println("T-110 — C-0010's positional variance, free tile ...")
    val workingFoundation = Gen1Tile.FOUNDATION_AT_WORKING_POINT
    val intactDishing = OrigamiGrillage(
        sheet = sheet, lengthX = Gen1Tile.EDGE_X, beamCount = DUPLEXES,
        foundationStiffness = workingFoundation,
        columns = CrossoverLayout.centred(NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0)
    ).thermalFluctuation().dishingRms
    val variance = listOf(ConsumptionPattern.SPREAD, ConsumptionPattern.INTERFACE_FIRST)
        .flatMap { pattern ->
            CONSUMPTION_LEVELS.map { consumed ->
                val retained = retainedSites(inventory, consumed, pattern)
                val fraction = consumed.toDouble() / inventory.size
                val model = OrigamiGrillage(
                    sheet = sheet, lengthX = Gen1Tile.EDGE_X, beamCount = DUPLEXES,
                    foundationStiffness = workingFoundation,
                    columns = CrossoverLayout.centred(
                        NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0
                    ),
                    consumedCrossovers = inventory.toSet() - retained
                )
                val fluctuation = model.thermalFluctuation()
                val plate = PlateOnFoundation(
                    smearedPlate(fraction), workingFoundation, emptyList(), basisDegree = 12
                )
                val budget = plate.positionalVarianceBudget()
                T110VarianceRecord(
                    pattern = pattern.label,
                    consumed = consumed,
                    consumedFraction = fraction,
                    latticePistonRms = fluctuation.pistonRms,
                    latticeDishingRms = fluctuation.dishingRms,
                    latticeCentreRms = fluctuation.centreRms,
                    latticeDishingOverPiston = fluctuation.dishingRms / fluctuation.pistonRms,
                    platePerpendicularRigidity = smearedPlate(fraction).rigidityY,
                    plateAreaRms = budget.areaRms,
                    plateCornerRms = budget.cornerRms,
                    plateDishingOverPiston = budget.dishingOverPiston,
                    latticeDishingOverIntact = fluctuation.dishingRms / intactDishing
                )
            }
        }

    // ------------------------------------------------------------------ P6: the connected ceiling
    println("T-110 — C-0046's elastica at the connectivity ceiling ...")
    val far = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS).rotationalStiffness
    val ceiling = (34..46).toList().plus(listOf(50, 56)).distinct().sorted().map { paths ->
        val point = tradePoint(
            paths, 1, far, MANDATE, Gen1Tile.ACCEPTABLE_STROKE, Gen1Tile.DESIRED_STROKE
        )
        T110CeilingRecord(
            pathCount = paths,
            consumedFraction = paths.toDouble() / INVENTORY_BEST,
            keepsSheetConnected = paths <= ceilingBest,
            armLength = point.armLength,
            armLengthBasePairs = point.armLengthBasePairs,
            tangentAtWorking = point.tangentAtWorking,
            usableStroke = point.usableStroke,
            forcePerPathAtWorking = point.forcePerPathAtWorking,
            clearsAcceptableStroke = point.usableStroke >= Gen1Tile.ACCEPTABLE_STROKE
        )
    }

    // ------------------------------------------------------------------ gate 4: convergence
    println("T-110 — convergence ...")
    val spent45 = consumedSites(inventory, 45, ConsumptionPattern.SPREAD)
    val meshValues = listOf(1, 2, 4).map { subdivisions ->
        subdivisions to lattice(sheet, spent45, gridSupports, subdivisions = subdivisions)
            .solve(solvedField).peakDishing()
    }
    val penaltyValues = listOf(1e3, 1e4, 1e5, 1e6).map { penalty ->
        penalty to lattice(sheet, spent45, gridSupports, linkStiffness = penalty)
            .solve(solvedField).peakDishing()
    }
    val basisValues = listOf(8, 10, 12).map { degree ->
        degree to PlateOnFoundation(
            smearedPlate(45.0 / 56.0), Gen1Tile.FOUNDATION_SECANT, gridSupports,
            basisDegree = degree
        ).solve(solvedField).peakDishing()
    }
    val convergence = buildList {
        val finestMesh = meshValues.last().second
        meshValues.forEach { (subdivisions, value) ->
            add(
                T110ConvergenceRecord(
                    "lattice peak dishing at 45 spent, nested mesh 1 c 2 c 4",
                    "subdivisions = $subdivisions", value,
                    abs(value - finestMesh) / finestMesh
                )
            )
        }
        val finestPenalty = penaltyValues.last().second
        penaltyValues.forEach { (penalty, value) ->
            add(
                T110ConvergenceRecord(
                    "lattice peak dishing at 45 spent, crossover link penalty",
                    "k_link = %.0e".format(penalty), value,
                    abs(value - finestPenalty) / finestPenalty
                )
            )
        }
        val finestBasis = basisValues.last().second
        basisValues.forEach { (degree, value) ->
            add(
                T110ConvergenceRecord(
                    "plate peak dishing at 45 spent, Ritz basis degree",
                    "degree = $degree", value, abs(value - finestBasis) / finestBasis
                )
            )
        }
    }

    // ------------------------------------------------------------------ gate 5: upstream
    println("T-110 — upstream reproductions ...")
    fun reproduce(source: String, quantity: String, published: Double, here: Double) =
        T110ReproductionRecord(
            source, quantity, published, here,
            if (published == 0.0) abs(here) else abs(here - published) / abs(published)
        )

    val continuumPerpendicular = hinge * sheet.interhelicalDistance / sheet.crossoverSpacing
    val intactVoigt = uniformCurvatureRigidity(
        INVENTORY_BEST, hinge, sheet.interhelicalDistance, area
    )
    val intactGrid = lattice(sheet, emptySet(), gridSupports).solve(solvedField)
    val singleColumn = lattice(
        sheet, emptySet(),
        couplingSupports(attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY), MANDATE)
    ).solve(solvedField)
    val design45 = ceiling.first { it.pathCount == 45 }
    val reproductions = listOf(
        reproduce("C-0009", "D_perp, continuum k_theta d / p [pN nm]", 3.345, continuumPerpendicular),
        reproduce("C-0009", "D_perp, lattice imposed field [pN nm]", 3.397, intactVoigt),
        reproduce("C-0009", "lattice / continuum D_perp ratio", 1.015467, intactVoigt / continuumPerpendicular),
        reproduce("C-0009", "D_parallel = EI/d [pN nm]", 85.50, alongHelices),
        reproduce("C-0009", "bending anisotropy D_par / D_perp", 25.56, alongHelices / continuumPerpendicular),
        reproduce("C-0040", "crossovers on one interface of the nominal layout", 4.0,
            retainedPerInterface(inventory, DUPLEXES).max().toDouble()),
        reproduce("C-0015", "whole-tile crossover inventory", 56.0, inventory.size.toDouble()),
        reproduce("C-0047", "3 x 15 dishing over stroke, C-0022's solved load", 0.218,
            intactGrid.peakDishing() / freeStroke),
        reproduce("C-0047", "1 x 15 dishing over stroke, C-0022's solved load", 0.695,
            singleColumn.peakDishing() / freeStroke),
        reproduce("C-0022", "the FREE uncoupled tile's dishing over stroke", 0.308,
            lattice(sheet, emptySet(), emptyList()).solve(solvedField).peakDishing() / freeStroke),
        reproduce("C-0046", "the (45, 1) arm [nm]", 9.131, design45.armLength),
        reproduce("C-0046", "the (45, 1) tangent at 3 nm [pN/nm]", 39.18, design45.tangentAtWorking),
        reproduce("C-0046", "the (45, 1) usable stroke [nm]", 3.119, design45.usableStroke),
        reproduce("C-0046", "the (56, 1) usable stroke [nm]", 3.312,
            ceiling.first { it.pathCount == 56 }.usableStroke)
    )

    // ------------------------------------------------------------------ runtime falsifiers
    check(
        flatness
            .filter { it.profile.startsWith("uniform") && it.placement.startsWith("NONE") }
            .all { it.latticePeakDishing < 1e-9 && it.platePeakDishing < 1e-9 }
    ) {
        "a uniform load on a FREE tile on a uniform foundation must dish exactly zero at every " +
                "consumption level, in the lattice and in the plate alike"
    }
    check(rigidity.filter { it.consumed == 0 }.all { it.components == 1 }) {
        "the intact sheet must be connected"
    }
    check(rigidity.filter { it.consumed >= 45 }.none { it.connected }) {
        "every one of C-0046's designs must disconnect the sheet"
    }
    check(
        rigidity.all {
            abs(it.latticeImposedFieldRigidity - it.uniformCurvatureRigidity) <=
                    1e-9 * max(1.0, it.uniformCurvatureRigidity)
        }
    ) { "the lattice's imposed-field rigidity must equal the uniform-curvature closed form" }

    // ------------------------------------------------------------------ the findings
    val bestConnected = ceiling.first { it.pathCount == ceilingBest }
    val threshold = ceiling.firstOrNull { it.clearsAcceptableStroke }
    val design45Grid = flatness.first {
        it.consumed == 45 && it.pattern == ConsumptionPattern.SPREAD.label &&
                it.placement.startsWith("GRID") && it.profile.startsWith("C-0022")
    }
    val design45Hinge = flatness.first {
        it.consumed == 45 && it.pattern == ConsumptionPattern.SPREAD.label &&
                it.placement.startsWith("AT_HINGE") && it.profile.startsWith("C-0022")
    }
    val intactRecord = flatness.first {
        it.consumed == 0 && it.pattern == ConsumptionPattern.SPREAD.label &&
                it.placement.startsWith("GRID") && it.profile.startsWith("C-0022")
    }
    val connectedGrid = flatness.first {
        it.consumed == ceilingBest && it.pattern == ConsumptionPattern.SPREAD.label &&
                it.placement.startsWith("GRID") && it.profile.startsWith("C-0022")
    }
    val intactFree = flatness.first {
        it.consumed == 0 && it.pattern == ConsumptionPattern.SPREAD.label &&
                it.placement.startsWith("NONE") && it.profile.startsWith("C-0022")
    }
    val design45Free = flatness.first {
        it.consumed == 45 && it.pattern == ConsumptionPattern.SPREAD.label &&
                it.placement.startsWith("NONE") && it.profile.startsWith("C-0022")
    }
    val varianceFull = variance.first {
        it.consumed == 56 && it.pattern == ConsumptionPattern.SPREAD.label
    }
    val variance45 = variance.first {
        it.consumed == 45 && it.pattern == ConsumptionPattern.SPREAD.label
    }

    val findings = listOf(
        ("HINGE USE AND INTERFACE USE ARE EXCLUSIVE AT THE SITE, and the consequence is a " +
                "pigeonhole, not a degradation. A connected sheet needs one retained crossover " +
                "on each of its 14 interfaces, so at most %d of 56 (%.1f %%) can be spent — and " +
                "C-0046's admissible region is 80-100 %%. Every design in it severs the sheet: " +
                "45 spent leaves at least 4 pieces, 50 at least 9, and 56 leaves fifteen " +
                "separate duplexes.").format(
            ceilingBest, 100.0 * maximumConsumedFractionForConnectivity(INVENTORY_BEST, DUPLEXES)
        ),
        ("D_perp DOES NOT DEGRADE LINEARLY, IT COLLAPSES. Bending across the helices is 14 " +
                "hinge lines in SERIES, so the rigidity is a harmonic mean and one empty " +
                "interface annihilates it: the smeared reading falls from %.3f to %.3f pN nm at " +
                "45 spent (%.0f %% of it left) while the honest one is EXACTLY ZERO, and the " +
                "anisotropy goes from %.1f to unbounded. A continuum plate can only express the " +
                "first, which is the whole excess.").format(
            intactVoigt,
            rigidity.first {
                it.consumed == 45 && it.pattern == ConsumptionPattern.SPREAD.label
            }.uniformCurvatureRigidity,
            100.0 * (1.0 - 45.0 / 56.0), alongHelices / continuumPerpendicular
        ),
        ("FLATNESS UNDER C-0022's SOLVED LOAD IS CHEAP UP TO THE CEILING AND EXPENSIVE PAST IT. " +
                "On C-0015's 3 x 15 grid the tile dishes %.3f of the stroke intact, %.3f at the " +
                "42-crossover connected ceiling (+%.0f %%) and %.3f at 45 (%.2fx) — the step is " +
                "at severance, not at consumption. Uncoupled the same step runs %.3f to %.3f. " +
                "T-5b's convention is 0.10 and nothing in this study reaches it, which is " +
                "CH-0034's saturation, not a new failure.").format(
            intactRecord.dishingOverStroke, connectedGrid.dishingOverStroke,
            100.0 * (connectedGrid.dishingOverStroke / intactRecord.dishingOverStroke - 1.0),
            design45Grid.dishingOverStroke,
            design45Grid.dishingOverStroke / intactRecord.dishingOverStroke,
            intactFree.dishingOverStroke, design45Free.dishingOverStroke
        ),
        ("THE LOAD DISTRIBUTION IMPROVES WHILE THE SHEET FAILS, and the two are the same fact. " +
                "The peak per-load-path crossover force falls from %.3f pN intact to %.3f pN at " +
                "45 spent and to exactly zero at 56 — because a crossover that has been removed " +
                "cannot be overloaded. C-0026's exact zero is reached by DELETING the load path, " +
                "not by balancing it, and the duplex shear runs the other way: %.2f pN against " +
                "%.2f pN, against the 65 pN nicked ceiling.").format(
            intactRecord.peakCrossoverForce, design45Grid.peakCrossoverForce,
            design45Grid.peakDuplexShear, intactRecord.peakDuplexShear
        ),
        ("C-0010's INSENSITIVITY IS CORRECT AND IT IS NOT THE RELEVANT CHANNEL. A 2x change in " +
                "D_perp moves the SMEARED PLATE by 2.5 %%, and this study reproduces that: the " +
                "plate's area RMS moves %.1f %% over the whole consumption range. The LATTICE " +
                "moves %.1f %% at 45 spent and %.1f %% at 56 — %.0fx more — because a crossover " +
                "is TWO elements and only one of them is D_perp: the vertical link is a " +
                "CONSTRAINT tying two duplex surfaces together, it carries no bending rigidity " +
                "at all, and a continuum plate has no way to represent its removal.").format(
            100.0 * (varianceFull.plateAreaRms / variance.first { it.consumed == 0 }.plateAreaRms - 1.0),
            100.0 * (variance45.latticeDishingOverIntact - 1.0),
            100.0 * (varianceFull.latticeDishingOverIntact - 1.0),
            (varianceFull.latticeDishingOverIntact - 1.0) /
                    (varianceFull.plateAreaRms / variance.first { it.consumed == 0 }.plateAreaRms - 1.0)
        ),
        ("THE HINGE SITES ARE NOT AN ATTACHMENT GRID, so the generous 'a hinge IS an attachment' " +
                "reading is the worse design and not the better one. At 45 spent the coupling " +
                "attached at its own hinge sites dishes %.3f of the stroke against %.3f on " +
                "C-0015's 3 x 15 grid — %.1fx at the SAME path count — and puts %.2f pN on a " +
                "crossover against %.4f. A crossover lattice is a 32 bp x 2.69 nm lattice with a " +
                "parity, and a flatness grid is three columns of fifteen rows; they are " +
                "different shapes, which is C-0015's own lesson in a new place.").format(
            design45Hinge.dishingOverStroke, design45Grid.dishingOverStroke,
            design45Hinge.dishingOverStroke / design45Grid.dishingOverStroke,
            design45Hinge.peakCrossoverForce, design45Grid.peakCrossoverForce
        ),
        ("THE CONNECTED CEILING IS %d PATHS AND IT %s CLEAR SECTION 3's ACCEPTABLE STROKE: a " +
                "%d-path E5a array places an arm of %.3f nm and delivers %.3f nm of usable " +
                "stroke against the 3 nm clause. C-0046 bracketed the path-count threshold at " +
                "34 < n <= 45 and did not resolve it; it is %d paths. So the branch survives on " +
                "a sheet in one piece over the window %d <= n <= %d, and NONE of C-0046's three " +
                "reported designs is in it.").format(
            ceilingBest, if (bestConnected.clearsAcceptableStroke) "DOES" else "does NOT",
            ceilingBest, bestConnected.armLength, bestConnected.usableStroke,
            threshold?.pathCount ?: -1, threshold?.pathCount ?: -1, ceilingBest
        )
    )

    val result = T110Result(
        task = "T-110 — what spending 80-100 % of the tile's crossovers on hinges does to the sheet",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; " +
                "40.0 x 40.35 nm tile, 15 duplexes at the SAXS-measured 2.69 nm; " +
                "8 symmetrically centred crossover columns; C-0022's SOLVED edge profile at " +
                "2 mM, 10 nm, 0.192 V; C-0017's 33.3333 pN/nm mandate as n equal springs",
        decision = "A hinge line is a set of crossovers sharing ONE interface and ONE PAIR OF " +
                "BODIES (C-0040), and k_theta is the interhelical DIHEDRAL constant, so a " +
                "crossover that hinges a flexure arm is a crossover that no longer joins its " +
                "two sheet duplexes. The two uses are EXCLUSIVE AT THE SITE: a consumed " +
                "crossover supplies neither the dihedral spring nor the vertical link.",
        bounds = bounds,
        rigidity = rigidity,
        flatness = flatness,
        variance = variance,
        ceiling = ceiling,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings,
        parameters = mapOf(
            "duplexes" to DUPLEXES.toDouble(),
            "interfaces" to (DUPLEXES - 1).toDouble(),
            "crossoverColumns" to NOMINAL_COLUMNS.toDouble(),
            "inventory" to inventory.size.toDouble(),
            "connectivityCeiling" to ceilingBest.toDouble(),
            "connectivityCeilingWorstPhase" to ceilingWorst.toDouble(),
            "hingeStiffness" to hinge,
            "interhelicalDistance" to sheet.interhelicalDistance,
            "crossoverSpacing" to sheet.crossoverSpacing,
            "edgeX" to Gen1Tile.EDGE_X,
            "edgeY" to lengthY,
            "interiorPressure" to interiorPressure,
            "foundationSecant" to Gen1Tile.FOUNDATION_SECANT,
            "foundationAtWorkingPoint" to workingFoundation,
            "mandate" to MANDATE,
            "freeTileStroke" to freeStroke,
            "solvedTaperDepth" to smooth.depth,
            "solvedTaperWidth" to smooth.width,
            "solvedRimDepth" to rim.depth,
            "farAnchorage" to far
        )
    )

    val output = File("gpd/results/T-110-consumed-crossover-sheet.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    output.writeText(
        json.encodeToString(
            (json.encodeToJsonElement(result) as JsonObject).roundedForResult()
        )
    )
    t110Report(result, output, started)
}

// ---------------------------------------------------------------------------------------------
// the report
// ---------------------------------------------------------------------------------------------

private fun t110Report(result: T110Result, output: File, started: Long) {
    println()
    println("=".repeat(112))
    println(result.task)
    println("=".repeat(112))

    println()
    println("--- the geometric decision ".padEnd(112, '-'))
    println(result.decision)

    println()
    println("--- the cheap bounds ".padEnd(112, '-'))
    println("%-62s %14s %-10s".format("bound", "value", "unit"))
    result.bounds.forEach {
        println("%-62s %14.5f %-10s".format(it.name.take(62), it.value, it.unit))
        println("    %s".format(it.settles))
    }

    println()
    println("--- P1/P2: the rigidity and the connectivity ".padEnd(112, '-'))
    println(
        "%-46s %5s %6s %5s %10s %10s %8s %7s".format(
            "pattern", "spent", "frac", "empty", "D_Voigt", "D_Reuss", "aniso", "pieces"
        )
    )
    result.rigidity.forEach {
        println(
            "%-46s %5d %6.3f %5d %10.4f %10.4f %8.1f %7d".format(
                it.pattern.take(46), it.consumed, it.consumedFraction, it.emptyInterfaces,
                it.uniformCurvatureRigidity, it.uniformMomentRigidity,
                it.anisotropyUniformCurvature, it.components
            )
        )
    }

    println()
    println("--- P3/P4: flatness and load, under C-0022's SOLVED profile ".padEnd(112, '-'))
    println(
        "%-30s %-12s %5s %7s %9s %6s %9s %9s".format(
            "pattern", "placement", "spent", "dish/st", "lat/plate", "flat", "xover[pN]", "shear[pN]"
        )
    )
    result.flatness.filter { it.profile.startsWith("C-0022") }.forEach {
        println(
            "%-30s %-12s %5d %7.3f %9.3f %6s %9.4f %9.3f".format(
                it.pattern.take(30), it.placement.take(12), it.consumed, it.dishingOverStroke,
                it.latticeOverPlate, it.flat, it.peakCrossoverForce, it.peakDuplexShear
            )
        )
    }

    println()
    println("--- P5: C-0010's positional variance ".padEnd(112, '-'))
    println(
        "%-46s %5s %10s %10s %10s %10s".format(
            "pattern", "spent", "dish[nm]", "/intact", "centre[nm]", "plate[nm]"
        )
    )
    result.variance.forEach {
        println(
            "%-46s %5d %10.5f %10.5f %10.5f %10.5f".format(
                it.pattern.take(46), it.consumed, it.latticeDishingRms,
                it.latticeDishingOverIntact, it.latticeCentreRms, it.plateAreaRms
            )
        )
    }

    println()
    println("--- P6: C-0046's elastica against the connectivity ceiling ".padEnd(112, '-'))
    println(
        "%6s %8s %10s %10s %10s %10s %8s".format(
            "paths", "connect", "arm[nm]", "arm[bp]", "tan(3)", "usable", "clears3"
        )
    )
    result.ceiling.forEach {
        println(
            "%6d %8s %10.4f %10.2f %10.3f %10.4f %8s".format(
                it.pathCount, it.keepsSheetConnected, it.armLength, it.armLengthBasePairs,
                it.tangentAtWorking, it.usableStroke, it.clearsAcceptableStroke
            )
        )
    }

    println()
    println("--- gate 4: convergence ".padEnd(112, '-'))
    println("%-58s %-16s %14s %12s".format("axis", "setting", "value", "departure"))
    result.convergence.forEach {
        println(
            "%-58s %-16s %14.7f %12.3e".format(
                it.axis.take(58), it.setting, it.value, it.departureFromFinest
            )
        )
    }

    println()
    println("--- gate 5: upstream reproductions ".padEnd(112, '-'))
    println("%-10s %-52s %11s %11s %11s".format("source", "quantity", "published", "here", "departure"))
    result.reproductions.forEach {
        println(
            "%-10s %-52s %11.5f %11.5f %11.2e".format(
                it.source, it.quantity.take(52), it.published, it.reproduced, it.relativeDeparture
            )
        )
    }

    println()
    println("--- findings ".padEnd(112, '-'))
    result.findings.forEachIndexed { index, finding ->
        println("${index + 1}. $finding")
        println()
    }

    println("wrote ${output.path} in %.1f s".format((System.currentTimeMillis() - started) / 1000.0))
}
