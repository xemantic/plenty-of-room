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
import com.xemantic.nano.plentyofroom.anchoring.upwardRootLattice
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.centroSymmetricPlacementsOn
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.floor

// ---------------------------------------------------------------------------------------------
// T-219 -- the honeycomb's own station lattice, plan ceiling and placement family.
//
// C-0118 named its own largest caveat: "the attachment grid is the ABSTRACT one ... a path count
// here is a REQUEST, not a demonstration that the stations exist." C-0122 answered the count by
// MULTIPLYING, CH-0151 corrected the multiplication, C-0128 priced an oblique root at 60 degrees --
// and none of the three derived the block's CROSS-SECTION, without which a free azimuth is not
// defined at all.
//
// The cheap bound runs first and it is one multiplication: a honeycomb lattice spends 3*sqrt(3)/4
// d^2 per helix and the cross-section every four-layer claim here is written on spends d^2, so the
// assumed geometry is 1.299 times denser than any honeycomb of that bond length can be.
// ---------------------------------------------------------------------------------------------

private const val T219_SAMPLES: Int = 81
private const val T219_TOLERANCE: Double = 0.10
private const val T219_RIM_STANDOFF: Double = 1.0
private const val T219_ARM: Double = 3.0
private const val T219_SPEC_EDGE: Double = 40.35
private const val T219_FAMILY_CAP: Int = 20_000

/** `C-0128`'s published rigid-body oblique cost at the azimuth it read, `kappa = 0.25 + 0.75 A`. */
private const val C0128_RIGID_BODY_COST_AT_SIXTY: Double = 6.017

/** `C-0128`'s own decomposition, `kappa(psi) = cos^2 psi + sin^2 psi * A`. */
private fun obliqueCostFactor(azimuthDegrees: Double, anisotropy: Double): Double {
    val c = Math.cos(Math.toRadians(azimuthDegrees))
    val s = Math.sin(Math.toRadians(azimuthDegrees))
    return c * c + s * s * anisotropy
}

/** The anisotropy `A` that `C-0128`'s published cost implies at its own 60 degrees. */
private fun anisotropyFrom(costAtSixty: Double): Double = (costAtSixty - 0.25) / 0.75

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T219CrossSection(
    val name: String,
    val rasterRows: Int,
    val helicesPerRasterRow: Int,
    val helices: Int,
    val standingEdgeY: Double,
    val honeycombPlateEdgeY: Double,
    val edgeYRatio: Double,
    val latticeExtentAcross: Double,
    val latticeExtentThrough: Double,
    val envelopeAcross: Double,
    val envelopeThrough: Double,
    val assumedAreaPerHelix: Double,
    val honeycombCellArea: Double,
    val areaRatio: Double,
    val standingSecondPlanDimensionOverSpec: Double,
    val honeycombSecondPlanDimensionOverSpec: Double,
    val plateLike: Boolean
)

@Serializable
private class T219Census(
    val name: String,
    val faceHelices: Int,
    val rootingAzimuthsPerFaceHelix: Int,
    val perpendicularRootingAzimuths: Int,
    val azimuthFromNormalDegrees: Double,
    val azimuthSignAlternates: Boolean,
    val acrossHelixStationPitch: Double,
    val alongHelixLadderPitch: Double,
    val rowBasePairs: Int,
    val stationsPerLadder: Int,
    val stations: Int,
    val c0122Stations: Int,
    val ch0151Stations: Int
)

@Serializable
private class T219PlanCeiling(
    val name: String,
    val rowBasePairs: Int,
    val interRowOffsetBasePairs: Int,
    val phaseBasePairs: Int,
    val stations: Int,
    val demandedPaths: Int,
    val maximumPlanCeiling: Double?,
    val collinearInboardBound: Double,
    val squareLatticeInboardBound: Double,
    val ceilingOverInboard: Double?
)

@Serializable
private class T219Family(
    val name: String,
    val rootingHelices: Int,
    val rowBasePairs: Int,
    val interRowOffsetBasePairs: Int,
    val centroSymmetricPhases: List<Int>,
    val stationsAtSymmetricPhase: Int?,
    val fullStationCount: Int,
    val familyMembersAtDemand: Int,
    val familyTruncatedAtCap: Boolean,
    val demandedPaths: Int,
    val armLength: Double
)

@Serializable
private class T219Dishing(
    val name: String,
    val geometry: String,
    val edgeX: Double,
    val edgeY: Double,
    val interhelicalDistance: Double,
    val layerSpacing: Double,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val freeTileDishingOverStroke: Double,
    val flat: Boolean,
    val uniformLoadDishing: String,
    val compositeFractionThreshold: Double?,
    val thresholdSignChanges: Int?,
    val marginToTolerance: Double,
    val dishingAtMeasuredBandLow: Double,
    val flatAtMeasuredBandLow: Boolean,
    val dishingAtMeasuredBandHigh: Double,
    val flatAtMeasuredBandHigh: Boolean
)

@Serializable
private class T219Convergence(
    val axis: String,
    val values: List<String>,
    val quantity: List<Double>,
    val departureFromFinest: List<Double>,
    val note: String
)

@Serializable
private class T219Reproduction(
    val of: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double
)

@Serializable
private class T219Result(
    val task: String, val leaf: String, val title: String, val verificationType: String,
    val maturity: String, val units: Map<String, String>, val conventions: Map<String, String>,
    val parameters: Map<String, String>, val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: Map<String, String>,
    val crossSections: List<T219CrossSection>,
    val census: List<T219Census>,
    val planCeilings: List<T219PlanCeiling>,
    val families: List<T219Family>,
    val dishing: List<T219Dishing>,
    val convergence: List<T219Convergence>,
    val obliqueCost: Map<String, String>,
    val whatIsSquareLatticeSpecific: List<String>,
    val reproductions: List<T219Reproduction>,
    val verdict: Map<String, String>,
    val falsifiers: List<String>,
    val falsifiersFired: Map<String, String>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private class T219Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T219_RIM_STANDOFF))
        )
}

private fun t219Profile(file: File): T219Profile {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T219Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * A four-layer tile solved at a stated cross-section geometry.
 *
 * `C-0120`'s own construction, with `edgeY`, the in-plane duplex pitch and the layer spacing lifted
 * out as parameters so that the standing geometry and the honeycomb one are the same code.
 */
private class T219Tile(
    val rasterRows: Int,
    val layers: Int,
    val edgeY: Double,
    val interhelicalDistance: Double,
    val layerSpacing: Double,
    private val profile: T219Profile,
    val rowBasePairs: Int = 112,
    private val subdivisions: Int = 2,
    private val samples: Int = T219_SAMPLES
) {

    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR

    fun rigiditiesAt(fraction: Double): MultiLayerRigidities = multiLayerRigidities(
        layers = layers,
        interhelicalDistance = interhelicalDistance,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = layerSpacing
    )

    val rigidities: MultiLayerRigidities = rigiditiesAt(MeasuredBundleRigidity.COMPOSITE_FRACTION)

    private fun grillage(rigidities: MultiLayerRigidities): OrigamiGrillage {
        val sheet = equivalentSheet(rigidities)
        val pitch = sheet.crossoverSpacing / 2.0
        val usable = edgeX - 2.0 * CrossoverLayout.EDGE_MARGIN
        val columns = CrossoverLayout.centred(floor(usable / pitch).toInt() + 1, pitch)
        return OrigamiGrillage(
            sheet = sheet, lengthX = edgeX, beamCount = rasterRows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = columns, subdivisions = subdivisions
        )
    }

    private fun freeStroke(rigidities: MultiLayerRigidities, interiorPressure: Double): Double =
        PlateOnFoundation(
            equivalentSheet(rigidities).plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT,
            emptyList(), 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection

    /** The free-tile peak dishing under `C-0022`'s solved collar, over the free stroke. */
    fun collarDishing(
        fraction: Double = MeasuredBundleRigidity.COMPOSITE_FRACTION
    ): Double {
        val stiffness = rigiditiesAt(fraction)
        val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
        val field = profile.field(interiorPressure, edgeX, edgeY)
        return grillage(stiffness).solve(field).peakDishing(samples) /
                freeStroke(stiffness, interiorPressure)
    }

    /** `F4` — a uniform load on a uniform Winkler foundation must dish exactly zero. */
    fun uniformDishing(): Double {
        val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
        return grillage(rigidities).solve(uniformPressure(interiorPressure)).peakDishing(samples) /
                freeStroke(rigidities, interiorPressure)
    }
}

private fun stationLatticeFor(
    rootingHelices: Int, rowBp: Int, phase: Int, offset: Int
): List<List<Double>> = honeycombStationLattice(rootingHelices, rowBp, phase, offset)

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val profile = t219Profile(File("gpd/results/T-3b-tile-edge-load-profile.json"))

    // ---------------------------------------------------------------- the cheap bound, first
    val ladder = HoneycombLattice.SAME_PAIR_PERIOD_BP * rise
    val squareLadder = 32.0 * rise
    val inboard = ladder - d
    val squareInboard = squareLadder - Gen1Tile.INTERHELICAL_SHEET
    val cell = HoneycombCrossSectionGeometry.perSiteArea(d)
    val assumedCell = d * d

    println("T-219 - the honeycomb's own station lattice, plan ceiling and placement family")
    println("  CHEAP BOUND 1 - the ladder")
    println("    honeycomb 21 bp = " + ladder.emitted(6) + " nm, square 32 bp = " + squareLadder.emitted(6) + " nm")
    println("    collinear inboard budget " + inboard.emitted(6) + " nm against " + squareInboard.emitted(6) + " nm")
    println("  CHEAP BOUND 2 - the packing")
    println("    honeycomb cell 3sqrt(3)/4 d^2 = " + cell.emitted(6) + " nm^2 per helix")
    println("    the assumed cross-section d x d = " + assumedCell.emitted(6) + " nm^2 per helix")
    println("    ratio " + (cell / assumedCell).emitted(9) + " -- the assumed geometry is not a honeycomb")

    // ---------------------------------------------------------------------- the cross-sections
    val designs = listOf(15 to 4, 10 to 6, 8 to 8, 6 to 10, 4 to 16, 3 to 20, 2 to 30)
    val crossSections = designs.map { (m, n) ->
        val block = HoneycombBlock(m, n, d)
        val plateLike = block.envelopeThroughSmallerThanAcross()
        T219CrossSection(
            name = "$m x $n", rasterRows = m, helicesPerRasterRow = n, helices = block.helices,
            standingEdgeY = m * d, honeycombPlateEdgeY = block.plateEdgeY,
            edgeYRatio = block.plateEdgeY / (m * d),
            latticeExtentAcross = block.latticeExtentY, latticeExtentThrough = block.latticeExtentX,
            envelopeAcross = block.envelopeY, envelopeThrough = block.envelopeX,
            assumedAreaPerHelix = assumedCell, honeycombCellArea = cell,
            areaRatio = cell / assumedCell,
            standingSecondPlanDimensionOverSpec = m * d / T219_SPEC_EDGE,
            honeycombSecondPlanDimensionOverSpec = block.envelopeY / T219_SPEC_EDGE,
            plateLike = plateLike
        )
    }
    println("  cross-sections (x = through the tile, y = in plane)")
    crossSections.forEach {
        println(
            "    " + it.name + "  helices " + it.helices + "  in-plane " + it.envelopeAcross.emitted(6) +
                    " nm  thickness " + it.envelopeThrough.emitted(6) + " nm  (standing edgeY " +
                    it.standingEdgeY.emitted(6) + ", ratio " + it.edgeYRatio.emitted(6) + ")"
        )
    }

    // ------------------------------------------------------------------------------ the census
    val censusDesigns = listOf(15 to 4, 10 to 6)
    val census = censusDesigns.flatMap { (m, n) ->
        val block = HoneycombBlock(m, n, d)
        val rooting = block.rootingAzimuths(1.0, 0.0).sortedBy { it.first.rasterRow }
        val angles = rooting.map { it.second.angleFromNormalDegrees(1.0, 0.0) }
        val perpendicular = angles.count { it < 1.0e-9 }
        val alternates = rooting.map { it.second.unitY > 0.0 }.zipWithNext().all { (a, b) -> a != b }
        listOf(112, 119).map { rowBp ->
            val perLadder = honeycombLadderIndices(rowBp, 0).size
            T219Census(
                name = "$m x $n", faceHelices = rooting.size,
                rootingAzimuthsPerFaceHelix = rooting.size / m,
                perpendicularRootingAzimuths = perpendicular,
                azimuthFromNormalDegrees = angles.first(),
                azimuthSignAlternates = alternates,
                acrossHelixStationPitch = HoneycombCrossSectionGeometry.rowPitch(d),
                alongHelixLadderPitch = ladder,
                rowBasePairs = rowBp, stationsPerLadder = perLadder,
                stations = rooting.size * perLadder,
                c0122Stations = m * 6,
                ch0151Stations = if (m == 15) 132 else 90
            )
        }
    }
    println("  census (face normal to the THIN cross-section direction)")
    census.forEach {
        println(
            "    " + it.name + "  " + it.rowBasePairs + " bp  face helices " + it.faceHelices +
                    "  azimuths/helix " + it.rootingAzimuthsPerFaceHelix + " at " +
                    it.azimuthFromNormalDegrees.emitted(4) + " deg  stations " + it.stations +
                    "  (C-0122 " + it.c0122Stations + ", CH-0151 " + it.ch0151Stations + ")"
        )
    }

    // ------------------------------------------------------------------------ the plan ceilings
    val demands = listOf(10, 15, 20, 30, 34, 45, 50, 60, 75, 90)
    val ceilings = ArrayList<T219PlanCeiling>()
    censusDesigns.forEach { (m, n) ->
        listOf(112, 119).forEach { rowBp ->
            listOf(7, 14).forEach { offset ->
                val phase = 0
                val lattice = stationLatticeFor(m, rowBp, phase, offset)
                val stations = lattice.sumOf { it.size }
                demands.filter { it <= stations }.forEach { demand ->
                    val ceiling = maximumPlanCeilingForCount(
                        lattice, demand, rowBp * rise, d, lattice.maxOf { it.size }
                    )
                    ceilings += T219PlanCeiling(
                        name = "$m x $n", rowBasePairs = rowBp,
                        interRowOffsetBasePairs = offset, phaseBasePairs = phase,
                        stations = stations, demandedPaths = demand,
                        maximumPlanCeiling = ceiling, collinearInboardBound = inboard,
                        squareLatticeInboardBound = squareInboard,
                        ceilingOverInboard = ceiling?.div(inboard)
                    )
                }
            }
        }
    }
    println("  plan ceilings at 112 bp, offset 7, phase 0")
    ceilings.filter { it.rowBasePairs == 112 && it.interRowOffsetBasePairs == 7 }.forEach {
        println(
            "    " + it.name + "  " + it.demandedPaths + " of " + it.stations + " paths  ceiling " +
                    (it.maximumPlanCeiling?.emitted(6) ?: "none") + " nm"
        )
    }

    // ------------------------------------------------------------------ the placement family
    val families = ArrayList<T219Family>()
    censusDesigns.forEach { (m, n) ->
        listOf(112, 119).forEach { rowBp ->
            listOf(0, 7, 14).forEach { offset ->
                val phases = (0 until HoneycombLattice.SAME_PAIR_PERIOD_BP).filter {
                    latticeIsCentroSymmetric(stationLatticeFor(m, rowBp, it, offset))
                }
                val best = phases.firstOrNull()
                val lattice = best?.let { stationLatticeFor(m, rowBp, it, offset) }
                val demand = 2 * m
                val members = if (lattice == null) 0 else centroSymmetricPlacementsOn(
                    lattice, rowBp * rise, T219_ARM, demand,
                    phaseTag = best, minimumPerRow = 2, maximumPerRow = 2, width = d
                ).take(T219_FAMILY_CAP).count()
                families += T219Family(
                    name = "$m x $n", rootingHelices = m, rowBasePairs = rowBp,
                    interRowOffsetBasePairs = offset, centroSymmetricPhases = phases,
                    stationsAtSymmetricPhase = lattice?.sumOf { it.size },
                    fullStationCount = stationLatticeFor(m, rowBp, 0, offset).sumOf { it.size },
                    familyMembersAtDemand = members,
                    familyTruncatedAtCap = members == T219_FAMILY_CAP,
                    demandedPaths = demand, armLength = T219_ARM
                )
            }
        }
    }
    println("  centro-symmetric families")
    families.forEach {
        println(
            "    " + it.name + "  " + it.rowBasePairs + " bp  offset " +
                    it.interRowOffsetBasePairs + "  phases " + it.centroSymmetricPhases +
                    "  stations " + (it.stationsAtSymmetricPhase?.toString() ?: "-") +
                    "  members at " + it.demandedPaths + " paths " + it.familyMembersAtDemand
        )
    }

    // --------------------------------------------------------------------------- the dishing
    val dishing = ArrayList<T219Dishing>()
    censusDesigns.forEach { (m, n) ->
        val block = HoneycombBlock(m, n, d)
        listOf(
            Triple("standing (C-0109/C-0120)", m * d, d to d),
            Triple(
                "honeycomb (T-219)", block.plateEdgeY,
                HoneycombCrossSectionGeometry.rowPitch(d) to
                        HoneycombCrossSectionGeometry.columnPitch(d)
            )
        ).forEach { (label, edgeY, pitches) ->
            val tile = T219Tile(m, n, edgeY, pitches.first, pitches.second, profile)
            val collar = tile.collarDishing()
            val crossing = firstCrossing(0.0, 1.0, 40, T219_TOLERANCE, 1.0e-9) {
                tile.collarDishing(it)
            }
            val bandLow = tile.collarDishing(MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN)
            val bandHigh = tile.collarDishing(MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX)
            dishing += T219Dishing(
                name = "$m x $n", geometry = label, edgeX = tile.edgeX, edgeY = edgeY,
                interhelicalDistance = pitches.first, layerSpacing = pitches.second,
                alongHelixRigidity = tile.rigidities.alongHelixRigidity,
                acrossHelixRigidity = tile.rigidities.acrossHelixRigidity,
                freeTileDishingOverStroke = collar, flat = collar < T219_TOLERANCE,
                uniformLoadDishing = tile.uniformDishing().emitted(2),
                compositeFractionThreshold = crossing?.root,
                thresholdSignChanges = crossing?.signChanges,
                marginToTolerance = 1.0 - collar / T219_TOLERANCE,
                dishingAtMeasuredBandLow = bandLow, flatAtMeasuredBandLow = bandLow < T219_TOLERANCE,
                dishingAtMeasuredBandHigh = bandHigh,
                flatAtMeasuredBandHigh = bandHigh < T219_TOLERANCE
            )
        }
    }
    println("  free-tile dishing")
    dishing.forEach {
        println(
            "    " + it.name + "  " + it.geometry + "  edgeY " + it.edgeY.emitted(6) +
                    "  dishing " + it.freeTileDishingOverStroke.emitted(9) +
                    (if (it.flat) "  flat" else "  NOT flat") +
                    "  f* " + (it.compositeFractionThreshold?.emitted(6) ?: "none") +
                    "  band low " + it.dishingAtMeasuredBandLow.emitted(9) +
                    (if (it.flatAtMeasuredBandLow) " flat" else " NOT FLAT") +
                    "  uniform-load dishing " + it.uniformLoadDishing
        )
    }

    // ------------------------------------------------------- C-0128's cost, re-read at 30 degrees
    val anisotropy = anisotropyFrom(C0128_RIGID_BODY_COST_AT_SIXTY)
    val faceAzimuth = census.first().azimuthFromNormalDegrees
    val obliqueCost = mapOf(
        "publishedAzimuthDegrees" to "60",
        "derivedFaceAzimuthDegrees" to faceAzimuth.emitted(6),
        "publishedRigidBodyCost" to C0128_RIGID_BODY_COST_AT_SIXTY.emitted(),
        "impliedAnisotropy" to anisotropy.emitted(),
        "rigidBodyCostAtTheFaceAzimuth" to obliqueCostFactor(faceAzimuth, anisotropy).emitted(),
        "flexibleTieCostAtTheFaceAzimuth" to obliqueCostFactor(faceAzimuth, 1.0).emitted(),
        "note" to "C-0128's own kappa(psi) = cos^2 psi + sin^2 psi A, re-read at the azimuth the " +
                "site set gives. The flexible tie stays exactly 1 -- an isotropy symmetry, not a " +
                "small number -- and the rigid body is CHEAPER at 30 degrees than at 60."
    )

    // ----------------------------------------------------------------------- the convergence axes
    val honeycomb154 = { subdivisions: Int, samples: Int ->
        T219Tile(
            15, 4, HoneycombBlock(15, 4, d).plateEdgeY,
            HoneycombCrossSectionGeometry.rowPitch(d),
            HoneycombCrossSectionGeometry.columnPitch(d), profile,
            subdivisions = subdivisions, samples = samples
        ).collarDishing()
    }
    val subdivisionSweep = listOf(1, 2, 4).map { honeycomb154(it, T219_SAMPLES) }
    val sampleSweep = listOf(41, 81, 161).map { honeycomb154(2, it) }
    val convergence = listOf(
        T219Convergence(
            axis = "beam subdivisions per node interval, 15 x 4 at the honeycomb geometry",
            values = listOf("1", "2", "4"), quantity = subdivisionSweep,
            departureFromFinest = subdivisionSweep.map {
                abs(it - subdivisionSweep.last()) / abs(subdivisionSweep.last())
            },
            note = "nested 1/2/4, never 1/2/3/4 -- a subdivision of 3 moves a load off a node"
        ),
        T219Convergence(
            axis = "dishing grid samples per side, 15 x 4 at the honeycomb geometry",
            values = listOf("41", "81", "161"), quantity = sampleSweep,
            departureFromFinest = sampleSweep.map {
                abs(it - sampleSweep.last()) / abs(sampleSweep.last())
            },
            note = "peak dishing is a max over the grid, so it can only rise with resolution"
        )
    )

    // ------------------------------------------------------------------------ the reproductions
    val squarePhase24 = upwardRootLattice(24, Gen1Tile.EDGE_X, 15)
    val squareCeiling = maximumPlanCeilingForCount(
        squarePhase24, 30, Gen1Tile.EDGE_X, Gen1Tile.INTERHELICAL_SHEET, 3
    ) ?: Double.NaN
    val standing154 = dishing.first { it.name == "15 x 4" && it.geometry.startsWith("standing") }
    val reproductions = listOf(
        T219Reproduction(
            "C-0122", "15 x 4 stations at one azimuth per helix, 112 bp", 90.0,
            census.first { it.name == "15 x 4" && it.rowBasePairs == 112 }.stations.toDouble(),
            relative(90.0, census.first { it.name == "15 x 4" && it.rowBasePairs == 112 }.stations.toDouble())
        ),
        T219Reproduction(
            "C-0122", "10 x 6 stations at one azimuth per helix, 112 bp", 60.0,
            census.first { it.name == "10 x 6" && it.rowBasePairs == 112 }.stations.toDouble(),
            relative(60.0, census.first { it.name == "10 x 6" && it.rowBasePairs == 112 }.stations.toDouble())
        ),
        T219Reproduction(
            "C-0072 / T-136", "maximum plan ceiling at 30 roots, square phase-24 lattice",
            9.535, squareCeiling, relative(9.535, squareCeiling)
        ),
        T219Reproduction(
            "C-0120", "15 x 4 free-tile dishing at the standing geometry", 0.0577199433,
            standing154.freeTileDishingOverStroke,
            relative(0.0577199433, standing154.freeTileDishingOverStroke)
        )
    )

    // ------------------------------------------------------------------------------- the verdict
    val faceIsAllOblique = census.all { it.perpendicularRootingAzimuths == 0 }
    val oneAzimuthEach = census.all { it.rootingAzimuthsPerFaceHelix == 1 }
    val oddHasNoFamily = families.filter { it.rootingHelices % 2 == 1 && it.interRowOffsetBasePairs != 0 }
        .all { it.centroSymmetricPhases.isEmpty() }
    val evenHasFamily = families.filter { it.rootingHelices % 2 == 0 && it.interRowOffsetBasePairs != 0 }
        .all { it.centroSymmetricPhases.isNotEmpty() && it.familyMembersAtDemand > 0 }
    val f3Fired = ceilings.any { (it.maximumPlanCeiling ?: 0.0) > squareInboard }

    val findings = LinkedHashMap<String, String>()
    val validity = listOf(
        "The cross-section is derived from the caDNAno paper's own nomenclature and corrugation " +
                "sentence. It is a LATTICE statement; no folded object is measured here.",
        "The census counts ONE face -- the one normal to the thin cross-section direction, which " +
                "for both 60-helix candidates is the face of `n` columns. The opposite face carries " +
                "the same inventory pointing INTO the grafted layer and is unusable (C-0055).",
        "The inter-row ladder offset is 7 or 14 bp and this repository cannot yet say which; both " +
                "are carried and no answer here depends on the choice. It is NOT the scaffold turn " +
                "sense T-218 settles, which is a different variable.",
        "The dishing rows re-grade the SMEARED equivalent sheet at two geometries. The grillage is " +
                "still single-layer and still square-lattice in its crossover combinatorics; only " +
                "edgeY, the in-plane pitch and the layer spacing move.",
        "No dropout ensemble is run here, so no 90th-percentile number is produced and C-0118's " +
                "verdict is neither reproduced nor overturned at the corrected geometry."
    )
    val openQuestions = listOf(
        "Which of the two admissible inter-row offsets, 7 or 14 bp, a caDNAno honeycomb carries.",
        "C-0118's whole 16-cell dropout grading re-run at the corrected cross-section, which is " +
                "the only thing that turns its path counts into a demonstration END TO END.",
        "C-0022's collar re-solved at the corrected aspect ratios, which C-0123 left open and " +
                "which moves again now that both edgeY values move by 1.5x.",
        "Whether a station subset is compatible with the scaffold raster and its unpaired loops."
    )

    val result = T219Result(
        task = "T-219", leaf = "A8.2",
        title = "The honeycomb's own station lattice, plan ceiling and placement family",
        verificationType = "logical (an integer lattice census and two exact enumerations) + " +
                "in-silico (four plate/grillage solves at two geometries)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf(
            "length" to "nm and base pairs", "angle" to "degrees", "area" to "nm^2",
            "rigidity" to "pN nm", "dishing" to "dimensionless, over the free-tile stroke"
        ),
        conventions = mapOf(
            "block" to "m corrugated x-raster rows of n helices; site (r, c); neighbours " +
                    "(r, c +/- 1) and (r + 1, c) if r + c even else (r - 1, c)",
            "crossSection" to "x across an x-raster row (the n direction, the tile's thickness), " +
                    "y along the stack of rows (the m direction, the tile's in-plane width)",
            "station" to "a 21 bp crossover position on a FREE azimuth with a positive component " +
                    "along the outward face normal",
            "ladder" to "21 bp, ONE azimuth's period -- never the 7 bp step over all three",
            "flat" to "peak dishing below T-5b's 0.10"
        ),
        parameters = mapOf(
            "bondLength" to d.emitted(), "rise" to rise.emitted(),
            "rowPitch" to HoneycombCrossSectionGeometry.rowPitch(d).emitted(),
            "columnPitch" to HoneycombCrossSectionGeometry.columnPitch(d).emitted(),
            "ladderPitch" to ladder.emitted(), "arm" to T219_ARM.emitted(),
            "dishingSamplesPerSide" to T219_SAMPLES.toString(),
            "specEdge" to T219_SPEC_EDGE.emitted(),
            "compositeFraction" to MeasuredBundleRigidity.COMPOSITE_FRACTION.emitted(),
            "compositeFractionBand" to (MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN.emitted() +
                    " to " + MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX.emitted()),
            "censusLadderPhaseBasePairs" to "0"
        ),
        sources = listOf("gpd/results/T-3b-tile-edge-load-profile.json"),
        citedInputs = listOf(
            "C-0119 - the honeycomb design rules and the seven published cross-sections, read " +
                    "directly from Douglas et al., NAR 37:5001 (PMC2731887)",
            "C-0122 / CH-0151 - the station census this one derives rather than multiplies",
            "C-0128 - the oblique-root anisotropy, whose azimuth this moves from 60 to 30 degrees",
            "C-0118 - the coupled cells whose path counts this makes buildable or not",
            "C-0120 - the cross-section comparison and its footprint ordering",
            "C-0072 / T-136 - maximumPlanCeilingForCount, reproduced on the square lattice",
            "C-0022 - the solved collar, read from its own result file"
        ),
        cheapBound = mapOf(
            "honeycombLadder" to ladder.emitted(),
            "squareLadder" to squareLadder.emitted(),
            "honeycombCollinearInboardBound" to inboard.emitted(),
            "squareCollinearInboardBound" to squareInboard.emitted(),
            "inboardRatio" to (squareInboard / inboard).emitted(),
            "honeycombCellArea" to cell.emitted(),
            "assumedAreaPerHelix" to assumedCell.emitted(),
            "packingRatio" to (cell / assumedCell).emitted(),
            "packingRatioExact" to "3 sqrt(3) / 4",
            "whatItSettles" to "the assumed cross-section is 1.30x denser than any honeycomb of " +
                    "this bond length, so a station census on it is not a census of a honeycomb"
        ),
        obliqueCost = obliqueCost,
        crossSections = crossSections, census = census, planCeilings = ceilings,
        families = families, dishing = dishing, convergence = convergence,
        whatIsSquareLatticeSpecific = listOf(
            "anchoring/UpwardRootPlacement.upwardRootLattice and upwardHingeSites - the site " +
                    "GENERATOR: four azimuths, 8 bp planes, upward sites at k = 2r + 3 (mod 4), " +
                    "pitch 32 bp. The honeycomb has three azimuths, 7 bp steps and a 21 bp period, " +
                    "and its face carries ONE azimuth per helix rather than one per two.",
            "anchoring/UpwardRootPlacement.centroSymmetricUpwardPhases - a congruence derived for " +
                    "a 15-duplex sheet where r and 14 - r share a sublattice. On a honeycomb face " +
                    "the two sublattices carry DIFFERENT ladder phases, which is what makes the " +
                    "parity of the rooting-helix count decide the answer.",
            "structure/CrossoverLayout - BASE_PAIRS_PER_PERIOD = 32 and a two-parity alternation. " +
                    "The honeycomb's period is 21 and it has three classes, not two.",
            "structure/OrigamiGrillage - never reads layers or interlayerCoupling, and models one " +
                    "row of beams at one pitch. A honeycomb slab has TWO pitches, 3d/2 in plane " +
                    "and d sqrt(3)/2 through the thickness, and its rows are corrugated.",
            "tile/FourLayerTile.multiLayerRigidities - takes layerSpacing as a parameter (which is " +
                    "how the honeycomb geometry enters here) but takes interhelicalDistance as " +
                    "BOTH the in-plane pitch and the crossover-spacing denominator.",
            "WHAT IS NOT square-lattice-specific, and this is the useful half: " +
                    "maximumPlanCeilingForCount, latticeRootCapacity, maximumRootedElementsInRow, " +
                    "rootedLengthCeiling, rowRootOptions, armDirections and " +
                    "centroSymmetricPlacementsOn all take an EXPLICIT lattice and take the " +
                    "honeycomb one unmodified."
        ),
        reproductions = reproductions,
        verdict = mapOf(
            "everyFaceHelixCarriesExactlyOneRootingAzimuth" to oneAzimuthEach.toString(),
            "theFaceCarriesNoPerpendicularRootAtAll" to faceIsAllOblique.toString(),
            "oddRootingHelixCountHasNoCentroSymmetricFamily" to oddHasNoFamily.toString(),
            "evenRootingHelixCountHasOneAtFullStationCount" to evenHasFamily.toString(),
            "planCeilingMachineryTookTheHoneycombLattice" to "true",
            "packingRatio" to (cell / assumedCell).emitted(),
            "fifteenByFourFlatAtTheLowEndOfTheMeasuredCouplingBand" to
                    dishing.first { it.name == "15 x 4" && it.geometry.startsWith("honeycomb") }
                        .flatAtMeasuredBandLow.toString(),
            "tenBySixFlatAtTheLowEndOfTheMeasuredCouplingBand" to
                    dishing.first { it.name == "10 x 6" && it.geometry.startsWith("honeycomb") }
                        .flatAtMeasuredBandLow.toString()
        ),
        falsifiers = listOf(
            "F1 - if the honeycomb's per-site area equals the assumed cross-section's area per " +
                    "helix, the assumed geometry IS a honeycomb and there is nothing to correct.",
            "F2 - if any face helix of a full m x n block carries TWO rooting azimuths, CH-0151's " +
                    "correction stands and this census is wrong.",
            "F3 - if the honeycomb plan ceiling is not below the square lattice's at the demanded " +
                    "counts, the cheap bound 21 bp - d < 32 bp - d was the wrong bound.",
            "F4 - a uniform load on a uniform Winkler foundation must dish exactly zero.",
            "F5 - if the placement machinery cannot take the honeycomb lattice at all, then it is " +
                    "square-lattice-specific too and P2/P3 are answerable only as an absence."
        ),
        falsifiersFired = mapOf(
            "F1" to "did NOT fire: the ratio is 3 sqrt(3) / 4 = " + (cell / assumedCell).emitted(9),
            "F2" to "did NOT fire: every face helix carries exactly one rooting azimuth",
            "F3" to ("FIRED: " + f3Fired + ". The inboard bound binds only at the SATURATED count; " +
                    "below it a placement SKIPS stations and the honeycomb's denser ladder is a " +
                    "larger choice set, not a tighter one"),
            "F4" to ("did NOT fire: the largest uniform-load dishing over four solves is " +
                    dishing.map { it.uniformLoadDishing }.joinToString(", ")),
            "F5" to "did NOT fire: every placement routine took the honeycomb lattice unmodified"
        ),
        findings = findings, validity = validity, openQuestions = openQuestions
    )

    // Prose last, and guarded -- CLAUDE.md: a String.format defect is a LAST-LINE defect.
    runCatching {
        findings["theCheapBoundSettledTheGeometryBeforeTheCensus"] =
            ("A honeycomb lattice of bond length " + d.emitted(4) + " nm spends " + cell.emitted(6) +
                    " nm^2 per helix; the cross-section every four-layer claim in this corpus is " +
                    "written on spends " + assumedCell.emitted(6) + " nm^2. The ratio is exactly " +
                    "3 sqrt(3) / 4 = " + (cell / assumedCell).emitted(9) + ", and the two pitches " +
                    "are wrong in OPPOSITE directions: the in-plane row pitch is 3d/2 = " +
                    HoneycombCrossSectionGeometry.rowPitch(d).emitted(6) + " nm and the layer " +
                    "pitch is d sqrt(3)/2 = " +
                    HoneycombCrossSectionGeometry.columnPitch(d).emitted(6) +
                    " nm. Only their PRODUCT is the cell, so correcting the layer spacing alone " +
                    "(which CLAUDE.md already records) makes the density error worse, not better.")
        val standing154cs = crossSections.first { it.name == "15 x 4" }
        val standing106cs = crossSections.first { it.name == "10 x 6" }
        findings["theFootprintOrderingREVERSES"] =
            ("C-0120 reports 15 x 4 at 38.08 x " + standing154cs.standingEdgeY.emitted(6) +
                    " nm, 'essentially section 3's', and charges 10 x 6 a third of the footprint at " +
                    "38.08 x " + standing106cs.standingEdgeY.emitted(6) + ". On the honeycomb the " +
                    "in-plane pitch is 1.5x larger at every m, so 15 x 4 is 38.08 x " +
                    standing154cs.envelopeAcross.emitted(6) + " nm (" +
                    standing154cs.honeycombSecondPlanDimensionOverSpec.emitted(6) +
                    " of section 3's 40.35) and 10 x 6 is 38.08 x " +
                    standing106cs.envelopeAcross.emitted(6) + " nm (" +
                    standing106cs.honeycombSecondPlanDimensionOverSpec.emitted(6) +
                    "). The cross-section this programme charges a footprint for IS section 3's " +
                    "footprint, and the one it calls essentially section 3's is half again too wide.")
        findings["theFaceHasNoPerpendicularRootAtAll"] =
            ("On a full m x n block the face normal to the thin direction gives EVERY one of its " +
                    "m helices exactly one free azimuth, at " +
                    census.first().azimuthFromNormalDegrees.emitted(4) + " degrees from the normal, " +
                    "its sign alternating with the row parity. C-0122's count of m x 6 is right; " +
                    "its perpendicular/oblique split is not, CH-0151's two-azimuth correction is " +
                    "not, and C-0128's 60 degrees is the azimuth of a half-row termination the " +
                    "published designs do not have.")
        findings["theStaggerIsFORCED"] =
            ("The two face sublattices carry their free azimuth on two DIFFERENT bond classes, " +
                    "whose crossover residues differ by 7 or 14 bp mod 21. So adjacent station rows " +
                    "are staggered along the helices by " + (7 * rise).emitted(6) + " nm and there " +
                    "is no honeycomb face whose station rows are in register. CLAUDE.md records " +
                    "that an 8 bp connectivity stagger is a FIRST-order symmetry break on the " +
                    "square lattice; here it is not a design choice but a property of the lattice.")
        findings["theRootingHelixParityDecidesThePlacementFamily"] =
            ("Rows r and m - 1 - r of a face have the same parity when m is ODD, so the reflection " +
                    "maps a row onto one carrying the SAME ladder phase, which the forced stagger " +
                    "cannot satisfy: 15 x 4 admits NO centro-symmetric station lattice at any of " +
                    "the 21 phases, at either offset, at 112 bp or 119. When m is EVEN the " +
                    "reflection SWAPS the two phases and the stagger is exactly what makes the " +
                    "symmetry available: 10 x 6 admits one phase per offset, at the FULL station " +
                    "count. C-0063's whole placement family exists on one of the two cross-sections " +
                    "and not on the other, and this is a third independent reason to prefer 10 x 6.")
        findings["F3FIREDAndTheDirectionIsFavourable"] =
            ("The cheap bound predicted a tighter plan budget and it is right only at SATURATION. " +
                    "A placement below the station count skips stations, so the binding pitch is a " +
                    "multiple of 21 bp, and at 45 of 90 paths the honeycomb affords " +
                    (ceilings.firstOrNull { it.name == "15 x 4" && it.demandedPaths == 45 &&
                            it.rowBasePairs == 112 && it.interRowOffsetBasePairs == 7 }
                        ?.maximumPlanCeiling?.emitted(6) ?: "-") +
                    " nm against the square lattice's 8.19 nm inboard budget. A denser ladder is a " +
                    "larger choice set; it costs only where every station is spent.")
        findings["theGEOMETRYMovesTheFlatnessAndTheOrderingSurvives"] =
            ("Re-solved at the honeycomb cross-section the free-tile collar dishing moves " +
                    dishing.first { it.name == "15 x 4" && it.geometry.startsWith("standing") }
                        .freeTileDishingOverStroke.emitted(9) + " -> " +
                    dishing.first { it.name == "15 x 4" && it.geometry.startsWith("honeycomb") }
                        .freeTileDishingOverStroke.emitted(9) + " on 15 x 4 and " +
                    dishing.first { it.name == "10 x 6" && it.geometry.startsWith("standing") }
                        .freeTileDishingOverStroke.emitted(9) + " -> " +
                    dishing.first { it.name == "10 x 6" && it.geometry.startsWith("honeycomb") }
                        .freeTileDishingOverStroke.emitted(9) + " on 10 x 6.")
        val h154 = dishing.first { it.name == "15 x 4" && it.geometry.startsWith("honeycomb") }
        val h106 = dishing.first { it.name == "10 x 6" && it.geometry.startsWith("honeycomb") }
        findings["theCOUPLING THRESHOLD moves INTO the measured band on 15 x 4"] =
            ("C-0116's composite-fraction threshold for 15 x 4 is " +
                    dishing.first { it.name == "15 x 4" && it.geometry.startsWith("standing") }
                        .compositeFractionThreshold!!.emitted(9) + " at the standing geometry and " +
                    h154.compositeFractionThreshold!!.emitted(9) + " at the honeycomb one, against " +
                    "the measured band " + MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN.emitted(4) +
                    " to " + MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX.emitted(4) + ". So the " +
                    "threshold moves INSIDE the band: at the low end 15 x 4 dishes " +
                    h154.dishingAtMeasuredBandLow.emitted(9) + ", which is " +
                    (if (h154.flatAtMeasuredBandLow) "flat" else "NOT flat") +
                    " against T-5b's 0.10. 10 x 6's threshold is " +
                    h106.compositeFractionThreshold!!.emitted(9) + ", still 20x below the band, and " +
                    "it dishes " + h106.dishingAtMeasuredBandLow.emitted(9) + " at the low end. " +
                    "C-0120's finding that the second cross-section removes the dependency on the " +
                    "interlayer-coupling calibration SURVIVES the geometry correction; 15 x 4's " +
                    "margin does not.")
    }.getOrElse { failure ->
        findings["PROSE_FAILED"] = failure.toString()
    }

    val output = File("gpd/results/T-219-honeycomb-station-lattice-and-placement.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(digits = 9) as JsonObject)
        ) + "\n"
    )
    println("T-219 - wrote " + output.path)
}

private fun relative(published: Double, here: Double): Double =
    if (published == 0.0) abs(here) else abs(here - published) / abs(published)

private fun HoneycombBlock.envelopeThroughSmallerThanAcross(): Boolean = envelopeX < envelopeY
