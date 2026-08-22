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
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
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
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-120` — is a sheet held together by ONE crossover per interface still a plate?
 *
 * Runs `C-0009`'s discreteness criteria down the retained-crossover axis to the connectivity
 * ceiling `C-0054` established, inverts each of them, and then settles what replaces the plate
 * by putting **three** models side by side under `C-0022`'s solved load: the smeared plate, the
 * depleted lattice and the uncoupled beam array.
 *
 * Emits `gpd/results/T-120-connectivity-ceiling-plate.json`.
 */

private const val DUPLEXES = 15
private const val NOMINAL_COLUMNS = 8
private const val INVENTORY = 56
private const val CONNECTIVITY_CEILING = 42
private const val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
private const val RIM_STANDOFF = 1.0
private const val FLATNESS_TOLERANCE = 0.10
private const val PLATE_BASIS_DEGREE = 12

/** Consumption levels: 0, the ceiling, the criterion thresholds around it, and past them. */
private val LEVELS = listOf(0, 14, 28, 31, 34, 37, 40, 42, 45, 49, 52, 56)

/** `C-0009`'s own foundation sweep, so its published row ends are reproduced. */
private val FOUNDATION_MULTIPLIERS = listOf(0.25, 0.5, 1.0, 2.0, 4.0)

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T120BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String
)

@Serializable
private data class T120CriterionRecord(
    val foundationMultiplier: Double,
    val consumed: Int,
    val retained: Int,
    val effectiveInterfacePitch: Double,
    val acrossHelixRigidity: Double,
    val bendingLengthAlongHelices: Double,
    val bendingLengthAcrossHelices: Double,
    val acrossLengthOverPitch: Double,
    val alongLengthOverPitch: Double,
    val acrossLengthOverInterhelical: Double,
    val crossoversInAnchorPatch: Double,
    val continuumValidByMatchedCriteria: Boolean,
    val continuumValidByPatchCount: Boolean
)

@Serializable
private data class T120ThresholdRecord(
    val criterion: String,
    val foundationMultiplier: Double,
    val retainedAtUnity: Double,
    val consumedAtUnity: Double,
    val reachableOnThisSheet: Boolean,
    val againstConnectivityCeiling: String
)

@Serializable
private data class T120CensusRecord(
    val pattern: String,
    val consumed: Int,
    val retained: Int,
    val anchors: Int,
    val continuumPrediction: Double,
    val minimumInPatch: Int,
    val meanInPatch: Double,
    val maximumInPatch: Int,
    val anchorsWithNoCrossover: Int,
    val fractionWithNoCrossover: Double,
    val nearestCrossoverMean: Double,
    val nearestCrossoverMax: Double,
    val bendingLengthAcrossHelices: Double
)

@Serializable
private data class T120ModelRecord(
    val pattern: String,
    val placement: String,
    val profile: String,
    val consumed: Int,
    val retained: Int,
    val components: Int,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val beamArrayPeakDishing: Double,
    val plateDeparture: Double,
    val beamArrayDeparture: Double,
    val nearerModel: String,
    val departureRatio: Double,
    val dishingOverStroke: Double,
    val flat: Boolean
)

@Serializable
private data class T120EnergyRecord(
    val placement: String,
    val consumed: Int,
    val retained: Int,
    val beamEnergy: Double,
    val hingeEnergy: Double,
    val linkEnergy: Double,
    val foundationEnergy: Double,
    val anchorEnergy: Double,
    val hingeShareOfStructural: Double,
    val hingeShareOfTotal: Double
)

@Serializable
private data class T120RegistrationRecord(
    val pattern: String,
    val consumed: Int,
    val retained: Int,
    val samples: Int,
    val minimumCompliance: Double,
    val maximumCompliance: Double,
    val spread: Double,
    val plateMaximumCompliance: Double,
    val plateSpread: Double,
    val beamArrayMaximumCompliance: Double,
    val plateDeparture: Double,
    val beamArrayDeparture: Double,
    val nearerModel: String
)

@Serializable
private data class T120VarianceRecord(
    val consumed: Int,
    val retained: Int,
    val latticeDishingRms: Double,
    val latticeDishingRmsAtDoubleRigidity: Double,
    val latticeSensitivity: Double,
    val plateAreaRms: Double,
    val plateAreaRmsAtDoubleRigidity: Double,
    val plateSensitivity: Double
)

@Serializable
private data class T120ConvergenceRecord(
    val axis: String,
    val setting: String,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
private data class T120ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double,
    /**
     * Whether the published figure is a **number** this study must land on, or a **prose
     * bracket** it must merely be consistent with. `C-0010`'s *"a 2× change in `D_⊥` moves the
     * answer by 2.5 %"* is the second: it is quoted from a three-row table on a 40 × 40 tile,
     * against the 40 × 40.35 footprint every lattice claim uses, and the same lever read on
     * that table's own end points is 3.5 %. Gating on it would be reproducing a rounding.
     */
    val strict: Boolean = true
)

@Serializable
private data class T120Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T120BoundRecord>,
    val criteria: List<T120CriterionRecord>,
    val thresholds: List<T120ThresholdRecord>,
    val census: List<T120CensusRecord>,
    val models: List<T120ModelRecord>,
    val energies: List<T120EnergyRecord>,
    val registration: List<T120RegistrationRecord>,
    val variance: List<T120VarianceRecord>,
    val convergence: List<T120ConvergenceRecord>,
    val reproductions: List<T120ReproductionRecord>,
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
    linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS,
    foundationStiffness: Double = Gen1Tile.FOUNDATION_SECANT,
    hingeSheet: OrigamiSheet = sheet
) = OrigamiGrillage(
    sheet = hingeSheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = foundationStiffness,
    columns = CrossoverLayout.centred(NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0),
    subdivisions = subdivisions,
    linkStiffness = linkStiffness,
    supports = supports,
    consumedCrossovers = consumed
)

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias**. */
private fun solvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-120 consumes the SOLVED edge profile " +
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
    val plateModel = sheet.plate(Gen1Tile.EDGE_X, lengthY)

    val inventory = lattice(sheet, emptySet(), emptyList()).crossoverSites
    check(inventory.size == INVENTORY) {
        "the nominal layout must carry $INVENTORY crossovers, has ${inventory.size}"
    }

    fun criteriaAt(retained: Int, multiplier: Double = 1.0) = latticeDiscreteness(
        retained, INVENTORY, sheet.crossoverSpacing, sheet.interhelicalDistance,
        alongHelices, hinge, Gen1Tile.FOUNDATION_SECANT * multiplier
    )

    println("T-120 — reading C-0022's solved edge profile ...")
    val (smooth, rim) = solvedProfile(ResultInputs.T_3B.file())
    val solvedField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY, listOf(smooth, rim)
    )
    val uniformField = uniformPressure(interiorPressure)
    val profiles = listOf(
        "uniform (the load case the exact zero is written on)" to uniformField,
        "C-0022 solved, 2 mM, 10 nm, 0.192 V" to solvedField
    )

    // ------------------------------------------------------------------ the cheap bounds
    println("T-120 — the cheap bounds, which decide the predicate ...")
    val intact = criteriaAt(INVENTORY)
    val ceiling = criteriaAt(INVENTORY - CONNECTIVITY_CEILING)
    fun threshold(name: String, multiplier: Double): Double = when (name) {
        "patch" -> retainedForPatchCount(
            1.0, INVENTORY, sheet.crossoverSpacing, sheet.interhelicalDistance,
            alongHelices, hinge, Gen1Tile.FOUNDATION_SECANT * multiplier
        )
        "across" -> retainedForAcrossHelixCriterion(
            1.0, INVENTORY, sheet.crossoverSpacing, sheet.interhelicalDistance,
            alongHelices, hinge, Gen1Tile.FOUNDATION_SECANT * multiplier
        )
        else -> retainedForAlongHelixCriterion(
            1.0, INVENTORY, sheet.crossoverSpacing, sheet.interhelicalDistance,
            alongHelices, hinge, Gen1Tile.FOUNDATION_SECANT * multiplier
        )
    }
    val bounds = listOf(
        T120BoundRecord(
            "crossovers in an anchor's influence patch, INTACT sheet",
            intact.crossoversInAnchorPatch, "crossovers",
            "C-0009's own 3.9 — 'four elements is not a continuum', and it is the baseline"
        ),
        T120BoundRecord(
            "crossovers in an anchor's influence patch, at the CONNECTIVITY CEILING",
            ceiling.crossoversInAnchorPatch, "crossovers",
            "UNDER ONE. The declared falsifier was that it stay at or above 1; it did not fire"
        ),
        T120BoundRecord(
            "the retained count at which the patch holds exactly one crossover",
            threshold("patch", 1.0), "crossovers",
            "the continuum reduction's own threshold, inverted rather than evaluated"
        ),
        T120BoundRecord(
            "the SPENT count at which the patch holds exactly one crossover",
            INVENTORY - threshold("patch", 1.0), "crossovers",
            "37 against C-0054's connectivity ceiling of 42: the sheet stops being a " +
                    "continuum FIVE crossovers before it stops being connected"
        ),
        T120BoundRecord(
            "ell_perp/d at the ceiling",
            ceiling.acrossLengthOverInterhelical, "-",
            "1.06, still above 1 — the across-helix matched criterion does NOT see the failure"
        ),
        T120BoundRecord(
            "ell_par/p_eff at the ceiling",
            ceiling.alongLengthOverPitch, "-",
            "0.21 against C-0009's 0.83 intact: the criterion that already failed, failing 4x harder"
        ),
        T120BoundRecord(
            "the retained count ell_par/p_eff would need",
            threshold("along", 1.0), "crossovers",
            "MORE than the sheet owns (56), which is C-0009's finding restated: this criterion " +
                    "fails on the intact sheet and no consumption level can rescue it"
        ),
        T120BoundRecord(
            "the foundation multiplier at which the two ceilings coincide",
            ceiling.crossoversInAnchorPatch * ceiling.crossoversInAnchorPatch, "-",
            "patch scales as k_f^(-1/2), so the continuum and connectivity ceilings meet at " +
                    "0.48x C-0001's secant; softer than that, connectivity fails first"
        )
    )

    // ------------------------------------------------------------------ the criteria and their inversions
    println("T-120 — the four criteria over 5 foundations x ${LEVELS.size} levels ...")
    // a sheet with no crossover at all has no across-helix rigidity and therefore no
    // criterion: that state is the BEAM_ARRAY below, not a depleted plate
    val criteria = FOUNDATION_MULTIPLIERS.flatMap { multiplier ->
        LEVELS.filter { it < INVENTORY }.map { consumed ->
            val retained = INVENTORY - consumed
            val record = criteriaAt(retained, multiplier)
            T120CriterionRecord(
                foundationMultiplier = multiplier,
                consumed = consumed,
                retained = retained,
                effectiveInterfacePitch = record.effectiveInterfacePitch,
                acrossHelixRigidity = record.acrossHelixRigidity,
                bendingLengthAlongHelices = record.bendingLengthAlongHelices,
                bendingLengthAcrossHelices = record.bendingLengthAcrossHelices,
                acrossLengthOverPitch = record.acrossLengthOverPitch,
                alongLengthOverPitch = record.alongLengthOverPitch,
                acrossLengthOverInterhelical = record.acrossLengthOverInterhelical,
                crossoversInAnchorPatch = record.crossoversInAnchorPatch,
                continuumValidByMatchedCriteria = record.continuumValidByMatchedCriteria,
                continuumValidByPatchCount = record.continuumValidByPatchCount
            )
        }
    }

    val thresholds = FOUNDATION_MULTIPLIERS.flatMap { multiplier ->
        listOf(
            "crossovers in an anchor patch (no convention)" to threshold("patch", multiplier),
            "ell_perp/d (matched, across the helices)" to threshold("across", multiplier),
            "ell_par/p_eff (matched, along the helices)" to threshold("along", multiplier)
        ).map { (name, retained) ->
            val consumed = INVENTORY - retained
            T120ThresholdRecord(
                criterion = name,
                foundationMultiplier = multiplier,
                retainedAtUnity = retained,
                consumedAtUnity = consumed,
                reachableOnThisSheet = retained in 0.0..INVENTORY.toDouble(),
                againstConnectivityCeiling = when {
                    retained > INVENTORY ->
                        "unreachable: the criterion fails on the INTACT sheet"
                    consumed < CONNECTIVITY_CEILING ->
                        "the continuum fails FIRST, at $CONNECTIVITY_CEILING - " +
                                "%.1f = %.1f crossovers before severance".format(
                                    consumed, CONNECTIVITY_CEILING - consumed
                                )
                    else -> "connectivity fails first, at $CONNECTIVITY_CEILING"
                }
            )
        }
    }

    // ------------------------------------------------------------------ the two retentions
    /**
     * `C-0054`'s round robin over the interfaces, whose column tie-break takes the lowest
     * available one — so at the ceiling every survivor sits in the two lowest columns.
     */
    val roundRobin = "SPREAD — C-0054's round robin over the interfaces"

    /** One survivor per interface, staggered along the helices; then round robin for the rest. */
    val staggered = "STAGGERED — one per interface, spread along the helices too"

    val staggeredCore = staggeredRetention(inventory, DUPLEXES, NOMINAL_COLUMNS)
    fun retention(pattern: String, consumed: Int): Set<CrossoverSite> {
        val keep = INVENTORY - consumed
        if (pattern == roundRobin || keep < staggeredCore.size) {
            return retainedSites(inventory, consumed, ConsumptionPattern.SPREAD)
        }
        val extra = retainedSites(inventory, consumed, ConsumptionPattern.SPREAD)
            .filterNot { it in staggeredCore }
        return staggeredCore + extra.take(keep - staggeredCore.size)
    }

    // ------------------------------------------------------------------ the integer census
    println("T-120 — the patch census over C-0015's 45 attachment stations, both retentions ...")
    val siteGeometry = lattice(sheet, emptySet(), emptyList()).crossovers
        .associateBy { CrossoverSite(it.lowerBeam, it.column) }
    val anchorGrid = attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lengthY)
    val census = listOf(roundRobin, staggered).flatMap { pattern ->
        LEVELS.filter { it < INVENTORY }.map { consumed ->
            val retained = retention(pattern, consumed)
            val positions = retained.map { siteGeometry.getValue(it).let { c -> c.x to c.y } }
            val record = criteriaAt(retained.size)
            val counts = anchorGrid.map { (x, y) ->
                crossoversInEllipticalPatch(
                    positions, x, y,
                    record.bendingLengthAlongHelices, record.bendingLengthAcrossHelices
                )
            }
            val nearest = anchorGrid.map { (x, y) ->
                positions.minOf { (px, py) -> sqrt((px - x) * (px - x) + (py - y) * (py - y)) }
            }
            T120CensusRecord(
                pattern = pattern,
                consumed = consumed,
                retained = retained.size,
                anchors = anchorGrid.size,
                continuumPrediction = record.crossoversInAnchorPatch,
                minimumInPatch = counts.min(),
                meanInPatch = counts.average(),
                maximumInPatch = counts.max(),
                anchorsWithNoCrossover = counts.count { it == 0 },
                fractionWithNoCrossover = counts.count { it == 0 }.toDouble() / counts.size,
                nearestCrossoverMean = nearest.average(),
                nearestCrossoverMax = nearest.max(),
                bendingLengthAcrossHelices = record.bendingLengthAcrossHelices
            )
        }
    }

    // ------------------------------------------------------------------ three models side by side
    println("T-120 — the three models under C-0022's solved load ...")
    val gridSupports = couplingSupports(anchorGrid, MANDATE)
    val singleColumn = couplingSupports(
        attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY), MANDATE
    )
    val freeStroke = PlateOnFoundation(
        plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = PLATE_BASIS_DEGREE
    ).solve(uniformField).meanDeflection

    fun smearedPlate(retained: Int) = plateModel.copy(
        rigidityY = max(1e-9, plateModel.rigidityY * retained / INVENTORY)
    )

    val placements = listOf(
        "NONE — free tile" to emptyList<PointSupport>(),
        "GRID — C-0015's 3 x 15" to gridSupports,
        "COLUMN — C-0041's 1 x 15" to singleColumn
    )

    /** `C-0009`'s concentrated-lever case — the load path the plate is known to get wrong. */
    val leverLoad = listOf(PointLoad(0.0, 0.0, Gen1Tile.TARGET_FORCE))
    val loadCases: List<Triple<String, PressureField, List<PointLoad>>> =
        profiles.map { (name, field) -> Triple(name, field, emptyList<PointLoad>()) } +
                Triple(
                    "LEVER — one 100 pN point load at the tile centre",
                    uniformPressure(0.0), leverLoad
                )

    val models = buildList {
        listOf(roundRobin, staggered).forEach { pattern ->
        placements.forEach { (placement, supports) ->
            val beamArray = lattice(sheet, inventory.toSet(), supports)
            loadCases.forEach { (profileName, field, points) ->
                val beamDishing = beamArray.solve(field, points).peakDishing()
                LEVELS.forEach { consumed ->
                    val retained = INVENTORY - consumed
                    val spent = inventory.toSet() - retention(pattern, consumed)
                    val latticeDishing =
                        lattice(sheet, spent, supports).solve(field, points).peakDishing()
                    val plateDishing = PlateOnFoundation(
                        smearedPlate(retained), Gen1Tile.FOUNDATION_SECANT, supports,
                        basisDegree = PLATE_BASIS_DEGREE
                    ).solve(field, points).peakDishing()
                    val selection = if (abs(latticeDishing) > 1e-9)
                        modelSelection(latticeDishing, plateDishing, beamDishing) else null
                    add(
                        T120ModelRecord(
                            pattern = pattern,
                            placement = placement,
                            profile = profileName,
                            consumed = consumed,
                            retained = retained,
                            components = sheetComponents(retention(pattern, consumed), DUPLEXES),
                            latticePeakDishing = latticeDishing,
                            platePeakDishing = plateDishing,
                            beamArrayPeakDishing = beamDishing,
                            plateDeparture = selection?.plateDeparture ?: 0.0,
                            beamArrayDeparture = selection?.beamArrayDeparture ?: 0.0,
                            nearerModel = selection?.nearerModel ?: "NEITHER — all three dish zero",
                            departureRatio = selection?.departureRatio ?: 0.0,
                            dishingOverStroke = latticeDishing / freeStroke,
                            flat = latticeDishing / freeStroke < FLATNESS_TOLERANCE
                        )
                    )
                }
            }
        }
        }
    }

    // ------------------------------------------------------------------ the energy split
    println("T-120 — where the strain energy sits ...")
    val energies = listOf("NONE — free tile" to emptyList<PointSupport>(),
        "GRID — C-0015's 3 x 15" to gridSupports).flatMap { (placement, supports) ->
        LEVELS.map { consumed ->
            val spent = consumedSites(inventory, consumed, ConsumptionPattern.SPREAD)
            val model = lattice(sheet, spent, supports)
            val solution = model.solve(solvedField)
            val field = solution.coefficients
            val beam = model.beamEnergy(field)
            val hingeStore = model.hingeEnergy(field)
            val link = model.linkEnergy(field)
            val foundation = model.foundationEnergy(field)
            val anchor = supports.sumOf {
                0.5 * it.stiffness * solution.deflection(it.x, it.y) *
                        solution.deflection(it.x, it.y)
            }
            val structural = beam + hingeStore + link
            T120EnergyRecord(
                placement = placement,
                consumed = consumed,
                retained = INVENTORY - consumed,
                beamEnergy = beam,
                hingeEnergy = hingeStore,
                linkEnergy = link,
                foundationEnergy = foundation,
                anchorEnergy = anchor,
                hingeShareOfStructural = if (structural > 0.0) hingeStore / structural else 0.0,
                hingeShareOfTotal = hingeStore / (structural + foundation + anchor)
            )
        }
    }

    // ------------------------------------------------------------------ homogeneity
    println("T-120 — the registration spread, which needs no length convention ...")
    val cellSamples = 5
    val beamArrayFree = lattice(sheet, inventory.toSet(), emptyList())
    val registration = listOf(roundRobin, staggered).flatMap { pattern ->
        LEVELS.filter { it < INVENTORY }.map { consumed ->
            val spent = inventory.toSet() - retention(pattern, consumed)
            val model = lattice(sheet, spent, emptyList())
            val pitch = criteriaAt(INVENTORY - consumed).effectiveInterfacePitch
            val cellX = minOf(pitch, Gen1Tile.EDGE_X / 2.0)
            val stations = (0 until cellSamples).flatMap { i ->
                (0 until 3).map { j ->
                    val x = -cellX / 2.0 + cellX * i / (cellSamples - 1.0)
                    x to sheet.interhelicalDistance * (j - 1.0)
                }
            }
            fun complianceOf(solve: (Double, Double) -> Double) =
                stations.map { (x, y) -> solve(x, y) }
            val compliances = complianceOf { x, y ->
                model.solve(pointLoads = listOf(PointLoad(x, y, 1.0))).deflection(x, y)
            }
            val plate = PlateOnFoundation(
                smearedPlate(INVENTORY - consumed), Gen1Tile.FOUNDATION_SECANT,
                emptyList(), basisDegree = PLATE_BASIS_DEGREE
            )
            val plateCompliances = complianceOf { x, y ->
                plate.solve(pointLoads = listOf(PointLoad(x, y, 1.0))).deflection(x, y)
            }
            val beamCompliances = complianceOf { x, y ->
                beamArrayFree.solve(pointLoads = listOf(PointLoad(x, y, 1.0))).deflection(x, y)
            }
            val spread = registrationSpread(compliances)
            val plateSpreadRecord = registrationSpread(plateCompliances)
            val selection = modelSelection(
                spread.maximum, plateSpreadRecord.maximum, beamCompliances.max()
            )
            T120RegistrationRecord(
                pattern = pattern,
                consumed = consumed,
                retained = INVENTORY - consumed,
                samples = spread.samples,
                minimumCompliance = spread.minimum,
                maximumCompliance = spread.maximum,
                spread = spread.ratio,
                plateMaximumCompliance = plateSpreadRecord.maximum,
                plateSpread = plateSpreadRecord.ratio,
                beamArrayMaximumCompliance = beamCompliances.max(),
                plateDeparture = selection.plateDeparture,
                beamArrayDeparture = selection.beamArrayDeparture,
                nearerModel = selection.nearerModel
            )
        }
    }

    // ------------------------------------------------------------------ C-0010's insensitivity
    println("T-120 — C-0010's D_perp insensitivity, re-tested at every level ...")
    val workingFoundation = Gen1Tile.FOUNDATION_AT_WORKING_POINT
    val doubledSheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP, crossoverAlpha = 2.0
    )
    val variance = listOf(0, 14, 28, 42).map { consumed ->
        val spent = consumedSites(inventory, consumed, ConsumptionPattern.SPREAD)
        val base = lattice(
            sheet, spent, emptyList(), foundationStiffness = workingFoundation
        ).thermalFluctuation().dishingRms
        val doubled = lattice(
            sheet, spent, emptyList(), foundationStiffness = workingFoundation,
            hingeSheet = doubledSheet
        ).thermalFluctuation().dishingRms
        val retained = INVENTORY - consumed
        val platePlain = PlateOnFoundation(
            smearedPlate(retained), workingFoundation, emptyList(),
            basisDegree = PLATE_BASIS_DEGREE
        ).positionalVarianceBudget().areaRms
        val plateDoubled = PlateOnFoundation(
            smearedPlate(retained).let { it.copy(rigidityY = 2.0 * it.rigidityY) },
            workingFoundation, emptyList(), basisDegree = PLATE_BASIS_DEGREE
        ).positionalVarianceBudget().areaRms
        T120VarianceRecord(
            consumed = consumed,
            retained = retained,
            latticeDishingRms = base,
            latticeDishingRmsAtDoubleRigidity = doubled,
            latticeSensitivity = abs(doubled - base) / base,
            plateAreaRms = platePlain,
            plateAreaRmsAtDoubleRigidity = plateDoubled,
            plateSensitivity = abs(plateDoubled - platePlain) / platePlain
        )
    }

    // ------------------------------------------------------------------ gate 4
    println("T-120 — convergence ...")
    val ceilingSpent = consumedSites(inventory, CONNECTIVITY_CEILING, ConsumptionPattern.SPREAD)
    val meshValues = listOf(1, 2, 4).map { subdivisions ->
        subdivisions to lattice(sheet, ceilingSpent, gridSupports, subdivisions = subdivisions)
            .solve(solvedField).peakDishing()
    }
    val penaltyValues = listOf(1e3, 1e4, 1e5, 1e6).map { penalty ->
        penalty to lattice(sheet, ceilingSpent, gridSupports, linkStiffness = penalty)
            .solve(solvedField).peakDishing()
    }
    val basisValues = listOf(8, 10, 12).map { degree ->
        degree to PlateOnFoundation(
            smearedPlate(INVENTORY - CONNECTIVITY_CEILING), Gen1Tile.FOUNDATION_SECANT,
            gridSupports, basisDegree = degree
        ).solve(solvedField).peakDishing()
    }
    val convergence = buildList {
        val finestMesh = meshValues.last().second
        meshValues.forEach { (subdivisions, value) ->
            add(
                T120ConvergenceRecord(
                    "lattice peak dishing at the ceiling, nested mesh 1 c 2 c 4",
                    "subdivisions = $subdivisions", value,
                    abs(value - finestMesh) / finestMesh
                )
            )
        }
        val finestPenalty = penaltyValues.last().second
        penaltyValues.forEach { (penalty, value) ->
            add(
                T120ConvergenceRecord(
                    "lattice peak dishing at the ceiling, crossover link penalty",
                    "k_link = %.0e".format(penalty), value,
                    abs(value - finestPenalty) / finestPenalty
                )
            )
        }
        val finestBasis = basisValues.last().second
        basisValues.forEach { (degree, value) ->
            add(
                T120ConvergenceRecord(
                    "plate peak dishing at the ceiling, Ritz basis degree",
                    "degree = $degree", value, abs(value - finestBasis) / finestBasis
                )
            )
        }
        // the census is a count, so its convergence axis is the sample grid, not a mesh —
        // and it is taken on the STAGGERED retention, because on the round robin every
        // survivor sits at low x and a one-column grid at x = 0 then reports exactly zero,
        // which is a statement about that tie-break rather than about the sampling
        val retainedCeiling = staggeredCore
            .map { siteGeometry.getValue(it).let { c -> c.x to c.y } }
        val record = criteriaAt(INVENTORY - CONNECTIVITY_CEILING)
        val meanValues = listOf(15, 45, 135).map { anchors ->
            val grid = attachmentGrid(anchors / DUPLEXES, DUPLEXES, Gen1Tile.EDGE_X, lengthY)
            anchors to grid.map { (x, y) ->
                crossoversInEllipticalPatch(
                    retainedCeiling, x, y,
                    record.bendingLengthAlongHelices, record.bendingLengthAcrossHelices
                )
            }.average()
        }
        val finestMean = meanValues.last().second
        meanValues.forEach { (anchors, mean) ->
            add(
                T120ConvergenceRecord(
                    "mean crossovers in an anchor patch at the ceiling, sample grid",
                    "$anchors stations", mean, abs(mean - finestMean) / finestMean
                )
            )
        }
    }

    // ------------------------------------------------------------------ gate 5
    println("T-120 — upstream reproductions ...")
    fun reproduction(
        source: String,
        quantity: String,
        published: Double,
        here: Double,
        strict: Boolean = true
    ) = T120ReproductionRecord(
        source, quantity, published, here, abs(here - published) / abs(published), strict
    )

    fun model(pattern: String, placement: String, profile: String, consumed: Int) =
        models.first {
            it.pattern == pattern && it.placement.startsWith(placement) &&
                    it.profile.startsWith(profile) && it.consumed == consumed
        }
    val gridDishing = model(roundRobin, "GRID", "C-0022", 0)
    val columnDishing = model(roundRobin, "COLUMN", "C-0022", 0)
    val freeDishing = model(roundRobin, "NONE", "C-0022", 0)
    val ceilingDishing = model(roundRobin, "GRID", "C-0022", CONNECTIVITY_CEILING)
    val reproductions = listOf(
        reproduction("C-0009", "D_parallel = EI/d [pN nm]", 85.50, plateModel.rigidityX),
        reproduction("C-0009", "D_perp = k_theta d/p [pN nm]", 3.345, plateModel.rigidityY),
        reproduction(
            "C-0009", "bending anisotropy D_par/D_perp", 25.56,
            plateModel.rigidityX / plateModel.rigidityY
        ),
        reproduction(
            "C-0009", "ell_par [nm], nominal foundation", 9.07,
            intact.bendingLengthAlongHelices
        ),
        reproduction(
            "C-0009", "ell_perp [nm], nominal foundation", 4.03,
            intact.bendingLengthAcrossHelices
        ),
        reproduction("C-0009", "ell_par/p intact, nominal", 0.83, intact.alongLengthOverPitch),
        reproduction(
            "C-0009", "ell_perp/d intact, nominal", 1.50, intact.acrossLengthOverInterhelical
        ),
        reproduction(
            "C-0006", "ell_perp/p intact, nominal (the MISMATCHED one)", 0.37,
            intact.acrossLengthOverPitch
        ),
        reproduction(
            "C-0009", "crossovers in an anchor patch, intact", 3.9,
            intact.crossoversInAnchorPatch
        ),
        reproduction(
            "C-0009", "ell_par/p at 0.25x the foundation", 1.18,
            criteriaAt(INVENTORY, 0.25).alongLengthOverPitch
        ),
        reproduction(
            "C-0009", "crossovers in a patch at 0.25x the foundation", 7.9,
            criteriaAt(INVENTORY, 0.25).crossoversInAnchorPatch
        ),
        reproduction(
            "C-0009", "ell_perp/d at 4x the foundation", 1.06,
            criteriaAt(INVENTORY, 4.0).acrossLengthOverInterhelical
        ),
        reproduction(
            "C-0009", "crossovers in a patch at 4x the foundation", 2.0,
            criteriaAt(INVENTORY, 4.0).crossoversInAnchorPatch
        ),
        reproduction(
            "C-0054", "the connectivity ceiling [crossovers]", 42.0,
            maximumConsumedForConnectivity(INVENTORY, DUPLEXES).toDouble()
        ),
        reproduction(
            "C-0054", "the Voigt/Reuss ratio on a uniform lattice", (15.0 / 14.0) * (15.0 / 14.0),
            uniformMomentRigidity(
                List(DUPLEXES - 1) { 1 }, hinge, Gen1Tile.EDGE_X, lengthY
            ) / uniformCurvatureRigidity(
                DUPLEXES - 1, hinge, sheet.interhelicalDistance, area
            )
        ),
        reproduction("C-0047", "dishing/stroke, 3 x 15, solved", 0.218, gridDishing.dishingOverStroke),
        reproduction("C-0047", "dishing/stroke, 1 x 15, solved", 0.695, columnDishing.dishingOverStroke),
        reproduction("C-0022", "dishing/stroke, free uncoupled tile", 0.308, freeDishing.dishingOverStroke),
        reproduction("C-0054", "dishing/stroke, 3 x 15 at the ceiling", 0.242, ceilingDishing.dishingOverStroke),
        reproduction(
            "C-0010", "the plate's D_perp insensitivity to a factor of two", 0.025,
            variance.first { it.consumed == 0 }.plateSensitivity, strict = false
        )
    )

    // ------------------------------------------------------------------ the runtime falsifiers
    println("T-120 — the runtime falsifiers ...")
    models.filter { it.profile.startsWith("uniform") && it.placement.startsWith("NONE") }
        .forEach {
            check(abs(it.latticePeakDishing) < 1e-9 && abs(it.platePeakDishing) < 1e-9) {
                "a uniform load must dish a free tile exactly zero at ${it.consumed} spent, " +
                        "got ${it.latticePeakDishing} (lattice) and ${it.platePeakDishing} (plate)"
            }
        }
    check(ceiling.crossoversInAnchorPatch < 1.0) {
        "falsifier 1: the patch count at the ceiling is ${ceiling.crossoversInAnchorPatch}"
    }
    check(reproductions.filter { it.strict }.all { it.relativeDeparture < 0.02 }) {
        "an upstream reproduction departed by more than 2 %: " +
                reproductions.filter { it.strict }.maxBy { it.relativeDeparture }
    }
    // the one non-strict row must still be the SAME statement: foundation-dominated, i.e.
    // a factor of two in D_perp worth single-digit percent and not tens
    check(reproductions.first { !it.strict }.reproduced < 0.10) {
        "C-0010's shape modes are no longer foundation-dominated on the intact sheet"
    }
    // the ordering the whole verdict rests on, asserted rather than read off a table
    val orderedThresholds = listOf(
        registration.first { it.pattern == staggered && it.nearerModel == "BEAM_ARRAY" }
            .consumed.toDouble(),
        INVENTORY - threshold("patch", 1.0),
        CONNECTIVITY_CEILING.toDouble(),
        models.first {
            it.pattern == staggered && it.placement.startsWith("GRID") &&
                    it.profile.startsWith("C-0022") && it.nearerModel == "BEAM_ARRAY"
        }.consumed.toDouble()
    )
    check(orderedThresholds.zipWithNext().all { (a, b) -> a < b }) {
        "the four thresholds are not in the order the verdict states: $orderedThresholds"
    }

    // ------------------------------------------------------------------ the findings
    val patchThreshold = threshold("patch", 1.0)
    val crossing = models.filter {
        it.pattern == staggered && it.placement.startsWith("GRID") &&
                it.profile.startsWith("C-0022")
    }.firstOrNull { it.nearerModel == "BEAM_ARRAY" }
    val leverCrossing = models.filter {
        it.pattern == staggered && it.placement.startsWith("NONE") &&
                it.profile.startsWith("LEVER")
    }.firstOrNull { it.nearerModel == "BEAM_ARRAY" }
    val smoothAtCeiling = models.first {
        it.pattern == staggered && it.placement.startsWith("GRID") &&
                it.profile.startsWith("C-0022") && it.consumed == CONNECTIVITY_CEILING
    }
    val leverAtCeiling = models.first {
        it.pattern == staggered && it.placement.startsWith("NONE") &&
                it.profile.startsWith("LEVER") && it.consumed == CONNECTIVITY_CEILING
    }
    val registrationAtCeiling = registration.first {
        it.pattern == staggered && it.consumed == CONNECTIVITY_CEILING
    }
    val complianceCrossing = registration.filter { it.pattern == staggered }
        .firstOrNull { it.nearerModel == "BEAM_ARRAY" }
    val censusRoundRobin = census.first {
        it.pattern == roundRobin && it.consumed == CONNECTIVITY_CEILING
    }
    val censusStaggered = census.first {
        it.pattern == staggered && it.consumed == CONNECTIVITY_CEILING
    }
    val findings = listOf(
        ("The predicate, answered in closed form: at C-0054's connectivity ceiling an anchor's " +
                "influence patch contains %.3f crossovers, against C-0009's %.1f on the intact " +
                "sheet. Inverted rather than evaluated, the patch reaches exactly one at " +
                "%.1f retained, i.e. %.1f SPENT — so the sheet stops being a continuum FIVE " +
                "crossovers before it stops being connected, and the plate reduction fails " +
                "INSIDE the region C-0054 declares buildable.").format(
            ceiling.crossoversInAnchorPatch, intact.crossoversInAnchorPatch,
            patchThreshold, INVENTORY - patchThreshold
        ),
        ("The three criteria disagree, and which one a design trusts decides the answer. " +
                "ell_perp/d reads %.3f at the ceiling — still above one, so the across-helix " +
                "matched criterion never sees the failure at all; it crosses one only at " +
                "%.1f spent, past the ceiling. ell_par/p_eff reads %.3f, and it demands " +
                "%.1f crossovers, MORE than the sheet owns, so it fails on the intact sheet " +
                "and cannot distinguish the ceiling from anything. Only the patch count, " +
                "which carries no direction convention, crosses inside the design region.").format(
            ceiling.acrossLengthOverInterhelical, INVENTORY - threshold("across", 1.0),
            ceiling.alongLengthOverPitch, threshold("along", 1.0)
        ),
        ("Depleting the crossovers to a quarter is EXACTLY a fourfold stiffer foundation for " +
                "ell_perp/d — both divide D_perp/k_f by four — and it is not for the other " +
                "two, which separate by exactly 2 sqrt 2. That is why the ceiling's %.3f is " +
                "the same number as C-0009's own 4x-foundation corner, and it is a warning: " +
                "an across-helix criterion cannot tell a depleted sheet from a stiffer layer.").format(
            ceiling.acrossLengthOverInterhelical
        ),
        ("The continuum density and the integer census stop being the same statement below one " +
                "crossover per patch. At the ceiling the density says %.3f, and over C-0015's " +
                "45 attachment stations %d of them (%d on C-0054's own round robin, whose " +
                "column tie-break clusters every survivor into the two lowest columns) have " +
                "NO retained crossover inside their own influence patch. The load path from " +
                "those anchors reaches the foundation and never reaches the sheet's " +
                "across-helix path at all — and WHICH crossovers survive is now a design " +
                "variable: the mean distance from an anchor to the nearest surviving " +
                "crossover is %.2f nm staggered against %.2f nm on the round robin, " +
                "%.2fx, at identical count and identical density.").format(
            ceiling.crossoversInAnchorPatch, censusStaggered.anchorsWithNoCrossover,
            censusRoundRobin.anchorsWithNoCrossover,
            censusStaggered.nearestCrossoverMean, censusRoundRobin.nearestCrossoverMean,
            censusRoundRobin.nearestCrossoverMean / censusStaggered.nearestCrossoverMean
        ),
        ("C-0009's own split survives the ceiling and is what settles 'is it still a plate': " +
                "the answer depends on how the load meets the sheet. Under C-0022's SMOOTH " +
                "solved load on the 3 x 15 grid the smeared plate is still within %.1f %% of " +
                "the lattice at the ceiling and is the nearer model; under a CONCENTRATED " +
                "100 pN lever it is out by %.1f %% on the staggered retention and %.1f %% on " +
                "C-0054's round robin — 2 to 5 times worse, and the SPREAD depends on where " +
                "the load lands relative to the survivors, which is itself the finding. The " +
                "point compliance a coupling actually feels varies by %.2fx over one " +
                "crossover cell where the plate says %.2fx: a plate cannot be inhomogeneous, " +
                "so that spread is not an error in the plate's number, it is a quantity the " +
                "plate does not have.").format(
            100.0 * smoothAtCeiling.plateDeparture, 100.0 * leverAtCeiling.plateDeparture,
            100.0 * models.first {
                it.pattern == roundRobin && it.placement.startsWith("NONE") &&
                        it.profile.startsWith("LEVER") && it.consumed == CONNECTIVITY_CEILING
            }.plateDeparture,
            registrationAtCeiling.spread, registrationAtCeiling.plateSpread
        ),
        (if (crossing != null && complianceCrossing != null)
            ("What replaces the plate, and the four thresholds fall in a strict order that " +
                    "no single criterion would have produced. The uncoupled BEAM ARRAY — " +
                    "fifteen Euler-Bernoulli duplexes on one shared Winkler foundation — " +
                    "becomes the nearer model to the lattice at %d spent on the POINT " +
                    "COMPLIANCE, at %.1f retained on the patch criterion, and only at %d " +
                    "spent on the SMOOTH dishing, which is severance. So: %d (a coupling's " +
                    "own stiffness) < %.0f (the continuum criterion) < %d (connectivity) " +
                    "< %d (a smooth load notices). A design that reads any one of these as " +
                    "'where the plate fails' is off by up to %.2fx in the crossover count. " +
                    "The concentrated lever crosses at %d spent on C-0054's round robin and " +
                    "%d on the staggered retention, which brackets the middle of that order " +
                    "and is the one number here that a PLACEMENT moves.")
                .format(
                    complianceCrossing.consumed, patchThreshold, crossing.consumed,
                    complianceCrossing.consumed, INVENTORY - patchThreshold,
                    CONNECTIVITY_CEILING, crossing.consumed,
                    crossing.consumed.toDouble() / complianceCrossing.consumed,
                    models.first {
                        it.pattern == roundRobin && it.placement.startsWith("NONE") &&
                                it.profile.startsWith("LEVER") && it.nearerModel == "BEAM_ARRAY"
                    }.consumed,
                    leverCrossing?.consumed ?: -1
                )
        else
            "The smeared plate remains the nearer model to the lattice at every consumption " +
                    "level swept, which would falsify the replacement argument — recorded " +
                    "rather than repaired."),
        ("C-0054's harmonic/smeared discrepancy is NOT a modelling artefact and it is also not " +
                "what fails first. At and below the ceiling every interface still holds a " +
                "crossover, the two conventions agree to (15/14)^2 = %.4f exactly, and the " +
                "series reading is finite. The collapse to zero is a property of the FIRST " +
                "EMPTY interface, which is at 43 spent — one past the ceiling and six past " +
                "where the patch criterion has already refused the plate.").format(
            (15.0 / 14.0) * (15.0 / 14.0)
        )
    )

    val result = T120Result(
        task = "T-120 — is a sheet held together by ONE crossover per interface still a plate?",
        leaf = "A8.2 (structural rigidity), with A1.2 for the anchoring scheme",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; " +
                "40.0 x 40.35 nm tile, 15 duplexes at the SAXS-measured 2.69 nm; " +
                "8 symmetrically centred crossover columns, 56 crossovers; C-0022's SOLVED " +
                "edge profile at 2 mM, 10 nm, 0.192 V; C-0017's 33.3333 pN/nm mandate",
        decision = "The depleted lattice is parametrised by ONE number, the retained crossover " +
                "count, through p_eff = p N/N_ret and D_perp = k_theta d/p_eff. Both reduce to " +
                "C-0009's own constants at N_ret = N exactly, so every criterion below is that " +
                "claim's published number times an exact power of the retained count — and " +
                "each therefore INVERTS, which is what the task needs and what evaluating " +
                "them at 14 would not have given.",
        bounds = bounds,
        criteria = criteria,
        thresholds = thresholds,
        census = census,
        models = models,
        energies = energies,
        registration = registration,
        variance = variance,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings,
        parameters = mapOf(
            "duplexes" to DUPLEXES.toDouble(),
            "interfaces" to (DUPLEXES - 1).toDouble(),
            "crossoverColumns" to NOMINAL_COLUMNS.toDouble(),
            "inventory" to INVENTORY.toDouble(),
            "connectivityCeiling" to CONNECTIVITY_CEILING.toDouble(),
            "retainedAtCeiling" to (INVENTORY - CONNECTIVITY_CEILING).toDouble(),
            "hingeStiffness" to hinge,
            "interhelicalDistance" to sheet.interhelicalDistance,
            "crossoverSpacing" to sheet.crossoverSpacing,
            "alongHelixRigidity" to alongHelices,
            "edgeX" to Gen1Tile.EDGE_X,
            "edgeY" to lengthY,
            "interiorPressure" to interiorPressure,
            "foundationSecant" to Gen1Tile.FOUNDATION_SECANT,
            "foundationAtWorkingPoint" to workingFoundation,
            "mandate" to MANDATE,
            "freeTileStroke" to freeStroke,
            "patchThresholdRetained" to patchThreshold,
            "patchThresholdSpent" to (INVENTORY - patchThreshold),
            "solvedTaperDepth" to smooth.depth,
            "solvedTaperWidth" to smooth.width,
            "solvedRimDepth" to rim.depth
        )
    )

    val output = File("gpd/results/T-120-connectivity-ceiling-plate.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    output.writeText(
        json.encodeToString(
            (json.encodeToJsonElement(result) as JsonObject).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)
        )
    )
    t120Report(result, output, started)
}

// ---------------------------------------------------------------------------------------------
// the report
// ---------------------------------------------------------------------------------------------

private fun t120Report(result: T120Result, output: File, started: Long) {
    println()
    println("=".repeat(114))
    println(result.task)
    println("=".repeat(114))
    println()
    println(result.decision)

    println()
    println("--- the cheap bounds ".padEnd(114, '-'))
    result.bounds.forEach {
        println("%-64s %14.5f %-12s".format(it.name.take(64), it.value, it.unit))
        println("    %s".format(it.settles))
    }

    println()
    println("--- the four criteria at the nominal foundation ".padEnd(114, '-'))
    println(
        "%6s %6s %10s %10s %10s %10s %10s %10s".format(
            "spent", "kept", "p_eff", "D_perp", "l_perp", "l_perp/p", "l_par/p", "in patch"
        )
    )
    result.criteria.filter { it.foundationMultiplier == 1.0 }.forEach {
        println(
            "%6d %6d %10.3f %10.4f %10.4f %10.4f %10.4f %10.4f".format(
                it.consumed, it.retained, it.effectiveInterfacePitch, it.acrossHelixRigidity,
                it.bendingLengthAcrossHelices, it.acrossLengthOverInterhelical,
                it.alongLengthOverPitch, it.crossoversInAnchorPatch
            )
        )
    }

    println()
    println("--- the inversions: the count each criterion crosses ONE at ".padEnd(114, '-'))
    println("%-46s %6s %10s %10s  %s".format("criterion", "k_f x", "kept", "spent", "verdict"))
    result.thresholds.forEach {
        println(
            "%-46s %6.2f %10.2f %10.2f  %s".format(
                it.criterion.take(46), it.foundationMultiplier, it.retainedAtUnity,
                it.consumedAtUnity, it.againstConnectivityCeiling.take(46)
            )
        )
    }

    println()
    println("--- the integer census over C-0015's 45 stations ".padEnd(114, '-'))
    println(
        "%-12s %6s %6s %10s %5s %8s %5s %6s %12s".format(
            "pattern", "spent", "kept", "density", "min", "mean", "max", "none", "nearest[nm]"
        )
    )
    result.census.forEach {
        println(
            "%-12s %6d %6d %10.4f %5d %8.4f %5d %6d %12.3f".format(
                it.pattern.take(12), it.consumed, it.retained, it.continuumPrediction,
                it.minimumInPatch, it.meanInPatch, it.maximumInPatch,
                it.anchorsWithNoCrossover, it.nearestCrossoverMean
            )
        )
    }

    println()
    println("--- three models under C-0022's SOLVED load ".padEnd(114, '-'))
    println(
        "%-11s %-16s %-8s %5s %5s %10s %10s %10s %11s".format(
            "pattern", "placement", "profile", "spent", "piece", "lattice", "plate",
            "beams", "nearer"
        )
    )
    result.models.filter { !it.profile.startsWith("uniform") }.forEach {
        println(
            "%-11s %-16s %-8s %5d %5d %10.4f %10.4f %10.4f %11s".format(
                it.pattern.take(11), it.placement.take(16), it.profile.take(8), it.consumed,
                it.components, it.latticePeakDishing, it.platePeakDishing,
                it.beamArrayPeakDishing, it.nearerModel.take(11)
            )
        )
    }

    println()
    println("--- where the strain energy sits, under the solved load ".padEnd(114, '-'))
    println(
        "%-24s %6s %12s %12s %12s %12s".format(
            "placement", "spent", "beams", "hinges", "foundation", "hinge share"
        )
    )
    result.energies.forEach {
        println(
            "%-24s %6d %12.5f %12.5f %12.5f %12.5f".format(
                it.placement.take(24), it.consumed, it.beamEnergy, it.hingeEnergy,
                it.foundationEnergy, it.hingeShareOfTotal
            )
        )
    }

    println()
    println("--- homogeneity: the point compliance over one crossover cell ".padEnd(114, '-'))
    println(
        "%-12s %6s %6s %12s %12s %8s %10s %10s %12s".format(
            "pattern", "spent", "kept", "lat max", "plate max", "spread", "plate sprd",
            "beams max", "nearer"
        )
    )
    result.registration.forEach {
        println(
            "%-12s %6d %6d %12.6f %12.6f %8.4f %10.4f %10.6f %12s".format(
                it.pattern.take(12), it.consumed, it.retained, it.maximumCompliance,
                it.plateMaximumCompliance, it.spread, it.plateSpread,
                it.beamArrayMaximumCompliance, it.nearerModel.take(12)
            )
        )
    }

    println()
    println("--- C-0010's D_perp insensitivity, re-tested ".padEnd(114, '-'))
    println("%6s %6s %12s %12s %12s %12s".format(
        "spent", "kept", "lat RMS", "lat x2", "lat sens", "plate sens"
    ))
    result.variance.forEach {
        println(
            "%6d %6d %12.5f %12.5f %12.5f %12.5f".format(
                it.consumed, it.retained, it.latticeDishingRms,
                it.latticeDishingRmsAtDoubleRigidity, it.latticeSensitivity,
                it.plateSensitivity
            )
        )
    }

    println()
    println("--- gate 4: convergence ".padEnd(114, '-'))
    println("%-60s %-18s %14s %12s".format("axis", "setting", "value", "departure"))
    result.convergence.forEach {
        println(
            "%-60s %-18s %14.7f %12.3e".format(
                it.axis.take(60), it.setting, it.value, it.departureFromFinest
            )
        )
    }

    println()
    println("--- gate 5: upstream reproductions ".padEnd(114, '-'))
    println(
        "%-10s %-52s %11s %11s %11s".format(
            "source", "quantity", "published", "here", "departure"
        )
    )
    result.reproductions.forEach {
        println(
            "%-10s %-52s %11.5f %11.5f %11.2e".format(
                it.source, it.quantity.take(52), it.published, it.reproduced,
                it.relativeDeparture
            )
        )
    }

    println()
    println("--- findings ".padEnd(114, '-'))
    result.findings.forEachIndexed { index, finding ->
        println("${index + 1}. $finding")
        println()
    }

    println("wrote ${output.path} in %.1f s".format((System.currentTimeMillis() - started) / 1000.0))
}
