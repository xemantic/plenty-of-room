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

import com.xemantic.nano.plentyofroom.anchoring.maximumPlanCeilingForCount
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.latticeInfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.coupling.winklerBendingLength
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
import com.xemantic.nano.plentyofroom.electrostatics.BluntEndStacking
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundForResult
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

// ---------------------------------------------------------------------------------------------
// T-245 -- re-select the honeycomb two-length row pair on SCAFFOLD CLOSURE, and re-grade there.
//
// C-0148 showed that C-0140's recommended 112 / 108 bp raster cannot be drawn on caDNAno's
// default rules: no lattice constant b0 serves its 59 raster crossovers and 10 of them would
// have to be FORCED. C-0140's five candidates were selected under a filter -- "a stagger of at
// most 4 bp" -- that CH-0187 shows is unstated and that has nothing to do with closure. So the
// selection is re-run inside the CLOSING family, and C-0146's eight 10 x 6 coupled cells are
// re-graded at whatever it returns, on that pair's own ROW-DERIVED crossover-column count.
//
// The cheap bound is exhaustive: closure depends on the two lengths only through their residues
// modulo 21, so 441 cases settle the family before any width, scaffold or solve.
// ---------------------------------------------------------------------------------------------

private const val T245_SAMPLES: Int = 81
private const val T245_TOLERANCE: Double = 0.10
private const val T245_RIM_STANDOFF: Double = 1.0
private const val T245_RIM_BAND: Double = 6.7
private const val T245_SEED: Long = 197_197L
private const val T245_BAND_LOW: Double = 0.26
private const val T245_BAND_MEASURED: Double = 0.30
private const val T245_DECLARED_REALISATIONS: Int = 4000
private const val T245_NOMINAL_WIDTH: Double = 40.0

/**
 * What a **solved** field of this lattice can be asserted to.
 *
 * `CLAUDE.md`: two solves of two *identically constructed* `OrigamiGrillage` objects in one JVM
 * differ by ~4 ulp, the JIT recompiling a hot reduction between the two calls. So the same-tile
 * identity is a threshold, never a value.
 */
private const val T245_SOLVED_FIELD_TOLERANCE: Double = 1e-10

/** `C-0140`'s recommendation, which `C-0148` showed cannot be drawn — the control throughout. */
private const val T245_BASELINE_ONE: Int = 112
private const val T245_BASELINE_TWO: Int = 108

/** `C-0069`'s recommended output element, for the plan-ceiling reading. */
private const val T245_ELEMENT_LENGTH: Double = 8.16439083

/** The length range the closing family is enumerated over, and the stagger ceiling. */
private const val T245_MIN_LENGTH: Int = 60
private const val T245_MAX_LENGTH: Int = 200
private const val T245_MAX_STAGGER: Int = 42

private val t245Realisations: Int =
    System.getenv("T245_REALISATIONS")?.toIntOrNull() ?: T245_DECLARED_REALISATIONS

/**
 * This number rendered for a **sentence**, at a stated precision.
 *
 * `C-0150`: a raw `Double.toString()` interpolated into prose carries the shortest round-trip
 * decimal, up to seventeen digits, so a JIT recompilation of a hot reduction moves the sentence
 * while the file's own numeric fields do not. The floor is **dimensionless** here, not
 * [com.xemantic.nano.plentyofroom.structure.RESULT_ABSOLUTE_FLOOR]'s claim in the locked units
 * (`P-18`).
 */
private fun Double.t245Rounded(digits: Int = 9, floor: Double = 1e-12): Double =
    roundForResult(this, digits, floor)

// ------------------------------------------------------------------------------ the records

@Serializable
private class T245ClosureCell(
    val crossSection: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val firstAxialSign: Int,
    val mirrored: Boolean,
    val axialReversed: Boolean,
    val rasterCrossovers: Int,
    val distinctReducedResidues: List<Int>,
    val closes: Boolean,
    val offRuleCrossovers: Int
)

@Serializable
private class T245ResidueClass(
    val crossSection: String,
    val senseOneResidue: Int,
    val senseTwoResidue: Int,
    val differenceModulo21: Int,
    val leastAbsoluteStagger: Int
)

@Serializable
private class T245FamilyMember(
    val crossSection: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val closes: Boolean,
    val offRuleCrossovers: Int,
    val staggerBasePairs: Int,
    val rowSpanBasePairs: Int,
    val blockExtentBasePairs: Int,
    val blockExtentNm: Double,
    val extentDeparturePercent: Double,
    val interfaceWindowBasePairs: Int,
    val interfaceWindowNm: Double,
    val rowDerivedColumns: Int,
    val rowSpanColumns: Int,
    val boundingBoxColumns: Int,
    val guardIsInertOnTheInterface: Boolean,
    val scaffoldNucleotides: Int,
    val scaffoldSpareOnM13: Int,
    val fitsM13: Boolean,
    val unpairedNucleotidesPerHelixOnM13: Int,
    val frontFaceRaggednessBasePairs: Int,
    val frontFaceRaggednessNm: Double,
    val rearFaceRaggednessBasePairs: Int,
    val stackingClearanceNm: Double,
    val stackingClearanceRises: Double,
    val classZeroResidue: Int?,
    val ladderPhaseBasePairs: Int?,
    val interRowOffsetBasePairs: Int,
    val stationsOnFace: Int?,
    val sparsestRowStations: Int?,
    val stationsAtSaturation: Int
)

@Serializable
private class T245Axis(
    val axis: String,
    val units: String,
    val betterIs: String,
    val recommended: String,
    val baseline: String,
    val winner: String,
    val note: String
)

@Serializable
private class T245ColumnCount(
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val windowReading: String,
    val windowBasePairs: Int,
    val windowNm: Double,
    val edgeMargin: Double,
    val edgeMarginConvention: String,
    val crossoverColumns: Int,
    val slackBeyondLastPitch: Double
)

@Serializable
private class T245PlanCeiling(
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val latticeDescription: String,
    val stationsOnFace: Int,
    val demandedPaths: Int,
    val maximumPlanCeiling: Double?,
    val affordsRecommendedElement: Boolean?
)

@Serializable
private class T245Geometry(
    val label: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val widthReading: String,
    val rasterRows: Int,
    val layers: Int,
    val compositeFraction: Double,
    val edgeX: Double,
    val edgeY: Double,
    val crossoverColumns: Int,
    val columnReading: String,
    val interiorPressure: Double,
    val freeStroke: Double,
    val reachAlong: Double,
    val reachAcross: Double
)

@Serializable
private class T245Reference(
    val label: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val crossoverColumns: Int,
    val compositeFraction: Double,
    val uncoupledDishingOverStroke: Double,
    val flat: Boolean
)

@Serializable
private class T245Cell(
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val closes: Boolean,
    val crossoverColumns: Int,
    val columnReading: String,
    val placement: String,
    val ladderPhase: Int,
    val ladderOffset: Int,
    val compositeFraction: Double,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val perPathStiffness: Double,
    val totalStiffness: Double,
    val alongHelixSnapDeparture: Double,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double?,
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val uncoupledDishingOverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T245Paired(
    val comparison: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val numeratorP90: Double,
    val denominatorP90: Double,
    val ratioOfPercentiles: Double,
    val medianOfRatios: Double,
    val p90OfRatios: Double,
    val bestRatio: Double,
    val worstRatio: Double,
    val fractionAbove: Double,
    val signsAgree: Boolean
)

@Serializable
private class T245Convergence(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departure: Double?
)

@Serializable
private class T245Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double
)

@Serializable
private class T245Falsifier(
    val id: String,
    val statement: String,
    val declaredOpen: Boolean,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private class T245Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: Map<String, String>,
    val closureSweep: List<T245ClosureCell>,
    val closingResidueClasses: List<T245ResidueClass>,
    val closingFamily: List<T245FamilyMember>,
    val selectionAxes: List<T245Axis>,
    val columnCounts: List<T245ColumnCount>,
    val planCeilings: List<T245PlanCeiling>,
    val geometries: List<T245Geometry>,
    val references: List<T245Reference>,
    val cells: List<T245Cell>,
    val paired: List<T245Paired>,
    val verdict: Map<String, String>,
    val convergence: List<T245Convergence>,
    val reproductions: List<T245Reproduction>,
    val falsifiers: List<T245Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ------------------------------------------------------------------------------ the load

private class T245Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T245_RIM_STANDOFF))
        )
}

private fun t245Profile(file: File): T245Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T245Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

private fun t245Published(file: File, section: String, predicate: (JsonObject) -> Boolean,
                          key: String): Double {
    require(file.exists()) { "upstream result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(section).jsonArray.map { it.jsonObject }.first(predicate)
    return record.getValue(key).jsonPrimitive.content.toDouble()
}

// ------------------------------------------------------------------------------ the tile

/**
 * A four-layer honeycomb tile at a stated axial width and a stated **crossover-column count**.
 *
 * `C-0142`'s tile with the column count lifted out of the width: `T-243` established that a
 * crossover column serves an **interface** and its window is the intersection of two row spans,
 * not the bounding box, so the count is no longer a function of `edgeX` at all.
 */
private class T245Tile(
    val label: String,
    val rasterRows: Int,
    val layers: Int,
    val edgeX: Double,
    val edgeY: Double,
    val inPlanePitch: Double,
    val layerSpacing: Double,
    val compositeFraction: Double,
    val crossoverColumns: Int,
    private val profile: T245Profile
) {

    init {
        require(abs(rasterRows * inPlanePitch - edgeY) < 1e-9 * edgeY) {
            "edgeY " + edgeY + " is not " + rasterRows + " rows at pitch " + inPlanePitch
        }
        require(crossoverColumns >= 1) {
            "crossoverColumns must be at least one, was: " + crossoverColumns
        }
    }

    val rigidities: MultiLayerRigidities = multiLayerRigidities(
        layers = layers,
        interhelicalDistance = inPlanePitch,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = compositeFraction,
        layerSpacing = layerSpacing
    )

    private val sheet = equivalentSheet(rigidities)

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val reachAlong: Double =
        winklerBendingLength(rigidities.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT)

    val reachAcross: Double =
        winklerBendingLength(rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT)

    val lattice: OrigamiGrillage by lazy {
        OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = rasterRows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(crossoverColumns, sheet.crossoverSpacing / 2.0),
            subdivisions = 2
        )
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(pressureField).peakDishing(T245_SAMPLES) / freeStroke
    }
}

private class T245Graded(val cell: T245Cell, val sample: DoubleArray)

@Suppress("LongParameterList")
private fun gradeT245Cell(
    tile: T245Tile,
    config: T245Config,
    columns: Int,
    grid: List<Pair<Double, Double>>,
    snapDeparture: Double,
    distribution: String,
    stiffnesses: List<Double>,
    realisations: Int
): T245Graded {
    val surrogate = latticeInfluenceSurrogate(tile.lattice, grid, tile.pressureField, T245_SAMPLES)
    val incorporation = measuredDepthIncorporation(tile.edgeX, tile.edgeY)
    val ensemble = dropoutEnsemble(
        grid.map { (x, y) -> incorporation.at(x, y) }, realisations, T245_SEED
    )
    val nominal = surrogate.solve(stiffnesses).peakDishing / tile.freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T245_TOLERANCE
    )
    return T245Graded(
        T245Cell(
            senseOneBasePairs = config.senseOne,
            senseTwoBasePairs = config.senseTwo,
            closes = config.closes,
            crossoverColumns = tile.crossoverColumns,
            columnReading = config.columnReading,
            placement = config.placement,
            ladderPhase = config.ladderPhase,
            ladderOffset = config.ladderOffset,
            compositeFraction = tile.compositeFraction,
            columns = columns,
            rows = tile.rasterRows,
            pathCount = grid.size,
            distribution = distribution,
            perPathStiffness = stiffnesses.max(),
            totalStiffness = stiffnesses.sum(),
            alongHelixSnapDeparture = snapDeparture,
            nominalOverStroke = nominal,
            worstSingleRemovalOverStroke =
                worstSinglePathRemoval(surrogate, stiffnesses) / tile.freeStroke,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            uncoupledDishingOverStroke = tile.uncoupledDishing,
            flatAtNominal = nominal < T245_TOLERANCE,
            flatAtP90 = summary.flatAtP90,
            beatsUncoupledAtP90 = summary.p90 < tile.uncoupledDishing
        ),
        sample
    )
}

private fun t245Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T245_RIM_BAND || abs(y) > edgeY / 2.0 - T245_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** One graded configuration: a length pair, a column count, a placement and a fraction. */
private class T245Config(
    val senseOne: Int,
    val senseTwo: Int,
    val closes: Boolean,
    val crossoverColumns: Int,
    val columnReading: String,
    val placement: String,
    val compositeFraction: Double,
    val ladderPhase: Int = 0,
    val ladderOffset: Int = 14
) {
    val key: String get() = senseOne.toString() + "/" + senseTwo + "|" + crossoverColumns + "|" +
            placement + "|" + compositeFraction + "|" + ladderPhase + "|" + ladderOffset
}

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val rowPitch = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch = HoneycombCrossSectionGeometry.columnPitch(d)
    val ladderPitch = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP * rise / 2.0
    val profile = t245Profile(ResultInputs.T_3B.file())
    val t244 = ResultInputs.T_244.file()
    val t218 = ResultInputs.T_218.file()
    val t235 = ResultInputs.T_235.file()

    // ============================================== Deliverable 1 -- the cheap bound, exhaustive
    println("T-245 - the cheap bound, before any Monte Carlo")
    val designs = listOf("10 x 6" to (10 to 6), "15 x 4" to (15 to 4))
    val residueClasses = ArrayList<T245ResidueClass>()
    designs.forEach { (name, shape) ->
        closingResiduePairs(shape.first, shape.second).forEach { (one, two) ->
            val difference = Math.floorMod(one - two, 21)
            residueClasses += T245ResidueClass(
                crossSection = name,
                senseOneResidue = one,
                senseTwoResidue = two,
                differenceModulo21 = difference,
                leastAbsoluteStagger = minOf(difference, 21 - difference)
            )
        }
    }
    val closingOn106 = closingResiduePairs(10, 6)
    val closingOn154 = closingResiduePairs(15, 4)
    val closureIsCrossSectionFree = closingOn106.toSet() == closingOn154.toSet()
    val minimumStagger = minimumClosingStaggerBasePairs(10, 6)
    println("  closing residue pairs (10 x 6): " + closingOn106 +
            "; identical on 15 x 4: " + closureIsCrossSectionFree)
    println("  every closing pair has L1 - L2 = " +
            residueClasses.map { it.differenceModulo21 }.distinct() +
            " (mod 21), so the MINIMUM closing stagger is " + minimumStagger + " bp = " +
            (minimumStagger * rise).t245Rounded(3) + " nm")
    println("  C-0140's filter admits a stagger of at most 4 bp, which the closing family " +
            "never reaches: the filter and closure are DISJOINT")

    // the convention sweep, which is also C-0148's reproduction
    val shortlist = listOf(
        112 to 108, 101 to 109, 102 to 109, 112 to 109, 122 to 119, 101 to 108, 112 to 119,
        102 to 88
    )
    val closureSweep = ArrayList<T245ClosureCell>()
    designs.forEach { (name, shape) ->
        shortlist.forEach { (a, b) ->
            listOf(1, -1).forEach { sign ->
                listOf(false, true).forEach { mirrored ->
                    listOf(false, true).forEach { reversed ->
                        val residues = HoneycombRasterResidues(
                            shape.first, shape.second, a, b, sign, mirrored, reversed
                        )
                        closureSweep += T245ClosureCell(
                            crossSection = name,
                            senseOneBasePairs = a,
                            senseTwoBasePairs = b,
                            firstAxialSign = sign,
                            mirrored = mirrored,
                            axialReversed = reversed,
                            rasterCrossovers = residues.rasterCrossovers,
                            distinctReducedResidues = residues.distinctReducedResidues,
                            closes = residues.closes,
                            offRuleCrossovers = residues.offRuleCrossovers
                        )
                    }
                }
            }
        }
    }
    val conventionFree = closureSweep.groupBy {
        it.crossSection + "|" + it.senseOneBasePairs + "/" + it.senseTwoBasePairs
    }.all { (_, cells) -> cells.map { it.closes }.toSet().size == 1 }
    println("  the closure verdict is convention-free over 8 readings at " +
            shortlist.size + " pairs and both cross-sections: " + conventionFree)

    // ------------------------------------------------------------------ the closing FAMILY
    val margins = listOf(
        CrossoverLayout.EDGE_MARGIN to "the standing numerical guard",
        0.5 * rise to "half a base-pair rise",
        rise to "one base-pair rise"
    )
    fun member(name: String, rows: Int, perRow: Int, a: Int, b: Int): T245FamilyMember {
        val p = honeycombRasterProfile(rows, perRow, a, b)
        val clearance = p.frontFaceRaggednessNm - BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET
        return T245FamilyMember(
            crossSection = name,
            senseOneBasePairs = a,
            senseTwoBasePairs = b,
            closes = p.closes,
            offRuleCrossovers = p.offRuleCrossovers,
            staggerBasePairs = p.staggerBasePairs,
            rowSpanBasePairs = p.rowSpanBasePairs,
            blockExtentBasePairs = p.blockExtentBasePairs,
            blockExtentNm = p.blockExtentNm,
            extentDeparturePercent =
                100.0 * (p.blockExtentNm - T245_NOMINAL_WIDTH) / T245_NOMINAL_WIDTH,
            interfaceWindowBasePairs = p.interfaceWindowBasePairs,
            interfaceWindowNm = p.interfaceWindowNm,
            rowDerivedColumns =
                crossoverColumnsIn(p.interfaceWindowNm, ladderPitch, CrossoverLayout.EDGE_MARGIN),
            rowSpanColumns =
                crossoverColumnsIn(p.rowSpanNm, ladderPitch, CrossoverLayout.EDGE_MARGIN),
            boundingBoxColumns =
                crossoverColumnsIn(p.blockExtentNm, ladderPitch, CrossoverLayout.EDGE_MARGIN),
            guardIsInertOnTheInterface =
                guardIsInert(p.interfaceWindowNm, ladderPitch, margins.map { it.first }),
            scaffoldNucleotides = p.scaffoldNucleotides,
            scaffoldSpareOnM13 = p.scaffoldSpareOnM13,
            fitsM13 = p.fitsM13,
            unpairedNucleotidesPerHelixOnM13 = p.unpairedNucleotidesPerHelixOnM13,
            frontFaceRaggednessBasePairs = p.frontFaceRaggednessBasePairs,
            frontFaceRaggednessNm = p.frontFaceRaggednessNm,
            rearFaceRaggednessBasePairs = p.rearFaceRaggednessBasePairs,
            stackingClearanceNm = clearance,
            stackingClearanceRises = clearance / rise,
            classZeroResidue = p.classZeroResidue,
            ladderPhaseBasePairs = p.ladderPhaseBasePairs,
            interRowOffsetBasePairs = p.interRowOffsetBasePairs,
            stationsOnFace = p.stationsOnFace,
            sparsestRowStations = p.sparsestRowStations,
            stationsAtSaturation = p.stationsAtSaturation
        )
    }

    val familyPairs = closingLengthPairs(
        10, 6, T245_MIN_LENGTH, T245_MAX_LENGTH, T245_MAX_STAGGER
    )
    val family = ArrayList<T245FamilyMember>()
    familyPairs.forEach { (a, b) -> family += member("10 x 6", 10, 6, a, b) }
    familyPairs.forEach { (a, b) -> family += member("15 x 4", 15, 4, a, b) }
    // C-0140's four non-closing candidates, as the control, at both cross-sections
    listOf(112 to 108, 101 to 109, 112 to 109, 122 to 119).forEach { (a, b) ->
        family += member("10 x 6", 10, 6, a, b)
        family += member("15 x 4", 15, 4, a, b)
    }

    // The selection rule, stated once and applied at both cross-sections: among the pairs that
    // CLOSE and fit M13, take the best |extent - 40 nm|; break the tie on crossover columns, then
    // on the station census, then on the stagger. Every axis is emitted for every member, so this
    // is a selection and not an assertion.
    fun select(crossSection: String): T245FamilyMember {
        val admissible = family.filter {
            it.crossSection == crossSection && it.closes && it.fitsM13
        }
        val best = admissible.minOf { abs(it.extentDeparturePercent) }
        return admissible
            .filter { abs(it.extentDeparturePercent) <= best + 1e-9 }
            .sortedWith(compareByDescending<T245FamilyMember> { it.rowDerivedColumns }
                .thenByDescending { it.stationsOnFace ?: 0 }
                .thenBy { it.staggerBasePairs })
            .first()
    }
    val affordable = family.filter {
        it.crossSection == "10 x 6" && it.closes && it.fitsM13
    }
    val bestExtent = affordable.minOf { abs(it.extentDeparturePercent) }
    val atBestExtent = affordable.filter { abs(it.extentDeparturePercent) <= bestExtent + 1e-9 }
    val recommendation = select("10 x 6")
    val recommendationOn154 = select("15 x 4")
    val selectionIsCrossSectionFree =
        recommendationOn154.senseOneBasePairs == recommendation.senseOneBasePairs &&
                recommendationOn154.senseTwoBasePairs == recommendation.senseTwoBasePairs
    println("  the same rule on 15 x 4 returns " + recommendationOn154.senseOneBasePairs + " / " +
            recommendationOn154.senseTwoBasePairs + " bp, " +
            recommendationOn154.stationsOnFace + " of 90 stations: cross-section-free = " +
            selectionIsCrossSectionFree)
    println("  the closing family inside M13 on 10 x 6: " + affordable.size + " pairs; " +
            "best |extent - 40 nm| is " + bestExtent.t245Rounded(3) + " %, reached by " +
            atBestExtent.size + " of them")
    println("  RECOMMENDED: " + recommendation.senseOneBasePairs + " / " +
            recommendation.senseTwoBasePairs + " bp - extent " +
            recommendation.blockExtentBasePairs + " bp = " +
            recommendation.blockExtentNm.t245Rounded(4) + " nm, interface window " +
            recommendation.interfaceWindowBasePairs + " bp, " +
            recommendation.rowDerivedColumns + " crossover columns, " +
            recommendation.stationsOnFace + " stations, phase " +
            recommendation.ladderPhaseBasePairs + " / offset " +
            recommendation.interRowOffsetBasePairs)

    // CH-0189 asks whether some CLOSING pair's determined phase saturates the station census,
    // which would bring back the six-column placement CH-0184 reported and CH-0189 withdrew.
    // One does, and it is unique inside M13 -- so the answer is a PRICE rather than a negative.
    val saturating = affordable.filter {
        it.stationsOnFace == it.stationsAtSaturation && (it.sparsestRowStations ?: 0) >= 6
    }.sortedBy { abs(it.extentDeparturePercent) }
    println("  closing pairs inside M13 whose DETERMINED phase saturates the 10 x 6 census: " +
            saturating.size + (if (saturating.isEmpty()) "" else
        " -- " + saturating.first().senseOneBasePairs + " / " +
                saturating.first().senseTwoBasePairs + " bp at " +
                saturating.first().extentDeparturePercent.t245Rounded(4) + " % of SS3's 40.0 nm"))

    val recommendedOne = recommendation.senseOneBasePairs
    val recommendedTwo = recommendation.senseTwoBasePairs
    val baseline = family.first {
        it.crossSection == "10 x 6" && it.senseOneBasePairs == T245_BASELINE_ONE &&
                it.senseTwoBasePairs == T245_BASELINE_TWO
    }

    // ------------------------------------------------------------------ CH-0187's four axes
    fun axis(
        name: String, units: String, betterIs: String,
        mine: Double, theirs: Double, note: String
    ): T245Axis {
        val winner = when {
            abs(mine - theirs) < 1e-12 -> "TIE"
            (betterIs == "smaller") == (mine < theirs) -> "$recommendedOne / $recommendedTwo"
            else -> "$T245_BASELINE_ONE / $T245_BASELINE_TWO"
        }
        return T245Axis(
            axis = name, units = units, betterIs = betterIs,
            recommended = mine.t245Rounded(6).toString(),
            baseline = theirs.t245Rounded(6).toString(),
            winner = winner, note = note
        )
    }
    val axes = listOf(
        T245Axis(
            axis = "scaffold closure on caDNAno's default +-5 bp rule",
            units = "raster crossovers that must be FORCED, of 59",
            betterIs = "smaller",
            recommended = recommendation.offRuleCrossovers.toString(),
            baseline = baseline.offRuleCrossovers.toString(),
            winner = "$recommendedOne / $recommendedTwo",
            note = "a RULE, not a preference; the axis CH-0187 did not have and C-0148 supplied"
        ),
        axis(
            "axial extent against SS3's nominal 40.0 nm", "per cent", "smaller",
            abs(recommendation.extentDeparturePercent), abs(baseline.extentDeparturePercent),
            "the only axis C-0140's own rule scored, and closure costs NOTHING on it"
        ),
        axis(
            "scaffold on M13's 7 249 nt", "nucleotides spent", "smaller",
            recommendation.scaffoldNucleotides.toDouble(),
            baseline.scaffoldNucleotides.toDouble(),
            "route A, an antiparallel crossover at every turn; both fit"
        ),
        axis(
            "front-face relief against the blunt-end stacking onset", "base-pair rises", "larger",
            recommendation.stackingClearanceRises, baseline.stackingClearanceRises,
            "CLAUDE.md: a margin below one rise is not quotable, and the baseline's is 0.18"
        ),
        axis(
            "front-face relief against C-0141's SATURATED outboard plan ceiling", "nm", "smaller",
            recommendation.frontFaceRaggednessNm, baseline.frontFaceRaggednessNm,
            "the one axis CH-0187 gives 112 / 108 -- and saturation is unreachable at a " +
                    "determined phase, which carries 55 of 60 stations"
        ),
        T245Axis(
            axis = "row-derived crossover columns", units = "columns",
            betterIs = "larger",
            recommended = recommendation.rowDerivedColumns.toString(),
            baseline = baseline.rowDerivedColumns.toString(),
            winner = "$T245_BASELINE_ONE / $T245_BASELINE_TWO",
            note = "the whole cost of closure on the flatness axis, and it is ONE column"
        ),
        T245Axis(
            axis = "stations on the face at the DETERMINED phase", units = "of 60",
            betterIs = "larger",
            recommended = recommendation.stationsOnFace.toString(),
            baseline = "none - no phase is determined where no b0 serves the raster",
            winner = "$recommendedOne / $recommendedTwo",
            note = "CH-0189: a 21-phase sweep on a non-closing raster is a sweep over designs " +
                    "that need forced crossovers"
        )
    )
    axes.forEach {
        println("  axis: " + it.axis + " -> " + it.recommended + " against " + it.baseline +
                ", winner " + it.winner)
    }

    // ------------------------------------------------------------------ the column readings
    val columnCounts = ArrayList<T245ColumnCount>()
    listOf(recommendedOne to recommendedTwo, T245_BASELINE_ONE to T245_BASELINE_TWO)
        .forEach { (a, b) ->
            val p = honeycombRasterProfile(10, 6, a, b)
            listOf(
                Triple("interface (two adjacent row spans intersected)",
                    p.interfaceWindowBasePairs, p.interfaceWindowNm),
                Triple("x-raster row span", p.rowSpanBasePairs, p.rowSpanNm),
                Triple("bounding box", p.blockExtentBasePairs, p.blockExtentNm)
            ).forEach { (reading, bp, nm) ->
                margins.forEach { (margin, convention) ->
                    val count = crossoverColumnsIn(nm, ladderPitch, margin)
                    columnCounts += T245ColumnCount(
                        senseOneBasePairs = a, senseTwoBasePairs = b,
                        windowReading = reading, windowBasePairs = bp, windowNm = nm,
                        edgeMargin = margin, edgeMarginConvention = convention,
                        crossoverColumns = count,
                        slackBeyondLastPitch = columnSlack(nm, ladderPitch, margin)
                    )
                }
            }
        }
    columnCounts.filter { it.senseOneBasePairs == recommendedOne }.forEach {
        println("  columns at " + it.windowReading + " (" + it.windowBasePairs + " bp), guard " +
                it.edgeMargin.t245Rounded(3) + " nm: " + it.crossoverColumns +
                " (slack " + it.slackBeyondLastPitch.t245Rounded(3) + " nm)")
    }

    // ------------------------------------------------------------------ the plan ceilings
    val determinedPhase = recommendation.ladderPhaseBasePairs!!
    val determinedOffset = recommendation.interRowOffsetBasePairs
    val recommendedRaster = twoLengthRaster(10, 6, recommendedOne, recommendedTwo)
    val determinedLattice = recommendedRaster.stationLattice(determinedPhase, determinedOffset)
    val baselineRaster = twoLengthRaster(10, 6, T245_BASELINE_ONE, T245_BASELINE_TWO)
    val baselineLattice = baselineRaster.stationLattice(0, 7)
    val planCeilings = ArrayList<T245PlanCeiling>()
    listOf(
        Triple("$recommendedOne / $recommendedTwo, DETERMINED phase $determinedPhase / " +
                "offset $determinedOffset", determinedLattice,
            recommendedOne to recommendedTwo),
        Triple("$T245_BASELINE_ONE / $T245_BASELINE_TWO, C-0142's swept phase 0 / offset 7",
            baselineLattice, T245_BASELINE_ONE to T245_BASELINE_TWO)
    ).forEach { (label, lattice, pair) ->
        val stations = lattice.sumOf { it.size }
        listOf(10, 20, 30, 50, 55).filter { it <= stations }.forEach { demand ->
            val ceiling = maximumPlanCeilingForCount(
                lattice, demand, recommendedRaster.blockExtent(), d, lattice.maxOf { it.size }
            )
            planCeilings += T245PlanCeiling(
                senseOneBasePairs = pair.first, senseTwoBasePairs = pair.second,
                latticeDescription = label, stationsOnFace = stations, demandedPaths = demand,
                maximumPlanCeiling = ceiling,
                affordsRecommendedElement = ceiling?.let { it >= T245_ELEMENT_LENGTH }
            )
        }
    }
    planCeilings.forEach {
        println("  plan ceiling, " + it.latticeDescription + ", " + it.demandedPaths +
                " paths: " + it.maximumPlanCeiling?.t245Rounded(6) +
                " nm, affords C-0069's element: " + it.affordsRecommendedElement)
    }

    // ================================================= Deliverable 2 -- the re-graded cells
    val edgeY = HoneycombBlock(10, 6, d).plateEdgeY
    val recommendedColumns = recommendation.rowDerivedColumns
    val baselineColumns = baseline.rowDerivedColumns
    val boxColumns = baseline.boundingBoxColumns

    val configs = listOf(
        T245Config(recommendedOne, recommendedTwo, true, recommendedColumns,
            "interface (row-derived)", "abstract grid", T245_BAND_MEASURED),
        T245Config(recommendedOne, recommendedTwo, true, recommendedColumns,
            "interface (row-derived)", "abstract grid", T245_BAND_LOW),
        T245Config(recommendedOne, recommendedTwo, true, recommendedColumns,
            "interface (row-derived)", "determined station lattice", T245_BAND_MEASURED,
            determinedPhase, determinedOffset),
        T245Config(recommendedOne, recommendedTwo, true, baselineColumns,
            "C-0148's row-derived count at the non-closing pair", "abstract grid",
            T245_BAND_MEASURED),
        T245Config(T245_BASELINE_ONE, T245_BASELINE_TWO, false, baselineColumns,
            "interface (row-derived)", "abstract grid", T245_BAND_MEASURED),
        T245Config(T245_BASELINE_ONE, T245_BASELINE_TWO, false, baselineColumns,
            "interface (row-derived)", "abstract grid", T245_BAND_LOW),
        T245Config(T245_BASELINE_ONE, T245_BASELINE_TWO, false, boxColumns,
            "bounding box", "abstract grid", T245_BAND_MEASURED),
        T245Config(T245_BASELINE_ONE, T245_BASELINE_TWO, false, boxColumns,
            "bounding box", "two-length station lattice", T245_BAND_MEASURED, 0, 7)
    )

    fun tileOf(config: T245Config): T245Tile = T245Tile(
        label = config.senseOne.toString() + " / " + config.senseTwo + " bp",
        rasterRows = 10,
        layers = 6,
        edgeX = honeycombRasterProfile(10, 6, config.senseOne, config.senseTwo).blockExtentNm,
        edgeY = edgeY,
        inPlanePitch = rowPitch,
        layerSpacing = columnPitch,
        compositeFraction = config.compositeFraction,
        crossoverColumns = config.crossoverColumns,
        profile = profile
    )

    val geometries = ArrayList<T245Geometry>()
    val references = ArrayList<T245Reference>()
    val seen = HashSet<String>()
    configs.forEach { config ->
        val tile = tileOf(config)
        val id = config.senseOne.toString() + "/" + config.senseTwo + "|" +
                config.crossoverColumns + "|" + config.compositeFraction
        if (seen.add(id)) {
            geometries += T245Geometry(
                label = tile.label,
                senseOneBasePairs = config.senseOne,
                senseTwoBasePairs = config.senseTwo,
                widthReading = "block extent " +
                        honeycombRasterProfile(10, 6, config.senseOne, config.senseTwo)
                            .blockExtentBasePairs + " bp",
                rasterRows = 10, layers = 6,
                compositeFraction = config.compositeFraction,
                edgeX = tile.edgeX, edgeY = tile.edgeY,
                crossoverColumns = tile.crossoverColumns,
                columnReading = config.columnReading,
                interiorPressure = tile.interiorPressure,
                freeStroke = tile.freeStroke,
                reachAlong = tile.reachAlong,
                reachAcross = tile.reachAcross
            )
            references += T245Reference(
                label = tile.label,
                senseOneBasePairs = config.senseOne,
                senseTwoBasePairs = config.senseTwo,
                crossoverColumns = tile.crossoverColumns,
                compositeFraction = config.compositeFraction,
                uncoupledDishingOverStroke = tile.uncoupledDishing,
                flat = tile.uncoupledDishing < T245_TOLERANCE
            )
        }
    }
    references.forEach {
        println("  uncoupled " + it.label + "  " + it.crossoverColumns + " col  f = " +
                it.compositeFraction.t245Rounded(3) + "  dishing " +
                it.uncoupledDishingOverStroke.t245Rounded(9) +
                (if (it.flat) "  flat" else "  NOT FLAT"))
    }

    val gradedColumns = listOf(1, 2, 3, 5)
    val cells = ArrayList<T245Cell>()
    val samples = HashMap<String, DoubleArray>()
    val refusals = ArrayList<String>()
    configs.forEach { config ->
        val tile = tileOf(config)
        val raster = twoLengthRaster(10, 6, config.senseOne, config.senseTwo)
        gradedColumns.forEach { columns ->
            val abstractGrid = attachmentGrid(columns, 10, tile.edgeX, tile.edgeY)
            val grid = try {
                if (config.placement == "abstract grid") abstractGrid
                else twoLengthSnappedGrid(
                    raster, columns, tile.edgeY, config.ladderPhase, config.ladderOffset
                )
            } catch (e: IllegalArgumentException) {
                refusals += config.key + "|" + columns + " columns: " + e.message
                null
            } ?: return@forEach
            val snapDeparture = alongHelixDeparture(abstractGrid, grid)
            t245Distributions(grid, tile.edgeX, tile.edgeY).forEach { (label, stiffnesses) ->
                val graded = gradeT245Cell(
                    tile, config, columns, grid, snapDeparture, label, stiffnesses,
                    t245Realisations
                )
                cells += graded.cell
                samples[config.key + "|" + columns + "|" + label] = graded.sample
                println("  " + config.senseOne + "/" + config.senseTwo + "  " +
                        tile.crossoverColumns + " col  " + config.placement + "  f=" +
                        config.compositeFraction.t245Rounded(3) + "  " + columns + " x 10 = " +
                        grid.size + " paths, " + label + "  p90 " +
                        graded.cell.p90OverStroke.t245Rounded(9) +
                        (if (graded.cell.flatAtP90) "  FLAT at p90" else "  not flat at p90"))
            }
        }
    }
    if (refusals.isNotEmpty()) refusals.forEach { println("  REFUSED: " + it) }

    // ------------------------------------------------------------------ the paired readings
    val paired = ArrayList<T245Paired>()
    fun pair(comparison: String, numerator: T245Config, denominator: T245Config) {
        gradedColumns.forEach { columns ->
            listOf("equal springs", "rim-graded 5:1").forEach { label ->
                val a = samples[numerator.key + "|" + columns + "|" + label]
                val b = samples[denominator.key + "|" + columns + "|" + label]
                if (a == null || b == null) return@forEach
                val summary = pairedRatioSummary(a, b)
                fun cellOf(config: T245Config) = cells.first {
                    it.senseOneBasePairs == config.senseOne &&
                            it.senseTwoBasePairs == config.senseTwo &&
                            it.crossoverColumns == config.crossoverColumns &&
                            it.placement == config.placement &&
                            it.compositeFraction == config.compositeFraction &&
                            it.ladderPhase == config.ladderPhase &&
                            it.columns == columns && it.distribution == label
                }
                paired += T245Paired(
                    comparison = comparison,
                    columns = columns,
                    pathCount = cellOf(numerator).pathCount,
                    distribution = label,
                    numeratorP90 = cellOf(numerator).p90OverStroke,
                    denominatorP90 = cellOf(denominator).p90OverStroke,
                    ratioOfPercentiles = summary.ratioOfPercentiles,
                    medianOfRatios = summary.median,
                    p90OfRatios = summary.p90,
                    bestRatio = summary.best,
                    worstRatio = summary.worst,
                    fractionAbove = summary.fractionAbove,
                    signsAgree = (summary.median > 1.0) == (summary.ratioOfPercentiles > 1.0)
                )
            }
        }
    }
    pair("what CLOSURE costs: the recommended pair on its own row-derived columns over " +
            "C-0140's pair on its", configs[0], configs[4])
    pair("what the DETERMINED station lattice costs against the abstract grid, at the " +
            "recommendation", configs[2], configs[0])
    pair("the bounding-box column reading over the recommended row-derived one",
        configs[6], configs[0])

    // ------------------------------------------------------------------ the convergence axis
    val convergenceConfig = configs[0]
    val convergenceTile = tileOf(convergenceConfig)
    val convergence = ArrayList<T245Convergence>()
    run {
        val grid = attachmentGrid(1, 10, convergenceTile.edgeX, convergenceTile.edgeY)
        val stiffnesses = equalShareOfMandate(grid.size)
        var previous: Double? = null
        listOf(1000, 2000, t245Realisations).forEach { count ->
            val graded = gradeT245Cell(
                convergenceTile, convergenceConfig, 1, grid, 0.0, "equal springs",
                stiffnesses, count
            )
            val value = graded.cell.p90OverStroke
            convergence += T245Convergence(
                axis = "dropout realisations, one common stream restricted per cell",
                setting = count.toString() + " realisations",
                quantity = "p90 dishing over the free stroke, recommended cell",
                value = value,
                departure = previous?.let { abs(value - it) / it }
            )
            previous = value
        }
    }
    margins.forEach { (margin, convention) ->
        convergence += T245Convergence(
            axis = "EDGE_MARGIN, a numerical GUARD carried on the convergence axis",
            setting = convention + " (" + margin.t245Rounded(3) + " nm)",
            quantity = "crossover columns from the recommended pair's interface window",
            value = crossoverColumnsIn(
                recommendation.interfaceWindowNm, ladderPitch, margin
            ).toDouble(),
            departure = null
        )
    }

    // ------------------------------------------------------------------ the reproductions
    val reproductions = ArrayList<T245Reproduction>()
    fun reproduce(source: String, quantity: String, published: Double, here: Double) {
        reproductions += T245Reproduction(
            source = source, quantity = quantity, published = published, here = here,
            departure = if (published == 0.0) abs(here) else abs(here - published) / abs(published)
        )
    }
    run {
        val determinedRecord = Json.parseToJsonElement(t244.readText())
            .jsonObject.getValue("determined").jsonArray.map { it.jsonObject }
            .first { it.getValue("crossSection").jsonPrimitive.content == "10 x 6" }
        fun field(name: String) = determinedRecord.getValue(name).jsonPrimitive.content.toDouble()
        reproduce("C-0148 / T-244", "the determined class-zero residue b0", field("classZeroResidue"),
            recommendation.classZeroResidue!!.toDouble())
        reproduce("C-0148 / T-244", "the determined ladder phase",
            field("ladderPhaseBasePairs"), determinedPhase.toDouble())
        reproduce("C-0148 / T-244", "the inter-row ladder offset",
            field("interRowOffsetBasePairs"), determinedOffset.toDouble())
        reproduce("C-0148 / T-244", "stations on the face at the determined phase",
            field("stationsOnFace"), recommendation.stationsOnFace!!.toDouble())
        val closureRecord = Json.parseToJsonElement(t244.readText())
            .jsonObject.getValue("closure").jsonArray.map { it.jsonObject }
            .first {
                it.getValue("crossSection").jsonPrimitive.content == "10 x 6" &&
                        it.getValue("senseOneBasePairs").jsonPrimitive.content == "112" &&
                        it.getValue("senseTwoBasePairs").jsonPrimitive.content == "108" &&
                        it.getValue("firstAxialSign").jsonPrimitive.content == "1" &&
                        it.getValue("mirrored").jsonPrimitive.content == "false" &&
                        it.getValue("axialReversed").jsonPrimitive.content == "false"
            }
        reproduce("C-0148 / T-244", "forced raster crossovers at 112 / 108 on 10 x 6",
            closureRecord.getValue("offRuleCrossovers").jsonPrimitive.content.toDouble(),
            baseline.offRuleCrossovers.toDouble())
    }
    run {
        val widths = Json.parseToJsonElement(t218.readText())
            .jsonObject.getValue("widths").jsonArray.map { it.jsonObject }
        fun width(a: Int, b: Int, key: String) = widths.first {
            it.getValue("senseOneRowLength").jsonPrimitive.content.toInt() == a &&
                    it.getValue("senseTwoRowLength").jsonPrimitive.content.toInt() == b
        }.getValue(key).jsonPrimitive.content.toDouble()
        val on154 = honeycombRasterProfile(15, 4, recommendedOne, recommendedTwo)
        reproduce("C-0140 / T-218", "the block extent at the recommended pair",
            width(recommendedOne, recommendedTwo, "axialExtentBasePairs"),
            recommendation.blockExtentBasePairs.toDouble())
        reproduce("C-0140 / T-218", "the scaffold at the recommended pair on 15 x 4",
            width(recommendedOne, recommendedTwo, "scaffoldNucleotides"),
            on154.scaffoldNucleotides.toDouble())
        reproduce("C-0140 / T-218", "the front-face raggedness at the recommended pair",
            width(recommendedOne, recommendedTwo, "frontFaceRaggednessBasePairs"),
            on154.frontFaceRaggednessBasePairs.toDouble())
        reproduce("C-0140 / T-218", "the rear-face raggedness at the recommended pair",
            width(recommendedOne, recommendedTwo, "rearFaceRaggednessBasePairs"),
            on154.rearFaceRaggednessBasePairs.toDouble())
        reproduce("C-0140 / T-218", "the scaffold at 112 / 108 on 15 x 4",
            width(T245_BASELINE_ONE, T245_BASELINE_TWO, "scaffoldNucleotides"),
            honeycombRasterProfile(15, 4, T245_BASELINE_ONE, T245_BASELINE_TWO)
                .scaffoldNucleotides.toDouble())
    }
    run {
        val root = Json.parseToJsonElement(t235.readText()).jsonObject
        val published = root.getValue("cells").jsonArray.map { it.jsonObject }
        val publishedReferences = root.getValue("references").jsonArray.map { it.jsonObject }
        fun match(margin: Double, fraction: Double, placement: String,
                  columns: Int, distribution: String) = published.first {
            it.getValue("crossSection").jsonPrimitive.content == "10 x 6" &&
                    it.getValue("widthReading").jsonPrimitive.content == "block extent 116 bp" &&
                    it.getValue("edgeMargin").jsonPrimitive.content.toDouble() == margin &&
                    it.getValue("compositeFraction").jsonPrimitive.content.toDouble() == fraction &&
                    it.getValue("placement").jsonPrimitive.content == placement &&
                    it.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                    it.getValue("distribution").jsonPrimitive.content == distribution
        }.getValue("p90OverStroke").jsonPrimitive.content.toDouble()
        listOf(
            Triple(0.17, T245_BAND_MEASURED, configs[4]),
            Triple(0.17, T245_BAND_LOW, configs[5]),
            Triple(CrossoverLayout.EDGE_MARGIN, T245_BAND_MEASURED, configs[6])
        ).forEach { (margin, fraction, config) ->
            gradedColumns.forEach { columns ->
                listOf("equal springs", "rim-graded 5:1").forEach { distribution ->
                    val here = cells.firstOrNull {
                        it.senseOneBasePairs == config.senseOne &&
                                it.crossoverColumns == config.crossoverColumns &&
                                it.compositeFraction == config.compositeFraction &&
                                it.placement == "abstract grid" && it.columns == columns &&
                                it.distribution == distribution
                    } ?: return@forEach
                    reproduce(
                        "C-0146 / T-235",
                        "p90 at " + config.crossoverColumns + " columns, f = " + fraction +
                                ", " + columns + " x 10, " + distribution,
                        match(margin, fraction, "abstract grid", columns, distribution),
                        here.p90OverStroke
                    )
                }
            }
        }
        gradedColumns.forEach { columns ->
            listOf("equal springs", "rim-graded 5:1").forEach { distribution ->
                val here = cells.firstOrNull {
                    it.senseOneBasePairs == T245_BASELINE_ONE &&
                            it.placement == "two-length station lattice" &&
                            it.columns == columns && it.distribution == distribution
                } ?: return@forEach
                reproduce(
                    "C-0146 / T-235",
                    "p90 on the two-length lattice at phase 0 / offset 7, " + columns +
                            " x 10, " + distribution,
                    match(CrossoverLayout.EDGE_MARGIN, T245_BAND_MEASURED,
                        "two-length station lattice", columns, distribution),
                    here.p90OverStroke
                )
            }
        }
        fun reference(margin: Double, fraction: Double) = publishedReferences.first {
            it.getValue("crossSection").jsonPrimitive.content == "10 x 6" &&
                    it.getValue("widthReading").jsonPrimitive.content == "block extent 116 bp" &&
                    it.getValue("edgeMargin").jsonPrimitive.content.toDouble() == margin &&
                    it.getValue("compositeFraction").jsonPrimitive.content.toDouble() == fraction
        }.getValue("uncoupledDishingOverStroke").jsonPrimitive.content.toDouble()
        listOf(
            Triple(0.17, T245_BAND_MEASURED, baselineColumns),
            Triple(0.17, T245_BAND_LOW, baselineColumns),
            Triple(CrossoverLayout.EDGE_MARGIN, T245_BAND_MEASURED, boxColumns)
        ).forEach { (margin, fraction, columns) ->
            val here = references.firstOrNull {
                it.senseOneBasePairs == T245_BASELINE_ONE && it.crossoverColumns == columns &&
                        it.compositeFraction == fraction
            } ?: return@forEach
            reproduce("C-0146 / T-235",
                "the uncoupled tile at " + columns + " columns, f = " + fraction,
                reference(margin, fraction), here.uncoupledDishingOverStroke)
        }
    }
    val uniformRouteBSlack = (HONEYCOMB_M13_NUCLEOTIDES - 60 * 112) / 60
    reproduce("C-0147 / T-230", "M13's route-B turn slack at a UNIFORM 112 bp row", 8.0,
        uniformRouteBSlack.toDouble())
    println("  reproductions: " + reproductions.size + ", worst departure " +
            reproductions.maxOf { it.departure }.t245Rounded(2, 0.0))

    // ------------------------------------------------------------------ the verdict sets
    fun cellSet(config: T245Config) = cells.filter {
        it.senseOneBasePairs == config.senseOne && it.senseTwoBasePairs == config.senseTwo &&
                it.crossoverColumns == config.crossoverColumns &&
                it.placement == config.placement &&
                it.compositeFraction == config.compositeFraction &&
                it.ladderPhase == config.ladderPhase
    }
    val recommendedCells = cellSet(configs[0])
    val recommendedLowCells = cellSet(configs[1])
    val latticeCells = cellSet(configs[2])
    val baselineCells = cellSet(configs[4])
    val baselineBoxCells = cellSet(configs[6])
    fun headline(set: List<T245Cell>) = set.firstOrNull {
        it.columns == 1 && it.distribution == "equal springs"
    }
    val headlineMeasured = headline(recommendedCells)!!
    val headlineLow = headline(recommendedLowCells)!!
    val headlineLattice = headline(latticeCells)

    // The identity: at the block width the two pairs give the SAME tile, so at the SAME column
    // count every cell must agree. That is what makes "closure costs one crossover column" a
    // measurement rather than an inference.
    //
    // It is asserted as a THRESHOLD and not emitted as a value. CLAUDE.md: "Bit-identical is not
    // assertable on a SOLVED FIELD of this lattice" -- two identically constructed grillages in
    // one JVM differ by a few ulp because the JIT recompiles a hot reduction between the two
    // calls -- and a field carrying that difference moves between runs (4.2e-17 against 1.4e-17
    // here) while every other field of this file is byte-identical. A number that is nothing but
    // ulp noise makes the whole file un-diffable, which is the check the rounding layer exists
    // to enable.
    val identityCells = cellSet(configs[3])
    val identityDeparture = identityCells.maxOfOrNull { mine ->
        val theirs = baselineCells.first {
            it.columns == mine.columns && it.distribution == mine.distribution
        }
        abs(mine.p90OverStroke - theirs.p90OverStroke)
    } ?: 0.0
    val identityHolds = identityDeparture < T245_SOLVED_FIELD_TOLERANCE
    println("  the same-tile identity holds to " + T245_SOLVED_FIELD_TOLERANCE + ": " +
            identityHolds)

    // ------------------------------------------------------------------ the falsifiers
    val betterExtent = affordable.filter {
        abs(it.extentDeparturePercent) < abs(recommendation.extentDeparturePercent) - 1e-9
    }
    val belowSeven = closingLengthPairs(10, 6, T245_MIN_LENGTH, T245_MAX_LENGTH, T245_MAX_STAGGER)
        .filter { (a, b) -> abs(a - b) < 7 }
    val signDisagreements = paired.filter { !it.signsAgree }
    val falsifiers = listOf(
        T245Falsifier("F1",
            "some closing pair inside M13 beats the recommended one on the axial-extent axis",
            false, betterExtent.isNotEmpty(),
            "the best |extent - 40 nm| in the closing family is " +
                    bestExtent.t245Rounded(3) + " %, reached by " + atBestExtent.size +
                    " pairs including the recommended one; " + betterExtent.size + " beat it"),
        T245Falsifier("F2",
            "the closing family admits a stagger below 7 bp, so C-0140's filter is merely " +
                    "unstated rather than disjoint from closure",
            false, belowSeven.isNotEmpty(),
            "every closing residue pair has L1 - L2 = " +
                    residueClasses.map { it.differenceModulo21 }.distinct() +
                    " (mod 21), so the least stagger is " + minimumStagger +
                    " bp; " + belowSeven.size + " pairs of " + familyPairs.size + " fall below it"),
        T245Falsifier("F3",
            "the recommended cell -- one column, ten paths, equal springs -- loses T-5b's 0.10 " +
                    "at p90 at either end of C-0116's measured band",
            true, !(headlineMeasured.flatAtP90 && headlineLow.flatAtP90),
            "p90 " + headlineMeasured.p90OverStroke.t245Rounded(9) + " at f = 0.30 and " +
                    headlineLow.p90OverStroke.t245Rounded(9) + " at f = 0.26, against 0.10"),
        T245Falsifier("F4",
            "a graded column count is refused on the DETERMINED station lattice",
            true, refusals.any { it.contains("determined") },
            if (refusals.isEmpty()) "0 of " + (configs.size * gradedColumns.size) +
                    " (configuration, column count) pairs refused"
            else refusals.joinToString("; ")),
        T245Falsifier("F5",
            "the closure verdict depends on the cross-section or on the sign, mirror or datum " +
                    "convention",
            false, !(closureIsCrossSectionFree && conventionFree),
            "identical over " + closureSweep.size + " readings and both cross-sections"),
        T245Falsifier("F6",
            "the recommended pair's row-derived crossover-column count is not the 10 CH-0188 " +
                    "predicts",
            false, recommendedColumns != 10,
            "the interface window is " + recommendation.interfaceWindowBasePairs + " bp = " +
                    recommendation.interfaceWindowNm.t245Rounded(6) + " nm and gives " +
                    recommendedColumns + " columns at all three EDGE_MARGIN conventions"),
        T245Falsifier("F7",
            "a paired median-of-ratios and its ratio-of-percentiles disagree in SIGN",
            true, signDisagreements.isNotEmpty(),
            signDisagreements.size.toString() + " of " + paired.size + " paired rows disagree"),
        T245Falsifier("F8",
            "the recommended pair does not fit M13",
            false, !recommendation.fitsM13,
            "route A spends " + recommendation.scaffoldNucleotides + " nt of M13's " +
                    HONEYCOMB_M13_NUCLEOTIDES + ", leaving " + recommendation.scaffoldSpareOnM13)
    )
    falsifiers.forEach {
        println("  " + it.id + (if (it.fired) "  FIRED  " else "  did not fire  ") + it.statement)
    }

    // ------------------------------------------------------------------ the emission
    val flatMeasured = recommendedCells.count { it.flatAtP90 }
    val flatLow = recommendedLowCells.count { it.flatAtP90 }
    val flatBaseline = baselineCells.count { it.flatAtP90 }
    val flatBox = baselineBoxCells.count { it.flatAtP90 }
    val flatLattice = latticeCells.count { it.flatAtP90 }

    val result = T245Result(
        task = "T-245",
        leaf = "A8.2",
        title = "Re-selecting the honeycomb two-length row pair on SCAFFOLD CLOSURE, and " +
                "re-grading the coupled cells at its own row-derived crossover-column count",
        verificationType = "logical (exact integer arithmetic on the crossover-residue lattice, " +
                "exhaustive over residue pairs modulo 21) + in-silico (C-0142's influence " +
                "surrogate and Monte Carlo dropout grading on one common stream)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "closure rule is caDNAno's published DEFAULT and the raster is a lattice " +
                "statement; no folded object is measured.",
        units = mapOf(
            "length" to "nm",
            "axialPosition" to "base pairs on one global z",
            "residue" to "base pairs modulo 21",
            "stiffness" to "pN/nm",
            "dishing" to "dimensionless, a fraction of the free-tile stroke",
            "scaffold" to "nucleotides"
        ),
        conventions = mapOf(
            "closure" to "every raster crossover is a SCAFFOLD crossover and sits at " +
                    "b0 + 7c +- 5 (mod 21); one b0 serves the whole design, so the reduced " +
                    "residues (level - 7 class) mod 21 must number at most two and be 10 apart",
            "extent" to "the block's own axial bounding window over its interior helices, " +
                    "which is 2 max - min for a two-length raster",
            "columns" to "a crossover column serves an INTERFACE, so its window is the " +
                    "intersection of two adjacent row spans (T-243), not the bounding box",
            "phase" to "measured from the block's own low plane, TwoLengthRaster's convention",
            "face" to "the +x face, the one pointing away from the grafted layer"
        ),
        parameters = mapOf(
            "crossSection" to "10 x 6 (design (ii)), 60 helices; 15 x 4 carried for the closure " +
                    "sweep only",
            "residuePairsSwept" to (21 * 21).toString() + " per cross-section, exhaustive",
            "lengthRange" to T245_MIN_LENGTH.toString() + " to " + T245_MAX_LENGTH + " bp",
            "maximumStagger" to T245_MAX_STAGGER.toString() + " bp",
            "risePerBasePair" to rise.toString(),
            "interhelicalDistance" to d.toString(),
            "crossoverColumnPitch" to ladderPitch.t245Rounded(9).toString(),
            "realisations" to t245Realisations.toString(),
            "smokeRun" to (t245Realisations != T245_DECLARED_REALISATIONS).toString(),
            "seed" to T245_SEED.toString(),
            "dishingSamples" to T245_SAMPLES.toString(),
            "flatnessTolerance" to T245_TOLERANCE.toString(),
            "compositeFractions" to "0.26 and 0.30, C-0116's measured band",
            "mandate" to "C-0017 at SS3's acceptable clause, 33.3333 pN/nm on the SUM",
            "bluntEndStackingOnset" to BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET.toString(),
            "recommendedElementLength" to T245_ELEMENT_LENGTH.toString(),
            "primarySource" to "Douglas et al., Nucleic Acids Res. 37:5001 (caDNAno), " +
                    "PMC2731887, read directly"
        ),
        sources = listOf("gpd/data/T-151-sources/PMC2731887-fullTextXML.xml"),
        citedInputs = listOf(
            "gpd/results/T-244-face-bond-class-residues.json",
            "gpd/results/T-218-honeycomb-raster-turn-sense.json",
            "gpd/results/T-235-coupled-cells-at-the-two-length-raster.json",
            "gpd/results/T-3b-tile-edge-load-profile.json"
        ),
        cheapBound = mapOf(
            "whatItSaid" to "Closure depends on the two row lengths only through their residues " +
                    "modulo 21, so 441 cases settle the whole family before any width, scaffold " +
                    "or solve. Three residue pairs close, they are the same on both 60-helix " +
                    "cross-sections, and every one of them has L1 - L2 = 14 (mod 21) -- so the " +
                    "MINIMUM stagger a closing raster can carry is 7 bp. C-0140's selection " +
                    "filter admits a stagger of at most 4 bp, which means the filter and the " +
                    "closing family are DISJOINT: the rule that chose 112 / 108 is exactly the " +
                    "rule that could not have chosen a drawable raster.",
            "cost" to "exact integer arithmetic; no solve, no mesh, no sampling",
            "closingResiduePairs" to closingOn106.toString(),
            "minimumClosingStaggerBasePairs" to minimumStagger.toString(),
            "C-0140sFilterBasePairs" to "4",
            "recommendation" to "$recommendedOne / $recommendedTwo bp",
            "whatItLeftForTheMonteCarlo" to "one pair, one column count, and the eight cells " +
                    "C-0146 grades -- rather than a family"
        ),
        closureSweep = closureSweep,
        closingResidueClasses = residueClasses,
        closingFamily = family,
        selectionAxes = axes,
        columnCounts = columnCounts,
        planCeilings = planCeilings,
        geometries = geometries,
        references = references,
        cells = cells,
        paired = paired,
        verdict = mapOf(
            "recommendedPair" to "$recommendedOne / $recommendedTwo bp",
            "closes" to recommendation.closes.toString(),
            "forcedCrossovers" to recommendation.offRuleCrossovers.toString(),
            "blockExtentBasePairs" to recommendation.blockExtentBasePairs.toString(),
            "blockExtentNm" to recommendation.blockExtentNm.t245Rounded(6).toString(),
            "extentDeparturePercent" to
                    recommendation.extentDeparturePercent.t245Rounded(3).toString(),
            "rowDerivedColumns" to recommendedColumns.toString(),
            "baselineRowDerivedColumns" to baselineColumns.toString(),
            "determinedLadderPhase" to determinedPhase.toString(),
            "interRowOffset" to determinedOffset.toString(),
            "stationsOnFace" to recommendation.stationsOnFace.toString(),
            "sameRuleOn15x4" to recommendationOn154.senseOneBasePairs.toString() + " / " +
                    recommendationOn154.senseTwoBasePairs + " bp, " +
                    recommendationOn154.stationsOnFace + " of 90 stations",
            "selectionIsCrossSectionFree" to selectionIsCrossSectionFree.toString(),
            "saturatingClosingPairsInsideM13" to saturating.size.toString(),
            "cheapestSaturatingPair" to (saturating.firstOrNull()?.let {
                it.senseOneBasePairs.toString() + " / " + it.senseTwoBasePairs + " bp"
            } ?: "none"),
            "cheapestSaturatingExtentDeparturePercent" to (saturating.firstOrNull()?.let {
                it.extentDeparturePercent.t245Rounded(4).toString()
            } ?: "none"),
            "flatCellsAtTheRecommendation" to flatMeasured.toString() + " of " +
                    recommendedCells.size,
            "flatCellsAtTheLowBand" to flatLow.toString() + " of " + recommendedLowCells.size,
            "flatCellsAtC0140sPairAndItsOwnCount" to flatBaseline.toString() + " of " +
                    baselineCells.size,
            "flatCellsAtTheBoundingBoxCount" to flatBox.toString() + " of " +
                    baselineBoxCells.size,
            "flatCellsOnTheDeterminedLattice" to flatLattice.toString() + " of " +
                    latticeCells.size,
            "headlineCellP90AtMeasuredBand" to
                    headlineMeasured.p90OverStroke.t245Rounded(9).toString(),
            "headlineCellP90AtLowBand" to
                    headlineLow.p90OverStroke.t245Rounded(9).toString(),
            "headlineCellSurvivesT5b" to
                    (headlineMeasured.flatAtP90 && headlineLow.flatAtP90).toString(),
            "sameTileIdentityHoldsBelow" to T245_SOLVED_FIELD_TOLERANCE.toString(),
            "sameTileIdentityHolds" to identityHolds.toString(),
            "scaffoldSpareOnM13" to recommendation.scaffoldSpareOnM13.toString(),
            "unpairedNucleotidesPerHelixAtTheRecommendedLengths" to
                    recommendation.unpairedNucleotidesPerHelixOnM13.toString(),
            "unpairedNucleotidesPerHelixAtAUniform112bpRow" to uniformRouteBSlack.toString()
        ),
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = mapOf(
            "THE_FILTER_AND_CLOSURE_ARE_DISJOINT" to
                    "Every closing residue pair has L1 - L2 = 14 (mod 21), so |L1 - L2| is 7, " +
                    "14, 28, ... and the minimum stagger a DRAWABLE two-length honeycomb raster " +
                    "can carry is 7 bp = " + (7 * rise).t245Rounded(3) + " nm. C-0140's " +
                    "minimum stagger of 3 bp is a PER-HELIX minimum (C-0136's row-length rule " +
                    "applied to one helix at a time) and no pair reaching it closes. Its " +
                    "selection filter -- 'a stagger of at most 4 bp' -- therefore excludes the " +
                    "closing family entirely, which is why CH-0188 found none of its five " +
                    "recommended pairs drawable except the one the filter had already dropped.",
            "THE_RECOMMENDATION" to
                    "$recommendedOne / $recommendedTwo bp. Inside the closing family it is " +
                    "jointly best on axial extent (" +
                    recommendation.blockExtentBasePairs + " bp = " +
                    recommendation.blockExtentNm.t245Rounded(4) + " nm, " +
                    recommendation.extentDeparturePercent.t245Rounded(3) + " % on SS3's " +
                    "40.0 nm) and strictly best among the pairs that tie there on every other " +
                    "axis: " + recommendation.rowDerivedColumns + " crossover columns against " +
                    "9 and 6, " + recommendation.stationsOnFace + " stations against 50 and 40, " +
                    "and a 7 bp stagger against 14 and 28.",
            "CLOSURE_COSTS_EXACTLY_ONE_CROSSOVER_COLUMN" to
                    "C-0140's 112 / 108 and the recommended pair have the SAME block extent, " +
                    "116 bp = 39.44 nm, so at the width SS3 is owed they are the same tile -- " +
                    "graded at the same column count every one of the eight cells agrees below " +
                    T245_SOLVED_FIELD_TOLERANCE + ", which is what a SOLVED field of this " +
                    "lattice can be asserted to (two identically constructed grillages differ " +
                    "by a few ulp). What differs is the " +
                    "INTERFACE window, 102 bp against 108, and with it the row-derived column " +
                    "count, " + recommendedColumns + " against " + baselineColumns + ". The " +
                    "entire cost of drawing the raster on caDNAno's own rules, on the flatness " +
                    "axis, is one crossover column.",
            "THE_PHASE_IS_NOW_A_DESIGN" to
                    "At the recommended pair the ladder phase is DETERMINED at " +
                    determinedPhase + " with the 14 bp inter-row offset, carrying " +
                    recommendation.stationsOnFace + " of 60 stations, 5 and 6 alternating. " +
                    "C-0141's and C-0146's 21-phase sweep is replaced by one design, and the " +
                    "sparsest row's " + recommendation.sparsestRowStations + " stations cap a " +
                    "placement at five columns -- so the six-column placement CH-0184 reported " +
                    "does not return.",
            "THE_SIX_COLUMN_PLACEMENT_RETURNS_AT_A_PRICE" to
                    "CH-0189 asks whether some CLOSING pair's determined phase saturates the " +
                    "station census, which would restore the six-column placement CH-0184 " +
                    "reported at an undrawable raster. Exactly " + saturating.size + " does " +
                    "inside M13 on 10 x 6 -- " +
                    (saturating.firstOrNull()?.let {
                        it.senseOneBasePairs.toString() + " / " + it.senseTwoBasePairs +
                                " bp, 60 of 60 stations, 6 per row, determined phase " +
                                it.ladderPhaseBasePairs + ", " + it.rowDerivedColumns +
                                " crossover columns -- and its axial extent is " +
                                it.blockExtentBasePairs + " bp = " +
                                it.blockExtentNm.t245Rounded(4) + " nm, " +
                                it.extentDeparturePercent.t245Rounded(4) + " % on SS3's 40.0 nm. " +
                                "No saturating closing pair inside M13 comes within 5 % of the " +
                                "nominal width. So the answer to CH-0189's open item is a PRICE, " +
                                "not a negative: the six-column placement exists and costs a " +
                                "tile 16 % too wide."
                    } ?: "none, so the six-column placement does not return at all."),
            "THE_BUDGETS" to
                    "Route A spends " + recommendation.scaffoldNucleotides + " nt of M13's " +
                    HONEYCOMB_M13_NUCLEOTIDES + " on 10 x 6, " +
                    recommendation.scaffoldSpareOnM13 + " spare against " +
                    baseline.scaffoldSpareOnM13 + " at 112 / 108 -- so closure BUYS " +
                    (recommendation.scaffoldSpareOnM13 - baseline.scaffoldSpareOnM13) +
                    " nt. Read as C-0147's own unpaired-nucleotide allowance, the same lengths " +
                    "afford " + recommendation.unpairedNucleotidesPerHelixOnM13 +
                    " nt per helix against " + uniformRouteBSlack + " at its uniform 112 bp row " +
                    "-- so the loop route, which C-0147 finds strained at 8 nt, gains headroom " +
                    "rather than losing it.",
            "THE_STACKING_MARGIN_BECOMES_QUOTABLE" to
                    "The front-face relief goes from 4 bp = 1.36 nm to 7 bp = " +
                    recommendation.frontFaceRaggednessNm.t245Rounded(4) + " nm, so the " +
                    "clearance past the blunt-end stacking onset goes from 0.18 rises -- below " +
                    "the design language's own quantum, and by CLAUDE.md's rule not a quotable " +
                    "margin at all -- to " + recommendation.stackingClearanceRises
                        .t245Rounded(3) + " rises."
        ),
        validity = listOf(
            "The +-5 bp rule is caDNAno's DEFAULT, not a law: the same paper permits forcing a " +
                    "crossover between any two scaffold bases and warns that departure from the " +
                    "default rules 'may lead to folding failure if too much deviation from " +
                    "canonical DNA geometry is implied'. A non-closing raster is off-rule and " +
                    "buildable, not impossible, and this repository still cannot price one.",
            "The half turn is 5.25 bp and caDNAno writes 5; every residue here inherits that " +
                    "rounding, and it is the source's own.",
            "The closing family is enumerated over " + T245_MIN_LENGTH + " to " +
                    T245_MAX_LENGTH + " bp at a stagger of at most " + T245_MAX_STAGGER +
                    " bp. The residue enumeration behind it is exhaustive; the LENGTH " +
                    "enumeration is not, and a pair outside that box is not excluded.",
            "The tile is a SMEARED equivalent sheet with one lengthX, so the inter-row axial " +
                    "stagger -- 7 bp here, larger than C-0140's 4 -- is not representable at " +
                    "all. Only the interface WINDOW carries it, through the column count.",
            "The grillage is single-layer and square-lattice in its crossover combinatorics " +
                    "(C-0141's standing caveat); a multi-layer honeycomb face enters only as a " +
                    "smeared equivalent sheet.",
            "The dropout statistics are measured on a single-layer Rothemund rectangle and only " +
                    "the profile transfers, in nm (C-0087, C-0109).",
            "The station census counts ONE face, the one pointing away from the grafted layer.",
            "Kirchhoff is not safe at these thicknesses (C-0109, C-0120): every D_parallel is an " +
                    "upper bound.",
            "Nothing here measures a folded object. The whole selection is lattice arithmetic on " +
                    "a published design rule."
        ),
        openQuestions = listOf(
            "What a FORCED scaffold crossover costs in folding yield -- the axis on which " +
                    "112 / 108 would have to be defended, and which no measurement in this " +
                    "corpus reaches (T-246).",
            "Whether the 7 bp inter-row stagger changes the ragged-face verdict C-0147 reached " +
                    "at 4 bp; its coefficient on SS3's flatness was exactly zero and the rim " +
                    "modulation bound scales with the relief.",
            "Whether C-0089's percentile descent recovers any cell the tenth column loses.",
            "Which width reading a folded block is owed against SS3 -- unchanged by this task, " +
                    "because both pairs give the same 116 bp box and the same 109 or 112 bp row."
        )
    )

    val output = File("gpd/results/T-245-closing-raster-selection.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.BOTH, null) as JsonObject)
        ) + "\n"
    )
    println("T-245 - wrote " + output.path)
}
